package com.r.bigconf.core.service;

import com.r.bigconf.core.filter.DummyWavFilter;
import com.r.bigconf.core.filter.Filter;
import com.r.bigconf.core.model.Conference;
import com.r.bigconf.core.model.User;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public abstract class BaseConferenceService implements ConferenceService {

    public static final Filter FILTER = new DummyWavFilter();

    @Override
    public CompletableFuture<Conference> joinToConference(UUID conferenceId, User user) {
        return getConference(conferenceId).thenApplyAsync(conference -> {
            if(conference != null) {
                conference.getUsers().add(user);
            }
            return conference;
        });
    }

    @Override
    public CompletableFuture<Conference> leaveConference(UUID conferenceId, User user) {
        return getConference(conferenceId).thenApplyAsync(conference -> {
            if(conference != null) {
                conference.getUsers().remove(user);
            }
            return conference;
        });
    }

    protected Conference createConferenceInstance(User user) {
        Conference conference = new Conference();
        conference.getUsers().add(user);
        return conference;
    }
}
