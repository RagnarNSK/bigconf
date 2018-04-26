import {ConfStartedEvent, confStoppedEvent} from "./events.js";

/*
class Conference {
     id;
     recordInterval;
     createdBy;
     isActive;
}*/
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
    controller: ['$scope', 'restRoutes', 'settings', 'eventBus', function ($scope, restRoutes, settings, bus) {
        $scope.confs = [];

        $scope.createConference = function () {
            $.post(restRoutes.startConference).done(function (conference) {
                bus.dispatchEvent(new ConfStartedEvent(conference));
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

        bus.addEventListener(confStoppedEvent, function (event) {
            refresh(false);
        })
    }]
};


