/**
 *  Copyright (C) 2002-2013   The FreeCol Team
 *
 *  This file is part of FreeCol.
 *
 *  FreeCol is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 2 of the License, or
 *  (at your option) any later version.
 *
 *  FreeCol is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with FreeCol.  If not, see <http://www.gnu.org/licenses/>.
 */

package net.sf.freecol.client.gui.panel;

import java.awt.Dimension;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileFilter;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import net.sf.freecol.client.FreeColClient;
import net.sf.freecol.client.gui.GUI;
import net.sf.freecol.client.gui.i18n.Messages;
import net.sf.freecol.client.gui.option.BooleanOptionUI;
import net.sf.freecol.client.gui.option.FileOptionUI;
import net.sf.freecol.client.gui.option.OptionGroupUI;
import net.sf.freecol.common.io.FreeColDirectories;
import net.sf.freecol.common.io.FreeColSavegameFile;
import net.sf.freecol.common.option.FileOption;
import net.sf.freecol.common.option.MapGeneratorOptions;
import net.sf.freecol.common.option.OptionGroup;


/**
 * Dialog for changing the map generator options.
 *
 * @see MapGeneratorOptions
 * @see OptionGroup
 */
public final class MapGeneratorOptionsDialog extends OptionsDialog {

    private static final Logger logger = Logger.getLogger(MapGeneratorOptionsDialog.class.getName());

    private static final FileFilter fsgFilter = new FileFilter() {
            public boolean accept(File file) {
                return file.isFile() && file.getName().endsWith(".fsg");
            }
        };

    private static final String OPTION_GROUP_ID
        = MapGeneratorOptions.getXMLElementTagName();


    /**
     * Creates a dialog to set the map generator options.
     *
     * @param freeColClient The <code>FreeColClient</code> for the game.
     * @param mgo the map generator options
     * @param editable whether the options may be edited
     * @param loadCustomOptions whether to load custom options
     */
    public MapGeneratorOptionsDialog(FreeColClient freeColClient,
                                     OptionGroup mgo, boolean editable,
                                     boolean loadCustomOptions) {
        super(freeColClient, editable, mgo, mgo.getName(),
            "map_generator_options.xml", OPTION_GROUP_ID);
        
        if (isEditable() && loadCustomOptions) loadCustomOptions();

        if (isEditable()) {
            // TODO: The update should be solved by PropertyEvent.
            File mapDirectory = FreeColDirectories.getMapsDirectory();
            if (mapDirectory.isDirectory()) {
                JPanel mapPanel = new JPanel();
                for (final File file : mapDirectory.listFiles(fsgFilter)) {
                    JButton mapButton = makeMapButton(file);
                    mapButton.addActionListener(new ActionListener() {
                            public void actionPerformed(ActionEvent e) {
                                setFile(file);
                            }
                        });
                    mapPanel.add(mapButton);
                }

                JScrollPane scrollPane = new JScrollPane(mapPanel,
                    JScrollPane.VERTICAL_SCROLLBAR_NEVER,
                    JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
                scrollPane.getVerticalScrollBar().setUnitIncrement(16);
                scrollPane.getViewport().setOpaque(false);
                // TODO: find out how to do this properly
                scrollPane.setMinimumSize(new Dimension(400, 110));

                panel.add(scrollPane);
            }
        }
        initialize();
    }


    // Internals

    private JButton makeMapButton(File file) {
        String mapName = file.getName().substring(0, file.getName()
                                                         .lastIndexOf('.'));
        JButton mapButton = new JButton(Messages.message("freecol.map."
                + mapName));
        try {
            FreeColSavegameFile savegame = new FreeColSavegameFile(file);
            Image thumbnail = ImageIO.read(savegame
                .getInputStream(FreeColSavegameFile.THUMBNAIL_FILE));
            mapButton.setIcon(new ImageIcon(thumbnail));
            try {
                Properties properties = new Properties();
                properties.load(savegame
                    .getInputStream(FreeColSavegameFile.SAVEGAME_PROPERTIES));
                mapButton.setToolTipText(properties.getProperty("map.width")
                    + "\u00D7"
                    + properties.getProperty("map.height"));
            } catch (Exception e) {
                logger.log(Level.WARNING, "Unable to load savegame.", e);
            }
            mapButton.setHorizontalTextPosition(JButton.CENTER);
            mapButton.setVerticalTextPosition(JButton.BOTTOM);
        } catch (Exception e) {
            logger.log(Level.WARNING, "Failed to read thumbnail.", e);
        }
        return mapButton;
    }

    private void setFile(File file) {
        OptionGroup group = getGroup();
        ((FileOption)group.getOption(MapGeneratorOptions.IMPORT_FILE))
            .setValue(file);

        group.setBoolean(MapGeneratorOptions.IMPORT_RUMOURS, false);
        group.setBoolean(MapGeneratorOptions.IMPORT_TERRAIN, true);
        group.setBoolean(MapGeneratorOptions.IMPORT_BONUSES, false);
        getOptionUI().reset();
    }


    // Override OptionsDialog

    /**
     * {@inheritDoc}
     */
    @Override
    public OptionGroup getResponse() {
        OptionGroup value = super.getResponse();
        if (value != null) {
            if (freeColClient.isMapEditor()) {
                value = null;
            } else {
                freeColClient.getPreGameController().sendMapGeneratorOptions();
            }
        }
        return value;
    }
}
