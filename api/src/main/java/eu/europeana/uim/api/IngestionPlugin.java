package eu.europeana.uim.api;

import eu.europeana.uim.TKey;
import eu.europeana.uim.MDRFieldRegistry;
import eu.europeana.uim.MetaDataRecord;

public interface IngestionPlugin extends WorkflowStep {

	public String getIdentifier();
	public String getDescription();
	
	public TKey<MDRFieldRegistry,?>[] getInputParameters();
	public TKey<MDRFieldRegistry,?>[] getOutputParameters();
	public TKey<MDRFieldRegistry,?>[] getTransientParameters();

    public void processRecord(MetaDataRecord<?> mdr);
	
	
}
