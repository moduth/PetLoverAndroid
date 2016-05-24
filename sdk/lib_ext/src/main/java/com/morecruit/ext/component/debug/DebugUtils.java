package com.morecruit.ext.component.debug;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * <p>Utilities for debug.</p>
 * Created by zhaiyifan on 2015/8/3.
 */
public final class DebugUtils {

    private final static String[] LOGCAT_COMMAND = new String[]{
            "logcat",
            "-d",
            "-v time"
    };

    private static volatile String sLineSeparator;

    private DebugUtils() {
        // static usage.
    }

    /**
     * Dump whole logcat.
     *
     * @return Logcat.
     */
    public static String dumpLogcat() {
        return dumpLogcat(-1);
    }

    /**
     * Dump logcat with corresponding length.
     *
     * @param maxLength Maximum length of logcat to dump.
     * @return Logcat.
     */
    public static String dumpLogcat(int maxLength) {

        final StringBuilder log = new StringBuilder();

        Process process = null;
        BufferedReader reader = null;
        try {
            String[] commandLine = LOGCAT_COMMAND;
            process = Runtime.getRuntime().exec(commandLine);
            reader = new BufferedReader(new InputStreamReader(process.getInputStream()));

            String line;
            while ((line = reader.readLine()) != null && (maxLength < 0 || log.length() < maxLength)) {
                log.append(line);
                log.append(getLineSeparator());
            }
        } catch (Throwable e) {
            // empty.
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    // empty.
                }
            }
            if (process != null) {
                process.destroy();
            }
        }
        return log.toString();
    }

    private static String getLineSeparator() {
        if (sLineSeparator == null) {
            sLineSeparator = System.getProperty("line.separator");
        }
        return sLineSeparator;
    }
}
