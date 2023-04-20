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
        if(usernames.size()>2){
            List<String> sortedUsernameList = new ArrayList<>(usernames);
            Collections.sort(sortedUsernameList);
            StringBuilder chatroomNameBuilder = new StringBuilder();
            chatroomNameBuilder.append(sortedUsernameList.get(0));
            chatroomNameBuilder.append(", ");
            chatroomNameBuilder.append(sortedUsernameList.get(1));
            chatroomNameBuilder.append(", ");
            chatroomNameBuilder.append(sortedUsernameList.get(2));
            if (sortedUsernameList.size()>3){
                chatroomNameBuilder.append("...");
            }
            chatroomNameBuilder.append("(");
            chatroomNameBuilder.append(usernames.size());
            chatroomNameBuilder.append(")");
            return  chatroomNameBuilder.toString();
        } else {
            for (String name:usernames){
                if (!name.equals(client_name)){
                    System.out.println("chatroomModel :" + name + " " + usernames + " " + client_name);
                    return name;
                }
            }
        }
        return "name error";
    }
}
