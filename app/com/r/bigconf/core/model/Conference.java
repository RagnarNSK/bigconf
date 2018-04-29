package com.r.bigconf.core.model;

import lombok.Data;

import java.util.UUID;

@Data
public class Conference {
    public static final int DEFAULT_RECORD_INTERVAL = 500;
    private final UUID id;
    private final int recordInterval;
    private final String createdBy;
    private volatile boolean active = true;

    public Conference(String createdBy) {
        this(DEFAULT_RECORD_INTERVAL, createdBy);
    }

    public Conference(int recordInterval, String createdBy) {
        id = UUID.randomUUID();
        this.recordInterval = recordInterval;
        this.createdBy = createdBy;
    }

}
