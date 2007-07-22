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
import com.lowagie.text.pdf.PdfPTable;

import javax.swing.*;
import java.io.*;
import java.util.HashMap;
import java.util.Enumeration;

import org.argouml.ui.argopdf.ArgoPDFMenuPlugin;
import org.argouml.ui.targetmanager.TargetManager;
import org.argouml.i18n.Translator;
import org.argouml.argopdf.ui.TreeNode;
import org.argouml.uml.UseCases;
import org.argouml.uml.diagram.use_case.ui.UMLUseCaseDiagram;
import org.apache.log4j.Logger;

/**
 * PdfReport represents the implementation of IReport interface by using
 * <a href="http://www.lowagie.com/iText/">iText</a> free open source library.
 *
 *
 * @author Dmitry Churbanau
 * @version 0.1
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
    private HashMap options = new HashMap();

    private Document document;

    /**
     * Generates report and saves it to the path specified bu the user
     * @return null if report was generated successfully, otherwise error message
     */
    public String generateReport() {
        try {
            saveOptions();
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

            document = new Document();

            PdfWriter writer;
            try {
                writer = PdfWriter.getInstance(document,new FileOutputStream(path));
            } catch(FileNotFoundException ex) {
                LOG.debug(ex.getMessage());
                return Translator.localize("argopdf.report.error.file.is.used.by.another.application");
            } catch(DocumentException ex) {
                LOG.debug("Can not create an instance of PdfWriter class.");
                return Translator.localize("argopdf.report.error.file.is.used.by.another.application");
            }

            generateMetadata();
            document.open();
            if(generateTitlePage) {
                generateTitlePage(writer);
            }

            generateUseCaseDiagrams();

            document.close();

            return null;
        } catch(OutOfMemoryError ex) {
            //todo Manage Out of memmory exception
            LOG.debug(ex.getMessage());
        } finally {
            loadOptions();
        }
        return null;
    }

    /**
     * Generates title page of the report
     *
     * @param writer an instance of <i>PdfWriter</i> object of current document
     */
    private void generateTitlePage(PdfWriter writer) {

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

            //Sets the author of the report in the absolute position on the title page
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

    /**
     * Generates use case diagrams, which was selected by the user
     */
    private void generateUseCaseDiagrams() {
        if(reportTree != null) {
            TreeNode root = (TreeNode)reportTree.getModel().getRoot();
            if(root.isSelected()) {
                TreeNode node = (TreeNode)root.getFirstChild();

                //process this node only if it is a folder, which contains all
                //use case diagrams and it is selected
                if(node != null && (node.getUserObject() instanceof UseCases) && node.isSelected()) {

                    TreeNode useCaseDiagramNode = (TreeNode)node.getFirstChild();
                    Chapter useCaseChapter = new Chapter(new Paragraph(Translator.localize("argopdf.report.part.usecase.title"),
                                                                       new Font(Font.HELVETICA, 25, Font.BOLD)), 1);
                    boolean addUseCase = false;
                    while(useCaseDiagramNode != null) {
                        if(useCaseDiagramNode.isSelected()) {
                            Object useCase = useCaseDiagramNode.getUserObject();
                            if(useCase instanceof UMLUseCaseDiagram) {
                                addUseCaseDiagram(useCaseChapter, (UMLUseCaseDiagram)useCase);
                                addUseCase = true;
                            }
                        }
                        useCaseDiagramNode = (TreeNode)useCaseDiagramNode.getNextSibling();
                    }

                    if(addUseCase) {
                        try {
                            document.add(useCaseChapter);
                        } catch(DocumentException ex) {
                            LOG.debug(ex.getMessage());
                        }
                    }

                }

            }
        }
    }

    /**
     * Generates metadata of the report. This method should be called before
     * instance of the <i>Document</i> class will be called.
     */
    private void generateMetadata() {
        document.addTitle(title);
        document.addAuthor(author);
        document.addSubject(Translator.localize("argopdf.report.metadata.subject"));
        document.addCreator(ArgoPDFMenuPlugin.ARGO_PDF_NAME + " " + ArgoPDFMenuPlugin.ARGO_PDF_VERSION);
    }

    /**
     * Adds Use Case diagram to the report.
     *
     * @param chapter chapter of Use Case diagrams
     * @param diagram Use Case diagram to add to the report
     */
    private void addUseCaseDiagram(Chapter chapter, UMLUseCaseDiagram diagram) {

        if(diagram == null) return;
        LOG.debug("Add Use Case diagram: " + diagram.getName());

        //Creates section in pdf file, which will contain
        //info about Use Case diagram, which is in processing
        Paragraph paragraph = new Paragraph(diagram.getName(), new Font(Font.HELVETICA, 20, Font.BOLD));
        Section section = chapter.addSection(paragraph, 2);
        section.add(Chunk.NEWLINE);

        Image im = ReportUtils.makeImageOfDiagram(diagram);
        if(im != null) {
            ReportUtils.adjustImageSizeToDocumentPageSize(im,  document);
            section.add(new Chunk(im, 0, 0, true));
        }

        section.add(Chunk.NEWLINE);
        Paragraph summary = UseCasesDiagramHelper.generateSummaryInfo(diagram);
        Paragraph details = UseCasesDiagramHelper.generateDetailedInfo(diagram);
        section.add(summary);
        section.add(details);

        //Enumeration elements = diagram.elements();
        section.add(Chunk.NEXTPAGE);
    }

    /**
     * Saves options, which can be changed during report generation
     */
    private void saveOptions() {
        TargetManager tm = TargetManager.getInstance();

        options.put("tm.targets", tm.getTargets());
        options.put("tm.target", tm.getTarget());
    }

    /**
     * Loads options, which was saved earlier
     */
    private void loadOptions() {
        TargetManager tm = TargetManager.getInstance();
        tm.setTargets((java.util.List)options.get("tm.targets"));
        tm.setTarget(options.get("tm.target"));
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
