package eu.europeana.uim.enrichment.utils;

import org.apache.commons.lang.StringUtils;
import org.osgi.framework.BundleActivator;

import com.google.code.morphia.Datastore;
import com.google.code.morphia.Morphia;
import com.google.code.morphia.mapping.DefaultCreator;
import com.mongodb.DBObject;
import com.mongodb.Mongo;

import eu.europeana.corelib.definitions.solr.beans.FullBean;
import eu.europeana.corelib.solr.bean.impl.FullBeanImpl;
import eu.europeana.corelib.solr.entity.AgentImpl;
import eu.europeana.corelib.solr.entity.AggregationImpl;
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
import eu.europeana.corelib.solr.exceptions.MongoDBException;
import eu.europeana.corelib.solr.server.impl.EdmMongoServerImpl;

public class OsgiEdmMongoServer extends EdmMongoServerImpl {
	private Mongo mongoServer;
	private String databaseName;
	private String username;
	private String password;
	private Datastore datastore;
	private Morphia morphia;
	
	public OsgiEdmMongoServer(Mongo mongoServer, String databaseName,
			String username, String password) throws MongoDBException {
		super();
		this.mongoServer = mongoServer;
		this.databaseName = databaseName;
		this.username = username;
		this.password = password;
		createDatastore();
		
	}

	
	private void createDatastore()
	{
		morphia = new Morphia();
//		morphia.getMapper().getOptions().setObjectFactory(new DefaultCreator() {
//            @Override
//            protected ClassLoader getClassLoaderForClass(String clazz, DBObject object) {
//            	return MongoBundleActivator.getClassLoader();
//            }
//        });
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

		datastore = morphia.createDatastore(mongoServer, databaseName);

		if (StringUtils.isNotBlank(this.username)
				&& StringUtils.isNotBlank(this.password)) {
			datastore.getDB().authenticate(this.username,
					this.password.toCharArray());
		}
		datastore.ensureIndexes();
	}
	
	@Override
	public synchronized <T> T searchByAbout(Class<T> clazz, String about) {

		return datastore.find(clazz).filter("about", about).get();
	}
	
	@Override
	public synchronized FullBean getFullBean(String id) {
		if (datastore.find(FullBeanImpl.class).field("about").equal(id).get() != null) {
			return datastore.find(FullBeanImpl.class).field("about").equal(id)
					.get();
		}
		return null;
	}

	@Override
	public Datastore getDatastore() {
		return this.datastore;
	}
}