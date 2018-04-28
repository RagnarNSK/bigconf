package com.r.bigconf.ignite.process;

import com.google.common.collect.Maps;
import com.r.bigconf.core.model.ConferenceUserInstantData;
import com.r.bigconf.core.model.ConferenceUsers;
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
    private ConferenceUsers conferenceUsers;

    public IgniteConferenceProcessData(Ignite ignite, UUID conferenceId) {
        this.conferenceId = conferenceId;
        this.conferenceProcessDataService = new ConferenceProcessDataAffinityService(ignite);
        conferenceUsers = conferenceProcessDataService.getUsers(conferenceId);
    }

    @Override
    public Map<String, ByteBuffer> getUsersIncomingData() {
        Collection<String> usersList = conferenceProcessDataService.getUsers(conferenceId).getUsersData().stream()
                .map(ConferenceUserInstantData::getUserId)
                .collect(Collectors.toSet());
        Map<String, ByteBuffer> result = Maps.newHashMap();
        usersList.forEach(userId -> {
            ByteBuffer data = conferenceProcessDataService.getUserIncomingData(conferenceId, userId);
            if (data != null) {
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

    @Override
    public ConferenceUsers getUsersInstantDataToModify() {
        return conferenceUsers;
    }

    public void saveUsersData() {
        conferenceProcessDataService.saveUserForConference(conferenceId, conferenceUsers);
    }
}
