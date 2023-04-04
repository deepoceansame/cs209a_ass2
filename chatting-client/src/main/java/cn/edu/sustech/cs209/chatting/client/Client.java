package cn.edu.sustech.cs209.chatting.client;

import cn.edu.sustech.cs209.chatting.common.HelpPacket;
import cn.edu.sustech.cs209.chatting.common.OperationCode;

import java.io.*;
import java.net.Socket;
import java.util.List;
import java.util.Scanner;

public class Client implements Runnable{

    public List<String> usernames;
    public BufferedReader bufferedReader;
    public BufferedWriter bufferedWriter;
    public Socket serverSocket;
    public ObjectOutputStream objectOutputStream;
    public ObjectInputStream objectInputStream;
    public List<String> existUserNames;

    public Client() throws IOException {
        serverSocket = new Socket("localhost", 7777);
        OutputStream outputStream = serverSocket.getOutputStream();
        objectOutputStream = new ObjectOutputStream(outputStream);
        InputStream inputStream = serverSocket.getInputStream();
        objectInputStream = new ObjectInputStream(inputStream);
    }

    public void startHandleInfoFromServer() {
        new Thread(
                () -> {
                    try{
                        while(serverSocket.isConnected()){
                            Scanner in = new Scanner(System.in);
                            System.out.println("input new userName");
                            String username = in.nextLine();
                            HelpPacket hp = new HelpPacket();
                            hp.operationCode = OperationCode.WANT_TO_PARTI;
                            hp.newUserName = username;
                            System.out.println("echo your input username: "+username);
                            objectOutputStream.writeObject(hp);
                            System.out.println("have output and reset");
                            HelpPacket re_hp = (HelpPacket) objectInputStream.readObject();
                            System.out.println("isSuccess");
                            System.out.println(re_hp.isSuccess);
                            System.out.println("existUserNames");
                            System.out.println(re_hp.existUsernames);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        closeEverything(serverSocket, bufferedReader, bufferedWriter,
                                objectInputStream, objectOutputStream);
                    }
                }
        ).start();
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
