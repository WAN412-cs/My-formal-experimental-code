package Team.CloudStorage.EAVONE;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

import edu.asu.emit.algorithm.graph.Path;
import edu.asu.emit.algorithm.graph.abstraction.BaseVertex;
import edu.asu.emit.qyan.test.YenTopKShortestPathsAlgTest;

import javax.swing.*;

public class VNE {
    public String SNFile;
    public String VNsFileDir;
    public static EOSubstrateNetwork sub;// = new EOSubstrateNetwork();;
    public static EOSubstrateNetwork subStatic;
    public static VONRequest reqs[];
    public static S2VNode s2v_n[];
    public static S2VLink s2v_l[];
    public static Req2Sub v2s[];
    public int embedModelOrAlgo = -1;
    public double cpuSRate = 0;//�ܵ�cpu������ֵ
    public double resSRate = 0;//�ܵ�res������ֵ
    public double slotSRate = 0;//�ܵ�slot������ֵ
    //public int B;
    //public int M;
    public double GHGByTimeWindow = 0;
    public double energyByTimeWindow = 0;
    public double GHGwithoutdegrees = 0;
    public double energyWithoutdegrees = 0;
    //public void VNE(EOSubstrateNetwork sub,VONRequest reqs[],int reqsNum)
    //{
    //	Init(sub,reqs,reqsNum);
    //}
    public static List<Integer> zhanyonglinkarray = new ArrayList<>();
    public static List<Integer> activatedNodeList = new ArrayList<>();

    public void Recordxiumian(EOSubstrateNetwork sub) {
        int jhnodenumber = 0;
        int xmnodenumber = 0;
        int zhanyonglinknumber = 0;
        int xmlinknumber = 0;

        double jhlength = 0;
        for (int i = 0; i < sub.nodes; i++) {
            if (sub.cpu[i] != sub.maxcpu[i] && !activatedNodeList.contains(i)) {
                jhnodenumber = jhnodenumber + 1;
                activatedNodeList.add(i);
            }
        }
        sub.jihuonodenumber = jhnodenumber + sub.jihuonodenumber;
        sub.xiumiannodenumber = sub.nodes - jhnodenumber;
        for (int i = 0; i < sub.links; i++) {
            for (int j = 0; j < sub.slotsNum; j++) {
                if (sub.slots[i][j] == 1) {

                } else {
                    zhanyonglinknumber = zhanyonglinknumber + 1;
                    zhanyonglinkarray.add(i);
                    break;
                }
            }
        }
        xmlinknumber = sub.links - zhanyonglinknumber;
        sub.xiumianlinknumber = sub.xiumianlinknumber + xmlinknumber;
        for (int j = 0; j < zhanyonglinkarray.size(); j++) {
            jhlength = jhlength + sub.link[zhanyonglinkarray.get(j)].length;
        }
        sub.jihuolength = jhlength + sub.jihuolength;


    }

    public double[] InitSNodeAMnew(double sNodePageRank[], EOSubstrateNetwork sub) {
        double[][] array = new double[sub.nodes][sub.nodes];
        int[][] degreeArray = new int[sub.nodes][sub.nodes];
        //����ӳ����ڽӾ���  ֵΪӳ��Ĵ���
        for (int i = 0; i < s2v_l.length; i++) {
            if (s2v_l[i].req.size() != 0) {
                array[sub.link[i].from][sub.link[i].to] = sub.link[i].bw - s2v_l[i].rest_bw;
                array[sub.link[i].to][sub.link[i].from] = sub.link[i].bw - s2v_l[i].rest_bw;
                degreeArray[sub.link[i].from][sub.link[i].to] += s2v_l[i].req.size();
                degreeArray[sub.link[i].to][sub.link[i].from] += s2v_l[i].req.size();
            }
        }
//        for (int i = 0; i < reqs[index].nodes; i++) {
//            for (int j = 0; j <ilpsubnet.size(); j++) {
//                int physicalNode = ilpsubnet.get(j);
//                if (s2v_n[physicalNode].rest_cpu >= reqs[index].cpu[i]) {
//                    int degreeValue = 0;
//                    for (int k = 0; k < sub.nodes; k++) {
//                        degreeValue += degreeArray[j][k]; // �ۼӽڵ�j���������Ӷ���
//                    }

        for (int i = 0; i < sub.nodes; i++) {
            for (int j = 0; j < sub.nodes; j++) {
                // Ϊÿ������ڵ�j����ʵʱ����
                // �����ܺĲ������Ӷ���Ӱ�� (150+1329.33+120+150+85*degreeValue+80)
                //double baseEnergy = sub.node_GHG[j] * 400;
                //double baseEnergy = sub.node_GHG[i] * (400 + 120 + 132.933);
                //�µļ��㷽ʽ
                double baseEnergy = sub.node_GHG[i] * (20 + 100 + 40 + 2.5 + 400);
                //double baseEnergy = sub.node_GHG[j] * (400+120+132.933+85*degreeValue);
                //double baseEnergy = sub.node_GHG[j] * (150 + 1329.33 + 120 + 150 + 85*degreeValue + 80) * 100 / 3600000;
                // ��̬�ܺı���ԭ���㷽ʽ


                //double dynamicEnergy = sub.node_GHG[i] * ((600 / sub.maxcpu[i]) + (0.18 + 0.465) * array[i][j]);
                //�µļ��㷽ʽ
                double dynamicEnergy = sub.node_GHG[i] * ((600 / sub.maxcpu[i]) + 0.075 * array[i][j]);
                //double dynamicEnergy = sub.node_GHG[i] * 600*0.18*0.465 / sub.maxcpu[i];
                //double dynamicEnergy = sub.node_GHG[j]*150*(sub.maxcpu[j]-sub.cpu[j])/sub.maxcpu[j]*100/3600000*reqs[index].cpu[i];
                sNodePageRank[i] = baseEnergy + dynamicEnergy;
            }
        }
        return sNodePageRank;

    }

    public double[] InitSNodeAMnew1(double sNodePageRank[], List<Integer> subnet, int index) {
        double[][] array = new double[sub.nodes][sub.nodes];
        int[][] degreeArray = new int[sub.nodes][sub.nodes];

        //����ӳ����ڽӾ���  ֵΪӳ��Ĵ���
        for (int i = 0; i < s2v_l.length; i++) {
            if (s2v_l[i].req.size() != 0) {
                array[sub.link[i].from][sub.link[i].to] = sub.link[i].bw - s2v_l[i].rest_bw;
                array[sub.link[i].to][sub.link[i].from] = sub.link[i].bw - s2v_l[i].rest_bw;
                degreeArray[sub.link[i].from][sub.link[i].to] += s2v_l[i].req.size();
                degreeArray[sub.link[i].to][sub.link[i].from] += s2v_l[i].req.size();
            }
        }

        for (int i = 0; i < reqs[index].nodes; i++) {
            for (int j = 0; j< subnet.size(); j++) {
                int physicalNode = subnet.get(j);
                if (s2v_n[physicalNode].rest_cpu >= reqs[index].cpu[i]) {
                    // ��ȷ��������ڵ�Ķ���
                    int degreeValue = 0;
                    for (int k = 0; k < sub.nodes; k++) {
                        degreeValue += degreeArray[physicalNode][k];
                    }

                    if (Math.abs(s2v_n[physicalNode].rest_cpu - sub.maxcpu[physicalNode]) < 0.0001) {
                        // ���Ƕ���Ӱ��Ļ����ܺļ���
                        double baseEnergy = sub.node_GHG[physicalNode] * (400 + 120 + 132.933 + 85 * degreeValue + 80);
                        double dynamicEnergy = sub.node_GHG[physicalNode] * 600 * 0.18 * 0.465 * reqs[index].cpu[i] / sub.maxcpu[physicalNode];
                        sNodePageRank[physicalNode] = baseEnergy + dynamicEnergy;
                    } else {
                        // �Ѽ���ڵ�ֻ����㶯̬�ܺ�
                        sNodePageRank[physicalNode] = sub.node_GHG[physicalNode] * 600 * 0.18 * 0.465 * reqs[index].cpu[i] / sub.maxcpu[physicalNode];
                    }
                }
            }
        }
        return sNodePageRank;
    }

    public double[] InitSNodeAMnew2(double sNodePageRank[], EOSubstrateNetwork sub, int index,double sumOtherVirtualCPU) {
        double[][] array = new double[sub.nodes][sub.nodes];
        int[][] degreeArray = new int[sub.nodes][sub.nodes];

        // ������·�ϵĴ���Ͷ�����Ϣ
        for (int i = 0; i < s2v_l.length; i++) {
            if (s2v_l[i].req.size() != 0) {
                array[sub.link[i].from][sub.link[i].to] = sub.link[i].bw - s2v_l[i].rest_bw;
                array[sub.link[i].to][sub.link[i].from] = sub.link[i].bw - s2v_l[i].rest_bw;
                degreeArray[sub.link[i].from][sub.link[i].to] += s2v_l[i].req.size();
                degreeArray[sub.link[i].to][sub.link[i].from] += s2v_l[i].req.size();
            }
        }

            for (int j = 0; j < sub.nodes; j++) {

                    // ��������ڵ�Ķ�
                    int degreeValue = 0;
                    for (int k = 0; k < sub.nodes; k++) {
                        degreeValue += degreeArray[j][k];
                    }

                    double Fci = sub.node_GHG[j];
                    //double Efi = 400 + 120 + 1329.33;
                    double Efi = 20 + 100 + 40 + 2.5 + 400;

                    double Eli = 600;
                    double Csi = sub.maxcpu[j];

                    // ? ���������ڵ� j ���Ѿ�ӳ�������ڵ�ļ�����Դ�ܺ�
                    // ������֪����ڵ����CPU + ��ǰ�ڵ��CPU

                        // �Ǽ���ڵ㣬���ǹ̶��ܺ�
                        double dynamicEnergy = Fci * Eli / Csi;
                        double baseEnergy = Fci * Efi ;
                        sNodePageRank[j] = baseEnergy + dynamicEnergy;
                        // �Ѽ���ڵ㣬�����Ƕ�̬�ܺ�

                    }
        return sNodePageRank;
    }


    public double[] InitSNodeAM(double sNodePageRank[], EOSubstrateNetwork sub) {
        for (int i = 0; i < sub.nodes; i++) {
            //sNodePageRank[i] = sub.maxcpu[i]/(sub.node_GHG[i] + sub.maxcpu[i] * sub.node_GHG[i]) ;
            sNodePageRank[i] = (sub.node_GHG[i] + sub.maxcpu[i] * sub.node_GHG[i])/sub.maxcpu[i] ;
        }
        return sNodePageRank;

    }

    public double[] InitSNodeAM2(double sNodePageRank[], EOSubstrateNetwork sub) {

        for (int i = 0; i < sub.nodes; i++) {
            //sNodePageRank[i]=sub.maxcpu[i]/(sub.node_GHG[i]+sub.maxcpu[i]*sub.node_GHG[i]);
            //sNodePageRank[i]=sub.cpu[i]/(sub.node_GHG[i]+sub.cpu[i]*sub.node_GHG[i]);
            sNodePageRank[i] = (sub.node_GHG[i] + sub.maxcpu[i] * sub.node_GHG[i]) / sub.maxcpu[i];
            //sNodePageRank[i]=(sub.node_GHG[i]+sub.cpu[i]*sub.node_GHG[i])/sub.cpu[i];
        }
        return sNodePageRank;

    }

    public double[] InitSNodeAM1(double sNodePageRank[], List<Integer> subnet) {
        for (int i = 0; i < subnet.size(); i++) {
            int nodeIndex = subnet.get(i);
            sNodePageRank[i] = (sub.node_GHG[nodeIndex] + sub.cpu[nodeIndex] * sub.node_GHG[nodeIndex]) / sub.cpu[nodeIndex];
        }
        return sNodePageRank;
    }

    public void calculateCpu(EOSubstrateNetwork sub) {
//����CPU��Ƭ��
        for (int i = 0; i < sub.nodes; i++) {
            if (sub.cpu[i] < 1) {
                sub.smallCpu++;
            } else if (sub.cpu[i] > 50) {
                sub.largeCpu++;
            }

        }

        double EBFM = 0;
        //�����ص���Ƭ����
        double LargeB = 0;
        //�ϴ�Ƶ�ײ�����
        for (int i = 0; i < sub.links; i++) {
            double consecutive1 = 0;//����Ƶ�ײ�����
            double rowSum = 0;

            for (int j = 0; j < sub.slotsNum; j++) {
                if (sub.slots[i][j] == 1) {
                    consecutive1++;
                } else if (consecutive1 > 0) {//����Ƶ������
                    rowSum += (consecutive1 / sub.slotsNum) * Math.log(sub.slotsNum / consecutive1);
                    if (consecutive1 > 5) {//����Ƶ�׳�����
                        sub.LargeB++;
                        sub.LargeBS += consecutive1;
                    }
                    consecutive1 = 0;
                }
            }

            if (consecutive1 > 0) {
                rowSum += (consecutive1 / sub.slotsNum) * Math.log(sub.slotsNum / consecutive1);
                if (consecutive1 > 5) {//����Ƶ�׳�����
                    sub.LargeB++;
                    sub.LargeBS += consecutive1;
                }
            }
            EBFM += rowSum;
        }
        sub.EBFA += EBFM;
        sub.timeWindowsNumber++;

    }

    public void VONEEmbed(String inSNFile, String inVNsFileDir, int reqsNum, int delay) throws IOException {
        //����SN��VNs
        SNFile = inSNFile;
        VNsFileDir = inVNsFileDir;

        sub = new EOSubstrateNetwork();
        CreateSN(sub);

        subStatic = new EOSubstrateNetwork();
        CreateSN(subStatic);

        reqs = new VONRequest[reqsNum];
        CreateVNs(reqs, reqsNum);

        System.out.println("It has already succeeded in creating the SN and VN Requests.");

        Init(sub, reqs, reqsNum);

        return;
    }

    //Init the v2s,s2v_n,s2v_l
    public void Init(EOSubstrateNetwork sub, VONRequest reqs[], int reqsNum) {
        v2s = new Req2Sub[reqsNum];
        for (int i = 0; i < reqsNum; i++) {
            v2s[i] = new Req2Sub();
            v2s[i].map = Parameters.STATE_NEW;
        }


        s2v_n = new S2VNode[sub.nodes];
        for (int i = 0; i < sub.nodes; i++) {
            s2v_n[i] = new S2VNode();
            s2v_n[i].rest_cpu = sub.cpu[i];
        }

        s2v_l = new S2VLink[sub.links];
        for (int i = 0; i < sub.links; i++) {
            s2v_l[i] = new S2VLink();
            s2v_l[i].rest_bw = sub.link[i].bw;
        }
    }


    /*���ƣ�void FindNoEmbedVLink(......)
     * ���ܣ�Ѱ��������ڵ����ӵ�δӳ���������·
     * ������
     * 	reqsΪ����������
     * 	indexΪ��index����������
     * 	vNodeΪ����ڵ㣻
     * 	vnodeEmbedΪ����ڵ�ӳ��ģ��
     * 	vlinkEmbedΪ������·ӳ��ģ��
     * ����ֵ��     ��vLink���ӵ�δӳ���������·��-1��ʾδ�ҵ���
     */
    public int FindNoEmbedVLink(VONRequest reqs[], int index, int vNode, int[] vnodeEmbed, int[] vlinkEmbed) {
        //�ҵ�������ڵ�vLink���ӵ���·
        for (int i = 0; i < reqs[index].links; i++) {
            if (reqs[index].link[i].from == vNode || reqs[index].link[i].to == vNode) {
                if (vlinkEmbed[i] == -1) {
                    if (reqs[index].link[i].from == vNode && vnodeEmbed[reqs[index].link[i].to] != -1)
                        return i;//�ҵ���δӳ���������·
                    if (reqs[index].link[i].to == vNode && vnodeEmbed[reqs[index].link[i].from] != -1)
                        return i;//�ҵ���δӳ���������·
                    //if (reqs[index].link[i].from == vNode)
                    //    return i;//�ҵ���δӳ���������·
                    //if (reqs[index].link[i].to == vNode)
                    //    return i;//�ҵ���δӳ���������·
                }
            }
        }
        return -1;//û���ҵ�������·
    }

    /*
     * ����Yen�㷨�ҵ�k���·��
     */
    public int GetKShortestPath(EOSubstrateNetwork sub, int sNode1, int sNode2, DistanceParent[][] kSPath) {
        //start:����Yen�㷨�ҵ�k���·��
        //дͼ�ļ�
        String graphData = "graph.data";
        WriteFileOfGraph(sub, graphData);
        List<Path> myPath;
        YenTopKShortestPathsAlgTest myTest = new YenTopKShortestPathsAlgTest(graphData);
        myPath = myTest.testYenShortestPathsAlg(Parameters.K_PATH, sNode1, sNode2);
        if (Parameters.DebugModel == true) {
            System.out.println("cxh:" + myPath);
            System.out.println("size:" + myPath.size());
        }

        //DistanceParent[][] kSPath = new DistanceParent[Parameters.K_PATH][sub.nodes];//�洢k���·�������ݽṹ
        //DistanceParent[] sPath  = new DistanceParent[sub.nodes];
        int oNode1 = -1;
        int oNode2 = -1;
        for (int i = 0; i < myPath.size(); i++) {
            if (Parameters.DebugModel == true) {
                System.out.println(myPath.get(i).getVertexList());
                System.out.println(myPath.get(i).getWeight());
            }
            List<BaseVertex> myVertex = myPath.get(i).getVertexList();
            int node1 = myVertex.get(0).getId();
            oNode1 = node1;
            //sPath[node1] = new DistanceParent(node1,length);
            for (int j = 0; j < myVertex.size(); j++) {
                //System.out.println(myVertex.get(j));
                int node2 = myVertex.get(j).getId();
                double length = myPath.get(i).getWeight();//myVertex.get(j).getWeight();
                if (Parameters.DebugModel == true) {
                    System.out.println("cxh:" + node2 + " length:" + length);
                }
                kSPath[i][node1] = new DistanceParent(node2, length);
                node1 = node2;
            }
            oNode2 = node1;
        }
        if (Parameters.DebugModel == true) {
            //WeightedDirectedGraph myGraph1 = new WeightedDirectedGraph(sub.nodes);
            //myGraph1.CreateDireGraph(sub.nodes);//�����ڵ�
            //myGraph1.CreateEdge(sub);//������

            //for(int i=0;i<myPath.size();i++){
            //	System.out.println("path:"+i);
            //	myGraph1.PrintPath(oNode2,oNode1,kSPath[i]);
            //}
        }
        return myPath.size();
    }

    /*
     * ����Yen�㷨�ҵ�k���·��
     */
    public int GetKShortestPath1(WeightedDirectedGraph sub, int sNode1, int sNode2, DistanceParent[][] kSPath) {
        //start:����Yen�㷨�ҵ�k���·��
        //дͼ�ļ�
        String graphData = "graph.data";
        WriteFileOfGraph1(sub, graphData);
        List<Path> myPath;
        YenTopKShortestPathsAlgTest myTest = new YenTopKShortestPathsAlgTest(graphData);
        myPath = myTest.testYenShortestPathsAlg(Parameters.K_PATH, sNode1, sNode2);
        if (Parameters.DebugModel == true) {
            System.out.println("cxh:" + myPath);
            System.out.println("size:" + myPath.size());
        }

        //DistanceParent[][] kSPath = new DistanceParent[Parameters.K_PATH][sub.nodes];//�洢k���·�������ݽṹ
        //DistanceParent[] sPath  = new DistanceParent[sub.nodes];
        int oNode1 = -1;
        int oNode2 = -1;
        for (int i = 0; i < myPath.size(); i++) {
            if (Parameters.DebugModel == true) {
                System.out.println(myPath.get(i).getVertexList());
                System.out.println(myPath.get(i).getWeight());
            }
            List<BaseVertex> myVertex = myPath.get(i).getVertexList();
            int node1 = myVertex.get(0).getId();
            oNode1 = node1;
            //sPath[node1] = new DistanceParent(node1,length);
            for (int j = 0; j < myVertex.size(); j++) {
                //System.out.println(myVertex.get(j));
                int node2 = myVertex.get(j).getId();
                double length = myPath.get(i).getWeight();//myVertex.get(j).getWeight();
                if (Parameters.DebugModel == true) {
                    System.out.println("cxh:" + node2 + " length:" + length);
                }
                kSPath[i][node1] = new DistanceParent(node2, length);
                node1 = node2;
            }
            oNode2 = node1;
        }
        return myPath.size();
    }

    /*����ÿ��·������Ҫ��slots����
     *
     *����ֵ��<=0��ʾʧ�ܣ�
     */
    private int GetSlotNumByOnePath(AuxiliaryGraph auxGraph, DistanceParent[] shortestPath, int pathNum, int sNode1, int sNode2, double bw, double pathLen[]) {
        //�ҵ���·����slots������[BWd/(12.5*mp)],mp=1,2,3,4,BPSK:3000km,QPSK:1500km,8QAM:750km,16QAM:375km
        int sNode3 = sNode2;
        int sNode4 = sNode2;
        int linkNo = -1;
        int mp = -1;
        if (shortestPath[sNode2] == null) return 0;
        int pathLength = 0;
        while (shortestPath[sNode2].parentVert != sNode1) {
            sNode3 = sNode2;
            sNode2 = shortestPath[sNode2].parentVert;
            if (sNode3 == sNode4) continue;
            linkNo = GetLinkNum(auxGraph, sNode3, sNode2);
            pathLength += auxGraph.link[linkNo].length;
            //if(auxGraph.link[linkNo].length <= 375 && mp < 4) mp = 4;
            //else if(auxGraph.link[linkNo].length <= 750 && mp < 3) mp = 3;
            //else if(auxGraph.link[linkNo].length <= 1500 && mp < 2) mp = 2;
            //else if(auxGraph.link[linkNo].length <= 3000 && mp < 1) mp = 1;
        }
        System.out.println("pathLength:" + pathLength);
        pathLen[0] = pathLength;
        return CalculateSlots(bw, pathLength);//GetSlotsByLength(pathLength,bw);
        //if(pathLength <= 375) mp = 4;
        //else if(pathLength <= 750) mp = 3;
        //else if(pathLength <= 1500) mp = 2;
        //else if(pathLength <= 3000) mp = 1;
        //else return 0;//����3000km�����޷�����
        //int slotNum = (int) (Math.floor(bw/(12.5*mp))+1);
        //return slotNum;
    }

    /*����:�õ�path��Ӧ��������·ӳ���·�����
	 * ����ֵ��-1:ʧ�ܣ�>=0���ɹ����ء�

	//
	public int GetSlotsByLength(double pathLength,double bw)
	{
		int mp = -1;
		if(pathLength <= 375) mp = 4;
		else if(pathLength <= 750) mp = 3;
		else if(pathLength <= 1500) mp = 2;
		else if(pathLength <= 3000) mp = 1;
		else return 0;//����3000km�����޷�����
		int slotNum = (int) (Math.floor(bw/(12.5*mp))+1);
		return slotNum;
	}*/
    /*����:�õ�path��Ӧ��������·ӳ���·�����
     * ����ֵ��-1:ʧ�ܣ�>=0���ɹ����ء�
     */
    //
    public int GetPathNoInVirtualLinkAndPath(int path, int[][] pathNo, int[] pathEff, VONRequest reqs[], int index) {
        for (int i = 0; i < reqs[index].links; i++)//ÿ��������·
        {
            for (int j = 0; j < pathEff[i]; j++)//ÿ��·��
            {
                if (pathNo[i][j] == path) return j;
            }
        }
        return -1;
    }

    /*����:���ÿ��������ÿ��·����slot����ֵ
     * 	path:ĳ��������·��Ӧ��kPath�ĵ�path��·����path={0,1,..,Parameters.K_PATH-1}
     * ����ֵ��-1��ʧ�ܣ�û���ҵ����ʵ�slots��;
     * >=0���ɹ�����slots��
     */
    public int EffectSlotOnPath(AuxiliaryGraph auxGraph, DistanceParent[][][] kShortestPath, int[][] pathSlots, int path, int slotNum, int vLinkNo, VONRequest reqs[], int index) {
        if (path < 0) return -1;
        //ĳ��·��shortestPath[vLinkNo][sNode2]��ĳ��slot����,������·vLinkNo
        int vNode1 = -1, vNode2 = -1, sNode1 = -1, sNode2 = -1, sNode3 = -1, sNode4 = -1;
        vNode1 = reqs[index].link[vLinkNo].from;
        vNode2 = reqs[index].link[vLinkNo].to;
        sNode1 = auxGraph.virtualNodes[vNode1];
        sNode2 = auxGraph.virtualNodes[vNode2];
        sNode3 = sNode2;

        int slotNumT = slotNum;
        boolean find = true;
        //��slotNumT���pathSlots[vLinkNo][path]��slot�Ƿ�����Ҫ��
        while (slotNumT < auxGraph.slotsNum) {//Ƶ��slot����
            find = true;
            //System.out.println(vLinkNo+"-"+path+"-"+sNode3);
            while (kShortestPath[vLinkNo][path][sNode3] != null && kShortestPath[vLinkNo][path][sNode3].parentVert != sNode1) {
                sNode4 = sNode3;
                sNode3 = kShortestPath[vLinkNo][path][sNode3].parentVert;
                if (sNode4 == sNode1 || sNode4 == sNode2 || sNode3 == sNode1 || sNode3 == sNode2) {
                    continue;
                }
                int link = GetLinkNum(auxGraph, sNode4, sNode3);

                //����slotNumT��slotNumT+pathSlots[vLinkNo][path]��slots�Ƿ�����Ҫ��
                if (Parameters.DebugModel) System.out.println("auxGraph.slots[][]=");
                int slotSum = 0;
                for (int i = slotNumT; i < slotNumT + pathSlots[vLinkNo][path] && i < Parameters.MaxSlots; i++) {
                    slotSum++;
                    //System.out.println(slotNumT+"-"+link);
                    if (slotNumT >= auxGraph.slotsNum) return -1;
                    if (Parameters.DebugModel)
                        System.out.println(link + " " + i + ":" + auxGraph.slots[link][i] + "\r\n");
                    if (auxGraph.slots[link][i] == 0) {
                        find = false;
                        slotNumT++;
                        break;
                    }
                }
                if (slotSum < pathSlots[vLinkNo][path]) find = false;
                if (Parameters.DebugModel)
                    System.out.println("find:" + find + " slotNumT:" + slotNumT + " slotSum:" + slotSum + " nPathSlots:" + pathSlots[vLinkNo][path]);
                if (slotSum < pathSlots[vLinkNo][path]) return -1;//find = false;

            }
            if (find == true) return slotNumT;
        }
        return -1;
    }

    /*���ÿ��������ÿ��·����MDֵ
     * CalPathMD(pathMD,pathLength,reqs,index);
     */
    public boolean CalPathMD(int[][] pathMD, double[][] pathLength, int[] pathEff, VONRequest reqs[], int index) {
        boolean find = false;
        for (int i = 0; i < reqs[index].links; i++)//ÿ��������·
        {
            for (int j = 0; j < pathEff[i]; j++)//ÿ��·��
            {
                pathMD[i][j] = CalMD(reqs[index].link[i].bw, pathLength[i][j]);
                System.out.println("pathMD[][]=" + i + " " + j + " " + pathMD[i][j] + " bw:" + reqs[index].link[i].bw + " plen:" + pathLength[i][j]);
                if (pathMD[i][j] > -1) find = true;
            }
        }
        return find;
    }


    /*���ÿ��������ÿ��·����slots����
     *
     */
    public void CalculatePathSlotsAndEffects(AuxiliaryGraph auxGraph, DistanceParent[][][] kShortestPath, int[][] pathSlots, int[][] pathLength, int[][] pathNo, int[] pathEff, VONRequest reqs[], int index, double pathLen[][]) {
        int pathNum = 0;
        double pa[] = new double[1];
        for (int i = 0; i < reqs[index].links; i++)//ÿ��������·
        {
            for (int j = 0; j < pathEff[i]; j++)//ÿ��·��
            {
                int node1 = auxGraph.virtualNodes[reqs[index].link[i].from];
                int node2 = auxGraph.virtualNodes[reqs[index].link[i].to];

                pathSlots[i][j] = GetSlotNumByOnePath(auxGraph, kShortestPath[i][j], j, node1, node2, reqs[index].link[i].bw, pa);//����ÿ��·������Ҫ��slots����
                pathLen[i][j] = pa[0];
                pathLength[i][j] = GetPathJump(auxGraph, kShortestPath[i][j], j, node1, node2);//����ÿ��·���ϵ�����
                //System.out.println(x);
                if (pathLength[i][j] <= 2 || pathSlots[i][j] == -1) {
                    pathNo[i][j] = -1;
                    pathNum++;
                } else {
                    pathNo[i][j] = pathNum;
                    System.out.println("pathNo[" + i + "," + j + "]:" + pathNum + " pathJump:" + pathLength[i][j] + " slots:" + pathSlots[i][j]);
                    PrintKShortestPath(auxGraph, kShortestPath[i][j], j, node1, node2);
                    pathNum++;
                }
            }
        }
    }

    /******************************************************************
     *����:void PreEmbedVLinkByKShortestPath(......)
     *����:Ԥ��ӳ��������·
     *����:reqsΪ����������
     *	  indexΪ��index����������
     *    noEmbedVLinkΪ������·��
     *    vnodeEmbedΪ����ڵ�ӳ����
     *    p[noEmbedVLink][]:������·noEmbedVLinkӳ�������·��
     *	  ret[noEmbedVLink][0]:Ƶ�ײ���ʼ������ret[noEmbedVLink][1]:Ƶ�ײ�����
     *����ֵ: false:δ�ҵ���ʧ�ܷ��أ�
     *    true:��K��·�����ҵ����ʵ�����Ƶ�ײۣ�·��������p[noEmbedVLink],ret[noEmbedVLink][0] = findSlotIndex;ret[noEmbedVLink][1] = slotInPath[i];
     ******************************************************************/
    public boolean PreEmbedVLinkByKShortestPath(EOSubstrateNetwork sub, VONRequest reqs[], int index, int noEmbedVLink, int[] vNodeEmbed, int p[][], int ret[][]) {
        //CreateKShortestPath(sub,reqs,index);
        //�ҵ���Ӧ�������˵�
        int sNode1, sNode2;
        sNode1 = vNodeEmbed[reqs[index].link[noEmbedVLink].from];
        sNode2 = vNodeEmbed[reqs[index].link[noEmbedVLink].to];
        //
        DistanceParent[][] kSPath = new DistanceParent[Parameters.K_PATH][sub.nodes];//�洢k���·�������ݽṹ

        int pathRet = -1;
        pathRet = GetKShortestPath(sub, sNode1, sNode2, kSPath);


        //DistanceParent[][] path = myGraph.kShortestPath;//sNode2----->path[sNode2].parentVert
        double[] pathLength = new double[Parameters.K_PATH];
        int[] slotInPath = new int[Parameters.K_PATH];
        int findSlotIndex = -1;
        int i = 0;
        //int pathRet = myPath.size();//·��������
        for (i = 0; i < pathRet; i++) {//ÿ��·��i
            //��¼·��ӳ��������������·noEmbedVLinkӳ�䵽·��p[noEmbedVLink]
            WeightedDirectedGraph myGraph = new WeightedDirectedGraph(sub.nodes);
            //myGraph.CreateDireGraph(sub.nodes);//�����ڵ�
            myGraph.CreateEdge(sub);//������
            if(Parameters.DebugModel) {
                System.out.println("GetPath:" + sNode1 + "-" + sNode2);
            }
            myGraph.GetPath(p[noEmbedVLink], sNode2, sNode1, kSPath[i]);//����p
            if(Parameters.DebugModel) {
                PrintPath(p[noEmbedVLink], sNode2, sNode1);
            }
            pathLength[i] = GetPathLength(sub, kSPath[i], sNode1, sNode2);//�����·�����ȣ�Ϊ����ļ���Ƶ�ײ���׼��
            if(Parameters.DebugModel) {
                System.out.println("path[" + i + "].length:" + pathLength[i]);
            }
            slotInPath[i] = CalculateSlots(reqs[index].link[noEmbedVLink].bw, pathLength[i]);//����Ƶ�ײ�����
            if (slotInPath[i] <= 0) continue;
            //����Ƿ�����������Ҫ��
            if(Parameters.DebugModel) {
                System.out.println("path[" + i + "].length:" + pathLength[i] + " slotsNum:" + slotInPath[i]);
            }
            if(Parameters.DebugModel) {
                myGraph.PrintPath(sNode2, sNode1, kSPath[i]);
            }
            findSlotIndex = CheckIfEnoughSlotsOnPath(sub, kSPath[i], 0, slotInPath[i], sNode1, sNode2);//Ѱ������Ҫ���Ƶ�ײ�����
            if (findSlotIndex == -1) {
                continue;//��ǰ·����Ƶ�ײ�����0��ʼ��û���ҵ���Ӧ������Ƶ�ײ�
            }
            ret[noEmbedVLink][0] = findSlotIndex;
            ret[noEmbedVLink][1] = slotInPath[i];
            break;//�ҵ���·��i��Ƶ�ײ�����findSlotIndex
        }
        if (i == pathRet) return false;//��K��·����û���ҵ����ʵ�����Ƶ�ײ�
            //UpdateSub(subCopy,sNode2,sNode1,ret[i],p[i]);
        else
            return true;//��K��·�����ҵ����ʵ�����Ƶ�ײۣ�·��������p[noEmbedVLink],ret[noEmbedVLink][0] = findSlotIndex;ret[noEmbedVLink][1] = slotInPath[i];
    }


    public static int GetLinkNum1(EOSubstrateNetwork sub, int sNode1, int sNode2) {
        int linkNum = -1;
        for (int i = 0; i < sub.links; i++) {
            if ((sub.link[i].from == sNode1 && sub.link[i].to == sNode2) || (sub.link[i].from == sNode2 && sub.link[i].to == sNode1)) {
                linkNum = i;
                break;
            }
        }
        return linkNum;
    }

    public static int getNodeDegree(int node, EOSubstrateNetwork sub) {
        int degree = 0;
        for (int i = 0; i < sub.links; i++) {
            if (sub.link[i].from == node || sub.link[i].to == node) {
                degree++;
            }
        }
        return degree;
    }

    public static class CarbonCalculator {
        // ��Ӧ�Ⱥ�����ֵԽС��ʾ̼�ŷ�Խ�ţ�
        public static List<Integer> getLinkIndexesFromPath(DistanceParent[] path,
                                                           EOSubstrateNetwork sub,
                                                           int sNode1, int sNode2) {

            List<Integer> indexlist = new ArrayList<>();
            int currentNode = sNode1;
            while (currentNode != sNode2) {
                int linkNo = GetLinkNum1(sub, currentNode, path[currentNode].parentVert);
                if (linkNo != -1) {
                    indexlist.add(linkNo);
                }
                currentNode = path[currentNode].parentVert;
            }

            return indexlist;

        }

        public static double fitnessFunction(DistanceParent[] path,
                                             EOSubstrateNetwork sub,
                                             double durationHours, int sNode1, int sNode2, List<Integer> Indexes, VONRequest[] reqs, int noEmbedVLink, int index) {

            double loadFactor = calculateGlobalLoadFactor(sub);
            final double W1 = 0.5 + 0.3 * loadFactor;
            final double W5 = 0.5 - 0.3 * loadFactor;
            final double W2 = 1 - W1 - W5;
            double linkCarbon = calculateLinkCarbon(sub, Indexes, durationHours, reqs, noEmbedVLink, index);
            double nodeCarbon = calculateNodeCarbon(sub, getActivatedNodes(path, sNode1, sNode2), durationHours);
            //double loadFactor = calculateLoadFactor(sub, Indexes,reqs,noEmbedVLink,index);
            //double criticality = calculateCriticality(sub, Indexes,reqs,noEmbedVLink,index);
            double compactness =calculateEntropyBasedFragmentation(sub, Indexes);//calculateSpectrumCompactness  calculateEntropyBasedFragmentation
            return W1 * linkCarbon + W5 * nodeCarbon + W2 * compactness;
        }

        private static double calculateGlobalLoadFactor(EOSubstrateNetwork sub) {
            double nodeLoadSum = 0;
            double linkLoadSum = 0;

            // ����ڵ㸺�أ�CPU�����ʣ�
            for (int i = 0; i < sub.nodes; i++) {
                double used = sub.maxcpu[i] - s2v_n[i].rest_cpu;
                nodeLoadSum += used / sub.maxcpu[i];
            }

            // ������·���أ����������ʣ�
            for (int j = 0; j < sub.links; j++) {
                double used = sub.link[j].bw - s2v_l[j].rest_bw;
                linkLoadSum += used / sub.link[j].bw;
            }

            double avgNodeLoad = nodeLoadSum / sub.nodes;
            double avgLinkLoad = linkLoadSum / sub.links;

            // �ۺϸ������ӣ��ɵ���Ȩ�أ�
            return 0.6 * avgLinkLoad + 0.4 * avgNodeLoad;
        }

        // ��·̼�ŷ����֣����Ĳ�����
        private static double calculateLinkCarbon(EOSubstrateNetwork sub,
                                                  List<Integer> linkIndexes,
                                                  double duration,VONRequest[] reqs,int noEmbedVLink,int index) {
            double total = 0;
            for (int linkNo : linkIndexes) {
                double usedBW = (sub.link[linkNo].bw - s2v_l[linkNo].rest_bw)
                        + reqs[index].link[noEmbedVLink].bw;//����Ԥ��ӳ���ж�ʱ  Ӧ������·�Ѿ�ӳ�����ж�����Ӧ�Ⱥ�����С
                double transmission = (0.645 * usedBW * sub.link[linkNo].length) / 80;
                total += transmission * duration / 3.6e6;
            }
            return total;
        }

        private static List<Integer> getActivatedNodes(DistanceParent[] path,int sNode1, int sNode2   ) {
            List<Integer> nodes = new ArrayList<>();
            int currentNode =sNode1;
            while (currentNode != -1 && currentNode != sNode2 && path[currentNode] != null) {
                nodes.add(currentNode);
                currentNode = path[currentNode].parentVert;
            }
            nodes.add(sNode2);
            return nodes;
        }

        // �ڵ�̼�ŷż��㣨�ۼ����м���ڵ㣩
        private static double calculateNodeCarbon(EOSubstrateNetwork sub,
                                                  List<Integer> nodes,
                                                  double duration) {
            double total = 0;
            for (int node : nodes) {
                int degree = getNodeDegree(node, sub);
                double power = (400 + 1329.33 + 120 + 150) + (85 * degree + 80);
                total += sub.node_GHG[node] * power * duration / 3.6e6;
            }
            return total;
        }
        private static double calculateLoadFactor(EOSubstrateNetwork sub,
                                                  List<Integer> linkIndexes,
                                                  VONRequest[] reqs,int noEmbedVLink,int index) {
            double sum = 0;
            for (int linkNo : linkIndexes) {
                // ���赱ǰ��������ѱ�ʹ��
                double used = (sub.link[linkNo].bw - s2v_l[linkNo].rest_bw)
                        + reqs[index].link[noEmbedVLink].bw;
                if(used > sub.link[linkNo].bw) return Double.MAX_VALUE; // ��Ч·��
                sum += used / (double)sub.link[linkNo].bw;
            }
            return sum / linkIndexes.size();
        }

        private static double calculateCriticality(EOSubstrateNetwork sub,
                                                   List<Integer> linkIndexes,
                                                   VONRequest[] reqs,int noEmbedVLink,int index) {
            double sum = 0;
            for (int linkNo : linkIndexes) {
                double remaining = s2v_l[linkNo].rest_bw
                        - reqs[index].link[noEmbedVLink].bw; // �۳���ǰ����
                if(remaining < 0) return Double.MAX_VALUE; //
                int from = sub.link[linkNo].from;
                int to = sub.link[linkNo].to;
                double bwFactor = remaining / (double)sub.link[linkNo].bw;
                sum += (getNodeDegree(from, sub) + getNodeDegree(to, sub)) * bwFactor;
            }
            return sum / (2.0 * (sub.nodes - 1) * linkIndexes.size());
        }
    //�����ص�Ƶ����Ƭ����
        private static double calculateEntropyBasedFragmentation(EOSubstrateNetwork sub,
                                                           List<Integer> linkIndexes) {
            double totalEntropy = 0;
            for (int linkNo : linkIndexes) {
                int[] slots = new int[]{sub.slotsNum}; // �������Ƶ�ײ�״̬����
                int freeSlots = countFreeSlots(slots,linkNo);
                if (freeSlots == 0) return Double.MAX_VALUE;
                double entropy = 0;
                int blockSize = 0;
                for (int j = 0; j < sub.slotsNum; j++) {
                    if(sub.slots[linkNo][j] == 1) {
                        blockSize++;
                    }else if (blockSize > 0) {
                        double p = (double) blockSize / freeSlots;
                        entropy -= p * Math.log(p);
                        blockSize = 0;
                    }
                }
                if (blockSize > 0) {
                    double p = (double) blockSize / freeSlots;
                    entropy -= p * Math.log(p);
                }
                totalEntropy += entropy;
            }
            return totalEntropy / linkIndexes.size();
        }

    private static int countFreeSlots(int[] slots ,int linkNo) {
        int count = 0;
        for (int j = 0; j < sub.slotsNum; j++) {
            if(sub.slots[linkNo][j] == 1) {
                count++;
            }
        }
        return count;
    }
    //����Ƶ�ײ۵ľɷ���
        private static double calculateSpectrumCompactness(EOSubstrateNetwork sub,
                                                           List<Integer> linkIndexes) {
            double totalFrag = 0;
            for (int linkNo : linkIndexes) {
                int[] slots = new int[]{sub.slotsNum}; // �������Ƶ�ײ�״̬����
                int continuousBlocks =countContinuousBlocks(slots,linkNo);
                totalFrag += 1.0 / (continuousBlocks + 1); // ������Խ������Խ��
            }
            return totalFrag / linkIndexes.size();
        }

        // ͳ���������п�����������������
        private static int countContinuousBlocks(int[] slots ,int linkNo) {
            int maxContinuous = 0;
            int current = 0;
            for (int j = 0; j < sub.slotsNum; j++) {
                if(sub.slots[linkNo][j] == 1) {
                    current++;
                    maxContinuous = Math.max(maxContinuous, current);
                }else{
                    current=0;
                }
            }
            return maxContinuous; // ������������鳤��
        }
    }
    public List<DistanceParent[]> evaluate_path_value(List<DistanceParent[]> population,
                                                  EOSubstrateNetwork sub,
                                                  VONRequest[] reqs,
                                                  int index,
                                                  int noEmbedVLink,
                                                  int sNode1,
                                                  int sNode2
                                                 ) {
        // ��ʼ����Ⱥ
        List<DistanceParent[]> currentPop = new ArrayList<>(population);
        if (currentPop.isEmpty()) {
            throw new IllegalArgumentException("��ʼ��Ⱥ����Ϊ��");
        }
        double durationHours = reqs[index].duration / 100.0;
            // ������Ӧ��
            Map<DistanceParent[], Double> fitness = new HashMap<>();

            for (DistanceParent[] path : currentPop) {
                // ԭLambda���ʽ�еļ����߼�
                List<Integer>  linkIndexes = CarbonCalculator.getLinkIndexesFromPath(
                        path, sub, sNode1, sNode2);
                fitness.put(path, CarbonCalculator.fitnessFunction(
                        path, sub, durationHours,sNode1,sNode2,linkIndexes,reqs,noEmbedVLink,index));
            }


        // �滻Collections.min��д��
        return currentPop.stream()
                .sorted(Comparator.comparingDouble(path -> {
                    // ������ʽ������Ӧ��ֵ
                    List<Integer> linkIndexes = CarbonCalculator.getLinkIndexesFromPath(
                            path, sub, sNode1, sNode2);
                    return CarbonCalculator.fitnessFunction(path, sub, durationHours,sNode1,sNode2,linkIndexes,reqs,noEmbedVLink,index);
                }))
                .collect(Collectors.toList());
    }
    private List<Integer> reconstructPath(DistanceParent[] path,int sNode1, int sNode2  ) {
        List<Integer> nodes = new ArrayList<>();
        Integer current =sNode1; // ·���յ�
        while(current != null && current != sNode2) {
            nodes.add(current);
            current = path[current].parentVert;
        }
        nodes.add(sNode2);
        return nodes;
    }
    private List<Integer> normalizePath(List<Integer> path) {
        // ת��Ϊ��׼������㵽�յ㣩
        if(path.get(0) > path.get(path.size()-1)) {
            Collections.reverse(path);
        }
        return new ArrayList<>(path);
    }
    public boolean PreEmbedVLinkBy9166(EOSubstrateNetwork sub, VONRequest reqs[], int index, int noEmbedVLink, int[] vNodeEmbed, int p[][], int ret[][]) {
        //CreateKShortestPath(sub,reqs,index);
        //�ҵ���Ӧ�������˵�
        int sNode1, sNode2;
        sNode1 = vNodeEmbed[reqs[index].link[noEmbedVLink].from];
        sNode2 = vNodeEmbed[reqs[index].link[noEmbedVLink].to];
        //
        DistanceParent[][] kSPath = new DistanceParent[Parameters.K_PATH][sub.nodes];//�洢k���·�������ݽṹ

        int pathRet = -1;
        pathRet = GetKShortestPath(sub, sNode1, sNode2, kSPath);
        List<DistanceParent[]> population = new ArrayList<>();
        //buildValidPopulation( kSPath[],int sNode1, int sNode2)
        for (int i=0; i<pathRet; i++) {
            population.add(kSPath[i]);
        }
        // ��PreEmbedVLinkByGA�������޸���Ӧ�Ⱥ�������
        List<DistanceParent[]> sortedPopulation =evaluate_path_value(
                population,
                sub,
                reqs,
                index,
                noEmbedVLink,
                sNode1,
                sNode2
        );

        int MAX_SAME_LENGTH = 5;
        int sameLengthCount = 0;
        double prevLength = -1;
        List<List<Integer>> uniquePaths = new ArrayList<>();
        for (DistanceParent[] candidate : sortedPopulation) {
            List<Integer> currentPath = reconstructPath(candidate, sNode1, sNode2);
            List<Integer> normalized = normalizePath(currentPath);
            if(uniquePaths.stream().anyMatch(path -> path.equals(normalized))) continue;
            uniquePaths.add(normalized);
            WeightedDirectedGraph myGraph = new WeightedDirectedGraph(sub.nodes);
            myGraph.CreateEdge(sub);

            // ��ȡ·������
            double currentLen = GetPathLength(sub, candidate, sNode1, sNode2);
            if(Math.abs(currentLen - prevLength) < 1e-6) {
                sameLengthCount++;
                if(sameLengthCount > MAX_SAME_LENGTH) {
                    break; // ��ֹ��ͬ����·������ѭ��
                }
            } else {
                sameLengthCount = 0;
                prevLength = currentLen;
            }

            int slotsNeeded = CalculateSlots(reqs[index].link[noEmbedVLink].bw, currentLen);
            // ��������·��
            myGraph.GetPath(p[noEmbedVLink], sNode2, sNode1, candidate);
            int findSlotIndex = -1;
            // ���Ƶ�ײۿ�����
            findSlotIndex = CheckIfEnoughSlotsOnPath(sub, candidate, 0, slotsNeeded, sNode1, sNode2);
            if (findSlotIndex != -1) {
                ret[noEmbedVLink][0] = findSlotIndex;
                ret[noEmbedVLink][1] = slotsNeeded;
                return true;
            }
        }
        return false;

        //DistanceParent[][] path = myGraph.kShortestPath;//sNode2----->path[sNode2].parentVert

      /*  int i = 0;
        //int pathRet = myPath.size();//·��������
        //for (i = 0; i < pathRet; i++) {//ÿ��·��i
            //��¼·��ӳ��������������·noEmbedVLinkӳ�䵽·��p[noEmbedVLink]
            WeightedDirectedGraph myGraph = new WeightedDirectedGraph(sub.nodes);
            //myGraph.CreateDireGraph(sub.nodes);//�����ڵ�
            myGraph.CreateEdge(sub);//������
            System.out.println("GetPath:" + sNode1 + "-" + sNode2);
            myGraph.GetPath(p[noEmbedVLink], sNode2, sNode1, kSPath[i]);//����p
            PrintPath(p[noEmbedVLink], sNode2, sNode1);
            pathLength[i] = GetPathLength(sub, kSPath[i], sNode1, sNode2);//�����·�����ȣ�Ϊ����ļ���Ƶ�ײ���׼��
            System.out.println("path[" + i + "].length:" + pathLength[i]);
            slotInPath[i] = CalculateSlots(reqs[index].link[noEmbedVLink].bw, pathLength[i]);//����Ƶ�ײ�����
            if (slotInPath[i] <= 0) return -1;
            //����Ƿ�����������Ҫ��
            System.out.println("path[" + i + "].length:" + pathLength[i] + " slotsNum:" + slotInPath[i]);
            myGraph.PrintPath(sNode2, sNode1, kSPath[i]);

            findSlotIndex = CheckIfEnoughSlotsOnPath(sub, kSPath[i], 0, slotInPath[i], sNode1, sNode2);//Ѱ������Ҫ���Ƶ�ײ�����
            if (findSlotIndex == -1) {
               return -1;//��ǰ·����Ƶ�ײ�����0��ʼ��û���ҵ���Ӧ������Ƶ�ײ�
            }
            ret[noEmbedVLink][0] = findSlotIndex;
            ret[noEmbedVLink][1] = slotInPath[i];
            return;//�ҵ���·��i��Ƶ�ײ�����findSlotIndex*/
        //if (i == pathRet) return false;//��K��·����û���ҵ����ʵ�����Ƶ�ײ�
        //UpdateSub(subCopy,sNode2,sNode1,ret[i],p[i]);
        //else
        //  return true;//��K��·�����ҵ����ʵ�����Ƶ�ײۣ�·��������p[noEmbedVLink],ret[noEmbedVLink][0] = findSlotIndex;ret[noEmbedVLink][1] = slotInPath[i];
    }
    //�Ը���ͼԤӳ��������·
    public boolean PreEmbedVLinkByAuxiGraph(EOSubstrateNetwork sub, VONRequest reqs[], int index, int noEmbedVLink, int[] vNodeEmbed, int p[][], int ret[][]) {
        //CreateKShortestPath(sub,reqs,index);
        //�ҵ���Ӧ�������˵�
        int sNode1, sNode2;
        sNode1 = vNodeEmbed[reqs[index].link[noEmbedVLink].from];
        sNode2 = vNodeEmbed[reqs[index].link[noEmbedVLink].to];
        //
        DistanceParent[][] kSPath = new DistanceParent[Parameters.K_PATH][sub.nodes];//�洢k���·�������ݽṹ

        int pathRet = -1;
        pathRet = GetKShortestPath(sub, sNode1, sNode2, kSPath);


        //DistanceParent[][] path = myGraph.kShortestPath;//sNode2----->path[sNode2].parentVert
        double[] pathLength = new double[Parameters.K_PATH];
        int[] slotInPath = new int[Parameters.K_PATH];
        int findSlotIndex = -1;
        int i = 0;
        //int pathRet = myPath.size();//·��������
        for (i = 0; i < pathRet; i++) {//ÿ��·��i
            //��¼·��ӳ��������������·noEmbedVLinkӳ�䵽·��p[noEmbedVLink]
            WeightedDirectedGraph myGraph = new WeightedDirectedGraph(sub.nodes);
            //myGraph.CreateDireGraph(sub.nodes);//�����ڵ�
            myGraph.CreateEdge(sub);//������
            System.out.println("GetPath:" + sNode1 + "-" + sNode2);
            myGraph.GetPath(p[noEmbedVLink], sNode2, sNode1, kSPath[i]);//����p
            PrintPath(p[noEmbedVLink], sNode2, sNode1);
            pathLength[i] = GetPathLength(sub, kSPath[i], sNode1, sNode2);//�����·�����ȣ�Ϊ����ļ���Ƶ�ײ���׼��
            System.out.println("path[" + i + "].length:" + pathLength[i]);
            slotInPath[i] = CalculateSlots(reqs[index].link[noEmbedVLink].bw, pathLength[i]);//����Ƶ�ײ�����
            if (slotInPath[i] <= 0) continue;
            //����Ƿ�����������Ҫ��
            System.out.println("path[" + i + "].length:" + pathLength[i] + " slotsNum:" + slotInPath[i]);
            myGraph.PrintPath(sNode2, sNode1, kSPath[i]);

            findSlotIndex = CheckIfEnoughSlotsOnPath(sub, kSPath[i], 0, slotInPath[i], sNode1, sNode2);//Ѱ������Ҫ���Ƶ�ײ�����
            if (findSlotIndex == -1) {
                continue;//��ǰ·����Ƶ�ײ�����0��ʼ��û���ҵ���Ӧ������Ƶ�ײ�
            }
            ret[noEmbedVLink][0] = findSlotIndex;
            ret[noEmbedVLink][1] = slotInPath[i];
            break;//�ҵ���·��i��Ƶ�ײ�����findSlotIndex
        }
        if (i == pathRet) return false;//��K��·����û���ҵ����ʵ�����Ƶ�ײ�
            //UpdateSub(subCopy,sNode2,sNode1,ret[i],p[i]);
        else
            return true;//��K��·�����ҵ����ʵ�����Ƶ�ײۣ�·��������p[noEmbedVLink],ret[noEmbedVLink][0] = findSlotIndex;ret[noEmbedVLink][1] = slotInPath[i];
    }


    //Save the results of the virtual node embedding.
    public void AddNodesMap(VONRequest reqs[], int index, ArrayList<Object> list) {
        for (int i = 0; i < reqs[index].nodes; i++) {
            int snode = (int) list.get(i);
            int reqCount = s2v_n[snode].req_count;

            s2v_n[snode].rest_cpu -= reqs[index].cpu[i];
            s2v_n[snode].req.add(reqCount, index);//reqid;
            s2v_n[snode].vnode.add(reqCount, i); //nodeid;
            s2v_n[snode].cpu.add(reqCount, reqs[index].cpu[i]);// = req[reqid].cpu[nodeid];
            s2v_n[snode].req_count++;

            v2s[index].snode.add(i, snode);
        }
    }

    /*Save the results of the virtual node embedding.
     *
     */
    public void AddNodesMap(VONRequest reqs[], int index, int nodeEmbed[]) {
        for (int i = 0; i < reqs[index].nodes; i++) {
            int snode = nodeEmbed[i];//(int)list.get(i);
            if(Parameters.DebugModel) {
                System.out.println(index + "-" + reqs[index].nodes + ":" + i + ":" + snode);
            }
            int reqCount = s2v_n[snode].req_count;

            s2v_n[snode].rest_cpu -= reqs[index].cpu[i];
            s2v_n[snode].req.add(reqCount, index);//reqid;
            s2v_n[snode].vnode.add(reqCount, i); //nodeid;
            s2v_n[snode].cpu.add(reqCount, reqs[index].cpu[i]);// = req[reqid].cpu[nodeid];
            s2v_n[snode].req_count++;

            v2s[index].snode.add(i, snode);
        }
    }

    /*Save the results of the virtual node embedding.
     *
     */
    public void AddNodesMapSub(EOSubstrateNetwork sub, VONRequest reqs[], int index, int nodeEmbed[]) {
        for (int i = 0; i < reqs[index].nodes; i++) {
            int snode = nodeEmbed[i];//(int)list.get(i);

            sub.cpu[snode] -= reqs[index].cpu[i];
        }
    }


    //save the result of embedding virtual nodes to the text document.
    public void SaveNodeEmbedding(VONRequest reqs[], int index) {
        Tools myDowith = new Tools();
        String data = "";
        //for(int index=0;index<reqs.length;index++){
        for (int i = 0; i < reqs[index].nodes; i++) {
            int snode = v2s[index].snode.get(i);
            data += index + " " + i + " " + snode + "\r\n";
        }
        //}
        myDowith.SaveFile("nodeEmbed.dat", data, false);
    }

    public void SaveNodeEmbedding(VONRequest reqs[]) {
        //int index
        Tools myDowith = new Tools();
        String data = "";
        for (int index = 0; index < reqs.length; index++) {
            for (int i = 0; i < reqs[index].nodes; i++) {
                int snode = v2s[index].snode.get(i);
                data += index + " " + i + " " + snode + "\r\n";
            }
        }
        myDowith.SaveFile("nodeEmbed.dat", data, false);
    }

    /*��¼��ѡ����*/
    public void RecordFeasiResolve(Hashtable slotAllocHash, int cut, int pathIndex, int slotNo, int slotNum) {
        //��¼
        String keyNode1;
        keyNode1 = String.valueOf(pathIndex) + "," + String.valueOf(slotNo) + "," + slotNum;
        slotAllocHash.put(keyNode1, cut);//�Ᵽ����hash����
    }

    /*�õ�cut:�㷨�Ǹ��ݸ�slotNo������slot�Ƿ���У�����������1*/
    public int GetCut(EOSubstrateNetwork sub, int p[], int slotNo, int sNode1, int sNode2) {
        //ÿ���ߵ�����slot�Ƿ���У�������Ϊ1������Ϊ0��
        int cut = 0, findCut = 0;
        ;
        int linkNum = -1;
        int tmpNode = sNode2;
        while (tmpNode != sNode1) {
            cut = 0;
            linkNum = GetLinkNum(sub, p[tmpNode], tmpNode);//�õ�linkNum
            if (sub.slots[linkNum][slotNo] != 1) {
                //����
                System.out.println("GetCut(sub.slots[" + linkNum + "," + slotNo + "]) is error.****************");
                if (Parameters.ErrorRecord) {
                    String str = "GetCut():" + "GetCut(sub.slots[" + linkNum + "," + slotNo + "]) is error." + "\r\n";
                    WriteFilePlus("error.txt", str);
                }
                break;
            }
            if (slotNo == 0 && sub.slots[linkNum][slotNo + 1] == 1) cut++;//�ڶ���slot
            else if (slotNo == sub.slotsNum - 1 && sub.slots[linkNum][slotNo - 1] == 1) cut++;//���һ��slot
            else if (slotNo < sub.slotsNum - 1) {
                if (sub.slots[linkNum][slotNo - 1] == 1) cut++;//slotNo��һ��slot
                if (sub.slots[linkNum][slotNo + 1] == 1) cut++;//slotNo��һ��slot
            }
            if (cut > 1) {
                findCut = 2;
                break;
            }
            if (cut > findCut) findCut = cut;
            tmpNode = p[tmpNode];
        }
        return findCut;
    }

    /*
     * ���ýڵ����߱�ʶ
     */
    void SetHiberNodes(EOSubstrateNetwork subCopy, VONRequest reqs[], int index, int vLinkNo, int nodeNum, int[] nodeH, int[] nDegree) {
        //��ѯnodeH���߱�ʶ
        int nhSum = 0;
        for (int i = 0; i < sub.nodes; i++) {
            //if(nodeH[i] == 1 && reqs[index].cpu[vLinkNo] <= sub.cpu[i]) nhSum++;
            if (nodeH[i] == 1) nhSum++;
        }
        if (nodeNum <= nhSum) return;
        int degree = 0, maxDeNode = -1;
        while (nodeNum > nhSum) {
            maxDeNode = -1;
            degree = 0;
            //�������Ѿ�ӳ��Ľڵ����ӵĶ����Ľڵ�
            for (int j = 0; j < sub.links; j++) {
                if (nodeH[sub.link[j].from] == 1 && nodeH[sub.link[j].to] == -1) {
                    if (degree < nDegree[sub.link[j].to]) {
                        //if(reqs[index].cpu[vLinkNo] <= sub.cpu[sub.link[j].to]){
                        maxDeNode = sub.link[j].to;
                        degree = nDegree[sub.link[j].to];
                        //}
                    }
                } else if (nodeH[sub.link[j].to] == 1 && nodeH[sub.link[j].from] == -1) {
                    if (degree < nDegree[sub.link[j].from]) {
                        //if(reqs[index].cpu[vLinkNo] <= sub.cpu[sub.link[j].from]){
                        maxDeNode = sub.link[j].from;
                        degree = nDegree[sub.link[j].from];
                        //}
                    }
                }
            }
            if (maxDeNode == -1) {//˵�����нڵ㶼û���������߱�ʶ
                for (int j = 0; j < sub.nodes; j++) {
                    if (degree < nDegree[j]) {
                        //if(reqs[index].cpu[vLinkNo] <= sub.cpu[j]){
                        //if(reqs[index].cpu[vLinkNo] <= sub.cpu[j]){
                        maxDeNode = j;
                        degree = nDegree[j];
                        //}
                    }
                }
            }
            //�������߱�ʶ
            if (maxDeNode > -1) nodeH[maxDeNode] = 1;
            nhSum++;
        }
    }

    /*ͨ��(sNode1,sNode2)�õ���·��*/
    public int GetLinkNum(AuxiliaryGraph sub, int sNode1, int sNode2) {
        int linkNum = -1;
        for (int i = 0; i < sub.links; i++) {
            if ((sub.link[i].from == sNode1 && sub.link[i].to == sNode2) || (sub.link[i].from == sNode2 && sub.link[i].to == sNode1)) {
                linkNum = i;
                break;
            }
        }
        return linkNum;
    }

    /*��kShortestPath�õ�·��p*/
    public void GetPath(int p[], int sNode1, int sNode2, DistanceParent[] shortestPath) {
        p[sNode1] = -1;
        p[sNode2] = shortestPath[sNode2].parentVert;

        while (shortestPath[sNode2].parentVert != sNode1) {
            sNode2 = shortestPath[sNode2].parentVert;
            p[sNode2] = shortestPath[sNode2].parentVert;
        }
    }

    /*����K���·���ı�*/
    public void CreateEdge(EOSubstrateNetwork sub, int sNode1, int sNode2, WeightedDirectedGraph myGraph) {
        //���ӱ�
        for (int i = 0; i < sub.links; i++) {
            if (sub.link[i].from == sNode1 && sub.link[i].to == sNode2)
                myGraph.addEdge(sub.link[i].from, sub.link[i].to, 1);
            else if (sub.link[i].to == sNode2)
                myGraph.addEdge(sub.link[i].from, sub.link[i].to, 1);
            else if (sub.link[i].from == sNode1)
                myGraph.addEdge(sub.link[i].from, sub.link[i].to, 1);
            else {
                myGraph.addEdge(sub.link[i].from, sub.link[i].to, 1);
                myGraph.addEdge(sub.link[i].to, sub.link[i].from, 1);
            }
        }
    }

    /*Check if the links along the path have enough free slots.
     *>1:successfully;
     *-1:failed.*/
    public int CheckAllPathFreeSlots(EOSubstrateNetwork sub, int linkNum, int slotNo, int slotNum) {
        if (slotNum > sub.slotsNum) {
            System.out.println("CheckAllPathFreeSlots is error, slotNum.************************");
            if (Parameters.ErrorRecord) {
                String str = "CheckAllPathFreeSlots():" + "CheckAllPathFreeSlots is error, slotNum." + "\r\n";
                WriteFilePlus("error.txt", str);
            }
            return -1;
        }
        if (linkNum > sub.links - 1) {
            System.out.println("CheckAllPathFreeSlots is error, linkNum.************************");
            if (Parameters.ErrorRecord) {
                String str = "CheckAllPathFreeSlots():" + "CheckAllPathFreeSlots is error, slotNum." + "\r\n";
                WriteFilePlus("error.txt", str);
            }
            return -1;
        }
        for (int i = slotNo; i < slotNo + slotNum; i++) {
            if (i > sub.slotsNum - 1) return -1;
            if (sub.slots[linkNum][i] != 1) return -1;
        }
        return 1;
    }

    /*
     * ����:����p
     * ����:retSlotSE[]:��ʼ����
     *     retSlotEE[]:��������
     *     retLinkE[]:��·ӳ����
     *     kShortestPath[][][]:���·������
     *     pathEff[]:ÿ������·��ӳ�����Ч·������
     *     pathNo[][]:·���ı��
     *     virtualNodes[]:����ڵ�ӳ�����չͼ�ڵ���
     *     retNodeE[]:�ڵ�ӳ��
     * ������:������
     * ����ʱ��:2017-09-28*/
    public void CreateShortestPathFromKPaths(VONRequest reqs[], int index, DistanceParent kShortestPath[][][], int virtualNodes[], int retLinkE[], int pathEff[], int p[][]) {
        for (int i = 0; i < reqs[index].links; i++) {
            int sNode1 = virtualNodes[reqs[index].link[i].from];//������·�˵�from��Ӧ�ĸ���ͼ�ڵ���
            int sNode2 = virtualNodes[reqs[index].link[i].to];//������·�˵�to��Ӧ�ĸ���ͼ�ڵ���
            int sLinkNo = retLinkE[i];//��·ӳ���·�����
            int pathNo1 = 0;
            boolean find = false;
            int vLinkNo = 0;
            int pathByLink = 0;
            for (vLinkNo = 0; vLinkNo < reqs[index].links; vLinkNo++) {
                find = false;
                for (pathByLink = 0; pathByLink < pathEff[vLinkNo]; pathByLink++) {
                    if (pathNo1 == sLinkNo) {//�ҵ���·��kShortestPath[k][j]
                        //����·��kShortestPath[k][j]������p
                        find = true;
                        break;
                    }
                    pathNo1++;
                }
                if (find) break;
            }
            //��ʼ��p[][]
            int sNode3 = sNode2;
            while (kShortestPath[vLinkNo][pathByLink][sNode3].parentVert != sNode1) {
                if (sNode3 != sNode2) {//���������˵�
                    p[i][sNode3] = kShortestPath[vLinkNo][pathByLink][sNode3].parentVert;
                }
                sNode3 = kShortestPath[vLinkNo][pathByLink][sNode3].parentVert;
            }
            //p[i][sNode3] = kShortestPath[vLinkNo][pathByLink][sNode3].parentVert;
        }
    }

    /*����Sub���������
     * ����sub;2������S2VLink s2v_l[] = new S2VLink[reqs[index].links];
     * 3������v2s
     */
    public void AddLinksMapBySPFA(EOSubstrateNetwork sub, VONRequest reqs[], int index, int ret[][], int p[][]) {
        //1���ڵ�ӳ�䣬����sub.slots;
        for (int i = 0; i < reqs[index].links; i++) {
            int snode1, snode2, vnode1, vnode2;
            vnode1 = reqs[index].link[i].from;
            vnode2 = reqs[index].link[i].to;
            snode1 = v2s[index].snode.get(vnode1);
            snode2 = v2s[index].snode.get(vnode2);
            UpdateSub(sub, snode2, snode1, ret[i], p[i]);
        }

        //2������S2VLink s2v_l[]
        int snodeMid1, snodeMid, sNode1, req_count;
        for (int i = 0; i < reqs[index].links; i++) {
            snodeMid1 = reqs[index].link[i].to;
            sNode1 = reqs[index].link[i].from;
            snodeMid1 = v2s[index].snode.get(snodeMid1);

            if (p[i][snodeMid1] == -1) {
                snodeMid1 = reqs[index].link[i].from;
                sNode1 = reqs[index].link[i].to;
                snodeMid1 = v2s[index].snode.get(snodeMid1);
                if (p[i][snodeMid1] == -1) {
                    System.out.println("error!************* in AddLinksMapBySPFA1" + " reqs[" + index + "]");
                    if (Parameters.ErrorRecord) {
                        String str = "reqs[" + index + "] AddLinksMapBySPFA():" + "error1." + "\r\n";
                        WriteFilePlus("error.txt", str);
                    }
                }
            }

            sNode1 = v2s[index].snode.get(sNode1);
            while (p[i][snodeMid1] != -1) {
                snodeMid = p[i][snodeMid1];//
                req_count = s2v_l[sub.linksNo[snodeMid][snodeMid1]].req_count;
                if(Parameters.DebugModel) {
                    System.out.println("linkNo:" + sub.linksNo[snodeMid][snodeMid1] + " " + req_count + " " + index);
                }
                s2v_l[sub.linksNo[snodeMid][snodeMid1]].req.add(req_count, index);
                s2v_l[sub.linksNo[snodeMid][snodeMid1]].bw.add(req_count, reqs[index].link[i].bw);
                s2v_l[sub.linksNo[snodeMid][snodeMid1]].vlink.add(req_count, i);
                s2v_l[sub.linksNo[snodeMid][snodeMid1]].rest_bw -= reqs[index].link[i].bw;
                s2v_l[sub.linksNo[snodeMid][snodeMid1]].req_count++;

                snodeMid1 = snodeMid;
                if (snodeMid1 == sNode1) break;//�ӻ�㵽Դ���·���Ѿ�����
            }
        }

        //3������v2s[]����·ӳ����Ϣ
        int pathLength = 0;

        for (int i = 0; i < reqs[index].links; i++) {
            snodeMid1 = reqs[index].link[i].to;
            sNode1 = reqs[index].link[i].from;
            snodeMid1 = v2s[index].snode.get(snodeMid1);

            if (p[i][snodeMid1] == -1) {
                snodeMid1 = reqs[index].link[i].from;
                sNode1 = reqs[index].link[i].to;
                snodeMid1 = v2s[index].snode.get(snodeMid1);
                if (p[i][snodeMid1] == -1) {
                    System.out.println("error!************* in AddLinksMapBySPFA2");
                    if (Parameters.ErrorRecord) {
                        String str = "reqs[" + index + "] AddLinksMapBySPFA():" + "error2." + "\r\n";
                        WriteFilePlus("error.txt", str);
                    }
                }
            }

            sNode1 = v2s[index].snode.get(sNode1);

            if (Parameters.DebugModel) System.out.println("snodeMid1:" + snodeMid1);
            //sNode1 = v2s[index].snode.get(sNode1);

            pathLength = 0;
            LinkedList<Integer> link = new LinkedList<Integer>();
            while (p[i][snodeMid1] != -1) {
                snodeMid = p[i][snodeMid1];
                link.add(pathLength, snodeMid1);
                pathLength++;    //·������

                snodeMid1 = snodeMid;
                //if(snodeMid1 == sNode1) break;//�ӻ�㵽Դ���·���Ѿ�����
            }
            link.add(pathLength, snodeMid1);

            SpathFlow pathFlow = new SpathFlow();
            pathFlow.link = link;
            pathFlow.len = pathLength;
            if (Parameters.DebugModel) System.out.println("vlink:" + i + " pathLength:" + pathLength);

            if (p[i][reqs[index].link[i].to] != -1) {
                snodeMid1 = reqs[index].link[i].to;
            } else {
                snodeMid1 = reqs[index].link[i].from;
            }

            snodeMid1 = reqs[index].link[i].to;
            snodeMid1 = v2s[index].snode.get(snodeMid1);

            if (p[i][snodeMid1] == -1) {
                snodeMid1 = reqs[index].link[i].from;
                snodeMid1 = v2s[index].snode.get(snodeMid1);
                if (p[i][snodeMid1] == -1) {
                    System.out.println("error!************* in AddLinksMapBySPFA3");
                    if (Parameters.ErrorRecord) {
                        String str = "reqs[" + index + "] AddLinksMapBySPFA():" + "error3." + "\r\n";
                        WriteFilePlus("error.txt", str);
                    }
                }
            }

            //snodeMid1 = v2s[index].snode.get(snodeMid1);
            for (int ii = 0; ii < pathLength; ii++) {
                snodeMid = p[i][snodeMid1];
                //System.out.print(snodeMid1+"-");
                snodeMid1 = snodeMid;
            }
            //System.out.print(snodeMid1);
            //System.out.println("");

            pathFlow.bw = reqs[index].link[i].bw;
            v2s[index].pathFlow.add(i, pathFlow);
            v2s[index].flowLen.add(i, 1);//1����·����·ӳ�䣻i����i����·��
            v2s[index].startSlotNo.add(i, ret[i][0]);//�������ʼ����
            if(Parameters.DebugModel) {
                System.out.println("v2s[].slotNum:" + (ret[i][1] - ret[i][0] + 1));
            }
            v2s[index].slotNum.add(i, ret[i][1] - ret[i][0] + 1);    //�����ɣ���Ϊret[i][1]�ǽ�ֹƵ�ײ����������������������Ƶ������v2s[i].slotNum.get(j)
        }
        //����ӳ��ɹ���־
        v2s[index].map = Parameters.STATE_MAP_LINK;
        reqs[index].map = Parameters.STATE_MAP_LINK;
    }

    /*����Sub���������
     * ����sub;2������S2VLink s2v_l[] = new S2VLink[reqs[index].links];
     * 3������v2s
     */
    public void AddLinksMapBySPFANoSlots(EOSubstrateNetwork sub, VONRequest reqs[], int index, int ret[][], int p[][]) {
  		/*
  		//1���ڵ�ӳ�䣬����sub.slots;
  		for(int i=0;i<reqs[index].links;i++){
  			int snode1,snode2,vnode1,vnode2;
  			vnode1 = reqs[index].link[i].from;
  			vnode2 = reqs[index].link[i].to;
  			snode1 = v2s[index].snode.get(vnode1);
  			snode2 = v2s[index].snode.get(vnode2);
  			UpdateSub(sub,snode2,snode1,ret[i],p[i]);
  		}
  		*/

        //2������S2VLink s2v_l[]
        int snodeMid1, snodeMid, sNode1, req_count;
        for (int i = 0; i < reqs[index].links; i++) {
            snodeMid1 = reqs[index].link[i].to;
            sNode1 = reqs[index].link[i].from;
            snodeMid1 = v2s[index].snode.get(snodeMid1);

            if (p[i][snodeMid1] == -1) {
                snodeMid1 = reqs[index].link[i].from;
                sNode1 = reqs[index].link[i].to;
                snodeMid1 = v2s[index].snode.get(snodeMid1);
                if (p[i][snodeMid1] == -1) {
                    System.out.println("error!************* in AddLinksMapBySPFA");
                    if (Parameters.ErrorRecord) {
                        String str = "reqs[" + index + "] AddLinksMapBySPFA():" + "error1." + "\r\n";
                        WriteFilePlus("error.txt", str);
                    }
                }
            }

            sNode1 = v2s[index].snode.get(sNode1);
            while (p[i][snodeMid1] != -1) {
                snodeMid = p[i][snodeMid1];//
                req_count = s2v_l[sub.linksNo[snodeMid][snodeMid1]].req_count;
                System.out.println("linkNo:" + sub.linksNo[snodeMid][snodeMid1] + " " + req_count + " " + index);
                s2v_l[sub.linksNo[snodeMid][snodeMid1]].req.add(req_count, index);
                s2v_l[sub.linksNo[snodeMid][snodeMid1]].bw.add(req_count, reqs[index].link[i].bw);
                s2v_l[sub.linksNo[snodeMid][snodeMid1]].vlink.add(req_count, i);
                s2v_l[sub.linksNo[snodeMid][snodeMid1]].rest_bw -= reqs[index].link[i].bw;
                s2v_l[sub.linksNo[snodeMid][snodeMid1]].req_count++;

                snodeMid1 = snodeMid;
                if (snodeMid1 == sNode1) break;//�ӻ�㵽Դ���·���Ѿ�����
            }
        }

        //3������v2s[]����·ӳ����Ϣ
        int pathLength = 0;

        for (int i = 0; i < reqs[index].links; i++) {
            snodeMid1 = reqs[index].link[i].to;
            sNode1 = reqs[index].link[i].from;
            snodeMid1 = v2s[index].snode.get(snodeMid1);

            if (p[i][snodeMid1] == -1) {
                snodeMid1 = reqs[index].link[i].from;
                sNode1 = reqs[index].link[i].to;
                snodeMid1 = v2s[index].snode.get(snodeMid1);
                if (p[i][snodeMid1] == -1) {
                    System.out.println("error!*************");
                    if (Parameters.ErrorRecord) {
                        String str = "reqs[" + index + "] AddLinksMapBySPFA():" + "error2." + "\r\n";
                        WriteFilePlus("error.txt", str);
                    }
                }
            }

            sNode1 = v2s[index].snode.get(sNode1);

            if (Parameters.DebugModel) System.out.println("snodeMid1:" + snodeMid1);
            //sNode1 = v2s[index].snode.get(sNode1);

            pathLength = 0;
            LinkedList<Integer> link = new LinkedList<Integer>();
            while (p[i][snodeMid1] != -1) {
                snodeMid = p[i][snodeMid1];
                link.add(pathLength, snodeMid1);
                pathLength++;    //·������

                snodeMid1 = snodeMid;
                //if(snodeMid1 == sNode1) break;//�ӻ�㵽Դ���·���Ѿ�����
            }
            link.add(pathLength, snodeMid1);

            SpathFlow pathFlow = new SpathFlow();
            pathFlow.link = link;
            pathFlow.len = pathLength;
            if (Parameters.DebugModel) System.out.println("vlink:" + i + " pathLength:" + pathLength);

            if (p[i][reqs[index].link[i].to] != -1) {
                snodeMid1 = reqs[index].link[i].to;
            } else {
                snodeMid1 = reqs[index].link[i].from;
            }

            snodeMid1 = reqs[index].link[i].to;
            snodeMid1 = v2s[index].snode.get(snodeMid1);

            if (p[i][snodeMid1] == -1) {
                snodeMid1 = reqs[index].link[i].from;
                snodeMid1 = v2s[index].snode.get(snodeMid1);
                if (p[i][snodeMid1] == -1) {
                    System.out.println("error!*************");
                    if (Parameters.ErrorRecord) {
                        String str = "reqs[" + index + "] AddLinksMapBySPFA():" + "error3." + "\r\n";
                        WriteFilePlus("error.txt", str);
                    }
                }
            }

            //snodeMid1 = v2s[index].snode.get(snodeMid1);
            for (int ii = 0; ii < pathLength; ii++) {
                snodeMid = p[i][snodeMid1];
                //System.out.print(snodeMid1+"-");
                snodeMid1 = snodeMid;
            }
            //System.out.print(snodeMid1);
            //System.out.println("");

            pathFlow.bw = reqs[index].link[i].bw;
            v2s[index].pathFlow.add(i, pathFlow);
            v2s[index].flowLen.add(i, 1);//1����·����·ӳ�䣻i����i����·��
            v2s[index].startSlotNo.add(i, ret[i][0]);//�������ʼ����
            v2s[index].slotNum.add(i, ret[i][1] - ret[i][0] + 1);    //�����ɣ���Ϊret[i][1]�ǽ�ֹƵ�ײ����������������������Ƶ������v2s[i].slotNum.get(j)
        }
        //����ӳ��ɹ���־
        v2s[index].map = Parameters.STATE_MAP_LINK;
        reqs[index].map = Parameters.STATE_MAP_LINK;
    }

    /* ���ƣ�UpdateSub()
     * ���ܣ�����Sub���������
     * ������sub���ײ�����磻sNode1->sNode2��·���������˵㣻ret[0]-ret[1]����ʼ�ͽ�ֹ��Ƶ�ײ�������p[]��·��
     */
    public void UpdateSub(EOSubstrateNetwork sub, int sNode1, int sNode2, int ret[], int p[]) {

        if (Parameters.DebugModel)
            System.out.println("UpdateSub " + sNode1 + "-" + sNode2 + ":" + ret[0] + " " + ret[1]);

        int snodeMid1, snodeMid;
        //snodeMid1 = sNode2;
        snodeMid1 = sNode1;
        while (p[snodeMid1] != -1) {
            if (Parameters.DebugModel) System.out.println(snodeMid1 + "-" + p[snodeMid1]);
            snodeMid = p[snodeMid1];
            int kk = sub.linksNo[snodeMid][snodeMid1];

            if (Parameters.RecordLogModel) {
                int pa = ret[1];
                String str = "update sub.slots[" + kk + "][" + ret[0] + "-" + pa + "]=0\r\n";
                WriteFilePlus("process.txt", str);
            }
            if (ret[0] <= ret[1]) {
                for (int i = ret[0]; i <= ret[1]; i++) {
                    if (i < 0) continue;
                    if (i >= Parameters.MaxSlots) break;
                    //System.out.println("linksNo["+snodeMid+","+snodeMid1+"]="+kk+" i:"+i);
                    sub.slots[kk][i] = 0;
                }
            } else {
                System.out.println("ret[0]>ret[1] because ret[0] mush little equal ret[1]" + "Error***********");
                return;
  				/*
  				for(int i=ret[1];i<ret[0];i++){
  	  				if(i<0) continue;
  	  				if(i >= Parameters.MaxSlots) break;
  	  				//System.out.println("linksNo["+snodeMid+","+snodeMid1+"]="+kk+" i:"+i);
  	  				sub.slots[kk][i] = 0;
  	  			}*/
            }
            snodeMid1 = snodeMid;
            if (snodeMid1 == sNode1) break;//�ӻ�㵽Դ���·���Ѿ�����
        }
        snodeMid1 = sNode2;
        while (p[snodeMid1] != -1) {
            if (Parameters.DebugModel) System.out.println(snodeMid1 + "-" + p[snodeMid1]);
            snodeMid = p[snodeMid1];
            //for(int i=ret[0];i<ret[0]+ret[1];i++){
            int kk = sub.linksNo[snodeMid][snodeMid1];
            if (Parameters.RecordLogModel) {
                int pa = ret[1];
                String str = "update sub.slots[" + kk + "][" + ret[0] + "-" + pa + "]=0\r\n";
                WriteFilePlus("process.txt", str);
            }
            if (ret[0] <= ret[1]) {
                for (int i = ret[0]; i <= ret[1]; i++) {
                    if (i < 0) continue;
                    if (i >= Parameters.MaxSlots) break;

                    if (Parameters.DebugModel)
                        System.out.println("linksNo[" + snodeMid + "," + snodeMid1 + "]=" + kk + " i:" + i);
                    sub.slots[kk][i] = 0;
                }
            } else {
                System.out.println("ret[0]>ret[1] because ret[0] mush little equal ret[1]" + "Error***********");
                return;
  				/*
  				for(int i=ret[1];i<ret[0];i++){
  	  				if(i<0) continue;
  	  				if(i >= Parameters.MaxSlots) break;

  	  				if(Parameters.DebugModel) System.out.println("linksNo["+snodeMid+","+snodeMid1+"]="+kk+" i:"+i);
  	  				sub.slots[kk][i] = 0;
  	  			}
  	  			*/
            }
            snodeMid1 = snodeMid;
            if (snodeMid1 == sNode1) break;//�ӻ�㵽Դ���·���Ѿ�����
        }
        if (Parameters.DebugModel) System.out.println("UpdateSub is done.");

    }

    /* ���ƣ�UpdateSub()
     * ���ܣ�����Sub���������
     * ������sub���ײ�����磻sNode1->sNode2��·���������˵㣻ret[0]-ret[1]����ʼ�ͽ�ֹ��Ƶ�ײ�������p[]��·��
     */
    public boolean CheckSubSlots(EOSubstrateNetwork sub, int sNode1, int sNode2, int ret[], int p[]) {

        if (Parameters.DebugModel)
            System.out.println("CheckSubSlots " + sNode1 + "-" + sNode2 + ":" + ret[0] + " " + ret[1]);

        int snodeMid1, snodeMid;
        //snodeMid1 = sNode2;
        snodeMid1 = sNode1;
        while (p[snodeMid1] != -1) {
            if (Parameters.DebugModel) System.out.println(snodeMid1 + "-" + p[snodeMid1]);
            snodeMid = p[snodeMid1];
            int kk = sub.linksNo[snodeMid][snodeMid1];

            if (Parameters.RecordLogModel) {
                int pa = ret[1];
                String str = "Check sub.slots[" + kk + "][" + ret[0] + "-" + pa + "]=0\r\n";
                WriteFilePlus("process.txt", str);
            }
            if (ret[0] <= ret[1]) {
                for (int i = ret[0]; i <= ret[1]; i++) {
                    if (i < 0) continue;
                    if (i >= Parameters.MaxSlots) break;
                    //System.out.println("linksNo["+snodeMid+","+snodeMid1+"]="+kk+" i:"+i);
                    if (sub.slots[kk][i] == 0) {
                        return false;
                    }
                    sub.slots[kk][i] = 0;
                }
            } else {
                System.out.println("ret[0]>ret[1] because ret[0] mush little equal ret[1]" + "Error***********");
                return false;
            }
            snodeMid1 = snodeMid;
            if (snodeMid1 == sNode1) break;//�ӻ�㵽Դ���·���Ѿ�����
        }
        snodeMid1 = sNode2;
        while (p[snodeMid1] != -1) {
            if (Parameters.DebugModel) System.out.println(snodeMid1 + "-" + p[snodeMid1]);
            snodeMid = p[snodeMid1];
            //for(int i=ret[0];i<ret[0]+ret[1];i++){
            int kk = sub.linksNo[snodeMid][snodeMid1];
            if (Parameters.RecordLogModel) {
                int pa = ret[1];
                String str = "Check sub.slots[" + kk + "][" + ret[0] + "-" + pa + "]=0\r\n";
                WriteFilePlus("process.txt", str);
            }
            if (ret[0] <= ret[1]) {
                for (int i = ret[0]; i <= ret[1]; i++) {
                    if (i < 0) continue;
                    if (i >= Parameters.MaxSlots) break;

                    if (Parameters.DebugModel)
                        System.out.println("linksNo[" + snodeMid + "," + snodeMid1 + "]=" + kk + " i:" + i);
                    if (sub.slots[kk][i] == 0) return false;
                    sub.slots[kk][i] = 0;
                }
            } else {
                System.out.println("ret[0]>ret[1] because ret[0] mush little equal ret[1]" + "Error***********");
                return false;
            }
            snodeMid1 = snodeMid;
            if (snodeMid1 == sNode1) break;//�ӻ�㵽Դ���·���Ѿ�����
        }
        if (Parameters.DebugModel) System.out.println("UpdateSub is done.");
        return true;
    }

    /* ���ƣ�CheckIfEnoughSlotsOnPath
     * ���ܣ����ݹ�·������Ƿ����㹻������Ƶ�ײۣ����������۵�Ҫ��
     * ������sub���ײ�����磻path��·����slotNo����ʼƵ�ײ�������slotNum�������Ƶ�ײ�����
     *      sNode1\sNode2��·���������˵㣻
     * ����ֵ��-1��ʧ�ܷ��أ�>-1���ɹ�����Ƶ�ײ�������
     * �����ˣ�������
     * ����ʱ�䣺2019.1.7
     */
    public int CheckIfEnoughSlotsOnPath(EOSubstrateNetwork sub, DistanceParent[] path, int slotNo, int slotNum, int sNode1, int sNode2) {
        boolean checkIf = true;
        //if(path[sNode2] == null) return 0;
        //if(path[sNode1] == null) return 0;
        for (int i = slotNo; i < sub.slotsNum; i++) {
            if (path[sNode2] == null) break;
            int sNode3 = sNode2;
            checkIf = true;
            if (path[sNode3].parentVert != -1) {
                while (path[sNode3].parentVert != sNode1) {
                    int linkIndex = GetLinkNum(sub, sNode3, path[sNode3].parentVert);
                    if (CheckIfEnoughSlotsOnLink(sub, linkIndex, i, slotNum) == -1) {//���Ƶ�ײ��Ƿ�����Ҫ��
                        checkIf = false;
                        break;//������Ҫ��
                    }
                    sNode3 = path[sNode3].parentVert;
                }
                if (!checkIf) continue;
                //���sNode3->path[sNode3].parentVert
                int linkIndex = GetLinkNum(sub, sNode3, path[sNode3].parentVert);
                if (CheckIfEnoughSlotsOnLink(sub, linkIndex, i, slotNum) == -1) {//���Ƶ�ײ��Ƿ�����Ҫ��
                    checkIf = false;
                }
            }
            if (checkIf) return i;
        }
        for (int i = slotNo; i < sub.slotsNum; i++) {
            if (path[sNode1] == null) break;
            int sNode3 = sNode1;
            checkIf = true;
            if (path[sNode3].parentVert != -1) {
                while (path[sNode3].parentVert != sNode2) {
                    int linkIndex = GetLinkNum(sub, sNode3, path[sNode3].parentVert);
                    if (CheckIfEnoughSlotsOnLink(sub, linkIndex, i, slotNum) == -1) {//���Ƶ�ײ��Ƿ�����Ҫ��
                        checkIf = false;
                        break;//������Ҫ��
                    }
                    sNode3 = path[sNode3].parentVert;
                }
                //���sNode3->path[sNode3].parentVert
                int linkIndex = GetLinkNum(sub, sNode3, path[sNode3].parentVert);
                if (CheckIfEnoughSlotsOnLink(sub, linkIndex, i, slotNum) == -1) {//���Ƶ�ײ��Ƿ�����Ҫ��
                    checkIf = false;
                }
            }
            if (checkIf) return i;
        }

        return -1;
    }

    /******************************************************************
     *���ƣ�AuxiliaryGraph CreateAuxiliaryDiagram(......)
     *���ܣ���ILPģ�ͣ�WangY��Ϊ��������������ͼ
     *������	  subΪ���Թ�����
     *	      indexΪ��index����������
     *����ֵ��AuxiliaryGraph
     *******************************************************************/
    public AuxiliaryGraph CreateAuxiliaryDiagram(EOSubstrateNetwork sub, VONRequest reqs[], int index) {
        AuxiliaryGraph auxGraph = new AuxiliaryGraph();
        auxGraph.cpu = sub.cpu;
        auxGraph.diffSlot = sub.diffSlot;
        auxGraph.faNodes = sub.faNodes;
        auxGraph.faNodesNum = sub.faNodesNum;

        auxGraph.links = sub.links + reqs[index].links;
        auxGraph.linksNo = sub.linksNo;
        auxGraph.modulationLevel = sub.modulationLevel;
        auxGraph.modulevel = sub.modulevel;
        auxGraph.nodes = sub.nodes + reqs[index].nodes;
        auxGraph.opticalReach = sub.opticalReach;
        auxGraph.slotGHz = sub.slotGHz;
        auxGraph.slots = sub.slots;
        auxGraph.slotsNum = sub.slotsNum;
        auxGraph.transRate = sub.transRate;
        //auxGraph.virtServLinks = sub.virtServLinks;
        //auxGraph.virtualNodes = sub.virtualNodes;
        auxGraph.netNodes = sub.nodes - sub.faNodesNum;


        auxGraph.virtualNodes = new int[reqs[index].nodes];
        for (int i = sub.nodes, j = 0; i < sub.nodes + reqs[index].nodes; i++, j++) {
            auxGraph.virtualNodes[j] = i;//����ڵ��ڸ���ͼ�еĽڵ��
        }
        //�����ߣ�����ڵ���fNode֮�����·����CPU�͵���λ��
        int auxLinkNum = 0;//����������
        //���㸨��������
        for (int i = 0; i < reqs[index].nodes; i++) {
            for (int k = 0; k < sub.faNodesNum; k++) {
                if (reqs[index].cpu[i] <= auxGraph.cpu[auxGraph.faNodes[k]]) {
                    //if(auxGraph.cpu[auxGraph.virtualNodes[i]] <= auxGraph.cpu[auxGraph.faNodes[k]]){
                    auxLinkNum++;
                }
            }
        }
        auxGraph.links = sub.links + reqs[index].links + auxLinkNum - 1;
        auxGraph.link = new LinkStruct[sub.links + reqs[index].links + auxLinkNum - 1];//;
        for (int i = 0; i < sub.links; i++) {
            auxGraph.link[i] = sub.link[i];
        }
        for (int i = sub.links; i < sub.links + reqs[index].links + auxLinkNum - 1; i++) {
            auxGraph.link[i] = new LinkStruct();
        }

        auxGraph.virtServLinks = new LinkStruct[auxLinkNum];//����ռ�
        for (int i = 0; i < auxLinkNum; i++) {
            auxGraph.virtServLinks[i] = new LinkStruct();
        }

        //auxGraph.virtServLinks = new LinkStruct[auxLinkNum];//����ռ�
        int auxLinkID = sub.links + reqs[index].links - 1;//auxGraph.links;
        int virtualServeLink = 0;
        for (int i = 0; i < reqs[index].nodes; i++) {
            for (int k = 0; k < sub.faNodesNum; k++) {
                if (reqs[index].cpu[i] <= auxGraph.cpu[auxGraph.faNodes[k]]) {
                    //if(auxGraph.cpu[auxGraph.virtualNodes[i]] <= auxGraph.cpu[auxGraph.faNodes[k]]){
                    //���CPU��������������·����
                    auxGraph.link[auxLinkID].from = auxGraph.virtualNodes[i];
                    auxGraph.link[auxLinkID].to = auxGraph.faNodes[k];
                    auxGraph.link[auxLinkID].bw = 10000;//�����ߵ�������������
                    auxGraph.link[auxLinkID].length = 0;

                    auxGraph.virtServLinks[virtualServeLink].from = auxGraph.virtualNodes[i];
                    auxGraph.virtServLinks[virtualServeLink].to = auxGraph.faNodes[k];
                    auxGraph.virtServLinks[virtualServeLink].bw = 10000;
                    auxGraph.virtServLinks[virtualServeLink].length = 0;
                    virtualServeLink++;
                    auxLinkID++;
                }
            }
        }
        for (int i = 0; i < sub.links; i++) {
            for (int j = 0; j < Parameters.MaxSlots; j++) {
                auxGraph.slots[i][j] = sub.slots[i][j];
            }
        }


        auxGraph.serverNodes = sub.faNodes;

        return auxGraph;
    }

    /******************************************************************
     *���ƣ�AuxiliaryGraph CreateAuxiliaryDiagram(......)
     *���ܣ���ILPģ�ͣ�WangY��Ϊ��������������ͼ
     *������	  subΪ���Թ�����
     *	      indexΪ��index����������
     *����ֵ��AuxiliaryGraph
     *�����ˣ���������2019.1.14
     *******************************************************************/
    public AuxiliaryGraph CreateAuxiliaryDiagramBy(EOSubstrateNetwork sub, VONRequest reqs[], int index, int vNode1, int vNode2, int sNode1, int sNode2) {
        AuxiliaryGraph auxGraph = new AuxiliaryGraph();
        auxGraph.cpu = sub.cpu;
        auxGraph.diffSlot = sub.diffSlot;
        auxGraph.faNodes = sub.faNodes;
        auxGraph.faNodesNum = sub.faNodesNum;

        auxGraph.links = sub.links + reqs[index].links;
        auxGraph.linksNo = sub.linksNo;
        auxGraph.modulationLevel = sub.modulationLevel;
        auxGraph.modulevel = sub.modulevel;
        auxGraph.nodes = sub.nodes + reqs[index].nodes;
        auxGraph.opticalReach = sub.opticalReach;
        auxGraph.slotGHz = sub.slotGHz;
        auxGraph.slots = sub.slots;
        auxGraph.slotsNum = sub.slotsNum;
        auxGraph.transRate = sub.transRate;
        //auxGraph.virtServLinks = sub.virtServLinks;
        //auxGraph.virtualNodes = sub.virtualNodes;
        auxGraph.netNodes = sub.nodes - sub.faNodesNum;


        auxGraph.virtualNodes = new int[reqs[index].nodes];
        for (int i = sub.nodes, j = 0; i < sub.nodes + reqs[index].nodes; i++, j++) {
            auxGraph.virtualNodes[j] = i;//����ڵ��ڸ���ͼ�еĽڵ��
        }
        //�����ߣ�����ڵ���fNode֮�����·����CPU�͵���λ��
        int auxLinkNum = 0;//����������
        //���㸨��������
        auxLinkNum++;
        auxLinkNum++;
  		/*for(int i=0; i < reqs[index].nodes; i++){
  			for(int k=0;k<sub.faNodesNum;k++){
  				if(reqs[index].cpu[i] <= auxGraph.cpu[auxGraph.faNodes[k]]){
  				//if(auxGraph.cpu[auxGraph.virtualNodes[i]] <= auxGraph.cpu[auxGraph.faNodes[k]]){
  					auxLinkNum++;
  				}
  			}
  		}*/
        auxGraph.links = sub.links + auxLinkNum - 1;
        auxGraph.link = new LinkStruct[sub.links + auxLinkNum - 1];//;
        for (int i = 0; i < sub.links; i++) {
            auxGraph.link[i] = sub.link[i];
        }
        for (int i = sub.links; i < sub.links + auxLinkNum - 1; i++) {
            auxGraph.link[i] = new LinkStruct();
        }

        auxGraph.virtServLinks = new LinkStruct[auxLinkNum];//����ռ�
        for (int i = 0; i < auxLinkNum; i++) {
            auxGraph.virtServLinks[i] = new LinkStruct();
        }

        //auxGraph.virtServLinks = new LinkStruct[auxLinkNum];//����ռ�
        int auxLinkID = sub.links - 1;//auxGraph.links;
        int virtualServeLink = 0;
        auxGraph.link[auxLinkID].from = vNode1;
        auxGraph.link[auxLinkID].to = sNode1;
        auxGraph.link[auxLinkID].bw = 0;//�����ߵ�������������
        auxGraph.link[auxLinkID].length = 1;

        auxGraph.virtServLinks[virtualServeLink].from = vNode1;
        auxGraph.virtServLinks[virtualServeLink].to = sNode1;
        auxGraph.virtServLinks[virtualServeLink].bw = 0;
        auxGraph.virtServLinks[virtualServeLink].length = 1;
        virtualServeLink++;
        auxLinkID++;

        auxGraph.link[auxLinkID].from = vNode2;
        auxGraph.link[auxLinkID].to = sNode2;
        auxGraph.link[auxLinkID].bw = 0;//�����ߵ�������������
        auxGraph.link[auxLinkID].length = 1;

        auxGraph.virtServLinks[virtualServeLink].from = vNode2;
        auxGraph.virtServLinks[virtualServeLink].to = sNode2;
        auxGraph.virtServLinks[virtualServeLink].bw = 0;
        auxGraph.virtServLinks[virtualServeLink].length = 1;

        auxGraph.serverNodes = sub.faNodes;

        return auxGraph;
    }

    /* ���ƣ�CheckIfEnoughSlotsOnLink
     * ���ܣ�������·�š�Ƶ�ײۺš�Ƶ�ײ������͸���ۣ��ж���·�Ƿ�����������Ҫ��
     * ������sub���ײ�����磻linkNum����·�ţ�slotNo��Ƶ�ײۺţ�slotNum��Ƶ�ײ�����
     * ����ֵ��-1��ʧ�ܷ��أ�1������Ҫ��
     * �����ˣ�������
     * ����ʱ�䣺2019.1.7
     */
    public int CheckIfEnoughSlotsOnLink(EOSubstrateNetwork sub, int linkNum, int slotNo, int slotNum) {
        //�������
        if (linkNum < 0 || slotNo < 0 || slotNum < 0) return -1;
        if (slotNum > sub.slotsNum) {
            if (Parameters.DebugModel) {
                System.out.println("CheckAllPathFreeSlots is error, slotNum.************************");
            }
            if (Parameters.ErrorRecord) {
                String str = "CheckIfEnoughSlotsOnLink():" + "error1, slotNum." + "\r\n";
                WriteFilePlus("error.txt", str);
            }
            return -1;
        }
        if (linkNum > sub.links - 1) {
            if (Parameters.DebugModel) {
                System.out.println("CheckIfEnoughSlotsOnLink is error, linkNum.************************");
            }
            if (Parameters.ErrorRecord) {
                String str = "CheckIfEnoughSlotsOnLink():" + "error2, slotNum." + "\r\n";
                WriteFilePlus("error.txt", str);
            }
            return -1;
        }
        //���е�·���ϼ���Ƿ����㹻������Ƶ�ײ�
//        if (slotNo > 0 && sub.slots[linkNum][slotNo - 1] != 1) return -1;//������ڽӵ�Ƶ�ײ��Ƿ�����Ҫ��
        for (int i = slotNo; i < slotNo + slotNum; i++) {
            if (i > sub.slotsNum - 1) return -1;
            if (Parameters.DebugModel) System.out.println("sub.slots[" + linkNum + "][" + i + "]");
            if (sub.slots[linkNum][i] != 1 && i < sub.slotsNum) return -1;
        }
        int t = slotNo + slotNum;
        if (Parameters.DebugModel) System.out.println("sub.slots[" + linkNum + "][" + t + "]");
        if (slotNo + slotNum < sub.slotsNum - 1 && sub.slots[linkNum][slotNo + slotNum] != 1)
            return -1;//������ڽӵ�Ƶ�ײ��Ƿ�����Ҫ��
        return 1;
    }

    /* ���ƣ�CheckIfEnoughSlotsOnLink
     * ���ܣ�������·�š������ж���·�Ƿ�����������Ҫ��
     * ������sub���ײ�����磻linkNum����·�ţ�bw������
     * ����ֵ��-1��ʧ�ܷ��أ�>-1������Ҫ���Ƶ�ײۣ�
     * �����ˣ�������
     * ����ʱ�䣺2019.6.19
     */
    public int CheckIfEnoughSlotsOnLink(EOSubstrateNetwork sub, int linkNum, double bw) {
        //����bw����Ƶ�ײ�
        int slotNum = -1;
        slotNum = CalculateSlots(bw, sub.link[linkNum].length);

        //�������
        //if(linkNum < 0 || slotNo < 0 || slotNum < 0) return -1;
        if (slotNum > sub.slotsNum) {
            if (Parameters.DebugModel) {
                System.out.println("CheckAllPathFreeSlots is error, slotNum.************************");
            }
            if (Parameters.ErrorRecord) {
                String str = "CheckIfEnoughSlotsOnLink():CheckAllPathFreeSlots is error, slotNum. \r\n";
                WriteFilePlus("error.txt", str);
            }
            return -1;
        }
        if (linkNum > sub.links - 1) {
            if (Parameters.DebugModel) {
                System.out.println("CheckAllPathFreeSlots is error, linkNum.************************");
            }
            if (Parameters.ErrorRecord) {
                String str = "CheckIfEnoughSlotsOnLink():CheckAllPathFreeSlots is error, slotNum. \r\n";
                WriteFilePlus("error.txt", str);
            }
            return -1;
        }
        //���е�·���ϼ���Ƿ����㹻������Ƶ�ײ�
        int slotNo = 0;
        //if(slotNo>0 && sub.slots[linkNum][slotNo-1]!=1) return -1;//������ڽӵ�Ƶ�ײ��Ƿ�����Ҫ��
        boolean find = true;
        while (slotNo < sub.slotsNum) {
            find = true;
            for (int i = slotNo; i < slotNo + slotNum; i++) {
                if (i > sub.slotsNum - 1) return -1;
                if (Parameters.DebugModel) {
                    System.out.println("sub.slots[" + linkNum + "][" + i + "]");
                }

                if (sub.slots[linkNum][i] != 1 && i < sub.slotsNum) {
                    find = false;
                }
            }
            if (find) return slotNo;
            slotNo++;
        }
        return -1;
    }

    /* ���ƣ�CalculateSlots
     * ���ܣ����ݴ������ƺͱ���������������Ҫ��Ƶ�ײ�����
     * ������bw������Ĵ���mp�����Ʋ�����guardBand����������
     * ����ֵ��-1��ʧ�ܷ��أ�>0��������Ƶ�ײ�������
     * �����ˣ�������
     * ����ʱ�䣺2019.6.22
     */
    public int CalculateSlots(double bw, int mp, int guardBand) {
        int slotNum = (int) (Math.ceil(bw / (12.5 * mp))) + guardBand;
        return slotNum;
    }

    /* ���ƣ�CalculateSlots
     * ���ܣ����ݴ������ơ���������ͳ��ȣ���������Ҫ��Ƶ�ײ�����
     * ������bw������Ĵ���mp�����Ʋ�����guardBand����������length:����
     * ����ֵ��-1��ʧ�ܷ��أ�>0��������Ƶ�ײ�������
     * �����ˣ�������
     * ����ʱ�䣺2019.7.11
     */
    public int CalculateSlots(double bw, int mp, int guardBand, double length, double mdLength) {
        if (length > mdLength) return 0;
        int slotNum = (int) (Math.ceil(bw / (12.5 * mp))) + guardBand;
        return slotNum;
    }

    /* ���ƣ�CalMD
     * ���ܣ����ݹ�·�����ȣ��������ģʽ
     * ������bw������Ĵ���pathLength��·������
     * ����ֵ��-1��ʧ�ܷ��أ�>0������������ģʽ��
     * �����ˣ�������
     * ����ʱ�䣺2020.1.31
     */
    public int CalMD(double bw, double pathLength) {
        int mp = -1;
        if (pathLength < 0) return -1;//ʧ�ܷ���
        if (pathLength <= 375) mp = 4;
        else if (pathLength <= 750) mp = 3;
        else if (pathLength <= 1500) mp = 2;
        else if (pathLength <= 3000) mp = 1;
        else return -1;//����3000km�����޷�����
        return mp;
    }

    /* ���ƣ�CalculateSlots
     * ���ܣ����ݹ�·�����ȣ���������Ҫ��Ƶ�ײ�����
     * ������bw������Ĵ���pathLength��·������
     * ����ֵ��-1��ʧ�ܷ��أ�>0��������Ƶ�ײ�������
     * �����ˣ�������
     * ����ʱ�䣺2019.1.6
     */
    public int CalculateSlots(double bw, double pathLength) {
        int mp = -1;
        if (pathLength < 0) return -1;//ʧ�ܷ���
        if (pathLength <= 375) mp = 4;
        else if (pathLength <= 750) mp = 3;
        else if (pathLength <= 1500) mp = 2;
        else if (pathLength <= 3000) mp = 1;
        else return -1;//����3000km�����޷�����
        int slotNum = (int) (Math.ceil(bw / (12.5 * mp)) + Parameters.GuardBand);
        if (slotNum >= Parameters.MaxSlots && Parameters.DebugModel) return -1;
        if(Parameters.DebugModel) {
            System.out.println("bw:" + bw + " mp:" + mp + " slotNum:" + slotNum + " pathLength:" + pathLength);
        }
        return slotNum;
		/*
		int mp = -1;
		if(pathLength <= 375) mp = 4;
		else if(pathLength <= 750) mp = 3;
		else if(pathLength <= 1500) mp = 2;
		else if(pathLength <= 3000) mp = 1;
		else return 0;//����3000km�����޷�����
		int slotNum = (int) (Math.floor(bw/(12.5*mp))+1);
		return slotNum;*/
    }
public int CalculateMp(double length) {
        int mp = -1;
        if (length <= 375) mp = 4;
        else if (length <= 750) mp = 3;
        else if (length <= 1500) mp = 2;
        else if (length <= 3000) mp = 1;
        else return -1;//����3000km�����޷�����
        return mp;
    }
    public double getPowerByCapacityAndLength(double capacity, double totalPathLength) {
        // ���ձ��ӳ�����
        if (capacity <= 40) { // 40 Gbps
            if (totalPathLength <= 600) return 154.8;
            else if (totalPathLength <= 1900) return 183.6;
            else if (totalPathLength <= 2500) return 183.6;
            else if (totalPathLength <= 3000) return 183.6;
            else if (totalPathLength <= 4000) return 183.6;
        } else if (capacity <= 100) { // 100 Gbps
            if (totalPathLength <= 600) return 198;
            else if (totalPathLength <= 1900) return 270;
            else if (totalPathLength <= 2500) return 270;
            else if (totalPathLength <= 3000) return 270;
            else if (totalPathLength <= 3500) return 270;
        } else if (capacity <= 200) { // 200 Gbps
            if (totalPathLength <= 500) return 333;
            else if (totalPathLength <= 600) return 333;
            else if (totalPathLength <= 750) return 333;
            else if (totalPathLength <= 1900) return 432;
            else if (totalPathLength <= 2200) return 432;
            else if (totalPathLength <= 2500) return 432;
        } else if (capacity <= 400) { // 400 Gbps
            if (totalPathLength <= 500) return 432;
            else if (totalPathLength <= 600) return 432;
            else if (totalPathLength <= 750) return 432;
            else if (totalPathLength <= 1900) return 630;
            else if (totalPathLength <= 2200) return 630;
            else if (totalPathLength <= 2500) return 630;
        }

        // �������Χ����Ĭ��ֵ��-1��ʾ����
        return -1;
    }



    /*
     * ͨ��(sNode1,sNode2)�õ���·��
     */
    public int GetLinkNum(VONRequest reqs[], int index, int sNode1, int sNode2) {
        int linkNum = -1;
        for (int i = 0; i < reqs[index].links; i++) {
            if ((reqs[index].link[i].from == sNode1 && reqs[index].link[i].to == sNode2) || (reqs[index].link[i].from == sNode2 && reqs[index].link[i].to == sNode1)) {
                linkNum = i;
                break;
            }
        }
        return linkNum;
    }

    /*
     * ͨ��(sNode1,sNode2)�õ���·��
     */
    public int GetLinkNum(EOSubstrateNetwork sub, int sNode1, int sNode2) {
        int linkNum = -1;
        for (int i = 0; i < sub.links; i++) {
            if ((sub.link[i].from == sNode1 && sub.link[i].to == sNode2) || (sub.link[i].from == sNode2 && sub.link[i].to == sNode1)) {
                linkNum = i;
                break;
            }
        }
        return linkNum;
    }

    /*
     * ���ܣ�ͨ��(sNode1,sNode2)�õ���·�ţ������ظ���·�ĳ���
     * �����ˣ�chen xh
     * ����ʱ�䣺2019/7/12
     */
    public double GetLength(EOSubstrateNetwork sub, int sNode1, int sNode2) {
        int linkNum = -1;
        for (int i = 0; i < sub.links; i++) {
            if ((sub.link[i].from == sNode1 && sub.link[i].to == sNode2) || (sub.link[i].from == sNode2 && sub.link[i].to == sNode1)) {
                linkNum = i;
                break;
            }
        }
        return sub.link[linkNum].length;
    }

    /*
     * ���ܣ�ͨ��(sNode1,sNode2)�õ���·�ţ������ظ���·�ĳ���
     * �����ˣ�chen xh
     * ����ʱ�䣺2019/7/12
     */
    public void GetMDAndMDLength(int[] MD, int[] MDLength) {
        MD[0] = Parameters.MDBPSK;
        MD[1] = Parameters.MDQPSK;
        MD[2] = Parameters.MD8QAM;
        MD[3] = Parameters.MD16QAM;
        MD[4] = Parameters.MD64QAM;
        MD[5] = Parameters.MD256QAM;

        MDLength[0] = Parameters.MDBPSK_Length;
        MDLength[1] = Parameters.MDQPSK_Length;
        MDLength[2] = Parameters.MD8QAM_Length;
        MDLength[3] = Parameters.MD16QAM_Length;
        MDLength[4] = Parameters.MD64QAM_Length;
        MDLength[5] = Parameters.MD256QAM_Length;
    }

    /*
     * ����ÿ��·������
     */
    public double GetPathLength(EOSubstrateNetwork sub, DistanceParent[] shortestPath, int sNode1, int sNode2) {
        double pathLength = 0;
        int linkIndex = -1;//link
        if (shortestPath[sNode2] != null) {
            if (shortestPath[sNode2].parentVert == sNode1) {
                linkIndex = GetLinkNum(sub, sNode2, shortestPath[sNode2].parentVert);
                pathLength += sub.link[linkIndex].length;
            } else while (shortestPath[sNode2].parentVert != sNode1) {
                linkIndex = GetLinkNum(sub, sNode2, shortestPath[sNode2].parentVert);
                pathLength += sub.link[linkIndex].length;
                sNode2 = shortestPath[sNode2].parentVert;
            }
        } else if (shortestPath[sNode1] != null) {
            if (shortestPath[sNode1].parentVert == sNode2) {
                linkIndex = GetLinkNum(sub, sNode1, shortestPath[sNode1].parentVert);
                pathLength += sub.link[linkIndex].length;
            } else while (shortestPath[sNode1].parentVert != sNode2) {
                linkIndex = GetLinkNum(sub, sNode1, shortestPath[sNode1].parentVert);
                pathLength += sub.link[linkIndex].length;
                sNode1 = shortestPath[sNode1].parentVert;
            }
        } else {
            return -1;
        }
        if(Parameters.DebugModel) {
            System.out.println("pathLength:" + pathLength);
        }
        return pathLength;
    }

    /*
     * ����ÿ��·�����������С�ڵ���2����˵��·����Ч
     */
    public int GetPathJump(AuxiliaryGraph sub, DistanceParent[] shortestPath, int pathNum, int sNode1, int sNode2) {
        int pathLength = 0;
        if (shortestPath[sNode2] == null) return 0;
        while (shortestPath[sNode2].parentVert != sNode1) {
            sNode2 = shortestPath[sNode2].parentVert;
            pathLength++;
        }
        return pathLength + 1;
    }

    /*����:�õ�����ĳ����·node1->node2��·������
     **����ֵ��·����������*/
    public int GetPathSumPassLink(AuxiliaryGraph auxGraph, DistanceParent[][][] kShortestPath, int[][] pathNo, int node1, int node2, int[] pathEff, VONRequest reqs[], int index) {
        int pathSum = 0;
        for (int i = 0; i < reqs[index].links; i++) {
            for (int j = 0; j < pathEff[i]; j++) {
                if (pathNo[i][j] > -1) {
                    int vNode1 = -1, vNode2 = -1, sNode1 = -1, sNode2 = -1;

                    vNode1 = reqs[index].link[i].from;
                    vNode2 = reqs[index].link[i].to;
                    sNode1 = auxGraph.virtualNodes[vNode1];
                    sNode2 = auxGraph.virtualNodes[vNode2];
                    if ((sNode1 == node1 && sNode2 == node2) || (sNode2 == node1 && sNode1 == node2)) {
                        pathSum++;
                        continue;
                    }
                    while (kShortestPath[i][j][sNode2].parentVert != sNode1) {
                        int sNode3 = kShortestPath[i][j][sNode2].parentVert;
                        if ((sNode3 == node1 && sNode2 == node2) || (sNode2 == node1 && sNode3 == node2)) {
                            pathSum++;
                            break;
                        }
                        sNode2 = kShortestPath[i][j][sNode2].parentVert;
                    }
                    if (sNode2 == node2 && kShortestPath[i][j][sNode2].parentVert == node1)
                        pathSum++;
                }
            }
        }
        return pathSum;
    }

    /*����:�õ�����ĳ���ڵ�node��·�����
     **����ֵ��-1��ʧ�ܷ��أ�˵��û���ҵ�������·��
     **      >-1:�ɹ����أ�˵���ҵ�������·����*/
    public int IncludeNodeInPath(AuxiliaryGraph auxGraph, DistanceParent[][][] kShortestPath, int[][] pathNo, int node, VONRequest reqs[], int index) {
        for (int i = 0; i < reqs[index].links; i++) {
            for (int j = 0; j < Parameters.K_PATH; j++) {
                if (pathNo[i][j] > -1) {
                    int vNode1 = -1, vNode2 = -1, sNode1 = -1, sNode2 = -1;

                    vNode1 = reqs[index].link[i].from;
                    vNode2 = reqs[index].link[i].to;
                    sNode1 = auxGraph.virtualNodes[vNode1];
                    sNode2 = auxGraph.virtualNodes[vNode2];
                    if (sNode1 == node || sNode2 == node) return pathNo[i][j];
                    while (kShortestPath[i][j][sNode2].parentVert != sNode1) {
                        sNode2 = kShortestPath[i][j][sNode2].parentVert;
                        if (sNode2 == node) return pathNo[i][j];
                    }
                }
            }
        }
        return -1;
    }

    /*����:�õ�����ڵ��Ӧ�ĸ���ͼ�ڵ�Ķ�
     **����ֵ������ڵ��Ӧ�ĸ���ͼ�ڵ�Ķ�*/
    public int GetDegreeOfNode(AuxiliaryGraph auxGraph, int node) {
        int degree = 0;
        for (int j = 0; j < auxGraph.virtServLinks.length; j++) {
            if (auxGraph.virtServLinks[j].from == node) degree++;
        }
        return degree;
    }

    /*����:�õ�����ĳ����·node1->node2��·�����
     **����ֵ��-1��ʧ�ܷ��أ�˵��û���ҵ�������·��
     **      >-1:�ɹ�����·����Ч��ţ�˵���ҵ�������·����*/
    public int IncludeLinkInPath(AuxiliaryGraph auxGraph, DistanceParent[][][] kShortestPath, int[][] pathNo, int vLinkNo, int pathNum, int k, VONRequest reqs[], int index) {
        int node1, node2;
        //node1 = auxGraph.link[vLinkNo].from;
        //node2 = auxGraph.link[vLinkNo].to;
        node1 = auxGraph.virtServLinks[k].from;
        node2 = auxGraph.virtServLinks[k].to;
        if (pathNo[vLinkNo][pathNum] > -1)//˵������������Ч·��
        {
            int vNode1 = -1, vNode2 = -1, sNode1 = -1, sNode2 = -1;

            vNode1 = reqs[index].link[vLinkNo].from;
            vNode2 = reqs[index].link[vLinkNo].to;
            sNode1 = auxGraph.virtualNodes[vNode1];
            sNode2 = auxGraph.virtualNodes[vNode2];
            int sNode3 = -1;
            if ((sNode1 == node1 && sNode2 == node2) || (sNode1 == node2 && sNode2 == node1))
                return pathNo[vLinkNo][pathNum];
            while (kShortestPath[vLinkNo][pathNum][sNode2].parentVert != sNode1) {
                sNode3 = sNode2;
                sNode2 = kShortestPath[vLinkNo][pathNum][sNode2].parentVert;

                if ((sNode3 == node1 && sNode2 == node2) || (sNode3 == node2 && sNode2 == node1))
                    return pathNo[vLinkNo][pathNum];
            }
            if ((sNode1 == node1 && sNode2 == node2) || (sNode1 == node2 && sNode2 == node1))
                return pathNo[vLinkNo][pathNum];

        }

        return -1;
    }

    //******************************************************************
    //���ƣ�int CheckIfSlotEnoughByNode(......)
    //���ܣ����ײ�ڵ����·��Ƶ���Ƿ���������ڵ����ӵ�Ƶ��Ҫ��
    //������
    //	      subΪ��������
    //	      sNodeΪ����ڵ�
    //	      reqsΪ����������
    //	      indexΪ��index����������
    //����ֵ��contSlotsNum���ɹ�������������Ƶ�ײ�������-1��ʧ�ܷ���
    //      link[0]:���ص���·�ţ�
    //******************************************************************
    public int CheckIfSlotEnoughByNode(EOSubstrateNetwork sub, int sNode, VONRequest reqs[], int index, int vNode, int link[]) {
        //��������ڵ��������·������С����·�����ȳ���2��Ҫ������������·����
        //int degree = 0;
        double maxVBW = 0;
        double minVBW = 10000;
        for (int i = 0; i < reqs[index].links; i++) {
            if (vNode == reqs[index].link[i].from || vNode == reqs[index].link[i].to) {
                //degree ++;
                if (maxVBW < reqs[index].link[i].bw) maxVBW = reqs[index].link[i].bw;
                if (minVBW > reqs[index].link[i].bw) minVBW = reqs[index].link[i].bw;
            }
        }
        //������Ҫ���ٵ�Ƶ�ײ�����
        int minSlots = 0;
        //minSlots = (int) (Math.floor(minVBW/(12.5*4))+1);
        minSlots = (int) (Math.ceil(maxVBW / (12.5 * 4)) + Parameters.GuardBand);
        if (minSlots > Parameters.MaxSlots) return -1;
        //��������ڵ�sNode����·Ƶ�ײ���������С����·����
        int indexOfSlots = -1;
        for (int i = 0; i < sub.links; i++) {
            if (sub.link[i].from == sNode || sub.link[i].to == sNode) {
                //�������������Ƶ�ײ�����>=minSlots if(sub.slots[i][])
                indexOfSlots = GetIndexOfSlotsByNum(sub, i, minSlots);
                if (indexOfSlots > -1) {
                    link[0] = i;
                    return indexOfSlots;
                }
            }
        }
        return -1;
    }

    //******************************************************************
    //���ƣ�int GetIndexOfSlotsByNum(......)
    //���ܣ����ײ�ڵ����·�����Ĺ�Ƶ���Ƿ�����Ƶ������Ҫ��
    //������
    //	      subΪ��������
    //	      slotNumΪ�ײ�����Ƶ�ײ�����
    //����ֵ��contSlotsNum���ɹ�������С��Ƶ�ײ�������-1��ʧ�ܷ���
    //******************************************************************
    private int GetIndexOfSlotsByNum(EOSubstrateNetwork sub, int linkNum, int slotNum) {
        int contSlotsNum = -1;
        //boolean find = true;
        for (int i = 0; i < sub.slotsNum - slotNum + 1; i++) {
            //find = true;
            for (int j = 0; j < slotNum; j++) {
                if (sub.slots[linkNum][j] == 0) {
                    //find = false;
                    break;
                }
            }
            return i;
        }
        return contSlotsNum;
    }

    /*
     * �������·��ӳ���Ƿ���Ч
     */
    public boolean CheckTwoPathsSlotsIfEff(VONRequest reqs[], int index, DistanceParent[][][] kShortestPath, int p[][], int virtualNodes[], int retLinkE[], int retSlotSE[], int retSlotEE[], int vLinkNo1, int vLinkNo2) {
        //ȡ��һ��·����ÿһ����·l{ij}
        //����ڶ���·���Ƿ�����·l{ij}�غ�,��������غϣ�����slots�Ƿ��ͻ�������ͻ���򷵻�true�����򣬷���false
        int sNode1 = virtualNodes[reqs[index].link[vLinkNo1].from];//������·�˵�from��Ӧ�ĸ���ͼ�ڵ���
        int sNode2 = virtualNodes[reqs[index].link[vLinkNo1].to];//������·�˵�to��Ӧ�ĸ���ͼ�ڵ���
        int sNode3 = sNode2;
        int pathByLink = GetPathNoInLinkByPath(reqs, index, retLinkE[vLinkNo1]);//retLinkE[vLinkNo1];//GetPathByLink(vLinkNo1);//������·��Ӧ�ĵڼ���·��
        boolean find = false;
        //while(kShortestPath[vLinkNo1][pathByLink][sNode3].parentVert != sNode1){
        while (sNode3 != sNode1) {
            //if(sNode3 != sNode2){//���������˵�
            //	p[vLinkNo1][sNode3] = kShortestPath[vLinkNo1][pathByLink][sNode3].parentVert;
            //}
            System.out.println(sNode3 + "->");
            int sNode4 = -1;
            if (kShortestPath[vLinkNo1][pathByLink][sNode3] != null) {
                sNode4 = kShortestPath[vLinkNo1][pathByLink][sNode3].parentVert;
            } else {
                return false;
            }
            if (sNode3 == sNode1 || sNode3 == sNode2 || sNode4 == sNode1 || sNode4 == sNode2) {
                sNode3 = kShortestPath[vLinkNo1][pathByLink][sNode3].parentVert;
                continue;
            }
            if (CheckIfSameLinkInTwoPath(sNode3, sNode4, virtualNodes, reqs, index, kShortestPath, p, retLinkE, vLinkNo2) == true) {
                find = true;
                break;
            }
            sNode3 = kShortestPath[vLinkNo1][pathByLink][sNode3].parentVert;
        }
        //System.out.println(sNode3+"->"+sNode1+"("+retSlotSE[i]+"-"+retSlotEE[i]+")");
        System.out.println("retSlotSE[" + vLinkNo1 + "]=" + retSlotSE[vLinkNo1]);
        System.out.println("retSlotEE[" + vLinkNo1 + "]=" + retSlotEE[vLinkNo1]);
        System.out.println("retSlotSE[" + vLinkNo2 + "]=" + retSlotSE[vLinkNo2]);
        System.out.println("retSlotEE[" + vLinkNo2 + "]=" + retSlotEE[vLinkNo2]);
        if (find == true) {//˵���ҵ�����ͬ����·,����Ƶ�ײ��Ƿ���Ч
            if (retSlotSE[vLinkNo1] <= retSlotEE[vLinkNo2] && retSlotSE[vLinkNo1] >= retSlotSE[vLinkNo2]) {
                System.out.println("1 false");
                return false;
            } else if (retSlotEE[vLinkNo1] <= retSlotEE[vLinkNo2] && retSlotEE[vLinkNo1] >= retSlotSE[vLinkNo2]) {
                System.out.println("2 false");
                return false;
            }
        }
        return true;
    }

    /*
     * ͨ��·����ţ��õ�ĳ����·ӳ���·�����
     */
    public int GetPathNoInLinkByPath(VONRequest reqs[], int index, int path) {
        int pathSum = 0;
        for (int i = 0; i < reqs[index].links; i++) {
            for (int j = 0; j < Parameters.K_PATH; j++) {
                if (path == pathSum)
                    return j;
                pathSum++;
            }
        }
        return -1;
    }

    /*
     * ���һ����·(sNode1,sNode2)�Ƿ���·����
     * ������sNode1-sNode2:��ʾ��·�������˵㣻
     */
    public boolean CheckIfSameLinkInTwoPath(int sNode1, int sNode2, int virtualNodes[], VONRequest reqs[], int index, DistanceParent[][][] kShortestPath, int p[][], int retLinkE[], int vLinkNo) {
        int sNode4 = virtualNodes[reqs[index].link[vLinkNo].from];//������·�˵�from��Ӧ�ĸ���ͼ�ڵ���
        int sNode5 = virtualNodes[reqs[index].link[vLinkNo].to];//������·�˵�to��Ӧ�ĸ���ͼ�ڵ���
        int sNode3 = sNode5;
        int pathByLink = GetPathNoInLinkByPath(reqs, index, retLinkE[vLinkNo]);
        //while(kShortestPath[vLinkNo][pathByLink][sNode3].parentVert != sNode4){
        while (sNode3 != sNode4) {
            //if(sNode3 != sNode2){//���������˵�
            //p[vLinkNo][sNode3] = kShortestPath[vLinkNo][pathByLink][sNode3].parentVert;
            //}
            System.out.print("kShortestPath[" + vLinkNo + "][" + pathByLink + "][" + sNode3 + "].");
            if (kShortestPath[vLinkNo][pathByLink][sNode3] != null)
                System.out.print(kShortestPath[vLinkNo][pathByLink][sNode3].parentVert + "\r\n");
            int sNode6 = -1;
            if (kShortestPath[vLinkNo][pathByLink][sNode3] != null)
                sNode6 = kShortestPath[vLinkNo][pathByLink][sNode3].parentVert;
            else break;
            if ((sNode3 == sNode1 && sNode6 == sNode2) || (sNode3 == sNode2 && sNode6 == sNode1)) {
                return true;
            }
            sNode3 = kShortestPath[vLinkNo][pathByLink][sNode3].parentVert;
        }
        //int sNode6 = kShortestPath[vLinkNo][pathByLink][sNode3].parentVert;
        //if((sNode3 == sNode1 && sNode6 == sNode2)||(sNode3 == sNode2 && sNode6 == sNode1)){
        //	return true;
        //}
        return false;
    }

    /*
     * ���·��ӳ���Ƿ���Ч
     *
     */
    public boolean CheckPathSlotsIfEff(VONRequest reqs[], int index, DistanceParent[][][] kShortestPath, int p[][], int virtualNodes[], int retLinkE[], int retSlotSE[], int retSlotEE[]) {
        //boolean CheckTwoPathsSlotsIfEff(VONRequest reqs[],int index,DistanceParent[][][]  kShortestPath,int p[][],int virtualNodes[],int retLinkE[],int retSlotSE[],int retSlotEE[],int vLinkNo1,int vLinkNo2)

        for (int i = 0; i < reqs[index].links; i++) {
            for (int j = i + 1; j < reqs[index].links; j++) {
                boolean check = CheckTwoPathsSlotsIfEff(reqs, index, kShortestPath, p, virtualNodes, retLinkE, retSlotSE, retSlotEE, i, j);
                if (check == false) return false;
            }
        }
        return true;
    }


    /**
     * ���ƣ�processActivatedNode
     * ���ܣ�����������ڵ��ϵ�����ڵ㼰�����������·������ӳ��
     * ������
     * sub            �ײ���������
     * reqs           ����������������
     * activatedSNode ���������ڵ���
     *
     * @return
     */
    public void processActivatedNode(EOSubstrateNetwork sub, VONRequest[] reqs, int activatedSNode,double[] result) {
        // 1) ȡ��������ڵ��ϱ�ӳ�������ڵ��б��ɶԣ�VN��������VN�ڵ�����ڵ�������
        result[0] = 0;  // ��ʼ���ܺ��ۼ�ֵ
        result[1] = 0;  // ��ʼ����·����
        List<Integer> vnIndices   = s2v_n[activatedSNode].req;
        List<Integer> vnodeIndices= s2v_n[activatedSNode].vnode;

        if (vnIndices == null || vnodeIndices == null || vnIndices.size() != vnodeIndices.size()) {
            // �ɻ�����־��ӳ����Ϣ��������ֱ�ӷ���
            return ;
        }

        // ��¼�Ѵ���ġ�������·��������ͬһ�� vLink �������˵��ظ�����
        // key = (vnIndex << 32) | vLinkIdx
        java.util.Set<Long> processedVLinks = new java.util.HashSet<>();

        // 2) �������ڵ㴦����֤˳���ȵ�һ������ڵ�����й�����·���ٵڶ�������
        for (int i = 0; i < vnIndices.size(); i++) {
            int vnIndex = vnIndices.get(i);
            int vnode   = vnodeIndices.get(i);

            if (vnIndex < 0 || vnIndex >= reqs.length || reqs[vnIndex] == null) {
                // VN �����Ƿ�������
                continue;
            }
            VONRequest vn = reqs[vnIndex];

            // 3) �ҳ����뵱ǰ����ڵ������������������·
            //    ע�⣺��Ҫȥ�أ�������Ҫ��ÿ��������������·���г�����
            //    ȥ���� processedVLinks ��������������ڵ��ȥ�أ���
            java.util.List<Integer> associatedVLinks = new java.util.ArrayList<>();
            for (int vLinkIdx = 0; vLinkIdx < vn.links; vLinkIdx++) {
                LinkStruct vLink = vn.link[vLinkIdx];
                if (vLink == null) continue;
                if (vLink.from == vnode || vLink.to == vnode) {
                    associatedVLinks.add(vLinkIdx);
                }
            }

            // 4) ����������������·������������ӳ��
            for (int vLinkIdx : associatedVLinks) {
                long key = (((long) vnIndex) << 32) | (vLinkIdx & 0xffffffffL);
                // ������ vLink �ڱ�����ڵ�����������Ѿ��������������
                if (!processedVLinks.add(key)) {
                    continue;
                }

                // ȡ�� vLink ������·��
                if (v2s[vnIndex] == null || v2s[vnIndex].pathFlow == null) continue;
                if (vLinkIdx < 0 || vLinkIdx >= v2s[vnIndex].pathFlow.size()) continue;

                SpathFlow pathFlow = v2s[vnIndex].pathFlow.get(vLinkIdx);
                if (pathFlow == null || pathFlow.link == null || pathFlow.link.size() < 2) {
                    continue;
                }

                java.util.List<Integer> physicalNodePath = pathFlow.link;
                // ����ڵ����� [s1, s2, s3, ...]
                // ���������·�� (s1->s2, s2->s3, ...)
                double totalPathLength = 0;
                for (int j = 0; j < physicalNodePath.size() - 1; j++) {
                    int sNodeFrom = physicalNodePath.get(j);
                    int sNodeTo   = physicalNodePath.get(j + 1);

                    int physicalLinkNum = GetLinkNum(sub, sNodeFrom, sNodeTo);
                    if (physicalLinkNum < 0) {
                        // ���Է����������糣����
                        physicalLinkNum = GetLinkNum(sub, sNodeTo, sNodeFrom);
                        if (physicalLinkNum < 0) {

                        }
                    }
                    double segmentLength = sub.link[physicalLinkNum].length; // ����������·����
                    totalPathLength += segmentLength;
                    // �Ը�������·�ν�������߼������Ʒ�/�ܺ�/ͳ��/��ǵȣ�
                    //processPhysicalLink(sub, vnIndex, vnode, vLinkIdx, physicalLinkNum, sNodeFrom, sNodeTo);
                }
                int slotNum = CalculateSlots(vn.link[vLinkIdx].bw, totalPathLength);
                int mp = CalculateMp(totalPathLength);
                double capacity = slotNum * 12.5 * mp;
                double power = getPowerByCapacityAndLength(capacity, totalPathLength);
                result[0] += power;  // �ۼ��ܺ�
                result[1] += 1;      // �ۼ���·����
            }
            // ���ˣ���ǰ����ڵ�����й���������·�Ѵ�����ϣ�������һ������ڵ�
        }
        return ;
    }
    public  void  CalculateEnergyConsumption(EOSubstrateNetwork sub, VONRequest reqs[], int end, int time){
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
        for (int i=0; i<sub.nodes;i++) {
            int valueA=0;//�ڵ�Ķ�
            int degreeValue=0;
            for (int j = 0; j < sub.nodes; j++) {
                if (array[i][j] != 0) {
                    valueA++;
//                    for(int k=0;k<sub.links;k++){
//                        if((i==sub.link[k].from&&j==sub.link[k].to)||(i==sub.link[k].to&&j==sub.link[k].from)){
//
//                        }
                    degreeValue+=degreeArray[i][j];
                }
            }

            if (valueA!=0) {//����ڵ�i������ ����Ҫ��������ܺ� /3600 0000 ת��Ϊkwh
                energyByTimeWindow+=(150+1329.33 + 120 + 150+85*degreeValue+80)*100/3600000;
                GHGByTimeWindow+=sub.node_GHG[i]*(150+1329.33 + 120 + 150+85*degreeValue+80)*100/3600000;
            }
        }

        for (int i=0;i<s2v_n.length;i++){
            if (s2v_n[i].req_count!=0){//����ڵ�iΪ�����ڵ� ��Ҫ��������ܺ�
                energyByTimeWindow+=150*(sub.maxcpu[i]-sub.cpu[i])/sub.maxcpu[i]*100/3600000;
                GHGByTimeWindow+=sub.node_GHG[i]*150*(sub.maxcpu[i]-sub.cpu[i])/sub.maxcpu[i]*100/3600000;
            }
        }
        for (int i=0;i<s2v_n.length;i++){//��·�϶����ܺ�
            for (int j=0;j<s2v_n.length;j++){
                energyByTimeWindow+=array[i][j]*(0.18+0.465)*100/3600000;
                GHGByTimeWindow+=(sub.node_GHG[i]+sub.node_GHG[j])/2*array[i][j]*(0.18+0.465)*100/3600000;
                if(array[i][j]!=0){// i j�б�ʹ�� �ڽӾ�����˫��� �ᵼ���ظ����� ����ÿ����·*0.5
                    for(int k=0;k<sub.links;k++){
                        if((i==sub.link[k].from&&j==sub.link[k].to)||(i==sub.link[k].to&&j==sub.link[k].from)){
                            energyByTimeWindow+=(sub.link[k].length/80+(sub.link[k].length%80!=0 ?1:0)+1)*110*0.5*100/3600000;
                            GHGByTimeWindow+=0.5*(sub.node_GHG[i]+sub.node_GHG[j])*(sub.link[k].length/80+(sub.link[k].length%80!=0 ?1:0)+1)*110*0.5*100/3600000;
                        }
                    }
                }
            }
        }

        double endTime=0;
        if (end==reqs.length){
            for(int i=0;i<s2v_n.length;i++){
                for (int j=0;j<s2v_n[i].req_count;j++){
                    if (v2s[s2v_n[i].req.get(j)].maptime+reqs[s2v_n[i].req.get(j)].duration>=endTime){
                        endTime=v2s[s2v_n[i].req.get(j)].maptime+reqs[s2v_n[i].req.get(j)].duration;
                    }
                }
            }
            double duration=endTime-time;
            if (duration<=0){
                duration=0;
            }
            for (int i=0; i<sub.nodes;i++) {
                int valueA=0;//�ڵ�Ķ�
                for (int j = 0; j < sub.nodes; j++) {
                    if (array[i][j] != 0) {
                        valueA++;
                    }
                }
                if (valueA!=0) {//����ڵ�i������ ����Ҫ��������ܺ� /3600 0000 ת��Ϊkwh
                    energyByTimeWindow+=(150+1329.33 + 120 + 150+85*valueA+80)*duration/3600000;
                    GHGByTimeWindow+=sub.node_GHG[i]*(150+1329.33 + 120 + 150+85*valueA+80)*duration/3600000;
                }
            }

            for (int i=0;i<s2v_n.length;i++){
                if (s2v_n[i].req_count!=0){//����ڵ�iΪ�����ڵ� ��Ҫ��������ܺ�
                    energyByTimeWindow+=150*(sub.maxcpu[i]-sub.cpu[i])/sub.maxcpu[i]*duration/3600000;
                    GHGByTimeWindow+=sub.node_GHG[i]*150*(sub.maxcpu[i]-sub.cpu[i])/sub.maxcpu[i]*duration/3600000;
                }
            }
            for (int i=0;i<s2v_n.length;i++){//��·�϶����ܺ�
                for (int j=0;j<s2v_n.length;j++){
                    energyByTimeWindow+=array[i][j]*(0.18+0.465)*duration/3600000;
                    GHGByTimeWindow+=(sub.node_GHG[i]+sub.node_GHG[j])/2*array[i][j]*(0.18+0.465)*duration/3600000;
                    if(array[i][j]!=0){// i j�б�ʹ�� �ڽӾ�����˫��� �ᵼ���ظ����� ����ÿ����·*0.5
                        for(int k=0;k<sub.links;k++){
                            if((i==sub.link[k].from&&j==sub.link[k].to)||(i==sub.link[k].to&&j==sub.link[k].from)){
                                energyByTimeWindow+=(sub.link[k].length/80+(sub.link[k].length%80!=0 ?1:0)+1)*110*0.5*duration/3600000;
                                GHGByTimeWindow+=0.5*(sub.node_GHG[i]+sub.node_GHG[j])*(sub.link[k].length/80+(sub.link[k].length%80!=0 ?1:0)+1)*110*0.5*duration/3600000;
                            }
                        }
                    }
                }
            }
        }


    }
    /**
  �����ܺļ�̼�ŷŸ�
   */
    public  void  CalculateEnergyConsumption1(EOSubstrateNetwork sub, VONRequest reqs[], int end, int time){
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
        for (int i=0; i<sub.nodes;i++) {
            int valueA=0;//�ڵ�Ķ�
            int degreeValue=0;
            for (int j = 0; j < sub.nodes; j++) {
                if (array[i][j] != 0) {
                   valueA++;
//                    for(int k=0;k<sub.links;k++){
//                        if((i==sub.link[k].from&&j==sub.link[k].to)||(i==sub.link[k].to&&j==sub.link[k].from)){
//
//                        }
                   degreeValue+=degreeArray[i][j];
                }
            }

            if (valueA!=0) {//����ڵ�i������ ����Ҫ��������ܺ� /3600 0000 ת��Ϊkwh
                // 1.��ȡ��ǰ����ڵ���ӳ�������ڵ�ļ���
                // 1. ����������飨[0]���ܺģ�[1]�������
                double[] linkResult = new double[2];
                // 2. �������飬�����ڲ����޸���ֵ
                processActivatedNode(sub, reqs, i, linkResult);

                // 3. �������л�ȡ�ۼӽ�������ۼӵ����ܺ�
                double nodeLinkTotalEnergy = linkResult[0];  // ��·�ܺ��ܺ�
                int nodeLinkCount = (int) linkResult[1];     // ��·������ǿת int��
                int sbvtNum = ((int)Math.ceil(nodeLinkCount / 3.0) + 1)*2;
                double sbvtEnergy = 0.5 * nodeLinkTotalEnergy;
                //170->server,560->IPR,->SBVT,135 * degreeValue +150->BV-OXC,25 + 10 * 16->VER
                energyByTimeWindow+=(170 + 560 * sbvtNum + nodeLinkTotalEnergy + sbvtEnergy +  135 * valueA +150 + 3*(25+10 * 16))*100/3600000;
                //energyByTimeWindow+=(150+1329.33 + 120 + 150+85*degreeValue+80)*100/3600000;
                //GHGByTimeWindow+=sub.node_GHG[i]*(150+1329.33 + 120 + 150+85*degreeValue+80)*100/3600000;
                GHGByTimeWindow+=sub.node_GHG[i]*((170 + 560 * sbvtNum + nodeLinkTotalEnergy + sbvtEnergy +  135 * valueA +150 + 3*(25+10 * 16))*100/3600000);

            }
        }

        for (int i=0;i<s2v_n.length;i++){
            if (s2v_n[i].req_count!=0){//����ڵ�iΪ�����ڵ� ��Ҫ��������ܺ�
                //(487-170)*u_i
                energyByTimeWindow+=((487-170)*(sub.maxcpu[i]-sub.cpu[i])/sub.maxcpu[i])*100/3600000;
                //energyByTimeWindow+=150*(sub.maxcpu[i]-sub.cpu[i])/sub.maxcpu[i]*100/3600000;
                GHGByTimeWindow+=sub.node_GHG[i]*((487-170)*(sub.maxcpu[i]-sub.cpu[i])/sub.maxcpu[i])*100/3600000;
                //GHGByTimeWindow+=sub.node_GHG[i]*150*(sub.maxcpu[i]-sub.cpu[i])/sub.maxcpu[i]*100/3600000;
            }
        }
        for (int i=0;i<s2v_n.length;i++){//��·�϶����ܺ�
            for (int j=0;j<s2v_n.length;j++){
               // energyByTimeWindow+=array[i][j]*(0.18+0.465)*100/3600000;
                //energyByTimeWindow+=array[i][j]*(0.075)*100/3600000;
                //GHGByTimeWindow+=(sub.node_GHG[i]+sub.node_GHG[j])/2*array[i][j]*(0.18+0.465)*100/3600000;
               // GHGByTimeWindow+=(sub.node_GHG[i]+sub.node_GHG[j])/2*array[i][j]*(0.075)*100/3600000;
                if(array[i][j]!=0){// i j�б�ʹ�� �ڽӾ�����˫��� �ᵼ���ظ����� ����ÿ����·*0.5
                    for(int k=0;k<sub.links;k++){
                        if((i==sub.link[k].from&&j==sub.link[k].to)||(i==sub.link[k].to&&j==sub.link[k].from)){
                            //EDFA�Ĺ�����30*m+140*n��m�ǹ�Ŵ�����������n�ǵ���Ŵ�����������
                            // һ����Ŵ�����������������Ŵ�������n=2*m  ��·û80km����һ��EDFA
                            double edfaNum = Math.ceil(sub.link[k].length / 80.0) + 1;
                            energyByTimeWindow+= 0.5 * (edfaNum*30+140*2*edfaNum)*100/3600000;//110->40
                            GHGByTimeWindow+=0.5*(sub.node_GHG[i]+sub.node_GHG[j])*(edfaNum*30+140*2*edfaNum) *100/3600000;
//                            energyByTimeWindow+= edfaNum*40*0.5*100/3600000;//110->40
//                            GHGByTimeWindow+=0.5*(sub.node_GHG[i]+sub.node_GHG[j])*edfaNum*40*0.5*100/3600000;
                            //double edfaNum = (sub.link[k].length/80+(sub.link[k].length%80!=0 ?1:0)+1);
                        }
                    }
                }
            }
        }

        double endTime=0;
        if (end==reqs.length){
            for(int i=0;i<s2v_n.length;i++){
                for (int j=0;j<s2v_n[i].req_count;j++){
                    if (v2s[s2v_n[i].req.get(j)].maptime+reqs[s2v_n[i].req.get(j)].duration>=endTime){
                        endTime=v2s[s2v_n[i].req.get(j)].maptime+reqs[s2v_n[i].req.get(j)].duration;
                    }
                }
            }
            double duration=endTime-time;
            if (duration<=0){
                duration=0;
            }
            for (int i=0; i<sub.nodes;i++) {
                int valueA=0;//�ڵ�Ķ�
                for (int j = 0; j < sub.nodes; j++) {
                    if (array[i][j] != 0) {
                        valueA++;
                    }
                }
                if (valueA!=0) {//����ڵ�i������ ����Ҫ��������ܺ� /3600 0000 ת��Ϊkwh
                    energyByTimeWindow += (170 + 560 + 20 + 135 * valueA + 150 + 25 + 10 * 16) * duration / 3600000;
                    GHGByTimeWindow += sub.node_GHG[i] * (170 + 560 + 20 + 135 * valueA + 150 + 25 + 10 * 16) * duration / 3600000;
//                    energyByTimeWindow+=((150+2.5 + 20 + 100))*duration/3600000;//(150+1329.33 + 120 + 150+85*valueA+80)->(150+2.5 + 20 + 100)
//                    GHGByTimeWindow+=sub.node_GHG[i]*((150+2.5 + 20 + 100))*duration/3600000;//(150+1329.33 + 120 + 150+85*valueA+80)->(150+2.5 + 20 + 100)
                }
            }

            for (int i=0;i<s2v_n.length;i++){
                if (s2v_n[i].req_count!=0){//����ڵ�iΪ�����ڵ� ��Ҫ��������ܺ�
                    energyByTimeWindow+=(480-170)*(sub.maxcpu[i]-sub.cpu[i])/sub.maxcpu[i]*duration/3600000;
                    GHGByTimeWindow+=sub.node_GHG[i]*(480-170)*(sub.maxcpu[i]-sub.cpu[i])/sub.maxcpu[i]*duration/3600000;
//                    energyByTimeWindow+=150*(sub.maxcpu[i]-sub.cpu[i])/sub.maxcpu[i]*duration/3600000;
//                    GHGByTimeWindow+=sub.node_GHG[i]*150*(sub.maxcpu[i]-sub.cpu[i])/sub.maxcpu[i]*duration/3600000;
                }
            }
            for (int i=0;i<s2v_n.length;i++){//��·�϶����ܺ�
                for (int j=0;j<s2v_n.length;j++){
//                    energyByTimeWindow+=array[i][j]*(0.075)*duration/3600000;//(0.18+0.465)->0.075
//                    GHGByTimeWindow+=(sub.node_GHG[i]+sub.node_GHG[j])/2*array[i][j]*(0.075)*duration/3600000;//(0.18+0.465)->0.075
                    if(array[i][j]!=0){// i j�б�ʹ�� �ڽӾ�����˫��� �ᵼ���ظ����� ����ÿ����·*0.5
                        for(int k=0;k<sub.links;k++){
                            if((i==sub.link[k].from&&j==sub.link[k].to)||(i==sub.link[k].to&&j==sub.link[k].from)){
                                double edfaNum = Math.ceil(sub.link[k].length / 80.0) + 1;
                                energyByTimeWindow += 0.5 * (edfaNum * 30 + 140 * 2 * edfaNum) * duration / 3600000;//110->40
                                GHGByTimeWindow += 0.5 * (sub.node_GHG[i] + sub.node_GHG[j]) * (edfaNum * 30 + 140 * 2 * edfaNum) * duration / 3600000;
//                              energyByTimeWindow+=(sub.link[k].length/80+(sub.link[k].length%80!=0 ?1:0)+1)*40*0.5*duration/3600000;//110->40
//                              GHGByTimeWindow+=0.5*(sub.node_GHG[i]+sub.node_GHG[j])*(sub.link[k].length/80+(sub.link[k].length%80!=0 ?1:0)+1)*40*0.5*duration/3600000;//110->40
                            }
                        }
                    }
                }
            }
        }


    }


    /*
     * ���ܣ��ͷ�����[0,end]֮�����������ӳ�����Դ
     */
    public void ReleaseAllResourceAmongZeroToEnd(EOSubstrateNetwork sub, VONRequest reqs[], int end, int time) {
        sub.time = time;
        //Release the resources.
        for (int i = 0; i < end; i++) {
            if ((v2s[i].map == Parameters.STATE_MAP_LINK || v2s[i].map == Parameters.STATE_MAP_SUCC) && v2s[i].maptime + reqs[i].duration <= time ) {
                if (Parameters.RecordLogModel) {
                    WriteFilePlus("process.txt", "req[" + i + "] release before");
                    WriteFileOfGraph(sub, "process.txt", true);
                }

                ReleaseResourceFlow(sub, reqs, i); //Release the allocated resources of the ith VN request.
                v2s[i].map = Parameters.STATE_DONE;
                if (Parameters.RecordLogModel) {
                    WriteFilePlus("process.txt", "req[" + i + "] release after");
                    WriteFileOfGraph(sub, "process.txt", true);
                }

                System.out.println("Release Resource:" + i + "------------------------------");

                if (Parameters.DebugModel) {
                    System.out.println("Print sub when req=1");
                    PrintSN(sub);
                }
            }
        }
    }

    //Release the allocated resource of the ith virtual network request.
    public void ReleaseResourceFlow(EOSubstrateNetwork sub, VONRequest reqs[], int index) {
        System.out.println("reqs[" + index + "] release.");
        reqs[index].map = Parameters.STATE_DONE;
        //�ͷ�s2v_n
        for (int i = 0; i < reqs[index].nodes; i++) {
            int snode;
            snode = v2s[index].snode.get(i);
            for (int j = 0; j < s2v_n[snode].req_count; j++) {
                if (s2v_n[snode].req.get(j) == index) {
                    s2v_n[snode].cpu.remove(j);
                    s2v_n[snode].req.remove(j);
                    s2v_n[snode].vnode.remove(j);
                    s2v_n[snode].rest_cpu += reqs[index].cpu[i];
                    s2v_n[snode].req_count--;
                    break;
                }
            }
            //����sub.cpu
            sub.cpu[snode] += reqs[index].cpu[i];
        }
        //�ͷ�s2v_l��sub.slots
        for (int i = 0; i < reqs[index].links; i++) {
            int startSlotNo, slotNum;//�����Ƶ�׿�ʼ�����������Ƶ������
            startSlotNo = (int) v2s[index].startSlotNo.get(i);
            slotNum = (int) v2s[index].slotNum.get(i);

            int snode1, snode2, vnode1, vnode2;
            vnode1 = reqs[index].link[i].from;
            vnode2 = reqs[index].link[i].to;
            //snode1 = v2s[index].snode.get(vnode1);
            snode1 = v2s[index].snode.get(vnode2);
            //snode2 = v2s[index].snode.get(vnode2);

            for (int ii = 0; ii < v2s[index].pathFlow.get(i).len; ii++) {
                snode1 = v2s[index].pathFlow.get(i).link.get(ii);
                snode2 = v2s[index].pathFlow.get(i).link.get(ii + 1);
                int slink;
                slink = sub.linksNo[snode1][snode2];

                //�ͷ�sub.slots
                if (Parameters.RecordLogModel) {
                    String str = "Release slot[" + slink + "][" + startSlotNo + "-" + (startSlotNo + slotNum - 1) + "]" + "\r\n";
                    WriteFilePlus("process.txt", str);
                }
                for (int k = startSlotNo; k < startSlotNo + slotNum && k < sub.slotsNum; k++) {
                    if (Parameters.DebugModel) {
                        System.out.println("ii:" + ii + " k:" + k + " slink:" + slink + " snode1:" + snode1 + " snode2:" + snode2 + " " + sub.linksNo[snode2][snode1]);
                    }
                    if (k < 0) continue;
                    sub.slots[slink][k] = 1;
                    //if(index == 1)
                    //   System.out.println("sub.slots["+slink+"]["+k+"]");
                }
                System.out.println(index + "release.----------");
                for (int j = 0; j < s2v_l[slink].req.size(); j++) {
                    if ((int) s2v_l[slink].req.get(j) == index) {
                        //�ͷ�s2v_l
                        s2v_l[slink].bw.remove(j);
                        s2v_l[slink].vlink.remove(j);
                        s2v_l[slink].req.remove(j);
                    }
                }
                s2v_l[slink].req_count--;
                s2v_l[slink].rest_bw += reqs[index].link[i].bw;

                snode1 = snode2;
            }

        }
    }

    //ͳ����Դ����
    public void CalResDisAfterVNE(EOSubstrateNetwork sub, VONRequest reqs[], int index, double ret[]) {
        int i = index;
        double cpuRate, resRate, slotRate, slots;
        cpuRate = slotRate = resRate = slots = 0;//�ܵ�cpu��ʣ����ܵ�cpu
        for (int j = 0; j < sub.nodes; j++) {
            cpuRate += s2v_n[j].rest_cpu / sub.cpu[j];
        }
        cpuSRate = cpuRate / sub.nodes;

        for (int j = 0; j < sub.links; j++) {
            for (int k = 0; k < Parameters.MaxSlots; k++) {
                if (sub.slots[j][k] == 1) slots++;
            }
        }
        slotRate += slots * 1.0 / (sub.links * Parameters.MaxSlots);
        slotSRate += slotRate;
    }
    
    /*
     * Function:计算能耗和碳排放
     * Create time: 2026/1/28
     * creator: Chen Xiaohua
     */
    public void CalculateEnergyCarbon(EOSubstrateNetwork sub, VONRequest reqs[], int index, int algorithmName, String message) {
        int i = index;
        int form=0;
        int to =0;
        double cpuRate, resRate, slotRate, slots;
        cpuRate = slotRate = resRate = slots = 0;//�ܵ�cpu��ʣ����ܵ�cpu
        if (true) {
            double nodeEnergy = 0;
            double GHG=0;
            if(s2v_l!=null){

            }
            String succ = "after embed succ req " + index;
            String fail = "after embed fail req " + index;
            //20220714gai
            if (message.equals(succ)) {
                v2s[index].maptime = reqs[index].time;

            }

            if (message.equals(succ) || message.equals(fail)) {

                if (v2s[index].map == 9) {

                    int[] node = new int[sub.nodes];
                    int[] nodeMap = new int[sub.nodes];
                    for (int ii = 0; ii < sub.nodes; ii++) {
                        node[ii] = 0;//����ڵ㱻����
                        nodeMap[ii] = 0;//����ڵ㱻ӳ��

                    }
                    int sizeOfPathFlow = v2s[index].pathFlow.size();
                    for (int ii = 0; ii < sizeOfPathFlow; ii++) {
                        node[v2s[index].pathFlow.get(ii).link.get(0)] = 1;
                        node[v2s[index].pathFlow.get(ii).link.get(1)] = 1;
                    }
                    for (int ii = 0; ii < reqs[index].nodes; ii++) {
                        nodeMap[v2s[index].snode.get(ii)] = 1;
                    }
                    int[][] array = new int[sub.nodes][sub.nodes];
                    for (int link=0; link<reqs[index].links;link++){
                        for (int flow =0;flow<v2s[index].pathFlow.get(link).len-1;flow++){
                            array[v2s[index].pathFlow.get(link).link.get(flow)][v2s[index].pathFlow.get(link).link.get(flow+1)]=1;
                            array[v2s[index].pathFlow.get(link).link.get(flow+1)][v2s[index].pathFlow.get(link).link.get(flow)]=1;
                        }
                    }

                    for (int ii = 0; ii < sub.nodes; ii++) {
                        if (node[ii] == 1) {
                            double valueA = 0;
                            double valueB = 1;
                            for (int nodes = 0; nodes < sub.nodes; nodes++){
                                if (array[ii][nodes]==1){
                                    valueA++;
                                }

                            }


                            if ((v2s[index].maptime+reqs[index].duration - (v2s[index].maptime + reqs[index].duration) % 100 + 100 )>sub.cpuTime[ii]) {
                                if (sub.cpuTime[ii] <=v2s[index].maptime) {
                                    sub.cpuOnTime[ii] += (reqs[index].duration - (v2s[index].maptime + reqs[index].duration) % 100 + 100);
                                    sub.cpuOn[ii]=sub.cpuOn[ii]+1;
                                    //  nodeEnergy = nodeEnergy + (1329.33 + 120 + 150) * (reqs[index].duration - (v2s[index].maptime + reqs[index].duration) % 100 + 100);//0.1
                                    //GHG=GHG + (1329.33 + 120 + 150) * (reqs[index].duration - (v2s[index].maptime + reqs[index].duration) % 100 + 100)*sub.node_GHG[ii];

//                                    nodeEnergy = nodeEnergy + (8.33*sub.maxcpu[ii]+1329.33 + 120 + 150) * (reqs[index].duration - (v2s[index].maptime + reqs[index].duration) % 100 + 100);//0.1
//                                    GHG=GHG + (8.33*sub.maxcpu[ii]+1329.33 + 120 + 150) * (reqs[index].duration - (v2s[index].maptime + reqs[index].duration) % 100 + 100)*sub.node_GHG[ii];

                                    nodeEnergy = nodeEnergy + (400+1329.33 + 120 + 150+85*valueA+80*valueB) * (reqs[index].duration - (v2s[index].maptime + reqs[index].duration) % 100 + 100);//0.1
                                    GHG=GHG + (400+1329.33 + 120 + 150+85*valueA+80*valueB) * (reqs[index].duration - (v2s[index].maptime + reqs[index].duration) % 100 + 100)*sub.node_GHG[ii];


                                    energyWithoutdegrees = energyWithoutdegrees+ (400+1329.33 + 120 + 150) * (reqs[index].duration - (v2s[index].maptime + reqs[index].duration) % 100 + 100);//0.1
                                    GHGwithoutdegrees=GHGwithoutdegrees + (400+1329.33 + 120 + 150) * (reqs[index].duration - (v2s[index].maptime + reqs[index].duration) % 100 + 100)*sub.node_GHG[ii];


                                } else {

                                    sub.cpuOnTime[ii] += v2s[index].maptime + reqs[index].duration - (v2s[index].maptime + reqs[index].duration) % 100 + 100 - sub.cpuTime[ii];
                                    //     nodeEnergy = nodeEnergy + (1329.33 + 120 + 150) * (v2s[index].maptime + reqs[index].duration - (v2s[index].maptime + reqs[index].duration) % 100 + 100 - sub.cpuTime[ii]);
                                    // GHG = GHG +(1329.33 + 120 + 150) * (v2s[index].maptime + reqs[index].duration - (v2s[index].maptime + reqs[index].duration) % 100 + 100 - sub.cpuTime[ii])* sub.node_GHG[ii];
//
//                                    nodeEnergy = nodeEnergy + (8.33*sub.maxcpu[ii]+1329.33 + 120 + 150) * (v2s[index].maptime + reqs[index].duration - (v2s[index].maptime + reqs[index].duration) % 100 + 100 - sub.cpuTime[ii]);
//                                    GHG = GHG +(8.33*sub.maxcpu[ii]+1329.33 + 120 + 150) * (v2s[index].maptime + reqs[index].duration - (v2s[index].maptime + reqs[index].duration) % 100 + 100 - sub.cpuTime[ii])* sub.node_GHG[ii];
//
                                    nodeEnergy = nodeEnergy + (400+1329.33 + 120 + 150+85*valueA+80*valueB) * (v2s[index].maptime + reqs[index].duration - (v2s[index].maptime + reqs[index].duration) % 100 + 100 - sub.cpuTime[ii]);
                                    GHG = GHG +(400+1329.33 + 120 + 150+85*valueA+80*valueB) * (v2s[index].maptime + reqs[index].duration - (v2s[index].maptime + reqs[index].duration) % 100 + 100 - sub.cpuTime[ii])* sub.node_GHG[ii];



                                    energyWithoutdegrees = energyWithoutdegrees + (400+1329.33 + 120 + 150) * (v2s[index].maptime + reqs[index].duration - (v2s[index].maptime + reqs[index].duration) % 100 + 100 - sub.cpuTime[ii]);
                                    GHGwithoutdegrees = GHGwithoutdegrees +(400+1329.33 + 120 + 150) * (v2s[index].maptime + reqs[index].duration - (v2s[index].maptime + reqs[index].duration) % 100 + 100 - sub.cpuTime[ii])* sub.node_GHG[ii];

                                }
                                sub.cpuTime[ii] = v2s[index].maptime + reqs[index].duration - (v2s[index].maptime + reqs[index].duration) % 100 + 100;
                            }


//
//							nodeEnergy = nodeEnergy + (1329.33 + 120 + 150) * (reqs[index].duration-(v2s[index].maptime+reqs[index].duration )% 100 + 100);//0.1
                        }
                    }
                    for (int ii = 0; ii < reqs[i].nodes; ii++) {
                        //    nodeEnergy = nodeEnergy + reqs[index].cpu[ii] * 0.465 * (reqs[index].duration - (v2s[index].maptime + reqs[index].duration) % 100 + 100);
                        //GHG =GHG+sub.node_GHG[v2s[index].snode.get(ii)]*reqs[index].cpu[ii] * 0.465 * (reqs[index].duration - (v2s[index].maptime + reqs[index].duration) % 100 + 100);
//
//                        nodeEnergy = nodeEnergy + 600*reqs[index].cpu[ii] /sub.cpu[v2s[index].snode.get(ii)]* sub.cpu[v2s[index].snode.get(ii)]/48 * (reqs[index].duration - (v2s[index].maptime + reqs[index].duration) % 100 + 100);
//                        GHG =GHG+sub.node_GHG[v2s[index].snode.get(ii)] *600*reqs[index].cpu[ii] /sub.cpu[v2s[index].snode.get(ii)]* sub.cpu[v2s[index].snode.get(ii)]/48 * (reqs[index].duration - (v2s[index].maptime + reqs[index].duration) % 100 + 100);
// //double a = 600.00*(reqs[index].cpu[ii] /sub.cpu[v2s[index].snode.get(ii)]);
                        nodeEnergy = nodeEnergy +  (reqs[index].cpu[ii] *600/sub.maxcpu[v2s[index].snode.get(ii)]) * (reqs[index].duration - (v2s[index].maptime + reqs[index].duration) % 100 + 100);
                        GHG =GHG+sub.node_GHG[v2s[index].snode.get(ii)]* 600*(reqs[index].cpu[ii] /sub.maxcpu[v2s[index].snode.get(ii)]) * (reqs[index].duration - (v2s[index].maptime + reqs[index].duration) % 100 + 100);


                        energyWithoutdegrees = energyWithoutdegrees +  (reqs[index].cpu[ii] *600/sub.maxcpu[v2s[index].snode.get(ii)]) * (reqs[index].duration - (v2s[index].maptime + reqs[index].duration) % 100 + 100);
                        GHGwithoutdegrees =GHGwithoutdegrees+sub.node_GHG[v2s[index].snode.get(ii)]* 600*(reqs[index].cpu[ii] /sub.maxcpu[v2s[index].snode.get(ii)]) * (reqs[index].duration - (v2s[index].maptime + reqs[index].duration) % 100 + 100);

                    }
                    for (int ii = 0; ii < sizeOfPathFlow; ii++) {
                        //int sizeOfLink=v2s[index].pathFlow.get(sizeOfPathFlow).link.size();
                        //  nodeEnergy = nodeEnergy + (v2s[index].pathFlow.get(ii).len+1)* v2s[index].pathFlow.get(ii).bw * 0.18 * (reqs[index].duration - (v2s[index].maptime + reqs[index].duration) % 100 + 100);
                        nodeEnergy = nodeEnergy + (v2s[index].pathFlow.get(ii).len+1)* v2s[index].pathFlow.get(ii).bw * (0.18+0.465 )* (reqs[index].duration - (v2s[index].maptime + reqs[index].duration) % 100 + 100);

                        energyWithoutdegrees = energyWithoutdegrees + (v2s[index].pathFlow.get(ii).len+1)* v2s[index].pathFlow.get(ii).bw * (0.18+0.465 )* (reqs[index].duration - (v2s[index].maptime + reqs[index].duration) % 100 + 100);

                        for (int j=0;j<v2s[index].pathFlow.get(ii).len; j++){
                            //  GHG=GHG+sub.node_GHG[v2s[index].pathFlow.get(ii).link.get(j)]*v2s[index].pathFlow.get(ii).bw * 0.18 * (reqs[index].duration - (v2s[index].maptime + reqs[index].duration) % 100 + 100);
                            GHG=GHG+sub.node_GHG[v2s[index].pathFlow.get(ii).link.get(j)]*v2s[index].pathFlow.get(ii).bw * (0.18+0.465 ) * (reqs[index].duration - (v2s[index].maptime + reqs[index].duration) % 100 + 100);
                            GHGwithoutdegrees=GHGwithoutdegrees+sub.node_GHG[v2s[index].pathFlow.get(ii).link.get(j)]*v2s[index].pathFlow.get(ii).bw * (0.18+0.465 ) * (reqs[index].duration - (v2s[index].maptime + reqs[index].duration) % 100 + 100);

                        }
                    }

                    for (int ii =0;ii<sizeOfPathFlow;ii++){
                        for (int j = 0; j< v2s[index].pathFlow.get(ii).len; j++){
                            form=v2s[index].pathFlow.get(ii).link.get(j);
                            to=v2s[index].pathFlow.get(ii).link.get(j+1);
//                            sub.link[sub.linksNo[form][to]].times=sub.link[sub.linksNo[form][to]].times< (v2s[index].maptime+reqs[index].duration - (v2s[index].maptime + reqs[index].duration) % 100 + 100 ) ? (v2s[index].maptime+reqs[index].duration - (v2s[index].maptime + reqs[index].duration) % 100 + 100 ) :sub.link[sub.linksNo[form][to]].times;
                            if (sub.link[sub.linksNo[form][to]].times< (v2s[index].maptime+reqs[index].duration - (v2s[index].maptime + reqs[index].duration) % 100 + 100 )  ){
                                if (v2s[index].maptime >(v2s[index].maptime+reqs[index].duration - (v2s[index].maptime + reqs[index].duration) % 100 + 100 ) ){
                                    nodeEnergy=nodeEnergy+110*(sub.link[sub.linksNo[form][to]].length/80+(sub.link[sub.linksNo[form][to]].length % 80 !=0 ? 1:0 )+1)*((v2s[index].maptime+reqs[index].duration - (v2s[index].maptime + reqs[index].duration) % 100 + 100 ) -v2s[index].maptime) ;
                                    GHG=GHG+(sub.node_GHG[form]+sub.node_GHG[to])*0.5*110*(sub.link[sub.linksNo[form][to]].length/80+(sub.link[sub.linksNo[form][to]].length % 80 !=0 ? 1:0 )+1)*((v2s[index].maptime+reqs[index].duration - (v2s[index].maptime + reqs[index].duration) % 100 + 100 ) -v2s[index].maptime);

                                    energyWithoutdegrees=energyWithoutdegrees+110*(sub.link[sub.linksNo[form][to]].length/80+(sub.link[sub.linksNo[form][to]].length % 80 !=0 ? 1:0 )+1)*((v2s[index].maptime+reqs[index].duration - (v2s[index].maptime + reqs[index].duration) % 100 + 100 ) -v2s[index].maptime) ;
                                    GHGwithoutdegrees=GHGwithoutdegrees+(sub.node_GHG[form]+sub.node_GHG[to])*0.5*110*(sub.link[sub.linksNo[form][to]].length/80+(sub.link[sub.linksNo[form][to]].length % 80 !=0 ? 1:0 )+1)*((v2s[index].maptime+reqs[index].duration - (v2s[index].maptime + reqs[index].duration) % 100 + 100 ) -v2s[index].maptime);



                                }else{
                                    nodeEnergy=nodeEnergy+110*(sub.link[sub.linksNo[form][to]].length/80+(sub.link[sub.linksNo[form][to]].length % 80 !=0 ? 1:0 )+1)*((v2s[index].maptime+reqs[index].duration - (v2s[index].maptime + reqs[index].duration) % 100 + 100 ) -sub.link[sub.linksNo[form][to]].times) ;
                                    GHG =GHG +(sub.node_GHG[form]+sub.node_GHG[to])*0.5*110*(sub.link[sub.linksNo[form][to]].length/80+(sub.link[sub.linksNo[form][to]].length % 80 !=0 ? 1:0 )+1)*((v2s[index].maptime+reqs[index].duration - (v2s[index].maptime + reqs[index].duration) % 100 + 100 ) -sub.link[sub.linksNo[form][to]].times) ;


                                    energyWithoutdegrees=energyWithoutdegrees+110*(sub.link[sub.linksNo[form][to]].length/80+(sub.link[sub.linksNo[form][to]].length % 80 !=0 ? 1:0 )+1)*((v2s[index].maptime+reqs[index].duration - (v2s[index].maptime + reqs[index].duration) % 100 + 100 ) -sub.link[sub.linksNo[form][to]].times) ;
                                    GHGwithoutdegrees =GHGwithoutdegrees +(sub.node_GHG[form]+sub.node_GHG[to])*0.5*110*(sub.link[sub.linksNo[form][to]].length/80+(sub.link[sub.linksNo[form][to]].length % 80 !=0 ? 1:0 )+1)*((v2s[index].maptime+reqs[index].duration - (v2s[index].maptime + reqs[index].duration) % 100 + 100 ) -sub.link[sub.linksNo[form][to]].times) ;

                                }
                                sub.link[sub.linksNo[form][to]].times = (v2s[index].maptime+reqs[index].duration - (v2s[index].maptime + reqs[index].duration) % 100 + 100 );

                            }
                        }
                    }


                    String Energy = "";
                    if (index != 0) {
                        Energy += "\n";
                    }
                    Energy += nodeEnergy;
                    v2s[index].energy = nodeEnergy/3600000;
                    v2s[index].GHG=GHG/3600000;
                    energyWithoutdegrees/=3600000;
                    GHGwithoutdegrees/=3600000;
                }
                double cpuSum = 0;
                double bwSum = 0;
                double revenue = 0;
                int timeWindowsSum = 0;

                timeWindowsSum = reqs[index].time;


                for (int j = 0; j <= index; j++) {
                    if (reqs[j].map == Parameters.STATE_MAP_LINK || reqs[j].map == Parameters.STATE_DONE || reqs[j].map == Parameters.STATE_MAP_SUCC) {

                        for (int jj = 0; jj < reqs[j].nodes; jj++) {
                            cpuSum += reqs[j].cpu[jj];
                        }
                        for (int jj = 0; jj < reqs[j].links; jj++) {
                            bwSum += reqs[j].link[jj].bw;
                        }
                    }
                }
                revenue = (cpuSum + bwSum) / timeWindowsSum;

                v2s[index].revenue = revenue;

            }


            String str = message + "In DebugVNE,\r\n";
            //str = "embed req["+i+"]"+" successfully.\r\n";
            //for(int j=0;j<reqs[i].nodes;j++)
            //	str += j+" is embedded to "+v2s[i].snode.get(j)+".\r\n";
            //str += "cpu[]:";
            for (int j = 0; j < sub.nodes; j++) {
                str += j + "=" + sub.cpu[j] + " ";
                cpuRate += s2v_n[j].rest_cpu / subStatic.cpu[j];
                //cpuSRate += cpuRate;
            }
            if (cpuSRate > 0) cpuSRate = (cpuSRate + cpuRate / sub.nodes) / 2;//cpu��Դ�ֲ���ƽ��ֵΪcpuSRate
            else cpuSRate = cpuRate / sub.nodes;
            str += "\r\nrest cpu[]:";
            for (int j = 0; j < sub.nodes; j++) {
                str += j + "=" + s2v_n[j].rest_cpu + " ";
                //restCPU += s2v_n[j].rest_cpu;
            }

            //WriteFilePlus("process.txt", str);



        }
    }

    //��¼ӳ����
    public void DebugVNE(EOSubstrateNetwork sub, VONRequest reqs[], int index, int algorithmName, String message) {
        int i = index;
        int form=0;
        int to =0;
        double cpuRate, resRate, slotRate, slots;
        cpuRate = slotRate = resRate = slots = 0;//�ܵ�cpu��ʣ����ܵ�cpu
        if (Parameters.RecordLogModel) {
            double nodeEnergy = 0;
            double GHG=0;
//  			for(int ii=0;ii<sub.nodes;ii++){
//  				if(s2v_n[ii].req_count==1){
//  					nodeEnergy=nodeEnergy+1329.33+120+150;
//				}
//  				if (s2v_n[ii].req_count)
//			}
            if(s2v_l!=null){

            }
            String succ = "after embed succ req " + index;
            String fail = "after embed fail req " + index;
            //20220714gai
            if (message.equals(succ)) {
                v2s[index].maptime = reqs[index].time;

            }

            if (message.equals(succ) || message.equals(fail)) {

                if (v2s[index].map == 9) {

                    int[] node = new int[sub.nodes];
                    int[] nodeMap = new int[sub.nodes];
                    for (int ii = 0; ii < sub.nodes; ii++) {
                        node[ii] = 0;//����ڵ㱻����
                        nodeMap[ii] = 0;//����ڵ㱻ӳ��

                    }
                    int sizeOfPathFlow = v2s[index].pathFlow.size();
                    for (int ii = 0; ii < sizeOfPathFlow; ii++) {
                        node[v2s[index].pathFlow.get(ii).link.get(0)] = 1;
                        node[v2s[index].pathFlow.get(ii).link.get(1)] = 1;
                    }
                    for (int ii = 0; ii < reqs[index].nodes; ii++) {
                        nodeMap[v2s[index].snode.get(ii)] = 1;
                    }
//                    for (int ii = 0; ii < sub.nodes; ii++) {
//                        if (node[ii] == 1) {
//                            if (v2s[index].maptime + reqs[index].duration >sub.cpuTime[ii]) {
//                                if (sub.cpuTime[ii] <=sub.time - 100) {
//                                    sub.cpuOnTime[ii] += (reqs[index].duration - (v2s[index].maptime + reqs[index].duration) % 100 + 100);
//                                    sub.cpuOn[ii]=sub.cpuOn[ii]+1;
//                                    nodeEnergy = nodeEnergy + (1329.33 + 120 + 150) * (reqs[index].duration - (v2s[index].maptime + reqs[index].duration) % 100 + 100);//0.1
//                                } else {
//
//                                    sub.cpuOnTime[ii] += v2s[index].maptime + reqs[index].duration - (v2s[index].maptime + reqs[index].duration) % 100 + 100 - sub.cpuTime[ii];
//                                    nodeEnergy = nodeEnergy + (1329.33 + 120 + 150) * v2s[index].maptime + reqs[index].duration - (v2s[index].maptime + reqs[index].duration) % 100 + 100 - sub.cpuTime[ii];
//                                }
//                                sub.cpuTime[ii] = v2s[index].maptime + reqs[index].duration - (v2s[index].maptime + reqs[index].duration) % 100 + 100;
//                            }
//
//
////
////							nodeEnergy = nodeEnergy + (1329.33 + 120 + 150) * (reqs[index].duration-(v2s[index].maptime+reqs[index].duration )% 100 + 100);//0.1
//                        }
//                    }
                    int[][] array = new int[sub.nodes][sub.nodes];
                    for (int link=0; link<reqs[index].links;link++){
                        for (int flow =0;flow<v2s[index].pathFlow.get(link).len-1;flow++){
                            array[v2s[index].pathFlow.get(link).link.get(flow)][v2s[index].pathFlow.get(link).link.get(flow+1)]=1;
                            array[v2s[index].pathFlow.get(link).link.get(flow+1)][v2s[index].pathFlow.get(link).link.get(flow)]=1;
                        }
                    }

                    for (int ii = 0; ii < sub.nodes; ii++) {
                        if (node[ii] == 1) {
                            double valueA = 0;
                            double valueB = 1;
                            for (int nodes = 0; nodes < sub.nodes; nodes++){
                                if (array[ii][nodes]==1){
                                    valueA++;
                                }

                            }


                            if ((v2s[index].maptime+reqs[index].duration - (v2s[index].maptime + reqs[index].duration) % 100 + 100 )>sub.cpuTime[ii]) {
                                if (sub.cpuTime[ii] <=v2s[index].maptime) {
                                    sub.cpuOnTime[ii] += (reqs[index].duration - (v2s[index].maptime + reqs[index].duration) % 100 + 100);
                                    sub.cpuOn[ii]=sub.cpuOn[ii]+1;
                                  //  nodeEnergy = nodeEnergy + (1329.33 + 120 + 150) * (reqs[index].duration - (v2s[index].maptime + reqs[index].duration) % 100 + 100);//0.1
                                    //GHG=GHG + (1329.33 + 120 + 150) * (reqs[index].duration - (v2s[index].maptime + reqs[index].duration) % 100 + 100)*sub.node_GHG[ii];

//                                    nodeEnergy = nodeEnergy + (8.33*sub.maxcpu[ii]+1329.33 + 120 + 150) * (reqs[index].duration - (v2s[index].maptime + reqs[index].duration) % 100 + 100);//0.1
//                                    GHG=GHG + (8.33*sub.maxcpu[ii]+1329.33 + 120 + 150) * (reqs[index].duration - (v2s[index].maptime + reqs[index].duration) % 100 + 100)*sub.node_GHG[ii];

                                    nodeEnergy = nodeEnergy + (400+1329.33 + 120 + 150+85*valueA+80*valueB) * (reqs[index].duration - (v2s[index].maptime + reqs[index].duration) % 100 + 100);//0.1
                                    GHG=GHG + (400+1329.33 + 120 + 150+85*valueA+80*valueB) * (reqs[index].duration - (v2s[index].maptime + reqs[index].duration) % 100 + 100)*sub.node_GHG[ii];


                                    energyWithoutdegrees = energyWithoutdegrees+ (400+1329.33 + 120 + 150) * (reqs[index].duration - (v2s[index].maptime + reqs[index].duration) % 100 + 100);//0.1
                                    GHGwithoutdegrees=GHGwithoutdegrees + (400+1329.33 + 120 + 150) * (reqs[index].duration - (v2s[index].maptime + reqs[index].duration) % 100 + 100)*sub.node_GHG[ii];


                                } else {

                                    sub.cpuOnTime[ii] += v2s[index].maptime + reqs[index].duration - (v2s[index].maptime + reqs[index].duration) % 100 + 100 - sub.cpuTime[ii];
                               //     nodeEnergy = nodeEnergy + (1329.33 + 120 + 150) * (v2s[index].maptime + reqs[index].duration - (v2s[index].maptime + reqs[index].duration) % 100 + 100 - sub.cpuTime[ii]);
                                   // GHG = GHG +(1329.33 + 120 + 150) * (v2s[index].maptime + reqs[index].duration - (v2s[index].maptime + reqs[index].duration) % 100 + 100 - sub.cpuTime[ii])* sub.node_GHG[ii];
//
//                                    nodeEnergy = nodeEnergy + (8.33*sub.maxcpu[ii]+1329.33 + 120 + 150) * (v2s[index].maptime + reqs[index].duration - (v2s[index].maptime + reqs[index].duration) % 100 + 100 - sub.cpuTime[ii]);
//                                    GHG = GHG +(8.33*sub.maxcpu[ii]+1329.33 + 120 + 150) * (v2s[index].maptime + reqs[index].duration - (v2s[index].maptime + reqs[index].duration) % 100 + 100 - sub.cpuTime[ii])* sub.node_GHG[ii];
//
                                    nodeEnergy = nodeEnergy + (400+1329.33 + 120 + 150+85*valueA+80*valueB) * (v2s[index].maptime + reqs[index].duration - (v2s[index].maptime + reqs[index].duration) % 100 + 100 - sub.cpuTime[ii]);
                                    GHG = GHG +(400+1329.33 + 120 + 150+85*valueA+80*valueB) * (v2s[index].maptime + reqs[index].duration - (v2s[index].maptime + reqs[index].duration) % 100 + 100 - sub.cpuTime[ii])* sub.node_GHG[ii];



                                    energyWithoutdegrees = energyWithoutdegrees + (400+1329.33 + 120 + 150) * (v2s[index].maptime + reqs[index].duration - (v2s[index].maptime + reqs[index].duration) % 100 + 100 - sub.cpuTime[ii]);
                                    GHGwithoutdegrees = GHGwithoutdegrees +(400+1329.33 + 120 + 150) * (v2s[index].maptime + reqs[index].duration - (v2s[index].maptime + reqs[index].duration) % 100 + 100 - sub.cpuTime[ii])* sub.node_GHG[ii];

                                }
                                sub.cpuTime[ii] = v2s[index].maptime + reqs[index].duration - (v2s[index].maptime + reqs[index].duration) % 100 + 100;
                            }


//
//							nodeEnergy = nodeEnergy + (1329.33 + 120 + 150) * (reqs[index].duration-(v2s[index].maptime+reqs[index].duration )% 100 + 100);//0.1
                        }
                    }
                    for (int ii = 0; ii < reqs[i].nodes; ii++) {
                    //    nodeEnergy = nodeEnergy + reqs[index].cpu[ii] * 0.465 * (reqs[index].duration - (v2s[index].maptime + reqs[index].duration) % 100 + 100);
                        //GHG =GHG+sub.node_GHG[v2s[index].snode.get(ii)]*reqs[index].cpu[ii] * 0.465 * (reqs[index].duration - (v2s[index].maptime + reqs[index].duration) % 100 + 100);
//
//                        nodeEnergy = nodeEnergy + 600*reqs[index].cpu[ii] /sub.cpu[v2s[index].snode.get(ii)]* sub.cpu[v2s[index].snode.get(ii)]/48 * (reqs[index].duration - (v2s[index].maptime + reqs[index].duration) % 100 + 100);
//                        GHG =GHG+sub.node_GHG[v2s[index].snode.get(ii)] *600*reqs[index].cpu[ii] /sub.cpu[v2s[index].snode.get(ii)]* sub.cpu[v2s[index].snode.get(ii)]/48 * (reqs[index].duration - (v2s[index].maptime + reqs[index].duration) % 100 + 100);
// //double a = 600.00*(reqs[index].cpu[ii] /sub.cpu[v2s[index].snode.get(ii)]);
                        nodeEnergy = nodeEnergy +  (reqs[index].cpu[ii] *600/sub.maxcpu[v2s[index].snode.get(ii)]) * (reqs[index].duration - (v2s[index].maptime + reqs[index].duration) % 100 + 100);
                        GHG =GHG+sub.node_GHG[v2s[index].snode.get(ii)]* 600*(reqs[index].cpu[ii] /sub.maxcpu[v2s[index].snode.get(ii)]) * (reqs[index].duration - (v2s[index].maptime + reqs[index].duration) % 100 + 100);


                        energyWithoutdegrees = energyWithoutdegrees +  (reqs[index].cpu[ii] *600/sub.maxcpu[v2s[index].snode.get(ii)]) * (reqs[index].duration - (v2s[index].maptime + reqs[index].duration) % 100 + 100);
                        GHGwithoutdegrees =GHGwithoutdegrees+sub.node_GHG[v2s[index].snode.get(ii)]* 600*(reqs[index].cpu[ii] /sub.maxcpu[v2s[index].snode.get(ii)]) * (reqs[index].duration - (v2s[index].maptime + reqs[index].duration) % 100 + 100);

                    }
                    for (int ii = 0; ii < sizeOfPathFlow; ii++) {
                        //int sizeOfLink=v2s[index].pathFlow.get(sizeOfPathFlow).link.size();
                      //  nodeEnergy = nodeEnergy + (v2s[index].pathFlow.get(ii).len+1)* v2s[index].pathFlow.get(ii).bw * 0.18 * (reqs[index].duration - (v2s[index].maptime + reqs[index].duration) % 100 + 100);
                        nodeEnergy = nodeEnergy + (v2s[index].pathFlow.get(ii).len+1)* v2s[index].pathFlow.get(ii).bw * (0.18+0.465 )* (reqs[index].duration - (v2s[index].maptime + reqs[index].duration) % 100 + 100);

                        energyWithoutdegrees = energyWithoutdegrees + (v2s[index].pathFlow.get(ii).len+1)* v2s[index].pathFlow.get(ii).bw * (0.18+0.465 )* (reqs[index].duration - (v2s[index].maptime + reqs[index].duration) % 100 + 100);

                        for (int j=0;j<v2s[index].pathFlow.get(ii).len; j++){
                          //  GHG=GHG+sub.node_GHG[v2s[index].pathFlow.get(ii).link.get(j)]*v2s[index].pathFlow.get(ii).bw * 0.18 * (reqs[index].duration - (v2s[index].maptime + reqs[index].duration) % 100 + 100);
                            GHG=GHG+sub.node_GHG[v2s[index].pathFlow.get(ii).link.get(j)]*v2s[index].pathFlow.get(ii).bw * (0.18+0.465 ) * (reqs[index].duration - (v2s[index].maptime + reqs[index].duration) % 100 + 100);
                            GHGwithoutdegrees=GHGwithoutdegrees+sub.node_GHG[v2s[index].pathFlow.get(ii).link.get(j)]*v2s[index].pathFlow.get(ii).bw * (0.18+0.465 ) * (reqs[index].duration - (v2s[index].maptime + reqs[index].duration) % 100 + 100);

                        }
                    }

                    for (int ii =0;ii<sizeOfPathFlow;ii++){
                        for (int j = 0; j< v2s[index].pathFlow.get(ii).len; j++){
                            form=v2s[index].pathFlow.get(ii).link.get(j);
                            to=v2s[index].pathFlow.get(ii).link.get(j+1);
//                            sub.link[sub.linksNo[form][to]].times=sub.link[sub.linksNo[form][to]].times< (v2s[index].maptime+reqs[index].duration - (v2s[index].maptime + reqs[index].duration) % 100 + 100 ) ? (v2s[index].maptime+reqs[index].duration - (v2s[index].maptime + reqs[index].duration) % 100 + 100 ) :sub.link[sub.linksNo[form][to]].times;
                            if (sub.link[sub.linksNo[form][to]].times< (v2s[index].maptime+reqs[index].duration - (v2s[index].maptime + reqs[index].duration) % 100 + 100 )  ){
                                if (v2s[index].maptime >(v2s[index].maptime+reqs[index].duration - (v2s[index].maptime + reqs[index].duration) % 100 + 100 ) ){
                                    nodeEnergy=nodeEnergy+110*(sub.link[sub.linksNo[form][to]].length/80+(sub.link[sub.linksNo[form][to]].length % 80 !=0 ? 1:0 )+1)*((v2s[index].maptime+reqs[index].duration - (v2s[index].maptime + reqs[index].duration) % 100 + 100 ) -v2s[index].maptime) ;
                                    GHG=GHG+(sub.node_GHG[form]+sub.node_GHG[to])*0.5*110*(sub.link[sub.linksNo[form][to]].length/80+(sub.link[sub.linksNo[form][to]].length % 80 !=0 ? 1:0 )+1)*((v2s[index].maptime+reqs[index].duration - (v2s[index].maptime + reqs[index].duration) % 100 + 100 ) -v2s[index].maptime);

                                    energyWithoutdegrees=energyWithoutdegrees+110*(sub.link[sub.linksNo[form][to]].length/80+(sub.link[sub.linksNo[form][to]].length % 80 !=0 ? 1:0 )+1)*((v2s[index].maptime+reqs[index].duration - (v2s[index].maptime + reqs[index].duration) % 100 + 100 ) -v2s[index].maptime) ;
                                    GHGwithoutdegrees=GHGwithoutdegrees+(sub.node_GHG[form]+sub.node_GHG[to])*0.5*110*(sub.link[sub.linksNo[form][to]].length/80+(sub.link[sub.linksNo[form][to]].length % 80 !=0 ? 1:0 )+1)*((v2s[index].maptime+reqs[index].duration - (v2s[index].maptime + reqs[index].duration) % 100 + 100 ) -v2s[index].maptime);



                                }else{
                                    nodeEnergy=nodeEnergy+110*(sub.link[sub.linksNo[form][to]].length/80+(sub.link[sub.linksNo[form][to]].length % 80 !=0 ? 1:0 )+1)*((v2s[index].maptime+reqs[index].duration - (v2s[index].maptime + reqs[index].duration) % 100 + 100 ) -sub.link[sub.linksNo[form][to]].times) ;
                                    GHG =GHG +(sub.node_GHG[form]+sub.node_GHG[to])*0.5*110*(sub.link[sub.linksNo[form][to]].length/80+(sub.link[sub.linksNo[form][to]].length % 80 !=0 ? 1:0 )+1)*((v2s[index].maptime+reqs[index].duration - (v2s[index].maptime + reqs[index].duration) % 100 + 100 ) -sub.link[sub.linksNo[form][to]].times) ;


                                    energyWithoutdegrees=energyWithoutdegrees+110*(sub.link[sub.linksNo[form][to]].length/80+(sub.link[sub.linksNo[form][to]].length % 80 !=0 ? 1:0 )+1)*((v2s[index].maptime+reqs[index].duration - (v2s[index].maptime + reqs[index].duration) % 100 + 100 ) -sub.link[sub.linksNo[form][to]].times) ;
                                    GHGwithoutdegrees =GHGwithoutdegrees +(sub.node_GHG[form]+sub.node_GHG[to])*0.5*110*(sub.link[sub.linksNo[form][to]].length/80+(sub.link[sub.linksNo[form][to]].length % 80 !=0 ? 1:0 )+1)*((v2s[index].maptime+reqs[index].duration - (v2s[index].maptime + reqs[index].duration) % 100 + 100 ) -sub.link[sub.linksNo[form][to]].times) ;

                                }
                                sub.link[sub.linksNo[form][to]].times = (v2s[index].maptime+reqs[index].duration - (v2s[index].maptime + reqs[index].duration) % 100 + 100 );

                            }
                        }
                    }


                    String Energy = "";
                    if (index != 0) {
                        Energy += "\n";
                    }
                    Energy += nodeEnergy;
                    v2s[index].energy = nodeEnergy/3600000;
                    v2s[index].GHG=GHG/3600000;
                    energyWithoutdegrees/=3600000;
                    GHGwithoutdegrees/=3600000;
                }
                double cpuSum = 0;
                double bwSum = 0;
                double revenue = 0;
                int timeWindowsSum = 0;

                timeWindowsSum = reqs[index].time;


                for (int j = 0; j <= index; j++) {
                    if (reqs[j].map == Parameters.STATE_MAP_LINK || reqs[j].map == Parameters.STATE_DONE || reqs[j].map == Parameters.STATE_MAP_SUCC) {

                        for (int jj = 0; jj < reqs[j].nodes; jj++) {
                            cpuSum += reqs[j].cpu[jj];
                        }
                        for (int jj = 0; jj < reqs[j].links; jj++) {
                            bwSum += reqs[j].link[jj].bw;
                        }
                    }
                }
                revenue = (cpuSum + bwSum) / timeWindowsSum;

                v2s[index].revenue = revenue;

            }


            String str = message + "In DebugVNE,\r\n";
            //str = "embed req["+i+"]"+" successfully.\r\n";
            //for(int j=0;j<reqs[i].nodes;j++)
            //	str += j+" is embedded to "+v2s[i].snode.get(j)+".\r\n";
            //str += "cpu[]:";
            for (int j = 0; j < sub.nodes; j++) {
                str += j + "=" + sub.cpu[j] + " ";
                cpuRate += s2v_n[j].rest_cpu / subStatic.cpu[j];
                //cpuSRate += cpuRate;
            }
            if (cpuSRate > 0) cpuSRate = (cpuSRate + cpuRate / sub.nodes) / 2;//cpu��Դ�ֲ���ƽ��ֵΪcpuSRate
            else cpuSRate = cpuRate / sub.nodes;
            str += "\r\nrest cpu[]:";
            for (int j = 0; j < sub.nodes; j++) {
                str += j + "=" + s2v_n[j].rest_cpu + " ";
                //restCPU += s2v_n[j].rest_cpu;
            }

            WriteFilePlus("process.txt", str);

            str = "sub.slots:\r\n";
            for (int j = 0; j < sub.links; j++) {
                str += sub.link[j].from + "-" + sub.link[j].to + "sub.slots:\r\n";
                slots = 0;
                for (int k = 0; k < Parameters.MaxSlots; k++) {
                    str += sub.slots[j][k] + " ";
                    if (sub.slots[j][k] == 1) slots++;
                }
                slotRate += slots * 1.0 / Parameters.MaxSlots;

//				if(slotSRate > 0) slotSRate = (slotSRate + slotRate)/2;
//				else slotSRate += slotRate;
//				str +=".\r\n";
            }
/*********************2022/3/27ritchie��********************************/
            if (slotSRate > 0) slotSRate = (slotSRate + slotRate / sub.links) / 2;
            else slotSRate += slotRate / sub.links;
            str += ".\r\n";
/**********************2022/3/27ritchie��********************************/
            WriteFilePlus("process.txt", str);

            str = "\r\ncpuRate=" + cpuRate + " slotRate=" + slotRate + " resRate=" + (cpuRate + slotRate) / 2 + " ";
            str += " cpuSRate=" + cpuSRate + " slotSRate=" + slotSRate + " resSRate=" + (cpuSRate + slotSRate) / 2 + "\r\n";
            WriteFilePlus("process.txt", str);

            str = "\r\nreq[" + i + "].cpu=\r\n";
            for (int j = 0; j < reqs[i].nodes; j++) {
                str += j + "=" + reqs[i].cpu[j] + " ";
            }
            str += "\r\nreq[" + i + "].link.bw=\r\n";
            for (int j = 0; j < reqs[i].links; j++) {
                str += j + "=" + reqs[i].link[j].bw + " ";
            }
            WriteFilePlus("process.txt", str);
            int energy = 0;

        }
    }

    /**
     * ��дһ����¼����
     */
    public void RecordResultsOfVNE1(EOSubstrateNetwork sub, VONRequest reqs[], long costTime, int algorithmName,
                                   int totalRequests, int successfulRequests, double totalRewardSum, int globalStep, long interval) {
        //����ϵͳ���桢�����ʡ�����ɱ��ȡ�����ʱ��
        int n, rate;
        n = reqs.length;//n���ܵ�������������
        double cpuSum, bwSum, bwSubSum, flotsSubSum, slotSum;
        cpuSum = bwSum = bwSubSum = flotsSubSum = slotSum = 0;
        double duration = 0;
        double refuSlotSum, receSlotSum;
        refuSlotSum = receSlotSum = 0;

        int timeWindowsSum = reqs[n - 1].time;

        int reqNodes = 0;
        int reqLinkNum = 0;
        rate = 0;        //rate��ӳ��ɹ���������������
        for (int i = 0; i < n; i++) {
            if (reqs[i].map == Parameters.STATE_MAP_LINK || reqs[i].map == Parameters.STATE_DONE || reqs[i].map == Parameters.STATE_MAP_SUCC) {
                //if(v2s[i].map == Parameters.STATE_MAP_LINK || v2s[i].map == Parameters.STATE_DONE || v2s[i].map == Parameters.STATE_MAP_SUCC) {
                rate++;
                reqNodes += reqs[i].nodes;
                for (int j = 0; j < reqs[i].nodes; j++) {
                    cpuSum += reqs[i].cpu[j];
                }
                reqLinkNum += reqs[i].links;
                duration += reqs[i].duration;
                for (int j = 0; j < reqs[i].links; j++) {
                    receSlotSum += reqs[i].link[j].speed;
                    bwSum += reqs[i].link[j].bw;
                    slotSum += v2s[i].slotNum.get(j);//reqs[i].slotN;
                    SpathFlow tmLL = v2s[i].pathFlow.get(j);//��i�����������j����·��ӳ��·��
                    for (int k = 0; k < v2s[i].flowLen.get(j); k++) {//��ǰ�ǵ�·����·ӳ��
                        bwSubSum += tmLL.bw * tmLL.len;
                        flotsSubSum += (v2s[i].slotNum.get(j) * tmLL.len);
                    }
                }
                if (Parameters.StaticRecord == true) {
                    String data = i + " " + String.valueOf(slotSum) + " " + String.valueOf(flotsSubSum) + " " + String.valueOf(slotSum * 1.0 / flotsSubSum) + "\r\n";
                    Tools myDowith = new Tools();
                    myDowith.SaveFile("Cost.dat", data, true);
                }
                //DebugVNE(sub,reqs,i,Parameters.CurrentVONEMethod);

            } else {
                for (int j = 0; j < reqs[i].links; j++) {
                    refuSlotSum += reqs[i].link[j].speed;
                }
                System.out.println("The " + i + " VN is failed. The state is " + v2s[i].map + ". The time of mapping is " + v2s[i].tryMapTime + ". The slots are " + (Math.ceil(reqs[i].link[0].bw / 12.5) + Parameters.GuardBand));
            }
        }
        double recieveRate = rate * 1.0 / n; //������������ʣ�������������
        double rvc = (cpuSum + bwSum) / (cpuSum + bwSubSum);//����ɱ���
        double revenue = (cpuSum + bwSum) / timeWindowsSum;    //ϵͳ���棡����������

        System.out.println("slotSum:" + slotSum + " flotsSubSum:" + flotsSubSum + " bwSubSum:" + bwSubSum);

        double slotRVC = slotSum * 1.0 / flotsSubSum;//��·slot����ɱ���

        double rvc1 = (cpuSum + bwSum) / (cpuSum + flotsSubSum * 12.5);//����ɱ��ȣ�������

        //�ڵ���Դ������+��·��Դ������+���߽ڵ�����+����ڵ�����+������·����+
        //d13 = linkSubRateSum / timeWindowsSum;//ÿ��ʱ�䴰�ĵײ���·������
        //d14 = nodeSubRateSum / timeWindowsSum;//ÿ��ʱ�䴰�ĵײ����������
        //double

        double avSubLinkBW = bwSum / sub.links;  //һ���ײ���·�������ѧ����
        double avSubNodeCPU = cpuSum / sub.nodes;  //һ���ײ�ڵ�CPU��Դ����ѧ����
        //.d3 = sub.nodes; //�ײ�ڵ�����
        //d4 = sub.links; //�ײ���·����
        double avVNByOneWindow = (1.0 * n) / timeWindowsSum; //һ��ʱ�䴰�ڵ��������������������

        double avReqLinkNum = 0;
        avReqLinkNum = (1.0 * reqLinkNum) / n; //һ�������������·����

        double avBWReq = 0;
        avBWReq = bwSum / reqLinkNum; //һ��������·����

        double avDuration = 0;
        avDuration = duration / n;  //һ���������������ʱ��

        double avReqNodes = 0;
        avReqNodes = reqNodes / n;  //һ����������Ľڵ�����

        double avCPUSum = 0;
        avCPUSum = cpuSum / reqNodes; //һ������ڵ�CPU��������Դ��

        System.out.println("reqNodes:" + reqNodes + " " + " n:" + n + " reqs[i].nodes:" + reqs[0].nodes);

        Tools myDowith = new Tools();
        String data = "";//";\r\n";

        //��ǰʱ��
        Date now = new Date();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");//���Է�����޸����ڸ�ʽ
        String curDT = dateFormat.format(now);

        // ����DRL���ͳ����Ϣ
        double successRate = 0;
        double avgReward = 0;
        if (totalRequests > 0) {
            successRate = (double) successfulRequests / totalRequests * 100.0;
            avgReward = totalRewardSum / Math.max(1, globalStep); // ƽ����ʱ����
        }

        //�ܺ�
        double energy = 0;
        double GHG =0;
        double[] map = new double[n];
        double acceptance;
        String energyResults = "";
        energyResults += curDT + " " + String.valueOf(algorithmName);
        for (int i = 0; i < n; i++) {
            map[i] = 0;
            if (v2s[i].energy != 0) {
                if (i == 0) {
                    map[i] = 1;
                } else {
                    map[i] = map[i - 1] + 1;
                }
            } else {
                if (i == 0) {
                    map[i] = 0;
                } else {
                    map[i] = map[i - 1];
                }
            }
            energy += v2s[i].energy;
            energyResults = energyResults + " " + energy;
        }
        for (int i = 0; i < n; i++) {
            acceptance = map[i] / (i + 1);
            energyResults = energyResults + " " + acceptance;
        }
        for (int i = 0; i < n; i++) {
            energyResults = energyResults + " " + v2s[i].revenue;
        }
        for (int i = 0; i < n; i++) {
            GHG+=v2s[i].GHG;
            energyResults=energyResults+" "+GHG;
        }
        energyResults += "\n";
        myDowith.SaveFile("energyResults.dat", energyResults, true);


        String subOnTime = "";
        int subOn = 0;
        double onTime=0;

        subOnTime += curDT + " " + String.valueOf(algorithmName)+" " +n+" ";
        for (int i =0;i<sub.nodes;i++){
            subOn+=sub.cpuOn[i];
            onTime+=sub.cpuOnTime[i];
        }
        subOnTime+=subOn+" "+onTime+" ";
        for (int i=0;i<sub.nodes;i++){
            subOnTime=subOnTime+" node"+i+":"+sub.cpuOnTime[i]+" ";

        }
        subOnTime+="\n";

        myDowith.SaveFile("subOnTime.txt", subOnTime, true);

        double EBFM=0;
        //�����ص���Ƭ����
        for (int i = 0; i <sub.links ; i++) {
            double consecutive1 = 0;
            double rowSum = 0;

            for (int j = 0; j < sub.slotsNum; j++) {
                if (sub.slots[i][j] == 1) {
                    consecutive1++;
                } else if (consecutive1 > 0) {
                    rowSum += (consecutive1 / sub.slotsNum) * Math.log(sub.slotsNum / consecutive1);
                    consecutive1 = 0;
                }
            }

            if (consecutive1 > 0) {
                rowSum += (consecutive1 / sub.slotsNum) * Math.log(sub.slotsNum / consecutive1);
            }
            EBFM += rowSum;
        }

        //�㷨+Ŀ¼+������������+ʱ�䴰��С+������+����ɱ���+ϵͳ����+���ѵ�ʱ��+��·slot����ɱ���+����������(refuSlotSum/(refuSlotSum+receSlotSum))
        //��Щ�ֶ�δͳ��(�ڵ���Դ������+��·��Դ������+���߽ڵ�����+����ڵ�����+������·����+)
        //һ���ײ���·�������ѧ���� +һ���ײ�ڵ�CPU��Դ����ѧ����+�ײ�ڵ�����+�ײ���·����+
        //һ��ʱ�䴰�ڵ��������������������+һ�������������·����+һ��������·����+һ���������������ʱ��+
        //һ����������Ľڵ�����+һ������ڵ�CPU��������Դ��

        data = curDT + " " + String.valueOf(algorithmName) + " " + Parameters.vcpuPara + " " + Parameters.vbwPara + " " + VNsFileDir + " " + String.valueOf(n) + " " + sub.slotsNum + " " + String.valueOf(timeWindowsSum) + " �����ʣ�" + String.valueOf(recieveRate) + " " + "��·slot����ɱ��ȣ�"+String.valueOf(slotRVC) + " " +"����ɱ��ȣ�"+ String.valueOf(rvc) + " ϵͳ���棺" + String.valueOf(revenue) + " " + "costTime��"+String.valueOf(costTime) + " " + String.valueOf(refuSlotSum / (refuSlotSum + receSlotSum)) + " ";
        data += String.valueOf(avSubLinkBW) + " " + String.valueOf(avSubNodeCPU) + " " + String.valueOf(sub.nodes) + " " + String.valueOf(sub.links) + " ";
        data += String.valueOf(avVNByOneWindow) + " " + String.valueOf(avReqLinkNum) + " " + String.valueOf(avBWReq) + " " + String.valueOf(avDuration) + " ";
        data += String.valueOf(avReqNodes) + " " + String.valueOf(avCPUSum) + " " + "��·slot����ɱ��ȣ�"+rvc1 + " " +"energy��"+ energy +  " " +"GHG��"+GHG+" "+ EBFM+" "+sub.smallCpu+" "+sub.smallCpu/timeWindowsSum+" CPU��Ƭ�� "+sub.smallCpu/sub.timeWindowsNumber+" ��CPU�� "+sub.largeCpu/sub.timeWindowsNumber+" ƽ��EDFA: "+sub.EBFA/sub.timeWindowsNumber+" ƽ����Ƶ��: "+sub.LargeB/sub.timeWindowsNumber+" ƽ����·������: "+sub.LargeBS/(sub.timeWindowsNumber * sub.links);

        // ���DRLͳ����Ϣ
        data += " ��������: " + totalRequests + " �ɹ�ӳ����: " + successfulRequests + " �ɹ���: " + String.format("%.2f", successRate) + "% �ܽ���: " + String.format("%.4f", totalRewardSum) + " ƽ����ʱ����: " + String.format("%.4f", avgReward) + " ȫ�ֲ���: " + globalStep + " ��ʱ: " + interval + "��";

        data += "\r\n";
        myDowith.SaveFile("results.dat", data, true);

        System.out.println("data:" + data);
        System.out.println("costTime:" + costTime);

        // ���DRL�����Ϣ������̨
        System.out.printf("totalRequests: %d, successfulRequests: %d, successRate: %.2f%%, totalRewardSum: %.4f, avgReward: %.4f, globalStep: %d, interval: %d",
                totalRequests, successfulRequests, successRate, totalRewardSum, avgReward, globalStep, interval);
    }

    //��¼ӳ����
    public void RecordResultsOfVNE(EOSubstrateNetwork sub, VONRequest reqs[], long costTime, int algorithmName) {
        //����ϵͳ���桢�����ʡ�����ɱ��ȡ�����ʱ��
        int n, rate;
        n = reqs.length;//n���ܵ�������������
        double cpuSum, bwSum, bwSubSum, flotsSubSum, slotSum;
        cpuSum = bwSum = bwSubSum = flotsSubSum = slotSum = 0;
        double duration = 0;
        double refuSlotSum, receSlotSum;
        refuSlotSum = receSlotSum = 0;

        int timeWindowsSum = reqs[n - 1].time;

        int reqNodes = 0;
        int reqLinkNum = 0;
        rate = 0;        //rate��ӳ��ɹ���������������
        for (int i = 0; i < n; i++) {
            if (reqs[i].map == Parameters.STATE_MAP_LINK || reqs[i].map == Parameters.STATE_DONE || reqs[i].map == Parameters.STATE_MAP_SUCC) {
                //if(v2s[i].map == Parameters.STATE_MAP_LINK || v2s[i].map == Parameters.STATE_DONE || v2s[i].map == Parameters.STATE_MAP_SUCC) {
                rate++;
                reqNodes += reqs[i].nodes;
                for (int j = 0; j < reqs[i].nodes; j++) {
                    cpuSum += reqs[i].cpu[j];
                }
                reqLinkNum += reqs[i].links;
                duration += reqs[i].duration;
                for (int j = 0; j < reqs[i].links; j++) {
                    receSlotSum += reqs[i].link[j].speed;
                    bwSum += reqs[i].link[j].bw;
                    slotSum += v2s[i].slotNum.get(j);//reqs[i].slotN;
                    SpathFlow tmLL = v2s[i].pathFlow.get(j);//��i�����������j����·��ӳ��·��
                    for (int k = 0; k < v2s[i].flowLen.get(j); k++) {//��ǰ�ǵ�·����·ӳ��
                        bwSubSum += tmLL.bw * tmLL.len;
                        flotsSubSum += (v2s[i].slotNum.get(j) * tmLL.len);
                    }
                }
                if (Parameters.StaticRecord == true) {
                    String data = i + " " + String.valueOf(slotSum) + " " + String.valueOf(flotsSubSum) + " " + String.valueOf(slotSum * 1.0 / flotsSubSum) + "\r\n";
                    Tools myDowith = new Tools();
                    myDowith.SaveFile("Cost.dat", data, true);
                }
                //DebugVNE(sub,reqs,i,Parameters.CurrentVONEMethod);

            } else {
                for (int j = 0; j < reqs[i].links; j++) {
                    refuSlotSum += reqs[i].link[j].speed;
                }
                System.out.println("The " + i + " VN is failed. The state is " + v2s[i].map + ". The time of mapping is " + v2s[i].tryMapTime + ". The slots are " + (Math.ceil(reqs[i].link[0].bw / 12.5) + Parameters.GuardBand));
            }
        }
        double recieveRate = rate * 1.0 / n; //������������ʣ�������������
        double rvc = (cpuSum + bwSum) / (cpuSum + bwSubSum);//����ɱ���
        double revenue = (cpuSum + bwSum) / timeWindowsSum;    //ϵͳ���棡����������

        System.out.println("slotSum:" + slotSum + " flotsSubSum:" + flotsSubSum + " bwSubSum:" + bwSubSum);

        double slotRVC = slotSum * 1.0 / flotsSubSum;//��·slot����ɱ���

        double rvc1 = (cpuSum + bwSum) / (cpuSum + flotsSubSum * 12.5);//����ɱ��ȣ�������

        //�ڵ���Դ������+��·��Դ������+���߽ڵ�����+����ڵ�����+������·����+
        //d13 = linkSubRateSum / timeWindowsSum;//ÿ��ʱ�䴰�ĵײ���·������
        //d14 = nodeSubRateSum / timeWindowsSum;//ÿ��ʱ�䴰�ĵײ����������
        //double

        double avSubLinkBW = bwSum / sub.links;  //һ���ײ���·�������ѧ����
        double avSubNodeCPU = cpuSum / sub.nodes;  //һ���ײ�ڵ�CPU��Դ����ѧ����
        //.d3 = sub.nodes; //�ײ�ڵ�����
        //d4 = sub.links; //�ײ���·����
        double avVNByOneWindow = (1.0 * n) / timeWindowsSum; //һ��ʱ�䴰�ڵ��������������������

        double avReqLinkNum = 0;
        avReqLinkNum = (1.0 * reqLinkNum) / n; //һ�������������·����

        double avBWReq = 0;
        avBWReq = bwSum / reqLinkNum; //һ��������·����

        double avDuration = 0;
        avDuration = duration / n;  //һ���������������ʱ��

        double avReqNodes = 0;
        avReqNodes = reqNodes / n;  //һ����������Ľڵ�����

        double avCPUSum = 0;
        avCPUSum = cpuSum / reqNodes; //һ������ڵ�CPU��������Դ��

        System.out.println("reqNodes:" + reqNodes + " " + " n:" + n + " reqs[i].nodes:" + reqs[0].nodes);

        Tools myDowith = new Tools();
        String data = "";//";\r\n";

        //��ǰʱ��
        Date now = new Date();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");//���Է�����޸����ڸ�ʽ
        String curDT = dateFormat.format(now);

        //�ܺ�
        double energy = 0;
        double GHG =0;
        double[] map = new double[n];
        double acceptance;
        String energyResults = "";
        energyResults += curDT + " " + String.valueOf(algorithmName);
        for (int i = 0; i < n; i++) {
            map[i] = 0;
            if (v2s[i].energy != 0) {
                if (i == 0) {
                    map[i] = 1;
                } else {
                    map[i] = map[i - 1] + 1;
                }
            } else {
                if (i == 0) {
                    map[i] = 0;
                } else {
                    map[i] = map[i - 1];
                }
            }
            energy += v2s[i].energy;
            energyResults = energyResults + " " + energy;
        }
        for (int i = 0; i < n; i++) {
            acceptance = map[i] / (i + 1);
            energyResults = energyResults + " " + acceptance;
        }
        for (int i = 0; i < n; i++) {
            energyResults = energyResults + " " + v2s[i].revenue;
        }
        for (int i = 0; i < n; i++) {
            GHG+=v2s[i].GHG;
            energyResults=energyResults+" "+GHG;
        }
        energyResults += "\n";
        myDowith.SaveFile("energyResults.dat", energyResults, true);


        String subOnTime = "";
        int subOn = 0;
        double onTime=0;

        subOnTime += curDT + " " + String.valueOf(algorithmName)+" " +n+" ";
        for (int i =0;i<sub.nodes;i++){
            subOn+=sub.cpuOn[i];
            onTime+=sub.cpuOnTime[i];
        }
        subOnTime+=subOn+" "+onTime+" ";
        for (int i=0;i<sub.nodes;i++){
            subOnTime=subOnTime+" node"+i+":"+sub.cpuOnTime[i]+" ";

        }
        subOnTime+="\n";

        myDowith.SaveFile("subOnTime.txt", subOnTime, true);

//		BufferedReader reader = null;
//		int energy=0;
//		double sumEnergy=0;
//		String energyResults="";
//		energyResults+= curDT+ " " + String.valueOf(algorithmName) ;
//		try {
//			System.out.println("����Ϊ��λ��ȡ�ļ����ݣ�һ�ζ�һ���У�");
////			reader = new BufferedReader(new FileReader("glpsolRSA.o"));
//			reader = new BufferedReader(new FileReader("energy.txt"));
//			String tempString = null;
//
//			while ((tempString = reader.readLine()) != null){
//				energy=energy+Double.valueOf(tempString).intValue();
//				energyResults=energyResults+" "+energy;
//			}
//			energyResults +="\n";
//			myDowith.SaveFile("energyResults.dat", energyResults, true);
////			Tools sumOfEnergy= new Tools();
////			String myData="sum of energy:"+energy;
////			sumOfEnergy.SaveFile("Energy.dat",myData,true);
//
//
//		} catch (IOException e) {
//
//			//e.printStackTrace();
//		} finally {
//			if (reader != null) {
//				try {
//					reader.close();
//				} catch (IOException e1) {
//
//				}
//			}
//		}
        double EBFM=0;
        //�����ص���Ƭ����
        for (int i = 0; i <sub.links ; i++) {
            double consecutive1 = 0;
            double rowSum = 0;

            for (int j = 0; j < sub.slotsNum; j++) {
                if (sub.slots[i][j] == 1) {
                    consecutive1++;
                } else if (consecutive1 > 0) {
                    rowSum += (consecutive1 / sub.slotsNum) * Math.log(sub.slotsNum / consecutive1);
                    consecutive1 = 0;
                }
            }

            if (consecutive1 > 0) {
                rowSum += (consecutive1 / sub.slotsNum) * Math.log(sub.slotsNum / consecutive1);
            }
            EBFM += rowSum;
        }

        //�㷨+Ŀ¼+������������+ʱ�䴰��С+������+����ɱ���+ϵͳ����+���ѵ�ʱ��+��·slot����ɱ���+����������(refuSlotSum/(refuSlotSum+receSlotSum))
        //��Щ�ֶ�δͳ��(�ڵ���Դ������+��·��Դ������+���߽ڵ�����+����ڵ�����+������·����+)
        //һ���ײ���·�������ѧ���� +һ���ײ�ڵ�CPU��Դ����ѧ����+�ײ�ڵ�����+�ײ���·����+
        //һ��ʱ�䴰�ڵ��������������������+һ�������������·����+һ��������·����+һ���������������ʱ��+
        //һ����������Ľڵ�����+һ������ڵ�CPU��������Դ��

        data = curDT + " " + String.valueOf(algorithmName) + " " + Parameters.vcpuPara + " " + Parameters.vbwPara + " " + VNsFileDir + " " + String.valueOf(n) + " " + sub.slotsNum + " " + String.valueOf(timeWindowsSum) + " recieveRate:" + String.valueOf(recieveRate) + " " + "energy:"+ energy +   "  " + "GHG:"+GHG+ " " + "slotRVC:"+String.valueOf(slotRVC) + " " +" rvc:"+ String.valueOf(rvc) + " revenue:" + String.valueOf(revenue) + " " + "costTime:"+String.valueOf(costTime) + " " + String.valueOf(refuSlotSum / (refuSlotSum + receSlotSum)) + " ";
        data += String.valueOf(avSubLinkBW) + " " + String.valueOf(avSubNodeCPU) + " " + String.valueOf(sub.nodes) + " " + String.valueOf(sub.links) + " ";
        data += String.valueOf(avVNByOneWindow) + " " + String.valueOf(avReqLinkNum) + " " + String.valueOf(avBWReq) + " " + String.valueOf(avDuration) + " ";
        data += String.valueOf(avReqNodes) + " " + String.valueOf(avCPUSum) + " " + " rvc1:"+rvc1 + " "+ EBFM+" "+sub.smallCpu+" "+sub.smallCpu/timeWindowsSum+" smallCpu:"+sub.smallCpu/sub.timeWindowsNumber+"  largeCpu:"+sub.largeCpu/sub.timeWindowsNumber+" EBFA:"+sub.EBFA/sub.timeWindowsNumber+" LargeB:"+sub.LargeB/sub.timeWindowsNumber+" LargeBS:"+sub.LargeBS/(sub.timeWindowsNumber * sub.links)+"\r\n";
        myDowith.SaveFile("results.dat", data, true);

        System.out.println("data:" + data);
        System.out.println("costTime:" + costTime);
    }

    /*
     * ���ƣ�WriteFilePlus(String fileName,String content);
     */
    public void WriteFilePlus(String fileName, String content) {
        Tools myDowith = new Tools();
        Date now = new Date();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");//���Է�����޸����ڸ�ʽ
        String curDT = dateFormat.format(now);
        content = curDT + content + "\r\n";
        myDowith.SaveFile(fileName, content, true);
    }

    /*
     * ���ƣ�WriteFileOfGraph(sub,graphData);
     */
    public void WriteFileOfGraph(EOSubstrateNetwork sub, String fileName) {
        Tools myDowith = new Tools();
        String data = "";
        data += sub.nodes + "\r\n";
        for (int i = 0; i < sub.links; i++) {
            //data += sub.link[i].from + " " + sub.link[i].to + " " + sub.link[i].length + "\r\n";
            //data += sub.link[i].to + " " + sub.link[i].from + " " + sub.link[i].length + "\r\n";
            data += sub.link[i].from + " " + sub.link[i].to + " " + 1 + "\r\n";
            data += sub.link[i].to + " " + sub.link[i].from + " " + 1 + "\r\n";
        }
        myDowith.SaveFile(fileName, data, false);
    }

    /*
     * ���ƣ�WriteFileOfGraph(sub,graphData);
     */
    public void WriteFileOfGraph(EOSubstrateNetwork sub, String fileName, boolean append) {
        Tools myDowith = new Tools();
        String data = "";
        data += sub.nodes + "\r\n";
        for (int i = 0; i < sub.links; i++) {
            data += "link[" + i + "]:";
            for (int j = 0; j < Parameters.MaxSlots; j++) {
                data += sub.slots[i][j] + " ";
            }
            data += "\r\n";
            //data += sub.link[i].from + " " + sub.link[i].to + " " + sub.link[i].length + "\r\n";
            //data += sub.link[i].to + " " + sub.link[i].from + " " + sub.link[i].length + "\r\n";
        }
        data += "\r\n";
        myDowith.SaveFile(fileName, data, append);
        data = "nodes.cpu:";
        for (int i = 0; i < sub.nodes; i++) {
            data += sub.cpu[i] + " ";
        }
        data += "\r\n";
        myDowith.SaveFile(fileName, data, append);
    }

    /*
     * ���ƣ�WriteFileOfGraph(sub,graphData);
     */
    public void WriteFileOfGraph1(WeightedDirectedGraph sub, String fileName) {
        Tools myDowith = new Tools();
        String data = "";
        data += sub.nVerts + "\r\n";
        for (int i = 0; i < sub.nVerts; i++) {
            for (int j = 0; j < sub.nVerts; j++) {
                if (i != j && sub.adjMat[i][j] < Parameters.MAX_VALUE_INT && sub.adjMat[i][j] < Parameters.MAX_VALUE_DOUBLE)
                    data += i + " " + j + " " + sub.adjMat[i][j] + "\r\n";
            }
        }
        myDowith.SaveFile(fileName, data, false);
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
    public boolean FindVONEOptimalSolutionPlusWangY(AuxiliaryGraph auxGraph, int retNodeE[], int retLinkE[], int retSlotSE[], int retSlotEE[], int retSlotBE[], int retLinkMD[][]) {
        BufferedReader reader = null;

        int keySNode1 = -1, keySNode2 = -1;

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
                    tempString = tempString.replace("Objective:  shPath = ", "");
                    tempString = tempString.replace("(MINimum)", "");
                    tempString = tempString.trim();
                    //minLength = Integer.parseInt(tempString);
                    //hashResolve = new Hashtable(minLength,(float)1.0);//����hash
                }
                if (line > 6 && tempString.indexOf(" y[") != -1) {//˵���ҵ������Ž��x����
                    //ȥ��ǰ��Ĳ��֣�3 x[0,2]       *              1             0             1
                    //�Կո�ָ���ȡ��һ������
                    String tmpStr = "";
                    //System.out.println("line " + line + ": " + tempString);

                    //String tempString1 = reader.readLine();
                    //System.out.println(tempString1);
                    String tempString1 = tempString.trim();

                    tmpStr = tempString1.substring(tempString1.indexOf("*") + 1);
                    tmpStr = tmpStr.trim();
                    //System.out.println("line " + line + ": " + tmpStr);

                    tmpStr = tmpStr.substring(0, tmpStr.indexOf(" "));
                    //System.out.println("line " + line + ": " + tmpStr);
                    if (Integer.parseInt(tmpStr) == 1) {//˵���ҵ���һ����
                        //�õ�һ���⸳ֵ��tmpStr������x[0,2]
                        tempString = tempString.trim();
                        tmpStr = tempString.substring(tempString.indexOf(" ") + 1);

                        //x[0,6,0,1,7,6],����s��keyVNode1ӳ��Ľڵ㣬t��keyVNode2ӳ��Ľڵ�
                        //y[0,0]

                        keySNode1 = Integer.parseInt(tmpStr.substring(tmpStr.indexOf("[") + 1, tmpStr.indexOf(",")));
                        //System.out.println("keyNode1:"+keySNode1);
                        tmpStr = tmpStr.substring(tmpStr.indexOf(",") + 1);
                        keySNode2 = Integer.parseInt(tmpStr.substring(0, tmpStr.indexOf("]")));
                        //System.out.println("keyNode2:"+keySNode2);
                        //int retNodeE[],int retLinkE[],int retSlotSE[],int retSlotEE[],int retSlotBE[]
                        //hashVLinkToPath.put(keySNode1,keySNode2);//�Ᵽ����hash����
                        retLinkE[keySNode1] = keySNode2;
                        System.out.println("keySNode1:" + keySNode1 + " keySNode2:" + keySNode2);
                        //retLinkMD[keySNode1][keySNode2] = 1;
                    }
                } else if (line > 6 && tempString.indexOf(" M[") != -1) {//˵���ҵ������Ž��x����
                    String tmpStr = "";
                    //System.out.println("line " + line + ": " + tempString);
                    //String tempString1 = reader.readLine();
                    String tempString1 = tempString.trim();

                    tmpStr = tempString1.substring(tempString1.indexOf("*") + 1);
                    tmpStr = tmpStr.trim();
                    //System.out.println("line " + line + ": " + tmpStr);

                    tmpStr = tmpStr.substring(0, tmpStr.indexOf(" "));
                    //System.out.println("line " + line + ": " + tmpStr);
                    if (Integer.parseInt(tmpStr) == 1) {//˵���ҵ���һ����
                        //�õ�һ���⸳ֵ��tmpStr������x[0,2]
                        //var f{(i,j) in E,(m,n) in Ev,s in Vf,t in Vf,k in MSet}, binary;
                        tempString = tempString.trim();
                        tmpStr = tempString.substring(tempString.indexOf(" ") + 1);//ȥ��ǰ����к�
                        //System.out.println("line " + line + ": " + tmpStr);
                        //tmpStr = tmpStr.substring(0,tmpStr.indexOf(" "));		//�õ�f[i,j,m,n,s,t,k]
                        //System.out.println("line " + line + ": " + tmpStr);
                        int keyNode1 = -1;
                        //keyNode1 = Integer.parseInt(tmpStr.substring(tmpStr.indexOf("[")+1, tmpStr.indexOf(",")));//�õ�f[i,j,m,n,s,t,k]��i
                        //System.out.println("keyNode1:"+keyNode1);
                        //M[5,1]
                        keySNode1 = Integer.parseInt(tmpStr.substring(tmpStr.indexOf("[") + 1, tmpStr.indexOf(",")));
                        //System.out.println("keyNode1:"+keySNode1);
                        tmpStr = tmpStr.substring(tmpStr.indexOf(",") + 1);
                        keySNode2 = Integer.parseInt(tmpStr.substring(0, tmpStr.indexOf("]")));

                        //hashVNodeToSNode.put(keySNode1,keySNode2);//�Ᵽ����hash����
                        //int retNodeE[],int retLinkE[],int retSlotSE[],int retSlotEE[],int retSlotBE[]
                        for (int i = 0; i < auxGraph.virtualNodes.length; i++) {
                            if (auxGraph.virtualNodes[i] == keySNode1)
                                retNodeE[i] = keySNode2;
                        }
                    }
                } else if (line > 6 && tempString.indexOf(" E[") != -1) {//˵���ҵ������Ž��x����
                    String tmpStr = "";
                    //System.out.println("line " + line + ": " + tempString);
                    //String tempString1 = reader.readLine();
                    tempString = tempString.trim();
                    tmpStr = tempString.substring(tempString.indexOf(" ") + 1);//ȥ��ǰ����к�

                    //E[0]
                    keySNode1 = Integer.parseInt(tmpStr.substring(tmpStr.indexOf("[") + 1, tmpStr.indexOf("]")));
                    //System.out.println("keyNode1:"+keySNode1);
                    //System.out.println("tempString:"+tempString+" tmpStr:"+tmpStr);
                    tmpStr = tempString.substring(tempString.indexOf("*") + 1).trim();//ȥ��*ǰ�����Ϣ
                    //System.out.println("tmpStr:"+tmpStr);
                    if (tmpStr.indexOf(" ") != -1) {
                        tmpStr = tmpStr.substring(0, tmpStr.indexOf(" ")).trim();
                    }
                    //System.out.println("tmpStr:"+tmpStr);
                    keySNode2 = Integer.parseInt(tmpStr);

                    //hashESlot.put(keySNode1,keySNode2);//�Ᵽ����hash����
                    //int retNodeE[],int retLinkE[],int retSlotSE[],int retSlotEE[],int retSlotBE[]
                    retSlotEE[keySNode1] = keySNode2;
                } else if (line > 6 && tempString.indexOf(" S[") != -1) {//˵���ҵ������Ž��x����
                    String tmpStr = "";
                    //System.out.println("line " + line + ": " + tempString);
                    //String tempString1 = reader.readLine();
                    tempString = tempString.trim();
                    tmpStr = tempString.substring(tempString.indexOf(" ") + 1);//ȥ��ǰ����к�

                    //E[0]
                    keySNode1 = Integer.parseInt(tmpStr.substring(tmpStr.indexOf("[") + 1, tmpStr.indexOf("]")));
                    //System.out.println("keyNode1:"+keySNode1);

                    tmpStr = tempString.substring(tempString.indexOf("*") + 1).trim();//ȥ��*ǰ�����Ϣ
                    //System.out.println("tmpStr:"+tmpStr);
                    if (tmpStr.indexOf(" ") != -1) {
                        tmpStr = tmpStr.substring(0, tmpStr.indexOf(" ")).trim();
                    }
                    keySNode2 = Integer.parseInt(tmpStr);

                    //hashSSlot.put(keySNode1,keySNode2);//�Ᵽ����hash����
                    //int retNodeE[],int retLinkE[],int retSlotSE[],int retSlotEE[],int retSlotBE[]
                    retSlotSE[keySNode1] = keySNode2;
                } else if (line > 6 && tempString.indexOf(" B[") != -1) {//˵���ҵ������Ž��x����
                    String tmpStr = "";
                    //System.out.println("line " + line + ": " + tempString);
                    //String tempString1 = reader.readLine();
                    tempString = tempString.trim();
                    tmpStr = tempString.substring(tempString.indexOf(" ") + 1).trim();//ȥ��ǰ����к�

                    //E[0]
                    keySNode1 = Integer.parseInt(tmpStr.substring(tmpStr.indexOf("[") + 1, tmpStr.indexOf("]")));
                    //System.out.println("keyNode1:"+keySNode1);

                    tmpStr = tempString.substring(tempString.indexOf("*") + 1).trim();//ȥ��*ǰ�����Ϣ
                    //System.out.println("tmpStr:"+tmpStr);
                    if (tmpStr.indexOf(" ") != -1) {
                        tmpStr = tmpStr.substring(0, tmpStr.indexOf(" ")).trim();
                    }
                    keySNode2 = Integer.parseInt(tmpStr);

                    //hashBSlot.put(keySNode1,keySNode2);//�Ᵽ����hash����
                    //int retNodeE[],int retLinkE[],int retSlotSE[],int retSlotEE[],int retSlotBE[]
                    retSlotBE[keySNode1] = keySNode2;
                }

                line++;
            }
            reader.close();
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
        return true;
    }

    /*
     * ���Ƶ�ײ��Ƿ��ͻ
     */
    public boolean CheckLinksMapByMIPWangYPlus(EOSubstrateNetwork sub, VONRequest reqs[], int index, int retSlotSE[], int retSlotEE[], int retLinkE[], DistanceParent kShortestPath[][][], int pathEff[], int pathNo[][], int virtualNodes[], int retNodeE[]) {
        //0����ӳ�������ݽṹ�У�����p��ret���ݽṹ;
        int p[][] = new int[reqs[index].links][sub.nodes];
        int ret[][] = new int[reqs[index].links][2];
        for (int i = 0; i < reqs[index].links; i++) {
            ret[i][0] = retSlotSE[i];
            ret[i][1] = retSlotEE[i];//+1;
        }
        for (int i = 0; i < reqs[index].links; i++) {
            for (int j = 0; j < sub.nodes; j++) {
                p[i][j] = -1;//��ʼ��
            }
        }
        //����p
        CreateShortestPathFromKPaths(reqs, index, kShortestPath, virtualNodes, retLinkE, pathEff, p);


        //1���ڵ�ӳ�䣬����sub.slots;
        boolean check = true;
        for (int i = 0; i < reqs[index].links; i++) {
            int snode1, snode2, vnode1, vnode2;
            vnode1 = reqs[index].link[i].to;
            vnode2 = reqs[index].link[i].from;
            snode1 = retNodeE[vnode1];//v2s[index].snode.get(vnode1);
            snode2 = retNodeE[vnode2];//v2s[index].snode.get(vnode2);
            check = CheckSubSlots(sub, snode1, snode2, ret[i], p[i]);
            if (check == false) return false;
        }
        return true;
    }

    /*����:��·ӳ��
     * ����:retSlotSE[]:��ʼ����
     * retSlotEE[]:��������
     * retLinkE[]:��·ӳ����
     * kShortestPath[][][]:���·������
     * pathEff[]:ÿ������·��ӳ�����Ч·������
     * pathNo[][]:·���ı��
     * virtualNodes[]:����ڵ�ӳ�����չͼ�ڵ���
     * retNodeE[]:�ڵ�ӳ��
     * ������:������
     * ����ʱ��:2017-09-28
     */
    public void AddLinksMapByMIPWangYPlus(EOSubstrateNetwork sub, VONRequest reqs[], int index, int retSlotSE[], int retSlotEE[], int retLinkE[], DistanceParent kShortestPath[][][], int pathEff[], int pathNo[][], int virtualNodes[], int retNodeE[]) {
        //0����ӳ�������ݽṹ�У�����p��ret���ݽṹ;
        int p[][] = new int[reqs[index].links][sub.nodes];
        int ret[][] = new int[reqs[index].links][2];
        for (int i = 0; i < reqs[index].links; i++) {
            ret[i][0] = retSlotSE[i];
            ret[i][1] = retSlotEE[i];//+1;
        }
        for (int i = 0; i < reqs[index].links; i++) {
            for (int j = 0; j < sub.nodes; j++) {
                p[i][j] = -1;//��ʼ��
            }
        }
        //����p
        CreateShortestPathFromKPaths(reqs, index, kShortestPath, virtualNodes, retLinkE, pathEff, p);


        //1���ڵ�ӳ�䣬����sub.slots;
        for (int i = 0; i < reqs[index].links; i++) {
            int snode1, snode2, vnode1, vnode2;
            vnode1 = reqs[index].link[i].to;
            vnode2 = reqs[index].link[i].from;
            snode1 = v2s[index].snode.get(vnode1);
            snode2 = v2s[index].snode.get(vnode2);
            UpdateSub(sub, snode1, snode2, ret[i], p[i]);
        }

        //2������S2VLink s2v_l[]
        int snodeMid1, snodeMid, sNode1, req_count;
        for (int i = 0; i < reqs[index].links; i++) {
            snodeMid1 = reqs[index].link[i].to;
            sNode1 = reqs[index].link[i].from;
            snodeMid1 = v2s[index].snode.get(snodeMid1);
            sNode1 = v2s[index].snode.get(sNode1);
            while (p[i][snodeMid1] != -1) {
                snodeMid = p[i][snodeMid1];
                req_count = s2v_l[sub.linksNo[snodeMid][snodeMid1]].req_count;
                s2v_l[sub.linksNo[snodeMid][snodeMid1]].req.add(req_count, index);
                s2v_l[sub.linksNo[snodeMid][snodeMid1]].bw.add(req_count, reqs[index].link[i].bw);
                s2v_l[sub.linksNo[snodeMid][snodeMid1]].vlink.add(req_count, i);
                s2v_l[sub.linksNo[snodeMid][snodeMid1]].rest_bw -= reqs[index].link[i].bw;
                s2v_l[sub.linksNo[snodeMid][snodeMid1]].req_count++;

                snodeMid1 = snodeMid;
                if (snodeMid1 == sNode1) break;//�ӻ�㵽Դ���·���Ѿ�����
            }
        }

        //3������v2s[]����·ӳ����Ϣ
        int pathLength = 0;

        for (int i = 0; i < reqs[index].links; i++) {
            snodeMid1 = reqs[index].link[i].to;
            sNode1 = reqs[index].link[i].from;
            snodeMid1 = v2s[index].snode.get(snodeMid1);
            System.out.println("snodeMid1:" + snodeMid1);
            sNode1 = v2s[index].snode.get(sNode1);
            pathLength = 0;
            LinkedList<Integer> link = new LinkedList<Integer>();
            while (p[i][snodeMid1] != -1) {
                snodeMid = p[i][snodeMid1];
                link.add(pathLength, snodeMid1);
                pathLength++;    //·������
                snodeMid1 = snodeMid;
                //if(snodeMid1 == sNode1) break;//�ӻ�㵽Դ���·���Ѿ�����
            }
            if (pathLength == 0) {
                snodeMid1 = reqs[index].link[i].from;
                sNode1 = reqs[index].link[i].to;
                snodeMid1 = v2s[index].snode.get(snodeMid1);
                System.out.println("snodeMid1:" + snodeMid1);
                sNode1 = v2s[index].snode.get(sNode1);
                pathLength = 0;
                while (p[i][snodeMid1] != -1) {
                    snodeMid = p[i][snodeMid1];
                    link.add(pathLength, snodeMid1);
                    pathLength++;    //·������
                    snodeMid1 = snodeMid;
                    //if(snodeMid1 == sNode1) break;//�ӻ�㵽Դ���·���Ѿ�����
                }
            }
            link.add(pathLength, snodeMid1);

            SpathFlow pathFlow = new SpathFlow();
            pathFlow.link = link;
            pathFlow.len = pathLength;
            System.out.println("vlink:" + i + " pathLength:" + pathLength);
            snodeMid1 = reqs[index].link[i].to;
            //snodeMid1 = reqs[index].link[i].from;
            snodeMid1 = v2s[index].snode.get(snodeMid1);
            for (int ii = 0; ii < pathLength; ii++) {
                snodeMid = p[i][snodeMid1];
                //System.out.print(snodeMid1+"-");
                snodeMid1 = snodeMid;
            }
            //System.out.print(snodeMid1);
            //System.out.println("");
            if (ret[i][1] == ret[i][0] || pathLength == 0) {
                System.out.println("ret[i][1]=ret[i][0].Error!******************");
            }
            if (pathLength == 0) {
                System.out.println("PathLength==0.Error!******************");
            }
            pathFlow.bw = reqs[index].link[i].bw;
            v2s[index].pathFlow.add(i, pathFlow);
            v2s[index].flowLen.add(i, 1);//1����·����·ӳ�䣻i����i����·��
            v2s[index].startSlotNo.add(i, ret[i][0]);//�������ʼ����
            v2s[index].slotNum.add(i, ret[i][1] - ret[i][0] + 1);    //�����Ƶ������
        }
        //����ӳ��ɹ���־
        v2s[index].map = Parameters.STATE_MAP_LINK;
        reqs[index].map = Parameters.STATE_MAP_LINK;
    }


    /*
     * ���ù��ڵ���������״̬
     */
    public void SetExpireVNState(VONRequest reqs[], int end, int time, int delay) {
        for (int i = 0; i < end; i++) {
            if (reqs[i].time + delay < time) {
                if (v2s[i].map == Parameters.STATE_DONE) {
                    //printf("v[%d]=Done\n",i);
                } else if (v2s[i].map == Parameters.STATE_MAP_LINK) {
                    //printf("v[%d]=Link Done\n",i);STATE_MAP_SUCC
                } else if (v2s[i].map == Parameters.STATE_MAP_SUCC) {
                    //printf("v[%d]=Link Done\n",i);
                } else {
                    //printf("v[%d]=%d expire\n", i,v2s[i].map);
                    v2s[i].map = Parameters.STATE_EXPIRE;
                }
            }
        }
    }

    /*
     * ��ӡ·��
     */
    public void PrintKShortestPath(AuxiliaryGraph sub, DistanceParent[] shortestPath, int pathNum, int sNode1, int sNode2) {
        int pathLength = 0;
        if (shortestPath[sNode2] == null) return;
        while (shortestPath[sNode2].parentVert != sNode1) {
            if (Parameters.DebugModel) System.out.print(sNode2 + "->");
            sNode2 = shortestPath[sNode2].parentVert;
            pathLength++;
        }
        if (Parameters.DebugModel) System.out.println(sNode2 + "->" + sNode1);
        //return pathLength+1;
    }

    public void PrintVNE(EOSubstrateNetwork sub, VONRequest reqs[]) {
        //if(v2s[i].map == Parameters.STATE_MAP_LINK || v2s[i].map == Parameters.STATE_DONE || v2s[i].map == Parameters.STATE_MAP_SUCC) {

        for (int index = 0; index < reqs.length; index++) {
            if (reqs[index].map == Parameters.STATE_MAP_LINK || reqs[index].map == Parameters.STATE_DONE || reqs[index].map == Parameters.STATE_MAP_SUCC) {
                PrintNodeEmbedding(reqs, index);
                PrintLinkEmbedding(reqs, index);
                PrintResultOfVN(sub, reqs, index);
            }
        }
    }

    //Print the result of embedding virtual nodes.
    public void PrintNodeEmbedding(VONRequest reqs[], int index) {
        System.out.println("The " + index + "th virtual network node embedding.");
        for (int i = 0; i < reqs[index].nodes; i++) {
            //System.out.println("v2s["+i+"].snode:"+v2s[index].snode.get(i));
            int snode = v2s[index].snode.get(i);
            System.out.println(i + "---" + snode);
            int reqCount = s2v_n[snode].req_count;
            //System.out.println("s2v_n["+v2s[index].snode.get(i)+"].req_count:"+s2v_n[v2s[index].snode.get(i)].req_count);
            for (int j = 0; j < reqCount; j++) {
                //System.out.println("snode:"+snode+" rest_cpu:"+s2v_n[snode].rest_cpu+" req:"+s2v_n[snode].req.get(j)+" vnode:"+s2v_n[snode].vnode.get(j)+" cpu:"+s2v_n[snode].cpu.get(j));
            }
        }
    }

    //Print the result of embedding virtual nodes.
    public void PrintNodeEmbedding(VONRequest reqs[]) {
        for (int index = 0; index < reqs.length; index++) {
            if (reqs[index].map == Parameters.STATE_MAP_LINK || reqs[index].map == Parameters.STATE_DONE || reqs[index].map == Parameters.STATE_MAP_SUCC) {
                System.out.println("reqs[" + index + "].map:" + reqs[index].map);
                System.out.println("The " + index + "th virtual network node embedding.");
                for (int i = 0; i < reqs[index].nodes; i++) {
                    //System.out.println("v2s["+i+"].snode:"+v2s[index].snode.get(i));
                    int snode = v2s[index].snode.get(i);
                    System.out.println(i + "---" + snode);
                    int reqCount = s2v_n[snode].req_count;
                    //System.out.println("s2v_n["+v2s[index].snode.get(i)+"].req_count:"+s2v_n[v2s[index].snode.get(i)].req_count);
                    for (int j = 0; j < reqCount; j++) {
                        //System.out.println("snode:"+snode+" rest_cpu:"+s2v_n[snode].rest_cpu+" req:"+s2v_n[snode].req.get(j)+" vnode:"+s2v_n[snode].vnode.get(j)+" cpu:"+s2v_n[snode].cpu.get(j));
                    }
                }
            }
        }
    }

    public void PrintLinkEmbedding(VONRequest reqs[], int index) {
        System.out.println("PrintLinkEmbedding-------------------");
        for (int i = 0; i < reqs[index].links; i++) {
            int snode1, snode2, vnode1, vnode2;
            vnode1 = reqs[index].link[i].from;
            vnode2 = reqs[index].link[i].to;
            snode1 = v2s[index].snode.get(vnode1);//reqs[index].link[i].from;
            snode2 = v2s[index].snode.get(vnode2);//reqs[index].link[i].to;
            System.out.println("vlink:" + vnode1 + "-" + vnode2);
            System.out.println("slink:" + snode1 + "-" + snode2);
            for (int j = 0; j < v2s[index].flowLen.get(i); j++) {//��������flowLen
                int snodeMid1 = -1;//,snodeMid2=-1;
                for (int k = 0; k < v2s[index].pathFlow.get(i).len; k++) {
                    snodeMid1 = (int) v2s[index].pathFlow.get(i).link.get(k);
                    //snodeMid2 = (int)v2s[index].pathFlow.get(i).link.get(k+1);
                    //int slink = sub.linksNo[snodeMid1][snodeMid2];
                    System.out.print(snodeMid1 + "->");
                }
                //System.out.println("");
                System.out.println(v2s[index].pathFlow.get(i).link.get(v2s[index].pathFlow.get(i).len));
            }
            System.out.println("startSlotNo:" + v2s[index].startSlotNo.get(i));
            System.out.println("slotNum:" + v2s[index].slotNum.get(i));
            System.out.println("");
        }
    }

    public void PrintLinkEmbedding(VONRequest reqs[]) {
        for (int index = 0; index < reqs.length; index++) {
            if (reqs[index].map == Parameters.STATE_MAP_LINK || reqs[index].map == Parameters.STATE_DONE || reqs[index].map == Parameters.STATE_MAP_SUCC) {
                //if(reqs[index].map != Parameters.STATE_MAP_SUCC) continue;
                System.out.println("PrintLinkEmbedding-------------------");
                for (int i = 0; i < reqs[index].links; i++) {
                    int snode1, snode2, vnode1, vnode2;
                    vnode1 = reqs[index].link[i].from;
                    vnode2 = reqs[index].link[i].to;
                    snode1 = v2s[index].snode.get(vnode1);//reqs[index].link[i].from;
                    snode2 = v2s[index].snode.get(vnode2);//reqs[index].link[i].to;
                    System.out.println("vlink:" + vnode1 + "-" + vnode2);
                    System.out.println("slink:" + snode1 + "-" + snode2);
                    for (int j = 0; j < v2s[index].flowLen.get(i); j++) {//��������flowLen
                        int snodeMid1 = -1;//,snodeMid2=-1;
                        for (int k = 0; k < v2s[index].pathFlow.get(i).len; k++) {
                            snodeMid1 = (int) v2s[index].pathFlow.get(i).link.get(k);
                            //snodeMid2 = (int)v2s[index].pathFlow.get(i).link.get(k+1);
                            //int slink = sub.linksNo[snodeMid1][snodeMid2];
                            System.out.print(snodeMid1 + "->");
                        }
                        //System.out.println("");
                        System.out.println(v2s[index].pathFlow.get(i).link.get(v2s[index].pathFlow.get(i).len));
                    }
                    System.out.println("startSlotNo:" + v2s[index].startSlotNo.get(i));
                    System.out.println("slotNum:" + v2s[index].slotNum.get(i));
                    System.out.println("");
                }
            }
        }
    }

    public void Print_sub_slots(EOSubstrateNetwork sub) {
        //��ӡsub.slots[][]
        for (int slink = 0; slink < sub.links; slink++) {
            //if(s2v_l[slink].req_count != 0){
            System.out.println("slink:" + slink + "(" + sub.link[slink].from + "," + sub.link[slink].to + ")" + " sub.slots:" + s2v_l[slink].req_count);
            //��ӡsub.slots
            for (int k = 0; k < sub.slotsNum; k++) {
                System.out.print(sub.slots[slink][k] + " ");
            }
            System.out.println("");
            //}
        }
    }

    public void Print_s2v_l(EOSubstrateNetwork sub, VONRequest reqs[], int index) {
        //��ӡs2v_l
        for (int slink = 0; slink < sub.links; slink++) {
            if (s2v_l[slink].req_count != 0)
                System.out.println("slink:" + slink + "(" + sub.link[slink].from + "," + sub.link[slink].to + ")" + " is embedded:");
            //System.out.println("The link of req["+index+"]."+i+" is embedded to the substrate links. Req_count:"+s2v_l[slink].req_count+" rest_bw:"+s2v_l[slink].rest_bw);
            for (int j = 0; j < s2v_l[slink].req_count; j++) {
                if ((int) s2v_l[slink].req.get(j) == index) {
                    System.out.print(s2v_l[slink].vlink.get(j) + "(" + s2v_l[slink].bw.get(j) + ") ");
                }
                System.out.println("");
            }
        }
    }

    public void Print_s2v_n(VONRequest reqs[], int index) {
        //��ӡs2v_n
        for (int i = 0; i < reqs[index].nodes; i++) {
            int snode;
            snode = v2s[index].snode.get(i);
            System.out.println("The node of req[" + index + "]." + i + " is embedded to " + snode + ". The information of s2v_n is followed:");
            for (int j = 0; j < s2v_n[snode].req.size(); j++) {
                if (s2v_n[snode].req.get(j) == index) {
                    System.out.println("cpu:" + s2v_n[snode].cpu.get(j) + " rest_cpu:" + s2v_n[snode].rest_cpu + " req_count" + s2v_n[snode].req_count);
                }
            }
        }
    }

    public void Print_s2v_n(VONRequest reqs[]) {
        for (int index = 0; index < reqs.length; index++) {
            //��ӡs2v_n
            for (int i = 0; i < reqs[index].nodes; i++) {
                int snode;
                snode = v2s[index].snode.get(i);
                System.out.println("The node of req[" + index + "]." + i + " is embedded to " + snode + ". The information of s2v_n is followed:");
                for (int j = 0; j < s2v_n[snode].req.size(); j++) {
                    if (s2v_n[snode].req.get(j) == index) {
                        System.out.println("cpu:" + s2v_n[snode].cpu.get(j) + " rest_cpu:" + s2v_n[snode].rest_cpu + " req_count" + s2v_n[snode].req_count);
                    }
                }
            }
        }
    }

    public void PrintResultOfVN(EOSubstrateNetwork sub, VONRequest reqs[]) {
        for (int index = 0; index < reqs.length; index++) {
            //��ӡs2v_n
            Print_s2v_n(reqs, index);

            //��ӡs2v_l
            Print_s2v_l(sub, reqs, index);

            //��ӡsub.slots[][]
            Print_sub_slots(sub);
        }
    }

    public void PrintResultOfVN(EOSubstrateNetwork sub, VONRequest reqs[], int index) {
        //��ӡs2v_n
        Print_s2v_n(reqs, index);

        //��ӡs2v_l
        Print_s2v_l(sub, reqs, index);

        //��ӡsub.slots[][]
        Print_sub_slots(sub);
    }

    public void PrintVNs(VONRequest reqs[], int reqsNum) {
        for (int i = 0; i < reqsNum; i++) {
            System.out.println("reqs[" + i + "]:" + reqs[i].map + " " + reqs[i].nodes + " " +
                    reqs[i].links + " " + reqs[i].split + " " +
                    reqs[i].time + " " + reqs[i].duration + " " +
                    reqs[i].topo + " " + reqs[i].revenue);

            for (int j = 0; j < reqs[i].nodes; j++) {
                System.out.println("reqs[" + i + "].cpu[" + j + "]:" + reqs[i].cpu[j]);
            }

            for (int j = 0; j < reqs[i].links; j++) {
                System.out.println("reqs[" + i + "].link[" + j + "]:" + reqs[i].link[j].from + " " + reqs[i].link[j].to + " " + reqs[i].link[j].bw + " " + reqs[i].link[j].speed);
            }
        }
    }

    /*
     * ��ӡĳ��·��
     */
    public void PrintPath(VONRequest reqs[], int index, DistanceParent[][][] kShortestPath, int p[][], int virtualNodes[], int pathEff[], int retLinkE[], int retSlotSE[], int retSlotEE[]) {
        String data = "\r\n-----------" + index + "-----------\r\n";

        for (int i = 0; i < reqs[index].links; i++) {
            int sNode1 = virtualNodes[reqs[index].link[i].from];//������·�˵�from��Ӧ�ĸ���ͼ�ڵ���
            int sNode2 = virtualNodes[reqs[index].link[i].to];//������·�˵�to��Ӧ�ĸ���ͼ�ڵ���
            int sLinkNo = retLinkE[i];//��·ӳ���·�����
            int pathNo1 = 0;
            boolean find = false;
            int vLinkNo = 0;
            int pathByLink = 0;
            for (vLinkNo = 0; vLinkNo < reqs[index].links; vLinkNo++) {
                find = false;
                for (pathByLink = 0; pathByLink < pathEff[vLinkNo]; pathByLink++) {
                    if (pathNo1 == sLinkNo) {//�ҵ���·��kShortestPath[k][j]
                        //����·��kShortestPath[k][j]������p
                        find = true;
                        break;
                    }
                    pathNo1++;
                }
                if (find) break;
            }
            //��ʼ��p[][]
            int sNode3 = sNode2;

            data += "virtual link " + i + " is embedded:" + retLinkE[i] + "\r\n";
            System.out.println("virtual link " + i + " is embedded:" + retLinkE[i] + "\r\n");
            while (kShortestPath[vLinkNo][pathByLink][sNode3].parentVert != sNode1) {
                if (sNode3 != sNode2) {//���������˵�
                    p[i][sNode3] = kShortestPath[vLinkNo][pathByLink][sNode3].parentVert;
                }
                data += sNode3 + "->";
                System.out.print(sNode3 + "->");
                sNode3 = kShortestPath[vLinkNo][pathByLink][sNode3].parentVert;
            }
            data += sNode3 + "->" + sNode1 + "(" + retSlotSE[i] + "-" + retSlotEE[i] + ")\r\n";
            ;
            System.out.println(sNode3 + "->" + sNode1 + "(" + retSlotSE[i] + "-" + retSlotEE[i] + ")");
            //System.out.println(retSlotSE[i]+"-"+retSlotEE[i]);
            //p[i][sNode3] = kShortestPath[vLinkNo][pathByLink][sNode3].parentVert;
        }
        Tools myDowith = new Tools();
        myDowith.SaveFile("EmbedOutput.dat", data, true);
    }

    public void PrintPath(VONRequest reqs[], int index, int vlink, int p[], int sNode1, int sNode2) {
        System.out.print(reqs[index].link[vlink].from + "-" + reqs[index].link[vlink].to + ":");
        while (p[sNode2] != -1) {
            System.out.print(sNode2 + "-");
            sNode2 = p[sNode2];
        }
        System.out.print(sNode2);
        System.out.println("");
    }

    public void PrintPath(int p[], int sNode1, int sNode2) {
        String str = "";
        System.out.println("PrintPath() start:" + sNode1 + "-" + sNode2);
        if (Parameters.DebugModel) str += "PrintPath() start:" + sNode1 + "-" + sNode2 + "\r\n";
        //System.out.print(reqs[index].link[vlink].from+"-"+reqs[index].link[vlink].to+":");
        if (p[sNode2] != -1) {
            while (p[sNode2] != -1) {
                System.out.print(sNode2 + "-");
                if (Parameters.DebugModel) str += sNode2 + "-";
                sNode2 = p[sNode2];
            }
            System.out.print(sNode2);
            System.out.println("");
            if (Parameters.DebugModel) str += sNode2 + "\r\n";
        } else if (p[sNode1] != -1) {
            while (p[sNode1] != -1) {
                System.out.print(sNode1 + "-");
                if (Parameters.DebugModel) str += sNode1 + "-";
                sNode1 = p[sNode1];
            }
            System.out.print(sNode1);
            System.out.println("");
            if (Parameters.DebugModel) str += sNode1 + "\r\n";
        } else {
            System.out.println("PrintPath(): error. There is not a path." + sNode1 + "-" + sNode2);
        }
        if (Parameters.DebugModel) {
            WriteFilePlus("process.txt", str);
        }
    }

    public void PrintSN(EOSubstrateNetwork sub) {
        //Create sub.cpu[].
        for (int i = 0; i < sub.nodes; i++) {
            System.out.println("sub.cpu[" + i + "]:" + sub.cpu[i]);
        }

        //Create sub.link[].
        for (int i = 0; i < sub.links; i++) {
            System.out.println("sub.link[" + i + "]:" + sub.link[i].from + " " + sub.link[i].to + " " + sub.link[i].bw + " " + sub.link[i].speed);
        }

        for (int i = 0; i < sub.links; i++) {
            for (int j = 0; j < sub.slotsNum; j++)//Parameters.MaxSlots
                System.out.println("sub.slots[" + i + "][" + j + "]:" + sub.slots[i][j]);
        }

        //Create sub.modulevel[]\transRate[]\opticalReach[]
        for (int i = 0; i < sub.modulationLevel; i++) {
            System.out.println("sub.modulevel[" + i + "]:" + sub.modulevel[i] + " " + sub.transRate[i] + " " + sub.opticalReach[i]);
        }
    }

    /*
     * ����cpu
     */
    public void UpdateSub(EOSubstrateNetwork toSub, EOSubstrateNetwork fromSub) {
        for (int i = 0; i < fromSub.nodes; i++) {
            toSub.cpu[i] = fromSub.cpu[i];
        }
    }

    /*
     * ����slots
     */
    public void UpdateSubSlots(EOSubstrateNetwork toSub, EOSubstrateNetwork fromSub) {
        for (int i = 0; i < fromSub.nodes; i++) {
            for (int j = 0; j < Parameters.MaxSlots; j++) {
                toSub.slots[i][j] = fromSub.slots[i][j];
            }
        }
    }

    /*
     * ����cpu
     */
    public void UpdateSub(EOSubstrateNetwork sub, int sNode, double cpu) {
        sub.cpu[sNode] -= cpu;
    }

    /*
     * ����
     */
    public void Clone(EOSubstrateNetwork toSub, EOSubstrateNetwork fromSub) {
        toSub.diffSlot = fromSub.diffSlot;
        toSub.faNodesNum = fromSub.faNodesNum;
        toSub.links = fromSub.links;
        toSub.modulationLevel = fromSub.modulationLevel;
        toSub.modulevel = fromSub.modulevel;
        toSub.netNodes = fromSub.netNodes;
        toSub.nodes = fromSub.nodes;
        toSub.opticalReach = fromSub.opticalReach;
        toSub.slotGHz = fromSub.slotGHz;
        toSub.slotsNum = fromSub.slotsNum;
        toSub.transRate = fromSub.transRate;

        toSub.maxcpu = new double[fromSub.nodes];
        toSub.cpu = new double[fromSub.nodes];
        toSub.faNodes = new int[fromSub.faNodesNum];
        toSub.link = new LinkStruct[fromSub.links];
        toSub.slots = new int[fromSub.links][fromSub.slotsNum];
        toSub.linksNo = new int[fromSub.nodes][fromSub.nodes];
        for (int i = 0; i < fromSub.nodes; i++) {
            toSub.cpu[i] = fromSub.cpu[i];
        }
        for (int i = 0; i < fromSub.nodes; i++) {
            toSub.maxcpu[i] = fromSub.maxcpu[i];
        }
        for (int i = 0; i < fromSub.faNodesNum; i++) {
            toSub.faNodes[i] = fromSub.faNodes[i];
        }
        for (int i = 0; i < fromSub.links; i++) {
            toSub.link[i] = fromSub.link[i];
        }
        for (int i = 0; i < fromSub.links; i++) {
            for (int j = 0; j < fromSub.slotsNum; j++)
                toSub.slots[i][j] = fromSub.slots[i][j];
        }
        for (int i = 0; i < fromSub.nodes; i++) {
            for (int j = 0; j < fromSub.nodes; j++)
                toSub.linksNo[i][j] = fromSub.linksNo[i][j];
        }
        toSub.node_GHG = new  double[sub.nodes];
        for (int i = 0; i < sub.nodes; i++) {
            toSub.node_GHG[i]=fromSub.node_GHG[i];
        }
    }

    public void CreateSN(EOSubstrateNetwork sub) throws IOException {
        String strBuff;
        BufferedReader data = new BufferedReader(new InputStreamReader(new FileInputStream(SNFile)));
        //read a line which means a number of virtual optical network requests.
        //For an example, 10 is the number of virtual optical network requests.
        strBuff = data.readLine();    //the number of virtual optical network requests
        String[] strcol = strBuff.split(" ");
        int nodesNum = Integer.valueOf(strcol[0]);//�ڵ�����
        int linksNum = Integer.valueOf(strcol[1]);//��·����
        int slotsNum = Integer.valueOf(strcol[2]);//The number of slots each link.
        //slotsNum = Parameters.MaxSlots;
        double slotGHz = Double.valueOf(strcol[3]);//Teach lhe transmission rate ink.
        int modulationLevel = Integer.valueOf(strcol[4]);//The number of modulation level.
        int diffSlot = Integer.valueOf(strcol[5]);//The number of slot between two slots.

        int faNodesNum = Integer.valueOf(strcol[6]);

        Parameters.MaxSlots = slotsNum;//2019.4.30����


        //sub = new EOSubstrateNetwork();//Create the elastic optical substrate network.
        sub.cpu = new double[nodesNum];
        sub.maxcpu = new double[nodesNum];
        sub.nodes = nodesNum;
        sub.links = linksNum;
        sub.link = new LinkStruct[linksNum];
        sub.opticalReach = new double[modulationLevel];
        sub.slotsNum = slotsNum;
        sub.slotGHz = slotGHz;
        sub.modulationLevel = modulationLevel;
        sub.transRate = new double[modulationLevel];
        sub.slots = new int[linksNum][slotsNum];
        sub.modulevel = new String[sub.modulationLevel + 1];
        sub.diffSlot = diffSlot;
        sub.faNodes = new int[faNodesNum];
        sub.faNodesNum = faNodesNum;
//24 7.16
        sub.jihuonodenumber=0;
        sub.jihuolinknumber=0;
        sub.jihuolength=0;

        sub.cpuTime = new double[nodesNum];
        sub.cpuOnTime = new double[nodesNum];
        sub.cpuOn = new int[nodesNum];
        //sub.link_times=new double[linksNum];//2022.09.13 ��·ʱ�䣨��·ʹ��״̬�����ڼ���EDFA
        sub.time = 0;
        sub.smallCpu=0;
        sub.largeCpu=0;
        sub.timeWindowsNumber=0;
        sub.LargeB=0;
        sub.EBFA=0;
        sub.LargeBS=0;

        sub.node_GHG = new  double[nodesNum];

        //Init slots.
        for (int i = 0; i < sub.links; i++) {
            for (int j = 0; j < sub.slotsNum; j++)
                sub.slots[i][j] = 1;//����Ϊ1��ռ��Ϊ0
        }
        //Create sub.cpu[].
        for (int i = 0; i < sub.nodes; i++) {
            strBuff = data.readLine(); //Read the line of a sub information.
            sub.cpu[i] = Double.valueOf(strBuff);///2.0;
            sub.maxcpu[i] = sub.cpu[i];
            sub.cpuOnTime[i] = 0;
        }
        // Create sub.GHG[]
         for (int i=0;i<sub.nodes;i++){
             strBuff = data.readLine(); //Read the line of a sub information.
             sub.node_GHG[i]=Double.valueOf(strBuff);
         }



        //Create sub.link[].
        for (int i = 0; i < sub.links; i++) {
            strBuff = data.readLine(); //Read the line of a sub information.
            strcol = strBuff.split(" ");
            sub.link[i] = new LinkStruct();
            sub.link[i].from = Integer.valueOf(strcol[0]);
            sub.link[i].to = Integer.valueOf(strcol[1]);
            sub.link[i].bw = Double.valueOf(strcol[2]);
            sub.link[i].length = Double.valueOf(strcol[3]);///10.0;	//�������5
            sub.link[i].times=0;//2022/09/13 ��·״̬ʱ��
        }

        //����from��to�õ���·��linkNo[sub.nodes][sub.nodes]
        sub.linksNo = new int[sub.nodes][sub.nodes];
        for (int i = 0; i < sub.nodes; i++) {
            for (int j = 0; j < sub.nodes; j++)
                sub.linksNo[i][j] = -1;
        }
        for (int i = 0; i < sub.links; i++) {
            sub.linksNo[sub.link[i].from][sub.link[i].to] = sub.linksNo[sub.link[i].to][sub.link[i].from] = i;
        }

        //Create sub.modulevel[]\transRate[]\opticalReach[]
        for (int i = 0; i < sub.modulationLevel; i++) {
            strBuff = data.readLine(); //Read the line of a sub information.
            strcol = strBuff.split(" ");
            sub.modulevel[i] = String.valueOf(strcol[0]);
            sub.transRate[i] = Double.valueOf(strcol[1]);
            sub.opticalReach[i] = Double.valueOf(strcol[2]);
        }

        //����sub.opticalReach
        boolean changed;
        double reachMid;
        String strMid;
        for (int i = 0; i < sub.modulationLevel; i++) {
            changed = false;
            for (int j = i + 1; j < sub.modulationLevel; j++) {
                if (sub.opticalReach[i] < sub.opticalReach[j]) {
                    changed = true;
                    reachMid = sub.opticalReach[i];    //change the opticalReach[i] and opticalReach[j]
                    sub.opticalReach[i] = sub.opticalReach[j];
                    sub.opticalReach[j] = reachMid;

                    reachMid = sub.transRate[i];
                    sub.transRate[i] = sub.transRate[j];
                    sub.transRate[j] = reachMid;

                    strMid = sub.modulevel[i];
                    sub.modulevel[i] = sub.modulevel[j];
                    sub.modulevel[j] = strMid;
                }
            }
            if (!changed) break;
        }

        //Create sub.faNodes[]:�����faNodes�ڵ��.
        for (int i = 0; i < faNodesNum; i++) {
            strBuff = data.readLine(); //Read the line of a sub information.
            sub.faNodes[i] = Integer.valueOf(strBuff);
        }

        data.close();
        return;
    }


    //Create virtual network requests from the files from the directory of VNsFileDir.
    public void CreateVNs(VONRequest reqs[], int reqsNum) throws IOException {
        String strBuff;
        String[] strcol;
        BufferedReader data;// = new BufferedReader(new InputStreamReader(new FileInputStream(VNsFileDir)));

        String strName = "";
        for (int i = 0; i < reqsNum; i++) {
            strName = "" + VNsFileDir + "/req" + String.valueOf(i) + ".txt";
            data = new BufferedReader(new InputStreamReader(new FileInputStream(strName)));
            strBuff = data.readLine();
            strcol = strBuff.split(" ");
            reqs[i] = new VONRequest();
            reqs[i].map = Parameters.STATE_NEW;//1;
            reqs[i].nodes = Integer.valueOf(strcol[0]);
            reqs[i].links = Integer.valueOf(strcol[1]);
            reqs[i].split = Integer.valueOf(strcol[2]);
            reqs[i].time = Integer.valueOf(strcol[3]);
            reqs[i].duration = Integer.valueOf(strcol[4]);
            reqs[i].topo = Integer.valueOf(strcol[5]);
            reqs[i].revenue = 0;
            reqs[i].cpu = new double[reqs[i].nodes];
            for (int j = 0; j < reqs[i].nodes; j++) {
                strBuff = data.readLine();
                strcol = strBuff.split(" ");//2020-9-15add chenxh
                reqs[i].revenue = 0;
                //reqs[i].cpu[j] = Double.valueOf(strBuff)/1.5;//;//1.5���ģ
                //reqs[i].cpu[j] = Double.valueOf(strcol[2]);///2.5;
                reqs[i].cpu[j] = Double.valueOf(strcol[0]) * Parameters.vcpuPara;//1.2;////*1.5;;//*0.06;//*1.5;//*0.3;
                reqs[i].revenue += reqs[i].cpu[j];
            }

            reqs[i].link = new LinkStruct[reqs[i].links];
            for (int j = 0; j < reqs[i].links; j++) {
                strBuff = data.readLine();
                strcol = strBuff.split(" ");
                reqs[i].link[j] = new LinkStruct();
                reqs[i].link[j].from = Integer.valueOf(strcol[0]);
                reqs[i].link[j].to = Integer.valueOf(strcol[1]);
                reqs[i].link[j].bw = Double.valueOf(strcol[2]) * Parameters.vbwPara;//1.0;//*0.02;//*5;//*0.02;//*0.2;//���ģ*30;//*0.2;//*0.2С��ģ;//*30;//30���ģ;//*5;//*30;//*30;// * 60;
                reqs[i].link[j].speed = Double.valueOf(strcol[3]);//*0.01;
                reqs[i].revenue += reqs[i].link[j].bw;
            }
            data.close();
        }

    }

    //start pagerank
    public boolean judge(int nodes_num, float backup[], double result[]) {
        int i;
        double ELSILON = 0.0001;
        for (i = 0; i < nodes_num; i++) {
            if (Math.abs(result[i] - backup[i]) >= ELSILON) {
                //float f = fabs (result[i] - backup[i]) ;
                return true;
            }
        }
        return false;
    }

    public double[] pagerank(int nodes_num, int links_num, double cpu[], LinkStruct[] link, double[] sum_adj, float sum_all, double cb_value[], double rank[]) {
        int i, j, k;
        float DAMPING = (float)0.26;//0.15;0.26
        float backup[] = new float[nodes_num];
        float poss1[][] = new float[nodes_num][nodes_num];
        // poss1 = (float**) malloc (nodes_num * sizeof (float*));
        //   for (i=0; i<nodes_num; i++)
        //     poss1[i] = (float*) malloc (nodes_num * sizeof (float));

        for (i = 0; i < nodes_num; i++) {
            for (j = 0; j < nodes_num; j++) {
                poss1[i][j] = 0;
                poss1[i][j] = (float) (DAMPING * cb_value[j] / sum_all);  //DAMPING=0.15��p(uj)  ,,poss1[i][j]����ڵ�j��cb_valueռ���нڵ�ļ���֮��

                for (k = 0; k < links_num; k++) {
                    if ((link[k].from == i && link[k].to == j) || (link[k].from == j && link[k].to == i)) {
                        poss1[i][j] += (1 - DAMPING) * cb_value[j] / sum_adj[i];//1-DAMPING=0.85��p(uf)   �������������ڵ�i��j֮���й�������poss1[i][j]�ټ��� j�ڵ��cb_valueֵռi�ڵ���Χ���нڵ�cb_value�ļ���֮��
                        if (sum_adj[i] == 0)
                            System.out.println("bupt:sum_adj " + i + " is 0\n");
                        // printf();
                    }
                }
            }
        }
        //���Կ���poss[i][j]


        // backup = (float*) malloc (nodes_num * sizeof (float));
        for (i = 0; i < nodes_num; i++)
            backup[i] = 0;
        int iteration_time = 0;
        for (i = 0; i < nodes_num; i++) {
            rank[i] = rank[i] / sum_all;   //pagerank��ʼֵ���������нڵ�rankֵ������ʵ���ǵ�ǰcb_value��ֵռ���нڵ�cb_value��ֵ�ļ���֮��
        }
        while (judge(nodes_num, backup, rank))   //�����������Сֵ0.0001����ѭ��
        {
            iteration_time++;  //��������++
            for (i = 0; i < nodes_num; i++) {
                backup[i] = (float) rank[i];   //����backup��rank
                rank[i] = 0;    //��ʼ���µ�rank
            }

            for (i = 0; i < nodes_num; i++) {
                for (j = 0; j < nodes_num; j++)
                    rank[i] = rank[i] + backup[j] * poss1[j][i];    //�µ�rank=rank+������rank* poss1[j][i]
            }
        }


//	#ifdef DEBUG_RW
        System.out.println("bupt: iteration need " + iteration_time + " times\n");  //��Ҫ���ٴε���
//	#endif
//    free (backup);
//	    for(i = 0;i<nodes_num;i++)
//	        free(poss1[i]);
//	    free(poss1);
        return rank;
    }

    public double[] energypagerank(int nodes_num, int links_num, double cpu[], LinkStruct[] link, double[] sum_adj, float sum_all, double cb_value[], double rank[]) {
        int i, j, k;
        float DAMPING = (float) 0.01;//0.15;
        float backup[] = new float[nodes_num];
        float poss1[][] = new float[nodes_num][nodes_num];
        // poss1 = (float**) malloc (nodes_num * sizeof (float*));
        //   for (i=0; i<nodes_num; i++)
        //     poss1[i] = (float*) malloc (nodes_num * sizeof (float));

        for (i = 0; i < nodes_num; i++) {
            for (j = 0; j < nodes_num; j++) {
                poss1[i][j] = 0;
                poss1[i][j] = (float) (DAMPING * cb_value[j] / sum_all);  //DAMPING=0.15��p(uj)  ,,poss1[i][j]����ڵ�j��cb_valueռ���нڵ�ļ���֮��

                for (k = 0; k < links_num; k++) {
                    if ((link[k].from == i && link[k].to == j) || (link[k].from == j && link[k].to == i)) {
                        poss1[i][j] += (1 - DAMPING) * cb_value[j] / sum_adj[i];//1-DAMPING=0.85��p(uf)   �������������ڵ�i��j֮���й�������poss1[i][j]�ټ��� j�ڵ��cb_valueֵռi�ڵ���Χ���нڵ�cb_value�ļ���֮��
                        if (sum_adj[i] == 0)
                            System.out.println("bupt:sum_adj " + i + " is 0\n");
                        // printf();
                    }
                }
            }
        }
        //���Կ���poss[i][j]


        // backup = (float*) malloc (nodes_num * sizeof (float));
        for (i = 0; i < nodes_num; i++)
            backup[i] = 0;
        int iteration_time = 0;
        for (i = 0; i < nodes_num; i++) {
            rank[i] = rank[i] / sum_all;   //pagerank��ʼֵ���������нڵ�rankֵ������ʵ���ǵ�ǰcb_value��ֵռ���нڵ�cb_value��ֵ�ļ���֮��
        }
        while (judge(nodes_num, backup, rank))   //�����������Сֵ0.0001����ѭ��
        {
            iteration_time++;  //��������++
            for (i = 0; i < nodes_num; i++) {
                backup[i] = (float) rank[i];   //����backup��rank
                rank[i] = 0;    //��ʼ���µ�rank
            }

            for (i = 0; i < nodes_num; i++) {
                for (j = 0; j < nodes_num; j++)
                    rank[i] = rank[i] + backup[j] * poss1[j][i];    //�µ�rank=rank+������rank* poss1[j][i]
            }
        }


//	#ifdef DEBUG_RW
        System.out.println("bupt: iteration need " + iteration_time + " times\n");  //��Ҫ���ٴε���
//	#endif
//    free (backup);
//	    for(i = 0;i<nodes_num;i++)
//	        free(poss1[i]);
//	    free(poss1);
        return rank;
    }

    //decide whether continue iteration
    /*
     * ���ܣ�����ʣ��restSlots[]
     */
    public void InitRestSlots(double restSlots[], EOSubstrateNetwork sub) {
        int sum = 0;
        for (int i = 0; i < sub.links; i++) {
            sum = 0;
            for (int j = 0; j < Parameters.MaxSlots; j++) {
                if (sub.slots[i][j] == 1) {
                    sum++;
                }
            }
            restSlots[i] += (double) sum;
        }
    }

    public double[] InitSNodePageRank(double sNodePageRank[], EOSubstrateNetwork sub) {
        int i, j, k;
        float sum_bw = 0;
        float sum_all = 0;
        sub.cb_value = new double[sub.nodes];
        sub.sum_adj = new double[sub.nodes];
        //sub.rank=new double[ sub.nodes];
        double restSlots[] = new double[sub.links];//�ײ���·�Ŀ���slot
        InitRestSlots(restSlots, sub);//��ʼ������slot

        for (i = 0; i < sub.nodes; i++) {
            sum_bw = 0;
            //���ѭ���Ǽ����ÿһ���ڵ���Χ��ʣ�����
            for (k = 0; k < sub.links; k++) {
                if (sub.link[k].from == i || sub.link[k].to == i) {
                    //if(sub.link[k].from == i && s2v_l[k].rest_bw > 0)
                    if (sub.link[k].from == i && restSlots[k] > 0) {//cxh�޸�2019.07.27
                        j = sub.link[k].to;
                        //sum_bw += s2v_l[k].rest_bw;
                        sum_bw += restSlots[k];//cxh�޸�2019.07.27
                    } else if (sub.link[k].to == i && restSlots[k] > 0) {//cxh�޸�2019.07.27
                        //}else if(sub.link[k].to == i && s2v_l[k].rest_bw > 0){
                        j = sub.link[k].from;
                        //sum_bw += s2v_l[k].rest_bw;
                        sum_bw += restSlots[k];
                    }
                }
            }

            sub.cb_value[i] = s2v_n[i].rest_cpu * sum_bw;  //cb_value��ʾ��ǰ�ýڵ���Χʣ������Լ�����cpu�ĳ˻�

            //  sub.rank[i] = s2v_n[i].rest_cpu*sum_bw;
            sNodePageRank[i] = s2v_n[i].rest_cpu * sum_bw;
            sum_all += sub.cb_value[i];                 //sum_allΪ���нڵ��cb_value��ֵ

            if (s2v_n[i].rest_cpu < 0 || sum_bw < 0 || sub.cb_value[i] < 0) {
                System.out.println("bupt: error! cb_value is non negative\n");
                System.out.println("bupt:s2v_n[" + i + "].rest_cpu is " + s2v_n[i].rest_cpu + ",sum_bw is " + sum_bw + ",sub.cb_value is " + sub.cb_value[i] + "\n");
            }

        }
        for (i = 0; i < sub.nodes; i++) {
            sub.sum_adj[i] = 0;
            for (k = 0; k < sub.links; k++) {

                if (sub.link[k].from == i || sub.link[k].to == i) {
                    if (sub.link[k].from == i) {
                        j = sub.link[k].to;
                        sub.sum_adj[i] += sub.cb_value[j];   //sub.sum_adj[i]Ϊ������ǰ�ڵ��rankֵΪ��Χ���нڵ�� cb_value���ܺ�
                    } else if (sub.link[k].to == i) {
                        j = sub.link[k].from;
                        sub.sum_adj[i] += sub.cb_value[j];
                    }
                }

            }
        }
        System.out.println("bupt: sum_all is " + sum_all + "\n");
        sNodePageRank = pagerank(sub.nodes, sub.links, sub.cpu, sub.link, sub.sum_adj, sum_all, sub.cb_value, sNodePageRank);
        return sNodePageRank;

    }

    public double[] InitSNodeEnergyPageRank(double sNodePageRank[], EOSubstrateNetwork sub) {
        int i, j, k;
        float sum_bw = 0;
        float sum_all = 0;
        float sum_cpu = 0;
        sub.cb_value = new double[sub.nodes];
        sub.sum_adj = new double[sub.nodes];
        //sub.rank=new double[ sub.nodes];
        double restSlots[] = new double[sub.links];//�ײ���·�Ŀ���slot
        InitRestSlots(restSlots, sub);//��ʼ������slot

        for (i = 0; i < sub.nodes; i++) {
            sum_bw = 0;
            sum_cpu = 0;
            //���ѭ���Ǽ����ÿһ���ڵ���Χ��ʣ�����
            for (k = 0; k < sub.links; k++) {
                if (sub.link[k].from == i || sub.link[k].to == i) {
                    //if(sub.link[k].from == i && s2v_l[k].rest_bw > 0)
                    if (sub.link[k].from == i && restSlots[k] > 0) {//cxh�޸�2019.07.27
                        j = sub.link[k].to;
                        //sum_bw += s2v_l[k].rest_bw;
                        sum_bw += restSlots[k];//cxh�޸�2019.07.27
                    } else if (sub.link[k].to == i && restSlots[k] > 0) {//cxh�޸�2019.07.27
                        //}else if(sub.link[k].to == i && s2v_l[k].rest_bw > 0){
                        j = sub.link[k].from;
                        //sum_bw += s2v_l[k].rest_bw;
                        sum_bw += restSlots[k];
                    }
                }
            }
            for (k = 0; k < sub.links; k++) {
                if (sub.link[k].from == i && restSlots[k] > 0) {
                    sum_cpu += s2v_n[sub.link[k].to].rest_cpu;
                } else if (sub.link[k].to == i && restSlots[k] > 0) {
                    sum_cpu += s2v_n[sub.link[k].from].rest_cpu;
                }
            }

            sub.cb_value[i] = (s2v_n[i].rest_cpu + sum_cpu) * sum_bw;  //cb_value��ʾ��ǰ�ýڵ���Χʣ������Լ�����cpu+��Χcpu�ĳ˻�

            //  sub.rank[i] = s2v_n[i].rest_cpu*sum_bw;
            sNodePageRank[i] = s2v_n[i].rest_cpu * sum_bw;
            sum_all += sub.cb_value[i];                 //sum_allΪ���нڵ��cb_value��ֵ

            if (s2v_n[i].rest_cpu < 0 || sum_bw < 0 || sub.cb_value[i] < 0) {
                System.out.println("bupt: error! cb_value is non negative\n");
                System.out.println("bupt:s2v_n[" + i + "].rest_cpu is " + s2v_n[i].rest_cpu + ",sum_bw is " + sum_bw + ",sub.cb_value is " + sub.cb_value[i] + "\n");
            }

        }
        for (i = 0; i < sub.nodes; i++) {
            sub.sum_adj[i] = 0;
            for (k = 0; k < sub.links; k++) {

                if (sub.link[k].from == i || sub.link[k].to == i) {
                    if (sub.link[k].from == i) {
                        j = sub.link[k].to;
                        sub.sum_adj[i] += sub.cb_value[j];   //sub.sum_adj[i]Ϊ������ǰ�ڵ��rankֵΪ��Χ���нڵ�� cb_value���ܺ�
                    } else if (sub.link[k].to == i) {
                        j = sub.link[k].from;
                        sub.sum_adj[i] += sub.cb_value[j];
                    }
                }

            }
        }
        System.out.println("bupt: sum_all is " + sum_all + "\n");
        sNodePageRank = pagerank(sub.nodes, sub.links, sub.cpu, sub.link, sub.sum_adj, sum_all, sub.cb_value, sNodePageRank);
        return sNodePageRank;

    }

    public double[] InitSNodePageRankOfGHG(double sNodePageRank[], EOSubstrateNetwork sub) {
        int i, j, k;
        float sum_bw = 0;
        float sum_all = 0;
        float sum_cpu = 0;
        float sum_ghg = 0;
        double norm =0;

        sub.cb_value = new double[sub.nodes];
        sub.sum_adj = new double[sub.nodes];
        double subrank[]=new double[ sub.nodes];
        double restSlots[] = new double[sub.links];//�ײ���·�Ŀ���slot
        double  CENM[]=new double[sub.nodes];
        InitRestSlots(restSlots, sub);//��ʼ������slot

        for (i = 0; i < sub.nodes; i++) {
            sum_bw = 0;
            sum_cpu = 0;
            sum_ghg = 0;
            //���ѭ���Ǽ����ÿһ���ڵ���Χ��ʣ�����
            for (k = 0; k < sub.links; k++) {
                if (sub.link[k].from == i || sub.link[k].to == i) {
                    //if(sub.link[k].from == i && s2v_l[k].rest_bw > 0)
                    if (sub.link[k].from == i && restSlots[k] > 0) {//cxh�޸�2019.07.27
                        j = sub.link[k].to;
                        //sum_bw += s2v_l[k].rest_bw;
                        sum_bw += restSlots[k];//cxh�޸�2019.07.27
                    } else if (sub.link[k].to == i && restSlots[k] > 0) {//cxh�޸�2019.07.27
                        //}else if(sub.link[k].to == i && s2v_l[k].rest_bw > 0){
                        j = sub.link[k].from;
                        //sum_bw += s2v_l[k].rest_bw;
                        sum_bw += restSlots[k];
                    }
                }
            }
            //���ڽڵ������Դ֮��
            for (k = 0; k < sub.links; k++) {
                if (sub.link[k].from == i && restSlots[k] > 0) {
                    sum_cpu += s2v_n[sub.link[k].to].rest_cpu;
                } else if (sub.link[k].to == i && restSlots[k] > 0) {
                    sum_cpu += s2v_n[sub.link[k].from].rest_cpu;
                }
            }

            //�ڵ��̼�ŷ�ϵ����ģ��2������2114165

            for (k=0;k<sub.nodes;k++){
                norm = norm + sub.node_GHG[k]*sub.node_GHG[k];
            }
            norm = Math.sqrt(norm);


// ��Χ�ڵ�̼�ŷ��ŷ����ĵ���֮��
            for (k=0;k<sub.links;k++){
                if(sub.link[k].from == i && restSlots[k] > 0){
//                    sum_ghg += (1.0/sub.node_GHG[sub.link[k].to]);
                    sum_ghg += norm/(sub.node_GHG[sub.link[k].to]);

                } else if (sub.link[k].to == i && restSlots[k] > 0) {
//                    sum_ghg += 1.0/(sub.node_GHG[sub.link[k].from]);
                    sum_ghg += norm/(sub.node_GHG[sub.link[k].from]);

                }
            }
            CENM=InitSNodeAM2(CENM,sub);
            //sub.cb_value[i] = (s2v_n[i].rest_cpu + sum_cpu) * sum_bw*CENM[i];
            sub.cb_value[i] = (s2v_n[i].rest_cpu + sum_cpu) * sum_bw*(norm /(sub.node_GHG[i])+(sum_ghg));  //cb_value��ʾ��ǰ�ýڵ���Χʣ������Լ�����cpu+��Χcpu�ĳ˻�
//            sub.cb_value[i] = (s2v_n[i].rest_cpu + sum_cpu) * sum_bw /(sub.node_GHG[i]);  //cb_value��ʾ��ǰ�ýڵ���Χʣ������Լ�����cpu+��Χcpu�ĳ˻�

            subrank[i]=sub.cb_value[i];
            //  sub.rank[i] = s2v_n[i].rest_cpu*sum_bw;
            subrank[i] = s2v_n[i].rest_cpu * sum_bw;
            sum_all += sub.cb_value[i];                 //sum_allΪ���нڵ��cb_value��ֵ

            if (s2v_n[i].rest_cpu < 0 || sum_bw < 0 || sub.cb_value[i] < 0) {
                System.out.println("bupt: error! cb_value is non negative\n");
                System.out.println("bupt:s2v_n[" + i + "].rest_cpu is " + s2v_n[i].rest_cpu + ",sum_bw is " + sum_bw + ",sub.cb_value is " + sub.cb_value[i] + "\n");
            }

        }
        for (i = 0; i < sub.nodes; i++) {
            sub.sum_adj[i] = 0;
            for (k = 0; k < sub.links; k++) {

                if (sub.link[k].from == i || sub.link[k].to == i) {
                    if (sub.link[k].from == i) {
                        j = sub.link[k].to;
                        sub.sum_adj[i] += sub.cb_value[j];   //sub.sum_adj[i]Ϊ������ǰ�ڵ��rankֵΪ��Χ���нڵ�� cb_value���ܺ�
                    } else if (sub.link[k].to == i) {
                        j = sub.link[k].from;
                        sub.sum_adj[i] += sub.cb_value[j];
                    }
                }

            }
        }
        System.out.println("bupt: sum_all is " + sum_all + "\n");
        sNodePageRank = pagerank(sub.nodes, sub.links, sub.cpu, sub.link, sub.sum_adj, sum_all, sub.cb_value, subrank);
        return sNodePageRank;

    }
    public double[] InitSNodePageRankOfGHG1(double sNodePageRank[], EOSubstrateNetwork sub) {
        int i, j, k;
        float sum_bw = 0;
        float sum_all = 0;
        float sum_cpu = 0;
        float sum_ghg = 0;
        double norm =0;

        sub.cb_value = new double[sub.nodes];
        sub.sum_adj = new double[sub.nodes];
        double subrank[]=new double[ sub.nodes];
        double restSlots[] = new double[sub.links];//�ײ���·�Ŀ���slot
        double  CENM[]=new double[sub.nodes];
        InitRestSlots(restSlots, sub);//��ʼ������slot

        for (i = 0; i < sub.nodes; i++) {
            sum_bw = 0;
            sum_cpu = 0;
            sum_ghg = 0;
            //���ѭ���Ǽ����ÿһ���ڵ���Χ��ʣ�����
            for (k = 0; k < sub.links; k++) {
                if (sub.link[k].from == i || sub.link[k].to == i) {
                    //if(sub.link[k].from == i && s2v_l[k].rest_bw > 0)
                    if (sub.link[k].from == i && restSlots[k] > 0) {//cxh�޸�2019.07.27
                        j = sub.link[k].to;
                        //sum_bw += s2v_l[k].rest_bw;
                        sum_bw += restSlots[k];//cxh�޸�2019.07.27
                    } else if (sub.link[k].to == i && restSlots[k] > 0) {//cxh�޸�2019.07.27
                        //}else if(sub.link[k].to == i && s2v_l[k].rest_bw > 0){
                        j = sub.link[k].from;
                        //sum_bw += s2v_l[k].rest_bw;
                        sum_bw += restSlots[k];
                    }
                }
            }
            //���ڽڵ������Դ֮��
            for (k = 0; k < sub.links; k++) {
                if (sub.link[k].from == i && restSlots[k] > 0) {
                    sum_cpu += s2v_n[sub.link[k].to].rest_cpu;
                } else if (sub.link[k].to == i && restSlots[k] > 0) {
                    sum_cpu += s2v_n[sub.link[k].from].rest_cpu;
                }
            }

            //�ڵ��̼�ŷ�ϵ����ģ��2������2114165

            for (k=0;k<sub.nodes;k++){
                norm = norm + sub.node_GHG[k]*sub.node_GHG[k];
            }
            norm = Math.sqrt(norm);


// ��Χ�ڵ�̼�ŷ��ŷ����ĵ���֮��
            for (k=0;k<sub.links;k++){
                if(sub.link[k].from == i && restSlots[k] > 0){
//                    sum_ghg += (1.0/sub.node_GHG[sub.link[k].to]);
                    sum_ghg += norm/(sub.node_GHG[sub.link[k].to]);

                } else if (sub.link[k].to == i && restSlots[k] > 0) {
//                    sum_ghg += 1.0/(sub.node_GHG[sub.link[k].from]);
                    sum_ghg += norm/(sub.node_GHG[sub.link[k].from]);

                }
            }
            CENM=InitSNodeAM2(CENM,sub);
            sub.cb_value[i] = (s2v_n[i].rest_cpu + sum_cpu) * sum_bw*CENM[i];
            //sub.cb_value[i] = (s2v_n[i].rest_cpu + sum_cpu) * sum_bw*(norm /(sub.node_GHG[i])+(sum_ghg));  //cb_value��ʾ��ǰ�ýڵ���Χʣ������Լ�����cpu+��Χcpu�ĳ˻�
//            sub.cb_value[i] = (s2v_n[i].rest_cpu + sum_cpu) * sum_bw /(sub.node_GHG[i]);  //cb_value��ʾ��ǰ�ýڵ���Χʣ������Լ�����cpu+��Χcpu�ĳ˻�

            subrank[i]=sub.cb_value[i];
            //  sub.rank[i] = s2v_n[i].rest_cpu*sum_bw;
            subrank[i] = s2v_n[i].rest_cpu * sum_bw;
            sum_all += sub.cb_value[i];                 //sum_allΪ���нڵ��cb_value��ֵ

            if (s2v_n[i].rest_cpu < 0 || sum_bw < 0 || sub.cb_value[i] < 0) {
                System.out.println("bupt: error! cb_value is non negative\n");
                System.out.println("bupt:s2v_n[" + i + "].rest_cpu is " + s2v_n[i].rest_cpu + ",sum_bw is " + sum_bw + ",sub.cb_value is " + sub.cb_value[i] + "\n");
            }

        }
        for (i = 0; i < sub.nodes; i++) {
            sub.sum_adj[i] = 0;
            for (k = 0; k < sub.links; k++) {

                if (sub.link[k].from == i || sub.link[k].to == i) {
                    if (sub.link[k].from == i) {
                        j = sub.link[k].to;
                        sub.sum_adj[i] += sub.cb_value[j];   //sub.sum_adj[i]Ϊ������ǰ�ڵ��rankֵΪ��Χ���нڵ�� cb_value���ܺ�
                    } else if (sub.link[k].to == i) {
                        j = sub.link[k].from;
                        sub.sum_adj[i] += sub.cb_value[j];
                    }
                }

            }
        }
        System.out.println("bupt: sum_all is " + sum_all + "\n");
        sNodePageRank = pagerank(sub.nodes, sub.links, sub.cpu, sub.link, sub.sum_adj, sum_all, sub.cb_value, subrank);
        return sNodePageRank;

    }

    public double[] InitVNodePageRank(double[] vNodePageRank, VONRequest req[], int index) {
        int j, k;
        int i = index;
        //int n=req[index].nodes;
        float sum_all;

        sum_all = 0;

        req[i].cb_value = new double[req[i].nodes];
        req[i].sum_adj = new double[req[i].nodes];
        req[i].rank = new double[req[i].nodes];
        for (j = 0; j < req[i].nodes; j++) {
            float sum_bw = 0;
            for (k = 0; k < req[i].links; k++) {
                if (req[i].link[k].from == j) {
                    sum_bw += req[i].link[k].bw;
                } else if (req[i].link[k].to == j) {
                    sum_bw += req[i].link[k].bw;

                }
            }
            //System.out.println(req[i].cpu[j]+"      "+sum_bw);
            // double a=req[i].cpu[j] * sum_bw;
            // System.out.println(a);

            req[i].cb_value[j] = req[i].cpu[j] * sum_bw;
            req[i].rank[j] = req[i].cb_value[j];
            sum_all += req[i].cb_value[j];

        }
        for (j = 0; j < req[i].nodes; j++) {
            for (k = 0; k < req[i].links; k++) {
                if (req[i].link[k].from == j) {
                    req[i].sum_adj[j] += req[i].cb_value[req[i].link[k].to];
                } else if (req[i].link[k].to == j) {
                    req[i].sum_adj[j] += req[i].cb_value[req[i].link[k].from];

                }

            }

        }

        //sNodePageRank= pagerank(req[i].nodes, req[i].links, req[i].cpu, req[i].link, req[i].sum_adj, sum_all, req[i].cb_value, sNodePageRank);
        vNodePageRank = pagerank(req[i].nodes, req[i].links, req[i].cpu, req[i].link, req[i].sum_adj, sum_all, req[i].cb_value, req[i].rank);//2019.7.24�޸�
        //return sNodePageRank;
        return vNodePageRank;//2019.7.24�޸�
    }

    public double[] InitVNodeEnergyPageRank(double[] vNodePageRank, VONRequest req[], int index) {
        int j, k;
        int i = index;
        //int n=req[index].nodes;
        float sum_all;

        sum_all = 0;

        req[i].cb_value = new double[req[i].nodes];
        req[i].sum_adj = new double[req[i].nodes];
        req[i].rank = new double[req[i].nodes];
        for (j = 0; j < req[i].nodes; j++) {
            float sum_bw = 0;
            float sum_cpu = 0;
            for (k = 0; k < req[i].links; k++) {
                if (req[i].link[k].from == j) {
                    sum_bw += req[i].link[k].bw;
                } else if (req[i].link[k].to == j) {
                    sum_bw += req[i].link[k].bw;

                }
            }

            for (k = 0; k < req[i].links; k++) {
                if (req[i].link[k].from == j) {
                    sum_cpu += req[i].cpu[req[i].link[k].to];
                } else if (req[i].link[k].to == j) {
                    sum_cpu += req[i].cpu[req[i].link[k].from];
                }
            }
            //System.out.println(req[i].cpu[j]+"      "+sum_bw);
            // double a=req[i].cpu[j] * sum_bw;
            // System.out.println(a);

            req[i].cb_value[j] = (req[i].cpu[j] + sum_cpu) * sum_bw;
            req[i].rank[j] = req[i].cb_value[j];
            sum_all += req[i].cb_value[j];

        }
        for (j = 0; j < req[i].nodes; j++) {
            for (k = 0; k < req[i].links; k++) {
                if (req[i].link[k].from == j) {
                    req[i].sum_adj[j] += req[i].cb_value[req[i].link[k].to];
                } else if (req[i].link[k].to == j) {
                    req[i].sum_adj[j] += req[i].cb_value[req[i].link[k].from];

                }

            }

        }

        //sNodePageRank= pagerank(req[i].nodes, req[i].links, req[i].cpu, req[i].link, req[i].sum_adj, sum_all, req[i].cb_value, sNodePageRank);
        vNodePageRank = energypagerank(req[i].nodes, req[i].links, req[i].cpu, req[i].link, req[i].sum_adj, sum_all, req[i].cb_value, req[i].rank);//2019.7.24�޸�
        //return sNodePageRank;
        return vNodePageRank;//2019.7.24�޸�
    }
    public double[] InitVNodePageRankOfGHG(double[] vNodePageRank, VONRequest req[], int index) {
        int j, k;
        int i = index;
        //int n=req[index].nodes;
        float sum_all;
        float sum_ghg=0;

        sum_all = 0;

        req[i].cb_value = new double[req[i].nodes];
        req[i].sum_adj = new double[req[i].nodes];
        req[i].rank = new double[req[i].nodes];
        for (j = 0; j < req[i].nodes; j++) {
            float sum_bw = 0;
            float sum_cpu = 0;
            for (k = 0; k < req[i].links; k++) {
                if (req[i].link[k].from == j) {
                    sum_bw += req[i].link[k].bw;
                } else if (req[i].link[k].to == j) {
                    sum_bw += req[i].link[k].bw;

                }
            }

            for (k = 0; k < req[i].links; k++) {
                if (req[i].link[k].from == j) {
                    sum_cpu += req[i].cpu[req[i].link[k].to];
                } else if (req[i].link[k].to == j) {
                    sum_cpu += req[i].cpu[req[i].link[k].from];
                }
            }
            for (k = 0; k < req[i].links; k++) {
                if (req[i].link[k].from == j) {
//                    sum_ghg += 1000.0/321.81;
                    sum_ghg+=1;
                } else if (req[i].link[k].to == j) {
//                    sum_ghg += 1000.0/321.81;
                    sum_ghg+=1;
                }
            }

            //System.out.println(req[i].cpu[j]+"      "+sum_bw);
            // double a=req[i].cpu[j] * sum_bw;
            // System.out.println(a);

            req[i].cb_value[j] = (req[i].cpu[j] + sum_cpu) * sum_bw*(1+sum_ghg);
            req[i].rank[j] = req[i].cb_value[j];
            sum_all += req[i].cb_value[j];

        }
        for (j = 0; j < req[i].nodes; j++) {
            for (k = 0; k < req[i].links; k++) {
                if (req[i].link[k].from == j) {
                    req[i].sum_adj[j] += req[i].cb_value[req[i].link[k].to];
                } else if (req[i].link[k].to == j) {
                    req[i].sum_adj[j] += req[i].cb_value[req[i].link[k].from];

                }

            }

        }

        //sNodePageRank= pagerank(req[i].nodes, req[i].links, req[i].cpu, req[i].link, req[i].sum_adj, sum_all, req[i].cb_value, sNodePageRank);
        vNodePageRank = energypagerank(req[i].nodes, req[i].links, req[i].cpu, req[i].link, req[i].sum_adj, sum_all, req[i].cb_value, req[i].rank);//2019.7.24�޸�
        //return sNodePageRank;
        return vNodePageRank;//2019.7.24�޸�
    }

    public double[] InitVNodeEnergyPageRankEasy(double[] vNodePageRank, VONRequest req[], int index) {
        int j, k;
        int i = index;
        //int n=req[index].nodes;
        float sum_all;

        sum_all = 0;

        req[i].cb_value = new double[req[i].nodes];
        req[i].sum_adj = new double[req[i].nodes];
        req[i].rank = new double[req[i].nodes];
        for (j = 0; j < req[i].nodes; j++) {
            float sum_bw = 0;
            float sum_cpu = 0;
            for (k = 0; k < req[i].links; k++) {
                if (req[i].link[k].from == j) {
                    sum_bw += req[i].link[k].bw;
                } else if (req[i].link[k].to == j) {
                    sum_bw += req[i].link[k].bw;

                }
            }

            for (k = 0; k < req[i].links; k++) {
                if (req[i].link[k].from == j) {
                    sum_cpu += req[i].cpu[req[i].link[k].to];
                } else if (req[i].link[k].to == j) {
                    sum_cpu += req[i].cpu[req[i].link[k].from];
                }
            }
            //System.out.println(req[i].cpu[j]+"      "+sum_bw);
            // double a=req[i].cpu[j] * sum_bw;
            // System.out.println(a);

            req[i].cb_value[j] = (req[i].cpu[j] + sum_cpu) * sum_bw;
            req[i].rank[j] = req[i].cb_value[j];
            sum_all += req[i].cb_value[j];

        }
        for (j = 0; j < req[i].nodes; j++) {
            for (k = 0; k < req[i].links; k++) {
                if (req[i].link[k].from == j) {
                    req[i].sum_adj[j] += req[i].cb_value[req[i].link[k].to];
                } else if (req[i].link[k].to == j) {
                    req[i].sum_adj[j] += req[i].cb_value[req[i].link[k].from];

                }

            }

        }

        //sNodePageRank= pagerank(req[i].nodes, req[i].links, req[i].cpu, req[i].link, req[i].sum_adj, sum_all, req[i].cb_value, sNodePageRank);
        vNodePageRank = energypagerank(req[i].nodes, req[i].links, req[i].cpu, req[i].link, req[i].sum_adj, sum_all, req[i].cb_value, req[i].rank);//2019.7.24�޸�
        //return sNodePageRank;
        return vNodePageRank;//2019.7.24�޸�
    }

    public double[] InitSNodeEnergyPageRankEasy(double sNodePageRank[], EOSubstrateNetwork sub) {
        int i, j, k;
        float sum_bw = 0;
        float sum_all = 0;
        float sum_cpu = 0;
        sub.cb_value = new double[sub.nodes];
        sub.sum_adj = new double[sub.nodes];
        //sub.rank=new double[ sub.nodes];
        double restSlots[] = new double[sub.links];//�ײ���·�Ŀ���slot
        InitRestSlots(restSlots, sub);//��ʼ������slot

        for (i = 0; i < sub.nodes; i++) {
            sum_bw = 0;
            sum_cpu = 0;
            //���ѭ���Ǽ����ÿһ���ڵ���Χ��ʣ�����
            for (k = 0; k < sub.links; k++) {
                if (sub.link[k].from == i || sub.link[k].to == i) {
                    //if(sub.link[k].from == i && s2v_l[k].rest_bw > 0)
                    if (sub.link[k].from == i && restSlots[k] > 0) {//cxh�޸�2019.07.27
                        j = sub.link[k].to;
                        //sum_bw += s2v_l[k].rest_bw;
                        sum_bw += restSlots[k];//cxh�޸�2019.07.27
                    } else if (sub.link[k].to == i && restSlots[k] > 0) {//cxh�޸�2019.07.27
                        //}else if(sub.link[k].to == i && s2v_l[k].rest_bw > 0){
                        j = sub.link[k].from;
                        //sum_bw += s2v_l[k].rest_bw;
                        sum_bw += restSlots[k];
                    }
                }
            }
            for (k = 0; k < sub.links; k++) {
                if (sub.link[k].from == i && restSlots[k] > 0) {
                    sum_cpu += s2v_n[sub.link[k].to].rest_cpu;
                } else if (sub.link[k].to == i && restSlots[k] > 0) {
                    sum_cpu += s2v_n[sub.link[k].from].rest_cpu;
                }
            }

            sub.cb_value[i] = (s2v_n[i].rest_cpu + sum_cpu) * sum_bw;  //cb_value��ʾ��ǰ�ýڵ���Χʣ������Լ�����cpu�ĳ˻�

            //  sub.rank[i] = s2v_n[i].rest_cpu*sum_bw;
            sNodePageRank[i] = s2v_n[i].rest_cpu * sum_bw;
            sum_all += sub.cb_value[i];                 //sum_allΪ���нڵ��cb_value��ֵ

            if (s2v_n[i].rest_cpu < 0 || sum_bw < 0 || sub.cb_value[i] < 0) {
                System.out.println("bupt: error! cb_value is non negative\n");
                System.out.println("bupt:s2v_n[" + i + "].rest_cpu is " + s2v_n[i].rest_cpu + ",sum_bw is " + sum_bw + ",sub.cb_value is " + sub.cb_value[i] + "\n");
            }

        }
        for (i = 0; i < sub.nodes; i++) {
            sub.sum_adj[i] = 0;
            for (k = 0; k < sub.links; k++) {

                if (sub.link[k].from == i || sub.link[k].to == i) {
                    if (sub.link[k].from == i) {
                        j = sub.link[k].to;
                        sub.sum_adj[i] += sub.cb_value[j];   //sub.sum_adj[i]Ϊ������ǰ�ڵ��rankֵΪ��Χ���нڵ�� cb_value���ܺ�
                    } else if (sub.link[k].to == i) {
                        j = sub.link[k].from;
                        sub.sum_adj[i] += sub.cb_value[j];
                    }
                }

            }
        }
        System.out.println("bupt: sum_all is " + sum_all + "\n");
        sNodePageRank = pagerank(sub.nodes, sub.links, sub.cpu, sub.link, sub.sum_adj, sum_all, sub.cb_value, sNodePageRank);
        return sNodePageRank;

    }

    public static class VNEByEOpticalNet extends VNE {
        public int embedModelOrAlgo = -1;
        //VONEByTranModel myTransModel = new VONEByTranModel();

        VNEByEOpticalNet(String inSNFile,String inVNsFileDir)
        {
            //Set file name of substrate network and the directory of the virtual networks.
            SNFile = inSNFile;
            VNsFileDir = inVNsFileDir;

            try {
                //CreateSN(EOSubstrateNetwork sub);
                /*File directory = new File(".");
                String path = null;
                try {
                    path = directory.getCanonicalPath();//��ȡ��ǰ·��
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                } */
            }
            catch (Exception ex) {
                JOptionPane.showMessageDialog(null, ex.getStackTrace());
            }
        }


        //The algorithm of mapping the VNs.
        public void V2SEmbed(EOSubstrateNetwork sub,VONRequest reqs[],int delay,int embedAlgorithm) throws IOException
        {
            embedModelOrAlgo = embedAlgorithm;//ӳ��ģ�ͻ����㷨������embedModelOrAlgo
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

                //Release the resources.
                ReleaseAllResourceAmongZeroToEnd(sub,reqs,end,time);

                //Set the expire of STATE_EXPIRE.
                SetExpireVNState(reqs,end,time,delay);

                //Allocate the resources.
                AllocateResources(sub,reqs,start, end, time,delay,embedAlgorithm);

                time += Parameters.TIME_INTERVAL;  //ʱ�䴰����һ����λ

            }
            Date endDate = new Date();//��¼ӳ�俪ʼ��ʱ��
            long interval = (endDate.getTime() - startDate.getTime())/1000;//��¼ʱ���룩

            //��¼��Ϣ������ϵͳ���桢�����ʡ�����ɱ��ȡ�������Ƭ�����Ѷ��壬�����������ΪС��2�������Ŀ���SlotsΪ��Ƭ��
            if(Parameters.DebugModel) System.out.println("RecordResultsOfVNE.");
            RecordResultsOfVNE(sub,reqs,interval,embedAlgorithm);
            if(Parameters.DebugModel) System.out.println("PrintfVNE.");
            //if(Parameters.DebugModel)
            PrintNodeEmbedding(reqs);
            PrintLinkEmbedding(reqs);
            //PrintVNE(sub, reqs);
            //PrintResultOfVN(sub,reqs);
        }



        //
        private void AllocateResources(EOSubstrateNetwork sub,VONRequest reqs[],int start,int end,int time,int delay,int embedAlgorithm) throws IOException
        {
            System.out.println("start:" + start + " end:" + end);
            for(int i=start;i<end;i++){
                //if(reqs[i].time+delay < time) {
                    //System.out.println("The expired VN:" + i + " delay:"+delay);
                    //continue; //The expired VN.
                //}
                if(v2s[i].map == Parameters.STATE_NEW || v2s[i].map == Parameters.STATE_MAP_NODE_FAIL || v2s[i].map == Parameters.STATE_MAP_FAIL || v2s[i].map == Parameters.STATE_MAP_Link_FAIL) {
                    ArrayList<Object> list = new ArrayList<Object>();  //��¼�ڵ�ӳ����
                    int p[][] = new int[reqs[i].links][sub.nodes];
                    int ret[][] = new int[reqs[i].links][4];
                    //ret[][0]:����������·ӳ���������㣻ret[][1]:����������·ӳ��������յ�
                    //ret[][2]:���ص���ʼƵ�ײۣ�ret[][3]:���ص�Ƶ�ײ�����
                    v2s[i].tryMapTime ++;	//��¼ӳ�����
                    if(reqs[i].topo == Parameters.TOPO_GENERAL || reqs[i].topo == Parameters.TOPO_STAR) {
                        int embedCategory=embedAlgorithm,retPreMapLink = -2;
                        if(embedCategory == Parameters.MapVONEBy01ILP){
                            //MapVONEBy01ILP(EOSubstrateNetwork sub,VONRequest reqs[],int index,int ret[],int p[][],ArrayList list)
                            if(MapVONEBy01ILP(sub,reqs,i,ret[0],p,list)!=-1){
                                AddNodesMap(reqs,i,list);//�ڵ�ӳ��
                                AddLinksMapByMIP(sub,reqs,i,ret,p);	//��·ӳ��
                                v2s[i].map = Parameters.STATE_MAP_SUCC;
                                reqs[i].map = Parameters.STATE_MAP_SUCC;
                            } else {
                                v2s[i].map = Parameters.STATE_MAP_NODE_FAIL;
                                reqs[i].map = Parameters.STATE_MAP_NODE_FAIL;
                            }
                        } else if(embedCategory == Parameters.MapVONE3PByWangYAndChenxh || embedCategory == Parameters.MapVONE3ByWangY) {
                            if(MapVONEByILPWangYPlus3Nodes(sub,reqs,i,ret[0],p,list)!=-1){
                                //AddNodesMap(reqs,i,list);//�ڵ�ӳ��
                                //AddLinksMapByMIP(sub,reqs,i,ret,p);	//��·ӳ��
                                if(Parameters.DebugModel) Print_sub_slots(sub);
                                v2s[i].map = Parameters.STATE_MAP_SUCC;
                                reqs[i].map = Parameters.STATE_MAP_SUCC;
                            } else {
                                v2s[i].map = Parameters.STATE_MAP_NODE_FAIL;
                                reqs[i].map = Parameters.STATE_MAP_NODE_FAIL;
                            }
                        } else if(embedCategory == Parameters.MapVONETranModel){
                            /*//VONEByTranModel myTransModel = new VONEByTranModel(sub,reqs);
                            if(myTransModel.MapVONEByTranModel(sub, reqs, i)!=-1){
                            //if(MapVONEByTranModel(sub,reqs,i,ret[0],p,list)!=-1){
                                //AddNodesMap(reqs,i,list);//�ڵ�ӳ��
                                //AddLinksMapByMIP(sub,reqs,i,ret,p);	//��·ӳ��
                                //s2v_n = myTransModel.s2v_n;
                                //s2v_l = myTransModel.s2v_l;
                                //v2s = myTransModel.v2s;
                                if(Parameters.DebugModel) Print_sub_slots(sub);
                                v2s[i].map = Parameters.STATE_MAP_SUCC;
                                reqs[i].map = Parameters.STATE_MAP_SUCC;
                            } else {
                                v2s[i].map = Parameters.STATE_MAP_NODE_FAIL;
                                reqs[i].map = Parameters.STATE_MAP_NODE_FAIL;
                            }*/
                        }
                    }

                }
            }
        }
        //******************************************************************
        //���ƣ�int MapVONEBy01ILP(......)
        //���ܣ���01ILPģ��ӳ�����������, ����ɹ������s2v_n��v2s
        //������
        //	      s2v_nΪ����ڵ�ӳ�������ڵ����ݽṹ
        //	      s2v_lΪ������·ӳ��������·���ݽṹ
        //	      v2sΪ����ӳ��������������ݽṹ
        //	      indexΪ��index����������
        //����ֵ��0���ɹ����أ�-1��ʧ�ܷ���
        //******************************************************************
        private int MapVONEBy01ILP(EOSubstrateNetwork sub,VONRequest reqs[],int index,int ret[],int p[][],ArrayList<Object> list)
        {
            FindVONEByOne01ILP(sub,reqs,index);
            if(FindVONEOptimalSolution(ret,p[0],list)){//�ҵ������Ž�
                //ret[0] = startSlotNum;
                return 0;//�ɹ��ҵ�VONE��
            }
            return -1;
        }

        //******************************************************************
        //���ƣ�int MapVONEByILPWangYTwoNodes(......)
        //���ܣ���ILPģ�ͣ�WangY��ӳ�����������, ����ɹ������s2v_n��v2s
        //������
        //	      s2v_nΪ����ڵ�ӳ�������ڵ����ݽṹ
        //	      s2v_lΪ������·ӳ��������·���ݽṹ
        //	      v2sΪ����ӳ��������������ݽṹ
        //	      indexΪ��index����������
        //����ֵ��0���ɹ����أ�-1��ʧ�ܷ���
        //******************************************************************
        private int MapVONEByILPWangYTwoNodes(EOSubstrateNetwork sub,VONRequest reqs[],int index,int ret[],int p[][],ArrayList<Object> list)
        {
            //��������ͼ
            //����k��·��
            //���ÿ��·����ÿ�������slots����
            //��WangY��ILPģ�����
            //�����Ž�

            FindVONEByOne01ILP(sub,reqs,index);
            if(FindVONEOptimalSolution(ret,p[0],list)){//�ҵ������Ž�
                //ret[0] = startSlotNum;
                return 0;//�ɹ��ҵ�VONE��
            }
            return -1;
        }


        //******************************************************************
        //���ƣ�int MapVONEByTranModel(......)
        //���ܣ�������ģ��ӳ�����������, ����ɹ������s2v_n��v2s
        //������
        //	      s2v_nΪ����ڵ�ӳ�������ڵ����ݽṹ
        //	      s2v_lΪ������·ӳ��������·���ݽṹ
        //	      v2sΪ����ӳ��������������ݽṹ
        //	      indexΪ��index����������
        //����ֵ��0���ɹ����أ�-1��ʧ�ܷ���
        //******************************************************************
        private int MapVONEByTranModel(EOSubstrateNetwork sub,VONRequest reqs[],int index,int ret[],int p[][],ArrayList<Object> list)
        {
            //��������ģ��
            double[][] transModel = new double[reqs[index].nodes][sub.nodes];
            double[][] indexModel = new double[reqs[index].nodes][sub.nodes];
            int slotNum = -1;
            for(int i=0;i<reqs[index].nodes;i++){
                for(int j=0;j<sub.nodes;j++){
                    if(reqs[index].cpu[i] <= s2v_n[j].rest_cpu + Parameters.MIN_VALUE_DOUBLE){//�ײ�ڵ��CPU��������ڵ�
                        //slotNum = CheckIfSlotEnoughByNode(sub,j,reqs,index,i);
                        if( slotNum > -1){//�����ײ�ڵ�j�����ӵ���·Ƶ�ײ۴�������ڵ�i����Ĳ�
                            transModel[i][j] = 1.0/s2v_n[j].rest_cpu;//div(1.0,s2v_n[j].rest_cpu,10);//1.0/(1.0*s2v_n[j].rest_cpu);
                            indexModel[i][j] = slotNum;
                        } else {
                            transModel[i][j] = -1;//-1������ӳ��
                            indexModel[i][j] = -1;
                        }
                    } else {
                        transModel[i][j] = -1;//-1������ӳ��
                    }
                }
            }
            //��ʼ������
            int[] vnodeEmbed = new int[reqs[index].nodes];
            int[] snodeEmbed = new int[sub.nodes];
            for(int i=0; i<reqs[index].nodes; i++){
                vnodeEmbed[i] = 1;//1������δ���䣬0�����Ѿ�����
            }
            for(int i=0; i<sub.nodes; i++){
                snodeEmbed[i] = 1;//1������δ���䣬0�����Ѿ�����
            }

            int num = 0;
            int minIndexReq = -1;
            int minIndexSub = -1;
            double minElement = 100000;
            while(num < reqs[index].nodes){
                //Ѱ����СԪ�أ���������minIndexReq��minIndexSub
                for(int i=0;i<reqs[index].nodes;i++){
                    for(int j=0;j<sub.nodes;j++){
                        if(minElement>transModel[i][j] && transModel[i][j]>-1 && vnodeEmbed[i]==1 && snodeEmbed[j]==1){//vnodeEmbed[i] == 1��ʾ����ڵ�iδ��ӳ��
                            minIndexReq = i;
                            minIndexSub = j;
                            minElement = transModel[i][j];
                        }
                    }
                }
                if(minIndexReq > -1) return -1;//û���ҵ���СԪ��
            }

            //Ѱ����С����·Ƶ�ײ�
            //
            return -1;
        }


        //******************************************************************
        //���ƣ�int MapVONEByILPWangYTwoNodes(......)
        //���ܣ���ILPģ�ͣ�WangY��ӳ�����������, ����ɹ������s2v_n��v2s
        //������
        //	      s2v_nΪ����ڵ�ӳ�������ڵ����ݽṹ
        //	      s2v_lΪ������·ӳ��������·���ݽṹ
        //	      v2sΪ����ӳ��������������ݽṹ
        //	      indexΪ��index����������
        //����ֵ��0���ɹ����أ�-1��ʧ�ܷ���
        //******************************************************************
        private int MapVONEByILPWangYPlus3Nodes(EOSubstrateNetwork sub,VONRequest reqs[],int index,int ret[],int p[][],ArrayList<Object> list)
        {
            //��������ͼ
            AuxiliaryGraph auxGraph = new AuxiliaryGraph();
            auxGraph = CreateAuxiliaryDiagram(sub,reqs,index);

            //����k��·����·�����ȴ���2��
            WeightedDirectedGraph myGraph = new WeightedDirectedGraph(auxGraph.nodes,sub.nodes);
            myGraph.CreateDireGraph(auxGraph);//�����ڵ�
            //CreateEdgeFromAux(auxGraph,myGraph);

            int pathK = Parameters.K_PATH;//5;//ҪѰ�ҵ�K���·��
            int pathRet = -1;//��¼���ص����·������
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
                if(pathRet <= 0) return -1;//û��·��������ʧ��
                else {
                    for(int j=0;j<myGraph.kShortestPath.length;j++){
                        for(int k=0;k<myGraph.kShortestPath[j].length;k++){
                            kShortestPath[i][j][k] = myGraph.kShortestPath[j][k];
                        }
                    }
                    //kShortestPath[i] = myGraph.kShortestPath;//�ҵ���·����������
                    System.out.println("----------------print ret path.");
                    for(int k=0;k<pathRet;k++)
                        myGraph.displayPaths(myGraph.kShortestPath[k]);
                }
            }

            //���ÿ��·����ÿ�������slots�����Լ��Ƿ���Ч
            int[][] pathSlots = new int[reqs[index].links][Parameters.K_PATH];
            int[][] pathLength = new int[reqs[index].links][Parameters.K_PATH];
            double[][] pathLen = new double[reqs[index].links][Parameters.K_PATH];
            int[][] pathNo = new int[reqs[index].links][Parameters.K_PATH];

            CalculatePathSlotsAndEffects(auxGraph,kShortestPath,pathSlots,pathLength,pathNo,pathEff,reqs,index,pathLen);

            //��WangY��ILPģ����⣬���߸�����ǿ�Ļ���·����ӳ��ģ�����
            FindVONEWangYPlusByOne01ILP(auxGraph,kShortestPath,pathSlots,pathLength,pathNo,pathEff,reqs,index);

            //�����Ž�
            int retNodeE[],retLinkE[],retSlotSE[],retSlotEE[],retSlotBE[],retLinkMD[][];
            retNodeE = new int[reqs[index].nodes];
            retLinkE = new int[reqs[index].links];
            retSlotSE = new int[reqs[index].links];
            retSlotEE = new int[reqs[index].links];
            retSlotBE = new int[reqs[index].links];

            retLinkMD = new int[reqs[index].links][Parameters.K_PATH];

            if(FindVONEOptimalSolutionPlusWangY(auxGraph,retNodeE,retLinkE,retSlotSE,retSlotEE,retSlotBE,retLinkMD)){//�ҵ������Ž�
                //AddNodesMap(reqs,index,list);//�ڵ�ӳ��
                //for(int i=0;i<reqs[index].links;i++){
                //	System.out.println("link "+i+" is embedded the path:"+retLinkE[i]);
                //}
                if(Parameters.DebugModel == true){
                    String str = "\r\nretLinkMD[][]=\r\n";
                    for(int i=0;i<reqs[index].links;i++){
                        for(int j=0;j<Parameters.K_PATH;j++){
                            if(retLinkMD[i][j] == 1){
                                str += i+" "+j+" 1\r\n";
                            }
                        }
                    }
                    WriteFilePlus("process.txt",str);
                }

                //��⹲����·�Ķ��·���Ƿ����slot��ͻ�������ͻ������false�����򣬷���true
                //boolean CheckPathSlotsIfEff(VONRequest reqs[],int index,DistanceParent[][][]  kShortestPath,int p[][],int virtualNodes[],int retLinkE[],int retSlotSE[],int retSlotEE[])
                if(CheckPathSlotsIfEff(reqs,index,kShortestPath,p,auxGraph.virtualNodes,retLinkE,retSlotSE,retSlotEE) == false) {
                    //System.out.println("������ĳ����·�У�����Ƶ�ײ۳�ͻ");
                    String data = index + " ����Ƶ�ײ۳�ͻ\r\n";
                    Tools myDowith = new Tools();
                    myDowith.SaveFile("EmbedOutput.dat", data, true);
                    return -1;
                }
                //System.out.println("������Ƶ�ײ۳�ͻ");
                PrintPath(reqs,index,kShortestPath,p,auxGraph.virtualNodes,pathEff,retLinkE,retSlotSE,retSlotEE);

                AddNodesMap(reqs,index,retNodeE);//�ڵ�ӳ��
                AddLinksMapByMIPWangYPlus(sub,reqs,index,retSlotSE,retSlotEE,retLinkE,kShortestPath,pathEff,pathNo,auxGraph.virtualNodes,retNodeE);
                //AddLinksMapByMIP(sub,reqs,i,ret,p);	//��·ӳ��
                return 0;//�ɹ��ҵ�VONE��
            }
            return -1;
        }


        //����:������������ӳ��
        //����:auxGraph������ͼ
        //    kShortestPath:ÿ������·�����·��
        //    pathSlots:·���������slots����
        //    pathLength:·���ĳ���
        //    pathNo:·���ı��
        //    int[] pathEff:��Ч·������
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
                    if(pathLength[i][j] > 2) data += " " + pathNo[i][j];
                }
                data += ";\r\n";
            }
            myDowith.SaveFile("glpsolRSA.dat", data, true);

            //set Nv:=4 5 7;/*����ڵ�ļ���*/
            data = "set Nv:=";
            for(int i = 0; i < reqs[index].nodes; i++){		//sub.nodes
                data += " " + auxGraph.virtualNodes[i];
            }
            data += ";\r\n";
            myDowith.SaveFile("glpsolRSA.dat", data, true);

            //set F:=0 1 2 3;/*�������ڵ㣨facility nodes���ļ���*/
            data = "set F:=";
            for(int i = 0; i < auxGraph.faNodesNum; i++){		//sub.nodes
                data += " " + auxGraph.faNodes[i];
            }
            data += ";\r\n";
            myDowith.SaveFile("glpsolRSA.dat", data, true);

            //set Na:=0 1 2 3 4 5 6 7;/*����ͼ�Ľڵ㼯�ϣ�Na=F��Nv������ڵ�*/
            data = "set Na:=";
            for(int i = 0; i < auxGraph.nodes; i++){		//sub.nodes
                data += " " + i;
            }
            data += ";\r\n";
            myDowith.SaveFile("glpsolRSA.dat", data, true);

            //set A[4]:=
            //4 0
            //4 2
            //;/*��ڵ㣨facility nodes��u�ĸ����ߵļ���*/
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
            //;/*������ڵ㣨facility nodes��u�ĸ����ߵļ���*/
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

            //set Af[4]:=0 2;/*ÿ������ڵ��ӳ��Ľڵ㼯��*/
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

            //set FS[0,0]:=0 1 2 3 4 5 6 7 8 9;/*·��p�Ͽ��ܵ���ʼƵ�ײ���������*/
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
            //;/*��������ڵ�ĸ����ߵļ���*/
            data = "set Ef:=\r\n";
            for(int i = 0; i < auxGraph.virtServLinks.length; i++){
                data += auxGraph.virtServLinks[i].from + " " + auxGraph.virtServLinks[i].to + "\r\n";
            }
            data += ";\r\n";
            myDowith.SaveFile("glpsolRSA.dat", data, true);

            //set D:=0 1 2;/*������·����*/
            //set DNo[0]:=1 2;/*������·����*/
            //set DNo[1]:=0 2;/*������·����*/
            //set DNo[2]:=0 1;/*������·����*/
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
            /*Du{u in Nv};��������ڵ�u��������·����
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
            //;/*����������·��p�������Ƶ�ײ�����*/
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
            //		;/*·��p������*/
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
            //		;/*������·(u,v)��·������*/
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
            /*param Sita{p in P,(u,v) in Ef}, binary;�����Ʊ�����*/
            data = "param Sita:=\r\n";
            for (int i = 0; i < reqs[index].links; i++) {
                for(int j = 0; j < pathEff[i]; j++)
                {
                    if(pathNo[i][j] > -1) {
                        for(int k = 0; k < auxGraph.virtServLinks.length; k++){
                            int incl = IncludeLinkInPath(auxGraph,kShortestPath,pathNo,i,j,k,reqs,index);
                            if(incl > -1){//˵������
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

            /*param fs{d in D,p in P,i in MSet};·��p�ϵĵ�i����ʼƵ�ײ�����*/
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
                            data += i + " " + pathNo[i][j] + " " + k + " " + effeSlot + "\r\n";
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
            //param MSlots:=9;/*����Ƶ�ײ�����*/
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
                Process process = null;
                if(embedModelOrAlgo == Parameters.MapVONE3ByWangY){
                    process = Runtime.getRuntime().exec("cmd /c C:/������/VONE/VONE/glpk-4.60/w64/glpsol.exe -m C:/������/VONE/VONE/glpk-4.60/w64/glpsolMILVONE3NodesNoDataWangY.mod -d glpsolRSA.dat -o glpsolRSA.o");
                } else if(embedModelOrAlgo == Parameters.MapVONE3PByWangYAndChenxh){
                    process = Runtime.getRuntime().exec("cmd /c C:/������/VONE/VONE/glpk-4.60/w64/glpsol.exe -m C:/������/VONE/VONE/glpk-4.60/w64/glpsolMILPVONE3NodesNoDataWangY.mod -d glpsolRSA.dat -o glpsolRSA.o");
                }
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









        //����ÿ��·������Ҫ��slots����
        //����ֵ��<=0��ʾʧ�ܣ�
        private int GetSlotNumByOnePath(AuxiliaryGraph auxGraph,DistanceParent[] shortestPath,int pathNum,int sNode1,int sNode2,double bw)
        {
            //�ҵ���·����slots������[BWd/(12.5*mp)],mp=1,2,3,4,BPSK:3000km,QPSK:1500km,8QAM:750km,16QAM:375km
            int sNode3 = sNode2;
            int sNode4 = sNode2;
            int linkNo = -1;
            int mp = -1;
            if(shortestPath[sNode2] == null) return 0;
            int pathLength = 0;
            while(shortestPath[sNode2].parentVert != sNode1){
                sNode3 = sNode2;
                sNode2 = shortestPath[sNode2].parentVert;
                if(sNode3 == sNode4) continue;
                linkNo = GetLinkNum(auxGraph,sNode3,sNode2);
                pathLength += auxGraph.link[linkNo].length;
                //if(auxGraph.link[linkNo].length <= 375 && mp < 4) mp = 4;
                //else if(auxGraph.link[linkNo].length <= 750 && mp < 3) mp = 3;
                //else if(auxGraph.link[linkNo].length <= 1500 && mp < 2) mp = 2;
                //else if(auxGraph.link[linkNo].length <= 3000 && mp < 1) mp = 1;
            }
            if(pathLength <= 375) mp = 4;
            else if(pathLength <= 750) mp = 3;
            else if(pathLength <= 1500) mp = 2;
            else if(pathLength <= 3000) mp = 1;
            else return 0;//����3000km�����޷�����
            int slotNum = (int) (Math.floor(bw/(12.5*mp))+1);
            return slotNum;
        }


        //����K���·���ı�
        private void CreateEdgeFromAux(AuxiliaryGraph auxGraph,WeightedDirectedGraph myGraph)
        {
            //���ӱ�
            for(int i=0; i<auxGraph.links; i++){
                myGraph.addEdge(auxGraph.link[i].from,auxGraph.link[i].to,auxGraph.link[i].bw);
                myGraph.addEdge(auxGraph.link[i].to,auxGraph.link[i].from,auxGraph.link[i].bw);
            }
        }




        //******************************************************************
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
        //�������ڣ�2017-08-18
        //******************************************************************
        private boolean FindVONEOptimalSolution(int ret[],int p[],ArrayList<Object> list)
        {
            BufferedReader reader = null;
            int minLength = -1;
            int minSlotIndex,maxSlotIndex;
            int hashFirst = -1;//·���ĵ�һ����ʶ������1->9->0�����hashFirst=-1����sNode1=1��
            int keyVNode1 = -1,keyVNode2 = -1;//keyVNode1->s;keyVNode2->t
            int sNode1 = -1,sNode2 = -1;
            minSlotIndex = 1000;
            maxSlotIndex = -1;
            try {
                    System.out.println("����Ϊ��λ��ȡ�ļ����ݣ�һ�ζ�һ���У�");
                    reader = new BufferedReader(new FileReader("glpsolRSA.o"));
                    String tempString = null;
                    Hashtable hashResolve = null;//��Ž��HashTable
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
                            tempString = tempString.replace("Objective:  shPath = ", "");
                            tempString = tempString.replace("(MINimum)", "");
                            tempString = tempString.trim();
                            minLength = Integer.parseInt(tempString);
                            hashResolve = new Hashtable(minLength,(float)1.0);//����hash
                        }
                        if(line > 6 && tempString.indexOf(" x[") != -1){//˵���ҵ������Ž��x����
                            //ȥ��ǰ��Ĳ��֣�3 x[0,2]       *              1             0             1
                            //�Կո�ָ���ȡ��һ������
                            String tmpStr = "";
                            //System.out.println("line " + line + ": " + tempString);

                            String tempString1 = reader.readLine();
                            //System.out.println(tempString1);

                            tmpStr = tempString1.substring(tempString1.indexOf("*")+1);
                            tmpStr = tmpStr.trim();
                            //System.out.println("line " + line + ": " + tmpStr);

                            tmpStr = tmpStr.substring(0,tmpStr.indexOf(" "));
                            //System.out.println("line " + line + ": " + tmpStr);
                            if(Integer.parseInt(tmpStr) == 1){//˵���ҵ���һ����
                                //�õ�һ���⸳ֵ��tmpStr������x[0,2]
                                tempString = tempString.trim();
                                tmpStr = tempString.substring(tempString.indexOf(" ")+1);
                                //System.out.println("line " + line + ": " + tmpStr);
                                //tmpStr = tmpStr.substring(0,tmpStr.indexOf(" "));
                                //System.out.println("line " + line + ": " + tmpStr);

                                //x[0,6,0,1,7,6],����s��keyVNode1ӳ��Ľڵ㣬t��keyVNode2ӳ��Ľڵ�
                                int keySNode1 = -1,keySNode2 = -1;
                                keySNode1 = Integer.parseInt(tmpStr.substring(tmpStr.indexOf("[")+1, tmpStr.indexOf(",")));
                                //System.out.println("keyNode1:"+keySNode1);
                                tmpStr = tmpStr.substring(tmpStr.indexOf(",")+1);
                                keySNode2 = Integer.parseInt(tmpStr.substring(0, tmpStr.indexOf(",")));
                                //System.out.println("keyNode2:"+keySNode2);

                                tmpStr = tmpStr.substring(tmpStr.indexOf(",")+1);
                                keyVNode1 = Integer.parseInt(tmpStr.substring(0, tmpStr.indexOf(",")));
                                tmpStr = tmpStr.substring(tmpStr.indexOf(",")+1);
                                keyVNode2 = Integer.parseInt(tmpStr.substring(0, tmpStr.indexOf(",")));

                                //tmpStr = tmpStr.substring(tmpStr.indexOf(",")+1);
                                //s = Integer.parseInt(tmpStr.substring(0, tmpStr.indexOf(",")));
                                //tmpStr = tmpStr.substring(tmpStr.indexOf(",")+1);
                                //t = Integer.parseInt(tmpStr.substring(0, tmpStr.indexOf("]")));

                                //System.out.println("keyNode2:"+keyNode2);
                                //hashResolve.put(keySNode1+"-"+keySNode2,keyVNode1+"-"+keyVNode2+":"+s+"-"+t);//�Ᵽ����hash����
                                hashResolve.put(keySNode1,keySNode2);//�Ᵽ����hash����

                                //if(hashFirst == -1){
                                //	sNode1 = keySNode1;//·���ĵ�һ���ڵ�
                                //	hashFirst = 0;
                                //}
                                //sNode2 = keySNode2;//·���������һ���ڵ�
                            }
                        } else if(line > 6 && tempString.indexOf(" f[") != -1){//˵���ҵ������Ž��x����
                            String tmpStr = "";
                            //System.out.println("line " + line + ": " + tempString);
                            String tempString1 = reader.readLine();

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
                                tmpStr = tmpStr.substring(tmpStr.indexOf(",")+1);		//�õ�j,m,n,s,t,k]
                                tmpStr = tmpStr.substring(tmpStr.indexOf(",")+1);		//�õ�m,n,s,t,k]
                                tmpStr = tmpStr.substring(tmpStr.indexOf(",")+1);		//�õ�n,s,t,k]
                                tmpStr = tmpStr.substring(tmpStr.indexOf(",")+1);		//�õ�s,t,k]
                                tmpStr = tmpStr.substring(tmpStr.indexOf(",")+1);		//�õ�t,k]
                                keyNode1 = Integer.parseInt(tmpStr.substring(tmpStr.indexOf(",")+1, tmpStr.indexOf("]")));//�õ�f[i,j,m,n,s,t,k]��k

                                if(minSlotIndex > keyNode1) minSlotIndex = keyNode1;//minSlotIndex��С��slot����
                                if(maxSlotIndex < keyNode1) maxSlotIndex = keyNode1;//maxSlotIndex����slot����
                            }
                        } else if(line > 6 && tempString.indexOf(" y[") != -1){//˵���ҵ������Ž��x����
                            String tmpStr = "";
                            //System.out.println("line " + line + ": " + tempString);
                            //String tempString1 = reader.readLine();

                            tmpStr = tempString.substring(tempString.indexOf("*")+1);
                            tmpStr = tmpStr.trim();
                            //System.out.println("line " + line + ": " + tmpStr);

                            tmpStr = tmpStr.substring(0,tmpStr.indexOf(" "));
                            //System.out.println("line " + line + ": " + tmpStr);
                            if(Integer.parseInt(tmpStr) == 1){//˵���ҵ���һ����
                                //�õ�һ���⸳ֵ��tmpStr������ 38786 y[0,7]       *              1             0             1
                                //var f{(i,j) in E,(m,n) in Ev,s in Vf,t in Vf,k in MSet}, binary;
                                tempString = tempString.trim();
                                tmpStr = tempString.substring(tempString.indexOf(" ")+1);//ȥ��ǰ����к�
                                //System.out.println("line " + line + ": " + tmpStr);
                                //tmpStr = tmpStr.substring(0,tmpStr.indexOf(" "));		//�õ�f[i,j,m,n,s,t,k]
                                //System.out.println("line " + line + ": " + tmpStr);
                                int keyNode1 = -1;
                                //keyNode1 = Integer.parseInt(tmpStr.substring(tmpStr.indexOf("[")+1, tmpStr.indexOf(",")));//�õ�y[i,j]��i
                                //System.out.println("keyNode1:"+keyNode1);
                                keyNode1 = Integer.parseInt(tmpStr.substring(tmpStr.indexOf(",")+1, tmpStr.indexOf("]")));//�õ�f[i,j]��j

                                if(hashFirst == -1){
                                    sNode1 = keyNode1;//·���ĵ�һ���ڵ�
                                    hashFirst = 0;
                                } else {
                                    sNode2 = keyNode1;//·���������һ���ڵ�
                                }
                            }
                        }
                        line++;
                    }
                    reader.close();

                    //����·��
                    int node1 = sNode1,node2;
                    //System.out.println(""+sNode1+"->"+sNode2);
                    for(int i = 0; i < minLength; i++){
                        //System.out.println(node1+":"+hashResolve.get(node1).toString());
                        node2 = Integer.parseInt(hashResolve.get(node1).toString());
                        p[node1] = node2;
                        //System.out.println("p["+node1+"]:"+p[node1]);
                        node1 = node2;
                    }
                    p[sNode2] = -1;

                    //ret[0]=snode1(��vnode1ӳ�������ڵ�)
                    //	      ret[1]=snode2(��vnode2ӳ�������ڵ�)
                    //        ret[2]=minSlotIndex(��Ƶ�ײ۵ĵ�λ)
                    //        ret[3]=maxSlotIndex(��Ƶ�ײ۵ĸ�λ)
                    ret[0] = minSlotIndex;
                    ret[1] = maxSlotIndex;
                    list.add(sNode1);
                    list.add(sNode2);
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

        //******************************************************************
        //���ƣ�void FindVONEByOne01ILP(......)
        //���ܣ�ͨ��0-1-ILP��GLPK�㷨�ҵ��������·����VONEӳ���
        //������
        //	      sNode1ΪԴ��
        //	      sNode2Ϊ���
        //	      speedΪ��������������ٶ�
        //	      subΪ��������
        //����ֵ��
        //******************************************************************
        private void FindVONEByOne01ILP(EOSubstrateNetwork sub, VONRequest reqs[],int index)
        {
            int M = sub.slotsNum;

            Tools myDowith = new Tools();

            String data;
            //set V:= 0 1 2 3 4 5 6;
            data = "set V:=";
            for(int i = 0; i < sub.nodes; i++){		//sub.nodes
                data += " " + i;
            }
            data += ";\r\n";
            myDowith.SaveFile("glpsolRSA.dat", data, false);

            //set AlloSlotsSet:= 2 3;
            //data = "set AlloSlotsSet:=";
            //for(int i = startSlotNum; i < endSlotNum; i++){
            //	data += " " + i;
            //}
            //data += ";\r\n";
            //data = "set AlloSlotsSet:=" + startSlotNum + " " + endSlotNum + ";\r\n";
            //myDowith.SaveFile("glpsolRSA.dat", data, true);

            //set E:=
            data = "set E:=\r\n";
            for(int i = 0; i < sub.links; i++){
                data += sub.link[i].from + " " + sub.link[i].to + "\r\n";
                data += sub.link[i].to + " " + sub.link[i].from + "\r\n";
                //if(sub.link[i].from != S || sub.link[i].to != T)
                //	data += sub.link[i].to + " " + sub.link[i].from + "\r\n";
            }
            data += ";\r\n";
            myDowith.SaveFile("glpsolRSA.dat", data, true);

            data = "set Vv:=";
            for (int j = 0; j < reqs[index].nodes; j++) {
                data += " " + j;
            }
            data += ";\r\n";
            myDowith.SaveFile("glpsolRSA.dat", data, true);

            //set Vf:=6 7 8 9;faNodes
            data = "set Vf:=";
            for (int j = 0; j < sub.faNodes.length; j++) {
                data += " " + sub.faNodes[j];
            }
            data += ";\r\n";
            myDowith.SaveFile("glpsolRSA.dat", data, true);

            //set Vnf:=0 1 2 3 4 5;
            data = "set Vnf:=";
            for (int j = 0; j < sub.nodes-sub.faNodes.length; j++) {
                data += " " + j;
            }
            data += ";\r\n";
            myDowith.SaveFile("glpsolRSA.dat", data, true);

            //set Ev:=
            //0 1
            //;
            data = "set Ev:=\r\n";
            data += "0 1\r\n";
            data += ";\r\n";
            myDowith.SaveFile("glpsolRSA.dat", data, true);

            //param M:=10;
            //set S:= 0;
            //set T:= 6;
            //param B:=
            //		0 1 2
            //		;
            data = "param B:=\r\n";
            for (int j = 0; j < reqs[index].links; j++) {
                data += reqs[index].link[j].from + " " + reqs[index].link[j].to + " " + (int)Math.floor(reqs[index].link[j].speed/75);//reqs[index].link[j].speed;
            }
            data += ";\r\n";
            myDowith.SaveFile("glpsolRSA.dat", data, true);

            //param Cv:=
            //0 1
            //1 1
            //;
            data = "param Cv:=\r\n";
            for (int j = 0; j < reqs[index].nodes; j++) {
                data += j + " " + reqs[index].cpu[j] + "\r\n";
            }
            data += ";\r\n";
            myDowith.SaveFile("glpsolRSA.dat", data, true);

            //param Cs:=
            //		6 2
            //		7 2
            //		8 2
            //		9 2
            //		;
            data = "param Cs:=\r\n";
            for (int j = 0; j < sub.faNodes.length; j++) {
                //data += sub.faNodes[j] + " " + sub.cpu[sub.faNodes[j]] + "\r\n";
                data += sub.faNodes[j] + " " + s2v_n[sub.faNodes[j]].rest_cpu + "\r\n";
                //s2v_n[snode].rest_cpu
            }
            data += ";\r\n";
            myDowith.SaveFile("glpsolRSA.dat", data, true);

            //set MSet:=0 1 2 3 4 5;
            //set MBSet:=0 1 2 3 4;
            //set MSet:= 0 1 2 3 4 5 6 7 8 9; /*MSet={0,1,2,��,M-1}*/
            data = "set MSet:=";
            for(int i = 0; i < M; i++){
                data += " " + i;
            }
            data += ";\r\n";
            //myDowith.SaveFile("glpsolRSA.dat", data, true);

            //set MBSet:=0 1 2 3 4 5 6 7 8;/*BSet={0,1,2,��,M-B}*/
            data += "set MBSet:=";
            //slotNum = (int)Math.floor(reqs[index].link[0].speed/75)+1;
            //for(int i = 0; i <= M-(int)Math.floor(reqs[index].link[0].speed/75)-1; i++){
            for(int i = 0; i <= M-2; i++){
                data += " " + i;
            }
            data += ";\r\n";
            myDowith.SaveFile("glpsolRSA.dat", data, true);


            //set BSet:=0 1;/*BSet={0,1,2,��,B-1}*/��y���ʹ��
            //data += "set BSet:=";
            //for(int i = 0; i < B; i++){
            //	data += " " + i;
            //}
            //data += ";\r\n";
            //myDowith.SaveFile("glpsolRSA.dat", data, true);

            //set BMatch[0,1,0,1,0]:=0 1;
            //set BMatch[0,1,0,1,1]:=1 2;
            //set BMatch[0,1,0,1,2]:=2 3;
            //set BMatch[0,1,0,1,3]:=3 4;
            //set BMatch[0,1,0,1,4]:=4 5;
            //set BMatch[0,1,0,1,5]:=;
            //data = "";

            for(int i = 0; i < M; i++){
                for(int j = 0; j < reqs[index].links; j++) {
                    for(int k=0; k < sub.links; k++){
                        data = "";
                        data += "set BMatch[" + sub.link[k].from + "," +  sub.link[k].to + "," + reqs[index].link[j].from + "," + reqs[index].link[j].to + "," + i + "] :=";
                        for(int m = i; m < i+(int)Math.floor(reqs[index].link[j].speed/75) && m < M; m++){
                            data += " " + m;
                        }
                        data += ";\r\n";

                        data += "set BMatch[" + sub.link[k].to + "," +  sub.link[k].from + "," + reqs[index].link[j].from + "," + reqs[index].link[j].to + "," + i + "] :=";
                        for(int m = i; m < i+(int)Math.floor(reqs[index].link[j].speed/75) && m < M; m++){
                            data += " " + m;
                        }
                        data += ";\r\n";
                        myDowith.SaveFile("glpsolRSA.dat", data, true);
                    }
                }
            }
            //set PMSet[1,0,0,1,0]:=2 3 4 5;
            //set PMSet[1,0,0,1,1]:=3 4 5;
            //set PMSet[1,0,0,1,2]:=4 5;
            //set PMSet[1,0,0,1,3]:=5;
            //set PMSet[1,0,0,1,4]:=;
            //set PMSet[1,0,0,1,5]:=;
            //PMSet[i]
            for(int i = 0; i < M; i++){
                for(int j = 0; j < reqs[index].links; j++) {
                    for(int k=0; k < sub.links; k++){
                        data = "";
                        data += "set PMSet[" + sub.link[k].from + "," +  sub.link[k].to + "," + reqs[index].link[j].from + "," + reqs[index].link[j].to + "," + i + "] :=";
                        //for(int ii=i+(int)Math.floor(reqs[index].link[j].speed/75); ii < M; ii++){
                        for(int ii=i; ii < M; ii++){
                            data += ii + " ";
                        }
                        data += ";\r\n";

                        //data = "";
                        data += "set PMSet[" + sub.link[k].to + "," +  sub.link[k].from + "," + reqs[index].link[j].from + "," + reqs[index].link[j].to + "," + i + "] :=";
                        //for(int ii=i+(int)Math.floor(reqs[index].link[j].speed/75); ii < M; ii++){
                        for(int ii=i; ii < M; ii++){
                            data += ii + " ";
                        }
                        data += ";\r\n";

                        myDowith.SaveFile("glpsolRSA.dat", data, true);
                    }
                }
            }

            //�ڵ����㼯��I[i]
            for(int i = 0; i < sub.nodes; i++){
                data = "";
                data += "set I[" + i + "] :=";
                for(int j = 0; j < sub.links; j++){
                    if(sub.link[j].to == i){
                        data += " " + sub.link[j].from;
                    }
                    if(sub.link[j].from == i){
                        data += " " + sub.link[j].to;
                    }
                }
                data += ";\r\n";
                myDowith.SaveFile("glpsolRSA.dat", data, true);
            }
            //�ڵ�ĳ��㼯��O[i]
            for(int i = 0; i < sub.nodes; i++){
                data = "";
                data += "set O[" + i + "] :=";
                for(int j = 0; j < sub.links; j++){
                    if(sub.link[j].to == i){
                        data += " " + sub.link[j].from;
                    }
                    if(sub.link[j].from == i){
                        data += " " + sub.link[j].to;
                    }
                }
                data += ";\r\n";
                myDowith.SaveFile("glpsolRSA.dat", data, true);
            }

            //param e:=
            //0 1 0 1
            //0 1 1 0
            //0 1 2 1
            //;
            //end;
            data = "param e:=\r\n";
            myDowith.SaveFile("glpsolRSA.dat", data, true);
            for(int i=0;i<sub.links;i++){
                for(int j=0;j<sub.slotsNum;j++){
                    int k = 1-sub.slots[i][j];
                    data = (sub.link[i].from + " " + sub.link[i].to + " " + j + " " + k + "\r\n");
                    data += (sub.link[i].to + " " + sub.link[i].from + " " + j + " " + k + "\r\n");
                    //sub.slots[i][j] = 1;//����Ϊ1��ռ��Ϊ0
                    myDowith.SaveFile("glpsolRSA.dat", data, true);
                }
            }
            data = ";\r\n";
            data += "end;\r\n";
            myDowith.SaveFile("glpsolRSA.dat", data, true);

            System.out.println("Done");

            try {
                String s;
                //Process process = Runtime.getRuntime().exec("cmd /c E:/�������⻯/��ԴJavaƵ�׷���/winglpk-4.60/glpk-4.60/w64/glpsol.exe -m E:/�������⻯/��ԴJavaƵ�׷���/winglpk-4.60/glpk-4.60/w64/glpsolRSA.mod -d E:/�������⻯/��ԴJavaƵ�׷���/winglpk-4.60/glpk-4.60/w64/glpsolRSA.dat -o glpsolRSA.o");
                Process process = Runtime.getRuntime().exec("cmd /c C:/������/VONE/VONE/glpk-4.60/w64/glpsol.exe -m C:/������/VONE/VONE/glpk-4.60/w64/glpsol01ILPVONE2NodesCXH.mod -d glpsolRSA.dat -o glpsolRSA.o");
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
        //******************************************************************
        //���ƣ�int PreMapNodeRandom(......)
        //���ܣ�������㷨ӳ��ڵ㣨��Ҫ����RSA��, ����ɹ������s2v_n��v2s
        //������
        //	      s2v_nΪ����ڵ�ӳ�������ڵ����ݽṹ
        //	      s2v_lΪ������·ӳ��������·���ݽṹ
        //	      v2sΪ����ӳ��������������ݽṹ
        //	      indexΪ��index����������
        //����ֵ��0���ɹ����أ�-1��ʧ�ܷ���
        //******************************************************************
        private int PreMapNodeByData(int index,ArrayList<Object> list)
        {
            BufferedReader reader = null;
            int minLength = -1;
            try {
                    System.out.println("����Ϊ��λ��ȡ�ļ����ݣ�һ�ζ�һ���У�"+index);
                    reader = new BufferedReader(new FileReader("nodeEmbed.dat"));
                    String tempString = null;
                    //Hashtable hashResolve = null;//��Ž��HashTable
                    int line = 1;
                    //һ�ζ���һ�У�ֱ������nullΪ�ļ�����
                    while ((tempString = reader.readLine()) != null) {
                        if(line == index*2+1) {//˵���ҵ��˸����������ӳ����
                            String zeroStr,oneStr;
                            zeroStr = tempString;
                            oneStr = reader.readLine();
                            int zeroNode = Integer.parseInt(zeroStr.substring(zeroStr.lastIndexOf(" ")+1));
                            int oneNode = Integer.parseInt(oneStr.substring(oneStr.lastIndexOf(" ")+1));
                            list.add(zeroNode);
                            list.add(oneNode);
                            break;
                        }
                        line++;
                    }
                    reader.close();
                    v2s[index].map = Parameters.STATE_MAP_NODE;
                    return 0;

             } catch (IOException e) {
                    e.printStackTrace();
                    return -1;
             } finally {
                    if (reader != null) {
                        try {
                            reader.close();
                        } catch (IOException e1) {

                        }
                    }
            }
        }



        //******************************************************************
        //���ƣ�int PreMapNodeRandom(......)
        //���ܣ�������㷨ӳ��ڵ㣨��Ҫ����RSA��, ����ɹ������s2v_n��v2s
        //������
        //	      s2v_nΪ����ڵ�ӳ�������ڵ����ݽṹ
        //	      s2v_lΪ������·ӳ��������·���ݽṹ
        //	      v2sΪ����ӳ��������������ݽṹ
        //	      indexΪ��index����������
        //����ֵ��0���ɹ����أ�-1��ʧ�ܷ���
        //******************************************************************
        private int PreMapNodeRandom(EOSubstrateNetwork sub,VONRequest reqs[],int index,ArrayList<Object> list)
        {
            //int snode = 0;
            Tools myTools = new Tools();
            //ArrayList list = new ArrayList();

            myTools.CreateDiffRandom(0, sub.nodes-1, list);
            //System.out.println(list);

            //AddNodesMap(reqs,index,list);
            v2s[index].map = Parameters.STATE_MAP_NODE;
            return 0;
        }
        //******************************************************************
        //���ƣ�int PreMapLinkByFA(......)//PreMapLinkShape
        //���ܣ���Fragmentation-aware RSA�㷨ӳ����·, ����ɹ������s2v_n��v2s
        //������
        //	      s2v_nΪ����ڵ�ӳ�������ڵ����ݽṹ
        //	      s2v_lΪ������·ӳ��������·���ݽṹ
        //	      v2sΪ����ӳ��������������ݽṹ
        //	      indexΪ��index����������
        //����ֵ��0���ɹ����أ�-1��ʧ�ܷ���
        //******************************************************************
        private int PreMapLinkByFA(EOSubstrateNetwork sub,VONRequest reqs[],int index,int p[][],int ret[][],ArrayList<Object> list)
        {
            //Ԥ����
            //���Ƶ�ǰ������������·��״̬s2v_l
            //S2VLink s2v_l_c[] = new S2VLink[reqs[index].links];
            //s2v_l_c = s2v_l;
            EOSubstrateNetwork subCopy = new EOSubstrateNetwork();
            subCopy = sub;

            //int p[][] = new int[reqs[index].links][sub.nodes];
            //int ret[][] = new int[reqs[index].links][2];//ret[][0]:���ص���ʼƵ�ײۣ�ret[][1]:���ص�Ƶ�ײ�����
            for(int i=0;i<reqs[index].links;i++){
                //�ҵ���i��������·�������˵�(vNode1,vNode2)��ӳ�������ڵ�(sNode1,sNode2,speed)
                int vNode1,vNode2,sNode1,sNode2;
                vNode1 = reqs[index].link[i].from;
                vNode2 = reqs[index].link[i].to;
                sNode1 = (int)list.get(vNode1);//v2s[index].snode.get(vNode1);
                sNode2 = (int)list.get(vNode2);//v2s[index].snode.get(vNode2);
                double speed;
                speed = reqs[index].link[i].speed;

                //�ҵ����ʵ���··��������ҵ�·������Ԥ����(������sub_copy)�����򣬷���-1
                if(FindPathByFA(subCopy,sNode1,sNode2,speed,sub.diffSlot,ret[i],p[i]) == 1){//�ҵ�·��
                    //����sub_copy,ret[][0]:���ص���ʼƵ�ײۣ�ret[][1]:���ص�Ƶ�ײ�������p[]:·��
                    System.out.println("The "+ index +" req.link["+i+"] succeeds to find the path."+sNode1+"-"+sNode2);
                    //PrintPath(reqs,index,i,p[i],sNode1,sNode2);
                    //UpdateSub(subCopy,sNode1,sNode2,ret[i],p[i]);
                    UpdateSub(subCopy,sNode2,sNode1,ret[i],p[i]);
                } else {//δ�ҵ�·��
                    System.out.println("The "+ index +" req.link["+i+"] fails to find the path.");
                    return -1;
                }
            }
            return 0;
        }


        //******************************************************************
        //���ƣ�int PreMapLinkBySPFA(......)//PreMapLinkShape
        //���ܣ������·���㷨ӳ����·, ����ɹ������s2v_n��v2s
        //������
        //	      s2v_nΪ����ڵ�ӳ�������ڵ����ݽṹ
        //	      s2v_lΪ������·ӳ��������·���ݽṹ
        //	      v2sΪ����ӳ��������������ݽṹ
        //	      indexΪ��index����������
        //        int p[][] = new int[reqs[index].links][sub.nodes];
        //        int ret[][] = new int[reqs[index].links][2];//ret[][0]:���ص���ʼƵ�ײۣ�ret[][1]:���ص�Ƶ�ײ�����

        //����ֵ��0���ɹ����أ�-1��ʧ�ܷ���
        //******************************************************************
        private int PreMapLinkBySPFA(EOSubstrateNetwork sub,VONRequest reqs[],int index,int p[][],int ret[][],ArrayList<Object> list)
        {
            //Ԥ����
            //���Ƶ�ǰ������������·��״̬s2v_l
            //S2VLink s2v_l_c[] = new S2VLink[reqs[index].links];
            //s2v_l_c = s2v_l;
            EOSubstrateNetwork subCopy = new EOSubstrateNetwork();
            subCopy = sub;

            //int p[][] = new int[reqs[index].links][sub.nodes];
            //int ret[][] = new int[reqs[index].links][2];//ret[][0]:���ص���ʼƵ�ײۣ�ret[][1]:���ص�Ƶ�ײ�����
            for(int i=0;i<reqs[index].links;i++){
                //�ҵ���i��������·�������˵�(vNode1,vNode2)��ӳ�������ڵ�(sNode1,sNode2,speed)
                int vNode1,vNode2,sNode1,sNode2;
                vNode1 = reqs[index].link[i].from;
                vNode2 = reqs[index].link[i].to;
                sNode1 = (int)list.get(vNode1);//v2s[index].snode.get(vNode1);
                sNode2 = (int)list.get(vNode2);//v2s[index].snode.get(vNode2);
                double speed;
                speed = reqs[index].link[i].speed;

                //�ҵ����ʵ���··��������ҵ�·������Ԥ����(������sub_copy)�����򣬷���-1
                if(FindPathBySPFA(subCopy,sNode1,sNode2,speed,sub.diffSlot,ret[i],p[i]) == 1){//�ҵ�·��
                    //����sub_copy,ret[][0]:���ص���ʼƵ�ײۣ�ret[][1]:���ص�Ƶ�ײ�������p[]:·��
                    System.out.println("The "+ index +" req.link["+i+"] succeeds to find the path."+sNode1+"-"+sNode2);
                    //PrintPath(reqs,index,i,p[i],sNode1,sNode2);
                    //UpdateSub(subCopy,sNode1,sNode2,ret[i],p[i]);
                    UpdateSub(subCopy,sNode2,sNode1,ret[i],p[i]);
                } else {//δ�ҵ�·��
                    System.out.println("The "+ index +" req.link["+i+"] fails to find the path.");
                    return -1;
                }
            }
            return 0;
        }

        //******************************************************************
        //���ƣ�int PreMapLinkBySPFA(......)//PreMapLinkShape
        //���ܣ������·���㷨ӳ����·, ����ɹ������s2v_n��v2s
        //������
        //	      s2v_nΪ����ڵ�ӳ�������ڵ����ݽṹ
        //	      s2v_lΪ������·ӳ��������·���ݽṹ
        //	      v2sΪ����ӳ��������������ݽṹ
        //	      indexΪ��index����������
        //����ֵ��0���ɹ����أ�-1��ʧ�ܷ���
        //******************************************************************
        private int PreMapLinkBySPFAEnh(EOSubstrateNetwork sub,VONRequest reqs[],int index,int p[][],int ret[][],ArrayList<Object> list)
        {
            //Ԥ����
            //���Ƶ�ǰ������������·��״̬s2v_l
            //S2VLink s2v_l_c[] = new S2VLink[reqs[index].links];
            //s2v_l_c = s2v_l;
            EOSubstrateNetwork subCopy = new EOSubstrateNetwork();
            subCopy = sub;

            //int p[][] = new int[reqs[index].links][sub.nodes];
            //int ret[][] = new int[reqs[index].links][2];//ret[][0]:���ص���ʼƵ�ײۣ�ret[][1]:���ص�Ƶ�ײ�����
            for(int i=0;i<reqs[index].links;i++){
                //�ҵ���i��������·�������˵�(vNode1,vNode2)��ӳ�������ڵ�(sNode1,sNode2,speed)
                int vNode1,vNode2,sNode1,sNode2;
                vNode1 = reqs[index].link[i].from;
                vNode2 = reqs[index].link[i].to;
                sNode1 = (int)list.get(vNode1);//v2s[index].snode.get(vNode1);
                sNode2 = (int)list.get(vNode2);//v2s[index].snode.get(vNode2);
                double speed;
                speed = reqs[index].link[i].speed;

                //�ҵ����ʵ���··��������ҵ�·������Ԥ����(������sub_copy)�����򣬷���-1
                if(FindPathBySPFAEnh(subCopy,sNode1,sNode2,speed,sub.diffSlot,ret[i],p[i]) == 1){//�ҵ�·��
                    //����sub_copy,ret[][0]:���ص���ʼƵ�ײۣ�ret[][1]:���ص�Ƶ�ײ�������p[]:·��
                    System.out.println("The "+ index +" req.link["+i+"] succeeds to find the path."+sNode1+"-"+sNode2);
                    //PrintPath(reqs,index,i,p[i],sNode1,sNode2);
                    //UpdateSub(subCopy,sNode1,sNode2,ret[i],p[i]);
                    UpdateSub(subCopy,sNode2,sNode1,ret[i],p[i]);
                } else {//δ�ҵ�·��
                    System.out.println("The "+ index +" req.link["+i+"] fails to find the path.");
                    return -1;
                }
            }
            return 0;
        }
        //******************************************************************
        //���ƣ�int MapLinkShape(......)
        //���ܣ�����״��֪�㷨ӳ����·, ����ɹ������s2v_n��v2s
        //������
        //	      s2v_nΪ����ڵ�ӳ�������ڵ����ݽṹ
        //	      s2v_lΪ������·ӳ��������·���ݽṹ
        //	      v2sΪ����ӳ��������������ݽṹ
        //	      indexΪ��index����������
        //����ֵ��0���ɹ����أ�-1��ʧ�ܷ���
        //******************************************************************
        private int PreMapLinkShapeByMIPEnh(EOSubstrateNetwork sub,VONRequest reqs[],int index,int p[][],int ret[][],ArrayList<Object> list) throws IOException
        {
            //Ԥ����
            //���Ƶ�ǰ������������·��״̬s2v_l
            //S2VLink s2v_l_c[] = new S2VLink[reqs[index].links];
            //s2v_l_c = s2v_l;
            EOSubstrateNetwork subCopy = new EOSubstrateNetwork();
            subCopy = sub;

            //int p[][] = new int[reqs[index].links][sub.nodes];
            //int ret[][] = new int[reqs[index].links][2];//ret[][0]:���ص���ʼƵ�ײۣ�ret[][1]:���ص�Ƶ�ײ�����
            for(int i=0;i<reqs[index].links;i++){
                //�ҵ���i��������·�������˵�(vNode1,vNode2)��ӳ�������ڵ�(sNode1,sNode2,speed)
                int vNode1,vNode2,sNode1,sNode2;
                vNode1 = reqs[index].link[i].from;
                vNode2 = reqs[index].link[i].to;
                sNode1 = (int)list.get(vNode1);//v2s[index].snode.get(vNode1);
                sNode2 = (int)list.get(vNode2);//v2s[index].snode.get(vNode2);
                double speed;
                speed = reqs[index].link[i].speed;

                //�ҵ����ʵ���··��������ҵ�·������Ԥ����(������sub_copy)�����򣬷���-1
                if(FindPathByMIPEnh(subCopy,sNode1,sNode2,speed,sub.diffSlot,ret[i],p[i]) == 1){//�ҵ�·��
                    //����sub_copy,ret[][0]:���ص���ʼƵ�ײۣ�ret[][1]:���ص�Ƶ�ײ�������p[]:·��
                    System.out.println("The "+ index +" req.link["+i+"] succeeds to find the path."+sNode1+"-"+sNode2);
                    //PrintPath(reqs,index,i,p[i],sNode1,sNode2);
                    UpdateSub(subCopy,sNode1,sNode2,ret[i],p[i]);
                } else {//δ�ҵ�·��
                    System.out.println("The "+ index +" req.link["+i+"] fails to find the path.");
                    return -1;
                }
            }
            return 0;
        }

        //******************************************************************
        //���ƣ�int PreMapLinkShapeBy01ILP(......)
        //���ܣ���01ILPģ��ӳ����·, ����ɹ������s2v_n��v2s
        //������
        //	      s2v_nΪ����ڵ�ӳ�������ڵ����ݽṹ
        //	      s2v_lΪ������·ӳ��������·���ݽṹ
        //	      v2sΪ����ӳ��������������ݽṹ
        //	      indexΪ��index����������
        //����ֵ��0���ɹ����أ�-1��ʧ�ܷ���
        //******************************************************************
        private int PreMapLinkShapeBy01ILP(EOSubstrateNetwork sub,VONRequest reqs[],int index,int p[][],int ret[][],ArrayList list) throws IOException
        {
            //Ԥ����
            EOSubstrateNetwork subCopy = new EOSubstrateNetwork();
            subCopy = sub;

            for(int i=0;i<reqs[index].links;i++){
                //�ҵ���i��������·�������˵�(vNode1,vNode2)��ӳ�������ڵ�(sNode1,sNode2,speed)
                int vNode1,vNode2,sNode1,sNode2;
                vNode1 = reqs[index].link[i].from;
                vNode2 = reqs[index].link[i].to;
                sNode1 = (int)list.get(vNode1);//v2s[index].snode.get(vNode1);
                sNode2 = (int)list.get(vNode2);//v2s[index].snode.get(vNode2);
                double speed;
                speed = reqs[index].link[i].speed;

                //�ҵ����ʵ���··��������ҵ�·������Ԥ����(������sub_copy)�����򣬷���-1
                //if(FindPathByMIP(subCopy,sNode1,sNode2,speed,sub.diffSlot,ret[i],p[i]) == 1){//�ҵ�·��
                if(FindPathBy01ILP(subCopy,sNode1,sNode2,speed,sub.diffSlot,ret[i],p[i]) == 1){//�ҵ�·��
                    //����sub_copy,ret[][0]:���ص���ʼƵ�ײۣ�ret[][1]:���ص�Ƶ�ײ�������p[]:·��
                    System.out.println("The "+ index +" req.link["+i+"] succeeds to find the path."+sNode1+"-"+sNode2);
                    //PrintPath(reqs,index,i,p[i],sNode1,sNode2);
                    UpdateSub(subCopy,sNode1,sNode2,ret[i],p[i]);
                } else {//δ�ҵ�·��
                    System.out.println("The "+ index +" req.link["+i+"] fails to find the path.");
                    return -1;
                }
            }
            return 0;
        }


        //******************************************************************
        //���ƣ�int MapLinkShape(......)
        //���ܣ�����״��֪�㷨ӳ����·, ����ɹ������s2v_n��v2s
        //������
        //	      s2v_nΪ����ڵ�ӳ�������ڵ����ݽṹ
        //	      s2v_lΪ������·ӳ��������·���ݽṹ
        //	      v2sΪ����ӳ��������������ݽṹ
        //	      indexΪ��index����������
        //����ֵ��0���ɹ����أ�-1��ʧ�ܷ���
        //******************************************************************
        private int PreMapLinkShapeByMIP(EOSubstrateNetwork sub,VONRequest reqs[],int index,int p[][],int ret[][],ArrayList<Object> list) throws IOException
        {
            //Ԥ����
            //���Ƶ�ǰ������������·��״̬s2v_l
            //S2VLink s2v_l_c[] = new S2VLink[reqs[index].links];
            //s2v_l_c = s2v_l;
            EOSubstrateNetwork subCopy = new EOSubstrateNetwork();
            subCopy = sub;

            //int p[][] = new int[reqs[index].links][sub.nodes];
            //int ret[][] = new int[reqs[index].links][2];//ret[][0]:���ص���ʼƵ�ײۣ�ret[][1]:���ص�Ƶ�ײ�����
            for(int i=0;i<reqs[index].links;i++){
                //�ҵ���i��������·�������˵�(vNode1,vNode2)��ӳ�������ڵ�(sNode1,sNode2,speed)
                int vNode1,vNode2,sNode1,sNode2;
                vNode1 = reqs[index].link[i].from;
                vNode2 = reqs[index].link[i].to;
                sNode1 = (int)list.get(vNode1);//v2s[index].snode.get(vNode1);
                sNode2 = (int)list.get(vNode2);//v2s[index].snode.get(vNode2);
                double speed;
                speed = reqs[index].link[i].speed;

                //�ҵ����ʵ���··��������ҵ�·������Ԥ����(������sub_copy)�����򣬷���-1
                if(FindPathByMIP(subCopy,sNode1,sNode2,speed,sub.diffSlot,ret[i],p[i]) == 1){//�ҵ�·��
                    //����sub_copy,ret[][0]:���ص���ʼƵ�ײۣ�ret[][1]:���ص�Ƶ�ײ�������p[]:·��
                    System.out.println("The "+ index +" req.link["+i+"] succeeds to find the path."+sNode1+"-"+sNode2);
                    //PrintPath(reqs,index,i,p[i],sNode1,sNode2);
                    UpdateSub(subCopy,sNode1,sNode2,ret[i],p[i]);
                } else {//δ�ҵ�·��
                    System.out.println("The "+ index +" req.link["+i+"] fails to find the path.");
                    return -1;
                }
            }
            return 0;
        }








        //����Sub���������
        //1������sub;2������S2VLink s2v_l[] = new S2VLink[reqs[index].links];
        //3������v2s
        private void AddLinksMapByMIP(EOSubstrateNetwork sub,VONRequest reqs[],int index,int ret[][],int p[][])
        {
            //1���ڵ�ӳ�䣬����sub.slots;
            for(int i=0;i<reqs[index].links;i++){
                int snode1,snode2,vnode1,vnode2;
                vnode1 = reqs[index].link[i].from;
                vnode2 = reqs[index].link[i].to;
                snode1 = v2s[index].snode.get(vnode1);
                snode2 = v2s[index].snode.get(vnode2);
                UpdateSub(sub,snode1,snode2,ret[i],p[i]);
            }

            //2������S2VLink s2v_l[]
            int snodeMid1,snodeMid,sNode1,req_count;
            for(int i=0;i<reqs[index].links;i++){
                //snodeMid1 = reqs[index].link[i].to;
                //sNode1 = reqs[index].link[i].from;
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

            //3������v2s[]����·ӳ����Ϣ
            int pathLength = 0;

            for(int i=0;i<reqs[index].links;i++){
                //snodeMid1 = reqs[index].link[i].to;
                //sNode1 = reqs[index].link[i].from;
                snodeMid1 = reqs[index].link[i].from;
                sNode1 = reqs[index].link[i].to;
                snodeMid1 = v2s[index].snode.get(snodeMid1);
                System.out.println("snodeMid1:"+snodeMid1);
                sNode1 = v2s[index].snode.get(sNode1);
                pathLength = 0;
                LinkedList<Integer> link = new LinkedList<Integer>();
                while(p[i][snodeMid1] != -1) {
                    snodeMid = p[i][snodeMid1];
                    link.add(pathLength,snodeMid1);
                    pathLength++;	//·������

                    snodeMid1 = snodeMid;
                    //if(snodeMid1 == sNode1) break;//�ӻ�㵽Դ���·���Ѿ�����
                }
                link.add(pathLength,snodeMid1);

                SpathFlow pathFlow = new SpathFlow();
                pathFlow.link = link;
                pathFlow.len = pathLength;
                System.out.println("vlink:"+i+" pathLength:"+pathLength);
                //snodeMid1 = reqs[index].link[i].to;
                snodeMid1 = reqs[index].link[i].from;
                snodeMid1 = v2s[index].snode.get(snodeMid1);
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
                v2s[index].slotNum.add(i,ret[i][1]);	//�����Ƶ������
            }
            //����ӳ��ɹ���־
            v2s[index].map = Parameters.STATE_MAP_LINK;
            reqs[index].map = Parameters.STATE_MAP_LINK;
        }





        //******************************************************************
        //���ƣ�bool FindPathByOneMIP(......)
        //���ܣ�ͨ��һ��MIP��GLPK�㷨�ҵ���̵�·��
        //������
        //	      sNode1ΪԴ��
        //	      sNode2Ϊ���
        //	      speedΪ��������������ٶ�
        //	      subΪ��������
        //����ֵ��1���ɹ����أ�-1��ʧ�ܷ���
        //******************************************************************
        private void FindPathByOneMIP(EOSubstrateNetwork sub, int startSlotNum,int endSlotNum, int B, int M, int S, int T)
        {
            Tools myDowith = new Tools();

            String data;
            //set V:= 0 1 2 3 4 5 6;
            data = "set V:=";
            for(int i = 0; i < sub.nodes; i++){		//sub.nodes
                data += " " + i;
            }
            data += ";\r\n";
            myDowith.SaveFile("glpsolRSA.dat", data, false);

            //set AlloSlotsSet:= 2 3;
            data = "set AlloSlotsSet:=";
            for(int i = startSlotNum; i < endSlotNum; i++){
                data += " " + i;
            }
            data += ";\r\n";
            //data = "set AlloSlotsSet:=" + startSlotNum + " " + endSlotNum + ";\r\n";
            //myDowith.SaveFile("glpsolRSA.dat", data, true);

            //set E:=
            data += "set E:=\r\n";
            for(int i = 0; i < sub.links; i++){
                data += sub.link[i].from + " " + sub.link[i].to + "\r\n";
                data += sub.link[i].to + " " + sub.link[i].from + "\r\n";
            }
            data += ";\r\n";
            //myDowith.SaveFile("glpsolRSA.dat", data, true);

            //param B:=2;
            //param M:=10;
            //set S:= 0;
            //set T:= 6;
            data += "param B:=" + B + ";\r\n";
            data += "param M:=" + M + ";\r\n";
            data += "set S:=" + S + ";\r\n";
            data += "set T:=" + T + ";\r\n";
            //myDowith.SaveFile("glpsolRSA.dat", data, true);

            //Set NoST;NoST=N-S-T
            //set NoST:= 1 2 3 4 5;
            data += "set NoST:=";
            for(int i = 0; i < sub.nodes; i++){
                if(i != S && i != T) data += " " + i;
            }
            data += ";\r\n";
            //myDowith.SaveFile("glpsolRSA.dat", data, true);

            //set BSet:=0 1;/*BSet={0,1,2,��,B-1}*/��y���ʹ��
            data += "set BSet:=";
            for(int i = 0; i < B; i++){
                data += " " + i;
            }
            data += ";\r\n";
            //myDowith.SaveFile("glpsolRSA.dat", data, true);

            //set MSet:= 0 1 2 3 4 5 6 7 8 9; /*MSet={0,1,2,��,M-1}*/
            data += "set MSet:=";
            for(int i = 0; i < M; i++){
                data += " " + i;
            }
            data += ";\r\n";
            //myDowith.SaveFile("glpsolRSA.dat", data, true);

            //set MBSet:=0 1 2 3 4 5 6 7 8;/*BSet={0,1,2,��,M-B}*/
            data += "set MBSet:=";
            for(int i = 0; i < M-B; i++){
                data += " " + i;
            }
            data += ";\r\n";
            myDowith.SaveFile("glpsolRSA.dat", data, true);

            //set BMatch[0] :=0 1;
            //set BMatch[1] :=1 2;
            //set BMatch[2] :=2 3;
            //set BMatch[3] :=3 4;
            //set BMatch[4] :=4 5;
            //set BMatch[5] :=5 6;
            //set BMatch[6] :=6 7;
            //set BMatch[7] :=7 8;
            //set BMatch[8] :=8 9;
            //data = "";
            for(int i = 0; i < M-B; i++){
                data = "";
                data += "set BMatch[" + i + "] :=";
                for(int j = 0; j < B; j++){
                    int k = i + j;
                    data += " " + k;
                }
                data += ";\r\n";
                myDowith.SaveFile("glpsolRSA.dat", data, true);
            }


            //param e:=
            //0 1 0 1
            //0 1 1 0
            //0 1 2 1
            //;
            //end;
            data = "param e:=\r\n";
            myDowith.SaveFile("glpsolRSA.dat", data, true);
            for(int i=0;i<sub.links;i++){
                for(int j=0;j<sub.slotsNum;j++){
                    int k = 1-sub.slots[i][j];
                    data = (sub.link[i].from + " " + sub.link[i].to + " " + j + " " + k + "\r\n");
                    data += (sub.link[i].to + " " + sub.link[i].from + " " + j + " " + k + "\r\n");
                    //sub.slots[i][j] = 1;//����Ϊ1��ռ��Ϊ0
                    myDowith.SaveFile("glpsolRSA.dat", data, true);
                }
            }
            data = ";\r\n";
            data += "end;\r\n";
            myDowith.SaveFile("glpsolRSA.dat", data, true);

            System.out.println("Done");

            try {
                String s;
                //Process process = Runtime.getRuntime().exec("cmd /c E:/�������⻯/��ԴJavaƵ�׷���/winglpk-4.60/glpk-4.60/w64/glpsol.exe -m E:/�������⻯/��ԴJavaƵ�׷���/winglpk-4.60/glpk-4.60/w64/glpsolRSA.mod -d E:/�������⻯/��ԴJavaƵ�׷���/winglpk-4.60/glpk-4.60/w64/glpsolRSA.dat -o glpsolRSA.o");
                Process process = Runtime.getRuntime().exec("cmd /c C:/������/RSA-EAVNE/glpk-4.60/w64/glpsol.exe -m C:/������/RSA-EAVNE/glpk-4.60/w64/glpsolRSA.mod -d glpsolRSA.dat -o glpsolRSA.o");
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



        //******************************************************************
        //���ƣ�bool FindPathByOne01ILP(......)
        //���ܣ�ͨ��һ��MIP��GLPK�㷨�ҵ���̵�·��
        //������
        //	      sNode1ΪԴ��
        //	      sNode2Ϊ���
        //	      speedΪ��������������ٶ�
        //	      subΪ��������
        //����ֵ��1���ɹ����أ�-1��ʧ�ܷ���
        //******************************************************************
        private void FindPathByOne01ILP(EOSubstrateNetwork sub, int startSlotNum,int endSlotNum, int B, int M, int S, int T)
        {
            Tools myDowith = new Tools();

            String data;
            //set V:= 0 1 2 3 4 5 6;
            data = "set V:=";
            for(int i = 0; i < sub.nodes; i++){		//sub.nodes
                data += " " + i;
            }
            data += ";\r\n";
            myDowith.SaveFile("glpsolRSA.dat", data, false);

            //set AlloSlotsSet:= 2 3;
            //data = "set AlloSlotsSet:=";
            //for(int i = startSlotNum; i < endSlotNum; i++){
            //	data += " " + i;
            //}
            //data += ";\r\n";
            //data = "set AlloSlotsSet:=" + startSlotNum + " " + endSlotNum + ";\r\n";
            //myDowith.SaveFile("glpsolRSA.dat", data, true);

            //set E:=
            data = "set E:=\r\n";
            for(int i = 0; i < sub.links; i++){
                data += sub.link[i].from + " " + sub.link[i].to + "\r\n";
                if(sub.link[i].from != S || sub.link[i].to != T)
                    data += sub.link[i].to + " " + sub.link[i].from + "\r\n";
            }
            data += ";\r\n";
            //myDowith.SaveFile("glpsolRSA.dat", data, true);

            //param B:=2;
            //param M:=10;
            //set S:= 0;
            //set T:= 6;
            data += "param B:=" + B + ";\r\n";
            data += "param M:=" + M + ";\r\n";
            data += "set S:=" + S + ";\r\n";
            data += "set T:=" + T + ";\r\n";
            //myDowith.SaveFile("glpsolRSA.dat", data, true);

            //Set NoST;NoST=N-S-T
            //set NoST:= 1 2 3 4 5;
            data += "set NoST:=";
            for(int i = 0; i < sub.nodes; i++){
                if(i != S && i != T)
                    data += " " + i;
            }
            data += ";\r\n";
            //myDowith.SaveFile("glpsolRSA.dat", data, true);

            //set BSet:=0 1;/*BSet={0,1,2,��,B-1}*/��y���ʹ��
            data += "set BSet:=";
            for(int i = 0; i < B; i++){
                data += " " + i;
            }
            data += ";\r\n";
            //myDowith.SaveFile("glpsolRSA.dat", data, true);

            //set MSet:= 0 1 2 3 4 5 6 7 8 9; /*MSet={0,1,2,��,M-1}*/
            data += "set MSet:=";
            for(int i = 0; i < M; i++){
                data += " " + i;
            }
            data += ";\r\n";
            //myDowith.SaveFile("glpsolRSA.dat", data, true);

            //set MBSet:=0 1 2 3 4 5 6 7 8;/*BSet={0,1,2,��,M-B}*/
            data += "set MBSet:=";
            for(int i = 0; i <= M-B; i++){
                data += " " + i;
            }
            data += ";\r\n";
            myDowith.SaveFile("glpsolRSA.dat", data, true);

            //set BMatch[0] :=0 1;
            //set BMatch[1] :=1 2;
            //set BMatch[2] :=2 3;
            //set BMatch[3] :=3 4;
            //set BMatch[4] :=4 5;
            //set BMatch[5] :=5 6;
            //set BMatch[6] :=6 7;
            //set BMatch[7] :=7 8;
            //set BMatch[8] :=8 9;
            //data = "";
            for(int i = 0; i <= M-B; i++){
                data = "";
                data += "set BMatch[" + i + "] :=";
                for(int j = 0; j < B; j++){
                    int k = i + j;
                    data += " " + k;
                }
                data += ";\r\n";
                myDowith.SaveFile("glpsolRSA.dat", data, true);
            }
            //�ڵ����㼯��I[i]
            for(int i = 0; i < sub.nodes; i++){
                data = "";
                data += "set I[" + i + "] :=";
                for(int j = 0; j < sub.links; j++){
                    if(sub.link[j].to == i){
                        if(i != S) data += " " + sub.link[j].from;
                    }
                    if(sub.link[j].from == i){
                        if(i != S) data += " " + sub.link[j].to;
                    }
                }
                data += ";\r\n";
                myDowith.SaveFile("glpsolRSA.dat", data, true);
            }
            //�ڵ�ĳ��㼯��O[i]
            for(int i = 0; i < sub.nodes; i++){
                data = "";
                data += "set O[" + i + "] :=";
                for(int j = 0; j < sub.links; j++){
                    if(sub.link[j].to == i){
                        if(i != T) data += " " + sub.link[j].from;
                    }
                    if(sub.link[j].from == i){
                        if(i != T) data += " " + sub.link[j].to;
                    }
                }
                data += ";\r\n";
                myDowith.SaveFile("glpsolRSA.dat", data, true);
            }
            //PMSet[i]
            for(int i = 0; i <= M-B; i++){
                data = "";
                data += "set PMSet[" + i + "] :=";
                for(int j = 2; j < 2+B-1; j++){
                    int k = i + j;
                    if(i+j < M) data += (" " + k);
                    //System.out.println(i + j);
                }
                data += ";\r\n";
                myDowith.SaveFile("glpsolRSA.dat", data, true);
            }
            //param e:=
            //0 1 0 1
            //0 1 1 0
            //0 1 2 1
            //;
            //end;
            data = "param e:=\r\n";
            myDowith.SaveFile("glpsolRSA.dat", data, true);
            for(int i=0;i<sub.links;i++){
                for(int j=0;j<sub.slotsNum;j++){
                    int k = 1-sub.slots[i][j];
                    data = (sub.link[i].from + " " + sub.link[i].to + " " + j + " " + k + "\r\n");
                    if(sub.link[i].from != S || sub.link[i].to != T)
                        data += (sub.link[i].to + " " + sub.link[i].from + " " + j + " " + k + "\r\n");
                    //sub.slots[i][j] = 1;//����Ϊ1��ռ��Ϊ0
                    myDowith.SaveFile("glpsolRSA.dat", data, true);
                }
            }
            data = ";\r\n";
            data += "end;\r\n";
            myDowith.SaveFile("glpsolRSA.dat", data, true);

            System.out.println("Done");

            try {
                String s;
                //Process process = Runtime.getRuntime().exec("cmd /c E:/�������⻯/��ԴJavaƵ�׷���/winglpk-4.60/glpk-4.60/w64/glpsol.exe -m E:/�������⻯/��ԴJavaƵ�׷���/winglpk-4.60/glpk-4.60/w64/glpsolRSA.mod -d E:/�������⻯/��ԴJavaƵ�׷���/winglpk-4.60/glpk-4.60/w64/glpsolRSA.dat -o glpsolRSA.o");
                Process process = Runtime.getRuntime().exec("cmd /c C:/������/RSA-EAVNE/glpk-4.60/w64/glpsol.exe -m C:/������/RSA-EAVNE/glpk-4.60/w64/glpsol01ILPRSACXH.mod -d glpsolRSA.dat -o glpsolRSA.o");
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

        //******************************************************************
        //���ƣ�void FindOptimalSolution(......)
        //���ܣ�ͨ��һ��MIP��GLPK�㷨�ҵ���̵�·��
        //������
        //	      sNode1ΪԴ��
        //	      sNode2Ϊ���
        //	      speedΪ��������������ٶ�
        //	      subΪ��������
        //����ֵ��1���ɹ����أ�-1��ʧ�ܷ���
        //******************************************************************
        private Boolean FindOptimalSolution(int startSlotNum,int endSlotNum,int sNode1,int sNode2,int p[])
        {
            BufferedReader reader = null;
            int minLength = -1;
            try {
                    System.out.println("����Ϊ��λ��ȡ�ļ����ݣ�һ�ζ�һ���У�");
                    reader = new BufferedReader(new FileReader("glpsolRSA.o"));
                    String tempString = null;
                    Hashtable hashResolve = null;//��Ž��HashTable
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
                            tempString = tempString.replace("Objective:  shPath = ", "");
                            tempString = tempString.replace("(MINimum)", "");
                            tempString = tempString.trim();
                            minLength = Integer.parseInt(tempString);
                            hashResolve = new Hashtable(minLength,(float)1.0);//����hash
                        }
                        if(line > 6 && tempString.indexOf(" x[") != -1){//˵���ҵ������Ž��x����
                            //ȥ��ǰ��Ĳ��֣�3 x[0,2]       *              1             0             1
                            //�Կո�ָ���ȡ��һ������
                            String tmpStr = "";
                            //System.out.println("line " + line + ": " + tempString);

                            tmpStr = tempString.substring(tempString.indexOf("*")+1);
                            tmpStr = tmpStr.trim();
                            //System.out.println("line " + line + ": " + tmpStr);

                            tmpStr = tmpStr.substring(0,tmpStr.indexOf(" "));
                            //System.out.println("line " + line + ": " + tmpStr);
                            if(Integer.parseInt(tmpStr) == 1){//˵���ҵ���һ����
                                //�õ�һ���⸳ֵ��tmpStr������x[0,2]
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
                                hashResolve.put(keyNode1,keyNode2);//�Ᵽ����hash����
                            }
                        }
                        line++;
                    }
                    reader.close();

                    //����·��
                    int node1 = sNode1,node2;
                    System.out.println(""+sNode1+"->"+sNode2);
                    for(int i = 0; i < minLength; i++){
                        node2 = Integer.parseInt(hashResolve.get(node1).toString());
                        p[node1] = node2;
                        System.out.println("p["+node1+"]:"+p[node1]);
                        node1 = node2;
                    }
                    p[sNode2] = -1;
                    return true;
                    //��ȡ����
                    /*
                    String str = "";
                    Enumeration enum1 = hashResolve.elements();
                    System.out.println("The length:" + minLength +".\nThe element of hash is: ");
                    while(enum1.hasMoreElements()) {
                        str = enum1.nextElement().toString();
                        System.out.println(str+" ");
                        if(str.contains("x["+sNode1+",")) break;
                    }*/

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

        //******************************************************************
        //���ƣ�void FindOptimalSolutionBy01ILP(......)
        //���ܣ�ͨ��01ILP��GLPK�㷨�ҵ���̵�·�� ��Ƶ��
        //������
        //	      slotNum[]:���صĲ�����slotNum[0]Ϊ��ʼ��Ƶ�ײ�startSlotNum��slotNum[1]Ϊ������Ƶ�ײ�endSlotNum
        //    	  sNode1��ΪԴ��
        //	      sNode2��Ϊ���
        //		  int p[]��Ϊ���ص�·��
        //����ֵ��1���ɹ����أ�-1��ʧ�ܷ���
        //����ʱ�䣺2017-07-27
        //�����ˣ�������
        //******************************************************************
        private Boolean FindOptimalSolutionBy01ILP(int slotNum,int sNode1,int sNode2,int p[],int slotNumIndex[])
        {
            BufferedReader reader = null;
            int minLength = -1;
            try {
                    System.out.println("����Ϊ��λ��ȡ�ļ����ݣ�һ�ζ�һ���У�");
                    reader = new BufferedReader(new FileReader("glpsolRSA.o"));
                    String tempString = null;
                    Hashtable hashResolve = null;//��Ž��HashTable
                    //Hashtable hashResolveIndex = null;//��Ž��HashTable
                    int line = 1;
                    int minSlotIndex = 10000;//minSlotIndex��С��slot����
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
                            tempString = tempString.replace("Objective:  shPath = ", "");
                            tempString = tempString.replace("(MINimum)", "");
                            tempString = tempString.trim();
                            minLength = Integer.parseInt(tempString);
                            hashResolve = new Hashtable(minLength,(float)1.0);//����hash
                            //hashResolveIndex = new Hashtable(slotNum*minLength,(float)1.0);//����hash
                        }
                        if(line > 6 && tempString.indexOf(" x[") != -1){//˵���ҵ������Ž��x����
                            //ȥ��ǰ��Ĳ��֣�3 x[0,2]       *              1             0             1
                            //�Կո�ָ���ȡ��һ������
                            String tmpStr = "";
                            //System.out.println("line " + line + ": " + tempString);

                            tmpStr = tempString.substring(tempString.indexOf("*")+1);
                            tmpStr = tmpStr.trim();
                            //System.out.println("line " + line + ": " + tmpStr);

                            tmpStr = tmpStr.substring(0,tmpStr.indexOf(" "));
                            //System.out.println("line " + line + ": " + tmpStr);
                            if(Integer.parseInt(tmpStr) == 1){//˵���ҵ���һ����
                                //�õ�һ���⸳ֵ��tmpStr������x[0,2]
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
                                hashResolve.put(keyNode1,keyNode2);//�Ᵽ����hash����
                            }
                        }
                        if(line > 6 && tempString.indexOf(" f[") != -1){//˵���ҵ������Ž��x����
                            //ȥ��ǰ��Ĳ��֣�3 f[1,2,0]       *              1             0             1
                            //�Կո�ָ���ȡ��һ������
                            String tmpStr = "";
                            //System.out.println("line " + line + ": " + tempString);

                            tmpStr = tempString.substring(tempString.indexOf("*")+1);
                            tmpStr = tmpStr.trim();
                            //System.out.println("line " + line + ": " + tmpStr);

                            tmpStr = tmpStr.substring(0,tmpStr.indexOf(" "));
                            //System.out.println("line " + line + ": " + tmpStr);
                            if(Integer.parseInt(tmpStr) == 1){//˵���ҵ���һ����
                                //�õ�һ���⸳ֵ��tmpStr������x[0,2]
                                tempString = tempString.trim();
                                tmpStr = tempString.substring(tempString.indexOf(" ")+1);//ȥ��ǰ����к�
                                //System.out.println("line " + line + ": " + tmpStr);
                                tmpStr = tmpStr.substring(0,tmpStr.indexOf(" "));		//�õ�f[i,j,k]
                                //System.out.println("line " + line + ": " + tmpStr);
                                int keyNode1 = -1,keyNode2 = -1,keyNode3 = -1;
                                keyNode1 = Integer.parseInt(tmpStr.substring(tmpStr.indexOf("[")+1, tmpStr.indexOf(",")));//�õ�f[i,j,k]��i
                                //System.out.println("keyNode1:"+keyNode1);
                                tmpStr = tmpStr.substring(tmpStr.indexOf(",")+1);		//�õ�j,k]
                                keyNode2 = Integer.parseInt(tmpStr.substring(0,tmpStr.indexOf(",")));//�õ�f[i,j,k]��j
                                //System.out.println("keyNode2:"+keyNode2);
                                keyNode3 = Integer.parseInt(tmpStr.substring(tmpStr.indexOf(",")+1, tmpStr.indexOf("]")));//�õ�f[i,j,k]��k
                                //System.out.println("keyNode2:"+keyNode2);
                                //hashResolveIndex.put(keyNode1+","+keyNode2+","+keyNode3,keyNode3);//�Ᵽ����hash����
                                if(minSlotIndex > keyNode3) minSlotIndex = keyNode3;//minSlotIndex��С��slot����
                            }
                        }
                        line++;
                    }
                    reader.close();

                    //����·��
                    int node1 = sNode1,node2;
                    System.out.println(""+sNode1+"->"+sNode2);
                    for(int i = 0; i < minLength; i++){
                        node2 = Integer.parseInt(hashResolve.get(node1).toString());
                        p[node1] = node2;
                        System.out.println("p["+node1+"]:"+p[node1]);
                        node1 = node2;
                    }
                    p[sNode2] = -1;

                    //�õ�[minSlotIndex,maxSlotIndex]
                    slotNumIndex[0] = minSlotIndex;
                    slotNumIndex[1] = minSlotIndex + slotNum - 1;

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
        //******************************************************************
        //���ƣ�int FindPathBy01ILP(......)
        //���ܣ�ͨ��01ILP��GLPK�㷨�ҵ���̵�·��
        //������
        //	      sNode1ΪԴ��
        //	      sNode2Ϊ���
        //	      speedΪ��������������ٶ�
        //	      subΪ��������
        //����ֵ��1���ɹ����أ�-1��ʧ�ܷ���
        //******************************************************************
        private int FindPathBy01ILP(EOSubstrateNetwork sub,int sNode1,int sNode2,double speed,int difSlotsNum,int ret[],int p[])
        {
            //�㷨
            //���ȸ�����͵�Ƶ�ײۣ�����GLPK�㷨��������������·���ģ����˳�
            double speedFind = -1;
            boolean find = false;
            for(int i=0;i<sub.modulationLevel;i++){
                if(sub.opticalReach[i] >= speed && sub.opticalReach[i+1] < speed) {
                    speedFind = sub.transRate[i];//ѡ�����ģʽ
                    find = true;
                }
            }
            if(speed < sub.opticalReach[0]) {
                find = true;
                speedFind = sub.opticalReach[0];
            }
            if(speed >= sub.opticalReach[sub.modulationLevel-1]){
                find = true;
                speedFind = sub.opticalReach[sub.modulationLevel-1];
            }
            if(!find) return -1;
            int slotNum = -1;
            speedFind = 75;//12.5;////BPSK 12.5 4000//64QAM 75 12.5
            slotNum = (int)Math.floor(speed/speedFind)+1;

            int startSlotNum = 0;
            //������·�������е���·�Ƿ����㹻��slots���������Ǵ�С����
            int i=0;
            ret[1] = slotNum + difSlotsNum;//���ص�Ƶ�ײ�����
            //boolean findSlot = false;

            //����01ILP���Թ滮ģ��int p[][] = new int[reqs[i].links][sub.nodes];
            int slotNumIndex[] = new int[2];
            FindPathByOne01ILP(sub,startSlotNum,startSlotNum+slotNum+difSlotsNum,ret[1],sub.slotsNum,sNode1,sNode2);//����һ��MIP
            //FindOptimalSolutionBy01ILP(int slotNum,int sNode1,int sNode2,int p[],int slotNumIndex[]);
            //if(FindOptimalSolution(startSlotNum,startSlotNum+slotNum+difSlotsNum,sNode1,sNode2,p)){//�ҵ������Ž�
            if(FindOptimalSolutionBy01ILP(slotNum+difSlotsNum,sNode1,sNode2,p,slotNumIndex)){//�ҵ������Ž�
                ret[0] = slotNumIndex[0];//startSlotNum;
                return 1;
            }

            ret[1] = slotNum;//���ص�Ƶ�ײ�����
            startSlotNum = sub.slotsNum-slotNum;
            FindPathByOneMIP(sub,startSlotNum,startSlotNum+slotNum,ret[1],sub.slotsNum,sNode1,sNode2);//����һ��MIP
            if(FindOptimalSolution(startSlotNum,startSlotNum+slotNum,sNode1,sNode2,p)){//�ҵ������Ž�
                ret[0] = startSlotNum;
                return 1;
            }

            return -1;
        }


        //******************************************************************
        //���ƣ�int FindPathByMIP(......)
        //���ܣ�ͨ��MIP��GLPK�㷨�ҵ���̵�·��
        //������
        //	      sNode1ΪԴ��
        //	      sNode2Ϊ���
        //	      speedΪ��������������ٶ�
        //	      subΪ��������
        //����ֵ��1���ɹ����أ�-1��ʧ�ܷ���
        //******************************************************************
        private int FindPathByMIP(EOSubstrateNetwork sub,int sNode1,int sNode2,double speed,int difSlotsNum,int ret[],int p[])
        {
            //�㷨
            //���ȸ�����͵�Ƶ�ײۣ�����GLPK�㷨��������������·���ģ����˳�
            double speedFind = -1;
            boolean find = false;
            for(int i=0;i<sub.modulationLevel;i++){
                if(sub.opticalReach[i] >= speed && sub.opticalReach[i+1] < speed) {
                    speedFind = sub.transRate[i];//ѡ�����ģʽ
                    find = true;
                }
            }
            if(speed < sub.opticalReach[0]) {
                find = true;
                speedFind = sub.opticalReach[0];
            }
            if(speed >= sub.opticalReach[sub.modulationLevel-1]){
                find = true;
                speedFind = sub.opticalReach[sub.modulationLevel-1];
            }
            if(!find) return -1;
            int slotNum = -1;
            speedFind = 75;//12.5;////BPSK 12.5 4000//64QAM 75 12.5
            slotNum = (int)Math.floor(speed/speedFind)+1;

            int startSlotNum = 0;
            //������·�������е���·�Ƿ����㹻��slots���������Ǵ�С����
            int i=0;
            ret[1] = slotNum + difSlotsNum;//���ص�Ƶ�ײ�����
            boolean findSlot = false;

            //FindPathByOneMIP(sub,startSlotNum,startSlotNum+slotNum+difSlotsNum);//����һ��MIP
            while(startSlotNum < sub.slotsNum-slotNum-difSlotsNum){
                FindPathByOneMIP(sub,startSlotNum,startSlotNum+slotNum+difSlotsNum,ret[1],sub.slotsNum,sNode1,sNode2);//����һ��MIP
                if(FindOptimalSolution(startSlotNum,startSlotNum+slotNum+difSlotsNum,sNode1,sNode2,p)){//�ҵ������Ž�
                    ret[0] = startSlotNum;
                    return 1;
                }
                startSlotNum ++;
            }
            ret[1] = slotNum;//���ص�Ƶ�ײ�����
            findSlot = false;
            startSlotNum = 0;
            while(startSlotNum <= sub.slotsNum-slotNum){
                FindPathByOneMIP(sub,startSlotNum,startSlotNum+slotNum,ret[1],sub.slotsNum,sNode1,sNode2);//����һ��MIP
                if(FindOptimalSolution(startSlotNum,startSlotNum+slotNum,sNode1,sNode2,p)){//�ҵ������Ž�
                    ret[0] = startSlotNum;
                    return 1;
                }
                startSlotNum ++;
            }
            return -1;
            /////////////////////////////////////////////////////
            /*
            Queue<Integer> q = new LinkedList<Integer>();
            boolean visit[] = new boolean[sub.nodes];
            double d[] = new double[sub.nodes];	//c��f��������������,bΪcost���ñ�,dΪ·��
            double b[][] = new double[sub.nodes][sub.nodes];
            int c[][] = new int[sub.nodes][sub.nodes];
            int needSlotNum = 1;//��speed����
            //int p[] = new int[sub.nodes];

            //Init p[]
            for(int i=0;i<sub.nodes;i++){
                p[i] = -1;
            }

            //Init b[][].
            for(int i=0;i<sub.nodes;i++){
                for(int j=0;j<sub.nodes;j++){
                    b[i][j] = -1;
                    c[i][j] = 0;
                }
            }
            for(int i=0;i<sub.links;i++){
                b[sub.link[i].from][sub.link[i].to] = 1;//b[i][j]Ϊ����֮��ľ��룬�����������֮������·����ֵΪ1.
                b[sub.link[i].to][sub.link[i].from] = 1;
            }

            //Init visit[],d[].
            for(int i=0;i<sub.nodes;i++){
                visit[i] = false;
                d[i] = Parameters.MAX_VALUE_INT;//dΪ·��
                //c��f��������������,bΪcost���ñ�,dΪ·��
            }

            //Init c[][].
            for(int i=0;i<sub.links;i++){
                int leftSlots = 0;
                for(int j=0;j<sub.slotsNum;j++){
                    leftSlots += sub.slots[i][j];
                }
                c[sub.link[i].from][sub.link[i].to] = c[sub.link[i].to][sub.link[i].from] = leftSlots;
            }
            //Ѱ�����·��
            q.add(sNode1);  //���ڵ�s=0�����,����ʼ�ڵ�
            visit[sNode1] = true;      //˵���ڵ��Ѿ��������
            d[sNode1] = 0;
            while(!q.isEmpty()) {   //���q���գ���˵������Ҫ���
                int u = q.remove();  //ȡ���е���ǰ��
                visit[u] = false;   //˵��u�õ�δ�������
                for(int v=0; v<sub.nodes; v++){ //�����нڵ���м��
                    if(c[u][v] >= needSlotNum && b[u][v]>=0 && (d[v] > d[u] + b[u][v] || d[v] > d[u] + b[v][u])) {  //c��f��������������,bΪcost���ñ�,dΪ·��
                        d[v] = d[u] + b[u][v];  //d������̾���
                        p[v] = u;       //p�������·����v�ڵ��ǰһ���ڵ�
                        //printf("u=%d,v=%d=%d.\n",u,v,d[v]);
                        if(!visit[v]) { //˵���ýڵ㲻�ڶ����У���Ҫ�����
                          q.add(v);
                          visit[v] = true;
                        }
                        if(sub.linksNo[u][v] == -1)//���·��
                            System.out.println("In SPFA, sub.linksNo["+ u +"]["+v+"] error.");
                    }
                }
            }
            //�ҵ�·���ϵ���̾���minLength
            double minLength = 0;
            double lengthMid = Parameters.MAX_VALUE_DOUBLE;
            int snodeMid,snodeMid1;
            if(p[sNode2] != -1) {
                minLength = sub.link[sNode2].length;
            } else {
                return -1;
            }
            snodeMid1 = sNode2;
            while(p[snodeMid1] != -1) {
                snodeMid = p[snodeMid1];
                if(minLength > sub.link[snodeMid].length) minLength = sub.link[snodeMid].length;
                if(sub.linksNo[snodeMid][snodeMid1] == -1)//���·��
                    System.out.println("In finding the minLength, sub.linksNo["+ snodeMid1 +"]["+sNode1+"] error.");
                snodeMid1 = snodeMid;
                if(snodeMid1 == sNode1) break;//�ӻ�㵽Դ���·���Ѿ�����
            }


            //�ҵ���Ӧ�ĵ��Ƽ����봫���ʣ��Ӷ�ȷ��slot����
            double speedFind = -1;
            boolean find = false;
            for(int i=0;i<sub.modulationLevel;i++){
                if(sub.opticalReach[i] >= speed && sub.opticalReach[i+1] < speed) {
                    speedFind = sub.transRate[i];
                    find = true;
                }
            }
            if(!find) return -1;
            int slotNum = -1;
            slotNum = (int)Math.floor(speed/speedFind)+1;

            //������·�������е���·�Ƿ����㹻��slots���������Ǵ�С����
            int i=0;
            ret[1] = slotNum + difSlotsNum;//���ص�Ƶ�ײ�����
            boolean findSlot = false;
            for(i=0;i<sub.slotsNum-slotNum-difSlotsNum;i++){
                snodeMid1 = sNode2;
                while(snodeMid1 != sNode1) {//˵����snodeMid1->sNode1��·����������е�·�����ж��Ƿ����㹻�Ŀ���Ƶ�׿�
                    //�õ�snodeMid1->sNode1����·��
                    if(sub.linksNo[snodeMid1][p[snodeMid1]] == -1)
                        System.out.println("In finding slots, sub.linksNo["+ snodeMid1 +"]["+p[snodeMid1]+"] error.");
                    if(CheckAllPathFreeSlots(sub,sub.linksNo[snodeMid1][p[snodeMid1]],i,slotNum+difSlotsNum) == -1){//failed
                        break;
                    }
                    snodeMid1 = p[snodeMid1];
                }
                if(snodeMid1 == sNode1) {
                    findSlot = true;
                    break;
                }
            }
            if(i == sub.slotsNum-slotNum-difSlotsNum && !findSlot) {//��ĩ�˵Ŀ���Ƶ�׿飬����Ƿ���
                ret[1] = slotNum;//���ص�Ƶ�ײ�����
                findSlot = false;
                for(;i<sub.slotsNum-slotNum;i++){
                    snodeMid1 = sNode2;
                    while(snodeMid1 != sNode1) {//˵����snodeMid1->sNode1��·����������е�·�����ж��Ƿ����㹻�Ŀ���Ƶ�׿�
                        //�õ�snodeMid1->sNode1����·��
                        if(sub.linksNo[snodeMid1][sNode1] == -1)
                            System.out.println("sub.linksNo["+ snodeMid1 +"]["+sNode1+"] error.");
                        if(CheckAllPathFreeSlots(sub,sub.linksNo[snodeMid1][sNode1],i,slotNum) == -1){//failed
                            break;
                        }
                        snodeMid1 = p[snodeMid1];
                    }
                    if(snodeMid1 == sNode1) {
                        findSlot = true;
                        break;
                    }
                }
            }
            if(i < sub.slotsNum-slotNum && i >= 0){//˵���ҵ��˿����Ƶ�׿�
                ret[0] = i;
                return 1;
            } else {//failed.
                return -1;
            }
            */
        }
        //******************************************************************
        //���ƣ�int FindPathByMIP(......)
        //���ܣ�ͨ��MIP��GLPK�㷨�ҵ���̵�·��
        //������
        //	      sNode1ΪԴ��
        //	      sNode2Ϊ���
        //	      speedΪ��������������ٶ�
        //	      subΪ��������
        //����ֵ��1���ɹ����أ�-1��ʧ�ܷ���
        //******************************************************************
        private int FindPathByMIPEnh(EOSubstrateNetwork sub,int sNode1,int sNode2,double speed,int difSlotsNum,int ret[],int p[])
        {
            //�㷨
            //���ȸ�����͵�Ƶ�ײۣ�����GLPK�㷨��������������·���ģ����˳�
            double speedFind = -1;
            boolean find = false;
            for(int i=0;i<sub.modulationLevel;i++){
                if(sub.opticalReach[i] >= speed && sub.opticalReach[i+1] < speed) {
                    speedFind = sub.transRate[i];//ѡ�����ģʽ
                    find = true;
                }
            }
            if(speed < sub.opticalReach[0]) {
                find = true;
                speedFind = sub.opticalReach[0];
            }
            if(speed >= sub.opticalReach[sub.modulationLevel-1]){
                find = true;
                speedFind = sub.opticalReach[sub.modulationLevel-1];
            }
            if(!find) return -1;
            int slotNum = -1;
            speedFind = 75;//12.5;////BPSK 12.5 4000//64QAM 75 12.5
            slotNum = (int)Math.floor(speed/speedFind)+1;

            int startSlotNum = 0;
            //������·�������е���·�Ƿ����㹻��slots���������Ǵ�С����
            int i=0;
            ret[1] = slotNum + difSlotsNum;//���ص�Ƶ�ײ�����
            boolean findSlot = false;

            //FindPathByOneMIP(sub,startSlotNum,startSlotNum+slotNum+difSlotsNum);//����һ��MIP
            while(startSlotNum < sub.slotsNum-slotNum-difSlotsNum){
                //�ж������˵��Ƿ�����н⣬���û�У�����ҪMIP
                if(!FindResoInTwoEnd(sub,sNode1,sNode2,slotNum+difSlotsNum,startSlotNum)){
                    startSlotNum ++;
                    continue;
                }
                FindPathByOneMIP(sub,startSlotNum,startSlotNum+slotNum+difSlotsNum,ret[1],sub.slotsNum,sNode1,sNode2);//����һ��MIP
                if(FindOptimalSolution(startSlotNum,startSlotNum+slotNum+difSlotsNum,sNode1,sNode2,p)){//�ҵ������Ž�
                    ret[0] = startSlotNum;
                    return 1;
                }
                startSlotNum ++;
            }
            ret[1] = slotNum;//���ص�Ƶ�ײ�����
            findSlot = false;
            startSlotNum = 0;
            while(startSlotNum <= sub.slotsNum-slotNum){
                //�ж������˵��Ƿ�����н⣬���û�У�����ҪMIP
                if(!FindResoInTwoEnd(sub,sNode1,sNode2,slotNum,startSlotNum)){
                    startSlotNum ++;
                    continue;
                }
                FindPathByOneMIP(sub,startSlotNum,startSlotNum+slotNum,ret[1],sub.slotsNum,sNode1,sNode2);//����һ��MIP
                if(FindOptimalSolution(startSlotNum,startSlotNum+slotNum,sNode1,sNode2,p)){//�ҵ������Ž�
                    ret[0] = startSlotNum;
                    return 1;
                }
                startSlotNum ++;
            }
            return -1;
        }

        //�ж������˵��Ƿ�����н⣬���û�У��򷵻�false�����򣬷���true
        private boolean FindResoInTwoEnd(EOSubstrateNetwork sub,int sNode1,int sNode2,int slotsFreeSum,int startSlotsNo)
        {
            boolean find1 = false;
            boolean find2 = false;
            int findSlot1=0,findSlot2=0;
            if(startSlotsNo+slotsFreeSum >= sub.slotsNum) return false;//˵���������㹻����ô����е�slot
            for(int i=0;i<sub.links;i++){
                findSlot1 = 0;
                if(sub.link[i].from == sNode1 || sub.link[i].to == sNode1){
                    for(int j=startSlotsNo;j<startSlotsNo+slotsFreeSum;j++){
                        findSlot1 += sub.slots[i][j];
                    }
                    if(findSlot1 == slotsFreeSum) {
                        find1 = true;
                        break;
                    }
                }
            }
            for(int i=0;i<sub.links;i++){
                findSlot2 = 0;
                if(sub.link[i].from == sNode2 || sub.link[i].to == sNode2){
                    for(int j=startSlotsNo;j<startSlotsNo+slotsFreeSum;j++){
                        findSlot2 += sub.slots[i][j];
                    }
                    if(findSlot2 == slotsFreeSum) {
                        find2 = true;
                        break;
                    }
                }
            }
            if(find1 && find2) return true;
            return false;
        }
        //******************************************************************
        //���ƣ�int FindPathBySPFA(......)
        //���ܣ��ҵ����ʵ�·��
        //������
        //	      sNode1ΪԴ��
        //	      sNode2Ϊ���
        //	      speedΪ��������������ٶ�
        //	      subΪ��������
        //����ֵ��1���ɹ����أ�-1��ʧ�ܷ���
        //******************************************************************
        private int FindPathBySPFA(EOSubstrateNetwork sub,int sNode1,int sNode2,double speed,int difSlotsNum,int ret[],int p[])
        {
            Queue<Integer> q = new LinkedList<Integer>();
            boolean visit[] = new boolean[sub.nodes];
            double d[] = new double[sub.nodes];	//c��f��������������,bΪcost���ñ�,dΪ·��
            double b[][] = new double[sub.nodes][sub.nodes];
            int c[][] = new int[sub.nodes][sub.nodes];
            int needSlotNum = 1;//��speed����
            //int p[] = new int[sub.nodes];

            //Init p[]
            for(int i=0;i<sub.nodes;i++){
                p[i] = -1;
            }

            //Init b[][].
            for(int i=0;i<sub.nodes;i++){
                for(int j=0;j<sub.nodes;j++){
                    b[i][j] = -1;
                    c[i][j] = 0;
                }
            }
            for(int i=0;i<sub.links;i++){
                b[sub.link[i].from][sub.link[i].to] = 1;//b[i][j]Ϊ����֮��ľ��룬�����������֮������·����ֵΪ1.
                b[sub.link[i].to][sub.link[i].from] = 1;
            }

            //Init visit[],d[].
            for(int i=0;i<sub.nodes;i++){
                visit[i] = false;
                d[i] = Parameters.MAX_VALUE_INT;//dΪ·��
                //c��f��������������,bΪcost���ñ�,dΪ·��
            }

            //Init c[][].
            for(int i=0;i<sub.links;i++){
                int leftSlots = 0;
                for(int j=0;j<sub.slotsNum;j++){
                    leftSlots += sub.slots[i][j];
                }
                c[sub.link[i].from][sub.link[i].to] = c[sub.link[i].to][sub.link[i].from] = leftSlots;
            }
            //Ѱ�����·��
            q.add(sNode1);  //���ڵ�s=0�����,����ʼ�ڵ�
            visit[sNode1] = true;      //˵���ڵ��Ѿ��������
            d[sNode1] = 0;
            while(!q.isEmpty()) {   //���q���գ���˵������Ҫ���
                int u = q.remove();  //ȡ���е���ǰ��
                visit[u] = false;   //˵��u�õ�δ�������
                for(int v=0; v<sub.nodes; v++){ //�����нڵ���м��
                    if(c[u][v] >= needSlotNum && b[u][v]>=0 && (d[v] > d[u] + b[u][v] || d[v] > d[u] + b[v][u])) {  //c��f��������������,bΪcost���ñ�,dΪ·��
                        d[v] = d[u] + b[u][v];  //d������̾���
                        p[v] = u;       //p�������·����v�ڵ��ǰһ���ڵ�
                        //printf("u=%d,v=%d=%d.\n",u,v,d[v]);
                        if(!visit[v]) { //˵���ýڵ㲻�ڶ����У���Ҫ�����
                          q.add(v);
                          visit[v] = true;
                        }
                        if(sub.linksNo[u][v] == -1)//���·��
                            System.out.println("In SPFA, sub.linksNo["+ u +"]["+v+"] error.");
                    }
                }
            }
            //�ҵ�·���ϵ���̾���minLength
            double minLength = 0;
            double lengthMid = Parameters.MAX_VALUE_DOUBLE;
            int snodeMid,snodeMid1;
            if(p[sNode2] != -1) {
                minLength = sub.link[sNode2].length;
            } else {
                return -1;
            }
            snodeMid1 = sNode2;
            while(p[snodeMid1] != -1) {
                snodeMid = p[snodeMid1];
                if(minLength > sub.link[snodeMid].length)
                    minLength = sub.link[snodeMid].length;
                if(sub.linksNo[snodeMid][snodeMid1] == -1)//���·��
                    System.out.println("In finding the minLength, sub.linksNo["+ snodeMid1 +"]["+sNode1+"] error.");
                snodeMid1 = snodeMid;
                if(snodeMid1 == sNode1) break;//�ӻ�㵽Դ���·���Ѿ�����
            }


            //�ҵ���Ӧ�ĵ��Ƽ����봫���ʣ��Ӷ�ȷ��slot����
            double speedFind = -1;
            boolean find = false;
            for(int i=0;i<sub.modulationLevel;i++){
                if(sub.opticalReach[i] >= speed && sub.opticalReach[i+1] < speed) {
                    speedFind = sub.transRate[i];
                    find = true;
                }
            }
            if(speed < sub.opticalReach[0]) {
                find = true;
                speedFind = sub.opticalReach[0];
            }
            if(speed >= sub.opticalReach[sub.modulationLevel-1]){
                find = true;
                speedFind = sub.opticalReach[sub.modulationLevel-1];
            }
            if(!find) return -1;
            int slotNum = -1;

            speedFind = 75;//12.5;////BPSK 12.5 4000//64QAM 75 12.5
            slotNum = (int)Math.floor(speed/speedFind)+1;

            if(slotNum > sub.slotsNum) return -1;//���������Դ�������ֵ����ʧ���˳�

            //������·�������е���·�Ƿ����㹻��slots���������Ǵ�С����
            int i=0;
            ret[1] = slotNum + difSlotsNum;//���ص�Ƶ�ײ�����mo'gu
            boolean findSlot = false;
            for(i=0;i<sub.slotsNum-slotNum-difSlotsNum;i++){
                snodeMid1 = sNode2;
                while(snodeMid1 != sNode1) {//˵����snodeMid1->sNode1��·����������е�·�����ж��Ƿ����㹻�Ŀ���Ƶ�׿�
                    //�õ�snodeMid1->sNode1����·��
                    if(sub.linksNo[snodeMid1][p[snodeMid1]] == -1)
                        System.out.println("In finding slots, sub.linksNo["+ snodeMid1 +"]["+p[snodeMid1]+"] error.");
                    if(CheckAllPathFreeSlots(sub,sub.linksNo[snodeMid1][p[snodeMid1]],i,slotNum+difSlotsNum) == -1){//failed
                        break;
                    }
                    snodeMid1 = p[snodeMid1];
                }
                if(snodeMid1 == sNode1) {
                    findSlot = true;
                    break;
                }
            }


            //����߼��slot��
            if((i == sub.slotsNum-slotNum-difSlotsNum || i == sub.slotsNum-slotNum) && !findSlot) {//��ĩ�˵Ŀ���Ƶ�׿飬����Ƿ���
                ret[1] = slotNum;//���ص�Ƶ�ײ�����
                findSlot = false;
                for(;i<sub.slotsNum-slotNum;i++){
                    snodeMid1 = sNode2;
                    while(snodeMid1 != sNode1) {//˵����snodeMid1->sNode1��·����������е�·�����ж��Ƿ����㹻�Ŀ���Ƶ�׿�
                        //�õ�snodeMid1->sNode1����·��
                        if(sub.linksNo[snodeMid1][p[snodeMid1]] == -1)
                            System.out.println("sub.linksNo["+ snodeMid1 +"]["+p[snodeMid1]+"] error.");
                        if(CheckAllPathFreeSlots(sub,sub.linksNo[snodeMid1][p[snodeMid1]],i,slotNum) == -1){//failed
                            break;
                        }
                        snodeMid1 = p[snodeMid1];
                    }
                    if(snodeMid1 == sNode1) {
                        findSlot = true;
                        break;
                    }
                }
            }
            //�θ�
            if(!findSlot){
                ret[1] = slotNum;//���ص�Ƶ�ײ�����
                for(i=sub.slotsNum-slotNum;i<=sub.slotsNum-1;i++){//��
                    snodeMid1 = sNode2;
                    while(snodeMid1 != sNode1) {//˵����snodeMid1->sNode1��·����������е�·�����ж��Ƿ����㹻�Ŀ���Ƶ�׿�
                        //�õ�snodeMid1->sNode1����·��
                        if(sub.linksNo[snodeMid1][p[snodeMid1]] == -1)
                            System.out.println("In finding slots, sub.linksNo["+ snodeMid1 +"]["+p[snodeMid1]+"] error.");
                        if(CheckAllPathFreeSlots(sub,sub.linksNo[snodeMid1][p[snodeMid1]],i,slotNum) == -1){//failed
                            break;
                        }
                        snodeMid1 = p[snodeMid1];
                    }
                    if(snodeMid1 == sNode1) {
                        findSlot = true;
                        break;
                    }
                }
            }

            if(i <= sub.slotsNum-slotNum && i >= 0){//˵���ҵ��˿����Ƶ�׿�
                ret[0] = i;
                return 1;
            } else {//failed.
                return -1;
            }
        }

        //******************************************************************
        //���ƣ�int FindPathBySPFA(......)
        //���ܣ��ҵ����ʵ�·��
        //������
        //	      sNode1ΪԴ��
        //	      sNode2Ϊ���
        //	      speedΪ��������������ٶ�
        //	      subΪ��������
        //����ֵ��1���ɹ����أ�-1��ʧ�ܷ���
        //******************************************************************
        private int FindPathBySPFAEnh(EOSubstrateNetwork sub,int sNode1,int sNode2,double speed,int difSlotsNum,int ret[],int p[])
        {
            Queue<Integer> q = new LinkedList<Integer>();
            boolean visit[] = new boolean[sub.nodes];
            double d[] = new double[sub.nodes];	//c��f��������������,bΪcost���ñ�,dΪ·��
            double b[][] = new double[sub.nodes][sub.nodes];
            int c[][] = new int[sub.nodes][sub.nodes];
            int needSlotNum = 1;//��speed����
            //int p[] = new int[sub.nodes];

            //Init p[]
            for(int i=0;i<sub.nodes;i++){
                p[i] = -1;
            }

            //Init b[][].
            for(int i=0;i<sub.nodes;i++){
                for(int j=0;j<sub.nodes;j++){
                    b[i][j] = -1;
                    c[i][j] = 0;
                }
            }
            for(int i=0;i<sub.links;i++){
                b[sub.link[i].from][sub.link[i].to] = 1;//b[i][j]Ϊ����֮��ľ��룬�����������֮������·����ֵΪ1.
                b[sub.link[i].to][sub.link[i].from] = 1;
            }

            //Init visit[],d[].
            for(int i=0;i<sub.nodes;i++){
                visit[i] = false;
                d[i] = Parameters.MAX_VALUE_INT;//dΪ·��
                //c��f��������������,bΪcost���ñ�,dΪ·��
            }

            //Init c[][].
            for(int i=0;i<sub.links;i++){
                int leftSlots = 0;
                for(int j=0;j<sub.slotsNum;j++){
                    leftSlots += sub.slots[i][j];
                }
                c[sub.link[i].from][sub.link[i].to] = c[sub.link[i].to][sub.link[i].from] = leftSlots;
            }
            //Ѱ�����·��
            q.add(sNode1);  //���ڵ�s=0�����,����ʼ�ڵ�
            visit[sNode1] = true;      //˵���ڵ��Ѿ��������
            d[sNode1] = 0;
            while(!q.isEmpty()) {   //���q���գ���˵������Ҫ���
                int u = q.remove();  //ȡ���е���ǰ��
                visit[u] = false;   //˵��u�õ�δ�������
                for(int v=0; v<sub.nodes; v++){ //�����нڵ���м��
                    if(c[u][v] >= needSlotNum && b[u][v]>=0 && (d[v] > d[u] + b[u][v] || d[v] > d[u] + b[v][u])) {  //c��f��������������,bΪcost���ñ�,dΪ·��
                        d[v] = d[u] + b[u][v];  //d������̾���
                        p[v] = u;       //p�������·����v�ڵ��ǰһ���ڵ�
                        //printf("u=%d,v=%d=%d.\n",u,v,d[v]);
                        if(!visit[v]) { //˵���ýڵ㲻�ڶ����У���Ҫ�����
                          q.add(v);
                          visit[v] = true;
                        }
                        if(sub.linksNo[u][v] == -1)//���·��
                            System.out.println("In SPFA, sub.linksNo["+ u +"]["+v+"] error.");
                    }
                }
            }
            //�ҵ�·���ϵ���̾���minLength
            double minLength = 0;
            double lengthMid = Parameters.MAX_VALUE_DOUBLE;
            int snodeMid,snodeMid1;
            if(p[sNode2] != -1) {
                minLength = sub.link[sNode2].length;
            } else {
                return -1;
            }
            snodeMid1 = sNode2;
            while(p[snodeMid1] != -1) {
                snodeMid = p[snodeMid1];
                if(minLength > sub.link[snodeMid].length)
                    minLength = sub.link[snodeMid].length;
                if(sub.linksNo[snodeMid][snodeMid1] == -1)//���·��
                    System.out.println("In finding the minLength, sub.linksNo["+ snodeMid1 +"]["+sNode1+"] error.");
                snodeMid1 = snodeMid;
                if(snodeMid1 == sNode1) break;//�ӻ�㵽Դ���·���Ѿ�����
            }


            //�ҵ���Ӧ�ĵ��Ƽ����봫���ʣ��Ӷ�ȷ��slot����
            double speedFind = -1;
            boolean find = false;
            for(int i=0;i<sub.modulationLevel;i++){
                if(sub.opticalReach[i] >= speed && sub.opticalReach[i+1] < speed) {
                    speedFind = sub.transRate[i];
                    find = true;
                }
            }
            if(speed < sub.opticalReach[0]) {
                find = true;
                speedFind = sub.opticalReach[0];
            }
            if(speed >= sub.opticalReach[sub.modulationLevel-1]){
                find = true;
                speedFind = sub.opticalReach[sub.modulationLevel-1];
            }
            if(!find) return -1;
            int slotNum = -1;

            speedFind = 75;//12.5;////BPSK 12.5 4000//64QAM 75 12.5
            slotNum = (int)Math.floor(speed/speedFind)+1;

            if(slotNum > sub.slotsNum) return -1;//���������Դ�������ֵ����ʧ���˳�

            //������·�������е���·�Ƿ����㹻��slots���������Ǵ�С����
            int i=0;
            ret[1] = slotNum + difSlotsNum;//���ص�Ƶ�ײ�����mo'gu
            boolean findSlot = false;
            for(i=0;i<sub.slotsNum-slotNum-difSlotsNum;i++){
                snodeMid1 = sNode2;
                while(snodeMid1 != sNode1) {//˵����snodeMid1->sNode1��·����������е�·�����ж��Ƿ����㹻�Ŀ���Ƶ�׿�
                    //�õ�snodeMid1->sNode1����·��
                    if(sub.linksNo[snodeMid1][p[snodeMid1]] == -1)
                        System.out.println("In finding slots, sub.linksNo["+ snodeMid1 +"]["+p[snodeMid1]+"] error.");
                    if(CheckAllPathFreeSlots(sub,sub.linksNo[snodeMid1][p[snodeMid1]],i,slotNum+difSlotsNum) == -1){//failed
                        break;
                    }
                    snodeMid1 = p[snodeMid1];
                }
                if(snodeMid1 == sNode1) {
                    findSlot = true;
                    break;
                }
            }


            //����߼��slot��
            if((i == sub.slotsNum-slotNum-difSlotsNum || i == sub.slotsNum-slotNum) && !findSlot) {//��ĩ�˵Ŀ���Ƶ�׿飬����Ƿ���
                ret[1] = slotNum;//���ص�Ƶ�ײ�����
                findSlot = false;
                for(;i<sub.slotsNum-slotNum;i++){
                    snodeMid1 = sNode2;
                    while(snodeMid1 != sNode1) {//˵����snodeMid1->sNode1��·����������е�·�����ж��Ƿ����㹻�Ŀ���Ƶ�׿�
                        //�õ�snodeMid1->sNode1����·��
                        if(sub.linksNo[snodeMid1][p[snodeMid1]] == -1)
                            System.out.println("sub.linksNo["+ snodeMid1 +"]["+p[snodeMid1]+"] error.");
                        if(CheckAllPathFreeSlots(sub,sub.linksNo[snodeMid1][p[snodeMid1]],i,slotNum) == -1){//failed
                            break;
                        }
                        snodeMid1 = p[snodeMid1];
                    }
                    if(snodeMid1 == sNode1) {
                        findSlot = true;
                        break;
                    }
                }
            }
            //�θ�
            if(!findSlot){
                ret[1] = slotNum;//���ص�Ƶ�ײ�����
                for(i=sub.slotsNum-slotNum;i<=sub.slotsNum-1;i++){//��
                    snodeMid1 = sNode2;
                    while(snodeMid1 != sNode1) {//˵����snodeMid1->sNode1��·����������е�·�����ж��Ƿ����㹻�Ŀ���Ƶ�׿�
                        //�õ�snodeMid1->sNode1����·��
                        if(sub.linksNo[snodeMid1][p[snodeMid1]] == -1)
                            System.out.println("In finding slots, sub.linksNo["+ snodeMid1 +"]["+p[snodeMid1]+"] error.");
                        if(CheckAllPathFreeSlots(sub,sub.linksNo[snodeMid1][p[snodeMid1]],i,slotNum) == -1){//failed
                            break;
                        }
                        snodeMid1 = p[snodeMid1];
                    }
                    if(snodeMid1 == sNode1) {
                        findSlot = true;
                        break;
                    }
                }
            }

            if(i <= sub.slotsNum-slotNum && i >= 0){//˵���ҵ��˿����Ƶ�׿�
                ret[0] = i;
                return 1;
            } else {//failed.
                return -1;
            }
        }
        //******************************************************************
        //���ƣ�int FindPathByFA(......)
        //���ܣ��Ի���K���·����Fragmentation-aware RSA�㷨�ҵ����ʵ�·��
        //������
        //	      sNode1ΪԴ��
        //	      sNode2Ϊ���
        //	      speedΪ��������������ٶ�
        //	      subΪ��������
        //����ֵ��1���ɹ����أ�-1��ʧ�ܷ���
        //******************************************************************
        private int FindPathByFA(EOSubstrateNetwork sub,int sNode1,int sNode2,double speed,int difSlotsNum,int ret[],int p[])
        {
            double speedFind = 75;//12.5;////BPSK 12.5 4000//64QAM 75 12.5
            int slotNum = (int)Math.floor(speed/speedFind) + 1;//����slot������+ difSlotsNum
            //�ҵ�k���·��
            WeightedDirectedGraph myGraph = new WeightedDirectedGraph(sub.nodes);
            myGraph.CreateDireGraph(sub.nodes);//�����ڵ�
            CreateEdge(sub,sNode1,sNode2,myGraph);

            int pathK = Parameters.K_PATH;//5;//ҪѰ�ҵ�K���·��
            int pathRet = -1;//��¼���ص����·������
            pathRet = myGraph.findKShortestPath(pathK,sNode1,sNode2);
            if(pathRet <= 0) return -1;//û��·��������ʧ��

            //�������еĺ�ѡ������cut
            DistanceParent[][] path = myGraph.kShortestPath;//sNode2----->path[sNode2].parentVert
            Hashtable<String,Integer> slotAllocHash=new Hashtable<String,Integer>(2,(float)0.8);//��ѡslot���䷽��
            for(int i=0; i<pathRet; i++){//ÿ��·��i
                GetPath(p,sNode1,sNode2,path[i]);//����p
                GetSolutionNumFromKPaths(sub,p,sNode1,sNode2,slotAllocHash,i,slotNum,difSlotsNum);//��¼cut�ĺ�ѡ����
            }

            //�õ���С��cut����
            int solutionNum = 0;//��¼������Сcut�ĺ�ѡ��������
            Hashtable<String,Integer> minCutHash=new Hashtable<String,Integer>(2,(float)0.8);//��Сcut����,?����2�ǲ��Ǵ��ˣ���Ϊ�ؼ����Ǵ�ŵ�String
            solutionNum = GetMinCutNum(slotAllocHash,minCutHash);//�õ���Сcut�������伯��minCutHash
            if(solutionNum <= 0) return -1;//û��slot����

            //��ֻ��һ����С��cut��ѡ����
            if(solutionNum == 1){//ֻ��һ����Сcut�������򷵻�
                String keyNode1 = "";
                for(Map.Entry entry : minCutHash.entrySet()){
                    keyNode1 = (String)entry.getKey();
                }
                //String keyNode1 = String.valueOf(minCutHash.elements());//keyNode1 = String.valueOf(pathIndex) + "," +String.valueOf(slotNo) + "," + String.valueOf(slotNum);
                int position = keyNode1.indexOf(",");
                int pathIndex = Integer.parseInt(keyNode1.substring(0,position));
                keyNode1 = keyNode1.substring(position+1);
                position = keyNode1.indexOf(",");
                int slotNo = Integer.parseInt(keyNode1.substring(0,position));
                int findSlotNum = Integer.parseInt(keyNode1.substring(position+1));
                ret[0] = slotNo;//���ص���ʼslot���
                ret[1] = findSlotNum;//���ص�Ƶ�ײ�����

                GetPath(p,sNode1,sNode2,path[pathIndex]);//����p
                return 1;//�ɹ�����
            }

            //���򣬴Ӷ����ͬ��cut�������ҵ���С��misalignment
            int[] ret1 = new int[3];
            GetMinMisalignment(sub,path,minCutHash,sNode1,sNode2,ret1);//ret[0]:·��i��ret[1]:slot���

            ret[0] = ret1[1];//���ص���ʼslot���
            ret[1] = ret1[2];//���ص�Ƶ�ײ�����
            GetPath(p,sNode1,sNode2,path[ret1[0]]);//����p
            return 1;
        }

        //�Ӷ����ͬ��cut�������ҵ���С��misalignment
        //����
        //ret[0] = pathIndexArr[findMinMisali];//·��index
        //ret[1] = slotNoArr[findMinMisali];//���ص���ʼslot���
        //ret[2] = slotNumArr[findMinMisali];//���ص�slotNum
        private void GetMinMisalignment(EOSubstrateNetwork sub,DistanceParent[][] path,Hashtable<String,Integer> minCutHash,int sNode1,int sNode2,int ret[])
        {
            int slotNoArr[] = new int[minCutHash.size()];
            int misalignmentArr[] = new int[minCutHash.size()];
            int pathIndexArr[] = new int[minCutHash.size()];
            int slotNumArr[] = new int[minCutHash.size()];
            String str = "";
            int minCutIndex = 0;

            //����misalignment
            for(Map.Entry entry : minCutHash.entrySet()){
                str = (String)entry.getKey();
                int position = str.indexOf(",");
                int pathIndex = Integer.parseInt(str.substring(0, position));
                str = str.substring(position+1);
                position = str.indexOf(",");
                int slotNo = Integer.parseInt(str.substring(0,position));
                int slotNum = Integer.parseInt(str.substring(position+1));

                int misalig = GetMisalignment(sub,path,pathIndex,slotNo,slotNum,sNode1,sNode2);//�õ�misalignment

                //��¼��minMisalignment
                pathIndexArr[minCutIndex] = pathIndex;
                slotNoArr[minCutIndex] = slotNo;
                misalignmentArr[minCutIndex] = misalig;
                slotNumArr[minCutIndex] = slotNum;
                minCutIndex ++;
            }

            int minMisalignment = Parameters.MAX_VALUE_INT;
            int minSlotNo = -1;
            int pathIndex = -1;
            int findMinMisali = -1;
            //����С��misalignment
            for(int i = 0; i < minCutIndex; i++){
                if(misalignmentArr[i] < minMisalignment){
                    minMisalignment = misalignmentArr[i];
                    findMinMisali = i;
                }
            }
            //����
            ret[0] = pathIndexArr[findMinMisali];//·��index
            ret[1] = slotNoArr[findMinMisali];//���ص���ʼslot���
            ret[2] = slotNumArr[findMinMisali];//���ص�slotNum
        }

        //�õ�misalignment
        //����ֵΪmisalignment
        private int GetMisalignment(EOSubstrateNetwork sub,DistanceParent[][] path,int pathIndex,int slotNo,int slotNum,int sNode1,int sNode2)
        {
            //�õ�sNode1->sNode2·��p
            int p[] = new int[sub.nodes];
            int msialignment = 0;
            GetPath(p,sNode1,sNode2,path[pathIndex]);//����p

            //���δ�sNode2��sNode1�ĸ�����·���ҵ����Ӧ����sNode2Ϊ�ڵ���ڽ���·misalignment
            while(sNode2 != sNode1){
                //sNode2->p[sNode2]����·
                for(int i = 0; i < sub.links; i++){
                    //�ҵ��ڽ���·
                    if(sub.link[i].from == sNode2 && sub.link[i].to != p[sNode2]){
                        //������·i��misalignment
                        msialignment += GetMisalignmentByOneLink(sub,i,slotNo,slotNum);
                    } else if(sub.link[i].to == sNode2 && sub.link[i].from != p[sNode2]){
                        //������·i��misalignment
                        msialignment += GetMisalignmentByOneLink(sub,i,slotNo,slotNum);
                    }
                }
                sNode2 = p[sNode2];
            }
            return msialignment;
        }

        //������·linkNo��misalignment
        private int GetMisalignmentByOneLink(EOSubstrateNetwork sub,int linkNo,int slotNo,int slotNum)
        {
            int misalignment = 0;
            for(int i = slotNo; i < slotNo + slotNum; i ++){
                if(sub.slots[linkNo][i] == 1) misalignment++;
                else if(sub.slots[linkNo][i] == 0) misalignment--;
                else System.out.println("GetMisalignmentByOneLink is error.****************");
            }
            return misalignment;
        }

        //�õ���Сcut�������伯��minCutHash
        private int GetMinCutNum(Hashtable<String,Integer> slotAllocHash,Hashtable<String,Integer> minCutHash)
        {
            int minCut = Parameters.MAX_VALUE_INT;
            String str = "";
            int cutNum = 0;
            //�õ���С��minCut
            for(Map.Entry entry : slotAllocHash.entrySet()){
                if(minCut > (Integer)entry.getValue())
                    minCut = (Integer)entry.getValue();
            }
            //�õ���С��minCut����
            //int cutNum = 0;
            for(Map.Entry entry : slotAllocHash.entrySet()){
                if(minCut == (int)entry.getValue()){
                    minCut = (int)entry.getValue();
                    cutNum ++;
                    minCutHash.put((String)entry.getKey(),(Integer)entry.getValue());
                }
            }

            return cutNum;
        }
        //��¼cut�ĺ�ѡ����
        private void GetSolutionNumFromKPaths(EOSubstrateNetwork sub,int p[],int sNode1,int sNode2,Hashtable slotAllocHash,int pathIndex,int slotNum,int difSlotsNum)
        {
            //�ӵ͵��߼��slot��
            for(int i=0;i<sub.slotsNum-slotNum-difSlotsNum;i++){//��
                //���·�������е�slot���Ƿ���Ч
                boolean effective = true;
                int sNodeTmp = sNode2;
                while(sNodeTmp != sNode1)	{
                    int linkNum = GetLinkNum(sub,p[sNodeTmp],sNodeTmp);
                    if(linkNum == -1) {
                        System.out.println("GetSolutionNumFromKPaths is error.********************");
                        break;
                    }
                    if(CheckAllPathFreeSlots(sub,linkNum,i,slotNum+difSlotsNum) == -1){
                        effective = false;
                        break;
                    }
                    sNodeTmp = p[sNodeTmp];
                }
                if(effective == true){
                    int cut = GetCut(sub,p,i,sNode1,sNode2); //����cut
                    RecordFeasiResolve(slotAllocHash,cut,pathIndex,i,slotNum+difSlotsNum);//��¼��ѡ����
                }
            }

            //����߼��slot��
            for(int i=sub.slotsNum-slotNum;i<=sub.slotsNum-slotNum;i++){//��
                //���·�������е�slot���Ƿ���Ч
                if(i < 0){
                    //effective = false;
                    return ;
                }
                boolean effective = true;
                int sNodeTmp = sNode2;
                while(sNodeTmp != sNode1)	{
                    int linkNum = GetLinkNum(sub,p[sNodeTmp],sNodeTmp);
                    if(CheckAllPathFreeSlots(sub,linkNum,i,slotNum) == -1){
                        effective = false;
                        break;
                    }
                    sNodeTmp = p[sNodeTmp];
                }
                if(effective == true){
                    int cut = GetCut(sub,p,i,sNode1,sNode2); //����cut
                    RecordFeasiResolve(slotAllocHash,cut,pathIndex,i,slotNum);//��¼��ѡ����
                }
            }

        }







    }
    public double[] simplenewFindEGminpathDistance (EOSubstrateNetwork sub,List<Path> myPath){
        List path1= null,path2= null;
        String finallypath1=null,finallypath2=null;
        path1=myPath.get(0).getVertexList();
        //path2=myPath.get(1).getVertexList();
        String[] patharray = new String[myPath.size()];//�ַ������͵�·������,[[], [], ..., []]
        int [][] pathintarray= new int[myPath.size()][];//��ÿ��·���Ķ��㱣���ڶ�ά����������myPath.size()�У���̬������ÿһ�еĳ��Ⱥ�ÿ��·�������Ľڵ��й�
        for(int i=0;i<myPath.size();i++){
            patharray[i]=myPath.get(i).getVertexList().toString();
            //ȥ���ַ��е����źͿո�
            String clearPath =patharray[i].replace("[","").replace("]","").replace(" ","");
            String[] numberStrings = clearPath.split(",");
            int[] numbers = new int[numberStrings.length];
            for (int j = 0; j < numberStrings.length; j++) {
                numbers[j] = Integer.parseInt(numberStrings[j]);
            }
            pathintarray[i]=numbers;
        }

        int finallypath1len=0;
        int finallypath2len=0;
        double pathleng1=0;
        double pathleng2=0;
        double sumpathsleng=-1;
        double realsumpathsleng=Parameters.MAX_VALUE_DOUBLE;//1
        int finallyindex1=-1;
        int finallyindex2=-1;
        double[] finallyarray=new double[3];//���Ҫ����ȥ�����飬��һ����·�����Ⱥͣ��ڶ�����ѡ��ĵ�һ��·���������������ǵڶ���·����������myPath�е�������
        if(path1.size()==2||path1.size()>2){
            finallyindex1=0;
            finallyindex2=0;
            finallypath1=path1.toString();
            //finallypath2=path2.toString();
            // System.out.println("finally 1:"+finallypath1+" 2:"+finallypath2);
            finallypath1len=path1.size();
            //finallypath2len=path2.size();
            for(int a=0;a<finallypath1len-1;a++){
                for(int b=0;b<sub.links;b++){
                    int fnode=pathintarray[0][a];
                    int snode=pathintarray[0][a+1];
                    if((fnode==sub.link[b].from&&snode==sub.link[b].to)||(snode==sub.link[b].from&&fnode==sub.link[b].to)){
                        pathleng1=sub.link[b].length+pathleng1;
                    }
                }
            }

//            for(int a=0;a<finallypath2len-1;a++){
//                for(int b=0;b<sub.links;b++){
//                    int fnode=pathintarray[1][a];
//                    int snode=pathintarray[1][a+1];
//                    if((fnode==sub.link[b].from&&snode==sub.link[b].to)||(snode==sub.link[b].from&&fnode==sub.link[b].to)){
//                        pathleng2=sub.link[b].length+pathleng2;
//                    }
//                }
//            }
            sumpathsleng=pathleng1;//2

        }
//        else if(path1.size()>2){
//            ArrayList<List<Integer>> pathlist = new ArrayList<>();//����һ��Ԫ��Ϊ�б�����飬������ֻʣ�м�ڵ��·��������
//            for(int k=0;k<myPath.size();k++){
//                List<Integer> newpath=new ArrayList<>();//ֻʣ�м�ڵ��·��
//                int subpathlen = myPath.get(k).getVertexList().size();
//                for(int j=0;j<subpathlen;j++){
//                    int insertnum=pathintarray[k][j];
//                    newpath.add(insertnum);
//                }
//                newpath.remove(0);
//                newpath.remove(newpath.size()-1);
//                pathlist.add(newpath);
//            }
//            System.out.println("ֻʣ�м�ڵ��·������"+pathlist);
//            ArrayList<List<Integer>> TwoPathlist = new ArrayList<>();//����һ���������������ཻ·���������б�ļ���
//            boolean intersectionFlag=false;
//            for(int i=0;i<pathlist.size()-1;i++){
//                for(int j=i+1;j<pathlist.size();j++){
//                    List<Integer> element1=pathlist.get(i);
//                    List<Integer> element2=pathlist.get(j);
//                    List<Integer> intersection=new ArrayList<>(element1);
//                    intersection.retainAll(element2);//�ж�����·���Ƿ��ཻ��û�н������ǲ��ཻ
//                    if(intersection.isEmpty()){//�ж�Ԫ��1��Ԫ��2�Ľ����Ƿ�Ϊ��
//                        intersectionFlag=true;
//                        List<Integer> recordpathindex= new ArrayList<>();//���������ཻ��·����mypath�е�������ɵ��б�
//                        recordpathindex.add(i);
//                        recordpathindex.add(j);
//                        TwoPathlist.add(recordpathindex);
//                    }
//                }
//            }
//            if(intersectionFlag){
//                System.out.println("�����ཻ·����������ļ��ϣ�"+TwoPathlist);
//                for(int i=0;i<TwoPathlist.size();i++){//3
//                    List<Integer> TwoPathindex=TwoPathlist.get(i);
//                    if(realsumpathsleng>getTwolenghSum(myPath,pathintarray,TwoPathindex)){
//                        realsumpathsleng=getTwolenghSum(myPath,pathintarray,TwoPathindex);
//                    }
//                }
//                ////sumpathsleng=realsumpathsleng;//4
//                int index1=TwoPathlist.get(0).get(0);
//                int index2=TwoPathlist.get(0).get(1);
//                finallyindex1=index1;
//                finallyindex2=index2;
//                finallypath1=myPath.get(index1).getVertexList().toString();//=patharray[TwoPathlist.get(0).get(0)]
//                finallypath2=myPath.get(index2).getVertexList().toString();
//                System.out.println("finally 1:"+finallypath1+" 2:"+finallypath2);
//                finallypath1len=myPath.get(index1).getVertexList().size();
//                finallypath2len=myPath.get(index2).getVertexList().size();
//                for(int a=0;a<finallypath1len-1;a++){
//                    for(int b=0;b<sub.links;b++){
//                        int fnode=pathintarray[index1][a];
//                        int snode=pathintarray[index1][a+1];
//                        if((fnode==sub.link[b].from&&snode==sub.link[b].to)||(snode==sub.link[b].from&&fnode==sub.link[b].to)){
//                            pathleng1=sub.link[b].length+pathleng1;
//                        }
//                    }
//                }
//                //System.out.println(pathleng1);
//                for(int a=0;a<finallypath2len-1;a++){
//                    for(int b=0;b<sub.links;b++){
//                        int fnode=pathintarray[index2][a];
//                        int snode=pathintarray[index2][a+1];
//                        if((fnode==sub.link[b].from&&snode==sub.link[b].to)||(snode==sub.link[b].from&&fnode==sub.link[b].to)){
//                            pathleng2=sub.link[b].length+pathleng2;
//                        }
//                    }
//                }
//                sumpathsleng=pathleng1;
//                //System.out.println(pathleng2);
//            }else{
//                System.out.println("δ�ҵ������ཻ������·��");
//                finallyarray[0]=-1;
//                finallyarray[1]=-1;
//                finallyarray[2]=-1;
//                return finallyarray;
//            }
//        }
        ////sumpathsleng=pathleng1+pathleng2;//5
        finallyarray[0]=sumpathsleng;
        finallyarray[1]=finallyindex1;
        finallyarray[2]=finallyindex2;
        System.out.println(sumpathsleng+", "+finallyindex1+", "+finallyindex2);
        return finallyarray;
    }
    //��ȡ��̵Ĳ��ཻ������·���ĳ���֮��,23.6.6
    public double getTwolenghSum(List<Path> myPath,int [][] pathintarray,List<Integer> TwoPathindex){
        double lenghsum=Parameters.MAX_VALUE_DOUBLE;
        double pathleng1=0;
        double pathleng2=0;
        int index1=TwoPathindex.get(0);
        int index2=TwoPathindex.get(1);
        int finallypath1len=myPath.get(index1).getVertexList().size();
        int finallypath2len=myPath.get(index2).getVertexList().size();
        for(int a=0;a<finallypath1len-1;a++){
            for(int b=0;b<sub.links;b++){
                int fnode=pathintarray[index1][a];
                int snode=pathintarray[index1][a+1];
                if((fnode==sub.link[b].from&&snode==sub.link[b].to)||(snode==sub.link[b].from&&fnode==sub.link[b].to)){
                    pathleng1=sub.link[b].length+pathleng1;
                }
            }
        }
        for(int a=0;a<finallypath2len-1;a++){
            for(int b=0;b<sub.links;b++){
                int fnode=pathintarray[index2][a];
                int snode=pathintarray[index2][a+1];
                if((fnode==sub.link[b].from&&snode==sub.link[b].to)||(snode==sub.link[b].from&&fnode==sub.link[b].to)){
                    pathleng2=sub.link[b].length+pathleng2;
                }
            }
        }
        lenghsum=pathleng1+pathleng2;
        return lenghsum;
    }
    //******************************************************************
    //2023.5.27
    //����EGͼ�е�Ե�֮��ľ���,����������·�������;��볤�ȣ�����finallyarray��
    //******************************************************************
    public double[] newFindEGminpathDistance (EOSubstrateNetwork sub,List<Path> myPath){
        List path1= null,path2= null;
        String finallypath1=null,finallypath2=null;
        path1=myPath.get(0).getVertexList();
        path2=myPath.get(1).getVertexList();
        String[] patharray = new String[myPath.size()];//�ַ������͵�·������,[[], [], ..., []]
        int [][] pathintarray= new int[myPath.size()][];//��ÿ��·���Ķ��㱣���ڶ�ά����������myPath.size()�У���̬������ÿһ�еĳ��Ⱥ�ÿ��·�������Ľڵ��й�
        for(int i=0;i<myPath.size();i++){
            patharray[i]=myPath.get(i).getVertexList().toString();
            //ȥ���ַ��е����źͿո�
            String clearPath =patharray[i].replace("[","").replace("]","").replace(" ","");
            String[] numberStrings = clearPath.split(",");
            int[] numbers = new int[numberStrings.length];
            for (int j = 0; j < numberStrings.length; j++) {
                numbers[j] = Integer.parseInt(numberStrings[j]);
            }
            pathintarray[i]=numbers;
        }

        int finallypath1len=0;
        int finallypath2len=0;
        double pathleng1=0;
        double pathleng2=0;
        double sumpathsleng=-1;
        double realsumpathsleng=Parameters.MAX_VALUE_DOUBLE;//1
        int finallyindex1=-1;
        int finallyindex2=-1;
        double[] finallyarray=new double[3];//���Ҫ����ȥ�����飬��һ����·�����Ⱥͣ��ڶ�����ѡ��ĵ�һ��·���������������ǵڶ���·����������myPath�е�������
        if(path1.size()==2){
            finallyindex1=0;
            finallyindex2=1;
            finallypath1=path1.toString();
            finallypath2=path2.toString();
            System.out.println("finally 1:"+finallypath1+" 2:"+finallypath2);
            finallypath1len=path1.size();
            finallypath2len=path2.size();
            for(int a=0;a<finallypath1len-1;a++){
                for(int b=0;b<sub.links;b++){
                    int fnode=pathintarray[0][a];
                    int snode=pathintarray[0][a+1];
                    if((fnode==sub.link[b].from&&snode==sub.link[b].to)||(snode==sub.link[b].from&&fnode==sub.link[b].to)){
                        pathleng1=sub.link[b].length+pathleng1;
                    }
                }
            }
            //System.out.println(pathleng1);
            for(int a=0;a<finallypath2len-1;a++){
                for(int b=0;b<sub.links;b++){
                    int fnode=pathintarray[1][a];
                    int snode=pathintarray[1][a+1];
                    if((fnode==sub.link[b].from&&snode==sub.link[b].to)||(snode==sub.link[b].from&&fnode==sub.link[b].to)){
                        pathleng2=sub.link[b].length+pathleng2;
                    }
                }
            }
            sumpathsleng=pathleng1+pathleng2;//2
            //System.out.println(pathleng2);
        }else if(path1.size()>2){
            ArrayList<List<Integer>> pathlist = new ArrayList<>();//����һ��Ԫ��Ϊ�б�����飬������ֻʣ�м�ڵ��·��������
            for(int k=0;k<myPath.size();k++){
                List<Integer> newpath=new ArrayList<>();//ֻʣ�м�ڵ��·��
                int subpathlen = myPath.get(k).getVertexList().size();
                for(int j=0;j<subpathlen;j++){
                    int insertnum=pathintarray[k][j];
                    newpath.add(insertnum);
                }
                newpath.remove(0);
                newpath.remove(newpath.size()-1);
                pathlist.add(newpath);
            }
            System.out.println("ֻʣ�м�ڵ��·������"+pathlist);
            ArrayList<List<Integer>> TwoPathlist = new ArrayList<>();//����һ���������������ཻ·���������б�ļ���
            boolean intersectionFlag=false;
            for(int i=0;i<pathlist.size()-1;i++){
                for(int j=i+1;j<pathlist.size();j++){
                    List<Integer> element1=pathlist.get(i);
                    List<Integer> element2=pathlist.get(j);
                    List<Integer> intersection=new ArrayList<>(element1);
                    intersection.retainAll(element2);//�ж�����·���Ƿ��ཻ��û�н������ǲ��ཻ
                    if(intersection.isEmpty()){//�ж�Ԫ��1��Ԫ��2�Ľ����Ƿ�Ϊ��
                        intersectionFlag=true;
                        List<Integer> recordpathindex= new ArrayList<>();//���������ཻ��·����mypath�е�������ɵ��б�
                        recordpathindex.add(i);
                        recordpathindex.add(j);
                        TwoPathlist.add(recordpathindex);
                    }
                }
            }
            if(intersectionFlag){
                System.out.println("�����ཻ·����������ļ��ϣ�"+TwoPathlist);
                int TwoPathlistindex=-1;
                for(int i=0;i<TwoPathlist.size();i++){//3
                    List<Integer> TwoPathindex=TwoPathlist.get(i);
                    if(realsumpathsleng>getTwolenghSum(myPath,pathintarray,TwoPathindex)){
                        realsumpathsleng=getTwolenghSum(myPath,pathintarray,TwoPathindex);
                        TwoPathlistindex=i;
                    }
                }
                sumpathsleng=realsumpathsleng;//4
//                int index1=TwoPathlist.get(0).get(0);
//                int index2=TwoPathlist.get(0).get(1);
                int index1=TwoPathlist.get(TwoPathlistindex).get(0);//6
                int index2=TwoPathlist.get(TwoPathlistindex).get(1);
                finallyindex1=index1;
                finallyindex2=index2;
                finallypath1=myPath.get(index1).getVertexList().toString();//=patharray[TwoPathlist.get(0).get(0)]
                finallypath2=myPath.get(index2).getVertexList().toString();
                System.out.println("finally 1:"+finallypath1+" 2:"+finallypath2);
                finallypath1len=myPath.get(index1).getVertexList().size();
                finallypath2len=myPath.get(index2).getVertexList().size();
                for(int a=0;a<finallypath1len-1;a++){
                    for(int b=0;b<sub.links;b++){
                        int fnode=pathintarray[index1][a];
                        int snode=pathintarray[index1][a+1];
                        if((fnode==sub.link[b].from&&snode==sub.link[b].to)||(snode==sub.link[b].from&&fnode==sub.link[b].to)){
                            pathleng1=sub.link[b].length+pathleng1;
                        }
                    }
                }
                //System.out.println(pathleng1);
                for(int a=0;a<finallypath2len-1;a++){
                    for(int b=0;b<sub.links;b++){
                        int fnode=pathintarray[index2][a];
                        int snode=pathintarray[index2][a+1];
                        if((fnode==sub.link[b].from&&snode==sub.link[b].to)||(snode==sub.link[b].from&&fnode==sub.link[b].to)){
                            pathleng2=sub.link[b].length+pathleng2;
                        }
                    }
                }
                //System.out.println(pathleng2);
            }else{
                System.out.println("δ�ҵ������ཻ������·��");
                finallyarray[0]=-1;
                finallyarray[1]=-1;
                finallyarray[2]=-1;
                return finallyarray;
            }
        }
        //sumpathsleng=pathleng1+pathleng2;//5
        finallyarray[0]=sumpathsleng;
        finallyarray[1]=finallyindex1;
        finallyarray[2]=finallyindex2;
        System.out.println(sumpathsleng+", "+finallyindex1+", "+finallyindex2);
        return finallyarray;
    }
    /*���ʹ��EG_Graph
            ArrayList<Object> AList=new ArrayList<>();//����һ���µ�ArrayList<Object>
            AList=EG_Graph(sub);//��ȡEG_Graph�ķ���ֵ��AList��EG_Graph�ķ���ֵ����һ������һ�£�
            //��ȡֵ�ľ�����֮��ȡ����ֱֵ�Ӷ�ά����
            double[][] array1=(double[][])AList.get(0);
            double[][] array2=(double[][])AList.get(1);
            double[][] array3=(double[][])AList.get(2);
            String[][] array4=(String[][])AList.get(3);
            String[][] array5=(String[][])AList.get(4);
            System.out.println("==============");
            for (int i = 0; i < array2.length; i++) {
                for (int j = 0; j < array2.length; j++) {
                    double element = array2[i][j];
                    System.out.print(element + " ");
                }
                System.out.println();
            }
     */
    //******************************************************************
    //ESE_VONE 2023.5.26
    //lyy
    //EGͼ
    //******************************************************************
    public ArrayList<Object> EG_Graph (EOSubstrateNetwork sub)
    {
        ArrayList<Object> AList=new ArrayList<>();//����һ��ArrayList���洢�����еĶ����ά����
        class EGObject {
            public double[][] arraylength;//�����ȡ�ľ���
            public double[][] index1;//�����һ��·����myPath�е�·������
            public double[][] index2;//����ڶ���·����myPath�е�·������
            public String[][] path1;//��һ��·�����ַ�����ʽ
            public String[][] path2;//�ڶ���·�����ַ�����ʽ
            public EGObject(double[][] arraylength, double[][] index1,double[][] index2,String [][] path1, String [][] path2) {
                this.arraylength = arraylength;
                this.index1 = index1;
                this.index2 = index2;
                this. path1 =  path1;
                this. path2 =  path2;
            }

        }
        double[][] arraylength=new double[sub.nodes][sub.nodes];
        double[][] index1=new double[sub.nodes][sub.nodes];
        double[][] index2=new double[sub.nodes][sub.nodes];
        String[][] path1=new String[sub.nodes][sub.nodes];
        String[][] path2=new String[sub.nodes][sub.nodes];
        EGObject obj = new EGObject(arraylength, index1,index2, path1, path2);
        //��ʼ��
        for(int j = 0; j < sub.nodes; j++ ) {
            for (int k = 0; k < sub.nodes; k++ ) {
                arraylength[j][k] = Parameters.MAX_VALUE_DOUBLE;
                index1[j][k] = -1;
                index2[j][k] = -1;
            }
        }
        double[] outputArray=new double[3];//�����洢newFindEGminpathDistance�����ķ���ֵ

        for(int j = 0; j < sub.nodes-1; j++ ) {
            for (int k = j+1; k < sub.nodes; k++) {
                int pathRet = -1;
                int KpathNumber=230;
                DistanceParent[][] kSPath = new DistanceParent[KpathNumber][sub.nodes];//�洢k���·�������ݽṹ
                pathRet = GetKShortestPath(sub, j , k, kSPath);//�����Testing batch processing of top-k shortest paths!
                String graphData = "graph.data";
                List<Path> myPath;
                YenTopKShortestPathsAlgTest myTest = new YenTopKShortestPathsAlgTest(graphData);
                myPath = myTest.testYenShortestPathsAlg(KpathNumber, j, k);//Testing batch processing of top-k shortest paths!
                System.out.println(j+"---->"+k+":"+myPath);

                outputArray=simplenewFindEGminpathDistance(sub,myPath);//simplenewFindEGminpathDistance(sub,myPath);//newFindEGminpathDistance (sub,myPath);
                //�������еĶ�ά���鸳ֵ
                obj.arraylength[j][k]=obj.arraylength[k][j]=outputArray[0];
                obj.index1[j][k]=obj.index1[k][j]=outputArray[1];
                obj.index2[j][k]=obj.index2[k][j]=outputArray[2];
                obj.path1[j][k]=obj.path1[k][j]=myPath.get((int)outputArray[1]).getVertexList().toString();
                obj.path2[j][k]=obj.path2[k][j]=myPath.get((int)outputArray[2]).getVertexList().toString();
            }
        }
        //������Ķ�ά�������AList
        AList.add(obj.arraylength);
        AList.add(obj.index1);
        AList.add(obj.index2);
        AList.add(obj.path1);
        AList.add(obj.path2);

//        for (int i = 0; i < obj.index2.length; i++) {
//            for (int j = 0; j < obj.index2.length; j++) {
//                double element = obj.index2[i][j];
//                System.out.print(element + " ");
//            }
//            System.out.println();
//        }
        return AList;
    }
    public ArrayList<Object> SubNetGraph (EOSubstrateNetwork sub,int FirstNetNumber,int SecondNetNumber){
//        ArrayList<Object> SubAList=new ArrayList<>();//����һ����ά����ArrayList���洢���������
//        class SubNetObject {
//            public List<Integer> SubNet1;//��һ���������б�
//            public List<Integer> SubNet2;//�ڶ����������б�
//            public SubNetObject(List<Integer> SubNet1, List<Integer> SubNet2) {
//                this.SubNet1 = SubNet1;
//                this.SubNet2 = SubNet2;
//            }
//        }
//        //int FirstNetNumber=6;//��һ�����������ڵ�����
//        //int SecondNetNumber=12;//�ڶ������������ڵ�����
//        List<Integer> SubNet1=new ArrayList<>();
//        List<Integer> SubNet2=new ArrayList<>();
//        SubNetObject obj = new SubNetObject(SubNet1,SubNet2);
//        //��ȡ�ײ�ڵ��NodeRankֵ
//        double sNodePageRank[] = new double[sub.nodes];
//        int nSortSNode[] = new int[sub.nodes];
//        sNodePageRank= InitSNodeEnergyPageRank(sNodePageRank, sub);
//        sort(sNodePageRank,nSortSNode);
//        //��ȡ��һ���������׵�
//        SubNet1.add(nSortSNode[0]);
//        //��һ���������������noderankֵ�����Ľڵ㣬��һ�������ڵ㣬��noderankֵ�������
//        for(int i=1;i<nSortSNode.length;i++){
//            for(int j=0;j<sub.links;j++){
//                if(((nSortSNode[0]==sub.link[j].from&&nSortSNode[i]==sub.link[j].to)||(nSortSNode[i]==sub.link[j].from&&nSortSNode[0]==sub.link[j].to))&&SubNet1.size()<FirstNetNumber&&!SubNet1.contains(nSortSNode[i])){
//                    SubNet1.add(nSortSNode[i]);
//                }
//            }
//        }
//        //copy���еĵ�һ�������糤��
//        int SubNet1length=SubNet1.size();
//        //��һ�������������һ��ڵ������Ľڵ㣬���õڶ��������ڵ�
//        for(int i=1;i<nSortSNode.length;i++){
//            if(NodeToNodeConnect(1,i,SubNet1length,SubNet1,nSortSNode)){
//                if(SubNet1.size()<FirstNetNumber&&!SubNet1.contains(nSortSNode[i])){
//                    SubNet1.add(nSortSNode[i]);
//                }
//            }
//        }
//        //����һ�������紫������
//        obj.SubNet1=SubNet1;
//        //ȡ�ڶ�����������׽ڵ�
//        for(int i=0;i<nSortSNode.length;i++){
//            if(!SubNet1.contains(nSortSNode[i])){
//                SubNet2.add(nSortSNode[i]);
//                break;
//            }
//        }
//
//        //�ڶ����������������noderankֵ�����Ľڵ㣬��һ�������ڵ㣬��noderankֵ�������
//        for(int i=1;i<nSortSNode.length;i++){
//            for(int j=0;j<sub.links;j++){
//                if(((SubNet2.get(0)==sub.link[j].from&&nSortSNode[i]==sub.link[j].to)||(nSortSNode[i]==sub.link[j].from&&SubNet2.get(0)==sub.link[j].to))&&SubNet2.size()<SecondNetNumber&&!SubNet1.contains(nSortSNode[i])&&!SubNet2.contains(nSortSNode[i])){
//                    SubNet2.add(nSortSNode[i]);
//                }
//            }
//        }
//        //copy���еĵ�һ�������糤��
//        int SubNet2length=SubNet2.size();
//        //�ڶ��������������һ��ڵ������Ľڵ㣬���õڶ��������ڵ�
//        for(int i=1;i<nSortSNode.length;i++){
//            if(NodeToNodeConnect(1,i,SubNet2length,SubNet2,nSortSNode)){
//                if(SubNet2.size()<SecondNetNumber&&!SubNet1.contains(nSortSNode[i])&&!SubNet2.contains(nSortSNode[i])){
//                    SubNet2.add(nSortSNode[i]);
//                }
//            }
//        }
//        //���ڶ��������紫������
//        obj.SubNet2=SubNet2;
//
//        SubAList.add(obj.SubNet1);
//        SubAList.add(obj.SubNet2);
//
////        for(int i=0;i<SubNet1.size();i++){
////            System.out.print(SubNet1.get(i));
////        }
//        return SubAList;

        ArrayList<Object> SubAList=new ArrayList<>();//����һ����ά����ArrayList���洢���������
        class SubNetObject {
            public List<Integer> SubNet1;//��һ���������б�
            public List<Integer> SubNet2;//�ڶ����������б�
            public  List<Integer> SubNetsort;//��ȥ����������ʣ�µ�NRֵ����
            public  double[] energysNodePageRank;//�״�NRֵ
            public SubNetObject(List<Integer> SubNet1, List<Integer> SubNet2,List<Integer> SubNetsort ,double[] energysNodePageRank) {
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
        SubNetObject obj = new SubNetObject(SubNet1,SubNet2,SubNetsort,energysNodePageRank);
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
        //��һ�������������һ��ڵ������Ľڵ㣬���õڶ��������ڵ�
        for(int i=1;i<nSortSNode.length;i++){
            if(NodeToNodeConnect(1,i,SubNet1length,SubNet1,nSortSNode)){
                if(SubNet1.size()<FirstNetNumber&&!SubNet1.contains(nSortSNode[i])){
                    SubNet1.add(nSortSNode[i]);
                }
            }
        }
        int SubNet1length1=SubNet1.size();
        //��һ������������ڶ���ڵ������Ľڵ㣬���õ����������ڵ�
        for(int i=1;i<nSortSNode.length;i++){
            if(NodeToNodeConnect(SubNet1length,i,SubNet1length1,SubNet1,nSortSNode)){
                if(SubNet1.size()<FirstNetNumber&&!SubNet1.contains(nSortSNode[i])){
                    SubNet1.add(nSortSNode[i]);
                }
            }
        }
        //����һ�������紫������
        obj.SubNet1=SubNet1;
        //ȡ�ڶ�����������׽ڵ�
        for(int i=0;i<nSortSNode.length;i++){
            if(!SubNet1.contains(nSortSNode[i])){
                SubNet2.add(nSortSNode[i]);
                break;
            }
        }
        //�ڶ����������������noderankֵ�����Ľڵ㣬��һ�������ڵ㣬��noderankֵ�������
        for(int i=1;i<nSortSNode.length;i++){
            for(int j=0;j<sub.links;j++){
                if(((SubNet2.get(0)==sub.link[j].from&&nSortSNode[i]==sub.link[j].to)||(nSortSNode[i]==sub.link[j].from&&SubNet2.get(0)==sub.link[j].to))&&SubNet2.size()<SecondNetNumber&&!SubNet1.contains(nSortSNode[i])&&!SubNet2.contains(nSortSNode[i])){
                    SubNet2.add(nSortSNode[i]);
                }
            }
        }
        //copy���еĵ�һ�������糤��
        int SubNet2length=SubNet2.size();
        //�ڶ��������������һ��ڵ������Ľڵ㣬���õڶ��������ڵ�
        for(int i=1;i<nSortSNode.length;i++){
            if(NodeToNodeConnect(1,i,SubNet2length,SubNet2,nSortSNode)){
                if(SubNet2.size()<SecondNetNumber&&!SubNet1.contains(nSortSNode[i])&&!SubNet2.contains(nSortSNode[i])){
                    SubNet2.add(nSortSNode[i]);
                }
            }
        }
        int SubNet2length2=SubNet2.size();
        //�ڶ�������������ڶ���ڵ������Ľڵ㣬���õ����������ڵ�
        for(int i=1;i<nSortSNode.length;i++){
            if(NodeToNodeConnect(SubNet2length,i,SubNet2length2,SubNet2,nSortSNode)){
                if(SubNet2.size()<SecondNetNumber&&!SubNet1.contains(nSortSNode[i])&&!SubNet2.contains(nSortSNode[i])){
                    SubNet2.add(nSortSNode[i]);
                }
            }
        }
        //���ڶ��������紫������
        obj.SubNet2=SubNet2;

        for (int i=0;i<nSortSNode.length;i++){
            if(!SubNet1.contains(nSortSNode[i])&&!SubNet2.contains(nSortSNode[i]))
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

    //�ǵ�������
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
    //�ڵ�i�Ƿ����һ��ڵ�������һ���ڵ�������indexΪ��һ��ĵ�һ��������
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
    //·���ַ���ת����һά����
    public int[] Getpath_array( String path){
        String clearPath =path.replace("[","").replace("]","").replace(" ","");
        String[] numberStrings = clearPath.split(",");
        int[] numbers = new int[numberStrings.length];
        for (int j = 0; j < numberStrings.length; j++) {
            numbers[j] = Integer.parseInt(numberStrings[j]);
        }
        return numbers;
    }
}






