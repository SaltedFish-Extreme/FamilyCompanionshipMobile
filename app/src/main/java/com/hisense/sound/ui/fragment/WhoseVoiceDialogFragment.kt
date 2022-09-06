package com.hisense.sound.ui.fragment

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.content.res.AppCompatResources
import androidx.fragment.app.DialogFragment
import com.drake.serialize.intent.openActivity
import com.hisense.sound.R
import com.hisense.sound.ui.activity.VoiceTrainingActivity
import com.hisense.sound.widget.view.clickNoRepeat
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Created by 咸鱼至尊 on 2022/2/13
 *
 * desc: 选择声音DialogFragment
 */
class WhoseVoiceDialogFragment : DialogFragment() {

    private val dadVoice: TextView by lazy { requireView().findViewById<TextView>(R.id.dad_voice) }
    private val momVoice: TextView by lazy { requireView().findViewById<TextView>(R.id.mom_voice) }

    private val scope by lazy { MainScope() }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_dialog_whose_voice, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        //遍历保存过的模型列表key，有谁的声音，则修改对应选项的背景及不可点击
        /*AppConfig.ModelMap.keys.forEach {
            if (it == getString(R.string.dad)) {
                dadVoice.background = AppCompatResources.getDrawable(requireContext(), R.drawable.bg_dialog_option_radius_25_disable)
                dadVoice.isEnabled = false
            }
            if (it == getString(R.string.mom)) {
                momVoice.background = AppCompatResources.getDrawable(requireContext(), R.drawable.bg_dialog_option_radius_25_disable)
                momVoice.isEnabled = false
            }
        }*/
        //选中谁的声音，则跳转声音模型训练页开启训练，并传递其类别
        dadVoice.clickNoRepeat {
            dadVoice.background = AppCompatResources.getDrawable(requireContext(), R.drawable.bg_composite)
            dadVoice.setTextColor(resources.getColor(R.color.white, null))
            scope.launch {
                delay(500)
                openActivity<VoiceTrainingActivity>("type" to getString(R.string.dad))
                requireActivity().finish()
            }
        }
        momVoice.clickNoRepeat {
            momVoice.background = AppCompatResources.getDrawable(requireContext(), R.drawable.bg_composite)
            momVoice.setTextColor(resources.getColor(R.color.white, null))
            scope.launch {
                delay(500)
                openActivity<VoiceTrainingActivity>("type" to getString(R.string.mom))
                requireActivity().finish()
            }
        }
    }

    override fun onStart() {
        super.onStart()
        //背景透明
        requireDialog().window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
    }

    override fun onDestroy() {
        super.onDestroy()
        scope.cancel()
    }
}