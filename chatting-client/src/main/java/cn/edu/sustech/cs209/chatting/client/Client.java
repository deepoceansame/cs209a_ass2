package cn.edu.sustech.cs209.chatting.client;

import cn.edu.sustech.cs209.chatting.common.HelpPacket;
import cn.edu.sustech.cs209.chatting.common.OperationCode;

import java.io.*;
import java.net.Socket;
import java.util.List;
import java.util.Scanner;
import java.util.Set;

public class Client implements Runnable{

    public List<String> usernames;
    public BufferedReader bufferedReader;
    public BufferedWriter bufferedWriter;
    public Socket serverSocket;
    public ObjectOutputStream objectOutputStream;
    public ObjectInputStream objectInputStream;
    public Set<String> existUserNames;
    public boolean hasParticipated;

    public Thread clientListenThread;

    public Client() throws IOException {
        serverSocket = new Socket("localhost", 7777);
        OutputStream outputStream = serverSocket.getOutputStream();
        objectOutputStream = new ObjectOutputStream(outputStream);
        InputStream inputStream = serverSocket.getInputStream();
        objectInputStream = new ObjectInputStream(inputStream);
        hasParticipated = false;
    }

    public void startHandleInfoFromServer() {
        clientListenThread = new Thread(
                () -> {
                    try{
                        while(serverSocket.isConnected()){
                            HelpPacket re_hp = (HelpPacket) objectInputStream.readObject();
                            handleRehp(re_hp);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        closeEverything(serverSocket, bufferedReader, bufferedWriter,
                                objectInputStream, objectOutputStream);
                    }
                }
        );

        clientListenThread.start();
    }

    private void handleRehp(HelpPacket re_hp){

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
