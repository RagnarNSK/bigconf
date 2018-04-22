package com.r.bigconf.ignite.service;

import com.r.bigconf.core.model.User;
import com.r.bigconf.core.service.UserService;
import com.r.bigconf.ignite.CacheDataType;
import com.r.bigconf.ignite.IgniteHolder;
import org.apache.ignite.cache.CacheMode;

import javax.cache.Cache;
import javax.inject.Inject;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import static com.r.bigconf.ignite.util.AsyncUtils.toCF;

public class IgniteUserService implements UserService {

    private static final String CACHE_NAME = "userCache";

    private final IgniteServiceHelper<String,User> ish;

    @Inject
    public IgniteUserService(IgniteHolder igniteHolder) {
        ish = new IgniteServiceHelper<>(CACHE_NAME, igniteHolder, String.class, User.class, CacheDataType.USERS_DATA);
    }

    @Override
    public CompletableFuture<?> registerUser(User user) {
        return toCF(ish.getCache().putAsync(user.getId(), user));
    }

    @Override
    public CompletableFuture<User> getUser(String userId) {
        return toCF(ish.getCache().getAsync(userId));
    }

    @Override
    public CompletableFuture<List<User>> getUsers() {
        return toCF(ish.getCache().iterator(), Cache.Entry::getValue);
    }
}
