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

import javax.swing.*;
import javax.swing.tree.TreePath;
import javax.swing.tree.DefaultTreeModel;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 * Represents mouse listener for the TreeNode
 *
 * @author Dmitry Churbanau
 * @version 0.1
 */
public class TreeNodeSelectionListener extends MouseAdapter {
    
    private JTree tree;

    TreeNodeSelectionListener(JTree tree) {
        this.tree = tree;
    }

    public void mouseClicked(MouseEvent e) {
        int x = e.getX();
        int y = e.getY();
        int row = tree.getRowForLocation(x, y);
        TreePath path = tree.getPathForRow(row);
        if (path != null) {
            TreeNode node = (TreeNode)path.getLastPathComponent();
            if(node.isChildChecked()) {
                node.setChildChecked(false);
                node.setSelected(true);
                ((DefaultTreeModel) tree.getModel()).nodeChanged(node);
//                if (row == 0) {
                  tree.revalidate();
                  tree.repaint();
  //              }
            } else {
                boolean isSelected = ! (node.isSelected());
                node.setSelected(isSelected);
                ((DefaultTreeModel) tree.getModel()).nodeChanged(node);
                if(isSelected && node.getParent() != null) {
                    ((TreeNode)node.getParent()).setChildChecked(isSelected);
                    ((TreeNode)node.getParent()).setSelected(isSelected);
                    ((DefaultTreeModel) tree.getModel()).nodeChanged(node.getParent());
                }
    //            if (row == 0) {
                  tree.revalidate();
                  tree.repaint();
      //          }
            }
        }
    }
}
