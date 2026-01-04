package com.dwin.common_app.module;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.DatePicker;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;

import androidx.annotation.Nullable;
import androidx.appcompat.widget.SwitchCompat;
import androidx.core.content.res.ResourcesCompat;

import com.dwin.common_app.base.BaseFuncActivity;
import com.dwin.common_app.R;
import com.dwin.common_app.util.SystemUtils;

import java.util.Calendar;
import java.util.Locale;
import java.util.TimeZone;

public class TimeActivity extends BaseFuncActivity implements View.OnClickListener {
    private static final String TAG = "TimeActivity";

    private RelativeLayout layoutAutoTime, layoutTime12_24, layoutAutoTimezone, layoutTimezone;
    private SwitchCompat autoTimeStatus, time12_24Status, autoTimezoneStatus;
    private Spinner spinnerTimeZone;
    private TimePickerDialog timePickerDialog;
    private TimePickerDialog.OnTimeSetListener timeSetListener;
    private DatePickerDialog datePickerDialog;
    private DatePickerDialog.OnDateSetListener dateSetListener;
    private TextView tv_toolbar, tvSetTime, tvSetDate;
    private ImageView iv_back;
    private Calendar calendar;
    private SystemUtils mSystemUtils;
    private String[] mTimeZones;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_time);
        layoutAutoTime = findViewById(R.id.layout_autotime);
        layoutTime12_24 = findViewById(R.id.layout_time12_24);
        layoutAutoTimezone = findViewById(R.id.layout_autotimezone);
        layoutTimezone = findViewById(R.id.layout_timezone);
        autoTimeStatus = findViewById(R.id.autotime_status);
        time12_24Status = findViewById(R.id.time12_24_status);
        autoTimezoneStatus = findViewById(R.id.autotimezone_status);
        tvSetTime = findViewById(R.id.tv_set_time);
        tvSetDate = findViewById(R.id.tv_set_date);
        spinnerTimeZone = findViewById(R.id.spinner_timezone);
        tv_toolbar = findViewById(R.id.tv_toolbar);
        iv_back = findViewById(R.id.iv_back);
        tv_toolbar.setText(getResources().getString(R.string.system_time));
        initData();
        initListener();
    }

    private void initData() {
        mSystemUtils = new SystemUtils();

        boolean isTimeAuto = mSystemUtils.checkTimeAutoMode(this);
        autoTimeStatus.setChecked(isTimeAuto);
        updateTimeAndDateUI(autoTimeStatus.isChecked());

        time12_24Status.setChecked(mSystemUtils.checkTime_12_24Mode(this));

        boolean isTimezoneAuto = mSystemUtils.checkTimeZoneAutoMode(this);
        autoTimezoneStatus.setChecked(isTimezoneAuto);
        updateTimezoneUI(autoTimezoneStatus.isChecked());

        updateTimezoneData();
    }

    private void updateTimezoneData() {
        String defaultTimezone = TimeZone.getDefault().getID();
        Log.d(TAG, "defaultTimezone:" + defaultTimezone);
        mTimeZones = getResources().getStringArray(R.array.time_zone);
        int index = -1;
        if (mTimeZones != null) {
            for (int i = 0; i < mTimeZones.length; i++) {
                String timeZone = mTimeZones[i];
                if (timeZone != null && timeZone.equals(defaultTimezone)) {
                    spinnerTimeZone.setSelection(i, false);
                    index = i;
                    break;
                }
            }
        }
        if (index == -1) {
            spinnerTimeZone.setSelection(0, false);
        }
    }

    private void initListener() {
        iv_back.setOnClickListener(this);
        layoutAutoTime.setOnClickListener(this);
        layoutTime12_24.setOnClickListener(this);
        layoutAutoTimezone.setOnClickListener(this);
        tvSetTime.setOnClickListener(this);
        tvSetDate.setOnClickListener(this);

        timeSetListener = new TimePickerDialog.OnTimeSetListener() {
            @Override
            public void onTimeSet(TimePicker timePicker, int hour, int minute) {
                Log.i(TAG, "hour == " + hour + ", minute == " + minute +
                        ", second == " + 0 + ", millisecond == " + 0);
                if (mSystemUtils != null)
                    mSystemUtils.setTime(hour, minute);
            }
        };

        dateSetListener = new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker datePicker, int year, int month, int day) {
                Log.i(TAG, "year == " + year + ", month == " + month + 1 + ", day == " + day);
                if (mSystemUtils != null)
                    mSystemUtils.setDate(year, month + 1, day);
            }
        };

        spinnerTimeZone.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (mSystemUtils != null && mTimeZones != null && position < mTimeZones.length) {
                    String selectTz = mTimeZones[position];
                    if (selectTz == null || "undefine/undefine".equals(selectTz)) {
                        return;
                    }
                    String defaultTimezone = TimeZone.getDefault().getID();
                    if (selectTz.equals(defaultTimezone)) {
                        // 已是当前时区
                        return;
                    }
                    mSystemUtils.setTimeZone(TimeActivity.this, selectTz);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        registerTimeBroadcastReceiver();
    }

    private void registerTimeBroadcastReceiver() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_TIMEZONE_CHANGED);
        filter.addAction(Intent.ACTION_TIME_CHANGED);
        filter.addAction(Intent.ACTION_DATE_CHANGED);
        registerReceiver(timeReceiver, filter);
    }

    private void unRegisterTimeBroadcastReceiver() {
        unregisterReceiver(timeReceiver);
    }

    private final BroadcastReceiver timeReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (Intent.ACTION_TIMEZONE_CHANGED.equals(intent.getAction())) {
                Log.d(TAG, "ACTION_TIMEZONE_CHANGED");
                updateTimezoneData();
            } else if (Intent.ACTION_TIME_CHANGED.equals(intent.getAction())) {

            } else if (Intent.ACTION_DATE_CHANGED.equals(intent.getAction())) {

            }
        }
    };

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.iv_back) {
            finish();
        } else if (id == R.id.layout_autotime) {
            if (autoTimeStatus.isChecked()) {
                mSystemUtils.setTimeAutoMode(TimeActivity.this, 0);
            } else {
                mSystemUtils.setTimeAutoMode(TimeActivity.this, 1);
            }
            autoTimeStatus.setChecked(!autoTimeStatus.isChecked());
            updateTimeAndDateUI(autoTimeStatus.isChecked());
        } else if (id == R.id.layout_time12_24) {
            if (time12_24Status.isChecked()) {
                mSystemUtils.setTime_12_24Mode(TimeActivity.this, "12");
            } else {
                mSystemUtils.setTime_12_24Mode(TimeActivity.this, "24");
            }
            time12_24Status.setChecked(!time12_24Status.isChecked());
        } else if (id == R.id.layout_autotimezone) {
            if (autoTimezoneStatus.isChecked()) {
                mSystemUtils.setTimeZoneAutoMode(TimeActivity.this, 0);
            } else {
                mSystemUtils.setTimeZoneAutoMode(TimeActivity.this, 1);
            }
            autoTimezoneStatus.setChecked(!autoTimezoneStatus.isChecked());
            updateTimezoneUI(autoTimezoneStatus.isChecked());
        } else if (id == R.id.tv_set_time) {
            calendar = Calendar.getInstance();
            // 时间选择样式为圆盘
//                timePickerDialog = new TimePickerDialog(TimeActivity.this, timeSetListener,
//                        calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), true);
            // 时间选择样式为滚轮
            timePickerDialog = new TimePickerDialog(TimeActivity.this, AlertDialog.THEME_HOLO_LIGHT, timeSetListener,
                    calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), true);
            timePickerDialog.show();
        } else if (id == R.id.tv_set_date) {
            calendar = Calendar.getInstance(Locale.getDefault());
            // 日期选择样式为圆盘
//                datePickerDialog = new DatePickerDialog(TimeActivity.this, AlertDialog.THEME_HOLO_LIGHT, dateSetListener,
//                        calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH) + 1, calendar.get(Calendar.DAY_OF_MONTH));
            // 日期选择样式为滚轮
            datePickerDialog = new DatePickerDialog(TimeActivity.this, AlertDialog.THEME_HOLO_LIGHT, dateSetListener,
                    calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));
            datePickerDialog.show();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unRegisterTimeBroadcastReceiver();
    }

    private void updateTimeAndDateUI(boolean isAutoTime) {
        if (isAutoTime) {
            tvSetTime.setEnabled(false);
            tvSetDate.setEnabled(false);
            tvSetTime.setBackground(ResourcesCompat.getDrawable(getResources(), R.drawable.background_disable_border_bottom, null));
            tvSetDate.setBackground(ResourcesCompat.getDrawable(getResources(), R.drawable.background_disable_border_bottom, null));
        } else {
            tvSetTime.setEnabled(true);
            tvSetDate.setEnabled(true);
            tvSetTime.setBackground(ResourcesCompat.getDrawable(getResources(), R.drawable.background_border_bottom, null));
            tvSetDate.setBackground(ResourcesCompat.getDrawable(getResources(), R.drawable.background_border_bottom, null));
        }
    }

    private void updateTimezoneUI(boolean isAutoTimezone) {
        if (isAutoTimezone) {
            spinnerTimeZone.setEnabled(false);
            layoutTimezone.setBackground(ResourcesCompat.getDrawable(getResources(), R.drawable.background_disable_border_bottom, null));
        } else {
            spinnerTimeZone.setEnabled(true);
            layoutTimezone.setBackground(ResourcesCompat.getDrawable(getResources(), R.drawable.background_border_bottom, null));
        }
    }
}
