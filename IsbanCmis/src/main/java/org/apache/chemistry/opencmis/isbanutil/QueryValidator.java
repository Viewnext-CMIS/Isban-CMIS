package org.apache.chemistry.opencmis.isbanutil;

import org.apache.chemistry.opencmis.commons.exceptions.CmisInvalidArgumentException;

public class QueryValidator {
	/**
	 * 
	 * @param query
	 * @return
	 */
	public static boolean validarStatement(String query) throws CmisInvalidArgumentException {
		boolean validado = false;
		// Obligatorios

		String select = query.substring(0, query.toLowerCase().indexOf("from"));
		validado = valSelect(select); // SELECT VALIDATION

		if (validado) {// FROM VALIDATION
			if(query.toLowerCase().indexOf("where")>0) {
			String from = query.substring(query.toLowerCase().indexOf("from") + 4,
					query.toLowerCase().indexOf("where"));
			validado = valFrom(from);
			}else {
				throw new CmisInvalidArgumentException ("A minimum condition is needed.");
			}
		}
		// Opcionales

		if (validado) {
			if (query.toLowerCase().indexOf("order by") > 0) { // WHERE VALIDATION
				String where = query.substring(query.toLowerCase().indexOf("where") + 5,
						query.toLowerCase().indexOf("order by"));
				validado = valWhere(where);
			} else {
				String where = query.substring(query.toLowerCase().indexOf("where") + 5, query.length());
				validado = valWhere(where);
			}

		}
		if (validado) {
			if (query.toLowerCase().indexOf("order by") > 0) { // ORDER-BY VALIDATION
				String orderBy = query.substring(query.toLowerCase().indexOf("order by") + 8, query.length());
				validado = valOrder(orderBy);
			}
		}
		return validado;
	}

	/**
	 * 
	 * @param select
	 * @return
	 */
	private static boolean valSelect(String select) {
		boolean validado = true;

		return validado;
	}

	/**
	 * 
	 * @param from
	 * @return
	 */
	private static boolean valFrom(String from) {
		boolean validado = true;

		return validado;
	}

	/**
	 * 
	 * @param where
	 * @return
	 */
	private static boolean valWhere(String where) {
		boolean validado = false;
		// Comprobamos el primer nivel y contains and tree
		
		if (!valContains(where) || !valInTree(where) ) {
			validado = false;
		} else {
			validado = true;
		}

		return validado;
	}

	/**
	 * 
	 * @param orderby
	 * @return
	 */
	private static boolean valOrder(String orderby) {
		boolean validado = true;

		return validado;
	}

	private static boolean valInTree(String where) {// Comprobamos el primer nivel de contains
		boolean validado = true;
		if (where.toLowerCase().indexOf("in_tree") > 0) {
			int or = where.toLowerCase().indexOf(" or ");
			int contains = where.toLowerCase().indexOf("in_tree");

			if (contains != where.toLowerCase().lastIndexOf("in_tree"))
				validado = false;
			if (validado && contains > or && or>0)
				validado = false;
		}
		return validado;
	}

	private static boolean valContains(String where) {// Comprobamos el primer nivel de inTree
		boolean validado = true;
		if (where.toLowerCase().indexOf("contains") > 0) {
			int or = where.toLowerCase().indexOf(" or ");
			int contains = where.toLowerCase().indexOf("contains");

			if (contains != where.toLowerCase().lastIndexOf("contains")) // Solo existe un contains
				validado = false;
			if (validado && contains > or && or>0) //El contains esta antes del primer OR
				validado = false;
		}
		return validado;
	}
}
