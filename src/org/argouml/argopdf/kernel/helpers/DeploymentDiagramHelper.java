package org.argouml.argopdf.kernel.helpers;

import org.apache.log4j.Logger;
import org.argouml.uml.diagram.deployment.ui.UMLDeploymentDiagram;
import org.argouml.argopdf.kernel.ReportUtils;
import org.argouml.argopdf.kernel.PdfReport;
import com.lowagie.text.*;

/**
 * Contains helper methods for processing deployment diagrams
 *
 * @author Dmitry Churbanau
 * @version 0.1
 */
public class DeploymentDiagramHelper {

    private static final Logger LOG = Logger.getLogger(DeploymentDiagramHelper.class);

    /**
     * Generates info of the deployment diagram
     *
     * @param document current document instance
     * @param section  an instance of <i>Section</i> class, where diagram info will be situated
     * @param diagram  an instance of <i>UMLDeploymentDiagram</i> class, which info will be generated
     * @param generateDiagrams defines, whether diagram image should be generated
     */
    public static void generateDiagramInfo(Document document, Section section, UMLDeploymentDiagram diagram, boolean generateDiagrams) {
        if(diagram == null) return;
        LOG.debug("generate info of activity diagram: " + diagram.getName());

        boolean addSection = false;
        String name = ReportUtils.getElementName(diagram);
        if(section != null) {
            Paragraph title = ReportUtils.generateTitle(name, 1, false);
            section = section.addSection(title, section.depth() + 1);
        } else {
            addSection = true;
            section = PdfReport.generateNewChapter(name, false);
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
