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
package eu.europeana.uim.sugarcrmclient.ws;


import eu.europeana.uim.sugarcrm.GenericSugarCrmException;
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
import eu.europeana.uim.sugarcrmclient.jibxbindings.GetNoteAttachment;
import eu.europeana.uim.sugarcrmclient.jibxbindings.GetNoteAttachmentResponse;
import eu.europeana.uim.sugarcrmclient.jibxbindings.GetRelationships;
import eu.europeana.uim.sugarcrmclient.jibxbindings.GetRelationshipsResponse;
import eu.europeana.uim.sugarcrmclient.jibxbindings.GetUserId;
import eu.europeana.uim.sugarcrmclient.jibxbindings.GetUserIdResponse;
import eu.europeana.uim.sugarcrmclient.jibxbindings.IsUserAdmin;
import eu.europeana.uim.sugarcrmclient.jibxbindings.IsUserAdminResponse;
import eu.europeana.uim.sugarcrmclient.jibxbindings.Login;
import eu.europeana.uim.sugarcrmclient.jibxbindings.LoginResponse;
import eu.europeana.uim.sugarcrmclient.jibxbindings.Logout;
import eu.europeana.uim.sugarcrmclient.jibxbindings.LogoutResponse;
import eu.europeana.uim.sugarcrmclient.jibxbindings.SetEntry;
import eu.europeana.uim.sugarcrmclient.jibxbindings.SetEntryResponse;
import eu.europeana.uim.sugarcrmclient.jibxbindings.SetNoteAttachment;
import eu.europeana.uim.sugarcrmclient.jibxbindings.SetNoteAttachmentResponse;
import eu.europeana.uim.sugarcrmclient.ws.exceptions.JIXBFileAttachmentException;
import eu.europeana.uim.sugarcrmclient.ws.exceptions.JIXBLoginFailureException;
import eu.europeana.uim.sugarcrmclient.ws.exceptions.JIXBLogoutFailureException;
import eu.europeana.uim.sugarcrmclient.ws.exceptions.JIXBQueryResultException;

/**
 *  Interface describing the available SOAP based sugarCRM operations 
 *  
 * @author Georgios Markakis
 */
public interface SugarWsClient {


	/**
	 * Public method for performing Login operations (see Junit test for usage example) 
	 * 
	 * @param login the Login object
	 * @return a String containing the current Session id
	 * @throws JIXBLoginFailureException when login credentials are incorrect
	 * @throws GenericSugarCrmException 
	 */
	public String login(Login login) throws JIXBLoginFailureException;
	
	
	/**
	 * Public method for performing Login operations (see JUnit test for usage example) 
	 * 
	 * @param login
	 * @return a LoginResponse object
	 * @throws JIXBLoginFailureException when login credentials are incorrect
	 * @throws GenericSugarCrmException
	 */
	public LoginResponse login2(Login login) throws JIXBLoginFailureException;
	
	
	/**
	 * Public method for performing Logout operations (see Junit test for usage example) 
	 * 
	 * @param a logout request object
	 * @return a LogoutResponse object
	 * @throws JIXBLogoutFailureException when logout fails
	 * @throws GenericSugarCrmException
	 */
	public LogoutResponse logout(Logout request) throws JIXBLogoutFailureException;
	
	/**
	 * This method returns an object indicating that the current user has admin privileges or not.
	 * @param request
	 * @return
	 * @throws GenericSugarCrmException 
	 */
	public IsUserAdminResponse is_user_admin(IsUserAdmin request) throws GenericSugarCrmException;
	
	/**
	 * This method gives the user name of the user who "owns" the specific session 
	 * 
	 * @param request
	 * @return
	 * @throws GenericSugarCrmException 
	 */
	public GetUserIdResponse get_user_id(GetUserId request) throws GenericSugarCrmException;
	
	/**
	 * Shows all the available module names in SugarCRM 
	 * 
	 * @param request
	 * @return
	 * @throws JIXBQueryResultException 
	 * @throws GenericSugarCrmException 
	 */
	public GetAvailableModulesResponse get_available_modules(GetAvailableModules request) throws JIXBQueryResultException ;
	
	/**
	 * Get the fields for a specific module 
	 * 
	 * @param request 
	 * @return a GetModuleFieldsResponse containing a list of module fields
	 * @throws JIXBQueryResultException 
	 */
	public GetModuleFieldsResponse get_module_fields(GetModuleFields request) throws JIXBQueryResultException;
	
	/**
	 * Performs a query on the records contained in sugarCRM
	 *  
	 * @param request
	 * @return
	 * @throws JIXBQueryResultException 
	 */
	public GetEntryListResponse get_entry_list(GetEntryList request) throws JIXBQueryResultException;
	
	/**
	 * Gets a specific entry
	 * @param request
	 * @return
	 * @throws JIXBQueryResultException 
	 */
	public GetEntryResponse get_entry(GetEntry request) throws JIXBQueryResultException;
	
	/**
	 * Creates/Updates an entry in SugarCRM
	 * 
	 * @param request
	 * @return 
	 * @throws JIXBQueryResultException 
	 */
	public SetEntryResponse set_entry(SetEntry request) throws JIXBQueryResultException;
	
	/**
	 * Gets the entries for a request
	 * 
	 * @param request
	 * @return
	 * @throws JIXBQueryResultException 
	 */
	public GetEntriesResponse get_entries(GetEntries request) throws JIXBQueryResultException;
	
	
	/**
	 * Sets a note attachment to a record
	 * 
	 * @param request
	 * @return
	 * @throws JIXBFileAttachmentException 
	 */
	public SetNoteAttachmentResponse set_note_attachment(SetNoteAttachment request) throws JIXBFileAttachmentException;
	
	/**
	 * Gets a note attachment from a record
	 * 
	 * @param request
	 * @return
	 * @throws JIXBFileAttachmentException 
	 */
	public GetNoteAttachmentResponse get_note_attachment(GetNoteAttachment request) throws JIXBFileAttachmentException;
	
	/**
	 * Gets the Relationships for a specific module
	 * @param request
	 * @return
	 * @throws JIXBQueryResultException
	 */
	public GetRelationshipsResponse get_relationships(GetRelationships request) throws JIXBQueryResultException;
	
}
