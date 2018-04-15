package com.r.bigconf.core.service;

import com.r.bigconf.core.model.User;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public interface UserService {
    CompletableFuture<?> registerUser(User user);
    CompletableFuture<User> getUser(String userId);
    CompletableFuture<List<User>> getUsers();

    CompletableFuture<User> getCurrentUser();
}
