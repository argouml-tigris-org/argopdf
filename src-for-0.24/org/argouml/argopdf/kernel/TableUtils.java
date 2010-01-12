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

import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.*;
import com.lowagie.text.Image;
import com.lowagie.text.Font;

import java.awt.*;
import java.util.ArrayList;
import java.io.IOException;

import org.apache.log4j.Logger;
import org.argouml.i18n.Translator;
import org.argouml.application.helpers.ResourceLoaderWrapper;

import javax.swing.*;

/**
 * Contains helper methods for working with tables of iText library.
 *
 * @author Dmitry Churbanau
 * @version 0.1
 */
public class TableUtils {

    private static final Logger LOG = Logger.getLogger(TableUtils.class);

    public static Color TABLE_HEADER_COLOR = Color.BLUE;

    /**
     * Generates standard table 'cap'.
     *
     * @param cellCount amount of cells in one row of the table.
     * @return an instance of <i>PdfPTable</i> class
     */
    public static PdfPTable generateTableCap(int cellCount) {
        return generateTableCap(cellCount, 10, 10);
    }

    public static PdfPTable generateTableCap(int cellCount, int spacingBefore, int spacingAfter) {
        PdfPTable table = new PdfPTable(cellCount);
        table.setWidthPercentage(100);
        table.setSpacingBefore(spacingBefore);
        table.setSpacingAfter(spacingAfter);

        return table;
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
            PdfPTable table = generateTableCap(2);

            try {
                table.setWidths(new float[]{1f, 2f});

                com.lowagie.text.Font captionFont = new com.lowagie.text.Font(com.lowagie.text.Font.HELVETICA, 12, com.lowagie.text.Font.BOLD, Color.WHITE);
                table.addCell(createCell(Translator.localize("argopdf.report.name"),
                                                     1, TABLE_HEADER_COLOR, captionFont));
                table.addCell(createCell(Translator.localize("argopdf.report.documentation"),
                                                     1, TABLE_HEADER_COLOR, captionFont));

                for (Object elem : elements) {
                    table.addCell(createCell(ReportUtils.getElementNameWithReference(elem), 1, null, ReportUtils.getImageName(elem), null));
                    table.addCell(createCell(ReportUtils.getElementsDocumentation(elem)));
                }

            } catch(DocumentException ex) {
                LOG.debug(ex.getMessage());
            }

            return table;
        }
        return null;
    }

    /**
     * Creates a cell with a table inside of it, which contains an image and a label.
     *
     * @param text       text of a cell
     * @param imageName  standard name of an image, which should be placed in the first cell
     * @return           a table, which contains image in the first cell and text in the second one
     */
    public static PdfPCell createCell(String text, String imageName) {
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
    public static PdfPCell createCell(String text, String imageName, float[] tableWidths) {
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
    public static PdfPCell createCell(String text, int colspan, Color color, String imageName, com.lowagie.text.Font cellFont) {
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
    public static PdfPCell createCell(Anchor anchor, int colspan, Color color, String imageName, com.lowagie.text.Font cellFont) {
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
    public static PdfPCell createCell(String text, int colspan, Color color, String imageName, com.lowagie.text.Font cellFont, float[] tableWidth) {
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
    public static PdfPCell createCell(Anchor anchor, int colspan, Color color, String imageName, com.lowagie.text.Font cellFont, float[] tableWidth) {
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
    public static PdfPCell createCell(String text) {
        return createCell(text, 1, null, null);
    }

    /**
     * Creates a cell of a table, which contains a simple text.
     *
     * @param text     text of a cell
     * @param colspan  colspan of a table
     * @return         a cell of a table
     */
    public static PdfPCell createCell(String text, int colspan) {
        return createCell(text, colspan, null, null);
    }

    /**
     * Creates a cell of a table which contains a simple text, have a table cap color.
     *
     * @param text     text of a cell
     * @param cellFont font of a cell text
     * @return         a cap cell of a table
     */
    protected static PdfPCell createHeaderCell(String text, com.lowagie.text.Font cellFont) {
        return createCell(text, 1, TableUtils.TABLE_HEADER_COLOR, cellFont);
    }

    /**
     * Creates a cell of a table which contains a simple text, have a table cap color.
     *
     * @param text     text of a cell
     * @param colspan  colspan of a table
     * @param cellFont font of a cell text
     * @return         a cap cell of a table
     */
    public static PdfPCell createHeaderCell(String text, int colspan, com.lowagie.text.Font cellFont) {
        return createCell(text, colspan, TableUtils.TABLE_HEADER_COLOR, cellFont);
    }


    /**
     * Creates a cell of a table which contains a simple text, have a table cap color.
     *
     * @param text     text of a cell
     * @return         a cap cell of a table
     */
    public static PdfPCell createHeaderCellWithFont(String text) {
        return createHeaderCell(text, new Font(Font.HELVETICA, 12, Font.BOLD, Color.WHITE));
    }

    /**
     * Creates a cell of a table which contains a simple text, have a table cap color.
     *
     * @param text     text of a cell
     * @param colspan  colspan of a table
     * @return         a cap cell of a table
     */
    public static PdfPCell createHeaderCellWithFont(String text, int colspan) {
        return createHeaderCell(text, colspan, new Font(Font.HELVETICA, 12, Font.BOLD, Color.WHITE));
    }

    /**
     * Creates a cell of a table which contains a simple text.
     *
     * @param text     text of a cell
     * @param colspan  colspan of a table
     * @param color    color of a cell
     * @param cellFont font of a cell text
     * @return         a cell of a table
     */
    public static PdfPCell createCell(String text, int colspan, Color color, com.lowagie.text.Font cellFont) {
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
    public static PdfPCell createCell(Anchor anchor, int colspan, Color color, com.lowagie.text.Font cellFont) {
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
    public static PdfPTable createImageLabelTable(String text, String imageName) {
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
    public static PdfPTable createImageLabelTable(String text, String imageName, Color colorName, com.lowagie.text.Font cellFont) {
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
    public static PdfPTable createImageLabelTable(String text, Anchor anchor, String imageName, Color colorName, float[] tableWidth, com.lowagie.text.Font cellFont) {
        if(text == null && anchor == null) return null;
        try {
            PdfPTable nestedTable = new PdfPTable(2);
            nestedTable.setWidthPercentage(100);
            nestedTable.setWidths(tableWidth);

            ImageIcon icon = ResourceLoaderWrapper.lookupIconResource(imageName);
            if (icon == null) {
                if (imageName != null && imageName.length() > 1) {
                    icon = ResourceLoaderWrapper.lookupIconResource(imageName.substring(0, 1).toUpperCase() + imageName.substring(1));
                }
            }
            System.out.println("TableUtils.createImageLabelTable; imageName = ["+imageName+"]");
            System.out.println("TableUtils.createImageLabelTable; icon = ["+icon+"]");
            Image im = null;
            if (icon != null) {
                im = Image.getInstance(icon.getImage(), null);
                im.setAlignment(Image.ALIGN_LEFT);
            }
            PdfPCell nestCell_1 = null;
            if (im != null) {
                nestCell_1 = new PdfPCell(im, false);
            } else {
                nestCell_1 = new PdfPCell();
            }
            nestCell_1.setHorizontalAlignment(Element.ALIGN_LEFT);
            nestCell_1.setVerticalAlignment(Element.ALIGN_MIDDLE);
            nestCell_1.setBorder(0);
            if(colorName != null) {
                nestCell_1.setBackgroundColor(colorName);
            }
            nestedTable.addCell(nestCell_1);

            PdfPCell nestCell_2;
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

    public static PdfPTable createTable(String[] titles, ArrayList elements, float[] tableWidth) {
        PdfPTable table;
        if(titles != null && titles.length > 0) {
            table = generateTableCap(titles.length);
        } else {
            if(elements == null || elements.size() == 0) return null;
            table = generateTableCap(elements.size());
        }
        table.setWidthPercentage(100);
        try {
            table.setWidths(tableWidth);

            if(titles != null) {
                for(String el : titles) {
                    PdfPCell cell = createHeaderCellWithFont(el);
                    cell.setHorizontalAlignment(Element.ALIGN_LEFT);
                    cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
                    cell.setBorder(0);
                    table.addCell(cell);
                }
            }

            for(Object el : elements) {
                PdfPCell cell = new PdfPCell();
                if(el instanceof Image) {
                    cell = new PdfPCell((Image)el, false);
                } else if(el instanceof String){
                    cell = createCell((String)el);
                } else if(el instanceof Anchor) {
                    cell = createCell((Anchor)el, 1, null, null);
                }
                cell.setHorizontalAlignment(Element.ALIGN_LEFT);
                cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
                cell.setBorder(0);
                table.addCell(cell);
            }

        } catch(DocumentException ex) {
            LOG.debug(ex.getMessage());
        }


        return table;
    }
}
