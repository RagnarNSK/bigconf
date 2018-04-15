package com.r.bigconf.ignite;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.ignite.Ignite;
import org.apache.ignite.Ignition;
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
        ignite = Ignition.start();
        log.info("Ignite {} started", ignite.name());
        lifecycle.addStopHook(() -> {
            log.info("Ignite close start");
            ignite.close();
            log.info("Ignite closed");
            return CompletableFuture.completedFuture(null);
        });
    }
}
