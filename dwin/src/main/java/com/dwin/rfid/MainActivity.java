package com.dwin.rfid;

import android.content.Context;
import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.dwin.common_app.adapter.FunctionAdapter;
import com.dwin.common_app.bean.FuncBean;
import com.dwin.common_app.module.AutoStartActivity;
import com.dwin.common_app.module.BootAnimationActivity;
import com.dwin.common_app.module.FirmwareActivity;
import com.dwin.common_app.module.GpioActivity;
import com.dwin.common_app.module.InstallActivity;
import com.dwin.common_app.module.LogActivity;
import com.dwin.common_app.module.NetworkActivity;
import com.dwin.common_app.module.ResetFactoryActivity;
import com.dwin.common_app.module.ScreenRotationActivity;
import com.dwin.common_app.module.SerialPortActivity;
import com.dwin.common_app.module.SystemUIActivity;
import com.dwin.common_app.module.HomeActivity;
import com.dwin.common_app.module.TimeActivity;
import com.dwin.common_app.module.WallpaperActivity;
import com.dwin.common_app.rfiddemo.ReadOrWrite1Activity;
import com.dwin.common_app.rfiddemo.ReadOrWrite2Activity;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity implements FunctionAdapter.OnItemClickListener, View.OnClickListener {
    private static final String TAG = "MainActivity";

    public static final int ID_HOME_APPLICATION = 1;
    public static final int ID_BOOT_ANIMATION = 2;
    public static final int ID_SYSTEM_UI = 3;
    public static final int ID_SYSTEM_TIME = 4;
    public static final int ID_HARDWARE_INFORMATION = 5;
    public static final int ID_SYSTEM_WALLPAPER = 6;
    public static final int ID__SYSTEM_SCREEN_ROTATION = 7;
    public static final int ID_APP_SELF_START = 8;
    public static final int ID_SILENT_INSTALLTATION = 9;
    public static final int ID_NETWORK = 10;
    public static final int ID_TIMED_SLEEP = 11;
    public static final int ID_LOG = 12;
    public static final int ID_SERIAL_PORT = 13;
    public static final int ID_GPIO = 14;
    public static final int ID_RESET_FACTORY = 15;

    private FunctionAdapter functionAdapter;
    private RecyclerView recyclerView;
    private ArrayList<FuncBean> funcBeans = new ArrayList<>();
    private GridLayoutManager gridLayout;
    private SpaceItemDecoration spaceItemDecoration;
    private ImageView iv_back;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        iv_back = findViewById(R.id.iv_back);
        iv_back.setOnClickListener(this);
        recyclerView = findViewById(R.id.recycler_view);
        initData();
        startActivity(new Intent(this, ReadOrWrite2Activity.class));
    }

    private String mBootAnimation = "";
    private String mSystemUI = "";
    private String mSystemTime = "";
    private String mHardwareInformation = "";
    private String mSystemScreenRotation = "";
    private String mAppSelfStart = "";
    private String mSilentInstallation = "";
    private String mHomeApplication = "";
    private String mSystemWallpaper = "";
    private String mNetwork = "";
    private String mTimedSleep = "";
    private String mLog = "";
    private String mSerialPort = "";
    private String mGpio = "";
    private String mResetFactory = "";

    private void initData() {
        mBootAnimation = getResources().getString(R.string.boot_animation);
        mSystemUI = getResources().getString(R.string.system_ui);
        mSystemTime = getResources().getString(R.string.system_time);
        mHardwareInformation = getResources().getString(R.string.hardware_information);
        mSystemScreenRotation = getResources().getString(R.string.system_screen_rotation);
        mAppSelfStart = getResources().getString(R.string.app_self_startup);
        mSilentInstallation = getResources().getString(R.string.silent_installation);
        mHomeApplication = getResources().getString(R.string.home_application);
        mSystemWallpaper = getResources().getString(R.string.system_wallpaper);
        mNetwork = getResources().getString(R.string.network);
        mTimedSleep = getResources().getString(R.string.timed_sleep);
        mLog = getResources().getString(R.string.logcat);
        mSerialPort = getResources().getString(R.string.serial_port);
        mGpio = getResources().getString(R.string.gpio);
        mResetFactory = getResources().getString(R.string.reset_factory);

        FuncBean homeBean = new FuncBean(ID_HOME_APPLICATION, mHomeApplication, R.drawable.ic_baseline_home_24);
        FuncBean bootAnimationBean = new FuncBean(ID_BOOT_ANIMATION, mBootAnimation, R.drawable.ic_baseline_remove_red_eye_24);
        FuncBean systemUiBean = new FuncBean(ID_SYSTEM_UI, mSystemUI, R.drawable.ic_baseline_clarify_24px);
        FuncBean timeBean = new FuncBean(ID_SYSTEM_TIME, mSystemTime, R.drawable.ic_baseline_access_time_24);
        FuncBean hardwareStatusBean = new FuncBean(ID_HARDWARE_INFORMATION, mHardwareInformation, R.drawable.ic_baseline_memory_24);
        FuncBean wallpaperBean = new FuncBean(ID_SYSTEM_WALLPAPER, mSystemWallpaper, R.drawable.ic_baseline_wallpaper_24);
        FuncBean screenRotationBean = new FuncBean(ID__SYSTEM_SCREEN_ROTATION, mSystemScreenRotation, R.drawable.ic_baseline_screen_rotation_24);
        FuncBean autoStartBean = new FuncBean(ID_APP_SELF_START, mAppSelfStart, R.drawable.ic_baseline_auto_start_24);
        FuncBean installBean = new FuncBean(ID_SILENT_INSTALLTATION, mSilentInstallation, R.drawable.install_icon);
        FuncBean networkBean = new FuncBean(ID_NETWORK, mNetwork, R.drawable.ic_baseline_settings_ethernet_24);
        //FuncBean timedSleepBean = new FuncBean(ID_TIMED_SLEEP, mTimedSleep, R.drawable.ic_baseline_screen_timeout_24);
        FuncBean logBean = new FuncBean(ID_LOG, mLog, R.drawable.ic_baseline_log_24);
        FuncBean serialBean = new FuncBean(ID_SERIAL_PORT, mSerialPort, R.drawable.ic_baseline_serial_port_24);
        FuncBean gpioBean = new FuncBean(ID_GPIO, mGpio, R.drawable.ic_baseline_gpio_24);
        FuncBean resetFactoryBean = new FuncBean(ID_RESET_FACTORY, mResetFactory, R.drawable.ic_baseline_reset_factory_24);

        funcBeans.add(homeBean);
        funcBeans.add(bootAnimationBean);
        funcBeans.add(systemUiBean);
        funcBeans.add(timeBean);
        funcBeans.add(hardwareStatusBean);
        funcBeans.add(wallpaperBean);
        funcBeans.add(screenRotationBean);
        funcBeans.add(autoStartBean);
        funcBeans.add(installBean);
        funcBeans.add(networkBean);
        //if (BuildConfig.isShowTimedSleep) {
        //    funcBeans.add(timedSleepBean);
        //}
        funcBeans.add(logBean);
        funcBeans.add(serialBean);
        funcBeans.add(gpioBean);
        funcBeans.add(resetFactoryBean);

        recyclerView.setPadding(0, 0, 0, 0);
        spaceItemDecoration = new SpaceItemDecoration(1, 1);
        recyclerView.addItemDecoration(spaceItemDecoration);

        functionAdapter = new FunctionAdapter(funcBeans, this);
        recyclerView.setAdapter(functionAdapter);
        gridLayout = new GridLayoutManager(this, 3);
        gridLayout.setSpanCount(3);
        recyclerView.setLayoutManager(gridLayout);

        getAndroiodScreenProperty();
        Log.d("daibin", "getCPUID():" + getCPUID());
    }

    public static String getCPUID() {
        String cpuId = "";
        try {
            Process process = Runtime.getRuntime().exec("cat /proc/cpuinfo");
            InputStream is = process.getInputStream();
            BufferedReader br = new BufferedReader(new InputStreamReader(is));
            String line;
            while ((line = br.readLine()) != null) {
                if (line.contains("Serial") || line.contains("serial")) {
                    String[] parts = line.split(":");
                    if (parts.length > 1) {
                        cpuId = parts[1].trim();
                        break;
                    }
                }
            }
            is.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return cpuId;
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        //Log.d("daibin","dispatchKeyEvent");
        return super.dispatchKeyEvent(event);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        //Log.d("daibin","onKeyDown");
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        Log.d("daibin", "onKeyUp  keyCode:" + keyCode + " ," + event.getUnicodeChar() + "  ," + event.getCharacters());
        return super.onKeyUp(keyCode, event);
    }

    @Override
    public void onItemClick(View v, int position) {
        FuncBean clickedBean = funcBeans.get(position);
        switch (clickedBean.getFunctionId()) {
            case ID_HOME_APPLICATION:
                v.getContext().startActivity(new Intent(v.getContext(), HomeActivity.class));
                //overridePendingTransition(R.anim.slide_in_bottom, R.anim.slide_out_bottom);
                break;
            case ID_BOOT_ANIMATION:
                v.getContext().startActivity(new Intent(v.getContext(), BootAnimationActivity.class));
                break;
            case ID_SYSTEM_UI:
                v.getContext().startActivity(new Intent(v.getContext(), SystemUIActivity.class));
                break;
            case ID_SYSTEM_TIME:
                v.getContext().startActivity(new Intent(v.getContext(), TimeActivity.class));
                break;
            case ID_HARDWARE_INFORMATION:
                v.getContext().startActivity(new Intent(v.getContext(), FirmwareActivity.class));
                break;
            case ID_SYSTEM_WALLPAPER:
                v.getContext().startActivity(new Intent(v.getContext(), WallpaperActivity.class));
                break;
            case ID__SYSTEM_SCREEN_ROTATION:
                v.getContext().startActivity(new Intent(v.getContext(), ScreenRotationActivity.class));
                break;
            case ID_APP_SELF_START:
                v.getContext().startActivity(new Intent(v.getContext(), AutoStartActivity.class));
                break;
            case ID_SILENT_INSTALLTATION:
                v.getContext().startActivity(new Intent(v.getContext(), InstallActivity.class));
                break;
            case ID_NETWORK:
                v.getContext().startActivity(new Intent(v.getContext(), NetworkActivity.class));
                break;
            case ID_LOG:
                v.getContext().startActivity(new Intent(v.getContext(), LogActivity.class));
                break;
            case ID_SERIAL_PORT:
                v.getContext().startActivity(new Intent(v.getContext(), SerialPortActivity.class));
                break;
            case ID_GPIO:
                v.getContext().startActivity(new Intent(v.getContext(), GpioActivity.class));
                break;
            case ID_RESET_FACTORY:
                v.getContext().startActivity(new Intent(v.getContext(), ResetFactoryActivity.class));
                break;
        }
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.iv_back) {
            finish();
        }
    }

    static class SpaceItemDecoration extends RecyclerView.ItemDecoration {
        //leftRight为横向间的距离 topBottom为纵向间距离
        private int leftRight;
        private int topBottom;

        public void setDecoration(int leftRight, int topBottom) {
            this.leftRight = leftRight;
            this.topBottom = topBottom;
        }

        public SpaceItemDecoration(int leftRight, int topBottom) {
            this.leftRight = leftRight;
            this.topBottom = topBottom;
        }

        @Override
        public void onDraw(Canvas c, RecyclerView parent, RecyclerView.State state) {
            super.onDraw(c, parent, state);
        }

        @Override
        public void getItemOffsets(Rect outRect, android.view.View view, RecyclerView parent, RecyclerView.State state) {
            LinearLayoutManager layoutManager = (LinearLayoutManager) parent.getLayoutManager();
            //竖直方向的
            if (layoutManager.getOrientation() == LinearLayoutManager.VERTICAL) {
                //最后一项需要 bottom
                if (parent.getChildAdapterPosition(view) == layoutManager.getItemCount() - 1) {
                    outRect.bottom = topBottom;
                }
                outRect.top = topBottom;
                outRect.left = leftRight;
                outRect.right = leftRight;
            } else {
                //最后一项需要right
                if (parent.getChildAdapterPosition(view) == layoutManager.getItemCount() - 1) {
                    outRect.right = leftRight;
                }
                outRect.top = topBottom;
                outRect.left = leftRight;
                outRect.bottom = topBottom;
            }
        }
    }

    public void getAndroiodScreenProperty() {
        WindowManager wm = (WindowManager) this.getSystemService(Context.WINDOW_SERVICE);
        DisplayMetrics dm = new DisplayMetrics();
        wm.getDefaultDisplay().getMetrics(dm);
        int width = dm.widthPixels;         // 屏幕宽度（像素）
        int height = dm.heightPixels;       // 屏幕高度（像素）
        float density = dm.density;         // 屏幕密度（0.75 / 1.0 / 1.5）
        int densityDpi = dm.densityDpi;     // 屏幕密度dpi（120 / 160 / 240）
        // 屏幕宽度算法:屏幕宽度（像素）/屏幕密度
        int screenWidth = (int) (width / density);  // 屏幕宽度(dp)
        int screenHeight = (int) (height / density);// 屏幕高度(dp)

        Log.d("daibin", "屏幕宽度（像素）：" + width);
        Log.d("daibin", "屏幕高度（像素）：" + height);
        Log.d("daibin", "屏幕密度（0.75 / 1.0 / 1.5）：" + density);
        Log.d("daibin", "屏幕密度dpi（120 / 160 / 240）：" + densityDpi);
        Log.d("daibin", "屏幕宽度（dp）：" + screenWidth);
        Log.d("daibin", "屏幕高度（dp）：" + screenHeight);
    }
}