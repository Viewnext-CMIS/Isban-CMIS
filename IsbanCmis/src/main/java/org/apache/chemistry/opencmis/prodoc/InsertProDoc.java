package org.apache.chemistry.opencmis.prodoc;

import org.apache.chemistry.opencmis.commons.data.Properties;
import org.apache.chemistry.opencmis.commons.data.PropertyData;

import prodoc.PDException;
import prodoc.PDFolders;
/**
 * 
 * @author Viewnext:Sergio Rodriguez Oyola
 *
 */
public class InsertProDoc {
	
	public InsertProDoc() {		
		
	}
	/**
	 * Crea carpetas
	 * @param properties
	 * @param sesion
	 * @return
	 */
	public static String crearCarpeta(Properties properties, SesionProDoc sesion) {

		PDFolders folder = null;
		try {
			PropertyData<?> nombre = properties.getProperties().get("cmis:name");

			folder = new PDFolders(sesion.getMainSession());
			folder.setTitle(nombre.getValues().get(0).toString());
			folder.setParentId("RootFolder");
			folder.insert();

		} catch (PDException e) {
			e.printStackTrace();
		}
		return folder.getPDId();
	}
}
