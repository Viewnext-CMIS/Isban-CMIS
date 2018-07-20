package org.apache.chemistry.opencmis.isbanutil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.apache.chemistry.opencmis.commons.PropertyIds;
import org.apache.chemistry.opencmis.commons.definitions.MutablePropertyDefinition;
import org.apache.chemistry.opencmis.commons.definitions.PropertyDefinition;
import org.apache.chemistry.opencmis.commons.enums.BaseTypeId;
import org.apache.chemistry.opencmis.commons.enums.Cardinality;
import org.apache.chemistry.opencmis.commons.enums.PropertyType;
import org.apache.chemistry.opencmis.commons.enums.Updatability;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.BindingsObjectFactoryImpl;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.DocumentTypeDefinitionImpl;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.FolderTypeDefinitionImpl;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.PropertyBooleanDefinitionImpl;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.PropertyDateTimeDefinitionImpl;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.PropertyDecimalDefinitionImpl;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.PropertyIntegerDefinitionImpl;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.PropertyStringDefinitionImpl;
import org.apache.chemistry.opencmis.commons.server.CallContext;
import org.apache.chemistry.opencmis.commons.spi.BindingsObjectFactory;
import org.apache.chemistry.opencmis.fileshare.FileShareTypeManager;
import org.apache.chemistry.opencmis.prodoc.SesionProDoc;

import prodoc.Attribute;
import prodoc.PDDocs;
import prodoc.PDException;
import prodoc.PDFolders;
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
    public static ArrayList<Object> crearObjectType(CallContext context, SesionProDoc sesion, String nombreObjectType)
            throws PDException {

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

                PropertyStringDefinitionImpl propString = new PropertyStringDefinitionImpl();
                PropertyDateTimeDefinitionImpl propDate = new PropertyDateTimeDefinitionImpl();

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

                    // cmis:creatable
                    typeDef.setIsCreatable(true);

                    // cmis:fileable
                    typeDef.setIsFileable(true);

                    // cmis:queryable
                    typeDef.setIsQueryable(true);

                    // cmis:fulltextIndexed
                    typeDef.setIsFulltextIndexed(true);

                    // cmis:includedInSupertypeQuery
                    typeDef.setIsIncludedInSupertypeQuery(true);

                    // cmis:controllablePolicy
                    typeDef.setIsControllablePolicy(true);

                    // cmis:controllableACL
                    typeDef.setIsControllableAcl(true);

                    // cmis:versionable
                    typeDef.setIsVersionable(false);

                    // cmis:contentStreamAllowed

                    // --> FIN Datos "cabecera" CMIS

                    PDDocs docAux = new PDDocs(sesion.getMainSession(), nombreObjectType);
                    Record recAux = docAux.getRecSum();

                    recAux.initList();
                    Attribute attr = recAux.nextAttr();
                    boolean multivaluado = attr.isMultivalued();

                    PropertyDefinition<?> prop = null;

                    BindingsObjectFactory objectFactory = new BindingsObjectFactoryImpl();

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

                            crearPropiedad(propDate, PropertyType.DATETIME, nombreAttr, attr.getDescription(),
                                    multivaluado, true, false, false, false, Updatability.READWRITE);

                            propertyDefinitions.put(PropertyIds.CREATION_DATE, propDate);

                            propertyDefinitions.put(PropertyIds.LAST_MODIFICATION_DATE, propDate);

                            break;

                        case "DocType":

                            // prop3.setPropertyType(PropertyType.ID);

                            crearPropiedad(propString, PropertyType.STRING, nombreAttr, attr.getDescription(),
                                    multivaluado, true, true, false, false, Updatability.READWRITE);

                            propertyDefinitions.put(PropertyIds.OBJECT_TYPE_ID, propString);

                            break;

                        case "LockedBy":

                            crearPropiedad(propString, PropertyType.STRING, nombreAttr, attr.getDescription(),
                                    multivaluado, true, true, false, false, Updatability.READWRITE);

                            propertyDefinitions.put(nombreAttr, propString);

                            break;

                        case "MimeType":

                            crearPropiedad(propString, PropertyType.STRING, nombreAttr, attr.getDescription(),
                                    multivaluado, true, true, false, false, Updatability.READWRITE);

                            propertyDefinitions.put(PropertyIds.CONTENT_STREAM_MIME_TYPE, propString);

                            break;

                        case "Name":

                            crearPropiedad(propString, PropertyType.STRING, nombreAttr, attr.getDescription(),
                                    multivaluado, true, true, false, false, Updatability.READWRITE);

                            propertyDefinitions.put(PropertyIds.CONTENT_STREAM_FILE_NAME, propString);

                            break;

                        case "PDAutor":

                            crearPropiedad(propString, PropertyType.STRING, nombreAttr, attr.getDescription(),
                                    multivaluado, true, true, false, false, Updatability.READWRITE);

                            propertyDefinitions.put(PropertyIds.CREATED_BY, propString);

                            propertyDefinitions.put(PropertyIds.LAST_MODIFIED_BY, propString);

                            break;

                        case "PDDate":

                            crearPropiedad(propDate, PropertyType.DATETIME, nombreAttr, attr.getDescription(),
                                    multivaluado, true, true, false, false, Updatability.READWRITE);

                            propertyDefinitions.put(PropertyIds.CREATION_DATE, propDate);

                            propertyDefinitions.put(PropertyIds.LAST_MODIFICATION_DATE, propDate);

                            break;

                        case "PDId":

                            // prop9.setPropertyType(PropertyType.ID);

                            crearPropiedad(propString, PropertyType.STRING, nombreAttr, attr.getDescription(),
                                    multivaluado, true, true, false, false, Updatability.READWRITE);

                            propertyDefinitions.put(PropertyIds.OBJECT_ID, propString);

                            break;

                        case "ParentId":

                            // prop10.setPropertyType(PropertyType.ID);

                            crearPropiedad(propString, PropertyType.STRING, nombreAttr, attr.getDescription(),
                                    multivaluado, true, true, false, false, Updatability.READWRITE);

                            propertyDefinitions.put(PropertyIds.PARENT_ID, propString);

                            break;

                        case "PurgeDate":
                            break;

                        case "Reposit":
                            break;

                        case "Status":
                            break;

                        case "Title":

                            crearPropiedad(propString, PropertyType.STRING, nombreAttr, attr.getDescription(),
                                    multivaluado, true, true, false, false, Updatability.READWRITE);

                            propertyDefinitions.put(PropertyIds.NAME, propString);

                            break;

                        case "Version":

                            crearPropiedad(propString, PropertyType.STRING, nombreAttr, attr.getDescription(),
                                    multivaluado, true, true, false, false, Updatability.READWRITE);

                            propertyDefinitions.put(PropertyIds.VERSION_LABEL, propString);

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
                    typeDef.setId(recObjOPD.getAttr("Name").getValue().toString());

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

                    // <cmis:creatable>true</cmis:creatable>
                    typeDef.setIsCreatable(true);

                    // <cmis:fileable>true</cmis:fileable>
                    typeDef.setIsFileable(true);

                    // <cmis:queryable>false</cmis:queryable>
                    typeDef.setIsQueryable(true);

                    // <cmis:fulltextIndexed>false</cmis:fulltextIndexed>
                    typeDef.setIsFulltextIndexed(true);

                    // <cmis:includedInSupertypeQuery>true</cmis:includedInSupertypeQuery>
                    typeDef.setIsIncludedInSupertypeQuery(true);

                    // <cmis:controllablePolicy>false</cmis:controllablePolicy>
                    typeDef.setIsControllablePolicy(true);

                    // <cmis:controllableACL>false</cmis:controllableACL>
                    typeDef.setIsControllableAcl(true);

                    // <cmis:contentStreamAllowed>required</cmis:contentStreamAllowed>

                    // --> FIN Datos "cabecera" CMIS

                    PDFolders folAux = new PDFolders(sesion.getMainSession(), nombreObjectType);
                    Record recAux = folAux.getRecSum();

                    recAux.initList();
                    Attribute attr = recAux.nextAttr();
                    boolean multivaluado = attr.isMultivalued();
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

                            // prop3.setPropertyType(PropertyType.ID);
                            // prop3.setUpdatability(Updatability.READWRITE);

                            crearPropiedad(propString, PropertyType.STRING, nombreAttr, attr.getDescription(),
                                    multivaluado, false, false, false, false, Updatability.READWRITE);

                            propertyDefinitions.put(PropertyIds.OBJECT_TYPE_ID, propString);

                            break;

                        case "PDAutor":

                            crearPropiedad(propString, PropertyType.STRING, nombreAttr, attr.getDescription(),
                                    multivaluado, false, false, false, false, Updatability.READWRITE);

                            propertyDefinitions.put(PropertyIds.CREATED_BY, propString);

                            propertyDefinitions.put(PropertyIds.LAST_MODIFIED_BY, propString);

                            break;

                        case "PDDate":

                            crearPropiedad(propDate, PropertyType.DATETIME, nombreAttr, attr.getDescription(),
                                    multivaluado, false, false, false, false, Updatability.READWRITE);

                            propertyDefinitions.put(PropertyIds.CREATION_DATE, propDate);

                            propertyDefinitions.put(PropertyIds.LAST_MODIFICATION_DATE, propDate);

                            break;

                        case "PDId":

                            // prop9.setPropertyType(PropertyType.ID);

                            crearPropiedad(propString, PropertyType.STRING, nombreAttr, attr.getDescription(),
                                    multivaluado, false, false, false, false, Updatability.READWRITE);

                            propertyDefinitions.put(PropertyIds.OBJECT_ID, propString);

                            break;

                        case "ParentId":

                            // prop10.setPropertyType(PropertyType.ID);

                            crearPropiedad(propString, PropertyType.STRING, nombreAttr, attr.getDescription(),
                                    multivaluado, false, false, false, false, Updatability.READWRITE);

                            propertyDefinitions.put(PropertyIds.PARENT_ID, propString);

                            break;

                        case "Title":

                            crearPropiedad(propString, PropertyType.STRING, nombreAttr, attr.getDescription(),
                                    multivaluado, false, false, false, false, Updatability.READWRITE);

                            propertyDefinitions.put(PropertyIds.NAME, propString);

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
        boolean multivaluado = attr.isMultivalued();

        switch (tipoAttr) {

        case tINTEGER:
            PropertyIntegerDefinitionImpl propInt = new PropertyIntegerDefinitionImpl();

            crearPropiedad(propInt, PropertyType.INTEGER, nombreAttr, attr.getDescription(), multivaluado, true, false,
                    false, false, Updatability.READWRITE);

            properties.put(nombreAttr, propInt);

            break;

        case tFLOAT:
            PropertyDecimalDefinitionImpl propFloat = new PropertyDecimalDefinitionImpl();

            crearPropiedad(propFloat, PropertyType.DECIMAL, nombreAttr, attr.getDescription(), multivaluado, true,
                    false, false, false, Updatability.READWRITE);

            properties.put(nombreAttr, propFloat);

            break;

        case tSTRING:

            PropertyStringDefinitionImpl propString = new PropertyStringDefinitionImpl();

            crearPropiedad(propString, PropertyType.STRING, nombreAttr, attr.getDescription(), multivaluado, true,
                    false, false, false, Updatability.READWRITE);

            properties.put(nombreAttr, propString);

            break;

        case tDATE:

            PropertyDateTimeDefinitionImpl propDate = new PropertyDateTimeDefinitionImpl();

            crearPropiedad(propDate, PropertyType.DATETIME, nombreAttr, attr.getDescription(), multivaluado, true,
                    false, false, false, Updatability.READWRITE);

            properties.put(nombreAttr, propDate);

            break;

        case tBOOLEAN:

            PropertyBooleanDefinitionImpl propBool = new PropertyBooleanDefinitionImpl();

            crearPropiedad(propBool, PropertyType.BOOLEAN, nombreAttr, attr.getDescription(), multivaluado, true, false,
                    false, false, Updatability.READWRITE);

            properties.put(nombreAttr, propBool);

            break;

        case tTIMESTAMP:

            PropertyDateTimeDefinitionImpl propTime = new PropertyDateTimeDefinitionImpl();

            crearPropiedad(propTime, PropertyType.DATETIME, nombreAttr, attr.getDescription(), multivaluado, true,
                    false, false, false, Updatability.READWRITE);

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

    private static void crearPropiedad(MutablePropertyDefinition prop, PropertyType propertyType, String nombre,
            String descripcion, boolean multivaluado, boolean isQueryable, boolean isOrderable, boolean isRequired,
            boolean isInherited, Updatability updatability) {

        prop.setDisplayName(nombre);
        prop.setDescription(descripcion);
        prop.setLocalName(nombre);
        prop.setId(nombre);
        prop.setPropertyType(propertyType);

        if (!multivaluado) {
            prop.setCardinality(Cardinality.SINGLE);
        } else {
            prop.setCardinality(Cardinality.MULTI);
        }

        prop.setIsQueryable(isQueryable);
        prop.setIsOrderable(isOrderable);
        prop.setIsRequired(isRequired);
        prop.setIsInherited(isInherited);
        prop.setUpdatability(updatability);
    }

}
