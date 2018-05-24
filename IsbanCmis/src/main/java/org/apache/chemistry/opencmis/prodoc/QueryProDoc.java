package org.apache.chemistry.opencmis.prodoc;

import java.util.HashMap;
import java.util.List;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;

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

    static final HashMap<String, Integer> valOperComp = new HashMap<String, Integer>() {{
        put("=", 0);
        put(">", 1);
        put("<", 2);
        put(">=", 3);
        put("<=", 4);
        put("<>", 5);
        //put("cINList", 6);
        //put("cINQuery", 7);        
        put("like", 8);
    }};
    
    
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
				
		
	      System.out.println(">>>>> INICIO busquedaFolder .... "); 
	        
	        PDFolders objFolder = null;
	        
	        try {
	            objFolder = new PDFolders(sesion);
	            
	            Query queryOPD = makeQuery(sesion, query, true, docType);
	             
	            Cursor Cur = objFolder.getDrv().OpenCursor(queryOPD);

	            Record r = objFolder.getDrv().NextRec(Cur);

	            while(r != null){

	                System.out.println("");
	                System.out.println("--- Datos de la carpeta ---");            

	                r.initList();
	                Attribute attr = r.nextAttr();
	                while(attr != null){
	                    System.out.println("- " + attr.getName() + " : " + attr.getValue());

	                    attr = r.nextAttr();
	                }   

	                r = objFolder.getDrv().NextRec(Cur);
	            }

	            objFolder.getDrv().CloseCursor(Cur);
	        
	        } catch (PDException ex) {
	            Logger.getLogger(QueryProDoc.class.getName()).log(Level.SEVERE, null, ex);
	        }   
	            
	        System.out.println(">>>>> FIN busquedaFolder .... "); 

	        
	        
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
	private static Query makeQuery(DriverGeneric sesion, String query, boolean isFolder, String docType) {
	    
	    String strSelect = null;
	    String strFrom = null;
	    String strWhere = null;
	    String strGroup = null;
	    String strOrder = null;
	    
	    // Comenzamos a trocear la query
	    // Obtenemos el trozo de cadena hasta encontrar la palabra "FROM"
	    boolean bFrom = query.contains("FROM");
	    if(bFrom){
	        int posFrom = query.indexOf("FROM");                
	        strSelect = query.substring(0,posFrom-1);        
	        System.out.println(">>>>> strSelect : -->" + strSelect + "<--");                

	       // Obtenemos el trozo de cadena hasta encontrar la palabra "WHERE", si existe
	       boolean bWhere = query.contains("WHERE");
	       if(bWhere){
	           int posWhere = query.indexOf("WHERE");           
	           //strFrom = query.substring(posFrom+5, posWhere-1);
	           strFrom = docType;
	           System.out.println(">>>>> strFrom : -->" + strFrom + "<--");
	            
	           boolean bGroup = query.contains("GROUP");         
	           if(bGroup){
	               int posGroup = query.indexOf("GROUP");
	               strWhere = query.substring(posWhere+6, posGroup-1);
	               System.out.println(">>>>> strWhere : -->" + strWhere + "<--");

	               boolean bOrder = query.contains("ORDER");         
	               if(bOrder){
	                   int posOrder = query.indexOf("ORDER");
	                   strGroup = query.substring(posGroup+9, posOrder-1);
	                   System.out.println(">>>>> strGroup : -->" + strGroup + "<--");

	                   strOrder = query.substring(posOrder+9, query.length());
	                   System.out.println(">>>>> strOrder : -->" + strOrder + "<--");
	               }else{                
	                   strGroup = query.substring(posGroup+9, query.length());
	                   System.out.println(">>>>> strGroup : -->" + strGroup + "<--");
	               } 

	           }else{
	               strWhere = query.substring(posWhere+6, query.length());
	               System.out.println(">>>>> strSelect : -->" + strSelect + "<--");
	           }        
	       }else{
	            strFrom = query.substring(posFrom+5, query.length());
	            System.out.println(">>>>> strFrom : -->" + strFrom);
	       }
	    }
	    
	    // Guardamos los datos obtenidos en los campos correspondientes
	    Vector pTables = obtenerVectorString(strFrom);             
	    Record pFields = getRecordStruct(sesion, isFolder, docType);
	    Conditions pWhere = null;
	    if(strWhere != null){
	        pWhere = getConds(sesion, strWhere, isFolder, docType);
	    }
	    Vector pOrderList = null;
	    if(strOrder != null){
	        pOrderList = obtenerVectorString(strOrder);
	    }
	    
	    
	    //public Query(String pTable, Record pFields, Conditions pWhere)
	    //Query queryOPD = new Query(pTable, pFields, pWhere);
	    
	    //public Query(Vector pTables, Record pFields, Conditions pWhere, Vector pOrderList)
	    Query queryOPD = new Query(pTables, pFields, pWhere, pOrderList);

	    return queryOPD;
	}


	private static Vector obtenerVectorString(String strFrom){
	    
	    String[] vecStrTables = strFrom.split(",");
	    Vector vectTables = new Vector();
	    
	    for (int i = 0; i < vecStrTables.length; i++) {        
	        vectTables.add(vecStrTables[i]);
	    }
	    
	    return vectTables;
	}


	private static Record getRecordStruct(DriverGeneric MainSession, boolean isFolder, String docType){
	    
	    Record pFields = null;
	    try {
	        if(isFolder){
	            PDFolders folder = new PDFolders(MainSession);
	            pFields = folder.getRecSum();
	        }else{
	            PDDocs doc = new PDDocs(MainSession);
	            doc.setDocType(docType);
	            pFields = doc.getRecSum();
	        }
	    } catch (PDException ex) {
	        Logger.getLogger(QueryProDoc.class.getName()).log(Level.SEVERE, null, ex);
	    }
	    
	    return pFields;
	}


	private static Conditions getConds(DriverGeneric sesion, String strConditions, boolean isFolder, String docType){

	    Condition cond;
	    Conditions conds = new Conditions();    
	    //String strAux = strConditions;
	    
	    boolean bAnd = strConditions.contains("AND");
	/*    
	    while(bAnd){
	        
	        bAnd = strAux.contains("AND");
	    }
	*/    
	    String strAnd;
	    if(bAnd){
	        int posAnd = strConditions.indexOf("AND");
	        strAnd = strConditions.substring(0, posAnd);
	        System.out.println(">>>>> strAnd : -->" + strAnd + "<--");        
	/*        
	        strOrder = query.substring(posOrder+9, query.length());
	        System.out.println(">>>>> strOrder : -->" + strOrder + "<--");
	*/        
	    }else{                        	       

	        // Separamos los campos de la condicion
	        String[] vCond = strConditions.split(" ");
	        
	        String strCampo = vCond[0].trim();
	        String strOper = vCond[1].trim();
	        int valOper = valOperComp.get(strOper);
	        
	// Hay que crear strValor segun el tipo de datos que sea (String, Integer, Boolean o Date)
	// Habra que comprobar si vienen comillas (String) o no --> Si no vienen comprobar si es "true" o "false"
	        
	        if(isFolder) {
	            try {
                    PDFolders fol = new PDFolders(sesion);
                } catch (PDException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
	        }else {
	            
                try {
                    PDDocs doc = new PDDocs(sesion);
                    doc.setDocType(docType);
                } catch (PDException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }	            
	        }
	        
	        String strValor = vCond[2].trim();
	        for (int i = 3; i < vCond.length; i++) {
	            strValor = strValor.concat(" ");
	            strValor = strValor.concat(vCond[i]);
	        }
	       
	        try {
	            cond = new Condition(strCampo, valOper, strValor);
	            conds.addCondition(cond);
	        } catch (PDException ex) {
	            Logger.getLogger(QueryProDoc.class.getName()).log(Level.SEVERE, null, ex);
	        }
	        System.out.println(">>>>> strCampo : -->" + strCampo + "<--");
	        System.out.println(">>>>> strValor : -->" + strValor + "<--");
	    } 
	    
	    return conds;
	}
	
}
