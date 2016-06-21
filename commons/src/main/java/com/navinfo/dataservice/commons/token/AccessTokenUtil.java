package com.navinfo.dataservice.commons.token;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

public class AccessTokenUtil {

	private static final transient Logger log = Logger
			.getLogger(AccessTokenUtil.class);

	private static Map<Integer, Character> mapD2N = new HashMap<Integer, Character>() {
		{

			put(0, '0');
			put(1, '1');
			put(2, '2');
			put(3, '3');
			put(4, '4');
			put(5, '5');
			put(6, '6');
			put(7, '7');
			put(8, '8');
			put(9, '9');
			put(10, 'A');
			put(11, 'B');
			put(12, 'C');
			put(13, 'D');
			put(14, 'E');
			put(15, 'F');
			put(16, 'G');
			put(17, 'H');
			put(18, 'I');
			put(19, 'J');
			put(20, 'K');
			put(21, 'L');
			put(22, 'M');
			put(23, 'N');
			put(24, 'O');
			put(25, 'P');
			put(26, 'Q');
			put(27, 'R');
			put(28, 'S');
			put(29, 'T');
			put(30, 'U');
			put(31, 'V');
			put(32, 'W');
			put(33, 'X');
			put(34, 'Y');
			put(35, 'Z');

		}
	};

	private static Map<Character, Integer> mapN2D = new HashMap<Character, Integer>() {
		{
			put('0', 0);
			put('1', 1);
			put('2', 2);
			put('3', 3);
			put('4', 4);
			put('5', 5);
			put('6', 6);
			put('7', 7);
			put('8', 8);
			put('9', 9);
			put('A', 10);
			put('B', 11);
			put('C', 12);
			put('D', 13);
			put('E', 14);
			put('F', 15);
			put('G', 16);
			put('H', 17);
			put('I', 18);
			put('J', 19);
			put('K', 20);
			put('L', 21);
			put('M', 22);
			put('N', 23);
			put('O', 24);
			put('P', 25);
			put('Q', 26);
			put('R', 27);
			put('S', 28);
			put('T', 29);
			put('U', 30);
			put('V', 31);
			put('W', 32);
			put('X', 33);
			put('Y', 34);
			put('Z', 35);
		}
	};

	public static String d2n(long d) {

		StringBuilder sb = new StringBuilder();

		long div = d / 36;

		int mod = (int) (d % 36);

		sb.append(mapD2N.get(mod));

		while (div >= 36) {
			mod = (int) (div % 36);

			div = div / 36;

			sb.append(mapD2N.get(mod));
		}

		sb.append(mapD2N.get((int)div));

		sb.reverse();

		return sb.toString();
	}

	public static long n2d(String str) {
		try {
			str = StringUtils.stripStart(str, "0");

			str = StringUtils.reverse(str);

			long result = 0;

			char[] array = str.toCharArray();

			for (int i = 0; i < array.length; i++) {

				result += mapN2D.get(array[i]) * (Math.pow(36, i));
			}

			return result;

		} catch (Exception ex) {

			log.error(ex.getMessage());

			return 0;
		}

	}
}
