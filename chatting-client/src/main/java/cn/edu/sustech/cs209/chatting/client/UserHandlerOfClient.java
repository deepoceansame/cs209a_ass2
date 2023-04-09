package cn.edu.sustech.cs209.chatting.client;

import cn.edu.sustech.cs209.chatting.common.HelpPacket;
import cn.edu.sustech.cs209.chatting.common.Message;
import cn.edu.sustech.cs209.chatting.common.OperationCode;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Scanner;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class UserHandlerOfClient implements Runnable{

    Client client;
    ObjectOutputStream objectOutputStream;
    ObjectInputStream objectInputStream;
    Socket serverSocket;

    public UserHandlerOfClient(Client client){
        this.client = client;
        objectOutputStream = client.objectOutputStream;
        objectInputStream = client.objectInputStream;
        serverSocket = client.serverSocket;
    }
    @Override
    public void run() {
        try{
            while (serverSocket.isConnected()) {
                Scanner in = new Scanner(System.in);
                if (!client.hasParticipated){
                    System.out.println("please input a username");
                    String input_username = in.nextLine();
                    sendWantiToParti(input_username);
                    while (!client.hasParticipated){
                        if (client.re_wanti_parti_duplicate){
                            System.out.println("user handler: duplicate name Please input another");
                            client.re_wanti_parti_duplicate = false;
                            input_username = in.nextLine();
                            sendWantiToParti(input_username);
                        }
                    }
                    client.username = input_username;
                    System.out.println("participate successful");
                }
                System.out.println("0=showCurrentUsers 1=addNewChatroom 2=showCurrentChatroom " +
                        "3=enterChatroom 4=sendMessage 5=showMessageOfCurrentChatroom");
                int userOpCode = in.nextInt();
                if (userOpCode==0){
                    System.out.println(client.existUserNames);
                }
                else if (userOpCode==1){
                    System.out.println("please enter number of users of this chatroom");
                    int num = in.nextInt();
                    System.out.println("please enter a line of userNames");
                    String[] usernamesArray = new String[num];
                    for (int i=0;i<num;i++){
                        usernamesArray[i] = in.next();
                    }
                    Set<String> usernamesOfNewChatroom = new HashSet<>(Arrays.asList(usernamesArray));
                    sendNewChatroom(usernamesOfNewChatroom);
                }
                else if (userOpCode==2){
                    System.out.println(client.chatroomMap);
                }
                else if(userOpCode==3){
                    System.out.println("please input chatroom id you want to enter");
                    client.currentChatroomId = in.nextLong();
                }
                else if(userOpCode==4){
                    System.out.println("please input your message");
                    String blankNL = in.nextLine();
                    String content = in.nextLine();
                    System.out.println("echo your message " + content);
                    Message message = new Message(System.currentTimeMillis(), client.username, null, content);
                    message.chatroomId = client.currentChatroomId;
                    sendNewMessage(message);
                }
                else if (userOpCode==5){
                    System.out.println(client.chatroomMap.get(client.currentChatroomId).messages);
                }
            }
        } catch (Exception e){
            e.printStackTrace();
        }

    }

    public void sendWantiToParti(String userName) throws IOException {
        HelpPacket hp = new HelpPacket();
        hp.operationCode = OperationCode.WANT_TO_PARTI;
        hp.newUserName = userName;
        objectOutputStream.writeObject(hp);
    }

    public void sendNewChatroom(Set<String> usernamesOfNewChatroom) throws IOException {
        HelpPacket hp = new HelpPacket();
        hp.operationCode = OperationCode.NEW_CHATROOM;
        hp.newChatRoomUsernames = usernamesOfNewChatroom;
        objectOutputStream.writeObject(hp);
    }

    public void sendNewMessage(Message message) throws IOException {
        HelpPacket hp = new HelpPacket();
        hp.operationCode = OperationCode.NEW_MESSAGE;
        hp.newMessage = message;
        objectOutputStream.writeObject(hp);
    }
}
