package Team.CloudStorage.EAVONE;
import java.util.*; 
import java.lang.*; 
import java.io.*; 

public class ShortestPath {
	static final int V = 7; 
    int minDistance(int dist[], Boolean sptSet[]) 
    { 
        // Initialize min value 
        int min = Integer.MAX_VALUE, min_index = -1; 
  
        for (int v = 0; v < V; v++) 
            if (sptSet[v] == false && dist[v] <= min) { 
                min = dist[v]; 
                min_index = v; 
            } 
  
        return min_index; 
    } 
  
    // A utility function to print the constructed distance array 
    void printSolution(int dist[]) 
    { 
        System.out.println("Vertex \t\t Distance from Source"); 
        for (int i = 0; i < V; i++) 
            System.out.println(i + " \t\t " + dist[i]); 
    } 
  
  
   void dijkstra(int graph[][], int src) 
    { 
        int dist[] = new int[V];  
       
  
        
        Boolean sptSet[] = new Boolean[V]; 
  
        
        for (int i = 0; i < V; i++) { 
            dist[i] = Integer.MAX_VALUE; 
            sptSet[i] = false; 
        } 
  
        // Distance of source vertex from itself is 0 
        dist[src] = 0; 
  
        
        for (int count = 0; count < V - 1; count++) { 
            
            int u = minDistance(dist, sptSet); 
  
            
            sptSet[u] = true; 
  
          
            for (int v = 0; v < V; v++) 
  
                
                if (!sptSet[v] && graph[u][v] != 0 && dist[u] != Integer.MAX_VALUE && dist[u] + graph[u][v] < dist[v]) 
                    dist[v] = dist[u] + graph[u][v]; 
        } 
  
        // print the constructed distance array 
        printSolution(dist); 
    } 
  
    
    public static void main1(String[] args) 
    { 
        /* Let us create the example graph discussed above */
        /*int graph[][] = new int[][] { { 0, 1, 2, 2, 0, 0, 0 }, 
                                      { 0, 1, 2, 3, 4, 5, 6 }, 
                                      { 0, 4, 7, 4, 0, 0, 0 }, 
                                      { 1, 4, 6, 0, 0, 0, 0 }, 
                                      { 2, 0, 0, 9, 0, 10, 0 }, 
                                      { 0, 0, 4, 14, 10, 0, 2 }, 
                                      { 0, 0, 0, 0, 0, 2, 0 }}; */

       int graph[][] = new int[][] { { -1, -1, -1, -1, 0, 0, 0 }, 
                                          { 0, 1, 2, 3, 4, 5, 6 }, 
                                          { 0, 4, 7, 4, 0, 0, 0 }, 
                                          { 1, 4, 6, 0, 0, 0, 0 }, 
                                          { 2, 0, 0, 9, 0, 10, 0 }, 
                                          { 0, 0, 4, 14, 10, 0, 2 }, 
                                          { 0, 0, 0, 0, 0, 2, 0 }}; 
        ShortestPath t = new ShortestPath(); 
        //t.dijkstra(graph, 0,1,2); 
    } 
}
