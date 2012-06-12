/* LinkcheckServer.java - created on Jul 15, 2011, Copyright (c) 2011 The European Library, all rights reserved */
package eu.europeana.uim.plugin.thumbler.impl;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.UnknownHostException;
import java.util.logging.Logger;

import org.apache.commons.io.IOUtils;
import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.util.EntityUtils;
import org.theeuropeanlibrary.collections.guarded.Guarded;
import org.theeuropeanlibrary.uim.check.weblink.http.AbstractWeblinkServer;
import org.theeuropeanlibrary.uim.check.weblink.http.GuardedMetaDataRecordUrl;
import org.theeuropeanlibrary.uim.check.weblink.http.HttpClientSetup;
import org.theeuropeanlibrary.uim.check.weblink.http.Submission;
import eu.europeana.corelib.db.dao.impl.NosqlDaoImpl;
import eu.europeana.corelib.db.entity.nosql.ImageCache;
import eu.europeana.corelib.db.service.ThumbnailService;
import eu.europeana.corelib.db.service.impl.ThumbnailServiceImpl;

import com.google.code.morphia.Datastore;
import com.google.code.morphia.Morphia;
import com.mongodb.Mongo;
import com.mongodb.MongoException;


/**
 * HTTP Link checker with internal thread pool using the @see {@link HttpClientSetup} to check the
 * status of links. Initially we try the HEAD method which would be optimal but not widely
 * supported.
 * 
 * If the HEAD method is not supported by the webserver we do use as fallback forth on the GET
 * method.
 * 
 * 
 * @author Andreas Juffinger (andreas.juffinger@kb.nl)
 * @author Georgios Markakis (gwarkx@hotmail.com)
 * @since Jul 15, 2011
 */
public class EuropeanaWeblinkThumbler extends AbstractWeblinkServer {
    private static final Logger    log = Logger.getLogger(EuropeanaWeblinkThumbler.class.getName());

    private static EuropeanaWeblinkThumbler instance;

    private static ThumbnailService thumbnailHandler;
    
    /**
     * Private singleton constructor.
     */
    public EuropeanaWeblinkThumbler() {
    	
    	Morphia mor = new Morphia();
    	Mongo mongo;
		try {
			mongo = new Mongo();
	    	String dbName =  "imageUIMDB";
	    	
			Datastore store = mor.createDatastore(mongo , dbName);
	    	
			NosqlDaoImpl morphiaDAOImpl = new NosqlDaoImpl(ImageCache.class,store);
			
			ThumbnailServiceImpl thumbnailService = new ThumbnailServiceImpl();
			thumbnailService.setDao(morphiaDAOImpl);
			
			thumbnailHandler = thumbnailService;
			
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (MongoException e) {
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
        return new CacheTask((GuardedMetaDataRecordUrl<?>)guarded);
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
                Submission submission = getSubmission(guarded.getExecution());
                File target = null;

                int status = 0;
                HttpResponse response = null;
                HttpRequestBase http = new HttpGet(guarded.getUrl().toExternalForm());

                try {
                    response = HttpClientSetup.getHttpClient().execute(http);
                    status = response.getStatusLine().getStatusCode();

                    if (status == HttpStatus.SC_BAD_REQUEST) {
                        log.info("Bad request: <" + guarded.getUrl() + ">.");

                    } else if (status == HttpStatus.SC_OK) {
                        String name = guarded.getMetaDataRecord().getId() + "_" +
                                      guarded.getIndex();
                        target = store(response, guarded.getDirectory(), name);
                    }

                } catch (Throwable t) {
                    synchronized (submission) {
                        submission.incrExceptions();
                    }

                    log.info("Failed to store url: <" + guarded.getUrl() + "> to <" +
                             guarded.getDirectory().getAbsolutePath() + "> " + t.getMessage());

                    guarded.processed(0, t.getLocalizedMessage());
                } finally {
                    try {
                        if (response != null) EntityUtils.consume(response.getEntity());

                        synchronized (submission) {
                            submission.incrStatus(status);
                            submission.incrProcessed();
                            submission.removeRemaining(guarded);
                        }

                        if (status == HttpStatus.SC_OK && target != null && target.exists()) {

                            guarded.processed(status, "file://" + target.getAbsolutePath());
                        } else if (status > 0) {
                            if (response != null)
                                guarded.processed(status,
                                        response.getStatusLine().getReasonPhrase());
                        } else {
                            // already handled within catch clause
                            log.info("Status was zero: <" + guarded.getUrl() + ">.");
                        }
                    } catch (Throwable t) {
                        t.printStackTrace();
                    }

                }
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
        protected File store(HttpResponse response, File directory, String name) {
            File target = null;
            try {

                Header content = response.getFirstHeader("Content-Type");
                if (content != null) {
                    String value = content.getValue();
                    value = value.replaceAll(".*/", ""); // remove image/ or application/
                    value = value.replaceAll("//s.*", ""); // remove everything after whitespaces
                    target = new File(directory, name + "." + value.toLowerCase());
                } else {
                    // TODO: use magic bytes
                    target = new File(directory, name + ".jpg");
                }

                InputStream in = response.getEntity().getContent();
                IOUtils.copyLarge(in, new FileOutputStream(target));
                
                guarded.getExecution().getDataSet();
                
                //thumbnailHandler.storeThumbnail(objectId, imageId, collectionId, url);
                
                
                return target;
            } catch (Throwable t) {
                if (target != null && target.exists()) {
                    target.delete();
                }
            }
            return null;
        }
    }
}
