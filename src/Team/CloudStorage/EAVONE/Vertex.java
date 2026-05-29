package Team.CloudStorage.EAVONE;

public class Vertex {
	public String        label;        // label (e.g. 'A')char  
	public int nodeName;

    public boolean    wasVisited;
    public boolean    isInTree;

    public Vertex(String lab ) // constructor
    {
        label = lab;
        wasVisited = false;
        isInTree = false;
        nodeName = -1;
    }
}
