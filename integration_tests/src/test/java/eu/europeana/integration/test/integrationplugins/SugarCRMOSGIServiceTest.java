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
package eu.europeana.integration.test.integrationplugins;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import java.util.HashMap;
import java.util.List;
import org.apache.log4j.Logger;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.ops4j.pax.exam.junit.JUnit4TestRunner;
import eu.europeana.uim.store.Collection;
import eu.europeana.uim.store.Provider;
import eu.europeana.uim.sugarcrm.ConnectionStatus;
import eu.europeana.uim.sugarcrm.SugarCrmService;
import eu.europeana.uim.sugarcrm.SugarCrmRecord;
import eu.europeana.uim.model.europeanaspecific.fieldvalues.EuropeanaDatasetStates;
import eu.europeana.uim.model.europeanaspecific.fieldvalues.EuropeanaRetrievableField;
import eu.europeana.uim.model.europeanaspecific.fieldvalues.EuropeanaUpdatableField;
import eu.europeana.uim.sugarcrmclient.plugin.objects.queries.ComplexSugarCrmQuery;
import eu.europeana.uim.sugarcrmclient.plugin.objects.queries.CustomSugarCrmQuery;
import eu.europeana.uim.sugarcrmclient.plugin.objects.queries.EqOp;
import eu.europeana.uim.sugarcrmclient.plugin.objects.queries.SimpleSugarCrmQuery;
import eu.europeana.uim.sugarcrm.model.UpdatableField;
import eu.europeana.uim.workflow.Workflow;

/**
 * Class implementing Unit Tests for UIM Repox Service
 * This is a PAX-EXAM OSGI based test. This practically means that tests are executed within
 * an OSGI Karaf container created by PAX-EXAM. Make sure that no other instance of Karaf is
 * running locally during the execution of the tests (otherwise the tests will fail)
 * 
 * @author Georgios Markakis (gwarkx@hotmail.com)
 * @since May 30, 2011
 */
@RunWith(JUnit4TestRunner.class)
public class SugarCRMOSGIServiceTest  extends AbstractEuropeanaIntegrationTest{

	
	private static org.apache.log4j.Logger LOGGER = Logger.getLogger(SugarCRMOSGIServiceTest.class);

	    
    /**
     * Tests the GetConnectionInfo functionality
     * @throws Exception
     */
    @Test
    public void testGetConnectionInfo() throws Exception{
        Thread.sleep(time2w84service);
        SugarCrmService service = getOsgiService(SugarCrmService.class);
		assertNotNull(service);
		ConnectionStatus status = service.showConnectionStatus();
		assertNotNull(status.getDefaultURI());
		assertNotNull(status.getSessionID());
		LOGGER.debug(status);   	
    }
	

    /**
     * Tests the Update Session functionality
     * 
     * @throws Exception
     */
    @Test
    public void testUpdateSession() throws Exception{
        Thread.sleep(time2w84service);
    	SugarCrmService service = getOsgiService(SugarCrmService.class);
    	ConnectionStatus statusbefore = service.showConnectionStatus();
    	String sessionIDbefore = statusbefore.getSessionID();	
    	String username = "test";
		String password = "test";		
		String sessionIDafter = service.updateSession(username, password);		
		assertNotNull(sessionIDbefore);
		assertNotNull(sessionIDafter);
		assertTrue(!sessionIDbefore.equals(sessionIDafter));
    }
    
    

    /**
     * Tests the execution of a Simple Query
     * 
     * @throws Exception
     */
    @Test
    public void testRetrieveRecordsSimpleQuery() throws Exception{
        Thread.sleep(time2w84service);
    	SugarCrmService service = getOsgiService(SugarCrmService.class);
    	
		EuropeanaDatasetStates status = EuropeanaDatasetStates.INGESTION_COMPLETE;
		SimpleSugarCrmQuery query =  new SimpleSugarCrmQuery(status);
		query.setMaxResults(1000);
		query.setOffset(0);
		query.setOrderBy(EuropeanaRetrievableField.DATE_ENTERED);
		List<SugarCrmRecord> records = service.retrieveRecords(query);
		LOGGER.debug("Number of Records retrieved: " + records.size());
		LOGGER.debug("NO | RECORD ID                          | COLLECTION NAME");

		for(int i=0; i< records.size(); i++){
			LOGGER.debug( (i+1) + " : " + records.get(i).getItemValue(EuropeanaRetrievableField.ID) + " | " +
					records.get(i).getItemValue(EuropeanaRetrievableField.NAME)	) ;
		}
    }
    
    
    
    /**
     * Tests the execution of a Complex Query
     * 
     * @throws Exception
     */
    @Test
    public void testRetrieveRecordsComplexQuery() throws Exception{
        Thread.sleep(time2w84service);
    	SugarCrmService service = getOsgiService(SugarCrmService.class);
		ComplexSugarCrmQuery query =  new ComplexSugarCrmQuery(EuropeanaRetrievableField.NAME ,EqOp.LIKE,"00101_M_PT_Gulbenkian_biblioteca_digital" );
		List<SugarCrmRecord> records = service.retrieveRecords(query);
		System.out.println("Number of Records retrieved: " + records.size());
		System.out.println("NO | RECORD ID                          | COLLECTION NAME");

		for(int i=0; i< records.size(); i++){
			LOGGER.debug( (i+1) + " : " + records.get(i).getItemValue(EuropeanaRetrievableField.ID) + " | " +
					records.get(i).getItemValue(EuropeanaRetrievableField.NAME)	) ;
		}
	}

    
    /**
     * Tests the execution of a Custom Query
     * 
     * @throws Exception
     */
    @Test
    public void testRetrieveRecordsCustomQuery() throws Exception{
        Thread.sleep(time2w84service);
    	SugarCrmService service = getOsgiService(SugarCrmService.class);
    	
		CustomSugarCrmQuery query =  new CustomSugarCrmQuery("opportunities.sales_stage LIKE 'Needs%Analysis'");
		
		List<SugarCrmRecord> records = service.retrieveRecords(query);
		LOGGER.debug("Number of Records retrieved: " + records.size());
		LOGGER.debug("NO | RECORD ID                          | COLLECTION NAME");

		for(int i=0; i< records.size(); i++){
			LOGGER.debug( (i+1) + " : " + records.get(i).getItemValue(EuropeanaRetrievableField.ID) + " | " +
					records.get(i).getItemValue(EuropeanaRetrievableField.NAME)	) ;
		}
	}
    
    

    /**
     * Tests the fetch Record functionality 
     * 
     * @throws Exception
     */
    @Test
    public void testfetchRecord() throws Exception{
        Thread.sleep(time2w84service);
    	String recId = "a2098f49-37db-2362-3e4b-4c5861d23639";
    	SugarCrmService service = getOsgiService(SugarCrmService.class);
    	
    	SugarCrmRecord rec = service.retrieveRecord(recId);
		assertNotNull(rec);
    }
    
    

    /**
     * Tests the Update Record functionality 
     * @throws Exception
     */
    @Test
    public void testupdateRecord() throws Exception{
        Thread.sleep(time2w84service);
    	SugarCrmService service = getOsgiService(SugarCrmService.class);
    	
		String recordID = "a2098f49-37db-2362-3e4b-4c5861d23639";
		String threcords = "100";
		String himages = "50";
		String htetx = "30";
		String hvideo = "10";
		String hsound = "10";
		
		HashMap<UpdatableField, String> values  = new HashMap<UpdatableField, String>();
		values.put(EuropeanaUpdatableField.TOTAL_INGESTED, threcords);
		values.put(EuropeanaUpdatableField.INGESTED_IMAGE, himages);
		values.put(EuropeanaUpdatableField.INGESTED_TEXT, htetx);			
		values.put(EuropeanaUpdatableField.INGESTED_VIDEO, hvideo);
		values.put(EuropeanaUpdatableField.INGESTED_SOUND, hsound);
		
		service.updateRecordData(recordID, values);
    }
    
    
    

    /**
     * Tests the Change Record Status functionality 
     * @throws Exception
     */
    @Test
    public void testChangeRecordStatus() throws Exception{
        Thread.sleep(time2w84service);
    	SugarCrmService service = getOsgiService(SugarCrmService.class);
    	
		String recordID = "a2098f49-37db-2362-3e4b-4c5861d23639";
		EuropeanaDatasetStates chstate = EuropeanaDatasetStates.INGESTION_COMPLETE; 
		service.changeEntryStatus(recordID, chstate);
    }
    

    
    
    /**
     * Tests the Populate UIM from Record functionality (automatically infer 
     * Providers and Collections from a record)
     * @throws Exception
     */
    @Test
    public void testPopulateUIMfromRecord() throws Exception{
        Thread.sleep(time2w84service);
    	SugarCrmService service = getOsgiService(SugarCrmService.class);
		SugarCrmRecord re = service.retrieveRecord("a2098f49-37db-2362-3e4b-4c5861d23639");
		
		Provider prov = service.updateProviderFromRecord(re);
		Collection coll = service.updateCollectionFromRecord(re, prov);
		
    }
    
    
    
    /**
     * Tests the Initialize Workflow from a SugarCRM Record with a specific ID functionality.
     * 
     * @throws Exception
     */
    @Test
    public void testInitWorkflowByID() throws Exception{
        Thread.sleep(time2w84service);
    	SugarCrmService service = getOsgiService(SugarCrmService.class);
    	
		String recordID = "a2098f49-37db-2362-3e4b-4c5861d23639";
		String worklfowName = "SysoutWorkflow";
		SugarCrmRecord record = service.retrieveRecord(recordID);
		assertNotNull(record);
		EuropeanaDatasetStates endstate = EuropeanaDatasetStates.HARVESTING_PENDING; 
		Workflow wf = service.initWorkflowFromRecord(worklfowName, record, endstate);
		assertNotNull(wf);
    }
    
    
    
    /**
     * Tests the Initialize multiple Workflows from many SugarCRM Records having the same state functionality.
     * @throws Exception
     */
    @Test
    public void testInitWorkflowsByState() throws Exception{
        Thread.sleep(time2w84service);
    	SugarCrmService service = getOsgiService(SugarCrmService.class);
    	
		String wfname = "SysoutWorkflow";
		EuropeanaDatasetStates currentstate = EuropeanaDatasetStates.HARVESTING_PENDING;
		EuropeanaDatasetStates ndstate = EuropeanaDatasetStates.INGESTION_COMPLETE; 
		List<Workflow> wfs = service.initWorkflowsFromRecords(wfname, currentstate, ndstate);

    }
    
    
    
    /**
     * Tests the Add Note Attachment to record functionality
     * @throws Exception
     */
    @Test
    public void testaddNoteAttachmentToRecord() throws Exception{
        Thread.sleep(time2w84service);
    	SugarCrmService service = getOsgiService(SugarCrmService.class);
		String recordID = "a2098f49-37db-2362-3e4b-4c5861d23639";
		String message = "Exception Stacktrace....";
    	service.addNoteAttachmentToRecord(recordID, message);
    }

    

    
    
}
