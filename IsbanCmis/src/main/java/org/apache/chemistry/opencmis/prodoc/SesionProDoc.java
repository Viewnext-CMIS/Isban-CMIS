package org.apache.chemistry.opencmis.prodoc;

import prodoc.DriverGeneric;
import prodoc.ProdocFW;
/**
 * 
 * @author Viewnext:Sergio Rodriguez Oyola
 *
 */
public class SesionProDoc {

	private DriverGeneric MainSession = null;

	private String user;
	private String pass;
/**
 * Sesion de OpenProDoc
 * @param conector
 * @param properties
 * @param user
 * @param pass
 */
	public SesionProDoc(String conector, String properties, String user, String pass) {
		try {
			if (conector != null && properties != null && user != null && pass != null) {
				if (!conector.isEmpty() && !properties.isEmpty() && !user.isEmpty()) {
					ProdocFW.InitProdoc(conector, properties);
					this.MainSession = ProdocFW.getSession(conector, user, pass);
					this.user = user;
					this.pass = pass;
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public DriverGeneric getMainSession() {
		return MainSession;
	}

	public void setMainSession(DriverGeneric mainSession) {
		MainSession = mainSession;
	}

	public String getUser() {
		return user;
	}

	public String getPass() {
		return pass;
	}

}
