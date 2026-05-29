package Team.CloudStorage.EAVONE.DeepReinforceLearning;

//package com.vne.drl.agent;

import org.deeplearning4j.nn.api.OptimizationAlgorithm;
import org.deeplearning4j.nn.conf.*;
import org.deeplearning4j.nn.conf.inputs.InputType;
import org.deeplearning4j.nn.conf.layers.*;
import org.deeplearning4j.nn.conf.preprocessor.FeedForwardToRnnPreProcessor;
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;
import org.deeplearning4j.nn.weights.WeightInit;
import org.deeplearning4j.optimize.api.TrainingListener;
import org.deeplearning4j.optimize.listeners.ScoreIterationListener;
import org.deeplearning4j.util.ModelSerializer;
import org.nd4j.linalg.activations.Activation;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.factory.Nd4j;
import org.nd4j.linalg.learning.config.Adam;
import org.nd4j.linalg.lossfunctions.LossFunctions;

import java.io.File;
import java.io.IOException;
import java.util.*;

import org.deeplearning4j.nn.conf.MultiLayerConfiguration;
import org.deeplearning4j.nn.conf.NeuralNetConfiguration;
import org.deeplearning4j.nn.conf.layers.ConvolutionLayer;
import org.deeplearning4j.nn.conf.layers.DenseLayer;
import org.deeplearning4j.nn.conf.layers.OutputLayer;

import org.nd4j.linalg.ops.transforms.Transforms;

import java.util.Arrays;


/**
 * 四层策略网络（对应论文中的结构）
 */
public class PolicyNetwork {
    private MultiLayerNetwork network;
    private int inputSize;
    private int outputSize;
    private int featPerNode = 3;//输入的物理节点的状态数量
    private int nodeCount;
    
    public PolicyNetwork(int inputSize, int outputSize, int nodeCount) {
        this.inputSize = inputSize;
        this.outputSize = outputSize;
        this.nodeCount = nodeCount;
        buildNetwork();
    }
    
    private void buildNetwork() {
        // 四层结构，如论文所述
        MultiLayerConfiguration conf = new NeuralNetConfiguration.Builder()
            .seed(123)
            .updater(new Adam(0.001))
            .list()
            // 提取层（全连接）
            .layer(new DenseLayer.Builder()
                .nIn(inputSize).nOut(128)
                .activation(Activation.RELU)
                .build())
            // 卷积层（或全连接替代）
            .layer(new DenseLayer.Builder()
                .nIn(128).nOut(64)
                .activation(Activation.RELU)
                .build())
            // 概率层
            .layer(new OutputLayer.Builder(LossFunctions.LossFunction.NEGATIVELOGLIKELIHOOD)
                .nIn(64).nOut(outputSize)
                .activation(Activation.SOFTMAX)
                .build())
            .build();
        
        this.network = new MultiLayerNetwork(conf);
        this.network.init();
    }
    
 // ===========================
    // ===创建基于CNN的模型===
    // ===========================
    private void buildNetworkCNN() {
        MultiLayerConfiguration conf = new NeuralNetConfiguration.Builder()
                .seed(123)
                .updater(new Adam(0.001))
                .list()
                // =======CNN层噶度和宽度分别是1和3，两个方向的步长分别是1和1，因为有两个卷积=======
                //featPerNode:为输入通道
                .layer(new ConvolutionLayer.Builder(new int[]{1, 3}, new int[]{1, 1})
                        .nIn(featPerNode)       
                        .nOut(2)                
                        .activation(Activation.RELU)
                        .build())


                // ======= �����㣺ȫ���Ӳ�1 =======
                .layer(new DenseLayer.Builder()
                        .nOut(16)               // 64-32�����Ͳ��������Ա��ֱ����
                        .activation(Activation.RELU)
                        .build())

                // ======= ���Ĳ㣺����� =======
                .layer(new OutputLayer.Builder(LossFunctions.LossFunction.MCXENT)
                        .activation(Activation.SOFTMAX)
                        .nOut(nodeCount)        // ����ڵ��� = ��������
                        .build())

                // ======= �������� =======
                .setInputType(InputType.convolutional(1, nodeCount, featPerNode))
                .build();

        network = new MultiLayerNetwork(conf);
        network.init();

        // �������Ƶ�ʵ���һ�㣬��ֹƵ����ӡ�����ٶ�
        network.setListeners(new ScoreIterationListener(1000));

        System.out.println("DRLAgent ��ʼ���ɹ� | �ڵ���=" + nodeCount + " ������=" + featPerNode);
    }
    
    public double[] forward(double[] state) {
        INDArray input = Nd4j.create(state).reshape(1, inputSize);
        INDArray output = network.output(input);
        return output.toDoubleVector();
    }
    
    public double[] forwardLog(double[] state) {
        INDArray input = Nd4j.create(state).reshape(1, inputSize);
        INDArray preOutput = network.feedForward(input).get(2);  // 获取softmax前输出
        return preOutput.toDoubleVector();
    }
    
    public void updateWeights(double[] state, double[] gradient, double lr,double[] output) {
        // 自定义梯度更新逻辑
        // 这里简化表示，实际需处理梯度累积
        network.fit(Nd4j.create(state),Nd4j.create(output));//.fit(new SingletonDataSet(
            //Nd4j.create(state), 
            //Nd4j.create(gradient)
        //));
    }
}