var wsUri = "ws://localhost:9000/room";
var output;

function init() {
    output = document.getElementById("output");
    testWebSocket();
}

function roomHello() {
    var roomHello = {
        "username": "Sally",
        "userId": "sally876",
        "version": 2
    };
    doSend("roomHello,_," + JSON.stringify(roomHello));
}

function roomGoodbye() {
  var roomGoodbye = {
    "username": "Harry",
    "userId": "harry912"
  };

  doSend("roomGoodbye,_," + JSON.stringify(roomGoodbye));
}

function chat() {
    var chat = {
        "username": "Scarlet",
        "userId": "scarlet973",
        "content": "Fancy running into you in a room like this..."
    };
    doSend("room,_," + JSON.stringify(chat));
}

function play() {
    var play = {
        "username": "Connor",
        "userId": "highlander54",
        "content": "/play with the other one."
    };
    doSend("room,_," + JSON.stringify(play));
}

function argh() {
    var asdfs = {
        "username": "Mick",
        "userId": "mick42",
        "content": "/asdsdfsadfa"
    };
    doSend("room,_," + JSON.stringify(asdfs));
}

function gowest() {
    var west = {
        "username": "Scarlet",
        "userId": "scarlet973",
        "content": "/go west"
    };
    doSend("room,_," + JSON.stringify(west));
}


function testWebSocket() {
    websocket = new WebSocket(wsUri);
    websocket.onopen    = function(evt) { onOpen(evt) };
    websocket.onclose   = function(evt) { onClose(evt) };
    websocket.onmessage = function(evt) { onMessage(evt) };
    websocket.onerror   = function(evt) { onError(evt) };
}

function clearScreen() {
    output.innerHTML=""
}

function writeToScreen(message) {
    var pre = document.createElement("p");
    pre.style.wordWrap = "break-word";
    pre.innerHTML = message;
    output.appendChild(pre);
}

function onOpen(evt) {
    writeToScreen("CONNECTED");
    roomHello()
}

function onClose(evt) {
    writeToScreen("DISCONNECTED");
}

function onMessage(evt) {
    writeToScreen('<span style="color: blue;">RESPONSE: ' + evt.data+'</span>');
    //websocket.close();
}

function onError(evt) {
    writeToScreen('<span style="color: red;">ERROR:</span> ' + evt.data);
}

function doSend(message) {
    writeToScreen("SENT: " + message);
    websocket.send(message);
}

window.addEventListener("load", init, false);



