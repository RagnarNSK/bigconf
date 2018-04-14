package com.r.bigconf.core.manager;

import com.r.bigconf.core.model.Conference;
import com.r.bigconf.core.model.User;

import java.util.UUID;

public interface ConferenceManager {

    Conference startConference(User user);

    Conference joinToConference(UUID conferenceId, User user);

    Conference leaveConference(UUID conferenceId, User user);

    void close();
}
