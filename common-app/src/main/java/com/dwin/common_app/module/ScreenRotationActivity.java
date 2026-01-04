package com.dwin.common_app.module;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;

import com.dwin.common_app.R;
import com.dwin.common_app.adapter.TextSpinnerAdapter;
import com.dwin.common_app.base.BaseFuncActivity;
import com.dwin.common_app.util.RotateUtils;

public class ScreenRotationActivity extends BaseFuncActivity implements View.OnClickListener {

    private TextView tv_toolbar;
    private ImageView iv_back;
    private Spinner screenRotate;
    private String[] mRotate;
    private TextSpinnerAdapter mAdapter;

    private static final String TAG = "ScreenRotationActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_screen_rotation);

        tv_toolbar = findViewById(R.id.tv_toolbar);
        iv_back = findViewById(R.id.iv_back);
        screenRotate = findViewById(R.id.screen_rotate);
        mRotate = getResources().getStringArray(R.array.screen_rotate_entries);
        mAdapter = new TextSpinnerAdapter(this, mRotate);
        screenRotate.setAdapter(mAdapter);

        tv_toolbar.setText(getResources().getString(R.string.system_screen_rotation));
        //Settings.System.putInt(getContentResolver(), Settings.System.USER_ROTATION, rotationValue);

        initSpinnerData();
        initEvent();
    }

    private void initEvent() {
        iv_back.setOnClickListener(this);

        // 监听 Spinner 的选择变化
        screenRotate.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View view, int position, long id) {
                String selectedRotation = mRotate[position];
                Log.d(TAG, "Spinner item selected: " + selectedRotation);

                // 进行屏幕旋转操作
                setScreenRotation(selectedRotation);  // 更新屏幕旋转角度
            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
                Log.d(TAG, "No item selected in Spinner.");
            }
        });
    }

    private void initSpinnerData() {
        String currentRotation = RotateUtils.getCurrentRotationFromFile();
        String formattedCurrentRotation = RotateUtils.formatRotation(currentRotation);
        Log.d(TAG, "Current rotation from file: " + formattedCurrentRotation);

        // 设置 Spinner 选中项
        int selectedPosition = RotateUtils.getRotationPosition(formattedCurrentRotation, mRotate);
        Log.d(TAG, "Selected position for Spinner: " + selectedPosition);

        // 设置初始旋转状态
        screenRotate.setSelection(selectedPosition, false);

    }

    // 设置屏幕旋转角度
    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.iv_back) {
            Log.d(TAG, "Back button clicked, finishing activity.");
            finish();
        }
    }

    private void setScreenRotation(String newValue) {
        String currentRotation = RotateUtils.getCurrentRotationFromFile();
        String formattedCurrentRotation = RotateUtils.formatRotation(currentRotation);
        String formattedNewValue = newValue.trim();
        Log.d(TAG, "Current rotation: " + formattedCurrentRotation + ", new value: " + newValue);

        // 检查新的旋转值是否与当前值相同
        if (formattedNewValue.equals(formattedCurrentRotation)) {
            Log.d(TAG, "Rotation angle is the same as current setting. No changes made.");
            return;
        }

        // 获取对应的 orientation 值
        String orientationValue = RotateUtils.getOrientationValue(formattedNewValue);

        // 写入新的旋转值到文件并进行重启
        if (RotateUtils.writeOrientationFile(orientationValue)) {
            // 更新 Spinner 的值为新选择的角度
            int newPosition = RotateUtils.getRotationPosition(formattedNewValue, mRotate);
            screenRotate.setSelection(newPosition);
            Log.d(TAG, "Rotation value written successfully. Rebooting system...");
            //重启
            mDWAndroidApi.rebootSystem();
        }
    }
}
