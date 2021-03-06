package eu.europeana.uim.gui.cp.shared.validation;

import com.google.gwt.user.client.rpc.IsSerializable;
import com.google.gwt.view.client.ProvidesKey;

public class FailedRecordDTO implements IsSerializable{

	/**
	 * The generated europeana ID
	 */
	private String europeanaId;
	
	/**
	 * The original provider's id
	 */
	private String originalId;
	
	/**
	 * The collection Id
	 */
	private String collectionId;
	
	/**
	 * The reason it failed to ingest
	 */
	private String lookupState;
	
	
	/**
	 * Extra message for failure reason
	 */
	private String message;
	
	
	/**
	 * Ingestion Failure Date
	 */
	private String date;
	
	/**
	 * The resulting EDM
	 */
	private String edm;

    public static final ProvidesKey<FailedRecordDTO> KEY_PROVIDER = new ProvidesKey<FailedRecordDTO>() {
        @Override
        public Object getKey(
                FailedRecordDTO item) {
            return item == null
                    ? null
                    : item.getOriginalId();
        }
    }; 
    
	public String getEuropeanaId() {
		return europeanaId;
	}

	public void setEuropeanaId(String europeanaId) {
		this.europeanaId = europeanaId;
	}

	public String getOriginalId() {
		return originalId;
	}

	public void setOriginalId(String originalId) {
		this.originalId = originalId;
	}

	public String getCollectionId() {
		return collectionId;
	}

	public void setCollectionId(String collectionId) {
		this.collectionId = collectionId;
	}

	public String getLookupState() {
		return lookupState;
	}

	public void setLookupState(String lookupState) {
		this.lookupState = lookupState;
	}

	public String getEdm() {
		return edm;
	}

	public void setEdm(String edm) {
		this.edm = edm;
	}

	/**
	 * @return the date
	 */
	public String getDate() {
		return date;
	}

	/**
	 * @param date the date to set
	 */
	public void setDate(String date) {
		this.date = date;
	}

	/**
	 * @return the message
	 */
	public String getMessage() {
		return message;
	}

	/**
	 * @param message the message to set
	 */
	public void setMessage(String message) {
		this.message = message;
	}
	
	

}
