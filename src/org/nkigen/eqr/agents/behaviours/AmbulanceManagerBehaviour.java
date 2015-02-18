package org.nkigen.eqr.agents.behaviours;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.nkigen.eqr.agents.EQRAgentsHelper;
import org.nkigen.eqr.messages.EQRLocationUpdate;
import org.nkigen.eqr.messages.EQRRoutingResult;
import org.nkigen.eqr.models.EQREmergencyPoint;
import org.nkigen.maps.routing.EQRPoint;
import org.nkigen.maps.routing.graphhopper.EQRGraphHopperResult;
import org.nkigen.maps.viewer.updates.EQRAmbulanceLocations;
import org.nkigen.maps.viewer.updates.EQRPatientStatusItem;

import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.SimpleBehaviour;
import jade.lang.acl.ACLMessage;

public class AmbulanceManagerBehaviour extends SimpleBehaviour {

	boolean done;
	EQRRoutingResult route;
	EQREmergencyPoint ambulance;
	EQREmergencyPoint patient;
	AID update_server;

	List<EQRPoint> route_to_patient;
	long duration;
	int next_index;
	public AmbulanceManagerBehaviour(Agent agent,EQRRoutingResult route,
			EQREmergencyPoint ambulance, EQREmergencyPoint patient) {
		super(agent);
		this.ambulance = ambulance;
		this.patient = patient;
		this.route = route;
		if(route instanceof EQRGraphHopperResult){
			route_to_patient = ((EQRGraphHopperResult) route).getPoints();
			duration = ((EQRGraphHopperResult) route).getDuration();
		}
		update_server = EQRAgentsHelper.locateUpdateServer(myAgent);
	}

	@Override
	public void action() {
		
		/*TODO: Add traffic data*/
		if(next_index < route_to_patient.size()){
			EQRPoint point = route_to_patient.get(next_index);
			EQRLocationUpdate patient_loc1 = new EQRLocationUpdate(
					EQRLocationUpdate.AMBULANCE_LOCATION, ambulance.getId());
			EQRLocationUpdate loc1 = new EQRLocationUpdate(
					EQRLocationUpdate.PATIENT_LOCATION, patient.getId());
			
			patient_loc1.setIsMoving(true);
			patient_loc1.setIsDead(false);
			patient_loc1.setItemId(ambulance.getId());
			patient_loc1.setCurrent(point);
			patient_loc1.setHeading(new EQRPoint(patient.getLatitude(), patient
					.getLongitude()));
			patient_loc1.setIsMoving(true);
			


			if (next_index == 0) {
				loc1.setIsMoving(false);
				loc1.setIsDead(false);
				loc1.setCurrent(new EQRPoint(patient.getLatitude(), patient
						.getLongitude()));
				loc1.setHeading(point);
				loc1.setIsMoving(false);
				loc1.setItemId(patient.getId());
			}

			if(update_server == null){
				update_server = EQRAgentsHelper.locateUpdateServer(myAgent);
			}
			
			ACLMessage msg1 = new ACLMessage(ACLMessage.PROPAGATE);
			ACLMessage msg2 = new ACLMessage(ACLMessage.PROPAGATE);
			try {
				msg1.setContentObject(patient_loc1);
				msg2.setContentObject(loc1);
				msg1.addReceiver(update_server);
				msg2.addReceiver(update_server);
				myAgent.send(msg1);
				myAgent.send(msg2);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			next_index++;
			block(duration/route_to_patient.size());
		}

	}

	@Override
	public boolean done() {
		// TODO Auto-generated method stub
		return false;
	}

}
