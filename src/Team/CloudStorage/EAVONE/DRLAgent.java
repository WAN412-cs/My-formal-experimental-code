package Team.CloudStorage.EAVONE;

import org.deeplearning4j.nn.conf.MultiLayerConfiguration;
import org.deeplearning4j.nn.conf.NeuralNetConfiguration;
import org.deeplearning4j.nn.conf.inputs.InputType;
import org.deeplearning4j.nn.conf.layers.ConvolutionLayer;
import org.deeplearning4j.nn.conf.layers.DenseLayer;
import org.deeplearning4j.nn.conf.layers.OutputLayer;
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;
import org.deeplearning4j.optimize.listeners.ScoreIterationListener;
import org.deeplearning4j.util.ModelSerializer;
import org.nd4j.linalg.activations.Activation;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.factory.Nd4j;
import org.nd4j.linalg.ops.transforms.Transforms;
import org.nd4j.linalg.learning.config.Adam;
import org.nd4j.linalg.lossfunctions.LossFunctions;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;

/**
 * DRLAgent��DL4J 1.0.0-M2.x ���ݰ棩
 * - �Ĳ����ṹ (Input �� Conv �� Dense �� Softmax)
 * - ���߼�ʱ���� (Algorithm 1 Line 11)
 * - ������Ȩ rt (Eq. 25)
 * - �¶� Softmax ̽������
 * - ���˲� mask ��Ч����ڵ�
 * - ��̬ѧϰ�ʵ��� (ȫ�� Adam ʵ��)
 */
public class DRLAgent {

    // ========= ������ =========
    private double temperature = 1.0;
    private static final double TEMP_DECAY = 0.002;
    private static final double MIN_TEMPERATURE = 0.1;

    // ========= ����ṹ���� =========
    private final int nodeCount;        // ���ά�� = ����ڵ���
    private final int featPerNode = 3;  // ״̬���� (R_S, TR_B, Dis)


    // ========= ѵ��״̬ =========
    private int trainingCount = 0;
    private double lastReward = 0.0;

    // ========= ȫ�� Adam �Ż������ɶ�̬����ѧϰ�ʣ� =========
    private final Adam optimizer = new Adam(0.01);

    private MultiLayerNetwork network;

    public DRLAgent(int nodeCount) {
        this.nodeCount = nodeCount;
        buildNetwork();
    }

    // ===========================
    // === ��ʱ���������� ===
    // ===========================
    public void updateWithCurrentExperience(double[][] stateMatrix, int[] vNodeEmbed,
                                            double reward, double[][] nextStateMatrix, double rt) {
        try {
            INDArray state = toCnnInput(stateMatrix);
            INDArray action = createPaperStandardAction(vNodeEmbed);

            double weightedReward = reward * rt;
            performImmediateUpdate(state, action, weightedReward);

            updateTemperature();
            trainingCount++;
            this.lastReward = reward;
            if(Parameters.DebugModel) {
                System.out.printf(" ���߸��� #%d | ����=%.4f, rt=%.4f, ��Ȩ����=%.4f, �¶�=%.4f%n",
                        trainingCount, reward, rt, weightedReward, temperature);
            }

        } catch (Exception e) {
            System.err.println(" ��ʱ����ʧ��: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // ===========================
    // === ��ʱ���������� ===
    // ===========================
    private void performImmediateUpdate(INDArray state, INDArray action, double reward) {
        try {
            INDArray inputBatch = state.reshape(1, state.size(1), state.size(2), state.size(3));
            INDArray labelBatch = action.reshape(1, action.size(1));

            applyRewardWeightedTraining(inputBatch, labelBatch, reward);

        } catch (Exception e) {
            System.err.println("?? ����������ʧ��: " + e.getMessage());
            e.printStackTrace();
        }
    }


    // ===========================
    // === ������Ȩѵ�� ===
    // ===========================
    private void applyRewardWeightedTraining(INDArray inputs, INDArray labels, double reward) {
        double baseLr = 0.01;
        double lrMultiplier = calculateLearningRateMultiplier(reward);
        double actualLr = baseLr * lrMultiplier;

        try {
            // ��̬����ȫ�� Adam ѧϰ��
            optimizer.setLearningRate(actualLr);

            // �Ƴ����� NullPointerException �� setStateViewArray ����
            // network.getUpdater().setStateViewArray(network, null, false);

            network.setListeners(new ScoreIterationListener(1000));

            network.fit(inputs, labels);
            if(Parameters.DebugModel) {
                System.out.printf("������Ȩѵ��: ����=%.4f, ����=%.2f, ʵ��LR=%.5f%n",
                        reward, lrMultiplier, optimizer.getLearningRate());
            }

        } catch (Exception e) {
            System.err.println("������Ȩѵ��ʧ��: " + e.getMessage());
            e.printStackTrace();
        }
    }


    private double calculateLearningRateMultiplier(double reward) {
        // ʹ�� if-else if ������������ if������ж�Ч��
        if (reward > 0.8) return 2.0;
        else if (reward > 0.6) return 1.5;
        else if (reward > 0.3) return 1.0;
        else if (reward > 0.0) return 0.7;
        else if (reward > -0.5) return 0.5;
        else return 0.3;
    }

    // ===========================
    // === one-hot �������� ===
    // ===========================
    private INDArray createPaperStandardAction(int[] vNodeEmbed) {
        double[] action = new double[nodeCount];
        Arrays.fill(action, 0.0);
        for (int sNode : vNodeEmbed) {
            if (sNode >= 0 && sNode < action.length) {
                action[sNode] = 1.0;
            }
        }
        return Nd4j.create(action).reshape(1, action.length);
    }

    // ===========================
    // === ��������ṹ ===
    // ===========================
    private void buildNetwork() {
        MultiLayerConfiguration conf = new NeuralNetConfiguration.Builder()
                .seed(123)
                .updater(optimizer) // ? ʹ��ȫ�� Adam ʵ��
                .list()
                // ======= ��һ�㣺��������� =======
                .layer(new ConvolutionLayer.Builder(new int[]{1, 3}, new int[]{1, 1})
                        .nIn(featPerNode)       // ������������ÿ���ڵ�3������
                        .nOut(2)                // ��16����8�����ٲ�����
                        .activation(Activation.RELU)
                        .build())

                // ======= �ڶ��㣺ȫ�ֳػ��� =======
                .layer(new org.deeplearning4j.nn.conf.layers.GlobalPoolingLayer.Builder()
                        .poolingType(org.deeplearning4j.nn.conf.layers.PoolingType.AVG) // ��Ϊƽ���ػ�����ƽ��
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

    // ===========================
    // === ״̬����ת CNN ���� ===
    // ===========================
    public INDArray toCnnInput(double[][] stateMatrix) {
        INDArray input = Nd4j.create(1, featPerNode, 1, nodeCount);
        for (int n = 0; n < nodeCount; n++) {
            for (int c = 0; c < featPerNode; c++) {
                double v = (n < stateMatrix.length && c < stateMatrix[n].length) ? stateMatrix[n][c] : 0.0;
                input.putScalar(new int[]{0, c, 0, n}, v);
            }
        }
        return input;
    }

    // ===========================
    // === softmax ������� ===
    // ===========================
    public double[] getActionProbabilities(double[][] stateMatrix) {
    	// 检查网络输入层配置
        for (int i = 0; i < network.getnLayers(); i++) {
            System.out.println("第 " + i + " 层:");
            System.out.println("  类型: " + network.getLayer(i).type());
            System.out.println("  输入大小: " + network.layerInputSize(i));
            System.out.println("  输出大小: " + network.layerSize(i));
            System.out.println("  参数数量: " + network.getLayer(i).numParams());
        }
        
        INDArray input = toCnnInput(stateMatrix);
        INDArray output = network.output(input, false);
        System.out.println("✅ 输出数据: " + output);

        INDArray logp = Transforms.log(output.add(1e-10), true);
        INDArray tempered = logp.div(temperature);
        INDArray exp = Transforms.exp(tempered, true);
        INDArray probs = exp.div(exp.sum(1));

        return probs.reshape(probs.length()).toDoubleVector();
    }

    // ===========================
    // === ���˲� ===
    // ===========================
    public double[] maskInvalidNodes(double[] probs, EOSubstrateNetwork sub) {
        double[] masked = Arrays.copyOf(probs, probs.length);
        for (int s = 0; s < masked.length; s++) {
            boolean invalid = false;
            if (s >= sub.nodes || sub.cpu[s] <= 0.0) invalid = true;

            boolean hasPositiveLink = false;
            for (int i = 0; i < sub.links; i++) {
                if ((sub.link[i].from == s || sub.link[i].to == s) && sub.link[i].bw > 0.0) {
                    hasPositiveLink = true; break;
                }
            }
            if (!hasPositiveLink) invalid = true;
            if (invalid) masked[s] = 0.0;
        }

        double sum = Arrays.stream(masked).sum();
        if (sum <= 0.0) Arrays.fill(masked, 1.0 / masked.length);
        else for (int i = 0; i < masked.length; i++) masked[i] /= sum;

        return masked;
    }

    // ===========================
    // === �¶�˥�� ===
    // ===========================
    public void updateTemperature() {
        temperature = Math.max(MIN_TEMPERATURE, temperature - TEMP_DECAY);
    }

    // ===========================
    // === ��Ϣ�ӿ� ===
    // ===========================
    public int getOutputSize() { return nodeCount; }
    public double getTemperature() { return temperature; }
    public int getTrainingCount() { return trainingCount; }

    public double getLastReward() { return lastReward; }
    public void setLastReward(double reward) { this.lastReward = reward; }

    public String getTrainingStats() {
        double loss = network.score();
        return String.format("ѵ������=%d, ��ʧ=%.4f, �¶�=%.4f",
                trainingCount, loss, temperature);
    }

    public void saveModel(String path) throws IOException {
        ModelSerializer.writeModel(network, new File(path), true);
    }

    public void loadModel(String path) throws IOException {
        File f = new File(path);
        if (f.exists()) network = ModelSerializer.restoreMultiLayerNetwork(f);
        else saveModel(path);
        //else throw new IOException("ģ���ļ�������: " + path);
    }
    /**
     * �ڲ��Խ׶λ�ȡ��߸��ʵĶ�����ȷ���Բ��ԣ�
     */
    public int getBestAction(double[][] stateMatrix, EOSubstrateNetwork sub,int[] sNodeEmbed) {
        double[] probabilities = getActionProbabilities(stateMatrix);
        probabilities = maskInvalidNodes(probabilities, sub);

        int bestAction = -1;
        double bestProb = -1.0;

        for (int i = 0; i < probabilities.length; i++) {
            if (probabilities[i] > bestProb && sNodeEmbed[i] == -1 ) {
                bestProb = probabilities[i];
                bestAction = i;
            }
        }

        return bestAction;
    }
    
    

}
