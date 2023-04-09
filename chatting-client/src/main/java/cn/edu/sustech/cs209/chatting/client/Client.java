package cn.edu.sustech.cs209.chatting.client;

import cn.edu.sustech.cs209.chatting.common.Chatroom;

import java.io.*;
import java.net.Socket;
import java.util.*;

public class Client implements Runnable{

    public BufferedReader bufferedReader;
    public BufferedWriter bufferedWriter;
    public Socket serverSocket;
    public ObjectOutputStream objectOutputStream;
    public ObjectInputStream objectInputStream;
    public Set<String> existUserNames;
    public boolean hasParticipated;
    public Thread serverHandlerThread;
    public Runnable serverHandler;
    public Thread userHandlerThread;
    public Runnable userHandler;
    public String username;
    public Map<Long, Chatroom> chatroomMap;
    public Long currentChatroomId;
    public volatile boolean re_wanti_parti_duplicate;

    public Client() throws IOException {
        serverSocket = new Socket("localhost", 7777);
        OutputStream outputStream = serverSocket.getOutputStream();
        objectOutputStream = new ObjectOutputStream(outputStream);
        InputStream inputStream = serverSocket.getInputStream();
        objectInputStream = new ObjectInputStream(inputStream);
        chatroomMap = new HashMap<>();
        hasParticipated = false;
    }

    public void startHandleInfoFromServer() {
        ServerInfoHandlerOfClient serverInfoHandler = new ServerInfoHandlerOfClient(this);
        serverHandler = serverInfoHandler;
        serverHandlerThread = new Thread(serverInfoHandler);
        serverHandlerThread.start();
    }

    public void startHandleUser() {
        UserHandlerOfClient userHandlerOfClient = new UserHandlerOfClient(this);
        userHandler = userHandlerOfClient;
        userHandlerThread = new Thread(userHandler);
        userHandlerThread.start();
    }

    @Override
    public void run() {

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
