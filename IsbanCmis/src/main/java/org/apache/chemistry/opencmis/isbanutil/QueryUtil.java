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
		select = adaptarInFolder(select);
		select = delContains(select);
		select = delInTree(select);
		select= eliminarWhere(select);
		
		return select;
	}

	private static String delInTree(String select) {
		String salida = select;
		if (select.toLowerCase().contains("in_tree")) {
			int firstPos = select.toLowerCase().indexOf("in_tree");
			int lastPost = select.toLowerCase().lastIndexOf("in_tree");
			String ini = select.substring(0, firstPos).trim();
			String end = select.substring(lastPost, select.length());
			end=end.substring(end.indexOf("')")+2,end.length());
			end=eliminarOpLogico(end);
			salida=ini+" "+end;

		}
		return salida;
	}
	
	private static String eliminarOpLogico (String fin) {
		if (fin.trim().length() > 0) {
			if (fin.toLowerCase().indexOf("and") > 0) {
				fin = fin.substring(fin.toLowerCase().indexOf("and") + 3, fin.length()).trim();
			} else if (fin.toLowerCase().indexOf("or") > 0) {
				fin = fin.substring(fin.toLowerCase().indexOf("or") + 2, fin.length()).trim();
			}
		}
		return fin;
	}
	
	private static String eliminarWhere(String select) {
		int wherePos=select.toLowerCase().indexOf("where")+5;
		if(wherePos>=select.trim().length()) {
			select=select.substring(0,select.toLowerCase().indexOf("where"));
		}
		
		return select;
	}

	private static String delContains(String select) {
		String salida = select;
		if (select.toLowerCase().indexOf("contains") > 0) {
			int firstPos = select.toLowerCase().indexOf("contains");
			int lastPos = select.toLowerCase().lastIndexOf("contains") + 8;
			String ini = select.substring(0, firstPos).trim();
			String fin = select.substring(lastPos, select.length()).trim();
			fin = fin.substring(fin.indexOf("')") + 2, fin.length());
			fin=eliminarOpLogico(fin);
		
			salida = ini + " " + fin;
		}

		return salida;
	}

	private static String traducirCmis(String select) {
		select = select.replace(",", " , ");
		for (String key : listaTradMetadatas.keySet()) {
			select = select.replace(key, traduccionCmis(key));
		}
		select = select.replace("cmis:", "");

		return select;
	}

	public static String adaptarInTree(String select) {
		boolean encontrado = false;
		if (select.toLowerCase().contains("in_tree(") || select.toLowerCase().contains("in_tree (")) {
			if (select.toLowerCase().indexOf("in_tree(") == -1) {
				int punto = select.toLowerCase().indexOf("in_tree (");
				select = select.substring(punto + 8, select.length());
				if (select.toLowerCase().contains("in_tree(") || select.toLowerCase().contains("in_tree ("))
					encontrado = true;
			} else if (select.toLowerCase().indexOf("in_tree(") == -1) {
				int punto = select.toLowerCase().indexOf("in_tree (");
				select = select.substring(punto + 7, select.length());
				if (select.toLowerCase().contains("in_tree(") || select.toLowerCase().contains("in_tree ("))
					encontrado = true;
			} else if ((select.toLowerCase().indexOf("in_tree(") < select.toLowerCase().indexOf("in_tree ("))
					|| select.toLowerCase().indexOf("in_tree (") < 0) {
				int punto = select.toLowerCase().indexOf("in_tree(");
				select = select.substring(punto + 7, select.length());
				if (select.toLowerCase().contains("in_tree(") || select.toLowerCase().contains("in_tree ("))
					encontrado = true;
			} else if ((select.toLowerCase().indexOf("in_tree(") > select.toLowerCase().indexOf("in_tree ("))
					|| select.toLowerCase().indexOf("in_tree(") < 0) {
				int punto = select.toLowerCase().indexOf("in_tree (");
				select = select.substring(punto + 8, select.length());
				if (select.toLowerCase().contains("in_tree(") || select.toLowerCase().contains("in_tree ("))
					encontrado = true;
			}
			if (encontrado) {
				select = adaptarInTree(select);
			} else {
				select = getInTree(select);
			}
		} else {
			select = "";
		}
		return select;
	}

	private static String getInTree(String select) {

		String salida = "";
		if (select.indexOf(",") > 0) {
			salida = (select.split(","))[1].trim();
			salida = salida.substring(1, salida.indexOf("')"));
		} else {
			salida = select.substring(2, select.length() - 2);
		}

		return salida;
	}

	private static String adaptarInFolder(String select) {

		boolean encontrado = false;
		if (select.toLowerCase().contains("in_folder(") || select.toLowerCase().contains("in_folder (")) {
			if (select.toLowerCase().indexOf("in_folder(") == -1) {
				int punto = select.toLowerCase().indexOf("in_folder (");
				String inFolder = select.substring(punto, punto + 11);
				select = getInFolder(select, inFolder);
				if (select.toLowerCase().contains("in_folder(") || select.toLowerCase().contains("in_folder ("))
					encontrado = true;
			} else if (select.toLowerCase().indexOf("in_folder (") == -1) {
				int punto = select.toLowerCase().indexOf("in_folder(");
				String inFolder = select.substring(punto, punto + 10);
				select = getInFolder(select, inFolder);
				if (select.toLowerCase().contains("in_folder(") || select.toLowerCase().contains("in_folder ("))
					encontrado = true;
			} else if ((select.toLowerCase().indexOf("in_folder(") < select.toLowerCase().indexOf("in_folder ("))
					|| select.toLowerCase().indexOf("in_folder (") < 0) {
				int punto = select.toLowerCase().indexOf("in_folder(");
				String inFolder = select.substring(punto, punto + 10);
				select = getInFolder(select, inFolder);
				if (select.toLowerCase().contains("in_folder(") || select.toLowerCase().contains("in_folder ("))
					encontrado = true;
			} else if ((select.toLowerCase().indexOf("in_folder(") > select.toLowerCase().indexOf("in_folder ("))
					|| select.toLowerCase().indexOf("in_folder(") < 0) {
				int punto = select.toLowerCase().indexOf("in_folder (");
				String inFolder = select.substring(punto, punto + 11);
				select = getInFolder(select, inFolder);
				if (select.toLowerCase().contains("in_folder(") || select.toLowerCase().contains("in_folder ("))
					encontrado = true;
			}
			if (encontrado) {
				select = adaptarInFolder(select);
			}
		}
		return select;

	}

	private static String getInFolder(String select, String inFolder) {
		String first = select.substring(0, select.indexOf(inFolder));
		String end = select.substring(first.length() + ("in_folder".length()), select.length());
		String valor = end.substring(0, end.indexOf("\")") + 2);
		end = end.substring(valor.length(), end.length());
		valor = valor.replace("(", "");
		valor = valor.replace(")", "");
		valor = valor.replace("\"", "");
		if (valor.contains(",")) {
			valor = (valor.split(","))[1].trim();
		}

		select = first + "function_InFolder=" + "\"" + valor + "\"" + end;

		select.toString();

		return select;
	}

	public static String adaptarContains(String select) {
		boolean encontrado = false;
		if (select.toLowerCase().contains("contains(") || select.toLowerCase().contains("contains (")) {
			if (select.toLowerCase().indexOf("contains(") == -1) {
				int punto = select.toLowerCase().indexOf("contains (");
				select = select.substring(punto + 11, select.length());
				if (select.toLowerCase().contains("contains(") || select.toLowerCase().contains("contains ("))
					encontrado = true;
			} else if (select.toLowerCase().indexOf("contains (") == -1) {
				int punto = select.toLowerCase().indexOf("contains(");
				select = select.substring(punto + 10, select.length());
				if (select.toLowerCase().contains("contains(") || select.toLowerCase().contains("contains ("))
					encontrado = true;
			} else if ((select.toLowerCase().indexOf("contains(") < select.toLowerCase().indexOf("contains ("))) {
				int punto = select.toLowerCase().indexOf("contains(");
				select = select.substring(punto + 10, select.length());
				if (select.toLowerCase().contains("contains(") || select.toLowerCase().contains("contains ("))
					encontrado = true;
			} else if ((select.toLowerCase().indexOf("contains(") > select.toLowerCase().indexOf("contains ("))) {
				int punto = select.toLowerCase().indexOf("contains (");
				select = select.substring(punto + 11, select.length());
				if (select.toLowerCase().contains("contains(") || select.toLowerCase().contains("contains ("))
					encontrado = true;
			}
			if (encontrado) {
				select = adaptarContains(select);
			} else {
				select = getContains(select);
			}
		} else {
			select = "";
		}

		return select;
	}

	private static String getContains(String statement) {
		String salida = statement.substring(0, statement.indexOf("')"));
		return salida;
	}

}
