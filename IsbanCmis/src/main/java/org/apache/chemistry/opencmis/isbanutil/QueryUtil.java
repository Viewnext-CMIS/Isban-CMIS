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

	public static String traduccionCmis(String campo) {
		return listaTradMetadatas.get(campo)!=null? listaTradMetadatas.get(campo) : campo;
		
	}
	
	public static String adaptarAProdoc(String select) {
		select= select.replace(",", " , ");
		for(String key : listaTradMetadatas.keySet()) {
			select=select.replace(key, traduccionCmis(key));
		}
		select=select.replace("cmis:", "");
		return select;
		
	}

}
