package com.dwin.common_app.module;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.appcompat.widget.SwitchCompat;

import com.dwin.common_app.R;
import com.dwin.common_app.base.BaseFuncActivity;

import java.io.IOException;
import java.io.OutputStreamWriter;

public class ResetFactoryActivity extends BaseFuncActivity implements View.OnClickListener {

    private static final String TAG = "ResetFactoryActivity";

    private SwitchCompat resetFactoryStatus;
    private TextView tv_toolbar;
    private ImageView iv_back;
    private RelativeLayout layoutResetFactory;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reset_factory);

        resetFactoryStatus = findViewById(R.id.reset_factory_status);
        layoutResetFactory = findViewById(R.id.layout_reset_factory);
        tv_toolbar = findViewById(R.id.tv_toolbar);
        iv_back = findViewById(R.id.iv_back);
        tv_toolbar.setText(getResources().getString(R.string.reset_factory));

        initEvent();
        resetFactoryStatus.setChecked(false);
    }

    private void initEvent() {
        iv_back.setOnClickListener(this);
        layoutResetFactory.setOnClickListener(this);

    }

    private void resetFactory() {
        Runtime runtime = Runtime.getRuntime();
        Process proc = null;
        OutputStreamWriter osw = null;

        try {
            // 获取 root 权限
            proc = runtime.exec("su");
            osw = new OutputStreamWriter(proc.getOutputStream());

            // 发送 MASTER_CLEAR 广播命令
            osw.write("am broadcast -a android.intent.action.MASTER_CLEAR -n android/com.android.server.MasterClearReceiver\n");
            osw.write("sync\n");  // 同步操作
            Thread.sleep(1000);   // 可以根据需要调整延时
            osw.flush();
            osw.close();
        } catch (IOException | InterruptedException ex) {
            ex.printStackTrace();
            Log.d(TAG, "resetFactory error: " + ex.getMessage());
        } finally {
            if (osw != null) {
                try {
                    osw.close();
                } catch (IOException e) {
                    e.printStackTrace();
                    Log.d(TAG, "Error closing OutputStreamWriter: " + e.getMessage());
                }
            }
        }
        try {
            if (proc != null)
                proc.waitFor();
        } catch (InterruptedException e) {
            e.printStackTrace();
            Log.d(TAG, "Error while waiting for process: " + e.getMessage());
        }
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.iv_back) {
            finish();
        } else if (id == R.id.layout_reset_factory) {
            if (resetFactoryStatus.isChecked()) {
                Log.d(TAG, "恢复出厂设置操作被关闭.");
            } else {
                Log.d(TAG, "恢复出厂设置操作被触发.");
                showFactoryResetConfirmationDialog();
            }
            resetFactoryStatus.setChecked(!resetFactoryStatus.isChecked());
        }
    }

    private void showFactoryResetConfirmationDialog() {
        String message = getResources().getString(R.string.reset_factory_dialog_tip);
        SpannableString spannableMessage = new SpannableString(message);

        // 设置整个字符串为红色
        spannableMessage.setSpan(new ForegroundColorSpan(Color.RED), 0, message.length(), 0);

        new AlertDialog.Builder(this)
                .setTitle(getResources().getString(R.string.reset_factory_dialog_title))
                .setMessage(spannableMessage)
                .setPositiveButton(getResources().getString(R.string.sure), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // 用户确认，执行恢复出厂设置
                        Log.d(TAG, "恢复出厂设置操作被确认.");
                        resetFactory();
                    }
                })
                .setNegativeButton(getResources().getString(R.string.cancel), null)
                .show();
    }
}