package eu.europeana.uim.enrichment.utils;

import java.util.ArrayList;
import java.util.Collection;

import org.apache.solr.common.SolrInputDocument;

import eu.europeana.corelib.definitions.model.EdmLabel;
import eu.europeana.corelib.solr.entity.ConceptImpl;
/*
	CC_SKOS_BROADER("cc_skos_broader", SolrType.NOT_STORED), 
	CC_SKOS_NARROWER("cc_skos_narrower", SolrType.NOT_STORED),
	CC_SKOS_RELATED("cc_skos_related", SolrType.NOT_STORED),
	CC_SKOS_BROADMATCH("cc_skos_broadMatch", SolrType.NOT_STORED),
	CC_SKOS_NARROWMATCH("cc_skos_narrowMatch", SolrType.NOT_STORED),
	CC_SKOS_RELATEDMATCH("cc_skos_relatedMatch", SolrType.NOT_STORED),
	CC_SKOS_EXACTMATCH("cc_skos_exactMatch", SolrType.NOT_STORED),
	CC_SKOS_CLOSEMATCH("cc_skos_closeMatch", SolrType.NOT_STORED),
	CC_SKOS_NOTATIONS("cc_skos_notation", SolrType.NOT_STORED),
	CC_SKOS_INSCHEME("cc_skos_inScheme", SolrType.NOT_STORED),
 */
public class ConceptSolrCreator {

	public void create(SolrInputDocument doc, ConceptImpl concept){
		Collection<Object> values = doc.getFieldValues(
				EdmLabel.SKOS_CONCEPT.toString());
		if (values == null) {
			values = new ArrayList<Object>();
		}
		values.add(concept.getAbout());
		doc.setField(EdmLabel.SKOS_CONCEPT.toString(), values);

		if (concept.getPrefLabel() != null) {
			for (String key : concept.getPrefLabel().keySet()) {
				values = doc.getFieldValues(
						EdmLabel.CC_SKOS_PREF_LABEL.toString() + "." + key);
				if (values == null) {
					values = new ArrayList<Object>();
				}
				values.addAll(concept.getPrefLabel().get(key));
				doc.setField(
						EdmLabel.CC_SKOS_PREF_LABEL.toString() + "." + key,
						values);
			}
		}
		if (concept.getAltLabel() != null) {
			for (String key : concept.getAltLabel().keySet()) {
				values = doc.getFieldValues(
						EdmLabel.CC_SKOS_ALT_LABEL.toString() + "." + key);
				if (values == null) {
					values = new ArrayList<Object>();
				}
				values.addAll(concept.getAltLabel().get(key));
				doc.setField(EdmLabel.CC_SKOS_ALT_LABEL.toString() + "." + key,
						values);
			}
		}
		
		if (concept.getNote() != null) {
			for (String key : concept.getNote().keySet()) {
				values = doc.getFieldValues(
						EdmLabel.CC_SKOS_NOTE.toString());
				if (values == null) {
					values = new ArrayList<Object>();
				}
				values.addAll(concept.getNote().get(key));
				doc.setField(EdmLabel.CC_SKOS_NOTE.toString(),
						values);
			}
		}
		
		if (concept.getPrefLabel() != null) {
			for (String key : concept.getHiddenLabel().keySet()) {
				values = doc.getFieldValues(
						EdmLabel.CC_SKOS_HIDDEN_LABEL.toString() + "." + key);
				if (values == null) {
					values = new ArrayList<Object>();
				}
				values.addAll(concept.getHiddenLabel().get(key));
				doc.setField(
						EdmLabel.CC_SKOS_HIDDEN_LABEL.toString() + "." + key,
						values);
			}
		}
		
		
		if (concept.getBroader() != null) {
			for (String key : concept.getBroader()) {
				values = doc.getFieldValues(
						EdmLabel.CC_SKOS_BROADER.toString());
				if (values == null) {
					values = new ArrayList<Object>();
				}
				values.add(key);
				doc.setField(EdmLabel.CC_SKOS_BROADER.toString(),
						values);
			}
		}

		if (concept.getBroadMatch() != null) {
			for (String key : concept.getBroadMatch()) {
				values = doc.getFieldValues(
						EdmLabel.CC_SKOS_BROADMATCH.toString());
				if (values == null) {
					values = new ArrayList<Object>();
				}
				values.add(key);
				doc.setField(EdmLabel.CC_SKOS_BROADMATCH.toString(),
						values);
			}
		}
		

		if (concept.getNarrower() != null) {
			for (String key : concept.getNarrower()) {
				values = doc.getFieldValues(
						EdmLabel.CC_SKOS_NARROWER.toString());
				if (values == null) {
					values = new ArrayList<Object>();
				}
				values.add(key);
				doc.setField(EdmLabel.CC_SKOS_NARROWER.toString(),
						values);
			}
		}
		

		if (concept.getRelated() != null) {
			for (String key : concept.getRelated()) {
				values = doc.getFieldValues(
						EdmLabel.CC_SKOS_RELATED.toString());
				if (values == null) {
					values = new ArrayList<Object>();
				}
				values.add(key);
				doc.setField(EdmLabel.CC_SKOS_RELATED.toString(),
						values);
			}
		}
		

		if (concept.getNarrowMatch() != null) {
			for (String key : concept.getNarrowMatch()) {
				values = doc.getField(
						EdmLabel.CC_SKOS_NARROWMATCH.toString())
						.getValues();
				if (values == null) {
					values = new ArrayList<Object>();
				}
				values.add(key);
				doc.setField(EdmLabel.CC_SKOS_NARROWMATCH.toString(),
						values);
			}
		}
		

		if (concept.getRelatedMatch() != null) {
			for (String key : concept.getRelatedMatch()) {
				values = doc.getFieldValues(
						EdmLabel.CC_SKOS_RELATEDMATCH.toString());
				if (values == null) {
					values = new ArrayList<Object>();
				}
				values.add(key);
				doc.setField(EdmLabel.CC_SKOS_RELATEDMATCH.toString(),
						values);
			}
		}
		


		if (concept.getExactMatch() != null) {
			for (String key : concept.getExactMatch()) {
				values = doc.getFieldValues(
						EdmLabel.CC_SKOS_EXACTMATCH.toString());
				if (values == null) {
					values = new ArrayList<Object>();
				}
				values.add(key);
				doc.setField(EdmLabel.CC_SKOS_EXACTMATCH.toString(),
						values);
			}
		}
		


		if (concept.getCloseMatch() != null) {
			for (String key : concept.getCloseMatch()) {
				values = doc.getFieldValues(
						EdmLabel.CC_SKOS_CLOSEMATCH.toString());
				if (values == null) {
					values = new ArrayList<Object>();
				}
				values.add(key);
				doc.setField(EdmLabel.CC_SKOS_CLOSEMATCH.toString(),
						values);
			}
		}
		

		if (concept.getNotation() != null) {
			for (String key : concept.getNotation().keySet()) {
				values = doc.getFieldValues(
						EdmLabel.CC_SKOS_NOTATIONS.toString());
				if (values == null) {
					values = new ArrayList<Object>();
				}
				values.addAll(concept.getNotation().get(key));
				doc.setField(EdmLabel.CC_SKOS_NOTATIONS.toString(),
						values);
			}
		}
		


		if (concept.getInScheme() != null) {
			for (String key : concept.getInScheme()) {
				values = doc.getFieldValues(
						EdmLabel.CC_SKOS_INSCHEME.toString());
				if (values == null) {
					values = new ArrayList<Object>();
				}
				values.add(key);
				doc.setField(EdmLabel.CC_SKOS_INSCHEME.toString(),
						values);
			}
		}
	}
}
