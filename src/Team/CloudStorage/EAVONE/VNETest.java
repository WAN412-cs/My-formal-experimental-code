package Team.CloudStorage.EAVONE;
import java.util.*;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;

import javax.swing.JOptionPane;//.swing.JOptionPane;

public class VNETest {
	public static void main(String [] args)  
	{
		try{
			String SNFile,VNsFile;
			SNFile = "sub.txt";
			//SNFile = "sub-24-43-EON.txt";//���ģ
			//SNFile = "sub-28-41-EON.txt";//���ģ
			//SNFile = "sub-10-14-EON.txt";
//			SNFile = "sub-6-8-EON.txt";//С��ģ
//			SNFile = "sub-6-8-EON-ShDis.txt";//С��ģ�̾���
			//SNFile = "sub-24-43-EON-GHG.txt";//���ģ
			//SNFile = "sub-6-8-EON-GHG.txt";//С��ģ�̾���
			//SNFile ="sub-100-570-EON-GHGD1.txt";//���ģ ŷ�޹Ǹ����� ��GHG
			SNFile = "sub-28-41-EON-GHG.txt";//С��ģ�̾���

			//1247
			VNsFile = "req-10node-erl50";
			VNsFile = "req-3node\\req-3node-erl50-0";
			VNsFile = "req-10node\\req-10node-erl50-0";
			VNsFile = "reqs-erl\\reqs-erl40-10-125\\reqs-erl40-4-10-10-125-5000-1";
			VNsFile = "reqs-erl\\reqs-erl100-10-125\\reqs-erl100-10-10-10-125-5000-0";
			VNsFile = "reqs-erl\\reqs-erl200-10-125\\reqs-erl200-10-20-10-125-5000-0";
			VNsFile = "reqs-erl\\reqs-erl300-10-125\\reqs-erl300-10-30-10-125-5000-0";
			VNsFile = "reqs-erl\\reqs-erl400-10-125\\reqs-erl400-10-40-10-125-5000-0";
			VNsFile = "reqs-erl\\reqs-erl500-10-125\\reqs-erl500-10-50-10-125-5000-0";
			VNsFile = "reqs-erl\\reqs-erl500-50-125\\reqs-erl500-10-50-50-125-5000-0";
			VNsFile = "reqs-erl\\reqs-erl400-50-125\\reqs-erl400-10-40-50-125-5000-0";
			VNsFile = "reqs-erl\\reqs-erl300-50-125\\reqs-erl300-10-30-50-125-5000-0";
			VNsFile = "reqs-erl\\reqs-erl200-50-125\\reqs-erl200-10-20-50-125-5000-0";
			VNsFile = "reqs-erl\\reqs-erl100-50-125\\reqs-erl100-10-10-50-125-5000-0";
			//VNsFile = "reqs-erl\\reqs-erl40-10-125\\reqs-erl40-4-10-10-125-5000-0";
			VNsFile = "reqs-erl\\reqs-erl100-10-125\\reqs-erl100-10-10-10-125-5000-0";
			VNsFile = "reqs-erl\\reqs-erl300-10-125\\reqs-erl300-10-30-10-125-5000-0";
			//VNsFile = "reqs-erl\\reqs-erl400-10-125\\reqs-erl400-10-40-10-125-5000-0";
			VNsFile = "reqs-erl\\reqs-erl500-10-125\\reqs-erl500-10-50-10-125-5000-0";
			//VNsFile = "req-10node-erl50";
			//VNsFile = "req-3node-10\\req-3node-erl50-0";


			//VNsFile = "req-10node-erl50";
			VNsFile = "..\\reqs\\reqs-erl\\reqs-erl50-50-125-6\\0";
			//VNsFile = "reqs-erl\\reqs-erl100-50-125-6\\reqs-erl100-10-50-50-125-5000-6-0";
			//VNsFile = "reqs-t";
			//VNsFile = "req-3node\\req-3node-erl50-0";
			//VNsFile = "reqs-erl\\reqs-erl100-50-125-6\\reqs-erl100-10-50-50-125-5000-6-0";
			//VNsFile = "reqs-erl\\reqs-erl40-50-125\\reqs-erl40-4-10-50-125-5000-0";
			//VNsFile = "reqs-erl\\reqs-erl40-50-125-6\\0";
			//VNsFile = "req-3node\\req-3node-erl50-0";
			//VNsFile = "reqs-erl\\reqs-erl50-50-125-4\\0";
//			VNsFile = "..\\..\\reqs\\reqs-erl10-50-125-4\\0";
			///VNsFile = "..\\reqs\\reqs-erl\\reqs-erl10-50-125-4-staticAnalyse\\0";

			//VNsFile = "reqs-erl\\test";


			int reqsNum1;// = 10;
			int delay1 = 100;
			//MapVONEEnTranModel
			//MapVONELin_SortByNodeDegreeAndBW
			/*
			VONEFactory vf = new VONEFactory();
			VNE vone;
			//���Ż�MapVONE3PByWangYAndChenxh//MapVONE3ByWangY//MapVONE3PByWangY
			//MapVONE01ILPLin//MapVONE01ILPPRLinCXH//MapVONE3ByWangY//MapVONE01ILPPRLinCXHNode
			//����ʽMapVONELin_SortByNodeDegreeAndBW//MapVONELin_SortByBW//
			//MapVONEMIPTranAndPRankByCXH//MapVONEPageRank//MapVONEEnTranModel//MapVONETranModel
			//MapVONE01ILPLin_EquilibriumCXH//MapVONE01ILPLin//MapVONECXHNode
			double vbwPara=0.2,vcpuPara=0.2;
			Parameters.vbwPara = vbwPara;
			Parameters.vcpuPara = vcpuPara;
			Parameters.CurrentVONEMethod = Parameters.MapVONE01ILPLin_EquilibriumCXH;//MapVONE01ILPPRLinCXHNode;
			vone = vf.GetVONEMethod(Parameters.CurrentVONEMethod);
			vone.VONEEmbed(SNFile,VNsFile,reqsNum1,delay1);
			*/
			///*


				reqsNum1=500;//200;//5000;
			    int modelNum[]={2281};//2280,2281,105,161,913//MapVONEByDeepReinforceLearning
						//{11,15,1511,106,8161,915};
			// {17,18,19,105,161,913,222,1051};

					try {
						for (int nn=0;nn<modelNum.length;nn++){



						for (double vbwPara =0.1; vbwPara <=0.1; vbwPara += 0.4) {
							double vcpuPara;

							for (vcpuPara =0.1; vcpuPara <=0.1; vcpuPara++) {//0.08
								for (int ii =200; ii <=200; ii = ii + 100)//���

									//for(int ii=10;ii<=30;ii=ii+10)//С��ģ
									for (int jj =0; jj <=0; jj++) {
										Parameters.vbwPara = vbwPara;
										Parameters.vcpuPara = vcpuPara;
 										//VNsFile = "../reqs-erl/reqs-erl" + ii + "-50-125-4/" +jj;//С��ģ
										//VNsFile = "..\\reqs\\reqs-erl"+ii+"-50-125-6\\"+"reqs-erl"+ii+"-10-50-50-125-5000-6-"+jj;//���ģ
										//VNsFile = "..\\reqs\\reqs-erl"+ii+"-50-125-6-500h\\"+"reqs-erl"+ii+"-10-50-50-125-5000-6-"+jj+"-500h";//���ģ
										VNsFile = "..\\reqs\\reqs-erl\\reqs-erl"+ii+"-50-125-6\\"+"reqs-erl"+ii+"-10-50-50-125-5000-6-"+jj;//���ģ

										VONEFactory vf = new VONEFactory();
										VNE vone;//MapVONE01ILPLin//MapVONE01ILPPR

											//Parameters.CurrentVONEMethod=modelNum[nn];
											//                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                      LinCXH//./mkreq 12500 1 50 50 50 100 100 reqs/1

										Parameters.CurrentVONEMethod = modelNum[nn];//MapVONELin_SortByBW;//MapVONEPageRankOfGHGByVogel;//MapVONENCRbyILP;//MapVONE01ILPLin;//MapVONENCRbyILP;//MapVONE01ILPLin;//MapVONENCRbyILP;//MapVONE3ByWangY;//MapVONE01ILPLin_EquilibriumCXH;//MapVONENCRbyILP;//MapVONE01ILPPRLinCXH;//MapVONE01ILPLin;//MapVONE01ILPPRLinCXH;//MapVONEPageRank_equilibrium ;//MapVONECXHNode;//MapVONEILPByY_L;//MapVONE01ILPLin;//MapVONEILPByY_L;//MapVONELin_SortByBW;//MapVONE01ILPLinFB;//MapVONE01ILPLin;//MapVONE01ILPLin_EquilibriumCXH;//MapVONE01ILPLin;//FB;//MapVONE01ILPLin;//FB;//MapVONEPageRank;//MapVONEMIPTranAndPRankByCXH;//MapVONEPageRank;//MapVONELin_SortByBW;//MapVONE01ILPPRLinCXHNode;//MapVONE01ILPLin;//MapVONE01ILPLin;//MapVONELin_FB_SortByNodeDegreeAndBW;//MapVONE01ILPLin;//MapVONELin_FB_SortByNodeDegreeAndBW;//MapVONE3ByWangY;//MapVONE01ILPLin;//MapVONE3ByWangY;//MapVONELin_FB_SortByNodeDegreeAndBW;//MapVONELin_SortByBW;//MapVONEMIPTranAndPRankByCXH;//MapVONE3PByWangYAndChenxh;//MapVONE3PByWangYAndChenxh;//MapVONE3ByWangY;//MapVONELin_SortByNodeDegreeAndBW;//MapVONELin_SortByNodeDegreeAndBW;//MapVONEPageRank;//MapVONE3PByWangY;//MapVONEEnTranModel;//MapVONELin;//MapVONEPageRank;//.MapVONELin;//MapVONEEnTranModel;//MapVONETranModel;//MapVONE3PByWangY;//MapVONE3ByWangY;//MapVONETranModel;//MapVONE3PByWangY;//MapVONE3ByWangY;//.MapVONE3PByWangY;//.MapVONETranModel;
										vone = vf.GetVONEMethod(Parameters.CurrentVONEMethod);
										//for(reqsNum1=1000;reqsNum1<=5000;reqsNum1=reqsNum1+1000) {
											vone.VONEEmbed(SNFile, VNsFile, reqsNum1, delay1);
										//}
										}

								//if(vbwPara<5) vbwPara += 4;
								//else if(vbwPara<10) vbwPara += 5;
								//else if(vbwPara<30) vbwPara += 10;
								//else vbwPara += 10;
								//if (vcpuPara <= 0.4) vcpuPara += 0.1;
								//else if (vcpuPara <= 1) vcpuPara += 0.2;
							}

						}
						}
					} catch (Exception e) {
						e.printStackTrace();

					}
					//*/



			//VONEFactory vf = new VONEFactory();
			//VNE vone;
			//
			//static int MapVONELin_SortByNodeDegree = 17;//lin_algo1
			//static int MapVONELin_SortByNodeDegreeAndBW = 18;//lin_algo2
			//static int MapVONELin_SortByBW = 19;//lin_algo3
			//MapVONEEnTranModel
			//MapVONEMIPTranAndPRankByCXH
			//MapVONE3ByWangY
			//MapVONE01ILPLin
			//MapVONETranILPByChenxh
			//MapVONE3PByWangYAndChenxh
			//MapVONELin_SortByNodeDegreeAndBW
			//MapVONEPageRank
			//MapVONETranILPByChenxh
			//MapVONE01ILPPRLinCXH
			//Parameters.CurrentVONEMethod = Parameters.MapVONEEnTranModel;//MapVONELin_SortByBW;//MapVONEMIPTranAndPRankByCXH;//MapVONE3PByWangYAndChenxh;//MapVONE3PByWangYAndChenxh;//MapVONE3ByWangY;//MapVONELin_SortByNodeDegreeAndBW;//MapVONELin_SortByNodeDegreeAndBW;//MapVONEPageRank;//MapVONE3PByWangY;//MapVONEEnTranModel;//MapVONELin;//MapVONEPageRank;//.MapVONELin;//MapVONEEnTranModel;//MapVONETranModel;//MapVONE3PByWangY;//MapVONE3ByWangY;//MapVONETranModel;//MapVONE3PByWangY;//MapVONE3ByWangY;//.MapVONE3PByWangY;//.MapVONETranModel;
			//vone = vf.GetVONEMethod(Parameters.CurrentVONEMethod);
			//vone.VONEEmbed(SNFile,VNsFile,reqsNum1,delay1);
		} catch(Exception e){
			e.printStackTrace();
		}
		/*
		//String SNFile,VNsFile;
		//SNFile = "sub.txt";
		//VNsFile = "C:\\Users\\chenxh\\workspace\\RSA-EAVNE\\requests-100-0-10-10";
		//VNsFile = "requests-100-0-10-10";
		//VNsFile = "reqs-20-100";//"reqs";//"co";
		//VNsFile = "req-erl-100";
		//VNsFile = "req-erl-50";
		//VNsFile = "reqs-1000-700";
		//VNsFile = "requests-100-0-10-10";
		//VNsFile = "req-3node-2link-erl50";
		//VNsFile = "req-10node-erl50";//"req-10node\\req-10node-erl50-0";//
		//VNsFile = "req-3node\\req-3node-erl50-0";
		//VNsFile = "req-3node-10\\req-3node-erl50-0";
		//VNsFile = "req-2node-10\\req-2node-erl50-0";
		int reqsNum = 2; 
		
		EOSubstrateNetwork sub = new EOSubstrateNetwork();
		VONRequest[] reqs = new VONRequest[reqsNum];
		System.out.println("The length of reqs:"+reqs.length);
		//VNEByEOpticalNet vne = new VNEByEOpticalNet(SNFile,VNsFile);//Create elastic optical network
		//VNEByEOpticalNet vne = new VNEByEOpticalNet(SNFile,VNsFile);//Create elastic optical network
		//VONRequest reqs = VONRequest(VNsFile);
		VONEByTranModel vne = new VONEByTranModel(SNFile,VNsFile);
		try {
			
			vne.CreateSN(sub);
			System.out.println("It has already succeeded in creating the elastic optical substrate network.");
			//vne.PrintSN(sub);
			
			vne.CreateVNs(reqs,reqsNum);
			//vne.PrintVNs(reqs,reqsNum);
			System.out.println("It has already succeeded in creating the VN Requests.");
			
			vne.Init(sub, reqs, reqsNum);
			System.out.println("Init successfully.");
			
			int delay = 100;
			//vne.V2SEmbed(sub,reqs,delay,Parameters.MapLinkByMIP);
			//vne.V2SEmbed(sub,reqs,delay,Parameters.MapLinkByMIPEnh);
			//vne.V2SEmbed(sub,reqs,delay,Parameters.MapLinkByMIPTimes);
			//vne.V2SEmbed(sub,reqs,delay,Parameters.MapLinkBySPFA);
			//vne.V2SEmbed(sub,reqs,delay,Parameters.MapLinkByFA);
			//vne.V2SEmbed(sub,reqs,delay,Parameters.MapLinkBy01ILP);
			//vne.V2SEmbed(sub,reqs,delay,Parameters.MapVONEBy01ILP);
			//vne.V2SEmbed(sub,reqs,delay,Parameters.MapVONE3PByWangY);//chenxh�����
			//vne.V2SEmbed(sub,reqs,delay,Parameters.MapVONE3ByWangY);
			vne.V2SEmbed(sub,reqs,delay,Parameters.MapVONETranModel);
			//vne.CreateKShortestPath(sub, reqs, 0);
			//System.out.println("PrintfVNE.");
			//vne.PrintVNE(sub, reqs);
			//vne.SaveNodeEmbedding(reqs);
			
			JOptionPane.showMessageDialog(null, "�ɹ������Դ���䣡", "�ɹ�", JOptionPane.ERROR_MESSAGE);

		} 
		catch (Exception ex) {
			JOptionPane.showMessageDialog(null, ex.getStackTrace());
			JOptionPane.showMessageDialog(null, ex.getStackTrace(), "ʧ��", JOptionPane.ERROR_MESSAGE);

		}	*/			
    }  
}





class FindHop 
{

	static int min_num_of_edges = 0, edge_count = 0;
	int pathList[];
	
	public static void main1(String[] args)
	{
		@SuppressWarnings("resource")
		Scanner input = new Scanner(System.in);
		Graph g = new Graph(6);
		g.addEdge(0, 1, 0.1);
		g.addEdge(0, 2, 0.3);
		g.addEdge(0, 5, 0.2);
		g.addEdge(1, 2, 0.2);
		g.addEdge(1, 4, 0.1);
		g.addEdge(2, 3, 0.5);
		g.addEdge(1, 3, 0.4);
		g.addEdge(3, 4, 0.8);
		g.addEdge(3, 5, 0.7);
		g.addEdge(4, 5, 0.9);

		System.out.println("Enter the source: ");
		int u = input.nextInt();
		System.out.println("Enter the destination point: ");
		int v = input.nextInt();
		System.out.println("The different paths from "+u+ " to "+v+ " are:");
		g.printAllPaths(u, v);
		System.out.print("Minimum number of edges between " +u+" and "+v+" larger or equal to 2 is: ");
		g.minEdgeDFS(u, v);
		
		
	}

	static class Graph
	{

		// No. of vertices
		int V;

		// Pointer to an array containing
		// adjacency lists
		Vector<Integer>[] adj;

		
		// function for finding minimum number
		// of edges using DFS, src= source a and des = destination b
		private void minEdgeDFSUtil(boolean[] visited, 
									int src, int des) 
		{

			// For keeping track of visited
			// nodes in DFS
			visited[src] = true;
			
			// If we have found the destination vertex
			// then check whether count of total number of edges
			// is less than the minimum number of edges or not
			if (src == des && edge_count>=2)
			{
				if (min_num_of_edges > edge_count)
					min_num_of_edges = edge_count;
			}

			// If current vertex is not destination keep tracking 
			else
			{
				//adj = the next vertices in the neighborhood 
				for (int i : adj[src]) 
				{
					int v = i;

					if (!visited[v]) 
					{					
						edge_count++;					
						minEdgeDFSUtil(visited, v, des);
						}
					}
			}

			// Decrement the count of number of edges
			// and mark current vertex as unvisited
			visited[src] = false;
			edge_count--;
	
		}
		

		// Constructor
		@SuppressWarnings("unchecked")
		Graph(int V) {
			this.V = V;
			adj = new Vector[V];

			for (int i = 0; i < V; i++)
				adj[i] = new Vector<>();
		}

		// Function to add an edge to graph
		void addEdge(int src, int des, double dis) 
		{
			adj[src].add(des);
		}

		// Function to print minimum number of edges
		// It uses recursive minEdgeDFSUtil
		void minEdgeDFS(int u, int v)
		{

			// To keep track of all the
			// visited vertices
			boolean[] visited = new boolean[this.V];
			ArrayList<Integer> pathList = new ArrayList<>();
					

			pathList.add(u);
														
			// To store minimum number of edges
			
			//	min_num_of_edges;
			min_num_of_edges =Integer.MAX_VALUE;
			// To store total number of
			// edges in each path
			edge_count = 0;
			
			minEdgeDFSUtil(visited, u, v);
				
			// Print the minimum number of edges
				
				System.out.println( min_num_of_edges);
		}
		//Function to print all the paths from the source to destination
		 public void printAllPaths(int u, int v) 
		    { 
			 
		        boolean[] isVisited = new boolean[V]; 
		        ArrayList<Integer> pathList = new ArrayList<>(); 
		  
		        // add source to pathlist[] 
		        pathList.add(u); 
		  
		        printAllPathsUtil(u, v, isVisited, pathList); 
		    } 
		 private void printAllPathsUtil(Integer u, Integer d, 
                boolean[] isVisited, 
                List<Integer> localPathList) 
						{ 
					//If the destination is found		
				if (u.equals(d)) { 
					System.out.println(localPathList); 			
		  			return; 
						} 
								
				// Mark the current node 
					isVisited[u] = true; 
					
					// Recur for all the vertices 
					// adjacent to current vertex 
					for (Integer i : adj[u]) { 
					if (!isVisited[i]) { 
					// store current node in the path
					
						 localPathList.add(i); 
						 printAllPathsUtil(i, d, isVisited, localPathList); 
								
					// remove current node from the path
	        	  
						localPathList.remove(i); 
						} 
		     		} 
								
		// Mark the current node 
					isVisited[u] = false; 
				} 
			}


	
}


