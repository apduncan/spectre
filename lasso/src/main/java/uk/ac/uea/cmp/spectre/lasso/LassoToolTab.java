/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.ac.uea.cmp.spectre.lasso;

import uk.ac.uea.cmp.spectre.core.ui.gui.StatusTracker;
import uk.ac.uea.cmp.spectre.core.ui.gui.ToolHost;

import java.io.File;

/**
 *
 * @author Anthony Duncan
 */
public interface LassoToolTab {
    String tabTitle();
    void runTool(File input, File output, ToolHost host, StatusTracker tracker);
    void setRunning(boolean running);
}
