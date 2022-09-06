package com.hisense.sound.ui.dialog

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.SystemClock
import android.text.TextUtils
import android.view.WindowManager
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import com.hisense.sound.R

/**
 * author : Android 轮子哥
 *
 * github : https://github.com/getActivity/AndroidProject-Kotlin
 *
 * time : 2018/12/2
 *
 * desc : 提示对话框
 */
class TipsDialog {

    companion object {
        const val ICON_FINISH: Int = R.drawable.tips_finish_ic
        const val ICON_ERROR: Int = R.drawable.tips_error_ic
        const val ICON_WARNING: Int = R.drawable.tips_warning_ic
    }

    class Builder(context: Context) : Dialog(context), Runnable {

        private val messageView: TextView? by lazy { findViewById(R.id.tv_tips_message) }
        private val iconView: ImageView? by lazy { findViewById(R.id.iv_tips_icon) }

        private var duration = 1000

        init {
            setContentView(R.layout.dialog_tips)
            window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            window?.setWindowAnimations(android.R.style.Animation_Toast)
            window?.clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND)
            setCancelable(false)
        }

        fun setIcon(@DrawableRes id: Int): Builder = apply {
            iconView?.setImageResource(id)
        }

        fun setDuration(duration: Int): Builder = apply {
            this.duration = duration
        }

        fun setMessage(@StringRes id: Int): Builder = apply {
            setMessage(context.getString(id))
        }

        fun setMessage(text: CharSequence?): Builder = apply {
            messageView?.text = text
        }

        override fun onCreate(savedInstanceState: Bundle?) {
            // 如果显示的图标为空就抛出异常
            requireNotNull(iconView?.drawable) { "The display type must be specified" }
            // 如果内容为空就抛出异常
            require(!TextUtils.isEmpty(messageView?.text.toString())) { "Dialog message not null" }
            super.onCreate(savedInstanceState)
        }

        override fun onStart() {
            super.onStart()
            // 延迟自动关闭
            Handler(Looper.getMainLooper()).postAtTime(this, SystemClock.uptimeMillis() + if (duration < 0) 0 else duration)
        }

        override fun run() {
            if (!isShowing) {
                return
            }
            dismiss()
        }
    }
}