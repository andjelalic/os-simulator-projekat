package filesystem;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Directory extends FsNode{
    private Map<String, FsNode> children;

    public Directory(String name, Directory parent) {
        super(name, parent);
    }

    public void addChild(FsNode node){
        if(children.containsKey(node.name)){
            // ispisuje se poruka da nešto sa tim nazivom već postoji i daje se izbor da se replace postojeći node
            // ako bude komplikovano za implementirati, nema izbora za replace, biće samo može - ne može i ćao
            if(node instanceof File){
                System.out.println("U trenutnom direktorijumu već postoji datoteka sa istim nazivom.");
            }else{
                System.out.println("U trenutnom direktorijumu već postoji direktorijum sa istim nazivom.");
            }
        }else{
            children.put(node.name, node);
        }
    }

    public FsNode getChild(String name){
        if(children.containsKey(name))
            return children.get(name);
        return null;
    }

    public List<FsNode> list(){
        // ????? biće nešto
        return null;
    }
}
