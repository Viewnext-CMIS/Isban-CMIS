package org.apache.chemistry.opencmis.fileshare;

import prodoc.DriverGeneric;
import prodoc.ProdocFW;

public class SesionProDoc {

	private DriverGeneric MainSession = null;
	public  DriverGeneric getMainSession() {
		return MainSession;
	}

	public void setMainSession(DriverGeneric mainSession) {
		MainSession = mainSession;
	}

	private String user;
	private String pass;

	public SesionProDoc(String conector, String properties, String user, String pass) {
		try {
			if (conector != null && properties != null && user != null && pass != null) {
				if (!conector.isEmpty() && !properties.isEmpty() && !user.isEmpty()) {
					ProdocFW.InitProdoc(conector, properties);
					this.MainSession = ProdocFW.getSession(conector, user, pass);
					this.user=user;
					this.pass=pass;
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public String getUser() {
		return user;
	}
	public String getPass() {
		return pass;
	}

}
