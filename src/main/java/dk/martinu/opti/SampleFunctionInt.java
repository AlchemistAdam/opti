package dk.martinu.opti;

/**
 * Super type for sample functions that read pixels with {@code int} data
 * elements.
 *
 * @author Adam Martinu
 */
@FunctionalInterface
public interface SampleFunctionInt extends SampleFunction<int[]> { }
