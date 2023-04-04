package cn.edu.sustech.cs209.chatting.server;

import cn.edu.sustech.cs209.chatting.common.HelpPacket;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class Main {

    public static List<String> usernames;

    public static void main(String[] args) throws IOException {
        usernames = new ArrayList<>();
        ServerSocket ss = new ServerSocket(7777);
        System.out.println("ServerSocket awaiting connections...");
        while (!ss.isClosed()) {
            Socket socket = ss.accept(); // blocking call, this will wait until a connection is attempted on this port.
            System.out.println("Connection from " + socket + "!");

            new Thread(new HandleClient(socket)).start();
        }
    }
}

class HandleClient implements Runnable{

    Socket clientSocket;
    ObjectOutputStream objectOutputStream;

    ObjectInputStream objectInputStream;

    public HandleClient(Socket clientSocket) throws IOException {
        this.clientSocket = clientSocket;
        OutputStream outputStream = clientSocket.getOutputStream();
        objectOutputStream = new ObjectOutputStream(outputStream);
        InputStream inputStream = clientSocket.getInputStream();
        objectInputStream = new ObjectInputStream(inputStream);
    }

    @Override
    public void run() {
        try{
            while(clientSocket.isConnected()){
                HelpPacket hp = (HelpPacket) objectInputStream.readUnshared();
                System.out.println("OperationCode "+hp.operationCode);
                System.out.println("adding username "+hp.newUserName);
                Main.usernames.add(hp.newUserName);
                HelpPacket re_hp = new HelpPacket();
                re_hp.isSuccess = true;
                re_hp.existUsernames = new ArrayList<>(Main.usernames);
                objectOutputStream.writeObject(re_hp);
            }
        } catch (Exception e) {
            e.printStackTrace();
            closeEverything(clientSocket, null, null,
                    objectInputStream, objectOutputStream);
        }
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
