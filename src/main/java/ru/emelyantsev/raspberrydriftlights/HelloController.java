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
        VL53L0X sensor = new VL53L0X();
        sensor.setSignalRateLimit(0.1); // Ограничение на минимальный сигнал
        sensor.setMeasurementTimingBudgetMicroseconds(200000); // Увеличиваем тайминг для лучшей точности
        sensor.setVcselPulsePeriod(VL53L0X.VcselPeriodType.PreRange, 18);
        sensor.setVcselPulsePeriod(VL53L0X.VcselPeriodType.FinalRange, 14); // Set timing budget for accuracy

        try {
            while (true) {
                sensor.startRanging();
                Thread.sleep(1000);
                int distance = sensor.getDistance();
                System.out.println("Distance: " + distance + " mm");
                Thread.sleep(1000);
            }
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } finally {
            sensor.close();
        }
    }
}