package org.argouml.argopdf.kernel;

import org.argouml.uml.diagram.use_case.ui.UMLUseCaseDiagram;
import org.argouml.uml.diagram.ui.UMLDiagram;
import org.argouml.uml.ui.foundation.core.ActionSetModelElementVisibility;
import org.argouml.uml.ui.foundation.core.ActionSetAssociationEndAggregation;
import org.argouml.model.Model;
import org.argouml.i18n.Translator;
import org.apache.log4j.Logger;
import org.netbeans.mdr.handlers.AEIndexSetWrapper;

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
                table.addCell(ReportUtils.createCell(Model.getFacade().getName(elem), 1, null, ReportUtils.getImageName(elem), null));
                table.addCell(ReportUtils.createCell(ReportUtils.getElementDocumentation(elem)));
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
            String imageName = ReportUtils.getImageName(elem);

            details.add(Chunk.NEWLINE);
            details.add(ReportUtils.createImageLabelPhrase(imageName,
                        " " + (Model.getFacade().getName(elem) == null ? "" : Model.getFacade().getName(elem))));

            details.add(ReportUtils.createModifiersInfo(elem));

            if(Model.getFacade().isAActor(elem)) {

                Paragraph assosiationsInfo = generateActorRelationships(elem);
                if(assosiationsInfo != null) {
                    details.add(Chunk.NEWLINE);
                    details.add(assosiationsInfo);
                }

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
     * Generates relationships info of Actor.
     *
     * @param actor an instance of <i>Actor</i> class, which assosiations info will be generated.
     * @return an instance of <i>Paragraph</i> class, which contains assosiations info.
     *         If <i>actor</i> parameter is not an instance of <i>Actor</i> class, return null.
     *         If actor does not have assosiations, return null.
     */
    private static Paragraph generateActorRelationships(Object actor) {
        if(Model.getFacade().isAActor(actor)) {
            Collection assEnds        = Model.getFacade().getAssociationEnds(actor);
            Collection generalization = Model.getFacade().getGeneralizations(actor);
            Collection specialization = Model.getFacade().getSpecializations(actor);

            Paragraph assInfo = null;
            if(assEnds.size() > 0 || generalization.size() > 0 || specialization.size() > 0) {
                Chunk assTitle = new Chunk(Translator.localize("argopdf.report.part.usecase.relationships.title"));
                assTitle.setFont(new Font(Font.HELVETICA, 16, Font.BOLD));
                assInfo = new Paragraph(assTitle);
            }

            if(generalization.size() > 0) {
                for(Object el : generalization) {
                    assInfo.add(Chunk.NEWLINE);
                    assInfo.add(generateGeneralizationInfo(el));
                }
            }

            if(specialization.size() > 0) {
                for(Object el : specialization) {
                    assInfo.add(Chunk.NEWLINE);
                    assInfo.add(generateGeneralizationInfo(el));
                }
            }

            if(assEnds.size() > 0) {

                for(Object el : assEnds) {
                    assInfo.add(Chunk.NEWLINE);
                    assInfo.add(generateAssociatedElementsInfo(el));
                }

                return assInfo;
            }

        }

        return null;
    }

    private static void addToCollection(Collection dest, Collection source) {
        if(source != null && source.size() > 0) {
            Object[] sourceArr = source.toArray();
            for(int i = 0 ; i < sourceArr.length; ++i) {
                dest.add(sourceArr[i]);
            }
        }
    }

    /**
     * Finds all relationship associations of the use case.
     *
     * @param useCase an instance of <i>UseCase</i> class, which relationship associations
     *        will be found.
     * @return an instance of <i>java.util.Collection</i> class, which contains all
     *         relationship associations of the use case.
     *         if parameter <i>useCase</i> is not an instance of <i>UseCase</i> class, returns null.
     */
    private static java.util.Collection getAllUseCaseRelationShips(Object useCase) {
        if(Model.getFacade().isAUseCase(useCase)) {
            Collection returnRes = new ArrayList();
            addToCollection(returnRes, Model.getFacade().getAssociationEnds(useCase));
            addToCollection(returnRes, Model.getFacade().getGeneralizations(useCase));
            addToCollection(returnRes, Model.getFacade().getSpecializations(useCase));
            addToCollection(returnRes, Model.getFacade().getClientDependencies(useCase));
            addToCollection(returnRes, Model.getFacade().getSupplierDependencies(useCase));
            addToCollection(returnRes, Model.getFacade().getExtends(useCase));
            addToCollection(returnRes, Model.getFacade().getExtensionPoints(useCase));
            addToCollection(returnRes, Model.getFacade().getIncludes(useCase));
            return returnRes;
        }

        return null;
    }

    /**
     * Generates relationships info of Use Case.
     *
     * @param useCase an instance of <i>UseCase</i> class, which relationships info will be generated.
     * @return an instance of <i>Paragraph</i> class, which contains relationships info.
     *         If <i>useCase</i> parameter is not an instance of <i>UseCase</i> class, return null.
     *         If useCase does not have relationships, returns null.
     */
    private static Paragraph generateUseCaseRelationships(Object useCase) {
        if(Model.getFacade().isAUseCase(useCase)) {
            Collection relationships = getAllUseCaseRelationShips(useCase);

            
            Paragraph assInfo = null;
            if(relationships.size() > 0) {
                Chunk assTitle = new Chunk(Translator.localize("argopdf.report.part.usecase.relationships.title"));
                assTitle.setFont(new Font(Font.HELVETICA, 16, Font.BOLD));
                assInfo = new Paragraph(assTitle);
            }

            if(relationships.size() > 0) {

                for(Object el : relationships) {
                    assInfo.add(Chunk.NEWLINE);
                    if(Model.getFacade().isAAssociationEnd(el)) {
                        assInfo.add(generateAssociatedElementsInfo(el));
                    } else if(Model.getFacade().isAGeneralization(el)) {
                        assInfo.add(generateGeneralizationInfo(el));
                    } else if(Model.getFacade().isADependency(el)) {
                        assInfo.add(generateDependencyInfo(el));
                    } else if(Model.getFacade().isAExtend(el)) {
                        assInfo.add(generateExtendInfo(el));
                    } else if(Model.getFacade().isAExtensionPoint(el)) {
                        assInfo.add(generateExtensionPointInfo(el));
                    } else if(Model.getFacade().isAInclude(el)) {
                        assInfo.add(generateIncludeInfo(el));
                    }
                }

                return assInfo;
            }

        }

        return null;
    }

    private static PdfPTable generateIncludeInfo(Object include) {
        if(Model.getFacade().isAInclude(include)) {
            PdfPTable table = new PdfPTable(2);
            table.setWidthPercentage(100);

            try {
                table.setWidths(new float[]{2f, 6f});

                String extPName = Model.getFacade().getName(include);
                if(extPName == null || "".equals(extPName)) {
                    extPName = Translator.localize("argopdf.report.part.usecase.include.table.unnamed.title");
                }
                extPName += " : " + Translator.localize("argopdf.report.part.usecase.include.table.title");
                table.addCell(ReportUtils.createCell(extPName, 2,
                                                     ReportUtils.TABLE_HEADER_COLOR,
                                                     new Font(Font.HELVETICA, 12, Font.BOLD, Color.WHITE)));

                Object baseUC = Model.getFacade().getBase(include);
                table.addCell(ReportUtils.createCell(Translator.localize("argopdf.report.part.usecase.include.table.baseuc")));
                table.addCell(ReportUtils.createCell(Model.getFacade().getName(baseUC), ReportUtils.getImageName(baseUC), new float[]{1, 20}));

                Object inclUC = Model.getFacade().getAddition(include);
                table.addCell(ReportUtils.createCell(Model.getFacade().getName(inclUC), ReportUtils.getImageName(inclUC), new float[]{1, 20}));

                table.addCell(ReportUtils.createCell(Translator.localize("argopdf.report.part.usecase.include.table.documentation")));
                table.addCell(ReportUtils.createCell(ReportUtils.getElementDocumentation(include)));

                return table;
            } catch(DocumentException ex) {
                LOG.debug(ex.getMessage());
            }

        }

        return null;
    }

    private static PdfPTable generateExtensionPointInfo(Object extensionPoint) {
        if(Model.getFacade().isAExtensionPoint(extensionPoint)) {
            PdfPTable table = new PdfPTable(2);
            table.setWidthPercentage(100);

            try {
                table.setWidths(new float[]{2f, 6f});

                String extPName = Model.getFacade().getName(extensionPoint);
                if(extPName == null || "".equals(extPName)) {
                    extPName = Translator.localize("argopdf.report.part.usecase.extension.point.table.unnamed.title");
                }
                extPName += " : " + Translator.localize("argopdf.report.part.usecase.extension.point.table.title");
                table.addCell(ReportUtils.createCell(extPName, 2,
                                                     ReportUtils.TABLE_HEADER_COLOR,
                                                     new Font(Font.HELVETICA, 12, Font.BOLD, Color.WHITE)));

                Object baseUC = Model.getFacade().getUseCase(extensionPoint);

                table.addCell(ReportUtils.createCell(Translator.localize("argopdf.report.part.usecase.extension.point.table.baseuc")));
                table.addCell(ReportUtils.createCell(Model.getFacade().getName(baseUC), ReportUtils.getImageName(baseUC), new float[]{1, 20}));

                table.addCell(ReportUtils.createCell(Translator.localize("argopdf.report.part.usecase.extension.point.table.documentation")));
                table.addCell(ReportUtils.createCell(ReportUtils.getElementDocumentation(extensionPoint)));

                return table;
            } catch(DocumentException ex) {
                LOG.debug(ex.getMessage());
            }
        }

        return null;
    }

    private static PdfPTable generateExtendInfo(Object extend) {
        if(Model.getFacade().isAExtend(extend)) {
            PdfPTable table = new PdfPTable(2);
            table.setWidthPercentage(100);

            try {
                table.setWidths(new float[]{2f, 6f});

                String extName = Model.getFacade().getName(extend);
                if(extName == null || "".equals(extName)) {
                    extName = Translator.localize("argopdf.report.part.usecase.extend.table.unnamed.title");
                }
                extName += " : " + Translator.localize("argopdf.report.part.usecase.extend.table.title");
                table.addCell(ReportUtils.createCell(extName, 2,
                                                     ReportUtils.TABLE_HEADER_COLOR,
                                                     new Font(Font.HELVETICA, 12, Font.BOLD, Color.WHITE)));

                Object baseUC = Model.getFacade().getBase(extend);
                Object extension = Model.getFacade().getExtension(extend);
                Object condition = Model.getFacade().getCondition(extend);

                table.addCell(ReportUtils.createCell(Translator.localize("argopdf.report.part.usecase.extend.table.baseuc")));
                table.addCell(ReportUtils.createCell(Model.getFacade().getName(baseUC), ReportUtils.getImageName(baseUC), new float[]{1, 20}));

                table.addCell(ReportUtils.createCell(Translator.localize("argopdf.report.part.usecase.extend.table.extension")));
                table.addCell(ReportUtils.createCell(Model.getFacade().getName(extension), ReportUtils.getImageName(extension), new float[]{1, 20}));

                if (condition == null) {
                    condition = "";
                } else {
                    condition = Model.getFacade().getBody(condition);
                    if(condition == null) {
                        condition = "";
                    }
                }
                table.addCell(ReportUtils.createCell(Translator.localize("argopdf.report.part.usecase.extend.table.condition")));
                table.addCell(ReportUtils.createCell((String)condition));

                table.addCell(ReportUtils.createCell(Translator.localize("argopdf.report.part.usecase.extend.table.documentation")));
                table.addCell(ReportUtils.createCell(ReportUtils.getElementDocumentation(extend)));
                

                return table;
            } catch(DocumentException ex) {
                LOG.debug(ex.getMessage());
            }
        }

        return null;
    }

    /**
     * Generates information of dependency relationship.
     *
     * @param dependency an instance of <i>Dependency</i> class, witch info will be generated.
     * @return an instance of <i>PdfPTable</i> class, which contains info of appropriate dependency.
     *         if parameter <i>dependency</i> is not an instance of <i>Dependency</i> class, returns null.
     */
    private static PdfPTable generateDependencyInfo(Object dependency) {
        if(Model.getFacade().isADependency(dependency)) {
            PdfPTable table = new PdfPTable(2);
            table.setWidthPercentage(100);

            try {
                table.setWidths(new float[]{2f, 6f});

                String depName = Model.getFacade().getName(dependency);
                if(depName == null || "".equals(depName)) {
                    depName = Translator.localize("argopdf.report.part.usecase.dependency.table.unnamed.title");
                }
                depName += " : " + Translator.localize("argopdf.report.part.usecase.dependency.table.title");
                table.addCell(ReportUtils.createCell(depName, 2,
                                                     ReportUtils.TABLE_HEADER_COLOR,
                                                     new Font(Font.HELVETICA, 12, Font.BOLD, Color.WHITE)));

                Collection suppliers = Model.getFacade().getSuppliers(dependency);
                Collection clients = Model.getFacade().getClients(dependency);

                for(Object el : suppliers) {
                    table.addCell(ReportUtils.createCell(Translator.localize("argopdf.report.part.usecase.dependency.table.supplier")));
                    table.addCell(ReportUtils.createCell(Model.getFacade().getName(el), ReportUtils.getImageName(el), new float[]{1, 20}));
                }

                for(Object el : clients) {
                    table.addCell(ReportUtils.createCell(Translator.localize("argopdf.report.part.usecase.dependency.table.client")));
                    table.addCell(ReportUtils.createCell(Model.getFacade().getName(el), ReportUtils.getImageName(el), new float[]{1, 20}));
                }

                table.addCell(ReportUtils.createCell(Translator.localize("argopdf.report.part.usecase.dependency.table.documentation")));
                table.addCell(ReportUtils.createCell(ReportUtils.getElementDocumentation(dependency)));

                return table;
            } catch(DocumentException ex) {
                LOG.debug(ex.getMessage());
            }
        }

        return null;
    }

    /**
     * Generates information of generalization relationship.
     *
     * @param generalization an instance of <i>Generalization</i> class, witch info will be generated.
     * @return an instance of <i>PdfPTable</i> class, which contains info of appropriate generalization.
     *         if parameter <i>generalization</i> is not an instance of <i>Generalization</i> class, returns null.
     */
    private static PdfPTable generateGeneralizationInfo(Object generalization) {
        if(Model.getFacade().isAGeneralization(generalization)) {
            PdfPTable table = new PdfPTable(2);
            table.setWidthPercentage(100);

            try {
                table.setWidths(new float[]{2f, 6f});

                String genName = (String)Model.getFacade().getDiscriminator(generalization);
                if(genName == null || "".equals(genName)) {
                    genName = Translator.localize("argopdf.report.part.usecase.generalization.table.unnamed.title");
                }
                genName += " : " + Translator.localize("argopdf.report.part.usecase.generalization.title");
                table.addCell(ReportUtils.createCell(genName, 2,
                                                     ReportUtils.TABLE_HEADER_COLOR,
                                                     new Font(Font.HELVETICA, 12, Font.BOLD, Color.WHITE)));

                Object parent = Model.getFacade().getParent(generalization);
                Object child = Model.getFacade().getChild(generalization);

                table.addCell(ReportUtils.createCell(Translator.localize("argopdf.report.part.usecase.generalization.table.parent")));
                table.addCell(ReportUtils.createCell(Model.getFacade().getName(parent), ReportUtils.getImageName(parent), new float[]{1, 20}));

                table.addCell(ReportUtils.createCell(Translator.localize("argopdf.report.part.usecase.generalization.table.child")));
                table.addCell(ReportUtils.createCell(Model.getFacade().getName(child), ReportUtils.getImageName(child), new float[]{1, 20}));

                table.addCell(ReportUtils.createCell(Translator.localize("argopdf.report.part.usecase.generalization.table.documentation")));
                table.addCell(ReportUtils.createCell(ReportUtils.getElementDocumentation(generalization)));

                return table;
            } catch(DocumentException ex) {
                LOG.debug(ex.getMessage());
            }
        }

        return null;
    }

    /**
     * Generates information of assosiation relationship.
     *
     * @param assEnd an instance of <i>AssociationEnd</i> class, by which info of assosiated
     *               element will be generated
     * @return an instance of <i>PdfPTable</i> class, which contains info of assosiated element
     */
    private static PdfPTable generateAssociatedElementsInfo(Object assEnd) {
        if(Model.getFacade().isAAssociationEnd(assEnd)) {
            PdfPTable table = new PdfPTable(2);
            table.setWidthPercentage(100);

            try {
                table.setWidths(new float[]{2f, 6f});

                Object assosiation = Model.getFacade().getAssociation(assEnd);

                String assName = Model.getFacade().getName(assosiation);
                if(assName == null || "".equals(assName)) {
                    assName = Translator.localize("argopdf.report.part.usecase.assosiation.table.unnamed.title");
                }
                assName += " : " + Translator.localize("argopdf.report.part.usecase.assosiation.table.title");

                table.addCell(ReportUtils.createCell(assName, 2,
                                                     ReportUtils.TABLE_HEADER_COLOR,
                                                     new Font(Font.HELVETICA, 12, Font.BOLD, Color.WHITE)));

                table.addCell(ReportUtils.createCell(Translator.localize("argopdf.report.part.usecase.assosiation.table.abstract")));
                table.addCell(ReportUtils.createCell(String.valueOf(Model.getFacade().isAbstract(assosiation)), 2));
                table.addCell(ReportUtils.createCell(Translator.localize("argopdf.report.part.usecase.assosiation.table.leaf")));
                table.addCell(ReportUtils.createCell(String.valueOf(Model.getFacade().isLeaf(assosiation)), 2));
                table.addCell(ReportUtils.createCell(Translator.localize("argopdf.report.part.usecase.assosiation.table.root")));
                table.addCell(ReportUtils.createCell(String.valueOf(Model.getFacade().isRoot(assosiation)), 2));

                if(Model.getFacade().isAActor(Model.getFacade().getType(assEnd))  ||
                   Model.getFacade().isAUseCase(Model.getFacade().getType(assEnd))) {
                    Collection connection = Model.getFacade().getConnections(assosiation);
                    for(Object el : connection) {
                        if(Model.getFacade().isAAssociationEnd(el)) {
                            table.addCell(ReportUtils.createCell("   ", 2));
                            PdfPTable assToTable = generateAssosiatedEndInfo(el);
                            PdfPCell cell2 = new PdfPCell(assToTable);
                            cell2.setColspan(2);
                            table.addCell(cell2);
                        }
                    }
                }

                return table;
            } catch(DocumentException ex) {
                LOG.debug(ex.getMessage());
            }

        }

        return null;
    }

    /**
     * Generates information of assosiation end.
     *
     * @param assEnd        an instance of <i>AssociationEnd</i> class, which info will be generated
     * @return An instance of <i>PdfPTable</i> class, which contains info of assosiation end.
     *         If <i>assEnd</i> is not an instance of AssociationEnd class, returns null.
     */
    private static PdfPTable generateAssosiatedEndInfo(Object assEnd) {
        if(Model.getFacade().isAAssociationEnd(assEnd)) {
            PdfPTable table = new PdfPTable(2);
            table.setWidthPercentage(100);

            try {
                table.setWidths(new float[]{2f, 6f});
                Object element = Model.getFacade().getType(assEnd);

                String imageName = ReportUtils.getImageName(element);
                table.addCell(ReportUtils.createCell(Model.getFacade().getName(element), 2, null,  imageName, null, new float[]{1, 20}));

                table.addCell(ReportUtils.createCell(Translator.localize("argopdf.report.part.usecase.assosiation.table.assosiation.end")));
                table.addCell(ReportUtils.createCell(Model.getFacade().getName(assEnd)));

                table.addCell(ReportUtils.createCell(Translator.localize("argopdf.report.part.usecase.assosiation.table.multiplicity")));
                table.addCell(Model.getFacade().toString(Model.getFacade().getMultiplicity(assEnd)));

                table.addCell(ReportUtils.createCell(Translator.localize("argopdf.report.part.usecase.assosiation.table.visibility")));
                table.addCell(getAssosiationEndVisibility(assEnd));

                table.addCell(ReportUtils.createCell(Translator.localize("argopdf.report.part.usecase.assosiation.table.aggregation")));
                table.addCell(getAggregationKindEndVisibility(assEnd));

                table.addCell(ReportUtils.createCell(Translator.localize("argopdf.report.part.usecase.assosiation.table.navigable")));
                table.addCell(Model.getFacade().toString(Model.getFacade().isNavigable(assEnd)));

                table.addCell(ReportUtils.createCell(Translator.localize("argopdf.report.part.usecase.assosiation.table.documentation")));
                table.addCell(ReportUtils.createCell(ReportUtils.getElementDocumentation(assEnd)));

                return table;
            } catch(DocumentException ex) {
                LOG.debug(ex.getMessage());
            }

        }

        return null;
    }


}
