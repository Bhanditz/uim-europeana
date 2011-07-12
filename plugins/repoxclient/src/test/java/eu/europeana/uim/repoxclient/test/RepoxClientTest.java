/*
 * Copyright 2007 EDL FOUNDATION
 *
 * Licensed under the EUPL, Version 1.1 or - as soon they
 * will be approved by the European Commission - subsequent
 * versions of the EUPL (the "Licence");
 * you may not use this work except in compliance with the
 * Licence.
 * You may obtain a copy of the Licence at:
 *
 * http://ec.europa.eu/idabc/eupl
 *
 * Unless required by applicable law or agreed to in
 * writing, software distributed under the Licence is
 * distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied.
 * See the Licence for the specific language governing
 * permissions and limitations under the Licence.
 */
package eu.europeana.uim.repoxclient.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.StringWriter;

import javax.annotation.Resource;

import org.apache.log4j.Logger;
import org.jibx.runtime.BindingDirectory;
import org.jibx.runtime.IBindingFactory;
import org.jibx.runtime.IMarshallingContext;
import org.jibx.runtime.JiBXException;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import eu.europeana.uim.repoxclient.jibxbindings.Aggregator;
import eu.europeana.uim.repoxclient.jibxbindings.Aggregators;
import eu.europeana.uim.repoxclient.jibxbindings.Country;
import eu.europeana.uim.repoxclient.jibxbindings.DataSources;
import eu.europeana.uim.repoxclient.jibxbindings.DataProviders;
import eu.europeana.uim.repoxclient.jibxbindings.Description;
import eu.europeana.uim.repoxclient.jibxbindings.Name;
import eu.europeana.uim.repoxclient.jibxbindings.NameCode;
import eu.europeana.uim.repoxclient.jibxbindings.Provider;
import eu.europeana.uim.repoxclient.jibxbindings.Success;
import eu.europeana.uim.repoxclient.jibxbindings.Type;
import eu.europeana.uim.repoxclient.jibxbindings.Url;
import eu.europeana.uim.repoxclient.plugin.RepoxRestClient;
import eu.europeana.uim.repoxclient.test.utils.TestUtils;


/**
 * 
 * @author Georgios Markakis
 */

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations={"classpath:testContext.xml"})
public class RepoxClientTest {

@Resource
RepoxRestClient repoxRestClient;	
	
private static org.apache.log4j.Logger LOGGER = Logger.getLogger(RepoxClientTest.class);	


/*
 * Retrieval operations
 */

/**
 * @throws Exception
 */
@Test
public void testGetDatasources() throws Exception{
	DataSources ds =repoxRestClient.retrieveDataSources();
	TestUtils.logMarshalledObject(ds,LOGGER);	
}


/**
 * @throws Exception
 */
@Test
public void testGetAggregators() throws Exception{
	Aggregators aggrs =repoxRestClient.retrieveAggregators();
	TestUtils.logMarshalledObject(aggrs,LOGGER);
}
	

/**
 * @throws Exception
 */
@Test
public void testGetProviders() throws Exception{
	DataProviders prov =repoxRestClient.retrieveProviders();
	TestUtils.logMarshalledObject(prov,LOGGER);
}


/*
 * Creation operations Tests
 */



/**
 * @throws Exception
 */
@Test
public void testCreateUpdateDeleteAggregator() throws Exception{

	//Initialize the Aggregator Object
	Aggregator aggr = TestUtils.createAggregatorObj();

	//Create the Aggregator
	Aggregator rtAggr =  repoxRestClient.createAggregator(aggr);
	assertNotNull(rtAggr);
	assertEquals(aggr.getName().getName(),rtAggr.getName().getName());
	assertEquals(aggr.getNameCode().getNameCode(),rtAggr.getNameCode().getNameCode());
	assertEquals(aggr.getUrl().getUrl(),rtAggr.getUrl().getUrl());
	TestUtils.logMarshalledObject(rtAggr,LOGGER);
	
	//Update the Aggregator
	NameCode upnamecode = new NameCode();
	upnamecode.setNameCode("7777");
	rtAggr.setNameCode(upnamecode);
	Aggregator upAggr =  repoxRestClient.updateAggregator(rtAggr);
	assertNotNull(upAggr);
	assertEquals(rtAggr.getId(),upAggr.getId());
	assertEquals(rtAggr.getName().getName(),upAggr.getName().getName());
	assertEquals(rtAggr.getNameCode().getNameCode(),upAggr.getNameCode().getNameCode());
	assertEquals(rtAggr.getUrl().getUrl(),upAggr.getUrl().getUrl());
	TestUtils.logMarshalledObject(upAggr,LOGGER);
	
	//Delete the Aggregator
	Success res = repoxRestClient.deleteAggregator(rtAggr.getId());
	assertNotNull(res);
	TestUtils.logMarshalledObject(res,LOGGER);
	
}





/**
 * Test the creation, Update & Deletion of a Provider
 *  
 * @throws Exception
 */
@Test
public void testCreateUpdateDeleteProvider() throws Exception{
	
	//Create an Aggregator for testing purposes
	Aggregator aggr = 	TestUtils.createAggregatorObj();
	Aggregator rtAggr =  repoxRestClient.createAggregator(aggr);	
	assertNotNull(rtAggr);
	TestUtils.logMarshalledObject(rtAggr,LOGGER);
	
	//Create a Provider
	Provider prov = TestUtils.createProviderObj();	
	Provider respprov =  repoxRestClient.createProvider(prov, rtAggr);
	assertNotNull(respprov);
	TestUtils.logMarshalledObject(respprov,LOGGER);
	
	//Update the provider
    Name name3 = new Name();
    name3.setName("JunitContainerProviderUPD");
    respprov.setName(name3);
	
    Provider upprov =  repoxRestClient.updateProvider(respprov);
	assertNotNull(upprov);
	assertEquals(respprov.getId(),upprov.getId());
	assertEquals(respprov.getName().getName(),upprov.getName().getName());
	TestUtils.logMarshalledObject(upprov,LOGGER);
	
	//Delete the Provider
	Success res = repoxRestClient.deleteProvider(upprov);
	assertNotNull(res);
	TestUtils.logMarshalledObject(res,LOGGER);
	
	//Delete the Aggregator
	Success aggres = repoxRestClient.deleteAggregator(rtAggr.getId());
	assertNotNull(aggres);
	TestUtils.logMarshalledObject(aggres,LOGGER);
}


}
