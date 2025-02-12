package ru.emelyantsev.raspberrydriftlights;

import com.pi4j.Pi4J;
import com.pi4j.context.Context;
import com.pi4j.io.IOType;
import com.pi4j.io.i2c.I2C;
import com.pi4j.io.i2c.I2CConfig;
import com.pi4j.io.i2c.I2CProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.Comparator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Pi4j {
    private static final Logger logger = LoggerFactory.getLogger(Pi4j.class);
    // Адреса устройств
    private static final int TCA9548A_ADDRESS = 0x70; // Адрес мультиплексора TCA9548A
    private static final int VL53L0X_DEFAULT_ADDRESS = 0x29; // Адрес датчика VL53L0X
    // Контекст Pi4J и устройства
    private Context pi4j;
    private I2C tca9548a;
    private I2C vl53l0x;
   // private I2C vl53l0x2;
   private int[] tcaChannels = {1, 2};
   private int[] newAddresses = {0x31, 0x32};
    private static final ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor();
    private static final Map<Integer, VL53L0X_Device> sensors = new ConcurrentHashMap<>();


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
        Arrays.stream(tcaChannels).forEach(channel -> {
            logger.info("Start change");
            logger.info(pi4j.registry().allByIoType(IOType.I2C).toString());
            vl53l0x = pi4j.i2c().create(1, 0x29);
            setNewAddress(channel, newAddresses[channel], vl53l0x);
            vl53l0x = null;
            logger.info("End change");
        sensors.put(channel, new VL53L0X_Device(pi4j, 1, newAddresses[channel], "info"));});
//        for (Map.Entry<Integer, VL53L0X_Device> entry : sensors.entrySet()) {
//            executor.submit(() -> run(entry.getValue(), ));
//        }
        logger.info("TCA9548A initialized.");
//        Arrays.stream(tcaChannelsreverse).forEach(channel -> {
//            executor.submit(() -> run(sensors.get(channel), channel));
//        });
        executor.submit(() -> run(sensors.get(0), 1));
        try {
            Thread.sleep(10000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        executor.submit(() -> run(sensors.get(1), 2));
    }

    // Выбор канала на мультиплексоре TCA9548A
    private void selectChannel(int channel) {
        if (channel < 0 || channel > 7) {
            throw new IllegalArgumentException("Channel must be between 0 and 7");
        }
        tca9548a.writeRegister(0x00, (byte) (1 << channel));
        logger.info("Selected channel: {}", channel);
    }

    public void setNewAddress(int channel, int newAddress, I2C vl53l0x) {
        // Выбор канала на мультиплексоре
        selectChannel(channel);
        vl53l0x.writeRegister(0x8A, (byte) newAddress);
        vl53l0x.shutdown(pi4j);
        vl53l0x.close();
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        String deviceId = "I2C-1.41";
        if (pi4j.registry().exists(deviceId)) {
            logger.warn("Removing existing I2C instance: {}", deviceId);
            pi4j.registry().remove(deviceId);
        }
        logger.info("Changed address for sensor on channel {} to 0x{}", channel, Integer.toHexString(newAddress));
    }

    // Чтение расстояния с датчика VL53L0X
    private int readDistance(VL53L0X_Device vl53l0x, Integer channel) {
        logger.info("try to read distance {}" , channel);
        return vl53l0x.range();
    }

    public void run(VL53L0X_Device sensor, Integer channel) {
        logger.info("Starting sensor: {}", channel);
        while (true) {
            int distance = readDistance(sensor, channel);
            logger.info("Distance: {} mm, Sensor {}", distance, channel);
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }
}