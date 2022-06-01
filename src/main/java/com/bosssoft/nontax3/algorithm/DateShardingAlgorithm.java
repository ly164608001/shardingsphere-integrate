package com.bosssoft.nontax3.algorithm;

import com.google.common.base.Preconditions;
import lombok.Getter;
import lombok.Setter;
import org.apache.shardingsphere.infra.config.exception.ShardingSphereConfigurationException;
import org.apache.shardingsphere.sharding.algorithm.sharding.ShardingAlgorithmException;
import org.apache.shardingsphere.sharding.api.sharding.standard.PreciseShardingValue;
import org.apache.shardingsphere.sharding.api.sharding.standard.RangeShardingValue;
import org.apache.shardingsphere.sharding.api.sharding.standard.StandardShardingAlgorithm;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;
import java.util.Collection;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @desc 根据日期分片算法
 * @author ly
 * @date 2021/1/8
 */
public class DateShardingAlgorithm implements StandardShardingAlgorithm<Comparable<?>> {

    private static final String DATE_PATTERN_KEY = "date-pattern";

    private static final String DATE_LOWER_KEY = "date-lower";

    private static final String DATE_UPPER_KEY = "date-upper";

    private static final String SHARDING_SUFFIX_FORMAT_KEY = "sharding-suffix-pattern";

    private static final String DATE_AMOUNT_KEY = "date-interval-amount";

    private static final String DATE_UNIT_KEY = "date-interval-unit";

    @Getter
    @Setter
    private Properties props = new Properties();

    private DateTimeFormatter dateTimeFormatter;

    private int dateTimePatternLength;

    private LocalDate dateLower;

    private LocalDate dateUpper;

    private DateTimeFormatter tableSuffixPattern;

    private int stepAmount;

    private ChronoUnit stepUnit;

    @Override
    public void init() {
        String dateTimePattern = getDateTimePattern();
        dateTimeFormatter = DateTimeFormatter.ofPattern(dateTimePattern);
        dateTimePatternLength = dateTimePattern.length();
        dateLower = getDateLower(dateTimePattern);
        dateUpper = getDateUpper(dateTimePattern);
        tableSuffixPattern = getTableSuffixPattern();
        stepAmount = Integer.parseInt(props.getOrDefault(DATE_AMOUNT_KEY, 1).toString());
        stepUnit = props.containsKey(DATE_UNIT_KEY) ? getStepUnit(props.getProperty(DATE_UNIT_KEY)) : ChronoUnit.DAYS;
    }

    private String getDateTimePattern() {
        Preconditions.checkArgument(props.containsKey(DATE_PATTERN_KEY), "% can not be null.", DATE_PATTERN_KEY);
        return props.getProperty(DATE_PATTERN_KEY);
    }

    private LocalDate getDateLower(final String dateTimePattern) {
        Preconditions.checkArgument(props.containsKey(DATE_LOWER_KEY), "% can not be null.", DATE_LOWER_KEY);
        return getDate(DATE_LOWER_KEY, props.getProperty(DATE_LOWER_KEY), dateTimePattern);
    }

    private LocalDate getDateUpper(final String dateTimePattern) {
        return props.containsKey(DATE_UPPER_KEY) ? getDate(DATE_UPPER_KEY, props.getProperty(DATE_UPPER_KEY), dateTimePattern) : LocalDate.now();
    }

    private LocalDate getDate(final String dateTimeKey, final String dateTimeValue, final String dateTimePattern) {
        try {
            return LocalDate.parse(dateTimeValue, dateTimeFormatter);
        } catch (final DateTimeParseException ex) {
            throw new ShardingSphereConfigurationException("Invalid %s, datetime pattern should be `%s`, value is `%s`", dateTimeKey, dateTimePattern, dateTimeValue);
        }
    }

    private DateTimeFormatter getTableSuffixPattern() {
        Preconditions.checkArgument(props.containsKey(SHARDING_SUFFIX_FORMAT_KEY), "% can not be null.", SHARDING_SUFFIX_FORMAT_KEY);
        return DateTimeFormatter.ofPattern(props.getProperty(SHARDING_SUFFIX_FORMAT_KEY));
    }

    private ChronoUnit getStepUnit(final String stepUnit) {
        for (ChronoUnit each : ChronoUnit.values()) {
            if (each.toString().equalsIgnoreCase(stepUnit)) {
                return each;
            }
        }
        throw new UnsupportedOperationException(String.format("Cannot find step unit for specified %s property: `%s`", DATE_UNIT_KEY, stepUnit));
    }

    @Override
    public String doSharding(final Collection<String> availableTargetNames, final PreciseShardingValue<Comparable<?>> shardingValue) {
        return availableTargetNames.stream()
                .filter(each -> each.endsWith(parseDateTime(shardingValue.getValue().toString()).format(tableSuffixPattern)))
                .findFirst().orElseThrow(() -> new ShardingAlgorithmException(String.format("failed to shard value %s, and availableTables %s", shardingValue, availableTargetNames)));
    }

    @Override
    public Collection<String> doSharding(final Collection<String> availableTargetNames, final RangeShardingValue<Comparable<?>> shardingValue) {
        boolean hasStartTime = shardingValue.getValueRange().hasLowerBound();
        boolean hasEndTime = shardingValue.getValueRange().hasUpperBound();
        if (!hasStartTime && !hasEndTime) {
            return availableTargetNames;
        }
        LocalDate startDate = hasStartTime ? parseDateTime(shardingValue.getValueRange().lowerEndpoint().toString()) : dateLower;
        LocalDate endDate = hasEndTime ? parseDateTime(shardingValue.getValueRange().upperEndpoint().toString()) : dateUpper;
        LocalDate calculateTime = startDate;
        Set<String> result = new HashSet<>();
        while (!calculateTime.isAfter(endDate)) {
            result.addAll(getMatchedTables(calculateTime, availableTargetNames));
            calculateTime = calculateTime.plus(stepAmount, stepUnit);
        }
        result.addAll(getMatchedTables(endDate, availableTargetNames));
        return result;
    }

    private LocalDate parseDateTime(final String value) {
        return LocalDate.parse(value.substring(0, dateTimePatternLength), dateTimeFormatter);
    }

    private Collection<String> getMatchedTables(final LocalDate date, final Collection<String> availableTargetNames) {
        String tableSuffix = date.format(tableSuffixPattern);
        return availableTargetNames.parallelStream().filter(each -> each.endsWith(tableSuffix)).collect(Collectors.toSet());
    }

    @Override
    public String getType() {
        return "DATE";
    }

    @Override
    public Properties getProps() {
        return props;
    }

    @Override
    public void setProps(Properties props) {
        this.props = props;
    }
}
