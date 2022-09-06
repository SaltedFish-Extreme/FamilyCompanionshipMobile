package com.hisense.sound.logic.dao;

import android.content.Context;
import android.util.Log;

import com.hisense.hitv.hicloud.bean.account.AppCodeReply;
import com.hisense.hitv.hicloud.bean.account.AppCodeSSO;
import com.hisense.hitv.hicloud.bean.account.CustomerInfo;
import com.hisense.hitv.hicloud.bean.account.GetUriReply;
import com.hisense.hitv.hicloud.bean.account.ReplyInfo;
import com.hisense.hitv.hicloud.bean.account.SignonReplyInfo;
import com.hisense.hitv.hicloud.bean.account.ThirdAccountOauthLoginReplay;
import com.hisense.hitv.hicloud.bean.global.HiSDKInfo;
import com.hisense.hitv.hicloud.factory.HiCloudServiceFactory;
import com.hisense.hitv.hicloud.service.HiCloudAccountService;
import com.hisense.hitv.hicloud.util.DeviceConfig;
import com.hisense.hitv.hicloud.util.Params;
import com.hisense.sound.BuildConfig;
import com.hisense.sound.util.KLog;

import java.util.HashMap;

/**
 * Created by Victor on 2018/5/29. (ง •̀_•́)ง
 * 所有方法不能在主线程调用，否则无效
 * <p>
 * desc：海信APP服务实现类
 */
public class HiServiceImpl implements HiService {
    private static final String TAG = "HiServiceImpl";

    private static class SingleTon {
        private static HiServiceImpl INSTANCE = new HiServiceImpl();
    }

    public static HiServiceImpl obtain() {
        return SingleTon.INSTANCE;
    }

    //hitv 登录相关地址

    //public static final String DEFAULT_WG_HITV_IP = "27.223.99.157";
    /**
     * bas-wg.hismarttv.com
     */
    public static final String DEFAULT_WG_HITV_IP = BuildConfig.DEBUG ? "27.223.99.157" : "bas-wg.hismarttv.com";

    private String mIp = DEFAULT_WG_HITV_IP;

    public static String getIp() {
        return DEFAULT_WG_HITV_IP;

    }

    public void setIp(String ip) {
        mIp = ip;
    }

    private HiCloudAccountService getService() {
        return getService(null);
    }

    private HiCloudAccountService getService(String token) {

        HiSDKInfo info = new HiSDKInfo();
        info.setDomainName(mIp);
        info.setToken(token);
        info.setHttps(true);
        return HiCloudServiceFactory.getHiCloudAccountService(info);
    }

    @Override
    public AppCodeReply appAuth(final String appKey, final String appSecret) {
        HashMap<String, String> map = new HashMap<>();
        map.put(Params.APPKEY, appKey);
        map.put(Params.APPSECRET, appSecret);
        return getService().appAuth(map);
    }

    @Override
    public AppCodeSSO appSSOAuth(final String appKey, final String appSecret, final String deviceId) {
        HashMap<String, String> map = new HashMap<>();
        map.put(Params.APPKEY, appKey);
        map.put(Params.APPSECRET, appSecret);
        map.put(Params.DEVICEID, deviceId);
        AppCodeSSO reply = getService().appAuthSSO(map);
        if (reply.getReply() != 2) {
            String appCodeSSO = reply.getCode();
            map = new HashMap<>(3);
            map.put(Params.APPCODE, appCodeSSO);
            map.put(Params.LOGINNAME, "");
            map.put(Params.DEVICEID, deviceId);
            SignonReplyInfo signonReply = getService().signon(map);
            if (signonReply != null) {
                reply.setToken(signonReply.getToken());
            }
        }
        return reply;
    }

    @Override
    public GetUriReply getUri(HashMap<String, String> map) {
        /*        HashMap<String, String> map = new HashMap<>(3);
        map.put(Params.ACCESSTOKEN, tokenSSO); //TODO
        map.put(Params.BLOGID, blogId);
        map.put(Params.CALLBACKPATH, callBackPath);*/
        return getService().getUri(map);
    }

    @Override
    public GetUriReply getUri(String tokenSSO, int blogId, String callBackPath) {

        return getService().getUri(tokenSSO, blogId, callBackPath);
    }


    @Override
    public SignonReplyInfo login(String loginName, String password, String deviceId, String appCode) {
        HashMap<String, String> map = new HashMap<>();
        map.put(Params.DEVICEID, deviceId);
        map.put(Params.LOGINNAME, loginName);
        map.put(Params.PASSWORD, password);
        map.put(Params.APPCODE, appCode);
        SignonReplyInfo reply = getService().signon(map);
        return reply;
    }

    @Override
    public ReplyInfo logout(String token, String deviceId) {
        HashMap<String, String> map = new HashMap<>();
        map.put(Params.DEVICEID, deviceId);
        ReplyInfo reply = getService(token).logout(map);
        if (reply != null && reply.getReply() == 0) {
            KLog.INSTANCE.e(TAG, "logout success");
        } else {
            KLog.INSTANCE.e(TAG, "logout error, but do clearToken");
        }
        //todo TokenManager.INSTANCE.clearToken();
        return reply;
    }

    @Override
    public CustomerInfo getCustomerInfo(String token) {
        return getService(token).getCustomerInfo();
    }

    @Override
    public String getDeviceId(Context context) {
        Log.d("唯一设备id:", DeviceConfig.getDeviceId(context));
        return DeviceConfig.getDeviceId(context);
    }

    @Override
    public ReplyInfo updateCustomerInfo(String token, HashMap<String, String> map) {
        return getService(token).updateCustomerInfo(map);
    }

    @Override
    public ReplyInfo modifyPassword(String token, String oldPwd, String newPwd) {
        HashMap<String, String> map = new HashMap<>();
        map.put("oldPwd", oldPwd);
        map.put("newPwd", newPwd);
        return getService(token).modifyPassword(map);
    }

    @Override
    public SignonReplyInfo refreshToken(String appKey, String refreshToken) {
        HashMap<String, String> map = new HashMap<>();
        map.put("appKey", appKey);
        map.put("refreshToken", refreshToken);
        SignonReplyInfo reply = getService().refreshToken2(map);
        return reply;
    }

    @Override
    public SignonReplyInfo register(HashMap<String, String> map) {
        return getService().register(map);
    }

    @Override
    public SignonReplyInfo register(String loginName, String password, String deviceId, String email, String registerType, String mobilePhone, String appCode) {
        HashMap<String, String> map = new HashMap<>(7);
        map.put(Params.LOGINNAME, loginName);
        map.put(Params.PASSWORD, password);
        map.put(Params.EMAIL, email);
        map.put(Params.DEVICEID, deviceId);
        map.put(Params.REGISTER_TYPE, registerType);
        map.put(Params.MOBILEPHONE, mobilePhone);
        map.put(Params.APPCODE, appCode);
        return getService().register(map);
    }


    @Override
    public ReplyInfo sendCaptcha(String tokenSSO, String phone) {
        return getService().sendMobileAuthCode(tokenSSO, phone);
    }

    @Override
    public ReplyInfo validateCaptcha(String tokenSSO, String phone, String captcha) {
        return getService().checkMobileAuthCode(tokenSSO, phone, captcha);
    }

    @Override
    public ReplyInfo findPasswordSendCaptcha(String loginName, String appCode, String tokenSSO) {
        HashMap<String, String> map = new HashMap<>(2);
        map.put("loginName", loginName);
        map.put("appCode", appCode);
        return getService(tokenSSO).findPassswordCode(map);
    }

    @Override
    public ReplyInfo findPasswordByCaptcha(String loginName, String captcha, String newPassword, String tokenSSO) {
        HashMap<String, String> map = new HashMap<>();
        map.put("loginName", loginName);
        map.put("checkCode", captcha);
        map.put("newPwd", newPassword);
        return getService(tokenSSO).findPassswordByCode(map);
    }

/*    @Override
    public boolean isTokenWillExpire() {
        long tokenTimeCreate = AccountSpUtil.getLong(TOKEN_CREATE);
        long tokenTimeExpire = AccountSpUtil.getLong(TOKEN_EXPIRE);
        long diff = System.currentTimeMillis() / 1000 - tokenTimeCreate;
        return TextUtils.isEmpty(getToken()) || (tokenTimeCreate > 0 && tokenTimeExpire > 0 && diff > 0 && diff > (tokenTimeExpire * 0.9f));
    }*/

/*    @Override
    public String getToken() {
        return AccountSpUtil.getString(TOKEN);
    }*/

    @Override
    public ThirdAccountOauthLoginReplay thirdAccountOauthLogin(HashMap<String, String> map) {
        ThirdAccountOauthLoginReplay oauthLoginReplay = getService().thirdAccountOauthLogin(map);
        SignonReplyInfo reply = oauthLoginReplay.getSignonReplyInfo();
        if (reply != null) {
            //todo TokenManager.INSTANCE.saveToken(reply);
        }
        return oauthLoginReplay;
    }

    @Override
    public ReplyInfo validateMobile(String ssoToken, String mobilePhone) {
        HashMap<String, String> map = new HashMap<>(1);
        map.put("mobilePhone", mobilePhone);
        return getService(ssoToken).validateMobile(map);
    }

/*    @Override
    public ReplyInfo sendMobileAuthCode(String ssoToken, String mobilePhone) {
        return getService(ssoToken).sendMobileAuthCode(ssoToken, mobilePhone);
    }*/

/*    @Override
    public ReplyInfo findPassswordCode(String ssoToken, String appCodeSSO, String loginName) {
        HashMap<String, String> map = new HashMap<>(2);
        map.put("loginName", loginName);
        map.put(,)
        getService(ssoToken).findPassswordCode();
        return null;
    }*/

/*    private void saveOauthToken(ThirdAccountOauthLoginReplay reply) {
        AccountSpUtil.setString(TOKEN, reply.getThirdAccessToken());
        AccountSpUtil.setString(TOKEN_REFRESH, reply.getRefreshToken());
        AccountSpUtil.setLong(TOKEN_CREATE, reply.getTokenCreateTime());
        AccountSpUtil.setLong(TOKEN_EXPIRE, reply.getTokenExpireTime());
    }*/

}