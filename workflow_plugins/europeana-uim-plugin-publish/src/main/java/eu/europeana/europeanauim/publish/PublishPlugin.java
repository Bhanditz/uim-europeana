package eu.europeana.europeanauim.publish;

import java.io.IOException;
import java.io.StringReader;
import java.net.MalformedURLException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.common.SolrInputDocument;
import org.jibx.runtime.IBindingFactory;
import org.jibx.runtime.IUnmarshallingContext;
import org.jibx.runtime.JiBXException;
import org.theeuropeanlibrary.model.common.qualifier.Status;

import eu.europeana.corelib.definitions.jibx.RDF;
import eu.europeana.corelib.edm.utils.MongoConstructor;
import eu.europeana.corelib.edm.utils.construct.SolrDocumentHandler;
import eu.europeana.corelib.solr.bean.impl.FullBeanImpl;
import eu.europeana.corelib.tools.lookuptable.EuropeanaId;
import eu.europeana.europeanauim.publish.service.PublishService;
import eu.europeana.uim.common.TKey;
import eu.europeana.uim.logging.LoggingEngine;
import eu.europeana.uim.model.europeana.EuropeanaModelRegistry;
import eu.europeana.uim.model.europeana.EuropeanaRedirectId;
import eu.europeana.uim.orchestration.ExecutionContext;
import eu.europeana.uim.plugin.ingestion.AbstractIngestionPlugin;
import eu.europeana.uim.plugin.ingestion.CorruptedDatasetException;
import eu.europeana.uim.plugin.ingestion.IngestionPluginFailedException;
import eu.europeana.uim.storage.StorageEngineException;
import eu.europeana.uim.store.Collection;
import eu.europeana.uim.store.MetaDataRecord;

public class PublishPlugin<I> extends AbstractIngestionPlugin<MetaDataRecord<I>, I> {

  private static PublishService publishService;
  private static IBindingFactory bfact;
  private static LoggingEngine logEngine;
  private static final Logger log = Logger.getLogger(PublishPlugin.class.getName());

  public PublishPlugin(String name, String description) {
    super(name, description);
    // TODO Auto-generated constructor stub
  }

  public PublishPlugin(PublishService publishService, String name, String description) {
    super(name, description);
    PublishPlugin.publishService = publishService;
  }

  public TKey<?, ?>[] getInputFields() {
    // TODO Auto-generated method stub
    return null;
  }

  public TKey<?, ?>[] getOptionalFields() {
    // TODO Auto-generated method stub
    return null;
  }

  public TKey<?, ?>[] getOutputFields() {
    // TODO Auto-generated method stub
    return null;
  }

  public boolean process(MetaDataRecord<I> mdr, ExecutionContext<MetaDataRecord<I>, I> context)
      throws IngestionPluginFailedException, CorruptedDatasetException {

    List<Status> status = mdr.getValues(EuropeanaModelRegistry.STATUS);

    if (!(status != null && status.get(0).equals(Status.DELETED))) {
      String value = null;
      if (mdr.getValues(EuropeanaModelRegistry.EDMENRICHEDRECORD) != null
          && mdr.getValues(EuropeanaModelRegistry.EDMENRICHEDRECORD).size() > 0) {
        value = mdr.getValues(EuropeanaModelRegistry.EDMENRICHEDRECORD).get(0);
      }
      else
        return false;
      IUnmarshallingContext uctx;
      try {
        uctx = bfact.createUnmarshallingContext();
        RDF rdf = (RDF) uctx.unmarshalDocument(new StringReader(value));

        FullBeanImpl fBean = new MongoConstructor().constructFullBean(rdf);
        SolrInputDocument doc =
            new SolrDocumentHandler(publishService.getSolrServer()).generate(fBean);
        // TODO Add CRF fields here

        publishService.getSolrServer().add(doc);

      } catch (JiBXException e) {
        logEngine.logFailed(context.getExecution(), Level.SEVERE, this, e, e.getMessage());
        log.log(Level.SEVERE, "JiBXException occured with error " + e.getMessage() + "\n");
        e.printStackTrace();
        return false;
      } catch (Exception e) {
        logEngine.logFailed(context.getExecution(), Level.SEVERE, this, e, e.getMessage());
        e.printStackTrace();
        log.log(Level.SEVERE, "Generic Exception occured with error " + e.getMessage() + "\n");
        return false;
      }

      // Check if a redirect exist and push it to production
      if (mdr.getValues(EuropeanaModelRegistry.EDMRECORDREDIRECT) != null
          && mdr.getValues(EuropeanaModelRegistry.EDMENRICHEDRECORD).size() > 0) {
        // Push redirects to production
        EuropeanaId europeanaId =
            publishService
                .getEuropeanaIdMongoServer()
                .retrieveEuropeanaIdFromNew(
                    mdr.getValues(EuropeanaModelRegistry.EDMRECORDREDIRECT).get(0).getNewId())
                .get(0);
        publishService.getEuropeanaIdMongoServerProduction().saveEuropeanaId(europeanaId);
        // Remove it from the mdr
        mdr.deleteValues(EuropeanaModelRegistry.EDMRECORDREDIRECT);
        try {
          context.getStorageEngine().updateMetaDataRecord(mdr);
        } catch (StorageEngineException e) {
          logEngine.logFailed(context.getExecution(), Level.SEVERE, this, e, e.getMessage());
          log.log(Level.SEVERE, "Storage Engine Exception occured with error " + e.getMessage()
              + "\nRetrying");
        }
      }
    } else // Delete records from production
    {
      String value = null;
      if (mdr.getValues(EuropeanaModelRegistry.EDMENRICHEDRECORD) != null
          && mdr.getValues(EuropeanaModelRegistry.EDMENRICHEDRECORD).size() > 0) {
        value = mdr.getValues(EuropeanaModelRegistry.EDMENRICHEDRECORD).get(0);
      } else {
        value = mdr.getValues(EuropeanaModelRegistry.EDMRECORD).get(0);
      }

      IUnmarshallingContext uctx;
      try {
        uctx = bfact.createUnmarshallingContext();
        RDF rdf = (RDF) uctx.unmarshalDocument(new StringReader(value));

        FullBeanImpl fBean = new MongoConstructor().constructFullBean(rdf);

        publishService.getSolrServer().deleteById(fBean.getId());

      } catch (JiBXException e) {
        logEngine.logFailed(context.getExecution(), Level.SEVERE, this, e, e.getMessage());
        log.log(Level.SEVERE, "JiBXException occured with error " + e.getMessage() + "\n");
        e.printStackTrace();
        return false;
      } catch (Exception e) {
        logEngine.logFailed(context.getExecution(), Level.SEVERE, this, e, e.getMessage());
        e.printStackTrace();
        log.log(Level.SEVERE, "Generic Exception occured with error " + e.getMessage() + "\n");
        return false;
      }
    }
    return true;
  }

  public void initialize(ExecutionContext<MetaDataRecord<I>, I> context)
      throws IngestionPluginFailedException {
    SolrServer solrServer = publishService.getSolrServer();
    logEngine = context.getLoggingEngine();
    try {
      solrServer.optimize();
    } catch (SolrServerException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    } catch (IOException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }

  }

  public void completed(ExecutionContext<MetaDataRecord<I>, I> context)
      throws IngestionPluginFailedException {
    // TODO Auto-generated method stub

  }

  public void initialize() {
    // TODO Auto-generated method stub

  }

  public void shutdown() {
    // TODO Auto-generated method stub

  }

  public List<String> getParameters() {
    // TODO Auto-generated method stub
    return null;
  }

  public int getPreferredThreadCount() {
    // TODO Auto-generated method stub
    return 1;
  }

  public int getMaximumThreadCount() {
    // TODO Auto-generated method stub
    return 1;
  }

}
