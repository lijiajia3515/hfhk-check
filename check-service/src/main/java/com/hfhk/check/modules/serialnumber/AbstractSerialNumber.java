package com.hfhk.check.modules.serialnumber;

import lombok.Setter;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * 抽象编码策略
 */
public abstract class AbstractSerialNumber implements SerialNumber {

	protected static final String delimiter = "-";

	@Setter
	protected SerialNumberSettings settings;

	protected AbstractSerialNumber() {
		settings = SerialNumberSettings.builder().defaultStrategy(SerialNumberStrategy.Number).build();
	}

	protected AbstractSerialNumber(SerialNumberSettings settings) {
		this.settings = settings;
	}

	@Override
	public String encode(List<Long> serialNumber, String delimiter) {
		return IntStream.range(0, serialNumber.size())
			.mapToObj(x -> settings.strategy(x).encode(serialNumber.get(x)))
			.collect(Collectors.joining(delimiter));
	}

	@Override
	public String encode(List<Long> serialNumber) {
		return encode(serialNumber, delimiter);
	}

	@Override
	public List<Long> decode(String sn, String delimiter) {
		List<String> source = Optional.ofNullable(sn)
			.map(x -> Arrays.asList(x.split(delimiter)))
			.stream()
			.flatMap(Collection::stream)
			.collect(Collectors.toList());
		return IntStream.range(0, source.size())
			.mapToLong(x -> settings.strategy(x).decode(source.get(x)))
			.boxed()
			.collect(Collectors.toList());
	}

	@Override
	public List<Long> decode(String sn) {
		return decode(sn, delimiter);
	}

	@Override
	public String getDelimiter() {
		return delimiter;
	}
}
