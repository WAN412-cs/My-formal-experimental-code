// TrainingExperience.java
package Team.CloudStorage.EAVONE.DRLMD_VONE;

import org.nd4j.linalg.api.ndarray.INDArray;

public class TrainingExperience {
    private final INDArray state;
    private final INDArray action;
    private final double reward;
    private final INDArray nextState;


    public TrainingExperience(INDArray state, INDArray action, double reward, INDArray nextState) {
        this.state = state;
        this.action = action;
        this.reward = reward;
        this.nextState = nextState;
    }

    // Getters
    public INDArray getState() { return state; }
    public INDArray getAction() { return action; }
    public double getReward() { return reward; }
    public INDArray getNextState() { return nextState; }
}
