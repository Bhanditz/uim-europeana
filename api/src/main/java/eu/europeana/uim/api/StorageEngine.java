package eu.europeana.uim.api;

import java.util.List;

import eu.europeana.uim.FieldRegistry;
import eu.europeana.uim.MetaDataRecord;
import eu.europeana.uim.store.Collection;
import eu.europeana.uim.store.Execution;
import eu.europeana.uim.store.Provider;
import eu.europeana.uim.store.Request;


public interface StorageEngine {

	public String getIdentifier();
	public long size();
	

	Provider createProvider();
	void updateProvider(Provider provider) throws StorageEngineException;
	Provider getProvider(long id);
	Provider findProvider(String mnemonic);
	List<Provider> getProvider();

	Collection createCollection(Provider provider);
	void updateCollection(Collection collection) throws StorageEngineException;
	Collection getCollection(long id);
	Collection findCollection(String mnemonic);
	List<Collection> getCollections(Provider provider);

	Request createRequest(Collection collection);
	void updateRequest(Request request) throws StorageEngineException;
	List<Request> getRequests(Collection collection);

	MetaDataRecord<FieldRegistry> createMetaDataRecord(Request request);
	void updateMetaDataRecord(MetaDataRecord<FieldRegistry> record) throws StorageEngineException;
	
	Execution createExecution();
	void updateExecution(Execution execution) throws StorageEngineException;
	List<Execution> getExecutions();

	MetaDataRecord<FieldRegistry>[] getMetaDataRecords(long...ids);
	

	long[] getByRequest(Request request);
	long[] getByCollection(Collection collection);
	long[] getByProvider(Provider provider, boolean recursive);
	long[] getAllIds();

    int getTotalByRequest(Request request);
    int getTotalByCollection(Collection collection);
    int getTotalByProvider(Provider provider, boolean recursive);
    int getTotalForAllIds();

}
