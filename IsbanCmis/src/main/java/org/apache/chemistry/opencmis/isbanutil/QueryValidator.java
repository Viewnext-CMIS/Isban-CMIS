package org.apache.chemistry.opencmis.isbanutil;

public class QueryValidator {

	public static boolean validarStatement(String query) {
		boolean validado=false;
		String select = query.substring(0, query.toLowerCase().indexOf("from"));
		validado=valSelect(select);
		
		String from=query.substring(query.toLowerCase().indexOf("from")+4,query.toLowerCase().indexOf("where"));
		validado=valFrom(from);
		
		String where=query.substring(query.toLowerCase().indexOf("where")+5,query.toLowerCase().indexOf("order by"));
		validado=valWhere(where);
		
		String orderBy=query.substring(query.toLowerCase().indexOf("order by"),query.length());
		validado=valOrder(orderBy);
		
		return validado;
	}
	private static boolean valSelect(String select) {
		boolean validado=false;
		
		return validado;
	}
	private static boolean valFrom(String from) {
		boolean validado=false;
		
		return validado;
	}
	
	private static boolean valWhere(String where) {
		boolean validado=false;
		
		return validado;
	}
	private static boolean valOrder(String orderby) {
		boolean validado=false;
		
		return validado;
	}
}
