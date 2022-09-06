package com.hisense.sound.ui.activity

import android.content.DialogInterface
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.content.res.AppCompatResources
import com.baker.engrave.lib.BakerVoiceEngraver
import com.baker.engrave.lib.callback.ContentTextCallback
import com.baker.engrave.lib.callback.PlayListener
import com.baker.engrave.lib.callback.RecordCallback
import com.bumptech.glide.Glide
import com.drake.serialize.intent.openActivity
import com.hisense.sound.R
import com.hisense.sound.ui.dialog.Dialog
import com.hisense.sound.ui.dialog.WaitDialog
import com.hisense.sound.util.KLog
import com.hisense.sound.widget.view.*
import com.hjq.toast.ToastUtils
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/** 录音页面 */
class RecordSoundActivity : BaseActivity() {

    private val comeback: ImageView by lazy { findViewById<ImageView>(R.id.comeback) }
    private val tvIndex: TextView by lazy { findViewById<TextView>(R.id.tv_index_value) }
    private val tvTotal: TextView by lazy { findViewById<TextView>(R.id.tv_index_total) }
    private val tvContentText: TextView by lazy { findViewById<TextView>(R.id.tv_content) }
    private val tvTips: TextView by lazy { findViewById<TextView>(R.id.tv_recognize_result) }
    private val btnPlay: ImageView by lazy { findViewById<ImageView>(R.id.btnPlay) }
    private val tvRecordStart: TextView by lazy { findViewById<TextView>(R.id.tv_record_start) }
    private val ivRecordStart: ImageView by lazy { findViewById<ImageView>(R.id.iv_record_start) }
    private val llRecordStart: LinearLayout by lazy { findViewById<LinearLayout>(R.id.ll_record_start) }
    private val flPre: FrameLayout by lazy { findViewById<FrameLayout>(R.id.flPre) }
    private val flNext: FrameLayout by lazy { findViewById<FrameLayout>(R.id.flNext) }
    private val ivNext: ImageView by lazy { findViewById<ImageView>(R.id.iv_next) }
    private val tvNext: TextView by lazy { findViewById<TextView>(R.id.tv_next) }

    private val scope by lazy { MainScope() }

    private var contentTexts: Array<String> = arrayOf()//文本总数量
    private var currentIndex = 0//当前录音索引
    private var startOrEnd = true//开始或结束录音

    /** 等待加载框 */
    private val dialog by lazy { WaitDialog.Builder(this, true).setMessage(getString(R.string.playing)) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_record_sound)
        comeback.clickNoRepeat {
            //返回按钮点击显示退出录音弹窗
            Dialog.getConfirmDialog(this, getString(R.string.quit_record_tip)) { _, _ ->
                //非常建议在录音过程中异常退出的话，调用此方法通知服务器，这样的话会及时释放当前训练模型所占用的名额。
                BakerVoiceEngraver.getInstance().recordInterrupt()
                finish()
            }.show()
        }
        //隐藏录音中提示
        tvTips.visibility = View.INVISIBLE
        //按钮点击事件
        setOnclickNoRepeat(flPre, flNext, btnPlay, llRecordStart) {
            when (it.id) {
                R.id.ll_record_start -> {
                    if (startOrEnd) {
                        // 开始录音
                        val result = BakerVoiceEngraver.getInstance().startRecord(currentIndex)
                        // 0=mouldId为空，1=无权限，2=开启成功
                        KLog.d(result.toString())
                        //点击录音，应修改试听按钮UI为不可播放（适用重新录音）
                        btnPlay.setImageDrawable(AppCompatResources.getDrawable(this, R.drawable.btn_record_play_disabled))
                    } else {
                        // 结束录音上传  0=mouldId为空, 1=结束成功，开始上传识别。
                        BakerVoiceEngraver.getInstance().endRecord()
                    }
                }
                R.id.flPre -> {
                    toPre()
                }
                R.id.flNext -> {
                    toNext()
                }
                R.id.btnPlay -> {
                    if (BakerVoiceEngraver.getInstance().isRecord(currentIndex)) {
                        toPlay()
                    }
                }
            }
        }
        //TODO 注意设置回调在调用对应方法之前。
        initCallback()
        //QueryId的作用是xxx，非常建议设置，如果在初始化时已经填写了QueryId，此方法不必重复设置。
        //如果要上传QueryID，请务必在调用getVoiceMouldId()方法之前调用。
        //BakerVoiceEngraver.getInstance().setQueryId(AppConfig.QueryId)
        BakerVoiceEngraver.getInstance().getTextList() //获取文本列表，回调在callback里
        BakerVoiceEngraver.getInstance().getVoiceMouldId() //获取，不知道获取的啥
        //返回键监听，显示确认退出弹窗
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                Dialog.getConfirmDialog(this@RecordSoundActivity, getString(R.string.quit_record_tip)) { dialog, _ ->
                    //非常建议在录音过程中异常退出的话，调用此方法通知服务器，这样的话会及时释放当前训练模型所占用的名额。
                    BakerVoiceEngraver.getInstance().recordInterrupt()
                    finish()
                }.show()
            }
        })
    }

    /** 初始化接口回调 */
    private fun initCallback() {
        //获取文本内容接口回调
        BakerVoiceEngraver.getInstance().setContentTextCallback(object : ContentTextCallback {
            //获取文本列表成功，更新UI状态
            override fun contentTextList(strList: Array<String>) {
                runOnUiThread {
                    if (strList.isNotEmpty()) {
                        contentTexts = strList
                        tvIndex.text = "${currentIndex + 1}"
                        tvTotal.text = "${contentTexts.size}"
                        tvContentText.text = contentTexts[0]
                        currentIndex = 0
                    }
                }
            }

            //失败则记录日志
            override fun onContentTextError(errorCode: Int, message: String) {
                KLog.e("onContentTextError errorCode = $errorCode message = $message")
            }
        })

        //录音|识别|质量检测接口回调
        BakerVoiceEngraver.getInstance().setRecordCallback(object : RecordCallback {
            // 1=录音中， 2=识别中， 3=最终结果：通过， 4=最终结果：不通过
            override fun recordsResult(typeCode: Int, recognizeResult: Int) {
                runOnUiThread {
                    when (typeCode) {
                        1 -> {
                            tvTips.text = getString(R.string.recording)
                            tvTips.visibility = View.VISIBLE
                            startOrEnd = false
                            tvRecordStart.text = getString(R.string.click_start_recognition)
                            tvRecordStart.isEnabled = true
                            //录音切换到动态UI图
                            Glide.with(this@RecordSoundActivity)
                                .load(AppCompatResources.getDrawable(this@RecordSoundActivity, R.drawable.ic_record_flag_dynamic))
                                .into(ivRecordStart)
                        }
                        2 -> {
                            tvTips.text = getString(R.string.identifying)
                            tvTips.visibility = View.VISIBLE
                            tvRecordStart.isEnabled = false
                            //识别切换回静态UI图
                            Glide.with(this@RecordSoundActivity)
                                .load(AppCompatResources.getDrawable(this@RecordSoundActivity, R.drawable.ic_record_flag_normal))
                                .into(ivRecordStart)
                        }
                        3 -> {
                            tvTips.text = getString(R.string.string_recognize_success, recognizeResult)
                            tvTips.visibility = View.VISIBLE
                            startOrEnd = true
                            toNext()
                        }
                        4 -> {
                            tvTips.text = getString(R.string.string_recognize_fail, recognizeResult)
                            tvTips.visibility = View.VISIBLE
                            startOrEnd = true
                            tvRecordStart.isEnabled = true
                            tvRecordStart.text = getString(R.string.re_record)
                        }
                    }
                }
            }

            //录音音量监听接口回调，可更新UI
            override fun recordVolume(volume: Int) {}

            //录音失败接口回调，更新UI，重新录音
            override fun onRecordError(errorCode: Int, message: String) {
                runOnUiThread {
                    KLog.e("errorCode=$errorCode, message=$message")
                    ToastUtils.debugShow(message)
                    startOrEnd = true
                    tvTips.text = getString(R.string.record_error)
                    tvTips.visibility = View.VISIBLE
                    tvRecordStart.text = getString(R.string.re_record)
                    tvRecordStart.isEnabled = true
                }
            }
        })
    }

    /** 上一条 */
    private fun toPre() {
        if (currentIndex > 0) {
            currentIndex--
            updateView()
        } else {
            ToastUtils.show(R.string.first_item)
        }
    }

    /** 下一条 */
    private fun toNext() {
        //判断当前这条是否成功录制
        if (BakerVoiceEngraver.getInstance().isRecord(currentIndex)) {
            //如果全部录制完成，则显示完成文本图标
            if (currentIndex >= contentTexts.size - 1) {
                tvTips.text = getString(R.string.record_finish)
                tvNext.text = getString(R.string.finish)
                ivNext.setImageDrawable(AppCompatResources.getDrawable(this, R.drawable.ic_record_finish))
                //上一条及录音不可用，可听本段录音
                flPre.isEnabled = false
                llRecordStart.isEnabled = false
                btnPlay.setImageDrawable(AppCompatResources.getDrawable(this, R.drawable.btn_record_play))
                //下一条（完成）点击打开输入声音类别弹窗
                flNext.clickNoRepeat {
                    val editText = ClearEditText(this)
                    editText.setSingleLine()
                    editText.gravity = Gravity.CENTER
                    editText.setTextColor(resources.getColor(R.color.black, null))
                    editText.textSize = 20F
                    editText.setInputRegex(RegexEditText.REGEX_NAME)
                    val inputDialog = AlertDialog.Builder(this)
                    inputDialog.setTitle("请输入发音人").setIcon(R.drawable.info).setView(editText)
                    inputDialog.setPositiveButton("确定") { _: DialogInterface?, _: Int ->
                        openActivity<VoiceTrainingActivity>("type" to editText.textString())
                        finish()
                    }.show()
                }
            } else {
                //否则跳转下一条，更新UI
                currentIndex++
                updateView()
            }
        } else {
            ToastUtils.show(R.string.current_recording_not_successfully)
        }
    }

    /** 更新视图 */
    private fun updateView() {
        //跳转故事文本后，过0.5s隐藏提示框
        scope.launch {
            delay(500)
            tvTips.visibility = View.INVISIBLE
        }
        tvRecordStart.isEnabled = true
        //如果当前条录制成功
        if (BakerVoiceEngraver.getInstance().isRecord(currentIndex)) {
            tvRecordStart.text = getString(R.string.re_record)
            //更新试听按钮UI为可播放
            btnPlay.setImageDrawable(AppCompatResources.getDrawable(this, R.drawable.btn_record_play))
        } else {
            tvRecordStart.text = getString(R.string.start_recording)
            //更新试听按钮UI为不可播放
            btnPlay.setImageDrawable(AppCompatResources.getDrawable(this, R.drawable.btn_record_play_disabled))
        }
        //更新索引及文本
        tvIndex.text = "${currentIndex + 1}"
        tvContentText.text = contentTexts[currentIndex]
    }

    /** 试听 */
    private fun toPlay() {
        BakerVoiceEngraver.getInstance().startPlay(currentIndex, object : PlayListener {
            override fun playStart() {
                KLog.d(getString(R.string.audition_begins))
                showProgressDialog()
            }

            override fun playEnd() {
                ToastUtils.show(R.string.audition_over)
                disMissProgressDialog()
            }

            override fun playError(e: Exception) {
                KLog.d("${getString(R.string.audition_error)}${e.localizedMessage}")
                disMissProgressDialog()
            }
        })
    }

    /** 显示进度条 */
    fun showProgressDialog() {
        runOnUiThread {
            dialog.show()
            dialog.setOnDismissListener { BakerVoiceEngraver.getInstance().stopPlay() }
            dialog.setOnCancelListener { BakerVoiceEngraver.getInstance().stopPlay() }
        }
    }

    /** 取消进度条显示 */
    fun disMissProgressDialog() {
        runOnUiThread {
            dialog.dismiss()
        }
    }
}