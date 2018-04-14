package com.r.bigconf.local;

import com.r.bigconf.core.manager.BaseConferenceManager;
import com.r.bigconf.core.manager.ConferenceManager;
import com.r.bigconf.core.model.Conference;
import com.r.bigconf.core.model.User;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class ConferenceManagerLocalThreads extends BaseConferenceManager implements ConferenceManager {

    private final ThreadPoolExecutor threadPoolExecutor = new ThreadPoolExecutor(1, 5,
            60L, TimeUnit.SECONDS,
            new ArrayBlockingQueue<Runnable>(1));

    private final Set<SingleThreadConferenceProcess> activeProcesses = new HashSet<>();

    @Override
    public Conference startConference(User user) {
        Conference conference = createConferenceInstance(user);
        SingleThreadConferenceProcess process = new SingleThreadConferenceProcess(conference);
        activeProcesses.add(process);
        threadPoolExecutor.execute(process);
        return conference;
    }

    @Override
    public void close() {
        //TODO check if it's enough
        activeProcesses.forEach(confProcess -> confProcess.getConference().setActive(false));
    }

    @Override
    public Conference getConference(UUID conferenceId) {
        return activeProcesses.stream()
                .filter(confProcess -> conferenceId.equals(confProcess.getConference().getId()))
                .map(SingleThreadConferenceProcess::getConference)
                .findFirst().orElse(null);
    }
}
