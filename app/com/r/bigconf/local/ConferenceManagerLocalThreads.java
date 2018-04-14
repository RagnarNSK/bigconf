package com.r.bigconf.local;

import com.r.bigconf.core.manager.BaseConferenceManager;
import com.r.bigconf.core.manager.ConferenceManager;
import com.r.bigconf.core.model.Conference;
import com.r.bigconf.core.model.User;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class ConferenceManagerLocalThreads extends BaseConferenceManager implements ConferenceManager {

    private final ThreadPoolExecutor threadPoolExecutor = new ThreadPoolExecutor(1, 5,
            60L, TimeUnit.SECONDS,
            new ArrayBlockingQueue<Runnable>(1));


    @Override
    public Conference startConference(User user) {
        Conference conference = createConferenceInstance(user);
        SingleThreadConferenceProcess process = new SingleThreadConferenceProcess(conference);
        getActiveProcesses().add(process);
        threadPoolExecutor.execute(process);
        return conference;
    }


}
