/*
 * CRLauncher - https://github.com/CRLauncher/CRLauncher
 * Copyright (C) 2024-2025 CRLauncher
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

package me.theentropyshard.crlauncher.gui.dialogs.instancesettings.tab.screenshots;

import me.theentropyshard.crlauncher.CRLauncher;
import me.theentropyshard.crlauncher.gui.dialogs.instancesettings.tab.Tab;
import me.theentropyshard.crlauncher.instance.CosmicInstance;

import javax.swing.*;
import java.awt.*;

public class ScreenshotsTab extends Tab {
    private final ScreenshotsPanel screenshotsPanel;

    public ScreenshotsTab(CosmicInstance instance, JDialog dialog) {
        super(CRLauncher.getInstance().getLanguage().getString("gui.instanceSettingsDialog.screenshotsTab.name"), instance, dialog);

        JPanel root = this.getRoot();
        root.setLayout(new BorderLayout());

        this.screenshotsPanel = new ScreenshotsPanel();
        root.add(this.screenshotsPanel, BorderLayout.CENTER);

        this.loadScreenshots();
    }

    public void loadScreenshots() {
        new ScreenshotsLoader(this.screenshotsPanel, this.getInstance().getCosmicDir().resolve("screenshots")).execute();
    }
}
