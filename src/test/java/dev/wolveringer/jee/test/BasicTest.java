package dev.wolveringer.jee.test;

import static org.junit.Assert.*;

import org.junit.Test;

import dev.wolveringer.jee.ExpressionEvaluator;

public class BasicTest {

	@Test
	public void mainTest() {
		ExpressionEvaluator ev = new ExpressionEvaluator();
		//ev.evaluate("(x+y == y-x)");
		//ev.evaluate("x+y+(z*2)");
		//ev.evaluate("x*2+y+sin(22-5)+x");
		ev.evaluate("abs(-1-2)");
	}

}
