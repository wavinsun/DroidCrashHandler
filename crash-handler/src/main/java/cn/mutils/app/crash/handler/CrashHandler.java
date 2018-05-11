package cn.mutils.app.crash.handler;

import java.io.PrintWriter;
import java.io.StringWriter;

/**
 * Created by wenhua.ywh on 2018/2/24.
 */

public class CrashHandler {

    private static CrashDataController sController;

    public static void init(CrashDataController controller) {
        if (controller == null) {
            throw new RuntimeException("Controller is null!");
        }
        sController = controller;
        ExceptionHandler.bindDefault(sController.getApplication());
    }

    public static CrashDataController getController() {
        return sController;
    }

    public static String getExceptionInfo(Throwable e) {
        StringBuilder info = new StringBuilder();
        PrintWriter writer = null;
        try {
            writer = new PrintWriter(new StringWriter());
            e.printStackTrace(writer);
            info.append(writer.toString());
            info.append(getAppendInfo(e));
        } catch (Throwable ex) {
        } finally {
            CrashUtil.closeQuietly(writer);
        }
        return info.toString();
    }

    public static void recordCrash(String exceptionInfo, Throwable e, boolean isHeapError, Thread t) {
        LogUtil.e("Begin record crash");
        try {
            LogUtil.e("Finish record crash");
        } catch (Throwable ex) {
            e.printStackTrace();
        }
    }

    private static String getAppendInfo(Throwable e) {
        StringBuilder sb = new StringBuilder();
        sb.append(CrashUtil.getLogcat());
        return sb.toString();
    }

}
