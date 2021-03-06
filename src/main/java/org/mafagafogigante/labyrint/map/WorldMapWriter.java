package org.mafagafogigante.labyrint.map;

import org.mafagafogigante.labyrint.game.DungeonString;
import org.mafagafogigante.labyrint.gui.GameWindow;
import org.mafagafogigante.labyrint.io.Writer;

import org.jetbrains.annotations.NotNull;

public final class WorldMapWriter {

  private WorldMapWriter() {
    throw new AssertionError();
  }

  public static void writeMap() {
    renderMap(WorldMap.makeWorldMap(getMapRows(), getMapColumns(), true));
  }

  public static void writeDebugMap() {
    renderMap(WorldMap.makeWorldMap(getMapRows(), getMapColumns(), false));
  }

  private static int getMapRows() {
    return GameWindow.getRows() - 1;
  }

  private static int getMapColumns() {
    return GameWindow.getColumns();
  }

  /**
   * Writes a WorldMap to the screen. This erases all the content currently on the screen.
   *
   * @param map a WorldMap, not null
   */
  private static void renderMap(@NotNull WorldMap map) {
    DungeonString string = new DungeonString();
    WorldMapSymbol[][] worldMapSymbolMatrix = map.getSymbolMatrix();
    for (int i = 1; i < worldMapSymbolMatrix.length - 1; i++) {
      for (WorldMapSymbol symbol : worldMapSymbolMatrix[i]) {
        // OK as setColor verifies if the color change is necessary (does not replace a color by itself).
        string.setColor(symbol.getColor());
        string.append(symbol.getCharacterAsString());
      }
      if (i < worldMapSymbolMatrix.length - 1) {
        string.append("\n");
      }
    }
    WorldMapLegend.renderLegend(worldMapSymbolMatrix, string);
    Writer.write(string);
  }

}
