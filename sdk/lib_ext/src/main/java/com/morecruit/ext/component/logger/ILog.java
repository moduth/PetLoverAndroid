package com.morecruit.ext.component.logger;

/**
 * Log unified interface.
 * <p/>
 * Created by zhaiyifan on 2015/7/31.
 */
interface ILog {

    // with tag
    void v(String tag, String text);

    void d(String tag, String text);

    void i(String tag, String text);

    void w(String tag, String text);

    void e(String tag, String text);

    void e(Exception e);

    // without tag
    void v(String text);

    void d(String text);

    void i(String text);

    void w(String text);

    void e(String text);
}