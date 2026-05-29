// ���ļ�����������µ�������
package Team.CloudStorage.EAVONE;

import Team.CloudStorage.EAVONE.DRLMD_VONE.StateGenerator;
import Team.CloudStorage.EAVONE.DRLMD_VONE.TrainingExperience;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.factory.Nd4j;

import java.io.IOException;
import java.util.*;

import static Team.CloudStorage.EAVONE.Parameters.*;
//import commons-beanutils;

//import org.apache.commons.beanutils.BeanUtils;


public class VONEByPageRank extends VNE {
	private static final double TRAIN_TEST_RATIO = 0.5; // 80%ѵ����20%����
	private int trainThreshold = 0;

	private int totalRequests = 0;
	private int successfulRequests = 0;
	private double totalRewardSum = 0.0;
	private int globalStep = 0;
	public static  ArrayList<Object> AList=new ArrayList<>();
	public static  ArrayList<Object> SubAList=new ArrayList<>();
	private DRLAgent drlAgent;


	// ����ѵ�����ݽṹ��
	class TrainingData {
		INDArray input;
		INDArray labels;
		double reward;

		public TrainingData(INDArray input, INDArray labels, double reward) {
			this.input = input;
			this.labels = labels;
			this.reward = reward;
		}
	}

	public void VONEEmbed(String inSNFile,String inVNsFileDir,int reqsNum,int delay) throws IOException
	{
		//����SN��VNs
		super.VONEEmbed(inSNFile, inVNsFileDir, reqsNum, delay);
		if(Parameters.CurrentVONEMethod == Parameters.MapVONEAM913){
			SubAList=SubNetGraph1(sub,2);
		}
		if(Parameters.CurrentVONEMethod == Parameters.MapVONE_ESE){
			AList=EG_Graph(sub);
		}

		// ����ѵ������ֵ
		trainThreshold = (int) (reqsNum * TRAIN_TEST_RATIO);
		V2SEmbed(sub,reqs,delay);//,Parameters.MapVONETranModel
	}

	private boolean isTrainingPhase(int requestIndex) {
		return requestIndex < trainThreshold;
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

		// ��ʼ��ȫ��ͳ�Ʊ���
		totalRequests = 0;
		successfulRequests = 0;
		totalRewardSum = 0.0;
		globalStep = 0;

		while (end < n || reqs[n-1].time+delay>time) {   //The value of n is the number of all the VNs.
			while (end < n && reqs[end].time < time) end++;
			for(sStart=0;sStart<n-1 && (reqs[sStart].time+delay)<time;sStart++) ;//˵���ҵ��˵�ǰ��С�Ŀ�ʼ������������
			//for(sStart=0;reqs[sStart].time<time;sStart++) ;
			start = sStart;
			System.out.println("sStart:" + sStart + " end:" + end);

			//Release the resources.
			ReleaseAllResourceAmongZeroToEnd(sub,reqs,end,time);
			//Set the expire of STATE_EXPIRE.
			SetExpireVNState(reqs,end,time,delay);
			//Allocate the resources.
			AllocateResources(sub,reqs,start, end); // ����Ҫ����ֵ

			Recordxiumian(sub);

			calculateCpu(sub);
			CalculateEnergyConsumption(sub, reqs, end, time); // �����ܺ�̼�ŷ�
			time += Parameters.TIME_INTERVAL;  //ʱ�䴰����һ����λ
		}

		Date endDate = new Date();//��¼ӳ�俪ʼ��ʱ��
		long interval = (endDate.getTime() - startDate.getTime())/1000;//��¼ʱ���룩

		// �������ͳ�ƽ����ʹ��ȫ�ֱ�����
		if (totalRequests > 0) {
			double successRate = (double) successfulRequests / totalRequests * 100.0;
			double avgReward = totalRewardSum / Math.max(1, globalStep); // ƽ����ʱ����
			System.out.printf("ӳ����� - ��������: %d, �ɹ�ӳ����: %d, �ɹ���: %.2f%%, �ܽ���: %.4f, ƽ����ʱ����: %.4f, ȫ�ֲ���: %d, ��ʱ: %d��%n",
					totalRequests, successfulRequests, successRate, totalRewardSum, avgReward, globalStep, interval);
		}
		if (drlAgent != null) {
			System.out.println("  Agent stats: " + drlAgent.getTrainingStats());
		}


		if (Parameters.DebugModel)
			System.out.println("RecordResultsOfVNE.");
		if(Parameters.CurrentVONEMethod == MapVONEDRLMD_VONE){
			RecordResultsOfVNE1(sub, reqs, interval, Parameters.CurrentVONEMethod,totalRequests,successfulRequests,totalRewardSum,globalStep,interval);
		}else {
			RecordResultsOfVNE(sub, reqs, interval, Parameters.CurrentVONEMethod);
		}
		if (Parameters.DebugModel)
			System.out.println("PrintfVNE.");
		//if(Parameters.DebugModel) PrintNodeEmbedding(reqs);
		//if(Parameters.DebugModel) PrintLinkEmbedding(reqs);
		//PrintVNE(sub, reqs);PrintResultOfVN(sub,reqs);
	}




	public static List<Integer> updateSubAList(ArrayList<Object> subAList, List<Integer> activatedNodeList) {
		// ��� subAList �Ƿ�������һ�����б���activatedNodeList��Ϊ��
		if (subAList == null || subAList.isEmpty() || activatedNodeList == null || activatedNodeList.isEmpty()) {
			System.out.println("SubAList or activatedNodeList is empty or null.");
			return null; // ���� null ��ʾ����ʧ��
		}

		// ����һ���µ����б����� activatedNodeList �еĽڵ�
		List<Integer> newFirstSubList = new ArrayList<>(activatedNodeList);

		// ���ظ��º�����б�
		return newFirstSubList;
	}
		// �����µ����б��Ա��滻 SubAList �еĵ�һ�����б�



	//
	private void AllocateResources(EOSubstrateNetwork sub,VONRequest reqs[],int start,int end) throws IOException
	{
		System.out.println("start:" + start + " end:" + end);
		// ��ǰ����ͳ�Ʊ������ֲ���
		int windowTotalRequests = end - start;
		int windowSuccessfulRequests = 0;
		double windowRewardSum = 0.0;
		int windowSteps = 0;
		totalRequests += windowTotalRequests;
		for(int i=start;i<end;i++){
			if(v2s[i].map == Parameters.STATE_NEW || v2s[i].map == Parameters.STATE_MAP_NODE_FAIL || v2s[i].map == Parameters.STATE_MAP_FAIL || v2s[i].map == Parameters.STATE_MAP_Link_FAIL) {
				ArrayList<Object> list = new ArrayList<Object>();  //��¼�ڵ�ӳ����
				int p[][] = new int[reqs[i].links][sub.nodes];
				int ret[][] = new int[reqs[i].links][4];
				//ret[][0]:����������·ӳ���������㣻ret[][1]:����������·ӳ��������յ�
				//ret[][2]:���ص���ʼƵ�ײۣ�ret[][3]:���ص�Ƶ�ײ�����
				v2s[i].tryMapTime ++;	//��¼ӳ�����
				if(reqs[i].topo == Parameters.TOPO_GENERAL || reqs[i].topo == Parameters.TOPO_STAR) {
					if(Parameters.CurrentVONEMethod == Parameters.MapVONEPageRank||Parameters.CurrentVONEMethod == Parameters.MapVONEPageRank_equilibrium){
						DebugVNE(sub,reqs,i,Parameters.CurrentVONEMethod,"before embed req "+i);
						if(MapVONEByEnTranModel(sub, reqs, i)!=-1){
							if(Parameters.DebugModel) Print_sub_slots(sub);
							v2s[i].map = Parameters.STATE_MAP_SUCC;
							reqs[i].map = Parameters.STATE_MAP_SUCC;
							DebugVNE(sub,reqs,i,Parameters.CurrentVONEMethod,"after embed succ req "+i);
						} else {
							v2s[i].map = Parameters.STATE_MAP_NODE_FAIL;
							reqs[i].map = Parameters.STATE_MAP_NODE_FAIL;
							DebugVNE(sub,reqs,i,Parameters.CurrentVONEMethod,"after embed fail req "+i);
						}
					} else if(Parameters.CurrentVONEMethod == Parameters.MapVONEEnergyByPageRank){
						DebugVNE(sub,reqs,i,Parameters.CurrentVONEMethod,"before embed req "+i);
						if(MapVONEByY_L(sub, reqs, i)!=-1){
							if(Parameters.DebugModel) Print_sub_slots(sub);
							v2s[i].map = Parameters.STATE_MAP_SUCC;
							reqs[i].map = Parameters.STATE_MAP_SUCC;
							DebugVNE(sub,reqs,i,Parameters.CurrentVONEMethod,"after embed succ req "+i);
						} else {
							v2s[i].map = Parameters.STATE_MAP_NODE_FAIL;
							reqs[i].map = Parameters.STATE_MAP_NODE_FAIL;
							DebugVNE(sub,reqs,i,Parameters.CurrentVONEMethod,"after embed fail req "+i);
						}
					}else if(Parameters.CurrentVONEMethod == Parameters.MapVONEEnergyByPageRankEasy){
						DebugVNE(sub,reqs,i,Parameters.CurrentVONEMethod,"before embed req "+i);
						if(MapVONEByEasy(sub, reqs, i)!=-1){
							if(Parameters.DebugModel) Print_sub_slots(sub);
							v2s[i].map = Parameters.STATE_MAP_SUCC;
							reqs[i].map = Parameters.STATE_MAP_SUCC;
							DebugVNE(sub,reqs,i,Parameters.CurrentVONEMethod,"after embed succ req "+i);
						} else {
							v2s[i].map = Parameters.STATE_MAP_NODE_FAIL;
							reqs[i].map = Parameters.STATE_MAP_NODE_FAIL;
							DebugVNE(sub,reqs,i,Parameters.CurrentVONEMethod,"after embed fail req "+i);
						}
					}else if(Parameters.CurrentVONEMethod == Parameters.MapVONEEnergyByVogelPageRank){
						DebugVNE(sub,reqs,i,Parameters.CurrentVONEMethod,"before embed req "+i);
						if(MapVONEByVogel(sub, reqs, i)!=-1){
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
					else if(Parameters.CurrentVONEMethod == Parameters.MapVONEPageRankOfGHG){
						DebugVNE(sub,reqs,i,Parameters.CurrentVONEMethod,"before embed req "+i);
						if(MapVONEByGHGPageRank(sub, reqs, i)!=-1){
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
					else if(Parameters.CurrentVONEMethod == Parameters.TestMethod){
						DebugVNE(sub,reqs,i,Parameters.CurrentVONEMethod,"before embed req "+i);
						if(MapVONETestMethod(sub, reqs, i)!=-1){
							if(Parameters.DebugModel) Print_sub_slots(sub);
							v2s[i].map = Parameters.STATE_MAP_SUCC;
							reqs[i].map = Parameters.STATE_MAP_SUCC;
							DebugVNE(sub,reqs,i,Parameters.CurrentVONEMethod,"after embed succ req "+i);
						} else {
							v2s[i].map = Parameters.STATE_MAP_NODE_FAIL;
							reqs[i].map = Parameters.STATE_MAP_NODE_FAIL;
							DebugVNE(sub,reqs,i,Parameters.CurrentVONEMethod,"after embed fail req "+i);
						}
					}else if(Parameters.CurrentVONEMethod == Parameters.MapVONEAM){
						DebugVNE(sub,reqs,i,Parameters.CurrentVONEMethod,"before embed req "+i);
						if(MapVONEAM(sub, reqs, i)!=-1){
							if(Parameters.DebugModel) Print_sub_slots(sub);
							v2s[i].map = Parameters.STATE_MAP_SUCC;
							reqs[i].map = Parameters.STATE_MAP_SUCC;
							DebugVNE(sub,reqs,i,Parameters.CurrentVONEMethod,"after embed succ req "+i);
						} else {
							v2s[i].map = Parameters.STATE_MAP_NODE_FAIL;
							reqs[i].map = Parameters.STATE_MAP_NODE_FAIL;
							DebugVNE(sub,reqs,i,Parameters.CurrentVONEMethod,"after embed fail req "+i);
						}
					}else if(Parameters.CurrentVONEMethod == Parameters.MapVONEAM913){
						DebugVNE(sub,reqs,i,Parameters.CurrentVONEMethod,"before embed req "+i);
						if(MapVONEAM913(sub, reqs, i)!=-1){
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

					else if(Parameters.CurrentVONEMethod == Parameters.MapVONEPageRankOfGHGByVogel||Parameters.CurrentVONEMethod == Parameters.MapVONEPageRankOfGHGByVogelPro){
						DebugVNE(sub,reqs,i,Parameters.CurrentVONEMethod,"before embed req "+i);
						if(MapVONEPageRankOfGHGByVogel(sub, reqs, i)!=-1){
							if(Parameters.DebugModel) Print_sub_slots(sub);
							v2s[i].map = Parameters.STATE_MAP_SUCC;
							reqs[i].map = Parameters.STATE_MAP_SUCC;
							DebugVNE(sub,reqs,i,Parameters.CurrentVONEMethod,"after embed succ req "+i);
						} else {
							v2s[i].map = Parameters.STATE_MAP_NODE_FAIL;
							reqs[i].map = Parameters.STATE_MAP_NODE_FAIL;
							DebugVNE(sub,reqs,i,Parameters.CurrentVONEMethod,"after embed fail req "+i);
						}
					}else if(Parameters.CurrentVONEMethod == Parameters.MapVONEPageRankOfGHGByVogelcenm){
						DebugVNE(sub,reqs,i,Parameters.CurrentVONEMethod,"before embed req "+i);
						if(MapVONEPageRankOfGHGByVogel1(sub, reqs, i)!=-1){
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
					else if(Parameters.CurrentVONEMethod == Parameters.MapVONE_ESE) {
						DebugVNE(sub, reqs, i, Parameters.CurrentVONEMethod, "before embed req " + i);
						if (MapVONEByESE_Wei(sub, reqs, i) != -1) {
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
					else if(Parameters.CurrentVONEMethod == Parameters.MapVONEDRLMD_VONE) {
						DebugVNE(sub, reqs, i, Parameters.CurrentVONEMethod, "before embed req " + i);
						if (MapVONDRLMD_VONE(sub, reqs, i) != -1) {//MapVONDRLMD_VONE
							if (Parameters.DebugModel) Print_sub_slots(sub);
							v2s[i].map = Parameters.STATE_MAP_SUCC;
							reqs[i].map = Parameters.STATE_MAP_SUCC;
							windowSuccessfulRequests++; // ���ӳɹ�����

							// ��ȡDRL����Ľ���ֵ
							if (drlAgent != null) {
								double reward = drlAgent.getLastReward();
								windowRewardSum += reward;
							}
							windowSteps++; // ���Ӳ���
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
		successfulRequests += windowSuccessfulRequests;
		totalRewardSum += windowRewardSum;
		globalStep += windowSteps;
	}

	/******************************************************************
	���ƣ�int MapVONEByTranModel(......)
	���ܣ�������ģ��ӳ�����������, ����ɹ������s2v_n��v2s
	������
		      s2v_nΪ����ڵ�ӳ�������ڵ����ݽṹ
		      s2v_lΪ������·ӳ��������·���ݽṹ
		      v2sΪ����ӳ��������������ݽṹ
		      indexΪ��index����������
	,int ret[],int p[][],ArrayList<Object> list
	����ֵ��0���ɹ����أ�-1��ʧ�ܷ���
	******************************************************************/
	private int MapVONEByTranModel(EOSubstrateNetwork sub,VONRequest reqs[],int index)
	{
		//��������ģ�ͺ���С���õ�Ƶ�ײ�����
		double[][] transModel = new double[reqs[index].nodes][sub.nodes];
		int[][] indexModel = new int[reqs[index].nodes][sub.nodes];
		int[][] linkModel = new int[reqs[index].nodes][sub.nodes];
		InitTranModel(sub,reqs,index,transModel,indexModel,linkModel);

		//��ʼ������,-1������δ���䣬>-1�����Ѿ�����Ľڵ������·��
		int[] vNodeEmbed = new int[reqs[index].nodes];
		int[] sNodeEmbed = new int[sub.nodes];
		int[] vLinkEmbed = new int[reqs[index].links];
		InitAllocModel(sub,reqs,index,vNodeEmbed,sNodeEmbed,vLinkEmbed);

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
		while(num < reqs[index].nodes){
			//Ѱ����СԪ�أ���������minElement[0]\minElement[1];minIndexReq��minIndexSub
			FindMinElement(subCopy,reqs,index,transModel,vNodeEmbed,sNodeEmbed,minElement);
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
				retOther[noEmbedVLink][1] = ret[noEmbedVLink][0]+ret[noEmbedVLink][1];
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
		AddLinksMapBySPFA(sub,reqs,index,ret,p);//���µײ�����

		return 0;//�ɹ�����
	}
	/******************************************************************
	���ƣ�int MapVONEByTranModel(......)
	���ܣ�������ģ��ӳ�����������, ����ɹ������s2v_n��v2s
	������
		      s2v_nΪ����ڵ�ӳ�������ڵ����ݽṹ
		      s2v_lΪ������·ӳ��������·���ݽṹ
		      v2sΪ����ӳ��������������ݽṹ
		      indexΪ��index����������
	,int ret[],int p[][],ArrayList<Object> list
	����ֵ��0���ɹ����أ�-1��ʧ�ܷ���
	******************************************************************/
	private int MapVONEByEnTranModel(EOSubstrateNetwork sub,VONRequest reqs[],int index)
	{
		//��������ģ�ͺ���С���õ�Ƶ�ײ�����
		double[][] transModel = new double[reqs[index].nodes][sub.nodes];
		int[][] indexModel = new int[reqs[index].nodes][sub.nodes];
		int[][] linkModel = new int[reqs[index].nodes][sub.nodes];
		 InitTranModel(sub,reqs,index,transModel,indexModel,linkModel);

		//��ʼ������,-1������δ���䣬>-1�����Ѿ�����Ľڵ������·��
		int[] vNodeEmbed = new int[reqs[index].nodes];
		int[] sNodeEmbed = new int[sub.nodes];
		int[] vLinkEmbed = new int[reqs[index].links];
		InitAllocModel(sub,reqs,index,vNodeEmbed,sNodeEmbed,vLinkEmbed);

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
		while(num < reqs[index].nodes){
			//Ѱ����СԪ�أ���������minElement[0]\minElement[1];minIndexReq��minIndexSub
			FindEnMinElement(subCopy,reqs,index,transModel,vNodeEmbed,sNodeEmbed,minElement);
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


	/******************************************************************
	���ƣ�void MapVONEByEnTranModel(......)
	���ܣ�������ģ��Ϊ��������ǿ������ģ��ӳ�����������, ����ɹ������s2v_n��v2s
	������
		      s2v_nΪ����ڵ�ӳ�������ڵ����ݽṹ
		      s2v_lΪ������·ӳ��������·���ݽṹ
		      v2sΪ����ӳ��������������ݽṹ
		      indexΪ��index����������
	,int ret[],int p[][],ArrayList<Object> list
	����ֵ��0���ɹ����أ�-1��ʧ�ܷ���
	******************************************************************/
	private int MapVONEByRuTranModel(EOSubstrateNetwork sub,VONRequest reqs[],int index)
	{
		//start ��ʼ��
		//��������ģ�ͺ���С���õ�Ƶ�ײ�����
		double[][] transModel = new double[reqs[index].nodes][sub.nodes];
		int[][] indexModel = new int[reqs[index].nodes][sub.nodes];
		int[][] linkModel = new int[reqs[index].nodes][sub.nodes];
		InitTranModel(sub,reqs,index,transModel,indexModel,linkModel);

		//��ʼ������,-1������δ���䣬>-1�����Ѿ�����Ľڵ������·��
		int[] vNodeEmbed = new int[reqs[index].nodes];
		int[] sNodeEmbed = new int[sub.nodes];
		int[] vLinkEmbed = new int[reqs[index].links];
		InitAllocModel(sub,reqs,index,vNodeEmbed,sNodeEmbed,vLinkEmbed);

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
		Clone(subCopy,sub);
		//end ��ʼ��

		//�ҵ�δӳ���������·vlink��Ӧ������ڵ�vq��Լ������������·�Ĵ����������һ������ڵ��Ѿ���ӳ��
		int evlink = 0,vp1,vp2,sp1,sp2,vlink;//evlinkӳ���������·������vp1��vp2��vlink���ҵ���δӳ���������·vlink�������˵�
		int[] vTwoNodeAndLink = new int[3];//vp1=vTwoNodeAndLink[0];vp2=vTwoNodeAndLink[1];vlink=vTwoNodeAndLink[2];

		while(FindNoEVlink(reqs,index,vLinkEmbed,vNodeEmbed,vTwoNodeAndLink)){
			vp1=vTwoNodeAndLink[0];
			vp2=vTwoNodeAndLink[1];
			vlink=vTwoNodeAndLink[2];
			if(vNodeEmbed[vp1]==-1 && vNodeEmbed[vp2]==-1){
				sp1 = FindSNodeByVNode(sub,vp1,transModel,sNodeEmbed);//�ҵ�vp1ӳ���sp1�ڵ�
				if(sp1 == -1){//ʧ�ܷ���
					return -1;
				}
				//sp2 = FindSNodeByVNodeIncludeLink(vp1,vp2,sp1,vlink,p);//�ҵ�vp2ӳ���sp2�ڵ�,�Ҷ�Ӧ����·ӳ��
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
			//Ԥ����
			//PreEmbedNodesAndLinks();
			evlink++;
		}
		if(evlink == reqs[index].links){
			//EmbedNodesAndLinks();
			return 1;
		} else {
			return -1;//ӳ��ʧ��
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
	private void FindMinElementByVogel(EOSubstrateNetwork sub,VONRequest reqs[],int index,double[][] transModel,int[] vnodeEmbed,int[] snodeEmbed,int[] minElent)
	{
		//Ѱ����СԪ�أ���������minIndexReq��minIndexSub
		minElent[0] = minElent[1] = -1;
		double minElement = 10000;
		double Difference[]=new double[sub.nodes+reqs[index].nodes];
		double lines[]=new double[sub.nodes];
		double rows[]=new double[reqs[index].nodes];
		for(int i=0;i<reqs[index].nodes;i++){
			double min =10000;
			int minN=-1;
			for (int j=0;j<sub.nodes;j++){
				if (min>transModel[i][j] && transModel[i][j]>-1 && vnodeEmbed[i]==-1 && snodeEmbed[j]==-1){
					min=transModel[i][j];
					minN=j;
				}
			}
			rows[i]=10000;
			for (int j=0;j<sub.nodes;j++){
				if (rows[i]>transModel[i][j]-min&&j!=minN){
					rows[i]=transModel[i][j]-min;
					Difference[i]=transModel[i][j]-min;
				}
			}
		}
		for (int j=0;j<sub.nodes;j++){
			double min = 10000;
			int minN=-1;
			for (int i =0;i< reqs[index].nodes;i++){
				if (min>transModel[i][j] && transModel[i][j]>-1 && vnodeEmbed[i]==-1 && snodeEmbed[j]==-1){
					min=transModel[i][j];
					minN=i;
				}
			}
			lines[j]=10000;
			for (int i=0;i<reqs[index].nodes;i++){
				if (lines[j]>transModel[i][j]-min&&i!=minN){
					lines[j]=transModel[i][j]-min;
					Difference[reqs[index].nodes+j]=transModel[i][j]-min;
				}
			}
		}
		double mi=10000;
		int r=-1;int l=-1;
		for(int i=0;i<reqs[index].nodes;i++){
			if (mi>rows[i]&&rows[i]!=10000){
				mi=rows[i];
				r=i;
			}
		}
		for(int i=0;i<sub.nodes;i++){
			if (mi>lines[i]&&lines[i]!=10000){
				mi=lines[i];
				r=-1;l=i;
			}
		}
		if (r!=-1&& l ==-1){
			for (int i=0;i<sub.nodes;i++ ){
				if (minElement>transModel[r][i]&&transModel[r][i]>-1&&snodeEmbed[i]==-1){
					minElent[0]=r;
					minElent[1]=i;
					minElement=transModel[r][i];
				}
			}
		}
		if (r==-1&& l !=-1){
			for (int i  =0;i<reqs[index].nodes;i++){
				if (minElement>transModel[i][l]&&transModel[i][l]>-1&&vnodeEmbed[i]>-1){
					minElent[0]=i;
					minElent[1]=l;
					minElement=transModel[i][l];
				}
			}
		}




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
		for(int i=0;i<reqs[index].nodes;i++){// i������ ����ڵ�
			for(int j=0;j<sub.nodes;j++){//j������ ����ڵ�
				//�ж��Ƿ�i�ڵ��Ƿ����Ѿ�ӳ�������ڵ�����
				for(int k=0;k<reqs[index].links;k++){// k ������������·
					//����ڵ�i ���� ������·k�����  ���� ������·k���յ�        ��ӳ��            ����ڵ�i����������·k���յ㲢�� ��㱻ӳ��
					if((i == reqs[index].link[k].from && vnodeEmbed[reqs[index].link[k].to] != -1) || (i == reqs[index].link[k].to && vnodeEmbed[reqs[index].link[k].from] != -1)){
						//��Сֵ���� i j   ij����-1 ��û��ӳ��
						if(minElement>transModel[i][j] && transModel[i][j]>-1 && vnodeEmbed[i]==-1 && snodeEmbed[j]==-1){//vnodeEmbed[i] == -1��ʾ����ڵ�iδ��ӳ��
							if(i == reqs[index].link[k].from) {//i����·k���յ�  othVNode ���ڽڵ�
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
				//�Ѿ���ӳ�������ڵ����ڵ�δ��ӳ�������ڵ����С�˼�
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
/******************************************************************
 ���ƣ�FindEnMinElementByVogel
 ����ֵ��     minElentΪ��СԪ�أ�minElent[0]:��СԪ������ڵ㣻minElent[1]:��СԪ������ڵ㣻
 ���ܣ�Vogel��
******************************************************************* */
	private void  FindEnMinElementByVogel(EOSubstrateNetwork sub,VONRequest reqs[],int index,double[][] transModel,int[] vnodeEmbed,int[] snodeEmbed,int[] minElent)
	{
		//Ѱ����СԪ�أ���������minIndexReq��minIndexSub
		double minElement = 10000;
		double[] maxElement = new double[2];
		int[] maxRow = new int[2];
		// �� �� ��
		maxRow[0] = maxRow[1] = -1;
		//maxElement[0]�����ֵ
		maxElement[0] = maxElement[1] = -10;
		minElent[0] = minElent[1] = -1;
		int othVNode, othSNode;
		minElement = 10000;
		int slotNoRe = Parameters.MAX_VALUE_INT;
		if(Parameters.CurrentVONEMethod == Parameters.MapVONEPageRankOfGHGByVogelPro){
			//*****************************************************************************************************
//			for (int i = 0; i < reqs[index].nodes; i++) {// i������ ����ڵ�
//				for (int k = 0; k < reqs[index].links; k++) {// k ������������·
//					if ((i == reqs[index].link[k].from && vnodeEmbed[reqs[index].link[k].to] != -1) || (i == reqs[index].link[k].to && vnodeEmbed[reqs[index].link[k].from] != -1)) {
//						if (maxElement[0]  <transModel[i][sub.nodes]){
//							maxElement[0]=transModel[i][sub.nodes];
//							minElement=10000;
//							minElent[0] =minElent[1] = -1;//minIndexReq = i;
//
//								for (int j = 0; j < sub.nodes; j++) {//j������ ����ڵ�
//									if (minElement > transModel[i][j] && transModel[i][j] > -1 && vnodeEmbed[i] == -1 && snodeEmbed[j] == -1) {//vnodeEmbed[i] == -1��ʾ����ڵ�iδ��ӳ��
//									int slotNoRe1 = CheckIfEnoughSlotsOnLink(sub, k, reqs[index].link[k].bw);
//							if (slotNoRe1 <=slotNoRe) {//
////									if(slotNoRe1>-1){//
//										minElent[0] = i;//minIndexReq = i;
//										minElent[1] = j;//minIndexSub = j;
////										maxElement[0]  = transModel[i][sub.nodes];
//										minElement = transModel[i][j];
//										slotNoRe = slotNoRe1;
//									}
//
//								}
//							}
//
//
//
//						}
//					}
//				}
//			}
			//*****************************************************************

			for (int i = 0; i < reqs[index].nodes; i++) {// i������ ����ڵ�
				for (int k = 0; k < reqs[index].links; k++) {// k ������������·
					if ((i == reqs[index].link[k].from && vnodeEmbed[reqs[index].link[k].to] != -1&&vnodeEmbed[i]==-1) || (i == reqs[index].link[k].to && vnodeEmbed[reqs[index].link[k].from] != -1&&vnodeEmbed[i]==-1)) {
					//����ӳ��ڵ���
						for (int j = 0; j < sub.nodes; j++) {//j������ ����ڵ�
							for (int l=0;l<sub.links;l++){
								if ((j==sub.link[l].from&&snodeEmbed[sub.link[l].to]!=-1&&snodeEmbed[j]==-1)||(j==sub.link[l].to&&snodeEmbed[sub.link[l].from]!=-1&&snodeEmbed[j]==-1)){
									if (minElement >= transModel[i][j] && transModel[i][j] != -1){
										minElent[0] = i;//minIndexReq = i;
										minElent[1] = j;//minIndexSub = j;
										minElement = transModel[i][j];
									}
								}
							}
						}
					}
				}
			}
		}else {
			for (int i = 0; i < reqs[index].nodes; i++) {// i������ ����ڵ�
				for (int j = 0; j < sub.nodes; j++) {//j������ ����ڵ�
					//�ж��Ƿ�i�ڵ��Ƿ����Ѿ�ӳ�������ڵ�����
					for (int k = 0; k < reqs[index].links; k++) {// k ������������·
						//����ڵ�i ���� ������·k�����  ���� ������·k���յ�        ��ӳ��            ����ڵ�i����������·k���յ㲢�� ��㱻ӳ��
						if ((i == reqs[index].link[k].from && vnodeEmbed[reqs[index].link[k].to] != -1) || (i == reqs[index].link[k].to && vnodeEmbed[reqs[index].link[k].from] != -1)) {
							//��Сֵ���� i j   ij����-1 ��û��ӳ��
							if (maxElement[0]<=transModel[i][sub.nodes]&&minElement >= transModel[i][j] && transModel[i][j] > -1 && vnodeEmbed[i] == -1 && snodeEmbed[j] == -1) {//vnodeEmbed[i] == -1��ʾ����ڵ�iδ��ӳ��
								if (i == reqs[index].link[k].from) {//i����·k���յ�  othVNode ���ڽڵ�
									othVNode = reqs[index].link[k].to;
								} else if (i == reqs[index].link[k].to) {
									othVNode = reqs[index].link[k].from;

								}
								int slotNoRe1 = CheckIfEnoughSlotsOnLink(sub, k, reqs[index].link[k].bw);

//								if(slotNoRe1>-1) {//
//									if(slotNoRe1>-1){//
//										minElent[0] = i;//minIndexReq = i;
//										minElent[1] = j;//minIndexSub = j;
////									maxElement[0]  = transModel[i][sub.nodes];
//										minElement = transModel[i][j];
//										slotNoRe = slotNoRe1;
									if (slotNoRe1 <= slotNoRe) {////
//								if(slotNoRe1>-1){//
										minElent[0] = i;//minIndexReq = i;
										minElent[1] = j;//minIndexSub = j;
									maxElement[0]  = transModel[i][sub.nodes];

										minElement = transModel[i][j];
										slotNoRe = slotNoRe1;
									}
//								}
							}
						}
					}
				}
			}
		}

		if (minElent[0] != -1) return;
		//Ѱ����СԪ��
		minElent[0] = minElent[1] = -1;
		minElement = 10000;
		maxElement[0]=maxElement[1]=-10;
		//�����ֵ
		for (int i = 0; i < reqs[index].nodes; i++) {
			if (maxElement[0] < transModel[i][sub.nodes]) {
				maxElement[0] = transModel[i][sub.nodes];
				maxRow[0] = i;

			}
		}
		//�����ֵ
		for (int i = 0; i < sub.nodes; i++) {
			if (maxElement[1] < transModel[reqs[index].nodes][i]) {
				maxElement[1] = transModel[reqs[index].nodes][i];
				maxRow[1] = i;
			}
		}
		if (maxElement[0] >= maxElement[1]) {
			for (int i = 0; i < sub.nodes; i++) {
				if (minElement > transModel[maxRow[0]][i] && transModel[maxRow[0]][i] != -1 && vnodeEmbed[maxRow[0]] == -1 && snodeEmbed[i] == -1) {
					minElement = transModel[maxRow[0]][i];
					minElent[0] = maxRow[0];
					minElent[1] = i;
				}
			}
		} else {
			for (int i = 0; i < reqs[index].nodes; i++) {
				if (minElement > transModel[i][maxRow[1]] && transModel[i][maxRow[1]] != -1 && vnodeEmbed[i] == -1 && snodeEmbed[maxRow[1]] == -1) {
					minElement = transModel[i][maxRow[1]];
					minElent[0] = i;
					minElent[1] = maxRow[1];
				}
			}
		}



	}


	/******************************************************************
	 ���ƣ�FindEnMinElementByVogel
	 ����ֵ��     minElentΪ��СԪ�أ�minElent[0]:��СԪ������ڵ㣻minElent[1]:��СԪ������ڵ㣻
	 ���ܣ�Vogel��
	 ******************************************************************* */
	private void FindDifferentElementByVogel(EOSubstrateNetwork sub,VONRequest reqs[],int index,double[][] transModel,int[] vnodeEmbed,int[] snodeEmbed,int[] minElent)
	{
		//Ѱ����СԪ�أ���������minIndexReq��minIndexSub
		double minElement = 10000;
		double vogelElement = 10000;
		double[] maxElement = new double[2];
		int[] vogelElent = new int[2];
		int[] maxRow = new int[2];
		// �� �� ��
		maxRow[0] = maxRow[1] = -1;
		//maxElement[0]�����ֵ
		maxElement[0] = maxElement[1] = -1;
		vogelElent[0] = vogelElent[1] = -1;
		int othVNode, othSNode;

		int slotNoRe = Parameters.MAX_VALUE_INT;

		//Ѱ����СԪ��
		minElent[0] = minElent[1] = -1;
		for (int i = 0; i < reqs[index].nodes; i++) {
			for (int j= 0; j < sub.nodes; j++) {
				if (minElement>transModel[i][j]&&transModel[i][j]>-1){
					minElent[0] = i;
					minElent[1] = j;
					minElement=transModel[i][j];
				}
			}
		}

		//�����ֵ
		for (int i = 0; i < reqs[index].nodes; i++) {
			if (maxElement[0] < transModel[i][sub.nodes]) {
				maxElement[0] = transModel[i][sub.nodes];
				maxRow[0] = i;

			}
		}
		//�����ֵ
		for (int i = 0; i < sub.nodes; i++) {
			if (maxElement[1] < transModel[reqs[index].nodes][i]) {
				maxElement[1] = transModel[reqs[index].nodes][i];
				maxRow[1] = i;
			}
		}
		if (maxElement[0] >= maxElement[1]) {
			for (int i = 0; i < sub.nodes; i++) {
				if (vogelElement > transModel[maxRow[0]][i] && transModel[maxRow[0]][i] != -1 && vnodeEmbed[maxRow[0]] == -1 && snodeEmbed[i] == -1) {
					vogelElement = transModel[maxRow[0]][i];
					vogelElent[0] = maxRow[0];
					vogelElent[1] = i;
				}
			}
		} else {
			for (int i = 0; i < reqs[index].nodes; i++) {
				if (vogelElement > transModel[i][maxRow[1]] && transModel[i][maxRow[1]] != -1 && vnodeEmbed[i] == -1 && snodeEmbed[maxRow[1]] == -1) {
					vogelElement = transModel[i][maxRow[1]];
					vogelElent[0] = i;
					vogelElent[1] = maxRow[1];
				}
			}
		}
		if (reqs[index].nodes<6){
			if (minElent[0]!=vogelElent[0]&&minElent[1]!=vogelElent[1]&&minElent[0]!=-1&&vogelElent[0]!=-1){
				Tools myDowith = new Tools();
				String data = VNsFileDir+" index : "+index+"Vogel:"+vogelElent[0]+"to"+vogelElent[1]+" cpu: "+reqs[index].cpu[vogelElent[0]]+" GHG:"+(reqs[index].cpu[vogelElent[0]]/sub.cpu[vogelElent[1]]*600+400)*sub.node_GHG[vogelElent[1]] +"    min:"+minElent[0]+"to"+minElent[1]+" cpu: "+reqs[index].cpu[minElent[0]]+" GHG:"+(reqs[index].cpu[minElent[0]]/sub.cpu[minElent[1]] *600+400)*sub.node_GHG[minElent[1]]+"\n";
				for (int i = 0; i <= reqs[index].nodes; i++) {
					for (int j = 0; j<=sub.nodes; j++) {
						data+=transModel[i][j]+"   " ;
					}
					data+="\n";
				}
				data+="\n\n\n";
				myDowith.SaveFile("theMinIWantNode0115.txt", data, true);

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
		//����pagerankֵ
		double vNodePageRank[] = new double[reqs[index].nodes];
		double sNodePageRank[] = new double[sub.nodes];
		//	InitVNodePageRank(reqs,index);

		vNodePageRank=InitVNodePageRank(vNodePageRank,reqs,index);
		sNodePageRank= InitSNodePageRank(sNodePageRank, sub);

		//��������ģ�ͺ���С��������·��
		int slotNum = -1;
		int link[] = new int[1];
		for(int i=0;i<reqs[index].nodes;i++){
			for(int j=0;j<sub.nodes;j++){
				if(reqs[index].cpu[i] <= s2v_n[j].rest_cpu + Parameters.MIN_VALUE_DOUBLE){//�ײ�ڵ��CPU��������ڵ�
					slotNum = CheckIfSlotEnoughByNode(sub,j,reqs,index,i,link);
					if( slotNum > -1){//�����ײ�ڵ�j�����ӵ���·Ƶ�ײ۴�������ڵ�i����Ĳ�
						transModel[i][j]=1.0/(s2v_n[j].rest_cpu -reqs[index].cpu[i]);
//						transModel[i][j] = Math.abs(vNodePageRank[i]-sNodePageRank[j]);//transModel[i][j] = 1.0/s2v_n[j].rest_cpu;//div(1.0,s2v_n[j].rest_cpu,10);//1.0/(1.0*s2v_n[j].rest_cpu);
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
	private void InitModel(EOSubstrateNetwork sub,VONRequest reqs[],int index,double[][] transModel,int[][] slotIndexModel,int[][] linkModel)
	{
		//����pagerankֵ
		double vNodePageRank[] = new double[reqs[index].nodes];
		double sNodePageRank[] = new double[sub.nodes];
		//	InitVNodePageRank(reqs,index);

		vNodePageRank=InitVNodeEnergyPageRank(vNodePageRank,reqs,index);
		sNodePageRank= InitSNodeEnergyPageRank(sNodePageRank, sub);

		//��������ģ�ͺ���С��������·��
		int slotNum = -1;
		int link[] = new int[1];
		for(int i=0;i<reqs[index].nodes;i++){
			for(int j=0;j<sub.nodes;j++){
				if(reqs[index].cpu[i] <= s2v_n[j].rest_cpu + Parameters.MIN_VALUE_DOUBLE){//�ײ�ڵ��CPU��������ڵ�
					slotNum = CheckIfSlotEnoughByNode(sub,j,reqs,index,i,link);
					if( slotNum > -1){//�����ײ�ڵ�j�����ӵ���·Ƶ�ײ۴�������ڵ�i����Ĳ�
						transModel[i][j] = Math.abs(vNodePageRank[i]-sNodePageRank[j]);//transModel[i][j] = 1.0/s2v_n[j].rest_cpu;//div(1.0,s2v_n[j].rest_cpu,10);//1.0/(1.0*s2v_n[j].rest_cpu);
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



//��ʼ��GHGģ��

	private void InitGHGModel(EOSubstrateNetwork sub,VONRequest reqs[],int index,double[][] transModel,int[][] slotIndexModel,int[][] linkModel)
	{
		//����pagerankֵ
		double vNodePageRank[] = new double[reqs[index].nodes];
		double sNodePageRank[] = new double[sub.nodes];
		//	InitVNodePageRank(reqs,index);

		vNodePageRank=InitVNodePageRankOfGHG(vNodePageRank,reqs,index);
		sNodePageRank= InitSNodePageRankOfGHG(sNodePageRank, sub);
		/*sNodePageRank= InitSNodeAM2(sNodePageRank, sub);
		double[] vCpuSorted = Arrays.copyOf(reqs[index].cpu, reqs[index].nodes);
		Integer[] vNodeIndexes = new Integer[reqs[index].nodes];
		for(int i=0; i<reqs[index].nodes; i++) vNodeIndexes[i] = i;

		// ʹ�ñȽ���ʵ�ֽ�������
		Arrays.sort(vNodeIndexes, (a, b) -> Double.compare(reqs[index].cpu[b], reqs[index].cpu[a]));

		// ��������������ڵ�PageRank����
		for(int i=0; i<reqs[index].nodes; i++){
			vNodePageRank[i] = vCpuSorted[vNodeIndexes[i]];
		}*/

		//��������ģ�ͺ���С��������·��
		int slotNum = -1;
		int link[] = new int[1];
		for(int i=0;i<reqs[index].nodes;i++){
			for(int j=0;j<sub.nodes;j++){
				if(reqs[index].cpu[i] <= s2v_n[j].rest_cpu + Parameters.MIN_VALUE_DOUBLE){//�ײ�ڵ��CPU��������ڵ�
					slotNum = CheckIfSlotEnoughByNode(sub,j,reqs,index,i,link);
					if( slotNum > -1){//�����ײ�ڵ�j�����ӵ���·Ƶ�ײ۴�������ڵ�i����Ĳ�
						transModel[i][j] = Math.abs(vNodePageRank[i]-sNodePageRank[j]);
//						transModel[i][j] = Math.abs(vNodePageRank[i]*reqs[index].nodes-sNodePageRank[j]*sub.nodes);//transModel[i][j] = 1.0/s2v_n[j].rest_cpu;//div(1.0,s2v_n[j].rest_cpu,10);//1.0/(1.0*s2v_n[j].rest_cpu);
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
	private void InitGHGModel1(EOSubstrateNetwork sub,VONRequest reqs[],int index,double[][] transModel,int[][] slotIndexModel,int[][] linkModel)
	{
		//����pagerankֵ
		double vNodePageRank[] = new double[reqs[index].nodes];
		double sNodePageRank[] = new double[sub.nodes];
		//	InitVNodePageRank(reqs,index);

		//vNodePageRank=InitVNodePageRankOfGHG(vNodePageRank,reqs,index);
		sNodePageRank= InitSNodePageRankOfGHG1(sNodePageRank, sub);
		//sNodePageRank= InitSNodeAM2(sNodePageRank, sub);
		double[] vCpuSorted = Arrays.copyOf(reqs[index].cpu, reqs[index].nodes);
		Integer[] vNodeIndexes = new Integer[reqs[index].nodes];
		for(int i=0; i<reqs[index].nodes; i++) vNodeIndexes[i] = i;

		// ʹ�ñȽ���ʵ�ֽ�������
		Arrays.sort(vNodeIndexes, (a, b) -> Double.compare(reqs[index].cpu[b], reqs[index].cpu[a]));

		// ��������������ڵ�PageRank����
		for(int i=0; i<reqs[index].nodes; i++){
			vNodePageRank[i] = vCpuSorted[vNodeIndexes[i]];
		}

		//��������ģ�ͺ���С��������·��
		int slotNum = -1;
		int link[] = new int[1];
		for(int i=0;i<reqs[index].nodes;i++){
			for(int j=0;j<sub.nodes;j++){
				if(reqs[index].cpu[i] <= s2v_n[j].rest_cpu + Parameters.MIN_VALUE_DOUBLE){//�ײ�ڵ��CPU��������ڵ�
					slotNum = CheckIfSlotEnoughByNode(sub,j,reqs,index,i,link);
					if( slotNum > -1){//�����ײ�ڵ�j�����ӵ���·Ƶ�ײ۴�������ڵ�i����Ĳ�
						transModel[i][j] = Math.abs(vNodePageRank[i]-sNodePageRank[j]);
//						transModel[i][j] = Math.abs(vNodePageRank[i]*reqs[index].nodes-sNodePageRank[j]*sub.nodes);//transModel[i][j] = 1.0/s2v_n[j].rest_cpu;//div(1.0,s2v_n[j].rest_cpu,10);//1.0/(1.0*s2v_n[j].rest_cpu);
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

	/******************************************************************
	 ���ƣ�int MapVONEByY_L(......)
	 ���ܣ�������ģ��ӳ�����������, ����ɹ������s2v_n��v2s
	 ������
	 s2v_nΪ����ڵ�ӳ�������ڵ����ݽṹ
	 s2v_lΪ������·ӳ��������·���ݽṹ
	 v2sΪ����ӳ��������������ݽṹ
	 indexΪ��index����������
	 ,int ret[],int p[][],ArrayList<Object> list
	 ����ֵ��0���ɹ����أ�-1��ʧ�ܷ���
	 ******************************************************************/
	private int MapVONEByY_L(EOSubstrateNetwork sub,VONRequest reqs[],int index)
	{
		//��������ģ�ͺ���С���õ�Ƶ�ײ�����
		double[][] transModel = new double[reqs[index].nodes][sub.nodes];
		int[][] indexModel = new int[reqs[index].nodes][sub.nodes];
		int[][] linkModel = new int[reqs[index].nodes][sub.nodes];
		InitModel(sub,reqs,index,transModel,indexModel,linkModel);

		//��ʼ������,-1������δ���䣬>-1�����Ѿ�����Ľڵ������·��
		int[] vNodeEmbed = new int[reqs[index].nodes];
		int[] sNodeEmbed = new int[sub.nodes];
		int[] vLinkEmbed = new int[reqs[index].links];
		InitAllocModel(sub,reqs,index,vNodeEmbed,sNodeEmbed,vLinkEmbed);

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
		while(num < reqs[index].nodes){
			//Ѱ����СԪ�أ���������minElement[0]\minElement[1];minIndexReq��minIndexSub
			FindEnMinElement(subCopy,reqs,index,transModel,vNodeEmbed,sNodeEmbed,minElement);
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


	private int MapVONEByGHGPageRank(EOSubstrateNetwork sub,VONRequest reqs[],int index)
	{
		//��������ģ�ͺ���С���õ�Ƶ�ײ�����
		double[][] transModel = new double[reqs[index].nodes][sub.nodes];
		int[][] indexModel = new int[reqs[index].nodes][sub.nodes];
		int[][] linkModel = new int[reqs[index].nodes][sub.nodes];
		InitGHGModel(sub,reqs,index,transModel,indexModel,linkModel);

		//��ʼ������,-1������δ���䣬>-1�����Ѿ�����Ľڵ������·��
		int[] vNodeEmbed = new int[reqs[index].nodes];
		int[] sNodeEmbed = new int[sub.nodes];
		int[] vLinkEmbed = new int[reqs[index].links];
		InitAllocModel(sub,reqs,index,vNodeEmbed,sNodeEmbed,vLinkEmbed);

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
		while(num < reqs[index].nodes){
			//Ѱ����СԪ�أ���������minElement[0]\minElement[1];minIndexReq��minIndexSub
			FindEnMinElement(subCopy,reqs,index,transModel,vNodeEmbed,sNodeEmbed,minElement);
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

	private int MapVONETestMethod(EOSubstrateNetwork sub,VONRequest reqs[],int index)
	{
		//��������ģ�ͺ���С���õ�Ƶ�ײ�����
		double[][] transModel = new double[reqs[index].nodes+1][sub.nodes+1];
		int[][] indexModel = new int[reqs[index].nodes][sub.nodes];
		int[][] linkModel = new int[reqs[index].nodes][sub.nodes];
		InitGHGModel(sub,reqs,index,transModel,indexModel,linkModel);


		//��ʼ������,-1������δ���䣬>-1�����Ѿ�����Ľڵ������·��
		int[] vNodeEmbed = new int[reqs[index].nodes];
		int[] sNodeEmbed = new int[sub.nodes];
		int[] vLinkEmbed = new int[reqs[index].links];
		InitAllocModel(sub,reqs,index,vNodeEmbed,sNodeEmbed,vLinkEmbed);

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
		while(num < reqs[index].nodes){
			CalculateTheDifference(sub,reqs,index,transModel,vNodeEmbed,sNodeEmbed);
			//Ѱ����СԪ�أ���������minElement[0]\minElement[1];minIndexReq��minIndexSub
			FindDifferentElementByVogel(subCopy,reqs,index,transModel,vNodeEmbed,sNodeEmbed,minElement);
			 return -1;//û���ҵ���СԪ��

		}


		return 0;//�ɹ�����
	}
//	public double[] InitSNodeAM(double sNodePageRank[], EOSubstrateNetwork sub) {
//
//		for (int i = 0;i<sub.nodes;i++){
//			sNodePageRank[i]=(sub.node_GHG[i]+sub.maxcpu[i]*sub.node_GHG[i])/sub.maxcpu[i];
//		}
//		return sNodePageRank;
//
//	}
	//��a�ǵ�������
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

	//��a��������
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
	private void AMnodeMapping_1(EOSubstrateNetwork sub,VONRequest reqs[],int index,int[] vnodeEmbed,int[] snodeEmbed,int[] minElent,int [] nSortVNode,int [] nSortSNode,List<Integer> subnet)
	{
		minElent[0] = minElent[1] = -1;
		for (int i = 0; i < reqs[index].nodes; i++) {
			for (int j = 0; j < subnet.size(); j++) {
				if (vnodeEmbed[nSortVNode[i]] == -1 && snodeEmbed[nSortSNode[j]] == -1 && reqs[index].cpu[nSortVNode[i]] < sub.cpu[nSortSNode[j]]) {
					minElent[0] = nSortVNode[i];//minIndexReq = i;
					minElent[1] = nSortSNode[j];//minIndexSub = j;
					return;
				}
			}
		}
	}

	private void AMnodeMapping(EOSubstrateNetwork sub,VONRequest reqs[],int index,int[] vnodeEmbed,int[] snodeEmbed,int[] minElent,int [] nSortVNode,int [] nSortSNode)
	{
		minElent[0] = minElent[1] = -1;
		for(int i=0;i<reqs[index].nodes;i++){
			for (int j=0;j<sub.nodes;j++){
				if (vnodeEmbed[nSortVNode[i]]==-1&&snodeEmbed[nSortSNode[j]]==-1&&reqs[index].cpu[nSortVNode[i]]<sub.cpu[nSortSNode[j]]){
					minElent[0] = nSortVNode[i];//minIndexReq = i;
					minElent[1] = nSortSNode[j];//minIndexSub = j;
					return ;
				}
			}
		}
	}
	//������ͼ̼�ŷ����Ӻͼ�����Դ�̵�ӳ��ģ��
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
				SubAList = bestState;
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
	// ... ԭ�д����е��������� ...
	private int MapVONEAM913(EOSubstrateNetwork sub,VONRequest reqs[],int index)
	{

		// ��MapVONEAM913��������Ӷ�̬�����߼�
		int reqNodes = reqs[index].nodes;
		int sMax = (int)(ALPHA * reqNodes + BETA);
		int sMin = (int)(GAMMA * reqNodes);


		//SubAList = SubNetGraph(sub, FirstNetNumber );
		boolean triedAllNodes = false;
		int method = 0;
		while(MapVONEAM913_1(sub,reqs,index,method) == -1&& triedAllNodes==false) {//ֻҪû��ӳ��ɹ�������ͼ��

			// ������Դ���
			double utilization = calculateUtilization(sub, SubAList);
			if(utilization < THETA_LOW && SubAList.size() > sMin) {
				restoreFromHistory(sub,index);  // �������˻���
			}
			SubAList = SubNetGraph2(sub, SubAList,index);
			// ������ʷ״̬����
			if(SubAList.size() <= sMax) {
				cacheHistoryState(SubAList);
			}

			//SubAList=SubNetGraph2(sub,SubAList );
			List<Integer> sum=(List<Integer>)SubAList.get(method);
			int jishu=0;
			for(int i=0;i< sum.size();i++){
				 jishu++;
			}
			if (jishu>=sub.nodes) {
				int mappingResult = MapVONEAM913_1(sub, reqs, index, method);
				triedAllNodes = true;
				if (mappingResult == 0) {
					System.out.println("embed reqs " + index + " successfully after using all nodes.");
					return 0;
				} else {
					return -1;//ӳ��ʧ��
				}
			}
		}
		return 0;
	}
	public ArrayList<Object> SubNetGraph2(EOSubstrateNetwork sub, ArrayList<Object> resultFromSubNetGraph1,int index) {
		// ��ȡ���е��������б��������������
		ArrayList<Integer> SubNet1 = (ArrayList<Integer>) resultFromSubNetGraph1.get(0);
		double[] energysNodePageRank = (double[]) resultFromSubNetGraph1.get(1);

		// ���³�ʼ���������Ի�ȡ���µ���������

		int[] nSortSNode = new int[sub.nodes];
		double sNodeAM1[] = new double[sub.nodes];
		//sNodeAM1= InitSNodeAM(sNodeAM1, sub);
		//sNodeAM1= InitSNodeAMnew1(sNodeAM1, SubNet1, index);
		//sNodeAM1= InitSNodeAMnew(sNodeAM1, sub);
		for (int i = 0; i < sub.nodes; i++) {
			sNodeAM1[i]=energysNodePageRank[i];
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

	// �������������ڼ��ڵ��Ƿ�����ͼSubNet1�е���һ�ڵ�������
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

      //vNodeAM=InitVNodePageRankOfGHG(vNodeAM,reqs,index);

		//sNodeAM= InitSNodeAMnew(sNodeAM, sub);

		//sNodeAM= InitSNodeAM(sNodeAM, sub);
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
	private int MapVONEAM913_1(EOSubstrateNetwork sub,VONRequest reqs[],int index,int method)
	{
		List<Integer> subnet=(List<Integer>)SubAList.get(method);

		double[][] transModel = new double[reqs[index].nodes+1][sub.nodes+1];
		int[][] indexModel = new int[reqs[index].nodes][sub.nodes];
		int[][] linkModel = new int[reqs[index].nodes][sub.nodes];
		//����AMֵ
		//double sNodeAM[] = new double[sub.nodes];
		//	InitVNodePageRank(reqs,index);
		int nSortVNode[]=new int[reqs[index].nodes];
		int nSortSNode[] = new int[subnet.size()];

		double sNodeAM1[] = new double[sub.nodes];

		//sNodeAM1= InitSNodeAM1(sNodeAM1, subnet);
		sNodeAM1= InitSNodeAMnew1(sNodeAM1, subnet, index);
		sortA3(sNodeAM1,nSortSNode,subnet);
//		for(int i=0;i<subnet.size();i++) {
//			nSortSNode[i] = subnet.get(i);
//		}
		//sortA(sNodeAM1,nSortSNode);
		//vNodeAM=InitVNodePageRankOfGHG(vNodeAM,reqs,index);
		//sNodeAM= InitSNodeAM(sNodeAM, sub);
		sort(reqs[index].cpu,nSortVNode);
		//sortA(sNodeAM,nSortSNode);
		//��ʼ������,-1������δ���䣬>-1�����Ѿ�����Ľڵ������·��
		int[] vNodeEmbed = new int[reqs[index].nodes];
		int[] sNodeEmbed = new int[sub.nodes];
		int[] vLinkEmbed = new int[reqs[index].links];
		InitAllocModel(sub,reqs,index,vNodeEmbed,sNodeEmbed,vLinkEmbed);

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
		int j = 0;
		//for(int i = 0;i < subnet.size() && j < sub.nodes; i++){
			///nSortSNode[j] = subnet.get(i);
			//j++;
	//	}
		while(num < reqs[index].nodes){

// ȷ��numû�г���nSortSNode����Ĵ�С
//			if (num > sub.nodes) {
//				// ���������ߵ��������С
//			}
			//Ѱ�ҽڵ�ӳ�䣬��������minElement[0]\minElement[1];minIndexReq��minIndexSub
			AMnodeMapping_1(subCopy,reqs,index,vNodeEmbed,sNodeEmbed,minElement,nSortVNode,nSortSNode,subnet);
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
		//����ALSET

		return 0;//�ɹ�����
	}

	private static int[] sortA2(double[] sNodeAM1, int[] nSortSNode, List<Integer> subnet) {
		// ������ʱ����洢��������
		Integer[] temp = new Integer[subnet.size()];
		for (int i = 0; i < temp.length; i++) {
			temp[i] = i; // ������ 0, 1, 2, ..., subnet.size()-1 ����temp����
		}

		// ʹ��Arrays.sort���н������򣬸���sNodeAM1�����ֵ����temp����
		Arrays.sort(temp, new Comparator<Integer>() {
			@Override
			public int compare(Integer o1, Integer o2) {
				// ����sNodeAM1��ֵ��������ע��Ƚ�ʱ������Ӧ��˳����o2, o1
				return Double.compare(sNodeAM1[o2], sNodeAM1[o1]) * -1; // ����-1ʵ�ֽ�������
			}
		});

		// ���������������������������

		for (int i = 0; i < temp.length; i++) {
			nSortSNode[i] = subnet.get(temp[i]); // ����������temp����ȡsubnet�е�ֵ
		}

		// ����������������������
		return nSortSNode;
	}


	private static int[] sortA3(double[] sNodeAM1, int[] nSortSNode, List<Integer> subnet) {
		// ɸѡ�� sNodeAM1 �з� 0 ֵ������
		List<Integer> nonZeroIndices = new ArrayList<>();
		for (int i = 0; i < sNodeAM1.length; i++) {
			if (sNodeAM1[i] != 0) {
				nonZeroIndices.add(i);
			}
		}

		// �Է� 0 ֵ���������� sNodeAM1 ��Ӧ��ֵ������������
		nonZeroIndices.sort(Comparator.comparingDouble(index -> sNodeAM1[index]));

		// ��������������䵽 nSortSNode ����
		for (int i = 0; i < nonZeroIndices.size(); i++) {
			nSortSNode[i] = nonZeroIndices.get(i);
		}

		// ������������������
		return Arrays.copyOf(nSortSNode, nonZeroIndices.size());
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
	//����̼�ŷ����Ӻͼ�����Դ�̵�ӳ��ģ��
	private int MapVONEAM(EOSubstrateNetwork sub,VONRequest reqs[],int index)
	{
		//��������ģ�ͺ���С���õ�Ƶ�ײ�����
		double[][] transModel = new double[reqs[index].nodes+1][sub.nodes+1];
		int[][] indexModel = new int[reqs[index].nodes][sub.nodes];
		int[][] linkModel = new int[reqs[index].nodes][sub.nodes];
		//����AMֵ
		double sNodeAM[] = new double[sub.nodes];
		//	InitVNodePageRank(reqs,index);
		int nSortVNode[]=new int[reqs[index].nodes];
		int nSortSNode[] = new int[sub.nodes];

//		vNodeAM=InitVNodePageRankOfGHG(vNodeAM,reqs,index);
		sNodeAM= InitSNodeAM(sNodeAM, sub);
		sort(reqs[index].cpu,nSortVNode);
		sortA(sNodeAM,nSortSNode);
		//��ʼ������,-1������δ���䣬>-1�����Ѿ�����Ľڵ������·��
		int[] vNodeEmbed = new int[reqs[index].nodes];
		int[] sNodeEmbed = new int[sub.nodes];
		int[] vLinkEmbed = new int[reqs[index].links];
		InitAllocModel(sub,reqs,index,vNodeEmbed,sNodeEmbed,vLinkEmbed);

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
		while(num < reqs[index].nodes){
			//Ѱ�ҽڵ�ӳ�䣬��������minElement[0]\minElement[1];minIndexReq��minIndexSub
			AMnodeMapping(subCopy,reqs,index,vNodeEmbed,sNodeEmbed,minElement,nSortVNode,nSortSNode);
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
	private int[] GetTwoNodesDegree(int linkindex,VONRequest reqs[],int index,int[] vNodesDegree)
	{
		int[] TwoNode=new int[2];
		for(int i=0;i<reqs[index].nodes;i++){
			vNodesDegree[i] = 0;//��ʼ��
		}
		for(int j=0;j<reqs[index].links;j++){
			if(reqs[index].link[j].bw > 0){
				vNodesDegree[reqs[index].link[j].from] ++;
				vNodesDegree[reqs[index].link[j].to] ++;
			}
		}
		if(vNodesDegree[reqs[index].link[linkindex].from]>vNodesDegree[reqs[index].link[linkindex].to]){
			TwoNode[0]=reqs[index].link[linkindex].from;
			TwoNode[1]=reqs[index].link[linkindex].to;
		}else{
			TwoNode[0]=reqs[index].link[linkindex].to;
			TwoNode[1]=reqs[index].link[linkindex].from;
		}
		return TwoNode;
	}
	//��ȡ�������������Ľڵ����
	private int GetMaxNodeDegree(VONRequest reqs[],int index,int[] vNodesDegree)
	{
		int MaxvNodeDegree=0;
		for(int i=0;i<reqs[index].nodes;i++){
			vNodesDegree[i] = 0;//ÿ���ڵ�Ķ�����ʼ��
		}
		for(int j=0;j<reqs[index].links;j++){
			if(reqs[index].link[j].bw > 0){
				vNodesDegree[reqs[index].link[j].from] ++;
				vNodesDegree[reqs[index].link[j].to] ++;
			}
		}
		for (int i = 0; i < vNodesDegree.length; i++) {
			if (vNodesDegree[i] > MaxvNodeDegree) {
				MaxvNodeDegree = vNodesDegree[i];
			}
		}
		return MaxvNodeDegree;
	}
	private static void sortIndex(double[] a, int[] b) {
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
	public static double[][] getShortestPath(double[][] adjMatrix, int n, int k) {
		int m = n * (n - 1) / 2;
		double[][] edges = new double[m][3];
		int p = 0;
		for (int i = 0; i < n; i++) {
			for (int j = i + 1; j < n; j++) {
				edges[p][0] = i;
				edges[p][1] = j;
				edges[p][2] = adjMatrix[i][j];
				p++;
			}
		}
		Arrays.sort(edges, new Comparator<double[]>() {
			@Override
			public int compare(double[] o1, double[] o2) {
				return Double.compare(o1[2], o2[2]);
			}
		});
		double[][] result = new double[k][2];
		int count = 0;
		for (int i = 0; i < m && count < k; i++) {
			if (edges[i][0] != edges[i][1]) {
				result[count][0] = edges[i][0];
				result[count][1] = edges[i][1];
				count++;
			}
		}
		return result;
	}
	//���ݾ����ȫ���Ӿ����AKֵ��ȫ���Ӿ���������Ҫ���ݾ��룬��θ���akֵ������ֵ��EGͼ�е���·�����ڵ�[i,j]
	public static double[][] getShortestPath(double[][] adjMatrixA, double[][] adjMatrixB, int n, int k) {
		int m = n * (n - 1) / 2;
		double[][] edges = new double[m][4];
		int p = 0;
		for (int i = 0; i < n; i++) {
			for (int j = i + 1; j < n; j++) {
				edges[p][0] = i;
				edges[p][1] = j;
				edges[p][2] = adjMatrixA[i][j];
				edges[p][3] = adjMatrixB[i][j];
				p++;
			}
		}
		Arrays.sort(edges, new Comparator<double[]>() {
			@Override
			public int compare(double[] o1, double[] o2) {
				if (o1[2] != o2[2]) {
					return Double.compare(o1[2], o2[2]);
				} else {
					return Double.compare(o2[3], o1[3]);
				}
			}
		});
		double[][] result = new double[k][2];
		int count = 0;
		for (int i = 0; i < m && count < k; i++) {
			if (edges[i][0] != edges[i][1]) {
				result[count][0] = edges[i][0];
				result[count][1] = edges[i][1];
				count++;
			}
		}
		return result;
	}
	//��ȡEGͼ����·��AKֵ���������˵��AKֵ֮�ͣ�i,jΪ��·���˽ڵ�
	public double getLinkAKvalue(int i,int j,int KD){
		double[] lenghi=new double[sub.nodes];
		int[] bwsortindexi=new int[sub.nodes];
		double[] lenghj=new double[sub.nodes];
		int[] bwsortindexj=new int[sub.nodes];
		double sumleng=0;
		double[][] array_length=(double[][])AList.get(0);
		for(int k=0;k< sub.nodes;k++){
			lenghi[k]=array_length[i][k];
			bwsortindexi[k]=0;
		}
		sortIndex(lenghi, bwsortindexi);
		for(int k=0;k< sub.nodes;k++){
			lenghj[k]=array_length[j][k];
			bwsortindexj[k]=0;
		}
		sortIndex(lenghj, bwsortindexj);
		for(int g=0;g<KD;g++){//bwsortindexi[g]��ʾg���̵���·
			sumleng=lenghi[bwsortindexi[g]]+lenghj[bwsortindexj[g]]+sumleng;
		}
		double averagelengh=sumleng/KD;
		return averagelengh;
	}
	//��ȡEGͼ�е��ڵ��AKֵ
	public double getDegreeAKvalue(int i,int KD){
		double[] lenghi=new double[sub.nodes];
		int[] bwsortindexi=new int[sub.nodes];
		double sumleng=0;
		double[][] array_length=(double[][])AList.get(0);
		for(int k=0;k< sub.nodes;k++){
			lenghi[k]=array_length[i][k];
			bwsortindexi[k]=0;
		}
		sortIndex(lenghi, bwsortindexi);
		for(int g=0;g<KD;g++){//bwsortindexi[g]��ʾg���̵���·
			sumleng=lenghi[bwsortindexi[g]]+sumleng;
		}
		double averagelengh=sumleng/KD;
		return averagelengh;
	}
	//��ӳ��
	private int  SecondaryMapping(EOSubstrateNetwork sub,VONRequest reqs[],int index){

		//��ʼ������,-1������δ���䣬>-1�����Ѿ�����Ľڵ������·��
		int[] vNodeEmbed = new int[reqs[index].nodes];
		int[] sNodeEmbed = new int[sub.nodes];
		int[] vLinkEmbed = new int[reqs[index].links];
		double[] vLinkBW = new  double[reqs[index].links];

		int []slotInPath=new int[reqs[index].links];
		int[] vSortLink = new  int[reqs[index].links];
		InitAllocModel(sub,reqs,index,vNodeEmbed,sNodeEmbed,vLinkEmbed);
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
		Clone(subCopy,sub);
		int[] sNodeSet= new  int [sub.nodes];
		//������ڵ����MRR����
		int[] vNodeSet = new int[reqs[index].nodes];
		SortVNodeByMRR(reqs,index,vNodeSet);
		SortSNodeByMRCC(sub,sNodeSet);
		for (int i=0;i<reqs[index].nodes;i++){
			int flag=0;
			for (int j = 0; j <sub.nodes;j++ ) {
				if (vNodeEmbed[i]==-1&&sNodeEmbed[j]==-1){
					if (reqs[index].cpu[i]<=sub.cpu[j]){
						vNodeEmbed[i]=j;
						sNodeEmbed[j]=i;
						UpdateSub(subCopy,j,reqs[index].cpu[i]);
						flag=1;
					}
				}
			}
			if (flag==0){

				return -1;//����ӳ��ʧ��
			}
		}
		//��������·���ݴ�������
		for (int i= 0; i < reqs[index].links;i++) {
			vLinkBW[i]=reqs[index].link[i].bw;
		}
		sort(vLinkBW,vSortLink);
		for (int noEmbedVLink= 0; noEmbedVLink < reqs[index].links;noEmbedVLink++){
			int SuccessFlag2=0;
			//������·δ��ӳ��
			if (vLinkEmbed[vSortLink[noEmbedVLink]]==-1){
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
				vLinkEmbed[noEmbedVLink] = 1;
				//��
//				int endVNode=0;
//				int startVNode=0;
//				endVNode=reqs[index].link[vSortLink[k]].to;
//				startVNode=	reqs[index].link[vSortLink[k]].from;
//				String[][]  array=(String[][]) AList.get(3);
//				int[] path=Getpath_array(array[vNodeEmbed[startVNode]][vNodeEmbed[endVNode]]);
//				double[][]  arraylengh=(double[][]) AList.get(0);
//				double pathlen=arraylengh[vNodeEmbed[startVNode]][vNodeEmbed[endVNode]];
//				int a=path[1];
//				for (int i = 0; i <path.length-1; i++) {
//				    p[k][path[i]]=path[i+1];
//				}
//				p[k][path[path.length]]=-1;
//				slotInPath[k]=CalculateSlots(reqs[index].link[k].bw,pathlen);
//				if (slotInPath[k] <= 0) continue;
//				//����Ƿ�����������Ҫ��
//
//				int findSlotIndex = -1;
//
////				findSlotIndex = CheckIfEnoughSlotsOnPathnew(sub, kSPath[i], 0, slotInPath[k], sNode1, sNode2);//Ѱ������Ҫ���Ƶ�ײ�����
//				if (findSlotIndex == -1) {
//					continue;//��ǰ·����Ƶ�ײ�����0��ʼ��û���ҵ���Ӧ������Ƶ�ײ�
//				}
//				ret[k][0] = findSlotIndex;
//				ret[k][1] = slotInPath[k];
//				break;//�ҵ���·��i��Ƶ�ײ�����findSlotIndex

			}
		}
		//���������·û��ӳ�䣬��ʧ�ܷ���
		for(int i=0;i<reqs[index].links;i++){
			if(vLinkEmbed[i] == -1) return -1;//ʧ�ܷ���
		}
		//����cpu
		//����cpu
		UpdateSub(sub,subCopy);
		//��¼�ڵ����·ӳ����
		AddNodesMap(reqs,index,vNodeEmbed);//����s2v_n��v2s
		AddLinksMapBySPFA(sub,reqs,index,retOther,p);//���µײ�����

		//���µײ�����slots
		UpdateSubSlots(sub,subCopy);

		return 0;//���سɹ�
	}


	/****
	 * ������ڵ����mrr�ǵ�������
	 * @param reqs
	 * @param index
	 * @param
	 */
	private void  SortVNodeByMRR(VONRequest reqs[],int index,int[] vSortNode){
		//��������ڵ��MRRֵ
		double[] MRR_VNode=new  double[reqs[index].nodes];
		double totalCPUOfVirtualNodes=0;
		double[] bwOfLinksNearNodes=new double[reqs[index].nodes];
		double totalBWOfVirtualNodes=0;

		for (int i = 0; i < reqs[index].nodes; i++){
			//������cpu
			totalCPUOfVirtualNodes +=reqs[index].cpu[i];
			//����ڵ㸽��BW
			for (int k = 0; k <reqs[index].links;k++ ) {
				if (reqs[index].link[k].from==i||reqs[index].link[k].to==i) {
					bwOfLinksNearNodes[i]+=reqs[index].link[k].bw;
				}
			}
			totalBWOfVirtualNodes+=bwOfLinksNearNodes[i];
		}

		for (int i = 0; i <reqs[index].nodes;i++) {
			MRR_VNode[i]=reqs[index].cpu[i]/totalBWOfVirtualNodes*bwOfLinksNearNodes[i]/totalBWOfVirtualNodes;
		}
		sort(MRR_VNode,vSortNode);
	}

	/***
	 *
	 * ����MRCC������ڵ���з���������
	 * @param sub
	 * @param nSortSNodes
	 */

	private void SortSNodeByMRCC(EOSubstrateNetwork sub,int[]nSortSNodes){
		//��������ڵ���CPU
		double totalCPUOfPhysicalNodes=0;
		//����Ƶ�ײ�
		double[] availableSpectrumSlots=new double[sub.links];
		double[] Debris=new double[sub.links];
		//ASC
		double[] ASC= new double[sub.links];
		double[]B= new double[sub.links];
		double []ASCAroundNodes=new double[sub.nodes];
		double AllASCAroundNodes=0;
		double[] MRCC=new  double[sub.nodes];
		for (int k =0;k<sub.links;k++){
			for (int s=0;s<sub.slotsNum;s++){
				availableSpectrumSlots[k]+=sub.slots[k][s];
			}
			for (int s = 0; s < sub.slotsNum-1; s++){
				Debris[k]+=sub.slots[k][s]*sub.slots[k][s+1];
			}
			B[k]=availableSpectrumSlots[k]-Debris[k];
			ASC[k]=availableSpectrumSlots[k] /sub.slotsNum/B[k];
		}

		for (int i =0;i<sub.nodes;i++){
			totalCPUOfPhysicalNodes += sub.cpu[i];
			for (int k = 0;k< sub.links;k++){
				if (sub.link[k].from==i||sub.link[k].to == i){
					ASCAroundNodes[i]+=ASC[k];
				}
			}
			AllASCAroundNodes+=ASCAroundNodes[i];
		}
		for (int i=0;i<sub.nodes;i++){
			MRCC[i]=sub.cpu[i]/totalCPUOfPhysicalNodes*ASCAroundNodes[i]/AllASCAroundNodes;
		}
		sort(MRCC,nSortSNodes);


	}
//	private static void sort(double[] a, int[] b) {
//		Integer[] temp = new Integer[a.length];
//		for (int i = 0; i < temp.length; i++) {
//			temp[i] = i;
//		}
//		Arrays.sort(temp, new Comparator<Integer>() {
//			@Override
//			public int compare(Integer o1, Integer o2) {
//				return Double.compare(a[o2], a[o1]);
//			}
//		});
//		for (int i = 0; i < temp.length; i++) {
//			b[i] = temp[i];
//		}
//	}
//	{
//

	private int MapVONEByESE_Wei(EOSubstrateNetwork sub,VONRequest reqs[],int index){
		if (primaryMapping(sub,reqs,index)==-1){
			if (SecondaryMapping(sub,reqs,index)==-1){
				return -1;
			}
		}
		return 0;
	}
	/******************************************************************
	 ���ƣ�MapVONEByESE_Wei����weiwentingde ESE-VONE�㷨
	 s2v_nΪ����ڵ�ӳ�������ڵ����ݽṹ
	 s2v_lΪ������·ӳ��������·���ݽṹ
	 v2sΪ����ӳ��������������ݽṹ
	 indexΪ��index����������
	 ,int ret[],int p[][],ArrayList<Object> list
	 ����ֵ��0���ɹ����أ�-1��ʧ�ܷ���
	 ******************************************************************/
	private int primaryMapping(EOSubstrateNetwork sub,VONRequest reqs[],int index)
	{
		//��ʼ������,-1������δ���䣬>-1�����Ѿ�����Ľڵ������·��
		int[] vNodeEmbed = new int[reqs[index].nodes];
		int[] sNodeEmbed = new int[sub.nodes];
		int[] vLinkEmbed = new int[reqs[index].links];
		InitAllocModel(sub,reqs,index,vNodeEmbed,sNodeEmbed,vLinkEmbed);
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
		Clone(subCopy,sub);

		int[] vNodesDegree = new int[reqs[index].nodes];//�洢ÿһ���ڵ�Ľڵ�ȵ�һά����
		int KD = GetMaxNodeDegree(reqs,index,vNodesDegree);//�������ڵ��
		double[][] array_length=(double[][])AList.get(0);
		double[][] array_Ak=new double[sub.nodes][sub.nodes];//Akֵ���ڽӾ���
		for(int i=0;i<sub.nodes;i++){
			array_Ak[i][i]=Parameters.MAX_VALUE_DOUBLE;
		}
		for(int i=0;i<sub.nodes;i++){
			for(int j=i+1;j<sub.nodes;j++)
				array_Ak[i][j]=array_Ak[j][i]=getLinkAKvalue(i, j, KD);
		}
		double[][] arraynew=getShortestPath(array_length,array_Ak,sub.nodes, sub.nodes* (sub.nodes-1)/2);//���յ���·�������

		double[] Bw=new double[reqs[index].links];
		int[] bwsortindex=new int[reqs[index].links];//�������ǵ����������·��������
		for(int i=0;i<reqs[index].links;i++){
			Bw[i] = reqs[index].link[i].bw;
			bwsortindex[i]=0;
		}
		sort(Bw, bwsortindex);//�����������������и��ݴ���������bwsortindex���б�ʾ���ݴ����ź�˳���������·��

		int SuccessFlag1=-1;
		for(int i=0;i<bwsortindex.length;i++){//����������������Ҫӳ�����·,��ʼѭ��
			SuccessFlag1=-1;
			if(vNodeEmbed[reqs[index].link[bwsortindex[i]].from]==-1&&vNodeEmbed[reqs[index].link[bwsortindex[i]].to]==-1) {
				int[] vnodesdegree = new int[reqs[index].nodes];
				int[] TwovNodes = GetTwoNodesDegree(bwsortindex[i], reqs, index, vnodesdegree);//bwsortindex[i]�������������ӳ�����·��
				int vl = TwovNodes[0];//�����нڵ�Ƚϴ�Ķ˵�
				int vs = TwovNodes[1];//��С�Ķ˵�
				for (int j = 0; j < arraynew.length; j++) {
					if (sNodeEmbed[(int) arraynew[j][0]] == -1 && sNodeEmbed[(int) arraynew[j][1]] == -1) {//�ж�������ڵ��Ƿ�ӳ��
						int pl = (int) arraynew[j][0];
						int ps = (int) arraynew[j][1];
						if (getDegreeAKvalue(ps, KD) > getDegreeAKvalue(pl, KD)) {
							pl = (int) arraynew[j][1];
							ps = (int) arraynew[j][0];
						}
						if (reqs[index].cpu[vl] <= sub.cpu[ps] && reqs[index].cpu[vs] <= sub.cpu[pl]) {
							vNodeEmbed[vl] = ps;
							vNodeEmbed[vs] = pl;
							sNodeEmbed[ps] = vl;
							//����cpu
							UpdateSub(subCopy,ps,reqs[index].cpu[vl]);
							sNodeEmbed[pl] = vs;
							//����cpu
							UpdateSub(subCopy,pl,reqs[index].cpu[vs]);
						} else if (reqs[index].cpu[vs] <= sub.cpu[ps] && reqs[index].cpu[vl] <= sub.cpu[pl]) {
							vNodeEmbed[vl] = pl;
							vNodeEmbed[vs] = ps;
							sNodeEmbed[pl] = vl;
							//����cpu
							UpdateSub(subCopy,pl,reqs[index].cpu[vl]);
							sNodeEmbed[ps] = vs;
							//����cpu
							UpdateSub(subCopy,ps,reqs[index].cpu[vs]);
						} else {
							continue;
						}
						if (PreEmbedVLinkByKShortestPath(subCopy, reqs, index, bwsortindex[i], vNodeEmbed, p, ret)) {
							vLinkEmbed[bwsortindex[i]] = 1;
							retOther[bwsortindex[i]][0] = ret[bwsortindex[i]][0];
							retOther[bwsortindex[i]][1] = ret[bwsortindex[i]][0] + ret[bwsortindex[i]][1] - 1;
							UpdateSub(subCopy, pl, ps, retOther[bwsortindex[i]], p[bwsortindex[i]]);
							SuccessFlag1 = 0;
							break;
						} else {
							continue;
						}
					}
				}
				if(SuccessFlag1==-1){
					return -1;
				}
			}
			else if(vNodeEmbed[reqs[index].link[bwsortindex[i]].from]==-1&&vNodeEmbed[reqs[index].link[bwsortindex[i]].to]!=-1){
				int pm=vNodeEmbed[reqs[index].link[bwsortindex[i]].to];
				int vu=reqs[index].link[bwsortindex[i]].from;
				int  pu=-1;
				for(int j=0;j<arraynew.length;j++){
					if(pm==(int)arraynew[j][0]||pm==(int)arraynew[j][1]){
						if(pm==(int)arraynew[j][0]&&sNodeEmbed[(int)arraynew[j][1]]==-1){
							pu=(int)arraynew[j][1];
							break;
						}else if(pm==(int)arraynew[j][1]&&sNodeEmbed[(int)arraynew[j][0]]==-1){
							pu=(int)arraynew[j][0];
							break;
						}
					}
				}
				if(pu!=-1&&(reqs[index].cpu[vu]<=sub.cpu[pu])){
					vNodeEmbed[vu]=pu;
					sNodeEmbed[pu] = vu;
					//����cpu
					UpdateSub(subCopy,pu,reqs[index].cpu[vu]);
				}else{
					return -1;
				}
				if(PreEmbedVLinkByKShortestPath(subCopy,reqs,index,bwsortindex[i],vNodeEmbed,p,ret)){
					vLinkEmbed[bwsortindex[i]] = 1;
					retOther[bwsortindex[i]][0] = ret[bwsortindex[i]][0];
					retOther[bwsortindex[i]][1] = ret[bwsortindex[i]][0]+ret[bwsortindex[i]][1]-1;
					UpdateSub(subCopy,pm,pu,retOther[bwsortindex[i]],p[bwsortindex[i]]);
					SuccessFlag1=0;
				}else {
					return -1;
				}
			}
			else if(vNodeEmbed[reqs[index].link[bwsortindex[i]].from]!=-1&&vNodeEmbed[reqs[index].link[bwsortindex[i]].to]==-1){
				int pm=vNodeEmbed[reqs[index].link[bwsortindex[i]].from];
				int vu=reqs[index].link[bwsortindex[i]].to;
				int  pu=-1;
				for(int j=0;j<arraynew.length;j++){
					if(pm==(int)arraynew[j][0]||pm==(int)arraynew[j][1]){
						if(pm==(int)arraynew[j][0]&&sNodeEmbed[(int)arraynew[j][1]]==-1){
							pu=(int)arraynew[j][1];
							break;
						}else if(pm==(int)arraynew[j][1]&&sNodeEmbed[(int)arraynew[j][0]]==-1){
							pu=(int)arraynew[j][0];
							break;
						}
					}
				}
				if(pu!=-1&&(reqs[index].cpu[vu]<=sub.cpu[pu])){
					vNodeEmbed[vu]=pu;
					sNodeEmbed[pu] = vu;
					//����cpu
					UpdateSub(subCopy,pu,reqs[index].cpu[vu]);
				}else{
					return -1;
				}
				if(PreEmbedVLinkByKShortestPath(subCopy,reqs,index,bwsortindex[i],vNodeEmbed,p,ret)){
					vLinkEmbed[bwsortindex[i]] = 1;
					retOther[bwsortindex[i]][0] = ret[bwsortindex[i]][0];
					retOther[bwsortindex[i]][1] = ret[bwsortindex[i]][0]+ret[bwsortindex[i]][1]-1;
					UpdateSub(subCopy,pu,pm,retOther[bwsortindex[i]],p[bwsortindex[i]]);
					SuccessFlag1=0;
				}else {
					return -1;
				}
			}
			else{
				int pl=vNodeEmbed[reqs[index].link[bwsortindex[i]].from];
				int pm=vNodeEmbed[reqs[index].link[bwsortindex[i]].to];
				if(PreEmbedVLinkByKShortestPath(subCopy,reqs,index,bwsortindex[i],vNodeEmbed,p,ret)){
					vLinkEmbed[bwsortindex[i]] = 1;
					retOther[bwsortindex[i]][0] = ret[bwsortindex[i]][0];
					retOther[bwsortindex[i]][1] = ret[bwsortindex[i]][0]+ret[bwsortindex[i]][1]-1;
					UpdateSub(subCopy,pm,pl,retOther[bwsortindex[i]],p[bwsortindex[i]]);
					SuccessFlag1=0;
				}else {
					return -1;
				}
			}
		}

		//����cpu
		UpdateSub(sub,subCopy);
		//��¼�ڵ����·ӳ����
		AddNodesMap(reqs,index,vNodeEmbed);//����s2v_n��v2s
		AddLinksMapBySPFA(sub,reqs,index,retOther,p);//���µײ�����
		//���µײ�����slots
		UpdateSubSlots(sub,subCopy);

		return SuccessFlag1;//�ɹ�����
	}


	private int MapVONEPageRankOfGHGByVogel(EOSubstrateNetwork sub,VONRequest reqs[],int index)
	{
		//��������ģ�ͺ���С���õ�Ƶ�ײ�����
		double[][] transModel = new double[reqs[index].nodes+1][sub.nodes+1];
		int[][] indexModel = new int[reqs[index].nodes][sub.nodes];
		int[][] linkModel = new int[reqs[index].nodes][sub.nodes];
		InitGHGModel(sub,reqs,index,transModel,indexModel,linkModel);


		//��ʼ������,-1������δ���䣬>-1�����Ѿ�����Ľڵ������·��
		int[] vNodeEmbed = new int[reqs[index].nodes];
		int[] sNodeEmbed = new int[sub.nodes];
		int[] vLinkEmbed = new int[reqs[index].links];
		InitAllocModel(sub,reqs,index,vNodeEmbed,sNodeEmbed,vLinkEmbed);

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
		while(num < reqs[index].nodes){
			CalculateTheDifference(sub,reqs,index,transModel,vNodeEmbed,sNodeEmbed);
			//Ѱ����СԪ�أ���������minElement[0]\minElement[1];minIndexReq��minIndexSub
			FindEnMinElementByVogel(subCopy,reqs,index,transModel,vNodeEmbed,sNodeEmbed,minElement);
//
//			Tools myDowith = new Tools();
//			String data ="";
//			for (int i = 0; i <= reqs[index].nodes; i++) {
//				for (int j = 0; j<=sub.nodes; j++) {
//					data+=transModel[i][j]+"   " ;
//				}
//				data+="\n";
//			}
//			data+="\n\n\n";
//			myDowith.SaveFile("theMinIWantNode1151.txt", data, true);

			//FindEnMinElement(subCopy,reqs,index,transModel,vNodeEmbed,sNodeEmbed,minElement);
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
	private int MapVONEPageRankOfGHGByVogel1(EOSubstrateNetwork sub,VONRequest reqs[],int index)
	{
		//��������ģ�ͺ���С���õ�Ƶ�ײ�����
		double[][] transModel = new double[reqs[index].nodes+1][sub.nodes+1];
		int[][] indexModel = new int[reqs[index].nodes][sub.nodes];
		int[][] linkModel = new int[reqs[index].nodes][sub.nodes];
		InitGHGModel1(sub,reqs,index,transModel,indexModel,linkModel);


		//��ʼ������,-1������δ���䣬>-1�����Ѿ�����Ľڵ������·��
		int[] vNodeEmbed = new int[reqs[index].nodes];
		int[] sNodeEmbed = new int[sub.nodes];
		int[] vLinkEmbed = new int[reqs[index].links];
		InitAllocModel(sub,reqs,index,vNodeEmbed,sNodeEmbed,vLinkEmbed);

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
		while(num < reqs[index].nodes){
			CalculateTheDifference(sub,reqs,index,transModel,vNodeEmbed,sNodeEmbed);
			//Ѱ����СԪ�أ���������minElement[0]\minElement[1];minIndexReq��minIndexSub
			FindEnMinElementByVogel(subCopy,reqs,index,transModel,vNodeEmbed,sNodeEmbed,minElement);
//
//			Tools myDowith = new Tools();
//			String data ="";
//			for (int i = 0; i <= reqs[index].nodes; i++) {
//				for (int j = 0; j<=sub.nodes; j++) {
//					data+=transModel[i][j]+"   " ;
//				}
//				data+="\n";
//			}
//			data+="\n\n\n";
//			myDowith.SaveFile("theMinIWantNode1151.txt", data, true);

			//FindEnMinElement(subCopy,reqs,index,transModel,vNodeEmbed,sNodeEmbed,minElement);
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
/*�����������*/
	private void  CalculateTheDifference(EOSubstrateNetwork sub,VONRequest reqs[],int index,double[][] transModel,int[] vNodeEmbed,int[]sNodeEmbed){
		double minRow =  Parameters.MAX_VALUE_INT;
		double secRow =  Parameters.MAX_VALUE_INT;
		double minCol = Parameters.MAX_VALUE_INT;
		double secCol =Parameters.MAX_VALUE_INT;

		for(int i =0;i<reqs[index].nodes;i++){//�����в��
			minRow =  Parameters.MAX_VALUE_INT;
			 secRow =  Parameters.MAX_VALUE_INT;
			for(int j=0;j<sub.nodes;j++){
				if(vNodeEmbed[i]==-1&&sNodeEmbed[j]==-1&&transModel[i][j]!=-1&&transModel[i][j]<secRow){
					if (transModel[i][j]<minRow){
						secRow=minRow;
						minRow = transModel[i][j];
					}else {
						secRow=transModel[i][j];
					}
				}
			}
			if ((secRow== Parameters.MAX_VALUE_INT )||(minRow == Parameters.MAX_VALUE_INT)){
				transModel[i][sub.nodes]=0;
			}else {
				transModel[i][sub.nodes]=secRow-minRow;
			}

		}

		for (int i =0;i<sub.nodes;i++){//�����в��
			 minCol = Parameters.MAX_VALUE_INT;
			 secCol =Parameters.MAX_VALUE_INT;
			for(int j = 0;j<reqs[index].nodes;j++){
				if (vNodeEmbed[j]==-1&&sNodeEmbed[i]==-1&&transModel[j][i]!=-1&&transModel[j][i]<secCol){
					if (transModel[j][i]<minCol){
						secCol=minCol;
						minCol = transModel[j][i];
					}else {
						secCol=transModel[j][i];
					}
				}
			}
			if ((secCol== Parameters.MAX_VALUE_INT )||(minCol == Parameters.MAX_VALUE_INT)){
				transModel[reqs[index].nodes][i]=0;
			}else {
				transModel[reqs[index].nodes][i]=secCol-minCol;
			}
		}

	}
	private int MapVONEByVogel(EOSubstrateNetwork sub,VONRequest reqs[],int index)
	{
		//��������ģ�ͺ���С���õ�Ƶ�ײ�����
		double[][] transModel = new double[reqs[index].nodes][sub.nodes];
		int[][] indexModel = new int[reqs[index].nodes][sub.nodes];
		int[][] linkModel = new int[reqs[index].nodes][sub.nodes];
		InitModel(sub,reqs,index,transModel,indexModel,linkModel);

		//��ʼ������,-1������δ���䣬>-1�����Ѿ�����Ľڵ������·��
		int[] vNodeEmbed = new int[reqs[index].nodes];
		int[] sNodeEmbed = new int[sub.nodes];
		int[] vLinkEmbed = new int[reqs[index].links];
		InitAllocModel(sub,reqs,index,vNodeEmbed,sNodeEmbed,vLinkEmbed);

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
		while(num < reqs[index].nodes){
			//Ѱ����СԪ�أ���������minElement[0]\minElement[1];minIndexReq��minIndexSub
			FindMinElementByVogel(subCopy,reqs,index,transModel,vNodeEmbed,sNodeEmbed,minElement);
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

	private int MapVONEByEasy(EOSubstrateNetwork sub,VONRequest reqs[],int index) {

		double vNodePageRank[]= new double[reqs[index].nodes];
		double sNodePageRank[] = new double[sub.nodes];
		//	InitVNodePageRank(reqs,index);

		vNodePageRank = InitVNodeEnergyPageRank(vNodePageRank, reqs, index);
		sNodePageRank = InitSNodeEnergyPageRank(sNodePageRank, sub);

		//[][0]�ڵ���� [][1] pagerankֵ
		double sortvNodePageRank[] []=new double[reqs[index].nodes][2];
		double sortsNodePageRank[] []= new double[sub.nodes][2];
		for(int i=0;i<reqs[index].nodes;i++){
			sortvNodePageRank[i][0]=i;
			sortvNodePageRank[i][1]=vNodePageRank[i];
		}
		for (int i =0;i<sub.nodes;i++){
			sortsNodePageRank[i][0]=i;
			sortsNodePageRank[i][1]=sNodePageRank[i];
		}
		sortvPageRank(sortvNodePageRank,reqs,index);
		sortsPageRank(sortsNodePageRank,sub);



		//��ʼ������,-1������δ���䣬>-1�����Ѿ�����Ľڵ������·��
		int[] vNodeEmbed = new int[reqs[index].nodes];
		int[] sNodeEmbed = new int[sub.nodes];
		int[] vLinkEmbed = new int[reqs[index].links];
		InitAllocModel(sub,reqs,index,vNodeEmbed,sNodeEmbed,vLinkEmbed);

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

		Clone(subCopy,sub);

		int num = 0;
		int[] minElement = new int[2];//minElement[0]����ڵ㣻minElement[1]����ڵ�;
		while(num < reqs[index].nodes){
			//Ѱ��pagerankֵ���Ľڵ�

			FindNode(subCopy,reqs,index,vNodeEmbed,sNodeEmbed,minElement,sortvNodePageRank,sortsNodePageRank,num);
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

	private void sortvPageRank(double sortvNodePageRank[][],VONRequest reqs[],int index){
		for (int i =0;i<reqs[index].nodes;i++){
			for (int j =0;j<reqs[index].nodes-1-i;j++){
				if(sortvNodePageRank[j][1] < sortvNodePageRank[j+1][1]){
					double temp[][]=new double[1][2];
					temp[0][0] =sortvNodePageRank[j][0];
					temp[0][1] =sortvNodePageRank[j][1];
					sortvNodePageRank[j][0]=sortvNodePageRank[j+1][0];
					sortvNodePageRank[j][1]=sortvNodePageRank[j+1][1];
					sortvNodePageRank[j+1][0]=temp[0][0];
					sortvNodePageRank[j+1][1]=temp[0][1];
				}
			}
		}
	}
	private void sortsPageRank(double sortsNodePageRank[][],EOSubstrateNetwork sub){
		for (int i =0;i<sub.nodes;i++){
			for (int j =0;j<sub.nodes-1-i;j++){
				if(sortsNodePageRank[j][1]<sortsNodePageRank[j+1][1]){
					double temp[][]=new double[1][2];
					temp[0][0] =sortsNodePageRank[j][0];
					temp[0][1] =sortsNodePageRank[j][1];
					sortsNodePageRank[j][0]=sortsNodePageRank[j+1][0];
					sortsNodePageRank[j][1]=sortsNodePageRank[j+1][1];
					sortsNodePageRank[j+1][0]=temp[0][0];
					sortsNodePageRank[j+1][1]=temp[0][1];
				}
			}
		}
	}
	private void FindNode(EOSubstrateNetwork subCopy,VONRequest reqs[],int index,int[] vnodeEmbed,int[] snodeEmbed,int[] minElent,double[][]sortvNodePageRank,double[][]sortsNodePageRank,int num)

	{
		minElent[0] = minElent[1] = -1;
		for (int i =0;i<subCopy.nodes;i++){
			for(int k=0;k<reqs[index].links;k++){
				if ((reqs[index].cpu[(int) sortvNodePageRank[num][0]] <= subCopy.cpu[(int) sortsNodePageRank[num][0]] + Parameters.MIN_VALUE_DOUBLE) && vnodeEmbed[(int) sortvNodePageRank[num][0]] == -1 && snodeEmbed[(int) sortsNodePageRank[i][0]] == -1 ) {
					minElent[0] = (int) sortvNodePageRank[num][0];
					minElent[1] = (int) sortsNodePageRank[num][0];
					return;
				}
			}

		}

	}
	private int MapVONDRLMD_VONE(EOSubstrateNetwork sub, VONRequest reqs[], int index) throws IOException {
		System.out.println(" ��ʼ MD-VNE �������");

		// �� agent δ��ʼ�����ڿ�ʼǰ��ʼ����ʹ�õ�ǰ��������ڵ�����
		if (drlAgent == null) {
			drlAgent = new DRLAgent(sub.nodes);
			drlAgent.loadModel("drl_agent_final.zip");
		}
		// ֻ����ǰ���󣬲����ж���ѵ��
		int result = MapVONDRLMD_VONE1(sub, reqs, index, globalStep);
		// ���� temperature
		drlAgent.updateTemperature();

		// ������ɺ󱣴�����ģ��
		try {
			//drlAgent.saveModel("drl_agent_final.zip");
			//System.out.println("����ģ���ѱ��浽: drl_agent_final.zip");
		} catch (Exception e) {
			System.err.println("��������ģ��ʧ��: " + e.getMessage());
			e.printStackTrace();
		}

		System.out.println("MD-VNE ������ɡ�");
		return result;
	}

	private int MapVONDRLMD_VONE1(EOSubstrateNetwork sub, VONRequest reqs[], int index, int globalStep) {
		boolean mappingSuccess = false;
		double reward = 0.0;
		double[][] currentState = null;  // ��ʼ��Ϊ null
		double[][] nextState = null;
		int[] vNodeEmbed = null;

		try {

			// ����ӳ��ǰ��״̬���󣨲����������ṹ��R_S, TR_B, Dis��
			StateGenerator stateGenerator = new StateGenerator();
			currentState = stateGenerator.getStateMatrix(sub); // ���ṩ������������

			// ��ȡ�ڵ������ʲ� mask
			double[] probabilities = drlAgent.getActionProbabilities(currentState);
			probabilities = drlAgent.maskInvalidNodes(probabilities, sub); // ����ǿ����

			// ��ʼ������ģ��
			vNodeEmbed = new int[reqs[index].nodes];
			Arrays.fill(vNodeEmbed, -1);
			int[] sNodeEmbed = new int[sub.nodes];
			Arrays.fill(sNodeEmbed, -1);
			int[] vLinkEmbed = new int[reqs[index].links];
			Arrays.fill(vLinkEmbed, -1);

			InitAllocModel(sub, reqs, index, vNodeEmbed, sNodeEmbed, vLinkEmbed);

			int[][] p = new int[reqs[index].links][sub.nodes];
			int[][] ret = new int[reqs[index].links][2];
			int[][] retOther = new int[reqs[index].links][2];

			for (int i = 0; i < reqs[index].links; i++) {
				Arrays.fill(p[i], -1);
				ret[i][0] = ret[i][1] = -1;
				retOther[i][0] = retOther[i][1] = -1;
			}

			EOSubstrateNetwork subCopy = new EOSubstrateNetwork();
			Clone(subCopy, sub);

			// �ڵ�ӳ��ѭ���������ģ���ڵ���ߣ�
			int num = 0;
			mappingSuccess = true;

			while (num < reqs[index].nodes) {
				// ��ѡδӳ�������ڵ㣨��˳����Զ�����ԣ�
				int selectedVNode = -1;
				for (int vNode = 0; vNode < reqs[index].nodes; vNode++) {
					if (vNodeEmbed[vNode] == -1) {
						selectedVNode = vNode;
						break;
					}
				}
				if (selectedVNode == -1) { mappingSuccess = false; break; }

				int selectedSNode = selectNodeByProbability(probabilities, sub, reqs, index, selectedVNode, sNodeEmbed);
				if (selectedSNode == -1) {
					mappingSuccess = false;
					break;
				}

				// ִ�нڵ�ӳ�䣨ռ����Դ��
				vNodeEmbed[selectedVNode] = selectedSNode;
				sNodeEmbed[selectedSNode] = selectedVNode;
				UpdateSub(subCopy, selectedSNode, reqs[index].cpu[selectedVNode]);

				// ������ӳ�������ڵ㣬����ӳ����δӳ���������·��K-shortest��
				int noEmbedVLink = FindNoEmbedVLink(reqs, index, selectedVNode, vNodeEmbed, vLinkEmbed);
				while (noEmbedVLink > -1) {
					if (!PreEmbedVLinkByKShortestPath(subCopy, reqs, index, noEmbedVLink, vNodeEmbed, p, ret)) {
						mappingSuccess = false; break;
					}
					vLinkEmbed[noEmbedVLink] = 1;

					int sNode1 = vNodeEmbed[reqs[index].link[noEmbedVLink].from];
					int sNode2 = vNodeEmbed[reqs[index].link[noEmbedVLink].to];

					retOther[noEmbedVLink][0] = ret[noEmbedVLink][0];
					retOther[noEmbedVLink][1] = ret[noEmbedVLink][0] + ret[noEmbedVLink][1] - 1;

					UpdateSub(subCopy, sNode2, sNode1, retOther[noEmbedVLink], p[noEmbedVLink]);

					if (Parameters.DebugModel) {
						System.out.println(noEmbedVLink + "(" + retOther[noEmbedVLink][0] + "-" + retOther[noEmbedVLink][1] + ")");
						PrintPath(p[noEmbedVLink], sNode2, sNode1);
					}
					noEmbedVLink = FindNoEmbedVLink(reqs, index, selectedVNode, vNodeEmbed, vLinkEmbed);
				}

				if (!mappingSuccess) break;
				num++;
			}

			if (!mappingSuccess) {
				reward = -1.0;
			} else {
				// ʵ��Ӧ�õ��������粢���㼴ʱ����
				UpdateSub(sub, subCopy);
				AddNodesMap(reqs, index, vNodeEmbed);
				AddLinksMapBySPFA(sub, reqs, index, retOther, p);
				UpdateSubSlots(sub, subCopy);

				reward = calculateImmediateReward(sub, reqs[index], vNodeEmbed, p, retOther);
				nextState = stateGenerator.getStateMatrix(sub);
			}

			// ���� rt������ȫ�ֲ��� �� epoch�����Զ��壩
			double rt = computeRt(globalStep);

			// ����������������������㷨1��11�У����� rt ��ΪȨ�ش��� agent
			drlAgent.updateWithCurrentExperience(currentState, vNodeEmbed, reward, nextState != null ? nextState : currentState, rt);

			// �������һ�� reward �Ա�ѵ��ͳ��
			drlAgent.setLastReward(reward);

			return mappingSuccess ? 1 : -1;

		} catch (Exception e) {
			e.printStackTrace();
			// ȷ�� currentState ��Ϊ null ʱ�Ž��и���
			if (currentState != null && vNodeEmbed != null && drlAgent != null) {
				drlAgent.updateWithCurrentExperience(currentState, vNodeEmbed, -1.0, currentState, computeRt(globalStep));
				drlAgent.setLastReward(-1.0);
			}
			return -1;
		}
	}
	/**
	 * ���Խ׶ε�DRLӳ�䷽��
	 * �ڲ��Խ׶Σ�ֻʹ��ѵ���õ�ģ�ͽ���ӳ����ߣ�������ģ�Ͳ���
	 */
	private int MapVONDRLMD_VONETest(EOSubstrateNetwork sub, VONRequest reqs[], int index) {
		try {
			// ��ʼ�� DRL ��������δ��ʼ����
			if (drlAgent == null) {
				drlAgent = new DRLAgent(sub.nodes);
				// �ڲ��Խ׶Σ�����ѵ���õ�ģ��
				try {
					drlAgent.loadModel("drl_agent_final.zip");
					System.out.println("���Խ׶Σ��ɹ�����ѵ��ģ��");
				} catch (Exception e) {
					System.err.println("���Խ׶Σ�����ģ��ʧ�ܣ�ʹ���������: " + e.getMessage());
					return -1; // ����ʧ��ֱ�ӷ���
				}
			}

			// ����ӳ��ǰ��״̬����
			StateGenerator stateGenerator = new StateGenerator();
			double[][] currentState = stateGenerator.getStateMatrix(sub);

			// ��ʼ������ģ��
			int[] vNodeEmbed = new int[reqs[index].nodes];
			int[] sNodeEmbed = new int[sub.nodes];
			int[] vLinkEmbed = new int[reqs[index].links];

			Arrays.fill(vNodeEmbed, -1);
			Arrays.fill(sNodeEmbed, -1);
			Arrays.fill(vLinkEmbed, -1);

			InitAllocModel(sub, reqs, index, vNodeEmbed, sNodeEmbed, vLinkEmbed);

			int[][] p = new int[reqs[index].links][sub.nodes];
			int[][] ret = new int[reqs[index].links][2];
			int[][] retOther = new int[reqs[index].links][2];

			for (int i = 0; i < reqs[index].links; i++) {
				Arrays.fill(p[i], -1);
				ret[i][0] = ret[i][1] = -1;
				retOther[i][0] = retOther[i][1] = -1;
			}

			EOSubstrateNetwork subCopy = new EOSubstrateNetwork();
			Clone(subCopy, sub);

			// �ڵ�ӳ��ѭ��
			boolean mappingSuccess = true;
			int num = 0;

			while (num < reqs[index].nodes) {
				// ѡ��δӳ�������ڵ�
				int selectedVNode = -1;
				for (int vNode = 0; vNode < reqs[index].nodes; vNode++) {
					if (vNodeEmbed[vNode] == -1) {
						selectedVNode = vNode;
						break;
					}
				}

				if (selectedVNode == -1) {
					mappingSuccess = false;
					break;
				}

				// �ڲ��Խ׶Σ�ʹ�� DRLAgent �� getBestAction ����ѡ���������ڵ㣨ȷ���Բ��ԣ�
				int selectedSNode = drlAgent.getBestAction(currentState, sub,sNodeEmbed);

				// ��֤ѡ��������ڵ��Ƿ���Ч
				if (selectedSNode == -1 || sNodeEmbed[selectedSNode] != -1 ||
						reqs[index].cpu[selectedVNode] > sub.cpu[selectedSNode]) {
					mappingSuccess = false;
					break;
				}

				// ִ�нڵ�ӳ��
				vNodeEmbed[selectedVNode] = selectedSNode;
				sNodeEmbed[selectedSNode] = selectedVNode;
				UpdateSub(subCopy, selectedSNode, reqs[index].cpu[selectedVNode]);

				// ����״̬�����Է�ӳ��Դ�仯
				currentState = stateGenerator.getStateMatrix(subCopy);

				// ӳ�����������·
				int noEmbedVLink = FindNoEmbedVLink(reqs, index, selectedVNode, vNodeEmbed, vLinkEmbed);
				while (noEmbedVLink > -1) {
					if (!PreEmbedVLinkByKShortestPath(subCopy, reqs, index, noEmbedVLink, vNodeEmbed, p, ret)) {
						mappingSuccess = false;
						break;
					}
					vLinkEmbed[noEmbedVLink] = 1;

					int sNode1 = vNodeEmbed[reqs[index].link[noEmbedVLink].from];
					int sNode2 = vNodeEmbed[reqs[index].link[noEmbedVLink].to];

					retOther[noEmbedVLink][0] = ret[noEmbedVLink][0];
					retOther[noEmbedVLink][1] = ret[noEmbedVLink][0] + ret[noEmbedVLink][1] - 1;

					UpdateSub(subCopy, sNode2, sNode1, retOther[noEmbedVLink], p[noEmbedVLink]);

					if (Parameters.DebugModel) {
						System.out.println(noEmbedVLink + "(" + retOther[noEmbedVLink][0] + "-" + retOther[noEmbedVLink][1] + ")");
						PrintPath(p[noEmbedVLink], sNode2, sNode1);
					}
					noEmbedVLink = FindNoEmbedVLink(reqs, index, selectedVNode, vNodeEmbed, vLinkEmbed);
				}

				if (!mappingSuccess) break;
				num++;
			}

			if (mappingSuccess) {
				// ʵ��Ӧ�õ���������
				UpdateSub(sub, subCopy);
				AddNodesMap(reqs, index, vNodeEmbed);
				AddLinksMapBySPFA(sub, reqs, index, retOther, p);
				UpdateSubSlots(sub, subCopy);

				System.out.println("���Խ׶Σ����� " + index + " ӳ��ɹ�");
				return 1; // �ɹ�
			} else {
				System.out.println("���Խ׶Σ����� " + index + " ӳ��ʧ��");
				return -1; // ʧ��
			}

		} catch (Exception e) {
			e.printStackTrace();
			return -1;
		}
	}


	// ===================== ���� rt��˥��Ȩ�أ� =====================
	private double computeRt(int globalStep) {
		// ��ָ��/����˥��������rt = max(0.1, 1.0 - decay * steps)
		double rt = Math.max(0.05, 1.0 - RT_DECAY_RATE * globalStep);
		return rt;
	}
	/**
	 * ���� softmax �����������ѡ������ڵ�
	 */
	private int selectNodeByProbability(double[] probabilities, EOSubstrateNetwork sub,
										VONRequest[] reqs, int index, int vNode, int[] sNodeEmbed) {
		List<Integer> candidates = new ArrayList<>();
		List<Double> candidateProbs = new ArrayList<>();
		double totalProb = 0.0;

		// ͬʱ�ռ���ѡ�ڵ�ͼ����ܸ���
		for (int sNode = 0; sNode < sub.nodes; sNode++) {
			if (sNodeEmbed[sNode] == -1 &&
					sub.cpu[sNode] >= reqs[index].cpu[vNode] + Parameters.MIN_VALUE_DOUBLE) {
				candidates.add(sNode);
				double prob = probabilities[sNode];
				candidateProbs.add(prob);
				totalProb += prob;
			}
		}

		if (candidates.isEmpty()) {
			return -1;
		}

		// ������к�ѡ���ʶ�Ϊ0��ʹ�þ��ȷֲ�
		if (totalProb <= 0) {
			int randomIndex = (int) (Math.random() * candidates.size());
			return candidates.get(randomIndex);
		}

		// softmax����
		double rand = Math.random() * totalProb;
		double cumulative = 0.0;

		for (int i = 0; i < candidates.size(); i++) {
			cumulative += candidateProbs.get(i);
			if (rand <= cumulative) {
				return candidates.get(i);
			}
		}

		return candidates.get(candidates.size() - 1);
	}


	/**
	 * ��������ͳ���߼��ļ�ʱ��������
	 * ��RecordResultsOfVNE����һ�µ�����ɱ����㷽ʽ
	 */
	private double calculateImmediateReward(EOSubstrateNetwork sub, VONRequest req,
											int[] vNodeEmbed, int[][] p, int[][] ret) {
		double revenue = 0.0;  // ����
		double cost = 0.0;     // �ɱ�

		// 1. ����ڵ�CPU���棨��RecordResultsOfVNEһ�£�
		for (int i = 0; i < req.nodes; i++) {
			revenue += req.cpu[i];
		}

		// 2. ������·�������棨��RecordResultsOfVNEһ�£�
		for (int i = 0; i < req.links; i++) {
			revenue += req.link[i].bw;
		}

		// 3. ����ڵ�ɱ���CPU���ģ�
		double nodeCost = 0.0;
		for (int i = 0; i < req.nodes; i++) {
			nodeCost += req.cpu[i];
		}

		// 4. ������·�ɱ������� �� ·�����ȣ���RecordResultsOfVNE�е�bwSubSumһ�£�
		double linkCost = 0.0;
		for (int i = 0; i < req.links; i++) {
			if (p[i][0] != -1) { // ��·ӳ��ɹ�
				// ����·�����ȣ�������
				int pathLength = calculatePathLength(p[i]);
				linkCost += req.link[i].bw * pathLength;
			}
		}

		// �ܳɱ� = �ڵ�ɱ� + ��·�ɱ�����RecordResultsOfVNE�е�cpuSum + bwSubSumһ�£�
		cost = nodeCost + linkCost;
		// 6. �����ܽ���
		double reward = 0.0;
		if (cost > 0) {
			// ��ʽ1������ɱ��ȣ���RecordResultsOfVNE�е�rvcһ�£�
			double rvc = revenue / cost;
			reward = rvc;

			// ��ʽ2�������ܺĵĵ�������
			// double adjustedReward = rvc - energyCost / (revenue + 1e-8);
			// reward = Math.max(0, adjustedReward); // ȷ���Ǹ�
		} else {
			// �ɱ�Ϊ0���������
			reward = 1.0;
		}

		// ������Ϣ
		if (Parameters.DebugModel) {
			System.out.printf(" ��ʱ�������� - ����: %.2f, �ɱ�: %.2f, �ܺ�: %.2f, ���ս���: %.4f%n",
					revenue, cost,  reward);
		}

		return reward;
	}

	/**
	 * ����·�����ȣ�������
	 */
	private int calculatePathLength(int[] path) {
		int length = 0;
		for (int i = 0; i < path.length; i++) {
			if (path[i] == -1) break;
			length++;
		}
		return Math.max(0, length - 1); // ���� = �ڵ��� - 1
	}

}
