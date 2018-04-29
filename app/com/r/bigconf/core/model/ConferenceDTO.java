package com.r.bigconf.core.model;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.Set;
import java.util.UUID;

@Data
@AllArgsConstructor
public class ConferenceDTO {
    private final UUID id;
    private final String createdBy;
    private final Set<String> userIds;
}
