package tests;

import java.util.Random;

/**
 * TEST of how evenly spread random numbers are along domain interval (test of
 * randomization quality).
 * 
 * @author starnet © 2021
 *
 */
public class RandTest {
	final static double P = 0.01;
	final static int SAMPLE_SIZE = 1000;

	public static void main(String[] args) {
		Random rand = new Random();
		int complyCount = 0;
		for (int i = 0; i < SAMPLE_SIZE; i++) {
			double rn = rand.nextDouble();
			System.out.println(rn);
			if (rn <= P) {
				complyCount++;
			}
		}
		System.out.println("\n The count of NUMBERS<=" + P + " ==> " + complyCount + " of " + SAMPLE_SIZE + ".");
	}

}
