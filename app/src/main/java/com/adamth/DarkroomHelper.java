package com.adamth;

import android.app.Application;
import android.content.Context;

public class DarkroomHelper extends Application {
    private static Context context;

    public void onCreate() {
        super.onCreate();
        DarkroomHelper.context = getApplicationContext();
    }

    public static Context getAppContext() {
        return DarkroomHelper.context;
    }
}
