package ru.emelyantsev.raspberrydriftlights;

import com.pi4j.Pi4J;
import com.pi4j.context.Context;
import com.pi4j.io.i2c.I2C;
import com.pi4j.io.i2c.I2CConfig;
import com.pi4j.io.i2c.I2CProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Pi4j {
    private static final Logger logger = LoggerFactory.getLogger(Pi4j.class);

    // Адреса устройств
    private static final int TCA9548A_ADDRESS = 0x70; // Адрес мультиплексора TCA9548A
    private static final int VL53L0X_DEFAULT_ADDRESS = 0x29; // Адрес датчика VL53L0X

    // Контекст Pi4J и устройства
    private Context pi4j;
    private I2C tca9548a;
    private I2C vl53l0x1;
    private I2C vl53l0x2;

    public Pi4j() {
        // Инициализация Pi4J
        pi4j = Pi4J.newAutoContext();
        logger.info("Pi4J initialized.");

        // Настройка I2C для мультиплексора TCA9548A
        I2CProvider i2CProvider = pi4j.provider("linuxfs-i2c");
        I2CConfig tca9548aConfig = I2C.newConfigBuilder(pi4j)
                .id("TCA9548A") // Уникальный идентификатор для мультиплексора
                .bus(1) // Номер шины I2C (обычно 1 на Raspberry Pi)
                .device(TCA9548A_ADDRESS)
                .build();
        tca9548a = i2CProvider.create(tca9548aConfig);
        logger.info("TCA9548A initialized.");
    }

    // Выбор канала на мультиплексоре TCA9548A
    private void selectChannel(int channel) {
        if (channel < 0 || channel > 7) {
            throw new IllegalArgumentException("Channel must be between 0 and 7");
        }
        tca9548a.writeRegister(0x00, (byte) (1 << channel));
        logger.info("Selected channel: {}", channel);
    }

    // Инициализация датчика VL53L0X
    private void initVL53L0X(I2C vl53l0x) {
        // Запуск измерения (пример для VL53L0X)
        vl53l0x.writeRegister(0x00, (byte) 0x01); // Замените на реальный регистр
        logger.info("VL53L0X initialized.");
    }

    // Чтение расстояния с датчика VL53L0X
    private int readDistance(I2C vl53l0x) {
        // Ожидание готовности данных (пример для VL53L0X)
        while ((vl53l0x.readRegister(0x13) & 0x07) == 0) { // Замените на реальный регистр
            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                logger.error("Interrupted while waiting for data.", e);
            }
        }

        // Чтение расстояния (пример для VL53L0X)
        int distance = vl53l0x.readRegister(0x14 + 10) << 8; // Замените на реальные регистры
        distance |= vl53l0x.readRegister(0x14 + 11);
        logger.info("Distance read: {} mm", distance);
        return distance;
    }

    public void run() {
        try {
            I2CProvider i2CProvider = pi4j.provider("linuxfs-i2c");

            // Настройка и создание первого датчика (канал 0)
            selectChannel(0);
            I2CConfig vl53l0xConfig1 = I2C.newConfigBuilder(pi4j)
                    .id("VL53L0X-1") // Уникальный идентификатор для первого датчика
                    .bus(1)
                    .device(VL53L0X_DEFAULT_ADDRESS)
                    .build();
            vl53l0x1 = i2CProvider.create(vl53l0xConfig1);
            initVL53L0X(vl53l0x1);

            // Настройка и создание второго датчика (канал 1)
            selectChannel(1);
            I2CConfig vl53l0xConfig2 = I2C.newConfigBuilder(pi4j)
                    .id("VL53L0X-2") // Уникальный идентификатор для второго датчика
                    .bus(1)
                    .device(VL53L0X_DEFAULT_ADDRESS)
                    .build();
            vl53l0x2 = i2CProvider.create(vl53l0xConfig2);
            initVL53L0X(vl53l0x2);

            // Чтение данных с датчиков
            while (true) {
                selectChannel(0);
                int distance1 = readDistance(vl53l0x1);
                logger.info("Distance from sensor 1: {} mm", distance1);

                selectChannel(1);
                int distance2 = readDistance(vl53l0x2);
                logger.info("Distance from sensor 2: {} mm", distance2);

                Thread.sleep(1000); // Пауза между измерениями
            }
        } catch (Exception e) {
            logger.error("An error occurred.", e);
        } finally {
            // Закрытие устройств и освобождение ресурсов
            if (vl53l0x1 != null) vl53l0x1.close();
            if (vl53l0x2 != null) vl53l0x2.close();
            if (tca9548a != null) tca9548a.close();
            if (pi4j != null) pi4j.shutdown();
            logger.info("Resources released.");
        }
    }
}