package sim;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.NoSuchElementException;
import java.util.PriorityQueue;
import java.util.Random;

public class Conference implements Group {

	private ArrayList<Division> divs;
	private ArrayList<Team> teams;
	private String name;
	private League league;
	private int amountOfPlayoffTeams;
	private int size;
	private Team winner;
	private int teamsPerDivisionPlayoffs;
	private char playoffStructure;

	/**
	 * Constructing conference which consists of divisions. Choose the amount of
	 * teams that make the playoffs. This can only be one of two possible
	 * conferences in the league. If the conference already exists, it will be
	 * overridden with this new one. If the season is in session, no new conference
	 * can be created until the season is over.
	 * 
	 * @param name                    - name of conference.
	 * @param confNumber              - either conference 1 or conference 2.
	 * @param league                  - league this conference will be apart of.
	 * @param amountOfPlayoffTeams    - amount of teams that will make the playoffs.
	 * @param playoffTeamsPerDivision - amount of teams in each division that makes
	 *                                the playoffs.
	 * @throws IllegalArgumentException - throws exception if confNumber isn't 1 or
	 *                                  2 or if the season is currently in session.
	 */
	public Conference(String name, League league, int amountOfPlayoffTeams, int playoffTeamsPerDivision)
			throws IllegalArgumentException {
		this(name, league, amountOfPlayoffTeams, playoffTeamsPerDivision, 'L');
	}

	/**
	 * Constructing conference which consists of divisions. Choose the amount of
	 * teams that make the playoffs. This can only be one of two possible
	 * conferences in the league. If the conference already exists, it will be
	 * overridden with this new one. If the season is in session, no new conference
	 * can be created until the season is over.
	 * 
	 * @param name                    - name of conference.
	 * @param confNumber              - either conference 1 or conference 2.
	 * @param league                  - league this conference will be apart of.
	 * @param amountOfPlayoffTeams    - amount of teams that will make the playoffs.
	 * @param playoffTeamsPerDivision - amount of teams in each division that makes
	 *                                the playoffs.
	 * @param playoffType             - Determines how the standings are structured.
	 *                                'L' means that the division leaders are always
	 *                                the top seeds. Then the amount of playoff
	 *                                teams per division must be one. 'D' means it
	 *                                is a divisional playoff format where playoff
	 *                                rankings are determined entirely on division
	 *                                rankings aside from the wildcards. 'P' means
	 *                                that division leaders are only guaranteed
	 *                                playoffs and nothing else. 'N' means it is
	 *                                strictly based off point totals regardless of
	 *                                division rank.
	 * @throws IllegalArgumentException - throws exception if confNumber isn't 1 or
	 *                                  2 or if the season is currently in session.
	 */
	public Conference(String name, League league, int amountOfPlayoffTeams, int playoffTeamsPerDivision,
			char standingsType) throws IllegalArgumentException {
		if (league.seasonInSession()) {
			throw new IllegalArgumentException("Cannot add or change conference while season is in session.");
		}
		if (amountOfPlayoffTeams < playoffTeamsPerDivision) {
			throw new IllegalArgumentException("There must be more playoff spots than divisional playoff spots.");
		}
		divs = new ArrayList<>();
		teams = new ArrayList<>();
		this.name = name;
		this.amountOfPlayoffTeams = amountOfPlayoffTeams;
		this.league = league;
		league.setConference(this);
		playoffStructure = toUppercase(standingsType);
		if (playoffStructure == 'L') {
			teamsPerDivisionPlayoffs = 1;
		} else {
			teamsPerDivisionPlayoffs = playoffTeamsPerDivision;
		}
	}

	/**
	 * Converts letter to uppercase.
	 * 
	 * @param letter - letter representing standings structure type.
	 * @return letter to uppercase.
	 */
	private char toUppercase(char letter) {
		String str = "" + letter;
		str = str.toUpperCase();
		return str.charAt(0);
	}

	/**
	 * Changes how the standings are formatted. 'L' means that the division leaders
	 * are always the top seeds. 'D' means it is a divisional playoff format where
	 * playoff rankings are determined entirely on division rankings aside from the
	 * wildcards. 'P' means that division leaders are only guaranteed playoffs and
	 * nothing else. 'N' means it is strictly based off point totals regardless of
	 * division rank.
	 * 
	 * @param type - type of format to change to.
	 */
	public void changeStandingsStructure(char type) {
		type = toUppercase(type);
		if (type == 'L') {
			playoffStructure = 'L';
		} else if (type == 'D') {
			playoffStructure = 'D';
		} else if (type == 'P') {
			playoffStructure = 'P';
		} else {
			playoffStructure = 'N';
		}
	}

	/**
	 * current conference standings format.
	 */
	public String currentFormat() {
		if (playoffStructure == 'L') {
			return "Division leaders are guaranteed the top positions in the conference.";
		} else if (playoffStructure == 'P') {
			return "Division leaders are only guaranteed playoffs and nothing more.";
		}
		return "Standings are based off of point totals.";
	}

	/**
	 * Change the amount of teams that can make the playoffs.
	 * 
	 * @param numberOfPlayoffTeams - new number of teams that can make the playoffs.
	 * @return number of teams to make the playoffs.
	 */
	public boolean changeNumberOfPlayoffTeams(int numberOfPlayoffTeams) throws IllegalArgumentException {
		if (league.seasonInSession()) {
			return false;
		}
		if (numberOfPlayoffTeams < teamsPerDivisionPlayoffs) {
			throw new IllegalArgumentException("There must be more playoff spots than divisional playoff spots.");
		}
		amountOfPlayoffTeams = numberOfPlayoffTeams;
		return true;
	}

	/**
	 * amount of teams in each division that are guaranteed playoffs given there is
	 * enough playoff spots.
	 */
	public int teamsPerDivisionPlayoffs() {
		return teamsPerDivisionPlayoffs;
	}

	/**
	 * league this conference is apart of.
	 */
	public League league() {
		return league;
	}

	/**
	 * Removes division from conference if season is not in session. This will also
	 * remove the teams in the division.
	 * 
	 * @param div - division to remove
	 * @return true if division can be removed, false otherwise.
	 */
	public boolean removeDivision(Division div) {
		if (league.seasonInSession()) {
			return false;
		}
		if (divs.remove(div)) {
			for (Team t : div.teams()) {
				removeTeamConference(t);
				league.removeTeamLeague(t);
			}
			return true;
		}
		return false;
	}

	/**
	 * Remove team from current conference, division, league.
	 * 
	 * @param team - team to remove.
	 * @return true if it is removed and false otherwise.
	 */
	public boolean removeTeam(Team team) {
		return team.division().removeTeam(team);
	}

	/**
	 * Remove team from conference.
	 * 
	 * @param team - team to remove.
	 */
	protected void removeTeamConference(Team team) {
		teams.remove(team);
		size--;
	}

	/**
	 * The team that won the conference in the playoffs (not necessarily the leader
	 * in the regular season).
	 */
	public Team winner() {
		return winner;
	}

	/**
	 * Adds division to this conference. Also adds each team to the conference as
	 * well.
	 * 
	 * @param div - new division in conference.
	 */
	protected void addDivision(Division div) {
		ArrayList<Team> divTeams = div.teams();
		for (Team t : divTeams) {
			addTeam(t);
		}
		divs.add(div);
	}

	/**
	 * amount of teams that will make the playoffs in this conference. All the
	 * playoff spots not reserved for division leaders are wild card spots.
	 */
	public int amountOfPlayoffTeams() {
		if (teams.size() < amountOfPlayoffTeams) {
			return teams.size();
		}
		return amountOfPlayoffTeams;
	}

	/**
	 * Adds team to conference. Every team that is in a division in this conference
	 * should also be in this conference.
	 * 
	 * @param team - team to add to conference.
	 */
	protected void addTeam(Team team) {
		teams.add(team);
		size++;
		league.addTeam(team);
	}

	/**
	 * teams in this conference in order of their ranking.
	 */
	@Override
	public ArrayList<Team> teams() {
		return teams;
	}

	/**
	 * leader of conference.
	 */
	@Override
	public Team leader() {
		return teams.get(0);
	}

	/**
	 * If there is a tie between teams, first it will check the head to head
	 * matchups between tied teams. If teams are still tied, this shuffles the teams
	 * randomly.
	 * 
	 * @param teams - all teams in the division.
	 */
	private void tieBreakCheck(ArrayList<Team> teams) {
		Collections.sort(teams, (x, y) -> x.compareTo(y));
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
					sameDivisionCheck(tiedTeams);
				}
				for (int j = 0; j < numberOfTies; j++) {
					teams.set(i + j, tiedTeams.get(j));
				}
				i += numberOfTies - 1;
			}
		}
	}

	/**
	 * current regular season.
	 */
	public RegularSeason regularSeason() {
		return league().regularSeason();
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
				sameDivisionCheck(subTies);
				for (int j = 0; j < numberOfTies; j++) {
					tiedTeams.set(i + j, subTies.get(j));
				}
				i += numberOfTies - 1;
			}
		}
	}

	private void sameDivisionCheck(ArrayList<Team> tiedTeams) {
		ArrayList<ArrayList<Team>> arr = new ArrayList<>();
		for (int i = 0; i < divs.size(); i++) {
			arr.add(new ArrayList<>());
		}
		for (Team t : tiedTeams) {
			arr.get(divs.indexOf(t.division())).add(t);
		}
		for (ArrayList<Team> ars : arr) {
			Collections.sort(ars, (x, y) -> x.divisionSeeding() - y.divisionSeeding());
		}
		for (int i = 0; i < tiedTeams.size(); i++) {
			ArrayList<Team> divOfTeam = arr.get(divs.indexOf(tiedTeams.get(i).division()));
			tiedTeams.set(i, divOfTeam.remove(divOfTeam.size() - 1));
		}
	}

	/**
	 * updates standings so that the division leaders always take up the top
	 * positions.
	 */
	private void updateStandingsDivAlwaysLeader() {
		ArrayList<Team> divLeaders = new ArrayList<>();
		ArrayList<Team> otherTeams = new ArrayList<>();
		for (Division div : divs) {
			ArrayList<Team> thisDiv = div.teams();
			// account for if division is small.
			int min = 1;
			for (int i = 0; i < min; i++) {
				divLeaders.add(thisDiv.get(i));
			}
			for (int i = min; i < thisDiv.size(); i++) {
				otherTeams.add(thisDiv.get(i));
			}
		}
		tieBreakCheck(divLeaders);
		tieBreakCheck(otherTeams);
		int seed = 1;
		while (!divLeaders.isEmpty()) {
			teams.set(seed - 1, divLeaders.remove(divLeaders.size() - 1));
			seed++;
		}
		while (!otherTeams.isEmpty()) {
			teams.set(seed - 1, otherTeams.remove(otherTeams.size() - 1));
			seed++;
		}
	}

	/**
	 * conference seeding is dependent on division ranking. All non-playoff teams
	 * are just ranked off point totals.
	 */
	private void updateStandingsDependentOnDivisionRanking() {
		int seed = 1;
		for (int i = 0; i < teamsPerDivisionPlayoffs; i++) {
			ArrayList<Team> arr = new ArrayList<>();
			for (Division div : divs) {
				if (i < div.size()) {
					arr.add(div.seedingOfTeam(i + 1));
				} else {
					break;
				}
			}
			tieBreakCheck(arr);
			while (!arr.isEmpty()) {
				teams.set(seed - 1, arr.remove(arr.size() - 1));
				seed++;
			}
		}
		ArrayList<Team> remainingTeams = new ArrayList<>();
		for (Division div : divs) {
			for (int i = div.size() - 1; i >= teamsPerDivisionPlayoffs; i--) {
				remainingTeams.add(div.seedingOfTeam(i + 1));
			}
		}
		tieBreakCheck(remainingTeams);
		while (!remainingTeams.isEmpty()) {
			teams.set(seed - 1, remainingTeams.remove(remainingTeams.size() - 1));
			seed++;
		}
	}

	/**
	 * standings are determined by overall points regardless of divison ranking.
	 */
	private void updateStandingsDivLeadersDoesNotMatter() {
		int seed = 1;
		PriorityQueue<Team> queue = new PriorityQueue<Team>((x, y) -> y.compareTo(x));
		for (Division div : divs) {
			queue.add(div.leader());
		}
		while (!queue.isEmpty()) {
			Team nextTeam = queue.poll();
			teams.set(seed - 1, nextTeam);
			Division div = nextTeam.division();
			if (nextTeam != div.last()) {
				queue.add(div.teams().get(nextTeam.divisionSeeding()));
			}
			seed++;
		}
		tieBreakCheck(teams);
		Collections.reverse(teams);
	}

	/**
	 * updates standings so that the division leaders are only guaranteed playoffs,
	 * not seeding.
	 */
	private void updateStandingsDivLeadersOnlyInPlayoffs() {
		ArrayList<Team> divLeaders = new ArrayList<>();
		ArrayList<Team> otherTeams = new ArrayList<>();
		for (Division div : divs) {
			ArrayList<Team> thisDiv = div.teams();
			// account for if division is small.
			int min = Math.min(teamsPerDivisionPlayoffs, thisDiv.size());
			for (int i = 0; i < min; i++) {
				divLeaders.add(thisDiv.get(i));
			}
			for (int i = min; i < thisDiv.size(); i++) {
				otherTeams.add(thisDiv.get(i));
			}
		}
		tieBreakCheck(divLeaders);
		tieBreakCheck(otherTeams);
		int playoffTeamNum = amountOfPlayoffTeams();
		int seed = 1;
		while (!(divLeaders.isEmpty() || otherTeams.isEmpty()) && seed <= playoffTeamNum) {
			// to ensure that all division winners get a playoff spot.
			int divLeaderSize = divLeaders.size();
			int remainingTeamsSize = otherTeams.size();
			if (divLeaderSize > playoffTeamNum - seed
					|| divLeaders.get(divLeaderSize - 1).compareTo(otherTeams.get(remainingTeamsSize - 1)) > 0) {
				teams.set(seed - 1, divLeaders.remove(divLeaderSize - 1));
			} else if (divLeaders.get(divLeaderSize - 1).compareTo(otherTeams.get(remainingTeamsSize - 1)) < 0) {
				teams.set(seed - 1, otherTeams.remove(remainingTeamsSize - 1));
			} else if (checkDivision(divLeaders, otherTeams.get(remainingTeamsSize - 1))) {
				teams.set(seed - 1, divLeaders.remove(divLeaderSize - 1));
			} else {
				if (new Random().nextBoolean()) {
					teams.set(seed - 1, divLeaders.remove(divLeaderSize - 1));
				} else {
					teams.set(seed - 1, otherTeams.remove(remainingTeamsSize - 1));
				}
			}
			seed++;
		}
		while (!divLeaders.isEmpty()) {
			teams.set(seed - 1, divLeaders.remove(divLeaders.size() - 1));
			seed++;
		}
		while (!otherTeams.isEmpty()) {
			teams.set(seed - 1, otherTeams.remove(otherTeams.size() - 1));
			seed++;
		}
	}

	/**
	 * updates the standings according to what the playoff picture would be if the
	 * season were to end at its current state. All division winners are guaranteed
	 * to make the playoffs.
	 */
	protected void updateStandings() {
		for (Division div : divs) {
			div.updateStandings();
		}
		if (playoffStructure == 'L') {
			updateStandingsDivAlwaysLeader();
		} else if (playoffStructure == 'D') {
			updateStandingsDependentOnDivisionRanking();
		} else if (playoffStructure == 'P') {
			updateStandingsDivLeadersOnlyInPlayoffs();
		} else {
			updateStandingsDivLeadersDoesNotMatter();
		}
	}

	/**
	 * all divisions in this conference.
	 */
	public ArrayList<Division> divisions() {
		return divs;
	}

	/**
	 * amount of teams that will make the playoffs in a wild card spot.
	 * 
	 * @return
	 */
	public int numberOfWildcardTeams() {
		int num = 0;
		for (Division div : divs) {
			num += Math.min(div.size(), teamsPerDivisionPlayoffs);
		}
		if (num > amountOfPlayoffTeams) {
			return 0;
		}
		return amountOfPlayoffTeams - num;
	}

	/**
	 * See if a team in a list is in the same division as another team.
	 * 
	 * @param arr  - list of teams.
	 * @param team - team to check division of.
	 * @return true if there exists a team with the same division, false otherwise.
	 */
	private boolean checkDivision(ArrayList<Team> arr, Team team) {
		for (Team t : arr) {
			if (team.division() == t.division()) {
				return true;
			}
		}
		return false;
	}

	/**
	 * standings for each division in the conference.
	 */
	public String divisionalStandings() {
		String str = regularSeason().year() + "-" + (regularSeason().year() + 1) + " " + name
				+ " Conference Divisional Standings\n";
		int amountOfChar = str.length();
		for (int i = 0; i < amountOfChar - 1; i++) {
			str += "*";
		}
		for (Division div : divs) {
			str += "\n\n" + div.standings();
		}
		return str;
	}

	/**
	 * standings for divisional playoffs.
	 */
	private String standingsDivisionalPlayoffs() {
		String str = regularSeason().year() + "-" + (regularSeason().year() + 1) + " " + name
				+ " Conference Standings\n";
		int amountOfChar = str.length();
		for (int i = 0; i < amountOfChar - 1; i++) {
			str += "*";
		}
		str += "\n";
		ArrayList<Team> divLeaders = new ArrayList<Team>();
		for (Division div : divs) {
			divLeaders.add(div.leader());
		}
		Collections.sort(divLeaders, (x, y) -> y.conferenceSeeding() - x.conferenceSeeding());
		while (!divLeaders.isEmpty()) {
			Division div = divLeaders.remove(divLeaders.size() - 1).division();
			str += "\n" + div.name() + " Division";
			for (int i = 0; i < div.amountOfPlayoffTeams(); i++) {
				str += "\n{" + (i + 1) + "}   " + div.teams().get(i).toString();
			}
			str += "\n";
		}
		int count = 0;
		for (int i = 0; i < teams.size(); i++) {
			Team team = teams.get(i);
			if (team.divisionSeeding() > teamsPerDivisionPlayoffs) {
				str += "\n" + team.toString();
				count++;
				if (count == numberOfWildcardTeams()) {
					str += "\n------------------------------------------------------------------------------------------";
				}
			}
		}
		return str;
	}

	/**
	 * standings of the conference.
	 */
	@Override
	public String standings() {
		if (playoffStructure == 'D') {
			return standingsDivisionalPlayoffs();
		}
		String str = regularSeason().year() + "-" + (regularSeason().year() + 1) + " " + name
				+ " Conference Standings\n";
		int amountOfChar = str.length();
		for (int i = 0; i < amountOfChar - 1; i++) {
			str += "*";
		}
		for (int i = 0; i < amountOfPlayoffTeams(); i++) {
			str += "\n" + teams.get(i).toString();
		}
		str += "\n------------------------------------------------------------------------------------------";
		for (int i = amountOfPlayoffTeams(); i < size; i++) {
			str += "\n" + teams.get(i).toString();
		}
		return str;
	}

	/**
	 * Finds the seeding of a given team in the conference. Throws
	 * NoSuchElementException if the team is not in this conference.
	 */
	@Override
	public int seedingOfTeam(Team team) throws NoSuchElementException {
		int seed = teams.indexOf(team) + 1;
		if (seed == 0) {
			throw new NoSuchElementException(
					"The " + team.city() + " " + team.name() + " are not in the " + this.name + " conference.");
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
	 * amount of teams in the conference.
	 */
	@Override
	public int size() {
		return size;
	}

	/**
	 * name of conference.
	 */
	@Override
	public String name() {
		return name;
	}

	/**
	 * returns the last place team of the conference.
	 */
	@Override
	public Team last() {
		return teams.get(size - 1);
	}

	/**
	 * hashcode of a conference.
	 */
	public int hashCode() {
		return name.hashCode();
	}

}
