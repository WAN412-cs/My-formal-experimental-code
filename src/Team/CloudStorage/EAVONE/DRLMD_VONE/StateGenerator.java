package Team.CloudStorage.EAVONE.DRLMD_VONE;

import Team.CloudStorage.EAVONE.EOSubstrateNetwork;
import org.junit.jupiter.api.Test;


/**
 * 状态生成器，用于生成DRL代理所需的状态矩阵
 */


public class StateGenerator {
    // 添加缓存机制
    private double[][] cachedStateMatrix = null;
    private long lastUpdateTime = 0;
    private static final long CACHE_TIMEOUT = 1000; // 1秒缓存

    /**
     * 生成状态矩阵
     * 状态包含三个维度：
     * 1. 节点剩余存储 R_C
     * 2. 关联链路总剩余带宽 TR_B
     * 3. 节点间距离 Dis
     */
    public double[][] getStateMatrix(EOSubstrateNetwork sub) {
        long currentTime = System.currentTimeMillis();
        if (cachedStateMatrix != null && (currentTime - lastUpdateTime) < CACHE_TIMEOUT) {
            return cachedStateMatrix;
        }

        int nodeCount = sub.nodes;
        double[][] stateMatrix = new double[nodeCount][3];

        for (int i = 0; i < nodeCount; i++) {
            // 1. 节点剩余存储 R_S (这里用CPU资源模拟)
            stateMatrix[i][0] = sub.cpu[i];

            // 3. 关联链路总剩余带宽 TR_B
            stateMatrix[i][1] = calculateTotalLinkBandwidth(sub, i);

            // 4. 节点间距离 Dis (这里简化为节点索引差的绝对值)
            stateMatrix[i][2] = calculateAverageDistance(sub, i);
        }

        // 更新缓存
        cachedStateMatrix = stateMatrix;
        lastUpdateTime = currentTime;
        return stateMatrix;

    }

    /**
     * 计算节点关联链路的总剩余带宽
     */
    private double calculateTotalLinkBandwidth(EOSubstrateNetwork sub, int nodeIndex) {
        double totalBandwidth = 0.0;
        int linkCount = 0;

        for (int i = 0; i < sub.links; i++) {
            // 检查链路是否与该节点相连
            if (sub.link[i].from == nodeIndex || sub.link[i].to == nodeIndex) {
                // 直接使用链路的总带宽属性，而不是计算频谱槽
                totalBandwidth += sub.link[i].bw; // 假设link对象有bandwidth属性
                linkCount++;
            }
        }

        return totalBandwidth;
    }

    /**
     * 计算节点的加权物理距离（基于跳数权重）
     * 权重因子为 1/(1+跳数)，跳数越少权重越大
     */
    private double calculateAverageDistance(EOSubstrateNetwork sub, int nodeIndex) {
        double weightedDistanceSum = 0.0;  // 分子：加权距离和
        double weightSum = 0.0;            // 分母：权重和

        for (int j = 0; j < sub.nodes; j++) {
            if (j != nodeIndex) {
                // 计算从nodeIndex到j的最短路径跳数
                int hops = calculateShortestPathHops(sub, nodeIndex, j);

                // 计算物理距离的平方（这里简化为一维距离的平方）
                double physicalDistanceSquared = Math.pow(Math.abs(nodeIndex - j), 2);

                // 计算权重：1/(1+跳数)
                double weight = 1.0 / (1.0 + hops);

                // 累加加权距离和权重
                weightedDistanceSum += weight * physicalDistanceSquared;
                weightSum += weight;
            }
        }

        // 返回加权平均距离
        return weightSum > 0 ? weightedDistanceSum / weightSum : 0.0;
    }

    /**
     * 计算两个节点之间的最短路径跳数
     * 这里需要根据实际的网络拓扑结构实现
     */
    private int calculateShortestPathHops(EOSubstrateNetwork sub, int fromNode, int toNode) {
        // 简单实现：如果两节点直接相连，则跳数为1，否则为2
        // 实际应用中应该使用BFS等算法计算最短路径
        if (fromNode == toNode) {
            return 0;
        }

        // 检查是否直接相连
        for (int i = 0; i < sub.links; i++) {
            if ((sub.link[i].from == fromNode && sub.link[i].to == toNode) ||
                    (sub.link[i].from == toNode && sub.link[i].to == fromNode)) {
                return 1;
            }
        }

        // 如果不直接相连，假设最多2跳（简单网络模型）
        return 2;
    }
}