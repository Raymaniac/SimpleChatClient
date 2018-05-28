package chatApp;

import com.google.gson.Gson;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import util.ChatTab;
import util.ClientList;
import util.Message;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.*;

public class Controller {

    private String userName;
    private boolean isLoggedin = false;
    private boolean isConnected = false;
    private String requestUrl = "http://localhost:9090/";

    private List<ChatTab> tabList = new ArrayList<ChatTab>();

    private Gson gson = new Gson();

    @FXML TextField messageInput;
    @FXML Button sendButton;
    @FXML TabPane chatTabs;

    //Menu interaction
    @FXML MenuItem newChatItem;


    @FXML
    public void sendMessage() {

        String text = messageInput.getText();

        int index = chatTabs.getSelectionModel().getSelectedIndex();
        System.out.println("Selected index: " + index);

        //check if input is System command
        if(text.charAt(0) == '?') {

            if(text.contains("?login:")) {

                if(isLoggedin) {
                    showMessage(index, "You are already logged in!");
                }else {
                    String[] item = text.split(":");
                    login(item[1]);
                    isLoggedin = true;
                }

            }else if(text.contains("?logout")) {

                if(isLoggedin) {
                    logout();
                    isLoggedin = false;
                }

            }else if(text.contains("?onlineClients")) {

                getOnlineClients(index);

            }else if(text.contains("?connect:")) {

                if(isLoggedin) {
                    String[] item = text.split(":");
                    connect(item[1], index);
                    isConnected = true;
                }else {
                    showMessage(index, "You have to log in, first, to connect to someone");
                }

            }else if(text.contains("?disconnect")) {

                if(isConnected) {
                    disconnect();
                    isConnected = false;
                }

            }

        }else{

            if(isLoggedin && isConnected) {
                showMessage(index, messageInput.getText());
                //Do Request
                Message msg = new Message();
                msg.sender = userName;
                msg.recipient = tabList.get(index).getRecipientName();
                msg.messageText = messageInput.getText();

                Gson gson = new Gson();

                String payload = gson.toJson(msg);

                try {
                    postRequest("sendMessage", payload);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }else {
                showMessage(index, "You have to be logged in and connected to someone to send messages!");
            }
        }

        messageInput.clear();
    }

    @FXML
    public void closeApplication() {
        disconnect();
        logout();
        System.exit(0);
    }

    @FXML
    public void openChat() {
        tabList.add(new ChatTab(requestUrl));
        updateTabPane();
    }

    private void closeChat() {
        int index = chatTabs.getSelectionModel().getSelectedIndex();
        tabList.remove(index);
        updateTabPane();
    }

    /* ============ Helper methods ============ */

    private void updateTabPane() {
        chatTabs.getTabs().clear();
        for(ChatTab tab : tabList) {
            chatTabs.getTabs().add(tab.getPane());
        }
    }

    private void login(String userName) {
        try{
            String payload = "cName:" + userName;

            postRequest("login", payload);

            openChat();
        } catch (Exception e) {
            e.printStackTrace();
        }finally {
            this.userName = userName;
        }
    }

    private void logout() {
        try{
            String payload = "cName:" + userName;

            postRequest("logout", payload);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void connect(String recipient, int tabIndex) {
        tabList.get(tabIndex).setRecipientName(recipient);
        tabList.get(tabIndex).setChatPartner(recipient);
    }

    private void disconnect() {

        closeChat();
    }

    private void getOnlineClients(int tabIndex) {
        try{

            String response = getRequest("getOnlineClients", null);

            ClientList clients = gson.fromJson(response, ClientList.class);

            String displayMessage = "Online:\n";

            for(String client : clients.clients) {
                displayMessage += client + "\n";
                System.out.println("Client: " + client + " is online");
            }
            showMessage(tabIndex, displayMessage);

        }catch(Exception e) {
            e.printStackTrace();
        }
    }

    private void showMessage(int tabIndex, String text) {

        tabList.get(tabIndex).appendMessage(userName, text);

    }

    private String getRequest(String requestPath, String urlParams) throws Exception {

        String urlStr = "";

        if(urlParams != null) {
            urlStr = requestUrl + requestPath + "/?" + urlParams;
        }else {
            urlStr = requestUrl + requestPath;
        }

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
        StringBuffer response = new StringBuffer();

        while ((inputLine = in.readLine()) != null) {
            response.append(inputLine);
        }

        in.close();

        return response.toString();
    }

    private String postRequest(String requestPath, String payload) throws Exception {

        String urlStr = requestUrl + requestPath;

        URL url = new URL(urlStr);

        byte[] postDataBytes = payload.getBytes("UTF-8");

        HttpURLConnection conn = (HttpURLConnection)url.openConnection();
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "application/json");
        conn.setRequestProperty("Content-Length", String.valueOf(postDataBytes.length));
        conn.setDoOutput(true);
        conn.getOutputStream().write(postDataBytes);

        Reader in = new BufferedReader(new InputStreamReader(conn.getInputStream(), "UTF-8"));
        StringBuilder sb = new StringBuilder();

        for (int c; (c = in.read()) >= 0;)
            sb.append((char)c);

        String response = sb.toString();
        System.out.println(response);

        return response;
    }
}
