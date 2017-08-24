package com.r.bigconf.processing;

import com.r.bigconf.model.Conference;

import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;

public class ConfProcess implements Runnable {

    private final Conference conference;
    public boolean isActive;

    private ConferenceChannelsData active;
    private ConferenceChannelsData building;

    private Map<Integer, ByteBuffer> incoming = new HashMap<>();

    public ConfProcess(Conference conference) {
        this.conference = conference;
    }

    @Override
    public void run() {
        while (isActive) {
            if (System.currentTimeMillis() % conference.getRecordInterval() == 0) {
                Map<Integer, ByteBuffer> incomingBuffer = new HashMap<>(incoming);
                incoming.clear();
                building = new ConferenceChannelsData();
                incomingBuffer.forEach((id,bytes) -> {

                });
            }
        }

    }

    public ByteBuffer getForUser(int userId) {
        if (active != null) {
            ByteBuffer special = active.audioChannels.get(userId);
            return special != null ? special : active.commonChannel;
        } else {
            return null;
        }
    }

    private static class ConferenceChannelsData {
        /**
         * Audio channels map
         * key - integer - user id
         * value - wav content of conference WITHOUT source of user identified by key
         */
        private Map<Integer, ByteBuffer> audioChannels = new HashMap<>();
        private ByteBuffer commonChannel;
    }
}
