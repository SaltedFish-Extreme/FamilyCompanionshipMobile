package com.hisense.sound.logic.model

/**
 * 故事数据类
 *
 * @property title 标题
 * @property text 内容
 * @property state 播放状态
 */
data class StoryModel(
    val title: String,
    val text: String,
    var state: Boolean
)