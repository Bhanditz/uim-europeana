 /**
 * Copyright 2007 EDL FOUNDATION
 *
 * Licensed under the EUPL, Version 1.1 or - as soon they
 * will be approved by the European Commission - subsequent
 * versions of the EUPL (the "Licence");
 * you may not use this work except in compliance with the
 * Licence.
 * You may obtain a copy of the Licence at:
 *
 * http://ec.europa.eu/idabc/eupl
 *
 * Unless required by applicable law or agreed to in
 * writing, software distributed under the Licence is
 * distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied.
 * See the Licence for the specific language governing
 * permissions and limitations under the Licence.
 */ 
package eu.europeana.uim.plugin.thumbler.service;

import java.io.File;
import java.io.Serializable;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.theeuropeanlibrary.model.common.Link;
import org.theeuropeanlibrary.model.common.qualifier.LinkStatus;
import org.theeuropeanlibrary.model.tel.ObjectModelRegistry;
import org.theeuropeanlibrary.uim.check.weblink.AbstractLinkIngestionPlugin;
import org.theeuropeanlibrary.uim.check.weblink.http.GuardedMetaDataRecordUrl;
import org.theeuropeanlibrary.uim.check.weblink.http.Submission;
import eu.europeana.uim.model.adapters.AdapterFactory;
import eu.europeana.uim.model.adapters.MetadataRecordAdapter;
import eu.europeana.uim.model.adapters.QValueAdapterStrategy;
import eu.europeana.uim.model.adapters.europeana.EuropeanaLinkAdapterStrategy;
import eu.europeana.uim.plugin.thumbler.impl.EuropeanaWeblinkThumbler;
import eu.europeana.uim.store.Collection;
import eu.europeana.uim.store.Execution;
import eu.europeana.uim.store.MetaDataRecord;
import eu.europeana.uim.store.Request;
import eu.europeana.uim.store.UimDataSet;
import eu.europeana.uim.store.MetaDataRecord.QualifiedValue;
import eu.europeana.uim.sugar.SugarService;
import eu.europeana.uim.orchestration.ActiveExecution;
import eu.europeana.uim.orchestration.ExecutionContext;
import eu.europeana.uim.plugin.ingestion.CorruptedDatasetException;
import eu.europeana.uim.plugin.ingestion.IngestionPluginFailedException;
import eu.europeana.uim.logging.LoggingEngine;
import eu.europeana.uim.storage.StorageEngine;
import eu.europeana.uim.storage.StorageEngineException;
import eu.europeana.uim.common.TKey;



/**
 * Main Implementation of the Thumbler plugin. It is basically a variation
 * of the linkchecking plugin that checks only the links that represent 
 * images in the relevant section of the EDM document (europeana:object)
 * and uses these to create 3 different thumbnails (TINY,MEDIUM,LARGE).
 * It also embeds metadata information contained in the correspording EDM
 * XML document in all three thumbnails. 
 * 
 * @author Georgios Markakis <gwarkx@hotmail.com>
 * @since 12 Apr 2012
 */
public class ThumblerPlugin<I> extends AbstractLinkIngestionPlugin<I> {

    private final static SimpleDateFormat df        = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    /**
     * Set the Logging variable to use logging within this class
     */
    private static final Logger           log       = Logger.getLogger(ThumblerPlugin.class.getName());
    
    
    private static SugarService           sugarService;
    
    protected static final TKey<ThumblerPlugin, EuropeanaLinkData> DATA = TKey.register(
    		ThumblerPlugin.class,
            "europeanalinkdata", EuropeanaLinkData.class);
    
    
    
	/**
	 * Default Constructor
	 */
	public ThumblerPlugin() {
		super("thumbler_plugin", "store remote thumbnails into MongoDB ");
	}



	/* (non-Javadoc)
	 * @see eu.europeana.uim.api.IngestionPlugin#processRecord(eu.europeana.uim.store.MetaDataRecord, eu.europeana.uim.api.ExecutionContext)
	 */
	@Override
    public boolean process(MetaDataRecord<I> mdr, ExecutionContext<MetaDataRecord<I>, I> context)
            throws IngestionPluginFailedException, CorruptedDatasetException {
		
		EuropeanaLinkData value = context.getValue(DATA);
		
		List<QualifiedValue<Link>> linkList = mdr.getQualifiedValues(ObjectModelRegistry.LINK);
		
	        int index = 0;
            for(QualifiedValue<Link> qlink : linkList){ 
            	
            	synchronized (value) {
                    value.submitted++;
                }
            	
                final LoggingEngine<I> loggingEngine = context.getLoggingEngine();
                try {
					EuropeanaWeblinkThumbler.getShared().offer(
					        new GuardedMetaDataRecordUrl<I>(context.getExecution(), mdr, qlink.getValue(), index++,
					                new URL(qlink.getValue().getUrl())) {
					            @SuppressWarnings("unchecked")
					            @Override
					            public void processed(int status, String message) {
					                LinkStatus state = null;
					                
					                if (status == 0) {
					                    state = LinkStatus.CACHED;
					                } else if (status == 1) {
					                    state = LinkStatus.FAILED_CONNECTION;
					                } 

					                getLink().setLastChecked(new Date());
					                getLink().setLinkStatus(state);

					                String time = df.format(getLink().getLastChecked());

					                Execution<I> execution = getExecution();

					                loggingEngine.logLink(execution, "thumbler", getMetaDataRecord(),
					                        getLink().getUrl(), status, time, message,
					                        getUrl().getHost(), getUrl().getPath());

					                Submission submission = EuropeanaWeblinkThumbler.getShared().getSubmission(
					                        execution);

					                if (submission != null) {
					                    synchronized (submission) {
					                        execution.putValue("thumbler.processed",
					                                "" + submission.getProcessed());
					                        
					                        if (!execution.isActive()) {
					                            
					                            // need to store our own
					                            try {
					                                if (submission.getProcessed() % 500 == 0) {
					                                    if (((StorageEngine<I>)submission.getStorageEngine()) != null) {
					                                        ((StorageEngine<I>)submission.getStorageEngine()).updateExecution(execution);
					                                    }
					                                } else if (!submission.hasRemaining()) {
					                                    if (((StorageEngine<I>)submission.getStorageEngine()) != null) {
					                                        ((StorageEngine<I>)submission.getStorageEngine()).updateExecution(execution);
					                                    }
					                                }
					                            } catch (StorageEngineException e) {
					                                throw new RuntimeException(
					                                        "Caused by StorageEngineException", e);
					                            }
					                            
					                        }
					                    }
					                }

					                log.info("Checked <" + getLink().getUrl() + ">" + message);
					            }
					        }, context);
				} catch (MalformedURLException e) {
					e.printStackTrace();
				}

            }

		return false;
	}
		

	/* (non-Javadoc)
	 * @see eu.europeana.uim.api.IngestionPlugin#getParameters()
	 */
	public List<String> getParameters() {
        return Collections.emptyList();
	}


	/* (non-Javadoc)
	 * @see eu.europeana.uim.api.IngestionPlugin#initialize(eu.europeana.uim.api.ExecutionContext)
	 */
	@Override
	public void initialize(ExecutionContext<MetaDataRecord<I>,I> context)
			throws IngestionPluginFailedException {
		EuropeanaLinkData value = new EuropeanaLinkData();
		
	      Collection<?> collection = null;
	        UimDataSet<?> dataset = context.getDataSet();
	        if (dataset instanceof Collection) {
	            collection = (Collection<?>)dataset;
	        } else if (dataset instanceof Request<?>) {
	            collection = ((Request<?>)dataset).getCollection();
	        }

	        String time = df.format(new Date());
	        String mnem = collection != null ? collection.getMnemonic() : "NULL";
	        String name = collection != null ? collection.getName() : "No collection";

	        context.putValue(DATA, value);
	        
	        context.getLoggingEngine().log(context.getExecution(), Level.INFO, "thumbler",
	                "initialize", mnem, name, time);
	}

	
    /* (non-Javadoc)
     * @see org.theeuropeanlibrary.uim.check.weblink.AbstractLinkIngestionPlugin#completed(eu.europeana.uim.api.ExecutionContext)
     */
    @Override
    public void completed(ExecutionContext<MetaDataRecord<I>,I> context) throws IngestionPluginFailedException {
    	EuropeanaLinkData value = context.getValue(DATA);


        Collection<I> collection = null;
        UimDataSet<I> dataset = context.getDataSet();
        if (dataset instanceof Collection) {
            collection = (Collection<I>)dataset;
        } else if (dataset instanceof Request<?>) {
            collection = ((Request<I>)dataset).getCollection();
        }

        String time = df.format(new Date());
        String mnem = collection != null ? collection.getMnemonic() : "NULL";
        String name = collection != null ? collection.getName() : "No collection";
        context.getLoggingEngine().log(context.getExecution(), Level.INFO, "thumbler",
                "completed", mnem, name, "" + value.submitted, "" + value.ignored, time);

        context.getExecution().putValue("thumbler.ignored", "" + value.ignored);
        context.getExecution().putValue("thumbler.submitted", "" + value.submitted);

        
        
        Submission submission = EuropeanaWeblinkThumbler.getShared().getSubmission(context.getExecution());

        if (submission != null) {
        	context.getExecution().putValue("thumbler.errors", "" + submission.getExceptions());
        	context.getExecution().putValue("thumbler.processed", "" + submission.getProcessed());

        }

        try {
            if (collection != null) {

                //collection.putValue(SugarControlledVocabulary.COLLECTION_LINK_VALIDATION,
                //        "" + context.getExecution().getId());

                ((ActiveExecution<MetaDataRecord<I>,I>)context).getStorageEngine().updateCollection(collection);

                if (getSugarService() != null) {
                    getSugarService().updateCollection(collection);
                }
            }
        } catch (Throwable t) {
            context.getLoggingEngine().logFailed(Level.INFO, this, t,
                    "Update collection or sugar service on " + collection + " failed");
            log.log(Level.WARNING, "Failed to update collection or call sugar service: " +
                                   collection, t);
        }

    }



    /**
     * @param sugarService
     */
    public void setSugarService(SugarService sugarService) {
    	ThumblerPlugin.sugarService = sugarService;
    }

    /**
     * @param sugarService
     */
    public void unsetSugarService(SugarService sugarService) {
    	ThumblerPlugin.sugarService = null;
    }

    /**
     * @return the sugar service
     */
    public SugarService getSugarService() {
        return ThumblerPlugin.sugarService;
    }


    /**
     * Container holding all execution specific information for the validation plugin.
     */
    protected static class EuropeanaLinkData implements Serializable {

		private static final long serialVersionUID = 1L;

		int             ignored    = 0;
        int             submitted  = 0;

        File            directory;
    }


}
