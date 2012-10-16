package eu.europeana.uim.enrichment;

import java.io.IOException;
import java.io.StringReader;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.lang.StringUtils;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.CommonsHttpSolrServer;
import org.apache.solr.common.SolrException;
import org.apache.solr.common.SolrInputDocument;
import org.jibx.runtime.BindingDirectory;
import org.jibx.runtime.IBindingFactory;
import org.jibx.runtime.IUnmarshallingContext;
import org.jibx.runtime.JiBXException;

import com.google.code.morphia.Morphia;
import com.mongodb.Mongo;

import eu.annocultor.converters.europeana.Entity;
import eu.annocultor.converters.europeana.Field;
import eu.europeana.corelib.definitions.jibx.AgentType;
import eu.europeana.corelib.definitions.jibx.AggregatedCHO;
import eu.europeana.corelib.definitions.jibx.Concept;
import eu.europeana.corelib.definitions.jibx.Country;
import eu.europeana.corelib.definitions.jibx.Creator;
import eu.europeana.corelib.definitions.jibx.EuropeanaAggregationType;
import eu.europeana.corelib.definitions.jibx.HasMet;
import eu.europeana.corelib.definitions.jibx.LandingPage;
import eu.europeana.corelib.definitions.jibx.Language1;
import eu.europeana.corelib.definitions.jibx.LiteralType;
import eu.europeana.corelib.definitions.jibx.PlaceType;
import eu.europeana.corelib.definitions.jibx.ProvidedCHOType;
import eu.europeana.corelib.definitions.jibx.ProxyFor;
import eu.europeana.corelib.definitions.jibx.ProxyIn;
import eu.europeana.corelib.definitions.jibx.ProxyType;
import eu.europeana.corelib.definitions.jibx.RDF;
import eu.europeana.corelib.definitions.jibx.RDF.Choice;
import eu.europeana.corelib.definitions.jibx.ResourceOrLiteralType;
import eu.europeana.corelib.definitions.jibx.ResourceOrLiteralType.Lang;
import eu.europeana.corelib.definitions.jibx.ResourceType;
import eu.europeana.corelib.definitions.jibx.Rights1;
import eu.europeana.corelib.definitions.jibx.TimeSpanType;
import eu.europeana.corelib.definitions.model.EdmLabel;
import eu.europeana.corelib.definitions.solr.beans.FullBean;
import eu.europeana.corelib.dereference.impl.RdfMethod;
import eu.europeana.corelib.solr.bean.impl.FullBeanImpl;
import eu.europeana.corelib.solr.utils.EseEdmMap;
import eu.europeana.corelib.solr.utils.MongoConstructor;
import eu.europeana.corelib.solr.utils.SolrConstructor;
import eu.europeana.corelib.tools.lookuptable.EuropeanaId;
import eu.europeana.corelib.tools.lookuptable.EuropeanaIdMongoServer;
import eu.europeana.corelib.tools.utils.HashUtils;
import eu.europeana.corelib.tools.utils.PreSipCreatorUtils;
import eu.europeana.corelib.tools.utils.SipCreatorUtils;
import eu.europeana.uim.api.AbstractIngestionPlugin;
import eu.europeana.uim.api.CorruptedMetadataRecordException;
import eu.europeana.uim.api.ExecutionContext;
import eu.europeana.uim.api.IngestionPluginFailedException;
import eu.europeana.uim.common.TKey;
import eu.europeana.uim.enrichment.service.EnrichmentService;
import eu.europeana.uim.enrichment.utils.OsgiEdmMongoServer;
import eu.europeana.uim.enrichment.utils.PropertyReader;
import eu.europeana.uim.enrichment.utils.UimConfigurationProperty;
import eu.europeana.uim.model.europeana.EuropeanaModelRegistry;
import eu.europeana.uim.model.europeanaspecific.fieldvalues.ControlledVocabularyProxy;
import eu.europeana.uim.model.europeanaspecific.fieldvalues.EuropeanaRetrievableField;
import eu.europeana.uim.store.Collection;
import eu.europeana.uim.store.MetaDataRecord;
import eu.europeana.uim.sugarcrm.SugarCrmRecord;
import eu.europeana.uim.sugarcrm.SugarCrmService;

/**
 * Enrichment plugin implementation
 * 
 * @author Yorgos.Mamakis@ kb.nl
 * 
 */
public class EnrichmentPlugin extends AbstractIngestionPlugin {

	private static CommonsHttpSolrServer solrServer;

	private static String mongoDB;
	private static String mongoHost = PropertyReader
			.getProperty(UimConfigurationProperty.MONGO_HOSTURL);
	private static String mongoPort = PropertyReader
			.getProperty(UimConfigurationProperty.MONGO_HOSTPORT);
	private static String solrUrl;
	private static String solrCore;
	private static int recordNumber;
	private static String europeanaID = PropertyReader
			.getProperty(UimConfigurationProperty.MONGO_DB_EUROPEANA_ID);
	private static final int RETRIES = 10;
	private static String repository = PropertyReader
			.getProperty(UimConfigurationProperty.UIM_REPOSITORY);
	private static SugarCrmService sugarCrmService;
	private static EnrichmentService enrichmentService;
	private static String previewsOnlyInPortal;
	private static String collections = PropertyReader
			.getProperty(UimConfigurationProperty.MONGO_DB_COLLECTIONS);
	private static Morphia morphia;
	private static List<SolrInputDocument> solrList;
	private static OsgiEdmMongoServer mongoServer;
	private static Mongo mongo;
	private final static String PORTALURL = "http:///www.europeana.eu/portal/record";
	private final static String SUFFIX = ".html";

	private static String country;
	private static String creator;
	private static String language;
	private static String rights;

	private final static String countryProperty = "edm:country";
	private final static String creatorProperty = "dc:creator";
	private final static String languageProperty = "edm:language";
	private final static String rightsProperty = "edm:rights";

	public EnrichmentPlugin(String name, String description) {
		super(name, description);
		// TODO Auto-generated constructor stub
	}

	public EnrichmentPlugin() {
		super("", "");
		// TODO Auto-generated constructor stub
	}

	private static final Logger log = Logger.getLogger(EnrichmentPlugin.class
			.getName());
	/**
	 * The parameters used by this WorkflowStart
	 */
	private static final List<String> params = new ArrayList<String>() {
		private static final long serialVersionUID = 1L;
		{
			add(countryProperty);
			add(creatorProperty);
			add(languageProperty);
			add(rightsProperty);
		}

	};

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

	public void initialize() {
		// TODO Auto-generated method stub

	}

	public void shutdown() {
		// TODO Auto-generated method stub

	}

	public List<String> getParameters() {
		// TODO Auto-generated method stub
		return params;
	}

	public int getPreferredThreadCount() {
		// TODO Auto-generated method stub
		return 1;
	}

	public int getMaximumThreadCount() {
		// TODO Auto-generated method stub
		return 1;
	}

	public <I> void initialize(ExecutionContext<I> context)
			throws IngestionPluginFailedException {
		// TODO Auto-generated method stub

		try {
			solrList = new ArrayList<SolrInputDocument>();
			solrServer = enrichmentService.getSolrServer();
			mongo = new Mongo(mongoHost, Integer.parseInt(mongoPort));
			mongoDB = enrichmentService.getMongoDB();
			String uname = PropertyReader.getProperty(UimConfigurationProperty.MONGO_USERNAME)!=null?PropertyReader.getProperty(UimConfigurationProperty.MONGO_USERNAME):"";
			String pass = PropertyReader.getProperty(UimConfigurationProperty.MONGO_PASSWORD)!=null?PropertyReader.getProperty(UimConfigurationProperty.MONGO_PASSWORD):"";
			mongoServer = new OsgiEdmMongoServer(mongo, mongoDB, uname, pass);
			morphia = new Morphia();

			mongoServer.createDatastore(morphia);
			@SuppressWarnings("rawtypes")
			Collection collection = (Collection) context.getExecution()
					.getDataSet();
			String sugarCrmId = collection
					.getValue(ControlledVocabularyProxy.SUGARCRMID);
			SugarCrmRecord sugarCrmRecord = sugarCrmService
					.retrieveRecord(sugarCrmId);
			previewsOnlyInPortal = sugarCrmRecord
					.getItemValue(EuropeanaRetrievableField.PREVIEWS_ONLY_IN_PORTAL);

			country = context.getProperties().getProperty(countryProperty);
			creator = context.getProperties().getProperty(creatorProperty);
			language = context.getProperties().getProperty(languageProperty);
			rights = context.getProperties().getProperty(rightsProperty);

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public <I> void completed(ExecutionContext<I> context)
			throws IngestionPluginFailedException {
		try {
			solrServer.add(solrList);
			System.out.println("Adding " + solrList.size() + " documents");

			solrServer.commit();
			System.out.println("Committed in Solr Server");
			solrServer.optimize();
			System.out.println("Optimized");
			solrList = new ArrayList<SolrInputDocument>();
			mongoServer.close();
		} catch (IOException e) {
			context.getLoggingEngine().logFailed(
					Level.SEVERE,
					this,
					e,
					"Input/Output exception occured in Solr with the following message: "
							+ e.getMessage());
		} catch (SolrServerException e) {
			context.getLoggingEngine().logFailed(
					Level.SEVERE,
					this,
					e,
					"Solr server exception occured in Solr with the following message: "
							+ e.getMessage());
		}

	}

	public <I> boolean processRecord(MetaDataRecord<I> mdr,
			ExecutionContext<I> context) throws IngestionPluginFailedException,
			CorruptedMetadataRecordException {
		IBindingFactory bfact;
		MongoConstructor mongoConstructor = new MongoConstructor();

		try {

			bfact = BindingDirectory.getFactory(RDF.class);

			IUnmarshallingContext uctx = bfact.createUnmarshallingContext();

			String value = mdr.getValues(
					EuropeanaModelRegistry.EDMDEREFERENCEDRECORD).get(0);
			RDF rdf = (RDF) uctx.unmarshalDocument(new StringReader(value));
			List<Entity> entities = enrichmentService
					.enrich(new SolrConstructor().constructSolrDocument(rdf));
			mergeEntities(rdf, entities);
			RDF rdfFinal = cleanRDF(rdf);

			SolrInputDocument solrInputDocument = new SolrConstructor()
					.constructSolrDocument(rdfFinal);
			FullBeanImpl fullBean = mongoConstructor.constructFullBean(
					rdfFinal, mongoServer);
			solrInputDocument.addField(
					EdmLabel.PREVIEW_NO_DISTRIBUTE.toString(),
					previewsOnlyInPortal);
			fullBean.getAggregations()
					.get(0)
					.setEdmPreviewNoDistribute(
							Boolean.parseBoolean(previewsOnlyInPortal));

			String collectionId = (String) mdr.getCollection().getId();

			String fileName = (String) mdr.getCollection().getName();
			String hash = hashExists(collectionId, fileName, fullBean);

			if (StringUtils.isNotEmpty(hash)) {
				createLookupEntry(mongo, fullBean, hash);
			}

			fullBean.setEuropeanaCollectionName(new String[] { fileName });
			if (mongoServer.getFullBean(fullBean.getAbout()) == null) {

				mongoServer.getDatastore().save(fullBean);
			}

			int retries = 0;
			while (retries < RETRIES) {
				try {
					solrList.add(solrInputDocument);

					recordNumber++;
					return true;
				} catch (SolrException e) {
					log.log(Level.WARNING, "Solr Exception occured with error "
							+ e.getMessage() + "\nRetrying");
				}
				retries++;
			}
			return false;

		} catch (JiBXException e) {
			log.log(Level.WARNING,
					"JibX Exception occured with error " + e.getMessage()
							+ "\nRetrying");
		} catch (MalformedURLException e) {
			log.log(Level.WARNING,
					"Malformed URL Exception occured with error "
							+ e.getMessage() + "\nRetrying");
		} catch (InstantiationException e) {
			log.log(Level.WARNING,
					"Instantiation Exception occured with error "
							+ e.getMessage() + "\nRetrying");
		} catch (IllegalAccessException e) {
			log.log(Level.WARNING,
					"Illegal Access Exception occured with error "
							+ e.getMessage() + "\nRetrying");
		} catch (IOException e) {
			log.log(Level.WARNING,
					"IO Exception occured with error " + e.getMessage()
							+ "\nRetrying");
		} catch (Exception e) {

			log.log(Level.WARNING,
					"Generic Exception occured with error " + e.getMessage()
							+ "\nRetrying");
			e.printStackTrace();
		}
		return false;
	}

	private void mergeEntities(RDF rdf, List<Entity> entities)
			throws SecurityException, IllegalArgumentException,
			NoSuchMethodException, IllegalAccessException,
			InvocationTargetException {
		ProxyType europeanaProxy = null;
		ProvidedCHOType cho = null;
		List<RDF.Choice> choices = rdf.getChoiceList();
		for (RDF.Choice choice : choices) {
			if (choice.ifProvidedCHO()) {
				cho = choice.getProvidedCHO();

			}
			if (choice.ifProxy()
					&& choice.getProxy().getEuropeanaProxy() != null
					&& choice.getProxy().getEuropeanaProxy().isEuropeanaProxy()) {
				europeanaProxy = choice.getProxy();
			}
		}
		europeanaProxy.setAbout("/proxy/europeana" + cho.getAbout());
		ProxyFor pf = new ProxyFor();
		pf.setResource(cho.getAbout());
		europeanaProxy.setProxyFor(pf);
		List<ProxyIn> pinList = new ArrayList<ProxyIn>();
		ProxyIn pin = new ProxyIn();
		pin.setResource("/aggregation/europeana" + cho.getAbout());
		pinList.add(pin);
		europeanaProxy.setProxyInList(pinList);
		for (Entity entity : entities) {
			RDF.Choice choice = new RDF.Choice();
			if (StringUtils.equals(entity.getClassName(), "Concept")) {
				Concept concept = new Concept();
				List<Field> fields = entity.getFields();
				if (fields != null && fields.size() > 0) {
					for (Field field : fields) {
						if (StringUtils.equalsIgnoreCase(field.getName(),
								"skos_concept")) {
							concept.setAbout(field
									.getValues()
									.get(field.getValues().keySet().iterator()
											.next()).get(0));
							addToHasMetList(
									europeanaProxy,
									field.getValues()
											.get(field.getValues().keySet()
													.iterator().next()).get(0));
						} else {
							if (field.getValues() != null) {
								for (Entry<String, List<String>> entry : field
										.getValues().entrySet()) {
									for (String str : entry.getValue()) {
										appendConceptValue(concept,
												field.getName(), str,
												"_@xml:lang", entry.getKey());
									}
								}
							}
						}

					}

					choice.setConcept(concept);
					choices.add(choice);
					rdf.setChoiceList(choices);
				}
			} else if (StringUtils.equals(entity.getClassName(), "Timespan")) {

				TimeSpanType ts = new TimeSpanType();
				List<Field> fields = entity.getFields();
				if (fields != null && fields.size() > 0) {
					for (Field field : fields) {
						if (StringUtils.equalsIgnoreCase(field.getName(),
								"edm_timespan")) {
							ts.setAbout(field
									.getValues()
									.get(field.getValues().keySet().iterator()
											.next()).get(0));
							addToHasMetList(
									europeanaProxy,
									field.getValues()
											.get(field.getValues().keySet()
													.iterator().next()).get(0));
						} else {
							for (Entry<String, List<String>> entry : field
									.getValues().entrySet()) {
								for (String str : entry.getValue()) {
									appendValue(TimeSpanType.class, ts,
											field.getName(), str, "_@xml:lang",
											entry.getKey());
								}
							}
						}

					}

					choice.setTimeSpan(ts);
					choices.add(choice);
					rdf.setChoiceList(choices);
				}
			} else if (StringUtils.equals(entity.getClassName(), "Agent")) {

				AgentType ts = new AgentType();
				List<Field> fields = entity.getFields();
				if (fields != null && fields.size() > 0) {
					for (Field field : fields) {
						if (StringUtils.equalsIgnoreCase(field.getName(),
								"edm_agent")) {
							ts.setAbout(field
									.getValues()
									.get(field.getValues().keySet().iterator()
											.next()).get(0));
							addToHasMetList(
									europeanaProxy,
									field.getValues()
											.get(field.getValues().keySet()
													.iterator().next()).get(0));
						} else {
							for (Entry<String, List<String>> entry : field
									.getValues().entrySet()) {
								for (String str : entry.getValue()) {
									appendValue(AgentType.class, ts,
											field.getName(), str, "_@xml:lang",
											entry.getKey());
								}
							}
						}

					}

					choice.setAgent(ts);
					choices.add(choice);
					rdf.setChoiceList(choices);
				}
			} else {
				PlaceType ts = new PlaceType();
				List<Field> fields = entity.getFields();
				if (fields != null && fields.size() > 0) {
					for (Field field : fields) {
						if (StringUtils.equalsIgnoreCase(field.getName(),
								"edm_place")) {
							ts.setAbout(field
									.getValues()
									.get(field.getValues().keySet().iterator()
											.next()).get(0));
							addToHasMetList(
									europeanaProxy,
									field.getValues()
											.get(field.getValues().keySet()
													.iterator().next()).get(0));
						} else {
							if (field.getValues() != null) {
								for (Entry<String, List<String>> entry : field
										.getValues().entrySet()) {
									for (String str : entry.getValue()) {
										appendValue(PlaceType.class, ts,
												field.getName(), str,
												"_@xml:lang", entry.getKey());
									}
								}
							}
						}

					}

					choice.setPlace(ts);
					choices.add(choice);
					rdf.setChoiceList(choices);
				}
			}
		}
		RDF.Choice choice = new RDF.Choice();
		choice.setProxy(europeanaProxy);
	}

	private void addToHasMetList(ProxyType europeanaProxy, String value) {
		List<HasMet> hasMetList = europeanaProxy.getHasMetList() != null ? europeanaProxy
				.getHasMetList() : new ArrayList<HasMet>();
		HasMet hasMet = new HasMet();
		hasMet.setString(value);
		hasMetList.add(hasMet);
		europeanaProxy.setHasMetList(hasMetList);
	}

	private RDF cleanRDF(RDF rdf) {
		RDF rdfFinal = new RDF();
		List<AgentType> agents = new CopyOnWriteArrayList<AgentType>();
		List<TimeSpanType> timespans = new CopyOnWriteArrayList<TimeSpanType>();
		List<PlaceType> places = new CopyOnWriteArrayList<PlaceType>();
		List<Concept> concepts = new CopyOnWriteArrayList<Concept>();
		List<RDF.Choice> choices = new ArrayList<RDF.Choice>();
		for (RDF.Choice rdfChoice : rdf.getChoiceList()) {
			if (rdfChoice.ifAgent()) {
				AgentType newAgent = rdfChoice.getAgent();
				for (AgentType agent : agents) {
					if (StringUtils.equals(agent.getAbout(),
							newAgent.getAbout())) {
						if (agent.getPrefLabelList().size() <= newAgent
								.getPrefLabelList().size()) {
							agents.remove(agent);
						}
					}
				}
				agents.add(newAgent);
			} else if (rdfChoice.ifConcept()) {
				Concept newConcept = rdfChoice.getConcept();
				for (Concept concept : concepts) {
					if (StringUtils.equals(concept.getAbout(),
							newConcept.getAbout())) {
						if (concept.getChoiceList().size() <= newConcept
								.getChoiceList().size()) {
							concepts.remove(concept);
						}
					}
				}
				concepts.add(newConcept);
			} else if (rdfChoice.ifTimeSpan()) {
				TimeSpanType newTs = rdfChoice.getTimeSpan();
				for (TimeSpanType ts : timespans) {
					if (StringUtils.equals(ts.getAbout(), newTs.getAbout())) {
						if (ts.getIsPartOfList().size() <= newTs
								.getIsPartOfList().size()) {
							timespans.remove(ts);
						}
					}
				}
				timespans.add(newTs);
			} else if (rdfChoice.ifPlace()) {
				PlaceType newPlace = rdfChoice.getPlace();
				for (PlaceType place : places) {
					if (StringUtils.equals(place.getAbout(),
							newPlace.getAbout())) {
						if (place.getPrefLabelList().size() <= newPlace
								.getPrefLabelList().size()) {
							places.remove(place);
						}
					}
				}
				places.add(newPlace);
			} else {
				choices.add(rdfChoice);
			}
		}
		for (AgentType agent : agents) {
			Choice choice = new Choice();
			choice.setAgent(agent);
			choices.add(choice);
		}
		for (PlaceType place : places) {
			Choice choice = new Choice();
			choice.setPlace(place);
			choices.add(choice);
		}
		for (TimeSpanType timespan : timespans) {
			Choice choice = new Choice();
			choice.setTimeSpan(timespan);
			choices.add(choice);
		}
		for (Concept concept : concepts) {
			Choice choice = new Choice();
			choice.setConcept(concept);
			choices.add(choice);
		}
		EuropeanaAggregationType europeanaAggregation = createEuropeanaAggregation(rdf);
		RDF.Choice choice = new RDF.Choice();
		choice.setEuropeanaAggregation(europeanaAggregation);
		choices.add(choice);
		rdfFinal.setChoiceList(choices);
		return rdfFinal;
	}

	private EuropeanaAggregationType createEuropeanaAggregation(RDF rdf) {
		EuropeanaAggregationType europeanaAggregation = new EuropeanaAggregationType();
		List<RDF.Choice> rdfList = rdf.getChoiceList();
		for (RDF.Choice ch : rdfList) {
			if (ch.ifProvidedCHO()) {
				ProvidedCHOType cho = ch.getProvidedCHO();
				europeanaAggregation.setAbout("/aggregation/europeana/"
						+ cho.getAbout());
				LandingPage lp = new LandingPage();
				lp.setResource(PORTALURL + cho.getAbout() + SUFFIX);
				europeanaAggregation.setLandingPage(lp);
				Country countryType = new Country();
				countryType.setString(country);
				europeanaAggregation.setCountry(countryType);
				Creator creatorType = new Creator();
				creatorType.setString(creator);
				europeanaAggregation.setCreator(creatorType);
				Language1 languageType = new Language1();
				languageType.setString(language);
				europeanaAggregation.setLanguage(languageType);
				Rights1 rightsType = new Rights1();
				rightsType.setResource(rights);
				europeanaAggregation.setRights(rightsType);
				AggregatedCHO aggrCHO = new AggregatedCHO();
				aggrCHO.setResource(cho.getAbout());
				europeanaAggregation.setAggregatedCHO(aggrCHO);
			}
		}
		return europeanaAggregation;
	}

	public CommonsHttpSolrServer getSolrServer() {
		return solrServer;
	}

	public void setSolrServer(CommonsHttpSolrServer solrServer) {
		EnrichmentPlugin.solrServer = solrServer;
	}

	public void setSugarCrmService(SugarCrmService sugarCrmService) {
		EnrichmentPlugin.sugarCrmService = sugarCrmService;
	}

	public String getEuropeanaID() {
		return europeanaID;
	}

	public void setEuropeanaID(String europeanaID) {
		EnrichmentPlugin.europeanaID = europeanaID;
	}

	public int getRecords() {
		return recordNumber;
	}

	public String getRepository() {
		return repository;
	}

	public void setRepository(String repository) {
		EnrichmentPlugin.repository = repository;
	}

	public String getCollections() {
		return collections;
	}

	public void setCollections(String collections) {
		EnrichmentPlugin.collections = collections;
	}

	public SugarCrmService getSugarCrmService() {
		return sugarCrmService;
	}

	public String getMongoDB() {
		return mongoDB;
	}

	public void setMongoDB(String mongoDB) {
		EnrichmentPlugin.mongoDB = mongoDB;
	}

	public String getMongoHost() {
		return mongoHost;
	}

	public void setMongoHost(String mongoHost) {
		EnrichmentPlugin.mongoHost = mongoHost;
	}

	public String getMongoPort() {
		return mongoPort;
	}

	public void setMongoPort(String mongoPort) {
		EnrichmentPlugin.mongoPort = mongoPort;
	}

	public String getSolrUrl() {
		return solrUrl;
	}

	public void setSolrUrl(String solrUrl) {
		EnrichmentPlugin.solrUrl = solrUrl;
	}

	public String getSolrCore() {
		return solrCore;
	}

	public void setSolrCore(String solrCore) {
		EnrichmentPlugin.solrCore = solrCore;
	}

	public EnrichmentService getEnrichmentService() {
		return enrichmentService;
	}

	public void setEnrichmentService(EnrichmentService enrichmentService) {
		EnrichmentPlugin.enrichmentService = enrichmentService;
	}

	private void createLookupEntry(Mongo mongo, FullBean fullBean, String hash) {
		EuropeanaIdMongoServer europeanaIdMongoServer = new EuropeanaIdMongoServer(
				mongo, europeanaID);
		EuropeanaId europeanaId = europeanaIdMongoServer
				.retrieveEuropeanaIdFromOld(hash).get(0);
		europeanaId.setNewId(fullBean.getAbout());
		europeanaIdMongoServer.saveEuropeanaId(europeanaId);

	}

	private String hashExists(String collectionId, String fileName,
			FullBean fullBean) {
		SipCreatorUtils sipCreatorUtils = new SipCreatorUtils();
		sipCreatorUtils.setRepository(repository);
		if (sipCreatorUtils.getHashField(collectionId, fileName) != null) {
			return HashUtils.createHash(EseEdmMap.valueOf(
					sipCreatorUtils.getHashField(collectionId, fileName))
					.getEdmValue(fullBean));
		}
		PreSipCreatorUtils preSipCreatorUtils = new PreSipCreatorUtils();
		preSipCreatorUtils.setRepository(repository);
		if (preSipCreatorUtils.getHashField(collectionId, fileName) != null) {
			return HashUtils.createHash(EseEdmMap.valueOf(
					preSipCreatorUtils.getHashField(collectionId, fileName))
					.getEdmValue(fullBean));
		}
		return null;
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	private <T> T appendValue(Class<T> clazz, T obj, String edmLabel,
			String val, String edmAttr, String valAttr)
			throws SecurityException, NoSuchMethodException,
			IllegalArgumentException, IllegalAccessException,
			InvocationTargetException {
		RdfMethod RDF = null;
		for (RdfMethod rdfMethod : RdfMethod.values()) {
			if (StringUtils.equals(rdfMethod.getSolrField(), edmLabel)) {
				RDF = rdfMethod;
			}
		}

		//
		if (RDF != null) {
			if (RDF.getMethodName().endsWith("List")) {

				Method mthd = clazz.getMethod(RDF.getMethodName());

				List lst = mthd.invoke(obj) != null ? (ArrayList) mthd
						.invoke(obj) : new ArrayList();
				if (RDF.getClazz().getSuperclass()
						.isAssignableFrom(ResourceType.class)) {

					ResourceType rs = new ResourceType();
					rs.setResource(val);
					lst.add(RDF.returnObject(RDF.getClazz(), rs));

				} else if (RDF.getClazz().getSuperclass()
						.isAssignableFrom(ResourceOrLiteralType.class)) {
					ResourceOrLiteralType rs = new ResourceOrLiteralType();
					if (isURI(val)) {
						rs.setResource(val);
					} else {
						rs.setString(val);
					}
					if (edmAttr != null
							&& StringUtils.equals(
									StringUtils.split(edmAttr, "@")[1],
									"xml:lang")) {
						Lang lang = new Lang();
						lang.setLang(valAttr);
						rs.setLang(lang);
					}
					lst.add(RDF.returnObject(RDF.getClazz(), rs));
				} else if (RDF.getClazz().getSuperclass()
						.isAssignableFrom(LiteralType.class)) {
					LiteralType rs = new LiteralType();
					rs.setString(val);
					if (edmAttr != null
							&& StringUtils.equals(
									StringUtils.split(edmAttr, "@")[1],
									"xml:lang")) {
						LiteralType.Lang lang = new LiteralType.Lang();
						lang.setLang(valAttr);
						rs.setLang(lang);
					}
					lst.add(RDF.returnObject(RDF.getClazz(), rs));
				}

				Class<?>[] cls = new Class<?>[1];
				cls[0] = List.class;
				Method method = obj.getClass().getMethod(
						StringUtils.replace(RDF.getMethodName(), "get", "set"),
						cls);
				method.invoke(obj, lst);
			} else {
				if (RDF.getClazz().isAssignableFrom(ResourceType.class)) {
					ResourceType rs = new ResourceType();
					rs.setResource(val);
					Class<?>[] cls = new Class<?>[1];
					cls[0] = RDF.getClazz();
					Method method = obj.getClass().getMethod(
							StringUtils.replace(RDF.getMethodName(), "get",
									"set"), cls);
					method.invoke(obj, RDF.returnObject(RDF.getClazz(), rs));
				} else if (RDF.getClazz().isAssignableFrom(LiteralType.class)) {
					LiteralType rs = new LiteralType();
					rs.setString(val);
					if (edmAttr != null
							&& StringUtils.equals(
									StringUtils.split(edmAttr, "@")[1],
									"xml:lang")) {
						LiteralType.Lang lang = new LiteralType.Lang();
						lang.setLang(valAttr);
						rs.setLang(lang);
					}
					Class<?>[] cls = new Class<?>[1];
					cls[0] = RDF.getClazz();
					Method method = obj.getClass().getMethod(
							StringUtils.replace(RDF.getMethodName(), "get",
									"set"), cls);
					method.invoke(obj, RDF.returnObject(RDF.getClazz(), rs));

				} else if (RDF.getClazz().isAssignableFrom(
						ResourceOrLiteralType.class)) {
					ResourceOrLiteralType rs = new ResourceOrLiteralType();
					if (isURI(val)) {
						rs.setResource(val);
					} else {
						rs.setString(val);
					}
					if (edmAttr != null
							&& StringUtils.equals(
									StringUtils.split(edmAttr, "@")[1],
									"xml:lang")) {
						Lang lang = new Lang();
						lang.setLang(valAttr);
						rs.setLang(lang);
					}
					Class<?>[] cls = new Class<?>[1];
					cls[0] = clazz;
					Method method = obj.getClass().getMethod(
							StringUtils.replace(RDF.getMethodName(), "get",
									"set"), cls);
					method.invoke(obj, RDF.returnObject(RDF.getClazz(), rs));
				}
			}
		}
		//
		return obj;
	}

	private Concept appendConceptValue(Concept concept, String edmLabel,
			String val, String edmAttr, String valAttr)
			throws SecurityException, NoSuchMethodException,
			IllegalArgumentException, IllegalAccessException,
			InvocationTargetException {
		RdfMethod RDF = null;
		for (RdfMethod rdfMethod : RdfMethod.values()) {
			if (StringUtils.equals(rdfMethod.getSolrField(), edmLabel)) {
				RDF = rdfMethod;
				break;
			}
		}
		List<Concept.Choice> lst = concept.getChoiceList() != null ? concept
				.getChoiceList() : new ArrayList<Concept.Choice>();
		if (RDF.getClazz().getSuperclass().isAssignableFrom(ResourceType.class)) {

			ResourceType obj = new ResourceType();
			obj.setResource(val);
			Class<?>[] cls = new Class<?>[1];
			cls[0] = RDF.getClazz();
			Concept.Choice choice = new Concept.Choice();
			Method method = choice.getClass()
					.getMethod(
							StringUtils.replace(RDF.getMethodName(), "get",
									"set"), cls);
			method.invoke(choice, RDF.returnObject(RDF.getClazz(), obj));
			lst.add(choice);

		} else if (RDF.getClazz().getSuperclass()
				.isAssignableFrom(ResourceOrLiteralType.class)) {

			ResourceOrLiteralType obj = new ResourceOrLiteralType();

			if (isURI(val)) {
				obj.setResource(val);
			} else {
				obj.setString(val);
			}
			if (edmAttr != null
					&& StringUtils.equals(StringUtils.split(edmAttr, "@")[1],
							"xml:lang")) {
				Lang lang = new Lang();
				lang.setLang(valAttr);
				obj.setLang(lang);
			}
			Class<?>[] cls = new Class<?>[1];
			cls[0] = RDF.getClazz();
			Concept.Choice choice = new Concept.Choice();
			Method method = choice.getClass()
					.getMethod(
							StringUtils.replace(RDF.getMethodName(), "get",
									"set"), cls);
			method.invoke(choice, RDF.returnObject(RDF.getClazz(), obj));
			lst.add(choice);

		} else if (RDF.getClazz().getSuperclass()
				.isAssignableFrom(LiteralType.class)) {
			LiteralType obj = new LiteralType();
			obj.setString(val);
			if (edmAttr != null) {
				LiteralType.Lang lang = new LiteralType.Lang();
				lang.setLang(valAttr);
				obj.setLang(lang);
			}
			Class<?>[] cls = new Class<?>[1];
			cls[0] = RDF.getClazz();
			Concept.Choice choice = new Concept.Choice();
			Method method = choice.getClass()
					.getMethod(
							StringUtils.replace(RDF.getMethodName(), "get",
									"set"), cls);
			method.invoke(choice, RDF.returnObject(RDF.getClazz(), obj));
			lst.add(choice);
		}
		concept.setChoiceList(lst);
		return concept;
	}

	private static boolean isURI(String uri) {

		try {
			new URL(uri);
			return true;
		} catch (MalformedURLException e) {
			return false;
		}

	}
}
