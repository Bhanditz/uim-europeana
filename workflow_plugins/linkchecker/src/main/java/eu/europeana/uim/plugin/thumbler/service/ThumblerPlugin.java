/*
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

import java.io.StringReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.jibx.runtime.BindingDirectory;
import org.jibx.runtime.IBindingFactory;
import org.jibx.runtime.IUnmarshallingContext;
import org.jibx.runtime.JiBXException;
import org.theeuropeanlibrary.model.common.Link;
import org.theeuropeanlibrary.model.common.qualifier.LinkStatus;
import org.theeuropeanlibrary.model.tel.ObjectModelRegistry;
import org.theeuropeanlibrary.uim.check.weblink.AbstractLinkIngestionPlugin;
import org.theeuropeanlibrary.uim.check.weblink.http.GuardedMetaDataRecordUrl;
import org.theeuropeanlibrary.uim.check.weblink.http.Submission;
import org.theeuropeanlibrary.uim.check.weblink.http.WeblinkLinkchecker;
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
import eu.europeana.uim.sugarcrm.SugarService;
import eu.europeana.uim.api.CorruptedMetadataRecordException;
import eu.europeana.uim.api.ExecutionContext;
import eu.europeana.uim.api.IngestionPluginFailedException;
import eu.europeana.uim.api.LoggingEngine;
import eu.europeana.uim.api.StorageEngine;
import eu.europeana.uim.api.StorageEngineException;
import eu.europeana.uim.common.TKey;



/**
 *
 * @author Georgios Markakis <gwarkx@hotmail.com>
 * @since 12 Apr 2012
 */
public class ThumblerPlugin extends AbstractLinkIngestionPlugin {

    private final static SimpleDateFormat df        = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    /**
     * Set the Logging variable to use logging within this class
     */
    private static final Logger           log       = Logger.getLogger(ThumblerPlugin.class.getName());
    
    
    private static SugarService           sugarService;
    
	public ThumblerPlugin() {
		super("thumbler_plugin", "check the integrity of http links contained in records");
	}



	/* (non-Javadoc)
	 * @see eu.europeana.uim.api.IngestionPlugin#processRecord(eu.europeana.uim.store.MetaDataRecord, eu.europeana.uim.api.ExecutionContext)
	 */
	public   <I> boolean processRecord(MetaDataRecord<I> mdr, ExecutionContext<I> context)	
    throws IngestionPluginFailedException, CorruptedMetadataRecordException{
		
		
        // Adapter that ensures compatibility with the europeana datamodel 
		Map<TKey<?, ?>, QValueAdapterStrategy<?, ?, ?, ?>> strategies =  new HashMap<TKey<?, ?>, QValueAdapterStrategy<?, ?, ?, ?>>();
		
		strategies.put(ObjectModelRegistry.LINK, new EuropeanaLinkAdapterStrategy());
		
		 MetadataRecordAdapter<I, QValueAdapterStrategy<?, ?, ?, ?>> mdrad = AdapterFactory.getAdapter(mdr, strategies);
		 
		 List<QualifiedValue<Link>> linkList = mdrad.getQualifiedValues(ObjectModelRegistry.LINK);
		
	
	        int index = 0;

            for(QualifiedValue<Link> qlink : linkList){
            	

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
					                    state = LinkStatus.FAILED_CONNECTION;
					                } else if (status < 400) {
					                    state = LinkStatus.VALID;
					                } else if (status < 500) {
					                    state = LinkStatus.FAILED_FOURHUNDRED;
					                } else {
					                    state = LinkStatus.FAILED_FIVEHUNDRED;
					                }

					                getLink().setLastChecked(new Date());
					                getLink().setLinkStatus(state);

					                String time = df.format(getLink().getLastChecked());

					                Execution<I> execution = getExecution();

					                loggingEngine.logLink(execution, "linkcheck", getMetaDataRecord(),
					                        getLink().getUrl(), status, time, message,
					                        getUrl().getHost(), getUrl().getPath());

					                Submission submission = WeblinkLinkchecker.getShared().getSubmission(
					                        execution);

					                if (submission != null) {
					                    synchronized (submission) {
					                        execution.putValue("linkcheck.processed",
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
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

            }

		return false;
	}
		

	public void shutdown() {
		// TODO Auto-generated method stub
		
	}

	public List<String> getParameters() {
        return Collections.EMPTY_LIST;
	}


	public void initialize() {
  
		
	}

	public <I> void initialize(ExecutionContext<I> context)
			throws IngestionPluginFailedException {
		
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

	        context.getLoggingEngine().log(context.getExecution(), Level.INFO, "linkcheck",
	                "initialize", mnem, name, time);
	}

	
    @Override
    public <I> void completed(ExecutionContext<I> context) throws IngestionPluginFailedException {
        Data value = context.getValue(DATA);

        /*
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
        context.getLoggingEngine().log(context.getExecution(), Level.INFO, "linkcheck",
                "completed", mnem, name, "" + value.submitted, "" + value.ignored, time);

        context.getExecution().putValue("linkcheck.ignored", "" + value.ignored);
        context.getExecution().putValue("linkcheck.submitted", "" + value.submitted);

        Submission submission = WeblinkLinkchecker.getShared().getSubmission(context.getExecution());
        if (submission != null) {
            context.getExecution().putValue("linkcheck.processed", "" + submission.getProcessed());
        }

        try {
            if (collection != null) {

                collection.putValue(SugarControlledVocabulary.COLLECTION_LINK_VALIDATION,
                        "" + context.getExecution().getId());

                ((ActiveExecution<I>)context).getStorageEngine().updateCollection(collection);

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
        */
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




}
