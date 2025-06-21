package graph;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class TopicManagerSingleton {
    public static class TopicManager {
        private static final TopicManager instance = new TopicManager();
        Map<String, Topic> topics;

        private TopicManager() {
            topics = new ConcurrentHashMap<>();
        }

        /**
         * Retrieves a Topic object associated with the given numbers.
         * If the Topic does not exist, a new one is created and added to the map.
         *
         * @param numbers The unique identifier for the Topic.
         * @return The Topic object associated with the given numbers.
         */
        public Topic getTopic(String numbers) {
            Topic topic = topics.get(numbers);
            if (topic == null) { // If Topic doesn't exist, create a new one
                topic = new Topic(numbers);
                topics.put(numbers, topic);
            }
            return topic;
        }

        public Collection<Topic> getTopics() {
            return topics.values();
        }

        public void clear() {
            topics.clear();
        }
    }

    public static TopicManager get() {
        return TopicManager.instance;
    }
}
