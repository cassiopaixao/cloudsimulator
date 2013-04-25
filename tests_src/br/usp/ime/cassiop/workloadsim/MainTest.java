package br.usp.ime.cassiop.workloadsim;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

import br.usp.ime.cassiop.workloadsim.ExecutionBuilder.EnvironmentToUse;
import br.usp.ime.cassiop.workloadsim.ExecutionBuilder.MigrationControlToUse;
import br.usp.ime.cassiop.workloadsim.ExecutionBuilder.PlacementType;

public class MainTest {

	private Main main = null;

	@Before
	public void before() {
		main = new Main();
	}

	@Test
	public void testGetErrorArguments() {
		String args = "-e 0.3 -e 0.2 -e -0.1";

		main.getArguments(args.split(" "));

		assertTrue(main.errorArgs.contains(Double.valueOf(0.3)));
		assertTrue(main.errorArgs.contains(Double.valueOf(0.2)));
		assertTrue(main.errorArgs.contains(Double.valueOf(-0.1)));
	}

	@Test
	public void testGetMigrationArguments() {
		String args = "-m ifchange -m ifoverload -m khanna";

		main.getArguments(args.split(" "));

		assertTrue(main.migrationsToUse
				.contains(MigrationControlToUse.MIGRATE_IF_CHANGE));
		assertTrue(main.migrationsToUse
				.contains(MigrationControlToUse.MIGRATE_IF_CHANGE_AND_SERVER_BECOMES_OVERLOADED));
		assertTrue(main.migrationsToUse.contains(MigrationControlToUse.KHANNA));
		assertFalse(main.migrationsToUse.contains(MigrationControlToUse.NONE));
	}

	@Test
	public void testGetMigrationIfArguments() {
		String args = "-m if-";

		main.getArguments(args.split(" "));

		assertTrue(main.migrationsToUse
				.contains(MigrationControlToUse.MIGRATE_IF_CHANGE));
		assertTrue(main.migrationsToUse
				.contains(MigrationControlToUse.MIGRATE_IF_CHANGE_AND_SERVER_BECOMES_OVERLOADED));
		assertFalse(main.migrationsToUse.contains(MigrationControlToUse.KHANNA));
		assertFalse(main.migrationsToUse.contains(MigrationControlToUse.NONE));
	}

	@Test
	public void testGetMigrationAllArguments() {
		String args = "-m all";

		main.getArguments(args.split(" "));

		assertTrue(main.migrationsToUse
				.contains(MigrationControlToUse.MIGRATE_IF_CHANGE));
		assertTrue(main.migrationsToUse
				.contains(MigrationControlToUse.MIGRATE_IF_CHANGE_AND_SERVER_BECOMES_OVERLOADED));
		assertTrue(main.migrationsToUse.contains(MigrationControlToUse.KHANNA));
		assertTrue(main.migrationsToUse.contains(MigrationControlToUse.NONE));
	}

	@Test
	public void testGetPlacementArguments() {
		String args = "-p bfd -p ffd -p khanna";

		main.getArguments(args.split(" "));

		assertTrue(main.placementsToUse.contains(PlacementType.BEST_FIT_DEC));
		assertTrue(main.placementsToUse.contains(PlacementType.FIRST_FIT_DEC));
		assertFalse(main.placementsToUse.contains(PlacementType.WORST_FIT_DEC));
		assertTrue(main.placementsToUse.contains(PlacementType.KHANNA));
		assertFalse(main.placementsToUse
				.contains(PlacementType.ALMOST_WORST_FIT));
		assertFalse(main.placementsToUse
				.contains(PlacementType.ALMOST_WORST_FIT_DEC));
	}

	@Test
	public void testGetPlacementAlmostArguments() {
		String args = "-p awf -p awfd -p wfd";

		main.getArguments(args.split(" "));

		assertFalse(main.placementsToUse.contains(PlacementType.BEST_FIT_DEC));
		assertFalse(main.placementsToUse.contains(PlacementType.FIRST_FIT_DEC));
		assertTrue(main.placementsToUse.contains(PlacementType.WORST_FIT_DEC));
		assertFalse(main.placementsToUse.contains(PlacementType.KHANNA));
		assertTrue(main.placementsToUse
				.contains(PlacementType.ALMOST_WORST_FIT));
		assertTrue(main.placementsToUse
				.contains(PlacementType.ALMOST_WORST_FIT_DEC));
	}

	@Test
	public void testGetPlacementAllArguments() {
		String args = "-p all";

		main.getArguments(args.split(" "));

		assertTrue(main.placementsToUse.contains(PlacementType.BEST_FIT_DEC));
		assertTrue(main.placementsToUse.contains(PlacementType.FIRST_FIT_DEC));
		assertTrue(main.placementsToUse.contains(PlacementType.WORST_FIT_DEC));
		assertTrue(main.placementsToUse.contains(PlacementType.KHANNA));
		assertTrue(main.placementsToUse
				.contains(PlacementType.ALMOST_WORST_FIT));
		assertTrue(main.placementsToUse
				.contains(PlacementType.ALMOST_WORST_FIT_DEC));
	}

	@Test
	public void testGetEnvironmentArguments() {
		String args = "-c google";

		main.getArguments(args.split(" "));

		assertFalse(main.environmentsToUse
				.contains(EnvironmentToUse.HOMOGENEOUS));
		assertTrue(main.environmentsToUse.contains(EnvironmentToUse.GOOGLE));
		assertFalse(main.environmentsToUse.contains(EnvironmentToUse.TEST));
	}

	@Test
	public void testGetEnvironmentHomogeneousAndTestArguments() {
		String args = "-c test -c homogeneous";

		main.getArguments(args.split(" "));

		assertTrue(main.environmentsToUse
				.contains(EnvironmentToUse.HOMOGENEOUS));
		assertFalse(main.environmentsToUse.contains(EnvironmentToUse.GOOGLE));
		assertTrue(main.environmentsToUse.contains(EnvironmentToUse.TEST));
	}
	
	@Test
	public void testGetEnvironmentHomogeneousAndGoogleClusterArguments() {
		String args = "-c google -c homogeneous";

		main.getArguments(args.split(" "));

		assertTrue(main.environmentsToUse
				.contains(EnvironmentToUse.HOMOGENEOUS));
		assertTrue(main.environmentsToUse.contains(EnvironmentToUse.GOOGLE));
		assertFalse(main.environmentsToUse.contains(EnvironmentToUse.TEST));
	}

	@Test
	public void testGetEnvironmentDefault() {
		String args = "";

		main.getArguments(args.split(" "));

		assertFalse(main.environmentsToUse
				.contains(EnvironmentToUse.HOMOGENEOUS));
		assertFalse(main.environmentsToUse.contains(EnvironmentToUse.GOOGLE));
		assertFalse(main.environmentsToUse.contains(EnvironmentToUse.TEST));
	}

	@Test
	public void testGetLogggingArgument() {
		String args = "-l";

		main.getArguments(args.split(" "));

		assertTrue(main.shouldLog);
	}

	@Test
	public void testGetNotLogggingArgument() {
		String args = "";

		main.getArguments(args.split(" "));

		assertFalse(main.shouldLog);
	}

	@Test
	public void testGetNumThreadsArgument() {
		String args = "-t 5";

		main.getArguments(args.split(" "));

		assertEquals(main.maxThreads, 5);
	}

	@Test
	public void testGetDefaultNumThreadsArgument() {
		String args = "";

		main.getArguments(args.split(" "));

		assertEquals(main.maxThreads, 1);
	}

	@Test
	public void testGetInvalidNumThreadsArgument() {
		String args = "-t -3";

		main.getArguments(args.split(" "));

		assertEquals(main.maxThreads, 1);
	}
}
