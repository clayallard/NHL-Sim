package sim;

import java.util.ArrayList;
import java.util.Collections;
import java.util.PriorityQueue;

public class Bracket {

	private char type;
	private ArrayList<Team> teams;
	private ArrayList<ArrayList<Series>> rounds;
	private int roundNumber;
	private int teamsRemaining;

	private static final int[] seriesHomeGames = new int[] { 1, 1, -1, -1, 1, -1, 1 };

	public Bracket(ArrayList<Team> teams, char seedingStructure) {
		type = seedingStructure;
		teamsRemaining = teams.size();
		rounds = new ArrayList<>();
		this.teams = new ArrayList<>();
		for (int i = 0; i < teamsRemaining; i++) {
			Team team = teams.get(i);
			this.teams.add(team);
		}
		nextRound();
	}

	/**
	 * number of teams in this bracket.
	 */
	public int numberOfTeams() {
		return teams.size();
	}

	/**
	 * number of teams remaining in this playoff bracket.
	 */
	public int teamsRemaining() {
		return teamsRemaining;
	}

	public ArrayList<ArrayList<Series>> rounds() {
		return rounds;
	}

	public ArrayList<String> matchups() {
		ArrayList<String> matchups = new ArrayList<>();
		for (ArrayList<Series> arr : rounds) {
			for (Series ser : arr) {
				matchups.add(ser.standings());
			}
		}
		return matchups;
	}

	/**
	 * optimal sum of seeds to guarantee that the lower seeds play the teams they
	 * are suppose to play in the first round.
	 * 
	 * @param number - number of teams.
	 * @return the optimal sum of the seeds of two teams.
	 */
	private int optimalSumOfSeeds(int number) {
		int pow = 2;
		while (pow < number) {
			pow *= 2;
		}
		return pow + 1;
	}

	/**
	 * the winner of the bracket tournament. Returns null if the winner hasn't been
	 * determined yet.
	 */
	public Team winner() {
		if (isOver()) {
			return teams.get(0);
		} else {
			return null;
		}
	}

	/**
	 * creates all the matchups for the next round of play.
	 */
	private void nextRound() {
		if (rounds.size() != roundNumber || teamsRemaining < 2) {
			return;
		}
		ArrayList<Series> newRound = new ArrayList<Series>();
		rounds.add(newRound);
		int optSeedNum = optimalSumOfSeeds(teamsRemaining);
		for (int i = teamsRemaining; i > optSeedNum / 2; i--) {
			Team lowerSeed = teams.get(i - 1);
			Team higherSeed = teams.get(optSeedNum - i - 1);
			lowerSeed.incrementResult();
			higherSeed.incrementResult();
			newRound.add(new Series(lowerSeed, higherSeed, seriesHomeGames));
		}
	}

	/**
	 * simulates an entire round of the playoffs.
	 */
	public void simulateRound() {
		if (type == 'R') {
			simRoundReseeding();
		} else if (type == 'N') {
			simRoundNoReseeding();
		} 
		if (!isOver()) {
			roundNumber++;
			nextRound();
		}
	}

	private void swap(ArrayList<Team> arr, Team t1, Team t2) {
		int indHigher = arr.indexOf(t1);
		int indLower = arr.indexOf(t2);
		Collections.swap(arr, indHigher, indLower);
	}

	private void simRoundReseeding() {
		for (Series ser : rounds.get(roundNumber)) {
			ser.simSeries();
			Team loser = ser.loser();
			teams.remove(loser);
			teams.add(loser);
			teamsRemaining--;
		}
	}

	private void simRoundNoReseeding() {
		for (Series ser : rounds.get(roundNumber)) {
			ser.simSeries();
			if (ser.winner() == ser.lowerSeed()) {
				swap(teams, ser.higherSeed(), ser.lowerSeed());
			}
			teamsRemaining--;
		}
		for (int i = 0; i < teamsRemaining / 2; i++) {
			if (teams.get(i).conferenceSeeding() > teams.get(teamsRemaining - i - 1).conferenceSeeding()) {
				Collections.swap(teams, i, teamsRemaining - i - 1);
			}
		}
	}

	/**
	 * determines if this playoff bracket is over meaning there are no more
	 * matchups.
	 */
	public boolean isOver() {
		return teamsRemaining == 1;
	}
}
