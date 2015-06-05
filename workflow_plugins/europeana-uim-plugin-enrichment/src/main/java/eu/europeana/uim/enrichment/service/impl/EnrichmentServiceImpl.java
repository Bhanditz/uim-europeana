/*
 * Copyright 2007-2012 The Europeana Foundation
 * 
 * Licenced under the EUPL, Version 1.1 (the "Licence") and subsequent versions as approved by the
 * European Commission; You may not use this work except in compliance with the Licence.
 * 
 * You may obtain a copy of the Licence at: http://joinup.ec.europa.eu/software/page/eupl
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the Licence
 * is distributed on an "AS IS" basis, without warranties or conditions of any kind, either express
 * or implied. See the Licence for the specific language governing permissions and limitations under
 * the Licence.
 */
package eu.europeana.uim.enrichment.service.impl;

import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

import org.apache.solr.client.solrj.impl.CloudSolrServer;
import org.apache.solr.client.solrj.impl.HttpSolrServer;
import org.apache.solr.client.solrj.impl.LBHttpSolrServer;

import com.google.code.morphia.Datastore;
import com.google.code.morphia.Morphia;
import com.mongodb.Mongo;
import com.mongodb.MongoException;
import com.mongodb.ServerAddress;

import eu.europeana.corelib.edm.exceptions.MongoDBException;
import eu.europeana.corelib.lookup.impl.CollectionMongoServerImpl;
import eu.europeana.corelib.solr.bean.impl.FullBeanImpl;
import eu.europeana.corelib.solr.entity.AgentImpl;
import eu.europeana.corelib.solr.entity.AggregationImpl;
import eu.europeana.corelib.solr.entity.BasicProxyImpl;
import eu.europeana.corelib.solr.entity.ConceptImpl;
import eu.europeana.corelib.solr.entity.ConceptSchemeImpl;
import eu.europeana.corelib.solr.entity.EuropeanaAggregationImpl;
import eu.europeana.corelib.solr.entity.EventImpl;
import eu.europeana.corelib.solr.entity.PhysicalThingImpl;
import eu.europeana.corelib.solr.entity.PlaceImpl;
import eu.europeana.corelib.solr.entity.ProvidedCHOImpl;
import eu.europeana.corelib.solr.entity.ProxyImpl;
import eu.europeana.corelib.solr.entity.TimespanImpl;
import eu.europeana.corelib.solr.entity.WebResourceImpl;
import eu.europeana.corelib.tools.lookuptable.Collection;
import eu.europeana.corelib.tools.lookuptable.CollectionMongoServer;
import eu.europeana.corelib.tools.lookuptable.EuropeanaIdMongoServer;
import eu.europeana.uim.common.BlockingInitializer;
import eu.europeana.uim.enrichment.service.EnrichmentService;
import eu.europeana.uim.enrichment.utils.OsgiEdmMongoServer;
import eu.europeana.uim.enrichment.utils.OsgiEuropeanaIdMongoServer;
import eu.europeana.uim.enrichment.utils.PropertyReader;
import eu.europeana.uim.enrichment.utils.UimConfigurationProperty;

/**
 * See EnrichmentService
 * 
 * @author Yorgos.Mamakis@ kb.nl
 * 
 */
public class EnrichmentServiceImpl implements EnrichmentService {

  private static HttpSolrServer solrServer;
  private static HttpSolrServer solrProductionServer;
  private static CloudSolrServer cloudSolrServer;
  private static CloudSolrServer cloudSolrProductionServer;
  private static String mongoDB = PropertyReader
      .getProperty(UimConfigurationProperty.MONGO_DB_EUROPEANA);
  private static String[] mongoHost = PropertyReader.getProperty(
      UimConfigurationProperty.MONGO_HOSTURL).split(",");
  private static String mongoPort = PropertyReader
      .getProperty(UimConfigurationProperty.MONGO_HOSTPORT);
  private static String zookeeperUrl = PropertyReader
      .getProperty(UimConfigurationProperty.ZOOKEEPER_HOSTURL);
  // private static String solrUrl =
  // PropertyReader.getProperty(UimConfigurationProperty.SOLR_HOSTURL);
  // private static String solrProductionUrl = PropertyReader
  // .getProperty(UimConfigurationProperty.SOLR_PRODUCTION_HOSTURL);
  private static String solrCore = PropertyReader.getProperty(UimConfigurationProperty.SOLR_CORE);

  private static String[] cloudSolrUrl = PropertyReader.getProperty(
      UimConfigurationProperty.CLOUD_SOLR_HOSTURL).split(",");
  private static String cloudSolrCore = PropertyReader
      .getProperty(UimConfigurationProperty.CLOUD_SOLR_CORE);

  private static CollectionMongoServer cmServer;
  private static OsgiEuropeanaIdMongoServer idserver;
  private static OsgiEdmMongoServer server;


  public EnrichmentServiceImpl() {
    
    try {

      LBHttpSolrServer lbTarget = new LBHttpSolrServer(cloudSolrUrl);
      cloudSolrServer = new CloudSolrServer(zookeeperUrl, lbTarget);
      cloudSolrServer.setDefaultCollection(cloudSolrCore);
      cloudSolrServer.connect();
      
      cloudSolrProductionServer = new CloudSolrServer(zookeeperUrl, lbTarget);
      cloudSolrProductionServer.setDefaultCollection(cloudSolrCore);
      cloudSolrProductionServer.connect();
      
//      solrServer = new HttpSolrServer(new URL(solrUrl) + solrCore);
//      solrProductionServer = new HttpSolrServer(new URL(solrProductionUrl) + solrCore);
    } catch (Exception e) {
      e.printStackTrace();
    }



    BlockingInitializer initializer = new BlockingInitializer() {

      @Override
      protected void initializeInternal() {      
        try {
          Morphia morphia = new Morphia();
          morphia.map(Collection.class);
          List<ServerAddress> addresses = new ArrayList<>();
          for (String mongoStr : mongoHost) {
            ServerAddress address = new ServerAddress(mongoStr, 27017);
            addresses.add(address);
        }
        Mongo tgtMongo = new Mongo(addresses);
          Datastore datastore =
              morphia.createDatastore(tgtMongo,
                  "collections");
          cmServer = new CollectionMongoServerImpl();
          datastore.ensureIndexes();
          cmServer.setDatastore(datastore);
          BlockingInitializer colInitializer = new BlockingInitializer() {

            @Override
            protected void initializeInternal() {
              Collection col = new Collection();
              cmServer.findOldCollectionId("test");
            }
          };
          colInitializer.initialize(Collection.class.getClassLoader());
        } catch (NumberFormatException e) {
          // TODO Auto-generated catch block
          e.printStackTrace();
        } catch (UnknownHostException e) {
          // TODO Auto-generated catch block
          e.printStackTrace();
        } catch (MongoException e) {
          // TODO Auto-generated catch block
          e.printStackTrace();
        }

      }
    };

    initializer.initialize(CollectionMongoServerImpl.class.getClassLoader());



    try {

      BlockingInitializer mongoInitializer = new BlockingInitializer() {

        @Override
        protected void initializeInternal() {
          // TODO Auto-generated method stub
          String uname =
              PropertyReader.getProperty(UimConfigurationProperty.MONGO_USERNAME) != null ? PropertyReader
                  .getProperty(UimConfigurationProperty.MONGO_USERNAME) : "";
          String pass =
              PropertyReader.getProperty(UimConfigurationProperty.MONGO_PASSWORD) != null ? PropertyReader
                  .getProperty(UimConfigurationProperty.MONGO_PASSWORD) : "";
          Morphia morphia = new Morphia();

          morphia.map(FullBeanImpl.class);
          morphia.map(ProvidedCHOImpl.class);
          morphia.map(AgentImpl.class);
          morphia.map(AggregationImpl.class);
          morphia.map(ConceptImpl.class);
          morphia.map(ProxyImpl.class);
          morphia.map(PlaceImpl.class);
          morphia.map(TimespanImpl.class);
          morphia.map(WebResourceImpl.class);
          morphia.map(EuropeanaAggregationImpl.class);
          morphia.map(EventImpl.class);
          morphia.map(PhysicalThingImpl.class);
          morphia.map(ConceptSchemeImpl.class);
          morphia.map(BasicProxyImpl.class);
          try {
            List<ServerAddress> addresses = new ArrayList<>();
            for (String mongoStr : mongoHost) {
              ServerAddress address = new ServerAddress(mongoStr, 27017);
              addresses.add(address);
          }
          Mongo tgtMongo = new Mongo(addresses);
            server =
                new OsgiEdmMongoServer(tgtMongo,
                    "europeana", uname, pass);
          } catch (NumberFormatException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
          } catch (MongoDBException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
          } catch (UnknownHostException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
          } catch (MongoException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
          }
          server.createDatastore(morphia);
        }
      };
      mongoInitializer.initialize(OsgiEdmMongoServer.class.getClassLoader());



      List<ServerAddress> addresses = new ArrayList<>();
      for (String mongoStr : mongoHost) {
        ServerAddress address = new ServerAddress(mongoStr, 27017);
        addresses.add(address);
    }
    Mongo tgtMongo = new Mongo(addresses);
      idserver =
          new OsgiEuropeanaIdMongoServer((tgtMongo),
              "EuropeanaId");
      idserver.createDatastore();

    } catch (NumberFormatException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    } catch (UnknownHostException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    } catch (MongoException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }

  }

  // @Override
  // public HttpSolrServer getSolrServer() {
  // return solrServer;
  // }

  @Override
  public CloudSolrServer getCloudSolrServer() {
    return cloudSolrServer;
  }

  // @Override
  // public HttpSolrServer getProductionSolrServer() {
  // return solrProductionServer;
  // }

  @Override
  public CloudSolrServer getProductionCloudSolrServer() {
    return cloudSolrProductionServer;
  }

  @Override
  public String getMongoDB() {
    return mongoDB;
  }

  public void setMongoDB(String mongoDB) {
    EnrichmentServiceImpl.mongoDB = mongoDB;
  }

  public String[] getMongoHost() {
    return mongoHost;
  }

  public void setMongoHost(String[] mongoHost) {
    EnrichmentServiceImpl.mongoHost = mongoHost;
  }

  public String getMongoPort() {
    return mongoPort;
  }

  public void setMongoPort(String mongoPort) {
    EnrichmentServiceImpl.mongoPort = mongoPort;
  }

  // public String getSolrUrl() {
  // return solrUrl;
  // }

  // public void setSolrUrl(String solrUrl) {
  // EnrichmentServiceImpl.solrUrl = solrUrl;
  // }

  public String getSolrCore() {
    return solrCore;
  }

  public void setSolrCore(String solrCore) {
    EnrichmentServiceImpl.solrCore = solrCore;
  }

  public void setSolrServer(HttpSolrServer solrServer) {
    EnrichmentServiceImpl.solrServer = solrServer;
  }

  @Override
  public CollectionMongoServer getCollectionMongoServer() {
    return cmServer;
  }

  @Override
  public EuropeanaIdMongoServer getEuropeanaIdMongoServer() {
    return idserver;
  }

  @Override
  public OsgiEdmMongoServer getEuropeanaMongoServer() {
    // TODO Auto-generated method stub
    return server;
  }

}
