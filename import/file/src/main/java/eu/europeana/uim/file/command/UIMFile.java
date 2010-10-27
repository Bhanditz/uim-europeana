package eu.europeana.uim.file.command;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.PrintStream;
import java.util.List;

import org.apache.felix.gogo.commands.Action;
import org.osgi.service.command.CommandSession;
import org.osgi.service.command.Function;
import org.springframework.beans.factory.annotation.Autowired;

import eu.europeana.uim.api.Registry;
import eu.europeana.uim.command.ConsoleProgressMonitor;
import eu.europeana.uim.common.parse.XMLStreamParserException;
import eu.europeana.uim.file.ReadRecordsFromFile;
import eu.europeana.uim.store.Aggregator;
import eu.europeana.uim.store.Collection;
import eu.europeana.uim.store.Provider;
import eu.europeana.uim.store.Request;
import eu.europeana.uim.store.StorageEngine;


public class UIMFile implements Function, Action {

	private String filename;

	private String format;

	private long collid = -1;

	private Registry registry;

	public UIMFile() {
	}

	@Autowired
	public UIMFile(Registry registry) {
		this.registry = registry;
	}
	
	
	@Override
	public Object execute(CommandSession session) throws Exception {
		return execute(session, null);
	}

	
	@Override
	public Object execute(CommandSession commandSession, List<Object> arguments) throws Exception {
		if (!readArguments(arguments)) return null;

		StorageEngine storage = registry.getActiveStorage();
		long[] ids = execute(storage, commandSession);
		if (ids == null) {
			commandSession.getConsole().println("Failed to read any records from: <" + filename + ">");
		} else {
			commandSession.getConsole().println("Read " + ids.length + " records from: <" + filename + ">");
		}
		return null;
	}

	
	

	/** Method which uses the specified fields (filename, format, collection) to actually 
	 * call the doImport method and read the content fromt the file into the defined storage.
	 * 
	 * Some information is printed to the provided print stream.
	 * 
	 * @param storage storage engines which is the target of the file content
	 * @param out stream to which log/info output goes
	 * @return 
	 * @throws FileNotFoundException 
	 * @throws Exception
	 */
	public long[] execute(StorageEngine storage, CommandSession commandSession) throws XMLStreamParserException, FileNotFoundException {
		File file = new File(filename);
		if (file.exists()) {
			InputStream f = new FileInputStream(file);
			Collection collection = setupCollection(collid, storage, commandSession.getConsole());
			if (collection != null) {
				Request request = storage.createRequest(collection);
				storage.updateRequest(request);
				
				long[] ids = new ReadRecordsFromFile().doEseImport(f, storage, request, new ConsoleProgressMonitor(commandSession));
				return ids;
			} else {
				commandSession.getConsole().println("Collection: <" + collid + "> not found.");
			}
		} else {
			commandSession.getConsole().println("File: <" + file.getAbsolutePath() + "> not found.");
		}
		return null;
	}




	private Collection setupCollection(long target, StorageEngine storage, PrintStream out) {
		Collection collection = storage.getCollection(target);
		if (collection == null) {
			if (target < 0) {
				Aggregator aggregator = storage.createAggregator();
				storage.updateAggregator(aggregator);

				Provider provider = storage.createProvider(aggregator);
				storage.updateProvider(provider);

				collection = storage.createCollection(provider);
				storage.updateCollection(collection);

				out.println("Created aggregator, provider, collection: " + aggregator.getId() + ", " + provider.getId() + ", " + collection.getId());
			} else {
				out.println("Collection <" + target + "> does not exist.");
				return null;
			}
		}
		return collection;
	}

	private boolean readArguments(List<Object> arguments) {
		if (arguments.size() < 2) {
			System.out.println("Filename and format must be specified. uim:file filename format collection");
			return false;
		}

		filename = arguments.get(0).toString();
		if (filename == null) {
			System.out.println("Filename must be specified. uim:file filename format collection");
			return false;
		}

		format = arguments.get(1).toString();
		if (format == null) {
			System.out.println("Format must be specified. uim:file filename format collection");
			return false;
		}

		if (arguments.size() > 2) {
			collid = Long.parseLong(arguments.get(2).toString());
		}
		return true;
	}

	/**
	 * @return the registry
	 */
	public Registry getRegistry() {
		return registry;
	}

	/**
	 * @param registry the registry to set
	 */
	public void setRegistry(Registry registry) {
		this.registry = registry;
	}

	
	
}
