package com.wingmoney.core.log;

import ch.qos.logback.classic.PatternLayout;
import ch.qos.logback.classic.spi.ILoggingEvent;
import com.wingmoney.core.ICore;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class MaskingPatternLayout extends PatternLayout {

    private Pattern multilinePattern;
    private List<String> maskPatterns = new ArrayList<>(ICore.DEFAULT_MASKING_LOG_REGX);

    public MaskingPatternLayout() {
        multilinePattern = Pattern.compile(maskPatterns.stream().collect(Collectors.joining("|")), Pattern.MULTILINE);
    }

    public void addMaskPattern(String maskPattern) {
        maskPatterns.add(maskPattern);
        multilinePattern = Pattern.compile(maskPatterns.stream().collect(Collectors.joining("|")), Pattern.MULTILINE);
    }

    @Override
    public String doLayout(ILoggingEvent event) {
        return maskMessage(super.doLayout(event));
    }

    private String maskMessage(String message) {
        if (multilinePattern == null) {
            return message;
        }
        StringBuilder sb = new StringBuilder(message);
        Matcher matcher = multilinePattern.matcher(sb);
        while (matcher.find()) {
            IntStream.rangeClosed(1, matcher.groupCount()).forEach(group -> {
                if (matcher.group(group) != null) {
                    IntStream.range(matcher.start(group), matcher.end(group)).forEach(i -> sb.setCharAt(i, '*'));
                }
            });
        }
        return sb.toString();
    }
}
