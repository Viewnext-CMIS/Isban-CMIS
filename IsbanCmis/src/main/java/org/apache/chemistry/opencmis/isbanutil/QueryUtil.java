package org.apache.chemistry.opencmis.isbanutil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class QueryUtil {

	private static Map<String, String> listaTradMetadatas;
	static {
		listaTradMetadatas = new HashMap<>();
		listaTradMetadatas.put("cmis:name", "title");
		listaTradMetadatas.put("cmis:objectid", "pdid");
		listaTradMetadatas.put("cmis:parentid", "parentFoder");
		listaTradMetadatas.put("cmis:contentstreamfilename", "fileName");
		listaTradMetadatas.put("cmis:contentstreammimetype", "mime");
		listaTradMetadatas = Collections.unmodifiableMap(listaTradMetadatas);
	}

	public static HashMap<String, Object> getPropertiesStatement(String statement) {
		HashMap<String, Object> salida = null;
		try {
			salida = new HashMap<>();
			salida.put("SELECT", getParamSelect(statement));
			salida.put("FROM", getParamFrom(statement));

		} catch (Exception e) {

		}
		return salida;

	}

	private static List<String> getParamFrom(String statement) {
		List<String> listaTipos = new ArrayList<>();
		String[] afrom;
		if (statement.contains("where")) {
			afrom = statement.substring(statement.toLowerCase().indexOf(" from ")+6, statement.toLowerCase().indexOf(" where ")).split(",");
		}else if (statement.contains("order by")){
			afrom = statement.substring(statement.toLowerCase().indexOf(" from ")+6, statement.toLowerCase().indexOf(" order by ")).split(",");
		}else {
			afrom = statement.substring(statement.toLowerCase().indexOf(" from ")+6, statement.toLowerCase().length()).split(",");
		}
		String[] atipo = null;
		for(String tipo : afrom) {
			if(tipo.toLowerCase().contains(" as ")) {
				tipo=tipo.substring(0,tipo.toLowerCase().indexOf(" as "));
			}
			if(tipo.contains(":")) {
				atipo= tipo.split(":");
				listaTipos.add(atipo[1]);
			}else {
				listaTipos.add(tipo);
			}
			
		}

		return listaTipos;
	}

	private static List<String> getParamSelect(String sql) {
		sql = sql.toLowerCase();
		String select = sql.substring(6, sql.toLowerCase().indexOf("from")).trim();
		String[] aSelect = select.split(",");
		List<String> salida = new ArrayList<>();
		for (String a : aSelect) {
			a=a.trim();
			salida.add((listaTradMetadatas.get(a) != null) ? listaTradMetadatas.get(a) : a);

		}

		return salida;

	}

}
