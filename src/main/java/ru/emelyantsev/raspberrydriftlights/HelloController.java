package ru.emelyantsev.raspberrydriftlights;

import javafx.fxml.FXML;
import javafx.scene.control.Label;

public class HelloController {
    @FXML
    private Label welcomeText;

    @FXML
    protected void onHelloButtonClick() {
        //Pi4j pi4j = new Pi4j();
//        pi4j.run();
        //Pi4J3 pi4J3 = new Pi4J3();
        VL53L0X vl53L0X = new VL53L0X();
    }
}