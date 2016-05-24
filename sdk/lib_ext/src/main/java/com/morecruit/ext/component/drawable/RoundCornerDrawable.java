package com.morecruit.ext.component.drawable;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.view.Gravity;

public class RoundCornerDrawable extends DrawableDecorator {

    private static final int DEFAULT_PAINT_FLAGS =
            Paint.FILTER_BITMAP_FLAG | Paint.DITHER_FLAG;

    private final static ThreadLocal<Paint> sLocalPaint = new ThreadLocal<Paint>() {
        protected Paint initialValue() {
            Paint paint = new Paint(DEFAULT_PAINT_FLAGS);
            paint.setAntiAlias(true);
            return paint;
        }
    };

    private RoundCornerState mState;

    private RoundCorner mRoundCorner;

    private Path mPath = new Path();
    private RectF mTmpRect = new RectF();

    public RoundCornerDrawable(Drawable drawable) {
        this(drawable, 0);
    }

    public RoundCornerDrawable(Drawable drawable, float radius) {
        mState = new RoundCornerState(null, this, null);
        mState.setDrawable(drawable);
        setConstantState(mState);
        setRadius(radius);
        init(drawable);
    }

    public RoundCornerDrawable(Drawable drawable, float[] radiusArray) {
        mState = new RoundCornerState(null, this, null);
        mState.setDrawable(drawable);
        setConstantState(mState);
        setRadius(radiusArray);
        init(drawable);
    }

    private void init(Drawable drawable) {
        if (drawable == null) {
            return;
        }
        if (drawable instanceof BitmapDrawable) {
            mRoundCorner = new RoundCornerBitmap((BitmapDrawable) drawable);
        }
    }

    /**
     * Specify radii for all 4 corners.
     *
     * @param radius
     */
    public void setRadius(float radius) {
        mState.mUseArray = false;
        if (mState.mRadius != radius) {
            mState.mRadius = radius;
            invalidateSelf();
        }
    }

    /**
     * Specify radii for each of the 4 corners. For each corner, the array
     * contains 2 values, [X_radius, Y_radius]. The corners are ordered
     * top-left, top-right, bottom-right, bottom-left
     *
     * @param radiusArray the x and y radii of the corners
     */
    public void setRadius(float[] radiusArray) {
        if (radiusArray != null && radiusArray.length < 8) {
            throw new ArrayIndexOutOfBoundsException("radius array must have >= 8 values");
        }
        mState.mUseArray = true;
        mState.mRadiusArray = radiusArray;
        invalidateSelf();
    }

    /**
     * Set whether this drawable is oval.
     *
     * @param isOval whether is oval.
     */
    public void setOval(boolean isOval) {
        if (mState.mIsOval != isOval) {
            mState.mIsOval = isOval;
            invalidateSelf();
        }
    }

    @Override
    protected void onBoundsChange(Rect bounds) {
        super.onBoundsChange(bounds);
        if (mRoundCorner != null && mRoundCorner.isValid()) {
            mRoundCorner.boundsChanged(bounds);
        }
    }

    @Override
    public void draw(Canvas canvas) {
        boolean isOval = mState.mIsOval;
        float radius = mState.mRadius;
        float[] radiusArray = mState.mRadiusArray;
        if (!isOval && radius == 0 && radiusArray == null) {
            super.draw(canvas);

        } else if (mRoundCorner != null && mRoundCorner.isValid()) {
            Paint paint = sLocalPaint.get();
            Path path = mPath;
            RectF rect = mTmpRect;
            rect.set(getBounds());
            // rest paint.
            paint.setShader(null);
            // apply and draw.
            mRoundCorner.prepareDraw(paint);
            if (isOval) {
                canvas.drawOval(rect, paint);
            } else {
                if (mState.mUseArray) {
                    path.reset();
                    path.addRoundRect(rect, radiusArray, Path.Direction.CW);
                    canvas.drawPath(path, paint);
                } else {
                    canvas.drawRoundRect(rect, radius, radius, paint);
                }
            }

        } else {
            // common way. should handle random exception
            // on hardware accelerate device(which don't support clipPath).
            Path path = mPath;
            RectF rect = mTmpRect;
            // generate round rect path.
            rect.set(getBounds());
            path.reset();
            if (isOval) {
                path.addOval(rect, Path.Direction.CW);
            } else {
                if (mState.mUseArray) {
                    path.addRoundRect(rect, radiusArray, Path.Direction.CW);
                } else {
                    path.addRoundRect(rect, radius, radius, Path.Direction.CW);
                }
            }
            // save and draw (restore).
            int saveCount = canvas.save();
            try {
                canvas.clipPath(path);
                super.draw(canvas);
            } catch (UnsupportedOperationException e) {
                // do nothing.
            } finally {
                canvas.restoreToCount(saveCount);
            }
        }
    }

    static abstract class RoundCorner {

        public abstract void prepareDraw(Paint paint);

        public abstract void boundsChanged(Rect bounds);

        public abstract boolean isValid();
    }

    static class RoundCornerBitmap extends RoundCorner {

        private final Shader mShader;
        private final Shader.TileMode mTileModeX, mTileModeY;
        private final boolean mNoTile;
        private final int mGravity;
        private final int mBitmapWidth, mBitmapHeight;
        private Matrix mMatrix;

        public RoundCornerBitmap(BitmapDrawable bitmapDrawable) {
            Bitmap bitmap = bitmapDrawable.getBitmap();
            Shader.TileMode tileModeX = bitmapDrawable.getTileModeX();
            Shader.TileMode tileModeY = bitmapDrawable.getTileModeY();

            mTileModeX = tileModeX == null ? Shader.TileMode.CLAMP : tileModeX;
            mTileModeY = tileModeY == null ? Shader.TileMode.CLAMP : tileModeY;
            mNoTile = tileModeX == null && tileModeY == null;
            mShader = bitmap == null ? null
                    : new BitmapShader(bitmap, mTileModeX, mTileModeY);
            mGravity = bitmapDrawable.getGravity();
            mBitmapWidth = bitmap == null ? -1 : bitmap.getWidth();
            mBitmapHeight = bitmap == null ? -1 : bitmap.getHeight();
        }

        @Override
        public void prepareDraw(Paint paint) {
            paint.setShader(mShader);
        }

        @Override
        public void boundsChanged(Rect bounds) {
            if (mShader == null) {
                return;
            }
            if (mGravity == Gravity.FILL && mNoTile) {
                // fill when no tile mode.
                int width = bounds.width();
                int height = bounds.height();
                float widthScale = mBitmapWidth <= 0 ? 1 : (float) width / mBitmapWidth;
                float heightScale = mBitmapHeight <= 0 ? 1 : (float) height / mBitmapHeight;

                if (mMatrix == null) {
                    // new in demand.
                    mMatrix = new Matrix();
                }
                mMatrix.reset();
                mMatrix.setScale(widthScale, heightScale);
                mShader.setLocalMatrix(mMatrix);
            }
        }

        @Override
        public boolean isValid() {
            return mShader != null;
        }
    }

    // ------------ constant state -------------
    static class RoundCornerState extends DrawableDecoratorState {

        float mRadius;
        float[] mRadiusArray;
        boolean mUseArray;
        boolean mIsOval;

        RoundCornerState(RoundCornerState orig, DrawableDecorator owner, Resources res) {
            super(orig, owner, res);
            if (orig != null) {
                mRadius = orig.mRadius;
                mRadiusArray = newArray(orig.mRadiusArray);
                mUseArray = orig.mUseArray;
                mIsOval = orig.mIsOval;
            }
        }

        @Override
        public Drawable newDrawable() {
            return new RoundCornerDrawable(this, null);
        }

        @Override
        public Drawable newDrawable(Resources res) {
            return new RoundCornerDrawable(this, res);
        }

        private static float[] newArray(float[] copy) {
            if (copy == null) {
                return null;
            }
            int length = copy.length;
            float[] array = new float[length];
            if (length > 0) {
                System.arraycopy(copy, 0, array, 0, length);
            }
            return array;
        }
    }

    private RoundCornerDrawable(RoundCornerState state, Resources res) {
        mState = new RoundCornerState(state, this, res);
        setConstantState(mState);
    }
}
