package cn.edu.sustech.cs209.chatting.common;

import java.io.Serializable;
import java.util.List;
import java.util.Set;


public class HelpPacket implements Serializable {



    public OperationCode operationCode;
    public Set<String> existUsernames;
    public List<String> newChatRoomUsernames;
    public Message newMessage;
    public Long newChatRoomId;
    public String exitedUserName;
    public List<Message> chatRoomMessages;
    public String newUserName;
    public boolean isSuccess;
    public String attachMessage;
}
