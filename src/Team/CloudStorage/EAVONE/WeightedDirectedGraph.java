/*
1.Dijkstra
1)      适用条件&范围：
a)       单源最短路径(从源点s到其它所有顶点v);
b)       有向图&无向图(无向图可以看作(u,v),(v,u)同属于边集E的有向图)
c)       所有边权非负(任取(i,j)∈E都有Wij≥0);

2)      算法描述：
a)       初始化：dis[v]=maxint(v∈V,v≠s); dis[s]=0; pre[s]=s; S={s};
b)       For i:=1 to n
            1.取V-S中的一顶点u使得dis[u]=min{dis[v]|v∈V-S}
            2.S=S+{u}
            3.For V-S中每个顶点v do Relax(u,v,Wu,v)
c)       算法结束：dis[i]为s到i的最短距离；pre[i]为i的前驱节点

3)      算法优化：
    使用二叉堆(Binary Heap)来实现每步的DeleteMin(ExtractMin，即算法步骤b中第1步)操作，算法复杂度从O(V^2)降到O((V+E)㏒V)。推荐对稀疏图使用。
    使用Fibonacci Heap(或其他Decrease操作O(1),DeleteMin操作O(logn)的数据结构)可以将复杂度降到O(E+V㏒V)；如果边权值均为不大于C的正整数，则使用Radix Heap可以达到O(E+V㏒C)。
 */
package Team.CloudStorage.EAVONE;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.LinkedList;

import javax.swing.JOptionPane;

public class WeightedDirectedGraph {
	//private final int            MAX_VERTS    = 200;
	public final double            INFINITY    = 1000000;
    public Vertex[]            vertexList;            // list of vertices
    public double[][]                adjMat;                // adjacency matrix
    public int                    nVerts;                // current number of vertices
    private int                    nTree;                    // number of verts in tree
    public DistanceParent[]    shortestPath;                    // array for shortest-path data
    public DistanceParent[]    shortestPathLength;                    // array for shortest-path-length data，记录路径长度 
    
    private int exVirNodes;
    private int                    currentVertex;            // current vertex
    private double                    startToCurrentDistance;        // distance to currentVert
    
    //private DistanceParent[][]                 kShortestPath;//k最短路径,kShortestPath[0][]:表示第1个最短路径
    //private LinkedList<Vertex> extendNodes = new LinkedList<Vertex>(); 
    private Hashtable extendNodeHash=new Hashtable(2,(float)0.8);//扩展节点
    private Hashtable extendArcHash=new Hashtable(2,(float)0.8);//新增弧
    private int extendArcNum = 0;
    public DistanceParent[][]   kShortestPath;
    
    
    public WeightedDirectedGraph(int MAX_VERTS,int inExVirNodes)
    {
    	exVirNodes = inExVirNodes;
        vertexList = new Vertex[MAX_VERTS];
        // adjacency matrix
        adjMat = new double[MAX_VERTS][MAX_VERTS];
        nVerts = 0;
        nTree = 0;
        for(int j = 0; j < MAX_VERTS; j++ ) {
            // set adjacency
            for (int k = 0; k < MAX_VERTS; k++ ) {
                adjMat[j][k] = INFINITY;
            }
        }    
        for(int i=0;i<MAX_VERTS;i++){
    		//addVertex(String.valueOf(i));    //增加节点
    	}
        shortestPath = new DistanceParent[MAX_VERTS]; // shortest paths
    }
    
    public WeightedDirectedGraph(int MAX_VERTS)
    {
        vertexList = new Vertex[MAX_VERTS];
        // adjacency matrix
        adjMat = new double[MAX_VERTS][MAX_VERTS];
        nVerts = 0;
        nTree = 0;
        for(int j = 0; j < MAX_VERTS; j++ ) {
            // set adjacency
            for (int k = 0; k < MAX_VERTS; k++ ) {
                adjMat[j][k] = INFINITY;
            }
        }
        for(int i=0;i<MAX_VERTS;i++){
    		addVertex(String.valueOf(i));    //增加节点
    	}
        shortestPath = new DistanceParent[MAX_VERTS]; // shortest paths
    }
    
    //求K最短路径
    //返回值：最短路径条数
    public int findKShortestPath(int K,int s,int t)
    {
    	if(K <= 0) return K;
    	int tt = t;
    	int k = 0,exNVerts = 0;//exNVerts为扩展节点数量    	
    	//DistanceParent[][]   kShortestPath = new DistanceParent[K][nVerts];
    	kShortestPath = new DistanceParent[K][nVerts];
    	//DistanceParent[][]   kShortestPathSupp = new DistanceParent[K][nVerts];
    	DistanceParent[]   kShortestPathSupp;
    	//LinkedList<DistanceParent> kShortestPathLink = new LinkedList<DistanceParent>(); 
    	
    	findShortestPath(s);//shortestPath[],vertexList[]表示最短路径
    	if(shortestPath[t].distance == INFINITY ) return 0;//不存在最短路径
    	    	
    	//记录路径
    	kShortestPath[0] = shortestPath;
    	kShortestPathSupp = shortestPath;
    	//kShortestPathSupp[0] = shortestPath;    	
    	//for(int i=0;i<shortestPath.length;i++){
    	//	kShortestPathLink.add(shortestPath[i]);
    	//}
    	
    	while(k < K-1){
    		//找到第一个节点开始的入度大于1的第一个节点，记为nh
    		int[] ret = new int[3];
    		int findIf = GetFirstNodeOfLargerInDegree(s,tt,ret,kShortestPathSupp);    		
    		int nh = ret[0];//找到该节点
    		int preNH = ret[1];//找到该节点的前驱结点
    		int afterNH = ret[2];//找到该节点的后续节点
    		if(findIf != -1 && nh != -1){//说明 找到了节点
    			if(extendNodeHash.containsKey(String.valueOf(nh))){	//扩展节点在节点集合中
    				nh = GetFirstNoExtNodeInShortestPath(nh,tt,kShortestPathSupp);//找到除去nh的第一个节点
    			} else {//扩展节点不在节点集合中
    				//建立扩展节点
    				int extendNode = nVerts+exNVerts;
    				exNVerts ++;
    				extendNodeHash.put(String.valueOf(nh), String.valueOf(extendNode));//增加扩展节点
    				if(Parameters.DebugModel) System.out.print("("+nh+","+extendNode+")"); // B=
    				LinkedList<Arc> arcs = new LinkedList<Arc>(); 
    				arcs = GetArcNodes(nh,preNH);//得到前驱结点和弧路径集合
    				for (Arc tmp: arcs) {  
    					tmp.node2 = extendNode;//
    					if(Parameters.DebugModel) System.out.println(extendArcNum+"("+tmp.node1+","+tmp.node2+","+tmp.cost+")"); // B=
    					extendArcHash.put(extendArcNum, tmp);//增加弧
    					extendArcNum++;
    				}  
    				nh = afterNH;
    			}
    		}
    		
    		//添加nh的所有后续节点（包括nh）的扩展节点
    		int j = tt;    		
        	while(j != nh && nh != -1){
        		int extendNode = nVerts+exNVerts;
        		extendNodeHash.put(String.valueOf(j), String.valueOf(extendNode));//增加扩展节点
        		if(Parameters.DebugModel) System.out.print("("+j+","+extendNode+")"); // B=
        		exNVerts ++;
        		if(exNVerts > nVerts*nVerts*nVerts) return k;
        		j = kShortestPathSupp[j].parentVert;
        	} 
        	int extendNode = -1;
        	if(nh != -1){
        		extendNode = nVerts+exNVerts;//扩展节点
        		if(Parameters.DebugModel) System.out.print("("+nh+","+extendNode+")"); // B=
        		extendNodeHash.put(String.valueOf(nh), String.valueOf(extendNode));//增加扩展节点
        		exNVerts ++;
        	}
        	
        	
        	//分别从nh的前驱结点到其扩展节点nh‘的弧，权值不变，添加到集合中
        	j = tt;
        	while(j != nh && nh != -1){     
        		//System.out.println(extendNodeHash.get(String.valueOf(j))); 
        		String str = String.valueOf(extendNodeHash.get(String.valueOf(j)));
        		extendNode = Integer.valueOf(str);//(int)extendNodeHash.get(String.valueOf(j));//得到j的扩展节点extendNode
        		LinkedList<Arc> arcs = new LinkedList<Arc>(); 
				arcs = GetArcNodes(j,kShortestPathSupp[j].parentVert);//得到前驱结点和弧路径集合//得到j的前驱结点
				for (Arc tmp: arcs) {  
					tmp.node2 = extendNode;//
					if(Parameters.DebugModel) System.out.println(extendArcNum+"("+tmp.node1+","+tmp.node2+","+tmp.cost+")"); 					
					extendArcHash.put(extendArcNum, tmp);//增加弧
					extendArcNum++;	
					
					//如果tmp.node1有扩展节点，则增加扩展节点与该节点的弧
					String str1 = String.valueOf(extendNodeHash.get(String.valueOf(tmp.node1)));
					//System.out.print(extendNode+","+str1); 
					if(str1 != "null"){
						int extExtNode = Integer.valueOf(str1);//得到j的扩展节点extendNode
						if(extExtNode != -1){
							tmp.node1 = extExtNode;
							tmp.node2 = extendNode;//
							if(Parameters.DebugModel) System.out.println(extendArcNum+"("+tmp.node1+","+tmp.node2+","+tmp.cost+")"); 
							extendArcHash.put(extendArcNum, tmp);//增加弧
							extendArcNum++;
						}
					}
				}	
				//判断j的kShortestPathSupp[j].parentVert是否有扩展节点，如果有a，则增加该弧(a,extendNode,kShortestPathSupp[j].cost)
				String str1 = String.valueOf(extendNodeHash.get(String.valueOf(kShortestPathSupp[j].parentVert)));
				if(str1 != "null"){
					int extExtNode = Integer.valueOf(str1);//得到j的扩展节点extendNode
					if(extExtNode != -1 && extendNode != -1){
						Arc tmp = new Arc();
						tmp.node1 = extExtNode;
						tmp.node2 = extendNode;//
						tmp.cost = kShortestPathSupp[j].distance - kShortestPathSupp[kShortestPathSupp[j].parentVert].distance;
						if(Parameters.DebugModel) System.out.println(extendArcNum+"("+tmp.node1+","+tmp.node2+","+tmp.cost+")"); 
						extendArcHash.put(extendArcNum, tmp);//增加弧
						extendArcNum++;
					}
				}
        		j = kShortestPathSupp[j].parentVert;
        	}     
        	if(j == nh){
        		String str = String.valueOf(extendNodeHash.get(String.valueOf(j)));
        		extendNode = Integer.valueOf(str);//(int)extendNodeHash.get(String.valueOf(j));//得到j的扩展节点extendNode
        		LinkedList<Arc> arcs = new LinkedList<Arc>(); 
				arcs = GetArcNodes(j,kShortestPathSupp[j].parentVert);//得到前驱结点和弧路径集合//得到j的前驱结点
				for (Arc tmp: arcs) {  
					if(tmp.node1 != -1 && tmp.node2 != -1)
					{
						tmp.node2 = extendNode;//
						if(Parameters.DebugModel) System.out.println(extendArcNum+"("+tmp.node1+","+tmp.node2+","+tmp.cost+")"); 
						extendArcHash.put(extendArcNum, tmp);//增加弧
						extendArcNum++;		
					}
					
					
					//如果tmp.node1有扩展节点，则增加扩展节点与该节点的弧
					String str1 = String.valueOf(extendNodeHash.get(String.valueOf(tmp.node1)));
					//System.out.print(extendNode+","+str1); // B=
					if(str1 != "null"){
						int extExtNode = Integer.valueOf(str1);//得到j的扩展节点extendNode
						if(extExtNode != -1 && extendNode != -1){
							tmp.node1 = extExtNode;
							tmp.node2 = extendNode;//
							if(Parameters.DebugModel) System.out.println(extendArcNum+"("+tmp.node1+","+tmp.node2+","+tmp.cost+")"); 
							extendArcHash.put(extendArcNum, tmp);//增加弧
							extendArcNum++;
						}
					}
				}
				//判断j的kShortestPathSupp[j].parentVert是否有扩展节点，如果有a，则增加该弧(a,extendNode,kShortestPathSupp[j].cost)
				String str1 = String.valueOf(extendNodeHash.get(String.valueOf(kShortestPathSupp[j].parentVert)));
				if(str1 != "null"){
					int extExtNode = Integer.valueOf(str1);//得到j的扩展节点extendNode
					if(extExtNode != -1 && extendNode != -1){
						Arc tmp = new Arc();
						tmp.node1 = extExtNode;
						tmp.node2 = extendNode;//
						tmp.cost = kShortestPathSupp[j].distance - kShortestPathSupp[kShortestPathSupp[j].parentVert].distance;
						if(Parameters.DebugModel) System.out.println(extendArcNum+"("+tmp.node1+","+tmp.node2+","+tmp.cost+")"); 
						extendArcHash.put(extendArcNum, tmp);//增加弧
						extendArcNum++;
					}
				}
        	}
    		
    		//构造新的图结构，求最短路径
        	int nodesSum = exNVerts + nVerts;      
        	if(Parameters.DebugModel) System.out.println(nodesSum); 
        	WeightedDirectedGraph myGraph = new WeightedDirectedGraph(nodesSum);
        	myGraph.CreateNodesOfDireGraph(nodesSum);//创建节点        
        	if(Parameters.DebugModel) System.out.println("-------create arc----------------"+nodesSum);
        	//根据当前adjMat创建弧
        	for(int i = 0; i < nVerts; i++ ) {
        		for(int jj = 0; jj < nVerts; jj++ ) {
        			if(adjMat[i][jj] != INFINITY && adjMat[i][jj] >= 0) {
        				myGraph.CreateArcsOfDireGraph(i, jj, (int)adjMat[i][jj]);//创建弧
        				//System.out.print("("+i+","+jj+","+adjMat[i][jj]+")");
                    }
        		}                
            }  
        	
        	//根据扩展弧创建弧
        	Enumeration enum1 = extendArcHash.elements();
        	while(enum1.hasMoreElements()){
        		Arc tmp = (Arc)enum1.nextElement();
        		myGraph.CreateArcsOfDireGraph(tmp.node1, tmp.node2, (int)tmp.cost);//创建弧
        		if(Parameters.DebugModel) System.out.print("("+tmp.node1+","+tmp.node2+","+tmp.cost+")");
        	}
        	
        	if(Parameters.DebugModel) System.out.println("");
        	if(Parameters.DebugModel) System.out.println("-------create arc end----------------");
        	
			myGraph.findShortestPath(s);//找到最短路径s->tExtNode
			//记录s->tExtNode路径
			
			if(Parameters.DebugModel) System.out.println("kShortestPathSupp"); 
			
			k++;//k路径增加
			kShortestPathSupp = myGraph.shortestPath;
			//kShortestPath[k] = myGraph.shortestPath;
			
			//转换扩展节点为原始节点,更新kShortestPath。
			//算法：1）找到t对应的最终扩展节点tExt，以myGraph.shortestPath的tExt更新kShortestPath[t]；
			//      2）在最短路径中找到tExt的前驱结点pareExt，找到原始的节点pareNode，以myGraph.shortestPath的pareExt更新kShortestPath[pareNode]；
			String tExtNodeStr = GetExtExtendNode(String.valueOf(tt));
			for(int i=0;i<nVerts;i++){
				kShortestPath[k][i] = new DistanceParent(-1,-1);
			}
			
			int oriTT = Integer.valueOf(GetOriNodeFromExtendNode(String.valueOf(tt)));
			kShortestPath[k][oriTT].distance = myGraph.shortestPath[Integer.valueOf(tExtNodeStr)].distance;
			String oriNode = "";
			String startNode = "";
			startNode = String.valueOf(myGraph.shortestPath[Integer.valueOf(tExtNodeStr)].parentVert);
			oriNode = GetOriNodeFromExtendNode(startNode);
			if(Parameters.DebugModel) System.out.println(oriNode+"-"+startNode);
			kShortestPath[k][oriTT].parentVert = Integer.valueOf(oriNode);
			int oriIntNode = Integer.valueOf(oriNode);
			while(oriIntNode != s)
			{
				//更新distance
				
				kShortestPath[k][oriIntNode].distance = myGraph.shortestPath[Integer.valueOf(startNode)].distance;
								
				//更新parentVert(先得到父节点的原始节点)
				oriNode = GetOriNodeFromExtendNode(String.valueOf(myGraph.shortestPath[Integer.valueOf(startNode)].parentVert));
				kShortestPath[k][oriIntNode].parentVert = Integer.valueOf(oriNode);
				
				//得到startNode节点的parentNode
				startNode = String.valueOf(myGraph.shortestPath[Integer.valueOf(startNode)].parentVert);
				oriNode = GetOriNodeFromExtendNode(startNode);				
				oriIntNode = Integer.valueOf(oriNode);
			}
			
			tt = Integer.valueOf(GetExtExtendNode(String.valueOf(tt)));
			
			if(Parameters.DebugModel) displayPaths(kShortestPath[k]);
			
			//判断路径k是否是可行路径，如果不是，则返回
			if(kShortestPath[k][oriTT].distance == INFINITY){
				k--;
				break;
			}
			
			//判断路径k与k-1是否相同，如果相同，则说明没有路径，返回
			if(JudgePathIsSame(kShortestPath[k],kShortestPath[k-1]) == true){
				k--;
				break;
			}
				
    	}
    	if(Parameters.DebugModel) System.out.println("displayPaths"); 
    	//displayPaths
    	//for(int i=0;i<k;i++){
    		//displayPaths(kShortestPathSupp[i]);
    	//}
    	if(Parameters.DebugModel){
    		for(int i=0;i<k+1;i++){
    			displayPaths(kShortestPath[i]);
    		}
    	}
    	return k+1;
    }
    
    //求解最短路径长度大于等于pathLeng的K最短路径
    //返回值：最短路径条数
    public int findKShortestPathByMIL(int K,int s,int t,int pathLength)
    {
    	if(K <= 0) return K;
    	int tt = t;
    	int k = 0;
    	//DistanceParent[][]   kShortestPath = new DistanceParent[K][nVerts];
    	kShortestPath = new DistanceParent[K][nVerts];
    	//DistanceParent[][]   kShortestPathSupp = new DistanceParent[K][nVerts];
    	//DistanceParent[]   kShortestPathSupp;
    	findShortestPathBy01ILP(s,t,pathLength);
    	//findShortestPath(s,t,pathLength);//shortestPath[],vertexList[]表示最短路径
    	if(shortestPath[t].distance == INFINITY ) return 0;//不存在最短路径
    	    	
    	//记录路径
    	kShortestPath[0] = shortestPath;
    	//kShortestPathSupp = shortestPath;
    	
    	WeightedDirectedGraph myGraph = new WeightedDirectedGraph(nVerts,exVirNodes);
    	myGraph.CreateNodesOfDireGraph(nVerts);//创建节点        
    	//根据当前adjMat创建弧
    	for(int i = 0; i < nVerts; i++ ) {
    		for(int jj = 0; jj < nVerts; jj++ ) {
    			if(adjMat[i][jj] != INFINITY && adjMat[i][jj] >= 0) {
    				myGraph.CreateArcsOfDireGraph(i, jj, (int)adjMat[i][jj]);//创建弧
                }
    		}                
        }   
    	
    	//去掉已经映射的与t相邻的边，并计算
    	for(k=1;k<K;k++){//依次寻找K-1个路径
    		//DistanceParent[][] kShortestPath1 = new DistanceParent[K][nVerts];
    		myGraph.CreateArcsOfDireGraph(t, kShortestPath[k-1][t].parentVert, (int)INFINITY);//去掉弧
    		myGraph.CreateArcsOfDireGraph(kShortestPath[k-1][t].parentVert, t, (int)INFINITY);//去掉弧
    		myGraph.findShortestPathBy01ILP(s,t,pathLength);
    		if(myGraph.shortestPath[t].distance == INFINITY ) return k;//不存在最短路径
    		for(int i=0;i<nVerts;i++){
    			kShortestPath[k][i] = myGraph.shortestPath[i];
    		}
    		//kShortestPath[k] = myGraph.shortestPath;
    	}
    	System.out.println("-------------print the "+ k +" path");
    	for(int i=0;i<k;i++){//依次寻找K-1个路径
    		displayPaths(kShortestPath[i]);
    	}
    	return k;
    }
    //求解最短路径长度大于等于pathLeng的K最短路径
    //返回值：最短路径条数
    public int findKShortestPath(int K,int s,int t,int pathLength)
    {
    	if(K <= 0) return K;
    	int tt = t;
    	int k = 0,exNVerts = 0;//exNVerts为扩展节点数量    	
    	//DistanceParent[][]   kShortestPath = new DistanceParent[K][nVerts];
    	kShortestPath = new DistanceParent[K][nVerts];
    	//DistanceParent[][]   kShortestPathSupp = new DistanceParent[K][nVerts];
    	DistanceParent[]   kShortestPathSupp;
    	//LinkedList<DistanceParent> kShortestPathLink = new LinkedList<DistanceParent>(); 
    	findShortestPathBy01ILP(s,t,pathLength);
    	//findShortestPath(s,t,pathLength);//shortestPath[],vertexList[]表示最短路径
    	if(shortestPath[t].distance == INFINITY ) return 0;//不存在最短路径
    	    	
    	//记录路径
    	kShortestPath[0] = shortestPath;
    	kShortestPathSupp = shortestPath;
    	//kShortestPathSupp[0] = shortestPath;    	
    	//for(int i=0;i<shortestPath.length;i++){
    	//	kShortestPathLink.add(shortestPath[i]);
    	//}
    	
    	while(k < K-1){
    		//找到第一个节点开始的入度大于1的第一个节点，记为nh
    		int[] ret = new int[3];
    		int findIf = GetFirstNodeOfLargerInDegree(s,tt,ret,kShortestPathSupp);    		
    		int nh = ret[0];//找到该节点
    		int preNH = ret[1];//找到该节点的前驱结点
    		int afterNH = ret[2];//找到该节点的后续节点
    		if(findIf != -1 && nh != -1){//说明 找到了节点
    			if(extendNodeHash.containsKey(String.valueOf(nh))){	//扩展节点在节点集合中
    				nh = GetFirstNoExtNodeInShortestPath(nh,tt,kShortestPathSupp);//找到除去nh的第一个节点
    			} else {//扩展节点不在节点集合中
    				//建立扩展节点
    				int extendNode = nVerts+exNVerts;
    				exNVerts ++;
    				extendNodeHash.put(String.valueOf(nh), String.valueOf(extendNode));//增加扩展节点
    				System.out.print("("+nh+","+extendNode+")"); // B=
    				LinkedList<Arc> arcs = new LinkedList<Arc>(); 
    				arcs = GetArcNodes(nh,preNH);//得到前驱结点和弧路径集合
    				for (Arc tmp: arcs) {  
    					tmp.node2 = extendNode;//
    					System.out.println(extendArcNum+"("+tmp.node1+","+tmp.node2+","+tmp.cost+")"); // B=
    					extendArcHash.put(extendArcNum, tmp);//增加弧
    					extendArcNum++;
    				}  
    				nh = afterNH;
    			}
    		}
    		
    		//添加nh的所有后续节点（包括nh）的扩展节点
    		int j = tt;    		
        	while(j != nh && nh != -1){
        		int extendNode = nVerts+exNVerts;
        		extendNodeHash.put(String.valueOf(j), String.valueOf(extendNode));//增加扩展节点
        		System.out.print("("+j+","+extendNode+")"); // B=
        		exNVerts ++;
        		j = kShortestPathSupp[j].parentVert;
        	} 
        	int extendNode = -1;
        	if(nh != -1){
        		extendNode = nVerts+exNVerts;//扩展节点
            	System.out.print("("+nh+","+extendNode+")"); // B=
        		extendNodeHash.put(String.valueOf(nh), String.valueOf(extendNode));//增加扩展节点
        		exNVerts ++;
        	}
        	
        	
        	//分别从nh的前驱结点到其扩展节点nh‘的弧，权值不变，添加到集合中
        	j = tt;
        	while(j != nh && nh != -1){     
        		//System.out.println(extendNodeHash.get(String.valueOf(j))); 
        		String str = String.valueOf(extendNodeHash.get(String.valueOf(j)));
        		extendNode = Integer.valueOf(str);//(int)extendNodeHash.get(String.valueOf(j));//得到j的扩展节点extendNode
        		LinkedList<Arc> arcs = new LinkedList<Arc>(); 
				arcs = GetArcNodes(j,kShortestPathSupp[j].parentVert);//得到前驱结点和弧路径集合//得到j的前驱结点
				for (Arc tmp: arcs) {  
					tmp.node2 = extendNode;//
					System.out.println(extendArcNum+"("+tmp.node1+","+tmp.node2+","+tmp.cost+")"); 					
					extendArcHash.put(extendArcNum, tmp);//增加弧
					extendArcNum++;	
					
					//如果tmp.node1有扩展节点，则增加扩展节点与该节点的弧
					String str1 = String.valueOf(extendNodeHash.get(String.valueOf(tmp.node1)));
					//System.out.print(extendNode+","+str1); 
					if(str1 != "null"){
						int extExtNode = Integer.valueOf(str1);//得到j的扩展节点extendNode
						if(extExtNode != -1){
							tmp.node1 = extExtNode;
							tmp.node2 = extendNode;//
							System.out.println(extendArcNum+"("+tmp.node1+","+tmp.node2+","+tmp.cost+")"); 
							extendArcHash.put(extendArcNum, tmp);//增加弧
							extendArcNum++;
						}
					}
				}	
				//判断j的kShortestPathSupp[j].parentVert是否有扩展节点，如果有a，则增加该弧(a,extendNode,kShortestPathSupp[j].cost)
				String str1 = String.valueOf(extendNodeHash.get(String.valueOf(kShortestPathSupp[j].parentVert)));
				if(str1 != "null"){
					int extExtNode = Integer.valueOf(str1);//得到j的扩展节点extendNode
					if(extExtNode != -1 && extendNode != -1){
						Arc tmp = new Arc();
						tmp.node1 = extExtNode;
						tmp.node2 = extendNode;//
						tmp.cost = kShortestPathSupp[j].distance - kShortestPathSupp[kShortestPathSupp[j].parentVert].distance;
						System.out.println(extendArcNum+"("+tmp.node1+","+tmp.node2+","+tmp.cost+")"); 
						extendArcHash.put(extendArcNum, tmp);//增加弧
						extendArcNum++;
					}
				}
        		j = kShortestPathSupp[j].parentVert;
        	}     
        	if(j == nh){
        		String str = String.valueOf(extendNodeHash.get(String.valueOf(j)));
        		extendNode = Integer.valueOf(str);//(int)extendNodeHash.get(String.valueOf(j));//得到j的扩展节点extendNode
        		LinkedList<Arc> arcs = new LinkedList<Arc>(); 
				arcs = GetArcNodes(j,kShortestPathSupp[j].parentVert);//得到前驱结点和弧路径集合//得到j的前驱结点
				for (Arc tmp: arcs) {  
					if(tmp.node1 != -1 && tmp.node2 != -1)
					{
						tmp.node2 = extendNode;//
						System.out.println(extendArcNum+"("+tmp.node1+","+tmp.node2+","+tmp.cost+")"); 
						extendArcHash.put(extendArcNum, tmp);//增加弧
						extendArcNum++;		
					}
					
					
					//如果tmp.node1有扩展节点，则增加扩展节点与该节点的弧
					String str1 = String.valueOf(extendNodeHash.get(String.valueOf(tmp.node1)));
					//System.out.print(extendNode+","+str1); // B=
					if(str1 != "null"){
						int extExtNode = Integer.valueOf(str1);//得到j的扩展节点extendNode
						if(extExtNode != -1 && extendNode != -1){
							tmp.node1 = extExtNode;
							tmp.node2 = extendNode;//
							System.out.println(extendArcNum+"("+tmp.node1+","+tmp.node2+","+tmp.cost+")"); 
							extendArcHash.put(extendArcNum, tmp);//增加弧
							extendArcNum++;
						}
					}
				}
				//判断j的kShortestPathSupp[j].parentVert是否有扩展节点，如果有a，则增加该弧(a,extendNode,kShortestPathSupp[j].cost)
				String str1 = String.valueOf(extendNodeHash.get(String.valueOf(kShortestPathSupp[j].parentVert)));
				if(str1 != "null"){
					int extExtNode = Integer.valueOf(str1);//得到j的扩展节点extendNode
					if(extExtNode != -1 && extendNode != -1){
						Arc tmp = new Arc();
						tmp.node1 = extExtNode;
						tmp.node2 = extendNode;//
						tmp.cost = kShortestPathSupp[j].distance - kShortestPathSupp[kShortestPathSupp[j].parentVert].distance;
						System.out.println(extendArcNum+"("+tmp.node1+","+tmp.node2+","+tmp.cost+")"); 
						extendArcHash.put(extendArcNum, tmp);//增加弧
						extendArcNum++;
					}
				}
        	}
    		
    		//构造新的图结构，求最短路径
        	int nodesSum = exNVerts + nVerts;      
        	System.out.println(nodesSum); 
        	WeightedDirectedGraph myGraph = new WeightedDirectedGraph(nodesSum);
        	myGraph.CreateNodesOfDireGraph(nodesSum);//创建节点        
        	System.out.println("-------create arc----------------"+nodesSum);
        	//根据当前adjMat创建弧
        	for(int i = 0; i < nVerts; i++ ) {
        		for(int jj = 0; jj < nVerts; jj++ ) {
        			if(adjMat[i][jj] != INFINITY && adjMat[i][jj] >= 0) {
        				myGraph.CreateArcsOfDireGraph(i, jj, (int)adjMat[i][jj]);//创建弧
        				//System.out.print("("+i+","+jj+","+adjMat[i][jj]+")");
                    }
        		}                
            }  
        	
        	//根据扩展弧创建弧
        	Enumeration enum1 = extendArcHash.elements();
        	while(enum1.hasMoreElements()){
        		Arc tmp = (Arc)enum1.nextElement();
        		myGraph.CreateArcsOfDireGraph(tmp.node1, tmp.node2, (int)tmp.cost);//创建弧
        		System.out.print("("+tmp.node1+","+tmp.node2+","+tmp.cost+")");
        	}
        	
        	System.out.println("");
        	System.out.println("-------create arc end----------------");
        	
        	myGraph.findShortestPathBy01ILP(s,t,pathLength);
			//myGraph.findShortestPath(s,t,pathLength);//找到最短路径s->tExtNode
			//记录s->tExtNode路径
			
			System.out.println("kShortestPathSupp"); 
			
			k++;//k路径增加
			kShortestPathSupp = myGraph.shortestPath;
			//kShortestPath[k] = myGraph.shortestPath;
			
			//转换扩展节点为原始节点,更新kShortestPath。
			//算法：1）找到t对应的最终扩展节点tExt，以myGraph.shortestPath的tExt更新kShortestPath[t]；
			//      2）在最短路径中找到tExt的前驱结点pareExt，找到原始的节点pareNode，以myGraph.shortestPath的pareExt更新kShortestPath[pareNode]；
			String tExtNodeStr = GetExtExtendNode(String.valueOf(tt));
			for(int i=0;i<nVerts;i++){
				kShortestPath[k][i] = new DistanceParent(-1,-1);
			}
			
			int oriTT = Integer.valueOf(GetOriNodeFromExtendNode(String.valueOf(tt)));
			kShortestPath[k][oriTT].distance = myGraph.shortestPath[Integer.valueOf(tExtNodeStr)].distance;
			String oriNode = "";
			String startNode = "";
			startNode = String.valueOf(myGraph.shortestPath[Integer.valueOf(tExtNodeStr)].parentVert);
			oriNode = GetOriNodeFromExtendNode(startNode);
			System.out.println(oriNode+"-"+startNode);
			kShortestPath[k][oriTT].parentVert = Integer.valueOf(oriNode);
			int oriIntNode = Integer.valueOf(oriNode);
			while(oriIntNode != s)
			{
				//更新distance
				kShortestPath[k][oriIntNode].distance = myGraph.shortestPath[Integer.valueOf(startNode)].distance;
								
				//更新parentVert(先得到父节点的原始节点)
				oriNode = GetOriNodeFromExtendNode(String.valueOf(myGraph.shortestPath[Integer.valueOf(startNode)].parentVert));
				kShortestPath[k][oriIntNode].parentVert = Integer.valueOf(oriNode);
				
				//得到startNode节点的parentNode
				startNode = String.valueOf(myGraph.shortestPath[Integer.valueOf(startNode)].parentVert);
				oriNode = GetOriNodeFromExtendNode(startNode);				
				oriIntNode = Integer.valueOf(oriNode);
			}
			
			tt = Integer.valueOf(GetExtExtendNode(String.valueOf(tt)));
			
			displayPaths(kShortestPath[k]);
			
			//判断路径k是否是可行路径，如果不是，则返回
			if(kShortestPath[k][oriTT].distance == INFINITY){
				k--;
				break;
			}
			
			//判断路径k与k-1是否相同，如果相同，则说明没有路径，返回
			if(JudgePathIsSame(kShortestPath[k],kShortestPath[k-1]) == true){
				k--;
				break;
			}
				
    	}
    	System.out.println("displayPaths"); 
    	//displayPaths
    	//for(int i=0;i<k;i++){
    		//displayPaths(kShortestPathSupp[i]);
    	//}
    	for(int i=0;i<k+1;i++){
    		displayPaths(kShortestPath[i]);
    	}
    	return k+1;
    }
    
    //判断两条路径是否相同，如果相同则返回true；否则，返回false。
    private boolean JudgePathIsSame(DistanceParent[] path1,DistanceParent[] path2)
    {
    	boolean same = true;
    	for(int i=0; i<nVerts; i++){
    		if(path1[i].parentVert != path2[i].parentVert){
    			same = false;
    			break;
    		}
    	}
    	return same;    	
    }
    
    //得到节点node最终扩展节点
    private String GetExtExtendNode(String node)
    {
    	String endExtNode;
    	endExtNode = node;
    	while(extendNodeHash.containsKey(endExtNode)){
    		endExtNode = String.valueOf(extendNodeHash.get(endExtNode));
    	}
    	return endExtNode;
    }
    
  //得到扩展节点node的原始节点
    private String GetOriNodeFromExtendNode(String node)
    {    	
    	String endExtNode = "";
    	boolean find = false;
    	int jj = 0;
    	if(Integer.parseInt(node) < nVerts) return node;
    	for(jj = 0; jj < nVerts; jj++ )
    	{
    		endExtNode = String.valueOf(jj);
    		while(extendNodeHash.containsKey(endExtNode)){
    			if(Integer.valueOf(endExtNode) == Integer.valueOf(node)){
    				find = true;
    				break;
    			}
        		endExtNode = String.valueOf(extendNodeHash.get(endExtNode));
        		if(Integer.valueOf(endExtNode) == Integer.valueOf(node)){
    				find = true;
    				break;
    			}
        	}
    		if(find == true) break;
    	}
    	if(find == true) endExtNode = String.valueOf(jj);
    	else System.out.println("Error!******************");
    	return endExtNode;
    }
    
    //在路径path中找到满足条件的第一个节点：其扩展节点不在N中
    private int GetFirstNoExtNodeInShortestPath(int startNode,int endNode,DistanceParent[] shortestPathSupp)
    {
    	int j = endNode;
    	int findNode = -1;
    	while(shortestPathSupp[j].parentVert != startNode){
    		//判断节点j是否在扩展节点
    		if(!extendNodeHash.containsKey(String.valueOf(j))){
    			findNode = j;
    		}
    		j = shortestPathSupp[j].parentVert;
    	} 
    	if(!extendNodeHash.containsKey(String.valueOf(j))){
			findNode = j;
		}
    	return findNode;
    }
    
    //得到节点的前驱结点集合（不包括preNH节点）
    private LinkedList<Arc> GetArcNodes(int nh,int preNH){
    	LinkedList<Arc> arcNodes = new LinkedList<Arc>();
    	if(nh < nVerts)
    	{
    		for(int j = 0; j < nVerts; j++ ) {
                if(adjMat[j][nh] != INFINITY && j!= preNH) {
                	Arc myArc = new Arc();
                	myArc.node1 = j;
                	myArc.node2 = nh;
                	myArc.cost = adjMat[j][nh];
                	arcNodes.add(myArc);
                }
            }  
    	}
    	
    	//检测扩展弧的节点前驱结点集合
    	Enumeration enum2=extendArcHash.elements();
		while(enum2.hasMoreElements()){
			Arc myArc = (Arc)(enum2.nextElement());
			if(myArc.node2 == nh && myArc.node1 != preNH) arcNodes.add(myArc);
		}
    	return arcNodes;
    }
    
    //名称：GetFirstNodeOfLargerInDegree
    //在路径中找到第一个节点开始的入度大于1的第一个节点
    //返回值：-1：说明未找到符合条件的点
    //       >-1：找到符合条件的点。
    private int GetFirstNodeOfLargerInDegree(int s,int t,int[] ret,DistanceParent[] kShortestPathSupp)
    {
    	if(s == t) return -1;
    	int j = t;
    	int firstNodeOfInDegree = -1;
    	int parentNode = -1;
    	int afterNode = -1;
    	while(j != s){    		
    		int inDegree = GetInDegree(j);//计算j的入度
    		if(inDegree > 1) {    			
    			firstNodeOfInDegree = j;
    			parentNode = kShortestPathSupp[j].parentVert;
    			ret[0] = firstNodeOfInDegree;
    			ret[1] = parentNode;
    			ret[2] = afterNode;
    		}    
    		afterNode = j;
    		j = kShortestPathSupp[j].parentVert;
    	}    	
    	return firstNodeOfInDegree;
    }
    //得到节点入度
    private int GetInDegree(int node){
    	int degree = 0;
    	if(node < nVerts)
    	{
    		for(int i=0;i<nVerts;i++){
        		if(adjMat[i][node] >= 0 && adjMat[i][node] != INFINITY) degree++;
        	}    	
    	}
    	//检测扩展弧的节点入度
    	Enumeration enum2=extendArcHash.elements();
		while(enum2.hasMoreElements()){
			Arc myArc = (Arc)(enum2.nextElement());
			if(myArc.node2 == node) degree++;
		}
    	return degree;
    }

    /**
     * 求最短路径算法：Dijkstra算法。
     * 返回值为：1，找到了最短路径:
     *          0，没有找到最短路径；
     */
    public void findShortestPath(int startTree)
    {
        //int startTree = 0;//从0节点开始

        vertexList[startTree].isInTree = true;//将该节点放入树中

        nTree = 1;
        
        //初始化最短路径表，以邻接矩阵中的 startTree 行数据初始化
        for(int i = 0; i < nVerts; i++) {
            shortestPath[i] = new DistanceParent( startTree, adjMat[startTree][i] );
        }
        
        while( nTree < nVerts )
        {
            int indexMin = getMinFromShortestPath();// 从最短路径表中得到目前的最小值

            double minDist = shortestPath[indexMin].distance;
            
            if ( minDist == INFINITY ) // 如果为 INFINITY ，表明不可达，或者都在树中了。
            { 
                //System.out.println( "There are unreachable vertices" );
                break; // sPath is complete

            } else {
                currentVertex = indexMin; // 将最小的赋值给currentVert，为即将进入树中作准备

                startToCurrentDistance = shortestPath[indexMin].distance;//路径权重最小

            }
            // 将当前节点放入树中
            vertexList[currentVertex].isInTree = true;
            nTree++;
            adjust_sPath(); // 更新最短路径表

        }//end while
        
        if(Parameters.DebugModel) displayPaths(); // display sPath[] contents
                
        nTree = 0; // clear tree

        for(int j=0; j<nVerts; j++)
        {
            vertexList[j].isInTree = false;
        }
    }
    
    /**
     * 求最短路径算法：Dijkstra算法。
     * 返回值为：1，找到了最短路径:
     *          0，没有找到最短路径；
     */
    public void findShortestPath(int startTree,int[] nodeH)
    {
        //int startTree = 0;//从0节点开始

        vertexList[startTree].isInTree = true;//将该节点放入树中

        nTree = 1;
        
        //初始化最短路径表，以邻接矩阵中的 startTree 行数据初始化
        for(int i = 0; i < nVerts; i++) {
            shortestPath[i] = new DistanceParent( startTree, adjMat[startTree][i] );
        }
        
        while( nTree < nVerts )
        {
            int indexMin = getMinFromShortestPath(nodeH);// 从最短路径表中得到目前的最小值

            double minDist = shortestPath[indexMin].distance;
            
            if ( minDist == INFINITY ) // 如果为 INFINITY ，表明不可达，或者都在树中了。
            { 
                //System.out.println( "There are unreachable vertices" );
                break; // sPath is complete

            } else {
                currentVertex = indexMin; // 将最小的赋值给currentVert，为即将进入树中作准备

                startToCurrentDistance = shortestPath[indexMin].distance;//路径权重最小

            }
            // 将当前节点放入树中
            vertexList[currentVertex].isInTree = true;
            nTree++;
            adjust_sPath(); // 更新最短路径表

        }//end while
        
        if(Parameters.DebugModel) displayPaths(); // display sPath[] contents
                
        nTree = 0; // clear tree

        for(int j=0; j<nVerts; j++)
        {
            vertexList[j].isInTree = false;
        }
    }
    //计算每条路径跳数长度
  	private int GetPathJump(DistanceParent[] shortestPath,int sNode1,int sNode2)
  	{
  		int pathLength = 0;
  		if(shortestPath[sNode2] == null) return 0;
  		while(shortestPath[sNode2].parentVert != sNode1){
  			sNode2 = shortestPath[sNode2].parentVert;
  			pathLength ++;
  		}	
  		return pathLength+1;
  	}
    /**
     * 求通过某一条链路（其中某个端点是startTree）的具有最低索引号的频谱槽的最短路径算法：增强的Dijkstra算法。
     * 参数：startTree：起始节点；
     *      slotIndex：频谱槽起始索引
     *      secondNode：与startTree紧邻的链路节点
     *      minSlotNum：连续频谱槽最小宽度
     * 算法：未完成，待补充
     * 创建人：陈晓华
     * 创建时间：2019.1.5
     * 返回值为：1，找到了最短路径:
     *          0，没有找到最短路径；
     */
    public void findShortestPathUnderConstraint(int startTree,int slotIndex,int secondNode)
    {
        //int startTree = 0;//从0节点开始

        vertexList[startTree].isInTree = true;//将该节点放入树中

        nTree = 1;
        
        //初始化最短路径表，以邻接矩阵中的 startTree 行数据初始化
        for(int i = 0; i < nVerts; i++) {
            shortestPath[i] = new DistanceParent( startTree, adjMat[startTree][i] );
        }
        
        while( nTree < nVerts )
        {
            int indexMin = getMinFromShortestPath();// 从最短路径表中得到目前的最小值

            double minDist = shortestPath[indexMin].distance;
            
            if ( minDist == INFINITY ) // 如果为 INFINITY ，表明不可达，或者都在树中了。
            { 
                System.out.println( "There are unreachable vertices" );
                break; // sPath is complete

            } else {
                currentVertex = indexMin; // 将最小的赋值给currentVert，为即将进入树中作准备

                startToCurrentDistance = shortestPath[indexMin].distance;//路径权重最小

            }
            // 将当前节点放入树中
            vertexList[currentVertex].isInTree = true;
            nTree++;
            adjust_sPath(); // 更新最短路径表

        }//end while
        
        displayPaths(); // display sPath[] contents
                
        nTree = 0; // clear tree

        for(int j=0; j<nVerts; j++)
        {
            vertexList[j].isInTree = false;
        }
    }
    
    /**
     * 求最短路径算法：增强的Dijkstra算法。
     * 参数:startTree:开始节点；
     *     endTree:结束节点；
     *     pathLength:满足路径长度大于pathLength的最短路径
     * 返回值为：1，找到了最短路径:
     *          0，没有找到最短路径；
     * 创建人：陈晓华
     * 创建时间：2017-09-19
     */
    public void findShortestPath(int startTree,int endTree,int pathLength)
    {
        //int startTree = 0;//从0节点开始

        vertexList[startTree].isInTree = true;//将该节点放入树中

        nTree = 1;
        
        //初始化最短路径表，以邻接矩阵中的 startTree 行数据初始化
        for(int i = 0; i < nVerts; i++) {
            shortestPath[i] = new DistanceParent( startTree, adjMat[startTree][i] );
            if(adjMat[startTree][i] < INFINITY) shortestPath[i].pathLength ++;
            //shortestPathLength[i] = new DistanceParent( startTree,0);//路径长度等于0，表示没有路径
        }
        
        while( nTree < nVerts )
        {
            int indexMin = getMinFromShortestPath(endTree,pathLength);// 从最短路径表中得到目前的最小值，且不在已经找到的

            double minDist = shortestPath[indexMin].distance;
            
            if ( minDist == INFINITY ) // 如果为 INFINITY ，表明不可达，或者都在树中了。
            { 
                System.out.println( "There are unreachable vertices" );
                break; // sPath is complete
            } else {
                if(indexMin == endTree){
                	if(shortestPath[currentVertex].pathLength > pathLength-1){
                		currentVertex = indexMin; // 将最小的赋值给currentVert，为即将进入树中作准备
                        startToCurrentDistance = shortestPath[indexMin].distance;//路径权重最小
                	} else {
                		
                	}
                } else {
                	currentVertex = indexMin; // 将最小的赋值给currentVert，为即将进入树中作准备
                    startToCurrentDistance = shortestPath[indexMin].distance;//路径权重最小
                }
            }
            // 将当前节点放入树中
            vertexList[currentVertex].isInTree = true;
            nTree++;
            //adjust_sPath(); // 更新最短路径表
            adjust_sPath_Length(pathLength,endTree);// 更新最短路径表

        }//end while
        
        displayPaths(); // display sPath[] contents
                
        nTree = 0; // clear tree

        for(int j=0; j<nVerts; j++)
        {
            vertexList[j].isInTree = false;
        }
    }
    
    /**
     * 求最短路径算法：跳数约束下的最短路径ILP算法。
     * 参数:startTree:开始节点；
     *     endTree:结束节点；
     *     pathLength:满足路径长度大于pathLength的最短路径
     * 返回值为：1，找到了最短路径:
     *          0，没有找到最短路径；
     * 创建人：陈晓华
     * 创建时间：2017-09-23
     */
    public void findShortestPathBy01ILP(int startTree,int endTree,int pathLength)
    {
    	//初始化最短路径表，以邻接矩阵中的 startTree 行数据初始化
        for(int i = 0; i < nVerts; i++) {
            shortestPath[i] = new DistanceParent( startTree, adjMat[startTree][i] );
        }
        
    	Tools myDowith = new Tools();
		
		String data;
		//set V:= 0 1 2 3 4 5 6;
		data = "set V:=";
	    for(int i = 0; i < nVerts; i++){		//sub.nodes	
			data += " " + i;   
		}
		data += ";\r\n";  		
		myDowith.SaveFile("glpsolRSA.dat", data, false);
		
		data = "set E:=\r\n";
	    for(int i = 0; i < nVerts; i++){		//sub.nodes	
	    	for(int j = i+1; j < nVerts; j++){
	    		if(adjMat[i][j] < INFINITY || adjMat[j][i] < INFINITY){
	    			data += i + " " + j + "\r\n";
	    			data += j + " " + i + "\r\n";
	    		}
	    	}
		}
		data += ";\r\n";  		
		myDowith.SaveFile("glpsolRSA.dat", data, true);
		
		data = "set S:=" + startTree + ";\r\n";
		data += "set T:=" + endTree + ";\r\n";
		myDowith.SaveFile("glpsolRSA.dat", data, true);
		
		data = "set NoST:=";
	    for(int i = 0; i < nVerts; i++){		//sub.nodes	
	    	if(i == startTree || i == endTree) continue;
	    	data += " " + i;
		}
		data += ";\r\n";  		
		myDowith.SaveFile("glpsolRSA.dat", data, true);
		
		data = "param g:=\r\n";
	    for(int i = 0; i < nVerts; i++){		//sub.nodes	
	    	for(int j = i+1; j < nVerts; j++){
	    		if(adjMat[i][j] < INFINITY || adjMat[j][i] < INFINITY) {
	    			if(adjMat[i][j] < INFINITY){
	    				data += i + " " + j + " " + adjMat[i][j] + "\r\n"; 
	    				data += j + " " + i + " " + adjMat[i][j] + "\r\n"; 
	    			} else {
	    				data += i + " " + j + " " + adjMat[j][i] + "\r\n"; 
	    				data += j + " " + i + " " + adjMat[j][i] + "\r\n";
	    			}
	    		}
	    	}
		}
	    data += ";\r\n";  		
		myDowith.SaveFile("glpsolRSA.dat", data, true);
		//计算链路数量linkSum
		int linkSum=0;
		for(int i = 0; i < exVirNodes; i++){		//sub.nodes	
	    	for(int j = i+1; j < exVirNodes; j++){
	    		if(adjMat[i][j] < INFINITY) {
	    			linkSum++; 
	    		}
	    	}
		}
		//构造linkSum条链路
		LinkStruct[] links = new LinkStruct[linkSum];
		int linkNum=0;
		for(int i = 0; i < exVirNodes; i++){		//sub.nodes	
	    	for(int j = i+1; j < exVirNodes; j++){
	    		if(adjMat[i][j] < INFINITY) {
	    			links[linkNum] = new LinkStruct();
	    			links[linkNum].from = i;
	    			links[linkNum].to = j;
	    			linkNum++;
	    		}
	    	}
		}
		//计算有效性
		int threeSum = 0;
		for(int i=0;i<linkSum;i++){
			for(int j=i+1;j<linkSum;j++){
				for(int k=j+1;k<linkSum;k++){
					threeSum++;
				}
			}
		}
		int[] effCirclePath = new int[threeSum];
		int threeNum = 0;
		for(int i=0;i<linkSum;i++){
			for(int j=i+1;j<linkSum;j++){
				for(int k=j+1;k<linkSum;k++){
					if(CalculateNodesSum(links,i,j,k)<=3){
						//System.out.println("Links(nodes<=3):"+i+"-"+j+"-"+k);
						effCirclePath[threeNum] = 1;//有效
					} else {
						effCirclePath[threeNum] = 0;//无效
					}
					threeNum++;
				}
			}
		}
		//计算三条链路的数量，并设置ValueSet；
		threeNum = 0;
		//设置param ValueSet:=
		//2 3
		//3 3;3333333333333333333333333333333
		data = "param ValueSet:=\r\n";
		for(int i=0;i<linkSum;i++){
			for(int j=i+1;j<linkSum;j++){
				for(int k=j+1;k<linkSum;k++){
					if(effCirclePath[threeNum]==1)
						data += threeNum + " 3" + "\r\n";
					threeNum++;
				}
			}
		}
		data += ";\r\n";  		
		myDowith.SaveFile("glpsolRSA.dat", data, true);
		
		//set SSet:=2 3;
		data = "set SSet:=";
		for(int i=0;i<threeSum;i++){
			if(effCirclePath[i]==1)
				data += " " + i;
		}
		data += ";\r\n";  		
		myDowith.SaveFile("glpsolRSA.dat", data, true);
		//设置三条链路的VSet
		//for(int ii=0;ii<threeSum;){
			//data = "set VSet["+ii+"]:=\r\n";
		int ii = 0;
		for(int i=0;i<linkSum;i++){
			for(int j=i+1;j<linkSum;j++){
				for(int k=j+1;k<linkSum;k++){
					if(effCirclePath[ii]==1){
						data = "set VSet["+ii+"]:=\r\n";
						data += links[i].from + " " + links[i].to + "\r\n";
						data += links[i].to + " " + links[i].from + "\r\n";
						data += links[j].from + " " + links[j].to + "\r\n";
						data += links[j].to + " " + links[j].from + "\r\n";
						data += links[k].from + " " + links[k].to + "\r\n";
						data += links[k].to + " " + links[k].from + "\r\n";
						data += ";\r\n";  		
						myDowith.SaveFile("glpsolRSA.dat", data, true);
					}
					//System.out.println(ii);
					ii++;
				}
			}
		}	
		//}
		
		data = "end;\r\n"; 
		myDowith.SaveFile("glpsolRSA.dat", data, true);

		System.out.println("Get the datas of the shortest path is done.");
		
		try {
			String s;
			//Process process = Runtime.getRuntime().exec("cmd /c E:/网络虚拟化/开源Java频谱分配/winglpk-4.60/glpk-4.60/w64/glpsol.exe -m E:/网络虚拟化/开源Java频谱分配/winglpk-4.60/glpk-4.60/w64/glpsolRSA.mod -d E:/网络虚拟化/开源Java频谱分配/winglpk-4.60/glpk-4.60/w64/glpsolRSA.dat -o glpsolRSA.o");
			Process process = Runtime.getRuntime().exec("cmd /c D:/360Downloads/ylc/相关材料/相关材料/VNE_GHG_4/glpk-4.60/w64/glpsol.exe -m D:/360Downloads/ylc/相关材料/相关材料/VNE_GHG_4/glpk-4.60/w64/glpsol01ILShortestPNoDataCXH.mod -d glpsolRSA.dat -o glpsolRSA.o");
			BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(process.getInputStream()));
			while((s=bufferedReader.readLine()) != null)
				System.out.println(s);
			process.waitFor();
			System.out.println("It has done the exec.");	
			
			GetShortestPath(startTree,endTree);
	        displayPaths(); // display sPath[] contents
		} 
		catch (Exception ex) {
			JOptionPane.showMessageDialog(null, ex.getStackTrace());
		}		
        
		
                
        
    }
    /*
     * 计算三条链路的节点数量
     */
    private int CalculateNodesSum(LinkStruct[] links,int i,int j,int k)
    {
    	int sum = 6;
    	int[] num = new int[6];
    	num[0] = links[i].from;
    	num[1] = links[i].to;
    	num[2] = links[j].from;
    	num[3] = links[j].to;
    	num[4] = links[k].from;
    	num[5] = links[k].to;
    	int size = 6;
    	for(int ii=0;ii<size;ii++){
    		for(int jj=ii+1;jj<size;jj++){
    			if(num[jj] == num[ii]){//移动数据
    				for(int kk=jj+1;kk<size;kk++){
    					num[kk-1] = num[kk];
    				}
    				size--;
    			}
    		}
    	}
    	
    	return size;
    }
    /*
     * 功能：判断是否是哪几个节点
     * 返回值：-1：失败返回；>=0返回的几个节点；
     */
    private int JudgeNodeNum(int setNum[],int setSumIndex)
    {
    	int indexSum = 0;
    	for(int i=0;i<setNum.length;i++){
    		indexSum += setNum[i];
    		if(indexSum <= setSumIndex) return i;
    	}
    	return -1;
    }
    /*
     * 功能：求解C(m,n)
     * 创建者：陈晓华
     * 创建时间：2017-10-6
     */
    private int CalculateCmn(int m,int n)
    {
    	int Cmn = 0;
    	Cmn = CalP(m)/(CalP(n)*CalP(m-n));
    	return Cmn;
    }
    /*
     * 功能：求解阶乘P(m,n)
     * 返回值：-1：失败；>0：成功返回阶乘
     * 创建者：陈晓华
     * 创建时间：2017-10-6
     */
    private int CalP(int m)
    {
    	if(m<0) return -1;
    	if(m==0 || m==1) return 1;
    	int p = 1;
    	for(int i=1;i<=m;i++) p*=i;
    	return p;
    }
    /**
     * 显示最短路径
     */
    //public boolean GetShortestPath()
    public boolean GetShortestPath(int startTree,int endTree)
    {
    	BufferedReader reader = null;
		//int minLength = 0;
		//Hashtable hashResolve = new Hashtable(nVerts,(float)1.0);
		try {
	            System.out.println("以行为单位读取文件内容，一次读一整行：");
	            reader = new BufferedReader(new FileReader("glpsolRSA.o"));
	            String tempString = null;
	            int line = 1;
	            //一次读入一行，直到读入null为文件结束
	            while ((tempString = reader.readLine()) != null) {
	                //显示行号 //
	            	//System.out.println("line " + line + ": " + tempString);
	                if (line == 5 && tempString.indexOf("OPTIMAL") == -1) {  //说明未找到最优解 
	                	System.out.println("line " + line + ": " + tempString + "No Found the optimal resolvetion.");
	                	return false;
	                } 
	                if (line == 6) {  //找到最短路径长度minLength
	                	//去掉前后空格。去掉前面："Objective:  shPath = ";去掉后面："(MINimum)"
	                	tempString = tempString.replace("Objective:  shPath = ", "");
	                	tempString = tempString.replace("(MINimum)", "");
	                	tempString = tempString.trim();
	                	//minLength = Integer.parseInt(tempString);
	                	//hashResolve = new Hashtable(minLength,(float)1.0);//生成hash
	                }
	                if(line > 6 && tempString.indexOf(" x[") != -1){//说明找到了最优解的x的行
	                	//去掉前面的部分：3 x[0,2]       *              1             0             1 
	                	//以空格分隔，取第一个数据
	                	String tmpStr = "";
	                	//System.out.println("line " + line + ": " + tempString);
	                	
	                	tmpStr = tempString.substring(tempString.indexOf("*")+1);
	                	tmpStr = tmpStr.trim();
	                	//System.out.println("line " + line + ": " + tmpStr);
	                	
	                	tmpStr = tmpStr.substring(0,tmpStr.indexOf(" "));
	                	//System.out.println("line " + line + ": " + tmpStr);
	                	if(Integer.parseInt(tmpStr) == 1){//说明找到了一个解
	                		//得到一个解赋值给tmpStr，例如x[0,2]
	                		tempString = tempString.trim();
	                		tmpStr = tempString.substring(tempString.indexOf(" ")+1);
	                		System.out.println("line " + line + ": " + tmpStr);
	                		tmpStr = tmpStr.substring(0,tmpStr.indexOf(" "));
	                		System.out.println("line " + line + ": " + tmpStr);
	                		int keyNode1 = -1,keyNode2 = -1;
	                		keyNode1 = Integer.parseInt(tmpStr.substring(tmpStr.indexOf("[")+1, tmpStr.indexOf(",")));
	                		System.out.println("keyNode1:"+keyNode1);
	                		keyNode2 = Integer.parseInt(tmpStr.substring(tmpStr.indexOf(",")+1, tmpStr.indexOf("]")));
	                		System.out.println("keyNode2:"+keyNode2);
	                		
	                		//minLength ++；
	                		//hashResolve = new Hashtable(minLength,(float)1.0);
	                		
	                		//hashResolve.put(keyNode2,keyNode1);//解保存在hash表中
	                		shortestPath[keyNode2].parentVert = keyNode1;
	                		shortestPath[keyNode2].distance = adjMat[keyNode1][keyNode2];
	                	}
	                }
	                line++;
	            }
	            reader.close();
	            System.out.println("test 2019.5.1.");
	            //更新长度
	            for(int j=0; j<nVerts; j++){
	            	if(j != startTree){
	            		shortestPath[j].distance = GetDistance(startTree,j);
	            	}
	            }
	            
	            
	            return true;
	            
	     } catch (IOException e) {
	            e.printStackTrace();
	     } finally {
	            if (reader != null) {
	                try {
	                    reader.close();
	                } catch (IOException e1) {
	                }
	            }
	     }  
        return false;
    }
    /**
     * 计算节点node1->node2的最短路径
     */
    private double GetDistance(int node1,int node2)
    {
    	double dis = 0;
    	int node = shortestPath[node2].parentVert;
    	int nodeSum = 0;
    	int node3 = node2;
    	while(node != node1 && nodeSum < shortestPath.length){
    		System.out.println("node:"+node+" node1:"+node1+" node2:"+node2);
    		dis += adjMat[node2][node];
    		node2 = node;
    		node = shortestPath[node2].parentVert;
    		nodeSum++;
    	}
    	dis += adjMat[node2][node];
    	
    	if(nodeSum == shortestPath.length){//说明路径错误
    		dis = 0;
    		nodeSum = 0;
    		node2 = node3;
    		node = shortestPath[node1].parentVert;
    		while(node != node2 && nodeSum < shortestPath.length){
        		System.out.println("node:"+node+" node1:"+node1+" node2:"+node2);
        		dis += adjMat[node1][node];
        		node1 = node;
        		node = shortestPath[node1].parentVert;
        		nodeSum++;
        	}
    	}
    	if(nodeSum == shortestPath.length){//说明路径错误
    		System.out.println("error***********************************************");
    		return -1;
    	}
    	return dis;
    }
    /**
     * 显示最短路径
     */
    public void displayPaths()
    {
        for(int j = 0; j < nVerts; j++ ) // display contents of sPath[]
        {
            System.out.print( vertexList[j].label + "=" ); // B=
            
            if ( shortestPath[j].distance == INFINITY ) {
                System.out.print( "inf" ); // inf
            } else {
                System.out.print( shortestPath[j].distance ); // 50
            }
            String parent = vertexList[shortestPath[j].parentVert].label;
            System.out.print( "(" + parent + ") " ); // (A)
        }
        System.out.println( "" );
    }
    
    //记录最短路径
    public void displayPaths(DistanceParent[] kShortestPath)
    {
        for(int j = 0; j < nVerts; j++ ) // display contents of sPath[]
        {
            System.out.print( vertexList[j].label + "=" ); // B=
            
            if ( kShortestPath[j].distance == INFINITY ) {
                System.out.print( "inf" ); // inf
            } else {
                System.out.print( kShortestPath[j].distance ); // 50
            }
            String parent = String.valueOf(kShortestPath[j].parentVert);//vertexList[shortestPath[j].parentVert].label;
            System.out.print( "(" + parent + ") " ); // (A)
        }
        System.out.println( "" );
    }

    /**
     * 更新最短路径表
     */
    public void adjust_sPath()
    {
        for (int column=0; column < nVerts;column++ )//跳过自身，从1开始
        {
            if ( false == vertexList[column].isInTree )//如果不在树中
            {
                // calculate distance for one sPath entry                
                double currentToFringe = adjMat[currentVertex][column];// 当前点到column的距离

                double startToFringe = startToCurrentDistance + currentToFringe;//计算起始点到column的距离
                
                // 与原来最短路径表中的权重值进行比较
                if ( startToFringe < shortestPath[column].distance ) // 如果新值更小，就更新最短路径表
                { 
                    shortestPath[column].parentVert = currentVertex;
                    shortestPath[column].distance = startToFringe;
                }//end if
            }//end if
        }//end for
    }
    
    /**
     * 更新最短路径表
     */
    public void adjust_sPath_Length(int pathLength,int endNode)
    {
    	for(int column=0; column < nVerts;column++ )//跳过自身，从1开始
        {
            if(false == vertexList[column].isInTree )//如果不在树中
            {
                // calculate distance for one sPath entry                
                double currentToFringe = adjMat[currentVertex][column];// 当前点到column的距离

                double startToFringe = startToCurrentDistance + currentToFringe;//计算起始点到column的距离
                
                // 与原来最短路径表中的权重值进行比较
                if( startToFringe < shortestPath[column].distance && column != endNode) // 如果新值更小，就更新最短路径表
                { 
                	shortestPath[column].parentVert = currentVertex;
                    shortestPath[column].distance = startToFringe;
                	shortestPath[column].pathLength = shortestPath[currentVertex].pathLength + 1;
                } else if( startToFringe < shortestPath[column].distance && column == endNode) {// 如果新值更小，就更新最短路径表 
                	if(shortestPath[currentVertex].pathLength > pathLength-1){
                		shortestPath[column].parentVert = currentVertex;
                        shortestPath[column].distance = startToFringe;
                    	shortestPath[column].pathLength = shortestPath[currentVertex].pathLength + 1;
                	} else {//小于路径长度pathLength-1,说明这条路径行不通，则什么都不要做，这是因为已经到了最后一个节点了
                		
                	}
                }//end if
            }//end if
        }//end for
    }

    /**
     * 从最短路径表中得到目前的最小值
     * @return 返回最小值的index
     */
    public int getMinFromShortestPath()
    {        
        double minDist = INFINITY; // assume minimum

        int indexMin = 0;
        for ( int j = 0; j < nVerts; j++ ) // for each vertex,
        { // if it's in tree and
            if ( !vertexList[j].isInTree && // smaller than old one
                    shortestPath[j].distance < minDist )
            {
                minDist = shortestPath[j].distance;
                indexMin = j; // update minimum

            }
        } // end for

        return indexMin;
    }
    
    /**
     * 从最短路径表中得到目前的最小值
     * @return 返回最小值的index
     */
    public int getMinFromShortestPath(int[] nodeH)
    {        
        double minDist = INFINITY; // assume minimum

        int indexMin = 0;
        for ( int j = 0; j < nVerts; j++ ) // for each vertex,
        { // if it's in tree and
            if ( !vertexList[j].isInTree && // smaller than old one
                    shortestPath[j].distance < minDist )
            {
            	if(j<(nVerts-2)/2 && nodeH[j] == 1){
            		minDist = shortestPath[j].distance;
                    indexMin = j; // update minimum
            	} else if(j>=(nVerts-2)/2&& j< nVerts-2){
            		int i=j-(nVerts-2)/2;
            		if(nodeH[i] == 1){
            			minDist = shortestPath[j].distance;
            			indexMin = j; // update minimum
            		}
            	} else {
            		minDist = shortestPath[j].distance;
                    indexMin = j; // update minimum
            	}
            }
        } // end for

        return indexMin;
    }
    
    /**
     * 从最短路径表中得到目前的最小值
     * @return 返回满足条件的最小值的index
     * 创建者:陈晓华
     * 创建时间:2017-09-19
     */
    public int getMinFromShortestPath(int endNode,int pathLength)
    {        
        double minDist = INFINITY; // assume minimum

        int indexMin = -1;
        for ( int j = 0; j < nVerts; j++ ) // for each vertex,
        { // if it's in tree and
            if ( !vertexList[j].isInTree && // smaller than old one
                    shortestPath[j].distance < minDist )
            {
            	if(j == endNode){
            		if(shortestPath[j].pathLength > pathLength-1){
            			minDist = shortestPath[j].distance;
                		indexMin = j; // update minimum
            		}
            	} else {
            		minDist = shortestPath[j].distance;
            		indexMin = j; // update minimum
            	}

            }
        } // end for

        return indexMin;
    }
    
    /**
     * 添加一条边
     * @param start 边的起点
     * @param end 边的终点
     * @param weight 边的权重
     */
    public void addEdge( int start, int end, double weight )
    {
        adjMat[start][end] = weight;    //有方向
    }
    
    /**
     * 添加一个节点
     * 
     * @param lab
     */
    public void addVertex(String lab) // argument is label
    {
    	//System.out.println(nVerts+"-"+vertexList.length);
        vertexList[nVerts++] = new Vertex(lab);
    }
    
    //创建有向图的节点
    public void CreateNodesOfDireGraph(int nodeSum)
    {
    	for(int i=0;i<nodeSum;i++){
    		addVertex(String.valueOf(i));    //增加节点
    	}
    }
    
    //创建有向图的弧
    public void CreateArcsOfDireGraph(int node1,int node2,int cost)
    {
    	addEdge(node1,node2,(int)cost);
    }
    
    //创建有向图节点
    public void CreateDireGraph(int nodeSum)
    {
    	for(int i=0;i<nodeSum;i++){
    		addVertex(String.valueOf(i));    //增加节点
    	}
    }
    
    //功能创建有向图链路
    //创建人：陈晓华
    //创建时间：2019.1.5
    public void CreateEdge(EOSubstrateNetwork sub)
    {
    	//增加边
    	for(int i=0; i<sub.links; i++){
    		addEdge(sub.link[i].from,sub.link[i].to,1);
			addEdge(sub.link[i].to,sub.link[i].from,1);
		}
    }
    
  	
  	
  	
  	/*功能：从kShortestPath得到路径p
  	 * 创建人：陈晓华
  	 * 创建时间：2019.1.6
  	*/
  	public void GetPath(int p[],int sNode1,int sNode2,DistanceParent[]  shortestPath)
  	{
  		p[sNode1] = -1;
  		p[sNode2] = shortestPath[sNode2].parentVert;
  		while(shortestPath[sNode2].parentVert != sNode1){
  			sNode2 = shortestPath[sNode2].parentVert;
  			p[sNode2] = shortestPath[sNode2].parentVert;
  		}		
  	}
  	
  	/*功能：从kShortestPath得到路径p
  	 * 创建人：陈晓华
  	 * 创建时间：2019.7.13
  	*/
  	public boolean GetPath(int p[],WeightedDirectedGraph auxGraph)
  	{
  		int sNode1,sNode2,sNode3;
  		if(auxGraph.shortestPath[auxGraph.nVerts-2].parentVert == auxGraph.nVerts-2){
  			sNode1 = auxGraph.shortestPath[auxGraph.nVerts-1].parentVert;
  			
  			if(sNode1 > auxGraph.nVerts/2-2) sNode2 = sNode1 - auxGraph.nVerts/2 + 1;
  			else sNode2 = sNode1;
  			System.out.println("shortestPath["+(auxGraph.nVerts-1)+"].parentVert"+sNode1);
  			System.out.println("sNode1:"+sNode1+" sNode2:"+sNode2);

  			while(sNode1 != auxGraph.nVerts-2){
  				sNode1 = auxGraph.shortestPath[sNode1].parentVert;
  				if(sNode1 == auxGraph.nVerts-2){
  					break;
  				}
  				if(sNode1 > auxGraph.nVerts/2-2) sNode3 = sNode1 - auxGraph.nVerts/2 + 1;
  				else sNode3 = sNode1;
  				System.out.println("sNode1:"+sNode1+" sNode2:"+sNode2 + " sNode3:"+sNode3);

  				p[sNode2] = sNode3;
  				System.out.println("p["+sNode2+"]="+sNode3);
  				sNode2 = sNode3;
  			}
  			p[sNode2] = -1;
  			System.out.println("p["+sNode2+"]="+p[sNode2]);
  		} else if(auxGraph.shortestPath[auxGraph.nVerts-1].parentVert == auxGraph.nVerts-1){
  			sNode1 = auxGraph.shortestPath[auxGraph.nVerts-2].parentVert;
  			
  			if(sNode1 > auxGraph.nVerts/2-2) sNode2 = sNode1 - auxGraph.nVerts/2 + 1;
  			else sNode2 = sNode1;
  			System.out.println("shortestPath["+(auxGraph.nVerts-2)+"].parentVert"+sNode1);
  			System.out.println("sNode1:"+sNode1+" sNode2:"+sNode2);

  			while(sNode1 != auxGraph.nVerts-1){
  				sNode1 = auxGraph.shortestPath[sNode1].parentVert;
  				if(sNode1 == auxGraph.nVerts-1){
  					break;
  				}
  				if(sNode1 > auxGraph.nVerts/2-2) sNode3 = sNode1 - auxGraph.nVerts/2 + 1;
  				else sNode3 = sNode1;
  				System.out.println("sNode1:"+sNode1+"sNode2:"+sNode2 + " sNode3:"+sNode3);
  				p[sNode2] = sNode3;
  				System.out.println("p["+sNode2+"]="+sNode3);
  				sNode2 = sNode3;
  			}
  			p[sNode2] = -1;
  			System.out.println("p["+sNode2+"]="+p[sNode2]);
  		} else {
  			System.out.println("Error in GetPath **************");
  			
  			return false;
  		}
  		return true;
  			
  	}
  	
  	public boolean GetPathCXH(int p[],WeightedDirectedGraph auxGraph)
  	{
  		int sNode1,sNode2,sNode3;
  		if(auxGraph.shortestPath[auxGraph.nVerts-2].parentVert == auxGraph.nVerts-2){
  			sNode1 = auxGraph.shortestPath[auxGraph.nVerts-1].parentVert;
  			
  			//if(sNode1 > auxGraph.nVerts/2-2) sNode2 = sNode1 - auxGraph.nVerts/2 + 1;
  			//else 
  			sNode2 = sNode1;
  			//System.out.println("shortestPath["+(auxGraph.nVerts-1)+"].parentVert"+sNode1);
  			//System.out.println("sNode1:"+sNode1+" sNode2:"+sNode2);

  			while(sNode1 != auxGraph.nVerts-2){
  				sNode1 = auxGraph.shortestPath[sNode1].parentVert;
  				if(sNode1 == auxGraph.nVerts-2){
  					break;
  				}
  				//if(sNode1 > auxGraph.nVerts/2-2) sNode3 = sNode1 - auxGraph.nVerts/2 + 1;
  				//else sNode3 = sNode1;
  				sNode3 = sNode1;
  				//System.out.println("sNode1:"+sNode1+" sNode2:"+sNode2 + " sNode3:"+sNode3);

  				p[sNode2] = sNode3;
  				//System.out.println("p["+sNode2+"]="+sNode3);
  				sNode2 = sNode3;
  			}
  			p[sNode2] = -1;
  			//System.out.println("p["+sNode2+"]="+p[sNode2]);
  		} else if(auxGraph.shortestPath[auxGraph.nVerts-1].parentVert == auxGraph.nVerts-1){
  			sNode1 = auxGraph.shortestPath[auxGraph.nVerts-2].parentVert;
  			
  			//if(sNode1 > auxGraph.nVerts/2-2) sNode2 = sNode1 - auxGraph.nVerts/2 + 1;
  			//else sNode2 = sNode1;
  			sNode2 = sNode1;
  			//System.out.println("shortestPath["+(auxGraph.nVerts-2)+"].parentVert"+sNode1);
  			//System.out.println("sNode1:"+sNode1+" sNode2:"+sNode2);

  			while(sNode1 != auxGraph.nVerts-1){
  				sNode1 = auxGraph.shortestPath[sNode1].parentVert;
  				if(sNode1 == auxGraph.nVerts-1){
  					break;
  				}
  				//if(sNode1 > auxGraph.nVerts/2-2) sNode3 = sNode1 - auxGraph.nVerts/2 + 1;
  				//else sNode3 = sNode1;
  				sNode3 = sNode1;
  				//System.out.println("sNode1:"+sNode1+"sNode2:"+sNode2 + " sNode3:"+sNode3);
  				p[sNode2] = sNode3;
  				//System.out.println("p["+sNode2+"]="+sNode3);
  				sNode2 = sNode3;
  			}
  			p[sNode2] = -1;
  			System.out.println("p["+sNode2+"]="+p[sNode2]);
  		} else {
  			System.out.println("Error in GetPath **************");
  			
  			return false;
  		}
  		return true;
  			
  	}
  	
  	/*功能：从kShortestPath得到路径p
  	 * 创建人：陈晓华
  	 * 创建时间：2019.1.6
  	*/
  	public void PrintPath(int sNode1,int sNode2,DistanceParent[]  shortestPath)
  	{
  		//p[sNode1] = -1;
  		//p[sNode2] = shortestPath[sNode2].parentVert;
  		while(shortestPath[sNode2].parentVert != sNode1){
  			System.out.print(sNode2+"->");
  			sNode2 = shortestPath[sNode2].parentVert;
  			//p[sNode2] = shortestPath[sNode2].parentVert;
  		}	
  		System.out.println(sNode2+"->"+sNode1);
  	}
    
    //创建有向图
    public void CreateDireGraph(AuxiliaryGraph auxGraph)
    {
    	for(int i=0;i<auxGraph.nodes;i++){
    		addVertex(String.valueOf(i));    //增加节点
    	}
    	for(int i=0;i<auxGraph.links;i++){
    		adjMat[auxGraph.link[i].from][auxGraph.link[i].to] = auxGraph.link[i].bw;
    		adjMat[auxGraph.link[i].to][auxGraph.link[i].from] = auxGraph.link[i].bw;
    	}
    }
    
    
}
