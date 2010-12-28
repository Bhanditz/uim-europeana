INDEX
=====

1/ Project structure
2/ Installation
3/ Test import from file
4/ GWT Development mode
5/ Technologies in use


#1 PROJECT STRUCTURE
====================

Path                              Name                                                    Description
-------------------------------------------------------------------------------------------------------------------------------------------
/                                 Unified Ingestion Manager                                The root maven project
/common                           Unified Ingestion Manager: Common                        Shared classes and resources (e.g. for testing)
/api                              Unified Ingestion Manager: API                           The UIM API bundle, used by plugins
/plugins/basic                    Unified Ingestion Manager: API Basic Implementation      The basic/default implementation of the API

/gui/uim-webconsole-extension     Unified Ingestion Manager: Webconsole extension          UIM GUI extension for the Karaf Webconsole
/gui/uim-gui-gwt                  Unified Ingestion Manager: GWT User Interface            UIM GWT frontend
/storage/memory                   Unified Ingestion Manager: Storage Backend Memory        In-memory implementation of the storage engine

/plugins/fileimp                  Unified Ingestion Manager: Import from File              Bundle to import data from a XML file
/plugins/integration              Unified Ingestion Manager: Integration tests             The integration tests, using PAX-Exam
/plugins/dummy                    Unified Ingestion Manager: Dummy Plugin                  Our beloved dummy plugin

/workflows/dummy                  Unified Ingestion Manager: Dummy Workflow                Our beloved dummy workflow



#2 INSTALLATION
===============

1) Get Apache Felix Karaf at http://karaf.apache.org/

   Temporary (until we add it to the europeana artifactory / the ProgressBar widget makes it to GWT main):
   install the additional gwt incubator JAR as follows:
    'cd gui/uim-gui-gwt'
    'mvn install:install-file -DgroupId=com.google -DartifactId=gwt-incubator -Dversion=20101117-r1766 -Dpackaging=jar -Dfile=lib/gwt-incubator-20101117-r1766.jar'

2) Build UIM with maven
   - 'mvn install'

3) Start Karaf:
   - go to the Karaf main directory
   - start it 'bin/start'

4) Connect to Karaf:
   - go to the Karaf main directory
   - connect with 'bin/client'

5) Set-up dependencies in Karaf:
   - install necessary features:
     - features:install spring
     - features:install war

6) Configure UIM Feature
   - 'features:addurl file://<project-path>/etc/uim-features.xml'
     - you can check if the feature "uim-core" is available via 'features:list'
   - 'features:install uim-core'

   (alternative: install bundles by hand. Note that you need to install them in the right order for this to work
    'osgi:install -s mvn:eu.europeana/europeana-uim-common/1.0.0-SNAPSHOT'
    'osgi:install -s mvn:eu.europeana/europeana-uim-api/1.0.0-SNAPSHOT'
    'osgi:install -s mvn:eu.europeana/europeana-uim-plugin-basic/1.0.0-SNAPSHOT'
    'osgi:install -s mvn:eu.europeana/europeana-uim-storage-memory/1.0.0-SNAPSHOT')

7) Verify if UIM is up and running (Note that auto completion with TAB does only work when blueprint is used)
   - in Karaf shell: 'uim:info'

8) Load/Show sample data:
   - in Karaf shell: 'uim:store -o loadSampleData'
   - in Karaf shell: 'uim:store -o listProvider'
   - in Karaf shell: 'uim:store -o listCollection'

9) Check the web GUI
   Fire up a browser at http://127.0.0.1:8181/gui

#3 TEST IMPORT FROM FILE
========================

1) Install karaf and the UIM API

2) Build UIM module import file (build automatically when building the toplevel of UIM)

3) Install import file: 'osgi:install -s mvn:eu.europeana/europeana-uim-import-file/1.0.0-SNAPSHOT'

4) Verify if UIM File Import is up and running:
   - in Karaf shell: 'uim:file'
   - should complain about missing arguments
   
5) Import ESE file:
   - in Karaf shell: 'uim:file -c 000 file://<project-path>/common/src/test/resources/readingeurope.xml'

#4 GWT DEVELOPMENT MODE
=======================

We use GWT (Google Web Toolkit) for frontend development.

In order to run the development mode:

1) make sure Karaf is up and running and UIM is active
2) launch the GWT development mode:
   - 'cd gui/uim-gui-gwt'
   - mvn gwt:run
3) start a browser at the indicated URL. You may need to install the GWT plugin for frontend development

#5 TECHNOLOGIES IN USE
======================

- the project runs on Apache Karaf which bundles Felix and other Apache OSGi projects (http://karaf.apache.org)
- we use the OSGi Blueprint Container specification.
  Karaf/Felix uses the Apache Aries implementation for that purpose, which handles inversion of control through declarative configuration (see the OSGI-INF.blueprint packages)
- we use PAX Exam for integration tests (http://wiki.ops4j.org/display/paxexam/Pax+Exam)
- we further use Spring for dependency injection in JUnit tests (not integration tests, nor runtime) (http://www.springsource.org/)