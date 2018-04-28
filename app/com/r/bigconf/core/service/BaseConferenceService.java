package com.r.bigconf.core.service;

import com.r.bigconf.core.filter.DummyWavFilter;
import com.r.bigconf.core.filter.Filter;
import com.r.bigconf.core.model.Conference;
import com.r.bigconf.core.model.ConferenceUserInstantData;
import com.r.bigconf.core.model.ConferenceUsers;
import com.r.bigconf.core.model.User;

public abstract class BaseConferenceService implements ConferenceService {

    public static final Filter FILTER = new DummyWavFilter();

    protected Conference createConferenceInstance(User user) {
        return new Conference(user.getId());
    }

    protected void addUser(ConferenceUsers users, String userId) {
        users.getUsersData().add(new ConferenceUserInstantData(userId, true, false));
    }

    protected void removeUser(ConferenceUsers users, String userId) {
        users.getUsersData().removeIf(data->data.getUserId().equals(userId));
    }
}
