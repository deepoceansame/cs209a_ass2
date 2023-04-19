package cn.edu.sustech.cs209.chatting.server;

import cn.edu.sustech.cs209.chatting.common.Chatroom;
import cn.edu.sustech.cs209.chatting.common.HelpPacket;
import cn.edu.sustech.cs209.chatting.common.Message;
import cn.edu.sustech.cs209.chatting.common.OperationCode;
import javafx.collections.FXCollections;

import java.io.*;
import java.net.Socket;
import java.net.SocketException;
import java.util.*;

class ClientHandler implements Runnable {

    Server server;
    Socket clientSocket;
    ObjectOutputStream objectOutputStream;
    ObjectInputStream objectInputStream;
    public boolean hasParticipated;
    String username;

    public ClientHandler(Socket clientSocket, Server server) throws IOException {
        this.clientSocket = clientSocket;
        this.server = server;
        OutputStream outputStream = clientSocket.getOutputStream();
        objectOutputStream = new ObjectOutputStream(outputStream);
        InputStream inputStream = clientSocket.getInputStream();
        objectInputStream = new ObjectInputStream(inputStream);
        hasParticipated = false;
    }

    @Override
    public void run() {
        try {
            while (clientSocket.isConnected()) {
                // get object
                HelpPacket hp = (HelpPacket) objectInputStream.readObject();
                System.out.println("OperationCode " + hp.operationCode);
                handleHelpPacket(hp);
            }
        }
        catch (EOFException eof) {
            closeEverything(clientSocket, null, null,
                    objectInputStream, objectOutputStream);
        }
        catch (SocketException se) {
            Thread.currentThread().interrupt();
        }
        catch (Exception e) {
            e.printStackTrace();
            closeEverything(clientSocket, null, null,
                    objectInputStream, objectOutputStream);
        }
    }

    private void handleHelpPacket(HelpPacket hp) throws IOException {
        OperationCode opCode = hp.operationCode;
        if (opCode==OperationCode.WANT_TO_PARTI){
            handleWantToParti(hp);
        }
        else if (opCode==OperationCode.GET_EXIST_USERNAMES){
            handleGetExistUsernames(hp);
        }
        else if (opCode==OperationCode.NEW_CHATROOM){
            handleNewChatRoom(hp);
        }
        else if (opCode==OperationCode.NEW_MESSAGE) {
            handleNewMessage(hp);
        }
        else if (opCode==OperationCode.WANT_TO_EXIT) {
            handleWantToExit(hp);
        }
    }


    private void handleWantToParti(HelpPacket hp) throws IOException {
        String userNameHP = hp.newUserName;
        System.out.println(userNameHP + " want to participate");
        if (!server.userNameToClientHandler.containsKey(userNameHP)){
            server.usernames.add(hp.newUserName);
            server.userNameToClientHandler.put(userNameHP, this);
            hasParticipated = true;
            this.username = userNameHP;

            HelpPacket re_hp = new HelpPacket();
            re_hp.operationCode = OperationCode.RE_WANTI_TO_PARTI;
            re_hp.isSuccess = true;
            re_hp.existUsernames = new HashSet<>(server.usernames);
            objectOutputStream.writeObject(re_hp);
            objectOutputStream.flush();

            String un;
            ClientHandler ch;
            for (Map.Entry<String, ClientHandler> entry:server.userNameToClientHandler.entrySet()){
                un = entry.getKey();
                ch = entry.getValue();
                if (!un.equals(userNameHP)){
                    ch.sendReNewUsername(userNameHP);
                }
            }

        } else {
            HelpPacket re_hp = new HelpPacket();
            re_hp.operationCode = OperationCode.RE_WANTI_TO_PARTI;
            re_hp.isSuccess = false;
            re_hp.attachMessage = "duplicate name, can not participate in";
            objectOutputStream.writeObject(re_hp);
            objectOutputStream.flush();
        }

    }

    private void handleGetExistUsernames(HelpPacket hp) throws IOException {

    }
    private void handleNewChatRoom(HelpPacket hp) throws IOException {
        Long chatRoomId = server.notUsedChatRoomId.getAndAdd(1);
        Set<String> usernamesOfTheChatRoom = hp.newChatRoomUsernames;
        System.out.println(username + " want to add new chatroom with" + usernamesOfTheChatRoom);

        usernamesOfTheChatRoom.add(username);

        Chatroom chatroom = new Chatroom(chatRoomId, usernamesOfTheChatRoom);
        server.chatroomMap.put(chatRoomId, chatroom);

        ClientHandler ch;
        for (String username:usernamesOfTheChatRoom){
            if (server.usernames.contains(username)){
                ch = server.userNameToClientHandler.get(username);
                ch.sendReNewChatroom(chatRoomId, usernamesOfTheChatRoom);
            }
        }
    }

    private void handleNewMessage(HelpPacket hp) throws IOException {
        Message message = hp.newMessage;
        Long chatRoomId = message.chatroomId;
        Chatroom chatroom = server.chatroomMap.get(chatRoomId);
        chatroom.messages.add(message);
        Set<String> usernames = chatroom.usernames;
        ClientHandler ch;
        for (String username : usernames) {
            ch = server.userNameToClientHandler.get(username);
            Message message_send =
                    new Message(message.getTimestamp(), message.getSentBy(), message.getSendTo(), message.getData());
            message_send.chatroomId = message.chatroomId;
            ch.sendReNewMessage(message_send);
        }

    }

    private void handleWantToExit(HelpPacket hp) throws IOException {
        String exitUsername = hp.exitedUserName;
        System.out.println("serer client handler: " + exitUsername + " want to exit");
        server.usernames.remove(exitUsername);
        server.userNameToClientHandler.remove(exitUsername);
        Map<Long, Chatroom> newChatroomMap = new HashMap<>();
        for (Map.Entry<Long, Chatroom> entry:server.chatroomMap.entrySet()) {
            Long chatrooId = entry.getKey();
            Chatroom chatroom = entry.getValue();
            if (chatroom.usernames.contains(username)) {
                chatroom.usernames.remove(exitUsername);
                if (chatroom.usernames.size()>=2){
                    newChatroomMap.put(chatrooId, chatroom);
                }
            }
            else {
                newChatroomMap.put(chatrooId, chatroom);
            }
        }
        server.chatroomMap = newChatroomMap;
        ClientHandler ch;
        for (String name:server.usernames) {
            ch = server.userNameToClientHandler.get(name);
            ch.sendReUserExit(exitUsername);
            ch.closeEverything(clientSocket, null, null,
                    objectInputStream, objectOutputStream);
        }
    }

    public void sendReNewUsername(String username) throws IOException {
        HelpPacket hp = new HelpPacket();
        hp.newUserName = username;
        hp.operationCode = OperationCode.RE_NEW_USERNAME;
        objectOutputStream.writeObject(hp);
        objectOutputStream.flush();
    }

    public void sendReNewChatroom(Long chatRoomId, Set<String> usernamesOfNewChatroom) throws IOException {
        HelpPacket hp = new HelpPacket();
        hp.newChatRoomUsernames = usernamesOfNewChatroom;
        hp.newChatRoomId = chatRoomId;
        hp.operationCode = OperationCode.RE_NEW_CHATROOM;
        objectOutputStream.writeObject(hp);
        objectOutputStream.flush();
    }

    public void sendReNewMessage(Message message) throws IOException {
        HelpPacket hp = new HelpPacket();
        hp.newMessage = message;
        hp.operationCode = OperationCode.RE_NEW_MESSAGE;
        objectOutputStream.writeObject(hp);
        objectOutputStream.flush();
    }
    public void sendReUserExit(String exitUsername) throws IOException {
        HelpPacket hp = new HelpPacket();
        hp.exitedUserName = exitUsername;
        hp.operationCode = OperationCode.RE_USER_EXIT;
        objectOutputStream.writeObject(hp);
        objectOutputStream.flush();
    }
    public void closeEverything(
            Socket socket,
            BufferedReader bufferedReader, BufferedWriter bufferedWriter,
            ObjectInputStream objectInputStream, ObjectOutputStream objectOutputStream
    ) {
        try {
            if (socket != null) {
                socket.close();
            }
            if (bufferedReader != null) {
                bufferedReader.close();
            }
            if (bufferedWriter != null) {
                bufferedWriter.close();
            }
            if (objectInputStream != null) {
                objectInputStream.close();
            }
            if (objectOutputStream != null) {
                objectOutputStream.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof ClientHandler){
          return ((ClientHandler)obj).username.equals(username);
        } else {
            return false;
        }
    }
}
