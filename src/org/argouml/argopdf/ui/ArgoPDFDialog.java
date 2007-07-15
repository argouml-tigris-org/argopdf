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
package org.argouml.argopdf.ui;

import org.argouml.i18n.Translator;
import org.argouml.kernel.ProjectManager;
import org.argouml.kernel.Project;
import org.argouml.uml.diagram.use_case.ui.UMLUseCaseDiagram;
import org.argouml.uml.UseCases;
import org.argouml.argopdf.kernel.PdfReport;
import org.argouml.argopdf.kernel.IReport;
import org.tigris.swidgets.LabelledLayout;

import javax.swing.*;
import javax.swing.filechooser.FileFilter;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.Vector;
import java.util.Iterator;


/**
 *
 * The dialog is displayed when ArgoPDF is started from the ArgoUML menu.
 *
 * @author Dzmitry Churbanau
 * @version 0.1
 */
public class ArgoPDFDialog extends JDialog {

    public static String PDF = ".pdf";

    public static String JPG  = ".jpg";
    public static String JPEG = ".jpeg";
    public static String GIF  = ".gif";
    public static String PNG  = ".png";
    public static String TIF  = ".tif";
    public static String TIFF = ".tiff";

    //default size of the ArgoPDF dialof
    private static final Dimension DEFAULT_SIZE = new Dimension(640, 480);

    //Field which contains path, where report will be saved
    private JTextField pathField;
    //Check box 'Generate Table of contents'
    private JCheckBox generateToC;
    //Check box 'Generate diagrams'
    private JCheckBox generateDiagrams;
    //Check box 'Generate title page'
    private JCheckBox generateTitlePage;
    //Field which contains path to the logo
    private JTextField logoPath;
    //Field which contains title of the report
    private JTextField title;
    //Field which contains author of the report
    private JTextField author;
    //Component which contains tree of report contents 
    private JScrollPane scrollPane;
    //Represents current ArgoUML project
    private Project currentProject;
    //Represents report contents tree
    private JTree tree;
    //File chooser of the report
    JFileChooser reportChooser;
    //Logo chooser of the report
    JFileChooser logoChooser;

    /**
     * ArgoPDF dialog constructor
     *
     * @see javax.swing.JDialog#JDialog(Frame, String, boolean);
     */
    public ArgoPDFDialog(Frame parent, String title, boolean modal) {
        super(parent, title, modal);
        initComponents();
    }

    public void setCurrentProject(Project project) {
        this.currentProject = project;
    }

    /**
     * Inits the components of ArgoPDF dialog
     */
    private void initComponents() {
    	setSize(DEFAULT_SIZE);
        setLayout(new BorderLayout());
        add(createTopPanel(), BorderLayout.NORTH);

        JTabbedPane optionsTabbedPane = new JTabbedPane();
        optionsTabbedPane.addTab(Translator.localize("argopdf.dialog.tab.general.name"), createGeneralTab());
        optionsTabbedPane.addTab(Translator.localize("argopdf.dialog.tab.title.page.name"), createTitlePageTab());
        add(optionsTabbedPane, BorderLayout.CENTER);

        add(createBottomPanel(), BorderLayout.SOUTH);
    }

    /**
     * Creates top JPanel, which includes:
     * <u>
     *   <li>Label 'Path to the report'</li>
     *   <li>Input field, which contains path to the report</li>
     *   <li>Button thanks to which user can select path where to save report</li>
     * <u>
     *
     * @return JPanel which contains top components of the dialog
     */
    private JPanel createTopPanel() {
        JPanel topPanel = new JPanel(new GridBagLayout());
        GridBagConstraints topConstraints = new GridBagConstraints();

        topConstraints.fill = GridBagConstraints.NONE;
        topConstraints.weightx = 0.0;
        topConstraints.insets = new Insets(5, 10, 0, 0);
        JLabel path = new JLabel(Translator.localize("argopdf.dialog.top.panel.path.label")+":  ");
        topPanel.add(path, topConstraints);

        topConstraints.fill = GridBagConstraints.BOTH;
        topConstraints.weightx = 1.0;
        topConstraints.insets = new Insets(5, 0, 0, 0);
        pathField = new JTextField("", 20);
        topPanel.add(pathField, topConstraints);

        JButton saveButton = new JButton(". . .");
        topConstraints.fill = GridBagConstraints.NONE;
        topConstraints.gridwidth = GridBagConstraints.REMAINDER;
        topConstraints.weightx = 0.0;
        topConstraints.insets = new Insets(5, 5, 0, 5);
        topPanel.add(saveButton, topConstraints);

        saveButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {

                if(reportChooser == null) {
                    reportChooser = new JFileChooser();

                    reportChooser.setFileFilter(new FileFilter() {
                        public boolean accept(File file) {
                            String path = file.getPath();
                            if(file.isDirectory()) {
                                return true;
                            }

                            return (path != null && path.toLowerCase().endsWith(PDF));
                        }

                        public String getDescription() {
                            return "PDF Files(*.pdf)"; //todo translate or not?
                        }
                    });
                }
                int option = reportChooser.showSaveDialog(ArgoPDFDialog.this);
                if (option == JFileChooser.APPROVE_OPTION) {
                  if (reportChooser.getSelectedFile() != null) {
                      String path = reportChooser.getSelectedFile().getPath();
                      if(path.toLowerCase().endsWith(PDF)) {
                          pathField.setText(path);
                      } else {
                          pathField.setText(path+PDF);
                      }
                  }
                }                

            }
        });

        return topPanel;
    }

    /**
     * Creates bottom panel which contains two buttons: 'Generate' and 'Cancel'
     *
     * @return JPanel which contains bottom components of the dialog
     */
    private JPanel createBottomPanel() {
        JPanel bottomPanel = new JPanel(new GridBagLayout());
        bottomPanel.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);

        GridBagConstraints bottomConstraints = new GridBagConstraints();

        JButton cancelButton = new JButton(Translator.localize("argopdf.dialog.tab.general.button.cancel"));
        bottomConstraints.fill = GridBagConstraints.NONE;
        bottomConstraints.weightx = 0.0;
        bottomConstraints.insets = new Insets(3, 5, 3, 0);
        bottomPanel.add(cancelButton, bottomConstraints);
        cancelButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                closeDialog();
            }
        });

        bottomConstraints.weightx = 1.0;
        bottomConstraints.fill = GridBagConstraints.NONE;
        bottomConstraints.anchor = GridBagConstraints.EAST;
        bottomConstraints.insets = new Insets(3, 5, 3, 0);
        JButton generateButton = new JButton(Translator.localize("argopdf.dialog.tab.general.button.generate"));
        bottomPanel.add(generateButton, bottomConstraints);
        generateButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                
                IReport report = new PdfReport();
                setOptions(report);
                String result = report.generateReport();
                if(result != null) {
                    JOptionPane.showMessageDialog(ArgoPDFDialog.this, result);
                    return;
                }

                JOptionPane.showMessageDialog(ArgoPDFDialog.this, Translator.localize("argopdf.report.message.report.generated"));

                closeDialog();
            }

            private void setOptions(IReport report) {
                report.setPath(pathField.getText());
                report.setTitle(title.getText());
                report.setAuthor(author.getText());
                report.setLogoPath(logoPath.getText());

                report.setGenerateTitlePage(generateTitlePage.isSelected());
                report.setGenerateTableOfContents(generateToC.isSelected());
                report.setGenerateDiagrams(generateDiagrams.isSelected());
                report.setTree(tree);
            }
        });

        return bottomPanel;
    }

    /**
     * Creates one of the tabs ('General' tab) where user can select main options of the report
     * and define the structure of the report
     *
     * @return JPanel which includes main components of the report and structure of the report
     */
    private JPanel createGeneralTab() {
        final JPanel tab = new JPanel();
        tab.setLayout(new BoxLayout(tab, BoxLayout.X_AXIS));

        JPanel optionsPanel = new JPanel();
        optionsPanel.setLayout(new BoxLayout(optionsPanel, BoxLayout.Y_AXIS));
        optionsPanel.setBorder(new TitledBorder(org.argouml.cognitive.Translator.localize("argopdf.dialog.tab.general.options.title")));
        optionsPanel.setMaximumSize(new Dimension(DEFAULT_SIZE.width/3, 1000));
        optionsPanel.setMinimumSize(new Dimension(DEFAULT_SIZE.width/3, DEFAULT_SIZE.height));

        generateToC = new JCheckBox(Translator.localize("argopdf.dialog.tab.general.options.checkbox.generate.table.of.contents"), true);
        optionsPanel.add(generateToC);

        generateDiagrams = new JCheckBox(Translator.localize("argopdf.dialog.tab.general.options.checkbox.generate.diagrams"), true);
        optionsPanel.add(generateDiagrams);
        tab.add(optionsPanel);

        scrollPane = new JScrollPane();
        scrollPane.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        scrollPane.setBackground(Color.WHITE);
        fillTreeScrollPane();
        tab.add(scrollPane);

        return tab;
    }

    /**
     * Creates new tree of report contents and tie it with scroll pane
     */
    public void fillTreeScrollPane() {
        //prevent of too early creation
        if(scrollPane == null) return;
        scrollPane.getViewport().add(createTreeOfContentsArea());
    }


    /**
     * Creates one of the tabs ('Title page' tab) where user can select options of the title page
     * of the report
     *
     * @return JPanel which includes options of the title page of the report
     */
    private JPanel createTitlePageTab() {
        final JPanel tab = new JPanel();
        tab.setLayout(new BorderLayout());
        int labelGap = 5;
        int componentGap = 1;
        JPanel top = new JPanel(new LabelledLayout(labelGap, componentGap));

        generateTitlePage = new JCheckBox(Translator.localize("argopdf.dialog.tab.title.page.options.generate.title.page"), true);
        top.add(generateTitlePage);

        JPanel logoPanel = new JPanel(new GridBagLayout());
        GridBagConstraints constr = new GridBagConstraints();

        constr.weightx = 1.0;
        constr.fill = GridBagConstraints.HORIZONTAL;
        logoPath = new JTextField("");
        logoPanel.add(logoPath, constr);

        constr.weightx = 0.0;
        constr.fill = GridBagConstraints.NONE;
        constr.gridwidth = GridBagConstraints.REMAINDER;
        constr.insets = new Insets(0, 5, 0, 0);
        JButton logoPathBtn = new JButton(". . .");
        logoPathBtn.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {

                if(logoChooser == null) {
                    logoChooser = new JFileChooser();

                    logoChooser.setFileFilter(new FileFilter() {
                        public boolean accept(File file) {
                            String path = file.getPath();
                            if(file.isDirectory()) {
                                return true;
                            }
                            if(path != null) {
                                path = path.toLowerCase();

                                return (path.endsWith(JPG)  ||
                                        path.endsWith(JPEG) ||
                                        path.endsWith(GIF)  ||
                                        path.endsWith(PNG)  ||
                                        path.endsWith(TIF)  ||
                                        path.endsWith(TIFF));
                            }

                            return false;
                        }

                        public String getDescription() {
                            return "Image files (*.jpg, *.jpeg, *.gif, *.png, *.tif, *.tiff)";
                        }
                    });
                }
                int option = logoChooser.showOpenDialog(ArgoPDFDialog.this);
                if (option == JFileChooser.APPROVE_OPTION) {
                  if (logoChooser.getSelectedFile() != null) {
                      String path = logoChooser.getSelectedFile().getPath();

                      if(path.endsWith(JPG) || path.endsWith(JPEG) || path.endsWith(GIF) ||
                         path.endsWith(PNG) || path.endsWith(TIF) || path.endsWith(TIFF)) {
                          logoPath.setText(logoChooser.getSelectedFile().getPath());
                      }
                  }
                }

            }
        });
        logoPanel.add(logoPathBtn, constr);
        
        constr.insets = new Insets(0, 0, 0, 0);
        JLabel label = new JLabel(Translator.localize("argopdf.dialog.tab.title.page.logo.image.note") + ":");

        label.setLabelFor(logoPanel);
        top.add(label);
        top.add(logoPanel);

        label = new JLabel(Translator.localize("argopdf.dialog.tab.title.page.title.note") + ":");
        title = new JTextField();
        label.setLabelFor(title);
        top.add(label);
        top.add(title);

	    label = new JLabel(Translator.localize("argopdf.dialog.tab.title.page.author.name") + ":");
        author = new JTextField();
        label.setLabelFor(author);
        top.add(label);
        top.add(author);

        top.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
	    tab.add(top, BorderLayout.NORTH);

        return tab;
    }

    /**
     * Creates tree of the project data, which can be included in the report
     *
     * @return tree for the contents area of the dialog window
     */
    private JTree createTreeOfContentsArea() {

        if(this.currentProject == null) {
            this.currentProject = ProjectManager.getManager().getCurrentProject();
        }

        Project currentProject = this.currentProject;
        Vector diagrams = currentProject.getDiagrams();
        Iterator iter = diagrams.iterator();

        TreeNode node = new TreeNode(currentProject);

        TreeNode useCaseFolderNode = new TreeNode(new UseCases());
        while(iter.hasNext()) {
            Object diagram = iter.next();
            if(diagram instanceof UMLUseCaseDiagram) {
                useCaseFolderNode.add(new TreeNode(diagram));
            }
        }
        node.add(useCaseFolderNode);
        node.add(new TreeNode(new UMLUseCaseDiagram()));

        tree = new JTree(node);
        tree.setCellRenderer(new TreeRenderer());
        tree.addMouseListener(new TreeNodeSelectionListener(tree));
        tree.setShowsRootHandles(true);

        return tree;
    }

    /**
     * Closes dialog
     */
    private void closeDialog() {

        this.setVisible(false);
    }
}