package Team.CloudStorage.EAVONE.DeepReinforceLearning;

import Team.CloudStorage.EAVONE.*;
import org.deeplearning4j.nn.api.OptimizationAlgorithm;
import org.deeplearning4j.nn.conf.ConvolutionMode;
import org.deeplearning4j.nn.conf.MultiLayerConfiguration;
import org.deeplearning4j.nn.conf.NeuralNetConfiguration;
import org.deeplearning4j.nn.conf.inputs.InputType;
import org.deeplearning4j.nn.conf.layers.DenseLayer;
import org.deeplearning4j.nn.conf.layers.GlobalPoolingLayer;
import org.deeplearning4j.nn.conf.layers.OutputLayer;
import org.deeplearning4j.nn.conf.layers.PoolingType;
import org.deeplearning4j.nn.gradient.Gradient;
import org.deeplearning4j.nn.conf.layers.ConvolutionLayer;
//import org.deeplearning4j.nn.layers.convolution.ConvolutionLayer;
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;
import org.deeplearning4j.nn.weights.WeightInit;
import org.deeplearning4j.nn.workspace.LayerWorkspaceMgr;
import org.deeplearning4j.util.ModelSerializer;
import org.nd4j.linalg.activations.Activation;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.api.ops.impl.transforms.strict.Exp;
import org.nd4j.linalg.factory.Nd4j;
import org.nd4j.linalg.learning.config.Adam;
import org.nd4j.linalg.lossfunctions.LossFunctions;
import org.nd4j.linalg.ops.transforms.Transforms;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import org.nd4j.linalg.api.memory.MemoryWorkspace;
import org.nd4j.linalg.factory.Nd4j;


import org.deeplearning4j.nn.conf.*;
import org.deeplearning4j.nn.conf.layers.*;

public class DeepReinforceLearningAgent {
	private MultiLayerNetwork network;
	private Random random = new Random();

/*2026.5.18 21:42新增代码的测试模型,在16号模型代码的基础上测试，减少了训练时间会降低效果,继续优化修改
    // 2026.5.14重大新增：用于追踪全局环境平均表现的滑动基线与计数器==============================
    private double movingBaseline = 0.0;
    private long baselineCount = 0;

 */
	
	public DeepReinforceLearningAgent(int nNodes, int nFeatures) {
        int height = 1;  // 节点数量作为"高度"
        int width = nNodes;//nFeatures; // 特征数量作为"宽度"
        int nChannels =  nFeatures; // 特征数量作为"宽度"// 1;单通道，可视为灰度图
        
        MultiLayerConfiguration conf = new NeuralNetConfiguration.Builder()
                .seed(12345)
                .optimizationAlgo(OptimizationAlgorithm.STOCHASTIC_GRADIENT_DESCENT)
                .updater(new Adam(0.001))
                .list()
                
                // ===== 第1层：提取层（卷积层1） =====
                // 使用卷积提取局部特征（节点与其邻居的关系）
                .layer(new ConvolutionLayer.Builder(3, 3) // 3x3卷积核
                        .name("extraction_layer")
                        .nIn(nChannels)
                        .nOut(16)  // 16个滤波器
                        .stride(1, 1)
                        .padding(1, 1) // 保持输出尺寸
                        .weightInit(WeightInit.XAVIER)
                        .activation(Activation.RELU)
                        .build())
                
                // ===== 第2层：卷积层（进一步特征提取） =====
                .layer(new ConvolutionLayer.Builder(3, 3)
                        .name("convolution_layer")
                        .nOut(16)//32
                        .stride(1, 1)
                        .padding(1, 1)
                        .activation(Activation.RELU)
                        .build())
                //.layer(new DenseLayer.Builder()
                //        .nOut(512)
                //        .activation(Activation.RELU)
                //        .build())
                //.layer(new org.deeplearning4j.nn.conf.layers.GlobalPoolingLayer.Builder()
                //        .poolingType(org.deeplearning4j.nn.conf.layers.PoolingType.AVG) // ��Ϊƽ���ػ�����ƽ��
                //        .build())
                // ===== 展平 =====
                //=========================5.12的重大修改====================
                //.layer(new GlobalPoolingLayer.Builder(PoolingType.AVG).name("global_pooling").build())//===========================
                
                // ===== 第3层：概率层（全连接 + Softmax） =====
                .layer(new DenseLayer.Builder()
                        .name("probability_layer")
                        .nOut(32)//
                        .activation(Activation.RELU)
                        .build())
                
                // ===== 输出层：节点选择概率 =====
                .layer(new OutputLayer.Builder(LossFunctions.LossFunction.NEGATIVELOGLIKELIHOOD)
                        .name("output_layer")
                        .nOut(nNodes) // 输出每个节点的选择概率
                        .activation(Activation.SOFTMAX)
                        .build())
                
                // 输入格式设置
                .setInputType(InputType.convolutional(height, width, nChannels))
                .build();

        network = new MultiLayerNetwork(conf);
        network.init();
    }
	
	/*
	 * Function：导入模型
	 * Createor:Chen Xiaohua
	 * Create time: 2026/1/23
	 */
	public void loadModel(String path) throws IOException {
        File f = new File(path);
        if (f.exists()) {
        	network = ModelSerializer.restoreMultiLayerNetwork(f);
        	System.out.println("模型加载成功！");
            System.out.println("模型信息:");
            System.out.println("  - 层数: " + network.getnLayers());
            System.out.println("  - 参数数量: " + network.numParams());
        } else {
        	saveModel(path);//throw new IOException("Error: " + path);
        	//network = ModelSerializer.restoreMultiLayerNetwork(f);
        }
    }
	
	
	/**
     * 保存模型到文件 MultiLayerNetwork model,
     */
    public void saveModel(String path) {
        try {
            File modelFile = new File(path);
            
            // 确保父目录存在
            if (modelFile.getParentFile() != null) {
                modelFile.getParentFile().mkdirs();
            }
            
            ModelSerializer.writeModel(network, modelFile, true);
            System.out.println("模型已保存到: " + modelFile.getAbsolutePath());
            System.out.println("文件大小: " + modelFile.length() + " 字节");
        } catch (IOException e) {
            System.err.println("保存模型失败: " + e.getMessage());
            // 不抛出异常，允许程序继续运行
        }
    }
	
	/**
     * 重塑状态矩阵为CNN输入格式
     * 原始形状: [nNodes,nFeatures] [nNodes, nFeatures]
     * CNN形状: [batchSize, channels, height, width] = [1, 1, nNodes, nFeatures]
     */
    private INDArray reshapeForCNN(INDArray state) {
        // state形状: [nNodes, nFeatures] 
    	//(添加批次维度,1:批次,nFeatures=state.size(1):通道,1:heigh,state.size(0)=nNodes宽度)
        INDArray reshaped = state.reshape(1, state.size(1), 1, state.size(0));
        return reshaped;
    }

    // ==========================================
    // 新增：用于测试阶段推理的计数器，防止高频调用GC
    // ==========================================
    private long inferenceCount = 0;

    /**2026.5.16 16:20新增代码的测试模型,在12号模型代码的基础上测试，减少训练时间和测试时间
     * 前向传播，返回节点选择概率 (极速修复版)
     */
    public float[] forward2(INDArray state) {
        try {
            // 1. 张量重塑与网络推理
            INDArray cnnInput = reshapeForCNN(state);
            INDArray output = network.output(cnnInput);

            // 2. 将结果转换为 float 数组并带出，脱离 ND4J 的沉重绑定
            float[] result = output.toFloatVector();

            // ==========================================
            // 【核心修复】：测试阶段的内存垃圾回收兜底机制！
            // ==========================================
            if (!Parameters.TrainOrTest) { // 如果是测试阶段 (不执行 update)
                inferenceCount++;
                // 每推理 32 次，手动清空一次底层 C++ 堆外内存碎片
                if (inferenceCount % 32 == 0) {
                    Nd4j.getMemoryManager().invokeGc();
                }
            }

            return result;

        } catch (Exception e) {
            System.out.println(" 前向传播异常: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }


/**2026.5.16修改，减少训练时间
 * 前向传播，返回节点选择概率
 */
public float[] forward1(INDArray state) {
    try {
        // 直接调用，不加外部 Workspace。DL4J 内部会自动管理前向传播的内存
        INDArray output = network.output(reshapeForCNN(state));

        // 将结果转换为 float 数组并带出，脱离 ND4J 的沉重绑定，速度极快
        return output.toFloatVector();
    } catch (Exception e) {
        System.out.println("❌ 前向传播异常: " + e.getMessage());
        e.printStackTrace();
        return null;
    }
}


    /**
     * 前向传播，返回节点选择概率
     */
    public INDArray forward(INDArray state) {
    	//System.out.println("=== forward() 函数调试 ===");
        
        // 1. 检查state
        if (state == null) {
            System.out.println("❌ ERROR: state为null");
            return null;
        }
        
        //System.out.println("state形状: " + state.shapeInfoToString());
        //System.out.println("state长度: " + state.length());
        //System.out.println("state是否为空: " + state.isEmpty());
        
        // 检查数据内容,2026.5.16修改注释：===============================
        //if (!state.isEmpty()) {
        //   System.out.println("state数据: " + Arrays.toString(state.data().asFloat()));
            //System.out.println("state是否有NaN: " + hasNaN(state));
        //}
        
        // 2. 检查网络
        if (network == null) {
            System.out.println("❌ ERROR: network为null");
            return null;
        }
        
        //System.out.println("网络层数: " + network.getnLayers());
        
        // 检查网络输入层配置
        //for (int i = 0; i < network.getnLayers(); i++) {
        //    System.out.println("第 " + i + " 层:");
        //    System.out.println("  类型: " + network.getLayer(i).type());
        //    System.out.println("  输入大小: " + network.layerInputSize(i));
        //    System.out.println("  输出大小: " + network.layerSize(i));
        //    System.out.println("  参数数量: " + network.getLayer(i).numParams());
        //}
        //long expectedInputSize = network.getLayer(0).conf().getLayer().getNIn();

        //org.deeplearning4j.nn.conf.layers.Layer firstLayerConfig = 
        //	    network.getLayer(0).conf().getLayer();
        //long expectedInputSize = firstLayerConfig.getNIn();
        	
        //Layer firstLayer = network.getLayer(0);//.getLayer(0);
        //long expectedInputSize = firstLayer.conf().getLayer().getNIn();
        //System.out.println("网络期望输入维度: " + expectedInputSize);
        long expectedInputSize = network.layerInputSize(0);
        //System.out.println("网络期望输入维度: " + expectedInputSize);
        //System.out.println("实际输入维度: " + reshapeForCNN(state).size(1));
        //System.out.println("实际输入维度: " + state.size(1));
        
        // 3. 尝试执行前向传播
        try {
            //System.out.println("执行 network.output(state)...");
            INDArray output = network.output(reshapeForCNN(state));
            
            if (output == null) {
                System.out.println("❌ ERROR: network.output()返回null");
                return null;
            }
            
            //System.out.println("✅ 输出形状: " + output.shapeInfoToString());
            //System.out.println("✅ 输出数据: " + output);2026.5.16修改注释：==============================================
            //System.out.println("✅ 输出和: " + output.sumNumber());
            
            //INDArray cnnInput = reshapeForCNN(state);
            //return network.output(cnnInput);
            
            return output;
            
        } catch (Exception e) {
            System.out.println("❌ 前向传播异常: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }
/*2026.5.16新增，减少训练时间
 * 选择动作
 */
    public int selectAction2(float[] probabilities, double epsilon, EOSubstrateNetwork sub, VONRequest reqs[], int index, int[] sNodeEmbed, int vNode, int[] vLinkEmbed, int[] vNodeEmbed) {
        List<Integer> validNodes = new ArrayList<>();
        for (int i = 0; i < sNodeEmbed.length; i++) {
            if (sNodeEmbed[i] == -1 && sub.cpu[i] >= reqs[index].cpu[vNode]) {
                validNodes.add(i);
            }
        }

        if (validNodes.isEmpty()) {
            return -1;
        }

        if (Parameters.TrainOrTest && random.nextDouble() < epsilon) {
            return validNodes.get(random.nextInt(validNodes.size()));
        }

        int bestNode = -1;
        float bestScore = -Float.MAX_VALUE;

        for (int node : validNodes) {
            // 【修改点】：直接从 float 数组中通过下标取值，极其快速
            float baseProb = probabilities[node];
            float bonus = 0.0f;

            for (int i = 0; i < reqs[index].links; i++) {
                int fromV = reqs[index].link[i].from;
                int toV = reqs[index].link[i].to;

                if (fromV == vNode && vNodeEmbed[toV] > -1) {
                    bonus += 0.5f;// 【2026.5.20修改】：降低人工干预的权重，从 0.5f 降到 0.15f
                    //bonus += 0.15f;
                } else if (toV == vNode && vNodeEmbed[fromV] > -1) {
                    bonus += 0.5f;// 【2026.5.20修改】：降低人工干预的权重，从 0.5f 降到 0.15f
                    //bonus += 0.15f;
                }
            }

            float finalScore = baseProb + bonus + (float)(sub.cpu[node] * 0.001);
            //float finalScore = (baseProb * 2.5f) + bonus + (float)(sub.cpu[node] * 0.001);// 【2026.5.20修改】：放大神经网络的策略权重(baseProb * 2.5f)

            if (finalScore > bestScore) {
                bestScore = finalScore;
                bestNode = node;
            }
        }
        return (bestNode != -1) ? bestNode : validNodes.get(0);
    }


/**
     * 选择动作（节点） - 带探索的ε-greedy策略-增强版
     * Name: selectActionEnhanced
     * Create time: 2026/5/12
     */
    public int selectAction1(INDArray probabilities, double epsilon, EOSubstrateNetwork sub, VONRequest reqs[], int index, int[] sNodeEmbed, int vNode, int[] vLinkEmbed, int[] vNodeEmbed) {
        // 首先获取当前全部有效且未在此次请求中被占用的物理节点
        List<Integer> validNodes = new ArrayList<>();
        for (int i = 0; i < sNodeEmbed.length; i++) {
            if (sNodeEmbed[i] == -1 && sub.cpu[i] >= reqs[index].cpu[vNode]) {
                validNodes.add(i);
            }
        }

        if (validNodes.isEmpty()) {
            return -1;
        }

        // 训练阶段支持 epsilon 探索机制
        if (Parameters.TrainOrTest && random.nextDouble() < epsilon) {
            return validNodes.get(random.nextInt(validNodes.size()));
        }

        // 核心优化阶段（测试与训练利用分支共用最佳决策逻辑）
        int bestNode = -1;
        float bestScore = -Float.MAX_VALUE;

        for (int node : validNodes) {
            // 获取神经网络输出的基础概率作为底分
            float baseProb = probabilities.getFloat(0, node);
            float bonus = 0.0f;

            // 完美修复邻居拓扑亲和度检查逻辑
            for (int i = 0; i < reqs[index].links; i++) {
                int fromV = reqs[index].link[i].from;
                int toV = reqs[index].link[i].to;

                // 场景A：当前部署节点是链路起点，且终点已被映射
                if (fromV == vNode && vNodeEmbed[toV] > -1) {
                    int mappedSubNode = vNodeEmbed[toV];
                    // 检查物理底层两节点之间是否有直连光纤或较近拓扑，给予大幅加分引导
                    bonus += 0.5f;
                }
                // 场景B：当前部署节点是链路终点，且起点已被映射
                else if (toV == vNode && vNodeEmbed[fromV] > -1) {
                    int mappedSubNode = vNodeEmbed[fromV];
                    bonus += 0.5f;
                }
            }

            // 融合物理算力充裕度与神经网络策略的综合评分机制
            float finalScore = baseProb + bonus + (float)(sub.cpu[node] * 0.001);

            if (finalScore > bestScore) {
                bestScore = finalScore;
                bestNode = node;
            }
        }

        // 保底防护：若评分未命中则默认返回首个合规机房
        return (bestNode != -1) ? bestNode : validNodes.get(0);
    }
    
    /**
     * 选择动作（节点） - 带探索的ε-greedy策略-消融实验
     * Name: selectActionAblation
     * Create time: 2026/1/29
     * Creator: Chen Xiaohua
     */
    public int selectActionAblation(INDArray probabilities, double epsilon, EOSubstrateNetwork sub,VONRequest reqs[],int index, int[] sNodeEmbed, int vNode,int[] vLinkEmbed,int[] vNodeEmbed) {
// ε-greedy策略，只选择sNodeEmbed[] != -1的节点
        
        // 首先，获取所有有效的候选节点（sNodeEmbed != -1的节点）
        List<Integer> validNodes = new ArrayList<>();
        for (int i = 0; i < sNodeEmbed.length; i++) {
            if (sNodeEmbed[i] == -1 && sub.cpu[i] >= reqs[index].cpu[vNode]) {
                validNodes.add(i);
            }
        }
        
        // 如果没有有效节点，返回-1表示有效选择
        if (validNodes.isEmpty()) {
            return -1;
        }
        if(Parameters.TrainOrTest) {//train
        	if (random.nextDouble() < epsilon) {
                // 探索：从有效节点中随机选择
                int randomIndex = random.nextInt(validNodes.size());
                return validNodes.get(randomIndex);
        	} else {
                // 利用：从有效节点中选择概率最高的节点
                int bestNode = -1;
                float bestProb = -Float.MAX_VALUE;
                
                // 遍历所有有效节点，且有链路的节点，找到概率最高的
                for (int node : validNodes) {
                	float prob = probabilities.getFloat(0, node);
                	for(int i=0;i<reqs[index].links;i++) {
                		if(vNodeEmbed[reqs[index].link[i].from] > -1 && reqs[index].link[i].to == node ) {
                			if (prob > bestProb) {
                                bestProb = prob;
                                bestNode = node;
                            }
                		} else if(vNodeEmbed[reqs[index].link[i].to] > -1 && reqs[index].link[i].from == node) {
                			if (prob > bestProb) {
                                bestProb = prob;
                                bestNode = node;
                            }
                		}
                	}  
                }
                if(bestNode > -1) return bestNode;
                
                // 遍历所有有效节点，找到概率最高的
                for (int node : validNodes) {
                    float prob = probabilities.getFloat(0, node);
                    if (prob > bestProb) {
                        bestProb = prob;
                        bestNode = node;
                    }
                }
                
                // 如果没找到有效节点，则随机选择一个有效节点
                if (bestNode == -1) {
                    int randomIndex = random.nextInt(validNodes.size());
                    return validNodes.get(randomIndex);
                }
                
                return bestNode;
            }
        } else {
        	// 利用：从有效节点中选择概率最高的节点
            int bestNode = -1;
            float bestProb = -Float.MAX_VALUE;
            
            // 遍历所有有效节点，且有链路的节点，找到概率最高的
            for (int node : validNodes) {
            	float prob = (float)sub.cpu[node];//probabilities.getFloat(0, node);
            	for(int i=0;i<reqs[index].links;i++) {
            		if(vNodeEmbed[reqs[index].link[i].from] > -1 && vNodeEmbed[reqs[index].link[i].to] == node ) {
            			if (prob > bestProb) {
                            bestProb = prob;
                            bestNode = node;
                        }
            		} else if(vNodeEmbed[reqs[index].link[i].to] > -1 && vNodeEmbed[reqs[index].link[i].from] == node) {
            			if (prob > bestProb) {
                            bestProb = prob;
                            bestNode = node;
                        }
            		}
            	}  
            }
            if(bestNode > -1) return bestNode;
            
            // 遍历所有有效节点，找到概率最高的
            for (int node : validNodes) {
                float prob = (float)sub.cpu[node];//probabilities.getFloat(0, node);
                if (prob > bestProb) {
                    bestProb = prob;
                    bestNode = node;
                }
            }
            return bestNode;
        }
    }
    
    /**
     * 计算温度参数的softmax（用于鼓励探索）
     */
    public INDArray temperatureSoftmax(INDArray logits, double temperature) {
        INDArray scaledLogits = logits.div(temperature);
        //INDArray expLogits = Nd4j.exp(scaledLogits);
        INDArray expLogits = Nd4j.getExecutioner().exec(new Exp(scaledLogits));
        return expLogits.div(expLogits.sumNumber());
    }
    
    /**
     * 训练更新网络（使用REINFORCE策略梯度）
     */
    public void update111(INDArray state, int action, double reward, double gamma, double learningRate) {
        // 前向传播获取概率
        INDArray cnnInput = reshapeForCNN(state);
        INDArray output = network.output(cnnInput);
        
        // 计算动作的概率
        double actionProb = output.getDouble(0, action);
        
        // 计算策略梯度（简化REINFORCE）
        INDArray gradient = Nd4j.zerosLike(output);
        gradient.putScalar(new int[]{0, action}, -reward * gamma / actionProb);
        
        // 更新网络参数
        network.setInput(cnnInput);
        network.setLabels(gradient); // 使用梯度作为标签（简化）
        network.fit(cnnInput, gradient);
    }

    // ==========================================
    //2026.5.14重大新增,减少深度学习训练时间======================
    // 新增：批量经验回放池与基线全局变量
    // ==========================================
    private List<INDArray> stateBuffer = new ArrayList<>();
    private List<Integer> actionBuffer = new ArrayList<>();
    private List<Double> rewardBuffer = new ArrayList<>();
    private int batchSize = 32; // 提速核心：矩阵打包大小（可根据需要调大至 64）2026.5.18修改
    //private int batchSize = 8; // ,2026.5.19修改为8，2026.5.18修改为16，减少训练时间以后结果不好，继续优化
    private double movingBaseline = 0.0;
    private long baselineCount = 0;

    /**
     * 训练更新网络（智能积攒经验，延迟批量更新），5.16新增
     */
    public void update3(INDArray state, int action, double reward, double gamma, double learningRate) {
        try {
            // 1. 将当前步的经验存入回放池缓存（仅存储，不发生耗时的梯度计算）
            stateBuffer.add(state);
            actionBuffer.add(action);
            rewardBuffer.add(reward);

            // 2. 当积攒的数据达到指定 Batch Size 时，触发一次极其高效的批量矩阵运算
            if (stateBuffer.size() >= batchSize) {
                triggerBatchUpdate(learningRate);
            }
        } catch (Exception e) {
            System.err.println("经验池存储失败: " + e.getMessage());
        }
    }


    /**2026.5.20 14:05新增代码的测试模型,在19号模型代码的基础上测试，减少了训练时间会降低效果,继续优化修改，100请求
     * 核心提速与寻优引擎：具备“破局能力”的批量更新逻辑
     * 核心提速与寻优引擎：【极速贪婪记忆版】批量更新
     */
    /**
     * 核心提速与寻优引擎：【按比例奖惩的极速版】批量更新
     */
    private void triggerBatchUpdate1(double learningRate) {
        int currentBatchSize = stateBuffer.size();
        if (currentBatchSize == 0) return;

        try {
            // 1. 张量拼接
            INDArray[] reshapedStates = new INDArray[currentBatchSize];
            for (int i = 0; i < currentBatchSize; i++) {
                reshapedStates[i] = reshapeForCNN(stateBuffer.get(i));
            }
            INDArray batchInput = Nd4j.vstack(reshapedStates);
            INDArray batchOutput = network.output(batchInput);
            INDArray batchLabels = batchOutput.dup();

            int numActions = (int) batchOutput.size(1);

            for (int i = 0; i < currentBatchSize; i++) {
                double reward = rewardBuffer.get(i);
                int action = actionBuffer.get(i);
                double actionProb = batchOutput.getDouble(i, action);

                if (reward > 0) {
                    // ==========================================
                    // 【核心修复：按比例放大奖赏！】
                    // 杜绝无脑加固定值！优秀的低碳路径(reward大)加分多，高碳路径(reward小)加分少！
                    // 乘以 10.0 是为了适应 Batch=8 的平滑特性，赋予模型足够的下探动力
                    // ==========================================
                    double boost = learningRate * reward * 10.0;
                    double targetProb = Math.min(0.99, actionProb + boost);

                    batchLabels.putScalar(new int[]{i, action}, targetProb);
                    adjustRemainderBatch(batchLabels, i, action, actionProb, targetProb, numActions);
                } else {
                    // 映射失败：执行严厉的按比例惩罚
                    double penalty = learningRate * Math.abs(reward) * 10.0;
                    double targetProb = Math.max(0.001, actionProb - penalty);
                    batchLabels.putScalar(new int[]{i, action}, targetProb);
                    adjustRemainderBatch(batchLabels, i, action, actionProb, targetProb, numActions);
                }

                // ==========================================
                // 强制对矩阵的第 i 行进行归一化，绝对防止交叉熵爆炸
                // ==========================================
                INDArray row = batchLabels.getRow(i);
                row.divi(row.sumNumber().doubleValue());
            }

            // 批量反向传播，平稳且极速
            network.fit(batchInput, batchLabels);

            // 显式提示 ND4J 清理堆外内存垃圾
            Nd4j.getMemoryManager().invokeGc();

            stateBuffer.clear();
            actionBuffer.clear();
            rewardBuffer.clear();

        } catch (Exception e) {
            System.err.println("批量更新失败: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * 核心提速引擎：批量更新逻辑
     */

    private void triggerBatchUpdate(double learningRate) {
        int currentBatchSize = stateBuffer.size();
        if (currentBatchSize == 0) return;

        try {
            // 移除 MemoryWorkspace 包装，避免和 DL4J 内部机制冲突
            INDArray[] reshapedStates = new INDArray[currentBatchSize];
            for (int i = 0; i < currentBatchSize; i++) {
                reshapedStates[i] = reshapeForCNN(stateBuffer.get(i));
            }

            // 拼接批次并获取输出
            INDArray batchInput = Nd4j.vstack(reshapedStates);
            INDArray batchOutput = network.output(batchInput);
            INDArray batchLabels = batchOutput.dup();

            int numActions = (int) batchOutput.size(1);
            double stableLR = learningRate * 0.2;

            for (int i = 0; i < currentBatchSize; i++) {
                double reward = rewardBuffer.get(i);
                int action = actionBuffer.get(i);
                double actionProb = batchOutput.getDouble(i, action);

                baselineCount++;
                if (baselineCount == 1) {
                    movingBaseline = reward;
                } else {
                    movingBaseline = 0.02 * reward + 0.98 * movingBaseline;
                }
                double advantage = reward - movingBaseline;

                if (reward < -0.5) {
                    double newProb = Math.max(0.001, actionProb - stableLR * 2.0);
                    batchLabels.putScalar(new int[]{i, action}, newProb);
                    adjustRemainderBatch(batchLabels, i, action, actionProb, newProb, numActions);
                } else if (advantage > 0) {
                    double boost = stableLR * advantage;
                    double newProb = Math.min(0.99, actionProb + boost);
                    batchLabels.putScalar(new int[]{i, action}, newProb);
                    adjustRemainderBatch(batchLabels, i, action, actionProb, newProb, numActions);
                } else {
                    double penalty = stableLR * Math.abs(advantage);
                    double newProb = Math.max(0.005, actionProb - penalty);
                    batchLabels.putScalar(new int[]{i, action}, newProb);
                    adjustRemainderBatch(batchLabels, i, action, actionProb, newProb, numActions);
                }

                INDArray row = batchLabels.getRow(i);
                row.divi(row.sumNumber().doubleValue());
            }

            // 批量反向传播，交给 DL4J 内部去管理底层内存
            network.fit(batchInput, batchLabels);

            // ==========================================
            // 核心兜底策略：显式提示 ND4J 清理堆外内存垃圾
            // 每处理完 32 个经验才调用一次，极大地减少了卡顿
            // ==========================================
            Nd4j.getMemoryManager().invokeGc();

            // 清空经验池，迎接下一批
            stateBuffer.clear();
            actionBuffer.clear();
            rewardBuffer.clear();

        } catch (Exception e) {
            System.err.println("批量更新失败: " + e.getMessage());
            e.printStackTrace();
        }
    }


    /**
     * 辅助方法：在二维矩阵中等比例缩放其余节点的概率
     */
    private void adjustRemainderBatch(INDArray batchLabels, int rowIndex, int targetAction, double oldTargetProb, double newTargetProb, int totalActions) {
        double remainder = 1.0 - newTargetProb;
        double oldRemainder = 1.0 - oldTargetProb;
        if (oldRemainder > 1e-6) {
            for (int j = 0; j < totalActions; j++) {
                if (j != targetAction) {
                    double scaledVal = batchLabels.getDouble(rowIndex, j) * (remainder / oldRemainder);
                    batchLabels.putScalar(new int[]{rowIndex, j}, Math.max(1e-6, scaledVal));
                }
            }
        }
    }
//===============================================================
    /**
     * 2026.5.14重大新增
     * 功能：严厉惩罚并压低该次选择的概率！这将逼迫智能体拼命寻找全网最绿、跳数最短的极致解
     * 训练更新网络（使用REINFORCE策略梯度）
     */
    /*
    public void update2(INDArray state, int action, double reward, double gamma, double learningRate) {
        try {
            // 1. 动态更新滑动平均基线 (Baseline)
            baselineCount++;
            if (baselineCount == 1) {
                movingBaseline = reward; // 首次直接初始化
            } else {
                // 采用平滑加权追踪系统的平均回报水平
                movingBaseline = 0.02 * reward + 0.98 * movingBaseline;
            }

            // 2. 计算核心指标：优势值 (Advantage) = 实际回报 - 平均基线
            // 只有超越平均水平的决策，才配得到权重的提升！
            double advantage = reward - movingBaseline;

            // 前向传播获取当前输出
            INDArray cnnInput = reshapeForCNN(state);
            INDArray output = network.output(cnnInput);

            double actionProb = output.getDouble(0, action);
            int numActions = (int) output.size(1);
            INDArray labels = output.dup();

            // 为了抑制高频单步更新带来的遗忘震荡，动态缩小单步实际作用的步长
            double stableLR = learningRate * 0.2;

            // 3. 基于优势值进行合规的策略梯度方向引导
            // 如果映射彻底失败 (reward == -1.0)，绕过基线予以绝对惩罚
            if (reward < -0.5) {
                double newProb = Math.max(0.001, actionProb - stableLR * 2.0);
                labels.putScalar(0, action, newProb);
                adjustRemainder(labels, action, actionProb, newProb, numActions);
            }
            // 超越基线：表现优异，予以正向奖励
            else if (advantage > 0) {
                double boost = stableLR * advantage;
                double newProb = Math.min(0.99, actionProb + boost);
                labels.putScalar(0, action, newProb);
                adjustRemainder(labels, action, actionProb, newProb, numActions);
            }
            // 成功连通但低于基线：平庸的成功，予以温和抑制，倒逼模型去选更优解
            else {
                double penalty = stableLR * Math.abs(advantage);
                double newProb = Math.max(0.005, actionProb - penalty);
                labels.putScalar(0, action, newProb);
                adjustRemainder(labels, action, actionProb, newProb, numActions);
            }

            // 严谨归一化，绝对保障传入交叉熵的标签总和完美为 1.0
            labels.divi(labels.sumNumber().doubleValue());

            // 执行网络权重的平滑微调
            network.fit(cnnInput, labels);

        } catch (Exception e) {
            System.err.println("动态基线更新失败: " + e.getMessage());
        }
    }

     */

    // 辅助私有方法：按比例无损缩放其余候选节点的概率分布
    private void adjustRemainder(INDArray labels, int targetAction, double oldTargetProb, double newTargetProb, int totalActions) {
        double remainder = 1.0 - newTargetProb;
        double oldRemainder = 1.0 - oldTargetProb;
        if (oldRemainder > 1e-6) {
            for (int i = 0; i < totalActions; i++) {
                if (i != targetAction) {
                    double scaledVal = labels.getDouble(0, i) * (remainder / oldRemainder);
                    labels.putScalar(0, i, Math.max(1e-6, scaledVal));
                }
            }
        }
    }

//===============================================================
    /**
     * 2026.5.12新增
     * 训练更新网络（使用REINFORCE策略梯度）
     */
    public void update1(INDArray state, int action, double reward, double gamma, double learningRate) {
        try {
        	// 前向传播获取概率
            INDArray cnnInput = reshapeForCNN(state);
            INDArray output = network.output(cnnInput);
            
            // 计算动作的概率
            double actionProb = output.getDouble(0, action);
            
            int numActions = (int) output.size(1);
            INDArray labels = Nd4j.zeros(1, numActions);
            
            // 策略梯度：奖励越高，该动作的目标概率越大
            // 这里简化处理，将奖励作为目标值
            double targetValue = Math.tanh(reward); // 压缩到[-1, 1]范围
            labels.putScalar(new int[]{0, action}, targetValue);
            
            network.fit(cnnInput, labels);
            
            System.out.printf("更新: 动作=%d, 奖励=%.3f, 概率=%.3f%n", 
                             action, reward, actionProb);
            
        } catch (Exception e) {
            System.err.println("更新失败: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void update(INDArray state, int action, double reward, double gamma, double learningRate) {
        try {
            // 前向传播获取当前合法概率分布
            INDArray cnnInput = reshapeForCNN(state);
            INDArray output = network.output(cnnInput);

            double actionProb = output.getDouble(0, action);
            int numActions = (int) output.size(1);

            // 复制当前输出作为基底标签，确保整体和始终为 1.0
            INDArray labels = output.dup();

            // 动态平滑调整策略：安全、合规，绝对杜绝梯度爆炸
            if (reward > 0) {
                // 映射成功或正反馈：推高当前物理机房的选中概率
                double boost = learningRate * reward; // 根据回报动态决定提升步长
                double newProb = Math.min(0.99, actionProb + boost);
                labels.putScalar(0, action, newProb);

                // 其余机房概率等比例自适应衰减，严格维持概率总和为 1.0
                double remainder = 1.0 - newProb;
                double oldRemainder = 1.0 - actionProb;
                if (oldRemainder > 1e-6) {
                    for (int i = 0; i < numActions; i++) {
                        if (i != action) {
                            double scaledVal = labels.getDouble(0, i) * (remainder / oldRemainder);
                            labels.putScalar(0, i, Math.max(1e-5, scaledVal));
                        }
                    }
                }
            } else {
                // 映射失败或负反馈：安全压低当前动作概率
                double newProb = Math.max(0.001, actionProb - learningRate * Math.abs(reward));
                labels.putScalar(0, action, newProb);

                double remainder = 1.0 - newProb;
                double oldRemainder = 1.0 - actionProb;
                if (oldRemainder > 1e-6) {
                    for (int i = 0; i < numActions; i++) {
                        if (i != action) {
                            double scaledVal = labels.getDouble(0, i) * (remainder / oldRemainder);
                            labels.putScalar(0, i, Math.max(1e-5, scaledVal));
                        }
                    }
                }
            }

            // 归一化保险锁：强制除以总和，严防浮点数精度累积误差
            labels.divi(labels.sumNumber().doubleValue());

            // 安全更新网络
            network.fit(cnnInput, labels);

            // 调试输出可保留观察
            // System.out.printf("更新成功: 动作=%d, 奖励=%.3f, 旧概率=%.3f, 新概率=%.3f%n", action, reward, actionProb, labels.getDouble(0, action));

        } catch (Exception e) {
            System.err.println("更新失败: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    
    private void applyGradient(Gradient gradient, double lr) {
        try {
            // 方法A：直接更新参数（最简单）
            INDArray params = network.params();
            INDArray grad = gradient.gradient();
            
            if (grad != null) {
                // 应用学习率
                params.subi(grad.mul(lr));
            }
            
            // 方法B：使用优化器配置（如果需要复杂的优化器如Adam）
            updateWithOptimizer(gradient, lr);
            
        } catch (Exception e) {
            System.err.println("应用梯度失败: " + e.getMessage());
        }
    }
    
    private void updateWithOptimizer(Gradient gradient, double lr) {
        // 获取优化器
        org.deeplearning4j.nn.api.Updater updater = network.getUpdater();
        
        if (updater != null) {
            try {
                // 创建workspace manager
                LayerWorkspaceMgr workspaceMgr = LayerWorkspaceMgr.noWorkspaces();
                
                // 更新优化器状态
                updater.update(network, gradient, 
                    network.getIterationCount(), // iteration
                    0,                          // epoch  
                    1,                          // batchSize
                    workspaceMgr);              // 必需参数
                
            } catch (Exception e) {
                System.err.println("优化器更新失败，使用简单更新: " + e.getMessage());
                
                // 回退到简单更新
                INDArray params = network.params();
                INDArray grad = gradient.gradient();
                if (grad != null) {
                    params.subi(grad.mul(lr));
                }
            }
        }
    }
    
    /**
     * 获取网络参数（用于保存/加载模型）
     */
    public INDArray getParameters() {
        return network.params();
    }
    
    /**
     * 设置网络参数
     */
    public void setParameters(INDArray params) {
        network.setParams(params);
    }
	
    
    
}
