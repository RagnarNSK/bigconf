package com.r.bigconf.model;

import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

@Getter
public class Conference {
    public static final int DEFAULT_RECORD_INTERVAL = 100;
    private final int recordInterval;
    private final List<User> users = new ArrayList<>();

    public Conference() {
        recordInterval = DEFAULT_RECORD_INTERVAL;
    }

    public Conference(int recordInterval) {
        this.recordInterval = recordInterval;
    }

}
