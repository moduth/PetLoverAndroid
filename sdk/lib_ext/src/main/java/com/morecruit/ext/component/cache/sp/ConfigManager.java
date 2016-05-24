package com.morecruit.ext.component.cache.sp;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;

import java.util.HashMap;

/**
 * 内部设置管理
 *
 * @author markzhai on 2015/8/4.
 */
public class ConfigManager {

    private static HashMap<String, SharedPreferences> sPreferencesMap = new HashMap<>();
    private static boolean sAsyncSupport = true;
    private static Context sContext;

    /**
     * 程序用全局设置
     */
    public static final String KEY_APPLICATION = "crew";
    /**
     * 账号用，登录态等，会被统一清空
     */
    public static final String KEY_ACCOUNT = "account";

    /**
     * 股吧消息数据
     */
    public static final String KEY_GUBA_MSG = "usermsgcount";

    public static final String KEY_ADV = "em_adv";

    public class ADV {
        public static final String SP_STARTUP_ADV_ID = "startupAdId";
        public static final String SP_STARTUP_ADV_TIME = "startupAdShowTime";
        public static final String SP_STARTUP_ADV_URL = "startupAdImageUrlAndroid";
        public static final String SP_STARTUP_ADV_DOWNLOADED = "startupAdImageDownloaded";
        public static final String SP_STARTUP_ADV_LASTSHOWTIME = "startupAdLastShowTime";
        public static final String SP_STARTUP_ADV_INTERVAL = "startupAdInterval";
        public static final long DEFAULT_INTERVAL = 2;

        public static final String SP_STARTUP_JUMP_URI = "jumpAndroidUrl";
        public static final String SP_STARTUP_ADV_BEGIN_TIME = "startupAdBeginTime";
        public static final String SP_STARTUP_ADV_EXPIRED_TIME = "startupAdExpiredTime";
        public static final String SP_STARTUP_ADV_WEBPAGE_URL = "startupAdMonitorShowUrl";

        public static final String SP_PULL_ADV_ID = "pullrefreshAdId";
        public static final String SP_PULL_ADV_URL = "pullrefreshAdImageUrlAndroid";
        public static final String SP_PULL_ADV_BEGIN_TIME = "pullrefreshAdBeginTime";
        public static final String SP_PULL_ADV_EXPIRED_TIME = "pullrefreshAdExpiredTime";

        public static final String SP_QI_QUAN_SWITCH = "switch";
        public static final String SP_QI_QUAN_BEGIN_TIME = "beginTime";
        public static final String SP_QI_QUAN_EXPIRED_TIME = "expiredTime";

        public static final String SP_PHONE_REGISTER_TYPE = "registerType";
        public static final String SP_PHONE_REGISTER_URL = "registerUrl";
    }

    /**
     * 初始化，通常放在Application的onCreate
     *
     * @param context Application context
     */
    public static void init(Context context) {
        sContext = context;

        // apply operation is only support >= api 9
        sAsyncSupport = Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD;
    }

    public static boolean contains(String key) {
        return contains(key, KEY_APPLICATION);
    }

    public static boolean contains(String key, String preferencesKey) {
        return getPreferences(preferencesKey).contains(key);
    }

    public static void clear(String preferencesKey) {
        if (sAsyncSupport) {
            getPreferences(preferencesKey).edit().clear().apply();
        } else {
            getPreferences(preferencesKey).edit().clear().commit();
        }
    }

    public static void remove(String key) {
        remove(key, KEY_APPLICATION);
    }

    public static void remove(String key, String preferencesKey) {
        getPreferences(preferencesKey).edit().remove(key).commit();
    }

    public static String getString(String key) {
        return getString(key, "");
    }

    public static String getString(String key, String defaultValue) {
        return getString(key, defaultValue, KEY_APPLICATION);
    }

    public static String getString(String key, String defaultValue, String preferencesKey) {
        return getPreferences(preferencesKey).getString(key, defaultValue);
    }

    public static boolean getBoolean(String key, boolean defaultValue) {
        return getBoolean(key, defaultValue, KEY_APPLICATION);
    }

    public static boolean getBoolean(String key, boolean defaultValue, String preferencesKey) {
        return getPreferences(preferencesKey).getBoolean(key, defaultValue);
    }

    public static int getInt(String key, int defaultValue) {
        return getInt(key, defaultValue, KEY_APPLICATION);
    }

    public static int getInt(String key, int defaultValue, String preferencesKey) {
        return getPreferences(preferencesKey).getInt(key, defaultValue);
    }

    public static long getLong(String key, long defaultValue) {
        return getLong(key, defaultValue, KEY_APPLICATION);
    }

    public static long getLong(String key, long defaultValue, String preferencesKey) {
        return getPreferences(preferencesKey).getLong(key, defaultValue);
    }

    public static float getFloat(String key, float defaultValue) {
        return getFloat(key, defaultValue, KEY_APPLICATION);
    }

    public static float getFloat(String key, float defaultValue, String preferencesKey) {
        return getPreferences(preferencesKey).getFloat(key, defaultValue);
    }

    public static void putString(String key, String value) {
        putString(key, value, KEY_APPLICATION);
    }

    public static void putString(String key, String value, String preferencesKey) {
        putString(key, value, preferencesKey, true);
    }

    public static void putString(String key, String value, String preferencesKey, boolean async) {
        if (async && sAsyncSupport) {
            getPreferences(preferencesKey).edit().putString(key, value).apply();
        } else {
            getPreferences(preferencesKey).edit().putString(key, value).commit();
        }
    }

    public static void putBoolean(String key, boolean value) {
        putBoolean(key, value, KEY_APPLICATION);
    }

    public static void putBoolean(String key, boolean value, String preferencesKey) {
        putBoolean(key, value, preferencesKey, true);
    }

    public static void putBoolean(String key, boolean value, String preferencesKey, boolean async) {
        if (async && sAsyncSupport) {
            getPreferences(preferencesKey).edit().putBoolean(key, value).apply();
        } else {
            getPreferences(preferencesKey).edit().putBoolean(key, value).commit();
        }
    }

    public static void putInt(String key, int value) {
        putInt(key, value, KEY_APPLICATION);
    }

    public static void putInt(String key, int value, String preferencesKey) {
        putInt(key, value, preferencesKey, true);
    }

    public static void putInt(String key, int value, String preferencesKey, boolean async) {
        if (async && sAsyncSupport) {
            getPreferences(preferencesKey).edit().putInt(key, value).apply();
        } else {
            getPreferences(preferencesKey).edit().putInt(key, value).commit();
        }
    }

    public static void putLong(String key, long value) {
        putLong(key, value, KEY_APPLICATION);
    }

    public static void putLong(String key, long value, String preferencesKey) {
        putLong(key, value, preferencesKey, true);
    }

    public static void putLong(String key, long value, String preferencesKey, boolean async) {
        if (async && sAsyncSupport) {
            getPreferences(preferencesKey).edit().putLong(key, value).apply();
        } else {
            getPreferences(preferencesKey).edit().putLong(key, value).commit();
        }
    }

    public static void putFloat(String key, float value) {
        putFloat(key, value, KEY_APPLICATION);
    }

    public static void putFloat(String key, float value, String preferencesKey) {
        putFloat(key, value, preferencesKey, true);
    }

    public static void putFloat(String key, float value, String preferencesKey, boolean async) {
        if (async && sAsyncSupport) {
            getPreferences(preferencesKey).edit().putFloat(key, value).apply();
        } else {
            getPreferences(preferencesKey).edit().putFloat(key, value).commit();
        }
    }

    public static void clearLoginStatus() {
        sPreferencesMap.get(KEY_APPLICATION).edit()
                .putString("BindMob", "")
                .putString("PassUsrPswd", "")
                .putBoolean("PassAuLoginFlag", false)
                .putBoolean("PassFlag", false)
                .putString("ChooseStocksPermission", "")
                .putInt("PermissionStartDate", 0)
                .putInt("PermissionEndDate", 0)
                .putBoolean("SyncFlagDismiss0", false)
                .putBoolean("SyncFlagDismiss1", false)
                .putBoolean("SyncFlagDismiss2", false)
                .putInt("UseCount", 0)
                .putInt("UserLevel", 0)
                .putInt("RecentOpenExpDate", 0)
                .putString("muid", "")
                .putString("nickname", "")
                .putString("intro", "")
                .putString("loginType", "")
                .putString("pi", "")
                .putString("mret0", "")
                .putBoolean("vuser", false)
                .putString("callcenter_curpos", "")
                .putString("mobile_secret", "")
                .putInt("superscriptcnt", 0)
                .putInt("skip_superscriptcnt", 0)
                .putInt("groupmessageint", 0)
                .putBoolean("hasMass", false)
                .putString("bindmobile", "")
                .putString("tencent", "")
                .putString("sina", "")
                .putString("weixin", "")
                .putBoolean("loginState", true)
                .putString("sid", "").commit();
    }

    public static SharedPreferences getPreferences(String key) {
        if (sPreferencesMap.containsKey(key)) {
            return sPreferencesMap.get(key);
        } else {
            SharedPreferences preferences = sContext.getSharedPreferences(key, Context.MODE_PRIVATE);
            sPreferencesMap.put(key, preferences);
            return preferences;
        }
    }
}