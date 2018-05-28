package util;

import com.google.gson.Gson;
import javafx.concurrent.Task;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Tab;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class ChatTab {

    private Tab pane;
    private ScrollPane tabRoot;
    private VBox display;
    private String requestUrl;

    private String userName;
    private String chatPartner;

    private List<MessageContainer> msgList = new ArrayList<MessageContainer>();

    public ChatTab(String requestUrl) {
        this.requestUrl = requestUrl;
        pane = new Tab();
        pane.setText("Unknown");
        tabRoot = new ScrollPane();
        display = new VBox(10);
        VBox.setVgrow(display, Priority.ALWAYS);
        tabRoot.setContent(display);
        tabRoot.setFitToWidth(true);
        pane.setContent(tabRoot);

        Task<Integer> worker = new Task<Integer>() {

            long lastUpdate = 0;
            int updateFrequency = 2000; //ms

            @Override
            protected Integer call() throws Exception {

                while(!isCancelled()) {
                    if( (System.currentTimeMillis() - lastUpdate) > updateFrequency ) {
                        try{
                            String urlStr = requestUrl + "getMessages/?cName=" + userName + "&chatPartner=" + chatPartner;


                            URL url = new URL(urlStr);
                            HttpURLConnection con = (HttpURLConnection) url.openConnection();
                            // optional default is GET
                            con.setRequestMethod("GET");
                            //add request header
                            con.setRequestProperty("User-Agent", "Mozilla/5.0");
                            int responseCode = con.getResponseCode();
                            System.out.println("Sending 'GET' request to URL : " + url);
                            System.out.println("Response Code : " + responseCode);
                            BufferedReader in = new BufferedReader(
                                    new InputStreamReader(con.getInputStream()));
                            String inputLine;
                            StringBuffer responseBuffer = new StringBuffer();

                            while ((inputLine = in.readLine()) != null) {
                                responseBuffer.append(inputLine);
                            }

                            String response = responseBuffer.toString();

                            in.close();

                            Gson gson = new Gson();

                            Response resp = gson.fromJson(response, Response.class);

                            for(Message msg : resp.messages) {
                                appendMessage(msg.sender, msg.messageText);

                            }

                        }catch(Exception e) {
                            e.printStackTrace();
                        }
                    }
                }

                return null;
            }
        };

        Thread t = new Thread(worker);
        t.setDaemon(false);
        t.start();
    }

    public Tab getPane() {
        return pane;
    }

    public String getRecipientName() {
        return pane.getText();
    }

    public void setRecipientName(String recipientName) {
        pane.setText(recipientName);
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public void setChatPartner(String chatPartner) {
        this.chatPartner = chatPartner;
    }

    public void appendMessage(Message msg) {
        msgList.add(new MessageContainer(msg));
        updateDisplay();
    }

    //Mostly used for displaying help messages
    public void appendMessage(String sender, String message) {
        System.out.println("Appending Message from " + sender + " with " + message);
        msgList.add(new MessageContainer(sender, message));
        updateDisplay();
    }

    private void updateDisplay() {
        display.getChildren().clear();
        for(MessageContainer container : msgList){
            display.getChildren().add(container.getContainer());
        }

        System.out.println("Display count: " + display.getChildren().size());
    }
}
