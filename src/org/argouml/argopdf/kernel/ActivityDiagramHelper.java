package org.argouml.argopdf.kernel;

import org.apache.log4j.Logger;
import org.argouml.uml.diagram.activity.ui.UMLActivityDiagram;
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
     */
    public static void generateDiagramInfo(Document document, Section section, UMLActivityDiagram diagram) {
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
        if(im != null) {
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
