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
package org.argouml.ui.argopdf;

import org.argouml.moduleloader.ModuleInterface;
import org.argouml.ui.cmd.GenericArgoMenuBar;
import org.argouml.ui.ProjectBrowser;
import org.argouml.ui.ArgoFrame;
import org.apache.log4j.Logger;
import org.argouml.argopdf.ui.ArgoPDFDialog;

import javax.swing.*;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

/**
 * This class manages enabling, disabling and identifying the ArgoPDF module
 *
 * @author Dzmitry Churbanau
 * @since 0.1
 */
public class ArgoPDFMenuPlugin implements ModuleInterface, ActionListener {

    /**
     * Logger
     */
    private static final Logger LOG = Logger.getLogger(ArgoPDFMenuPlugin.class);

    /**
     * The menu item which represents ArgoPDF in ArgoUML.
     */
    private JMenuItem menuItem;

    private ArgoPDFDialog argoPDFDialog;

    /**
     *  Class constructor.
     */
    public ArgoPDFMenuPlugin() {
        menuItem = new JMenuItem("ArgoPDF");
        menuItem.addActionListener(this);

        argoPDFDialog = new ArgoPDFDialog(ArgoFrame.getInstance(), "ArgoPDF", true);
    }

    //////////////////////////////////////////////////////////
    //  Methods of ModuleInterface

    /**
     * @see org.argouml.moduleloader.ModuleInterface#enable()
     */
    public boolean enable() {
        //register ArgoPDF in ArgoUMLs Tools menu
        GenericArgoMenuBar menubar = (GenericArgoMenuBar) ProjectBrowser.getInstance().getJMenuBar();
        menubar.getTools().add(menuItem);
	    return true;        
    }

    /**
     * @see org.argouml.moduleloader.ModuleInterface#disable()
     */
    public boolean disable() {
        //disable ArgoPDF in ArgoUMLs Tools menu
	    GenericArgoMenuBar menubar = (GenericArgoMenuBar) ProjectBrowser.getInstance().getJMenuBar();
	    menubar.getTools().remove(menuItem);
	    return true;
    }

    /**
     * @see org.argouml.moduleloader.ModuleInterface#getName()
     */
    public String getName() {
        return "ArgoPDF";
    }

    /**
     * @see org.argouml.moduleloader.ModuleInterface#getInfo(int)
     */
    public String getInfo(int type) {
        switch (type) {
            case DESCRIPTION:
                return "This module is a report generator tool.";
            case AUTHOR:
                return "Dzmitry Churbanau";
            case VERSION:
                return "0.1";
            default:
                return null;
        }
    }

    //  End of ModuleInterface methods
    //////////////////////////////////////////////////////////


    /**
     * Show ArgoPDF dialog
     */
    public void actionPerformed(ActionEvent event) {
        LOG.info("Showing ArgoPDF Dialog");
        argoPDFDialog.setVisible(true);
    }
}
