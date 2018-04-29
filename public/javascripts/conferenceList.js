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
                <li ng-repeat="conf in confs" ng-click="confClick(conf.id)">
                    <span>Conference created by {{conf.createdBy}}</span>
                    <span ng-if="conf.joined">You are joined to this conf</span> 
                </li>
            </ul>
</div>
`,
    controller: ['$scope', 'confService', 'userService', 'settings', 'eventBus', async function ($scope, confService, userService, settings, bus) {
        $scope.confs = [];

        $scope.createConference = async function () {
            let conference = await confService.createConference();
            bus.dispatchEvent(new ConfStartedEvent(conference));
            refresh(false);
        };
        $scope.confClick = function (id) {
            console.log("Conf " + id + " clicked");
        };

        let refresh = async function (schedule) {
            let currentUser = await userService.getCurrentUser();
            $scope.confs = await confService.list();
            $scope.confs.forEach(conf=>{
                if(!!conf.userIds){
                    conf.joined = conf.userIds.indexOf(currentUser.id) > -1;
                }
            });
            $scope.$applyAsync();
            if (schedule) {
                setTimeout(async function () {
                    await refresh(true)
                }, settings.conferenceListUpdateIntervalMs);
            }
        };
        await refresh(true);

        bus.addEventListener(confStoppedEvent, function (event) {
            refresh(false);
        })
    }]
};


