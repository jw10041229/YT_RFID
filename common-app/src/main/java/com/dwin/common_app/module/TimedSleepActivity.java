package com.dwin.common_app.module;

import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;

import com.dwin.common_app.R;
import com.dwin.common_app.adapter.TextSpinnerAdapter;
import com.dwin.common_app.base.BaseFuncActivity;

public class TimedSleepActivity extends BaseFuncActivity implements View.OnClickListener {

    private TextView tv_toolbar;
    private ImageView iv_back;
    private Spinner mSpTimedSleep;
    private String[] mTimedValues;
    private TextSpinnerAdapter mAdapter;

    private static final String TAG = "TimedSleepActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_timed_sleep);

        tv_toolbar = findViewById(R.id.tv_toolbar);
        iv_back = findViewById(R.id.iv_back);
        mSpTimedSleep = findViewById(R.id.sp_timed_sleep);
        mTimedValues = getResources().getStringArray(R.array.screen_timeout_values);
        mAdapter = new TextSpinnerAdapter(this, getResources().getStringArray(R.array.screen_timeout_entries));
        mSpTimedSleep.setAdapter(mAdapter);

        tv_toolbar.setText(getResources().getString(R.string.timed_sleep));

        initSpinnerData();
        initEvent();
    }

    private void initEvent() {
        iv_back.setOnClickListener(this);

        // 监听 Spinner 的选择变化
        mSpTimedSleep.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View view, int position, long id) {
                String selectedTime = mTimedValues[position];
                Log.d(TAG, "Spinner item selected: " + selectedTime);

                setScreenSleepTime(selectedTime);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
                Log.d(TAG, "No item selected in Spinner.");
            }
        });
    }

    private void initSpinnerData() {
        long currentSleepTime = Settings.System.getLong(getContentResolver(), Settings.System.SCREEN_OFF_TIMEOUT, 2147483647);
        for (int i = 0; i < mTimedValues.length; i++) {
            try {
                if (Long.parseLong(mTimedValues[i]) == currentSleepTime) {
                    mSpTimedSleep.setSelection(i, false);
                    break;
                }
            } catch (Exception ignored) {
            }
        }
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.iv_back) {
            Log.d(TAG, "Back button clicked, finishing activity.");
            finish();
        }
    }

    private void setScreenSleepTime(String newValue) {
        long selectSleepTime = 0L;
        try {
            selectSleepTime = Long.parseLong(newValue);
        } catch (Exception e) {
            selectSleepTime = 2147483647;
        }

        long currentSleepTime = Settings.System.getLong(getContentResolver(), Settings.System.SCREEN_OFF_TIMEOUT, 2147483647);

        // 检查新的旋转值是否与当前值相同
        if (selectSleepTime == currentSleepTime) {
            Log.d(TAG, "Set sleep time no change");
            return;
        }
        Settings.System.putLong(getContentResolver(), Settings.System.SCREEN_OFF_TIMEOUT, selectSleepTime);
    }
}
