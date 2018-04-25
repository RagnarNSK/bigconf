import {ConfStartedEvent, confStoppedEvent} from "./events.js";

/*
class Conference {
     id;
     recordInterval;
     createdBy;
     isActive;
}*/
//TODO confStoppedEvent and bus
export const ConferenceListComponent = {
    template: `
<div class="conferencesBlock">
            <div class="controls">
                <button class="createConfButton" ng-click="createConference()">Create</button>            
            </div>        
            <ul class="conferencesList">
                <li ng-repeat="conf in confs" ng-click="confClick(conf.id)">Conference created by {{conf.createdBy}}</li>
            </ul>
</div>
`,
    controller: ['$scope', 'restRoutes', 'settings', function ($scope, restRoutes, settings) {
        $scope.confs = [];

        $scope.createConference = function () {
            $.post(restRoutes.startConference).done(function (conference) {
                window.dispatchEvent(new ConfStartedEvent(conference));
                refresh(false);
            });
        };
        $scope.confClick = function (id) {
            console.log("Conf " + id + " clicked");
        };

        let refresh = function (schedule) {
            $.getJSON(restRoutes.conferencesList).done(function (data) {
                $scope.confs = data;
                $scope.$applyAsync();
                if (schedule) {
                    setTimeout(function () {
                        refresh(true)
                    }, settings.conferenceListUpdateIntervalMs);
                }
            });
        };
        refresh(true);
    }]
};


