package eu.europeana.uim.orchestration;

import eu.europeana.uim.FieldRegistry;
import eu.europeana.uim.MetaDataRecord;
import eu.europeana.uim.api.ActiveExecution;
import eu.europeana.uim.api.Registry;
import eu.europeana.uim.api.SavePoint;
import eu.europeana.uim.api.StorageEngineException;
import eu.europeana.uim.api.Task;
import eu.europeana.uim.api.TaskStatus;
import eu.europeana.uim.api.Workflow;
import eu.europeana.uim.api.WorkflowStep;
import eu.europeana.uim.workflow.StepProcessorProvider;

import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.Vector;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

/**
 * Builds the execution process for a given workflow, and executes one or more Executions for it. Also does exception handling, reporting etc.
 * A WorkflowProcessor handles the processing on a per-record basis for multiple Executions.
 * <br/>
 * When created, the WorkflowProcessor creates a list of StepProcessors, one for each workflow step.
 * When executed, the WorkflowProcessor starts itself (as repeated timer task) and walks over the list of StepProcessors that represent the workflow,
 * refilling the queues as necessary.
 * It communicates with a parent Orchestrator in order to perform storage operations and retrieve the next elements to process.
 *
 * @author Manuel Bernhardt <bernhardt.manuel@gmail.com>
 */
public class WorkflowProcessor extends TimerTask implements RecordProvider, ProcessingMonitor {

    private static Logger log = Logger.getLogger(WorkflowProcessor.class.getName());

    private final Workflow workflow;

    private final UIMOrchestrator orchestrator;

    private final Registry registry;

    private final StepProcessorProvider stepProcessorProvider;

    private Vector<UIMExecution> executions = new Vector<UIMExecution>();

    // keep track of tasks for monitoring
    private Map<Task, ActiveExecution> tasks = new ConcurrentHashMap<Task, ActiveExecution>();

    protected Vector<StepProcessor> stepProcessors = new Vector<StepProcessor>();

    private final Timer processorTimer;

    /**
     * Creates a new WorkflowProcessor and adds the Execution to it
     *
     * @param w the Workflow this processor follows
     * @param o the Orchestrator for this processor
     */
    public WorkflowProcessor(Workflow w, UIMOrchestrator o, Registry r, StepProcessorProvider provider) {
        this.orchestrator = o;
        this.workflow = w;
        this.registry = r;
        this.stepProcessorProvider = provider;
        processorTimer = new Timer();

        // construct the set of StepThreadPools based on the workflow
        // here comes in the logic for SavePoints, i.e. we mark a StepProcessor as "saving" if:
        // - a SavePoint is defined _after_ the step in the workflow
        // (- by default, if the step is a ProcessingContainer?)
        for (int i = 0; i < w.getSteps().size(); i++) {
            WorkflowStep step = w.getSteps().get(i);


            // TODO save point logic for processing containers -- have a queue of some sort?

            // ignore SavePoints as actual steps, they are just descriptive information
            if (!(step instanceof SavePoint)) {
                boolean savePoint = false;
                if (i < w.getSteps().size() - 1) {
                    WorkflowStep next = w.getSteps().get(i + 1);
                    if (next instanceof SavePoint) {
                        savePoint = true;
                    }
                } else if (i == w.getSteps().size() - 1) {
                    // we always save after the last step
                    savePoint = true;
                }
                stepProcessors.add(stepProcessorProvider.createStepProcessor(step, this, savePoint));
            }
        }
    }

    /**
     * Adds a new execution to the processor
     *
     * @param e the UIMExecution to be handled by the processor
     */
    public void addExecution(UIMExecution e) {
        e.getMonitor().beginTask(buildTaskName(e), orchestrator.getTotal(e));
        this.executions.add(e);
    }

    @Override
    public void addTask(Task t, ActiveExecution e) {
        tasks.put(t, e);
    }

    /**
     * Removes an Execution from the processor. As a result, a graceful shutdown of the Execution will occur
     *
     * @param e the Execution to remove
     */
    public void removeExecution(UIMExecution e) {
        this.executions.remove(e);

        // TODO remove all associated tasks from the tasks map
    }

    /**
     * Starts the processor
     */
    public void start() {
        log.info("Starting new WorkflowProcessor for Workfow '" + workflow.getName() + "'");

        // TODO make this configurable
        processorTimer.schedule(this, 0, 100);
    }

    @Override
    public void run() {

        // System.out.println("Tick tack");

        // asynchronous: start new thread that will
        // - for the first WorkflowStepTreadPool, retrieve actual MDRs from the storage and pass them to the first queue
        // - walk over all pools and move tasks from one queue to the next depending on how full the next one is
        // - handle reporting (using the throwables)
        // - handle cancelling
        // - handle logging
        // - become idle if there's nothing much to do (optimization)
        // - implement WorldPeace

        if (executions.size() > 0) {
            for (int i = 0; i < stepProcessors.size(); i++) {
                StepProcessor sp = stepProcessors.get(i);
                //System.out.println("STEP " + i + " " + sp.toString());

                if (i == 0) {
                    // special treatment for the first step which gets MDRs directly from the storage
                    fillFirstStepProcessorQueue(sp);
                } else {
                    StepProcessor previous = stepProcessors.get(i - 1);
                    sp.startProcessing();
                    previous.passToNext(sp);

                    if (i == stepProcessors.size() - 1) {
                        // clear the successful tasks of the last step
                        sp.clearSuccess();
                    }
                }
            }

            // monitoring for tasks
            Vector<Task> doneTasks = new Vector<Task>();
            for (Task t : tasks.keySet()) {
                if (t.getStatus() == TaskStatus.DONE || t.getStatus() == TaskStatus.FAILED) {
                    tasks.get(t).getMonitor().worked(1);
                    doneTasks.add(t);
                }
            }
            for (Task t : doneTasks) {
                tasks.remove(t);
            }

            // check our executions
            Vector<ActiveExecution> done = new Vector<ActiveExecution>();
            for (UIMExecution e : executions) {
                if (executionDone(e)) {
                    done.add(e);
                    e.getMonitor().done();
                }
            }
            for (ActiveExecution d : done) {
                executions.remove(d);
                orchestrator.notifyExecutionDone(d);
            }

        }


    }

    private boolean executionDone(ActiveExecution e) {
        boolean allTasksDone = true;
        for (Task t : tasks.keySet()) {
            if (t.getStatus() != TaskStatus.DONE) {
                allTasksDone = false;
            } else {
                // cleanup
                tasks.remove(t);
            }
        }
        return orchestrator.allDataProcessed(e) && allTasksDone;
    }

    /**
     * For the first step in the workflow, retrieve batches MetaDataRecords and create UIMTasks out of them
     *
     * @param sp the StepProcessor for the worklow
     */
    private void fillFirstStepProcessorQueue(StepProcessor sp) {
        // TODO we probably can do this dynamically. For this Orchestrator#getBatchFor needs to handle an argument
        // right now we have a fixed batch size that we use in order to refill the queues
        if (sp.remainingCapacity() > UIMOrchestrator.BATCH_SIZE * executions.size()) {
            for (UIMExecution e : executions) {
                long[] work = orchestrator.getBatchFor(e);
                if (work != null) {
                    sp.addRecords(e, work);
                }
            }
        }
        sp.startProcessing();
    }

    @Override
    public MetaDataRecord<FieldRegistry> getMetaDataRecord(long id) {
        if (registry.getStorage() == null) {
            throw new RuntimeException("No storage module active");
        }
        return registry.getStorage().getMetaDataRecords(id)[0];
    }

    @Override
    public void updateMetaDataRecord(MetaDataRecord<FieldRegistry> mdr) throws StorageEngineException {
        if (registry.getStorage() == null) {
            throw new RuntimeException("No storage module active");
        }
        registry.getStorage().updateMetaDataRecord(mdr);
    }

    private String buildTaskName(ActiveExecution ae) {
        return "Workflow: " + workflow.getName() + " Execution: " + ae.getId();
    }

    @Override
    public String toString() {
        String res = "";
        res += "WorkflowProcessor for workflow '" + workflow.getName() + "' (" + workflow.getDescription() + ")\n\n";
        for(StepProcessor sp : stepProcessors) {
            res += sp.toString() + "\n";
        }
        return res;
    }

    public static void main(String... args) {

        // testing how the ThreadPoolExecutor works

        LinkedBlockingQueue<Runnable> queue = new LinkedBlockingQueue<Runnable>();
        for (int i = 0; i < 100; i++) {
            queue.add(new DummyRunnable());
        }
        ThreadPoolExecutor threadPoolExecutor = new ThreadPoolExecutor(1, 10, StepProcessor.KEEP_ALIVE_TIME, TimeUnit.MILLISECONDS, queue);
        threadPoolExecutor.execute(new Thread());
    }

    private static class DummyRunnable implements Runnable {
        private static int count = 0;
        private int id = 0;

        public DummyRunnable() {
            id = count++;
        }

        @Override
        public void run() {
            System.out.println("DummyRunnable " + id);
            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

        }
    }

}
