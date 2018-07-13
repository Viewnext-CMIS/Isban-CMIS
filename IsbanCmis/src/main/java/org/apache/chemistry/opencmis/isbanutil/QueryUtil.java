package org.apache.chemistry.opencmis.isbanutil;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

import org.apache.chemistry.opencmis.prodoc.QueryProDoc;

import net.sf.jsqlparser.statement.select.PlainSelect;
import prodoc.DriverGeneric;
import prodoc.PDException;
import prodoc.PDObjDefs;

public class QueryUtil {

    public static Map<String, String> listaTradMetadatas;
    static {
        listaTradMetadatas = new HashMap<>();
        listaTradMetadatas.put("cmis:name", "Title");
        listaTradMetadatas.put("cmis:objectId", "PDId");
        listaTradMetadatas.put("cmis:parentId", "ParentId");
        listaTradMetadatas.put("cmis:createdBy", "PDAutor");
        listaTradMetadatas.put("cmis:creationDate", "PDDate");
        listaTradMetadatas.put("cmis:lastModifiedBy", "PDAutor");
        listaTradMetadatas.put("cmis:lastModificationDate", "PDDate");
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
            put("cINList", 6);
            put("cINQuery", 7);
            put("like", 8);
        }
    };

    /**
     * 
     * @param tipo
     * @param statement
     * @param sesion
     * @param selectStatement
     * @return
     * @throws PDException
     */
    public static Vector<String> goToQuery(String tipo, String fulltext, String inTree, DriverGeneric sesion,
            PlainSelect selectStatement) throws PDException {
        Vector<String> listaSalida = new Vector<>();
        if (tipo.equalsIgnoreCase("document") || tipo.equalsIgnoreCase("PD_DOCS")) {

            listaSalida.addAll(QueryProDoc.busquedaDoc(fulltext, inTree, sesion, "PD_DOCS", selectStatement));

        } else if (tipo.equalsIgnoreCase("folder") || tipo.equalsIgnoreCase("PD_FOLDERS")) {
            listaSalida.addAll(QueryProDoc.busquedaFolder(fulltext, inTree, sesion, "PD_FOLDERS", selectStatement));
        } else {
            PDObjDefs od = new PDObjDefs(sesion);
            od.Load(tipo);
            String tipoObj = od.getClassType();
            if (tipoObj.equalsIgnoreCase("document")) {
                listaSalida.addAll(QueryProDoc.busquedaDoc(fulltext, inTree, sesion, tipo, selectStatement));
            } else {
                listaSalida.addAll(QueryProDoc.busquedaFolder(fulltext, inTree, sesion, tipo, selectStatement));
            }
        }
        return listaSalida;
    }

    public static Integer getProdocOper(String operador) {
        return valOperComp.get(operador.trim()) != null ? valOperComp.get(operador.trim()) : null;
    }

    public static String traduccionCmis(String campo) {
        return listaTradMetadatas.get(campo) != null ? listaTradMetadatas.get(campo) : campo;

    }

    private static String getTipoParam(int tipo) {
        String tipoSalida = "";
        switch (tipo) {
        case 0:
            tipoSalida = "Function_Contains";
            break;
        case 1:
            tipoSalida = "Function_InTree";
            break;
        default:
            break;
        }

        return tipoSalida;
    }

    public static String getAddParam(String query, int tipo) {
        String salida = "";
        String tipoParam = getTipoParam(tipo);
        if (query.toLowerCase().indexOf("order by") > 0) {
            query = query.substring(0, query.toLowerCase().indexOf("order by"));
        }
        if (query.indexOf(tipoParam) > 0) {
            String[] aQuery = query.split(tipoParam);
            String aux = aQuery[aQuery.length - 1];
            if (aux.toLowerCase().indexOf(" and ") > 0 && aux.toLowerCase().indexOf(" or ") < 0) {
                salida = aux.substring(aux.indexOf("=") + 1, aux.toLowerCase().indexOf(" and ")).replace("'", "");
            } else if (aux.toLowerCase().indexOf(" and ") < 0 && aux.toLowerCase().indexOf(" or ") > 0) {
                salida = aux.substring(aux.indexOf("=") + 1, aux.toLowerCase().indexOf(" or ")).replace("'", "");
            } else if (aux.toLowerCase().indexOf(" or ") > aux.toLowerCase().indexOf(" and ")) {
                salida = aux.substring(aux.indexOf("=") + 1, aux.toLowerCase().indexOf(" and ")).replace("'", "");
            } else if (aux.toLowerCase().indexOf(" or ") < aux.toLowerCase().indexOf(" and ")) {
                salida = aux.substring(aux.indexOf("=") + 1, aux.toLowerCase().indexOf(" or ")).replace("'", "");
            } else {
                salida = aux.substring(aux.indexOf("=") + 1, aux.length()).replace("'", "");
            }

        }

        return salida;

    }

    public static String adaptarAProdoc(String select) {
        select = traducirCmis(select);
        select = adaptarContains(select);
        select = adaptarInFolder(select);
        select = adaptarInTree(select);
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
        boolean encontrado = false;
        if (select.toLowerCase().contains("in_tree(") || select.toLowerCase().contains("in_tree (")) {
            if (select.toLowerCase().indexOf("in_tree(") == -1) {
                int punto = select.toLowerCase().indexOf("in_tree (");
                String inTree = select.substring(punto, punto + 9);
                select = getInTree(select, inTree);
                if (select.toLowerCase().contains("in_tree(") || select.toLowerCase().contains("in_tree ("))
                    encontrado = true;
            } else if (select.toLowerCase().indexOf("in_tree (") == -1) {
                int punto = select.toLowerCase().indexOf("in_tree(");
                String inTree = select.substring(punto, punto + 8);
                select = getInTree(select, inTree);
                if (select.toLowerCase().contains("in_tree(") || select.toLowerCase().contains("in_tree ("))
                    encontrado = true;
            } else if ((select.toLowerCase().indexOf("in_tree(") < select.toLowerCase().indexOf("in_tree ("))
                    || select.toLowerCase().indexOf("in_tree (") < 0) {
                int punto = select.toLowerCase().indexOf("in_tree(");
                String inTree = select.substring(punto, punto + 8);
                select = getInTree(select, inTree);
                if (select.toLowerCase().contains("in_tree(") || select.toLowerCase().contains("in_tree ("))
                    encontrado = true;
            } else if ((select.toLowerCase().indexOf("in_tree(") > select.toLowerCase().indexOf("in_tree ("))
                    || select.toLowerCase().indexOf("in_tree(") < 0) {
                int punto = select.toLowerCase().indexOf("in_tree (");
                String inTree = select.substring(punto, punto + 9);
                select = getInTree(select, inTree);
                if (select.toLowerCase().contains("in_tree(") || select.toLowerCase().contains("in_tree ("))
                    encontrado = true;
            }
            if (encontrado) {
                select = adaptarInTree(select);
            }
        }
        return select;
    }

    private static String getInTree(String select, String inTree) {

        String first = select.substring(0, select.indexOf(inTree));
        String end = select.substring(first.length() + ("in_Tree".length()), select.length());
        String valor = end.substring(0, end.indexOf("\')") + 2);
        end = end.substring(valor.length(), end.length());
        valor = valor.replace("(", "");
        valor = valor.replace(")", "");
        valor = valor.replace("\"", "");
        if (valor.contains(",")) {
            valor = (valor.split(","))[1].trim();
        }

        select = first + "Function_InTree=" + valor + end;

        select.toString();

        return select;
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
        String valor = end.substring(0, end.indexOf("\')") + 2);
        end = end.substring(valor.length(), end.length());
        valor = valor.replace("('", "");
        valor = valor.replace("')", "");
        valor = valor.replace("\"", "");
        if (valor.contains(",")) {
            valor = (valor.split(","))[1].trim().replace("'", "");
        }

        select = first + "function_InFolder=" + valor + end;

        select.toString();

        return select;
    }

    private static String adaptarContains(String select) {
        boolean encontrado = false;
        if (select.toLowerCase().contains("contains(") || select.toLowerCase().contains("contains (")) {
            if (select.toLowerCase().indexOf("contains(") == -1) {
                int punto = select.toLowerCase().indexOf("contains (");
                String contains = select.substring(punto, punto + 10);
                select = getContains(select, contains);
                if (select.toLowerCase().contains("contains(") || select.toLowerCase().contains("contains ("))
                    encontrado = true;
            } else if (select.toLowerCase().indexOf("contains (") == -1) {
                int punto = select.toLowerCase().indexOf("contains(");
                String contains = select.substring(punto, punto + 9);
                select = getContains(select, contains);
                if (select.toLowerCase().contains("contains(") || select.toLowerCase().contains("contains ("))
                    encontrado = true;
            } else if ((select.toLowerCase().indexOf("contains(") < select.toLowerCase().indexOf("contains ("))) {
                int punto = select.toLowerCase().indexOf("contains(");
                String contains = select.substring(punto, punto + 9);
                select = getContains(select, contains);
                if (select.toLowerCase().contains("contains(") || select.toLowerCase().contains("contains ("))
                    encontrado = true;
            } else if ((select.toLowerCase().indexOf("contains(") > select.toLowerCase().indexOf("contains ("))) {
                int punto = select.toLowerCase().indexOf("contains (");
                String contains = select.substring(punto, punto + 10);
                select = getContains(select, contains);
                if (select.toLowerCase().contains("contains(") || select.toLowerCase().contains("contains ("))
                    encontrado = true;
            }
            if (encontrado) {
                select = adaptarContains(select);
            }
        }
        return select;
    }

    private static String getContains(String statement, String contains) {
        String salida = "";
        String first = statement.substring(0, statement.indexOf(contains));
        String end = statement.substring(statement.indexOf(contains) + contains.length() - 1, statement.length());
        String medio = end.substring(0, end.indexOf("\')") + 2);
        salida += first + "Function_Contains=";
        salida += medio.substring(1, medio.length() - 1) + " ";
        salida += end.substring(medio.length(), end.length());
        return salida;
    }

}
