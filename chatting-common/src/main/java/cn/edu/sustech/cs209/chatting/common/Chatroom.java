package cn.edu.sustech.cs209.chatting.common;


import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

public class Chatroom {
    public Long chatRoomId;
    public Set<String> usernames;
    public ObservableList<Message> messages;
    public String client_name;

    public Chatroom(Long chatRoomId, Set<String> usernames){
        this.chatRoomId = chatRoomId;
        this.usernames = usernames;
        messages = FXCollections.observableArrayList(new ArrayList<>());
    }

    @Override
    public String toString() {
        return "[" + chatRoomId + " " + usernames + "]";
    }
}
