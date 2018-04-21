package com.r.bigconf.core.processing.model;

import com.r.bigconf.core.filter.Filter;

import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;

public class ConferenceProcessDataObject implements ConferenceProcessData {
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

    @Override
    public Map<Integer, ByteBuffer> getUsersIncomingData() {
        Map<Integer, ByteBuffer> ret = new HashMap<>(incoming);
        incoming.clear();
        return ret;
    }

    @Override
    public ConferenceChannelsData getChannelsDataObjectToFill() {
        return building;
    }

    @Override
    public void replaceWithNewChannelsData(ConferenceChannelsData builtData) {
        ConferenceChannelsData activeBackup = active;
        active = builtData;
        activeBackup.getAudioChannels().clear();
        activeBackup.setCommonChannel(null);
        building = activeBackup;
    }
}