package com.morecruit.ext.component.logger;

import android.os.Environment;

/**
 * Logger configuration constants
 * <p/>
 * Created by zhaiyifan on 2015/8/18.
 */
class LogConstants {
    // default tag
    public static final String TAG = "Logger";
    // log directory
    public static final String PATH = Environment.getExternalStorageDirectory() + "/com/morecruit/ext/component/logger/";
    // log filename
    public static final String FILE_NAME = "com.morecruit.ext.component.logger.log";
}
