package Team.CloudStorage.EAVONE;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Date;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.List;
//import commons-beanutils;

import javax.swing.JOptionPane;

//import org.apache.commons.beanutils.BeanUtils;

import edu.asu.emit.algorithm.graph.Graph;
import edu.asu.emit.algorithm.graph.Path;
import edu.asu.emit.algorithm.graph.VariableGraph;
import edu.asu.emit.algorithm.graph.abstraction.BaseVertex;
import edu.asu.emit.algorithm.graph.shortestpaths.DijkstraShortestPathAlg;
import edu.asu.emit.algorithm.graph.shortestpaths.YenTopKShortestPathsAlg;
import edu.asu.emit.qyan.test.YenTopKShortestPathsAlgTest;


public class VONEByTranModel extends VNE {
	
	public void VONEEmbed(String inSNFile,String inVNsFileDir,int reqsNum,int delay) throws IOException
	{
		//创建SN和VNs
		super.VONEEmbed(inSNFile, inVNsFileDir, reqsNum, delay);
		//embedModelOrAlgo = Parameters.MapVONETranModel;
    	//V2SEmbed
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
	    if(Parameters.DebugModel) System.out.println("RecordResultsOfVNE.");
	    RecordResultsOfVNE(sub,reqs,interval,Parameters.CurrentVONEMethod);
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
				WriteFilePlus("process.txt","req["+i+"] allocation in TranModel");
				if(reqs[i].topo == Parameters.TOPO_GENERAL || reqs[i].topo == Parameters.TOPO_STAR) {					
					if(Parameters.CurrentVONEMethod == Parameters.MapVONETranModel){
						if(MapVONEByTranModel(sub, reqs, i)!=-1){
							if(Parameters.DebugModel) Print_sub_slots(sub);
							v2s[i].map = Parameters.STATE_MAP_SUCC;
							reqs[i].map = Parameters.STATE_MAP_SUCC;
						} else {
							v2s[i].map = Parameters.STATE_MAP_NODE_FAIL;
							reqs[i].map = Parameters.STATE_MAP_NODE_FAIL;
						}
					} else if(Parameters.CurrentVONEMethod == Parameters.MapVONEEnTranModel){
						DebugVNE(sub,reqs,i,Parameters.CurrentVONEMethod,"before embed req "+i);
						if(MapVONEByEnTranModel(sub, reqs, i)!=-1){
							if(Parameters.DebugModel) Print_sub_slots(sub);
							v2s[i].map = Parameters.STATE_MAP_SUCC;
							reqs[i].map = Parameters.STATE_MAP_SUCC;
							DebugVNE(sub,reqs,i,Parameters.CurrentVONEMethod,"after embed succ req "+i);
						} else {
							v2s[i].map = Parameters.STATE_MAP_NODE_FAIL;
							reqs[i].map = Parameters.STATE_MAP_NODE_FAIL;
						}
					} else if(Parameters.CurrentVONEMethod == Parameters.MapVONETranILPByChenxh){
						DebugVNE(sub,reqs,i,Parameters.CurrentVONEMethod,"before embed req "+i);
						if(MapVONEByILPNETranModel(sub, reqs, i)!=-1){
							if(Parameters.DebugModel) Print_sub_slots(sub);
							v2s[i].map = Parameters.STATE_MAP_SUCC;
							reqs[i].map = Parameters.STATE_MAP_SUCC;
							DebugVNE(sub,reqs,i,Parameters.CurrentVONEMethod,"after embed succ req "+i);
						} else {
							v2s[i].map = Parameters.STATE_MAP_NODE_FAIL;
							reqs[i].map = Parameters.STATE_MAP_NODE_FAIL;
						}
					}
					
					
				}
			}
		}
	}
		
	/******************************************************************
	名称：int MapVONEByTranModel(......)
	功能：以运输模型映射虚拟光网络, 如果成功则填充s2v_n和v2s 
	参数：
		      s2v_n为物理节点映射虚网节点数据结构
		      s2v_l为物理链路映射虚网链路数据结构
		      v2s为虚网映射物理网络的数据结构 
		      index为第index个虚网请求
	,int ret[],int p[][],ArrayList<Object> list
	返回值：0：成功返回；-1：失败返回 
	******************************************************************/
	private int MapVONEByTranModel(EOSubstrateNetwork sub,VONRequest reqs[],int index)
	{
		//创建运输模型和最小可用的频谱槽索引
		double[][] transModel = new double[reqs[index].nodes][sub.nodes];
		int[][] indexModel = new int[reqs[index].nodes][sub.nodes];
		int[][] linkModel = new int[reqs[index].nodes][sub.nodes];
		InitTranModel(sub,reqs,index,transModel,indexModel,linkModel);
		
		//初始化分配,-1代表着未分配，>-1代表已经分配的节点或者链路号
		int[] vNodeEmbed = new int[reqs[index].nodes];
		int[] sNodeEmbed = new int[sub.nodes];
		int[] vLinkEmbed = new int[reqs[index].links];
		InitAllocModel(sub,reqs,index,vNodeEmbed,sNodeEmbed,vLinkEmbed);
		
		//p[][]:记录路径；ret[][]:ret[][0]:返回的起始频谱槽；ret[][1]:返回的频谱槽数量
		int p[][] = new int[reqs[index].links][sub.nodes];
		int ret[][] = new int[reqs[index].links][2];//ret[][0]:返回的起始频谱槽；ret[][1]:返回的频谱槽数量
		int retOther[][] = new int[reqs[index].links][2];
		for(int i=0;i<reqs[index].links;i++){
			for(int j=0;j<sub.nodes;j++)
				p[i][j] = -1;
			ret[i][0] = ret[i][0] = -1;
			retOther[i][0] = retOther[i][0] = -1;
		}
		EOSubstrateNetwork subCopy = new EOSubstrateNetwork();
		
		//BeanUtils.copyProperties(subCopy,sub);
		//subCopy = sub;
		Clone(subCopy,sub);
		int noEmbedVLink = -1;
		int num = 0;
		int[] minElement = new int[2];//minElement[0]虚拟节点；minElement[1]物理节点;
		while(num < reqs[index].nodes){
			//寻找最小元素，索引放在minElement[0]\minElement[1];minIndexReq，minIndexSub
			FindMinElement(subCopy,reqs,index,transModel,vNodeEmbed,sNodeEmbed,minElement);
			if(minElement[0] == -1) return -1;//没有找到最小元素
			vNodeEmbed[minElement[0]] = minElement[1];//虚拟节点minElement[0]映射到物理节点minElement[1]
			sNodeEmbed[minElement[1]] = minElement[0];//物理节点minElement[1]映射给虚拟节点minElement[0]
			//更新cpu
			UpdateSub(subCopy,minElement[1],reqs[index].cpu[minElement[0]]);
			
			//在虚拟网络中寻找是否存在的未映射的虚拟链路，如果存在，则映射；
			
			noEmbedVLink=FindNoEmbedVLink(reqs,index,minElement[0],vNodeEmbed,vLinkEmbed);
			while(noEmbedVLink > -1){//如果找到了未映射的虚拟链路，则映射该链路
				//映射该虚拟链路,映射结果保存在p[][]中，表示虚拟链路映射的路径;ret[][0]:起始频谱槽索引；ret[][1]:频谱槽数量；
				if(!PreEmbedVLinkByKShortestPath(subCopy,reqs,index,noEmbedVLink,vNodeEmbed,p,ret)){//noEmbedVLink虚拟链路，snodeEmbed对应的物理节点
					return -1;//失败返回
				}
				//链路已经分配
				vLinkEmbed[noEmbedVLink] = 1;
				//更新底层网络subCopy
				//UpdateSub(EOSubstrateNetwork sub,int sNode1,int sNode2,int ret[],int p[])
				int sNode1,sNode2;
				sNode1 = vNodeEmbed[reqs[index].link[noEmbedVLink].from];
				sNode2 = vNodeEmbed[reqs[index].link[noEmbedVLink].to];
				retOther[noEmbedVLink][0] = ret[noEmbedVLink][0];
				retOther[noEmbedVLink][1] = ret[noEmbedVLink][0]+ret[noEmbedVLink][1];
				UpdateSub(subCopy,sNode2,sNode1,retOther[noEmbedVLink],p[noEmbedVLink]);
				
				//UpdateSub(sub,snode2,snode1,ret[i],p[i]);
				
				if(Parameters.DebugModel) {
					System.out.println(noEmbedVLink+"("+retOther[noEmbedVLink][0]+"-"+retOther[noEmbedVLink][1]+")");
					PrintPath(p[noEmbedVLink],sNode2,sNode1);
				}
				noEmbedVLink=FindNoEmbedVLink(reqs,index,minElement[0],vNodeEmbed,vLinkEmbed);
			}
			num ++;
		}
		System.out.println("noEmbedVLink:"+noEmbedVLink);
		//if(noEmbedVLink == -1) return -1;
		//如果存在链路没有映射，则失败返回
		for(int i=0;i<reqs[index].links;i++){
			if(vLinkEmbed[i] == -1) return -1;//失败返回
		}
		//更新cpu
		UpdateSub(sub,subCopy);
		//记录节点和链路映射结果
		AddNodesMap(reqs,index,vNodeEmbed);//更新s2v_n和v2s
		
		//ret[noEmbedVLink][1]=ret[noEmbedVLink][0]+ret[noEmbedVLink][1];
		
		AddLinksMapBySPFA(sub,reqs,index,ret,p);//更新底层网络
		
		//更新底层网络slots
		UpdateSubSlots(sub,subCopy);
	  	
		return 0;//成功返回
	}
	
	private int MapVONEByMaxNodeCostTranModel(EOSubstrateNetwork sub,VONRequest reqs[],int index)
	{
		//创建运输模型和最小可用的频谱槽索引
		double[][] transModel = new double[reqs[index].nodes][sub.nodes];
		int[][] indexModel = new int[reqs[index].nodes][sub.nodes];
		int[][] linkModel = new int[reqs[index].nodes][sub.nodes];
		//InitTranModel(sub,reqs,index,transModel,indexModel,linkModel);
		InitMaxiNodeCostTranModel(subStatic,reqs,index,transModel,indexModel,linkModel);
		
		//初始化分配,-1代表着未分配，>-1代表已经分配的节点或者链路号
		int[] vNodeEmbed = new int[reqs[index].nodes];
		int[] sNodeEmbed = new int[sub.nodes];
		int[] vLinkEmbed = new int[reqs[index].links];
		InitAllocModel(sub,reqs,index,vNodeEmbed,sNodeEmbed,vLinkEmbed);
		
		//p[][]:记录路径；ret[][]:ret[][0]:返回的起始频谱槽；ret[][1]:返回的频谱槽数量
		int p[][] = new int[reqs[index].links][sub.nodes];
		int ret[][] = new int[reqs[index].links][2];//ret[][0]:返回的起始频谱槽；ret[][1]:返回的频谱槽数量
		int retOther[][] = new int[reqs[index].links][2];
		for(int i=0;i<reqs[index].links;i++){
			for(int j=0;j<sub.nodes;j++)
				p[i][j] = -1;
			ret[i][0] = ret[i][0] = -1;
			retOther[i][0] = retOther[i][0] = -1;
		}
		EOSubstrateNetwork subCopy = new EOSubstrateNetwork();
		
		//BeanUtils.copyProperties(subCopy,sub);
		//subCopy = sub;
		Clone(subCopy,sub);
		int noEmbedVLink = -1;
		int num = 0;
		int[] minElement = new int[2];//minElement[0]虚拟节点；minElement[1]物理节点;
		while(num < reqs[index].nodes){
			//寻找最小元素，索引放在minElement[0]\minElement[1];minIndexReq，minIndexSub
			//FindMinElement(subCopy,reqs,index,transModel,vNodeEmbed,sNodeEmbed,minElement);
			//寻找最大元素
			FindMaxElement(subCopy,reqs,index,transModel,vNodeEmbed,sNodeEmbed,minElement);
			if(minElement[0] == -1) return -1;//没有找到最小元素
			vNodeEmbed[minElement[0]] = minElement[1];//虚拟节点minElement[0]映射到物理节点minElement[1]
			sNodeEmbed[minElement[1]] = minElement[0];//物理节点minElement[1]映射给虚拟节点minElement[0]
			//更新cpu
			UpdateSub(subCopy,minElement[1],reqs[index].cpu[minElement[0]]);
			
			//在虚拟网络中寻找是否存在的未映射的虚拟链路，如果存在，则映射；
			
			noEmbedVLink=FindNoEmbedVLink(reqs,index,minElement[0],vNodeEmbed,vLinkEmbed);
			while(noEmbedVLink > -1){//如果找到了未映射的虚拟链路，则映射该链路
				//映射该虚拟链路,映射结果保存在p[][]中，表示虚拟链路映射的路径;ret[][0]:起始频谱槽索引；ret[][1]:频谱槽数量；
				//if(!PreEmbedVLinkByKShortestPath(subCopy,reqs,index,noEmbedVLink,vNodeEmbed,p,ret)){//noEmbedVLink虚拟链路，snodeEmbed对应的物理节点
				//	return -1;//失败返回
				//}
				//创建辅助图，映射虚拟链路
				if(!PreEmbedVLinkByAuxiGraph(subCopy,reqs,index,noEmbedVLink,vNodeEmbed,p,ret)){//noEmbedVLink虚拟链路，snodeEmbed对应的物理节点
					return -1;//失败返回
				}
				//链路已经分配
				vLinkEmbed[noEmbedVLink] = 1;
				//更新底层网络subCopy
				//UpdateSub(EOSubstrateNetwork sub,int sNode1,int sNode2,int ret[],int p[])
				int sNode1,sNode2;
				sNode1 = vNodeEmbed[reqs[index].link[noEmbedVLink].from];
				sNode2 = vNodeEmbed[reqs[index].link[noEmbedVLink].to];
				retOther[noEmbedVLink][0] = ret[noEmbedVLink][0];
				retOther[noEmbedVLink][1] = ret[noEmbedVLink][0]+ret[noEmbedVLink][1];
				UpdateSub(subCopy,sNode2,sNode1,retOther[noEmbedVLink],p[noEmbedVLink]);
				
				//UpdateSub(sub,snode2,snode1,ret[i],p[i]);
				
				if(Parameters.DebugModel) {
					System.out.println(noEmbedVLink+"("+retOther[noEmbedVLink][0]+"-"+retOther[noEmbedVLink][1]+")");
					PrintPath(p[noEmbedVLink],sNode2,sNode1);
				}
				noEmbedVLink=FindNoEmbedVLink(reqs,index,minElement[0],vNodeEmbed,vLinkEmbed);
			}
			num ++;
		}
		System.out.println("noEmbedVLink:"+noEmbedVLink);
		//if(noEmbedVLink == -1) return -1;
		//如果存在链路没有映射，则失败返回
		for(int i=0;i<reqs[index].links;i++){
			if(vLinkEmbed[i] == -1) return -1;//失败返回
		}
		//更新cpu
		UpdateSub(sub,subCopy);
		//记录节点和链路映射结果
		AddNodesMap(reqs,index,vNodeEmbed);//更新s2v_n和v2s
		
		//ret[noEmbedVLink][1]=ret[noEmbedVLink][0]+ret[noEmbedVLink][1];
		
		AddLinksMapBySPFA(sub,reqs,index,ret,p);//更新底层网络
		
		//更新底层网络slots
		UpdateSubSlots(sub,subCopy);
	  	
		return 0;//成功返回
	}
	/******************************************************************
	名称：int MapVONEByTranModel(......)
	功能：以节点映射优先映射, 如果成功则填充s2v_n和v2s 
	参数：
		      s2v_n为物理节点映射虚网节点数据结构
		      s2v_l为物理链路映射虚网链路数据结构
		      v2s为虚网映射物理网络的数据结构 
		      index为第index个虚网请求
	,int ret[],int p[][],ArrayList<Object> list
	返回值：0：成功返回；-1：失败返回 
	******************************************************************/
	private int MapVONEByILPNETranModel(EOSubstrateNetwork sub,VONRequest reqs[],int index)
	{
		int noEmbedVLink = -1;
		//初始化分配,-1代表着未分配，>-1代表已经分配的节点或者链路号
		int[] vNodeEmbed = new int[reqs[index].nodes];
		int[] sNodeEmbed = new int[sub.nodes];
		int[] vLinkEmbed = new int[reqs[index].links];
		InitAllocModel(sub,reqs,index,vNodeEmbed,sNodeEmbed,vLinkEmbed);
		
		//p[][]:记录路径；ret[][]:ret[][0]:返回的起始频谱槽；ret[][1]:返回的频谱槽数量
		int p[][] = new int[reqs[index].links][sub.nodes];
		int ret[][] = new int[reqs[index].links][2];//ret[][0]:返回的起始频谱槽；ret[][1]:返回的频谱槽数量
		int retOther[][] = new int[reqs[index].links][2];
		for(int i=0;i<reqs[index].links;i++){
			for(int j=0;j<sub.nodes;j++)
				p[i][j] = -1;
			ret[i][0] = ret[i][1] = -1;
			retOther[i][0] = retOther[i][1] = -1;
		}
		
		EOSubstrateNetwork subCopy = new EOSubstrateNetwork();
		Clone(subCopy,sub);
		//以LP模型求解节点映射
		CreateVONEILPNEByCXH(sub,reqs,index);
		if(FindOptSoluILPNEByCXH(sub,reqs,index,vNodeEmbed)){//找到了最优解
			noEmbedVLink=FindNoEmbedVLink(reqs,index,vLinkEmbed);
			while(noEmbedVLink > -1){//如果找到了未映射的虚拟链路，则映射该链路
				//映射该虚拟链路,映射结果保存在p[][]中，表示虚拟链路映射的路径;ret[][0]:起始频谱槽索引；ret[][1]:频谱槽数量；
				if(!PreEmbedVLinkByKShortestPath(subCopy,reqs,index,noEmbedVLink,vNodeEmbed,p,ret)){//noEmbedVLink虚拟链路，snodeEmbed对应的物理节点
					return -1;//失败返回
				}
				//链路已经分配
				vLinkEmbed[noEmbedVLink] = 1;
				//更新底层网络subCopy
				//UpdateSub(EOSubstrateNetwork sub,int sNode1,int sNode2,int ret[],int p[])
				int sNode1,sNode2;
				sNode1 = vNodeEmbed[reqs[index].link[noEmbedVLink].from];
				sNode2 = vNodeEmbed[reqs[index].link[noEmbedVLink].to];
				retOther[noEmbedVLink][0] = ret[noEmbedVLink][0];
				retOther[noEmbedVLink][1] = ret[noEmbedVLink][0]+ret[noEmbedVLink][1];
				UpdateSub(subCopy,sNode2,sNode1,retOther[noEmbedVLink],p[noEmbedVLink]);
				
				if(Parameters.DebugModel) {
					System.out.println(noEmbedVLink+"("+retOther[noEmbedVLink][0]+"-"+retOther[noEmbedVLink][1]+")");
					PrintPath(p[noEmbedVLink],sNode2,sNode1);
				}
				noEmbedVLink=FindNoEmbedVLink(reqs,index,vLinkEmbed);
			}
		} else {
			return -1;
		}
		//如果存在链路没有映射，则失败返回
		for(int i=0;i<reqs[index].links;i++){
			if(vLinkEmbed[i] == -1) return -1;//失败返回
		}
		//更新cpu
		for(int i=0;i<reqs[index].nodes;i++){
			int sNode = vNodeEmbed[i];
			sub.cpu[sNode] -= reqs[index].cpu[i];
		}
		
		//记录节点和链路映射结果
		AddNodesMap(reqs,index,vNodeEmbed);//更新s2v_n和v2s
		AddLinksMapBySPFA(sub,reqs,index,retOther,p);//更新底层网络
		
		//更新底层网络slots
		UpdateSubSlots(sub,subCopy);
		return 0;//成功返回
	}
	/*名称：void FindNoEmbedVLink(......)
	 * 功能：寻找与虚拟节点连接的未映射的虚拟链路
	 * 参数：
	 * 	reqs为虚网光网络
	 * 	index为第index个虚网请求
	 * 	vNode为虚拟节点；
	 * 	vnodeEmbed为虚拟节点映射模型
	 * 	vlinkEmbed为物理链路映射模型
	 * 返回值：     与vLink连接的未映射的虚拟链路，-1表示未找到；
	 */
	public int FindNoEmbedVLink(VONRequest reqs[],int index,int[] vlinkEmbed)
	{
		//找到与虚拟节点vLink连接的链路
		int maxBWVL = -1;
		double maxBW = -1;
		for(int i=0;i<reqs[index].links;i++){
			if(vlinkEmbed[i] == -1) {
				if(maxBW <= reqs[index].link[i].bw) {
					maxBW = reqs[index].link[i].bw;
					maxBWVL = i;
				}
			}
		}
		return maxBWVL;//没有找到虚拟链路
	}
	/******************************************************************
	//名称：int FindVONEOptimalSolution(......)
	//功能：以01ILP模型映射虚拟光网络, 如果成功则返回true、ret[],p[] 
	//参数：
	//	      ret[]为返回的映射结果：
	//        ret[0]=minSlotIndex(即频谱槽的低位)
	//        ret[1]=maxSlotIndex(即频谱槽的高位)
	//	      p[]为映射的路径
	////	  list为映射的物理节点
	//返回值：true：成功返回；false：失败返回 
	//创建人：陈晓华
	//创建日期：2017-09-27
	//******************************************************************/
	public boolean FindOptSoluILPNEByCXH(EOSubstrateNetwork sub,VONRequest reqs[],int index,int retNodeE[])
	{
		BufferedReader reader = null;
		
		int keySNode1 = -1;
		int keyVNode1 = -1;
		
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
	                	tempString = tempString.replace("Objective:  slotsMin = ", "");
	                	tempString = tempString.replace("(MINimum)", "");
	                	tempString = tempString.trim();
	                } else if(line > 6 && tempString.indexOf(" A[") != -1){//说明找到了最优解的x的行
	                	String tmpStr = "";
	                	//System.out.println("line " + line + ": " + tempString);
	                	//String tempString1 = reader.readLine();
	                	String tempString1 = tempString.trim();
	                	
	                	tmpStr = tempString1.substring(tempString1.indexOf("*")+1);
	                	tmpStr = tmpStr.trim();
	                	//System.out.println("line " + line + ": " + tmpStr);
	                	
	                	tmpStr = tmpStr.substring(0,tmpStr.indexOf(" "));
	                	//System.out.println("line " + line + ": " + tmpStr);
	                	if(Integer.parseInt(tmpStr) == 1){//说明找到了一个解
	                		//得到一个解赋值给tmpStr，例如x[0,2]
	                		//var f{(i,j) in E,(m,n) in Ev,s in Vf,t in Vf,k in MSet}, binary;
	                		tempString = tempString.trim();
	                		tmpStr = tempString.substring(tempString.indexOf(" ")+1);//去掉前面的行号
	                		//System.out.println("line " + line + ": " + tmpStr);
	                		//tmpStr = tmpStr.substring(0,tmpStr.indexOf(" "));		//得到f[i,j,m,n,s,t,k]
	                		//System.out.println("line " + line + ": " + tmpStr);
	                		int keyNode1 = -1;
	                		//keyNode1 = Integer.parseInt(tmpStr.substring(tmpStr.indexOf("[")+1, tmpStr.indexOf(",")));//得到f[i,j,m,n,s,t,k]的i
	                		//System.out.println("keyNode1:"+keyNode1);
	                		//M[5,1]
	                		keyVNode1 = Integer.parseInt(tmpStr.substring(tmpStr.indexOf("[")+1, tmpStr.indexOf(",")));
	                		//System.out.println("keyNode1:"+keySNode1);
	                		tmpStr = tmpStr.substring(tmpStr.indexOf(",")+1);
	                		keySNode1 = Integer.parseInt(tmpStr.substring(0, tmpStr.indexOf("]")));
	                		
	                		retNodeE[keyVNode1] = keySNode1;//虚拟节点keyVNode1被映射到物理节点keySNode1上
	                	}
	                }
	                
	                line++;
	            } 
	            reader.close();
	     } catch (IOException e) {
	            e.printStackTrace();
	            return false;
	     } finally {
	            if (reader != null) {
	                try {
	                    reader.close();
	                } catch (IOException e1) {
	                	return false;
	                }
	            }
	     }  
        return true;
	}
	
	/*
	 * void CreateVONE01ILPByLin()
	 * 功能：构建01ILP模型求解
	 */
	private void CreateVONEILPNEByCXH(EOSubstrateNetwork sub,VONRequest reqs[],int index)
	{
		int M = sub.slotsNum;
		
		Tools myDowith = new Tools();
		
		String data;
		data = "set Nv:=";
		for(int i = 0; i < reqs[index].nodes; i++){
			data += " " + i;    
		}
		data += ";\r\n"; 		 
		myDowith.SaveFile("glpsolRSA.dat", data, false);
		
		/*
		data = "set Lv:=\r\n";
		for(int j = 0; j < reqs[index].links; j ++) {
			if(reqs[index].link[j].from < reqs[index].link[j].to)
				data += reqs[index].link[j].from + " " + reqs[index].link[j].to + "\r\n";
		}
		data += ";\r\n"; 		 
		myDowith.SaveFile("glpsolRSA.dat", data, true);
		*/
		data = "set Ns:=";
		for(int j = 0; j < sub.nodes; j ++) {
			data += j + " ";
		}
		data += ";\r\n"; 		 
		myDowith.SaveFile("glpsolRSA.dat", data, true);
		/*
		data = "";
		for(int j = 0; j < sub.nodes; j ++) {
			data += "set Nss[" + j + "]:=";
			for(int i=0;i<sub.links;i++){
				if(sub.link[i].from == j){
					if(sub.link[i].from < sub.link[i].to) 
						data += sub.link[i].to + " ";
				}
			}
			data += ";\r\n"; 
		}		 
		myDowith.SaveFile("glpsolRSA.dat", data, true);
		
		data = "";
		for(int j = 0; j < sub.nodes; j++) {
			data = "set Nss1[" + j + "]:=";
			for(int i=0;i<sub.links;i++){
				if(sub.link[i].to == j){
					if(sub.link[i].from < sub.link[i].to) 
						data += sub.link[i].from + " ";
				}
			}
			data += ";\r\n"; 
			myDowith.SaveFile("glpsolRSA.dat", data, true);
		}		 
		
		
		data = "set Ls:=\r\n";
		for(int j = 0; j < sub.links; j ++) {
			if(sub.link[j].from < sub.link[j].to)
				data += sub.link[j].from + " " + sub.link[j].to + "\r\n";
		}
		data += ";\r\n"; 		 
		myDowith.SaveFile("glpsolRSA.dat", data, true);
		*/
		data = "";
		for(int j = 0; j < reqs[index].nodes; j ++) {
			data += "set X["+j+"]:=";
			for(int i=0;i<sub.nodes;i++){
				if(s2v_n[i].rest_cpu >= reqs[index].cpu[j]){//
					data += i + " ";
				}
			}
			data += ";\r\n"; 
		}	 
		myDowith.SaveFile("glpsolRSA.dat", data, true);
		/*
		data = "set SlotIndex:=";
		for(int j = 0; j < sub.slotsNum; j ++) {
			data += j + " ";
		}	
		data += ";\r\n"; 
		myDowith.SaveFile("glpsolRSA.dat", data, true);
		
		data = "set MD:=1,2,3,4,6,8;\r\n";
		myDowith.SaveFile("glpsolRSA.dat", data, true);
		
		//Slot{(i,j) in Lv,a in SlotIndex,model in MD};
		data = "";
		for(int j = 0; j < reqs[index].links; j++) {
			if(reqs[index].link[j].from > reqs[index].link[j].to) continue;
			for(int i=0;i<sub.slotsNum;i++){
				for(int k=1;k<9;){
					data = "set Slot["+reqs[index].link[j].from+","+reqs[index].link[j].to+","+i+","+k+"]:=";
					for(int p=i;p<sub.slotsNum&&(p<i+Math.ceil(reqs[index].link[j].bw/(12.5*k))+sub.diffSlot);p++){
						if(i+Math.ceil(reqs[index].link[j].bw/(12.5*k))+sub.diffSlot > sub.slotsNum) {
							break;
						}
						data += p + " ";
						break;
					}
					data += ";\r\n"; 
					myDowith.SaveFile("glpsolRSA.dat", data, true);
					if(k>=4) k+=2;
					else k++;
				}
			}
		}	
		
		
		//set Slot1{(i,j) in Lv,a in SlotIndex,model in MD};//从a+|bw/model|+G-1到a+|bw/model|+G-1+1,...,a
		data = "";
		for(int j = 0; j < reqs[index].links; j ++) {
			if(reqs[index].link[j].from > reqs[index].link[j].to) continue;
			for(int i=0;i<sub.slotsNum;i++){
				for(int k=1;k<9;){
					data = "set Slot1["+reqs[index].link[j].from+","+reqs[index].link[j].to+","+i+","+k+"]:=";
					for(int p=i;p>=0&&(p>=i-Math.ceil(reqs[index].link[j].bw/(12.5*k))-sub.diffSlot+1);p--){
						if(i-Math.ceil(reqs[index].link[j].bw/(12.5*k))-sub.diffSlot+1<0) {
							break;
						}
						data += p + " ";
					}
					data += ";\r\n"; 
					if(k>=4) k+=2;
					else k++;
					myDowith.SaveFile("glpsolRSA.dat", data, true);
				}
			}
		}	
		*/
		
		//param c{k in Ns};/*物理节点的计算单位代价*/
		data = "param c:=\r\n";
		for (int i = 0; i < sub.nodes; i++) {
			data += i + " " + 1/s2v_n[i].rest_cpu +"\r\n";//s2v_n[i].rest_cpu
		}
		data += ";\r\n";	
		myDowith.SaveFile("glpsolRSA.dat", data, true);
		/*
		//param cs{(i,j) in Ls};//物理链路的频谱槽单位代价，与长度成比例
		data = "param cs:=\r\n";
		for (int i = 0; i < sub.links; i++) {
			if(sub.link[i].from > sub.link[i].to) continue;
			data += sub.link[i].from + " " + sub.link[i].to + " " + sub.link[i].length+"\r\n";
			
		}
		data += ";\r\n";	
		myDowith.SaveFile("glpsolRSA.dat", data, true);
		*/
		//param p{i in Nv};/*虚拟节点CPU*/
		data = "param p:=\r\n";
		for (int i = 0; i < reqs[index].nodes; i++) {
			data += i + " " + reqs[index].cpu[i]+"\r\n";
		}
		data += ";\r\n";	
		myDowith.SaveFile("glpsolRSA.dat", data, true);
		/*
		//param pbw{(i,j) in Lv};//虚拟链路带宽请求
		data = "param pbw:=\r\n";
		for (int i = 0; i < reqs[index].links; i++) {
			if(reqs[index].link[i].from > reqs[index].link[i].to) continue;
			data += reqs[index].link[i].from + " " + reqs[index].link[i].to + " " + reqs[index].link[i].bw+"\r\n";
		}
		data += ";\r\n";	
		myDowith.SaveFile("glpsolRSA.dat", data, true);
		
		//param F;//最大的频谱槽索引
		data = "param F:="+(sub.slotsNum-1)+";\r\n";
		myDowith.SaveFile("glpsolRSA.dat", data, true);
		*/
		//param U{k in Ns};/*物理节点k的剩余cpu*/
		data = "param U:=\r\n";
		for (int i = 0; i < sub.nodes; i++) {
			data += i + " " + s2v_n[i].rest_cpu +"\r\n";//s2v_n[i].rest_cpu
		}
		data += ";\r\n";	
		myDowith.SaveFile("glpsolRSA.dat", data, true);
		/*
		//param USlot{(i,j) in Ls,t in SlotIndex};物理链路频谱槽的状态，1表示空闲；0表示被占用
		data = "param USlot:=\r\n";
		for (int i = 0; i < sub.links; i++) {
			if(sub.link[i].from > sub.link[i].to) continue;
			for(int j=0;j<sub.slotsNum;j++){
				data += sub.link[i].from + " " + sub.link[i].to + " " +j + " "+ sub.slots[i][j]+"\r\n";
			}
		}
		data += ";\r\n";	
		myDowith.SaveFile("glpsolRSA.dat", data, true);
		
		//param d{(i,j) in Ls};//物理链路的长度
		data = "param d:=\r\n";
		for (int i = 0; i < sub.links; i++) {
			if(sub.link[i].from > sub.link[i].to) continue;
			data += sub.link[i].from + " " + sub.link[i].to + " " + sub.link[i].length+"\r\n";
		}
		data += ";\r\n";	
		myDowith.SaveFile("glpsolRSA.dat", data, true);
		
		//param dis{m in MD};//调制模式最大传输距离
		data = "param dis:=\r\n";
		data += "1 3000\r\n";
		data += "2 1500\r\n";
		data += "3 750\r\n";
		data += "4 375\r\n";
		data += "6 94\r\n";
		data += "8 24\r\n";
		data += ";\r\n";	
		myDowith.SaveFile("glpsolRSA.dat", data, true);
		
		//param R;//最大的整数
		data = "param R:=100000000;\r\n";
		myDowith.SaveFile("glpsolRSA.dat", data, true);
		
		//param G;//隔离槽数量
		data = "param G:="+sub.diffSlot+";\r\n";
		myDowith.SaveFile("glpsolRSA.dat", data, true);
		*/
		System.out.println("Done");
		
		try {
			String s;
			Process process = null;
			process = Runtime.getRuntime().exec("cmd /c C:/陈晓华/VONE/VONE/glpk-4.60/w64/glpsol.exe -m C:/陈晓华/VONE/VONE/glpk-4.60/w64/glpsolILPNEByCXH.mod -d glpsolRSA.dat -o glpsolRSA.o");
			
			//if(embedModelOrAlgo == Parameters.MapVONE3ByWangY){
			//	process = Runtime.getRuntime().exec("cmd /c C:/陈晓华/VONE/VONE/glpk-4.60/w64/glpsol.exe -m C:/陈晓华/VONE/VONE/glpk-4.60/w64/glpsolMILVONE3NodesNoDataWangY.mod -d glpsolRSA.dat -o glpsolRSA.o");
			//} else if(embedModelOrAlgo == Parameters.MapVONE3PByWangYAndChenxh){
			//	process = Runtime.getRuntime().exec("cmd /c C:/陈晓华/VONE/VONE/glpk-4.60/w64/glpsol.exe -m C:/陈晓华/VONE/VONE/glpk-4.60/w64/glpsolMILPVONE3NodesNoDataWangY.mod -d glpsolRSA.dat -o glpsolRSA.o");
			//}
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
	/******************************************************************
	名称：int MapVONEByTranModel(......)
	功能：以运输模型映射虚拟光网络, 如果成功则填充s2v_n和v2s 
	参数：
		      s2v_n为物理节点映射虚网节点数据结构
		      s2v_l为物理链路映射虚网链路数据结构
		      v2s为虚网映射物理网络的数据结构 
		      index为第index个虚网请求
	,int ret[],int p[][],ArrayList<Object> list
	返回值：0：成功返回；-1：失败返回 
	******************************************************************/
	private int MapVONEByEnTranModel(EOSubstrateNetwork sub,VONRequest reqs[],int index)
	{
		//创建运输模型和最小可用的频谱槽索引
		double[][] transModel = new double[reqs[index].nodes][sub.nodes];
		int[][] indexModel = new int[reqs[index].nodes][sub.nodes];
		int[][] linkModel = new int[reqs[index].nodes][sub.nodes];
		InitTranModel(sub,reqs,index,transModel,indexModel,linkModel);
		
		//初始化分配,-1代表着未分配，>-1代表已经分配的节点或者链路号
		int[] vNodeEmbed = new int[reqs[index].nodes];
		int[] sNodeEmbed = new int[sub.nodes];
		int[] vLinkEmbed = new int[reqs[index].links];
		InitAllocModel(sub,reqs,index,vNodeEmbed,sNodeEmbed,vLinkEmbed);
		
		//p[][]:记录路径；ret[][]:ret[][0]:返回的起始频谱槽；ret[][1]:返回的频谱槽数量
		int p[][] = new int[reqs[index].links][sub.nodes];
		int ret[][] = new int[reqs[index].links][2];//ret[][0]:返回的起始频谱槽；ret[][1]:返回的频谱槽数量
		int retOther[][] = new int[reqs[index].links][2];
		for(int i=0;i<reqs[index].links;i++){
			for(int j=0;j<sub.nodes;j++)
				p[i][j] = -1;
			ret[i][0] = ret[i][1] = -1;
			retOther[i][0] = retOther[i][1] = -1;
		}
		EOSubstrateNetwork subCopy = new EOSubstrateNetwork();
		
		//BeanUtils.copyProperties(subCopy,sub);
		//subCopy = sub;
		Clone(subCopy,sub);
		
		int num = 0;
		int[] minElement = new int[2];//minElement[0]虚拟节点；minElement[1]物理节点;
		while(num < reqs[index].nodes){
			//寻找最小元素，索引放在minElement[0]\minElement[1];minIndexReq，minIndexSub
			FindEnMinElement(subCopy,reqs,index,transModel,vNodeEmbed,sNodeEmbed,minElement);
			if(minElement[0] == -1) return -1;//没有找到最小元素
			vNodeEmbed[minElement[0]] = minElement[1];//虚拟节点minElement[0]映射到物理节点minElement[1]
			sNodeEmbed[minElement[1]] = minElement[0];//物理节点minElement[1]映射给虚拟节点minElement[0]
			//更新cpu
			UpdateSub(subCopy,minElement[1],reqs[index].cpu[minElement[0]]);
			
			//在虚拟网络中寻找是否存在的未映射的虚拟链路，如果存在，则映射；
			int noEmbedVLink = -1;
			noEmbedVLink=FindNoEmbedVLink(reqs,index,minElement[0],vNodeEmbed,vLinkEmbed);
			while(noEmbedVLink > -1){//如果找到了未映射的虚拟链路，则映射该链路
				//映射该虚拟链路,映射结果保存在p[][]中，表示虚拟链路映射的路径;ret[][0]:起始频谱槽索引；ret[][1]:频谱槽数量；
				if(!PreEmbedVLinkByKShortestPath(subCopy,reqs,index,noEmbedVLink,vNodeEmbed,p,ret)){//noEmbedVLink虚拟链路，snodeEmbed对应的物理节点
					return -1;//失败返回
				}
				//链路已经分配
				vLinkEmbed[noEmbedVLink] = 1;
				//更新底层网络subCopy
				//UpdateSub(EOSubstrateNetwork sub,int sNode1,int sNode2,int ret[],int p[])
				int sNode1,sNode2;
				sNode1 = vNodeEmbed[reqs[index].link[noEmbedVLink].from];
				sNode2 = vNodeEmbed[reqs[index].link[noEmbedVLink].to];
				retOther[noEmbedVLink][0] = ret[noEmbedVLink][0];
				retOther[noEmbedVLink][1] = ret[noEmbedVLink][0]+ret[noEmbedVLink][1]-1;
				UpdateSub(subCopy,sNode2,sNode1,retOther[noEmbedVLink],p[noEmbedVLink]);
				if(Parameters.DebugModel) {
					System.out.println(noEmbedVLink+"("+retOther[noEmbedVLink][0]+"-"+retOther[noEmbedVLink][1]+")");
					PrintPath(p[noEmbedVLink],sNode2,sNode1);
				}
				noEmbedVLink=FindNoEmbedVLink(reqs,index,minElement[0],vNodeEmbed,vLinkEmbed);
			}
			num ++;
		}
		
		//如果存在链路没有映射，则失败返回
		for(int i=0;i<reqs[index].links;i++){
			if(vLinkEmbed[i] == -1) return -1;//失败返回
		}
		//更新cpu
		UpdateSub(sub,subCopy);
		
		//记录节点和链路映射结果
		AddNodesMap(reqs,index,vNodeEmbed);//更新s2v_n和v2s
		AddLinksMapBySPFA(sub,reqs,index,retOther,p);//更新底层网络
		
		//更新底层网络slots
		UpdateSubSlots(sub,subCopy);
	  	
		return 0;//成功返回
	}	
	/******************************************************************
	名称：void MapVONEByEnTranModel(......)
	功能：以运输模型为基础的增强型运输模型映射虚拟光网络, 如果成功则填充s2v_n和v2s 
	参数：
		      s2v_n为物理节点映射虚网节点数据结构
		      s2v_l为物理链路映射虚网链路数据结构
		      v2s为虚网映射物理网络的数据结构 
		      index为第index个虚网请求
	,int ret[],int p[][],ArrayList<Object> list
	返回值：0：成功返回；-1：失败返回 
	******************************************************************/
	private int MapVONEByRuTranModel(EOSubstrateNetwork sub,VONRequest reqs[],int index)
	{
		//start 初始化
		//创建运输模型和最小可用的频谱槽索引
		double[][] transModel = new double[reqs[index].nodes][sub.nodes];
		int[][] indexModel = new int[reqs[index].nodes][sub.nodes];
		int[][] linkModel = new int[reqs[index].nodes][sub.nodes];
		InitTranModel(sub,reqs,index,transModel,indexModel,linkModel);
		
		//初始化分配,-1代表着未分配，>-1代表已经分配的节点或者链路号
		int[] vNodeEmbed = new int[reqs[index].nodes];
		int[] sNodeEmbed = new int[sub.nodes];
		int[] vLinkEmbed = new int[reqs[index].links];
		InitAllocModel(sub,reqs,index,vNodeEmbed,sNodeEmbed,vLinkEmbed);
		
		//p[][]:记录路径；ret[][]:ret[][0]:返回的起始频谱槽；ret[][1]:返回的频谱槽数量
		int p[][] = new int[reqs[index].links][sub.nodes];
		int ret[][] = new int[reqs[index].links][2];//ret[][0]:返回的起始频谱槽；ret[][1]:返回的频谱槽数量
		int retOther[][] = new int[reqs[index].links][2];
		for(int i=0;i<reqs[index].links;i++){
			for(int j=0;j<sub.nodes;j++)
				p[i][j] = -1;
			ret[i][0] = ret[i][0] = -1;
			retOther[i][0] = retOther[i][0] = -1;
		}
		EOSubstrateNetwork subCopy = new EOSubstrateNetwork();
		Clone(subCopy,sub);
		//end 初始化
		
		//找到未映射的虚拟链路vlink对应的虚拟节点vq，约束：该虚拟链路的带宽需求最大；一个虚拟节点已经被映射
		int evlink = 0,vp1,vp2,sp1,sp2,vlink;//evlink映射的虚拟链路数量，vp1，vp2，vlink是找到的未映射的虚拟链路vlink的两个端点
		int[] vTwoNodeAndLink = new int[3];//vp1=vTwoNodeAndLink[0];vp2=vTwoNodeAndLink[1];vlink=vTwoNodeAndLink[2];
		
		while(FindNoEVlink(reqs,index,vLinkEmbed,vNodeEmbed,vTwoNodeAndLink)){
			vp1=vTwoNodeAndLink[0];
			vp2=vTwoNodeAndLink[1];
			vlink=vTwoNodeAndLink[2];
			if(vNodeEmbed[vp1]==-1 && vNodeEmbed[vp2]==-1){
				sp1 = FindSNodeByVNode(sub,vp1,transModel,sNodeEmbed);//找到vp1映射的sp1节点
				if(sp1 == -1){//失败返回
					return -1;
				}
				//sp2 = FindSNodeByVNodeIncludeLink(vp1,vp2,sp1,vlink,p);//找到vp2映射的sp2节点,且对应的链路映射
			} else if(vNodeEmbed[vp1]==-1 && vNodeEmbed[vp2]!=-1){
				sp2 = vNodeEmbed[vp2];
				//sp1 = FindSNodeByVNode(vp2,vp1,sp2,vlink,p);
			} else if(vNodeEmbed[vp1]!=-1 && vNodeEmbed[vp2]==-1){
				sp1 = vNodeEmbed[vp1];
				//sp2 = FindSNodeByVNode(vp1,vp2,sp1,vlink,p);
			} else if(vNodeEmbed[vp1]!=-1 && vNodeEmbed[vp2]!=-1){
				sp1 = vNodeEmbed[vp1];
				sp2 = vNodeEmbed[vp2];
				//EmbedVLink(sp1,sp2,p);
			}
			//预分配
			//PreEmbedNodesAndLinks();
			evlink++;
		}
		if(evlink == reqs[index].links){
			//EmbedNodesAndLinks();
			return 1;
		} else {
			return -1;//映射失败
		}
	}
	/******************************************************************
	名称：int FindSNodeByVNode(......)
	功能：找到对应的虚拟节点映射的底层节点
	算法：根据运输模型transModel[][]和物理节点映射sNodeEmbed[]
	           首先检测
	参数：double[][] transModel
		      
	返回值：true：成功返回；false：失败返回 
	******************************************************************/
	private int FindSNodeByVNodeIncludeLink(EOSubstrateNetwork sub,int vNode,double[][] transModel,int[] sNodeEmbed)
	{
		double embedCost = Parameters.MAX_VALUE_DOUBLE;
		int i=-1;
		double nodeECost = -1, linkECost = -1;
		for(i=0;i<sub.nodes;i++){
			if(transModel[vNode][i] > -1  && sNodeEmbed[i] == -1 ){
				//检查映射代价，以最短路径的链路映射代价linkECost和节点映射代价nodeECost
				nodeECost = transModel[vNode][i];
				//linkECost = LinkEmbedCost(subCopy);
				//if(!PreEmbedVLinkByKShortestPath(subCopy,reqs,index,noEmbedVLink,vNodeEmbed,p,ret)){//noEmbedVLink虚拟链路，snodeEmbed对应的物理节点
				//	return -1;//失败返回
				//}
				embedCost = Parameters.NodeECoEfficient * nodeECost + Parameters.LinkECoEfficient * linkECost;
			}
		}
		if(i>=sub.nodes) return -1;
		else return i;
	}
	/******************************************************************
	名称：boolean LinkEmbedCost(......)
	功能：提供一个物理节点sp1和带宽bw，并提供sp2，计算sp2->sp1的映射代价
	算法：
	参数：double[][] transModel：运输模型
	返回值：1）true：成功返回；false：失败返回 
	      2）路径；
	      3）映射频谱槽总量：
	      4）映射的频谱槽起始和截止索引：
	******************************************************************/
	private boolean LinkEmbedCost(EOSubstrateNetwork sub,int vNode,double[][] transModel,int[] sNodeEmbed)
	{
		int[] flag = new int[sub.nodes];
		int[] prev = new int[sub.nodes];
		int[] dist = new int[sub.nodes];
		for(int i=0;i<sub.nodes;i++){
			flag[i] = -1;//说明i节点不在s中
			prev[i] = -1;
			dist[i] = -1;
		}
		return true;
		//s[sp2] = 
		//
	}
	
	/******************************************************************
	名称：int FindSNodeByVNode(......)
	功能：找到对应的虚拟节点映射的底层节点
	算法：根据运输模型transModel[][]和物理节点映射sNodeEmbed[]
	           在运输模型中找到最小的值，且物理节点未映射
	参数：double[][] transModel：运输模型
	返回值：>-1：成功返回；-1：失败返回 
	******************************************************************/
	private int FindSNodeByVNode(EOSubstrateNetwork sub,int vNode,double[][] transModel,int[] sNodeEmbed)
	{
		double embedCost = Parameters.MAX_VALUE_DOUBLE;
		int i=-1;
		for(i=0;i<sub.nodes;i++){
			if(transModel[vNode][i] > -1 && embedCost < transModel[vNode][i] && sNodeEmbed[i] == -1 ){
				embedCost = transModel[vNode][i];
			}
		}
		if(i>=sub.nodes) return -1;
		else return i;
	}
	
	/******************************************************************
	名称：boolean FindNoEVlink(......)
	功能：找到未映射的虚拟链路,1)首先
	参数：
		      
	返回值：true：成功返回；false：失败返回 
	******************************************************************/
	private boolean FindNoEVlink(VONRequest reqs[],int index,int[] vLinkEmbed,int[] vNodeEmbed,int[] vTwoNodeAndLink)
	{
		double maxBW = Parameters.MIN_VALUE_DOUBLE;
		int i=0;
		//首先，找到没有映射的虚拟链路，其中的节点最少有一个已经映射
		for(i=0;i<reqs[index].links;i++){
			if(vLinkEmbed[i] == -1 && maxBW < reqs[index].link[i].bw && (vNodeEmbed[reqs[index].link[i].from] > -1 ||vNodeEmbed[reqs[index].link[i].to] > -1))
				maxBW = reqs[index].link[i].bw;
		}
		if(i<reqs[index].links){
			vTwoNodeAndLink[0] = reqs[index].link[i].from;
			vTwoNodeAndLink[1] = reqs[index].link[i].to;
			vTwoNodeAndLink[2] = i;
			
			return true;
		}
		maxBW = Parameters.MIN_VALUE_DOUBLE;
		//其次，找到没有映射的虚拟链路
		for(i=0;i<reqs[index].links;i++){
			if(vLinkEmbed[i] == -1 && maxBW < reqs[index].link[i].bw)
				maxBW = reqs[index].link[i].bw;
		}
		if(i>=reqs[index].links){
			return false;
		}
		vTwoNodeAndLink[0] = reqs[index].link[i].from;
		vTwoNodeAndLink[1] = reqs[index].link[i].to;
		vTwoNodeAndLink[2] = i;
		
		return true;
	}
	
	
	/******************************************************************
	名称：void FindMinElement(......)
	功能：寻找最小元素
	参数：
		      sub为物理网络
		      reqs为虚网光网络
		      index为第index个虚网请求
	        transModel为运输模型；
	        vnodeEmbed为虚拟节点映射模型
	        snodeEmbed为物理节点映射模型
	返回值：     minElent为最小元素，minElent[0]:最小元素虚拟节点；minElent[1]:最小元素物理节点；
	******************************************************************/
	private void FindMinElement(EOSubstrateNetwork sub,VONRequest reqs[],int index,double[][] transModel,int[] vnodeEmbed,int[] snodeEmbed,int[] minElent)
	{
		//寻找最小元素，索引放在minIndexReq，minIndexSub
		minElent[0] = minElent[1] = -1;
		double minElement = 10000;
		for(int i=0;i<reqs[index].nodes;i++){
			for(int j=0;j<sub.nodes;j++){
				if(minElement>transModel[i][j] && transModel[i][j]>-1 && vnodeEmbed[i]==-1 && snodeEmbed[j]==-1){//vnodeEmbed[i] == -1表示虚拟节点i未被映射
					minElent[0] = i;//minIndexReq = i;
					minElent[1] = j;//minIndexSub = j;
					minElement = transModel[i][j];
				}
			}
		}
		//if(minElent[0] > -1) return -1;//没有找到最小元素
	}
	
	private void FindMaxElement(EOSubstrateNetwork sub,VONRequest reqs[],int index,double[][] transModel,int[] vnodeEmbed,int[] snodeEmbed,int[] minElent)
	{
		//寻找最小元素，索引放在minIndexReq，minIndexSub
		minElent[0] = minElent[1] = -1;
		double minElement = -1;
		for(int i=0;i<reqs[index].nodes;i++){
			for(int j=0;j<sub.nodes;j++){
				if(minElement<transModel[i][j] && transModel[i][j]>-1 && vnodeEmbed[i]==-1 && snodeEmbed[j]==-1){//vnodeEmbed[i] == -1表示虚拟节点i未被映射
					minElent[0] = i;//minIndexReq = i;
					minElent[1] = j;//minIndexSub = j;
					minElement = transModel[i][j];
				}
			}
		}
		//if(minElent[0] > -1) return -1;//没有找到最小元素
	}
	
	
	
	/******************************************************************
	名称：void FindMinElement(......)
	功能：寻找最小元素
	参数：
		      sub为物理网络
		      reqs为虚网光网络
		      index为第index个虚网请求
	        transModel为运输模型；
	        vnodeEmbed为虚拟节点映射模型
	        snodeEmbed为物理节点映射模型
	返回值：     minElent为最小元素，minElent[0]:最小元素虚拟节点；minElent[1]:最小元素物理节点；
	******************************************************************/
	private void FindEnMinElement(EOSubstrateNetwork sub,VONRequest reqs[],int index,double[][] transModel,int[] vnodeEmbed,int[] snodeEmbed,int[] minElent)
	{
		//寻找最小元素，索引放在minIndexReq，minIndexSub
		double minElement = 10000;
		//找到与已经映射的节点连接的未映射的节点
		minElent[0] = minElent[1] = -1;
		int othVNode,othSNode;
		minElement = 10000;
		int slotNoRe = Parameters.MAX_VALUE_INT;
		for(int i=0;i<reqs[index].nodes;i++){
			//判断是否i节点是否与已经映射的虚拟节点连接
			for(int k=0;k<reqs[index].links;k++){
				if((i == reqs[index].link[k].from && vnodeEmbed[reqs[index].link[k].to] != -1) || (i == reqs[index].link[k].to && vnodeEmbed[reqs[index].link[k].from] != -1)){
					for(int j=0;j<sub.nodes;j++){
						if(minElement>transModel[i][j] && transModel[i][j]>-1 && vnodeEmbed[i]==-1 && snodeEmbed[j]==-1){//vnodeEmbed[i] == -1表示虚拟节点i未被映射
							if(i == reqs[index].link[k].from) {
								othVNode = reqs[index].link[k].to;
								othSNode = vnodeEmbed[othVNode];
							} else if(i == reqs[index].link[k].to) {
								othVNode = reqs[index].link[k].from;
								othSNode = vnodeEmbed[othVNode];
							}
							int slotNoRe1 = CheckIfEnoughSlotsOnLink(sub,k,reqs[index].link[k].bw);
							if(slotNoRe1 <= slotNoRe){
								minElent[0] = i;//minIndexReq = i;
								minElent[1] = j;//minIndexSub = j;
								minElement = transModel[i][j];
								slotNoRe = slotNoRe1;
							}
						}
					}
				}
				//if(minElement>transModel[i][j] && transModel[i][j]>-1 && vnodeEmbed[i]==-1 && snodeEmbed[j]==-1){//vnodeEmbed[i] == -1表示虚拟节点i未被映射
				//}
			}
		}
		if(minElent[0] != -1) return ;
		//寻找最小元素
		minElent[0] = minElent[1] = -1;
		minElement = 10000;
		for(int i=0;i<reqs[index].nodes;i++){
			for(int j=0;j<sub.nodes;j++){
				if(minElement>transModel[i][j] && transModel[i][j]>-1 && vnodeEmbed[i]==-1 && snodeEmbed[j]==-1){//vnodeEmbed[i] == -1表示虚拟节点i未被映射
					minElent[0] = i;//minIndexReq = i;
					minElent[1] = j;//minIndexSub = j;
					minElement = transModel[i][j];
				}
			}
		}
	}
	
		
	
	//******************************************************************
	//名称：int InitAllocModel(......)
	//功能：初始化分配模型
	//参数：
	//	      sub为物理网络
	//	      reqs为虚网光网络
	//	      index为第index个虚网请求
	//返回值：     vnodeEmbed为虚拟节点映射模型//-1代表着未分配，>-1代表已经分配
	//        snodeEmbed为物理节点映射模型//-1代表着未分配，>-1代表已经分配
	//        vlinkEmbed为虚拟链路映射模型//-1代表着未分配，>-1代表已经分配
	//******************************************************************
	private void InitAllocModel(EOSubstrateNetwork sub,VONRequest reqs[],int index,int[] vnodeEmbed,int[] snodeEmbed,int[] vlinkEmbed)
	{
		for(int i=0; i<reqs[index].nodes; i++){
			vnodeEmbed[i] = -1;//-1代表着未分配，>-1代表已经分配
		}
		for(int i=0; i<sub.nodes; i++){
			snodeEmbed[i] = -1;//-1代表着未分配，>-1代表已经分配
		}
		for(int i=0; i<reqs[index].links; i++){
			vlinkEmbed[i] = -1;//-1代表着未分配，>-1代表已经分配
		}
	}
	//******************************************************************
	//名称：int InitTranModel(......)
	//功能：初始化运输模型
	//参数：
	//	      sub为物理网络
	//	      sNode为物理节点
	//	      reqs为虚网光网络
	//	      index为第index个虚网请求
	//	      transModel为返回的传输模型
	//        indexModel为返回的传输模型中最小可用的频谱索引
	//返回值：
	//******************************************************************
	private void InitTranModel(EOSubstrateNetwork sub,VONRequest reqs[],int index,double[][] transModel,int[][] slotIndexModel,int[][] linkModel)
	{
		//创建运输模型和最小索引和链路号
		int slotNum = -1;
		int link[] = new int[1];
		for(int i=0;i<reqs[index].nodes;i++){
			for(int j=0;j<sub.nodes;j++){
				if(reqs[index].cpu[i] <= s2v_n[j].rest_cpu + Parameters.MIN_VALUE_DOUBLE){//底层节点的CPU大于虚拟节点
					slotNum = CheckIfSlotEnoughByNode(sub,j,reqs,index,i,link);
					if( slotNum > -1){//如果与底层节点j相连接的链路频谱槽大于虚拟节点i请求的槽
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
	
	private void InitMaxiNodeCostTranModel(EOSubstrateNetwork sub,VONRequest reqs[],int index,double[][] transModel,int[][] slotIndexModel,int[][] linkModel)
	{
		//创建运输模型和最小索引和链路号
		int slotNum = -1;
		int link[] = new int[1];
		for(int i=0;i<reqs[index].nodes;i++){
			for(int j=0;j<sub.nodes;j++){
				if(reqs[index].cpu[i] <= s2v_n[j].rest_cpu + Parameters.MIN_VALUE_DOUBLE){//底层节点的CPU大于虚拟节点
					transModel[i][j] = (s2v_n[j].rest_cpu-reqs[index].cpu[i])/sub.cpu[j];
					/*slotNum = CheckIfSlotEnoughByNode(sub,j,reqs,index,i,link);
					if( slotNum > -1){//如果与底层节点j相连接的链路频谱槽大于虚拟节点i请求的槽
						transModel[i][j] = 1.0/s2v_n[j].rest_cpu;//div(1.0,s2v_n[j].rest_cpu,10);//1.0/(1.0*s2v_n[j].rest_cpu);
						slotIndexModel[i][j] = slotNum;
						linkModel[i][j] = link[0];
					} else {
						transModel[i][j] = -1;//-1代表不能映射
						slotIndexModel[i][j] = -1;
					} */ 
				} else {
					transModel[i][j] = -1;//-1代表不能映射
				}
			}
		}
	}
	
	
}
