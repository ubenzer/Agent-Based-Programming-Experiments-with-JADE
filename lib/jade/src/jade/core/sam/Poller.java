/*****************************************************************
 JADE - Java Agent DEvelopment Framework is a framework to develop
 multi-agent systems in compliance with the FIPA specifications.
 Copyright (C) 2000 CSELT S.p.A.
 
 GNU Lesser General Public License
 
 This library is free software; you can redistribute it and/or
 modify it under the terms of the GNU Lesser General Public
 License as published by the Free Software Foundation, 
 version 2.1 of the License. 
 
 This library is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 Lesser General Public License for more details.
 
 You should have received a copy of the GNU Lesser General Public
 License along with this library; if not, write to the
 Free Software Foundation, Inc., 59 Temple Place - Suite 330,
 Boston, MA  02111-1307, USA.
 *****************************************************************/

package jade.core.sam;

//#DOTNET_EXCLUDE_FILE

import java.util.Date;

import jade.core.IMTPException;
import jade.core.Service;
import jade.core.ServiceException;
import jade.util.Logger;

class Poller extends Thread {
	private SAMService myService;
	private SAMInfoHandler[] handlers;
	private long period;
	private boolean active;
	
	private Logger myLogger = Logger.getMyLogger(getClass().getName()); 
	
	Poller(SAMService service, long p, SAMInfoHandler[] hh) {
		super();
		myService = service;
		period = p;
		handlers = hh;
		setName("SAMService-Poller");
	}
	
	public synchronized void startPolling() {
		active = true;
		start();
	}
	
	public synchronized void stopPolling() {
		active = false;
		interrupt();
	}
	
	public void run() {
		myLogger.log(Logger.CONFIG, "SAMService poller thread started");
		try {
			while (active) {
				Thread.sleep(period);
				poll();
			}
		}
		catch (InterruptedException ie) {
			if (active) {
				myLogger.log(Logger.WARNING, "SAMService poller thread unexpectedly interrupted");
			}
		}
		for (SAMInfoHandler h : handlers) {
			h.shutdown();
		}
		myLogger.log(Logger.CONFIG, "SAMService poller thread terminated");
	}
	
	/**
	 * This is the method that produces a new record for each monitored entity/counter.
	 * It is invoked periodically by the poller Thread. At each invocation it retrieves the 
	 * relevant information from each SAM Service slice.  
	 */
	void poll() {
		myLogger.log(Logger.FINE, "Retrieving SAM information from all nodes");
		Date timeStamp = new Date();
		SAMInfo globalInfo = new SAMInfo();
		try {
			Service.Slice[] slices = myService.getAllSlices();
			for (int i = 0; i < slices.length; i++) {
				SAMSlice s = (SAMSlice) slices[i];
				try {
					startWatchDog();
					SAMInfo sliceInfo = s.getSAMInfo();
					globalInfo.update(sliceInfo);
					myLogger.log(Logger.FINER, "SAM information successfully retrieved from node "+s.getNode().getName());
				}
				catch (Exception imtpe) {
					// Note that getAllSlices() always retrieves "fresh" slices --> no need for any retry
					myLogger.log(Logger.WARNING, "Error retrieving SAM information from node "+s.getNode().getName(), imtpe);
				}
				finally {
					stopWatchDog();
				}
			}
			
			for (SAMInfoHandler h : handlers) {
				h.handle(timeStamp, globalInfo);
			}
		}
		catch (ServiceException se) {
			myLogger.log(Logger.WARNING, "Error retrieving SAM slices", se);
		}
		catch (Exception e) {
			myLogger.log(Logger.WARNING, "Unexpected error polling SAM information", e);
		}
	}
	
	private void startWatchDog() {
		// FIXME: To be implemented
	}

	private void stopWatchDog() {
		// FIXME: To be implemented
	}
	
}
