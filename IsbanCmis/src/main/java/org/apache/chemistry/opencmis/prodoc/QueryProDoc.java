package org.apache.chemistry.opencmis.prodoc;

import java.util.List;

import prodoc.DriverGeneric;
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
	 * @param camposSelect 
	 * @return
	 */
	public static List<Object> busquedaFolder(String query, DriverGeneric sesion,String docType, List<String> camposSelect) {
		makeQuery(sesion, query, true, docType);
		return null;
	}

	/**
	 * 
	 * @param query
	 * @param sesion
	 * @param docType
	 * @param camposSelect 
	 * @return
	 */
	public static List<Object> busquedaDoc(String query, DriverGeneric sesion, String docType, List<String> camposSelect) {
		makeQuery(sesion, query, false, docType);
		return null;
	}
	/**
	 * 
	 * @return
	 */
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
