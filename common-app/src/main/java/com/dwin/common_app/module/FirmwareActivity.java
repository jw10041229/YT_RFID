package com.dwin.common_app.module;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.dwin.common_app.R;
import com.dwin.common_app.base.BaseFuncActivity;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class FirmwareActivity extends BaseFuncActivity implements View.OnClickListener {
    private static final String TAG = "FirmwareActivity";

    private TextView tv_toolbar, tvDeviceName, tvDeviceModel, tvCpuModel, tvAndroidVers, tvFirmwareVers, tvDeviceManufacturer, tvSerialNo;
    private ImageView iv_back;

    @SuppressLint("HardwareIds")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_firmware);

        tv_toolbar = findViewById(R.id.tv_toolbar);
        tv_toolbar.setText(getResources().getString(R.string.hardware_information));
        iv_back = findViewById(R.id.iv_back);
        iv_back.setOnClickListener(this);

        tvDeviceName = findViewById(R.id.tv_device_name);
        tvDeviceModel = findViewById(R.id.tv_device_model);
        tvDeviceManufacturer = findViewById(R.id.tv_device_manufacturer);
        tvCpuModel = findViewById(R.id.tv_cpu_model);
        tvAndroidVers = findViewById(R.id.tv_android_vers);
        tvFirmwareVers = findViewById(R.id.tv_firmware_vers);
        tvSerialNo = findViewById(R.id.tv_serialno);

        tvDeviceName.setText(getDeviceName(this));
        tvDeviceModel.setText(Build.MODEL);
        tvDeviceManufacturer.setText(Build.MANUFACTURER);
        tvCpuModel.setText(getCpuModelFromCpuinfo());
        tvAndroidVers.setText(Build.VERSION.RELEASE);
        tvFirmwareVers.setText((Build.ID + " " + Build.VERSION.INCREMENTAL));
        tvSerialNo.setText(getCPUID());
        //if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        //    tvSerialNo.setText(Build.getSerial());
        //} else {
        // tvSerialNo.setText(Build.SERIAL);
        //}
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.iv_back) {
            finish();
        }
    }

    public static String getDeviceName(Context context) {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.P) {
            // API 28 and above
            return Settings.Global.getString(context.getContentResolver(), Settings.Global.DEVICE_NAME);
        } else {
            // API 27 and below
            return Build.MODEL;
        }
    }

    public static String getCpuModelFromCpuinfo() {
        String cpuModel = "";
        try (BufferedReader bufferedReader = new BufferedReader(new FileReader("/proc/cpuinfo"))) {
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                if (line.contains("Hardware")) {
                    int index = line.indexOf(":");
                    if (index != -1) {
                        cpuModel = line.substring(index + 1).trim();
                        break;
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return cpuModel;
    }

    public static String getCPUID() {
        String cpuId = "";
        try {
            Process process = Runtime.getRuntime().exec("cat /proc/cpuinfo");
            BufferedReader br = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;
            while ((line = br.readLine()) != null) {
                if (line.toLowerCase().contains("serial")) {
                    String[] parts = line.split(":");
                    if (parts.length > 1) {
                        cpuId = parts[1].trim();
                        break;
                    }
                }
            }
            br.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return cpuId;
    }

}