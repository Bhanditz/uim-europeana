package eu.europeana.uim.workflow;

import eu.europeana.uim.api.WorkflowStep;
import eu.europeana.uim.orchestration.StepProcessor;
import eu.europeana.uim.orchestration.WorkflowProcessor;

/**
 * @author Manuel Bernhardt <bernhardt.manuel@gmail.com>
 */
public interface StepProcessorProvider {

    StepProcessor createStepProcessor(WorkflowStep step, WorkflowProcessor processor, boolean isSavePoint);

}
