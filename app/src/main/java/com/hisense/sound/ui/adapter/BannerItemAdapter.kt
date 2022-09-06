package com.hisense.sound.ui.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.hisense.sound.R
import com.hisense.sound.logic.model.BannerModel
import com.hjq.toast.ToastUtils
import com.youth.banner.adapter.BannerAdapter

/**
 * Created by 咸鱼至尊 on 2021/12/20
 *
 * desc: banner布局适配器，图片+标题+描述
 */
class BannerItemAdapter(dataList: List<BannerModel>) : BannerAdapter<BannerModel, BannerItemAdapter.BannerHolder>(dataList) {

    inner class BannerHolder(view: View) : RecyclerView.ViewHolder(view) {
        val imageView: ImageView = view.findViewById(R.id.banner_picture)
        val title: TextView = view.findViewById(R.id.banner_title)
        val desc: TextView = view.findViewById(R.id.banner_desc)
    }

    override fun onCreateHolder(parent: ViewGroup, viewType: Int): BannerHolder {
        //填充视图
        return BannerHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_banner, parent, false))
    }

    override fun onBindView(holder: BannerHolder, data: BannerModel, position: Int, size: Int) {
        holder.imageView.run {
            //加载本地drawable图片
            Glide.with(context).load(data.imageDrawable).into(this)
            //设置标题
            holder.title.text = data.title
            //设置描述
            holder.desc.text = data.desc
            //item点击事件
            setOnBannerListener { mData, _ ->
                ToastUtils.debugShow(mData.title)
            }
        }
    }
}