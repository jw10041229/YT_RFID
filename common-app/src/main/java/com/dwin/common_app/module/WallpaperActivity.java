package com.dwin.common_app.module;

import android.app.WallpaperManager;
import android.content.ContentResolver;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;

import com.dwin.common_app.R;
import com.dwin.common_app.base.BaseFuncActivity;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

public class WallpaperActivity extends BaseFuncActivity implements View.OnClickListener {
    private static final String TAG = "SetWallpaper";
    private static final int PICK_IMAGE_REQUEST = 1;  // 图片选择请求码

    private TextView tvSetWallpaper, tvResetWallpaper;
    private TextView tv_toolbar;
    private ImageView iv_back;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wallpaper);
        tv_toolbar = findViewById(R.id.tv_toolbar);
        iv_back = findViewById(R.id.iv_back);
        tvSetWallpaper = findViewById(R.id.tv_set_wallpaper);
        tvResetWallpaper = findViewById(R.id.tv_reset_wallpaper);
        tv_toolbar.setText(getResources().getString(R.string.system_wallpaper));
        tvSetWallpaper.setOnClickListener(this);
        tvResetWallpaper.setOnClickListener(this);
        iv_back.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.tv_set_wallpaper) {
            openImagePicker();
        } else if (id == R.id.tv_reset_wallpaper) {
            showResetWallpaperDialog();
        } else if (id == R.id.iv_back) {
            finish();
        }
    }

    // 显示重置壁纸的确认对话框
    private void showResetWallpaperDialog() {
        new AlertDialog.Builder(this)
                .setTitle(getString(R.string.reset_system_wallpaper_confirm))
                .setNegativeButton(getString(R.string.cancel), null)
                .setPositiveButton(getString(R.string.sure), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        resetWallpaper();
                    }
                })
                .show();
    }

    // 执行重置壁纸操作
    private void resetWallpaper() {
        try {
            WallpaperManager wallpaperManager = WallpaperManager.getInstance(getApplicationContext());
            // 重置为默认壁纸或清空当前壁纸（根据设备支持的 API）
            wallpaperManager.clear();  // 注意：部分设备上可能不支持 clear() 方法
            Toast.makeText(this, getString(R.string.wallpaper_reset_success), Toast.LENGTH_SHORT).show();
        } catch (IOException e) {
            Log.e(TAG, "重置壁纸时发生异常: " + e.getMessage(), e);
            Toast.makeText(this, getString(R.string.wallpaper_reset_failed), Toast.LENGTH_SHORT).show();
        }
    }

    // 打开图片选择器
    private void openImagePicker() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        intent.setType("image/*");
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null) {
            Uri imageUri = data.getData();
            if (imageUri != null) {
                setWallpaperFromUri(imageUri);
            }
        }
    }

    // 设置系统壁纸
    private void setWallpaperFromUri(Uri imageUri) {
        try {
            // 获取壁纸管理器实例
            WallpaperManager wallpaperManager = WallpaperManager.getInstance(getApplicationContext());
            Log.d(TAG, "壁纸管理器实例获取成功");

            // 使用 ContentResolver 获取图片的 InputStream
            ContentResolver contentResolver = getContentResolver();
            InputStream inputStream = contentResolver.openInputStream(imageUri);

            if (inputStream == null) {
                Log.e(TAG, "无法从 URI 获取 InputStream");
                Toast.makeText(this, getString(R.string.wallpaper_get_failed), Toast.LENGTH_SHORT).show();
                return;
            }

            // 使用 Bitmap 对象作为壁纸
            Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
            if (bitmap == null) {
                Log.e(TAG, "壁纸图片解码失败，bitmap 为 null");
                Toast.makeText(this, getString(R.string.wallpaper_decode_failed), Toast.LENGTH_SHORT).show();
                return;
            }

            Log.d(TAG, "壁纸图片解码成功");

            // 设置壁纸
            wallpaperManager.setBitmap(bitmap);
            Log.d(TAG, "壁纸设置成功");

            // 设置成功的提示
            Toast.makeText(this, getString(R.string.wallpaper_set_success), Toast.LENGTH_SHORT).show();

        } catch (FileNotFoundException e) {
            Log.e(TAG, "无法打开图片文件: " + e.getMessage(), e);
            Toast.makeText(this, getString(R.string.wallpaper_open_failed), Toast.LENGTH_SHORT).show();
        } catch (IOException e) {
            Log.e(TAG, "设置壁纸时发生异常: " + e.getMessage(), e);
            Toast.makeText(this, getString(R.string.wallpaper_set_failed), Toast.LENGTH_SHORT).show();
        } catch (OutOfMemoryError e) {
            Log.e(TAG, "内存不足，无法设置壁纸", e);
            Toast.makeText(this, getString(R.string.wallpaper_insufficient_memory), Toast.LENGTH_SHORT).show();
        }
    }
}
