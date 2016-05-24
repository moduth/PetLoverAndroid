package com.morecruit.ext.component.cache;

import java.lang.ref.ReferenceQueue;
import java.lang.ref.WeakReference;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * <p>线程安全的链式WeakCache. 和{@link WeakCache}相比能提供一些API如{@link #entryRemoved}，
 * 内部使用{@link ReferenceQueue}实现</p>
 * Created by markzhai on 2015/8/3.
 */
public class LinkedWeakCache<K, V> {

    private final Map<K, Entry<K, V>> mWeakMap =
            new LinkedHashMap<K, Entry<K, V>>();
    private ReferenceQueue<V> mQueue = new ReferenceQueue<V>();

    public LinkedWeakCache() {
    }

    private static class Entry<K, V> extends WeakReference<V> {
        K mKey;

        public Entry(K key, V value, ReferenceQueue<V> queue) {
            super(value, queue);
            mKey = key;
        }
    }

    @SuppressWarnings("unchecked")
    private void cleanUpWeakMap() {
        while (true) {
            Entry<K, V> entry = (Entry<K, V>) mQueue.poll();
            if (entry == null) {
                break;
            }
            synchronized (this) {
                mWeakMap.remove(entry.mKey);
            }
            // oldValue must be null while poll out from reference queue.
            entryRemoved(true, entry.mKey, null, null);
        }
    }

    /**
     * 对应key是否在cache中.
     *
     * @param key 需要查找的key.
     */
    public final boolean containsKey(K key) {
        cleanUpWeakMap();
        synchronized (this) {
            return mWeakMap.containsKey(key);
        }
    }

    /**
     * 将键值对放到cache里.
     *
     * @return 如果对应key存在前值，则返回前值.
     */
    public final V put(K key, V value) {
        cleanUpWeakMap();

        Entry<K, V> prev;
        synchronized (this) {
            prev = mWeakMap.put(key, new Entry<K, V>(key, value, mQueue));
        }
        if (prev != null) {
            entryRemoved(false, prev.mKey, prev.get(), value);
        }
        return prev == null ? null : prev.get();
    }

    /**
     * 获得key对应的value.
     *
     * @param key 需要查找的key.
     * @return key对应的value.
     */
    public final V get(K key) {
        cleanUpWeakMap();
        synchronized (this) {
            Entry<K, V> entry = mWeakMap.get(key);
            return entry == null ? null : entry.get();
        }
    }

    /**
     * 删除key对应的键值对.
     *
     * @param key 需要查找的key.
     * @return 删掉的value，不存在则返回null.
     */
    public final V remove(K key) {
        cleanUpWeakMap();

        Entry<K, V> entry;
        synchronized (this) {
            entry = mWeakMap.remove(key);
        }
        if (entry != null) {
            entryRemoved(false, entry.mKey, entry.get(), null);
        }
        return entry == null ? null : entry.get();
    }

    /**
     * 清除整个cache，会对每个被删除的entry call{@link #entryRemoved}.
     */
    public final void evictAll() {
        while (true) {
            Entry<K, V> entry;
            synchronized (this) {
                if (mWeakMap.isEmpty()) {
                    mQueue = new ReferenceQueue<V>();
                    break;
                }
                K key = mWeakMap.keySet().iterator().next();
                entry = mWeakMap.remove(key);
            }
            if (entry != null) {
                entryRemoved(true, entry.mKey, entry.get(), null);
            }
        }
    }

    /**
     * 调用在被删除的entry上, 可能通过{@link #evictAll}被清空,
     * 也可能是单个的{@link #remove}, 或者是通过{@link #put}被替换了.
     * 默认实现什么都没有做.
     * <p>
     * <p>该方法不是synchronization的，其他线程在该方法执行的时候也可能访问cache.
     *
     * @param evicted  返回true如果该entry是因为引用释放被收回的，
     *                 返回false如果删除是因为{@link #put} 或 {@link #remove}.
     * @param oldValue {@code key}对应的旧值, 如果该value还没有被释放.
     * @param newValue {@code key}对应的新值, 如果存在的话.
     *                 如果该值非空，则此次移除是因为 {@link #put}.
     *                 否则是由清空或者 {@link #remove} 导致的.
     */
    protected void entryRemoved(boolean evicted, K key, V oldValue, V newValue) {
    }
}
