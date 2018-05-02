package com.r.bigconf.core.service;

import com.r.bigconf.core.filter.DummyWavFilter;
import com.r.bigconf.core.filter.Filter;
import com.r.bigconf.core.model.Conference;
import com.r.bigconf.core.model.ConferenceUserInstantData;
import com.r.bigconf.core.model.ConferenceUsers;
import com.r.bigconf.core.model.User;

import java.nio.ByteBuffer;
import java.util.UUID;

public abstract class BaseConferenceService implements ConferenceService {

    public static final Filter FILTER = new DummyWavFilter();

    protected Conference createConferenceInstance(User user) {
        return new Conference(user.getId());
    }

    protected void addUser(ConferenceUsers users, String userId) {
        if(users != null) {
            users.getUsersData().add(new ConferenceUserInstantData(userId, true, false));
        }
    }

    protected void removeUser(ConferenceUsers users, String userId) {
        if(users != null) {
            users.getUsersData().removeIf(data->data.getUserId().equals(userId));
        }
    }

    protected ByteBuffer processIncomingUserData(UUID conferenceId, String userId, ByteBuffer soundData) {
        return FILTER.filter(soundData);
    }
}
