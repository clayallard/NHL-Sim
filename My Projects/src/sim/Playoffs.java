package sim;

import java.util.ArrayList;
import java.util.Collections;

public class Playoffs {

	private ArrayList<Team> confWinners;
	private ArrayList<Bracket> preDivMatchups;
	private ArrayList<Bracket> preConfMatchups;
	private Bracket finalsMatchups;
	private ArrayList<ArrayList<Series>> allSeries;
	private char type;
	private League league;
	private int amountOfRounds;
	private int maxSubRounds;
	private Team winner;

	public Playoffs(League league, char type) {
		this.league = league;
		this.type = type;
		confWinners = new ArrayList<>();
		preDivMatchups = new ArrayList<>();
		preConfMatchups = new ArrayList<>();
		allSeries = new ArrayList<>();
	}

	public ArrayList<Team> playoffTeams() {
		ArrayList<Team> playoffTeams = new ArrayList<>();
		for (Team t : league.teams()) {
			if (t.playoffs()) {
				playoffTeams.add(t);
			}
		}
		return playoffTeams;
	}

	/**
	 * determines if the playoffs are over.
	 */
	public boolean isOver() {
		return winner != null;
	}

	/**
	 * Amount of rounds required for a playoff bracket depending on the amount of
	 * teams.
	 * 
	 * @param amountOfTeams - amount of teams in the bracket.
	 * @return amount of rounds necessary.
	 */
	private int amountOfSubRounds(int amountOfTeams) {
		return (int) Math.ceil(Math.log(amountOfTeams) / Math.log(2));
	}

	/**
	 * amount of rounds required for the entire tournament.
	 * 
	 * @return
	 */
	public int amountOfRounds() {
		ArrayList<Integer> ints = new ArrayList<>();
		for (Conference conf : league.conferences()) {
			ints.add(conf.amountOfPlayoffTeams());
		}
		int max = maxNumberOfRounds(ints);
		amountOfRounds = max + amountOfSubRounds(league.conferenceAmount());
		return amountOfRounds;
	}

	/**
	 * Maximum amount of rounds accross all
	 * 
	 * @param teams
	 * @return
	 */
	private int maxNumberOfRounds(ArrayList<Integer> teams) {
		int max = 0;
		for (Integer amount : teams) {
			amount = amountOfSubRounds(amount);
			if (amount > max) {
				max = amount;
			}
		}
		maxSubRounds = max;
		return max;
	}

	public ArrayList<ArrayList<Series>> allSeries() {
		int round = 0;
		ArrayList<Integer> ints = new ArrayList<>();
		for (Conference conf : league.conferences()) {
			ints.add(conf.amountOfPlayoffTeams());
		}
		int max = maxNumberOfRounds(ints);
		while (round < max) {
			allSeries.add(new ArrayList<>());
			for (Bracket brack : preConfMatchups) {
				if (amountOfSubRounds(brack.teamsRemaining()) <= max) {
					for (Series ser : brack.rounds().get(round)) {
						allSeries.get(round).add(ser);
					}
				}
			}
			round++;
		}
		amountOfRounds();
		while (round < amountOfRounds) {
			allSeries.add(new ArrayList<>());
			for (Series ser : finalsMatchups.rounds().get(round - max)) {
				allSeries.get(round).add(ser);
			}
			round++;
		}
//			for (Series ser : finalsMatchups.rounds().get(round - max)) {
//				allSeries.get(round).add(ser);
//		}
		return allSeries;
	}

	public String toString() {
		String str = league.regularSeason().year() + "-" + (league.regularSeason().year() + 1) + " Playoffs\n";
		for (int i = 1; i <= allSeries.size(); i++) {
			str += "\nRound " + i;
			ArrayList<Series> arr = allSeries.get(i - 1);
			str += "\n" + arr.get(0).standings() + "   " + arr.get(0).seriesSummary();
			for (int j = 1; j < arr.size(); j++) {
				Series ser = arr.get(j);
				if (ser.conference() != arr.get(j - 1).conference() && i <= maxSubRounds) {
					str += "\n";
				}
				str += "\n" + ser.standings() + "   " + ser.seriesSummary();
			}
			str += "\n";
		}
		return str;
	}

	public void simPlayoffs() {
		if (type == 'D') {
			simDivisionPlayoffs();
			return;
		}
		for (Conference conf : league.conferences()) {
			ArrayList<Team> playoffTeams = new ArrayList<>();
			for (int i = 0; i < conf.amountOfPlayoffTeams(); i++) {
				playoffTeams.add(conf.teams().get(i));
			}
			Bracket confPlay = new Bracket(playoffTeams, type);
			preConfMatchups.add(confPlay);
			while (!confPlay.isOver()) {
				confPlay.simulateRound();
			}
			confWinners.add(confPlay.winner());
		}
		Collections.sort(confWinners, (x, y) -> x.leagueSeeding() - y.leagueSeeding());
		finalsMatchups = new Bracket(confWinners, type);
		while (!finalsMatchups.isOver()) {
			finalsMatchups.simulateRound();
		}
		winner = finalsMatchups.winner();
		winner.incrementResult();
		allSeries();
	}

	private Team[] wildCardTeams(Conference conf) {
		int amountOfWildCard = conf.amountOfPlayoffTeams() - conf.teamsPerDivisionPlayoffs() * conf.divisions().size();
		Team[] teams = new Team[amountOfWildCard];
		int counter = 0;
		for (Team t : conf.teams()) {
			if (t.divisionSeeding() > conf.teamsPerDivisionPlayoffs()) {
				teams[counter] = t;
				counter++;
				if (counter == amountOfWildCard) {
					break;
				}
			}
		}
		return teams;
	}

	// could change later but just want to get the real life case to work.
	private void simDivisionPlayoffs() {
		for (Conference conf : league.conferences()) {
			int amountOfWildCard = conf.amountOfPlayoffTeams()
					- conf.teamsPerDivisionPlayoffs() * conf.divisions().size();
			if (amountOfWildCard != conf.divisions().size()) {
				throw new IllegalArgumentException("One wildcard per division");
			}
			ArrayList<Team> divLeaders = new ArrayList<>();
			for (Division div : conf.divisions()) {
				divLeaders.add(div.leader());
			}
			Collections.sort(divLeaders, (x, y) -> y.leagueSeeding() - x.leagueSeeding());
			int counter = 0;
			Team[] wildCard = wildCardTeams(conf);
			ArrayList<Team> divWinners = new ArrayList<>();
			for (Team t : divLeaders) {
				ArrayList<Team> divPlayoffTeams = new ArrayList<>();
				for (int i = 0; i < conf.teamsPerDivisionPlayoffs(); i++) {
					divPlayoffTeams.add(t.division().teams().get(i));
				}
				divPlayoffTeams.add(wildCard[counter]);
				Bracket divPlay = new Bracket(divPlayoffTeams, 'N');
				preDivMatchups.add(divPlay);
				while (!divPlay.isOver()) {
					divPlay.simulateRound();
				}
				divWinners.add(divPlay.winner());
			}
			Collections.sort(divWinners, (x, y) -> x.leagueSeeding() - y.leagueSeeding());
			Bracket confPlay = new Bracket(divWinners, 'R');
			preConfMatchups.add(confPlay);
			while (!confPlay.isOver()) {
				confPlay.simulateRound();
			}
			confWinners.add(confPlay.winner());
		}
		Collections.sort(confWinners, (x, y) -> x.leagueSeeding() - y.leagueSeeding());
		finalsMatchups = new Bracket(confWinners, 'R');
		while (!finalsMatchups.isOver()) {
			finalsMatchups.simulateRound();
		}
		winner = finalsMatchups.winner();
		winner.incrementResult();
		allDivSeries();
	}

	public ArrayList<ArrayList<Series>> allDivSeries() {
		int round = 0;
		ArrayList<Integer> ints = new ArrayList<>();
		for (Conference conf : league.conferences()) {
			ints.add(conf.amountOfPlayoffTeams());
		}
		int max = maxNumberOfRounds(ints);
		while (round < 2) {
			allSeries.add(new ArrayList<>());
			for (Bracket brack : preDivMatchups) {
				if (amountOfSubRounds(brack.teamsRemaining()) <= max) {
					for (Series ser : brack.rounds().get(round)) {
						allSeries.get(round).add(ser);
					}
				}
			}
			round++;
		}
		while (round < 3) {
			allSeries.add(new ArrayList<>());
			for (Bracket brack : preConfMatchups) {
				if (amountOfSubRounds(brack.teamsRemaining()) <= max) {
					for (Series ser : brack.rounds().get(round - 2)) {
						allSeries.get(round - 2).add(ser);
					}
				}
			}
			round++;
		}
		amountOfRounds();
		while (round < amountOfRounds) {
			allSeries.add(new ArrayList<>());
			for (Series ser : finalsMatchups.rounds().get(round - max)) {
				allSeries.get(round).add(ser);
			}
			round++;
		}
//			for (Series ser : finalsMatchups.rounds().get(round - max)) {
//				allSeries.get(round).add(ser);
//		}
		return allSeries;
	}

	public Team winner() {
		return winner;
	}

}
