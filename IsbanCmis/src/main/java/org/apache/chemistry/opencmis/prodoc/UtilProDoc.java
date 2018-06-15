package org.apache.chemistry.opencmis.prodoc;

import java.io.File;
import java.util.Date;
import java.util.Iterator;

import org.apache.chemistry.opencmis.commons.data.Properties;
import org.apache.chemistry.opencmis.commons.exceptions.CmisObjectNotFoundException;

import prodoc.Attribute;
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
public class UtilProDoc {

    public UtilProDoc() {
        super();
    }

//    public static Properties getProperties(File file, String objectId, SesionProDoc sesion) {
//
//        Record recObjOPD = null;
//        ObjPD objOPD = null;
//        
//        if (file == null) {
//            throw new IllegalArgumentException("File must not be null!");
//        }
//
//        if (!file.exists()) {
//            throw new CmisObjectNotFoundException("Object not found!");
//        }
//        
//        // Obtenemos el record del objeto
//        try {
//            if (file.isDirectory()) {
//                objOPD = (PDFolders) objOPD;
//                PDFolders folder = new PDFolders(sesion.getMainSession(), objectId);
//                recObjOPD = folder.getRecord();
//            } else {
//                objOPD = (PDDocs) objOPD;
//                PDDocs doc = new PDDocs(sesion.getMainSession(), objectId);
//                recObjOPD = doc.getRecord();
//            }
//        } catch (PDException e) {
//            e.printStackTrace();
//        }               
//        
//        while (recObjOPD != null) {                
//
//                recObjOPD.initList();
//                Attribute attr = recObjOPD.nextAttr();
//
//                String nombreCampo = attr.getName();
//                String valorCampo = attr.getValue().toString();
//                
//                boolean enc = false;
//
//                switch (nombreCampo) {
//
//                // Tipo cmis:baseTypeId
//                case "baseTypeId":
//                    PDObjDefs D = new PDObjDefs(objOPD.getDrv());
//
//                    Record rec = D.Load((String) recObjOPD.getAttr("DocType").getValue());
//                    strMostrar = (String) rec.getAttr("Parent").getValue();
//
//                    recordString = recordString.concat(strMostrar).concat("&&");
//                    enc = true;
//                    break;
//
//                // Tipo cmis:isImmutable
//                case "isImmutable":
//                    recordString = recordString.concat("true").concat("&&");
//                    enc = true;
//                    break;
//
//                // cmis:isLatestVersion
//                case "isLatestVersion":
//                    String strWhere = where.toString();
//                    if (strWhere.contains("Version")) {
//                        recordString = recordString.concat("false").concat("&&");
//                    } else {
//                        recordString = recordString.concat("true").concat("&&");
//                    }
//                    enc = true;
//                    break;
//
//                // cmis:isLatestMajorVersion
//                case "isLatestMajorVersion":
//                    String strWhere1 = where.toString();
//                    if (strWhere1.contains("Version")) {
//                        recordString = recordString.concat("false").concat("&&");
//                    } else {
//                        recordString = recordString.concat("true").concat("&&");
//                    }
//                    enc = true;
//                    break;
//
//                // Tipo cmis:isMajorVersion
//                case "isMajorVersion":
//                    recordString = recordString.concat("true").concat("&&");
//                    enc = true;
//                    break;
//
//                // Tipo cmis:IsPrivateWorkingCopy
//                case "IsPrivateWorkingCopy":
//                    Attribute attrLockedBy = recObjOPD.getAttr("LockedBy");
//                    String usuSesion = obj.getDrv().getUser().getName();
//                    if (attrLockedBy.getValue() != null && attrLockedBy.getValue().toString().equals(usuSesion)) {
//                        recordString = recordString.concat("true").concat("&&");
//                    } else {
//                        recordString = recordString.concat("false").concat("&&");
//                    }
//                    enc = true;
//                    break;
//
//                // Tipo cmis:isVersionSeriesCheckedOut
//                case "isVersionSeriesCheckedOut":
//                    Attribute attrAux = recObjOPD.getAttr("LockedBy");
//                    if (attrAux.getValue() != null) {
//                        recordString = recordString.concat("true").concat("&&");
//                    }
//                    enc = true;
//                    break;
//
//                // Tipo cmis:versionSeriesCheckedOutId
//                case "versionSeriesCheckedOutId":
//                    Attribute attrLockedBy1 = recObjOPD.getAttr("LockedBy");
//                    String usuSesion1 = obj.getDrv().getUser().getName();
//                    if (attrLockedBy1.getValue() != null && attrLockedBy1.getValue().toString().equals(usuSesion1)) {
//                        recordString = recordString.concat(usuSesion1).concat("&&");
//                    } else {
//                        recordString = recordString.concat("").concat("&&");
//                    }
//                    enc = true;
//                    break;
//
//                // cmis:contentStreamId --> Siempre 0
//                case "contentStreamId":
//                    recordString = recordString.concat("0").concat("&&");
//                    enc = true;
//                    break;
//
//                // Tipo cmis:path
//                case "path":
//
//                    if (isFolder) {
//
//                        Attribute attrPDId = recObjOPD.getAttr("PDId");
//                        strMostrar = ((PDFolders) obj).getPathId((String) attrPDId.getValue());
//
//                        recordString = recordString.concat(strMostrar).concat("&&");
//                    } else {
//                        recordString = recordString.concat("").concat("&&");
//                    }
//
//                    enc = true;
//                    break;
//
//                // cmis:allowedChildObjectTypeIds --> "not set" siempre
//                case "allowedChildObjectTypeIds":
//                    recordString = recordString.concat("not set").concat("&&");
//                    enc = true;
//                    break;
//
//                default:
//                    while (attr != null && !enc) {
//
//                        strMostrar = attr.getName();
//                        if (strMostrar.equals(nombre)) {
//
//                            if (attr.getType() == 5) {
//                                recordString = recordString.concat(convertirDateToString((Date) attr.getValue()))
//                                        .concat("&&");
//                            } else {
//                                recordString = recordString.concat((String) attr.getValue()).concat("&&");
//                            }
//
//                            enc = true;
//                        }
//
//                        attr = recObjOPD.nextAttr();
//                    }
//
//                    break;
//                }
//
//            }
//
//            result.add(recordString);
//
//            record = obj.getDrv().NextRec(cur);
//        }
//        
//        
//        Properties props = null;
//
//        return props;
//
//    }
}
