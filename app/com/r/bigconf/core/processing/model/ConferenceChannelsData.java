package com.r.bigconf.core.processing.model;

import lombok.Data;

import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;

@Data
public class ConferenceChannelsData {

    //TODO probably will need to replace ByteBuffers

    /**
     * Audio channels map
     * key - integer - user id
     * value - wav content of conference WITHOUT source of user identified by key
     */
    private final Map<String, ByteBuffer> audioChannels = new HashMap<>();
    private ByteBuffer commonChannel;
}
