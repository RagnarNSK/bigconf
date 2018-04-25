import {UsersListComponent, MyUserComponent} from './home.js';
import {ConferenceListComponent} from "./conferenceList.js";
import {ConfProcessComponent} from "./conferenceProcess.js";

$.getJSON(bigconfRestRoutes.settings).done(startApp);

function startApp(settings){
    $.when($.ready).then(function () {
        let module = angular.module('bigConfApp', []);
        module.constant('restRoutes', bigconfRestRoutes);

        let usersList = $(`<users-list />`);
        let myUser = $(`<my-user />`);
        let confList = $(`<conferences-list>`);
        let confProcess = $(`<div id="ConfProcess"></div>`);

        let root = $("#root");
        let splashscreen = $("#splashscreen");
        root.hide();
        root.append(myUser);
        root.append(usersList);
        root.append(confList);
        root.append(confProcess);

        module.constant("settings", settings);
        module.component("myUser", MyUserComponent);
        module.component("usersList", UsersListComponent);
        module.component("conferencesList", ConferenceListComponent);
        new ConfProcessComponent(confProcess, window, settings).init();

        splashscreen.hide();
        root.show();


        angular.element(function() {
            angular.bootstrap(document, ['bigConfApp']);
        });
    });
}