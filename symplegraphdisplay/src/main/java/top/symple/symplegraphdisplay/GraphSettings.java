package top.symple.symplegraphdisplay;

public class GraphSettings {
    private static final double DEFAULT_UPDATE_INTERVAL = 1;
    private static final boolean DEFAULT_STORE_DATA = false;

    private double updateInterval;
    private boolean storeData;

    public GraphSettings() {
        this.updateInterval = DEFAULT_UPDATE_INTERVAL;
        this.storeData = DEFAULT_STORE_DATA;
    }

    /**
     * Set the delay between graph updates
     * @param updateInterval time in sec
     */
    public GraphSettings setUpdateInterval(double updateInterval) {
        this.updateInterval = updateInterval;
        return this;
    }

    /**
     * When `true` it will store the data in the memory
     * @param storeData should store data in memory?
     */
    public GraphSettings setStoreData(boolean storeData) {
        this.storeData = storeData;
        return this;
    }

    /**
     * Get the delay between graph updates
     * @return delay between graph updates in sec
     */
    public double getUpdateInterval() {
        return updateInterval;
    }

    /**
     * @return `true` if storing data in memory
     */
    public boolean isStoreData() {
        return storeData;
    }

    /**
     * Reset the current settings to default
     */
    public void reset() {
        this.updateInterval = DEFAULT_UPDATE_INTERVAL;
        this.storeData = DEFAULT_STORE_DATA;
    }

    /**
     * <Strong>NOTE:</Strong> This method will copy the settings from the settings parameter to this {@link GraphSettings} instance
     */
    public void fromSettings(GraphSettings settings) {
        this.updateInterval = settings.updateInterval;
        this.storeData = settings.storeData;
    }
}
