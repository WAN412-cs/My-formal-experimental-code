package Team.CloudStorage.EAVONE;

public class VNRequest {
	public int VNNameID;	
	public int split;
	public int time;
	public int topo;
	public int duration;
	public double revenue;
	public int nodes;      //The number of virtual nodes.
	public int links;      //The number of virtual links.
	public double cpu[];
	public LinkStruct link[];
	public String VNsFileDir;
	public int map;
	
	public double cb_value[];
	public double sum_adj[];
	public double rank[];

}
