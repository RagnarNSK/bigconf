import {template} from "./templateUtil.js";
import {ConfStartedEvent, confStoppedEvent} from "./events.js";


/*
class Conference {
     id;
     recordInterval;
     createdBy;
     isActive;
}*/


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

        this.listItemContent = `<li data-conf-id="{{id}}">Conf createdBy {{createdBy}}</li>`;
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
                let item = $(template(self.listItemContent, conf));
                item.click(function(){
                   console.log("Conf "+ $(this).data("conf-id")+" clicked");
                });
                list.append(item);
            });
            if(schedule) {
                setTimeout(function(){self.refresh(true)}, self.settings.conferenceListUpdateIntervalMs);
            }
        });
    }

    join(conferenceId) {
        //TODO
    }
}


