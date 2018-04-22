import {UsersList, MyUserComponent} from './home.js';
import {ConferenceListComponent} from "./conferenceList.js";
import {ConfProcessComponent} from "./conferenceProcess.js";

$.getJSON(bigconfRestRoutes.settings).done(startApp);

function startApp(settings){
    $.when($.ready).then(function () {
        let usersList = $(`<div id="UsersList"></div>`);
        let myUser = $(`<div id="MyUser"></div>`);
        let confList = $(`<div id="ConfList"></div>`);
        let confProcess = $(`<div id="ConfProcess"></div>`);

        let root = $("#root");
        let splashscreen = $("#splashscreen");
        root.hide();
        root.append(myUser);
        root.append(usersList);
        root.append(confList);
        root.append(confProcess);

        new MyUserComponent(myUser).init();
        new UsersList(usersList).init();
        new ConferenceListComponent(confList, window, settings).init();
        new ConfProcessComponent(confProcess, window, settings).init();

        splashscreen.hide();
        root.show();
    });
}