package eu.europeana.uim.deactivation;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

import org.apache.solr.client.solrj.SolrServerException;
import org.jibx.runtime.BindingDirectory;
import org.jibx.runtime.IBindingFactory;
import org.jibx.runtime.IUnmarshallingContext;
import org.jibx.runtime.JiBXException;
import org.theeuropeanlibrary.model.common.qualifier.Status;

import eu.europeana.corelib.definitions.jibx.RDF;
import eu.europeana.corelib.definitions.solr.beans.FullBean;
import eu.europeana.uim.common.TKey;
import eu.europeana.uim.deactivation.service.DeactivationService;
import eu.europeana.uim.model.europeana.EuropeanaModelRegistry;
import eu.europeana.uim.orchestration.ExecutionContext;
import eu.europeana.uim.plugin.ingestion.AbstractIngestionPlugin;
import eu.europeana.uim.plugin.ingestion.CorruptedDatasetException;
import eu.europeana.uim.plugin.ingestion.IngestionPluginFailedException;
import eu.europeana.uim.store.MetaDataRecord;

/**
 * Collection Deactivation Plugin
 * 
 * @author gmamakis
 * 
 */
public class DeactivatePlugin<I> extends AbstractIngestionPlugin<MetaDataRecord<I>,I> {


	private static DeactivationService dService;
	private static List<String> europeanaIds;
	private final static int DELETE_THRESHOLD = 1000;


	public DeactivatePlugin(String name,
			String description) {
		super(name, description);
	}
	
	public DeactivatePlugin(){
		super("","");
	}

	public DeactivatePlugin(DeactivationService dService, String name,
			String description) {
		super(name, description);
		dService.initialize();
		DeactivatePlugin.dService=dService;
		
	}
	
	
	
	public TKey<?, ?>[] getInputFields() {
		return null;
	}

	public int getMaximumThreadCount() {
		return 12;
	}

	public TKey<?, ?>[] getOptionalFields() {
		return null;
	}

	public TKey<?, ?>[] getOutputFields() {
		return null;
	}

	public List<String> getParameters() {
		return new ArrayList<String>();
	}

	public int getPreferredThreadCount() {
		return 10;
	}

	/* (non-Javadoc)
	 * @see eu.europeana.uim.plugin.Plugin#initialize()
	 */
	public void initialize() {
	}

	/* (non-Javadoc)
	 * @see eu.europeana.uim.plugin.ExecutionPlugin#initialize(eu.europeana.uim.orchestration.ExecutionContext)
	 */
	@Override
	public void initialize(
			ExecutionContext<MetaDataRecord<I>, I> arg0)
			throws IngestionPluginFailedException {
	
		europeanaIds = new ArrayList<String>();
		
	}
	
	/* (non-Javadoc)
	 * @see eu.europeana.uim.plugin.ingestion.IngestionPlugin#process(eu.europeana.uim.store.UimDataSet, eu.europeana.uim.orchestration.ExecutionContext)
	 */
	@Override
	public boolean process(
			MetaDataRecord<I> mdr,
			ExecutionContext<MetaDataRecord<I>, I> arg1)
			throws IngestionPluginFailedException,
			CorruptedDatasetException {
		IBindingFactory bfact;

		try {
			bfact = BindingDirectory.getFactory(RDF.class);

			IUnmarshallingContext uctx = bfact.createUnmarshallingContext();
			String value;
			if(mdr.getValues(EuropeanaModelRegistry.EDMDEREFERENCEDRECORD)!=null&&mdr.getValues(EuropeanaModelRegistry.EDMDEREFERENCEDRECORD).size()>0){
				value = mdr
						.getValues(EuropeanaModelRegistry.EDMDEREFERENCEDRECORD)
						.get(0);
			} else {
				value = mdr.getValues(EuropeanaModelRegistry.EDMRECORD)
						.get(0);
			}
			mdr.addValue(EuropeanaModelRegistry.STATUS, Status.DELETED);
			// TODO: disable in UIM
			RDF rdf = (RDF) uctx.unmarshalDocument(new StringReader(value));
			
			FullBean fBean = dService.getMongoServer().getFullBean(
					rdf.getProvidedCHOList().get(0).getAbout());
			europeanaIds.add(fBean.getAbout());
			dService.getMongoServer().delete(fBean.getAggregations());
			dService.getMongoServer().delete(fBean.getProvidedCHOs());
			dService.getMongoServer().delete(fBean.getProxies());
			dService.getMongoServer().delete(fBean.getEuropeanaAggregation());
			dService.getMongoServer().delete(fBean);
			if(europeanaIds.size()==DELETE_THRESHOLD){
				try {
					dService.getSolrServer().deleteById(europeanaIds);
				} catch (SolrServerException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				europeanaIds = new ArrayList<String>();
			}
			return true;
		} catch (JiBXException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return false;
	}
	
	
	
	
	/* (non-Javadoc)
	 * @see eu.europeana.uim.plugin.Plugin#shutdown()
	 */
	public void shutdown() {

	}



	/* (non-Javadoc)
	 * @see eu.europeana.uim.plugin.ExecutionPlugin#completed(eu.europeana.uim.orchestration.ExecutionContext)
	 */
	@Override
	public void completed(
			ExecutionContext<MetaDataRecord<I>, I> arg0)
			throws IngestionPluginFailedException {
	}

	public DeactivationService getdService() {
		return dService;
	}

	public void setdService(DeactivationService dService) {
		DeactivatePlugin.dService = dService;
	}
	
	

}
