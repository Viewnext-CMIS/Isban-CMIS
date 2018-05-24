package org.apache.chemistry.opencmis.prodoc;

import prodoc.DriverGeneric;
import prodoc.PDDocs;
import prodoc.Query;

/**
 * 
 * @author Viewnext:Sergio Rodriguez Oyola
 *
 */
public class QueryProDoc {

	public QueryProDoc() {

	}

	/**
	 * 
	 * @param query
	 * @param sesion
	 * @return
	 */
	public static String busquedaFolder(String query, DriverGeneric sesion,String docType) {
		makeQuery(sesion, query, true, docType);
		return null;
	}

	/**
	 * 
	 * @param query
	 * @param sesion
	 * @param docType
	 * @return
	 */
	public static String busquedaDoc(String query, DriverGeneric sesion, String docType) {
		makeQuery(sesion, query, false, docType);
		return null;
	}
	
	public static String busquedaVersion() {
		return null;

	}

	/**
	 * 
	 * @param sesion
	 * @param query
	 * @param isFolder
	 * @param docType
	 * @return
	 */
	private static Query makeQuery(DriverGeneric sesion, String query, Boolean isFolder, String docType) {
		return null;
	}

}
