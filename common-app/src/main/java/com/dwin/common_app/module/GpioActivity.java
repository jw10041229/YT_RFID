package com.dwin.common_app.module;

import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.dwin.common_app.R;
import com.dwin.gpiolibrary.Gpio;
import com.dwin.gpiolibrary.GpioInitCallBack;

import java.util.List;

public class GpioActivity extends AppCompatActivity implements GpioInitCallBack {
    private final static String TAG = GpioActivity.class.getName();

    private TextView tv_toolbar;
    private EditText mEtGpioNum;
    private Spinner mSpGpioValue,mSpGpioDirection;
    private ImageView iv_back;
    private int pendingSetGpioNum = -1;
    private int pendingSetValue = -1;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gpio);
        tv_toolbar = findViewById(R.id.tv_toolbar);
        tv_toolbar.setText(getResources().getString(R.string.gpio));
        iv_back = findViewById(R.id.iv_back);
        iv_back.setOnClickListener(v -> {
            finish();
        });

        mEtGpioNum = findViewById(R.id.et_gpio_num);
        mSpGpioValue = findViewById(R.id.spinner_gpio_value);
        mSpGpioDirection = findViewById(R.id.spinner_gpio_direction);

        // 初始化 Spinner 适配器
        ArrayAdapter<CharSequence> valueAdapter = ArrayAdapter.createFromResource(this,
                R.array.gpio_value_array, android.R.layout.simple_spinner_item);
        valueAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mSpGpioValue.setAdapter(valueAdapter);

        ArrayAdapter<CharSequence> directionAdapter = ArrayAdapter.createFromResource(this,
                R.array.gpio_direction_array, android.R.layout.simple_spinner_item);
        directionAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mSpGpioDirection.setAdapter(directionAdapter);

        // 初始化 GPIO

        //int[] gpioArr = {20, 148, 115, 114, 126, 127, 128, 153};
        //Gpio.initGpio(gpioArr, 0, "in", this, this);

        // 设置 GPIO value
        findViewById(R.id.btn_set_gpio_value).setOnClickListener(v -> {
            try {
                int gpioNum = Integer.parseInt(mEtGpioNum.getText().toString());
                int value = Integer.parseInt(mSpGpioValue.getSelectedItem().toString());
                pendingSetGpioNum = gpioNum;
                pendingSetValue = value;

                int[] singleGpio = {gpioNum};
                Gpio.initGpio(singleGpio, 0, "in", this, this);
            } catch (Exception e) {
                Toast.makeText(this, getString(R.string.set_gpio_value_error), Toast.LENGTH_SHORT).show();
            }
        });

        // 获取 GPIO value
        findViewById(R.id.btn_get_gpio_value).setOnClickListener(v -> {
            try {
                int gpioNum = Integer.parseInt(mEtGpioNum.getText().toString());
                int gpioValue = Gpio.getGpioValue(gpioNum);
                Toast.makeText(this, "GPIO " + gpioNum + " value: " + gpioValue, Toast.LENGTH_SHORT).show();
            } catch (Exception e) {
                Toast.makeText(this, getString(R.string.get_gpio_value_error), Toast.LENGTH_SHORT).show();
            }
        });

        // 设置 GPIO direction
        findViewById(R.id.btn_set_gpio_direction).setOnClickListener(v -> {
            try {
                int gpioNum = Integer.parseInt(mEtGpioNum.getText().toString());
                String direction = mSpGpioDirection.getSelectedItem().toString();
                Gpio.setGpioDirection(gpioNum, direction);
            } catch (Exception e) {
                Toast.makeText(this,  getString(R.string.set_gpio_direction_error), Toast.LENGTH_SHORT).show();
            }
        });

        // 获取 GPIO direction
        findViewById(R.id.btn_get_gpio_direction).setOnClickListener(v -> {
            try {
                int gpioNum = Integer.parseInt(mEtGpioNum.getText().toString());
                String gpioDirection = Gpio.getGpioDirection(gpioNum);
                Toast.makeText(this, "GPIO " + gpioNum + " direction: " + gpioDirection, Toast.LENGTH_SHORT).show();
            } catch (Exception e) {
                Toast.makeText(this, getString(R.string.get_gpio_direction_error), Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void initState(boolean success, List<Integer> gpio) {
        Log.d(TAG, "initState: success = " + success + " gpio = " + gpio.toString());

        if (success && gpio.contains(pendingSetGpioNum)) {
            // 初始化完成，先设置方向为输出
            Gpio.setGpioDirection(pendingSetGpioNum, "out");

            // 设置GPIO值
            Gpio.setGpioValue(pendingSetGpioNum, pendingSetValue);

            // 清空待设置变量，避免重复执行
            pendingSetGpioNum = -1;
            pendingSetValue = -1;
        } else if (!success) {
            Toast.makeText(this, "GPIO 初始化失败", Toast.LENGTH_SHORT).show();
        }
    }
}