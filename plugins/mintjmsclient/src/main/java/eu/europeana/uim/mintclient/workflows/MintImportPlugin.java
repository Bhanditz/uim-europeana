/*
 * Copyright 2007-2012 The Europeana Foundation
 *
 *  Licenced under the EUPL, Version 1.1 (the "Licence") and subsequent versions as approved
 *  by the European Commission;
 *  You may not use this work except in compliance with the Licence.
 * 
 *  You may obtain a copy of the Licence at:
 *  http://joinup.ec.europa.eu/software/page/eupl
 *
 *  Unless required by applicable law or agreed to in writing, software distributed under
 *  the Licence is distributed on an "AS IS" basis, without warranties or conditions of
 *  any kind, either express or implied.
 *  See the Licence for the specific language governing permissions and limitations under
 *  the Licence.
 */
package eu.europeana.uim.mintclient.workflows;

import java.util.ArrayList;
import java.util.List;

import eu.europeana.uim.api.AbstractIngestionPlugin;
import eu.europeana.uim.api.CorruptedMetadataRecordException;
import eu.europeana.uim.api.ExecutionContext;
import eu.europeana.uim.api.IngestionPluginFailedException;
import eu.europeana.uim.api.StorageEngineException;
import eu.europeana.uim.common.TKey;
import eu.europeana.uim.mintclient.service.MintUIMService;
import eu.europeana.uim.mintclient.service.exceptions.MintOSGIClientException;
import eu.europeana.uim.mintclient.service.exceptions.MintRemoteException;
import eu.europeana.uim.model.europeanaspecific.fieldvalues.EuropeanaDatasetStates;
import eu.europeana.uim.store.Collection;
import eu.europeana.uim.store.MetaDataRecord;
import eu.europeana.uim.store.Provider;
import eu.europeana.uim.sugarcrm.QueryResultException;
import eu.europeana.uim.sugarcrm.SugarCrmService;

/**
 *
 * @author Georgios Markakis <gwarkx@hotmail.com>
 * @since 3 Apr 2012
 */
public class MintImportPlugin extends AbstractIngestionPlugin{

	private static MintUIMService mintservice;
	private static SugarCrmService sugarservice;

	
	/** Property which allows to overwrite base url from collection/provider */
	public static final String fullingest = "repox.fullingest";
	
	/**
	 * The parameters used by this Plugin
	 */
	private static final List<String> params = new ArrayList<String>() {
		private static final long serialVersionUID = 1L;
		{
			add(fullingest);
		}
	};
	
	
	/**
	 * @param repoxservice
	 * @param sugarservice
	 * @param name
	 * @param description
	 */
	public MintImportPlugin(MintUIMService mintservice,
			SugarCrmService sugarservice,String name, String description) {
		super(name, description);
		MintImportPlugin.mintservice = mintservice;
		MintImportPlugin.sugarservice = sugarservice;
	}

	@Override
	public <I> void completed(ExecutionContext<I> context)
			throws IngestionPluginFailedException {
	
		Collection<I> collection = (Collection<I>) context.getDataSet();
		Provider<I> provider = collection.getProvider();
		
		try {
			
			mintservice.createMintOrganization(provider);
			
			//TODO:Commented out for tthe time being. Implement it when user management for Mint is ready
			//mintservice.createMintAuthorizedUser(provider);
			
			mintservice.createMappingSession(collection);
			
			String sugarID = collection.getValue("sugarCRMID");
			sugarservice.changeEntryStatus(sugarID, 
					EuropeanaDatasetStates.MAPPING_AND_NORMALIZATION);
			
		} catch (MintOSGIClientException e) {
			throw new IngestionPluginFailedException("Error while creating mapping in Mint",e);
		} catch (MintRemoteException e) {
			throw new IngestionPluginFailedException("Error while creating mapping in Mint",e);
		} catch (StorageEngineException e) {
			throw new IngestionPluginFailedException("Error while storing data in UIM",e);
		} catch (QueryResultException e) {
			throw new IngestionPluginFailedException("Error while updating data in SugarCRM",e);
		}
	}

	@Override
	public TKey<?, ?>[] getInputFields() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int getMaximumThreadCount() {
		return 10;
	}

	@Override
	public TKey<?, ?>[] getOptionalFields() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public TKey<?, ?>[] getOutputFields() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<String> getParameters() {
		return params;
	}

	@Override
	public int getPreferredThreadCount() {
		// TODO Auto-generated method stub
		return 5;
	}

	@Override
	public void initialize() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public <I> void initialize(ExecutionContext<I> arg0)
			throws IngestionPluginFailedException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public <I> boolean processRecord(MetaDataRecord<I> arg0,
			ExecutionContext<I> arg1) throws IngestionPluginFailedException,
			CorruptedMetadataRecordException {
		// Do Nothing
		return false;
	}

	@Override
	public void shutdown() {
		// TODO Auto-generated method stub
		
	}

}
