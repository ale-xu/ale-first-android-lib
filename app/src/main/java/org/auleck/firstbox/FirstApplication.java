package org.auleck.firstbox;

import android.app.Application;
import android.util.Log;

import com.tencent.mmkv.MMKV;

public class FirstApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        String rootDir = MMKV.initialize(this) ;
        Log.d("asdf", "onCreate: " + rootDir);
    }
}
