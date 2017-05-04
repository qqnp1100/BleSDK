package com.blue.blesdk;

import android.app.Application;
import android.util.Log;

import com.blue.androiddemo.blesdk.StwSDK;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * Created by macbook on 17/3/30.
 */

public class App extends Application{
    @Override
    public void onCreate() {
        super.onCreate();
        StwSDK.init(this);
        StwSDK.getInstance().setConfig(inputStream2String("bleconfig.json"));
    }

    String inputStream2String(String s){
        StringBuffer buffer = new StringBuffer();
        try {
            BufferedReader in = null;
            in = new BufferedReader(new InputStreamReader(getAssets().open(s)));

            String line = "";
            while ((line = in.readLine()) != null){
                buffer.append(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return buffer.toString();
    }
}
