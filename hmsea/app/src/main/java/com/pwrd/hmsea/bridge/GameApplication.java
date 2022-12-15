package com.pwrd.hmsea.bridge;

import com.wpsdk.global.core.application.GlobalApplication;
import com.wpsdk.global.core.application.GlobalSdk;

public class GameApplication extends GlobalApplication {
    @Override
    public void onCreate() {
        super.onCreate();
        //todo add------1-------
        GlobalSdk.init(this);
    }
}


