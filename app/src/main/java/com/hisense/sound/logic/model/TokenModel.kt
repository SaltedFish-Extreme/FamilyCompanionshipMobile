package com.hisense.sound.logic.model

/**
 * 声音复刻后合成音频所需的token数据类
 *
 * @property access_token token信息
 */
data class TokenModel(
    val access_token: String,
    val expires_in: Int,
    val scope: String,
    val token_type: String
)