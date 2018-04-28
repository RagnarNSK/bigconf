package com.r.bigconf.core.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ConferenceUserInstantData {
    private String userId;
    private boolean muted;
    private boolean speaking;

}
