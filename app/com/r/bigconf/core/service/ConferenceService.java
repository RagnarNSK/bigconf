package com.r.bigconf.core.service;

import com.r.bigconf.core.model.Conference;
import com.r.bigconf.core.model.User;

import java.nio.ByteBuffer;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public interface ConferenceService {

    CompletableFuture<Conference> getConference(UUID conferenceId);

    CompletableFuture<Conference> startConference(User user);

    CompletableFuture<Conference> joinToConference(UUID conferenceId, User user);

    CompletableFuture<Conference> leaveConference(UUID conferenceId, User user);

    void close();

    CompletableFuture<ByteBuffer> getForUser(UUID conferenceId, String userId);

    CompletableFuture<Void> addIncoming(UUID conferenceId, String userId, ByteBuffer byteBuffer);

    CompletableFuture<List<Conference>> listAvailableConferences(User user);
}
