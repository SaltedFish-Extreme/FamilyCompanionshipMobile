package com.hisense.sound.logic.model

/** 返回数据为空的数据类 */
data class NoDataResponse(val code: Int, val msg: String, val data: Nothing? = null)