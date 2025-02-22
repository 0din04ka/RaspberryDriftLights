package ru.emelyantsev.raspberrydriftlights;

import com.pi4j.Pi4J;
import com.pi4j.context.Context;
import com.pi4j.io.i2c.I2C;
import com.pi4j.io.i2c.I2CConfig;
import com.pi4j.io.i2c.I2CProvider;

public class Pi4J3 {
    // Адрес датчика VL53L0X по умолчанию
    private static final int VL53L0X_I2C_ADDRESS = 0x29;
    private static final int TCA9548A_ADDRESS = 0x70; // Адрес мультиплексора TCA9548A

    private I2C tca9548a;

    Pi4J3() {
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
            pythonVL533L0X sensor = new pythonVL533L0X(vl53l0x);
            sensor.initialize();
            sensor.startMeasurement();

            while (true) {
                int distance = sensor.readDistance();
                System.out.println("Distance: " + distance);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            pi4j.shutdown();
        }
    }
}
