package ru.emelyantsev.raspberrydriftlights;

import com.pi4j.Pi4J;
import com.pi4j.context.Context;
import com.pi4j.io.i2c.I2C;
import com.pi4j.io.i2c.I2CConfig;
import com.pi4j.io.i2c.I2CProvider;

public class Pi4J2 {
    // Адрес датчика VL53L0X по умолчанию
    private static final int VL53L0X_I2C_ADDRESS = 0x29;
    private static final int TCA9548A_ADDRESS = 0x70; // Адрес мультиплексора TCA9548A

    // Команды для работы с датчиком
    private static final int VL53L0X_REG_IDENTIFICATION_MODEL_ID = 0xC0;
    private static final int VL53L0X_REG_SYSRANGE_START = 0x00;
    private static final int VL53L0X_REG_RESULT_RANGE_STATUS = 0x14;
    private static final int VL53L0X_REG_RESULT_INTERRUPT_STATUS = 0x13;
    private static final int VL53L0X_REG_RESULT_RANGE_VAL = 0x14 + 10;
    private I2C tca9548a;


    Pi4J2() {
        Context pi4j = Pi4J.newAutoContext();

        I2CProvider i2CProvider = pi4j.provider("linuxfs-i2c");
        I2CConfig tca9548aConfig = I2C.newConfigBuilder(pi4j)
                .id("TCA9548A") // Уникальный идентификатор для мультиплексора
                .bus(1) // Номер шины I2C (обычно 1 на Raspberry Pi)
                .device(TCA9548A_ADDRESS)
                .build();
        tca9548a = i2CProvider.create(tca9548aConfig);

        tca9548a.writeRegister(0x00, (byte) (1 << 2));
        // Настройка I2C
        I2CConfig i2cConfig = I2C.newConfigBuilder(pi4j)
                .id("VL53L0X")
                .bus(1) // I2C bus 1 (GPIO2 и GPIO3)
                .device(VL53L0X_I2C_ADDRESS)
                .build();

        // Создание I2C устройства
        try (I2C vl53l0x = pi4j.create(i2cConfig)) {

            // Проверка подключения датчика
            int modelId = vl53l0x.readRegister(VL53L0X_REG_IDENTIFICATION_MODEL_ID);
            System.out.println("VL53L0X Model ID: " + modelId);

            // Основной цикл для чтения расстояния
            while (true) {
                // Запуск измерения
                vl53l0x.writeRegister(VL53L0X_REG_SYSRANGE_START, 0x01);

                // Ожидание завершения измерения
                while ((vl53l0x.readRegister(VL53L0X_REG_RESULT_INTERRUPT_STATUS) & 0x07) == 0) {
                    Thread.sleep(10);
                }

                // Чтение результата
                int range = vl53l0x.readRegister(VL53L0X_REG_RESULT_RANGE_VAL);
                System.out.println("Distance: " + range + " mm");

                // Задержка между измерениями
                Thread.sleep(1000);
            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            // Завершение работы Pi4J
            pi4j.shutdown();
        }
    }
}
