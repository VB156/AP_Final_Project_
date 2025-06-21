package configs;

/**
 * This interface defines the methods that must be implemented by any class that represents a configuration.
 */
public interface Config {
    void create();
    String getName();
    int getVersion();
    void close();
}
