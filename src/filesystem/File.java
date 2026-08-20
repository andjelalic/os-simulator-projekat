package filesystem;

public class File extends FsNode{
    private StringBuilder content;

    public File(String name, Directory parent){
        super(name, parent);
        this.content = new StringBuilder();
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
}
