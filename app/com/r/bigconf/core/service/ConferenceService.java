package com.r.bigconf.core.service;

import com.r.bigconf.core.model.Conference;
import com.r.bigconf.core.model.ConferenceDTO;
import com.r.bigconf.core.model.ConferenceUsers;
import com.r.bigconf.core.model.User;

import java.nio.ByteBuffer;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public interface ConferenceService {

    CompletableFuture<Conference> getConference(UUID conferenceId);

    CompletableFuture<ConferenceUsers> getConferenceUsers(UUID conferenceId);

    CompletableFuture<Conference> startConference(User user);

    CompletableFuture<?> stopConference(User user, UUID conferenceId);

    CompletableFuture<Conference> joinToConference(UUID conferenceId, User user);

    CompletableFuture<Conference> leaveConference(UUID conferenceId, User user);

    CompletableFuture<ByteBuffer> getForUser(UUID conferenceId, String userId);

    CompletableFuture<?> addIncoming(UUID conferenceId, String userId, ByteBuffer byteBuffer);

    CompletableFuture<List<ConferenceDTO>> listAvailableConferences(User user);
}
