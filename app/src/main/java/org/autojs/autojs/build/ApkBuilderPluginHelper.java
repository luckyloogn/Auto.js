package org.autojs.autojs.build;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;

import com.stardust.pio.UncheckedIOException;

import org.autojs.autojs.BuildConfig;

import com.stardust.util.DeveloperUtils;

import java.io.IOException;
import java.io.InputStream;

/**
 * Created by Stardust on 2017/11/29.
 */

public class ApkBuilderPluginHelper {

    private static final String TEMPLATE_APK_PATH = "template.apk";

    public static InputStream openTemplateApk(Context context) {
        try {
            return context.getAssets().open(TEMPLATE_APK_PATH);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

}


