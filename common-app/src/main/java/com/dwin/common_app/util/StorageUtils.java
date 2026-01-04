package com.dwin.common_app.util;

import android.content.Context;
import android.os.storage.StorageManager;
import android.util.Log;

import com.dwin.common_app.bean.StorageBean;

import java.io.File;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

public class StorageUtils {
    public static ArrayList<StorageBean> readExternalStoragePath(Context context) {
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

                    Log.d("daibin", "sd:" + sd
                            + "  ,usb:" + usb
                            + "  ,path:" + file.getAbsolutePath());
                    StorageBean bean = new StorageBean();
                    bean.setSdcard(sd);
                    bean.setUSB(usb);
                    bean.setPath(file.getAbsolutePath());
                    mLists.add(bean);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            Log.d("daibin", "getStoragePath error:" + e.getMessage());
            return null;
        }
        return mLists;
    }
}
