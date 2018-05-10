package com.riverssen;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

import java.security.Security;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Main extends Application
{
    public static Scene root;
    public static boolean running;

    public static void main(String args[])
    {
        Security.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider());

        launch();
    }

    @Override
    public void start(Stage primaryStage) throws Exception
    {
        Parent root = FXMLLoader.load(getClass().getResource("wallet.fxml"));
        primaryStage.setTitle("Riverwallet v0.0.12a");
        primaryStage.setScene(Main.root = new Scene(root, 600, 395));
        primaryStage.setResizable(false);
        primaryStage.getIcons().add(new Image(getClass().getResourceAsStream("icon.png")));

        running = true;

        ExecutorService service = Executors.newFixedThreadPool(1);
        service.execute(()->{
            while (running)
            {
                Controller.updateBalance();
                try
                {
                    Thread.sleep(60000 * 3);
                } catch (InterruptedException e)
                {
                }
            }
        });

        primaryStage.setOnCloseRequest((e)->{
            running = false;
            service.shutdownNow();
        });
        primaryStage.show();
    }
}