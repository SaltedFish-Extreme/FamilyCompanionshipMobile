package com.hisense.sound.logic.model

/**
 * 用户模型信息数据类
 *
 * @property createTime 创建模型时间
 * @property deleteStatus 删除状态
 * @property trainStatus 训练状态
 * @property userAccount 用户账号
 * @property userModelId 模型ID
 * @property userModelType 模型类型
 */
data class UserModelInfoResponse(
    val createTime: String,
    val deleteStatus: Int,
    val trainStatus: Int,
    val userAccount: String,
    val userModelId: String,
    val userModelType: String
)