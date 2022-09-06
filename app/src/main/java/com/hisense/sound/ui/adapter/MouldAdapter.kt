package com.hisense.sound.ui.adapter

import android.annotation.SuppressLint
import android.graphics.drawable.ColorDrawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.PopupWindow
import android.widget.TextView
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.drake.serialize.intent.openActivity
import com.hisense.sound.R
import com.hisense.sound.logic.dao.AppConfig
import com.hisense.sound.logic.model.MouldModel
import com.hisense.sound.ui.activity.ReadStoryActivity
import com.hisense.sound.widget.view.visibleOrGone
import com.hjq.toast.ToastUtils

/**
 * Created by 咸鱼至尊 on 2021/12/20
 *
 * desc: 模型列表适配器
 */
class MouldAdapter : BaseAdapter<MouldModel>(R.layout.item_mould) {

    init {
        this.setOnItemClickListener { _, _, position ->
            //声音模型训练中，不允许跳转故事播放页面
            if (data[position].training != 6) {
                ToastUtils.show(R.string.wait_training_finish)
                return@setOnItemClickListener
            }
            //跳转故事播放页面，传递模型ID及发音人名称
            context.openActivity<ReadStoryActivity>("mould" to data[position].mouldId, "person" to data[position].type)
        }
    }

    override fun onItemViewHolderCreated(viewHolder: BaseViewHolder, viewType: Int) {
        super.onItemViewHolderCreated(viewHolder, viewType)
        /*viewHolder.getView<LinearLayout>(R.id.ll_mould_bg).setOnLongClickListener {
            initPopWindow(it)
            return@setOnLongClickListener true
        }*/
    }

    override fun convert(holder: BaseViewHolder, item: MouldModel) {
        //根据训练状态显示隐藏训练中蒙板
        holder.getView<TextView>(R.id.tv_mould_training).visibleOrGone(item.training != 6)
        //设置声音所属类型
        holder.setText(R.id.tv_mould_type, item.type)
        //设置声音背景颜色
        when (item.type) {
        "爸爸" -> holder.getView<LinearLayout>(R.id.ll_mould_bg).setBackgroundColor(context.resources.getColor(R.color.dad_color, null))
        "妈妈" -> holder.getView<LinearLayout>(R.id.ll_mould_bg).setBackgroundColor(context.resources.getColor(R.color.mom_color, null))
        //"哥哥" -> holder.getView<LinearLayout>(R.id.ll_mould_bg).setBackgroundColor(context.resources.getColor(R.color.bro_color, null))
        //"姐姐" -> holder.getView<LinearLayout>(R.id.ll_mould_bg).setBackgroundColor(context.resources.getColor(R.color.sis_color, null))
        }
        //根据保存的发音人名称设置声音使用状态
        holder.getView<TextView>(R.id.tv_mould_using).visibleOrGone(AppConfig.PlayMusicPerson == item.type)
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun initPopWindow(v: View) {
        val view: View = LayoutInflater.from(context).inflate(R.layout.layout_popup, null, false)
        val btnXixi: Button = view.findViewById(R.id.btn_xixi) as Button
        val btnHehe: Button = view.findViewById(R.id.btn_hehe) as Button
        //1.构造一个PopupWindow，参数依次是加载的View，宽高
        val popWindow = PopupWindow(
            view,
            ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT, true
        )
        popWindow.animationStyle = R.anim.anim_pop //设置加载动画

        //这些为了点击非PopupWindow区域，PopupWindow会消失的，如果没有下面的
        //代码的话，你会发现，当你把PopupWindow显示出来了，无论你按多少次后退键
        //PopupWindow并不会关闭，而且退不出程序，加上下述代码可以解决这个问题
        popWindow.isTouchable = true
        popWindow.setTouchInterceptor { _, _ ->
            false
            // 这里如果返回true的话，touch事件将被拦截
            // 拦截后 PopupWindow的onTouchEvent不被调用，这样点击外部区域无法dismiss
        }
        popWindow.setBackgroundDrawable(ColorDrawable(0x00000000)) //要为popWindow设置一个背景才有效

        //设置popupWindow显示的位置，参数依次是参照View，x轴的偏移量，y轴的偏移量
        popWindow.showAsDropDown(v, 50, 0)

        //设置popupWindow里的按 钮的事件
        btnXixi.setOnClickListener { ToastUtils.debugShow("你点击了嘻嘻~") }
        btnHehe.setOnClickListener {
            ToastUtils.debugShow("你点击了呵呵~")
            popWindow.dismiss()
        }
    }
}