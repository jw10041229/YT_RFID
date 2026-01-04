package com.dwin.common_app.module;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.appcompat.widget.SwitchCompat;
import androidx.core.content.res.ResourcesCompat;

import com.dwin.common_app.R;
import com.dwin.common_app.base.BaseFuncV2Activity;
import com.dwin.common_app.util.ToastUtils;
import com.dwin.dw_android_11_sdk.DWErrorCode;

import java.io.File;

public class LogActivity extends BaseFuncV2Activity implements View.OnClickListener {

    private static final String TAG = "LogActivity";

    private SwitchCompat swLogSave;
    private TextView tv_toolbar, tvExportLog, tvClearLog;
    private ImageView iv_back, ivSdStorage, ivUStorage;
    private RelativeLayout layoutStartLogcat;

    @Override
    protected void updateStorageState(int sdNum, int uNum) {
        if (ivSdStorage != null)
            ivSdStorage.setVisibility(sdNum == 0 ? View.GONE : View.VISIBLE);
        if (ivUStorage != null)
            ivUStorage.setVisibility(uNum == 0 ? View.GONE : View.VISIBLE);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_log);

        swLogSave = findViewById(R.id.sw_log_save);
        layoutStartLogcat = findViewById(R.id.layout_start_logcat);
        tvExportLog = findViewById(R.id.tv_export_log);
        tvClearLog = findViewById(R.id.tv_clear_log);
        tv_toolbar = findViewById(R.id.tv_toolbar);
        iv_back = findViewById(R.id.iv_back);
        tv_toolbar.setText(getResources().getString(R.string.logcat));

        ivSdStorage = findViewById(R.id.iv_sd_storage);
        ivUStorage = findViewById(R.id.iv_u_storage);

        swLogSave.setChecked(mDWAndroidApi.isLogSaving());
        updateClearLogUI(swLogSave.isChecked());

        initEvent();
        checkExternalSorageDevices();
    }

    private void initEvent() {
        iv_back.setOnClickListener(this);
        layoutStartLogcat.setOnClickListener(this);
        tvExportLog.setOnClickListener(this);
        tvClearLog.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.iv_back) {
            finish();
        } else if (id == R.id.layout_start_logcat) {
            swLogSave.setChecked(!swLogSave.isChecked());
            updateClearLogUI(swLogSave.isChecked());
            if (swLogSave.isChecked()) {
                mDWAndroidApi.startLogSave(1024, 10);
            } else {
                mDWAndroidApi.stopLogSave();
            }
        } else if (id == R.id.tv_export_log) {
            if (TextUtils.isEmpty(STORAGE_NAME)) {
                ToastUtils.showShort(LogActivity.this, getResources().getString(R.string.log_export_path_empty));
                return;
            }

            String path = "/storage" + STORAGE_NAME.replace(":", "");
            File file = new File(path);
            if (!file.isDirectory()) {
                ToastUtils.showShort(LogActivity.this, getResources().getString(R.string.log_export_path_empty));
                return;
            }
            showSetFuncTips(getResources().getString(R.string.log_export_confirm), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    showLoadingDialog(getResources().getString(R.string.exporting));
                    new Thread(() -> {
                        int i = mDWAndroidApi.copyLogToStorage(path);
                        try {
                            Thread.sleep(500);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                if (i == DWErrorCode.DW_COMMON_SUCCESS) {
                                    ToastUtils.showShort(LogActivity.this, getResources().getString(R.string.log_export_success));
                                } else {
                                    ToastUtils.showShort(LogActivity.this, getResources().getString(R.string.log_export_failed) + i);
                                }
                                hideLoadingDialog();
                            }
                        });
                    }).start();
                }
            });
        } else if (id == R.id.tv_clear_log) {
            showSetFuncTips(getResources().getString(R.string.clear_log_confirm), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    showLoadingDialog(getResources().getString(R.string.clearing));
                    new Thread(() -> {
                        int i = mDWAndroidApi.clearLog();
                        try {
                            Thread.sleep(500);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                if (i == DWErrorCode.DW_COMMON_SUCCESS) {
                                    ToastUtils.showShort(LogActivity.this, getResources().getString(R.string.clear_log_success));
                                } else {
                                    ToastUtils.showShort(LogActivity.this, getResources().getString(R.string.clear_log_failed) + i);
                                }
                                hideLoadingDialog();
                            }
                        });
                    }).start();
                }
            });
        }
    }

    private void updateClearLogUI(boolean isOpen) {
        if (isOpen) {
            tvClearLog.setEnabled(false);
            tvClearLog.setBackground(ResourcesCompat.getDrawable(getResources(), R.drawable.background_disable_border_bottom, null));
        } else {
            tvClearLog.setEnabled(true);
            tvClearLog.setBackground(ResourcesCompat.getDrawable(getResources(), R.drawable.background_border_bottom, null));
        }
    }

//    private void copyLogToStorage(String path) {
//        if (TextUtils.isEmpty(path)) {
//            Log.d(TAG, "copyLogToStorage error: Storage path is null");
//            return;
//        }
//        Runtime runtime = Runtime.getRuntime();
//        java.lang.Process proc = null;
//        OutputStreamWriter osw = null;
//
//        try { // Run Script
//            proc = runtime.exec("su");
//            osw = new OutputStreamWriter(proc.getOutputStream());
//            osw.write("mount -o remount,rw /data/misc/logd" + "\n");
//            osw.write("cp -rf /data/misc/logd " + path + "\n");
//            osw.write("sync" + "\n");
//            osw.flush();
//            osw.close();
//            Log.d(TAG, "copyLogToStorage success");
//        } catch (IOException ex) {
//            ex.printStackTrace();
//            Log.d(TAG, "copyLogToStorage error:" + ex.getMessage());
//        } finally {
//            if (osw != null) {
//                try {
//                    osw.close();
//                } catch (IOException e) {
//                    e.printStackTrace();
//                    Log.d(TAG, "copyLogToStorage error:" + e.getMessage());
//                }
//            }
//        }
//        try {
//            if (proc != null) {
//                proc.waitFor();
//            }
//        } catch (Exception e) {
//            e.printStackTrace();
//            Log.d(TAG, "copyLogToStorage error:" + e.getMessage());
//        }
//    }

    private ProgressDialog progressDialog;

    private void showLoadingDialog(String content) {
        hideLoadingDialog();
        progressDialog = new ProgressDialog(LogActivity.this); // 创建进度对话框
        progressDialog.setMessage(content); // 设置对话框信息
        progressDialog.setCancelable(false); // 设置不可取消
        progressDialog.show(); // 显示对话框
    }

    private void hideLoadingDialog() {
        if (progressDialog != null) {
            progressDialog.dismiss();
        }
    }
}