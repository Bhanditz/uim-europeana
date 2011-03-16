package eu.europeana.uim.sugarcrmclient.plugin.command;

import java.io.PrintStream;
import java.util.List;

import org.apache.felix.gogo.commands.Action;
import org.apache.felix.gogo.commands.Argument;
import org.apache.felix.gogo.commands.Command;
import org.apache.felix.gogo.commands.Option;
import org.osgi.service.command.CommandSession;
import org.osgi.service.command.Function;

import eu.europeana.uim.sugarcrmclient.plugin.SugarCRMAgent;
import eu.europeana.uim.sugarcrmclient.plugin.SugarCRMAgentImpl;


@Command(name = "uim", scope = "sugaragent")
public class UIM2SugarCrm implements Function, Action {

	enum Operation {info,updatesession,showavailablemodules,showmodulefields,
		pending4ingestion,notifyIngestionSuccsess,notifyIngestionFailure,getpolltime,setpolltime}
	
	private SugarCRMAgent sugarcrmPlugin;
	
	@Option(name = "-o", aliases = {"--operation"}, required = false)
	private Operation operation;
	
	@Argument(index = 0)
	private String argument0;

	@Argument(index = 1)
	private String argument1;
	
	
	public UIM2SugarCrm (SugarCRMAgent sugarcrmPlugin ){
		this.sugarcrmPlugin = sugarcrmPlugin;
	}
	
	
	@Override
	public Object execute(CommandSession commandsession) throws Exception {

		PrintStream out = commandsession.getConsole();
		
		if (operation == null) {
			out.println("Please specify an operation with the '-o' option. Possible values are:");
			out.println("  info                    \t\t\t\t provides inforamtion regarding the existing remote connection to SugarCRM");
			out.println("  updatesession           \t\t\t\t creates a new session for the client");			
			out.println("  showavailablemodules    \t\t\t\t shows the available modules in Sugar CRM");		
			out.println("  showmodulefields        \t\t\t\t shows the available fields for a given module");		
			out.println("  pending4ingestion       \t\t\t\t shows all entries in SugarCRM who are candidates for ingestion initiation.");
			out.println("  notifyIngestionSuccsess \t\t\t\t notifies SugarCRM that ingestion for the specified entries was successfull");
			out.println("  notifyIngestionFailure  \t\t\t\t notifies SugarCRM that ingestion for the specified entries has failed");
			out.println("  getpolltime             \t\t\t\t shows the time set for the next polling session");
			out.println("  setpolltime             \t\t\t\t sets the time for the next polling session");
			return null;
		}
		
		switch (operation) {
		case info:
			out.println(sugarcrmPlugin.showConnectionStatus());
			break;
		case updatesession:
			out.println(sugarcrmPlugin.updateSession());
			break;
		case showavailablemodules:
			out.println(sugarcrmPlugin.showAvailableModules());
			break;			
		case showmodulefields:
			if(argument0!=null)
			{	
			out.println(sugarcrmPlugin.showModuleFields(argument0));
			}
			else
			{
				out.println("Please define the name of the module");
			}
			break;	
		case pending4ingestion:
			out.println(sugarcrmPlugin.pollForHarvestInitiators());
			break;
		case notifyIngestionSuccsess:
			out.println("Not implemented yet.");
			break;
		case notifyIngestionFailure:
			out.println("Not implemented yet.");
			break;
		case getpolltime:
			out.println("Not implemented yet.");
			break;
		case setpolltime:
			out.println("Not implemented yet.");
			break;
		default:
			out.println("Unknown Command...");
		}
		
		
		return null;
	}

	@Override
	public Object execute(CommandSession commandsession, List<Object> arg1)
			throws Exception {

		PrintStream out = commandsession.getConsole();
		
		return null;
	}
	
	
	
	

}
