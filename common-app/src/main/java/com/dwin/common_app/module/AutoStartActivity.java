package com.dwin.common_app.module;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.Configuration;
import android.os.Bundle;
import android.provider.Settings;
import android.text.InputFilter;
import android.text.Spanned;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatButton;
import androidx.appcompat.widget.AppCompatEditText;
import androidx.appcompat.widget.SwitchCompat;

import com.dwin.common_app.R;
import com.dwin.common_app.adapter.IconTextSpinnerAdapter;
import com.dwin.common_app.base.BaseFuncActivity;
import com.dwin.common_app.bean.AppInfoBean;
import com.dwin.common_app.util.ToastUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class AutoStartActivity extends BaseFuncActivity implements View.OnClickListener {
    private static final String TAG = "AutoStartActivity";

    private TextView tv_toolbar, tvResetSystem;
    private ImageView iv_back;
    private SwitchCompat swStartApp;
    private RelativeLayout layoutStartApp;
    private Spinner spinnerSelfRestartApp, spinnerCrashRestartApp;
    private AppCompatEditText etIntervalTime;
    private AppCompatButton btnSave;
    private IconTextSpinnerAdapter mSelfRestartAdapter, mCrashRestartAdapter;
    private final List<AppInfoBean> mLists = new ArrayList<>();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_auto_start);
        tv_toolbar = findViewById(R.id.tv_toolbar);
        iv_back = findViewById(R.id.iv_back);
        spinnerSelfRestartApp = findViewById(R.id.spinner_self_restart_app);
        spinnerCrashRestartApp = findViewById(R.id.spinner_crash_restart_app);
        etIntervalTime = findViewById(R.id.et_interval_time);
        tv_toolbar.setText(getResources().getString(R.string.app_self_startup));

        mSelfRestartAdapter = new IconTextSpinnerAdapter(this, filterApps());
        spinnerSelfRestartApp.setAdapter(mSelfRestartAdapter);

        mCrashRestartAdapter = new IconTextSpinnerAdapter(this, filterApps());
        spinnerCrashRestartApp.setAdapter(mCrashRestartAdapter);

        layoutStartApp = findViewById(R.id.layout_start_app);
        swStartApp = findViewById(R.id.sw_start_app);

        btnSave = findViewById(R.id.btn_save);

        tvResetSystem = findViewById(R.id.tv_reset_system);

        initSpinnerData();
        initListener();
    }

    private void initSpinnerData() {
        int mAutoStartApp = Settings.Global.getInt(getContentResolver(), "allow_auto_start_custom_app", 0);
        swStartApp.setChecked(mAutoStartApp == 1);

        String pkgcls = Settings.Global.getString(getContentResolver(), "auto_start_custom_app_pkg");
        if (!TextUtils.isEmpty(pkgcls) && pkgcls.contains(",")) {
            String[] parts = pkgcls.split(",");
            if (parts.length == 2) {
                String pkg = parts[0];
                List<AppInfoBean> list = mSelfRestartAdapter.getList();
                if (!TextUtils.isEmpty(pkg) && list != null) {
                    for (int i = 0; i < list.size(); i++) {
                        AppInfoBean appInfo = list.get(i);
                        if (appInfo != null && pkg.equals(appInfo.getPkgName())) {
                            spinnerSelfRestartApp.setSelection(i, false);
                            break;
                        }
                    }
                }
            }
        }

        long mCheckTime = Settings.Global.getLong(getContentResolver(), "auto_start_custom_app_check_time", 30000L);
        etIntervalTime.setText((mCheckTime + ""));

        String protectApp = mDWAndroidApi.readProtectApp();
        if (!TextUtils.isEmpty(protectApp)) {
            List<AppInfoBean> list = mCrashRestartAdapter.getList();
            for (int i = 0; i < list.size(); i++) {
                AppInfoBean appInfo = list.get(i);
                if (appInfo != null && protectApp.equals(appInfo.getPkgName())) {
                    spinnerCrashRestartApp.setSelection(i, false);
                    break;
                }
            }
        }
    }

    private void initListener() {
        iv_back.setOnClickListener(this);
        layoutStartApp.setOnClickListener(this);
        btnSave.setOnClickListener(this);
        tvResetSystem.setOnClickListener(this);
        // 设置 Spinner 监听器
        spinnerSelfRestartApp.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                if (position >= 0 && position < mLists.size()) {
                    AppInfoBean selectedApp = mLists.get(position);
                    if (selectedApp == null) {
                        // 如果选择了null，清除设置的包名和类名
                        Settings.Global.putString(getContentResolver(), "auto_start_custom_app_pkg", "");
                        Log.d(TAG, "Cleared auto-start settings because 'null' was selected.");
                        syncSystem();
                        return;  // 直接返回，不保存任何信息
                    }
                    if (selectedApp != null) {
                        String packageName = selectedApp.getPkgName();
                        String className = selectedApp.getClsName();

                        // 将包名和类名保存到 Settings.Global
                        if (!TextUtils.isEmpty(packageName) && !TextUtils.isEmpty(className)) {
                            String pkgcls = packageName + "," + className; // 合并包名和类名
                            Settings.Global.putString(getContentResolver(), "auto_start_custom_app_pkg", pkgcls);  // 保存到 Settings.Global
                            Log.d(TAG, "Saved package and class: " + pkgcls);
                            syncSystem();
                            //showRebootDialog();
                        }
                    }
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
                // Nothing to do here
            }
        });

        spinnerCrashRestartApp.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                if (position >= 0 && position < mLists.size()) {
                    AppInfoBean selectedApp = mLists.get(position);
                    if (selectedApp == null) {
                        // 如果选择了null，清除设置的包名和类名
                        mDWAndroidApi.setProtectApp("");
                        Log.d(TAG, "Cleared crash-start settings because 'null' was selected.");
                        syncSystem();
                        return;  // 直接返回，不保存任何信息
                    }
                    if (selectedApp != null) {
                        String packageName = selectedApp.getPkgName();
                        // 将包名和类名保存到 Settings.Global
                        if (!TextUtils.isEmpty(packageName)) {
                            int i = mDWAndroidApi.setProtectApp(packageName);
                            Log.d(TAG, "Saved crash-start package: " + packageName + " ,result:" + i);
                            syncSystem();
                        }
                    }
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
                // Nothing to do here
            }
        });

        InputFilter[] filters = new InputFilter[1];
        filters[0] = new InputFilter() {
            public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {
                if (end > start) {
                    String destTxt = dest.toString();
                    String resultingTxt = destTxt.substring(0, dstart) + source.subSequence(start, end) + destTxt.substring(dend);
                    if (!resultingTxt.matches("\\d*") || Integer.parseInt(resultingTxt) < 0 || Integer.parseInt(resultingTxt) > 10 * 60 * 1000) {
                        return "";
                    }
                }
                return null;
            }
        };
        etIntervalTime.setFilters(filters);
    }


    private synchronized List<AppInfoBean> filterApps() {
        mLists.clear();
        mLists.add(null);  // 在列表开头加入 null

        List<ResolveInfo> apps = getApps();
        for (int i = 0; i < apps.size(); i++) {
            ResolveInfo resolveInfo = apps.get(i);
            String packageName = resolveInfo.activityInfo.packageName;
            String clsName = resolveInfo.activityInfo.name;

            if (TextUtils.isEmpty(clsName) && clsName.equals("com.android.settings.FallbackHome")) {
                continue;
            }
            AppInfoBean bean = new AppInfoBean();
            bean.setAppName(resolveInfo.loadLabel(getPackageManager()).toString());
            bean.setIcon(resolveInfo.loadIcon(getPackageManager()));
            bean.setPkgName(packageName);
            bean.setClsName(clsName);
            Log.d("huangchen", bean.getAppName() + " ," + bean.getPkgName() + " ," + bean.getClsName());
            mLists.add(bean);
        }
        return mLists;
    }

    @SuppressLint("QueryPermissionsNeeded")
    private List<ResolveInfo> getApps() {
        PackageManager pm = getPackageManager();
        Intent mainIntent = new Intent(Intent.ACTION_MAIN, null);
        mainIntent.addCategory(Intent.CATEGORY_LAUNCHER);
        return pm.queryIntentActivities(mainIntent, 0);
    }

//    private void showRebootDialog() {
//        new AlertDialog.Builder(this)
//                .setTitle("重启提示")
//                .setMessage("设置已保存。为了生效，请重启设备。")
//                .setPositiveButton("重启", new DialogInterface.OnClickListener() {
//                    @Override
//                    public void onClick(DialogInterface dialog, int which) {
//                        // 可以在此处触发设备重启，或者提供用户进一步操作的引导
//                        // 重启设备或重启应用的逻辑
//                        mDWAndroidApi.rebootSystem();
//                    }
//                })
//                .setNegativeButton("取消", null)
//                .show();
//    }

    @Override
    public void onConfigurationChanged(@NonNull Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        if (mSelfRestartAdapter != null) {
            mSelfRestartAdapter.setList(filterApps());
            mSelfRestartAdapter.notifyDataSetChanged();
        }
        Log.d("daibin", "onConfigurationChanged");
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.iv_back) {
            finish();
        } else if (v.getId() == R.id.layout_start_app) {
            if (swStartApp.isChecked()) {
                Settings.Global.putInt(getContentResolver(), "allow_auto_start_custom_app", 0);
            } else {
                Settings.Global.putInt(getContentResolver(), "allow_auto_start_custom_app", 1);
            }
            syncSystem();
            swStartApp.setChecked(!swStartApp.isChecked());
        } else if (v.getId() == R.id.btn_save) {
            String time = Objects.requireNonNull(etIntervalTime.getText()).toString();
            if (TextUtils.isEmpty(time)) {
                ToastUtils.showShort(AutoStartActivity.this, getResources().getString(R.string.interval_time_empty));
                return;
            }
            long intervalTime = Long.parseLong(time);
            Settings.Global.putLong(getContentResolver(), "auto_start_custom_app_check_time", intervalTime);
            syncSystem();
            ToastUtils.showShort(AutoStartActivity.this, getResources().getString(R.string.interval_time_set_success));
        } else if (v.getId() == R.id.tv_reset_system) {
            showRebootSystemTips();
        }
    }
}



