package com.morecruit.ext.utils;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import com.morecruit.ext.component.logger.Logger;

/**
 * 支持串行化的方便的map，提供了多种API
 *
 * @author markzhai on 16/3/5
 */
public class Pack<E> implements Serializable {

    private static final String LOG_TAG = "Pack";

    private static final long serialVersionUID = -2313525078625494026L;

    private HashMap<E, Object> mMap = new HashMap<E, Object>();

    public Pack() {

    }

    public Pack(Pack<E> pack) {
        mMap.putAll(pack.mMap);
    }

    //------------get for common-------------
    public boolean getBoolean(E key, boolean defaultValue) {
        Object o = mMap.get(key);
        if (o == null) {
            return defaultValue;
        }
        try {
            return (Boolean) o;
        } catch (ClassCastException e) {
            typeWarning(key, o, "Boolean", defaultValue, e);
            return defaultValue;
        }
    }

    public byte getByte(E key, byte defaultValue) {
        Object o = mMap.get(key);
        if (o == null) {
            return defaultValue;
        }
        try {
            return (Byte) o;
        } catch (ClassCastException e) {
            typeWarning(key, o, "Byte", defaultValue, e);
            return defaultValue;
        }
    }

    public char getChar(E key, char defaultValue) {
        Object o = mMap.get(key);
        if (o == null) {
            return defaultValue;
        }
        try {
            return (Character) o;
        } catch (ClassCastException e) {
            typeWarning(key, o, "Character", defaultValue, e);
            return defaultValue;
        }
    }

    public short getShort(E key, short defaultValue) {
        Object o = mMap.get(key);
        if (o == null) {
            return defaultValue;
        }
        try {
            return (Short) o;
        } catch (ClassCastException e) {
            typeWarning(key, o, "Short", defaultValue, e);
            return defaultValue;
        }
    }

    public int getInt(E key, int defaultValue) {
        Object o = mMap.get(key);
        if (o == null) {
            return defaultValue;
        }
        try {
            return (Integer) o;
        } catch (ClassCastException e) {
            typeWarning(key, o, "Integer", defaultValue, e);
            return defaultValue;
        }
    }

    public long getLong(E key, long defaultValue) {
        Object o = mMap.get(key);
        if (o == null) {
            return defaultValue;
        }
        try {
            return (Long) o;
        } catch (ClassCastException e) {
            typeWarning(key, o, "Long", defaultValue, e);
            return defaultValue;
        }
    }

    public float getFloat(E key, float defaultValue) {
        Object o = mMap.get(key);
        if (o == null) {
            return defaultValue;
        }
        try {
            return (Float) o;
        } catch (ClassCastException e) {
            typeWarning(key, o, "Float", defaultValue, e);
            return defaultValue;
        }
    }

    public double getDouble(E key, double defaultValue) {
        Object o = mMap.get(key);
        if (o == null) {
            return defaultValue;
        }
        try {
            return (Double) o;
        } catch (ClassCastException e) {
            typeWarning(key, o, "Double", defaultValue, e);
            return defaultValue;
        }
    }

    public CharSequence getCharSequence(E key) {
        Object o = mMap.get(key);
        if (o == null) {
            return null;
        }
        try {
            return (CharSequence) o;
        } catch (ClassCastException e) {
            typeWarning(key, o, "CharSequence", e);
            return null;
        }
    }

    public String getString(E key) {
        Object o = mMap.get(key);
        if (o == null) {
            return null;
        }
        try {
            return (String) o;
        } catch (ClassCastException e) {
            typeWarning(key, o, "String", e);
            return null;
        }
    }

    //-----------get for common array----------
    public boolean[] getBooleanArray(E key) {
        Object o = mMap.get(key);
        if (o == null) {
            return null;
        }
        try {
            return (boolean[]) o;
        } catch (ClassCastException e) {
            typeWarning(key, o, "boolean[]", e);
            return null;
        }
    }

    public byte[] getByteArray(E key) {
        Object o = mMap.get(key);
        if (o == null) {
            return null;
        }
        try {
            return (byte[]) o;
        } catch (ClassCastException e) {
            typeWarning(key, o, "byte[]", e);
            return null;
        }
    }

    public char[] getCharArray(E key) {
        Object o = mMap.get(key);
        if (o == null) {
            return null;
        }
        try {
            return (char[]) o;
        } catch (ClassCastException e) {
            typeWarning(key, o, "char[]", e);
            return null;
        }
    }

    public int[] getIntArray(E key) {
        Object o = mMap.get(key);
        if (o == null) {
            return null;
        }
        try {
            return (int[]) o;
        } catch (ClassCastException e) {
            typeWarning(key, o, "int[]", e);
            return null;
        }
    }

    public long[] getLongArray(E key) {
        Object o = mMap.get(key);
        if (o == null) {
            return null;
        }
        try {
            return (long[]) o;
        } catch (ClassCastException e) {
            typeWarning(key, o, "long[]", e);
            return null;
        }
    }

    public float[] getFloatArray(E key) {
        Object o = mMap.get(key);
        if (o == null) {
            return null;
        }
        try {
            return (float[]) o;
        } catch (ClassCastException e) {
            typeWarning(key, o, "float[]", e);
            return null;
        }
    }

    public double[] getDoubleArray(E key) {
        Object o = mMap.get(key);
        if (o == null) {
            return null;
        }
        try {
            return (double[]) o;
        } catch (ClassCastException e) {
            typeWarning(key, o, "double[]", e);
            return null;
        }
    }

    public String[] getStringArray(E key) {
        Object o = mMap.get(key);
        if (o == null) {
            return null;
        }
        try {
            return (String[]) o;
        } catch (ClassCastException e) {
            typeWarning(key, o, "String[]", e);
            return null;
        }
    }

    public CharSequence[] getCharSequenceArray(E key) {
        Object o = mMap.get(key);
        if (o == null) {
            return null;
        }
        try {
            return (CharSequence[]) o;
        } catch (ClassCastException e) {
            typeWarning(key, o, "CharSequence[]", e);
            return null;
        }
    }

    //--------------get for object--------------
    public Object get(E key) {
        return mMap.get(key);
    }

    //--------------put for common--------------
    public void putBoolean(E key, boolean value) {
        mMap.put(key, value);
    }

    public void putByte(E key, byte value) {
        mMap.put(key, value);
    }

    public void putChar(E key, char value) {
        mMap.put(key, value);
    }

    public void putShort(E key, short value) {
        mMap.put(key, value);
    }

    public void putInt(E key, int value) {
        mMap.put(key, value);
    }

    public void putLong(E key, long value) {
        mMap.put(key, value);
    }

    public void putFloat(E key, float value) {
        mMap.put(key, value);
    }

    public void putDouble(E key, double value) {
        mMap.put(key, value);
    }

    public void putString(E key, String value) {
        mMap.put(key, value);
    }

    public void putCharSequence(E key, CharSequence value) {
        mMap.put(key, value);
    }

    //--------------put for common array--------------
    public void putBooleanArray(E key, boolean[] value) {
        mMap.put(key, value);
    }

    public void putByteArray(E key, byte[] value) {
        mMap.put(key, value);
    }

    public void putCharArray(E key, char[] value) {
        mMap.put(key, value);
    }

    public void putShortArray(E key, short[] value) {
        mMap.put(key, value);
    }

    public void putIntArray(E key, int[] value) {
        mMap.put(key, value);
    }

    public void putLongArray(E key, long[] value) {
        mMap.put(key, value);
    }

    public void putFloatArray(E key, float[] value) {
        mMap.put(key, value);
    }

    public void putDoubleArray(E key, double[] value) {
        mMap.put(key, value);
    }

    public void putStringArray(E key, String[] value) {
        mMap.put(key, value);
    }

    public void putCharSequenceArray(E key, CharSequence[] value) {
        mMap.put(key, value);
    }

    //--------------put for object--------------
    public void put(E key, Object value) {
        mMap.put(key, value);
    }

    //----------------put for map---------------
    public void putAll(Pack<E> pack) {
        mMap.putAll(pack.mMap);
    }

    public boolean contains(E key) {
        return mMap.containsKey(key);
    }

    public void clear() {
        mMap.clear();
    }

    //----------------inner for map---------------
    protected final Map<E, Object> getMap() {
        return mMap;
    }

    protected final void putMap(Map<E, Object> map) {
        if (map != null) {
            mMap.putAll(map);
        }
    }

    //------------------------------------------
    // Log a message if the value was non-null but not of the expected type
    private void typeWarning(E key, Object value, String className,
                             Object defaultValue, ClassCastException e) {
        StringBuilder sb = new StringBuilder();
        sb.append("Key ");
        sb.append(key);
        sb.append(" expected ");
        sb.append(className);
        sb.append(" but value was a ");
        sb.append(value.getClass().getName());
        sb.append(".  The default value ");
        sb.append(defaultValue);
        sb.append(" was returned.");
        Logger.w(LOG_TAG, sb.toString());
        Logger.w(LOG_TAG, "Attempt to cast generated internal exception:", e);
    }

    private void typeWarning(E key, Object value, String className,
                             ClassCastException e) {
        typeWarning(key, value, className, "<null>", e);
    }
}
