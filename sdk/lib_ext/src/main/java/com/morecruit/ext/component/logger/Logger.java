package com.morecruit.ext.component.logger;

import android.content.Context;
import android.util.Log;
import android.util.Pair;

import com.morecruit.ext.utils.ArrayUtils;
import com.morecruit.ext.utils.ObjectUtils;

import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

/**
 * LogProxy，unified log interface。
 * 实际实现为init时注入的logger，通过组合模式实现Logcat和文件混合输出
 * <p/>
 * Created by zhaiyifan on 2015/7/31.
 */
public class Logger {
    // Use release Logger by default
    private static ILog mLog = ReleaseLogger.getInstance();

    public static void init(Context context, ILog logImpl) {
        if (logImpl == null) {
            throw new NullPointerException("log implementation == null");
        }
        LoggerFile.configure(context);
        mLog = logImpl;
    }

    public static void v(String tag, String text) {
        mLog.v(tag, text);
    }

    public static void v(String tag, Object object){
        mLog.v(tag, wrapCollection(object));
    }

    public static void v(String tag, String text, Throwable tr) {
        mLog.v(tag, text + '\n' + getStackTraceString(tr));
    }

    public static void d(String tag, String text) {
        mLog.d(tag, text);
    }

    public static void d(String tag, Object object){
        mLog.d(tag, wrapCollection(object));
    }

    public static void d(String tag, String text, Throwable tr) {
        mLog.d(tag, text + '\n' + getStackTraceString(tr));
    }

    /**
     * log for debug
     *
     * @param tag    tag
     * @param format message format, such as "%d ..."
     * @param params message content params
     * @see android.util.Log#d(String, String)
     */
    public static void d(String tag, String format, Object... params) {
        String msg = String.format(format, params);
        Logger.d(tag, msg);
    }

    public static void i(String tag, String text) {
        mLog.i(tag, text);
    }

    public static void i(String tag, Object object){
        mLog.i(tag, wrapCollection(object));
    }

    public static void i(String tag, String text, Throwable tr) {
        mLog.i(tag, text + '\n' + getStackTraceString(tr));
    }

    public static void w(String tag, String text) {
        mLog.w(tag, text);
    }

    public static void w(String tag, Throwable tr) {
        mLog.w(tag, getStackTraceString(tr));
    }

    public static void w(String tag, Object object){
        mLog.w(tag, wrapCollection(object));
    }

    public static void w(String tag, String text, Throwable tr) {
        mLog.w(tag, text + '\n' + getStackTraceString(tr));
    }

    public static void e(String tag, String text, Throwable tr) {
        mLog.e(tag, text + '\n' + getStackTraceString(tr));
    }

    public static void e(String tag, Object object){
        mLog.e(tag, wrapCollection(object));
    }

    public static void e(String tag, String text) {
        mLog.e(tag, text);
    }

    public static void e(Exception e) {
        mLog.e(e);
    }

    public static void v(String text) {
        mLog.v(text);
    }

    public static void d(String text) {
        mLog.d(text);
    }

    public static void i(String text) {
        mLog.i(text);
    }

    public static void w(String text) {
        mLog.w(text);
    }

    public static void e(String text) {
        mLog.e(text);
    }

    /**
     * 从Throwable获得可以log的stacktrace
     */
    public static String getStackTraceString(Throwable tr) {
        return Log.getStackTraceString(tr);
    }

    /**
     * 把Object转化为String，方便输出到log中，支持Collection、Map、Array、Throwable
     *
     * @param object Collection、Map、Array、Throwable、或者任意其他Object（会用反射获取）
     * @return 可log的String
     */
    public static String wrapCollection(Object object) {
        String message = null;
        if (object != null) {
            final String simpleName = object.getClass().getSimpleName();
            if (object instanceof Throwable) {
                message = getStackTraceString((Throwable) object);
            } else if (object instanceof String) {
                message = (String) object;
            } else if (object.getClass().isArray()) {
                String msg = "over two-dimension array not supported yet";
                int dim = ArrayUtils.getArrayDimension(object);
                switch (dim) {
                    case 1:
                        Pair pair = ArrayUtils.arrayToString(object);
                        msg = simpleName.replace("[]", "[" + pair.first + "] {\n");
                        msg += pair.second + "\n";
                        break;
                    case 2:
                        Pair pair1 = ArrayUtils.arrayToObject(object);
                        Pair pair2 = (Pair) pair1.first;
                        msg = simpleName.replace("[][]", "[" + pair2.first + "][" + pair2.second + "] {\n");
                        msg += pair1.second + "\n";
                        break;
                    default:
                        break;
                }
                message = msg + "}";
            } else if (object instanceof Collection) {
                Collection collection = (Collection) object;
                String msg = "%s size = %d [\n";
                msg = String.format(msg, simpleName, collection.size());
                if (!collection.isEmpty()) {
                    Iterator<Object> iterator = collection.iterator();
                    int flag = 0;
                    while (iterator.hasNext()) {
                        String itemString = "[%d]:%s%s";
                        Object item = iterator.next();
                        msg += String.format(itemString, flag, ObjectUtils.objectToString(item),
                                flag++ < collection.size() - 1 ? ",\n" : "\n");
                    }
                }
                message = msg + "\n]";
            } else if (object instanceof Map) {
                String msg = simpleName + " {\n";
                Map<Object, Object> map = (Map<Object, Object>) object;
                Set<Object> keys = map.keySet();
                for (Object key : keys) {
                    String itemString = "[%s -> %s]\n";
                    Object value = map.get(key);
                    msg += String.format(itemString, ObjectUtils.objectToString(key),
                            ObjectUtils.objectToString(value));
                }
                message = msg + "}";
            } else {
                message = ObjectUtils.objectToString(object);
            }
        } else {
            // will give corresponding null-pointer message
            message = ObjectUtils.objectToString(object);
        }
        return message;
    }
}