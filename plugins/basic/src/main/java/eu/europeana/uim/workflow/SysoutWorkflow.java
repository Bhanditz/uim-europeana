package eu.europeana.uim.workflow;

import eu.europeana.uim.api.AbstractWorkflow;
import eu.europeana.uim.api.IngestionWorkflowStep;
import eu.europeana.uim.orchestration.BatchWorkflowStart;

public class SysoutWorkflow extends AbstractWorkflow {

	public SysoutWorkflow(int plugins, int batchSize, boolean randsleep, boolean savepoint) {
		setName(SysoutWorkflow.class.getSimpleName()); 
		setDescription("Simple workflow which uses several SysoutPlugins to report to the console about processing");
		setStart(new BatchWorkflowStart(batchSize));
		
		for (int i = 0 ; i < plugins; i++){
			addStep(new IngestionWorkflowStep(new SysoutPlugin("" + i, randsleep), savepoint));
		}
		
	}


}
