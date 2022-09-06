package com.hisense.sound.logic.dao

import android.media.MediaPlayer
import com.drake.serialize.serialize.serialLazy

/**
 * Created by zhongyi.ex on 2022/7/22
 *
 * APP配置类：全局常量与变量（单例|延迟初始化|序列化存储磁盘）
 */
object AppConfig {
    /** 声音复刻ID */
    val APIKey by serialLazy("8f05d7ac1d604966a824f43d9f3ef99c")

    /** 声音复刻密钥 */
    val APISecret by serialLazy("5b2c1e92930346dbbf5de4519acfc551")

    /** 与声音训练模型关联的查询ID */
    var QueryId by serialLazy("")

    /** 保存的声音训练模型集合 */
    var ModelMap by serialLazy(mutableMapOf<String, String>())

    /** 生成访问令牌API路径 */
    val GenerateTokenAPI by serialLazy("https://openapi.data-baker.com/oauth/2.0/token")

    /** 访问令牌类型 */
    val GrantType by serialLazy("client_credentials")

    /** 访问令牌 */
    var AccessToken by serialLazy("")

    /** 上次请求访问令牌的时间 */
    var requestTokenTime by serialLazy(0L)

    /** 请求令牌间隔时间（18小时） */
    val requestIntervals by serialLazy(1000 * 60 * 60 * 18)

    /** 声音合成API路径 */
    val SoundSynthesisAPI by serialLazy("https://openapi.data-baker.com/tts_personal")

    /** 正在播放的音频名称（标题_模型ID.mp3） */
    var PlayMusicName by serialLazy("")

    /** 正在播放的音频item位置 */
    var PlayMusicPosition by serialLazy(0)

    /** 正在播放的音频发音人 */
    var PlayMusicPerson by serialLazy("")

    /** 全局媒体播放器对象实例（单例） */
    lateinit var mediaPlayer: MediaPlayer

    /** 会员中心账号登录ID */
    val AppLoginKey by serialLazy("1184951009")

    /** 会员中心账号登录密钥 */
    val AppLoginSecret by serialLazy("ed6f7q4xpkbfwpert8eqv6evfh8ud30f")

    /** 用户账号 todo 后续不会存储在app内，通过登录过后得到的refreshToken再次刷新登录状态 */
    var AccountName by serialLazy("")

    /** 用户密码 todo 后续不会存储在app内，通过登录过后得到的refreshToken再次刷新登录状态 */
    var AccountPassword by serialLazy("")

    /** 用户登录后的token */
    var AccountToken by serialLazy("")

    /** 用户登录后的customID（唯一标识） */
    var AccountCustomID by serialLazy("")

    /** 上次登录请求的时间 */
    var requestLoginTime by serialLazy(0L)

    /** 登录操作间隔时间（2天半） */
    val LoginIntervals by serialLazy(1000 * 60 * 60 * 24 * 2.5)
}