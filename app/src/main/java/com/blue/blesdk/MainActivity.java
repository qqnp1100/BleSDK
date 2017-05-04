package com.blue.blesdk;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.TextView;

import com.blue.androiddemo.blesdk.Atomizer;
import com.blue.androiddemo.blesdk.BleRx;
import com.blue.androiddemo.blesdk.StwSDK;
import com.blue.androiddemo.blesdk.TemperatureUnit;
import com.blue.androiddemo.blesdk.exception.BleException;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.InputStream;
import java.util.Arrays;
import java.util.UUID;

import android.util.Log;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;
import rx.Subscription;

public class MainActivity extends AppCompatActivity {

    @InjectView(R.id.tv_workmode)
    TextView tvWorkmode;
    @InjectView(R.id.tv_power)
    TextView tvPower;
    @InjectView(R.id.tv_tempmode)
    TextView tvTempmode;
    @InjectView(R.id.tv_temp)
    TextView tvTemp;
    @InjectView(R.id.tv_voltagae)
    TextView tvVoltagae;
    @InjectView(R.id.tv_linenum)
    TextView tvLinenum;
    @InjectView(R.id.tv_linedata)
    TextView tvLinedata;
    @InjectView(R.id.tv_atomizertype)
    TextView tvAtomizertype;
    @InjectView(R.id.tv_atomizer)
    TextView tvAtomizer;
    @InjectView(R.id.tv_battery)
    TextView tvBattery;
    @InjectView(R.id.tv_tcrmode)
    TextView tvTcrmode;
    @InjectView(R.id.tv_atomizerpower)
    TextView tvAtomizerpower;
    @InjectView(R.id.tv_atomizerrate)
    TextView tvAtomizerrate;
    @InjectView(R.id.sb_power)
    SeekBar sbPower;
    @InjectView(R.id.sb_temp)
    SeekBar sbTemp;
    @InjectView(R.id.sb_voltage)
    SeekBar sbVoltage;
    @InjectView(R.id.tv_version)
    TextView tvVersion;
    @InjectView(R.id.tv_name)
    EditText tvName;
    private Subscription dataSub;
    private Subscription stateSub;
    private int tprogress = 400;
    private TemperatureUnit unit = TemperatureUnit.Fahrenheit;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.inject(this);
        initView();
        dataSub = StwSDK.getInstance().bleData().subscribe(data -> {
            switch (data.getCommandType()) {
                case BATTERY:
                    tvBattery.setText(data.getBattery() + "%");
                    break;
                case POWER:
                    clean();
                    tvPower.setText(data.getPower() / 10.0 + "");
                    tvWorkmode.setText("POWER");
                    sbPower.setProgress(data.getPower() / 10);
                    break;
                case VOLTAGE:
                    clean();
                    tvVoltagae.setText(data.getVoltage() / 100.00 + "");
                    tvWorkmode.setText("VOLTAGE");
                    sbVoltage.setProgress(data.getVoltage());
                    break;
                case TEMPERATURE:
                    clean();
                    tvTempmode.setText(data.getTemperatureUnit() + "");
                    tvTemp.setText(data.getTemperature() + "");
                    tvWorkmode.setText("TEMPERATURE");
                    tvAtomizertype.setText(data.getAtomizerType() + "");
                    tprogress = data.getTemperatureUnit().getValue() == TemperatureUnit.Celsius.getValue() ? 200 : 400;
                    unit = data.getTemperatureUnit();
                    sbTemp.setMax(tprogress);
                    sbTemp.setProgress(data.getTemperature() - tprogress / 2);
                    break;
                case BYPASS:
                    clean();
                    tvWorkmode.setText("BYPASS");
                    break;
                case CUSTOM:
                    clean();
                    tvWorkmode.setText("CUSTOM");
                    tvLinenum.setText(data.getLineNum() + "");
                    StwSDK.getInstance().getLineData(data.getLineNum());//获取曲线数据
                    break;
                case TCR:
                    tvAtomizerrate.setText(data.getAtomizerRate() + "");
                    tvAtomizerpower.setText(data.getAtomizerPower() / 10.0 + "");
                    tvTcrmode.setText(data.getAtomizerType() + "");
                    break;
                case VERSION:
                    tvVersion.setText(data.getDeviceVersion() + "," + data.getDeviceVersion());
                    break;
                case LINE_DATA:
                    tvLinedata.setText(Arrays.toString(data.getLineData()));
                    break;
                case ATOMIZER:
                    tvAtomizer.setText(data.getAtomizerResistance() / 1000.000 + "");
                    break;
            }
        });
        stateSub = StwSDK.getInstance().bleState().subscribe(state -> {
            switch (state) {
                case CONNECTED:
                    break;
                case DISCONNECT:
                    finish();
                    break;
                case CONNECTTING:
                    break;
            }
        });
        StwSDK.getInstance().getWorkMode();

    }

    private void initView() {
        sbPower.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                clean();
                try {
                    StwSDK.getInstance().setModePower(seekBar.getProgress() * 10);
                } catch (BleException e) {
                    e.printStackTrace();
                }
                tvPower.setText(seekBar.getProgress() + "");
                tvWorkmode.setText("POWER");
            }
        });
        sbVoltage.setMax(112);
        sbVoltage.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                clean();
                try {
                    StwSDK.getInstance().setModeVoltage(seekBar.getProgress());
                } catch (BleException e) {
                    e.printStackTrace();
                }
                tvVoltagae.setText(seekBar.getProgress() / 100.00 + "");
                tvWorkmode.setText("VOLTAGE");
            }
        });

        sbTemp.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                clean();
                tvWorkmode.setText("TEMPERATURE");
                try {
                    StwSDK.getInstance().setModeTemp(unit, seekBar.getProgress() + tprogress / 2, Atomizer.Ni, true);
                } catch (BleException e) {
                    e.printStackTrace();
                }
                tvTemp.setText((seekBar.getProgress() + tprogress / 2) + "");
                tvTempmode.setText(unit + "");
            }
        });
    }

    private void clean() {
        ViewGroup group = (ViewGroup) ((ViewGroup) findViewById(android.R.id.content)).getChildAt(0);
        clean(group);
    }

    private void clean(ViewGroup viewGroup) {
        if (viewGroup == null) {
            return;
        }
        int count = viewGroup.getChildCount();
        for (int i = 0; i < count; i++) {
            View view = viewGroup.getChildAt(i);
            if (view instanceof TextView) { // 若是Button记录下
                TextView newDtv = (TextView) view;
                if (newDtv.getTag() != null) {
                    newDtv.setText("");
                }
            } else if (view instanceof ViewGroup) {
                // 若是布局控件（LinearLayout或RelativeLayout）,继续查询子View
                this.clean((ViewGroup) view);
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        dataSub.unsubscribe();
        stateSub.unsubscribe();
        StwSDK.getInstance().disConnect();
    }

    @OnClick({R.id.bt_b, R.id.bt_m1, R.id.bt_ni, R.id.bt_name, R.id.bt_version, R.id.bt_updata, R.id.bt_cupdata})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.bt_b:
                try {
                    StwSDK.getInstance().getBattery();
                } catch (BleException e) {
                    e.printStackTrace();
                }
                break;
            case R.id.bt_m1:
                StwSDK.getInstance().getTcr(Atomizer.M1);
                break;
            case R.id.bt_ni:
                StwSDK.getInstance().getTcr(Atomizer.Ni);
                break;
            case R.id.bt_name:
                try {
                    StwSDK.getInstance().setName(tvName.getText().toString());
                } catch (BleException e) {
                    e.printStackTrace();
                }
                break;
            case R.id.bt_version:
                try {
                    StwSDK.getInstance().getDeviceData();
                } catch (BleException e) {
                    e.printStackTrace();
                }
                break;
            case R.id.bt_updata:
                try {
                    InputStream open = getAssets().open("7a11a816559dbfc11037b8cbdf8b916f.bin");
                    ByteArrayOutputStream outputStream = new ByteArrayOutputStream(1000);
                    byte[] b = new byte[1000];
                    int n;
                    while ((n = open.read(b)) != -1) {
                        outputStream.write(b, 0, n);
                    }
                    open.close();
                    outputStream.close();
                    StwSDK.getInstance().updata(outputStream.toByteArray(), new StwSDK.OnUpdataLisener() {
                        @Override
                        public void onUpdata(int i) {
                            Log.e("xxx", "updata=" + i);
                        }

                        @Override
                        public void onSuccess() {
                            Log.e("xxx", "onSuccess=");
                        }

                        @Override
                        public void onFile(int i) {
                            Log.e("xxx", "onFile");
                        }
                    });
                } catch (Exception e) {

                }
                break;
            case R.id.bt_cupdata:
                StwSDK.getInstance().updataCancel();
            break;

        }
    }
}
