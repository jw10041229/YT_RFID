package com.dwin.common_app.module;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import androidx.appcompat.widget.AppCompatEditText;
import androidx.appcompat.widget.AppCompatTextView;

import com.dwin.common_app.R;
import com.dwin.common_app.base.BaseFuncActivity;
import com.dwin.dw_android_11_sdk.NetworkInfoBean;
import com.dwin.dw_android_11_sdk.WiFiHandlerInternal;

import java.util.HashMap;
import java.util.Objects;

import android.text.Editable;
import android.text.TextWatcher;

public class NetworkActivity extends BaseFuncActivity implements View.OnClickListener {
    private static final String TAG = "NetworkActivity";

    private TextView tv_toolbar;
    private ImageView iv_back;
    private AppCompatEditText mEtWifiIp, mEtWifiMask, mEtWifiGateway, mEtWifiDns1,
            mEtWifiDns2, mEtEthIp, mEtEthMask, mEtEthGateway, mEtEthDns1, mEtEthDns2;
    private RadioGroup mRgInterfaceName, mRgWifiMode, mRgEthMode;
    private RadioButton mRbEth0, mRbEth1, mRbWifiDynamic, mRbWifiStatic, mRbEthDynamic, mRbEthStatic;
    private AppCompatTextView mTvWifiSave, mTvEthSave;

    public static final String DEFAULT_IP = "0.0.0.0";

    private String mWifiIp = "0.0.0.0";
    private String mWifiGateway = "0.0.0.0";
    private String mWifiMask = "0.0.0.0";
    private String mWifiDns1 = "0.0.0.0";
    private String mWifiDns2 = "0.0.0.0";

    private String mEth0Ip = "0.0.0.0";
    private String mEth0Gateway = "0.0.0.0";
    private String mEth0Mask = "0.0.0.0";
    private String mEth0Dns1 = "0.0.0.0";
    private String mEth0Dns2 = "0.0.0.0";

    private String mEth1Ip = "0.0.0.0";
    private String mEth1Gateway = "0.0.0.0";
    private String mEth1Mask = "0.0.0.0";
    private String mEth1Dns1 = "0.0.0.0";
    private String mEth1Dns2 = "0.0.0.0";

    public static final int ETH0 = 0;
    public static final int ETH1 = 1;
    private int mEthState = ETH0;

    public static final int DYNAMIC = 0;
    public static final int STATIC = 1;
    private int mWifiMode = DYNAMIC;
    private int mEthMode = DYNAMIC;

    private NetworkChangeReceiver networkChangeReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_network);

        tv_toolbar = findViewById(R.id.tv_toolbar);
        tv_toolbar.setText(getResources().getString(R.string.network));
        iv_back = findViewById(R.id.iv_back);

        initView();
        initData();
        initEvent();
    }

    private void initData() {
        updateNetworkData();

        boolean staticIp = WiFiHandlerInternal.isStaticIp(this);
        if(staticIp){
            mWifiMode = STATIC;
            updateWifiStaticIPUI();
        }else{
            mWifiMode = DYNAMIC;
            updateWifiDynamicIPUI();
        }

        updateEthNetworkUI();

        networkChangeReceiver = new NetworkChangeReceiver();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(ConnectivityManager.CONNECTIVITY_ACTION); // 监听网络连接状态变化
        registerReceiver(networkChangeReceiver, intentFilter);
    }

    private void initView() {
        mEtWifiIp = findViewById(R.id.et_wifi_ip);
        mEtWifiMask = findViewById(R.id.et_wifi_mask);
        mEtWifiGateway = findViewById(R.id.et_wifi_gateway);
        mEtWifiDns1 = findViewById(R.id.et_wifi_dns1);
        mEtWifiDns2 = findViewById(R.id.et_wifi_dns2);

        mEtEthIp = findViewById(R.id.et_eth_ip);
        mEtEthMask = findViewById(R.id.et_eth_mask);
        mEtEthGateway = findViewById(R.id.et_eth_gateway);
        mEtEthDns1 = findViewById(R.id.et_eth_dns1);
        mEtEthDns2 = findViewById(R.id.et_eth_dns2);

        mRgInterfaceName = findViewById(R.id.rg_interfacename);
        mRbEth0 = findViewById(R.id.rb_eth0);
        mRbEth1 = findViewById(R.id.rb_eth1);

        mRgWifiMode = findViewById(R.id.rg_wifi_mode);
        mRbWifiDynamic = findViewById(R.id.rb_wifi_dynamic);
        mRbWifiStatic = findViewById(R.id.rb_wifi_static);

        mRgEthMode = findViewById(R.id.rg_eth_mode);
        mRbEthDynamic = findViewById(R.id.rb_eth_dynamic);
        mRbEthStatic = findViewById(R.id.rb_eth_static);

        mTvWifiSave = findViewById(R.id.tv_wifi_save);
        mTvEthSave = findViewById(R.id.tv_eth_save);
    }

    private void initEvent() {
        iv_back.setOnClickListener(this);
        mTvWifiSave.setOnClickListener(this);
        mTvEthSave.setOnClickListener(this);

        addTextWatchers();
        mRgInterfaceName.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                if (checkedId == R.id.rb_eth0) {
                    mEthState = ETH0;
                    updateNetworkData();
                    updateEthNetworkUI();
                } else if (checkedId == R.id.rb_eth1) {
                    mEthState = ETH1;
                    updateNetworkData();
                    updateEthNetworkUI();
                }
            }
        });

        mRgWifiMode.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                if (checkedId == R.id.rb_wifi_dynamic) {
                    mWifiMode = DYNAMIC;
                    updateWifiDynamicIPUI();
                } else if (checkedId == R.id.rb_wifi_static) {
                    mWifiMode = STATIC;
                    updateWifiStaticIPUI();
                }
            }
        });

        mRgEthMode.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                if (checkedId == R.id.rb_eth_dynamic) {
                    mEthMode = DYNAMIC;
                    updateEthDynamicIPUI();
                } else if (checkedId == R.id.rb_eth_static) {
                    mEthMode = STATIC;
                    updateEthStaticIPUI();
                }
            }
        });
    }

    private void addTextWatchers() {
        TextWatcher watcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) { }

            @Override
            public void afterTextChanged(Editable s) {
                checkWifiStaticInputAndUpdateSaveBtn();
                checkEthStaticInputAndUpdateSaveBtn();
            }
        };

        mEtWifiIp.addTextChangedListener(watcher);
        mEtWifiMask.addTextChangedListener(watcher);
        mEtWifiGateway.addTextChangedListener(watcher);
        mEtWifiDns1.addTextChangedListener(watcher);
        mEtWifiDns2.addTextChangedListener(watcher);

        mEtEthIp.addTextChangedListener(watcher);
        mEtEthMask.addTextChangedListener(watcher);
        mEtEthGateway.addTextChangedListener(watcher);
        mEtEthDns1.addTextChangedListener(watcher);
        mEtEthDns2.addTextChangedListener(watcher);
    }

    private void checkWifiStaticInputAndUpdateSaveBtn() {
        if (mWifiMode == STATIC) {
            String ip = mEtWifiIp.getText().toString().trim();
            String mask = mEtWifiMask.getText().toString().trim();
            String gateway = mEtWifiGateway.getText().toString().trim();
            String dns1 = mEtWifiDns1.getText().toString().trim();
            String dns2 = mEtWifiDns2.getText().toString().trim();

            boolean allZeros = DEFAULT_IP.equals(ip) && DEFAULT_IP.equals(mask)
                    && DEFAULT_IP.equals(gateway) && DEFAULT_IP.equals(dns1) && DEFAULT_IP.equals(dns2);

            mTvWifiSave.setEnabled(!allZeros);
            mTvWifiSave.setAlpha(allZeros ? 0.5f : 1f);  // 设置半透明表示禁用，或者根据你项目风格调整
        } else {
            // 动态模式下保存按钮默认启用
            mTvWifiSave.setEnabled(true);
            mTvWifiSave.setAlpha(1f);
        }
    }

    // 判断以太网静态IP输入框是否全为"0.0.0.0"
    private void checkEthStaticInputAndUpdateSaveBtn() {
        if (mEthMode == STATIC) {
            String ip = mEtEthIp.getText().toString().trim();
            String mask = mEtEthMask.getText().toString().trim();
            String gateway = mEtEthGateway.getText().toString().trim();
            String dns1 = mEtEthDns1.getText().toString().trim();
            String dns2 = mEtEthDns2.getText().toString().trim();

            boolean allZeros = DEFAULT_IP.equals(ip) && DEFAULT_IP.equals(mask)
                    && DEFAULT_IP.equals(gateway) && DEFAULT_IP.equals(dns1) && DEFAULT_IP.equals(dns2);

            mTvEthSave.setEnabled(!allZeros);
            mTvEthSave.setAlpha(allZeros ? 0.5f : 1f);
        } else {
            mTvEthSave.setEnabled(true);
            mTvEthSave.setAlpha(1f);
        }
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.iv_back) {
            Log.d(TAG, "Back button clicked, finishing activity.");
            finish();
        } else if (v.getId() == R.id.tv_wifi_save) {
            if (mWifiMode == DYNAMIC) {
                mDWAndroidApi.setWiFiDynamicConfig();
            } else if (mWifiMode == STATIC) {
                mWifiIp= Objects.requireNonNull(mEtWifiIp.getText()).toString();
                mWifiMask= Objects.requireNonNull(mEtWifiMask.getText()).toString();
                mWifiGateway= Objects.requireNonNull(mEtWifiGateway.getText()).toString();
                mWifiDns1= Objects.requireNonNull(mEtWifiDns1.getText()).toString();
                mWifiDns2= Objects.requireNonNull(mEtWifiDns2.getText()).toString();
                mDWAndroidApi.setWiFiStaticConfig(mWifiIp, mWifiMask, mWifiGateway, mWifiDns1, mWifiDns2);
            }
        } else if (v.getId() == R.id.tv_eth_save) {
            if (mEthMode == DYNAMIC) {
                mDWAndroidApi.setEthernetDynamicConfig(mEthState == 1 ? "eth1" : "eth0");
            } else if (mEthMode == STATIC) {
                if (mEthState == 1) {
                    mEth1Ip= Objects.requireNonNull(mEtEthIp.getText()).toString();
                    mEth1Mask= Objects.requireNonNull(mEtEthMask.getText()).toString();
                    mEth1Gateway= Objects.requireNonNull(mEtEthGateway.getText()).toString();
                    mEth1Dns1= Objects.requireNonNull(mEtEthDns1.getText()).toString();
                    mEth1Dns2= Objects.requireNonNull(mEtEthDns2.getText()).toString();
                    mDWAndroidApi.setEthernetStaticConfig("eth1", mEth1Ip, mEth1Mask, mEth1Gateway, mEth1Dns1, mEth1Dns2);
                } else if (mEthState == 0) {
                    mEth0Ip= Objects.requireNonNull(mEtEthIp.getText()).toString();
                    mEth0Mask= Objects.requireNonNull(mEtEthMask.getText()).toString();
                    mEth0Gateway= Objects.requireNonNull(mEtEthGateway.getText()).toString();
                    mEth0Dns1= Objects.requireNonNull(mEtEthDns1.getText()).toString();
                    mEth0Dns2= Objects.requireNonNull(mEtEthDns2.getText()).toString();
                    mDWAndroidApi.setEthernetStaticConfig("eth0", mEth0Ip, mEth0Mask, mEth0Gateway, mEth0Dns1, mEth0Dns2);
                }
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(networkChangeReceiver);
    }

    public class NetworkChangeReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
            if (networkInfo != null && networkInfo.isConnected()) {
                // 检查网络类型，例如是WiFi还是移动数据
                if (networkInfo.getType() == ConnectivityManager.TYPE_WIFI) {
                    Log.d("daibin", "Connected to WiFi");
                } else if (networkInfo.getType() == ConnectivityManager.TYPE_ETHERNET) {
                    Log.d("daibin", "Connected to Ethernet");
                } else {
                    Log.d("daibin", "Connected to Mobile Data");
                }
                updateNetworkData();
                updateEthNetworkUI();
            } else {
                Log.d("daibin", "Disconnected");
                updateNetworkData();
                updateEthNetworkUI();
            }
        }
    }

    private void updateWifiDynamicIPUI() {

        mEtWifiIp.setEnabled(false);
        mEtWifiMask.setEnabled(false);
        mEtWifiGateway.setEnabled(false);
        mEtWifiDns1.setEnabled(false);
        mEtWifiDns2.setEnabled(false);
        mRbWifiDynamic.setChecked(true);
    }

    private void updateWifiStaticIPUI() {

        mEtWifiIp.setEnabled(true);
        mEtWifiMask.setEnabled(true);
        mEtWifiGateway.setEnabled(true);
        mEtWifiDns1.setEnabled(true);
        mEtWifiDns2.setEnabled(true);
        mRbWifiStatic.setChecked(true);
    }

    private void updateEthDynamicIPUI() {
        mRbEthDynamic.setChecked(true);

        mEtEthIp.setEnabled(false);
        mEtEthMask.setEnabled(false);
        mEtEthGateway.setEnabled(false);
        mEtEthDns1.setEnabled(false);
        mEtEthDns2.setEnabled(false);
    }

    private void updateEthStaticIPUI() {
        mRbEthStatic.setChecked(true);

        mEtEthIp.setEnabled(true);
        mEtEthMask.setEnabled(true);
        mEtEthGateway.setEnabled(true);
        mEtEthDns1.setEnabled(true);
        mEtEthDns2.setEnabled(true);
    }

    private void updateNetworkData() {
        HashMap<String, NetworkInfoBean> mNetWorkInfos = mDWAndroidApi.getNetWorkInfo();
        NetworkInfoBean wlan0Bean = mNetWorkInfos.get("wlan0");
        Log.d("daibin", "wlan0:" + (wlan0Bean == null ? "null" : wlan0Bean.toString()));
        if (wlan0Bean != null) {
            mWifiIp = wlan0Bean.getIp();
            mWifiGateway = wlan0Bean.getGateway();
            mWifiMask = wlan0Bean.getMask();
            mWifiDns1 = wlan0Bean.getDns1();
            mWifiDns2 = wlan0Bean.getDns2();
            mEtWifiIp.setText(TextUtils.isEmpty(mWifiIp) ? DEFAULT_IP : mWifiIp);
            mEtWifiMask.setText(TextUtils.isEmpty(mWifiMask) ? DEFAULT_IP : mWifiMask);
            mEtWifiGateway.setText(TextUtils.isEmpty(mWifiGateway) ? DEFAULT_IP : mWifiGateway);
            mEtWifiDns1.setText(TextUtils.isEmpty(mWifiDns1) ? DEFAULT_IP : mWifiDns1);
            mEtWifiDns2.setText(TextUtils.isEmpty(mWifiDns2) ? DEFAULT_IP : mWifiDns2);
        } else {
            mEtWifiIp.setText(DEFAULT_IP);
            mEtWifiMask.setText(DEFAULT_IP);
            mEtWifiGateway.setText(DEFAULT_IP);
            mEtWifiDns1.setText(DEFAULT_IP);
            mEtWifiDns2.setText(DEFAULT_IP);
        }

        NetworkInfoBean eth0Bean = mNetWorkInfos.get("eth0");
        Log.d("daibin", "eth0:" + (eth0Bean == null ? "null" : eth0Bean.toString()));
        if (eth0Bean != null) {
            mEth0Ip = eth0Bean.getIp();
            mEth0Gateway = eth0Bean.getGateway();
            mEth0Mask = eth0Bean.getMask();
            mEth0Dns1 = eth0Bean.getDns1();
            mEth0Dns2 = eth0Bean.getDns2();
        } else {
            mEth0Ip = DEFAULT_IP;
            mEth0Gateway = DEFAULT_IP;
            mEth0Mask = DEFAULT_IP;
            mEth0Dns1 = DEFAULT_IP;
            mEth0Dns2 = DEFAULT_IP;
        }

        NetworkInfoBean eth1Bean = mNetWorkInfos.get("eth1");
        Log.d("daibin", "eth1:" + (eth1Bean == null ? "null" : eth1Bean.toString()));
        if (eth1Bean != null) {
            mEth1Ip = eth1Bean.getIp();
            mEth1Gateway = eth1Bean.getGateway();
            mEth1Mask = eth1Bean.getMask();
            mEth1Dns1 = eth1Bean.getDns1();
            mEth1Dns2 = eth1Bean.getDns2();
        } else {
            mEth1Ip = DEFAULT_IP;
            mEth1Gateway = DEFAULT_IP;
            mEth1Mask = DEFAULT_IP;
            mEth1Dns1 = DEFAULT_IP;
            mEth1Dns2 = DEFAULT_IP;
        }
    }

    private void updateEthNetworkUI() {
        if (mEthState == ETH1) {
            mEtEthIp.setText(TextUtils.isEmpty(mEth1Ip) ? DEFAULT_IP : mEth1Ip);
            mEtEthMask.setText(TextUtils.isEmpty(mEth1Mask) ? DEFAULT_IP : mEth1Mask);
            mEtEthGateway.setText(TextUtils.isEmpty(mEth1Gateway) ? DEFAULT_IP : mEth1Gateway);
            mEtEthDns1.setText(TextUtils.isEmpty(mEth1Dns1) ? DEFAULT_IP : mEth1Dns1);
            mEtEthDns2.setText(TextUtils.isEmpty(mEth1Dns2) ? DEFAULT_IP : mEth1Dns2);

            mRbEth1.setChecked(true);
            mEthMode = Settings.Global.getInt(getContentResolver(), "ethernet1_mode", 0); // 0：动态，1：静态
            if (mEthMode == DYNAMIC) {
                updateEthDynamicIPUI();
            } else if (mEthMode == STATIC) {
                updateEthStaticIPUI();
            }
        } else {
            mEtEthIp.setText(TextUtils.isEmpty(mEth0Ip) ? DEFAULT_IP : mEth0Ip);
            mEtEthMask.setText(TextUtils.isEmpty(mEth0Mask) ? DEFAULT_IP : mEth0Mask);
            mEtEthGateway.setText(TextUtils.isEmpty(mEth0Gateway) ? DEFAULT_IP : mEth0Gateway);
            mEtEthDns1.setText(TextUtils.isEmpty(mEth0Dns1) ? DEFAULT_IP : mEth0Dns1);
            mEtEthDns2.setText(TextUtils.isEmpty(mEth0Dns2) ? DEFAULT_IP : mEth0Dns2);

            mRbEth0.setChecked(true);
            mEthMode = Settings.Global.getInt(getContentResolver(), "ethernet0_mode", 0); // 0：动态，1：静态
            if (mEthMode == DYNAMIC) {
                updateEthDynamicIPUI();
            } else if (mEthMode == STATIC) {
                updateEthStaticIPUI();
            }
        }
    }
}
