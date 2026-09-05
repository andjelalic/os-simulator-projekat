package filesystem;

import io.DiskDevice;

import java.util.ArrayList;

public class FileSystem {
    private Directory root;
    private DiskDevice disk;


    public FileSystem(Directory root, DiskDevice disk) {
        this.root = root;
        this.disk = disk;
    }

    public File createFile(String path){
        String normalizedPath = path.startsWith("/")
                ? path.substring(1)
                : path;

        String[] segments = normalizedPath.split("/");
        String fileName = segments[segments.length-1];

        Directory parent = resolveParent(segments);
        if(parent.getChild(fileName) != null){
            throw new IllegalArgumentException("A node with the same name already exists in the given directory");
        }

        File file = new File(fileName, parent);
        parent.addChild(file);

        return file;
    }

    public Directory createDirectory(String path){
        String normalizedPath = path.startsWith("/")
                ? path.substring(1)
                : path;

        String[] segments = normalizedPath.split("/");
        String directoryName = segments[segments.length-1];

        Directory parent = resolveParent(segments);
        if(parent.getChild(directoryName) != null){
            throw new IllegalArgumentException("A node with the same name already exists in the given directory");
        }

        Directory directory = new Directory(directoryName, parent);
        parent.addChild(directory);

        return directory;
    }

    public OpenFileHandle open(String path, FileMode mode) {
        FsNode node = resolve(path);

        if (!(node instanceof File file)) {
            throw new IllegalArgumentException(
                    "Path does not point to a file"
            );
        }

        return new OpenFileHandle(file, mode);
    }

    public FsNode resolve(String path) {
        String normalizedPath = path.startsWith("/")
                ? path.substring(1)
                : path;

        if (normalizedPath.isEmpty()) {
            return root;
        }

        String[] segments = normalizedPath.split("/");

        FsNode current = root;

        for (String segment : segments) {
            if (!(current instanceof Directory directory)) {
                throw new IllegalArgumentException(
                        current.getName() + " is not a directory"
                );
            }

            FsNode child = directory.getChild(segment);

            if (child == null) {
                throw new IllegalArgumentException(
                        "No such file or directory: " + segment
                );
            }

            current = child;
        }

        return current;
    }

    // pomoćna metoda recimo
    private Directory resolveParent(String[] segments) {
        Directory parent = root;

        for(int i = 0; i < segments.length - 1; i++) {
            FsNode node = parent.getChild(segments[i]);

            if(node == null){
                throw new IllegalArgumentException("there is no directory named " + segments[i] + " in the " + parent.getName() + " directory");
            }

            if(!(node instanceof Directory)){
                throw new IllegalArgumentException(node.getName() + " is not a directory");
            }

            parent = (Directory) node;
        }
        return parent;
    }

    public void delete(String path) {
        FsNode node = resolve(path);

        if (node == root) {
            throw new IllegalArgumentException("Cannot delete root");
        }

        deleteRecursively(node);

        node.getParent().removeChild(node);
    }

    private void deleteRecursively(FsNode node) {
        if (node instanceof File) {
            // ovde treba da se oslobodi prostor na disku, što još nisam implementirala
            return;
        }

        Directory directory = (Directory) node;

        for (FsNode child : new ArrayList<>(directory.list())) {
            deleteRecursively(child);
            directory.removeChild(child);
        }
    }

    public Directory getRoot() {
        return root;
    }

    public DiskDevice getDisk() {
        return disk;
    }
}
