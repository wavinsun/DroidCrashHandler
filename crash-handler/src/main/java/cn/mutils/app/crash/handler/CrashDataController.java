package cn.mutils.app.crash.handler;

import android.app.Activity;
import android.app.Application;

import java.io.File;

/**
 * Created by wenhua.ywh on 2018/2/24.
 */
public abstract class CrashDataController {

    public abstract Application getApplication();

    public abstract Activity getTopActivity();

    public boolean isForceDumpHprofData() {
        return false;
    }

    public File getDumpHprofDataFile() {
        return null;
    }

    public void onRecordStart() {

    }

    // 如果返回true代表第三方自己处理纪录
    public boolean onRecordStartEx(Thread t, Throwable e, String message) {
        return false;
    }

    public void onRecordEnd() {

    }

}
