package com.hisense.sound.logic.dao

import android.content.Context
import com.hmct.cloud.sdk.bean.UniversalBean

/**
 * @author qinwendong
 * @date 2020/10/23 descrption:
 * @desc 海信APP通用服务接口
 */
interface HiUniversalService {

    /** 登录包含匿名登陆 */
    fun sign_in(context: Context, loginName: String?, pwd: String?): UniversalBean

    /** POST aaa/sign_out退出登录 */
    fun sign_out(context: Context, accessToken: String?): UniversalBean

    fun get_third_bind_info(
        thirdPlatformId: String,
        accessToken: String,
        deviceId: String
    ): UniversalBean

    /**
     * GET cam/user/validate_mobile 5 手机号唯一性校验
     *
     * @param accessToken 需要授权token，用以身份验证
     */
    fun validate_mobile(
        mobilephone: String,
        accessToken: String,
        deviceId: String
    ): UniversalBean

    /** POST cam/user/mobile_update 修改手机号 password AES加密。如果传值，表示同时修改密码为该密码。 */
    fun mobile_update(
        mobilephone: String,
        password: String?,
        authCode: String,
        accessToken: String,
        deviceId: String
    ): UniversalBean

    /**
     * POST cam/user/mobile_auth_code_update 修改当前登录账号的手机号 password
     * AES加密。如果传值，表示同时修改密码为该密码。
     */
    fun mobile_auth_code_update(
        mobilephone: String,
        authCode: String,
        accessToken: String,
        deviceId: String
    ): UniversalBean

    /** GET cam/mobile/send_mobile_tm_auth_code2 发送手机短信验证码（电视终端使用） */
    fun send_mobile_tm_auth_code(
        mobilephone: String,
        accessToken: String,
        deviceId: String
    ): UniversalBean

    /** 20登录和注册 */
    fun mobile_auth_code_regist_v2(
        context: Context,
        mobilephone: String?,
        password: String?,
        authCode: String?

    ): UniversalBean

    /**
     * 22 手机号验证码注册并绑定第三方账号 - 20200313 POST
     * cam/user/mobile_auth_code_regist_and_bind
     *
     * 此接口也支持已存在账号的绑定
     */
    fun mobile_auth_code_regist_and_bind(
        context: Context,
        mobilephone: String?,
        password: String?,
        authCode: String?,
        appKey: String?,
        appSecret: String?,
        thirdPlatformId: String?,
        userId: String?,
        thirdToken: String?
    ): UniversalBean

    /** 25 第三方code登录(微信二维码、微信小程序、华为) - 20200313 */
    fun third_code_login_v2(
        context: Context,
        thirdPlatformId: String,
        callbackParams: String,
        appKey: String,
        appSecret: String,
        deviceId: String
    ): UniversalBean

    /** GET cam/user/get_customer_info */
    fun get_customer_info(
        context: Context,
        accessToken: String?
    ): UniversalBean

    /**
     * 23 第三方账号登陆绑定 - 20200313 最后更新时间 2020-03-13 07:35:37
     *
     * 20200313: 增加字段agreePolicy
     *
     * @param thirdPlatformId
     * @param deviceId 签名，32字节 Signature的获取算法：
     *     RC4(md5(loginName,timeStamp),md5(password))，再转成大写16进制。
     *     其中：loginName和timeStamp都作为字符串拼在一起作为一个整串进行MD5md5(password)是作为RC4的密钥，文档以下部分所有的RC4算法都采用最后一个参数作为密钥
     * @return
     */
    fun third_login_bind_v2(
        context: Context,
        loginName: String,
        password: String?,
        thirdPlatformId: String,
        thirdToken: String,
        userId: String,
    ): UniversalBean

}