package Team.CloudStorage.EAVONE;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
//import java.io.BufferedReader;
//import java.io.FileInputStream;
//import java.io.IOException;
//import java.io.InputStreamReader;
import java.util.*;

public class Tools {
	
	/******************************************************************
	名称：Sort(......)
	功能：排序
	参数：desOrInc：1：降序；2：升序
	返回值：
	创建时间：2019.6.24
	创建人：chen xiaohua
	******************************************************************/
	public void Sort(int desOrInc,int[] arr)
	{
		boolean sort = false;
		int t = -1;
		for(int i=0;i<arr.length-1;i++){
			sort = false;
			for(int j=0;j<arr.length-1-i;j++){
				if(desOrInc == 1){//降序
					if(arr[i] < arr[i+1]) {
						t = arr[i];
						arr[i] = arr[i+1];
						arr[i+1] = t;
						sort = true;
					}
				} else if(desOrInc == 2){//升序
					if(arr[i] > arr[i+1]) {
						t = arr[i];
						arr[i] = arr[i+1];
						arr[i+1] = t;
						sort = true;
					}
				}
			}
			if(!sort) break;
		}
	}
	
	/******************************************************************
	名称：Sort(......)
	功能：排序
	参数：desOrInc：1：降序；2：升序
	返回值：
	创建时间：2019.6.24
	创建人：chen xiaohua
	******************************************************************/
	public void Sort(int desOrInc,int[] arr,int[] arrIndex)
	{
		boolean sort = false;
		int t = -1;
		for(int i=0;i<arr.length;i++){
			arrIndex[i] = i;
		}
		for(int i=0;i<arr.length-1;i++){
			sort = false;
			for(int j=0;j<arr.length-1-i;j++){
				if(desOrInc == 1){//降序
					if(arr[i] < arr[i+1]) {
						t = arr[i];
						arr[i] = arr[i+1];
						arr[i+1] = t;

						t = arrIndex[i];
						arrIndex[i] = arrIndex[i+1];
						arrIndex[i+1] = t;
						
						sort = true;
					}
				} else if(desOrInc == 2){//升序
					if(arr[i] > arr[i+1]) {
						t = arr[i];
						arr[i] = arr[i+1];
						arr[i+1] = t;
						
						t = arrIndex[i];
						arrIndex[i] = arrIndex[i+1];
						arrIndex[i+1] = t;
						
						sort = true;
					}
				}
			}
			if(!sort) break;
		}
	}
	
	public void CreateDiffRandom(int start,int end,ArrayList<Object> list){
		//ArrayList list = new ArrayList();                 
        int n = end-start;  
        Random rand = new Random();  
        boolean[] bool = new boolean[n];            
        int num =0;  
          
        for (int i = 0; i<n; i++){        
            do{  
                //如果产生的数相同继续循环  
                num = rand.nextInt(n);                     
            }while(bool[num]);               
            bool[num] =true;                
            list.add(num);            
        }  
	}
	
	//得到随机数GetRandom
	//返回值的范围为：[min,max)
	public static int GetRandom(int min, int max)
	{
		Random random = new Random();
		int s = random.nextInt(max) % (max - min + 1) + min;
		return s;
		//return String.valueOf(s);
	}
	
	//返回范围为[min,max)的n个随机数的数组
	public void CreateDiffRandomRetArr(int n,int min,int max,int[] randomArr)
	{
		//ArrayList list = new ArrayList();                 
        //int n = end-start;  
        //Random rand = new Random();  
        //boolean[] bool = new boolean[n];  
		if(n > (max-min)) {
			System.out.println("CreateDiffRandomRetArr is error.*******************");
			return ;
		}
        int num =0;  
        boolean find = false;
        int j = 0;
        
        for (int i = 0; i < n; ){        
        	//如果产生的数相同继续循环
        	num = GetRandom(min,max);
        	find = false;
        	for(j = 0; j < i; j++){
        		if(num == randomArr[j]) {
        			find = true;        			
        			break;
        		}
        	}
        	if(!find || j==i){
        		randomArr[i] = num;
        		System.out.print(num+" ");
        		i++;
        	}  
        }  
        System.out.println("");
	}
	
	//名称：SaveFile
	//功能：保存数据
	//参数：1）strName：文件名
	//      2）content：内容
	//      3)append:是否已追加的方式保持数据,false:新增方式；true：追加方式
	public void SaveFile(String fileName,String data,boolean append)
	{
		try{
			//String data = "sadfa ";
		    //File file =new File("glpsolRSA.dat");
		    File file =new File(fileName);
		    //if file doesnt exists, then create it
		    if(!file.exists()){
		       file.createNewFile();
		    }
            //true = append file
		    FileWriter fileWritter = new FileWriter(file.getName(),append);
		    BufferedWriter bufferWritter = new BufferedWriter(fileWritter);
		    
		    bufferWritter.write(data);
		    bufferWritter.flush();
		    bufferWritter.close();
		    //fileWritter.flush();
		    fileWritter.close();
		    //System.out.println("Done");
		}catch(IOException e){
			e.printStackTrace();
		}
	}
	
	 public void readFileByLines(String fileName,String data,int lineNum) {
		 BufferedReader reader = null;
		 try {
	            System.out.println("以行为单位读取文件内容，一次读一整行：");
	            reader = new BufferedReader(new FileReader(fileName));
	            String tempString = null;
	            int line = 1;
	            // 一次读入一行，直到读入null为文件结束
	            while ((tempString = reader.readLine()) != null) {
	                // 显示行号
	                System.out.println("line " + line + ": " + tempString);
	                if(line == lineNum) break;
	                line++;
	            }
	            reader.close();
	            data = tempString; 
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
	        
	 }
}
