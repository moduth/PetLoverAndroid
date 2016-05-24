package com.morecruit.ext.utils;

/**
 * @author markzhai on 16/2/29
 * @version 1.0.0
 */
public final class ConstantUtils {

    /**
     * hashCode()使用的系数
     */
    public static final int HASHCODE_FACTOR = 31;

    /**
     * 千(十进制)
     */
    public static final int THOUSAND = 1000;

    /**
     * 百(十进制)
     */
    public static final int HUNDRED = 100;

    /**
     * 十(十进制)
     */
    public static final int TEN = 10;

    /**
     * 千(二进制)
     */
    public static final int KILO = 1024;

    /**
     * 空字符串，用于避免字符串为null的场合
     */
    public static final String BLANK_STRING = "";

    /**
     * 临时文件扩展名
     */
    public static final String TMP_EXT = ".tmp";

    /**
     * 一分钟有多少毫秒
     */
    public static final long MILLS_PER_MIN = 60 * 1000;

    /**
     * 一天有多少毫秒
     */
    public static final long MILLS_PER_DAY = 24 * 60 * 60 * 1000;

    /**
     * 一小时有多少毫秒
     */
    public static final long MILLS_PER_HOUR = 60 * 60 * 1000;

    /**
     * 一分钟有多少秒
     */
    public static final int SECONDS_PER_MINUTE = 60;

    /**
     * 一秒有多少毫秒
     */
    public static final int MILLIS_PER_SECOND = 1000;

    /**
     * 高16位掩码
     */
    public static final int MASK_HIGH_16_BITS = 0xFFFF0000;

    /**
     * 低16位掩码
     */
    public static final int MASK_LOW_16_BITS = 0x0000FFFF;

    /**
     * 16位偏移
     */
    public static final int OFFSET_16_BITS = 16;

    public static final String UTF_8 = "UTF-8";
}
