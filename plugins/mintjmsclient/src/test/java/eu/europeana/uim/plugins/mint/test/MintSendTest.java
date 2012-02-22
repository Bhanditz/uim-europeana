/**
 * 
 */
package eu.europeana.uim.plugins.mint.test;

import java.util.ArrayList;
import java.util.List;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import eu.europeana.uim.mintclient.ampq.MintAMPQClientImpl;
import eu.europeana.uim.mintclient.jibxbindings.CreateUserCommand;
import eu.europeana.uim.mintclient.jibxbindings.CreateOrganizationCommand;
import eu.europeana.uim.mintclient.jibxbindings.CreateImportCommand;
import eu.europeana.uim.mintclient.jibxbindings.GetImportsCommand;
import eu.europeana.uim.mintclient.jibxbindings.GetTransformationsCommand;
import eu.europeana.uim.mintclient.jibxbindings.PublicationCommand;
import eu.europeana.uim.mintclient.plugin.MintAMPQClient;

/**
 * @author geomark
 *
 */
public class MintSendTest {

	private static MintAMPQClient client;
	
	@BeforeClass public static void initclient() {
		client = new MintAMPQClientImpl();
	}
	    
	@AfterClass public static void tearDown() {
	   client = null;
	   System.gc();
	}
	
	
	
	@Test
	public void createOrganizationTest(){
		CreateOrganizationCommand command = new CreateOrganizationCommand();
		
		command.setCorrelationId("correlationId");
		command.setCountry("es");
		command.setEnglishName("TestOrg");
		command.setName("TestOrg");
		command.setType("Type");
		command.setUserId("1002");
		client.createOrganization(command);
	}
	
	@Test
	public void createUserTest(){
		CreateUserCommand command = new CreateUserCommand();
		command.setCorrelationId("correlationId");
		command.setEmail("email");
		command.setFirstName("firstName");
		command.setLastName("lastName");
		command.setUserName("userXXXWAASDFFF");
		command.setPassword("werwer");
		command.setPhone("234234234");
		command.setOrganization("1001");
		client.createUser(command);
	}
	

	
	
	

	
	@Test
	public void createImportsTest(){
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
	
	@Test
	public void getImportsTest(){
		GetImportsCommand command =  new GetImportsCommand();
		command.setCorrelationId("provid");
		command.setOrganizationId("1002");
		client.getImports(command);
	}
	
	@Test
	public void getTransformations(){
		GetTransformationsCommand command = new GetTransformationsCommand();
		command.setCorrelationId("correlationId");
		command.setOrganizationId("1002");
		client.getTransformations(command);
	}
	
	@Test
	public void publishCollection(){
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
