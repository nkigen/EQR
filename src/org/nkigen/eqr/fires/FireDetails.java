package org.nkigen.eqr.fires;

import org.nkigen.eqr.common.EmergencyDetails;
import org.nkigen.eqr.common.EmergencyStateChangeInitiator;

public class FireDetails  extends EmergencyDetails{

	int status;
	
	public FireDetails() {
	
	}

	public int getStatus() {
		return status;
	}

	public void setStatus(int status) {
		this.status = status;
		EmergencyStateChangeInitiator.getInstance().notifyStateChanged(this);
	}
}
