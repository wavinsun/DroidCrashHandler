package cn.mutils.app.crash.handler;

import android.text.TextUtils;
import android.util.Log;

import java.lang.reflect.Method;

/**
 * Created by wenhua.ywh on 2018/2/24.
 */

public class LogUtil {

    private static final String DEFAULT_TAG = "CrashHandler";

    private static Method sLogE = null;

    static {
        try {
            Class<?> logClass = Class.forName("android.util.Log");
            sLogE = logClass.getMethod("e", String.class, String.class);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void e(String message) {
        e(DEFAULT_TAG, message);
    }

    public static void e(String tag, String message) {
        if (sLogE == null) {
            new NullPointerException("android.util.Log#e() is not found!").printStackTrace();
        } else {
            tag = TextUtils.isEmpty(tag) ? DEFAULT_TAG : tag;
            try {
                sLogE.invoke(null, tag, message);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public static void e(String tag, String message, Throwable e) {
        e(tag, message + "\n" + Log.getStackTraceString(e));
    }

    public static void e(String message, Throwable e) {
        e(DEFAULT_TAG, message, e);
    }

}
