/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.europeana.uim.plugin.thumbler.service;

import eu.europeana.corelib.definitions.jibx.Aggregation;
import eu.europeana.corelib.definitions.jibx.HasView;
import eu.europeana.corelib.definitions.jibx.RDF;
import eu.europeana.harvester.client.HarvesterClientImpl;
import eu.europeana.harvester.domain.DocumentReferenceTaskType;
import eu.europeana.harvester.domain.JobState;
import eu.europeana.harvester.domain.ProcessingJob;
import eu.europeana.harvester.domain.ProcessingJobSubTask;
import eu.europeana.harvester.domain.ProcessingJobSubTaskType;
import eu.europeana.harvester.domain.ProcessingJobTaskDocumentReference;
import eu.europeana.harvester.domain.ReferenceOwner;
import eu.europeana.harvester.domain.SourceDocumentReference;
import eu.europeana.uim.common.TKey;
import eu.europeana.uim.model.europeana.EuropeanaModelRegistry;
import eu.europeana.uim.orchestration.ExecutionContext;
import eu.europeana.uim.plugin.ingestion.AbstractIngestionPlugin;
import eu.europeana.uim.plugin.ingestion.CorruptedDatasetException;
import eu.europeana.uim.plugin.ingestion.IngestionPluginFailedException;
import eu.europeana.uim.plugin.thumbler.InstanceCreator;
import eu.europeana.uim.store.Collection;
import eu.europeana.uim.store.MetaDataRecord;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jibx.runtime.BindingDirectory;
import org.jibx.runtime.IBindingFactory;
import org.jibx.runtime.IUnmarshallingContext;
import org.jibx.runtime.JiBXException;
import org.theeuropeanlibrary.model.common.qualifier.Status;

/**
 *
 * @author gmamakis
 */
public class ImageCachingPlugin<I> extends
        AbstractIngestionPlugin<MetaDataRecord<I>, I> {

    private static IBindingFactory bfact;
    private static HarvesterClientImpl client;
    private static InstanceCreator creator;

    static {
        try {
            // Should be placed in a static block for performance reasons
            bfact = BindingDirectory.getFactory(RDF.class);

        } catch (JiBXException e) {
            e.printStackTrace();
        }

    }
    private static final List<String> params = new ArrayList<String>() {
        private static final long serialVersionUID = 1L;

        {
            add("collection.priority");

        }
    };

    public ImageCachingPlugin() {
        super("", "");
    }

    public ImageCachingPlugin(String name, String description) {
        super(name, description);
    }

    @Override
    public TKey<?, ?>[] getInputFields() {
        return null;
    }

    @Override
    public TKey<?, ?>[] getOptionalFields() {
        return null;
    }

    @Override
    public TKey<?, ?>[] getOutputFields() {
        return null;
    }

    @Override
    public boolean process(MetaDataRecord<I> mdr, ExecutionContext<MetaDataRecord<I>, I> context) throws
            IngestionPluginFailedException, CorruptedDatasetException {

        String value = null;
        String collection = ((Collection) context.getExecution().getDataSet()).
                getMnemonic();
        String provider = ((Collection) context.getExecution().getDataSet()).getProvider().getMnemonic();
        if (mdr.getValues(EuropeanaModelRegistry.EDMDEREFERENCEDRECORD) != null
                && mdr.getValues(EuropeanaModelRegistry.EDMDEREFERENCEDRECORD)
                .size() > 0) {
            value = mdr.getValues(EuropeanaModelRegistry.EDMDEREFERENCEDRECORD)
                    .get(0);
        } else {
            value = mdr.getValues(EuropeanaModelRegistry.EDMRECORD).get(0);
        }
        try {
            IUnmarshallingContext uctx = bfact.createUnmarshallingContext();
            RDF rdf = (RDF) uctx.unmarshalDocument(new StringReader(value));
            List<Status> status = mdr
                    .getValues(EuropeanaModelRegistry.STATUS);
            if (!(status != null && status.size() > 0 && status.get(0).equals(Status.DELETED))) {
                String record = rdf.getProvidedCHOList().get(0).getAbout();

                ReferenceOwner owner = new ReferenceOwner(provider, collection, record);
                List<ProcessingJobTaskDocumentReference> tasks = new ArrayList<>();
                List<SourceDocumentReference> docRefs = new ArrayList<>();
                Set<Link> urls = getUrls(rdf);
                for (Link url : urls) {
                    SourceDocumentReference docRef = new SourceDocumentReference(owner, null, url.getUrl(), null, null, 1l,
                            null, true);
                    docRefs.add(docRef);

                    List<ProcessingJobSubTask> jobTask = new ArrayList<>();
                    ProcessingJobSubTask subTask = new ProcessingJobSubTask(ProcessingJobSubTaskType.META_EXTRACTION, null);
                    jobTask.add(subTask);
                    if (url.getIsEdmObject()) {
                        jobTask.add(new ProcessingJobSubTask(ProcessingJobSubTaskType.COLOR_EXTRACTION, null));
                        jobTask.add(new ProcessingJobSubTask(ProcessingJobSubTaskType.GENERATE_THUMBNAIL, null));
                    }
                    tasks.add(new ProcessingJobTaskDocumentReference(DocumentReferenceTaskType.CONDITIONAL_DOWNLOAD,
                            docRef.getId(), jobTask));
                }
                client.createOrModifySourceDocumentReference(docRefs);
                int priority = context.getProperties().getProperty(
                        "collection.priority") != null ? Integer.parseInt(context.getProperties().getProperty(
                                                "collection.priority")) : 50;
                ProcessingJob job = new ProcessingJob(priority, new Date(), owner, tasks, JobState.READY, record);
                client.createProcessingJob(job);
                client.startJob(job.getId());

            }
        } catch (JiBXException ex) {
            Logger.getLogger(LinkCheckingPlugin.class.getName()).log(Level.SEVERE, null, ex);
        }

        return true;

    }

    @Override
    public void initialize(ExecutionContext<MetaDataRecord<I>, I> context) throws IngestionPluginFailedException {
        client = new HarvesterClientImpl(creator.getDatastore(), creator.getConfig());
    }

    @Override
    public void completed(ExecutionContext<MetaDataRecord<I>, I> context) throws IngestionPluginFailedException {

    }

    @Override
    public void initialize() {

    }

    @Override
    public void shutdown() {
    }

    @Override
    public List<String> getParameters() {
        return params;
    }

    @Override
    public int getPreferredThreadCount() {
        return 1;
    }

    @Override
    public int getMaximumThreadCount() {
        return 10;
    }

    private Set<Link> getUrls(RDF rdf) {
        Set<Link> urls = new HashSet<>();
        Aggregation aggr = rdf.getAggregationList().get(0);

        if (aggr.getIsShownBy() != null) {
            String url = aggr.getIsShownBy().getResource();
            urls.add(new Link(url, false));
        }
        if (aggr.getObject() != null) {
            String url = aggr.getObject().getResource();
            urls.add(new Link(url, true));
        }
        if (aggr.getHasViewList() != null) {
            for (HasView hasView : aggr.getHasViewList()) {
                String url = hasView.getResource();
                urls.add(new Link(url, false));
            }
        }

        return urls;
    }

    public void setCreator(InstanceCreator creator) {
        this.creator = creator;
    }

    private class Link {

        private String url;
        private Boolean isEdmObject;

        public Link(String url, Boolean isEdmObject) {
            this.url = url;
            this.isEdmObject = isEdmObject;
        }

        public String getUrl() {
            return url;
        }

        public Boolean getIsEdmObject() {
            return isEdmObject;
        }

        @Override
        public int hashCode() {
            int hash = 7;
            hash = 97 * hash + Objects.hashCode(this.url);
            hash = 97 * hash + Objects.hashCode(this.isEdmObject);
            return hash;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            final Link other = (Link) obj;
            if (!Objects.equals(this.url, other.url)) {
                return false;
            }
            if (!Objects.equals(this.isEdmObject, other.isEdmObject)) {
                return false;
            }
            return true;
        }
    }
}
