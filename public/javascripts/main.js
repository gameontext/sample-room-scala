var wsUri = "ws://" + window.document.location.host + "/room";
var output;

function init() {
    output = document.getElementById("output");
    input = document.getElementById("input");
    wsdest = document.getElementById("wsdest");
    wsdest.value=wsUri;
    testWebSocket();
}

function connect() {
    testWebSocket();
}

function roomHello() {
    var roomHello = {
        "username": "Sally",
        "userId": "sally876",
        "version": 2
    };
    writeToInput("roomHello,_," + JSON.stringify(roomHello));
}

function roomGoodbye() {
    var roomGoodbye = {
	"username": "Harry",
	"userId": "harry912"
    };

    writeToInput("roomGoodbye,_," + JSON.stringify(roomGoodbye));
}

function chat() {
    var chat = {
        "username": "Scarlet",
        "userId": "scarlet973",
        "content": "Fancy running into you in a room like this..."
    };
    writeToInput("room,_," + JSON.stringify(chat));
}

function play() {
    var play = {
        "username": "Connor",
        "userId": "highlander54",
        "content": "/play with the other one."
    };
    writeToInput("room,_," + JSON.stringify(play));
}

function argh() {
    var asdfs = {
        "username": "Mick",
        "userId": "mick42",
        "content": "/asdsdfsadfa"
    };
    writeToInput("room,_," + JSON.stringify(asdfs));
}

function gowest() {
    var west = {
        "username": "Scarlet",
        "userId": "scarlet973",
        "content": "/go west"
    };
    writeToInput("room,_," + JSON.stringify(west));
}


function testWebSocket() {
    websocket = new WebSocket(wsdest.value);
    websocket.onopen    = function(evt) { onOpen(evt) };
    websocket.onclose   = function(evt) { onClose(evt) };
    websocket.onmessage = function(evt) { onMessage(evt) };
    websocket.onerror   = function(evt) { onError(evt) };
}

function send() {
    doSend(input.value)
}

function clearScreen() {
    output.innerHTML=""
}

function writeToInput(message) {
    input.value=message;
}

function writeToScreen(message) {
    var pre = document.createElement("p");
    pre.style.wordWrap = "break-word";
    pre.innerHTML = message;
    output.appendChild(pre);
}

function onOpen(evt) {
    writeToScreen(wsdest.value + "...CONNECTED");
}

function onClose(evt) {
    writeToScreen("DISCONNECTED");
}

function onMessage(evt) {
    writeToScreen('<span style="color: blue;">RESPONSE: ' + evt.data+'</span>');
}

function onError(evt) {
    writeToScreen('<span style="color: red;">ERROR:</span> ' + evt.data);
}

function doSend(message) {
    writeToScreen("SENT: " + message);
    websocket.send(message);
}

window.addEventListener("load", init, false);



