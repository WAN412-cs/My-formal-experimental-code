package Team.CloudStorage.EAVONE;


public class AuxiliaryGraph extends EOSubstrateNetwork{
	public int[] serverNodes;	//服务器节点
	public int[] virtualNodes;	//虚拟节点
	public LinkStruct[] virtServLinks;//在辅助图中，虚拟节点与服务器节点之间的链路
}

