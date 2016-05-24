package com.morecruit.ext.component.drawable;

import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;

/**
 * A single drawable container which supports multiple scale strategies.
 */
public class ScaleDrawable extends DrawableDecorator {

    /**
     * Scale type definitions.
     */
    public enum ScaleType {

        CROP_CENTER(0),

        CROP_START(1),

        CROP_END(2),

        FIT_CENTER(3),

        FIT_START(4),

        FIT_END(5),

        MATCH_WIDTH_TOP(6),

        MATCH_WIDTH_BOTTOM(7),

        MATCH_WIDTH_CENTER(8),

        CENTER(9),

        CROP_BY_PIVOT(10);

        ScaleType(int ni) {
            nativeInt = ni;
        }

        final int nativeInt;
    }

    private static final float PIVOT_DEFAULT_VALUE = 0f;

    private ScaleState mScaleState;
    private ScaleType mScaleType;
    private Matrix mDrawMatrix;

    private float mPivotXRate = PIVOT_DEFAULT_VALUE;
    private float mPivotYRate = PIVOT_DEFAULT_VALUE;

    private Rect mTmpRect = new Rect();

    /**
     * Construct a identical ScaleDrawable.
     *
     * @param drawable Drawable to contain.
     */
    public ScaleDrawable(Drawable drawable) {
        this(drawable, null);
    }

    /**
     * Construct a ScaleDrawable with corresponding scale type.
     *
     * @param drawable  Drawable to contain.
     * @param scaleType Scale type to apply.
     */
    public ScaleDrawable(Drawable drawable, ScaleType scaleType) {
        mScaleState = new ScaleState(null, this, null);
        mScaleState.setDrawable(drawable);
        setConstantState(mScaleState);
        setScaleType(scaleType);
    }

    /**
     * Set which scale type to apply, null for no scale.
     *
     * @param scaleType Scale type to apply.
     */
    public void setScaleType(ScaleType scaleType) {
        if (mScaleType != scaleType) {
            mScaleType = scaleType;
            updateDrawMatrix();
        }
    }

    /**
     * Set the pivot for {@link ScaleType#CROP_BY_PIVOT}.
     *
     * @param x X pivot, should be [0, 1].
     * @param y Y pivot, should be [0, 1].
     */
    public void setPivot(float x, float y) {
        if (mPivotXRate != x || mPivotYRate != y) {
            this.mPivotXRate = x;
            this.mPivotYRate = y;
            updateDrawMatrix();
        }
    }

    @Override
    protected void onBoundsChange(Rect bounds) {
        final int width = getIntrinsicWidth();
        final int height = getIntrinsicHeight();
        Rect tmpBounds = bounds;
        if (width > 0 && height > 0) {
            tmpBounds = mTmpRect;
            tmpBounds.set(0, 0, width, height);
        }
        super.onBoundsChange(tmpBounds);
        updateDrawMatrix();
    }

    @Override
    public void draw(Canvas canvas) {
        Matrix drawMatrix = mDrawMatrix;
        if (drawMatrix == null || drawMatrix.isIdentity()) {
            super.draw(canvas);

        } else {
            int saveCount = canvas.getSaveCount();
            canvas.save();

            canvas.concat(drawMatrix);
            super.draw(canvas);

            canvas.restoreToCount(saveCount);
        }
    }

    @Override
    public int getMinimumWidth() {
        return 0;
    }

    @Override
    public int getMinimumHeight() {
        return 0;
    }

    @Override
    public Drawable.ConstantState getConstantState() {
        if (mScaleState.canConstantState()) {
            mScaleState.mChangingConfigurations = getChangingConfigurations();
            return mScaleState;
        }
        return null;
    }

    private void updateDrawMatrix() {
        final ScaleType scaleType = mScaleType;
        if (scaleType == null) {
            if (mDrawMatrix != null) mDrawMatrix.reset();
            return;
        }

        if (mDrawMatrix == null) {
            mDrawMatrix = new Matrix();
        }

        int srcWidth = getIntrinsicWidth();
        int srcHeight = getIntrinsicHeight();

        int dstWidth = getBounds().width();
        int dstHeight = getBounds().height();

        //boolean fits = (srcWidth < 0 || dstWidth == srcWidth) &&
        //        (srcHeight < 0 || dstHeight == srcHeight);

        switch (scaleType) {
        case CROP_CENTER: {
            float scale;
            float dx = 0, dy = 0;

            if (srcWidth * dstHeight > dstWidth * srcHeight) {
                scale = (float) dstHeight / (float) srcHeight;
                dx = (dstWidth - srcWidth * scale) * 0.5f;
            } else {
                scale = (float) dstWidth / (float) srcWidth;
                dy = (dstHeight - srcHeight * scale) * 0.5f;
            }

            mDrawMatrix.setScale(scale, scale);
            mDrawMatrix.postTranslate((int) (dx + 0.5f), (int) (dy + 0.5f));
            break;
        }

        case CROP_START: {
            float scale;
            float dx = 0, dy = 0;

            if (srcWidth * dstHeight > dstWidth * srcHeight) {
                scale = (float) dstHeight / (float) srcHeight;
                dx = 0;
            } else {
                scale = (float) dstWidth / (float) srcWidth;
                dy = 0;
            }

            mDrawMatrix.setScale(scale, scale);
            mDrawMatrix.postTranslate((int) (dx + 0.5f), (int) (dy + 0.5f));
            break;
        }

        case CROP_END: {
            float scale;
            float dx = 0, dy = 0;

            if (srcWidth * dstHeight > dstWidth * srcHeight) {
                scale = (float) dstHeight / (float) srcHeight;
                dx = (dstWidth - srcWidth * scale) * 1.0f;
            } else {
                scale = (float) dstWidth / (float) srcWidth;
                dy = (dstHeight - srcHeight * scale) * 1.0f;
            }

            mDrawMatrix.setScale(scale, scale);
            mDrawMatrix.postTranslate((int) (dx + 0.5f), (int) (dy + 0.5f));
            break;
        }

        case FIT_CENTER: {
            float scale;
            float dx = 0, dy = 0;

            if (srcWidth * dstHeight > dstWidth * srcHeight) {
                scale = (float) dstWidth / (float) srcWidth;
                dy = (dstHeight - srcHeight * scale) * 0.5f;
            } else {
                scale = (float) dstHeight / (float) srcHeight;
                dx = (dstWidth - srcWidth * scale) * 0.5f;
            }

            mDrawMatrix.setScale(scale, scale);
            mDrawMatrix.postTranslate((int) (dx + 0.5f), (int) (dy + 0.5f));
            break;
        }

        case FIT_START: {
            float scale;
            float dx = 0, dy = 0;

            if (srcWidth * dstHeight > dstWidth * srcHeight) {
                scale = (float) dstWidth / (float) srcWidth;
                dy = 0;
            } else {
                scale = (float) dstHeight / (float) srcHeight;
                dx = 0;
            }

            mDrawMatrix.setScale(scale, scale);
            mDrawMatrix.postTranslate((int) (dx + 0.5f), (int) (dy + 0.5f));
            break;
        }

        case FIT_END: {
            float scale;
            float dx = 0, dy = 0;

            if (srcWidth * dstHeight > dstWidth * srcHeight) {
                scale = (float) dstWidth / (float) srcWidth;
                dy = (dstHeight - srcHeight * scale) * 1.0f;
            } else {
                scale = (float) dstHeight / (float) srcHeight;
                dx = (dstWidth - srcWidth * scale) * 1.0f;
            }

            mDrawMatrix.setScale(scale, scale);
            mDrawMatrix.postTranslate((int) (dx + 0.5f), (int) (dy + 0.5f));
        }

        case MATCH_WIDTH_TOP: {
            float scale;
            float dx = 0, dy = 0;

            scale = (float) dstWidth / (float) srcWidth;

            mDrawMatrix.setScale(scale, scale);
            mDrawMatrix.postTranslate((int) (dx + 0.5f), (int) (dy + 0.5f));
            break;
        }

        case MATCH_WIDTH_BOTTOM: {
            float scale;
            float dx = 0, dy = 0;

            scale = (float) dstWidth / (float) srcWidth;
            dy = (dstHeight - srcHeight * scale) * 1.0f;

            mDrawMatrix.setScale(scale, scale);
            mDrawMatrix.postTranslate((int) (dx + 0.5f), (int) (dy + 0.5f));
            break;
        }

        case MATCH_WIDTH_CENTER: {
            float scale;
            float dx = 0, dy = 0;

            scale = (float) dstWidth / (float) srcWidth;
            dy = (dstHeight - srcHeight * scale) * 0.5f;

            mDrawMatrix.setScale(scale, scale);
            mDrawMatrix.postTranslate((int) (dx + 0.5f), (int) (dy + 0.5f));
            break;
        }

        case CENTER: {
            float dx = 0, dy = 0;

            dy = (dstHeight - srcHeight) * 0.5f;
            dx = (dstWidth - srcWidth) * 0.5f;

            mDrawMatrix.postTranslate((int) (dx + 0.5f), (int) (dy + 0.5f));
            break;
        }

        case CROP_BY_PIVOT: {
            float scale;

            if (srcWidth * dstHeight > dstWidth * srcHeight) {
                scale = (float) dstHeight / (float) srcHeight;
            } else {
                scale = (float) dstWidth / (float) srcWidth;
            }

            float halfWidth = dstWidth * 0.5f;
            float halfHeight = dstHeight * 0.5f;

            float startX = 0;
            float startY = 0;
            srcWidth *= scale;
            srcHeight *= scale;

            float pivotX = mPivotXRate * srcWidth;
            float pivotY = mPivotYRate * srcHeight;

            if (srcWidth > dstWidth && pivotX > halfWidth) { //如果原图宽度超过最大宽度并且中心点是在当前显示区域一半以外才去偏移中心点
                startX = pivotX - halfWidth;

                //如果其实位置太右,则会有部分没显示出来
                float maxOffsetX = srcWidth - dstWidth;
                startX = Math.min(maxOffsetX, startX);
            }

            if (srcHeight > dstHeight && pivotY > halfHeight) {
                startY = pivotY - halfHeight;
                float maxOffsetY = srcHeight - dstHeight;
                startY = Math.min(maxOffsetY, startY);
            }

            mDrawMatrix.setScale(scale, scale);
            mDrawMatrix.postTranslate((int) (startX + 0.5f) * -1, (int) (startY + 0.5f) * -1);
            break;
        }
        }
    }

    static class ScaleState extends DrawableDecoratorState {

        ScaleState(ScaleState orig, ScaleDrawable owner, Resources res) {
            super(orig, owner, res);
        }

        @Override
        public Drawable newDrawable() {
            return new ScaleDrawable(this, null);
        }

        @Override
        public Drawable newDrawable(Resources res) {
            return new ScaleDrawable(this, res);
        }
    }

    private ScaleDrawable(ScaleState state, Resources res) {
        mScaleState = new ScaleState(state, this, res);
        setConstantState(mScaleState);
    }
}
