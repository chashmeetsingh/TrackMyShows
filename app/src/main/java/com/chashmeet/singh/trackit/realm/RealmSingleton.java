package com.chashmeet.singh.trackit.realm;

import io.realm.Realm;
import io.realm.RealmConfiguration;
import com.chashmeet.singh.trackit.misc.App;

public class RealmSingleton {

    private static final int REALM_SCHEMA_VERSION = 5;
    static RealmSingleton mInstance = null;
    private Realm realm;

    private RealmSingleton() {
        try {
            realm = Realm.getDefaultInstance();
        } catch (Exception e) {
            RealmConfiguration config = new RealmConfiguration.Builder(App.getAppContext())
                    .name("tmsdb.realm")
                    .schemaVersion(REALM_SCHEMA_VERSION)
                    .migration(new MyRealmMigration())
                    .build();
            Realm.setDefaultConfiguration(config);
            realm = Realm.getDefaultInstance();
        }
    }

    public static RealmSingleton getInstance() {
        if (mInstance == null) {
            mInstance = new RealmSingleton();
        }
        return mInstance;
    }

    public Realm getRealm() {
        return realm;
    }
}
