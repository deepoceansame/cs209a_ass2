package cn.edu.sustech.cs209.chatting.common;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

public class Chatroom {
    public Long chatRoomId;
    public Set<String> usernames;
    public List<Message> messages;

    public Chatroom(Long chatRoomId, Set<String> usernames){
        this.chatRoomId = chatRoomId;
        this.usernames = usernames;
        messages = Collections.synchronizedList(new ArrayList<>());
    }

    @Override
    public String toString() {
        return "[" + chatRoomId + " " + usernames + "]";
    }
}
