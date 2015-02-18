package org.nkigen.eqr.agents.behaviours;

import java.awt.Color;
import java.io.IOException;
import java.io.Serializable;

import org.nkigen.eqr.agents.EQRAgentsHelper;
import org.nkigen.eqr.messages.EQRLocationUpdate;
import org.nkigen.eqr.messages.EQRRoutingCriteria;
import org.nkigen.maps.viewer.EQRViewerPoint;
import org.nkigen.maps.viewer.updates.EQRAmbulanceLocations;
import org.nkigen.maps.viewer.updates.EQRFireEngineLocation;
import org.nkigen.maps.viewer.updates.EQRFiresUpdatesItem;
import org.nkigen.maps.viewer.updates.EQRPatientStatusItem;
import org.nkigen.maps.viewer.updates.EQRStatusPanelItem;
import org.nkigen.maps.viewer.updates.EQRUpdateWindow;

import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.OneShotBehaviour;
import jade.lang.acl.ACLMessage;

public class UpdateServerBehaviour extends CyclicBehaviour{

	Agent agent;
	public UpdateServerBehaviour(Agent agent) {
		super(agent);
		this.agent = agent;
	}
	
	@Override
	public void action() {
		ACLMessage msg = agent.receive();
		if (msg == null) {
			System.out.println("Update Server: New message received but its NULL");
			block();
			return;
		}
		System.out.println("Update Server: New message received");
		try {
			System.out.println("Update Server: New message received....Message ok Probing");
			Object content = msg.getContentObject();
			switch (msg.getPerformative()) {
			case ACLMessage.PROPAGATE:
				if(content instanceof EQRLocationUpdate){
					System.out.println("Update Server: Handling a location update");
					agent.addBehaviour(new HandleLocationUpdate((EQRLocationUpdate)content));
				}
				break;
				default:
					break;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private class HandleLocationUpdate extends OneShotBehaviour{
		EQRLocationUpdate msg;
		EQRUpdateWindow win;
		public HandleLocationUpdate(EQRLocationUpdate msg) {
		this.msg = msg;
		win = EQRUpdateWindow.getInstance();
		}
		@Override
		public void action() {
			sendToUpdateWindow();
			sendToViewer();
			
		}
		
		private void sendToViewer(){
			/*Prepare a msg to send to the veiwer agent*/
			int type = msg.getType();
			EQRViewerPoint point = new EQRViewerPoint(msg.getItemId());
			point.setIsMoving(msg.getIsMoving());
			point.setIsDead(msg.getIsDead());
			point.setPoint(msg.getCurrent());
			point.setType(type);
			point.setColor();
			
			//Send Message to viewer Agent 
			AID viewer = EQRAgentsHelper.locateViewer(agent);
			ACLMessage msg2 = new ACLMessage(ACLMessage.INFORM);
				try {
					msg2.setContentObject( point);
				msg2.addReceiver(viewer);
				System.out.println("Contacting Viewer server... Please wait!");
				agent.send(msg2);
				System.out.println("Message send to Viewer ... Please wait!");
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

			
		}
		private void sendToUpdateWindow(){
			int type = msg.getType();
			EQRStatusPanelItem update;
			
			switch(type){
			case EQRLocationUpdate.AMBULANCE_LOCATION:
				update = new EQRAmbulanceLocations();
				((EQRAmbulanceLocations)update).setItem_id(msg.getItemId());
				((EQRAmbulanceLocations)update).setHeading(msg.getHeading());
				((EQRAmbulanceLocations)update).setLocation(msg.getCurrent());
				if(msg.getIsMoving())
					((EQRAmbulanceLocations)update).isAtBase(false);
				win.newItem(EQRStatusPanelItem.AMBULANCE_LOCATION_ITEM, update);
				break;
			case EQRLocationUpdate.FIRE_ENGINE_LOCATION:
				update = new EQRFireEngineLocation();
				((EQRFireEngineLocation)update).setItem_id(msg.getItemId());
				((EQRFireEngineLocation)update).setHeading(msg.getHeading());
				((EQRFireEngineLocation)update).setLocation(msg.getCurrent());
				if(msg.getIsMoving())
					((EQRFireEngineLocation)update).isAtBase(false);
				win.newItem(EQRStatusPanelItem.FIRE_ENGINE_LOCATION_ITEM, update);
				break;
			case EQRLocationUpdate.FIRE_LOCATION:
				update = new EQRFiresUpdatesItem();
				((EQRFiresUpdatesItem)update).setItem_id(msg.getItemId());
				((EQRFiresUpdatesItem)update).setEst_time_to_reach(0);
				((EQRFiresUpdatesItem)update).setFireLocation(msg.getCurrent());
				((EQRFiresUpdatesItem)update).setClosest_engine(msg.getHeading());
				/*Get Closest Engines*/
				break;
			case EQRLocationUpdate.PATIENT_LOCATION:
				/*Get Closest Ambulance*/
				update = new EQRPatientStatusItem();
				((EQRPatientStatusItem)update).setItem_id(msg.getItemId());
				((EQRPatientStatusItem)update).setEst_time_to_reach(0);
				((EQRPatientStatusItem)update).setPatientLocation(msg.getCurrent());
				((EQRPatientStatusItem)update).setClosest_vehicle_loc(msg.getHeading());
				((EQRPatientStatusItem)update).setDeadline(0);
				/*SET DEADLINE*/
				break;
			}
			
			
		}
		
	}

}
