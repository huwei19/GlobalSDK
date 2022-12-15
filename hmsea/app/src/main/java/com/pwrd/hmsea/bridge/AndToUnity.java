package com.pwrd.hmsea.bridge;
//huwei 2022/01/06

import android.util.Log;

import com.unity3d.player.UnityPlayer;

import org.json.JSONObject;

public class AndToUnity {
    //登陆成功
    public static void onLoginSucceed(String userId, String token, String uuid) {
        try {
            JSONObject firstJson = new JSONObject();
            firstJson.put("uid", userId); //固定为uid，具体可以看客户端 AndroidToUnity.cs文件的onLoginSucceed方法
            firstJson.put("token", token);
            firstJson.put("UUID", uuid);
            CallUnityMethod("onLoginSucceed", firstJson.toString());
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    //登陆失败
    public static void onLoginFailed() {
        try {
            CallUnityMethod("onLoginFailed", "");
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    //登陆取消
    public static void onLoginCancelled() {
        try {
            CallUnityMethod("onLoginCancelled", "");
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    //登出成功
    public static void onLogoutSucceed() {
        try {
            CallUnityMethod("onLogoutSucceed", "");
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    //登出失败
    public static void onLogoutFailed() {
        try {
            CallUnityMethod("onLogoutFailed", "");
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }


    //退出游戏
    public static void onExit() {
        try {
            CallUnityMethod("onExit", "");
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    //支付成功
    public static void onPaySucceed(String commonOrderId) {
        try {
            CallUnityMethod("onPaySucceed", commonOrderId);
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    //支付失败
    public static void onPayFailed(String msg) {
        try {
            CallUnityMethod("onPayFailed", msg);
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    //支付取消
    public static void onPayCancelled(String commonOrderId) {
        try {
            CallUnityMethod("onPayCancelled", commonOrderId);
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    //支付未知
    public static void onPayUnkown(String msg) {
        try {
            CallUnityMethod("onPayUnkown", msg);
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    //问卷成功
    public static void onSurveyFinish(String commonOrderId) {
        try {
            CallUnityMethod("onSurveyFinish", commonOrderId);
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
    //分享回调
    public static void OnShare(String code) {
        try {
            CallUnityMethod("OnShare", code);
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
    //--------  unity接口 结束-----------
    //角色信息列表回调
    public static void OnGetUserRoleInfoListFinish(String result,String value) {
        try {
            JSONObject firstJson = new JSONObject();
            firstJson.put("result", result);
            firstJson.put("value", value);
            CallUnityMethod("onGetUserRoleInfoListResult",firstJson.toString());
        }catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public static void OnGetUserRoleInfoListFail(String message) {
        try {
            JSONObject firstJson = new JSONObject();
            firstJson.put("result", "fail");
            firstJson.put("message", "message");
            CallUnityMethod("onGetUserRoleInfoListResult",firstJson.toString());
        }catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
    //其他函数方法
    public static void OnUnityLog(String status) {
        try {
            CallUnityMethod("OnUnityLog", status);
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    //调用unity内置的方法，想unity传递信息
    static void CallUnityMethod(String fun, String str) {
        UnityPlayer.UnitySendMessage("SdkObject", fun, str);
    }

}
