package dk.martinu.opti.img.spi.png;

import dk.martinu.opti.img.spi.ImageDataException;

public class FilterMethod_0 implements FilterMethod {

    public static final int NONE = 0;
    public static final int SUB = 1;
    public static final int UP = 2;
    public static final int AVERAGE = 3;
    public static final int PAETH = 4;

    /**
     * DOC reconstruct
     *
     * @param src   the filtered bytes source
     * @param lines the number of scanlines
     * @param len   the length of a reconstructed scanline
     * @return
     * @throws ImageDataException
     */
    @Override
    public byte[] reconstruct(byte[] src, int lines, int len) throws ImageDataException {
        // destination for reconstructed bytes
        byte[] dest = new byte[src.length - lines];
        /*
        reconstruct filtered bytes
        https://www.w3.org/TR/png/#9Filter-types
        ----
        i: current scanline
        j: index of filtered bytes in src
        k: index of reconstructed bytes in dest
        ----
        j and k are assigned in outer loop and incremented in inner loops
         */
        for (int i = 0, j = 1, k = 0; i < lines; i++, j = i * (len + 1) + 1, k = i * len) {
            // filter type preceding filtered bytes in scanline
            int filterType = src[j - 1];

            if (filterType == NONE) {
                System.arraycopy(src, j, dest, k, len);
            }

            else if (filterType == SUB) {
                int prev = 0;
                for (int max = j + len; j < max; j++, k++) {
                    prev = src[j] + prev;
                    dest[k] = (byte) prev;
                }
            }

            else if (filterType == UP) {
                if (i == 0) {
                    System.arraycopy(src, j, dest, k, len);
                }
                else {
                    for (int max = j + len; j < max; j++, k++) {
                        dest[k] = (byte) (src[j] + dest[k - len]);
                    }
                }
            }

            else if (filterType == AVERAGE) {
                int prev = 0;
                if (i == 0) {
                    for (int max = j + len; j < max; j++, k++) {
                        prev = src[j] + ((prev & 0xFF) >>> 1);
                        dest[k] = (byte) prev;
                    }
                }
                else {
                    for (int max = j + len; j < max; j++, k++) {
                        prev = src[j] + ((prev + dest[k - len] & 0xFF) >>> 1);
                        dest[k] = (byte) prev;
                    }
                }
            }

            // https://www.w3.org/TR/png/#9Filter-type-4-Paeth
            else if (filterType == PAETH) {
                if (i == 0) {
                    int prev = 0;
                    for (int max = j + len; j < max; j++, k++) {
                        prev = src[j] + prev;
                        dest[k] = (byte) prev;
                    }
                }
                else {
                    // TODO it might be necessary to mask ints for abs to work correctly
                    // reconstructed byte variables
                    int a, b = dest[k - len], c;
                    // paeth function variables
                    int p, pa, pb, pc, pr;

                    final int max = j + len;
                    // the predictor (pr) is always 'b' for the first byte
                    int prev = src[j++] + b;
                    dest[k++] = (byte) prev;

                    while (j < max) {
                        // swap over 'c' and 'a', and reassign 'b'
                        c = b;
                        b = dest[k - len];
                        a = prev;
                        // update paeth variables
                        p = a + b - c;
                        pa = Math.abs(p - a);
                        pb = Math.abs(p - b);
                        pc = Math.abs(p - c);
                        // determine predictor
                        if (pa <= pb && pa <= pc) {
                            pr = a;
                        }
                        else if (pb <= pc) {
                            pr = b;
                        }
                        else {
                            pr = c;
                        }

                        prev = src[j++] + pr;
                        dest[k++] = (byte) prev;
                    }
                }
            }
            else {
                throw new ImageDataException("invalid filter type {%d}", filterType);
            }
        }
        return dest;
    }
}
