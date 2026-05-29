package Team.CloudStorage.EAVONE;
//import java.util.*;
import java.io.FileInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class VONRequests {
	//Map<String,VONRequest> VNs = new HashMap<String,VONRequest>();  
	  
	//Student s1 = new Student("张三");  
	//Student s2 = new Student("李四");  
	//studentMap.put(s1.name,s1);  
	//studentMap.put(s2.name,s2);  
	
	public VONRequests(int inReqsNum,String fileName){
		reqsNum = inReqsNum;
		reqs = new VONRequest[reqsNum];
		try {
			CreateOneVN(fileName);
		}
		catch(Exception e) {
			System.out.println("The exception messages：" + e.getMessage());
		}
	}
	int reqsNum;
	int nodesNum,linksNum;
	//String fileName;
	VONRequest[] VNRNodes;
	VONRequest[] reqs;
	
	private void CreateOneVN(String fileName) throws IOException {
		String strBuff;
		BufferedReader data = new BufferedReader(new InputStreamReader(new FileInputStream(fileName)));
		//read a line which means a number of virtual optical network requests. 
		//For an example, 10 is the number of virtual optical network requests. 
		strBuff = data.readLine();	//the number of virtual optical network requests
		String[] strcol = strBuff.split(" ");
		nodesNum = Integer.valueOf(strcol[1]);
		linksNum = Integer.valueOf(strcol[2]);
		VNRNodes = new VONRequest[Integer.valueOf(strcol[1])]; //create the VNRs whose number is the value of strBuff
		for(int i=0;i<nodesNum;i++){
			strBuff = data.readLine(); //Read the line of a VNRequest information.
		}
		data.close();
		return ;
	}
}


