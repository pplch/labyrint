package org.mafagafogigante.labyrint.commands;

import org.mafagafogigante.labyrint.game.DungeonString;
import org.mafagafogigante.labyrint.io.Writer;

import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;

/**
 * A set of Commands.
 */
public final class CommandSet {

  private static final int COMMAND_NAME_COLUMN_WIDTH = 20;

  private final Map<String, Command> commands = new TreeMap<>();

  private CommandSet() {
  }

  /**
   * Constructs an empty CommandSet containing only the "commands" Command.
   */
  static CommandSet emptyCommandSet() {
    final CommandSet commandSet = new CommandSet();
    commandSet.addCommand(new Command("příkazy", "Vypíše všechny známé příkazy.") {
      @Override
      public void execute(@NotNull String[] arguments) {
        String filter = arguments.length == 0 ? null : arguments[0];
        List<CommandDescription> descriptions = commandSet.getCommandDescriptions();
        DungeonString dungeonString = new DungeonString();
        int count = 0;
        for (CommandDescription description : descriptions) {
          if (filter == null || StringUtils.startsWithIgnoreCase(description.getName(), filter)) {
            count++;
            dungeonString.append(StringUtils.rightPad(description.getName(), COMMAND_NAME_COLUMN_WIDTH));
            dungeonString.append(description.getInfo());
            dungeonString.append("\n");
          }
        }
        if (count == 0 && filter != null) {
          Writer.write("Žádný příkaz nezačíná '" + filter + "'.");
        } else {
          if (count > 1) {
            dungeonString.append("\nSeznam ");
            dungeonString.append(String.valueOf(count));
            dungeonString.append(" příkazů.");
            if (filter == null) {
              dungeonString.setColor(Color.ORANGE);
              dungeonString.append("\nTento seznam můžete filtrovat zadáním počátečních písmen příkazu, např. „příkazy kouz“ ");
            }
          }
          Writer.write(dungeonString);
        }
      }
    });
    return commandSet;
  }

  /**
   * Retrieves a Command corresponding to the specified token or null if no command matches the token.
   */
  Command getCommand(String token) {
    return commands.get(token.toLowerCase(Locale.ENGLISH));
  }

  /**
   * Adds a Command to this CommandSet.
   *
   * @param command a Command object, not null
   */
  void addCommand(@NotNull Command command) {
    String name = command.getDescription().getName().toLowerCase(Locale.ENGLISH);
    if (commands.containsKey(name)) {
      throw new IllegalArgumentException("Pokusil se přidat '" + name + "' vícekrát.");
    }
    commands.put(name, command);
  }

  /**
   * Returns an unmodifiable view of the List of CommandDescriptions.
   *
   * @return an unmodifiable List of CommandDescriptions
   */
  private List<CommandDescription> getCommandDescriptions() {
    List<CommandDescription> descriptions = new ArrayList<>(commands.size());
    for (Command command : commands.values()) {
      descriptions.add(command.getDescription());
    }
    return descriptions;
  }

  @Override
  public String toString() {
    return String.format("CommandSet velikosti %d.", commands.size());
  }

  /**
   * Retrieves a list of the names of the commands closest to the provided token according to their Levenshtein
   * distance.
   */
  List<String> getClosestCommands(final String token) {
    List<String> closestCommands = new ArrayList<>();
    int best = Integer.MAX_VALUE;
    for (Command command : commands.values()) {
      String commandName = command.getDescription().getName();
      int distance = StringDistanceMetrics.levenshteinDistance(token, commandName);
      if (distance < best) {
        closestCommands.clear();
        best = distance;
      }
      if (distance == best) {
        closestCommands.add(commandName);
      }
    }
    return closestCommands;
  }

}
