export const confStartedEvent = "confStarted";
export const confStoppedEvent = "confStopped";
export const confLeftEvent = "confLeft";
export const confUsersInfoEvent = "confUsers";

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
export class ConfLeftEvent extends Event {
    constructor(conference) {
        super(confLeftEvent);
        this.conference = conference;
    }
    getConference() {
        return this.conference;
    }
}

export class ConfUsersInfo extends Event{
    constructor(confUsersJsonString) {
        super(confUsersInfoEvent);
        this.confUsersJsonString = confUsersJsonString;
    }
    getConfUsersList() {
        let list = JSON.parse(this.confUsersJsonString);
        let ret = [];
        list.forEach(function(userShortInfo){
            ret.push({
                id: userShortInfo.u,
                name: "TODO name for user "+ userShortInfo.u,
                muted: !!userShortInfo.m,
                speaking: !!userShortInfo.s
            })
        });
        return ret;
    }
}