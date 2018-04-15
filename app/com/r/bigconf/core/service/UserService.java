package com.r.bigconf.core.service;

import com.r.bigconf.core.model.User;

import java.util.List;

public interface UserService {
    void registerUser(User user);
    User getUser(String userId);
    List<User> getUsers();
}
