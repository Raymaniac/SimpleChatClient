package util;

import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.HBox;
import javafx.scene.text.Font;
import javafx.scene.text.TextAlignment;

public class MessageContainer {

    private HBox container;
    private Label displayText;

    private Message msg;

    private String style = "-fx-border-color: black;" +
                            "-fx-border-width: 0 0 1 0;" +
                            "-fx-padding: 5;";

    public MessageContainer(String sender, String messageText) {
        container = new HBox();
        displayText = new Label(sender + " - " + messageText);

        container.setStyle(style);
        displayText.setFont(Font.font("Verdana", 17));
        displayText.setWrapText(true);
        displayText.setTextAlignment(TextAlignment.RIGHT);

        container.getChildren().add(displayText);
    }

    public MessageContainer(Message msg) {
        this.msg = msg;

        container = new HBox(10);
        displayText = new Label(msg.sender + " - " + msg.messageText);

        container.setStyle(style);
        displayText.setFont(Font.font("Verdana", 17));
        displayText.setWrapText(true);
        displayText.setTextAlignment(TextAlignment.LEFT);

        container.getChildren().add(displayText);
    }

    public HBox getContainer() {
        return container;
    }
}
