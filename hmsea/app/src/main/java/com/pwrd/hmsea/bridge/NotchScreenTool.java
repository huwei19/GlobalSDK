package com.pwrd.hmsea.bridge;

import java.lang.reflect.Constructor;
//YYL
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.os.Build;
import android.util.Log;
import android.view.DisplayCutout;
import android.view.View;
import android.view.Window;
import android.view.WindowInsets;
import android.view.WindowManager;
import android.view.WindowManager.LayoutParams;

public class NotchScreenTool {
    //刘海屏、水滴屏等异型屏支持的Android系统版本：8.0-》全面屏  8.0以上-》刘海屏、水滴屏等异型屏
    public static boolean isNotchSupportVersion() {
        int curApiVersion = Build.VERSION.SDK_INT;
        if (curApiVersion > 26) {
            return true;
        }
        return false;
    }

    //检查流行机型是否存在刘海屏
    public static boolean isNotch(Activity context) {
        if (isNotchSupportVersion()) {
            if (isNotch_VIVO(context) || isNotch_OPPO(context) || isNotch_HUAWEI(context) || isNotch_XIAOMI(context) || isNotch_AndP(context) != null)
                return true;
            else
                return false;
        } else
            return false;
    }


    //检查vivo是否存在刘海屏、水滴屏等异型屏
    public static boolean isNotch_VIVO(Activity context) {
        boolean isNotch = false;
        try {
            ClassLoader cl = context.getClassLoader();
            Class cls = cl.loadClass("android.util.FtFeature");
            Method method = cls.getMethod("isFeatureSupport", int.class);
            isNotch = (boolean) method.invoke(cls, 0x00000020);//0x00000020：是否有刘海  0x00000008：是否有圆角
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } finally {
            return isNotch;
        }
    }

    //检查oppo是否存在刘海屏、水滴屏等异型屏
    public static boolean isNotch_OPPO(Activity context) {
        boolean isNotch = false;
        try {
            isNotch = context.getPackageManager().hasSystemFeature("com.oppo.feature.screen.heteromorphism");
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            return isNotch;
        }
    }

    //------------ huawei start ---------------
    //检查huawei是否存在刘海屏、水滴屏等异型屏
    public static boolean isNotch_HUAWEI(Activity context) {
        boolean isNotch = false;
        try {
            ClassLoader cl = context.getClassLoader();
            Class cls = cl.loadClass("com.huawei.android.util.HwNotchSizeUtil");
            Method method = cls.getMethod("hasNotchInScreen");
            isNotch = (boolean) method.invoke(cls);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (isNotch)
                setFullScreenWindowLayoutInDisplayCutout(context.getWindow());
            return isNotch;
        }
    }

    /*刘海屏全屏显示FLAG*/
    public static final int FLAG_NOTCH_SUPPORT = 0x00010000;

    /**
     * 设置应用窗口在华为刘海屏手机使用刘海区
     *
     * @param window 应用页面window对象
     */
    public static void setFullScreenWindowLayoutInDisplayCutout(Window window) {
        if (window == null) {
            return;
        }
        try {
            LayoutParams layoutParams = window.getAttributes();
            Class layoutParamsExCls = Class.forName("com.huawei.android.view.LayoutParamsEx");
            Constructor con = layoutParamsExCls.getConstructor(LayoutParams.class);
            Object layoutParamsExObj = con.newInstance(layoutParams);
            Method method = layoutParamsExCls.getMethod("addHwFlags", int.class);
            method.invoke(layoutParamsExObj, FLAG_NOTCH_SUPPORT);
        } catch (ClassNotFoundException | NoSuchMethodException | IllegalAccessException | InstantiationException
                | InvocationTargetException e) {
            Log.e("test", "hw add notch screen flag api error");
        } catch (Exception e) {
            Log.e("test", "other Exception");
        }
    }
    //------------ huawei end ---------------

    //���xiaomi�Ƿ������������ˮ������������
    public static boolean isNotch_XIAOMI(Activity context) {
        boolean isNotch = false;
        try {
            ClassLoader cl = context.getClassLoader();
            Class cls = cl.loadClass("android.os.SystemProperties");
            Method method = cls.getMethod("getInt", String.class, int.class);
            isNotch = ((int) method.invoke(null, "ro.miui.notch", 0) == 1);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } finally {
            return isNotch;
        }
    }

    //检查xiaomi是否存在刘海屏、水滴屏等异型屏
    @SuppressLint("NewApi")
    public static DisplayCutout isNotch_AndP(Activity activity) {
        View decorView = activity.getWindow().getDecorView();
        if (decorView != null && Build.VERSION.SDK_INT >= 28) {
            WindowInsets windowInsets = decorView.getRootWindowInsets();
            if (windowInsets != null)
                return windowInsets.getDisplayCutout();
        }
        return null;
    }
}
