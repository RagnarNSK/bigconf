import {template} from "./templateUtil.js";

const confStartedEvent = "confStarted";
const confStoppedEvent = "confStopped";

/*
class Conference {
     id;
     recordInterval;
     createdBy;
     isActive;
}*/


class ConfStartedEvent extends Event{
    constructor(conference) {
        super(confStartedEvent);
        this.conference = conference;
    }
    getConference(){
        return this.conference;
    }
}

class ConfStoppedEvent extends Event{
    constructor(conferenceId) {
        super(confStoppedEvent);
        this.conferenceId = conferenceId;
    }
    getConferenceId(){
        return this.conferenceId;
    }
}

export class ConferenceListComponent {

    constructor(container, bus, settings) {
        this.container = container;
        this.bus = bus;
        this.settings = settings;
        this.listUrl = bigconfRestRoutes.conferencesList;
        this.startNewUrl = bigconfRestRoutes.startConference;
        this.content = `
        <div class="conferencesBlock">
            <div class="controls">
                <button class="createConfButton">Create</button>            
            </div>        
            <ul class="conferencesList"/>
        </div>
        `;

        this.listItemContent = `<li data-confId="{{id}}">Conf createdBy {{createdBy}}</li>`;
    }

    init() {
        var self = this;
        let content = $(self.content);
        content.find(".createConfButton").click(function () {
            $.post(self.startNewUrl).done(function (conference) {
                self.bus.dispatchEvent(new ConfStartedEvent(conference));
                self.refresh(false);
            });
        });
        $(self.container).append(content);
        self.refresh(true);
        self.bus.addEventListener(confStoppedEvent, function(event) {
            console.log("Conference " + event.getConferenceId() + " stopped");
            self.refresh(false);
        });
    }

    refresh(schedule) {
        var self = this;
        let list = $(self.container).find(".conferencesList");
        $.getJSON(self.listUrl).done(function (data) {
            list.empty();
            data.forEach(function (conf) {
                list.append(template(self.listItemContent, conf));
            });
            if(schedule) {
                setTimeout(function(){self.refresh(true)}, self.settings.conferenceListUpdateIntervalMs);
            }
        });
    }
}


export class ConfProcessComponent {

    constructor(container,  bus, settings) {
        this.container = container;
        this.bus = bus;
        this.settings = settings;
        this.uploadConfSoundDataURL = bigconfRestRoutes.uploadConfSoundData;
        this.getConfSoundDataURL = bigconfRestRoutes.getConfSoundData;
        this.stopConfUrl = bigconfRestRoutes.stopConference;
        this.content = `
        <div class="confProcessBlock">
            <div class="controls">
                <button class="muteButton">Mute/unmute</button>
                
                TODO only for creator 
                <button class="stopButton">Stop</button>           
            </div>        
            <ul class="usersList"/>
        </div>
        `;
        this.reset();

        this.listItemContent = `<li data-userId="{{id}}">{{name}}</li>`;
        this.mediaRecorder = null;
    }

    reset() {
        this.confEnabled = false;
        this.recording = false;
        this.interval = this.settings.defaultRecordIntervalMs;
        this.conferenceId = null;
    }

    startRecord() {
        this.mediaRecorder.start(self.interval);
    }

    stopRecord() {
        this.mediaRecorder.stop();
    }

    stopConf() {
        let self = this;
        self.confEnabled = false;
        $.post(self.getUrlWithConfId(self.stopConfUrl)).done(function(){
            let confId = self.conferenceId;
            self.reset();
            $(self.container).find(".confProcessBlock").remove();
            self.bus.dispatchEvent(new ConfStoppedEvent(confId));
        });
    }

    startConf() {
        let self = this;
        try {
            window.AudioContext = window.AudioContext || window.webkitAudioContext || window.mozAudioContext;
            let context = new AudioContext();
            let request = new XMLHttpRequest();
            request.responseType = 'arraybuffer';
            request.onload = function () {
                if (request.status === 200) {
                    var source = context.createBufferSource();
                    context.decodeAudioData(request.response, function (buffer) {
                        source.buffer = buffer;
                    }, null);
                    source.connect(context.destination);
                    source.start(0);
                }
            };
            function confLoop() {
                setTimeout(function () {
                    if (self.confEnabled) {
                        request.open('GET', self.getUrlWithConfId(self.getConfSoundDataURL), true);
                        request.send();
                        confLoop();
                    }
                }, self.interval);
            }
            self.confEnabled = true;
            confLoop();
        } catch (e) {
            alert("Web Audio API not supported");
        }
    }


    init() {
        let self = this;
        const mediaConstraints = {
            audio: true
        };
        navigator.getUserMedia(mediaConstraints, onMediaSuccess, onMediaError);
        function onMediaSuccess(stream) {
            self.mediaRecorder = new MediaStreamRecorder(stream);
            let mimeType = 'audio/wav';
            self.mediaRecorder.mimeType = mimeType;
            self.mediaRecorder.audioChannels = 1;
            self.mediaRecorder.ondataavailable = function (blob) {
                if(self.recording) {
                    var file = new File([blob], 'audio-record', {
                        type: mimeType
                    });
                    makeXMLHttpRequest(self.getUrlWithConfId(self.uploadConfSoundDataURL), file, function () {
                        console.log('Record uploaded');
                    });
                } else {
                    self.stopRecord();
                }
            };
        }


        function onMediaError(e) {
            console.error('media error', e);
            alert("error" + e.name)
        }

        function makeXMLHttpRequest(url, data, callback) {
            var request = new XMLHttpRequest();
            request.onreadystatechange = function () {
                if (request.readyState == 4 && request.status == 200) {
                    callback();
                }
            };
            request.open('POST', url);
            request.send(data);
        }


        this.bus.addEventListener(confStartedEvent, function(event){
            self.conferenceId = event.getConference().id;
            self.interval = event.getConference().recordInterval;
            console.log("On conf started "+self.conferenceId + " with interval " + self.interval);
            if(!!self.conferenceId && !!self.interval) {
                let block = $(self.content);
                let muteButton = block.find(".muteButton");
                muteButton.click(function () {
                    muteButton.prop('disabled', true);
                    if(self.recording) {
                        self.stopRecord()
                    } else {
                        self.startRecord()
                    }
                    self.recording = !self.recording;
                    muteButton.prop('disabled', false);
                });

                block.find(".stopButton").click(function(){
                    self.stopConf();
                });

                $(self.container).append(block);
                self.startConf();
            }
        })
    }

    getUrlWithConfId(url) {
        return url + "?confId=" + this.conferenceId;
    }
}