package com.dwin.common_app.module;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.dwin.common_app.R;
import com.dwin.common_app.bean.EpcBean;
import com.dwin.gpiolibrary.Gpio;
import com.dwin.gpiolibrary.GpioInitCallBack;
import com.dwin.serialportlibrary.SerialHandle;
import com.dwin.serialportlibrary.SerialInter;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class SerialPortExActivity extends AppCompatActivity implements SerialInter, GpioInitCallBack {

    private final static String TAG = "SerialPortActivity";
    private final static String RF_ADDR = "/dev/ttyS5";
    private final static String LORA_ADDR = "/dev/ttyS9";

    private final static int RF_GPIO_NUM = 5;
    private final static int LORA_GPIO_NUM = 137;
    private int pendingSetGpioNum = -1;
    private int pendingSetValue = -1;
    private TextView tv_toolbar;
    private ImageView iv_back;
    private Spinner mSpSerialNum;
    private EditText mEtSendValue;
    private EditText mEtSerialInfo;

    private final StringBuilder builder = new StringBuilder();
    private SerialHandle serialHandleRF;
    private SerialHandle serialHandleLora;
    ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor();
    int timeCount = 0;
    LinkedHashMap<String, LinkedList<EpcBean>> epcMap = new LinkedHashMap<>();
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
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                mEtSerialInfo.setSelection(mEtSerialInfo.getText().length());
            }
        });

        serialHandleRF = new SerialHandle();
        serialHandleRF.addSerialInter(this);
        serialHandleRF.open(RF_ADDR, 115200, 8, 1, 0, true, 100);

        serialHandleLora = new SerialHandle();
        serialHandleLora.addSerialInter(this);
        serialHandleLora.open(LORA_ADDR, 115200, 8, 1, 0, true, 100);


        ((Button) findViewById(R.id.btn_send_value)).setOnClickListener(v -> {
            String str = mEtSendValue.getText().toString();
            if (TextUtils.isEmpty(str)) {
                return;
            }
            serialHandleRF.send(str.getBytes());
            builder.append("Send: ").append(str).append("\n");
            mEtSerialInfo.setText(builder.toString());
        });

        executorService.scheduleAtFixedRate(() -> {
            timeCount ++;
            if (timeCount == 1) {
                openRF();
            }
            //前一分钟是RF模块开启
            if (timeCount == 2) {
                //RF模块采集一分钟后，关闭
                closeRF();
                //同时开启Lora模块，三分钟
                openLora();
                //往Lora写数据
                sendDataToLora();
            }
            //Lora模块，三分钟后停止，开启RF模块
            if (timeCount == 5) {
                closeLora();
                openRF();
                timeCount = 1;
            }
        },0L,60, TimeUnit.SECONDS);
    }

    @Override
    public void connectMsg(String path, boolean isSuccess) {
        Log.d(TAG, "connectMsg: path = " + path + " isSuccess = " + isSuccess);
        runOnUiThread(() -> {
            if (isSuccess) {
                Toast.makeText(SerialPortExActivity.this, getString(R.string.open_serial_port) + path, Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(SerialPortExActivity.this, getString(R.string.close_serial_port) + path, Toast.LENGTH_SHORT).show();
            }
        });
    }
    void sendDataToLora() {
        Iterator<Map.Entry<String, LinkedList<EpcBean>>> it =
                epcMap.entrySet().iterator();

        while (it.hasNext()) {
            Map.Entry<String, LinkedList<EpcBean>> entry = it.next();
            String epc = entry.getKey();
            LinkedList<EpcBean> list = entry.getValue();
            StringBuilder sb = new StringBuilder();
            for (EpcBean epcBean : list) {
                sb.append(epc)
                        .append("#")
                        .append(epcBean.getWd())
                        .append("\n");
            }
            serialHandleLora.send(sb.toString().getBytes());
            it.remove();
        }
    }
    @Override
    public void readData(String path, byte[] bytes, int size) {
        runOnUiThread(() -> {
            if (path.equals(RF_ADDR)) {
                String data = new String(bytes, 0, size);
                Log.d(TAG, "readData: path = " + path + " data = " + data);
                builder.append("Read: ").append(data).append("\n");
                mEtSerialInfo.setText(builder.toString());

                String epc = "epc" + data;
                String wd = "1111";

                EpcBean epcBean = new EpcBean();
                epcBean.setEpc(epc);
                epcBean.setWd(wd);
                makeEpcMap(epcBean);

            }
        });
    }
    private void makeEpcMap(EpcBean epcBean) {
        String epc = epcBean.getEpc();
        if (epcMap.containsKey(epc)) {
            LinkedList<EpcBean> epcBeans = epcMap.get(epc);
            if (null != epcBeans) {
                epcBeans.addLast(epcBean);
                if (epcBeans.size() > 64) {
                    epcBeans.removeFirst();
                }
            } else {
                LinkedList<EpcBean> epcList = new LinkedList<>();
                epcList.addLast(epcBean);
                epcMap.put(epc,epcList);
            }
        } else {
            LinkedList<EpcBean> epcBeans = new LinkedList<>();
            epcBeans.addLast(epcBean);
            epcMap.put(epc,epcBeans);
        }
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

    private void openRF() {
        setGpioValue(RF_GPIO_NUM,1);
        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
        }
        serialHandleRF.send("5A0001021000080000000101020006ED08".getBytes());
    }

    private void openLora() {
        setGpioValue(LORA_GPIO_NUM,1);
        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
        }
    }

    private void setGpioValue(int gpioNum, int gpioValue) {
        try {
            pendingSetGpioNum = gpioNum;
            pendingSetValue = gpioValue;
            int[] singleGpio = {gpioNum};
            Gpio.initGpio(singleGpio, 0, "in", this, this);
        } catch (Exception e) {
            Toast.makeText(this, getString(R.string.set_gpio_value_error), Toast.LENGTH_SHORT).show();
        }
    }
    private void closeRF() {
        setGpioValue(5,0);
        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
        }
    }

    private void closeLora() {
        setGpioValue(137,0);
        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
        }
    }

    @Override
    protected void onDestroy() {
        serialHandleRF.close();
        serialHandleLora.close();
        super.onDestroy();
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
