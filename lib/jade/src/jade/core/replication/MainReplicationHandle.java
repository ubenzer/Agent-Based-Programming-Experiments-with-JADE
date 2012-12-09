package jade.core.replication;

import jade.core.GenericCommand;
import jade.core.Service;
import jade.core.ServiceFinder;
import jade.core.ServiceNotActiveException;
import jade.util.Logger;

/**
 * This class allows other services to exploit the MainReplicationService to keep 
 * local information in synch among slices on replicated Main Containers 
 */
public class MainReplicationHandle {
	//#J2ME_EXCLUDE_BEGIN
	private String myService;
	private MainReplicationService replicationService;
	
	private Logger myLogger = Logger.getMyLogger(getClass().getName());
	//#J2ME_EXCLUDE_END
	
	public MainReplicationHandle(Service svc, ServiceFinder sf) {
		//#J2ME_EXCLUDE_BEGIN
		myService = svc.getName();
		try {
			replicationService = (MainReplicationService) sf.findService(MainReplicationSlice.NAME);
		}
		catch (ServiceNotActiveException snat) {
			// MainReplicationService not active --> just do nothing
		}
		catch (Exception e) {
			// Should never happen
			myLogger.log(Logger.WARNING, "Error accessing the local MainReplicationService.", e);
		}
		//#J2ME_EXCLUDE_END
	}
	
	public void invokeReplicatedMethod(String methodName, Object[] params) {
		//#J2ME_EXCLUDE_BEGIN
		if (replicationService != null) {
			GenericCommand cmd = new GenericCommand(MainReplicationSlice.H_INVOKESERVICEMETHOD, MainReplicationSlice.NAME, null);
			cmd.addParam(myService);
			cmd.addParam(methodName);
			cmd.addParam(params);
			try {
				replicationService.broadcastToReplicas(cmd, false);
			}
			catch (Exception e) {
				// Should never happen as real exceptions are logged inside broadcastToReplicas()
				myLogger.log(Logger.WARNING, "Error propagating H-command " + cmd.getName() +" to replicas. Method to invoke on replicas was "+methodName, e);
			}
		}
		//#J2ME_EXCLUDE_END
	}

}
