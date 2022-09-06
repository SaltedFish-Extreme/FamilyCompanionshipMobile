package com.hisense.sound.logic.model

/** 服务器返回数据的基类 */
data class ApiResponse<T>(val code: Int, val msg: String, val data: T)