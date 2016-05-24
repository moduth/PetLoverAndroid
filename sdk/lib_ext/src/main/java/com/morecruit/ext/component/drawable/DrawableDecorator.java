package com.morecruit.ext.component.drawable;

import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;

/**
 * Container for one drawable. This is used to decorate target drawables. When subclass this,
 * you should call {@link #setConstantState(DrawableDecorator.DrawableDecoratorState)} in constructor.
 */
public class DrawableDecorator extends Drawable implements Drawable.Callback {

    private static final boolean DEFAULT_DITHER = true;

    private DrawableDecoratorState mState;
    private boolean mMutated;

    private int mAlpha = 0xFF;
    private ColorFilter mColorFilter;

    // overrides from Drawable.Callback

    @Override
    public void invalidateDrawable(Drawable who) {
        invalidateSelf();
    }

    @Override
    public void scheduleDrawable(Drawable who, Runnable what, long when) {
        scheduleSelf(what, when);
    }

    @Override
    public void unscheduleDrawable(Drawable who, Runnable what) {
        unscheduleSelf(what);
    }

    // overrides from Drawable

    @Override
    public void draw(Canvas canvas) {
        if (mState.mDrawable != null) {
            mState.mDrawable.draw(canvas);
        }
    }

    @Override
    public int getChangingConfigurations() {
        return super.getChangingConfigurations()
                | mState.mChangingConfigurations
                | mState.mChildrenChangingConfigurations;
    }

    @Override
    public boolean getPadding(Rect padding) {
        if (mState.mDrawable != null) {
            return mState.mDrawable.getPadding(padding);
        } else {
            return super.getPadding(padding);
        }
    }

    @Override
    public boolean setVisible(boolean visible, boolean restart) {
        boolean changed = super.setVisible(visible, restart);
        if (mState.mDrawable != null) {
            mState.mDrawable.setVisible(visible, restart);
        }
        return changed;
    }

    @Override
    public void setAlpha(int alpha) {
        if (mAlpha != alpha) {
            mAlpha = alpha;
            if (mState.mDrawable != null) {
                mState.mDrawable.mutate().setAlpha(alpha);
            }
        }
    }

    @Override
    public int getAlpha() {
        return mAlpha;
    }

    @Override
    public void setColorFilter(ColorFilter cf) {
        if (mColorFilter != cf) {
            mColorFilter = cf;
            if (mState.mDrawable != null) {
                mState.mDrawable.mutate().setColorFilter(cf);
            }
        }
    }

    @Override
    public void setDither(boolean dither) {
        if (mState.mDither != dither) {
            mState.mDither = dither;
            if (mState.mDrawable != null) {
                mState.mDrawable.mutate().setDither(dither);
            }
        }
    }

    @Override
    public int getOpacity() {
        return mState.mDrawable != null ? mState.mDrawable.getOpacity() : PixelFormat.TRANSPARENT;
    }

    @Override
    public boolean isStateful() {
        return mState.mDrawable != null && mState.mDrawable.isStateful();
    }

    @Override
    protected boolean onStateChange(int[] state) {
        return mState.mDrawable != null && mState.mDrawable.setState(state);
    }

    @Override
    protected boolean onLevelChange(int level) {
        return mState.mDrawable != null && mState.mDrawable.setLevel(level);
    }

    @Override
    protected void onBoundsChange(Rect bounds) {
        if (mState.mDrawable != null) {
            mState.mDrawable.setBounds(bounds);
        }
    }

    @Override
    public int getIntrinsicWidth() {
        return mState.mDrawable != null ? mState.mDrawable.getIntrinsicWidth() : -1;
    }

    @Override
    public int getIntrinsicHeight() {
        return mState.mDrawable != null ? mState.mDrawable.getIntrinsicHeight() : -1;
    }

    @Override
    public int getMinimumWidth() {
        return mState.mDrawable != null ? mState.mDrawable.getMinimumWidth() : 0;
    }

    @Override
    public int getMinimumHeight() {
        return mState.mDrawable != null ? mState.mDrawable.getMinimumHeight() : 0;
    }

    @Override
    public ConstantState getConstantState() {
        if (mState.canConstantState()) {
            mState.mChangingConfigurations = getChangingConfigurations();
            return mState;
        }
        return null;
    }

    @Override
    public Drawable mutate() {
        if (!mMutated && super.mutate() == this) {
            mState.mutate();
            mMutated = true;
        }
        return this;
    }

    /**
     * Set the drawable to decorate.
     *
     * @param decor Drawable to decorate.
     */
    public void setDecor(Drawable decor) {
        mState.setDrawable(decor);
        invalidateSelf();
    }

    /**
     * Get the decor drawable.
     *
     * @return Decor drawable.
     */
    public Drawable getDecor() {
        return mState.mDrawable;
    }

    /**
     * A ConstantState that can contain several {@link Drawable}s.
     * <p/>
     * This class was made public to enable testing, and its visibility may change in a future
     * release.
     */
    public abstract static class DrawableDecoratorState extends ConstantState {
        final DrawableDecorator mOwner;
        final Resources mRes;

        int mChangingConfigurations;
        int mChildrenChangingConfigurations;

        Drawable mDrawable;

        boolean mDither = DEFAULT_DITHER;

        boolean mMutated;

        protected DrawableDecoratorState(DrawableDecoratorState orig, DrawableDecorator owner,
                                         Resources res) {
            mOwner = owner;
            mRes = res;

            if (orig != null) {
                mChangingConfigurations = orig.mChangingConfigurations;
                mChildrenChangingConfigurations = orig.mChildrenChangingConfigurations;

                if (orig.mDrawable != null) {
                    mDrawable = orig.mDrawable.getConstantState().newDrawable(res);
                    mDrawable.setCallback(owner);
                }

                mDither = orig.mDither;
                mMutated = orig.mMutated;
            }
        }

        @Override
        public int getChangingConfigurations() {
            return mChangingConfigurations | mChildrenChangingConfigurations;
        }

        public final void setDrawable(Drawable dr) {
            if (mDrawable != dr) {
                if (mDrawable != null) {
                    mDrawable.setCallback(null);
                }
                if (dr != null) {
                    dr.setCallback(mOwner);
                    // update drawable.
                    dr.mutate();
                    dr.setAlpha(mOwner.mAlpha);
                    dr.setVisible(mOwner.isVisible(), true);
                    dr.setDither(mDither);
                    dr.setColorFilter(mOwner.mColorFilter);
                    dr.setState(mOwner.getState());
                    dr.setLevel(mOwner.getLevel());
                    dr.setBounds(mOwner.getBounds());
                }
                mDrawable = dr;

                mChildrenChangingConfigurations = dr != null ?
                        dr.getChangingConfigurations() : 0;
            }
        }

        public final Drawable getDrawable() {
            return mDrawable;
        }

        final void mutate() {
            if (mDrawable != null) {
                mDrawable.mutate();
            }

            mMutated = true;
        }

        public boolean canConstantState() {
            boolean can = true;
            if (mDrawable != null) {
                can = mDrawable.getConstantState() != null;
            }
            return can;
        }
    }

    /**
     * Set the constant state for this.
     *
     * @param state Constant state.
     */
    protected void setConstantState(DrawableDecoratorState state) {
        mState = state;
    }
}
