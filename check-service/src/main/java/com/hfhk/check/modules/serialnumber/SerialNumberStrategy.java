package com.hfhk.check.modules.serialnumber;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public enum SerialNumberStrategy {
	/**
	 * 16进制
	 */
	Hex {
		@Override
		public String encode(Long number) {
			return Long.toHexString(number);
		}

		@Override
		public Long decode(String sn) {
			try {
				return Long.parseLong(sn, 16);
			} catch (NumberFormatException e) {
				return 0L;
			}
		}
	},
	/**
	 * 22 进制
	 */
	BASE_22() {
		private static final int RADIX = 22;

		private final List<Character> DIGITS = Arrays.asList(
			'A', 'B', 'C', 'D', 'E',
			'F', 'G', 'H', 'J', 'K',
			'M', 'N', 'P', 'Q', 'R',
			'S', 'T', 'V', 'W', 'X',
			'Y', 'Z'
		);
		private final Map<Character, Integer> DIGITS_MAP = IntStream.range(0, DIGITS.size()).boxed().collect(Collectors.toMap(DIGITS::get, x -> x));

		@Override
		public String encode(Long number) {
			return Long.toString(number, RADIX).chars()
				.mapToObj(x -> (char) x)
				.map(x -> DIGITS.get(Character.digit(x, RADIX)))
				.map(Object::toString)
				.collect(Collectors.joining());
		}

		@Override
		public Long decode(String sn) {
			try {
				return Long.parseLong(sn.chars()
						.mapToObj(x -> (char) x)
						.map(x -> Long.toString(DIGITS_MAP.getOrDefault(x, 0), RADIX))
						.collect(Collectors.joining()),
					RADIX);
			} catch (NumberFormatException e) {
				return 0L;
			}
		}
	},

	/**
	 * 32 进制
	 */
	BASE_32() {
		private final List<Character> DIGITS = Arrays.asList(
			'0', '1', '2', '3', '4',
			'5', '6', '7', '8', '9',
			'A', 'B', 'C', 'D', 'E',
			'F', 'G', 'H', 'J', 'K',
			'M', 'N', 'P', 'Q', 'R',
			'S', 'T', 'V', 'W', 'X',
			'Y', 'Z'
		);

		@Override
		public String encode(Long number) {
			int radix = 32;
			return Long.toString(number, radix).chars()
				.mapToObj(x -> DIGITS.get(Character.digit(x, radix)))
				.map(Object::toString)
				.collect(Collectors.joining());
		}

		@Override
		public Long decode(String sn) {
			return null;
		}
	},

	/**
	 * 纯数字
	 */
	Number() {
		@Override
		public String encode(Long number) {
			return number.toString();
		}

		@Override
		public Long decode(String sn) {
			return Long.parseLong(sn);
		}
	},

	/**
	 * 3位数字
	 */
	NUMBER3() {
		@Override
		public String encode(Long number) {
			return String.format("%03d", number);
		}

		@Override
		public Long decode(String sn) {
			try {
				return Long.parseLong(sn);
			} catch (NumberFormatException e) {
				return 0L;
			}
		}
	},

	/**
	 * 4位数字
	 */
	NUMBER4() {
		@Override
		public String encode(Long number) {
			return String.format("%04d", number);
		}

		@Override
		public Long decode(String sn) {
			try {
				return Long.parseLong(sn);
			} catch (NumberFormatException e) {
				return 0L;
			}
		}
	},

	/**
	 * 4位数字
	 */
	PROBLEM_NUMBER3() {
		private static final String PREFIX = "P";

		@Override
		public String encode(Long number) {
			return String.format("P%03d", number);
		}

		@Override
		public Long decode(String sn) {
			try {
				return Long.parseLong(sn.replaceFirst(PREFIX, ""));
			} catch (NumberFormatException e) {
				return 0L;
			}
		}
	},

	/**
	 * 5位数字
	 */
	NUMBER5() {
		@Override
		public String encode(Long number) {
			return String.format("%05d", number);
		}

		@Override
		public Long decode(String sn) {
			try {
				return Long.parseLong(sn);
			} catch (NumberFormatException e) {
				return 0L;
			}
		}
	},
	;

	public abstract String encode(Long number);

	public abstract Long decode(String sn);
}
