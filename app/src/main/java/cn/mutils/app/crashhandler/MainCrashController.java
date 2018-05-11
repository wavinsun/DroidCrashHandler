package cn.mutils.app.crashhandler;

import android.app.Activity;
import android.app.Application;

import cn.mutils.app.crash.handler.CrashDataController;

/**
 * Created by wenhua.ywh on 2018/2/24.
 */

public class MainCrashController extends CrashDataController {
    @Override
    public Application getApplication() {
        return MainApplication.getApplication();
    }

    @Override
    public Activity getTopActivity() {
        return null;
    }
}
