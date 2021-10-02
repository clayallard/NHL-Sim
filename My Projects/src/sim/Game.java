package sim;

import java.util.Random;

public class Game {

	private Team home;
	private Team away;
	private Team winner;
	private int countOT;
	private boolean gamePlayed;
	private char gameType;
	private int gameNumber;
	private boolean neutral;

	final static double root = .6;
	final static double homeAdv = 1.15;

	/**
	 * Constructs game of with away team and home team. If the game type is not put
	 * in as a parameter, it will be treated as an exhibition match. There are 3
	 * options: 'E' is for exhibition which means it does not affect standings or
	 * team dynamics. 'R' is for regular season which means it is a regular season
	 * game that does chance the standings and team dynamics. 'P' is for playoffs
	 * which means it is a playoff game. Doesn't affect the standings, but will
	 * change team dynamics and is played as part of determining a champion.
	 * Lowercase works as well. If another letter is put in that is not one of these
	 * 3, it will be considered an exhibition match.
	 * 
	 * @param awayTeam - the away team.
	 * @param homeTeam - the home team.
	 */
	public Game(Team awayTeam, Team homeTeam) {
		this(awayTeam, homeTeam, 'E');
	}

	/**
	 * Constructs game of with away team and home team. If the game type is not put
	 * in as a parameter, it will be treated as an exhibition match. There are 3
	 * options: 'E' is for exhibition which means it does not affect standings or
	 * team dynamics. 'R' is for regular season which means it is a regular season
	 * game that does chance the standings and team dynamics. 'P' is for playoffs
	 * which means it is a playoff game. Doesn't affect the standings, but will
	 * change team dynamics and is played as part of determining a champion.
	 * Lowercase works as well. If another letter is put in that is not one of these
	 * 3, it will be considered an exhibition match.
	 * 
	 * @param awayTeam - the away team.
	 * @param homeTeam - the home team.
	 * @param gameType - chooses whether this game is an exhibition match, regular
	 *                 season game, or playoff game.
	 */
	public Game(Team awayTeam, Team homeTeam, char gameType) {
		home = homeTeam;
		away = awayTeam;
		this.gameType = gameType;
	}

	/**
	 * counts the amount of overtimes in order to properly format the final score.
	 */
	protected String otCount() {
		if (countOT == 0) {
			return "";
		} else if (countOT == 1) {
			return "(OT)";
		} else {
			return "(" + countOT + "OT)";
		}
	}

	/**
	 * sets regular season game number.
	 * 
	 * @param gnum - number to set game to.
	 */
	protected void setGameNumber(int gnum) {
		gameNumber = gnum;
	}

	/**
	 * game number for this particular playoff/regular season game. An ordering for
	 * when the games are played.
	 */
	public int gameNumber() {
		return gameNumber;
	}

	/**
	 * amount of overtimes.
	 */
	public int amountOfOvertimes() {
		return countOT;
	}

	/**
	 * if selected to be true, there is no team with a home advantage.
	 */
	public boolean neutral(boolean homeAdvantage) {
		neutral = homeAdvantage;
		return neutral;
	}

	/**
	 * Gives the team the proper letter for whether they won or lost. 'W' if the
	 * team won, 'L' if the team lost.
	 * 
	 * @param team - team that played.
	 * @return 'W' if the team won, 'L' if the team lost.
	 */
	private char gameResultString(Team team) {
		if (winner == team) {
			return 'W';
		} else {
			return 'L';
		}
	}

	/**
	 * converts the game result into a string using the teams abbreviations.
	 */
	public String toString() {
		String str = "";
		if (gameNumber != 0) {
			str += gameNumber + "   ";
		}
		if (gamePlayed) {
			str += away.abbreviation() + " " + gameResultString(away) + " @ " + home.abbreviation() + " "
					+ gameResultString(home) + "  " + otCount();
		} else {
			str += away.abbreviation() + " @ " + home.abbreviation();
			;
		}
		return str;
	}

	/**
	 * the home team of this game.
	 */
	public Team home() {
		return home;
	}

	/**
	 * the away team of this game.
	 */
	public Team away() {
		return away;
	}

	/**
	 * winner of this game. Returns null if the game hasn't been played yet.
	 */
	public Team winner() {
		return winner;
	}

	/**
	 * loser of this game. Returns null if the game hasn't been played yet.
	 */
	public Team loser() {
		if (winner == home) {
			return away;
		} else {
			return home;
		}
	}

	/**
	 * determines whether or not the game has been played.
	 */
	public boolean gamePlayed() {
		return gamePlayed;
	}

	/**
	 * determines whether this game went to overtime or not.
	 */
	public boolean ot() {
		return countOT > 0;
	}

	/**
	 * transfers the game results to each of the competing teams.
	 */
	private void gameResult() {
		int homeGameResult = gameResult(home);
		home.gameResult(homeGameResult);
		away.gameResult(3 - homeGameResult);
	}

	/**
	 * game result for specific team.
	 * 
	 * @param team - team to get result of.
	 * @return 0 for loss, for OT loss, 2 for OT win, and 3 for win.
	 */
	protected int gameResult(Team team) {
		if (winner == team) {
			if (countOT == 0) {
				return 3;
			} else {
				return 2;
			}
		} else {
			if (countOT == 0) {
				return 0;
			} else {
				return 1;
			}
		}
	}

	/**
	 * determines home advantage value depending on if the location of play is
	 * neutral.
	 */
	private double homeAdvantage() {
		double val;
		if (neutral) {
			val = homeAdv;
		} else {
			val = 1;
		}
		return val;
	}

	/**
	 * sims playoff game.
	 */
	private void simPlayoffGame() {
		Random rng = new Random();
		double p = rng.nextDouble();
		Team worseTeam;
		Team betterTeam;
		double noAdv = homeAdvantage();
		if (home.skillLevel() * homeAdv / noAdv > away.skillLevel()) {
			worseTeam = away;
			betterTeam = home;
		} else {
			worseTeam = home;
			betterTeam = away;
		}
		double worseTeamSkill = worseTeam.skillLevel();
		double betterTeamSkill = betterTeam.skillLevel() * homeAdv / noAdv;
		double otProb = .27 - .4 * Math.pow(Math.abs(betterTeamSkill / (betterTeamSkill + worseTeamSkill) - .5), root);
		double winProb = Math.pow(betterTeamSkill / (betterTeamSkill + worseTeamSkill) - .5, root) + .5;
		while (p < Math.pow(otProb * .9, countOT + 1)) {
			countOT++;
		}
		if (countOT == 0) {
			if (p < otProb + winProb * (1 - otProb)) {
				winner = betterTeam;
			} else {
				winner = worseTeam;
			}
		} else {
			winProb = (winProb - .5) / 2 + .5;
			if (p < Math.pow(otProb, countOT + 1)
					+ winProb * (Math.pow(otProb, countOT) - Math.pow(otProb, countOT + 1))) {
				winner = betterTeam;
			} else {
				winner = worseTeam;
			}
		}
		gamePlayed = true;
	}

	/*
	 * sims regular season game.
	 */
	private void simRegularSeasonGame() {
		Random rng = new Random();
		double p = rng.nextDouble();
		Team worseTeam;
		Team betterTeam;
		double noAdv = homeAdvantage();
		if (home.skillLevel() * homeAdv / noAdv > away.skillLevel()) {
			worseTeam = away;
			betterTeam = home;
		} else {
			worseTeam = home;
			betterTeam = away;
		}
		double worseTeamSkill = worseTeam.skillLevel();
		double betterTeamSkill = betterTeam.skillLevel() * homeAdv / noAdv;
		double otProb = .28 - .4 * Math.pow(Math.abs(betterTeamSkill / (betterTeamSkill + worseTeamSkill) - .5), root);
		double winProb = Math.pow(betterTeamSkill / (betterTeamSkill + worseTeamSkill) - .5, root) + .5;
		if (p < otProb) {
			countOT = 1;
			if (p < otProb * ((winProb - .5) / 4 + .5)) {
				winner = betterTeam;
			} else {
				winner = worseTeam;
			}
		} else if (p < otProb + winProb * (1 - otProb)) {
			winner = betterTeam;
		} else {
			winner = worseTeam;
		}
		gamePlayed = true;
	}

	/**
	 * sims exhibition match.
	 */
	private void exhibitionMatch() {
		Random rng = new Random();
		double p = rng.nextDouble();
		Team worseTeam;
		Team betterTeam;
		double noAdv = homeAdvantage();
		if (home.skillLevel() * homeAdv / noAdv > away.skillLevel()) {
			worseTeam = away;
			betterTeam = home;
		} else {
			worseTeam = home;
			betterTeam = away;
		}
		double worseTeamSkill = worseTeam.skillLevel();
		double betterTeamSkill = betterTeam.skillLevel() * homeAdv / noAdv;
		double otProb = .28 - .4 * Math.pow(Math.abs(betterTeamSkill / (betterTeamSkill + worseTeamSkill) - .5), root);
		double winProb = Math.pow(betterTeamSkill / (betterTeamSkill + worseTeamSkill) - .5, root) + .5;
		if (p < otProb) {
			countOT = 1;
			if (p < otProb * ((winProb - .5) / 4 + .5)) {
				winner = betterTeam;
			} else {
				winner = worseTeam;
			}
		} else if (p < otProb + winProb * (1 - otProb)) {
			winner = betterTeam;
		} else {
			winner = worseTeam;
		}
		gamePlayed = true;
	}

	/**
	 * The amount of team points earned. The 3 signifies that the team won in
	 * regulation but not that they got 3 points.
	 * 
	 * @param points - result code.
	 * @return amount of points according to what they get in the standings.
	 */
	protected int pointsEarned(int points) {
		if (points == 3) {
			return 2;
		}
		return points;
	}

	/**
	 * The amount of team points earned. The 3 signifies that the team won in
	 * regulation but not that they got 3 points.
	 * 
	 * @param team - team with the points.
	 * @return amount of points according to what they get in the standings.
	 */
	protected int pointsEarned(Team team) {
		return pointsEarned(gameResult(team));
	}

	/**
	 * simulates this game based on each team's skill level.
	 */
	protected void simGame() {
		if (!gamePlayed) {
			if (gameType == 'R' || gameType == 'r') {
				simRegularSeasonGame();
				gameResult();
				away.changeSkill();
				home.changeSkill();
			} else if (gameType == 'P' || gameType == 'p') {
				simPlayoffGame();
				away.changeSkill();
				home.changeSkill();
			} else {
				exhibitionMatch();
			}
		}
	}

}