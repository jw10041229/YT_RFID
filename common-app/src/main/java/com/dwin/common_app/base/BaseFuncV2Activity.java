package com.dwin.common_app.base;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;

import androidx.annotation.Nullable;

import com.dwin.common_app.bean.StorageBean;
import com.dwin.common_app.util.StorageUtils;

import java.io.File;
import java.util.ArrayList;

public abstract class BaseFuncV2Activity extends BaseFuncActivity {
    private static final String TAG = "BaseFuncV2Activity";

    protected final Handler mH = new Handler();

    protected static final String URI_PREFIX = "content://com.android.externalstorage.documents/document";
    protected String STORAGE_NAME = "sdcard:";

    protected abstract void updateStorageState(int sdNum, int uNum);

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        registerUSBBroadcast();
    }

    private void registerUSBBroadcast() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_MEDIA_MOUNTED);
        filter.addAction(Intent.ACTION_MEDIA_REMOVED);
        filter.addAction(Intent.ACTION_MEDIA_UNMOUNTED);
        filter.addAction(Intent.ACTION_MEDIA_EJECT);
        filter.addDataScheme("file");
        registerReceiver(usbReceiver, filter);
    }

    private void unRegisterUSBBroadcast() {
        if (usbReceiver != null) {
            unregisterReceiver(usbReceiver);
        }
    }

    private final BroadcastReceiver usbReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d(TAG, "intent.getAction():" + intent.getAction());
            if (intent.getAction().equals(Intent.ACTION_MEDIA_MOUNTED)) {
                //接收到U盘设备插入广播
                Log.d(TAG, "接收到U盘设备插入");
                mH.removeCallbacks(delayCheckStorageDevices);
                mH.postDelayed(delayCheckStorageDevices, 1000);
            } else if (intent.getAction().equals(Intent.ACTION_MEDIA_UNMOUNTED) || intent.getAction().equals(Intent.ACTION_MEDIA_EJECT)) {
                //接收到U盘设备拔出广播
                Log.d(TAG, "接收到U盘设备拔出");
                mH.removeCallbacks(delayCheckStorageDevices);
                mH.postDelayed(delayCheckStorageDevices, 1000);
            }
        }
    };

    private final Runnable delayCheckStorageDevices = new Runnable() {
        @Override
        public void run() {
            checkExternalSorageDevices();
        }
    };

    protected synchronized void checkExternalSorageDevices() {
        ArrayList<StorageBean> storageBeans = StorageUtils.readExternalStoragePath(BaseFuncV2Activity.this);
        if (storageBeans != null) {
            int sdNum = 0;
            int uNum = 0;
            String sdPath = "";
            String uPath = "";
            for (int i = 0; i < storageBeans.size(); i++) {
                StorageBean storageBean = storageBeans.get(i);
                String path = storageBean.getPath();
                if (TextUtils.isEmpty(path)) {
                    continue;
                }
                if (storageBean.isUSB()) {
                    uNum++;
                    uPath = path;
                } else if (storageBean.isSdcard()) {
                    sdNum++;
                    sdPath = path;
                }
            }
            if (!TextUtils.isEmpty(uPath)) {
                if (uPath.contains("/storage")) {
                    uPath = uPath.replace("/storage", "") + ":";
                } else if (uPath.contains("storage")) {
                    uPath = uPath.replace("storage", "") + ":";
                }
                STORAGE_NAME = uPath;
            } else if (!TextUtils.isEmpty(sdPath)) {
                if (sdPath.contains("/storage")) {
                    sdPath = sdPath.replace("/storage", "") + ":";
                } else if (sdPath.contains("storage")) {
                    sdPath = sdPath.replace("storage", "") + ":";
                }
                STORAGE_NAME = sdPath;
            }
            Log.d("daibin", "STORAGE_NAME:" + STORAGE_NAME);
            updateStorageState(sdNum, uNum);
        }
    }

    protected void openDocumentByUri(String uriString, int requestCode) {
        if (TextUtils.isEmpty(uriString)) return;
        Uri uri = Uri.parse(uriString);
        // 本地存储
        //Uri uri = Uri.parse("content://com.android.externalstorage.documents/document/primary:Download");
        //Uri uri = Uri.parse("content://com.android.externalstorage.documents/document/4249-8CFF:");
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("*/*");
        intent.putExtra(DocumentsContract.EXTRA_INITIAL_URI, uri);
        startActivityForResult(intent, requestCode);
    }

    protected String getRealPathFromUri(Context context, Uri uri) {
        if (uri == null) return null;

        String authority = uri.getAuthority();
        if (authority == null) return null;

        if (authority.equals("com.android.externalstorage.documents")) {
            return getPathFromExternalStorage(context, uri);
        } else if (authority.equals("com.android.providers.media.documents")) {
            return getPathFromMediaStore(context, uri);
        } else if ("content".equalsIgnoreCase(uri.getScheme())) {
            // 通用 ContentResolver 查询（适用于部分 URI）
            try (Cursor cursor = context.getContentResolver().query(uri,
                    new String[]{MediaStore.MediaColumns.DATA}, null, null, null)) {
                if (cursor != null && cursor.moveToFirst()) {
                    return cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DATA));
                }
            }
        } else if ("file".equalsIgnoreCase(uri.getScheme())) {
            return uri.getPath();
        }
        return null;
    }

    private String getPathFromMediaStore(Context context, Uri uri) {
        String docId = DocumentsContract.getDocumentId(uri);
        String[] split = docId.split(":");
        String type = split[0], id = split[1];

        Uri contentUri = null;
        switch (type) {
            case "image":
                contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                break;
            case "video":
                contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
                break;
            case "audio":
                contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
                break;
        }
        if (contentUri == null) return null;
        try (Cursor cursor = context.getContentResolver().query(
                contentUri,
                new String[]{MediaStore.MediaColumns.DATA},
                MediaStore.MediaColumns._ID + "=?",
                new String[]{id},
                null)) {
            if (cursor != null && cursor.moveToFirst()) {
                return cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DATA));
            }
        }
        return null;
    }

    private String getPathFromExternalStorage(Context context, Uri uri) {
        String docId = DocumentsContract.getDocumentId(uri);
        String[] split = docId.split(":");
        if (split.length < 2) return null;
        String type = split[0], path = split[1];
        if ("primary".equalsIgnoreCase(type)) {
            return Environment.getExternalStorageDirectory() + "/" + path;
        } else {
            // 处理 SD 卡或其他存储卷（需适配 Android 5.0+）
            File[] externalDirs = context.getExternalFilesDirs(null);
            for (File file : externalDirs) {
                if (file != null && file.getAbsolutePath().contains(type)) {
//                    String absolutePath = file.getAbsolutePath();
//                    if (absolutePath.startsWith("/")) {
//                        absolutePath = absolutePath.replaceFirst("/", "");
//                    }
//                    String[] s = absolutePath.split("/");
//                    Log.d("daibin", "getPathFromExternalStorage  =" + Arrays.toString(s));
//                    return "/" + s[0] + "/" + s[1] + "/" + path;
                    String[] s = file.getAbsolutePath().split(type);
                    return s[0] + type + "/" + path;
                }
            }
        }
        return null;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unRegisterUSBBroadcast();
    }
}
