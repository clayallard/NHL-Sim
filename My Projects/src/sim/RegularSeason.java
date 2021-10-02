package sim;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;

public class RegularSeason {

	private ArrayList<Game> games;
	private int gamesPlayed;
	private League league;
	private int seasonYear;
	private boolean seasonOver;

	/**
	 * Creates a regular season with a schedule and automatically starts the season.
	 * 
	 * @param league
	 * @param schedule
	 * @param year
	 */
	public RegularSeason(League league, ArrayList<Game> schedule, int year) {
		games = schedule;
		seasonYear = year;
		this.league = league;
		league.startSeason(this);
		for (int i = 0; i < games.size(); i++) {
			games.get(i).setGameNumber(i + 1);
		}
		for (Team t : league.teams()) {
			for (int i = 0; i < 26; i++) {
				t.changeSkill();
			}
		}
		for (Team t : league.teams()) {
			t.preSkillLevel();
		}
	}

	/**
	 * true if the season has started, false otherwise.
	 */
	public boolean seasonStarted() {
		return gamesPlayed != 0;
	}

	/**
	 * the total amount of games for all teams in the league.
	 */
	public int totalAmountOfGames() {
		return games.size();
	}

	/**
	 * returns a specific game.
	 * 
	 * @param gameNumber - game number.
	 * @return the particular game chosen.
	 */
	public Game game(int gameNumber) {
		if (gameNumber > games.size() || gameNumber < 1) {
			return null;
		}
		return games.get(gameNumber);
	}

	/**
	 * every game for this regular season.
	 */
	public ArrayList<Game> games() {
		return games;
	}

	public String schedule() {
		String str = seasonYear + "-" + (seasonYear + 1) + " " + league.name() + " Schedule\n";
		int amountOfChar = str.length();
		for (int i = 0; i < amountOfChar - 1; i++) {
			str += "_";
		}
		str += "\n";
		for (int i = 0; i < games.size(); i++) {
			str += "\n" + games.get(i).toString();
		}
		return str;
	}

	/**
	 * current season.
	 */
	public int year() {
		return seasonYear;
	}

	/**
	 * Generates the scheduele for a specific team.
	 * 
	 * @param team - team to get schedule from.
	 * @return all games this team plays.
	 */
	public ArrayList<Game> gamesForTeam(Team team) {
		ArrayList<Game> teamGames = new ArrayList<>();
		for (Game g : games) {
			if (team == g.away() || team == g.home()) {
				teamGames.add(g);
			}
		}
		return teamGames;
	}

	/**
	 * simulates the entire regular season.
	 */
	public void simSeason() {
		simToThisGame(totalAmountOfGames());
	}

	/**
	 * similates a specified amount of games as given by the user. If that amount
	 * exceeds the amount for the rest of the season, it will just simulate the rest
	 * of the season.
	 * 
	 * @param amount - amount of games to simulate.
	 */
	public void simNextAmountOfGames(int amount) {
		simToThisGame(gamesPlayed + amount);
	}

	/**
	 * Simulates to specific game number of the season.
	 * 
	 * @param gameNumber - game number.
	 */
	public void simToThisGame(int gameNumber) {
		if (gameNumber > totalAmountOfGames()) {
			gameNumber = totalAmountOfGames();
		}
		for (int i = gamesPlayed; i < gameNumber; i++) {
			games.get(i).simGame();
			gamesPlayed++;
		}
		league.updateStandings();
		setSeasonOver();
	}

	/**
	 * simulates the next game on the schedule.
	 */
	public void simNextGame() {
		if (!seasonOver) {
			simToThisGame(gamesPlayed + 1);
		}
	}

	/**
	 * determines if the regular season is over.
	 * 
	 * @return
	 */
	private void setSeasonOver() {
		if (gamesPlayed == totalAmountOfGames()) {
			seasonOver = true;
			league.setMadePlayoffs();
			for (Team t : league.teams()) {
				if (!t.playoffs()) {
					t.setSeasonResult(-1);
					double amountOfChanges = league.teams().indexOf(t) - league.teams().size() / 2;
					if (amountOfChanges < 0) {
						amountOfChanges = 0;
					} else {
						amountOfChanges = Math.pow(amountOfChanges, 2);
					}
					for (int i = 0; i < amountOfChanges; i++) {
						t.offSeasonChangeSkill();
					}
				}
			}
		}
	}

	public boolean seasonOver() {
		return seasonOver;
	}

	/**
	 * All the matchups between two teams in the regular season.
	 * 
	 * @param t1 - team being matched up.
	 * @param t2 - team being matched up.
	 * @return all games where both these teams play eachother.
	 */
	public ArrayList<Game> matchups(Team t1, Team t2) {
		ArrayList<Game> matches = new ArrayList<>();
		for (Game g : games) {
			if ((t1 == g.away() && t2 == g.home()) || (t2 == g.away() && t1 == g.home())) {
				matches.add(g);
			}
		}
		return matches;
	}

	private int doubleCompare(double d1, double d2) {
		if (d1 > d2) {
			return 1;
		} else if (d1 < d2) {
			return -1;
		}
		return 0;
	}

	/**
	 * determines the winner of the head to head matchups of two teams. Returns null
	 * if they tied. They are first compared by points and then by regulation wins.
	 * 
	 * @param t1 - team 1 to compare.
	 * @param t2 - team 2 to compare.
	 * @return The team with more points against the other. Null if both teams tie.
	 */
	public Team headToHead(Team t1, Team t2) {
		ArrayList<Game> matchups = matchups(t1, t2);
		int t1points = 0;
		int secT1points = 0;
		int t2points = 0;
		int secT2points = 0;
		for (Game g : matchups) {
			if (!g.gamePlayed()) {
				break;
			}
			t1points += g.pointsEarned(t1);
			secT1points += g.gameResult(t1);
			t2points += g.pointsEarned(t2);
			secT2points += g.gameResult(t2);
		}
		if (t1points > t2points) {
			return t1;
		} else if (t1points < t2points) {
			return t2;
		} else if (secT1points > secT2points) {
			return t1;
		} else if (secT1points > secT2points) {
			return t2;
		}
		return null;
	}

	protected void pointTieCheck(HashMap<Team, double[]> map, ArrayList<Team> teams) {
		Collections.sort(teams, (x, y) -> doubleCompare(map.get(y)[0], map.get(x)[0]));
		for (int i = 0; i < teams.size() - 1; i++) {
			if (map.get(teams.get(i))[0] == map.get(teams.get(i + 1))[0]) {
				if (map.get(teams.get(i))[1] < map.get(teams.get(i + 1))[1]) {
					Collections.swap(teams, i, i + 1);
					for (int j = i; j > 0; j--) {
						if (map.get(teams.get(j))[1] < map.get(teams.get(j - 1))[1]) {
							Collections.swap(teams, j, j - 1);
						}
					}
				}
			}
		}
	}

	/**
	 * given a list of teams, this shows the points percentage of each team facing
	 * just the teams in this group.
	 * 
	 * @param teams - list of teams
	 * @return hashmap of teams to their points percentage against the other teams
	 *         in this list.
	 */
	public HashMap<Team, double[]> headToHead(ArrayList<Team> teams) {
		HashMap<Team, double[]> map = new HashMap<>();
		for (Team t : teams) {
			int pointCount = 0;
			int secondaryPointCount = 0;
			int gameCount = 0;
			for (Team s : teams) {
				if (s == t) {
					continue;
				}
				ArrayList<Game> gamesAgainstTeam = matchups(t, s);
				for (Game g : gamesAgainstTeam) {
					if (!g.gamePlayed()) {
						break;
					}
					pointCount += g.pointsEarned(t);
					secondaryPointCount += g.gameResult(t);
					gameCount++;
				}
			}
			if (gameCount == 0) {
				map.put(t, new double[] { 0.0, 0.0 });
			} else {
				map.put(t, new double[] { (double) pointCount / gameCount, (double) secondaryPointCount / gameCount });
			}

		}
		return map;
	}

}
