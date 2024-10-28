package org.autojs.autojs.ui.project;

import android.content.Context;
import org.autojs.autojs.R;
import java.util.Arrays;
import java.util.Comparator;

public class PermissionProvider {

    public static String[][] getPermissions(Context context) {
        String[][] permissions = {
                {"android.permission.ACCESS_WIFI_STATE", context.getString(R.string.desc_permission_access_wifi_state)},
                {"android.permission.ACCESS_NETWORK_STATE", context.getString(R.string.desc_permission_access_network_state)},
                {"android.permission.ACCESS_FINE_LOCATION", context.getString(R.string.desc_permission_access_fine_location)},
                {"android.permission.ACCESS_COARSE_LOCATION", context.getString(R.string.desc_permission_access_coarse_location)},
                {"android.permission.SCHEDULE_EXACT_ALARM", context.getString(R.string.desc_permission_schedule_exact_alarm)},
                {"android.permission.QUERY_ALL_PACKAGES", context.getString(R.string.desc_permission_query_all_packages)},
                {"android.permission.WRITE_EXTERNAL_STORAGE", context.getString(R.string.desc_permission_write_external_storage)},
                {"android.permission.MANAGE_EXTERNAL_STORAGE", context.getString(R.string.desc_permission_manage_external_storage)},
                {"android.permission.READ_EXTERNAL_STORAGE", context.getString(R.string.desc_permission_read_external_storage)},
                {"android.permission.INTERNET", context.getString(R.string.desc_permission_internet)},
                {"android.permission.SYSTEM_ALERT_WINDOW", context.getString(R.string.desc_permission_system_alert_window)},
                {"android.permission.REQUEST_IGNORE_BATTERY_OPTIMIZATIONS", context.getString(R.string.desc_permission_request_ignore_battery_optimizations)},
                {"android.permission.RECEIVE_BOOT_COMPLETED", context.getString(R.string.desc_permission_receive_boot_completed)},
                {"android.permission.FOREGROUND_SERVICE", context.getString(R.string.desc_permission_foreground_service)},
                {"android.permission.RECORD_AUDIO", context.getString(R.string.desc_permission_record_audio)},
                {"android.permission.READ_PHONE_STATE", context.getString(R.string.desc_permission_read_phone_state)},
                {"com.android.launcher.permission.INSTALL_SHORTCUT", context.getString(R.string.desc_permission_install_shortcut)},
                {"com.android.launcher.permission.UNINSTALL_SHORTCUT", context.getString(R.string.desc_permission_uninstall_shortcut)}
        };

        // 按照第一列（权限名）进行排序
        Arrays.sort(permissions, new Comparator<String[]>() {
            @Override
            public int compare(String[] o1, String[] o2) {
                return o1[0].compareTo(o2[0]);
            }
        });

        return permissions;
    }
}