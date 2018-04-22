package com.r.bigconf.ignite.service.affinity;

import com.r.bigconf.core.model.ConferenceUsers;
import org.apache.ignite.Ignite;

import java.nio.ByteBuffer;
import java.util.Collection;
import java.util.UUID;

public class ConferenceProcessDataAffinityService {

    public static final String USERS_CACHE_NAME = "conferenceUsersCache";
    public static final String SOUND_DATA_CACHE_NAME = "conferenceSoundDataCache";

    private final IgniteAffinityServiceHelper<String, ByteBuffer> ish;
    private final IgniteAffinityServiceHelper<UUID, ConferenceUsers> usersIsh;

    public ConferenceProcessDataAffinityService(Ignite ignite) {
        this.ish = new IgniteAffinityServiceHelper<>(SOUND_DATA_CACHE_NAME, ignite, String.class, ByteBuffer.class);
        this.usersIsh = new IgniteAffinityServiceHelper<>(USERS_CACHE_NAME, ignite, UUID.class, ConferenceUsers.class);
    }

    public Collection<String> getUsersList(UUID conferenceId) {
        return usersIsh.getCache().get(conferenceId).getUsersList();
    }

    public ByteBuffer getUserIncomingData(UUID conferenceId, String userId) {
        return ish.getCache().get(getIncomingSoundKey(conferenceId, userId));
    }

    public void storeCommonSound(UUID conferenceId, ByteBuffer commonChannel) {
        String key = getCommonSoundKey(conferenceId);
        if (commonChannel != null) {
            ish.getCache().put(key, commonChannel);
        } else {
            ish.getCache().remove(key);
        }
    }

    public void storeUserSound(UUID conferenceId, String userId, ByteBuffer bytes) {
        String key = getSoundKey(conferenceId, userId);
        if (bytes != null) {
            ish.getCache().put(key, bytes);
        } else {
            ish.getCache().remove(key);
        }
    }

    public void saveUserForConference(UUID id, ConferenceUsers conferenceUsers) {
        usersIsh.getCache().put(id, conferenceUsers);
    }

    public static String getIncomingSoundKey(UUID conferenceId, String userId) {
        return "incomingSound:" + conferenceId + ":" + userId;
    }

    public static String getSoundKey(UUID conferenceId, String userId) {
        return "sound:" + conferenceId + ":" + userId;
    }

    public static String getCommonSoundKey(UUID conferenceId) {
        return "sound:" + conferenceId + ":%common%";
    }

}
