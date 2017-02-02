package com.chashmeet.singh.trackit.activity;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SwitchCompat;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.util.List;

import io.realm.Realm;
import io.realm.RealmList;
import io.realm.RealmResults;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import com.chashmeet.singh.trackit.R;
import com.chashmeet.singh.trackit.api.trakt.TraktV2;
import com.chashmeet.singh.trackit.api.trakt.entities.AccessToken;
import com.chashmeet.singh.trackit.api.trakt.entities.BaseShow;
import com.chashmeet.singh.trackit.api.trakt.enums.Extended;
import com.chashmeet.singh.trackit.api.trakt.enums.IdType;
import com.chashmeet.singh.trackit.helper.DataHelper;
import com.chashmeet.singh.trackit.helper.DatabaseHelper;
import com.chashmeet.singh.trackit.misc.App;
import com.chashmeet.singh.trackit.misc.Constants;
import com.chashmeet.singh.trackit.realm.RealmBackupRestore;
import com.chashmeet.singh.trackit.realm.RealmNotification;
import com.chashmeet.singh.trackit.realm.RealmSetNotification;
import com.chashmeet.singh.trackit.realm.RealmShow;
import com.chashmeet.singh.trackit.realm.RealmSingleton;
import com.chashmeet.singh.trackit.utility.MyFilePickerActivity;
import com.chashmeet.singh.trackit.utility.TraktClient;

import static com.chashmeet.singh.trackit.utility.TraktClient.CLIENT_DATA_TYPE;

public class SettingsActivity extends AppCompatActivity {

    private SwitchCompat notificationSwitch;
    private TextView tvTrakt;
    private TextView tvResetTutorial;
    private TextView tvBackup;
    private TextView tvRestore;
    private TextView tvAddTraktShows;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        notificationSwitch = (SwitchCompat) findViewById(R.id.notification_switch);
        tvTrakt = (TextView) findViewById(R.id.tv_trakt);
        tvResetTutorial = (TextView) findViewById(R.id.tv_reset_tutorial);
        tvBackup = (TextView) findViewById(R.id.tv_backup);
        tvRestore = (TextView) findViewById(R.id.tv_restore);
        tvAddTraktShows = (TextView) findViewById(R.id.tv_trakt_add_all_shows);

        updateViews();
        setViewListeners();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case Constants.LOGIN_REQUEST_CODE:
                if (resultCode == Activity.RESULT_OK) {
                    tvTrakt.setText(R.string.logout);
                    tvAddTraktShows.setVisibility(View.VISIBLE);
                    TraktClient.firstSync();
                }
                break;
            case Constants.FILE_CODE_BACKUP:
                if (resultCode == Activity.RESULT_OK) {
                    Uri uri = data.getData();
                    RealmBackupRestore backup = new RealmBackupRestore();
                    backup.backup(uri.getPath());
                    //Log.d("SettingsActivity", uri.getPath());
                }
                break;
            case Constants.FILE_CODE_RESTORE:
                if (resultCode == Activity.RESULT_OK) {
                    Uri uri = data.getData();
                    RealmBackupRestore restore = new RealmBackupRestore();
                    restore.restore(uri.getPath());
                    Log.d("SettingsActivity", uri.getPath());
                }
                break;
        }
    }

    private void updateViews() {
        if (!DataHelper.TRAKT_ACCESS_TOKEN.equals("")) {
            tvTrakt.setText(R.string.logout);
            tvAddTraktShows.setVisibility(View.VISIBLE);
        }
        notificationSwitch.setChecked(DataHelper.SHOW_NOTIFICATION);
    }

    private void setViewListeners() {
        notificationSwitch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DataHelper.setNotificationPreference(SettingsActivity.this,
                        notificationSwitch.isChecked());
                Log.d("SettingsActivity", String.valueOf(notificationSwitch.isChecked()));
                if (!notificationSwitch.isChecked()) {
                    Realm realm = RealmSingleton.getInstance().getRealm();
                    realm.executeTransactionAsync(new Realm.Transaction() {
                        @Override
                        public void execute(Realm realm) {
                            realm.delete(RealmNotification.class);

                            RealmResults<RealmSetNotification> setNotifications = realm
                                    .where(RealmSetNotification.class)
                                    .findAll();

                            for (int i = 0; i < setNotifications.size(); i++) {
                                Intent notificationIntent = new Intent("android.media.action.DISPLAY_NOTIFICATION");
                                AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
                                notificationIntent.addCategory("android.intent.category.DEFAULT");
                                PendingIntent broadcast = PendingIntent.getBroadcast(SettingsActivity.this,
                                        setNotifications.get(i).getNotificationID(),
                                        notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);
                                alarmManager.cancel(broadcast);
                            }

                            realm.delete(RealmSetNotification.class);
                        }
                    });
                }
            }
        });

        tvTrakt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (DataHelper.TRAKT_ACCESS_TOKEN.equals("")) {
                    startActivityForResult(new Intent(SettingsActivity.this, LoginActivity.class),
                            Constants.LOGIN_REQUEST_CODE);
                } else {
                    traktLogout();
                }
            }
        });

        tvResetTutorial.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SharedPreferences pref = SettingsActivity.this.getSharedPreferences(
                        "material_showcaseview_prefs", Context.MODE_PRIVATE);
                pref.edit().clear().apply();
                Toast.makeText(SettingsActivity.this, "Tutorial Reset!", Toast.LENGTH_SHORT)
                        .show();
            }
        });

        tvAddTraktShows.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                addTraktShows();
            }
        });

        tvBackup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(SettingsActivity.this, MyFilePickerActivity.class);
                i.putExtra(MyFilePickerActivity.EXTRA_ALLOW_MULTIPLE, false);
                i.putExtra(MyFilePickerActivity.EXTRA_ALLOW_CREATE_DIR, true);
                i.putExtra(MyFilePickerActivity.EXTRA_MODE, MyFilePickerActivity.MODE_DIR);
                i.putExtra(MyFilePickerActivity.EXTRA_START_PATH, Environment.getExternalStorageDirectory().getPath());

                startActivityForResult(i, Constants.FILE_CODE_BACKUP);
            }
        });

        tvRestore.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(SettingsActivity.this, MyFilePickerActivity.class);
                i.putExtra(MyFilePickerActivity.EXTRA_ALLOW_MULTIPLE, false);
                i.putExtra(MyFilePickerActivity.EXTRA_ALLOW_CREATE_DIR, false);
                i.putExtra(MyFilePickerActivity.EXTRA_MODE, MyFilePickerActivity.MODE_FILE);
                i.putExtra(MyFilePickerActivity.EXTRA_START_PATH, Environment.getExternalStorageDirectory().getPath());

                startActivityForResult(i, Constants.FILE_CODE_RESTORE);
            }
        });
    }

    private void addTraktShows() {

        final ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage(getString(R.string.fetching_data_from_trakt));
        progressDialog.setIndeterminate(false);
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressDialog.setCancelable(false);
        progressDialog.show();

        Intent in = new Intent(MainActivity.ACTION);
        in.putExtra("data_type", CLIENT_DATA_TYPE);
        in.putExtra("show", true);
        LocalBroadcastManager.getInstance(App.getInstance()).sendBroadcast(in);

        TraktV2 traktV2 = new TraktV2(DataHelper.TRAKT_CLIENT_ID);
        traktV2.accessToken(DataHelper.TRAKT_ACCESS_TOKEN);
        Call<List<BaseShow>> call = traktV2.sync().watchedShows(Extended.DEFAULT_MIN);

        call.enqueue(new Callback<List<BaseShow>>() {
            @Override
            public void onResponse(Call<List<BaseShow>> call, final Response<List<BaseShow>> response) {
                if (response.code() == 200 && response.body() != null) {
                    Realm realm = RealmSingleton.getInstance().getRealm();
                    realm.executeTransaction(new Realm.Transaction() {
                        @Override
                        public void execute(Realm realm) {

                            RealmList<RealmShow> showList = new RealmList<>();
                            DatabaseHelper databaseHelper = new DatabaseHelper();

                            for (BaseShow show : response.body()) {

                                RealmShow realmShow = realm.where(RealmShow.class)
                                        .equalTo("showID", show.show.ids.tvdb)
                                        .findFirst();

                                if (realmShow == null) {
                                    realmShow = new RealmShow();
                                    realmShow.setShowID(show.show.ids.tvdb);
                                    showList.add(realmShow);
                                }
                            }

                            if (showList.isEmpty()) {
                                Intent in = new Intent(MainActivity.ACTION);
                                in.putExtra("data_type", CLIENT_DATA_TYPE);
                                in.putExtra("show", false);
                                LocalBroadcastManager.getInstance(App.getInstance()).sendBroadcast(in);
                                Toast.makeText(getApplicationContext(), "No shows to add",
                                        Toast.LENGTH_LONG).show();
                            } else {
                                databaseHelper.updateDB(IdType.TVDB, showList, new DatabaseHelper.onUpdateListener() {
                                    @Override
                                    public void onResults(boolean result) {
                                        Intent in = new Intent(MainActivity.ACTION);
                                        in.putExtra("data_type", CLIENT_DATA_TYPE);
                                        in.putExtra("show", false);
                                        LocalBroadcastManager.getInstance(App.getInstance()).sendBroadcast(in);
                                        Toast.makeText(getApplicationContext(), "All shows added",
                                                Toast.LENGTH_LONG).show();
                                    }
                                });
                                SettingsActivity.super.onBackPressed();
                                Toast.makeText(getApplicationContext(), "Shows will appear in your list shortly",
                                        Toast.LENGTH_LONG).show();
                                Intent in = new Intent(MainActivity.ACTION);
                                in.putExtra("data_type", CLIENT_DATA_TYPE);
                                in.putExtra("show", true);
                                LocalBroadcastManager.getInstance(App.getInstance()).sendBroadcast(in);
                            }
                        }
                    });
                } else {
                    Intent in = new Intent(MainActivity.ACTION);
                    in.putExtra("data_type", CLIENT_DATA_TYPE);
                    in.putExtra("show", false);
                    LocalBroadcastManager.getInstance(App.getInstance()).sendBroadcast(in);
                    Toast.makeText(getApplicationContext(), "Could not connect to Trakt",
                            Toast.LENGTH_LONG).show();
                }
                progressDialog.dismiss();
            }

            @Override
            public void onFailure(Call<List<BaseShow>> call, Throwable t) {
                progressDialog.dismiss();
                Intent in = new Intent(MainActivity.ACTION);
                in.putExtra("data_type", CLIENT_DATA_TYPE);
                in.putExtra("show", false);
                LocalBroadcastManager.getInstance(App.getInstance()).sendBroadcast(in);
                Toast.makeText(getApplicationContext(), "Could not connect to Trakt",
                        Toast.LENGTH_LONG).show();
            }
        });
    }

    private void traktLogout() {
        final ProgressDialog pDialog = new ProgressDialog(SettingsActivity.this);
        pDialog.setMessage("Logging out...");
        pDialog.setIndeterminate(true);
        pDialog.setCancelable(false);
        pDialog.show();

        final TraktV2 traktV2 = new TraktV2(DataHelper.TRAKT_CLIENT_ID);
        new Thread() {
            @Override
            public void run() {
                try {
                    retrofit2.Response<AccessToken> response = traktV2.revokeAccessToken(DataHelper.TRAKT_ACCESS_TOKEN);
                    if (response.code() == 200) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                pDialog.dismiss();
                                tvTrakt.setText(R.string.login_to_trakt);
                                tvAddTraktShows.setVisibility(View.GONE);
                                Toast.makeText(getApplicationContext(), "Logged out successfully",
                                        Toast.LENGTH_SHORT).show();
                            }
                        });
                        DataHelper.removeTraktData(SettingsActivity.this);
                    } else {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                String errorText = "Connection timed out";
                                Toast.makeText(getApplicationContext(), "Error logging out. " + errorText,
                                        Toast.LENGTH_LONG).show();
                            }
                        });
                    }
                } catch (IOException e) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(getApplicationContext(), "Could not connect to Trakt",
                                    Toast.LENGTH_LONG).show();
                        }
                    });
                }
            }
        }.start();
    }
}
