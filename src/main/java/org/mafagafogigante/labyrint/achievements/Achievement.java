package org.mafagafogigante.labyrint.achievements;

import org.mafagafogigante.labyrint.game.Id;
import org.mafagafogigante.labyrint.game.PartOfDay;
import org.mafagafogigante.labyrint.stats.BattleStatistics;
import org.mafagafogigante.labyrint.stats.ExplorationStatistics;
import org.mafagafogigante.labyrint.stats.Statistics;
import org.mafagafogigante.labyrint.util.CounterMap;

import java.util.Collection;
import java.util.Set;

/**
 * Achievement class.
 */
public class Achievement {

  private final Id id;
  private final String name;
  private final String info;
  private final String text;

  private final BattleComponent battle;
  private final ExplorationComponent exploration;

  /**
   * Constructs an Achievement with the specified ID, name and info.
   *
   * @param info the String displayed when the "Achievements" command is used
   * @param text the String used to explain why the character unlocked the achievement
   */
  Achievement(String id, String name, String info, String text,
              Collection<BattleStatisticsRequirement> battleRequirements, CounterMap<Id> killsByLocationId,
              CounterMap<Id> visitedLocations, CounterMap<Id> maximumNumberOfVisits,
              Set<PartOfDay> partsOfDayOfDiscovery, int discoveryCount) {
    this.id = new Id(id);
    this.name = name;
    this.info = info;
    this.text = text;
    battle = new BattleComponent(battleRequirements);
    exploration = new ExplorationComponent(killsByLocationId, visitedLocations, maximumNumberOfVisits,
            partsOfDayOfDiscovery, discoveryCount);
  }

  public Id getId() {
    return id;
  }

  public String getName() {
    return name;
  }

  public String getInfo() {
    return info;
  }

  public String getText() {
    return text;
  }

  /**
   * Evaluates if the statistics fulfill this Achievement's conditions.
   *
   * @return true if the Achievement is fulfilled, false otherwise.
   */
  boolean isFulfilled(Statistics statistics) {
    BattleStatistics battleStatistics = statistics.getBattleStatistics();
    ExplorationStatistics explorationStatistics = statistics.getExplorationStatistics();
    return battle.isFulfilled(battleStatistics) && exploration.isFulfilled(explorationStatistics);
  }

  @Override
  public String toString() {
    return String.format("Úspěch{id=%s, name='%s', info='%s', text='%s'}", id, name, info, text);
  }

}
