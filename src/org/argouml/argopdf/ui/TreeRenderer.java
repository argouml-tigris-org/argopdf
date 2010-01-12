/* $Id$
 *****************************************************************************
 * Copyright (c) 2009 Contributors - see below
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    comp_
 *****************************************************************************
 *
 * Some portions of this file was previously release using the BSD License:
 */

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

import org.argouml.kernel.Project;
import org.argouml.model.Model;
import org.argouml.application.helpers.ResourceLoaderWrapper;
import org.argouml.uml.diagram.use_case.ui.UMLUseCaseDiagram;
import org.argouml.uml.diagram.static_structure.ui.UMLClassDiagram;
import org.argouml.uml.diagram.sequence.ui.UMLSequenceDiagram;
import org.argouml.uml.diagram.collaboration.ui.UMLCollaborationDiagram;
import org.argouml.uml.diagram.activity.ui.UMLActivityDiagram;
import org.argouml.uml.diagram.deployment.ui.UMLDeploymentDiagram;
import org.argouml.uml.UseCases;
import org.argouml.i18n.Translator;

import javax.swing.*;
import javax.swing.tree.TreeCellRenderer;
import java.awt.*;

/**
 * TreeRenderer is the implementation of a TreeCellRenderer for a TreeNode
 *
 * @author Dmitry Churbanau
 * @version 0.1
 */
public class TreeRenderer extends JPanel implements TreeCellRenderer {

    private JCheckBox check = new JCheckBox();
    private TreeLabel label = new TreeLabel();

    public TreeRenderer() {
        setLayout(null);
        setBackground(Color.WHITE);
        check.setBackground(Color.WHITE);
        add(check);
        add(label);
    }

    public Component getTreeCellRendererComponent(JTree tree, Object value, boolean isSelected,
                                                  boolean expanded, boolean leaf, int row,
                                                  boolean hasFocus) {

        //todo experimentation; must be reimplemented
        Object userObject = ((TreeNode)value).getUserObject();
        String stringValue = "undefined value";
        Icon icon = null;
        if(userObject instanceof Project) {
            Object model = ((Project)userObject).getModel();
            stringValue = Model.getFacade().getName(model);
            icon = ResourceLoaderWrapper.getInstance().lookupIcon(model);
        } else {
            if(userObject instanceof UseCases) {
                stringValue = Translator.localize("argopdf.dialog.tab.general.tree.usecases");
                icon = ResourceLoaderWrapper.getInstance().lookupIcon("Package");
            } else if(userObject instanceof UMLUseCaseDiagram) {
                stringValue = ((UMLUseCaseDiagram)userObject).getName();
                icon = ResourceLoaderWrapper.getInstance().lookupIcon(userObject);
            } else if(userObject instanceof UMLClassDiagram) {
                stringValue = ((UMLClassDiagram)userObject).getName();
                icon = ResourceLoaderWrapper.getInstance().lookupIcon(userObject);
            } else if(userObject instanceof UMLSequenceDiagram) {
                stringValue = ((UMLSequenceDiagram)userObject).getName();
                icon = ResourceLoaderWrapper.getInstance().lookupIcon(userObject);
            } else if(userObject instanceof UMLCollaborationDiagram) {
                stringValue = ((UMLCollaborationDiagram)userObject).getName();
                icon = ResourceLoaderWrapper.getInstance().lookupIcon(userObject);
            } else if(userObject instanceof UMLActivityDiagram) {
                stringValue = ((UMLActivityDiagram)userObject).getName();
                icon = ResourceLoaderWrapper.getInstance().lookupIcon(userObject);
            } else if(userObject instanceof UMLDeploymentDiagram) {
                stringValue = ((UMLDeploymentDiagram)userObject).getName();
                icon = ResourceLoaderWrapper.getInstance().lookupIcon(userObject);
            } else if(Model.getFacade().isAPackage(userObject)) {
                stringValue = Model.getFacade().getName(userObject);
                icon = ResourceLoaderWrapper.getInstance().lookupIcon(userObject);
            }
        }

        if (icon != null) {
            label.setIcon(icon);
        }
        check.setSelected(((TreeNode) value).isSelected());
        label.setText(stringValue);
        
        return this;
    }

    public Dimension getPreferredSize() {
        Dimension d_check = check.getPreferredSize();
        Dimension d_label = label.getPreferredSize();
        return new Dimension(d_check.width + d_label.width,
            (d_check.height < d_label.height ? d_label.height
                : d_check.height));
    }    

    public void doLayout() {
        Dimension d_check = check.getPreferredSize();
        Dimension d_label = label.getPreferredSize();
        int y_check = 0;
        int y_label = 0;
        if (d_check.height < d_label.height) {
          y_check = (d_label.height - d_check.height) / 2;
        } else {
          y_label = (d_check.height - d_label.height) / 2;
        }

        check.setLocation(0, y_check);
        check.setBounds(0, y_check, d_check.width, d_check.height);
        label.setLocation(d_check.width, y_label);
        label.setBounds(d_check.width, y_label, d_label.width, d_label.height);
    }
}
