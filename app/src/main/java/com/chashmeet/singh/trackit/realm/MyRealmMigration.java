package com.chashmeet.singh.trackit.realm;

import android.content.Context;
import android.content.SharedPreferences;

import io.realm.DynamicRealm;
import io.realm.DynamicRealmObject;
import io.realm.RealmMigration;
import io.realm.RealmObjectSchema;
import io.realm.RealmSchema;
import com.chashmeet.singh.trackit.misc.App;

class MyRealmMigration implements RealmMigration {

    @Override
    public void migrate(final DynamicRealm realm, long oldVersion, long newVersion) {

        RealmSchema schema = realm.getSchema();

        if (oldVersion == 1) {
            schema.create("RealmSync")
                    .addField("itemID", int.class)
                    .addField("itemType", int.class)
                    .addField("itemState", int.class);

            oldVersion++;
        }

        if (oldVersion == 2) {

            schema.get("RealmShow")
                    .addField("imdbID", String.class)
                    .addField("timeOffset", long.class)
                    .addField("tmdbID", int.class)
                    .addField("userRating", double.class)
                    .addField("hidden", boolean.class)
                    .removeField("actors")
                    .transform(new RealmObjectSchema.Function() {
                        @Override
                        public void apply(DynamicRealmObject obj) {
                            obj.set("imdbID", "");
                            obj.set("tmdbID", 0);
                            obj.set("timeOffset", 0);
                            obj.set("userRating", 0.0);
                            obj.set("hidden", false);
                        }
                    });

            schema.get("RealmEpisode")
                    .addField("traktID", int.class)
                    .addField("tmdbID", int.class)
                    .addField("userRating", double.class)
                    .removeField("writer")
                    .transform(new RealmObjectSchema.Function() {
                        @Override
                        public void apply(DynamicRealmObject obj) {
                            obj.set("traktID", 0);
                            obj.set("tmdbID", 0);
                            obj.set("userRating", 0.0);
                        }
                    });

            SharedPreferences sharedPref = App.getAppContext().getSharedPreferences("app_preferences", Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPref.edit();
            editor.putBoolean("update_db", true);
            editor.commit();

            oldVersion++;
        }

        if (oldVersion == 3) {
            schema.get("RealmEpisode")
                    .addField("collectedAt", String.class)
                    .addField("watchedAt", String.class)
                    .transform(new RealmObjectSchema.Function() {
                        @Override
                        public void apply(DynamicRealmObject obj) {
                            obj.set("collectedAt", "");
                            obj.set("watchedAt", "");
                        }
                    });

            schema.get("RealmSync")
                    .addField("collectedAt", String.class)
                    .addField("watchedAt", String.class)
                    .transform(new RealmObjectSchema.Function() {
                        @Override
                        public void apply(DynamicRealmObject obj) {
                            obj.set("collectedAt", "");
                            obj.set("watchedAt", "");
                        }
                    });

            oldVersion++;
        }

        if (oldVersion == 4) {
            schema.get("RealmShow")
                    .addField("lastUpdated", long.class)
                    .transform(new RealmObjectSchema.Function() {
                        @Override
                        public void apply(DynamicRealmObject obj) {
                            obj.set("lastUpdated", 0);
                        }
                    });
        }
    }
}
