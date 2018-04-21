export class ConferenceListComponent {

    constructor(container) {
        this.container = container;
        this.content = `
        <div class="conferencesBlock">
            <div class="controls">
                <button class="createConfButton">Create</button>            
            </div>        
        </div>
        `;
    }

    init() {
        var self = this;
        let content = $(self.content);
        content.find(".createConfButton").click(function () {
            $.post()
        });
        $(self.container).append(content)
    }
}
