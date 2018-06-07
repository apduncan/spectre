/*
 *  Suite of PhylogEnetiC Tools for Reticulate Evolution (SPECTRE)
 *  Copyright (C) 2017  UEA School of Computing Sciences
 *
 *  This program is free software: you can redistribute it and/or modify it under the term of the GNU General Public
 *  License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any
 *  later version.
 *
 *  This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied
 *  warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License along with this program.  If not, see
 *  <http://www.gnu.org/licenses/>.
 */

package uk.ac.uea.cmp.spectre.lasso;
/*
 *  Suite of PhylogEnetiC Tools for Reticulate Evolution (SPECTRE)
 *  Copyright (C) 2017  UEA School of Computing Sciences
 *
 *  This program is free software: you can redistribute it and/or modify it under the term of the GNU General Public
 *  License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any
 *  later version.
 *
 *  This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied
 *  warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License along with this program.  If not, see
 *  <http://www.gnu.org/licenses/>.
 */

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.ac.uea.cmp.spectre.core.ui.gui.StatusTracker;
import uk.ac.uea.cmp.spectre.core.ui.gui.ToolHost;
import uk.ac.uea.cmp.spectre.core.util.LogConfig;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.io.File;
import java.net.URISyntaxException;
import java.util.Arrays;

public class LassoGUI extends JFrame implements ToolHost {
    // UI designed using NetBeans form designer
    // IntelliJ form designer generates code which is IntelliJ speicific, NetBeans generate standard Java class

    /**
     * Creates new form uk.ac.uea.cmp.spectre.lasso.LassoGUI
     */
    public LassoGUI() {
        initComponents();
        initTooltips();
        try {
            setIconImage((new ImageIcon(uk.ac.uea.cmp.spectre.core.ui.gui.LookAndFeel.getLogoFilePath()).getImage()));
        } catch (URISyntaxException e) {
            log.debug("Could not load logo");
        }
        tracker = new StatusTracker(prgProgress, lblProgress);
        runner = new LassoRunner(this);
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">
    private void initComponents() {

        pnlClique = new javax.swing.JPanel();
        cmbCliqueAlg = new javax.swing.JComboBox<>();
        lblCliqueAlg = new javax.swing.JLabel();
        spnrCliqueAttempts = new javax.swing.JSpinner();
        lblCliqueAttempt = new javax.swing.JLabel();
        pnlIO = new javax.swing.JPanel();
        lblInput = new javax.swing.JLabel();
        txtInput = new javax.swing.JTextField();
        btnInput = new javax.swing.JButton();
        lblOutput = new javax.swing.JLabel();
        txtOutput = new javax.swing.JTextField();
        btnOutput = new javax.swing.JButton();
        pnlDistance = new javax.swing.JPanel();
        lblDistanceAlg = new javax.swing.JLabel();
        cmbDistanceAlg = new javax.swing.JComboBox<>();
        pnlLasso = new javax.swing.JPanel();
        lblRuns = new javax.swing.JLabel();
        snprRuns = new javax.swing.JSpinner();
        prgProgress = new javax.swing.JProgressBar();
        btnExecute = new javax.swing.JButton();
        lblProgress = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("uk.ac.uea.cmp.spectre.lasso.Lasso");

        pnlClique.setBorder(javax.swing.BorderFactory.createTitledBorder("Clique Options"));
        pnlClique.setToolTipText("");
        pnlClique.setName(""); // NOI18N

        cmbCliqueAlg.setModel(getCliqueOptions());
        cmbCliqueAlg.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cmbCliqueAlgActionPerformed(evt);
            }
        });

        lblCliqueAlg.setText("Algorithm");

        spnrCliqueAttempts.setModel(new javax.swing.SpinnerNumberModel(10, 0, null, 1));
        spnrCliqueAttempts.setValue(10);

        lblCliqueAttempt.setText("Attempts");

        javax.swing.GroupLayout pnlCliqueLayout = new javax.swing.GroupLayout(pnlClique);
        pnlClique.setLayout(pnlCliqueLayout);
        pnlCliqueLayout.setHorizontalGroup(
                pnlCliqueLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(pnlCliqueLayout.createSequentialGroup()
                                .addContainerGap()
                                .addGroup(pnlCliqueLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                        .addComponent(lblCliqueAlg)
                                        .addComponent(lblCliqueAttempt))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(pnlCliqueLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                        .addComponent(cmbCliqueAlg, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                        .addGroup(pnlCliqueLayout.createSequentialGroup()
                                                .addComponent(spnrCliqueAttempts, javax.swing.GroupLayout.PREFERRED_SIZE, 51, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                .addGap(0, 0, Short.MAX_VALUE)))
                                .addContainerGap())
        );
        pnlCliqueLayout.setVerticalGroup(
                pnlCliqueLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(pnlCliqueLayout.createSequentialGroup()
                                .addGroup(pnlCliqueLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                        .addComponent(cmbCliqueAlg, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addComponent(lblCliqueAlg))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(pnlCliqueLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                        .addComponent(spnrCliqueAttempts, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addComponent(lblCliqueAttempt)))
        );

        pnlIO.setBorder(javax.swing.BorderFactory.createTitledBorder("Input/Output"));

        lblInput.setText("Input");

        btnInput.setText("...");
        btnInput.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnInputActionPerformed(evt);
            }
        });

        lblOutput.setText("Output");

        btnOutput.setText("...");
        btnOutput.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnOutputActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout pnlIOLayout = new javax.swing.GroupLayout(pnlIO);
        pnlIO.setLayout(pnlIOLayout);
        pnlIOLayout.setHorizontalGroup(
                pnlIOLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(pnlIOLayout.createSequentialGroup()
                                .addContainerGap()
                                .addGroup(pnlIOLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                        .addComponent(lblOutput)
                                        .addComponent(lblInput))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(pnlIOLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                        .addGroup(pnlIOLayout.createSequentialGroup()
                                                .addComponent(txtInput, javax.swing.GroupLayout.PREFERRED_SIZE, 247, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                .addComponent(btnInput))
                                        .addGroup(pnlIOLayout.createSequentialGroup()
                                                .addComponent(txtOutput, javax.swing.GroupLayout.PREFERRED_SIZE, 247, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                .addComponent(btnOutput)))
                                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        pnlIOLayout.setVerticalGroup(
                pnlIOLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(pnlIOLayout.createSequentialGroup()
                                .addGroup(pnlIOLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                        .addComponent(txtInput, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addComponent(btnInput)
                                        .addComponent(lblInput))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(pnlIOLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                        .addComponent(txtOutput, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addComponent(btnOutput)
                                        .addComponent(lblOutput)))
        );

        pnlDistance.setBorder(javax.swing.BorderFactory.createTitledBorder("Distance Update Options"));

        lblDistanceAlg.setText("Algorithm");

        cmbDistanceAlg.setModel(getDistanceOptions());
        cmbDistanceAlg.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cmbDistanceAlgActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout pnlDistanceLayout = new javax.swing.GroupLayout(pnlDistance);
        pnlDistance.setLayout(pnlDistanceLayout);
        pnlDistanceLayout.setHorizontalGroup(
                pnlDistanceLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, pnlDistanceLayout.createSequentialGroup()
                                .addContainerGap()
                                .addComponent(lblDistanceAlg)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(cmbDistanceAlg, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addContainerGap())
        );
        pnlDistanceLayout.setVerticalGroup(
                pnlDistanceLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(pnlDistanceLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                .addComponent(cmbDistanceAlg, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addComponent(lblDistanceAlg))
        );

        pnlLasso.setBorder(javax.swing.BorderFactory.createTitledBorder("uk.ac.uea.cmp.spectre.lasso.Lasso Options"));

        lblRuns.setText("Runs");

        snprRuns.setModel(new javax.swing.SpinnerNumberModel(10, 0, null, 1));
        snprRuns.setValue(10);

        javax.swing.GroupLayout pnlLassoLayout = new javax.swing.GroupLayout(pnlLasso);
        pnlLasso.setLayout(pnlLassoLayout);
        pnlLassoLayout.setHorizontalGroup(
                pnlLassoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(pnlLassoLayout.createSequentialGroup()
                                .addGap(48, 48, 48)
                                .addComponent(lblRuns)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(snprRuns, javax.swing.GroupLayout.PREFERRED_SIZE, 54, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        pnlLassoLayout.setVerticalGroup(
                pnlLassoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(pnlLassoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                .addComponent(snprRuns, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addComponent(lblRuns))
        );

        btnExecute.setText("Execute");
        btnExecute.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnExecuteActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
                layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(layout.createSequentialGroup()
                                .addContainerGap()
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                        .addComponent(prgProgress, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                        .addComponent(pnlIO, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                        .addComponent(pnlClique, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                        .addComponent(pnlDistance, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                        .addComponent(pnlLasso, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                        .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                                                .addComponent(lblProgress, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                .addComponent(btnExecute)))
                                .addContainerGap())
        );
        layout.setVerticalGroup(
                layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(layout.createSequentialGroup()
                                .addContainerGap()
                                .addComponent(pnlIO, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(pnlClique, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(pnlDistance, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(pnlLasso, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(prgProgress, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                        .addComponent(btnExecute)
                                        .addComponent(lblProgress))
                                .addGap(16, 16, 16))
        );

        pack();
    }// </editor-fold>

    private void btnInputActionPerformed(java.awt.event.ActionEvent evt) {
        final JFileChooser fc = new JFileChooser();
        fc.setDialogTitle("Input matrix");
        fc.setFileSelectionMode(JFileChooser.OPEN_DIALOG);
        if (evt.getSource() == btnInput) {
            int returnVal = fc.showOpenDialog(LassoGUI.this);
            if (returnVal == JFileChooser.APPROVE_OPTION) {
                File file = fc.getSelectedFile();
                String z = file.getAbsolutePath();
                txtInput.setText(z);
            } else {
                log.debug("Open command cancelled by user.");
            }
        }
    }

    private void btnOutputActionPerformed(java.awt.event.ActionEvent evt) {
        final JFileChooser fc = new JFileChooser();
        fc.setDialogTitle("Output");
        fc.addChoosableFileFilter(new FileNameExtensionFilter("Nexus", "nex"));
        if (evt.getSource() == btnOutput) {
            int returnVal = fc.showSaveDialog(LassoGUI.this);
            if (returnVal == JFileChooser.APPROVE_OPTION) {
                File file = fc.getSelectedFile();
                String z = file.getAbsolutePath();
                txtOutput.setText(z);
            } else {
                log.debug("Open command cancelled by user.");
            }
        }
    }

    private void btnExecuteActionPerformed(java.awt.event.ActionEvent evt) {
        // Validate arguments
        File input = new File(txtInput.getText());
        if(!input.exists()) {
            showErrorDialog("Could not find input file");
            return;
        }
        File output = new File(txtOutput.getText());
        Integer attempts = null;
        try {
            attempts = Integer.class.cast(spnrCliqueAttempts.getValue());
        } catch (Exception e) {
            showErrorDialog("Number of clique attempts invalid");
            return;
        }
        Integer runs = null;
        try {
            runs = Integer.class.cast(snprRuns.getValue());
        } catch (Exception e) {
            showErrorDialog("Number of uk.ac.uea.cmp.spectre.lasso.Lasso runs invalid");
            return;
        }
        if(runs < 1 || attempts < 1) {
            showErrorDialog("Numbers of runs and clique attempts must be above 0");
            return;
        }
        // Convert enum strings to enums
        CliqueFinderFactory finder = null;
        try {
            finder = CliqueFinderFactory.valueOf(String.class.cast(cmbCliqueAlg.getSelectedItem()));
        } catch (Exception e) {
            showErrorDialog("Clique finding method invalid");
            return;
        }
        DistanceUpdaterFactory updater = null;
        try {
            updater = DistanceUpdaterFactory.valueOf(String.class.cast(cmbDistanceAlg.getSelectedItem()));
        } catch (Exception e) {
            showErrorDialog("Distance update method invalid");
            return;
        }

        // Create uk.ac.uea.cmp.spectre.lasso.LassoOptions object
        LassoOptions options = new LassoOptions();
        options.setInput(input);
        options.setOutput(output);
        options.setLassoRuns(runs);
        options.setCliqueAttempts(attempts);
        options.setCliqueFinder(finder);
        options.setUpdater(updater);

        // Execute uk.ac.uea.cmp.spectre.lasso.Lasso
        runner.runLasso(options, tracker);
    }

    private void cmbCliqueAlgActionPerformed(java.awt.event.ActionEvent evt) {
        // Use this block to toggle on / off any algorithm specific options
        // Currently only one clique finding method, so no toggling needed currently
    }

    private void cmbDistanceAlgActionPerformed(java.awt.event.ActionEvent evt) {
        // Use this block to toggle on / off any algorithm specific options
        // Currently only one distance update method, so no toggling needed currently
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(LassoGUI.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(LassoGUI.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(LassoGUI.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(LassoGUI.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>
        LogConfig.defaultConfig();
        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new LassoGUI().setVisible(true);
            }
        });
    }

    private DefaultComboBoxModel<String> getCliqueOptions() {
        String[] opts = Arrays.stream(CliqueFinderFactory.values()).map(v -> v.toString()).toArray(String[]::new);
        return new DefaultComboBoxModel<>(opts);
    }

    private DefaultComboBoxModel<String> getDistanceOptions() {
        String[] opts = Arrays.stream(DistanceUpdaterFactory.values()).map(v -> v.toString()).toArray(String[]::new);
        return new DefaultComboBoxModel<>(opts);
    }

    private void initTooltips() {
        //Set up tooltips on some elements based on uk.ac.uea.cmp.spectre.lasso.LassoOptions
        lblCliqueAlg.setToolTipText(LassoOptions.DESC_CLIQUE_FINDER);
        cmbCliqueAlg.setToolTipText(LassoOptions.DESC_CLIQUE_FINDER);
        lblDistanceAlg.setToolTipText(LassoOptions.DESC_DISTANCE_UPDATER);
        cmbDistanceAlg.setToolTipText(LassoOptions.DESC_DISTANCE_UPDATER);
        lblCliqueAttempt.setToolTipText(LassoOptions.DESC_CLIQUE_ATTEMPTS);
        spnrCliqueAttempts.setToolTipText(LassoOptions.DESC_CLIQUE_ATTEMPTS);
        lblInput.setToolTipText(LassoOptions.DESC_INPUT);
        txtInput.setToolTipText(LassoOptions.DESC_INPUT);
        txtOutput.setToolTipText(LassoOptions.DESC_OUTPUT);
        lblOutput.setToolTipText(LassoOptions.DESC_OUTPUT);
        lblRuns.setToolTipText(LassoOptions.DESC_LASSO_RUNS);
        snprRuns.setToolTipText(LassoOptions.DESC_LASSO_RUNS);
    }

    @Override
    public void update() {

    }

    @Override
    public void setRunningStatus(boolean running) {

    }

    @Override
    public void showErrorDialog(String message) {
        JOptionPane.showMessageDialog(this,
                message,
                "uk.ac.uea.cmp.spectre.lasso.Lasso Error",
                JOptionPane.ERROR_MESSAGE);
    }

    private static Logger log = LoggerFactory.getLogger(Lasso.class);
    private StatusTracker tracker;
    private LassoRunner runner;
    // Variables declaration - do not modify
    private javax.swing.JButton btnExecute;
    private javax.swing.JButton btnInput;
    private javax.swing.JButton btnOutput;
    private javax.swing.JComboBox<String> cmbCliqueAlg;
    private javax.swing.JComboBox<String> cmbDistanceAlg;
    private javax.swing.JLabel lblCliqueAlg;
    private javax.swing.JLabel lblCliqueAttempt;
    private javax.swing.JLabel lblDistanceAlg;
    private javax.swing.JLabel lblInput;
    private javax.swing.JLabel lblOutput;
    private javax.swing.JLabel lblProgress;
    private javax.swing.JLabel lblRuns;
    private javax.swing.JPanel pnlClique;
    private javax.swing.JPanel pnlDistance;
    private javax.swing.JPanel pnlIO;
    private javax.swing.JPanel pnlLasso;
    private javax.swing.JProgressBar prgProgress;
    private javax.swing.JSpinner snprRuns;
    private javax.swing.JSpinner spnrCliqueAttempts;
    private javax.swing.JTextField txtInput;
    private javax.swing.JTextField txtOutput;
    // End of variables declaration
}