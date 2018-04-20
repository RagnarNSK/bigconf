const listItemTemplate = `<li >{{name}}</li>`;
export class UsersList {
    constructor(container) {
        this.container = container;
        this.url = bigconfRestRoutes.usersList;
    }
    init() {
        let container = this.container;
        let self = this;
        $(container).append("Initializing");
        $.getJSON(this.url).done(function (data) {
            $(container).empty();
            data.forEach(function (user) {
                let filledTemplate = listItemTemplate.replace(/{{name}}/g, user.name);
                let element = $(filledTemplate);
                element.click(function () {
                    self.onclick(user.id)
                });
                $(container).append(element)
            })
        });
    }
    onclick(id) {
        console.log("user " + id + "clicked");
    }
}


const myUserTemplate = `Hello {{name}}`;
export class MyUserComponent {
    constructor(container) {
        this.container = container;
        this.url = bigconfRestRoutes.usersMe;
    }
    init(){
        let container = this.container;
        let self = this;
        $.getJSON(this.url).done(function (data) {
            $(container).empty();
            let filledTemplate = myUserTemplate.replace(/{{name}}/g, data.name);
            $(container).append(filledTemplate);
        });
    }

}
