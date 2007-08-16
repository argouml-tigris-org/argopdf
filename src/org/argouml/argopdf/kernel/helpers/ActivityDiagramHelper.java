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
package org.argouml.argopdf.kernel.helpers;

import org.apache.log4j.Logger;
import org.argouml.uml.diagram.activity.ui.UMLActivityDiagram;
import org.argouml.argopdf.kernel.PdfReport;
import org.argouml.argopdf.kernel.ReportUtils;
import com.lowagie.text.*;

/**
 * Contains helper methods for processing activity diagrams
 *
 * @author Dmitry Churbanau
 * @version 0.1
 */
public class ActivityDiagramHelper {

    private static final Logger LOG = Logger.getLogger(ActivityDiagramHelper .class);

    /**
     * Generates info of the sequence diagram
     *
     * @param document current document instance
     * @param section  an instance of <i>Section</i> class, where diagram info will be situated
     * @param diagram  an instance of <i>UMLActivityDiagram</i> class, which info will be generated
     * @param generateDiagrams defines, whether diagram image should be generated
     */
    public static void generateDiagramInfo(Document document, Section section, UMLActivityDiagram diagram, boolean generateDiagrams) {
        if(diagram == null) return;
        LOG.debug("generate info of activity diagram: " + diagram.getName());

        boolean addSection = false;
        if(section != null) {
            Paragraph title = ReportUtils.generateTitle(ReportUtils.getElementName(diagram), 1, false);
            section = section.addSection(title, section.depth() + 1);
        } else {
            addSection = true;
            section = PdfReport.generateNewChapter(ReportUtils.getElementName(diagram), false);
        }

        Image im = ReportUtils.makeImageOfDiagram(diagram);
        if(im != null && generateDiagrams) {
            ReportUtils.adjustImageSizeToDocumentPageSize(im,  document);
            section.add(Chunk.NEWLINE);
            section.add(new Chunk(im, 0, 0, true));
            section.add(Chunk.NEWLINE);

            section.add(Chunk.NEWPAGE);
        }

        if(addSection) {
            try {
                document.add(section);
            } catch(DocumentException ex) {
                LOG.debug(ex.getMessage());
            }
        }
    }
}