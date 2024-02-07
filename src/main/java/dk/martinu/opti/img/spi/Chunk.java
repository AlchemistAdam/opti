package dk.martinu.opti.img.spi;

import java.util.Objects;

public record Chunk(int type, byte[] data, int crc) {

    private static final int ancillaryBit = 0x10 << 24;
    private static final int privateBit = 0x10 << 16;
    private static final int reservedBit = 0x10 << 8;
    private static final int ancillaryBit = 0x20 << 24;
    private static final int privateBit = 0x20 << 16;
    private static final int reservedBit = 0x20 << 8;

    public Chunk(int type, byte[] data, int crc) {
        this.type = type;
        this.data = Objects.requireNonNull(data, "data is null");
        this.crc = crc;
    }

    public boolean isCritical() {
        return (type & ancillaryBit) == 0;
    }

    public boolean isPrivate() {
        return (type & privateBit) != 0;
    }

    public boolean isReserved() {
        return (type & reservedBit) != 0;
    }
}
