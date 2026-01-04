package com.dwin.common_app.util;

import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.backup.BackupManager;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.SystemClock;
import android.os.storage.StorageManager;
import android.provider.Settings;
import android.util.Log;

import com.dwin.dw_android_11_sdk.DWErrorCode;
import com.dwin.dw_android_11_sdk.StorageBean;

import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class SystemUtils {
    private static final String TAG = SystemUtils.class.getSimpleName();

    /**
     * 设置系统时间
     *
     * @param hour   小时
     * @param minute 分钟
     */
    public int setTime(int hour, int minute) {
        try {
            Calendar calendar = Calendar.getInstance();
            calendar.set(Calendar.HOUR_OF_DAY, hour);
            calendar.set(Calendar.MINUTE, minute);
            long timeMills = calendar.getTimeInMillis();
            boolean b = SystemClock.setCurrentTimeMillis(timeMills);
            return b ? DWErrorCode.DW_COMMON_SUCCESS : DWErrorCode.DW_TIME_OR_DATE_SET_ERROR;
        } catch (Exception e) {
            e.printStackTrace();
            return DWErrorCode.DW_TIME_OR_DATE_SET_ERROR;
        }
    }

    /**
     * 设置系统时间
     *
     * @param hour   小时
     * @param minute 分钟
     */
    public int setTime(int year, int month, int day, int hour, int minute) {
        try {
            Calendar calendar = Calendar.getInstance();
            calendar.set(Calendar.YEAR, year);
            calendar.set(Calendar.MONTH, month - 1);
            calendar.set(Calendar.DAY_OF_MONTH, day);
            calendar.set(Calendar.HOUR_OF_DAY, hour);
            calendar.set(Calendar.MINUTE, minute);
            long timeMills = calendar.getTimeInMillis();
            boolean b = SystemClock.setCurrentTimeMillis(timeMills);
            return b ? DWErrorCode.DW_COMMON_SUCCESS : DWErrorCode.DW_TIME_OR_DATE_SET_ERROR;
        } catch (Exception e) {
            e.printStackTrace();
            return DWErrorCode.DW_TIME_OR_DATE_SET_ERROR;
        }
    }

    /**
     * 设置系统日期
     *
     * @param year  年
     * @param month 月
     * @param day   日
     */
    public int setDate(int year, int month, int day) {
        try {
            Calendar calendar = Calendar.getInstance();
            calendar.set(Calendar.YEAR, year);
            calendar.set(Calendar.MONTH, month - 1);
            calendar.set(Calendar.DAY_OF_MONTH, day);
            long timeMills = calendar.getTimeInMillis();
            boolean b = SystemClock.setCurrentTimeMillis(timeMills);
            return b ? DWErrorCode.DW_COMMON_SUCCESS : DWErrorCode.DW_TIME_OR_DATE_SET_ERROR;
        } catch (Exception e) {
            e.printStackTrace();
            return DWErrorCode.DW_TIME_OR_DATE_SET_ERROR;
        }
    }

    /**
     * 设置系统是否自动获取时间
     *
     * @param context 上下文
     * @param auto    1:自动获取时间，0:不自动获取时间
     */
    public int setTimeAutoMode(Context context, int auto) {
        boolean result = Settings.Global.putInt(context.getContentResolver(), Settings.Global.AUTO_TIME, auto);
        return result ? DWErrorCode.DW_COMMON_SUCCESS : DWErrorCode.DW_COMMON_ERROR_SET_SETTINGS_GLOBAL;
    }

    /**
     * 判断系统是否自动获取时间
     *
     * @param context 上下文
     * @return true:自动获取时间，false:不自动获取时间
     */
    public boolean checkTimeAutoMode(Context context) {
        try {
            return Settings.Global.getInt(context.getContentResolver(), Settings.Global.AUTO_TIME) > 0;
        } catch (Settings.SettingNotFoundException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * 设置系统时区
     *
     * @param timeZone 时区ID
     */
    @SuppressLint("MissingPermission")
    public void setTimeZone(Context context, String timeZone) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        alarmManager.setTimeZone(timeZone);
    }

    /**
     * 设置系统是否自动获取时区
     *
     * @param context 上下文
     * @param auto    1:自动获取时区，0:不自动获取时区
     */
    public int setTimeZoneAutoMode(Context context, int auto) {
        boolean result = Settings.Global.putInt(context.getContentResolver(), Settings.Global.AUTO_TIME_ZONE, auto);
        return result ? DWErrorCode.DW_COMMON_SUCCESS : DWErrorCode.DW_COMMON_ERROR_SET_SETTINGS_GLOBAL;
    }

    /**
     * 判断系统是否自动获取时区
     *
     * @param context 上下文
     */
    public boolean checkTimeZoneAutoMode(Context context) {
        try {
            return Settings.Global.getInt(context.getContentResolver(), Settings.Global.AUTO_TIME_ZONE) > 0;
        } catch (Settings.SettingNotFoundException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * 设置系统是否使用24小时制
     *
     * @param context 上下文
     * @param value   "24"、"12"
     */
    public int setTime_12_24Mode(Context context, String value) {
        boolean result = Settings.System.putString(context.getContentResolver(), Settings.System.TIME_12_24, value);
        return result ? DWErrorCode.DW_COMMON_SUCCESS : DWErrorCode.DW_COMMON_ERROR_SET_SETTINGS_SYSTEM;
    }

    /**
     * 判断系统是否使用24小时制
     *
     * @param context 上下文
     */
    public boolean checkTime_12_24Mode(Context context) {
        String result = Settings.System.getString(context.getContentResolver(), Settings.System.TIME_12_24);
        return "24".equals(result);
    }

    public int setLanguage(Locale locale) {
        try {
            Object objIActMag, objActMagNative;
            Class clzIActMag = Class.forName("android.app.IActivityManager");

            Class clzActMagNative = Class.forName("android.app.ActivityManagerNative");
            //amn = ActivityManagerNative.getDefault();
            Method mtdActMagNative$getDefault = clzActMagNative.getDeclaredMethod("getDefault");
            objIActMag = mtdActMagNative$getDefault.invoke(clzActMagNative);
            // objIActMag = amn.getConfiguration();
            Method mtdIActMag$getConfiguration = clzIActMag.getDeclaredMethod("getConfiguration");
            Configuration config = (Configuration) mtdIActMag$getConfiguration.invoke(objIActMag);
            // set the locale to the new value
            config.locale = locale;

            //持久化  config.userSetLocale = true;
            Class clzConfig = Class
                    .forName("android.content.res.Configuration");
            java.lang.reflect.Field userSetLocale = clzConfig
                    .getField("userSetLocale");
            userSetLocale.set(config, true);

            //如果有阿拉伯语，必须加上，否则阿拉伯语与其它语言切换时，布局与文字方向不会改变
            Method setLayoutDirection = clzConfig.getDeclaredMethod("setLayoutDirection", Locale.class);
            setLayoutDirection.invoke(config, locale);

            // 此处需要声明权限:android.permission.CHANGE_CONFIGURATION
            // 会重新调用 onCreate();
            Class[] clzParams = {Configuration.class};
            // objIActMag.updateConfiguration(config);
            Method mtdIActMag$updateConfiguration = clzIActMag.getDeclaredMethod("updateConfiguration", clzParams);

            mtdIActMag$updateConfiguration.invoke(objIActMag, config);
            BackupManager.dataChanged("com.android.providers.settings");

        } catch (Exception e) {
            e.printStackTrace();
            return DWErrorCode.DW_LANGUAGE_SET_ERROR;
        }
        return DWErrorCode.DW_COMMON_SUCCESS;
    }

    public ArrayList<StorageBean> readExternalStoragePath(Context context) {
        ArrayList<StorageBean> mLists = new ArrayList<>();
        StorageManager mStorageManager = (StorageManager) context.getSystemService(Context.STORAGE_SERVICE);
        Class<?> volumeInfoClazz;
        Class<?> diskInfoClaszz;
        try {
            volumeInfoClazz = Class.forName("android.os.storage.VolumeInfo");
            diskInfoClaszz = Class.forName("android.os.storage.DiskInfo");
            Method mStorageManagerGetVolumes = Class.forName("android.os.storage.StorageManager").getMethod("getVolumes");
            Method mVolumeInfoGetDisk = volumeInfoClazz.getMethod("getDisk");
            Method mVolumeInfoGetPath = volumeInfoClazz.getMethod("getPath");
            Method mDiskInfoIsUsb = diskInfoClaszz.getMethod("isUsb");
            Method mDiskInfoIsSd = diskInfoClaszz.getMethod("isSd");
            //Method getState = volumeInfoClazz.getMethod("getState");
            List<Object> mListVolumeInfo = (List<Object>) mStorageManagerGetVolumes.invoke(mStorageManager);
            assert mListVolumeInfo != null;
            for (int i = 0; i < mListVolumeInfo.size(); i++) {
                Object volumeInfo = mListVolumeInfo.get(i);
                Object diskInfo = mVolumeInfoGetDisk.invoke(volumeInfo);
                //int state = (int) getState.invoke(volumeInfo);
                if (diskInfo == null) continue;
                boolean sd = (boolean) mDiskInfoIsSd.invoke(diskInfo);
                boolean usb = (boolean) mDiskInfoIsUsb.invoke(diskInfo);
                File file = (File) mVolumeInfoGetPath.invoke(volumeInfo);
                if (file != null) {
                    //android.os.StatFs statfs = new android.os.StatFs(file.getAbsolutePath());
                    //long nBlocSize = statfs.getBlockSizeLong();
                    //long blockCountLong = statfs.getBlockCountLong();
                    //long nAvailaBlock = statfs.getAvailableBlocksLong();
                    //long totalSize = blockCountLong * nBlocSize;
                    //long avlableSize = nBlocSize * nAvailaBlock;

                    //Log.d(TAG, "sd:" + sd
                    //        + "  ,usb:" + usb
                    //        + "  ,path:" + file.getAbsolutePath());
                    StorageBean bean = new StorageBean();
                    bean.setSdcard(sd);
                    bean.setUSB(usb);
                    bean.setPath(file.getAbsolutePath());
                    mLists.add(bean);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            Log.d(TAG, "getStoragePath error:" + e.getMessage());
            return null;
        }
        return mLists;
    }

    public int installApkSilently(File apkfile) {
        Process process = null;
        OutputStream out = null;
        DataOutputStream dataOutputStream = null;
        try {
            process = Runtime.getRuntime().exec("su");
            out = process.getOutputStream();
            dataOutputStream = new DataOutputStream(out);
            dataOutputStream.writeBytes("chmod 777 " + apkfile.getPath() + "\n");
            dataOutputStream.writeBytes("pm install -r " + apkfile.getPath() + "\n");
            dataOutputStream.writeBytes("sync" + "\n");
            // 提交命令
            dataOutputStream.flush();
            // 关闭流操作
            dataOutputStream.close();
            out.close();
            Log.d(TAG, "Install app success:" + apkfile.getPath());
        } catch (IOException e) {
            e.printStackTrace();
            Log.d(TAG, "Install app error:" + e.getMessage());
            return DWErrorCode.DW_INSTALL_APP_ERROR;
        } finally {
            if (dataOutputStream != null) {
                try {
                    dataOutputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (out != null) {
                try {
                    out.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        try {
            if (process != null)
                process.waitFor();
        } catch (InterruptedException e) {
            e.printStackTrace();
            return DWErrorCode.DW_INSTALL_APP_ERROR;
        }
        return DWErrorCode.DW_COMMON_SUCCESS;
    }

    public void uninstallAPK(Context context, String packageName) {
        Uri uri = Uri.parse("package:" + packageName);
        Intent intent = new Intent(Intent.ACTION_DELETE, uri);
        context.startActivity(intent);
    }
}
