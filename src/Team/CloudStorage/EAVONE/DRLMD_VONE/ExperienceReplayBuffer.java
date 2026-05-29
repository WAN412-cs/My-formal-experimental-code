// ExperienceReplayBuffer.java
package Team.CloudStorage.EAVONE.DRLMD_VONE;

import java.util.*;

public class ExperienceReplayBuffer {
    public final int capacity;
    private final List<TrainingExperience> buffer;
    private final Random random;

    public ExperienceReplayBuffer(int capacity) {
        this.capacity = capacity;
        this.buffer = new ArrayList<>();
        this.random = new Random();
    }

    public void add(TrainingExperience experience) {
        if (buffer.size() >= capacity) {
            buffer.remove(0); // 移除最旧的经验
        }
        buffer.add(experience);
    }

    public List<TrainingExperience> sample(int batchSize) {
        List<TrainingExperience> batch = new ArrayList<>();
        int size = Math.min(batchSize, buffer.size());
        for (int i = 0; i < size; i++) {
            batch.add(buffer.get(random.nextInt(buffer.size())));
        }
        return batch;
    }

    public int size() {
        return buffer.size();
    }
}
