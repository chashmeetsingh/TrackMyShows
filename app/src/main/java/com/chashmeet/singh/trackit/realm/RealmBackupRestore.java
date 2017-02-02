package com.chashmeet.singh.trackit.realm;

import android.util.Log;
import android.widget.Toast;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import io.realm.Realm;
import com.chashmeet.singh.trackit.misc.App;
import com.chashmeet.singh.trackit.utility.FilteredFilePickerFragment;

public class RealmBackupRestore {

    private final String TAG = "RealmBackupRestore";

    private String date = new SimpleDateFormat("yyyyMMdd").format(new Date());

    private final String EXPORT_REALM_FILE_NAME = "TMSBackup-" + date +
            FilteredFilePickerFragment.EXTENSION;

    public void backup(final String exportRealmPath) {
        try {
            // create a backup file
            File exportRealmFile = new File(exportRealmPath, EXPORT_REALM_FILE_NAME);

            // if backup file already exists, delete it
            exportRealmFile.delete();

            // copy current realm to backup file
            Realm realm = RealmSingleton.getInstance().getRealm();
            realm.writeCopyTo(exportRealmFile);
            Toast.makeText(App.getAppContext(), "Backup Complete", Toast.LENGTH_LONG).show();
        } catch (IOException e) {
            Toast.makeText(App.getAppContext(), "Backup Failed!", Toast.LENGTH_LONG).show();
            Log.e(TAG, String.valueOf(e));
        }
    }

    public void restore(final String importRealmPath) {
        copyBundledRealmFile(importRealmPath, "tmsdb.realm");
    }

    private String copyBundledRealmFile(String oldFilePath, String outFileName) {
        try {
            RealmSingleton.getInstance().getRealm().close();
            RealmSingleton.mInstance = null;
            File file = new File(App.getAppContext().getFilesDir(), outFileName);
            FileOutputStream outputStream = new FileOutputStream(file);
            FileInputStream inputStream = new FileInputStream(new File(oldFilePath));

            byte[] buf = new byte[1024];
            int bytesRead;
            while ((bytesRead = inputStream.read(buf)) > 0) {
                outputStream.write(buf, 0, bytesRead);
            }
            outputStream.close();
            Toast.makeText(App.getAppContext(), "Data restored", Toast.LENGTH_LONG).show();
            return file.getAbsolutePath();
        } catch (IOException e) {
            Log.e(TAG, String.valueOf(e));
            Toast.makeText(App.getAppContext(), "Failed to restore data!", Toast.LENGTH_LONG).show();
        }
        return null;
    }
}
