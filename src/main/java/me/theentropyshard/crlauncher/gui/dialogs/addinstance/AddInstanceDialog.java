/*
 * CRLauncher - https://github.com/CRLauncher/CRLauncher
 * Copyright (C) 2024 CRLauncher
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <https://www.gnu.org/licenses/>.
 */

package me.theentropyshard.crlauncher.gui.dialogs.addinstance;

import me.theentropyshard.crlauncher.CRLauncher;
import me.theentropyshard.crlauncher.Settings;
import me.theentropyshard.crlauncher.cosmic.icon.IconManager;
import me.theentropyshard.crlauncher.gui.components.InstanceItem;
import me.theentropyshard.crlauncher.gui.dialogs.AppDialog;
import me.theentropyshard.crlauncher.gui.utils.MessageBox;
import me.theentropyshard.crlauncher.gui.utils.SwingUtils;
import me.theentropyshard.crlauncher.gui.view.playview.PlayView;
import me.theentropyshard.crlauncher.instance.InstanceAlreadyExistsException;
import me.theentropyshard.crlauncher.instance.InstanceManager;
import me.theentropyshard.crlauncher.logging.Log;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.IOException;

public class AddInstanceDialog extends AppDialog {
    

    private final JTextField nameField;
    private final JTextField groupField;
    private final JButton addButton;
    private final JCheckBox preAlphasBox;

    private boolean nameEdited;

    public AddInstanceDialog(PlayView playView, String groupName) {
        super(CRLauncher.frame, "Add New Instance");

        JPanel root = new JPanel(new BorderLayout());

        InputMap inputMap = root.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), "ESCAPE");

        ActionMap actionMap = root.getActionMap();
        actionMap.put("ESCAPE", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                AddInstanceDialog.this.getDialog().dispose();
            }
        });

        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBorder(new EmptyBorder(10, 10, 0, 10));

        JPanel headerPanelLeftPanel = new JPanel();
        headerPanelLeftPanel.setLayout(new GridLayout(2, 1));
        headerPanelLeftPanel.add(new JLabel("Name:") {{
            this.setVerticalTextPosition(JLabel.CENTER);
            this.setBorder(new EmptyBorder(0, 0, 0, 10));
        }});
        headerPanelLeftPanel.add(new JLabel("Group:") {{
            this.setVerticalTextPosition(JLabel.CENTER);
            this.setBorder(new EmptyBorder(0, 0, 0, 10));
        }});

        JPanel headerPanelRightPanel = new JPanel();
        headerPanelRightPanel.setLayout(new GridLayout(2, 1));

        this.nameField = new JTextField();
        this.nameField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyTyped(KeyEvent e) {
                AddInstanceDialog.this.nameEdited = true;
            }
        });
        headerPanelRightPanel.add(this.nameField);

        this.groupField = new JTextField(groupName);
        headerPanelRightPanel.add(this.groupField);

        headerPanel.add(headerPanelLeftPanel, BorderLayout.WEST);
        headerPanel.add(headerPanelRightPanel, BorderLayout.CENTER);

        JCheckBox updateToLatestAutomatically = new JCheckBox("Automatically update to the latest version");
        updateToLatestAutomatically.setSelected(CRLauncher.getInstance().getSettings().settingsDialogUpdateToLatest);
        updateToLatestAutomatically.addActionListener(e -> {
            Settings settings = CRLauncher.getInstance().getSettings();
            settings.settingsDialogUpdateToLatest = !settings.settingsDialogUpdateToLatest;
        });
        headerPanel.add(updateToLatestAutomatically, BorderLayout.SOUTH);

        root.add(headerPanel, BorderLayout.NORTH);

        JTable versionsTable = new JTable();
        versionsTable.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

                if (c instanceof JLabel label) {
                    if (column == 0) {
                        label.setHorizontalAlignment(JLabel.LEFT);
                    } else {
                        label.setHorizontalAlignment(JLabel.CENTER);
                    }
                }

                return c;
            }
        });
        SwingUtils.setJTableColumnsWidth(versionsTable, 70, 15, 15);
        versionsTable.getTableHeader().setEnabled(false);
        versionsTable.getSelectionModel().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        CosmicVersionsTableModel tableModel = new CosmicVersionsTableModel(this, versionsTable);
        versionsTable.setModel(tableModel);

        versionsTable.getSelectionModel().addListSelectionListener(e -> {
            if (this.nameEdited) {
                return;
            }

            int selectedRow = versionsTable.getSelectedRow();

            if (selectedRow != -1) {
                selectedRow = versionsTable.convertRowIndexToModel(selectedRow);
                this.nameField.setText(String.valueOf(versionsTable.getModel().getValueAt(selectedRow, 0)));
            }
        });

        JScrollPane scrollPane = new JScrollPane(
                versionsTable,
                JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
                JScrollPane.HORIZONTAL_SCROLLBAR_NEVER
        );

        JPanel filterPanel = new JPanel();
        filterPanel.setLayout(new GridBagLayout());

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.anchor = GridBagConstraints.NORTH;
        gbc.fill = GridBagConstraints.NONE;
        gbc.gridy++;

        JLabel filterLabel = new JLabel("Filter");
        filterLabel.setHorizontalAlignment(SwingConstants.CENTER);
        filterPanel.add(filterLabel, gbc);

        gbc.anchor = GridBagConstraints.WEST;

        this.preAlphasBox = new JCheckBox("Pre-Alpha", true);
        JCheckBox experimentsBox = new JCheckBox("Experiments");

        gbc.gridy++;
        filterPanel.add(this.preAlphasBox, gbc);

        gbc.gridy++;
        gbc.anchor = GridBagConstraints.NORTHWEST;
        gbc.weighty = 1.0;
        filterPanel.add(experimentsBox, gbc);

        JPanel centerPanel = new JPanel(new BorderLayout());
        centerPanel.setBorder(new EmptyBorder(5, 10, 5, 10));
        centerPanel.add(scrollPane, BorderLayout.CENTER);
        centerPanel.add(filterPanel, BorderLayout.EAST);

        JPanel buttonsPanel = new JPanel(new BorderLayout());

        FlowLayout leftLayout = new FlowLayout(FlowLayout.LEFT);
        leftLayout.setHgap(0);
        leftLayout.setVgap(0);

        JPanel leftButtonsPanel = new JPanel(leftLayout);
        buttonsPanel.add(leftButtonsPanel, BorderLayout.WEST);

        JButton refreshManifest = new JButton("Refresh");
        refreshManifest.addActionListener(e -> {
            tableModel.reload(true);
            root.revalidate();
        });

        leftButtonsPanel.add(refreshManifest);

        FlowLayout rightLayout = new FlowLayout(FlowLayout.RIGHT);
        rightLayout.setHgap(10);
        rightLayout.setVgap(0);

        JPanel rightButtonsPanel = new JPanel(rightLayout);
        buttonsPanel.add(rightButtonsPanel, BorderLayout.EAST);

        this.addButton = new JButton("Add");
        this.getDialog().getRootPane().setDefaultButton(this.addButton);
        this.addButton.setEnabled(false);
        this.addButton.addActionListener(e -> {
            String instanceName = this.nameField.getText();
            if (instanceName.trim().isEmpty()) {
                MessageBox.showErrorMessage(
                        AddInstanceDialog.this.getDialog(),
                        "Instance name cannot be empty"
                );

                return;
            }

            if (versionsTable.getSelectedRow() == -1) {
                MessageBox.showErrorMessage(
                        AddInstanceDialog.this.getDialog(),
                        "Cosmic version is not selected"
                );

                return;
            }

            IconManager iconManager = CRLauncher.getInstance().getIconManager();

            String chosenGroupName = this.groupField.getText();
            playView.addInstanceItem(new InstanceItem(iconManager.getIcon("cosmic_logo_x32.png").icon(), instanceName), chosenGroupName);
            this.getDialog().dispose();
            TableModel model = versionsTable.getModel();
            int selectedRow = versionsTable.getSelectedRow();
            selectedRow = versionsTable.convertRowIndexToModel(selectedRow);
            String mcVersion = String.valueOf(model.getValueAt(selectedRow, 0));
            CRLauncher.getInstance().doTask(() -> {
                InstanceManager instanceManager = CRLauncher.getInstance().getInstanceManager();

                try {
                    instanceManager.createInstance(instanceName, chosenGroupName, mcVersion,
                            CRLauncher.getInstance().getSettings().settingsDialogUpdateToLatest);
                } catch (InstanceAlreadyExistsException ex) {
                    MessageBox.showErrorMessage(
                            AddInstanceDialog.this.getDialog(),
                            ex.getMessage()
                    );

                    Log.warn(ex.getMessage());
                } catch (IOException ex) {
                    Log.error("Unable to create new instance", ex);
                }
            });
        });
        rightButtonsPanel.add(this.addButton);
        JButton cancelButton = new JButton("Cancel");
        cancelButton.addActionListener(e -> {
            this.getDialog().dispose();
        });
        rightButtonsPanel.add(cancelButton);
        buttonsPanel.setBorder(new EmptyBorder(6, 10, 10, 0));
        root.add(buttonsPanel, BorderLayout.SOUTH);

        root.add(centerPanel, BorderLayout.CENTER);
        root.setPreferredSize(new Dimension(900, 480));

        this.setResizable(false);
        this.setContent(root);
        this.center(0);
        this.setVisible(true);
    }

    public JButton getAddButton() {
        return this.addButton;
    }

    public JCheckBox getPreAlphasBox() {
        return this.preAlphasBox;
    }
}
