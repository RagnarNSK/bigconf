package com.r.bigconf.ignite.service.affinity;

import org.apache.ignite.Ignite;
import org.apache.ignite.IgniteCache;

import javax.cache.configuration.Configuration;

public class IgniteAffinityServiceHelper<K,V> {

    private final String cacheName;
    private final Ignite ignite;

    public IgniteAffinityServiceHelper(String cacheName,
                                       Ignite ignite,
                                       Class<K> keyType,
                                       Class<V> valType) {
        this.cacheName = cacheName;
        this.ignite = ignite;
        //TODO disable checkTypes by config
        Configuration<?,?> cfg = ignite.cache(cacheName).getConfiguration(Configuration.class);
        if(cfg.getKeyType() != keyType || cfg.getValueType() != valType){
            throw new IllegalArgumentException("Types not matching");
        }
    }

    public IgniteCache<K, V> getCache() {
        return ignite.cache(cacheName);
    }

    public Ignite getIgnite() {
        return ignite;
    }
}
