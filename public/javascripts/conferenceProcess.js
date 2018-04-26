import {confStartedEvent, ConfStoppedEvent, ConfUsersInfo, confUsersInfoEvent} from "./events.js";

export const ConfProcessComponent = {
    template: `
        <div class="confProcessBlock" >
            <div ng-if="confEnabled">
                <div class="controls">
                    <button class="muteButton" ng-click="toggleMuted()">Mute/unmute</button>
                    
                    TODO only for creator 
                    <button class="stopButton" ng-click="stopConf()">Stop</button>           
                </div>        
                <ul class="usersList"/>
            </div>
        </div>
        `,
    controller: ['$scope', 'restRoutes', 'settings', function ($scope, restRoutes, settings) {

        $scope.confEnabled = false;
        $scope.conferenceId = '951ac562-e633-48c3-8b18-9479f42c58ec';

        function getUrlWithConfId(url) {
            return url + "?confId=" + $scope.conferenceId;
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
                    makeXMLHttpRequest(getUrlWithConfId(restRoutes.uploadConfSoundData), file, function () {
                        console.log('Record uploaded');
                    });
                } else {
                    $scope.stopRecord();
                }
            };
        }

        function onMediaError(e) {
            console.error('media error', e);
            alert("error" + e.name)
        }

        function makeXMLHttpRequest(url, data, callback) {
            var request = new XMLHttpRequest();
            request.onreadystatechange = function () {
                if (request.readyState == 4 && request.status == 200) {
                    callback();
                }
            };
            request.open('POST', url);
            request.send(data);
        }

        $scope.reset = function () {
            $scope.confEnabled = false;
            $scope.recording = false;
            $scope.interval = settings.defaultRecordIntervalMs;
            $scope.conferenceId = null;
        };

        $scope.startRecord = function () {
            $scope.mediaRecorder.start($scope.interval);
        };

        $scope.stopRecord = function () {
            $scope.mediaRecorder.stop();
        };

        $scope.stopConf = function () {
            $scope.confEnabled = false;
            $.post(getUrlWithConfId(restRoutes.stopConference)).done(function () {
                let confId = $scope.conferenceId;
                $scope.reset();
                window.dispatchEvent(new ConfStoppedEvent(confId));
            });
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
            try {
                window.AudioContext = window.AudioContext || window.webkitAudioContext || window.mozAudioContext;
                let context = new AudioContext();
                let request = new XMLHttpRequest();
                request.responseType = 'arraybuffer';
                request.onload = function () {
                    if (request.status === 200) {
                        var source = context.createBufferSource();
                        context.decodeAudioData(request.response, function (buffer) {
                            source.buffer = buffer;
                        }, null);
                        source.connect(context.destination);
                        source.start(0);
                    }
                    let confUsersHeader = request.getResponseHeader("confUsers");
                    if(!!confUsersHeader) {
                        window.dispatchEvent(new ConfUsersInfo(confUsersHeader));
                    }
                };

                function confLoop() {
                    setTimeout(function () {
                        if ($scope.confEnabled) {
                            request.open('GET', getUrlWithConfId(restRoutes.getConfSoundData), true);
                            request.send();
                            confLoop();
                        }
                    }, $scope.interval);
                }
                $scope.confEnabled = true;
                confLoop();
                $scope.$applyAsync();
            } catch (e) {
                alert("Web Audio API not supported");
            }
        };


        window.addEventListener(confUsersInfoEvent, function (event) {
            $scope.users = event.getConfUsersList();
        });

        window.addEventListener(confStartedEvent, function (event) {
            $scope.conferenceId = event.getConference().id;
            $scope.interval = event.getConference().recordInterval;
            console.log("On conf started " + $scope.conferenceId + " with interval " + $scope.interval);
            if (!!$scope.conferenceId && !!$scope.interval) {
                $scope.startConf();
            }
            $scope.$applyAsync();
        })
    }]
};
