import {UsersListComponent, MyUserComponent} from './users.js';
import {ConferenceListComponent} from "./conferenceList.js";
import {ConfProcessComponent} from "./conferenceProcess.js";
import {UserService} from "./services.js";

$.getJSON(bigconfRestRoutes.settings).done(startApp);

function startApp(settings){
    $.when($.ready).then(function () {
        let module = angular.module('bigConfApp', []);
        module.constant('restRoutes', bigconfRestRoutes);

        let appContent = $(`
   <div class="globalUsersList" ng-controller="GlobalUsersList">
        <users-list users="globalUsers" on-click="globalUserClick(userId)"/>   
   </div>     
   <my-user />
   <conferences-list />
   <conf-process />
`);

        let root = $("#root");
        let splashscreen = $("#splashscreen");
        root.hide();
        root.append(appContent);

        module.constant("settings", settings);
        module.constant("eventBus", window);
        module.service("userService", UserService);
        module.component("myUser", MyUserComponent);
        module.component("usersList", UsersListComponent);
        module.component("conferencesList", ConferenceListComponent);
        module.component("confProcess", ConfProcessComponent);
        module.controller("GlobalUsersList",['$scope','userService', async function ($scope, userService) {
            $scope.globalUsers = await userService.getAllUsers();
            $scope.globalUserClick = function (userId) {
                console.log("Global user " + userId + " clicked");
            }
        }]);

        splashscreen.hide();
        root.show();


        angular.element(function() {
            angular.bootstrap(document, ['bigConfApp']);
        });
    });
}