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
package eu.europeana.uim.sugarcrmclient.ws.quartz;

import java.util.List;

import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.scheduling.quartz.QuartzJobBean;

import eu.europeana.uim.sugarcrmclient.internal.helpers.ClientUtils;
import eu.europeana.uim.sugarcrmclient.jibxbindings.GetEntryListResponse;
import eu.europeana.uim.sugarcrmclient.plugin.SugarCRMServiceImpl;
import eu.europeana.uim.sugarcrmclient.plugin.SugarCRMService;
import eu.europeana.uim.sugarcrmclient.plugin.objects.SugarCrmRecord;
import eu.europeana.uim.sugarcrmclient.plugin.objects.data.RetrievableField;
import eu.europeana.uim.sugarcrmclient.plugin.objects.listeners.PollingListener;
import eu.europeana.uim.sugarcrmclient.plugin.objects.queries.SimpleSugarCrmQuery;
import eu.europeana.uim.sugarcrmclient.plugin.objects.queries.SugarCrmQuery;
import eu.europeana.uim.sugarcrmclient.ws.exceptions.GenericSugarCRMException;
import eu.europeana.uim.sugarcrmclient.ws.exceptions.QueryResultException;


/**
 * This Class implements the Quartz-based polling mechanism for sugarcrm
 * plugin.
 *   
 * @author georgiosmarkakis
 */
public class PollingBean extends QuartzJobBean {

	  private SugarCRMService sugarcrmPlugin;
	  
	  /**
	   * Setter for sugarcrmPlugin spring injected property
	   */ 
	  public void setSugarcrmPlugin(SugarCRMService sugarcrmPlugin) {
	    this.sugarcrmPlugin = sugarcrmPlugin;
	  }	
	
	
	/* (non-Javadoc)
	 * @see org.springframework.scheduling.quartz.QuartzJobBean#executeInternal(org.quartz.JobExecutionContext)
	 */
	@Override
	protected void executeInternal(JobExecutionContext arg0)
			throws JobExecutionException {



			List<PollingListener> pollingListeners = sugarcrmPlugin.getPollingListeners();
			
			if(!pollingListeners.isEmpty()){
				
				for(PollingListener listener:pollingListeners){
					
					SimpleSugarCrmQuery query = new SimpleSugarCrmQuery();
					
					query.setStatus(listener.getTrigger());
					query.setMaxResults(100);
					query.setOffset(0);
					query.setOrderBy(RetrievableField.DATE_ENTERED);
					try {
						
						List<SugarCrmRecord> results = sugarcrmPlugin.retrieveRecords(query);
						
						listener.performAction(sugarcrmPlugin, results);
						
					} catch (GenericSugarCRMException e) {

						e.printStackTrace();
					}
				}
			}
			
			



	}

}
