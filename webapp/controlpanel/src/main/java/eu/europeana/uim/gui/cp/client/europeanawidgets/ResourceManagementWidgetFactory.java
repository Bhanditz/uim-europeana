/**
 * 
 */
package eu.europeana.uim.gui.cp.client.europeanawidgets;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.gwt.user.client.ui.TabLayoutPanel;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;

import eu.europeana.uim.gui.cp.client.services.IntegrationSeviceProxyAsync;
import eu.europeana.uim.gui.cp.client.utils.EuropeanaClientConstants;
import eu.europeana.uim.gui.cp.client.utils.RepoxOperationType;
import eu.europeana.uim.gui.cp.shared.IntegrationStatusDTO;
import eu.europeana.uim.gui.cp.shared.RepoxExecutionStatusDTO;
import eu.europeana.uim.gui.cp.shared.IntegrationStatusDTO.TYPE;

/**
 * 
 * 
 * @author Georgios Markakis
 */
public class ResourceManagementWidgetFactory {

	
    final IntegrationSeviceProxyAsync integrationservice;
	
	public ResourceManagementWidgetFactory(IntegrationSeviceProxyAsync integrationservice){
		this.integrationservice = integrationservice;
	}
	

	/**
	 * Creates the integration info sub panels displaying integration-specific information
	 * 
	 * @param status
	 */
	public void generateIntergationInfoPanel(TabLayoutPanel tabInfoSubPanel,IntegrationStatusDTO status,
			IntegrationSeviceProxyAsync integrationservice){

		if(status.getType().equals(TYPE.UNIDENTIFIED)){
			tabInfoSubPanel.setVisible(false);
		}
		else{
			tabInfoSubPanel.setVisible(true);
			tabInfoSubPanel.clear();
			
			tabInfoSubPanel.add(createGeneralInfoTabContent(status), new HTML("Resource Info"));
			tabInfoSubPanel.add(createIntrgrationInfoTabContent(status), new HTML("Integration Info"));
			
			if(status.getSugarCRMID() != null){
			tabInfoSubPanel.add(createSugarCrmTabContent(status), new HTML("SugarCRM"));
			}
			
			if(status.getRepoxID() != null){
				tabInfoSubPanel.add(createRepoxTabContent(status), new HTML("Repox"));
			}

			tabInfoSubPanel.add(createResourcePropertiesTabContent(status), new HTML("Resource Properties"));
		}


		
	}
	
	
	/**
	 * @param status
	 * @return
	 */
	private  ScrollPanel createGeneralInfoTabContent(IntegrationStatusDTO status){


		ScrollPanel generalInfoContainer =new ScrollPanel();
		FlexTable generalInfoTable = new FlexTable();
		generalInfoContainer.add(generalInfoTable);
				
		
	    	generalInfoTable.setWidget(0, 0, new HTML("Type:"));
	    	generalInfoTable.setWidget(0, 1, new HTML(status.getType().toString()));
	    	
	    	generalInfoTable.setWidget(1, 0, new HTML("Name:"));          	
	    	generalInfoTable.setWidget(1, 1, new HTML(status.getInfo()));
	    	
	    	generalInfoTable.setWidget(2, 0, new HTML("Identifier:"));
	    	generalInfoTable.setWidget(2, 1, new HTML(status.getId()));
	    	
	    	generalInfoTable.setWidget(2, 0, new HTML("Description:"));
	    	generalInfoTable.setWidget(2, 1, new HTML(status.getDescription()));
	    	
	    	
	    	switch(status.getType()){
			
			case COLLECTION:

		    	generalInfoTable.setWidget(3, 0, new HTML("Processing Phase:"));
		    	generalInfoTable.setWidget(3, 1, new HTML(status.getState()));
		    	
		    	
		    	generalInfoTable.setWidget(4, 0, new HTML("Metadata Format:"));
		    	generalInfoTable.setWidget(4, 1, new HTML(status.getResourceProperties().get("METADATA_FORMAT")));
				
		    	generalInfoTable.setWidget(5, 0, new HTML("Is Collection Deleted:"));
		    	generalInfoTable.setWidget(5, 1, new HTML(status.getResourceProperties().get("DELETED")));
		    	
		    	generalInfoTable.setWidget(6, 0, new HTML("Is Collection Published:"));
		    	generalInfoTable.setWidget(6, 1, new HTML(status.getResourceProperties().get("ENABLED")));
		    	
		    	
				break;
				
			case PROVIDER:
				
		    	generalInfoTable.setWidget(3, 0, new HTML("Provider Website:"));
		    	generalInfoTable.setWidget(3, 1, new HTML(status.getResourceProperties().get("PROVIDERWEBSITE")));
		    	
		    	generalInfoTable.setWidget(4, 0, new HTML("Provider Country:"));
		    	generalInfoTable.setWidget(4, 1, new HTML(status.getResourceProperties().get("PROVIDERCOUNTRY")));

		    	generalInfoTable.setWidget(5, 0, new HTML("Provider Type:"));
		    	generalInfoTable.setWidget(5, 1, new HTML(status.getResourceProperties().get("PROVIDERTYPE")));
			  }

	    	
		return generalInfoContainer;

	}
	
	
	
	/**
	 * @param status
	 * @return
	 */
	private  ScrollPanel createIntrgrationInfoTabContent(IntegrationStatusDTO status){

		ScrollPanel container =new ScrollPanel();

		FlexTable resourcePropertiesTable = new FlexTable();
		container.add(resourcePropertiesTable);
		
		resourcePropertiesTable.setWidget(3, 0, new HTML("SugarCRM:"));
    	
    	if(status.getSugarCRMID() == null){
    		resourcePropertiesTable.setWidget(3, 1, new Image(EuropeanaClientConstants.ERRORIMAGELOC)); 
    	}
    	else{
    		
    		resourcePropertiesTable.setWidget(3, 1, new Image(EuropeanaClientConstants.SUCCESSIMAGELOC)); 
   		
    	}
    	
    	resourcePropertiesTable.setWidget(4, 0, new HTML("Repox:"));
    	
    	if(status.getRepoxID() == null){
    		resourcePropertiesTable.setWidget(4, 1, new Image(EuropeanaClientConstants.ERRORIMAGELOC)); 
    	}
    	else{
    		resourcePropertiesTable.setWidget(4, 1, new Image(EuropeanaClientConstants.SUCCESSIMAGELOC)); 
    	}
		
		return container;
	}

	
	/**
	 * @param status
	 * @returncreateResourcePropertiesTabContent
	 */
	private  ScrollPanel createResourcePropertiesTabContent(IntegrationStatusDTO status){
		
		
		ScrollPanel container =new ScrollPanel();

		FlexTable resourcePropertiesTable = new FlexTable();
		container.add(resourcePropertiesTable);
		
		HashMap<String,String> propertiesMap =  status.getResourceProperties();
		int i =0;
		
		Iterator it = propertiesMap.entrySet().iterator();
	    while (it.hasNext()) {
	        Map.Entry pairs = (Map.Entry)it.next();
	        
	        resourcePropertiesTable.setWidget(i, 0, new HTML(pairs.getKey().toString()));
	        resourcePropertiesTable.setWidget(i, 1, new HTML(pairs.getValue().toString()));
	   
	        it.remove(); // avoids a ConcurrentModificationException
	        i++;
	    }
	    
		return container;
	}
	
	
	
	/**
	 * @param status
	 * @return
	 */
	private  ScrollPanel createSugarCrmTabContent(IntegrationStatusDTO status){
		ScrollPanel container =new ScrollPanel();

		FlexTable resourcePropertiesTable = new FlexTable();
		container.add(resourcePropertiesTable);
		
    	if(status.getSugarCRMID() == null){
    		resourcePropertiesTable.setWidget(0, 1, new HTML("Not represented in SugarCRM. " +
    				"Was this resource created manually? Please use the 'Import Resources' Wizard to create it")); 
    	}
    	else{
    		
    		resourcePropertiesTable.setWidget(1, 0, new HTML("ID:"));
    		resourcePropertiesTable.setWidget(1, 1, new HTML(status.getSugarCRMID()));
    		resourcePropertiesTable.setWidget(2, 0, new HTML("Link:"));
    		
			Anchor hyper = new Anchor();
			hyper.setName("");
			hyper.setText(status.getSugarURL());
			hyper.setHref(status.getSugarURL());
			hyper.setTarget("NEW");
			resourcePropertiesTable.setWidget(2, 1, hyper);
   		
    		resourcePropertiesTable.setWidget(3, 0, new HTML("Dataset Status:"));
    		resourcePropertiesTable.setWidget(3, 1, new HTML(status.getState()));
			
    		
    		resourcePropertiesTable.setWidget(3, 0, new HTML("Last Updated:"));
    		resourcePropertiesTable.setWidget(3, 1, new HTML(status.getResourceProperties().get("DATE_ENTERED")));
    		
    	}
		
		
		return container;
	}
	
	
	
	
	/**
	 * @param status
	 * @return
	 */
	private  ScrollPanel createRepoxTabContent(IntegrationStatusDTO status){
		ScrollPanel container =new ScrollPanel();

		FlexTable resourcePropertiesTable = new FlexTable();
		container.add(resourcePropertiesTable);
		
		resourcePropertiesTable.setWidget(0, 0, new HTML("ID:"));
		resourcePropertiesTable.setWidget(0, 1, new HTML(status.getRepoxID()));
		
		resourcePropertiesTable.setWidget(4, 0, new HTML("Link:"));
    	
    	if(status.getRepoxID() == null){
    		resourcePropertiesTable.setWidget(4, 1, new HTML("Not represented in Repox")); 
    	}
    	else{
    		
			Anchor hyper = new Anchor();
			hyper.setName("RepoxLink");
			hyper.setText(status.getRepoxURL());
			hyper.setHref(status.getRepoxURL());
			hyper.setTarget("NEW");
			
			resourcePropertiesTable.setWidget(4, 1,hyper);  
    	}
		
		
		switch(status.getType()){
		
		case COLLECTION:

			resourcePropertiesTable.setWidget(5, 0, new HTML("Harvesting Type:"));
			String htype = status.getResourceProperties().get("HARVESTING_TYPE");
			resourcePropertiesTable.setWidget(5, 1, new HTML(htype));   
			
			resourcePropertiesTable.setWidget(6, 0, new HTML("Harvesting Status:"));
			resourcePropertiesTable.setWidget(6, 1, new HTML(status.getHarvestingStatus().getStatus().getDescription()));  
			
			
			resourcePropertiesTable.setWidget(7, 0, new HTML("Percentage complete:"));
			resourcePropertiesTable.setWidget(7, 1, new HTML(status.getHarvestingStatus().getPercentage()));  
			
			resourcePropertiesTable.setWidget(8, 0, new HTML("Number of records:"));
			resourcePropertiesTable.setWidget(8, 1, new HTML(status.getHarvestingStatus().getNoRecords()));  
			
			resourcePropertiesTable.setWidget(9, 0, new HTML("Time Left:"));
			resourcePropertiesTable.setWidget(9, 1, new HTML(status.getHarvestingStatus().getTimeleft()));  
			
			if(htype.equals("oai_pmh")){
				resourcePropertiesTable.setWidget(10, 0, new HTML("Harvest URL:"));
				resourcePropertiesTable.setWidget(10, 1, new HTML(status.getResourceProperties().get("HARVEST_URL")));   
				
				resourcePropertiesTable.setWidget(11, 0, new HTML("Metadata Namespace:"));
				resourcePropertiesTable.setWidget(11, 1, new HTML(status.getResourceProperties().get("METADATA_SCHEMA")));   
				
				resourcePropertiesTable.setWidget(12, 0, new HTML("Metadata Schema:"));
				resourcePropertiesTable.setWidget(12, 1, new HTML(status.getResourceProperties().get("METADATA_SCHEMAL")));   
				
				resourcePropertiesTable.setWidget(13, 0, new HTML("Setspec:"));
				resourcePropertiesTable.setWidget(13, 1, new HTML(status.getResourceProperties().get("SETSPEC")));   
			}
			
			
			
			
 
			
			
			
			
			
			resourcePropertiesTable.setWidget(14, 0, new HTML("<hr></hr>"));           		    	
			resourcePropertiesTable.setWidget(14, 0, new HTML("Permitted operations:"));
		
			GeneralInfoCommandMenu command = this.new GeneralInfoCommandMenu(status);
			
			resourcePropertiesTable.setWidget(14, 1, command.getOperationsListBox());
			// Create a table to layout the content

			
			resourcePropertiesTable.setWidget(14, 2, command.generateRepoxCommandButton());
			
			break;
			
		case PROVIDER:

		  }

		return container;
	}
	
	
	
	
	private  ScrollPanel createMintTabContent(IntegrationStatusDTO status){
		ScrollPanel container =new ScrollPanel();

		FlexTable resourcePropertiesTable = new FlexTable();
		container.add(resourcePropertiesTable);
		
		
		return container;
	}
	
	
	

	
	private class GeneralInfoCommandMenu{
		public  ListBox operationsListBox;



		public  DialogBox operationDialog;

		private final IntegrationStatusDTO status;
		
		GeneralInfoCommandMenu(IntegrationStatusDTO status){

			this.status = status;
			
			operationsListBox = new ListBox(false);
			
			operationsListBox.addItem(RepoxOperationType.INITIATE_COMPLETE_HARVESTING.getDescription());   
			operationsListBox.addItem(RepoxOperationType.INITIATE_INCREMENTAL_HARVESTING.getDescription()); 
	  		operationsListBox.addItem(RepoxOperationType.VIEW_HARVEST_LOG.getDescription());  
	  		
	  		operationDialog = new DialogBox();
	  		operationDialog.setModal(true);
			
			ScrollPanel dialogContents = new ScrollPanel();
			operationDialog.setWidget(dialogContents);
		}
		
		
		
		
		
		/**
		 * 
		 * 
		 * @return
		 */
		public Button generateRepoxCommandButton(){
			Button actionButton = new Button("Execute");
			actionButton.addClickHandler(new ClickHandler() {
			 
			
				
				@Override
				public void onClick(ClickEvent event) {

					
					operationDialog.center();
					
					int index = operationsListBox.getSelectedIndex();
					
					String value = operationsListBox.getValue(index);
					
					RepoxOperationType optype = null;
					
					if (value.equals(RepoxOperationType.INITIATE_COMPLETE_HARVESTING.getDescription())){
						optype = RepoxOperationType.INITIATE_COMPLETE_HARVESTING;
					}
					else if(value.equals(RepoxOperationType.SCHEDULE_HARVESTING.getDescription())){
						optype = RepoxOperationType.SCHEDULE_HARVESTING;
					}
					else if(value.equals(RepoxOperationType.VIEW_HARVEST_LOG.getDescription())){
						optype = RepoxOperationType.VIEW_HARVEST_LOG;
					}
					
					
					if(optype != null){
					integrationservice.performRepoxRemoteOperation(optype, status.getId(), new AsyncCallback<RepoxExecutionStatusDTO>(){

						@Override
						public void onFailure(Throwable caught) {
							operationDialog.clear();
							
							VerticalPanel vp = new VerticalPanel();
							vp.add(new HTML("<verbatim>"+caught.getMessage()+"</verbatim>"));
							vp.add(createOperationsCloseButton());
							operationDialog.setText("An Unclassified Exception Occured (send this to you know whom)");
							operationDialog.setWidget(vp);

						}

						@Override
						public void onSuccess(RepoxExecutionStatusDTO result) {

							operationDialog.clear();
							
							VerticalPanel vp = new VerticalPanel();
							vp.add(new HTML("<verbatim>"+result.getLogMessage()+"</verbatim>"));
							vp.add(createOperationsCloseButton());
							operationDialog.setWidget(vp);
							operationDialog.setText(result.getOperationMessage());

						}	
					});
					}
				}
			});
			
			return actionButton;
		}
		
		
		private Button createOperationsCloseButton(){
			
			Button closebutton = new Button("Close");
			
			closebutton.addClickHandler(new ClickHandler() {

				@Override
				public void onClick(ClickEvent event) {
					operationDialog.hide();
					
				}
				
			});
			
			return closebutton;
		}
		
		
		public ListBox getOperationsListBox() {
			return operationsListBox;
		}


		public void setOperationsListBox(ListBox operationsListBox) {
			this.operationsListBox = operationsListBox;
		}
	}
	
	


	
	
	

}
