package com.wingmoney.core.log;

import com.fasterxml.jackson.core.JsonGenerator;
import net.logstash.logback.mask.FieldMasker;
import net.logstash.logback.mask.MaskingJsonGenerator;
import net.logstash.logback.mask.ValueMasker;

import java.io.IOException;
import java.util.Collection;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.IntStream;

public class LogStashMaskingJsonGenerator extends MaskingJsonGenerator {

    private Pattern multilinePattern;

    public LogStashMaskingJsonGenerator() {
        this(null, null, null);
    }

    public LogStashMaskingJsonGenerator(JsonGenerator delegate, Collection<FieldMasker> fieldMaskers, Collection<ValueMasker> valueMaskers) {
        super(delegate, fieldMaskers, valueMaskers);
    }

    public Pattern getMultilinePattern() {
        return multilinePattern;
    }

    public void setMultilinePattern(Pattern multilinePattern) {
        this.multilinePattern = multilinePattern;
    }

    @Override
    public void writeString(String text) throws IOException {
        super.writeString(maskMessage(text));
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
