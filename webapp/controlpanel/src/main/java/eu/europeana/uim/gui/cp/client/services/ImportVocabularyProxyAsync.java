package eu.europeana.uim.gui.cp.client.services;

import java.util.List;

import com.google.gwt.user.client.rpc.AsyncCallback;

import eu.europeana.uim.gui.cp.shared.ControlledVocabularyDTO;
import eu.europeana.uim.gui.cp.shared.EdmFieldDTO;
import eu.europeana.uim.gui.cp.shared.MappingDTO;
/**
 * Interface used for importing Controlled vocabularies (Async)
 * @author Yorgos.Mamakis@ kb.nl
 *
 */
public interface ImportVocabularyProxyAsync {
	public void importVocabulary(ControlledVocabularyDTO vocabulary, AsyncCallback<ControlledVocabularyDTO> mapping);
	public void mapField(String originalField,String mappedField,AsyncCallback<MappingDTO> result);
	public void saveMapping(int iterations,String[] vocabularyRules, String vocabularyURL,AsyncCallback<Boolean> result);
	public void retrieveVocabularies(AsyncCallback<List<ControlledVocabularyDTO>> vocabularies);
	public void retrieveEdmFields( AsyncCallback<List<EdmFieldDTO>> edmFields);
	public void removeVocabulary(String vocabularyName, AsyncCallback<Boolean> result);
	public void setVocabulary(String vocabularyName, String vocabularyUri, AsyncCallback<Boolean> result);
}
