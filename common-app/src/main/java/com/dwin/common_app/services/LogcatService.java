package com.dwin.common_app.services;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;
import android.text.TextUtils;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import com.dwin.common_app.R;

import java.io.IOException;

public class LogcatService extends Service {
    public static final String TAG = LogcatService.class.getSimpleName();

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "LogcatService onCreate");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "LogcatService onStartCommand");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel("channel_id", "channel_name", NotificationManager.IMPORTANCE_DEFAULT);
            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(channel);
            Notification notification = new NotificationCompat.Builder(this, "channel_id")
                    .setContentTitle("Foreground Service")
                    .setContentText("Service is running in the foreground")
                    .setSmallIcon(R.mipmap.ic_launcher_round)
                    .build();
            startForeground(1, notification);
        } else {
            startForeground(1, new Notification());
        }

        String extra = intent.getStringExtra("log_path");
        int file_szie = intent.getIntExtra("file_szie", 1024);
        int file_num = intent.getIntExtra("file_num", 10);
        startLogSave(extra, file_szie, file_num);
        return START_STICKY;
    }

    @Override
    public void onTaskRemoved(Intent rootIntent) {
        //super.onTaskRemoved(rootIntent);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "LogcatService onDestroy");
        stopLogSave();
        stopForeground(true);
        stopSelf();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private Process mProcess;
    private String mLogPath = "";
    private boolean isStartLog = false;

    /**
     * 保存日志到指定路径
     *
     * @param logPath 日志保存路径
     * @param r       单个文件保存大小，单位KB
     *                默认值为1024KB
     *                设置范围[1024，10240]
     * @param n       日志输出最大个数
     *                默认值为10
     *                设置范围[1，100]
     */
    public synchronized void startLogSave(String logPath, int r, int n) {
        if (TextUtils.isEmpty(logPath)) {
            return;
        }

        if (r < 1024 || r > 10240) {
            r = 1024;
        }

        if (n < 1 || n > 100) {
            r = 10;
        }

        if (mProcess != null) {
            stopLogSave();
        }
        try {
            String cmd = "logcat -f " + logPath + " -v time -r " + r + " -n " + n;
            mProcess = Runtime.getRuntime().exec(cmd);
            mLogPath = logPath;
            isStartLog = true;
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private synchronized void stopLogSave() {
        isStartLog = false;
        if (mProcess != null) {
            mProcess.destroy();
            mProcess = null;
        } else {
            if (TextUtils.isEmpty(mLogPath)) {
                return;
            }
            try {
                String cmd = "pkill -f logcat -f " + mLogPath;
                Process process = Runtime.getRuntime().exec(cmd);
                process.destroy();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private boolean isLogSaving() {
        return isStartLog;
    }
}
