package net.swimmi.timelimit;

import android.app.ActivityManager;
import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Binder;
import android.os.IBinder;
import android.os.SystemClock;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

public class WatchDogService extends Service {

    private List<String> packageList = new ArrayList<>();
    private static final String TAG = "WatchDogService";
    private static boolean flag = true;
    private ActivityManager am;
    private SharedPreferences isOpenSP;
    private SharedPreferences.Editor editor;

    @Override
    public void onCreate() {
        super.onCreate();
        //服务一旦启动要在后台监视任务栈最顶端应用
        am = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        isOpenSP = getSharedPreferences("isOpen", Context.MODE_PRIVATE);
        editor = isOpenSP.edit();

        new Thread() {
            @Override
            public void run() {
                super.run();
                while (flag) {
                    synchronized (WatchDogService.class) {
                        List<ActivityManager.RunningTaskInfo> runningTasks = am.getRunningTasks(1);
                        ActivityManager.RunningTaskInfo runningTaskInfo = runningTasks.get(0);
                        ComponentName topActivity = runningTaskInfo.topActivity;
                        String packageName = topActivity.getPackageName();

                        if (packageList.contains(packageName)) {
                            // 判断packageName是否已打开
                            boolean isOpen = isOpenSP.getBoolean(packageName, false);
                            if (!isOpen) {
                                editor.putBoolean(packageName, true);
                                editor.apply();
                                Log.i(TAG, packageName + "正在存储");
                            }
                        }
                        SystemClock.sleep(500);
                    }
                    //Log.i(TAG, "服务在循环");
                }
            }
        }.start();
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        flag = false;
    }

    public class MyBinder extends Binder implements IMyBinder {

        @Override
        public void setPackageNames(List<String> packageNames) {
            packageList.clear();
            packageList.addAll(packageNames);
        }
    }

    MyBinder myBinder = new MyBinder();

    @Override
    public IBinder onBind(Intent intent) {
        return myBinder;
    }
}
