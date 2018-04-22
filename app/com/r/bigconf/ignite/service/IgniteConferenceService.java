package com.r.bigconf.ignite.service;

import com.r.bigconf.core.model.ConferenceUsers;
import com.r.bigconf.core.service.BaseConferenceService;
import com.r.bigconf.core.model.Conference;
import com.r.bigconf.core.model.User;
import com.r.bigconf.ignite.CacheDataType;
import com.r.bigconf.ignite.service.affinity.ConferenceAffinityService;
import com.r.bigconf.ignite.service.affinity.ConferenceProcessDataAffinityService;
import com.r.bigconf.ignite.IgniteHolder;
import org.apache.ignite.Ignite;
import org.apache.ignite.IgniteCache;
import org.apache.ignite.cache.CacheEntryProcessor;
import org.apache.ignite.cache.CacheMode;
import org.apache.ignite.cache.query.QueryCursor;
import org.apache.ignite.cache.query.ScanQuery;
import org.apache.ignite.lang.IgniteRunnable;
import org.apache.ignite.resources.IgniteInstanceResource;

import javax.cache.Cache;
import javax.cache.processor.EntryProcessorException;
import javax.cache.processor.MutableEntry;
import javax.inject.Inject;
import java.nio.ByteBuffer;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import static com.r.bigconf.ignite.util.AsyncUtils.toCF;

public class IgniteConferenceService extends BaseConferenceService {


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
        return toCF(ish.getIgnite().compute()
                .affinityRunAsync(ConferenceAffinityService.CACHE_NAME, id, new StartConference(conference)))
                .thenComposeAsync((nothing)->getConference(id));

    }

    @Override
    public CompletableFuture<Conference> joinToConference(UUID conferenceId, User user) {
        return toCF(confUsersISH.getCache()
                .invokeAsync(conferenceId, (CacheEntryProcessor<UUID, ConferenceUsers, Object>) (entry, arguments) -> {
            entry.getValue().getUsersList().add((String)arguments[0]);
            return entry;
        }, user.getId())).thenComposeAsync((nothing)->getConference(conferenceId));
    }

    @Override
    public CompletableFuture<Conference> leaveConference(UUID conferenceId, User user) {
        return toCF(confUsersISH.getCache()
                .invokeAsync(conferenceId, (CacheEntryProcessor<UUID, ConferenceUsers, Object>) (entry, arguments) -> {
            entry.getValue().getUsersList().remove(arguments[0]);
            return entry;
        }, user.getId())).thenComposeAsync((nothing)->getConference(conferenceId));
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
    public CompletableFuture<Void> addIncoming(UUID conferenceId, String userId, ByteBuffer byteBuffer) {
        String key = ConferenceProcessDataAffinityService.getIncomingSoundKey(conferenceId, userId);
        return toCF(soundISH.getCache().putAsync(key, byteBuffer));
    }


    @Override
    public CompletableFuture<List<Conference>> listAvailableConferences(User user) {
        //TODO filter conferences
        QueryCursor<Cache.Entry<UUID, Conference>> cursor = ish.getCache().query(new ScanQuery<>());
        return toCF(cursor.iterator(), Cache.Entry::getValue);
    }

    @Override
    public CompletableFuture<Conference> getConference(UUID conferenceId) {
        return toCF(ish.getCache().getAsync(conferenceId));
    }

    @Override
    public CompletableFuture<ConferenceUsers> getConferenceUsers(UUID conferenceId) {
        return toCF(confUsersISH.getCache().getAsync(conferenceId));
    }


    private static class StartConference implements IgniteRunnable {
        @IgniteInstanceResource
        private Ignite ignite;
        private final Conference conference;

        public StartConference(Conference conference) {
            this.conference = conference;
        }

        @Override
        public void run() {
            ConferenceAffinityService conferenceService = new ConferenceAffinityService(ignite);
            conferenceService.start(conference);
        };
    }
}
