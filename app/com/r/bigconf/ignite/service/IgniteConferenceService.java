package com.r.bigconf.ignite.service;

import com.r.bigconf.core.processing.model.ConferenceChannelsData;
import com.r.bigconf.core.processing.model.ConferenceProcessData;
import com.r.bigconf.core.service.BaseConferenceService;
import com.r.bigconf.core.model.Conference;
import com.r.bigconf.core.model.User;
import com.r.bigconf.ignite.process.ConferenceDataProvider;
import com.r.bigconf.ignite.process.IgniteConferenceProcess;
import com.r.bigconf.ignite.IgniteHolder;
import org.apache.ignite.Ignite;
import org.apache.ignite.IgniteCache;
import org.apache.ignite.cache.CacheMode;
import org.apache.ignite.cache.query.QueryCursor;
import org.apache.ignite.cache.query.ScanQuery;

import javax.cache.Cache;
import javax.inject.Inject;
import java.nio.ByteBuffer;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import static com.r.bigconf.ignite.util.AsyncUtils.toCF;

public class IgniteConferenceService extends BaseConferenceService {

    private static final String CACHE_NAME = "conferencesCache";
    private static final String SOUND_DATA_CACHE_NAME = "conferenceSoundDataCache";

    private final IgniteServiceHelper<UUID, Conference> ish;
    private final IgniteServiceHelper<String, ByteBuffer> soundISH;

    private final ConferenceDataProvider provider = new IgniteConferenceDataProvider(CACHE_NAME);

    @Inject
    public IgniteConferenceService(IgniteHolder igniteHolder) {
        ish = new IgniteServiceHelper<>(CACHE_NAME, igniteHolder, CacheMode.PARTITIONED, UUID.class, Conference.class);
        soundISH = new IgniteServiceHelper<>(SOUND_DATA_CACHE_NAME, igniteHolder, CacheMode.PARTITIONED, String.class, ByteBuffer.class);
    }

    @Override
    public CompletableFuture<Conference> startConference(User user) {
        //TODO run compute collocated
        Conference conference = createConferenceInstance(user);
        UUID id = conference.getId();
        IgniteConferenceProcess confProcess = new IgniteConferenceProcess(id, provider);
        return toCF(ish.getCache().putAsync(id, conference)).thenComposeAsync((nothing) -> {
            ish.getIgnite().compute().run(confProcess);
            return toCF(ish.getCache().getAsync(id));
        });

    }

    @Override
    public void close() {
        //TODO
    }

    @Override
    public CompletableFuture<ByteBuffer> getForUser(UUID conferenceId, String userId) {
        IgniteCache<String, ByteBuffer> cache = soundISH.getCache();
        String soundKey = getSoundKey(conferenceId, userId);
        if (cache.containsKey(soundKey)) {
            return toCF(cache.getAsync(soundKey));
        } else {
            return toCF(cache.getAsync(getCommonSoundKey(conferenceId)));
        }
    }

    @Override
    public CompletableFuture<Void> addIncoming(UUID conferenceId, String userId, ByteBuffer byteBuffer) {
        return toCF(soundISH.getCache().putAsync(getIncomingSoundKey(conferenceId, userId), byteBuffer));
    }

    private static String getIncomingSoundKey(UUID conferenceId, String userId) {
        return "incomingSound:" + conferenceId + ":" + userId;
    }

    private static String getSoundKey(UUID conferenceId, String userId) {
        return "sound:" + conferenceId + ":" + userId;
    }

    private static String getCommonSoundKey(UUID conferenceId) {
        return "sound:" + conferenceId + ":%common%";
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

    private static class IgniteConferenceDataProvider implements ConferenceDataProvider {

        private final String conferenceCacheName;

        IgniteConferenceDataProvider(String conferenceCacheName) {
            this.conferenceCacheName = conferenceCacheName;
        }

        @Override
        public Conference getConference(Ignite ignite, UUID conferenceId) {
            return (Conference) ignite.cache(conferenceCacheName).get(conferenceId);
        }

        @Override
        public ConferenceProcessData getConferenceProcessData(Ignite ignite, UUID conferenceId) {
            return new IgniteConferenceProcessData(ignite, getConference(ignite, conferenceId));
        }

        private static class IgniteConferenceProcessData implements ConferenceProcessData {

            private final Ignite ignite;
            private final Conference conference;

            private IgniteConferenceProcessData(Ignite ignite, Conference conference) {
                this.ignite = ignite;
                this.conference = conference;
            }


            @Override
            public Map<String, ByteBuffer> getUsersIncomingData() {
                UUID conferenceId = conference.getId();
                return conference.getUsers().stream()
                        .collect(Collectors.toMap(
                                User::getId,
                                (user) -> getCache().get(getIncomingSoundKey(conferenceId, user.getId()))));
            }

            @Override
            public ConferenceChannelsData getChannelsDataObjectToFill() {
                return new ConferenceChannelsData();
            }

            @Override
            public void replaceWithNewChannelsData(ConferenceChannelsData builtData) {
                UUID conferenceId = conference.getId();
                IgniteCache<String, ByteBuffer> cache = getCache();
                cache.put(getCommonSoundKey(conferenceId), builtData.getCommonChannel());
                builtData.getAudioChannels().forEach((userId, bytes) -> {
                    cache.put(getSoundKey(conferenceId, userId), bytes);
                });

            }

            private IgniteCache<String, ByteBuffer> getCache() {
                return ignite.cache(SOUND_DATA_CACHE_NAME);
            }
        }
    }

}
