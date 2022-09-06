package com.hisense.sound.logic.dao

import com.hisense.sound.BuildConfig

/**
 * Created by 咸鱼至尊 on 2022/1/8
 *
 * desc: 网络请求API类
 */
object NetApi {

    /** 服务器IP地址 */
    private val NetIP = if (BuildConfig.DEBUG) {
        "http://221.0.232.158:40001"
    } else {
        "http://221.0.232.158:40002"
    }

    /** 查询用户模型信息API */
    val GetUserModelInfoAPI = "$NetIP/userModelInfo/getUserModelInfos"

    /** 新增用户模型信息API */
    val AddUserModelInfoAPI = "$NetIP/userModelInfo/saveUserModelInfo"

    /** 删除用户模型信息API */
    val RemoveUserModelInfoAPI = "$NetIP/userModelInfo/removeUserModelInfo"

    /** 更新用户模型信息API */
    val UpdateUserModelInfoAPI = "$NetIP/userModelInfo/updateUserModelInfo"
}