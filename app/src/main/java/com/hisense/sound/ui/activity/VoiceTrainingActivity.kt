package com.hisense.sound.ui.activity

import android.os.Bundle
import com.baker.engrave.lib.BakerVoiceEngraver
import com.baker.engrave.lib.callback.UploadRecordsCallback
import com.drake.channel.sendTag
import com.drake.net.Post
import com.drake.net.utils.scopeNetLife
import com.drake.serialize.intent.bundle
import com.hisense.sound.R
import com.hisense.sound.logic.dao.AppConfig
import com.hisense.sound.logic.dao.NetApi
import com.hisense.sound.logic.model.NoDataResponse
import com.hjq.toast.ToastUtils
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/** 声音训练页面 */
class VoiceTrainingActivity : BaseActivity(), UploadRecordsCallback {

    //传递过来的模型发音人类别
    private val type: String by bundle()

    private val scope by lazy { MainScope() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_voice_training)
        //注册 上传录音训练模型 回调
        BakerVoiceEngraver.getInstance().setUploadRecordsCallback(this)
        //开启声音模型训练
        BakerVoiceEngraver.getInstance().finishRecords("", "")
    }

    /**
     * 上传录音接口回调
     *
     * @param result 是否成功
     * @param mouldId 返回的模型ID
     */
    override fun uploadRecordsResult(result: Boolean, mouldId: String?) {
        //同步UI线程
        runOnUiThread {
            //上传成功
            if (result) {
                //TODO 此处应该将mouldId与用户的关系储存维护起来，此后要体验声音，都需要用到mouldId。
                if (!mouldId.isNullOrBlank()) {
                    //服务器添加模型
                    scopeNetLife {
                        Post<NoDataResponse>(NetApi.AddUserModelInfoAPI) {
                            json(
                                "accessToken" to AppConfig.AccountToken,
                                "userAccount" to AppConfig.AccountCustomID,
                                "userModelType" to type,
                                "userModelId" to mouldId
                            )
                        }.await()
                        ToastUtils.debugShow("添加成功")
                        sendTag("tag_turn_on_voice_training")
                    }.catch {
                        ToastUtils.show("添加失败，请重新录制声音")
                    }
                }
            } else {
                BakerVoiceEngraver.getInstance().recordInterrupt()
                ToastUtils.show(R.string.re_recording_prompt)
            }
            scope.launch {
                delay(1500)
                finish()
            }
        }
    }

    /**
     * 上传录音失败回调
     *
     * @param errorCode 错误码
     * @param message 返回信息
     */
    override fun onUploadError(errorCode: Int, message: String?) {
        scope.launch {
            ToastUtils.show(R.string.re_recording_prompt)
            BakerVoiceEngraver.getInstance().recordInterrupt()
            delay(1500)
            finish()
        }
    }

    override fun onBackPressed() {
        return
    }

    override fun onDestroy() {
        super.onDestroy()
        scope.cancel()
    }

}