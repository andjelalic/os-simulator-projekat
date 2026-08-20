package memory;

import process.PCB;

public class MemorySegment {
    public static final int SEGMENT_SIZE = 16;

    private final PCB owner;
    private int base;
    private int limit = SEGMENT_SIZE;

    public MemorySegment(PCB owner, int base) {
        this.owner = owner;
        this.base = base;
    }

    public PCB getOwner() {
        return owner;
    }

    public int getBase() {
        return base;
    }

    public void setBase(int base) {
        this.base = base;
    }

    public int getLimit() {
        return limit;
    }

}
