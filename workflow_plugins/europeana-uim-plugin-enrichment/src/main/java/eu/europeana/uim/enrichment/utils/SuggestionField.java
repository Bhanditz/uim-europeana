package eu.europeana.uim.enrichment.utils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrInputDocument;

public enum SuggestionField {

	EUROPEANA_ID("europeana_id"),
	PROXY_DC_CONTRIBUTOR("proxy_dc_contributor."),
	PROXY_DC_CREATOR("proxy_dc_creator."),
	AG_SKOS_PREFLABEL("ag_skos_prefLabel."),
	PROXY_DC_COVERAGE("proxy_dc_coverage."),
	PROXY_DCTERMS_SPATIAL("proxy_dcterms_spatial"),
	PL_SKOS_PREFLABEL("pl_skos_prefLabel."),
	PROXY_DC_TYPE("proxy_dc_type."),
	PROXY_DC_SUBJECT("proxy_dc_subject."),
	CC_SKOS_PREFLABEL("cc_skos_prefLabel."),
	CC_SKOS_BROADER("cc_skos_broader."),
	PROXY_DCTERMS_CREATED("proxy_dcterms_created."),
	PROXY_DCTERMS_TEMPORAL("proxy_dcterms_temporal."),
	PROXY_DC_DATE("proxy_dc_date."),
	TS_SKOS_PREFLABEL("ts_skos_prefLabel."),
	PROXY_DC_TITLE("proxy_dc_title."),
	PROXY_DCTERMS_ALTERNATIVE("proxy_dcterms_alternative.");
	
	private String field;
	
	private SuggestionField(String field){
		this.field = field;
	}
	
	
	public SolrInputDocument transferField(SolrInputDocument input, SolrInputDocument output){
		Collection<String> fieldNames = input.getFieldNames();
		List<String> fieldNamesFound = new ArrayList<String>();
		for(String fieldName : fieldNames){
			if(StringUtils.startsWith(fieldName, field)){
				fieldNamesFound.add(fieldName);
			}
		}
		
		for(String fieldName: fieldNamesFound){
			output.setField(fieldName, input.getFieldValue(fieldName));
		}
		return output;
	}
	
}
