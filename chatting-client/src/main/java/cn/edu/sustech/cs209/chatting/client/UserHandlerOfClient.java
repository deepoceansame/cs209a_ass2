package cn.edu.sustech.cs209.chatting.client;

import cn.edu.sustech.cs209.chatting.common.HelpPacket;
import cn.edu.sustech.cs209.chatting.common.OperationCode;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.Scanner;
import java.util.concurrent.TimeUnit;

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
                    String input_userName = in.nextLine();
                    sendWantiToParti(input_userName);
                    while (!client.hasParticipated){
                        if (client.re_wanti_parti_duplicate){
                            System.out.println("user handler: duplicate Name Please input another");
                            client.re_wanti_parti_duplicate = false;
                            input_userName = in.nextLine();
                            sendWantiToParti(input_userName);
                        }
                    }
                    System.out.println("participate successful");
                }
                System.out.println(client.existUserNames);
                TimeUnit.SECONDS.sleep(5);
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
}
