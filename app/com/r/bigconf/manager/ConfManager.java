package com.r.bigconf.manager;

import com.r.bigconf.model.Conference;
import com.r.bigconf.model.User;
import com.r.bigconf.processing.ConfProcess;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class ConfManager {

    private final ThreadPoolExecutor threadPoolExecutor = new ThreadPoolExecutor(1, 5,
            60L, TimeUnit.SECONDS,
            new ArrayBlockingQueue<Runnable>(1));

    private final Set<ConfProcess> activeProcesses = new HashSet<>();

    public Conference startConference(User user) {
        Conference conference = new Conference();
        conference.getUsers().add(user);
        ConfProcess process = new ConfProcess(conference);
        activeProcesses.add(process);
        threadPoolExecutor.execute(process);
        return conference;
    }


}
