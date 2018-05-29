package org.apache.chemistry.opencmis.isbanutil;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class QueryUtil {

	public static Map<String, String> listaTradMetadatas;
	static {
		listaTradMetadatas = new HashMap<>();
		listaTradMetadatas.put("cmis:name", "Title");
		listaTradMetadatas.put("cmis:objectId", "PDId");
		listaTradMetadatas.put("cmis:parentId", "ParentId");
		listaTradMetadatas.put("cmis:createdBy", "PDAutor");
		listaTradMetadatas.put("cmis:creationDate", "PDDate");
		listaTradMetadatas = Collections.unmodifiableMap(listaTradMetadatas);
	}

	static final HashMap<String, Integer> valOperComp = new HashMap<String, Integer>() {
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;

		{
			put("=", 0);
			put(">", 1);
			put("<", 2);
			put(">=", 3);
			put("<=", 4);
			put("<>", 5);
			// put("cINList", 6);
			// put("cINQuery", 7);
			put("like", 8);
		}
	};

	public static Integer getProdocOper(String operador) {
		return valOperComp.get(operador.trim()) != null ? valOperComp.get(operador.trim()) : null;
	}

	public static String traduccionCmis(String campo) {
		return listaTradMetadatas.get(campo) != null ? listaTradMetadatas.get(campo) : campo;

	}

	public static String adaptarAProdoc(String select) {
		select = traducirCmis(select);
		select = adaptarContains(select);
		return select;
	}

	private static String traducirCmis(String select) {
		select = select.replace(",", " , ");
		for (String key : listaTradMetadatas.keySet()) {
			select = select.replace(key, traduccionCmis(key));
		}
		select = select.replace("cmis:", "");

		return select;
	}

	private static String adaptarInTree(String select) {
		return select;
	}

	private static String adaptarInFolder(String select) {
		boolean encontrado = false;
		if (select.contains("in_folder(")) {

		} else if (select.contains("in_folder (")) {

		} else 	if(select.contains("IN_FOLDER(")) {
			
		}else {
			//TODO: Continuar luego
		}

			return select;

	}

	private static String adaptarContains(String select) {

		boolean encontrado = false;
		if (select.contains("contains(")) {
			select = getContains(select, "contains(");
			encontrado = true;
		} else if (select.contains("contains (")) {
			select = getContains(select, "contains (");
			encontrado = true;
		} else if (select.contains("Contains (")) {
			select = getContains(select, "Contains (");
			encontrado = true;
		} else if (select.contains("Contains(")) {
			select = getContains(select, "Contains(");
			encontrado = true;
		} else if (select.contains("CONTAINS(")) {
			select = getContains(select, "CONTAINS(");
			encontrado = true;
		} else if (select.contains("CONTAINS (")) {
			select = getContains(select, "CONTAINS (");
			encontrado = true;
		}
		if (encontrado) {
			select = adaptarAProdoc(select);
		}
		return select;

	}

	private static String getContains(String statement, String contains) {
		String salida = "";
		String first = statement.substring(0, statement.indexOf(contains));
		String end = statement.substring(statement.indexOf(contains) + contains.length() - 1, statement.length());
		String medio = end.substring(0, end.indexOf("\")") + 2);
		salida += first + " " + "function_contains=";
		salida += medio.substring(1, medio.length() - 1) + " ";
		salida += end.substring(medio.length(), end.length());
		return salida;
	}

}
