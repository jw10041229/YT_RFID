package com.dwin.common_app.module;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.dwin.common_app.R;
import com.dwin.common_app.base.BaseFuncV2Activity;
import com.dwin.common_app.util.SharedPreferencesUtils;
import com.dwin.common_app.util.ToastUtils;
import com.dwin.dw_android_11_sdk.DWErrorCode;

import java.io.File;

public class BootAnimationActivity extends BaseFuncV2Activity implements View.OnClickListener {
    private static final String TAG = "BootAnimationActivity";

    private static final String BOOT_ANIM_DEFAULT_PATH = "/sdcard/bootanimation.zip";
    private static final String BOOT_VIDEO_DEFAULT_PATH = "/sdcard/bootanimation.ts";
    private static final String BOOT_LOGO_DEFAULT_PATH = "/sdcard/logo.bmp";

    private RelativeLayout layoutSetBootLogo, layoutSetBootVideo, layoutSetBootAnim;
    private TextView tvBootAnimSet, tvBootAnimReset, tvBootAnimPath, tvBootVideoSet, tvBootVideoReset, tvBootVideoPath,
            tvBootLogoSet, tvBootLogoReset, tvBootLogoPath, tvResetSystem, tv_toolbar;
    private ImageView iv_back, ivSelectBootanim, ivSelectBootVideo, ivSelectLogo, ivSdStorage, ivUStorage;

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
        setContentView(R.layout.activity_boot_animation);

        tv_toolbar = findViewById(R.id.tv_toolbar);

        layoutSetBootAnim = findViewById(R.id.layout_set_bootanim);
        layoutSetBootVideo = findViewById(R.id.layout_set_bootvideo);
        layoutSetBootLogo = findViewById(R.id.layout_set_bootlogo);

        tvBootAnimSet = findViewById(R.id.tv_set_bootanim);
        tvBootAnimReset = findViewById(R.id.tv_reset_bootanim);
        tvBootAnimPath = findViewById(R.id.tv_bootanim_path);
        ivSelectBootanim = findViewById(R.id.iv_select_bootanim);

        tvBootVideoSet = findViewById(R.id.tv_set_bootvideo);
        tvBootVideoReset = findViewById(R.id.tv_reset_bootvideo);
        tvBootVideoPath = findViewById(R.id.tv_bootvideo_path);
        ivSelectBootVideo = findViewById(R.id.iv_select_bootvideo);

        tvBootLogoSet = findViewById(R.id.tv_set_bootlogo);
        tvBootLogoReset = findViewById(R.id.tv_reset_bootlogo);
        tvBootLogoPath = findViewById(R.id.tv_bootlogo_path);
        ivSelectLogo = findViewById(R.id.iv_select_logo);

        ivSdStorage = findViewById(R.id.iv_sd_storage);
        ivUStorage = findViewById(R.id.iv_u_storage);

        tvResetSystem = findViewById(R.id.tv_reset_system);

        iv_back = findViewById(R.id.iv_back);
        tv_toolbar.setText(getResources().getString(R.string.boot_animation));

        initListener();
        initData();
        checkExternalSorageDevices();
    }

    private void initData() {
        String bootAnimation = (String) SharedPreferencesUtils.getParam(BootAnimationActivity.this, "boot_animation", BOOT_ANIM_DEFAULT_PATH);
        String bootVideo = (String) SharedPreferencesUtils.getParam(BootAnimationActivity.this, "boot_video", BOOT_VIDEO_DEFAULT_PATH);
        String bootLogo = (String) SharedPreferencesUtils.getParam(BootAnimationActivity.this, "boot_logo", BOOT_LOGO_DEFAULT_PATH);

        tvBootAnimPath.setText(bootAnimation);
        tvBootVideoPath.setText(bootVideo);
        tvBootLogoPath.setText(bootLogo);
    }

    private void initListener() {
        layoutSetBootAnim.setOnClickListener(this);
        tvBootAnimReset.setOnClickListener(this);
        ivSelectBootanim.setOnClickListener(this);
        layoutSetBootVideo.setOnClickListener(this);
        tvBootVideoReset.setOnClickListener(this);
        ivSelectBootVideo.setOnClickListener(this);
        layoutSetBootLogo.setOnClickListener(this);
        tvBootLogoReset.setOnClickListener(this);
        ivSelectLogo.setOnClickListener(this);
        tvResetSystem.setOnClickListener(this);

        iv_back.setOnClickListener(this);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Uri uri = (data == null ? null : data.getData());
        if (requestCode == 1000 && resultCode == Activity.RESULT_OK) {
            if (uri != null) {
                String path = getRealPathFromUri(BootAnimationActivity.this, uri);
                Log.d("daibin", "bootanimPath=" + path);
                if (path != null) {
                    tvBootAnimPath.setText(path);
                }
            }
        } else if (requestCode == 1001 && resultCode == Activity.RESULT_OK) {
            if (uri != null) {
                String path = getRealPathFromUri(BootAnimationActivity.this, uri);
                Log.d("daibin", "bootVideoPath=" + path);
                if (path != null) {
                    tvBootVideoPath.setText(path);
                }
            }
        } else if (requestCode == 1002 && resultCode == Activity.RESULT_OK) {
            if (uri != null) {
                String path = getRealPathFromUri(BootAnimationActivity.this, uri);
                Log.d("daibin", "bootLogoPath=" + path);
                if (path != null) {
                    tvBootLogoPath.setText(path);
                }
            }
        }
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.iv_back) {
            finish();
        } else if (id == R.id.layout_set_bootanim) {
            String animPath = tvBootAnimPath.getText().toString().trim();
            if (TextUtils.isEmpty(animPath)) {
                ToastUtils.showShort(BootAnimationActivity.this, getResources().getString(R.string.boot_animation_path_empty));
                return;
            }
            File file = new File(animPath);
            if (!file.exists()) {
                ToastUtils.showShort(BootAnimationActivity.this, getResources().getString(R.string.boot_animation_path_not_exist));
                return;
            }
            if (!animPath.endsWith("bootanimation.zip")) {
                ToastUtils.showShort(BootAnimationActivity.this, getResources().getString(R.string.boot_animation_format_incorrect));
                return;
            }
            showSetFuncTips(getResources().getString(R.string.boot_animation_set_confirm), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    int i = mDWAndroidApi.setBootAnimation(animPath);
                    if (i == DWErrorCode.DW_COMMON_SUCCESS) {
                        SharedPreferencesUtils.setParam(BootAnimationActivity.this, "boot_animation", animPath);
                        syncSystem();
                        ToastUtils.showShort(BootAnimationActivity.this, getResources().getString(R.string.boot_animation_set_success));
                    } else {
                        ToastUtils.showShort(BootAnimationActivity.this, getResources().getString(R.string.boot_animation_set_failed) + i);
                    }
                }
            });
        } else if (id == R.id.tv_reset_bootanim) {
            showSetFuncTips(getResources().getString(R.string.boot_animation_reset_confirm), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    int i = mDWAndroidApi.deleteBootAnimation();
                    if (i == DWErrorCode.DW_COMMON_SUCCESS) {
                        ToastUtils.showShort(BootAnimationActivity.this, getResources().getString(R.string.boot_animation_reset_success));
                    } else {
                        ToastUtils.showShort(BootAnimationActivity.this, getResources().getString(R.string.boot_animation_reset_failed) + i);
                    }
                }
            });
        } else if (id == R.id.iv_select_bootanim) {
            openDocumentByUri(URI_PREFIX + STORAGE_NAME, 1000);
        } else if (id == R.id.layout_set_bootvideo) {
            String videoPath = tvBootVideoPath.getText().toString().trim();
            if (TextUtils.isEmpty(videoPath)) {
                ToastUtils.showShort(BootAnimationActivity.this, getResources().getString(R.string.boot_video_path_empty));
                return;
            }
            File file = new File(videoPath);
            if (!file.exists()) {
                ToastUtils.showShort(BootAnimationActivity.this, getResources().getString(R.string.boot_video_path_not_exist));
                return;
            }
            if (!videoPath.endsWith("bootanimation.ts")) {
                ToastUtils.showShort(BootAnimationActivity.this, getResources().getString(R.string.boot_video_format_incorrect));
                return;
            }
            showSetFuncTips(getResources().getString(R.string.boot_video_set_confirm), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    int i = mDWAndroidApi.setBootVideo(videoPath);
                    if (i == DWErrorCode.DW_COMMON_SUCCESS) {
                        SharedPreferencesUtils.setParam(BootAnimationActivity.this, "boot_video", videoPath);
                        syncSystem();
                        ToastUtils.showShort(BootAnimationActivity.this, getResources().getString(R.string.boot_video_set_success));
                    } else {
                        ToastUtils.showShort(BootAnimationActivity.this, getResources().getString(R.string.boot_video_set_failed) + i);
                    }
                }
            });
        } else if (id == R.id.tv_reset_bootvideo) {
            showSetFuncTips(getResources().getString(R.string.boot_video_reset_confirm), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    int i = mDWAndroidApi.deleteBootVideo();
                    if (i == DWErrorCode.DW_COMMON_SUCCESS) {
                        ToastUtils.showShort(BootAnimationActivity.this, getResources().getString(R.string.boot_video_reset_success));
                    } else {
                        ToastUtils.showShort(BootAnimationActivity.this, getResources().getString(R.string.boot_video_reset_failed) + i);
                    }
                }
            });
        } else if (id == R.id.iv_select_bootvideo) {
            openDocumentByUri(URI_PREFIX + STORAGE_NAME, 1001);
        } else if (id == R.id.layout_set_bootlogo) {
            String logoPath = tvBootLogoPath.getText().toString().trim();
            if (TextUtils.isEmpty(logoPath)) {
                ToastUtils.showShort(BootAnimationActivity.this, getResources().getString(R.string.boot_logo_path_empty));
                return;
            }
            File file = new File(logoPath);
            if (!file.exists()) {
                ToastUtils.showShort(BootAnimationActivity.this, getResources().getString(R.string.boot_logo_path_not_exist));
                return;
            }
            if (!logoPath.endsWith("logo.bmp")) {
                ToastUtils.showShort(BootAnimationActivity.this, getResources().getString(R.string.boot_logo_format_incorrect));
                return;
            }
            showSetFuncTips(getResources().getString(R.string.boot_logo_set_confirm), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    int i = mDWAndroidApi.setBootLogo(logoPath);
                    if (i == DWErrorCode.DW_COMMON_SUCCESS) {
                        SharedPreferencesUtils.setParam(BootAnimationActivity.this, "boot_logo", logoPath);
                        syncSystem();
                        ToastUtils.showShort(BootAnimationActivity.this, getResources().getString(R.string.boot_logo_set_success));
                    } else {
                        ToastUtils.showShort(BootAnimationActivity.this, getResources().getString(R.string.boot_logo_set_failed) + i);
                    }
                }
            });
        } else if (id == R.id.tv_reset_bootlogo) {
            showSetFuncTips(getResources().getString(R.string.boot_logo_reset_confirm), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    int i = mDWAndroidApi.deleteBootLogo();
                    if (i == DWErrorCode.DW_COMMON_SUCCESS) {
                        ToastUtils.showShort(BootAnimationActivity.this, getResources().getString(R.string.boot_logo_reset_success));
                    } else {
                        ToastUtils.showShort(BootAnimationActivity.this, getResources().getString(R.string.boot_logo_reset_failed) + i);
                    }
                }
            });
        } else if (id == R.id.iv_select_logo) {
            openDocumentByUri(URI_PREFIX + STORAGE_NAME, 1002);
        } else if (id == R.id.tv_reset_system) {
            showRebootSystemTips();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}
