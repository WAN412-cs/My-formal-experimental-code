package Team.CloudStorage.EAVONE;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Date;

import javax.swing.JOptionPane;

public class VONEByKSPath extends VNE {
	
	public void VONEEmbed(String inSNFile,String inVNsFileDir,int reqsNum,int delay) throws IOException
	{
		//创建SN和VNs
		super.VONEEmbed(inSNFile, inVNsFileDir, reqsNum, delay);
		embedModelOrAlgo = Parameters.CurrentVONEMethod;//.MapVONE3ByWangY;//.MapVONETranModel;
		V2SEmbed(sub,reqs,delay);//,Parameters.MapVONETranModel
	}
	
	/*The algorithm of mapping the VNs.*/
	private void V2SEmbed(EOSubstrateNetwork sub,VONRequest reqs[],int delay) throws IOException
	{
		//embedModelOrAlgo = embedAlgorithm;//映射模型或者算法保存在embedModelOrAlgo
		int end,n,time,start,sStart;
		time = Parameters.TIME_INTERVAL;
		end = 0;
		n = reqs.length;
		System.out.println("reqs.length:"+n);
		Date startDate = new Date();//记录映射开始的时间
	    while (end < n || reqs[n-1].time+delay>time) {   //The value of n is the number of all the VNs.
	        while (end < n && reqs[end].time < time) end++; 
	        for(sStart=0;sStart<n-1 && (reqs[sStart].time+delay)<time;sStart++) ;//说明找到了当前最小的开始虚拟网络请求
	        //for(sStart=0;reqs[sStart].time<time;sStart++) ;
	        start = sStart;
	        System.out.println("sStart:" + sStart + " end:" + end);
	        
	        //Release the resources.
	        ReleaseAllResourceAmongZeroToEnd(sub,reqs,end,time);
	        
	        //Set the expire of STATE_EXPIRE.
	        SetExpireVNState(reqs,end,time,delay);
	                
	        //Allocate the resources.
	        AllocateResources(sub,reqs,start, end);	     
	        
	        time += Parameters.TIME_INTERVAL;  //时间窗下移一个单位

	    }
	    Date endDate = new Date();//记录映射开始的时间
	    long interval = (endDate.getTime() - startDate.getTime())/1000;//记录时间差（秒）

	    //记录信息，例如系统收益、接收率、收益成本比、带宽碎片（很难定义，例如可以设置为小于2个连续的空余Slots为碎片）
	    if(Parameters.DebugModel) {
	    	System.out.println("RecordResultsOfVNE.");
	    }
	    RecordResultsOfVNE(sub,reqs,interval,embedModelOrAlgo);
	    
	    if(Parameters.DebugModel) System.out.println("PrintfVNE.");

	    //if(Parameters.DebugModel) PrintNodeEmbedding(reqs);
	    //if(Parameters.DebugModel) PrintLinkEmbedding(reqs);
		//PrintVNE(sub, reqs);PrintResultOfVN(sub,reqs);
	}
	
	//
	private void AllocateResources(EOSubstrateNetwork sub,VONRequest reqs[],int start,int end) throws IOException
	{
		System.out.println("start:" + start + " end:" + end);
		for(int i=start;i<end;i++){
			if(v2s[i].map == Parameters.STATE_NEW || v2s[i].map == Parameters.STATE_MAP_NODE_FAIL || v2s[i].map == Parameters.STATE_MAP_FAIL || v2s[i].map == Parameters.STATE_MAP_Link_FAIL) {
				ArrayList<Object> list = new ArrayList<Object>();  //记录节点映射结果
				int p[][] = new int[reqs[i].links][sub.nodes];
				int ret[][] = new int[reqs[i].links][4];
				//ret[][0]:返回虚拟链路映射的物理起点；ret[][1]:返回虚拟链路映射的物理终点
				//ret[][2]:返回的起始频谱槽；ret[][3]:返回的频谱槽数量
				v2s[i].tryMapTime ++;	//记录映射次数			
				if(reqs[i].topo == Parameters.TOPO_GENERAL || reqs[i].topo == Parameters.TOPO_STAR) {
					DebugVNE(sub,reqs,i,Parameters.CurrentVONEMethod,"before embed req "+i);
					if(MapVONEByILPWangYPlus3Nodes(sub,reqs,i,ret[0],p,list)!=-1){
						if(Parameters.DebugModel) Print_sub_slots(sub);
						v2s[i].map = Parameters.STATE_MAP_SUCC;
						reqs[i].map = Parameters.STATE_MAP_SUCC;
						//DebugVNE(sub,reqs,i,Parameters.CurrentVONEMethod,"after embed succ req "+i);
					} else {
						v2s[i].map = Parameters.STATE_MAP_NODE_FAIL;
						reqs[i].map = Parameters.STATE_MAP_NODE_FAIL;
					}
				}
			}
		}
	}


	/*
	 * 采用多路径映射的ILP模型映射
	 */
	private int MapVONEByILPWangYPlus3Nodes(EOSubstrateNetwork sub,VONRequest reqs[],int index,int ret[],int p[][],ArrayList<Object> list)
	{
		//创建辅助图
		AuxiliaryGraph auxGraph = new AuxiliaryGraph();
		auxGraph = CreateAuxiliaryDiagram(sub,reqs,index);

		//建立k条路径（路径长度大于2）
		WeightedDirectedGraph myGraph = new WeightedDirectedGraph(auxGraph.nodes,sub.nodes);
		myGraph.CreateDireGraph(auxGraph);//创建节点
		//CreateEdgeFromAux(auxGraph,myGraph);


		int pathK = Parameters.K_PATH;//5;//要寻找的K最短路径
		int pathRet = -1;//记录返回的最短路径条数
		//int pathKSum = 0;
		int[] pathEff = new int[reqs[index].links];

		DistanceParent[][][]  kShortestPath = new DistanceParent[reqs[index].links][pathK][auxGraph.nodes];

		for(int i=0; i < reqs[index].links; i++)
		{
			int sNode1,sNode2;
			sNode1 = reqs[index].link[i].from + sub.nodes;
			sNode2 = reqs[index].link[i].to + sub.nodes;
			int limitPathLength = 2;
			System.out.println("link "+i+":"+sNode1+","+sNode2+"-----------");
			//pathRet = myGraph.findKShortestPath(pathK, sNode1, sNode2);//.findKShortestPath(pathK,sNode1,sNode2,limitPathLength);
			//pathRet = myGraph.findKShortestPath(pathK,sNode1,sNode2,limitPathLength);
			pathRet = myGraph.findKShortestPathByMIL(pathK,sNode1,sNode2,limitPathLength);
			pathEff[i] = pathRet;
			if(pathRet <= 0) return -1;//没有路径，返回失败
			for(int j=0;j<myGraph.kShortestPath.length;j++){
				for(int k=0;k<myGraph.kShortestPath[j].length;k++){
					kShortestPath[i][j][k] = myGraph.kShortestPath[j][k];
				}
			}
			//kShortestPath[i] = myGraph.kShortestPath;//找到了路径，并保存
			System.out.println("----------------print ret path.");
			for(int k=0;k<pathRet;k++)
				myGraph.displayPaths(myGraph.kShortestPath[k]);
		}

		//求解每条路径在每个请求的slots数量以及是否有效
		int[][] pathSlots = new int[reqs[index].links][Parameters.K_PATH];
		int[][] pathLength = new int[reqs[index].links][Parameters.K_PATH];
		int[][] pathNo = new int[reqs[index].links][Parameters.K_PATH];
		int[][] pathMD = new int[reqs[index].links][Parameters.K_PATH];
		double[][] pathLen = new double[reqs[index].links][Parameters.K_PATH];
		CalculatePathSlotsAndEffects(auxGraph,kShortestPath,pathSlots,pathLength,pathNo,pathEff,reqs,index,pathLen);
		boolean find = false;

		find = CalPathMD(pathMD,pathLen,pathEff,reqs,index);
		if(!find) return -1;//得到MD调制模式

		find = false;
		for(int i=0;i<reqs[index].links;i++){
			for(int j=0;j<Parameters.K_PATH;j++){
				if(pathNo[i][j]>-1) find = true;
			}
		}//if(pathSlots)
		if(!find) return -1;

		//以WangY的ILP模型求解，或者根据增强的基于路径的映射模型求解
		if(Parameters.CurrentVONEMethod == Parameters.MapVONEMIPTranAndPRankByCXH){
			FindVONETranPageRank(auxGraph,kShortestPath,pathSlots,pathLength,pathNo,pathEff,reqs,index);
		} else if(Parameters.CurrentVONEMethod == Parameters.MapVONETranILPByChenxh){
			FindVONETran(auxGraph,kShortestPath,pathSlots,pathLength,pathNo,pathEff,reqs,index);
		} else {
			FindVONEWangYPlusByOne01ILP(auxGraph,kShortestPath,pathSlots,pathLength,pathNo,pathEff,reqs,index);
		}

		//找最优解
		int retNodeE[],retLinkE[],retSlotSE[],retSlotEE[],retSlotBE[],retLinkMD[][];
		retNodeE = new int[reqs[index].nodes];
		retLinkE = new int[reqs[index].links];
		retLinkMD = new int[reqs[index].links][Parameters.K_PATH];
		retSlotSE = new int[reqs[index].links];
		retSlotEE = new int[reqs[index].links];
		retSlotBE = new int[reqs[index].links];

		if(FindVONEOptimalSolutionPlusWangY(auxGraph,retNodeE,retLinkE,retSlotSE,retSlotEE,retSlotBE,retLinkMD)){//找到了最优解
			//AddNodesMap(reqs,index,list);//节点映射
			//for(int i=0;i<reqs[index].links;i++){
			//	System.out.println("link "+i+" is embedded the path:"+retLinkE[i]);
			//}
			if(Parameters.DebugModel == true){
				String str = "\r\nretLinkMD[][]=\r\n";
				for(int i=0;i<reqs[index].links;i++){
					for(int j=0;j<Parameters.K_PATH;j++){
						if(retLinkMD[i][j] == 1){
							str += i+" "+j+" "+pathMD[i][j]+" "+pathSlots[i][j]+"\r\n";
						}
					}
				}
				WriteFilePlus("process.txt",str);
			}
			//检测是否与已有的频谱槽冲突，即slot已经是0，如果还要继续分配，则有问题。

			//监测共享链路的多个路径是否存在slot冲突，如果冲突，返回false；否则，返回true
			//boolean CheckPathSlotsIfEff(VONRequest reqs[],int index,DistanceParent[][][]  kShortestPath,int p[][],int virtualNodes[],int retLinkE[],int retSlotSE[],int retSlotEE[])

			if(CheckPathSlotsIfEff(reqs,index,kShortestPath,p,auxGraph.virtualNodes,retLinkE,retSlotSE,retSlotEE) == false) {
				//System.out.println("共享在某条链路中，存在频谱槽冲突");
				String data = index + " 存在频谱槽冲突\r\n";
				Tools myDowith = new Tools();
				myDowith.SaveFile("EmbedOutput.dat", data, true);
				return -1;
			}
			//System.out.println("不存在频谱槽冲突");
			PrintPath(reqs,index,kShortestPath,p,auxGraph.virtualNodes,pathEff,retLinkE,retSlotSE,retSlotEE);
			//AddNodesMap(reqs,index,retNodeE);//节点映射
			//AddLinksMapByMIPWangYPlus(sub,reqs,index,retSlotSE,retSlotEE,retLinkE,kShortestPath,pathEff,pathNo,auxGraph.virtualNodes,retNodeE);
			boolean check = true;
			EOSubstrateNetwork subCopy = new EOSubstrateNetwork();
			Clone(subCopy,sub);
			check = CheckLinksMapByMIPWangYPlus(subCopy,reqs,index,retSlotSE,retSlotEE,retLinkE,kShortestPath,pathEff,pathNo,auxGraph.virtualNodes,retNodeE);
			if(check == false) return -1;
			AddNodesMap(reqs,index,retNodeE);//节点映射
			AddLinksMapByMIPWangYPlus(sub,reqs,index,retSlotSE,retSlotEE,retLinkE,kShortestPath,pathEff,pathNo,auxGraph.virtualNodes,retNodeE);
			//更新cpu
			for(int i=0;i<reqs[index].nodes;i++){
				sub.cpu[retNodeE[i]] -= reqs[index].cpu[i];
			}
			//retNodeE[]
			//UpdateSub(sub,subCopy);
			return 0;//成功找到VONE解
		}
		return -1;
	}
	
	/*功能:求解虚拟光网络映射
	 * 参数:auxGraph：辅助图
	 * kShortestPath:每条虚拟路径最短路径
	 * pathSlots:路径上请求的slots数量
	 * pathLength:路径的长度
	 * pathNo:路径的编号
	 * int[] pathEff:有效路径数量
	 */
	private void FindVONEWangYPlusByOne01ILP(AuxiliaryGraph auxGraph,DistanceParent[][][]  kShortestPath,int[][] pathSlots,int[][] pathLength,int[][] pathNo,int[] pathEff, VONRequest reqs[],int index)
	{
		int M = auxGraph.slotsNum;
		
		Tools myDowith = new Tools();
		
		String data;
		
		data = "set MSet:=";
		for(int i = 0; i < M; i++){
			data += " " + i; 
		}
		data += ";\r\n"; 		 
		myDowith.SaveFile("glpsolRSA.dat", data, false);
		
		data = "set Path:=";
		int pathSum = 0;
		for(int j = 0; j < reqs[index].links; j ++) {
			pathSum += pathEff[j];
		}
		for(int i = 0; i < pathSum; i++){
			data += " " + i; 
		}
		data += ";\r\n"; 		 
		myDowith.SaveFile("glpsolRSA.dat", data, true);
		
		//set P[0]:=0 1 6;
		//set P[1]:=4 5;
		//set P[2]:=2 3;
		data = "";
		for(int i = 0; i < reqs[index].links; i++){
			data += "set P[" + i + "]:=";
			for(int j = 0; j < pathEff[i]; j++){
				if(pathLength[i][j] > 2 && pathNo[i][j] != -1) data += " " + pathNo[i][j];
			}
			data += ";\r\n";
		}
		myDowith.SaveFile("glpsolRSA.dat", data, true);
		
		//set Nv:=4 5 7;/*虚拟节点的集合*/
		data = "set Nv:=";
		for(int i = 0; i < reqs[index].nodes; i++){		//sub.nodes	
			data += " " + auxGraph.virtualNodes[i];   
		}
		data += ";\r\n";
		myDowith.SaveFile("glpsolRSA.dat", data, true);
		
		//set F:=0 1 2 3;/*服务器节点（facility nodes）的集合*/
		data = "set F:=";
		for(int i = 0; i < auxGraph.faNodesNum; i++){		//sub.nodes	
			data += " " + auxGraph.faNodes[i];   
		}
		data += ";\r\n";
		myDowith.SaveFile("glpsolRSA.dat", data, true);
		
		//set Na:=0 1 2 3 4 5 6 7;/*辅助图的节点集合，Na=F并Nv并网络节点*/
		data = "set Na:=";
		for(int i = 0; i < auxGraph.nodes; i++){		//sub.nodes	
			data += " " + i;   
		}
		data += ";\r\n";
		myDowith.SaveFile("glpsolRSA.dat", data, true);
		
		//set A[4]:=
		//4 0
		//4 2
		//;/*与节点（facility nodes）u的辅助边的集合*/
		for(int i = 0; i < reqs[index].nodes; i ++)
		{
			data = "set A[" + auxGraph.virtualNodes[i] + "]:=\r\n";
			for(int j = 0; j < auxGraph.virtServLinks.length; j ++)
			{
				if(auxGraph.virtServLinks[j].from == auxGraph.virtualNodes[i])
					//if(reqs[index].cpu[i] <= s2v_n[auxGraph.virtServLinks[j].to].rest_cpu)
					data += auxGraph.virtServLinks[j].from + " " + auxGraph.virtServLinks[j].to + "\r\n";
				if(auxGraph.virtServLinks[j].to == auxGraph.virtualNodes[i])
					//if(reqs[index].cpu[i] <= s2v_n[auxGraph.virtServLinks[j].from].rest_cpu)
					data += auxGraph.virtServLinks[j].to + " " + auxGraph.virtServLinks[j].from + "\r\n";
			}
			data += ";\r\n";
			myDowith.SaveFile("glpsolRSA.dat", data, true);
		}
		//set Afa[0]:=
		//4 0
		//;/*与物理节点（facility nodes）u的辅助边的集合*/
		for(int i = 0; i < auxGraph.faNodesNum; i ++)
		{
			data = "set Afa[" + auxGraph.faNodes[i] + "]:=\r\n";
			for(int j = 0; j < auxGraph.virtServLinks.length; j ++)
			{
				if(auxGraph.virtServLinks[j].to == auxGraph.faNodes[i])
					data += auxGraph.virtServLinks[j].from + " " + auxGraph.virtServLinks[j].to + "\r\n";
				if(auxGraph.virtServLinks[j].from == auxGraph.faNodes[i])
					data += auxGraph.virtServLinks[j].from + " " + auxGraph.virtServLinks[j].to + "\r\n";
			}
			data += ";\r\n";
			myDowith.SaveFile("glpsolRSA.dat", data, true);
		}
		
		//set Af[4]:=0 2;/*每个虚拟节点可映射的节点集合*/
		for(int i = 0; i < reqs[index].nodes; i ++)
		{
			data = "set Af[" + auxGraph.virtualNodes[i] + "]:=";
			for(int j = 0; j < auxGraph.virtServLinks.length; j ++)
			{
				if(auxGraph.virtServLinks[j].from == auxGraph.virtualNodes[i])
					data += " " + auxGraph.virtServLinks[j].to;
				if(auxGraph.virtServLinks[j].to == auxGraph.virtualNodes[i])
					data += " " + auxGraph.virtServLinks[j].from;
			}
			data += ";\r\n";
			myDowith.SaveFile("glpsolRSA.dat", data, true);
		}
		
		data = "set Elink:=\r\n";
		for(int i = 0; i < auxGraph.links; i++){		//sub.nodes	
			boolean find = false;
			for(int k=0;k<reqs[index].nodes;k++){
				if(auxGraph.link[i].from == auxGraph.virtualNodes[k] || auxGraph.link[i].to == auxGraph.virtualNodes[k]){
					find = true;
					break;
				}
			}
			if(find) continue;
			if(auxGraph.link[i].from == auxGraph.link[i].to) continue;
			data += " " + auxGraph.link[i].from + " " + auxGraph.link[i].to + "\r\n";   
		}
		data += ";\r\n";
		myDowith.SaveFile("glpsolRSA.dat", data, true);
		
		//set FS[0,0]:=0 1 2 3 4 5 6 7 8 9;/*路径p上可能的起始频谱槽索引集合*/
		//int pathSum = CalculatePathsSum(pathNo,reqs,index);
		
		for(int i = 0; i < reqs[index].links; i ++){
			for(int j = 0; j < pathEff[i]; j++){
			//for(int j = 0; j < pathSum; j ++)
				
				if(pathNo[i][j] == -1) {
					continue;
				}
				int preEffectPath = -1;
				
				//data = "set FS[" + i + "," + j + "]:=";
				data = "set FS[" + i + "," + pathNo[i][j] + "]:=";
				for(int k = j; k < M; k++)
				{
					int path = GetPathNoInVirtualLinkAndPath(j,pathNo,pathEff,reqs,index);
					int effectPath = EffectSlotOnPath(auxGraph,kShortestPath,pathSlots,path,k,i,reqs,index);
					if(Parameters.DebugModel) System.out.println("effectPath:"+effectPath);
					if(effectPath >= 0 && preEffectPath != effectPath){
						data += " " + effectPath;
						preEffectPath = effectPath;
					}
				}
				data += ";\r\n";
				myDowith.SaveFile("glpsolRSA.dat", data, true);
			}
		}
		
		//set Ef:=
		//4 0
		//4 2
		//5 1
		//5 3
		//7 2
		//7 3
		//;/*与服务器节点的辅助边的集合*/
		data = "set Ef:=\r\n";
		for(int i = 0; i < auxGraph.virtServLinks.length; i++){
			data += auxGraph.virtServLinks[i].from + " " + auxGraph.virtServLinks[i].to + "\r\n"; 
		}
		data += ";\r\n";
		myDowith.SaveFile("glpsolRSA.dat", data, true);

		//set D:=0 1 2;/*虚拟链路集合*/
		//set DNo[0]:=1 2;/*虚拟链路集合*/
		//set DNo[1]:=0 2;/*虚拟链路集合*/
		//set DNo[2]:=0 1;/*虚拟链路集合*/
		data = "set D:=";
		for(int i = 0; i < reqs[index].links; i++){
			data += " " + i; 
		}
		data += ";\r\n"; 
		myDowith.SaveFile("glpsolRSA.dat", data, true);
		
		data = "";
		for(int i = 0; i < reqs[index].links; i++){
			data += "set DNo[" + i + "]:=";
			for(int j = 0; j < reqs[index].links; j++){
				if(i != j) data += " " + j;
			}
			data += ";\r\n"; 
		}
		myDowith.SaveFile("glpsolRSA.dat", data, true);

		//set Du[4]:=0 2;
		//set Du[5]:=0 1;
		//set Du[7]:=1 2;
		/*Du{u in Nv};具有虚拟节点u的虚拟链路集合
		for(int i = 0; i < reqs[index].nodes; i ++)
		{
			int auxNode = auxGraph.virtualNodes[i];
			data = "set Du[" + auxNode + "]:=";
			for(int j = 0; j < pathSum; j++){
				int findPathNo = IncludeNodeInPath(auxGraph,kShortestPath,pathNo,auxNode,reqs,index);
				//int findPathNo = IncludeNodeInPath(auxGraph,kShortestPath,j,auxNode,reqs,index);
				if(findPathNo > -1 && findPathNo == j) data += " " + j; 
			}
			data += ";\r\n";
			myDowith.SaveFile("glpsolRSA.dat", data, true);
		}
		*/
		//param NSd
		//0 0 1
		//0 1 2
		//0 2 3
		//2 0 3
		//2 1 3
		//;/*虚拟网络在路径p上请求的频谱槽数量*/
		data = "param NSd:=\r\n";
		for (int i = 0; i < reqs[index].links; i++) {
			for(int j = 0; j < pathEff[i]; j++)
			{
				if(pathNo[i][j] > -1 && pathSlots[i][j] > -1) {
					data += i + " " + pathNo[i][j] + " " + pathSlots[i][j] + "\r\n";
 				}
			}
		}
		data += ";\r\n";
		myDowith.SaveFile("glpsolRSA.dat", data, true);
		
		//param H:=
		//		0 4
		//		1 4
		//		2 4
		//		3 4
		//		4 4
		//		5 4
		//		6 4
		//		;/*路径p的跳数*/
		data = "param H:=\r\n";
		for (int i = 0; i < reqs[index].links; i++) {
			for(int j = 0; j < pathEff[i]; j++)
			{
				if(pathNo[i][j] > -1) {
					data += pathNo[i][j] + " " + pathLength[i][j] + "\r\n";
 				}
			}
		}
		data += ";\r\n";	
		myDowith.SaveFile("glpsolRSA.dat", data, true);
		
		//param PNum:=
		//		4 0 2
		//		0 4 2
		//		;/*经过链路(u,v)的路径数量*/
		data = "param PNum:=\r\n";
		for (int i = 0; i < auxGraph.virtServLinks.length; i++) {
			int node1 = auxGraph.virtServLinks[i].from;
			int node2 = auxGraph.virtServLinks[i].to;
			int pathSum1 = GetPathSumPassLink(auxGraph,kShortestPath,pathNo,node1,node2,pathEff,reqs,index);	
			data += auxGraph.virtServLinks[i].from + " " + auxGraph.virtServLinks[i].to + " " + pathSum1 + "\r\n";
			//data += auxGraph.virtServLinks[i].to + " " + auxGraph.virtServLinks[i].from + " " + pathSum1 + "\r\n";
		}
		data += ";\r\n";	
		myDowith.SaveFile("glpsolRSA.dat", data, true);
		
		//param Sita:=
		//		0 4 0 1
		//		0 4 2 0
		//		1 7 2 0
		//		1 7 3 0
		/*param Sita{p in P,(u,v) in Ef}, binary;二进制变量，*/
		data = "param Sita:=\r\n";
		for (int i = 0; i < reqs[index].links; i++) {
			for(int j = 0; j < pathEff[i]; j++)
			{
				if(pathNo[i][j] > -1) {
					for(int k = 0; k < auxGraph.virtServLinks.length; k++){
						int incl = IncludeLinkInPath(auxGraph,kShortestPath,pathNo,i,j,k,reqs,index);
						if(incl > -1){//说明包含
							data += pathNo[i][j] + " " + auxGraph.virtServLinks[k].from + " " + auxGraph.virtServLinks[k].to + " 1\r\n"; 
						} else {
							data += pathNo[i][j] + " " + auxGraph.virtServLinks[k].from + " " + auxGraph.virtServLinks[k].to + " 0\r\n"; 
						}
					}
					//data += pathNo[i][j] + " " + pathLength[i][j] + "\r\n";
 				}
			}
		}
		data += ";\r\n";	
		myDowith.SaveFile("glpsolRSA.dat", data, true);
		
		/*param fs{d in D,p in P,i in MSet};路径p上的第i个起始频谱槽索引*/
		//param fs:=
		//0 0 0 0
		//0 0 1 1
		//;
		/*
		data = "param fs:=\r\n";
		for (int i = 0; i < reqs[index].links; i++) {
			for(int j = 0; j < pathEff[i]; j++)
			{
				if(pathNo[i][j] > -1) {
					for(int k = 0; k < auxGraph.slotsNum; k++){
						int effeSlot = EffectSlotOnPath(auxGraph,kShortestPath,pathSlots,j,k,i,reqs,index);
						//if(effeSlot > -1) 
						//	data += i + " " + pathNo[i][j] + " " + k + " " + 1 + "\r\n"; 
						if(effeSlot > -1) data += i + " " + pathNo[i][j] + " " + k + " " + effeSlot + "\r\n"; //" "+pathSlots[i][j]+
						//data += i + " " + pathNo[i][j] + " " + k + " " + sub.slots[i] + "\r\n"; 
						
					}
					//data += pathNo[i][j] + " " + pathLength[i][j] + "\r\n";
 				}
			}
		}
		data += ";\r\n";	
		myDowith.SaveFile("glpsolRSA.dat", data, true);
		*/
		data = "param fs:=\r\n";	
		for(int i = 0; i < reqs[index].links; i ++){
			for(int j = 0; j < pathEff[i]; j++){
				if(pathNo[i][j] == -1) {
					continue;
				}
				int preEffectPath = -1;
				//data = "set FS[" + i + "," + pathNo[i][j] + "]:=";
				for(int k = j; k < M; k++)
				{
					int path = GetPathNoInVirtualLinkAndPath(j,pathNo,pathEff,reqs,index);
					int effectPath = EffectSlotOnPath(auxGraph,kShortestPath,pathSlots,path,k,i,reqs,index);
					if(Parameters.DebugModel) System.out.println("effectPath:"+effectPath);
					if(effectPath >= 0 && preEffectPath != effectPath){
						data += i + " " + pathNo[i][j] + " " + effectPath + " " + effectPath + "\r\n";
						preEffectPath = effectPath;
					}
				}
			}		
		}
		data += ";\r\n";
		myDowith.SaveFile("glpsolRSA.dat", data, true);
		//param Degree:=
		//4 2
		//5 2
		//7 2
		//;
		//param MSlots:=9;/*最大的频谱槽索引*/
		data = "param Degree:=\r\n";
		for(int i = 0; i < reqs[index].nodes; i ++)
		{
			int auxNode = auxGraph.virtualNodes[i];
			int degree = GetDegreeOfNode(auxGraph,auxNode);
			data += auxNode + " " + degree + "\r\n";
		}
		data += ";\r\n";
		
		int MSlots = M-1;
		data += "param MSlots:=" + MSlots + ";\r\n";
		myDowith.SaveFile("glpsolRSA.dat", data, true);
		
		data = "end;\r\n"; 
		myDowith.SaveFile("glpsolRSA.dat", data, true);

		System.out.println("Done");
		
		try {
			String s;
			Process process = null;//
			//process = Runtime.getRuntime().exec("cmd /c C:/陈晓华/VONE/VONE/glpk-4.60/w64/glpsol.exe -m C:/陈晓华/VONE/VONE/glpk-4.60/w64/glpsolMILPVONE3NodesNoDataWangY.mod -d glpsolRSA.dat -o glpsolRSA.o");
			//process = Runtime.getRuntime().exec("cmd /c C:/陈晓华/VONE/VONE/glpk-4.60/w64/glpsol.exe -m C:/陈晓华/VONE/VONE/glpk-4.60/w64/glpsolMILVONE3NodesNoDataWangY.mod -d glpsolRSA.dat -o glpsolRSA.o");
			if(embedModelOrAlgo == Parameters.MapVONE3ByWangY){
				process = Runtime.getRuntime().exec("cmd /c D:/360Downloads/ylc/相关材料/相关材料/VNE_GHG_4/glpk-4.60/w64/glpsol.exe -m D:/360Downloads/ylc/相关材料/相关材料/VNE_GHG_4/glpk-4.60/w64/glpsolMILPVONE3NodesNoDataWangY.mod -d glpsolRSA.dat -o glpsolRSA.o");
			} else if(embedModelOrAlgo == Parameters.MapVONE3PByWangYAndChenxh){
				process = Runtime.getRuntime().exec("cmd /c D:/360Downloads/ylc/相关材料/相关材料/VNE_GHG_4/glpk-4.60/w64/glpsol.exe -mD:/360Downloads/ylc/相关材料/相关材料/VNE_GHG_4/glpk-4.60/w64/glpsolMILPVONE3PNodesNoDataWangY.mod -d glpsolRSA.dat -o glpsolRSA.o");
			} 
			//Process process = Runtime.getRuntime().exec("cmd /c C:/陈晓华/VONE/VONE/glpk-4.60/w64/glpsol.exe -m C:/陈晓华/VONE/VONE/glpk-4.60/w64/glpsol01ILPVONE3PNodesWangY.mod -d glpsolRSA.dat -o glpsolRSA.o");
			//Process process = Runtime.getRuntime().exec("cmd /c C:/陈晓华/VONE/VONE/glpk-4.60/w64/glpsol.exe -m C:/陈晓华/VONE/VONE/glpk-4.60/w64/glpsolMILPVONE3NodesNoDataWangY.mod -d glpsolRSA.dat -o glpsolRSA.o");
			BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(process.getInputStream()));
			while((s=bufferedReader.readLine()) != null)
				System.out.println(s);
			process.waitFor();
			System.out.println("It has done the exec.");				
		} 
		catch (Exception ex) {
			JOptionPane.showMessageDialog(null, ex.getStackTrace());
		}
		
		
	} 
	
	/*功能:求解虚拟光网络映射
	 * 参数:auxGraph：辅助图
	 * kShortestPath:每条虚拟路径最短路径
	 * pathSlots:路径上请求的slots数量
	 * pathLength:路径的长度
	 * pathNo:路径的编号
	 * int[] pathEff:有效路径数量MapVONEEnTranILPByChenxh
	 */
	private void FindVONETranPageRank(AuxiliaryGraph auxGraph,DistanceParent[][][]  kShortestPath,int[][] pathSlots,int[][] pathLength,int[][] pathNo,int[] pathEff, VONRequest reqs[],int index)
	{
		int M = auxGraph.slotsNum;
		
		Tools myDowith = new Tools();
		
		String data;
		
		data = "set MSet:=";
		for(int i = 0; i < M; i++){
			data += " " + i; 
		}
		data += ";\r\n"; 		 
		myDowith.SaveFile("glpsolRSA.dat", data, false);
		
		data = "set Path:=";
		int pathSum = 0;
		for(int j = 0; j < reqs[index].links; j ++) {
			pathSum += pathEff[j];
		}
		for(int i = 0; i < pathSum; i++){
			data += " " + i; 
		}
		data += ";\r\n"; 		 
		myDowith.SaveFile("glpsolRSA.dat", data, true);
		
		double[][] transModel = new double[reqs[index].nodes][sub.nodes];
		data = GetParaOfPageRank(sub,reqs,index,transModel);//增加transAndpagerank参数
		myDowith.SaveFile("glpsolRSA.dat", data, true);
		
		data = "param CPU:=\r\n";
		for(int i = 0; i < reqs[index].nodes; i++){		//sub.nodes	
			data += " " + auxGraph.virtualNodes[i] + " " + reqs[index].cpu[i] + "\r\n";;   
		}
		data += ";\r\n";
		myDowith.SaveFile("glpsolRSA.dat", data, true);
		
		//set P[0]:=0 1 6;
		//set P[1]:=4 5;
		//set P[2]:=2 3;
		data = "";
		for(int i = 0; i < reqs[index].links; i++){
			data += "set P[" + i + "]:=";
			for(int j = 0; j < pathEff[i]; j++){
				if(pathLength[i][j] > 2) data += " " + pathNo[i][j];
			}
			data += ";\r\n";
		}
		myDowith.SaveFile("glpsolRSA.dat", data, true);
		
		//set Nv:=4 5 7;/*虚拟节点的集合*/
		data = "set Nv:=";
		for(int i = 0; i < reqs[index].nodes; i++){		//sub.nodes	
			data += " " + auxGraph.virtualNodes[i];   
		}
		data += ";\r\n";
		myDowith.SaveFile("glpsolRSA.dat", data, true);
		
		//set F:=0 1 2 3;/*服务器节点（facility nodes）的集合*/
		data = "set F:=";
		for(int i = 0; i < auxGraph.faNodesNum; i++){		//sub.nodes	
			data += " " + auxGraph.faNodes[i];   
		}
		data += ";\r\n";
		myDowith.SaveFile("glpsolRSA.dat", data, true);
		
		//set Na:=0 1 2 3 4 5 6 7;/*辅助图的节点集合，Na=F并Nv并网络节点*/
		data = "set Na:=";
		for(int i = 0; i < auxGraph.nodes; i++){		//sub.nodes	
			data += " " + i;   
		}
		data += ";\r\n";
		myDowith.SaveFile("glpsolRSA.dat", data, true);
		
		//set A[4]:=
		//4 0
		//4 2
		//;/*与节点（facility nodes）u的辅助边的集合*/
		for(int i = 0; i < reqs[index].nodes; i ++)
		{
			data = "set A[" + auxGraph.virtualNodes[i] + "]:=\r\n";
			for(int j = 0; j < auxGraph.virtServLinks.length; j ++)
			{
				if(auxGraph.virtServLinks[j].from == auxGraph.virtualNodes[i])
					data += auxGraph.virtServLinks[j].from + " " + auxGraph.virtServLinks[j].to + "\r\n";
				if(auxGraph.virtServLinks[j].to == auxGraph.virtualNodes[i])
					data += auxGraph.virtServLinks[j].to + " " + auxGraph.virtServLinks[j].from + "\r\n";
			}
			data += ";\r\n";
			myDowith.SaveFile("glpsolRSA.dat", data, true);
		}
		//set Afa[0]:=
		//4 0
		//;/*与物理节点（facility nodes）u的辅助边的集合*/
		for(int i = 0; i < auxGraph.faNodesNum; i ++)
		{
			data = "set Afa[" + auxGraph.faNodes[i] + "]:=\r\n";
			for(int j = 0; j < auxGraph.virtServLinks.length; j ++)
			{
				if(auxGraph.virtServLinks[j].to == auxGraph.faNodes[i])
					data += auxGraph.virtServLinks[j].from + " " + auxGraph.virtServLinks[j].to + "\r\n";
				if(auxGraph.virtServLinks[j].from == auxGraph.faNodes[i])
					data += auxGraph.virtServLinks[j].from + " " + auxGraph.virtServLinks[j].to + "\r\n";
			}
			data += ";\r\n";
			myDowith.SaveFile("glpsolRSA.dat", data, true);
		}
		
		//set Af[4]:=0 2;/*每个虚拟节点可映射的节点集合*/
		for(int i = 0; i < reqs[index].nodes; i ++)
		{
			data = "set Af[" + auxGraph.virtualNodes[i] + "]:=";
			for(int j = 0; j < auxGraph.virtServLinks.length; j ++)
			{
				if(auxGraph.virtServLinks[j].from == auxGraph.virtualNodes[i])
					data += " " + auxGraph.virtServLinks[j].to;
				if(auxGraph.virtServLinks[j].to == auxGraph.virtualNodes[i])
					data += " " + auxGraph.virtServLinks[j].from;
			}
			data += ";\r\n";
			myDowith.SaveFile("glpsolRSA.dat", data, true);
		}
		
		for(int i=0;i<reqs[index].nodes;i++){
			data = "set Af1[" + auxGraph.virtualNodes[i] + "]:=";
			for(int j=0;j<sub.nodes;j++){
				//int ii= i+ sub.nodes;
				if(transModel[i][j] > -1)
					data += j + " ";
				//else data += ii + " " + j + " " + Parameters.MAX_VALUE_INT + "\r\n";
			}
			data += ";\r\n";
			myDowith.SaveFile("glpsolRSA.dat", data, true);
		}
		
		
		data = "set Elink:=\r\n";
		for(int i = 0; i < auxGraph.links; i++){		//sub.nodes	
			boolean find = false;
			for(int k=0;k<reqs[index].nodes;k++){
				if(auxGraph.link[i].from == auxGraph.virtualNodes[k] || auxGraph.link[i].to == auxGraph.virtualNodes[k]){
					find = true;
					break;
				}
			}
			if(find) continue;
			if(auxGraph.link[i].from == auxGraph.link[i].to) continue;
			data += " " + auxGraph.link[i].from + " " + auxGraph.link[i].to + "\r\n";   
		}
		data += ";\r\n";
		myDowith.SaveFile("glpsolRSA.dat", data, true);
		
		//set FS[0,0]:=0 1 2 3 4 5 6 7 8 9;/*路径p上可能的起始频谱槽索引集合*/
		//int pathSum = CalculatePathsSum(pathNo,reqs,index);
		
		for(int i = 0; i < reqs[index].links; i ++)
		{
			for(int j = 0; j < pathSum; j ++)
			{
				int preEffectPath = -1;
				data = "set FS[" + i + "," + j + "]:=";
				for(int k = j; k < M; k++)
				{
					int path = GetPathNoInVirtualLinkAndPath(j,pathNo,pathEff,reqs,index);
					int effectPath = EffectSlotOnPath(auxGraph,kShortestPath,pathSlots,path,k,i,reqs,index);
					if(effectPath >= 0 && preEffectPath != effectPath){
						data += " " + effectPath;
						preEffectPath = effectPath;
					}
				}
				data += ";\r\n";
				myDowith.SaveFile("glpsolRSA.dat", data, true);
			}
		}
		
		//set Ef:=
		//4 0
		//4 2
		//5 1
		//5 3
		//7 2
		//7 3
		//;/*与服务器节点的辅助边的集合*/
		data = "set Ef:=\r\n";
		for(int i = 0; i < auxGraph.virtServLinks.length; i++){
			data += auxGraph.virtServLinks[i].from + " " + auxGraph.virtServLinks[i].to + "\r\n"; 
		}
		data += ";\r\n";
		myDowith.SaveFile("glpsolRSA.dat", data, true);

		//set D:=0 1 2;/*虚拟链路集合*/
		//set DNo[0]:=1 2;/*虚拟链路集合*/
		//set DNo[1]:=0 2;/*虚拟链路集合*/
		//set DNo[2]:=0 1;/*虚拟链路集合*/
		data = "set D:=";
		for(int i = 0; i < reqs[index].links; i++){
			data += " " + i; 
		}
		data += ";\r\n"; 
		myDowith.SaveFile("glpsolRSA.dat", data, true);
		
		data = "";
		for(int i = 0; i < reqs[index].links; i++){
			data += "set DNo[" + i + "]:=";
			for(int j = 0; j < reqs[index].links; j++){
				if(i != j) data += " " + j;
			}
			data += ";\r\n"; 
		}
		myDowith.SaveFile("glpsolRSA.dat", data, true);

		//set Du[4]:=0 2;
		//set Du[5]:=0 1;
		//set Du[7]:=1 2;
		/*Du{u in Nv};具有虚拟节点u的虚拟链路集合
		for(int i = 0; i < reqs[index].nodes; i ++)
		{
			int auxNode = auxGraph.virtualNodes[i];
			data = "set Du[" + auxNode + "]:=";
			for(int j = 0; j < pathSum; j++){
				int findPathNo = IncludeNodeInPath(auxGraph,kShortestPath,pathNo,auxNode,reqs,index);
				//int findPathNo = IncludeNodeInPath(auxGraph,kShortestPath,j,auxNode,reqs,index);
				if(findPathNo > -1 && findPathNo == j) data += " " + j; 
			}
			data += ";\r\n";
			myDowith.SaveFile("glpsolRSA.dat", data, true);
		}
		*/
		//param NSd
		//0 0 1
		//0 1 2
		//0 2 3
		//2 0 3
		//2 1 3
		//;/*虚拟网络在路径p上请求的频谱槽数量*/
		data = "param NSd:=\r\n";
		for (int i = 0; i < reqs[index].links; i++) {
			for(int j = 0; j < pathEff[i]; j++)
			{
				if(pathNo[i][j] > -1) {
					data += i + " " + pathNo[i][j] + " " + pathSlots[i][j] + "\r\n";
 				}
			}
		}
		data += ";\r\n";
		myDowith.SaveFile("glpsolRSA.dat", data, true);
		
		//param H:=
		//		0 4
		//		1 4
		//		2 4
		//		3 4
		//		4 4
		//		5 4
		//		6 4
		//		;/*路径p的跳数*/
		data = "param H:=\r\n";
		for (int i = 0; i < reqs[index].links; i++) {
			for(int j = 0; j < pathEff[i]; j++)
			{
				if(pathNo[i][j] > -1) {
					data += pathNo[i][j] + " " + pathLength[i][j] + "\r\n";
 				}
			}
		}
		data += ";\r\n";	
		myDowith.SaveFile("glpsolRSA.dat", data, true);
		
		//param PNum:=
		//		4 0 2
		//		0 4 2
		//		;/*经过链路(u,v)的路径数量*/
		data = "param PNum:=\r\n";
		for (int i = 0; i < auxGraph.virtServLinks.length; i++) {
			int node1 = auxGraph.virtServLinks[i].from;
			int node2 = auxGraph.virtServLinks[i].to;
			int pathSum1 = GetPathSumPassLink(auxGraph,kShortestPath,pathNo,node1,node2,pathEff,reqs,index);	
			data += auxGraph.virtServLinks[i].from + " " + auxGraph.virtServLinks[i].to + " " + pathSum1 + "\r\n";
			//data += auxGraph.virtServLinks[i].to + " " + auxGraph.virtServLinks[i].from + " " + pathSum1 + "\r\n";
		}
		data += ";\r\n";	
		myDowith.SaveFile("glpsolRSA.dat", data, true);
		
		//param Sita:=
		//		0 4 0 1
		//		0 4 2 0
		//		1 7 2 0
		//		1 7 3 0
		/*param Sita{p in P,(u,v) in Ef}, binary;二进制变量，*/
		data = "param Sita:=\r\n";
		for (int i = 0; i < reqs[index].links; i++) {
			for(int j = 0; j < pathEff[i]; j++)
			{
				if(pathNo[i][j] > -1) {
					for(int k = 0; k < auxGraph.virtServLinks.length; k++){
						int incl = IncludeLinkInPath(auxGraph,kShortestPath,pathNo,i,j,k,reqs,index);
						if(incl > -1){//说明包含
							data += pathNo[i][j] + " " + auxGraph.virtServLinks[k].from + " " + auxGraph.virtServLinks[k].to + " 1\r\n"; 
						} else {
							data += pathNo[i][j] + " " + auxGraph.virtServLinks[k].from + " " + auxGraph.virtServLinks[k].to + " 0\r\n"; 
						}
					}
					//data += pathNo[i][j] + " " + pathLength[i][j] + "\r\n";
 				}
			}
		}
		data += ";\r\n";	
		myDowith.SaveFile("glpsolRSA.dat", data, true);
		
		/*param fs{d in D,p in P,i in MSet};路径p上的第i个起始频谱槽索引*/
		//param fs:=
		//0 0 0 0
		//0 0 1 1
		//;
		data = "param fs:=\r\n";
		for (int i = 0; i < reqs[index].links; i++) {
			for(int j = 0; j < pathEff[i]; j++)
			{
				if(pathNo[i][j] > -1) {
					for(int k = 0; k < auxGraph.slotsNum; k++){
						int effeSlot = EffectSlotOnPath(auxGraph,kShortestPath,pathSlots,j,k,i,reqs,index);
						if(effeSlot > -1) data += i + " " + pathNo[i][j] + " " + k + " " + effeSlot + "\r\n"; 
					}
					//data += pathNo[i][j] + " " + pathLength[i][j] + "\r\n";
 				}
			}
		}
		data += ";\r\n";	
		myDowith.SaveFile("glpsolRSA.dat", data, true);
		
		//param Degree:=
		//4 2
		//5 2
		//7 2
		//;
		//param MSlots:=9;/*最大的频谱槽索引*/
		data = "param Degree:=\r\n";
		for(int i = 0; i < reqs[index].nodes; i ++)
		{
			int auxNode = auxGraph.virtualNodes[i];
			int degree = GetDegreeOfNode(auxGraph,auxNode);
			data += auxNode + " " + degree + "\r\n";
		}
		data += ";\r\n";
		
		int MSlots = M-1;
		data += "param MSlots:=" + MSlots + ";\r\n";
		myDowith.SaveFile("glpsolRSA.dat", data, true);
		
		data = "end;\r\n"; 
		myDowith.SaveFile("glpsolRSA.dat", data, true);

		System.out.println("Done");
		
		try {
			String s;
			Process process = null;//
			//process = Runtime.getRuntime().exec("cmd /c C:/陈晓华/VONE/VONE/glpk-4.60/w64/glpsol.exe -m C:/陈晓华/VONE/VONE/glpk-4.60/w64/glpsolMILPVONE3NodesNoDataWangY.mod -d glpsolRSA.dat -o glpsolRSA.o");
			//process = Runtime.getRuntime().exec("cmd /c C:/陈晓华/VONE/VONE/glpk-4.60/w64/glpsol.exe -m C:/陈晓华/VONE/VONE/glpk-4.60/w64/glpsolMILVONE3NodesNoDataWangY.mod -d glpsolRSA.dat -o glpsolRSA.o");
			if(embedModelOrAlgo == Parameters.MapVONEMIPTranAndPRankByCXH){
				process = Runtime.getRuntime().exec("cmd /c C:/陈晓华/VONE/VONE/glpk-4.60/w64/glpsol.exe -m C:/陈晓华/VONE/VONE/glpk-4.60/w64/glpsolMILPNoDataByTranAndPageRankCXH.mod -d glpsolRSA.dat -o glpsolRSA.o");
				//process = Runtime.getRuntime().exec("cmd /c C:/陈晓华/VONE/VONE/glpk-4.60/w64/glpsol.exe -m C:/陈晓华/VONE/VONE/glpk-4.60/w64/glpsolMILPVONE3PNodesNoDataWangY.mod -d glpsolRSA.dat -o glpsolRSA.o");
				
			}
			//Process process = Runtime.getRuntime().exec("cmd /c C:/陈晓华/VONE/VONE/glpk-4.60/w64/glpsol.exe -m C:/陈晓华/VONE/VONE/glpk-4.60/w64/glpsol01ILPVONE3PNodesWangY.mod -d glpsolRSA.dat -o glpsolRSA.o");
			//Process process = Runtime.getRuntime().exec("cmd /c C:/陈晓华/VONE/VONE/glpk-4.60/w64/glpsol.exe -m C:/陈晓华/VONE/VONE/glpk-4.60/w64/glpsolMILPVONE3NodesNoDataWangY.mod -d glpsolRSA.dat -o glpsolRSA.o");
			BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(process.getInputStream()));
			while((s=bufferedReader.readLine()) != null)
				System.out.println(s);
			process.waitFor();
			System.out.println("It has done the exec.");				
		} 
		catch (Exception ex) {
			JOptionPane.showMessageDialog(null, ex.getStackTrace());
		}
	}
	
	private void FindVONETran(AuxiliaryGraph auxGraph,DistanceParent[][][]  kShortestPath,int[][] pathSlots,int[][] pathLength,int[][] pathNo,int[] pathEff, VONRequest reqs[],int index)
	{
		int M = auxGraph.slotsNum;
		
		Tools myDowith = new Tools();
		
		String data;
		
		data = "set MSet:=";
		for(int i = 0; i < M; i++){
			data += " " + i; 
		}
		data += ";\r\n"; 		 
		myDowith.SaveFile("glpsolRSA.dat", data, false);
		
		data = "set Path:=";
		int pathSum = 0;
		for(int j = 0; j < reqs[index].links; j ++) {
			pathSum += pathEff[j];
		}
		for(int i = 0; i < pathSum; i++){
			data += " " + i; 
		}
		data += ";\r\n"; 		 
		myDowith.SaveFile("glpsolRSA.dat", data, true);
		
		double[][] transModel = new double[reqs[index].nodes][sub.nodes];
		data = GetParaOfTransportation(sub,reqs,index,transModel);//增加transAndpagerank参数
		myDowith.SaveFile("glpsolRSA.dat", data, true);
		
		data = "param CPU:=\r\n";
		for(int i = 0; i < reqs[index].nodes; i++){		//sub.nodes	
			data += " " + auxGraph.virtualNodes[i] + " " + reqs[index].cpu[i] + "\r\n";;   
		}
		data += ";\r\n";
		myDowith.SaveFile("glpsolRSA.dat", data, true);
		
		//set P[0]:=0 1 6;
		//set P[1]:=4 5;
		//set P[2]:=2 3;
		data = "";
		for(int i = 0; i < reqs[index].links; i++){
			data += "set P[" + i + "]:=";
			for(int j = 0; j < pathEff[i]; j++){
				if(pathLength[i][j] > 2) data += " " + pathNo[i][j];
			}
			data += ";\r\n";
		}
		myDowith.SaveFile("glpsolRSA.dat", data, true);
		
		//set Nv:=4 5 7;/*虚拟节点的集合*/
		data = "set Nv:=";
		for(int i = 0; i < reqs[index].nodes; i++){		//sub.nodes	
			data += " " + auxGraph.virtualNodes[i];   
		}
		data += ";\r\n";
		myDowith.SaveFile("glpsolRSA.dat", data, true);
		
		//set F:=0 1 2 3;/*服务器节点（facility nodes）的集合*/
		data = "set F:=";
		for(int i = 0; i < auxGraph.faNodesNum; i++){		//sub.nodes	
			data += " " + auxGraph.faNodes[i];   
		}
		data += ";\r\n";
		myDowith.SaveFile("glpsolRSA.dat", data, true);
		
		//set Na:=0 1 2 3 4 5 6 7;/*辅助图的节点集合，Na=F并Nv并网络节点*/
		data = "set Na:=";
		for(int i = 0; i < auxGraph.nodes; i++){		//sub.nodes	
			data += " " + i;   
		}
		data += ";\r\n";
		myDowith.SaveFile("glpsolRSA.dat", data, true);
		
		//set A[4]:=
		//4 0
		//4 2
		//;/*与节点（facility nodes）u的辅助边的集合*/
		for(int i = 0; i < reqs[index].nodes; i ++)
		{
			data = "set A[" + auxGraph.virtualNodes[i] + "]:=\r\n";
			for(int j = 0; j < auxGraph.virtServLinks.length; j ++)
			{
				if(auxGraph.virtServLinks[j].from == auxGraph.virtualNodes[i])
					data += auxGraph.virtServLinks[j].from + " " + auxGraph.virtServLinks[j].to + "\r\n";
				if(auxGraph.virtServLinks[j].to == auxGraph.virtualNodes[i])
					data += auxGraph.virtServLinks[j].to + " " + auxGraph.virtServLinks[j].from + "\r\n";
			}
			data += ";\r\n";
			myDowith.SaveFile("glpsolRSA.dat", data, true);
		}
		//set Afa[0]:=
		//4 0
		//;/*与物理节点（facility nodes）u的辅助边的集合*/
		for(int i = 0; i < auxGraph.faNodesNum; i ++)
		{
			data = "set Afa[" + auxGraph.faNodes[i] + "]:=\r\n";
			for(int j = 0; j < auxGraph.virtServLinks.length; j ++)
			{
				if(auxGraph.virtServLinks[j].to == auxGraph.faNodes[i])
					data += auxGraph.virtServLinks[j].from + " " + auxGraph.virtServLinks[j].to + "\r\n";
				if(auxGraph.virtServLinks[j].from == auxGraph.faNodes[i])
					data += auxGraph.virtServLinks[j].from + " " + auxGraph.virtServLinks[j].to + "\r\n";
			}
			data += ";\r\n";
			myDowith.SaveFile("glpsolRSA.dat", data, true);
		}
		
		//set Af[4]:=0 2;/*每个虚拟节点可映射的节点集合*/
		for(int i = 0; i < reqs[index].nodes; i ++)
		{
			data = "set Af[" + auxGraph.virtualNodes[i] + "]:=";
			for(int j = 0; j < auxGraph.virtServLinks.length; j ++)
			{
				if(auxGraph.virtServLinks[j].from == auxGraph.virtualNodes[i])
					data += " " + auxGraph.virtServLinks[j].to;
				if(auxGraph.virtServLinks[j].to == auxGraph.virtualNodes[i])
					data += " " + auxGraph.virtServLinks[j].from;
			}
			data += ";\r\n";
			myDowith.SaveFile("glpsolRSA.dat", data, true);
		}
		
		for(int i=0;i<reqs[index].nodes;i++){
			data = "set Af1[" + auxGraph.virtualNodes[i] + "]:=";
			for(int j=0;j<sub.nodes;j++){
				//int ii= i+ sub.nodes;
				if(transModel[i][j] > -1)
					data += j + " ";
				//else data += ii + " " + j + " " + Parameters.MAX_VALUE_INT + "\r\n";
			}
			data += ";\r\n";
			myDowith.SaveFile("glpsolRSA.dat", data, true);
		}
		
		
		data = "set Elink:=\r\n";
		for(int i = 0; i < auxGraph.links; i++){		//sub.nodes	
			boolean find = false;
			for(int k=0;k<reqs[index].nodes;k++){
				if(auxGraph.link[i].from == auxGraph.virtualNodes[k] || auxGraph.link[i].to == auxGraph.virtualNodes[k]){
					find = true;
					break;
				}
			}
			if(find) continue;
			if(auxGraph.link[i].from == auxGraph.link[i].to) continue;
			data += " " + auxGraph.link[i].from + " " + auxGraph.link[i].to + "\r\n";   
		}
		data += ";\r\n";
		myDowith.SaveFile("glpsolRSA.dat", data, true);
		
		//set FS[0,0]:=0 1 2 3 4 5 6 7 8 9;/*路径p上可能的起始频谱槽索引集合*/
		//int pathSum = CalculatePathsSum(pathNo,reqs,index);
		
		for(int i = 0; i < reqs[index].links; i ++)
		{
			for(int j = 0; j < pathSum; j ++)
			{
				int preEffectPath = -1;
				data = "set FS[" + i + "," + j + "]:=";
				for(int k = j; k < M; k++)
				{
					int path = GetPathNoInVirtualLinkAndPath(j,pathNo,pathEff,reqs,index);
					int effectPath = EffectSlotOnPath(auxGraph,kShortestPath,pathSlots,path,k,i,reqs,index);
					if(effectPath >= 0 && preEffectPath != effectPath){
						data += " " + effectPath;
						preEffectPath = effectPath;
					}
				}
				data += ";\r\n";
				myDowith.SaveFile("glpsolRSA.dat", data, true);
			}
		}
		
		//set Ef:=
		//4 0
		//4 2
		//5 1
		//5 3
		//7 2
		//7 3
		//;/*与服务器节点的辅助边的集合*/
		data = "set Ef:=\r\n";
		for(int i = 0; i < auxGraph.virtServLinks.length; i++){
			data += auxGraph.virtServLinks[i].from + " " + auxGraph.virtServLinks[i].to + "\r\n"; 
		}
		data += ";\r\n";
		myDowith.SaveFile("glpsolRSA.dat", data, true);

		//set D:=0 1 2;/*虚拟链路集合*/
		//set DNo[0]:=1 2;/*虚拟链路集合*/
		//set DNo[1]:=0 2;/*虚拟链路集合*/
		//set DNo[2]:=0 1;/*虚拟链路集合*/
		data = "set D:=";
		for(int i = 0; i < reqs[index].links; i++){
			data += " " + i; 
		}
		data += ";\r\n"; 
		myDowith.SaveFile("glpsolRSA.dat", data, true);
		
		data = "";
		for(int i = 0; i < reqs[index].links; i++){
			data += "set DNo[" + i + "]:=";
			for(int j = 0; j < reqs[index].links; j++){
				if(i != j) data += " " + j;
			}
			data += ";\r\n"; 
		}
		myDowith.SaveFile("glpsolRSA.dat", data, true);

		//set Du[4]:=0 2;
		//set Du[5]:=0 1;
		//set Du[7]:=1 2;
		/*Du{u in Nv};具有虚拟节点u的虚拟链路集合
		for(int i = 0; i < reqs[index].nodes; i ++)
		{
			int auxNode = auxGraph.virtualNodes[i];
			data = "set Du[" + auxNode + "]:=";
			for(int j = 0; j < pathSum; j++){
				int findPathNo = IncludeNodeInPath(auxGraph,kShortestPath,pathNo,auxNode,reqs,index);
				//int findPathNo = IncludeNodeInPath(auxGraph,kShortestPath,j,auxNode,reqs,index);
				if(findPathNo > -1 && findPathNo == j) data += " " + j; 
			}
			data += ";\r\n";
			myDowith.SaveFile("glpsolRSA.dat", data, true);
		}
		*/
		//param NSd
		//0 0 1
		//0 1 2
		//0 2 3
		//2 0 3
		//2 1 3
		//;/*虚拟网络在路径p上请求的频谱槽数量*/
		data = "param NSd:=\r\n";
		for (int i = 0; i < reqs[index].links; i++) {
			for(int j = 0; j < pathEff[i]; j++)
			{
				if(pathNo[i][j] > -1) {
					data += i + " " + pathNo[i][j] + " " + pathSlots[i][j] + "\r\n";
 				}
			}
		}
		data += ";\r\n";
		myDowith.SaveFile("glpsolRSA.dat", data, true);
		
		//param H:=
		//		0 4
		//		1 4
		//		2 4
		//		3 4
		//		4 4
		//		5 4
		//		6 4
		//		;/*路径p的跳数*/
		data = "param H:=\r\n";
		for (int i = 0; i < reqs[index].links; i++) {
			for(int j = 0; j < pathEff[i]; j++)
			{
				if(pathNo[i][j] > -1) {
					data += pathNo[i][j] + " " + pathLength[i][j] + "\r\n";
 				}
			}
		}
		data += ";\r\n";	
		myDowith.SaveFile("glpsolRSA.dat", data, true);
		
		//param PNum:=
		//		4 0 2
		//		0 4 2
		//		;/*经过链路(u,v)的路径数量*/
		data = "param PNum:=\r\n";
		for (int i = 0; i < auxGraph.virtServLinks.length; i++) {
			int node1 = auxGraph.virtServLinks[i].from;
			int node2 = auxGraph.virtServLinks[i].to;
			int pathSum1 = GetPathSumPassLink(auxGraph,kShortestPath,pathNo,node1,node2,pathEff,reqs,index);	
			data += auxGraph.virtServLinks[i].from + " " + auxGraph.virtServLinks[i].to + " " + pathSum1 + "\r\n";
			//data += auxGraph.virtServLinks[i].to + " " + auxGraph.virtServLinks[i].from + " " + pathSum1 + "\r\n";
		}
		data += ";\r\n";	
		myDowith.SaveFile("glpsolRSA.dat", data, true);
		
		//param Sita:=
		//		0 4 0 1
		//		0 4 2 0
		//		1 7 2 0
		//		1 7 3 0
		/*param Sita{p in P,(u,v) in Ef}, binary;二进制变量，*/
		data = "param Sita:=\r\n";
		for (int i = 0; i < reqs[index].links; i++) {
			for(int j = 0; j < pathEff[i]; j++)
			{
				if(pathNo[i][j] > -1) {
					for(int k = 0; k < auxGraph.virtServLinks.length; k++){
						int incl = IncludeLinkInPath(auxGraph,kShortestPath,pathNo,i,j,k,reqs,index);
						if(incl > -1){//说明包含
							data += pathNo[i][j] + " " + auxGraph.virtServLinks[k].from + " " + auxGraph.virtServLinks[k].to + " 1\r\n"; 
						} else {
							data += pathNo[i][j] + " " + auxGraph.virtServLinks[k].from + " " + auxGraph.virtServLinks[k].to + " 0\r\n"; 
						}
					}
					//data += pathNo[i][j] + " " + pathLength[i][j] + "\r\n";
 				}
			}
		}
		data += ";\r\n";	
		myDowith.SaveFile("glpsolRSA.dat", data, true);
		
		/*param fs{d in D,p in P,i in MSet};路径p上的第i个起始频谱槽索引*/
		//param fs:=
		//0 0 0 0
		//0 0 1 1
		//;
		data = "param fs:=\r\n";
		for (int i = 0; i < reqs[index].links; i++) {
			for(int j = 0; j < pathEff[i]; j++)
			{
				if(pathNo[i][j] > -1) {
					for(int k = 0; k < auxGraph.slotsNum; k++){
						int effeSlot = EffectSlotOnPath(auxGraph,kShortestPath,pathSlots,j,k,i,reqs,index);
						if(effeSlot>-1) data += i + " " + pathNo[i][j] + " " + k + " " + effeSlot + "\r\n"; 
						
					}
					//data += pathNo[i][j] + " " + pathLength[i][j] + "\r\n";
 				}
			}
		}
		data += ";\r\n";	
		myDowith.SaveFile("glpsolRSA.dat", data, true);
		
		//param Degree:=
		//4 2
		//5 2
		//7 2
		//;
		//param MSlots:=9;/*最大的频谱槽索引*/
		data = "param Degree:=\r\n";
		for(int i = 0; i < reqs[index].nodes; i ++)
		{
			int auxNode = auxGraph.virtualNodes[i];
			int degree = GetDegreeOfNode(auxGraph,auxNode);
			data += auxNode + " " + degree + "\r\n";
		}
		data += ";\r\n";
		
		int MSlots = M-1;
		data += "param MSlots:=" + MSlots + ";\r\n";
		myDowith.SaveFile("glpsolRSA.dat", data, true);
		
		data = "end;\r\n"; 
		myDowith.SaveFile("glpsolRSA.dat", data, true);

		System.out.println("Done");
		
		try {
			String s;
			Process process = null;//
			//process = Runtime.getRuntime().exec("cmd /c C:/陈晓华/VONE/VONE/glpk-4.60/w64/glpsol.exe -m C:/陈晓华/VONE/VONE/glpk-4.60/w64/glpsolMILPVONE3NodesNoDataWangY.mod -d glpsolRSA.dat -o glpsolRSA.o");
			//process = Runtime.getRuntime().exec("cmd /c C:/陈晓华/VONE/VONE/glpk-4.60/w64/glpsol.exe -m C:/陈晓华/VONE/VONE/glpk-4.60/w64/glpsolMILVONE3NodesNoDataWangY.mod -d glpsolRSA.dat -o glpsolRSA.o");
			if(embedModelOrAlgo == Parameters.MapVONEMIPTranAndPRankByCXH || embedModelOrAlgo == Parameters.MapVONETranILPByChenxh){
				process = Runtime.getRuntime().exec("cmd /c C:/陈晓华/VONE/VONE/glpk-4.60/w64/glpsol.exe -m C:/陈晓华/VONE/VONE/glpk-4.60/w64/glpsolMILPNoDataByTranAndPageRankCXH.mod -d glpsolRSA.dat -o glpsolRSA.o");
				//process = Runtime.getRuntime().exec("cmd /c C:/陈晓华/VONE/VONE/glpk-4.60/w64/glpsol.exe -m C:/陈晓华/VONE/VONE/glpk-4.60/w64/glpsolMILPVONE3PNodesNoDataWangY.mod -d glpsolRSA.dat -o glpsolRSA.o");
				
			}
			//Process process = Runtime.getRuntime().exec("cmd /c C:/陈晓华/VONE/VONE/glpk-4.60/w64/glpsol.exe -m C:/陈晓华/VONE/VONE/glpk-4.60/w64/glpsol01ILPVONE3PNodesWangY.mod -d glpsolRSA.dat -o glpsolRSA.o");
			//Process process = Runtime.getRuntime().exec("cmd /c C:/陈晓华/VONE/VONE/glpk-4.60/w64/glpsol.exe -m C:/陈晓华/VONE/VONE/glpk-4.60/w64/glpsolMILPVONE3NodesNoDataWangY.mod -d glpsolRSA.dat -o glpsolRSA.o");
			BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(process.getInputStream()));
			while((s=bufferedReader.readLine()) != null)
				System.out.println(s);
			process.waitFor();
			System.out.println("It has done the exec.");				
		} 
		catch (Exception ex) {
			JOptionPane.showMessageDialog(null, ex.getStackTrace());
		}
	}
	
	/*功能:求解虚拟光网络映射
	 * 参数:auxGraph：辅助图
	 * kShortestPath:每条虚拟路径最短路径
	 * pathSlots:路径上请求的slots数量
	 * pathLength:路径的长度
	 * pathNo:路径的编号
	 * int[] pathEff:有效路径数量MapVONEEnTranILPByChenxh
	 */
	private void FindVONETranPageRank1(AuxiliaryGraph auxGraph,DistanceParent[][][]  kShortestPath,int[][] pathSlots,int[][] pathLength,int[][] pathNo,int[] pathEff, VONRequest reqs[],int index)
	{
		int M = auxGraph.slotsNum;
		
		Tools myDowith = new Tools();
		
		String data;
		
		data = "set MSet:=";
		for(int i = 0; i < M; i++){
			data += " " + i; 
		}
		data += ";\r\n"; 		 
		myDowith.SaveFile("glpsolRSA.dat", data, false);
		
		data = "set Path:=";
		int pathSum = 0;
		for(int j = 0; j < reqs[index].links; j ++) {
			pathSum += pathEff[j];
		}
		for(int i = 0; i < pathSum; i++){
			data += " " + i; 
		}
		data += ";\r\n"; 		 
		myDowith.SaveFile("glpsolRSA.dat", data, true);
		
		//set P[0]:=0 1 6;
		//set P[1]:=4 5;
		//set P[2]:=2 3;
		data = "";
		for(int i = 0; i < reqs[index].links; i++){
			data += "set P[" + i + "]:=";
			for(int j = 0; j < pathEff[i]; j++){
				if(pathLength[i][j] > 2) data += " " + pathNo[i][j];
			}
			data += ";\r\n";
		}
		myDowith.SaveFile("glpsolRSA.dat", data, true);
		
		//set Nv:=4 5 7;/*虚拟节点的集合*/
		data = "set Nv:=";
		for(int i = 0; i < reqs[index].nodes; i++){		//sub.nodes	
			data += " " + auxGraph.virtualNodes[i];   
		}
		data += ";\r\n";
		myDowith.SaveFile("glpsolRSA.dat", data, true);
		
		
		
		//set F:=0 1 2 3;/*服务器节点（facility nodes）的集合*/
		data = "set F:=";
		for(int i = 0; i < auxGraph.faNodesNum; i++){		//sub.nodes	
			data += " " + auxGraph.faNodes[i];   
		}
		data += ";\r\n";
		myDowith.SaveFile("glpsolRSA.dat", data, true);
		
		//set Na:=0 1 2 3 4 5 6 7;/*辅助图的节点集合，Na=F并Nv并网络节点*/
		data = "set Na:=";
		for(int i = 0; i < auxGraph.nodes; i++){		//sub.nodes	
			data += " " + i;   
		}
		data += ";\r\n";
		myDowith.SaveFile("glpsolRSA.dat", data, true);
		
		double[][] transModel = new double[reqs[index].nodes][sub.nodes];
		data = GetParaOfPageRank(sub,reqs,index,transModel);//增加transAndpagerank参数
		myDowith.SaveFile("glpsolRSA.dat", data, true);
		
		//set A[4]:=
		//4 0
		//4 2
		//;/*与节点（facility nodes）u的辅助边的集合*/
		for(int i = 0; i < reqs[index].nodes; i ++)
		{
			data = "set A[" + auxGraph.virtualNodes[i] + "]:=\r\n";
			for(int j = 0; j < auxGraph.virtServLinks.length; j ++)
			{
				if(auxGraph.virtServLinks[j].from == auxGraph.virtualNodes[i])
					if(transModel[i][auxGraph.virtServLinks[j].to] > -1)
						data += auxGraph.virtServLinks[j].from + " " + auxGraph.virtServLinks[j].to + "\r\n";
				if(auxGraph.virtServLinks[j].to == auxGraph.virtualNodes[i])
					if(transModel[i][auxGraph.virtServLinks[j].from] > -1)
						data += auxGraph.virtServLinks[j].to + " " + auxGraph.virtServLinks[j].from + "\r\n";
			}
			data += ";\r\n";
			myDowith.SaveFile("glpsolRSA.dat", data, true);
		}
		//set Afa[0]:=
		//4 0
		//;/*与物理节点（facility nodes）u的辅助边的集合*/
		for(int i = 0; i < auxGraph.faNodesNum; i ++)
		{
			data = "set Afa[" + auxGraph.faNodes[i] + "]:=\r\n";
			for(int j = 0; j < auxGraph.virtServLinks.length; j ++)
			{
				if(auxGraph.virtServLinks[j].to == auxGraph.faNodes[i]){
					//System.out.println(auxGraph.virtServLinks[j].from+" " +auxGraph.virtServLinks[j].to);
					//System.out.println(
					//if(transModel[i][auxGraph.virtServLinks[j].to] > -1)
						data += auxGraph.virtServLinks[j].from + " " + auxGraph.virtServLinks[j].to + "\r\n";
				}
				if(auxGraph.virtServLinks[j].from == auxGraph.faNodes[i]){
					//if(transModel[i][auxGraph.virtServLinks[j].to] > -1)
						data += auxGraph.virtServLinks[j].from + " " + auxGraph.virtServLinks[j].to + "\r\n";
				}
			}
			data += ";\r\n";
			myDowith.SaveFile("glpsolRSA.dat", data, true);
		}
		
		
		
		//set Af[4]:=0 2;/*每个虚拟节点可映射的节点集合*/
		for(int i = 0; i < reqs[index].nodes; i ++)
		{
			data = "set Af[" + auxGraph.virtualNodes[i] + "]:=";
			for(int j = 0; j < auxGraph.virtServLinks.length; j ++)
			{
				if(auxGraph.virtServLinks[j].from == auxGraph.virtualNodes[i])
					if(transModel[i][auxGraph.virtServLinks[j].to] > -1)
						if(transModel[i][auxGraph.virtServLinks[j].to] > -1)
							data += " " + auxGraph.virtServLinks[j].to;
				if(auxGraph.virtServLinks[j].to == auxGraph.virtualNodes[i])
					if(transModel[i][auxGraph.virtServLinks[j].from] > -1)
						if(transModel[i][auxGraph.virtServLinks[j].from] > -1)
							data += " " + auxGraph.virtServLinks[j].from;
			}
			data += ";\r\n";
			myDowith.SaveFile("glpsolRSA.dat", data, true);
		}
		
		data = "set Elink:=\r\n";
		for(int i = 0; i < auxGraph.links; i++){		//sub.nodes	
			boolean find = false;
			for(int k=0;k<reqs[index].nodes;k++){
				if(auxGraph.link[i].from == auxGraph.virtualNodes[k] || auxGraph.link[i].to == auxGraph.virtualNodes[k]){
					find = true;
					break;
				}
			}
			if(find) continue;
			if(auxGraph.link[i].from == auxGraph.link[i].to) continue;
			data += " " + auxGraph.link[i].from + " " + auxGraph.link[i].to + "\r\n";   
		}
		data += ";\r\n";
		myDowith.SaveFile("glpsolRSA.dat", data, true);
		
		//set FS[0,0]:=0 1 2 3 4 5 6 7 8 9;/*路径p上可能的起始频谱槽索引集合*/
		//int pathSum = CalculatePathsSum(pathNo,reqs,index);
		
		for(int i = 0; i < reqs[index].links; i ++)
		{
			for(int j = 0; j < pathSum; j ++)
			{
				int preEffectPath = -1;
				data = "set FS[" + i + "," + j + "]:=";
				for(int k = j; k < M; k++)
				{
					int path = GetPathNoInVirtualLinkAndPath(j,pathNo,pathEff,reqs,index);
					int effectPath = EffectSlotOnPath(auxGraph,kShortestPath,pathSlots,path,k,i,reqs,index);
					if(Parameters.DebugModel) System.out.println("effectPath:"+effectPath);
					if(effectPath >= 0 && preEffectPath != effectPath){
						data += " " + effectPath;
						preEffectPath = effectPath;
					}
				}
				data += ";\r\n";
				myDowith.SaveFile("glpsolRSA.dat", data, true);
			}
		}
		
		//set Ef:=
		//4 0
		//4 2
		//5 1
		//5 3
		//7 2
		//7 3
		//;/*与服务器节点的辅助边的集合*/
		data = "set Ef:=\r\n";
		for(int i = 0; i < auxGraph.virtServLinks.length; i++){
			data += auxGraph.virtServLinks[i].from + " " + auxGraph.virtServLinks[i].to + "\r\n"; 
		}
		data += ";\r\n";
		myDowith.SaveFile("glpsolRSA.dat", data, true);

		//set D:=0 1 2;/*虚拟链路集合*/
		//set DNo[0]:=1 2;/*虚拟链路集合*/
		//set DNo[1]:=0 2;/*虚拟链路集合*/
		//set DNo[2]:=0 1;/*虚拟链路集合*/
		data = "set D:=";
		for(int i = 0; i < reqs[index].links; i++){
			data += " " + i; 
		}
		data += ";\r\n"; 
		myDowith.SaveFile("glpsolRSA.dat", data, true);
		
		data = "";
		for(int i = 0; i < reqs[index].links; i++){
			data += "set DNo[" + i + "]:=";
			for(int j = 0; j < reqs[index].links; j++){
				if(i != j) data += " " + j;
			}
			data += ";\r\n"; 
		}
		myDowith.SaveFile("glpsolRSA.dat", data, true);

		//set Du[4]:=0 2;
		//set Du[5]:=0 1;
		//set Du[7]:=1 2;
		/*Du{u in Nv};具有虚拟节点u的虚拟链路集合
		for(int i = 0; i < reqs[index].nodes; i ++)
		{
			int auxNode = auxGraph.virtualNodes[i];
			data = "set Du[" + auxNode + "]:=";
			for(int j = 0; j < pathSum; j++){
				int findPathNo = IncludeNodeInPath(auxGraph,kShortestPath,pathNo,auxNode,reqs,index);
				//int findPathNo = IncludeNodeInPath(auxGraph,kShortestPath,j,auxNode,reqs,index);
				if(findPathNo > -1 && findPathNo == j) data += " " + j; 
			}
			data += ";\r\n";
			myDowith.SaveFile("glpsolRSA.dat", data, true);
		}
		*/
		//param NSd
		//0 0 1
		//0 1 2
		//0 2 3
		//2 0 3
		//2 1 3
		//;/*虚拟网络在路径p上请求的频谱槽数量*/
		data = "param NSd:=\r\n";
		for (int i = 0; i < reqs[index].links; i++) {
			for(int j = 0; j < pathEff[i]; j++)
			{
				if(pathNo[i][j] > -1) {
					data += i + " " + pathNo[i][j] + " " + pathSlots[i][j] + "\r\n";
 				}
			}
		}
		data += ";\r\n";
		myDowith.SaveFile("glpsolRSA.dat", data, true);
		
		//param H:=
		//		0 4
		//		1 4
		//		2 4
		//		3 4
		//		4 4
		//		5 4
		//		6 4
		//		;/*路径p的跳数*/
		data = "param H:=\r\n";
		for (int i = 0; i < reqs[index].links; i++) {
			for(int j = 0; j < pathEff[i]; j++)
			{
				if(pathNo[i][j] > -1) {
					data += pathNo[i][j] + " " + pathLength[i][j] + "\r\n";
 				}
			}
		}
		data += ";\r\n";	
		myDowith.SaveFile("glpsolRSA.dat", data, true);
		
		//param PNum:=
		//		4 0 2
		//		0 4 2
		//		;/*经过链路(u,v)的路径数量*/
		data = "param PNum:=\r\n";
		for (int i = 0; i < auxGraph.virtServLinks.length; i++) {
			int node1 = auxGraph.virtServLinks[i].from;
			int node2 = auxGraph.virtServLinks[i].to;
			int pathSum1 = GetPathSumPassLink(auxGraph,kShortestPath,pathNo,node1,node2,pathEff,reqs,index);	
			data += auxGraph.virtServLinks[i].from + " " + auxGraph.virtServLinks[i].to + " " + pathSum1 + "\r\n";
			//data += auxGraph.virtServLinks[i].to + " " + auxGraph.virtServLinks[i].from + " " + pathSum1 + "\r\n";
		}
		data += ";\r\n";	
		myDowith.SaveFile("glpsolRSA.dat", data, true);
		
		//param Sita:=
		//		0 4 0 1
		//		0 4 2 0
		//		1 7 2 0
		//		1 7 3 0
		/*param Sita{p in P,(u,v) in Ef}, binary;二进制变量，*/
		data = "param Sita:=\r\n";
		for (int i = 0; i < reqs[index].links; i++) {
			for(int j = 0; j < pathEff[i]; j++)
			{
				if(pathNo[i][j] > -1) {
					for(int k = 0; k < auxGraph.virtServLinks.length; k++){
						int incl = IncludeLinkInPath(auxGraph,kShortestPath,pathNo,i,j,k,reqs,index);
						if(incl > -1){//说明包含
							data += pathNo[i][j] + " " + auxGraph.virtServLinks[k].from + " " + auxGraph.virtServLinks[k].to + " 1\r\n"; 
						} else {
							data += pathNo[i][j] + " " + auxGraph.virtServLinks[k].from + " " + auxGraph.virtServLinks[k].to + " 0\r\n"; 
						}
					}
					//data += pathNo[i][j] + " " + pathLength[i][j] + "\r\n";
 				}
			}
		}
		data += ";\r\n";	
		myDowith.SaveFile("glpsolRSA.dat", data, true);
		
		/*param fs{d in D,p in P,i in MSet};路径p上的第i个起始频谱槽索引*/
		//param fs:=
		//0 0 0 0
		//0 0 1 1
		//;
		data = "param fs:=\r\n";
		for (int i = 0; i < reqs[index].links; i++) {
			for(int j = 0; j < pathEff[i]; j++)
			{
				if(pathNo[i][j] > -1) {
					for(int k = 0; k < auxGraph.slotsNum; k++){
						int effeSlot = EffectSlotOnPath(auxGraph,kShortestPath,pathSlots,j,k,i,reqs,index);
						if(Parameters.DebugModel) System.out.println("In param fs, effeSlot:"+effeSlot);
						if(effeSlot > -1 ) data += i + " " + pathNo[i][j] + " " + k + " " + effeSlot + "\r\n"; 
					}
					//data += pathNo[i][j] + " " + pathLength[i][j] + "\r\n";
 				}
			}
		}
		data += ";\r\n";	
		myDowith.SaveFile("glpsolRSA.dat", data, true);
		
		data = "param CPU:=\r\n";
		for(int i = 0; i < reqs[index].nodes; i++){		//sub.nodes	
			data += " " + auxGraph.virtualNodes[i] + " " + reqs[index].cpu[i] + "\r\n";;   
		}
		data += ";\r\n";
		myDowith.SaveFile("glpsolRSA.dat", data, true);
		
		//param Degree:=
		//4 2
		//5 2
		//7 2
		//;
		//param MSlots:=9;/*最大的频谱槽索引*/
		data = "param Degree:=\r\n";
		for(int i = 0; i < reqs[index].nodes; i ++)
		{
			int auxNode = auxGraph.virtualNodes[i];
			int degree = GetDegreeOfNode(auxGraph,auxNode);
			data += auxNode + " " + degree + "\r\n";
		}
		data += ";\r\n";
		
		
		
		int MSlots = M-1;
		data += "param MSlots:=" + MSlots + ";\r\n";
		myDowith.SaveFile("glpsolRSA.dat", data, true);
		
		data = "end;\r\n"; 
		myDowith.SaveFile("glpsolRSA.dat", data, true);

		System.out.println("Done");
		
		try {
			String s;
			Process process = null;//
			//process = Runtime.getRuntime().exec("cmd /c C:/陈晓华/VONE/VONE/glpk-4.60/w64/glpsol.exe -m C:/陈晓华/VONE/VONE/glpk-4.60/w64/glpsolMILPVONE3NodesNoDataWangY.mod -d glpsolRSA.dat -o glpsolRSA.o");
			//process = Runtime.getRuntime().exec("cmd /c C:/陈晓华/VONE/VONE/glpk-4.60/w64/glpsol.exe -m C:/陈晓华/VONE/VONE/glpk-4.60/w64/glpsolMILVONE3NodesNoDataWangY.mod -d glpsolRSA.dat -o glpsolRSA.o");
			if(embedModelOrAlgo == Parameters.MapVONEMIPTranAndPRankByCXH){
				process = Runtime.getRuntime().exec("cmd /c C:/陈晓华/VONE/VONE/glpk-4.60/w64/glpsol.exe -m C:/陈晓华/VONE/VONE/glpk-4.60/w64/glpsolMILPNoDataByTranAndPageRankCXH.mod -d glpsolRSA.dat -o glpsolRSA.o");
				//process = Runtime.getRuntime().exec("cmd /c C:/陈晓华/VONE/VONE/glpk-4.60/w64/glpsol.exe -m C:/陈晓华/VONE/VONE/glpk-4.60/w64/glpsolMILPVONE3PNodesNoDataWangY.mod -d glpsolRSA.dat -o glpsolRSA.o");
				
			}
			//Process process = Runtime.getRuntime().exec("cmd /c C:/陈晓华/VONE/VONE/glpk-4.60/w64/glpsol.exe -m C:/陈晓华/VONE/VONE/glpk-4.60/w64/glpsol01ILPVONE3PNodesWangY.mod -d glpsolRSA.dat -o glpsolRSA.o");
			//Process process = Runtime.getRuntime().exec("cmd /c C:/陈晓华/VONE/VONE/glpk-4.60/w64/glpsol.exe -m C:/陈晓华/VONE/VONE/glpk-4.60/w64/glpsolMILPVONE3NodesNoDataWangY.mod -d glpsolRSA.dat -o glpsolRSA.o");
			BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(process.getInputStream()));
			while((s=bufferedReader.readLine()) != null)
				System.out.println(s);
			process.waitFor();
			System.out.println("It has done the exec.");				
		} 
		catch (Exception ex) {
			JOptionPane.showMessageDialog(null, ex.getStackTrace());
		}
	}
	/*SavePageRank()
	 * 功能：保存物理节点和虚拟节点的pagerank
	 */
	private String GetParaOfPageRank(EOSubstrateNetwork sub,VONRequest reqs[],int index,double[][] transModel)
	{
		//创建运输模型和最小可用的频谱槽索引
		//double[][] transModel = new double[reqs[index].nodes][sub.nodes];
		int[][] indexModel = new int[reqs[index].nodes][sub.nodes];
		int[][] linkModel = new int[reqs[index].nodes][sub.nodes];
		InitTranPageRankModel(sub,reqs,index,transModel,indexModel,linkModel);
		
		String data = "param Cost:=\r\n";
		for(int i=0;i<reqs[index].nodes;i++){
			for(int j=0;j<sub.nodes;j++){
				int ii= i+ sub.nodes;
				if(transModel[i][j] > -1)
					data += ii + " " + j + " " + transModel[i][j] + "\r\n";
				//else data += ii + " " + j + " " + Parameters.MAX_VALUE_INT + "\r\n";
			}
		}
		data += ";\r\n";
		return data;
	}
	
	/*
	 * GetParaOfTransportation()
	 * 功能：保存cost[][]
	 */
	private String GetParaOfTransportation(EOSubstrateNetwork sub,VONRequest reqs[],int index,double[][] transModel)
	{
		//创建运输模型和最小可用的频谱槽索引
		//double[][] transModel = new double[reqs[index].nodes][sub.nodes];
		int[][] indexModel = new int[reqs[index].nodes][sub.nodes];
		int[][] linkModel = new int[reqs[index].nodes][sub.nodes];
		//InitTranPageRankModel(sub,reqs,index,transModel,indexModel,linkModel);
		InitTranModel(sub,reqs,index,transModel,indexModel,linkModel);
		
		String data = "param Cost:=\r\n";
		for(int i=0;i<reqs[index].nodes;i++){
			for(int j=0;j<sub.nodes;j++){
				int ii= i+ sub.nodes;
				if(transModel[i][j] > -1)
					data += ii + " " + j + " " + transModel[i][j] + "\r\n";
				//else data += ii + " " + j + " " + Parameters.MAX_VALUE_INT + "\r\n";
			}
		}
		data += ";\r\n";
		return data;
	}
	
	/******************************************************************
	名称：int InitTranModel(......)
	功能：初始化运输模型
	参数：
		      sub为物理网络
		      sNode为物理节点
		      reqs为虚网光网络
		      index为第index个虚网请求
		      transModel为返回的传输模型
	        indexModel为返回的传输模型中最小可用的频谱索引
	返回值：
	******************************************************************/
	private void InitTranPageRankModel(EOSubstrateNetwork sub,VONRequest reqs[],int index,double[][] transModel,int[][] slotIndexModel,int[][] linkModel)
	{
		//计算pagerank值
		double vNodePageRank[] = new double[reqs[index].nodes];
		double sNodePageRank[] = new double[sub.nodes];
		//	InitVNodePageRank(reqs,index);
		
		vNodePageRank=InitVNodePageRank(vNodePageRank,reqs,index);
		sNodePageRank= InitSNodePageRank(sNodePageRank, sub);
				
		//创建运输模型和最小索引和链路号
		int slotNum = -1;
		int link[] = new int[1];
		for(int i=0;i<reqs[index].nodes;i++){
			for(int j=0;j<sub.nodes;j++){
				if(reqs[index].cpu[i] <= s2v_n[j].rest_cpu + Parameters.MIN_VALUE_DOUBLE){//底层节点的CPU大于虚拟节点
					//slotNum = CheckIfSlotEnoughByNode(sub,j,reqs,index,i,link);
					slotNum = 0;
					if( slotNum > -1){//如果与底层节点j相连接的链路频谱槽大于虚拟节点i请求的槽
						transModel[i][j] = Math.abs(vNodePageRank[i]-sNodePageRank[j]);//transModel[i][j] = 1.0/s2v_n[j].rest_cpu;//div(1.0,s2v_n[j].rest_cpu,10);//1.0/(1.0*s2v_n[j].rest_cpu);
						slotIndexModel[i][j] = slotNum;
						linkModel[i][j] = link[0];
					} else {
						transModel[i][j] = -1;//-1代表不能映射
						slotIndexModel[i][j] = -1;
					}  
				} else {
					transModel[i][j] = -1;//-1代表不能映射
				}
			}
		}
	}
	/*
	 * 初始化运输模型参数
	 */
	private void InitTranModel(EOSubstrateNetwork sub,VONRequest reqs[],int index,double[][] transModel,int[][] slotIndexModel,int[][] linkModel)
	{
		//创建运输模型和最小索引和链路号
		int slotNum = -1;
		int link[] = new int[1];
		for(int i=0;i<reqs[index].nodes;i++){
			for(int j=0;j<sub.nodes;j++){
				if(reqs[index].cpu[i] <= s2v_n[j].rest_cpu + Parameters.MIN_VALUE_DOUBLE){//底层节点的CPU大于虚拟节点
					//slotNum = CheckIfSlotEnoughByNode(sub,j,reqs,index,i,link);
					slotNum = 0;
					if( slotNum > -1){//如果与底层节点j相连接的链路频谱槽大于虚拟节点i请求的槽
						//transModel[i][j] = Math.abs(vNodePageRank[i]-sNodePageRank[j]);
						transModel[i][j] = 1.0/s2v_n[j].rest_cpu;//div(1.0,s2v_n[j].rest_cpu,10);//1.0/(1.0*s2v_n[j].rest_cpu);
						slotIndexModel[i][j] = slotNum;
						linkModel[i][j] = link[0];
					} else {
						transModel[i][j] = -1;//-1代表不能映射
						slotIndexModel[i][j] = -1;
					}  
				} else {
					transModel[i][j] = -1;//-1代表不能映射
				}
			}
		}
	}
}
