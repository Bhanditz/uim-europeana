package eu.europeana.uim.store.memory;

import java.util.ArrayList;
import java.util.List;

import eu.europeana.uim.store.Provider;

public class MemoryProvider extends AbstractMemoryEntity implements Provider {

	private List<Provider> relatedOut = new ArrayList<Provider>();
	private List<Provider> relatedIn = new ArrayList<Provider>();
	
	private String name;
	
	private String oaiBaseUrl;
	private String oaiPrefix;
	
	/**
	 * @return the oaiPrefix
	 */
	public String getOaiPrefix() {
		return oaiPrefix;
	}

	/**
	 * @param oaiPrefix the oaiPrefix to set
	 */
	public void setOaiPrefix(String oaiPrefix) {
		this.oaiPrefix = oaiPrefix;
	}

	private boolean aggregator;
	
	public MemoryProvider() {
		super();
	}

	public MemoryProvider(long id) {
		super(id);
	}
	
	
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	
	
	
	
	/**
	 * @return the oaiBaseUrl
	 */
	public String getOaiBaseUrl() {
		return oaiBaseUrl;
	}

	/**
	 * @param oaiBaseUrl the oaiBaseUrl to set
	 */
	public void setOaiBaseUrl(String oaiBaseUrl) {
		this.oaiBaseUrl = oaiBaseUrl;
	}

	/**
	 * @return the aggregator
	 */
	public boolean isAggregator() {
		return aggregator;
	}

	/**
	 * @param aggregator the aggregator to set
	 */
	public void setAggregator(boolean aggregator) {
		this.aggregator = aggregator;
	}

	/**
	 * @return the providerOut
	 */
	public List<Provider> getRelatedOut() {
		return relatedOut;
	}

	/**
	 * @return the providerIn
	 */
	public List<Provider> getRelatedIn() {
		return relatedIn;
	}


	
	

}
