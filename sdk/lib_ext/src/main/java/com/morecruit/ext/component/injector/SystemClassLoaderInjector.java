package com.morecruit.ext.component.injector;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;

import java.io.File;
import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

import dalvik.system.DexClassLoader;
import dalvik.system.PathClassLoader;

/**
 * Dex注入
 * <p>
 * Created by zhaiyifan on 2015/8/4.
 */
final class SystemClassLoaderInjector {

    /**
     * @see {@link com.morecruit.ext.component.injector.core.AntiLazyLoad}
     */
    public final static String ANTI_LAZY_LOAD = "com.morecruit.ext.component.injector.core.AntiLazyLoad";

    private final static String CLASS_LOADER_ALIYUN = "dalvik.system.LexClassLoader";
    private final static String CLASS_LOADER_BASE_DEX = "dalvik.system.BaseDexClassLoader";

    public static void inject(Context context, String libPath) throws Exception {
        if (isAliyunOs()) {
            injectInAliyunOs(context, libPath);
            return;
        }

        if (!hasBaseDexClassLoader()) {
            injectBelowApiLevel14(context, libPath);
        } else {
            injectAboveEqualApiLevel14(context, libPath);
        }
    }

    private static boolean isAliyunOs() {
        try {
            Class.forName(CLASS_LOADER_ALIYUN);
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }

    private static boolean hasBaseDexClassLoader() {
        try {
            Class.forName(CLASS_LOADER_BASE_DEX);
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }

    private static void injectInAliyunOs(Context context, String libPath) throws Exception {
        PathClassLoader localClassLoader = (PathClassLoader) context.getClassLoader();
        new DexClassLoader(libPath, context.getDir("dex", 0).getAbsolutePath(), libPath, localClassLoader);
        String lexFileName = new File(libPath).getName();
        lexFileName = lexFileName.replaceAll("\\.[a-zA-Z0-9]+", ".lex");

        Class<?> classLexClassLoader = Class.forName(CLASS_LOADER_ALIYUN);
        Constructor<?> constructorLexClassLoader = classLexClassLoader.getConstructor(String.class, String.class,
                String.class, ClassLoader.class);
        Object localLexClassLoader = constructorLexClassLoader.newInstance(context.getDir("dex", 0).getAbsolutePath()
                + File.separator + lexFileName, context.getDir("dex", 0).getAbsolutePath(), libPath, localClassLoader);
        Method methodLoadClass = classLexClassLoader.getMethod("loadClass", String.class);
        methodLoadClass.invoke(localLexClassLoader, ANTI_LAZY_LOAD);
        setField(
                localClassLoader,
                PathClassLoader.class,
                "mPaths",
                appendArray(getField(localClassLoader, PathClassLoader.class, "mPaths"),
                        getField(localLexClassLoader, classLexClassLoader, "mRawDexPath")));
        setField(
                localClassLoader,
                PathClassLoader.class,
                "mFiles",
                combineArray(getField(localClassLoader, PathClassLoader.class, "mFiles"),
                        getField(localLexClassLoader, classLexClassLoader, "mFiles")));
        setField(
                localClassLoader,
                PathClassLoader.class,
                "mZips",
                combineArray(getField(localClassLoader, PathClassLoader.class, "mZips"),
                        getField(localLexClassLoader, classLexClassLoader, "mZips")));
        setField(
                localClassLoader,
                PathClassLoader.class,
                "mLexs",
                combineArray(getField(localClassLoader, PathClassLoader.class, "mLexs"),
                        getField(localLexClassLoader, classLexClassLoader, "mDexs")));

    }

    @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
    private static void injectBelowApiLevel14(Context context, String libPath) throws Exception {
        PathClassLoader pathClassLoader = (PathClassLoader) context.getClassLoader();
        DexClassLoader dexClassLoader = new DexClassLoader(libPath, context.getDir("dex", 0).getAbsolutePath(), libPath,
                context.getClassLoader());

        dexClassLoader.loadClass(ANTI_LAZY_LOAD);
        setField(
                pathClassLoader,
                PathClassLoader.class,
                "mPaths",
                appendArray(getField(pathClassLoader, PathClassLoader.class, "mPaths"),
                        getField(dexClassLoader, DexClassLoader.class, "mRawDexPath")));
        setField(
                pathClassLoader,
                PathClassLoader.class,
                "mFiles",
                combineArray(getField(pathClassLoader, PathClassLoader.class, "mFiles"),
                        getField(dexClassLoader, DexClassLoader.class, "mFiles")));
        setField(
                pathClassLoader,
                PathClassLoader.class,
                "mZips",
                combineArray(getField(pathClassLoader, PathClassLoader.class, "mZips"),
                        getField(dexClassLoader, DexClassLoader.class, "mZips")));
        setField(
                pathClassLoader,
                PathClassLoader.class,
                "mDexs",
                combineArray(getField(pathClassLoader, PathClassLoader.class, "mDexs"),
                        getField(dexClassLoader, DexClassLoader.class, "mDexs")));
    }

    private static void injectAboveEqualApiLevel14(Context context, String libPath) throws Exception {
        PathClassLoader pathClassLoader = (PathClassLoader) context.getClassLoader();
        DexClassLoader dexClassLoader = new DexClassLoader(libPath, context.getDir("dex", 0).getAbsolutePath(), libPath,
                context.getClassLoader());

        Object dexElements = combineArray(getDexElements(getPathList(pathClassLoader)),
                getDexElements(getPathList(dexClassLoader)));
        Object pathList = getPathList(pathClassLoader);
        setField(pathList, pathList.getClass(), "dexElements", dexElements);
    }

    private static Object getPathList(Object baseDexClassLoader) throws IllegalArgumentException, NoSuchFieldException,
            IllegalAccessException, ClassNotFoundException {
        return getField(baseDexClassLoader, Class.forName(CLASS_LOADER_BASE_DEX), "pathList");
    }

    private static Object getDexElements(Object paramObject) throws IllegalArgumentException, NoSuchFieldException,
            IllegalAccessException {
        return getField(paramObject, paramObject.getClass(), "dexElements");
    }

    private static Object getField(Object obj, Class<?> cl, String field) throws NoSuchFieldException,
            IllegalArgumentException, IllegalAccessException {
        Field localField = cl.getDeclaredField(field);
        localField.setAccessible(true);
        return localField.get(obj);
    }

    private static void setField(Object obj, Class<?> cl, String field, Object value) throws NoSuchFieldException,
            IllegalArgumentException, IllegalAccessException {
        Field localField = cl.getDeclaredField(field);
        localField.setAccessible(true);
        localField.set(obj, value);
    }

    private static Object combineArray(Object arrayLhs, Object arrayRhs) {
        Class<?> localClass = arrayLhs.getClass().getComponentType();
        int i = Array.getLength(arrayLhs);
        int j = i + Array.getLength(arrayRhs);
        Object result = Array.newInstance(localClass, j);
        for (int k = 0; k < j; ++k) {
            if (k < i) {
                Array.set(result, k, Array.get(arrayLhs, k));
            } else {
                Array.set(result, k, Array.get(arrayRhs, k - i));
            }
        }
        return result;
    }

    private static Object appendArray(Object array, Object value) {
        Class<?> localClass = array.getClass().getComponentType();
        int i = Array.getLength(array);
        int j = i + 1;
        Object localObject = Array.newInstance(localClass, j);
        for (int k = 0; k < j; ++k) {
            if (k < i) {
                Array.set(localObject, k, Array.get(array, k));
            } else {
                Array.set(localObject, k, value);
            }
        }
        return localObject;
    }
}
