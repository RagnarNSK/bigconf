package com.r.bigconf.ignite.service;

import com.r.bigconf.core.model.*;
import com.r.bigconf.core.service.BaseConferenceService;
import com.r.bigconf.ignite.CacheDataType;
import com.r.bigconf.ignite.service.affinity.ConferenceAffinityService;
import com.r.bigconf.ignite.service.affinity.ConferenceProcessDataAffinityService;
import com.r.bigconf.ignite.IgniteHolder;
import org.apache.ignite.Ignite;
import org.apache.ignite.IgniteCache;
import org.apache.ignite.cache.CacheEntryProcessor;
import org.apache.ignite.cache.query.QueryCursor;
import org.apache.ignite.cache.query.ScanQuery;
import org.apache.ignite.lang.IgniteCallable;
import org.apache.ignite.resources.IgniteInstanceResource;

import javax.cache.Cache;
import javax.inject.Inject;
import java.nio.ByteBuffer;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.ForkJoinPool;
import java.util.stream.Collectors;

import static com.r.bigconf.ignite.util.AsyncUtils.toCF;

public class IgniteConferenceService extends BaseConferenceService {

    private static final Executor affinityCallsExecutor = new ForkJoinPool();

    private final IgniteServiceHelper<UUID, Conference> ish;
    private final IgniteServiceHelper<String, ByteBuffer> soundISH;
    private final IgniteServiceHelper<UUID, ConferenceUsers> confUsersISH;

    @Inject
    public IgniteConferenceService(IgniteHolder igniteHolder) {
        ish = new IgniteServiceHelper<>(ConferenceAffinityService.CACHE_NAME, igniteHolder, UUID.class, Conference.class, CacheDataType.CONFERENCE_DATA);
        confUsersISH = new IgniteServiceHelper<>(ConferenceProcessDataAffinityService.USERS_CACHE_NAME, igniteHolder, UUID.class, ConferenceUsers.class, CacheDataType.CONFERENCE_DATA);
        soundISH = new IgniteServiceHelper<>(ConferenceProcessDataAffinityService.SOUND_DATA_CACHE_NAME, igniteHolder, String.class, ByteBuffer.class, CacheDataType.CONFERENCE_SOUND_DATA);
    }

    @Override
    public CompletableFuture<Conference> startConference(User user) {
        Conference conference = createConferenceInstance(user);
        UUID id = conference.getId();
        return CompletableFuture.supplyAsync(() -> {
            return ish.getIgnite().compute()
                    .affinityCall(ConferenceAffinityService.CACHE_NAME, id, new StartConference(conference));
        }, affinityCallsExecutor);

    }

    @Override
    public CompletableFuture<?> stopConference(User user, UUID conferenceId) {
        return toCF(ish.getCache().invokeAsync(conferenceId, (CacheEntryProcessor<UUID, Conference, Object>)(entry, args)->{
            if (entry.exists()) {
                Conference conference = entry.getValue();
                if (conference.getCreatedBy().equals(args[0])) {
                    conference.setActive(false);
                    entry.setValue(conference);
                } else {
                    throw new IllegalStateException("Not creator calls stopping");
                }
            }
            return entry;
        }, user.getId()));
    }

    @Override
    public CompletableFuture<Conference> joinToConference(UUID conferenceId, User user) {
        return toCF(confUsersISH.getCache()
                .invokeAsync(conferenceId, (CacheEntryProcessor<UUID, ConferenceUsers, Object>) (entry, arguments) -> {
                    ConferenceUsers users = entry.getValue();
                    String userId = (String) arguments[0];
                    addUser(users, userId);
                    entry.setValue(users);
                    return entry;
                }, user.getId())).thenComposeAsync((nothing) -> getConference(conferenceId));
    }

    @Override
    public CompletableFuture<Conference> leaveConference(UUID conferenceId, User user) {
        return toCF(confUsersISH.getCache()
                .invokeAsync(conferenceId, (CacheEntryProcessor<UUID, ConferenceUsers, Object>) (entry, arguments) -> {
                    ConferenceUsers users = entry.getValue();
                    String userId = (String) arguments[0];
                    removeUser(users, userId);
                    entry.setValue(users);
                    return entry;
                }, user.getId())).thenComposeAsync((nothing) -> getConference(conferenceId));
    }

    @Override
    public CompletableFuture<ByteBuffer> getForUser(UUID conferenceId, String userId) {
        IgniteCache<String, ByteBuffer> cache = soundISH.getCache();
        String soundKey = ConferenceProcessDataAffinityService.getSoundKey(conferenceId, userId);
        if (cache.containsKey(soundKey)) {
            return toCF(cache.getAsync(soundKey));
        } else {
            return toCF(cache.getAsync(ConferenceProcessDataAffinityService.getCommonSoundKey(conferenceId)));
        }
    }

    @Override
    public CompletableFuture<?> addIncoming(UUID conferenceId, String userId, ByteBuffer byteBuffer) {
        String key = ConferenceProcessDataAffinityService.getIncomingSoundKey(conferenceId, userId);
        return toCF(soundISH.getCache().putAsync(key, byteBuffer));
    }


    @Override
    public CompletableFuture<List<ConferenceDTO>> listAvailableConferences(User user) {
        //TODO filter conferences
        QueryCursor<ConferenceDTO> cursor = ish.getCache().query(new ScanQuery<>(),(entry)->{
            Conference conf = (Conference) entry.getValue();
            ConferenceUsers users = confUsersISH.getCache().get(conf.getId());
            return new ConferenceDTO(conf.getId(),
                    conf.getCreatedBy(),
                    users.getUsersData().stream()
                            .map(ConferenceUserInstantData::getUserId)
                            .collect(Collectors.toSet()));
        });
        return toCF(cursor.iterator(), v->v);
    }

    @Override
    public CompletableFuture<Conference> getConference(UUID conferenceId) {
        return toCF(ish.getCache().getAsync(conferenceId));
    }

    @Override
    public CompletableFuture<ConferenceUsers> getConferenceUsers(UUID conferenceId) {
        return toCF(confUsersISH.getCache().getAsync(conferenceId));
    }


    private static class StartConference implements IgniteCallable<Conference> {
        @IgniteInstanceResource
        private Ignite ignite;
        private final Conference conference;

        public StartConference(Conference conference) {
            this.conference = conference;
        }

        @Override
        public Conference call() throws Exception {
            ConferenceAffinityService conferenceService = new ConferenceAffinityService(ignite);
            return conferenceService.start(conference);
        }
    }
}
