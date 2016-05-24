package com.morecruit.ext.component.drawable;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;

import java.io.InputStream;

/**
 * Bitmap drawable which can and explicitly determine and re-determine it's size.
 */
public class ResizeableBitmapDrawable extends BitmapDrawable {

    private int mWidth = -1;
    private int mHeight = -1;

    public ResizeableBitmapDrawable(Resources res, Bitmap bitmap) {
        super(res, bitmap);
    }

    public ResizeableBitmapDrawable(Resources res, InputStream is) {
        super(res, is);
    }

    public ResizeableBitmapDrawable(Resources res, String filepath) {
        super(res, filepath);
    }

    /**
     * Resize this drawable.
     *
     * @param width  Drawable width. <= 0 means use the Bitmap' original width.
     * @param height Drawable height. <= 0 means use the Bitmap' original width.
     */
    public void resize(int width, int height) {
        if (mWidth != width || mHeight != height) {
            mWidth = width;
            mHeight = height;
        }
    }

    @Override
    public int getIntrinsicWidth() {
        return mWidth > 0 ? mWidth : super.getIntrinsicWidth();
    }

    @Override
    public int getIntrinsicHeight() {
        return mHeight > 0 ? mHeight : super.getIntrinsicHeight();
    }
}
