package com.hisense.sound.ui.fragment

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.fragment.app.DialogFragment
import com.baker.engrave.lib.BakerVoiceEngraver
import com.hisense.sound.R
import com.hisense.sound.widget.view.clickNoRepeat

/**
 * Created by 咸鱼至尊 on 2022/2/13
 *
 * desc: 确认退出DialogFragment
 */
class ConfirmQuitDialogFragment : DialogFragment() {

    private val quitSure: ImageView by lazy { requireView().findViewById<ImageView>(R.id.quit_sure) }
    private val quitCancel: ImageView by lazy { requireView().findViewById<ImageView>(R.id.quit_cancel) }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_dialog_confirm_quit, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        quitSure.clickNoRepeat {
            //非常建议在录音过程中异常退出的话，调用此方法通知服务器，这样的话会及时释放当前训练模型所占用的名额。
            BakerVoiceEngraver.getInstance().recordInterrupt()
            this.dismiss()
            requireActivity().finish()
        }
        quitCancel.clickNoRepeat { this.dismiss() }
    }

    override fun onStart() {
        super.onStart()
        //背景透明
        requireDialog().window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
    }
}