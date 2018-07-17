package org.apache.chemistry.opencmis.prodoc;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import org.apache.chemistry.opencmis.commons.exceptions.CmisInvalidArgumentException;

import net.sf.jsqlparser.expression.BinaryExpression;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.Parenthesis;
import net.sf.jsqlparser.expression.operators.conditional.AndExpression;
import net.sf.jsqlparser.expression.operators.conditional.OrExpression;
import net.sf.jsqlparser.expression.operators.relational.ExpressionList;
import net.sf.jsqlparser.expression.operators.relational.InExpression;
import net.sf.jsqlparser.statement.select.PlainSelect;
import prodoc.Attribute;
import prodoc.Condition;
import prodoc.Conditions;
import prodoc.Cursor;
import prodoc.DriverGeneric;
import prodoc.ObjPD;
import prodoc.PDDocs;
import prodoc.PDException;
import prodoc.PDFolders;
import prodoc.PDObjDefs;
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
            put("cINList", 6);
            put("cINQuery", 7);
            put("like", 8);
        }
    };

    static final List<String> camposCalculados = Arrays.asList("baseTypeId", "changetoken", "isLatestVersion",
            "isLatestMajorVersion", "IsPrivateWorkingCopy", "isVersionSeriesCheckedOut", "versionSeriesCheckedOutId",
            "contentStreamLength", "contentStreamId", "path", "allowedChildObjectTypeIds", "sourceId", "targetId",
            "policyText");

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
    public static List<String> busquedaFolder(String fullText, String inTree, DriverGeneric sesion, String docType,
            PlainSelect selectStatement) throws PDException {

        PDFolders objFolder = null;
        Cursor cur = new Cursor();
        List<String> result = new ArrayList<String>();

        try {

            objFolder = new PDFolders(sesion);

            List camposSelect = selectStatement.getSelectItems();

            String pTable = docType;

            Expression where = selectStatement.getWhere();

            Conditions condProdoc = new Conditions();

            if (where != null) {
                condProdoc = getConditions(where, null, sesion, true, docType);
            }

            List order = selectStatement.getOrderByElements();
            Vector pOrder = new Vector();
            if (order != null) {

                Iterator it = order.iterator();
                while (it.hasNext()) {

                    Object objIt = it.next();
                    String nombre = objIt.toString();

                    pOrder.add(nombre);
                }
            }

            String IdActFold = "RootFolder";
            if ((inTree != null) && !(inTree.isEmpty())) {
                IdActFold = inTree;
            }

            if (inTree == null || inTree.equals("")) {
                cur = objFolder.Search(docType, condProdoc, false, false, IdActFold, pOrder);
            } else { // Si tiene CONTAINS

                if (condProdoc == null) {
                    Conditions condsDoc = new Conditions();

                    cur = objFolder.Search(docType, condsDoc, false, true, IdActFold, pOrder);
                } else {
                    cur = objFolder.Search(docType, condProdoc, false, true, IdActFold, pOrder);
                }

            }

            Record record = objFolder.getDrv().NextRec(cur);

            result = obtenerSelectResult(cur, record, objFolder, true, camposSelect, where);

        } catch (PDException e) {
            throw e;
        } finally { // Cerramos el cursor
            objFolder.getDrv().CloseCursor(cur);
        }

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
    public static List<String> busquedaDoc(String fullText, String inTree, DriverGeneric sesion, String docType,
            PlainSelect selectStatement) throws PDException {

        PDDocs doc = null;
        Cursor cur = new Cursor();
        List<String> result = new ArrayList<String>();

        try {

            doc = new PDDocs(sesion);
            doc.setDocType(docType);

            List camposSelect = selectStatement.getSelectItems();

            String pTable = docType;

            Expression where = selectStatement.getWhere();
            Conditions condProdoc = new Conditions();

            if (where != null) {
                condProdoc = getConditions(where, null, sesion, false, docType);
            }

            List order = selectStatement.getOrderByElements();
            Vector pOrder = new Vector();
            if (order != null) {

                Iterator it = order.iterator();
                while (it.hasNext()) {

                    Object objIt = it.next();
                    String nombre = objIt.toString();

                    pOrder.add(nombre);
                }
            }

            String IdActFold = "RootFolder";
            if ((inTree != null) && !(inTree.isEmpty())) {
                IdActFold = inTree;
            }

            if (fullText == null || fullText.equals("") && inTree == null || inTree.equals("")) {
                cur = doc.Search(docType, condProdoc, false, false, false, IdActFold, pOrder);
            } else { // Si tiene CONTAINS

                if (condProdoc == null) {
                    Conditions condsDoc = new Conditions();

                    cur = doc.Search(fullText, docType, condsDoc, false, true, false, IdActFold, pOrder);
                } else {
                    cur = doc.Search(fullText, docType, condProdoc, false, true, false, IdActFold, pOrder);
                }

            }

            Record record = doc.getDrv().NextRec(cur);

            result = obtenerSelectResult(cur, record, doc, false, camposSelect, where);

        } catch (PDException e) {
            throw e;
        } finally { // Cerramos el cursor
            doc.getDrv().CloseCursor(cur);
        }

        return result;
    }

    /**
     * Obtiene el resultado de la busqueda
     * 
     * @param cur2
     * @param record
     * @param obj
     * @param isFolder
     * @param camposSelect
     * @return
     * @throws PDException
     */
    public static List<String> obtenerSelectResult(Cursor cur, Record record, ObjPD obj, boolean isFolder,
            List camposSelect, Expression where) throws PDException {

        List<String> result = new ArrayList<String>();

        if (isFolder) {
            obj = (PDFolders) obj;
        } else {
            obj = (PDDocs) obj;
        }

        while (record != null) {

            StringBuilder recordString = null;

            Iterator it = camposSelect.iterator();

            while (it.hasNext()) {

                Object objIt = it.next();
                String nombre = objIt.toString();

                record.initList();
                Attribute attr = record.nextAttr();

                boolean enc = false;

                switch (nombre) {

                // Tipo cmis:baseTypeId
                case "baseTypeId":
                    PDObjDefs D = new PDObjDefs(obj.getDrv());
                    Record rec = D.Load((String) record.getAttr("DocType").getValue());
                    recordString.append((String) rec.getAttr("Parent").getValue()).append("&&");

                    enc = true;
                    break;

                // Tipo cmis:isImmutable
                case "isImmutable":
                    recordString.append("true").append("&&");
                    enc = true;
                    break;

                // cmis:isLatestVersion
                case "isLatestVersion":
                    String strWhere = where.toString();
                    if (strWhere.contains("Version")) {
                        recordString.append("false").append("&&");
                    } else {
                        recordString.append("true").append("&&");
                    }
                    enc = true;
                    break;

                // cmis:isLatestMajorVersion
                case "isLatestMajorVersion":
                    String strWhere1 = where.toString();
                    if (strWhere1.contains("Version")) {
                        recordString.append("false").append("&&");
                    } else {
                        recordString.append("true").append("&&");
                    }
                    enc = true;
                    break;

                // Tipo cmis:isMajorVersion
                case "isMajorVersion":
                    recordString.append("true").append("&&");
                    enc = true;
                    break;

                // Tipo cmis:IsPrivateWorkingCopy
                case "IsPrivateWorkingCopy":
                    Attribute attrLockedBy = record.getAttr("LockedBy");
                    String usuSesion = obj.getDrv().getUser().getName();
                    if (attrLockedBy.getValue() != null && attrLockedBy.getValue().toString().equals(usuSesion)) {
                        recordString.append("true").append("&&");
                    } else {
                        recordString.append("false").append("&&");
                    }
                    enc = true;
                    break;

                // Tipo cmis:isVersionSeriesCheckedOut
                case "isVersionSeriesCheckedOut":
                    Attribute attrAux = record.getAttr("LockedBy");
                    if (attrAux.getValue() != null) {
                        recordString.append("true").append("&&");
                    }
                    enc = true;
                    break;

                // Tipo cmis:versionSeriesCheckedOutId
                case "versionSeriesCheckedOutId":
                    Attribute attrLockedBy1 = record.getAttr("LockedBy");
                    String usuSesion1 = obj.getDrv().getUser().getName();
                    if (attrLockedBy1.getValue() != null && attrLockedBy1.getValue().toString().equals(usuSesion1)) {
                        recordString.append(usuSesion1).append("&&");
                    } else {
                        recordString.append("").append("&&");
                    }
                    enc = true;
                    break;

                // cmis:contentStreamId --> Siempre 0
                case "contentStreamId":
                    recordString.append("0").append("&&");
                    enc = true;
                    break;

                // Tipo cmis:path
                case "path":
                    if (isFolder) {
                        Attribute attrPDId = record.getAttr("PDId");
                        recordString.append(((PDFolders) obj).getPathId((String) attrPDId.getValue())).append("&&");
                    } else {
                        recordString.append("").append("&&");
                    }

                    enc = true;
                    break;

                // cmis:allowedChildObjectTypeIds --> "not set" siempre
                case "allowedChildObjectTypeIds":
                    recordString.append("not set").append("&&");
                    enc = true;
                    break;

                default:
                    while (attr != null && !enc) {

                        if (attr.getName().equals(nombre)) {

                            if (attr.getType() == 5) {
                                recordString.append(convertirDateToString((Date) attr.getValue())).append("&&");
                            } else {
                                recordString.append((String) attr.getValue()).append("&&");
                            }

                            enc = true;
                        }

                        attr = record.nextAttr();
                    }

                    break;
                }

            }

            result.add(recordString.toString());

            record = obj.getDrv().NextRec(cur);
        }

        mostrar(result, camposSelect);

        System.out.println("");

        return result;
    }

    /**
     * Obtiene las condiciones de búsqueda
     * 
     * @param where
     * @param padre
     * @param sesion
     * @param isFolder
     * @param docType
     * @return
     * @throws PDException
     */
    private static Conditions getConditions(Expression where, Conditions padre, DriverGeneric sesion, boolean isFolder,
            String docType) throws PDException {

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

        } else if (where instanceof Parenthesis) {

            Conditions parenthCond = new Conditions();
            Parenthesis parEx = (Parenthesis) where;
            boolean or = parEx.isNot();
            Expression expre = parEx.getExpression();
            parenthCond = getConditions(parEx.getExpression(), parenthCond, sesion, isFolder, docType);
            if (padre == null) {
                padre = parenthCond;
            } else {
                padre.addCondition(parenthCond);
            }

        } else if (where instanceof InExpression) {

            InExpression inExpre = (InExpression) where;
            String campo = inExpre.getLeftExpression().toString();

            Condition prodocCond = null;
            HashSet<Object> hashSet = new HashSet<>();

            if (inExpre.getItemsList() instanceof ExpressionList) {
                List expressList = ((ExpressionList) inExpre.getItemsList()).getExpressions();
                if (expressList != null) {

                    Iterator it = expressList.iterator();
                    while (it.hasNext()) {

                        Object objIt = it.next();
                        String nombre = objIt.toString();

                        hashSet.add(nombre);
                    }
                }

                try {
                    prodocCond = new Condition(campo, hashSet);
                } catch (PDException e) {
                    e.printStackTrace();
                    throw e;
                }

                if (padre == null) {
                    padre = new Conditions();
                }

                padre.addCondition(prodocCond);

            }
            // // Si hay una SubSelect
            // else if (inExpre.getItemsList() instanceof SubSelect) {
            //
            // PlainSelect selectStatement = (PlainSelect) ((SubSelect)
            // inExpre.getItemsList()).getSelectBody();
            // Expression parEx = selectStatement.getWhere();
            // Conditions conds = null;
            // String pTable = docType;
            // Record pFields = getRecordStruct(sesion, isFolder, docType);
            // String campoSubSelect = inExpre.getLeftExpression().toString();
            // Conditions pWhere = getConditions(parEx, conds, sesion, isFolder, docType);
            // Query subSelect = new Query(pTable, pFields, pWhere);
            //
            // try {
            // prodocCond = new Condition(campoSubSelect, subSelect);
            // } catch (PDException e) {
            // e.printStackTrace();
            // }
            //
            // if (padre == null) {
            // padre = new Conditions();
            // }
            //
            // padre.addCondition(prodocCond);
            //
            // }

        } else {
            BinaryExpression be = (BinaryExpression) where;
            String campo = be.getLeftExpression().toString();
            String oper = be.getStringExpression().toString();
            int valOper = valOperComp.get(oper.toLowerCase());
            String valor = be.getRightExpression().toString();

            if (!esCalculado(campo)) {

                if (!campo.equals("Function_Contains") && !campo.equals("Function_InTree")) {

                    if (campo.equals("function_InFolder")) {
                        campo = "ParentId";
                    }

                    Condition prodocCond = getCond(campo, valOper, valor, sesion, isFolder, docType);

                    if (padre == null) {
                        padre = new Conditions();
                    }
                    padre.addCondition(prodocCond);
                }
            } else { // Si es una condicion con un campo calculado
                throw new CmisInvalidArgumentException("The field " + campo + " is not valid.");
            }
        }

        return padre;
    }

    /**
     * Convierte un Date en String
     * 
     * @param fecha
     * @return
     */
    public static String convertirDateToString(Date fecha) {

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String fechaCadena = sdf.format(fecha);

        return fechaCadena;
    }

    /**
     * Para saber si un campo es calculado
     * 
     * @param campo
     * @return
     */
    public static Boolean esCalculado(String campo) {

        boolean esCalculado = false;

        if (camposCalculados.contains(campo)) {
            esCalculado = true;
        }

        return esCalculado;
    }

    /**
     * Solo para pruebas
     * 
     * @param listaSalida
     * @param camposSelect
     */
    private static void mostrar(List<String> listaSalida, List camposSelect) {

        Iterator it = camposSelect.iterator();
        String cabecera = "";

        System.out.println();

        while (it.hasNext()) {

            Object objIt = it.next();
            String nombre = objIt.toString();

            if (cabecera != "") {
                cabecera += "    ||";
            }
            cabecera += "   " + nombre;

        }

        System.out.println(cabecera);
        System.out.println("_____________________________________");
        for (String a : listaSalida) {
            String salida = "";
            String[] mostrar = a.split("&&");
            for (String x : mostrar) {
                if (salida != "") {
                    salida += " || ";
                }
                salida += x;
            }
            System.out.println(salida);
        }

    }

    // /**
    // *
    // * @param sesion
    // * @param query
    // * @param isFolder
    // * @param docType
    // * @return
    // */
    // private static Query makeQuery(DriverGeneric sesion, String query, boolean
    // isFolder, String docType) {
    //
    // String strSelect = null;
    // String strFrom = null;
    // String strWhere = null;
    // String strGroup = null;
    // String strOrder = null;
    //
    // // Comenzamos a trocear la query
    // // Obtenemos el trozo de cadena hasta encontrar la palabra "FROM"
    // boolean bFrom = query.contains("FROM");
    // if (bFrom) {
    // int posFrom = query.indexOf("FROM");
    // strSelect = query.substring(0, posFrom - 1);
    // System.out.println(">>>>> strSelect : -->" + strSelect + "<--");
    //
    // // Obtenemos el trozo de cadena hasta encontrar la palabra "WHERE", si existe
    // boolean bWhere = query.contains("WHERE");
    // if (bWhere) {
    // int posWhere = query.indexOf("WHERE");
    // // strFrom = query.substring(posFrom+5, posWhere-1);
    // strFrom = docType;
    // System.out.println(">>>>> strFrom : -->" + strFrom + "<--");
    //
    // boolean bGroup = query.contains("GROUP");
    // if (bGroup) {
    // int posGroup = query.indexOf("GROUP");
    // strWhere = query.substring(posWhere + 6, posGroup - 1);
    // System.out.println(">>>>> strWhere : -->" + strWhere + "<--");
    //
    // boolean bOrder = query.contains("ORDER");
    // if (bOrder) {
    // int posOrder = query.indexOf("ORDER");
    // strGroup = query.substring(posGroup + 9, posOrder - 1);
    // System.out.println(">>>>> strGroup : -->" + strGroup + "<--");
    //
    // strOrder = query.substring(posOrder + 9, query.length());
    // System.out.println(">>>>> strOrder : -->" + strOrder + "<--");
    // } else {
    // strGroup = query.substring(posGroup + 9, query.length());
    // System.out.println(">>>>> strGroup : -->" + strGroup + "<--");
    // }
    //
    // } else {
    // strWhere = query.substring(posWhere + 6, query.length());
    // System.out.println(">>>>> strSelect : -->" + strSelect + "<--");
    // }
    // } else {
    // // strFrom = query.substring(posFrom+5, query.length());
    // strFrom = docType;
    // System.out.println(">>>>> strFrom : -->" + strFrom);
    // }
    // }
    //
    // // Guardamos los datos obtenidos en los campos correspondientes
    // Vector pTables = obtenerVectorString(strFrom);
    // Record pFields = getRecordStruct(sesion, isFolder, docType);
    // Conditions pWhere = null;
    // if (strWhere != null) {
    // Conditions conds = new Conditions();
    // pWhere = getConds(sesion, strWhere, isFolder, docType, conds);
    // }
    // Vector pOrderList = null;
    // if (strOrder != null) {
    // pOrderList = obtenerVectorString(strOrder);
    // }
    //
    // // public Query(String pTable, Record pFields, Conditions pWhere)
    // // Query queryOPD = new Query(pTable, pFields, pWhere);
    //
    // // public Query(Vector pTables, Record pFields, Conditions pWhere, Vector
    // // pOrderList)
    // Query queryOPD = new Query(pTables, pFields, pWhere, pOrderList);
    //
    // return queryOPD;
    // }

    // /**
    // *
    // * @param strFrom
    // * @return
    // */
    // private static Vector obtenerVectorString(String strFrom) {
    //
    // String[] vecStrTables = strFrom.split(",");
    // Vector vectTables = new Vector();
    //
    // for (int i = 0; i < vecStrTables.length; i++) {
    // vectTables.add(vecStrTables[i]);
    // }
    //
    // return vectTables;
    // }

    /**
     * Metodo para obtener la estructura record dependiendo del tipo de objeto y el
     * docType
     *
     * @param MainSession
     * @param isFolder
     * @param docType
     * @return
     * @throws PDException
     */
    private static Record getRecordStruct(DriverGeneric MainSession, boolean isFolder, String docType)
            throws PDException {

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
            ex.printStackTrace();
            throw ex;
        }

        return pFields;
    }

    // /**
    // * Metodo que obtiene las condiciones de la query
    // *
    // * @param sesion
    // * @param strConditions
    // * @param isFolder
    // * @param docType
    // * @return
    // */
    // private static Conditions getConds(DriverGeneric sesion, String
    // strConditions, boolean isFolder, String docType,
    // Conditions conds) {
    //
    // Condition cond = null;
    // // Conditions conds = new Conditions();
    // // String strAux = strConditions;
    //
    // boolean bAnd = strConditions.contains("AND");
    //
    // String strAnd;
    // if (bAnd) {
    //
    // int posAnd = strConditions.indexOf("AND");
    // strAnd = strConditions.substring(0, posAnd).trim();
    // System.out.println(">>>>> strAnd : -->" + strAnd + "<--");
    //
    // // Separamos los campos de la condicion
    // String[] vCond = strAnd.split(" ");
    //
    // String strCampo = vCond[0].trim();
    // String strOper = vCond[1].trim();
    // int valOper = valOperComp.get(strOper);
    // String strValor = vCond[2].trim();
    //
    // cond = getCond(strCampo, valOper, strValor, sesion, isFolder, docType);
    // conds.addCondition(cond);
    //
    // // Llamada recursiva
    // String strAux = strConditions.substring(posAnd + 4,
    // strConditions.length()).trim();
    // conds = getConds(sesion, strAux, isFolder, docType, conds);
    //
    // } else { //
    //
    // // Separamos los campos de la condicion
    // String[] vCond = strConditions.split(" ");
    //
    // // String strCampo = vCond[0].trim();
    // String strCampo = QueryUtil.listaTradMetadatas.get(vCond[0].trim()) != null
    // ? QueryUtil.listaTradMetadatas.get(vCond[0].trim())
    // : vCond[0].trim();
    //
    // String strOper = vCond[1].trim();
    // int valOper = valOperComp.get(strOper);
    //
    // cond = getCond(strCampo, valOper, vCond[2].trim(), sesion, isFolder,
    // docType);
    // conds.addCondition(cond);
    //
    // }
    //
    // return conds;
    // }

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
     * @throws PDException
     */
    private static Condition getCond(String strCampo, Integer valOper, String strValorCampo, DriverGeneric sesion,
            boolean isFolder, String docType) throws PDException {

        Condition cond = null;
        int attrType = 0;

        try {
            if (isFolder) {

                PDFolders fol = new PDFolders(sesion);
                Attribute attr = fol.getRecordStructPDFolder().getAttr(strCampo);
                attrType = attr.getType();

            } else {

                PDDocs doc = new PDDocs(sesion);
                doc.setDocType(docType);
                Attribute attr = doc.getRecordStruct().getAttr(strCampo);
                attrType = attr.getType();
            }

            switch (attrType) {
            case 0:
                Integer sValor = Integer.parseInt(strValorCampo);
                cond = new Condition(strCampo, valOper, sValor);
                break;

            case 1:
                Float fValor = Float.parseFloat(strValorCampo);
                cond = new Condition(strCampo, valOper, fValor);
                break;

            case 2:
                cond = new Condition(strCampo, valOper, strValorCampo);
                break;

            case 3:
                SimpleDateFormat formato = new SimpleDateFormat("dd/MM/yyyy");
                Date dValor = formato.parse(strValorCampo);
                cond = new Condition(strCampo, valOper, dValor);
                break;

            case 4:
                Boolean bValor = Boolean.parseBoolean(strValorCampo);
                cond = new Condition(strCampo, valOper, bValor);
                break;

            case 5:
                String strAux = strValorCampo.substring(1, strValorCampo.length() - 1);
                String strPatron = "yyyy-MM-dd HH:mm:ss";
                Date date = convertStringToTimestamp(strAux, strPatron);
                cond = new Condition(strCampo, valOper, date);
                break;

            default:
                break;
            }

        } catch (PDException e) {
            throw e;
        } catch (ParseException e) {
            throw new PDException(e.getMessage());
        }

        return cond;
    }

    /**
     * 
     * @param str_date
     * @param pattern
     * @return
     * @throws PDException
     */
    public static Date convertStringToTimestamp(String str_date, String pattern) throws PDException {
        try {

            SimpleDateFormat formatter = new SimpleDateFormat(pattern);
            Date date = formatter.parse(str_date);
            return date;

        } catch (ParseException e) {
            throw new PDException(e.getMessage());
        }
    }

    /**
     * 
     * @param sesion
     * @param tipo
     * @param objectId
     * @return
     * @throws PDException
     */
    public static Record getRecordObjectOPD(DriverGeneric sesion, String tipo, String objectId) throws PDException {

        PDDocs objDoc = new PDDocs(sesion);
        PDFolders objFolder = new PDFolders(sesion);
        Record record = null;

        try {

            if (tipo.equalsIgnoreCase("document") || tipo.equalsIgnoreCase("PD_DOCS")) {
                
                record = objDoc.LoadFull(objectId);

            } else if (tipo.equalsIgnoreCase("folder") || tipo.equalsIgnoreCase("PD_FOLDERS")) {

                record = objFolder.LoadFull(objectId);

            } else { // TODO: Probar que funciona correctamente esta parte
                PDObjDefs od = new PDObjDefs(sesion);
                od.Load(tipo);
                String tipoObj = od.getClassType();
                
                if (tipoObj.equalsIgnoreCase("document")) {
                    objDoc = new PDDocs(sesion, tipo);
                    record = objDoc.LoadFull(objectId);
                } else {

                    objFolder = new PDFolders(sesion, tipo);
                    record = objFolder.LoadFull(objectId);
                }
            }

            return record;

        } catch (PDException e) {
            throw e;
        }
    }

}
