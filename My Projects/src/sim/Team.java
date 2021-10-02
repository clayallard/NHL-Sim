package sim;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

public class Team implements Comparable<Team> {

	private double skillLevel;
	public double preskillLevel;
	private Division division;
	private String city;
	private String name;
	private String abr;
	private int wins;
	private int otWins;
	private int otLosses;
	private int losses;
	private double healthRating;
	private boolean playoffs;
	private int result;
	private ArrayList<Game> schedule;
	private ArrayList<Double> healthList = new ArrayList<>();
	private ArrayList<Double> eloList = new ArrayList<>();;

	final static double mean = 6250;
	final static double std = 750;

	/**
	 * Making new team without having any knowledge how good the team is. The skill
	 * of the team will be randomly chosen to the extent that they have just as much
	 * of a chance of being good or bad based solely on random chance.
	 * 
	 * @param city         - city the team is from.
	 * @param name         - name of team.
	 * @param abbreviation - two or three letter abbreviation.
	 * @param div          - division this team belongs to.
	 * @throws throws exception if team name or abbreviation already exists.
	 */
	public Team(String city, String name, String abbreviation, Division div) throws IllegalArgumentException {
		this(city, name, abbreviation, div, mean + std * new Random().nextGaussian());
	}

	private static double exponential(double lambda) {
		// creating random uniform(0,1)
		Random rng = new Random();
		double randomValue = rng.nextDouble();
		// plugging random number into G(u) = -lambda * ln(1 - u).
		return -lambda * Math.log(1 - randomValue);
	}

	private void proposedChanged(double val) {
		Random rng = new Random();
		double par = val * rng.nextDouble();
		double propChange = Math.sqrt(std) * exponential(par);
		if (rng.nextBoolean()) {
			propChange = -propChange;
		}
		propChange += skillLevel;
		double newLikelihood = normalPDF(propChange);
		double currentLikelihood = normalPDF(skillLevel);
		double compLikelihood = newLikelihood / currentLikelihood;
		double probOfChange = Math.min(compLikelihood, 1);
		if (rng.nextDouble() < probOfChange) {
			skillLevel = propChange;
		}
	}

	private double normalPDF(double val) {
		return Math.exp(-Math.pow((val - mean) / std, 2) / 2) / (Math.pow(2 * Math.PI, 1 / 2) * std);
	}

	protected void changeSkill() {
		Random rng = new Random();
		int val = rng.nextInt(160);
		if (val < 3) {
			proposedChanged(16);
		} else if (val < 9) {
			proposedChanged(8);
		} else if (val < 27) {
			proposedChanged(4);
		} else if (val < 81) {
			proposedChanged(2);
		} else {
			proposedChanged(1);
		}
		proposedHealthChange();
		if (!league().regularSeason().seasonOver() && league().regularSeason().seasonStarted()) {
			healthList.add(healthRating);
			eloList.add(skillLevel);
		}
	}

	protected void offSeasonChangeSkill() {
		Random rng = new Random();
		int val = rng.nextInt(200);
		if (val < 9) {
			proposedChanged(8);
		} else if (val < 27) {
			proposedChanged(4);
		} else if (val < 81) {
			proposedChanged(2);
		} else {
			proposedChanged(1);
		}
		proposedHealthChange();
	}

	private double valForOffseason() {
		Random rng = new Random();
		if (rng.nextInt(3) == 0) {
			return -.25 + 2 * rng.nextDouble();
		} else {
			return -.0025 + .02 * rng.nextDouble();
		}
	}

	protected void preSkillLevel() {
		preskillLevel = skillLevel;
	}

	private void proposedHealthChange() {
		Random rng = new Random();
		double val;
		if (!league().regularSeason().seasonStarted()) {
			val = valForOffseason();
		} else if (rng.nextInt(3) == 0) {
			val = -1 + 2 * rng.nextDouble();
		} else {
			val = -.01 + .02 * rng.nextDouble();
		}
		val += healthRating;
		double newLikelihood = healthPDF(val);
		double currentLikelihood = healthPDF(healthRating);
		double compLikelihood = newLikelihood / currentLikelihood;
		double probOfChange = Math.min(compLikelihood, 1);
		if (rng.nextDouble() < probOfChange) {
			healthRating = val;
		}
	}

	private double healthPDF(double val) {
		if (val > 0 && val < 1) {
			return 4 * Math.pow(val, 5);
		}
		return 0;
	}

	/**
	 * Making new team and setting its skill value. For a benchmark as to what a
	 * good/bad skill value is, 5000 is about average, 4500 is really bad and 5500
	 * is really good.
	 * 
	 * @param city         - city the team is from.
	 * @param name         - name of team.
	 * @param abbreviation - (typically two or three letters) abbreviation.
	 * @param div          - division this team belongs to.
	 * @throws throws exception if team name or abbreviation already exists.
	 */
	public Team(String city, String name, String abbreviation, Division div, double skillvalue)
			throws IllegalArgumentException {
		this.city = city;
		division = div;
		ArrayList<Team> teams = division.league().teams();
		for (Team t : teams) {
			if (t.name.equals(name) && t.abr.equals(abbreviation)) {
				throw new IllegalArgumentException("A team with the name \"" + name + "\" and abbreviation \""
						+ abbreviation + "\" already exists. Give this team another name and abbreviation.");
			} else if (t.abr.equals(abbreviation)) {
				throw new IllegalArgumentException("A team with the abbreviation \"" + abbreviation
						+ "\" already exists. Give this team another abbreviation.");
			} else if (t.name.equals(name)) {
				throw new IllegalArgumentException(
						"A team with the name \"" + name + "\" already exists. Give this team another name.");
			}
		}
		this.name = name;
		abr = abbreviation;
		division.addTeam(this);
		skillLevel = skillvalue;
		preskillLevel = skillvalue;
		healthRating = 1;
		result = 0;
	}

	/**
	 * set the skill level to whatever you would like.
	 * 
	 * @param skill - desired skill level.
	 */
	public void setSkill(double skill) {
		skillLevel = skill;
	}

	/**
	 * sset skill level to be completely random.
	 */
	public void resetSkill() {
		skillLevel = mean + std * new Random().nextGaussian();
	}

	/**
	 * city this team plays for.
	 */
	public String city() {
		return city;
	}

	/**
	 * change city.
	 * 
	 * @param newCity - the new city this team plays for.
	 */
	public void changeCity(String newCity) {
		city = newCity;
	}

	/**
	 * name of this team
	 */
	public String name() {
		return name;
	}

	/**
	 * change name.
	 * 
	 * @param newName - new name for this team.
	 */
	public void changeName(String newName) {
		name = newName;
	}

	/**
	 * this team's abbreviation. Usually shows up when displaying stats or
	 * standings.
	 */
	public String abbreviation() {
		return abr;
	}

	/**
	 * change abbreviation.
	 * 
	 * @param newAbr - new abbreviation.
	 */
	public void changeAbbreviation(String newAbr) {
		abr = newAbr;
	}

	/**
	 * total number of wins as shown in the standings (including overtime wins).
	 */
	public int wins() {
		return wins + otWins;
	}

	/**
	 * number of regulation wins.
	 */
	public int regulationWins() {
		return wins;
	}

	/**
	 * number of losses as shown in the standings (not including overtime losses).
	 */
	public int losses() {
		return losses;
	}

	/**
	 * total number of losses including overtime losses.
	 */
	public int totalLosses() {
		return losses + otLosses;
	}

	/**
	 * number of losses in overtime.
	 */
	public int overtimeLosses() {
		return otLosses;
	}

	/**
	 * number of wins in overtime.
	 */
	public int overtimeWins() {
		return otWins;
	}

	/**
	 * total amount of points this team has this season. 2 points for each win
	 * (regardless of overtime or regulation) and 1 point for an overtime loss.
	 */
	public int points() {
		return 2 * wins() + otLosses;
	}

	/**
	 * amount of games played this season so far.
	 */
	public int gamesPlayed() {
		return wins + losses + otWins + otLosses;
	}

	/**
	 * proportion of points acheived to amount of points possible.
	 */
	public double pointsPercentage() {
		if (gamesPlayed() == 0) {
			return 0.5;
		}
		double val = (double) points() / (2 * gamesPlayed());
		String str = String.format("%.3f %n", val);
		return Double.parseDouble(str);
	}

	/**
	 * how healthy this team currently is. 1 means perfect health. The lower the
	 * value, the worse.
	 */
	public double healthRating() {
		return healthRating;
	}

	/**
	 * this team made the playoffs.
	 */
	protected void madePlayoffs() {
		playoffs = true;
	}

	/**
	 * determines whether this team made the playoffs. This will never be true until
	 * the regular season ends.
	 */
	public boolean playoffs() {
		return playoffs;
	}

	/**
	 * Checks if two teams are the exact same.
	 * 
	 * @param t - team being compared to.
	 * @return true if they are the same and false otherwise.
	 */
	public boolean equals(Team t) {
		return this == t;
	}

	/**
	 * this team's record this season.
	 */
	public String record() {
		return wins() + "-" + losses + "-" + otLosses;
	}

	/**
	 * z means that the team clinched the top seed in the conference. y means the
	 * team clinched the top position in the division. x means the team clinched the
	 * playoffs. out means the team has no chance to make the playoffs. Otherwise,
	 * the outcome has not yet been determined.
	 */
	private String playoffStatusForStandings() {
		if (outOfPlayoffs()) {
			return " out";
		} else if (clinchedPresidentsTrophy()) {
			return "-z*";
		} else if (clinchedConference()) {
			return "-z";
		} else if (clinchedDivision()) {
			return "-y";
		} else if (clinchedPlayoffs()) {
			return "-x";
		}
		return "";
	}

	/**
	 * this team converted into string displaying its stats. Conference ranking,
	 * city, name, record, points, points percentage, regulation wins, divisional
	 * ranking, league ranking.
	 */
	public String toString() {
		return "[" + conference().seedingOfTeam(this) + "]\t(" + division.seedingOfTeam(this) + ")\t<"
				+ league().seedingOfTeam(this) + ">\t" + city + " " + name + playoffStatusForStandings() + "\t"
				+ record() + "\t" + points() + "\t" + pointsPercentage() + "\t" + wins + "\t" + endOfSeason()
				/*+ "\t" + preskillLevel + "\t" + skillLevel + "\t" + healthRating + "\t" + average(eloList) + "\t"
				+ average(healthList) + "    " + "    " + awayRecord() + "   " + awayPoints() + "   " + homeRecord()
				+ "   " + homePoints() + "   " + teamAgainstRating() + "   " + teamAgainstSeeding()*/;
	}

	private double average(ArrayList<Double> arr) {
		double val = 0;
		for (Double d : arr) {
			val += d;
		}
		return val / arr.size();
	}

	/**
	 * Returns opponent win to loss rating.
	 */
	public double teamAgainstRating() {
		double rating = 0;
		for (Game game : schedule) {
			if (game.winner() == this) {
				if (game.ot()) {
					rating += Math.pow((double) game.loser().points() / 2.0, 2);
				} else {
					rating += Math.pow(game.loser().points(), 2);
				}
			} else {
				if (game.loser() == this) {
					if (game.ot()) {
						rating += Math.pow((double) game.winner().points() / 4.0, 2);
					}
				}
			}
		}
		return rating;
	}

	/**
	 * Returns opponent win to loss rating.
	 */
	public double teamAgainstSeeding() {
		double rating = 0;
		for (Game game : schedule) {
			if (game.winner() == this) {
				if (game.ot()) {
					rating += Math.pow((double) (league().size() + 1 - game.loser().leagueSeeding()) / 2.0, 2);
				} else {
					rating += Math.pow((league().size() + 1 - game.loser().leagueSeeding()), 2);
				}
			} else {
				if (game.loser() == this) {
					if (game.ot()) {
						rating += Math.pow((double) (league().size() + 1 - game.winner().leagueSeeding()) / 4.0, 2);
					}
				}
			}
		}
		return rating;
	}

	/**
	 * Determines whether team has any chance of making the playoffs. True if they
	 * are out of contention, false otherwise.
	 */
	public boolean outOfPlayoffs() {
		if (schedule == null) {
			return false;
		}
		int playoffLineForConference = conference().amountOfPlayoffTeams();
		if (playoffLineForConference >= conferenceSeeding()) {
			return false;
		}
		if (league().regularSeason().seasonOver()) {
			if (conferenceSeeding() > playoffLineForConference) {
				return true;
			}
			return false;
		}
		int mostPossiblePoints = points() + 2 * (gamesRemaining());
		int countTeams = 0;
		for (Team t : conference().teams()) {
			if (t.points() > mostPossiblePoints && t != t.division().leader()) {
				countTeams++;
			}
		}
		Team divLeader = division.leader();
		// needs to be below the standard for divisional spot and wild card spot.
		if (mostPossiblePoints < divLeader.points()
				&& countTeams >= playoffLineForConference - conference().divisions().size()) {
			return true;
		}
		return false;
	}

	/**
	 * True if this team is guaranteed to win the conference. False otherwise.
	 */
	public boolean clinchedConference() {
		if (!this.equals(conference().leader())) {
			return false;
		}
		if (conference().size() < 2) {
			return true;
		}
		if (schedule == null) {
			return false;
		}
		if (league().regularSeason().seasonOver()) {
			if (conferenceSeeding() == 1) {
				return true;
			}
			return false;
		}
		for (Team t : conference().teams()) {
			if (this != t && t.points() + 2 * t.gamesRemaining() > points()) {
				return false;
			}
		}
		return true;
	}

	public boolean clinchedPresidentsTrophy() {
		if (!this.equals(league().leader())) {
			return false;
		}
		if (league().size() < 2) {
			return true;
		}
		if (schedule == null) {
			return false;
		}
		if (league().regularSeason().seasonOver()) {
			if (leagueSeeding() == 1) {
				return true;
			}
			return false;
		}
		for (Team t : league().teams()) {
			if (this != t && t.points() + 2 * t.gamesRemaining() > points()) {
				return false;
			}
		}
		return true;
	}

	/**
	 * True if this team is guaranteed to win the division. False otherwise.
	 */
	public boolean clinchedDivision() {
		if (!this.equals(division.leader())) {
			return false;
		}
		if (division.size() < 2) {
			return true;
		}
		if (schedule == null) {
			return false;
		}
		if (league().regularSeason().seasonOver()) {
			if (divisionSeeding() == 1) {
				return true;
			}
			return false;
		}
		for (Team t : division().teams()) {
			if (this != t && t.points() + 2 * t.gamesRemaining() > points()) {
				return false;
			}
		}
		return true;
	}

	/**
	 * True if this team is guaranteed to make the playoffs. False otherwise.
	 */
	public boolean clinchedPlayoffs() {
		int playoffLine = conference().amountOfPlayoffTeams();
		if (playoffLine >= conference().size()) {
			return true;
		}
		if (schedule == null) {
			return false;
		}
		if (playoffLine < conferenceSeeding()) {
			return false;
		}
		if (league().regularSeason().seasonOver()) {
			if (conferenceSeeding() <= playoffLine) {
				return true;
			}
			return false;
		}
		int count = 0;
		for (Team t : conference().teams()) {
			if (t.points() + 2 * t.gamesRemaining() < points() && t != t.division().leader()) {
				count++;
			}
		}
		if (count >= conference().size() - playoffLine) {
			return true;
		}
		return false;
	}

	/**
	 * division this team belongs to.
	 */
	public Division division() {
		return division;
	}

	/**
	 * Change division of this team. This cannot be changed while the season is in
	 * session.
	 * 
	 * @param div - new division to go to.
	 * @return true if change was successful, false if not.
	 */
	public boolean changeDivision(Division div) {
		if (league().seasonInSession()) {
			return false;
		}
		if (div.addTeam(this)) {
			division = div;
			return true;
		}
		return false;
	}

	/**
	 * conference this team belongs to.
	 */
	public Conference conference() {
		return division.conference();
	}

	/**
	 * league this team belongs to.
	 */
	public League league() {
		return conference().league();
	}

	/**
	 * Comparable used to compare one team to another team.
	 * 
	 * @param o - other team being compared to.
	 * @return positive number if this team is higher in the standings, negative if
	 *         it is lower, 0 if they are tied (in this instance, a tie would be
	 *         settled by using a random number generator.)
	 */
	@Override
	public int compareTo(Team o) {
		if (this.points() == o.points()) {
			if (this.compPointsPercentage(o) == 0) {
				if (this.wins == o.wins) {
					return this.otWins - o.otWins;
				}
				return this.wins - o.wins;
			}
			return this.compPointsPercentage(o);
		}
		return this.points() - o.points();
	}

	private int compPointsPercentage(Team o) {
		double pointDifference = this.pointsPercentage() - o.pointsPercentage();
		if (pointDifference > 0) {
			return 1;
		} else if (pointDifference < 0) {
			return -1;
		} else {
			return 0;
		}
	}

	/**
	 * Set team's final result. The amount of rounds played. -1 means missed the
	 * playoffs, amount of round + 1 would mean they won.
	 * 
	 * @param res - result of team this season.
	 */
	protected void setSeasonResult(int res) {
		result = res;
	}

	/**
	 * increments result by 1.
	 */
	protected void incrementResult() {
		result++;
	}

	/**
	 * final result of season. The farthest this team got this year.
	 */
	public String endOfSeason() {
		switch (result) {
		case -1:
			return "Missed Playoffs";
		case 1:
			return "Round 1";
		case 2:
			return "Round 2";
		case 3:
			return "Conference Final";
		case 4:
			return "Stanley Cup Final";
		case 5:
			return "Stanley Cup Champions";
		}
		return "Still Competing";
	}

	/**
	 * resets to a new season where all stats are back to 0.
	 */
	protected void newSeason() {
		wins = 0;
		losses = 0;
		otWins = 0;
		otLosses = 0;
		playoffs = false;
		result = 0;
		healthList.clear();
		eloList.clear();
	}

	/**
	 * the seeding this team is in their division.
	 */
	public int divisionSeeding() {
		return division.seedingOfTeam(this);
	}

	/**
	 * the seeding this team is in their conference.
	 */
	public int conferenceSeeding() {
		return conference().seedingOfTeam(this);
	}

	/**
	 * the seeding this team is in their league.
	 */
	public int leagueSeeding() {
		return league().seedingOfTeam(this);
	}

	/**
	 * Returns the result to determine whether this team won or lost.
	 * 
	 * @param result - integer representing the result of the game.
	 */
	protected void gameResult(int result) {
		if (result == 3) {
			wins++;
		} else if (result == 2) {
			otWins++;
		} else if (result == 1) {
			otLosses++;
		} else {
			losses++;
		}
	}

	/**
	 * skill level of team.
	 */
	protected double skillLevel() {
		double percentile = Gaussian.cdf((skillLevel - mean) / std) * healthRating;
		return mean + std * Gaussian.inverseCDF(percentile);
	}

	/**
	 * skill level of team ignoring health and injury issues.
	 */
	protected double trueSkillLevel() {
		return skillLevel;
	}

	/**
	 * all regular season games for this team this season.
	 */
	public ArrayList<Game> games() {
		schedule = regularSeason().gamesForTeam(this);
		return schedule;
	}

	public String schedule() {
		String str = regularSeason().year() + "-" + (regularSeason().year() + 1) + " Schedule (" + city + " " + name
				+ ")\n";
		int amountOfChar = str.length();
		for (int i = 0; i < amountOfChar - 1; i++) {
			str += "_";
		}
		str += "\n";
		for (int i = 0; i < schedule.size(); i++) {
			str += "\n" + schedule.get(i).toString();
		}
		return str;
	}

	/**
	 * the hashcode for this team.
	 */
	public int hashcode() {
		return name.hashCode();
	}

	/**
	 * all regular season home games for this team.
	 */
	public ArrayList<Game> homeGames() {
		if (schedule == null) {
			games();
		}
		ArrayList<Game> homeGames = new ArrayList<>();
		for (Game g : schedule) {
			if (this == g.home()) {
				homeGames.add(g);
			}
		}
		return homeGames;
	}

	/**
	 * Record of all games at home.
	 */
	public String homeRecord() {
		int w = 0;
		int l = 0;
		int o = 0;
		for (Game game : homeGames()) {
			if (game.winner() == this) {
				w++;
			} else if (game.loser() == this) {
				if (game.ot()) {
					o++;
				} else {
					l++;
				}
			}
		}
		return w + "-" + l + "-" + o;
	}

	/**
	 * Amount of points at home.
	 */
	public int homePoints() {
		int p = 0;
		for (Game game : homeGames()) {
			if (game.winner() == this) {
				p += 2;
			} else if (game.loser() == this) {
				if (game.ot()) {
					p++;
				}
			}
		}
		return p;
	}

	/**
	 * Amount of points away.
	 */
	public int awayPoints() {
		int p = 0;
		for (Game game : awayGames()) {
			if (game.winner() == this) {
				p += 2;
			} else if (game.loser() == this) {
				if (game.ot()) {
					p++;
				}
			}
		}
		return p;
	}

	/**
	 * Record of all games away.
	 */
	public String awayRecord() {
		int w = 0;
		int l = 0;
		int o = 0;
		for (Game game : awayGames()) {
			if (game.winner() == this) {
				w++;
			} else if (game.loser() == this) {
				if (game.ot()) {
					o++;
				} else {
					l++;
				}
			}
		}
		return w + "-" + l + "-" + o;
	}

	/**
	 * all regular season away games for this team.
	 */
	public ArrayList<Game> awayGames() {
		if (schedule == null) {
			games();
		}
		ArrayList<Game> awayGames = new ArrayList<>();
		for (Game g : schedule) {
			if (this == g.away()) {
				awayGames.add(g);
			}
		}
		return awayGames;
	}

	/**
	 * current regular season.
	 */
	public RegularSeason regularSeason() {
		return league().regularSeason();
	}

	/**
	 * all matchups this team has with another team this season.
	 * 
	 * @param team - the other team.
	 * @return all games against this team.
	 */
	public ArrayList<Game> matchups(Team team) {
		return regularSeason().matchups(this, team);
	}

	/**
	 * List of all games against specific Team.
	 * 
	 * @param team - other team.
	 * @return all games against this team.
	 */
	public String headToHeadSchedule(Team team) {
		String str = regularSeason().year() + "-" + (regularSeason().year() + 1) + " Schedule (" + city + " " + name
				+ " vs. " + team.city + " " + team.name + ")\n";
		int amountOfChar = str.length();
		for (int i = 0; i < amountOfChar - 1; i++) {
			str += "_";
		}
		str += "\n";
		ArrayList<Game> matches = matchups(team);
		for (int i = 0; i < matches.size(); i++) {
			str += "\n" + matches.get(i).toString();
		}
		return str;
	}

	/**
	 * total amount of games this team has for the season.
	 */
	public int totalAmountOfGames() {
		if (schedule == null) {
			return 0;
		}
		return schedule.size();
	}

	/**
	 * amount of games remaining until the end of the season.
	 */
	public int gamesRemaining() {
		return totalAmountOfGames() - gamesPlayed();
	}

	private static class Gaussian {

		// return pdf(x) = standard Gaussian pdf
		public static double pdf(double x) {
			return Math.exp(-x * x / 2) / Math.sqrt(2 * Math.PI);
		}

		// return pdf(x, mu, signma) = Gaussian pdf with mean mu and stddev sigma
		public static double pdf(double x, double mu, double sigma) {
			return pdf((x - mu) / sigma) / sigma;
		}

		// return cdf(z) = standard Gaussian cdf using Taylor approximation
		public static double cdf(double z) {
			if (z < -8.0)
				return 0.0;
			if (z > 8.0)
				return 1.0;
			double sum = 0.0, term = z;
			for (int i = 3; sum + term != sum; i += 2) {
				sum = sum + term;
				term = term * z * z / i;
			}
			return 0.5 + sum * pdf(z);
		}

		// return cdf(z, mu, sigma) = Gaussian cdf with mean mu and stddev sigma
		public static double cdf(double z, double mu, double sigma) {
			return cdf((z - mu) / sigma);
		}

		// Compute z such that cdf(z) = y via bisection search
		public static double inverseCDF(double y) {
			return inverseCDF(y, 0.00000001, -8, 8);
		}

		// bisection search
		private static double inverseCDF(double y, double delta, double lo, double hi) {
			double mid = lo + (hi - lo) / 2;
			if (hi - lo < delta)
				return mid;
			if (cdf(mid) > y)
				return inverseCDF(y, delta, lo, mid);
			else
				return inverseCDF(y, delta, mid, hi);
		}

		// return phi(x) = standard Gaussian pdf
		@Deprecated
		public static double phi(double x) {
			return pdf(x);
		}

		// return phi(x, mu, signma) = Gaussian pdf with mean mu and stddev sigma
		@Deprecated
		public static double phi(double x, double mu, double sigma) {
			return pdf(x, mu, sigma);
		}

		// return Phi(z) = standard Gaussian cdf using Taylor approximation
		@Deprecated
		public static double Phi(double z) {
			return cdf(z);
		}

		// return Phi(z, mu, sigma) = Gaussian cdf with mean mu and stddev sigma
		@Deprecated
		public static double Phi(double z, double mu, double sigma) {
			return cdf(z, mu, sigma);
		}

		// Compute z such that Phi(z) = y via bisection search
		@Deprecated
		public static double PhiInverse(double y) {
			return inverseCDF(y);
		}
	}
}
