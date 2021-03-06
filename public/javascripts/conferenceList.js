import {confLeftEvent, confStartedEvent, confStoppedEvent} from "./events.js";

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
                    <span ng-if="conf.joined">
                        <span>You are joined to this conf</span>
                        <span ng-if="!conf.active"><button ng-click="selectConf(conf)">Select</button></span>
                    </span> 
                    <span ng-if="!conf.joined">
                        <button ng-click="joinConf(conf.id)">Join</button>
                    </span>
                </li>
            </ul>
</div>
`,
    controller: ['$scope', 'confService', 'userService', 'settings', 'eventBus', async function ($scope, confService, userService, settings, bus) {
        $scope.confs = [];

        $scope.createConference = async function () {
            await confService.createConference();
        };
        $scope.confClick = function (id) {
            console.log("Conf " + id + " clicked");
        };
        $scope.selectConf = function (conference) {
            confService.selectConference(conference);
        };
        $scope.joinConf = function (id) {
            confService.joinConference(id);
        };

        let refresh = async function () {
            let currentUser = await userService.getCurrentUser();
            let currentConferenceId = confService.getCurrentConferenceId();
            $scope.confs = await confService.list();
            $scope.confs.forEach(conf=>{
                if(!!conf.userIds){
                    conf.joined = conf.userIds.indexOf(currentUser.id) > -1;
                    conf.active = currentConferenceId === conf.id;
                }
            });
            $scope.$apply();
        };

        bus.addEventListener(confStoppedEvent, function (event) {
            let conferenceId = event.getConferenceId();
            let removedIndex = $scope.confs.findIndex((conf)=>{
                return conferenceId === conf.id;
            });
            $scope.confs.splice(removedIndex,1);
        });

        bus.addEventListener(confLeftEvent, refresh);
        bus.addEventListener(confStartedEvent, refresh);


        setInterval(refresh, settings.conferenceListUpdateIntervalMs);
    }]
};


