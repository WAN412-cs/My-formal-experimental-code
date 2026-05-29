package Team.CloudStorage.EAVONE;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
//import java.util.*;

public class VirtualOpticalNetworkEmbedding extends VNE {
    
    
    /////////////////////////////////////
    //The function initiates the name of SNFile and VNsFile.
    ////////////////////////////////////
    public VirtualOpticalNetworkEmbedding(String inSNFile,String inVNsFile)
    {
    	SNFile = inSNFile;
    	VNsFileDir = inVNsFile;    
    }
    
    //////////////////////////////////////////////////
    //CreateVNs:Create the VNs from the file 
    //////////////////////////////////////////////////
    public void CreateVNs(EOSubstrateNetwork sub) throws IOException {
    	String strBuff;
		BufferedReader data = new BufferedReader(new InputStreamReader(new FileInputStream(SNFile)));
		//read a line which means a number of substrate network. 
		//For an example, 10 is the number of virtual optical network requests. 
		//fscanf(fp, "%d %d\n", &sub.nodes, &sub.links);
		strBuff = data.readLine();	//the number of virtual optical network requests
		String[] strcol = strBuff.split(" ");
		int nodesNum = Integer.valueOf(strcol[1]);
		int linksNum = Integer.valueOf(strcol[2]);
		
		sub.nodes = nodesNum;
		sub.links = linksNum;
		
		sub.cpu = new double[sub.nodes];
		sub.link = new LinkStruct[sub.links];		
		
		for(int i=0;i<nodesNum;i++){
			strBuff = data.readLine(); //Read the line of a SN information.
			sub.cpu[i] = Double.parseDouble(strBuff);
		}
		
		for(int i=0;i<linksNum;i++){
			strBuff = data.readLine(); //Read the line of a SN information.
			strcol = strBuff.split(" ");
			sub.link[i].from = Integer.parseInt(strcol[1]);
			sub.link[i].to = Integer.parseInt(strcol[2]);
			sub.link[i].bw = Double.parseDouble(strcol[3]);
		}
		
		data.close();
    }
    
    public void CreateSN() throws IOException {
    	String strBuff;
		BufferedReader data = new BufferedReader(new InputStreamReader(new FileInputStream(SNFile)));
		//read a line which means a number of virtual optical network requests. 
		//For an example, 10 is the number of virtual optical network requests. 
		strBuff = data.readLine();	//the number of virtual optical network requests
		String[] strcol = strBuff.split(" ");
		int nodesNum = Integer.valueOf(strcol[1]);
		int linksNum = Integer.valueOf(strcol[2]);
		//VONRequest[] VNRNodes = new VONRequest[Integer.valueOf(strcol[1])]; //create the VNRs whose number is the value of strBuff
		for(int i=0;i<nodesNum;i++){
			strBuff = data.readLine(); //Read the line of a VNRequest information.
		}
		data.close();
		
		nodesNum = nodesNum+1;
		linksNum = linksNum++;
		return ;
    }
    
    //名称：GenerateVN()
    //功能：生成虚拟网络请求
    public void GenerateVN(String VNsDirectory)
    {
    	
    }
}
