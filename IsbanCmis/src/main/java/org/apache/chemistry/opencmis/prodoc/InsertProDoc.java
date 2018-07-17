package org.apache.chemistry.opencmis.prodoc;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.apache.chemistry.opencmis.commons.data.ContentStream;
import org.apache.chemistry.opencmis.commons.data.Properties;
import org.apache.chemistry.opencmis.commons.data.PropertyData;

import prodoc.PDDocs;
import prodoc.PDException;
import prodoc.PDFolders;
import prodoc.PDMimeType;

/**
 * 
 * @author Viewnext:Sergio Rodriguez Oyola
 *
 */
public class InsertProDoc {

    public InsertProDoc() {

    }

    /**
     * Método para crear carpetas
     * 
     * @param properties
     * @param sesion
     * @param parentId
     * @return Id de la carpeta creada
     */
    public static String crearCarpeta(Properties properties, SesionProDoc sesion, String parentId) { 
        PDFolders folder = null;

        try {
            PropertyData<?> nombre = properties.getProperties().get("cmis:name");

            folder = new PDFolders(sesion.getMainSession());
            folder.setTitle(nombre.getValues().get(0).toString());
            if (parentId.equals("@root@")) {
                folder.setParentId("RootFolder");
            } else {
                folder.setParentId(parentId);
            }
            
            folder.insert();

        } catch (PDException e) {
            e.printStackTrace();
        }

        return folder.getPDId();
    }

    /**
     * Método para crear documentos
     * 
     * @param properties
     * @param sesion
     * @param parentId
     * @param contentStream
     * @return Id del documento creado
     */
    public static String crearDocumento(Properties properties, SesionProDoc sesion, String parentId,
            ContentStream contentStream, String objId) { //TODO --> ELIMINAR objId al terminar las pruebas
        PDDocs doc = null;
        try {
            PropertyData<?> nombre = properties.getProperties().get("cmis:name");

            doc = new PDDocs(sesion.getMainSession(), "PD_DOCS"); // TODO --> Modificar - Obtener el DocType
            doc.setTitle(nombre.getValues().get(0).toString());
            if (parentId.equals("@root@")) {
                doc.setParentId("RootFolder");
            } else {
                doc.setParentId(parentId);
            }

            doc.setPDId(objId); // TODO --> ELIMINAR CUANDO SE TERMINEN LAS PRUEBAS
            
            // doc.setFile("C:\\pruebas\\API_disruptiva\\DocCrearDoc.txt");

            // String resultStreamCont = getStreamContents(contentStream.getStream());

            InputStream stream = contentStream.getStream();
            doc.setStream(stream);
            PDMimeType mimetype = new PDMimeType(sesion.getMainSession());
            String mimeOPD = mimetype.SolveName(contentStream.getFileName());
            doc.setMimeType(mimeOPD);
            doc.setName(contentStream.getFileName());

            doc.insert();

        } catch (PDException e) {
            e.printStackTrace();
        }

        return doc.getPDId();
    }

    public static String getStreamContents(InputStream in) throws IOException {

        // InputStream in2 = new FileInputStream(new
        // File("C:\\pruebas\\API_disruptiva\\DocCrearDoc.txt"));

        BufferedReader reader = new BufferedReader(new InputStreamReader(in));
        StringBuilder out = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            out.append(line);
        }
        System.out.println(out.toString()); // Prints the string content read from input stream
        reader.close();

        return out.toString();
    }

}
