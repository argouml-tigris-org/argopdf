// $Id: UseCasesDiagramHelper.java 62 2007-08-25 19:18:28Z comp_ $
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

import org.argouml.uml.diagram.use_case.ui.UMLUseCaseDiagram;
import org.argouml.uml.diagram.ui.UMLDiagram;
import org.argouml.uml.ui.foundation.core.ActionSetModelElementVisibility;
import org.argouml.uml.ui.foundation.core.ActionSetAssociationEndAggregation;
import org.argouml.model.Model;
import org.argouml.i18n.Translator;
import org.argouml.argopdf.kernel.ReportUtils;
import org.argouml.argopdf.kernel.TableUtils;
import org.apache.log4j.Logger;

import java.util.List;
import java.util.ArrayList;
import java.util.Collection;
import java.awt.*;

import com.lowagie.text.*;
import com.lowagie.text.Font;
import com.lowagie.text.Image;
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
        String aggregation = null;
        if(Model.getFacade().isAAssociationEnd(assosiationEnd)) {
            Object kind = Model.getFacade().getAggregation(assosiationEnd);
            if(kind == null) return "";
            if (kind.equals(
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
     * Adds Use Case diagram to the report.
     *
     * @param document current document instance
     * @param chapter  chapter of Use Case diagrams
     * @param diagram  Use Case diagram to add to the report
     * @param generateDiagrams defines, whether diagram image should be generated
     */
    public static void addUseCaseDiagram(Document document, Chapter chapter, UMLUseCaseDiagram diagram, boolean generateDiagrams) {

        if(diagram == null) return;
        LOG.debug("Add Use Case diagram: " + diagram.getName());

        //Creates section in pdf file, which will contain
        //info about Use Case diagram, which is in processing
        Section section = chapter.addSection(ReportUtils.generateTitle(ReportUtils.getElementName(diagram), 1, false), 2);
        section.add(Chunk.NEWLINE);

        Image im = ReportUtils.makeImageOfDiagram(diagram);
        if(im != null && generateDiagrams) {
            ReportUtils.adjustImageSizeToDocumentPageSize(im,  document);
            section.add(new Chunk(im, 0, 0, true));
        }

        section.add(Chunk.NEWLINE);
        UseCasesDiagramHelper.generateSummaryInfo(section, diagram);
        UseCasesDiagramHelper.generateDetailedInfo(section, diagram);

        section.add(Chunk.NEXTPAGE);
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
     * Generates use cases diagram summary info
     *
     * @param section section, to which summary info block will be added
     * @param diagram current diagarm, which summary info will be generated
     */
    public static void generateSummaryInfo(Section section, UMLDiagram diagram) {

        if(diagram instanceof UMLUseCaseDiagram) {
            PdfPTable table = generateUseCasesSummaryInfo((UMLUseCaseDiagram)diagram);
            if(table != null) {
                Section subSect = section.addSection(ReportUtils.generateTitle(
                                                     Translator.localize("argopdf.report.summary"), 2, true),
                                                     0);

                subSect.add(table);
            }
        }

    }

    /**
     * Generates detailed info for uml element.
     *
     * @param section section, to which detailed info will be added
     * @param diagram diagram, which elements need to generate detailed info
     */
    public static void generateDetailedInfo(Section section, UMLDiagram diagram) {

        Section subSect = section.addSection(ReportUtils.generateTitle(
                                             Translator.localize("argopdf.report.details"), 2, true),
                                             0);

        if(diagram instanceof UMLUseCaseDiagram) {
            subSect.add(generateUseCaseDetailedInfo((UMLUseCaseDiagram)diagram));
        }
        subSect.add(Chunk.NEWLINE);

    }

    /**
     * Generates summary info of use case diagram.
     *
     * @param diagram an instance of current use case diagram
     * @return an instance of <i>PdfPTable</i> class with summary info
     *         null, if diagram does not have components
     */
    private static PdfPTable generateUseCasesSummaryInfo(UMLUseCaseDiagram diagram) {
        ArrayList elements = UseCasesDiagramHelper.getUseCaseActors(diagram);
        elements.addAll(UseCasesDiagramHelper.getUseCases(diagram));

        return TableUtils.generateElementsInfo(elements);
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

            details.add(Chunk.NEWLINE);
            details.add(ReportUtils.createImageLabelPhrase(ReportUtils.getImageName(elem), null, ReportUtils.getAnchorElementName(elem), 2));
            details.add(Chunk.NEWLINE);
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

    /**
     * Adds elements of one collection to another one
     *
     * @param dest
     * @param source
     */
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
                table.addCell(TableUtils.createCell(extPName, 2,
                                                     TableUtils.TABLE_HEADER_COLOR,
                                                     new Font(Font.HELVETICA, 12, Font.BOLD, Color.WHITE)));

                Object baseUC = Model.getFacade().getBase(include);
                table.addCell(TableUtils.createCell(Translator.localize("argopdf.report.part.usecase.include.table.baseuc")));
                table.addCell(TableUtils.createCell(Model.getFacade().getName(baseUC), ReportUtils.getImageName(baseUC), new float[]{1, 20}));

                Object inclUC = Model.getFacade().getAddition(include);
                table.addCell(TableUtils.createCell(Translator.localize("argopdf.report.part.usecase.include.table.includeduc")));
                table.addCell(TableUtils.createCell(Model.getFacade().getName(inclUC), ReportUtils.getImageName(inclUC), new float[]{1, 20}));

                table.addCell(TableUtils.createCell(Translator.localize("argopdf.report.part.usecase.include.table.documentation")));
                table.addCell(TableUtils.createCell(ReportUtils.getElementsDocumentation(include)));

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
                table.addCell(TableUtils.createCell(extPName, 2,
                                                     TableUtils.TABLE_HEADER_COLOR,
                                                     new Font(Font.HELVETICA, 12, Font.BOLD, Color.WHITE)));

                Object baseUC = Model.getFacade().getUseCase(extensionPoint);

                table.addCell(TableUtils.createCell(Translator.localize("argopdf.report.part.usecase.extension.point.table.baseuc")));
                table.addCell(TableUtils.createCell(Model.getFacade().getName(baseUC), ReportUtils.getImageName(baseUC), new float[]{1, 20}));

                table.addCell(TableUtils.createCell(Translator.localize("argopdf.report.part.usecase.extension.point.table.documentation")));
                table.addCell(TableUtils.createCell(ReportUtils.getElementsDocumentation(extensionPoint)));

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
                table.addCell(TableUtils.createCell(extName, 2,
                                                     TableUtils.TABLE_HEADER_COLOR,
                                                     new Font(Font.HELVETICA, 12, Font.BOLD, Color.WHITE)));

                Object baseUC = Model.getFacade().getBase(extend);
                Object extension = Model.getFacade().getExtension(extend);
                Object condition = Model.getFacade().getCondition(extend);

                table.addCell(TableUtils.createCell(Translator.localize("argopdf.report.part.usecase.extend.table.baseuc")));
                table.addCell(TableUtils.createCell(Model.getFacade().getName(baseUC), ReportUtils.getImageName(baseUC), new float[]{1, 20}));

                table.addCell(TableUtils.createCell(Translator.localize("argopdf.report.part.usecase.extend.table.extension")));
                table.addCell(TableUtils.createCell(Model.getFacade().getName(extension), ReportUtils.getImageName(extension), new float[]{1, 20}));

                if (condition == null) {
                    condition = "";
                } else {
                    condition = Model.getFacade().getBody(condition);
                    if(condition == null) {
                        condition = "";
                    }
                }
                table.addCell(TableUtils.createCell(Translator.localize("argopdf.report.part.usecase.extend.table.condition")));
                table.addCell(TableUtils.createCell((String)condition));

                table.addCell(TableUtils.createCell(Translator.localize("argopdf.report.part.usecase.extend.table.documentation")));
                table.addCell(TableUtils.createCell(ReportUtils.getElementsDocumentation(extend)));
                

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
                table.addCell(TableUtils.createCell(depName, 2,
                                                     TableUtils.TABLE_HEADER_COLOR,
                                                     new Font(Font.HELVETICA, 12, Font.BOLD, Color.WHITE)));

                Collection suppliers = Model.getFacade().getSuppliers(dependency);
                Collection clients = Model.getFacade().getClients(dependency);

                for(Object el : suppliers) {
                    table.addCell(TableUtils.createCell(Translator.localize("argopdf.report.part.usecase.dependency.table.supplier")));
                    table.addCell(TableUtils.createCell(Model.getFacade().getName(el), ReportUtils.getImageName(el), new float[]{1, 20}));
                }

                for(Object el : clients) {
                    table.addCell(TableUtils.createCell(Translator.localize("argopdf.report.part.usecase.dependency.table.client")));
                    table.addCell(TableUtils.createCell(Model.getFacade().getName(el), ReportUtils.getImageName(el), new float[]{1, 20}));
                }

                table.addCell(TableUtils.createCell(Translator.localize("argopdf.report.part.usecase.dependency.table.documentation")));
                table.addCell(TableUtils.createCell(ReportUtils.getElementsDocumentation(dependency)));

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
                table.addCell(TableUtils.createCell(genName, 2,
                                                     TableUtils.TABLE_HEADER_COLOR,
                                                     new Font(Font.HELVETICA, 12, Font.BOLD, Color.WHITE)));

                Object parent = Model.getFacade().getParent(generalization);
                Object child = Model.getFacade().getChild(generalization);

                table.addCell(TableUtils.createCell(Translator.localize("argopdf.report.part.usecase.generalization.table.parent")));
                table.addCell(TableUtils.createCell(Model.getFacade().getName(parent), ReportUtils.getImageName(parent), new float[]{1, 20}));

                table.addCell(TableUtils.createCell(Translator.localize("argopdf.report.part.usecase.generalization.table.child")));
                table.addCell(TableUtils.createCell(Model.getFacade().getName(child), ReportUtils.getImageName(child), new float[]{1, 20}));

                table.addCell(TableUtils.createCell(Translator.localize("argopdf.report.part.usecase.generalization.table.documentation")));
                table.addCell(TableUtils.createCell(ReportUtils.getElementsDocumentation(generalization)));

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

                String assName = ReportUtils.getElementName(assosiation) + " : " + Translator.localize("argopdf.report.association");

                table.addCell(TableUtils.createHeaderCellWithFont(assName, 2));

                table.addCell(TableUtils.createCell(Translator.localize("argopdf.report.part.usecase.assosiation.table.abstract")));
                table.addCell(TableUtils.createCell(String.valueOf(Model.getFacade().isAbstract(assosiation)), 2));
                table.addCell(TableUtils.createCell(Translator.localize("argopdf.report.part.usecase.assosiation.table.leaf")));
                table.addCell(TableUtils.createCell(String.valueOf(Model.getFacade().isLeaf(assosiation)), 2));
                table.addCell(TableUtils.createCell(Translator.localize("argopdf.report.part.usecase.assosiation.table.root")));
                table.addCell(TableUtils.createCell(String.valueOf(Model.getFacade().isRoot(assosiation)), 2));

                if(Model.getFacade().isAActor(Model.getFacade().getType(assEnd))  ||
                   Model.getFacade().isAUseCase(Model.getFacade().getType(assEnd))) {
                    Collection connection = Model.getFacade().getConnections(assosiation);
                    for(Object el : connection) {
                        if(Model.getFacade().isAAssociationEnd(el)) {
                            table.addCell(TableUtils.createCell("   ", 2));
                            PdfPTable assToTable = ReportUtils.generateAssosiatedEndInfo(el);
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


}
