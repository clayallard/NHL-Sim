package sim;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.NoSuchElementException;
import java.util.PriorityQueue;
import java.util.Random;

public class League implements Group {

	private ArrayList<Conference> confs;
	private String name;
	private ArrayList<Team> teams;
	private int size;
	private boolean seasonInSession;
	private RegularSeason season;

	public League(String name) {
		this.name = name;
		teams = new ArrayList<>();
		confs = new ArrayList<>();
	}

	/**
	 * Remove team from league. Cannot remove team while season is in session.
	 * 
	 * @param team - team to remove.
	 * @return true if team was able to be removed, false otherwise.
	 */
	public boolean removeTeam(Team team) {
		return team.division().removeTeam(team);
	}

	/**
	 * Tells whether there are 1 or 2 conferences.
	 */
	public int conferenceAmount() {
		return confs.size();
	}

	/**
	 * amount of teams that make the playoffs in the entire league.
	 */
	public int amountofPlayoffTeams() {
		int amount = 0;
		for (Conference conf : confs) {
			amount += conf.amountOfPlayoffTeams();
		}
		return amount;
	}

	/**
	 * Removes team from the league.
	 * 
	 * @param team - team to remove.
	 */
	protected void removeTeamLeague(Team team) {
		size--;
		teams.remove(team);
	}

	/**
	 * Removes division from league if the season is not in session. Removes all the
	 * teams in each of the divisions as well.
	 * 
	 * @param div - division to delete.
	 * @return true if the division was successfully deleted, false otherwise.
	 */
	public boolean removeDivision(Division div) {
		return div.conference().removeDivision(div);
	}

	/**
	 * Set conference to either be the first or the second. If the number is not 1
	 * or 2, nothing will happen.
	 * 
	 * @param conf - the conference.
	 */
	protected void setConference(Conference conf) {
		confs.add(conf);
		for (Team t : conf.teams()) {
			addTeam(t);
		}
	}

	/**
	 * Starts the season along with the schedule.
	 * 
	 * @param regSeason - the regular season that the league contains.
	 */
	protected void startSeason(RegularSeason regSeason) {
		season = regSeason;
		for (Team t : teams) {
			t.newSeason();
			t.games();
		}
		seasonInSession = true;
	}

	/**
	 * ends the season.
	 */
	protected void endSeason() {
		seasonInSession = false;
	}

	/**
	 * standings of each of the two conferences in the league.
	 */
	public String conferenceStandings() {
		String str = season.year() + "-" + (season.year() + 1) + " " + name + " Conference Standings\n";
		int amountOfChar = str.length();
		for (int i = 0; i < amountOfChar - 1; i++) {
			str += "/";
		}
		str += "\nDivision Rank\tConference Rank\tLeague Rank\tTeam\tW-L-OTL\tPoints\tWinning%\tRegulation Wins\tSeason Result"; 
		for (Conference conf : confs) {
			str += "\n\n" + conf.standings();
		}
		str += "\n";
		return str;
	}

	public int lowestPointTotal() {
		return teams.get(size - 1).points();
	}

	/**
	 * standings of each division in the league.
	 */
	public String divisionalStandings() {
		String str = season.year() + "-" + (season.year() + 1) + " " + name + " Conference Standings\n";
		int amountOfChar = str.length();
		for (int i = 0; i < amountOfChar - 1; i++) {
			str += "/";
		}
		str += "\n";
		for (Conference conf : confs) {
			str += "\n\n" + conf.divisionalStandings();
		}
		str += "\n";
		return str;
	}

	/**
	 * all the divisions in the entire league.
	 */
	public ArrayList<Division> divisions() {
		ArrayList<Division> divs = new ArrayList<>();
		for (Conference conf : confs) {
			divs.addAll(conf.divisions());
		}
		return divs;
	}

	/**
	 * all games in a String
	 */
	public String schedule() {
		return season.schedule();
	}

	/**
	 * updates standings of the entire league.
	 */
	protected void updateStandings() {
		int seed = 1;
		PriorityQueue<Team> queue = new PriorityQueue<Team>((x, y) -> y.compareTo(x));
		for (Conference conf : confs) {
			conf.updateStandings();
			queue.add(conf.leader());
		}
		while (!queue.isEmpty()) {
			Team nextTeam = queue.poll();
			teams.set(seed - 1, nextTeam);
			Conference conf = nextTeam.conference();
			if (nextTeam != conf.last()) {
				queue.add(conf.teams().get(nextTeam.conferenceSeeding()));
			}
			seed++;
		}
		tieBreakCheck(teams);
		Collections.reverse(teams);
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
		for (int i = 0; i < divisions().size(); i++) {
			arr.add(new ArrayList<>());
		}
		for (Team t : tiedTeams) {
			arr.get(divisions().indexOf(t.division())).add(t);
		}
		for (ArrayList<Team> ars : arr) {
			Collections.sort(ars, (x, y) -> x.divisionSeeding() - y.divisionSeeding());
		}
		for (int i = 0; i < tiedTeams.size(); i++) {
			ArrayList<Team> divOfTeam = arr.get(divisions().indexOf(tiedTeams.get(i).division()));
			tiedTeams.set(i, divOfTeam.remove(divOfTeam.size() - 1));
		}
	}

	/**
	 * all conferences in the league.
	 */
	public ArrayList<Conference> conferences() {
		return confs;
	}

	/**
	 * Copies teams in the teams instance variable for each conference over to make
	 * a copy of the array.
	 * 
	 * @param con - conference to take teams from.
	 * @return array with all the teams in the conference.
	 */
	private ArrayList<Team> copyTeamsOver(Conference con) {
		ArrayList<Team> confTeams = con.teams();
		ArrayList<Team> copy = new ArrayList<>();
		for (int i = 0; i < confTeams.size(); i++) {
			copy.add(confTeams.get(i));
		}
		Collections.sort(copy, (x, y) -> y.compareTo(x));
		return copy;
	}

	/**
	 * if games have been played so far this season, the season is in session which
	 * means no changes to divisions or conferences can be made until the season is
	 * over.
	 */
	public boolean seasonInSession() {
		return seasonInSession;
	}

	/**
	 * Adds every time to the league that belongs to one of its conferences.
	 * 
	 * @param team - team to add to league.
	 */
	protected void addTeam(Team team) {
		teams.add(team);
		size++;
	}

	/**
	 * list of all the teams in the league listed in order of point percentage.
	 */
	@Override
	public ArrayList<Team> teams() {
		return teams;
	}

	/**
	 * the leading team of the league.
	 */
	@Override
	public Team leader() {
		return teams.get(0);
	}

	/**
	 * standings of the league. Mostly important for draft lottery after the season.
	 */
	@Override
	public String standings() {
		String str = season.year() + "-" + (season.year() + 1) + " " + name + " Standings\n";
		int amountOfChar = str.length();
		for (int i = 0; i < amountOfChar - 1; i++) {
			str += "/";
		}
		for (int i = 0; i < size; i++) {
			str += "\n" + teams.get(i).toString();
		}
		str += "\n";
		return str;
	}

	protected void setMadePlayoffs() {
		for (Team t : teams) {
			if (t.conferenceSeeding() <= t.conference().amountOfPlayoffTeams()) {
				t.madePlayoffs();
			}
		}
	}

	/**
	 * Finds the seeding of a given team in the league. Throws
	 * NoSuchElementException if the team is not in this league.
	 */
	@Override
	public int seedingOfTeam(Team team) throws NoSuchElementException {
		int seed = teams.indexOf(team) + 1;
		if (seed == 0) {
			throw new NoSuchElementException(
					"The " + team.city() + " " + team.name() + " are not in the " + this.name + " league.");
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
	 * amount of teams in this league.
	 */
	@Override
	public int size() {
		return size;
	}

	/**
	 * name of league.
	 */
	@Override
	public String name() {
		return name;
	}

	/**
	 * the regular season.
	 */
	public RegularSeason regularSeason() {
		return season;
	}

	/**
	 * list of all the games in the season.
	 */
	public ArrayList<Game> games() {
		return season.games();
	}

	/**
	 * returns the last place team of the league.
	 */
	@Override
	public Team last() {
		return teams.get(size - 1);
	}

	private int compare(double t1, double t2) {
		if (t1 > t2) {
			return 1;
		} else if (t2 > t1) {
			return -1;
		}
		return 0;
	}

	/**
	 * calculates the current projected rankings of each team.
	 */
	protected ArrayList<Team> projRankings() {
		PriorityQueue<Team> projTeams = new PriorityQueue<>((x, y) -> compare(y.trueSkillLevel(), x.trueSkillLevel()));
		for (Team t : teams) {
			projTeams.add(t);
		}
		ArrayList<Team> rankings = new ArrayList<>();
		while (!projTeams.isEmpty()) {
			rankings.add(projTeams.poll());
		}
		return rankings;
	}

	public String projectedRankings() {
		String str = name + " Power Rankings\n";
		int amountOfChar = str.length();
		for (int i = 0; i < amountOfChar - 1; i++) {
			str += "/";
		}
		ArrayList<Team> rankings = projRankings();
		for (int i = 0; i < size; i++) {
			Team team = rankings.get(i);
			str += "\n" + (i + 1) + " <" + team.leagueSeeding() + "> " + team.city() + " " + team.name();
		}
		str += "\n";
		return str;
	}

}
