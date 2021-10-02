package sim;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.NoSuchElementException;

public class Division implements Group {

	private ArrayList<Team> teams;
	private Conference conf;
	private String name;
	private int size;

	/**
	 * Constructing new division in some conference.
	 * 
	 * @param name - name of division.
	 * @param conf - conference this division belongs to.
	 */
	public Division(String name, Conference conf) {
		this.name = name;
		this.conf = conf;
		teams = new ArrayList<>();
		conf.addDivision(this);
	}

	/**
	 * Removes team from division only if the season is not in session.
	 * 
	 * @param team - team to remove.
	 * @return true if team can be removed, false otherwise.
	 */
	protected boolean removeTeam(Team team) {
		if (league().seasonInSession()) {
			return false;
		}
		if (teams.remove(team)) {
			conf.removeTeamConference(team);
			league().removeTeamLeague(team);
			size--;
			return true;
		}
		return false;
	}

	/**
	 * teams in this division in order of their standings.
	 */
	@Override
	public ArrayList<Team> teams() {
		return teams;
	}

	/**
	 * adds new team to division. If the team is already in the league, it will move
	 * to this division and be removed from the other division.
	 * 
	 * @param team - team added to division.
	 */
	public boolean addTeam(Team team) {
		team.division().removeTeam(team);
		teams.add(team);
		size++;
		conf.addTeam(team);
		return true;
	}

	/**
	 * leader of division.
	 */
	@Override
	public Team leader() {
		return teams.get(0);
	}

	/**
	 * updates standings by ordering the list of teams according to their ranking.
	 */
	protected void updateStandings() {
		tieBreakCheck(teams);
	}

	/**
	 * current regular season.
	 */
	public RegularSeason regularSeason() {
		return league().regularSeason();
	}

	/**
	 * If there is a tie between teams, first it will check the head to head
	 * matchups between tied teams. If teams are still tied, this shuffles the teams
	 * randomly.
	 * 
	 * @param teams - all teams in the division.
	 */
	private void tieBreakCheck(ArrayList<Team> teams) {
		Collections.sort(teams, (x, y) -> y.compareTo(x));
		for (int i = 0; i < teams.size() - 1; i++) {
			if (teams.get(i).compareTo(teams.get(i + 1)) == 0) {
				int numberOfTies = 2;
				while (i + numberOfTies < teams.size() && teams.get(i).compareTo(teams.get(i + numberOfTies)) == 0) {
					numberOfTies++;
				}
				ArrayList<Team> tiedTeams = new ArrayList<>();
				for (int j = 0; j < numberOfTies; j++) {
					tiedTeams.add(teams.get(i + j));
				}
				if (regularSeason() != null) {
					trueTieBreakCheck(tiedTeams);
				} else {
					Collections.shuffle(tiedTeams);
				}
				for (int j = 0; j < numberOfTies; j++) {
					teams.set(i + j, tiedTeams.get(j));
				}
				i += numberOfTies - 1;
			}
		}
	}

	/**
	 * Checks if two arrays are equal to eachother.
	 * 
	 * @param arr1 - first array.
	 * @param arr2 - second array.
	 * @return true if they are equal false otherwise.
	 */
	private boolean arrayEquals(double[] arr1, double[] arr2) {
		if (arr1[0] == arr2[0] && arr1[1] == arr2[1]) {
			return true;
		}
		return false;
	}

	/**
	 * If teams are still tied after the tie breaker, this will break the tie by
	 * choosing randomly.
	 * 
	 * @param tiedTeams - teams that are tied beyond the tie breaker.
	 */
	private void trueTieBreakCheck(ArrayList<Team> tiedTeams) {
		HashMap<Team, double[]> map = regularSeason().headToHead(tiedTeams);
		regularSeason().pointTieCheck(map, tiedTeams);
		for (int i = 0; i < tiedTeams.size() - 1; i++) {
			if (arrayEquals(map.get(tiedTeams.get(i)), map.get(tiedTeams.get(i + 1)))) {
				int numberOfTies = 2;
				while (i + numberOfTies < tiedTeams.size()
						&& arrayEquals(map.get(tiedTeams.get(i)), map.get(tiedTeams.get(i + numberOfTies)))) {
					numberOfTies++;
				}
				ArrayList<Team> subTies = new ArrayList<>();
				for (int j = 0; j < numberOfTies; j++) {
					subTies.add(tiedTeams.get(i + j));
				}
				// no more tiebreaker so now it is just random.
				Collections.shuffle(subTies);
				for (int j = 0; j < numberOfTies; j++) {
					tiedTeams.set(i + j, subTies.get(j));
				}
				i += numberOfTies - 1;
			}
		}
	}

	/**
	 * amount of playoff teams for this division.
	 */
	public int amountOfPlayoffTeams() {
		return Math.min(conference().teamsPerDivisionPlayoffs(), size);
	}

	/**
	 * standings of the division.
	 */
	@Override
	public String standings() {
		String str = regularSeason().year() + "-" + (regularSeason().year() + 1) + " " + name + " Division\n";
		int amountOfChar = str.length();
		for (int i = 0; i < amountOfChar - 1; i++) {
			str += "^";
		}
		int cutoffLine = conference().teamsPerDivisionPlayoffs();
		for (int i = 0; i < cutoffLine; i++) {
			str += "\n" + teams.get(i).toString();
		}
		str += "\n------------------------------------------------------------------------------------------";
		for (int i = cutoffLine; i < size; i++) {
			str += "\n" + teams.get(i).toString();
		}
		return str;
	}

	/**
	 * Conference this division belongs to.
	 */
	public Conference conference() {
		return conf;
	}

	/**
	 * returns the last place team of the division.
	 */
	@Override
	public Team last() {
		return teams.get(size - 1);
	}

	/**
	 * league this division belongs to.
	 */
	public League league() {
		return conf.league();
	}

	/**
	 * name of division.
	 */
	@Override
	public String name() {
		return name;
	}

	/**
	 * Finds the seeding of a given team in the division. Throws
	 * NoSuchElementException if the team is not in this division.
	 */
	@Override
	public int seedingOfTeam(Team team) throws NoSuchElementException {
		int seed = teams.indexOf(team) + 1;
		if (seed == 0) {
			throw new NoSuchElementException(
					"The " + team.city() + " " + team.name() + " are not in the " + this.name + " division.");
		}
		return seed;
	}

	/**
	 * Finds the team at a specified seed. Throws IndexOutOfBoundsException if it is
	 * an invalid seeding (<1 or >division size).
	 */
	@Override
	public Team seedingOfTeam(int seed) throws IndexOutOfBoundsException {
		if (seed < 1 || seed > size) {
			throw new IndexOutOfBoundsException(seed + " is not a valid seeding.");
		}
		return teams.get(seed - 1);
	}

	/**
	 * amount of teams in the division.
	 */
	@Override
	public int size() {
		return size;
	}

}
