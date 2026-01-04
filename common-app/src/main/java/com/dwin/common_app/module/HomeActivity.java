package com.dwin.common_app.module;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.Configuration;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;

import com.dwin.common_app.R;
import com.dwin.common_app.util.ToastUtils;
import com.dwin.common_app.adapter.IconTextSpinnerAdapter;
import com.dwin.common_app.base.BaseFuncActivity;
import com.dwin.common_app.bean.AppInfoBean;

import java.util.ArrayList;
import java.util.List;

public class HomeActivity extends BaseFuncActivity implements View.OnClickListener {
    private static final String TAG = "HomeActivity";

    private TextView tvResetLauncher, tv_toolbar;
    private ImageView iv_back;
    private Spinner spinnerApps;
    private final List<AppInfoBean> mLists = new ArrayList<>();
    private IconTextSpinnerAdapter mAdapter;
    private final Handler mH = new Handler();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        tv_toolbar = findViewById(R.id.tv_toolbar);
        tv_toolbar.setText(getResources().getString(R.string.home_application));
        iv_back = findViewById(R.id.iv_back);
        tvResetLauncher = findViewById(R.id.tv_reset_launcher);

        spinnerApps = findViewById(R.id.spinner_apps);
        //String[] items = {"Item 1", "Item 2", "Item 3"};
        //ArrayAdapter<AppInfoBean> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, mLists);
        mAdapter = new IconTextSpinnerAdapter(this, filterApps());
        //adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerApps.setAdapter(mAdapter);

        initSpinnerData();
        initListener();
        registerAppInstallBroadcast();
    }

    private void initSpinnerData() {
        String launcher_pkgname = Settings.Global.getString(getContentResolver(), "launcher_pkgname");
        if (TextUtils.isEmpty(launcher_pkgname)) {
            launcher_pkgname = "com.android.launcher3";
        }
        List<AppInfoBean> list = mAdapter.getList();
        if (!TextUtils.isEmpty(launcher_pkgname) && list != null) {
            for (int i = 0; i < list.size(); i++) {
                if (launcher_pkgname.equals(list.get(i).getPkgName())) {
                    spinnerApps.setSelection(i, false);
                    break;
                }
            }
        }
    }

    private void initListener() {
        iv_back.setOnClickListener(this);
        tvResetLauncher.setOnClickListener(this);

        spinnerApps.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                // 选择项被选中时的回调
                AppInfoBean appInfoBean = mLists.get(position);
                // 处理选中的项...
                if (appInfoBean != null) {
                    String launcher_pkgname = Settings.Global.getString(getContentResolver(), "launcher_pkgname");
                    if (!TextUtils.isEmpty(appInfoBean.getPkgName())
                            && !TextUtils.isEmpty(launcher_pkgname)
                            && appInfoBean.getPkgName().equals(launcher_pkgname)) {
                        //  已经是当前桌面
                        return;
                    }
                    setDefaultLauncherImpl(appInfoBean.getPkgName(), appInfoBean.getClsName());
                }
                mH.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        if (mDWAndroidApi != null) {
                            mDWAndroidApi.rebootSystem();
                        }
                    }
                }, 1000);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // 没有选择任何项时的回调
            }
        });
    }

    @Override
    public void onConfigurationChanged(@NonNull Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        if (mAdapter != null) {
            mAdapter.setList(filterApps());
            mAdapter.notifyDataSetChanged();
        }
        Log.d("daibin", "onConfigurationChanged");
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.iv_back) {
            finish();
            //overridePendingTransition(R.anim.slide_in_bottom, R.anim.slide_out_bottom);
        } else if (id == R.id.tv_reset_launcher) {
            showResetLauncherTips();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unRegisterAppInstallBroadcast();
    }

    private void showResetLauncherTips() {
        new AlertDialog.Builder(HomeActivity.this)
                .setTitle(getString(R.string.reset_home_application_confirm))
                .setNegativeButton(getString(R.string.cancel), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                })
                .setPositiveButton(getString(R.string.sure), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String launcher_pkgname = Settings.Global.getString(getContentResolver(), "launcher_pkgname");
                        if (TextUtils.isEmpty(launcher_pkgname)) {
                            launcher_pkgname = "com.android.launcher3";
                        }

                        if ("com.android.launcher3".equals(launcher_pkgname)) {
                            ToastUtils.showShort(HomeActivity.this, getString(R.string.native_system_desktop));
                        } else {
                            setDefaultLauncherImpl("com.android.launcher3", "com.android.launcher3.uioverrides.QuickstepLauncher");
                            mH.postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    if (mDWAndroidApi != null) {
                                        mDWAndroidApi.rebootSystem();
                                    }
                                }
                            }, 1000);
                        }
                    }
                })
                .show();
    }

    private synchronized List<AppInfoBean> filterApps() {
        //String currentLauncherPackageName = Settings.Global.getString(getContentResolver(), "launcher_pkgname");

        mLists.clear();

        // Quickstep ,com.android.launcher3 ,com.android.launcher3.uioverrides.QuickstepLauncher
        Drawable applicationIcon = null;
        try {
            applicationIcon = getPackageManager().getApplicationIcon("com.android.launcher3");
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

        AppInfoBean b = new AppInfoBean();
        b.setAppName("Quickstep");
        b.setIcon(applicationIcon);
        b.setPkgName("com.android.launcher3");
        b.setClsName("com.android.launcher3.uioverrides.QuickstepLauncher");
        Log.d("daibin", b.getAppName() + " ," + b.getPkgName() + " ," + b.getClsName());
        mLists.add(b);

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
            Log.d("daibin", bean.getAppName() + " ," + bean.getPkgName() + " ," + bean.getClsName());
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

    private volatile boolean isRegisterInstallReceiver = false;

    private void registerAppInstallBroadcast() {
        IntentFilter filter = new IntentFilter();
        filter.addDataScheme("package");
        filter.addAction(Intent.ACTION_PACKAGE_ADDED);
        filter.addAction(Intent.ACTION_PACKAGE_REMOVED);
        filter.addAction(Intent.ACTION_PACKAGE_REPLACED);
        registerReceiver(installedReceiver, filter);
        isRegisterInstallReceiver = true;
    }

    private void unRegisterAppInstallBroadcast() {
        if (installedReceiver != null && isRegisterInstallReceiver) {
            unregisterReceiver(installedReceiver);
            isRegisterInstallReceiver = false;
        }
    }

    private final BroadcastReceiver installedReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (Intent.ACTION_PACKAGE_ADDED.equals(intent.getAction())) {
                String packageName = intent.getDataString();
                if (mAdapter != null) {
                    mAdapter.setList(filterApps());
                    mAdapter.notifyDataSetChanged();
                }
                Log.d("daibin", "安装了:" + packageName + "包名的程序");
                //Toast.makeText(context, "安装了:" + packageName + "包名的程序", Toast.LENGTH_SHORT).show();
            }
            if (Intent.ACTION_PACKAGE_REMOVED.equals(intent.getAction())) {
                String packageName = intent.getDataString();
                if (mAdapter != null) {
                    mAdapter.setList(filterApps());
                    mAdapter.notifyDataSetChanged();
                }
                Log.d("daibin", "卸载了:" + packageName + "包名的程序");
                //Toast.makeText(context, "卸载了:" + packageName + "包名的程序", Toast.LENGTH_SHORT).show();

                //if (!TextUtils.isEmpty(packageName) && packageName.contains("package:")) {
                //    packageName = packageName.replace("package:", "").trim();
                //}
                //String currentLauncherPackageName = Settings.Global.getString(context.getContentResolver(), "launcher_pkgname");
                //Log.d("daibin", "packageName:" + packageName + "  ,currentLauncherPackageName:" + currentLauncherPackageName);
                //if (!TextUtils.isEmpty(currentLauncherPackageName) && currentLauncherPackageName.equals(packageName)) {
                //    setDefaultLauncherImpl("com.dwin.dwinlauncher", "com.dwin.dwinlauncher.MainActivity");
                //}
            }
            if (Intent.ACTION_PACKAGE_REPLACED.equals(intent.getAction())) {
                String packageName = intent.getDataString();
                if (mAdapter != null) {
                    mAdapter.setList(filterApps());
                    mAdapter.notifyDataSetChanged();
                }
                Log.d("daibin", "覆盖安装了:" + packageName + "包名的程序");
                //Toast.makeText(context, "覆盖安装了:" + packageName + "包名的程序", Toast.LENGTH_SHORT).show();
            }
        }
    };

//    @RequiresApi(api = Build.VERSION_CODES.Q)
//    public void setRoleHolderAsUser(String roleName, String packageName,
//                                    int flags, UserHandle user, Context context) {
//        RoleManager roleManager = (RoleManager) context.getSystemService(Context.ROLE_SERVICE);
//        Executor executor = context.getMainExecutor();
//        Consumer<Boolean> callback = successful -> {
//            if (successful) {
//                Log.d(TAG, "Package added as role holder, role: " + roleName + ", package: " + packageName);
//            } else {
//                Log.d(TAG, "Failed to add package as role holder, role: " + roleName + ", package: "
//                        + packageName);
//            }
//        };
//        roleManager.addRoleHolderAsUser(roleName, packageName, flags, user, executor, callback);
//    }
    /**
     * 设置桌面应用
     *
     * @param  packageName 桌面应用包名
     * @param  className   桌面应用类名
     */
    private boolean setDefaultLauncherImpl(String packageName, String className) {
        if (TextUtils.isEmpty(packageName) || TextUtils.isEmpty(className)) {
            return false;
        }

        PackageManager pm = getPackageManager();
        //ResolveInfo currentLauncher = getCurrentLauncher();
        String currentLauncherPackageName = Settings.Global.getString(getContentResolver(), "launcher_pkgname");
        if (TextUtils.isEmpty(currentLauncherPackageName)) {
            currentLauncherPackageName = "com.android.launcher3";
        }
        Log.d("daibin", "setDefaultLauncherImpl currentLauncherPackageName:" + currentLauncherPackageName);
        List<ResolveInfo> packageInfos;
        if ("com.android.launcher3".equals(packageName)) {
            packageInfos = getResolveInfoList(true);
        } else {
            packageInfos = getResolveInfoList(false);
        }


        ResolveInfo futureLauncher = null;

        for (ResolveInfo ri : packageInfos) {
            if (!TextUtils.isEmpty(ri.activityInfo.packageName) && !TextUtils.isEmpty(packageName)
                    && TextUtils.equals(ri.activityInfo.packageName, packageName)) {
                futureLauncher = ri;
            }
        }
        if (futureLauncher == null) {
            Log.d("daibin", "setDefaultLauncherImpl futureLauncher == null");
            return false;
        }

        pm.clearPackagePreferredActivities(currentLauncherPackageName);
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(Intent.ACTION_MAIN);
        intentFilter.addCategory(Intent.CATEGORY_HOME);
        intentFilter.addCategory(Intent.CATEGORY_DEFAULT);
        intentFilter.setPriority(1000);
        ComponentName componentName = new ComponentName(futureLauncher.activityInfo.packageName,
                futureLauncher.activityInfo.name);
        ComponentName[] componentNames = new ComponentName[packageInfos.size()];
        int defaultMatch = 0;
        for (int i = 0; i < packageInfos.size(); i++) {
            ResolveInfo resolveInfo = packageInfos.get(i);
            componentNames[i] = new ComponentName(resolveInfo.activityInfo.packageName, resolveInfo.activityInfo.name);
            if (defaultMatch < resolveInfo.match) {
                defaultMatch = resolveInfo.match;
            }
        }
        //pm.clearPackagePreferredActivities(currentLauncher.activityInfo.packageName);
        pm.addPreferredActivity(intentFilter, defaultMatch, componentNames, componentName);
        Log.d("daibin", "setDefaultLauncherImpl success");
        Settings.Global.putString(getContentResolver(), "launcher_pkgname", packageName);
        Settings.Global.putString(getContentResolver(), "launcher_classname", className);
        getContentResolver().notifyChange(Settings.Global.getUriFor("launcher_pkgname"), null);
        getContentResolver().notifyChange(Settings.Global.getUriFor("launcher_classname"), null);
        return true;
    }

    private List<ResolveInfo> getResolveInfoList() {
        PackageManager pm = getPackageManager();
        Intent intent = new Intent(Intent.ACTION_MAIN, null);
        //intent.addCategory(Intent.CATEGORY_HOME);
        //intent.addCategory(Intent.CATEGORY_DEFAULT);
        intent.addCategory(Intent.CATEGORY_LAUNCHER);
        return pm.queryIntentActivities(intent, 0);
    }

    private List<ResolveInfo> getResolveInfoList(boolean isLauncher3) {
        PackageManager pm = getPackageManager();
        Intent intent = new Intent(Intent.ACTION_MAIN, null);
        //intent.addCategory(Intent.CATEGORY_DEFAULT);
        if (isLauncher3) {
            intent.addCategory(Intent.CATEGORY_HOME);
        } else {
            intent.addCategory(Intent.CATEGORY_LAUNCHER);
        }
        return pm.queryIntentActivities(intent, 0);
    }
}
