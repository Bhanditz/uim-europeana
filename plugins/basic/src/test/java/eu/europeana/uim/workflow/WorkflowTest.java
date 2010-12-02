package eu.europeana.uim.workflow;

import eu.europeana.uim.MetaDataRecord;
import eu.europeana.uim.MetaDataRecordHandler;
import eu.europeana.uim.api.IngestionPlugin;
import eu.europeana.uim.api.Orchestrator;
import eu.europeana.uim.api.Registry;
import eu.europeana.uim.api.StorageEngine;
import eu.europeana.uim.api.StorageEngineException;
import eu.europeana.uim.api.Workflow;
import eu.europeana.uim.api.WorkflowStep;
import eu.europeana.uim.command.ConsoleProgressMonitor;
import eu.europeana.uim.common.ProgressMonitor;
import eu.europeana.uim.common.parse.RecordParser;
import eu.europeana.uim.common.parse.XMLStreamParserException;
import eu.europeana.uim.orchestration.UIMExecution;
import eu.europeana.uim.orchestration.WorkflowProcessor;
import eu.europeana.uim.store.Collection;
import eu.europeana.uim.store.Request;
import eu.europeana.uim.store.memory.MemoryStorageEngine;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.times;

/**
 * Test for UIMWorkflow construction
 *
 * @author Manuel Bernhardt <bernhardt.manuel@gmail.com>
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = "/META-INF/spring/test-bundle-context.xml")
public class WorkflowTest {

    @Autowired
    private Registry registry;

    private long[] testIDs;

    @Test
    @Ignore // for the moment, until we have parallel executions again
    public void buildWorkfowRepresentation() {

        Workflow w = buildTestWorkflow();

        assertEquals(0l, w.getId().longValue());
        assertEquals("First workflow", w.getName());
        assertEquals("Test workflow", w.getDescription());
        WorkflowStep s = w.getSteps().get(2);
        assertTrue(s instanceof ProcessingContainer);
        assertEquals(3, ((ProcessingContainer) s).getSteps().size());
    }

    private Workflow buildTestWorkflow() {
        Workflow w = new UIMWorkflow(0l, "First workflow", "Test workflow");
        w.addStep(new MockPlugin("Plugin1"));
        w.addStep(new MockPlugin("Plugin2"));
        ProcessingContainer c = new ProcessingContainer();
        //w.addStep(c);
        c.addStep(new MockPlugin("ParallelPlugin1"));
        c.addStep(new MockPlugin("ParallelPlugin2"));
        c.addStep(new MockPlugin("ParallelPlugin3"));
        w.addStep(new MockPlugin("FinalPlugin"));
        return w;
    }

    @Before
    public void setup() throws Exception {

        // we don't do a tear down, as the main thread of the test finishes before the asynchronous methods
        // we want to test. so instead we make a clean initialisation.

        if (registry.getActiveStorage() != null) {
            registry.removeStorage(registry.getActiveStorage());
        }

        registry.addStorage(new MemoryStorageEngine());
        testIDs = loadTestData();
    }


    @Test
    public void runWorkflow() throws Exception {

        // FIXME this test is green also when nothing is really processed
        // we need to check that 900 MDRs have been touched

        // TODO write separate test to check execution behavior etc.

        Workflow w = new UIMWorkflow(0l, "First workflow", "Test workflow");

        IngestionPlugin p1 = mock(IngestionPlugin.class);
        IngestionPlugin p2 = mock(IngestionPlugin.class);
        IngestionPlugin pp1 = mock(IngestionPlugin.class);
        IngestionPlugin pp2 = mock(IngestionPlugin.class);
        IngestionPlugin pp3 = mock(IngestionPlugin.class);
        IngestionPlugin p3 = mock(IngestionPlugin.class);

        when(p1.getIdentifier()).thenReturn("Plugin 1");
        when(p2.getIdentifier()).thenReturn("Plugin 2");
        when(p3.getIdentifier()).thenReturn("Plugin 2");
        when(pp1.getIdentifier()).thenReturn("Parallel Plugin 1");
        when(pp2.getIdentifier()).thenReturn("Parallel Plugin 2");
        when(pp3.getIdentifier()).thenReturn("Parallel Plugin 3");

        w.addStep(p1);
        w.addStep(p2);
        w.addStep(p3);

        ProcessingContainer c = new ProcessingContainer();
        //w.addStep(c);
        c.addStep(pp1);
        c.addStep(pp2);
        c.addStep(pp3);

        UIMExecution e = mock(UIMExecution.class);
        when(e.getId()).thenReturn(0l);

        Orchestrator o = mock(Orchestrator.class);
        when(o.allDataProcessed(e)).thenReturn(false);

        // this is so clumsy
        long[] a1 = new long[100];
        long[] a2 = new long[100];
        long[] a3 = new long[100];
        long[] a4 = new long[100];
        long[] a5 = new long[100];
        long[] a6 = new long[100];
        long[] a7 = new long[100];
        long[] a8 = new long[100];
        long[] a9 = new long[100];
        System.arraycopy(testIDs, 0, a1, 0, 100);
        System.arraycopy(testIDs, 100, a2, 0, 100);
        System.arraycopy(testIDs, 200, a3, 0, 100);
        System.arraycopy(testIDs, 300, a4, 0, 100);
        System.arraycopy(testIDs, 400, a5, 0, 100);
        System.arraycopy(testIDs, 500, a6, 0, 100);
        System.arraycopy(testIDs, 600, a7, 0, 100);
        System.arraycopy(testIDs, 700, a8, 0, 100);
        System.arraycopy(testIDs, 800, a9, 0, 100);

//        ProgressMonitor monitor = mock(ProgressMonitor.class);
        ProgressMonitor monitor = new ConsoleProgressMonitor(System.out);
        when(e.getMonitor()).thenReturn(monitor);
        when(o.getBatchFor(e)).thenReturn(a1, a2, a3, a4, a5, a6, a7, a8, a9, null);
        when(o.getTotal(e)).thenReturn(999);

        WorkflowProcessor processor = new WorkflowProcessor(w, o, registry);
        processor.addExecution(e);
        processor.start();

        // this is a sortof hack
        // but I don't see a better way to mimic the behaviour of an actual orchestrator with mockito
        Thread.sleep(2000);
        when(o.allDataProcessed(e)).thenReturn(true);

        Thread.sleep(1000);

        verify(p1, times(900)).processRecord(any(MetaDataRecord.class));
        verify(p2, times(900)).processRecord(any(MetaDataRecord.class));
        verify(p3, times(900)).processRecord(any(MetaDataRecord.class));
    }


    private long[] loadTestData() {

        // TODO maybe move this to common, so it can be used in unit tests all over the place.

        try {

            StorageEngine storage = registry.getActiveStorage();
            Collection targetcoll = storage.findCollection("000");

            Request request = storage.createRequest(targetcoll);
            storage.updateRequest(request);

            RecordParser parser = new RecordParser();
            MetaDataRecordHandler handler = new MetaDataRecordHandler(storage, request, "europeana:record");

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            PrintStream ps = new PrintStream(baos);

            // parse the file/stream
            parser.parse(getClass().getResourceAsStream("/readingeurope.xml"), handler, new ConsoleProgressMonitor(ps));
            return storage.getByRequest(request);

        } catch (StorageEngineException e) {
            e.printStackTrace();
        } catch (XMLStreamParserException e) {
            e.printStackTrace();
        }

        return null;
    }


}
