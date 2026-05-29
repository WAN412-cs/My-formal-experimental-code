package Team.CloudStorage.EAVONE;

public class Parameters {
	//mapState:The state of VN. 
	//1:STATE_NEW
	static int STATE_NEW = 1;
	static int STATE_MAP_LINK = 2;
	static int STATE_DONE = 3;
	static int STATE_EXPIRE = 4;
	static int STATE_MAP_NODE_FAIL = 5;
	static int STATE_MAP_FAIL = 6;
	static int STATE_MAP_NODE = 7;
	static int STATE_MAP_Link_FAIL = 8;
	static int STATE_MAP_SUCC = 9;
	
	static int TOPO_STAR = 0;
	static int TOPO_GENERAL = 1;
	
	static int MAX_VALUE_INT = 999999999;
	static double MAX_VALUE_DOUBLE = 999999999;
	public static double MIN_VALUE_DOUBLE = 0.00000001;
	
	//public final int mapStatee = 2;
	static int TIME_INTERVAL = 100;
	
	static int MIPTIMES = 6;//MIP���ô���
	
	//K���·������
	static int K_PATH = 5;
	
	//EMBED_CATEGORY
	static int MapLinkByMIP = 1;
	static int MapLinkByMIPEnh = 7;
	static int MapLinkBy01ILP = 9;
	static int MapLinkBySPFA = 2;
	static int MapLinkBySPFAEnh = 8;
	static int MapLinkByFA = 3;
	static int MapLinkByFACA = 4;
	static int MapLinkByMIPTimes = 5;
	static int MapLinkByMIPParall = 6;
	
	static int MapVONEBy01ILP = 10;
	static int MapVONE3ByWangY = 11;//
	static int MapVONE3PByWangYAndChenxh = 12;//chenxh�������ǿ·��ӳ��ģ��
	static int MapVONETranModel = 13;//chenxh���������ģ��
	static int MapVONEEnTranModel = 14;//chenxh�������ǿ����ģ��
	static int MapVONE01ILPLin = 15;//lin01ILPģ��
	static int MapVONE01ILPLinnodeilp = 1555;
	static int MapVONE01ILPLinFB = 1512;//lin01ILPģ��+feedback
	static int MapVONE01ILPLinStrong = 1513;//lin01ILPģ��+��չ��������ͼ��������Ƶ�ײ۷���<=1Լ����
	static int MapVONE01ILPLin_EquilibriumCXH = 1511;//lin01ILPģ��
	static int MapVONE01ILPLin_EquilibriumCXHnodeilp = 15111;
	static int MapVONE01ILPLinCXH = 151;//lin01ILPģ��cxh��������ڵ����Ϊ1/ʣ��CPU
	static int MapVONE01ILPPRLinCXH = 152;//lin01ILPģ��cxh��������ڵ�iӳ�䵽����ڵ�j�Ĵ���Ϊac[i,j]=|PR(i)-PR(j)|
	static int MapVONECXHNode = 153;//lin01ILPģ��cxh��������ڵ�iӳ�䵽����ڵ�j�Ĵ���Ϊac[i,j]=(����ڵ�ӳ�䵽����ڵ��ʣ���CPU)/����ڵ��ܵ�CPU
	static int MapVONEPageRank = 16;
	static int MapVONEPageRank_equilibrium = 161;//
	static int MapVONELin_SortByNodeDegree = 17;//lin_algo1
	static int MapVONELin_SortByNodeDegreeAndBW = 18;//lin_algo2
	static int MapVONELin_SortByBW = 19;//lin_algo3
	static int MapVONELin_FB_SortByNodeDegree = 117;//lin_algo1
	static int MapVONELin_FB_SortByNodeDegreeAndBW = 118;//lin_algo2
	static int MapVONELin_FB_SortByBW = 119;//lin_algo3
	static int MapVONELin_FA = 120;//����ͼ�����ϣ�������Ƭ��֪����
	static int MapVONELin_FACA = 120;//����ͼ�����ϣ�������Ƭ��֪������ӵ�����⼼��
	
	static int MapVONETranILPByChenxh = 20;//chenxh����ģ�ͣ�ֻ���ǽڵ��������
	static int MapVONEEnTranILPByChenxh = 21;//chenxh��ǿ����ģ�ͣ����ǽڵ�������ۺ�Ƶ�ײ�
	static int MapVONEEnTranCompILPByChenxh = 22;//chenxh��ǿ����ģ�ͣ����ǽڵ�������ۡ�Ƶ�ײۺͽ�����
	static int MapVONEMIPTranAndPRankByCXH = 23;//MILPģ�ͣ�����������ģ�ͺ�PageRank
	static  int MapVONEILPByY_L =404;//�ܺĸ�֪�������Թ滮ģ��
	static int MapVONEEnergyByPageRank = 101;//����PageRank���ܺĸ�֪ģ��
	static int MapVONEEnergyByPageRankEasy = 102;//����PageRank���ܺĸ�֪ģ��,�򵥵�ӳ�䣬��PageRankֵ��������
	static  int MapVONEEnergyByVogelPageRank=103;//�������
	static int  MapVONEPageRankOfGHG = 104;//����PageRank��̼��֪ģ��
	static int MapVONEPageRankOfGHGByVogel = 105;//ʹ�÷�������Ļ���PageRank��̼��֪ģ��
	static int MapVONEPageRankOfGHGByVogelPro = 1051;//ʹ�÷�������Ļ���PageRank��̼��֪ģ��
	static int MapVONEPageRankOfGHGByVogelcenm = 1055;
	static  int MapVONE_ESE = 222;///WeiWenTing

	static  int MapEVONENodeRank =333;//big_to_big

	static  int MapVONENodeRankZM= 444;//zhumin

	static  int MapVONENodeRank_SubNet= 555;//����������ʽ
	static  int MapVONENCRbyILP=106;//NCR ILP
	static  int MapVONENCRbyILPnodeilp=1066;//ֻ���ǽڵ�ilp��106����ʽ�㷨
	static int CurrentVONEMethod = -1;//11;13;
	static int FBStep = 1;//11;13;
	static  int TestMethod=748;//����
	public static boolean DebugModel = false;//true��ʾ��;false����ʾ�� ����
	static boolean RecordLogModel = true;//true��ʾ��;false����ʾ�� ��־��¼
	static boolean ErrorRecord = false;//true��ʾ��;false����ʾ�� ����
	static boolean StaticRecord = true;//true��ʾ��;false����ʾ�٣���ʾ��¼ͳ�ƽ��
	static int MapVONEAM =816;//����̼�ŷ����Ӻͼ�����Դ�������ģ��
	static int MapVONEAM913 =913;
	static int MapVONEILPAM =8161;//����̼�ŷ����Ӻͼ�����Դ�������ILPģ��//{11,15,1511,106,8161,915};
	static int MapVONEILPAM915 =915;
    static int MapVONEILPAM916 =916;//ֻ���ǽڵ�ilp��913����ʽ�㷨
	static int MapVONEILPAM9166 =9166;//��916�Ļ����ϸı�ڵ�ӳ��Ŀ�꺯��
	static int MapVONE01ILPNodeRank_Sub = 1010;//������ILP

	static int MapVONEDRLMD_VONE = 2280;
	static int MapVONEByDeepReinforceLearning = 2281;//cxh实现深度强化学习2024文章
	public static boolean TrainOrTest =true;//TrainOrTest是训练还是测试，true是训练，false是测试
	public static  int maxStepsPerEpisode = 50;//每次Episode最大的步长
	static int MaxSlots =-1;//��sub�ĵ�һ��������
	
	static double vbwPara = -1;//������·����
	static double vcpuPara = -1;//����cpu����
	
	static double NodeECoEfficient = 0.5;//�ڵ�ӳ�����ϵ��
	static double LinkECoEfficient = 0.5;//��· ӳ�����ϵ��
	
	static int MDSum = 6;//����ϵ������
	
	static int MDBPSK = 1;//3000 km
	static int MDQPSK = 2;//1500 km
	static int MD8QAM = 3;//750 km
	static int MD16QAM = 4;//375 km
	static int MD64QAM = 6;//94 km
	static int MD256QAM = 8;//24 km
	
	static int MDBPSK_Length = 3000;//3000 km
	static int MDQPSK_Length = 1500;//1500 km
	static int MD8QAM_Length = 750;//750 km
	static int MD16QAM_Length = 375;//375 km
	static int MD64QAM_Length = 94;//94 km
	static int MD256QAM_Length = 24;//24 km
	
	static int GuardBand = 1;
	
	static double LinkCost = 0.1;//��·���ۣ�Li�㷨
	static int R = 1000000;//Li�㷨

	static int EPOCHS = 1;           // ѵ���غ����������������Σ�
	static int BATCH_SIZE = 64;        // ����С����ԭ����64��
	static double RT_DECAY_RATE = 0.0005; // rt ˥�����ʣ��ɵ���
	static int LOG_EVERY_EPOCHS = 5;   // ÿ5��epoch��ӡһ��ͳ��

}
