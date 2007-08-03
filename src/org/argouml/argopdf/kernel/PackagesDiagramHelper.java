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

import org.apache.log4j.Logger;
import org.omg.uml.modelmanagement.UmlPackage;
import org.argouml.kernel.ProjectManager;
import org.argouml.uml.diagram.static_structure.ui.UMLClassDiagram;

import java.util.Vector;

import com.lowagie.text.Section;
import com.lowagie.text.Document;

/**
 * Contains helper methods for processing packages
 *
 * @author Dmitry Churbanau
 * @version 0.1
 */
public class PackagesDiagramHelper {

    private static final Logger LOG = Logger.getLogger(PackagesDiagramHelper.class);

    /**
     * Generates info of the uml package (package which is a node of the contents tree). Info includes:
     *           -  class diagrams
     *
     * @param document   current document instance
     * @param section    section of the uml package info
     * @param umlPackage an instance of <i>UmlPackage</i> class, which will be processed
     */
    public static void generateContentPackageInfo(Document document, Section section, UmlPackage umlPackage) {
        if(umlPackage == null) return;
        LOG.debug("process node package " + umlPackage.getName());

        Vector diagrams  = ProjectManager.getManager().getCurrentProject().getDiagrams();
        for(Object el : diagrams) {

            if(el instanceof UMLClassDiagram && umlPackage.equals(((UMLClassDiagram)el).getNamespace())) {
                ClassDiagramHelper.generateDiagramInfo(document, section, (UMLClassDiagram)el);
            }
            
        }

        System.out.println("PackagesDiagramHelper.generateContentPackageInfo umlPackage.hashCode() = '"+umlPackage.hashCode()+"'");
    }

}
