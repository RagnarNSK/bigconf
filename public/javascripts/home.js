import {template} from "./templateUtil.js";


export class UsersList {
    constructor(container) {
        this.container = container;
        this.url = bigconfRestRoutes.usersList;
        this.content = `<li >{{name}}</li>`;
    }

    init() {
        let self = this;
        $(self.container).append("Initializing");
        $.getJSON(this.url).done(function (data) {
            $(self.container).empty();
            data.forEach(function (user) {
                let filledTemplate = template(self.content, user);
                let element = $(filledTemplate);
                element.click(function () {
                    self.onclick(user.id)
                });
                $(self.container).append(element)
            })
        });
    }

    onclick(id) {
        console.log("user " + id + " clicked");
    }
}

export class MyUserComponent {
    constructor(container) {
        this.container = container;
        this.url = bigconfRestRoutes.usersMe;
        this.content = `
<div class="user-block">
    <h2>{{name}}</h2>
</div>
`;
    }

    init() {
        let self = this;
        $.getJSON(this.url).done(function (data) {
            $(self.container).empty();
            let filledTemplate = template(self.content, data);
            $(self.container).append(filledTemplate);
        });
    }

}
