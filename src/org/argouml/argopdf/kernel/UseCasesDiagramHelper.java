package org.argouml.argopdf.kernel;

import org.argouml.uml.diagram.use_case.ui.UMLUseCaseDiagram;
import org.argouml.uml.diagram.ui.UMLDiagram;
import org.argouml.model.Model;
import org.argouml.application.helpers.ResourceLoaderWrapper;
import org.apache.log4j.Logger;

import java.util.List;
import java.util.ArrayList;
import java.awt.*;
import java.io.IOException;

import com.lowagie.text.*;
import com.lowagie.text.Font;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfPCell;

import javax.swing.*;

/**
 * Contains helper methods for processing Use Case diagrams
 *
 * @author Dmitry Churbanau
 * @version 0.1
 */
public class UseCasesDiagramHelper {

    private static final Logger LOG = Logger.getLogger(UseCasesDiagramHelper.class);

    /**
     * Returns all actors of use case diagram.
     *
     * @param diagram Use Case diagram, actors of which need to be found
     * @return all actors of use case diagram 
     */
    public static ArrayList getUseCaseActors(UMLUseCaseDiagram diagram) {
        List nodes = diagram.getNodes();
        ArrayList actors = new ArrayList();

        for (Object node : nodes) {
            if (Model.getFacade().isAActor(node)) {
                actors.add(node);
            }
        }

        return actors;
    }

    /**
     * Returns all use cases of use case diagram.
     *
     * @param diagram Use Case diagram, use cases of which need to be found
     * @return all use cases of use case diagram
     */
    public static ArrayList getUseCases(UMLUseCaseDiagram diagram) {
        List nodes = diagram.getNodes();
        ArrayList useCases = new ArrayList();

        for (Object node : nodes) {
            if (Model.getFacade().isAUseCase(node)) {
                useCases.add(node);
            }
        }

        return useCases;
    }

    /**
     * Generates uml summary info
     *
     * @param diagram current diagarm, which summary info will be generated
     * @return an instance of <i>Paragraph</i> class, which contains summary info
     */
    public static Paragraph generateSummaryInfo(UMLDiagram diagram) {

        //todo translate
        Chunk sumChunk = new Chunk("Summary");
        sumChunk.setFont(new Font(Font.HELVETICA, 18, Font.BOLD));
        sumChunk.setUnderline(1f, -1f);
        Paragraph summary = new Paragraph(sumChunk);

        try {
            PdfPTable table = new PdfPTable(2);
            table.setWidthPercentage(100);
            table.setWidths(new float[]{1f, 2f});

            //todo translate
            table.addCell(createCell("Name", 1, Color.BLUE));
            table.addCell(createCell("Documentation", 1, Color.BLUE));

            if(diagram instanceof UMLUseCaseDiagram) {
                generateUseCasesSummaryInfo(table, (UMLUseCaseDiagram)diagram);
            }

            summary.add(table);
        } catch(DocumentException ex) {
            LOG.debug(ex.getMessage());
        }

        return summary;
    }

    /**
     * Generates detailed info for uml element.
     *
     * @param element element, which detailed info should be generated
     * @return an instance if <i>Paragraph</i> class, which contains detailed info
     */
    public static Paragraph generateDetailedInfo(Object element) {
        Paragraph detailed = new Paragraph("");

        return detailed;
    }

    /**
     * Generates summary info of use case diagram.
     *
     * @param table   an instance of <i>PdfPTable</i> class, which should contain summary info
     * @param diagram an instance of current use case diagram
     */
    private static void generateUseCasesSummaryInfo(PdfPTable table, UMLUseCaseDiagram diagram) {
        ArrayList elements = UseCasesDiagramHelper.getUseCaseActors(diagram);
        elements.addAll(UseCasesDiagramHelper.getUseCases(diagram));

        for (Object elem : elements) {
            String imageName = "";
            if(Model.getFacade().isAActor(elem)) {
                imageName = "Actor";
            } else if(Model.getFacade().isAUseCase(elem)) {
                imageName = "UseCase";
            }
            table.addCell(createCell(Model.getFacade().getName(elem), 1, null, imageName));
            Object taggedValue = Model.getFacade().getTaggedValue(elem, "documentation");
            String documentation = "";
            if(taggedValue != null) {
                documentation = Model.getFacade().getValueOfTag(taggedValue);
            }
            table.addCell(createCell(documentation, 1, null));
        }

    }

    /**
     * Creates a cell of a table, which contains an image and a label.
     *
     * @param text      text of a cell
     * @param colspan   colspan of a cell
     * @param color     color of a cell
     * @param imageName name of image to insert
     * @return cell of a table
     */
    private static PdfPCell createCell(String text, int colspan, Color color, String imageName) {
            PdfPCell retCell = new PdfPCell();
            retCell.setColspan(colspan);
            retCell.addElement(createImageLabelTable(text, imageName, color));
            retCell.setHorizontalAlignment(Element.ALIGN_LEFT);

            return retCell;
    }

    /**
     * Creates a cell of a table, which contains a simple text.
     *
     * @param text    text of a cell
     * @param colspan colspan of a table
     * @param color   color of a cell
     * @return a cell of a table
     */
    private static PdfPCell createCell(String text, int colspan, Color color) {
        PdfPCell cell = new PdfPCell(new Paragraph(text));
        cell.setColspan(colspan);
        if(color != null) {
            cell.setBackgroundColor(color);
        }

        return cell;
    }

    /**
     * Creates a table, which contains image in the first cell and text in the second one.
     *
     * @param text      text of a second cell
     * @param imageName name of an image, which should be placed in the first cell
     * @param colorName color of a table, if null, will be white
     * @return a table, which contains image in the first cell and text in the second one
     */
    private static PdfPTable createImageLabelTable(String text, String imageName, Color colorName) {
        try {
            PdfPTable nestedTable = new PdfPTable(2);
            nestedTable.setWidths(new float[]{1, 7});

            ImageIcon icon = ResourceLoaderWrapper.lookupIconResource(imageName);
            PdfPCell nestCell_1 = new PdfPCell(com.lowagie.text.Image.getInstance(icon.getImage(), null), false);
            nestCell_1.setHorizontalAlignment(Element.ALIGN_LEFT);
            nestCell_1.setVerticalAlignment(Element.ALIGN_MIDDLE);
            nestCell_1.setBorder(0);
            nestedTable.addCell(nestCell_1);

            PdfPCell nestCell_2 = createCell(text, 1, null);
            nestCell_2.setHorizontalAlignment(Element.ALIGN_LEFT);
            nestCell_2.setVerticalAlignment(Element.ALIGN_MIDDLE);
            nestCell_2.setBorder(0);
            nestedTable.addCell(nestCell_2);

            return nestedTable;
        } catch(DocumentException ex) {
            LOG.debug(ex.getMessage());
        } catch(IOException ex) {
            LOG.debug(ex.getMessage());
        }

        return null;
    }

}
