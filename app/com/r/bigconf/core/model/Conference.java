package com.r.bigconf.core.model;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Data
public class Conference {
    public static final int DEFAULT_RECORD_INTERVAL = 500;
    private final UUID id;
    private final int recordInterval;
    private final List<User> users = new ArrayList<>();
    private volatile boolean isActive = true;

    public Conference() {
        this(DEFAULT_RECORD_INTERVAL);
    }

    public Conference(int recordInterval) {
        id = UUID.randomUUID();
        this.recordInterval = recordInterval;
    }

}
