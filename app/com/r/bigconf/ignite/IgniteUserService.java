package com.r.bigconf.ignite;

import com.r.bigconf.core.model.User;
import com.r.bigconf.core.service.UserService;
import org.apache.ignite.IgniteCache;
import org.apache.ignite.cache.CacheMode;
import org.apache.ignite.configuration.CacheConfiguration;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;

public class IgniteUserService implements UserService {

    private static final String CACHE_NAME = "userCache";

    private final IgniteHolder igniteHolder;

    @Inject
    public IgniteUserService(IgniteHolder igniteHolder) {
        this.igniteHolder = igniteHolder;
        CacheConfiguration<String, User> cfg = new CacheConfiguration<>();
        cfg.setName(CACHE_NAME);
        cfg.setCacheMode(CacheMode.REPLICATED);
        igniteHolder.getIgnite().createCache(cfg);
    }

    @Override
    public void registerUser(User user) {
        getCache().put(user.getId(), user);
    }

    @Override
    public User getUser(String userId) {
        IgniteCache<String, User> cache = getCache();
        return cache.get(userId);
    }

    @Override
    public List<User> getUsers() {
        return new ArrayList<>(getUsers());
    }


    private IgniteCache<String, User> getCache() {
        return igniteHolder.getIgnite().cache(CACHE_NAME);
    }
}
