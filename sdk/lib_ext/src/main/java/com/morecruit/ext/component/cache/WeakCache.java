package com.morecruit.ext.component.cache;

import java.lang.ref.ReferenceQueue;
import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.Map;

/**
 * 线程安全的弱cache，内部使用{@link ReferenceQueue}实现
 * <p/>
 * Created by markzhai on 2015/8/3.
 */
public class WeakCache<K, V> {

    private final Map<K, Entry<K, V>> mWeakMap = new HashMap<K, Entry<K, V>>();
    private ReferenceQueue<V> mQueue = new ReferenceQueue<V>();

    public WeakCache() {
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
        Entry<K, V> entry = (Entry<K, V>) mQueue.poll();
        while (entry != null) {
            mWeakMap.remove(entry.mKey);
            entry = (Entry<K, V>) mQueue.poll();
        }
    }

    /**
     * 对应key是否在cache中.
     *
     * @param key 需要查找的key.
     */
    public synchronized final boolean containsKey(K key) {
        cleanUpWeakMap();
        return mWeakMap.containsKey(key);
    }

    /**
     * 将键值对放到cache里.
     *
     * @return 如果对应key存在前值，则返回前值.
     */
    public synchronized final V put(K key, V value) {
        cleanUpWeakMap();
        Entry<K, V> entry = mWeakMap.put(
                key, new Entry<K, V>(key, value, mQueue));
        return entry == null ? null : entry.get();
    }

    /**
     * 获得key对应的value.
     *
     * @param key 需要查找的key.
     * @return key对应的value.
     */
    public synchronized final V get(K key) {
        cleanUpWeakMap();
        Entry<K, V> entry = mWeakMap.get(key);
        return entry == null ? null : entry.get();
    }

    /**
     * 删除key对应的键值对.
     *
     * @param key 需要查找的key.
     * @return 删掉的value，不存在则返回null.
     */
    public synchronized final V remove(K key) {
        cleanUpWeakMap();
        Entry<K, V> entry = mWeakMap.remove(key);
        return entry == null ? null : entry.get();
    }

    /**
     * 清除整个cache
     */
    public synchronized final void evictAll() {
        mWeakMap.clear();
        mQueue = new ReferenceQueue<V>();
    }
}
