package com.hisense.sound.ui.activity

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.animation.AlphaAnimation
import android.view.animation.Animation
import androidx.constraintlayout.widget.ConstraintLayout
import com.baker.engrave.lib.BakerVoiceEngraver
import com.baker.engrave.lib.callback.InitListener
import com.drake.serialize.intent.openActivity
import com.hisense.sound.MyApplication
import com.hisense.sound.R
import com.hisense.sound.logic.dao.AppConfig
import com.hjq.permissions.OnPermissionCallback
import com.hjq.permissions.Permission
import com.hjq.permissions.XXPermissions
import com.hjq.toast.ToastUtils
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/** 闪屏页 */
@SuppressLint("CustomSplashScreen")
class SplashActivity : BaseActivity() {

    //初始化协程作用域
    private val mainScope by lazy { MainScope() }
    private val layoutSplash: ConstraintLayout by lazy { findViewById(R.id.layout_splash) }
    private val alphaAnimation: AlphaAnimation by lazy { AlphaAnimation(0.3F, 1.0F) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)
        //动画效果
        alphaAnimation.run {
            //动画持续时间
            duration = 1200
            setAnimationListener(object : Animation.AnimationListener {
                override fun onAnimationRepeat(p0: Animation?) {}

                override fun onAnimationEnd(p0: Animation?) {
                }

                override fun onAnimationStart(p0: Animation?) {
                    XXPermissions.with(this@SplashActivity)
                        //申请权限
                        .permission(Permission.RECORD_AUDIO, Permission.MANAGE_EXTERNAL_STORAGE)
                        .request(object : OnPermissionCallback {
                            override fun onGranted(permissions: MutableList<String>, all: Boolean) {
                                if (all) {
                                    mainScope.launch {
                                        //初始化复刻SDK
                                        BakerVoiceEngraver.getInstance()
                                            .initSDK(this@SplashActivity, AppConfig.APIKey, AppConfig.APISecret, null, object :
                                                InitListener {
                                                //成功跳转主页
                                                override fun onInitSuccess() {
                                                    mainScope.launch {
                                                        delay(500)
                                                        jumpToMain()
                                                    }
                                                }

                                                //失败弹出吐司关闭页面
                                                override fun onInitError(p0: Exception?) {
                                                    ToastUtils.show(R.string.init_sdk_error)
                                                    ToastUtils.debugShow(p0?.localizedMessage)
                                                    mainScope.launch {
                                                        delay(2000)
                                                        finish()
                                                    }
                                                }
                                            })
                                    }
                                } else {
                                    ToastUtils.show(R.string.not_grant_all_permissions)
                                    mainScope.launch {
                                        delay(1500)
                                        finish()
                                    }
                                }
                            }

                            override fun onDenied(permissions: MutableList<String>, never: Boolean) {
                                if (never) {
                                    ToastUtils.show(R.string.ever_deny_grant_permissions)
                                    // 如果是被永久拒绝就跳转到应用权限系统设置页面
                                    XXPermissions.startPermissionActivity(MyApplication.context, permissions)
                                } else {
                                    mainScope.launch {
                                        ToastUtils.show(R.string.not_grant_partial_permissions)
                                        delay(1500)
                                        finish()
                                    }
                                }
                            }
                        })
                }
            })
        }
        //加载内容视图动画效果
        layoutSplash.startAnimation(alphaAnimation)
    }

    /** 跳转主页 */
    fun jumpToMain() {
        //跳转主页并销毁该页面
        if (AppConfig.AccountName.isBlank()) {
            openActivity<LoginActivity>()
        } else {
            openActivity<MainActivity>()
        }
        finish()
        //转场结束动画效果
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
    }

    override fun onDestroy() {
        super.onDestroy()
        //销毁时关闭协程作用域
        mainScope.cancel()
    }
}