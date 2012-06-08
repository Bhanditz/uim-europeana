package eu.europeana.uim.gui.cp.server;

import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;

import com.mongodb.Mongo;
import com.mongodb.MongoException;

import eu.europeana.corelib.definitions.model.EdmLabel;
import eu.europeana.corelib.dereference.ControlledVocabulary;
import eu.europeana.corelib.dereference.impl.ControlledVocabularyImpl;
import eu.europeana.corelib.dereference.impl.Extractor;
import eu.europeana.corelib.dereference.impl.VocabularyMongoServer;
import eu.europeana.uim.gui.cp.client.services.ImportVocabularyProxy;
import eu.europeana.uim.gui.cp.shared.ControlledVocabularyDTO;
import eu.europeana.uim.gui.cp.shared.EdmFieldDTO;
import eu.europeana.uim.gui.cp.shared.MappingDTO;
import eu.europeana.uim.gui.cp.shared.OriginalFieldDTO;

public class ImportVocabularyProxyImpl extends
IntegrationServicesProviderServlet implements ImportVocabularyProxy {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private static ControlledVocabulary controlledVocabulary;

	private static Extractor extractor;
	private static VocabularyMongoServer mongo;
	
	{
		try {
			mongo = new VocabularyMongoServer(new Mongo("127.0.0.1", 27017), "vocabulary");
			
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (MongoException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	@Override
	public ControlledVocabularyDTO importVocabulary(
			ControlledVocabularyDTO vocabulary) {
		controlledVocabulary = new ControlledVocabularyImpl(
				vocabulary.getName());
		
		controlledVocabulary.setURI(vocabulary.getUri());
		controlledVocabulary.setLocation(vocabulary.getLocation());
		if (StringUtils.isNotEmpty(vocabulary.getSuffix())) {
			controlledVocabulary.setSuffix(vocabulary.getSuffix());
		}
		
			
		extractor = new Extractor(controlledVocabulary,mongo);
		vocabulary.setMapping(convertEdmMap(extractor
				.readSchema(vocabulary.getLocation())));
		return vocabulary;
	}

	public MappingDTO mapField(String originalField, String mappedField) {
	
		extractor = new Extractor(controlledVocabulary,mongo);
		extractor.setMappedField(originalField, EdmLabel.getEdmLabel(mappedField));
		OriginalFieldDTO originalFieldDTO = new OriginalFieldDTO();
		originalFieldDTO.setField(originalField);
		EdmFieldDTO edmFieldDTO = new EdmFieldDTO();
		edmFieldDTO.setField(mappedField);
		MappingDTO mapping = new MappingDTO();
		mapping.setMapped(edmFieldDTO);
		mapping.setOriginal(originalFieldDTO);
		return mapping;
	}

	public boolean saveMapping() {
		if (extractor != null) {
			extractor.saveMapping();
			return true;
		}
		return false;
	}

	private List<MappingDTO> convertEdmMap(Map<String, EdmLabel> readSchema) {
		List<MappingDTO> returnMap = new ArrayList<MappingDTO>();
		
		for (String key : readSchema.keySet()) {
			MappingDTO map = new MappingDTO();
			OriginalFieldDTO original = new OriginalFieldDTO();
			original.setField(key);
			map.setOriginal(original);
			EdmFieldDTO edmField = new EdmFieldDTO();
			edmField.setField(StringUtils
					.isNotEmpty(readSchema.get(key).toString()) ? readSchema
					.get(key).toString() : null);
			map.setMapped(edmField);
			returnMap.add(map);
		}
		return returnMap;
	}

	@Override
	public List<ControlledVocabularyDTO> retrieveVocabularies() {
		extractor = new Extractor(controlledVocabulary,mongo);
		List<ControlledVocabularyImpl> controlledVocabularies = new ArrayList<ControlledVocabularyImpl>();
		try{
			controlledVocabularies = extractor.getControlledVocabularies();
		}
		catch(Exception e){
			e.printStackTrace();
		}
		List<ControlledVocabularyDTO> vocabularyDTOs = new ArrayList<ControlledVocabularyDTO>();
		for (ControlledVocabularyImpl controlledVocabulary : controlledVocabularies) {
			ControlledVocabularyDTO vocabularyDTO = new ControlledVocabularyDTO();
			vocabularyDTO.setName(controlledVocabulary.getName());
			vocabularyDTO.setUri(controlledVocabulary.getURI());
			vocabularyDTO.setLocation(controlledVocabulary.getLocation());
			vocabularyDTO.setSuffix(controlledVocabulary.getSuffix());
			vocabularyDTO.setMapping(convertEdmMap(controlledVocabulary
					.getElements()));
			vocabularyDTOs.add(vocabularyDTO);
		}
		return vocabularyDTOs;
	}

	@Override
	public List<EdmFieldDTO> retrieveEdmFields() {
		List<EdmFieldDTO> edmFields = new ArrayList<EdmFieldDTO>();
		for(EdmLabel edmField : EdmLabel.values()){
			EdmFieldDTO edmFieldDTO = new EdmFieldDTO();
			edmFieldDTO.setField(edmField.toString());
			edmFields.add(edmFieldDTO);
		}
		return edmFields;
	}

	@Override
	public boolean removeVocabulary(String vocabularyName) {
		if (extractor != null) {
		extractor.removeVocabulary(vocabularyName);
		return true;
		}
		return false;
	}

}
