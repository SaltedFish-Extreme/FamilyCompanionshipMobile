package com.hisense.sound.ui.activity

import android.os.Bundle
import android.view.animation.AnimationUtils
import androidx.constraintlayout.widget.ConstraintLayout
import com.drake.net.utils.withIO
import com.drake.serialize.intent.openActivity
import com.google.gson.Gson
import com.hisense.app.widget.ext.hideSoftKeyboard
import com.hisense.sound.R
import com.hisense.sound.logic.dao.AppConfig
import com.hisense.sound.logic.dao.HiUniversalServiceImpl
import com.hisense.sound.logic.model.LoginModel
import com.hisense.sound.util.InputTextManager
import com.hisense.sound.util.KLog
import com.hisense.sound.widget.view.*
import com.hjq.toast.ToastUtils
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/** 登录页面 */
class LoginActivity : BaseActivity() {

    private val layoutLogin: ConstraintLayout by lazy { findViewById<ConstraintLayout>(R.id.layout_login) }
    private val etLoginUsername: ClearEditText by lazy { findViewById<ClearEditText>(R.id.et_login_username) }
    private val etLoginPassword: PasswordEditText by lazy { findViewById<PasswordEditText>(R.id.et_login_password) }
    private val btnLogin: SubmitButton by lazy { findViewById<SubmitButton>(R.id.btn_login) }

    /** 协程作用域 */
    private val mainScope by lazy { MainScope() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        layoutLogin.setOnClickListener { hideSoftKeyboard(this) }
        //联动登陆按钮和账号密码输入框
        btnLogin.let {
            InputTextManager.with(this)
                .addView(etLoginUsername)
                .addView(etLoginPassword)
                .setMain(it)
                .build()
        }
        //登陆按钮点击事件
        btnLogin.clickNoRepeat {
            //隐藏输入法
            hideSoftKeyboard(this)
            mainScope.launch {
                //切换到IO线程发起请求
                withIO {
                    //延迟请求
                    delay(500)
                    //登录
                    val signIn =
                        HiUniversalServiceImpl.sign_in(this@LoginActivity, etLoginUsername.textStringTrim(), etLoginPassword.textStringTrim())
                    //请求返回不包含错误码即视为登录成功
                    if (!signIn.result.contains("errorCode")) {
                        //登录成功，更新UI
                        runOnUiThread {
                            //获取到成功返回的token
                            val fromJson = Gson().fromJson(signIn.result, LoginModel::class.java)
                            KLog.d(fromJson.token)
                            //保存下来token信息及CustomID唯一标识，模型查询ID
                            AppConfig.AccountToken = fromJson.token
                            AppConfig.AccountCustomID = fromJson.customerId
                            AppConfig.QueryId = fromJson.customerId
                            //登陆按钮显示成功
                            btnLogin.showSucceed()
                        }
                        //保存下来账号密码
                        AppConfig.AccountName = etLoginUsername.textStringTrim()
                        AppConfig.AccountPassword = etLoginPassword.textStringTrim()
                        //更新登录时间
                        AppConfig.requestLoginTime = System.currentTimeMillis()
                        //延迟一秒跳到主页
                        delay(1000)
                        openActivity<MainActivity>()
                        finish()
                    } else {
                        //登录失败，更新UI
                        runOnUiThread {
                            ToastUtils.show(R.string.login_fail_tip)
                            //登陆按钮显示失败
                            btnLogin.showError(2000)
                            //账号输入框加载动画效果
                            etLoginUsername.startAnimation(
                                AnimationUtils.loadAnimation(
                                    this@LoginActivity,
                                    R.anim.shake_anim
                                )
                            )
                            //密码输入框加载动画效果
                            etLoginPassword.startAnimation(
                                AnimationUtils.loadAnimation(
                                    this@LoginActivity,
                                    R.anim.shake_anim
                                )
                            )
                        }
                    }
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        //关闭协程作用域
        mainScope.cancel()
    }
}