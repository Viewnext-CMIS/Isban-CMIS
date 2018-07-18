package org.apache.chemistry.opencmis.prodoc;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import javax.activation.MimeType;
import javax.activation.MimetypesFileTypeMap;

import org.apache.chemistry.opencmis.commons.data.ContentStream;
import org.apache.chemistry.opencmis.commons.data.Properties;
import org.apache.chemistry.opencmis.commons.data.PropertyData;

import prodoc.PDDocs;
import prodoc.PDException;
import prodoc.PDFolders;
import prodoc.PDMimeType;
import prodoc.Record;

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
     * @param typeId 
     * @param contentStream
     * @return Id del documento creado
     */
    public static String crearDocumento(Properties properties, SesionProDoc sesion, String parentId,
            String typeId, ContentStream contentStream) {
        PDDocs doc = null;
        try {
            PropertyData<?> nombre = properties.getProperties().get("cmis:name");
            if(typeId.equalsIgnoreCase("cmis:document")) {
            	typeId="PD_DOCS";
            }
           
            doc = new PDDocs(sesion.getMainSession(), typeId); 

            doc.setTitle(nombre.getValues().get(0).toString());
            if (parentId.equals("@root@")) {
                doc.setParentId("RootFolder");
            } else {
            	doc.setParentId(parentId);
            }

            InputStream stream = contentStream.getStream();
            doc.setStream(stream);
            String nomAdj=contentStream.getFileName();
            String ext =nomAdj.substring(nomAdj.indexOf(".")+1).toLowerCase();
            
            doc.setMimeType(ext);
            doc.setName(nomAdj);
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
