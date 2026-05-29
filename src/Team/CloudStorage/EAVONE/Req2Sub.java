package Team.CloudStorage.EAVONE;
import java.util.LinkedList;

public class Req2Sub {
	public int map;
	public int maptime;
	public int tryMapTime = 0;//映射次数
	public LinkedList<Integer> snode = new LinkedList<Integer>();
	public LinkedList<SpathFlow> pathFlow = new LinkedList<SpathFlow>();
	public LinkedList<Integer> flowLen = new LinkedList<Integer>(); 
	public LinkedList<Integer> startSlotNo = new LinkedList<Integer>();	//分配的频谱起始索引
	public LinkedList<Integer> slotNum = new LinkedList<Integer>();	//分配的频谱数量
	public double energy;
	public double revenue;
	public double GHG;
}
