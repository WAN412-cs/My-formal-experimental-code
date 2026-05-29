package Team.CloudStorage.EAVONE.DeepReinforceLearning;


//环境模型:包括资源状态表示、物理网络和虚拟网络请求

/*
 * 功能：资源状态类ResourceState
 * 创建者：陈晓华
 * 创建时间：2026-01-14
 */
//package com.vne.drl.environment;

import Team.CloudStorage.EAVONE.*;//.EAVONE;

import Team.CloudStorage.EAVONE.EOSubstrateNetwork;
import Team.CloudStorage.EAVONE.VONRequest;

import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.factory.Nd4j;


/**
 * 物理节点资源状态（CPU, 内存, 带宽等）
 */
public class ResourceState {
	private double[][] stateMatrix = null;//状态矩阵
    private int nodeId;
    private double cpu;      // CPU资源
    private double memory;   // 内存资源,save for the future
    private double storage;  // 存储资源,save for the future
    private double bandwidth;// 带宽资源
    private double energy;   // 能耗,save for the future
    
    public ResourceState(int nodeId, double cpu, double memory, 
                        double storage, double bandwidth) {
        this.nodeId = nodeId;
        this.cpu = cpu;
        this.memory = memory;
        this.storage = storage;
        this.bandwidth = bandwidth;
        this.energy = 0.0;
    }
    
    public ResourceState(int nodeId, double cpu) {
    	this.nodeId = nodeId;
    	this.cpu = cpu;
    	//this.energy = 0.0;
    }
    
    public ResourceState() {
    	//this.nodeId = nodeId;
    	//this.cpu = cpu;
    	//this.energy = 0.0;
    }
    /*
     * Function:得到状态矩阵
     * Create time:2026/1/24
     * Creator: Chen Xiaohua
     */
    //public double[][] getStateMatrix(EOSubstrateNetwork sub,VONRequest reqs[], int index,int vNode) {
    //    double[][] stateMatrix = new double[sub.nodes][3];

    //    for (int i = 0; i < sub.nodes; i++) {
    //        stateMatrix[i][0] = calculateCPU(sub,i,reqs,index,vNode);
    //        stateMatrix[i][1] = calculateTotalLinkBandwidth(sub, i);
    //        stateMatrix[i][2] = calculateAverageDistance(sub, i);
    //    }

    //    return stateMatrix;
    //}
    
    /*
     * extractState()
     * Create time:2026/1/26
     * Creator: Chen Xiaohua
     */
    //public double[][] extractState(EOSubstrateNetwork sub,VONRequest reqs[], int index,int vNode) {
    //	double[][] stateMatrix = new double[sub.nodes][3];
    //    for (int i = 0; i < sub.nodes; i++) {
    //        stateMatrix[i][0] = calculateCPU(sub,i,reqs,index,vNode);
    //        stateMatrix[i][1] = calculateTotalLinkBandwidth(sub, i);
    //        stateMatrix[i][2] = calculateAverageDistance(sub, i);
    //    }
    //    return stateMatrix;
    //}
    
    /*
     * extractState()
     * Create time:2026/1/26
     * Creator: Chen Xiaohua
     */
    public INDArray GetState(EOSubstrateNetwork sub,VONRequest reqs[], int index,int vNode) {     
    	// 假设有n个节点，每个节点有3个特征
        int n = sub.nodes;
        INDArray state = Nd4j.zeros(n, 3);
        for (int i = 0; i < sub.nodes; i++) {
            state.putScalar(new int[]{i, 0}, calculateCPU(sub,i,reqs,index,vNode));
            state.putScalar(new int[]{i, 1}, calculateTotalLinkBandwidth(sub, i));
            state.putScalar(new int[]{i, 2}, calculateAverageDistance(sub, i));
            //i++;
        }
        return state;
    }
    
    /*
     * Function:calculateCPU()
     * Create time:2026/1/24
     * Creator: Chen Xiaohua
     */
    public double calculateCPU(EOSubstrateNetwork sub,int sNode,VONRequest reqs[], int index,int vNode) {
    	if(sub.cpu[sNode] < reqs[index].cpu[vNode]) return 0;
    	else return sub.cpu[sNode];
    }
    
    /*
     * Function:calculateAverageDistance()
     * Create time:2026/1/24
     * Creator: Chen Xiaohua
     */
    private double calculateAverageDistance(EOSubstrateNetwork sub, int sNode) {
        int length = 0;//链路的长度之和
        int linkSum = 0;//链路的距离之和

        for (int i = 0; i < sub.links; i++) {
            if (sub.link[i].from == sNode || sub.link[i].to == sNode) {
            	length += sub.link[i].length;
            	linkSum++;
            }
        }
        return length/(linkSum + Parameters.MIN_VALUE_DOUBLE);
    }
    
    /*
     * Function:calculateTotalLinkBandwidth()
     * Create time:2026/1/24
     * Creator: Chen Xiaohua
     */
    private double calculateTotalLinkBandwidth(EOSubstrateNetwork sub, int sNode) {
        int slotSum = 0;

        for (int i = 0; i < sub.links; i++) {
            if (sub.link[i].from == sNode || sub.link[i].to == sNode) {
            	for (int j = 0; j < sub.slotsNum; j++) {
            		if(sub.slots[i][j] == 1) slotSum++;
            	}
            }
        }
        return slotSum*12.5;//简化版本，直接乘以12.5，可修改为乘以距离有关的调制模式
    }
    
    private static int countContinuousBlocks(EOSubstrateNetwork sub,int[] slots ,int linkNo) {
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

    
    // 转换为神经网络输入向量
    //public INDArray toINDArray() {
    //    return Nd4j.create(new double[]{cpu, memory, storage, bandwidth, energy}, 
    //                      new int[]{1, 5});
    //}
    
    // 检查资源是否足够
    public boolean hasEnoughResources(ResourceState required) {
    	return cpu >= required.cpu;// &&
                //bandwidth >= required.bandwidth;
        //return cpu >= required.cpu &&
          //     memory >= required.memory &&
            //   storage >= required.storage &&
              // bandwidth >= required.bandwidth;
    }
    
    // 分配资源
    public void allocate(ResourceState required) {
        this.cpu -= required.cpu;
        this.memory -= required.memory;
        this.storage -= required.storage;
        this.bandwidth -= required.bandwidth;
        // 更新能耗模型（简化）
        this.energy += 0.1 * (required.cpu + required.memory);
    }
    
    // 释放资源
    public void release(ResourceState required) {
        this.cpu += required.cpu;
        //this.memory += required.memory;
        //this.storage += required.storage;
        //this.bandwidth += required.bandwidth;
    }
    
    // Getters and Setters
    public double getCpu() { return cpu; }
    public double getMemory() { return memory; }
    public double getStorage() { return storage; }
    public double getBandwidth() { return bandwidth; }
    public double getEnergy() { return energy; }
    public int getNodeId() { return nodeId; }
}