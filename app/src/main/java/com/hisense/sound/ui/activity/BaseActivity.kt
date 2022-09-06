package com.hisense.sound.ui.activity

import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.ActivityInfo
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.gyf.immersionbar.ktx.immersionBar
import com.hisense.sound.R

/**
 * Created by 咸鱼至尊 on 2021/12/9
 *
 * desc: Activity基类 目前只用来实现状态栏沉浸|强制竖屏|转场动画效果
 */
open class BaseActivity : AppCompatActivity() {
    @SuppressLint("SourceLockedOrientationActivity")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //状态栏沉浸（设置透明底部导航栏，设置状态栏颜色，设置深色图标，最后调用）
        immersionBar {
            transparentNavigationBar()
            statusBarColor("#80000000")
            statusBarDarkFont(true)
            navigationBarDarkIcon(true)
            init()
        }
        //强制页面竖屏显示
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
    }

    @Deprecated("Deprecated in Java")
    @Suppress("DEPRECATION")
    override fun startActivityForResult(intent: Intent, requestCode: Int, options: Bundle?) {
        super.startActivityForResult(intent, requestCode, options)
        //转场动画效果
        overridePendingTransition(R.anim.right_in_activity, R.anim.right_out_activity)
    }

    override fun onDestroy() {
        super.onDestroy()
        //转场动画效果
        overridePendingTransition(R.anim.left_in_activity, R.anim.left_out_activity)
    }
}