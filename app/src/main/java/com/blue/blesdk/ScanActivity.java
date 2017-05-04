package com.blue.blesdk;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.blue.androiddemo.blesdk.BleRx;
import com.blue.androiddemo.blesdk.StwSDK;
import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.chad.library.adapter.base.listener.OnItemChildClickListener;
import com.chad.library.adapter.base.listener.OnItemClickListener;

import java.util.ArrayList;
import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;
import rx.Subscription;

public class ScanActivity extends AppCompatActivity {

    @InjectView(R.id.listview)
    RecyclerView listview;
    @InjectView(R.id.scan)
    Button scan;
    private Subscription scanSub;
    private List<Device> list;
    private MyAdapter adpter;
    private Subscription stateSub;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scan);
        ButterKnife.inject(this);
        list = new ArrayList<>();
        listview.setLayoutManager(new LinearLayoutManager(this));
        listview.setAdapter(adpter = new MyAdapter(R.layout.device, list));
        listview.addOnItemTouchListener(new OnItemClickListener() {
            @Override
            public void SimpleOnItemClick(BaseQuickAdapter baseQuickAdapter, View view, int i) {
                if (scanSub != null && !scanSub.isUnsubscribed())
                    scanSub.unsubscribe();
                scan.setText("扫描");
                StwSDK.getInstance().connect(list.get(i).mac);
            }

        });
         stateSub = StwSDK.getInstance().bleState().subscribe(state -> {
            switch (state) {
                case CONNECTED:
                    startActivity(new Intent(this, MainActivity.class));

                    break;
                case DISCONNECT:
                    break;
                case CONNECTTING:
                    break;
            }
        });
    }


    @OnClick({R.id.scan})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.scan:
                if (Build.VERSION.SDK_INT >= 23) {
                    if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, 10);
                        return;
                    }
                }
                if (scanSub != null && !scanSub.isUnsubscribed()) {
                    scanSub.unsubscribe();
                    scan.setText("扫描");
                    return;
                }
                list.clear();
                adpter.notifyDataSetChanged();
                scanSub = StwSDK.getInstance().scan().subscribe(device -> {
                    for (Device device1 : list) {
                        if (device1.mac.equals(device.getBleDevice().getMacAddress()))
                            return;
                    }
                    list.add(new Device(device.getBleDevice().getName(), device.getBleDevice().getMacAddress()));
                    adpter.notifyDataSetChanged();
                });
                scan.setText("停止");
                break;
        }
    }

    public class MyAdapter extends BaseQuickAdapter<Device, BaseViewHolder> {

        public MyAdapter(int layoutResId, List<Device> data) {
            super(layoutResId, data);
        }

        @Override
        protected void convert(BaseViewHolder baseViewHolder, Device device) {
            baseViewHolder.setText(R.id.name, device.name);
        }
    }

    public class Device {
        public String name;
        public String mac;

        public Device(String name, String mac) {
            this.name = name;
            this.mac = mac;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (scanSub != null && !scanSub.isUnsubscribed())
            scanSub.unsubscribe();
        stateSub.unsubscribe();
    }
}
