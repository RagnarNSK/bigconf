package com.r.bigconf.core.service;

import com.r.bigconf.core.model.Conference;
import com.r.bigconf.core.model.User;

import java.nio.ByteBuffer;
import java.util.UUID;

public interface ConferenceService {

    Conference getConference(UUID conferenceId);

    Conference startConference(User user);

    Conference joinToConference(UUID conferenceId, User user);

    Conference leaveConference(UUID conferenceId, User user);

    void close();

    ByteBuffer getForUser(String userId);

    void addIncoming(String userId, ByteBuffer byteBuffer);
}
