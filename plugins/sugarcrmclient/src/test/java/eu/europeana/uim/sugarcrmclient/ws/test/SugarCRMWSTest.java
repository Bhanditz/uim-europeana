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
package eu.europeana.uim.sugarcrmclient.ws.test;

import static org.junit.Assert.*;
import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.w3c.dom.Element;

import eu.europeana.uim.sugar.GenericSugarCrmException;
import eu.europeana.uim.sugarcrmclient.ws.SugarWsClient;
import eu.europeana.uim.sugarcrmclient.ws.exceptions.JIXBFileAttachmentException;
import eu.europeana.uim.sugarcrmclient.ws.exceptions.JIXBQueryResultException;
import eu.europeana.uim.sugarcrmclient.jibxbindings.GetAvailableModules;
import eu.europeana.uim.sugarcrmclient.jibxbindings.GetAvailableModulesResponse;
import eu.europeana.uim.sugarcrmclient.jibxbindings.GetEntries;
import eu.europeana.uim.sugarcrmclient.jibxbindings.GetEntriesResponse;
import eu.europeana.uim.sugarcrmclient.jibxbindings.GetEntry;
import eu.europeana.uim.sugarcrmclient.jibxbindings.GetEntryList;
import eu.europeana.uim.sugarcrmclient.jibxbindings.GetEntryListResponse;
import eu.europeana.uim.sugarcrmclient.jibxbindings.GetEntryResponse;
import eu.europeana.uim.sugarcrmclient.jibxbindings.GetModuleFields;
import eu.europeana.uim.sugarcrmclient.jibxbindings.GetModuleFieldsResponse;
import eu.europeana.uim.sugarcrmclient.jibxbindings.GetRelationships;
import eu.europeana.uim.sugarcrmclient.jibxbindings.GetRelationshipsResponse;
import eu.europeana.uim.sugarcrmclient.jibxbindings.GetUserId;
import eu.europeana.uim.sugarcrmclient.jibxbindings.GetUserIdResponse;
import eu.europeana.uim.sugarcrmclient.jibxbindings.Login;
import eu.europeana.uim.sugarcrmclient.jibxbindings.Logout;
import eu.europeana.uim.sugarcrmclient.jibxbindings.LogoutResponse;
import eu.europeana.uim.sugarcrmclient.jibxbindings.ModuleList;
import eu.europeana.uim.sugarcrmclient.jibxbindings.NameValue;
import eu.europeana.uim.sugarcrmclient.jibxbindings.NameValueList;
import eu.europeana.uim.sugarcrmclient.jibxbindings.NoteAttachment;
import eu.europeana.uim.sugarcrmclient.jibxbindings.SelectFields;
import eu.europeana.uim.sugarcrmclient.jibxbindings.SetEntry;
import eu.europeana.uim.sugarcrmclient.jibxbindings.SetEntryResponse;
import eu.europeana.uim.sugarcrmclient.jibxbindings.SetNoteAttachment;
import eu.europeana.uim.sugarcrmclient.jibxbindings.SetNoteAttachmentResponse;
import eu.europeana.uim.sugarcrmclient.jibxbindings.UserAuth;
import eu.europeana.uim.sugarcrmclient.jibxbindings.LoginResponse;
import eu.europeana.uim.sugarcrmclient.jibxbindings.IsUserAdmin;
import eu.europeana.uim.sugarcrmclient.jibxbindings.IsUserAdminResponse;
import eu.europeana.uim.sugarcrmclient.internal.helpers.ClientUtils;
import org.apache.log4j.Logger;


/**
 * Class implementing Unit Tests for SugarWsClient
 * 
 * @author Georgios Markakis
 */

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations={"classpath:test-context.xml"})
public final class SugarCRMWSTest {

	@Resource
	private SugarWsClient sugarWsClient; 
		
	private static org.apache.log4j.Logger LOGGER = Logger.getLogger(SugarCRMWSTest.class);

	/**
	 * User Login Test (make sure that the user has been created in advance in in the configured SCRM installation)
	 */
	@Test
	public void testLogin() throws Exception{
		LoginResponse response;
		Login login = ClientUtils.createStandardLoginObject(sugarWsClient.getUsername(), sugarWsClient.getPassword());			
		ClientUtils.logMarshalledObject(login);
		response = sugarWsClient.login2(login);		
		assertNotNull(response);
		LOGGER.info(response.getReturn().getId());
		ClientUtils.logMarshalledObject(response);
	}
	
	
	/**
	 * Is User Admin Test: Checks if the user has admin rights.
	 * @throws GenericSugarCrmException 
	 */
	@Test
	public void testIsUserAdmin() throws GenericSugarCrmException{	
		IsUserAdmin user = new IsUserAdmin();		
		user.setSession(sugarWsClient.getSessionID());
		IsUserAdminResponse response;
		response = sugarWsClient.isuseradmin(user);
		assertNotNull(response);
		ClientUtils.logMarshalledObject(response);
	}
	
	
	/**
	 * Get User ID Test: Retrieves a user id for a session.
	 * @throws GenericSugarCrmException 
	 */
	@Test
	public void testGetUserID() throws GenericSugarCrmException{	 
		GetUserId request = new GetUserId();
		request.setSession(sugarWsClient.getSessionID());
		ClientUtils.logMarshalledObject(request);
		GetUserIdResponse response;
		response = sugarWsClient.getuserid(request);
		assertNotNull(response);
		ClientUtils.logMarshalledObject(response);
	}
	
	
	/**
	 * Get Available Modules: Gets the available modules for a specific SugarCRM installation.
	 * @throws JIXBQueryResultException 
	 */
	@Test
	public void testGetAvailableModules() throws JIXBQueryResultException{	 	
		GetAvailableModules request = new GetAvailableModules();
		request.setSession(sugarWsClient.getSessionID());
		ClientUtils.logMarshalledObject(request);
		GetAvailableModulesResponse response;
		response = sugarWsClient.getavailablemodules(request);
		assertNotNull(response);		
		ClientUtils.logMarshalledObject(response);
		
		ModuleList mlist = response.getReturn();		
		assertNotNull(mlist);
		List<Element> elist = mlist.getModules().getArray().getAnyList();
		
		assertNotNull(elist);
		
		ClientUtils.logMarshalledObject(response);
	}

	
	
	/**
	 * Get Module Fields Test: Get the fields for a specific modules.
	 * @throws Exception 
	 */
	@Test
	public void testGetModuleFields() throws Exception{	 		
		GetModuleFields request = new GetModuleFields();
		request.setSession(sugarWsClient.getSessionID());
		request.setModuleName("Accounts");
		ClientUtils.logMarshalledObject(request);
		GetModuleFieldsResponse response;
		response = sugarWsClient.getmodulefields(request);
		assertNotNull(response);		
		ClientUtils.logMarshalledObject(response);
	}
	
	
	
	
	/**
	 * Get Entry List Test: Retrieves all fields from a specific module that match a 
	 * specific query String. 
	 */
	@Test
	public void testGetEntryList(){		
		GetEntryList request = new GetEntryList();
		
		ArrayList <String> fieldnames = new  ArrayList<String>();
		
		fieldnames.add("id");
		fieldnames.add("first_name");
		fieldnames.add("last_name");
		fieldnames.add("salutation");
		
		SelectFields fields = ClientUtils.generatePopulatedSelectFields(fieldnames);
		
  		request.setModuleName("Contacts");	
		request.setSelectFields(fields);
		request.setSession(sugarWsClient.getSessionID());
		request.setOrderBy("last_name");
		request.setMaxResults(10);
		request.setOffset(0);
		request.setQuery("(contacts.first_name LIKE '%M%')");
		
		ClientUtils.logMarshalledObject(request);
		GetEntryListResponse response;
		try {
			response = sugarWsClient.getentrylist(request);
			assertNotNull(response);
			ClientUtils.logMarshalledObject(response);
		} catch (JIXBQueryResultException e) {
			e.printStackTrace();
		}

		
	}
	
	
	/**
	 * Get Entry Test: Retrieves all fields from a specific module. 
	 * @throws JIXBQueryResultException 
	 */
	@Test
	public void testGetEntries() throws JIXBQueryResultException{	 		
		GetEntries request = new GetEntries();
		ArrayList <String> fieldnames = new  ArrayList<String>();
		fieldnames.add("id");
		fieldnames.add("first_name");
		fieldnames.add("last_name");
		fieldnames.add("salutation");
		SelectFields fields = ClientUtils.generatePopulatedSelectFields(fieldnames);
  		request.setModuleName("Contacts");	
		request.setSelectFields(fields);
		request.setSession(sugarWsClient.getSessionID());
		request.setIds(fields);
		ClientUtils.logMarshalledObject(request);
		GetEntriesResponse response;
		response = sugarWsClient.getentries(request);
		ClientUtils.logMarshalledObject(response);
	}
	
	
	
	
	
	
	/**
	 * Get Entry Test: Get a test entry for a specific ID.
	 * @throws JIXBQueryResultException 
	 */
	@Test
	public void testGetEntry() throws JIXBQueryResultException{
		GetEntry request = new GetEntry();
		request.setId("ca410eea-d4fb-0829-aa25-4c585fbb1136");
		request.setModuleName("Accounts");
		request.setSession(sugarWsClient.getSessionID());	
		SelectFields selectFields = new SelectFields();
		request.setSelectFields(selectFields );
		ClientUtils.logMarshalledObject(request);
		GetEntryResponse response;
		response = sugarWsClient.getentry(request);
		ClientUtils.logMarshalledObject(response);
	}
	
	
	
	/**
	 * Set Entry Test: Create an entry with the specified ID and the declared 
	 * name-value pairs and update it if it already exists. 
	 * @throws JIXBQueryResultException 
	 */
	@Test
	public void testSetEntry() throws JIXBQueryResultException{
		SetEntry request = new SetEntry();
		
		NameValue nv1 = new NameValue();
		nv1.setName("id");
		nv1.setValue("ac3f140f-ef12-ffab-6c97-4df512d4a9d8");
		
		NameValue nv0 = new NameValue();
		nv0.setName("first_name");
		nv0.setValue("JohnZXs");

		NameValue nv2 = new NameValue();
		nv2.setName("last_name");
		nv2.setValue("SmithZ");		

		ArrayList <NameValue> nvList = new  ArrayList <NameValue>();
		
		nvList.add(nv0);
		nvList.add(nv2);
		
		NameValueList valueList = ClientUtils.generatePopulatedNameValueList(nvList);
		
		request.setNameValueList(valueList);
		request.setModuleName("Contacts");
		request.setSession(sugarWsClient.getSessionID());	
		ClientUtils.logMarshalledObject(request);
		SetEntryResponse response;
		response = sugarWsClient.setentry(request);
		ClientUtils.logMarshalledObject(response);
	}
	
	/**
	 * Set Attachment Test: Create an attachment for a specific entry 
	 * @throws JIXBFileAttachmentException 
	 * 
	 */
	@Test
	public void testSetAttachment() throws JIXBFileAttachmentException{
		
		SetNoteAttachment request = new SetNoteAttachment();
	    NoteAttachment note = new NoteAttachment();
	    
	    note.setId("ac3f140f-ef12-ffab-6c97-4df512d4a9d8");
	    note.setFile("asdasdsadsadsadsad");
	    note.setFilename("test.txt");
	    
		request.setNote(note);
		request.setSession(sugarWsClient.getSessionID());
		ClientUtils.logMarshalledObject(request);
		SetNoteAttachmentResponse resp;
		resp = sugarWsClient.setnoteattachment(request );
		ClientUtils.logMarshalledObject(resp);

	}
	
	
	
	
	/**
	 * Get Relationships test: Gets the Related Organizations modules for a specific Dataset Record
	 * @throws Exception
	 */
	@Test
	public void testGetRelationships() throws Exception{
		
		GetRelationships request =  new GetRelationships();
		
		request.setDeleted(0);
		request.setModuleId("49357412-f6b0-9a19-78f0-4cffa2f08cd8");
		request.setModuleName("Opportunities");
		request.setRelatedModule("Accounts");
		request.setRelatedModuleQuery("");
		request.setSession(sugarWsClient.getSessionID());
		
		ClientUtils.logMarshalledObject(request);
		
		GetRelationshipsResponse resp = sugarWsClient.getrelationships(request);
		
		ClientUtils.logMarshalledObject(resp);
		
	}
	
	

		
	}

