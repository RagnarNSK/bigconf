package com.r.bigconf.core.manager;

import com.r.bigconf.core.model.Conference;
import com.r.bigconf.core.model.User;
import java.util.UUID;
import java.util.function.Function;

public abstract class BaseConferenceManager implements ConferenceManager {

    @Override
    public Conference joinToConference(UUID conferenceId, User user) {
        return withConference(conferenceId, (conference -> {
            conference.getUsers().add(user);
            return conference;
        }));
    }

    @Override
    public Conference leaveConference(UUID conferenceId, User user) {
        return withConference(conferenceId, (conference -> {
            conference.getUsers().remove(user);
            return conference;
        }));
    }

    protected  <T> T withConference(UUID conferenceId, Function<Conference, T> function) {
        Conference conference = getConference(conferenceId);
        if (conference != null) {
            return function.apply(conference);
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
