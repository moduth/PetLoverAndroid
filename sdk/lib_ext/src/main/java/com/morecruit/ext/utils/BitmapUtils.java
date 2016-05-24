package com.morecruit.ext.utils;

import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.media.ExifInterface;
import android.text.TextUtils;

import java.io.ByteArrayOutputStream;
import java.io.File;

/**
 * Bitmap工具类
 *
 * @author markzhai on 16/3/5
 */
public final class BitmapUtils {

    private static final int DEFAULT_QUALITY = 90;

    private BitmapUtils() {
        // static usage.
    }

    /**
     * Compress bitmap to byte array.
     *
     * @param bitmap Bitmap.
     * @return Byte array of bitmap.
     */
    public static byte[] compressToBytes(Bitmap bitmap) {
        return compressToBytes(bitmap, DEFAULT_QUALITY, bitmap.hasAlpha() ? Bitmap.CompressFormat.PNG : Bitmap.CompressFormat.JPEG);
    }

    /**
     * Compress bitmap to byte array.
     *
     * @param bitmap Bitmap.
     * @param format Compress format.
     * @return Byte array of bitmap.
     */
    public static byte[] compressToBytes(Bitmap bitmap, Bitmap.CompressFormat format) {
        return compressToBytes(bitmap, DEFAULT_QUALITY, format);
    }

    /**
     * Compress bitmap to byte array.
     *
     * @param bitmap  Bitmap.
     * @param quality Compress quality.
     * @param format  Compress format.
     * @return Byte array of bitmap.
     */
    public static byte[] compressToBytes(Bitmap bitmap, int quality, Bitmap.CompressFormat format) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream(65536);
        bitmap.compress(format, quality, baos);
        return baos.toByteArray();
    }

    /**
     * Rotate bitmap by it's exif info.
     *
     * @param bitmap Bitmap.
     * @param path   Path of bitmap.
     * @return Rotated bitmap, may be the original one if rotation is zero.
     */
    @SuppressLint("NewApi")
    public static Bitmap rotateBitmapByExif(Bitmap bitmap, String path) {
        if (bitmap == null || TextUtils.isEmpty(path)) {
            return bitmap;
        }

        // handle exif info.
        ExifInterface exif = null;
        try {
            exif = (new File(path)).exists() ? new ExifInterface(path) : null;
        } catch (Throwable e) {
            // ignore
        }
        if (exif == null) {
            return bitmap;
        }
        return rotateBitmapByExif(bitmap, exif);
    }

    /**
     * Rotate bitmap by it's exif info.
     *
     * @param bitmap Bitmap.
     * @param exif   Exif of this image.
     * @return Rotated bitmap, may be the original one if rotation is zero.
     */
    @SuppressLint("NewApi")
    public static Bitmap rotateBitmapByExif(Bitmap bitmap, ExifInterface exif) {
        if (bitmap == null || exif == null) {
            return bitmap;
        }

        int orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_UNDEFINED);
        int rotation = 0;
        switch (orientation) {
            case ExifInterface.ORIENTATION_ROTATE_90:
                rotation = 90;
                break;

            case ExifInterface.ORIENTATION_ROTATE_180:
                rotation = 180;
                break;

            case ExifInterface.ORIENTATION_ROTATE_270:
                rotation = 270;
                break;
        }
        return rotateBitmap(bitmap, rotation);
    }

    /**
     * Rotate bitmap with corresponding rotation.
     *
     * @param bitmap   Bitmap.
     * @param rotation Rotation.
     * @return Rotated bitmap, may be the original one if rotation is zero.
     */
    public static Bitmap rotateBitmap(Bitmap bitmap, int rotation) {
        rotation = rotation % 360;
        if (rotation == 0) {
            // do nothing.
            return bitmap;
        }

        boolean rotateDimension = (rotation > 45 && rotation < 135)
                || (rotation > 225 && rotation < 315);
        int width = !rotateDimension ? bitmap.getWidth() : bitmap.getHeight();
        int height = !rotateDimension ? bitmap.getHeight() : bitmap.getWidth();

        Bitmap newBitmap = null;
        try {
            newBitmap = Bitmap.createBitmap(width, height, bitmap.getConfig());
        } catch (Throwable e) {
            // do nothing.
        }
        if (newBitmap == null || newBitmap == bitmap) {
            // no enough memory or original bitmap returns.
            return bitmap;
        }

        Canvas canvas = new Canvas(newBitmap);
        int dx = (width - bitmap.getWidth()) / 2, dy = (height - bitmap.getHeight()) / 2;
        if (dx != 0 || dy != 0) {
            canvas.translate(dx, dy);
        }
        canvas.rotate(rotation, bitmap.getWidth() / 2, bitmap.getHeight() / 2);
        canvas.drawBitmap(bitmap, 0, 0, null);
        // recycle prev bitmap.
        bitmap.recycle();

        return newBitmap;
    }
}
