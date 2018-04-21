package com.r.bigconf.core.service;

import com.r.bigconf.core.filter.DummyWavFilter;
import com.r.bigconf.core.filter.Filter;
import com.r.bigconf.core.model.Conference;
import com.r.bigconf.core.model.User;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

public abstract class BaseConferenceService implements ConferenceService {

    public static final Filter FILTER = new DummyWavFilter();

    @Override
    public CompletableFuture<Conference> joinToConference(UUID conferenceId, User user) {
        return withConference(conferenceId, (conference -> {
            conference.getUsers().add(user);
            return conference;
        }));
    }

    @Override
    public CompletableFuture<Conference> leaveConference(UUID conferenceId, User user) {
        return withConference(conferenceId, (conference -> {
            conference.getUsers().remove(user);
            return conference;
        }));
    }

    protected  <T> CompletableFuture<T> withConference(UUID conferenceId, Function<Conference, T> function) {
        CompletableFuture<Conference> future = getConference(conferenceId);
        if (future != null) {
            return future.thenApplyAsync(function::apply);
        } else {
            return null;
        }
    }

    protected Conference createConferenceInstance(User user) {
        Conference conference = new Conference();
        conference.getUsers().add(user);
        return conference;
    }
}
