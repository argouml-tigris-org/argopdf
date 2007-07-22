package org.argouml.argopdf.kernel;

import org.argouml.uml.diagram.use_case.ui.UMLUseCaseDiagram;
import org.argouml.uml.diagram.ui.UMLDiagram;
import org.argouml.model.Model;
import org.argouml.application.helpers.ResourceLoaderWrapper;
import org.argouml.i18n.Translator;
import org.apache.log4j.Logger;

import java.util.List;
import java.util.ArrayList;
import java.util.Collection;
import java.awt.*;
import java.io.IOException;

import com.lowagie.text.*;
import com.lowagie.text.Font;
import com.lowagie.text.pdf.PdfPTable;

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

        Chunk sumChunk = new Chunk(Translator.localize("argopdf.report.part.usecase.summary.title"));
        sumChunk.setFont(new Font(Font.HELVETICA, 18, Font.BOLD));
        sumChunk.setUnderline(1f, -1f);
        Paragraph summary = new Paragraph(sumChunk);

        if(diagram instanceof UMLUseCaseDiagram) {
            summary.add(generateUseCasesSummaryInfo((UMLUseCaseDiagram)diagram));
        }

        return summary;
    }

    /**
     * Generates detailed info for uml element.
     *
     * @param diagram diagram, which elements need to generate detailed info
     * @return an instance if <i>Paragraph</i> class, which contains detailed info
     */
    public static Paragraph generateDetailedInfo(UMLDiagram diagram) {

        Chunk sumChunk = new Chunk(Translator.localize("argopdf.report.part.usecase.details.title"));
        sumChunk.setFont(new Font(Font.HELVETICA, 18, Font.BOLD));
        sumChunk.setUnderline(1f, -1f);
        Paragraph detailed = new Paragraph(Chunk.NEWLINE);
        detailed.add(sumChunk);
        detailed.add(Chunk.NEWLINE);

        if(diagram instanceof UMLUseCaseDiagram) {
            detailed.add(generateUseCaseDetailedInfo((UMLUseCaseDiagram)diagram));
        }

        return detailed;
    }

    /**
     * Generates summary info of use case diagram.
     *
     * @param diagram an instance of current use case diagram
     * @return an instance of <i>PdfPTable</i> class with summary info
     */
    private static PdfPTable generateUseCasesSummaryInfo(UMLUseCaseDiagram diagram) {
        ArrayList elements = UseCasesDiagramHelper.getUseCaseActors(diagram);
        elements.addAll(UseCasesDiagramHelper.getUseCases(diagram));

        return generatesElementsInfo(elements);
    }

    /**
     * Generates info of elements. Info is presented in a table, with name and documentation columns.
     *
     * @return and instance of <i>PdfPTable</i> class, which contains elements info.
     */
    private static PdfPTable generatesElementsInfo(ArrayList elements) {
        PdfPTable table = new PdfPTable(2);
        table.setWidthPercentage(100);
        try {
            table.setWidths(new float[]{1f, 2f});

            Font captionFont = new Font(Font.HELVETICA, 12, Font.BOLD, Color.WHITE);
            table.addCell(ReportUtils.createCell(Translator.localize("argopdf.report.part.usecase.info.table.name"),
                                                 1, ReportUtils.TABLE_HEADER_COLOR, captionFont));
            table.addCell(ReportUtils.createCell(Translator.localize("argopdf.report.part.usecase.info.table.documentation"),
                                                 1, ReportUtils.TABLE_HEADER_COLOR, captionFont));

            for (Object elem : elements) {
                String imageName = "";
                if(Model.getFacade().isAActor(elem)) {
                    imageName = "Actor";
                } else if(Model.getFacade().isAUseCase(elem)) {
                    imageName = "UseCase";
                } else {
                    //displays only Actors and Use Cases
                    continue;
                }
                table.addCell(ReportUtils.createCell(Model.getFacade().getName(elem), 1, null, imageName, null));
                Object taggedValue = Model.getFacade().getTaggedValue(elem, "documentation");
                String documentation = "";
                if(taggedValue != null) {
                    documentation = Model.getFacade().getValueOfTag(taggedValue);
                }
                table.addCell(ReportUtils.createCell(documentation, 1, null, null));
            }

        } catch(DocumentException ex) {
            LOG.debug(ex.getMessage());
        }

        return table;
    }

    /**
     * Generates detailed info of use case diagram
     *
     * @param diagram use case diagram, detailed info of which will be generated
     * @return an instance of <i>Paragraph</i> class, which contains detailed info of use case diagram
     */
    private static Paragraph generateUseCaseDetailedInfo(UMLUseCaseDiagram diagram) {
        ArrayList elements = UseCasesDiagramHelper.getUseCaseActors(diagram);
        elements.addAll(UseCasesDiagramHelper.getUseCases(diagram));

        Paragraph details = new Paragraph("");
        try {

            for (Object elem : elements) {
                String imageName = "";
                if(Model.getFacade().isAActor(elem)) {
                    imageName = "Actor";
                } else if(Model.getFacade().isAUseCase(elem)) {
                    imageName = "UseCase";
                } else {
                    //displays only Actors and Use Cases
                    continue;
                }

                ImageIcon icon = ResourceLoaderWrapper.lookupIconResource(imageName);
                com.lowagie.text.Image im = com.lowagie.text.Image.getInstance(icon.getImage(), null);
                im.setAlignment(com.lowagie.text.Image.ALIGN_BOTTOM);

                details.add(Chunk.NEWLINE);
                details.add(new Chunk(im, 0, 0, true));
                details.add(" " + (Model.getFacade().getName(elem) == null ? "" : Model.getFacade().getName(elem)));
                details.add(Chunk.NEWLINE);

                details.add(ReportUtils.createModifiersInfo(elem));

                if(Model.getFacade().isAActor(elem)) {

                    Paragraph parentsInfo = generatesParentsInfo(elem);
                    if(parentsInfo != null) {
                        details.add(Chunk.NEWLINE);
                        details.add(parentsInfo);
                    }

                    Paragraph childsInfo = generatesChildsInfo(elem);
                    if(childsInfo != null) {
                        details.add(Chunk.NEWLINE);
                        details.add(childsInfo);
                    }

/*
                    Collection general = Model.getFacade().getGeneralizations(elem);
                    System.out.println("UseCasesDiagramHelper.generateUseCaseDetailedInfo general = '"+general+"'");

                    for(Object el : general) {
                        Object parent = Model.getFacade().getParent(el);
                        Object child = Model.getFacade().getChild(el);
                        System.out.println("UseCasesDiagramHelper.generateUseCaseDetailedInfo parent = '"+parent+"'");
                        System.out.println("UseCasesDiagramHelper.generateUseCaseDetailedInfo child = '"+child+"'");
                    }
*/
                }

                details.add(Chunk.NEWLINE);
            }

        } catch(IOException ex) {
            LOG.debug(ex.getMessage());
        } catch(DocumentException ex) {
            LOG.debug(ex.getMessage());
        }

        return details;
    }

    /**
     * Generates parents info of an Actor
     *
     * @param actor an instance of <i>Actor</i> class, which parents info will be generated
     * @return an instance of <i>Paragraph</i> class, which contains parents info.
     *         If actor does not have parents, returns null.
     */
    private static Paragraph generatesParentsInfo(Object actor) {
        if(Model.getFacade().isAActor(actor)) {

            Collection general = Model.getFacade().getGeneralizations(actor);
            ArrayList parents = new ArrayList();
            for(Object el : general) {
                Object parent = Model.getFacade().getParent(el);
                parents.add(parent);
            }

            if(parents.size() > 0) {
                Chunk parentsTitle = new Chunk(Translator.localize("argopdf.report.part.usecase.parents.title"));
                parentsTitle.setFont(new Font(Font.HELVETICA, 16, Font.BOLD));
                //parentsTitle.setUnderline(1f, -1f);
                Paragraph parentsInfo = new Paragraph(parentsTitle);
                parentsInfo.add(Chunk.NEWLINE);

                parentsInfo.add(generatesElementsInfo(parents));

                return parentsInfo;
            }

        }

        return null;
    }

    /**
     * Generates childs info of an Actor.
     *
     * @param actor an instance of <i>Actor</i> class, which childs info will be generated
     * @return an instance of <i>Paragraph</i> class, which contains childs info.
     *         If actor does not have childs, returns null.
     */
    private static Paragraph generatesChildsInfo(Object actor) {
        if(Model.getFacade().isAActor(actor)) {

            Collection general = Model.getFacade().getSpecializations(actor);
            ArrayList childs = new ArrayList();
            for(Object el : general) {
                Object parent = Model.getFacade().getChild(el);
                childs.add(parent);
            }

            if(childs.size() > 0) {
                Chunk parentsTitle = new Chunk(Translator.localize("argopdf.report.part.usecase.childs.title"));
                parentsTitle.setFont(new Font(Font.HELVETICA, 16, Font.BOLD));
                //parentsTitle.setUnderline(1f, -1f);
                Paragraph parentsInfo = new Paragraph(parentsTitle);
                parentsInfo.add(Chunk.NEWLINE);

                parentsInfo.add(generatesElementsInfo(childs));

                return parentsInfo;
            }

        }

        return null;
    }


}
