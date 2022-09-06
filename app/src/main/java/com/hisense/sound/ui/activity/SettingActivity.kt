package com.hisense.sound.ui.activity

import android.os.Bundle
import android.widget.ImageView
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.drake.net.utils.withDefault
import com.drake.net.utils.withIO
import com.drake.serialize.intent.openActivity
import com.hisense.sound.R
import com.hisense.sound.logic.dao.AppConfig
import com.hisense.sound.logic.dao.HiUniversalServiceImpl
import com.hisense.sound.ui.adapter.MouldManageAdapter
import com.hisense.sound.ui.dialog.Dialog
import com.hisense.sound.util.ActivityCollector
import com.hisense.sound.widget.view.clickNoRepeat
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

class SettingActivity : BaseActivity() {

    private val comeback: ImageView by lazy { findViewById<ImageView>(R.id.comeback) }
    private val rv: RecyclerView by lazy { findViewById<RecyclerView>(R.id.rv) }
    private val exit: ImageView by lazy { findViewById<ImageView>(R.id.exit) }

    /** 适配器 */
    private val adapter by lazy { MouldManageAdapter() }

    private val scope by lazy { MainScope() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_setting)
        comeback.clickNoRepeat { finish() }
        exit.clickNoRepeat {
            Dialog.getConfirmDialog(this, "确定退出登录吗？") { _, _ ->
                scope.launch {
                    //调用退出登录接口
                    withIO {
                        HiUniversalServiceImpl.sign_out(this@SettingActivity, AppConfig.AccountToken)
                    }
                    //退出登录，置空用户信息、登录时间以及模型查询ID
                    withDefault {
                        AppConfig.AccountName = ""
                        AppConfig.AccountPassword = ""
                        AppConfig.AccountToken = ""
                        AppConfig.AccountCustomID = ""
                        AppConfig.requestLoginTime = 0L
                        AppConfig.QueryId = ""
                        openActivity<LoginActivity>()
                        ActivityCollector.finishAll()
                        finish()
                    }
                }
            }.show()
        }
        rv.layoutManager = GridLayoutManager(this, 2)
        adapter.setList(arrayListOf("妈妈的声音", "爸爸的声音"))
        rv.adapter = adapter
    }

    override fun onDestroy() {
        super.onDestroy()
        scope.cancel()
    }
}