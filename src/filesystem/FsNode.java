package filesystem;

public abstract class FsNode {
    protected String name;
    protected Directory parent;

    public FsNode(String name, Directory parent) {
        this.name = name;
        this.parent = parent;
    }

    public String getName() {
        return name;
    }

    public Directory getParent() {
        return parent;
    }

    public abstract String getPath();
}
