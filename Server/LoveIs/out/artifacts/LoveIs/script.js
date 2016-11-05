var webSocket;
var messages = document.getElementById("messages");


function openSocket() {
    if (webSocket !== undefined && webSocket.readyState !== WebSocket.CLOSED) {
        writeResponse("WebSocket is already opened.");
        return;
    }
    webSocket = new WebSocket("ws://192.168.1.18:8080/LoveIs/chat");
    webSocket.binaryType = "arraybuffer";

    webSocket.onopen = function (event) {
        if (event.data === undefined)
            return;

        writeResponse(event.data);
    };

    webSocket.onmessage = function (event) {
        writeResponse(event.data);
    };

    webSocket.onclose = function (event) {
        writeResponse("Connection closed");
    };
}


function send() {
    var text = document.getElementById("messageinput").value;
    webSocket.send(text);
}

function sendFile() {
    var file = document.getElementById('filename').files[0];
    var object = new Object();
    object.type = "file";
    object.fileName= file.name;
    object.fileSize= file.size;
    object.toFriend= "admin";
    json = JSON.stringify(object);
    webSocket.send(json);

    var reader = new FileReader();
    var rawData = new ArrayBuffer();
    //alert(file.name);

    reader.loadend = function() {

    }
    reader.onload = function(e) {
        rawData = e.target.result;
        webSocket.send(rawData);
        webSocket.send('end');
    }

    reader.readAsArrayBuffer(file);
}

function closeSocket() {
    webSocket.close();
}

function writeResponse(text) {
    messages.innerHTML += "<br/>" + text;
}