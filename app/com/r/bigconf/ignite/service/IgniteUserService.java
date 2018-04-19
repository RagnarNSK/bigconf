package com.r.bigconf.ignite.service;

import com.google.common.collect.Lists;
import com.r.bigconf.core.model.User;
import com.r.bigconf.core.service.UserService;
import com.r.bigconf.ignite.IgniteHolder;
import org.apache.ignite.IgniteCache;
import org.apache.ignite.cache.CacheMode;
import org.apache.ignite.configuration.CacheConfiguration;

import javax.inject.Inject;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import static com.r.bigconf.ignite.util.AsyncUtils.toCF;

public class IgniteUserService implements UserService {

    private static final String CACHE_NAME = "userCache";

    private final IgniteHolder igniteHolder;

    @Inject
    public IgniteUserService(IgniteHolder igniteHolder) {
        this.igniteHolder = igniteHolder;
        CacheConfiguration<String, User> cfg = new CacheConfiguration<>();
        cfg.setName(CACHE_NAME);
        cfg.setCacheMode(CacheMode.REPLICATED);
        igniteHolder.getIgnite().getOrCreateCache(cfg);
    }

    @Override
    public CompletableFuture<?> registerUser(User user) {
        return toCF(getCache().putAsync(user.getId(), user));
    }

    @Override
    public CompletableFuture<User> getUser(String userId) {
        return toCF(getCache().getAsync(userId));
    }

    @Override
    public CompletableFuture<List<User>> getUsers() {
        List<User> result = Lists.newArrayList();
        getCache().iterator().forEachRemaining(stringUserEntry -> {
            result.add(stringUserEntry.getValue());
        });
        return CompletableFuture.completedFuture(result);
    }

    private IgniteCache<String, User> getCache() {
        return igniteHolder.getIgnite().cache(CACHE_NAME);
    }
}
