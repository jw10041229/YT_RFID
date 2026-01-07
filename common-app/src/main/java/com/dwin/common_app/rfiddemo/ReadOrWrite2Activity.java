package com.dwin.common_app.rfiddemo;

import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.dwin.common_app.R;
import com.dwin.common_app.rfiddemo.adapter.RecycleViewAdapter;
import com.dwin.common_app.rfiddemo.entity.TagInfo;
import com.dwin.common_app.rfiddemo.util.GlobalClient;
import com.dwin.common_app.rfiddemo.util.ToastUtils;
import com.dwin.gpiolibrary.Gpio;
import com.dwin.gpiolibrary.GpioInitCallBack;
import com.dwin.serialportlibrary.SerialHandle;
import com.dwin.serialportlibrary.SerialInter;
import com.gg.reader.api.dal.GClient;
import com.gg.reader.api.dal.HandlerGpiOver;
import com.gg.reader.api.dal.HandlerGpiStart;
import com.gg.reader.api.dal.HandlerTag6bLog;
import com.gg.reader.api.dal.HandlerTag6bOver;
import com.gg.reader.api.dal.HandlerTagEpcLog;
import com.gg.reader.api.dal.HandlerTagEpcOver;
import com.gg.reader.api.dal.HandlerTagGbLog;
import com.gg.reader.api.dal.HandlerTagGbOver;
import com.gg.reader.api.protocol.gx.EnumG;
import com.gg.reader.api.protocol.gx.LogAppGpiOver;
import com.gg.reader.api.protocol.gx.LogAppGpiStart;
import com.gg.reader.api.protocol.gx.LogBase6bInfo;
import com.gg.reader.api.protocol.gx.LogBase6bOver;
import com.gg.reader.api.protocol.gx.LogBaseEpcInfo;
import com.gg.reader.api.protocol.gx.LogBaseEpcOver;
import com.gg.reader.api.protocol.gx.LogBaseGbInfo;
import com.gg.reader.api.protocol.gx.LogBaseGbOver;
import com.gg.reader.api.protocol.gx.MsgBaseInventoryEpc;
import com.gg.reader.api.protocol.gx.MsgBaseStop;
import com.gg.reader.api.protocol.gx.ParamEpcReadTid;
import com.gg.reader.api.utils.ThreadPoolUtils;

import java.lang.reflect.Field;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class ReadOrWrite2Activity extends AppCompatActivity implements SerialInter, GpioInitCallBack {
    private final static String TAG = "ReadOrWriteActivity";
    // region 组件变量
    Button read;
    Button stop;
    Button clean;
    private final static String RF_ADDR = "/dev/ttyS5";
    private final static String RF_485_ADDR = "/dev/ttyS7";
    private final static String LORA_ADDR = "/dev/ttyS9";
    private Map<String, CheckBox> checkBoxMap = new LinkedHashMap<String, CheckBox>();//checkBox集合
    private boolean isFirst = true;
    RadioGroup way;

    TextView timeCount;

    TextView readCount;

    TextView tagCount;

    TextView speed;
    private SerialHandle serialHandleRF;
    private SerialHandle serialHandleLora;
    // endregion

    private GClient client = GlobalClient.getClient();
    private boolean isClient = true;
    private Map<String, TagInfo> tagInfoMap = new LinkedHashMap<String, TagInfo>();//去重数据源
    private List<TagInfo> tagInfoList = new ArrayList<TagInfo>();//适配器所需数据源
    private Long index = 1l;//索引
    private RecycleViewAdapter adapter;
    private ParamEpcReadTid tidParam = null;
    private boolean[] isChecked = new boolean[]{false};//标识读tid
    private Handler mHandler = new Handler();
    private Runnable r = null;
    private int time = 0;
    private boolean isReader = false;
    private SimpleDateFormat formatter = new SimpleDateFormat("HH:mm:ss");
    AlertDialog antAlertDialog;
    private boolean isInitAnt = true;
    private int pendingSetGpioNumRFPower = -1;
    private int pendingSetValueRFPower = -1;
    private int pendingSetGpioNumRFSerial = -1;
    private int pendingSetValueRFSerial = -1;
    private final static int RF_POWER_GPIO_NUM = 5;
    private final static int RF_SERIAL_GPIO_NUM = 6;
    int countAgg = 0;
    private final static int LORA_GPIO_NUM = 137;
    //todo android 4 以上tcp通信需要在子线程，以下示例为兼容高版本系统tcp通信 所有sendSynMsg方法都放在子线程中执行
    ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rfid_write_read);
        timeCount = findViewById(R.id.timeCount);
        readCount = findViewById(R.id.readCount);
        tagCount = findViewById(R.id.tagCount);
        way = findViewById(R.id.way);
        speed = findViewById(R.id.speed);
        read = findViewById(R.id.read);
        read.setOnClickListener(view -> {
            readCard();
        });
        stop = findViewById(R.id.stop);
        stop.setOnClickListener(view -> {
            stopRead();
        });
        clean = findViewById(R.id.clean);
        clean.setOnClickListener(view -> {
            cleanData();
        });
        initRecycleView();
        subHandler(client);
        connectSerial();
        connectLora();
        connect485Serial();
        executorService.scheduleAtFixedRate(() -> {
            countAgg ++;
            if (countAgg == 1) {
                openRFGPIO();
            }
            //前一分钟是RF模块开启
            if (countAgg == 2) {
                //RF模块采集一分钟后，关闭
                closeRF();
                //同时开启Lora模块，三分钟
                //openLora();
                //往Lora写数据
                //sendDataToLora();
            }
            //Lora模块，三分钟后停止，开启RF模块
            if (countAgg == 5) {
                //closeLora();
                openRFGPIO();
                countAgg = 1;
            }
        },1L,60, TimeUnit.SECONDS);
    }

    //初始化RecycleView
    public void initRecycleView() {
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        RecyclerView rv = findViewById(R.id.recycle);
        rv.setLayoutManager(layoutManager);
        rv.addItemDecoration(new DividerItemDecoration(this, 1));
        adapter = new RecycleViewAdapter(tagInfoList);
        rv.setAdapter(adapter);
    }

    void connectSerial() {
        String param = RF_ADDR+ ":115200";
        if (client.openAndroidSerial(param, 1000)) {
            // TODO: 2023/11/13 485添加一个地址位
//                    if (client.openAndroidRs485(param+":1", 1000)) {
            ToastUtils.showText("串口连接成功");
        } else {
            ToastUtils.showText("串口连接失败");
        }
    }
    void connect485Serial() {
        serialHandleRF = new SerialHandle();
        serialHandleRF.addSerialInter(this);
        serialHandleRF.open(RF_485_ADDR, 9600, 8, 1, 0, true, 100);
    }
    void connectLora() {
        serialHandleLora = new SerialHandle();
        serialHandleLora.addSerialInter(this);
        serialHandleLora.open(LORA_ADDR, 115200, 8, 1, 0, true, 100);
    }

    //订阅
    public void subHandler(GClient client) {
        client.onTagEpcLog = new HandlerTagEpcLog() {
            public void log(String readerName, LogBaseEpcInfo info) {
                if (0 == info.getResult()) {
                    synchronized (tagInfoList) {
                        pooled6cData(info);
                    }
                }
            }
        };
        client.onTagEpcOver = new HandlerTagEpcOver() {
            public void log(String readerName, LogBaseEpcOver info) {
                handlerStop.sendEmptyMessage(1);
            }
        };
        client.onTag6bLog = new HandlerTag6bLog() {
            public void log(String readerName, LogBase6bInfo info) {
                if (info.getResult() == 0) {
                    synchronized (tagInfoList) {
                        pooled6bData(info);
                    }
                }
            }
        };
        client.onTag6bOver = new HandlerTag6bOver() {
            public void log(String readerName, LogBase6bOver info) {
                handlerStop.sendEmptyMessage(1);
            }
        };
        client.onTagGbLog = new HandlerTagGbLog() {
            public void log(String readerName, LogBaseGbInfo info) {
                if (info.getResult() == 0) {
                    synchronized (tagInfoList) {
                        pooledGbData(info);
                    }
                }

            }
        };
        client.onTagGbOver = new HandlerTagGbOver() {
            public void log(String readerName, LogBaseGbOver info) {
                handlerStop.sendEmptyMessage(new Message().what = 1);
            }
        };
        client.onGpiOver = new HandlerGpiOver() {
            @Override
            public void log(String s, LogAppGpiOver logAppGpiOver) {
                System.out.println(logAppGpiOver);
            }
        };
        client.onGpiStart = new HandlerGpiStart() {
            @Override
            public void log(String s, LogAppGpiStart logAppGpiStart) {
                System.out.println(logAppGpiStart);
            }
        };
    }

    //读卡
    public void readCard() {
        if (isClient) {
            if (!isReader) {
                if (isFirst) {
                    initPane();
                    isFirst  = false;
                }

                {
                    MsgBaseInventoryEpc msg = new MsgBaseInventoryEpc();
                    msg.setAntennaEnable(EnumG.AntennaNo_1);
                    if (way.getCheckedRadioButtonId() == R.id.single) {
                        msg.setInventoryMode(EnumG.InventoryMode_Single);
                    } else {
                        msg.setInventoryMode(EnumG.InventoryMode_Inventory);
                    }
                    msg.setRfmicron(1);
                    if (isChecked[0]) {
                        tidParam = new ParamEpcReadTid();
                        tidParam.setMode(EnumG.ParamTidMode_Auto);
                        tidParam.setLen(6);
                        msg.setReadTid(tidParam);
                    } else {
                        tidParam = null;
                    }
                    ThreadPoolUtils.run(new Runnable() {
                        @Override
                        public void run() {
                            client.sendSynMsg(msg);
                            if (0x00 == msg.getRtCode()) {
                                computedSpeed();
                                ToastUtils.handlerText("开始读卡");
                                isReader = true;
                            } else {
                                ToastUtils.handlerText(msg.getRtMsg());
                                handlerStop.sendEmptyMessage(1);
                            }
                        }
                    });
                }
            } else {
                ToastUtils.showText("请先停止读卡");
            }
        } else {
            ToastUtils.showText("未连接");
        }
    }

    //停止
    public void stopRead() {
        if (isClient) {
            MsgBaseStop msgStop = new MsgBaseStop();
            ThreadPoolUtils.run(new Runnable() {
                @Override
                public void run() {
                    client.sendSynMsg(msgStop);
                    if (0x00 == msgStop.getRtCode()) {
                        isReader = false;
                        ToastUtils.handlerText("停止成功");
                    } else {
                        ToastUtils.handlerText("停止失败");
                    }
                }
            });
        } else {
            ToastUtils.showText("未连接");
        }
    }

    //清屏
    public void cleanData() {
        if (isClient) {
            initPane();
        } else {
            ToastUtils.showText("未连接");
        }
    }

    //去重6C
    //todo 此处只统计所有天线读取次数和 需要细分天线 自行根据属性 info.getAntId() 统计
    public Map<String, TagInfo> pooled6cData(LogBaseEpcInfo info) {
        if (tagInfoMap.containsKey(info.getTid() + info.getEpc())) {
            TagInfo tagInfo = tagInfoMap.get(info.getTid() + info.getEpc());
            Long count = tagInfoMap.get(info.getTid() + info.getEpc()).getCount();
            count++;
            tagInfo.setRssi(info.getRssi() + "");
            tagInfo.setCount(count);
            tagInfo.setCtesiusLtu31(info.getCtesiusLtu31());
            tagInfoMap.put(info.getTid() + info.getEpc(), tagInfo);
        } else {
            TagInfo tag = new TagInfo();
            tag.setIndex(index);
            tag.setType("6C");
            tag.setEpc(info.getEpc());
            tag.setCount(1L);
            tag.setTid(info.getTid());
            tag.setRssi(info.getRssi() + "");
            tag.setCtesiusLtu31(info.getCtesiusLtu31());
            tagInfoMap.put(info.getTid() + info.getEpc(), tag);
            index++;
        }
        return tagInfoMap;
    }

    //去重6B
    public Map<String, TagInfo> pooled6bData(LogBase6bInfo info) {
        if (tagInfoMap.containsKey(info.getTid())) {
            TagInfo tagInfo = tagInfoMap.get(info.getTid());
            Long count = tagInfoMap.get(info.getTid()).getCount();
            count++;
            tagInfo.setRssi(info.getRssi() + "");
            tagInfo.setCount(count);
            tagInfoMap.put(info.getTid(), tagInfo);
        } else {
            TagInfo tag = new TagInfo();
            tag.setIndex(index);
            tag.setType("6B");
            tag.setCount(1L);
            if (info.getTid() != null) {
                tag.setTid(info.getTid());
            }
            tag.setRssi(info.getRssi() + "");
            tagInfoMap.put(info.getTid(), tag);
            index++;
        }
        return tagInfoMap;
    }

    //去重GB
    public Map<String, TagInfo> pooledGbData(LogBaseGbInfo info) {
        if (tagInfoMap.containsKey(info.getTid() + info.getEpc())) {
            TagInfo tagInfo = tagInfoMap.get(info.getTid() + info.getEpc());
            Long count = tagInfoMap.get(info.getTid() + info.getEpc()).getCount();
            count++;
            tagInfo.setRssi(info.getRssi() + "");
            tagInfo.setCount(count);
            tagInfoMap.put(info.getTid() + info.getEpc(), tagInfo);
        } else {
            TagInfo tag = new TagInfo();
            tag.setIndex(index);
            tag.setType("GB");
            tag.setEpc(info.getEpc());
            tag.setCount(1L);
            tag.setTid(info.getTid());
            tag.setRssi(info.getRssi() + "");
            tagInfoMap.put(info.getTid() + info.getEpc(), tag);
            index++;
        }
        return tagInfoMap;
    }

    //程序此界面
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (isClient) {
            if (isReader) {
                ThreadPoolUtils.run(new Runnable() {
                    @Override
                    public void run() {
                        MsgBaseStop stop = new MsgBaseStop();
                        client.sendSynMsg(stop);
                        Log.e("onDestroy", stop.getRtMsg());
                    }
                });
            }
        }
        close485Serial();
        closeRF();
    }

    public void getTabHead() {
        AlertDialog dialog = new AlertDialog.Builder(this).setTitle("读取类型:").setMultiChoiceItems(new String[]{"读TID"}, isChecked, new DialogInterface.OnMultiChoiceClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which, boolean isChecked) {
                System.out.println(which + "--" + isChecked);
            }
        }).setPositiveButton("确定", null).setNegativeButton("取消", null).show();
        //修改“确认”、“取消”按钮的字体大小
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextSize(26);
        dialog.getButton(DialogInterface.BUTTON_NEGATIVE).setTextSize(26);
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(Color.rgb(0, 87, 75));
        dialog.getButton(DialogInterface.BUTTON_NEGATIVE).setTextColor(Color.rgb(0, 87, 75));
        try {
            Field mAlert = AlertDialog.class.getDeclaredField("mAlert");
            mAlert.setAccessible(true);
            Object mAlertController = mAlert.get(dialog);
            //通过反射修改title字体大小和颜色
            Field mTitle = mAlertController.getClass().getDeclaredField("mTitleView");
            mTitle.setAccessible(true);
            TextView mTitleView = (TextView) mTitle.get(mAlertController);
            mTitleView.setTextSize(30);
            mTitleView.setTextColor(Color.rgb(0, 87, 75));
            //通过反射修改message字体大小和颜色
//            Field mMessage = mAlertController.getClass().getDeclaredField("mMessageView");
//            mMessage.setAccessible(true);
//            TextView mMessageView = (TextView) mMessage.get(mAlertController);
//            mMessageView.setTextSize(28);
//            mTitleView.setTextColor(Color.rgb(0, 87, 75));
        } catch (IllegalAccessException e1) {
            e1.printStackTrace();
        } catch (NoSuchFieldException e2) {
            e2.printStackTrace();
        }
    }


    //获取天线
    private long getAnt() {
        StringBuffer buffer = new StringBuffer();
        for (CheckBox box : checkBoxMap.values())
            if (box.isChecked()) {
                buffer.append(1);
            } else {
                buffer.append(0);
            }
        return Long.valueOf(buffer.reverse().toString(), 2);
    }

    //一秒刷新计算
    private void computedSpeed() {
        Map<String, Long> rateMap = new Hashtable<String, Long>();
        r = new Runnable() {
            @Override
            public void run() {
                String toTime = secToTime(++time);
                timeCount.setText(toTime + " (s)");

                synchronized (tagInfoList) {
                    tagInfoList.clear();
                    tagInfoList.addAll(tagInfoMap.values());
                    adapter.notifyDataSetChanged();
                }
                long before = 0;
                Long afterValue = rateMap.get("after");
                if (null != afterValue) {
                    before = afterValue;
                }
                long reads = getReadCount(tagInfoList);
                readCount.setText(reads + "");
                tagCount.setText(tagInfoList.size() + "");
                rateMap.put("after", reads);
                if (reads >= before) {
                    long rateValue = reads - before;
                    speed.setText(rateValue + " (t/s)");
                }
                //每隔1s循环执行run方法
                mHandler.postDelayed(this, 1000);

            }
        };
        //延迟一秒执行
        mHandler.postDelayed(r, 1000);
    }

    //初始化面板
    private void initPane() {
        index = 1l;
        time = 0;
        tagInfoMap.clear();
        tagInfoList.clear();
        adapter.notifyDataSetChanged();
        tagCount.setText(0 + "");
        readCount.setText(0 + "");
        timeCount.setText("00:00:00" + " (s)");
        speed.setText(0 + " (t/s)");
        adapter.setThisPosition(null);
    }

    //更新面板
    private void upDataPane() {
        adapter.notifyDataSetChanged();
        readCount.setText(getReadCount(tagInfoList) + "");
        tagCount.setText(tagInfoList.size() + "");
    }

    //获取读取总次数
    private long getReadCount(List<TagInfo> tagInfoList) {
        long readCount = 0;
        for (int i = 0; i < tagInfoList.size(); i++) {
            readCount += tagInfoList.get(i).getCount();
        }
        return readCount;
    }

    //格式化时间
    public String secToTime(long time) {
        formatter.setTimeZone(TimeZone.getTimeZone("GMT+00:00"));
        time = time * 1000;
        String hms = formatter.format(time);
        return hms;
    }


    final Handler handlerStop = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 1:
                    mHandler.removeCallbacks(r);
                    upDataPane();
                    isReader = false;
                    break;
            }
            super.handleMessage(msg);
        }
    };

    private void setGpioValue(int gpioNum, int gpioValue) {
        switch (gpioNum) {
            case RF_POWER_GPIO_NUM:
                pendingSetGpioNumRFPower = gpioNum;
                pendingSetValueRFPower = gpioValue;
                break;
            case RF_SERIAL_GPIO_NUM:
                pendingSetGpioNumRFSerial = gpioNum;
                pendingSetValueRFSerial = gpioValue;
                break;
        }
        try {
            int[] singleGpio = {gpioNum};
            Gpio.initGpio(singleGpio, 0, "in", this, this);
        } catch (Exception e) {
            Toast.makeText(this, getString(R.string.set_gpio_value_error), Toast.LENGTH_SHORT).show();
        }
    }

    private void openRFGPIO() {
        openRFPower();
        openRFSerial();
        runOnUiThread(this::readCard);
    }

    private void openLora() {
        setGpioValue(LORA_GPIO_NUM,1);
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
        }
    }

    private void openRFPower() {
        setGpioValue(RF_POWER_GPIO_NUM,1);
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
        }
    }
    void sendDataToLora() {
        String data = buildEpcTempString(tagInfoList);
        serialHandleLora.send(data.getBytes());
    }
    public static String buildEpcTempString(List<TagInfo> list) {
        if (list == null || list.isEmpty()) {
            return "";
        }

        Map<String, StringBuilder> epcMap = new LinkedHashMap<>();

        for (TagInfo tag : list) {
            String epc = tag.getEpc();
            if (epc == null) {
                continue;
            }

            StringBuilder tempSb = epcMap.computeIfAbsent(epc, k -> new StringBuilder());

            if (tempSb.length() > 0) {
                tempSb.append("#");
            }
            tempSb.append(tag.getCtesiusLtu31());
        }

        StringBuilder result = new StringBuilder();

        epcMap.forEach((epc, temps) -> {
            if (result.length() > 0) {
                result.append("&");
            }
            result.append(epc).append("@").append(temps);
        });
        return result.toString();
    }
    private void openRFSerial() {
        setGpioValue(RF_SERIAL_GPIO_NUM,1);
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
        }
    }

    private void closeRF() {
        runOnUiThread(this::stopRead);
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
        }
        setGpioValue(5,0);
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
        }
    }

    private void close485Serial() {
        serialHandleRF.close();
    }

    @Override
    public void connectMsg(String path, boolean isSuccess) {
        Log.d(TAG, "connectMsg: path = " + path + " isSuccess = " + isSuccess);
        runOnUiThread(() -> {
            if (isSuccess) {
                Toast.makeText(ReadOrWrite2Activity.this, getString(R.string.open_serial_port) + path, Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(ReadOrWrite2Activity.this, getString(R.string.close_serial_port) + path, Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void readData(String s, byte[] bytes, int size) {
        String data = new String(bytes, 0, size);
        if (data.contains("heatbeat")) {
            serialHandleRF.send("OK".getBytes());
        }

    }

    @Override
    public void initState(boolean success, List<Integer> gpio) {
        Log.d(TAG, "initState: success = " + success + " gpio = " + gpio.toString());

        if (success) {
            if (gpio.contains(pendingSetGpioNumRFPower)) {
                // 初始化完成，先设置方向为输出
                Gpio.setGpioDirection(pendingSetGpioNumRFPower, "out");

                // 设置GPIO值
                Gpio.setGpioValue(pendingSetGpioNumRFPower, pendingSetValueRFPower);

                // 清空待设置变量，避免重复执行
                pendingSetGpioNumRFPower = -1;
                pendingSetValueRFPower = -1;
            }
            if (gpio.contains(pendingSetGpioNumRFSerial)) {
                // 初始化完成，先设置方向为输出
                Gpio.setGpioDirection(pendingSetGpioNumRFSerial, "out");

                // 设置GPIO值
                Gpio.setGpioValue(pendingSetGpioNumRFSerial, pendingSetValueRFSerial);

                // 清空待设置变量，避免重复执行
                pendingSetGpioNumRFSerial = -1;
                pendingSetValueRFSerial = -1;
            }
        } else {
            Toast.makeText(this, "GPIO 初始化失败", Toast.LENGTH_SHORT).show();
        }
    }
}
