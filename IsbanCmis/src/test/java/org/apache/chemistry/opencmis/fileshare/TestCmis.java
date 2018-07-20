package org.apache.chemistry.opencmis.fileshare;

import java.io.ByteArrayInputStream;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;

import org.apache.chemistry.opencmis.client.api.CmisObject;
import org.apache.chemistry.opencmis.client.api.Document;
import org.apache.chemistry.opencmis.client.api.Folder;
import org.apache.chemistry.opencmis.client.api.Session;
import org.apache.chemistry.opencmis.client.api.SessionFactory;
import org.apache.chemistry.opencmis.client.runtime.SessionFactoryImpl;
import org.apache.chemistry.opencmis.commons.PropertyIds;
import org.apache.chemistry.opencmis.commons.SessionParameter;
import org.apache.chemistry.opencmis.commons.data.ContentStream;
import org.apache.chemistry.opencmis.commons.enums.BindingType;
import org.apache.chemistry.opencmis.commons.enums.VersioningState;



public class TestCmis {
	private static final String TEST_TXT = "test.txt";
	static Session session = null;
	final static String carpetaPadre = "Pruebas201807_20";
	final static String nombreDoc = "PruebaDocCustomCarpeta_201807_20";
	final static String nombreTipo = "PruebaDavidFullVer";
	final static String mime = "text/plain";

	@SuppressWarnings("serial")
	private static final Map<String, Object> listaAtrib = new HashMap<String, Object>() {

		{
			put("AT_CADENA_FIJA", "Fija_201807_20");
			put("AT_CADENA_VARIA", "Variable_201807_20");
			
		}
	};

	public static void main(String[] args) {
		try {
			crearSesion();
			crearDocumento();
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	private static void crearDocumento() throws UnsupportedEncodingException {

		// recuperamos la carpeta
		Folder parent = getFolder(session.getRootFolder());

		String textFileName = TEST_TXT;

		// prepare content - a simple text file
		String content = "Se ha insertado el archivo con nombre: " + nombreDoc;

		String filename = textFileName;
		String mimetype = mime;

		byte[] contentBytes = content.getBytes("UTF-8");
		ByteArrayInputStream stream = new ByteArrayInputStream(contentBytes);

		ContentStream contentStream = session.getObjectFactory().createContentStream(filename, contentBytes.length,
				mimetype, stream);

		// prepare properties
		Map<String, Object> properties = new HashMap<String, Object>();
		properties.put(PropertyIds.NAME, nombreDoc);
		properties.put(PropertyIds.OBJECT_TYPE_ID, nombreTipo);
		setMoreProperties(properties,listaAtrib);
		// create the document
		Document newDoc = parent.createDocument(properties, contentStream, VersioningState.NONE);
		System.out.println("Fin del test, se ha creado un documento con el ID: "+newDoc.getId()+" y Nombre: "+newDoc.getName()+" dentro de la carpeta "+parent.getName());

	}

	private static void setMoreProperties(Map<String, Object> properties, Map<String, Object> listaAtrib) {
		for(String key : listaAtrib.keySet()) {
			properties.put(key, listaAtrib.get(key));
		}
		
	}

	private static Folder getFolder(Folder parent) {
		for (CmisObject child : parent.getChildren()) {
			if (child instanceof Folder) {
				Folder childFolder = (Folder) child;
				if (child.getName().equals(carpetaPadre)) {
					parent = childFolder;
					break;
				} else {
					getFolder(childFolder);
				}
			}
		}
		return parent;
	}

	private static void crearSesion() {
		// default factory implementation
		SessionFactory factory = SessionFactoryImpl.newInstance();
		Map<String, String> parameters = new HashMap<String, String>();

		// user credentials
		parameters.put(SessionParameter.USER, "root");
		parameters.put(SessionParameter.PASSWORD, "root");

		// connection settings
		parameters.put(SessionParameter.BROWSER_URL,
				"http://localhost:8080/chemistry-opencmis-server-fileshare/browser");
		parameters.put(SessionParameter.BINDING_TYPE, BindingType.BROWSER.value());
		parameters.put(SessionParameter.REPOSITORY_ID, "test");

		// create session
		session = factory.createSession(parameters);

	}

}
