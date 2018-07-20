/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.chemistry.opencmis.fileshare;

import java.awt.Desktop;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.StringReader;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.EnumSet;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;

import org.apache.chemistry.opencmis.commons.BasicPermissions;
import org.apache.chemistry.opencmis.commons.PropertyIds;
import org.apache.chemistry.opencmis.commons.data.Ace;
import org.apache.chemistry.opencmis.commons.data.Acl;
import org.apache.chemistry.opencmis.commons.data.AllowableActions;
import org.apache.chemistry.opencmis.commons.data.BulkUpdateObjectIdAndChangeToken;
import org.apache.chemistry.opencmis.commons.data.ContentStream;
import org.apache.chemistry.opencmis.commons.data.ExtensionsData;
import org.apache.chemistry.opencmis.commons.data.FailedToDeleteData;
import org.apache.chemistry.opencmis.commons.data.ObjectData;
import org.apache.chemistry.opencmis.commons.data.ObjectInFolderContainer;
import org.apache.chemistry.opencmis.commons.data.ObjectInFolderData;
import org.apache.chemistry.opencmis.commons.data.ObjectInFolderList;
import org.apache.chemistry.opencmis.commons.data.ObjectList;
import org.apache.chemistry.opencmis.commons.data.ObjectParentData;
import org.apache.chemistry.opencmis.commons.data.PermissionMapping;
import org.apache.chemistry.opencmis.commons.data.Properties;
import org.apache.chemistry.opencmis.commons.data.PropertyData;
import org.apache.chemistry.opencmis.commons.data.PropertyDateTime;
import org.apache.chemistry.opencmis.commons.data.PropertyString;
import org.apache.chemistry.opencmis.commons.data.RepositoryInfo;
import org.apache.chemistry.opencmis.commons.definitions.PermissionDefinition;
import org.apache.chemistry.opencmis.commons.definitions.PropertyDefinition;
import org.apache.chemistry.opencmis.commons.definitions.TypeDefinition;
import org.apache.chemistry.opencmis.commons.definitions.TypeDefinitionContainer;
import org.apache.chemistry.opencmis.commons.definitions.TypeDefinitionList;
import org.apache.chemistry.opencmis.commons.enums.AclPropagation;
import org.apache.chemistry.opencmis.commons.enums.Action;
import org.apache.chemistry.opencmis.commons.enums.BaseTypeId;
import org.apache.chemistry.opencmis.commons.enums.CapabilityAcl;
import org.apache.chemistry.opencmis.commons.enums.CapabilityChanges;
import org.apache.chemistry.opencmis.commons.enums.CapabilityContentStreamUpdates;
import org.apache.chemistry.opencmis.commons.enums.CapabilityJoin;
import org.apache.chemistry.opencmis.commons.enums.CapabilityOrderBy;
import org.apache.chemistry.opencmis.commons.enums.CapabilityQuery;
import org.apache.chemistry.opencmis.commons.enums.CapabilityRenditions;
import org.apache.chemistry.opencmis.commons.enums.CmisVersion;
import org.apache.chemistry.opencmis.commons.enums.IncludeRelationships;
import org.apache.chemistry.opencmis.commons.enums.SupportedPermissions;
import org.apache.chemistry.opencmis.commons.enums.Updatability;
import org.apache.chemistry.opencmis.commons.enums.VersioningState;
import org.apache.chemistry.opencmis.commons.exceptions.CmisBaseException;
import org.apache.chemistry.opencmis.commons.exceptions.CmisConstraintException;
import org.apache.chemistry.opencmis.commons.exceptions.CmisContentAlreadyExistsException;
import org.apache.chemistry.opencmis.commons.exceptions.CmisInvalidArgumentException;
import org.apache.chemistry.opencmis.commons.exceptions.CmisNameConstraintViolationException;
import org.apache.chemistry.opencmis.commons.exceptions.CmisObjectNotFoundException;
import org.apache.chemistry.opencmis.commons.exceptions.CmisPermissionDeniedException;
import org.apache.chemistry.opencmis.commons.exceptions.CmisRuntimeException;
import org.apache.chemistry.opencmis.commons.exceptions.CmisStorageException;
import org.apache.chemistry.opencmis.commons.exceptions.CmisStreamNotSupportedException;
import org.apache.chemistry.opencmis.commons.exceptions.CmisUpdateConflictException;
import org.apache.chemistry.opencmis.commons.impl.Base64;
import org.apache.chemistry.opencmis.commons.impl.IOUtils;
import org.apache.chemistry.opencmis.commons.impl.MimeTypes;
import org.apache.chemistry.opencmis.commons.impl.XMLConstants;
import org.apache.chemistry.opencmis.commons.impl.XMLConverter;
import org.apache.chemistry.opencmis.commons.impl.XMLUtils;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.AccessControlEntryImpl;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.AccessControlListImpl;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.AccessControlPrincipalDataImpl;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.AclCapabilitiesDataImpl;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.AllowableActionsImpl;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.BindingsObjectFactoryImpl;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.BulkUpdateObjectIdAndChangeTokenImpl;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.ContentStreamImpl;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.CreatablePropertyTypesImpl;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.DocumentTypeDefinitionImpl;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.FailedToDeleteDataImpl;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.FolderTypeDefinitionImpl;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.NewTypeSettableAttributesImpl;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.ObjectDataImpl;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.ObjectInFolderContainerImpl;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.ObjectInFolderDataImpl;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.ObjectInFolderListImpl;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.ObjectParentDataImpl;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.PartialContentStreamImpl;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.PermissionDefinitionDataImpl;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.PermissionMappingDataImpl;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.PropertiesImpl;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.PropertyBooleanImpl;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.PropertyDateTimeImpl;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.PropertyDecimalImpl;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.PropertyHtmlImpl;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.PropertyIdImpl;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.PropertyIntegerImpl;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.PropertyStringImpl;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.PropertyUriImpl;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.RepositoryCapabilitiesImpl;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.RepositoryInfoImpl;
import org.apache.chemistry.opencmis.commons.impl.server.ObjectInfoImpl;
import org.apache.chemistry.opencmis.commons.server.CallContext;
import org.apache.chemistry.opencmis.commons.server.ObjectInfoHandler;
import org.apache.chemistry.opencmis.commons.spi.BindingsObjectFactory;
import org.apache.chemistry.opencmis.commons.spi.Holder;
import org.apache.chemistry.opencmis.isbanutil.FilterParser;
import org.apache.chemistry.opencmis.isbanutil.QueryUtil;
import org.apache.chemistry.opencmis.isbanutil.QueryValidator;
import org.apache.chemistry.opencmis.isbanutil.XmlUtil;
import org.apache.chemistry.opencmis.prodoc.InsertProDoc;
import org.apache.chemistry.opencmis.prodoc.SesionProDoc;
import org.apache.chemistry.opencmis.prodoc.UpdateProDoc;
import org.apache.chemistry.opencmis.server.impl.ServerVersion;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.parser.CCJSqlParserManager;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.select.FromItem;
import net.sf.jsqlparser.statement.select.Join;
import net.sf.jsqlparser.statement.select.PlainSelect;
import net.sf.jsqlparser.statement.select.Select;
import prodoc.Attribute;
import prodoc.Cursor;
import prodoc.ObjPD;
import prodoc.PDACL;
import prodoc.PDDocs;
import prodoc.PDException;
import prodoc.PDFolders;
import prodoc.PDMimeType;
import prodoc.PDObjDefs;
import prodoc.Record;

/**
 * Implements all repository operations.
 */
public class FileShareRepository {

    public static final int tINTEGER = 0;
    public static final int tFLOAT = 1;
    public static final int tSTRING = 2;
    public static final int tDATE = 3;
    public static final int tBOOLEAN = 4;
    public static final int tTIMESTAMP = 5;
    public static final int tTHES = 6;

    private static final Logger LOG = LoggerFactory.getLogger(FileShareRepository.class);

    private static final String ROOT_ID = "RootFolder";
    private static final String SHADOW_EXT = ".cmis.xml";
    private static final String SHADOW_FOLDER = "cmis.xml";

    private static final String USER_UNKNOWN = "<unknown>";

    private static final int BUFFER_SIZE = 64 * 1024;

    /** Repository id. */
    private String repositoryId = null;
    /** Root directory. */
    private File root = null;
    /** Types. */
    private FileShareTypeManager typeManager = null;
    /** Users. */
    private Map<String, Boolean> readWriteUserMap = null;

    /** CMIS 1.0 repository info. */
    private RepositoryInfo repositoryInfo10 = null;
    /** CMIS 1.1 repository info. */
    private RepositoryInfo repositoryInfo11 = null;

    public FileShareRepository(final String repositoryId, final String rootPath,
            final FileShareTypeManager typeManager) {

        // check repository id
        if (repositoryId == null || repositoryId.trim().length() == 0) {
            throw new IllegalArgumentException("Invalid repository id!");
        }

        this.repositoryId = repositoryId;

        // check root folder
        if (rootPath == null || rootPath.trim().length() == 0) {
            throw new IllegalArgumentException("Invalid root folder!");
        }

        root = new File("");

        // set type manager objects
        this.typeManager = typeManager;

        // set up read-write user map
        readWriteUserMap = new HashMap<String, Boolean>();

        // set up repository infos
        repositoryInfo10 = createRepositoryInfo(CmisVersion.CMIS_1_0);
        repositoryInfo11 = createRepositoryInfo(CmisVersion.CMIS_1_1);
    }

    private RepositoryInfo createRepositoryInfo(CmisVersion cmisVersion) {
        assert cmisVersion != null;

        RepositoryInfoImpl repositoryInfo = new RepositoryInfoImpl();

        repositoryInfo.setId(repositoryId);
        repositoryInfo.setName(repositoryId);
        repositoryInfo.setDescription(repositoryId);

        repositoryInfo.setCmisVersionSupported(cmisVersion.value());

        repositoryInfo.setProductName("OpenCMIS FileShare");
        repositoryInfo.setProductVersion(ServerVersion.OPENCMIS_VERSION);
        repositoryInfo.setVendorName("OpenCMIS");

        repositoryInfo.setRootFolder(ROOT_ID);

        repositoryInfo.setThinClientUri("");
        repositoryInfo.setChangesIncomplete(true);

        RepositoryCapabilitiesImpl capabilities = new RepositoryCapabilitiesImpl();
        capabilities.setCapabilityAcl(CapabilityAcl.DISCOVER);
        capabilities.setAllVersionsSearchable(false);
        capabilities.setCapabilityJoin(CapabilityJoin.NONE);
        capabilities.setSupportsMultifiling(false);
        capabilities.setSupportsUnfiling(false);
        capabilities.setSupportsVersionSpecificFiling(false);
        capabilities.setIsPwcSearchable(false);
        capabilities.setIsPwcUpdatable(false);
        capabilities.setCapabilityQuery(CapabilityQuery.FULLTEXTONLY);
        capabilities.setCapabilityChanges(CapabilityChanges.NONE);
        capabilities.setCapabilityContentStreamUpdates(CapabilityContentStreamUpdates.ANYTIME);
        capabilities.setSupportsGetDescendants(true);
        capabilities.setSupportsGetFolderTree(true);
        capabilities.setCapabilityRendition(CapabilityRenditions.NONE);

        if (cmisVersion != CmisVersion.CMIS_1_0) {
            capabilities.setCapabilityOrderBy(CapabilityOrderBy.COMMON);

            NewTypeSettableAttributesImpl typeSetAttributes = new NewTypeSettableAttributesImpl();
            typeSetAttributes.setCanSetControllableAcl(false);
            typeSetAttributes.setCanSetControllablePolicy(false);
            typeSetAttributes.setCanSetCreatable(false);
            typeSetAttributes.setCanSetDescription(false);
            typeSetAttributes.setCanSetDisplayName(false);
            typeSetAttributes.setCanSetFileable(false);
            typeSetAttributes.setCanSetFulltextIndexed(false);
            typeSetAttributes.setCanSetId(false);
            typeSetAttributes.setCanSetIncludedInSupertypeQuery(false);
            typeSetAttributes.setCanSetLocalName(false);
            typeSetAttributes.setCanSetLocalNamespace(false);
            typeSetAttributes.setCanSetQueryable(false);
            typeSetAttributes.setCanSetQueryName(false);

            capabilities.setNewTypeSettableAttributes(typeSetAttributes);

            CreatablePropertyTypesImpl creatablePropertyTypes = new CreatablePropertyTypesImpl();
            capabilities.setCreatablePropertyTypes(creatablePropertyTypes);
        }

        repositoryInfo.setCapabilities(capabilities);

        AclCapabilitiesDataImpl aclCapability = new AclCapabilitiesDataImpl();
        aclCapability.setSupportedPermissions(SupportedPermissions.BASIC);
        aclCapability.setAclPropagation(AclPropagation.OBJECTONLY);

        // permissions
        List<PermissionDefinition> permissions = new ArrayList<PermissionDefinition>();
        permissions.add(createPermission(BasicPermissions.READ, "Read"));
        permissions.add(createPermission(BasicPermissions.WRITE, "Write"));
        permissions.add(createPermission(BasicPermissions.ALL, "All"));
        aclCapability.setPermissionDefinitionData(permissions);

        // mapping
        List<PermissionMapping> list = new ArrayList<PermissionMapping>();
        list.add(createMapping(PermissionMapping.CAN_CREATE_DOCUMENT_FOLDER, BasicPermissions.READ));
        list.add(createMapping(PermissionMapping.CAN_CREATE_FOLDER_FOLDER, BasicPermissions.READ));
        list.add(createMapping(PermissionMapping.CAN_DELETE_CONTENT_DOCUMENT, BasicPermissions.WRITE));
        list.add(createMapping(PermissionMapping.CAN_DELETE_OBJECT, BasicPermissions.ALL));
        list.add(createMapping(PermissionMapping.CAN_DELETE_TREE_FOLDER, BasicPermissions.ALL));
        list.add(createMapping(PermissionMapping.CAN_GET_ACL_OBJECT, BasicPermissions.READ));
        list.add(createMapping(PermissionMapping.CAN_GET_ALL_VERSIONS_VERSION_SERIES, BasicPermissions.READ));
        list.add(createMapping(PermissionMapping.CAN_GET_CHILDREN_FOLDER, BasicPermissions.READ));
        list.add(createMapping(PermissionMapping.CAN_GET_DESCENDENTS_FOLDER, BasicPermissions.READ));
        list.add(createMapping(PermissionMapping.CAN_GET_FOLDER_PARENT_OBJECT, BasicPermissions.READ));
        list.add(createMapping(PermissionMapping.CAN_GET_PARENTS_FOLDER, BasicPermissions.READ));
        list.add(createMapping(PermissionMapping.CAN_GET_PROPERTIES_OBJECT, BasicPermissions.READ));
        list.add(createMapping(PermissionMapping.CAN_MOVE_OBJECT, BasicPermissions.WRITE));
        list.add(createMapping(PermissionMapping.CAN_MOVE_SOURCE, BasicPermissions.READ));
        list.add(createMapping(PermissionMapping.CAN_MOVE_TARGET, BasicPermissions.WRITE));
        list.add(createMapping(PermissionMapping.CAN_SET_CONTENT_DOCUMENT, BasicPermissions.WRITE));
        list.add(createMapping(PermissionMapping.CAN_UPDATE_PROPERTIES_OBJECT, BasicPermissions.WRITE));
        list.add(createMapping(PermissionMapping.CAN_VIEW_CONTENT_OBJECT, BasicPermissions.READ));
        Map<String, PermissionMapping> map = new LinkedHashMap<String, PermissionMapping>();
        for (PermissionMapping pm : list) {
            map.put(pm.getKey(), pm);
        }
        aclCapability.setPermissionMappingData(map);

        repositoryInfo.setAclCapabilities(aclCapability);

        return repositoryInfo;
    }

    private PermissionDefinition createPermission(String permission, String description) {
        PermissionDefinitionDataImpl pd = new PermissionDefinitionDataImpl();
        pd.setId(permission);
        pd.setDescription(description);

        return pd;
    }

    private PermissionMapping createMapping(String key, String permission) {
        PermissionMappingDataImpl pm = new PermissionMappingDataImpl();
        pm.setKey(key);
        pm.setPermissions(Collections.singletonList(permission));

        return pm;
    }

    /**
     * Returns the id of this repository.
     */
    public String getRepositoryId() {
        return repositoryId;
    }

    /**
     * Returns the root directory of this repository
     */
    public File getRootDirectory() {
        return root;
    }

    /**
     * Sets read-only flag for the given user.
     */
    public void setUserReadOnly(String user) {
        if (user == null || user.length() == 0) {
            return;
        }

        readWriteUserMap.put(user, true);
    }

    /**
     * Sets read-write flag for the given user.
     */
    public void setUserReadWrite(String user) {
        if (user == null || user.length() == 0) {
            return;
        }

        readWriteUserMap.put(user, false);
    }

    // --- CMIS operations ---

    /**
     * CMIS getRepositoryInfo.
     */
    public RepositoryInfo getRepositoryInfo(CallContext context) {
        debug("getRepositoryInfo");

        checkUser(context, false);

        if (context.getCmisVersion() == CmisVersion.CMIS_1_0) {
            return repositoryInfo10;
        } else {
            return repositoryInfo11;
        }
    }

    /**
     * CMIS getTypesChildren.
     */
    public TypeDefinitionList getTypeChildren(CallContext context, String typeId, Boolean includePropertyDefinitions,
            BigInteger maxItems, BigInteger skipCount) {
        debug("getTypesChildren");
        checkUser(context, false);

        return typeManager.getTypeChildren(context, typeId, includePropertyDefinitions, maxItems, skipCount);
    }

    /**
     * CMIS getTypesDescendants.
     */
    public List<TypeDefinitionContainer> getTypeDescendants(CallContext context, String typeId, BigInteger depth,
            Boolean includePropertyDefinitions) {
        debug("getTypesDescendants");
        checkUser(context, false);

        return typeManager.getTypeDescendants(context, typeId, depth, includePropertyDefinitions);
    }

    /**
     * CMIS getTypeDefinition.
     */
    public TypeDefinition getTypeDefinition(CallContext context, String typeId) {
        debug("getTypeDefinition");
        checkUser(context, false);

        return typeManager.getTypeDefinition(context, typeId);
    }

    /**
     * 
     * @param context
     * @param repositoryId
     * @param statement
     * @param searchAllVersions
     * @param includeAllowableActions
     * @param includeRelationships
     * @param renditionFilter
     * @param maxItems
     * @param skipCount
     * @param extension
     * @param sesProdoc
     * @return
     * @throws Exception
     * @throws JSQLParserException
     * @throws PDException
     */
    public ObjectList query(CallContext context, String repositoryId, String statement, Boolean searchAllVersions,
            Boolean includeAllowableActions, IncludeRelationships includeRelationships, String renditionFilter,
            BigInteger maxItems, BigInteger skipCount, ExtensionsData extension, SesionProDoc sesProdoc)
            throws Exception {
        CCJSqlParserManager managerSql = new CCJSqlParserManager();
        try {
            Vector<String> listaSalida = new Vector<>();

            if (!QueryValidator.validarStatement(statement)) {
                throw new CmisInvalidArgumentException(
                        "CONTAINS or IN_TREE must be unique. And they should be on the first level");
            }

            String query = QueryUtil.adaptarAProdoc(statement).trim();
            String contains = QueryUtil.getAddParam(query, 0);
            String inTree = QueryUtil.getAddParam(query, 1);

            Statement x = managerSql.parse(new StringReader(query));
            if (x instanceof Select) {
                PlainSelect selectStatement = (PlainSelect) ((Select) x).getSelectBody();
                FromItem from = selectStatement.getFromItem();
                listaSalida.addAll(QueryUtil.goToQuery(from.toString(), contains, inTree, sesProdoc.getMainSession(),
                        selectStatement));
                if (!selectStatement.getJoins().isEmpty()) {
                    Iterator it = selectStatement.getJoins().iterator();
                    while (it.hasNext()) {
                        Join j = (Join) it.next();
                        listaSalida.addAll(QueryUtil.goToQuery(j.toString(), contains, inTree,
                                sesProdoc.getMainSession(), selectStatement));
                    }
                }

            }
            mostrar(listaSalida, null);
        } catch (JSQLParserException e) {
            throw new CmisInvalidArgumentException(e.getCause().getMessage());
        } catch (PDException ex) {
            throw new CmisInvalidArgumentException(ex.getMessage());
        } catch (Exception exc) {
            throw exc;
        }

        return null;

    }

    /**
     * Solo para pruebas
     * 
     * @param listaSalida
     * @param camposSelect
     */
    private void mostrar(Vector<String> listaSalida, List<String> camposSelect) {
        String cabecera = "";
        for (String a : camposSelect) {
            if (cabecera != "") {
                cabecera += " || ";
            }
            cabecera += a;
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

    /**
     * Create* dispatch for AtomPub.
     */
    public ObjectData create(CallContext context, Properties properties, String folderId, ContentStream contentStream,
            VersioningState versioningState, ObjectInfoHandler objectInfos) {
        debug("create");
        boolean userReadOnly = checkUser(context, true);

        String typeId = FileShareUtils.getObjectTypeId(properties);
        TypeDefinition type = typeManager.getInternalTypeDefinition(typeId);
        if (type == null) {
            throw new CmisObjectNotFoundException("Type '" + typeId + "' is unknown!");
        }

        String objectId = null;
        if (type.getBaseTypeId() == BaseTypeId.CMIS_DOCUMENT) {
            objectId = createDocument(context, properties, folderId, contentStream, versioningState, null);// TODO: ->
                                                                                                           // modificar
                                                                                                           // null por
                                                                                                           // SesionProdoc
        } else if (type.getBaseTypeId() == BaseTypeId.CMIS_FOLDER) {
            if (contentStream != null || versioningState != null) {
                throw new CmisInvalidArgumentException("Cannot create a folder with content or a versioning state!");
            }

            objectId = createFolder(context, properties, folderId, null);// TODO: Sergio -> modificar null por
                                                                         // SesionProdoc
        } else {
            throw new CmisObjectNotFoundException("Cannot create object of type '" + typeId + "'!");
        }

        return compileObjectData(context, getFile(objectId), null, false, false, userReadOnly, objectInfos);
    }

    /**
     * CMIS createDocument.
     */
    public String createDocument(CallContext context, Properties properties, String folderId,
            ContentStream contentStream, VersioningState versioningState, SesionProDoc sesion) {

        debug("createDocument");
        checkUser(context, true);

        // check properties
        if (properties == null || properties.getProperties() == null) {
            throw new CmisInvalidArgumentException("Properties must be set!");
        }

        // check versioning state
        if (!(VersioningState.NONE == versioningState || versioningState == null)) {
            throw new CmisConstraintException("Versioning not supported!");
        }

        // check type
        String typeId = FileShareUtils.getObjectTypeId(properties);
        TypeDefinition type = typeManager.getInternalTypeDefinition(typeId);
        if (type == null) {
            throw new CmisObjectNotFoundException("Type '" + typeId + "' is unknown!");
        }
        if (type.getBaseTypeId() != BaseTypeId.CMIS_DOCUMENT) {
            throw new CmisInvalidArgumentException("Type must be a document type!");
        }

        // check the name
        String name = FileShareUtils.getStringProperty(properties, PropertyIds.NAME);
        if (!isValidName(name)) {
            throw new CmisNameConstraintViolationException("Name is not valid!");
        }

        String idFileOPD = InsertProDoc.crearDocumento(properties, sesion, folderId, contentStream, typeId);

        return idFileOPD;
    }

    /**
     * CMIS createDocumentFromSource.
     */
    public String createDocumentFromSource(CallContext context, String sourceId, Properties properties, String folderId,
            VersioningState versioningState) {
        debug("createDocumentFromSource");
        checkUser(context, true);

        // check versioning state
        if (!(VersioningState.NONE == versioningState || versioningState == null)) {
            throw new CmisConstraintException("Versioning not supported!");
        }

        // get parent File
        File parent = getFile(folderId);
        if (!parent.isDirectory()) {
            throw new CmisObjectNotFoundException("Parent is not a folder!");
        }

        // get source File
        File source = getFile(sourceId);
        if (!source.isFile()) {
            throw new CmisObjectNotFoundException("Source is not a document!");
        }

        // file name
        String name = source.getName();

        // get properties
        PropertiesImpl sourceProperties = new PropertiesImpl();
        readCustomProperties(source, sourceProperties, null, new ObjectInfoImpl());

        // get the type id
        String typeId = FileShareUtils.getIdProperty(sourceProperties, PropertyIds.OBJECT_TYPE_ID);
        if (typeId == null) {
            typeId = BaseTypeId.CMIS_DOCUMENT.value();
        }

        // copy properties
        PropertiesImpl newProperties = new PropertiesImpl();
        for (PropertyData<?> prop : sourceProperties.getProperties().values()) {
            if (prop.getId().equals(PropertyIds.OBJECT_TYPE_ID) || prop.getId().equals(PropertyIds.CREATED_BY)
                    || prop.getId().equals(PropertyIds.CREATION_DATE)
                    || prop.getId().equals(PropertyIds.LAST_MODIFIED_BY)) {
                continue;
            }

            newProperties.addProperty(prop);
        }

        // replace properties
        if (properties != null) {
            // find new name
            String newName = FileShareUtils.getStringProperty(properties, PropertyIds.NAME);
            if (newName != null) {
                if (!isValidName(newName)) {
                    throw new CmisNameConstraintViolationException("Name is not valid!");
                }
                name = newName;
            }

            // get the property definitions
            TypeDefinition type = typeManager.getInternalTypeDefinition(typeId);
            if (type == null) {
                throw new CmisObjectNotFoundException("Type '" + typeId + "' is unknown!");
            }
            if (type.getBaseTypeId() != BaseTypeId.CMIS_DOCUMENT) {
                throw new CmisInvalidArgumentException("Type must be a document type!");
            }

            // replace with new values
            for (PropertyData<?> prop : properties.getProperties().values()) {
                PropertyDefinition<?> propType = type.getPropertyDefinitions().get(prop.getId());

                // do we know that property?
                if (propType == null) {
                    throw new CmisConstraintException("Property '" + prop.getId() + "' is unknown!");
                }

                // can it be set?
                if (propType.getUpdatability() != Updatability.READWRITE) {
                    throw new CmisConstraintException("Property '" + prop.getId() + "' cannot be updated!");
                }

                // empty properties are invalid
                if (isEmptyProperty(prop)) {
                    throw new CmisConstraintException("Property '" + prop.getId() + "' must not be empty!");
                }

                newProperties.addProperty(prop);
            }
        }

        addPropertyId(newProperties, typeId, null, PropertyIds.OBJECT_TYPE_ID, typeId);
        addPropertyString(newProperties, typeId, null, PropertyIds.CREATED_BY, context.getUsername());
        addPropertyDateTime(newProperties, typeId, null, PropertyIds.CREATION_DATE,
                FileShareUtils.millisToCalendar(System.currentTimeMillis()));
        addPropertyString(newProperties, typeId, null, PropertyIds.LAST_MODIFIED_BY, context.getUsername());

        // check the file
        File newFile = new File(parent, name);
        if (newFile.exists()) {
            throw new CmisNameConstraintViolationException("Document already exists.");
        }

        // create the file
        try {
            newFile.createNewFile();
        } catch (IOException e) {
            throw new CmisStorageException("Could not create file: " + e.getMessage(), e);
        }

        // copy content
        try {
            writeContent(newFile, new FileInputStream(source));
        } catch (IOException e) {
            throw new CmisStorageException("Could not roead or write content: " + e.getMessage(), e);
        }

        // write properties
        writePropertiesFile(newFile, newProperties);

        return getId(newFile);
    }

    /**
     * Writes the content to disc.
     */
    private void writeContent(File newFile, InputStream stream) {
        OutputStream out = null;
        try {
            out = new FileOutputStream(newFile);
            IOUtils.copy(stream, out, BUFFER_SIZE);
        } catch (IOException e) {
            throw new CmisStorageException("Could not write content: " + e.getMessage(), e);
        } finally {
            IOUtils.closeQuietly(out);
            IOUtils.closeQuietly(stream);
        }
    }

    /**
     * CMIS createFolder.
     */
    public String createFolder(CallContext context, Properties properties, String folderId, SesionProDoc sesion) {

        String idFolderOPD = InsertProDoc.crearCarpeta(properties, sesion, folderId);

        return idFolderOPD;
    }

    /**
     * CMIS moveObject.
     */
    public ObjectData moveObject(CallContext context, Holder<String> objectId, String targetFolderId,
            ObjectInfoHandler objectInfos) {
        debug("moveObject");
        boolean userReadOnly = checkUser(context, true);

        if (objectId == null) {
            throw new CmisInvalidArgumentException("Id is not valid!");
        }

        // get the file and parent
        File file = getFile(objectId.getValue());
        File parent = getFile(targetFolderId);

        // build new path
        File newFile = new File(parent, file.getName());
        if (newFile.exists()) {
            throw new CmisStorageException("Object already exists!");
        }

        // move it
        if (!file.renameTo(newFile)) {
            throw new CmisStorageException("Move failed!");
        } else {
            // set new id
            objectId.setValue(getId(newFile));

            // if it is a file, move properties file too
            if (newFile.isFile()) {
                File propFile = getPropertiesFile(file);
                if (propFile.exists()) {
                    File newPropFile = new File(parent, propFile.getName());
                    if (!propFile.renameTo(newPropFile)) {
                        LOG.error("Could not rename properties file: {}", propFile.getName());
                    }
                }
            }
        }

        return compileObjectData(context, newFile, null, false, false, userReadOnly, objectInfos);
    }

    /**
     * CMIS setContentStream, deleteContentStream, and appendContentStream.
     */
    public void changeContentStream(CallContext context, Holder<String> objectId, Boolean overwriteFlag,
            ContentStream contentStream, boolean append) {
        debug("setContentStream or deleteContentStream or appendContentStream");
        checkUser(context, true);

        if (objectId == null) {
            throw new CmisInvalidArgumentException("Id is not valid!");
        }

        // get the file
        File file = getFile(objectId.getValue());
        if (!file.isFile()) {
            throw new CmisStreamNotSupportedException("Not a file!");
        }

        // check overwrite
        boolean owf = FileShareUtils.getBooleanParameter(overwriteFlag, true);
        if (!owf && file.length() > 0) {
            throw new CmisContentAlreadyExistsException("Content already exists!");
        }

        OutputStream out = null;
        InputStream in = null;
        try {
            out = new FileOutputStream(file, append);

            if (contentStream == null || contentStream.getStream() == null) {
                // delete content
                out.write(new byte[0]);
            } else {
                // set content
                in = contentStream.getStream();
                IOUtils.copy(in, out, BUFFER_SIZE);
            }
        } catch (Exception e) {
            throw new CmisStorageException("Could not write content: " + e.getMessage(), e);
        } finally {
            IOUtils.closeQuietly(out);
            IOUtils.closeQuietly(in);
        }
    }

    /**
     * CMIS deleteObject.
     */
    public void deleteObject(CallContext context, String objectId) {
        debug("deleteObject");
        checkUser(context, true);

        // get the file or folder
        File file = getFile(objectId);
        if (!file.exists()) {
            throw new CmisObjectNotFoundException("Object not found!");
        }

        // check if it is a folder and if it is empty
        if (!isFolderEmpty(file)) {
            throw new CmisConstraintException("Folder is not empty!");
        }

        // delete properties and actual file
        getPropertiesFile(file).delete();
        if (!file.delete()) {
            throw new CmisStorageException("Deletion failed!");
        }
    }

    /**
     * CMIS deleteTree.
     */
    public FailedToDeleteData deleteTree(CallContext context, String folderId, Boolean continueOnFailure) {
        debug("deleteTree");
        checkUser(context, true);

        boolean cof = FileShareUtils.getBooleanParameter(continueOnFailure, false);

        // get the file or folder
        File file = getFile(folderId);

        FailedToDeleteDataImpl result = new FailedToDeleteDataImpl();
        result.setIds(new ArrayList<String>());

        // if it is a folder, remove it recursively
        if (file.isDirectory()) {
            deleteFolder(file, cof, result);
        } else {
            throw new CmisConstraintException("Object is not a folder!");
        }

        return result;
    }

    /**
     * Removes a folder and its content.
     */
    private boolean deleteFolder(File folder, boolean continueOnFailure, FailedToDeleteDataImpl ftd) {
        boolean success = true;

        for (File file : folder.listFiles()) {
            if (file.isDirectory()) {
                if (!deleteFolder(file, continueOnFailure, ftd)) {
                    if (!continueOnFailure) {
                        return false;
                    }
                    success = false;
                }
            } else {
                if (!file.delete()) {
                    ftd.getIds().add(getId(file));
                    if (!continueOnFailure) {
                        return false;
                    }
                    success = false;
                }
            }
        }

        if (!folder.delete()) {
            ftd.getIds().add(getId(folder));
            success = false;
        }

        return success;
    }

    /**
     * CMIS updateProperties.
     */
    public ObjectData updateProperties(CallContext context, Holder<String> objectId, Properties properties,
            ObjectInfoHandler objectInfos, SesionProDoc sesion) {
        debug("updateProperties");
        boolean userReadOnly = checkUser(context, true);

        if (objectId == null || objectId.getValue() == null) {
            throw new CmisInvalidArgumentException("Id is not valid!");
        }

        // get the file or folder
        File file = getFile(objectId.getValue());
        String objId = objectId.getValue();

        if (file.isDirectory()) {
            System.out.println("Modificar Carpeta");
            // List<PropertyData<?>> propList = properties.getPropertyList();

            try {
                UpdateProDoc.modificarCarpeta(properties, objId, sesion);
            } catch (PDException e) {
                e.printStackTrace();
            }

        } else {
            System.out.println("Modificar Documento");
        }

        // get and check the new name
        String newName = FileShareUtils.getStringProperty(properties, PropertyIds.NAME);
        boolean isRename = (newName != null) && (!file.getName().equals(newName));
        if (isRename && !isValidName(newName)) {
            throw new CmisNameConstraintViolationException("Name is not valid!");
        }

        // get old properties
        PropertiesImpl oldProperties = new PropertiesImpl();
        readCustomProperties(file, oldProperties, null, new ObjectInfoImpl());

        // get the type id
        String typeId = FileShareUtils.getIdProperty(oldProperties, PropertyIds.OBJECT_TYPE_ID);
        if (typeId == null) {
            typeId = file.isDirectory() ? BaseTypeId.CMIS_FOLDER.value() : BaseTypeId.CMIS_DOCUMENT.value();
        }

        // get the creator
        String creator = FileShareUtils.getStringProperty(oldProperties, PropertyIds.CREATED_BY);
        if (creator == null) {
            creator = context.getUsername();
        }

        // get creation date
        GregorianCalendar creationDate = FileShareUtils.getDateTimeProperty(oldProperties, PropertyIds.CREATION_DATE);
        if (creationDate == null) {
            creationDate = FileShareUtils.millisToCalendar(file.lastModified());
        }

        // compile the properties
        Properties props = updateProperties(typeId, creator, creationDate, context.getUsername(), oldProperties,
                properties);

        // write properties
        writePropertiesFile(file, props);

        // rename file or folder if necessary
        File newFile = file;
        if (isRename) {
            File parent = file.getParentFile();
            File propFile = getPropertiesFile(file);
            newFile = new File(parent, newName);
            if (!file.renameTo(newFile)) {
                // if something went wrong, throw an exception
                throw new CmisUpdateConflictException("Could not rename object!");
            } else {
                // set new id
                objectId.setValue(getId(newFile));

                // if it is a file, rename properties file too
                if (newFile.isFile()) {
                    if (propFile.exists()) {
                        File newPropFile = new File(parent, newName + SHADOW_EXT);
                        if (!propFile.renameTo(newPropFile)) {
                            LOG.error("Could not rename properties file: {}", propFile.getName());
                        }
                    }
                }
            }
        }

        return compileObjectData(context, newFile, null, false, false, userReadOnly, objectInfos);
    }

    /**
     * Checks and updates a property set that can be written to disc.
     */
    private Properties updateProperties(String typeId, String creator, GregorianCalendar creationDate, String modifier,
            Properties oldProperties, Properties properties) {
        PropertiesImpl result = new PropertiesImpl();

        if (properties == null) {
            throw new CmisConstraintException("No properties!");
        }

        // get the property definitions
        TypeDefinition type = typeManager.getInternalTypeDefinition(typeId);
        if (type == null) {
            throw new CmisObjectNotFoundException("Type '" + typeId + "' is unknown!");
        }

        // copy old properties
        for (PropertyData<?> prop : oldProperties.getProperties().values()) {
            PropertyDefinition<?> propType = type.getPropertyDefinitions().get(prop.getId());

            // do we know that property?
            if (propType == null) {
                throw new CmisConstraintException("Property '" + prop.getId() + "' is unknown!");
            }

            // only add read/write properties
            if (propType.getUpdatability() != Updatability.READWRITE) {
                continue;
            }

            result.addProperty(prop);
        }

        // update properties
        for (PropertyData<?> prop : properties.getProperties().values()) {
            PropertyDefinition<?> propType = type.getPropertyDefinitions().get(prop.getId());

            // do we know that property?
            if (propType == null) {
                throw new CmisConstraintException("Property '" + prop.getId() + "' is unknown!");
            }

            // can it be set?
            if (propType.getUpdatability() == Updatability.READONLY) {
                throw new CmisConstraintException("Property '" + prop.getId() + "' is readonly!");
            }

            if (propType.getUpdatability() == Updatability.ONCREATE) {
                throw new CmisConstraintException("Property '" + prop.getId() + "' can only be set on create!");
            }

            // default or value
            if (isEmptyProperty(prop)) {
                addPropertyDefault(result, propType);
            } else {
                result.addProperty(prop);
            }
        }

        addPropertyId(result, typeId, null, PropertyIds.OBJECT_TYPE_ID, typeId);
        addPropertyString(result, typeId, null, PropertyIds.CREATED_BY, creator);
        addPropertyDateTime(result, typeId, null, PropertyIds.CREATION_DATE, creationDate);
        addPropertyString(result, typeId, null, PropertyIds.LAST_MODIFIED_BY, modifier);

        return result;
    }

    /**
     * CMIS bulkUpdateProperties.
     */
    public List<BulkUpdateObjectIdAndChangeToken> bulkUpdateProperties(CallContext context,
            List<BulkUpdateObjectIdAndChangeToken> objectIdAndChangeToken, Properties properties,
            ObjectInfoHandler objectInfos) {
        debug("bulkUpdateProperties");
        checkUser(context, true);

        if (objectIdAndChangeToken == null) {
            throw new CmisInvalidArgumentException("No object ids provided!");
        }

        List<BulkUpdateObjectIdAndChangeToken> result = new ArrayList<BulkUpdateObjectIdAndChangeToken>();

        for (BulkUpdateObjectIdAndChangeToken oid : objectIdAndChangeToken) {
            if (oid == null) {
                // ignore invalid ids
                continue;
            }
            try {
                Holder<String> oidHolder = new Holder<String>(oid.getId());
                updateProperties(context, oidHolder, properties, objectInfos, null); // TODO --> Falta SesionProDoc

                result.add(new BulkUpdateObjectIdAndChangeTokenImpl(oid.getId(), oidHolder.getValue(), null));
            } catch (CmisBaseException e) {
                // ignore exceptions - see specification
            }
        }

        return result;
    }

    /**
     * CMIS getObject.
     * 
     * @param sesProdoc
     * @throws PDException
     */
    public ObjectData getObject(CallContext context, String objectId, String versionServicesId, String filter,
            Boolean includeAllowableActions, Boolean includeAcl, ObjectInfoHandler objectInfos, SesionProDoc sesProdoc)
            throws PDException {

        LOG.debug("start getObject()");

        if (objectId == null && versionServicesId == null) {
            throw new CmisInvalidArgumentException("Object Id must be set.");
        }

        if (objectId == null) {
            objectId = versionServicesId;
        }

        PDFolders fold = null;
        PDDocs doc = null;

        // Para tratamiento de ACL y AllowableActions
        Record recObjOPD = null;
        Boolean isFolder = false;

        ObjectDataImpl objDataOut = null;
        try {
            fold = new PDFolders(sesProdoc.getMainSession());
            fold.LoadFull(objectId);
            if (fold.getParentId() != null) {
                recObjOPD = fold.getRecSum();
                isFolder = true;
                objDataOut = compileOPDProp(context, sesProdoc, filter, fold);

            } else {
                doc = new PDDocs(sesProdoc.getMainSession());
                doc.LoadFull(objectId);
                if (doc.getParentId() != null) {
                    recObjOPD = doc.getRecSum();
                    objDataOut = compileOPDProp(context, sesProdoc, filter, doc);
                }
            }

            boolean iaa = FileShareUtils.getBooleanParameter(includeAllowableActions, false);
            boolean iacl = FileShareUtils.getBooleanParameter(includeAcl, false);
            boolean userReadOnly = false;

            // TODO Realizar tratamiento
            if (iaa) {
                // // result.setAllowableActions(compileAllowableActions(file, userReadOnly));
                userReadOnly = checkUser(context, false);
                objDataOut.setAllowableActions(compileAllowableActions(sesProdoc, recObjOPD, isFolder, userReadOnly));
            }

            if (iacl) {
                // // result.setAcl(compileAcl(file));
                // // result.setIsExactAcl(true);
                // objDataOut.setAcl(compileAcl(recObjOPD, sesProdoc));
                // objDataOut.setIsExactAcl(true);
            }

            // if (context.isObjectInfoRequired()) {
            // objectInfo.setObject(result);
            // objectInfos.addObjectInfo(objectInfo);
            // }

        } catch (PDException e) {
            throw e;
        }

        LOG.debug("end getObject()");

        return objDataOut;
    }

    /**
     * CMIS getAllowableActions.
     */
    public AllowableActions getAllowableActions(CallContext context, String objectId) {
        debug("getAllowableActions");
        boolean userReadOnly = checkUser(context, false);

        File file = getFile(objectId);
        if (!file.exists()) {
            throw new CmisObjectNotFoundException("Object not found!");
        }

        return compileAllowableActions(file, userReadOnly);
    }

    /**
     * CMIS getACL.
     */
    public Acl getAcl(CallContext context, String objectId) {
        debug("getAcl");
        checkUser(context, false);

        // get the file or folder
        File file = getFile(objectId);
        if (!file.exists()) {
            throw new CmisObjectNotFoundException("Object not found!");
        }

        return compileAcl(file);
    }

    /**
     * CMIS getContentStream.
     * 
     * @throws PDException
     */
    public ContentStream getContentStream(CallContext context, String objectId, BigInteger offset, BigInteger length,
            SesionProDoc sesProdoc) throws PDException {
        debug("getContentStream");
        checkUser(context, false);

        // compile data
        ContentStreamImpl result = new ContentStreamImpl();

        // get the file
        try {
            PDDocs objDoc = new PDDocs(sesProdoc.getMainSession());
            Record recObjDoc = objDoc.LoadFull(objectId);

            PDObjDefs objDef = new PDObjDefs(sesProdoc.getMainSession());
            Record recObjDef = objDef.Load(recObjDoc.getAttr("DocType").getValue().toString());
            String valorAttr = recObjDef.getAttr("Parent").getValue().toString();

            if (!valorAttr.equals("PD_DOCS")) {
                throw new CmisStreamNotSupportedException("Not a file!");
            }

            String nombreDoc = recObjDoc.getAttr("Name").getValue().toString();
            boolean empiezaConHttp = nombreDoc.substring(0).startsWith("http");

            // Si el documento es un enlace (no tiene adjunto)
            if (empiezaConHttp) {

                Desktop enlace = Desktop.getDesktop();
                try {
                    enlace.browse(new URI(nombreDoc));
                } catch (IOException e) {
                    throw new PDException(e.getMessage());
                } catch (URISyntaxException e) {
                    throw new PDException(e.getMessage());
                }

            } else { // Si el documento tiene un adjunto

                // Obtenemos la ruta de la carpeta temporal del sistema
                String rutaTemp = System.getProperty("java.io.tmpdir");
                File destino = new File(rutaTemp + nombreDoc);

                // Obtener el Stream
                OutputStream out;
                try {
                    out = new FileOutputStream(destino);
                    objDoc.getStream(out);
                } catch (FileNotFoundException ex) {
                    throw new PDException(ex.getMessage());
                }

                InputStream stream = null;
                try {
                    stream = new BufferedInputStream(new FileInputStream(destino), 64 * 1024);
                    if (offset != null || length != null) {
                        stream = new ContentRangeInputStream(stream, offset, length);
                    }
                } catch (FileNotFoundException e) {
                    throw new CmisObjectNotFoundException(e.getMessage(), e);
                }

                if ((offset != null && offset.longValue() > 0) || length != null) {
                    result = new PartialContentStreamImpl();
                } else {
                    result = new ContentStreamImpl();
                }

                result.setFileName(recObjDoc.getAttr("Name").getValue().toString());

                PDMimeType mimetype = new PDMimeType(sesProdoc.getMainSession());
                String mimeDoc = mimetype.SolveName(recObjDoc.getAttr("Name").getValue().toString());

                result.setLength(BigInteger.valueOf(destino.length()));
                result.setMimeType(mimeDoc);
                result.setStream(stream);
            }

        } catch (PDException e1) {
            throw e1;
        }

        return result;

    }

    /**
     * CMIS getChildren.
     */
    @SuppressWarnings("unused")
    public ObjectInFolderList getChildren(CallContext context, String folderId, String filter, String orderBy,
            Boolean includeAllowableActions, Boolean includePathSegment, BigInteger maxItems, BigInteger skipCount,
            ObjectInfoHandler objectInfos, SesionProDoc sesProdoc) {
        boolean userReadOnly = checkUser(context, false);

        // split filter
        Set<String> filterCollection = FileShareUtils.splitFilter(filter);

        // set defaults if values not set
        boolean iaa = FileShareUtils.getBooleanParameter(includeAllowableActions, false);
        boolean ips = FileShareUtils.getBooleanParameter(includePathSegment, false);

        // skip and max
        int skip = skipCount == null ? 0 : skipCount.intValue();
        if (skip < 0) {
            skip = 0;
        }

        int max = maxItems == null ? Integer.MAX_VALUE : maxItems.intValue();
        if (max < 0) {
            max = Integer.MAX_VALUE;
        }
        //
        // // get the folder
        // File folder = new File("C:\\pruebaCMIS\\CmisFolderRoot");
        //
        // if (!folder.isDirectory()) {
        // throw new CmisObjectNotFoundException("Not a folder!");
        // }
        //
        // // get the children
        // List<File> children = new ArrayList<File>();
        // for (File child : folder.listFiles()) {
        // // skip hidden and shadow files
        // if (child.isHidden() || child.getName().equals(SHADOW_FOLDER) ||
        // child.getPath().endsWith(SHADOW_EXT)) {
        // continue;
        // }
        //
        // children.add(child);
        // }
        //
        // // very basic sorting
        // if (orderBy != null) {
        // boolean desc = false;
        // String queryName = orderBy;
        //
        // int commaIdx = orderBy.indexOf(',');
        // if (commaIdx > -1) {
        // queryName = orderBy.substring(0, commaIdx);
        // }
        //
        // queryName = queryName.trim();
        // if (queryName.toLowerCase(Locale.ENGLISH).endsWith(" desc")) {
        // desc = true;
        // queryName = queryName.substring(0, queryName.length() - 5).trim();
        // }
        //
        // Comparator<File> comparator = null;
        //
        // if ("cmis:name".equals(queryName)) {
        // comparator = new Comparator<File>() {
        // @Override
        // public int compare(File f1, File f2) {
        // return f1.getName().toLowerCase(Locale.ENGLISH)
        // .compareTo(f2.getName().toLowerCase(Locale.ENGLISH));
        // }
        // };
        // } else if ("cmis:creationDate".equals(queryName) ||
        // "cmis:lastModificationDate".equals(queryName)) {
        // comparator = new Comparator<File>() {
        // @Override
        // public int compare(File f1, File f2) {
        // return Long.compare(f1.lastModified(), f2.lastModified());
        // }
        // };
        // } else if ("cmis:contentStreamLength".equals(queryName)) {
        // comparator = new Comparator<File>() {
        // @Override
        // public int compare(File f1, File f2) {
        // return Long.compare(f1.length(), f2.length());
        // }
        // };
        // } else if ("cmis:objectId".equals(queryName)) {
        // comparator = new Comparator<File>() {
        // @Override
        // public int compare(File f1, File f2) {
        // try {
        // return fileToId(f1).compareTo(fileToId(f2));
        // } catch (IOException e) {
        // return 0;
        // }
        // }
        // };
        // } else if ("cmis:baseTypeId".equals(queryName)) {
        // comparator = new Comparator<File>() {
        // @Override
        // public int compare(File f1, File f2) {
        // if (f1.isDirectory() == f2.isDirectory()) {
        // return 0;
        // }
        // return f1.isDirectory() ? -1 : 1;
        // }
        // };
        // } else if ("cmis:createdBy".equals(queryName) ||
        // "cmis:lastModifiedBy".equals(queryName)) {
        // // do nothing
        // } else {
        // throw new CmisInvalidArgumentException("Cannot sort by " + queryName + ".");
        // }
        //
        // if (comparator != null) {
        // Collections.sort(children, comparator);
        // if (desc) {
        // Collections.reverse(children);
        // }
        // }
        // }
        //
        // // set object info of the the folder
        // if (context.isObjectInfoRequired()) {
        // compileObjectData(context, folder, null, false, false, userReadOnly,
        // objectInfos);
        // }

        // prepare result
        ObjectInFolderListImpl result = new ObjectInFolderListImpl();
        result.setObjects(new ArrayList<ObjectInFolderData>());
        result.setHasMoreItems(false);
        int count = 0;
        PDFolders fold = null;
        PDDocs doc = null;
        try {
            fold = new PDFolders(sesProdoc.getMainSession());
            fold.LoadFull(folderId);
            String tipo = null;
            // TODO Comprobar si hay que coger el Type del padre o el propio del objeto OPD
            if (fold.getPDId() != null) {
                // tipo = fold.getFolderType();
                String parentId = fold.getParentId();
                PDFolders foldParent = new PDFolders(sesProdoc.getMainSession());
                foldParent.LoadFull(parentId);
                tipo = foldParent.getFolderType();

            } else {
                doc = new PDDocs(sesProdoc.getMainSession());
                doc.LoadFull(folderId);
                if (doc.getPDId() != null) {
                    // tipo = doc.getDocType();
                    String parentId = doc.getParentId();
                    PDDocs docParent = new PDDocs(sesProdoc.getMainSession());
                    docParent.LoadFull(parentId);
                    tipo = docParent.getDocType();
                }
            }

            PDObjDefs od = new PDObjDefs(sesProdoc.getMainSession());
            od.Load(tipo);
            String tipoObj = od.getClassType();

            List<PDFolders> listaC = new ArrayList<>();

            // Si el objeto es una carpeta --> Obtenemos los descendientes
            if (tipoObj.toLowerCase().equals("folder")) {

                PDFolders folder = new PDFolders(sesProdoc.getMainSession());
                HashSet x = folder.getListDirectDescendList(folderId);

                for (Object id : x) {

                    PDFolders child = new PDFolders(sesProdoc.getMainSession());
                    child.Load(id.toString());

                    // TODO : ver tratamiento para todo tipo de carpetas EJ "ExampleFolder"
                    if (!child.getParentId().equals(child.getPDId())) {
                        // if (!child.getParentId().equals(child.getPDId()) &&
                        // child.getFolderType().equals("PD_FOLDERS")) {

                        listaC.add(child);
                    }
                }

                for (PDFolders child : listaC) {
                    count++;

                    if (skip > 0) {
                        skip--;
                        continue;
                    }

                    if (result.getObjects().size() >= max) {
                        result.setHasMoreItems(true);
                        continue;
                    }

                    // build and add child object
                    ObjectInFolderDataImpl objectInFolder = new ObjectInFolderDataImpl();
                    objectInFolder.setObject(compileOPDProp(context, sesProdoc, filter, child));
                    if (ips) {
                        objectInFolder.setPathSegment(child.getPDId());
                    }

                    result.getObjects().add(objectInFolder);
                }

                List<PDDocs> listaD = new ArrayList<>();

                PDDocs docAux = new PDDocs(sesProdoc.getMainSession());
                Cursor cursorDoc = docAux.getListContainedDocs(folderId);

                Record rec = docAux.getDrv().NextRec(cursorDoc);

                while (rec != null) {

                    String id = rec.getAttr("PDId").getValue().toString();
                    PDDocs docChild = new PDDocs(sesProdoc.getMainSession());
                    docChild.Load(id);

                    listaD.add(docChild);

                    rec = docAux.getDrv().NextRec(cursorDoc);
                }

                for (PDDocs childDoc : listaD) {
                    count++;

                    if (skip > 0) {
                        skip--;
                        continue;
                    }

                    if (result.getObjects().size() >= max) {
                        result.setHasMoreItems(true);
                        continue;
                    }

                    // build and add child object
                    ObjectInFolderDataImpl objectInFolder = new ObjectInFolderDataImpl();
                    objectInFolder.setObject(compileOPDProp(context, sesProdoc, filter, childDoc));
                    if (ips) {
                        objectInFolder.setPathSegment(childDoc.getPDId());
                    }

                    result.getObjects().add(objectInFolder);
                }

                result.setNumItems(BigInteger.valueOf(count));
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return result;
    }

    private ObjectDataImpl compileOPDProp(CallContext context, SesionProDoc sesProdoc, String filter, ObjPD child) {

        ObjectDataImpl objDataOut = new ObjectDataImpl();
        Record recOPD = null;

        try {

            String tipo;

            if (child instanceof PDFolders) {
                PDFolders fold = new PDFolders(sesProdoc.getMainSession());
                fold.LoadFull(((PDFolders) child).getPDId().toString());
                recOPD = fold.getRecSum();

                String folderType = recOPD.getAttr("FolderType").getValue().toString();
                if (!existObjType(folderType) && !folderType.equals("PD_FOLDERS")) {
                    ArrayList<Object> typeDefs = XmlUtil.crearObjectType(context, sesProdoc,
                            recOPD.getAttr("FolderType").getValue().toString());
                    FolderTypeDefinitionImpl typeDef = (FolderTypeDefinitionImpl) typeDefs.get(0);
                    this.typeManager.addTypeDefinition(typeDef);
                }

                tipo = "PD_FOLDERS";

            } else {
                PDDocs doc = new PDDocs(sesProdoc.getMainSession());
                doc.LoadFull(((PDDocs) child).getPDId().toString());
                recOPD = doc.getRecSum();

                // probando
                String docType = recOPD.getAttr("DocType").getValue().toString();
                if (!existObjType(docType) && !docType.equals("PD_DOCS")) {
                    ArrayList<Object> typeDefs = XmlUtil.crearObjectType(context, sesProdoc,
                            recOPD.getAttr("DocType").getValue().toString());
                    DocumentTypeDefinitionImpl typeDef = (DocumentTypeDefinitionImpl) typeDefs.get(0);
                    this.typeManager.addTypeDefinition(typeDef);
                }

                tipo = "PD_DOCS";
            }

            BindingsObjectFactory objectFactory = new BindingsObjectFactoryImpl();
            Map<String, PropertyData<?>> properties = new HashMap<String, PropertyData<?>>();
            List<String> requestedIds = FilterParser.getRequestedIdsFromFilter(filter);

            recOPD.initList();
            Attribute attr = recOPD.nextAttr();

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

                    if (FilterParser.isContainedInFilter(PropertyIds.OBJECT_TYPE_ID, requestedIds)) {

                        valorAttr = recOPD.getAttr("FolderType").getValue().toString();
                        if (valorAttr.equals("PD_FOLDERS")) {
                            valorAttr = "cmis:folder";
                        }

                        properties.put(PropertyIds.OBJECT_TYPE_ID,
                                objectFactory.createPropertyIdData(PropertyIds.OBJECT_TYPE_ID, valorAttr));
                    }

                    break;

                case "PDAutor":

                    // cmis:createdBy
                    if (FilterParser.isContainedInFilter(PropertyIds.CREATED_BY, requestedIds)) {
                        properties.put(PropertyIds.CREATED_BY,
                                objectFactory.createPropertyStringData(PropertyIds.CREATED_BY, valorAttr));
                    }

                    // cmis:lastModifiedBy
                    if (FilterParser.isContainedInFilter(PropertyIds.LAST_MODIFIED_BY, requestedIds)) {
                        properties.put(PropertyIds.LAST_MODIFIED_BY,
                                objectFactory.createPropertyStringData(PropertyIds.LAST_MODIFIED_BY, valorAttr));
                    }

                    break;

                case "PDDate":

                    if (tipo.equalsIgnoreCase("PD_FOLDERS")) {

                        GregorianCalendar cal = new GregorianCalendar();

                        if (!valorAttr.equals("")) {
                            cal.setTime((Date) attr.getValue());
                            // cmis:creationDate
                            if (FilterParser.isContainedInFilter(PropertyIds.CREATION_DATE, requestedIds)) {
                                properties.put(PropertyIds.CREATION_DATE,
                                        objectFactory.createPropertyDateTimeData(PropertyIds.CREATION_DATE, cal));
                            }

                            // cmis:lastModificationDate
                            if (FilterParser.isContainedInFilter(PropertyIds.LAST_MODIFICATION_DATE, requestedIds)) {
                                properties.put(PropertyIds.LAST_MODIFICATION_DATE, objectFactory
                                        .createPropertyDateTimeData(PropertyIds.LAST_MODIFICATION_DATE, cal));
                            }
                        }

                    }
                    break;

                case "PDId":

                    if (FilterParser.isContainedInFilter(PropertyIds.OBJECT_ID, requestedIds)) {
                        properties.put(PropertyIds.OBJECT_ID,
                                objectFactory.createPropertyIdData(PropertyIds.OBJECT_ID, valorAttr));

                        // TODO Comprobar si hay que cambiar el valor de RootFolder a @root@

                        // if(valorAttr.equals("RootFolder")) {
                        // properties.put(PropertyIds.OBJECT_ID,
                        // objectFactory.createPropertyIdData(PropertyIds.OBJECT_ID, "@root@"));
                        // }else {
                        // properties.put(PropertyIds.OBJECT_ID,
                        // objectFactory.createPropertyIdData(PropertyIds.OBJECT_ID, valorAttr));
                        // }
                    }

                    break;

                case "ParentId":

                    if (tipo.equalsIgnoreCase("PD_FOLDERS")) {

                        // cmis:parentId
                        if (FilterParser.isContainedInFilter(PropertyIds.PARENT_ID, requestedIds)) {
                            properties.put(PropertyIds.PARENT_ID, objectFactory.createPropertyIdData(
                                    PropertyIds.PARENT_ID, recOPD.getAttr("ParentId").getValue().toString()));
                        }
                    }

                    break;

                case "Title":

                    // cmis:name
                    if (FilterParser.isContainedInFilter(PropertyIds.NAME, requestedIds)) {
                        properties.put(PropertyIds.NAME,
                                objectFactory.createPropertyStringData(PropertyIds.NAME, valorAttr));
                    }

                    break;

                // Propiedades de los documentos

                case "DocDate":

                    GregorianCalendar cal = new GregorianCalendar();

                    if (!valorAttr.equals("")) {
                        cal.setTime((Date) attr.getValue());
                        // cmis:creationDate
                        if (FilterParser.isContainedInFilter(PropertyIds.CREATION_DATE, requestedIds)) {
                            properties.put(PropertyIds.CREATION_DATE,
                                    objectFactory.createPropertyDateTimeData(PropertyIds.CREATION_DATE, cal));
                        }

                        // cmis:lastModificationDate
                        if (FilterParser.isContainedInFilter(PropertyIds.LAST_MODIFICATION_DATE, requestedIds)) {
                            properties.put(PropertyIds.LAST_MODIFICATION_DATE,
                                    objectFactory.createPropertyDateTimeData(PropertyIds.LAST_MODIFICATION_DATE, cal));
                        }
                    }

                    break;

                case "DocType":

                    if (FilterParser.isContainedInFilter(PropertyIds.OBJECT_TYPE_ID, requestedIds)) {

                        valorAttr = recOPD.getAttr("DocType").getValue().toString();
                        if (valorAttr.equals("PD_DOCS")) {
                            valorAttr = "cmis:document";
                        }

                        properties.put(PropertyIds.OBJECT_TYPE_ID,
                                objectFactory.createPropertyIdData(PropertyIds.OBJECT_TYPE_ID, valorAttr));
                    }

                    break;

                case "LockedBy":
                    break;

                case "MimeType":

                    properties.put(PropertyIds.CONTENT_STREAM_MIME_TYPE,
                            objectFactory.createPropertyStringData(PropertyIds.CONTENT_STREAM_MIME_TYPE, valorAttr));
                    break;

                case "Name":

                    properties.put(PropertyIds.CONTENT_STREAM_FILE_NAME,
                            objectFactory.createPropertyStringData(PropertyIds.CONTENT_STREAM_FILE_NAME, valorAttr));
                    break;

                case "PurgeDate":
                    break;

                case "Reposit":
                    break;

                case "Status":
                    break;

                case "Version":

                    // cmis:versionLabel
                    if (FilterParser.isContainedInFilter(PropertyIds.VERSION_LABEL, requestedIds)) {
                        // attrAux = recOPD.getAttr("Version");
                        // valorAttr = attrAux.getValue().toString();
                        properties.put(PropertyIds.VERSION_LABEL,
                                objectFactory.createPropertyStringData(PropertyIds.VERSION_LABEL, valorAttr));
                    }
                    break;

                case "Note":

                    if (FilterParser.isContainedInFilter(PropertyIds.DESCRIPTION, requestedIds)) {
                        properties.put(PropertyIds.DESCRIPTION,
                                objectFactory.createPropertyStringData(PropertyIds.DESCRIPTION, valorAttr));
                    }
                    break;

                // Resto de atributos
                default:

                    // TODO Comprobar tratamiento para el resto de atributos que no sean los básicos
                    createOtherProperty(properties, recOPD, nombreAttr);

                    break;
                }

                attr = recOPD.nextAttr();
            }

            Attribute attrAux = null;
            String valorAttr = null;

            if (tipo.equalsIgnoreCase("PD_FOLDERS")) {

                // cmis:baseTypeId
                if (FilterParser.isContainedInFilter(PropertyIds.BASE_TYPE_ID, requestedIds)) {

                    PDObjDefs objDef = new PDObjDefs(sesProdoc.getMainSession());
                    Record recObjDef = objDef.Load(recOPD.getAttr("FolderType").getValue().toString());
                    valorAttr = recObjDef.getAttr("Parent").getValue().toString();

                    if (valorAttr.equals("PD_FOLDERS")) {
                        valorAttr = "cmis:folder";
                    }

                    properties.put(PropertyIds.BASE_TYPE_ID,
                            objectFactory.createPropertyStringData(PropertyIds.BASE_TYPE_ID, valorAttr));
                }

                // TODO Como tratamos este campo ??
                // // CMIS 1.1 properties
                // if (context.getCmisVersion() != CmisVersion.CMIS_1_0) {
                //
                // if (FilterParser.isContainedInFilter(PropertyIds.SECONDARY_OBJECT_TYPE_IDS,
                // requestedIds)) {
                // properties.put(PropertyIds.SECONDARY_OBJECT_TYPE_IDS,
                // objectFactory.createPropertyIdData(PropertyIds.SECONDARY_OBJECT_TYPE_IDS,
                // ""));
                // }
                // }

                // cmis:path
                if (FilterParser.isContainedInFilter(PropertyIds.PATH, requestedIds)) {

                    String path = null;
                    if (recOPD.getAttr("PDId").getValue().toString().equals("RootFolder")) {
                        path = "/";
                    } else {
                        path = ((PDFolders) child).getPathId(recOPD.getAttr("PDId").getValue().toString());
                    }

                    properties.put(PropertyIds.PATH, objectFactory.createPropertyStringData(PropertyIds.PATH, path));

                }

                // cmis:allowedChildObjectTypeIds
                if (FilterParser.isContainedInFilter(PropertyIds.ALLOWED_CHILD_OBJECT_TYPE_IDS, requestedIds)) {
                    properties.put(PropertyIds.ALLOWED_CHILD_OBJECT_TYPE_IDS, objectFactory
                            .createPropertyStringData(PropertyIds.ALLOWED_CHILD_OBJECT_TYPE_IDS, "not set"));
                }

            } else {

                if (tipo.equalsIgnoreCase("PD_DOCS")) {

                    String usuSesion = child.getDrv().getUser().getName();

                    // cmis:baseTypeId
                    if (FilterParser.isContainedInFilter(PropertyIds.BASE_TYPE_ID, requestedIds)) {

                        PDObjDefs objDef = new PDObjDefs(sesProdoc.getMainSession());
                        Record recObjDef = objDef.Load(recOPD.getAttr("DocType").getValue().toString());
                        valorAttr = recObjDef.getAttr("Parent").getValue().toString();

                        if (valorAttr.equals("PD_DOCS")) {
                            valorAttr = "cmis:document";
                        }

                        properties.put(PropertyIds.BASE_TYPE_ID,
                                objectFactory.createPropertyStringData(PropertyIds.BASE_TYPE_ID, valorAttr));
                    }

                    // cmis:isImmutable
                    if (FilterParser.isContainedInFilter(PropertyIds.IS_IMMUTABLE, requestedIds)) {
                        properties.put(PropertyIds.IS_IMMUTABLE,
                                objectFactory.createPropertyBooleanData(PropertyIds.IS_IMMUTABLE, true));
                    }

                    // cmis:isLatestVersion
                    if (FilterParser.isContainedInFilter(PropertyIds.IS_LATEST_VERSION, requestedIds)) {
                        properties.put(PropertyIds.IS_LATEST_VERSION,
                                objectFactory.createPropertyBooleanData(PropertyIds.IS_LATEST_VERSION, true));
                    }

                    // cmis:isMajorVersion
                    if (FilterParser.isContainedInFilter(PropertyIds.IS_MAJOR_VERSION, requestedIds)) {
                        properties.put(PropertyIds.IS_MAJOR_VERSION,
                                objectFactory.createPropertyBooleanData(PropertyIds.IS_MAJOR_VERSION, true));
                    }

                    // cmis:isLatestMajorVersion
                    if (FilterParser.isContainedInFilter(PropertyIds.IS_LATEST_MAJOR_VERSION, requestedIds)) {
                        properties.put(PropertyIds.IS_LATEST_MAJOR_VERSION,
                                objectFactory.createPropertyBooleanData(PropertyIds.IS_LATEST_MAJOR_VERSION, true));
                    }

                    // cmis:versionSeriesId
                    if (FilterParser.isContainedInFilter(PropertyIds.VERSION_SERIES_ID, requestedIds)) {
                        properties.put(PropertyIds.VERSION_SERIES_ID, objectFactory.createPropertyIdData(
                                PropertyIds.VERSION_SERIES_ID, recOPD.getAttr("PDId").getValue().toString()));
                    }

                    // cmis:isVersionSeriesCheckedOut
                    if (FilterParser.isContainedInFilter(PropertyIds.IS_VERSION_SERIES_CHECKED_OUT, requestedIds)) {
                        attrAux = recOPD.getAttr("LockedBy");
                        if (attrAux.getValue() != null) {
                            properties.put(PropertyIds.IS_VERSION_SERIES_CHECKED_OUT, objectFactory
                                    .createPropertyBooleanData(PropertyIds.IS_VERSION_SERIES_CHECKED_OUT, true));
                        } else {
                            properties.put(PropertyIds.IS_VERSION_SERIES_CHECKED_OUT, objectFactory
                                    .createPropertyBooleanData(PropertyIds.IS_VERSION_SERIES_CHECKED_OUT, false));
                        }
                    }

                    // cmis:versionSeriesCheckedOutBy
                    if (FilterParser.isContainedInFilter(PropertyIds.VERSION_SERIES_CHECKED_OUT_BY, requestedIds)) {
                        attrAux = recOPD.getAttr("LockedBy");
                        Object objAttr = attrAux.getValue();
                        String strValue = "";
                        if (objAttr != null) {
                            strValue = attrAux.getValue().toString();
                        }
                        properties.put(PropertyIds.VERSION_SERIES_CHECKED_OUT_BY, objectFactory
                                .createPropertyStringData(PropertyIds.VERSION_SERIES_CHECKED_OUT_BY, strValue));
                    }

                    // cmis:versionSeriesCheckedOutId
                    if (FilterParser.isContainedInFilter(PropertyIds.VERSION_SERIES_CHECKED_OUT_ID, requestedIds)) {
                        Object objAttr = recOPD.getAttr("LockedBy").getValue();
                        String strValue = "";
                        if (objAttr != null && attrAux.getValue().toString().equals(usuSesion)) {
                            strValue = attrAux.getValue().toString();
                        }
                        properties.put(PropertyIds.VERSION_SERIES_CHECKED_OUT_ID, objectFactory
                                .createPropertyStringData(PropertyIds.VERSION_SERIES_CHECKED_OUT_ID, strValue));

                        // attrAux = recOPD.getAttr("LockedBy");
                        // if (attrAux.getValue() != null &&
                        // !attrAux.getValue().toString().equals(usuSesion)) {
                        // properties.put(PropertyIds.VERSION_SERIES_CHECKED_OUT_ID, objectFactory
                        // .createPropertyStringData(PropertyIds.VERSION_SERIES_CHECKED_OUT_ID,
                        // usuSesion));
                        // } else {
                        // properties.put(PropertyIds.IS_PRIVATE_WORKING_COPY, objectFactory
                        // .createPropertyStringData(PropertyIds.VERSION_SERIES_CHECKED_OUT_ID, ""));
                        // }
                    }

                    // cmis:checkinComment
                    if (FilterParser.isContainedInFilter(PropertyIds.CHECKIN_COMMENT, requestedIds)) {
                        attrAux = recOPD.getAttr("Version");
                        valorAttr = attrAux.getValue().toString();
                        properties.put(PropertyIds.CHECKIN_COMMENT,
                                objectFactory.createPropertyStringData(PropertyIds.CHECKIN_COMMENT, valorAttr));
                    }

                    if (context.getCmisVersion() != CmisVersion.CMIS_1_0) {

                        // cmis:IsPrivateWorkingCopy
                        if (FilterParser.isContainedInFilter(PropertyIds.IS_PRIVATE_WORKING_COPY, requestedIds)) {
                            attrAux = recOPD.getAttr("LockedBy");
                            if (attrAux.getValue() != null && attrAux.getValue().toString().equals(usuSesion)) {
                                properties.put(PropertyIds.IS_PRIVATE_WORKING_COPY, objectFactory
                                        .createPropertyBooleanData(PropertyIds.IS_PRIVATE_WORKING_COPY, true));
                            } else {
                                properties.put(PropertyIds.IS_PRIVATE_WORKING_COPY, objectFactory
                                        .createPropertyBooleanData(PropertyIds.IS_PRIVATE_WORKING_COPY, false));
                            }
                        }
                    }

                    PDDocs objDoc = new PDDocs(sesProdoc.getMainSession());
                    objDoc.LoadFull(recOPD.getAttr("PDId").getValue().toString());
                    String nombreDoc = recOPD.getAttr("Name").getValue().toString();

                    boolean empiezaConHttp = nombreDoc.substring(0).startsWith("http");

                    // Si el documento es un enlace (no tiene adjunto)
                    if (empiezaConHttp) {

                        BigDecimal length = BigDecimal.valueOf(0);
                        properties.put(PropertyIds.CONTENT_STREAM_LENGTH,
                                objectFactory.createPropertyDecimalData(PropertyIds.CONTENT_STREAM_LENGTH, length));

                        properties.put(PropertyIds.CONTENT_STREAM_MIME_TYPE,
                                objectFactory.createPropertyStringData(PropertyIds.CONTENT_STREAM_MIME_TYPE, "http"));

                        properties.put(PropertyIds.CONTENT_STREAM_FILE_NAME, objectFactory
                                .createPropertyStringData(PropertyIds.CONTENT_STREAM_FILE_NAME, nombreDoc));

                    } else { // Si el documento tiene un adjunto

                        String rutaTemp = System.getProperty("java.io.tmpdir");
                        File destino = new File(rutaTemp + nombreDoc);

                        OutputStream out = null;
                        try {
                            out = new FileOutputStream(destino);
                            objDoc.getStream(out);
                        } catch (FileNotFoundException ex) {
                            throw new PDException(ex.getMessage());
                        }

                        if (destino.length() != 0) {

                            BigDecimal length = BigDecimal.valueOf(destino.length());
                            properties.put(PropertyIds.CONTENT_STREAM_LENGTH,
                                    objectFactory.createPropertyDecimalData(PropertyIds.CONTENT_STREAM_LENGTH, length));

                            properties.put(PropertyIds.CONTENT_STREAM_MIME_TYPE, objectFactory.createPropertyStringData(
                                    PropertyIds.CONTENT_STREAM_MIME_TYPE, MimeTypes.getMIMEType(destino)));

                            properties.put(PropertyIds.CONTENT_STREAM_FILE_NAME, objectFactory
                                    .createPropertyStringData(PropertyIds.CONTENT_STREAM_FILE_NAME, destino.getName()));
                        }

                        destino.delete();
                    }

                    // cmis:contentStreamId
                    if (FilterParser.isContainedInFilter(PropertyIds.CONTENT_STREAM_ID, requestedIds)) {
                        if (FilterParser.isContainedInFilter(PropertyIds.CONTENT_STREAM_ID, requestedIds)) {
                            properties.put(PropertyIds.CONTENT_STREAM_ID,
                                    objectFactory.createPropertyIdData(PropertyIds.CONTENT_STREAM_ID, "0"));
                        }
                    }

                    // TODO Error Indica que no existe la propiedad en el objeto --> Sería necesario
                    // mostrarlo
                    // // cmis:path
                    // if (FilterParser.isContainedInFilter(PropertyIds.PATH, requestedIds)) {
                    //
                    //// String path = null;
                    //// String parentId = recOPD.getAttr("ParentId").getValue().toString();
                    //// if (parentId.equals("RootFolder")) {
                    //// path = "/";
                    //// } else {
                    //// PDFolders parent = new PDFolders(sesProdoc.getMainSession());
                    //// parent.LoadFull(parentId);
                    //// path = parent.getPathId(parent.getPDId());
                    //// }
                    //
                    // properties.put(PropertyIds.PATH,
                    // objectFactory.createPropertyStringData(PropertyIds.PATH, destino.getPath()));
                    //
                    // }

                }
            }

            List<PropertyData<?>> propertiesList = new ArrayList<PropertyData<?>>(properties.values());
            Properties props = objectFactory.createPropertiesData(propertiesList);

            objDataOut.setProperties(props);

        } catch (PDException e) {
            e.printStackTrace();
        }

        return objDataOut;

    }

    private boolean existObjType(String nombreObjType) {

        boolean exist = false;
        TypeDefinition objType = this.typeManager.getInternalTypeDefinition(nombreObjType);

        if (objType != null) {
            exist = true;
        }

        return exist;
    }

    private void createOtherProperty(Map<String, PropertyData<?>> properties, Record recOPD, String nombreAttr) {

        Attribute attr = recOPD.getAttr(nombreAttr);
        BindingsObjectFactory objectFactory = new BindingsObjectFactoryImpl();

        int tipoAttr = attr.getType();

        switch (tipoAttr) {

        case tINTEGER:
            if (attr.getValue() != null) {
                BigInteger valorInt = BigInteger.valueOf((Integer) attr.getValue());
                properties.put(nombreAttr, objectFactory.createPropertyIntegerData(nombreAttr, valorInt));
            }
            
            break;

        case tFLOAT:
            if (attr.getValue() != null) {
                // MutablePropertyDecimal createPropertyDecimalData(String id, List<BigDecimal>
                // values);

                // MutablePropertyDecimal createPropertyDecimalData(String id, BigDecimal
                // value);
                BigDecimal valorFlo = BigDecimal.valueOf((Float) attr.getValue());
                properties.put(nombreAttr, objectFactory.createPropertyDecimalData(nombreAttr, valorFlo));
            }
            break;

        case tSTRING:
            if (attr.getValue() != null) {
                // MutablePropertyString createPropertyStringData(String id, List<String>
                // values);

                // MutablePropertyString createPropertyStringData(String id, String value);
                properties.put(nombreAttr,
                        objectFactory.createPropertyStringData(nombreAttr, attr.getValue().toString()));
            } else {
                properties.put(nombreAttr, objectFactory.createPropertyStringData(nombreAttr, ""));
            }
            break;

        case tDATE:
            if (attr.getValue() != null) {
            } else {
            }

            break;

        case tBOOLEAN:
            if (attr.getValue() != null) {
                // MutablePropertyBoolean createPropertyBooleanData(String id, List<Boolean>
                // values)

                // MutablePropertyBoolean createPropertyBooleanData(String id, Boolean value)

                properties.put(nombreAttr,
                        objectFactory.createPropertyBooleanData(nombreAttr, (Boolean) attr.getValue()));
            }
            break;

        case tTIMESTAMP:
            if (attr.getValue() != null) {
            }
            break;

        case tTHES:
            if (attr.getValue() != null) {
            }
            break;

        default:
            
            break;
        }

        // // MutablePropertyDateTime createPropertyDateTimeData(String id,
        // // List<GregorianCalendar> values);
        //
        // MutablePropertyDateTime createPropertyDateTimeData(String id,
        // GregorianCalendar value);
        //
        //
        // // MutablePropertyHtml createPropertyHtmlData(String id, List<String>
        // values);
        //
        // MutablePropertyHtml createPropertyHtmlData(String id, String value);
        //
        // // MutablePropertyId createPropertyIdData(String id, List<String> values);
        //
        // MutablePropertyId createPropertyIdData(String id, String value);
        //
        //
        // // MutablePropertyUri createPropertyUriData(String id, List<String> values);
        //
        // MutablePropertyUri createPropertyUriData(String id, String value);

    }

    /**
     * CMIS getDescendants.
     */
    public List<ObjectInFolderContainer> getDescendants(CallContext context, String folderId, BigInteger depth,
            String filter, Boolean includeAllowableActions, Boolean includePathSegment, ObjectInfoHandler objectInfos,
            boolean foldersOnly) {
        debug("getDescendants or getFolderTree");
        boolean userReadOnly = checkUser(context, false);

        // check depth
        int d = depth == null ? 2 : depth.intValue();
        if (d == 0) {
            throw new CmisInvalidArgumentException("Depth must not be 0!");
        }
        if (d < -1) {
            d = -1;
        }

        // split filter
        Set<String> filterCollection = FileShareUtils.splitFilter(filter);

        // set defaults if values not set
        boolean iaa = FileShareUtils.getBooleanParameter(includeAllowableActions, false);
        boolean ips = FileShareUtils.getBooleanParameter(includePathSegment, false);

        // get the folder
        File folder = getFile(folderId);
        if (!folder.isDirectory()) {
            throw new CmisObjectNotFoundException("Not a folder!");
        }

        // set object info of the the folder
        if (context.isObjectInfoRequired()) {
            compileObjectData(context, folder, null, false, false, userReadOnly, objectInfos);
        }

        // get the tree
        List<ObjectInFolderContainer> result = new ArrayList<ObjectInFolderContainer>();
        gatherDescendants(context, folder, result, foldersOnly, d, filterCollection, iaa, ips, userReadOnly,
                objectInfos);

        return result;
    }

    /**
     * Gather the children of a folder.
     */
    private void gatherDescendants(CallContext context, File folder, List<ObjectInFolderContainer> list,
            boolean foldersOnly, int depth, Set<String> filter, boolean includeAllowableActions,
            boolean includePathSegments, boolean userReadOnly, ObjectInfoHandler objectInfos) {
        assert folder != null;
        assert list != null;

        // iterate through children
        for (File child : folder.listFiles()) {
            // skip hidden and shadow files
            if (child.isHidden() || child.getName().equals(SHADOW_FOLDER) || child.getPath().endsWith(SHADOW_EXT)) {
                continue;
            }

            // folders only?
            if (foldersOnly && !child.isDirectory()) {
                continue;
            }

            // add to list
            ObjectInFolderDataImpl objectInFolder = new ObjectInFolderDataImpl();
            objectInFolder.setObject(compileObjectData(context, child, filter, includeAllowableActions, false,
                    userReadOnly, objectInfos));
            if (includePathSegments) {
                objectInFolder.setPathSegment(child.getName());
            }

            ObjectInFolderContainerImpl container = new ObjectInFolderContainerImpl();
            container.setObject(objectInFolder);

            list.add(container);

            // move to next level
            if (depth != 1 && child.isDirectory()) {
                container.setChildren(new ArrayList<ObjectInFolderContainer>());
                gatherDescendants(context, child, container.getChildren(), foldersOnly, depth - 1, filter,
                        includeAllowableActions, includePathSegments, userReadOnly, objectInfos);
            }
        }
    }

    /**
     * CMIS getFolderParent.
     * 
     * @throws PDException
     */
    public ObjectData getFolderParent(CallContext context, String folderId, String filter,
            ObjectInfoHandler objectInfos, SesionProDoc sesion) throws PDException {

        ObjectData obj = new ObjectDataImpl();

        try {

            PDFolders foldHija = new PDFolders(sesion.getMainSession());
            foldHija.LoadFull(folderId);
            String parentId = foldHija.getParentId();
            PDFolders foldParent = new PDFolders(sesion.getMainSession());
            foldParent.LoadFull(parentId);
            obj = compileOPDProp(context, sesion, filter, foldParent);

        } catch (PDException e) {
            throw e;
        }

        return obj;
    }

    /**
     * CMIS getObjectParents.
     */
    public List<ObjectParentData> getObjectParents(CallContext context, String objectId, String filter,
            Boolean includeAllowableActions, Boolean includeRelativePathSegment, ObjectInfoHandler objectInfos) {
        debug("getObjectParents");
        boolean userReadOnly = checkUser(context, false);

        // split filter
        Set<String> filterCollection = FileShareUtils.splitFilter(filter);

        // set defaults if values not set
        boolean iaa = FileShareUtils.getBooleanParameter(includeAllowableActions, false);
        boolean irps = FileShareUtils.getBooleanParameter(includeRelativePathSegment, false);

        // get the file or folder
        File file = getFile(objectId);

        // don't climb above the root folder
        if (root.equals(file)) {
            return Collections.emptyList();
        }

        // set object info of the the object
        if (context.isObjectInfoRequired()) {
            compileObjectData(context, file, null, false, false, userReadOnly, objectInfos);
        }

        // get parent folder
        File parent = file.getParentFile();
        ObjectData object = compileObjectData(context, parent, filterCollection, iaa, false, userReadOnly, objectInfos);

        ObjectParentDataImpl result = new ObjectParentDataImpl();
        result.setObject(object);
        if (irps) {
            result.setRelativePathSegment(file.getName());
        }

        return Collections.<ObjectParentData>singletonList(result);
    }

    /**
     * CMIS getObjectByPath.
     */
    public ObjectData getObjectByPath(CallContext context, String folderPath, String filter,
            boolean includeAllowableActions, boolean includeACL, ObjectInfoHandler objectInfos) {
        debug("getObjectByPath");
        boolean userReadOnly = checkUser(context, false);

        // split filter
        Set<String> filterCollection = FileShareUtils.splitFilter(filter);

        // check path
        if (folderPath == null || folderPath.length() == 0 || folderPath.charAt(0) != '/') {
            throw new CmisInvalidArgumentException("Invalid folder path!");
        }

        // get the file or folder
        File file = null;
        if (folderPath.length() == 1) {
            file = root;
        } else {
            String path = folderPath.replace('/', File.separatorChar).substring(1);
            file = new File(root, path);
        }

        if (!file.exists()) {
            throw new CmisObjectNotFoundException("Path doesn't exist.");
        }

        return compileObjectData(context, file, filterCollection, includeAllowableActions, includeACL, userReadOnly,
                objectInfos);
    }

    // --- helpers ---

    /**
     * Compiles an object type object from a file or folder.
     */
    private ObjectData compileObjectData(CallContext context, File file, Set<String> filter,
            boolean includeAllowableActions, boolean includeAcl, boolean userReadOnly, ObjectInfoHandler objectInfos) {
        ObjectDataImpl result = new ObjectDataImpl();
        ObjectInfoImpl objectInfo = new ObjectInfoImpl();

        result.setProperties(compileProperties(context, file, filter, objectInfo));

        if (includeAllowableActions) {
            result.setAllowableActions(compileAllowableActions(file, userReadOnly));
        }

        if (includeAcl) {
            result.setAcl(compileAcl(file));
            result.setIsExactAcl(true);
        }

        if (context.isObjectInfoRequired()) {
            objectInfo.setObject(result);
            objectInfos.addObjectInfo(objectInfo);
        }

        return result;
    }

    /**
     * Gathers all base properties of a file or folder.
     */
    private Properties compileProperties(CallContext context, File file, Set<String> orgfilter,
            ObjectInfoImpl objectInfo) {
        if (file == null) {
            throw new IllegalArgumentException("File must not be null!");
        }

        // we can't gather properties if the file or folder doesn't exist
        if (!file.exists()) {
            throw new CmisObjectNotFoundException("Object not found!");
        }

        // copy filter
        Set<String> filter = orgfilter == null ? null : new HashSet<String>(orgfilter);

        // find base type
        String typeId = null;

        if (file.isDirectory()) {
            typeId = BaseTypeId.CMIS_FOLDER.value();
            objectInfo.setBaseType(BaseTypeId.CMIS_FOLDER);
            objectInfo.setTypeId(typeId);
            objectInfo.setContentType(null);
            objectInfo.setFileName(null);
            objectInfo.setHasAcl(true);
            objectInfo.setHasContent(false);
            objectInfo.setVersionSeriesId(null);
            objectInfo.setIsCurrentVersion(true);
            objectInfo.setRelationshipSourceIds(null);
            objectInfo.setRelationshipTargetIds(null);
            objectInfo.setRenditionInfos(null);
            objectInfo.setSupportsDescendants(true);
            objectInfo.setSupportsFolderTree(true);
            objectInfo.setSupportsPolicies(false);
            objectInfo.setSupportsRelationships(false);
            objectInfo.setWorkingCopyId(null);
            objectInfo.setWorkingCopyOriginalId(null);
        } else {
            typeId = BaseTypeId.CMIS_DOCUMENT.value();
            objectInfo.setBaseType(BaseTypeId.CMIS_DOCUMENT);
            objectInfo.setTypeId(typeId);
            objectInfo.setHasAcl(true);
            objectInfo.setHasContent(true);
            objectInfo.setHasParent(true);
            objectInfo.setVersionSeriesId(null);
            objectInfo.setIsCurrentVersion(true);
            objectInfo.setRelationshipSourceIds(null);
            objectInfo.setRelationshipTargetIds(null);
            objectInfo.setRenditionInfos(null);
            objectInfo.setSupportsDescendants(false);
            objectInfo.setSupportsFolderTree(false);
            objectInfo.setSupportsPolicies(false);
            objectInfo.setSupportsRelationships(false);
            objectInfo.setWorkingCopyId(null);
            objectInfo.setWorkingCopyOriginalId(null);
        }

        // let's do it
        try {
            PropertiesImpl result = new PropertiesImpl();

            // id
            String id = fileToId(file);
            addPropertyId(result, typeId, filter, PropertyIds.OBJECT_ID, id);
            objectInfo.setId(id);

            // name
            String name = file.getName();
            addPropertyString(result, typeId, filter, PropertyIds.NAME, name);
            objectInfo.setName(name);

            // created and modified by
            addPropertyString(result, typeId, filter, PropertyIds.CREATED_BY, USER_UNKNOWN);
            addPropertyString(result, typeId, filter, PropertyIds.LAST_MODIFIED_BY, USER_UNKNOWN);
            objectInfo.setCreatedBy(USER_UNKNOWN);

            // creation and modification date
            GregorianCalendar lastModified = FileShareUtils.millisToCalendar(file.lastModified());
            addPropertyDateTime(result, typeId, filter, PropertyIds.CREATION_DATE, lastModified);
            addPropertyDateTime(result, typeId, filter, PropertyIds.LAST_MODIFICATION_DATE, lastModified);
            objectInfo.setCreationDate(lastModified);
            objectInfo.setLastModificationDate(lastModified);

            // change token - always null
            addPropertyString(result, typeId, filter, PropertyIds.CHANGE_TOKEN, null);

            // CMIS 1.1 properties
            if (context.getCmisVersion() != CmisVersion.CMIS_1_0) {
                addPropertyString(result, typeId, filter, PropertyIds.DESCRIPTION, null);
                addPropertyIdList(result, typeId, filter, PropertyIds.SECONDARY_OBJECT_TYPE_IDS, null);
            }

            // directory or file
            if (file.isDirectory()) {
                // base type and type name
                addPropertyId(result, typeId, filter, PropertyIds.BASE_TYPE_ID, BaseTypeId.CMIS_FOLDER.value());
                addPropertyId(result, typeId, filter, PropertyIds.OBJECT_TYPE_ID, BaseTypeId.CMIS_FOLDER.value());
                String path = getRepositoryPath(file);
                addPropertyString(result, typeId, filter, PropertyIds.PATH, path);

                // folder properties
                if (!root.equals(file)) {
                    addPropertyId(result, typeId, filter, PropertyIds.PARENT_ID,
                            (root.equals(file.getParentFile()) ? ROOT_ID : fileToId(file.getParentFile())));
                    objectInfo.setHasParent(true);
                } else {
                    addPropertyId(result, typeId, filter, PropertyIds.PARENT_ID, null);
                    objectInfo.setHasParent(false);
                }

                addPropertyIdList(result, typeId, filter, PropertyIds.ALLOWED_CHILD_OBJECT_TYPE_IDS, null);
            } else {
                // base type and type name
                addPropertyId(result, typeId, filter, PropertyIds.BASE_TYPE_ID, BaseTypeId.CMIS_DOCUMENT.value());
                addPropertyId(result, typeId, filter, PropertyIds.OBJECT_TYPE_ID, BaseTypeId.CMIS_DOCUMENT.value());

                // file properties
                addPropertyBoolean(result, typeId, filter, PropertyIds.IS_IMMUTABLE, false);
                addPropertyBoolean(result, typeId, filter, PropertyIds.IS_LATEST_VERSION, true);
                addPropertyBoolean(result, typeId, filter, PropertyIds.IS_MAJOR_VERSION, true);
                addPropertyBoolean(result, typeId, filter, PropertyIds.IS_LATEST_MAJOR_VERSION, true);
                addPropertyString(result, typeId, filter, PropertyIds.VERSION_LABEL, file.getName());
                addPropertyId(result, typeId, filter, PropertyIds.VERSION_SERIES_ID, fileToId(file));
                addPropertyBoolean(result, typeId, filter, PropertyIds.IS_VERSION_SERIES_CHECKED_OUT, false);
                addPropertyString(result, typeId, filter, PropertyIds.VERSION_SERIES_CHECKED_OUT_BY, null);
                addPropertyString(result, typeId, filter, PropertyIds.VERSION_SERIES_CHECKED_OUT_ID, null);
                addPropertyString(result, typeId, filter, PropertyIds.CHECKIN_COMMENT, "");
                if (context.getCmisVersion() != CmisVersion.CMIS_1_0) {
                    addPropertyBoolean(result, typeId, filter, PropertyIds.IS_PRIVATE_WORKING_COPY, false);
                }

                if (file.length() == 0) {
                    addPropertyBigInteger(result, typeId, filter, PropertyIds.CONTENT_STREAM_LENGTH, null);
                    addPropertyString(result, typeId, filter, PropertyIds.CONTENT_STREAM_MIME_TYPE, null);
                    addPropertyString(result, typeId, filter, PropertyIds.CONTENT_STREAM_FILE_NAME, null);

                    objectInfo.setHasContent(false);
                    objectInfo.setContentType(null);
                    objectInfo.setFileName(null);
                } else {
                    addPropertyInteger(result, typeId, filter, PropertyIds.CONTENT_STREAM_LENGTH, file.length());
                    addPropertyString(result, typeId, filter, PropertyIds.CONTENT_STREAM_MIME_TYPE,
                            MimeTypes.getMIMEType(file));
                    addPropertyString(result, typeId, filter, PropertyIds.CONTENT_STREAM_FILE_NAME, file.getName());

                    objectInfo.setHasContent(true);
                    objectInfo.setContentType(MimeTypes.getMIMEType(file));
                    objectInfo.setFileName(file.getName());
                }

                addPropertyId(result, typeId, filter, PropertyIds.CONTENT_STREAM_ID, null);
            }

            // read custom properties
            readCustomProperties(file, result, filter, objectInfo);

            if (filter != null) {
                if (!filter.isEmpty()) {
                    debug("Unknown filter properties: " + filter.toString());
                }
            }

            return result;
        } catch (CmisBaseException cbe) {
            throw cbe;
        } catch (Exception e) {
            throw new CmisRuntimeException(e.getMessage(), e);
        }
    }

    /**
     * Reads and adds properties.
     */
    private void readCustomProperties(File file, PropertiesImpl properties, Set<String> filter,
            ObjectInfoImpl objectInfo) {
        File propFile = getPropertiesFile(file);

        // if it doesn't exists, ignore it
        if (!propFile.exists()) {
            return;
        }

        // parse it
        ObjectData obj = null;
        InputStream stream = null;
        try {
            stream = new BufferedInputStream(new FileInputStream(propFile), 64 * 1024);
            XMLStreamReader parser = XMLUtils.createParser(stream);
            XMLUtils.findNextStartElemenet(parser);
            obj = XMLConverter.convertObject(parser);
            parser.close();
        } catch (Exception e) {
            LOG.warn("Unvalid CMIS properties: {}", propFile.getAbsolutePath(), e);
        } finally {
            IOUtils.closeQuietly(stream);
        }

        if (obj == null || obj.getProperties() == null) {
            return;
        }

        // add it to properties
        for (PropertyData<?> prop : obj.getProperties().getPropertyList()) {
            // overwrite object info
            if (prop instanceof PropertyString) {
                String firstValueStr = ((PropertyString) prop).getFirstValue();
                if (PropertyIds.NAME.equals(prop.getId())) {
                    objectInfo.setName(firstValueStr);
                } else if (PropertyIds.OBJECT_TYPE_ID.equals(prop.getId())) {
                    objectInfo.setTypeId(firstValueStr);
                } else if (PropertyIds.CREATED_BY.equals(prop.getId())) {
                    objectInfo.setCreatedBy(firstValueStr);
                } else if (PropertyIds.CONTENT_STREAM_MIME_TYPE.equals(prop.getId())) {
                    objectInfo.setContentType(firstValueStr);
                } else if (PropertyIds.CONTENT_STREAM_FILE_NAME.equals(prop.getId())) {
                    objectInfo.setFileName(firstValueStr);
                }
            }

            if (prop instanceof PropertyDateTime) {
                GregorianCalendar firstValueCal = ((PropertyDateTime) prop).getFirstValue();
                if (PropertyIds.CREATION_DATE.equals(prop.getId())) {
                    objectInfo.setCreationDate(firstValueCal);
                } else if (PropertyIds.LAST_MODIFICATION_DATE.equals(prop.getId())) {
                    objectInfo.setLastModificationDate(firstValueCal);
                }
            }

            // check filter
            if (filter != null) {
                if (!filter.contains(prop.getQueryName())) {
                    continue;
                } else {
                    filter.remove(prop.getQueryName());
                }
            }

            // don't overwrite id
            if (PropertyIds.OBJECT_ID.equals(prop.getId())) {
                continue;
            }

            // don't overwrite base type
            if (PropertyIds.BASE_TYPE_ID.equals(prop.getId())) {
                continue;
            }

            // add it
            properties.replaceProperty(prop);
        }
    }

    /**
     * Checks and compiles a property set that can be written to disc.
     */
    private PropertiesImpl compileWriteProperties(String typeId, String creator, String modifier,
            Properties properties) {
        PropertiesImpl result = new PropertiesImpl();
        Set<String> addedProps = new HashSet<String>();

        if (properties == null || properties.getProperties() == null) {
            throw new CmisConstraintException("No properties!");
        }

        // get the property definitions
        TypeDefinition type = typeManager.getInternalTypeDefinition(typeId);
        if (type == null) {
            throw new CmisObjectNotFoundException("Type '" + typeId + "' is unknown!");
        }

        // check if all required properties are there
        for (PropertyData<?> prop : properties.getProperties().values()) {
            PropertyDefinition<?> propType = type.getPropertyDefinitions().get(prop.getId());

            // do we know that property?
            if (propType == null) {
                throw new CmisConstraintException("Property '" + prop.getId() + "' is unknown!");
            }

            // can it be set?
            if (propType.getUpdatability() == Updatability.READONLY) {
                throw new CmisConstraintException("Property '" + prop.getId() + "' is readonly!");
            }

            // empty properties are invalid
            // TODO: check
            // if (isEmptyProperty(prop)) {
            // throw new CmisConstraintException("Property '" + prop.getId() +
            // "' must not be empty!");
            // }

            // add it
            result.addProperty(prop);
            addedProps.add(prop.getId());
        }

        // check if required properties are missing
        for (PropertyDefinition<?> propDef : type.getPropertyDefinitions().values()) {
            if (!addedProps.contains(propDef.getId()) && propDef.getUpdatability() != Updatability.READONLY) {
                if (!addPropertyDefault(result, propDef) && propDef.isRequired()) {
                    throw new CmisConstraintException("Property '" + propDef.getId() + "' is required!");
                }
            }
        }

        addPropertyId(result, typeId, null, PropertyIds.OBJECT_TYPE_ID, typeId);
        addPropertyString(result, typeId, null, PropertyIds.CREATED_BY, creator);
        addPropertyString(result, typeId, null, PropertyIds.LAST_MODIFIED_BY, modifier);

        return result;
    }

    /**
     * Writes the properties for a document or folder.
     */
    private void writePropertiesFile(File file, Properties properties) {
        File propFile = getPropertiesFile(file);

        // if no properties set delete the properties file
        if (properties == null || properties.getProperties() == null || properties.getProperties().size() == 0) {
            propFile.delete();
            return;
        }

        // create object
        ObjectDataImpl object = new ObjectDataImpl();
        object.setProperties(properties);

        OutputStream stream = null;
        try {
            stream = new BufferedOutputStream(new FileOutputStream(propFile));
            XMLStreamWriter writer = XMLUtils.createWriter(stream);
            XMLUtils.startXmlDocument(writer);
            XMLConverter.writeObject(writer, CmisVersion.CMIS_1_1, true, "object", XMLConstants.NAMESPACE_CMIS, object);
            XMLUtils.endXmlDocument(writer);
            writer.close();
        } catch (Exception e) {
            throw new CmisStorageException("Couldn't store properties!", e);
        } finally {
            IOUtils.closeQuietly(stream);
        }
    }

    private boolean isEmptyProperty(PropertyData<?> prop) {
        if (prop == null || prop.getValues() == null) {
            return true;
        }

        return prop.getValues().isEmpty();
    }

    private void addPropertyId(PropertiesImpl props, String typeId, Set<String> filter, String id, String value) {
        if (!checkAddProperty(props, typeId, filter, id)) {
            return;
        }

        props.addProperty(new PropertyIdImpl(id, value));
    }

    private void addPropertyIdList(PropertiesImpl props, String typeId, Set<String> filter, String id,
            List<String> value) {
        if (!checkAddProperty(props, typeId, filter, id)) {
            return;
        }

        props.addProperty(new PropertyIdImpl(id, value));
    }

    private void addPropertyString(PropertiesImpl props, String typeId, Set<String> filter, String id, String value) {
        if (!checkAddProperty(props, typeId, filter, id)) {
            return;
        }

        props.addProperty(new PropertyStringImpl(id, value));
    }

    private void addPropertyInteger(PropertiesImpl props, String typeId, Set<String> filter, String id, long value) {
        addPropertyBigInteger(props, typeId, filter, id, BigInteger.valueOf(value));
    }

    private void addPropertyBigInteger(PropertiesImpl props, String typeId, Set<String> filter, String id,
            BigInteger value) {
        if (!checkAddProperty(props, typeId, filter, id)) {
            return;
        }

        props.addProperty(new PropertyIntegerImpl(id, value));
    }

    private void addPropertyBoolean(PropertiesImpl props, String typeId, Set<String> filter, String id, boolean value) {
        if (!checkAddProperty(props, typeId, filter, id)) {
            return;
        }

        props.addProperty(new PropertyBooleanImpl(id, value));
    }

    private void addPropertyDateTime(PropertiesImpl props, String typeId, Set<String> filter, String id,
            GregorianCalendar value) {
        if (!checkAddProperty(props, typeId, filter, id)) {
            return;
        }

        props.addProperty(new PropertyDateTimeImpl(id, value));
    }

    private boolean checkAddProperty(Properties properties, String typeId, Set<String> filter, String id) {
        if (properties == null || properties.getProperties() == null) {
            throw new IllegalArgumentException("Properties must not be null!");
        }

        if (id == null) {
            throw new IllegalArgumentException("Id must not be null!");
        }

        TypeDefinition type = typeManager.getInternalTypeDefinition(typeId);
        if (type == null) {
            throw new IllegalArgumentException("Unknown type: " + typeId);
        }
        if (!type.getPropertyDefinitions().containsKey(id)) {
            throw new IllegalArgumentException("Unknown property: " + id);
        }

        String queryName = type.getPropertyDefinitions().get(id).getQueryName();

        if (queryName != null && filter != null) {
            if (!filter.contains(queryName)) {
                return false;
            } else {
                filter.remove(queryName);
            }
        }

        return true;
    }

    /**
     * Adds the default value of property if defined.
     */
    @SuppressWarnings("unchecked")
    private boolean addPropertyDefault(PropertiesImpl props, PropertyDefinition<?> propDef) {
        if (props == null || props.getProperties() == null) {
            throw new IllegalArgumentException("Props must not be null!");
        }

        if (propDef == null) {
            return false;
        }

        List<?> defaultValue = propDef.getDefaultValue();
        if (defaultValue != null && !defaultValue.isEmpty()) {
            switch (propDef.getPropertyType()) {
            case BOOLEAN:
                props.addProperty(new PropertyBooleanImpl(propDef.getId(), (List<Boolean>) defaultValue));
                break;
            case DATETIME:
                props.addProperty(new PropertyDateTimeImpl(propDef.getId(), (List<GregorianCalendar>) defaultValue));
                break;
            case DECIMAL:
                props.addProperty(new PropertyDecimalImpl(propDef.getId(), (List<BigDecimal>) defaultValue));
                break;
            case HTML:
                props.addProperty(new PropertyHtmlImpl(propDef.getId(), (List<String>) defaultValue));
                break;
            case ID:
                props.addProperty(new PropertyIdImpl(propDef.getId(), (List<String>) defaultValue));
                break;
            case INTEGER:
                props.addProperty(new PropertyIntegerImpl(propDef.getId(), (List<BigInteger>) defaultValue));
                break;
            case STRING:
                props.addProperty(new PropertyStringImpl(propDef.getId(), (List<String>) defaultValue));
                break;
            case URI:
                props.addProperty(new PropertyUriImpl(propDef.getId(), (List<String>) defaultValue));
                break;
            default:
                assert false;
            }

            return true;
        }

        return false;
    }

    /**
     * Compiles the allowable actions for a file or folder.
     */
    private AllowableActions compileAllowableActions(File file, boolean userReadOnly) {
        if (file == null) {
            throw new IllegalArgumentException("File must not be null!");
        }

        // we can't gather allowable actions if the file or folder doesn't exist
        if (!file.exists()) {
            throw new CmisObjectNotFoundException("Object not found!");
        }

        boolean isReadOnly = !file.canWrite();
        boolean isFolder = file.isDirectory();
        boolean isRoot = root.equals(file);

        Set<Action> aas = EnumSet.noneOf(Action.class);

        addAction(aas, Action.CAN_GET_OBJECT_PARENTS, !isRoot);
        addAction(aas, Action.CAN_GET_PROPERTIES, true);
        addAction(aas, Action.CAN_UPDATE_PROPERTIES, !userReadOnly && !isReadOnly);
        addAction(aas, Action.CAN_MOVE_OBJECT, !userReadOnly && !isRoot);
        addAction(aas, Action.CAN_DELETE_OBJECT, !userReadOnly && !isReadOnly && !isRoot);
        addAction(aas, Action.CAN_GET_ACL, true);

        if (isFolder) {
            addAction(aas, Action.CAN_GET_DESCENDANTS, true);
            addAction(aas, Action.CAN_GET_CHILDREN, true);
            addAction(aas, Action.CAN_GET_FOLDER_PARENT, !isRoot);
            addAction(aas, Action.CAN_GET_FOLDER_TREE, true);
            addAction(aas, Action.CAN_CREATE_DOCUMENT, !userReadOnly);
            addAction(aas, Action.CAN_CREATE_FOLDER, !userReadOnly);
            addAction(aas, Action.CAN_DELETE_TREE, !userReadOnly && !isReadOnly);
        } else {
            addAction(aas, Action.CAN_GET_CONTENT_STREAM, file.length() > 0);
            addAction(aas, Action.CAN_SET_CONTENT_STREAM, !userReadOnly && !isReadOnly);
            addAction(aas, Action.CAN_DELETE_CONTENT_STREAM, !userReadOnly && !isReadOnly);
            addAction(aas, Action.CAN_GET_ALL_VERSIONS, true);
        }

        AllowableActionsImpl result = new AllowableActionsImpl();
        result.setAllowableActions(aas);

        return result;
    }

    private AllowableActions compileAllowableActions(SesionProDoc sesProdoc, Record recObjOPD, boolean isFolder,
            boolean userReadOnly) throws PDException {

        // boolean isReadOnly = !file.canWrite();
        boolean isReadOnly = true; // TODO Ver como obtener este dato
        String parent = recObjOPD.getAttr("PDId").getValue().toString();
        boolean isRoot = parent.equals(ROOT_ID);

        Set<Action> aas = EnumSet.noneOf(Action.class);

        addAction(aas, Action.CAN_GET_OBJECT_PARENTS, !isRoot);
        addAction(aas, Action.CAN_GET_PROPERTIES, true);
        addAction(aas, Action.CAN_UPDATE_PROPERTIES, !userReadOnly && !isReadOnly);
        addAction(aas, Action.CAN_MOVE_OBJECT, !userReadOnly && !isRoot);
        addAction(aas, Action.CAN_DELETE_OBJECT, !userReadOnly && !isReadOnly && !isRoot);
        addAction(aas, Action.CAN_GET_ACL, true);

        if (isFolder) {
            addAction(aas, Action.CAN_GET_DESCENDANTS, true);
            addAction(aas, Action.CAN_GET_CHILDREN, true);
            addAction(aas, Action.CAN_GET_FOLDER_PARENT, !isRoot);
            addAction(aas, Action.CAN_GET_FOLDER_TREE, true);
            addAction(aas, Action.CAN_CREATE_DOCUMENT, !userReadOnly);
            addAction(aas, Action.CAN_CREATE_FOLDER, !userReadOnly);
            addAction(aas, Action.CAN_DELETE_TREE, !userReadOnly && !isReadOnly);
        } else {

            PDDocs objDoc = new PDDocs(sesProdoc.getMainSession());
            objDoc.LoadFull(recObjOPD.getAttr("PDId").getValue().toString());
            String nombreDoc = recObjOPD.getAttr("Name").getValue().toString();

            boolean empiezaConHttp = nombreDoc.substring(0).startsWith("http");

            // Si el documento es un enlace (no tiene adjunto)
            if (empiezaConHttp) {

                addAction(aas, Action.CAN_GET_CONTENT_STREAM, false);

            } else { // Si el documento tiene un adjunto

                String rutaTemp = System.getProperty("java.io.tmpdir");
                File destino = new File(rutaTemp + nombreDoc);

                OutputStream out;
                try {
                    out = new FileOutputStream(destino);
                    objDoc.getStream(out);
                } catch (FileNotFoundException ex) {
                    throw new PDException(ex.getMessage());
                }

                addAction(aas, Action.CAN_GET_CONTENT_STREAM, destino.length() > 0);
            }

            addAction(aas, Action.CAN_SET_CONTENT_STREAM, !userReadOnly && !isReadOnly);
            addAction(aas, Action.CAN_DELETE_CONTENT_STREAM, !userReadOnly && !isReadOnly);
            addAction(aas, Action.CAN_GET_ALL_VERSIONS, true);
        }

        AllowableActionsImpl result = new AllowableActionsImpl();
        result.setAllowableActions(aas);

        return result;
    }

    private void addAction(Set<Action> aas, Action action, boolean condition) {
        if (condition) {
            aas.add(action);
        }
    }

    /**
     * Compiles the ACL for a file or folder.
     */
    private Acl compileAcl(File file) {
        AccessControlListImpl result = new AccessControlListImpl();
        result.setAces(new ArrayList<Ace>());

        for (Map.Entry<String, Boolean> ue : readWriteUserMap.entrySet()) {
            // create principal
            AccessControlPrincipalDataImpl principal = new AccessControlPrincipalDataImpl(ue.getKey());

            // create ACE
            AccessControlEntryImpl entry = new AccessControlEntryImpl();
            entry.setPrincipal(principal);
            entry.setPermissions(new ArrayList<String>());
            entry.getPermissions().add(BasicPermissions.READ);
            if (!ue.getValue().booleanValue() && file.canWrite()) {
                entry.getPermissions().add(BasicPermissions.WRITE);
                entry.getPermissions().add(BasicPermissions.ALL);
            }

            entry.setDirect(true);

            // add ACE
            result.getAces().add(entry);
        }

        return result;
    }

    /**
     * Compiles the ACL for a file or folder.
     */
    private Acl compileAcl(Record recObjOPD, SesionProDoc sesProdoc) {
        AccessControlListImpl result = new AccessControlListImpl();
        result.setAces(new ArrayList<Ace>());

        // TODO Tratamiento ACL OPD a ACL CMIS
        try {
            Attribute aclObjOPD = recObjOPD.getAttr("ACL");

            PDACL aclOPD = new PDACL(sesProdoc.getMainSession());
            aclOPD.Load(aclObjOPD.getValue().toString());
            Record recACL = aclOPD.getRecord();
            System.out.println("ACL rec");
        } catch (PDException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        for (Map.Entry<String, Boolean> ue : readWriteUserMap.entrySet()) {
            // create principal
            AccessControlPrincipalDataImpl principal = new AccessControlPrincipalDataImpl(ue.getKey());

            // create ACE
            AccessControlEntryImpl entry = new AccessControlEntryImpl();
            entry.setPrincipal(principal);
            entry.setPermissions(new ArrayList<String>());
            entry.getPermissions().add(BasicPermissions.READ);
            // if (!ue.getValue().booleanValue() && file.canWrite()) {
            if (!ue.getValue().booleanValue()) {
                entry.getPermissions().add(BasicPermissions.WRITE);
                entry.getPermissions().add(BasicPermissions.ALL);
            }

            entry.setDirect(true);

            // add ACE
            result.getAces().add(entry);
        }

        return result;
    }

    /**
     * Checks if the given name is valid for a file system.
     * 
     * @param name
     *            the name to check
     * 
     * @return <code>true</code> if the name is valid, <code>false</code> otherwise
     */
    private boolean isValidName(String name) {
        if (name == null || name.length() == 0 || name.indexOf(File.separatorChar) != -1
                || name.indexOf(File.pathSeparatorChar) != -1) {
            return false;
        }

        return true;
    }

    /**
     * Checks if a folder is empty. A folder is considered as empty if no files or
     * only the shadow file reside in the folder.
     * 
     * @param folder
     *            the folder
     * 
     * @return <code>true</code> if the folder is empty.
     */
    private boolean isFolderEmpty(File folder) {
        if (!folder.isDirectory()) {
            return true;
        }

        String[] fileNames = folder.list();

        if (fileNames == null || fileNames.length == 0) {
            return true;
        }

        if (fileNames.length == 1 && fileNames[0].equals(SHADOW_FOLDER)) {
            return true;
        }

        return false;
    }

    /**
     * Checks if the user in the given context is valid for this repository and if
     * the user has the required permissions.
     */
    private boolean checkUser(CallContext context, boolean writeRequired) {
        if (context == null) {
            throw new CmisPermissionDeniedException("No user context!");
        }

        Boolean readOnly = readWriteUserMap.get(context.getUsername());
        if (readOnly == null) {
            throw new CmisPermissionDeniedException("Unknown user!");
        }

        if (readOnly.booleanValue() && writeRequired) {
            throw new CmisPermissionDeniedException("No write permission!");
        }

        return readOnly.booleanValue();
    }

    /**
     * Returns the properties file of the given file.
     */
    private File getPropertiesFile(File file) {
        if (file.isDirectory()) {
            return new File(file, SHADOW_FOLDER);
        }

        return new File(file.getAbsolutePath() + SHADOW_EXT);
    }

    /**
     * Returns the File object by id or throws an appropriate exception.
     */
    private File getFile(String id) {
        try {
            return idToFile(id);
        } catch (Exception e) {
            throw new CmisObjectNotFoundException(e.getMessage(), e);
        }
    }

    /**
     * Converts an id to a File object. A simple and insecure implementation, but
     * good enough for now.
     */
    private File idToFile(String id) throws IOException {
        if (id == null || id.length() == 0) {
            throw new CmisInvalidArgumentException("Id is not valid!");
        }

        if (id.equals(ROOT_ID)) {
            return root;
        }

        return new File(root,
                (new String(Base64.decode(id.getBytes("US-ASCII")), "UTF-8")).replace('/', File.separatorChar));

    }

    /**
     * Returns the id of a File object or throws an appropriate exception.
     */
    private String getId(File file) {
        try {
            return fileToId(file);
        } catch (Exception e) {
            throw new CmisRuntimeException(e.getMessage(), e);
        }
    }

    /**
     * Creates a File object from an id. A simple and insecure implementation, but
     * good enough for now.
     */
    private String fileToId(File file) throws IOException {
        if (file == null) {
            throw new IllegalArgumentException("File is not valid!");
        }

        if (root.equals(file)) {
            return ROOT_ID;
        }

        String path = getRepositoryPath(file);

        return Base64.encodeBytes(path.getBytes("UTF-8"));
    }

    private String getRepositoryPath(File file) {
        String path = file.getAbsolutePath().substring(root.getAbsolutePath().length()).replace(File.separatorChar,
                '/');
        if (path.length() == 0) {
            path = "/";
        } else if (path.charAt(0) != '/') {
            path = "/" + path;
        }
        return path;
    }

    private void debug(String msg) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("<{}> {}", repositoryId, msg);
        }
    }

}
