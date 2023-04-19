package cn.edu.sustech.cs209.chatting.client;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.stage.Stage;
import cn.edu.sustech.cs209.chatting.client.Controller;
import java.io.IOException;

public class Main extends Application {

    public static void main(String[] args) throws IOException {
        launch();
//        Client client = new Client();
//        client.startHandleInfoFromServer();
//        client.startHandleUser();
    }

    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("main.fxml"));
        stage.setScene(new Scene(fxmlLoader.load()));
        stage.setTitle("Chatting Client");
        stage.show();
        stage.setOnCloseRequest(windowEvent -> {
            try {

                Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
                alert.setTitle("exit");
                alert.setHeaderText("exit?");
                alert.setContentText("exit?");
                if (alert.showAndWait().get() == ButtonType.OK) {
                    Controller controller = (Controller) fxmlLoader.getController();
                    controller.client.sendWantiExit();
                    stage.close();
                    controller.client.closeEverything(controller.client.serverSocket, null, null,
                            controller.client.objectInputStream, controller.client.objectOutputStream);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }
}
