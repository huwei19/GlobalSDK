package com.pwrd.hmsea.bridge;
//huwei 2022/01/06
//sdk文档汇总： https://dev.sys.wanmei.net/#/sdks?sdkId=5&platformId=1

import android.app.Activity;
import android.app.ActivityManager;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.widget.Button;
import android.widget.LinearLayout;

import androidx.annotation.RequiresApi;
import androidx.core.content.FileProvider;

import com.unity3d.player.UnityPlayerActivity;
import com.wpsdk.activity.ActivityConfig;
import com.wpsdk.activity.ActivitySDK;
import com.wpsdk.activity.callback.ResultCallBack;
import com.wpsdk.activity.models.GameUserInfo;
import com.wpsdk.global.core.GlobalSDKPlatform;
import com.wpsdk.global.core.GlobalSDKUIPlatform;
import com.wpsdk.global.core.IGlobalSdkAPICallback;
import com.wpsdk.global.core.bean.RoleBean;
import com.wpsdk.global.core.bean.UserInfo;
import com.wpsdk.global.share.SharePlatform;
import com.wpsdk.global.share.ShareType;
import com.wpsdk.global.share.factory.ShareObj;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class MainActivity extends UnityPlayerActivity {
    private static Activity mContext;

    //1 --------  activity生命周期  开始-----------
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = this;

        Log.d("--GlobalBridge--", "------- 初始化SDK开始 ");

        //sdk相关
        GlobalSDKUIPlatform.getInstance().onCreate(this, getIntent());

        //初始化全球sdk
        initSDK();

        //初始化 活动sdk
        initActivitySDK();

        Log.d("--GlobalBridge--", "------- 初始化SDK结束 ");
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        GlobalSDKUIPlatform.getInstance().onRestart(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        GlobalSDKUIPlatform.getInstance().onRestart(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        GlobalSDKUIPlatform.getInstance().onRestart(this);
    }

    //用来获取 新打开的activity关闭后，返回的数据
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        GlobalSDKUIPlatform.getInstance().onActivityResult(mContext, requestCode, resultCode, data);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        GlobalSDKUIPlatform.getInstance().onDestroy(this);
    }

    //与启动模式配合使用
    // 第一次启动时的生命周期：onCreate–>onStart，会调用到setIntent(); 第二次启动（实例在栈中只是不在前台）：onNewIntent–>onRestart–>onStart
    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        GlobalSDKUIPlatform.getInstance().onNewIntent(this, intent);
    }
    //1 --------  activity生命周期  结束-----------


    //2 --------  unity接口 开始-----------
    //Sdk登陆
    public static void OnLogin() {
        Log.d("--GlobalBridge--", "------- 打开登陆界面");
        GlobalSDKUIPlatform.getInstance().loginBySdkView(mContext);
    }

    //支付
    public static void OnPay(String productId, String gameOrderId, String gameRoleId, String gameServerId, String gamePaySuccessUrl, String gameExtraInfo) {
        String payParams = "OnPay()-> \nproductId:" + productId + "\ngameOrderId:" + gameOrderId + "\ngameRoleId:" + gameRoleId + "\ngameServerId:" + gameServerId + "\ngamePaySuccessUrl:" + gamePaySuccessUrl + "\ngameExtraInfo:" + gameExtraInfo;
        Log.d("--GlobalBridge--", "------- 支付参数 pay params: " + payParams);
        GlobalSDKUIPlatform.getInstance().gameAddCash(mContext, productId, gameOrderId, gameRoleId, gameServerId, gamePaySuccessUrl, gameExtraInfo,
                new IGlobalSdkAPICallback.IPayCallback() {
                    @Override
                    public void onPayCancel() {
                        Log.d("--GlobalBridge--", "------- 支付取消 onPayCancel \ngameOrderId : " + gameOrderId);
                        //通知unity 支付取消
                        AndToUnity.onPayCancelled(gameOrderId);
                    }

                    @Override
                    public void onPaySuccess() {
                        Log.d("--GlobalBridge--", "------- 支付成功 onPaySuccess \ngameOrderId : " + gameOrderId);
                        //通知unity 支付成功
                        AndToUnity.onPaySucceed(gameOrderId);
                    }

                    @Override
                    public void onPayFail(int code, String message) {
                        Log.d("--GlobalBridge--", "------- 支付失败 onPayFail \ncode : " + code + " \nmessage : " + message);
                        //通知unity 支付失败
                        AndToUnity.onPayFailed(message);
                    }
                });
    }

    //获取设备id
    public static String UUID() {
        String s_UUID = GlobalSDKUIPlatform.getInstance().getDFUniqueIDs(mContext).get(GlobalSDKPlatform.ID.UD_ID);
        Log.d("--GlobalBridge--", "------- UUID 获取设备id \nUUID" + s_UUID);
        return s_UUID;
    }

    //获取用户登录状态 (是否已经登录SDk)
    @RequiresApi(api = Build.VERSION_CODES.GINGERBREAD)
    public static boolean GetOnLogin() {
        //如果 UserInfo 为空就认为 没有登录
        Boolean b_logined = !getUserInfo().isEmpty();
        Log.d("--GlobalBridge--", "------- GetOnLogin 是否已经登录SDK： \nb_logined: " + b_logined);
        return b_logined;
    }

    //获取用户信息 的json字符串
    public static String GetUserInfo() {
        UserInfo userInfo = GlobalSDKUIPlatform.getInstance().getUserInfo(mContext);
        String str_user = JsonUtil.object2Json(userInfo);
        Log.d("--GlobalBridge--", "------- \nuserInfo:" + str_user);
        return str_user;
    }

    //推送 注册即可
    public static void InitPushGameInfo(String roleid, int serverId) {
        GlobalSDKUIPlatform.getInstance().registerSdkPush(mContext, roleid, String.valueOf(serverId));
        Log.d("--GlobalBridge--", "------- 推送注册成功: \nroleid : " + roleid + " \nserverId : " + serverId);
    }

    //拍脸图 实际就是 打开活动页面 ---------->活动sdk相关
    public static void PatFaceAdvertising(String reuqestUrl, String uid, String token, String sId, String sName, String rid, String rName, String rLv, String rVip) {
        Log.d("--GlobalBridge--", "------- 拍脸图: \nsId : " + sId);
        //先设置ActivitySDK的gameUserInfo
        GameUserInfo gameUserInfo = new GameUserInfo.Builder()
                .setAppId(String.valueOf(1261))
                .setUid(uid)
                .setToken(token)
                .setServiceId(sId)
                .setServiceName(sName)
                .setRoleId(rid).setRoleName(rName)
                .setRoleLevel(rLv)
                .setRoleVip(rVip)
                .build();
        ActivitySDK.getInstance().setGameUserInfo(mContext, gameUserInfo);

        HashMap<String, String> param = new HashMap();
        param.put("signAppId", "1261@1");
        param.put("appId", "1261");
        param.put("activityType", "1");
        param.put("sdkId", "1");
        param.put("uid", uid);
        param.put("serverId", sId);
        param.put("roleId", rid);
        param.put("token", token);
        param.put("roleLevel", rLv);

        ActivitySDK.getInstance().getActivityData(mContext, reuqestUrl, "1261", param, true, false, new ActivitySDK.OnActivityDataListener() {
            public void onActivityData(boolean success, String data) {
                Log.d("--GlobalBridge--", "------- 拍脸图 onActivityData回调 \nsuccess : " + success + " \ndata : " + data);
                if (success) {
                    try {
                        JSONObject firstJson = new JSONObject(data);
                        int code = firstJson.getInt("code");
                        if (code == 0) {
                            JSONArray result = firstJson.getJSONArray("result");
                            if (result != null) {
                                JSONObject result_0 = result.getJSONObject(0);
                                if (result_0 != null) {
                                    JSONObject activityInfo = result_0.getJSONObject("activityInfo");
                                    if (activityInfo != null) {
                                        String url = activityInfo.getString("url");
                                        if (!TextUtils.isEmpty(url)) {
                                            Log.d("--GlobalBridge--", "------- 拍脸图成功打开 onActivityData \nurl : " + url);
                                            ActivitySDK.getInstance().showWebViewWithUrl(mContext, url);
                                        }
                                    }
                                }
                            }
                        }
                    } catch (Exception var9) {
                    }
                }
            }
        });
    }

    //埋点事件 角色相关埋点
    //type类型 1角色登录打点 2角色登录失败打点 3角色退出打点 4新创建角色打点 5角色升级打点
    public static void OnEvent(int type, String playerid, String name, String Lv, int ZoneId, String ZoneName, String PartyName, String Balance, String Vip, String createTime, String iP, String port, String code, String Message) {
        Log.d("--GlobalBridge--", "-------埋点 OnEvent \ntype : " + type + "\nplayerid : " + playerid);
        switch (type) {
            case 1:
                //角色登录打点
                GlobalSDKUIPlatform.getInstance().trackEventRoleLogin(mContext, playerid, String.valueOf(ZoneId), Vip, Lv, iP, port);
                break;
            case 2:
                //角色登录失败打点
                GlobalSDKUIPlatform.getInstance().trackEventRoleLoginError(mContext, playerid, String.valueOf(ZoneId), Vip, Lv, iP, port);
                break;
            case 3:
                //角色退出打点
                GlobalSDKUIPlatform.getInstance().trackEventRoleLogout(mContext, playerid, String.valueOf(ZoneId), Vip, Lv);
                break;
            case 4:
                //新创建角色打点
                GlobalSDKUIPlatform.getInstance().trackEventRoleCreate(mContext, playerid, String.valueOf(ZoneId), Vip, Lv, iP, port);
                break;
            case 5:
                //角色升级打点
                GlobalSDKUIPlatform.getInstance().trackEventRoleUpdate(mContext, playerid, String.valueOf(ZoneId), Vip, Lv);
        }
    }

    //自定义埋点 也就是统计打点
    public static void OnCustomEvent(String eventName, String eventContent) {
        Map<String, String> paramMap = new HashMap();
        paramMap.put(eventName, eventContent);

        Log.d("--GlobalBridge--", "-------自定义统计埋点 OnCustomEvent \neventName : " + eventName + "\neventContent : " + eventContent);
        GlobalSDKUIPlatform.getInstance().sdkTrackEvent(mContext, eventName, paramMap);
    }

    //广告打点
    public static void TrackEventAD(String eventName, String eventContent) {
        HashMap<String, Object> paramMap = new HashMap();
        paramMap.put(eventName, eventContent);

        Log.d("--GlobalBridge--", "-------广告打点 TrackEventAD \neventName : " + eventName + "\neventContent : " + eventContent);
        GlobalSDKUIPlatform.getInstance().trackEventAD(mContext, eventName, paramMap);
    }


    //资源埋点  state：当前状态（开始、错误、成功）
    //资源打点类型type： 1资源版本核对打点  2资源下载打点 3解压缩打点  4服务器列表打点
    public static void OnResEvent(int type, int onDownloadType, String url, String errorCode, String errorMsg) {
        //首先设置 state 类型
        String state = GlobalSDKPlatform.RecorderState.Begin;
        if (onDownloadType == 1) {
            state = GlobalSDKPlatform.RecorderState.Begin;
        } else if (onDownloadType == 2) {
            state = GlobalSDKPlatform.RecorderState.Success;
        } else if (onDownloadType == 3) {
            state = GlobalSDKPlatform.RecorderState.Error;
        }
        //日志输出
        Log.d("--GlobalBridge--", "-------资源埋点 OnResEvent \ntype : " + type + "\nstate : " + state + "\nurl : " + url + "\nerrorMsg : " + errorMsg);
        switch (type) {
            case 1:
                //资源版本 核对打点
                GlobalSDKUIPlatform.getInstance().trackGameResReqEvent(mContext, state, url, errorMsg);
                break;
            case 2:
                //资源下载 打点
                GlobalSDKUIPlatform.getInstance().trackGameUpdateAssetEvent(mContext, state, url, errorMsg);
                break;
            case 3:
                //解压缩打点
                GlobalSDKUIPlatform.getInstance().trackGameResDecEvent(mContext, state, errorMsg);
                break;
            case 4:
                //服务器列表打点
                GlobalSDKUIPlatform.getInstance().trackGameGetServerListEvent(mContext, state, url, errorMsg);
        }
    }

    //分享 海外仅facebook  分享类型：0微信好友； 1微信朋友圈； 2新浪； 3qq好友； 4qq空间 ； 5facebook
    @RequiresApi(api = Build.VERSION_CODES.GINGERBREAD)
    public static void SharePlatform(int shareAction, String mImagePath, String mTitle, String mDescription, String appName, String link) {
        Log.d("--GlobalBridge--", "-------分享 SharePlatform \nshareAction : " + shareAction + "\nmImagePath : " + mImagePath + "\nlink : " + link);
        ShareObj shareObj = null;
        if (shareAction != 5) {
            return;
        }
        final File file = new File(mImagePath);
        //final File file = new File(getExternalCacheDir(), mImagePath);
        Uri imgUri = null;
        if (file.exists()) {
            imgUri = FileProvider.getUriForFile(mContext, mContext.getPackageName() + ".globalSDK.fileProvider", file);
            shareObj = new ShareObj.Builder()
                    //分享平台
                    //SharePlatform.Facebook, 备注：Facebook
                    //SharePlatform.Ins, 备注：Ins
                    //SharePlatform.VK, 备注：VK
                    //SharePlatform.WX, 备注：微信
                    //SharePlatform.TWITTER, 备注：Twitter
                    .setSharePlatform(SharePlatform.Facebook)
                    //分享类型
                    //ShareType.Image, 备注：分享图片
                    //ShareType.Link, 备注：分享链接
                    .setShareType(ShareType.Image)
                    //分享图片调用setImagUri
                    .setImgUri(imgUri)
                    //如果分享链接，则调用setWebLink，
                    //.setWebLink(webLink)
                    //微信分享才需要调用setShareScene（0/好友，1/朋友圈）
                    //.setShareScene(shareScene)
                    .build();
        } else {
            if (!link.isEmpty()) {
                shareObj = new ShareObj.Builder()
                        //分享平台
                        //SharePlatform.Facebook, 备注：Facebook
                        //SharePlatform.Ins, 备注：Ins
                        //SharePlatform.VK, 备注：VK
                        //SharePlatform.WX, 备注：微信
                        //SharePlatform.TWITTER, 备注：Twitter
                        .setSharePlatform(SharePlatform.Facebook)
                        //分享类型
                        //ShareType.Image, 备注：分享图片
                        //ShareType.Link, 备注：分享链接
                        .setShareType(ShareType.Link)
                        //分享图片调用setImagUri
                        .setWebLink(link)
                        //如果分享链接，则调用setWebLink，
                        //.setWebLink(webLink)
                        //微信分享才需要调用setShareScene（0/好友，1/朋友圈）
                        //.setShareScene(shareScene)
                        .build();
            }
        }
        if (shareObj != null) {
            GlobalSDKUIPlatform.getInstance().shareOtherPlatforms(mContext, shareObj,
                    new IGlobalSdkAPICallback.IShareCallback() {
                        @Override
                        public void onShareSuccess() {
                            Log.d("--GlobalBridge--", "-------分享 onShareSuccess");
                            AndToUnity.OnShare("2");
                        }

                        @Override
                        public void onShareCancel() {
                            Log.d("--GlobalBridge--", "-------分享 onShareCancel");
                            AndToUnity.OnShare("2");
                        }

                        @Override
                        public void onShareFail() {
                            Log.d("--GlobalBridge--", "-------分享 onShareFail");
                            AndToUnity.OnShare("1");
                        }
                    });
        }
    }

    //问卷活动  ------
    public static void OpenShowSurvey(String url, String uid, String token, String sId, String sName, String rid, String rName, String rLv, String rVip) {
        Log.d("--GlobalBridge--", "-------活动问卷 OpenShowSurvey \nurl : " + url + "\nuid : " + uid + "\ntoken : " + token + "\nServerId : " + sId);
        GlobalSDKUIPlatform.getInstance().showSurveyUrl(mContext, url, new IGlobalSdkAPICallback.ISurveyCallback() {
            @Override
            public void onFinish(String surveyId, String answerId) {
                //问卷回答完成（surveyId：问卷id，answerId：回答者id）
                Log.d("--GlobalBridge--", "-------活动问卷 onFinish;  surveyId: " + surveyId + "\nanswerId: " + answerId);
                AndToUnity.onSurveyFinish("1");
            }

            @Override
            public void onWebClose() {
                //Web页面关闭回调
                Log.d("--GlobalBridge--", "-------活动问卷 onWebClose");
            }
        });
    }

    //检查流行机型 是否存在刘海屏
    public static boolean IsNotchScreen() {
        return NotchScreenTool.isNotch(mContext);
    }

    //联系客服  --->实际为 打开指定网页
    public static void OpenShowWebViewWithUrl(String url, String uid, String token, String sId, String sName, String rid, String rName, String rLv, String rVip) //url为网页地址
    {
        //第三个bool参数为是否显示导航栏:  注：如果游戏传入的url包含`barStyle`属性，则该参数不生效
        GlobalSDKUIPlatform.getInstance().openUrlByGame(mContext, url, true);
    }

    //获取系统内存
    public static long getSystemTotalMemory() {
        Log.d("--GlobalBridge--", "-------获取系统内存开始 getSystemTotalMemory");
        long result = 0;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            ActivityManager activityManager = (ActivityManager) mContext.getSystemService(Context.ACTIVITY_SERVICE);
            ActivityManager.MemoryInfo memoryInfo = new ActivityManager.MemoryInfo();
            activityManager.getMemoryInfo(memoryInfo);
            result = memoryInfo.totalMem;
        } else {
            try {
                BufferedReader bufferedReader = new BufferedReader(new FileReader("/proc/meminfo"));
                String[] params = bufferedReader.readLine().split("\\s+");
                result = Integer.valueOf(params[1]).intValue() * 1024;
                bufferedReader.close();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        Log.d("--GlobalBridge--", "-------获取系统内存成功 getSystemTotalMemory  \nresult : " + result);
        return result;
    }

    //登出
    public static void OnLogout() {
        Log.d("--GlobalBridge--", "-------登出 OnLogout");
        GlobalSDKUIPlatform.getInstance().logout(mContext);
    }

    //切换账号
    public static void OnSwitchAccount() {
        Log.d("--GlobalBridge--", "-------切换账号 uiSwitchAccount");
        GlobalSDKUIPlatform.getInstance().uiSwitchAccount(mContext);
    }

    //2 --------  unity接口 结束-----------


    //3 --------其他抽象出来的函数方法 开始
    //初始化 活动sdk
    private void initActivitySDK() {
        Log.d("--GlobalBridge--", "-------initActivitySDK 初始化");
        ActivityConfig config = (new ActivityConfig.Builder()).addSignInfo(String.valueOf(1261), "xwkcxwtz3v9vegzo4qrhujkqssw9jsv7")
                .setPosition(-1, -1).setBackgroundColorHex("#00000000")
                .setCutOutAdapt(true)
                .setOnJsActionListener(new ActivitySDK.OnJsActionListener() {
                    public void onJsAction(String action, ResultCallBack callBack) {
                        Log.d("--GlobalBridge--", "-------设置ActivityConfig后的回调：\naction:" + action);
                        HashMap<String, Object> hashMap = new HashMap();
                        callBack.onResultMap(hashMap);
                    }
                })
                .setChannelType(1) // 默认大陆0，海外需要单独设置.影响拍脸图活动，对象存储，朋友圈
                .build();

        //初始化ActivitySDK
        ActivitySDK.getInstance().init(mContext, config);
        Log.d("--GlobalBridge--", "-------initActivitySDK 初始化完成");
    }

    //初始化全球sdk
    private void initSDK() {
        GlobalSDKUIPlatform.getInstance().initSdk(this,
                new IGlobalSdkAPICallback.IInitCallback() {
                    @Override
                    public void finish() {
                        Log.d("--GlobalBridge--", "------- finish sdk初始化成功");
                    }
                },
                new IGlobalSdkAPICallback.ILoginCallback() {
                    @Override
                    public void onLoginSuccess(String uid, String token, String loginMethod) {
                        Log.d("--GlobalBridge--", "------- onLoginSuccess sdk登陆成功 \nuid : " + uid + " \ntoken : " + token + " \nloginMethod : " + loginMethod);
                        //通知unity 已经登录成功
                        AndToUnity.onLoginSucceed(uid, token, UUID());

                        //通知unity 获取角色信息
                        getRoles(uid, token);
                    }

                    @Override
                    public void onLoginFail(int code, String message, String loginMethod) {
                        Log.d("--GlobalBridge--", "------- onLoginFail sdk登陆失败 \ncode : " + code + " \nmessage : " + message + " \nloginMethod : " + loginMethod);
                        //通知unity 已经登录失败
                        AndToUnity.onLoginFailed();
                    }

                    @Override
                    public void onLoginCancel() {
                        Log.d("--GlobalBridge--", "------- onLoginCancel 登陆取消");
                        //通知unity 已经登录取消
                        AndToUnity.onLoginCancelled();
                    }

                    @Override
                    public void onDisagreePrivacy() {
                        Log.d("--GlobalBridge--", "------- onDisagreePrivacy 隐私不同意");
                    }
                },
                new IGlobalSdkAPICallback.ILogoutCallback() {
                    @Override
                    public void onLogoutSuccess(String logoutMethod) {
                        Log.d("--GlobalBridge--", "------- onLogoutSuccess 退出登陆成功 \n" + "logoutMethod: " + logoutMethod);
                        //通知unity 已经登出成功
                        AndToUnity.onLogoutSucceed();
                    }

                    @Override
                    public void onLogoutFail(int code, String message) {
                        Log.d("--GlobalBridge--", "------- onLogoutFail 退出登陆失败 \ncode : " + code + " \nmessage : " + message);
                        //通知unity 已经登出失败
                        AndToUnity.onLogoutFailed();
                    }
                }
        );
    }

    //获取用户角色信息
    private void getRoles(String uid, String token) {
        Log.d("--GlobalBridge--", "------- getRolesWithServerId 获取用户角色信息");
        ActivitySDK.getInstance().getRolesWithServerId(getApplicationContext(), uid, token, "",
                new ActivitySDK.OnRolesListener() {
                    @Override
                    public void onSuccess(List<com.wpsdk.activity.redeem.RoleBean> o) {
                        //获取角色信息成功
                        if (o != null && o.size() > 0) {
                            //玩家角色信息列表有数据
                            String value = "";
                            for (int i = 0; i < o.size(); i++) {
                                value = value + o.get(i).getServerId() + "," + o.get(i).getRoleName() + "," + o.get(i).getLev() + ";";
                            }
                            AndToUnity.OnGetUserRoleInfoListFinish("succeed", value);
                        } else {
                            //接口调用成功，玩家角色信息为空
                            AndToUnity.OnGetUserRoleInfoListFinish("nil", "");
                        }
                    }

                    @Override
                    public void onFail(int code, String msg) {
                        //获取角色信息失败
                        AndToUnity.OnGetUserRoleInfoListFail(msg);
                    }
                });
    }

    //获取用户信息 的json字符串
    private static String getUserInfo() {
        UserInfo userInfo = GlobalSDKUIPlatform.getInstance().getUserInfo(mContext);
        String str_user = JsonUtil.object2Json(userInfo);
        Log.d("--GlobalBridge--", "------- userInfo:" + str_user);
        return str_user;
    }

    //按键捕捉 例如安卓返回键
    @RequiresApi(api = Build.VERSION_CODES.CUPCAKE)
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            // 游戏调用自己的退出窗口
            AlertDialog alertDialog = new AlertDialog.Builder(mContext).create();
            alertDialog.setMessage("是否退出游戏？");
            alertDialog.setButton(DialogInterface.BUTTON_POSITIVE, "退出",
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            finish();
                            System.exit(0);
                            android.os.Process.killProcess(android.os.Process.myPid());
                        }
                    });
            alertDialog.setButton(DialogInterface.BUTTON_NEUTRAL, "取消",
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }

                    });

            alertDialog.show();
            Button mPositiveButton = alertDialog.getButton(AlertDialog.BUTTON_POSITIVE);
            Button mNeutralButton = alertDialog.getButton(AlertDialog.BUTTON_NEUTRAL);

            LinearLayout.LayoutParams positiveButtonLL = (LinearLayout.LayoutParams) mPositiveButton.getLayoutParams();
            positiveButtonLL.weight = 1;
            mPositiveButton.setLayoutParams(positiveButtonLL);

            LinearLayout.LayoutParams mNeutralButtonLL = (LinearLayout.LayoutParams) mNeutralButton.getLayoutParams();
            mNeutralButtonLL.weight = 1;
            mNeutralButton.setLayoutParams(mNeutralButtonLL);
        }
        return false;
    }
    //3 --------其他抽象出来的函数方法 结束
}