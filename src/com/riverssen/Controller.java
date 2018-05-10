package com.riverssen;

import java.awt.Desktop;
import java.awt.Image;
import java.awt.Insets;
import java.awt.Label;
import java.awt.Toolkit;
import java.awt.datatransfer.StringSelection;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigInteger;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ResourceBundle;

import javax.imageio.ImageIO;

import com.riverssen.core.security.PubKey;
import com.riverssen.core.security.Wallet;

import javafx.embed.swing.SwingFXUtils;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.DialogPane;
import javafx.scene.image.ImageView;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.text.Text;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Modality;

public class Controller implements Initializable
{
    private static Controller instance;
    @FXML
    javafx.scene.control.TextField name;
    @FXML
    javafx.scene.control.TextField seed;
    @FXML
    javafx.scene.control.TextField password;
    @FXML
    Text address;
    @FXML
    Text rvc;
    @FXML
            javafx.scene.control.Button copy;

    BufferedImage black = new BufferedImage(512, 512, BufferedImage.TYPE_INT_ARGB);
    BufferedImage image = new BufferedImage(512, 512, BufferedImage.TYPE_INT_ARGB);
    Wallet wallet = new Wallet();

    @Override
    public void initialize(URL location, ResourceBundle resources)
    {
        address.setText("");
        instance = this;
    }

    public void clickImportWallet()
    {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Open Resource File");
        File selectedFile = fileChooser.showOpenDialog(null);
        if (selectedFile == null)
        {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Alert");
            alert.setHeaderText("Wallet Import Error");
            alert.setContentText("Please choose an rwt file.");

            alert.showAndWait();
            return;
        }
        
        if(selectedFile.getName().endsWith(".txt"))
        {
        	try {
            	BufferedReader reader = new BufferedReader(new FileReader(selectedFile));
            	
            	String publicKey = reader.readLine();
            	
            	reader.close();
            	
                address.setText(publicKey);

                try
                {
                    final String api = "https://api.qrserver.com/v1/create-qr-code/?size=512x512&data=" + publicKey;

                    try
                    {
                        image = ImageIO.read(new URL(api));
                    } catch (IOException e)
                    {
                        e.printStackTrace();
                        image = black;
                    }
                } catch (Exception e)
                {
                    e.printStackTrace();
                }
        	} catch(Exception e)
        	{
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Alert");
                alert.setHeaderText("Wallet Import Error");
                alert.setContentText("Please choose a valid txt file.");

                alert.showAndWait();
                address.setText("");
                image = black;
        	}
        	
        	update();
        	return;
        }

        try
        {
            DataInputStream stream = new DataInputStream(new FileInputStream(selectedFile));

            int length = stream.readShort();

            stream.readShort();

            byte publicKey[] = new byte[length];

            stream.read(publicKey);

            stream.close();

            PubKey key = new PubKey(new String(publicKey));

            if (!key.isValid()) throw new Exception("invalid key");

            address.setText(key.getPublicWalletAddress());

            wallet.setPublicKey(key);

            try
            {
                final String api = "https://api.qrserver.com/v1/create-qr-code/?size=512x512&data=" + key.getPublicWalletAddress();

                try
                {
                    image = ImageIO.read(new URL(api));
                } catch (IOException e)
                {
                    e.printStackTrace();
                    image = black;
                }
            } catch (Exception e)
            {
                e.printStackTrace();
            }
        } catch (Exception e)
        {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Alert");
            alert.setHeaderText("Wallet Import Error");
            alert.setContentText("Please choose a valid rwt file.");

            alert.showAndWait();
            address.setText("");
            image = black;
            return;
        }

        update();
    }

    public void clickShowQRCode()
    {
    	Alert alert = new Alert(Alert.AlertType.INFORMATION);
    	
    	alert.initModality(Modality.APPLICATION_MODAL);
        alert.getDialogPane().setContentText("Make sure to scan your QR code to match it with your public address.");

        DialogPane dialogPane = alert.getDialogPane();
        GridPane grid = new GridPane();
        ColumnConstraints graphicColumn = new ColumnConstraints();
        graphicColumn.setFillWidth(false);
        graphicColumn.setHgrow(Priority.NEVER);
        ColumnConstraints textColumn = new ColumnConstraints();
        textColumn.setFillWidth(true);
        textColumn.setHgrow(Priority.ALWAYS);
        grid.getColumnConstraints().setAll(graphicColumn, textColumn);
        grid.setPadding(new javafx.geometry.Insets(5));

        ImageView imageView = new ImageView();
        imageView.setImage(SwingFXUtils.toFXImage(this.image, null));
        imageView.setFitWidth(512);
        imageView.setFitHeight(512);
        StackPane stackPane = new StackPane(imageView);
        stackPane.setAlignment(Pos.CENTER);
        grid.add(stackPane, 0, 0);

        dialogPane.setHeader(grid);
        dialogPane.setGraphic(null);

        alert.showAndWait();
    }

    public void clickGenerate()
    {
        if (name.getText().length() == 0)
        {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Alert");
            alert.setHeaderText("Wallet Generation Error");
            alert.setContentText("Please enter a name for your wallet.");

            alert.showAndWait();
            return;
        }

        if (seed.getText().length() == 0)
        {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Alert");
            alert.setHeaderText("Wallet Generation Error");
            alert.setContentText("Please enter a special seed to create your wallet. In case of loss, you can always regenerate your wallet using this seed!");

            alert.showAndWait();
            return;
        }

        if (password.getText().length() == 0)
        {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION, "", ButtonType.YES, ButtonType.NO);
            alert.setTitle("Alert");
            alert.setHeaderText("Wallet Generation Confirmation");
            alert.setContentText("Are you sure you don't want to encrypt your private wallet with a password?");

            alert.showAndWait().filter(r -> r == ButtonType.YES).ifPresent(r -> generateWallet());
            return;
        }

        generateWallet();
    }
    
    public void generateWallet()
    {
    	DirectoryChooser fileChooser = new DirectoryChooser();
        fileChooser.setTitle("Open Resource File");
        File selectedFile = fileChooser.showDialog(null);//.showOpenDialog(null);
        if (selectedFile == null)
        {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Alert");
            alert.setHeaderText("Wallet Generation Error");
            alert.setContentText("Please choose a directory for your wallet.");

            alert.showAndWait();
            return;
        }

        Wallet wallet = new Wallet(name.getText(), seed.getText());

        while(password.getText().length() < 16)
        {
//        	BigInteger b = new BigInteger(password.getText().getBytes());
        	password.setText(password.getText() + "0");
        }
        
        int i = wallet.export(password.getText().substring(0, 16), selectedFile);

        if (i == 0)
        {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Alert");
            alert.setHeaderText("Wallet Generation Successful!");
            alert.setContentText("Your public key is: " + wallet.getPublicKey().getPublicWalletAddress() + "\nWelcome to the Rivercoin club!\n\n\nPlease memorise the seed and password for your wallet and hide them somewhere secure:\nseed: " + seed.getText() + "\npass: " + password.getText());

            alert.showAndWait();
        } else
        {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Alert");
            alert.setHeaderText("Error!");
            alert.setContentText("An error occured! if your wallet was exported please delete it and try again!");

            alert.showAndWait();
            return;
        }


        name.setText("");
        seed.setText("");
        password.setText("");
    }

    public void clickLicenseLink(ActionEvent actionEvent)
    {
        try
        {
            Desktop.getDesktop().browse(new URI("http://www.riverssen.com/coin/wallet/license"));
        } catch (IOException e)
        {
            e.printStackTrace();
        } catch (URISyntaxException e)
        {
            e.printStackTrace();
        }
    }

    public void clickCopyAddress()
    {
        Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new StringSelection(address.getText()), null);
    }

    private synchronized void update()
    {
        String text = rvc.getText();

        try
        {
            URL url = new URL("http://riverssen.com/coin/wallet/balance?address=" + address.getText());

            HttpURLConnection httpcon = (HttpURLConnection) url.openConnection();
            httpcon.addRequestProperty("User-Agent", "Mozilla/4.0");

            BufferedReader reader = new BufferedReader(new InputStreamReader(httpcon.getInputStream()));

            text = "Rivercoin: " + reader.readLine();
            reader.close();
        } catch (Exception e)
        {
            e.printStackTrace();
        }

        rvc.setText(text);
    }

    public static void updateBalance()
    {
        if (instance != null) instance.update();
    }
    
    public void clickAPILicense()
    {
    	String url = "http://www.bouncycastle.org/licence.html";
    	
        try
        {
            Desktop.getDesktop().browse(new URI(url));
        } catch (Exception e)
        {
        	Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Alert");
            alert.setHeaderText("Connection Error!");
            alert.setContentText("An error occured!\nAPI license:\nLICENSE\n" +
            "Copyright (c) 2000 - 2018 The Legion of the Bouncy Castle Inc. (https://www.bouncycastle.org)\n" +
            "\n" +
            "Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the \"Software\"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:\n" +
            "\n" +
            "The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.\n" +
            "\n" +
            "THE SOFTWARE IS PROVIDED \"AS IS\", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.");
            e.printStackTrace();
        }
    }
    
    public void clickAPIWebsite()
    {
    	String url = "http://www.bouncycastle.org";
    	
    	try
        {
            Desktop.getDesktop().browse(new URI(url));
        } catch (Exception e)
        {
        	e.printStackTrace();
        }
    }
}