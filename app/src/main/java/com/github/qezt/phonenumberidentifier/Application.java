package com.github.qezt.phonenumberidentifier;

import com.karumi.dexter.Dexter;

public class Application extends android.app.Application {
    private static Application application;

    @Override
    public void onCreate() {
        super.onCreate();
        application = this;
        Dexter.initialize(this);
    }

    public static Application instance() {
        return application;
    }
}