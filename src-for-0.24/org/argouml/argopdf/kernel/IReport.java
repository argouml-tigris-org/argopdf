// $Id: IReport.java 62 2007-08-25 19:18:28Z comp_ $
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

import javax.swing.*;

/**
 * The IReport interface represents the basic interface for report in the ArgoPDF module.
 * To generate report, <i>generateReport()</i> method should be called. But before caling
 * this method, it is recomended to call methods, which set main options of the report:
 *      <u>
 *          <li><i>setPath(String path)</i> - sets the absolute path, where report will be saved</li>
 *          <li><i>setTitle(String title)</i> - sets title of the report</li>
 *          <li><i>setAuthor(String author)</i> - sets author name of the report</li>
 *          <li><i>setLogoPath(String logoPath)</i> - sets the absolute path to the logo image of the report</li>
 *          <li><i>setGenerateTitlePage(boolean value)</i> - defines, whether title page will be generated in the
 *          report. Call this method with parameter 'value' equals to true, if you want to generate title page in
 *          the report.</li>
 *          <li><i>setGenerateTableOfContents(boolean value)</i> - defines, whether table of contents will be
 *          generated in the report. Call this method with parameter 'value' equals to true, if you want to have 
 *          table of contents in the report.</li>
 *          <li><i>setGenerateDiagrams(boolean value)</i> - defines, whether images of diagrams will be generated
 *          in the report. Call this method with parameter 'value' equals to true, if you want to see images of
 *          diagrams in the report.</li>
 *      </u>
 *
 * After calling <i>generateReport()</i> method, report will be generated and saved in the path, which was defined by
 * calling <i>setPath(String path)</i> method. If path will not be defined, report will not be generated and error
 * message should be shown to the user.
 *
 * @author Dmitry Churbanau
 * @version 0.1
 */
public interface IReport {

    /**
     * Method performs the main work - generates report.
     *
     * @return error message, if report was not generated. Otherwise null.
     */
    String generateReport();

    /**
     * Sets the tree, which contains the selected elements of the report. Tree nodes
     * should be the instances of <i>TreeNode</i> class. Each tree node (except of the root node)
     * should contain the instance of one of the next classes as a user object:
     *          <u>
     *              <li><i>org.argouml.uml.diagram.use_case.ui.UMLUseCaseDiagram</i> class</li>
     *          </u>
     *
     * The root node should contain an instance of <i>org.argouml.kernel.Project</i> class.
     * Each node, which is the instance of <i>TreeNode</i> class, has public method <i>isSelected()</i>,
     * which returns true, if this element was selected by the user during making a choise of the
     * report contents. If this element was not selected <i>isSelected()</i> return false.
     *
     * @see org.argouml.argopdf.ui.TreeNode
     *
     * @param tree tree of report elements.
     */
    void setTree(JTree tree);

    /**
     * Defines, whether title page in the report will be generated or not. By default it should be generated.
     *
     * @param value defines, whether title page will be generated in the report or not. If value
     * equals to true, title page will be generated in the report, otherwise not.
     */
    void setGenerateTitlePage(boolean value);

    /**
     * Defines, whether table of contents will be generated in the report or not. By default it should be generated.
     *
     * @param value defines, whether table of contents will be generated in the report or not. If value
     * equals to true, table of contents will be generated in the report, otherwise not. 
     */
    void setGenerateTableOfContents(boolean value);

    /**
     * Defines, whether images of diagrams will be generated in the report or not. By default they should be generated.
     *
     * @param value defines, whether images of diagrams will be generated in the report or not. If value equals
     * to true, images of diagrams will be generated in the report, otherwise not.
     */
    void setGenerateDiagrams(boolean value);

    /**
     * Sets the path, where report will be saved
     *
     * @param path absolute path, where report will be saved
     */
    void setPath(String path);

    /**
     * Sets title of the report
     *
     * @param title title of the report
     */
    void setTitle(String title);

    /**
     * Sets author of the report
     *
     * @param author author of the report
     */
    void setAuthor(String author);

    /**
     * Sets path to the logo of the report. If you don't want to have a logo image on your
     * title page, do not call this method.
     * 
     * @param logoPath absolute path to the logo image of the report
     */
    void setLogoPath(String logoPath);
    
}
