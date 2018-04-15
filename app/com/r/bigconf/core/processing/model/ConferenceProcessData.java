package com.r.bigconf.core.processing.model;

import com.r.bigconf.core.filter.Filter;
import lombok.Data;

import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;

@Data
public class ConferenceProcessData {
    private volatile ConferenceChannelsData active = new ConferenceChannelsData();
    private ConferenceChannelsData building = new ConferenceChannelsData();
    private Map<Integer, ByteBuffer> incoming = new HashMap<>();


    public ByteBuffer getForUser(int userId) {
        if (active != null) {
            ByteBuffer special = active.getAudioChannels().get(userId);
            return special != null ? special : active.getCommonChannel();
        } else {
            return null;
        }
    }

    public void addIncoming(Integer userId, ByteBuffer byteBuffer, Filter filter) {
        incoming.put(userId, filter != null ? filter.filter(byteBuffer) : byteBuffer);
    }

}
