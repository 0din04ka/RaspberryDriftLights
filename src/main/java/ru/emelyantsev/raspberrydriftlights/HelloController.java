package ru.emelyantsev.raspberrydriftlights;

import javafx.fxml.FXML;
import javafx.scene.control.Label;

public class HelloController {
    @FXML
    private Label welcomeText;

    @FXML
    protected void onHelloButtonClick() {
        Pi4j pi4j = new Pi4j();
        pi4j.run();
    }
}