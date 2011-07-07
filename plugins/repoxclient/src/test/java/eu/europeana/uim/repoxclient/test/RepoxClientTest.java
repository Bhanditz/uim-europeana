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

import eu.europeana.uim.repoxclient.jibxbindings.Aggregators;
import eu.europeana.uim.repoxclient.jibxbindings.DataSources;
import eu.europeana.uim.repoxclient.jibxbindings.DataProviders;
import eu.europeana.uim.repoxclient.plugin.RepoxRestClient;


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
	
@Test
public void testGetDatasources() throws Exception{
	DataSources ds =repoxRestClient.retrieveDataSources();
	logMarshalledObject(ds);	
}

@Test
public void testGetAggregators() throws Exception{
	Aggregators ds =repoxRestClient.retrieveAggregators();
	logMarshalledObject(ds);	
}
	
@Test
public void testGetProviders() throws Exception{
	DataProviders ds =repoxRestClient.retrieveProviders();
	logMarshalledObject(ds);	
}

/**
 * This method marshals the contents of a  JIBX Element and outputs the results to the
 * Logger.  
 * @param jaxbObject A JIBX representation of a SugarCRM SOAP Element. 
 */
private  void logMarshalledObject(Object jibxObject){		
	IBindingFactory context;

	try {
		context = BindingDirectory.getFactory(jibxObject.getClass());

		IMarshallingContext mctx = context.createMarshallingContext();
		mctx.setIndent(2);
		StringWriter stringWriter = new StringWriter();
		mctx.setOutput(stringWriter);
		mctx.marshalDocument(jibxObject);
		LOGGER.info("===========================================");
		StringBuffer sb = new StringBuffer("Soap Ouput for Class: ");
		sb.append(jibxObject.getClass().getSimpleName());
		LOGGER.info(sb.toString());
		LOGGER.info(stringWriter.toString());
		LOGGER.info("===========================================");
	} catch (JiBXException e) {

		e.printStackTrace();
	}
}


}
