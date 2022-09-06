package com.hisense.sound.ui.activity

import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import com.baker.engrave.lib.BakerVoiceEngraver
import com.baker.engrave.lib.callback.DetectCallback
import com.drake.serialize.intent.openActivity
import com.hisense.sound.R
import com.hisense.sound.widget.view.clickNoRepeat
import com.hjq.toast.ToastUtils

/** 噪音检测页面 */
class EnvironmentalDetectActivity : BaseActivity(), DetectCallback {

    private val comeback: ImageView by lazy { findViewById<ImageView>(R.id.comeback) }
    private val detectValue: TextView by lazy { findViewById<TextView>(R.id.tv_db_value) }
    private val resultTipText: TextView by lazy { findViewById<TextView>(R.id.tv_result) }
    private val detectButton: Button by lazy { findViewById<Button>(R.id.engrave_start) }

    private var BTN_TYPE = 0 //0=默认状态，检测未通过。1=检测通过，可以复刻。

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_environmental_detect)
        comeback.clickNoRepeat { finish() }
        //注册 环境噪音检测 回调
        BakerVoiceEngraver.getInstance().setDetectCallback(this)
        //检测按钮点击事件
        detectButton.clickNoRepeat {
            //0=默认状态，检测未通过。1=检测通过，可以复刻。
            if (BTN_TYPE == 0) {
                //返回结果=1，开启检测成功。否则开启检测失败，最有可能的原因是没有麦克风、写SD卡权限。
                val resultCode = BakerVoiceEngraver.getInstance().startDBDetection()
                if (resultCode != 1) {
                    ToastUtils.show(getString(R.string.open_detection_fail) + getString(R.string.not_grant_all_permissions))
                } else {
                    detectButton.isEnabled = false
                    resultTipText.text = getString(R.string.environmental_noise_detection)
                }
            } else {
                openActivity<RecordSoundActivity>()
                finish()
            }
        }
    }

    /**
     * 环境音量变化监听
     *
     * @param value 分贝值
     */
    override fun dbDetecting(value: Int) {
        detectValue.text = getString(R.string.decibel_value, value)
    }

    /**
     * 环境噪音检测结果回调
     *
     * @param result 结果（1：成功；0：失败）
     * @param value 分贝值
     */
    override fun dbDetectionResult(result: Boolean, value: Int) {
        detectButton.isEnabled = true
        detectValue.text = getString(R.string.decibel_value, value)
        if (value > 70) {
            resultTipText.text = getString(R.string.ambient_noise_detection_failed)
        } else if (value > 50) {
            resultTipText.text = getString(R.string.general_environment)
        } else {
            resultTipText.text = getString(R.string.quiet_environment)
        }
        if (result) {
            BTN_TYPE = 1
            detectButton.text = getString(R.string.start_copying)
        } else {
            BTN_TYPE = 0
            detectButton.text = getString(R.string.detect_again)
        }
    }

    /**
     * 环境噪音检测错误回调
     *
     * @param errorCode 错误码
     * @param message 错误信息
     */
    override fun onDetectError(errorCode: Int, message: String?) {
        runOnUiThread {
            //更新UI，重新进行检测
            if (errorCode == 90013) {
                //因音频焦点丢失或电话等异常中断检测
                detectButton.isEnabled = true
                detectValue.text = getString(R.string.db_0)
                BTN_TYPE = 0
                resultTipText.text = getString(R.string.detect_interruption)
                detectButton.text = getString(R.string.detect_again)
            } else {
                ToastUtils.show(message)
            }
        }
    }
}