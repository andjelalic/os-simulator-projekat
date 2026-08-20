package filesystem;

public class OpenFileHandle {
    private final File file;
    private int position;
    private final FileMode mode;

    public OpenFileHandle(File file, FileMode mode){
        this.file = file;
        this.mode = mode;
        this.position = 0;
    }

    public File getFile() {
        return file;
    }

    public int getPosition() {
        return position;
    }

    public void setPosition(int position) {
        this.position = position;
    }

    public FileMode getMode() {
        return mode;
    }
}
