package ru.emelyantsev.raspberrydriftlights;

import com.pi4j.io.i2c.I2C;

public class pythonVL533L0X {
    // Адрес датчика по умолчанию
    private static final int DEFAULT_ADDRESS = 0x29;

    // Регистры датчика (примеры, могут отличаться в зависимости от датчика)
    private static final int SYSRANGE_START = 0x00;
    private static final int RESULT_INTERRUPT_STATUS = 0x13;
    private static final int RESULT_RANGE_STATUS = 0x14;
    private static final int FINAL_RANGE_CONFIG_MIN_COUNT_RATE_RTN_LIMIT = 0x44;

    // Переменная для работы с I2C (будет передана извне)
    private final I2C device;

    public pythonVL533L0X(I2C device) {
        this.device = device;
    }

    /**
     * Инициализация датчика.
     */
    public void initialize() throws Exception {
        // Включение датчика
        writeRegister(0x80, 0x01);
        writeRegister(0xFF, 0x01);
        writeRegister(0x00, 0x00);
        writeRegister(0x91, 0x00);
        writeRegister(0x00, 0x01);
        writeRegister(0xFF, 0x00);
        writeRegister(0x80, 0x00);

        // Калибровка (пример)
        writeRegister(FINAL_RANGE_CONFIG_MIN_COUNT_RATE_RTN_LIMIT, 0x00);
    }

    /**
     * Запуск измерения расстояния.
     */
    public void startMeasurement() throws Exception {
        writeRegister(SYSRANGE_START, 0x01);
    }

    /**
     * Проверка, завершено ли измерение.
     */
    public boolean isMeasurementComplete() throws Exception {
        return (readRegister(RESULT_INTERRUPT_STATUS) & 0x07) != 0;
    }

    /**
     * Чтение результата измерения.
     */
    public int readDistance() throws Exception {
        // Ожидание завершения измерения
        while (!isMeasurementComplete()) {
            Thread.sleep(10); // Небольшая задержка
        }

        // Чтение результата из регистров
        byte[] buffer = new byte[2];
        device.readRegister(RESULT_RANGE_STATUS, buffer, 0, 2);
        int range = (buffer[0] & 0xFF) << 8 | (buffer[1] & 0xFF);

        return range;
    }

    /**
     * Чтение значения из регистра.
     */
    private int readRegister(int register) throws Exception {
        return device.readRegister(register) & 0xFF;
    }

    /**
     * Запись значения в регистр.
     */
    private void writeRegister(int register, int value) throws Exception {
        device.writeRegister(register, (byte) value);
    }
}