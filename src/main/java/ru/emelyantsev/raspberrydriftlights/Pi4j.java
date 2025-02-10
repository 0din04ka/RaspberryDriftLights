package ru.emelyantsev.raspberrydriftlights;

import com.pi4j.Pi4J;
import com.pi4j.context.Context;
import com.pi4j.io.i2c.I2C;
import com.pi4j.io.i2c.I2CProvider;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Pi4j {
    private static final int TCA9548A_ADDR = 0x70;  // Адрес мультиплексора
    private static final int VL53L0X_ADDR = 0x29;   // Адрес датчика расстояния
    private static final int[] SENSOR_CHANNELS = {0, 1}; // Каналы, где подключены 2 датчика

    private static final ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor();
    private static final Map<Integer, Integer> distances = new ConcurrentHashMap<>();
    private static I2C tca9548a;
    private static I2CProvider i2cProvider;
    private static Context pi4j;

    public Pi4j() {
        pi4j = Pi4J.newAutoContext();
        i2cProvider = pi4j.provider("linuxfs-i2c");
        tca9548a = i2cProvider.create(1, TCA9548A_ADDR);

        // Запускаем виртуальные потоки для каждого датчика
        for (int channel : SENSOR_CHANNELS) {
            executor.submit(() -> readSensor(channel));
        }
    }

    private static void readSensor(int channel) {
        while (true) {
            try {
                // Включаем канал TCA9548A
                tca9548a.write((byte) (1 << channel));
                Thread.sleep(5); // Даем мультиплексору переключиться

                // Читаем данные с VL53L0X
                I2C vl53l0x = i2cProvider.create(1, VL53L0X_ADDR);
                int distance = vl53l0x.readRegisterWord(0x1E); // Читаем расстояние

                // Сохраняем данные
                distances.put(channel, distance);
                System.out.println("Датчик " + channel + ": " + distance + " мм");

                Thread.sleep(100); // Интервал опроса
            } catch (Exception e) {
                System.err.println("Ошибка при чтении датчика " + channel + ": " + e.getMessage());
            }
        }
    }
}
