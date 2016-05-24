package com.morecruit.ext.component.cache;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.util.TypedValue;

import com.morecruit.ext.utils.Singleton;

import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.Map;

/**
 * 资源的缓存服务，和系统的 {@link Resources} 相比, 提供了灵活控制drawable生成.
 *
 * @author markzhai on 2015/8/4.
 */
public class ResourcesCacheService {

    private final static Bitmap.Config sDefaultConfig = Bitmap.Config.RGB_565;

    private final Resources mResources;

    private final Map<String, WeakReference<Drawable.ConstantState>> mDrawableCache
            = new HashMap<String, WeakReference<Drawable.ConstantState>>();

    private final TypedValue mTmpValue = new TypedValue();
    private final BitmapFactory.Options mTmpOpts = new BitmapFactory.Options();

    public ResourcesCacheService(Context context) {
        mResources = context.getApplicationContext().getResources();
    }

    /**
     * Get the drawable with corresponding resource id.
     *
     * @param id Resource id.
     */
    public Drawable getDrawable(int id) {
        return getDrawable(id, null, 1);
    }

    /**
     * Get the drawable with corresponding resource id and config.
     *
     * @param id     Resource id.
     * @param config Config for Bitmap.
     */
    public Drawable getDrawable(int id, Bitmap.Config config) {
        return getDrawable(id, config, 1);
    }

    /**
     * Get the drawable with corresponding resource id and required sample size.
     *
     * @param id         Resource id.
     * @param sampleSize Sample size for Bitmap.
     */
    public Drawable getDrawable(int id, int sampleSize) {
        return getDrawable(id, null, sampleSize);
    }

    /**
     * Get the drawable with corresponding resource id, config and required sample size.
     *
     * @param id         Resource id.
     * @param config     Config for Bitmap.
     * @param sampleSize Sample size for Bitmap.
     */
    public Drawable getDrawable(int id, Bitmap.Config config, int sampleSize) {
        if (config == null) {
            config = sDefaultConfig;
        }
        Drawable drawable = null;
        if (config != Bitmap.Config.ARGB_8888 || sampleSize > 1) {
            // 8888 is android resources' default config, so ignore.
            synchronized (mTmpValue) {
                TypedValue value = mTmpValue;
                getValue(id, value, true);
                // prepare options.
                BitmapFactory.Options opts = mTmpOpts;
                opts.inPreferredConfig = config;
                opts.inSampleSize = sampleSize;
                opts.inDither = config == Bitmap.Config.RGB_565;
                drawable = loadDrawable(value, id, opts);
            }
        }
        return drawable != null ? drawable : mResources.getDrawable(id);
    }

    private Drawable loadDrawable(TypedValue value, int id, BitmapFactory.Options opts) {

        boolean isColorDrawable = false;
        if (value.type >= TypedValue.TYPE_FIRST_COLOR_INT &&
                value.type <= TypedValue.TYPE_LAST_COLOR_INT) {
            isColorDrawable = true;
        }
        if (isColorDrawable) {
            return null;
        }

        String file = value.string.toString();
        if (file.endsWith(".xml")) {
            return null;
        }

        final String key = getCachedKey(value, opts);

        Drawable dr = getCachedDrawable(mDrawableCache, key);

        if (dr != null) {
            // hit cache.
            return dr;
        }

        try {
            InputStream is = mResources.getAssets().openNonAssetFd(
                    value.assetCookie, file).createInputStream();
            dr = Drawable.createFromResourceStream(mResources, value, is,
                    file, opts);
            is.close();
        } catch (Exception e) {
            // empty.
        }

        if (dr != null) {
            dr.setChangingConfigurations(value.changingConfigurations);
            Drawable.ConstantState cs = dr.getConstantState();
            if (cs != null) {
                synchronized (mTmpValue) {
                    mDrawableCache.put(key, new WeakReference<Drawable.ConstantState>(cs));
                }
            }
        }

        return dr;
    }

    private Drawable getCachedDrawable(
            Map<String, WeakReference<Drawable.ConstantState>> drawableCache,
            String key) {
        synchronized (mTmpValue) {
            WeakReference<Drawable.ConstantState> wr = drawableCache.get(key);
            if (wr != null) {   // we have the key
                Drawable.ConstantState entry = wr.get();
                if (entry != null) {
                    return newDrawable(entry);
                } else {  // our entry has been purged
                    drawableCache.remove(key);
                }
            }
        }
        return null;
    }

    private void getValue(int id, TypedValue outValue, boolean resolveRefs) {
        mResources.getValue(id, outValue, resolveRefs);
    }

    private String getCachedKey(TypedValue value, BitmapFactory.Options opts) {
        final long key = (((long) value.assetCookie) << 32) | value.data;
        String cachedKey = String.valueOf(key);
        if (opts != null && opts.inPreferredConfig != null) {
            cachedKey = cachedKey + '_' + opts.inPreferredConfig;
        }
        if (opts != null && opts.inSampleSize > 1) {
            cachedKey = cachedKey + '_' + opts.inSampleSize;
        }
        return cachedKey;
    }

    private Drawable newDrawable(Drawable.ConstantState cs) {
        return cs.newDrawable(mResources);
    }

    // 单例
    private final static Singleton<ResourcesCacheService, Context> sSingleton = new Singleton<ResourcesCacheService, Context>() {
        @Override
        protected ResourcesCacheService create(Context context) {
            return new ResourcesCacheService(context);
        }
    };

    /**
     * Get the default {@link ResourcesCacheService}.
     *
     * @param context Application context.
     * @return Default {@link ResourcesCacheService}.
     */
    public static ResourcesCacheService getDefault(Context context) {
        return sSingleton.get(context);
    }
}
