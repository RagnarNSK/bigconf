package com.r.bigconf.processing;

import com.r.bigconf.model.Conference;

public class ConfProcess implements Runnable {

    private final Conference conference;
    public boolean isActive;

    public ConfProcess(Conference conference) {
        this.conference = conference;
    }

    @Override
    public void run() {
        while (isActive){

        }

    }
}
