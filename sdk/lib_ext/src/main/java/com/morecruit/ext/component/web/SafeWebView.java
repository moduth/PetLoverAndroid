package com.morecruit.ext.component.web;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Build;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.webkit.WebView;

/**
 * 安全的WebView，移除了Androidd的内部JS接口, 并验证进来的JS接口
 * 是否通过{@link SafeWebInterface}添加
 */
public class SafeWebView extends WebView {

    private final static String[] UNSAFE_JS_INTERFACE = new String[]{
            "searchBoxJavaBridge_",
            "accessibility",
            "accessibilityTraversal"
    };

    private final static String[] SAFE_JS_CLASS_PREFIX = new String[]{
            "android."
    };

    public SafeWebView(Context context) {
        super(context);
        init();
    }

    public SafeWebView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public SafeWebView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    @SuppressLint("NewApi")
    public SafeWebView(Context context, AttributeSet attrs, int defStyle, boolean privateBrowsing) {
        super(context, attrs, defStyle, privateBrowsing);
        init();
    }

    @SuppressLint("NewApi")
    private void init() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB
                && Build.VERSION.SDK_INT <= Build.VERSION_CODES.JELLY_BEAN) {
            for (String unsafe : UNSAFE_JS_INTERFACE) {
                super.removeJavascriptInterface(unsafe);
            }
        }
    }

    @SuppressLint("JavascriptInterface")
    @Override
    public void addJavascriptInterface(Object obj, String interfaceName) {
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.JELLY_BEAN) {
            Class<?> cls = obj.getClass();
            if (!cls.isAnnotationPresent(SafeWebInterface.class) && !isSafeJSClass(cls)) {
                throw new SecurityException("specified object " + obj + " is not declared safe with SafeWebInterface annotation.");
            }
        }
        super.addJavascriptInterface(obj, interfaceName);
    }

    @Override
    @SuppressLint("NewApi")
    public void removeJavascriptInterface(String interfaceName) {
        super.removeJavascriptInterface(interfaceName);
    }

    private static boolean isSafeJSClass(Class<?> cls) {
        String name = cls.getName();
        if (TextUtils.isEmpty(name)) {
            // should not happen.
            return false;
        }
        for (String prefix : SAFE_JS_CLASS_PREFIX) {
            if (name.startsWith(prefix)) {
                return true;
            }
        }
        return false;
    }
}
