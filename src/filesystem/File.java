package filesystem;

public class File extends FsNode{
    private StringBuilder content;

    private int startBlock;
    private int size;

    public File(String name, Directory parent){
        super(name, parent);
        this.content = new StringBuilder();
        this.startBlock = -1;
        this.size = 0;
    }

    public String read() {
        return content.toString();
    }

    public void write(String data) {
        content = new StringBuilder(data);
    }

    public void append(String data) {
        content.append(data);
    }

    @Override
    public String getPath() {
        return this.parent.getPath() + "/" + name;
    }
}
