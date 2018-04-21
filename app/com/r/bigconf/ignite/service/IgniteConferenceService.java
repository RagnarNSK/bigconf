package com.r.bigconf.ignite.service;

import com.r.bigconf.core.processing.model.ConferenceProcessData;
import com.r.bigconf.core.service.BaseConferenceService;
import com.r.bigconf.core.model.Conference;
import com.r.bigconf.core.model.User;
import com.r.bigconf.ignite.process.ConferenceDataProvider;
import com.r.bigconf.ignite.process.IgniteConferenceProcess;
import com.r.bigconf.ignite.IgniteHolder;
import org.apache.ignite.Ignite;
import org.apache.ignite.cache.CacheMode;
import org.apache.ignite.cache.query.QueryCursor;
import org.apache.ignite.cache.query.ScanQuery;

import javax.cache.Cache;
import javax.inject.Inject;
import java.nio.ByteBuffer;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import static com.r.bigconf.ignite.util.AsyncUtils.toCF;

public class IgniteConferenceService extends BaseConferenceService {

    private static final String CACHE_NAME = "conferencesCache";

    private final IgniteServiceHelper<UUID, Conference> ish;

    private final ConferenceDataProvider provider = new IgniteConferenceDataProvider(CACHE_NAME);

    @Inject
    public IgniteConferenceService(IgniteHolder igniteHolder) {
        ish = new IgniteServiceHelper<>(CACHE_NAME, igniteHolder, CacheMode.PARTITIONED, UUID.class, Conference.class);
    }

    @Override
    public CompletableFuture<Conference> startConference(User user) {
        //TODO run compute collocated
        Conference conference = createConferenceInstance(user);
        UUID id = conference.getId();
        IgniteConferenceProcess confProcess = new IgniteConferenceProcess(id, provider);
        return toCF(ish.getCache().putAsync(id,conference)).thenComposeAsync((nothing)->{
            ish.getIgnite().compute().run(confProcess);
            return toCF(ish.getCache().getAsync(id));
        });

    }

    @Override
    public void close() {
        //TODO
    }

    @Override
    public CompletableFuture<ByteBuffer> getForUser(String userId) {
        //TODO
        return CompletableFuture.completedFuture(null);
    }

    @Override
    public void addIncoming(String userId, ByteBuffer byteBuffer) {
        //TODO
    }

    @Override
    public CompletableFuture<List<Conference>> listAvailableConferences(User user) {
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
            //TODO
            return (Conference) ignite.cache(conferenceCacheName).get(conferenceId);
        }

        @Override
        public ConferenceProcessData getConferenceProcessData(Ignite ignite, UUID conferenceId) {
            //TODO
            return null;
        }
    }

}
