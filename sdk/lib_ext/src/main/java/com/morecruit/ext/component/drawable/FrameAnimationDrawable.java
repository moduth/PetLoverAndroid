package com.morecruit.ext.component.drawable;

import android.graphics.Canvas;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.Drawable;

/**
 * Frame animation drawable.
 */
public class FrameAnimationDrawable extends AnimationDrawable {

    private int mWidth = -1;
    private int mHeight = -1;
    private int mTotalDuration = 0;

    public FrameAnimationDrawable() {
        init();
    }

    public FrameAnimationDrawable(Drawable first, int duration) {
        this();
        addFrame(first, duration);
    }

    private void init() {
        setOneShot(false);
        setVisible(true, false);
    }

    private void computeSizeIfNeeded() {
        if (mWidth <= 0 || mHeight <= 0) {
            Drawable first = getFrame(0);
            if (first != null) {
                mWidth = first.getIntrinsicWidth();
                mHeight = first.getIntrinsicHeight();
            }
        }
    }

    @Override
    public void addFrame(Drawable frame, int duration) {
        super.addFrame(frame, duration);
        computeSizeIfNeeded();
        mTotalDuration += duration;
    }

    /**
     * Get the total duration of this FrameAnimationDrawable.
     *
     * @return Total duration of this FrameAnimationDrawable.
     */
    public int getTotalDuration() {
        return mTotalDuration;
    }

    @Override
    public int getIntrinsicWidth() {
        return mWidth;
    }

    @Override
    public int getIntrinsicHeight() {
        return mHeight;
    }

    @Override
    public void draw(Canvas canvas) {
        super.draw(canvas);
        start();
    }
}
