/* $Id$
 *****************************************************************************
 * Copyright (c) 2009 Contributors - see below
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    fpierre
 *****************************************************************************
 *
 * Some portions of this file was previously release using the BSD License:
 */

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

import org.argouml.uml.diagram.deployment.ui.UMLDeploymentDiagram;
import org.argouml.uml.diagram.activity.ui.UMLActivityDiagram;
import org.argouml.uml.diagram.collaboration.ui.UMLCollaborationDiagram;
import org.argouml.uml.diagram.sequence.ui.UMLSequenceDiagram;
import org.argouml.uml.diagram.state.ui.UMLStateDiagram;
import org.argouml.uml.diagram.ui.UMLDiagram;
import org.argouml.uml.diagram.static_structure.ui.UMLClassDiagram;
import org.argouml.uml.diagram.use_case.ui.UMLUseCaseDiagram;
import org.argouml.uml.ui.SaveGraphicsManager;
import org.argouml.uml.ui.foundation.core.ActionSetAssociationEndAggregation;
import org.argouml.ui.targetmanager.TargetManager;
import org.argouml.uml.diagram.ArgoDiagram;
import org.argouml.application.helpers.ResourceLoaderWrapper;
import org.argouml.configuration.Configuration;
import org.argouml.i18n.Translator;
import org.argouml.model.Model;
import org.argouml.model.Facade;
import org.argouml.argopdf.kernel.helpers.UseCasesDiagramHelper;
import org.tigris.gef.base.SaveGraphicsAction;
import org.tigris.gef.base.CmdSaveGraphics;
import org.apache.log4j.Logger;
import com.lowagie.text.*;
import com.lowagie.text.Image;
import com.lowagie.text.Font;
import com.lowagie.text.pdf.codec.PngImage;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;

import javax.swing.*;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.awt.*;
import java.util.ArrayList;

/**
 * Contains helper methods for report generation
 *
 * @author Dmitry Churbanau
 * @version 0.1
 */
public class ReportUtils {

    private static final Logger LOG = Logger.getLogger(ReportUtils.class);

    /**
     * Finds the image name of the uml object according to the instance of parameter class.
     *
     * @param umlObject an instance of uml object
     * @return the image name of uml object. If image name can not be found, returns empty string.
     */
    public static String getImageName(Object umlObject) {
        String imageName = "";
        if(Model.getFacade().isAUseCase(umlObject)) {
            imageName = "UseCase";
        } else if(Model.getFacade().isAActor(umlObject)) {
            imageName = "Actor";
        } else if(Model.getFacade().isAClass(umlObject)) {
            imageName = "Class";
        } else if(Model.getFacade().isAInterface(umlObject)) {
            imageName = "Interface";
        } else if(Model.getFacade().isAEnumeration(umlObject)) {
            imageName = "Enumeration";
        }


        return imageName;
    }

    /**
     * Returns element name. If element does not have a name, returns string 'Unnamed elementType',
     * where elemenType is a type of element: Actor, UseCase and etc. 
     *
     * @param element uml element
     * @return elements name, or string 'Unnamed elementType', if element does not have a name or it
     *         is empty
     */
    public static String getElementName(Object element) {
        String name;

        if(element instanceof UMLUseCaseDiagram       || element instanceof UMLClassDiagram    ||
           element instanceof UMLStateDiagram         || element instanceof UMLSequenceDiagram ||
           element instanceof UMLCollaborationDiagram || element instanceof UMLActivityDiagram ||
           element instanceof UMLDeploymentDiagram) {
            name = ((UMLDiagram)element).getName();
        } else {
            name = Model.getFacade().getName(element);
        }

        if("".equals(name) || name == null) {
            name = Translator.localize("argopdf.report.unnamed") + " ";

            if(Model.getFacade().isAActor(element)) {
                name += Translator.localize("argopdf.report.actor");
            } else if(Model.getFacade().isAUseCase(element)) {
                name += Translator.localize("argopdf.report.usecase");
            } else if(Model.getFacade().isAClass(element)) {
                name += Translator.localize("argopdf.report.class");
            } else if(Model.getFacade().isAInterface(element)) {
                name += Translator.localize("argopdf.report.interface");
            } else if(Model.getFacade().isAEnumeration(element)) {
                name += Translator.localize("argopdf.report.enumeration");
            } else if(Model.getFacade().isAEnumerationLiteral(element)) {
                name += Translator.localize("argopdf.report.literal");
            } else if(Model.getFacade().isAPackage(element)) {
                name += Translator.localize("argopdf.report.package");
            } else if(element instanceof UMLUseCaseDiagram) {
                name += Translator.localize("argopdf.report.diagram.usecase");
            } else if(element instanceof UMLClassDiagram) {
                name += Translator.localize("argopdf.report.diagram.class");
            } else if(element instanceof UMLStateDiagram) {
                name += Translator.localize("argopdf.report.diagram.statechart");
            } else if(element instanceof UMLSequenceDiagram) {
                name += Translator.localize("argopdf.report.diagram.sequence");
            } else if(element instanceof UMLCollaborationDiagram) {
                name += Translator.localize("argopdf.report.diagram.collaboration");
            } else if(element instanceof UMLActivityDiagram) {
                name += Translator.localize("argopdf.report.diagram.activity");
            } else if(element instanceof UMLDeploymentDiagram) {
                name += Translator.localize("argopdf.report.diagram.deployment");
            } else if(Model.getFacade().isADependency(element)) {
                name += Translator.localize("argopdf.report.dependency");
            } else if(Model.getFacade().isAAssociation(element)) {
                name += Translator.localize("argopdf.report.association");
            } else if(Model.getFacade().isAAssociationClass(element)) {
                name += Translator.localize("argopdf.report.association_class");
            }

        }

        return name;
    }

    /**
     * Generates element name with referenced anchor.
     *
     * @param element any uml element
     * @return an instance of <i>Anchor</i> class, which contains elements name and referenced anchor
     */
    public static Anchor getAnchorElementName(Object element) {
        String elementName = getElementName(element);
        Anchor anchor = new Anchor(elementName);
        anchor.setName(element.getClass() + "_" + element.hashCode());

        return anchor;
    }

    /**
     * Generates element name with reference to this element.
     *
     * @param element any uml element
     * @return an instance of <i>Anchor</i> class, which contains elements name and reference
     *         to this element
     */
    public static Anchor getElementNameWithReference(Object element) {
        String elementName = getElementName(element);
        Anchor anchor = new Anchor(elementName);

        //TODO check
        //anchor.setReference("#" + element.getClass() + "_" + element.hashCode());

        return anchor;
    }

    /**
     * Generates text font according to the depth parameter.
     *
     * @param depth parameter, according to which font size is determined.
     * @return an instance of <i>Font</i> class with appropriate size
     */
    public static Font getTextFont(int depth) {
        return new Font(Font.HELVETICA, depth == 0 ? 25 : depth == 1 ? 20 : depth == 2 ? 18 : depth == 3 ? 16 : depth == 4 ? 14 : 12, Font.BOLD);
    }

    /**
     * Generates title text.
     *
     * @param text       text of the title
     * @param depth      this parameter defines the size of the font
     * @param underline  defines, whether text will be underlined or not
     * @return           an instance of <i>Paragraph</i> class, which contains generated title text
     */
    public static Paragraph generateTitle(String text, int depth, boolean underline) {
        Chunk chunk = new Chunk(text);
        chunk.setFont(getTextFont(depth));
        if(underline) {
            chunk.setUnderline(1f, -1f);
        }

        return new Paragraph(chunk);
    }

    /**
     * Generates title text.
     *
     * @param anchor     anchor of the title
     * @param depth      this parameter defines the size of the font
     * @param underline  defines, whether text will be underlined or not
     * @return           an instance of <i>Paragraph</i> class, which contains generated title text
     */
    public static Paragraph generateTitle(Anchor anchor, int depth, boolean underline) {
        ArrayList chunks = anchor.getChunks();
        Font font = getTextFont(depth);
        for(Object el : chunks) {
            ((Chunk)el).setFont(font);
            if(underline) {
                ((Chunk)el).setUnderline(1f, -1f);
            }
        }

        Paragraph paragraph = new Paragraph(" ");
        paragraph.add(anchor);
        return paragraph;
    }

    /**
     * Returns documentation of element.
     *
     * @param element element, which documentation will be generated
     * @return documentation of element
     */
    public static String getElementsDocumentation(Object element) {
        Object taggedValue = Model.getFacade().getTaggedValue(element, "documentation");
        String documentation = "";
        if(taggedValue != null) {
            documentation = Model.getFacade().getValueOfTag(taggedValue);
        }

        return documentation;
    }

    /**
     * Creates an image of the diagram, which goes as parameter
     *
     * @param diagram an instance of <i>ArgoDiagram</i> class, which image needs to be generated
     * @return an instance of <i>Image</i> class, which contains an image of diagram
     */
    public static Image makeImageOfDiagram(ArgoDiagram diagram) {
        TargetManager tm = TargetManager.getInstance();
        tm.setTarget(diagram);

        //SaveGraphicsAction cmd = SaveGraphicsManager.getInstance().getSaveActionBySuffix("png");
        //CmdSaveGraphics sg = SaveGraphicsManager.getInstance().getSaveCommandBySuffix("png");
        SaveGraphicsAction sg = SaveGraphicsManager.getInstance().getSaveActionBySuffix("png");
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        //cmd.setStream(outputStream);
        sg.setStream(outputStream);
        //cmd.setScale(Configuration.getInteger(SaveGraphicsManager.KEY_GRAPHICS_RESOLUTION, 1));
        sg.setScale(Configuration.getInteger(SaveGraphicsManager.KEY_GRAPHICS_RESOLUTION, 1));
        //cmd.actionPerformed(null);
        sg.actionPerformed(null);

        Image im = null;
        try {
            im = PngImage.getImage(outputStream.toByteArray());
        } catch(IOException ex) {
            LOG.debug(ex.getMessage());
        }

        return im;
    }

    /**
     * Changes the image size, that it can be placed on one page
     *
     * @param image image, which size needs to change
     * @param document current document
     */
    public static void adjustImageSizeToDocumentPageSize(Image image, Document document) {
        if(image == null || document == null) return;

        float imageWidth = document.getPageSize().width() - document.rightMargin() - document.leftMargin();
        float imageHeight = document.getPageSize().height() - document.topMargin() - document.bottomMargin();

        if(image.height() > imageHeight || image.width() > imageWidth) {
            float coeff1 = imageWidth / image.width();
            float coeff2 = imageHeight / image.height();

            if(coeff2 < coeff1) {
                coeff1 = coeff2;
            }

            image.scaleAbsolute(image.width() * coeff1, image.height() * coeff1);
        }
    }

    /**
     * Generates info of modifiers which are common to every uml element. This info is
     * presented as a string with a comma separator.
     *
     * @param elem uml element
     * @return a string, which contains elements modifiers info
     */
    public static String createCommonModifiersString(Object elem) {
        if(elem != null) {
            StringBuffer values = new StringBuffer("");

            if(Model.getFacade().isAbstract(elem)) {
                values.append(Translator.localize("argopdf.report.abstract"));
            }

            if(Model.getFacade().isLeaf(elem)) {
                if(!"".equals(values.toString())) values.append(", ");
                values.append(Translator.localize("argopdf.report.leaf"));
            }

            if(Model.getFacade().isRoot(elem)) {
                if(!"".equals(values.toString())) values.append(", ");
                values.append(Translator.localize("argopdf.report.root"));
            }

            Object tv = Model.getFacade().getTaggedValue(elem, Facade.GENERATED_TAG);
            if (tv != null) {
                String tag = Model.getFacade().getValueOfTag(tv);
                if ("true".equals(tag)) {
                    //table.addCell(Translator.localize(trueResource));
                    if(!"".equals(values.toString())) values.append(", ");
                    values.append(Translator.localize("argopdf.report.derived"));
                }
            }

            return values.toString();
        }

        return "";
    }

    /**
     * Creates a table with modifiers info.
     *
     * @param elem uml element, which modifiers info need to be generated
     * @return an instance of <i>PdfPTable</i> class, which contains modifiers info
     */
    public static PdfPTable createModifiersInfo(Object elem) {
        PdfPTable table = new PdfPTable(2);
        try {
            table.setWidthPercentage(100);
            table.setWidths(new float[]{1f, 3f});

            Font captionFont = new Font(Font.HELVETICA, 12, Font.BOLD, Color.WHITE);
/*
            table.addCell(createCell(Translator.localize("argopdf.report.modifiers.table.name"),
                                     1, TABLE_HEADER_COLOR, captionFont));
            table.addCell(createCell(Translator.localize("argopdf.report.modifiers.table.value"),
                                     1, TABLE_HEADER_COLOR, captionFont));
*/
            table.addCell(TableUtils.createCell(Translator.localize("argopdf.report.modifiers"),
                                     2, TableUtils.TABLE_HEADER_COLOR, captionFont));

            String values = createCommonModifiersString(elem);

/*            String trueResource = "argopdf.report.modifiers.table.true";
            String falseResource = "argopdf.report.modifiers.table.false";
            table.addCell(createCell(Translator.localize("argopdf.report.modifiers.table.abstract")));
            table.addCell(createCell(Model.getFacade().isAbstract(elem) ?
                                     Translator.localize(trueResource) : Translator.localize(falseResource)));

            table.addCell(createCell(Translator.localize("argopdf.report.modifiers.table.leaf")));
            table.addCell(createCell(Model.getFacade().isLeaf(elem) ?
                                     Translator.localize(trueResource) : Translator.localize(falseResource)));

            table.addCell(createCell(Translator.localize("argopdf.report.modifiers.table.root")));
            table.addCell(createCell(Model.getFacade().isRoot(elem) ?
                                     Translator.localize(trueResource) : Translator.localize(falseResource)));
            
            table.addCell(createCell(Translator.localize("argopdf.report.modifiers.table.derived")));*/

            //table.addCell(Translator.localize(falseResource));
            if("".equals(values)) {
                table.addCell(TableUtils.createCell("  ", 2));
            } else {
                table.addCell(TableUtils.createCell(values, 2));
            }

        } catch(DocumentException ex) {
            LOG.debug(ex.getMessage());
        }

        return table;
    }



    /**
     * Creates an instance of <i>Phrase</i> class, which contains an image and its label or anchor.
     * At the beginning text parameter is checked. If it is null, anchor parameter is used. If
     * both parameters are null, method returns empty <i>Phrase</i>.
     *
     * @param imageName name of the image
     * @param text      label of the image
     * @param anchor    anchor of the image
     * @param textDepth depth of the title
     * @return an instance of <i>Phrase</i> class, with image and its label. Returns empty <i>Phrase</i> if both
     *         parameters: text and anchor are null.
     */
    public static Phrase createImageLabelPhrase(String imageName, String text, Anchor anchor, int textDepth) {
        Phrase phrase = new Phrase();
        if(text == null && anchor == null) return phrase;

        try {
            ImageIcon icon = ResourceLoaderWrapper.lookupIconResource(imageName);
            com.lowagie.text.Image im = com.lowagie.text.Image.getInstance(icon.getImage(), null);
            im.setAlignment(com.lowagie.text.Image.ALIGN_BOTTOM);

            phrase.add(new Chunk(im, 0, 0, true));
            if(text != null) {
                phrase.add(ReportUtils.generateTitle(text, textDepth, false));
            } else {
                phrase.add(ReportUtils.generateTitle(anchor, textDepth, false));
            }

        } catch(IOException ex) {
            LOG.debug(ex.getMessage());
        } catch(BadElementException ex) {
            LOG.debug(ex.getMessage());
        }

        return phrase;
    }

    /**
     * Generates an instance of <i>Phrase</i> class, with image and label in it.
     *
     * @param imageName name of the standard image
     * @param text      text to be inserted
     * @return an instance of <i>Phrase</i> class, with image and label in it
     */
    protected static Phrase createImageLabelPhrase(String imageName, String text) {
        return createImageLabelPhrase(imageName, text, null, 3);
    }

    /**
     * Generates information of assosiation end.
     *
     * @param assEnd        an instance of <i>AssociationEnd</i> class, which info will be generated
     * @return An instance of <i>PdfPTable</i> class, which contains info of assosiation end.
     *         If <i>assEnd</i> is not an instance of AssociationEnd class, returns null.
     */
    public static PdfPTable generateAssosiatedEndInfo(Object assEnd) {
        if(Model.getFacade().isAAssociationEnd(assEnd)) {
            PdfPTable table = new PdfPTable(2);
            table.setWidthPercentage(100);

            try {
                table.setWidths(new float[]{2f, 6f});
                Object element = Model.getFacade().getType(assEnd);

                String imageName = getImageName(element);
                table.addCell(TableUtils.createCell(ReportUtils.getElementNameWithReference(element), 2, null, imageName, null, new float[]{1, 20}));

                table.addCell(TableUtils.createCell(Translator.localize("argopdf.report.part.usecase.assosiation.table.assosiation.end")));
                table.addCell(TableUtils.createCell(Model.getFacade().getName(assEnd)));

                Object multiplicity = Model.getFacade().getMultiplicity(assEnd);
                if(multiplicity != null) {
                    table.addCell(TableUtils.createCell(Translator.localize("argopdf.report.part.usecase.assosiation.table.multiplicity")));
                    table.addCell(Model.getFacade().toString(multiplicity));
                }

                table.addCell(TableUtils.createCell(Translator.localize("argopdf.report.part.usecase.assosiation.table.visibility")));
                table.addCell(UseCasesDiagramHelper.getAssosiationEndVisibility(assEnd));

                String aggrKind = UseCasesDiagramHelper.getAggregationKindEndVisibility(assEnd);
                if(aggrKind != null && !ActionSetAssociationEndAggregation.NONE_COMMAND.equals(aggrKind)) {
                    table.addCell(TableUtils.createCell(Translator.localize("argopdf.report.part.usecase.assosiation.table.aggregation")));
                    table.addCell(aggrKind);
                }

                if(Model.getFacade().isNavigable(assEnd)) {
                    table.addCell(TableUtils.createCell(Translator.localize("argopdf.report.part.usecase.assosiation.table.navigable")));
                    table.addCell(Model.getFacade().toString(Model.getFacade().isNavigable(assEnd)));
                }

                table.addCell(TableUtils.createCell(Translator.localize("argopdf.report.part.usecase.assosiation.table.documentation")));
                table.addCell(TableUtils.createCell(getElementsDocumentation(assEnd)));

                return table;
            } catch(DocumentException ex) {
                LOG.debug(ex.getMessage());
            }

        }

        return null;
    }

}
