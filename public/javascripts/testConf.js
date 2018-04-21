
export class TestConf {
    constructor(container){
        this.container = container;
        this.content = `
    <div id="testConfBlock">
        <h1>Test conf block</h1>

        <button id="startConf">Start conf</button>
        <button id="stopConf">Stop conf</button>

        <button id="startButton">Start record</button>
        <button id="stopButton">Stop record</button>


    </div>
`
    }

    init(settings){
        var self = this;
        var interval = settings.defaultRecordIntervalMs;

        let block = $(self.content);
        $(self.container).append(block);

        var mediaConstraints = {
            audio: true
        };
        var uploadConfSoundDataURL = bigconfRestRoutes.uploadConfSoundData;
        var getConfSoundDataURL = bigconfRestRoutes.getConfSoundData;
        navigator.getUserMedia(mediaConstraints, onMediaSuccess, onMediaError);
        function onMediaSuccess(stream) {
            var mediaRecorder = new MediaStreamRecorder(stream);
            var mimeType = 'audio/wav';
            mediaRecorder.mimeType = mimeType;
            mediaRecorder.audioChannels = 1;
            mediaRecorder.ondataavailable = function (blob) {
                var file = new File([blob], 'audio-record', {
                    type: mimeType
                });
                makeXMLHttpRequest(uploadConfSoundDataURL, file, function () {
                    console.log('Record uploaded');
                });
            };

            function startRecord() {
                mediaRecorder.start(interval);
            }

            function stopRecord() {
                mediaRecorder.stop();
            }

            $('#startButton').click(startRecord);
            $('#stopButton').click(stopRecord);
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

        var confEnabled = false;

        function stopConf() {
            confEnabled = false;
        }

        function startConf() {
            try {
                window.AudioContext = window.AudioContext || window.webkitAudioContext || window.mozAudioContext;
                var context = new AudioContext();
                var request = new XMLHttpRequest();
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
                };

                function confLoop() {
                    setTimeout(function () {
                        if (confEnabled) {
                            request.open('GET', getConfSoundDataURL, true);
                            request.send();
                            confLoop();
                        }
                    }, interval);
                }

                confEnabled = true;
                confLoop();
            } catch (e) {
                alert("Web Audio API not supported");
            }
        }

        $('#startConf').click(startConf);
        $('#stopConf').click(stopConf);
    }
}