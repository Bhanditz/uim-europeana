/*
 * Copyright 2007-2012 The Europeana Foundation
 *
 *  Licenced under the EUPL, Version 1.1 (the "Licence") and subsequent versions as approved
 *  by the European Commission;
 *  You may not use this work except in compliance with the Licence.
 * 
 *  You may obtain a copy of the Licence at:
 *  http://joinup.ec.europa.eu/software/page/eupl
 *
 *  Unless required by applicable law or agreed to in writing, software distributed under
 *  the Licence is distributed on an "AS IS" basis, without warranties or conditions of
 *  any kind, either express or implied.
 *  See the Licence for the specific language governing permissions and limitations under
 *  the Licence.
 */
package eu.europeana.uim.enrichment;

import eu.europeana.corelib.definitions.edm.beans.FullBean;
import eu.europeana.corelib.definitions.edm.entity.ProvidedCHO;
import eu.europeana.corelib.definitions.jibx.EuropeanaType.Choice;
import eu.europeana.corelib.definitions.jibx.ProxyType;
import eu.europeana.corelib.definitions.jibx.RDF;
import eu.europeana.corelib.edm.exceptions.EdmFieldNotFoundException;
import eu.europeana.corelib.edm.exceptions.EdmValueNotFoundException;
import eu.europeana.corelib.edm.utils.EseEdmMap;
import eu.europeana.corelib.lookup.utils.HashUtils;
import eu.europeana.corelib.lookup.utils.PreSipCreatorUtils;
import eu.europeana.corelib.lookup.utils.SipCreatorUtils;
import eu.europeana.corelib.solr.bean.impl.FullBeanImpl;
import eu.europeana.corelib.solr.entity.AggregationImpl;
import eu.europeana.corelib.solr.entity.ProvidedCHOImpl;
import eu.europeana.corelib.solr.entity.ProxyImpl;
import eu.europeana.corelib.tools.lookuptable.EuropeanaId;
import eu.europeana.corelib.utils.EuropeanaUriUtils;
import eu.europeana.uim.common.TKey;
import eu.europeana.uim.enrichment.service.EnrichmentService;
import eu.europeana.uim.enrichment.utils.PropertyReader;
import eu.europeana.uim.enrichment.utils.UimConfigurationProperty;
import eu.europeana.uim.model.europeana.EuropeanaModelRegistry;
import eu.europeana.uim.model.europeana.EuropeanaRedirectId;
import eu.europeana.uim.orchestration.ExecutionContext;
import eu.europeana.uim.plugin.ingestion.AbstractIngestionPlugin;
import eu.europeana.uim.plugin.ingestion.CorruptedDatasetException;
import eu.europeana.uim.plugin.ingestion.IngestionPluginFailedException;
import eu.europeana.uim.store.MetaDataRecord;
import org.apache.commons.lang.StringUtils;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.CloudSolrServer;
import org.apache.solr.client.solrj.util.ClientUtils;
import org.apache.solr.common.SolrDocumentList;
import org.apache.solr.common.params.ModifiableSolrParams;
import org.jibx.runtime.BindingDirectory;
import org.jibx.runtime.IBindingFactory;
import org.jibx.runtime.IUnmarshallingContext;
import org.jibx.runtime.JiBXException;
import org.theeuropeanlibrary.model.common.qualifier.Status;

import java.io.StringReader;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Redirect creation plugin
 *
 * @author Yorgos.Mamakis@ kb.nl
 *
 * @param <I>
 */
public class LookupCreationPlugin<I> extends
        AbstractIngestionPlugin<MetaDataRecord<I>, I> {

    private static EnrichmentService enrichmentService;
    private static final Logger log = Logger
            .getLogger(LookupCreationPlugin.class.getName());
    private final static String OVERRIDESIPCREATOR = "override.sipcreator.field";
    private final static String USE_CUSTOM_FIELD = "redirect.use.custom.field";
    private final static String USE_FUNCTIONS = "redirect.use.custom.functions";
    private final static String CREATE_HASH = "redirect.field.create.hash";

    public LookupCreationPlugin(String name, String description) {
        super(name, description);

        // TODO Auto-generated constructor stub
    }

    public LookupCreationPlugin() {
        super("", "");

        // TODO Auto-generated constructor stub
    }

    private static String repository = PropertyReader
            .getProperty(UimConfigurationProperty.UIM_REPOSITORY);

    private static IBindingFactory bfact;

    static {
        try {
            // Should be placed in a static block for performance reasons
            bfact = BindingDirectory.getFactory(RDF.class);

        } catch (JiBXException e) {
            e.printStackTrace();
        }

    }

    private static List<String> params = new ArrayList<String>() {
        private static final long serialVersionUID = 1L;

        {
            add(OVERRIDESIPCREATOR);
            add(CREATE_HASH);
            add(USE_CUSTOM_FIELD);
            add(USE_FUNCTIONS);
        }
    };

    @Override
    public TKey<?, ?>[] getInputFields() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public TKey<?, ?>[] getOptionalFields() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public TKey<?, ?>[] getOutputFields() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void shutdown() {
        // TODO Auto-generated method stub

    }

    @Override
    public List<String> getParameters() {
        return params;
    }

    @Override
    public int getPreferredThreadCount() {
        // TODO Auto-generated method stub
        return 12;
    }

    @Override
    public int getMaximumThreadCount() {
        // TODO Auto-generated method stub
        return 15;
    }

    @Override
    public boolean process(MetaDataRecord<I> mdr,
            ExecutionContext<MetaDataRecord<I>, I> context)
            throws IngestionPluginFailedException, CorruptedDatasetException {
        IUnmarshallingContext uctx;

        try {
            List<Status> status = mdr.getValues(EuropeanaModelRegistry.STATUS);

            if (!(status != null && status.get(0).equals(Status.DELETED))) {
                String value = null;
                if (mdr.getValues(EuropeanaModelRegistry.EDMDEREFERENCEDRECORD)
                        != null
                        && mdr.getValues(
                                EuropeanaModelRegistry.EDMDEREFERENCEDRECORD)
                        .size() > 0) {
                    value = mdr.getValues(
                            EuropeanaModelRegistry.EDMDEREFERENCEDRECORD)
                            .get(0);
                } else {
                    value = mdr.getValues(EuropeanaModelRegistry.EDMRECORD)
                            .get(0);
                }
                 String fileName;
                 String collectionId = (String) mdr.getCollection()
                        .getMnemonic();
                    String oldCollectionId = enrichmentService
                            .getCollectionMongoServer().findOldCollectionId(
                                    collectionId);
                    if (oldCollectionId != null) {
                        fileName = oldCollectionId;
                    } else {
                        fileName = (String) mdr.getCollection().getName();
                    }
                uctx = bfact.createUnmarshallingContext();
                RDF rdf = (RDF) uctx.unmarshalDocument(new StringReader(value));
                
                if (context.getProperties().getProperty(OVERRIDESIPCREATOR)
                        == null
                        && context.getProperties()
                        .getProperty(USE_CUSTOM_FIELD) == null) {
                   

                    FullBeanImpl fullBean = constructFullBeanMock(rdf,
                            collectionId);
                    String hash = null;
                    try {
                        hash = hashExists(
                                collectionId,
                                fileName,
                                fullBean,
                                context.getProperties().getProperty(
                                        USE_FUNCTIONS));
                    } catch (Exception e) {
                        e.printStackTrace();
                        log.log(Level.SEVERE, e.getMessage());
                        return false;
                    }

                    if (StringUtils.isNotEmpty(hash)) {
                        EuropeanaId id = createLookupEntry(fullBean, fileName, collectionId, hash);

                        mdr.addValue(EuropeanaModelRegistry.EDMRECORDREDIRECT, true);
                        return true;
                    }
                } else {
                    String fieldValue = "";
                    if (context.getProperties().getProperty(OVERRIDESIPCREATOR)
                            != null) {
                    	boolean createHash = false;
                    	if(context.getProperties().getProperty(OVERRIDESIPCREATOR)
                            != null){
                    		createHash = true;
                    	}
                        if (context.getProperties()
                                .getProperty(OVERRIDESIPCREATOR)
                                .equalsIgnoreCase("edm:isShownAt")) {
                            fieldValue = rdf.getAggregationList().get(0)
                                    .getIsShownAt().getResource();
                        } else if (context.getProperties()
                                .getProperty(OVERRIDESIPCREATOR)
                                .equalsIgnoreCase("edm:isShownBy")) {
                            fieldValue = rdf.getAggregationList().get(0)
                                    .getIsShownBy().getResource();
                        } else if (context.getProperties()
                                .getProperty(OVERRIDESIPCREATOR)
                                .equalsIgnoreCase("dc:identifier")) {
                            ProxyType proxy = findProxy(rdf);
                            for (Choice choice : proxy.getChoiceList()) {
                                if (choice.ifIdentifier()) {
                                    fieldValue = choice.getIdentifier()
                                            .getString();
                                }
                            }
                        } else if (context.getProperties()
                                .getProperty(OVERRIDESIPCREATOR)
                                .equalsIgnoreCase("owl:sameAs")) {
                            fieldValue = rdf.getProvidedCHOList().get(0)
                                    .getSameAList().get(0).getResource();
                        }
                        EuropeanaId id = createLookupEntry(
                                rdf.getProvidedCHOList().get(0).getAbout(),
                                fileName,collectionId,
                                applyTransformations(
                                        createHash?HashUtils.createHash(fieldValue):fieldValue,
                                        context.getProperties().getProperty(
                                                USE_FUNCTIONS)));
                        if(id!=null) {

                            mdr.addValue(EuropeanaModelRegistry.EDMRECORDREDIRECT, true);
                        }
                    } else {
                        String edmValue = null;
                        if (context.getProperties()
                                .getProperty(USE_CUSTOM_FIELD)
                                .equalsIgnoreCase("edm:isShownAt")) {
                            fieldValue = rdf.getAggregationList().get(0)
                                    .getIsShownAt().getResource();
                            edmValue = "provider_aggregation_edm_isShownAt";
                        } else if (context.getProperties()
                                .getProperty(USE_CUSTOM_FIELD)
                                .equalsIgnoreCase("edm:isShownBy")) {
                            fieldValue = rdf.getAggregationList().get(0)
                                    .getIsShownBy().getResource();
                            edmValue = "provider_aggregation_edm_isShownBy";
                        } else if (context.getProperties()
                                .getProperty(USE_CUSTOM_FIELD)
                                .equalsIgnoreCase("dc:identifier")) {
                            ProxyType proxy = findProxy(rdf);
                            for (Choice choice : proxy.getChoiceList()) {
                                if (choice.ifIdentifier()) {
                                    fieldValue = choice.getIdentifier()
                                            .getString();
                                }
                            }
                            edmValue = "proxy_dc_identifier";
                        } else if (context.getProperties()
                                .getProperty(USE_CUSTOM_FIELD)
                                .equalsIgnoreCase("owl:sameAs")) {
                            fieldValue = rdf.getProvidedCHOList().get(0)
                                    .getSameAList().get(0).getResource();
                           edmValue = "proxy_owl_sameAs";
                        } else if (context.getProperties()
                                .getProperty(USE_CUSTOM_FIELD)
                                .equalsIgnoreCase("edm:object")) {
                            fieldValue = rdf.getAggregationList().get(0)
                                    .getObject().getResource();
                            edmValue = "provider_aggregation_edm_object";
                        }
                        EuropeanaId id = generateRedirectsFromCustomField(
                                fieldValue,
                                edmValue,
                                context.getProperties().getProperty(
                                        USE_FUNCTIONS), rdf.getProvidedCHOList().get(0).getAbout());
                        if(id!=null) {

                            //mdr.addValue(EuropeanaModelRegistry.EDMRECORDREDIRECT, true);
                            mdr.addValue(EuropeanaModelRegistry.EDMRECORDREDIRECTID,id.getOldId());
                        }
                    }

                }
            }
        } catch (JiBXException e) {
            log.log(Level.SEVERE, e.getMessage());
            return false;
        }
        return false;
    }

    private EuropeanaId generateRedirectsFromCustomField(String fieldValue,
            String property, String transformations, String newId) {

        try {
            String finalId = null;
            ModifiableSolrParams paramsOld = new ModifiableSolrParams();
            paramsOld.add(
                    "q",
                    property
                            + ":"
                            + ClientUtils
                            .escapeQueryChars(applyTransformations(
                                    fieldValue, transformations)));
            System.out.println(property
                    + ":"
                    + ClientUtils
                    .escapeQueryChars(applyTransformations(
                                    fieldValue, transformations)));
            CloudSolrServer solrServerProduction = enrichmentService
                    .getProductionCloudSolrServer();

            SolrDocumentList solrOldList = solrServerProduction.query(paramsOld).getResults();

            if (solrOldList.size() == 1) {
                finalId = solrOldList.get(0).getFirstValue("europeana_id")
                        .toString();
            }
            if (finalId != null && newId != null && !newId.equalsIgnoreCase(finalId)) {
                EuropeanaId id = new EuropeanaId();
                id.setOldId(finalId);
                id.setLastAccess(0);
                id.setTimestamp(new Date().getTime());
                id.setNewId(newId);
                saveEuropeanaId(id);
                return id;
            }
        } catch (SolrServerException e) {
            log.log(Level.SEVERE, e.getMessage());
        }
        return null;
    }

    private EuropeanaId createLookupEntry(String newId, String oldCollectionId, String newCollectionId,
            String value) {
        ModifiableSolrParams params = new ModifiableSolrParams();
        if (oldCollectionId.equals(newCollectionId)) {
            String finalId = EuropeanaUriUtils.createEuropeanaId(oldCollectionId,
                    value);
            params.add("q", "europeana_id:" + ClientUtils.escapeQueryChars(finalId));
            try {
                SolrDocumentList solrList = enrichmentService
                        .getProductionCloudSolrServer().query(params).getResults();
                if (solrList.size() > 0 && !(finalId.equals(newId))) {
                    EuropeanaId id = new EuropeanaId();
                    id.setOldId(finalId);
                    id.setLastAccess(0);
                    id.setTimestamp(new Date().getTime());
                    id.setNewId(newId);
                    saveEuropeanaId(id);
                    return id;
                }

            } catch (SolrServerException e) {
                log.log(Level.SEVERE, e.getMessage());
            }
        } else {
            String finalId = EuropeanaUriUtils.createEuropeanaId(oldCollectionId,
                    value);
            params.add("q", "europeana_id:" + ClientUtils.escapeQueryChars(finalId));
            try {
                SolrDocumentList solrList = enrichmentService
                        .getProductionCloudSolrServer().query(params).getResults();
                if (solrList.size() > 0 && !(finalId.equals(newId))) {
                    EuropeanaId id = new EuropeanaId();
                    id.setOldId(finalId);
                    id.setLastAccess(0);
                    id.setTimestamp(new Date().getTime());
                    id.setNewId(newId);
                    saveEuropeanaId(id);
                    return id;

                } else if(solrList.size()==0) {
                     finalId = EuropeanaUriUtils.createEuropeanaId(newCollectionId,
                    value);
                    params.set("q", "europeana_id:" + ClientUtils.escapeQueryChars(finalId));
                    if (solrList.size() > 0 && !(finalId.equals(newId))) {
                    EuropeanaId id = new EuropeanaId();
                    id.setOldId(finalId);
                    id.setLastAccess(0);
                    id.setTimestamp(new Date().getTime());
                    id.setNewId(newId);
                    saveEuropeanaId(id);
                    return id;

                }
                }

            } catch (SolrServerException e) {
                log.log(Level.SEVERE, e.getMessage());
            }
        }
        return null;
    }

    // Generate a minimum Fullbean
    private FullBeanImpl constructFullBeanMock(RDF rdf, String collectionId) {
        FullBeanImpl fBean = new FullBeanImpl();
        AggregationImpl aggr = new AggregationImpl();
        List<AggregationImpl> aggrs = new ArrayList<AggregationImpl>();
        aggr.setAbout(rdf.getAggregationList().get(0).getAbout());
        aggr.setEdmIsShownAt(rdf.getAggregationList().get(0).getIsShownAt()
                != null ? rdf
                .getAggregationList().get(0).getIsShownAt().getResource()
                : null);
        aggr.setEdmIsShownBy(rdf.getAggregationList().get(0).getIsShownBy()
                != null ? rdf
                .getAggregationList().get(0).getIsShownBy().getResource()
                : null);
        aggr.setEdmObject(rdf.getAggregationList().get(0).getObject() != null
                ? rdf
                .getAggregationList().get(0).getObject().getResource()
                : null);
        String[] hasView = new String[1];
        hasView[0] = rdf.getAggregationList().get(0).getObject() != null ? rdf
                .getAggregationList().get(0).getObject().getResource() : null;
        aggr.setHasView(hasView);
        aggrs.add(aggr);
        List<ProxyImpl> proxies = new ArrayList<ProxyImpl>();
        ProxyImpl proxy = new ProxyImpl();
        Map<String, List<String>> dcIdentifiers
                = new HashMap<String, List<String>>();
        ProxyType proxyRDF = findProxy(rdf);
        if (proxyRDF != null) {
            List<Choice> choices = proxyRDF.getChoiceList();
            List<String> val = new ArrayList<String>();
            for (Choice choice : choices) {
                if (choice.ifIdentifier()) {
                    val.add(choice.getIdentifier().getString());
                }
            }
            dcIdentifiers.put("def", val);
            proxy.setDcIdentifier(dcIdentifiers);
            proxies.add(proxy);
            fBean.setProxies(proxies);
        }
        fBean.setAggregations(aggrs);
        fBean.setAbout(rdf.getProvidedCHOList().get(0).getAbout());
        if (rdf.getProvidedCHOList().get(0).getSameAList() != null) {
            ProvidedCHO pCho = new ProvidedCHOImpl();
            pCho.setOwlSameAs(new String[]{rdf.getProvidedCHOList().get(0)
                .getSameAList().get(0).getResource()});
            List<ProvidedCHO> pChos = new ArrayList<ProvidedCHO>();
            pChos.add(pCho);
            fBean.setProvidedCHOs(pChos);
        }
        return fBean;
    }

    private ProxyType findProxy(RDF rdf) {
        for (ProxyType proxy : rdf.getProxyList()) {
            if (proxy.getEuropeanaProxy() == null
                    || !proxy.getEuropeanaProxy().isEuropeanaProxy()) {
                return proxy;
            }
        }
        return null;
    }

    @Override
    public void initialize(ExecutionContext<MetaDataRecord<I>, I> context)
            throws IngestionPluginFailedException {
        System.out.println(enrichmentService.getProductionCloudSolrServer().getZkStateReader().getClusterState().getLiveNodes());
    }

    @Override
    public void completed(ExecutionContext<MetaDataRecord<I>, I> context)
            throws IngestionPluginFailedException {

    }

    @Override
    public void initialize() {

    }

    private String hashExists(String collectionId, String fileName,
            FullBean fullBean, String transformations)
            throws EdmFieldNotFoundException, EdmValueNotFoundException,
            NullPointerException {
        SipCreatorUtils sipCreatorUtils = new SipCreatorUtils();
        sipCreatorUtils.setRepository(repository);
        String hashField = sipCreatorUtils.getHashField(fileName, fileName);
        if (hashField != null) {
            String val = null;

            val = EseEdmMap
                    .getEseEdmMap(
                            StringUtils.contains(hashField, "[") ? StringUtils.
                            substringBefore(
                                    hashField, "[")
                            : hashField, fullBean.getAbout())
                    .getEdmValue(fullBean);
            val = applyTransformations(val, transformations);
            if (val != null) {
                return HashUtils.createHash(val);
            }

        }
        PreSipCreatorUtils preSipCreatorUtils = new PreSipCreatorUtils();
        preSipCreatorUtils.setRepository(repository);

        if (preSipCreatorUtils.getHashField(fileName, fileName) != null) {
            String val = EseEdmMap.getEseEdmMap(
                    preSipCreatorUtils.getHashField(fileName, fileName),
                    fullBean.getAbout()).getEdmValue(fullBean);
            if (val != null) {
                return HashUtils.createHash(val);
            }
        }
        return null;
    }

    private String applyTransformations(String val, String transformations) {
        if (transformations == null) {
            return val;
        }
        String[] transforms = null;
        if (transformations.contains(").")) {
            transforms = StringUtils.split(transformations, ").");
        } else if (!transformations.contains(").") && transformations.endsWith(")")) {
            transforms = new String[]{transformations};
        }
        for (String transform : transforms) {
            if (!transform.endsWith(")")) {
                transform = transform + ")";
            }
            if (StringUtils.startsWith(transform, "replace")) {
                String[] replacements = StringUtils.split(
                        StringUtils.substringBetween(transform, "(", ")"),
                        ",");
                val = StringUtils.replace(val, replacements[0],
                        replacements[1]);
            }
            if (StringUtils.startsWith(transform, "substringBetween(")) {
                String[] replacements = StringUtils.split(
                        StringUtils.substringBetween(transform, "(", ")"),
                        ",");
                val = StringUtils.substringBetween(val, replacements[0],
                        replacements[1]);
            }
            if (StringUtils.startsWith(transform, "substringBeforeFirst(")) {
                String replacements = StringUtils.substringBetween(
                        transform, "(", ")");
                val = StringUtils.substringBefore(val, replacements);
            }
            if (StringUtils.startsWith(transform, "substringBeforeLast(")) {
                String replacements = StringUtils.substringBetween(
                        transform, "(", ")");
                val = StringUtils.substringBeforeLast(val, replacements);
            }
            if (StringUtils.startsWith(transform, "substringAfterLast(")) {
                String replacements = StringUtils.substringBetween(
                        transform, "(", ")");
                val = StringUtils.substringAfterLast(val, replacements);
            }
            if (StringUtils.startsWith(transform, "substringAfterFirst(")) {
                String replacements = StringUtils.substringBetween(
                        transform, "(", ")");
                val = StringUtils.substringAfter(val, replacements);
            }
            if (StringUtils.startsWith(transform, "concatBefore(")) {
                String replacements = StringUtils.substringBetween(
                        transform, "(", ")");
                val = replacements + val;
            }
            if (StringUtils.startsWith(transform, "concatAfter(")) {
                String replacements = StringUtils.substringBetween(
                        transform, "(", ")");
                val = val + replacements;
            }

        }

        return val;
    }

    private void saveEuropeanaId(EuropeanaId europeanaId) {
        enrichmentService.getEuropeanaIdMongoServer().saveEuropeanaId(
                europeanaId);
    }

    private EuropeanaId createLookupEntry(FullBean fullBean, String oldCollectionId, String newCollectionId,
            String hash) {

        ModifiableSolrParams params = new ModifiableSolrParams();
        params.add(
                "q",
                "europeana_id:"
                + ClientUtils.escapeQueryChars("/" + oldCollectionId + "/"
                        + hash));
        if (oldCollectionId.equals(newCollectionId)) {
            try {
                SolrDocumentList solrList = enrichmentService
                        .getProductionCloudSolrServer().query(params).getResults();
                if (solrList.size() > 0) {
                    EuropeanaId id = new EuropeanaId();
                    id.setOldId("/" + oldCollectionId + "/" + hash);
                    id.setLastAccess(0);
                    id.setTimestamp(new Date().getTime());
                    id.setNewId(fullBean.getAbout());
                    saveEuropeanaId(id);
                    return id;

                }

            } catch (SolrServerException e) {
                log.log(Level.SEVERE, e.getMessage());
            }
        } else {
            try {
                SolrDocumentList solrList = enrichmentService
                        .getProductionCloudSolrServer().query(params).getResults();
                if (solrList.size() > 0) {
                    EuropeanaId id = new EuropeanaId();
                    id.setOldId("/" + oldCollectionId + "/" + hash);
                    id.setLastAccess(0);
                    id.setTimestamp(new Date().getTime());
                    id.setNewId(fullBean.getAbout());
                    saveEuropeanaId(id);
                    return id;

                } else {
                    EuropeanaId id = new EuropeanaId();
                    id.setOldId("/" + newCollectionId + "/" + hash);
                    id.setLastAccess(0);
                    id.setTimestamp(new Date().getTime());
                    id.setNewId(fullBean.getAbout());
                    saveEuropeanaId(id);
                    return id;
                }

            } catch (SolrServerException e) {
                log.log(Level.SEVERE, e.getMessage());
            }
        }
        return null;
    }

    public void setEnrichmentService(EnrichmentService service) {
        LookupCreationPlugin.enrichmentService = service;
    }
}
