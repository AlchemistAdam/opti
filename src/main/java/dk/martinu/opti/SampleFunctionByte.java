package dk.martinu.opti;

/**
 * Super type for sample functions that read pixels with {@code byte} data
 * elements.
 *
 * @author Adam Martinu
 */
@FunctionalInterface
public interface SampleFunctionByte extends SampleFunction<byte[]> { }
