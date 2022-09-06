package com.hisense.sound.logic.dao

import android.content.Context
import android.text.TextUtils
import com.hisense.sound.BuildConfig
import com.hmct.cloud.sdk.bean.UniversalBean
import com.hmct.cloud.sdk.factory.HiCloudServiceFactory
import com.hmct.cloud.sdk.service.UniversalService
import com.hmct.cloud.sdk.utils.AESUtil
import com.hmct.cloud.sdk.utils.DeviceConfig
import com.hmct.cloud.sdk.utils.Params
import com.hmct.cloud.sdk.utils.SDKUtil
import java.util.*

//import com.hisense.hitv.hicloud.util.DeviceConfig;
/**
 * Created by Victor on 2018/5/29. (ง •̀_•́)ง
 * 所有方法不能在主线程调用，否则无效 聚好看 api网址 qinwendong@hisense.com 密码：
 * http://10.18.207.41:8080/sosoapi-web/auth/proj/list.htm
 *
 * http://10.18.207.41:8080/sosoapi-web/auth/doc/inter/tree/list.htm?projId=110&docId=110
 *
 * desc：海信APP通用服务实现类
 */
object HiUniversalServiceImpl : HiUniversalService {

    /**
     * 张浩-测试内网 Dns 10.18.222.28 张浩-外网 Dns 47.104.211.24 有效
     *
     * 聚好看 外网测试dns 139.217.91.3 -》"bas.wg.hismarttv.com"
     *
     * 只要你再研发中心就可以用10.18.207.100 --》10.18.222.36 443 完全可以能连通啊
     */
    private val HITV_IP = if (BuildConfig.DEBUG) {
        "http://" + "27.223.99.157"
    } else {
        "https://" + "bas-wg.hismarttv.com"
    }

    private val service: UniversalService
        get() = HiCloudServiceFactory.getUniversalService()


    /** GET cam/share/get_third_bind_info 17 根据海信账号查询第三方账号的绑定关系 */
    override fun get_third_bind_info(
        thirdPlatformId: String,
        accessToken: String,
        deviceId: String
    ): UniversalBean {
        val map = HashMap<String, String>()
        map["thirdPlatformId"] = thirdPlatformId
        map["accessToken"] = accessToken
        map[Params.APIVERSION] = "7.8"
        map[Params.TIMESTAMP] = System.currentTimeMillis().toString()
        map[Params.FORMAT] = "1"
        map[Params.INVOKESOURCE] = "1"
        map[Params.RANDSTR] = System.currentTimeMillis().toString()
        map["devSerial"] = deviceId
        //sign签名认证，由本次请求的所有参数计算的值。有值的参数按照顺序，RSA公钥加密。只有终端请求需要此参数，网站请求不需要。
        return service["$HITV_IP/cam/share/get_third_bind_info", map, false]
    }

    /**
     * GET cam/user/validate_mobile 5 手机号唯一性校验
     *
     * @param accessToken 需要授权token，用以身份验证
     */
    override fun validate_mobile(
        mobilephone: String,
        accessToken: String,
        deviceId: String
    ): UniversalBean {
        val map = HashMap<String, String>()
        map["mobilephone"] = mobilephone
        map["accessToken"] = accessToken
        map[Params.APIVERSION] = "7.8"
        map[Params.TIMESTAMP] = System.currentTimeMillis().toString()
        map[Params.FORMAT] = "1"
        map[Params.INVOKESOURCE] = "1"
        map[Params.RANDSTR] = System.currentTimeMillis().toString()
        map["devSerial"] = deviceId
        //sign签名认证，由本次请求的所有参数计算的值。有值的参数按照顺序，RSA公钥加密。只有终端请求需要此参数，网站请求不需要。
        return service["$HITV_IP/cam/user/mobile_update", map, false]
    }

    /** POST cam/user/mobile_update 修改手机号 password AES加密。如果传值，表示同时修改密码为该密码。 */
    override fun mobile_update(
        mobilephone: String,
        password: String?,
        authCode: String,
        accessToken: String,
        deviceId: String
    ): UniversalBean {
        val map = HashMap<String, String>()
        map["mobilephone"] = mobilephone
        map["password"] = AESUtil.encrypt(password)
        map["authCode"] = authCode
        map["accessToken"] = accessToken
        map[Params.APIVERSION] = "7.8"
        map[Params.TIMESTAMP] = System.currentTimeMillis().toString()
        map[Params.FORMAT] = "1"
        map[Params.INVOKESOURCE] = "1"
        map[Params.RANDSTR] = System.currentTimeMillis().toString()
        map["devSerial"] = deviceId
        //sign签名认证，由本次请求的所有参数计算的值。有值的参数按照顺序，RSA公钥加密。只有终端请求需要此参数，网站请求不需要。
        return service.post("$HITV_IP/cam/user/mobile_update", map, false)
    }

    /**
     * POST cam/user/mobile_auth_code_update 修改当前登录账号的手机号 password
     * AES加密。如果传值，表示同时修改密码为该密码。
     */
    override fun mobile_auth_code_update(
        mobilephone: String,
        authCode: String,
        accessToken: String,
        deviceId: String
    ): UniversalBean {
        val map = HashMap<String, String>()
        map["mobilephone"] = mobilephone
        map["authCode"] = authCode
        map["accessToken"] = accessToken
        map[Params.APIVERSION] = "7.8"
        map[Params.TIMESTAMP] = System.currentTimeMillis().toString()
        map[Params.FORMAT] = "1"
        map[Params.INVOKESOURCE] = "1"
        map[Params.RANDSTR] = System.currentTimeMillis().toString()
        map["devSerial"] = deviceId
        //sign签名认证，由本次请求的所有参数计算的值。有值的参数按照顺序，RSA公钥加密。只有终端请求需要此参数，网站请求不需要。
        return service.post("$HITV_IP/cam/user/mobile_auth_code_update", map, false)
    }

    /** GET cam/mobile/send_mobile_tm_auth_code2 发送手机短信验证码（电视终端使用） */
    override fun send_mobile_tm_auth_code(
        mobilephone: String,
        accessToken: String,
        deviceId: String
    ): UniversalBean {
        val map = HashMap<String, String>()
        map["mobilephone"] = mobilephone
        map["accessToken"] = accessToken
        map[Params.APIVERSION] = "7.8"
        map[Params.TIMESTAMP] = System.currentTimeMillis().toString()
        map[Params.FORMAT] = "1"
        map[Params.INVOKESOURCE] = "1"
        map[Params.RANDSTR] = System.currentTimeMillis().toString()
        map["devSerial"] = deviceId
        //sign签名认证，由本次请求的所有参数计算的值。有值的参数按照顺序，RSA公钥加密。只有终端请求需要此参数，网站请求不需要。
        return service["$HITV_IP/cam/mobile/send_mobile_tm_auth_code", map, false]
    }


    /**
     * 20 自动注册和登录 POST cam/user/mobile_auth_code_regist_v2
     *
     * 此接口也支持已存在账号自动登录
     */
    override fun mobile_auth_code_regist_v2(
        context: Context,
        mobilephone: String?,
        password: String?,
        authCode: String?,
    ): UniversalBean {
        val map = HashMap<String, String?>()
        map["mobilephone"] = mobilephone
        map[Params.APPKEY] = AppConfig.AppLoginKey
        map[Params.APPSECRET] = AppConfig.AppLoginSecret
        if (password.isNullOrEmpty()) {
            //map["password"] = null ,非必选参数，没有的话不穿
        } else {
            map["password"] = AESUtil.encrypt(password) //设置新密码
        }
        map["authCode"] = authCode
        map["subscriberType"] = "7" //7-智能手机
        map["extDevId"] = DeviceConfig.getDeviceId(context) //extDevId 设备ID外部标识，长度32字节。值可以是空。用户登录历史记录和展示。
        setSystemParams(map, context)
        return service.post("$HITV_IP/cam/user/mobile_auth_code_regist_v2", map, false)
    }

    /**
     * 22 手机号验证码注册并绑定第三方账号 - 20200313 POST
     * cam/user/mobile_auth_code_regist_and_bind
     *
     * 此接口也支持已存在账号的绑定
     */
    override fun mobile_auth_code_regist_and_bind(
        context: Context,
        mobilephone: String?,
        password: String?,
        authCode: String?,
        appKey: String?,
        appSecret: String?,
        thirdPlatformId: String?,
        userId: String?,
        thirdToken: String?
    ): UniversalBean {
        val map = HashMap<String, String?>()
        map["mobilephone"] = mobilephone
        map[Params.APPKEY] = appKey
        map[Params.APPSECRET] = appSecret
        if (password.isNullOrEmpty()) {
            //map["password"] = null ,非必选参数，没有的话不穿
        } else {
            map["password"] = AESUtil.encrypt(password) //设置新密码
        }
        map["authCode"] = authCode
        map["subscriberType"] = "7" //7-智能手机
        map["thirdPlatformId"] = thirdPlatformId
        map["userId"] = userId
        map["thirdToken"] = thirdToken
        setSystemParams(map, context)
        return service.post("$HITV_IP/cam/user/mobile_auth_code_regist_and_bind", map, false)
    }

    /** 25 第三方code登录(微信二维码、微信小程序、华为) - 20200313 */
    override fun third_code_login_v2(
        context: Context,
        thirdPlatformId: String,
        callbackParams: String,
        appKey: String,
        appSecret: String,
        deviceId: String
    ): UniversalBean {
        val map = HashMap<String, String?>()
        map["thirdPlatformId"] = thirdPlatformId
        map["callbackParams"] = callbackParams
        map[Params.APPKEY] = appKey
        map[Params.APPSECRET] = appSecret
        setSystemParams(map, context)
        map["alwaysRetThirdInfo"] = "1" //是否一定要返回第三方信息： 1 一定返回第三方信息,为了下一步绑定使用
        map["subscriberType"] = "7" //7-智能手机
        return service["$HITV_IP/cam/share/third_code_login_v2", map, false]
    }

    /** GET cam/user/get_customer_info */
    override fun get_customer_info(
        context: Context,
        accessToken: String?
    ): UniversalBean {
        val map = HashMap<String, String?>()
        map["queryType"] = "1" //查询类型。固定：1-查询本用户信息
        map["accessToken"] = accessToken
        setSystemParams(map, context)
        return service["$HITV_IP/cam/user/get_customer_info", map, false]
    }

    /**
     * 23 第三方账号登陆绑定 - 20200313 最后更新时间 2020-03-13 07:35:37
     *
     * 接口说明 第三方账号登录场景下的用户登陆并绑定
     *
     * 20200313: 增加字段agreePolicy
     *
     * @param thirdPlatformId
     * @param deviceId 签名，32字节 Signature的获取算法：
     *     RC4(md5(loginName,timeStamp),md5(password))，再转成大写16进制。
     *     其中：loginName和timeStamp都作为字符串拼在一起作为一个整串进行MD5md5(password)是作为RC4的密钥，文档以下部分所有的RC4算法都采用最后一个参数作为密钥
     * @return
     */
    override fun third_login_bind_v2(
        context: Context,
        loginName: String,
        password: String?,
        thirdPlatformId: String,
        thirdToken: String,
        userId: String,
    ): UniversalBean {

        //做为加密参数所以不能变
        val timeStamp = System.currentTimeMillis() / 1000 //时间戳 单位：秒数
        //前面的参数是密钥
        val signature = SDKUtil.byte2string(
            SDKUtil.rc4_crypt(
                SDKUtil.md5Encode(password), SDKUtil.md5Encode(loginName + timeStamp)
            )
        )
        val map = HashMap<String, String?>()
        map[Params.LOGINNAME] = loginName
        map["signature"] = signature
        map["thirdPlatformId"] = thirdPlatformId
        map["thirdToken"] = thirdToken
        map["userId"] = userId
        map[Params.APPKEY] = AppConfig.AppLoginKey
        map[Params.APPSECRET] = AppConfig.AppLoginSecret
        map["extDevId"] = DeviceConfig.getDeviceId(context) //extDevId 设备ID外部标识，长度32字节。值可以是空。用户登录历史记录和展示。
        setSystemParams(map, context)
        map["subscriberType"] = "7" //7-智能手机
        return service.post("$HITV_IP/cam/share/third_login_bind_v2", map, false)
    }

    /**
     * 登录名+密码/匿名登录
     *
     * 用户登录，返回token。登录名为空则是匿名。
     *
     * 20200313：controlFlag，有手机号的账号登录，密码连续错误达到次数后，在指定时间内，返回202080-需要手机验证码登录。 王震
     * 业务中台开发部 -匿名的时候signature=&loginName=就可以了
     */
    override fun sign_in(
        context: Context,
        loginName: String?,
        pwd: String?
    ): UniversalBean {

        //做为加密参数所以不能变
        val timeStamp = System.currentTimeMillis() / 1000 //时间戳 单位：秒数
        //前面的参数是密钥
        val map = HashMap<String, String?>()
        if (!TextUtils.isEmpty(loginName)) {
            val signature = SDKUtil.byte2string(
                SDKUtil.rc4_crypt(
                    SDKUtil.md5Encode(pwd), SDKUtil.md5Encode(loginName + timeStamp)
                )
            )
            map["signature"] = signature
        } else {
            map["signature"] = null
        }
        setSystemParams(map, context) //位置不能动
        map[Params.LOGINNAME] = loginName
        map[Params.TIMESTAMP] = timeStamp.toString()
        map["serviceType"] =
            "7" //用户类型:0-公众用户 （Default）1-集团/单位客户2-酒店用户3-广告机4-智能电视1.X用户5-智能电视2.0用户6-平板电脑PAD用户7-智能手机8-智能冰箱9-智能电视（Android)10-互联网机顶盒11-运营商用户99-第三方终端确定，并会增加
        map[Params.APPKEY] = AppConfig.AppLoginKey
        map[Params.APPSECRET] = AppConfig.AppLoginSecret
        map["extDevId"] = DeviceConfig.getDeviceId(context) //extDevId 设备ID外部标识，长度32字节。值可以是空。用户登录历史记录和展示。
        map["controlFlag"] = "1" //控制标志。0：无；1：登录错误次数达到上限后，必须用验证码登录。2020.3.12 add
        map["agreePolicy"] = "1" //登录即同意隐私协议
        //此接口不能穿多于参数，必须按照接口来
        return service.post("$HITV_IP/aaa/sign_in", map, false)
    }

    override fun sign_out(context: Context, accessToken: String?): UniversalBean {
        val map = HashMap<String, String?>()
        setSystemParams(map, context) //位置不能动
        map["accessToken"] = accessToken
        //此接口不能穿多于参数，必须按照接口来
        return service.post("$HITV_IP/aaa/sign_out", map, false)
    }

    fun sign_in_refreshtoken(
        context: Context,
        refreshToken: String?
    ): UniversalBean {
        val map = HashMap<String, String?>()
        setSystemParams(map, context)
        map["refreshToken"] = refreshToken
        map[Params.APPKEY] = AppConfig.AppLoginKey
        map[Params.APPSECRET] = AppConfig.AppLoginSecret
        map["extDevId"] = DeviceConfig.getDeviceId(context)
        //此接口不能穿多于参数，必须按照接口来
        return service.post(HITV_IP + "/aaa/sign_in_refreshtoken", map, false)
    }

    fun sign_in_token(
        context: Context,
        accessToken: String?
    ): UniversalBean {
        val map = HashMap<String, String?>()
        setSystemParams(map, context)
        map["accessToken"] = accessToken
        map["extDevId"] = DeviceConfig.getDeviceId(context)
        //此接口不能穿多于参数，必须按照接口来
        return service.post("$HITV_IP/aaa/sign_in_token", map, false)
    }

    fun upload_profile(
        context: Context,
        accessToken: String?
    ): UniversalBean {
        val map = HashMap<String, String?>()
        setSystemParams(map, context)
        map["accessToken"] = accessToken
        map["extDevId"] = DeviceConfig.getDeviceId(context)
        //此接口不能穿多于参数，必须按照接口来
        return service.post("$HITV_IP/custserv/social/upload_profile", map, false)
    }

    private fun setSystemParams(map: HashMap<String, String?>, context: Context) {
        map["format"] = "1"
        map["sourceType"] = "1"
        map["version"] = "7.8"
        map["timeStamp"] = System.currentTimeMillis().toString()
        map["randStr"] = System.currentTimeMillis().toString()
        map["deviceId"] = DeviceConfig.getDeviceId(context)
        map["devSerial"] = DeviceConfig.getDeviceId(context)
        map["languageId"] = "0" //
    }

}