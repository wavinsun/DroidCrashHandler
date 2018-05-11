package cn.mutils.app.crashhandler;

import android.app.Application;

import cn.mutils.app.crash.handler.CrashHandler;

/**
 * Created by wenhua.ywh on 2018/2/24.
 */

public class MainApplication extends Application {

    private static Application sApplication;

    @Override
    public void onCreate() {
        super.onCreate();
        sApplication = this;
        CrashHandler.init(new MainCrashController());
    }

    public static Application getApplication() {
        return sApplication;
    }
}
