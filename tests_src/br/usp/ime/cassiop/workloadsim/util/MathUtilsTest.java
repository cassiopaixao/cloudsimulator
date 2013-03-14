package br.usp.ime.cassiop.workloadsim.util;

import static org.junit.Assert.*;

import org.junit.Test;

public class MathUtilsTest {

	@Test
	public void testEqualsDoubleDouble() {
		assertTrue(MathUtils.equals(0.0, 0.0));
		assertTrue(MathUtils.equals(10.0, 10.0));
		assertTrue(MathUtils.equals(-5.0, -5.0));
		
		assertFalse(MathUtils.equals(0.0, 0.1));
		assertFalse(MathUtils.equals(-1.0, 0.0));
		assertFalse(MathUtils.equals(1.0, -1.0));

		assertFalse(MathUtils.equals(1.0, 1.000000001));
	}

	@Test
	public void testEqualsDoubleDoubleDouble() {
		assertTrue(MathUtils.equals(1.0, 0.0001, 1.0));
		assertTrue(MathUtils.equals(1.0, 1.0001, 0.1));

		assertFalse(MathUtils.equals(1.0, 0.0001, 0.9999));
		assertFalse(MathUtils.equals(1.0, 1.0001, 0.00001));
	}

	@Test
	public void testGreaterThanDoubleDouble() {
		assertTrue(MathUtils.greaterThan(1.0, 0.9));
		assertTrue(MathUtils.greaterThan(-1.0, -1.1));
		assertTrue(MathUtils.greaterThan(1.0, -1.0));
		assertTrue(MathUtils.greaterThan(1.0, 0.0));

		assertFalse(MathUtils.greaterThan(1.0, 1.1));
		assertFalse(MathUtils.greaterThan(-1.1, -1.0));
		assertFalse(MathUtils.greaterThan(-1.0, 1.0));
		assertFalse(MathUtils.greaterThan(0.0, 1.0));
	}

	@Test
	public void testGreaterThanDoubleDoubleDouble() {
		assertTrue(MathUtils.greaterThan(1.0, 0.9, 0.01));
		assertTrue(MathUtils.greaterThan(1.0, 0.89, 0.1));
		assertTrue(MathUtils.greaterThan(1.0, -1.0, 1.0));
		assertTrue(MathUtils.greaterThan(-1.0, -1.1, 0.09));

		assertFalse(MathUtils.greaterThan(1.0, 1.0, 0.0000000001));
		assertFalse(MathUtils.greaterThan(1.0, 2.0, 0.1));
		assertFalse(MathUtils.greaterThan(1.0, 2.0, 1.5));

		assertFalse(MathUtils.greaterThan(1.0, 0.9, 0.1));
		assertFalse(MathUtils.greaterThan(1.0, 0.999, 0.01));
		assertFalse(MathUtils.greaterThan(1.0, 0.99, 0.02));
		assertFalse(MathUtils.greaterThan(1.0, -1.0, 2.0));
		assertFalse(MathUtils.greaterThan(-1.0, -1.1, 0.11));

	}

	@Test
	public void testLessThanDoubleDouble() {
		assertTrue(MathUtils.lessThan(1.0, 1.1));
		assertTrue(MathUtils.lessThan(-1.1, -1.0));
		assertTrue(MathUtils.lessThan(-1.0, 1.0));
		assertTrue(MathUtils.lessThan(0.0, 1.0));

		assertFalse(MathUtils.lessThan(1.0, 0.9));
		assertFalse(MathUtils.lessThan(-1.0, -1.1));
		assertFalse(MathUtils.lessThan(1.0, -1.0));
		assertFalse(MathUtils.lessThan(1.0, 0.0));
	}

	@Test
	public void testLessThanDoubleDoubleDouble() {
		assertTrue(MathUtils.lessThan(1.0, 1.1, 0.01));
		assertTrue(MathUtils.lessThan(1.0, 2.0, 0.9));
		assertTrue(MathUtils.lessThan(-1.0, 0.0, 0.0));
		assertTrue(MathUtils.lessThan(-1.0, -0.5, 0.1));

		assertFalse(MathUtils.lessThan(1.0, 0.0, 0.1));
		assertFalse(MathUtils.lessThan(1.0, 0.5, 1.0));

		assertFalse(MathUtils.lessThan(1.0, 1.5, 0.51));
		assertFalse(MathUtils.lessThan(1.0, 1.1, 0.11));
		assertFalse(MathUtils.lessThan(1.0, 2.0, 1.01));

		assertFalse(MathUtils.lessThan(-1.0, 0.0, 1.5));
		assertFalse(MathUtils.lessThan(-1.0, -0.5, 1.0));
	}

	@Test
	public void testLessThanOrEqualsDoubleDouble() {
		// equals
		assertTrue(MathUtils.lessThanOrEquals(0.0, 0.0));
		assertTrue(MathUtils.lessThanOrEquals(10.0, 10.0));
		assertTrue(MathUtils.lessThanOrEquals(-5.0, -5.0));

		assertTrue(MathUtils.lessThanOrEquals(0.00000000001, 0.0));
		assertTrue(MathUtils.lessThanOrEquals(10.00000000001, 10.0));

		assertFalse(MathUtils.lessThanOrEquals(0.000000000011, 0.0));
		assertFalse(MathUtils.lessThanOrEquals(10.000000000011, 10.0));

		// less than
		assertTrue(MathUtils.lessThanOrEquals(1.0, 1.1));
		assertTrue(MathUtils.lessThanOrEquals(-1.1, -1.0));
		assertTrue(MathUtils.lessThanOrEquals(-1.0, 1.0));
		assertTrue(MathUtils.lessThanOrEquals(0.0, 1.0));

		assertFalse(MathUtils.lessThanOrEquals(1.0, 0.9));
		assertFalse(MathUtils.lessThanOrEquals(-1.0, -1.1));
		assertFalse(MathUtils.lessThanOrEquals(1.0, -1.0));
		assertFalse(MathUtils.lessThanOrEquals(1.0, 0.0));
	}

	@Test
	public void testLessThanOrEqualsDoubleDoubleDouble() {
		// equals
		assertTrue(MathUtils.lessThanOrEquals(1.0, 0.0001, 1.0));
		assertTrue(MathUtils.lessThanOrEquals(1.0, 0.9999, 0.0001));

		assertFalse(MathUtils.lessThanOrEquals(1.0, 0.0001, 0.00009));
		assertFalse(MathUtils.lessThanOrEquals(1.0, 0.9999, 0.00001));

		// less than
		assertTrue(MathUtils.lessThanOrEquals(1.0, 1.0001, 0.0000001));
		assertTrue(MathUtils.lessThanOrEquals(1.0, 1.00000001, 0.00000000001));
		assertTrue(MathUtils.lessThanOrEquals(-1.0, 1.0, 0.0000001));
		assertTrue(MathUtils.lessThanOrEquals(-1.0, 0, 0.01));

		assertFalse(MathUtils.lessThanOrEquals(1.0001, 1.0, 0.00001));
		assertFalse(MathUtils.lessThanOrEquals(1.1, 1.0, 0.09));

	}

}
