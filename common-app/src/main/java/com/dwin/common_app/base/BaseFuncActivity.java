package com.dwin.common_app.base;

import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.dwin.common_app.R;
import com.dwin.dw_android_11_sdk.DWAndroidApi;

import java.io.IOException;
import java.io.OutputStreamWriter;

public class BaseFuncActivity extends AppCompatActivity {

    protected DWAndroidApi mDWAndroidApi;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mDWAndroidApi = new DWAndroidApi(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mDWAndroidApi != null) {
            mDWAndroidApi.release();
        }
    }


    protected void showSetFuncTips(String title, DialogInterface.OnClickListener onConfirmClickListener) {
        new AlertDialog.Builder(BaseFuncActivity.this)
                .setTitle(title)
                .setNegativeButton(getResources().getString(R.string.cancel), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                })
                .setPositiveButton(getResources().getString(R.string.sure), onConfirmClickListener)
                .show();
    }

    protected void showRebootSystemTips() {
        new AlertDialog.Builder(BaseFuncActivity.this)
                .setTitle(getResources().getString(R.string.reboot_system_confirm))
                .setNegativeButton(getResources().getString(R.string.cancel), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                })
                .setPositiveButton(getResources().getString(R.string.reboot), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        mDWAndroidApi.rebootSystem();
                    }
                })
                .show();
    }

    /**
     * 同步系统，防止保存数据后掉电丢失
     */
    protected void syncSystem() {
        Runtime runtime = Runtime.getRuntime();
        java.lang.Process proc = null;
        OutputStreamWriter osw = null;

        try { // Run Script
            proc = runtime.exec("su");
            osw = new OutputStreamWriter(proc.getOutputStream());
            osw.write("sync" + "\n");
            osw.flush();
            osw.close();
            Log.d("daibin", "syncSystem success");
        } catch (IOException ex) {
            ex.printStackTrace();
            Log.d("daibin", "syncSystem error:" + ex.getMessage());
        } finally {
            if (osw != null) {
                try {
                    osw.close();
                } catch (IOException e) {
                    e.printStackTrace();
                    Log.d("daibin", "syncSystem error:" + e.getMessage());
                }
            }
        }
        try {
            if (proc != null)
                proc.waitFor();
        } catch (InterruptedException e) {
            e.printStackTrace();
            Log.d("daibin", "syncSystem error:" + e.getMessage());
        }
    }
}
