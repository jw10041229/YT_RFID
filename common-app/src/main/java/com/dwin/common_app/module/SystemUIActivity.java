package com.dwin.common_app.module;

import android.database.ContentObserver;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.widget.SwitchCompat;

import com.dwin.common_app.R;
import com.dwin.common_app.adapter.TextSpinnerAdapter;
import com.dwin.common_app.base.BaseFuncActivity;

import java.io.IOException;
import java.io.OutputStreamWriter;

public class SystemUIActivity extends BaseFuncActivity implements View.OnClickListener {

    public static final String SYS_PROPERTY_STATUS_BAR = "persist.sys.statusbar.enable";
    public static final String SYS_PROPERTY_NAVIGATION_BAR = "persist.sys.navigationbar.enable";
    public static final String SYS_PROPERTY_EXPLAN = "persist.sys.explan.enable";

    private static final int DEFAULT_VALUE = 1;

    private SwitchCompat topbarStatus, dropmenuStatus, navibarStatus;
    private TextView tv_toolbar;
    private ImageView iv_back;
    private Spinner spinnerDpi;
    private String[] mDpis;
    private TextSpinnerAdapter mAdapter;
    private RelativeLayout layoutStatusBar, layoutDropBar, layoutNaviBar;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_system_ui);
        topbarStatus = findViewById(R.id.topbar_status);
        dropmenuStatus = findViewById(R.id.dropmenu_status);
        navibarStatus = findViewById(R.id.navibar_status);
        layoutStatusBar = findViewById(R.id.layout_statusbar);
        layoutDropBar = findViewById(R.id.layout_dropbar);
        layoutNaviBar = findViewById(R.id.layout_navibar);
        tv_toolbar = findViewById(R.id.tv_toolbar);
        iv_back = findViewById(R.id.iv_back);
        tv_toolbar.setText(getResources().getString(R.string.system_ui));

        spinnerDpi = findViewById(R.id.spinner_dpi);
        mDpis = getResources().getStringArray(R.array.dpi);
        //String[] items = {"Item 1", "Item 2", "Item 3"};
        //ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, items);
        //adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mAdapter = new TextSpinnerAdapter(this, mDpis);
        spinnerDpi.setAdapter(mAdapter);

        initSpinnerData();
        initEvent();
        initData();
    }

    private final ContentObserver contentObserver = new ContentObserver(new Handler()) {
        @Override
        public void onChange(boolean selfChange, @Nullable Uri uri) {
            super.onChange(selfChange, uri);
            if (Settings.Global.getUriFor(SYS_PROPERTY_STATUS_BAR).equals(uri)) {
                topbarStatus.setChecked(Settings.Global.getInt(getContentResolver(), SYS_PROPERTY_STATUS_BAR, DEFAULT_VALUE) == 1);
            } else if (Settings.Global.getUriFor(SYS_PROPERTY_EXPLAN).equals(uri)) {
                dropmenuStatus.setChecked(Settings.Global.getInt(getContentResolver(), SYS_PROPERTY_EXPLAN, DEFAULT_VALUE) == 1);
            } else if (Settings.Global.getUriFor(SYS_PROPERTY_NAVIGATION_BAR).equals(uri)) {
                navibarStatus.setChecked(Settings.Global.getInt(getContentResolver(), SYS_PROPERTY_NAVIGATION_BAR, DEFAULT_VALUE) == 1);
            }
        }
    };

    private void initSpinnerData() {
        DisplayMetrics metrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metrics);
        String dpi = metrics.densityDpi + "";

        boolean isFind = false;
        if (mDpis != null) {
            for (int i = 0; i < mDpis.length; i++) {
                if (dpi.equals(mDpis[i])) {
                    spinnerDpi.setSelection(i, false);
                    isFind = true;
                    break;
                }
            }
        }
        if (!isFind) {
            spinnerDpi.setSelection(0, false);
        }
    }

    private void initData() {
        if (mDWAndroidApi != null) {
            topbarStatus.setChecked(mDWAndroidApi.isStatusBarShow() == 1);
            dropmenuStatus.setChecked(mDWAndroidApi.isDropDownMenuShow() == 1);
            navibarStatus.setChecked(mDWAndroidApi.isNavigationBarShow() == 1);
        }
    }

    private void initEvent() {
        iv_back.setOnClickListener(this);
        layoutStatusBar.setOnClickListener(this);
        layoutDropBar.setOnClickListener(this);
        layoutNaviBar.setOnClickListener(this);

        spinnerDpi.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                // 选择项被选中时的回调
                String dpi = mDpis[position];
                // 处理选中的项...
                if (dpi != null && !dpi.equals("none")) {
                    setSystemDensity(dpi);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // 没有选择任何项时的回调
            }
        });

        getContentResolver().registerContentObserver(Settings.Global.getUriFor(SYS_PROPERTY_STATUS_BAR), true, contentObserver);
        getContentResolver().registerContentObserver(Settings.Global.getUriFor(SYS_PROPERTY_NAVIGATION_BAR), true, contentObserver);
        getContentResolver().registerContentObserver(Settings.Global.getUriFor(SYS_PROPERTY_EXPLAN), true, contentObserver);
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.iv_back) {//Intent intent = new Intent(Settings.ACTION_DATE_SETTINGS);
            //if (intent.resolveActivity(getPackageManager()) != null) {
            //    startActivity(intent);
            //}
            finish();
        } else if (id == R.id.layout_statusbar) {
            if (topbarStatus.isChecked()) {
                mDWAndroidApi.hideStatusBar();
            } else {
                mDWAndroidApi.showStatusBar();
            }
            topbarStatus.setChecked(!topbarStatus.isChecked());
        } else if (id == R.id.layout_dropbar) {
            if (dropmenuStatus.isChecked()) {
                mDWAndroidApi.hideDropDownMenu();
            } else {
                mDWAndroidApi.showDropDownMenu();
            }
            dropmenuStatus.setChecked(!dropmenuStatus.isChecked());
        } else if (id == R.id.layout_navibar) {
            if (navibarStatus.isChecked()) {
                mDWAndroidApi.hideNavigationBar();
            } else {
                mDWAndroidApi.showNavigationBar();
            }
            navibarStatus.setChecked(!navibarStatus.isChecked());
        }
    }
    /**
     * 设置屏幕密度
     * @param  dpi 屏幕密度
     */
    private int setSystemDensity(String dpi) {
        Runtime runtime = Runtime.getRuntime();
        java.lang.Process proc = null;
        OutputStreamWriter osw = null;

        try { // Run Script
            proc = runtime.exec("su");
            osw = new OutputStreamWriter(proc.getOutputStream());
            osw.write("wm density " + dpi + "\n");
            osw.flush();
            osw.close();
            Log.d("daibin", "setSystemDensity success");
        } catch (IOException ex) {
            ex.printStackTrace();
            Log.d("daibin", "setSystemDensity error:" + ex.getMessage());
            return -1;
        } finally {
            if (osw != null) {
                try {
                    osw.close();
                } catch (IOException e) {
                    e.printStackTrace();
                    Log.d("daibin", "setSystemDensity error:" + e.getMessage());
                }
            }
        }
        try {
            if (proc != null)
                proc.waitFor();
            return 0;
        } catch (InterruptedException e) {
            e.printStackTrace();
            Log.d("daibin", "setSystemDensity error:" + e.getMessage());
            return -1;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        getContentResolver().unregisterContentObserver(contentObserver);
    }
}
