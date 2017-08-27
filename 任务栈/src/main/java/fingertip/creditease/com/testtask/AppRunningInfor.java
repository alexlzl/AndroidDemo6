package fingertip.creditease.com.testtask;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningAppProcessInfo;
import android.app.ActivityManager.RunningTaskInfo;
import android.content.ComponentName;
import android.content.Context;
import android.text.TextUtils;

import java.util.List;

/**
 * Describe: 获取APP运行信息
 * <p>
 * Author: lzl
 * <p>
 * Time 16/9/23 下午3:52
 */
public class AppRunningInfor {


    /**
     * Describe: 获取APP是否在前台运行
     * <p>
     * Author: lzl
     * <p>
     * Time 16/9/23 下午3:49
     */
    public static boolean isRunningForeground(Context context) {
        ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        ComponentName cn = am.getRunningTasks(1).get(0).topActivity;
        String currentPackageName = cn.getPackageName();
        if (!TextUtils.isEmpty(currentPackageName) && currentPackageName.equals(context.getPackageName())) {
            return true;
        }
        return false;
    }


    /**
     * Describe: 判断ACTIVITY是否运行在前台
     * <p>
     * Author: lzl
     * <p>
     * Time 16/9/23 下午3:51
     */
    public static boolean isActivityRunningForeground(Context context, String className) {
        if (context == null || TextUtils.isEmpty(className)) {
            return false;
        }

        ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        List<RunningTaskInfo> list = am.getRunningTasks(1);
        if (list != null && list.size() > 0) {
            ComponentName cpn = list.get(0).topActivity;
            if (className.equals(cpn.getClassName())) {
                return true;
            } else {
                return false;
            }
        } else {
            return false;
        }

    }

    /**
     * Describe: 判断APP是否前台在运行
     * <p>
     * Author: lzl
     * <p>
     * Time: 16/9/30 下午5:42
     */
    public static boolean isAppRunning(Context context) {
        ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        List<RunningTaskInfo> list = am.getRunningTasks(100);
        boolean isAppRunning = false;
        String MY_PKG_NAME = context.getPackageName();
        for (RunningTaskInfo info : list) {
            if (info.topActivity.getPackageName().equals(MY_PKG_NAME)
                    || info.baseActivity.getPackageName().equals(MY_PKG_NAME)) {
                isAppRunning = true;
                break;

            }
        }

        return isAppRunning;
    }

    /**
     * Describe: 由进程id获取进程名称
     * <p>
     * Author: lzl
     * <p>
     * Time 16/9/23 下午3:52
     */
    public static String getProcessName(Context cxt, int pid) {
        ActivityManager am = (ActivityManager) cxt.getSystemService(Context.ACTIVITY_SERVICE);
        List<RunningAppProcessInfo> runningApps = am.getRunningAppProcesses();
        if (runningApps == null) {
            return null;
        }
        for (RunningAppProcessInfo procInfo : runningApps) {
            if (procInfo.pid == pid) {
                return procInfo.processName;
            }
        }
        return null;
    }
}
