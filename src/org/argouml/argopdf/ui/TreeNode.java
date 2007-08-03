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

import org.argouml.model.Model;
import org.argouml.ui.explorer.rules.GoModelToElements;
import org.argouml.uml.diagram.static_structure.ui.UMLClassDiagram;
import org.argouml.kernel.ProjectManager;
import org.omg.uml.modelmanagement.UmlPackage;

import javax.swing.tree.DefaultMutableTreeNode;
import java.util.Enumeration;
import java.util.Collection;
import java.util.Vector;

/**
 * TreeNode is a part of a report contents tree, which is displayed in the contents
 * area on the 'General' tab in the ArgoPDF dialog
 *
 * @author Dmitry Churbanau
 * @version 0.1
 */
public class TreeNode extends DefaultMutableTreeNode {

    private boolean isSelected;

    public TreeNode() {
        this(null);
    }

    public TreeNode(Object userObject) {
        this(userObject, true, false);

        if(Model.getFacade().isAPackage(userObject)) {
            Vector diagrams = ProjectManager.getManager().getCurrentProject().getDiagrams();
            for(Object el : diagrams) {
                if (el instanceof UMLClassDiagram && userObject.equals(((UMLClassDiagram)el).getNamespace())) {
                    this.add(new TreeNode(el));
                }
            }

            Collection packages = (new GoModelToElements()).getChildren(userObject);
            for(Object el : packages) {
                if(Model.getFacade().isAPackage(el)) {
                    this.add(new TreeNode(el));
                }
            }

        }
        
    }

    public TreeNode(Object userObject, boolean allowsChildren, boolean isSelected) {
        super(userObject, allowsChildren);
        this.isSelected = isSelected;
    }

    public void setSelected(boolean isSelected) {
        this.isSelected = isSelected;

        if (children != null) {
            Enumeration e = children.elements();
            while (e.hasMoreElements()) {
              TreeNode node = (TreeNode) e.nextElement();
              node.setSelected(isSelected);
            }
        }
    }

    public boolean isSelected() {
        return isSelected;
    }
}
