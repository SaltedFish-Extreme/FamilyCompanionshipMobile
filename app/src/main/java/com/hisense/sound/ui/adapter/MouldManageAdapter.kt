package com.hisense.sound.ui.adapter

import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.hisense.sound.R
import com.hisense.sound.ui.dialog.Dialog
import com.hjq.toast.ToastUtils

/**
 * Created by 咸鱼至尊 on 2021/12/20
 *
 * desc: 模型管理列表适配器
 */
class MouldManageAdapter : BaseAdapter<String>(R.layout.item_sound) {

    init {
        this.setOnItemClickListener { _, _, position ->
            Dialog.getConfirmDialog(context, "确认删除该模型吗？") { _, _ ->
                ToastUtils.show(R.string.delete_success)
                this.removeAt(position)
            }.show()
        }
    }

    override fun convert(holder: BaseViewHolder, item: String) {
        //设置声音所属类型
        holder.setText(R.id.tv_delete_model, item)
    }
}