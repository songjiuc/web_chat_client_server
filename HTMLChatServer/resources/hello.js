let userName = document.getElementById("username");
let roomName = document.getElementById("roomname");
let message = document.getElementById("message");
let chatBox = document.getElementById("chatDiv");
let userBox = document.getElementById("userDiv");
let emptyChat = document.getElementById("emptyChatText");
let emptyUser = document.getElementById("emptyUserText");

roomName.addEventListener("keypress", handleKeyPressRoomCB);
message.addEventListener("keypress", handleKeyPressMessageCB);

let msg_sent;

// get current date and time
function getTimeStamp(){
    let date = new Date();
    let day = date.getFullYear() + "-" + (date.getMonth()+1) + "-" + (date.getDate());
    let time = date.getHours() + ":" + date.getMinutes() + ":" + date.getSeconds();
    let d = "[" + day + "-" + time +"] ";
    return d;
}

// Callback to handle when user enters the room name
function handleKeyPressRoomCB(event){
    if(event.keyCode === 13) {
        console.log(userName.value);
        console.log(roomName.value);
        event.preventDefault();

        let invalid = false;
        if(roomName.value === ""){
            invalid = true;
        }

        for (let i = 0; i<roomName.value.length;i++){
            if(roomName.value[i]<'a' || roomName.value[i]>'z'){
                invalid = true;
            }
        }

        if (invalid){
            alert("A valid room name contains only lowercase letters.");
            roomName.value = "<Enter A Valid Room Name>";
            roomName.select();
        }
        else{
            if(wsOpen) {
                msg_sent = "join" + " " + userName.value + " " + roomName.value + " ";
                ws.send(msg_sent);

                if(userBox.querySelector("#emptyUserText") !== null){
                    userBox.removeChild(emptyUser);
                }

            }
            else{
                message.value = "WS is not open.";
            }
        }
    }
}

// Callback to handle when user enters the message
function handleKeyPressMessageCB(event){
    if(event.keyCode ===13){
        console.log(message.value);
        event.preventDefault();
        if(wsOpen) {

            msg_sent = "message " + userName.value + " " + message.value + " " + roomName.value + " " + getTimeStamp();
            ws.send(msg_sent);
            message.value="";
        }
        else{
            message.value = "WS is not open.";
        }
    }
}

// Create a websocket
let ws = new WebSocket("ws://localhost:8080");
ws.onopen = handleOpenCB;
ws.onmessage = handleMessageCB;
ws.onclose = handleCloseCB;
ws.onerror = handleErrorCB;

let wsOpen = false;
function handleOpenCB(){
    wsOpen = true;
}

// Callback to handle when ws receive the message from server
function handleMessageCB(event){
    let msg = event.data;
    console.log(msg);
    let msgObj = JSON.parse(msg);
    console.log(msgObj);
    let msg_type = msgObj.type;
    let msg_user = msgObj.user;
    let msg_room = msgObj.room;
    let msg_message = msgObj.message;
    let msg_time = msgObj.time;

    let quote = document.createElement('blockquote');
    let newUser = document.createElement('blockquote');

    if(msg_type === "join"){
        quote.textContent = msg_user + " has joined the room " + msg_room + ".";
        newUser.textContent = msg_user;
        newUser.id = msg_user;

    }
    else if(msg_type==="leave") {
        quote.textContent = msg_user + " left the room " + msg_room + ".";
        document.getElementById(msg_user).remove();
    }
    else{
        quote.textContent = msg_time + " " + msg_user + ": " + msg_message;
    }

    if(chatBox.querySelector("#emptyChatText") !== null){
        chatBox.removeChild(emptyChat);
    }
    userBox.appendChild(newUser);
    userBox.scrollTop = userBox.scrollHeight;

    chatBox.appendChild(quote);
    chatBox.scrollTop = chatBox.scrollHeight;
}

function handleCloseCB(){
    // msg_sent = "leave" + " " + userName.value + " " + roomName.value;
    // ws.send(msg_sent);
    document.writeln("The server has left the building. Goodbye!");
}

function handleErrorCB(){
    msg_sent = "An error occurred."
    document.writeln(msg_sent);
}




