/*
 * function: VONE by deep reinforce learning
 * create time: 2026/1/20
 * author: Chen Xiaohua
 */

package Team.CloudStorage.EAVONE;

import Team.CloudStorage.EAVONE.DRLMD_VONE.StateGenerator;
import Team.CloudStorage.EAVONE.DeepReinforceLearning.DeepReinforceLearningAgent;
import Team.CloudStorage.EAVONE.DeepReinforceLearning.ResourceState;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;

import org.nd4j.linalg.api.ndarray.INDArray;

public class VONEByDeepReinforceLearning extends VNE {
	
	// DRL超参数
    private double gamma = 0.99;
    private double learningRate = 0.005;//2026.5.14修改，原本0.001
    private double epsilon = 0.1;
	// 衰减率
    private double epsilonDecay = 0.92;//5.20修改为0.98，2026.5.19修改为0.985，2026.5.14修改为0.92，原本0.995
	// 最低探索率
    private double minEpsilon = 0.01;
	//private String modeName = "drl_agent_final.zip2";//2026.5.12 20:15新增代码的测试模型,6个物理节点
	//private String modeName = "drl_agent_final.zip6";//2026.5.12 20:15新增代码的测试模型,6个物理节点结果不错,改成28个节点训练
	//private String modeName = "drl_agent_final.zip7";//2026.5.14 13:23新增代码的测试模型,在12号模型代码的基础上修改训练测试收敛性,100个请求
	//private String modeName = "drl_agent_final.zip1";//2026.5.14 13:23新增代码的测试模型,在12号模型代码的基础上正式开始训练，500个请求训练
	private String modeName = "drl_agent_final.zip3";//2026.5.16 16:20新增代码的测试模型,在12号模型代码的基础上测试，减少训练时间,500个请求
	//private String modeName = "drl_agent_final.zip4";//2026.5.17 21:45新增代码的测试模型,在16号模型代码的基础上测试，减少了训练时看看会不会降低效果,100请求
	//private String modeName = "drl_agent_final.zip5";//2026.5.18 21:42新增代码的测试模型,在16号模型代码的基础上测试，减少了训练时间会降低效果,继续优化修改，100请求
	//private String modeName = "drl_agent_final.zip8";//2026.5.19 21:05新增代码的测试模型,在16号模型代码的基础上测试，减少了训练时间会降低效果,继续优化修改探索率和衰减率，100请求
	//private String modeName = "drl_agent_final.zip9";//2026.5.20 21:50新增代码的测试模型,在16号模型代码的基础上测试，减少了训练时间会降低效果,极致贪心版，放大ai话语权，100请求
	//private String modeName = "drl_agent_final.zip10";//2026.5.20 21:50新增代码的测试模型,在16号模型代码的基础上测试，减少了训练时间会降低效果,极致贪心版，放大ai话语权，500请求

    private DeepReinforceLearningAgent agent = null;
    
	public void VONEEmbed(String inSNFile,String inVNsFileDir,int reqsNum,int delay) throws IOException
	{
		
		
		if(Parameters.TrainOrTest) {//训练
			
			System.out.println("开始训练CNN-based MD-VNE算法...");
	        System.out.println("模型将保存到: " + modeName);
	        System.out.println("初始探索率: " + epsilon);
			int episodeNum = 20;//训练轮次
			for(int episode = 0;episode < episodeNum; episode++) {//训练的次数10
				//Create EON and VONs
				super.VONEEmbed(inSNFile, inVNsFileDir, reqsNum, delay);
				Date startDate = new Date();
				
				//Init agent
				// 创建智能体
		        int inputSize = 3;//sub.nodes * 3; // 3个特征
		        int outputSize = sub.nodes;//.iotNodes.size(); // 可选节点数量
		        agent = new DeepReinforceLearningAgent(outputSize, inputSize);//int nNodes, int nFeatures
		        
		        agent.loadModel(modeName);
		        
				//InitEnvironment(reqsNum);
				V2SEmbed(sub,reqs,delay);
				// 衰减探索率
	            epsilon = Math.max(minEpsilon, epsilon * epsilonDecay);
	            System.out.println("curr探索率: " + epsilon);
	            
	            //保存模型
				agent.saveModel(modeName);
	            
	            Date endDate = new Date();//��¼ӳ�俪ʼ��ʱ��
				long interval = (endDate.getTime() - startDate.getTime())/1000;//��¼ʱ���룩
				RecordResultsOfVNE(sub, reqs, interval, Parameters.CurrentVONEMethod);
			}
			
			
			
		} else {
			//Create EON and VONs
			super.VONEEmbed(inSNFile, inVNsFileDir, reqsNum, delay);
			Date startDate = new Date();
			
			//Init agent
			// 创建智能体
	        int inputSize = 3;//sub.nodes * 4; // 4个特征
	        int outputSize = sub.nodes;//.iotNodes.size(); // 可选节点数量
	        agent = new DeepReinforceLearningAgent(outputSize, inputSize);//int nNodes, int nFeatures
	        
	        agent.loadModel(modeName);
	        
			System.out.println("开始测试CNN-based MD-VNE算法...");
			V2SEmbed(sub,reqs,delay);
			
			Date endDate = new Date();//��¼ӳ�俪ʼ��ʱ��
			long interval = (endDate.getTime() - startDate.getTime())/1000;//��¼ʱ���룩
			RecordResultsOfVNE(sub, reqs, interval, Parameters.CurrentVONEMethod);
		}
	}
	/*
	 * Function: InitEnvironment()
	 * Create time: 2026/1/29
	 * Creator: Chen Xiaohua
	 */
	void InitEnvironment(int VONNum)
	{
		//public static VONRequest reqs[];
	    //public static S2VNode s2v_n[];
	    //public static S2VLink s2v_l[];
	    //public static Req2Sub v2s[];
	    //reqs = null;
		for(int i=0;i<VONNum;i++) {
			reqs[i].map = Parameters.STATE_NEW;
		}
		//reqs[0].map = 0;
	    //s2v_n = null;
	    //s2v_l = null;
	    //v2s = null;
	    		
	}
	/*The algorithm of mapping the VNs.*/
	private void V2SEmbed(EOSubstrateNetwork sub,VONRequest reqs[],int delay) throws IOException
	{
		//embedModelOrAlgo = embedAlgorithm;//
		int end,n,time,start,sStart;
		time = Parameters.TIME_INTERVAL;
		end = 0;
		n = reqs.length;
		System.out.println("reqs.length:"+n);
		Date startDate = new Date();
		
		//if(Parameters.TrainOrTest || !Parameters.TrainOrTest) {//训练
			//for(int episode = 0;episode < 1; episode++) {//训练的次数10
			//	System.out.println("episode:"+episode);

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
		//	}
		//}

		//RecordResultsOfVNE(sub, reqs, interval, Parameters.CurrentVONEMethod);

		// �������ͳ�ƽ����ʹ��ȫ�ֱ�����
		//if (totalRequests > 0) {
		//	double successRate = (double) successfulRequests / totalRequests * 100.0;
		//	double avgReward = totalRewardSum / Math.max(1, globalStep); // ƽ����ʱ����
		//	System.out.printf("ӳ����� - ��������: %d, �ɹ�ӳ����: %d, �ɹ���: %.2f%%, �ܽ���: %.4f, ƽ����ʱ����: %.4f, ȫ�ֲ���: %d, ��ʱ: %d��%n",
		//			totalRequests, successfulRequests, successRate, totalRewardSum, avgReward, globalStep, interval);
		//}
		//if (drlAgent != null) {
		//	System.out.println("  Agent stats: " + drlAgent.getTrainingStats());
		//}


		//if (Parameters.DebugModel)
		//	System.out.println("RecordResultsOfVNE.");
		
		//if(Parameters.CurrentVONEMethod == MapVONEDRLMD_VONE){
		//	RecordResultsOfVNE1(sub, reqs, interval, Parameters.CurrentVONEMethod,totalRequests,successfulRequests,totalRewardSum,globalStep,interval);
		//}else {
		//	RecordResultsOfVNE(sub, reqs, interval, Parameters.CurrentVONEMethod);
		//}
		//if (Parameters.DebugModel)
		//	System.out.println("PrintfVNE.");
		//if(Parameters.DebugModel) PrintNodeEmbedding(reqs);
		//if(Parameters.DebugModel) PrintLinkEmbedding(reqs);
		//PrintVNE(sub, reqs);PrintResultOfVN(sub,reqs);
	}
	
	//
	private void AllocateResources(EOSubstrateNetwork sub,VONRequest reqs[],int start,int end) throws IOException
	{
		System.out.println("start:" + start + " end:" + end);
		int windowTotalRequests = end - start;
		int windowSuccessfulRequests = 0;
		double windowRewardSum = 0.0;
		int windowSteps = 0;
		//totalRequests += windowTotalRequests;
		for(int i=start;i<end;i++){
			if(v2s[i].map == Parameters.STATE_NEW || v2s[i].map == Parameters.STATE_MAP_NODE_FAIL || v2s[i].map == Parameters.STATE_MAP_FAIL || v2s[i].map == Parameters.STATE_MAP_Link_FAIL) {
				ArrayList<Object> list = new ArrayList<Object>();  //��¼�ڵ�ӳ����
				int p[][] = new int[reqs[i].links][sub.nodes];
				int ret[][] = new int[reqs[i].links][4];
				v2s[i].tryMapTime ++;	//
				if(reqs[i].topo == Parameters.TOPO_GENERAL || reqs[i].topo == Parameters.TOPO_STAR) {
					if(Parameters.CurrentVONEMethod == Parameters.MapVONEByDeepReinforceLearning){
						if(Parameters.TrainOrTest) {//训练
							if(MapVONEByDeepReinforceLearningTrain(sub, reqs, i)!=-1){
								if(Parameters.DebugModel) Print_sub_slots(sub);
								v2s[i].map = Parameters.STATE_MAP_SUCC;
								reqs[i].map = Parameters.STATE_MAP_SUCC;
								CalculateEnergyCarbon(sub,reqs,i,Parameters.CurrentVONEMethod,"after embed succ req "+i);
								//DebugVNE(sub,reqs,i,Parameters.CurrentVONEMethod,"after embed succ req "+i);
							} else {
								v2s[i].map = Parameters.STATE_MAP_NODE_FAIL;
								reqs[i].map = Parameters.STATE_MAP_NODE_FAIL;
								//DebugVNE(sub,reqs,i,Parameters.CurrentVONEMethod,"after embed fail req "+i);
							}
						} else {
							if(MapVONEByDeepReinforceLearningTest(sub, reqs, i)!=-1){
								if(Parameters.DebugModel) Print_sub_slots(sub);
								v2s[i].map = Parameters.STATE_MAP_SUCC;
								reqs[i].map = Parameters.STATE_MAP_SUCC;
								CalculateEnergyCarbon(sub,reqs,i,Parameters.CurrentVONEMethod,"after embed succ req "+i);
								//DebugVNE(sub,reqs,i,Parameters.CurrentVONEMethod,"after embed succ req "+i);
							} else {
								v2s[i].map = Parameters.STATE_MAP_NODE_FAIL;
								reqs[i].map = Parameters.STATE_MAP_NODE_FAIL;
								//DebugVNE(sub,reqs,i,Parameters.CurrentVONEMethod,"after embed fail req "+i);
							}
						}
						
					} 
				}

			}
		}
		//successfulRequests += windowSuccessfulRequests;
		//totalRewardSum += windowRewardSum;
		//globalStep += windowSteps;
	}
	
	/******************************************************************
	���ƣ�int MapVONEByDeepReinforceLearningTrain(......)
	���ܣ�������ģ��ӳ�����������, ����ɹ������s2v_n��v2s
	������
		      s2v_nΪ����ڵ�ӳ�������ڵ����ݽṹ
		      s2v_lΪ������·ӳ��������·���ݽṹ
		      v2sΪ����ӳ��������������ݽṹ
		      indexΪ��index����������
	,int ret[],int p[][],ArrayList<Object> list
	����ֵ��0���ɹ����أ�-1��ʧ�ܷ���
	******************************************************************/
	private int MapVONEByDeepReinforceLearningTrain(EOSubstrateNetwork sub,VONRequest reqs[],int index)
	{
		System.out.println("Start Reqs["+index+"]");
		
		//��������ģ�ͺ���С���õ�Ƶ�ײ�����
		//double[][] transModel = new double[reqs[index].nodes][sub.nodes];
		//int[][] indexModel = new int[reqs[index].nodes][sub.nodes];
		//int[][] linkModel = new int[reqs[index].nodes][sub.nodes];
		//InitTranModel(sub,reqs,index,transModel,indexModel,linkModel);

		//��ʼ������,-1������δ���䣬>-1�����Ѿ�����Ľڵ������·��
		int[] vNodeEmbed = new int[reqs[index].nodes];
		int[] sNodeEmbed = new int[sub.nodes];
		int[] vLinkEmbed = new int[reqs[index].links];
		//try {
		//	agent.loadModel(modeName);//.loadModel(modeName);
		//} catch (Exception e) {
        //    e.printStackTrace();
        //}
		
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

		// 计算所有节点的最大碳排放,2026.3.3
		double globalMaxCarbon = 0;
		for (int i = 0; i < sub.nodes; i++) {
			if (sub.node_GHG[i] > globalMaxCarbon) {
				globalMaxCarbon = sub.node_GHG[i];
			}
		}
		
		/////////////////////////////////////////////////////
		//double beta=0.7;//0.1~1
		double[][] currentState = null;
		double reward = 0.0;
		int num = 0;
		int[] minElement = new int[2];//minElement[0]����ڵ㣻minElement[1]����ڵ�;
		if(Parameters.TrainOrTest) {//) {//训练
			// 初始状态cxh
			ResourceState stateGenerator = new ResourceState();
			//currentState = stateGenerator.getStateMatrix(sub,reqs,index,num);
	        //extractState(subCopy,reqs,index,num);
			INDArray state;

			//INDArray actionProbs;//2026.5.16注释，减少训练时间
			float[] actionProbs = null;  // 改成 float 数组！2026.5.16修改，减少训练时间

			while(num < reqs[index].nodes){
				reward = 0;//回报
				//GetVNode(VONRequest reqs[],int index, int[] vNodeEmbed, int[] vLinkEmbed)
				int selectVNode = GetVNode(reqs,index,vNodeEmbed,vLinkEmbed);
				state = stateGenerator.GetState(subCopy,reqs,index,selectVNode);
				//for(int step = 0;step < Parameters.maxStepsPerEpisode;step++) {
					// 选择动作cxh
				actionProbs = agent.forward2(state);//AI思考
				int action = agent.selectAction2(actionProbs, epsilon,subCopy,reqs,index,sNodeEmbed,selectVNode,vLinkEmbed,vNodeEmbed);//选择动作
	            
	            //int action = agent.selectAction(actionProbs, epsilon,sNodeEmbed);
	            if(action == -1) {
	            	reward = -1.0;//return -1;
					//更新网络
					agent.update3(state, action, reward, gamma, learningRate);//.update1(reward, state, action);//.update(state, action, reward, reward, noEmbedVLink);
					System.out.println("Fail Reqs["+index+"]");
					return -1;
	            }
	            // 执行动作（尝试分配节点）cxh
	            boolean nodeSuccess = tryEmbedNode(sub,action,reqs,index,selectVNode);
	            boolean linkSuccess = false;
	            if (nodeSuccess) {
					vNodeEmbed[selectVNode] = action;//保存节点映射
					sNodeEmbed[action] = selectVNode;
					UpdateSub(subCopy,action,reqs[index].cpu[selectVNode]);

					// 1. 获取严谨归一化的落点机房碳因子 [0, 1]，2026.5.12
					double normalizedNodeGHG = sub.node_GHG[action] / globalMaxCarbon;

					// 归一化碳排放惩罚,2026.3.3
					//double normalizedCarbon = sub.node_GHG[action] / globalMaxCarbon;  // 范围 [0,1]
					//double carbonPenalty = -beta * normalizedCarbon;  // beta 为权重系数，例如 0.5
					//reward += carbonPenalty;


	            	// 执行链路映射
	                int noEmbedVLink = -1;
					noEmbedVLink=FindNoEmbedVLink(reqs,index,selectVNode,vNodeEmbed,vLinkEmbed);

					// 累加整条请求所有关联链路的统计指标，2026.5.12
					double totalLinkHops = 0.0;
					double totalPathGHG = 0.0;
					int mappedLinksCount = 0;

					while(noEmbedVLink > -1){
						if(!PreEmbedVLinkByKShortestPath(subCopy,reqs,index,noEmbedVLink,vNodeEmbed,p,ret)){//noEmbedVLink������·��snodeEmbed��Ӧ������ڵ�
							reward = -1.0;//return -1;
							//更新网络
							agent.update3(state, action, reward, gamma, learningRate);//.update1(reward, state, action);//.update(state, action, reward, reward, noEmbedVLink);
							System.out.println("Fail Reqs["+index+"]");
							return -1;
						}

						// 精确计算真实物理路由经过的机房与跳数,2026.5.12
						int validNodeCount = 0;
						double sumPathGHG = 0.0;

						for (int i = 0; i < sub.nodes; i++) {
							int pNodeIndex = p[noEmbedVLink][i];
							if (pNodeIndex == -1) break;
							validNodeCount++;
							sumPathGHG += sub.node_GHG[pNodeIndex];
						}

						double currentLinkHops = (validNodeCount > 1) ? (validNodeCount - 1.0) : 1.0;
						totalLinkHops += currentLinkHops;
						totalPathGHG += (sumPathGHG / validNodeCount);
						mappedLinksCount++;
						//================================
						vLinkEmbed[noEmbedVLink] = 1;
						int sNode1,sNode2;
						sNode1 = vNodeEmbed[reqs[index].link[noEmbedVLink].from];
						sNode2 = vNodeEmbed[reqs[index].link[noEmbedVLink].to];
						retOther[noEmbedVLink][0] = ret[noEmbedVLink][0];
						retOther[noEmbedVLink][1] = ret[noEmbedVLink][0]+ret[noEmbedVLink][1];
						UpdateSub(subCopy,sNode2,sNode1,retOther[noEmbedVLink],p[noEmbedVLink]);
						noEmbedVLink=FindNoEmbedVLink(reqs,index,selectVNode,vNodeEmbed,vLinkEmbed);
					}
					// 2. 【全新升华版复合奖励计算】,2026.5.12
					// 计算当前映射步骤的平均链路跳数，防范分母为0
					double avgHops = (mappedLinksCount > 0) ? (totalLinkHops / mappedLinksCount) : 1.0;
					double avgNormalizedLinkGHG = (mappedLinksCount > 0) ? ((totalPathGHG / mappedLinksCount) / globalMaxCarbon) : normalizedNodeGHG;

					// 基石：继承修改前最强引导，优先压制跳数与EDFA耗电
					double hopEfficiency = 1.0 / avgHops;

					// 创新：论文核心 CEA 因子惩罚，综合考虑节点与沿线机房碳排表现
					// 权重调优建议：0.3 的惩罚力度足以引导走向低碳，且不会喧宾夺主破坏最短路径
					double carbonPenalty = 0.3 * (0.5 * normalizedNodeGHG + 0.5 * avgNormalizedLinkGHG);

					// 最终单步奖励稳定在安全数值区间，智能体感知无比清晰
					reward = hopEfficiency - carbonPenalty;
					//=========================================
	            } else {
	                reward = -1.0; // 节点分配失败
	                //更新网络
	                agent.update3(state, action, reward, gamma, learningRate);//agent.update1(reward, state, action);//.update(state, action, reward, reward, noEmbedVLink);
	                System.out.println("Fail Reqs["+index+"]");
	                return -1;
	            }
	            num ++;
	            //double pathLength = GetPathLength(subCopy,p,noEmbedVLink,sNode2,sNode1);
				//reward += 1/pathLength;
	            //state = stateGenerator.GetState(subCopy,reqs,index,num);
	            //if(reward != 0) {//2026.5.8修改，只要没 return -1，不论 reward 多少都应该让 Agent 学习这个状态转移
	            //	agent.update(state, action, reward, gamma, learningRate);
	            //}
				//只要没 return -1，不论 reward 多少都应该让 Agent 学习这个状态转移
				agent.update3(state, action, reward, gamma, learningRate);
	            //agent.update1(reward, state, action);
			}
			
		}
		
		//����cpu
		UpdateSub(sub,subCopy);
		//��¼�ڵ����·ӳ����
		AddNodesMap(reqs,index,vNodeEmbed);//����s2v_n��v2s
		AddLinksMapBySPFA(sub,reqs,index,retOther,p);//���µײ�����
		System.out.println("Success Reqs["+index+"]");
		

		return 0;//�ɹ�����
	}
	/*
	 * Name: GetVNode()
	 * Function: Select the virtual node with the maximal CPU or connected to the embedded node
	 * Create time: 2026/1/29
	 * Creator: Chen Xiaohua
	 */
	public int GetVNode(VONRequest reqs[],int index, int[] vNodeEmbed, int[] vLinkEmbed) {
		int selectedVNode = -1;
		double maxCPU = -1;
		//找出某个虚拟节点，其连接的另外一个虚拟节点已经被映射，且CPU最大
		for(int i=0;i<reqs[index].links;i++) {
			if(vNodeEmbed[reqs[index].link[i].from] == -1 && vNodeEmbed[reqs[index].link[i].to] > -1) {
				if(reqs[index].cpu[reqs[index].link[i].from] > maxCPU) {
					maxCPU = reqs[index].cpu[reqs[index].link[i].from];
					selectedVNode = reqs[index].link[i].from;
				}
			} else if(vNodeEmbed[reqs[index].link[i].to] == -1 && vNodeEmbed[reqs[index].link[i].from] > -1) {
				if(reqs[index].cpu[reqs[index].link[i].to] > maxCPU) {
					maxCPU = reqs[index].cpu[reqs[index].link[i].to];
					selectedVNode = reqs[index].link[i].to;
				}
			}
		}
		if(maxCPU > 0) return selectedVNode;
		
		//找出CPU最大的节点
		for(int i=0;i<reqs[index].nodes;i++) {
			if (vNodeEmbed[i] == -1 && reqs[index].cpu[i] > maxCPU) {
				maxCPU = reqs[index].cpu[i];
				selectedVNode = i;
			}
		}
		return selectedVNode;
	}
	/*
	 * Function: GetPathLength()
	 * Create time: 2026/1/29
	 * Creator: Chen Xiaohua
	 */
	double GetPathLength(EOSubstrateNetwork sub,int p[],int sNode1,int sNode2)
	{
		double length = 1;
		if(p[sNode2] != -1) {
			while (p[sNode2] != sNode1) {
				length++;
				sNode2 = p[sNode2];
			}
		} else {
			while (p[sNode1] != sNode2) {
				length++;
				sNode1 = p[sNode1];
			}
		}
		return length;
	}

	private int MapVONEByDeepReinforceLearningTest(EOSubstrateNetwork sub,VONRequest reqs[],int index)
	{
		System.out.println("Start Reqs["+index+"]");

		//ģͺСõƵײ
		//double[][] transModel = new double[reqs[index].nodes][sub.nodes];
		//int[][] indexModel = new int[reqs[index].nodes][sub.nodes];
		//int[][] linkModel = new int[reqs[index].nodes][sub.nodes];
		//InitTranModel(sub,reqs,index,transModel,indexModel,linkModel);

		//ʼ,-1δ䣬>-1ѾĽڵ·
		int[] vNodeEmbed = new int[reqs[index].nodes];
		int[] sNodeEmbed = new int[sub.nodes];
		int[] vLinkEmbed = new int[reqs[index].links];
		//try {
		// agent.loadModel(modeName);//.loadModel(modeName);
		//} catch (Exception e) {
		//    e.printStackTrace();
		//}

		InitAllocModel(sub,reqs,index,vNodeEmbed,sNodeEmbed,vLinkEmbed);

		//p[][]:¼·ret[][]:ret[][0]:صʼƵײۣret[][1]:صƵײ
		int p[][] = new int[reqs[index].links][sub.nodes];
		int ret[][] = new int[reqs[index].links][2];//ret[][0]:صʼƵײۣret[][1]:صƵײ
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

		// 计算所有节点的最大碳排放，用于归一化
		double globalMaxCarbon = 0;
		for (int i = 0; i < sub.nodes; i++) {
			if (sub.node_GHG[i] > globalMaxCarbon) {
				globalMaxCarbon = sub.node_GHG[i];
			}
		}

		/////////////////////////////////////////////////////
		double[][] currentState = null;
		double reward = 0.0;
		int num = 0;
		int[] minElement = new int[2];//minElement[0]ڵ㣻minElement[1]ڵ;
		if(true) {//测试阶段
			// 初始状态cxh
			ResourceState stateGenerator = new ResourceState();
			//currentState = stateGenerator.getStateMatrix(sub,reqs,index,num);
			//extractState(subCopy,reqs,index,num);
			INDArray state;

			//INDArray actionProbs;//2026.5.16注释，减少训练时间
			float[] actionProbs = null;  // 改成 float 数组！2026.5.16修改，减少训练时间

			while(num < reqs[index].nodes){
				reward = 0;//回报清空
				int selectVNode = GetVNode(reqs,index,vNodeEmbed,vLinkEmbed);
				state = stateGenerator.GetState(subCopy,reqs,index,selectVNode);

				// 选择动作cxh
				actionProbs = agent.forward2(state);
				//selectActionAblation
				int action = agent.selectAction2(actionProbs, epsilon,subCopy,reqs,index,sNodeEmbed,selectVNode,vLinkEmbed,vNodeEmbed);

				if(action == -1) {
					reward = -1.0; // [同步修改] 失败绝对赋值
					System.out.println("Fail Reqs["+index+"]");
					return -1;
				}

				// 执行动作（尝试分配节点）cxh
				boolean nodeSuccess = tryEmbedNode(sub,action,reqs,index,selectVNode);
				boolean linkSuccess = false;

				if (nodeSuccess) {
					vNodeEmbed[selectVNode] = action;//保存节点映射
					sNodeEmbed[action] = selectVNode;
					UpdateSub(subCopy,action,reqs[index].cpu[selectVNode]);

					// 1. 获取严谨归一化的落点机房碳因子 [0, 1]
					double normalizedNodeGHG = sub.node_GHG[action] / globalMaxCarbon;

					// 执行链路映射
					int noEmbedVLink = FindNoEmbedVLink(reqs,index,selectVNode,vNodeEmbed,vLinkEmbed);

					// 累加整条请求所有关联链路的统计指标
					double totalLinkHops = 0.0;
					double totalPathGHG = 0.0;
					int mappedLinksCount = 0;

					while(noEmbedVLink > -1){
						if(!PreEmbedVLinkByKShortestPath(subCopy,reqs,index,noEmbedVLink,vNodeEmbed,p,ret)){
							reward = -1.0; // [同步修改] 链路失败绝对赋值
							System.out.println("Fail Reqs["+index+"]");
							return -1;
						}
						// 精确计算真实物理路由经过的机房与跳数
						int validNodeCount = 0;
						double sumPathGHG = 0.0;

						// 遍历定长数组，精准捕捉真实途经节点
						for (int i = 0; i < sub.nodes; i++) {
							int pNodeIndex = p[noEmbedVLink][i];
							if (pNodeIndex == -1) {
								break; // 真实物理路径结束
							}
							validNodeCount++;
							sumPathGHG += sub.node_GHG[pNodeIndex];
						}

						double currentLinkHops = (validNodeCount > 1) ? (validNodeCount - 1.0) : 1.0;
						totalLinkHops += currentLinkHops;
						totalPathGHG += (sumPathGHG / validNodeCount);
						mappedLinksCount++;

						vLinkEmbed[noEmbedVLink] = 1;
						int sNode1,sNode2;
						sNode1 = vNodeEmbed[reqs[index].link[noEmbedVLink].from];
						sNode2 = vNodeEmbed[reqs[index].link[noEmbedVLink].to];
						retOther[noEmbedVLink][0] = ret[noEmbedVLink][0];
						retOther[noEmbedVLink][1] = ret[noEmbedVLink][0]+ret[noEmbedVLink][1];
						UpdateSub(subCopy,sNode2,sNode1,retOther[noEmbedVLink],p[noEmbedVLink]);
						noEmbedVLink=FindNoEmbedVLink(reqs,index,selectVNode,vNodeEmbed,vLinkEmbed);
					} // 链路映射 while 结束

					// 2. 【全新升华版复合奖励计算】
					// 计算当前映射步骤的平均链路跳数，防范分母为0
					double avgHops = (mappedLinksCount > 0) ? (totalLinkHops / mappedLinksCount) : 1.0;
					double avgNormalizedLinkGHG = (mappedLinksCount > 0) ? ((totalPathGHG / mappedLinksCount) / globalMaxCarbon) : normalizedNodeGHG;

					// 基石：继承修改前最强引导，优先压制跳数与EDFA耗电
					double hopEfficiency = 1.0 / avgHops;

					// 创新：论文核心 CEA 因子惩罚，综合考虑节点与沿线机房碳排表现
					// 权重调优建议：0.3 的惩罚力度足以引导走向低碳，且不会喧宾夺主破坏最短路径
					double carbonPenalty = 0.3 * (0.5 * normalizedNodeGHG + 0.5 * avgNormalizedLinkGHG);

					// 最终单步奖励稳定在安全数值区间，智能体感知无比清晰
					reward = hopEfficiency - carbonPenalty;

				} else {
					reward = -1.0; // [同步修改] 节点分配失败绝对赋值
					System.out.println("Fail Reqs["+index+"]");
					return -1;
				}
				num ++;
			}
		}

		//cpu
		UpdateSub(sub,subCopy);
		//¼ڵ·ӳ
		AddNodesMap(reqs,index,vNodeEmbed);//s2v_nv2s
		AddLinksMapBySPFA(sub,reqs,index,retOther,p);//µײ
		System.out.println("Success Reqs["+index+"]");

		return 0;//ɹ
	}
	
	
	/*
	 * Function:tryEmbedNode
	 * Create time:2026/01/27
	 * Creator: Chen Xiaohua
	 */
	private boolean tryEmbedNode(EOSubstrateNetwork sub,int sNode,VONRequest reqs[],int index, int vNode) {
		if(sub.cpu[sNode] >= reqs[index].cpu[vNode]) return true;
		else return false;
	}
	
	/*
	 * Function: InitAllocModel()
	 * Create time:2026/01/27
	 * Creator: Chen Xiaohua
	 */
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

}
