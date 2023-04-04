package cn.edu.sustech.cs209.chatting.server;

import cn.edu.sustech.cs209.chatting.common.HelpPacket;
import cn.edu.sustech.cs209.chatting.common.OperationCode;

import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashSet;

class ClientHandler implements Runnable {

    Server server;
    Socket clientSocket;
    ObjectOutputStream objectOutputStream;
    ObjectInputStream objectInputStream;

    public ClientHandler(Socket clientSocket, Server server) throws IOException {
        this.clientSocket = clientSocket;
        this.server = server;
        OutputStream outputStream = clientSocket.getOutputStream();
        objectOutputStream = new ObjectOutputStream(outputStream);
        InputStream inputStream = clientSocket.getInputStream();
        objectInputStream = new ObjectInputStream(inputStream);
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
        } catch (Exception e) {
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
    }

    private void handleWantToParti(HelpPacket hp) throws IOException {
        String userName = hp.newUserName;
        System.out.println(userName + "want to participate");
        server.userNames.add(hp.newUserName);
        HelpPacket re_hp = new HelpPacket();
        re_hp.isSuccess = true;
        re_hp.existUsernames = new HashSet<>(server.userNames);
        objectOutputStream.writeObject(re_hp);
    }

    private void handleGetExistUsernames(HelpPacket hp) throws IOException {

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
}
