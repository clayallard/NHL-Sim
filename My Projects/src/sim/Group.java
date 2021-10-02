package sim;

import java.util.ArrayList;
import java.util.NoSuchElementException;

public interface Group {

	public ArrayList<Team> teams();

	public Team leader();

	public Team last();

	public String standings();

	public int seedingOfTeam(Team team) throws NoSuchElementException;

	public Team seedingOfTeam(int seed) throws IndexOutOfBoundsException;

	public int size();

	public String name();
}
