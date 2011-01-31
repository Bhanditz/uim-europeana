package eu.europeana.uim.api;


import java.util.Collection;
import java.util.List;

/**
 * Registry for UIM services
 */
public interface Registry {
    
    void addPlugin(IngestionPlugin plugin);

    IngestionPlugin getPlugin(String identifier);

    void removePlugin(IngestionPlugin plugin);

    void addStorage(StorageEngine storage);

    void removeStorage(StorageEngine storage);

    Collection<StorageEngine> getStorages();

    void setConfiguredStorageEngine(String configuredStorageEngine);

    void setActiveStorage(StorageEngine storage);

    void addWorkflow(Workflow workflow);

    List<Workflow> getWorkflows();

    Workflow getWorkflow(String identifier);

    Workflow getWorkflow(Long id);

    void removeWorkflow(Workflow workflow);

    StorageEngine getStorage();

    StorageEngine getStorage(String identifier);

    void addLoggingEngine(LoggingEngine<?> loggingEngine);

    void removeLoggingEngine(LoggingEngine<?> loggingEngine);

    List<LoggingEngine<?>> getLoggingEngines();

    LoggingEngine<?> getLoggingEngine();

    LoggingEngine<?> getLoggingEngine(String identifier);

    void setActiveLoggingEngine(LoggingEngine<?> loggingEngine);

}
