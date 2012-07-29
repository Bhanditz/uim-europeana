/* LinkcheckServer.java - created on Jul 15, 2011, Copyright (c) 2011 The European Library, all rights reserved */
package eu.europeana.uim.plugin.thumbler.impl;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.StringReader;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.logging.Logger;

import javax.imageio.ImageIO;


import org.apache.commons.io.FileUtils;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.util.EntityUtils;
import org.jibx.runtime.BindingDirectory;
import org.jibx.runtime.IBindingFactory;
import org.jibx.runtime.IUnmarshallingContext;
import org.jibx.runtime.JiBXException;
import org.theeuropeanlibrary.collections.guarded.Guarded;
import org.theeuropeanlibrary.model.common.Link;
import org.theeuropeanlibrary.uim.check.weblink.http.AbstractWeblinkServer;
import org.theeuropeanlibrary.uim.check.weblink.http.GuardedMetaDataRecordUrl;
import org.theeuropeanlibrary.uim.check.weblink.http.HttpClientSetup;
import org.theeuropeanlibrary.uim.check.weblink.http.Submission;
import eu.europeana.corelib.db.dao.impl.NosqlDaoImpl;
import eu.europeana.corelib.db.entity.nosql.ImageCache;
import eu.europeana.corelib.db.service.ThumbnailService;
import eu.europeana.corelib.db.service.impl.ThumbnailServiceImpl;
import eu.europeana.corelib.definitions.jibx.Aggregation;
import eu.europeana.corelib.definitions.jibx.HasView;
import eu.europeana.corelib.definitions.jibx.ProxyType;
import eu.europeana.corelib.definitions.jibx.RDF;
import eu.europeana.corelib.definitions.jibx.RDF.Choice;
import eu.europeana.corelib.definitions.jibx.ProvidedCHOType;



import eu.europeana.uim.model.europeana.EuropeanaModelRegistry;
import eu.europeana.uim.plugin.thumbler.utils.EDMXMPValues;
import eu.europeana.uim.plugin.thumbler.utils.ImageMagickUtils;
import eu.europeana.uim.store.Collection;
import eu.europeana.uim.store.MetaDataRecord;
import eu.europeana.uim.store.UimDataSet;

import com.google.code.morphia.Datastore;
import com.google.code.morphia.Morphia;
import com.mongodb.Mongo;
import com.mongodb.MongoException;

/**
 * HTTP Link checker with internal thread pool using the @see
 * {@link HttpClientSetup} to check the status of links. Initially we try the
 * HEAD method which would be optimal but not widely supported.
 * 
 * If the HEAD method is not supported by the webserver we do use as fallback
 * forth on the GET method.
 * 
 * 
 * @author Andreas Juffinger (andreas.juffinger@kb.nl)
 * @author Georgios Markakis (gwarkx@hotmail.com)
 * @since Jul 15, 2011
 */
public class EuropeanaWeblinkThumbler extends AbstractWeblinkServer {
	private static final Logger log = Logger
			.getLogger(EuropeanaWeblinkThumbler.class.getName());

	private static EuropeanaWeblinkThumbler instance;

	private static ThumbnailService thumbnailHandler;

	private static IBindingFactory bfact;

	private static IUnmarshallingContext uctx;

	private static final String STORAGELOCATION = "C:\\tmpimages\\";
	

	/**
	 * Private singleton constructor.
	 */
	public EuropeanaWeblinkThumbler() {

		Morphia mor = new Morphia();
		Mongo mongo;
		try {
			mongo = new Mongo();
			String dbName = "imageUIMDB";

			Datastore store = mor.createDatastore(mongo, dbName);

			@SuppressWarnings("unchecked")
			NosqlDaoImpl morphiaDAOImpl = new NosqlDaoImpl(ImageCache.class,
					store);

			ThumbnailServiceImpl thumbnailService = new ThumbnailServiceImpl();
			thumbnailService.setDao(morphiaDAOImpl);

			thumbnailHandler = thumbnailService;

			bfact = BindingDirectory.getFactory(RDF.class);
			uctx = bfact.createUnmarshallingContext();

		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (MongoException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (JiBXException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	
	/**
	 * @return the singleton linkchecker instance
	 */
	public static EuropeanaWeblinkThumbler getShared() {
		if (instance == null) {
			instance = new EuropeanaWeblinkThumbler();
		}
		return instance;
	}

	@Override
	public void shutdown() {
		super.shutdown();
		instance = null;
	}

	@Override
	public Runnable createTask(Guarded guarded) {
		return new CacheTask((GuardedMetaDataRecordUrl<?>) guarded);
	}

	
	/**
	 * @author Andreas Juffinger (andreas.juffinger@kb.nl)
	 * @since Jul 15, 2011
	 */
	private class CacheTask implements Runnable {
		private final GuardedMetaDataRecordUrl<?> guarded;

		public CacheTask(GuardedMetaDataRecordUrl<?> guarded) {
			this.guarded = guarded;
		}

		@Override
		public void run() {
			if (guarded != null) {
				store();
			}
		}

			
		/**
		 * @param response
		 * @param directory
		 *            where to store
		 * @param name
		 *            under which name to store
		 * @return file with downloaded content
		 */
		protected File store() {
			File target = null;
			try {

				UimDataSet<?> dataset = guarded.getExecution().getDataSet();

				MetaDataRecord<?> mdr = guarded.getMetaDataRecord();

				String value = mdr.getValues(EuropeanaModelRegistry.EDMRECORD)
						.get(0);

				StringReader reader = new StringReader(value);

				RDF edmRecord = (RDF) uctx.unmarshalDocument(reader);

				Map<EDMXMPValues, String> xmpvalues = populatevalues(edmRecord);

				String objectIDURI = xmpvalues
						.get(EDMXMPValues.xmpMM_OriginalDocumentID);

				Link offeredlink = guarded.getLink();



				if (offeredlink.getUrl().equals(objectIDURI)) {
					 if(!thumbnailHandler.exists(offeredlink.getUrl())){
						 
						 File img = retrieveFile(offeredlink.getUrl());

						 File convimg = ImageMagickUtils.convert(img);
						 
						 ImageMagickUtils.addXMPInfo(convimg,xmpvalues);

						 BufferedImage buff = ImageIO.read(convimg); 

					     Collection coll = (Collection) dataset;
						 
						 thumbnailHandler.storeThumbnail(objectIDURI,
									(String) coll.getId(), buff,
									offeredlink.getUrl());
					 }
				} else {
					List<Choice> elements = edmRecord.getChoiceList();
					for (Choice element : elements) {

						if (element.ifAggregation()) {

							Aggregation aggregation = element.getAggregation();

							List<HasView> has_views = aggregation.getHasViewList();

							for (HasView view : has_views) {
								String resource = view.getResource();
								if (resource.equals(offeredlink.getUrl())) {
									
									if(!thumbnailHandler.exists(objectIDURI,
											 resource)){
										
										File img = retrieveFile(resource);
										
										File convimg = ImageMagickUtils.convert(img);
										 
										ImageMagickUtils.addXMPInfo(convimg,xmpvalues);

										BufferedImage buff = ImageIO.read(convimg); 

										Collection coll = (Collection) dataset;
										
										thumbnailHandler.storeThumbnail(objectIDURI,resource,
													(String) coll.getMnemonic(), buff,
													offeredlink.getUrl());										 
									 }
								}

							}
						}
					}
				}
				

				return target;
			} catch (Throwable t) {
				
				log.severe(t.getMessage());

			}
			return null;
		}


		/**
		 * @param url
		 * @return
		 */
		private File retrieveFile(String url) {

			Submission submission = getSubmission(guarded.getExecution());
			File target = null;
			int status = 0;
			HttpResponse response = null;
			HttpRequestBase http = new HttpGet(guarded.getUrl()
					.toExternalForm());

			try {
				response = HttpClientSetup.getHttpClient().execute(http);
				status = response.getStatusLine().getStatusCode();

				if (status == HttpStatus.SC_BAD_REQUEST) {
					log.info("Bad request: <" + guarded.getUrl() + ">.");

					
				} else if (status == HttpStatus.SC_OK) {
					String name =  guarded.getUrl().getFile();  //new Integer(generator.nextInt()).toString();

					target = new File(STORAGELOCATION +name);
					FileUtils.copyURLToFile(new URL(url), target, 100, 1000);
					
					return target;

				}

			} catch (Throwable t) {
				synchronized (submission) {
					submission.incrExceptions();
				}

				log.info("Failed to store url: <" + guarded.getUrl() + ">");

				guarded.processed(0, t.getLocalizedMessage());
			} finally {
				try {
					if (response != null)
						EntityUtils.consume(response.getEntity());

					synchronized (submission) {
						submission.incrStatus(status);
						submission.incrProcessed();
						submission.removeRemaining(guarded);
					}

					if (status == HttpStatus.SC_OK && target != null
							&& target.exists()) {

						guarded.processed(status,
								"file://" + target.getAbsolutePath());
					} else if (status > 0) {
						if (response != null)
							guarded.processed(status, response.getStatusLine()
									.getReasonPhrase());
					} else {
						// already handled within catch clause
						log.info("Status was zero: <" + guarded.getUrl() + ">.");
					}
				} catch (Throwable t) {
					t.printStackTrace();
				}
			}

			return null;
		}
		
		

		/**
		 * @param edmRecord
		 * @return
		 */
		private Map<EDMXMPValues, String> populatevalues(RDF edmRecord) {

			HashMap<EDMXMPValues, String> EDMXMPValuesMap = new HashMap<EDMXMPValues, String>();

			List<Choice> elements = edmRecord.getChoiceList();

			for (Choice element : elements) {

				// Deal with Aggregation elements
				if (element.ifAggregation()) {

					Aggregation aggregation = element.getAggregation();


					if (aggregation.getIsShownAt() != null) {
						EDMXMPValuesMap.put(EDMXMPValues.xmpRights_Marked, aggregation
								.getIsShownAt().getResource());
						EDMXMPValuesMap.put(EDMXMPValues.cc_morePermissions, aggregation
								.getIsShownAt().getResource());
					}

					if (aggregation.getObject() != null) {
						EDMXMPValuesMap.put(EDMXMPValues.xmpMM_OriginalDocumentID,
								aggregation.getObject().getResource());
					}

					if (aggregation.getRights() != null) {
						EDMXMPValuesMap.put(EDMXMPValues.edm_rights, aggregation
								.getRights().getResource());

						if (aggregation
								.getRights()
								.getResource()
								.equals("http://creativecommons.org/publicdomain/mark/1.0/")
								|| aggregation
										.getRights()
										.getResource()
										.equals("http://creativecommons.org/publicdomain/zero/1.0/")) {

							EDMXMPValuesMap.put(EDMXMPValues.xmpRights_Marked, "False");
							EDMXMPValuesMap
									.put(EDMXMPValues.cc_useGuidelines,
											"http://www.europeana.eu/rights/pd-usage-guide/");
						} else {
							EDMXMPValuesMap.put(EDMXMPValues.xmpRights_Marked, "True");
						}

					}

					if (aggregation.getDataProvider() != null) {
						EDMXMPValuesMap.put(EDMXMPValues.edm_dataProvider, aggregation
								.getDataProvider().getResource());
					}

					if (aggregation.getProvider() != null) {
						EDMXMPValuesMap.put(EDMXMPValues.edm_provider, aggregation
								.getProvider().getResource());
					}

				}

				if (element.ifProxy()) {
					ProxyType pcho = element.getProxy();

				
					
					List<eu.europeana.corelib.definitions.jibx.EuropeanaType.Choice> dclist = pcho.getChoiceList();

					for (eu.europeana.corelib.definitions.jibx.EuropeanaType.Choice dcchoice : dclist) {
						if (dcchoice.ifCreator()) {
							EDMXMPValuesMap.put(EDMXMPValues.cc_attributionName,
									dcchoice.getCreator().getString());
						}
						if (dcchoice.ifTitle()) {
							EDMXMPValuesMap.put(EDMXMPValues.dc_title, dcchoice
									.getTitle().getString());
						}
						if (dcchoice.ifRights()) {
							EDMXMPValuesMap.put(EDMXMPValues.dc_rights, dcchoice
									.getRights().getString());
						}
					}

				}

			}
			return EDMXMPValuesMap;
		}

	}



}
