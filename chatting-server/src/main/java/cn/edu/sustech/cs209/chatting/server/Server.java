package cn.edu.sustech.cs209.chatting.server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.atomic.AtomicLong;

public class Server {
    public Set<String> userNames;
    public AtomicLong notUsedChatRoomId;
    public Map<String, ClientHandler> userNameToClientHandler;
    public Server(){
        userNames = Collections.synchronizedSet(new HashSet<>());
        notUsedChatRoomId = new AtomicLong(0);
        userNameToClientHandler = Collections.synchronizedMap(new HashMap<>());
    }

    public void start() throws IOException {
        ServerSocket ss = new ServerSocket(7777);
        System.out.println("ServerSocket awaiting connections...");
        while (!ss.isClosed()) {
            Socket clientSocket = ss.accept(); // blocking call, this will wait until a connection is attempted on this port.
            System.out.println("Connection from " + clientSocket + "!");
            ClientHandler handlerForThisClientSocket = new ClientHandler(clientSocket, this);
            new Thread(handlerForThisClientSocket).start();
        }
    }
}
