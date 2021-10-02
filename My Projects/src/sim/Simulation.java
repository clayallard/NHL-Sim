package sim;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Random;

public class Simulation {

	public static ArrayList<Game> scheduleGenerator1(League league) {
		ArrayList<Game> allGames = new ArrayList<>();
		ArrayList<Team> teams = league.teams();
		for (Team t : teams) {
			for (Team d : teams) {
				if (t != d) {
					allGames.add(new Game(d, t, 'R'));
				}
			}
			ArrayList<Team> divTeams = t.division().teams();
			for (Team d : divTeams) {
				if (t != d) {
					allGames.add(new Game(d, t, 'R'));
				}
			}
		}
		for (Conference conf : league.conferences()) {
			nonDivisionalConferenceGames(conf, allGames);
		}
		Collections.shuffle(allGames);
		return allGames;
	}

	private static void nonDivisionalConferenceGames(Conference conf, ArrayList<Game> games) {
		if (conf == null) {
			return;
		}
		ArrayList<Division> divs = conf.divisions();
		for (int i = 0; i < divs.size() - 1; i++) {
			for (int j = i + 1; j < divs.size(); j++) {
				games.addAll(randomHomeIce(divs.get(i), divs.get(j)));
			}
		}
	}

	private static ArrayList<Game> randomHomeIce(Division d1, Division d2) {
		ArrayList<Team> teamsInd1 = d1.teams();
		ArrayList<Team> teamsInd2 = d2.teams();
		ArrayList<Game> games1 = new ArrayList<>();
		ArrayList<Game> games2 = new ArrayList<>();
		ArrayList<Team> possibleMatchups = new ArrayList<>();
		Random rng = new Random();
		for (Team t : teamsInd2) {
			possibleMatchups.add(t);
			possibleMatchups.add(t);
		}
		int teamNumber = 0;
		for (Team t : teamsInd1) {
			if (possibleMatchups.size() == 4) {
				if (appearsTwice(possibleMatchups) == 1) {
					Team randTeam1 = possibleMatchups.remove(3);
					Team randTeam2 = possibleMatchups.remove(rng.nextInt(2));
					games1.add(new Game(randTeam1, t, 'R'));
					games1.add(new Game(randTeam2, t, 'R'));
					for (Team d : teamsInd2) {
						if (games1.get(2 * teamNumber).away() != d && games1.get(2 * teamNumber + 1).away() != d) {
							games2.add(new Game(t, d, 'R'));
						}
					}
					teamNumber++;
					continue;
				}
			}
			Team randTeam1 = possibleMatchups.remove(rng.nextInt(possibleMatchups.size()));
			int indexOfTeamToRemove = rng.nextInt(possibleMatchups.size());
			Team randTeam2 = possibleMatchups.get(indexOfTeamToRemove);
			// got unlucky and last two are the same team.
			while (randTeam1 == randTeam2) {
				indexOfTeamToRemove = rng.nextInt(possibleMatchups.size());
				randTeam2 = possibleMatchups.get(indexOfTeamToRemove);
			}
			possibleMatchups.remove(indexOfTeamToRemove);
			games1.add(new Game(randTeam1, t, 'R'));
			games1.add(new Game(randTeam2, t, 'R'));
			for (Team d : teamsInd2) {
				if (games1.get(2 * teamNumber).away() != d && games1.get(2 * teamNumber + 1).away() != d) {
					games2.add(new Game(t, d, 'R'));
				}
			}
			teamNumber++;
		}
		games1.addAll(games2);
		return games1;
	}

	private static int appearsTwice(ArrayList<Team> arr) {
		int count = 0;
		for (int i = 3; i > 0; i--) {
			if (arr.get(i) == arr.get(i - 1)) {
				count++;
				Collections.swap(arr, i, 3);
				Collections.swap(arr, i - 1, 2);
			}
		}
		return count;
	}

	private static ArrayList<Game> scheduleGenerator2021(League league) {
		ArrayList<Game> games = new ArrayList<>();
		for (Team t : league.teams()) {
			ArrayList<Team> divTeams = t.division().teams();
			for (Team d : divTeams) {
				if (t != d) {
					games.add(new Game(d, t, 'R'));
					games.add(new Game(d, t, 'R'));
					games.add(new Game(d, t, 'R'));
					games.add(new Game(d, t, 'R'));
				}
			}
		}
		return games;
	}

	private static double orderStatistic(int num) {
		ArrayList<Double> ints = new ArrayList<>();
		Random rng = new Random();
		if (rng.nextBoolean()) {
			while (num != 31 && rng.nextBoolean()) {
				num++;
			}
		} else {
			while (num != 1 && rng.nextBoolean()) {
				num--;
			}
		}
		for (int i = 0; i < 31; i++) {
			ints.add(new Random().nextGaussian() * 750 + 6250);
		}
		Collections.sort(ints, (x, y) -> y.compareTo(x));
		return ints.get(num - 1);
	}

	private static int compare(int[] a1, int[] a2) {
		if (a1[1] == a2[1]) {
			return a1[0] - a2[0];
		}
		return a1[1] - a2[1];
	}
	
	public static String sim2021Season(int years) {
		League nhl = new League("NHL");
		Conference c1 = new Conference("East", nhl, 4, 1, 'p');
		Conference c2 = new Conference("Central", nhl, 4, 1, 'p');
		Conference c3 = new Conference("West", nhl, 4, 1, 'p');
		Conference c4 = new Conference("North", nhl, 4, 1, 'p');
		Division d1 = new Division("East", c1);
		Division d2 = new Division("Central", c2);
		Division d3 = new Division("West", c3);
		Division d4 = new Division("North", c4);
		Team t1 = new Team("Boston", "Bruins", "BOS", d1, orderStatistic(9));
		Team t2 = new Team("Buffalo", "Sabres", "BUF", d1, orderStatistic(22));
		Team t3 = new Team("New Jersey", "Devils", "NJ", d1, orderStatistic(26));
		Team t4 = new Team("New York", "Islanders", "NYI", d1, orderStatistic(7));
		Team t5 = new Team("New York", "Rangers", "NYR", d1, orderStatistic(19));
		Team t6 = new Team("Philadelphia", "Flyers", "PHI", d1, orderStatistic(5));
		Team t7 = new Team("Pittsburgh", "Penguins", "PIT", d1, orderStatistic(13));
		Team t8 = new Team("Washington", "Capitals", "WSH", d1, orderStatistic(10));
		Team t9 = new Team("Carolina", "Hurricanes", "CAR", d2, orderStatistic(8));
		Team t10 = new Team("Columbus", "Blue Jackets", "CBJ", d2, orderStatistic(16));
		Team t11 = new Team("Detroit", "Red Wings", "DET", d2, orderStatistic(31));
		Team t12 = new Team("Chicago", "Blackhawks", "CHI", d2, orderStatistic(30));
		Team t13 = new Team("Florida", "Panthers", "FLA", d2, orderStatistic(23));
		Team t14 = new Team("Dallas", "Stars", "DAL", d2, orderStatistic(18));
		Team t15 = new Team("Nashville", "Predators", "NSH", d2, orderStatistic(20));
		Team t16 = new Team("Tampa Bay", "Lightning", "TB", d2, orderStatistic(2));
		Team t17 = new Team("Anaheim", "Ducks", "ANA", d3, orderStatistic(27));
		Team t18 = new Team("Arizona", "Coyotes", "ARI", d3, orderStatistic(28));
		Team t19 = new Team("Colorado", "Avalanche", "COL", d3, orderStatistic(1));
		Team t20 = new Team("Los Angeles", "Kings", "LA", d3, orderStatistic(24));
		Team t21 = new Team("Minnesota", "Wild", "MIN", d3, orderStatistic(21));
		Team t22 = new Team("San Jose", "Sharks", "SJ", d3, orderStatistic(29));
		Team t23 = new Team("St. Louis", "Blues", "STL", d3, orderStatistic(3));
		Team t24 = new Team("Vegas", "Golden Knights", "VGK", d3, orderStatistic(4));
		Team t25 = new Team("Calgary", "Flames", "CGY", d4, orderStatistic(12));
		Team t26 = new Team("Edmonton", "Oilers", "EDM", d4, orderStatistic(11));
		Team t27 = new Team("Montreal", "Canadiens", "MTL", d4, orderStatistic(14));
		Team t28 = new Team("Ottawa", "Senators", "OTT", d4, orderStatistic(25));
		Team t29 = new Team("Toronto", "Maple Leafs", "TOR", d4, orderStatistic(6));
		Team t30 = new Team("Vancouver", "Canucks", "VAN", d4, orderStatistic(15));
		Team t31 = new Team("Winnipeg", "Jets", "WPG", d4, orderStatistic(17));
		
//		int count = 0;
//		int amountOfDifferentTeams = 0;
//		ArrayList<Team> prevPlayoffTeams = new ArrayList<Team>();
		String s = "";
			for (int i = 0; i < years; i++) {
				ArrayList<Game> games = new ArrayList<>();
				games = scheduleGenerator2021(nhl);

				games.add(new Game(t25, t26, 'R'));
				games.add(new Game(t26, t25, 'R'));
				games.add(new Game(t25, t30, 'R'));
				games.add(new Game(t30, t25, 'R'));
				games.add(new Game(t30, t26, 'R'));
				games.add(new Game(t26, t30, 'R'));
				games.add(new Game(t27, t28, 'R'));
				games.add(new Game(t28, t27, 'R'));
				games.add(new Game(t27, t29, 'R'));
				games.add(new Game(t29, t27, 'R'));
				games.add(new Game(t28, t31, 'R'));
				games.add(new Game(t31, t28, 'R'));
				games.add(new Game(t29, t31, 'R'));
				games.add(new Game(t31, t29, 'R'));

				games.add(new Game(t26, t27, 'R'));
				games.add(new Game(t28, t26, 'R'));
				games.add(new Game(t29, t26, 'R'));
				games.add(new Game(t26, t31, 'R'));

				games.add(new Game(t27, t25, 'R'));
				games.add(new Game(t25, t28, 'R'));
				games.add(new Game(t25, t29, 'R'));
				games.add(new Game(t31, t25, 'R'));

				games.add(new Game(t27, t30, 'R'));
				games.add(new Game(t31, t27, 'R'));

				games.add(new Game(t29, t28, 'R'));
				games.add(new Game(t28, t30, 'R'));

				games.add(new Game(t30, t29, 'R'));

				games.add(new Game(t30, t31, 'R'));

				Collections.shuffle(games);

				RegularSeason reg = new RegularSeason(nhl, games, 2020 + i);
				reg.simSeason();
				Playoffs playoffs = new Playoffs(nhl, 'R');
				playoffs.simPlayoffs();
				s += nhl.conferenceStandings() + "\n";
				s += playoffs.toString() + "\n";
			}
			return s;
	}

	public static void main(String[] args) {
		
//		League nhl = new League("NHL");
//		Conference c1 = new Conference("East", nhl, 8, 3, 'n');
//		Conference c2 = new Conference("West", nhl, 8, 3, 'n');
//		Division d1 = new Division("Atlantic", c1);
//		Division d2 = new Division("Metroplolitan", c1);
//		Division d3 = new Division("Central", c2);
//		Division d4 = new Division("Pacific", c2);
//		Team t1 = new Team("Boston", "Bruins", "BOS", d1, orderStatistic(9));
//		Team t2 = new Team("Buffalo", "Sabres", "BUF", d1, orderStatistic(22));
//		Team t3 = new Team("New Jersey", "Devils", "NJ", d2, orderStatistic(26));
//		Team t4 = new Team("New York", "Islanders", "NYI", d2, orderStatistic(7));
//		Team t5 = new Team("New York", "Rangers", "NYR", d2, orderStatistic(19));
//		Team t6 = new Team("Philadelphia", "Flyers", "PHI", d2, orderStatistic(5));
//		Team t7 = new Team("Pittsburgh", "Penguins", "PIT", d2, orderStatistic(13));
//		Team t8 = new Team("Washington", "Capitals", "WSH", d2, orderStatistic(10));
//		Team t9 = new Team("Carolina", "Hurricanes", "CAR", d2, orderStatistic(8));
//		Team t10 = new Team("Columbus", "Blue Jackets", "CBJ", d2, orderStatistic(16));
//		Team t11 = new Team("Detroit", "Red Wings", "DET", d1, orderStatistic(31));
//		Team t12 = new Team("Chicago", "Blackhawks", "CHI", d3, orderStatistic(30));
//		Team t13 = new Team("Florida", "Panthers", "FLA", d1, orderStatistic(23));
//		Team t14 = new Team("Dallas", "Stars", "DAL", d3, orderStatistic(18));
//		Team t15 = new Team("Nashville", "Predators", "NSH", d3, orderStatistic(20));
//		Team t16 = new Team("Tampa Bay", "Lightning", "TB", d1, orderStatistic(2));
//		Team t17 = new Team("Anaheim", "Ducks", "ANA", d4, orderStatistic(27));
//		Team t18 = new Team("Arizona", "Coyotes", "ARI", d3, orderStatistic(28));
//		Team t19 = new Team("Colorado", "Avalanche", "COL", d3, orderStatistic(1));
//		Team t20 = new Team("Los Angeles", "Kings", "LA", d4, orderStatistic(24));
//		Team t21 = new Team("Minnesota", "Wild", "MIN", d3, orderStatistic(21));
//		Team t22 = new Team("San Jose", "Sharks", "SJ", d4, orderStatistic(29));
//		Team t23 = new Team("St. Louis", "Blues", "STL", d3, orderStatistic(3));
//		Team t24 = new Team("Vegas", "Golden Knights", "VGK", d4, orderStatistic(4));
//		Team t25 = new Team("Calgary", "Flames", "CGY", d4, orderStatistic(12));
//		Team t26 = new Team("Edmonton", "Oilers", "EDM", d4, orderStatistic(11));
//		Team t27 = new Team("Montreal", "Canadiens", "MTL", d1, orderStatistic(14));
//		Team t28 = new Team("Ottawa", "Senators", "OTT", d1, orderStatistic(25));
//		Team t29 = new Team("Toronto", "Maple Leafs", "TOR", d1, orderStatistic(6));
//		Team t30 = new Team("Vancouver", "Canucks", "VAN", d4, orderStatistic(15));
//		Team t31 = new Team("Winnipeg", "Jets", "WPG", d3, orderStatistic(17));
//		Team t32 = new Team("Seattle", "Kraken", "SEA", d4, new Random().nextGaussian() * 750 + 6250);
//		
//		ArrayList<Game> games = scheduleGenerator1(nhl);
//		RegularSeason reg = new RegularSeason(nhl, games, 2020 + 1);
//		reg.simSeason();
//		Playoffs playoffs = new Playoffs(nhl, 'R');
//		System.out.println(nhl.divisionalStandings());
//		playoffs.simPlayoffs();

		try {
			PrintWriter out = new PrintWriter("src/sim/nhlSim.txt");
			out.println(sim2021Season(1));
			out.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
