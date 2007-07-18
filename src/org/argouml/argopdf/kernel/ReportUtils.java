// $Id$
// Copyright (c) 2007 The Regents of the University of California. All
// Rights Reserved. Permission to use, copy, modify, and distribute this
// software and its documentation without fee, and without a written
// agreement is hereby granted, provided that the above copyright notice
// and this paragraph appear in all copies. This software program and
// documentation are copyrighted by The Regents of the University of
// California. The software program and documentation are supplied "AS
// IS", without any accompanying services from The Regents. The Regents
// does not warrant that the operation of the program will be
// uninterrupted or error-free. The end-user understands that the program
// was developed for research purposes and is advised not to rely
// exclusively on the program for any reason. IN NO EVENT SHALL THE
// UNIVERSITY OF CALIFORNIA BE LIABLE TO ANY PARTY FOR DIRECT, INDIRECT,
// SPECIAL, INCIDENTAL, OR CONSEQUENTIAL DAMAGES, INCLUDING LOST PROFITS,
// ARISING OUT OF THE USE OF THIS SOFTWARE AND ITS DOCUMENTATION, EVEN IF
// THE UNIVERSITY OF CALIFORNIA HAS BEEN ADVISED OF THE POSSIBILITY OF
// SUCH DAMAGE. THE UNIVERSITY OF CALIFORNIA SPECIFICALLY DISCLAIMS ANY
// WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
// MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE. THE SOFTWARE
// PROVIDED HEREUNDER IS ON AN "AS IS" BASIS, AND THE UNIVERSITY OF
// CALIFORNIA HAS NO OBLIGATIONS TO PROVIDE MAINTENANCE, SUPPORT,
// UPDATES, ENHANCEMENTS, OR MODIFICATIONS.
package org.argouml.argopdf.kernel;

import org.argouml.uml.diagram.ArgoDiagram;
import org.argouml.uml.ui.SaveGraphicsManager;
import org.argouml.ui.targetmanager.TargetManager;
import org.argouml.configuration.Configuration;
import org.tigris.gef.base.SaveGraphicsAction;
import org.apache.log4j.Logger;
import com.lowagie.text.Image;
import com.lowagie.text.Document;
import com.lowagie.text.pdf.codec.PngImage;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 * Contains helper methods for report generation
 *
 * @author Dmitry Churbanau
 * @version 0.1
 */
public class ReportUtils {

    private static final Logger LOG = Logger.getLogger(ReportUtils.class);

    /**
     * Creates an image of the diagram, which goes as parameter
     *
     * @param diagram an instance of <i>ArgoDiagram</i> class, which image needs to be generated
     * @return an instance of <i>Image</i> class, which contains an image of diagram
     */
    public static Image makeImageOfDiagram(ArgoDiagram diagram) {
        TargetManager tm = TargetManager.getInstance();
        tm.setTarget(diagram);

        SaveGraphicsAction cmd = SaveGraphicsManager.getInstance().getSaveActionBySuffix("png");
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        cmd.setStream(outputStream);
        cmd.setScale(Configuration.getInteger(SaveGraphicsManager.KEY_GRAPHICS_RESOLUTION, 1));
        cmd.actionPerformed(null);

        Image im = null;
        try {
            im = PngImage.getImage(outputStream.toByteArray());
        } catch(IOException ex) {
            LOG.debug(ex.getMessage());
        }

        return im;
    }

    /**
     * Changes the image size, that it can be placed on one page
     *
     * @param image image, which size needs to change
     * @param document current document
     */
    public static void adjustImageSizeToDocumentPageSize(Image image, Document document) {
        if(image == null || document == null) return;

        float imageWidth = document.getPageSize().width() - document.rightMargin() - document.leftMargin();
        float imageHeight = document.getPageSize().height() - document.topMargin() - document.bottomMargin();

        if(image.height() > imageHeight || image.width() > imageWidth) {
            float coeff1 = imageWidth / image.width();
            float coeff2 = imageHeight / image.height();

            if(coeff2 < coeff1) {
                coeff1 = coeff2;
            }

            image.scaleAbsolute(image.width() * coeff1, image.height() * coeff1);
        }
    }

}
