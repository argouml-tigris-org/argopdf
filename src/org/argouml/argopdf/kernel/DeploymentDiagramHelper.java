package org.argouml.argopdf.kernel;

import org.apache.log4j.Logger;
import org.argouml.uml.diagram.deployment.ui.UMLDeploymentDiagram;
import com.lowagie.text.*;

/**
 * Contains helper methods for processing deployment diagrams
 *
 * @author Dima
 * @version 1.0
 */
public class DeploymentDiagramHelper {

     private static final Logger LOG = Logger.getLogger(DeploymentDiagramHelper .class);

    /**
     * Generates info of the sequence diagram
     *
     * @param document current document instance
     * @param section  an instance of <i>Section</i> class, where diagram info will be situated
     * @param diagram  an instance of <i>UMLActivityDiagram</i> class, which info will be generated
     * @param generateDiagrams defines, whether diagram image should be generated
     */
    public static void generateDiagramInfo(Document document, Section section, UMLDeploymentDiagram diagram, boolean generateDiagrams) {
        if(diagram == null) return;
        LOG.debug("generate info of deployment diagram: " + diagram.getName());

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