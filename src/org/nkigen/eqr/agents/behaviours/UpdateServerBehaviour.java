package org.nkigen.eqr.agents.behaviours;

import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.OneShotBehaviour;
import jade.core.behaviours.ThreadedBehaviourFactory;
import jade.lang.acl.ACLMessage;
import jade.util.Logger;

import java.io.IOException;

import org.nkigen.eqr.agents.EQRAgentsHelper;
import org.nkigen.eqr.logs.EQRLogger;
import org.nkigen.eqr.messages.EQRLocationUpdate;
import org.nkigen.eqr.messages.TrafficUpdateMessage;
import org.nkigen.maps.viewer.EQRViewerPoint;
import org.nkigen.maps.viewer.updates.EQRAmbulanceLocations;
import org.nkigen.maps.viewer.updates.EQRFireEngineLocation;
import org.nkigen.maps.viewer.updates.EQRFiresUpdatesItem;
import org.nkigen.maps.viewer.updates.EQRPatientStatusItem;
import org.nkigen.maps.viewer.updates.EQRStatusPanelItem;
import org.nkigen.maps.viewer.updates.EQRUpdateWindow;

public class UpdateServerBehaviour extends CyclicBehaviour {

	//ThreadedBehaviourFactory tbf ;
	Logger logger;
	boolean is_subscribed = false;
	public UpdateServerBehaviour(Agent agent) {
		super(agent);

		//tbf = new ThreadedBehaviourFactory();
		logger = EQRLogger.prep(logger, myAgent.getLocalName());
	}

	@Override
	public void action() {
		if(!is_subscribed){
			TrafficUpdateMessage tum = new TrafficUpdateMessage();
			tum.subscribe();
			ACLMessage msg_tum = new ACLMessage(ACLMessage.SUBSCRIBE);
			AID ecc = EQRAgentsHelper.locateControlCenter(myAgent);
			while(ecc == null)
				ecc= EQRAgentsHelper.locateControlCenter(myAgent);
			msg_tum.addReceiver(ecc);
			try {
				msg_tum.setContentObject(tum);
				myAgent.send(msg_tum);
				is_subscribed = true;
				EQRLogger.log(logger, msg_tum, myAgent.getLocalName(), " Traffic update subscription sent");
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		ACLMessage msg = myAgent.receive();
		if (msg == null) {
			block();
			return;
		}

		EQRLogger.log(logger, msg, myAgent.getLocalName(), "Message received");
		switch (msg.getPerformative()) {
		case ACLMessage.PROPAGATE:
			try {
				Object content = msg.getContentObject();
				if (content instanceof EQRLocationUpdate) {
					myAgent.addBehaviour(new HandleLocationUpdate(
							(EQRLocationUpdate) content));

				}
			} catch (Exception e) {
				e.printStackTrace();
			}

			break;
		case ACLMessage.INFORM:
			try {
				Object content = msg.getContentObject();
				if (content instanceof TrafficUpdateMessage) {
					AID viewer = EQRAgentsHelper.locateViewer(myAgent);
					while(viewer == null)
						viewer = EQRAgentsHelper.locateViewer(myAgent);
					
					ACLMessage msg2 = new ACLMessage(ACLMessage.INFORM);
					try {
						msg2.setContentObject((TrafficUpdateMessage)content);
						msg2.addReceiver(viewer);
						myAgent.send(msg2);
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}

				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		default:
			break;
		}
	}

	private class HandleLocationUpdate extends OneShotBehaviour {
		EQRLocationUpdate msg;
		//EQRUpdateWindow win;

		public HandleLocationUpdate(EQRLocationUpdate msg) {
			this.msg = msg;
			//win = EQRUpdateWindow.getInstance();
		}

		@Override
		public void action() {
			//sendToUpdateWindow();
			sendToViewer();

		}

		private void sendToViewer() {
			/* Prepare a msg to send to the veiwer agent */
			EQRLogger.log(logger, null, myAgent.getLocalName(), "Sending update from "+ msg.getItemId().getLocalName());
			int type = msg.getType();
			EQRViewerPoint point = new EQRViewerPoint(msg.getItemId());
			point.setIsMoving(msg.getIsMoving());
			point.setStatus(msg.getStatus());
			point.setIsDead(msg.getIsDead());
			point.setPoint(msg.getCurrent());
			point.setType(type);
			point.setColor();

			// Send Message to viewer Agent
			AID viewer = EQRAgentsHelper.locateViewer(myAgent);
			while(viewer == null)
				viewer = EQRAgentsHelper.locateViewer(myAgent);
			
			ACLMessage msg2 = new ACLMessage(ACLMessage.INFORM);
			try {
				msg2.setContentObject(point);
				msg2.addReceiver(viewer);
				myAgent.send(msg2);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}
/*
		private void sendToUpdateWindow() {
			int type = msg.getType();
			EQRStatusPanelItem update;

			switch (type) {
			case EQRLocationUpdate.AMBULANCE_LOCATION:
				update = new EQRAmbulanceLocations();
				((EQRAmbulanceLocations) update).setItem_id(msg.getItemId());
				((EQRAmbulanceLocations) update).setHeading(msg.getHeading());
				((EQRAmbulanceLocations) update).setLocation(msg.getCurrent());
				if (msg.getIsMoving())
					((EQRAmbulanceLocations) update).isAtBase(false);
				win.newItem(EQRStatusPanelItem.AMBULANCE_LOCATION_ITEM, update);
				break;
			case EQRLocationUpdate.FIRE_ENGINE_LOCATION:
				update = new EQRFireEngineLocation();
				((EQRFireEngineLocation) update).setItem_id(msg.getItemId());
				((EQRFireEngineLocation) update).setHeading(msg.getHeading());
				((EQRFireEngineLocation) update).setLocation(msg.getCurrent());
				if (msg.getIsMoving())
					((EQRFireEngineLocation) update).isAtBase(false);
				win.newItem(EQRStatusPanelItem.FIRE_ENGINE_LOCATION_ITEM,
						update);
				break;
			case EQRLocationUpdate.FIRE_LOCATION:
				update = new EQRFiresUpdatesItem();
				((EQRFiresUpdatesItem) update).setItem_id(msg.getItemId());
				((EQRFiresUpdatesItem) update).setEst_time_to_reach(0);
				((EQRFiresUpdatesItem) update)
						.setFireLocation(msg.getCurrent());
				((EQRFiresUpdatesItem) update).setClosest_engine(msg
						.getHeading());
				
				break;
			case EQRLocationUpdate.PATIENT_LOCATION:
				
				update = new EQRPatientStatusItem();
				((EQRPatientStatusItem) update).setItem_id(msg.getItemId());
				((EQRPatientStatusItem) update).setEst_time_to_reach(0);
				((EQRPatientStatusItem) update).setPatientLocation(msg
						.getCurrent());
				((EQRPatientStatusItem) update).setClosest_vehicle_loc(msg
						.getHeading());
				((EQRPatientStatusItem) update).setDeadline(0);
			
				break;
			}

		}
*/
	}

}
