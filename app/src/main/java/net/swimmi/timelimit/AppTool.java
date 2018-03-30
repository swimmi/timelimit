package net.swimmi.timelimit;

import android.app.ActivityManager;
import android.app.Application;
import android.app.usage.UsageStats;
import android.app.usage.UsageStatsManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.provider.Settings;
import android.util.Log;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Comparator;
import java.util.List;

import static android.content.Context.USAGE_STATS_SERVICE;

/**
 * Created by swimmi on 2018/3/29.
 */

public class AppTool {
    static  String TAG = "AppTool";

    public static List<AppInfo> getAppList(PackageManager packageManager) {
        List<AppInfo> appInfoList = new ArrayList<>();
        try {
            List<PackageInfo> packageInfoList = packageManager.getInstalledPackages(0);
            for (int i = 0; i < packageInfoList.size(); i++) {
                PackageInfo packageInfo = packageInfoList.get(i);
                if ((ApplicationInfo.FLAG_SYSTEM & packageInfo.applicationInfo.flags) != 0) {
                    continue;
                }
                AppInfo appInfo = new AppInfo();
                if (packageInfo.applicationInfo.loadIcon(packageManager) == null) {
                    continue;
                }

                appInfo.setPackageName(packageInfo.packageName);
                appInfo.setIcon(packageInfo.applicationInfo.loadIcon(packageManager));
                appInfo.setAppName(packageInfo.applicationInfo.loadLabel(packageManager).toString());
                appInfo.setLimited(MainActivity.isLimitedSP.contains(packageInfo.packageName));
                appInfoList.add(appInfo);
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        return appInfoList;
    }

    public static List<AppInfo> getAppStatus(Context context, final List<String> appList) {

        PackageManager pm = context.getPackageManager();
        if(PackageManager.PERMISSION_GRANTED == pm.checkPermission("android.permission.PACKAGE_USAGE_STATS", context.getPackageName()))
            if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.LOLLIPOP)
                context.startActivity(new Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS));
        List<AppInfo> appInfoList = new ArrayList<>();
        Calendar beginCal = Calendar.getInstance();
        beginCal.add(Calendar.HOUR_OF_DAY, -1);
        Calendar endCal = Calendar.getInstance();
        UsageStatsManager manager= (UsageStatsManager)context.getSystemService(USAGE_STATS_SERVICE);
        List<UsageStats> stats = manager.queryUsageStats(UsageStatsManager.INTERVAL_DAILY,beginCal.getTimeInMillis(),endCal.getTimeInMillis());

        //今日打开过的应用
        for(UsageStats us : stats){
            if (!appList.contains(us.getPackageName())) {
                continue;
            }
            appList.remove(us.getPackageName());
            try {
                ApplicationInfo applicationInfo =  pm.getApplicationInfo(us.getPackageName(),PackageManager.GET_META_DATA);
                if((applicationInfo.flags & applicationInfo.FLAG_SYSTEM) <= 0){
                    AppInfo appInfo = new AppInfo();
                    appInfo.setIcon(applicationInfo.loadIcon(pm));
                    appInfo.setPackageName(applicationInfo.packageName);
                    appInfo.setAppName(applicationInfo.loadLabel(pm).toString());
                    appInfo.setTimeLong(us.getTotalTimeInForeground());
                    appInfo.setTimeStr(CommonUtil.longToString(us.getTotalTimeInForeground()));
                    appInfoList.add(appInfo);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        //今日未打开的应用
        /*for(String packageName : appList) {
            try {
                ApplicationInfo applicationInfo =  pm.getApplicationInfo(packageName,PackageManager.GET_META_DATA);
                AppInfo appInfo = new AppInfo();
                appInfo.setIcon(applicationInfo.loadIcon(pm));
                appInfo.setPackageName(applicationInfo.packageName);
                appInfo.setAppName(applicationInfo.loadLabel(pm).toString());
                appInfo.setTimeLong(0);
                appInfoList.add(appInfo);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }*/

        Comparator c = new Comparator<AppInfo>() {
            @Override
            public int compare(AppInfo a1, AppInfo a2) {
                if(a1.getTimeLong() < a2.getTimeLong())
                    return 1;
                else return -1;
            }
        };
        appInfoList.sort(c);
        return appInfoList;
    }
}
