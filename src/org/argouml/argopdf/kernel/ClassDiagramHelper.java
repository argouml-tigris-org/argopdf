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
import org.argouml.uml.diagram.static_structure.ui.UMLClassDiagram;
import org.argouml.uml.ui.foundation.core.ActionSetModelElementVisibility;
import org.argouml.uml.ui.foundation.core.ActionSetParameterDirectionKind;
import org.argouml.uml.ui.UMLExpressionModel2;
import org.argouml.i18n.Translator;
import org.argouml.model.Model;
import org.argouml.model.Facade;
import org.apache.log4j.Logger;
import org.omg.uml.foundation.core.*;
import org.omg.uml.foundation.datatypes.Expression;

import java.util.*;
import java.util.List;
import java.awt.*;

/**
 * Contains helper methods for processing class diagrams
 *
 * @author Dmitry Churbanau
 * @version 0.1
 */
public class ClassDiagramHelper {

    private static final Logger LOG = Logger.getLogger(ClassDiagramHelper.class);

    /**
     * Generates info of the class diagram
     *
     * @param document current document instance
     * @param section  an instance of <i>Section</i> class, where diagram info will be situated
     * @param diagram  an instance of <i>UMLClassDiagram</i> class, which info will be generated
     */
    public static void generateDiagramInfo(Document document, Section section, UMLClassDiagram diagram) {
        if(diagram == null) return;
        LOG.debug("generate info of class diagram: " + diagram.getName());

        Paragraph title = ReportUtils.generateTitle(ReportUtils.getElementName(diagram), 1, false);
        section = section.addSection(title, section.depth() + 1);

        Image im = ReportUtils.makeImageOfDiagram(diagram);
        if(im != null) {
            ReportUtils.adjustImageSizeToDocumentPageSize(im,  document);
            section.add(Chunk.NEWLINE);
            section.add(new Chunk(im, 0, 0, true));
            section.add(Chunk.NEWLINE);
        }

        generateSummaryInfo(section, diagram);
        generateDetailedInfo(section, diagram);
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
     * @param section section, to which detailed info block will be added
     * @param diagram current diagarm, which detailed info will be generated
     */
    public static void generateDetailedInfo(Section section, UMLClassDiagram diagram) {
        if(diagram == null) return;

        ArrayList elements = ClassDiagramHelper.getClassDiagramClasses(diagram);

        if(elements.size() > 0) {
            generateClassesDetailedInfo(section, diagram, elements);
        }

    }

    /**
     * Returns all classes of the class diagram
     *
     * @param diagram class diagram which classes will be found
     * @return ArrayList of classes of class diagram
     */
    private static ArrayList getClassDiagramClasses(UMLClassDiagram diagram) {
        java.util.List nodes = diagram.getNodes();
        ArrayList classes = new ArrayList();

        for (Object node : nodes) {
            if (Model.getFacade().isAClass(node)) {
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
        ArrayList elements = ClassDiagramHelper.getClassDiagramClasses(diagram);

        return ReportUtils.generateElementsInfo(elements);
    }

    /**
     * Generates detailed info of every component of class diagram
     *
     * @param section  section where diagram detailed info will be situated
     * @param diagram  an instance of current class diagram
     * @param elements elements of current class diagram. If null, elements will be collected in the method
     */
    private static void generateClassesDetailedInfo(Section section, UMLClassDiagram diagram, ArrayList elements) {
        if(elements == null) {
            elements = ClassDiagramHelper.getClassDiagramClasses(diagram);
        }

        if(elements.size() > 0) {
            Section subSect = section.addSection(ReportUtils.generateTitle(
                                                     Translator.localize("argopdf.report.details"), 2, true),
                                                     0);
            for(Object el : elements) {
                if(Model.getFacade().isAClass(el)) {
                    generateClassDetailedInfo(subSect, (UmlClass)el);
                }
            }
        }

    }

    /**
     * Generates detailed info of class of class diagram
     *
     * @param section
     * @param umlClass
     */
    private static void generateClassDetailedInfo(Section section, UmlClass umlClass) {
        if(umlClass == null) return;

        Section subSect = section.addSection("", 0);
        subSect.setBookmarkTitle(ReportUtils.getElementName(umlClass));
        
        Paragraph details = new Paragraph(ReportUtils.createImageLabelPhrase(ReportUtils.getImageName(umlClass), null, ReportUtils.getAnchorElementName(umlClass), 2));
        details.add(Chunk.NEWLINE);
        details.add(createClassModifiersInfo(umlClass));
        details.add(createClassAttributesInfo(umlClass));
        details.add(createClassOperationsInfo(umlClass));
        
        subSect.add(details);
    }

    /**
     * Generates info of modifiers of uml class
     *
     * @param umlClass
     * @return
     */
    private static Paragraph createClassModifiersInfo(UmlClass umlClass) {
        PdfPTable table = ReportUtils.generatedTableCap(2);
        try {
            table.setWidths(new float[]{1f, 3f});

            Font captionFont = new Font(Font.HELVETICA, 12, Font.BOLD, Color.WHITE);
            table.addCell(ReportUtils.createCell(Translator.localize("argopdf.report.property"),
                                     1, ReportUtils.TABLE_HEADER_COLOR, captionFont));
            table.addCell(ReportUtils.createCell(Translator.localize("argopdf.report.value"),
                                     1, ReportUtils.TABLE_HEADER_COLOR, captionFont));

            String values = getClassModifiersInfo(umlClass);

            if(!"".equals(values)) {
                table.addCell(ReportUtils.createCell(Translator.localize("argopdf.report.modifiers")));
                table.addCell(ReportUtils.createCell(values));
            }

            table.addCell(ReportUtils.createCell(Translator.localize("argopdf.report.visibility")));
            table.addCell(ReportUtils.createCell(ClassDiagramHelper.getVisibilityValue(umlClass)));

        } catch(DocumentException ex) {
            LOG.debug(ex.getMessage());
        }

        Paragraph paragraph = new Paragraph();
        paragraph.add(Chunk.NEWLINE);
        paragraph.add(ReportUtils.generateTitle(Translator.localize("argopdf.report.properties"), 4, false));
        paragraph.add(table);

        return paragraph;
    }

    /**
     * Generates info of modifiers of uml class
     *
     * @param umlClass
     * @return
     */
    public static String getClassModifiersInfo(UmlClass umlClass) {
        String values = ReportUtils.createCommonModifiersString(umlClass);

        if(Model.getFacade().isActive(umlClass)) {
            if(!"".equals(values.toString())) values += ", ";
            values += Translator.localize("argopdf.report.active");
        }

        return values;
    }

    /**
     * Generates string representative of visibility parameter of uml class
     *
     * @param umlClass
     * @return
     */
    public static String getVisibilityValue(UmlClass umlClass) {
        if (umlClass != null) {
            Object kind = Model.getFacade().getVisibility(umlClass);
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
     * Generates info of attributes of uml class
     *
     * @param umlClass
     * @return
     */
    public static Paragraph createClassAttributesInfo(UmlClass umlClass) {
        PdfPTable table = ReportUtils.generatedTableCap(2);

        try {
            table.setWidths(new float[]{1f, 3f});

            Font captionFont = new Font(Font.HELVETICA, 12, Font.BOLD, Color.WHITE);
            table.addCell(ReportUtils.createCell(Translator.localize("argopdf.report.attribute"),
                                     1, ReportUtils.TABLE_HEADER_COLOR, captionFont));
            table.addCell(ReportUtils.createCell(Translator.localize("argopdf.report.documentation"),
                                     1, ReportUtils.TABLE_HEADER_COLOR, captionFont));            

            List attributes = Model.getFacade().getAttributes(umlClass);
            if(attributes.size() == 0) {
                return new Paragraph("");
            }

            for(Object el : attributes) {
                Attribute attr = (Attribute)el;

                Object type = Model.getFacade().getType(attr);
                table.addCell(ReportUtils.createCell(attr.getName() + " : " + ((DataType)type).getName()));
                table.addCell(ReportUtils.createCell(ReportUtils.getElementDocumentation(attr)));
            }

        } catch(DocumentException ex) {
            LOG.debug(ex.getMessage());
        }
        Paragraph retPar = new Paragraph();
        retPar.add(ReportUtils.generateTitle(Translator.localize("argopdf.report.attributes"), 4, false));
        retPar.add(table);
        return retPar;
    }

    /**
     * Creates info of operations of uml class diagram
     *
     * @param umlClass
     * @return
     */
    public static Paragraph createClassOperationsInfo(UmlClass umlClass) {
        PdfPTable table = ReportUtils.generatedTableCap(2);

        try {
            table.setWidths(new float[]{2f, 3f});

            Font captionFont = new Font(Font.HELVETICA, 12, Font.BOLD, Color.WHITE);
            table.addCell(ReportUtils.createCell(Translator.localize("argopdf.report.operation"),
                                     1, ReportUtils.TABLE_HEADER_COLOR, captionFont));
            table.addCell(ReportUtils.createCell(Translator.localize("argopdf.report.documentation"),
                                     1, ReportUtils.TABLE_HEADER_COLOR, captionFont));

            List operations = Model.getFacade().getOperations(umlClass);
            if(operations.size() == 0) {
                return new Paragraph("");
            }

            for(Object el : operations) {
                Operation op = (Operation)el;

                String operation = generatesOperationString(op);

                Font operationFont = new Font(Font.HELVETICA, 12);
                if(Model.getFacade().isAbstract(op)) {
                    operationFont.setStyle(Font.ITALIC);
                }
                if(Model.getFacade().isStatic(op)) {
                    operationFont.setStyle(Font.UNDERLINE);
                }

                table.addCell(ReportUtils.createCell(operation, 1, null, operationFont));
                table.addCell(ReportUtils.createCell(ReportUtils.getElementDocumentation(op), 1, null, null));
            }

        } catch(DocumentException ex) {
            LOG.debug(ex.getMessage());
        }
        Paragraph retPar = new Paragraph();
        retPar.add(ReportUtils.generateTitle(Translator.localize("argopdf.report.operations"), 4, false));        
        retPar.add(table);
        return retPar;
    }

    /**
     * Generates string representative of class operation
     *
     * @param op class operation
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
                postfix += " : " + ((Classifier)type).getName();
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
}
