package com.dwin.common_app.module;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.dwin.common_app.R;
import com.dwin.common_app.base.BaseFuncV2Activity;
import com.dwin.common_app.util.ToastUtils;
import com.dwin.dw_android_11_sdk.DWErrorCode;

import java.io.File;

public class InstallActivity extends BaseFuncV2Activity implements View.OnClickListener {
    private static final String TAG = "AutoStartActivity";

    private ProgressDialog progressDialog;

    private TextView tv_toolbar, tvSelectAppPath, tvInstallApp;
    private ImageView iv_back, ivSelectApp, ivSdStorage, ivUStorage;

    @Override
    protected void updateStorageState(int sdNum, int uNum) {
        if (ivSdStorage != null)
            ivSdStorage.setVisibility(sdNum == 0 ? View.GONE : View.VISIBLE);
        if (ivUStorage != null)
            ivUStorage.setVisibility(uNum == 0 ? View.GONE : View.VISIBLE);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_install);
        tv_toolbar = findViewById(R.id.tv_toolbar);
        iv_back = findViewById(R.id.iv_back);
        tv_toolbar.setText(getResources().getString(R.string.silent_installation));

        tvSelectAppPath = findViewById(R.id.tv_select_app_path);
        ivSelectApp = findViewById(R.id.iv_select_app);
        tvInstallApp = findViewById(R.id.tv_install_app);

        ivSdStorage = findViewById(R.id.iv_sd_storage);
        ivUStorage = findViewById(R.id.iv_u_storage);

        initListener();
        checkExternalSorageDevices();
    }

    private void initListener() {
        iv_back.setOnClickListener(this);

        ivSelectApp.setOnClickListener(this);
        tvSelectAppPath.setOnClickListener(this);
        tvInstallApp.setOnClickListener(this);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Uri uri = (data == null ? null : data.getData());
        if (requestCode == 1000 && resultCode == Activity.RESULT_OK) {
            if (uri != null) {
                String path = getRealPathFromUri(InstallActivity.this, uri);
                Log.d("daibin", "apkPath=" + path);
                if (path != null) {
                    tvSelectAppPath.setText(path);
                }
            }
        }
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.iv_back) {
            finish();
        } else if (v.getId() == R.id.tv_install_app) {
            String apkPath = tvSelectAppPath.getText().toString().trim();
            if (TextUtils.isEmpty(apkPath)) {
                ToastUtils.showShort(InstallActivity.this, getResources().getString(R.string.apk_path_empty));
                return;
            }
            File file = new File(apkPath);
            if (!file.exists()) {
                ToastUtils.showShort(InstallActivity.this, getResources().getString(R.string.apk_path_not_exist));
                return;
            }
            if (!apkPath.endsWith(".apk")) {
                ToastUtils.showShort(InstallActivity.this, getResources().getString(R.string.apk_file_incorrect));
                return;
            }
            showSetFuncTips(getResources().getString(R.string.confirm_install), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    showLoadingDialog();
                    new Thread(() -> {
                        int i = mDWAndroidApi.installApp(apkPath, false);
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                if (i == DWErrorCode.DW_COMMON_SUCCESS) {
                                    ToastUtils.showShort(InstallActivity.this, getResources().getString(R.string.install_success));
                                } else {
                                    ToastUtils.showShort(InstallActivity.this, getResources().getString(R.string.install_failed) + i);
                                }
                                hideLoadingDialog();
                            }
                        });
                    }).start();
                }
            });
        } else if (v.getId() == R.id.iv_select_app) {
            openDocumentByUri(URI_PREFIX + STORAGE_NAME, 1000);
        }
    }

    private void showLoadingDialog() {
        hideLoadingDialog();
        progressDialog = new ProgressDialog(InstallActivity.this); // 创建进度对话框
        progressDialog.setMessage(getResources().getString(R.string.installing)); // 设置对话框信息
        progressDialog.setCancelable(false); // 设置不可取消
        progressDialog.show(); // 显示对话框
    }

    private void hideLoadingDialog() {
        if (progressDialog != null) {
            progressDialog.dismiss();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}



