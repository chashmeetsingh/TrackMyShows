package com.chashmeet.singh.trackit.activity;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SwitchCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.NumberPicker;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.TimeUnit;

import io.realm.Realm;
import io.realm.RealmResults;
import com.chashmeet.singh.trackit.R;
import com.chashmeet.singh.trackit.realm.RealmSetNotification;
import com.chashmeet.singh.trackit.realm.RealmShow;
import com.chashmeet.singh.trackit.realm.RealmSingleton;

public class ShowSettingsActivity extends AppCompatActivity {

    private int showID;
    private SwitchCompat notificationSwitch, updateImagesSwitch;
    private TextView posterTV, bannerTV, fanArtTV, notificationTimeTV;
    private RelativeLayout updateImagesLayout;
    private LinearLayout notificationTimeLayout;
    private long notificationOffsetDays = 0, notificationOffsetHours = 0,
            notificationOffsetMinutes = 0;
    private boolean notificationBefore = true;
    private long showTime;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_settings);

        showID = getIntent().getIntExtra("showID", 0);
        notificationSwitch = (SwitchCompat) findViewById(R.id.notification_switch);
        updateImagesSwitch = (SwitchCompat) findViewById(R.id.update_images_switch);
        posterTV = (TextView) findViewById(R.id.tv_poster);
        bannerTV = (TextView) findViewById(R.id.tv_banner);
        fanArtTV = (TextView) findViewById(R.id.tv_fanart);
        updateImagesLayout = (RelativeLayout) findViewById(R.id.update_images);
        notificationTimeLayout = (LinearLayout) findViewById(R.id.notification_time);
        notificationTimeTV = (TextView) findViewById(R.id.tv_notification_time);

        notificationViewPopulate();
        notificationSwitchListener();
        imagesSwitchListener();
        viewListeners();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                super.onBackPressed();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void notificationViewPopulate() {
        Realm realm = RealmSingleton.getInstance().getRealm();
        long offset = realm.where(RealmShow.class)
                .equalTo("showID", showID)
                .findFirst()
                .getTimeOffset();

        if (offset != 0) {
            if (offset > 0) {
                notificationBefore = false;
            } else {
                notificationBefore = true;
                offset = -offset;
            }

            notificationOffsetDays = TimeUnit.DAYS.convert(offset, TimeUnit.MILLISECONDS);
            offset -= TimeUnit.MILLISECONDS.convert(notificationOffsetDays, TimeUnit.DAYS);
            notificationOffsetHours = TimeUnit.HOURS.convert(offset, TimeUnit.MILLISECONDS);
            offset -= TimeUnit.MILLISECONDS.convert(notificationOffsetHours, TimeUnit.HOURS);
            notificationOffsetMinutes = TimeUnit.MINUTES.convert(offset, TimeUnit.MILLISECONDS);

            String notificationTime = notificationOffsetDays + " days, "
                    + notificationOffsetHours + " hours and "
                    + notificationOffsetMinutes + " minutes ";
            if (notificationBefore) {
                notificationTime += "before";
            } else {
                notificationTime += "after";
            }
            notificationTimeTV.setText(notificationTime);
        } else {
            notificationTimeTV.setText(R.string.notification_time_default);
        }
    }

    private void viewListeners() {
        posterTV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(ShowSettingsActivity.this, ShowImageActivity.class);
                intent.putExtra("showID", showID);
                intent.putExtra("type", 0);
                startActivity(intent);
            }
        });

        bannerTV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(ShowSettingsActivity.this, ShowImageActivity.class);
                intent.putExtra("showID", showID);
                intent.putExtra("type", 1);
                startActivity(intent);
            }
        });

        fanArtTV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(ShowSettingsActivity.this, ShowImageActivity.class);
                intent.putExtra("showID", showID);
                intent.putExtra("type", 2);
                startActivity(intent);
            }
        });

        notificationTimeLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                notificationTimeUpdate();
            }
        });
    }

    private void notificationSwitchListener() {
        final Realm realm = RealmSingleton.getInstance().getRealm();
        boolean isNotificationEnabled = realm.where(RealmShow.class)
                .equalTo("showID", showID)
                .findFirst()
                .isEnableNotification();
        notificationSwitch.setChecked(isNotificationEnabled);
        notificationSwitch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final boolean state = notificationSwitch.isChecked();
                realm.executeTransactionAsync(new Realm.Transaction() {
                    @Override
                    public void execute(Realm realm) {
                        realm.where(RealmShow.class)
                                .equalTo("showID", showID)
                                .findFirst()
                                .setEnableNotification(state);

                        RealmResults<RealmSetNotification> setNotifications = realm
                                .where(RealmSetNotification.class)
                                .equalTo("showID", showID)
                                .findAll();

                        for (int i = 0; i < setNotifications.size(); i++) {
                            Intent notificationIntent = new Intent("android.media.action.DISPLAY_NOTIFICATION");
                            AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
                            notificationIntent.addCategory("android.intent.category.DEFAULT");
                            PendingIntent broadcast = PendingIntent.getBroadcast(ShowSettingsActivity.this,
                                    setNotifications.get(i).getNotificationID(),
                                    notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);
                            alarmManager.cancel(broadcast);
                        }
                        setNotifications.deleteAllFromRealm();
                    }
                });
            }
        });
    }

    private void imagesSwitchListener() {
        final Realm realm = RealmSingleton.getInstance().getRealm();
        boolean isUpdateEnabled = realm.where(RealmShow.class)
                .equalTo("showID", showID)
                .findFirst()
                .isUpdateImages();
        updateImagesSwitch.setChecked(isUpdateEnabled);
        updateImagesLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                updateImagesSwitch.toggle();
                final boolean state = updateImagesSwitch.isChecked();
                realm.executeTransactionAsync(new Realm.Transaction() {
                    @Override
                    public void execute(Realm realm) {
                        realm.where(RealmShow.class)
                                .equalTo("showID", showID)
                                .findFirst()
                                .setUpdateImages(state);
                    }
                });
            }
        });
    }

    private void notificationTimeUpdate() {

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = this.getLayoutInflater();
        View viewInflated = inflater.inflate(R.layout.layout_dialog_box_time, null);

        final TextView tvTime = (TextView) viewInflated.findViewById(R.id.tv_time);
        final NumberPicker numberDays = (NumberPicker) viewInflated.findViewById(R.id.number_picker_days);
        final NumberPicker numberHours = (NumberPicker) viewInflated.findViewById(R.id.number_picker_hours);
        final NumberPicker numberMinutes = (NumberPicker) viewInflated.findViewById(R.id.number_picker_minutes);
        final Spinner spinner = (Spinner) viewInflated.findViewById(R.id.spinner_time);
        final Button buttonOK = (Button) viewInflated.findViewById(R.id.button_ok);
        final Button buttonCancel = (Button) viewInflated.findViewById(R.id.button_cancel);

        Realm realm = RealmSingleton.getInstance().getRealm();
        final Date originalShowTime = realm.where(RealmShow.class)
                .equalTo("showID", showID)
                .findFirst()
                .getAirTime();
        showTime = originalShowTime.getTime();

        final SimpleDateFormat timeFormatter = new SimpleDateFormat("hh:mm a");

        numberDays.setMinValue(0);
        numberDays.setMaxValue(7);

        numberHours.setMinValue(0);
        numberHours.setMaxValue(23);

        numberMinutes.setMinValue(0);
        numberMinutes.setMaxValue(59);

        numberDays.setValue((int) notificationOffsetDays);
        numberHours.setValue((int) notificationOffsetHours);
        numberMinutes.setValue((int) notificationOffsetMinutes);

        String timeArray[] = new String[]{"Before", "Later"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, R.layout.item_notification_spinner, timeArray);
        adapter.setDropDownViewResource(R.layout.item_notification_spinner);
        spinner.setAdapter(adapter);

        final AlertDialog dialog = builder
                .setView(viewInflated)
                .create();

        dialog.show();

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                showTime = originalShowTime.getTime();
                if (i == 0) {
                    notificationBefore = true;
                    showTime = showTime - (numberHours.getValue() * 3600000);
                    showTime = showTime - (numberMinutes.getValue() * 60000);
                } else if (i == 1) {
                    notificationBefore = false;
                    showTime = showTime + (numberHours.getValue() * 3600000);
                    showTime = showTime + (numberMinutes.getValue() * 60000);
                }
                tvTime.setText(timeFormatter.format(new Date(showTime)));
                Log.d("Settings", i + " Time set");
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        if (notificationBefore) {
            spinner.setSelection(0);
            showTime = originalShowTime.getTime();
            showTime = showTime - (notificationOffsetHours * 3600000);
            showTime = showTime - (notificationOffsetMinutes * 60000);
        } else {
            spinner.setSelection(1);
            showTime = originalShowTime.getTime();
            showTime = showTime + (notificationOffsetHours * 3600000);
            showTime = showTime + (notificationOffsetMinutes * 60000);
        }

        tvTime.setText(timeFormatter.format(new Date(showTime)));

        numberHours.setOnValueChangedListener(new NumberPicker.OnValueChangeListener() {
            @Override
            public void onValueChange(NumberPicker numberPicker, int i, int i1) {
                showTime = originalShowTime.getTime();
                if (spinner.getSelectedItemPosition() == 0) {
                    showTime = showTime - (i1 * 3600000);
                    showTime = showTime - (numberMinutes.getValue() * 60000);
                } else {
                    showTime = showTime + (i1 * 3600000);
                    showTime = showTime + (numberMinutes.getValue() * 60000);
                }
                tvTime.setText(timeFormatter.format(new Date(showTime)));
            }
        });

        numberMinutes.setOnValueChangedListener(new NumberPicker.OnValueChangeListener() {
            @Override
            public void onValueChange(NumberPicker numberPicker, int i, int i1) {
                showTime = originalShowTime.getTime();
                if (spinner.getSelectedItemPosition() == 0) {
                    showTime = showTime - (numberHours.getValue() * 3600000);
                    showTime = showTime - (i1 * 60000);
                } else {
                    showTime = showTime + (numberHours.getValue() * 3600000);
                    showTime = showTime + (i1 * 60000);
                }
                tvTime.setText(timeFormatter.format(new Date(showTime)));
            }
        });

        buttonOK.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Realm realm = RealmSingleton.getInstance().getRealm();
                realm.executeTransactionAsync(new Realm.Transaction() {
                    @Override
                    public void execute(Realm realm) {
                        try {
                            notificationOffsetDays = numberDays.getValue();
                        } catch (Exception e) {
                            notificationOffsetDays = 0;
                        }
                        try {
                            notificationOffsetHours = numberHours.getValue();
                        } catch (Exception e) {
                            notificationOffsetHours = 0;
                        }
                        try {
                            notificationOffsetMinutes = numberMinutes.getValue();
                        } catch (Exception e) {
                            notificationOffsetMinutes = 0;
                        }

                        long offset = 0;
                        offset += TimeUnit.MILLISECONDS.convert(notificationOffsetDays, TimeUnit.DAYS);
                        offset += TimeUnit.MILLISECONDS.convert(notificationOffsetHours, TimeUnit.HOURS);
                        offset += TimeUnit.MILLISECONDS.convert(notificationOffsetMinutes, TimeUnit.MINUTES);

                        if (spinner.getSelectedItemPosition() == 0) {
                            notificationBefore = true;
                            offset = -offset;
                        } else {
                            notificationBefore = false;
                        }

                        realm.where(RealmShow.class)
                                .equalTo("showID", showID)
                                .findFirst()
                                .setTimeOffset(offset);
                    }
                }, new Realm.Transaction.OnSuccess() {
                    @Override
                    public void onSuccess() {
                        dialog.dismiss();
                        notificationViewPopulate();
                    }
                });
            }
        });

        buttonCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
            }
        });
    }
}
