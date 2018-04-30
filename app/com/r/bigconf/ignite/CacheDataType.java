package com.r.bigconf.ignite;

import org.apache.ignite.cache.CacheAtomicityMode;
import org.apache.ignite.cache.CacheMode;
import org.apache.ignite.configuration.CacheConfiguration;

import javax.cache.expiry.CreatedExpiryPolicy;
import javax.cache.expiry.Duration;

public enum CacheDataType {
    USERS_DATA(Constants.PERSISTED){
        @Override
        public <K, V> void configure(CacheConfiguration<K, V> cfg) {
            cfg.setCacheMode(CacheMode.REPLICATED);
            cfg.setBackups(1);
            cfg.setDataRegionName(getRegionName());
            cfg.setAtomicityMode(CacheAtomicityMode.TRANSACTIONAL);
        }
    },
    CONFERENCE_DATA(Constants.NOT_PERSISTED){
        @Override
        public <K, V> void configure(CacheConfiguration<K, V> cfg) {
            cfg.setCacheMode(CacheMode.PARTITIONED);
            cfg.setBackups(0);
            cfg.setDataRegionName(getRegionName());
            cfg.setAtomicityMode(CacheAtomicityMode.TRANSACTIONAL);
        }
    },
    CONFERENCE_SOUND_DATA(Constants.NOT_PERSISTED){
        @Override
        public <K, V> void configure(CacheConfiguration<K, V> cfg) {
            cfg.setCacheMode(CacheMode.PARTITIONED);
            cfg.setBackups(0);
            cfg.setDataRegionName(getRegionName());
            cfg.setExpiryPolicyFactory(CreatedExpiryPolicy.factoryOf(Duration.ONE_MINUTE));
        }
    };

    private final String regionName;

    CacheDataType(String regionName) {
        this.regionName = regionName;
    }

    public String getRegionName() {
        return regionName;
    }

    public abstract <K, V> void configure(CacheConfiguration<K,V> cfg);

    public static class Constants {
        public static final String PERSISTED = "UsersData";
        public static final String NOT_PERSISTED = "ConfData";
    }
}
