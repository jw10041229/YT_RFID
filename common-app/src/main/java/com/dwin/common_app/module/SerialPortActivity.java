package com.dwin.common_app.module;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.dwin.common_app.R;
import com.dwin.serialportlibrary.SerialHandle;
import com.dwin.serialportlibrary.SerialInter;
import com.dwin.serialportlibrary.SerialPortFinder;

import java.util.Arrays;

public class SerialPortActivity extends AppCompatActivity implements SerialInter {

    private final static String TAG = "SerialPortActivity";

    private TextView tv_toolbar;
    private ImageView iv_back;
    private Spinner mSpSerialNum;
    private EditText mEtSendValue;
    private EditText mEtSerialInfo;

    private final StringBuilder builder = new StringBuilder();
    private SerialHandle serialHandle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_serial_port);
        tv_toolbar = findViewById(R.id.tv_toolbar);
        tv_toolbar.setText(getResources().getString(R.string.serial_port));
        iv_back = findViewById(R.id.iv_back);
        iv_back.setOnClickListener(v -> {
            finish();
        });

        mSpSerialNum = findViewById(R.id.sp_serial_num); // 替换为 Spinner
        mEtSendValue = findViewById(R.id.et_send_value);
        mEtSerialInfo = findViewById(R.id.et_serial_info);
        mEtSerialInfo.setMovementMethod(ScrollingMovementMethod.getInstance());
        mEtSerialInfo.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override public void afterTextChanged(Editable s) {
                mEtSerialInfo.setSelection(mEtSerialInfo.getText().length());
            }
        });

        serialHandle = new SerialHandle();
        serialHandle.addSerialInter(this);

        ((Button) findViewById(R.id.btn_open_serial_port)).setOnClickListener(v -> {
            if (mSpSerialNum.getSelectedItem() == null) {
                Toast.makeText(SerialPortActivity.this, "The serial port path was not obtained", Toast.LENGTH_SHORT).show();
                return;
            }
            String name = mSpSerialNum.getSelectedItem().toString();
            Log.d(TAG, "串口路径：" + name);
            Log.d(TAG, "准备打开串口，路径：" + name + ", 波特率：115200, 数据位：8, 停止位：1, 校验位：0");
            serialHandle.open(name, 115200, 8, 1, 0, true, 100);
        });


        ((Button) findViewById(R.id.btn_close_serial_port)).setOnClickListener(v -> {
            Log.d(TAG, "用户点击关闭串口按钮");
            serialHandle.close();
        });


        ((Button) findViewById(R.id.btn_send_value)).setOnClickListener(v -> {
            String str = mEtSendValue.getText().toString();
            if (TextUtils.isEmpty(str)) {
                return;
            }
            serialHandle.send(str.getBytes());
            builder.append("Send: ").append(str).append("\n");
            mEtSerialInfo.setText(builder.toString());
        });

        ((Button) findViewById(R.id.btn_clear_value)).setOnClickListener(v -> {
            builder.setLength(0);
            mEtSerialInfo.setText("");
        });

        // 加载串口路径并绑定到 Spinner
        new Thread(() -> {
            try {
                String[] driverList = new SerialPortFinder().getAllDevicesPath();
                Log.d(TAG, "加载到的串口设备列表：" + Arrays.toString(driverList));
                runOnUiThread(() -> {
                    ArrayAdapter<String> adapter = new ArrayAdapter<>(SerialPortActivity.this,
                            android.R.layout.simple_spinner_item, driverList);
                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    mSpSerialNum.setAdapter(adapter);
                });
            } catch (Exception e) {
                Log.e(TAG, "加载串口设备列表异常", e);
            }
        }).start();
    }

    @Override
    public void connectMsg(String path, boolean isSuccess) {
        Log.d(TAG, "connectMsg: path = " + path + " isSuccess = " + isSuccess);
        runOnUiThread(() -> {
            if (isSuccess) {
                Toast.makeText(SerialPortActivity.this, getString(R.string.open_serial_port) + path, Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(SerialPortActivity.this, getString(R.string.close_serial_port) + path, Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void readData(String path, byte[] bytes, int size) {
        runOnUiThread(() -> {
            String data = new String(bytes, 0, size);
            Log.d(TAG, "readData: path = " + path + " data = " + data);
            builder.append("Read: ").append(data).append("\n");
            mEtSerialInfo.setText(builder.toString());
        });
    }

    public String bytes2HexStr(byte[] src) {
        StringBuilder builder = new StringBuilder();
        if (src == null || src.length <= 0) return "";
        char[] buffer = new char[2];
        for (byte b : src) {
            buffer[0] = Character.forDigit((b >>> 4) & 0x0F, 16);
            buffer[1] = Character.forDigit(b & 0x0F, 16);
            builder.append(buffer);
        }
        return builder.toString().toUpperCase();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        serialHandle.close();
    }
}
