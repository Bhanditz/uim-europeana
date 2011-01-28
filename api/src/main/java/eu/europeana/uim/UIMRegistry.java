package eu.europeana.uim;

import eu.europeana.uim.api.IngestionPlugin;
import eu.europeana.uim.api.LoggingEngine;
import eu.europeana.uim.api.Orchestrator;
import eu.europeana.uim.api.Registry;
import eu.europeana.uim.api.StorageEngine;
import eu.europeana.uim.api.Workflow;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

public class UIMRegistry implements Registry {

    private static Logger log = Logger.getLogger(UIMRegistry.class.getName());

    private String defaultStorageEngine;
    private StorageEngine activeStorage = null;
    private List<StorageEngine> storages = new ArrayList<StorageEngine>();

    private String defaultLoggingEngine;
    private LoggingEngine activeLogging = null;
    private Map<String, LoggingEngine<?>> loggers = new HashMap<String, LoggingEngine<?>>();

    private Map<String, IngestionPlugin> plugins = new HashMap<String, IngestionPlugin>();
    private List<Workflow> workflows = new ArrayList<Workflow>();

    private Orchestrator orchestrator = null;

    public UIMRegistry() {
    }

    public void setDefaultStorageEngine(String defaultStorageEngine) {
        // do not allow setting false values
        if (activeStorage != null && getStorage(defaultStorageEngine) != null) {
            this.activeStorage = getStorage(defaultStorageEngine);
            this.defaultStorageEngine = defaultStorageEngine;
        } else if (activeStorage != null) {
            log.severe("Attempt to set default storage engine to '" + defaultStorageEngine + "' failed, not making the change");
        } else {
            log.info("Setting default storage engine to " + defaultStorageEngine);
            this.defaultStorageEngine = defaultStorageEngine;
        }
    }


    public void setDefaultLoggingEngine(String defaultLoggingEngine) {
        // do not allow setting false values
        if (activeLogging != null && getStorage(defaultLoggingEngine) != null) {
            this.activeLogging = getLoggingEngine(defaultLoggingEngine);
            this.defaultLoggingEngine = defaultLoggingEngine;
        } else if (activeLogging != null) {
            log.severe("Attempt to set default logging engine to '" + defaultLoggingEngine + "' failed, not making the change");
        } else {
            log.info("Setting default logging engine to " + defaultLoggingEngine);
            this.defaultLoggingEngine = defaultLoggingEngine;
        }
    }


    @Override
    public List<Workflow> getWorkflows() {
        return workflows;
    }

    @Override
    public Workflow getWorkflow(String identifier) {
        for (Workflow w : workflows) {
            if (w.getName().equals(identifier)) {
                return w;
            }
        }
        return null;
    }

    @Override
    public Workflow getWorkflow(Long id) {
        for (Workflow w : workflows) {
            if (w.getId().equals(id)) {
                return w;
            }
        }
        return null;
    }

    @Override
    public void addPlugin(IngestionPlugin plugin) {
        if (plugin != null) {
            log.info("Added plugin:" + plugin.getIdentifier());
            if (!plugins.containsKey(plugin.getIdentifier()))
                plugins.put(plugin.getIdentifier(), plugin);
        }
    }

    @Override
    public IngestionPlugin getPlugin(String identifier) {
        return plugins.get(identifier);
    }

    @Override
    public void removePlugin(IngestionPlugin plugin) {
        if (plugin != null) {
            log.info("Removed plugin:" + plugin.getIdentifier());
            plugins.remove(plugin.getIdentifier());
        }
    }


    @Override
    public void addStorage(StorageEngine storage) {
        if (storage != null) {
            log.info("Added storage:" + storage.getIdentifier());
            if (!storages.contains(storage)) {
                storage.initialize();
                this.storages.add(storage);

                // activate default storage
                if (activeStorage == null) {
                    activeStorage = storage;
                } else if (storage.getIdentifier().equals(defaultStorageEngine)) {
                    activeStorage = storage;
                    log.info("Making storage " + storage.getIdentifier() + " default");
                }
            }
        }
    }


    @Override
    public void removeStorage(StorageEngine storage) {
        if (storage != null) {
            log.info("Removed storage:" + storage.getIdentifier());
            storage.shutdown();
            this.storages.remove(storage);
        }
    }

    @Override
    public List<StorageEngine> getStorages() {
        return storages;
    }


    @Override
    public void addWorkflow(Workflow workflow) {
        if (workflow != null) {
            log.info("Added workflow: " + workflow.getName());
            if (!workflows.contains(workflow))
                workflows.add(workflow);
        }
    }

    @Override
    public void removeWorkflow(Workflow workflow) {
        if (workflow != null) {
            log.info("Removed workflow: " + workflow.getName());
            workflows.remove(workflow);
        }
    }

    @Override
    public void setActiveStorage(StorageEngine storage) {
        activeStorage = storage;
    }

    @Override
    public StorageEngine getStorage() {
        if (storages == null || storages.isEmpty()) return null;
        if (activeStorage == null) {
            if (getStorage(defaultStorageEngine) != null) {
                activeStorage = getStorage(defaultStorageEngine);
            } else {
                activeStorage = storages.get(0);
            }
        }
        return activeStorage;
    }

    @Override
    public StorageEngine getStorage(String identifier) {
        if (identifier == null || storages == null || storages.isEmpty()) return null;
        for (StorageEngine storage : storages) {
            if (identifier.equals(storage.getIdentifier())) {
                return storage;
            }
        }
        return null;
    }

    @Override
    public void addLoggingEngine(LoggingEngine logging) {
        if (logging != null) {
            log.info("Added logging engine:" + logging.getIdentifier());
            if (!loggers.containsKey(logging.getIdentifier())) {
                loggers.put(logging.getIdentifier(), logging);
                // activate default logging
                if (activeLogging == null) {
                    activeLogging = logging;
                } else if (logging.getIdentifier().equals(defaultLoggingEngine)) {
                    activeLogging = logging;
                    log.info("Making logging engine " + logging.getIdentifier() + " default");
                }
            }
        }
    }

    @Override
    public List<LoggingEngine<?>> getLoggingEngines() {
        List<LoggingEngine<?>> res = new ArrayList<LoggingEngine<?>>();
        res.addAll(loggers.values());
        return res;
    }

    @Override
    public LoggingEngine<?> getLoggingEngine() {
        if (loggers == null || loggers.isEmpty()) return null;
        if (activeLogging == null) {
            if (getLoggingEngine(defaultLoggingEngine) != null) {
                activeLogging = getLoggingEngine(defaultLoggingEngine);
            } else {
                activeLogging = loggers.values().toArray(new LoggingEngine[] {})[0];
            }
        }
        return activeLogging;
    }

    @Override
    public void setActiveLoggingEngine(LoggingEngine loggingEngine) {
        activeLogging = loggingEngine;
    }

    @Override
    public LoggingEngine<?> getLoggingEngine(String identifier) {
        if (identifier == null || loggers == null || loggers.isEmpty()) return null;
        return loggers.get(identifier);
    }

    public String toString() {
        StringBuilder builder = new StringBuilder();

        builder.append("\nRegistered plugins:");
        builder.append("\n--------------------------------------");
        if (plugins.isEmpty()) {
            builder.append("\n\tNo plugins. ");
        } else {
            for (IngestionPlugin plugin : plugins.values()) {
                if (builder.length() > 0) {
                    builder.append("\n\tPlugin:");
                }
                builder.append(plugin.getIdentifier()).append(": [").append(plugin.getDescription()).append("]");
            }
        }

        builder.append("\nRegistered workflows:");
        builder.append("\n--------------------------------------");
        if (plugins.isEmpty()) {
            builder.append("\n\tNo workflows. ");
        } else {
            for (Workflow worfklow : workflows) {
                if (builder.length() > 0) {
                    builder.append("\n\tWorkflow:");
                }
                builder.append(worfklow.getName()).append(": [").append(worfklow.getDescription()).append("]");
            }
        }

        builder.append("\nRegistered storage:");
        builder.append("\n--------------------------------------");
        if (storages.isEmpty()) {
            builder.append("\n\tNo storage.");
        } else {
            for (StorageEngine storage : storages) {
                if (builder.length() > 0) {
                    builder.append("\n\t");
                }
                if (activeStorage != null && activeStorage == storage) {
                    builder.append("* ");
                } else {
                    builder.append("  ");
                }
                builder.append(storage.getIdentifier());
                builder.append(" [").append(storage.getStatus()).append("] ");
                builder.append(storage.getConfiguration().toString());
            }
        }

        builder.append("\nRegistered logging:");
        builder.append("\n--------------------------------------");
        if (loggers.isEmpty()) {
            builder.append("\n\tNo logging.");
        } else {
            for (LoggingEngine loggingEngine : loggers.values()) {
                if (builder.length() > 0) {
                    builder.append("\n\t");
                }
                if (activeLogging != null && activeLogging == loggingEngine) {
                    builder.append("* ");
                } else {
                    builder.append("  ");
                }
                builder.append(loggingEngine.getIdentifier());
            }
        }


        return builder.toString();
    }

}
