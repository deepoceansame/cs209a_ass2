package cn.edu.sustech.cs209.chatting.server;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class Main {

    public static void main(String[] args) throws IOException {
        Server server = new Server();
        server.start();
    }
}

