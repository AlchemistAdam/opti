package dk.martinu.opti.img.spi.png;

final class ChunkType {

    public static final int IHDR = 'I' << 24 | 'H' << 16 | 'D' << 8 | 'R';
    public static final int PLTE = 'P' << 24 | 'L' << 16 | 'T' << 8 | 'E';
    public static final int IDAT = 'I' << 24 | 'D' << 16 | 'A' << 8 | 'T';
    public static final int IEND = 'I' << 24 | 'E' << 16 | 'N' << 8 | 'D';
    public static final int tRNS = 't' << 24 | 'R' << 16 | 'N' << 8 | 'S';
    public static final int cHRM = 'c' << 24 | 'H' << 16 | 'R' << 8 | 'M';
    public static final int gAMA = 'g' << 24 | 'A' << 16 | 'M' << 8 | 'A';
    public static final int iCCP = 'i' << 24 | 'C' << 16 | 'C' << 8 | 'P';
    public static final int sBIT = 's' << 24 | 'B' << 16 | 'I' << 8 | 'T';
    public static final int sRGB = 's' << 24 | 'R' << 16 | 'G' << 8 | 'B';
    public static final int cICP = 'c' << 24 | 'I' << 16 | 'C' << 8 | 'P';
    public static final int mDCv = 'm' << 24 | 'D' << 16 | 'C' << 8 | 'v';
    public static final int cLLi = 'c' << 24 | 'L' << 16 | 'L' << 8 | 'i';
    public static final int iTXt = 'i' << 24 | 'T' << 16 | 'X' << 8 | 't';
    public static final int tEXt = 't' << 24 | 'E' << 16 | 'X' << 8 | 't';
    public static final int zTXt = 'z' << 24 | 'T' << 16 | 'X' << 8 | 't';
    public static final int bKGD = 'b' << 24 | 'K' << 16 | 'G' << 8 | 'D';
    public static final int hIST = 'h' << 24 | 'I' << 16 | 'S' << 8 | 'T';
    public static final int pHYs = 'p' << 24 | 'H' << 16 | 'Y' << 8 | 's';
    public static final int sPLT = 'S' << 24 | 'P' << 16 | 'L' << 8 | 'T';
    public static final int eXIf = 'e' << 24 | 'X' << 16 | 'I' << 8 | 'f';
    public static final int tIME = 't' << 24 | 'I' << 16 | 'M' << 8 | 'E';

    private ChunkType() { }
}
