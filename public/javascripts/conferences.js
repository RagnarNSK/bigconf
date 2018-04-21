export class ConferenceListComponent {

    constructor(container) {
        this.container = container;
        this.listUrl = bigconfRestRoutes.conferencesList;
        this.content = `
        <div class="conferencesBlock">
            <div class="controls">
                <button class="createConfButton">Create</button>            
            </div>        
            <ul class="conferencesList"/>
        </div>
        `;
    }

    init() {
        var self = this;
        let content = $(self.content);
        content.find(".createConfButton").click(function () {
            console.log("TODO create conf");
        });
        $.getJSON()
        content.find(".conferencesList")

        $(self.container).append(content)
    }
}
