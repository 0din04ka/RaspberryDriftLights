package ru.emelyantsev.raspberrydriftlights;

import com.pi4j.Pi4J;
import com.pi4j.context.Context;
import com.pi4j.io.i2c.I2C;
import com.pi4j.io.i2c.I2CConfig;
import com.pi4j.io.i2c.I2CProvider;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Pi4j {
    private static final int TCA9548A_ADDRESS = 0x70; // Адрес мультиплексора TCA9548A
    private static final int VL53L0X_DEFAULT_ADDRESS = 0x29; // Адрес датчика VL53L0X

    // Регистры датчика VL53L0X
    private static final int VL53L0X_REG_SYSRANGE_START = 0x00;
    private static final int VL53L0X_REG_RESULT_RANGE_STATUS = 0x14;
    private static final int VL53L0X_REG_RESULT_INTERRUPT_STATUS = 0x13;

    private Context pi4j;
    private I2C tca9548a;

    public Pi4j() {
        // Инициализация Pi4J
        pi4j = Pi4J.newAutoContext();

        // Настройка I2C для мультиплексора TCA9548A
        I2CProvider i2CProvider = pi4j.provider("linuxfs-i2c");
        I2CConfig tca9548aConfig = I2C.newConfigBuilder(pi4j)
                .id("TCA9548A")
                .bus(1) // I2C шина (обычно 1 на Raspberry Pi)
                .device(TCA9548A_ADDRESS)
                .build();
        tca9548a = i2CProvider.create(tca9548aConfig);
    }

    // Выбор канала на мультиплексоре TCA9548A
    private void selectChannel(int channel) {
        if (channel < 0 || channel > 7) {
            throw new IllegalArgumentException("Channel must be between 0 and 7");
        }
        tca9548a.writeRegister(0x00, (byte) (1 << channel));
    }

    // Инициализация датчика VL53L0X
    private void initVL53L0X(I2C vl53l0x) {
        // Запуск измерения
        vl53l0x.writeRegister(VL53L0X_REG_SYSRANGE_START, (byte) 0x01);
    }

    // Чтение расстояния с датчика VL53L0X
    private int readDistance(I2C vl53l0x) {
        // Ожидание готовности данных
        while ((vl53l0x.readRegister(VL53L0X_REG_RESULT_INTERRUPT_STATUS) & 0x07) == 0) {
            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        // Чтение расстояния
        int distance = vl53l0x.readRegister(VL53L0X_REG_RESULT_RANGE_STATUS + 10) << 8;
        distance |= vl53l0x.readRegister(VL53L0X_REG_RESULT_RANGE_STATUS + 11);
        return distance;
    }

    public void run() {
        try {
            // Настройка I2C для датчиков VL53L0X
            I2CProvider i2CProvider = pi4j.provider("linuxfs-i2c");
            I2CConfig vl53l0xConfig = I2C.newConfigBuilder(pi4j)
                    .id("VL53L0X")
                    .bus(1)
                    .device(VL53L0X_DEFAULT_ADDRESS)
                    .build();

            // Работа с первым датчиком (канал 0)
            selectChannel(0);
            I2C vl53l0x1 = i2CProvider.create(vl53l0xConfig);
            initVL53L0X(vl53l0x1);

            // Работа со вторым датчиком (канал 1)
            selectChannel(1);
            I2C vl53l0x2 = i2CProvider.create(vl53l0xConfig);
            initVL53L0X(vl53l0x2);

            // Чтение данных с датчиков
            while (true) {
                selectChannel(0);
                int distance1 = readDistance(vl53l0x1);
                System.out.println("Distance from sensor 1: " + distance1 + " mm");

                selectChannel(1);
                int distance2 = readDistance(vl53l0x2);
                System.out.println("Distance from sensor 2: " + distance2 + " mm");

                Thread.sleep(1000); // Пауза между измерениями
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}