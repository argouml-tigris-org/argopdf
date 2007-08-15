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

import com.lowagie.text.*;
import com.lowagie.text.Font;
import com.lowagie.text.Image;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfPCell;
import org.argouml.uml.diagram.static_structure.ui.UMLClassDiagram;
import org.argouml.uml.ui.foundation.core.ActionSetModelElementVisibility;
import org.argouml.uml.ui.foundation.core.ActionSetParameterDirectionKind;
import org.argouml.i18n.Translator;
import org.argouml.model.Model;
import org.apache.log4j.Logger;
import org.omg.uml.foundation.core.*;
import org.omg.uml.foundation.core.Enumeration;
import org.omg.uml.foundation.datatypes.Expression;

import java.util.*;
import java.util.List;

/**
 * Contains helper methods for processing class diagrams
 *
 * @author Dmitry Churbanau
 * @version 0.1
 */
public class ClassDiagramHelper {

    private static final Logger LOG = Logger.getLogger(ClassDiagramHelper.class);

    /**
     * Generates string representative of class operation kind
     *
     * @param parameter class operation parameter
     * @return string representative of class operation kind
     */
    public static String getParameterKind(Parameter parameter) {
        if (parameter != null) {
            Object kind = Model.getFacade().getKind(parameter);
            if (kind == null || kind.equals(Model.getDirectionKind().getInParameter())) {
                return  ActionSetParameterDirectionKind.IN_COMMAND;
            } else if (kind.equals(Model.getDirectionKind().getInOutParameter())) {
                return ActionSetParameterDirectionKind.INOUT_COMMAND;
            } else if (kind.equals(Model.getDirectionKind().getOutParameter())) {
                return ActionSetParameterDirectionKind.OUT_COMMAND;
            } else {
                return ActionSetParameterDirectionKind.RETURN_COMMAND;
            }
        }

        return "";
    }

    /**
     * Generates string representative of visibility parameter of uml class
     *
     * @param umlObject an instance of uml object: <i>UmlClass</i>, <i>Interface</i>
     * @return a string, which contains visibility value of uml class
     */
    public static String getVisibilityValue(Object umlObject) {
        if (umlObject != null) {
            Object kind = Model.getFacade().getVisibility(umlObject);
            if (kind == null
                    || kind.equals(
                            Model.getVisibilityKind().getPublic())) {

                return ActionSetModelElementVisibility.PUBLIC_COMMAND;
            } else if (kind.equals(
                    Model.getVisibilityKind().getPackage())) {

                return ActionSetModelElementVisibility.PACKAGE_COMMAND;
            } else if (kind.equals(
                    Model.getVisibilityKind().getProtected())) {

                return ActionSetModelElementVisibility.PROTECTED_COMMAND;
            } else if (kind.equals(
                    Model.getVisibilityKind().getPrivate())) {

                return ActionSetModelElementVisibility.PRIVATE_COMMAND;
             }
        }
        return "";
    }

    /**
     * Generates info of the class diagram
     *
     * @param document current document instance
     * @param section  an instance of <i>Section</i> class, where diagram info will be situated
     * @param diagram  an instance of <i>UMLClassDiagram</i> class, which info will be generated
     * @param generateDiagrams defines, whether diagram image should be generated
     */
    public static void generateDiagramInfo(Document document, Section section, UMLClassDiagram diagram, boolean generateDiagrams) {
        if(diagram == null) return;
        LOG.debug("generate info of class diagram: " + diagram.getName());

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
            ReportUtils.adjustImageSizeToDocumentPageSize(im, document);
            section.add(Chunk.NEWLINE);
            section.add(new Chunk(im, 0, 0, true));
            section.add(Chunk.NEWLINE);
        }

        generateSummaryInfo(section, diagram);
        generateDetailedInfo(document, section, diagram, generateDiagrams);

        if(addSection) {
            try {
                document.add(section);
            } catch(DocumentException ex) {
                LOG.debug(ex.getMessage());
            }
        }
    }

    /**
     * Generates class diagram summary info
     *
     * @param section section, to which summary info block will be added
     * @param diagram current diagarm, which summary info will be generated
     */
    public static void generateSummaryInfo(Section section, UMLClassDiagram diagram) {
        if(diagram == null) return;

        PdfPTable table = generateClassesSummaryInfo(diagram);

        if(table != null) {
            Section subSect = section.addSection(ReportUtils.generateTitle(
                                                 Translator.localize("argopdf.report.summary"), 2, true),
                                                 0);

            subSect.add(table);
        }

    }

    /**
     * Generates class diagram detailed info
     *
     * @param document current document instance
     * @param section section, to which detailed info block will be added
     * @param diagram current diagarm, which detailed info will be generated
     * @param generateDiagrams defines, whether diagram image should be generated
     */
    public static void generateDetailedInfo(Document document, Section section, UMLClassDiagram diagram, boolean generateDiagrams) {
        if(diagram == null) return;

        ArrayList elements = ClassDiagramHelper.getClassDiagramUmlElements(diagram);

        if(elements.size() > 0) {
            generateClassesDetailedInfo(document, section, diagram, elements, generateDiagrams);
        }

    }

    /**
     * Returns all classes of the class diagram
     *
     * @param diagram class diagram which classes will be found
     * @return ArrayList of classes of class diagram
     */
    private static ArrayList getClassDiagramUmlElements(UMLClassDiagram diagram) {
        java.util.List nodes = diagram.getNodes();
        ArrayList classes = new ArrayList();

        for (Object node : nodes) {
            if (Model.getFacade().isAClass(node)) {
                classes.add(node);
            } else if(Model.getFacade().isAInterface(node)) {
                classes.add(node);
            } else if(Model.getFacade().isAEnumeration(node)) {
                classes.add(node);
            }
        }

        return classes;
    }

    /**
     * Generates summary info of class diagram
     *
     * @param diagram an instance of current class diagram
     * @return an instance of <i>PdfPTable</i> class with summary info
     *         null, if diagram does not have any components
     */
    private static PdfPTable generateClassesSummaryInfo(UMLClassDiagram diagram) {
        ArrayList elements = ClassDiagramHelper.getClassDiagramUmlElements(diagram);

        return TableUtils.generateElementsInfo(elements);
    }

    /**
     * Generates detailed info of every component of class diagram
     *
     * @param document current document instance
     * @param section  section where diagram detailed info will be situated
     * @param diagram  an instance of current class diagram
     * @param elements elements of current class diagram. If null, elements will be collected in the method
     * @param generateDiagrams defines, whether diagram image should be generated
     */
    private static void generateClassesDetailedInfo(Document document, Section section, UMLClassDiagram diagram, ArrayList elements, boolean generateDiagrams) {
        if(elements == null) {
            elements = ClassDiagramHelper.getClassDiagramUmlElements(diagram);
        }

        if(elements.size() > 0) {
            Section subSect = section.addSection(ReportUtils.generateTitle(
                                                     Translator.localize("argopdf.report.details"), 2, true),
                                                     0);
            for(Object el : elements) {
                if(Model.getFacade().isAClass(el)) {
                    generateClassDetailedInfo(document, subSect, (UmlClass)el, generateDiagrams);
                } else if(Model.getFacade().isAInterface(el)) {
                    generateInterfaceDetailedInfo(subSect, (Interface)el);
                } else if(Model.getFacade().isAEnumeration(el)) {
                    generateEnumerationDetailedInfo(subSect, (Enumeration)el);
                }
                subSect.add(Chunk.NEWLINE);
            }
        }

    }

    /**
     * Generates detailed info of class of class diagram
     *
     * @param document current document instance
     * @param section  section where detailed info will be situated
     * @param umlClass an instance of <i>UmlClass</i>
     * @param generateDiagrams defines, whether diagram image should be generated
     */
    private static void generateClassDetailedInfo(Document document, Section section, UmlClass umlClass, boolean generateDiagrams) {
        if(umlClass == null) return;

        Section subSect = section.addSection("", 0);
        subSect.setBookmarkTitle(ReportUtils.getElementName(umlClass));
        
        Paragraph details = new Paragraph(ReportUtils.createImageLabelPhrase(ReportUtils.getImageName(umlClass), null, ReportUtils.getAnchorElementName(umlClass), 2));
        subSect.add(details);

        createClassModifiersInfo(subSect, umlClass);
        createClassAttributesInfo(subSect, umlClass);
        createClassOperationsInfo(subSect, umlClass);
        generateRelationshipsInfo(subSect, umlClass);
        generateStateChartDiagrams(document, subSect, umlClass, generateDiagrams);
    }

    /**
     * Generates detailed info of interface of class diagram
     *
     * @param section section where detailed info will be situated
     * @param interf  an instance of <i>Interface</i> class
     */
    private static void generateInterfaceDetailedInfo(Section section, Interface interf) {
        if(interf == null) return;

        Section subSect = section.addSection("", 0);
        subSect.setBookmarkTitle(ReportUtils.getElementName(interf));

        Paragraph details = new Paragraph(ReportUtils.createImageLabelPhrase(ReportUtils.getImageName(interf), null, ReportUtils.getAnchorElementName(interf), 2));
        subSect.add(details);

        createClassModifiersInfo(subSect, interf);
        createClassOperationsInfo(subSect, interf);
        generateRelationshipsInfo(subSect, interf);
    }

    /**
     * Generates detailed info of enumeration of class diagram
     *
     * @param section     section where detailed info will be situated
     * @param enumeration an instance of <i>Enumeration</i> class
     */
    private static void generateEnumerationDetailedInfo(Section section, Enumeration enumeration) {
        if(enumeration == null) return;

        Section subSect = section.addSection("", 0);
        subSect.setBookmarkTitle(ReportUtils.getElementName(enumeration));

        Paragraph details = new Paragraph(ReportUtils.createImageLabelPhrase(ReportUtils.getImageName(enumeration), null, ReportUtils.getAnchorElementName(enumeration), 2));
        subSect.add(details);

        createClassModifiersInfo(subSect, enumeration);
        createEnumerationLiterals(subSect, enumeration);
        createClassOperationsInfo(subSect, enumeration);
        generateRelationshipsInfo(subSect, enumeration);
    }

    /**
     * Generates info of modifiers of uml class, interface
     *
     * @param section section where modifiers info will be situated
     * @param umlObject an instance of uml object: <i>umlClass</i>, <i>Interface<i/>, <i>Enumeration</i>
     */
    private static void createClassModifiersInfo(Section section, Object umlObject) {
        if(!Model.getFacade().isAClass(umlObject) && !Model.getFacade().isAInterface(umlObject) &&
           !Model.getFacade().isAEnumeration(umlObject)) {
            return;
        }
        PdfPTable table = TableUtils.generateTableCap(2);
        try {
            table.setWidths(new float[]{1f, 3f});

            table.addCell(TableUtils.createHeaderCellWithFont(Translator.localize("argopdf.report.property")));
            table.addCell(TableUtils.createHeaderCellWithFont(Translator.localize("argopdf.report.value")));

            String values = "";
            if(Model.getFacade().isAClass(umlObject)) {
                values = getClassModifiersInfo(umlObject);
            } else if(Model.getFacade().isAInterface(umlObject)) {
                values = ReportUtils.createCommonModifiersString(umlObject);
            }

            if(!"".equals(values)) {
                table.addCell(TableUtils.createCell(Translator.localize("argopdf.report.modifiers")));
                table.addCell(TableUtils.createCell(values));
            }

            table.addCell(TableUtils.createCell(Translator.localize("argopdf.report.visibility")));
            table.addCell(TableUtils.createCell(getVisibilityValue(umlObject)));

        } catch(DocumentException ex) {
            LOG.debug(ex.getMessage());
        }

        Section subSect = section.addSection("", 0);
        subSect.setBookmarkTitle(Translator.localize("argopdf.report.properties"));

        Paragraph paragraph = new Paragraph();
        paragraph.add(ReportUtils.generateTitle(Translator.localize("argopdf.report.properties"), 3, false));
        paragraph.add(table);
        subSect.add(paragraph);
    }

    /**
     * Generates info of modifiers of uml class. This info is presented as a string
     * with a comma separator.
     *
     * @param umlObject an instance of uml object: <i>UmlClass</i>, <i>AssociationClass</i>
     * @return a string, which contains elements modifiers info
     */
    public static String getClassModifiersInfo(Object umlObject) {
        String values = ReportUtils.createCommonModifiersString(umlObject);

        if(Model.getFacade().isActive(umlObject)) {
            if(!"".equals(values)) values += ", ";
            values += Translator.localize("argopdf.report.active");
        }

        return values;
    }


    /**
     * Generates info of attributes of uml class
     *
     * @param section section where attributes info will be situated
     * @param umlClass an instance of <i>UmlClass</i>
     */
    public static void createClassAttributesInfo(Section section, UmlClass umlClass) {
        PdfPTable table = TableUtils.generateTableCap(2);

        try {
            table.setWidths(new float[]{1f, 3f});

            table.addCell(TableUtils.createHeaderCellWithFont(Translator.localize("argopdf.report.attribute")));
            table.addCell(TableUtils.createHeaderCellWithFont(Translator.localize("argopdf.report.documentation")));

            List attributes = Model.getFacade().getAttributes(umlClass);
            if(attributes.size() == 0) {
                return;
            }

            for(Object el : attributes) {
                Attribute attr = (Attribute)el;

                if(Model.getFacade().getType(attr) != null) {
                    table.addCell(TableUtils.createCell(attr.getName() + " : " +
                                                        Model.getFacade().getName(Model.getFacade().getType(attr))));
                } else {
                    table.addCell(TableUtils.createCell(attr.getName()));
                }
                table.addCell(TableUtils.createCell(ReportUtils.getElementsDocumentation(attr)));
            }

        } catch(DocumentException ex) {
            LOG.debug(ex.getMessage());
        }

        Section subSect = section.addSection("", 0);
        subSect.setBookmarkTitle(Translator.localize("argopdf.report.attributes"));

        Paragraph paragraph = new Paragraph();
        paragraph.add(Chunk.NEWLINE);
        paragraph.add(ReportUtils.generateTitle(Translator.localize("argopdf.report.attributes"), 3, false));
        paragraph.add(table);
        subSect.add(paragraph);
    }

    /**
     * Creates info of operations of uml class diagram
     *
     * @param section section where operations info will be situated
     * @param umlObject an instance of uml object: <i>umlClass</i>, <i>Interface<i/>, <i>Enumeration</i>
     */
    public static void createClassOperationsInfo(Section section, Object umlObject) {
        List operations = Model.getFacade().getOperations(umlObject);
        if(operations.size() == 0) {
            return;
        }

        PdfPTable table = TableUtils.generateTableCap(2);

        try {
            table.setWidths(new float[]{2f, 3f});

            table.addCell(TableUtils.createHeaderCellWithFont(Translator.localize("argopdf.report.operation")));
            table.addCell(TableUtils.createHeaderCellWithFont(Translator.localize("argopdf.report.documentation")));

            for(Object el : operations) {
                Operation op = (Operation)el;

                String operation = generatesOperationString(op);

                Font operationFont = new Font(Font.HELVETICA, 12);
                if(Model.getFacade().isAbstract(op)) {
                    operationFont.setStyle(Font.ITALIC);
                }
                try {
                    if(Model.getFacade().isStatic(op)) {
                        operationFont.setStyle(Font.UNDERLINE);
                    }
                } catch(NullPointerException ex) {
                    LOG.debug("Operation is not static");
                }

                table.addCell(TableUtils.createCell(operation, 1, null, operationFont));
                table.addCell(TableUtils.createCell(ReportUtils.getElementsDocumentation(op), 1, null, null));
            }

        } catch(DocumentException ex) {
            LOG.debug(ex.getMessage());
        }
        Section subSect = section.addSection("", 0);
        subSect.setBookmarkTitle(Translator.localize("argopdf.report.operations"));

        Paragraph paragraph = new Paragraph();
        paragraph.add(Chunk.NEWLINE);
        paragraph.add(ReportUtils.generateTitle(Translator.localize("argopdf.report.operations"), 3, false));
        paragraph.add(table);
        subSect.add(paragraph);
    }

    /**
     * Creates info of literals of enumeration
     *
     * @param section   section where operations info will be situated
     * @param umlObject an instance of uml object: <i>Enumeration</i>
     */
    public static void createEnumerationLiterals(Section section, Object umlObject) {
        PdfPTable table = TableUtils.generateTableCap(2);

        List literals = Model.getFacade().getEnumerationLiterals(umlObject);
        if(literals.size() == 0) {
            return;
        }

        try {
            table.setWidths(new float[]{2f, 3f});

            table.addCell(TableUtils.createHeaderCellWithFont(Translator.localize("argopdf.report.literal")));
            table.addCell(TableUtils.createHeaderCellWithFont(Translator.localize("argopdf.report.documentation")));

            for(Object el : literals) {
                EnumerationLiteral elit = (EnumerationLiteral)el;
                table.addCell(TableUtils.createCell(ReportUtils.getElementName(elit)));
                table.addCell(TableUtils.createCell(ReportUtils.getElementsDocumentation(elit)));
            }

            Section subSect = section.addSection("", 0);
            subSect.setBookmarkTitle(Translator.localize("argopdf.report.literals"));

            Paragraph paragraph = new Paragraph();
            paragraph.add(Chunk.NEWLINE);
            paragraph.add(ReportUtils.generateTitle(Translator.localize("argopdf.report.literals"), 3, false));
            paragraph.add(table);
            subSect.add(paragraph);
        } catch(DocumentException ex) {
            LOG.debug(ex.getMessage());
        }
    }

    /**
     * Generates string representative of class operation
     *
     * @param op uml class operation
     * @return string representative of class operation
     */
    public static String generatesOperationString(Operation op) {
        Collection parameters = Model.getFacade().getParameters(op);
        String operation = op.getName()+"(";
        String postfix = "";
        for(Object el2 : parameters) {
            String kind = getParameterKind((Parameter)el2);

            if(ActionSetParameterDirectionKind.RETURN_COMMAND.equals(kind)) {
                Object type = Model.getFacade().getType(el2);
                if(type != null) {
                    postfix += " : " + ((Classifier)type).getName();
                }
            } else {
                if(ActionSetParameterDirectionKind.OUT_COMMAND.equals(kind) ||
                   ActionSetParameterDirectionKind.INOUT_COMMAND.equals(kind)) {
                    operation += kind + " ";
                }
                operation += ((Parameter)el2).getName();

                Object type = Model.getFacade().getType(el2);
                operation += " : " + ((Classifier)type).getName();
                String defValue = ((Expression)Model.getFacade().getDefaultValue(el2)).getBody();
                if(defValue != null && !"".equals(defValue)) {
                    operation += " = " + defValue;
                }
            }

            //todo explore, can be more than one????
            break;
        }
        operation += ")" + postfix;

        return operation;
    }

    /**
     * Generates relationships info of uml class.
     *
     * @param section   section where relationships info will be situated
     * @param umlObject an instance of uml object: <i>UmlClass</i>, <i>Interface</i>, <i>Enumeration</i>
     */
    private static void generateRelationshipsInfo(Section section, Object umlObject) {
        if(umlObject == null) return;

        if(Model.getFacade().isAClass(umlObject)) {
            generateClassDependenciesInfo(section, umlObject);
        }
        generateClassGeneralizationsInfo(section, umlObject);
        generateClassAssosiationsInfo(section, umlObject);
    }

    /**
     * Generates state chart diagrams of uml class
     *
     * @param document  current document instance
     * @param section   section where relationships info will be situated
     * @param umlClass  an instance of <i>UmlClass</i>
     * @param generateDiagrams defines, whether diagram image should be generated
     */
    private static void generateStateChartDiagrams(Document document, Section section, UmlClass umlClass, boolean generateDiagrams) {
        StateChartDiagramHelper.generateStateChartDiagrams(document, section, umlClass, generateDiagrams);
    }

    /**
     * Generates dependencies info of uml class
     *
     * @param section  section where dependencies info will be situated
     * @param umlObject an instance of uml object: <i>UmlClass</i>
     */
    private static void generateClassDependenciesInfo(Section section, Object umlObject) {
        if(!Model.getFacade().isAClass(umlObject)) return;
        Collection clientDepend = Model.getFacade().getClientDependencies(umlObject);
        Collection supplierDepend = Model.getFacade().getSupplierDependencies(umlObject);

        if(clientDepend.size() > 0 || supplierDepend.size() > 0) {
            Section subSect = section.addSection("", 0);
            subSect.setBookmarkTitle(Translator.localize("argopdf.report.dependencies"));

            if(clientDepend.size() > 0) {
                Paragraph paragraph = new Paragraph();
                paragraph.add(Chunk.NEWLINE);
                paragraph.add(ReportUtils.generateTitle(Translator.localize("argopdf.report.depend_on"), 3, false));
                paragraph.add(generateClientDependenciesInfo((UmlClass)umlObject, clientDepend));
                subSect.add(paragraph);
            }

            if(supplierDepend.size() > 0) {
                Paragraph paragraph = new Paragraph();
                paragraph.add(Chunk.NEWLINE);
                paragraph.add(ReportUtils.generateTitle(Translator.localize("argopdf.report.directs"), 3, false));
                paragraph.add(generateSupplierDependenciesInfo((UmlClass)umlObject, supplierDepend));
                subSect.add(paragraph);
            }

        }

    }

    /**
     * Generates class generalizations and specializations info.
     *
     * @param section  section where dependencies info will be situated
     * @param umlObject an instance of uml object: <i>UmlClass</i>, <i>Interface</i>
     */
    private static void generateClassGeneralizationsInfo(Section section, Object umlObject) {
        Collection generalizations = Model.getFacade().getGeneralizations(umlObject);
        Collection specializations  = Model.getFacade().getSpecializations(umlObject);

        if(generalizations.size() > 0 || specializations.size() > 0) {

            if(generalizations.size() > 0) {
                Section subSect = section.addSection("", 0);
                subSect.setBookmarkTitle(Translator.localize("argopdf.report.generalizations"));
                Paragraph paragraph = new Paragraph();
                paragraph.add(Chunk.NEWLINE);
                paragraph.add(ReportUtils.generateTitle(Translator.localize("argopdf.report.generalizes"), 3, false));
                paragraph.add(generateGeneralizationsInfo(umlObject, generalizations));
                subSect.add(paragraph);                
            }

            if(specializations.size() > 0) {
                Section subSect = section.addSection("", 0);
                subSect.setBookmarkTitle(Translator.localize("argopdf.report.specializations"));
                Paragraph paragraph = new Paragraph();
                paragraph.add(Chunk.NEWLINE);
                paragraph.add(ReportUtils.generateTitle(Translator.localize("argopdf.report.specializes"), 3, false));
                paragraph.add(generateSpecializationsInfo(umlObject, specializations));
                subSect.add(paragraph);
            }
        }
    }

    /**
     * Generates class assosiations info
     *
     * @param section  section where dependencies info will be situated
     * @param umlObject an instance of uml object: <i>UmlClass</i>, <i>Interface</i>
     */
    private static void generateClassAssosiationsInfo(Section section, Object umlObject) {
        Collection assEnds = Model.getFacade().getAssociationEnds(umlObject);

        if(assEnds.size() > 0) {
            Section subSect = section.addSection("", 0);
            subSect.setBookmarkTitle(Translator.localize("argopdf.report.associations"));

            Paragraph par = new Paragraph();
            par.add(Chunk.NEWLINE);
            par.add(ReportUtils.generateTitle(Translator.localize("argopdf.report.associations"), 3, false));

            for(Object el : assEnds) {
                if(Model.getFacade().isAAssociationEnd(el)) {
                    par.add(generateAssociatedElementsInfo(el));
                }
            }
            subSect.add(par);
        }
    }

    /**
     * Generates info of client dependencies info.
     *
     * @param umlClass an instance of <i>UmlClass</i>
     * @param clientDepend collection which contains client dependencies
     * @return an instance of <i>PdfPTable</i> where client dependecies info is situated
     */
    private static PdfPTable generateClientDependenciesInfo(UmlClass umlClass, Collection clientDepend) {
        if(clientDepend == null) {
            clientDepend = Model.getFacade().getClientDependencies(umlClass);
        }
        PdfPTable table = TableUtils.generateTableCap(2);
        try {
            table.setWidths(new float[]{2f, 3f});
            table.addCell(TableUtils.createHeaderCellWithFont(Translator.localize("argopdf.report.name")));
            table.addCell(TableUtils.createHeaderCellWithFont(Translator.localize("argopdf.report.dependency_documentation")));

            for(Object el : clientDepend) {
                if(Model.getFacade().isADependency(el)) {
                    Collection suppliers = Model.getFacade().getSuppliers(el);
                    if(suppliers.size() > 0) {
                        for (Object supplier : suppliers) {
                            if (Model.getFacade().isAClass(supplier) || Model.getFacade().isAInterface(supplier)) {
                                Anchor anchor = ReportUtils.getElementNameWithReference(supplier);
                                String imageName = "";
                                if(Model.getFacade().isAClass(supplier)) imageName = "class";
                                if(Model.getFacade().isAInterface(supplier)) imageName = "interface";
                                table.addCell(TableUtils.createImageLabelTable(null, anchor, imageName, null, new float[]{1, 7}, null));
                                table.addCell(ReportUtils.getElementsDocumentation(el));
                            }
                        }
                    }
                }
            }

        } catch(DocumentException ex) {
            LOG.debug(ex.getMessage());
        }

        return table;
    }

    /**
     * Generates info of supplier dependencies info.
     *
     * @param umlClass an instance of <i>UmlClass</i>
     * @param supplierDepend collection which contains supplier dependencies
     * @return an instance of <i>PdfPTable</i> where supplier dependecies info is situated
     */
    private static PdfPTable generateSupplierDependenciesInfo(UmlClass umlClass, Collection supplierDepend) {
        if(supplierDepend == null) {
            supplierDepend = Model.getFacade().getSupplierDependencies(umlClass);
        }
        PdfPTable table = TableUtils.generateTableCap(2);
        try {
            table.setWidths(new float[]{2f, 3f});
            table.addCell(TableUtils.createHeaderCellWithFont(Translator.localize("argopdf.report.name")));
            table.addCell(TableUtils.createHeaderCellWithFont(Translator.localize("argopdf.report.dependency_documentation")));

            for(Object el : supplierDepend) {
                if(Model.getFacade().isADependency(el)) {
                    Collection clients = Model.getFacade().getClients(el);
                    if(clients.size() > 0) {
                        for(Object client : clients) {
                            if (Model.getFacade().isAClass(client) || Model.getFacade().isAInterface(client)) {
                                Anchor anchor = ReportUtils.getElementNameWithReference(client);
                                String imageName = "";
                                if(Model.getFacade().isAClass(client)) imageName = "class";
                                if(Model.getFacade().isAInterface(client)) imageName = "interface";
                                table.addCell(TableUtils.createImageLabelTable(null, anchor, imageName, null, new float[]{1, 7}, null));
                                table.addCell(ReportUtils.getElementsDocumentation(el));
                            }
                        }
                    }
                }
            }
        } catch(DocumentException ex) {
            LOG.debug(ex.getMessage());
        }

        return table;
    }

    /**
     * Generates info of generalizations info.
     *
     * @param umlObject an instance of uml object: <i>UmlClass</i>, <i>Interface</i>
     * @param generalizations collection which contains generalizations
     * @return an instance of <i>PdfPTable</i> where generalizations info is situated
     */
    private static PdfPTable generateGeneralizationsInfo(Object umlObject, Collection generalizations) {
        if(generalizations == null) {
            generalizations = Model.getFacade().getGeneralizations(umlObject);
        }
        PdfPTable table = TableUtils.generateTableCap(2);
        try {
            table.setWidths(new float[]{2f, 3f});
            table.addCell(TableUtils.createHeaderCellWithFont(Translator.localize("argopdf.report.name")));
            table.addCell(TableUtils.createHeaderCellWithFont(Translator.localize("argopdf.report.generalization_documentation")));

            for(Object el : generalizations) {
                if(Model.getFacade().isAGeneralization(el)) {
                    Object parent = Model.getFacade().getParent(el);
                    if(Model.getFacade().isAClass(parent) || Model.getFacade().isAInterface(parent) ||
                       Model.getFacade().isAEnumeration(parent)) {
                        Anchor anchor = ReportUtils.getElementNameWithReference(parent);
                        table.addCell(TableUtils.createImageLabelTable(null, anchor, ReportUtils.getImageName(parent), null, new float[]{1, 7}, null));
                        table.addCell(ReportUtils.getElementsDocumentation(el));
                    }
                }
            }
        } catch(DocumentException ex) {
            LOG.debug(ex.getMessage());
        }

        return table;
    }

    /**
     * Generates info of specializations info
     *
     * @param umlObject an instance of uml object: <i>UmlClass</i>, <i>Interface</i>
     * @param specializations collection which contains specializations
     * @return an instance of <i>PdfPTable</i> where specializations info is situated
     */
    private static PdfPTable generateSpecializationsInfo(Object umlObject, Collection specializations) {
        if(specializations == null) {
            specializations = Model.getFacade().getSpecifications(umlObject);
        }
        PdfPTable table = TableUtils.generateTableCap(2);
        try {
            table.setWidths(new float[]{2f, 3f});
            table.addCell(TableUtils.createHeaderCellWithFont(Translator.localize("argopdf.report.name")));
            table.addCell(TableUtils.createHeaderCellWithFont(Translator.localize("argopdf.report.specialization_documentation")));

            for(Object el : specializations) {
                if(Model.getFacade().isAGeneralization(el)) {
                    Object child = Model.getFacade().getChild(el);
                    if(Model.getFacade().isAClass(child) || Model.getFacade().isAInterface(child) ||
                       Model.getFacade().isAEnumeration(child)) {
                        Anchor anchor = ReportUtils.getElementNameWithReference(child);
                        table.addCell(TableUtils.createImageLabelTable(null, anchor, ReportUtils.getImageName(child), null, new float[]{1, 7}, null));
                        table.addCell(ReportUtils.getElementsDocumentation(el));
                    }
                }
            }
        } catch(DocumentException ex) {
            LOG.debug(ex.getMessage());
        }

        return table;
    }

    /**
     * Generates info of assosiation relationship
     *
     * @param assEnd an instance of object which represents assosiation
     * @return an instance of <i>PdfPTable</i> where assosiation relationship info is situated
     */
    private static PdfPTable generateAssociatedElementsInfo(Object assEnd) {
        PdfPTable table = TableUtils.generateTableCap(2);
        try {
            table.setWidths(new float[]{2f, 6f});
        } catch(DocumentException ex) {
            LOG.debug(ex.getMessage());
        }
        if(Model.getFacade().isAAssociationEnd(assEnd)) {
            Object assosiation = Model.getFacade().getAssociation(assEnd);
            String assName = ReportUtils.getElementName(assosiation) + " : " + Translator.localize("argopdf.report.association");

            table.addCell(TableUtils.createHeaderCellWithFont(assName, 2));

            table.addCell(TableUtils.createCell(Translator.localize("argopdf.report.modifiers")));
            table.addCell(TableUtils.createCell(ReportUtils.createCommonModifiersString(assosiation)));

            Collection connection = Model.getFacade().getConnections(assosiation);
            for(Object el : connection) {
                Object type = Model.getFacade().getType(el);
                if(Model.getFacade().isAClass(type) || Model.getFacade().isAInterface(type) ||
                   Model.getFacade().isAEnumeration(type)) {
                    table.addCell(TableUtils.createCell("   ", 2));
                    PdfPTable assToTable = ReportUtils.generateAssosiatedEndInfo(el);
                    PdfPCell cell2 = new PdfPCell(assToTable);
                    cell2.setColspan(2);
                    table.addCell(cell2);
                }
            }

            if(Model.getFacade().isAAssociationClass(assosiation)) {
                table.addCell(TableUtils.createCell("   ", 2));
                table.addCell(TableUtils.createCell(Translator.localize("argopdf.report.association_class")));
                table.addCell(TableUtils.createCell(ReportUtils.getElementNameWithReference(assosiation), 2, null, "class", null, new float[]{1, 20}));
            }
        }

        return table;
    }

}
