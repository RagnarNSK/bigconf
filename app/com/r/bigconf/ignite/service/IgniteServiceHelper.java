package com.r.bigconf.ignite.service;

import com.r.bigconf.ignite.IgniteHolder;
import org.apache.ignite.Ignite;
import org.apache.ignite.IgniteCache;
import org.apache.ignite.cache.CacheMode;
import org.apache.ignite.configuration.CacheConfiguration;

public class IgniteServiceHelper<K,V> {

    private final String cacheName;
    private final IgniteHolder igniteHolder;

    public IgniteServiceHelper(String cacheName,
                               IgniteHolder igniteHolder,
                               CacheMode mode,
                               Class<K> keyType,
                               Class<V> valType) {
        this.cacheName = cacheName;
        this.igniteHolder = igniteHolder;
        CacheConfiguration<K, V> cfg = new CacheConfiguration<>();
        cfg.setName(cacheName);
        cfg.setCacheMode(mode);
        cfg.setTypes(keyType, valType);
        igniteHolder.getIgnite().getOrCreateCache(cfg);
    }

    public Ignite getIgnite(){
        return igniteHolder.getIgnite();
    }

    public IgniteCache<K, V> getCache() {
        return igniteHolder.getIgnite().cache(cacheName);
    }
}
