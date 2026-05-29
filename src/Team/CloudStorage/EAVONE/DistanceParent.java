package Team.CloudStorage.EAVONE;

public class DistanceParent {
	public double    distance;    // distance from start to this vertex
    public int    parentVert; // current parent of this vertex
    public int pathLength;

    public DistanceParent(int pv, double d) // constructor
    {
        distance = d;
        parentVert = pv;
    }
    
    public DistanceParent(int pv, double d, int pLength) // constructor
    {
        distance = d;
        parentVert = pv;
        pathLength = pLength;
    }
}
