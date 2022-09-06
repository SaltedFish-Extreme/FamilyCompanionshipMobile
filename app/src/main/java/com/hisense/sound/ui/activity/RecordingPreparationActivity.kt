package com.hisense.sound.ui.activity

import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import com.drake.serialize.intent.openActivity
import com.hisense.sound.R
import com.hisense.sound.widget.view.clickNoRepeat

/** 录音准备页面 */
class RecordingPreparationActivity : BaseActivity() {

    private val comeback: ImageView by lazy { findViewById<ImageView>(R.id.comeback) }
    private val button: Button by lazy { findViewById<Button>(R.id.button) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_recording_preparation)
        comeback.clickNoRepeat { finish() }
        button.clickNoRepeat {
            //跳转环境声音检测页面
            openActivity<EnvironmentalDetectActivity>()
            finish()
        }
    }
}