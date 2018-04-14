package com.r.bigconf.manager;

import com.r.bigconf.model.Conference;
import com.r.bigconf.model.User;
import com.r.bigconf.processing.ConfProcess;
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
        ConfProcess process = new ConfProcess(conference);
        getActiveProcesses().add(process);
        threadPoolExecutor.execute(process);
        return conference;
    }


}
