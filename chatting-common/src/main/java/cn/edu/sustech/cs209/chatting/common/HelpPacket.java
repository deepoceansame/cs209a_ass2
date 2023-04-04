package cn.edu.sustech.cs209.chatting.common;

import java.io.Serializable;
import java.util.List;


public class HelpPacket implements Serializable {



    public OperationCode operationCode;
    public List<String> existUsernames;
    public List<String> newChatRoomUsernames;
    public Message newMessage;
    public Long newChatRoomId;
    public String exitedUserName;
    public List<Message> chatRoomMessages;
    public String newUserName;
    public boolean isSuccess;
}
