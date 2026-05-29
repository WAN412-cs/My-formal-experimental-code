package Team.CloudStorage.EAVONE;


import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.*;
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

import static Team.CloudStorage.EAVONE.Parameters.MapVONEILPByY_L;


public class VONEByLin extends VNE {
	public static  ArrayList<Object> AList=new ArrayList<>();
	public static  ArrayList<Object> SubAListILP=new ArrayList<>();
	public void VONEEmbed(String inSNFile,String inVNsFileDir,int reqsNum,int delay) throws IOException
	{
		//����SN��VNs
		super.VONEEmbed(inSNFile, inVNsFileDir, reqsNum, delay);
		if(Parameters.CurrentVONEMethod == Parameters.MapVONEILPAM915||Parameters.CurrentVONEMethod == Parameters.MapVONEILPAM916){
			ILPSubAList=SubNetGraph1(sub,2);
		}else
		if(Parameters.CurrentVONEMethod == Parameters.MapVONE_ESE){
			AList=EG_Graph(sub);
		}
		if(Parameters.CurrentVONEMethod == Parameters.MapVONE01ILPNodeRank_Sub){
			SubAListILP=SubNetGraph(sub,4,2);

		}if (Parameters.CurrentVONEMethod == Parameters.MapVONEILPAM9166){
		    ILPSubAList=SubNetGraph9166(sub,2);

		}
		//embedModelOrAlgo = Parameters.MapVONETranModel;
    	//V2SEmbed
//		CreateVONE01ILPByY_L(sub,reqs,0);
		V2SEmbed(sub,reqs,delay);//,Parameters.MapVONETranModel
	}
	
	
	/*The algorithm of mapping the VNs.*/
	private void V2SEmbed(EOSubstrateNetwork sub,VONRequest reqs[],int delay) throws IOException
	{
		//embedModelOrAlgo = embedAlgorithm;//ӳ��ģ�ͻ����㷨������embedModelOrAlgo
		int end,n,time,start,sStart;
		time = Parameters.TIME_INTERVAL;
		end = 0;
		n = reqs.length;
		System.out.println("reqs.length:"+n);
		Date startDate = new Date();//��¼ӳ�俪ʼ��ʱ��
	    while (end < n || reqs[n-1].time+delay>time) {   //The value of n is the number of all the VNs.
	        while (end < n && reqs[end].time < time) end++; 
	        for(sStart=0;sStart<n-1 && (reqs[sStart].time+delay)<time;sStart++) ;//˵���ҵ��˵�ǰ��С�Ŀ�ʼ������������
	        //for(sStart=0;reqs[sStart].time<time;sStart++) ;
	        start = sStart;
	        System.out.println("sStart:" + sStart + " end:" + end);
	        
	        //Release the resources�ͷ�[0��end]��Դ
			ReleaseAllResourceAmongZeroToEnd(sub,reqs,end,time);
	        
	        //Set the expire of STATE_EXPIRE.����״̬
	        SetExpireVNState(reqs,end,time,delay);
	                
	        //Allocate the resources.
	        AllocateResources(sub,reqs,start, end);
			Recordxiumian(sub);
			calculateCpu(sub);
			CalculateEnergyConsumption(sub,reqs,end,time);//�����ܺ�̼�ŷ�
	        time += Parameters.TIME_INTERVAL;  //ʱ�䴰����һ����λ

	    }
	    Date endDate = new Date();//��¼ӳ�俪ʼ��ʱ��
	    long interval = (endDate.getTime() - startDate.getTime())/1000;//��¼ʱ���룩

	    //��¼��Ϣ������ϵͳ���桢�����ʡ�����ɱ��ȡ�������Ƭ�����Ѷ��壬�����������ΪС��2�������Ŀ���SlotsΪ��Ƭ��
	    if(Parameters.DebugModel){
	    	System.out.println("RecordResultsOfVNE.");
	    }
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
		double sumOtherVirtualCPU = 0;
		for(int i=start;i<end;i++){
			if(v2s[i].map == Parameters.STATE_NEW || v2s[i].map == Parameters.STATE_MAP_NODE_FAIL || v2s[i].map == Parameters.STATE_MAP_FAIL || v2s[i].map == Parameters.STATE_MAP_Link_FAIL) {
				ArrayList<Object> list = new ArrayList<Object>();  //��¼�ڵ�ӳ����
				int p[][] = new int[reqs[i].links][sub.nodes];
				int ret[][] = new int[reqs[i].links][4];
				//ret[][0]:����������·ӳ���������㣻ret[][1]:����������·ӳ��������յ�
				//ret[][2]:���ص���ʼƵ�ײۣ�ret[][3]:���ص�Ƶ�ײ�����
				v2s[i].tryMapTime ++;	//��¼ӳ�����	
				if(Parameters.RecordLogModel) WriteFilePlus("process.txt","req["+i+"] allocation in "+Parameters.CurrentVONEMethod);
				if(reqs[i].topo == Parameters.TOPO_GENERAL || reqs[i].topo == Parameters.TOPO_STAR) {					
					if(Parameters.CurrentVONEMethod == Parameters.MapVONECXHNode){
						DebugVNE(sub,reqs,i,Parameters.CurrentVONEMethod,"before embed req "+i);
						if(MapVONEByLinChenMaxNodeCost(sub, reqs, i)!=-1){
							if(Parameters.DebugModel) Print_sub_slots(sub);
							v2s[i].map = Parameters.STATE_MAP_SUCC;
							reqs[i].map = Parameters.STATE_MAP_SUCC;
							DebugVNE(sub,reqs,i,Parameters.CurrentVONEMethod,"after embed succ req "+i);
						} else {
							v2s[i].map = Parameters.STATE_MAP_NODE_FAIL;
							reqs[i].map = Parameters.STATE_MAP_NODE_FAIL;
							DebugVNE(sub,reqs,i,Parameters.CurrentVONEMethod,"after embed fail req "+i);
						}
					} else if(Parameters.CurrentVONEMethod == Parameters.MapVONELin_SortByNodeDegree || Parameters.CurrentVONEMethod == Parameters.MapVONELin_SortByNodeDegreeAndBW || Parameters.CurrentVONEMethod == Parameters.MapVONELin_SortByBW){
						DebugVNE(sub,reqs,i,Parameters.CurrentVONEMethod,"before embed req "+i);
						if(MapVONEByLin(sub, reqs, i)!=-1){
							if(Parameters.DebugModel) Print_sub_slots(sub);
							v2s[i].map = Parameters.STATE_MAP_SUCC;
							reqs[i].map = Parameters.STATE_MAP_SUCC;
							DebugVNE(sub,reqs,i,Parameters.CurrentVONEMethod,"after embed succ req "+i);
						} else {
							v2s[i].map = Parameters.STATE_MAP_NODE_FAIL;
							reqs[i].map = Parameters.STATE_MAP_NODE_FAIL;
							DebugVNE(sub,reqs,i,Parameters.CurrentVONEMethod,"after embed fail req "+i);
						}
					} else if(Parameters.CurrentVONEMethod == Parameters.MapVONELin_FB_SortByNodeDegree || Parameters.CurrentVONEMethod == Parameters.MapVONELin_FB_SortByNodeDegreeAndBW || Parameters.CurrentVONEMethod == Parameters.MapVONELin_FB_SortByBW){
						if(MapVONEFBByLinCXH(sub, reqs, i)!=-1){
							if(Parameters.DebugModel) Print_sub_slots(sub);
							v2s[i].map = Parameters.STATE_MAP_SUCC;
							reqs[i].map = Parameters.STATE_MAP_SUCC;
							DebugVNE(sub,reqs,i,Parameters.CurrentVONEMethod,"after embed succ req "+i);
						} else {
							v2s[i].map = Parameters.STATE_MAP_NODE_FAIL;
							reqs[i].map = Parameters.STATE_MAP_NODE_FAIL;
							DebugVNE(sub,reqs,i,Parameters.CurrentVONEMethod,"after embed fail req "+i);
						}
						
					}else if (Parameters.CurrentVONEMethod ==Parameters.MapVONEILPByY_L){
						if(MapVONEILPByY_L(sub, reqs, i)!=-1){
							if(Parameters.DebugModel) Print_sub_slots(sub);
							v2s[i].map = Parameters.STATE_MAP_SUCC;
							reqs[i].map = Parameters.STATE_MAP_SUCC;
							DebugVNE(sub,reqs,i,Parameters.CurrentVONEMethod,"after embed succ req "+i);
						} else {
							v2s[i].map = Parameters.STATE_MAP_NODE_FAIL;
							reqs[i].map = Parameters.STATE_MAP_NODE_FAIL;
							DebugVNE(sub,reqs,i,Parameters.CurrentVONEMethod,"after embed fail req "+i);
						}
					}
					else if(Parameters.CurrentVONEMethod == Parameters.MapVONE01ILPLin || Parameters.CurrentVONEMethod == Parameters.MapVONE01ILPLin_EquilibriumCXH||Parameters.CurrentVONEMethod == Parameters.MapVONE01ILPPRLinCXH||Parameters.CurrentVONEMethod == Parameters.MapVONE01ILPLinCXH || Parameters.CurrentVONEMethod == Parameters.MapVONECXHNode||Parameters.CurrentVONEMethod == Parameters.MapVONENCRbyILP||Parameters.CurrentVONEMethod == Parameters.MapVONEILPAM){
						DebugVNE(sub,reqs,i,Parameters.CurrentVONEMethod,"before embed req "+i);
						if(MapVONE01ILPByLin(sub, reqs, i)!=-1){//MapVONE01ILPByLin
							if(Parameters.DebugModel) Print_sub_slots(sub);
							v2s[i].map = Parameters.STATE_MAP_SUCC;
							reqs[i].map = Parameters.STATE_MAP_SUCC;
							DebugVNE(sub,reqs,i,Parameters.CurrentVONEMethod,"after embed succ req "+i);
						} else {
							v2s[i].map = Parameters.STATE_MAP_NODE_FAIL;
							reqs[i].map = Parameters.STATE_MAP_NODE_FAIL;
							DebugVNE(sub,reqs,i,Parameters.CurrentVONEMethod,"after embed fail req "+i);
						}
					}
					else if(Parameters.CurrentVONEMethod == Parameters.MapVONENCRbyILPnodeilp ){
						DebugVNE(sub,reqs,i,Parameters.CurrentVONEMethod,"before embed req "+i);
						if(MapVONE01ILPByLin_6(sub, reqs, i)!=-1){//MapVONE01ILPByLin
							if(Parameters.DebugModel) Print_sub_slots(sub);
							v2s[i].map = Parameters.STATE_MAP_SUCC;
							reqs[i].map = Parameters.STATE_MAP_SUCC;
							DebugVNE(sub,reqs,i,Parameters.CurrentVONEMethod,"after embed succ req "+i);
						} else {
							v2s[i].map = Parameters.STATE_MAP_NODE_FAIL;
							reqs[i].map = Parameters.STATE_MAP_NODE_FAIL;
							DebugVNE(sub,reqs,i,Parameters.CurrentVONEMethod,"after embed fail req "+i);
						}
					}
					else if(Parameters.CurrentVONEMethod == Parameters.  MapVONE01ILPLinnodeilp ) {
						DebugVNE(sub, reqs, i, Parameters.CurrentVONEMethod, "before embed req " + i);
						if (MapVONE01ILPByLin_5(sub, reqs, i) != -1) {//MapVONE01ILPByLin
							if (Parameters.DebugModel) Print_sub_slots(sub);
							v2s[i].map = Parameters.STATE_MAP_SUCC;
							reqs[i].map = Parameters.STATE_MAP_SUCC;
							DebugVNE(sub, reqs, i, Parameters.CurrentVONEMethod, "after embed succ req " + i);
						} else {
							v2s[i].map = Parameters.STATE_MAP_NODE_FAIL;
							reqs[i].map = Parameters.STATE_MAP_NODE_FAIL;
							DebugVNE(sub, reqs, i, Parameters.CurrentVONEMethod, "after embed fail req " + i);
						}
					}else if(Parameters.CurrentVONEMethod == Parameters.MapVONEILPAM916 ){
                        DebugVNE(sub,reqs,i,Parameters.CurrentVONEMethod,"before embed req "+i);
                        if(MapVONE01ILPByLin_3(sub, reqs, i)!=-1){//MapVONE01ILPByLin
                            if(Parameters.DebugModel) Print_sub_slots(sub);
                            v2s[i].map = Parameters.STATE_MAP_SUCC;
                            reqs[i].map = Parameters.STATE_MAP_SUCC;
                            DebugVNE(sub,reqs,i,Parameters.CurrentVONEMethod,"after embed succ req "+i);
                        } else {
                            v2s[i].map = Parameters.STATE_MAP_NODE_FAIL;
                            reqs[i].map = Parameters.STATE_MAP_NODE_FAIL;
                            DebugVNE(sub,reqs,i,Parameters.CurrentVONEMethod,"after embed fail req "+i);
                        }
                    } else if(Parameters.CurrentVONEMethod == Parameters.MapVONEILPAM9166 ){
						DebugVNE(sub,reqs,i,Parameters.CurrentVONEMethod,"before embed req "+i);
						if(MapVONE01ILPByLin9166(sub, reqs, i,sumOtherVirtualCPU)!=-1){//MapVONE01ILPByLin
							if(Parameters.DebugModel) Print_sub_slots(sub);
							v2s[i].map = Parameters.STATE_MAP_SUCC;
							reqs[i].map = Parameters.STATE_MAP_SUCC;
							DebugVNE(sub,reqs,i,Parameters.CurrentVONEMethod,"after embed succ req "+i);
						} else {
							v2s[i].map = Parameters.STATE_MAP_NODE_FAIL;
							reqs[i].map = Parameters.STATE_MAP_NODE_FAIL;
							DebugVNE(sub,reqs,i,Parameters.CurrentVONEMethod,"after embed fail req "+i);
						}
					} else if(Parameters.CurrentVONEMethod == Parameters. MapVONE01ILPLin_EquilibriumCXHnodeilp ){
						DebugVNE(sub,reqs,i,Parameters.CurrentVONEMethod,"before embed req "+i);
						if(MapVONE01ILPByLin_4(sub, reqs, i)!=-1){//MapVONE01ILPByLin
							if(Parameters.DebugModel) Print_sub_slots(sub);
							v2s[i].map = Parameters.STATE_MAP_SUCC;
							reqs[i].map = Parameters.STATE_MAP_SUCC;
							DebugVNE(sub,reqs,i,Parameters.CurrentVONEMethod,"after embed succ req "+i);
						} else {
							v2s[i].map = Parameters.STATE_MAP_NODE_FAIL;
							reqs[i].map = Parameters.STATE_MAP_NODE_FAIL;
							DebugVNE(sub,reqs,i,Parameters.CurrentVONEMethod,"after embed fail req "+i);
						}
					}else if(Parameters.CurrentVONEMethod == Parameters.MapVONEILPAM915){
						DebugVNE(sub,reqs,i,Parameters.CurrentVONEMethod,"before embed req "+i);
						if(MapVONE01ILPByLin915(sub, reqs, i,sumOtherVirtualCPU)!=-1){
							if(Parameters.DebugModel) Print_sub_slots(sub);
							v2s[i].map = Parameters.STATE_MAP_SUCC;
							reqs[i].map = Parameters.STATE_MAP_SUCC;
							DebugVNE(sub,reqs,i,Parameters.CurrentVONEMethod,"after embed succ req "+i);
						} else {
							v2s[i].map = Parameters.STATE_MAP_NODE_FAIL;
							reqs[i].map = Parameters.STATE_MAP_NODE_FAIL;
							DebugVNE(sub,reqs,i,Parameters.CurrentVONEMethod,"after embed fail req "+i);
						}
					}else if(Parameters.CurrentVONEMethod == Parameters.MapVONE01ILPLinFB){
						DebugVNE(sub,reqs,i,Parameters.CurrentVONEMethod,"before embed req "+i);
						if(MapVONE01ILPByLinFB(sub, reqs, i)!=-1){
							if(Parameters.DebugModel) Print_sub_slots(sub);
							v2s[i].map = Parameters.STATE_MAP_SUCC;
							reqs[i].map = Parameters.STATE_MAP_SUCC;
							DebugVNE(sub,reqs,i,Parameters.CurrentVONEMethod,"after embed succ req "+i);
						} else {
							v2s[i].map = Parameters.STATE_MAP_NODE_FAIL;
							reqs[i].map = Parameters.STATE_MAP_NODE_FAIL;
							DebugVNE(sub,reqs,i,Parameters.CurrentVONEMethod,"after embed fail req "+i);
						}
					} else if(Parameters.CurrentVONEMethod == Parameters.MapVONE01ILPLinStrong){
						DebugVNE(sub,reqs,i,Parameters.CurrentVONEMethod,"before embed req "+i);
						if(MapVONE01ILPByLinStrong(sub, reqs, i)!=-1){
							if(Parameters.DebugModel) Print_sub_slots(sub);
							v2s[i].map = Parameters.STATE_MAP_SUCC;
							reqs[i].map = Parameters.STATE_MAP_SUCC;
							DebugVNE(sub,reqs,i,Parameters.CurrentVONEMethod,"after embed succ req "+i);
						} else {
							v2s[i].map = Parameters.STATE_MAP_NODE_FAIL;
							reqs[i].map = Parameters.STATE_MAP_NODE_FAIL;
							DebugVNE(sub,reqs,i,Parameters.CurrentVONEMethod,"after embed fail req "+i);
						}
					}else if(Parameters.CurrentVONEMethod == Parameters.MapVONE01ILPNodeRank_Sub ) {
						DebugVNE(sub, reqs, i, Parameters.CurrentVONEMethod, "before embed req " + i);
						if (MapVONE01ILPBySub(sub, reqs, i) != -1) {
							if (Parameters.DebugModel) Print_sub_slots(sub);
							v2s[i].map = Parameters.STATE_MAP_SUCC;
							reqs[i].map = Parameters.STATE_MAP_SUCC;
							DebugVNE(sub, reqs, i, Parameters.CurrentVONEMethod, "after embed succ req " + i);
						} else {
							v2s[i].map = Parameters.STATE_MAP_NODE_FAIL;
							reqs[i].map = Parameters.STATE_MAP_NODE_FAIL;
							DebugVNE(sub, reqs, i, Parameters.CurrentVONEMethod, "after embed fail req " + i);
						}

					}
				}
			}
		}
	}

	private void AllocateResources_ILP(EOSubstrateNetwork sub,VONRequest reqs[],int start,int end) throws IOException
	{
		System.out.println("start:" + start + " end:" + end);
		for(int i=start;i<end;i++){
			if(v2s[i].map == Parameters.STATE_NEW || v2s[i].map == Parameters.STATE_MAP_NODE_FAIL || v2s[i].map == Parameters.STATE_MAP_FAIL || v2s[i].map == Parameters.STATE_MAP_Link_FAIL) {
				ArrayList<Object> list = new ArrayList<Object>();  //��¼�ڵ�ӳ����
				int p[][] = new int[reqs[i].links][sub.nodes];
				int ret[][] = new int[reqs[i].links][4];
				//ret[][0]:����������·ӳ���������㣻ret[][1]:����������·ӳ��������յ�
				//ret[][2]:���ص���ʼƵ�ײۣ�ret[][3]:���ص�Ƶ�ײ�����
				v2s[i].tryMapTime ++;	//��¼ӳ�����
				if(Parameters.RecordLogModel) WriteFilePlus("process.txt","req["+i+"] allocation in "+Parameters.CurrentVONEMethod);
				if(reqs[i].topo == Parameters.TOPO_GENERAL || reqs[i].topo == Parameters.TOPO_STAR) {
					if(Parameters.CurrentVONEMethod == Parameters.MapVONECXHNode){
						DebugVNE(sub,reqs,i,Parameters.CurrentVONEMethod,"before embed req "+i);
						if(MapVONEByLinChenMaxNodeCost(sub, reqs, i)!=-1){
							if(Parameters.DebugModel) Print_sub_slots(sub);
							v2s[i].map = Parameters.STATE_MAP_SUCC;
							reqs[i].map = Parameters.STATE_MAP_SUCC;
							DebugVNE(sub,reqs,i,Parameters.CurrentVONEMethod,"after embed succ req "+i);
						} else {
							v2s[i].map = Parameters.STATE_MAP_NODE_FAIL;
							reqs[i].map = Parameters.STATE_MAP_NODE_FAIL;
						}
					} else if(Parameters.CurrentVONEMethod == Parameters.MapVONELin_SortByNodeDegree || Parameters.CurrentVONEMethod == Parameters.MapVONELin_SortByNodeDegreeAndBW || Parameters.CurrentVONEMethod == Parameters.MapVONELin_SortByBW){
						DebugVNE(sub,reqs,i,Parameters.CurrentVONEMethod,"before embed req "+i);
						if(MapVONEByLin(sub, reqs, i)!=-1){
							if(Parameters.DebugModel) Print_sub_slots(sub);
							v2s[i].map = Parameters.STATE_MAP_SUCC;
							reqs[i].map = Parameters.STATE_MAP_SUCC;
							DebugVNE(sub,reqs,i,Parameters.CurrentVONEMethod,"after embed succ req "+i);
						} else {
							v2s[i].map = Parameters.STATE_MAP_NODE_FAIL;
							reqs[i].map = Parameters.STATE_MAP_NODE_FAIL;
						}
					} else if(Parameters.CurrentVONEMethod == Parameters.MapVONELin_FB_SortByNodeDegree || Parameters.CurrentVONEMethod == Parameters.MapVONELin_FB_SortByNodeDegreeAndBW || Parameters.CurrentVONEMethod == Parameters.MapVONELin_FB_SortByBW){
						if(MapVONEFBByLinCXH(sub, reqs, i)!=-1){
							if(Parameters.DebugModel) Print_sub_slots(sub);
							v2s[i].map = Parameters.STATE_MAP_SUCC;
							reqs[i].map = Parameters.STATE_MAP_SUCC;
							DebugVNE(sub,reqs,i,Parameters.CurrentVONEMethod,"after embed succ req "+i);
						} else {
							v2s[i].map = Parameters.STATE_MAP_NODE_FAIL;
							reqs[i].map = Parameters.STATE_MAP_NODE_FAIL;
						}

					} else if(Parameters.CurrentVONEMethod == Parameters.MapVONE01ILPLin || Parameters.CurrentVONEMethod == Parameters.MapVONE01ILPLin_EquilibriumCXH||Parameters.CurrentVONEMethod == Parameters.MapVONE01ILPPRLinCXH||Parameters.CurrentVONEMethod == Parameters.MapVONE01ILPLinCXH || Parameters.CurrentVONEMethod == Parameters.MapVONECXHNode){
						DebugVNE(sub,reqs,i,Parameters.CurrentVONEMethod,"before embed req "+i);
						if(MapVONE01ILPByLin(sub, reqs, i)!=-1){
							if(Parameters.DebugModel) Print_sub_slots(sub);
							v2s[i].map = Parameters.STATE_MAP_SUCC;
							reqs[i].map = Parameters.STATE_MAP_SUCC;
							DebugVNE(sub,reqs,i,Parameters.CurrentVONEMethod,"after embed succ req "+i);
						} else {
							v2s[i].map = Parameters.STATE_MAP_NODE_FAIL;
							reqs[i].map = Parameters.STATE_MAP_NODE_FAIL;
						}
					} else if(Parameters.CurrentVONEMethod == Parameters.MapVONE01ILPLinFB){
						DebugVNE(sub,reqs,i,Parameters.CurrentVONEMethod,"before embed req "+i);
						if(MapVONE01ILPByLinFB(sub, reqs, i)!=-1){
							if(Parameters.DebugModel) Print_sub_slots(sub);
							v2s[i].map = Parameters.STATE_MAP_SUCC;
							reqs[i].map = Parameters.STATE_MAP_SUCC;
							DebugVNE(sub,reqs,i,Parameters.CurrentVONEMethod,"after embed succ req "+i);
						} else {
							v2s[i].map = Parameters.STATE_MAP_NODE_FAIL;
							reqs[i].map = Parameters.STATE_MAP_NODE_FAIL;
						}
					} else if(Parameters.CurrentVONEMethod == Parameters.MapVONE01ILPLinStrong){
						DebugVNE(sub,reqs,i,Parameters.CurrentVONEMethod,"before embed req "+i);
						if(MapVONE01ILPByLinStrong(sub, reqs, i)!=-1){
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
	public static  ArrayList<Object> ILPSubAList=new ArrayList<>();
	/*private int MapVONE01ILPByLin915(EOSubstrateNetwork sub, VONRequest reqs[], int index) {
		int method = 0;
		boolean triedAllNodes = false; // ����Ƿ��������нڵ�

		while (true) {
			// ������ͼ
			int mappingResult = MapVONE01ILPByLin915_1(sub, reqs, index, method);
			if (mappingResult == 0) {
				// ӳ��ɹ�������0
				System.out.println("embed reqs " + index + " successfully after using all nodes.");
				return 0;
			}
			ILPSubAList = SubNetGraph2(sub, ILPSubAList);
			List<Integer> sum = (List<Integer>) ILPSubAList.get(method);
			int currentNodesUsed = sum.size();

			// ����Ƿ��Ѿ�ʹ�������нڵ�
			if (currentNodesUsed >= sub.nodes) {
				triedAllNodes = true; // ���Ϊ�ѳ������нڵ�
			}

			// ����ӳ��

			// ����Ѿ����������нڵ㲢��ӳ��ʧ�ܣ����˳�ѭ��
			if (triedAllNodes) {
				break;
			}
		}

		// ѭ�����������нڵ㶼�ѳ���ʹ�ã���ӳ��ʧ��
		System.out.println("Failed to embed reqs " + index + " after using all nodes.");
		return -1;
	}*/
	private int MapVONE01ILPByLin915(EOSubstrateNetwork sub,VONRequest reqs[],int index,double sumOtherVirtualCPU)
	{
		int method = 0;
		boolean triedAllNodes = false;
		while(MapVONE01ILPByLin915_1(sub,reqs,index,method) == -1 && triedAllNodes==false) {//ֻҪû��ӳ��ɹ�������ͼ��
			ILPSubAList=SubNetGraph2(sub,ILPSubAList ,index,sumOtherVirtualCPU);
			List<Integer> sum=(List<Integer>)ILPSubAList.get(method);
			int jishu=0;
			for(int i=0;i< sum.size();i++){
				jishu++;
			}

			if (jishu>=sub.nodes){
				triedAllNodes=true;
				int mappingResult = MapVONE01ILPByLin915_1(sub, reqs, index, method);
				if (mappingResult == 0) {
				// ӳ��ɹ�������0
				System.out.println("embed reqs " + index + " successfully after using all nodes.");
				return 0;
			}else{
					return -1;//ӳ��ʧ��
				}
			}

		}
		return 0;
	}


	private int MapVONE01ILPByLin915_1(EOSubstrateNetwork sub,VONRequest reqs[],int index,int method)
	{
		List<Integer> ilpsubnet=(List<Integer>)ILPSubAList.get(method);
		double[] energysNodePageRank =(double[]) ILPSubAList.get(1);
		//��Lin��01ILPģ�����
		CreateVONE01ILPByLinUndirectDiagramSub1(sub,reqs,index,ilpsubnet,energysNodePageRank);
		//��WangY��ILPģ����⣬���߸�����ǿ�Ļ���·����ӳ��ģ�����
		//FindVONEWangYPlusByOne01ILP(auxGraph,kShortestPath,pathSlots,pathLength,pathNo,pathEff,reqs,index);
		int retNodeE[];
		LinkedList[] retVLinkE;
		retVLinkE = new LinkedList[reqs[index].links];
		retNodeE = new int[reqs[index].nodes];
		if(FindOptSoluByLin(sub,reqs,index,retNodeE,retVLinkE)){//�ҵ������Ž�
			//System.out.println("������Ƶ�ײ۳�ͻ");
			//PrintPath(reqs,index,kShortestPath,p,auxGraph.virtualNodes,pathEff,retLinkE,retSlotSE,retSlotEE);
			AddNodesMap(reqs,index,retNodeE);//�ڵ�ӳ��
			AddNodesMapSub(sub,reqs,index,retNodeE);//�ڵ�ӳ���޸�sub.cpu
			AddLinksMapBy01ILPLin(sub,reqs,index,retNodeE,retVLinkE);

			System.out.println("embed reqs "+ index+" successfully");
			return 0;//�ɹ��ҵ�VONE��
		}

		return -1;
	}
	private int MapVONE01ILPByLin915_2(EOSubstrateNetwork sub,VONRequest reqs[],int index,int method) {
		List<Integer> ilpsubnet = (List<Integer>) ILPSubAList.get(method);
		double[] energysNodePageRank = (double[]) ILPSubAList.get(1);
		CreateVONE01ILPByLinUndirectDiagramSub2(sub, reqs, index, ilpsubnet, energysNodePageRank);
		int retNodeE[];
		LinkedList[] retVLinkE;
		retVLinkE = new LinkedList[reqs[index].links];
		retNodeE = new int[reqs[index].nodes];
		if(FindOptSoluBynodes(sub, reqs, index, retNodeE)){
			CreateVONE01ILPByLinUndirectDiagram3(sub, reqs, index,retNodeE, energysNodePageRank);
			if (FindOptSoluBylink(sub, reqs, index, retNodeE, retVLinkE)) {//�ң��������Ž�
				//System.out.println("������Ƶ�ײ۳�ͻ");
				//PrintPath(reqs,index,kShortestPath,p,auxGraph.virtualNodes,pathEff,retLinkE,retSlotSE,retSlotEE);
				AddNodesMap(reqs, index, retNodeE);//�ڵ�ӳ��
				AddNodesMapSub(sub, reqs, index, retNodeE);//�ڵ�ӳ���޸�sub.cpu
				AddLinksMapBy01ILPLin(sub, reqs, index, retNodeE, retVLinkE);

				System.out.println("embed reqs " + index + " successfully");
				return 0;//�ɹ��ҵ�VONE��
			}
		}
		return -1;
	}
	public ArrayList<Object> SubNetGraph2(EOSubstrateNetwork sub, ArrayList<Object> resultFromSubNetGraph1,int	index,double sumOtherVirtualCPU) {
		// ��ȡ���е��������б��������������
		ArrayList<Integer> SubNet1 = (ArrayList<Integer>) resultFromSubNetGraph1.get(0);
		double[]  energysNodePageRank  = (double[]) resultFromSubNetGraph1.get(1);

		// ���³�ʼ���������Ի�ȡ���µ���������
		int[] nSortSNode = new int[sub.nodes];
		double sNodeAM1[] = new double[sub.nodes];
		if(Parameters.CurrentVONEMethod == Parameters.MapVONEILPAM9166){
			sNodeAM1= InitSNodeAMnew2(sNodeAM1, sub, index,sumOtherVirtualCPU);
//			for (int i = 0; i < sub.nodes; i++) {
//					sNodeAM1[i] = energysNodePageRank[i];
//			}
		}else{
			sNodeAM1= InitSNodeAM(sNodeAM1, sub);
		}
		sortA(sNodeAM1,nSortSNode);


		// Ѱ��Ҫ��ӵ��½ڵ�
		for(int i=0;i<=0;i++) {
			int nextNodeToAdd = findNextNodeToAdd(nSortSNode, SubNet1);
			if (nextNodeToAdd != -1) {
				SubNet1.add(nextNodeToAdd); // ����½ڵ㵽�������б�
			}
			// ����������������
			for (int j = 0; j < sNodeAM1.length; j++) {
				energysNodePageRank[j] = sNodeAM1[j];
			}
            if(SubNet1.size()>=sub.nodes){
				break;
			}
		}
		// ֱ��ʹ�����е��б�����飬����Ҫ�ٴδ��� SubNetObject
		// SubAList.add(obj.SubNet1);
		// SubAList.add(obj.energysNodePageRank);

		// ȷ�����ص��б�������µ��������б��������������
		resultFromSubNetGraph1.set(0, SubNet1);
		resultFromSubNetGraph1.set(1, energysNodePageRank);

		return resultFromSubNetGraph1; // ���ظ��º���б�
	}

	// ���������������ҵ�Ҫ��ӵ���һ���ڵ�
	private int findNextNodeToAdd(int[] nSortSNode, ArrayList<Integer> SubNet1) {
		for (int sortedNode : nSortSNode) {
			// ���sortedNode�Ƿ���SubNet1��

			if (!SubNet1.contains(sortedNode)) {
				// ���sortedNode��SubNet1�е���һ�ڵ������ӣ��򷵻�����ڵ�
				if (isNodeConnectedToSubNet(sortedNode, SubNet1)) {
					return sortedNode;
				}
			}
		}
		return -1; // ���û���ҵ����������Ľڵ㣬����-1
	}
	private boolean isNodeConnectedToSubNet(int node, ArrayList<Integer> SubNet1) {
		for (int subNetNode : SubNet1) {
			for (int k = 0; k < sub.links; k++) {
				// ���node�Ƿ���SubNet1�еĽڵ�������
				if ((node == sub.link[k].from && SubNet1.contains(sub.link[k].to)) ||
						(node == sub.link[k].to && SubNet1.contains(sub.link[k].from))) {
					return true; // �ҵ����ӣ�����true
				}
			}
		}
		return false; // ���û���ҵ����ӣ�����false
	}
	public ArrayList<Object> SubNetGraph9166 (EOSubstrateNetwork sub, int FirstNetNumber){
		ArrayList<Object> SubAList=new ArrayList<>();//����һ����ά����ArrayList���洢���������
		class SubNetObject {
			public List<Integer> SubNet1;//��һ���������б�
			public  double[] energysNodePageRank;//�״�NRֵ
			public SubNetObject(List<Integer> SubNet1, double[] energysNodePageRank) {
				this.SubNet1 = SubNet1;
				this.energysNodePageRank = energysNodePageRank;
			}
		}
		List<Integer> SubNet1=new ArrayList<>();
		double[] energysNodePageRank=new double[sub.nodes];
		SubNetObject obj = new SubNetObject(SubNet1, energysNodePageRank);
		//��ȡ�ײ�ڵ��NodeRankֵ
		double sNodeAM[] = new double[sub.nodes];
		// InitVNodePageRank(reqs,index);

		int nSortSNode[] = new int[sub.nodes];

//    vNodeAM=InitVNodePageRankOfGHG(vNodeAM,reqs,index);
		if(Parameters.CurrentVONEMethod == Parameters.MapVONEILPAM9166){
			sNodeAM= InitSNodeAMnew(sNodeAM, sub);
		}else{
			sNodeAM= InitSNodeAM(sNodeAM, sub);
		}


		sortA(sNodeAM,nSortSNode);
		//���״�NRֵ
		for(int i=0;i<sNodeAM.length;i++){
			energysNodePageRank[i]=sNodeAM[i];
		}
		obj.energysNodePageRank=energysNodePageRank;
		//��ȡ��һ���������׵�
		SubNet1.add(nSortSNode[0]);
		//��һ���������������noderankֵ�����Ľڵ㣬��һ�������ڵ㣬��noderankֵ�������
		for(int i=1;i<nSortSNode.length;i++){
			for(int j=0;j<sub.links;j++){
				if(((nSortSNode[0]==sub.link[j].from&&nSortSNode[i]==sub.link[j].to)||(nSortSNode[i]==sub.link[j].from&&nSortSNode[0]==sub.link[j].to))&&SubNet1.size()<FirstNetNumber&&!SubNet1.contains(nSortSNode[i])){
					SubNet1.add(nSortSNode[i]);
				}
			}
		}
		//copy���еĵ�һ�������糤��
		int SubNet1length=SubNet1.size();
		for(int i=0;i<nSortSNode.length;i++){
			if(NodeToNodeConnect(0,i,SubNet1length,SubNet1,nSortSNode)){
				if(SubNet1.size()<FirstNetNumber&&!SubNet1.contains(nSortSNode[i])){
					SubNet1.add(nSortSNode[i]);
				}
			}
		}
		obj.SubNet1=SubNet1;

		SubNet1length=SubNet1.size();
		//��һ�������������һ��ڵ������Ľڵ㣬���õڶ��������ڵ�
		SubAList.add(obj.SubNet1);
		SubAList.add(obj.energysNodePageRank);
		return SubAList;
	}

	public ArrayList<Object> SubNetGraph1 (EOSubstrateNetwork sub, int FirstNetNumber){
		ArrayList<Object> SubAList=new ArrayList<>();//����һ����ά����ArrayList���洢���������
		class SubNetObject {
			public List<Integer> SubNet1;//��һ���������б�
			public  double[] energysNodePageRank;//�״�NRֵ
			public SubNetObject(List<Integer> SubNet1, double[] energysNodePageRank) {
				this.SubNet1 = SubNet1;
				this.energysNodePageRank = energysNodePageRank;
			}
		}
		List<Integer> SubNet1=new ArrayList<>();
		double[] energysNodePageRank=new double[sub.nodes];
		SubNetObject obj = new SubNetObject(SubNet1, energysNodePageRank);
		//��ȡ�ײ�ڵ��NodeRankֵ
		double sNodeAM[] = new double[sub.nodes];
		// InitVNodePageRank(reqs,index);

		int nSortSNode[] = new int[sub.nodes];

//    vNodeAM=InitVNodePageRankOfGHG(vNodeAM,reqs,index);
			sNodeAM= InitSNodeAM(sNodeAM, sub);



		sortA(sNodeAM,nSortSNode);
		//���״�NRֵ
		for(int i=0;i<sNodeAM.length;i++){
			energysNodePageRank[i]=sNodeAM[i];
		}
		obj.energysNodePageRank=energysNodePageRank;
		//��ȡ��һ���������׵�
		SubNet1.add(nSortSNode[0]);
		//��һ���������������noderankֵ�����Ľڵ㣬��һ�������ڵ㣬��noderankֵ�������
		for(int i=1;i<nSortSNode.length;i++){
			for(int j=0;j<sub.links;j++){
				if(((nSortSNode[0]==sub.link[j].from&&nSortSNode[i]==sub.link[j].to)||(nSortSNode[i]==sub.link[j].from&&nSortSNode[0]==sub.link[j].to))&&SubNet1.size()<FirstNetNumber&&!SubNet1.contains(nSortSNode[i])){
					SubNet1.add(nSortSNode[i]);
				}
			}
		}
		//copy���еĵ�һ�������糤��
		int SubNet1length=SubNet1.size();
		for(int i=0;i<nSortSNode.length;i++){
			if(NodeToNodeConnect(0,i,SubNet1length,SubNet1,nSortSNode)){
				if(SubNet1.size()<FirstNetNumber&&!SubNet1.contains(nSortSNode[i])){
					SubNet1.add(nSortSNode[i]);
				}
			}
		}
		obj.SubNet1=SubNet1;

		SubNet1length=SubNet1.size();
		//��һ�������������һ��ڵ������Ľڵ㣬���õڶ��������ڵ�
		SubAList.add(obj.SubNet1);
		SubAList.add(obj.energysNodePageRank);
		return SubAList;
	}
	private static void sortA(double[] a, int[] b) {
		Integer[] temp = new Integer[a.length];
		for (int i = 0; i < temp.length; i++) {
			temp[i] = i;
		}
		Arrays.sort(temp, new Comparator<Integer>() {
			@Override
			public int compare(Integer o1, Integer o2) {
				return Double.compare(a[o1], a[o2]);
			}
		});
		for (int i = 0; i < temp.length; i++) {
			b[i] = temp[i];
		}
	}
	public ArrayList<Object> SubNetGraph (EOSubstrateNetwork sub, int FirstNetNumber, int SecondNetNumber){
		ArrayList<Object> SubAList=new ArrayList<>();//����һ����ά����ArrayList���洢���������
		class SubNetObject {
			public List<Integer> SubNet1;//��һ���������б�
			public List<Integer> SubNet2;//�ڶ����������б�
			public  List<Integer> SubNetsort;//��ȥ����������ʣ�µ�NRֵ����
			public  double[] energysNodePageRank;//�״�NRֵ
			public SubNetObject(List<Integer> SubNet1, List<Integer> SubNet2, List<Integer> SubNetsort ,double[] energysNodePageRank) {
				this.SubNet1 = SubNet1;
				this.SubNet2 = SubNet2;
				this.SubNetsort = SubNetsort;
				this.energysNodePageRank = energysNodePageRank;
			}
		}
		//nt FirstNetNumber=8;//��һ�����������ڵ�����
		//int SecondNetNumber=8;//�ڶ������������ڵ�����
		List<Integer> SubNet1=new ArrayList<>();
		List<Integer> SubNet2=new ArrayList<>();
		List<Integer> SubNetsort=new ArrayList<>();
		double[] energysNodePageRank=new double[sub.nodes];
		SubNetObject obj = new SubNetObject(SubNet1, SubNet2, SubNetsort, energysNodePageRank);
		//��ȡ�ײ�ڵ��NodeRankֵ
		double sNodePageRank[] = new double[sub.nodes];
		int nSortSNode[] = new int[sub.nodes];
		sNodePageRank= InitSNodeEnergyPageRank(sNodePageRank, sub);
		sort(sNodePageRank,nSortSNode);
		//���״�NRֵ
		for(int i=0;i<sNodePageRank.length;i++){
			energysNodePageRank[i]=sNodePageRank[i];
		}
		obj.energysNodePageRank=energysNodePageRank;
		//��ȡ��һ���������׵�
		SubNet1.add(nSortSNode[0]);
		//��һ���������������noderankֵ�����Ľڵ㣬��һ�������ڵ㣬��noderankֵ�������
		for(int i=1;i<nSortSNode.length;i++){
			for(int j=0;j<sub.links;j++){
				if(((nSortSNode[0]==sub.link[j].from&&nSortSNode[i]==sub.link[j].to)||(nSortSNode[i]==sub.link[j].from&&nSortSNode[0]==sub.link[j].to))&&SubNet1.size()<FirstNetNumber&&!SubNet1.contains(nSortSNode[i])){
					SubNet1.add(nSortSNode[i]);
				}
			}
		}
		//copy���еĵ�һ�������糤��
		int SubNet1length=SubNet1.size();
		for(int i=0;i<nSortSNode.length;i++){
			if(NodeToNodeConnect(0,i,SubNet1length,SubNet1,nSortSNode)){
				if(SubNet1.size()<FirstNetNumber&&!SubNet1.contains(nSortSNode[i])){
					SubNet1.add(nSortSNode[i]);
				}
			}
		}
		obj.SubNet1=SubNet1;
		//copy���еĵ�һ�������糤��
		SubNet1length=SubNet1.size();
		//��һ�������������һ��ڵ������Ľڵ㣬���õڶ��������ڵ�
		for(int i = 0; i < nSortSNode.length; i++){
			if(NodeToNodeConnect(0,i,SubNet1length,SubNet1,nSortSNode)){
				if(SubNet2.size()+SubNet1length<SecondNetNumber && !SubNet1.contains(nSortSNode[i])){
					SubNet2.add(nSortSNode[i]);
				}
			}
		}
		int SubNet2length=SubNet2.size();
		for(int i = 0; i < nSortSNode.length; i++){
			if(NodeToNodeConnect(0,i,SubNet2length,SubNet2,nSortSNode)){
				if(SubNet2.size()+SubNet1length<SecondNetNumber && !SubNet2.contains(nSortSNode[i]) && !SubNet1.contains(nSortSNode[i])){
					SubNet2.add(nSortSNode[i]);
				}
			}
		}
		for(int i = 0; i < SubNet1length; i++) {
			SubNet2.add(SubNet1.get(i));
		}
		obj.SubNet2=SubNet2;

		for(int i=0;i<nSortSNode.length;i++) {
			SubNetsort.add(nSortSNode[i]);
		}

		obj.SubNetsort=SubNetsort;

		SubAList.add(obj.SubNet1);
		SubAList.add(obj.SubNet2);
		SubAList.add(obj.SubNetsort);
		SubAList.add(obj.energysNodePageRank);

//        for(int i=0;i<SubNet1.size();i++){
//            System.out.print(SubNet1.get(i));
//        }
		return SubAList;
	}
	public boolean NodeToNodeConnect(int index,int i,int SubNet1length,List<Integer> SubNet1,int nSortSNode []){
		for(int j=index;j<SubNet1length;j++){
			for(int k=0;k<sub.links;k++){
				if((nSortSNode[i]==sub.link[k].from&&SubNet1.get(j)==sub.link[k].to)||(nSortSNode[i]==sub.link[k].to&&SubNet1.get(j)==sub.link[k].from)){
					return true;
				}
			}
		}
		return false;
	}
	private static void sort(double[] a, int[] b) {
		Integer[] temp = new Integer[a.length];
		for (int i = 0; i < temp.length; i++) {
			temp[i] = i;
		}
		Arrays.sort(temp, new Comparator<Integer>() {
			@Override
			public int compare(Integer o1, Integer o2) {
				return Double.compare(a[o2], a[o1]);
			}
		});
		for (int i = 0; i < temp.length; i++) {
			b[i] = temp[i];
		}
	}


	private int MapVONE01ILPBySub(EOSubstrateNetwork sub,VONRequest reqs[],int index)
	{
		List<Integer> ilpsubnet1=(List<Integer>)SubAListILP.get(0);
		List<Integer> ilpsubnet2=(List<Integer>)SubAListILP.get(1);
		double[] energysNodePageRank =(double[]) SubAListILP.get(3);

		List<Integer> subnetmerge=new ArrayList<>();
		for(int i =0;i<ilpsubnet1.size();i++){
			subnetmerge.add(ilpsubnet1.get(i));
		}
		for(int i =0;i<ilpsubnet2.size();i++){
			subnetmerge.add(ilpsubnet2.get(i));
		}


		//01ILPģ�����
		//CreateVONE01ILPByLinUndirectDiagramSub(sub,reqs,index, ilpsubnet1);//�ڵ�NRֵʵʱ����
		CreateVONE01ILPByLinUndirectDiagramSub2(sub,reqs,index, ilpsubnet1,energysNodePageRank);
		int retNodeE[];
		LinkedList[] retVLinkE;
		retVLinkE = new LinkedList[reqs[index].links];
		retNodeE = new int[reqs[index].nodes];

		if(FindOptSoluByLin(sub,reqs,index,retNodeE,retVLinkE)){//�ҵ������Ž�
			//System.out.println("������Ƶ�ײ۳�ͻ");
			//PrintPath(reqs,index,kShortestPath,p,auxGraph.virtualNodes,pathEff,retLinkE,retSlotSE,retSlotEE);
			AddNodesMap(reqs,index,retNodeE);//�ڵ�ӳ��
			AddNodesMapSub(sub,reqs,index,retNodeE);//�ڵ�ӳ���޸�sub.cpu
			AddLinksMapBy01ILPLin(sub,reqs,index,retNodeE,retVLinkE);

			System.out.println("embed reqs "+ index+" successfully");
			return 0;//�ɹ��ҵ�VONE��
		}else{
			//CreateVONE01ILPByLinUndirectDiagramSub(sub,reqs,index, subnetmerge);//�ڵ�NRֵʵʱ����
			CreateVONE01ILPByLinUndirectDiagramSub2(sub,reqs,index, subnetmerge,energysNodePageRank);
			if(FindOptSoluByLin(sub,reqs,index,retNodeE,retVLinkE)){//�ҵ������Ž�
				//System.out.println("������Ƶ�ײ۳�ͻ");
				//PrintPath(reqs,index,kShortestPath,p,auxGraph.virtualNodes,pathEff,retLinkE,retSlotSE,retSlotEE);
				AddNodesMap(reqs,index,retNodeE);//�ڵ�ӳ��
				AddNodesMapSub(sub,reqs,index,retNodeE);//�ڵ�ӳ���޸�sub.cpu
				AddLinksMapBy01ILPLin(sub,reqs,index,retNodeE,retVLinkE);

				System.out.println("embed reqs "+ index+" successfully");
				return 0;//�ɹ��ҵ�VONE��
			}
		}
		return -1;

	}
   /*
	 * int MapVONE01ILPByLin()
	 * ���ܣ�ͨ��lin rongpinӳ��
	 */
	private int MapVONE01ILPByLin(EOSubstrateNetwork sub,VONRequest reqs[],int index)
	{
		//��Lin��01ILPģ�����
		CreateVONE01ILPByLinUndirectDiagram(sub,reqs,index);
		//��WangY��ILPģ����⣬���߸�����ǿ�Ļ���·����ӳ��ģ�����
		//FindVONEWangYPlusByOne01ILP(auxGraph,kShortestPath,pathSlots,pathLength,pathNo,pathEff,reqs,index);
		int retNodeE[];
		LinkedList[] retVLinkE;
		retVLinkE = new LinkedList[reqs[index].links];
		retNodeE = new int[reqs[index].nodes];
		if(FindOptSoluByLin(sub,reqs,index,retNodeE,retVLinkE)){//�ҵ������Ž�
			//System.out.println("������Ƶ�ײ۳�ͻ");
            //PrintPath(reqs,index,kShortestPath,p,auxGraph.virtualNodes,pathEff,retLinkE,retSlotSE,retSlotEE);
            AddNodesMap(reqs,index,retNodeE);//�ڵ�ӳ��
            AddNodesMapSub(sub,reqs,index,retNodeE);//�ڵ�ӳ���޸�sub.cpu
			AddLinksMapBy01ILPLin(sub,reqs,index,retNodeE,retVLinkE);
			
			System.out.println("embed reqs "+ index+" successfully");
			return 0;//�ɹ��ҵ�VONE��
		}
		
		return -1;
	}
    private int MapVONE01ILPByLin_1(EOSubstrateNetwork sub,VONRequest reqs[],int index,double sumOtherVirtualCPU)
    {
        int method = 0;
        boolean triedAllNodes = false;
        while(MapVONE01ILPByLin_2(sub,reqs,index,method) == -1 && triedAllNodes==false) {//ֻҪû��ӳ��ɹ�������ͼ��
            ILPSubAList=SubNetGraph2(sub,ILPSubAList,index,sumOtherVirtualCPU );
            List<Integer> sum=(List<Integer>)ILPSubAList.get(method);
            int jishu=0;
            for(int i=0;i< sum.size();i++){
                jishu++;
            }

            if (jishu>=sub.nodes){
                triedAllNodes=true;
                int mappingResult = MapVONE01ILPByLin_2(sub, reqs, index, method);
                if (mappingResult == 0) {
                    // ӳ��ɹ�������0
                    System.out.println("embed reqs " + index + " successfully after using all nodes.");
                    return 0;
                }else{
                    return -1;//ӳ��ʧ��
                }
            }

        }
        return 0;
    }
    private int MapVONE01ILPByLin_2(EOSubstrateNetwork sub,VONRequest reqs[],int index,int method)
    {
        //��Lin��01ILPģ�����
        List<Integer> ilpsubnet=(List<Integer>)ILPSubAList.get(method);
        double[] energysNodePageRank =(double[]) ILPSubAList.get(1);
        CreateVONE01ILPByLinUndirectDiagramSub2(sub,reqs,index,ilpsubnet,energysNodePageRank);
        int[] vNodeEmbed = new int[reqs[index].nodes];
        int[] sNodeEmbed = new int[sub.nodes];
        int[] vLinkEmbed = new int[reqs[index].links];
        InitAllocModel(sub,reqs,index,vNodeEmbed,sNodeEmbed,vLinkEmbed);
        int retNodeE[];
        LinkedList[] retVLinkE;
        retVLinkE = new LinkedList[reqs[index].links];
        retNodeE = new int[reqs[index].nodes];
        //p[][]:��¼·����ret[][]:ret[][0]:���ص���ʼƵ�ײۣ�ret[][1]:���ص�Ƶ�ײ�����
        int p[][] = new int[reqs[index].links][sub.nodes];
        int ret[][] = new int[reqs[index].links][2];//ret[][0]:���ص���ʼƵ�ײۣ�ret[][1]:���ص�Ƶ�ײ�����
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

        int num = 0;
        int[] minElement = new int[2];//minElement[0]����ڵ㣻minElement[1]����ڵ�;
        if(FindOptSoluByLin(sub,reqs,index,retNodeE,retVLinkE)){//�ҵ������Ž�
            while(num < reqs[index].nodes){
                //Ѱ�ҽڵ�ӳ�䣬��������minElement[0]\minElement[1];minIndexReq��minIndexSub
                AMnodeMapping1(subCopy,reqs,index,vNodeEmbed,sNodeEmbed,minElement,retNodeE,sNodeEmbed);

                if(minElement[0] == -1) return -1;//û���ҵ���СԪ��
                vNodeEmbed[minElement[0]] = minElement[1];//����ڵ�minElement[0]ӳ�䵽����ڵ�minElement[1]
                sNodeEmbed[minElement[1]] = minElement[0];//����ڵ�minElement[1]ӳ�������ڵ�minElement[0]
                //����cpu
                UpdateSub(subCopy,minElement[1],reqs[index].cpu[minElement[0]]);

                //������������Ѱ���Ƿ���ڵ�δӳ���������·��������ڣ���ӳ�䣻
                int noEmbedVLink = -1;
                noEmbedVLink=FindNoEmbedVLink(reqs,index,minElement[0],vNodeEmbed,vLinkEmbed);
                while(noEmbedVLink > -1){//����ҵ���δӳ���������·����ӳ�����·
                    //ӳ���������·,ӳ����������p[][]�У���ʾ������·ӳ���·��;ret[][0]:��ʼƵ�ײ�������ret[][1]:Ƶ�ײ�������
                    if(!PreEmbedVLinkByKShortestPath(subCopy,reqs,index,noEmbedVLink,vNodeEmbed,p,ret)){//noEmbedVLink������·��snodeEmbed��Ӧ������ڵ�
                        return -1;//ʧ�ܷ���
                    }
                    //��·�Ѿ�����
                    vLinkEmbed[noEmbedVLink] = 1;
                    //���µײ�����subCopy
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

            //���������·û��ӳ�䣬��ʧ�ܷ���
            for(int i=0;i<reqs[index].links;i++){
                if(vLinkEmbed[i] == -1) return -1;//ʧ�ܷ���
            }
            //����cpu
            UpdateSub(sub,subCopy);
            //��¼�ڵ����·ӳ����
            AddNodesMap(reqs,index,vNodeEmbed);//����s2v_n��v2s
            AddLinksMapBySPFA(sub,reqs,index,retOther,p);//���µײ�����

            //���µײ�����slots
            UpdateSubSlots(sub,subCopy);

            return 0;//�ɹ�����
        }

        return -1;
    }
	private LinkedList<ArrayList<Object>> historyStates = new LinkedList<>();
	private static final double ALPHA = 1.2;
	private static final double BETA = 3;
	private static final double GAMMA = 0.8;
	private static final double THETA_LOW = 0.4;
	// �������߷���
	private double calculateUtilization(EOSubstrateNetwork sub, ArrayList<Object> subList) {
		List<Integer> nodes = (List<Integer>)subList.get(0);
		double total = 0;
		for(int node : nodes) {
			total += sub.cpu[node] / sub.maxcpu[node];
		}
		return nodes.isEmpty() ? 0 : total / nodes.size();
	}

	private void cacheHistoryState(ArrayList<Object> currentState) {
		if(historyStates.size() > 5) historyStates.removeFirst();
		historyStates.addLast(cloneState(currentState));
	}

	private void restoreFromHistory(EOSubstrateNetwork sub,int index) {
		if(!historyStates.isEmpty()) {
			ArrayList<Object> bestState = findOptimalHistory(sub,index);
			if(bestState != null) {
				ILPSubAList = bestState;
			}
		}
	}
// ... �������з��� ...

	private ArrayList<Object> cloneState(ArrayList<Object> original) {
		ArrayList<Object> clone = new ArrayList<>();

		// ��������б�
		if(original.get(0) instanceof List) {
			List<Integer> subnet = new ArrayList<>((List<Integer>)original.get(0));
			clone.add(subnet);
		}

		// ���������������
		if(original.get(1) instanceof double[]) {
			double[] energyRank = Arrays.copyOf((double[])original.get(1), ((double[])original.get(1)).length);
			clone.add(energyRank);
		}

		return clone;
	}
	// ����������������·���
	private ArrayList<Object> findOptimalHistory(EOSubstrateNetwork sub,int index) {
		ArrayList<Object> bestState = null;
		double maxScore = Double.MIN_VALUE;
		int currentNodes = reqs[index].nodes; // ���赱ǰ�����ͨ�����Ա��������
		// ����������ʷ״̬
		for (ArrayList<Object> state : historyStates) {
			// ��֤״̬��ʽ
			if (state.size() < 2 || !(state.get(0) instanceof List) || !(state.get(1) instanceof double[])) {
				continue;
			}

			// ��ȡ�������ú��ܺ�����
			List<Integer> subnet = (List<Integer>) state.get(0);
			double[] energyRank = (double[]) state.get(1);
			int subnetSize = subnet.size();
			// �����ڵ��ģƥ�����֣�ռ30%Ȩ�أ�
			double sizeScore = 1.0 - Math.abs(subnetSize - currentNodes)/(double)Math.max(subnetSize, currentNodes);

			// ������Դ������
			double cpuUtilization = calculateCPUUtilization(sub, subnet);
			double linkUtilization = calculateLinkUtilization(sub, subnet);

			// ����̼�ŷ�Ч�ʣ�������getCarbonEmission������
			double carbonEfficiency = calculateCarbonEmission(sub, subnet);

			// �ۺ������÷֣��ɸ����������Ȩ�أ�
			double score = (cpuUtilization * 0.3)
					+ (linkUtilization * 0.3)
					+ (1/carbonEfficiency * 0.1)
					+ (sizeScore * 0.3);
			// ��������״̬
			if (score > maxScore && meetsCurrentRequirement(sub, subnet)) {
				maxScore = score;
				bestState = state;
			}
		}
		return bestState;
	}

	// ������������֤�����Ƿ����㵱ǰ����
	private boolean meetsCurrentRequirement(EOSubstrateNetwork sub, List<Integer> subnet) {
		// ��������ڵ��Ƿ��Կ���
		for (Integer node : subnet) {
			if (sub.cpu[node] < sub.cpu[node]*0.1) {
				return false;
			}
		}
		return true;
	}

	// ��������������CPU�����ʣ�ʾ��ʵ�֣�
	private double calculateCPUUtilization(EOSubstrateNetwork sub, List<Integer> subnet) {
		double totalUsed = 0;
		double totalCapacity = 0;
		for (Integer node : subnet) {
			totalUsed += sub.maxcpu[node] - sub.cpu[node];
			totalCapacity += sub.maxcpu[node];
		}
		return totalUsed / totalCapacity;
	}
	public static List<Integer> getSubnetLinkIds(List<Integer> subnet) {
		List<Integer> linkIds = new ArrayList<>();
		// ��������������·
		for(int i = 0; i < sub.links; i++) {
			// �ж���·���˽ڵ��Ƿ���������
			if(subnet.contains(sub.link[i].from) && subnet.contains(sub.link[i].to)) {
				linkIds.add(i); // �����·���
			}
		}
		return linkIds;
	}

	// ��·�����ʼ��㣨�����ʵ����·ģ��ʵ�֣�
	private double calculateLinkUtilization(EOSubstrateNetwork sub, List<Integer> subnet) {
		// ʵ���߼���������������·��ƽ��Ƶ�ײ�ʹ����
		// ʾ��α���룺
		int totalUsed = 0;
		int totalAvailable = 0;
		List<Integer> linkIndexes = getSubnetLinkIds(subnet);
		for (int linkid : linkIndexes) {
			for (int j = 0; j < sub.slotsNum; j++) {
				if (sub.slots[linkid][j] == 0){
					totalUsed++;//����Ϊ1��ռ��Ϊ0
				}
			}
			totalAvailable += Parameters.MaxSlots;// ��Ҫʵ��countUsedSlots����/totalAvailable += link.slots.length;
		}
		return totalAvailable > 0 ? (double)totalUsed / totalAvailable : 0.0;
	}


	// ̼�ŷ�Ч�ʼ��㣨�����ʵ���ܺ�ģ��ʵ�֣�
	private double calculateCarbonEmission(EOSubstrateNetwork sub, List<Integer> subnet) {
		// ʵ���߼������������ڵ�ĵ�λ������̼�ŷ�
		// ʾ��α���룺
		double totalEmission = 0;
		for (Integer node : subnet) {
			totalEmission += sub.node_GHG[node] * (sub.maxcpu[node] - sub.cpu[node]);
		}
		return totalEmission;
	}
	private int MapVONE01ILPByLin9166(EOSubstrateNetwork sub,VONRequest reqs[],int index,double sumOtherVirtualCPU)
	{

		int reqNodes = reqs[index].nodes;
		int sMax = (int)(ALPHA * reqNodes + BETA);
		int sMin = (int)(GAMMA * reqNodes);
		int method = 0;
		boolean triedAllNodes = false;

		while(MapVONE01ILPByLin_9166(sub,reqs,index,method) == -1 && triedAllNodes==false) {//ֻҪû��ӳ��ɹ�������ͼ��
			double utilization = calculateUtilization(sub, ILPSubAList);
			if(utilization < THETA_LOW && ILPSubAList.size() > sMin) {
				restoreFromHistory(sub,index);  // �������˻���
			}
		ILPSubAList=SubNetGraph2(sub,ILPSubAList,index ,sumOtherVirtualCPU);
			if(ILPSubAList.size() <= sMax) {
				cacheHistoryState(ILPSubAList);
			}

			List<Integer> sum=(List<Integer>)ILPSubAList.get(method);
			int jishu=0;
			for(int i=0;i< sum.size();i++){
				jishu++;
			}

			if (jishu>=sub.nodes){
				triedAllNodes=true;
				int mappingResult = MapVONE01ILPByLin_9166(sub, reqs, index, method);
				if (mappingResult == 0) {
					// ӳ��ɹ�������0
					System.out.println("embed reqs " + index + " successfully after using all nodes.");
					return 0;
				}else{
					return -1;//ӳ��ʧ��
				}
			}

		}
		return 0;
	}
    private int MapVONE01ILPByLin_9166(EOSubstrateNetwork sub,VONRequest reqs[],int index,int method)
    {
        //��Lin��01ILPģ�����
		int nSortVNode[]=new int[reqs[index].nodes];
		List<Integer> ilpsubnet=(List<Integer>)ILPSubAList.get(method);
		double[] energysNodePageRank =(double[]) ILPSubAList.get(1);
        CreateVONE01ILPByLinUndirectDiagramSub9166(sub,reqs,index,ilpsubnet,energysNodePageRank);//CreateVONE01ILPByLinUndirectDiagramSub4
        int[] vNodeEmbed = new int[reqs[index].nodes];
        int[] sNodeEmbed = new int[sub.nodes];
        int[] vLinkEmbed = new int[reqs[index].links];
        InitAllocModel(sub,reqs,index,vNodeEmbed,sNodeEmbed,vLinkEmbed);
        int retNodeE[];
        LinkedList[] retVLinkE;
		sort(reqs[index].cpu,nSortVNode);
        retVLinkE = new LinkedList[reqs[index].links];
        retNodeE = new int[reqs[index].nodes];
        //p[][]:��¼·����ret[][]:ret[][0]:���ص���ʼƵ�ײۣ�ret[][1]:���ص�Ƶ�ײ�����
        int p[][] = new int[reqs[index].links][sub.nodes];
        int ret[][] = new int[reqs[index].links][2];//ret[][0]:���ص���ʼƵ�ײۣ�ret[][1]:���ص�Ƶ�ײ�����
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

        int num = 0;
        int[] minElement = new int[2];//minElement[0]����ڵ㣻minElement[1]����ڵ�;
        if(FindOptSoluByLin(sub,reqs,index,retNodeE,retVLinkE)){//�ҵ������Ž�
            while(num < reqs[index].nodes){
                //Ѱ�ҽڵ�ӳ�䣬��������minElement[0]\minElement[1];minIndexReq��minIndexSub
				//AMnodeMapping1(subCopy,reqs,index,vNodeEmbed,sNodeEmbed,minElement,retNodeE,sNodeEmbed);
				AMnodeMapping3(subCopy,reqs,index,vNodeEmbed,sNodeEmbed,minElement,retNodeE);
				if(minElement[0] == -1) return -1;//û���ҵ���СԪ��
                vNodeEmbed[minElement[0]] = minElement[1];//����ڵ�minElement[0]ӳ�䵽����ڵ�minElement[1]
                sNodeEmbed[minElement[1]] = minElement[0];//����ڵ�minElement[0]ӳ�䵽����ڵ�minElement[1]
                //����cpu
                UpdateSub(subCopy,minElement[1],reqs[index].cpu[minElement[0]]);

                //������������Ѱ���Ƿ���ڵ�δӳ���������·��������ڣ���ӳ�䣻
                int noEmbedVLink = -1;
                noEmbedVLink=FindNoEmbedVLink(reqs,index,minElement[0],vNodeEmbed,vLinkEmbed);
                while(noEmbedVLink > -1){//����ҵ���δӳ���������·����ӳ�����·
                    //ӳ���������·,ӳ����������p[][]�У���ʾ������·ӳ���·��;ret[][0]:��ʼƵ�ײ�������ret[][1]:Ƶ�ײ�������
                    if(!PreEmbedVLinkBy9166(subCopy,reqs,index,noEmbedVLink,vNodeEmbed,p,ret)){//noEmbedVLink������·��snodeEmbed��Ӧ������ڵ�
                        return -1;//ʧ�ܷ���//PreEmbedVLinkByKShortestPath PreEmbedVLinkBy9166
                    }
                    //��·�Ѿ�����
                    vLinkEmbed[noEmbedVLink] = 1;
                    //���µײ�����subCopy
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

            //���������·û��ӳ�䣬��ʧ�ܷ���
            for(int i=0;i<reqs[index].links;i++){
                if(vLinkEmbed[i] == -1) return -1;//ʧ�ܷ���
            }
            //����cpu
            UpdateSub(sub,subCopy);
            //��¼�ڵ����·ӳ����
            AddNodesMap(reqs,index,vNodeEmbed);//����s2v_n��v2s
            AddLinksMapBySPFA(sub,reqs,index,retOther,p);//���µײ�����

            //���µײ�����slots
            UpdateSubSlots(sub,subCopy);

            return 0;//�ɹ�����
        }

        return -1;
    }
	private int MapVONE01ILPByLin_3(EOSubstrateNetwork sub,VONRequest reqs[],int index)
	{
		//��Lin��01ILPģ�����

		CreateVONE01ILPByLinUndirectDiagramSub4(sub,reqs,index);//CreateVONE01ILPByLinUndirectDiagramSub4
		int[] vNodeEmbed = new int[reqs[index].nodes];
		int[] sNodeEmbed = new int[sub.nodes];
		int[] vLinkEmbed = new int[reqs[index].links];
		InitAllocModel(sub,reqs,index,vNodeEmbed,sNodeEmbed,vLinkEmbed);
		int retNodeE[];
		LinkedList[] retVLinkE;
		retVLinkE = new LinkedList[reqs[index].links];
		retNodeE = new int[reqs[index].nodes];
		//p[][]:��¼·����ret[][]:ret[][0]:���ص���ʼƵ�ײۣ�ret[][1]:���ص�Ƶ�ײ�����
		int p[][] = new int[reqs[index].links][sub.nodes];
		int ret[][] = new int[reqs[index].links][2];//ret[][0]:���ص���ʼƵ�ײۣ�ret[][1]:���ص�Ƶ�ײ�����
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

		int num = 0;
		int[] minElement = new int[2];//minElement[0]����ڵ㣻minElement[1]����ڵ�;
		if(FindOptSoluByLin(sub,reqs,index,retNodeE,retVLinkE)){//�ҵ������Ž�
			while(num < reqs[index].nodes){
				//Ѱ�ҽڵ�ӳ�䣬��������minElement[0]\minElement[1];minIndexReq��minIndexSub
				//AMnodeMapping1(subCopy,reqs,index,vNodeEmbed,sNodeEmbed,minElement,retNodeE,sNodeEmbed);
				AMnodeMapping3(subCopy,reqs,index,vNodeEmbed,sNodeEmbed,minElement,retNodeE);
				if(minElement[0] == -1) return -1;//û���ҵ���СԪ��
				vNodeEmbed[minElement[0]] = minElement[1];//����ڵ�minElement[0]ӳ�䵽����ڵ�minElement[1]
				sNodeEmbed[minElement[1]] = minElement[0];//����ڵ�minElement[1]ӳ�������ڵ�minElement[0]
				//����cpu
				UpdateSub(subCopy,minElement[1],reqs[index].cpu[minElement[0]]);

				//������������Ѱ���Ƿ���ڵ�δӳ���������·��������ڣ���ӳ�䣻
				int noEmbedVLink = -1;
				noEmbedVLink=FindNoEmbedVLink(reqs,index,minElement[0],vNodeEmbed,vLinkEmbed);
				while(noEmbedVLink > -1){//����ҵ���δӳ���������·����ӳ�����·
					//ӳ���������·,ӳ����������p[][]�У���ʾ������·ӳ���·��;ret[][0]:��ʼƵ�ײ�������ret[][1]:Ƶ�ײ�������
					if(!PreEmbedVLinkBy9166(subCopy,reqs,index,noEmbedVLink,vNodeEmbed,p,ret)){//noEmbedVLink������·��snodeEmbed��Ӧ������ڵ�
						return -1;//ʧ�ܷ���//PreEmbedVLinkByKShortestPath PreEmbedVLinkBy9166
					}
					//��·�Ѿ�����
					vLinkEmbed[noEmbedVLink] = 1;
					//���µײ�����subCopy
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

			//���������·û��ӳ�䣬��ʧ�ܷ���
			for(int i=0;i<reqs[index].links;i++){
				if(vLinkEmbed[i] == -1) return -1;//ʧ�ܷ���
			}
			//����cpu
			UpdateSub(sub,subCopy);
			//��¼�ڵ����·ӳ����
			AddNodesMap(reqs,index,vNodeEmbed);//����s2v_n��v2s
			AddLinksMapBySPFA(sub,reqs,index,retOther,p);//���µײ�����

			//���µײ�����slots
			UpdateSubSlots(sub,subCopy);

			return 0;//�ɹ�����
		}

		return -1;
	}
	private int MapVONE01ILPByLin_4(EOSubstrateNetwork sub,VONRequest reqs[],int index)
	{
		//��Lin��01ILPģ�����
		CreateVONE01ILPByLinUndirectDiagramSub5(sub,reqs,index);
		int[] vNodeEmbed = new int[reqs[index].nodes];
		int[] sNodeEmbed = new int[sub.nodes];
		int[] vLinkEmbed = new int[reqs[index].links];
		InitAllocModel(sub,reqs,index,vNodeEmbed,sNodeEmbed,vLinkEmbed);
		int retNodeE[];
		LinkedList[] retVLinkE;
		retVLinkE = new LinkedList[reqs[index].links];
		retNodeE = new int[reqs[index].nodes];
		//p[][]:��¼·����ret[][]:ret[][0]:���ص���ʼƵ�ײۣ�ret[][1]:���ص�Ƶ�ײ�����
		int p[][] = new int[reqs[index].links][sub.nodes];
		int ret[][] = new int[reqs[index].links][2];//ret[][0]:���ص���ʼƵ�ײۣ�ret[][1]:���ص�Ƶ�ײ�����
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

		int num = 0;
		int[] minElement = new int[2];//minElement[0]����ڵ㣻minElement[1]����ڵ�;
		if(FindOptSoluByLin(sub,reqs,index,retNodeE,retVLinkE)){//�ҵ������Ž�
			while(num < reqs[index].nodes){
				//Ѱ�ҽڵ�ӳ�䣬��������minElement[0]\minElement[1];minIndexReq��minIndexSub
				AMnodeMapping1(subCopy,reqs,index,vNodeEmbed,sNodeEmbed,minElement,retNodeE,sNodeEmbed);
				if(minElement[0] == -1) return -1;//û���ҵ���СԪ��
				vNodeEmbed[minElement[0]] = minElement[1];//����ڵ�minElement[0]ӳ�䵽����ڵ�minElement[1]
				sNodeEmbed[minElement[1]] = minElement[0];//����ڵ�minElement[1]ӳ�������ڵ�minElement[0]
				//����cpu
				UpdateSub(subCopy,minElement[1],reqs[index].cpu[minElement[0]]);

				//������������Ѱ���Ƿ���ڵ�δӳ���������·��������ڣ���ӳ�䣻
				int noEmbedVLink = -1;
				noEmbedVLink=FindNoEmbedVLink(reqs,index,minElement[0],vNodeEmbed,vLinkEmbed);
				while(noEmbedVLink > -1){//����ҵ���δӳ���������·����ӳ�����·
					//ӳ���������·,ӳ����������p[][]�У���ʾ������·ӳ���·��;ret[][0]:��ʼƵ�ײ�������ret[][1]:Ƶ�ײ�������
					if(!PreEmbedVLinkByKShortestPath(subCopy,reqs,index,noEmbedVLink,vNodeEmbed,p,ret)){//noEmbedVLink������·��snodeEmbed��Ӧ������ڵ�
						return -1;//ʧ�ܷ���
					}
					//��·�Ѿ�����
					vLinkEmbed[noEmbedVLink] = 1;
					//���µײ�����subCopy
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

			//���������·û��ӳ�䣬��ʧ�ܷ���
			for(int i=0;i<reqs[index].links;i++){
				if(vLinkEmbed[i] == -1) return -1;//ʧ�ܷ���
			}
			//����cpu
			UpdateSub(sub,subCopy);
			//��¼�ڵ����·ӳ����
			AddNodesMap(reqs,index,vNodeEmbed);//����s2v_n��v2s
			AddLinksMapBySPFA(sub,reqs,index,retOther,p);//���µײ�����

			//���µײ�����slots
			UpdateSubSlots(sub,subCopy);

			return 0;//�ɹ�����
		}

		return -1;
	}
	private int MapVONE01ILPByLin_5(EOSubstrateNetwork sub,VONRequest reqs[],int index)
	{
		//��Lin��01ILPģ�����
		CreateVONE01ILPByLinUndirectDiagramSub6(sub,reqs,index);
		int[] vNodeEmbed = new int[reqs[index].nodes];
		int[] sNodeEmbed = new int[sub.nodes];
		int[] vLinkEmbed = new int[reqs[index].links];
		InitAllocModel(sub,reqs,index,vNodeEmbed,sNodeEmbed,vLinkEmbed);
		int retNodeE[];
		LinkedList[] retVLinkE;
		retVLinkE = new LinkedList[reqs[index].links];
		retNodeE = new int[reqs[index].nodes];
		//p[][]:��¼·����ret[][]:ret[][0]:���ص���ʼƵ�ײۣ�ret[][1]:���ص�Ƶ�ײ�����
		int p[][] = new int[reqs[index].links][sub.nodes];
		int ret[][] = new int[reqs[index].links][2];//ret[][0]:���ص���ʼƵ�ײۣ�ret[][1]:���ص�Ƶ�ײ�����
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

		int num = 0;
		int[] minElement = new int[2];//minElement[0]����ڵ㣻minElement[1]����ڵ�;
		if(FindOptSoluByLin(sub,reqs,index,retNodeE,retVLinkE)){//�ҵ������Ž�
			while(num < reqs[index].nodes){
				//Ѱ�ҽڵ�ӳ�䣬��������minElement[0]\minElement[1];minIndexReq��minIndexSub
				AMnodeMapping1(subCopy,reqs,index,vNodeEmbed,sNodeEmbed,minElement,retNodeE,sNodeEmbed);
				if(minElement[0] == -1) return -1;//û���ҵ���СԪ��
				vNodeEmbed[minElement[0]] = minElement[1];//����ڵ�minElement[0]ӳ�䵽����ڵ�minElement[1]
				sNodeEmbed[minElement[1]] = minElement[0];//����ڵ�minElement[1]ӳ�������ڵ�minElement[0]
				//����cpu
				UpdateSub(subCopy,minElement[1],reqs[index].cpu[minElement[0]]);

				//������������Ѱ���Ƿ���ڵ�δӳ���������·��������ڣ���ӳ�䣻
				int noEmbedVLink = -1;
				noEmbedVLink=FindNoEmbedVLink(reqs,index,minElement[0],vNodeEmbed,vLinkEmbed);
				while(noEmbedVLink > -1){//����ҵ���δӳ���������·����ӳ�����·
					//ӳ���������·,ӳ����������p[][]�У���ʾ������·ӳ���·��;ret[][0]:��ʼƵ�ײ�������ret[][1]:Ƶ�ײ�������
					if(!PreEmbedVLinkByKShortestPath(subCopy,reqs,index,noEmbedVLink,vNodeEmbed,p,ret)){//noEmbedVLink������·��snodeEmbed��Ӧ������ڵ�
						return -1;//ʧ�ܷ���
					}
					//��·�Ѿ�����
					vLinkEmbed[noEmbedVLink] = 1;
					//���µײ�����subCopy
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

			//���������·û��ӳ�䣬��ʧ�ܷ���
			for(int i=0;i<reqs[index].links;i++){
				if(vLinkEmbed[i] == -1) return -1;//ʧ�ܷ���
			}
			//����cpu
			UpdateSub(sub,subCopy);
			//��¼�ڵ����·ӳ����
			AddNodesMap(reqs,index,vNodeEmbed);//����s2v_n��v2s
			AddLinksMapBySPFA(sub,reqs,index,retOther,p);//���µײ�����

			//���µײ�����slots
			UpdateSubSlots(sub,subCopy);

			return 0;//�ɹ�����
		}

		return -1;
	}
	private int MapVONE01ILPByLin_6(EOSubstrateNetwork sub,VONRequest reqs[],int index)
	{
		//��Lin��01ILPģ�����
		CreateVONE01ILPByLinUndirectDiagramSub7(sub,reqs,index);
		int[] vNodeEmbed = new int[reqs[index].nodes];
		int[] sNodeEmbed = new int[sub.nodes];
		int[] vLinkEmbed = new int[reqs[index].links];
		InitAllocModel(sub,reqs,index,vNodeEmbed,sNodeEmbed,vLinkEmbed);
		int retNodeE[];
		LinkedList[] retVLinkE;
		retVLinkE = new LinkedList[reqs[index].links];
		retNodeE = new int[reqs[index].nodes];
		//p[][]:��¼·����ret[][]:ret[][0]:���ص���ʼƵ�ײۣ�ret[][1]:���ص�Ƶ�ײ�����
		int p[][] = new int[reqs[index].links][sub.nodes];
		int ret[][] = new int[reqs[index].links][2];//ret[][0]:���ص���ʼƵ�ײۣ�ret[][1]:���ص�Ƶ�ײ�����
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

		int num = 0;
		int[] minElement = new int[2];//minElement[0]����ڵ㣻minElement[1]����ڵ�;
		if(FindOptSoluByLin(sub,reqs,index,retNodeE,retVLinkE)){//�ҵ������Ž�
			while(num < reqs[index].nodes){
				//Ѱ�ҽڵ�ӳ�䣬��������minElement[0]\minElement[1];minIndexReq��minIndexSub
				AMnodeMapping1(subCopy,reqs,index,vNodeEmbed,sNodeEmbed,minElement,retNodeE,sNodeEmbed);
				if(minElement[0] == -1) return -1;//û���ҵ���СԪ��
				vNodeEmbed[minElement[0]] = minElement[1];//����ڵ�minElement[0]ӳ�䵽����ڵ�minElement[1]
				sNodeEmbed[minElement[1]] = minElement[0];//����ڵ�minElement[1]ӳ�������ڵ�minElement[0]
				//����cpu
				UpdateSub(subCopy,minElement[1],reqs[index].cpu[minElement[0]]);

				//������������Ѱ���Ƿ���ڵ�δӳ���������·��������ڣ���ӳ�䣻
				int noEmbedVLink = -1;
				noEmbedVLink=FindNoEmbedVLink(reqs,index,minElement[0],vNodeEmbed,vLinkEmbed);
				while(noEmbedVLink > -1){//����ҵ���δӳ���������·����ӳ�����·
					//ӳ���������·,ӳ����������p[][]�У���ʾ������·ӳ���·��;ret[][0]:��ʼƵ�ײ�������ret[][1]:Ƶ�ײ�������
					if(!PreEmbedVLinkByKShortestPath(subCopy,reqs,index,noEmbedVLink,vNodeEmbed,p,ret)){//noEmbedVLink������·��snodeEmbed��Ӧ������ڵ�
						return -1;//ʧ�ܷ���
					}
					//��·�Ѿ�����
					vLinkEmbed[noEmbedVLink] = 1;
					//���µײ�����subCopy
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

			//���������·û��ӳ�䣬��ʧ�ܷ���
			for(int i=0;i<reqs[index].links;i++){
				if(vLinkEmbed[i] == -1) return -1;//ʧ�ܷ���
			}
			//����cpu
			UpdateSub(sub,subCopy);
			//��¼�ڵ����·ӳ����
			AddNodesMap(reqs,index,vNodeEmbed);//����s2v_n��v2s
			AddLinksMapBySPFA(sub,reqs,index,retOther,p);//���µײ�����

			//���µײ�����slots
			UpdateSubSlots(sub,subCopy);

			return 0;//�ɹ�����
		}

		return -1;
	}
    private void AMnodeMapping1(EOSubstrateNetwork sub,VONRequest reqs[],int index,int[] vnodeEmbed,int[] snodeEmbed,
                                int[] minElent,int[] retNodeE, int[] nSortSNode) {
        minElent[0] = minElent[1] = -1;
        for(int i=0; i<reqs[index].nodes; i++){
            if(retNodeE[i] != -1 && snodeEmbed[retNodeE[i]] == -1 &&
                    reqs[index].cpu[i] <= sub.cpu[retNodeE[i]]){
                minElent[0] = i;        // ����ڵ�����
                minElent[1] = retNodeE[i]; // ILPԤ���������ڵ�
                return;
            }
        }
    }



	// ... ���д��� ...
	private void AMnodeMapping3(EOSubstrateNetwork sub, VONRequest reqs[], int index, int[] vnodeEmbed, int[] snodeEmbed,
								int[] minElent, int[] retNodeE) {
		minElent[0] = -1;
		minElent[1] = -1;
		for (int i = 0; i < retNodeE.length; i++) {
			// ��� retNodeE[i] ��Ϊ -1 �Ҷ�Ӧ������ڵ�δ��ӳ��
			if (retNodeE[i] != -1 && snodeEmbed[retNodeE[i]] == -1) {
				minElent[0] = i; // ����ڵ�����
				minElent[1] = retNodeE[i]; // ��Ӧ������ڵ�����
				return;
			}
		}
	}
	// ... ���д��� ...
	private int MapVONE01ILPByLinStrong(EOSubstrateNetwork sub,VONRequest reqs[],int index)
	{
		//��Lin��01ILPģ�����
		//CreateVONE01ILPByLinUndirectDiagram(sub,reqs,index);
		CreateVONE01ILPByLinStrong(sub,reqs,index);
		//��WangY��ILPģ����⣬���߸�����ǿ�Ļ���·����ӳ��ģ�����
		//FindVONEWangYPlusByOne01ILP(auxGraph,kShortestPath,pathSlots,pathLength,pathNo,pathEff,reqs,index);
		int retNodeE[];
		LinkedList[] retVLinkE;
		retVLinkE = new LinkedList[reqs[index].links];
		retNodeE = new int[reqs[index].nodes];
		if(
				FindOptSoluByLin(sub,reqs,index,retNodeE,retVLinkE)){//�ҵ������Ž�
			//System.out.println("������Ƶ�ײ۳�ͻ");
            //PrintPath(reqs,index,kShortestPath,p,auxGraph.virtualNodes,pathEff,retLinkE,retSlotSE,retSlotEE);
            AddNodesMap(reqs,index,retNodeE);//�ڵ�ӳ��
            AddNodesMapSub(sub,reqs,index,retNodeE);//�ڵ�ӳ���޸�sub.cpu
			AddLinksMapBy01ILPLin(sub,reqs,index,retNodeE,retVLinkE);
			
			System.out.println("embed reqs "+ index+" successfully");
			return 0;//�ɹ��ҵ�VONE��
		}
		
		return -1;
	}
	
	/*
	 * ��������
	 * �㷨��
	 * 1�����Ƚ��нڵ�ӳ��
	 * while(cost<0.6)
	 * 2��������ڵ�ӳ���ÿ���������
	 * 3��ѡ���������������������·����Ƶ�׺�CPU���������·���ϵĽڵ�
	 * 4���ƶ�����ڵ������С�ĵ㣬���½��
	 * 5�����ӳ�����
	 */
	private int MapVONE01ILPByLinFB(EOSubstrateNetwork sub,VONRequest reqs[],int index)
	{
		int retNodeE[];
		LinkedList[] retVLinkE;
		retVLinkE = new LinkedList[reqs[index].links];
		retNodeE = new int[reqs[index].nodes];
		int cost[] = new int[1];
		
		int indexS=0;//��ʼ����
		int resN=reqs[index].nodes;//���ñ�ʶ������ڵ�����
		int slotsNum = CalSlots(reqs,index,1);//��������ڵ�
		if(slotsNum <= 10)  slotsNum = 10;
		int indexE = indexS + slotsNum - 1;
		int retN = -1;
		int subMark[] = new int[sub.nodes];
		if(indexE>=Parameters.MaxSlots) indexE=Parameters.MaxSlots-1;
		resN = sub.nodes;//1*reqs[index].nodes;//
		while(resN <= sub.nodes){
			SetMarkForSubNodes(sub,reqs,index,resN,subMark);//retN[0]:��ʶ��Ŀ��retN[1]:��ʼ����
			indexS=0;indexE = indexS + slotsNum - 1;//Parameters.MaxSlots-1;//10*(indexS + slotsNum - 1);
			if(indexE>=Parameters.MaxSlots) break;//indexE=Parameters.MaxSlots-1;
			while(indexE<Parameters.MaxSlots){
				CreateVONE01ILPByLinFB(sub,reqs,index,indexS,indexE,subMark);
				if(FindOptSoluByLin(sub,reqs,index,retNodeE,retVLinkE)){//�ҵ������Ž�
					FindOptSoluCostByLin(cost);
					if(reqs[index].links*1.0/cost[0]>0.5){//����ӳ�����
						AddNodesMap(reqs,index,retNodeE);//�ڵ�ӳ��
						AddNodesMapSub(sub,reqs,index,retNodeE);//�ڵ�ӳ���޸�sub.cpu
						AddLinksMapBy01ILPLin(sub,reqs,index,retNodeE,retVLinkE);
					
						System.out.println("embed reqs "+ index+" successfully");
						return 0;//�ɹ��ҵ�VONE��
					}
				}
				indexS++;
				indexE++;
				//if(indexE>=Parameters.MaxSlots) indexE=Parameters.MaxSlots-1;
			}
			resN++;
		}
		return -1;
	}
	/*
	 * int MapVONE01ILPByLinFB()
	 * ���ܣ�ͨ��lin rongpin FBӳ��
	 */
	private int MapVONE01ILPByLinFB1(EOSubstrateNetwork sub,VONRequest reqs[],int index)
	{
		int retNodeE[];
		LinkedList[] retVLinkE;
		retVLinkE = new LinkedList[reqs[index].links];
		retNodeE = new int[reqs[index].nodes];
		int cost[] = new int[1];
		
		int indexS=0;//��ʼ����
		int resN=reqs[index].nodes;//���ñ�ʶ������ڵ�����
		int slotsNum = CalSlots(reqs,index,1);//��������ڵ�
		int indexE = indexS + slotsNum - 1;
		int retN = -1;
		int subMark[] = new int[sub.nodes];
		if(indexE>=Parameters.MaxSlots) indexE=Parameters.MaxSlots-1;
		resN = 2*reqs[index].nodes;
		while(resN <= sub.nodes){
			SetMarkForSubNodes(sub,reqs,index,resN,subMark);//retN[0]:��ʶ��Ŀ��retN[1]:��ʼ����
			indexS=0;indexE = (indexS + slotsNum - 1)*4;
			if(indexE>=Parameters.MaxSlots) indexE=Parameters.MaxSlots-1;
			while(indexS<Parameters.MaxSlots-1){
				CreateVONE01ILPByLinFB(sub,reqs,index,indexS,indexE,subMark);
				if(FindOptSoluByLin(sub,reqs,index,retNodeE,retVLinkE)){//�ҵ������Ž�
					FindOptSoluCostByLin(cost);
					if(reqs[index].links*1.0/cost[0]>0.6){//����ӳ�����
						AddNodesMap(reqs,index,retNodeE);//�ڵ�ӳ��
						AddNodesMapSub(sub,reqs,index,retNodeE);//�ڵ�ӳ���޸�sub.cpu
						AddLinksMapBy01ILPLin(sub,reqs,index,retNodeE,retVLinkE);
					
						System.out.println("embed reqs "+ index+" successfully");
						return 0;//�ɹ��ҵ�VONE��
					}
				}
				indexS++;
				indexE++;
			}
			resN++;
		}
		return -1;
	}
	/*
	 * ����ӳ���ʶ
	 */
	public void SetMarkForSubNodes(EOSubstrateNetwork sub,VONRequest reqs[],int index,int resN,int subMark[])
	{
		double cpuM=-1;
		int cpuMIndex=-1;
		//Ѱ�����CPU�ڵ�
		for(int i=0;i<sub.nodes;i++){
			if(cpuM < sub.cpu[i]){
				cpuM = sub.cpu[i];cpuMIndex = i;
			}
		}
		//���½ڵ����ӵĶ�
		subMark[cpuMIndex] = 1;
		int degreeNode[] = new int[sub.nodes];
		for(int i=0;i<sub.links;i++){
			if(subMark[sub.link[i].from] == 1 && subMark[sub.link[i].to] == 0) degreeNode[sub.link[i].to]++;
			if(subMark[sub.link[i].to] == 1 && subMark[sub.link[i].from] == 0) degreeNode[sub.link[i].from]++;
		}
		
		//ѡ�����Ķȵĵ�
		for(int j=1;j<resN;j++){
			//ѡ�����Ķȵĵ�
			int cpuMDegree = -1;
			int cpuMDeInd = -1;
			for(int i=0;i<sub.nodes;i++){
				if(cpuMDegree < degreeNode[i] && subMark[i]==0){
					cpuMDegree = degreeNode[i];
					cpuMDeInd = i;
				}
			}
			if(cpuMDeInd == -1) break;
			subMark[cpuMDeInd] = 1;//���ÿ�ӳ���ʶ��1��
			//���½ڵ�Ķ�
			for(int i=0;i<sub.links;i++){
				if(sub.link[i].from == cpuMDeInd && subMark[sub.link[i].to] == 0) degreeNode[sub.link[i].to]++;
				if(sub.link[i].to == cpuMDeInd && subMark[sub.link[i].from] == 0) degreeNode[sub.link[i].from]++;
			}
		}
		
		
	}
	
	/*
	 * ������������Ƶ�ײ�
	 */
	public int CalSlots(VONRequest reqs[],int index,int md)
	{
		double bw;
		bw = -1;
		for(int i=0;i<reqs[index].links;i++){
			if(bw < reqs[index].link[i].bw){
				bw = reqs[index].link[i].bw;
			}
		}
		return 2*CalculateSlots(bw,md,Parameters.GuardBand);
	}
	/*����:��·ӳ��
  	 * ����:
  	 * retNodeE[]:�ڵ�ӳ��
  	 * retVLinkE:��·ӳ�䣬����������·0ӳ���������·���ݽṹ��
  	 * String strOb = keyVNode1+"-"+keyVNode2+"-"+slotIndex+"-"+md;
	   retVLinkE[sLinkNo].add(strOb);
	   �����Ǳ��������ݽṹ             		
	 * retSlotSE[]:��ʼ����
  	 * retSlotEE[]:��������
  	 * retLinkE[]:��·ӳ����
  	 * kShortestPath[][][]:���·������
  	 * pathEff[]:ÿ������·��ӳ�����Ч·������
  	 * pathNo[][]:·���ı��
  	 * virtualNodes[]:����ڵ�ӳ�����չͼ�ڵ���
  	 * ������:������
  	 * ����ʱ��:2020-1-21
  	 */
	public void AddLinksMapBy01ILPLin(EOSubstrateNetwork sub,VONRequest reqs[],int index,int retNodeE[],LinkedList retVLinkE[])
	{
		//0����ӳ�������ݽṹ�У�����p��ret���ݽṹ;
		int p[][] = new int[reqs[index].links][sub.nodes];
		int p1[][] = new int[reqs[index].links][sub.nodes];
		int ret[][] = new int[reqs[index].links][2];
		int[] retSlotSE = new int[reqs[index].links];
		int[] retSlotEE = new int[reqs[index].links];
		
		for(int i=0;i<reqs[index].links;i++){
			for(int j=0;j<sub.nodes;j++){
				p[i][j] = -1;//��ʼ��
				p1[i][j] = -1;//��ʼ��
			}
		}
		//�ֽ�retVLinkE[sLinkNo].add(strOb);
		for(int i=0;i<reqs[index].links;i++){
			if(retVLinkE[i] != null){
				for (Object ob: retVLinkE[i]) {  
					String str = ob.toString();
					System.out.println(i+" value:"+ str);  
					String sNode1 = str.substring(0,str.indexOf("-"));
					str = str.substring(str.indexOf("-")+1);
					String sNode2 = str.substring(0,str.indexOf("-"));
					str = str.substring(str.indexOf("-")+1);
					String slotIndex = str.substring(0,str.indexOf("-"));
					str = str.substring(str.indexOf("-")+1);
					String md = str;
					System.out.println(sNode1+"-"+sNode2+"-"+slotIndex+"-"+md);
					retSlotSE[i] = Integer.parseInt(slotIndex);//��ʼ��Ƶ�ײ�����
					double d = Math.ceil(reqs[index].link[i].bw/(12.5*Integer.parseInt(md)));
					int slotNum = (new Double(d)).intValue()+Parameters.GuardBand;//
					System.out.println("slotNum:"+(slotNum));
					if(Parameters.DebugModel){
						String str1 = "\r\nSlotNum:"+(slotNum);
						WriteFilePlus("process.txt",str1);
					}
					retSlotEE[i] = Integer.parseInt(slotIndex)+slotNum-1;//��ֹ��Ƶ�ײ�����
					if(retSlotEE[i]>sub.slotsNum) System.out.println("***error. retSlotEE["+i+"]:"+retSlotEE[i]+" slotIndex:"+slotIndex+" slotNum:"+slotNum);
					//ӳ���·��
					int iSNode1 = Integer.parseInt(sNode1);
					int iSNode2 = Integer.parseInt(sNode2);                 
					p1[i][iSNode1] = iSNode2;//��ʼ��
				}
			}
		}
		//����p
		int[] sNode = new int[sub.nodes];
		
		for(int i=0;i<reqs[index].links;i++){
			for(int ii=0;ii<sub.nodes;ii++){
				sNode[ii] = -1;
			}
			int embedNode1 = retNodeE[reqs[index].link[i].from];
			int embedNode2 = retNodeE[reqs[index].link[i].to];
			//p[i][embedNode1] = 
			while(embedNode1 != embedNode2){
				if(p1[i][embedNode1] != -1 && sNode[embedNode1] == -1){
					p[i][embedNode1] = p1[i][embedNode1];
					sNode[embedNode1] = p1[i][embedNode1];
					embedNode1 = p1[i][embedNode1];
				} else {
					for(int j=0;j<sub.nodes;j++){
						if(p1[i][j] == embedNode1 && sNode[embedNode1] == -1){
							p[i][embedNode1] = j;
							sNode[embedNode1] = j;
							embedNode1 = j;
							break;
						}
					}
				}
			}
			p[i][embedNode1] = -1;
		}
		//��ӡpath
		if(Parameters.DebugModel){
			for(int i=0;i<reqs[index].links;i++){
				int embedNode1 = retNodeE[reqs[index].link[i].from];
				int embedNode2 = retNodeE[reqs[index].link[i].to];
				PrintPath(p[i],embedNode1,embedNode2);
			}
		}
		
		for(int i=0;i<reqs[index].links;i++){
			ret[i][0] = retSlotSE[i];
			//retSlotEE[i] = retSlotEE[i]-2;
			ret[i][1] = retSlotEE[i];
		}
		
		/*
		//����p
		CreateShortestPathFromKPaths(reqs,index,kShortestPath,virtualNodes,retLinkE,pathEff,p);
		*/
		
		//1���ڵ�ӳ�䣬����sub.slots;
		for(int i=0;i<reqs[index].links;i++){
			int snode1,snode2,vnode1,vnode2;
			vnode1 = reqs[index].link[i].to;
			vnode2 = reqs[index].link[i].from;
			snode1 = v2s[index].snode.get(vnode1);
			snode2 = v2s[index].snode.get(vnode2);
			UpdateSub(sub,snode1,snode2,ret[i],p[i]);
		}
		
		//2������S2VLink s2v_l[]
		int snodeMid1,snodeMid,sNode1,req_count;
		boolean find = false;
		for(int i=0;i<reqs[index].links;i++){
			snodeMid1 = reqs[index].link[i].to;
			sNode1 = reqs[index].link[i].from;
			snodeMid1 = v2s[index].snode.get(snodeMid1);
			sNode1 = v2s[index].snode.get(sNode1);
			while(p[i][snodeMid1] != -1) {
				find = true;
				snodeMid = p[i][snodeMid1];
				req_count = s2v_l[sub.linksNo[snodeMid][snodeMid1]].req_count;
				s2v_l[sub.linksNo[snodeMid][snodeMid1]].req.add(req_count,index);
				s2v_l[sub.linksNo[snodeMid][snodeMid1]].bw.add(req_count,reqs[index].link[i].bw);
				s2v_l[sub.linksNo[snodeMid][snodeMid1]].vlink.add(req_count,i);
				s2v_l[sub.linksNo[snodeMid][snodeMid1]].rest_bw -=  reqs[index].link[i].bw;
				s2v_l[sub.linksNo[snodeMid][snodeMid1]].req_count ++;
				
				snodeMid1 = snodeMid;
				if(snodeMid1 == sNode1) break;//�ӻ�㵽Դ���·���Ѿ�����
			}
		}
		if(find == false){
			for(int i=0;i<reqs[index].links;i++){
				snodeMid1 = reqs[index].link[i].from;
				sNode1 = reqs[index].link[i].to;
				snodeMid1 = v2s[index].snode.get(snodeMid1);
				sNode1 = v2s[index].snode.get(sNode1);
				while(p[i][snodeMid1] != -1) {
					snodeMid = p[i][snodeMid1];
					req_count = s2v_l[sub.linksNo[snodeMid][snodeMid1]].req_count;
					s2v_l[sub.linksNo[snodeMid][snodeMid1]].req.add(req_count,index);
					s2v_l[sub.linksNo[snodeMid][snodeMid1]].bw.add(req_count,reqs[index].link[i].bw);
					s2v_l[sub.linksNo[snodeMid][snodeMid1]].vlink.add(req_count,i);
					s2v_l[sub.linksNo[snodeMid][snodeMid1]].rest_bw -=  reqs[index].link[i].bw;
					s2v_l[sub.linksNo[snodeMid][snodeMid1]].req_count ++;
					
					snodeMid1 = snodeMid;
					if(snodeMid1 == sNode1) break;//�ӻ�㵽Դ���·���Ѿ�����
				}
			}
		}
		
		
		//3������v2s[]����·ӳ����Ϣ
		int pathLength = 0;
		find = false;
		
		for(int i=0;i<reqs[index].links;i++){
			snodeMid1 = reqs[index].link[i].to;
			sNode1 = reqs[index].link[i].from;
			snodeMid1 = v2s[index].snode.get(snodeMid1);
			System.out.println("snodeMid1:"+snodeMid1);
			sNode1 = v2s[index].snode.get(sNode1);
			pathLength = 0;
			LinkedList<Integer> link = new LinkedList<Integer>();
			while(p[i][snodeMid1] != -1) {
				find = true;
				snodeMid = p[i][snodeMid1];
				link.add(pathLength,snodeMid1);				
				pathLength++;	//·������
				
				snodeMid1 = snodeMid;
				//if(snodeMid1 == sNode1) break;//�ӻ�㵽Դ���·���Ѿ�����
			}
			if(pathLength == 0){
				snodeMid1 = reqs[index].link[i].from;
				sNode1 = reqs[index].link[i].to;
				snodeMid1 = v2s[index].snode.get(snodeMid1);
				System.out.println("snodeMid1:"+snodeMid1);
				sNode1 = v2s[index].snode.get(sNode1);
				pathLength = 0;
				//LinkedList<Integer> link = new LinkedList<Integer>();
				while(p[i][snodeMid1] != -1) {
					snodeMid = p[i][snodeMid1];
					link.add(pathLength,snodeMid1);				
					pathLength++;	//·������
					
					snodeMid1 = snodeMid;
					//if(snodeMid1 == sNode1) break;//�ӻ�㵽Դ���·���Ѿ�����
				}
			}
			link.add(pathLength,snodeMid1);	
			
			SpathFlow pathFlow = new SpathFlow();
			pathFlow.link = link;
			pathFlow.len = pathLength;
			
			if(find){
				snodeMid1 = reqs[index].link[i].to;
			} else {
				snodeMid1 = reqs[index].link[i].from;
			}
			
			//snodeMid1 = reqs[index].link[i].from;
			snodeMid1 = v2s[index].snode.get(snodeMid1);
			System.out.println("vlink:"+i+" pathLength:"+pathLength+" snodeMid1:"+snodeMid1);
			for(int ii=0;ii<pathLength;ii++){
				snodeMid = p[i][snodeMid1];
				//System.out.print(snodeMid1+"-");
				snodeMid1 = snodeMid;
			}
			//System.out.print(snodeMid1);
			//System.out.println("");
			
			pathFlow.bw = reqs[index].link[i].bw;
			v2s[index].pathFlow.add(i,pathFlow);
			v2s[index].flowLen.add(i,1);//1����·����·ӳ�䣻i����i����·��
			v2s[index].startSlotNo.add(i,ret[i][0]);//�������ʼ����
			v2s[index].slotNum.add(i,ret[i][1]-ret[i][0]+1);	//�����Ƶ������
			
		}
		
		
		
		
		//����ӳ��ɹ���־
		v2s[index].map = Parameters.STATE_MAP_LINK;
		reqs[index].map = Parameters.STATE_MAP_LINK;
	}
	/******************************************************************
	//���ƣ�int FindVONEOptimalSolution(......)
	//���ܣ���01ILPģ��ӳ�����������, ����ɹ��򷵻�true��ret[],p[] 
	//������
	//	      ret[]Ϊ���ص�ӳ������
	//        ret[0]=minSlotIndex(��Ƶ�ײ۵ĵ�λ)
	//        ret[1]=maxSlotIndex(��Ƶ�ײ۵ĸ�λ)
	//	      p[]Ϊӳ���·��
	////	  listΪӳ�������ڵ�
	//����ֵ��true���ɹ����أ�false��ʧ�ܷ��� 
	//�����ˣ�������
	//�������ڣ�2017-09-27
	//******************************************************************/
	public boolean FindOptSoluCostByLin(int linkSum[])
	{
		BufferedReader reader = null;
		linkSum[0] = 0;
		
		try {
	            System.out.println("����Ϊ��λ��ȡ�ļ����ݣ�һ�ζ�һ���У�");
	            reader = new BufferedReader(new FileReader("glpsolRSA.o"));
	            String tempString = null;
	            
	            int line = 1;
	            boolean find = false;
	            //һ�ζ���һ�У�ֱ������nullΪ�ļ�����
	            while ((tempString = reader.readLine()) != null) {
	                //��ʾ�к� //
	            	//System.out.println("line " + line + ": " + tempString);
	                if (line == 5 && tempString.indexOf("OPTIMAL") == -1) {  //˵��δ�ҵ����Ž� 
	                	System.out.println("line " + line + ": " + tempString + "No Found the optimal resolvetion.");
	                	return false;
	                } 
	                if (line == 6) {  //�ҵ����·������minLength
	                	
	                } else if(line > 6 && tempString.indexOf(" Z[") != -1){//˵���ҵ������Ž��x����
	                	//ȥ��ǰ��Ĳ��֣�3 x[0,2]       *              1             0             1 
	                	//�Կո�ָ���ȡ��һ������
	                	find = true;
	                	String tmpStr = "";
	                	String tempString1 = tempString.trim();//268 Z[0,1,0,1,11,4]
	                	
	                	String tempString2 = reader.readLine();//*              0             0             1 
	                			
	                	tmpStr = tempString2.substring(tempString2.indexOf("*")+1);
	                	tmpStr = tmpStr.trim();
	                	
	                	tmpStr = tmpStr.substring(0,tmpStr.indexOf(" "));
	                	if(Integer.parseInt(tmpStr) == 1){//˵���ҵ���һ����
	                		linkSum[0]++;
	                	}
	                } else if(line > 6 && tempString.indexOf(" A[") != -1){//˵���ҵ������Ž��x����
	                	if(find) return true;
	                }
	                
	                line++;
	            } 
	            reader.close();
	     } catch (IOException e) {
	    	 return false;
	            //e.printStackTrace();
	     } finally {
	            if (reader != null) {
	                try {
	                    reader.close();
	                } catch (IOException e1) {
	                	
	                }
	            }
	     }  
        return true;
	}
	
	/******************************************************************
	//���ƣ�int FindVONEOptimalSolution(......)
	//���ܣ���01ILPģ��ӳ�����������, ����ɹ��򷵻�true��ret[],p[] 
	//������
	//	      ret[]Ϊ���ص�ӳ������
	//        ret[0]=minSlotIndex(��Ƶ�ײ۵ĵ�λ)
	//        ret[1]=maxSlotIndex(��Ƶ�ײ۵ĸ�λ)
	//	      p[]Ϊӳ���·��
	////	  listΪӳ�������ڵ�
	//����ֵ��true���ɹ����أ�false��ʧ�ܷ��� 
	//�����ˣ�������
	//�������ڣ�2017-09-27
	//******************************************************************/
	public boolean FindOptSoluByLin(EOSubstrateNetwork sub,VONRequest reqs[],int index,int retNodeE[],LinkedList retVLinkE[])
	{
		BufferedReader reader = null;

		int keySNode1 = -1,keySNode2 = -1;
		int keyVNode1 = -1,keyVNode2 = -1,slotIndex=-1;
		//LinkedList<String> lList = new LinkedList<String>();
		for(int i=0;i<reqs[index].links;i++){
			retVLinkE[i] = new LinkedList<String>();
		}

		//int[][] retRSlotIndex = new int[reqs[index].links][sub.links];
		//for(int i=0;i<reqs[index].links;i++)
		//	for(int j;j<sub.links;j++) retRSlotIndex[i][j] = -1;

		try {
			System.out.println("����Ϊ��λ��ȡ�ļ����ݣ�һ�ζ�һ���У�");
			reader = new BufferedReader(new FileReader("glpsolRSA.o"));
			String tempString = null;

			int line = 1;
			//һ�ζ���һ�У�ֱ������nullΪ�ļ�����
			while ((tempString = reader.readLine()) != null) {
				//��ʾ�к� //
				//System.out.println("line " + line + ": " + tempString);
				if (line == 5 && tempString.indexOf("OPTIMAL") == -1) {  //˵��δ�ҵ����Ž�
					System.out.println("line " + line + ": " + tempString + "No Found the optimal resolvetion.");
					return false;
				}
				if (line == 6) {  //�ҵ����·������minLength
					//ȥ��ǰ��ո�ȥ��ǰ�棺"Objective:  shPath = ";ȥ�����棺"(MINimum)"
					tempString = tempString.replace("Objective:  slotsMin = ", "");
					tempString = tempString.replace("(MINimum)", "");
					tempString = tempString.trim();
					//minLength = Integer.parseInt(tempString);
					//hashResolve = new Hashtable(minLength,(float)1.0);//����hash
				} else if(line > 6 && tempString.indexOf(" Z[") != -1){//˵���ҵ������Ž��x����
					//ȥ��ǰ��Ĳ��֣�3 x[0,2]       *              1             0             1
					//�Կո�ָ���ȡ��һ������
					String tmpStr = "";
					//System.out.println("line " + line + ": " + tempString);

					//String tempString1 = reader.readLine();
					//System.out.println(tempString1);
					String tempString1 = tempString.trim();//268 Z[0,1,0,1,11,4]

					String tempString2 = reader.readLine();//*              0             0             1

					tmpStr = tempString2.substring(tempString2.indexOf("*")+1);
					tmpStr = tmpStr.trim();
					//System.out.println("line " + line + ": " + tmpStr);

					tmpStr = tmpStr.substring(0,tmpStr.indexOf(" "));
					//System.out.println("line " + line + ": " + tmpStr);
					if(Integer.parseInt(tmpStr) == 1){//˵���ҵ���һ����
						//�õ�һ���⸳ֵ��tmpStr������x[0,2]
						tempString = tempString.trim();
						tmpStr = tempString.substring(tempString.indexOf(" ")+1);

						//Z[0,1,0,1,11,4],����s��keyVNode1ӳ��Ľڵ㣬t��keyVNode2ӳ��Ľڵ�

						keyVNode1 = Integer.parseInt(tmpStr.substring(tmpStr.indexOf("[")+1, tmpStr.indexOf(",")));
						//System.out.println("keyNode1:"+keySNode1);
						tmpStr = tmpStr.substring(tmpStr.indexOf(",")+1);
						keyVNode2 = Integer.parseInt(tmpStr.substring(0, tmpStr.indexOf(",")));

						tmpStr = tmpStr.substring(tmpStr.indexOf(",")+1);
						keySNode1 = Integer.parseInt(tmpStr.substring(0, tmpStr.indexOf(",")));
						tmpStr = tmpStr.substring(tmpStr.indexOf(",")+1);
						keySNode2 = Integer.parseInt(tmpStr.substring(0, tmpStr.indexOf(",")));

						tmpStr = tmpStr.substring(tmpStr.indexOf(",")+1);
						slotIndex = Integer.parseInt(tmpStr.substring(0, tmpStr.indexOf(",")));

						tmpStr = tmpStr.substring(tmpStr.indexOf(",")+1);
						int md = Integer.parseInt(tmpStr.substring(0, tmpStr.indexOf("]")));
						//System.out.println("keyNode2:"+keySNode2);
						//int retNodeE[],int retLinkE[],int retSlotSE[],int retSlotEE[],int retSlotBE[]
						//hashVLinkToPath.put(keySNode1,keySNode2);//�Ᵽ����hash����
						//Hashtable hashVLinkToPath=new Hashtable(2,(float)0.8);//

						//int sLinkNo = GetLinkNum(sub,keySNode1,keySNode2);
						int vLinkNo = GetLinkNum(reqs,index,keyVNode1,keyVNode2);
						//String strOb = keyVNode1+"-"+keyVNode2+"-"+slotIndex+"-"+md;
						String strOb = keySNode1+"-"+keySNode2+"-"+slotIndex+"-"+md;
						if(Parameters.DebugModel) WriteFilePlus("process.txt",strOb);
						//lList.add(strOb);
						retVLinkE[vLinkNo].add(strOb);// = lList;
						//int vLinkNo = GetLinkNum(reqs,index,keyVNode1,keyVNode2);
						//retRSlotIndex[vLinkNo][sLinkNo] = slotIndex;

						//retLinkE[keySNode1] = keySNode2;
					}
				} else if(line > 6 && tempString.indexOf(" A[") != -1){//˵���ҵ������Ž��x����
					String tmpStr = "";
					//System.out.println("line " + line + ": " + tempString);
					//String tempString1 = reader.readLine();
					String tempString1 = tempString.trim();

					tmpStr = tempString1.substring(tempString1.indexOf("*")+1);
					tmpStr = tmpStr.trim();
					//System.out.println("line " + line + ": " + tmpStr);

					tmpStr = tmpStr.substring(0,tmpStr.indexOf(" "));
					//System.out.println("line " + line + ": " + tmpStr);
					if(Integer.parseInt(tmpStr) == 1){//˵���ҵ���һ����
						//�õ�һ���⸳ֵ��tmpStr������x[0,2]
						//var f{(i,j) in E,(m,n) in Ev,s in Vf,t in Vf,k in MSet}, binary;
						tempString = tempString.trim();
						tmpStr = tempString.substring(tempString.indexOf(" ")+1);//ȥ��ǰ����к�
						//System.out.println("line " + line + ": " + tmpStr);
						//tmpStr = tmpStr.substring(0,tmpStr.indexOf(" "));		//�õ�f[i,j,m,n,s,t,k]
						//System.out.println("line " + line + ": " + tmpStr);
						int keyNode1 = -1;
						//keyNode1 = Integer.parseInt(tmpStr.substring(tmpStr.indexOf("[")+1, tmpStr.indexOf(",")));//�õ�f[i,j,m,n,s,t,k]��i
						//System.out.println("keyNode1:"+keyNode1);
						//M[5,1]
						keyVNode1 = Integer.parseInt(tmpStr.substring(tmpStr.indexOf("[")+1, tmpStr.indexOf(",")));
						//System.out.println("keyNode1:"+keySNode1);
						tmpStr = tmpStr.substring(tmpStr.indexOf(",")+1);
						keySNode1 = Integer.parseInt(tmpStr.substring(0, tmpStr.indexOf("]")));

						retNodeE[keyVNode1] = keySNode1;//����ڵ�keyVNode1��ӳ�䵽����ڵ�keySNode1��
					}
				}

				line++;
			}
			reader.close();
		} catch (IOException e) {
			return false;
			//e.printStackTrace();
		} finally {
			if (reader != null) {
				try {
					reader.close();
				} catch (IOException e1) {

				}
			}
		}
		return true;
	}
	public boolean FindOptSoluBynodes(EOSubstrateNetwork sub,VONRequest reqs[],int index,int retNodeE[])
	{
		BufferedReader reader = null;

		int keySNode1 = -1,keySNode2 = -1;
		int keyVNode1 = -1,keyVNode2 = -1,slotIndex=-1;
		//LinkedList<String> lList = new LinkedList<String>();

		//int[][] retRSlotIndex = new int[reqs[index].links][sub.links];
		//for(int i=0;i<reqs[index].links;i++)
		//	for(int j;j<sub.links;j++) retRSlotIndex[i][j] = -1;

		try {
			System.out.println("����Ϊ��λ��ȡ�ļ����ݣ�һ�ζ�һ���У�");
			reader = new BufferedReader(new FileReader("glpsolRSA.o"));
			String tempString = null;

			int line = 1;
			//һ�ζ���һ�У�ֱ������nullΪ�ļ�����
			while ((tempString = reader.readLine()) != null) {
				//��ʾ�к� //
				//System.out.println("line " + line + ": " + tempString);
				if (line == 5 && tempString.indexOf("OPTIMAL") == -1) {  //˵��δ�ҵ����Ž�
					System.out.println("line " + line + ": " + tempString + "No Found the optimal resolvetion.");
					return false;
				}
				if (line == 6) {  //�ҵ����·������minLength
					//ȥ��ǰ��ո�ȥ��ǰ�棺"Objective:  shPath = ";ȥ�����棺"(MINimum)"
					tempString = tempString.replace("Objective:  slotsMin = ", "");
					tempString = tempString.replace("(MINimum)", "");
					tempString = tempString.trim();
					//minLength = Integer.parseInt(tempString);
					//hashResolve = new Hashtable(minLength,(float)1.0);//����hash
				} else if(line > 6 && tempString.indexOf(" A[") != -1){//˵���ҵ������Ž��x����
					String tmpStr = "";
					//System.out.println("line " + line + ": " + tempString);
					//String tempString1 = reader.readLine();
					String tempString1 = tempString.trim();

					tmpStr = tempString1.substring(tempString1.indexOf("*")+1);
					tmpStr = tmpStr.trim();
					//System.out.println("line " + line + ": " + tmpStr);

					tmpStr = tmpStr.substring(0,tmpStr.indexOf(" "));
					//System.out.println("line " + line + ": " + tmpStr);
					if(Integer.parseInt(tmpStr) == 1){//˵���ҵ���һ����
						//�õ�һ���⸳ֵ��tmpStr������x[0,2]
						//var f{(i,j) in E,(m,n) in Ev,s in Vf,t in Vf,k in MSet}, binary;
						tempString = tempString.trim();
						tmpStr = tempString.substring(tempString.indexOf(" ")+1);//ȥ��ǰ����к�
						//System.out.println("line " + line + ": " + tmpStr);
						//tmpStr = tmpStr.substring(0,tmpStr.indexOf(" "));		//�õ�f[i,j,m,n,s,t,k]
						//System.out.println("line " + line + ": " + tmpStr);
						int keyNode1 = -1;
						//keyNode1 = Integer.parseInt(tmpStr.substring(tmpStr.indexOf("[")+1, tmpStr.indexOf(",")));//�õ�f[i,j,m,n,s,t,k]��i
						//System.out.println("keyNode1:"+keyNode1);
						//M[5,1]
						keyVNode1 = Integer.parseInt(tmpStr.substring(tmpStr.indexOf("[")+1, tmpStr.indexOf(",")));
						//System.out.println("keyNode1:"+keySNode1);
						tmpStr = tmpStr.substring(tmpStr.indexOf(",")+1);
						keySNode1 = Integer.parseInt(tmpStr.substring(0, tmpStr.indexOf("]")));

						retNodeE[keyVNode1] = keySNode1;//����ڵ�keyVNode1��ӳ�䵽����ڵ�keySNode1��
					}
				}

				line++;
			}
			reader.close();
		} catch (IOException e) {
			return false;
			//e.printStackTrace();
		} finally {
			if (reader != null) {
				try {
					reader.close();
				} catch (IOException e1) {

				}
			}
		}
		return true;
	}
	public boolean FindOptSoluBylink(EOSubstrateNetwork sub,VONRequest reqs[],int index,int retNodeE[],LinkedList retVLinkE[])
	{
		BufferedReader reader = null;

		int keySNode1 = -1,keySNode2 = -1;
		int keyVNode1 = -1,keyVNode2 = -1,slotIndex=-1;
		//LinkedList<String> lList = new LinkedList<String>();
		for(int i=0;i<reqs[index].links;i++){
			retVLinkE[i] = new LinkedList<String>();
		}

		//int[][] retRSlotIndex = new int[reqs[index].links][sub.links];
		//for(int i=0;i<reqs[index].links;i++)
		//	for(int j;j<sub.links;j++) retRSlotIndex[i][j] = -1;

		try {
			System.out.println("����Ϊ��λ��ȡ�ļ����ݣ�һ�ζ�һ���У�");
			reader = new BufferedReader(new FileReader("glpsolRSA1.o"));
			String tempString = null;

			int line = 1;
			//һ�ζ���һ�У�ֱ������nullΪ�ļ�����
			while ((tempString = reader.readLine()) != null) {
				//��ʾ�к� //
				//System.out.println("line " + line + ": " + tempString);
				if (line == 5 && tempString.indexOf("OPTIMAL") == -1) {  //˵��δ�ҵ����Ž�
					System.out.println("line " + line + ": " + tempString + "No Found the optimal resolvetion.");
					return false;
				}
				if (line == 6) {  //�ҵ����·������minLength
					//ȥ��ǰ��ո�ȥ��ǰ�棺"Objective:  shPath = ";ȥ�����棺"(MINimum)"
					tempString = tempString.replace("Objective:  slotsMin = ", "");
					tempString = tempString.replace("(MINimum)", "");
					tempString = tempString.trim();
					//minLength = Integer.parseInt(tempString);
					//hashResolve = new Hashtable(minLength,(float)1.0);//����hash
				} else if(line > 6 && tempString.indexOf(" Z[") != -1){//˵���ҵ������Ž��x����
					//ȥ��ǰ��Ĳ��֣�3 x[0,2]       *              1             0             1
					//�Կո�ָ���ȡ��һ������
					String tmpStr = "";
					//System.out.println("line " + line + ": " + tempString);

					//String tempString1 = reader.readLine();
					//System.out.println(tempString1);
					String tempString1 = tempString.trim();//268 Z[0,1,0,1,11,4]

					String tempString2 = reader.readLine();//*              0             0             1

					tmpStr = tempString2.substring(tempString2.indexOf("*")+1);
					tmpStr = tmpStr.trim();
					//System.out.println("line " + line + ": " + tmpStr);

					tmpStr = tmpStr.substring(0,tmpStr.indexOf(" "));
					//System.out.println("line " + line + ": " + tmpStr);
					if(Integer.parseInt(tmpStr) == 1){//˵���ҵ���һ����
						//�õ�һ���⸳ֵ��tmpStr������x[0,2]
						tempString = tempString.trim();
						tmpStr = tempString.substring(tempString.indexOf(" ")+1);

						//Z[0,1,0,1,11,4],����s��keyVNode1ӳ��Ľڵ㣬t��keyVNode2ӳ��Ľڵ�

						keyVNode1 = Integer.parseInt(tmpStr.substring(tmpStr.indexOf("[")+1, tmpStr.indexOf(",")));
						//System.out.println("keyNode1:"+keySNode1);
						tmpStr = tmpStr.substring(tmpStr.indexOf(",")+1);
						keyVNode2 = Integer.parseInt(tmpStr.substring(0, tmpStr.indexOf(",")));

						tmpStr = tmpStr.substring(tmpStr.indexOf(",")+1);
						keySNode1 = Integer.parseInt(tmpStr.substring(0, tmpStr.indexOf(",")));
						tmpStr = tmpStr.substring(tmpStr.indexOf(",")+1);
						keySNode2 = Integer.parseInt(tmpStr.substring(0, tmpStr.indexOf(",")));

						tmpStr = tmpStr.substring(tmpStr.indexOf(",")+1);
						slotIndex = Integer.parseInt(tmpStr.substring(0, tmpStr.indexOf(",")));

						tmpStr = tmpStr.substring(tmpStr.indexOf(",")+1);
						int md = Integer.parseInt(tmpStr.substring(0, tmpStr.indexOf("]")));
						//System.out.println("keyNode2:"+keySNode2);
						//int retNodeE[],int retLinkE[],int retSlotSE[],int retSlotEE[],int retSlotBE[]
						//hashVLinkToPath.put(keySNode1,keySNode2);//�Ᵽ����hash����
						//Hashtable hashVLinkToPath=new Hashtable(2,(float)0.8);//

						//int sLinkNo = GetLinkNum(sub,keySNode1,keySNode2);
						int vLinkNo = GetLinkNum(reqs,index,keyVNode1,keyVNode2);
						//String strOb = keyVNode1+"-"+keyVNode2+"-"+slotIndex+"-"+md;
						String strOb = keySNode1+"-"+keySNode2+"-"+slotIndex+"-"+md;
						if(Parameters.DebugModel) WriteFilePlus("process.txt",strOb);
						//lList.add(strOb);
						retVLinkE[vLinkNo].add(strOb);// = lList;
						//int vLinkNo = GetLinkNum(reqs,index,keyVNode1,keyVNode2);
						//retRSlotIndex[vLinkNo][sLinkNo] = slotIndex;

						//retLinkE[keySNode1] = keySNode2;
					}
				} else if(line > 6 && tempString.indexOf(" A[") != -1){//˵���ҵ������Ž��x����
					String tmpStr = "";
					//System.out.println("line " + line + ": " + tempString);
					//String tempString1 = reader.readLine();
					String tempString1 = tempString.trim();

					tmpStr = tempString1.substring(tempString1.indexOf("*")+1);
					tmpStr = tmpStr.trim();
					//System.out.println("line " + line + ": " + tmpStr);

					tmpStr = tmpStr.substring(0,tmpStr.indexOf(" "));
					//System.out.println("line " + line + ": " + tmpStr);
					if(Integer.parseInt(tmpStr) == 1){//˵���ҵ���һ����
						//�õ�һ���⸳ֵ��tmpStr������x[0,2]
						//var f{(i,j) in E,(m,n) in Ev,s in Vf,t in Vf,k in MSet}, binary;
						tempString = tempString.trim();
						tmpStr = tempString.substring(tempString.indexOf(" ")+1);//ȥ��ǰ����к�
						//System.out.println("line " + line + ": " + tmpStr);
						//tmpStr = tmpStr.substring(0,tmpStr.indexOf(" "));		//�õ�f[i,j,m,n,s,t,k]
						//System.out.println("line " + line + ": " + tmpStr);
						int keyNode1 = -1;
						//keyNode1 = Integer.parseInt(tmpStr.substring(tmpStr.indexOf("[")+1, tmpStr.indexOf(",")));//�õ�f[i,j,m,n,s,t,k]��i
						//System.out.println("keyNode1:"+keyNode1);
						//M[5,1]
						keyVNode1 = Integer.parseInt(tmpStr.substring(tmpStr.indexOf("[")+1, tmpStr.indexOf(",")));
						//System.out.println("keyNode1:"+keySNode1);
						tmpStr = tmpStr.substring(tmpStr.indexOf(",")+1);
						keySNode1 = Integer.parseInt(tmpStr.substring(0, tmpStr.indexOf("]")));

						retNodeE[keyVNode1] = keySNode1;//����ڵ�keyVNode1��ӳ�䵽����ڵ�keySNode1��
					}
				}

				line++;
			}
			reader.close();
		} catch (IOException e) {
			return false;
			//e.printStackTrace();
		} finally {
			if (reader != null) {
				try {
					reader.close();
				} catch (IOException e1) {

				}
			}
		}
		return true;
	}
	private void CreateVONE01ILPByLinUndirectDiagramSub1(EOSubstrateNetwork sub,VONRequest reqs[],int index,List<Integer> ilpsubnet,double[] energysNodePageRank)
	{
		int M = sub.slotsNum;
		//�ҳ���ѡ���Ӧ�ĵײ���·23.12.6
		List<Integer>Linknumber =new ArrayList<>();
		for (int k = 0; k < ilpsubnet.size()-1; k++) {
			for (int kk = k+1; kk < ilpsubnet.size(); kk++)
				for (int m = 0; m < sub.links; m++) {
					if ((ilpsubnet.get(k) == sub.link[m].from && ilpsubnet.get(kk) == sub.link[m].to) || (ilpsubnet.get(k) == sub.link[m].to && ilpsubnet.get(kk) == sub.link[m].from)) {
						Linknumber.add(m);
					}
				}
		}

		Tools myDowith = new Tools();

		String data;
		data = "set Nv:=";
		for(int i = 0; i < reqs[index].nodes; i++){
			data += " " + i;
		}
		data += ";\r\n";
		myDowith.SaveFile("glpsolRSA.dat", data, false);

		data = "set Lv:=\r\n";
		for(int j = 0; j < reqs[index].links; j ++) {
			if(reqs[index].link[j].from < reqs[index].link[j].to)
				data += reqs[index].link[j].from + " " + reqs[index].link[j].to + "\r\n";
		}
		data += ";\r\n";
		myDowith.SaveFile("glpsolRSA.dat", data, true);

		//�޸ĵײ�ڵ㷶Χ 23.12.5
		data = "set Ns:=";
		for(int j = 0; j < ilpsubnet.size(); j ++) {
			data += ilpsubnet.get(j) + " ";
		}
		data += ";\r\n";
		myDowith.SaveFile("glpsolRSA.dat", data, true);
		//��Ӧ�޸�
		data = "";
		for(int j = 0; j < ilpsubnet.size(); j ++) {
			data += "set Nss[" + ilpsubnet.get(j) + "]:=";
			for(int i=0;i<Linknumber.size();i++){
				if(sub.link[Linknumber.get(i)].from == ilpsubnet.get(j)){
					if(sub.link[Linknumber.get(i)].from < sub.link[Linknumber.get(i)].to) //chenxhɾ��20200903���޸�����ͼΪ����ͼ
						data += sub.link[Linknumber.get(i)].to + " ";
				}
				if(sub.link[i].to == ilpsubnet.get(j)){
					//if(sub.link[i].from < sub.link[i].to) //chenxhɾ��20200903���޸�����ͼΪ����ͼ
					//data += sub.link[i].from + " ";
				}
			}
			data += ";\r\n";
		}
		myDowith.SaveFile("glpsolRSA.dat", data, true);
		//��Ӧ�޸�
		data = "";
		for(int j = 0; j <  ilpsubnet.size(); j++) {
			data = "set Nss1[" + ilpsubnet.get(j) + "]:=";
			for(int i=0;i<Linknumber.size();i++){
				if(sub.link[Linknumber.get(i)].to == ilpsubnet.get(j)){
					if(sub.link[Linknumber.get(i)].from < sub.link[Linknumber.get(i)].to) //chenxhɾ��20200903���޸�����ͼΪ����ͼ
						data += sub.link[Linknumber.get(i)].from + " ";
				}
				if(sub.link[i].from == ilpsubnet.get(j)){
					//if(sub.link[i].from < sub.link[i].to) //chenxhɾ��20200903���޸�����ͼΪ����ͼ
					//data += sub.link[i].to + " ";
				}
			}
			data += ";\r\n";
			myDowith.SaveFile("glpsolRSA.dat", data, true);
		}

		//��Ҫ���⼸���ڵ����γɵ�������·�ҳ���
		data = "set Ls:=\r\n";
		for(int j = 0; j < Linknumber.size(); j ++) {
			if(sub.link[Linknumber.get(j)].from < sub.link[Linknumber.get(j)].to)//chenxhɾ��20200903���޸�����ͼΪ����ͼ
				data += sub.link[Linknumber.get(j)].from + " " + sub.link[Linknumber.get(j)].to + "\r\n";
			//data += sub.link[j].to + " " + sub.link[j].from + "\r\n";//add chenxh20200903
		}
		data += ";\r\n";
		myDowith.SaveFile("glpsolRSA.dat", data, true);

		//��Ӧ�޸�
		data = "";
		for(int j = 0; j < reqs[index].nodes; j ++) {
			data += "set X["+j+"]:=";
			for(int i=0;i< ilpsubnet.size();i++){
				if(s2v_n[ilpsubnet.get(i)].rest_cpu >= reqs[index].cpu[j]){//
					data += ilpsubnet.get(i) + " ";
				}
			}
			data += ";\r\n";
		}
		myDowith.SaveFile("glpsolRSA.dat", data, true);

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
			if(reqs[index].link[j].from > reqs[index].link[j].to) continue;//chenxhɾ��20200903����ͼת����ͼ
			for(int i=0;i<sub.slotsNum;i++){
				for(int k=1;k<9;){
					data = "set Slot["+reqs[index].link[j].from+","+reqs[index].link[j].to+","+i+","+k+"]:=";
					for(int p=i;p<sub.slotsNum&&(p<i+Math.ceil(reqs[index].link[j].bw/(12.5*k))+sub.diffSlot);p++){
						//if(i+Math.ceil(reqs[index].link[j].bw/(12.5*k))+sub.diffSlot > sub.slotsNum) {
						//	break;
						//}
						data += p + " ";
						//break;
					}
					data += ";\r\n";
					myDowith.SaveFile("glpsolRSA.dat", data, true);
					if(k>=4) k+=2;
					else k++;
				}
			}
		}


		//set Slot1{(i,j) in Lv,a in SlotIndex,model in MD};/*��a+|bw/model|+G-1��a+|bw/model|+G-1+1,...,a*/
		data = "";
		for(int j = 0; j < reqs[index].links; j ++) {
			if(reqs[index].link[j].from > reqs[index].link[j].to) continue;//chenxhɾ��20200903
			for(int i=0;i<sub.slotsNum;i++){
				for(int k=1;k<9;){
					data = "set Slot1["+reqs[index].link[j].from+","+reqs[index].link[j].to+","+i+","+k+"]:=";
					for(int p=i;p>=0&&(p>=i-Math.ceil(reqs[index].link[j].bw/(12.5*k))-sub.diffSlot+1);p--){
						//if(i-Math.ceil(reqs[index].link[j].bw/(12.5*k))-sub.diffSlot+1<0) {
						//	break;
						//}
						data += p + " ";
					}
					data += ";\r\n";
					if(k>=4) k+=2;
					else k++;
					myDowith.SaveFile("glpsolRSA.dat", data, true);
				}
			}
		}

		//��Ӧ�޸�
		//param c{k in Ns};/*����ڵ�ļ��㵥λ����*/
		//param c{k in Ns};/*����ڵ�ļ��㵥λ����*/
		data = "param c:=\r\n";
		if(Parameters.CurrentVONEMethod == Parameters.MapVONE01ILPLinCXH || Parameters.CurrentVONEMethod == Parameters.MapVONE01ILPPRLinCXH || Parameters.CurrentVONEMethod == Parameters.MapVONECXHNode||Parameters.CurrentVONEMethod == Parameters.MapVONENCRbyILP||Parameters.CurrentVONEMethod == Parameters.MapVONEILPAM||Parameters.CurrentVONEMethod == Parameters.MapVONEILPAM915){
			for (int i = 0; i < ilpsubnet.size(); i++) {
				data += ilpsubnet.get(i) + " " + s2v_n[ilpsubnet.get(i)].rest_cpu +"\r\n";
			}
		} else if(Parameters.CurrentVONEMethod == Parameters.MapVONE01ILPLin){
			for (int i = 0; i < sub.nodes; i++) {
				data += i + " " + 1.0/s2v_n[i].rest_cpu +"\r\n";//s2v_n[i].rest_cpu
			}
		}
		data += ";\r\n";
		myDowith.SaveFile("glpsolRSA.dat", data, true);

		if(Parameters.CurrentVONEMethod == Parameters.MapVONE01ILPLin_EquilibriumCXH){
			data = "param costCPU:=\r\n";
			for (int i = 0; i < sub.nodes; i++) {
				for(int j = 0;j < reqs[index].nodes;j++)
					if(s2v_n[i].rest_cpu >= reqs[index].cpu[j])
						data += i + " " + j + " " + 1.0/(s2v_n[i].rest_cpu-reqs[index].cpu[j]) +"\r\n";//s2v_n[i].rest_cpu
			}
			data += ";\r\n";
			myDowith.SaveFile("glpsolRSA.dat", data, true);
		}

		data = "";
		if(Parameters.CurrentVONEMethod == Parameters.MapVONE01ILPPRLinCXH) {
			data = "param ac:=\r\n";
			//����pagerankֵ
			double vNodePageRank[] = new double[reqs[index].nodes];
			double sNodePageRank[] = new double[sub.nodes];
			//	InitVNodePageRank(reqs,index);

			vNodePageRank = InitVNodePageRank(vNodePageRank, reqs, index);
			sNodePageRank = InitSNodePageRank(sNodePageRank, sub);
			if (Parameters.RecordLogModel) {
				String str = "Check vNodePR[]=";
				for (int i = 0; i < reqs[index].nodes; i++) {
					str += "" + i + " " + vNodePageRank[i] + " ";
				}
				WriteFilePlus("process.txt", str);

				str = "Check sNodePR[]=";
				for (int i = 0; i < sub.nodes; i++) {
					str += "" + i + " " + sNodePageRank[i] + " ";
				}
				WriteFilePlus("process.txt", str);
			}
			for (int i = 0; i < reqs[index].nodes; i++) {
				for (int j = 0; j < sub.nodes; j++) {
					if (reqs[index].cpu[i] <= s2v_n[j].rest_cpu) {
						if (vNodePageRank[i] <= sNodePageRank[j])
							data += i + " " + j + " " + (sNodePageRank[j] - vNodePageRank[i]) + "\r\n";
						else data += i + " " + j + " " + (vNodePageRank[i] - sNodePageRank[j]) + "\r\n";
					}
				}
			}
			data += ";\r\n";
			myDowith.SaveFile("glpsolRSA.dat", data, true);
		}
		//��Ӧ�޸�
		//param cs{(i,j) in Ls};/*������·��Ƶ�ײ۵�λ���ۣ��볤�ȳɱ���*/
		data = "param cs:=\r\n";
		for (int i = 0; i < Linknumber.size(); i++) {
			if(sub.link[Linknumber.get(i)].from > sub.link[Linknumber.get(i)].to) continue;//chenxhɾ��20200903
			data += sub.link[Linknumber.get(i)].from + " " + sub.link[Linknumber.get(i)].to + " " + sub.link[Linknumber.get(i)].length+"\r\n";
			//data += sub.link[i].to + " " + sub.link[i].from + " " + sub.link[i].length+"\r\n";//20200903����
		}
		data += ";\r\n";
		myDowith.SaveFile("glpsolRSA.dat", data, true);

		//param p{i in Nv};/*����ڵ�CPU*/
		data = "param p:=\r\n";
		for (int i = 0; i < reqs[index].nodes; i++) {
			data += i + " " + reqs[index].cpu[i]+"\r\n";
		}
		data += ";\r\n";
		myDowith.SaveFile("glpsolRSA.dat", data, true);

		//param pbw{(i,j) in Lv};/*������·��������*/
		data = "param pbw:=\r\n";
		for (int i = 0; i < reqs[index].links; i++) {
			if(reqs[index].link[i].from > reqs[index].link[i].to) continue;
			data += reqs[index].link[i].from + " " + reqs[index].link[i].to + " " + reqs[index].link[i].bw+"\r\n";
		}
		data += ";\r\n";
		myDowith.SaveFile("glpsolRSA.dat", data, true);

		//param F;/*����Ƶ�ײ�����*/
		data = "param F:="+(sub.slotsNum-1)+";\r\n";
		myDowith.SaveFile("glpsolRSA.dat", data, true);
		//��Ӧ�޸�
		//param U{k in Ns};/*����ڵ�k��ʣ��cpu*/
		data = "param U:=\r\n";
		for (int i = 0; i < ilpsubnet.size(); i++) {
			data += ilpsubnet.get(i) + " " + s2v_n[ilpsubnet.get(i)].rest_cpu +"\r\n";//s2v_n[i].rest_cpu
		}
		data += ";\r\n";
		myDowith.SaveFile("glpsolRSA.dat", data, true);
		//��Ӧ�޸�
		//param USlot{(i,j) in Ls,t in SlotIndex};/*������·Ƶ�ײ۵�״̬��1��ʾ���У�0��ʾ��ռ��*/
		data = "param USlot:=\r\n";
		for (int i = 0; i < Linknumber.size(); i++) {
			if(sub.link[Linknumber.get(i)].from > sub.link[Linknumber.get(i)].to) continue;//chenxhɾ��20200903
			for(int j=0;j<sub.slotsNum;j++){
				data += sub.link[Linknumber.get(i)].from + " " + sub.link[Linknumber.get(i)].to + " " +j + " "+ sub.slots[Linknumber.get(i)][j]+"\r\n";
				//data += sub.link[i].to + " " + sub.link[i].from + " " +j + " "+ sub.slots[i][j]+"\r\n";//20200903����
			}
		}
		data += ";\r\n";
		myDowith.SaveFile("glpsolRSA.dat", data, true);
		//��Ӧ�޸�
		//param d{(i,j) in Ls};/*������·�ĳ���*/
		data = "param d:=\r\n";
		for (int i = 0; i < Linknumber.size(); i++) {
			if(sub.link[Linknumber.get(i)].from > sub.link[Linknumber.get(i)].to) continue;//chenxhɾ��20200903
			data += sub.link[Linknumber.get(i)].from + " " + sub.link[Linknumber.get(i)].to + " " + sub.link[Linknumber.get(i)].length+"\r\n";
			//data += sub.link[i].to + " " + sub.link[i].from + " " + sub.link[i].length+"\r\n";//����20200903
		}
		data += ";\r\n";
		myDowith.SaveFile("glpsolRSA.dat", data, true);

		//param dis{m in MD};/*����ģʽ��������*/
		data = "param dis:=\r\n";
		data += "1 3000\r\n";
		data += "2 1500\r\n";
		data += "3 750\r\n";
		data += "4 375\r\n";
		data += "6 94\r\n";
		data += "8 24\r\n";
		data += ";\r\n";
		myDowith.SaveFile("glpsolRSA.dat", data, true);

		//param R;/*��������*/
		data = "param R:=100000000;\r\n";
		myDowith.SaveFile("glpsolRSA.dat", data, true);

		//param G;/*���������*/
		data = "param G:="+sub.diffSlot+";\r\n";
		myDowith.SaveFile("glpsolRSA.dat", data, true);


		//���˴���
		data = "";
		//23.12.5
		/*if (Parameters.CurrentVONEMethod==Parameters.MapVONEILPAM915) {
			data = "param sv:=\r\n";
			double sNodeAM[] = new double[sub.nodes];
			sNodeAM = InitSNodeAM(sNodeAM, sub);
			for (int i = 0; i < reqs[index].nodes; i++) {
				for (int j = 0; j < sub.nodes; j++) {
					data += i + " " + j + " " + (sNodeAM[j]/ reqs[index].cpu[i] ) + "\r\n";
				}
			}
			data += ";\r\n";
			myDowith.SaveFile("glpsolRSA.dat", data, true);
		}*/


		if (Parameters.CurrentVONEMethod==Parameters.MapVONEILPAM915) {
			data = "param sv:=\r\n";
			double sNodeAM[] = new double[ilpsubnet.size()];
			sNodeAM = InitSNodeAM1(sNodeAM, ilpsubnet);
			for (int i = 0; i < reqs[index].nodes; i++) {
				for (int j = 0; j < ilpsubnet.size(); j++) {
					data += i + " " + ilpsubnet.get(j) + " " + (sNodeAM[j]/ reqs[index].cpu[i] ) + "\r\n";
				}
			}
			data += ";\r\n";
			myDowith.SaveFile("glpsolRSA.dat", data, true);
		}


		if(Parameters.CurrentVONEMethod == Parameters.MapVONENCRbyILP){
			data = "param sv:=\r\n";
			//����pagerankֵ
			double vNodePageRank[] = new double[reqs[index].nodes];
			double sNodePageRank[] = new double[sub.nodes];
			//	InitVNodePageRank(reqs,index);

			vNodePageRank=InitVNodePageRankOfGHG(vNodePageRank,reqs,index);
			sNodePageRank= InitSNodePageRankOfGHG(sNodePageRank, sub);

			for(int i=0;i<reqs[index].nodes;i++){
				for (int j = 0; j < sub.nodes; j++) {
					if(reqs[index].cpu[i]<=s2v_n[j].rest_cpu){
						if(vNodePageRank[i] <= sNodePageRank[j])
							data += i + " " + j + " " + (sNodePageRank[j]-vNodePageRank[i]) +"\r\n";
						else data += i + " " + j + " " + (vNodePageRank[i]-sNodePageRank[j]) +"\r\n";
					}
				}
			}
			data += ";\r\n";
			myDowith.SaveFile("glpsolRSA.dat", data, true);
		}



		System.out.println("Done");

		try {
			String s;
			Process process = null;

			if(Parameters.CurrentVONEMethod == Parameters.MapVONEILPAM915){
				process = Runtime.getRuntime().exec("cmd /c C:/VNE/DRL/VNE_GHG_4/glpk-4.60/w64/glpsol.exe -m C:/VNE/DRL/VNE_GHG_4/glpk-4.60/w64/glpsolMILPAM.mod -d glpsolRSA.dat -o glpsolRSA.o");
			}

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
	private void CreateVONE01ILPByLinUndirectDiagramSub2(EOSubstrateNetwork sub,VONRequest reqs[],int index,List<Integer> ilpsubnet,double[] energysNodePageRank)
	{

		//�ҳ���ѡ���Ӧ�ĵײ���·23.12.6

		Tools myDowith = new Tools();

		String data;
		data = "set Nv:=";
		for(int i = 0; i < reqs[index].nodes; i++){
			data += " " + i;
		}
		data += ";\r\n";
		myDowith.SaveFile("glpsolRSA.dat", data, false);


		//�޸ĵײ�ڵ㷶Χ 23.12.5
		data = "set Ns:=";
		for(int j = 0; j < ilpsubnet.size(); j ++) {
			data += ilpsubnet.get(j) + " ";
		}
		data += ";\r\n";
		myDowith.SaveFile("glpsolRSA.dat", data, true);
		//��Ӧ�޸�

		//��Ӧ�޸�
		data = "";
		for(int j = 0; j < reqs[index].nodes; j ++) {
			data += "set X["+j+"]:=";
			for(int i=0;i< ilpsubnet.size();i++){
				if(s2v_n[ilpsubnet.get(i)].rest_cpu >= reqs[index].cpu[j]){//
					data += ilpsubnet.get(i) + " ";
				}
			}
			data += ";\r\n";
		}
		myDowith.SaveFile("glpsolRSA.dat", data, true);


		//��Ӧ�޸�
		//param c{k in Ns};/*����ڵ�ļ��㵥λ����*/
		data = "param c:=\r\n";
		if(Parameters.CurrentVONEMethod == Parameters.MapVONE01ILPLinCXH || Parameters.CurrentVONEMethod == Parameters.MapVONE01ILPPRLinCXH || Parameters.CurrentVONEMethod == Parameters.MapVONECXHNode||Parameters.CurrentVONEMethod == Parameters.MapVONENCRbyILP||Parameters.CurrentVONEMethod == Parameters.MapVONEILPAM||Parameters.CurrentVONEMethod == Parameters.MapVONEILPAM915||Parameters.CurrentVONEMethod == Parameters.MapVONEILPAM916){
			for (int i = 0; i < ilpsubnet.size(); i++) {
				data += ilpsubnet.get(i) + " " + s2v_n[ilpsubnet.get(i)].rest_cpu +"\r\n";
			}
		} else if(Parameters.CurrentVONEMethod == Parameters.MapVONE01ILPLin){
			for (int i = 0; i < sub.nodes; i++) {
				data += i + " " + 1.0/s2v_n[i].rest_cpu +"\r\n";//s2v_n[i].rest_cpu
			}
		}
		data += ";\r\n";
		myDowith.SaveFile("glpsolRSA.dat", data, true);

		//param p{i in Nv};/*����ڵ�CPU*/
		data = "param p:=\r\n";
		for (int i = 0; i < reqs[index].nodes; i++) {
			data += i + " " + reqs[index].cpu[i]+"\r\n";
		}
		data += ";\r\n";
		myDowith.SaveFile("glpsolRSA.dat", data, true);


		data = "param U:=\r\n";
		for (int i = 0; i < ilpsubnet.size(); i++) {
			data += ilpsubnet.get(i) + " " + s2v_n[ilpsubnet.get(i)].rest_cpu +"\r\n";//s2v_n[i].rest_cpu
		}
		data += ";\r\n";
		myDowith.SaveFile("glpsolRSA.dat", data, true);
		//��Ӧ�޸����˴���


		if (Parameters.CurrentVONEMethod==Parameters.MapVONEILPAM916) {
			data = "param sv:=\r\n";
			double sNodeAM[] = new double[ilpsubnet.size()];
			sNodeAM = InitSNodeAM1(sNodeAM, ilpsubnet);
			for (int i = 0; i < reqs[index].nodes; i++) {
				for (int j = 0; j < ilpsubnet.size(); j++) {
					if(reqs[index].cpu[i]<=s2v_n[ilpsubnet.get(j)].rest_cpu) {
						data += i + " " + ilpsubnet.get(j) + " " + (sNodeAM[j] / reqs[index].cpu[i]) + "\r\n";
					}
				}
			}
			data += ";\r\n";
			myDowith.SaveFile("glpsolRSA.dat", data, true);
		}





		System.out.println("Done");

		try {
			String s;
			Process process = null;

			if(Parameters.CurrentVONEMethod == Parameters.MapVONEILPAM916){
				process = Runtime.getRuntime().exec("cmd /c C:/VNE/DRL/VNE_GHG_4/glpk-4.60/w64/glpsol.exe -m C:/VNE/DRL/VNE_GHG_4/glpk-4.60/w64/glpsolMILPAM1.mod -d glpsolRSA.dat -o glpsolRSA.o");
			}

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
	private void CreateVONE01ILPByLinUndirectDiagram3(EOSubstrateNetwork sub, VONRequest reqs[], int index, int retNodeE[], double[] energysNodePageRank) {
		int M = sub.slotsNum;

		List<Integer> Linknumber = new ArrayList<>();
		for (int k = 0; k < retNodeE.length - 1; k++) {
			for (int kk = k + 1; kk < retNodeE.length; kk++)
				for (int m = 0; m < sub.links; m++) {
					if ((retNodeE[k] == sub.link[m].from && retNodeE[kk] == sub.link[m].to) ||
							(retNodeE[k] == sub.link[m].to && retNodeE[kk] == sub.link[m].from)) {
						Linknumber.add(m);
					}
				}
		}

		Tools myDowith = new Tools();

		String data;

		data = "set Nv:=";
		for (int i = 0; i < reqs[index].nodes; i++) {
			data += " " + i;
		}
		data += ";\r\n";
		myDowith.SaveFile("glpsolRSA1.dat", data, false);

		data = "set Lv:=\r\n";
		for (int j = 0; j < reqs[index].links; j++) {
			if (reqs[index].link[j].from < reqs[index].link[j].to)
				data += reqs[index].link[j].from + " " + reqs[index].link[j].to + "\r\n";
		}
		data += ";\r\n";
		myDowith.SaveFile("glpsolRSA1.dat", data, true);

		data = "set Ns:=";
		for (int j = 0; j < retNodeE.length; j++) {
			data += retNodeE[j] + " ";
		}
		data += ";\r\n";
		myDowith.SaveFile("glpsolRSA1.dat", data, true);

		data = "";
		for (int j = 0; j < retNodeE.length; j++) {
			data += "set Nss[" + retNodeE[j] + "]:=";
			for (int i = 0; i < Linknumber.size(); i++) {
				if (sub.link[Linknumber.get(i)].from == retNodeE[j]) {
					if (sub.link[Linknumber.get(i)].from < sub.link[Linknumber.get(i)].to)
						data += sub.link[Linknumber.get(i)].to + " ";
				}
			}
			data += ";\r\n";
		}
		myDowith.SaveFile("glpsolRSA1.dat", data, true);

		data = "";
		for (int j = 0; j < retNodeE.length; j++) {
			data = "set Nss1[" + retNodeE[j] + "]:=";
			for (int i = 0; i < Linknumber.size(); i++) {
				if (sub.link[Linknumber.get(i)].to == retNodeE[j]) {
					if (sub.link[Linknumber.get(i)].from < sub.link[Linknumber.get(i)].to)
						data += sub.link[Linknumber.get(i)].from + " ";
				}
			}
			data += ";\r\n";
			myDowith.SaveFile("glpsolRSA1.dat", data, true);
		}

		data = "set Ls:=\r\n";
		for (int j = 0; j < Linknumber.size(); j++) {
			if (sub.link[Linknumber.get(j)].from < sub.link[Linknumber.get(j)].to)
				data += sub.link[Linknumber.get(j)].from + " " + sub.link[Linknumber.get(j)].to + "\r\n";
		}
		data += ";\r\n";
		myDowith.SaveFile("glpsolRSA1.dat", data, true);

		data = "set SlotIndex:=";
		for (int j = 0; j < sub.slotsNum; j++) {
			data += j + " ";
		}
		data += ";\r\n";
		myDowith.SaveFile("glpsolRSA1.dat", data, true);

		data = "set MD:=1,2,3,4,6,8;\r\n";
		myDowith.SaveFile("glpsolRSA1.dat", data, true);

		data = "";
		for (int j = 0; j < reqs[index].links; j++) {
			if (reqs[index].link[j].from > reqs[index].link[j].to) continue;
			for (int i = 0; i < sub.slotsNum; i++) {
				for (int k = 1; k < 9;) {
					data = "set Slot[" + reqs[index].link[j].from + "," + reqs[index].link[j].to + "," + i + "," + k + "]:=";
					for (int p = i; p < sub.slotsNum && (p < i + Math.ceil(reqs[index].link[j].bw / (12.5 * k)) + sub.diffSlot); p++) {
						data += p + " ";
					}
					data += ";\r\n";
					myDowith.SaveFile("glpsolRSA1.dat", data, true);
					if (k >= 4) k += 2;
					else k++;
				}
			}
		}

		data = "";
		for (int j = 0; j < reqs[index].links; j++) {
			if (reqs[index].link[j].from > reqs[index].link[j].to) continue;
			for (int i = 0; i < sub.slotsNum; i++) {
				for (int k = 1; k < 9;) {
					data = "set Slot1[" + reqs[index].link[j].from + "," + reqs[index].link[j].to + "," + i + "," + k + "]:=";
					for (int p = i; p >= 0 && (p >= i - Math.ceil(reqs[index].link[j].bw / (12.5 * k)) - sub.diffSlot + 1); p--) {
						data += p + " ";
					}
					data += ";\r\n";
					if (k >= 4) k += 2;
					else k++;
					myDowith.SaveFile("glpsolRSA1.dat", data, true);
				}
			}
		}

		data = "param cs:=\r\n";
		for (int i = 0; i < Linknumber.size(); i++) {
			if (sub.link[Linknumber.get(i)].from > sub.link[Linknumber.get(i)].to) continue;
			data += sub.link[Linknumber.get(i)].from + " " + sub.link[Linknumber.get(i)].to + " " + sub.link[Linknumber.get(i)].length + "\r\n";
		}
		data += ";\r\n";
		myDowith.SaveFile("glpsolRSA1.dat", data, true);

		data = "param pbw:=\r\n";
		for (int i = 0; i < reqs[index].links; i++) {
			if (reqs[index].link[i].from > reqs[index].link[i].to) continue;
			data += reqs[index].link[i].from + " " + reqs[index].link[i].to + " " + reqs[index].link[i].bw + "\r\n";
		}
		data += ";\r\n";
		myDowith.SaveFile("glpsolRSA1.dat", data, true);

		data = "param F:=" + (sub.slotsNum - 1) + ";\r\n";
		myDowith.SaveFile("glpsolRSA1.dat", data, true);


		//param USlot{(i,j) in Ls,t in SlotIndex};/*������·Ƶ�ײ۵�״̬��1��ʾ���У�0��ʾ��ռ��*/
		data = "param USlot:=\r\n";
		for (int i = 0; i < Linknumber.size(); i++) {
			if(sub.link[Linknumber.get(i)].from > sub.link[Linknumber.get(i)].to) continue;//chenxhɾ��20200903
			for(int j=0;j<sub.slotsNum;j++){
				data += sub.link[Linknumber.get(i)].from + " " + sub.link[Linknumber.get(i)].to + " " +j + " "+ sub.slots[Linknumber.get(i)][j]+"\r\n";
				//data += sub.link[i].to + " " + sub.link[i].from + " " +j + " "+ sub.slots[i][j]+"\r\n";//20200903����
			}
		}
		data += ";\r\n";
		myDowith.SaveFile("glpsolRSA1.dat", data, true);

		//param d{(i,j) in Ls};/*������·�ĳ���*/
			data = "param d:=\r\n";
			for (int i = 0; i < Linknumber.size(); i++) {
				if (sub.link[Linknumber.get(i)].from > sub.link[Linknumber.get(i)].to) continue;
				data += sub.link[Linknumber.get(i)].from + " " + sub.link[Linknumber.get(i)].to + " " + sub.link[Linknumber.get(i)].length + "\r\n";
			}
			data += ";\r\n";
			myDowith.SaveFile("glpsolRSA1.dat", data, true);

			data = "param dis:=\r\n";
			data += "1 3000\r\n";
			data += "2 1500\r\n";
			data += "3 750\r\n";
			data += "4 375\r\n";
			data += "6 94\r\n";
			data += "8 24\r\n";
			data += ";\r\n";
			myDowith.SaveFile("glpsolRSA1.dat", data, true);

			data = "param R:=100000000;\r\n";
			myDowith.SaveFile("glpsolRSA1.dat", data, true);

			data = "param G:=" + sub.diffSlot + ";\r\n";
			myDowith.SaveFile("glpsolRSA1.dat", data, true);

// ��� A_result ����
			data = "param A:=\r\n";
			for (int i = 0; i < reqs[index].nodes; i++) {
				int mappedNode = retNodeE[i]; // ��ȡ�ڵ� i ӳ�䵽������ڵ�
				data += i + " " + mappedNode + " " + 1 + "\r\n"; // ���� A[i, mappedNode] = 1
			}
			data += ";\r\n"; // ������������
			myDowith.SaveFile("glpsolRSA1.dat", data, true);

			System.out.println("Done");

		try {
			String s;
			// ����ļ�·��

			Process process = null;

			if(Parameters.CurrentVONEMethod == Parameters.MapVONEILPAM915){
				process = Runtime.getRuntime().exec("cmd /c D:/360Downloads/ylc/��ز���/��ز���/VNE_GHG_4/glpk-4.60/w64/glpsol.exe  -m D:/360Downloads/ylc/��ز���/��ز���/VNE_GHG_4/glpk-4.60/w64/glpsolMILPAM2.mod -d D:/360Downloads/ylc/��ز���/��ز���/VNE_GHG_4/glpsolRSA1.dat -o D:/360Downloads/ylc/��ز���/��ز���/VNE_GHG_4/glpsolRSA1.o");
			}

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
	private void CreateVONE01ILPByLinUndirectDiagramSub9166(EOSubstrateNetwork sub,VONRequest reqs[],int index,List<Integer> ilpsubnet,double[] energysNodePageRank)
	{

		//�ҳ���ѡ���Ӧ�ĵײ���·23.12.6

		Tools myDowith = new Tools();

		String data;
		data = "set Nv:=";
		for(int i = 0; i < reqs[index].nodes; i++){
			data += " " + i;
		}
		data += ";\r\n";
		myDowith.SaveFile("glpsolRSA.dat", data, false);


		//�޸ĵײ�ڵ㷶Χ 23.12.5
		data = "set Ns:=";
		for(int j = 0; j < ilpsubnet.size(); j ++) {
			data += ilpsubnet.get(j) + " ";
		}
		data += ";\r\n";
		myDowith.SaveFile("glpsolRSA.dat", data, true);
		//��Ӧ�޸�

		//��Ӧ�޸�
		data = "";
		for(int j = 0; j < reqs[index].nodes; j ++) {
			data += "set X["+j+"]:=";
			for(int i=0;i< ilpsubnet.size();i++){
				if(s2v_n[ilpsubnet.get(i)].rest_cpu >= reqs[index].cpu[j]){//
					data += ilpsubnet.get(i) + " ";
				}
			}
			data += ";\r\n";
		}
		myDowith.SaveFile("glpsolRSA.dat", data, true);


		//��Ӧ�޸�
		//param c{k in Ns};/*����ڵ�ļ��㵥λ����*/
		data = "param c:=\r\n";
		if(Parameters.CurrentVONEMethod == Parameters.MapVONEILPAM9166){
			for (int i = 0; i < ilpsubnet.size(); i++) {
				data += ilpsubnet.get(i) + " " + s2v_n[ilpsubnet.get(i)].rest_cpu +"\r\n";
			}
		} else if(Parameters.CurrentVONEMethod == Parameters.MapVONE01ILPLin){
			for (int i = 0; i < sub.nodes; i++) {
				data += i + " " + 1.0/s2v_n[i].rest_cpu +"\r\n";//s2v_n[i].rest_cpu
			}
		}
		data += ";\r\n";
		myDowith.SaveFile("glpsolRSA.dat", data, true);

		//param p{i in Nv};/*����ڵ�CPU*/
		data = "param p:=\r\n";
		for (int i = 0; i < reqs[index].nodes; i++) {
			data += i + " " + reqs[index].cpu[i]+"\r\n";
		}
		data += ";\r\n";
		myDowith.SaveFile("glpsolRSA.dat", data, true);

		data = "param U:=\r\n";
		for (int i = 0; i < ilpsubnet.size(); i++) {
			data += ilpsubnet.get(i) + " " + s2v_n[ilpsubnet.get(i)].rest_cpu +"\r\n";//s2v_n[i].rest_cpu
		}
		data += ";\r\n";
		myDowith.SaveFile("glpsolRSA.dat", data, true);


		if (Parameters.CurrentVONEMethod==Parameters.MapVONEILPAM916) {
			data = "param sv:=\r\n";
			double sNodeAM[] = new double[sub.nodes];
			sNodeAM = InitSNodeAM(sNodeAM, sub);
			for (int i = 0; i < reqs[index].nodes; i++) {
				for (int j = 0; j < sub.nodes; j++) {
					if(s2v_n[j].rest_cpu >= reqs[index].cpu[i]) { // ȷ������ڵ����㹻��Դ
						data += i + " " + j + " " + (sNodeAM[j]/ reqs[index].cpu[i] ) + "\r\n";
						//data += i + " " + j + " " + sNodeAM[j] + "\r\n";
					}
				}
			}
			data += ";\r\n";
			myDowith.SaveFile("glpsolRSA.dat", data, true);
		}
		if (Parameters.CurrentVONEMethod==Parameters.MapVONEILPAM9166) {
			data = "param sv:=\r\n";
			double MIS[] = new double[sub.nodes];
			double[][] array = new double[sub.nodes][sub.nodes];
			int[][] degreeArray = new int[sub.nodes][sub.nodes];
			//����ӳ����ڽӾ���  ֵΪӳ��Ĵ���
			for (int i=0; i<s2v_l.length;i++){
				if (s2v_l[i].req.size()!=0){
					array[sub.link[i].from][sub.link[i].to]=sub.link[i].bw-s2v_l[i].rest_bw;
					array[sub.link[i].to][sub.link[i].from]=sub.link[i].bw-s2v_l[i].rest_bw;
					degreeArray[sub.link[i].from][sub.link[i].to]+=s2v_l[i].req.size();
					degreeArray[sub.link[i].to][sub.link[i].from]+=s2v_l[i].req.size();
				}
			}
			for (int i = 0; i < reqs[index].nodes; i++) {
				for (int j = 0; j <ilpsubnet.size(); j++) {
					int physicalNode = ilpsubnet.get(j);
					if (s2v_n[physicalNode].rest_cpu >= reqs[index].cpu[i]) {
						int degreeValue = 0;
						for (int k = 0; k < sub.nodes; k++) {
							degreeValue += degreeArray[j][k]; // �ۼӽڵ�j���������Ӷ���
						}

						if (Math.abs(s2v_n[physicalNode].rest_cpu - sub.maxcpu[physicalNode]) < 0.0001) {
							// �����ܺĲ������Ӷ���Ӱ�� (150+1329.33+120+150+85*degreeValue+80)
							//double baseEnergy = sub.node_GHG[j] * 400;
							double baseEnergy = sub.node_GHG[physicalNode] * (20 + 100 + 40 + 2.5 + 400);
							//double baseEnergy = sub.node_GHG[j] * (400+120+132.933+85*degreeValue);
							//double baseEnergy = sub.node_GHG[j] * (150 + 1329.33 + 120 + 150 + 85*degreeValue + 80) * 100 / 3600000;
							// ��̬�ܺı���ԭ���㷽ʽ
							//double dynamicEnergy = sub.node_GHG[j]*600*reqs[index].cpu[i]/sub.maxcpu[j];
							//double dynamicEnergy = sub.node_GHG[physicalNode]*600*0.18*0.465*reqs[index].cpu[i]/sub.maxcpu[physicalNode];
							double dynamicEnergy = sub.node_GHG[physicalNode]*600*0.075*reqs[index].cpu[i]/sub.maxcpu[physicalNode];
							//double dynamicEnergy = sub.node_GHG[physicalNode] * ((600 / sub.maxcpu[physicalNode]) + (0.18 + 0.465) * array[i][physicalNode]);
							//double dynamicEnergy = sub.node_GHG[j]*150*(sub.maxcpu[j]-sub.cpu[j])/sub.maxcpu[j]*100/3600000*reqs[index].cpu[i];
							MIS[physicalNode] = baseEnergy + dynamicEnergy;
						} else {
							// �Ѽ���ڵ�ֻ����㶯̬�ܺ�
							MIS[physicalNode] = sub.node_GHG[physicalNode]*600*0.075*reqs[index].cpu[i]/sub.maxcpu[physicalNode];//sub.node_GHG[j]*600*reqs[index].cpu[i]/sub.maxcpu[j];
						}
						data += i + " " + physicalNode + " " + MIS[physicalNode] + "\r\n";
					}
				}
			}
			data += ";\r\n";
			myDowith.SaveFile("glpsolRSA.dat", data, true);
		}





		System.out.println("Done");

		try {
			String s;
			Process process = null;

			if(Parameters.CurrentVONEMethod == Parameters.MapVONEILPAM916||Parameters.CurrentVONEMethod == Parameters.MapVONEILPAM9166){
				process = Runtime.getRuntime().exec("cmd /c D:/360Downloads/ylc/��ز���/��ز���/VNE_GHG_4/glpk-4.60/w64/glpsol.exe -m D:/360Downloads/ylc/��ز���/��ز���/VNE_GHG_4/glpk-4.60/w64//glpsolMILPAM3.mod -d glpsolRSA.dat -o glpsolRSA.o");
			}

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
    private void CreateVONE01ILPByLinUndirectDiagramSub4(EOSubstrateNetwork sub,VONRequest reqs[],int index) {

		//�ҳ���ѡ���Ӧ�ĵײ���·23.12.6

		Tools myDowith = new Tools();

		String data;
		data = "set Nv:=";
		for (int i = 0; i < reqs[index].nodes; i++) {
			data += " " + i;
		}
		data += ";\r\n";
		myDowith.SaveFile("glpsolRSA.dat", data, false);


		//�޸ĵײ�ڵ㷶Χ 23.12.5
		data = "set Ns:=";
		for (int j = 0; j < sub.nodes; j++) {
			data += j + " ";
		}
		data += ";\r\n";
		myDowith.SaveFile("glpsolRSA.dat", data, true);
		//��Ӧ�޸�

		//��Ӧ�޸�
		data = "";
		for (int j = 0; j < reqs[index].nodes; j++) {
			data += "set X[" + j + "]:=";
			for (int i = 0; i < sub.nodes; i++) {
				if (s2v_n[i].rest_cpu >= reqs[index].cpu[j]) {//
					data += i + " ";
				}
			}
			data += ";\r\n";
		}
		myDowith.SaveFile("glpsolRSA.dat", data, true);


		//��Ӧ�޸�
		//param c{k in Ns};/*����ڵ�ļ��㵥λ����*/
		data = "param c:=\r\n";
		if (Parameters.CurrentVONEMethod == Parameters.MapVONE01ILPLinCXH || Parameters.CurrentVONEMethod == Parameters.MapVONE01ILPPRLinCXH || Parameters.CurrentVONEMethod == Parameters.MapVONECXHNode || Parameters.CurrentVONEMethod == Parameters.MapVONENCRbyILP || Parameters.CurrentVONEMethod == Parameters.MapVONEILPAM || Parameters.CurrentVONEMethod == Parameters.MapVONEILPAM915 || Parameters.CurrentVONEMethod == Parameters.MapVONEILPAM916 || Parameters.CurrentVONEMethod == Parameters.MapVONEILPAM9166) {
			for (int i = 0; i < sub.nodes; i++) {
				data += i + " " + s2v_n[i].rest_cpu + "\r\n";//s2v_n[i].rest_cpu
			}
		} else if (Parameters.CurrentVONEMethod == Parameters.MapVONE01ILPLin) {
			for (int i = 0; i < sub.nodes; i++) {
				data += i + " " + 1.0 / s2v_n[i].rest_cpu + "\r\n";//s2v_n[i].rest_cpu
			}
		}
		data += ";\r\n";
		myDowith.SaveFile("glpsolRSA.dat", data, true);

		//param p{i in Nv};/*����ڵ�CPU*/
		data = "param p:=\r\n";
		for (int i = 0; i < reqs[index].nodes; i++) {
			data += i + " " + reqs[index].cpu[i] + "\r\n";
		}
		data += ";\r\n";
		myDowith.SaveFile("glpsolRSA.dat", data, true);

		data = "param U:=\r\n";
		for (int i = 0; i < sub.nodes; i++) {
			data += i + " " + s2v_n[i].rest_cpu + "\r\n";//s2v_n[i].rest_cpu
		}
		data += ";\r\n";
		myDowith.SaveFile("glpsolRSA.dat", data, true);


		//for (int i = 0; i < reqs[index].nodes; i++) {
//				for (int j = 0; j < sub.nodes; j++) {
//					// Ϊÿ������ڵ�j����ʵʱ����
//					if (s2v_n[j].rest_cpu >= reqs[index].cpu[i]) {
//						int degreeValue = 0;
//						for (int k = 0; k < sub.nodes; k++) {
//							degreeValue += degreeArray[j][k]; // �ۼӽڵ�j���������Ӷ���
//						}
//
//						if (Math.abs(s2v_n[j].rest_cpu - sub.maxcpu[j]) < 0.0001) {
//							// �����ܺĲ������Ӷ���Ӱ�� (150+1329.33+120+150+85*degreeValue+80)
//							//double baseEnergy = sub.node_GHG[j] * 400;
//							double baseEnergy = sub.node_GHG[j] * (400+120+132.933);
//							//double baseEnergy = sub.node_GHG[j] * (400+120+132.933+85*degreeValue);
//							//double baseEnergy = sub.node_GHG[j] * (150 + 1329.33 + 120 + 150 + 85*degreeValue + 80) * 100 / 3600000;
//							// ��̬�ܺı���ԭ���㷽ʽ
//							//double dynamicEnergy = sub.node_GHG[j]*600*reqs[index].cpu[i]/sub.maxcpu[j];
//							double dynamicEnergy = sub.node_GHG[j]*600*0.18*0.465*reqs[index].cpu[i]/sub.maxcpu[j];
//							//double dynamicEnergy = sub.node_GHG[j]*150*(sub.maxcpu[j]-sub.cpu[j])/sub.maxcpu[j]*100/3600000*reqs[index].cpu[i];
//							MIS[j] = baseEnergy + dynamicEnergy;
//						} else {
//							// �Ѽ���ڵ�ֻ����㶯̬�ܺ�
//							MIS[j] = sub.node_GHG[j]*600*0.18*0.465*reqs[index].cpu[i]/sub.maxcpu[j];//sub.node_GHG[j]*600*reqs[index].cpu[i]/sub.maxcpu[j];
//						}

		if (Parameters.CurrentVONEMethod==Parameters.MapVONEILPAM9166 || Parameters.CurrentVONEMethod==Parameters.MapVONEILPAM916) {
			data = "param sv:=\r\n";
			double MIS[] = new double[sub.nodes];
			double[][] array = new double[sub.nodes][sub.nodes];
			int[][] degreeArray = new int[sub.nodes][sub.nodes];
			//����ӳ����ڽӾ���  ֵΪӳ��Ĵ���
			for (int i=0; i<s2v_l.length;i++){
				if (s2v_l[i].req.size()!=0){
					array[sub.link[i].from][sub.link[i].to]=sub.link[i].bw-s2v_l[i].rest_bw;
					array[sub.link[i].to][sub.link[i].from]=sub.link[i].bw-s2v_l[i].rest_bw;
					degreeArray[sub.link[i].from][sub.link[i].to]+=s2v_l[i].req.size();
					degreeArray[sub.link[i].to][sub.link[i].from]+=s2v_l[i].req.size();
				}
			}
			for (int i = 0; i < reqs[index].nodes; i++) {
				for (int j = 0; j < sub.nodes; j++) {
					// Ϊÿ������ڵ�j����ʵʱ����
					if (s2v_n[j].rest_cpu >= reqs[index].cpu[i]) {
						int degreeValue = 0;
						for (int k = 0; k < sub.nodes; k++) {
							degreeValue += degreeArray[j][k]; // �ۼӽڵ�j���������Ӷ���
						}

						double Fci = sub.node_GHG[j];
						double Efi = 20 + 100 + 40 + 2.5 + 400;
						double Eli = 600;
						double Csi = sub.maxcpu[j];

						// ? ���������ڵ� j ���Ѿ�ӳ�������ڵ�ļ�����Դ�ܺ�
						// ������֪����ڵ����CPU + ��ǰ�ڵ��CPU
						double sum = 0;
						sum = reqs[index].cpu[i];
						double sumOtherVirtualCPU = 0;
						sumOtherVirtualCPU += sub.maxcpu[j] - s2v_n[j].rest_cpu + reqs[index].cpu[i];

						if (Math.abs(s2v_n[j].rest_cpu - sub.maxcpu[j]) < 0.0001) {
							// �Ǽ���ڵ㣬���ǹ̶��ܺ�
							double baseEnergy = Fci * Eli * (sumOtherVirtualCPU / Csi);
							double dynamicEnergy = Fci * (Efi + Eli * (sumOtherVirtualCPU / Csi));
							MIS[j] = baseEnergy + dynamicEnergy;
						} else {
							// �Ѽ���ڵ㣬�����Ƕ�̬�ܺ�
							MIS[j] = Fci * (Efi + Eli * (sumOtherVirtualCPU / Csi));
						}
						data += i + " " + j + " " + MIS[j] + "\r\n";
					}
				}
			}
			data += ";\r\n";
			myDowith.SaveFile("glpsolRSA.dat", data, true);
		}



			System.out.println("Done");

			try {
				String s;
				Process process = null;

				if (Parameters.CurrentVONEMethod == Parameters.MapVONEILPAM916 || Parameters.CurrentVONEMethod == Parameters.MapVONEILPAM9166) {
					process = Runtime.getRuntime().exec("cmd /c C:/VNE/DRL/VNE_GHG_4/glpk-4.60/w64/glpsol.exe -m C:/VNE/DRL/VNE_GHG_4/glpk-4.60/w64/glpsolMILPAM1.mod -d glpsolRSA.dat -o glpsolRSA.o");
				}

				BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(process.getInputStream()));
				while ((s = bufferedReader.readLine()) != null)
					System.out.println(s);
				process.waitFor();
				System.out.println("It has done the exec.");
			} catch (Exception ex) {
				JOptionPane.showMessageDialog(null, ex.getStackTrace());
			}
		}

		private void CreateVONE01ILPByLinUndirectDiagramSub5(EOSubstrateNetwork sub,VONRequest reqs[],int index) {

		//�ҳ���ѡ���Ӧ�ĵײ���·23.12.6

		Tools myDowith = new Tools();

		String data;
		data = "set Nv:=";
		for(int i = 0; i < reqs[index].nodes; i++){
			data += " " + i;
		}
		data += ";\r\n";
		myDowith.SaveFile("glpsolRSA.dat", data, false);


		//�޸ĵײ�ڵ㷶Χ 23.12.5
		data = "set Ns:=";
		for(int j = 0; j < sub.nodes; j ++) {
			data += j + " ";
		}
		data += ";\r\n";
		myDowith.SaveFile("glpsolRSA.dat", data, true);
		//��Ӧ�޸�

		//��Ӧ�޸�
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


		if(Parameters.CurrentVONEMethod == Parameters.MapVONE01ILPLin_EquilibriumCXHnodeilp){
			data = "param costCPU:=\r\n";
			for (int i = 0; i < sub.nodes; i++) {
				for(int j = 0;j < reqs[index].nodes;j++)
					if(s2v_n[i].rest_cpu >= reqs[index].cpu[j])
						data += i + " " + j + " " + 1.0/(s2v_n[i].rest_cpu-reqs[index].cpu[j]) +"\r\n";//s2v_n[i].rest_cpu
			}
			data += ";\r\n";
			myDowith.SaveFile("glpsolRSA.dat", data, true);
		}


		System.out.println("Done");

		try {
			String s;
			Process process = null;

			if(Parameters.CurrentVONEMethod == Parameters.MapVONE01ILPLin_EquilibriumCXHnodeilp){
				process = Runtime.getRuntime().exec("cmd /c D:/360Downloads/ylc/��ز���/��ز���/VNE_GHG_4/glpk-4.60/w64/glpsol.exe -m D:/360Downloads/ylc/��ز���/��ز���/VNE_GHG_4/glpk-4.60/w64/glpsolMILPByLinUndirectDiagram-Equilibriumnodeilp.mod -d glpsolRSA.dat -o glpsolRSA.o");
			}

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
	private void CreateVONE01ILPByLinUndirectDiagramSub6(EOSubstrateNetwork sub,VONRequest reqs[],int index)
	{

		//�ҳ���ѡ���Ӧ�ĵײ���·23.12.6

		Tools myDowith = new Tools();

		String data;
		data = "set Nv:=";
		for(int i = 0; i < reqs[index].nodes; i++){
			data += " " + i;
		}
		data += ";\r\n";
		myDowith.SaveFile("glpsolRSA.dat", data, false);


		//�޸ĵײ�ڵ㷶Χ 23.12.5
		data = "set Ns:=";
		for(int j = 0; j < sub.nodes; j ++) {
			data += j + " ";
		}
		data += ";\r\n";
		myDowith.SaveFile("glpsolRSA.dat", data, true);
		//��Ӧ�޸�

		//��Ӧ�޸�
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

		data = "param c:=\r\n";
		if(Parameters.CurrentVONEMethod == Parameters.MapVONE01ILPLinCXH || Parameters.CurrentVONEMethod == Parameters.MapVONE01ILPPRLinCXH || Parameters.CurrentVONEMethod == Parameters.MapVONECXHNode||Parameters.CurrentVONEMethod == Parameters.MapVONENCRbyILP||Parameters.CurrentVONEMethod == Parameters.MapVONEILPAM){
			for (int i = 0; i < sub.nodes; i++) {
				data += i + " " + s2v_n[i].rest_cpu +"\r\n";//s2v_n[i].rest_cpu
			}
		} else if(Parameters.CurrentVONEMethod == Parameters.MapVONE01ILPLinnodeilp){
			for (int i = 0; i < sub.nodes; i++) {
				data += i + " " + 1.0/s2v_n[i].rest_cpu +"\r\n";//s2v_n[i].rest_cpu
			}
		}
		data += ";\r\n";
		myDowith.SaveFile("glpsolRSA.dat", data, true);
            //param p{i in Nv};/*����ڵ�CPU*/
		data = "param p:=\r\n";
		for (int i = 0; i < reqs[index].nodes; i++) {
			data += i + " " + reqs[index].cpu[i]+"\r\n";
		}
		data += ";\r\n";
		myDowith.SaveFile("glpsolRSA.dat", data, true);

		System.out.println("Done");

		try {
			String s;
			Process process = null;

			if(Parameters.CurrentVONEMethod == Parameters.MapVONE01ILPLinnodeilp){
				process = Runtime.getRuntime().exec("cmd /c D:/360Downloads/ylc/��ز���/��ز���/VNE_GHG_4/glpk-4.60/w64/glpsol.exe -m D:/360Downloads/ylc/��ز���/��ز���/VNE_GHG_4/glpk-4.60/w64/glpsolMILPByLinnodeilp.mod -d glpsolRSA.dat -o glpsolRSA.o");
			}

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
	private void CreateVONE01ILPByLinUndirectDiagramSub7(EOSubstrateNetwork sub,VONRequest reqs[],int index)
	{

		//�ҳ���ѡ���Ӧ�ĵײ���·23.12.6

		Tools myDowith = new Tools();

		String data;
		data = "set Nv:=";
		for(int i = 0; i < reqs[index].nodes; i++){
			data += " " + i;
		}
		data += ";\r\n";
		myDowith.SaveFile("glpsolRSA.dat", data, false);


		//�޸ĵײ�ڵ㷶Χ 23.12.5
		data = "set Ns:=";
		for(int j = 0; j < sub.nodes; j ++) {
			data += j + " ";
		}
		data += ";\r\n";
		myDowith.SaveFile("glpsolRSA.dat", data, true);
		//��Ӧ�޸�

		//��Ӧ�޸�
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


		//param p{i in Nv};/*����ڵ�CPU*/
		data = "param p:=\r\n";
		for (int i = 0; i < reqs[index].nodes; i++) {
			data += i + " " + reqs[index].cpu[i]+"\r\n";
		}
		data += ";\r\n";
		myDowith.SaveFile("glpsolRSA.dat", data, true);
		if(Parameters.CurrentVONEMethod == Parameters.MapVONENCRbyILP||Parameters.CurrentVONEMethod == Parameters.MapVONENCRbyILPnodeilp){
			data = "param sv:=\r\n";
			//����pagerankֵ
			double vNodePageRank[] = new double[reqs[index].nodes];
			double sNodePageRank[] = new double[sub.nodes];
			//	InitVNodePageRank(reqs,index);

			vNodePageRank=InitVNodePageRankOfGHG(vNodePageRank,reqs,index);
			sNodePageRank= InitSNodePageRankOfGHG(sNodePageRank, sub);

			for(int i=0;i<reqs[index].nodes;i++){
				for (int j = 0; j < sub.nodes; j++) {
					if(reqs[index].cpu[i]<=s2v_n[j].rest_cpu){
						if(vNodePageRank[i] <= sNodePageRank[j])
							data += i + " " + j + " " + (sNodePageRank[j]-vNodePageRank[i]) +"\r\n";
						else data += i + " " + j + " " + (vNodePageRank[i]-sNodePageRank[j]) +"\r\n";
					}
				}
			}
			data += ";\r\n";
			myDowith.SaveFile("glpsolRSA.dat", data, true);
		}

		System.out.println("Done");

		try {
			String s;
			Process process = null;

			if(Parameters.CurrentVONEMethod == Parameters.MapVONENCRbyILPnodeilp){
				process = Runtime.getRuntime().exec("cmd /c D:/360Downloads/ylc/��ز���/��ز���/VNE_GHG_4/glpk-4.60/w64/glpsol.exe -m D:/360Downloads/ylc/��ز���/��ز���/VNE_GHG_4/glpk-4.60/w64/glpsolMILPNCRnodeilp.mod -d glpsolRSA.dat -o glpsolRSA.o");
			}

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
	/*
	/*
	 * void CreateVONE01ILPByLin()
	 * ���ܣ�����01ILPģ�����
	 */
	private void CreateVONE01ILPByLinUndirectDiagram(EOSubstrateNetwork sub,VONRequest reqs[],int index)
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
		
		data = "set Lv:=\r\n";
		for(int j = 0; j < reqs[index].links; j ++) {
			if(reqs[index].link[j].from < reqs[index].link[j].to)
				data += reqs[index].link[j].from + " " + reqs[index].link[j].to + "\r\n";
		}
		data += ";\r\n"; 		 
		myDowith.SaveFile("glpsolRSA.dat", data, true);
		
		data = "set Ns:=";
		for(int j = 0; j < sub.nodes; j ++) {
			data += j + " ";
		}
		data += ";\r\n"; 		 
		myDowith.SaveFile("glpsolRSA.dat", data, true);
		
		data = "";
		for(int j = 0; j < sub.nodes; j ++) {
			data += "set Nss[" + j + "]:=";
			for(int i=0;i<sub.links;i++){
				if(sub.link[i].from == j){
					if(sub.link[i].from < sub.link[i].to) //chenxhɾ��20200903���޸�����ͼΪ����ͼ
						data += sub.link[i].to + " ";
				}
				if(sub.link[i].to == j){
					//if(sub.link[i].from < sub.link[i].to) //chenxhɾ��20200903���޸�����ͼΪ����ͼ
						//data += sub.link[i].from + " ";
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
					if(sub.link[i].from < sub.link[i].to) //chenxhɾ��20200903���޸�����ͼΪ����ͼ
						data += sub.link[i].from + " ";
				}
				if(sub.link[i].from == j){
					//if(sub.link[i].from < sub.link[i].to) //chenxhɾ��20200903���޸�����ͼΪ����ͼ
						//data += sub.link[i].to + " ";
				}
			}
			data += ";\r\n"; 
			myDowith.SaveFile("glpsolRSA.dat", data, true);
		}		 
		
		
		data = "set Ls:=\r\n";
		for(int j = 0; j < sub.links; j ++) {
			if(sub.link[j].from < sub.link[j].to)//chenxhɾ��20200903���޸�����ͼΪ����ͼ
				data += sub.link[j].from + " " + sub.link[j].to + "\r\n";
				//data += sub.link[j].to + " " + sub.link[j].from + "\r\n";//add chenxh20200903
		}
		data += ";\r\n"; 		 
		myDowith.SaveFile("glpsolRSA.dat", data, true);
		
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
			if(reqs[index].link[j].from > reqs[index].link[j].to) continue;//chenxhɾ��20200903����ͼת����ͼ
			for(int i=0;i<sub.slotsNum;i++){
				for(int k=1;k<9;){
					data = "set Slot["+reqs[index].link[j].from+","+reqs[index].link[j].to+","+i+","+k+"]:=";
					for(int p=i;p<sub.slotsNum&&(p<i+Math.ceil(reqs[index].link[j].bw/(12.5*k))+sub.diffSlot);p++){
						//if(i+Math.ceil(reqs[index].link[j].bw/(12.5*k))+sub.diffSlot > sub.slotsNum) {
						//	break;
						//}
						data += p + " ";
						//break;
					}
					data += ";\r\n"; 
					myDowith.SaveFile("glpsolRSA.dat", data, true);
					if(k>=4) k+=2;
					else k++;
				}
			}
		}	
		
		
		//set Slot1{(i,j) in Lv,a in SlotIndex,model in MD};/*��a+|bw/model|+G-1��a+|bw/model|+G-1+1,...,a*/
		data = "";
		for(int j = 0; j < reqs[index].links; j ++) {
			if(reqs[index].link[j].from > reqs[index].link[j].to) continue;//chenxhɾ��20200903
			for(int i=0;i<sub.slotsNum;i++){
				for(int k=1;k<9;){
					data = "set Slot1["+reqs[index].link[j].from+","+reqs[index].link[j].to+","+i+","+k+"]:=";
					for(int p=i;p>=0&&(p>=i-Math.ceil(reqs[index].link[j].bw/(12.5*k))-sub.diffSlot+1);p--){
						//if(i-Math.ceil(reqs[index].link[j].bw/(12.5*k))-sub.diffSlot+1<0) {
						//	break;
						//}
						data += p + " ";
					}
					data += ";\r\n"; 
					if(k>=4) k+=2;
					else k++;
					myDowith.SaveFile("glpsolRSA.dat", data, true);
				}
			}
		}	
		
		
		//param c{k in Ns};/*����ڵ�ļ��㵥λ����*/
		data = "param c:=\r\n";
		if(Parameters.CurrentVONEMethod == Parameters.MapVONE01ILPLinCXH || Parameters.CurrentVONEMethod == Parameters.MapVONE01ILPPRLinCXH || Parameters.CurrentVONEMethod == Parameters.MapVONECXHNode||Parameters.CurrentVONEMethod == Parameters.MapVONENCRbyILP||Parameters.CurrentVONEMethod == Parameters.MapVONEILPAM){
			for (int i = 0; i < sub.nodes; i++) {
				data += i + " " + s2v_n[i].rest_cpu +"\r\n";//s2v_n[i].rest_cpu
			}
		} else if(Parameters.CurrentVONEMethod == Parameters.MapVONE01ILPLin){
			for (int i = 0; i < sub.nodes; i++) {
				data += i + " " + 1.0/s2v_n[i].rest_cpu +"\r\n";//s2v_n[i].rest_cpu
			}
		} 
		data += ";\r\n";	
		myDowith.SaveFile("glpsolRSA.dat", data, true);
		
		if(Parameters.CurrentVONEMethod == Parameters.MapVONE01ILPLin_EquilibriumCXH){
			data = "param costCPU:=\r\n";
			for (int i = 0; i < sub.nodes; i++) {
				for(int j = 0;j < reqs[index].nodes;j++)
					if(s2v_n[i].rest_cpu >= reqs[index].cpu[j])
						data += i + " " + j + " " + 1.0/(s2v_n[i].rest_cpu-reqs[index].cpu[j]) +"\r\n";//s2v_n[i].rest_cpu
			}
			data += ";\r\n";	
			myDowith.SaveFile("glpsolRSA.dat", data, true);
		}
		
			
		//param c{k in Ns};/*����ڵ�ļ��㵥λ����*/
		
		if(Parameters.CurrentVONEMethod == Parameters.MapVONECXHNode){
			data = "param costOfNode:=\r\n";
			for (int i = 0; i < sub.nodes; i++) {
				data += i + " " + subStatic.cpu[i] +"\r\n";//s2v_n[i].rest_cpu
				//data += i + " " + s2v_n[i].rest_cpu +"\r\n";//s2v_n[i].rest_cpu
			}
			data += ";\r\n";	
			myDowith.SaveFile("glpsolRSA.dat", data, true);
		} 
		
		
		
		
		data = "";
		if(Parameters.CurrentVONEMethod == Parameters.MapVONE01ILPPRLinCXH){
			data = "param ac:=\r\n";
			//����pagerankֵ
			double vNodePageRank[] = new double[reqs[index].nodes];
			double sNodePageRank[] = new double[sub.nodes];
			//	InitVNodePageRank(reqs,index);
			
			vNodePageRank=InitVNodePageRank(vNodePageRank,reqs,index);
			sNodePageRank= InitSNodePageRank(sNodePageRank, sub);
			if(Parameters.RecordLogModel){
				String str = "Check vNodePR[]=";
				for(int i=0;i<reqs[index].nodes;i++){
					str += ""+i+" "+vNodePageRank[i]+" ";
				}
				WriteFilePlus("process.txt",str);
				
				str = "Check sNodePR[]=";
				for(int i=0;i<sub.nodes;i++){
					str += ""+i+" "+sNodePageRank[i]+" ";
				}
				WriteFilePlus("process.txt",str);
			}
			for(int i=0;i<reqs[index].nodes;i++){
				for (int j = 0; j < sub.nodes; j++) {
					if(reqs[index].cpu[i]<=s2v_n[j].rest_cpu){
						if(vNodePageRank[i] <= sNodePageRank[j])
							data += i + " " + j + " " + (sNodePageRank[j]-vNodePageRank[i]) +"\r\n";
						else data += i + " " + j + " " + (vNodePageRank[i]-sNodePageRank[j]) +"\r\n";
					}
				}
			}
			data += ";\r\n";	
			myDowith.SaveFile("glpsolRSA.dat", data, true);
		}
		
		
		//param cs{(i,j) in Ls};/*������·��Ƶ�ײ۵�λ���ۣ��볤�ȳɱ���*/
		data = "param cs:=\r\n";
		for (int i = 0; i < sub.links; i++) {
			if(sub.link[i].from > sub.link[i].to) continue;//chenxhɾ��20200903
			data += sub.link[i].from + " " + sub.link[i].to + " " + sub.link[i].length+"\r\n";
			//data += sub.link[i].to + " " + sub.link[i].from + " " + sub.link[i].length+"\r\n";//20200903����
		}
		data += ";\r\n";	
		myDowith.SaveFile("glpsolRSA.dat", data, true);
		
		//param p{i in Nv};/*����ڵ�CPU*/
		data = "param p:=\r\n";
		for (int i = 0; i < reqs[index].nodes; i++) {
			data += i + " " + reqs[index].cpu[i]+"\r\n";
		}
		data += ";\r\n";	
		myDowith.SaveFile("glpsolRSA.dat", data, true);
		
		//param pbw{(i,j) in Lv};/*������·��������*/
		data = "param pbw:=\r\n";
		for (int i = 0; i < reqs[index].links; i++) {
			if(reqs[index].link[i].from > reqs[index].link[i].to) continue;
			data += reqs[index].link[i].from + " " + reqs[index].link[i].to + " " + reqs[index].link[i].bw+"\r\n";
		}
		data += ";\r\n";	
		myDowith.SaveFile("glpsolRSA.dat", data, true);
		
		//param F;/*����Ƶ�ײ�����*/
		data = "param F:="+(sub.slotsNum-1)+";\r\n";
		myDowith.SaveFile("glpsolRSA.dat", data, true);
		
		//param U{k in Ns};/*����ڵ�k��ʣ��cpu*/
		data = "param U:=\r\n";
		for (int i = 0; i < sub.nodes; i++) {
			data += i + " " + s2v_n[i].rest_cpu +"\r\n";//s2v_n[i].rest_cpu
		}
		data += ";\r\n";	
		myDowith.SaveFile("glpsolRSA.dat", data, true);
		
		//param USlot{(i,j) in Ls,t in SlotIndex};/*������·Ƶ�ײ۵�״̬��1��ʾ���У�0��ʾ��ռ��*/
		data = "param USlot:=\r\n";
		for (int i = 0; i < sub.links; i++) {
			if(sub.link[i].from > sub.link[i].to) continue;//chenxhɾ��20200903
			for(int j=0;j<sub.slotsNum;j++){
				data += sub.link[i].from + " " + sub.link[i].to + " " +j + " "+ sub.slots[i][j]+"\r\n";
				//data += sub.link[i].to + " " + sub.link[i].from + " " +j + " "+ sub.slots[i][j]+"\r\n";//20200903����
			}
		}
		data += ";\r\n";	
		myDowith.SaveFile("glpsolRSA.dat", data, true);
		
		//param d{(i,j) in Ls};/*������·�ĳ���*/
		data = "param d:=\r\n";
		for (int i = 0; i < sub.links; i++) {
			if(sub.link[i].from > sub.link[i].to) continue;//chenxhɾ��20200903
			data += sub.link[i].from + " " + sub.link[i].to + " " + sub.link[i].length+"\r\n";
			//data += sub.link[i].to + " " + sub.link[i].from + " " + sub.link[i].length+"\r\n";//����20200903
		}
		data += ";\r\n";	
		myDowith.SaveFile("glpsolRSA.dat", data, true);
		
		//param dis{m in MD};/*����ģʽ��������*/
		data = "param dis:=\r\n";
		data += "1 3000\r\n";
		data += "2 1500\r\n";
		data += "3 750\r\n";
		data += "4 375\r\n";
		data += "6 94\r\n";
		data += "8 24\r\n";
		data += ";\r\n";	
		myDowith.SaveFile("glpsolRSA.dat", data, true);
		
		//param R;/*��������*/
		data = "param R:=100000000;\r\n";
		myDowith.SaveFile("glpsolRSA.dat", data, true);
		
		//param G;/*���������*/
		data = "param G:="+sub.diffSlot+";\r\n";
		myDowith.SaveFile("glpsolRSA.dat", data, true);


		data = "";

		if (Parameters.CurrentVONEMethod==Parameters.MapVONEILPAM) {
			data = "param sv:=\r\n";
			double sNodeAM[] = new double[sub.nodes];
			sNodeAM = InitSNodeAM(sNodeAM, sub);
			for (int i = 0; i < reqs[index].nodes; i++) {
				for (int j = 0; j < sub.nodes; j++) {
						data += i + " " + j + " " + (sNodeAM[j]/ reqs[index].cpu[i] ) + "\r\n";
				}
			}
			data += ";\r\n";
			myDowith.SaveFile("glpsolRSA.dat", data, true);
		}

		if(Parameters.CurrentVONEMethod == Parameters.MapVONENCRbyILP){
			data = "param sv:=\r\n";
			//����pagerankֵ
			double vNodePageRank[] = new double[reqs[index].nodes];
			double sNodePageRank[] = new double[sub.nodes];
			//	InitVNodePageRank(reqs,index);

			vNodePageRank=InitVNodePageRankOfGHG(vNodePageRank,reqs,index);
			sNodePageRank= InitSNodePageRankOfGHG(sNodePageRank, sub);

			for(int i=0;i<reqs[index].nodes;i++){
				for (int j = 0; j < sub.nodes; j++) {
					if(reqs[index].cpu[i]<=s2v_n[j].rest_cpu){
						if(vNodePageRank[i] <= sNodePageRank[j])
							data += i + " " + j + " " + (sNodePageRank[j]-vNodePageRank[i]) +"\r\n";
						else data += i + " " + j + " " + (vNodePageRank[i]-sNodePageRank[j]) +"\r\n";
					}
				}
			}
			data += ";\r\n";
			myDowith.SaveFile("glpsolRSA.dat", data, true);
		}
		
		System.out.println("Done");
		
		try {
			String s;
			Process process = null;

			if(Parameters.CurrentVONEMethod == Parameters.MapVONECXHNode){
				//process = Runtime.getRuntime().exec("cmd /c C:/������/VONE/VONE/glpk-4.60/w64/glpsol.exe -m C:/������/VONE/VONE/glpk-4.60/w64/glpsolMILPPRByLinChen.mod -d glpsolRSA.dat -o glpsolRSA.o");
				process = Runtime.getRuntime().exec("cmd /c D:/360Downloads/ylc/��ز���/��ز���/VNE_GHG_4/glpk-4.60/w64/glpsol.exe -m  D:/360Downloads/ylc/��ز���/��ز���/VNE_GHG_4/glpk-4.60/w64/glpsolMILPByLinChenNodeUndirectDiagram.mod -d glpsolRSA.dat -o glpsolRSA.o");

			} else if(Parameters.CurrentVONEMethod == Parameters.MapVONE01ILPPRLinCXH){
				//process = Runtime.getRuntime().exec("cmd /c C:/������/VONE/VONE/glpk-4.60/w64/glpsol.exe -m C:/������/VONE/VONE/glpk-4.60/w64/glpsolMILPPRByLinChen.mod -d glpsolRSA.dat -o glpsolRSA.o");
				process = Runtime.getRuntime().exec("cmd /c  D:/360Downloads/ylc/��ز���/��ز���/VNE_GHG_4/glpk-4.60/w64/glpsol.exe -m  D:/360Downloads/ylc/��ز���/��ز���/VNE_GHG_4/glpk-4.60/w64/glpsolMILPPRByLinChenUndirectDiagram.mod -d glpsolRSA.dat -o glpsolRSA.o");
			} else if(Parameters.CurrentVONEMethod == Parameters.MapVONE01ILPLin_EquilibriumCXH){
				//process = Runtime.getRuntime().exec("cmd /c E:/ʵ�����/��ز���/VNE_GHG_4/glpk-4.60/w64/glpsol.exe -m E:/ʵ�����/��ز���/VNE_GHG_4/glpk-4.60/w64/glpsolMILPPRByLinChen.mod -d glpsolRSA.dat -o glpsolRSA.o");
				process = Runtime.getRuntime().exec("cmd /c  D:/360Downloads/ylc/��ز���/��ز���/VNE_GHG_4/glpk-4.60/w64/glpsol.exe -m   D:/360Downloads/ylc/��ز���/��ز���/VNE_GHG_4/glpk-4.60/w64/glpsolMILPByLinUndirectDiagram-Equilibrium.mod -d glpsolRSA.dat -o glpsolRSA.o");
				//process = Runtime.getRuntime().exec("cmd /c C:/������/VONE/VNE/glpk-4.60/w64/glpsol.exe -m C:/������/VONE/VNE/glpk-4.60/w64/glpsolMILPByLinUndirectDiagram-glpsolMILPByLinUndirectDiagram-Equilibrium.mod -d glpsolRSA.dat -o glpsolRSA.o");

			}else if(Parameters.CurrentVONEMethod == Parameters.MapVONENCRbyILP) {
				process = Runtime.getRuntime().exec("cmd /c D:/360Downloads/ylc/��ز���/��ز���/VNE_GHG_4/glpk-4.60/w64/glpsol.exe -m  D:/360Downloads/ylc/��ز���/��ز���/VNE_GHG_4/glpk-4.60/w64/glpsolMILPNCR.mod -d glpsolRSA.dat -o glpsolRSA.o");
			}
			else if(Parameters.CurrentVONEMethod == Parameters.MapVONEILPAM) {
				process = Runtime.getRuntime().exec("cmd /c D:/360Downloads/ylc/��ز���/��ز���/VNE_GHG_4/glpk-4.60/w64/glpsol.exe -m D:/360Downloads/ylc/��ز���/��ز���/VNE_GHG_4/glpk-4.60/w64/glpsolMILPAM.mod -d glpsolRSA.dat -o glpsolRSA.o");
			}
			else {
				process = Runtime.getRuntime().exec("cmd /c D:/360Downloads/ylc/��ز���/��ز���/VNE_GHG_4/glpk-4.60/w64/glpsol.exe -m D:/360Downloads/ylc/��ز���/��ز���/VNE_GHG_4/glpk-4.60/w64/glpsolMILPByLin.mod  -d glpsolRSA.dat -o glpsolRSA.o");
				//process = Runtime.getRuntime().exec("cmd /c C:/������/VONE/VONE/glpk-4.60/w64/glpsol.exe -m C:/������/VONE/VONE/glpk-4.60/w64/glpsolMILPByLin.mod  -d glpsolRSA.dat -o glpsolRSA.o");
				//process = Runtime.getRuntime().exec("cmd /c C:/������//process = Runtime.getRuntime().exec("cmd /c C:/������/VNE/VNE/VNE/glpk-4.60/w64/glpsol.exe -m C:/������/VNE/VNE/VNE/glpk-4.60/w64/glpsolMILPByLinUndirectDiagram.mod -d glpsolRSA.dat -o glpsolRSA.o");
				//process = Runtime.getRuntime().exec("cmd /c C:/������/VONE/VNE/glpk-4.60/w64/glpsol.exe -m C:/������/VONE/VNE/glpk-4.60/w64/glpsolMILPByLinUndirectDiagram.mod -d glpsolRSA.dat -o glpsolRSA.o");
			}
			//if(embedModelOrAlgo == Parameters.MapVONE3ByWangY){
			//	process = Runtime.getRuntime().exec("cmd /c C:/������/VONE/VONE/glpk-4.60/w64/glpsol.exe -m C:/������/VONE/VONE/glpk-4.60/w64/glpsolMILVONE3NodesNoDataWangY.mod -d glpsolRSA.dat -o glpsolRSA.o");
			//} else if(embedModelOrAlgo == Parameters.MapVONE3PByWangYAndChenxh){
			//	process = Runtime.getRuntime().exec("cmd /c C:/������/VONE/VONE/glpk-4.60/w64/glpsol.exe -m C:/������/VONE/VONE/glpk-4.60/w64/glpsolMILPVONE3NodesNoDataWangY.mod -d glpsolRSA.dat -o glpsolRSA.o");
			//}
			//Process process = Runtime.getRuntime().exec("cmd /c C:/������/VONE/VONE/glpk-4.60/w64/glpsol.exe -m C:/������/VONE/VONE/glpk-4.60/w64/glpsol01ILPVONE3PNodesWangY.mod -d glpsolRSA.dat -o glpsolRSA.o");
			//Process process = Runtime.getRuntime().exec("cmd /c C:/������/VONE/VONE/glpk-4.60/w64/glpsol.exe -m C:/������/VONE/VONE/glpk-4.60/w64/glpsolMILPVONE3NodesNoDataWangY.mod -d glpsolRSA.dat -o glpsolRSA.o");
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
	/*
	 * Lin FB
	 */
	private void CreateVONE01ILPByLinFB(EOSubstrateNetwork sub,VONRequest reqs[],int index,int indexS,int indexE,int subMark[])
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
		
		data = "set Lv:=\r\n";
		for(int j = 0; j < reqs[index].links; j ++) {
			if(reqs[index].link[j].from < reqs[index].link[j].to)
				data += reqs[index].link[j].from + " " + reqs[index].link[j].to + "\r\n";
		}
		data += ";\r\n"; 		 
		myDowith.SaveFile("glpsolRSA.dat", data, true);
		
		data = "set Ns:=";
		for(int j = 0; j < sub.nodes; j ++) {
			if(subMark[j] == 1)
				data += j + " ";
		}
		data += ";\r\n"; 		 
		myDowith.SaveFile("glpsolRSA.dat", data, true);
		
		data = "";
		for(int j = 0; j < sub.nodes; j ++) {
			if(subMark[j] == 1){
				data += "set Nss[" + j + "]:=";
				for(int i=0;i<sub.links;i++){
					if(sub.link[i].from == j){
						if(sub.link[i].from < sub.link[i].to) //chenxhɾ��20200903���޸�����ͼΪ����ͼ
							if(subMark[sub.link[i].to] == 1)
								data += sub.link[i].to + " ";
					}
					if(sub.link[i].to == j){
						//if(sub.link[i].from < sub.link[i].to) //chenxhɾ��20200903���޸�����ͼΪ����ͼ
						//	data += sub.link[i].from + " ";
					}
				}
				data += ";\r\n"; 
			}
				
			
		}		 
		myDowith.SaveFile("glpsolRSA.dat", data, true);
		
		data = "";
		for(int j = 0; j < sub.nodes; j++) {
			if(subMark[j] == 1){
				data = "set Nss1[" + j + "]:=";
				for(int i=0;i<sub.links;i++){
					if(sub.link[i].to == j){
						if(sub.link[i].from < sub.link[i].to) //chenxhɾ��20200903���޸�����ͼΪ����ͼ
							if(subMark[sub.link[i].from] == 1)
								data += sub.link[i].from + " ";
					}
					if(sub.link[i].from == j){
						//if(sub.link[i].from < sub.link[i].to) //chenxhɾ��20200903���޸�����ͼΪ����ͼ
						//	data += sub.link[i].to + " ";
					}
				}
				data += ";\r\n"; 
				myDowith.SaveFile("glpsolRSA.dat", data, true);
			}
			
		}		 
		
		
		data = "set Ls:=\r\n";
		for(int j = 0; j < sub.links; j ++) {
			if(sub.link[j].from < sub.link[j].to)//chenxhɾ��20200903���޸�����ͼΪ����ͼ
				if(subMark[sub.link[j].from] == 1 && subMark[sub.link[j].to] == 1)
					data += sub.link[j].from + " " + sub.link[j].to + "\r\n";
				//data += sub.link[j].to + " " + sub.link[j].from + "\r\n";//add chenxh20200903
		}
		data += ";\r\n"; 		 
		myDowith.SaveFile("glpsolRSA.dat", data, true);
		
		data = "";
		for(int j = 0; j < reqs[index].nodes; j ++) {
			data += "set X["+j+"]:=";
			for(int i=0;i<sub.nodes;i++){
				if(subMark[i] == 0) continue;
				if(s2v_n[i].rest_cpu >= reqs[index].cpu[j]){//
					data += i + " ";
				}
			}
			data += ";\r\n"; 
		}	 
		myDowith.SaveFile("glpsolRSA.dat", data, true);
		
		data = "set SlotIndex:=";
		for(int j = indexS; j <= indexE; j ++) {
		//for(int j = 0; j < sub.slotsNum; j ++) {
			data += j + " ";
		}	
		data += ";\r\n"; 
		myDowith.SaveFile("glpsolRSA.dat", data, true);
		
		data = "set MD:=1,2,3,4,6,8;\r\n";
		myDowith.SaveFile("glpsolRSA.dat", data, true);
		
		//Slot{(i,j) in Lv,a in SlotIndex,model in MD};
		data = "";
		for(int j = 0; j < reqs[index].links; j++) {
			if(reqs[index].link[j].from > reqs[index].link[j].to) continue;//chenxhɾ��20200903����ͼת����ͼ
			//for(int i=0;i<sub.slotsNum;i++){
			for(int i=indexS;i<=indexE;i++){
				for(int k=1;k<9;){
					data = "set Slot["+reqs[index].link[j].from+","+reqs[index].link[j].to+","+i+","+k+"]:=";
					for(int p=i;p>=indexS&&p<=indexE&&p<sub.slotsNum&&(p<i+Math.ceil(reqs[index].link[j].bw/(12.5*k))+sub.diffSlot);p++){
						//if(i+Math.ceil(reqs[index].link[j].bw/(12.5*k))+sub.diffSlot > sub.slotsNum) {
						//	break;
						//}
						data += p + " ";
						//break;
					}
					data += ";\r\n"; 
					myDowith.SaveFile("glpsolRSA.dat", data, true);
					if(k>=4) k+=2;
					else k++;
				}
			}
		}	
		
		
		//set Slot1{(i,j) in Lv,a in SlotIndex,model in MD};/*��a+|bw/model|+G-1��a+|bw/model|+G-1+1,...,a*/
		data = "";
		for(int j = 0; j < reqs[index].links; j ++) {
			if(reqs[index].link[j].from > reqs[index].link[j].to) continue;//chenxhɾ��20200903
			for(int i=indexS;i<=indexE;i++){
			//for(int i=0;i<sub.slotsNum;i++){	
				for(int k=1;k<9;){
					data = "set Slot1["+reqs[index].link[j].from+","+reqs[index].link[j].to+","+i+","+k+"]:=";
					for(int p=i;p>=indexS&&p<=indexE&&p>=0&&(p>=i-Math.ceil(reqs[index].link[j].bw/(12.5*k))-sub.diffSlot+1);p--){
						//if(i-Math.ceil(reqs[index].link[j].bw/(12.5*k))-sub.diffSlot+1<0) {
						//	break;
						//}
						data += p + " ";
					}
					data += ";\r\n"; 
					if(k>=4) k+=2;
					else k++;
					myDowith.SaveFile("glpsolRSA.dat", data, true);
				}
			}
		}	
		
		
		//param c{k in Ns};/*����ڵ�ļ��㵥λ����*/
		data = "param c:=\r\n";
		if(Parameters.CurrentVONEMethod == Parameters.MapVONE01ILPLinCXH || Parameters.CurrentVONEMethod == Parameters.MapVONE01ILPPRLinCXH || Parameters.CurrentVONEMethod == Parameters.MapVONECXHNode){
			for (int i = 0; i < sub.nodes; i++) {
				if(subMark[i] == 1)
					data += i + " " + s2v_n[i].rest_cpu +"\r\n";//s2v_n[i].rest_cpu
			}
		} else if(Parameters.CurrentVONEMethod == Parameters.MapVONE01ILPLin || Parameters.CurrentVONEMethod == Parameters.MapVONE01ILPLinFB){
			for (int i = 0; i < sub.nodes; i++) {
				if(subMark[i] == 1)
					data += i + " " + s2v_n[i].rest_cpu +"\r\n";//s2v_n[i].rest_cpu
			}
		} 
		data += ";\r\n";	
		myDowith.SaveFile("glpsolRSA.dat", data, true);
		
		if(Parameters.CurrentVONEMethod == Parameters.MapVONE01ILPLin_EquilibriumCXH){
			data = "param costCPU:=\r\n";
			for (int i = 0; i < sub.nodes; i++) {
				if(subMark[i] == 0) continue;
				for(int j = 0;j < reqs[index].nodes;j++)
					if(s2v_n[i].rest_cpu >= reqs[index].cpu[j])
						data += i + " " + j + " " + 1.0/(s2v_n[i].rest_cpu-reqs[index].cpu[j]) +"\r\n";//s2v_n[i].rest_cpu
			}
			data += ";\r\n";	
			myDowith.SaveFile("glpsolRSA.dat", data, true);
		}
		
			
		//param c{k in Ns};/*����ڵ�ļ��㵥λ����*/
		
		if(Parameters.CurrentVONEMethod == Parameters.MapVONECXHNode){
			data = "param costOfNode:=\r\n";
			for (int i = 0; i < sub.nodes; i++) {
				if(subMark[i] == 0) continue;
				data += i + " " + subStatic.cpu[i] +"\r\n";//s2v_n[i].rest_cpu
				//data += i + " " + s2v_n[i].rest_cpu +"\r\n";//s2v_n[i].rest_cpu
			}
			data += ";\r\n";	
			myDowith.SaveFile("glpsolRSA.dat", data, true);
		} 
		
		
		
		
		data = "";
		if(Parameters.CurrentVONEMethod == Parameters.MapVONE01ILPPRLinCXH){
			data = "param ac:=\r\n";
			//����pagerankֵ
			double vNodePageRank[] = new double[reqs[index].nodes];
			double sNodePageRank[] = new double[sub.nodes];
			//	InitVNodePageRank(reqs,index);
			
			vNodePageRank=InitVNodePageRank(vNodePageRank,reqs,index);
			sNodePageRank= InitSNodePageRank(sNodePageRank, sub);
			if(Parameters.RecordLogModel){
				String str = "Check vNodePR[]=";
				for(int i=0;i<reqs[index].nodes;i++){
					str += ""+i+" "+vNodePageRank[i]+" ";
				}
				WriteFilePlus("process.txt",str);
				
				str = "Check sNodePR[]=";
				for(int i=0;i<sub.nodes;i++){
					if(subMark[i] == 0) continue;
					str += ""+i+" "+sNodePageRank[i]+" ";
				}
				WriteFilePlus("process.txt",str);
			}
			for(int i=0;i<reqs[index].nodes;i++){
				for (int j = 0; j < sub.nodes; j++) {
					if(subMark[j] == 0) continue;
					if(reqs[index].cpu[i]<=s2v_n[j].rest_cpu){
						if(vNodePageRank[i] <= sNodePageRank[j])
							data += i + " " + j + " " + (sNodePageRank[j]-vNodePageRank[i]) +"\r\n";
						else data += i + " " + j + " " + (vNodePageRank[i]-sNodePageRank[j]) +"\r\n";
					}
				}
			}
			data += ";\r\n";	
			myDowith.SaveFile("glpsolRSA.dat", data, true);
		}
		
		
		//param cs{(i,j) in Ls};/*������·��Ƶ�ײ۵�λ���ۣ��볤�ȳɱ���*/
		data = "param cs:=\r\n";
		for (int i = 0; i < sub.links; i++) {
			if(sub.link[i].from > sub.link[i].to) continue;//chenxhɾ��20200903
			if(subMark[sub.link[i].from] == 0 || subMark[sub.link[i].to] == 0) continue;
			data += sub.link[i].from + " " + sub.link[i].to + " " + sub.link[i].length+"\r\n";
			//data += sub.link[i].to + " " + sub.link[i].from + " " + sub.link[i].length+"\r\n";//20200903����
		}
		data += ";\r\n";	
		myDowith.SaveFile("glpsolRSA.dat", data, true);
		
		//param p{i in Nv};/*����ڵ�CPU*/
		data = "param p:=\r\n";
		for (int i = 0; i < reqs[index].nodes; i++) {
			data += i + " " + reqs[index].cpu[i]+"\r\n";
		}
		data += ";\r\n";	
		myDowith.SaveFile("glpsolRSA.dat", data, true);
		
		//param pbw{(i,j) in Lv};/*������·��������*/
		data = "param pbw:=\r\n";
		for (int i = 0; i < reqs[index].links; i++) {
			if(reqs[index].link[i].from > reqs[index].link[i].to) continue;
			data += reqs[index].link[i].from + " " + reqs[index].link[i].to + " " + reqs[index].link[i].bw+"\r\n";
		}
		data += ";\r\n";	
		myDowith.SaveFile("glpsolRSA.dat", data, true);
		
		//param F;/*����Ƶ�ײ�����*/
		data = "param F:="+(sub.slotsNum-1)+";\r\n";
		myDowith.SaveFile("glpsolRSA.dat", data, true);
		
		//param U{k in Ns};/*����ڵ�k��ʣ��cpu*/
		data = "param U:=\r\n";
		for (int i = 0; i < sub.nodes; i++) {
			if(subMark[i] == 0) continue;
			data += i + " " + s2v_n[i].rest_cpu +"\r\n";//s2v_n[i].rest_cpu
		}
		data += ";\r\n";	
		myDowith.SaveFile("glpsolRSA.dat", data, true);
		
		//param USlot{(i,j) in Ls,t in SlotIndex};/*������·Ƶ�ײ۵�״̬��1��ʾ���У�0��ʾ��ռ��*/
		data = "param USlot:=\r\n";
		for (int i = 0; i < sub.links; i++) {
			if(sub.link[i].from > sub.link[i].to) continue;//chenxhɾ��20200903
			for(int j=indexS;j<=indexE;j++){
			//for(int j=0;j<sub.slotsNum;j++){
				if(subMark[sub.link[i].from] == 0 || subMark[sub.link[i].to] == 0) continue;
				data += sub.link[i].from + " " + sub.link[i].to + " " +j + " "+ sub.slots[i][j]+"\r\n";
				//data += sub.link[i].to + " " + sub.link[i].from + " " +j + " "+ sub.slots[i][j]+"\r\n";//20200903����
			}
		}
		data += ";\r\n";	
		myDowith.SaveFile("glpsolRSA.dat", data, true);
		
		//param d{(i,j) in Ls};/*������·�ĳ���*/
		data = "param d:=\r\n";
		for (int i = 0; i < sub.links; i++) {
			if(sub.link[i].from > sub.link[i].to) continue;//chenxhɾ��20200903
			if(subMark[sub.link[i].from] == 0 || subMark[sub.link[i].to] == 0) continue;
			data += sub.link[i].from + " " + sub.link[i].to + " " + sub.link[i].length+"\r\n";
			//data += sub.link[i].to + " " + sub.link[i].from + " " + sub.link[i].length+"\r\n";//����20200903
		}
		data += ";\r\n";	
		myDowith.SaveFile("glpsolRSA.dat", data, true);
		
		//param dis{m in MD};/*����ģʽ��������*/
		data = "param dis:=\r\n";
		data += "1 3000\r\n";
		data += "2 1500\r\n";
		data += "3 750\r\n";
		data += "4 375\r\n";
		data += "6 94\r\n";
		data += "8 24\r\n";
		data += ";\r\n";	
		myDowith.SaveFile("glpsolRSA.dat", data, true);
		
		//param R;/*��������*/
		data = "param R:=100000000;\r\n";
		myDowith.SaveFile("glpsolRSA.dat", data, true);
		
		//param G;/*���������*/
		data = "param G:="+sub.diffSlot+";\r\n";
		myDowith.SaveFile("glpsolRSA.dat", data, true);
		
		System.out.println("Done");
		
		try {
			String s;
			Process process = null;
			if(Parameters.CurrentVONEMethod == Parameters.MapVONE01ILPLinStrong){
				//process = Runtime.getRuntime().exec("cmd /c C:/������/VONE/VONE/glpk-4.60/w64/glpsol.exe -m C:/������/VONE/VONE/glpk-4.60/w64/glpsolMILPPRByLinChen.mod -d glpsolRSA.dat -o glpsolRSA.o");
				process = Runtime.getRuntime().exec("cmd /c C:/������/VONE/VONE/glpk-4.60/w64/glpsol.exe -m C:/������/VONE/VONE/glpk-4.60/w64/glpsolMILPByLinStrong.mod -d glpsolRSA.dat -o glpsolRSA.o");
				//C:\������\VONE\VONE\glpk-4.60\w64
			} else if(Parameters.CurrentVONEMethod == Parameters.MapVONE01ILPLinFB){
				//process = Runtime.getRuntime().exec("cmd /c C:/������/VONE/VONE/glpk-4.60/w64/glpsol.exe -m C:/������/VONE/VONE/glpk-4.60/w64/glpsolMILPPRByLinChen.mod -d glpsolRSA.dat -o glpsolRSA.o");
				process = Runtime.getRuntime().exec("cmd /c C:/������/VONE/VONE/glpk-4.60/w64/glpsol.exe -m C:/������/VONE/VONE/glpk-4.60/w64/glpsolMILPByLinFB.mod -d glpsolRSA.dat -o glpsolRSA.o");
				//C:\������\VONE\VONE\glpk-4.60\w64
			} else if(Parameters.CurrentVONEMethod == Parameters.MapVONECXHNode){
				//process = Runtime.getRuntime().exec("cmd /c C:/������/VONE/VONE/glpk-4.60/w64/glpsol.exe -m C:/������/VONE/VONE/glpk-4.60/w64/glpsolMILPPRByLinChen.mod -d glpsolRSA.dat -o glpsolRSA.o");
				process = Runtime.getRuntime().exec("cmd /c C:/������/VONE/VNE/glpk-4.60/w64/glpsol.exe -m C:/������/VONE/VNE/glpk-4.60/w64/glpsolMILPByLinChenNodeUndirectDiagram.mod -d glpsolRSA.dat -o glpsolRSA.o");
				
			} else if(Parameters.CurrentVONEMethod == Parameters.MapVONE01ILPPRLinCXH){
				//process = Runtime.getRuntime().exec("cmd /c C:/������/VONE/VONE/glpk-4.60/w64/glpsol.exe -m C:/������/VONE/VONE/glpk-4.60/w64/glpsolMILPPRByLinChen.mod -d glpsolRSA.dat -o glpsolRSA.o");
				process = Runtime.getRuntime().exec("cmd /c C:/������/VONE/VNE/glpk-4.60/w64/glpsol.exe -m C:/������/VONE/VNE/glpk-4.60/w64/glpsolMILPPRByLinChenUndirectDiagram.mod -d glpsolRSA.dat -o glpsolRSA.o");
			} else if(Parameters.CurrentVONEMethod == Parameters.MapVONE01ILPLin_EquilibriumCXH){
				//process = Runtime.getRuntime().exec("cmd /c C:/������/VONE/VONE/glpk-4.60/w64/glpsol.exe -m C:/������/VONE/VONE/glpk-4.60/w64/glpsolMILPPRByLinChen.mod -d glpsolRSA.dat -o glpsolRSA.o");
				process = Runtime.getRuntime().exec("cmd /c C:/������/VNE/VNE/VNE/glpk-4.60/w64/glpsol.exe -m C:/������/VNE/VNE/VNE/glpk-4.60/w64/glpsolMILPByLinUndirectDiagram-Equilibrium.mod -d glpsolRSA.dat -o glpsolRSA.o");
				//process = Runtime.getRuntime().exec("cmd /c C:/������/VONE/VNE/glpk-4.60/w64/glpsol.exe -m C:/������/VONE/VNE/glpk-4.60/w64/glpsolMILPByLinUndirectDiagram-glpsolMILPByLinUndirectDiagram-Equilibrium.mod -d glpsolRSA.dat -o glpsolRSA.o");
				
			} else {
				process = Runtime.getRuntime().exec("cmd /c E:/���Թ�����/CEEA/VNE/glpk-4.60/w64/glpsol.exe -m E:/���Թ�����/CEEA/VNE/glpk-4.60/w64/glpsolMILPByLin.mod  -d glpsolRSA.dat -o glpsolRSA.o");
				//process = Runtime.getRuntime().exec("cmd /c C:/������/VONE/VONE/glpk-4.60/w64/glpsol.exe -m C:/������/VONE/VONE/glpk-4.60/w64/glpsolMILPByLin.mod  -d glpsolRSA.dat -o glpsolRSA.o");
				//process = Runtime.getRuntime().exec("cmd /c C:/������/VNE/VNE/VNE/glpk-4.60/w64/glpsol.exe -m C:/������/VNE/VNE/VNE/glpk-4.60/w64/glpsolMILPByLinUndirectDiagram.mod -d glpsolRSA.dat -o glpsolRSA.o");
				//process = Runtime.getRuntime().exec("cmd /c C:/������/VONE/VNE/glpk-4.60/w64/glpsol.exe -m C:/������/VONE/VNE/glpk-4.60/w64/glpsolMILPByLinUndirectDiagram.mod -d glpsolRSA.dat -o glpsolRSA.o");
			}
			//if(embedModelOrAlgo == Parameters.MapVONE3ByWangY){
			//	process = Runtime.getRuntime().exec("cmd /c C:/������/VONE/VONE/glpk-4.60/w64/glpsol.exe -m C:/������/VONE/VONE/glpk-4.60/w64/glpsolMILVONE3NodesNoDataWangY.mod -d glpsolRSA.dat -o glpsolRSA.o");
			//} else if(embedModelOrAlgo == Parameters.MapVONE3PByWangYAndChenxh){
			//	process = Runtime.getRuntime().exec("cmd /c C:/������/VONE/VONE/glpk-4.60/w64/glpsol.exe -m C:/������/VONE/VONE/glpk-4.60/w64/glpsolMILPVONE3NodesNoDataWangY.mod -d glpsolRSA.dat -o glpsolRSA.o");
			//}
			//Process process = Runtime.getRuntime().exec("cmd /c C:/������/VONE/VONE/glpk-4.60/w64/glpsol.exe -m C:/������/VONE/VONE/glpk-4.60/w64/glpsol01ILPVONE3PNodesWangY.mod -d glpsolRSA.dat -o glpsolRSA.o");
			//Process process = Runtime.getRuntime().exec("cmd /c C:/������/VONE/VONE/glpk-4.60/w64/glpsol.exe -m C:/������/VONE/VONE/glpk-4.60/w64/glpsolMILPVONE3NodesNoDataWangY.mod -d glpsolRSA.dat -o glpsolRSA.o");
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
	/*
	 * Lin FB
	 */
	private void CreateVONE01ILPByLinStrong(EOSubstrateNetwork sub,VONRequest reqs[],int index)
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
		
		data = "set Lv:=\r\n";
		for(int j = 0; j < reqs[index].links; j ++) {
			if(reqs[index].link[j].from < reqs[index].link[j].to)
				data += reqs[index].link[j].from + " " + reqs[index].link[j].to + "\r\n";
		}
		data += ";\r\n"; 		 
		myDowith.SaveFile("glpsolRSA.dat", data, true);
		
		data = "set Ns:=";
		for(int j = 0; j < sub.nodes; j ++) {
			data += j + " ";
		}
		data += ";\r\n"; 		 
		myDowith.SaveFile("glpsolRSA.dat", data, true);
		
		data = "";
		for(int j = 0; j < sub.nodes; j ++) {
			data += "set Nss[" + j + "]:=";
			for(int i=0;i<sub.links;i++){
				if(sub.link[i].from == j){
					if(sub.link[i].from < sub.link[i].to) //chenxhɾ��20200903���޸�����ͼΪ����ͼ
						data += sub.link[i].to + " ";
				}
				if(sub.link[i].to == j){
					if(sub.link[i].from < sub.link[i].to) //chenxhɾ��20200903���޸�����ͼΪ����ͼ
						data += sub.link[i].from + " ";
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
					if(sub.link[i].from < sub.link[i].to) //chenxhɾ��20200903���޸�����ͼΪ����ͼ
						data += sub.link[i].from + " ";
				}
				if(sub.link[i].from == j){
					if(sub.link[i].from < sub.link[i].to) //chenxhɾ��20200903���޸�����ͼΪ����ͼ
						data += sub.link[i].to + " ";
				}
			}
			data += ";\r\n"; 
			myDowith.SaveFile("glpsolRSA.dat", data, true);
		}		 
		
		
		data = "set Ls:=\r\n";
		for(int j = 0; j < sub.links; j ++) {
			if(sub.link[j].from < sub.link[j].to)//chenxhɾ��20200903���޸�����ͼΪ����ͼ
				data += sub.link[j].from + " " + sub.link[j].to + "\r\n";
				data += sub.link[j].to + " " + sub.link[j].from + "\r\n";//add chenxh20200903
		}
		data += ";\r\n"; 		 
		myDowith.SaveFile("glpsolRSA.dat", data, true);
		
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
			if(reqs[index].link[j].from > reqs[index].link[j].to) continue;//chenxhɾ��20200903����ͼת����ͼ
			for(int i=0;i<sub.slotsNum;i++){
				for(int k=1;k<9;){
					data = "set Slot["+reqs[index].link[j].from+","+reqs[index].link[j].to+","+i+","+k+"]:=";
					for(int p=i;p<sub.slotsNum&&(p<i+Math.ceil(reqs[index].link[j].bw/(12.5*k))+sub.diffSlot);p++){
						//if(i+Math.ceil(reqs[index].link[j].bw/(12.5*k))+sub.diffSlot > sub.slotsNum) {
						//	break;
						//}
						data += p + " ";
						//break;
					}
					data += ";\r\n"; 
					myDowith.SaveFile("glpsolRSA.dat", data, true);
					if(k>=4) k+=2;
					else k++;
				}
			}
		}	
		
		
		//set Slot1{(i,j) in Lv,a in SlotIndex,model in MD};/*��a+|bw/model|+G-1��a+|bw/model|+G-1+1,...,a*/
		data = "";
		for(int j = 0; j < reqs[index].links; j ++) {
			if(reqs[index].link[j].from > reqs[index].link[j].to) continue;//chenxhɾ��20200903
			for(int i=0;i<sub.slotsNum;i++){
				for(int k=1;k<9;){
					data = "set Slot1["+reqs[index].link[j].from+","+reqs[index].link[j].to+","+i+","+k+"]:=";
					for(int p=i;p>=0&&(p>=i-Math.ceil(reqs[index].link[j].bw/(12.5*k))-sub.diffSlot+1);p--){
						//if(i-Math.ceil(reqs[index].link[j].bw/(12.5*k))-sub.diffSlot+1<0) {
						//	break;
						//}
						data += p + " ";
					}
					data += ";\r\n"; 
					if(k>=4) k+=2;
					else k++;
					myDowith.SaveFile("glpsolRSA.dat", data, true);
				}
			}
		}	
		
		
		//param c{k in Ns};/*����ڵ�ļ��㵥λ����*/
		data = "param c:=\r\n";
		if(Parameters.CurrentVONEMethod == Parameters.MapVONE01ILPLinCXH || Parameters.CurrentVONEMethod == Parameters.MapVONE01ILPPRLinCXH || Parameters.CurrentVONEMethod == Parameters.MapVONECXHNode){
			for (int i = 0; i < sub.nodes; i++) {
				data += i + " " + s2v_n[i].rest_cpu +"\r\n";//s2v_n[i].rest_cpu
			}
		} else if(Parameters.CurrentVONEMethod == Parameters.MapVONE01ILPLin || Parameters.CurrentVONEMethod == Parameters.MapVONE01ILPLinFB){
			for (int i = 0; i < sub.nodes; i++) {
				data += i + " " + s2v_n[i].rest_cpu +"\r\n";//s2v_n[i].rest_cpu
			}
		} 
		data += ";\r\n";	
		myDowith.SaveFile("glpsolRSA.dat", data, true);
		
		if(Parameters.CurrentVONEMethod == Parameters.MapVONE01ILPLin_EquilibriumCXH){
			data = "param costCPU:=\r\n";
			for (int i = 0; i < sub.nodes; i++) {
				for(int j = 0;j < reqs[index].nodes;j++)
					if(s2v_n[i].rest_cpu >= reqs[index].cpu[j])
						data += i + " " + j + " " + 1.0/(s2v_n[i].rest_cpu-reqs[index].cpu[j]) +"\r\n";//s2v_n[i].rest_cpu
			}
			data += ";\r\n";	
			myDowith.SaveFile("glpsolRSA.dat", data, true);
		}
		
			
		//param c{k in Ns};/*����ڵ�ļ��㵥λ����*/
		
		if(Parameters.CurrentVONEMethod == Parameters.MapVONECXHNode){
			data = "param costOfNode:=\r\n";
			for (int i = 0; i < sub.nodes; i++) {
				data += i + " " + subStatic.cpu[i] +"\r\n";//s2v_n[i].rest_cpu
				//data += i + " " + s2v_n[i].rest_cpu +"\r\n";//s2v_n[i].rest_cpu
			}
			data += ";\r\n";	
			myDowith.SaveFile("glpsolRSA.dat", data, true);
		} 
		
		
		
		
		data = "";
		if(Parameters.CurrentVONEMethod == Parameters.MapVONE01ILPPRLinCXH){
			data = "param ac:=\r\n";
			//����pagerankֵ
			double vNodePageRank[] = new double[reqs[index].nodes];
			double sNodePageRank[] = new double[sub.nodes];
			//	InitVNodePageRank(reqs,index);
			
			vNodePageRank=InitVNodePageRank(vNodePageRank,reqs,index);
			sNodePageRank= InitSNodePageRank(sNodePageRank, sub);
			if(Parameters.RecordLogModel){
				String str = "Check vNodePR[]=";
				for(int i=0;i<reqs[index].nodes;i++){
					str += ""+i+" "+vNodePageRank[i]+" ";
				}
				WriteFilePlus("process.txt",str);
				
				str = "Check sNodePR[]=";
				for(int i=0;i<sub.nodes;i++){
					str += ""+i+" "+sNodePageRank[i]+" ";
				}
				WriteFilePlus("process.txt",str);
			}
			for(int i=0;i<reqs[index].nodes;i++){
				for (int j = 0; j < sub.nodes; j++) {
					if(reqs[index].cpu[i]<=s2v_n[j].rest_cpu){
						if(vNodePageRank[i] <= sNodePageRank[j])
							data += i + " " + j + " " + (sNodePageRank[j]-vNodePageRank[i]) +"\r\n";
						else data += i + " " + j + " " + (vNodePageRank[i]-sNodePageRank[j]) +"\r\n";
					}
				}
			}
			data += ";\r\n";	
			myDowith.SaveFile("glpsolRSA.dat", data, true);
		}
		
		
		//param cs{(i,j) in Ls};/*������·��Ƶ�ײ۵�λ���ۣ��볤�ȳɱ���*/
		data = "param cs:=\r\n";
		for (int i = 0; i < sub.links; i++) {
			if(sub.link[i].from > sub.link[i].to) continue;//chenxhɾ��20200903
			data += sub.link[i].from + " " + sub.link[i].to + " " + sub.link[i].length+"\r\n";
			data += sub.link[i].to + " " + sub.link[i].from + " " + sub.link[i].length+"\r\n";//20200903����
		}
		data += ";\r\n";	
		myDowith.SaveFile("glpsolRSA.dat", data, true);
		
		//param p{i in Nv};/*����ڵ�CPU*/
		data = "param p:=\r\n";
		for (int i = 0; i < reqs[index].nodes; i++) {
			data += i + " " + reqs[index].cpu[i]+"\r\n";
		}
		data += ";\r\n";	
		myDowith.SaveFile("glpsolRSA.dat", data, true);
		
		//param pbw{(i,j) in Lv};/*������·��������*/
		data = "param pbw:=\r\n";
		for (int i = 0; i < reqs[index].links; i++) {
			if(reqs[index].link[i].from > reqs[index].link[i].to) continue;
			data += reqs[index].link[i].from + " " + reqs[index].link[i].to + " " + reqs[index].link[i].bw+"\r\n";
		}
		data += ";\r\n";	
		myDowith.SaveFile("glpsolRSA.dat", data, true);
		
		//param F;/*����Ƶ�ײ�����*/
		data = "param F:="+(sub.slotsNum-1)+";\r\n";
		myDowith.SaveFile("glpsolRSA.dat", data, true);
		
		//param U{k in Ns};/*����ڵ�k��ʣ��cpu*/
		data = "param U:=\r\n";
		for (int i = 0; i < sub.nodes; i++) {
			data += i + " " + s2v_n[i].rest_cpu +"\r\n";//s2v_n[i].rest_cpu
		}
		data += ";\r\n";	
		myDowith.SaveFile("glpsolRSA.dat", data, true);
		
		//param USlot{(i,j) in Ls,t in SlotIndex};/*������·Ƶ�ײ۵�״̬��1��ʾ���У�0��ʾ��ռ��*/
		data = "param USlot:=\r\n";
		for (int i = 0; i < sub.links; i++) {
			if(sub.link[i].from > sub.link[i].to) continue;//chenxhɾ��20200903
			for(int j=0;j<sub.slotsNum;j++){
				data += sub.link[i].from + " " + sub.link[i].to + " " +j + " "+ sub.slots[i][j]+"\r\n";
				data += sub.link[i].to + " " + sub.link[i].from + " " +j + " "+ sub.slots[i][j]+"\r\n";//20200903����
			}
		}
		data += ";\r\n";	
		myDowith.SaveFile("glpsolRSA.dat", data, true);
		
		//param d{(i,j) in Ls};/*������·�ĳ���*/
		data = "param d:=\r\n";
		for (int i = 0; i < sub.links; i++) {
			if(sub.link[i].from > sub.link[i].to) continue;//chenxhɾ��20200903
			data += sub.link[i].from + " " + sub.link[i].to + " " + sub.link[i].length+"\r\n";
			data += sub.link[i].to + " " + sub.link[i].from + " " + sub.link[i].length+"\r\n";//����20200903
		}
		data += ";\r\n";	
		myDowith.SaveFile("glpsolRSA.dat", data, true);
		
		//param dis{m in MD};/*����ģʽ��������*/
		data = "param dis:=\r\n";
		data += "1 3000\r\n";
		data += "2 1500\r\n";
		data += "3 750\r\n";
		data += "4 375\r\n";
		data += "6 94\r\n";
		data += "8 24\r\n";
		data += ";\r\n";	
		myDowith.SaveFile("glpsolRSA.dat", data, true);
		
		//param R;/*��������*/
		data = "param R:=100000000;\r\n";
		myDowith.SaveFile("glpsolRSA.dat", data, true);
		
		//param G;/*���������*/
		data = "param G:="+sub.diffSlot+";\r\n";
		myDowith.SaveFile("glpsolRSA.dat", data, true);
		
		System.out.println("Done");
		
		try {
			String s;
			Process process = null;
			if(Parameters.CurrentVONEMethod == Parameters.MapVONE01ILPLinStrong){
				//process = Runtime.getRuntime().exec("cmd /c C:/������/VONE/VONE/glpk-4.60/w64/glpsol.exe -m C:/������/VONE/VONE/glpk-4.60/w64/glpsolMILPPRByLinChen.mod -d glpsolRSA.dat -o glpsolRSA.o");
				process = Runtime.getRuntime().exec("cmd /c C:/������/VONE/VONE/glpk-4.60/w64/glpsol.exe -m C:/������/VONE/VONE/glpk-4.60/w64/glpsolMILPByLinFB.mod -d glpsolRSA.dat -o glpsolRSA.o");
				//C:\������\VONE\VONE\glpk-4.60\w64
			} else if(Parameters.CurrentVONEMethod == Parameters.MapVONECXHNode){
				//process = Runtime.getRuntime().exec("cmd /c C:/������/VONE/VONE/glpk-4.60/w64/glpsol.exe -m C:/������/VONE/VONE/glpk-4.60/w64/glpsolMILPPRByLinChen.mod -d glpsolRSA.dat -o glpsolRSA.o");
				process = Runtime.getRuntime().exec("cmd /c C:/������/VONE/VNE/glpk-4.60/w64/glpsol.exe -m C:/������/VONE/VNE/glpk-4.60/w64/glpsolMILPByLinChenNodeUndirectDiagram.mod -d glpsolRSA.dat -o glpsolRSA.o");
				
			} else if(Parameters.CurrentVONEMethod == Parameters.MapVONE01ILPPRLinCXH){
				//process = Runtime.getRuntime().exec("cmd /c C:/������/VONE/VONE/glpk-4.60/w64/glpsol.exe -m C:/������/VONE/VONE/glpk-4.60/w64/glpsolMILPPRByLinChen.mod -d glpsolRSA.dat -o glpsolRSA.o");
				process = Runtime.getRuntime().exec("cmd /c C:/������/VONE/VNE/glpk-4.60/w64/glpsol.exe -m C:/������/VONE/VNE/glpk-4.60/w64/glpsolMILPPRByLinChenUndirectDiagram.mod -d glpsolRSA.dat -o glpsolRSA.o");
			} else if(Parameters.CurrentVONEMethod == Parameters.MapVONE01ILPLin_EquilibriumCXH){
				//process = Runtime.getRuntime().exec("cmd /c C:/������/VONE/VONE/glpk-4.60/w64/glpsol.exe -m C:/������/VONE/VONE/glpk-4.60/w64/glpsolMILPPRByLinChen.mod -d glpsolRSA.dat -o glpsolRSA.o");
				process = Runtime.getRuntime().exec("cmd /c C:/������/VNE/VNE/VNE/glpk-4.60/w64/glpsol.exe -m C:/������/VNE/VNE/VNE/glpk-4.60/w64/glpsolMILPByLinUndirectDiagram-Equilibrium.mod -d glpsolRSA.dat -o glpsolRSA.o");
				//process = Runtime.getRuntime().exec("cmd /c C:/������/VONE/VNE/glpk-4.60/w64/glpsol.exe -m C:/������/VONE/VNE/glpk-4.60/w64/glpsolMILPByLinUndirectDiagram-glpsolMILPByLinUndirectDiagram-Equilibrium.mod -d glpsolRSA.dat -o glpsolRSA.o");
				
			} else {
				process = Runtime.getRuntime().exec("cmd /c C:/������/VNE/VNE/VNE/glpk-4.60/w64/glpsol.exe -m C:/������/VNE/VNE/VNE/glpk-4.60/w64/glpsolMILPByLin.mod  -d glpsolRSA.dat -o glpsolRSA.o");
				//process = Runtime.getRuntime().exec("cmd /c C:/������/VONE/VONE/glpk-4.60/w64/glpsol.exe -m C:/������/VONE/VONE/glpk-4.60/w64/glpsolMILPByLin.mod  -d glpsolRSA.dat -o glpsolRSA.o");
				//process = Runtime.getRuntime().exec("cmd /c C:/������/VNE/VNE/VNE/glpk-4.60/w64/glpsol.exe -m C:/������/VNE/VNE/VNE/glpk-4.60/w64/glpsolMILPByLinUndirectDiagram.mod -d glpsolRSA.dat -o glpsolRSA.o");
				//process = Runtime.getRuntime().exec("cmd /c C:/������/VONE/VNE/glpk-4.60/w64/glpsol.exe -m C:/������/VONE/VNE/glpk-4.60/w64/glpsolMILPByLinUndirectDiagram.mod -d glpsolRSA.dat -o glpsolRSA.o");
			}
			//if(embedModelOrAlgo == Parameters.MapVONE3ByWangY){
			//	process = Runtime.getRuntime().exec("cmd /c C:/������/VONE/VONE/glpk-4.60/w64/glpsol.exe -m C:/������/VONE/VONE/glpk-4.60/w64/glpsolMILVONE3NodesNoDataWangY.mod -d glpsolRSA.dat -o glpsolRSA.o");
			//} else if(embedModelOrAlgo == Parameters.MapVONE3PByWangYAndChenxh){
			//	process = Runtime.getRuntime().exec("cmd /c C:/������/VONE/VONE/glpk-4.60/w64/glpsol.exe -m C:/������/VONE/VONE/glpk-4.60/w64/glpsolMILPVONE3NodesNoDataWangY.mod -d glpsolRSA.dat -o glpsolRSA.o");
			//}
			//Process process = Runtime.getRuntime().exec("cmd /c C:/������/VONE/VONE/glpk-4.60/w64/glpsol.exe -m C:/������/VONE/VONE/glpk-4.60/w64/glpsol01ILPVONE3PNodesWangY.mod -d glpsolRSA.dat -o glpsolRSA.o");
			//Process process = Runtime.getRuntime().exec("cmd /c C:/������/VONE/VONE/glpk-4.60/w64/glpsol.exe -m C:/������/VONE/VONE/glpk-4.60/w64/glpsolMILPVONE3NodesNoDataWangY.mod -d glpsolRSA.dat -o glpsolRSA.o");
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
	
	private void CreateVONE01ILPByLinDirectDiagram(EOSubstrateNetwork sub,VONRequest reqs[],int index)
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
		
		data = "set Lv:=\r\n";
		for(int j = 0; j < reqs[index].links; j ++) {
			if(reqs[index].link[j].from < reqs[index].link[j].to)
				data += reqs[index].link[j].from + " " + reqs[index].link[j].to + "\r\n";
		}
		data += ";\r\n"; 		 
		myDowith.SaveFile("glpsolRSA.dat", data, true);
		
		data = "set Ns:=";
		for(int j = 0; j < sub.nodes; j ++) {
			data += j + " ";
		}
		data += ";\r\n"; 		 
		myDowith.SaveFile("glpsolRSA.dat", data, true);
		
		data = "";
		for(int j = 0; j < sub.nodes; j ++) {
			data += "set Nss[" + j + "]:=";
			for(int i=0;i<sub.links;i++){
				if(sub.link[i].from == j){
					//if(sub.link[i].from < sub.link[i].to) //chenxhɾ��20200903���޸�����ͼΪ����ͼ
						data += sub.link[i].to + " ";
				}
				if(sub.link[i].to == j){
					//if(sub.link[i].from < sub.link[i].to) //chenxhɾ��20200903���޸�����ͼΪ����ͼ
						data += sub.link[i].from + " ";
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
					//if(sub.link[i].from < sub.link[i].to) //chenxhɾ��20200903���޸�����ͼΪ����ͼ
						data += sub.link[i].from + " ";
				}
				if(sub.link[i].from == j){
					//if(sub.link[i].from < sub.link[i].to) //chenxhɾ��20200903���޸�����ͼΪ����ͼ
						data += sub.link[i].to + " ";
				}
			}
			data += ";\r\n"; 
			myDowith.SaveFile("glpsolRSA.dat", data, true);
		}		 
		
		
		data = "set Ls:=\r\n";
		for(int j = 0; j < sub.links; j ++) {
			//if(sub.link[j].from < sub.link[j].to)//chenxhɾ��20200903���޸�����ͼΪ����ͼ
				data += sub.link[j].from + " " + sub.link[j].to + "\r\n";
				data += sub.link[j].to + " " + sub.link[j].from + "\r\n";//add chenxh20200903
		}
		data += ";\r\n"; 		 
		myDowith.SaveFile("glpsolRSA.dat", data, true);
		
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
			//if(reqs[index].link[j].from > reqs[index].link[j].to) continue;//chenxhɾ��20200903����ͼת����ͼ
			for(int i=0;i<sub.slotsNum;i++){
				for(int k=1;k<9;){
					data = "set Slot["+reqs[index].link[j].from+","+reqs[index].link[j].to+","+i+","+k+"]:=";
					for(int p=i;p<sub.slotsNum&&(p<i+Math.ceil(reqs[index].link[j].bw/(12.5*k))+sub.diffSlot);p++){
						//if(i+Math.ceil(reqs[index].link[j].bw/(12.5*k))+sub.diffSlot > sub.slotsNum) {
						//	break;
						//}
						data += p + " ";
						//break;
					}
					data += ";\r\n"; 
					myDowith.SaveFile("glpsolRSA.dat", data, true);
					if(k>=4) k+=2;
					else k++;
				}
			}
		}	
		
		
		//set Slot1{(i,j) in Lv,a in SlotIndex,model in MD};/*��a+|bw/model|+G-1��a+|bw/model|+G-1+1,...,a*/
		data = "";
		for(int j = 0; j < reqs[index].links; j ++) {
			//if(reqs[index].link[j].from > reqs[index].link[j].to) continue;//chenxhɾ��20200903
			for(int i=0;i<sub.slotsNum;i++){
				for(int k=1;k<9;){
					data = "set Slot1["+reqs[index].link[j].from+","+reqs[index].link[j].to+","+i+","+k+"]:=";
					for(int p=i;p>=0&&(p>=i-Math.ceil(reqs[index].link[j].bw/(12.5*k))-sub.diffSlot+1);p--){
						//if(i-Math.ceil(reqs[index].link[j].bw/(12.5*k))-sub.diffSlot+1<0) {
						//	break;
						//}
						data += p + " ";
					}
					data += ";\r\n"; 
					if(k>=4) k+=2;
					else k++;
					myDowith.SaveFile("glpsolRSA.dat", data, true);
				}
			}
		}	
		
		
		//param c{k in Ns};/*����ڵ�ļ��㵥λ����*/
		data = "param c:=\r\n";
		if(Parameters.CurrentVONEMethod == Parameters.MapVONE01ILPLinCXH || Parameters.CurrentVONEMethod == Parameters.MapVONE01ILPPRLinCXH){
			for (int i = 0; i < sub.nodes; i++) {
				data += i + " " + s2v_n[i].rest_cpu +"\r\n";//s2v_n[i].rest_cpu
			}
		} else if(Parameters.CurrentVONEMethod == Parameters.MapVONE01ILPLin){
			for (int i = 0; i < sub.nodes; i++) {
				data += i + " " + s2v_n[i].rest_cpu +"\r\n";//s2v_n[i].rest_cpu
			}
		} 
		data += ";\r\n";	
		myDowith.SaveFile("glpsolRSA.dat", data, true);
		data = "";
		if(Parameters.CurrentVONEMethod == Parameters.MapVONE01ILPPRLinCXH){
			data = "param ac:=\r\n";
			//����pagerankֵ
			double vNodePageRank[] = new double[reqs[index].nodes];
			double sNodePageRank[] = new double[sub.nodes];
			//	InitVNodePageRank(reqs,index);
			
			vNodePageRank=InitVNodePageRank(vNodePageRank,reqs,index);
			sNodePageRank= InitSNodePageRank(sNodePageRank, sub);
			if(Parameters.RecordLogModel){
				String str = "Check vNodePR[]=";
				for(int i=0;i<reqs[index].nodes;i++){
					str += ""+i+" "+vNodePageRank[i]+" ";
				}
				WriteFilePlus("process.txt",str);
				
				str = "Check sNodePR[]=";
				for(int i=0;i<sub.nodes;i++){
					str += ""+i+" "+sNodePageRank[i]+" ";
				}
				WriteFilePlus("process.txt",str);
			}
			for(int i=0;i<reqs[index].nodes;i++){
				for (int j = 0; j < sub.nodes; j++) {
					if(reqs[index].cpu[i]<=s2v_n[j].rest_cpu){
						if(vNodePageRank[i] <= sNodePageRank[j])
							data += i + " " + j + " " + (sNodePageRank[j]-vNodePageRank[i]) +"\r\n";
						else data += i + " " + j + " " + (vNodePageRank[i]-sNodePageRank[j]) +"\r\n";
					}
				}
			}
			data += ";\r\n";	
			myDowith.SaveFile("glpsolRSA.dat", data, true);
		}
		
		
		//param cs{(i,j) in Ls};/*������·��Ƶ�ײ۵�λ���ۣ��볤�ȳɱ���*/
		data = "param cs:=\r\n";
		for (int i = 0; i < sub.links; i++) {
			//if(sub.link[i].from > sub.link[i].to) continue;//chenxhɾ��20200903
			data += sub.link[i].from + " " + sub.link[i].to + " " + sub.link[i].length+"\r\n";
			data += sub.link[i].to + " " + sub.link[i].from + " " + sub.link[i].length+"\r\n";//20200903����
		}
		data += ";\r\n";	
		myDowith.SaveFile("glpsolRSA.dat", data, true);
		
		//param p{i in Nv};/*����ڵ�CPU*/
		data = "param p:=\r\n";
		for (int i = 0; i < reqs[index].nodes; i++) {
			data += i + " " + reqs[index].cpu[i]+"\r\n";
		}
		data += ";\r\n";	
		myDowith.SaveFile("glpsolRSA.dat", data, true);
		
		//param pbw{(i,j) in Lv};/*������·��������*/
		data = "param pbw:=\r\n";
		for (int i = 0; i < reqs[index].links; i++) {
			if(reqs[index].link[i].from > reqs[index].link[i].to) continue;
			data += reqs[index].link[i].from + " " + reqs[index].link[i].to + " " + reqs[index].link[i].bw+"\r\n";
		}
		data += ";\r\n";	
		myDowith.SaveFile("glpsolRSA.dat", data, true);
		
		//param F;/*����Ƶ�ײ�����*/
		data = "param F:="+(sub.slotsNum-1)+";\r\n";
		myDowith.SaveFile("glpsolRSA.dat", data, true);
		
		//param U{k in Ns};/*����ڵ�k��ʣ��cpu*/
		data = "param U:=\r\n";
		for (int i = 0; i < sub.nodes; i++) {
			data += i + " " + s2v_n[i].rest_cpu +"\r\n";//s2v_n[i].rest_cpu
		}
		data += ";\r\n";	
		myDowith.SaveFile("glpsolRSA.dat", data, true);
		
		//param USlot{(i,j) in Ls,t in SlotIndex};/*������·Ƶ�ײ۵�״̬��1��ʾ���У�0��ʾ��ռ��*/
		data = "param USlot:=\r\n";
		for (int i = 0; i < sub.links; i++) {
			//if(sub.link[i].from > sub.link[i].to) continue;//chenxhɾ��20200903
			for(int j=0;j<sub.slotsNum;j++){
				data += sub.link[i].from + " " + sub.link[i].to + " " +j + " "+ sub.slots[i][j]+"\r\n";
				data += sub.link[i].to + " " + sub.link[i].from + " " +j + " "+ sub.slots[i][j]+"\r\n";//20200903����
			}
		}
		data += ";\r\n";	
		myDowith.SaveFile("glpsolRSA.dat", data, true);
		
		//param d{(i,j) in Ls};/*������·�ĳ���*/
		data = "param d:=\r\n";
		for (int i = 0; i < sub.links; i++) {
			//if(sub.link[i].from > sub.link[i].to) continue;//chenxhɾ��20200903
			data += sub.link[i].from + " " + sub.link[i].to + " " + sub.link[i].length+"\r\n";
			data += sub.link[i].to + " " + sub.link[i].from + " " + sub.link[i].length+"\r\n";//����20200903
		}
		data += ";\r\n";	
		myDowith.SaveFile("glpsolRSA.dat", data, true);
		
		//param dis{m in MD};/*����ģʽ��������*/
		data = "param dis:=\r\n";
		data += "1 3000\r\n";
		data += "2 1500\r\n";
		data += "3 750\r\n";
		data += "4 375\r\n";
		data += "6 94\r\n";
		data += "8 24\r\n";
		data += ";\r\n";	
		myDowith.SaveFile("glpsolRSA.dat", data, true);
		
		//param R;/*��������*/
		data = "param R:=100000000;\r\n";
		myDowith.SaveFile("glpsolRSA.dat", data, true);
		
		//param G;/*���������*/
		data = "param G:="+sub.diffSlot+";\r\n";
		myDowith.SaveFile("glpsolRSA.dat", data, true);
		
		System.out.println("Done");
		
		try {
			String s;
			Process process = null;
			if(Parameters.CurrentVONEMethod == Parameters.MapVONE01ILPPRLinCXH){
				//process = Runtime.getRuntime().exec("cmd /c C:/������/VONE/VONE/glpk-4.60/w64/glpsol.exe -m C:/������/VONE/VONE/glpk-4.60/w64/glpsolMILPPRByLinChen.mod -d glpsolRSA.dat -o glpsolRSA.o");
				process = Runtime.getRuntime().exec("cmd /c  E:/���Թ�����/CEEA/VNE/glpk-4.60/w64/glpsol.exe -m  E:/���Թ�����/CEEA/VNE/glpk-4.60/w64/glpsolMILPPRByLinChenDirectDiagram.mod  -d glpsolRSA.dat -o glpsolRSA.o");
				
			} else {
				//process = Runtime.getRuntime().exec("cmd /c C:/������/VONE/VONE/glpk-4.60/w64/glpsol.exe -m C:/������/VONE/VONE/glpk-4.60/w64/glpsolMILPByLin.mod -d glpsolRSA.dat -o glpsolRSA.o");
				process = Runtime.getRuntime().exec("cmd /c C:/������/VONE/VNE/glpk-4.60/w64/glpsol.exe -m C:/������/VONE/VNE/glpk-4.60/w64/glpsolMILPByLinDirectDiagram.mod -d glpsolRSA.dat -o glpsolRSA.o");
			}
			//if(embedModelOrAlgo == Parameters.MapVONE3ByWangY){
			//	process = Runtime.getRuntime().exec("cmd /c C:/������/VONE/VONE/glpk-4.60/w64/glpsol.exe -m C:/������/VONE/VONE/glpk-4.60/w64/glpsolMILVONE3NodesNoDataWangY.mod -d glpsolRSA.dat -o glpsolRSA.o");
			//} else if(embedModelOrAlgo == Parameters.MapVONE3PByWangYAndChenxh){
			//	process = Runtime.getRuntime().exec("cmd /c C:/������/VONE/VONE/glpk-4.60/w64/glpsol.exe -m C:/������/VONE/VONE/glpk-4.60/w64/glpsolMILPVONE3NodesNoDataWangY.mod -d glpsolRSA.dat -o glpsolRSA.o");
			//}
			//Process process = Runtime.getRuntime().exec("cmd /c C:/������/VONE/VONE/glpk-4.60/w64/glpsol.exe -m C:/������/VONE/VONE/glpk-4.60/w64/glpsol01ILPVONE3PNodesWangY.mod -d glpsolRSA.dat -o glpsolRSA.o");
			//Process process = Runtime.getRuntime().exec("cmd /c C:/������/VONE/VONE/glpk-4.60/w64/glpsol.exe -m C:/������/VONE/VONE/glpk-4.60/w64/glpsolMILPVONE3NodesNoDataWangY.mod -d glpsolRSA.dat -o glpsolRSA.o");
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
	���ƣ�int MapVONEFBByLinCXH(......)
	���ܣ���Lin�����ϼ��Ϸ�������
	������
		      s2v_nΪ����ڵ�ӳ�������ڵ����ݽṹ
		      s2v_lΪ������·ӳ��������·���ݽṹ
		      v2sΪ����ӳ��������������ݽṹ 
		      indexΪ��index����������
	,int ret[],int p[][],ArrayList<Object> list
	����ֵ��0���ɹ����أ�-1��ʧ�ܷ��� 
	******************************************************************/
	private int MapVONEFBByLinCXH(EOSubstrateNetwork sub,VONRequest reqs[],int index)
	{
		//��ʼ������,-1������δ���䣬>-1�����Ѿ�����Ľڵ������·��
		int[] vNodeEmbed = new int[reqs[index].nodes];
		int[] sNodeEmbed = new int[sub.nodes];
		int[] vLinkEmbed = new int[reqs[index].links];
		
		for(int i=0;i<reqs[index].nodes;i++){
			vNodeEmbed[i] = -1;
		}
		for(int i=0;i<sub.nodes;i++){
			sNodeEmbed[i] = -1;
		}
		for(int i=0;i<reqs[index].links;i++){
			vLinkEmbed[i] = -1;
		}
		
		//p[][]:��¼·����slotIndex[][]:slotIndex[][0]:���ص���ʼƵ�ײۣ�slotIndex[][1]:���ص�Ƶ�ײ�����
		int p[][] = new int[reqs[index].links][sub.nodes];
		int slotIndex[][] = new int[reqs[index].links][2];//slotIndex[][0]:���ص���ʼƵ�ײۣ�slotIndex[][1]:���ص�Ƶ�ײ�����
		for(int i=0;i<reqs[index].links;i++){
			for(int j=0;j<sub.nodes;j++)
				p[i][j] = -1;
			slotIndex[i][0] = slotIndex[i][1] = -1;
		}
				
		EOSubstrateNetwork subCopy = new EOSubstrateNetwork();
		Clone(subCopy,sub);
		int[] MD = new int[Parameters.MDSum];
		int[] MDLength = new int[Parameters.MDSum];
		GetMDAndMDLength(MD,MDLength);//�õ�����ģʽ�е�md�Լ�����
		
		//�õ��ź����vlinks
		int[] vLinkSet = new int[reqs[index].links];
		if(Parameters.CurrentVONEMethod == Parameters.MapVONELin_FB_SortByNodeDegree){
			SortVLinksByAlg1(reqs,index,vLinkSet);
		} else if(Parameters.CurrentVONEMethod == Parameters.MapVONELin_FB_SortByNodeDegreeAndBW){
			SortVLinksByAlg2(reqs,index,vLinkSet);
		} else if(Parameters.CurrentVONEMethod == Parameters.MapVONELin_FB_SortByBW){
			SortVLinksByAlg3(reqs,index,vLinkSet);
		}
		//Alg1\Alg2\Alg3
		int g = 0;
		boolean findShPath = true;
		WeightedDirectedGraph auxGraph = new WeightedDirectedGraph(2*sub.nodes + 2);
		
		int[] ret = new int[2];
		int slotsNum = -1;//�����Ƶ�ײ�����
		int a=0;
		boolean findPath = false;
		int[] nodeEmbed = new int[2];//�ڵ�ӳ��Ľ��
		
		//�����������ݽṹ��ʼ����ʼ
		int nodeNum = subCopy.nodes;
		int[] nodeH = new int[subCopy.nodes];
		int[] nDegree = new int[subCopy.nodes];//�ڵ�Ķ�
		for(int ih=0;ih<nodeNum;ih++){
			nodeH[ih] = -1;//-1��ʾδ�������߱�ʶ
			nDegree[ih] = 0;//�ڵ�Ķȳ�ʼ��Ϊ0
		}
		for(int ih=0;ih<nodeNum;ih++){
			for(int ld=0;ld<subCopy.links;ld++)
				if(subCopy.link[ld].from == ih || subCopy.link[ld].to == ih)
					nDegree[ih]++;//�ڵ�Ķ�����1
		}
		//end
		//nodeNum = 2;
		for(int ii=0;ii<reqs[index].links;ii++){
			int i = vLinkSet[ii];
			for(int md=Parameters.MDSum-1;md>0;md--){
				g = CalculateSlots(reqs[index].link[i].bw,MD[md],Parameters.GuardBand);	
				for(a=0;a<Parameters.MaxSlots-g;) {
					findPath = false;
					auxGraph = BuildAuxGraphByAlg5(subCopy,reqs,index,i,a,a+g-1,MD[md],vLinkEmbed,p,vNodeEmbed,ret,MDLength[md],sNodeEmbed);//��������ͼ
					if(ret[0] == -2){//˵�����Ȳ��������
						break;
					} else if(ret[0] == -1){//˵��û�й���ɹ�����ͼ
						a++;
						continue;
					}
					//�������ƿ�ʼ
					nodeNum = 2;//subCopy.nodes;//2;////2;
					while(nodeNum <= subCopy.nodes){
						//���õײ������ӳ�����·�ͽڵ�
						SetHiberNodes(subCopy,reqs,index,i,nodeNum,nodeH,nDegree);
						auxGraph.findShortestPath(2*subCopy.nodes,nodeH);//from�ڵ㼴��2*sub.nodes
						//auxGraph.findShortestPath(2*subCopy.nodes);//from�ڵ㼴��2*sub.nodes
						if(auxGraph.shortestPath[2*subCopy.nodes+1].distance == auxGraph.INFINITY){
							nodeNum += Parameters.FBStep;
							//System.out.println("No find path."+auxGraph.shortestPath[2*subCopy.nodes].distance);
							continue;//˵��û���ҵ�·��
						} else {
							//if(auxGraph.shortestPath[2*subCopy.nodes+1].parentVert != 2*subCopy.nodes ){
							int n1 = auxGraph.shortestPath[2*subCopy.nodes+1].parentVert;
							int n2 = auxGraph.shortestPath[n1].parentVert;
							int n3 = auxGraph.shortestPath[n2].parentVert;
							if(n3 != 2*subCopy.nodes ){
								nodeNum += Parameters.FBStep;
								System.out.println("nodeNum��"+nodeNum+" subCopy.nodes:"+subCopy.nodes);
								if(nodeNum <= subCopy.nodes)
									continue;//˵��û���ҵ�·��
							}
							findPath = true;
						}
						
						if(!auxGraph.GetPath(p[i],auxGraph)){
							//findPath = false;
							//nodeNum += Parameters.FBStep;
							//continue;
						}
						
						GetNodesEmbed(subCopy,auxGraph,nodeEmbed);//�õ��ڵ�ӳ����
						if(nodeEmbed[0] == nodeEmbed[1]){//from�ڵ㼴��2*sub.nodes,to�ڵ㼴��2*sub.nodes+1
							DeleteHigherCostLink(auxGraph,subCopy.nodes*2,subCopy.nodes*2+1,nodeEmbed[0]);//ɾ���ϸߵĴ�����·
							findPath = false;
							//break;
							continue;
							//nodeNum += Parameters.FBStep;
							//continue;//line 10
						}
						if(Parameters.DebugModel){
							System.out.println("Print Path:");
							auxGraph.displayPaths();
							PrintPath(p[i],nodeEmbed[0],nodeEmbed[1]);
						}
						//if(Parameters.DebugModel) PrintPath(p[i],nodeEmbed[0],nodeEmbed[1]);
						slotsNum = g;//CalculateSlots(reqs[index].link[i].bw,MD[md],Parameters.GuardBand);
						if(PathFitConstraintOfModu(subCopy,p[i],nodeEmbed[0],nodeEmbed[1],MDLength[md])){
							//nodeNum += Parameters.FBStep;
							//continue;
							break;
						} else {
							nodeNum += Parameters.FBStep;
							//continue;
						}
					}
					//�������ƽ���
					if(findPath == false){
						a++;
						continue;
					} else {
						break;
					}
				}
				if(findPath && a<Parameters.MaxSlots-g){
					int vNodeFrom = reqs[index].link[i].from;
					int vNodeTo = reqs[index].link[i].to;
					
					GetNodesEmbed(subCopy,auxGraph,nodeEmbed);//�õ��ڵ�ӳ����
					//���½ڵ�ӳ��
					vNodeEmbed[vNodeFrom] = nodeEmbed[0];//����ڵ�ӳ�䵽����ڵ�
					vNodeEmbed[vNodeTo] = nodeEmbed[1];//����ڵ�ӳ�䵽����ڵ�
					//����cpu
					if(sNodeEmbed[nodeEmbed[0]] == -1){
						UpdateSub(subCopy,nodeEmbed[0],reqs[index].cpu[vNodeFrom]);
					}
					if(sNodeEmbed[nodeEmbed[1]] == -1){
						UpdateSub(subCopy,nodeEmbed[1],reqs[index].cpu[vNodeTo]);
					}
					sNodeEmbed[nodeEmbed[0]] = vNodeFrom;
					sNodeEmbed[nodeEmbed[1]] = vNodeTo;
					//������·ӳ��·������auxGraph��p
					//��·�Ѿ�����
					vLinkEmbed[i] = 1;
					//���µײ�����subCopy
					//UpdateSub(EOSubstrateNetwork sub,int sNode1,int sNode2,int ret[],int p[])
					int sNode1,sNode2;
					sNode1 = vNodeEmbed[reqs[index].link[i].from];
					sNode2 = vNodeEmbed[reqs[index].link[i].to];
					slotIndex[i][0] = a;//��ʼƵ�ײ�
					slotIndex[i][1] = a+slotsNum-1;//slotsNumΪ�����slot����
					if(Parameters.RecordLogModel){
						int aa=a+slotsNum-1;
						String str = "allocation req "+index+" req["+i+"]:["+a+"-"+aa+"]\r\n";
						WriteFilePlus("process.txt",str);
					}
					UpdateSub(subCopy,sNode2,sNode1,slotIndex[i],p[i]);
					
					
					break;
				}
			}
			if(!findPath || a>=Parameters.MaxSlots-g){//���û���ҵ�
				return -1;
			}
		}
		
		//���������·û��ӳ�䣬��ʧ�ܷ���
		for(int i=0;i<reqs[index].links;i++){
			if(vLinkEmbed[i] == -1) return -1;//ʧ�ܷ���
		}
		//����cpu
		UpdateSub(sub,subCopy);
		//��¼�ڵ����·ӳ����
		
		AddNodesMap(reqs,index,vNodeEmbed);//����s2v_n��v2s
		AddLinksMapBySPFA(sub,reqs,index,slotIndex,p);//���µײ�����
		//AddLinksMapBySPFA(sub,reqs,index,retOther,p);//���µײ�����
		if(Parameters.RecordLogModel){
			String str = "allocation req "+index+" is embedded successfully.";
			WriteFilePlus("process.txt",str);
			WriteFileOfGraph(sub,"process.txt",true);
		}
		UpdateSubSlots(sub,subCopy);
		return 0;
		
	}
	
	/******************************************************************
	���ƣ�int MapVONELinFA(......)
	���ܣ�����ͼ����������Ƭ��֪����
	������
		      s2v_nΪ����ڵ�ӳ�������ڵ����ݽṹ
		      s2v_lΪ������·ӳ��������·���ݽṹ
		      v2sΪ����ӳ��������������ݽṹ 
		      indexΪ��index����������
	,int ret[],int p[][],ArrayList<Object> list
	����ֵ��0���ɹ����أ�-1��ʧ�ܷ��� 
	����ʱ�䣺2020-07-17
	�����ߣ�������
	******************************************************************/
	private int MapVONEByLinFA(EOSubstrateNetwork sub,VONRequest reqs[],int index)
	{
		//��ʼ������,-1������δ���䣬>-1�����Ѿ�����Ľڵ������·��
		int[] vNodeEmbed = new int[reqs[index].nodes];
		int[] sNodeEmbed = new int[sub.nodes];
		int[] vLinkEmbed = new int[reqs[index].links];
		
		for(int i=0;i<reqs[index].nodes;i++){
			vNodeEmbed[i] = -1;
		}
		for(int i=0;i<sub.nodes;i++){
			sNodeEmbed[i] = -1;
		}
		for(int i=0;i<reqs[index].links;i++){
			vLinkEmbed[i] = -1;
		}
		
		//p[][]:��¼·����slotIndex[][]:slotIndex[][0]:���ص���ʼƵ�ײۣ�slotIndex[][1]:���ص�Ƶ�ײ�����
		int p[][] = new int[reqs[index].links][sub.nodes];
		int slotIndex[][] = new int[reqs[index].links][2];//slotIndex[][0]:���ص���ʼƵ�ײۣ�slotIndex[][1]:���ص�Ƶ�ײ�����
		for(int i=0;i<reqs[index].links;i++){
			for(int j=0;j<sub.nodes;j++)
				p[i][j] = -1;
			slotIndex[i][0] = slotIndex[i][1] = -1;
		}
				
		EOSubstrateNetwork subCopy = new EOSubstrateNetwork();
		Clone(subCopy,sub);
		int[] MD = new int[Parameters.MDSum];
		int[] MDLength = new int[Parameters.MDSum];
		GetMDAndMDLength(MD,MDLength);//�õ�����ģʽ�е�md�Լ�����
		
		//�õ��ź����vlinks
		int[] vLinkSet = new int[reqs[index].links];
		if(Parameters.CurrentVONEMethod == Parameters.MapVONELin_SortByNodeDegree){
			SortVLinksByAlg1(reqs,index,vLinkSet);
		} else if(Parameters.CurrentVONEMethod == Parameters.MapVONELin_SortByNodeDegreeAndBW){
			SortVLinksByAlg2(reqs,index,vLinkSet);
		} else if(Parameters.CurrentVONEMethod == Parameters.MapVONELin_SortByBW){
			SortVLinksByAlg3(reqs,index,vLinkSet);
		}
		//Alg1\Alg2\Alg3
		int g = 0;
		boolean findShPath = true;
		WeightedDirectedGraph auxGraph = new WeightedDirectedGraph(2*sub.nodes + 2);
		
		int[] ret = new int[2];
		int slotsNum = -1;//�����Ƶ�ײ�����
		int a=0;
		boolean findPath = false;
		int[] nodeEmbed = new int[2];//�ڵ�ӳ��Ľ��
		for(int ii=0;ii<reqs[index].links;ii++){
			int i = vLinkSet[ii];
			for(int md=Parameters.MDSum-1;md>0;md--){
				g = CalculateSlots(reqs[index].link[i].bw,MD[md],Parameters.GuardBand);
				
				for(a=0;a<Parameters.MaxSlots-g;) {
					findPath = false;
					//auxGraph = BuildAuxGraphByAlg5(subCopy,reqs,index,i,a,a+g-1,MD[md],vLinkEmbed,p,vNodeEmbed,ret,MDLength[md],sNodeEmbed);//��������ͼ
					auxGraph = BuildAuxGraphByAlg5(subCopy,reqs,index,i,a,a+g-1,MD[md],vLinkEmbed,p,vNodeEmbed,ret,MDLength[md],sNodeEmbed);//��������ͼ
					if(ret[0] == -2){//˵�����Ȳ��������
						break;
					} else if(ret[0] == -1){//˵��û�й���ɹ�����ͼ
						a++;
						continue;
					}
					//auxGraph.findShortestPath(2*subCopy.nodes);//from�ڵ㼴��2*sub.nodes
					//�ҵ�k���·������ÿ����ѡ·��������·�����У�
					
					//�������һ����С���ң���ѡ����С���У�
					//�����ڶ����ͬ�����У����ѡ��һ����
					//if(auxGraph.shortestPath[2*subCopy.nodes].distance == auxGraph.INFINITY && auxGraph.shortestPath[2*subCopy.nodes+1].distance == auxGraph.INFINITY){
					if(auxGraph.shortestPath[2*subCopy.nodes+1].distance == auxGraph.INFINITY){
						a++;
						//System.out.println("No find path."+auxGraph.shortestPath[2*subCopy.nodes].distance);
						continue;//˵��û���ҵ�·��
					} else {
						findPath = true;
					}
					
					if(Parameters.DebugModel) auxGraph.displayPaths();//displayPaths();
					if(!auxGraph.GetPath(p[i],auxGraph)){
						findPath = false;
					}
					GetNodesEmbed(subCopy,auxGraph,nodeEmbed);//�õ��ڵ�ӳ����
					if(nodeEmbed[0] == nodeEmbed[1]){//from�ڵ㼴��2*sub.nodes,to�ڵ㼴��2*sub.nodes+1
						DeleteHigherCostLink(auxGraph,subCopy.nodes*2,subCopy.nodes*2+1,nodeEmbed[0]);//ɾ���ϸߵĴ�����·
						a++;
						continue;//line 10
					}
					if(Parameters.DebugModel) PrintPath(p[i],nodeEmbed[0],nodeEmbed[1]);
					slotsNum = g;//CalculateSlots(reqs[index].link[i].bw,MD[md],Parameters.GuardBand);
					//int nodeRet = IfSameSSnode(subCopy,subCopy.nodes*2,subCopy.nodes*2+1,auxGraph);
					
					//if(nodeRet >= 0){//from�ڵ㼴��2*sub.nodes,to�ڵ㼴��2*sub.nodes+1
					
					if(PathFitConstraintOfModu(subCopy,p[i],nodeEmbed[0],nodeEmbed[1],MDLength[md])){
						//AddPathToSet(sub,auxGraph,embedSet,i);
						//a++;
						//continue;
						break;
					}
					a++;
				}
				if(findPath && a<Parameters.MaxSlots-g){
					int vNodeFrom = reqs[index].link[i].from;
					int vNodeTo = reqs[index].link[i].to;
					
					GetNodesEmbed(subCopy,auxGraph,nodeEmbed);//�õ��ڵ�ӳ����
					//���½ڵ�ӳ��
					vNodeEmbed[vNodeFrom] = nodeEmbed[0];//����ڵ�ӳ�䵽����ڵ�
					vNodeEmbed[vNodeTo] = nodeEmbed[1];//����ڵ�ӳ�䵽����ڵ�
					//����cpu
					if(sNodeEmbed[nodeEmbed[0]] == -1){
						UpdateSub(subCopy,nodeEmbed[0],reqs[index].cpu[vNodeFrom]);
					}
					if(sNodeEmbed[nodeEmbed[1]] == -1){
						UpdateSub(subCopy,nodeEmbed[1],reqs[index].cpu[vNodeTo]);
					}
					sNodeEmbed[nodeEmbed[0]] = vNodeFrom;
					sNodeEmbed[nodeEmbed[1]] = vNodeTo;
					//������·ӳ��·������auxGraph��p
					//��·�Ѿ�����
					vLinkEmbed[i] = 1;
					//���µײ�����subCopy
					//UpdateSub(EOSubstrateNetwork sub,int sNode1,int sNode2,int ret[],int p[])
					int sNode1,sNode2;
					sNode1 = vNodeEmbed[reqs[index].link[i].from];
					sNode2 = vNodeEmbed[reqs[index].link[i].to];
					slotIndex[i][0] = a;//��ʼƵ�ײ�
					slotIndex[i][1] = a+slotsNum-1;//slotsNumΪ�����slot����
					if(Parameters.RecordLogModel){
						int aa=a+slotsNum-1;
						String str = "allocation req "+index+" req.link["+i+"]:["+a+"-"+aa+"]\r\n";
						WriteFilePlus("process.txt",str);
					}
					UpdateSub(subCopy,sNode2,sNode1,slotIndex[i],p[i]);
					
					
					break;
				}
			}
			if(!findPath || a>=Parameters.MaxSlots-g){//���û���ҵ�
				return -1;
			}
		}
		
		//���������·û��ӳ�䣬��ʧ�ܷ���
		for(int i=0;i<reqs[index].links;i++){
			if(vLinkEmbed[i] == -1) return -1;//ʧ�ܷ���
		}
		//����cpu
		UpdateSub(sub,subCopy);
		//��¼�ڵ����·ӳ����
		
		AddNodesMap(reqs,index,vNodeEmbed);//����s2v_n��v2s
		AddLinksMapBySPFA(sub,reqs,index,slotIndex,p);//���µײ�����
		//AddLinksMapBySPFA(sub,reqs,index,retOther,p);//���µײ�����
		if(Parameters.RecordLogModel){
			String str = "allocation req "+index+" is embedded successfully.";
			WriteFilePlus("process.txt",str);
			WriteFileOfGraph(sub,"process.txt",true);
		}
		UpdateSubSlots(sub,subCopy);
		return 0;
	}
	
	/******************************************************************
	���ƣ�int MapVONEByLinFACA(......)
	���ܣ�����ͼ����������Ƭ��֪������ӵ�����⼼��
	������
		      s2v_nΪ����ڵ�ӳ�������ڵ����ݽṹ
		      s2v_lΪ������·ӳ��������·���ݽṹ
		      v2sΪ����ӳ��������������ݽṹ 
		      indexΪ��index����������
	,int ret[],int p[][],ArrayList<Object> list
	����ֵ��0���ɹ����أ�-1��ʧ�ܷ��� 
	******************************************************************/
	private int MapVONEByLinFACA(EOSubstrateNetwork sub,VONRequest reqs[],int index)
	{
		return 0;
	}
	
	
	/******************************************************************
	���ƣ�int MapVONEByLin(......)
	���ܣ�
	������
		      s2v_nΪ����ڵ�ӳ�������ڵ����ݽṹ
		      s2v_lΪ������·ӳ��������·���ݽṹ
		      v2sΪ����ӳ��������������ݽṹ 
		      indexΪ��index����������
	,int ret[],int p[][],ArrayList<Object> list
	����ֵ��0���ɹ����أ�-1��ʧ�ܷ��� 
	******************************************************************/
	private int MapVONEByLin(EOSubstrateNetwork sub,VONRequest reqs[],int index)
	{
		//��ʼ������,-1������δ���䣬>-1�����Ѿ�����Ľڵ������·��
		int[] vNodeEmbed = new int[reqs[index].nodes];
		int[] sNodeEmbed = new int[sub.nodes];
		int[] vLinkEmbed = new int[reqs[index].links];
		
		for(int i=0;i<reqs[index].nodes;i++){
			vNodeEmbed[i] = -1;
		}
		for(int i=0;i<sub.nodes;i++){
			sNodeEmbed[i] = -1;
		}
		for(int i=0;i<reqs[index].links;i++){
			vLinkEmbed[i] = -1;
		}
		
		//p[][]:��¼·����slotIndex[][]:slotIndex[][0]:���ص���ʼƵ�ײۣ�slotIndex[][1]:���ص�Ƶ�ײ�����
		int p[][] = new int[reqs[index].links][sub.nodes];
		int slotIndex[][] = new int[reqs[index].links][2];//slotIndex[][0]:���ص���ʼƵ�ײۣ�slotIndex[][1]:���ص�Ƶ�ײ�����
		for(int i=0;i<reqs[index].links;i++){
			for(int j=0;j<sub.nodes;j++)
				p[i][j] = -1;
			slotIndex[i][0] = slotIndex[i][1] = -1;
		}
				
		EOSubstrateNetwork subCopy = new EOSubstrateNetwork();
		Clone(subCopy,sub);
		int[] MD = new int[Parameters.MDSum];
		int[] MDLength = new int[Parameters.MDSum];
		GetMDAndMDLength(MD,MDLength);//�õ�����ģʽ�е�md�Լ�����
		
		//�õ��ź����vlinks
		int[] vLinkSet = new int[reqs[index].links];
		if(Parameters.CurrentVONEMethod == Parameters.MapVONELin_SortByNodeDegree){
			SortVLinksByAlg1(reqs,index,vLinkSet);
		} else if(Parameters.CurrentVONEMethod == Parameters.MapVONELin_SortByNodeDegreeAndBW){
			SortVLinksByAlg2(reqs,index,vLinkSet);
		} else if(Parameters.CurrentVONEMethod == Parameters.MapVONELin_SortByBW){
			SortVLinksByAlg3(reqs,index,vLinkSet);
		}
		//Alg1\Alg2\Alg3
		int g = 0;
		boolean findShPath = true;
		WeightedDirectedGraph auxGraph = new WeightedDirectedGraph(2*sub.nodes + 2);
		
		int[] ret = new int[2];
		int slotsNum = -1;//�����Ƶ�ײ�����
		int a=0;
		boolean findPath = false;
		int[] nodeEmbed = new int[2];//�ڵ�ӳ��Ľ��
		for(int ii=0;ii<reqs[index].links;ii++){
			int i = vLinkSet[ii];
			for(int md=Parameters.MDSum-1;md>0;md--){
				g = CalculateSlots(reqs[index].link[i].bw,MD[md],Parameters.GuardBand);
				
				for(a=0;a<Parameters.MaxSlots-g;) {
					findPath = false;
					auxGraph = BuildAuxGraphByAlg5(subCopy,reqs,index,i,a,a+g-1,MD[md],vLinkEmbed,p,vNodeEmbed,ret,MDLength[md],sNodeEmbed);//��������ͼ
					if(ret[0] == -2){//˵�����Ȳ��������
						break;
					} else if(ret[0] == -1){//˵��û�й���ɹ�����ͼ
						a++;
						continue;
					}
					auxGraph.findShortestPath(2*subCopy.nodes);//from�ڵ㼴��2*sub.nodes
					//if(auxGraph.shortestPath[2*subCopy.nodes].distance == auxGraph.INFINITY && auxGraph.shortestPath[2*subCopy.nodes+1].distance == auxGraph.INFINITY){
					if(auxGraph.shortestPath[2*subCopy.nodes+1].distance == auxGraph.INFINITY){
						a++;
						//System.out.println("No find path."+auxGraph.shortestPath[2*subCopy.nodes].distance);
						continue;//˵��û���ҵ�·��
					} else {
						findPath = true;
					}
					
					if(Parameters.DebugModel) auxGraph.displayPaths();//displayPaths();
					if(!auxGraph.GetPath(p[i],auxGraph)){
						findPath = false;
					}
					GetNodesEmbed(subCopy,auxGraph,nodeEmbed);//�õ��ڵ�ӳ����
					if(nodeEmbed[0] == nodeEmbed[1]){//from�ڵ㼴��2*sub.nodes,to�ڵ㼴��2*sub.nodes+1
						DeleteHigherCostLink(auxGraph,subCopy.nodes*2,subCopy.nodes*2+1,nodeEmbed[0]);//ɾ���ϸߵĴ�����·
						a++;
						continue;//line 10
					}
					if(Parameters.DebugModel) PrintPath(p[i],nodeEmbed[0],nodeEmbed[1]);
					slotsNum = g;//CalculateSlots(reqs[index].link[i].bw,MD[md],Parameters.GuardBand);
					//int nodeRet = IfSameSSnode(subCopy,subCopy.nodes*2,subCopy.nodes*2+1,auxGraph);
					
					//if(nodeRet >= 0){//from�ڵ㼴��2*sub.nodes,to�ڵ㼴��2*sub.nodes+1
					
					if(PathFitConstraintOfModu(subCopy,p[i],nodeEmbed[0],nodeEmbed[1],MDLength[md])){
						//AddPathToSet(sub,auxGraph,embedSet,i);
						//a++;
						//continue;
						break;
					}
					a++;
				}
				if(findPath && a<Parameters.MaxSlots-g){
					int vNodeFrom = reqs[index].link[i].from;
					int vNodeTo = reqs[index].link[i].to;
					
					GetNodesEmbed(subCopy,auxGraph,nodeEmbed);//�õ��ڵ�ӳ����
					//���½ڵ�ӳ��
					vNodeEmbed[vNodeFrom] = nodeEmbed[0];//����ڵ�ӳ�䵽����ڵ�
					vNodeEmbed[vNodeTo] = nodeEmbed[1];//����ڵ�ӳ�䵽����ڵ�
					//����cpu
					if(sNodeEmbed[nodeEmbed[0]] == -1){
						UpdateSub(subCopy,nodeEmbed[0],reqs[index].cpu[vNodeFrom]);
					}
					if(sNodeEmbed[nodeEmbed[1]] == -1){
						UpdateSub(subCopy,nodeEmbed[1],reqs[index].cpu[vNodeTo]);
					}
					sNodeEmbed[nodeEmbed[0]] = vNodeFrom;
					sNodeEmbed[nodeEmbed[1]] = vNodeTo;
					//������·ӳ��·������auxGraph��p
					//��·�Ѿ�����
					vLinkEmbed[i] = 1;
					//���µײ�����subCopy
					//UpdateSub(EOSubstrateNetwork sub,int sNode1,int sNode2,int ret[],int p[])
					int sNode1,sNode2;
					sNode1 = vNodeEmbed[reqs[index].link[i].from];
					sNode2 = vNodeEmbed[reqs[index].link[i].to];
					slotIndex[i][0] = a;//��ʼƵ�ײ�
					slotIndex[i][1] = a+slotsNum-1;//slotsNumΪ�����slot����
					if(Parameters.RecordLogModel){
						int aa=a+slotsNum-1;
						String str = "allocation req "+index+" req.link["+i+"]:["+a+"-"+aa+"]\r\n";
						WriteFilePlus("process.txt",str);
					}
					UpdateSub(subCopy,sNode2,sNode1,slotIndex[i],p[i]);
					
					
					break;
				}
			}
			if(!findPath || a>=Parameters.MaxSlots-g){//���û���ҵ�
				return -1;
			}
		}
		
		//���������·û��ӳ�䣬��ʧ�ܷ���
		for(int i=0;i<reqs[index].links;i++){
			if(vLinkEmbed[i] == -1) return -1;//ʧ�ܷ���
		}
		//����cpu
		UpdateSub(sub,subCopy);
		//��¼�ڵ����·ӳ����
		
		AddNodesMap(reqs,index,vNodeEmbed);//����s2v_n��v2s
		AddLinksMapBySPFA(sub,reqs,index,slotIndex,p);//���µײ�����
		//AddLinksMapBySPFA(sub,reqs,index,retOther,p);//���µײ�����
		if(Parameters.RecordLogModel){
			String str = "allocation req "+index+" is embedded successfully.";
			WriteFilePlus("process.txt",str);
			WriteFileOfGraph(sub,"process.txt",true);
		}
		UpdateSubSlots(sub,subCopy);
		return 0;
		
	}
	
	/******************************************************************
	���ƣ�int MapVONEByLinChenMaxNodeCost(......)
	���ܣ��Խڵ����ˮƽ��󻯷���ڵ�
	������
		      s2v_nΪ����ڵ�ӳ�������ڵ����ݽṹ
		      s2v_lΪ������·ӳ��������·���ݽṹ
		      v2sΪ����ӳ��������������ݽṹ 
		      indexΪ��index����������
	,int ret[],int p[][],ArrayList<Object> list
	����ֵ��0���ɹ����أ�-1��ʧ�ܷ��� 
	******************************************************************/
	private int MapVONEByLinChenMaxNodeCost(EOSubstrateNetwork sub,VONRequest reqs[],int index)
	{
		//��ʼ������,-1������δ���䣬>-1�����Ѿ�����Ľڵ������·��
		int[] vNodeEmbed = new int[reqs[index].nodes];
		int[] sNodeEmbed = new int[sub.nodes];
		int[] vLinkEmbed = new int[reqs[index].links];
		
		for(int i=0;i<reqs[index].nodes;i++){
			vNodeEmbed[i] = -1;
		}
		for(int i=0;i<sub.nodes;i++){
			sNodeEmbed[i] = -1;
		}
		for(int i=0;i<reqs[index].links;i++){
			vLinkEmbed[i] = -1;
		}
		
		//�������Ԫ�ط�ӳ��ڵ㣨�ڵ�ķ�������������󻯣�
		double[][] transModel = new double[reqs[index].nodes][sub.nodes];
		
		//InitTranModel(sub,reqs,index,transModel,indexModel,linkModel);
		InitMaxiNodeCostTranModel(subStatic,reqs,index,transModel);
		
		//��ʼ������,-1������δ���䣬>-1�����Ѿ�����Ľڵ������·��
		InitAllocModel(sub,reqs,index,vNodeEmbed,sNodeEmbed,vLinkEmbed);
				
		//�������Ԫ�ط�ӳ��ڵ㣨�ڵ�ķ�������������󻯣�
		//int retVNE = AllocNodes(sub,reqs,index,vNodeEmbed,sNodeEmbed,transModel);
		AllocNodesMinEleILP(sub,reqs,index,transModel);
		if(!FindOptSoluByNEmCXH(sub,reqs,index,vNodeEmbed,sNodeEmbed)) return -1;
		//int retVNE = AllocNodesMinEleLP(sub,reqs,index,vNodeEmbed,sNodeEmbed,transModel);
		//if(retVNE == -1) return -1;//ʧ�ܷ���
		
		//p[][]:��¼·����slotIndex[][]:slotIndex[][0]:���ص���ʼƵ�ײۣ�slotIndex[][1]:���ص�Ƶ�ײ�����
		int p[][] = new int[reqs[index].links][sub.nodes];
		int slotIndex[][] = new int[reqs[index].links][2];//slotIndex[][0]:���ص���ʼƵ�ײۣ�slotIndex[][1]:���ص�Ƶ�ײ�����
		for(int i=0;i<reqs[index].links;i++){
			for(int j=0;j<sub.nodes;j++)
				p[i][j] = -1;
			slotIndex[i][0] = slotIndex[i][1] = -1;
		}
				
		EOSubstrateNetwork subCopy = new EOSubstrateNetwork();
		Clone(subCopy,sub);
		
		
		
		int[] MD = new int[Parameters.MDSum];
		int[] MDLength = new int[Parameters.MDSum];
		GetMDAndMDLength(MD,MDLength);//�õ�����ģʽ�е�md�Լ�����
		
		//�õ��ź����vlinks
		/*
		int[] vLinkSet = new int[reqs[index].links];
		if(Parameters.CurrentVONEMethod == Parameters.MapVONELin_SortByNodeDegree){
			SortVLinksByAlg1(reqs,index,vLinkSet);
		} else if(Parameters.CurrentVONEMethod == Parameters.MapVONELin_SortByNodeDegreeAndBW){
			SortVLinksByAlg2(reqs,index,vLinkSet);
		} else if(Parameters.CurrentVONEMethod == Parameters.MapVONELin_SortByBW){
			SortVLinksByAlg3(reqs,index,vLinkSet);
		} else {
			SortVLinksByAlg3(reqs,index,vLinkSet);
		}*/
		//Alg1\Alg2\Alg3
		int g = 0;
		boolean findShPath = true;
		WeightedDirectedGraph auxGraph = new WeightedDirectedGraph(sub.nodes + 2);
		
		int[] ret = new int[2];
		int slotsNum = -1;//�����Ƶ�ײ�����
		int a=0;
		boolean findPath = false;
		int[] nodeEmbed = new int[2];//�ڵ�ӳ��Ľ��
		for(int ii=0;ii<reqs[index].links;ii++){
			int i = ii;//vLinkSet[ii];
			//nodeEmbed[0] = 
			for(int md=Parameters.MDSum-1;md>0;md--){
				g = CalculateSlots(reqs[index].link[i].bw,MD[md],Parameters.GuardBand);
				
				for(a=0;a<Parameters.MaxSlots-g;) {
					findPath = false;
					//auxGraph = BuildAuxGraphByAlg5(subCopy,reqs,index,i,a,a+g-1,MD[md],vLinkEmbed,p,vNodeEmbed,ret,MDLength[md],sNodeEmbed);//��������ͼ
					auxGraph = BuildAuxGraphByAlg5CXH(subCopy,reqs,index,i,a,a+g-1,MD[md],vLinkEmbed,p,vNodeEmbed,ret,MDLength[md],sNodeEmbed);//��������ͼ
					if(ret[0] == -2){//˵�����Ȳ��������
						break;
					} else if(ret[0] == -1){//˵��û�й���ɹ�����ͼ
						a++;
						continue;
					}
					auxGraph.findShortestPath(subCopy.nodes);//from�ڵ㼴��2*sub.nodes
					//if(auxGraph.shortestPath[2*subCopy.nodes].distance == auxGraph.INFINITY && auxGraph.shortestPath[2*subCopy.nodes+1].distance == auxGraph.INFINITY){
					if(auxGraph.shortestPath[subCopy.nodes+1].distance == auxGraph.INFINITY){
						a++;
						//System.out.println("No find path."+auxGraph.shortestPath[2*subCopy.nodes].distance);
						continue;//˵��û���ҵ�·��
					} else {
						findPath = true;
					}
					
					if(Parameters.DebugModel) auxGraph.displayPaths();//displayPaths();
					if(!auxGraph.GetPathCXH(p[i],auxGraph)){
						findPath = false;
						a++;
						continue;
					}
					nodeEmbed[0] = vNodeEmbed[reqs[index].link[i].to];
					nodeEmbed[1] = vNodeEmbed[reqs[index].link[i].from];
					//GetNodesEmbed(subCopy,auxGraph,nodeEmbed);//�õ��ڵ�ӳ����
					//if(Parameters.DebugModel) PrintPath(p[i],nodeEmbed[0],nodeEmbed[1]);
					slotsNum = g;//CalculateSlots(reqs[index].link[i].bw,MD[md],Parameters.GuardBand);
					if(PathFitConstraintOfModu(subCopy,p[i],nodeEmbed[0],nodeEmbed[1],MDLength[md])){
						//AddPathToSet(sub,auxGraph,embedSet,i);
						break;
					}
					a++;
				}
				if(findPath && a<Parameters.MaxSlots-g){
					int vNodeFrom = reqs[index].link[i].from;
					int vNodeTo = reqs[index].link[i].to;
					
					sNodeEmbed[nodeEmbed[0]] = vNodeFrom;
					sNodeEmbed[nodeEmbed[1]] = vNodeTo;
					//������·ӳ��·������auxGraph��p
					//��·�Ѿ�����
					vLinkEmbed[i] = 1;
					//���µײ�����subCopy
					//UpdateSub(EOSubstrateNetwork sub,int sNode1,int sNode2,int ret[],int p[])
					int sNode1,sNode2;
					sNode1 = vNodeEmbed[reqs[index].link[i].from];
					sNode2 = vNodeEmbed[reqs[index].link[i].to];
					slotIndex[i][0] = a;//��ʼƵ�ײ�
					slotIndex[i][1] = a+slotsNum-1;//slotsNumΪ�����slot����
					if(Parameters.RecordLogModel){
						int aa=a+slotsNum-1;
						String str = "allocation req "+index+" req.link["+i+"]:["+a+"-"+aa+"]\r\n";
						WriteFilePlus("process.txt",str);
					}
					UpdateSub(subCopy,sNode2,sNode1,slotIndex[i],p[i]);
					
					
					break;
				}
			}
			if(!findPath || a>=Parameters.MaxSlots-g){//���û���ҵ�
				return -1;
			}
		}
		
		//���������·û��ӳ�䣬��ʧ�ܷ���
		for(int i=0;i<reqs[index].links;i++){
			if(vLinkEmbed[i] == -1) return -1;//ʧ�ܷ���
		}
		//����CPU
		for(int i=0;i<reqs[index].nodes;i++){
			UpdateSub(subCopy,vNodeEmbed[i],reqs[index].cpu[i]);
		}
		
		//����cpu
		UpdateSub(sub,subCopy);
		
		//��¼�ڵ����·ӳ����
		
		AddNodesMap(reqs,index,vNodeEmbed);//����s2v_n��v2s
		AddLinksMapBySPFA(sub,reqs,index,slotIndex,p);//���µײ�����
		//AddLinksMapBySPFA(sub,reqs,index,retOther,p);//���µײ�����
		if(Parameters.RecordLogModel){
			String str = "allocation req "+index+" is embedded successfully.";
			WriteFilePlus("process.txt",str);
			WriteFileOfGraph(sub,"process.txt",true);
		}
		UpdateSubSlots(sub,subCopy);
		return 0;
		
	}
	private void InitMaxiNodeCostTranModel(EOSubstrateNetwork sub,VONRequest reqs[],int index,double[][] transModel)
	{
		//��������ģ�ͺ���С��������·��
		int slotNum = -1;
		int link[] = new int[1];
		for(int i=0;i<reqs[index].nodes;i++){
			for(int j=0;j<sub.nodes;j++){
				if(reqs[index].cpu[i] <= s2v_n[j].rest_cpu){//�ײ�ڵ��CPU��������ڵ� + Parameters.MIN_VALUE_DOUBLE
					//transModel[i][j] = (s2v_n[j].rest_cpu-reqs[index].cpu[i]+0.0001)/subStatic.cpu[j];//cxh����+0.0001);//s2v_n[j].rest_cpu;//(s2v_n[j].rest_cpu-reqs[index].cpu[i])/s2v_n[j].rest_cpu;///sub.cpu[j];
					//transModel[i][j] = subStatic.cpu[j]/(s2v_n[j].rest_cpu-reqs[index].cpu[i]+0.0001);//cxh����+0.0001);//s2v_n[j].rest_cpu;//(s2v_n[j].rest_cpu-reqs[index].cpu[i])/s2v_n[j].rest_cpu;///sub.cpu[j];
					//transModel[i][j] = (s2v_n[j].rest_cpu-reqs[index].cpu[i]+0.0001);//ʣ��cpu��ȥ�����cpu�������//+0.0001);//s2v_n[j].rest_cpu;//(s2v_n[j].rest_cpu-reqs[index].cpu[i])/s2v_n[j].rest_cpu;///sub.cpu[j];
					transModel[i][j] = 1/(s2v_n[j].rest_cpu-reqs[index].cpu[i]+0.0001);//ʣ��cpu��ȥ�����cpu�������//+0.0001);//s2v_n[j].rest_cpu;//(s2v_n[j].rest_cpu-reqs[index].cpu[i])/s2v_n[j].rest_cpu;///sub.cpu[j];
					//transModel[i][j] = (s2v_n[j].rest_cpu-reqs[index].cpu[i]+0.0001)/s2v_n[j].rest_cpu;//cxh����+0.0001);//s2v_n[j].rest_cpu;//(s2v_n[j].rest_cpu-reqs[index].cpu[i])/s2v_n[j].rest_cpu;///sub.cpu[j];
					//transModel[i][j] = (reqs[index].cpu[i])/subStatic.cpu[j];//cxh����+0.0001);//s2v_n[j].rest_cpu;//(s2v_n[j].rest_cpu-reqs[index].cpu[i])/s2v_n[j].rest_cpu;///sub.cpu[j];
					//transModel[i][j] = (reqs[index].cpu[i])/s2v_n[j].rest_cpu;//cxh����+0.0001);//s2v_n[j].rest_cpu;//(s2v_n[j].rest_cpu-reqs[index].cpu[i])/s2v_n[j].rest_cpu;///sub.cpu[j];
					//if(transModel[i][j] < 0) transModel[i][j] = -1*transModel[i][j];
				} else {
					transModel[i][j] = -1;//-1������ӳ��
				}
			}
		}
	}
	
	/*���ƣ�GetNodesEmbed
	 * ���ܣ��õ��ڵ�ӳ��Ľ��
	 */
	void GetNodesEmbed(EOSubstrateNetwork sub,WeightedDirectedGraph myGraph,int[] nodeEmbed)
	{
		if(myGraph.shortestPath[2*sub.nodes].parentVert == 2*sub.nodes){
			nodeEmbed[1] = myGraph.shortestPath[2*sub.nodes+1].parentVert;//fromӳ��
			int node = nodeEmbed[1];
			while(myGraph.shortestPath[node].parentVert != 2*sub.nodes){
				node = myGraph.shortestPath[node].parentVert;
			}
			//if(node > sub.nodes) node -= sub.nodes;
			nodeEmbed[0] = node;
  		} else if(myGraph.shortestPath[2*sub.nodes+1].parentVert == 2*sub.nodes+1){
  			nodeEmbed[0] = myGraph.shortestPath[2*sub.nodes].parentVert;//fromӳ��
			int node = nodeEmbed[0];
			while(myGraph.shortestPath[node].parentVert != 2*sub.nodes){
				node = myGraph.shortestPath[node].parentVert;
			}
			//if(node > sub.nodes) node -= sub.nodes;
			nodeEmbed[1] = node;
  		} else {
  			System.out.println("error1 in GetNodesEmbed*********");
  			if(Parameters.ErrorRecord){
  				String str = "GetNodesEmbed();\r\n";
				WriteFilePlus("error.txt",str);
  			}
  		}
  		if(nodeEmbed[0]>sub.nodes-1) nodeEmbed[0] -= sub.nodes;
  		if(nodeEmbed[1]>sub.nodes-1) nodeEmbed[1] -= sub.nodes;
	}
	/******************************************************************
  	*���ƣ�AddPathToSet(......)
  	*���ܣ��Ѹ���·���뵽S������
  	*������myGraph������ͼ��sNode1:�ڵ�1��sNode2���ڵ�2��sNode���ڵ�
  	*embedSet:��һ��Hashtable��key��������·��ţ�value��·������һ���ڵ���ɣ�
  	*     ����(sNode1,sNode2,length1)->(sNode2,sNode3,length2)...����LinkedList<Arc>
  	*����ֵ��
  	*�����ˣ�chen xh
  	*����ʱ�䣺2019/07/12
  	*�����ˣ�chen xh
  	*����ʱ�䣺2019/07/12
  	*******************************************************************/
  	public void AddPathToSet(EOSubstrateNetwork sub,WeightedDirectedGraph myGraph,Hashtable embedSet,int vLink)
  	{
  		int startNode = -1,endNode = -1;
  		int[] twoNodes = new int[2];
  		getStartAndEndNodesInPath(myGraph,2*sub.nodes,2*sub.nodes+1,twoNodes);
  		startNode = twoNodes[1];
  		endNode = twoNodes[0];
  		
  		//Ѱ��startNode->...->endNode���м�ڵ�
  		int node1 = myGraph.shortestPath[endNode].parentVert;//��endNodeӳ�䵽node1
  		int node2 = -1;
  		int sfNode1,sfNode2;//����ڵ�1������ڵ�2
  		sfNode1=-1;
  		sfNode2=-1;
  				
  		if(node1 > sub.nodes-1) sfNode1 = node1-sub.nodes;
  		else sfNode1 = node1;
  		double length = 0;
  		LinkedList<Arc> myLinkedList = new LinkedList<Arc>();
  		while(myGraph.shortestPath[node1].parentVert != startNode){
  			node1 = myGraph.shortestPath[node1].parentVert;
  			
  			if(node1 > sub.nodes-1) sfNode2 = node1-sub.nodes;
  			else sfNode2 = node1;
  			
  			Arc myArc = new Arc();
  			myArc.node1 = sfNode1;
  			myArc.node2 = sfNode2;
  			myLinkedList.add(myArc);
  			//length += GetLength(sub,sfNode1,sfNode2);
  			sfNode1 = sfNode2;
  		}
  		embedSet.put(vLink, myLinkedList);
  	}
	
	
	/******************************************************************
  	*���ƣ�DeleteHigherCostLink(......)
  	*���ܣ�ɾ�����۽ϸߵ���·
  	*������myGraph������ͼ��sNode1:�ڵ�1��sNode2���ڵ�2��sNode���ڵ�
  	*����ֵ��
  	*�����ˣ�chen xh
  	*����ʱ�䣺2019/07/12
  	*�����ˣ�chen xh
  	*����ʱ�䣺2019/07/12
  	*******************************************************************/
  	public void DeleteHigherCostLink(WeightedDirectedGraph myGraph,int sNode1,int sNode2,int sNode)
  	{
  		if(sNode < (myGraph.nVerts/2-1)){
  			if(myGraph.adjMat[sNode1][sNode] > myGraph.adjMat[sNode2][sNode]){
  	  			myGraph.adjMat[sNode1][sNode] = myGraph.INFINITY;
  	  			myGraph.adjMat[sNode][sNode1] = myGraph.INFINITY;
  	  			
  	  			myGraph.adjMat[sNode1][sNode+(myGraph.nVerts/2-1)] = myGraph.INFINITY;
	  			myGraph.adjMat[sNode+(myGraph.nVerts/2-1)][sNode1] = myGraph.INFINITY;
  	  		} else {
  	  			myGraph.adjMat[sNode2][sNode] = myGraph.INFINITY;
  	  			myGraph.adjMat[sNode][sNode2] = myGraph.INFINITY;
  	  			
  	  			myGraph.adjMat[sNode2][sNode+(myGraph.nVerts/2-1)] = myGraph.INFINITY;
  	  			myGraph.adjMat[sNode+(myGraph.nVerts/2-1)][sNode2] = myGraph.INFINITY;
  	  		}
  		} else {
  			if(myGraph.adjMat[sNode1][sNode] > myGraph.adjMat[sNode2][sNode]){
  	  			myGraph.adjMat[sNode1][sNode] = myGraph.INFINITY;
  	  			myGraph.adjMat[sNode][sNode1] = myGraph.INFINITY;
  	  			
  	  			myGraph.adjMat[sNode1][sNode-(myGraph.nVerts/2-1)] = myGraph.INFINITY;
	  			myGraph.adjMat[sNode-(myGraph.nVerts/2-1)][sNode1] = myGraph.INFINITY;
  	  		} else {
  	  			myGraph.adjMat[sNode2][sNode] = myGraph.INFINITY;
  	  			myGraph.adjMat[sNode][sNode2] = myGraph.INFINITY;
  	  			
  	  			myGraph.adjMat[sNode2][sNode-(myGraph.nVerts/2-1)] = myGraph.INFINITY;
  	  			myGraph.adjMat[sNode-(myGraph.nVerts/2-1)][sNode2] = myGraph.INFINITY;
  	  		}
  		}
  	}
	/*
	 * ���ܣ��õ�·���������˵㣬�����յ�
	 */
  	public void getStartAndEndNodesInPath(WeightedDirectedGraph myGraph,int sNode1,int sNode2,int[] node)
  	{
  		int startNode = -1,endNode = -1;
  		if(myGraph.shortestPath[sNode1].distance == myGraph.INFINITY){
  			startNode = sNode1;//��sNode1->...->sNode2
  			endNode = sNode2;
  		} if(myGraph.shortestPath[sNode1].distance == myGraph.INFINITY){
  			startNode = sNode2;//��sNode2->...->sNode1
  			endNode = sNode1;
  		} else {
  			System.out.println("error1 in IfSameSSnode*********");
  			if(Parameters.ErrorRecord){
  				String str = "IfSameSSnode();\r\n";
				WriteFilePlus("error.txt",str);
  			}
  		}
  		node[0] = startNode;
  		node[1] = endNode;
  	}
  	/*���ƣ�PathFitConstraintOfModu()
  	 * ���ܣ�ͨ��path���ж�·���Ƿ��������Ҫ��
  	 */
  	public boolean PathFitConstraintOfModu(EOSubstrateNetwork sub,int[] p,int sNode2,int sNode1,double maxLength)
  	{
  		double length = 0;
  		if(p[sNode2] != -1){
			while(p[sNode2] != -1) {
				length += GetLength(sub,sNode2,p[sNode2]);
				sNode2 = p[sNode2];
			}
		} else if(p[sNode1] != -1){
			while(p[sNode1] != -1) {
				length += GetLength(sub,sNode1,p[sNode1]);
				sNode1 = p[sNode1];
			}
		} else {
			System.out.println("PathFitConstraintOfModu(): error. There is not a path.");
			return false;
		}
  		if(length > maxLength) return false;
  		else return true;
  	}
	/******************************************************************
  	*���ƣ�PathFitConstraintOfModu(......)
  	*���ܣ�ͨ��path���ж�·���Ƿ��������Ҫ��ʾ����
  	*��������ڵ���5����������·�������ڵ�ֱ���0��1������ͼ��5*2+2=12���ڵ㡣
  	*path[0..11]����ǰ����Ϊpath[10]=2;path[2]=8;path[8]=11;path[11]=-1(��ʾ���������ӵĽ��);
  	*��ô����һ��·��p:10->2->8->11��
  	*��Ӹ�·��p�У��ҵ�10ӳ�䵽2��11ӳ�䵽8��
  	*Ȼ��7-sub.nodes=8-5=3��˵��8�������������еĽڵ�3������ڵ�11ӳ�䵽2.
  	*�������·(2,3)�ĳ��ȣ��ж��Ƿ�������Ƶ�Ҫ��������㷵��true�����򷵻�false��
  	*������sub���������磻sNode1:����չͼ�еĽڵ㣻sNode2������չͼ�еĽڵ㣻path��·����md������ģʽ
  	*����ֵ��true:·�������������Ҫ��false�����������Ҫ��
  	*�����ˣ�chenxiaoh
  	*����ʱ�䣺2019/07/01
  	*
  	*�����ˣ�
  	*����ʱ�䣺�����������滻��ȡ�¸ú�������;
  	*******************************************************************/
  	public boolean PathFitConstraintOfModu(EOSubstrateNetwork sub,WeightedDirectedGraph myGraph,int sNode1,int sNode2,double maxLength)
  	{
  		int startNode = -1,endNode = -1;
  		int[] twoNodes = new int[2];
  		getStartAndEndNodesInPath(myGraph,2*sub.nodes,2*sub.nodes+1,twoNodes);
  		startNode = twoNodes[1];
  		endNode = twoNodes[0];
  		
  		//Ѱ��startNode->...->endNode���м�ڵ�
  		int node1 = myGraph.shortestPath[endNode].parentVert;//��endNodeӳ�䵽node1
  		//int node2 = -1;
  		int sfNode1,sfNode2;//����ڵ�1������ڵ�2
  		sfNode1=-1;
  		sfNode2=-1;
  				
  		if(node1 > sub.nodes-1) sfNode1 = node1-sub.nodes;
  		else sfNode1 = node1;
  		double length = 0;
  		if(myGraph.shortestPath[node1].parentVert != -1){
  			while(myGraph.shortestPath[node1].parentVert != startNode){
  	  			node1 = myGraph.shortestPath[node1].parentVert;
  	  			
  	  			if(node1 > sub.nodes-1) sfNode2 = node1-sub.nodes;
  	  			else sfNode2 = node1;
  	  			
  	  			length += GetLength(sub,sfNode1,sfNode2);
  	  			
  	  			sfNode1 = sfNode2;
  	  		}
  		} 
  		if(length > maxLength) return false;
  		else return true;
  	}
	/******************************************************************
  	*���ƣ�CreateAuxiliaryDiagram(......)
  	*���ܣ�ͨ��path���ж�������·�Ƿ�ӳ����ͬһ������ڵ��ϣ�ʾ����
  	*��������ڵ���5����������·�������ڵ�ֱ���0��1������ͼ��5*2+2=12���ڵ㡣
  	*path[0..11]����ǰ����Ϊpath[10]=2;path[2]=7;path[7]=11;path[11]=-1(��ʾ���������ӵĽ��);
  	*��ô����һ��·��p:10->2->7->11��
  	*��Ӹ�·��p�У��ҵ�10ӳ�䵽2��11ӳ�䵽7��
  	*Ȼ��7-sub.nodes=7-5=2��˵��7�������������еĽڵ�2������ڵ�11ӳ�䵽2.
  	*����10��11��ӳ�䵽ͬһ������ڵ�2���򷵻�false
  	*������sub���������磻sNode1:����չͼ�еĽڵ㣻sNode2������չͼ�еĽڵ㣻path��·����
  	*����ֵ��int:>=0����ͬһ������ڵ㣻<0������ͬһ������ڵ��ϡ�
  	*�����ˣ�chen xh
  	*����ʱ�䣺2019/07/01
  	*�����ˣ�chen xh
  	*����ʱ�䣺2019/07/12
  	*******************************************************************/
  	public int IfSameSSnode(EOSubstrateNetwork sub,int sNode1,int sNode2,WeightedDirectedGraph myGraph)
  	{
  		int startNode = -1,endNode = -1;
  		int[] twoNodes = new int[2];
  		getStartAndEndNodesInPath(myGraph,2*sub.nodes,2*sub.nodes+1,twoNodes);
  		startNode = twoNodes[1];
  		endNode = twoNodes[0];
  		
  		//Ѱ��startNode->...->endNode���м�ڵ�
  		int node1 = myGraph.shortestPath[endNode].parentVert;//��endNodeӳ�䵽node1
  		int node2 = -1;
  		while(myGraph.shortestPath[endNode].parentVert != startNode){
  			endNode = myGraph.shortestPath[endNode].parentVert;
  			node2 = endNode;
  		}
  		if(node2 == -1) {
  			System.out.println("error in IfSameSSnode*********");
  			if(Parameters.ErrorRecord){
  				String str = "error IfSameSSnode();\r\n";
				WriteFilePlus("error.txt",str);
  			}
  		}
  		if(node1 == node2) return node1;
  		else return -1;
  	}
	
	/******************************************************************
  	*���ƣ�CreateAuxiliaryDiagram(......)
  	*���ܣ���������ͼ
  	*������sub:�ײ����磻reqs�������������󼯺ϣ�index����index���������磻
  	*     vLinkNum��������·��ţ�a1Slot��a2Slot����Ҫ�����Ƶ�ײ�[a1Slot,a2Slot]
  	*     modu������ģʽ;
  	*     embedSet:��һ��Hashtable��key��������·��ţ�value��·������һ���ڵ���ɣ�
  	*     ����(sNode1,sNode2,length1)->(sNode2,sNode3,length2)...����LinkedList<Arc>
  	*     vNodeEmbed:����ڵ�ӳ������
  	*     ����vNodeEmbed[0] = 2��ʾ����ڵ�0ӳ�䵽����ڵ�2�ϣ�vNodeEmbed[0] = -1����ʾ����ڵ�0��δӳ�䡣
  	*����ֵ��WeightedDirectedGraph;ret[0]:-2�����Ȳ����㵱ǰ���ƣ�-1��Ƶ�ײ۲�����Ҫ��
  	*�����ˣ�Xiaohua Chen
  	*����ʱ�䣺2019/06/29
  	*******************************************************************/
  	public WeightedDirectedGraph BuildAuxGraphByAlg5(EOSubstrateNetwork sub,VONRequest reqs[],int index,int vLinkNum,int a1Slot,int a2Slot,int modu,int[] vLinkEmbed,int[][] p,int[] vNodeEmbed,int[] ret,int mdLength,int[] sNodeEmbed)
  	{
  		//����ڵ������
  		int nodeNum = 0;
  		int s = -1;
		int d = -1; 
		int spi = -1;
		int dpi = -1;
		double cost = 0;
			
  		nodeNum = 2*sub.nodes + 2;//sub.nodes������ڵ㣻2*sub.nodes�������ظ��ڵ㣬+2��ʾ������·�������˵�
  		//��������ͼ
  		WeightedDirectedGraph myAuxGraph = new WeightedDirectedGraph(nodeNum);
  		
  		//�����������д�����·
  		boolean find = false;
  		boolean findMD = true;
  		for(int i=0;i<sub.links;i++){
  			if(CheckEnouSlots(sub,i,a1Slot,a2Slot)){//�����·�Ƿ�����slotҪ��
  				findMD = true;
  				if(!CheckMDAndLength(sub,i,mdLength)){
  					findMD = false;
  					continue;
  					//ret[0] = -2;//��ǰmd������length��Ҫ��û���κε���·���뵽����ͼ�У�ʧ�ܷ���
  		  			//return myAuxGraph;
  				}
  				find = true;
  				s = sub.link[i].from;
  				d = sub.link[i].to; 
  				spi = s + sub.nodes;
  				dpi = d + sub.nodes;
  				cost = (Parameters.LinkCost * sub.link[i].length) * sub.link[i].length;
  				myAuxGraph.addEdge(spi,dpi,cost);
  				myAuxGraph.addEdge(dpi,spi,cost);
  				
  				myAuxGraph.addEdge(s,dpi,cost);
  				myAuxGraph.addEdge(dpi,s,cost);//??

  				myAuxGraph.addEdge(d,spi,cost);
  				myAuxGraph.addEdge(spi,d,cost);//??
  			}
  		}
  		if(!find) {
  			if(!findMD){
  	  			ret[0] = -2;//��ǰmd������length��Ҫ��û���κε���·���뵽����ͼ�У�ʧ�ܷ���
  		  		return myAuxGraph;
  	  		}
  			ret[0] = -1;//��ǰmd������length��Ҫ��û���κε���·���뵽����ͼ�У�ʧ�ܷ���
  			return myAuxGraph;
  		}
  		//����������·��sub�ڵ����·
  		int vNode1 = reqs[index].link[vLinkNum].from;
  		int vNode2 = reqs[index].link[vLinkNum].to;
  		if(vNodeEmbed[vNode1] != -1) {
  			myAuxGraph.addEdge(2*sub.nodes,vNodeEmbed[vNode1],0);//vNode1->2*sub,����vNode1��ԭ��������ڵ�
  			myAuxGraph.addEdge(vNodeEmbed[vNode1],2*sub.nodes,0);
  		} else {
  			for(int i=0;i<sub.nodes;i++){//��������ڵ�vNode1������ڵ�֮��Ĵ���
  				//System.out.println("vNode1:"+vNode1);
  				if(sNodeEmbed[i]==-1 && sub.cpu[i] >= reqs[index].cpu[vNode1]){
  					double cost1 = Parameters.R*CalCostDinp(sub,reqs,index,vNode1,i,vLinkEmbed,vNodeEmbed);
  					double cost2 = 1.0/CalCostRin(sub,reqs,index,vNode1,i,vNodeEmbed,reqs[index].link[vLinkNum].bw,modu,Parameters.GuardBand,mdLength);
  					cost = cost1 + cost2;
  	  	  			myAuxGraph.addEdge(2*sub.nodes,i,cost);
  	  	  			myAuxGraph.addEdge(i,2*sub.nodes,cost);
  				}
  	  		}
  		}
  		if(vNodeEmbed[vNode2] != -1) {
  			myAuxGraph.addEdge(2*sub.nodes+1,vNodeEmbed[vNode2]+sub.nodes,0);//vNode2->2*sub+1������vNode2��ԭ���ĸ����ڵ�
  			myAuxGraph.addEdge(vNodeEmbed[vNode2]+sub.nodes,2*sub.nodes+1,0);
  		} else {
  			for(int i=0;i<sub.nodes;i++){//��������ڵ�vNode2������ڵ��Ӧ�ĸ����ڵ�֮��Ĵ���
  				if(sNodeEmbed[i]==-1 && sub.cpu[i] >= reqs[index].cpu[vNode2]){
  					double cost1 = Parameters.R*CalCostDinp(sub,reqs,index,vNode2,i+sub.nodes,vLinkEmbed,vNodeEmbed);
  					double cost2 = 1.0/CalCostRin(sub,reqs,index,vNode1,i,vNodeEmbed,reqs[index].link[vLinkNum].bw,modu,Parameters.GuardBand,mdLength);
  					cost = cost1 + cost2;
  	  	  			myAuxGraph.addEdge(2*sub.nodes+1,i+sub.nodes,cost);
  	  	  			myAuxGraph.addEdge(i+sub.nodes,2*sub.nodes+1,cost);
  				}
  	  		}
  		}
  		ret[0] = 1;//��ǰmd������length��Ҫ��û���κε���·���뵽����ͼ�У�ʧ�ܷ���
  		return myAuxGraph;
  	}
  	//�ڵ��Ѿ�ӳ�䣬���ݽڵ�ӳ��Ľ��������ͼ����ͼ������·��
  	public WeightedDirectedGraph BuildAuxGraphByAlg5CXH(EOSubstrateNetwork sub,VONRequest reqs[],int index,int vLinkNum,int a1Slot,int a2Slot,int modu,int[] vLinkEmbed,int[][] p,int[] vNodeEmbed,int[] ret,int mdLength,int[] sNodeEmbed)
  	{
  		//����ڵ������
  		int nodeNum = 0;
  		int s = -1;
		int d = -1; 
		int spi = -1;
		int dpi = -1;
		double cost = 0;
			
  		nodeNum = sub.nodes + 2;//sub.nodes������ڵ㣻2*sub.nodes�������ظ��ڵ㣬+2��ʾ������·�������˵�
  		//��������ͼ
  		WeightedDirectedGraph myAuxGraph = new WeightedDirectedGraph(nodeNum);
  		
  		//�����������д�����·
  		boolean find = false;
  		boolean findMD = true;
  		for(int i=0;i<sub.links;i++){
  			if(CheckEnouSlots(sub,i,a1Slot,a2Slot)){//�����·�Ƿ�����slotҪ��
  				findMD = true;
  				if(!CheckMDAndLength(sub,i,mdLength)){
  					findMD = false;
  					continue;
  					//ret[0] = -2;//��ǰmd������length��Ҫ��û���κε���·���뵽����ͼ�У�ʧ�ܷ���
  		  			//return myAuxGraph;
  				}
  				find = true;
  				s = sub.link[i].from;
  				d = sub.link[i].to; 
  				//spi = s + sub.nodes;
  				//dpi = d + sub.nodes;
  				//cost = (Parameters.LinkCost * sub.link[i].length) * sub.link[i].length;
  				myAuxGraph.addEdge(s,d,1);
  				myAuxGraph.addEdge(d,s,1);
  			}
  		}
  		if(!find) {
  			if(!findMD){
  	  			ret[0] = -2;//��ǰmd������length��Ҫ��û���κε���·���뵽����ͼ�У�ʧ�ܷ���
  		  		return myAuxGraph;
  	  		}
  			ret[0] = -1;//��ǰmd������length��Ҫ��û���κε���·���뵽����ͼ�У�ʧ�ܷ���
  			return myAuxGraph;
  		}
  		//����������·��sub�ڵ����·
  		int vNode1 = reqs[index].link[vLinkNum].from;
  		int vNode2 = reqs[index].link[vLinkNum].to;
  		if(vNodeEmbed[vNode1] != -1) {
  			myAuxGraph.addEdge(sub.nodes,vNodeEmbed[vNode1],1);//vNode1->2*sub,����vNode1��ԭ��������ڵ�
  			myAuxGraph.addEdge(vNodeEmbed[vNode1],sub.nodes,1);
  		} 
  		if(vNodeEmbed[vNode2] != -1) {
  			myAuxGraph.addEdge(sub.nodes+1,vNodeEmbed[vNode2],1);//vNode2->2*sub+1������vNode2��ԭ���ĸ����ڵ�
  			myAuxGraph.addEdge(vNodeEmbed[vNode2],sub.nodes+1,1);
  		} 
  		ret[0] = 1;//��ǰmd������length��Ҫ��û���κε���·���뵽����ͼ�У�ʧ�ܷ���
  		return myAuxGraph;
  	}
  	
  	
  	
  	/******************************************************************
  	*���ƣ�CalCostRin(......)
  	*���ܣ�����Dinp�Ĵ��ۣ�������ڵ�������ڵ�֮������Ӵ���
  	*������	  
  	*����ֵ����·����
  	*******************************************************************/
  	public double CalCostRin(EOSubstrateNetwork sub,VONRequest reqs[],int index,int iVNode,int nSNode,int[] vNodeEmbed,double bw,int md,int G,int mdLength)
  	{
  		double rin=0.0;
  	    int a=0,c=0; //a,����ڵ��Ӧ�Ŀ�����·��  ��   c��·������ ��Ƶ�ײ���
  	    double b=0.0,g;  //g �� Ҫ���Ƶ�ײ۷����� 
  	    boolean isfind;  //isfind �� ������Ƶ�ײ����Ƿ��ԴﵽҪ���Ƶ�ײ�����g��
  	    isfind=false;
  	    //g=(bw)/(double)md+G;
  	    int ae = 0;//��ǰ��md�������Ƶ�ײ�����
  	    g=CalculateSlots(bw,md,G);
  	    for(int i=0;i<sub.links;i++){
  	    	if(sub.link[i].from==nSNode || sub.link[i].to==nSNode) {
  	    		a++;//�ڵ�nSNode�Ķ�
  	    		for(int k=0;k<Parameters.MaxSlots;k++) {
  	    			b += sub.slots[i][k];
  	    		}
  	    		ae += CalculateSlots(bw,md,G,sub.link[i].length,mdLength);
  	    	}
  	    }
  	    if(a==0) return 0;
  	    rin=(double)(b+ae)/(double)a;
        return rin;
  	}
  	
  	/******************************************************************
  	*���ƣ�CalCostDinp(......)
  	*���ܣ�����Dinp�Ĵ��ۣ�������ڵ�������ڵ�֮������Ӵ���
  	*������	  
  	*����ֵ����·����
  	*******************************************************************/
  	public double CalCostDinp(EOSubstrateNetwork sub,VONRequest reqs[],int index,int iVNode,int nSNode,int[] vLinkEmbed,int[] vNodeEmbed)
  	{
  		//LinkedList<Arc> arcs = new LinkedList<Arc>(); //����arcs����
  		double distSum = 0;//�����������
  		double average;
  		int linkNo=-1;
  		double dist = 0;
  		boolean find = false;
  		int sSum = 0;
  		for(int i=0;i<vLinkEmbed.length;i++){
  			if(vLinkEmbed[i] == 1){//˵��������·�Ѿ�ӳ��
  				find = true;
  				sSum ++;
  				int vToSNode = vNodeEmbed[iVNode];
  				linkNo = GetLinkNum(sub,vToSNode,nSNode);//�õ�����ڵ���
  				if(linkNo >= 0) distSum += sub.link[linkNo].length;//���ڵ㳤��ȡ���ŵ�dist
  			}
  		}
  		if(!find) return 0;
  		return distSum/sSum;
  	}
  	
  	/******************************************************************
  	*���ƣ�CheckEnouSlots(......)
  	*���ܣ�����Ƿ����㹻��Ƶ�ײ�
  	*������	  
  	*����ֵ��True:�ɹ�;False:ʧ��
  	*******************************************************************/
  	public boolean CheckEnouSlots(EOSubstrateNetwork sub,int linkNum,int a1Slot,int a2Slot)
  	{
  		boolean find = true;
  		//���slot�Ƿ�����Ҫ��
  		for(int i=a1Slot;i<=a2Slot;i++){
  			if(sub.slots[linkNum][i]==0){
  				find = false;
  				break;
  			}
  		}
  		return find;
  	}
  	/******************************************************************
  	*���ƣ�CheckMDAndLength(......)
  	*���ܣ���鳤���Ƿ�����Ҫ��
  	*������	  
  	*����ֵ��True:�ɹ�;False:ʧ��
  	*******************************************************************/
  	public boolean CheckMDAndLength(EOSubstrateNetwork sub,int linkNum,int moduLength)
  	{
  		boolean find = true;
  		//���·���Ƿ����modu
  		if(sub.link[linkNum].length <= moduLength){
  			find = true;
  		} else {
  			find = false;
  		}
  		return find;
  	}
	/******************************************************************
	���ƣ�SortVLinksByAlg1(......)
	���ܣ���Alg1����������·��Sort virtual links by node degrees
	������
	����ֵ��
	******************************************************************/
	private void SortVLinksByAlg1(VONRequest reqs[],int index,int[] vSortLinks)
	{
		int[] vNodesDegree = new int[reqs[index].nodes];
		int[] vLinksDegreeByLargNode = new int[reqs[index].links];
		//int[] vSortLinksDegree = new int[reqs[index].links];
		GetNodesDegree(reqs,index,vNodesDegree);//����ڵ�Ķ�
		GetLinksDegree(1,reqs,index,vNodesDegree,vLinksDegreeByLargNode);//������·�Ķ�=�����˵��е���߶ȵĵ�
		
		int[] vLinksDegreeByLittNode = new int[reqs[index].links];
		GetLinksDegree(2,reqs,index,vNodesDegree,vLinksDegreeByLittNode);//������·�Ķ�=�����˵��е���С�ȵĵ�
		
		Tools myTools = new Tools();
		myTools.Sort(1,vLinksDegreeByLargNode,vSortLinks);//ð������:1��ʾ����vLinksDegreeByLargNode,vSortLinks���źõ�˳��
		
		Sort(vLinksDegreeByLargNode,vLinksDegreeByLittNode,vSortLinks);//vLinksDegreeByLargNode��ȣ�����vLinksDegreeByLittNode��������
	}
	
	/******************************************************************
	���ƣ�SortVLinksByAlg2(......)
	���ܣ���Alg3����������·��Sort virtual links by node degrees
	������
	����ֵ��
	******************************************************************/
	private void SortVLinksByAlg2(VONRequest reqs[],int index,int[] vSortLinks)
	{
		int[] vNodesDegree = new int[reqs[index].nodes];
		int[] vLinksDegreeByLargNode = new int[reqs[index].links];
		GetNodesDegree(reqs,index,vNodesDegree);//����ڵ�Ķ�
		GetLinksDegree(1,reqs,index,vNodesDegree,vLinksDegreeByLargNode);//������·�Ķ�=�����˵��е���߶ȵĵ�
		
		int[] vLinksDegreeByLittNode = new int[reqs[index].links];
		//GetLinksDegree(2,reqs,index,vNodesDegree,vLinksDegreeByLittNode);//������·�Ķ�=�����˵��е���С�ȵĵ�
		for(int i=0;i<reqs[index].links;i++){
			vLinksDegreeByLittNode[i] = (int)reqs[index].link[i].bw;
		}
		Tools myTools = new Tools();
		myTools.Sort(1,vLinksDegreeByLargNode,vSortLinks);//ð������:1��ʾ����
		
		Sort(vLinksDegreeByLargNode,vLinksDegreeByLittNode,vSortLinks);//vLinksDegreeByLargNode��ȣ�����vLinksDegreeByLittNode��������
		/*
        int q,p;
        q = 0;
        p = 0;
        LinkStruct temp;
        
    	for(int i=0;i<reqs[index].nodes-1;i++){
    		if(vLinksDegreeByLargNode[i]!=vLinksDegreeByLargNode[i+1]){
    			p = i;
    			for(int n=p;n<q-1;n++){
    				for(int m=p;m<q-1-n;m++){
    					if(reqs[index].link[m].bw<reqs[index].link[m+1].bw){
    						//temp = vSortLinks[m+1];
    						vSortLinks[m+1] = vSortLinks[m];
    						//vSortLinks[m] = temp;
    					}
    				}
    			}
    			q = i+1;
    		}
    	}*/			
	}
        
        
	
	/******************************************************************
	���ƣ�SortVLinksByAlg3(......)
	���ܣ���Alg3����������·��Sort virtual links by node degrees
	������
	����ֵ��
	******************************************************************/
	private void SortVLinksByAlg3(VONRequest reqs[],int index,int[] vSortLinks)
	{
		int[] vNodesDegree = new int[reqs[index].nodes];
		int[] vLinksDegreeByLargNode = new int[reqs[index].links];
		//int[] vSortLinksDegree = new int[reqs[index].links];
		//GetNodesDegree(reqs,index,vNodesDegree);//����ڵ�Ķ�
		//GetLinksDegree(1,reqs,index,vNodesDegree,vLinksDegreeByLargNode);//������·�Ķ�=�����˵��е���߶ȵĵ�
		for(int i=0;i<reqs[index].links;i++){
			vLinksDegreeByLargNode[i] = (int)reqs[index].link[i].bw;
		}
		//int[] vLinksDegreeByLittNode = new int[reqs[index].links];
		//GetLinksDegree(2,reqs,index,vNodesDegree,vLinksDegreeByLittNode);//������·�Ķ�=�����˵��е���С�ȵĵ�
		
		Tools myTools = new Tools();
		myTools.Sort(1,vLinksDegreeByLargNode,vSortLinks);//ð������:1��ʾ����
		
		//Sort(vLinksDegreeByLargNode,vLinksDegreeByLittNode,vSortLinks);//vLinksDegreeByLargNode��ȣ�����vLinksDegreeByLittNode��������
		
		/*
		LinkStruct temp;
		for(int i=0;i<reqs[index].nodes-1;i++){
			for(int j=0;j<reqs[index].nodes-i-1;i++){
				if(reqs[index].link[j].bw<reqs[index].link[j+1].bw){
					temp = reqs[index].link[j+1];
					reqs[index].link[j+1]=reqs[index].link[j];
					reqs[index].link[j] = temp;
				}
			}
		}*/
	}
	/******************************************************************
	���ƣ�Sort(......)
	���ܣ�vLinksDegreeByLargNode��ȣ�����vLinksDegreeByLittNode��������
	������
	�㷨����ǿ��ð�ݽ������У���vLinksDegreeByLargNode�Ѿ��ź�����
	����ֵ��vSortLinks
	����ʱ�䣺2016.6.24
	�����ˣ�������
	******************************************************************/
	private void Sort(int[] vLinksDegreeByLargNode,int[] vLinksDegreeByLittNode,int[] vSortLinks)
	{
		boolean find = false;
		int t = -1;
		for(int i=0;i<vLinksDegreeByLargNode.length-1;i++){
			find = false;
			for(int j=0;j<vLinksDegreeByLargNode.length-1-i;j++){
				if(vLinksDegreeByLargNode[i] == vLinksDegreeByLargNode[i+1]){
					if(vLinksDegreeByLittNode[vSortLinks[i]] < vLinksDegreeByLittNode[vSortLinks[i+1]]){
						t = vLinksDegreeByLargNode[i];
						vLinksDegreeByLargNode[i] = vLinksDegreeByLargNode[i+1];
						vLinksDegreeByLargNode[i+1] = t;
						
						t = vSortLinks[i];
						vSortLinks[i] = vSortLinks[i+1];
						vSortLinks[i+1] = t;
						find = true;
					}
				}
			}
			if(!find) break;
		}
	}
	/******************************************************************
	���ƣ�GetLinksDegree(......)
	���ܣ�������·�Ķȣ�=�����˵��е���߶ȵĵ�
	������category:1:��ʾ��Ľڵ����Ϊ��·�Ķȣ�2:��ʾС�Ľڵ����Ϊ��·�Ķ�
	����ֵ��vLinksDegree
	******************************************************************/
	private void GetLinksDegree(int category,VONRequest reqs[],int index,int[] vNodesDegree,int[] vLinksDegree)
	{
		for(int i=0;i<reqs[index].links;i++){
			vLinksDegree[i] = 0;//��ʼ��
		}
		for(int j=0;j<reqs[index].links;j++){
			if(category == 1){//1:��ʾ��Ľڵ����Ϊ��·�Ķȣ�
				if(vNodesDegree[reqs[index].link[j].from] >= vNodesDegree[reqs[index].link[j].to]){
					vLinksDegree[j] = vNodesDegree[reqs[index].link[j].from];
				} else {
					vLinksDegree[j] = vNodesDegree[reqs[index].link[j].to];
				}
			} else if(category == 2){//2:��ʾС�Ľڵ����Ϊ��·�Ķ�
				if(vNodesDegree[reqs[index].link[j].from] <= vNodesDegree[reqs[index].link[j].to]){
					vLinksDegree[j] = vNodesDegree[reqs[index].link[j].from];
				} else {
					vLinksDegree[j] = vNodesDegree[reqs[index].link[j].to];
				}
			}
		}
	}
	/******************************************************************
	���ƣ�GetNodesDegree(......)
	���ܣ�����ڵ�Ķ�
	������
	����ֵ��vNodesDegree
	******************************************************************/
	private void GetNodesDegree(VONRequest reqs[],int index,int[] vNodesDegree)
	{
		for(int i=0;i<reqs[index].nodes;i++){
			vNodesDegree[i] = 0;//��ʼ��
		}
		for(int j=0;j<reqs[index].links;j++){
			if(reqs[index].link[j].bw > 0){
				vNodesDegree[reqs[index].link[j].from] ++;
				vNodesDegree[reqs[index].link[j].to] ++;
			}
		}
	}
	
	 
	
	
	/******************************************************************
	���ƣ�int FindSNodeByVNode(......)
	���ܣ��ҵ���Ӧ������ڵ�ӳ��ĵײ�ڵ�
	�㷨����������ģ��transModel[][]������ڵ�ӳ��sNodeEmbed[]
	           ���ȼ��
	������double[][] transModel
		      
	����ֵ��true���ɹ����أ�false��ʧ�ܷ��� 
	******************************************************************/
	private int FindSNodeByVNodeIncludeLink(EOSubstrateNetwork sub,int vNode,double[][] transModel,int[] sNodeEmbed)
	{
		double embedCost = Parameters.MAX_VALUE_DOUBLE;
		int i=-1;
		double nodeECost = -1, linkECost = -1;
		for(i=0;i<sub.nodes;i++){
			if(transModel[vNode][i] > -1  && sNodeEmbed[i] == -1 ){
				//���ӳ����ۣ������·������·ӳ�����linkECost�ͽڵ�ӳ�����nodeECost
				nodeECost = transModel[vNode][i];
				//linkECost = LinkEmbedCost(subCopy);
				//if(!PreEmbedVLinkByKShortestPath(subCopy,reqs,index,noEmbedVLink,vNodeEmbed,p,ret)){//noEmbedVLink������·��snodeEmbed��Ӧ������ڵ�
				//	return -1;//ʧ�ܷ���
				//}
				embedCost = Parameters.NodeECoEfficient * nodeECost + Parameters.LinkECoEfficient * linkECost;
			}
		}
		if(i>=sub.nodes) return -1;
		else return i;
	}
	/******************************************************************
	���ƣ�boolean LinkEmbedCost(......)
	���ܣ��ṩһ������ڵ�sp1�ʹ���bw�����ṩsp2������sp2->sp1��ӳ�����
	�㷨��
	������double[][] transModel������ģ��
	����ֵ��1��true���ɹ����أ�false��ʧ�ܷ��� 
	      2��·����
	      3��ӳ��Ƶ�ײ�������
	      4��ӳ���Ƶ�ײ���ʼ�ͽ�ֹ������
	******************************************************************/
	private boolean LinkEmbedCost(EOSubstrateNetwork sub,int vNode,double[][] transModel,int[] sNodeEmbed)
	{
		int[] flag = new int[sub.nodes];
		int[] prev = new int[sub.nodes];
		int[] dist = new int[sub.nodes];
		for(int i=0;i<sub.nodes;i++){
			flag[i] = -1;//˵��i�ڵ㲻��s��
			prev[i] = -1;
			dist[i] = -1;
		}
		return true;
		//s[sp2] = 
		//
	}
	
	/******************************************************************
	���ƣ�int FindSNodeByVNode(......)
	���ܣ��ҵ���Ӧ������ڵ�ӳ��ĵײ�ڵ�
	�㷨����������ģ��transModel[][]������ڵ�ӳ��sNodeEmbed[]
	           ������ģ�����ҵ���С��ֵ��������ڵ�δӳ��
	������double[][] transModel������ģ��
	����ֵ��>-1���ɹ����أ�-1��ʧ�ܷ��� 
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
	���ƣ�boolean FindNoEVlink(......)
	���ܣ��ҵ�δӳ���������·,1)����
	������
		      
	����ֵ��true���ɹ����أ�false��ʧ�ܷ��� 
	******************************************************************/
	private boolean FindNoEVlink(VONRequest reqs[],int index,int[] vLinkEmbed,int[] vNodeEmbed,int[] vTwoNodeAndLink)
	{
		double maxBW = Parameters.MIN_VALUE_DOUBLE;
		int i=0;
		//���ȣ��ҵ�û��ӳ���������·�����еĽڵ�������һ���Ѿ�ӳ��
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
		//��Σ��ҵ�û��ӳ���������·
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
	���ƣ�void FindMinElement(......)
	���ܣ�Ѱ����СԪ��
	������
		      subΪ��������
		      reqsΪ����������
		      indexΪ��index����������
	        transModelΪ����ģ�ͣ�
	        vnodeEmbedΪ����ڵ�ӳ��ģ��
	        snodeEmbedΪ����ڵ�ӳ��ģ��
	����ֵ��     minElentΪ��СԪ�أ�minElent[0]:��СԪ������ڵ㣻minElent[1]:��СԪ������ڵ㣻
	******************************************************************/
	private void FindMinElement(EOSubstrateNetwork sub,VONRequest reqs[],int index,double[][] transModel,int[] vnodeEmbed,int[] snodeEmbed,int[] minElent)
	{
		//Ѱ����СԪ�أ���������minIndexReq��minIndexSub
		minElent[0] = minElent[1] = -1;
		double minElement = 10000;
		for(int i=0;i<reqs[index].nodes;i++){
			for(int j=0;j<sub.nodes;j++){
				if(minElement>transModel[i][j] && transModel[i][j]>-1 && vnodeEmbed[i]==-1 && snodeEmbed[j]==-1){//vnodeEmbed[i] == -1��ʾ����ڵ�iδ��ӳ��
					minElent[0] = i;//minIndexReq = i;
					minElent[1] = j;//minIndexSub = j;
					minElement = transModel[i][j];
				}
			}
		}
		//if(minElent[0] > -1) return -1;//û���ҵ���СԪ��
	}
	
	/******************************************************************
	���ƣ�void FindMinElement(......)
	���ܣ�Ѱ����СԪ��
	������
		      subΪ��������
		      reqsΪ����������
		      indexΪ��index����������
	        transModelΪ����ģ�ͣ�
	        vnodeEmbedΪ����ڵ�ӳ��ģ��
	        snodeEmbedΪ����ڵ�ӳ��ģ��
	����ֵ��     minElentΪ��СԪ�أ�minElent[0]:��СԪ������ڵ㣻minElent[1]:��СԪ������ڵ㣻
	******************************************************************/
	private void FindEnMinElement(EOSubstrateNetwork sub,VONRequest reqs[],int index,double[][] transModel,int[] vnodeEmbed,int[] snodeEmbed,int[] minElent)
	{
		//Ѱ����СԪ�أ���������minIndexReq��minIndexSub
		double minElement = 10000;
		//�ҵ����Ѿ�ӳ��Ľڵ����ӵ�δӳ��Ľڵ�
		minElent[0] = minElent[1] = -1;
		int othVNode,othSNode;
		minElement = 10000;
		int slotNoRe = Parameters.MAX_VALUE_INT;
		for(int i=0;i<reqs[index].nodes;i++){
			for(int j=0;j<sub.nodes;j++){
				//�ж��Ƿ�i�ڵ��Ƿ����Ѿ�ӳ�������ڵ�����
				for(int k=0;k<reqs[index].links;k++){
					if((i == reqs[index].link[k].from && vnodeEmbed[reqs[index].link[k].to] != -1) || (i == reqs[index].link[k].to && vnodeEmbed[reqs[index].link[k].from] != -1)){
						if(minElement>transModel[i][j] && transModel[i][j]>-1 && vnodeEmbed[i]==-1 && snodeEmbed[j]==-1){//vnodeEmbed[i] == -1��ʾ����ڵ�iδ��ӳ��
							if(i == reqs[index].link[k].from) {
								othVNode = reqs[index].link[k].to;
								othSNode = vnodeEmbed[othVNode];
							} else if(i == reqs[index].link[k].to) {
								othVNode = reqs[index].link[k].from;
								othSNode = vnodeEmbed[othVNode];
							}
							int slotNoRe1 = CheckIfEnoughSlotsOnLink(sub,k,reqs[index].link[k].bw);
							if(slotNoRe1 < slotNoRe){
								minElent[0] = i;//minIndexReq = i;
								minElent[1] = j;//minIndexSub = j;
								minElement = transModel[i][j];
								slotNoRe = slotNoRe1;
							}
						}
					}
				}
				//if(minElement>transModel[i][j] && transModel[i][j]>-1 && vnodeEmbed[i]==-1 && snodeEmbed[j]==-1){//vnodeEmbed[i] == -1��ʾ����ڵ�iδ��ӳ��
				//}
			}
		}
		if(minElent[0] != -1) return ;
		//Ѱ����СԪ��
		minElent[0] = minElent[1] = -1;
		minElement = 10000;
		for(int i=0;i<reqs[index].nodes;i++){
			for(int j=0;j<sub.nodes;j++){
				if(minElement>transModel[i][j] && transModel[i][j]>-1 && vnodeEmbed[i]==-1 && snodeEmbed[j]==-1){//vnodeEmbed[i] == -1��ʾ����ڵ�iδ��ӳ��
					minElent[0] = i;//minIndexReq = i;
					minElent[1] = j;//minIndexSub = j;
					minElement = transModel[i][j];
				}
			}
		}
	}
	
		
	
	//******************************************************************
	//���ƣ�int InitAllocModel(......)
	//���ܣ���ʼ������ģ��
	//������
	//	      subΪ��������
	//	      reqsΪ����������
	//	      indexΪ��index����������
	//����ֵ��     vnodeEmbedΪ����ڵ�ӳ��ģ��//-1������δ���䣬>-1�����Ѿ�����
	//        snodeEmbedΪ����ڵ�ӳ��ģ��//-1������δ���䣬>-1�����Ѿ�����
	//        vlinkEmbedΪ������·ӳ��ģ��//-1������δ���䣬>-1�����Ѿ�����
	//******************************************************************
	private void InitAllocModel(EOSubstrateNetwork sub,VONRequest reqs[],int index,int[] vnodeEmbed,int[] snodeEmbed,int[] vlinkEmbed)
	{
		for(int i=0; i<reqs[index].nodes; i++){
			vnodeEmbed[i] = -1;//-1������δ���䣬>-1�����Ѿ�����
		}
		for(int i=0; i<sub.nodes; i++){
			snodeEmbed[i] = -1;//-1������δ���䣬>-1�����Ѿ�����
		}
		for(int i=0; i<reqs[index].links; i++){
			vlinkEmbed[i] = -1;//-1������δ���䣬>-1�����Ѿ�����
		}
	}
	//�ڵ���䣺�����ڵ����ˮƽ,����LP���Թ滮���
	private void AllocNodesMinEleILP(EOSubstrateNetwork sub,VONRequest reqs[],int index,double[][] transModel)
	{
		Tools myDowith = new Tools();
		
		String data;
		data = "set Nv:=";
		for(int i = 0; i < reqs[index].nodes; i++){
			data += " " + i;    
		}
		data += ";\r\n"; 		 
		myDowith.SaveFile("glpsolRSA.dat", data, false);
		
		data = "set Ns:=";
		for(int j = 0; j < sub.nodes; j ++) {
			data += j + " ";
		}
		data += ";\r\n"; 		 
		myDowith.SaveFile("glpsolRSA.dat", data, true);
		
		data = "";
		for(int j = 0; j < reqs[index].nodes; j ++) {
			data += "set A[" + j + "]:=";
			for(int i=0;i<sub.nodes;i++){
				//if(sub.cpu[i] >= reqs[index].cpu[j]){
				if(s2v_n[i].rest_cpu >= reqs[index].cpu[j]){
					data += i + " ";
				}
			}
			data += ";\r\n"; 
		}
		myDowith.SaveFile("glpsolRSA.dat", data, true);
		
		data = "param cost:=\r\n";
		for(int j=0;j<reqs[index].nodes;j++)
			for (int i = 0; i < sub.nodes; i++) {
				data += j + " " + i + " " + transModel[j][i] +"\r\n";
			}
		data += ";\r\n";	
		myDowith.SaveFile("glpsolRSA.dat", data, true);
		
		try {
			String s;
			Process process = null;
			process = Runtime.getRuntime().exec("cmd /c C:/������/VONE/VNE/glpk-4.60/w64/glpsol.exe -m C:/������/VONE/VNE/glpk-4.60/w64/NEm01ILPNoDataByCXH.mod -d glpsolRSA.dat -o glpsolRSA.o");
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
	
	//�ڵ���䣺�����ڵ����ˮƽ,����LP���Թ滮���
	public boolean FindOptSoluByNEmCXH(EOSubstrateNetwork sub,VONRequest reqs[],int index,int retVNodeE[],int retSNodeEmbed[])
	{
		BufferedReader reader = null;
		
		int keySNode1 = -1,keySNode2 = -1;
		int keyVNode1 = -1,keyVNode2 = -1;//,slotIndex=-1;
		
		try {
			System.out.println("����Ϊ��λ��ȡ�ļ����ݣ�һ�ζ�һ���У�");
            reader = new BufferedReader(new FileReader("glpsolRSA.o"));
            String tempString = null;
            
            int line = 1;
            //һ�ζ���һ�У�ֱ������nullΪ�ļ�����
            while ((tempString = reader.readLine()) != null) {
                //��ʾ�к� //
            	//System.out.println("line " + line + ": " + tempString);
                if (line == 5 && tempString.indexOf("OPTIMAL") == -1) {  //˵��δ�ҵ����Ž� 
                	System.out.println("line " + line + ": " + tempString + "No Found the optimal resolvetion.");
                	return false;
                } 
                if(line > 6 && tempString.indexOf(" x[") != -1){//˵���ҵ������Ž��x����
                	String tmpStr = "";
                	String tempString1 = tempString.trim();
                	
                	tmpStr = tempString1.substring(tempString1.indexOf("*")+1);
                	tmpStr = tmpStr.trim();
                	//System.out.println("line " + line + ": " + tmpStr);
                	
                	tmpStr = tmpStr.substring(0,tmpStr.indexOf(" "));
                	//System.out.println("line " + line + ": " + tmpStr);
                	if(Integer.parseInt(tmpStr) == 1){//˵���ҵ���һ����
                		//�õ�һ���⸳ֵ��tmpStr������x[0,2]
                		//var f{(i,j) in E,(m,n) in Ev,s in Vf,t in Vf,k in MSet}, binary;
                		tempString = tempString.trim();
                		tmpStr = tempString.substring(tempString.indexOf(" ")+1);//ȥ��ǰ����к�
                		//System.out.println("line " + line + ": " + tmpStr);
                		//tmpStr = tmpStr.substring(0,tmpStr.indexOf(" "));		//�õ�f[i,j,m,n,s,t,k]
                		//System.out.println("line " + line + ": " + tmpStr);
                		int keyNode1 = -1;
                		//keyNode1 = Integer.parseInt(tmpStr.substring(tmpStr.indexOf("[")+1, tmpStr.indexOf(",")));//�õ�f[i,j,m,n,s,t,k]��i
                		//System.out.println("keyNode1:"+keyNode1);
                		//M[5,1]
                		keyVNode1 = Integer.parseInt(tmpStr.substring(tmpStr.indexOf("[")+1, tmpStr.indexOf(",")));
                		//System.out.println("keyNode1:"+keySNode1);
                		tmpStr = tmpStr.substring(tmpStr.indexOf(",")+1);
                		keySNode1 = Integer.parseInt(tmpStr.substring(0, tmpStr.indexOf("]")));
                		
                		retVNodeE[keyVNode1] = keySNode1;//����ڵ�keyVNode1��ӳ�䵽����ڵ�keySNode1��
                		retSNodeEmbed[keySNode1] = keyVNode1;
                	}
                }
                
                line++;
            } 
            reader.close();
	    } catch (IOException e) {
	    	 return false;
	    } finally {
	         if (reader != null) {
	            try {
	                reader.close();
	            } catch (IOException e1) {
	            }
	         }
	     }  
        return true;
	}
	
	//�ڵ���䣺�����ڵ����ˮƽ
	private int AllocNodes(EOSubstrateNetwork sub,VONRequest reqs[],int index,int[] vnodeEmbed,int[] snodeEmbed,double[][] transModel)
	{
		for(int i=0;i<reqs[index].nodes;i++){
			//�ҵ����Ԫ��
			double maxCost = 100000000;
			int maxVNode = -1;
			int maxSNode = -1;
			for(int j=0;j<reqs[index].nodes;j++){
				for(int k=0;k<sub.nodes;k++){
					if(maxCost>transModel[j][k] && transModel[j][k]>-1){
						maxCost = transModel[j][k];
						maxVNode = j;
						maxSNode = k;
					}
				}
			}
			if(maxVNode > -1){//�ҵ��˶�Ӧ�Ľڵ�
				vnodeEmbed[maxVNode] = maxSNode;
				snodeEmbed[maxSNode] = maxVNode;
				for(int ii=0;ii<reqs[index].nodes;ii++){
					transModel[ii][maxSNode] = -1;
				}
				for(int ii=0;ii<sub.nodes;ii++){
					transModel[maxVNode][ii] = -1;
				}
				
			} else {
				return -1;
			}
		}
		return 1;
	}
	
	
	//******************************************************************
	//���ƣ�int InitTranModel(......)
	//���ܣ���ʼ������ģ��
	//������
	//	      subΪ��������
	//	      sNodeΪ����ڵ�
	//	      reqsΪ����������
	//	      indexΪ��index����������
	//	      transModelΪ���صĴ���ģ��
	//        indexModelΪ���صĴ���ģ������С���õ�Ƶ������
	//����ֵ��
	//******************************************************************
	private void InitTranModel(EOSubstrateNetwork sub,VONRequest reqs[],int index,double[][] transModel,int[][] slotIndexModel,int[][] linkModel)
	{
		//��������ģ�ͺ���С��������·��
		int slotNum = -1;
		int link[] = new int[1];
		for(int i=0;i<reqs[index].nodes;i++){
			for(int j=0;j<sub.nodes;j++){
				if(reqs[index].cpu[i] <= s2v_n[j].rest_cpu + Parameters.MIN_VALUE_DOUBLE){//�ײ�ڵ��CPU��������ڵ�
					slotNum = CheckIfSlotEnoughByNode(sub,j,reqs,index,i,link);
					if( slotNum > -1){//�����ײ�ڵ�j�����ӵ���·Ƶ�ײ۴�������ڵ�i����Ĳ�
						transModel[i][j] = 1.0/s2v_n[j].rest_cpu;//div(1.0,s2v_n[j].rest_cpu,10);//1.0/(1.0*s2v_n[j].rest_cpu);
						slotIndexModel[i][j] = slotNum;
						linkModel[i][j] = link[0];
					} else {
						transModel[i][j] = -1;//-1������ӳ��
						slotIndexModel[i][j] = -1;
					}  
				} else {
					transModel[i][j] = -1;//-1������ӳ��
				}
			}
		}
	}

	private void CreateVONE01ILPByY_L(EOSubstrateNetwork sub,VONRequest reqs[],int index)
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

		data = "set Lv:=\r\n";
		for(int j = 0; j < reqs[index].links; j ++) {
			if(reqs[index].link[j].from < reqs[index].link[j].to)
				data += reqs[index].link[j].from + " " + reqs[index].link[j].to + "\r\n";
		}
		data += ";\r\n";
		myDowith.SaveFile("glpsolRSA.dat", data, true);

		data = "set Ns:=";
		for(int j = 0; j < sub.nodes; j ++) {
			data += j + " ";
		}
		data += ";\r\n";
		myDowith.SaveFile("glpsolRSA.dat", data, true);

		data = "set Ls:=\r\n";
		for(int j = 0; j < sub.links; j ++) {
			if(sub.link[j].from < sub.link[j].to)
				data += sub.link[j].from + " " + sub.link[j].to + "\r\n";
		}
		data += ";\r\n";
		myDowith.SaveFile("glpsolRSA.dat", data, true);

		data = "";
		for(int j = 0; j < sub.nodes; j ++) {
			data += "set Nsj[" + j + "]:=";
			for(int i=0;i<sub.links;i++){
				if(sub.link[i].to== j){
						data += sub.link[i].from + " ";
				}
			}
			data += ";\r\n";
		}
		myDowith.SaveFile("glpsolRSA.dat", data, true);

		data = "";
		for(int j = 0; j < sub.nodes; j++) {
			data = "set Nsb[" + j + "]:=";
			for(int i=0;i<sub.links;i++){
				if(sub.link[i].from == j){
						data += sub.link[i].to + " ";
				}
			}
			data += ";\r\n";
			myDowith.SaveFile("glpsolRSA.dat", data, true);
		}

		data = "";
		for(int j = 0; j < reqs[index].nodes; j ++) {
			data += "set T["+j+"]:=";
			for(int i=0;i<sub.nodes;i++){
				if(s2v_n[i].rest_cpu >= reqs[index].cpu[j]){//
					data += i + " ";
				}
			}
			data += ";\r\n";
		}
		myDowith.SaveFile("glpsolRSA.dat", data, true);

		data = "set SlotIndex:=";
		for(int j = 0; j < sub.slotsNum; j ++) {
			data += j + " ";
		}
		data += ";\r\n";
		myDowith.SaveFile("glpsolRSA.dat", data, true);

		data = "set MD:=1,2,3,4,6,8;\r\n";
		myDowith.SaveFile("glpsolRSA.dat", data, true);

		data="";
		for (int j = 0;j<sub.nodes;j++){
			data += "set NLs[" + j + "]:=\r\n";
			for(int i=0;i<sub.links;i++){
				if(sub.link[i].from == j||sub.link[i].to==j){
					data += sub.link[i].from+" "+sub.link[i].to + " \r\n";
				}
			}
			data += ";\r\n";
		}
		myDowith.SaveFile("glpsolRSA.dat", data, true);

		for(int j = 0; j < reqs[index].links; j++) {
			for(int i=0;i<sub.slotsNum;i++){
				for(int k=1;k<9;){
					data = "set Slotx["+reqs[index].link[j].from+","+reqs[index].link[j].to+","+i+","+k+"]:=";
					for(int p=i;p<sub.slotsNum&&(p<i+Math.ceil(reqs[index].link[j].bw/(12.5*k))+sub.diffSlot);p++){
						data += p + " ";
					}
					data += ";\r\n";
					myDowith.SaveFile("glpsolRSA.dat", data, true);
					if(k>=4) k+=2;
					else k++;
				}
			}
		}

		for(int j = 0; j < reqs[index].links; j ++) {
			if(reqs[index].link[j].from > reqs[index].link[j].to) continue;//chenxhɾ��20200903
			for(int i=0;i<sub.slotsNum;i++){
				for(int k=1;k<9;){
					data = "set Sloty["+reqs[index].link[j].from+","+reqs[index].link[j].to+","+i+","+k+"]:=";
					for(int p=i;p>=0&&(p>=i-Math.ceil(reqs[index].link[j].bw/(12.5*k))-sub.diffSlot+1);p--){
						data += p + " ";
					}
					data += ";\r\n";
					if(k>=4) k+=2;
					else k++;
					myDowith.SaveFile("glpsolRSA.dat", data, true);
				}
			}
		}

		data = "param p:=\r\n";
		for (int i = 0; i < reqs[index].nodes; i++) {
			data += i + " " + reqs[index].cpu[i]+"\r\n";
		}
		data += ";\r\n";
		myDowith.SaveFile("glpsolRSA.dat", data, true);

		data = "param bw:=\r\n";
		for (int i = 0; i < reqs[index].links; i++) {
			if(reqs[index].link[i].from > reqs[index].link[i].to) continue;
			data += reqs[index].link[i].from + " " + reqs[index].link[i].to + " " + reqs[index].link[i].bw+"\r\n";
		}
		data += ";\r\n";
		myDowith.SaveFile("glpsolRSA.dat", data, true);

		data = "param f:="+(sub.slotsNum-1)+";\r\n";
		myDowith.SaveFile("glpsolRSA.dat", data, true);

		data = "param u:=\r\n";
		for(int i =0;i<sub.nodes;i++){
			data+=i+" "+sub.cpu[i]+"\r\n";
		}
		data+=";\r\n";
		myDowith.SaveFile("glpsolRSA.dat", data, true);

		data = "param USlot:=\r\n";
		for (int i = 0; i < sub.links; i++) {
			for(int j=0;j<sub.slotsNum;j++){
				data += sub.link[i].from + " " + sub.link[i].to + " " +j + " "+ sub.slots[i][j]+"\r\n";
			}
		}
		data += ";\r\n";
		myDowith.SaveFile("glpsolRSA.dat", data, true);

		data = "param l:=\r\n";
		for (int i = 0; i < sub.links; i++) {
			data += sub.link[i].from + " " + sub.link[i].to + " " + sub.link[i].length+"\r\n";
		}
		data += ";\r\n";
		myDowith.SaveFile("glpsolRSA.dat", data, true);

		data = "param dm:=\r\n";
		data += "1 3000\r\n";
		data += "2 1500\r\n";
		data += "3 750\r\n";
		data += "4 375\r\n";
		data += "6 94\r\n";
		data += "8 24\r\n";
		data += ";\r\n";
		myDowith.SaveFile("glpsolRSA.dat", data, true);

		data = "param R:=100000000;\r\n";
		myDowith.SaveFile("glpsolRSA.dat", data, true);

		data = "param G:="+sub.diffSlot+";\r\n";
		myDowith.SaveFile("glpsolRSA.dat", data, true);

		data = "param W:=12.5;\r\n";
		myDowith.SaveFile("glpsolRSA.dat", data, true);

		data = "param N:=\r\n";int g;
		for (int i=0;i<reqs[index].links;i++){
			for (int k=1;k<9;){
				g=(int)(Math.ceil(reqs[index].link[i].bw/(12.5*k))+1);
				data+=reqs[index].link[i].from+" "+reqs[index].link[i].to+" "+k+" "+g+"\r\n";
				if(k>=4) k+=2;
				else k++;
			}
		}
		data+=";\r\n";
		myDowith.SaveFile("glpsolRSA.dat", data, true);

		data = "param K:=\r\n";
		for (int i =0;i<sub.nodes;i++){
			data+=i+" 1\r\n";
		}
		data+=";\r\n";
		myDowith.SaveFile("glpsolRSA.dat", data, true);

		data="param p1:=1329.33;\r\n" +
				"param p2:=120;\r\n" +
				"param p3:=150;\r\n" +
				"param p4:=0.465;\r\n" +
				"param p5:=0.18;\r\n"+
				"end;\r\n";
		myDowith.SaveFile("glpsolRSA.dat", data, true);


		System.out.println("Done");

		try {
			String s;
			Process process = null;

			if(Parameters.CurrentVONEMethod == Parameters.MapVONEILPByY_L){
				//process = Runtime.getRuntime().exec("cmd /c C:/������/VONE/VONE/glpk-4.60/w64/glpsol.exe -m C:/������/VONE/VONE/glpk-4.60/w64/glpsolMILPPRByLinChen.mod -d glpsolRSA.dat -o glpsolRSA.o");
				process = Runtime.getRuntime().exec("cmd  /c E:/���Թ�����/CEEA/VNE/glpk-4.60/w64/glpsol.exe -m E:/���Թ�����/CEEA/VNE/glpk-4.60/w64/glpsolMILByLin_LYY_ReviseByRitchie_2022_4_30_15.mod -d glpsolRSA.dat -o glpsolRSA.o");
				System.out.println("Done");
			} else if(Parameters.CurrentVONEMethod == Parameters.MapVONE01ILPPRLinCXH){
				//process = Runtime.getRuntime().exec("cmd /c C:/������/VONE/VONE/glpk-4.60/w64/glpsol.exe -m C:/������/VONE/VONE/glpk-4.60/w64/glpsolMILPPRByLinChen.mod -d glpsolRSA.dat -o glpsolRSA.o");
				process = Runtime.getRuntime().exec("cmd /c C:/������/VONE/VNE/glpk-4.60/w64/glpsol.exe -m C:/������/VONE/VNE/glpk-4.60/w64/glpsolMILPPRByLinChenUndirectDiagram.mod -d glpsolRSA.dat -o glpsolRSA.o");
			} else if(Parameters.CurrentVONEMethod == Parameters.MapVONE01ILPLin_EquilibriumCXH){
				//process = Runtime.getRuntime().exec("cmd /c C:/������/VONE/VONE/glpk-4.60/w64/glpsol.exe -m C:/������/VONE/VONE/glpk-4.60/w64/glpsolMILPPRByLinChen.mod -d glpsolRSA.dat -o glpsolRSA.o");
				process = Runtime.getRuntime().exec("cmd /c C:/������/VONE/VNE/glpk-4.60/w64/glpsol.exe -m C:/������/VONE/VNE/glpk-4.60/w64/glpsolMILPByLinUndirectDiagram-Equilibrium.mod -d glpsolRSA.dat -o glpsolRSA.o");
				//process = Runtime.getRuntime().exec("cmd /c C:/������/VONE/VNE/glpk-4.60/w64/glpsol.exe -m C:/������/VONE/VNE/glpk-4.60/w64/glpsolMILPByLinUndirectDiagram-glpsolMILPByLinUndirectDiagram-Equilibrium.mod -d glpsolRSA.dat -o glpsolRSA.o");

			} else {
				process = Runtime.getRuntime().exec("cmd /c C:/������/VONE/VNE/glpk-4.60/w64/glpsol.exe -m C:/������/VONE/VNE/glpk-4.60/w64/glpsolMILPByLin.mod  -d glpsolRSA.dat -o glpsolRSA.o");
				//process = Runtime.getRuntime().exec("cmd /c C:/������/VONE/VONE/glpk-4.60/w64/glpsol.exe -m C:/������/VONE/VONE/glpk-4.60/w64/glpsolMILPByLin.mod  -d glpsolRSA.dat -o glpsolRSA.o");
				//process = Runtime.getRuntime().exec("cmd /c C:/������/VNE/VNE/VNE/glpk-4.60/w64/glpsol.exe -m C:/������/VNE/VNE/VNE/glpk-4.60/w64/glpsolMILPByLinUndirectDiagram.mod -d glpsolRSA.dat -o glpsolRSA.o");
				//process = Runtime.getRuntime().exec("cmd /c C:/������/VONE/VNE/glpk-4.60/w64/glpsol.exe -m C:/������/VONE/VNE/glpk-4.60/w64/glpsolMILPByLinUndirectDiagram.mod -d glpsolRSA.dat -o glpsolRSA.o");
			}
			//if(embedModelOrAlgo == Parameters.MapVONE3ByWangY){
			//	process = Runtime.getRuntime().exec("cmd /c C:/������/VONE/VONE/glpk-4.60/w64/glpsol.exe -m C:/������/VONE/VONE/glpk-4.60/w64/glpsolMILVONE3NodesNoDataWangY.mod -d glpsolRSA.dat -o glpsolRSA.o");
			//} else if(embedModelOrAlgo == Parameters.MapVONE3PByWangYAndChenxh){
			//	process = Runtime.getRuntime().exec("cmd /c C:/������/VONE/VONE/glpk-4.60/w64/glpsol.exe -m C:/������/VONE/VONE/glpk-4.60/w64/glpsolMILPVONE3NodesNoDataWangY.mod -d glpsolRSA.dat -o glpsolRSA.o");
			//}
			//Process process = Runtime.getRuntime().exec("cmd /c C:/������/VONE/VONE/glpk-4.60/w64/glpsol.exe -m C:/������/VONE/VONE/glpk-4.60/w64/glpsol01ILPVONE3PNodesWangY.mod -d glpsolRSA.dat -o glpsolRSA.o");
			//Process process = Runtime.getRuntime().exec("cmd /c C:/������/VONE/VONE/glpk-4.60/w64/glpsol.exe -m C:/������/VONE/VONE/glpk-4.60/w64/glpsolMILPVONE3NodesNoDataWangY.mod -d glpsolRSA.dat -o glpsolRSA.o");
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

	private int MapVONEILPByY_L(EOSubstrateNetwork sub,VONRequest reqs[],int index)
	{
		CreateVONEILPByY_L_V2S(sub,reqs,index);
		int retNodeE[];
		LinkedList[] retVLinkE;
		retVLinkE = new LinkedList[reqs[index].links];
		retNodeE = new int[reqs[index].nodes];
		if(FindOptSoluByY_L(sub,reqs,index,retNodeE,retVLinkE)){//�ҵ������Ž�
			//System.out.println("������Ƶ�ײ۳�ͻ");
			//PrintPath(reqs,index,kShortestPath,p,auxGraph.virtualNodes,pathEff,retLinkE,retSlotSE,retSlotEE);
			AddNodesMap(reqs,index,retNodeE);//�ڵ�ӳ��
			AddNodesMapSub(sub,reqs,index,retNodeE);//�ڵ�ӳ���޸�sub.cpu
			AddLinksMapByILPBYY_L(sub,reqs,index,retNodeE,retVLinkE);

			System.out.println("embed reqs "+ index+" successfully");
			return 0;//�ɹ��ҵ�VONE��
		}

		return -1;
	}
	private void CreateVONEILPByY_L_V2S(EOSubstrateNetwork sub,VONRequest reqs[],int index)
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

		data = "set Lv:=\r\n";
		for(int j = 0; j < reqs[index].links; j ++) {
			if(reqs[index].link[j].from < reqs[index].link[j].to)
				data += reqs[index].link[j].from + " " + reqs[index].link[j].to + "\r\n";
		}
		data += ";\r\n";
		myDowith.SaveFile("glpsolRSA.dat", data, true);

		data = "set Ns:=";
		for(int j = 0; j < sub.nodes; j ++) {
			data += j + " ";
		}
		data += ";\r\n";
		myDowith.SaveFile("glpsolRSA.dat", data, true);

		data = "set Ls:=\r\n";
		for(int j = 0; j < sub.links; j ++) {
			if(sub.link[j].from < sub.link[j].to)
				data += sub.link[j].from + " " + sub.link[j].to + "\r\n";
		}
		data += ";\r\n";
		myDowith.SaveFile("glpsolRSA.dat", data, true);

		data = "";
		for(int j = 0; j < sub.nodes; j ++) {
			data += "set Nsj[" + j + "]:=";
			for(int i=0;i<sub.links;i++){
				if(sub.link[i].to== j){
					data += sub.link[i].from + " ";
				}
			}
			data += ";\r\n";
		}
		myDowith.SaveFile("glpsolRSA.dat", data, true);

		data = "";
		for(int j = 0; j < sub.nodes; j++) {
			data = "set Nsb[" + j + "]:=";
			for(int i=0;i<sub.links;i++){
				if(sub.link[i].from == j){
					data += sub.link[i].to + " ";
				}
			}
			data += ";\r\n";
			myDowith.SaveFile("glpsolRSA.dat", data, true);
		}

		data = "";
		for(int j = 0; j < reqs[index].nodes; j ++) {
			data += "set T["+j+"]:=";
			for(int i=0;i<sub.nodes;i++){
				if(s2v_n[i].rest_cpu >= reqs[index].cpu[j]){//
					data += i + " ";
				}
			}
			data += ";\r\n";
		}
		myDowith.SaveFile("glpsolRSA.dat", data, true);

		data = "set SlotIndex:=";
		for(int j = 0; j < sub.slotsNum; j ++) {
			data += j + " ";
		}
		data += ";\r\n";
		myDowith.SaveFile("glpsolRSA.dat", data, true);

		data = "set MD:=1,2,3,4,6,8;\r\n";
		myDowith.SaveFile("glpsolRSA.dat", data, true);

		data="";
		for (int j = 0;j<sub.nodes;j++){
			data += "set NLs[" + j + "]:=\r\n";
			for(int i=0;i<sub.links;i++){
				if(sub.link[i].from == j||sub.link[i].to==j){
					data += sub.link[i].from+" "+sub.link[i].to + " \r\n";
				}
			}
			data += ";\r\n";
		}
		myDowith.SaveFile("glpsolRSA.dat", data, true);

		for(int j = 0; j < reqs[index].links; j++) {
			for(int i=0;i<sub.slotsNum;i++){
				for(int k=1;k<9;){
					data = "set Slotx["+reqs[index].link[j].from+","+reqs[index].link[j].to+","+i+","+k+"]:=";
					for(int p=i;p<sub.slotsNum&&(p<i+Math.ceil(reqs[index].link[j].bw/(12.5*k))+sub.diffSlot);p++){
						data += p + " ";
					}
					data += ";\r\n";
					myDowith.SaveFile("glpsolRSA.dat", data, true);
					if(k>=4) k+=2;
					else k++;
				}
			}
		}

		for(int j = 0; j < reqs[index].links; j ++) {
			if(reqs[index].link[j].from > reqs[index].link[j].to) continue;//chenxhɾ��20200903
			for(int i=0;i<sub.slotsNum;i++){
				for(int k=1;k<9;){
					data = "set Sloty["+reqs[index].link[j].from+","+reqs[index].link[j].to+","+i+","+k+"]:=";
					for(int p=i;p>=0&&(p>=i-Math.ceil(reqs[index].link[j].bw/(12.5*k))-sub.diffSlot+1);p--){
						data += p + " ";
					}
					data += ";\r\n";
					if(k>=4) k+=2;
					else k++;
					myDowith.SaveFile("glpsolRSA.dat", data, true);
				}
			}
		}

		data = "param p:=\r\n";
		for (int i = 0; i < reqs[index].nodes; i++) {
			data += i + " " + reqs[index].cpu[i]+"\r\n";
		}
		data += ";\r\n";
		myDowith.SaveFile("glpsolRSA.dat", data, true);

		data = "param bw:=\r\n";
		for (int i = 0; i < reqs[index].links; i++) {
			if(reqs[index].link[i].from > reqs[index].link[i].to) continue;
			data += reqs[index].link[i].from + " " + reqs[index].link[i].to + " " + reqs[index].link[i].bw+"\r\n";
		}
		data += ";\r\n";
		myDowith.SaveFile("glpsolRSA.dat", data, true);

		data = "param f:="+(sub.slotsNum-1)+";\r\n";
		myDowith.SaveFile("glpsolRSA.dat", data, true);

		data = "param u:=\r\n";
		for(int i =0;i<sub.nodes;i++){
			data+=i+" "+sub.cpu[i]+"\r\n";
		}
		data+=";\r\n";
		myDowith.SaveFile("glpsolRSA.dat", data, true);

		data = "param USlot:=\r\n";
		for (int i = 0; i < sub.links; i++) {
			for(int j=0;j<sub.slotsNum;j++){
				data += sub.link[i].from + " " + sub.link[i].to + " " +j + " "+ sub.slots[i][j]+"\r\n";
			}
		}
		data += ";\r\n";
		myDowith.SaveFile("glpsolRSA.dat", data, true);

		data = "param l:=\r\n";
		for (int i = 0; i < sub.links; i++) {
			data += sub.link[i].from + " " + sub.link[i].to + " " + sub.link[i].length+"\r\n";
		}
		data += ";\r\n";
		myDowith.SaveFile("glpsolRSA.dat", data, true);

		data = "param dm:=\r\n";
		data += "1 3000\r\n";
		data += "2 1500\r\n";
		data += "3 750\r\n";
		data += "4 375\r\n";
		data += "6 94\r\n";
		data += "8 24\r\n";
		data += ";\r\n";
		myDowith.SaveFile("glpsolRSA.dat", data, true);

		data = "param R:=100000000;\r\n";
		myDowith.SaveFile("glpsolRSA.dat", data, true);

		data = "param G:="+sub.diffSlot+";\r\n";
		myDowith.SaveFile("glpsolRSA.dat", data, true);

		data = "param W:=12.5;\r\n";
		myDowith.SaveFile("glpsolRSA.dat", data, true);

		data = "param N:=\r\n";int g;
		for (int i=0;i<reqs[index].links;i++){
			for (int k=1;k<9;){
				g=(int)(Math.ceil(reqs[index].link[i].bw/(12.5*k))+1);
				data+=reqs[index].link[i].from+" "+reqs[index].link[i].to+" "+k+" "+g+"\r\n";
				if(k>=4) k+=2;
				else k++;
			}
		}
		data+=";\r\n";
		myDowith.SaveFile("glpsolRSA.dat", data, true);

		data = "param K:=\r\n";
		for (int i =0;i<sub.nodes;i++){
			if (sub.cpu[i]==sub.maxcpu[i]){
				data+=i+" 1\r\n";
			}else {
				data+=i+" 0\r\n";
			}

		}
		data+=";\r\n";
		myDowith.SaveFile("glpsolRSA.dat", data, true);

//		data="param p1:=1329.33;\r\n" +
//				"param p2:=120;\r\n" +
//				"param p3:=150;\r\n" +
//				"param p4:=0.465;\r\n" +
//				"param p5:=0.18;\r\n"+
//				"end;\r\n";
		data="param p1:=1329.33;\r\n" +//0.1
				"param p2:=120;\r\n" +
				"param p3:=150;\r\n" +
				"param p4:=0.465;\r\n" +
				"param p5:=0.18;\r\n"+
				"end;\r\n";
		myDowith.SaveFile("glpsolRSA.dat", data, true);


		System.out.println("Done");

		try {
			String s;
			Process process = null;

			if(Parameters.CurrentVONEMethod == Parameters.MapVONECXHNode){
				//process = Runtime.getRuntime().exec("cmd /c C:/������/VONE/VONE/glpk-4.60/w64/glpsol.exe -m C:/������/VONE/VONE/glpk-4.60/w64/glpsolMILPPRByLinChen.mod -d glpsolRSA.dat -o glpsolRSA.o");
				process = Runtime.getRuntime().exec("cmd /c C:/������/VONE/VNE/glpk-4.60/w64/glpsol.exe -m C:/������/VONE/VNE/glpk-4.60/w64/glpsolMILPByLinChenNodeUndirectDiagram.mod -d glpsolRSA.dat -o glpsolRSA.o");

			} else if(Parameters.CurrentVONEMethod == Parameters.MapVONE01ILPPRLinCXH){
				//process = Runtime.getRuntime().exec("cmd /c C:/������/VONE/VONE/glpk-4.60/w64/glpsol.exe -m C:/������/VONE/VONE/glpk-4.60/w64/glpsolMILPPRByLinChen.mod -d glpsolRSA.dat -o glpsolRSA.o");
				process = Runtime.getRuntime().exec("cmd /c C:/������/VONE/VNE/glpk-4.60/w64/glpsol.exe -m C:/������/VONE/VNE/glpk-4.60/w64/glpsolMILPPRByLinChenUndirectDiagram.mod -d glpsolRSA.dat -o glpsolRSA.o");
			} else if(Parameters.CurrentVONEMethod == Parameters.MapVONE01ILPLin_EquilibriumCXH){
				//process = Runtime.getRuntime().exec("cmd /c C:/������/VONE/VONE/glpk-4.60/w64/glpsol.exe -m C:/������/VONE/VONE/glpk-4.60/w64/glpsolMILPPRByLinChen.mod -d glpsolRSA.dat -o glpsolRSA.o");
				process = Runtime.getRuntime().exec("cmd /c C:/������/VONE/VNE/glpk-4.60/w64/glpsol.exe -m C:/������/VONE/VNE/glpk-4.60/w64/glpsolMILPByLinUndirectDiagram-Equilibrium.mod -d glpsolRSA.dat -o glpsolRSA.o");
				//process = Runtime.getRuntime().exec("cmd /c C:/������/VONE/VNE/glpk-4.60/w64/glpsol.exe -m C:/������/VONE/VNE/glpk-4.60/w64/glpsolMILPByLinUndirectDiagram-glpsolMILPByLinUndirectDiagram-Equilibrium.mod -d glpsolRSA.dat -o glpsolRSA.o");

			}else if (Parameters.CurrentVONEMethod == MapVONEILPByY_L){
				process =Runtime.getRuntime().exec("cmd  /c E:/���Թ�����/CEEA/VNE/glpk-4.60/w64/glpsol.exe -m E:/���Թ�����/CEEA/VNE/glpk-4.60/w64/glpsolMILByLin_LYY_ReviseByRitchie_2022_4_30_15.mod -d glpsolRSA.dat -o glpsolRSA.o");
			} else {
				process = Runtime.getRuntime().exec("cmd /c C:/������/VONE/VNE/glpk-4.60/w64/glpsol.exe -m C:/������/VONE/VNE/glpk-4.60/w64/glpsolMILPByLin.mod  -d glpsolRSA.dat -o glpsolRSA.o");
				//process = Runtime.getRuntime().exec("cmd /c C:/������/VONE/VONE/glpk-4.60/w64/glpsol.exe -m C:/������/VONE/VONE/glpk-4.60/w64/glpsolMILPByLin.mod  -d glpsolRSA.dat -o glpsolRSA.o");
				//process = Runtime.getRuntime().exec("cmd /c C:/������/VNE/VNE/VNE/glpk-4.60/w64/glpsol.exe -m C:/������/VNE/VNE/VNE/glpk-4.60/w64/glpsolMILPByLinUndirectDiagram.mod -d glpsolRSA.dat -o glpsolRSA.o");
				//process = Runtime.getRuntime().exec("cmd /c C:/������/VONE/VNE/glpk-4.60/w64/glpsol.exe -m C:/������/VONE/VNE/glpk-4.60/w64/glpsolMILPByLinUndirectDiagram.mod -d glpsolRSA.dat -o glpsolRSA.o");
			}
			//if(embedModelOrAlgo == Parameters.MapVONE3ByWangY){
			//	process = Runtime.getRuntime().exec("cmd /c C:/������/VONE/VONE/glpk-4.60/w64/glpsol.exe -m C:/������/VONE/VONE/glpk-4.60/w64/glpsolMILVONE3NodesNoDataWangY.mod -d glpsolRSA.dat -o glpsolRSA.o");
			//} else if(embedModelOrAlgo == Parameters.MapVONE3PByWangYAndChenxh){
			//	process = Runtime.getRuntime().exec("cmd /c C:/������/VONE/VONE/glpk-4.60/w64/glpsol.exe -m C:/������/VONE/VONE/glpk-4.60/w64/glpsolMILPVONE3NodesNoDataWangY.mod -d glpsolRSA.dat -o glpsolRSA.o");
			//}
			//Process process = Runtime.getRuntime().exec("cmd /c C:/������/VONE/VONE/glpk-4.60/w64/glpsol.exe -m C:/������/VONE/VONE/glpk-4.60/w64/glpsol01ILPVONE3PNodesWangY.mod -d glpsolRSA.dat -o glpsolRSA.o");
			//Process process = Runtime.getRuntime().exec("cmd /c C:/������/VONE/VONE/glpk-4.60/w64/glpsol.exe -m C:/������/VONE/VONE/glpk-4.60/w64/glpsolMILPVONE3NodesNoDataWangY.mod -d glpsolRSA.dat -o glpsolRSA.o");
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
	public boolean FindOptSoluByY_L(EOSubstrateNetwork sub,VONRequest reqs[],int index,int retNodeE[],LinkedList retVLinkE[])
	{
		BufferedReader reader = null;

		int keySNode1 = -1,keySNode2 = -1;
		int keyVNode1 = -1,keyVNode2 = -1,slotIndex=-1;
		//LinkedList<String> lList = new LinkedList<String>();
		for(int i=0;i<reqs[index].links;i++){
			retVLinkE[i] = new LinkedList<String>();
		}
		try {
			System.out.println("����Ϊ��λ��ȡ�ļ����ݣ�һ�ζ�һ���У�");
//			reader = new BufferedReader(new FileReader("glpsolRSA.o"));
			reader = new BufferedReader(new FileReader("glpsolRSA.o"));
			String tempString = null;

			int line = 1;
			//һ�ζ���һ�У�ֱ������nullΪ�ļ�����
			while ((tempString = reader.readLine()) != null) {
				//��ʾ�к� //
				if (line == 5 && tempString.indexOf("OPTIMAL") == -1) {  //˵��δ�ҵ����Ž�
					System.out.println("line " + line + ": " + tempString + "No Found the optimal resolvetion.");
					return false;
				}
				if (line == 6) {  //�ҵ����·������minLength
					//ȥ��ǰ��ո�ȥ��ǰ�棺"Objective:  shPath = ";ȥ�����棺"(MINimum)"
					tempString = tempString.replace("Objective:  slotANDenergyMin = ", "");
					tempString = tempString.replace("(MINimum)", "");
					tempString = tempString.trim();




					Tools myDowith = new Tools();
					String data1=tempString+"\n";
						myDowith.SaveFile("MIN09990001.txt", data1, true);

				} else if(line > 6 && tempString.indexOf(" V[") != -1){//˵���ҵ������Ž��x����
					//ȥ��ǰ��Ĳ��֣�3 x[0,2]       *              1             0             1
					//�Կո�ָ���ȡ��һ������
					String tmpStr = "";
					String tempString1 = tempString.trim();//268 Z[0,1,0,1,11,4]
					String tempString2 = reader.readLine();//*              0             0             1
					tmpStr = tempString2.substring(tempString2.indexOf("*")+1);
					tmpStr = tmpStr.trim();
					tmpStr = tmpStr.substring(0,tmpStr.indexOf(" "));
					//System.out.println("line " + line + ": " + tmpStr);
					if(Integer.parseInt(tmpStr) == 1){//˵���ҵ���һ����
						//�õ�һ���⸳ֵ��tmpStr������x[0,2]
						tempString = tempString.trim();
						tmpStr = tempString.substring(tempString.indexOf(" ")+1);

						//Z[0,1,0,1,11,4],����s��keyVNode1ӳ��Ľڵ㣬t��keyVNode2ӳ��Ľڵ�

						keyVNode1 = Integer.parseInt(tmpStr.substring(tmpStr.indexOf("[")+1, tmpStr.indexOf(",")));
						//System.out.println("keyNode1:"+keySNode1);
						tmpStr = tmpStr.substring(tmpStr.indexOf(",")+1);
						keyVNode2 = Integer.parseInt(tmpStr.substring(0, tmpStr.indexOf(",")));

						tmpStr = tmpStr.substring(tmpStr.indexOf(",")+1);
						keySNode1 = Integer.parseInt(tmpStr.substring(0, tmpStr.indexOf(",")));
						tmpStr = tmpStr.substring(tmpStr.indexOf(",")+1);
						keySNode2 = Integer.parseInt(tmpStr.substring(0, tmpStr.indexOf(",")));

						tmpStr = tmpStr.substring(tmpStr.indexOf(",")+1);
						slotIndex = Integer.parseInt(tmpStr.substring(0, tmpStr.indexOf(",")));

						tmpStr = tmpStr.substring(tmpStr.indexOf(",")+1);
						int md = Integer.parseInt(tmpStr.substring(0, tmpStr.indexOf("]")));
						int vLinkNo = GetLinkNum(reqs,index,keyVNode1,keyVNode2);
						//String strOb = keyVNode1+"-"+keyVNode2+"-"+slotIndex+"-"+md;
						String strOb = keySNode1+"-"+keySNode2+"-"+slotIndex+"-"+md;
						if(Parameters.DebugModel) WriteFilePlus("process.txt",strOb);
						//lList.add(strOb);
						retVLinkE[vLinkNo].add(strOb);// = lList;
						//int vLinkNo = GetLinkNum(reqs,index,keyVNode1,keyVNode2);
						//retRSlotIndex[vLinkNo][sLinkNo] = slotIndex;

						//retLinkE[keySNode1] = keySNode2;
					}
				} else if(line > 6 && tempString.indexOf(" F[") != -1){//˵���ҵ������Ž��x����
					String tmpStr = "";
					//System.out.println("line " + line + ": " + tempString);
					//String tempString1 = reader.readLine();
					String tempString1 = tempString.trim();

					tmpStr = tempString1.substring(tempString1.indexOf("*")+1);
					tmpStr = tmpStr.trim();
					//System.out.println("line " + line + ": " + tmpStr);

					tmpStr = tmpStr.substring(0,tmpStr.indexOf(" "));
					//System.out.println("line " + line + ": " + tmpStr);
					if(Integer.parseInt(tmpStr) == 1){//˵���ҵ���һ����
						//�õ�һ���⸳ֵ��tmpStr������x[0,2]
						//var f{(i,j) in E,(m,n) in Ev,s in Vf,t in Vf,k in MSet}, binary;
						tempString = tempString.trim();
						tmpStr = tempString.substring(tempString.indexOf(" ")+1);//ȥ��ǰ����к�
						//System.out.println("line " + line + ": " + tmpStr);
						//tmpStr = tmpStr.substring(0,tmpStr.indexOf(" "));		//�õ�f[i,j,m,n,s,t,k]
						//System.out.println("line " + line + ": " + tmpStr);
						int keyNode1 = -1;
						//keyNode1 = Integer.parseInt(tmpStr.substring(tmpStr.indexOf("[")+1, tmpStr.indexOf(",")));//�õ�f[i,j,m,n,s,t,k]��i
						//System.out.println("keyNode1:"+keyNode1);
						//M[5,1]
						keyVNode1 = Integer.parseInt(tmpStr.substring(tmpStr.indexOf("[")+1, tmpStr.indexOf(",")));
						//System.out.println("keyNode1:"+keySNode1);
						tmpStr = tmpStr.substring(tmpStr.indexOf(",")+1);
						keySNode1 = Integer.parseInt(tmpStr.substring(0, tmpStr.indexOf("]")));

						retNodeE[keyVNode1] = keySNode1;//����ڵ�keyVNode1��ӳ�䵽����ڵ�keySNode1��
					}
				}

				line++;
			}
			reader.close();
		} catch (IOException e) {
			return false;
			//e.printStackTrace();
		} finally {
			if (reader != null) {
				try {
					reader.close();
				} catch (IOException e1) {

				}
			}
		}
		return true;
	}
	//���ƣ�int FindVONEOptimalSolution(......)
	//���ܣ���01ILPģ��ӳ�����������, ����ɹ��򷵻�true��ret[],p[]
	//������
	//	      ret[]Ϊ���ص�ӳ������
	//        ret[0]=minSlotIndex(��Ƶ�ײ۵ĵ�λ)
	//        ret[1]=maxSlotIndex(��Ƶ�ײ۵ĸ�λ)
	//	      p[]Ϊӳ���·��
	////	  listΪӳ�������ڵ�
	//����ֵ��true���ɹ����أ�false��ʧ�ܷ���
	//�����ˣ�������
	//�������ڣ�2017-09-27

	public void AddLinksMapByILPBYY_L(EOSubstrateNetwork sub,VONRequest reqs[],int index,int retNodeE[],LinkedList retVLinkE[])
	{
		//0����ӳ�������ݽṹ�У�����p��ret���ݽṹ;
		int p[][] = new int[reqs[index].links][sub.nodes];
		int p1[][] = new int[reqs[index].links][sub.nodes];
		int ret[][] = new int[reqs[index].links][2];
		int[] retSlotSE = new int[reqs[index].links];
		int[] retSlotEE = new int[reqs[index].links];

		for(int i=0;i<reqs[index].links;i++){
			for(int j=0;j<sub.nodes;j++){
				p[i][j] = -1;//��ʼ��
				p1[i][j] = -1;//��ʼ��
			}
		}
		//�ֽ�retVLinkE[sLinkNo].add(strOb);
		for(int i=0;i<reqs[index].links;i++){
			if(retVLinkE[i] != null){
				for (Object ob: retVLinkE[i]) {
					String str = ob.toString();
					System.out.println(i+" value:"+ str);
					String sNode1 = str.substring(0,str.indexOf("-"));
					str = str.substring(str.indexOf("-")+1);
					String sNode2 = str.substring(0,str.indexOf("-"));
					str = str.substring(str.indexOf("-")+1);
					String slotIndex = str.substring(0,str.indexOf("-"));
					str = str.substring(str.indexOf("-")+1);
					String md = str;
					System.out.println(sNode1+"-"+sNode2+"-"+slotIndex+"-"+md);
					retSlotSE[i] = Integer.parseInt(slotIndex);//��ʼ��Ƶ�ײ�����
					double d = Math.ceil(reqs[index].link[i].bw/(12.5*Integer.parseInt(md)));
					int slotNum = (new Double(d)).intValue()+Parameters.GuardBand;//
					System.out.println("slotNum:"+(slotNum));
					if(Parameters.DebugModel){
						String str1 = "\r\nSlotNum:"+(slotNum);
						WriteFilePlus("process.txt",str1);
					}
					retSlotEE[i] = Integer.parseInt(slotIndex)+slotNum-1;//��ֹ��Ƶ�ײ�����
					if(retSlotEE[i]>sub.slotsNum) System.out.println("***error. retSlotEE["+i+"]:"+retSlotEE[i]+" slotIndex:"+slotIndex+" slotNum:"+slotNum);
					//ӳ���·��
					int iSNode1 = Integer.parseInt(sNode1);
					int iSNode2 = Integer.parseInt(sNode2);
					p1[i][iSNode1] = iSNode2;//��ʼ��
				}
			}
		}
		//����p
		int[] sNode = new int[sub.nodes];

		for(int i=0;i<reqs[index].links;i++){
			for(int ii=0;ii<sub.nodes;ii++){
				sNode[ii] = -1;
			}
			int embedNode1 = retNodeE[reqs[index].link[i].from];
			int embedNode2 = retNodeE[reqs[index].link[i].to];
			//p[i][embedNode1] =
			while(embedNode1 != embedNode2){
				if(p1[i][embedNode1] != -1 && sNode[embedNode1] == -1){
					p[i][embedNode1] = p1[i][embedNode1];
					sNode[embedNode1] = p1[i][embedNode1];
					embedNode1 = p1[i][embedNode1];
				} else {
					for(int j=0;j<sub.nodes;j++){
						if(p1[i][j] == embedNode1 && sNode[embedNode1] == -1){
							p[i][embedNode1] = j;
							sNode[embedNode1] = j;
							embedNode1 = j;
							break;
						}
					}
				}
			}
			p[i][embedNode1] = -1;
		}
		//��ӡpath
		if(Parameters.DebugModel){
			for(int i=0;i<reqs[index].links;i++){
				int embedNode1 = retNodeE[reqs[index].link[i].from];
				int embedNode2 = retNodeE[reqs[index].link[i].to];
				PrintPath(p[i],embedNode1,embedNode2);
			}
		}

		for(int i=0;i<reqs[index].links;i++){
			ret[i][0] = retSlotSE[i];
			//retSlotEE[i] = retSlotEE[i]-2;
			ret[i][1] = retSlotEE[i];
		}

		/*
		//����p
		CreateShortestPathFromKPaths(reqs,index,kShortestPath,virtualNodes,retLinkE,pathEff,p);
		*/

		//1���ڵ�ӳ�䣬����sub.slots;
		for(int i=0;i<reqs[index].links;i++){
			int snode1,snode2,vnode1,vnode2;
			vnode1 = reqs[index].link[i].to;
			vnode2 = reqs[index].link[i].from;
			snode1 = v2s[index].snode.get(vnode1);
			snode2 = v2s[index].snode.get(vnode2);
			UpdateSub(sub,snode1,snode2,ret[i],p[i]);
		}

		//2������S2VLink s2v_l[]
		int snodeMid1,snodeMid,sNode1,req_count;
		boolean find = false;
		for(int i=0;i<reqs[index].links;i++){
			snodeMid1 = reqs[index].link[i].to;
			sNode1 = reqs[index].link[i].from;
			snodeMid1 = v2s[index].snode.get(snodeMid1);
			sNode1 = v2s[index].snode.get(sNode1);
			while(p[i][snodeMid1] != -1) {
				find = true;
				snodeMid = p[i][snodeMid1];
				req_count = s2v_l[sub.linksNo[snodeMid][snodeMid1]].req_count;
				s2v_l[sub.linksNo[snodeMid][snodeMid1]].req.add(req_count,index);
				s2v_l[sub.linksNo[snodeMid][snodeMid1]].bw.add(req_count,reqs[index].link[i].bw);
				s2v_l[sub.linksNo[snodeMid][snodeMid1]].vlink.add(req_count,i);
				s2v_l[sub.linksNo[snodeMid][snodeMid1]].rest_bw -=  reqs[index].link[i].bw;
				s2v_l[sub.linksNo[snodeMid][snodeMid1]].req_count ++;

				snodeMid1 = snodeMid;
				if(snodeMid1 == sNode1) break;//�ӻ�㵽Դ���·���Ѿ�����
			}
		}
		if(find == false){
			for(int i=0;i<reqs[index].links;i++){
				snodeMid1 = reqs[index].link[i].from;
				sNode1 = reqs[index].link[i].to;
				snodeMid1 = v2s[index].snode.get(snodeMid1);
				sNode1 = v2s[index].snode.get(sNode1);
				while(p[i][snodeMid1] != -1) {
					snodeMid = p[i][snodeMid1];
					req_count = s2v_l[sub.linksNo[snodeMid][snodeMid1]].req_count;
					s2v_l[sub.linksNo[snodeMid][snodeMid1]].req.add(req_count,index);
					s2v_l[sub.linksNo[snodeMid][snodeMid1]].bw.add(req_count,reqs[index].link[i].bw);
					s2v_l[sub.linksNo[snodeMid][snodeMid1]].vlink.add(req_count,i);
					s2v_l[sub.linksNo[snodeMid][snodeMid1]].rest_bw -=  reqs[index].link[i].bw;
					s2v_l[sub.linksNo[snodeMid][snodeMid1]].req_count ++;

					snodeMid1 = snodeMid;
					if(snodeMid1 == sNode1) break;//�ӻ�㵽Դ���·���Ѿ�����
				}
			}
		}


		//3������v2s[]����·ӳ����Ϣ
		int pathLength = 0;
		find = false;

		for(int i=0;i<reqs[index].links;i++){
			snodeMid1 = reqs[index].link[i].to;
			sNode1 = reqs[index].link[i].from;
			snodeMid1 = v2s[index].snode.get(snodeMid1);
			System.out.println("snodeMid1:"+snodeMid1);
			sNode1 = v2s[index].snode.get(sNode1);
			pathLength = 0;
			LinkedList<Integer> link = new LinkedList<Integer>();
			while(p[i][snodeMid1] != -1) {
				find = true;
				snodeMid = p[i][snodeMid1];
				link.add(pathLength,snodeMid1);
				pathLength++;	//·������

				snodeMid1 = snodeMid;
				//if(snodeMid1 == sNode1) break;//�ӻ�㵽Դ���·���Ѿ�����
			}
			if(pathLength == 0){
				snodeMid1 = reqs[index].link[i].from;
				sNode1 = reqs[index].link[i].to;
				snodeMid1 = v2s[index].snode.get(snodeMid1);
				System.out.println("snodeMid1:"+snodeMid1);
				sNode1 = v2s[index].snode.get(sNode1);
				pathLength = 0;
				//LinkedList<Integer> link = new LinkedList<Integer>();
				while(p[i][snodeMid1] != -1) {
					snodeMid = p[i][snodeMid1];
					link.add(pathLength,snodeMid1);
					pathLength++;	//·������

					snodeMid1 = snodeMid;
					//if(snodeMid1 == sNode1) break;//�ӻ�㵽Դ���·���Ѿ�����
				}
			}
			link.add(pathLength,snodeMid1);

			SpathFlow pathFlow = new SpathFlow();
			pathFlow.link = link;
			pathFlow.len = pathLength;

			if(find){
				snodeMid1 = reqs[index].link[i].to;
			} else {
				snodeMid1 = reqs[index].link[i].from;
			}

			//snodeMid1 = reqs[index].link[i].from;
			snodeMid1 = v2s[index].snode.get(snodeMid1);
			System.out.println("vlink:"+i+" pathLength:"+pathLength+" snodeMid1:"+snodeMid1);
			for(int ii=0;ii<pathLength;ii++){
				snodeMid = p[i][snodeMid1];
				//System.out.print(snodeMid1+"-");
				snodeMid1 = snodeMid;
			}
			//System.out.print(snodeMid1);
			//System.out.println("");

			pathFlow.bw = reqs[index].link[i].bw;
			v2s[index].pathFlow.add(i,pathFlow);
			v2s[index].flowLen.add(i,1);//1����·����·ӳ�䣻i����i����·��
			v2s[index].startSlotNo.add(i,ret[i][0]);//�������ʼ����
			v2s[index].slotNum.add(i,ret[i][1]-ret[i][0]+1);	//�����Ƶ������

		}




		//����ӳ��ɹ���־
		v2s[index].map = Parameters.STATE_MAP_LINK;
		reqs[index].map = Parameters.STATE_MAP_LINK;
	}
}
