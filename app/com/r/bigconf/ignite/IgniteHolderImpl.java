package com.r.bigconf.ignite;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.ignite.Ignite;
import org.apache.ignite.Ignition;
import org.apache.ignite.configuration.DataStorageConfiguration;
import org.apache.ignite.configuration.IgniteConfiguration;
import play.inject.ApplicationLifecycle;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.concurrent.CompletableFuture;

@Slf4j
@Singleton
@Getter
public class IgniteHolderImpl implements IgniteHolder {

    private final Ignite ignite;

    @Inject
    public IgniteHolderImpl(ApplicationLifecycle lifecycle) {
        IgniteConfiguration cfg = new IgniteConfiguration();
        cfg.setClassLoader(this.getClass().getClassLoader());
        addPersistence(cfg);
        ignite = Ignition.getOrStart(cfg);
        ignite.active(true);
        log.info("Ignite {} started", ignite.name());
        lifecycle.addStopHook(() -> {
            ignite.close();
            log.info("Ignite closed");
            return CompletableFuture.completedFuture(null);
        });
    }

    private void addPersistence(IgniteConfiguration cfg) {
        DataStorageConfiguration storageCfg = new DataStorageConfiguration();
        storageCfg.getDefaultDataRegionConfiguration().setPersistenceEnabled(true);
        cfg.setDataStorageConfiguration(storageCfg);
    }
}
