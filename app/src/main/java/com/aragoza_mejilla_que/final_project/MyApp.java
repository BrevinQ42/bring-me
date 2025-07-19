package com.aragoza_mejilla_que.final_project;

import android.app.Application;

import io.realm.Realm;
import io.realm.RealmConfiguration;

public class MyApp extends Application {

    public void onCreate()
    {
        super.onCreate();

        Realm.init(this);

        RealmConfiguration config = new RealmConfiguration.Builder()
                .allowWritesOnUiThread(true)
                .build();
        Realm.setDefaultConfiguration(config);
    }
}
