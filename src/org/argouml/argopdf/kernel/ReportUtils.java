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

import org.argouml.uml.diagram.ArgoDiagram;
import org.argouml.uml.diagram.ui.UMLDiagram;
import org.argouml.uml.diagram.static_structure.ui.UMLClassDiagram;
import org.argouml.uml.diagram.use_case.ui.UMLUseCaseDiagram;
import org.argouml.uml.ui.SaveGraphicsManager;
import org.argouml.ui.targetmanager.TargetManager;
import org.argouml.configuration.Configuration;
import org.argouml.application.helpers.ResourceLoaderWrapper;
import org.argouml.i18n.Translator;
import org.argouml.model.Model;
import org.argouml.model.Facade;
import org.tigris.gef.base.SaveGraphicsAction;
import org.apache.log4j.Logger;
import org.omg.uml.foundation.core.UmlClass;
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

    public static Color TABLE_HEADER_COLOR = Color.BLUE;

    private static final Logger LOG = Logger.getLogger(ReportUtils.class);

    /**
     * Generates standart table 'cap'.
     *
     * @param cellCount amount of cells in one row of the table.
     * @return an instance of <i>PdfPTable</i> class
     */
    public static PdfPTable generatedTableCap(int cellCount) {
        PdfPTable table = new PdfPTable(cellCount);
        table.setWidthPercentage(100);
        table.setSpacingBefore(10f);
        table.setSpacingAfter(10f);

        return table;
    }

    /**
     * Finds the image name of the uml object according to the instance of parameter class.
     *
     * @param umlObject an instance of uml object
     * @return the image name of uml object. If image name can not be found, returns empty string.
     */
    public static String getImageName(Object umlObject) {
        String imageName = "";
        if(Model.getFacade().isAUseCase(umlObject)) {
            imageName = "useCase";
        } else if(Model.getFacade().isAActor(umlObject)) {
            imageName = "Actor";
        } else if(Model.getFacade().isAClass(umlObject)) {
            imageName = "Class";
        }

        return imageName;
    }

    /**
     * Returns element name. If element does not have a name, returns string 'Unnamed elementType',
     * where elemenType is a type of element: Actor, UseCase and etc. 
     *
     * @param element uml element
     * @return elements name, or string 'Unnamed elementType', if element does not have a name or it is
     *         empty
     */
    public static String getElementName(Object element) {
        String name = "";

        if(element instanceof UMLUseCaseDiagram || element instanceof UMLClassDiagram) {
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
            } else if(element instanceof UMLUseCaseDiagram) {
                name += Translator.localize("argopdf.report.diagram.usecase");
            } else if(element instanceof UMLClassDiagram) {
                name += Translator.localize("argopdf.report.diagram.class");
            } else if(Model.getFacade().isAPackage(element)) {
                name += Translator.localize("argopdf.report.package");
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
        anchor.setReference("#" + element.getClass() + "_" + element.hashCode());

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
     * Returns document of element.
     *
     * @param element element, witch document will be generated.
     * @return document of element
     */
    public static String getElementDocumentation(Object element) {
        Object taggedValue = Model.getFacade().getTaggedValue(element, "documentation");
        String documentation = "";
        if(taggedValue != null) {
            documentation = Model.getFacade().getValueOfTag(taggedValue);
        }

        return documentation;
    }

    /**
     * Generates info of elements. Info is presented in a table, with name and documentation columns.
     *
     * @param elements ArraList of alements, info of which will be generated
     * @return and instance of <i>PdfPTable</i> class, which contains elements info.
     *         null, if size of elements array == 0
     */
    public static PdfPTable generateElementsInfo(ArrayList elements) {
        if (elements != null && elements.size() > 0 ) {
            PdfPTable table = ReportUtils.generatedTableCap(2);

            try {
                table.setWidths(new float[]{1f, 2f});

                Font captionFont = new Font(Font.HELVETICA, 12, Font.BOLD, Color.WHITE);
                table.addCell(ReportUtils.createCell(Translator.localize("argopdf.report.name"),
                                                     1, ReportUtils.TABLE_HEADER_COLOR, captionFont));
                table.addCell(ReportUtils.createCell(Translator.localize("argopdf.report.documentation"),
                                                     1, ReportUtils.TABLE_HEADER_COLOR, captionFont));

                for (Object elem : elements) {
                    table.addCell(ReportUtils.createCell(ReportUtils.getElementNameWithReference(elem), 1, null, ReportUtils.getImageName(elem), null));
                    table.addCell(ReportUtils.createCell(ReportUtils.getElementDocumentation(elem)));
                }

            } catch(DocumentException ex) {
                LOG.debug(ex.getMessage());
            }

            return table;
        }
        return null;
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

        SaveGraphicsAction cmd = SaveGraphicsManager.getInstance().getSaveActionBySuffix("png");
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        cmd.setStream(outputStream);
        cmd.setScale(Configuration.getInteger(SaveGraphicsManager.KEY_GRAPHICS_RESOLUTION, 1));
        cmd.actionPerformed(null);

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
     * Creates a cell with a table inside of it, which contains an image and a label.
     *
     * @param text       text of a cell
     * @param imageName  standard name of an image, which should be placed in the first cell
     * @return           a table, which contains image in the first cell and text in the second one
     */
    protected static PdfPCell createCell(String text, String imageName) {
        return createCell(text, 1, null, imageName, null);
    }

    /**
     * Creates a cell with a table inside of it, which contains an image and a label.
     *
     * @param text        text of a cell
     * @param imageName   standard name of an image, which should be placed in the first cell
     * @param tableWidths array of relative widths of table cells
     * @return            a table, which contains image in the first cell and text in the second one
     */
    protected static PdfPCell createCell(String text, String imageName, float[] tableWidths) {
        if(tableWidths == null) tableWidths = new float[] {1, 7};
        return createCell(text, 1, null, imageName, null, tableWidths);
    }


    /**
     * Creates a cell with a table inside of it, which contains an image and a label.
     *
     * @param text      text of a cell
     * @param colspan   colspan of a cell
     * @param color     color of a cell
     * @param imageName name of image to insert
     * @param cellFont  font of a cell text
     * @return cell of a table
     */
    protected static PdfPCell createCell(String text, int colspan, Color color, String imageName, com.lowagie.text.Font cellFont) {
        return createCell(text, colspan, color, imageName, cellFont, null);
    }

    /**
     * Creates a cell with a table inside of it, which contains an image and a label. Label can contain
     * a reference or a reference anchor.
     *
     * @param anchor    anchor of a cell
     * @param colspan   colspan of a cell
     * @param color     color of a cell
     * @param imageName name of image to insert
     * @param cellFont  font of a cell text
     * @return cell of a table
     */
    protected static PdfPCell createCell(Anchor anchor, int colspan, Color color, String imageName, com.lowagie.text.Font cellFont) {
        return createCell(anchor, colspan, color, imageName, cellFont, null);
    }


    /**
     * Creates a cell with a table inside of it, which contains an image and a label.
     *
     * @param text      text of a cell
     * @param colspan   colspan of a cell
     * @param color     color of a cell
     * @param imageName name of image to insert
     * @param cellFont  font of a cell text
     * @param tableWidth array of relative widths of table cells
     * @return cell of a table
     */
    protected static PdfPCell createCell(String text, int colspan, Color color, String imageName, com.lowagie.text.Font cellFont, float[] tableWidth) {
        if(tableWidth == null) tableWidth = new float[] {1, 7};
        PdfPCell retCell = new PdfPCell();
        retCell.setColspan(colspan);
        retCell.addElement(createImageLabelTable(text, null, imageName, color, tableWidth, cellFont));
        retCell.setHorizontalAlignment(Element.ALIGN_LEFT);

        return retCell;
    }

    /**
     * Creates a cell with a table inside of it, which contains an image and a label. Label can contain
     * a reference or a reference anchor.
     *
     * @param anchor    anchor of a cell
     * @param colspan   colspan of a cell
     * @param color     color of a cell
     * @param imageName name of image to insert
     * @param cellFont  font of a cell text
     * @param tableWidth array of relative widths of table cells
     * @return cell of a table
     */
    protected static PdfPCell createCell(Anchor anchor, int colspan, Color color, String imageName, com.lowagie.text.Font cellFont, float[] tableWidth) {
        if(tableWidth == null) tableWidth = new float[] {1, 7};
        PdfPCell retCell = new PdfPCell();
        retCell.setColspan(colspan);
        retCell.addElement(createImageLabelTable(null, anchor, imageName, color, tableWidth, cellFont));
        retCell.setHorizontalAlignment(Element.ALIGN_LEFT);

        return retCell;
    }

    /**
     * Creates a cell of a table, which contains a simple text.
     *
     * @param text     text of a cell
     * @return         a cell of a table
     */
    protected static PdfPCell createCell(String text) {
        return createCell(text, 1, null, null);
    }

    /**
     * Creates a cell of a table, which contains a simple text.
     *
     * @param text     text of a cell
     * @param colspan  colspan of a table
     * @return         a cell of a table
     */
    protected static PdfPCell createCell(String text, int colspan) {
        return createCell(text, colspan, null, null);
    }

    /**
     * Creates a cell of a table, which contains a simple text.
     *
     * @param text     text of a cell
     * @param colspan  colspan of a table
     * @param color    color of a cell
     * @param cellFont font of a cell text
     * @return         a cell of a table
     */
    protected static PdfPCell createCell(String text, int colspan, Color color, com.lowagie.text.Font cellFont) {
        PdfPCell cell;
        if(cellFont != null) {
            cell= new PdfPCell(new Paragraph(text, cellFont));
        } else {
            cell= new PdfPCell(new Paragraph(text));
        }

        cell.setColspan(colspan);
        if(color != null) {
            cell.setBackgroundColor(color);
        }

        return cell;
    }

    /**
     * Creates a cell of a table, which contains an anchor.
     *
     * @param anchor   anchor of a cell
     * @param colspan  colspan of a table
     * @param color    color of a cell
     * @param cellFont font of a cell text
     * @return         a cell of a table
     */
    protected static PdfPCell createCell(Anchor anchor, int colspan, Color color, com.lowagie.text.Font cellFont) {
        PdfPCell cell;
        if(cellFont != null) {
            ArrayList chunks = anchor.getChunks();
            for(Object el : chunks) {
                ((Chunk)el).setFont(cellFont);
            }
            cell= new PdfPCell(new Paragraph(anchor));
        } else {
            cell= new PdfPCell(new Paragraph(anchor));
        }

        cell.setColspan(colspan);
        if(color != null) {
            cell.setBackgroundColor(color);
        }

        return cell;
    }


    /**
     * Creates a table, which contains image in the first cell and text in the second one.
     *
     * @param text       text of a cell
     * @param imageName  standard name of an image, which should be placed in the first cell
     * @return           a table, which contains image in the first cell and text in the second one
     */
    protected static PdfPTable createImageLabelTable(String text, String imageName) {
        return createImageLabelTable(text, imageName, null, null);
    }

    /**
     * Creates a table, which contains image in the first cell and text in the second one.
     *
     * @param text      text of a second cell
     * @param imageName standard name of an image, which should be placed in the first cell
     * @param colorName color of a table, if null, will be white
     * @param cellFont  font of a table text
     * @return a table, which contains image in the first cell and text in the second one
     */
    protected static PdfPTable createImageLabelTable(String text, String imageName, Color colorName, com.lowagie.text.Font cellFont) {
        return createImageLabelTable(text, null, imageName, colorName, new float[]{1, 7}, cellFont);
    }

    /**
     * Creates a table, which contains image in the first cell and text or anchor in the second one.
     * If text parameter is not null, second cell will contain this text, otherwise anchor
     * parameter is used. If both parameters is null, null is returned.
     *
     * @param text       text of a second cell
     * @param anchor     anchor of a second cell
     * @param imageName  standard name of an image, which should be placed in the first cell
     * @param colorName  color of a table, if null, will be white
     * @param tableWidth array of relative widths of table cells
     * @param cellFont   font of a table text
     * @return           a table, which contains image in the first cell and text or anchor in the second one.
     *                   Returns null if both parameters: text and anchor are null.
     */
    protected static PdfPTable createImageLabelTable(String text, Anchor anchor, String imageName, Color colorName, float[] tableWidth, com.lowagie.text.Font cellFont) {
        if(text == null && anchor == null) return null;
        try {
            PdfPTable nestedTable = new PdfPTable(2);
            nestedTable.setWidthPercentage(100);
            nestedTable.setWidths(tableWidth);

            ImageIcon icon = ResourceLoaderWrapper.lookupIconResource(imageName);
            Image im = Image.getInstance(icon.getImage(), null);
            im.setAlignment(Image.ALIGN_LEFT);
            PdfPCell nestCell_1 = new PdfPCell(im, false);
            nestCell_1.setHorizontalAlignment(Element.ALIGN_LEFT);
            nestCell_1.setVerticalAlignment(Element.ALIGN_MIDDLE);
            nestCell_1.setBorder(0);
            if(colorName != null) {
                nestCell_1.setBackgroundColor(colorName);
            }
            nestedTable.addCell(nestCell_1);

            PdfPCell nestCell_2 = null;
            if(text != null) {
                nestCell_2 = createCell(text, 1, colorName, cellFont);
            } else {
                nestCell_2 = createCell(anchor, 1, colorName, cellFont);
            }

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

    /**
     * Generates info of modifiers which are common to every uml element
     *
     * @param elem
     * @return
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

            Object tv = Model.getFacade().getTaggedValue(elem, Facade.DERIVED_TAG);
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
    protected static PdfPTable createModifiersInfo(Object elem) {
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
            table.addCell(createCell(Translator.localize("argopdf.report.modifiers"),
                                     2, TABLE_HEADER_COLOR, captionFont));

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
                table.addCell(createCell("  ", 2));
            } else {
                table.addCell(createCell(values, 2));
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
    protected static Phrase createImageLabelPhrase(String imageName, String text, Anchor anchor, int textDepth) {
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
}
