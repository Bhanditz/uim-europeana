package eu.europeana.uim.orchestration;

import eu.europeana.uim.MetaDataRecord;
import eu.europeana.uim.api.ActiveExecution;
import eu.europeana.uim.api.Orchestrator;
import eu.europeana.uim.api.Registry;
import eu.europeana.uim.api.StorageEngine;
import eu.europeana.uim.api.StorageEngineException;
import eu.europeana.uim.api.Workflow;
import eu.europeana.uim.api.WorkflowStepStatus;
import eu.europeana.uim.common.ProgressMonitor;
import eu.europeana.uim.store.Collection;
import eu.europeana.uim.store.Execution;
import eu.europeana.uim.store.Provider;
import eu.europeana.uim.store.Request;
import eu.europeana.uim.store.UimEntity;
import eu.europeana.uim.workflow.WorkflowProcessorProvider;
import org.osgi.service.blueprint.container.ServiceUnavailableException;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

/**
 * Orchestrates the ingestion job execution. The orchestrator keeps a map of WorkflowProcessors, one for each different workflow.
 * When a new request for workflow execution comes in, the WorkflowProcessor for the Workflow is retrieved, or created if it does not exist.
 */
public class UIMOrchestrator implements Orchestrator {

    private static Logger log = Logger.getLogger(UIMOrchestrator.class.getName());

    public static final int BATCH_SIZE = 100;

    private final Registry registry;

    private WorkflowProcessorProvider processorProvider;

    private Map<Workflow, WorkflowProcessor> processors = new HashMap<Workflow, WorkflowProcessor>();

    private Map<ActiveExecution, Integer> executionTotals = new HashMap<ActiveExecution, Integer>();


    public UIMOrchestrator(Registry registry, WorkflowProcessorProvider processorProvider) {
        this.registry = registry;
        this.processorProvider = processorProvider;
    }

    @Override
    public String getIdentifier() {
        return UIMOrchestrator.class.getSimpleName();
    }

    @Override
    public ActiveExecution executeWorkflow(Workflow w, MetaDataRecord<?> mdr, ProgressMonitor monitor) {
        monitor.beginTask(w.getName(), 1);
        return executeWorkflow(w, monitor, mdr);
    }

    @Override
    public ActiveExecution executeWorkflow(Workflow w, Collection c, ProgressMonitor monitor) {
        return executeWorkflow(w, monitor, c);
    }


    @Override
    public ActiveExecution executeWorkflow(Workflow w, Request r, ProgressMonitor monitor) {
        return executeWorkflow(w, monitor, r);
    }

    @Override
    public ActiveExecution executeWorkflow(Workflow w, Provider p, ProgressMonitor monitor) {
        return executeWorkflow(w, monitor, p);
    }

    @Override
    public java.util.Collection<ActiveExecution> getActiveExecutions() {
        return executionTotals.keySet();
    }

    @Override
    public void pause(ActiveExecution execution) {
        throw new RuntimeException("No can do");
    }

    @Override
    public void cancel(ActiveExecution execution) {
        throw new RuntimeException("No can do");
    }

    public boolean allDataProcessed(ActiveExecution e) {
        return executionTotals.get(e) == getTotal(e);
    }

    /**
     * Executes a given workflow. A new Execution is created and a WorkflowProcessor created if none exists for this workflow
     *
     * @param w the workflow to execute
     * @param monitor the ProgressMonitor tracking this Execution
     * @param dataset the data set on which this Execution runs
     * @return a new ActiveExecution for this execution request
     */
    private ActiveExecution executeWorkflow(Workflow w, ProgressMonitor monitor, UimEntity dataset) {
        Execution e = getStorageService().createExecution();
        UIMExecution activeExecution = new UIMExecution(e.getId(), dataset, monitor, w);
        executionTotals.put(activeExecution, 0);

        WorkflowProcessor wp = processors.get(w);
        if (wp == null) {
            wp = processorProvider.createProcessor(w, this, registry);
            processors.put(w, wp);
            wp.start();
        }
        e.setActive(true);
        e.setDataSet(dataset);
        e.setWorkflowIdentifier(w.getName());
        e.setStartTime(new Date());

        try {
            registry.getStorage().updateExecution(e);
        } catch (StorageEngineException e1) {
            log.severe("Could not update execution details: " + e1.getMessage());
            e1.printStackTrace();
        }
        wp.addExecution(activeExecution);
        return activeExecution;
    }

    private boolean hasExecution(Execution e) {
        for (Execution exec : executionTotals.keySet()) {
            if (exec.getId() == e.getId()) {
                return true;
            }
        }
        return false;
    }


    @Override
    public synchronized long[] getBatchFor(ActiveExecution e) {

        log.fine(String.format("Requesting next batch for execution %d", e.getId()));

        UIMExecution ae = (UIMExecution) e;
        Integer counter = executionTotals.get(ae);
        // if we don't have anything for this execution, assume it's done
        if (counter == null) {
            return null;
        }
        int total = getTotal(ae);
        long[] all;

        UimEntity dataset = ae.getDataSet();
        if (dataset instanceof MetaDataRecord<?>) {
            return new long[]{ae.getDataSet().getId()};
        } else if (dataset instanceof Collection) {
            all = getStorageService().getByCollection((Collection) dataset);
        } else if (dataset instanceof Provider) {
            all = getStorageService().getByProvider((Provider) dataset, false);
        } else {
            throw new RuntimeException("Should not be here");
        }

        if (all == null) {
            return null;
        }

        int remaining = total - counter;
        long[] result = null;
        if (remaining > BATCH_SIZE) {
            result = new long[BATCH_SIZE];
//            log.fine(String.format("Preparing batch from all MDRs with size %d, counter at %d, remaining: %d", all.length, counter, remaining));
            System.arraycopy(all, counter, result, 0, BATCH_SIZE);
            counter += BATCH_SIZE;
            executionTotals.put(e, counter);
        } else if (remaining < BATCH_SIZE && remaining > 0) {
            result = new long[remaining];
            System.arraycopy(all, counter, result, 0, remaining);
            counter = total;
            executionTotals.put(e, counter);
        } else if (remaining == 0) {
            return null;
        }

        return result;
    }

    @Override
    public int getTotal(ActiveExecution e) {
        UimEntity dataSet = e.getDataSet();
        if (dataSet instanceof MetaDataRecord<?>) {
            return 1;
        } else if (dataSet instanceof Collection) {
            return getStorageService().getTotalByCollection((Collection) dataSet);
        } else if (dataSet instanceof Provider) {
            return getStorageService().getTotalByProvider((Provider) dataSet, false);
        } else {
            throw new RuntimeException("Should not be here, we got a " + dataSet);
        }
    }

    protected void notifyExecutionDone(ActiveExecution e) {
        for (Execution execution : registry.getStorage().getExecutions()) {
            if (execution.getId() == e.getId()) {
                execution.setActive(false);
                execution.setEndTime(new Date());
                try {
                    registry.getStorage().updateExecution(execution);
                } catch (StorageEngineException e1) {
                    log.severe("Could not update execution details: " + e1.getMessage());
                    e1.printStackTrace();
                }
            }
        }
        executionTotals.remove(e);
    }

    private StorageEngine getStorageService() {
        StorageEngine r = null;
        try {
            r = registry.getStorage();
        } catch (Throwable t) {
            if (t instanceof ServiceUnavailableException) {
                // TODO shutdown gracefully
                t.printStackTrace();
            } else {
                t.printStackTrace();
            }
        }
        return r;
    }

    @Override
    public List<WorkflowStepStatus> getRuntimeStatus(Workflow w) {
        return processors.get(w).getRuntimeStatus(w);
    }
}
