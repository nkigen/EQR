package org.nkigen.eqr.agents;

import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

import org.nkigen.eqr.common.EQRAgentTypes;
import org.nkigen.eqr.logs.EQRLogger;
import org.nkigen.maps.routing.EQRPoint;

import jade.core.AID;
import jade.core.Agent;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.util.Logger;

public class EQRAgentsHelper {
	static AID update_server;
	static AID routing_server;
	static AID viewer;
	static AID command_center;

	/*
	 * Different types of objects in the experiment
	 */
	// public static

	public static String getCurrentTime() {
		return " ";// experiment.getSimClock().getTime().toString();

	}

	public static long getCurrentTime(boolean t) {

		return 0;// experiment.getSimClock().getTime().getTimeRounded();

	}

	
	public static final double EARTH_RADIUS = 6372.8 * 1000; // In kilometers

	public static double haversine(double lat1, double lon1, double lat2,
			double lon2) {
		double dLat = Math.toRadians(lat2 - lat1);
		double dLon = Math.toRadians(lon2 - lon1);
		Double latDistance =  Math.toRadians(lat2 - lat1);
        Double lonDistance = Math.toRadians(lon2 - lon1);
		   Double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2) + 
                   Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) * 
                   Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);
        Double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));
        
		return EARTH_RADIUS * c;
	}

	public static double getDistanceFromGPSCood(EQRPoint p1, EQRPoint p2) {
		return haversine(p1.getLatitude(), p1.getLongitude(), p2.getLatitude(),
				p2.getLongitude());
	}

	public static AID locateUpdateServer(Agent agent) {

		if (update_server != null)
			return update_server;
		System.out.println("Trying to locate the Update server");
		ServiceDescription sd = new ServiceDescription();
		sd.setType(EQRAgentTypes.UPDATES_AGENT);
		DFAgentDescription dfd = new DFAgentDescription();
		dfd.addServices(sd);
		try {
			DFAgentDescription[] dfds = DFService.search(agent, dfd);
			if (dfds.length > 0) {
				update_server = dfds[0].getName();
				System.out.println("Update Server found");
			} else
				System.out.println("Couldn't locate server!");
		} catch (Exception ex) {
			ex.printStackTrace();
			System.out.println("Failed searching int the DF!");
		}
		return update_server;
	}

	public static AID locateControlCenter(Agent agent) {

		if (command_center != null)
			return command_center;
		DFAgentDescription template = new DFAgentDescription();
		ServiceDescription sd = new ServiceDescription();
		sd.setType(EQRAgentTypes.EMERGENCY_CONTROL_CENTER_AGENT);
		template.addServices(sd);
		try {
			DFAgentDescription[] result = DFService.search(agent, template);
			if (result.length > 0)
				command_center = result[0].getName();

		} catch (FIPAException fe) {
			Logger logger = null;
			logger = EQRLogger.prep(logger, agent.getLocalName());
			EQRLogger.log(logger, null, agent.getLocalName(), fe
					.getStackTrace().toString());
			fe.printStackTrace();
		}

		return command_center;
	}

	public static AID locateRoutingServer(Agent agent) {

		if (routing_server != null)
			return routing_server;

		System.out.println("Trying to locate the routing server");
		ServiceDescription sd = new ServiceDescription();
		sd.setType(EQRAgentTypes.ROUTING_AGENT);
		DFAgentDescription dfd = new DFAgentDescription();
		dfd.addServices(sd);
		try {
			DFAgentDescription[] dfds = DFService.search(agent, dfd);
			if (dfds.length > 0) {
				routing_server = dfds[0].getName();
				System.out.println("Router found");
			} else
				System.out.println("Couldn't localize server!");
		} catch (Exception ex) {
			ex.printStackTrace();
			System.out.println("Failed searching int the DF!");
		}
		return routing_server;
	}

	public static AID locateViewer(Agent agent) {
		if (viewer != null)
			return viewer;
		// System.out.println("Trying to locate the Viewer server");
		ServiceDescription sd = new ServiceDescription();
		sd.setType(EQRAgentTypes.VIEWER_AGENT);
		DFAgentDescription dfd = new DFAgentDescription();
		dfd.addServices(sd);
		try {
			DFAgentDescription[] dfds = DFService.search(agent, dfd);
			if (dfds.length > 0) {
				viewer = dfds[0].getName();
				System.out.println("Viewer Server found "
						+ viewer.getLocalName() + " " + viewer.getName());
			}
			// else
			// System.out.println("Couldn't locate Viewer server!");
		} catch (Exception ex) {
			ex.printStackTrace();
			System.out.println("Failed for viewer searching int the DF!");
		}
		return viewer;
	}

	public static ArrayList<AID> locateBases(String base, Agent agent) {
		ArrayList<AID> bases = new ArrayList<AID>();

		DFAgentDescription template = new DFAgentDescription();
		ServiceDescription sd = new ServiceDescription();
		sd.setType(base);
		template.addServices(sd);
		try {
			DFAgentDescription[] result = DFService.search(agent, template);
			bases.clear();
			System.out.println(result.length);
			for (int i = 0; i < result.length; ++i) {
				bases.add(result[i].getName());
				System.out.println(result[i].getName().getLocalName()
						+ " Added to Bases of " + base);
			}
			return bases;
		} catch (FIPAException fe) {
			fe.printStackTrace();
		}
		return null;
	}

}
