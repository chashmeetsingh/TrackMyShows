package com.chashmeet.singh.trackit.utility;

import android.support.annotation.Nullable;

import com.nononsenseapps.filepicker.AbstractFilePickerActivity;
import com.nononsenseapps.filepicker.AbstractFilePickerFragment;

import java.io.File;

public class MyFilePickerActivity extends AbstractFilePickerActivity<File> {

    public MyFilePickerActivity() {
        super();
    }

    @Override
    protected AbstractFilePickerFragment<File> getFragment(
            @Nullable String startPath,
            int mode,
            boolean allowMultiple,
            boolean allowCreateDir,
            boolean allowExistingFile,
            boolean singleClick) {
        AbstractFilePickerFragment<File> fragment = new FilteredFilePickerFragment();
        fragment.setArgs(startPath, mode, allowMultiple, allowCreateDir, allowExistingFile, singleClick);
        return fragment;
    }
}
