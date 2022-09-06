package com.hisense.sound.ui.activity

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.ImageView
import androidx.appcompat.content.res.AppCompatResources
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.baker.engrave.lib.BakerVoiceEngraver
import com.baker.engrave.lib.bean.Mould
import com.baker.engrave.lib.callback.MouldCallback
import com.drake.channel.receiveTagLive
import com.drake.net.Get
import com.drake.net.utils.scopeNetLife
import com.drake.net.utils.withIO
import com.drake.serialize.intent.openActivity
import com.drake.statelayout.StateLayout
import com.google.gson.Gson
import com.hisense.sound.R
import com.hisense.sound.logic.dao.AppConfig
import com.hisense.sound.logic.dao.HiUniversalServiceImpl
import com.hisense.sound.logic.model.BannerModel
import com.hisense.sound.logic.model.LoginModel
import com.hisense.sound.logic.model.MouldModel
import com.hisense.sound.logic.model.TokenModel
import com.hisense.sound.ui.adapter.BannerItemAdapter
import com.hisense.sound.ui.adapter.MouldAdapter
import com.hisense.sound.util.ActivityCollector
import com.hisense.sound.util.KLog
import com.hisense.sound.widget.view.clickNoRepeat
import com.hisense.sound.widget.view.notNull
import com.hjq.toast.ToastUtils
import com.youth.banner.Banner
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/** 主页面 */
class MainActivity : BaseActivity() {

    private val state: StateLayout by lazy { findViewById<StateLayout>(R.id.state) }
    private val rv: RecyclerView by lazy { findViewById<RecyclerView>(R.id.rv) }
    private val addVoice: Button by lazy { findViewById<Button>(R.id.add_voice) }
    private val banner: Banner<BannerModel, BannerItemAdapter> by lazy { findViewById(R.id.banner) }
    private val ivGotoSetting: ImageView by lazy { findViewById<ImageView>(R.id.iv_goto_setting) }

    /** 协程作用域 */
    private val scope by lazy { MainScope() }

    /** 适配器 */
    private val adapter: MouldAdapter by lazy { MouldAdapter() }

    /** 退出时间 */
    private var exitTime = 0L

    /** 声音复刻SDK服务器返回的模型集合（key：模型ID，value：训练状态） */
    private val mouldMap by lazy { mutableMapOf<String, Int>() }

    @SuppressLint("NotifyDataSetChanged")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        ActivityCollector.addActivity(this)
        //登录过，并且已经超过了两天半，则重新登录 todo 后续使用refreshToken刷新登录
        if (AppConfig.AccountName.isNotBlank() && System.currentTimeMillis() - AppConfig.requestLoginTime > AppConfig.LoginIntervals) {
            scope.launch {
                withIO {
                    val signIn = HiUniversalServiceImpl.sign_in(this@MainActivity, AppConfig.AccountName, AppConfig.AccountPassword)
                    if (!signIn.result.contains("errorCode")) {
                        val fromJson = Gson().fromJson(signIn.result, LoginModel::class.java)
                        KLog.d(fromJson.token)
                        //刷新token信息
                        AppConfig.AccountToken = fromJson.token
                        //更新登录时间
                        AppConfig.requestLoginTime = System.currentTimeMillis()
                    }
                }
            }
        }
        ToastUtils.debugShow(AppConfig.AccountToken)
        //设置声音复刻服务的查询id
        BakerVoiceEngraver.getInstance().setQueryId(AppConfig.QueryId)
        try {
            //进来后没有初始化过媒体播放器则置空持久化的音频名称与发音人（e.g. 播放时清后台杀死APP，重新进来也应置空）
            AppConfig.mediaPlayer.notNull({}) {
                AppConfig.PlayMusicName = ""
                AppConfig.PlayMusicPerson = ""
            }
        } catch (e: Exception) {
            e.localizedMessage?.let { KLog.e(it) } ?: ToastUtils.debugShow(e)
            AppConfig.PlayMusicName = ""
            AppConfig.PlayMusicPerson = ""
        }
        //注册 获取模型列表 回调
        BakerVoiceEngraver.getInstance().setMouldCallback(mouldCallback)
        //设置recyclerView布局管理器（网格/3列）
        rv.layoutManager = GridLayoutManager(this, 3)
        //缺省页自动刷新
        state.onRefresh {
            //清除背景，(因为请求速度太快，让动画效果飞一会~)，获取QueryId所对应的模型列表
            scope.launch {
                state.background = null
                delay(1000)
                //从云服务器查询模型数据
                /*scopeNetLife {
                    val userModelInfo = Post<ApiResponse<ApiPagerResponse<ArrayList<UserModelInfoResponse>>>>(NetApi.GetUserModelInfoAPI) {
                        json("accessToken" to AppConfig.AccountToken, "userAccount" to AppConfig.AccountCustomID)
                    }.await()
                    if (userModelInfo.data.userModelInfoList.isEmpty()) {
                        ToastUtils.show("暂无数据")
                    } else {
                        userModelInfo.data.userModelInfoList.forEach {
                            adapter.addData(MouldModel(it.userModelId, it.userModelType, it.trainStatus))
                        }
                    }
                }.catch {
                    ToastUtils.show(it.localizedMessage)
                }*/
                //从标贝服务器获取该查询ID下的模型信息
                BakerVoiceEngraver.getInstance().getMouldList(1, 10, AppConfig.QueryId)
            }
        }.refreshing()
        //右上图标转到设置页
        ivGotoSetting.clickNoRepeat { openActivity<SettingActivity>() }
        //使用自定义的banner适配器并设置数据
        banner.setAdapter(BannerItemAdapter(getBannerData()))
        //添加生命周期管理
        banner.addBannerLifecycleObserver(this)
        //添加声音按钮点击事件
        addVoice.clickNoRepeat {
            if (adapter.data.size >= 2) {
                ToastUtils.show(R.string.voice_run_out_tip)
                return@clickNoRepeat
            }
            openActivity<RecordingPreparationActivity>()
        }
        //获取访问令牌(第一次进来和每隔18小时请求一次)
        if (AppConfig.AccessToken.isBlank() || System.currentTimeMillis() - AppConfig.requestTokenTime > AppConfig.requestIntervals) {
            scopeNetLife {
                val tokenModel =
                    Get<TokenModel>("${AppConfig.GenerateTokenAPI}?grant_type=${AppConfig.GrantType}&client_id=${AppConfig.APIKey}&client_secret=${AppConfig.APISecret}").await()
                //更新令牌信息及请求时间
                AppConfig.AccessToken = tokenModel.access_token
                AppConfig.requestTokenTime = System.currentTimeMillis()
                KLog.d(AppConfig.AccessToken)
            }
        }
        //收到播放|停止音乐事件后，整体刷新适配器
        receiveTagLive("tag_play_start", "tag_play_end")
        {
            adapter.notifyDataSetChanged()
        }
        //收到开启训练声音事件，整体刷新缺省页布局，服务器同步更新
        receiveTagLive("tag_turn_on_voice_training")
        {
            state.refresh()
        }
    }

    /**
     * 获取banner数据
     *
     * @return 轮播图适配器数据
     */
    private fun getBannerData(): List<BannerModel> {
        val list: MutableList<BannerModel> = ArrayList()
        list.add(
            BannerModel(
                AppCompatResources.getDrawable(this, R.drawable.bg_banner_card_woman)!!,
                getString(R.string.female_voice),
                getString(R.string.female_voice_desc),
                ""
            )
        )
        list.add(
            BannerModel(
                AppCompatResources.getDrawable(this, R.drawable.bg_banner_card_man)!!,
                getString(R.string.male_voice),
                getString(R.string.male_voice_desc),
                ""
            )
        )
        list.add(
            BannerModel(
                AppCompatResources.getDrawable(this, R.drawable.bg_banner_card_child)!!,
                getString(R.string.child_voice),
                getString(R.string.child_voice_desc),
                ""
            )
        )
        return list
    }

    private val mouldCallback: MouldCallback = object : MouldCallback() {
        /**
         * 获取模型列表错误回调
         *
         * @param errorCode 错误码
         * @param message 错误信息
         */
        override fun onMouldError(errorCode: Int, message: String) {
            Log.e("ExperienceFragment", "errorCode==$errorCode, message=$message")
            //清空缺省页背景，并显示错误缺省页
            runOnUiThread {
                state.background = null
                state.showError()
            }
        }

        /**
         * 模型信息回调
         *
         * @param mould 模型
         */
        override fun mouldInfo(mould: Mould) {
            KLog.d("status=" + mould.modelStatus + ", statusName=" + mould.statusName)
        }

        /**
         * 模型列表回调
         *
         * @param list
         */
        /*override fun mouldList(list: List<Mould>) {
            runOnUiThread {
                //如果该账号下有模型数据（已训练过）
                if (list.isNotEmpty()) {
                    list.forEach {
                        //将该模型的ID及训练状态保存到map集合中
                        mouldMap[it.modelId] = it.modelStatus
                    }
                    adapter.data.forEach {
                        //遍历数据源集合，判断如果键相同，并且值不相同，则修改该值的状态
                        if (mouldMap.containsKey(it.mouldId) && mouldMap[it.mouldId] != it.training) {
                            adapter.remove(it)
                            adapter.addData(MouldModel(it.mouldId, it.type, mouldMap[it.mouldId]!!))
                            //服务器修改模型信息
                            scopeNetLife {
                                Post<NoDataResponse>(NetApi.UpdateUserModelInfoAPI) {
                                    json(
                                        "accessToken" to AppConfig.AccountToken,
                                        "userAccount" to AppConfig.AccountCustomID,
                                        "userModelId" to it.mouldId,
                                        "trainStatus" to mouldMap[it.mouldId]
                                    )
                                }.await()
                                ToastUtils.debugShow("修改成功")
                            }.catch {
                                ToastUtils.debugShow(it.localizedMessage)
                            }
                        }
                    }
                    //设置适配器
                    rv.adapter = adapter
                    //显示缺省页背景
                    state.background = AppCompatResources.getDrawable(this@MainActivity, R.drawable.bg_item_rv_radius_20)
                } else {
                    //否则显示空背景
                    state.background = AppCompatResources.getDrawable(this@MainActivity, R.drawable.bg_home_rv)
                }
                //显示内容
                state.showContent()
            }
        }*/
        override fun mouldList(list: List<Mould>) {
            //todo 此处后续根据账号系统进行云端同步模型数据，暂时不做保存，先写死几个模型
            runOnUiThread {
                //todo 为了测试方便，本地模型集合为空，则默认写入一个模型进集合
                if (AppConfig.ModelMap.isEmpty()) {
                    AppConfig.ModelMap.run {
                        this[getString(R.string.mom)] = "f57a4f934604c8259fd5f177c03b0e9e2ucxi"
                        AppConfig.ModelMap = this
                    }
                }
                //获取模型集合数据
                val mouldList = AppConfig.ModelMap.map { MouldModel(it.value, it.key, 6) }
                //适配器设置数据
                adapter.setList(mouldList)
                //设置适配器
                rv.adapter = adapter
                //显示缺省页背景
                state.background = AppCompatResources.getDrawable(this@MainActivity, R.drawable.bg_item_rv_radius_20)
                //如果该账号下有模型数据（已训练过）
                if (list.isNotEmpty()) {
                    //添加模型进集合列表
                    //val mouldList = list.map { MouldModel(it.modelId, getKey(AppConfig.ModelMap, it.modelId), it.statusName) }
                    //获取模型集合数据
                    //val mouldList = AppConfig.ModelMap.map { MouldModel(it.value, it.key, false) }
                    //适配器设置数据
                    //adapter.addData(mouldList)
                    //todo 这里为了测试先将第一次提交训练的模型添加到适配器里
                    list[0].run {
                        adapter.addData(MouldModel(modelId, getString(R.string.dad), modelStatus))
                    }
                    //设置适配器
                    rv.adapter = adapter
                    //显示缺省页背景
                    state.background = AppCompatResources.getDrawable(this@MainActivity, R.drawable.bg_item_rv_radius_20)
                } else {
                    //否则显示空背景
                    //state.background = AppCompatResources.getDrawable(this@MainActivity, R.drawable.bg_home_rv)
                }
                //显示内容
                state.showContent()
            }
        }
    }

    override fun onBackPressed() {
        //返回键退出程序确认
        if (System.currentTimeMillis() - exitTime > 2000) {
            ToastUtils.show(R.string.exit)
            exitTime = System.currentTimeMillis()
            return
        }
        super.onBackPressed()
    }

    override fun onDestroy() {
        super.onDestroy()
        //关闭协程作用域
        scope.cancel()
    }
}