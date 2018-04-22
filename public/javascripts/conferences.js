import {template} from "./templateUtil.js";

export class ConferenceListComponent {

    constructor(container, settings) {
        this.container = container;
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

        this.listItemContent = `<li data-confId="{{id}}">Conf createdBy {{createBy}}</li>`;
    }

    init() {
        var self = this;
        let content = $(self.content);
        content.find(".createConfButton").click(function () {
            $.post(self.startNewUrl).done(function (conference) {
                console.log("conference create: " + conference.id);
            });
        });
        $(self.container).append(content);
        self.refresh();
    }

    refresh() {
        var self = this;
        let list = $(self.container).find(".conferencesList");
        $.getJSON(self.listUrl).done(function (data) {
            list.empty();
            data.forEach(function (conf) {
                list.append(template(self.listItemContent, conf));
            });
            setTimeout(function(){self.refresh()}, self.settings.conferenceListUpdateIntervalMs);
        });
    }
}
