package org.apache.chemistry.opencmis.prodoc;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.chemistry.opencmis.isbanutil.QueryUtil;

import net.sf.jsqlparser.expression.BinaryExpression;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.operators.conditional.AndExpression;
import net.sf.jsqlparser.expression.operators.conditional.OrExpression;
import net.sf.jsqlparser.statement.select.FromItem;
import net.sf.jsqlparser.statement.select.PlainSelect;
import prodoc.Attribute;
import prodoc.Condition;
import prodoc.Conditions;
import prodoc.Cursor;
import prodoc.DriverGeneric;
import prodoc.PDDocs;
import prodoc.PDException;
import prodoc.PDFolders;
import prodoc.Query;
import prodoc.Record;

/**
 * 
 * @author Viewnext:Sergio Rodriguez Oyola
 *
 */
public class QueryProDoc {

    static final HashMap<String, Integer> valOperComp = new HashMap<String, Integer>() {
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

    public QueryProDoc() {

    }

    /**
     * 
     * @param query
     * @param sesion
     * @param camposSelect
     * @return
     * @throws PDException
     */
    public static List<String> busquedaFolder(String statement, DriverGeneric sesion, String docType,
            PlainSelect selectStatement) throws PDException {

        List<String> result = new ArrayList<String>();
        PDFolders objFolder = new PDFolders(sesion);

        List camposSelect = selectStatement.getSelectItems();
        FromItem table = selectStatement.getFromItem();
        BinaryExpression where = (BinaryExpression) selectStatement.getWhere();
        List order = selectStatement.getOrderByElements();

        String pTable;
        // if (table.toString().equals("document")) {
        pTable = docType;
        // } else {
        // pTable = table.toString();
        // }

        Record pFields = objFolder.getRecordStructPDFolder();

        Conditions pWhere = new Conditions();
        if (where != null) {

            String campo = where.getLeftExpression().toString();
            String oper = where.getStringExpression().toString();
            int valOper = valOperComp.get(oper);
            String valor = where.getRightExpression().toString();

            Condition cond = getCond(campo, valOper, valor, sesion, true, docType);
            pWhere.addCondition(cond);
        }

        String pOrder = null;
        if (order != null) {

            Iterator it = order.iterator();
            while (it.hasNext()) {

                Object objIt = it.next();
                String nombre = objIt.toString();

                pOrder = nombre;
            }
        }

        Query queryOPD = new Query(pTable, pFields, pWhere, pOrder);

        Cursor cur = objFolder.getDrv().OpenCursor(queryOPD);

        Record record = objFolder.getDrv().NextRec(cur);

        while (record != null) {
            record.initList();
            String recordString = "";
            Attribute attr = record.nextAttr();

            while (attr != null) {

                System.out.println("- Atributo " + attr.getName());

                Iterator it = camposSelect.iterator();

                while (it.hasNext()) {

                    Object objIt = it.next();
                    String nombre = objIt.toString();
                    String nameAttr = attr.getName();
                    if (nameAttr.equals(nombre)) {

                        recordString = recordString.concat((String) attr.getValue()).concat("&&");
                    }
                }

                attr = record.nextAttr();
            }

            result.add(recordString);
            record = objFolder.getDrv().NextRec(cur);
        }

        objFolder.getDrv().CloseCursor(cur);

        return result;
    }

    /**
     * 
     * @param statement
     * @param sesion
     * @param docType
     * @param selectStatement
     * @return
     * @throws PDException
     */
    public static List<String> busquedaDoc(String statement, DriverGeneric sesion, String docType,
            PlainSelect selectStatement) throws PDException {

        List<String> result = new ArrayList<String>();

        PDDocs doc = new PDDocs(sesion);
        doc.setDocType(docType);

        List camposSelect = selectStatement.getSelectItems();
        FromItem table = selectStatement.getFromItem();
        List order = selectStatement.getOrderByElements();

        String pTable;
        pTable = docType;

        Record pFields = doc.getRecordStruct();

        Conditions condProdoc = getConditions(selectStatement.getWhere(), null, sesion, false, docType);

        String pOrder = null;
        if (order != null) {

            Iterator it = order.iterator();
            while (it.hasNext()) {

                Object objIt = it.next();
                String nombre = objIt.toString();

                pOrder = nombre;
            }
        }

        Query queryOPD = new Query(pTable, pFields, condProdoc, pOrder);

        Cursor Cur = doc.getDrv().OpenCursor(queryOPD);

        Record record = doc.getDrv().NextRec(Cur);

        while (record != null) {
            record.initList();
            String recordString = "";
            Attribute attr = record.nextAttr();

            while (attr != null) {

                Iterator it = camposSelect.iterator();

                while (it.hasNext()) {

                    Object objIt = it.next();
                    String nombre = objIt.toString();
                    String nameAttr = attr.getName();
                    if (nameAttr.equals(nombre)) {

                        recordString = recordString.concat((String) attr.getValue()).concat("&&");
                    }
                }

                attr = record.nextAttr();
            }

            System.out.println(">>>>>   " + recordString);

            result.add(recordString);
            record = doc.getDrv().NextRec(Cur);
        }

        doc.getDrv().CloseCursor(Cur);

        return result;
    }

    /**
     * 
     * @param where
     * @param padre
     * @param sesion
     * @param isFolder
     * @param docType
     * @return
     */
    private static Conditions getConditions(Expression where, Conditions padre, DriverGeneric sesion, boolean isFolder,
            String docType) {
        if (where instanceof AndExpression) {
            Conditions and = new Conditions();
            AndExpression andEx = (AndExpression) where;
            and = getConditions(andEx.getLeftExpression(), and, sesion, isFolder, docType);
            and = getConditions(andEx.getRightExpression(), and, sesion, isFolder, docType);
            if (padre == null) {
                padre = and;
            } else {
                padre.addCondition(and);
            }
        } else if (where instanceof OrExpression) {
            Conditions or = new Conditions();
            OrExpression orEx = (OrExpression) where;
            or.setOperatorAnd(false);
            or = getConditions(orEx.getLeftExpression(), or, sesion, isFolder, docType);
            or = getConditions(orEx.getRightExpression(), or, sesion, isFolder, docType);
            if (padre == null) {
                padre = or;
            } else {
                padre.addCondition(or);
            }
        } else {
            BinaryExpression be = (BinaryExpression) where;
            String campo = be.getLeftExpression().toString();
            String oper = be.getStringExpression().toString();
            int valOper = valOperComp.get(oper);
            String valor = be.getRightExpression().toString();

            Condition prodocCond = getCond(campo, valOper, valor, sesion, isFolder, docType);
            if (padre == null) {
                padre = new Conditions();
            }
            padre.addCondition(prodocCond);

        }

        return padre;
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
    private static Query makeQuery(DriverGeneric sesion, String query, boolean isFolder, String docType) {

        String strSelect = null;
        String strFrom = null;
        String strWhere = null;
        String strGroup = null;
        String strOrder = null;

        // Comenzamos a trocear la query
        // Obtenemos el trozo de cadena hasta encontrar la palabra "FROM"
        boolean bFrom = query.contains("FROM");
        if (bFrom) {
            int posFrom = query.indexOf("FROM");
            strSelect = query.substring(0, posFrom - 1);
            System.out.println(">>>>> strSelect : -->" + strSelect + "<--");

            // Obtenemos el trozo de cadena hasta encontrar la palabra "WHERE", si existe
            boolean bWhere = query.contains("WHERE");
            if (bWhere) {
                int posWhere = query.indexOf("WHERE");
                // strFrom = query.substring(posFrom+5, posWhere-1);
                strFrom = docType;
                System.out.println(">>>>> strFrom : -->" + strFrom + "<--");

                boolean bGroup = query.contains("GROUP");
                if (bGroup) {
                    int posGroup = query.indexOf("GROUP");
                    strWhere = query.substring(posWhere + 6, posGroup - 1);
                    System.out.println(">>>>> strWhere : -->" + strWhere + "<--");

                    boolean bOrder = query.contains("ORDER");
                    if (bOrder) {
                        int posOrder = query.indexOf("ORDER");
                        strGroup = query.substring(posGroup + 9, posOrder - 1);
                        System.out.println(">>>>> strGroup : -->" + strGroup + "<--");

                        strOrder = query.substring(posOrder + 9, query.length());
                        System.out.println(">>>>> strOrder : -->" + strOrder + "<--");
                    } else {
                        strGroup = query.substring(posGroup + 9, query.length());
                        System.out.println(">>>>> strGroup : -->" + strGroup + "<--");
                    }

                } else {
                    strWhere = query.substring(posWhere + 6, query.length());
                    System.out.println(">>>>> strSelect : -->" + strSelect + "<--");
                }
            } else {
                // strFrom = query.substring(posFrom+5, query.length());
                strFrom = docType;
                System.out.println(">>>>> strFrom : -->" + strFrom);
            }
        }

        // Guardamos los datos obtenidos en los campos correspondientes
        Vector pTables = obtenerVectorString(strFrom);
        Record pFields = getRecordStruct(sesion, isFolder, docType);
        Conditions pWhere = null;
        if (strWhere != null) {
            Conditions conds = new Conditions();
            pWhere = getConds(sesion, strWhere, isFolder, docType, conds);
        }
        Vector pOrderList = null;
        if (strOrder != null) {
            pOrderList = obtenerVectorString(strOrder);
        }

        // public Query(String pTable, Record pFields, Conditions pWhere)
        // Query queryOPD = new Query(pTable, pFields, pWhere);

        // public Query(Vector pTables, Record pFields, Conditions pWhere, Vector
        // pOrderList)
        Query queryOPD = new Query(pTables, pFields, pWhere, pOrderList);

        return queryOPD;
    }

    /**
     * 
     * @param strFrom
     * @return
     */
    private static Vector obtenerVectorString(String strFrom) {

        String[] vecStrTables = strFrom.split(",");
        Vector vectTables = new Vector();

        for (int i = 0; i < vecStrTables.length; i++) {
            vectTables.add(vecStrTables[i]);
        }

        return vectTables;
    }

    /**
     * Metodo para obtener la estructura record dependiendo del tipo de objeto y el
     * dovType
     * 
     * @param MainSession
     * @param isFolder
     * @param docType
     * @return
     */
    private static Record getRecordStruct(DriverGeneric MainSession, boolean isFolder, String docType) {

        Record pFields = null;
        try {
            if (isFolder) {
                PDFolders folder = new PDFolders(MainSession);
                pFields = folder.getRecSum();
            } else {
                PDDocs doc = new PDDocs(MainSession);
                doc.setDocType(docType);
                pFields = doc.getRecSum();
            }
        } catch (PDException ex) {
            Logger.getLogger(QueryProDoc.class.getName()).log(Level.SEVERE, null, ex);
        }

        return pFields;
    }

    /**
     * Metodo que obtiene las condiciones de la query
     * 
     * @param sesion
     * @param strConditions
     * @param isFolder
     * @param docType
     * @return
     */
    private static Conditions getConds(DriverGeneric sesion, String strConditions, boolean isFolder, String docType,
            Conditions conds) {

        Condition cond = null;
        // Conditions conds = new Conditions();
        // String strAux = strConditions;

        boolean bAnd = strConditions.contains("AND");

        String strAnd;
        if (bAnd) {

            int posAnd = strConditions.indexOf("AND");
            strAnd = strConditions.substring(0, posAnd).trim();
            System.out.println(">>>>> strAnd : -->" + strAnd + "<--");

            // Separamos los campos de la condicion
            String[] vCond = strAnd.split(" ");

            String strCampo = vCond[0].trim();
            String strOper = vCond[1].trim();
            int valOper = valOperComp.get(strOper);
            String strValor = vCond[2].trim();

            cond = getCond(strCampo, valOper, strValor, sesion, isFolder, docType);
            conds.addCondition(cond);

            // Llamada recursiva
            String strAux = strConditions.substring(posAnd + 4, strConditions.length()).trim();
            conds = getConds(sesion, strAux, isFolder, docType, conds);

        } else { //

            // Separamos los campos de la condicion
            String[] vCond = strConditions.split(" ");

            // String strCampo = vCond[0].trim();
            String strCampo = QueryUtil.listaTradMetadatas.get(vCond[0].trim()) != null
                    ? QueryUtil.listaTradMetadatas.get(vCond[0].trim())
                    : vCond[0].trim();

            String strOper = vCond[1].trim();
            int valOper = valOperComp.get(strOper);

            cond = getCond(strCampo, valOper, vCond[2].trim(), sesion, isFolder, docType);
            conds.addCondition(cond);

        }

        return conds;
    }

    /**
     * Metodo que devuelve una condicion
     * 
     * @param strCampo
     * @param valOper
     * @param strValorCampo
     * @param sesion
     * @param isFolder
     * @param docType
     * @return
     */
    private static Condition getCond(String strCampo, Integer valOper, String strValorCampo, DriverGeneric sesion,
            boolean isFolder, String docType) {

        Condition cond = null;
        int attrType = 0;

        if (isFolder) {
            try {
                PDFolders fol = new PDFolders(sesion);
                Attribute attr = fol.getRecordStructPDFolder().getAttr(strCampo);
                attrType = attr.getType();
            } catch (PDException e) {
                e.printStackTrace();
            }
        } else {

            try {
                PDDocs doc = new PDDocs(sesion);
                doc.setDocType(docType);
                Attribute attr = doc.getRecordStruct().getAttr(strCampo);
                attrType = attr.getType();
            } catch (PDException e) {
                e.printStackTrace();
            }
        }

        switch (attrType) {
        case 0:
            Integer sValor = Integer.parseInt(strValorCampo);
            try {
                cond = new Condition(strCampo, valOper, sValor);
            } catch (PDException e) {
                e.printStackTrace();
            }
            break;
        case 1:
            Float fValor = Float.parseFloat(strValorCampo);
            try {
                cond = new Condition(strCampo, valOper, fValor);
            } catch (PDException e) {
                e.printStackTrace();
            }
            break;
        case 2:
            // String strValor = strValorCampo.substring(1, strValorCampo.length() - 1); //
            // Eliminamos las comillas

            try {
                cond = new Condition(strCampo, valOper, strValorCampo);
            } catch (PDException e) {
                e.printStackTrace();
            }
            break;
        case 3:
            SimpleDateFormat formato = new SimpleDateFormat("dd/MM/yyyy");
            try {
                Date dValor = formato.parse(strValorCampo);
                cond = new Condition(strCampo, valOper, dValor);
            } catch (PDException e) {
                e.printStackTrace();
            } catch (ParseException e) {
                e.printStackTrace();
            }
            break;
        case 4:
            Boolean bValor = Boolean.parseBoolean(strValorCampo);
            try {
                cond = new Condition(strCampo, valOper, bValor);
            } catch (PDException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            break;
        case 5:
            SimpleDateFormat formato2 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            String strValor = strValorCampo.substring(1, strValorCampo.length() - 1); // Eliminamos las comillas
            try {
                Date dValor = formato2.parse(strValor);
                cond = new Condition(strCampo, valOper, strValorCampo);
            } catch (PDException e) {
                e.printStackTrace();
            } catch (ParseException e) {
                e.printStackTrace();
            }
            break;
        default:
            break;
        }

        return cond;
    }
}
