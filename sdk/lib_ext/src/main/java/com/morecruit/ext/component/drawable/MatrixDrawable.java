package com.morecruit.ext.component.drawable;

import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.drawable.Drawable;

/**
 * A single drawable container which supports {@link Matrix} transform.
 */
public class MatrixDrawable extends DrawableDecorator {

    private final MatrixState mState;
    private final Matrix mMatrix = new Matrix();

    /**
     * Construct a MatrixDrawable container.
     *
     * @param drawable Drawable to contain.
     */
    public MatrixDrawable(Drawable drawable) {
        this(drawable, null);
    }

    /**
     * Construct a MatrixDrawable with corresponding matrix.
     *
     * @param drawable Drawable to contain.
     * @param matrix   Matrix to apply.
     */
    public MatrixDrawable(Drawable drawable, Matrix matrix) {
        mState = new MatrixState(null, this, null);
        mState.setDrawable(drawable);
        setConstantState(mState);
        setMatrix(matrix);
    }

    /**
     * Return the drawable' matrix. This is applied to it's inner drawable when it is drawn.
     * Do not change this matrix in place. If you want a different matrix
     * applied to the drawable, be sure to call {@link #setMatrix}.
     */
    public Matrix getMatrix() {
        return mMatrix;
    }

    /**
     * Set the drawable' matrix, This is applied to it's inner drawable when it is drawn.
     *
     * @param matrix Matrix to apply.
     */
    public void setMatrix(Matrix matrix) {
        // collaps null and identity to just null
        if (matrix != null && matrix.isIdentity()) {
            matrix = null;
        }

        // don't invalidate unless we're actually changing our matrix
        if (matrix == null && !mMatrix.isIdentity() ||
                matrix != null && !mMatrix.equals(matrix)) {
            mMatrix.set(matrix);
            invalidateSelf();
        }
    }

    @Override
    public void draw(Canvas canvas) {
        if (mMatrix.isIdentity()) {
            super.draw(canvas);
        } else {
            int saveCount = canvas.getSaveCount();
            canvas.save();

            canvas.concat(mMatrix);
            super.draw(canvas);

            canvas.restoreToCount(saveCount);
        }
    }

    static class MatrixState extends DrawableDecoratorState {

        MatrixState(DrawableDecoratorState orig, DrawableDecorator owner, Resources res) {
            super(orig, owner, res);
        }

        @Override
        public Drawable newDrawable() {
            return new MatrixDrawable(this, null);
        }

        @Override
        public Drawable newDrawable(Resources res) {
            return new MatrixDrawable(this, res);
        }
    }

    private MatrixDrawable(MatrixState state, Resources res) {
        mState = new MatrixState(state, this, res);
        setConstantState(mState);
    }
}
