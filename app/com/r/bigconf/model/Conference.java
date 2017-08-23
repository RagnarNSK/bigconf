package com.r.bigconf.model;

import java.util.ArrayList;
import java.util.List;

public class Conference {
    public static final int DEFAULT_RECORD_INTERVAL = 100;
    private final int recordInterval;
    private List<User> users = new ArrayList<>();

    public Conference() {
        recordInterval = DEFAULT_RECORD_INTERVAL;
    }

    public Conference(int recordInterval) {
        this.recordInterval = recordInterval;
    }

    public List<User> getUsers() {
        return users;
    }

    public int getRecordInterval() {
        return recordInterval;
    }
}
