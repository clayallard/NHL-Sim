package sim;

import java.util.ArrayList;

public class Series {

	private Team higherSeed;
	private Team lowerSeed;
	private int[] winsForEachTeam;
	private ArrayList<Game> games;
	private int gamesPlayed;
	private Team winner;
	private boolean seriesOver;
	private String round;

	/**
	 * Creates a playoff series between two teams. The amount of games played is
	 * determined by the length of the boolean array. This array should be odd so
	 * that there is guaranteed to be a clear winner. If it is even, an extra game
	 * will be added where the higher seeded team will have home advantage.
	 * 
	 * @param lowerSeed     - lower seeded team (had less points).
	 * @param higherSeed    - higher seeded team.
	 * @param homeAdvantage - an array that represents who is home for each game.
	 *                      True means that the higher seeded team is home while
	 *                      false means the lower seeded team is home for that
	 *                      particular game.
	 */
	public Series(Team lowerSeed, Team higherSeed, int[] homeAdvantage) {
		this(lowerSeed, higherSeed, null, homeAdvantage);
	}

	/**
	 * Creates a playoff series between two teams. The amount of games played is
	 * determined by the length of the boolean array. This array should be odd so
	 * that there is guaranteed to be a clear winner. If it is even, an extra game
	 * will be added where neither team will have home advantage.
	 * 
	 * @param lowerSeed     - lower seeded team (had less points).
	 * @param higherSeed    - higher seeded team.
	 * @param homeAdvantage - an array that represents who is home for each game.
	 *                      True means that the higher seeded team is home while
	 *                      false means the lower seeded team is home for that
	 *                      particular game.
	 */
	public Series(Team lowerSeed, Team higherSeed, String round, int[] homeAdvantage) {
		this.round = round;
		this.lowerSeed = lowerSeed;
		this.higherSeed = higherSeed;
		winsForEachTeam = new int[2];
		games = new ArrayList<>();
		int gameNumber = 0;
		Game game;
		for (int i = 0; i < homeAdvantage.length; i++) {
			gameNumber++;
			if (homeAdvantage[i] > 0) {
				game = new Game(lowerSeed, higherSeed, 'P');
				games.add(game);
			} else if (homeAdvantage[i] < 0) {
				game = new Game(higherSeed, lowerSeed, 'P');
				games.add(game);
			} else {
				game = new Game(lowerSeed, higherSeed, 'P');
				game.neutral(true);
				games.add(game);
			}
			game.setGameNumber(gameNumber);
		}
		if (homeAdvantage.length % 2 == 0) {
			game = new Game(lowerSeed, higherSeed, 'P');
			games.add(game);
			game.neutral(true);
			game.setGameNumber(gameNumber + 1);
		}
	}

	/**
	 * conference that this series is played in.
	 */
	public Conference conference() {
		if (higherSeed.conference() == lowerSeed.conference()) {
			return higherSeed.conference();
		}
		return null;
	}

	/**
	 * higher seeded team.
	 */
	public Team higherSeed() {
		return higherSeed;
	}

	/**
	 * lower seeded team.
	 */
	public Team lowerSeed() {
		return lowerSeed;
	}

	private boolean necessaryGame(int gameNumber) {
		if (gamesPlayed + gamesToWin() - Math.max(winsForEachTeam[0], winsForEachTeam[1]) >= gameNumber) {
			return true;
		}
		return false;
	}

	public String seriesSummary() {
		String str = "[";
		ArrayList<Team> winners = winnerEachGame();
		str += winners.get(0).abbreviation() + games.get(0).otCount();
		for (int i = 1; i < winners.size(); i++) {
			Team s = winners.get(i);
			str += ", " + s.abbreviation() + games.get(i).otCount();
		}
		str += "]";
		return str;
	}

	/**
	 * all games in the series. There is a star if the game is not yet determined to
	 * be necessary. Any game that is unnecessary will not be shown.
	 */
	public String games() {
		int year = higherSeed.regularSeason().year();
		String str = year + "-" + (year + 1) + " ";
		if (round != null) {
			str += round;
		}
		str += "(" + lowerSeed.conferenceSeeding() + " " + lowerSeed.city() + " " + lowerSeed.name() + " vs. "
				+ higherSeed.conferenceSeeding() + " " + higherSeed.city() + " " + higherSeed.name() + ")\n";
		int amountOfChar = str.length();
		for (int i = 0; i < amountOfChar - 1; i++) {
			str += "_";
		}
		str += "\n";
		for (int i = 0; i < gamesToWin(); i++) {
			str += "\n" + games.get(i).toString();
		}
		int i = gamesToWin();
		while (necessaryGame(i + 1)) {
			str += "\n" + games.get(i).toString();
			i++;
		}
		if (!seriesOver) {
			for (int j = i; j < games.size(); j++) {
				str += "\n*" + games.get(j).toString();
			}
		} else {
			str += "\n\n[" + winner.conferenceSeeding() + "] " + winner.abbreviation() + "  defeats  ["
					+ loser().conferenceSeeding() + "] " + loser().abbreviation() + "   " + amountOfWins(winner) + "-"
					+ amountOfWins(loser());
		}
		str += "\n";
		return str;
	}

	/**
	 * the current score of the series.
	 */
	public String standings() {
		return higherSeed.conferenceSeeding() + " " + higherSeed.city() + " " + higherSeed.name() + "  "
				+ amountOfWins(higherSeed) + "-" + amountOfWins(lowerSeed) + "  " + lowerSeed.city() + " "
				+ lowerSeed.name() + " " + lowerSeed.conferenceSeeding();
	}

	/**
	 * the amount of wins necessary for a team to win the series.
	 */
	public int gamesToWin() {
		return games.size() / 2 + 1;
	}

	/**
	 * The amount of games that have been played in this series so far.
	 */
	public int gamesPlayed() {
		return gamesPlayed;
	}

	/**
	 * simulates entire playoff series.
	 */
	public void simSeries() {
		simToThisGame(games.size());
	}

	/**
	 * The amount of wins in this series from team.
	 * 
	 * @param team - team to get wins from.
	 * @return amount of wins from team. -1 if the team is not apart of this series.
	 */
	public int amountOfWins(Team team) {
		if (team == higherSeed) {
			return winsForEachTeam[1];
		}
		if (team == lowerSeed) {
			return winsForEachTeam[0];
		}
		return -1;
	}

	/**
	 * winner of each game in the series.
	 */
	public ArrayList<Team> winnerEachGame() {
		ArrayList<Team> winners = new ArrayList<>();
		int index = 0;
		while (index < 7 && games.get(index).gamePlayed()) {
			winners.add(games.get(index).winner());
			index++;
		}
		return winners;
	}

	/**
	 * Simulates only to a specific game in the series.
	 * 
	 * @param gameNumber - game number of simulate to.
	 */
	public void simToThisGame(int gameNumber) {
		if (gameNumber > games.size()) {
			gameNumber = games.size();
		}
		while (gamesPlayed < gameNumber && winsForEachTeam[1] < gamesToWin() && winsForEachTeam[0] < gamesToWin()) {
			Game game = games.get(gamesPlayed);
			game.simGame();
			if (game.winner() == lowerSeed) {
				winsForEachTeam[0]++;
			} else {
				winsForEachTeam[1]++;
			}
			gamesPlayed++;
		}
		if (winsForEachTeam[0] == gamesToWin()) {
			winner = lowerSeed;
			seriesOver = true;
		} else if (winsForEachTeam[1] == gamesToWin()) {
			winner = higherSeed;
			seriesOver = true;
		}
	}

	/**
	 * simulate the next game in the series.
	 */
	public void simNextGame() {
		simToThisGame(gamesPlayed + 1);
	}

	/**
	 * Simulate the next amount of games in this series.
	 * 
	 * @param amount - amount of games to simulate.
	 */
	public void simNextAmountOfGames(int amount) {
		simToThisGame(gamesPlayed + amount);
	}

	/**
	 * determines whether the series is still going or not.
	 */
	public boolean seriesInProgress() {
		return !seriesOver;
	}

	/**
	 * the winner of this series. If the winner has yet to be determined, then this
	 * will return null.
	 */
	public Team winner() {
		if (winner == higherSeed) {
			return higherSeed;
		} else if (winner == lowerSeed) {
			return lowerSeed;
		}
		return null;
	}

	/**
	 * the loser of this series. If the winner has yet to be determined, then this
	 * will return null.
	 */
	public Team loser() {
		if (winner == higherSeed) {
			return lowerSeed;
		} else if (winner == lowerSeed) {
			return higherSeed;
		}
		return null;
	}

}
