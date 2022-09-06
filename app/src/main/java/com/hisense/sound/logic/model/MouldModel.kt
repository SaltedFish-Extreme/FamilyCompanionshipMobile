package com.hisense.sound.logic.model

/**
 * 训练模型数据类
 *
 * @property mouldId 模型ID
 * @property type 类型（e.g. 爸爸、妈妈）
 * @property training 训练状态（true：正在训练；false：训练完成）
 */
data class MouldModel(
    val mouldId: String,
    val type: String,
    var training: Int
)