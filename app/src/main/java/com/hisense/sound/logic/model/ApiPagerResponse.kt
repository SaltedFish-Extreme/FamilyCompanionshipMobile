package com.hisense.sound.logic.model

/** 模型数据列表的基类 */
data class ApiPagerResponse<T>(
    val userModelInfoList: T
)