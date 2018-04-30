import {confLeftEvent, confStartedEvent, confStoppedEvent, confUsersInfoEvent} from "./events.js";

export const ConfProcessComponent = {
    template: `
        <div class="confProcessBlock" >
            <div ng-if="confEnabled">
                <div class="controls">
                    <button class="muteButton" ng-click="toggleMuted()">{{recording?'Mute':'Unmute'}}</button>                    
                    <button class="leaveButton" ng-click="leaveConf()" ng-if="currentUser.id!=conferenceMaster">Leave</button> 
                    <button class="stopButton" ng-click="stopConf()" ng-if="currentUser.id==conferenceMaster">Stop</button>  
                </div>        
                <ul><li ng-repeat="user in users" ng-click="confUserClick(user.id)">{{user.name}}, muted={{user.muted}}, speaking={{user.speaking}}</li></ul>
            </div>
        </div>
        `,
    controller: ['$scope', 'confService', 'settings', 'eventBus', 'userService', function ($scope, confProcessService, settings, bus, userService) {

        async function init() {
            $scope.currentUser = await userService.getCurrentUser();
        }

        init().then(() => {
            $scope.$applyAsync();
        });
        $scope.confEnabled = false;
        $scope.conferenceId = null;
        $scope.recording = false;
        $scope.getSoundInterval = null;

        $scope.confUserClick = function (confUserId) {
            console.log("Conf user " + confUserId + " clicked");
        }

        const mediaConstraints = {
            audio: true
        };
        navigator.getUserMedia(mediaConstraints, onMediaSuccess, onMediaError);

        function onMediaSuccess(stream) {
            $scope.mediaRecorder = new MediaStreamRecorder(stream);
            let mimeType = 'audio/wav';
            $scope.mediaRecorder.mimeType = mimeType;
            $scope.mediaRecorder.audioChannels = 1;
            $scope.mediaRecorder.ondataavailable = function (blob) {
                if ($scope.recording) {
                    var file = new File([blob], 'audio-record', {
                        type: mimeType
                    });
                    confProcessService.uploadConfSound(file)
                } else {
                    $scope.stopRecord();
                }
            };
        }

        function onMediaError(e) {
            console.error('media error', e);
            alert("error" + e.name)
        }

        $scope.reset = function () {
            $scope.confEnabled = false;
            $scope.recording = false;
            $scope.interval = settings.defaultRecordIntervalMs;
            if(!!$scope.getSoundInterval) {
                clearInterval($scope.getSoundInterval);
            }
        };

        $scope.startRecord = function () {
            $scope.mediaRecorder.start($scope.interval);
        };

        $scope.stopRecord = function () {
            $scope.mediaRecorder.stop();
        };

        $scope.stopConf = function () {
            confProcessService.stopConference();
        };

        $scope.leaveConf = function() {
            confProcessService.leaveConference();
        };

        $scope.toggleMuted = function () {
            if ($scope.recording) {
                $scope.stopRecord()
            } else {
                $scope.startRecord()
            }
            $scope.recording = !$scope.recording;
        };


        $scope.startConf = function () {
            $scope.confEnabled = true;
            $scope.getSoundInterval = setInterval(function () {
                if ($scope.confEnabled) {
                    confProcessService.getAndPlayConfSound();
                } else {
                    console.log("Warning! Conference loop executing while conf disabled");
                }
            }, $scope.interval);
        };


        bus.addEventListener(confUsersInfoEvent, function (event) {
            $scope.users = event.getConfUsersList();
            $scope.$applyAsync();
        });

        bus.addEventListener(confStartedEvent, function (event) {
            let id = event.getConference().id;
            $scope.interval = event.getConference().recordInterval;
            $scope.conferenceMaster = event.getConference().createdBy;
            console.log("On conf started " + id + " with interval " + $scope.interval);
            if (!!id && !!$scope.interval) {
                $scope.startConf();
            }
            $scope.$applyAsync();
        });
        bus.addEventListener(confLeftEvent, function (event) {
            $scope.reset();
            $scope.$applyAsync();
        });
        bus.addEventListener(confStoppedEvent, function (event) {
            $scope.reset();
            $scope.$applyAsync();
        });
    }]
};

