package com.sangcomz.fishbun.core;

import android.app.Application;

/**
 * Created by sangc on 2016-01-03.
 */
public class CommonApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
//        LeakCanary.install(this);
    }
}
