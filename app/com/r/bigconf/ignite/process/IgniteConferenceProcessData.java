package com.r.bigconf.ignite.process;

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

        return usersList.stream()
                .collect(Collectors.toMap(
                        userId->userId,
                        (userId) -> conferenceProcessDataService.getUserIncomingData(conferenceId, userId)));
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
