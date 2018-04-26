import {UsersListComponent, MyUserComponent} from './users.js';
import {ConferenceListComponent} from "./conferenceList.js";
import {ConfProcessComponent} from "./conferenceProcess.js";

$.getJSON(bigconfRestRoutes.settings).done(startApp);

function startApp(settings){
    $.when($.ready).then(function () {
        let module = angular.module('bigConfApp', []);
        module.constant('restRoutes', bigconfRestRoutes);

        let appContent = $(`
   <div class="globalUsersList" ng-controller="GlobalUsersList">
        <users-list users="globalUsers"/>   
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
        module.component("myUser", MyUserComponent);
        module.component("usersList", UsersListComponent);
        module.component("conferencesList", ConferenceListComponent);
        module.component("confProcess", ConfProcessComponent);
        module.controller("GlobalUsersList",['$scope','restRoutes', function ($scope, restRoutes) {
            $scope.globalUsers = [{id:0, name:'test'}];
            $.getJSON(restRoutes.usersList).done(function (data) {
                $scope.globalUsers = data;
                $scope.$digest();
            });
        }]);

        splashscreen.hide();
        root.show();


        angular.element(function() {
            angular.bootstrap(document, ['bigConfApp']);
        });
    });
}