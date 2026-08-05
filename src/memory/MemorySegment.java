package memory;

import process.PCB;

public class MemorySegment {
    private PCB owner;
    private int base, limit;

    public MemorySegment(PCB owner, int base, int limit) {
        this.owner = owner;
        this.base = base;
        this.limit = limit;
    }

    public PCB getOwner() {
        return owner;
    }

    public void setOwner(PCB owner) {
        this.owner = owner;
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

    public void setLimit(int limit) {
        this.limit = limit;
    }
}
