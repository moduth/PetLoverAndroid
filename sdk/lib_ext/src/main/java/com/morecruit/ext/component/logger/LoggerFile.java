package com.morecruit.ext.component.logger;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.os.Environment;

import com.morecruit.ext.utils.FileUtils;

import org.apache.log4j.Level;

import java.io.File;

/**
 * Log4j configure
 */
public class LoggerFile {

    public static boolean hasConfigured = false;

    public static abstract class Log4jWrapper {

        public abstract void trace(Object message);

        public abstract void trace(Object message, Throwable t);

        public abstract void debug(Object message);

        public abstract void debug(Object message, Throwable t);

        public abstract void info(Object message);

        public abstract void info(Object message, Throwable t);

        public abstract void warn(Object message);

        public abstract void warn(Object message, Throwable t);

        public abstract void warn(Throwable t);

        public abstract void error(Object message);

        public abstract void error(Object message, Throwable t);

        public abstract void error(Throwable t);

        public abstract void fatal(Object message);

        public abstract void fatal(Object message, Throwable t);

        public abstract void fatal(Throwable t);
    }

    public static boolean configure() {
        if (!Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            return false;
        }

        try {
            final LogConfigurator logConfigurator = new LogConfigurator();
            logConfigurator.setFileName(LogConstants.PATH + LogConstants.FILE_NAME);
            logConfigurator.setRootLevel(Level.ALL);
            logConfigurator.setFilePattern("%d - [%p::%c] - %m%n");
            int flags = 0;
            // per file 5MB in debug mode, 512KB in release mode
            boolean isDebugMode = (flags & ApplicationInfo.FLAG_DEBUGGABLE) != 0;
            if (isDebugMode) {
                logConfigurator.setMaxFileSize(5 * 1024 * 1024);
            } else {
                logConfigurator.setMaxFileSize(512 * 1024);
            }
            Log.i("isDebugMode", "configure() flags===>>>>" + flags
                    + ",ApplicationInfo.FLAG_DEBUGGABLE===>>>"
                    + ApplicationInfo.FLAG_DEBUGGABLE + ",isDebugMode==>>"
                    + isDebugMode);
            logConfigurator.configure();
            hasConfigured = true;

        } catch (Exception e) {
            return false;
        }
        return true;
    }

    public static boolean configure(Context context) {
        if (!Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            return false;
        }

        try {
            final LogConfigurator logConfigurator = new LogConfigurator();

            FileUtils.mkdirs(new File(LogConstants.PATH));

            logConfigurator.setImmediateFlush(true);
            logConfigurator.setFileName(LogConstants.PATH + LogConstants.FILE_NAME);
            logConfigurator.setRootLevel(Level.ALL);
            logConfigurator.setFilePattern("%d - [%p::%c] - %m%n");
            int flags = 0;
            try {
                flags = context.getPackageManager()
                        .getPackageInfo(context.getPackageName(), 0).applicationInfo.flags;
            } catch (Exception e) {
                e.printStackTrace();
            }
            // per file 5MB in debug mode, 512KB in release mode
            boolean isDebugMode = (flags & ApplicationInfo.FLAG_DEBUGGABLE) != 0;
            if (isDebugMode) {
                logConfigurator.setMaxFileSize(5 * 1024 * 1024);
            } else {
                logConfigurator.setMaxFileSize(512 * 1024);
            }

            Log.i("isDebugMode", "configure(Context context) flags===>>>>" + flags
                    + ",ApplicationInfo.FLAG_DEBUGGABLE===>>>"
                    + ApplicationInfo.FLAG_DEBUGGABLE + ",isDebugMode==>>"
                    + isDebugMode);
            logConfigurator.configure();
            hasConfigured = true;

        } catch (Exception e) {
            return false;
        }
        return true;
    }

    public static Log4jWrapper getLog4j(String str) {
        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            if (!hasConfigured) {
                if (configure()) {
                    return new LogToFile(org.apache.log4j.Logger.getLogger(str));
                }
            }
            return new LogToFile(org.apache.log4j.Logger.getLogger(str));
        }
        return null;
    }
}
