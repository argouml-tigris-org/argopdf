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

/**
 * The ReportITextFlyingSaucerImpl is a datastructure that represents the report
 * in ArgoPDF module which uses iText and Flying Saucer libraries
 *
 * At the beginning, html presentation of the report is generated.
 * Then by the instrumentality of iText and Flying Saucer libraries pdf
 * file is generated.
 *
 * @author Dzmitry Churbanau
 * @version 1.0
 */
public class ReportITextFlyingSaucerImpl implements Report {

    private String title;
    private String author;

    private String path;
    private String logoPath;

    private boolean generateTableOfContents;
    private boolean generateDiagrams;
    private boolean generateTitlePage;

    //StringBuffer, which contains html presentation of the report report
    private StringBuffer reportBuf = new StringBuffer();


    public ReportITextFlyingSaucerImpl(String path) {
        this.path = path;
    }


    /**
     * Generates report
     */
    public void generateReport() {

        reportBuf = new StringBuffer();
        reportBuf.append("<html>");
        if(generateTitlePage) {
            generateTitlePage();
        }
        if(generateTableOfContents) {
            generateTableOfContents();
        }
        generateReportContents();
        reportBuf.append("</html>");
    }

    /**
     * Generates html head of the report
     */
    private void generateHead() {
        reportBuf.append("<head>");
        generateStyle();
        reportBuf.append("</head>");
    }

    /**
     * Generates style of the report
     */
    private void generateStyle() {

    }

    /**
     * Generates html title page of the report
     */
    private void generateTitlePage() {

    }

    /**
     * Generates html table of contents of the report
     */
    private void generateTableOfContents() {

    }

    /**
     * Generates html report contents of the report
     */
    private void generateReportContents() {
        
    }
    
}
