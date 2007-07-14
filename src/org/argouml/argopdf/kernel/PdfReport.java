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
import com.lowagie.text.pdf.PdfWriter;
import com.lowagie.text.pdf.PdfContentByte;
import com.lowagie.text.pdf.BaseFont;

import javax.swing.*;
import javax.swing.tree.TreeModel;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.FileNotFoundException;

import org.argouml.ui.argopdf.ArgoPDFMenuPlugin;
import org.argouml.i18n.Translator;
import org.apache.log4j.Logger;

/**
 * PdfReport represents the implementation of IReport interface by using
 * <a href="http://www.lowagie.com/iText/">iText</a> free open source library.
 *
 *
 * @author Dima
 * @version 1.0
 * @see org.argouml.argopdf.kernel.IReport
 */
public class PdfReport implements IReport {

    private boolean generateTitlePage       = true;
    private boolean generateTableOfContents = true;
    private boolean generateDiagrams        = true;

    private String path;
    private String logoPath;
    private String title;
    private String author;

    private JTree reportTree;

    private static final Logger LOG = Logger.getLogger(PdfReport.class);

    public String generateReport() {

        if(path == null || "".equals(path)) {
            LOG.debug("Report path is not specified");
            return Translator.localize("argopdf.report.error.file.path.is.not.specified");
        }

        if(getLogoPath() != null && !"".equals(getLogoPath())) {
            try {
                Image.getInstance(getLogoPath());
            } catch(Exception ex) {
                LOG.debug("Report image path is incorrect");
                return Translator.localize("argopdf.report.error.image.logo.path.is.not.specified");
            }
        }

        Document document = new Document();

        PdfWriter writer;
        try {
            writer = PdfWriter.getInstance(document,new FileOutputStream(path));
        } catch(DocumentException ex) {
            LOG.debug("Can not create an instance of PdfWriter class.");
            return Translator.localize("argopdf.report.error.file.is.used.by.another.application");
        } catch(FileNotFoundException ex) {
            LOG.debug(ex.getMessage());
            return Translator.localize("argopdf.report.error.unknown");
        }

        generateMetadata(document);
        document.open();
        if(generateTitlePage) {
            generateTitlePage(document, writer);
        }

        generateUseCaseDiagrams();
            
        document.close();

        return null;
    }

    private void generateTitlePage(Document document, PdfWriter writer) {

        try {
            boolean addLogoImage = false;
            float imageHeight = 0;
            if(getLogoPath() != null && !"".equals(getLogoPath())) {
                Image im = null;
                try {
                    im = Image.getInstance(getLogoPath());
                    im.setAlignment(Image.LEFT);
                } catch(IOException ex) {
                    LOG.debug(ex.getMessage());
                }
                document.add(im);
                addLogoImage = true;
                imageHeight  = (im != null ? im.plainHeight() : 0);
            }

            if(getLogoPath() == null || "".equals(getLogoPath())) {
                document.add(new Chunk(' '));
                imageHeight += 12;
            }

            Paragraph paragraph = new Paragraph(getTitle(), new Font(Font.UNDEFINED, 25, Font.BOLD));
            //Sets before spacing, which depends, whether logo image was added to the report or not
            if(addLogoImage) {
                if(imageHeight >= 200) {
                    paragraph.setSpacingBefore(10);
                } else {
                    paragraph.setSpacingBefore(200 - imageHeight);
                }
            } else {
                paragraph.setSpacingBefore(200);
            }
            paragraph.setAlignment(Element.ALIGN_CENTER);
            document.add(paragraph);

            PdfContentByte cb = writer.getDirectContent();
            cb.saveState();
            cb.beginText();

            BaseFont bf = BaseFont.createFont(BaseFont.HELVETICA, BaseFont.CP1252, BaseFont.NOT_EMBEDDED);

            cb.moveText((PageSize.A4.width() - bf.getWidthPoint(getAuthor(), 12)) / 2, 100);
            cb.setFontAndSize(bf, 12);

            cb.showText(getAuthor());
            cb.endText();
            cb.restoreState();
            

        } catch(DocumentException ex) {
            LOG.debug(ex.getMessage());
        } catch(IOException ex) {
            LOG.debug(ex.getMessage());
        }

    }

    private void generateUseCaseDiagrams() {
        if(reportTree != null) {
            TreeModel model = reportTree.getModel();
            //System.out.println("PdfReport.generateUseCaseDiagrams model = '"+model+"'");
        }

    }

    /**
     * Generates metadata of the report. This method should be called before
     * instance of the <i>Document</i> class will be called.
     *
     * @param document current instance of the report.
     */
    private void generateMetadata(Document document) {
        document.addTitle(title);
        document.addAuthor(author);
        document.addSubject(Translator.localize("argopdf.report.metadata.subject"));
        document.addCreator(ArgoPDFMenuPlugin.ARGO_PDF_NAME + " " + ArgoPDFMenuPlugin.ARGO_PDF_VERSION);
    }

    public void setTree(JTree tree) {
        this.reportTree = tree;
    }

    public String getLogoPath() {
        return logoPath;
    }

    public void setLogoPath(String logoPath) {
        this.logoPath = logoPath;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public boolean isGenerateTitlePage() {
        return generateTitlePage;
    }

    public void setGenerateTitlePage(boolean generateTitlePage) {
        this.generateTitlePage = generateTitlePage;
    }

    public boolean isGenerateTableOfContents() {
        return generateTableOfContents;
    }

    public void setGenerateTableOfContents(boolean generateTableOfContents) {
        this.generateTableOfContents = generateTableOfContents;
    }

    public boolean isGenerateDiagrams() {
        return generateDiagrams;
    }

    public void setGenerateDiagrams(boolean generateDiagrams) {
        this.generateDiagrams = generateDiagrams;
    }
}
