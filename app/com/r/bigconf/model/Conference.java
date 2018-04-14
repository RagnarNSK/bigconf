package com.r.bigconf.model;

import lombok.Getter;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Getter
public class Conference {
    public static final int DEFAULT_RECORD_INTERVAL = 100;
    private final UUID id;
    private final int recordInterval;
    private final List<User> users = new ArrayList<>();

    public Conference() {
        this(DEFAULT_RECORD_INTERVAL);
    }

    public Conference(int recordInterval) {
        id = UUID.randomUUID();
        this.recordInterval = recordInterval;
    }

}
