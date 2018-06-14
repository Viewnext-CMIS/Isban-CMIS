package org.apache.chemistry.opencmis.prodoc;

import java.util.Iterator;
import java.util.List;

import org.apache.chemistry.opencmis.commons.data.Properties;
import org.apache.chemistry.opencmis.commons.data.PropertyData;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.PropertyStringImpl;
import org.apache.chemistry.opencmis.isbanutil.QueryUtil;

import prodoc.Attribute;
import prodoc.PDException;
import prodoc.PDFolders;
import prodoc.Record;

/**
 * 
 * @author Viewnext:Sergio Rodriguez Oyola
 *
 */
public class UpdateProDoc {

    public UpdateProDoc() {
        super();
    }

    public static void modificarCarpeta(Properties properties, String objectId, SesionProDoc sesion)
            throws PDException {

        PDFolders folder = new PDFolders(sesion.getMainSession());
        Record recFolder = folder.Load(objectId);

        List<PropertyData<?>> propList = properties.getPropertyList();
        Iterator it = propList.iterator();

        while (it.hasNext()) {

            PropertyStringImpl objIt = (PropertyStringImpl) it.next();
            String campo = QueryUtil.traduccionCmis(objIt.getId().toString());
            String strValorCampo = objIt.getValues().get(0).toString();

            Attribute attr = recFolder.getAttr(campo);
            if (attr != null) {
                attr.setValue(strValorCampo);
            }
        }

        folder.assignValues(recFolder);
        folder.update();
    }
}
