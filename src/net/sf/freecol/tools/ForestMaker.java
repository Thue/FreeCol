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

package net.sf.freecol.tools;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.geom.Rectangle2D;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.awt.Point;
import java.awt.Polygon;
import java.awt.Rectangle;
import java.awt.TexturePaint;
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import javax.imageio.ImageIO;


/**
 * Generate forest tiles.
 */
public class ForestMaker {

    private static String DESTDIR = "data/rules/classic/resources/images/forest";

    private static int BASE_WIDTH = 128;
    private static int BASE_HEIGHT = 64;
    private static int MARGIN = 20;
    private static int TREES = 60;
    private static int RIVER_MARGIN = 4;

    private static int HALF_WIDTH = BASE_WIDTH / 2;
    private static int HALF_HEIGHT = BASE_HEIGHT / 2;

    private static int[] LIMIT = new int[] {
        HALF_WIDTH, HALF_WIDTH, -HALF_WIDTH, -HALF_WIDTH
    };

    private static double[] SLOPE = new double[] {
        -0.5, 0.5, -0.5, 0.5
    };

    private static final int[] POWERS_OF_TWO
        = new int[] { 1, 2, 4, 8 };


    private static boolean drawBorders = false;
    private static boolean drawRivers = false;
    private static boolean drawTrees = true;


    private static class ImageLocation implements Comparable<ImageLocation> {
        BufferedImage image;
        int x, y;

        public ImageLocation(BufferedImage image, int x, int y) {
            this.image = image;
            this.x = x;
            this.y = y;
        }

        public int compareTo(ImageLocation other) {
            int dy = other.y - this.y;
            if (dy == 0) {
                return other.x - this.x;
            } else {
                return dy;
            }
        }

    }


    /**
     * Pass the source directory as first argument.
     */
    public static void main(String[] args) throws Exception {

        if (args.length == 0) {
            System.out.println("Usage: ForestMaker <directory>...");
            System.out.println("Directory name should match a directory in");
            System.out.println("   " + DESTDIR);
            System.exit(1);
        }

        String riverName = "data/rules/classic/resources/images/terrain/"
            + "ocean/center0.png";
        BufferedImage river = ImageIO.read(new File(riverName));
        // grab a rectangle completely filled with water
        river = river.getSubimage(44, 22, 40, 20);
        Rectangle2D rectangle = new Rectangle(0, 0, river.getWidth(), river.getHeight());
        TexturePaint texture = new TexturePaint(river, rectangle);


        for (String arg : args) {
            File sourceDirectory = new File(arg);
            if (!sourceDirectory.exists()) {
                System.out.println("Source directory " + arg + " does not exist.");
                continue;
            }
            String baseName = sourceDirectory.getName();
            File destinationDirectory = new File(DESTDIR, baseName);
            if (!destinationDirectory.exists()) {
                System.out.println("Destination directory " + destinationDirectory.getPath()
                                   + " does not exist.");
                continue;
            }
            File[] imageFiles = sourceDirectory.listFiles();
            if (imageFiles == null) {
                System.out.println("No images found in source directory " + arg + ".");
                continue;
            } else {
                System.out.println(imageFiles.length + " images found in source directory "
                                   + arg + ".");
            }
            List<BufferedImage> images = new ArrayList<BufferedImage>(imageFiles.length);
            int minimumHeight = Integer.MAX_VALUE;
            for (File imageFile : imageFiles) {
                if (imageFile.isFile() && imageFile.canRead()) {
                    try {
                        BufferedImage image = ImageIO.read(imageFile);
                        images.add(image);
                        if (image.getHeight() < minimumHeight) {
                            minimumHeight = image.getHeight();
                        }
                    } catch(Exception e) {
                        System.out.println("Unable to load image " + imageFile.getName() + ":\n");
                        e.printStackTrace();
                    }
                }
            }
            int numberOfImages = images.size();
            Random random = new Random(1492);

            // the tile itself
            int[] x_coords = new int[] {
                0, HALF_WIDTH, 0, -HALF_WIDTH
            };
            int[] y_coords = new int[] {
                -HALF_HEIGHT, 0, HALF_HEIGHT, 0
            };

            Polygon diamond = new Polygon(x_coords, y_coords, 4);
            Polygon[] polygons = getBranches(RIVER_MARGIN);

            for (int index = 0; index < 16; index++) {
                BufferedImage base = new BufferedImage(BASE_WIDTH, BASE_HEIGHT + MARGIN,
                                                       BufferedImage.TYPE_INT_ARGB);
                Graphics2D g = base.createGraphics();
                // for the sake of convenience: translate graphics in
                // order to use the centre of the tile as origin
                g.translate(HALF_WIDTH, HALF_HEIGHT + MARGIN);

                if (drawBorders) {
                    g.setColor(Color.RED);
                    g.draw(diamond);
                    for (Polygon p : polygons) {
                        g.draw(p);
                    }
                }

                g.setPaint(texture);
                g.setStroke(new BasicStroke(6));
                String counter = "";
                boolean[] branches = new boolean[4];
                if (index > 0) {
                    for (int i = 0; i < POWERS_OF_TWO.length; i++) {
                        if ((index & POWERS_OF_TWO[i]) == POWERS_OF_TWO[i]) {
                            branches[i] = true;
                            counter += "1";
                            if (drawRivers) {
                                g.drawLine(0, 0, LIMIT[i], getY(0, 0, SLOPE[i], LIMIT[i]));
                            }
                        } else {
                            counter += "0";
                        }
                    }
                }

                if (drawTrees) {
                    List<ImageLocation> trees = new ArrayList<ImageLocation>(TREES);
                    int count = 0;
                    // reduce number of trees if river branches are present
                    int numberOfTrees = (8 - Integer.bitCount(index)) * TREES / 8;

                    int x, y;
                    trees: while (count < numberOfTrees) {
                        images: for (int im = 0; im < numberOfImages; im++) {
                            BufferedImage image = images.get((count + im) % numberOfImages);
                            int width = image.getWidth();
                            int height = image.getHeight();
                            x = random.nextInt(BASE_WIDTH) - HALF_WIDTH;
                            if (count < 10) {
                                // choose a particularly favourable
                                // point near the southern edge of the
                                // diamond
                                y = HALF_HEIGHT - Math.abs(x)/2;
                                if (x > 0) {
                                    x -= width;
                                }
                            } else {
                                y = getRandomY(random, x);
                            }
                            // crown of the tree
                            int crown = y - height;
                            // assume trees are more or less symmetrical
                            if (diamond.contains(x + width/2, y)
                                && x + width < HALF_WIDTH
                                && y - height > -(HALF_HEIGHT + MARGIN)) {
                                for (int i = 0; i < branches.length; i++) {
                                    if (branches[i]
                                        && polygons[i].intersects(x, crown, width, height)) {
                                        // would obscure river image
                                        continue images;
                                    }
                                }
                                trees.add(new ImageLocation(image, x, y - height));
                                count++;
                            }
                        }
                    }

                    // sort by y, x coordinate
                    Collections.sort(trees);
                    for (ImageLocation imageLocation : trees) {
                        g.drawImage(imageLocation.image, imageLocation.x, imageLocation.y, null);
                    }

                }

                ImageIO.write(base, "png", new File(destinationDirectory,
                                                    sourceDirectory.getName() + counter + ".png"));

            }
        }
    }

    private static int getY(int x, int y, double slope, int newX) {
        return (int) (y + slope * (newX - x));
    }

    private static int getRandomY(Random random, int x) {
        int height = HALF_HEIGHT - Math.abs(x) / 2;
        return (height == 0) ? 0 : random.nextInt(2 * height) - height;
    }

    /**
     * Returns an array of polygons describing the river branches of
     * the tile.
     *
     * @param height an <code>int</code> value
     * @return a <code>Polygon[]</code> value
     */
    private static Polygon[] getBranches(int height) {
        int width = 2 * height;
        Polygon[] result = new Polygon[4];
        int[] xx = new int[] { 0, -width, 0, width };
        int[] yy = new int[] { height, 0, -height, 0 };
        for (int i = 0; i < 4; i++) {
            int[] x = new int[4];
            int[] y = new int[4];
            x[0] = xx[i];
            y[0] = yy[i];
            x[1] = xx[(i + 1) % 4];
            y[1] = yy[(i + 1) % 4];
            x[2] = LIMIT[i];
            y[2] = getY(x[1], y[1], SLOPE[i], LIMIT[i]);
            x[3] = LIMIT[i];
            y[3] = getY(x[0], y[0], SLOPE[i], LIMIT[i]);
            result[i] = new Polygon(x, y, 4);
        }
        return result;
    }
}

