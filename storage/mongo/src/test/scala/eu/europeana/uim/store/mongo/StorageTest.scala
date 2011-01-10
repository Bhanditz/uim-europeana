package eu.europeana.uim.store.mongo

import org.scalatest.matchers.ShouldMatchers
import com.mongodb.Mongo
import eu.europeana.uim.{MDRFieldRegistry, MetaDataRecord}
import org.junit.Test
import org.scalatest.junit.{ShouldMatchersForJUnit, JUnitSuite}

/**
 *
 * @author Manuel Bernhardt <bernhardt.manuel@gmail.com>
 */

class StorageTest extends JUnitSuite with ShouldMatchersForJUnit {

  def withEngine(testFunction: MongoStorageEngine => Unit) {
    val e: MongoStorageEngine = new MongoStorageEngine("TEST")
    e.initialize

    try {
      testFunction(e)
    } finally {
      e.shutdown
      // clear everything
      val m: Mongo = new Mongo();
      m.dropDatabase("TEST");


    }
  }

  // providers

  /**
   * engine creates providers and returns an incremented id
   */
  @Test def createProvider() {
    withEngine{
      engine => {
        val p = engine.createProvider
        val p1 = engine.createProvider
        p.getId should equal(0)
        p1.getId should equal(1)
      }
    }
  }

  /**
   * engine updates providers properly
   */
  @Test def updateProvider() {
    withEngine{
      engine => {
        val p = engine.createProvider
        p.getName should equal(null)
        p.setName("MyLibrary")
        engine.updateProvider(p)

        val p1 = engine.getProvider(p.getId)
        p.getName should equal("MyLibrary")
      }
    }
  }

  /**
   * engine retrieves providers properly
   */
  @Test def retrieveProvider() {
    withEngine{
      engine =>
        val p = engine.createProvider
        val id = p.getId
        val pp = engine.getProvider(id)
        p should equal(pp)

        engine.getProvider.size should equal(1);
    }
  }


  // collections

  /**
   * engine creates collections and returns an incremented id
   */
  @Test def createCollection() {
    withEngine{
      engine => {
        val p = engine.createProvider
        val c = engine.createCollection(p)
        val c1 = engine.createCollection(p)
        c.getId should equal(0)
        c1.getId should equal(1)
      }
    }

  }

  /**
   * engine updates collections properly
   */
  @Test def updateCollection() {
    withEngine{
      engine => {
        val p = engine.createProvider
        val c = engine.createCollection(p)
        c.getName should equal(null)
        c.setName("MyCollection")
        engine.updateCollection(c)

        val c1 = engine.getCollection(c.getId)
        c1.getName should equal("MyCollection")
      }
    }
  }

  /**
   * engine retrieves collections properly
   */
  @Test def retrieveCollection() {
    withEngine{
      engine =>
        val p = engine.createProvider
        val c = engine.createCollection(p)
        val id = c.getId
        val cc = engine.getCollection(id)
        c should equal(cc)

        engine.getCollections(p).size should equal(1);
    }
  }

  // requests

  /**
   * engine creates requests and returns an incremented id
   */
  @Test def createRequest() {
    withEngine{
      engine => {
        val p = engine.createProvider
        val c = engine.createCollection(p)
        val r = engine.createRequest(c)
        val r1 = engine.createRequest(c)
        r.getId should equal(0)
        r1.getId should equal(1)
      }
    }
  }

  /**
   * engine retrieves requests properly
   */
  @Test def retrieveRequest() {
    withEngine{
      engine =>
        val p = engine.createProvider
        val c = engine.createCollection(p)
        val r = engine.createRequest(c)
        val id = r.getId

        engine.getRequests(c).size should equal(1);
    }
  }

  // executions

  /**
   * engine creates executions and returns an incremented id
   */
  @Test def createExecution() {
    withEngine{
      engine => {
        val e = engine.createExecution
        val e1 = engine.createExecution
        e.getId should equal(0)
        e1.getId should equal(1)
      }
    }
  }

  /**
   * engine retrieves executions properly
   */
  @Test def retrieveExecution() {
    withEngine{
      engine =>
        val e = engine.createExecution
        val id = e.getId
        engine.getExecutions().size should equal(1)
    }
  }

  // mdrs

  /**
   * engine creates mdrs and returns an incremented id
   */
  @Test def createMdr() {
    withEngine{
      engine => {
        val p = engine.createProvider()
        val c = engine.createCollection(p)
        val r = engine.createRequest(c)
        val mdr = engine.createMetaDataRecord(r)
        val mdr1 = engine.createMetaDataRecord(r)
        mdr.getId should equal(0)
        mdr1.getId should equal(1)
      }
    }
  }


  /**
   * engine retrieves lists of mdrs
   */
  @Test def retrieveMdrList() {
    withEngine{
      engine => {
        val p = engine.createProvider()
        val c = engine.createCollection(p)
        val r = engine.createRequest(c)
        val mdr = engine.createMetaDataRecord(r)
        val mdr1 = engine.createMetaDataRecord(r)
        val mdr2 = engine.createMetaDataRecord(r)

        // FIXME does not yet work
        val l: Array[MetaDataRecord[MDRFieldRegistry]] = engine.getMetaDataRecords(0, 1, 2)
        l(0).getRequest.getId should equal(r.getId)
        l(1).getRequest.getId should equal(r.getId)
        l(2).getRequest.getId should equal(r.getId)


      }
    }
  }

}