package cn.edu.sustech.cs209.chatting.client;

import cn.edu.sustech.cs209.chatting.common.Chatroom;
import cn.edu.sustech.cs209.chatting.common.HelpPacket;
import cn.edu.sustech.cs209.chatting.common.Message;
import cn.edu.sustech.cs209.chatting.common.OperationCode;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.HashSet;

public class ServerInfoHandlerOfClient implements Runnable{

    public Client client;
    public Socket serverSocket;
    public ObjectInputStream objectInputStream;
    public ObjectOutputStream objectOutputStream;


    public ServerInfoHandlerOfClient(Client client){
        this.client = client;
        objectInputStream = client.objectInputStream;
        objectOutputStream = client.objectOutputStream;
        serverSocket = client.serverSocket;
    }

    @Override
    public void run() {
        try{
            while(serverSocket.isConnected()){
                HelpPacket re_hp = (HelpPacket) objectInputStream.readObject();
                handleRehp(re_hp);
            }
        } catch (Exception e) {
            e.printStackTrace();
            closeEverything(serverSocket, null, null,
                    objectInputStream, objectOutputStream);
        }
    }

    private void handleRehp(HelpPacket re_hp){
        OperationCode opCode = re_hp.operationCode;
        if (opCode==OperationCode.RE_WANTI_TO_PARTI){
            handleReWantiToParti(re_hp);
        }
        else if (opCode==OperationCode.RE_NEW_USERNAME){
            handleReNewUsername(re_hp);
        }
        else if (opCode==OperationCode.RE_NEW_CHATROOM){
            handleReNewChatroom(re_hp);
        }
        else if (opCode==OperationCode.RE_NEW_MESSAGE){
            handleReNewMessage(re_hp);
        }
    }

    public void handleReWantiToParti(HelpPacket re_hp){
        if (re_hp.isSuccess){
            client.existUserNames = new HashSet<>(re_hp.existUsernames);
            client.hasParticipated = true;
            System.out.println("server handler of client: parti success");
        } else {
            System.out.println("server handler of client: duplicate Name");
            client.re_wanti_parti_duplicate = true;
        }
    }

    public void handleReNewUsername(HelpPacket re_hp) {
        client.existUserNames.add(re_hp.newUserName);
        System.out.println("server handler of client: newUser " + re_hp.newUserName);
    }

    public void handleReNewChatroom(HelpPacket re_hp) {
        Chatroom chatroom = new Chatroom(re_hp.newChatRoomId, re_hp.newChatRoomUsernames);
        client.chatroomMap.put(re_hp.newChatRoomId, chatroom);
        System.out.println("server handler of client: add new chatRoom with " + re_hp.newChatRoomUsernames);
    }

    public void handleReNewMessage(HelpPacket re_hp) {
        Message message = re_hp.newMessage;
        Long chatroomId = message.chatroomId;
        Chatroom chatroom = client.chatroomMap.get(chatroomId);
        chatroom.messages.add(message);
        System.out.println("server handler of client: received a message from " + message.getSentBy());
    }

    public void closeEverything(
            Socket socket,
            BufferedReader bufferedReader, BufferedWriter bufferedWriter,
            ObjectInputStream objectInputStream, ObjectOutputStream objectOutputStream
    ) {
        try{
            if(socket!=null){
                socket.close();
            }
            if (bufferedReader!=null){
                bufferedReader.close();
            }
            if (bufferedWriter!=null){
                bufferedWriter.close();
            }
            if (objectInputStream!=null){
                objectInputStream.close();
            }
            if (objectOutputStream!=null){
                objectOutputStream.close();
            }
        } catch (Exception e){
            e.printStackTrace();
        }
    }
}
