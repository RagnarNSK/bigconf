package com.r.bigconf.core.manager;

import com.r.bigconf.core.model.Conference;
import com.r.bigconf.core.model.User;
import com.r.bigconf.local.SingleThreadConferenceProcess;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;

public abstract class BaseConferenceManager implements ConferenceManager {

    private final Set<SingleThreadConferenceProcess> activeProcesses = new HashSet<>();

    protected Conference createConferenceInstance(User user) {
        Conference conference = new Conference();
        conference.getUsers().add(user);
        return conference;
    }

    protected Set<SingleThreadConferenceProcess> getActiveProcesses() {
        return activeProcesses;
    }

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

    @Override
    public void close() {
        //TODO check if it's enough
        activeProcesses.forEach(confProcess -> confProcess.isActive = false);
    }

    protected  <T> T withConference(UUID conferenceId, Function<Conference, T> function) {
        Optional<Conference> optional = activeProcesses.stream()
                .filter(confProcess -> conferenceId.equals(confProcess.getConference().getId()))
                .map(SingleThreadConferenceProcess::getConference)
                .findFirst();
        if (optional.isPresent()) {
            Conference conference = optional.get();
            return function.apply(conference);
        } else {
            return null;
        }
    }
}
