/*
 * Copyright 2007-2012 The Europeana Foundation
 *
 *  Licenced under the EUPL, Version 1.1 (the "Licence") and subsequent versions as approved
 *  by the European Commission;
 *  You may not use this work except in compliance with the Licence.
 * 
 *  You may obtain a copy of the Licence at:
 *  http://joinup.ec.europa.eu/software/page/eupl
 *
 *  Unless required by applicable law or agreed to in writing, software distributed under
 *  the Licence is distributed on an "AS IS" basis, without warranties or conditions of
 *  any kind, either express or implied.
 *  See the Licence for the specific language governing permissions and limitations under
 *  the Licence.
 */
package eu.europeana.uim.repoxclient.utils;

import java.io.StringWriter;
import java.math.BigInteger;
import org.jibx.runtime.BindingDirectory;
import org.jibx.runtime.IBindingFactory;
import org.jibx.runtime.IMarshallingContext;
import org.jibx.runtime.JiBXException;
import eu.europeana.uim.repoxclient.jibxbindings.Address;
import eu.europeana.uim.repoxclient.jibxbindings.Aggregator;
import eu.europeana.uim.repoxclient.jibxbindings.Charset;
import eu.europeana.uim.repoxclient.jibxbindings.Country;
import eu.europeana.uim.repoxclient.jibxbindings.Database;
import eu.europeana.uim.repoxclient.jibxbindings.Description;
import eu.europeana.uim.repoxclient.jibxbindings.EarliestTimestamp;
import eu.europeana.uim.repoxclient.jibxbindings.FilePath;
import eu.europeana.uim.repoxclient.jibxbindings.Folder;
import eu.europeana.uim.repoxclient.jibxbindings.FtpPath;
import eu.europeana.uim.repoxclient.jibxbindings.IdXpath;
import eu.europeana.uim.repoxclient.jibxbindings.IsoFormat;
import eu.europeana.uim.repoxclient.jibxbindings.MaximumId;
import eu.europeana.uim.repoxclient.jibxbindings.Name;
import eu.europeana.uim.repoxclient.jibxbindings.NameCode;
import eu.europeana.uim.repoxclient.jibxbindings.Namespace;
import eu.europeana.uim.repoxclient.jibxbindings.NamespacePrefix;
import eu.europeana.uim.repoxclient.jibxbindings.NamespaceUri;
import eu.europeana.uim.repoxclient.jibxbindings.Namespaces;
import eu.europeana.uim.repoxclient.jibxbindings.OaiSet;
import eu.europeana.uim.repoxclient.jibxbindings.OaiSource;
import eu.europeana.uim.repoxclient.jibxbindings.Password;
import eu.europeana.uim.repoxclient.jibxbindings.Port;
import eu.europeana.uim.repoxclient.jibxbindings.Provider;
import eu.europeana.uim.repoxclient.jibxbindings.RecordIdPolicy;
import eu.europeana.uim.repoxclient.jibxbindings.RecordSyntax;
import eu.europeana.uim.repoxclient.jibxbindings.RecordXPath;
import eu.europeana.uim.repoxclient.jibxbindings.RetrieveStrategy;
import eu.europeana.uim.repoxclient.jibxbindings.Server;
import eu.europeana.uim.repoxclient.jibxbindings.Source.Choice;
import eu.europeana.uim.repoxclient.jibxbindings.Source.Sequence;
import eu.europeana.uim.repoxclient.jibxbindings.Source.Sequence1;
import eu.europeana.uim.repoxclient.jibxbindings.Source.Sequence2;
import eu.europeana.uim.repoxclient.jibxbindings.SplitRecords;
import eu.europeana.uim.repoxclient.jibxbindings.Target;
import eu.europeana.uim.repoxclient.jibxbindings.User;

import eu.europeana.uim.repoxclient.jibxbindings.Source;
import eu.europeana.uim.repoxclient.jibxbindings.Type;
import eu.europeana.uim.repoxclient.jibxbindings.Url;

/**
 * Utilities used for Unit Tests
 * 
 * @author Georgios Markakis
 * @author Yorgos Mamakis
 */
public class TestUtils {

	
	/**
	 * Utility classes should not have a public or default constructor.
	 */
	private TestUtils(){
	}
	
	/**
	 * @param strName
	 * @param strNameCode
	 * @param strUrl
	 * @return
	 */
	public static Aggregator createAggregatorObj(String strName, String strNameCode, String strUrl) {

		Aggregator aggr = new Aggregator();

		Name name = new Name();
		name.setName(strName);
		aggr.setName(name);
		NameCode namecode = new NameCode();
		namecode.setNameCode(strNameCode);
		aggr.setNameCode(namecode);
		Url url = new Url();
		url.setUrl(strUrl);
		aggr.setUrl(url);

		return aggr;

	}

	/**
	 * @return
	 */
	public static Provider createProviderObj() {

		Provider prov = new Provider();

		Name name2 = new Name();
		name2.setName("JunitContainerProvider");

		Country country = new Country();
		country.setCountry("gr");
		Description description = new Description();
		description.setDescription("Provider generated by Junit Tests");
		NameCode nameCode = new NameCode();
		nameCode.setNameCode("5555");
		Type type = new Type();
		type.setType("ARCHIVE");
		Url url = new Url();
		url.setUrl("www.europeana.eu");

		prov.setCountry(country);
		prov.setDescription(description);
		prov.setName(name2);
		prov.setNameCode(nameCode);
		prov.setType(type);
		prov.setUrl(url);

		return prov;

	}

	/**
	 * 
	 * /rest/dataSources/createOai?dataProviderId=DPRestr0&
	 * id=bdaSet&
	 * description=Biblioteca Digital Do Alentejo&
	 * nameCode=00123&name=Alentejo&
	 * exportPath=D:/Projectos/repoxdata_new&
	 * schema=http://www.europeana.eu/schemas/ese/ESE-V3.3.xsd&
	 * namespace=http://www.europeana.eu/schemas/ese/&
	 * metadataFormat=ese&
	 * oaiURL=http://bd1.inesc-id.pt:8080/repoxel/OAIHandler&
	 * oaiSet=bda
	 * 
	 * @param id
	 * @return
	 */
	public static Source createOAIDataSource(String id) {


		Source ds = new Source();
		ds.setId(id);
		Description des = new Description();
		des.setDescription("Biblioteca Digital Do Alentejo");
		ds.setDescription(des);
		ds.setNameCode("00123");
		ds.setName("Alentejo");
		ds.setExportPath("D:/Projectos/repoxdata_new");
		ds.setSchema("http://www.europeana.eu/schemas/ese/ESE-V3.3.xsd");
		ds.setNamespace("http://www.europeana.eu/schemas/ese/");
		ds.setMetadataFormat("ese");

		Sequence seq = new Sequence();
		OaiSet oaiSet = new OaiSet();
		oaiSet.setOaiSet("bda");
		seq.setOaiSet(oaiSet);
		OaiSource oaiSource = new OaiSource();
		oaiSource
				.setOaiSource("http://sip-manager.isti.cnr.it:8080/repoxUI_Europeana//OAIHandler");
		seq.setOaiSource(oaiSource);
		ds.setSequence(seq);

		return ds;
	}

	public static Source createZ3950TimestampDataSource(String id) {
		///rest/dataSources/createZ3950Timestamp?dataProviderId=DPRestr0&id=z3950TimeTest&description=test Z39.50 with time stamp&nameCode=00130&name=Z3950-TimeStamp&
		//exportPath=D:/Projectos/repoxdata_new&schema=info:lc/xmlns/marcxchange-v1.xsd&namespace=info:lc/xmlns/marcxchange-v1&address=193.6.201.205&port=1616&database=B1&
		//user=&password=&recordSyntax=usmarc&charset=UTF-8&earliestTimestamp=20110301&recordIdPolicy=IdGenerated&idXpath=&namespacePrefix=&namespaceUri=
		Source ds = new Source();
		ds.setId(id);
		Description des = new Description();
		des.setDescription("test Z39.50 with time stamp");
		ds.setDescription(des);
		ds.setNameCode("00130");
		ds.setName("Z3950-TimeStamp");
		ds.setExportPath("D:/Projectos/repoxdata_new");
		ds.setSchema("info:lc/xmlns/marcxchange-v1.xsd");
		ds.setNamespace("info:lc/xmlns/marcxchange-v1");
		Sequence2 seq = new Sequence2();
		Target target = new Target();
		Address address = new Address();
		address.setAddress("193.6.201.205");
		target.setAddress(address);
		Port port = new Port();
		port.setPort(BigInteger.valueOf(1616));
		target.setPort(port);
		Database database = new Database();
		database.setDatabase("B1");
		target.setDatabase(database);
		User user = new User();
		user.setUser("test");
		target.setUser(user);
		Password password = new Password();
		password.setPassword("test");
		target.setPassword(password);
		RecordSyntax recordSyntax = new RecordSyntax();
		recordSyntax.setRecordSyntax("usmarc");
		target.setRecordSyntax(recordSyntax);
		Charset charset = new Charset();
		charset.setCharset("UTF-8");
		target.setCharset(charset);
		seq.setTarget(target);
		ds.setSequence2(seq);
		Choice choice = new Choice();
		EarliestTimestamp earliestTimestamp = new EarliestTimestamp();
		earliestTimestamp.setEarliestTimestamp(BigInteger.valueOf(20110301));
		choice.setEarliestTimestamp(earliestTimestamp);
		ds.setChoice(choice);
		RecordIdPolicy recordIdPolicy = new RecordIdPolicy();
		recordIdPolicy.setType("IdGenerated");
		
		RecordIdPolicy.Sequence idPolicySequence = new RecordIdPolicy.Sequence();
		IdXpath idXpath = new IdXpath();
		idXpath.setIdXpath("test");
		idPolicySequence.setIdXpath(idXpath);
		recordIdPolicy.setSequence(idPolicySequence);
		Namespaces namespaces = new Namespaces();
		Namespace namespace = new Namespace();
		NamespacePrefix namespacePrefix = new NamespacePrefix();
		namespacePrefix.setNamespacePrefix("test");
		namespace.setNamespacePrefix(namespacePrefix);
		NamespaceUri namespaceUri = new NamespaceUri();
		namespaceUri.setNamespaceUri("test");
		namespace.setNamespaceUri(namespaceUri);
		namespaces.setNamespace(namespace);
		idPolicySequence.setNamespaces(namespaces);
		recordIdPolicy.setSequence(idPolicySequence);
		ds.setRecordIdPolicy(recordIdPolicy);
		
		
		return ds;
	}

	public static Source createZ3950IdFileDataSource(String id) {
		///rest/dataSources/createZ3950IdList?dataProviderId=DPRestr0&id=z3950IdFile&description=test Z39.50 with id list&nameCode=00124&name=Z3950-IdFile&
		//exportPath=D:/Projectos/repoxdata_new&schema=info:lc/xmlns/marcxchange-v1.xsd&namespace=info:lc/xmlns/marcxchange-v1&address=aleph.lbfl.li&port=9909&
		//database=LLB_IDS&user=&password=&recordSyntax=usmarc&charset=UTF-8&filePath=C:\folderZ3950\1900028192z3960idList.txt&recordIdPolicy=IdGenerated&idXpath=&
		//namespacePrefix=&namespaceUri=
		Source ds = new Source();
		ds.setId(id);
		Description des = new Description();
		des.setDescription("test");
		ds.setDescription(des);
		ds.setNameCode("00124");
		ds.setName("Z3950-IdFile");
		ds.setExportPath("D:/Projectos/repoxdata_new");
		ds.setSchema("info:lc/xmlns/marcxchange-v1.xsd");
		ds.setNamespace("info:lc/xmlns/marcxchange-v1");
		Sequence2 seq = new Sequence2();
		Target target = new Target();
		Address address = new Address();
		address.setAddress("aleph.lbfl.li");
		target.setAddress(address);
		Port port = new Port();
		port.setPort(BigInteger.valueOf(9909));
		target.setPort(port);
		Database database = new Database();
		database.setDatabase("LLB_IDS");
		target.setDatabase(database);
		User user = new User();
		user.setUser("test");
		target.setUser(user);
		Password password = new Password();
		password.setPassword("test");
		target.setPassword(password);
		RecordSyntax recordSyntax = new RecordSyntax();
		recordSyntax.setRecordSyntax("usmarc");
		target.setRecordSyntax(recordSyntax);
		Charset charset = new Charset();
		charset.setCharset("UTF-8");
		target.setCharset(charset);
		seq.setTarget(target);
		ds.setSequence2(seq);
		Choice choice = new Choice();
		FilePath filePath = new FilePath();
		filePath.setFilePath("C:\folderZ3950\1900028192z3960idList.txt");
		choice.setFilePath(filePath);
		ds.setChoice(choice);
		RecordIdPolicy recordIdPolicy = new RecordIdPolicy();
		recordIdPolicy.setType("IdGenerated");
		RecordIdPolicy.Sequence idPolicySequence = new RecordIdPolicy.Sequence();
		IdXpath idXpath = new IdXpath();
		idXpath.setIdXpath("test");
		idPolicySequence.setIdXpath(idXpath);
		
		Namespaces namespaces = new Namespaces();
		Namespace namespace = new Namespace();
		NamespacePrefix namespacePrefix = new NamespacePrefix();
		namespacePrefix.setNamespacePrefix("test");
		namespace.setNamespacePrefix(namespacePrefix);
		NamespaceUri namespaceUri = new NamespaceUri();
		namespaceUri.setNamespaceUri("test");
		namespace.setNamespaceUri(namespaceUri);
		namespaces.setNamespace(namespace);
		idPolicySequence.setNamespaces(namespaces);
		recordIdPolicy.setSequence(idPolicySequence);
		ds.setRecordIdPolicy(recordIdPolicy);
		
		
		return ds;
	}
	
	public static Source createZ3950IdSequenceDataSource(String id) {
		///rest/dataSources/createZ3950IdSequence?dataProviderId=DPRestr0&id=z3950IdSeqTest&description=test%20Z39.50%20with%20id%20sequence&nameCode=00129&name=Z3950-IdSeq&
		//exportPath=D:/Projectos/repoxdata_new&schema=info:lc/xmlns/marcxchange-v1.xsd&namespace=info:lc/xmlns/marcxchange-v1&address=aleph.lbfl.li&port=9909&database=LLB_IDS&
		//user=&password=&recordSyntax=usmarc&charset=UTF-8&maximumId=6000&recordIdPolicy=IdGenerated&idXpath=&namespacePrefix=&namespaceUri=
		Source ds = new Source();
		ds.setId(id);
		Description des = new Description();
		des.setDescription("test Z39.50 with id sequence");
		ds.setDescription(des);
		ds.setNameCode("00129");
		ds.setName("Z3950-Idseq");
		ds.setExportPath("D:/Projectos/repoxdata_new");
		ds.setSchema("info:lc/xmlns/marcxchange-v1.xsd");
		ds.setNamespace("info:lc/xmlns/marcxchange-v1");
		Sequence2 seq = new Sequence2();
		Target target = new Target();
		Address address = new Address();
		address.setAddress("aleph.lbfl.li");
		target.setAddress(address);
		Port port = new Port();
		port.setPort(BigInteger.valueOf(9909));
		target.setPort(port);
		Database database = new Database();
		database.setDatabase("LLB_IDS");
		target.setDatabase(database);
		User user = new User();
		user.setUser("test");
		target.setUser(user);
		Password password = new Password();
		password.setPassword("test");
		target.setPassword(password);
		RecordSyntax recordSyntax = new RecordSyntax();
		recordSyntax.setRecordSyntax("usmarc");
		target.setRecordSyntax(recordSyntax);
		Charset charset = new Charset();
		charset.setCharset("UTF-8");
		target.setCharset(charset);
		seq.setTarget(target);
		ds.setSequence2(seq);
		Choice choice = new Choice();
		MaximumId maximumId = new MaximumId();
		maximumId.setMaximumId(BigInteger.valueOf(6000));
		choice.setMaximumId(maximumId);
		ds.setChoice(choice);
		RecordIdPolicy recordIdPolicy = new RecordIdPolicy();
		recordIdPolicy.setType("IdGenerated");
		RecordIdPolicy.Sequence idPolicySequence = new RecordIdPolicy.Sequence();
		IdXpath idXpath = new IdXpath();
		idXpath.setIdXpath("test");
		idPolicySequence.setIdXpath(idXpath);
		Namespaces namespaces = new Namespaces();
		Namespace namespace = new Namespace();
		NamespacePrefix namespacePrefix = new NamespacePrefix();
		namespacePrefix.setNamespacePrefix("test");
		namespace.setNamespacePrefix(namespacePrefix);
		NamespaceUri namespaceUri = new NamespaceUri();
		namespaceUri.setNamespaceUri("test");
		namespace.setNamespaceUri(namespaceUri);
		namespaces.setNamespace(namespace);
		idPolicySequence.setNamespaces(namespaces);
		recordIdPolicy.setSequence(idPolicySequence);
		ds.setRecordIdPolicy(recordIdPolicy);
		
		
		return ds;
	}
	
	
	public static Source createFtpDataSource(String id) {
		///rest/dataSources/createFtp?dataProviderId=DPRestr0&id=ftpTest&description=test FTP data source&nameCode=00124&name=FTP&exportPath=D:/Projectos/repoxdata_new&
		//schema=http://www.europeana.eu/schemas/ese/ESE-V3.3.xsd&namespace=http://www.europeana.eu/schemas/ese/&metadataFormat=ese&isoFormat=&charset=&recordIdPolicy=IdGenerated&
		//idXpath=&namespacePrefix=&namespaceUri=&recordXPath=record&server=bd1.inesc-id.pt&user=ftp&password=pmath2010.&ftpPath=/Lizbeth
		Source ds = new Source();
		ds.setId(id);
		Description des = new Description();
		des.setDescription("test ftp Data source");
		ds.setDescription(des);
		ds.setNameCode("00124");
		ds.setName("FTP");
		ds.setExportPath("D:/Projectos/repoxdata_new");
		ds.setSchema("http://www.europeana.eu/schemas/ese/ESE-V3.3.xsd");
		ds.setNamespace("http://www.europeana.eu/schemas/ese/");
		Sequence2 seq = new Sequence2();
		Target target = new Target();
		
		
		Charset charset = new Charset();
		charset.setCharset("UTF-8");
		target.setCharset(charset);
		seq.setTarget(target);
		ds.setSequence2(seq);
		ds.setMetadataFormat("ese");
		Choice choice = new Choice();
		IsoFormat isoFormat = new IsoFormat();
		isoFormat.setIsoFormat("test");
		choice.setIsoFormat(isoFormat);
		choice.clearChoiceSelect();
		FtpPath ftpPath = new FtpPath();
		ftpPath.setFtpPath("/Lizbeth");
		choice.setFtpPath(ftpPath);
		ds.setChoice(choice);
		SplitRecords splitRecords = new SplitRecords();
		
		RecordXPath recordXPath = new RecordXPath();
		recordXPath.setRecordXPath("record");
		splitRecords.setRecordXPath(recordXPath);
		ds.setSplitRecords(splitRecords);
		Sequence1 seq1 = new Sequence1();
		RetrieveStrategy retrieveStrategy = new RetrieveStrategy();
		RetrieveStrategy.Choice choiceRetStr = new RetrieveStrategy.Choice();
		User user = new User();
		user.setUser("ftp");
		
		Password password = new Password();
		password.setPassword("password");
		choiceRetStr.setUser(user);
		choiceRetStr.setPassword(password);
		Server server = new Server();
		server.setServer("bd1.inesc-id.pt");
		choiceRetStr.setServer(server);
		retrieveStrategy.setChoice(choiceRetStr);
		seq1.setRetrieveStrategy(retrieveStrategy);
		ds.setSequence1(seq1);
		
		RecordIdPolicy recordIdPolicy = new RecordIdPolicy();
		recordIdPolicy.setType("IdGenerated");
		RecordIdPolicy.Sequence idPolicySequence = new RecordIdPolicy.Sequence();
		IdXpath idXpath = new IdXpath();
		idXpath.setIdXpath("test");
		idPolicySequence.setIdXpath(idXpath);
		Namespaces namespaces = new Namespaces();
		Namespace namespace = new Namespace();
		NamespacePrefix namespacePrefix = new NamespacePrefix();
		namespacePrefix.setNamespacePrefix("test");
		namespace.setNamespacePrefix(namespacePrefix);
		NamespaceUri namespaceUri = new NamespaceUri();
		namespaceUri.setNamespaceUri("test");
		namespace.setNamespaceUri(namespaceUri);
		namespaces.setNamespace(namespace);
		idPolicySequence.setNamespaces(namespaces);
		recordIdPolicy.setSequence(idPolicySequence);
		ds.setRecordIdPolicy(recordIdPolicy);
		
		
		return ds;
	}
	
	public static Source createHttpDataSource(String id) {
		///rest/dataSources/createHttp?dataProviderId=DPRestr0&id=httpTest&description=test HTTP data source&nameCode=00124&name=HTTP&exportPath=D:/Projectos/repoxdata_new&
		//schema=http://www.europeana.eu/schemas/ese/ESE-V3.3.xsd&namespace=http://www.europeana.eu/schemas/ese/&metadataFormat=ese&isoFormat=&charset=&recordIdPolicy=IdGenerated&
		//idXpath=&namespacePrefix=&namespaceUri=&recordXPath=record&url=http://digmap2.ist.utl.pt:8080/index_digital/contente/09428_Ag_DE_ELocal.zip
		Source ds = new Source();
		ds.setId(id);
		Description des = new Description();
		des.setDescription("test http Data source");
		ds.setDescription(des);
		ds.setNameCode("00124");
		ds.setName("HTTP");
		ds.setExportPath("D:/Projectos/repoxdata_new");
		ds.setSchema("http://www.europeana.eu/schemas/ese/ESE-V3.3.xsd");
		ds.setNamespace("http://www.europeana.eu/schemas/ese/");
		Sequence2 seq = new Sequence2();
		Target target = new Target();
		
		Charset charset = new Charset();
		charset.setCharset("UTF-8");
		target.setCharset(charset);
		seq.setTarget(target);
		ds.setSequence2(seq);
		ds.setMetadataFormat("ese");
		Choice choice = new Choice();
		IsoFormat isoFormat = new IsoFormat();
		isoFormat.setIsoFormat("test");
		choice.setIsoFormat(isoFormat);
		
		ds.setChoice(choice);
		SplitRecords splitRecords = new SplitRecords();
		RecordXPath recordXPath = new RecordXPath();
		recordXPath.setRecordXPath("record");
		splitRecords.setRecordXPath(recordXPath);
		ds.setSplitRecords(splitRecords);
		Sequence1 seq1 = new Sequence1();
		RetrieveStrategy retrieveStrategy = new RetrieveStrategy();
		RetrieveStrategy.Choice choiceRetStr = new RetrieveStrategy.Choice();
		
		Url url = new Url();
		url.setUrl("http://digmap2.ist.utl.pt:8080/index_digital/contente/09428_Ag_DE_ELocal.zip");
		choiceRetStr.setUrl(url);
		retrieveStrategy.setChoice(choiceRetStr);
		seq1.setRetrieveStrategy(retrieveStrategy);
		ds.setSequence1(seq1);
		
		RecordIdPolicy recordIdPolicy = new RecordIdPolicy();
		recordIdPolicy.setType("IdGenerated");
		RecordIdPolicy.Sequence idPolicySequence = new RecordIdPolicy.Sequence();
		IdXpath idXpath = new IdXpath();
		idXpath.setIdXpath("test");
		idPolicySequence.setIdXpath(idXpath);
		Namespaces namespaces = new Namespaces();
		Namespace namespace = new Namespace();
		NamespacePrefix namespacePrefix = new NamespacePrefix();
		namespacePrefix.setNamespacePrefix("test");
		namespace.setNamespacePrefix(namespacePrefix);
		NamespaceUri namespaceUri = new NamespaceUri();
		namespaceUri.setNamespaceUri("test");
		namespace.setNamespaceUri(namespaceUri);
		namespaces.setNamespace(namespace);
		idPolicySequence.setNamespaces(namespaces);
		recordIdPolicy.setSequence(idPolicySequence);
		ds.setRecordIdPolicy(recordIdPolicy);
		
		
		return ds;
	}
	
	public static Source createFolderDataSource(String id) {
		///rest/dataSources/createFolder?dataProviderId=DPRestr0&id=folderTest&description=test%20Folder%20data%20source&nameCode=00124&name=Folder&
		//exportPath=D:/Projectos/repoxdata_new&schema=info:lc/xmlns/marcxchange-v1.xsd&namespace=info:lc/xmlns/marcxchange-v1&metadataFormat=ISO2709&
		//isoFormat=pt.utl.ist.marc.iso2709.IteratorIso2709&charset=UTF-8&recordIdPolicy=IdExtracted&idXpath=/mx:record/mx:controlfield[@tag=%22001%22]&namespacePrefix=mx&
		//namespaceUri=info:lc/xmlns/marcxchange-v1&recordXPath=&folder=C:\folder
		Source ds = new Source();
		ds.setId(id);
		Description des = new Description();
		des.setDescription("test Folder source");
		ds.setDescription(des);
		ds.setNameCode("00124");
		ds.setName("Folder");
		ds.setExportPath("D:/Projectos/repoxdata_new");
		ds.setSchema("http://www.europeana.eu/schemas/ese/ESE-V3.3.xsd");
		ds.setNamespace("http://www.europeana.eu/schemas/ese/");
		Sequence2 seq = new Sequence2();
		Target target = new Target();
		
		Charset charset = new Charset();
		charset.setCharset("UTF-8");
		target.setCharset(charset);
		seq.setTarget(target);
		ds.setSequence2(seq);
		ds.setMetadataFormat("ese");
		
		Choice choice = new Choice();
		
		IsoFormat isoFormat = new IsoFormat();
		isoFormat.setIsoFormat("");
		choice.setIsoFormat(isoFormat);
		choice.clearChoiceSelect();
		Folder folder = new Folder();
		folder.setFolder("C:\folder");
		choice.setFolder(folder);
		ds.setChoice(choice);
		SplitRecords splitRecords = new SplitRecords();
		RecordXPath recordXPath = new RecordXPath();
		recordXPath.setRecordXPath("record");
		splitRecords.setRecordXPath(recordXPath);
		ds.setSplitRecords(splitRecords);

		
		RecordIdPolicy recordIdPolicy = new RecordIdPolicy();
		recordIdPolicy.setType("IdGenerated");
		RecordIdPolicy.Sequence idPolicySequence = new RecordIdPolicy.Sequence();
		IdXpath idXpath = new IdXpath();
		idXpath.setIdXpath("test");
		idPolicySequence.setIdXpath(idXpath);
		Namespaces namespaces = new Namespaces();
		Namespace namespace = new Namespace();
		NamespacePrefix namespacePrefix = new NamespacePrefix();
		namespacePrefix.setNamespacePrefix("test");
		namespace.setNamespacePrefix(namespacePrefix);
		NamespaceUri namespaceUri = new NamespaceUri();
		namespaceUri.setNamespaceUri("test");
		namespace.setNamespaceUri(namespaceUri);
		namespaces.setNamespace(namespace);
		idPolicySequence.setNamespaces(namespaces);
		recordIdPolicy.setSequence(idPolicySequence);
		ds.setRecordIdPolicy(recordIdPolicy);
		
		
		return ds;
	}
	
	/**
	 * This method marshals the contents of a JIBX Element and outputs the
	 * results to the Logger.
	 * 
	 * @param jaxbObject
	 *            A JIBX representation of a SugarCRM SOAP Element.
	 */
	public static void logMarshalledObject(Object jibxObject,
			org.apache.log4j.Logger LOGGER) {
		IBindingFactory context;

		try {
			context = BindingDirectory.getFactory(jibxObject.getClass());

			IMarshallingContext mctx = context.createMarshallingContext();
			mctx.setIndent(2);
			StringWriter stringWriter = new StringWriter();
			mctx.setOutput(stringWriter);
			mctx.marshalDocument(jibxObject);
			LOGGER.info("===========================================");
			StringBuffer sb = new StringBuffer("Rest XML Response for Class: ");
			sb.append(jibxObject.getClass().getSimpleName());
			LOGGER.info(sb.toString());
			LOGGER.info(stringWriter.toString());
			LOGGER.info("===========================================");
		} catch (JiBXException e) {
			LOGGER.error(e.getMessage());
		}
	}
}
