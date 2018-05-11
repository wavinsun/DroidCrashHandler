package cn.mutils.app.crash.handler;

import android.app.Application;
import android.os.Debug;
import android.os.Looper;

import java.io.File;
import java.util.concurrent.TimeoutException;

/**
 * Created by wenhua.ywh on 2018/2/24.
 */
class ExceptionHandler implements Thread.UncaughtExceptionHandler {

    // 已有的异常处理器
    static Thread.UncaughtExceptionHandler sSuperHandler;
    static volatile boolean sRecordCrash = false;
    static volatile Thread sProcessingThread = null;

    // 在崩溃线程调用
    private static boolean needContinue(Throwable e) {
        boolean localRecordCrash = false;
        synchronized (ExceptionHandler.class) {
            localRecordCrash = sRecordCrash;
            do {
                if (sRecordCrash) {
                    break;
                }
                sRecordCrash = true;
                sProcessingThread = Thread.currentThread();
            } while (false);
        }
        if (localRecordCrash) {
            // CrashAgain

            // 如果某个线程的崩溃正在处理中，
            //  崩溃在主线程，则主线程需要loop循环，因为主线程在某些场景下需要弹框提示
            if (sProcessingThread != null && Thread.currentThread() == Looper.getMainLooper().getThread()) {
                loop();
                return false;
            }

            // 在程序崩溃一次后，主线程再次崩溃
            if (sProcessingThread == null && Thread.currentThread() == Looper.getMainLooper().getThread()) {
                CrashUtil.exitProcess();
                return false;
            }

            // native线程不直接return，直接挂起当前线程
            if (e == null) {
                synchronized (ExceptionHandler.class) {
                    try {
                        ExceptionHandler.class.wait();
                    } catch (InterruptedException ex) {
                        ex.printStackTrace();
                    }
                }
            }
            return false;
        }
        return true;
    }

    @Override
    public void uncaughtException(Thread t, Throwable e) {
        javaException(t, e);
    }

    public static void javaException(final Thread t, final Throwable e) {
        if (e != null && (e instanceof TimeoutException)) {
            LogUtil.e(e.getMessage(), e);
            if (t == Looper.getMainLooper().getThread()) {
                loop();
            }
            return;
        }
        if (!needContinue(e)) {
            return;
        }

        dumpHeadInfo();

        Runnable r = new Runnable() {
            @Override
            public void run() {
                try {
                    try {
                        try {
                            CrashHandler.getController().onRecordStart();
                        } catch (Throwable ex) {
                            ex.printStackTrace();
                        }
                        try {
                            if (CrashHandler.getController().onRecordStartEx(t, e, null)) {
                                return;
                            }
                        } catch (Throwable ex) {
                            ex.printStackTrace();
                        }
                        String message = CrashHandler.getExceptionInfo(e);
                        CrashHandler.recordCrash(message, e, false, t);
                    } finally {
                        CrashHandler.getController().onRecordEnd();
                    }
                } catch (Throwable ex) {
                    CrashUtil.exitProcess();
                } finally {
                    sProcessingThread = null;
                }
            }
        };

        // 防止出现ANR，如果是主线程崩溃则启动一个线程处理
        if (Thread.currentThread() == Looper.getMainLooper().getThread()) {
            new Thread(r, "CrashTask").start();
            loop();
        } else {
            r.run();
        }
    }

    public static void nativeException(String message, Thread t) {

    }

    private static void dumpHeadInfo() {
        if (!CrashHandler.getController().isForceDumpHprofData()) {
            return;
        }
        try {
            File hprofFile = CrashHandler.getController().getDumpHprofDataFile();
            if (hprofFile != null) {
                if (hprofFile.exists()) {
                    hprofFile.delete();
                }
                Debug.dumpHprofData(hprofFile.getAbsolutePath());
            }
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    private static void loop() {
        try {
            Looper.loop();
        } catch (Throwable e) {
            javaException(Thread.currentThread(), e);
        } finally {
            CrashUtil.exitProcess();
        }
    }

    static void bindDefault(Application application) {
        Thread.UncaughtExceptionHandler handler = Thread.getDefaultUncaughtExceptionHandler();
        if (handler != null && !(handler instanceof ExceptionHandler)) {
            sSuperHandler = handler;
            Thread.setDefaultUncaughtExceptionHandler(new ExceptionHandler());
        }
    }
}
