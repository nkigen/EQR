package org.nkigen.eqr.agents.behaviours;

import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.Behaviour;
import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.UnreadableException;

import org.nkigen.eqr.ambulance.AmbulanceDetails;
import org.nkigen.eqr.ambulance.AmbulanceGoals;
import org.nkigen.eqr.common.EmergencyDetails;
import org.nkigen.eqr.common.EmergencyStateChangeInitiator;
import org.nkigen.eqr.common.EmergencyStateChangeListener;
import org.nkigen.eqr.messages.AmbulanceNotifyMessage;
import org.nkigen.eqr.models.EQREmergencyPoint;

public class AmbulanceBehaviour extends CyclicBehaviour implements
		EmergencyStateChangeListener {

	AmbulanceGoals goals;
	AmbulanceDetails details;

	public AmbulanceBehaviour(Agent agent) {
		super(agent);
		EmergencyStateChangeInitiator.getInstance().addListener(this);
		goals = new AmbulanceGoals();
	}

	@Override
	public void action() {
		ACLMessage msg = myAgent.receive();
		if (msg != null) {
			switch (msg.getPerformative()) {
			case ACLMessage.INFORM:
				try {
					Object content = msg.getContentObject();
					if (content instanceof AmbulanceNotifyMessage) {
						Object[] params = new Object[3];
						params[0] = myAgent;
						params[1] = (AmbulanceNotifyMessage) content;
						params[2] = details;

						Behaviour b = goals.executePlan(
								AmbulanceGoals.PICK_PATIENT, params);
						if (b != null)
							myAgent.addBehaviour(b);
					}
				} catch (UnreadableException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		} else {
			block();
		}
	}

	@Override
	public void onEmergencyStateChange(EmergencyDetails ed) {
		// TODO Auto-generated method stub

	}

}
