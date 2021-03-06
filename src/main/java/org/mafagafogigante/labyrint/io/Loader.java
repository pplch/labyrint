package org.mafagafogigante.labyrint.io;

import org.mafagafogigante.labyrint.game.DungeonString;
import org.mafagafogigante.labyrint.game.Game;
import org.mafagafogigante.labyrint.game.GameState;
import org.mafagafogigante.labyrint.logging.DungeonLogger;
import org.mafagafogigante.labyrint.util.Messenger;
import org.mafagafogigante.labyrint.util.StopWatch;

import org.jetbrains.annotations.NotNull;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;
import java.util.*;

import javax.swing.*;

/**
 * Loader class that handles saving and loading the game.
 */
public final class Loader {

  private static final File SAVES_FOLDER = new File("saves/");
  private static final String SAVE_EXTENSION = ".labyrint";
  private static final String VERSION_EXTENSION = ".version";
  private static final String DEFAULT_SAVE_NAME = "default" + SAVE_EXTENSION;
  private static final String SAVE_CONFIRM = "Chcete hru uložit?";
  private static final String LOAD_CONFIRM = "Chcete načíst hru?";

  private Loader() { // Ensure that this class cannot be instantiated.
    throw new AssertionError();
  }

  /**
   * Evaluates if a File is a save file by checking that it is a file and ends with the save extension.
   *
   * @param file a File object
   * @return true if the specified File is not {@code null} and has the properties of a save file
   */
  private static boolean isSaveFile(File file) {
    return file != null && file.getName().endsWith(SAVE_EXTENSION) && file.isFile();
  }

  /**
   * Checks if any file in the saves folder ends with the save extension.
   */
  public static boolean checkForSave() {
    return !getSavedFiles().isEmpty();
  }

  /**
   * Prompts the user to confirm an operation using a dialog window.
   */
  private static boolean confirmOperation(String confirmation) {
    UIManager.put("OptionPane.yesButtonText", "Ano");
    UIManager.put("OptionPane.noButtonText", "Ne");
    int result = JOptionPane.showConfirmDialog(Game.getGameWindow(), confirmation, null, JOptionPane.YES_NO_OPTION);
    Game.getGameWindow().requestFocusOnTextField();
    return result == JOptionPane.YES_OPTION;
  }

  /**
   * Appends the save file extension to a file name it if it does not ends with it already.
   *
   * @param name a file name
   * @return a String ending with the file extension
   */
  private static String ensureFileEndsWithExtension(String name, String extension) {
    return name.endsWith(extension) ? name : name + extension;
  }

  /**
   * Generates a new GameState and returns it.
   */
  public static GameState newGame() {
    GameState gameState = new GameState();
    DungeonString string = new DungeonString();
    string.append("Vytvořena nová hra.\n\n");
    string.append(gameState.getPreface());
    string.append("\n");
    Writer.write(string);
    Game.getGameWindow().requestFocusOnTextField();
    return gameState;
  }

  /**
   * Loads the newest save file if there is a save file. Otherwise, returns {@code null}.
   *
   * <p>Note that if the user does not confirm the operation in the dialog that pops up, this method return {@code
   * null}.
   *
   * @param requireConfirmation whether or not this method should require confirmation from the user
   * @return a GameState or null
   */
  public static GameState loadGame(boolean requireConfirmation) {
    if (checkForSave()) {
      if (!requireConfirmation || confirmOperation(LOAD_CONFIRM)) {
        return loadFile(getMostRecentlySavedFile());
      }
    }
    return null;
  }

  /**
   * Attempts to load the save file indicated by the first argument of the "load" command.
   *
   * <p>If the filename was provided but didn't match any files, a message about this is written.
   *
   * <p>If the load command was issued without arguments, this method delegates save loading to {@code
   * Loader.loadGame(false)}.
   *
   * <p>If no save file could be found, a message is written.
   *
   * <p>This method guarantees that the if null is returned, something is written to the screen.
   */
  public static GameState parseLoadCommand(String[] arguments) {
    if (arguments.length != 0) {
      // A save name was provided.
      String argument = arguments[0];
      argument = ensureFileEndsWithExtension(argument, SAVE_EXTENSION);
      File save = createSaveFileFromName(argument);
      if (isSaveFile(save)) {
        return loadFile(save);
      } else {
        Writer.write(save.getName() + " neexistuje nebo není soubor.");
        return null;
      }
    } else {
      GameState loadResult = loadGame(false); // Don't ask for confirmation. Typing load is not an easy mistake.
      if (loadResult == null) {
        Writer.write("Nebyla nalezena žádná uložená hra.");
      }
      return loadResult;
    }
  }

  /**
   * Saves the specified GameState to the default save file.
   */
  public static void saveGame(GameState gameState) {
    saveGame(gameState, null);
  }

  /**
   * Saves the specified GameState, using the default save file or the one defined in the IssuedCommand.
   *
   * <p>Only asks for confirmation if there already is a save file with the name.
   */
  public static void saveGame(GameState gameState, String[] arguments) {
    String saveName = DEFAULT_SAVE_NAME;
    if (arguments != null && arguments.length != 0) {
      saveName = arguments[0];
    }
    if (saveFileDoesNotExist(saveName) || confirmOperation(SAVE_CONFIRM)) {
      saveFile(gameState, saveName);
    }
  }

  /**
   * Tests whether the save file corresponding to the provided name does not exist.
   *
   * @param name the provided filename
   * @return true if the corresponding file does not exist
   */
  private static boolean saveFileDoesNotExist(String name) {
    return !createSaveFileFromName(name).exists();
  }

  /**
   * Returns a File object for the corresponding save file for a specified name.
   *
   * @param name the provided filename
   * @return a File object
   */
  private static File createSaveFileFromName(String name) {
    return new File(SAVES_FOLDER, ensureFileEndsWithExtension(name, SAVE_EXTENSION));
  }

  /**
   * Returns a File object for the corresponding version file for a specified save file.
   *
   * @param saveFile the save file
   * @return a File object
   */
  private static File createVersionFileFromSaveFile(File saveFile) {
    String path = saveFile.getPath();
    if (!path.endsWith(SAVE_EXTENSION)) {
      throw new IllegalArgumentException("soubor pro uložení nekončí výchozí příponou");
    }
    String versionFilePath = path.substring(0, path.length() - SAVE_EXTENSION.length()) + VERSION_EXTENSION;
    return new File(versionFilePath);
  }

  /**
   * Attempts to load a GameState from a file.
   *
   * @param file a File object
   * @return a GameState or {@code null} if something goes wrong.
   */
  private static GameState loadFile(File file) {
    StopWatch stopWatch = new StopWatch();
    try (BufferedInputStream in = new BufferedInputStream(new FileInputStream(file));
         ObjectInputStream objectInputStream = new ObjectInputStream(in)) {
      GameState loadedGameState = (GameState) objectInputStream.readObject();
      loadedGameState.setSaved(true); // It is saved, we just loaded it (needed as it now defaults to false).
      // Update the GameState version if required.
      if (loadedGameState.getGameVersion().compareTo(Version.getCurrentVersion()) < 0) {
        loadedGameState.setGameVersion(Version.getCurrentVersion());
      }
      String sizeString = Converter.bytesToHuman(file.length());
      DungeonLogger.info(String.format("Načteno %s za %s.", sizeString, stopWatch.toString()));
      Writer.write(String.format("Hra byla úspěšně načtena (načteno %s z %s).", sizeString, file.getName()));
      return loadedGameState;
    } catch (FileNotFoundException bad) { // The filed was moved or deleted.
      Writer.write("Zadanou uloženou hru nelze najít.");
      return null;
    } catch (ClassNotFoundException | IOException exception) {
      Writer.write("Uloženou hru nelze načíst.");
      DungeonLogger.logSevere(exception);
      return null;
    }
  }

  /**
   * Serializes the specified {@code GameState} state to a file.
   *
   * @param state a GameState
   * @param name the name of the file
   */
  private static void saveFile(GameState state, String name) {
    StopWatch stopWatch = new StopWatch();
    File saveFile = createSaveFileFromName(name);
    File versionFile = createVersionFileFromSaveFile(saveFile);
    ensureSavesFolderExists();
    try (BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream(saveFile));
         ObjectOutputStream objectOutputStream = new ObjectOutputStream(out)) {
      objectOutputStream.writeObject(state);
      state.setSaved(true);
      String sizeString = Converter.bytesToHuman(saveFile.length());
      Charset charset = DungeonCharset.DEFAULT_CHARSET;
      try (OutputStreamWriter writer = new OutputStreamWriter(new FileOutputStream(versionFile), charset)) {
        writer.write(state.getGameVersion().toString());
        writer.append(System.lineSeparator());
      }
      DungeonLogger.info(String.format("Uloženo %s za %s.", sizeString, stopWatch.toString()));
      Writer.write(String.format("Hra byla úspěšně uložena (uloženo %s do %s).", sizeString, saveFile.getName()));
    } catch (IOException exception) {
      Writer.write("Tuto hru nelze uložit.");
      DungeonLogger.logSevere(exception);
    }
  }

  private static void ensureSavesFolderExists() {
    if (!SAVES_FOLDER.exists()) {
      if (!SAVES_FOLDER.mkdir()) {
        Messenger.printFailedToCreateDirectoryMessage(SAVES_FOLDER.getName());
      }
    }
  }

  /**
   * Returns a list of abstract pathnames denoting the files in the saves folder that end with a valid extension sorted
   * in natural order.
   */
  @NotNull
  static List<File> getSavedFiles() {
    File[] fileArray = SAVES_FOLDER.listFiles(DungeonFilenameFilters.getExtensionFilter());
    if (fileArray == null) {
      fileArray = new File[0];
    }
    List<File> fileList = new ArrayList<>(Arrays.asList(fileArray));
    for (Iterator<File> fileIterator = fileList.iterator(); fileIterator.hasNext(); ) {
      File file = fileIterator.next();
      if (!file.isFile()) {
        fileIterator.remove();
      }
    }
    Collections.sort(fileList, new FileLastModifiedComparator());
    return fileList;
  }

  @NotNull
  static List<Version> getSavedFilesVersions(List<File> savedFiles) {
    List<Version> versionList = new ArrayList<>();
    char[] versionBuffer = new char[32];
    for (File saveFile : savedFiles) {
      File versionFile = createVersionFileFromSaveFile(saveFile);
      Charset charset = DungeonCharset.DEFAULT_CHARSET;
      try (InputStreamReader reader = new InputStreamReader(new FileInputStream(versionFile), charset)) {
        int end = reader.read(versionBuffer);
        while (end > 1 && Character.isWhitespace(versionBuffer[end - 1])) {
          end--;
        }
        versionList.add(new Version(new String(versionBuffer, 0, end)));
      } catch (FileNotFoundException exception) {
        versionList.add(null);
      } catch (IOException fatal) {
        DungeonLogger.logSevere(fatal);
      }
    }
    return versionList;
  }

  /**
   * Returns the most recently saved file. As a precondition, there must be at least one save file.
   */
  private static File getMostRecentlySavedFile() {
    List<File> fileList = getSavedFiles();
    if (fileList.isEmpty()) {
      throw new IllegalStateException("called getMostRecentlySavedFile() but there are no save files.");
    }
    return fileList.get(0);
  }

}
