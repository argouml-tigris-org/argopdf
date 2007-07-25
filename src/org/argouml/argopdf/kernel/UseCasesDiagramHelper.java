package org.argouml.argopdf.kernel;

import org.argouml.uml.diagram.use_case.ui.UMLUseCaseDiagram;
import org.argouml.uml.diagram.ui.UMLDiagram;
import org.argouml.uml.ui.foundation.core.ActionSetModelElementVisibility;
import org.argouml.uml.ui.foundation.core.ActionSetAssociationEndAggregation;
import org.argouml.model.Model;
import org.argouml.i18n.Translator;
import org.apache.log4j.Logger;

import java.util.List;
import java.util.ArrayList;
import java.util.Collection;
import java.awt.*;

import com.lowagie.text.*;
import com.lowagie.text.Font;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfPCell;

/**
 * Contains helper methods for processing Use Case diagrams
 *
 * @author Dmitry Churbanau
 * @version 0.1
 */
public class UseCasesDiagramHelper {

    private static final Logger LOG = Logger.getLogger(UseCasesDiagramHelper.class);

    /**
     * Returns the visibility value for the assosiation end.
     *
     * @param assosiationEnd an instance of <i>AssociationEnd</i> class
     * @return the visibility value of assosiation end
     */
    public static String getAssosiationEndVisibility(Object assosiationEnd) {
        String visibility = "";
        if(Model.getFacade().isAAssociationEnd(assosiationEnd)) {
            Object kind = Model.getFacade().getVisibility(assosiationEnd);
            if (kind == null
                    || kind.equals(
                            Model.getVisibilityKind().getPublic())) {
                visibility = ActionSetModelElementVisibility.PUBLIC_COMMAND;
            } else if (kind.equals(
                    Model.getVisibilityKind().getPackage())) {
                visibility = ActionSetModelElementVisibility.PACKAGE_COMMAND;
            } else if (kind.equals(
                    Model.getVisibilityKind().getProtected())) {
                visibility = ActionSetModelElementVisibility.PROTECTED_COMMAND;
            } else if (kind.equals(
                    Model.getVisibilityKind().getPrivate())) {
                visibility = ActionSetModelElementVisibility.PRIVATE_COMMAND;
            }
        }

        return visibility;
    }

     /**
     * Returns the aggregation kind value for the assosiation end.
     *
     * @param assosiationEnd an instance of <i>AssociationEnd</i> class
     * @return the aggregation kind value of assosiation end
     */
    public static String getAggregationKindEndVisibility(Object assosiationEnd) {
        String aggregation = "";
        if(Model.getFacade().isAAssociationEnd(assosiationEnd)) {
            Object kind = Model.getFacade().getAggregation(assosiationEnd);
            if (kind == null
                    || kind.equals(
                            Model.getAggregationKind().getNone())) {
                aggregation = ActionSetAssociationEndAggregation.NONE_COMMAND;
            } else if (kind.equals(
                    Model.getAggregationKind().getComposite())) {
                aggregation = ActionSetAssociationEndAggregation.COMPOSITE_COMMAND;
            } else if (kind.equals(
                    Model.getAggregationKind().getAggregate())) {
                aggregation = ActionSetAssociationEndAggregation.AGGREGATE_COMMAND;
            }
        }

        return aggregation;
    }

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

        return generateElementsInfo(elements);
    }

    /**
     * Generates info of elements. Info is presented in a table, with name and documentation columns.
     *
     * @param elements ArraList of alements, info of which will be generated
     * @return and instance of <i>PdfPTable</i> class, which contains elements info.
     */
    private static PdfPTable generateElementsInfo(ArrayList elements) {
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
                String imageName;
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
                table.addCell(ReportUtils.createCell(documentation));
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
        for (Object elem : elements) {
            String imageName;
            if(Model.getFacade().isAActor(elem)) {
                imageName = "Actor";
            } else if(Model.getFacade().isAUseCase(elem)) {
                imageName = "UseCase";
            } else {
                //displays only Actors and Use Cases
                continue;
            }

            details.add(Chunk.NEWLINE);
            details.add(ReportUtils.createImageLabelPhrase(imageName,
                        " " + (Model.getFacade().getName(elem) == null ? "" : Model.getFacade().getName(elem))));

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

                Paragraph assosiationsInfo = generateActorRelationships(elem);
                if(assosiationsInfo != null) {
                    details.add(Chunk.NEWLINE);
                    details.add(assosiationsInfo);
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
            } else if(Model.getFacade().isAUseCase(elem)) {
                Paragraph assosiationsInfo = generateUseCaseRelationships(elem);
                if(assosiationsInfo != null) {
                    details.add(Chunk.NEWLINE);
                    details.add(assosiationsInfo);
                }
            }

            details.add(Chunk.NEWLINE);
        }

        return details;
    }

    /**
     * Generates parents info of an Actor
     *
     * @param actor an instance of <i>Actor</i> class, which parents info will be generated
     * @return an instance of <i>Paragraph</i> class, which contains parents info.
     *         If <i>actor</i> parameter is not an instance of <i>Actor</i> class, return null.
     *         If actor does not have parents, returns null.
     */
    private static Paragraph generatesParentsInfo(Object actor) {
        if(Model.getFacade().isAActor(actor)) {

            Collection general = Model.getFacade().getGeneralizations(actor);
            ArrayList parents = new ArrayList();
            for(Object el : general) {
                parents.add(Model.getFacade().getParent(el));
            }

            if(parents.size() > 0) {
                Chunk parentsTitle = new Chunk(Translator.localize("argopdf.report.part.usecase.parents.title"));
                parentsTitle.setFont(new Font(Font.HELVETICA, 16, Font.BOLD));
                //parentsTitle.setUnderline(1f, -1f);
                Paragraph parentsInfo = new Paragraph(parentsTitle);

                parentsInfo.add(generateElementsInfo(parents));

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
     *         If <i>actor</i> parameter is not an instance of <i>Actor</i> class, return null.
     *         If actor does not have childs, returns null.
     */
    private static Paragraph generatesChildsInfo(Object actor) {
        if(Model.getFacade().isAActor(actor)) {

            Collection general = Model.getFacade().getSpecializations(actor);
            ArrayList childs = new ArrayList();
            for(Object el : general) {
                childs.add(Model.getFacade().getChild(el));
            }

            if(childs.size() > 0) {
                Chunk parentsTitle = new Chunk(Translator.localize("argopdf.report.part.usecase.children.title"));
                parentsTitle.setFont(new Font(Font.HELVETICA, 16, Font.BOLD));
                //parentsTitle.setUnderline(1f, -1f);
                Paragraph parentsInfo = new Paragraph(parentsTitle);

                parentsInfo.add(generateElementsInfo(childs));

                return parentsInfo;
            }

        }

        return null;
    }

    /**
     * Generates relationships info of Actor.
     *
     * @param actor an instance of <i>Actor</i> class, which assosiations info will be generated.
     * @return an instance of <i>Paragraph</i> class, which contains assosiations info.
     *         If <i>actor</i> parameter is not an instance of <i>Actor</i> class, return null.
     *         If actor does not have assosiations, return null.
     */
    private static Paragraph generateActorRelationships(Object actor) {
        if(Model.getFacade().isAActor(actor)) {
            Collection assEnds = Model.getFacade().getAssociationEnds(actor);

            if(assEnds.size() > 0) {
                Chunk assTitle = new Chunk(Translator.localize("argopdf.report.part.usecase.assosiations.title"));
                assTitle.setFont(new Font(Font.HELVETICA, 16, Font.BOLD));
                Paragraph assInfo = new Paragraph(assTitle);

                int amount = 0;
                for(Object el : assEnds) {
                    if(amount > 0) {
                        assInfo.add(Chunk.NEWLINE);
                    }
                    assInfo.add(generateAssosiatedElementsInfo(el));
                    amount++;
                }

                return assInfo;
            }

        }

        return null;
    }

    /**
     * Generates relationships info of Use Case.
     *
     * @param useCase an instance of <i>UseCase</i> class, which assosiations info will be generated.
     * @return an instance of <i>Paragraph</i> class, which contains assosiations info.
     *         If <i>useCase</i> parameter is not an instance of <i>UseCase</i> class, return null.
     *         If useCase does not have assosiations, return null.
     */
    private static Paragraph generateUseCaseRelationships(Object useCase) {
        if(Model.getFacade().isAUseCase(useCase)) {
            Collection assEnds = Model.getFacade().getAssociationEnds(useCase);

            if(assEnds.size() > 0) {
                Chunk assTitle = new Chunk(Translator.localize("argopdf.report.part.usecase.assosiations.title"));
                assTitle.setFont(new Font(Font.HELVETICA, 16, Font.BOLD));
                Paragraph assInfo = new Paragraph(assTitle);

                int amount = 0;
                for(Object el : assEnds) {
                    if(amount > 0) {
                        assInfo.add(Chunk.NEWLINE);
                    }
                    assInfo.add(generateAssosiatedElementsInfo(el));
                    amount++;
                }

                return assInfo;
            }

        }

        return null;
    }

    /**
     * Generates information of assosiation. It is important, that the type of assosiation end
     * must be an actor. Otherwise, method returns empty table.
     *
     * @param assEnd an instance of <i>AssociationEnd</i> class, by which info of assosiated
     *                element will be generated
     * @return an instance of <i>PdfPTable</i> class, which contains info of assosiated element
     */
    private static PdfPTable generateAssosiatedElementsInfo(Object assEnd) {
        if(Model.getFacade().isAAssociationEnd(assEnd)) {
            PdfPTable table = new PdfPTable(2);
            table.setWidthPercentage(100);

            try {
                table.setWidths(new float[]{2f, 6f});

                    Object assosiation = Model.getFacade().getAssociation(assEnd);

                    String assName = Model.getFacade().getName(assosiation);
                    if(assName == null && "".equals(assName)) {
                        assName = "";
                    } else {
                        assName += " : ";
                    }
                    assName += Translator.localize("argopdf.report.part.usecase.assosiation.table.title");

                    table.addCell(ReportUtils.createCell(assName, 2,
                                                         ReportUtils.TABLE_HEADER_COLOR,
                                                         new Font(Font.HELVETICA, 12, Font.BOLD, Color.WHITE)));

                    table.addCell(ReportUtils.createCell(Translator.localize("argopdf.report.part.usecase.assosiation.table.abstract")));
                    table.addCell(ReportUtils.createCell(String.valueOf(Model.getFacade().isAbstract(assosiation)), 2));
                    table.addCell(ReportUtils.createCell(Translator.localize("argopdf.report.part.usecase.assosiation.table.leaf")));
                    table.addCell(ReportUtils.createCell(String.valueOf(Model.getFacade().isLeaf(assosiation)), 2));
                    table.addCell(ReportUtils.createCell(Translator.localize("argopdf.report.part.usecase.assosiation.table.root")));
                    table.addCell(ReportUtils.createCell(String.valueOf(Model.getFacade().isRoot(assosiation)), 2));

                    table.addCell(ReportUtils.createCell("   ", 2));
                if(Model.getFacade().isAActor(Model.getFacade().getType(assEnd))) {
                    PdfPTable assFromTable = generateAssosiatedEndInfo(assEnd, Translator.localize("argopdf.report.part.usecase.assosiation.table.from"));
                    PdfPCell cell = new PdfPCell(assFromTable);
                    cell.setColspan(2);
                    table.addCell(cell);

                    table.addCell(ReportUtils.createCell("   ", 2));

                    Collection connection = Model.getFacade().getConnections(assosiation);
                    for(Object el : connection) {
                        if(Model.getFacade().isAAssociationEnd(el) && Model.getFacade().isAUseCase(Model.getFacade().getType(el))) {
                            PdfPTable assToTable = generateAssosiatedEndInfo(el, Translator.localize("argopdf.report.part.usecase.assosiation.table.to"));
                            PdfPCell cell2 = new PdfPCell(assToTable);
                            cell2.setColspan(2);
                            table.addCell(cell2);
                            break;
                        }
                    }
                } else if(Model.getFacade().isAUseCase(Model.getFacade().getType(assEnd))) {
                    PdfPTable assToTable = generateAssosiatedEndInfo(assEnd, Translator.localize("argopdf.report.part.usecase.assosiation.table.to"));
                    PdfPCell cell2 = new PdfPCell(assToTable);
                    cell2.setColspan(2);
                    table.addCell(cell2);

                    table.addCell(ReportUtils.createCell("   ", 2));

                    Collection connection = Model.getFacade().getConnections(assosiation);
                    for(Object el : connection) {
                        if(Model.getFacade().isAAssociationEnd(el) && Model.getFacade().isAActor(Model.getFacade().getType(el))) {
                            PdfPTable assFromTable = generateAssosiatedEndInfo(assEnd, Translator.localize("argopdf.report.part.usecase.assosiation.table.from"));
                            PdfPCell cell = new PdfPCell(assFromTable);
                            cell.setColspan(2);
                            table.addCell(cell);
                            break;
                        }
                    }
                }

            } catch(DocumentException ex) {
                LOG.debug(ex.getMessage());
            }

            return table;
        }

        return null;
    }

    /**
     * Generates information of assosiation end.
     *
     * @param assEnd        an instance of <i>AssociationEnd</i> class, which info will be generated
     * @param directionName string value of direction of the assosiation
     * @return An instance of <i>PdfPTable</i> class, which contains info of assosiation end.
     *         If <i>assEnd</i> is not an instance of AssociationEnd class, returns null.
     */
    private static PdfPTable generateAssosiatedEndInfo(Object assEnd, String directionName) {
        if(Model.getFacade().isAAssociationEnd(assEnd)) {
            PdfPTable table = new PdfPTable(2);
            table.setWidthPercentage(100);

            try {
                table.setWidths(new float[]{2f, 6f});
                Object element = Model.getFacade().getType(assEnd);

                table.addCell(ReportUtils.createCell(directionName));
                String imageName = "";
                if(Model.getFacade().isAActor(element)) {
                    imageName = "Actor";
                } else if(Model.getFacade().isAUseCase(element)) {
                    imageName = "UseCase";
                }
                table.addCell(ReportUtils.createCell(Model.getFacade().getName(element), imageName, new float[]{1, 20}));

                table.addCell(ReportUtils.createCell(Translator.localize("argopdf.report.part.usecase.assosiation.table.assosiation.end")));
                table.addCell(ReportUtils.createCell(Model.getFacade().getName(assEnd)));

                table.addCell(ReportUtils.createCell(Translator.localize("argopdf.report.part.usecase.assosiation.table.multiplicity")));
                table.addCell(Model.getFacade().toString(Model.getFacade().getMultiplicity(assEnd)));

                table.addCell(ReportUtils.createCell(Translator.localize("argopdf.report.part.usecase.assosiation.table.visibility")));
                table.addCell(getAssosiationEndVisibility(assEnd));

                table.addCell(ReportUtils.createCell(Translator.localize("argopdf.report.part.usecase.assosiation.table.aggregation")));
                table.addCell(getAggregationKindEndVisibility(assEnd));

                table.addCell(ReportUtils.createCell(Translator.localize("argopdf.report.part.usecase.assosiation.table.documentation")));
                Object taggedValue = Model.getFacade().getTaggedValue(assEnd, "documentation");
                String documentation = "";
                if(taggedValue != null) {
                    documentation = Model.getFacade().getValueOfTag(taggedValue);
                }
                table.addCell(ReportUtils.createCell(documentation));

                return table;
            } catch(DocumentException ex) {
                LOG.debug(ex.getMessage());
            }

        }

        return null;
    }


}
