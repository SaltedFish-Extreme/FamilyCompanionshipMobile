package com.hisense.sound.logic.model

import android.graphics.drawable.Drawable

/**
 * 首页banner数据类
 *
 * @property imageDrawable drawable图片资源
 * @property title 标题
 * @property desc 描述
 * @property modelId 对应的模型ID
 */
data class BannerModel(
    val imageDrawable: Drawable,
    val title: String,
    val desc: String,
    val modelId: String
)