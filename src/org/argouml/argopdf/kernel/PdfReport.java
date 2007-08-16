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
import java.io.*;
import java.util.HashMap;

import org.argouml.ui.argopdf.ArgoPDFMenuPlugin;
import org.argouml.ui.targetmanager.TargetManager;
import org.argouml.i18n.Translator;
import org.argouml.argopdf.ui.TreeNode;
import org.argouml.argopdf.kernel.helpers.*;
import org.argouml.uml.UseCases;
import org.argouml.uml.diagram.use_case.ui.UMLUseCaseDiagram;
import org.argouml.uml.diagram.static_structure.ui.UMLClassDiagram;
import org.argouml.uml.diagram.sequence.ui.UMLSequenceDiagram;
import org.argouml.uml.diagram.collaboration.ui.UMLCollaborationDiagram;
import org.argouml.uml.diagram.activity.ui.UMLActivityDiagram;
import org.argouml.uml.diagram.deployment.ui.UMLDeploymentDiagram;
import org.argouml.model.Model;
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

    //represents current chapter number
    private static int chapterNumber = 0;

    /**
     * Generates new chapter
     *
     * @param name      name of the chapter
     * @param underline true, if chapter should be undelined, otherwise false
     * @return an instance of new <i>Chapter</i> class with appropriate name
     */
    public static Chapter generateNewChapter(String name, boolean underline) {
        Chapter chapter = new Chapter(ReportUtils.generateTitle(name, 0, underline), ++chapterNumber);
        chapter.setBookmarkOpen(false);
        return chapter;
    }

    /**
     * Generates report and saves it to the path specified by the user
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
            processAllClassDiagrams((TreeNode)reportTree.getModel().getRoot(), null);
            processAllSequenceCollaborationActivityDiagrams((TreeNode)reportTree.getModel().getRoot(), null);
            generatePackagesInfo();
            generateDeploymentInfo();

            System.out.println("PdfReport.generateReport pn = '"+writer.getPageNumber()+"'");
            System.out.println("PdfReport.generateReport pn = '"+document.getPageNumber()+"'");
            beforeCloseOperations();


            document.close();

        } catch(OutOfMemoryError ex) {
            LOG.debug(ex.getMessage());
            return Translator.localize("argopdf.outofmemoryerror");
        } finally {
            loadOptions();
        }
        
        return null;
    }

    /**
     * Executes some operations before document will be closed.
     */
    private void beforeCloseOperations() {
        if(document.getPageNumber() == 0) {
            try {
                document.add(Chunk.NEWLINE);
            } catch(DocumentException ex) {
                LOG.debug(ex.getMessage());
            }
        }
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
                    Chapter useCaseChapter = generateNewChapter(Translator.localize("argopdf.report.part.usecase.title"), false);

                    boolean addUseCase = false;
                    while(useCaseDiagramNode != null) {
                        if(useCaseDiagramNode.isSelected()) {
                            Object useCase = useCaseDiagramNode.getUserObject();
                            if(useCase instanceof UMLUseCaseDiagram) {
                                UseCasesDiagramHelper.addUseCaseDiagram(document, useCaseChapter, (UMLUseCaseDiagram)useCase, generateDiagrams);
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
     * Generates packages info which was selected by the user
     */
    private void generatePackagesInfo() {
        if(reportTree != null) {
            TreeNode root = (TreeNode)reportTree.getModel().getRoot();
            if(root.isSelected()) {
                TreeNode node = (TreeNode)root.getFirstChild();

                while(node != null && node.isSelected()) {
                    processUmlPackage(null, node);
                    node = (TreeNode)node.getNextSibling();
                }

            }
        }
    }

    /**
     * Generates deployment info which was selected by the user
     */
    private void generateDeploymentInfo() {
        if(reportTree != null) {
            TreeNode root = (TreeNode)reportTree.getModel().getRoot();
            if(root.isSelected()) {
                TreeNode node = (TreeNode)root.getFirstChild();

                while(node != null && node.isSelected()) {
                    if(node.getUserObject() instanceof UMLDeploymentDiagram) {
                        DeploymentDiagramHelper.generateDiagramInfo(document, null, (UMLDeploymentDiagram)node.getUserObject(), generateDiagrams);
                    }
                    node = (TreeNode)node.getNextSibling();
                }

            }
        }
    }

    /**
     * Processes all class diagrams which are childrens in the content tree for <i>node</i>
     *
     * @param node    root node for class diagrams which will be processed
     * @param section current section
     */
    private void processAllClassDiagrams(TreeNode node, Section section) {
        if(node == null) return;
        TreeNode tNode = (TreeNode)node.getFirstChild();

        while(tNode != null) {
            Object el = tNode.getUserObject();
            if(el instanceof UMLClassDiagram && tNode.isSelected()) {
                ClassDiagramHelper.generateDiagramInfo(document, section, (UMLClassDiagram)el, generateDiagrams);
                if(section != null) {
                    section.add(Chunk.NEXTPAGE);
                }
            }
            tNode = (TreeNode)tNode.getNextSibling();
        }

    }

    /**
     * Processes all collaboration and sequence diagrams which are children in the content tree
     * for <i>node</i>
     *
     * @param node    root node for collaboration and sequence diagrams which will be processed
     * @param section current section
     */
    private void processAllSequenceCollaborationActivityDiagrams(TreeNode node, Section section) {
        if(node == null) return;

        TreeNode tNode = (TreeNode)node.getFirstChild();
        while(tNode != null) {
            Object el = tNode.getUserObject();
            if(tNode.isSelected()) {
                if(el instanceof UMLSequenceDiagram) {
                    SequenceDiagramHelper.generateDiagramInfo(document, section, (UMLSequenceDiagram)el, generateDiagrams);
                } else if(el instanceof UMLCollaborationDiagram) {
                    CollaborationDiagramHelper.generateDiagramInfo(document, section, (UMLCollaborationDiagram)el, generateDiagrams);
                } else if(el instanceof UMLActivityDiagram) {
                    ActivityDiagramHelper.generateDiagramInfo(document, section, (UMLActivityDiagram)el, generateDiagrams);
                }
            }
            tNode = (TreeNode)tNode.getNextSibling();
        }

    }

    /**
     * Recursively processes uml package: generates info of the selected uml package
     * and explores its subpackages
     *
     * @param packageNode an instance of <i>TreeNode</i> class, which represents uml package to process
     * @param section     current section
     */
    private void processUmlPackage(Section section, TreeNode packageNode) {
        if(Model.getFacade().isAPackage(packageNode.getUserObject()) && packageNode.isSelected()) {
            boolean firstChapter = section == null;
            String name = ReportUtils.getElementName(packageNode.getUserObject());

            if(firstChapter) {
                section = generateNewChapter(Translator.localize("argopdf.report.part.package.title") + " " + name, false);
            } else {
                Paragraph title = ReportUtils.generateTitle(Translator.localize("argopdf.report.part.package.title") + " " + name, 0, false);
                section = section.addSection(title, section.depth() + 1);
            }

            if(!packageNode.isLeaf()) {
                processAllClassDiagrams(packageNode, section);
                processAllSequenceCollaborationActivityDiagrams(packageNode, section);
            }

            if(!packageNode.isLeaf()) {
                TreeNode node = (TreeNode)packageNode.getFirstChild();
                while(node != null) {
                    if(Model.getFacade().isAPackage(node.getUserObject())) {
                        processUmlPackage(section, node);
                    }
                    node = (TreeNode)node.getNextSibling();
                }
            }


            if(firstChapter) {
                try {
                    document.add(section);
                } catch(DocumentException ex) {
                    LOG.debug(ex.getMessage());
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
     * Saves options, which can be changed during report generation
     */
    private void saveOptions() {
        chapterNumber = 0;

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
