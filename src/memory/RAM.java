package memory;

public class RAM {
    private int size;
    private int[] cells;

    public RAM(int size) {
        this.size = size;
        cells = new int[size];
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }

    public int[] getCells() {
        return cells;
    }

    public void setCells(int[] cells) {
        this.cells = cells;
    }
}
