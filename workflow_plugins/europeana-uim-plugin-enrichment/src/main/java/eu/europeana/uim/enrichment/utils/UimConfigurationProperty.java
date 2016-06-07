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
package eu.europeana.uim.enrichment.utils;

/**
 * UIM configuration enumeration
 * @author Yorgos.Mamakis@ kb.nl
 *
 */
public enum UimConfigurationProperty {

	MONGO_HOSTURL("mongo.hostUrl"),
	MONGO_HOSTPORT("mongo.hostPort"),
	MONGO_USERNAME("mongo.ingestion.username"),
	MONGO_PASSWORD("mongo.ingestion.password"),

	MONGO_DB_EUROPEANA("mongo.db.europeana"),
	ZOOKEEPER_HOSTURL("zookeeper.host"),
	ZOOKEEPER_HOSTURLPRODUCTION("zookeeper.production.host"),
	SOLR_HOSTURL("solr.hostUrl"),
	CLOUD_SOLR_HOSTURL("cloud.solr.hostUrl"),
	SOLR_CORE("solr.core"),
	CLOUD_SOLR_CORE("cloud.solr.core"),
	SOLR_CORE_MIGRATION("solr.core.migration"),
	MONGO_DB_COLLECTIONS("mongo.db.collections"),
	MONGO_DB_EUROPEANA_ID("mongo.db.europeanaId"),
	UIM_BCLIST_PATH("uim.bclist.path"),
	SUGARCRM_USERNAME("sugarcrm.username"),
	SUGARCRM_PASSWORD("sugarcrm.password"),
	UIM_REPOSITORY("uim.repository"),
	SOLR_PRODUCTION_HOSTURL("cloud.solr.hostUrl"),
	ENRICHMENT_PATH("enrichment.path"),
        CLIENT_HOSTURL("harvester.hostUrl"),
	CLIENT_HOSTPORT("harvester.hostPort"),
        CLIENT_DB("harvester.db"),
	CLIENT_USERNAME("harvester.username"),
	CLIENT_PASSWORD("harvester.password");
	
	String field;
	private UimConfigurationProperty(String field){
		this.field = field;
	}
	@Override
	public String toString(){
		return this.field;
	}
}
