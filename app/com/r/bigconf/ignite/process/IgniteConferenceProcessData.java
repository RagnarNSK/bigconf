package com.r.bigconf.ignite.process;

import com.google.common.collect.Maps;
import com.r.bigconf.core.processing.model.ConferenceChannelsData;
import com.r.bigconf.core.processing.model.ConferenceProcessData;
import com.r.bigconf.ignite.service.affinity.ConferenceProcessDataAffinityService;
import org.apache.ignite.Ignite;

import java.nio.ByteBuffer;
import java.util.Collection;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

public class IgniteConferenceProcessData implements ConferenceProcessData {

    private final UUID conferenceId;
    private final ConferenceProcessDataAffinityService conferenceProcessDataService;

    public IgniteConferenceProcessData(Ignite ignite, UUID conferenceId) {
        this.conferenceId = conferenceId;
        this.conferenceProcessDataService = new ConferenceProcessDataAffinityService(ignite);
    }

    @Override
    public Map<String, ByteBuffer> getUsersIncomingData() {
        Collection<String> usersList = conferenceProcessDataService.getUsersList(conferenceId);
        Map<String, ByteBuffer> result = Maps.newHashMap();
        usersList.forEach(userId -> {
            ByteBuffer data = conferenceProcessDataService.getUserIncomingData(conferenceId, userId);
            if(data != null) {
                result.put(userId, data);
            }
        });
        return result;
    }

    @Override
    public ConferenceChannelsData getChannelsDataObjectToFill() {
        return new ConferenceChannelsData();
    }

    @Override
    public void replaceWithNewChannelsData(ConferenceChannelsData builtData) {
            conferenceProcessDataService.storeCommonSound(conferenceId, builtData.getCommonChannel());
        builtData.getAudioChannels().forEach((userId, bytes) -> {
            conferenceProcessDataService.storeUserSound(conferenceId, userId, bytes);
        });

    }
}
