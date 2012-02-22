/**
 * 
 */
package eu.europeana.uim.mintclient.ampq;

import java.util.ArrayList;
import java.util.List;
import java.util.Observable;
import java.util.Observer;

import eu.europeana.uim.mintclient.jibxbindings.CreateImportCommand;
import eu.europeana.uim.mintclient.jibxbindings.CreateOrganizationCommand;
import eu.europeana.uim.mintclient.jibxbindings.CreateUserCommand;
import eu.europeana.uim.mintclient.jibxbindings.PublicationCommand;
import eu.europeana.uim.mintclient.plugin.MintAMPQClient;
import eu.europeana.uim.mintclient.plugin.MintUIMService;
import eu.europeana.uim.store.Collection;
import eu.europeana.uim.store.Provider;

/**
 * @author geomark
 *
 */
public class MintUIMServiceImpl implements MintUIMService {
private MintAMPQClient client;


	/* (non-Javadoc)
	 * @see eu.europeana.uim.mintclient.plugin.MintUIMService#createMintAuthorizedUser(eu.europeana.uim.store.Provider)
	 */
	@Override
	public void createMintAuthorizedUser(Provider<?> provider) {
		CreateUserCommand command = new CreateUserCommand();
		command.setCorrelationId("correlationId");
		command.setEmail("email");
		command.setFirstName("firstName");
		command.setLastName("lastName");
		command.setUserName("userX");
		command.setPassword("werwer");
		command.setPhone("234234234");
		command.setOrganization("1001");
		client.createUser(command);

	}

	/* (non-Javadoc)
	 * @see eu.europeana.uim.mintclient.plugin.MintUIMService#createMintOrganization(eu.europeana.uim.store.Provider)
	 */
	@Override
	public void createMintOrganization(Provider<?> provider) {
		CreateOrganizationCommand command = new CreateOrganizationCommand();
		command.setCorrelationId("correlationId");
		command.setCountry("es");
		command.setEnglishName("TestOrg");
		command.setName("TestOrg");
		command.setType("Type");
		command.setUserId("1002");
		client.createOrganization(command);

	}

	/* (non-Javadoc)
	 * @see eu.europeana.uim.mintclient.plugin.MintUIMService#createMappingSession(eu.europeana.uim.store.Collection)
	 */
	@Override
	public void createMappingSession(Collection<?> collection) {
		CreateImportCommand command = new CreateImportCommand();
		
		command.setCorrelationId("123");
		command.setUserId("1000");
		command.setOrganizationId("1");
		command.setJdbcRepoxURL("jdbc:postgresql://localhost:5432/repox");
		command.setRepoxUserName("postgres");
		command.setRepoxUserPassword("raistlin");
		command.setRepoxTableName("azores13");
		client.createImports(command);

	}

	/* (non-Javadoc)
	 * @see eu.europeana.uim.mintclient.plugin.MintUIMService#publishCollection(eu.europeana.uim.store.Collection)
	 */
	@Override
	public void publishCollection(Collection<?> collection) {
		PublicationCommand command = new PublicationCommand();
		command.setCorrelationId("correlationId");
		List<String> list =  new ArrayList();
		list.add("test1");
		list.add("test2");
		command.setIncludedImportList(list );
		command.setOrganizationId("orgid");
		command.setUserId("userId");
		client.publishCollection(command);

	}



}
