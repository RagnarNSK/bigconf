import {UsersList, MyUserComponent} from './home.js';
import {TestConf} from "./testConf.js";
import {ConferenceListComponent} from "./conferences.js";

$.getJSON(bigconfRestRoutes.settings).done(startApp);

function startApp(settings){
    $.when($.ready).then(function () {
        let usersList = $(`<div id="UsersList"></div>`);
        let myUser = $(`<div id="MyUser"></div>`);
        let confList = $(`<div id="ConfList"></div>`);

        let root = $("#root");
        let splashscreen = $("#splashscreen");
        root.hide();
        root.append(myUser);
        root.append(usersList);
        root.append(confList);

        new MyUserComponent(myUser).init();
        new UsersList(usersList).init();
        new ConferenceListComponent(confList,settings).init();

        new TestConf(root).init(settings);
        splashscreen.hide();
        root.show();
    });
}