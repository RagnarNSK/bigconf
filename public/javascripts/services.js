import {ConfLeftEvent, ConfStartedEvent, ConfStoppedEvent, ConfUsersInfo} from "./events.js";

class BaseJSONService {
    constructor(url) {
        this.get = async function () {
            return new Promise((resolve, reject) => {
                $.getJSON(url)
                    .done((data) => {
                        resolve(data);
                    })
                    .fail(() => {
                        reject();
                    });
            })
        }
        this.post = async function () {
            return new Promise((resolve, reject) => {
                $.post(url)
                    .done((data) => {
                        resolve(data);
                    })
                    .fail(() => {
                        reject();
                    });
            })
        }
    }
}

class BaseSingleFetchService {
    constructor(url) {
        const instance = this;
        instance.data = null;
        instance.initializing = false;
        instance.pending = [];
        instance.get = async function () {
            if (!instance.data) {
                if (!instance.initializing) {
                    instance.initializing = true;
                    return new Promise((resolve, reject) => {
                            $.getJSON(url)
                                .done(function (data) {
                                    instance.data = data;
                                    resolve(data);
                                    instance.pending.forEach(promise => {
                                        promise.resolve(data);
                                    })
                                })
                                .fail(error => {
                                    reject();
                                    instance.pending.forEach(promise => {
                                        promise.reject();
                                    })
                                });
                        }
                    );
                } else {
                    return new Promise((resolve, reject) => {
                        instance.pending.push({resolve: resolve, reject: reject});
                    });
                }
            } else {
                return new Promise(resolve => {
                    resolve(instance.data)
                })
            }
        }
    }
}


export const UserService = ['restRoutes', function (restRoutes) {
    const instance = {};
    instance.getCurrentUser = new BaseSingleFetchService(restRoutes.usersMe).get;
    instance.getAllUsers = new BaseSingleFetchService(restRoutes.usersList).get;
    return instance;
}];


export const ConferenceService = ['restRoutes', 'eventBus', function (restRoutes, bus) {
    const instance = {};
    instance.conference = null;
    instance.list = new BaseJSONService(restRoutes.conferencesList).get;


    function onJoinConf(conference) {
        instance.conference = conference;
        bus.dispatchEvent(new ConfStartedEvent(conference));
    }

    instance.createConference = async function () {
        let conference = await new BaseJSONService(restRoutes.startConference).post();
        onJoinConf(conference);
    };

    function getUrlWithConfId(url, confId) {
        return url + "?confId=" + confId;
    }

    try {
        window.AudioContext = window.AudioContext || window.webkitAudioContext || window.mozAudioContext;
        let context = new AudioContext();
        let request = new XMLHttpRequest();
        request.responseType = 'arraybuffer';
        request.onload = function () {
            if (request.status === 404) {
                bus.dispatchEvent(new ConfLeftEvent(instance.conference));
                instance.conference = null;
                if (!!instance.promise) {
                    instance.promise.reject();
                }
            } else {
                if (request.status === 200) {
                    const source = context.createBufferSource();
                    context.decodeAudioData(request.response, function (buffer) {
                        source.buffer = buffer;
                    }, null);
                    source.connect(context.destination);
                    source.start(0);
                    if (!!instance.promise) {
                        instance.promise.resolve();
                    }
                }
                let confUsersHeader = request.getResponseHeader("confUsers");
                if (!!confUsersHeader) {
                    bus.dispatchEvent(new ConfUsersInfo(confUsersHeader));
                }
            }
        };

        function makeXMLHttpRequest(url, data, callback) {
            const request = new XMLHttpRequest();
            request.onreadystatechange = function () {
                if (request.readyState == 4 && request.status == 200) {
                    callback();
                }
            };
            request.open('POST', url);
            request.send(data);
        };

        instance.getAndPlayConfSound = function () {
            if (!!instance.conference) {
                const confId = instance.conference.id;
                request.open('GET', getUrlWithConfId(restRoutes.getConfSoundData, confId), true);
                request.send();
            }
        };
        instance.uploadConfSound = function (file) {
            if (!!instance.conference) {
                const confId = instance.conference.id;
                makeXMLHttpRequest(getUrlWithConfId(restRoutes.uploadConfSoundData, confId), file, function () {
                    console.log('Record uploaded');
                });
            }
        };
    } catch (e) {
        alert("Web Audio API not supported");
        instance.getAndPlayConfSound = function () {
            console.log("audio API not supported")
        };
        instance.uploadConfSound = function () {
            console.log("audio API not supported")
        };
    }

    instance.stopConference = async function () {
        const confId = instance.conference.id;
        return new Promise((resolve, reject) => {
            $.post(getUrlWithConfId(restRoutes.stopConference, confId))
                .done(() => {
                    instance.conference = null;
                    bus.dispatchEvent(new ConfStoppedEvent(confId));
                    resolve();
                })
                .fail(() => {
                    //TODO check if needed to reset conference
                    //instance.conference = null;
                    reject();
                });
        })
    };

    instance.joinConference = async function (id) {
        let conference = await new BaseJSONService(getUrlWithConfId(restRoutes.joinConference, id)).post();
        onJoinConf(conference);
    };

    instance.leaveConference = async function () {
        if (!!instance.conference) {
            let conference = await new BaseJSONService(getUrlWithConfId(restRoutes.leaveConference, instance.conference.id)).post();
            bus.dispatchEvent(new ConfLeftEvent(conference));
        }
    };

    instance.selectConference = function (conference) {
        instance.conference = conference;
        bus.dispatchEvent(new ConfStartedEvent(conference));
    };

    instance.getCurrentConferenceId = function () {
        if (!!instance.conference) {
            return instance.conference.id;
        } else {
            return null;
        }
    };

    return instance;
}];
