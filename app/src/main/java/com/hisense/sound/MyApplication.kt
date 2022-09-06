package com.hisense.sound

import android.annotation.SuppressLint
import android.app.Application
import android.content.Context
import com.drake.net.NetConfig
import com.drake.net.okhttp.setConverter
import com.drake.statelayout.StateConfig
import com.hisense.sound.logic.dao.GsonConvert
import com.hjq.toast.ToastUtils
import okhttp3.OkHttpClient

/**
 * Created by 咸鱼至尊 on 2021/12/9
 *
 * desc: 全局Application对象
 */
class MyApplication : Application() {
    companion object {
        /** 全局context对象 */
        @SuppressLint("StaticFieldLeak")
        internal lateinit var context: Context
    }

    override fun onCreate() {
        super.onCreate()
        //延迟初始化全局context对象
        context = applicationContext
        //本地异常捕捉
        CrashHandler.register(this)
        //初始化Toast框架
        ToastUtils.init(this)
        //全局缺省页
        StateConfig.apply {
            emptyLayout = R.layout.layout_empty // 配置全局的空布局
            errorLayout = R.layout.layout_error // 配置全局的错误布局
            loadingLayout = R.layout.layout_loading // 配置全局的加载中布局
            setRetryIds(R.id.iv) // 全局的重试Id
        }
        //网络请求配置
        NetConfig.initialize("openapi.data-baker.com",
            null, fun OkHttpClient.Builder.() {
                //设置Gson解析方式
                setConverter(GsonConvert())
            })
    }
}