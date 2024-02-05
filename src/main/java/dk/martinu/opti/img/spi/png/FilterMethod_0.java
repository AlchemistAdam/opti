package dk.martinu.opti.img.spi.png;

import dk.martinu.opti.img.spi.ImageDataException;

final class FilterMethod_0 implements FilterMethod {

    private static final int TYPE_NONE = 0;
    private static final int TYPE_SUB = 1;
    private static final int TYPE_UP = 2;
    private static final int TYPE_AVERAGE = 3;
    private static final int TYPE_PAETH = 4;

    /**
     * DOC revert
     *
     * @param data   the filtered bytes source
     * @param offset index offset in the samples array
     * @param lines  the number of scanlines
     * @param nBytes the number of bytes in a scanline
     * @return
     * @throws ImageDataException
     */
    @Override
    public byte[] revert(byte[] data, int offset, int lines, int nBytes) throws ImageDataException {
        // destination for reconstructed bytes
        byte[] rec = new byte[data.length - lines];
        /*
        reconstruct filtered bytes
        https://www.w3.org/TR/png/#9Filter-types
        ----
        i: current scanline
        j: index of filtered bytes in samples
        k: index of reconstructed bytes in rec
        ----
        j and k are assigned in outer loop and incremented in inner loops
         */
        for (int i = 0, j = offset + 1, k = 0; i < lines; i++, j = i * (nBytes + 1) + 1, k = i * nBytes) {
            // filter type preceding filtered bytes in scanline
            int filterType = data[j - 1];

            if (filterType == TYPE_NONE) {
                System.arraycopy(data, j, rec, k, nBytes);
            }

            else if (filterType == TYPE_SUB) {
                int prev = 0;
                for (int max = j + nBytes; j < max; j++, k++) {
                    prev = data[j] + prev;
                    rec[k] = (byte) prev;
                }
            }

            else if (filterType == TYPE_UP) {
                if (i == 0) {
                    System.arraycopy(data, j, rec, k, nBytes);
                }
                else {
                    for (int max = j + nBytes; j < max; j++, k++) {
                        rec[k] = (byte) (data[j] + rec[k - nBytes]);
                    }
                }
            }

            else if (filterType == TYPE_AVERAGE) {
                int prev = 0;
                if (i == 0) {
                    for (int max = j + nBytes; j < max; j++, k++) {
                        prev = data[j] + ((prev & 0xFF) >>> 1);
                        rec[k] = (byte) prev;
                    }
                }
                else {
                    for (int max = j + nBytes; j < max; j++, k++) {
                        prev = data[j] + ((prev + rec[k - nBytes] & 0xFF) >>> 1);
                        rec[k] = (byte) prev;
                    }
                }
            }

            // https://www.w3.org/TR/png/#9Filter-type-4-Paeth
            else if (filterType == TYPE_PAETH) {
                if (i == 0) {
                    int prev = 0;
                    for (int max = j + nBytes; j < max; j++, k++) {
                        prev = data[j] + prev;
                        rec[k] = (byte) prev;
                    }
                }
                else {
                    // TODO it might be necessary to mask bytes for abs to work correctly
                    // reconstructed byte variables
                    int a, b = rec[k - nBytes], c;
                    // paeth function variables
                    int p, pa, pb, pc, pr;

                    final int max = j + nBytes;
                    // the predictor (pr) is always 'b' for the first byte
                    int prev = data[j++] + b;
                    rec[k++] = (byte) prev;

                    while (j < max) {
                        // swap over 'c' and 'a', and reassign 'b'
                        c = b;
                        b = rec[k - nBytes];
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

                        prev = data[j++] + pr;
                        rec[k++] = (byte) prev;
                    }
                }
            }
            else {
                throw new ImageDataException("invalid filter type {%d}", filterType);
            }
        }
        return rec;
    }
}
