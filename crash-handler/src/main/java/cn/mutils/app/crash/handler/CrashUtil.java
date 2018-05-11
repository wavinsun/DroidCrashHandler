package cn.mutils.app.crash.handler;

import android.app.Activity;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

/**
 * Created by wenhua.ywh on 2018/2/24.
 */

public class CrashUtil {

    public static void exitProcess() {
        moveCurrentActivityToBack();
        System.exit(0);
    }

    public static void moveCurrentActivityToBack() {
        try {
            Activity topActivity = CrashHandler.getController().getTopActivity();
            if (topActivity != null) {
                topActivity.moveTaskToBack(true);
            }
        } catch (Throwable e) {

        }
    }

    public static void closeQuietly(Closeable closeable) {
        if (closeable != null) {
            try {
                closeable.close();
            } catch (Throwable e) {
            }
        }
    }

    public static String readString(InputStream inputStream) throws IOException {
        Reader reader = new InputStreamReader(inputStream, "UTF-8");
        StringBuilder sb = new StringBuilder();
        char[] buffer = new char[1024];
        int count = -1;
        while ((count = reader.read(buffer)) != -1) {
            sb.append(buffer, 0, count);
        }
        return sb.toString();
    }

    public static String getLogcat() {
        final StringBuilder mainLog = new StringBuilder();
        final StringBuilder systemLog = new StringBuilder();

        List<Callable<Object>> callableList = new ArrayList<Callable<Object>>();
        callableList.add(new Callable<Object>() {
            @Override
            public Object call() throws Exception {
                try {
                    mainLog.append("-----main log-----\n");
                    Process process = Runtime.getRuntime().exec("logcat -d -v threadtime -b main -t 1000");
                    process.wait();
                    mainLog.append(readString(process.getInputStream()));
                } catch (Throwable e) {
                }
                return null;
            }
        });
        callableList.add(new Callable<Object>() {
            @Override
            public Object call() throws Exception {
                try {
                    systemLog.append("-----system log-----\n");
                    Process process = Runtime.getRuntime().exec("logcat -d -v threadtime -b system -t 1000");
                    process.waitFor();
                    systemLog.append(readString(process.getInputStream()));
                } catch (Throwable e) {

                }
                return null;
            }
        });
        CrashUtil.exec(callableList, 2000, 2);

        return new StringBuilder("\nLogcat:\n").
                append(mainLog).append(systemLog).toString();
    }

    public static <T> List<Future<T>> exec(List<Callable<T>> list, long timeout_ms, int threadCount) {
        int tCount = 0;
        if (threadCount > 0) {
            tCount = threadCount;
        }
        if (tCount <= 0) {
            tCount = Runtime.getRuntime().availableProcessors() + 1;
        }
        ExecutorService es = Executors.newFixedThreadPool(tCount, new ThreadFactory() {
            int count = 1;

            @Override
            public Thread newThread(Runnable r) {
                return new Thread(r, "CH-EXEC#" + (count++));
            }
        });
        try {
            if (timeout_ms > 0) {
                return es.invokeAll(list, timeout_ms, TimeUnit.MILLISECONDS);
            } else {
                return es.invokeAll(list);
            }
        } catch (Throwable e) {
            e.printStackTrace();
        }
        es.shutdownNow();
        return null;
    }

}
