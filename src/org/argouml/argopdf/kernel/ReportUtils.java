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
import org.argouml.uml.ui.SaveGraphicsManager;
import org.argouml.ui.targetmanager.TargetManager;
import org.argouml.configuration.Configuration;
import org.argouml.application.helpers.ResourceLoaderWrapper;
import org.argouml.i18n.Translator;
import org.argouml.model.Model;
import org.argouml.model.Facade;
import org.tigris.gef.base.SaveGraphicsAction;
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
        retCell.addElement(createImageLabelTable(text, imageName, color, tableWidth, cellFont));
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
        return createImageLabelTable(text, imageName, colorName, new float[]{1, 7}, cellFont);
    }

    /**
     * Creates a table, which contains image in the first cell and text in the second one.
     *
     * @param text       text of a second cell
     * @param imageName  standard name of an image, which should be placed in the first cell
     * @param colorName  color of a table, if null, will be white
     * @param tableWidth array of relative widths of table cells
     * @param cellFont   font of a table text
     * @return           a table, which contains image in the first cell and text in the second one
     */
    protected static PdfPTable createImageLabelTable(String text, String imageName, Color colorName, float[] tableWidth, com.lowagie.text.Font cellFont) {
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

            PdfPCell nestCell_2 = createCell(text, 1, colorName, cellFont);
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
            table.addCell(createCell(Translator.localize("argopdf.report.modifiers.table.name"),
                                     1, TABLE_HEADER_COLOR, captionFont));
            table.addCell(createCell(Translator.localize("argopdf.report.modifiers.table.value"),
                                     1, TABLE_HEADER_COLOR, captionFont));

            String trueResource = "argopdf.report.modifiers.table.true";
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
            
            table.addCell(createCell(Translator.localize("argopdf.report.modifiers.table.derived")));
            Object tv = Model.getFacade().getTaggedValue(elem, Facade.DERIVED_TAG);
            if (tv != null) {
                String tag = Model.getFacade().getValueOfTag(tv);
                if ("true".equals(tag)) {
                    table.addCell(Translator.localize(trueResource));
                }
            }
            table.addCell(Translator.localize(falseResource));

        } catch(DocumentException ex) {
            LOG.debug(ex.getMessage());
        }

        return table;
    }

    /**
     * Generates an instance of <i>Phrase</i> class, with image and label in it.
     *
     * @param imageName name of the standard image
     * @param text      text to be inserted
     * @return an instance of <i>Phrase</i> class, with image and label in it
     */
    protected static Phrase createImageLabelPhrase(String imageName, String text) {
        Phrase phrase = new Phrase();

        try {
            ImageIcon icon = ResourceLoaderWrapper.lookupIconResource(imageName);
            com.lowagie.text.Image im = com.lowagie.text.Image.getInstance(icon.getImage(), null);
            im.setAlignment(com.lowagie.text.Image.ALIGN_BOTTOM);

            phrase.add(new Chunk(im, 0, 0, true));
            phrase.add(text);

        } catch(IOException ex) {
            LOG.debug(ex.getMessage());
        } catch(BadElementException ex) {
            LOG.debug(ex.getMessage());
        }
        
        return phrase;
    }
}
