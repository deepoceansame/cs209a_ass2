package cn.edu.sustech.cs209.chatting.client;

import cn.edu.sustech.cs209.chatting.common.Chatroom;
import cn.edu.sustech.cs209.chatting.common.Message;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.MapChangeListener;
import javafx.collections.SetChangeListener;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import javafx.util.Callback;

import java.io.IOException;
import java.net.URL;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

public class Controller implements Initializable {

    @FXML
    public ListView<Chatroom> chatList;
    @FXML
    public ListView<Message> chatContentList;
    @FXML
    public ListView<String> usrList;
    @FXML
    public Label currentUsername;

    public String username;

    public Client client;

    @FXML
    public Label currentOnlineCnt;

    @FXML
    public TextArea inputArea;
    public Label usernamesOfChatroom;
    public Label notifyMessage;


    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {

        try {

            Dialog<String> dialog = new TextInputDialog();
            dialog.setTitle("Login");
            dialog.setHeaderText(null);
            dialog.setContentText("Username:");
            client = new Client();
            client.startHandleInfoFromServer();
            client.startHandleUser();
            client.controller = this;
            Optional<String> input = dialog.showAndWait();
            boolean needSend = true;

            while (!client.hasParticipated){
                if (input.isPresent() && !input.get().isEmpty()) {
                /*
                   TODO: Check if there is a user with the same name among the currently logged-in users,
                         if so, ask the user to change the username
                 */
                    username = input.get();
                    if (needSend){
                        client.sendWantiToParti(username);
                        needSend = false;
                    }
                    if (client.re_wanti_parti_duplicate){
                        System.out.println("gui: duplicate name");

                        Alert alert = new Alert(Alert.AlertType.INFORMATION);
                        alert.setTitle("Information Dialog");
                        alert.setHeaderText("input error");
                        alert.setContentText("duplicate name!");
                        alert.showAndWait();

                        client.re_wanti_parti_duplicate = false;
                        needSend = true;
                        input = dialog.showAndWait();
                    }
                } else{
                    Alert alert = new Alert(Alert.AlertType.INFORMATION);
                    alert.setTitle("Information Dialog");
                    alert.setHeaderText("input error");
                    alert.setContentText("invalid input");
                    alert.showAndWait();
                    needSend = true;
                    input = dialog.showAndWait();
                }
            }
            client.username = new SimpleStringProperty();
            client.username.set(username);
            currentUsername.textProperty().bind(client.username);
//            currentOnlineCnt.textProperty().bind(Bindings.size(client.existUserNames).asString());
            chatContentList.setCellFactory(new MessageCellFactory());
            currentOnlineCnt.textProperty().set(String.valueOf(client.existUserNames.size()));
            usrList.setItems(FXCollections.observableList(new ArrayList<>(client.existUserNames)));
            client.existUserNames.addListener(
                    (SetChangeListener<? super String>) change -> {
                        Platform.runLater(
                            () -> {
                                currentOnlineCnt.textProperty().set(String.valueOf(client.existUserNames.size()));
                                usrList.setItems(FXCollections.observableList(new ArrayList<>(client.existUserNames)));
                            }
                        );
                        System.out.println("gui stupid bind");
                    }
            );
            client.chatroomMap.addListener(
                    (MapChangeListener<? super Long, ? super Chatroom>) change -> {
                        Platform.runLater(
                            () -> {
                                chatList.setItems(FXCollections.observableList(new ArrayList<>(client.chatroomMap.values())));
                            }
                        );
                        Chatroom chatroom = change.getValueAdded();

//                        Platform.runLater(
//                                ()->{
//                                    chatroom.messages.addListener(
//                                            (ListChangeListener<? super Message>) change1 -> {
//                                                Platform.runLater(
//                                                        () -> {
//                                                            if (Objects.equals(client.currentChatroomId, chatroom.chatRoomId)) {
//                                                                chatContentList.getItems().clear();
//                                                                for (Message message:chatroom.messages){
//                                                                    chatContentList.getItems().add(message);
//                                                                }
//                                                            }
//                                                        }
//                                                );
//                                            }
//                                    );
//                                }
//                        );

                    }
            );
            chatList.getSelectionModel().selectedItemProperty().addListener(
                    (arg0, arg1, arg2) -> {

                        Chatroom chatroom = chatList.getSelectionModel().getSelectedItem();
                        if (chatroom != null){
                            client.currentChatroomId = chatroom.chatRoomId;
                            Platform.runLater(
                                    () -> {
//                                                    chatContentList.getItems().clear();
//                                                    for (Message message:chatroom.messages){
//                                                        chatContentList.getItems().add(message);
//                                                    }
                                        chatContentList.setItems(chatroom.messages);
                                        usernamesOfChatroom.textProperty().set(chatroom.usernames.toString());
                                    }
                            );
                            System.out.println("gui selected chatroom: "+ chatroom.messages+chatContentList.getItems());
                        }
                    }
            );
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void createPrivateChat() {
        try {
            AtomicReference<String> user = new AtomicReference<>();

            Stage stage = new Stage();
            ComboBox<String> userSel = new ComboBox<>();

            // FIXME: get the user list from server, the current user's name should be filtered out

            Set<String> usernames_show = new HashSet<>(client.existUserNames);
            usernames_show.remove(client.username.get());
            userSel.getItems().addAll(usernames_show);
            System.out.println("gui add private chat"+ usernames_show);

            Button okBtn = new Button("OK");
            okBtn.setOnAction(e -> {
                try {
                    user.set(userSel.getSelectionModel().getSelectedItem());
                    if (user.get() != null){
                        boolean isPrivateChatroomExist = false;
                        Long chatroomIdToGo = 0l;
                        for (Map.Entry<Long, Chatroom> entry: client.chatroomMap.entrySet()){
                            Long chatRoomId = entry.getKey();
                            Chatroom chatroom = entry.getValue();
                            if (chatroom.usernames.size()==2 && chatroom.usernames.contains(user.get())){
                                isPrivateChatroomExist = true;
                                chatroomIdToGo = chatRoomId;
                            }
                        }
                        if (isPrivateChatroomExist){
                            Chatroom chatroom = client.chatroomMap.get(chatroomIdToGo);
                            client.currentChatroomId = chatroom.chatRoomId;
                            Platform.runLater(
                                    () -> {
//                                                    chatContentList.getItems().clear();
//                                                    for (Message message:chatroom.messages){
//                                                        chatContentList.getItems().add(message);
//                                                    }
                                        chatContentList.setItems(chatroom.messages);
                                        usernamesOfChatroom.textProperty().set(chatroom.usernames.toString());
                                    }
                            );
                            System.out.println("gui private chat has existed: "
                                    +chatroom.messages+chatContentList.getItems());
                        } else {
                            Set<String> selectedUsers = new HashSet<>();
                            selectedUsers.add(user.get());
                            client.sendNewChatroom(selectedUsers);
                        }
                    }
                    stage.close();
                } catch (Exception e1) {
                    e1.printStackTrace();
                }
            });

            HBox box = new HBox(10);
            box.setAlignment(Pos.CENTER);
            box.setPadding(new Insets(20, 20, 20, 20));
            box.getChildren().addAll(userSel, okBtn);
            stage.setScene(new Scene(box));
            stage.showAndWait();
        } catch (Exception e) {
            e.printStackTrace();
        }

        // TODO: if the current user already chatted with the selected user, just open the chat with that user
        // TODO: otherwise, create a new chat item in the left panel, the title should be the selected user's name
    }

    /**
     * A new dialog should contain a multi-select list, showing all user's name.
     * You can select several users that will be joined in the group chat, including yourself.
     * <p>
     * The naming rule for group chats is similar to WeChat:
     * If there are > 3 users: display the first three usernames, sorted in lexicographic order, then use ellipsis with the number of users, for example:
     * UserA, UserB, UserC... (10)
     * If there are <= 3 users: do not display the ellipsis, for example:
     * UserA, UserB (2)
     */
    @FXML
    public void createGroupChat() {
        try{
            Stage stage = new Stage();
            ListView<String> users = new ListView<>();
            users.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
            Set<String> usernames_show = new HashSet<>(client.existUserNames);
            usernames_show.remove(client.username.get());
            users.getItems().addAll(usernames_show);
            System.out.println("gui add multiple chat"+ usernames_show);
            Button okBtn = new Button("OK");
            okBtn.setOnAction(e -> {
                try{
                    Set<String> selectedUsers = new HashSet<>(users.getSelectionModel().getSelectedItems());
                    System.out.println("gui add multiple chat selected usrs"+ selectedUsers);
                    if (selectedUsers.size()>0){
                        client.sendNewChatroom(selectedUsers);
                    }
                    stage.close();
                } catch (Exception e1) {
                    e1.printStackTrace();
                }
            });

            HBox box = new HBox(10);
            box.setAlignment(Pos.CENTER);
            box.setPadding(new Insets(20, 20, 20, 20));
            box.getChildren().addAll(users, okBtn);
            stage.setScene(new Scene(box));
            stage.showAndWait();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Sends the message to the <b>currently selected</b> chat.
     * <p>
     * Blank messages are not allowed.
     * After sending the message, you should clear the text input field.
     */
    @FXML
    public void doSendMessage() throws IOException {
        // TODO
        String data = inputArea.getText();
        if(data.trim().length() == 0){
            inputArea.setText("");
            return;
        }
        Message message = new Message(System.currentTimeMillis(), client.username.getValue(), null, data);
        message.chatroomId = client.currentChatroomId;
        if (client.chatroomMap.values().stream().map(chatroom -> chatroom.chatRoomId).collect(Collectors.toList()).contains(message.chatroomId)){
            client.sendNewMessage(message);
        }
        inputArea.setText("");
    }

    /**
     * You may change the cell factory if you changed the design of {@code Message} model.
     * Hint: you may also define a cell factory for the chats displayed in the left panel, or simply override the toString method.
     */
    private class MessageCellFactory implements Callback<ListView<Message>, ListCell<Message>> {
        @Override
        public ListCell<Message> call(ListView<Message> param) {
            return new ListCell<Message>() {

                @Override
                public void updateItem(Message msg, boolean empty) {
                    super.updateItem(msg, empty);
                    if (empty || Objects.isNull(msg)) {
                        setGraphic(null);
                        setText(null);
                        return;
                    }

                    HBox wrapper = new HBox();
                    Label nameLabel = new Label(msg.getSentBy());
                    Label msgLabel = new Label(msg.getData());

                    nameLabel.setPrefSize(50, 20);
                    nameLabel.setWrapText(true);
                    nameLabel.setStyle("-fx-border-color: black; -fx-border-width: 1px;");

                    if (username.equals(msg.getSentBy())) {
                        wrapper.setAlignment(Pos.TOP_RIGHT);
                        wrapper.getChildren().addAll(msgLabel, nameLabel);
                        msgLabel.setPadding(new Insets(0, 20, 0, 0));
                    } else {
                        wrapper.setAlignment(Pos.TOP_LEFT);
                        wrapper.getChildren().addAll(nameLabel, msgLabel);
                        msgLabel.setPadding(new Insets(0, 0, 0, 20));
                    }

                    setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
                    setGraphic(wrapper);
                }
            };
        }
    }
}
