export const confStartedEvent = "confStarted";
export const confStoppedEvent = "confStopped";

export class ConfStartedEvent extends Event {
    constructor(conference) {
        super(confStartedEvent);
        this.conference = conference;
    }

    getConference() {
        return this.conference;
    }
}

export class ConfStoppedEvent extends Event {
    constructor(conferenceId) {
        super(confStoppedEvent);
        this.conferenceId = conferenceId;
    }

    getConferenceId() {
        return this.conferenceId;
    }
}