package Team.CloudStorage.EAVONE;

public class EOSubstrateNetwork extends SubstrateNetwork{

	public double[] storage;    // 存储资源
	public double[] maxStorage; // 最大存储资源

	public EOSubstrateNetwork() {
		// 初始化存储数组
		this.storage = new double[200];   // 根据实际需要调整大小
		this.maxStorage = new double[200]; // 根据实际需要调整大小

		// ... 其他初始化代码
	}

	public int[][] slots;
	public int[][] linksNo;
	public double[] transRate;
	public double[] opticalReach;
	public String[] modulevel;
	public int slotsNum;
	public double slotGHz;
	public int modulationLevel;
	public int diffSlot;
	//public double[] length;
	public double[] cb_value;
	public double[] sum_adj;
	public  double [] cpuTime;
	public  double [] cpuOnTime;
	public  int [] cpuOn;

	public double smallCpu;//定义的较小的cpu
	public double largeCpu;//定义的较大的cpu
	public  double timeWindowsNumber;//经历的时间窗数量
	public  double EBFA;//基于熵的频谱碎片度量的和
	public  double LargeB;//较大的连续频谱碎片数量
	public  double LargeBS;//较大的连续频谱槽量


	public double [] node_GHG;//20220923 GHG 温室气体  排放量 g CO2 eq/kW h


	public int jihuonodenumber;//休眠节点数量
	public int jihuolinknumber;//休眠链路数量
	public double jihuolength;//休眠链路长度

	//public double [] link_times;
	public int xiumiannodenumber;
	public int xiumianlinknumber;//休眠链路数量


}


