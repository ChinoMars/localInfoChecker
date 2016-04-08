package com.chinomars.localInfoChecker;

import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.*;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;

import java.security.PublicKey;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class LocalInfoChecker extends Activity {
    /**
     * Called when the activity is first created.
     */
    public static String SPP_UUID = "00001101-0000-1000-8000-00805F9B34FB";

    Button btnLocalBTMACAddr, btnPairedBTMACAddr;

    ArrayAdapter<String> adtDevices;
    List<String> lstDevices;
    BluetoothAdapter btAdapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        btnLocalBTMACAddr = (Button) findViewById(R.id.btn_btMACAddr);
        btnLocalBTMACAddr.setOnClickListener(new ClickEvent());
        btnPairedBTMACAddr = (Button) findViewById(R.id.btn_pairedBTMACAddr);
        btnPairedBTMACAddr.setOnClickListener(new ClickEvent());
        btnPairedBTMACAddr.setEnabled(false);

        btAdapter = BluetoothAdapter.getDefaultAdapter();
        if (btAdapter == null) {
            Toast.makeText(LocalInfoChecker.this, "never found bluetooth adapter", 1000).show();
        }

        IntentFilter intent = new IntentFilter();
        intent.addAction(BluetoothDevice.ACTION_FOUND);
        intent.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED);
        intent.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        registerReceiver(searchDevices, intent);


        addPairedDevice();

    }

    private void addPairedDevice() { // 增加配对设备
        if (btAdapter == null) return;
        Set<BluetoothDevice> pairedDevices = btAdapter.getBondedDevices();
        if (pairedDevices.size() > 0) {
            for (BluetoothDevice device : pairedDevices) {
                String str = device.getName() + "|" + device.getAddress();
                lstDevices.add(str);
                adtDevices.notifyDataSetChanged();
            }
        }
    }

    private BroadcastReceiver searchDevices = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if(action.equals(BluetoothDevice.ACTION_FOUND)) {
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                String str = device.getName() + "|" + device.getAddress();

                if (lstDevices.indexOf(str) == -1) { // 防止重复添加
                    lstDevices.add(str); // 获取设备名称和mac地址
                    btnPairedBTMACAddr.setEnabled(true);
                    Log.d("LOG", str);
                }
                if (lstDevices.indexOf("null|" + device.getAddress()) != -1)
                    lstDevices.set(lstDevices.indexOf("null|" + device.getAddress()), str);
                adtDevices.notifyDataSetChanged();

            }
        }
    };

    class ClickEvent implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            if (v == btnLocalBTMACAddr) {
                mShowLocalBTMACAddr();
            } else if (v == btnPairedBTMACAddr) {
//                if (!btAdapter.isEnabled()) {
//                    btAdapter.enable();
//                }
                mShowBTDevicesInfo();
            }

        }
    }

    private void mShowLocalBTMACAddr() {
        if (btAdapter != null) {
            String addr = btAdapter.getAddress();
            new AlertDialog.Builder(this)
                    .setIcon(android.R.drawable.ic_dialog_info)
                    .setTitle("Local BLUETOOTH MAC address")
                    .setMessage(addr)
                    .setPositiveButton("Yes",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.dismiss();
                                }
                            }
                    )
                    .show();
        }
    }

    private void mShowBTDevicesInfo() {
        try {
            LinearLayout btDeviceLayout = new LinearLayout(this);
            btDeviceLayout.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
            ListView lvBTDevice = new ListView(this);
            lvBTDevice.setFadingEdgeLength(0);

            adtDevices = new ArrayAdapter<>(LocalInfoChecker.this, android.R.layout.simple_list_item_1, lstDevices);
            lvBTDevice.setAdapter(adtDevices);

            btDeviceLayout.addView(lvBTDevice);

            final AlertDialog dialog = new AlertDialog.Builder(this)
                    .setTitle("蓝牙设备信息")
                    .setView(btDeviceLayout)
                    .setIcon(android.R.drawable.ic_dialog_info)
                    .setNegativeButton("取消", new DialogInterface.OnClickListener(){
                        @Override
                        public void onClick(DialogInterface dialog, int which){
                            dialog.cancel();
                        }
                    }).create();
//        dialog.setCanceledOnTouchOutside(false); // 取消点击对话框区域外的部分弹出
            dialog.show();
        } catch (Exception e) {
            Log.e("TAG", "list bluetooth devices info error");
        }

    }

}
