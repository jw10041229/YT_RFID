package com.dwin.common_app.reveice;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;

public class BootBroadcastReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        // 检查是否是 BOOT_COMPLETED 广播
        if (Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())) {
            // 获取存储的包名和类名
            String[] appInfo = getAppInfo(context);
            String packageName = appInfo[0];
            String className = appInfo[1];

            if (!packageName.isEmpty() && !className.isEmpty()) {
                // 启动应用
                startApp(context, packageName, className);
            } else {
                Log.e("BootBroadcastReceiver", "未设置包名或类名");
            }
        }
    }

    // 启动应用的方法
    private void startApp(Context context, String packageName, String className) {
        try {
            Intent launchIntent = new Intent();
            launchIntent.setComponent(new ComponentName(packageName, className));
            launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(launchIntent);
        } catch (Exception e) {
            Log.e("BootBroadcastReceiver", "启动应用失败: " + e.getMessage());
        }
    }

    // 从 SharedPreferences 获取存储的包名和类名
    private String[] getAppInfo(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences("AppConfig", Context.MODE_PRIVATE);
        String packageName = sharedPreferences.getString("packageName", "");
        String className = sharedPreferences.getString("className", "");
        return new String[]{packageName, className};
    }
}