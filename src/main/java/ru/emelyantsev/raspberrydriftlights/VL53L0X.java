package ru.emelyantsev.raspberrydriftlights;
import com.pi4j.Pi4J;
import com.pi4j.context.Context;
import com.pi4j.io.gpio.digital.DigitalInput;
import com.pi4j.io.gpio.digital.DigitalInputConfig;
import com.pi4j.io.gpio.digital.DigitalState;
import com.pi4j.io.gpio.digital.PullResistance;
import com.pi4j.io.i2c.I2C;
import com.pi4j.io.i2c.I2CConfig;
import com.pi4j.io.i2c.I2CProvider;

public class VL53L0X {

    private static final int DEFAULT_I2C_ADDRESS = 0x29;
    private static final int I2C_BUS = 1;

    private Context pi4j;
    private I2C i2c;

    public VL53L0X() {
        this(I2C_BUS, DEFAULT_I2C_ADDRESS);
    }

    public VL53L0X(int bus, int address) {
        pi4j = Pi4J.newAutoContext();

        I2CProvider i2cProvider = pi4j.provider("linuxfs-i2c");
        I2CConfig config = I2C.newConfigBuilder(pi4j)
                .id("VL53L0X")
                .bus(bus)
                .device(address)
                .build();

        i2c = i2cProvider.create(config);
    }

    public void startRanging() {
        // Example: write to a register to start ranging
        i2c.writeRegister(0x00, (byte) 0x01); // Hypothetical start command
        System.out.printf("Starting ranging\n");
    }

    public void stopRanging() {
        // Example: write to a register to stop ranging
        i2c.writeRegister(0x00, (byte) 0x00); // Hypothetical stop command
    }

    public int getDistance() {
        byte[] buffer = new byte[2];
        i2c.readRegister(0x1E, buffer); // Hypothetical distance register
        int distance = ((buffer[0] & 0xFF) << 8) | (buffer[1] & 0xFF);
        return distance;
    }

    public void changeAddress(int newAddress) {
        // Write unlock sequence
        i2c.writeRegister(0x18, (byte) 0xAA); // Hypothetical unlock high
        i2c.writeRegister(0x19, (byte) 0xBB); // Hypothetical unlock low

        // Write new address
        i2c.writeRegister(0x8A, (byte) (newAddress & 0x7F));
    }

    public void configureGPIOInterrupt(int gpioPin) {
        DigitalInputConfig gpioConfig = DigitalInput.newConfigBuilder(pi4j)
                .id("VL53L0X-INT")
                .name("VL53L0X Interrupt")
                .address(gpioPin)
                .pull(PullResistance.PULL_UP)
                .build();

        DigitalInput interruptPin = pi4j.create(gpioConfig);

        interruptPin.addListener(e -> {
            if (e.state() == DigitalState.LOW) {
                System.out.println("VL53L0X Interrupt Triggered");
            }
        });
    }

    public void close() {
        if (i2c != null) i2c.close();
        if (pi4j != null) pi4j.shutdown();
    }
}