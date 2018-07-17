package org.apache.chemistry.opencmis.isbanutil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.apache.chemistry.opencmis.commons.PropertyIds;
import org.apache.chemistry.opencmis.commons.definitions.PropertyDefinition;
import org.apache.chemistry.opencmis.commons.enums.BaseTypeId;
import org.apache.chemistry.opencmis.commons.enums.Cardinality;
import org.apache.chemistry.opencmis.commons.enums.PropertyType;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.BindingsObjectFactoryImpl;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.DocumentTypeDefinitionImpl;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.FolderTypeDefinitionImpl;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.PropertyBooleanDefinitionImpl;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.PropertyDateTimeDefinitionImpl;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.PropertyDecimalDefinitionImpl;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.PropertyIdDefinitionImpl;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.PropertyIntegerDefinitionImpl;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.PropertyStringDefinitionImpl;
import org.apache.chemistry.opencmis.commons.server.CallContext;
import org.apache.chemistry.opencmis.commons.spi.BindingsObjectFactory;
import org.apache.chemistry.opencmis.fileshare.FileShareTypeManager;
import org.apache.chemistry.opencmis.prodoc.SesionProDoc;

import prodoc.Attribute;
import prodoc.PDDocs;
import prodoc.PDException;
import prodoc.PDObjDefs;
import prodoc.Record;

public class XmlUtil {

    public static final int tINTEGER = 0;
    public static final int tFLOAT = 1;
    public static final int tSTRING = 2;
    public static final int tDATE = 3;
    public static final int tBOOLEAN = 4;
    public static final int tTIMESTAMP = 5;
    public static final int tTHES = 6;

    /**
     * Método para convertir XML de un Object Type de OPd a CMIS
     * 
     * @throws PDException
     */
    public static ArrayList<Object> convertirXmlObjectType(CallContext context, SesionProDoc sesion,
            String nombreObjectType) throws PDException {

        ArrayList<Object> result = new ArrayList<Object>();

        FileShareTypeManager fileShRep = new FileShareTypeManager();

        PDObjDefs objOPD = new PDObjDefs(sesion.getMainSession());
        String tipoObj;

        try {
            // Leer ObjectType OPD
            objOPD.Load(nombreObjectType);

            // Si existe el ObjectType OPD
            if (objOPD.getName() != null) {

                Record recObjOPD = objOPD.getRecord();

                String objParentName = recObjOPD.getAttr("Parent").getValue().toString();

                // Tipo Doc
                if (objParentName.equals("PD_DOCS")) {

                    tipoObj = "cmis:document";

                    DocumentTypeDefinitionImpl typeDef = (DocumentTypeDefinitionImpl) fileShRep
                            .getTypeDefinition(context, tipoObj);

                    // Crear el Object Type
                    // --> Datos "cabecera" CMIS - Están rellenas todos los que son necesarios para

                    // cmis:id
                    typeDef.setId(recObjOPD.getAttr("Name").getValue().toString());

                    // cmis:localName
                    typeDef.setLocalName(recObjOPD.getAttr("Name").getValue().toString());

                    // cmis:localNamespace

                    // cmis:parentId
                    typeDef.setParentTypeId(tipoObj);

                    // cmis:displayName
                    typeDef.setDisplayName(recObjOPD.getAttr("Name").getValue().toString());

                    // cmis:queryName
                    typeDef.setQueryName(tipoObj);

                    // cmis:description
                    typeDef.setDescription(recObjOPD.getAttr("Description").getValue().toString());

                    // cmis:baseId
                    BaseTypeId baseT = BaseTypeId.fromValue(tipoObj);
                    typeDef.setBaseTypeId(baseT);

                    // // cmis:creatable
                    // typeDef.setIsCreatable(true);
                    //
                    // // cmis:fileable
                    // typeDef.setIsFileable(true);
                    //
                    // // cmis:queryable
                    // typeDef.setIsQueryable(true);
                    //
                    // // cmis:fulltextIndexed
                    // typeDef.setIsFulltextIndexed(true);
                    //
                    // // cmis:includedInSupertypeQuery
                    // typeDef.setIsIncludedInSupertypeQuery(true);
                    //
                    // // cmis:controllablePolicy
                    // typeDef.setIsControllablePolicy(true);
                    //
                    // // cmis:controllableACL
                    // typeDef.setIsControllableAcl(true);
                    //
                    // // cmis:versionable
                    // typeDef.setIsVersionable(false);

                    // cmis:contentStreamAllowed

                    // --> FIN Datos "cabecera" CMIS

                    PDDocs docAux = new PDDocs(sesion.getMainSession(), nombreObjectType);
                    Record recAux = docAux.getRecSum();

                    recAux.initList();
                    Attribute attr = recAux.nextAttr();
                    PropertyDefinition<?> prop = null;

                    BindingsObjectFactory objectFactory = new BindingsObjectFactoryImpl();
                    // Map<String, PropertyData<?>> properties = new HashMap<String,
                    // PropertyData<?>>();
                    Map<String, PropertyDefinition<?>> propertyDefinitions = new HashMap<String, PropertyDefinition<?>>();

                    while (attr != null) {

                        String nombreAttr = attr.getName().toString();
                        String valorAttr = null;

                        if (attr.getValue() != null) {
                            valorAttr = attr.getValue().toString();
                        } else {
                            valorAttr = "";
                        }

                        switch (nombreAttr) {

                        case "ACL":
                            // TODO Tratamiento ACL
                            break;

                        case "DocDate":

                            // prop = new PropertyDateTimeDefinitionImpl();

                            // probar con la siguiente opcion
                            PropertyDateTimeDefinitionImpl prop2 = new PropertyDateTimeDefinitionImpl();
                            prop2.setDisplayName(nombreAttr);
                            prop2.setDescription(attr.getDescription());
                            prop2.setLocalName(nombreAttr);
                            prop2.setId(nombreAttr);
                            prop2.setPropertyType(PropertyType.DATETIME);
                            prop2.setCardinality(Cardinality.SINGLE);

                            propertyDefinitions.put(PropertyIds.CREATION_DATE, prop2);

                            propertyDefinitions.put(PropertyIds.LAST_MODIFICATION_DATE, prop2);

                            break;

                        case "DocType":

                            // prop = new PropertyIdDefinitionImpl();
                            // propertyDefinitions.put(PropertyIds.OBJECT_TYPE_ID, prop);

                            PropertyIdDefinitionImpl prop3 = new PropertyIdDefinitionImpl();
                            prop3.setDisplayName(nombreAttr);
                            prop3.setDescription(attr.getDescription());
                            prop3.setLocalName(nombreAttr);
                            prop3.setId(nombreAttr);
                            prop3.setPropertyType(PropertyType.ID);
                            prop3.setCardinality(Cardinality.SINGLE);

                            propertyDefinitions.put(PropertyIds.OBJECT_TYPE_ID, prop3);

                            break;

                        case "LockedBy":

                            PropertyStringDefinitionImpl prop4 = new PropertyStringDefinitionImpl();
                            prop4.setDisplayName(nombreAttr);
                            prop4.setDescription(attr.getDescription());
                            prop4.setLocalName(nombreAttr);
                            prop4.setId(nombreAttr);
                            prop4.setPropertyType(PropertyType.STRING);
                            prop4.setCardinality(Cardinality.SINGLE);

                            propertyDefinitions.put(nombreAttr, prop4);

                            break;

                        case "MimeType":

                            // prop = new PropertyStringDefinitionImpl();
                            // propertyDefinitions.put(PropertyIds.CONTENT_STREAM_MIME_TYPE, prop);

                            PropertyStringDefinitionImpl prop5 = new PropertyStringDefinitionImpl();
                            prop5.setDisplayName(nombreAttr);
                            prop5.setDescription(attr.getDescription());
                            prop5.setLocalName(nombreAttr);
                            prop5.setId(nombreAttr);
                            prop5.setPropertyType(PropertyType.STRING);
                            prop5.setCardinality(Cardinality.SINGLE);

                            propertyDefinitions.put(PropertyIds.CONTENT_STREAM_MIME_TYPE, prop5);

                            break;

                        case "Name":
                            // prop = new PropertyStringDefinitionImpl();
                            // propertyDefinitions.put(PropertyIds.CONTENT_STREAM_FILE_NAME, prop);

                            PropertyStringDefinitionImpl prop6 = new PropertyStringDefinitionImpl();
                            prop6.setDisplayName(nombreAttr);
                            prop6.setDescription(attr.getDescription());
                            prop6.setLocalName(nombreAttr);
                            prop6.setId(nombreAttr);
                            prop6.setPropertyType(PropertyType.STRING);
                            prop6.setCardinality(Cardinality.SINGLE);

                            propertyDefinitions.put(PropertyIds.CONTENT_STREAM_FILE_NAME, prop6);

                            break;

                        case "PDAutor":

                            // prop = new PropertyStringDefinitionImpl();
                            //
                            // propertyDefinitions.put(PropertyIds.CREATED_BY, prop);
                            //
                            // propertyDefinitions.put(PropertyIds.LAST_MODIFIED_BY, prop);

                            PropertyStringDefinitionImpl prop7 = new PropertyStringDefinitionImpl();
                            prop7.setDisplayName(nombreAttr);
                            prop7.setDescription(attr.getDescription());
                            prop7.setLocalName(nombreAttr);
                            prop7.setId(nombreAttr);
                            prop7.setPropertyType(PropertyType.STRING);
                            prop7.setCardinality(Cardinality.SINGLE);

                            propertyDefinitions.put(PropertyIds.CREATED_BY, prop7);

                            propertyDefinitions.put(PropertyIds.LAST_MODIFIED_BY, prop7);

                            break;

                        case "PDDate":

                            // prop = new PropertyDateTimeDefinitionImpl();
                            //
                            // propertyDefinitions.put(PropertyIds.CREATION_DATE, prop);
                            //
                            // propertyDefinitions.put(PropertyIds.LAST_MODIFICATION_DATE, prop);

                            PropertyDateTimeDefinitionImpl prop8 = new PropertyDateTimeDefinitionImpl();
                            prop8.setDisplayName(nombreAttr);
                            prop8.setDescription(attr.getDescription());
                            prop8.setLocalName(nombreAttr);
                            prop8.setId(nombreAttr);
                            prop8.setPropertyType(PropertyType.DATETIME);
                            prop8.setCardinality(Cardinality.SINGLE);

                            propertyDefinitions.put(PropertyIds.CREATION_DATE, prop8);

                            propertyDefinitions.put(PropertyIds.LAST_MODIFICATION_DATE, prop8);

                            break;

                        case "PDId":

                            // prop = new PropertyIdDefinitionImpl();
                            // propertyDefinitions.put(PropertyIds.OBJECT_ID, prop);

                            PropertyIdDefinitionImpl prop9 = new PropertyIdDefinitionImpl();
                            prop9.setDisplayName(nombreAttr);
                            prop9.setDescription(attr.getDescription());
                            prop9.setLocalName(nombreAttr);
                            prop9.setId(nombreAttr);
                            prop9.setPropertyType(PropertyType.ID);
                            prop9.setCardinality(Cardinality.SINGLE);

                            propertyDefinitions.put(PropertyIds.OBJECT_ID, prop9);

                            break;

                        case "ParentId":

                            // prop = new PropertyIdDefinitionImpl();
                            // propertyDefinitions.put(PropertyIds.PARENT_ID, prop);

                            PropertyIdDefinitionImpl prop10 = new PropertyIdDefinitionImpl();
                            prop10.setDisplayName(nombreAttr);
                            prop10.setDescription(attr.getDescription());
                            prop10.setLocalName(nombreAttr);
                            prop10.setId(nombreAttr);
                            prop10.setPropertyType(PropertyType.ID);
                            prop10.setCardinality(Cardinality.SINGLE);

                            propertyDefinitions.put(PropertyIds.PARENT_ID, prop10);

                            break;

                        case "PurgeDate":
                            break;

                        case "Reposit":
                            break;

                        case "Status":
                            break;

                        case "Title":

                            // prop = new PropertyStringDefinitionImpl();
                            // propertyDefinitions.put(PropertyIds.NAME, prop);

                            PropertyStringDefinitionImpl prop11 = new PropertyStringDefinitionImpl();
                            prop11.setDisplayName(nombreAttr);
                            prop11.setDescription(attr.getDescription());
                            prop11.setLocalName(nombreAttr);
                            prop11.setId(nombreAttr);
                            prop11.setPropertyType(PropertyType.STRING);
                            prop11.setCardinality(Cardinality.SINGLE);

                            propertyDefinitions.put(PropertyIds.NAME, prop11);

                            break;

                        case "Version":

                            // prop = new PropertyStringDefinitionImpl();
                            // propertyDefinitions.put(PropertyIds.VERSION_LABEL, prop);

                            PropertyStringDefinitionImpl prop12 = new PropertyStringDefinitionImpl();
                            prop12.setDisplayName(nombreAttr);
                            prop12.setDescription(attr.getDescription());
                            prop12.setLocalName(nombreAttr);
                            prop12.setId(nombreAttr);
                            prop12.setPropertyType(PropertyType.STRING);
                            prop12.setCardinality(Cardinality.SINGLE);

                            propertyDefinitions.put(PropertyIds.VERSION_LABEL, prop12);

                            break;          

                        // Resto de atributos
                        default:

                            // TODO Comprobar tratamiento para el resto de atributos que no sean los básicos
                            createOtherProperty(propertyDefinitions, recAux, nombreAttr);
                            break;
                        }

                        attr = recAux.nextAttr();
                    }

                    // Insertamos las propiedades al objeto CMIS
                    typeDef.setPropertyDefinitions(propertyDefinitions);

                    result.add(typeDef);

                } else { // Tipo Folder

                    tipoObj = "cmis:folder";

                    FolderTypeDefinitionImpl typeDef = (FolderTypeDefinitionImpl) fileShRep.getTypeDefinition(context,
                            tipoObj);

                    // crear el Object Type
                    // --> Datos "cabecera" CMIS - Están rellenas todos los que son necesarios para

                    // <cmis:id>Prueba_CH</cmis:id>
                    typeDef.setLocalName(recObjOPD.getAttr("PDId").getValue().toString());

                    // <cmis:localName>Prueba_CH</cmis:localName>
                    typeDef.setLocalName(recObjOPD.getAttr("Name").getValue().toString());

                    // <cmis:localNamespace>http://chemistry.apache.org/opencmis/demo/</cmis:localNamespace>

                    // <cmis:parentId>cmis:document</cmis:parentId>
                    typeDef.setParentTypeId(tipoObj);

                    // <cmis:displayName>Prueba_CH</cmis:displayName>
                    typeDef.setDisplayName(recObjOPD.getAttr("Name").getValue().toString());

                    // <cmis:queryName>TESTDOCTYPE</cmis:queryName>
                    typeDef.setQueryName(tipoObj);

                    // <cmis:description>Mi prueba de definicion</cmis:description>

                    // <cmis:baseId>cmis:document</cmis:baseId>
                    // TODO Comrprobar funcionamiento
                    BaseTypeId baseT = BaseTypeId.fromValue(tipoObj);
                    typeDef.setBaseTypeId(baseT);

                    // // <cmis:creatable>true</cmis:creatable>
                    // typeDef.setIsCreatable(true);
                    //
                    // // <cmis:fileable>true</cmis:fileable>
                    // typeDef.setIsFileable(true);
                    //
                    // // <cmis:queryable>false</cmis:queryable>
                    // typeDef.setIsQueryable(true);
                    //
                    // // <cmis:fulltextIndexed>false</cmis:fulltextIndexed>
                    // typeDef.setIsFulltextIndexed(true);
                    //
                    // // <cmis:includedInSupertypeQuery>true</cmis:includedInSupertypeQuery>
                    // typeDef.setIsIncludedInSupertypeQuery(true);
                    //
                    // // <cmis:controllablePolicy>false</cmis:controllablePolicy>
                    // typeDef.setIsControllablePolicy(true);
                    //
                    // // <cmis:controllableACL>false</cmis:controllableACL>
                    // typeDef.setIsControllableAcl(true);

                    // <cmis:contentStreamAllowed>required</cmis:contentStreamAllowed>

                    // --> FIN Datos "cabecera" CMIS

                    PDDocs docAux = new PDDocs(sesion.getMainSession(), nombreObjectType);
                    Record recAux = docAux.getRecSum();

                    recAux.initList();
                    Attribute attr = recAux.nextAttr();
                    PropertyDefinition<?> prop = null;

                    BindingsObjectFactory objectFactory = new BindingsObjectFactoryImpl();
                    // Map<String, PropertyData<?>> properties = new HashMap<String,
                    // PropertyData<?>>();
                    Map<String, PropertyDefinition<?>> propertyDefinitions = new HashMap<String, PropertyDefinition<?>>();

                    while (attr != null) {

                        String nombreAttr = attr.getName().toString();
                        String valorAttr = null;

                        if (attr.getValue() != null) {
                            valorAttr = attr.getValue().toString();
                        } else {
                            valorAttr = "";
                        }

                        switch (nombreAttr) {

                        case "ACL":
                            // TODO Tratamiento ACL
                            break;

                        case "FolderType":

                            PropertyIdDefinitionImpl prop3 = new PropertyIdDefinitionImpl();
                            prop3.setDisplayName(nombreAttr);
                            prop3.setDescription(attr.getDescription());
                            prop3.setLocalName(nombreAttr);
                            prop3.setId(nombreAttr);
                            prop3.setPropertyType(PropertyType.ID);
                            prop3.setCardinality(Cardinality.SINGLE);

                            propertyDefinitions.put(PropertyIds.OBJECT_TYPE_ID, prop3);

                            break;

                        case "PDAutor":

                            // prop = new PropertyStringDefinitionImpl();
                            //
                            // propertyDefinitions.put(PropertyIds.CREATED_BY, prop);
                            //
                            // propertyDefinitions.put(PropertyIds.LAST_MODIFIED_BY, prop);

                            PropertyStringDefinitionImpl prop7 = new PropertyStringDefinitionImpl();
                            prop7.setDisplayName(nombreAttr);
                            prop7.setDescription(attr.getDescription());
                            prop7.setLocalName(nombreAttr);
                            prop7.setId(nombreAttr);
                            prop7.setPropertyType(PropertyType.STRING);
                            prop7.setCardinality(Cardinality.SINGLE);

                            propertyDefinitions.put(PropertyIds.CREATED_BY, prop7);

                            propertyDefinitions.put(PropertyIds.LAST_MODIFIED_BY, prop7);

                            break;

                        case "PDDate":

                            // prop = new PropertyDateTimeDefinitionImpl();
                            //
                            // propertyDefinitions.put(PropertyIds.CREATION_DATE, prop);
                            //
                            // propertyDefinitions.put(PropertyIds.LAST_MODIFICATION_DATE, prop);

                            PropertyDateTimeDefinitionImpl prop8 = new PropertyDateTimeDefinitionImpl();
                            prop8.setDisplayName(nombreAttr);
                            prop8.setDescription(attr.getDescription());
                            prop8.setLocalName(nombreAttr);
                            prop8.setId(nombreAttr);
                            prop8.setPropertyType(PropertyType.DATETIME);
                            prop8.setCardinality(Cardinality.SINGLE);

                            propertyDefinitions.put(PropertyIds.CREATION_DATE, prop8);

                            propertyDefinitions.put(PropertyIds.LAST_MODIFICATION_DATE, prop8);

                            break;

                        case "PDId":

                            // prop = new PropertyIdDefinitionImpl();
                            // propertyDefinitions.put(PropertyIds.OBJECT_ID, prop);

                            PropertyIdDefinitionImpl prop9 = new PropertyIdDefinitionImpl();
                            prop9.setDisplayName(nombreAttr);
                            prop9.setDescription(attr.getDescription());
                            prop9.setLocalName(nombreAttr);
                            prop9.setId(nombreAttr);
                            prop9.setPropertyType(PropertyType.ID);
                            prop9.setCardinality(Cardinality.SINGLE);

                            propertyDefinitions.put(PropertyIds.OBJECT_ID, prop9);

                            break;

                        case "ParentId":

                            // prop = new PropertyIdDefinitionImpl();
                            // propertyDefinitions.put(PropertyIds.PARENT_ID, prop);

                            PropertyIdDefinitionImpl prop10 = new PropertyIdDefinitionImpl();
                            prop10.setDisplayName(nombreAttr);
                            prop10.setDescription(attr.getDescription());
                            prop10.setLocalName(nombreAttr);
                            prop10.setId(nombreAttr);
                            prop10.setPropertyType(PropertyType.ID);
                            prop10.setCardinality(Cardinality.SINGLE);

                            propertyDefinitions.put(PropertyIds.PARENT_ID, prop10);

                            break;

                        case "Title":

                            // prop = new PropertyStringDefinitionImpl();
                            // propertyDefinitions.put(PropertyIds.NAME, prop);

                            PropertyStringDefinitionImpl prop11 = new PropertyStringDefinitionImpl();
                            prop11.setDisplayName(nombreAttr);
                            prop11.setDescription(attr.getDescription());
                            prop11.setLocalName(nombreAttr);
                            prop11.setId(nombreAttr);
                            prop11.setPropertyType(PropertyType.STRING);
                            prop11.setCardinality(Cardinality.SINGLE);

                            propertyDefinitions.put(PropertyIds.NAME, prop11);

                            break;

                        // Resto de atributos
                        default:

                            // TODO Comprobar tratamiento para el resto de atributos que no sean los básicos
                            createOtherProperty(propertyDefinitions, recAux, nombreAttr);
                            break;
                        }

                        attr = recAux.nextAttr();
                    }

                    // Insertamos las propiedades al objeto CMIS
                    typeDef.setPropertyDefinitions(propertyDefinitions);

                    result.add(typeDef);
                }

            }

        } catch (PDException e) {
            throw e;
        }

        return result;

    }

    private static void createOtherProperty(Map<String, PropertyDefinition<?>> properties, Record recOPD,
            String nombreAttr) {

        Attribute attr = recOPD.getAttr(nombreAttr);
        int tipoAttr = attr.getType();

        switch (tipoAttr) {

        case tINTEGER:
            PropertyIntegerDefinitionImpl propInt = new PropertyIntegerDefinitionImpl();
            propInt.setDisplayName(nombreAttr);
            propInt.setDescription(attr.getDescription());
            propInt.setLocalName(nombreAttr);
            propInt.setId(nombreAttr);
            propInt.setPropertyType(PropertyType.INTEGER);
            propInt.setCardinality(Cardinality.SINGLE);

            properties.put(nombreAttr, propInt);
            break;

        case tFLOAT:
            PropertyDecimalDefinitionImpl propFloat = new PropertyDecimalDefinitionImpl();
            propFloat.setDisplayName(nombreAttr);
            propFloat.setDescription(attr.getDescription());
            propFloat.setLocalName(nombreAttr);
            propFloat.setId(nombreAttr);
            propFloat.setPropertyType(PropertyType.DECIMAL);
            propFloat.setCardinality(Cardinality.SINGLE);

            properties.put(nombreAttr, propFloat);
            break;

        case tSTRING:

            PropertyStringDefinitionImpl propString = new PropertyStringDefinitionImpl();
            propString.setDisplayName(nombreAttr);
            propString.setDescription(attr.getDescription());
            propString.setLocalName(nombreAttr);
            propString.setId(nombreAttr);
            propString.setPropertyType(PropertyType.STRING);
            propString.setCardinality(Cardinality.SINGLE);

            properties.put(nombreAttr, propString);

            break;

        case tDATE:

            PropertyDateTimeDefinitionImpl propDate = new PropertyDateTimeDefinitionImpl();
            propDate.setDisplayName(nombreAttr);
            propDate.setDescription(attr.getDescription());
            propDate.setLocalName(nombreAttr);
            propDate.setId(nombreAttr);
            propDate.setPropertyType(PropertyType.DATETIME);
            propDate.setCardinality(Cardinality.SINGLE);

            properties.put(nombreAttr, propDate);

            break;

        case tBOOLEAN:

            PropertyBooleanDefinitionImpl propBool = new PropertyBooleanDefinitionImpl();
            propBool.setDisplayName(nombreAttr);
            propBool.setDescription(attr.getDescription());
            propBool.setLocalName(nombreAttr);
            propBool.setId(nombreAttr);
            propBool.setPropertyType(PropertyType.BOOLEAN);
            propBool.setCardinality(Cardinality.SINGLE);

            properties.put(nombreAttr, propBool);

            break;

        case tTIMESTAMP:

            PropertyDateTimeDefinitionImpl propTime = new PropertyDateTimeDefinitionImpl();
            propTime.setDisplayName(nombreAttr);
            propTime.setDescription(attr.getDescription());
            propTime.setLocalName(nombreAttr);
            propTime.setId(nombreAttr);
            propTime.setPropertyType(PropertyType.DATETIME);
            propTime.setCardinality(Cardinality.SINGLE);

            properties.put(nombreAttr, propTime);

            break;

        case tTHES:
            // TODO : Hacer tratamiento
            break;

        default:
            // TODO : Hacer tratamiento
            break;
        }

    }
}
