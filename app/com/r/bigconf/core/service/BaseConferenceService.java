package com.r.bigconf.core.service;

import com.r.bigconf.core.filter.DummyWavFilter;
import com.r.bigconf.core.filter.Filter;
import com.r.bigconf.core.model.Conference;
import com.r.bigconf.core.model.User;

public abstract class BaseConferenceService implements ConferenceService {

    public static final Filter FILTER = new DummyWavFilter();

    protected Conference createConferenceInstance(User user) {
        return new Conference(user.getId());
    }
}
