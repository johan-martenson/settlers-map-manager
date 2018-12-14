package org.appland.settlers.maps;

import org.jline.terminal.TerminalBuilder;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;

import java.io.IOException;

public class Inspector {

    @Option(name="--debug", usage="Print debug information")
    boolean debug = false;

    @Option(name="--file", usage="Map file to load")
    static String mapFile;

    @Option(name="--mode", usage="Set swd or wld file format explicitly")
    String mode = "swd";

    @Option(name="--override-width", usage="Override the detected width of the console")
    int widthOverride = -1;

    @Option(name="--override-height", usage="Override the detected height of the console")
    int heightOverride = -1;

    public static void main(String[] args) {
        Inspector inspector = new Inspector();
        CmdLineParser parser = new CmdLineParser(inspector);

        try {
            parser.parseArgument(args);
        } catch (CmdLineException e) {
            e.printStackTrace();
        }

        try {
            inspector.compare(mapFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void compare(String mapFilename) throws IOException {

        MapLoader mapLoader = new MapLoader();

        mapLoader.debug = debug;
        mapLoader.mode = mode;

        MapFile mapFile = mapLoader.loadMapFromFile(mapFilename);

        int consoleWidth = TerminalBuilder.terminal().getWidth();
        int consoleHeight = TerminalBuilder.terminal().getHeight();

        if (debug) {
            System.out.println("Detected console dimensions: " + consoleWidth + "x" + consoleHeight);
        }

        if (widthOverride != -1) {
            consoleWidth = widthOverride;
        }

        if (heightOverride != -1) {
            consoleHeight = heightOverride;
        }

        if (debug) {
            System.out.println("Using dimensions: " + consoleWidth + "x" + consoleHeight);
        }

        String[][] mapFileRender = renderMapFile(mapFile, consoleWidth, consoleHeight);

        /* Print the render of the map file */
        StringBuffer sb = new StringBuffer();
        for (String[] row : mapFileRender) {
            for (String character : row) {

                if (character == null) {
                    sb.append(" ");
                } else {
                    sb.append(character);
                }
            }

            sb.append("\n");
        }

        System.out.println(sb.toString());
    }

    private String[][] renderMapFile(MapFile mapFile, int consoleWidth, int consoleHeight) {
        int maxWidth = Math.min(consoleWidth, mapFile.getWidth() * 2 + 2);
        int maxHeight = Math.min(consoleHeight, mapFile.getHeight() + 1);

        if (debug) {
            System.out.println("Width: " + maxWidth + ", height: " + maxHeight);
        }

        String[][] bfr = new String[maxHeight][maxWidth];

        int row = 1;
        int index = 1;

        for (SpotData spot : mapFile.getSpots()) {

            if (row < maxHeight && index < maxWidth) {

                /* Set triangles above, instead of below because the iteration starts
                   from the bottom and runs upwards

                   This will look good but the maps are rendered upside down
                */

                /* Draw water */
                if (Texture.isWater(spot.getTextureBelow()) && row > 0) {
                    bfr[row - 1][index] = " ";
                } else if (bfr[row - 1][index] == null) {
                    bfr[row - 1][index] = ".";
                }

                if (row > 0 && index < maxWidth - 2 && Texture.isWater(spot.getTextureDownRight())) {
                    bfr[row - 1][index + 1] = " ";
                } else if (row > 0 && index < maxWidth - 2 && bfr[row - 1][index + 1] == null) {
                    bfr[row - 1][index + 1] = ".";
                }

                /* Place stones */
                if (spot.hasStone()) {
                    bfr[row][index] = "O";
                }

                /* Place trees */
                if (spot.hasTree()) {
                    bfr[row][index] = "T";
                }
            }

            if (index == mapFile.getWidth() * 2 - 2) {
                index = 2;
                row++;
            } else if (index >= mapFile.getWidth() * 2 - 1) {
                index = 1;
                row++;
            } else {
                index += 2;
            }
        }

        return bfr;
    }
}
