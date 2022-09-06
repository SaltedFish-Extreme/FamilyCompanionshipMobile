package com.hisense.sound.logic.model

/**
 * 用户登录数据类
 *
 * @property customerId 用户唯一ID
 * @property loginName 登录名
 * @property refreshToken 刷新token
 * @property resultCode 返回码
 * @property token 用户token信息
 */
data class LoginModel(
    val customerId: String,
    val loginName: String,
    val refreshToken: String,
    val refreshTokenExpiredTime: String,
    val resultCode: Int,
    val subscriberId: String,
    val token: String,
    val tokenCreateTime: String,
    val tokenExpiredTime: String
)