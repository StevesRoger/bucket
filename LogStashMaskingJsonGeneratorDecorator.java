package com.wingmoney.core.log;

import com.fasterxml.jackson.core.JsonGenerator;
import com.wingmoney.core.ICore;
import com.wingmoney.core.util.PropertyUtil;
import net.logstash.logback.decorate.JsonGeneratorDecorator;
import net.logstash.logback.mask.MaskingJsonGenerator;
import net.logstash.logback.mask.MaskingJsonGeneratorDecorator;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class LogStashMaskingJsonGeneratorDecorator extends MaskingJsonGeneratorDecorator {

    private Pattern multilinePattern;
    private List<String> maskPatterns = new ArrayList<>(ICore.DEFAULT_MASKING_LOG_REGX);

    public LogStashMaskingJsonGeneratorDecorator() {
        multilinePattern = Pattern.compile(maskPatterns.stream().collect(Collectors.joining("|")), Pattern.MULTILINE);
    }

    public void addMaskPattern(String maskPattern) {
        maskPatterns.add(maskPattern);
        multilinePattern = Pattern.compile(maskPatterns.stream().collect(Collectors.joining("|")), Pattern.MULTILINE);
    }

    @Override
    public synchronized void start() {
        super.start();
        try {
            Field field = this.getClass().getSuperclass().getDeclaredField("delegate");
            field.setAccessible(true);
            JsonGeneratorDecorator originalDelegate = (JsonGeneratorDecorator) field.get(this);
            JsonGeneratorDecorator delegate = v -> {
                LogStashMaskingJsonGenerator generator = new LogStashMaskingJsonGenerator(v, null, null);
                generator.setMultilinePattern(multilinePattern);
                JsonGenerator originalGenerator = originalDelegate.decorate(v);
                if (originalGenerator instanceof MaskingJsonGenerator)
                    PropertyUtil.copy(originalGenerator, generator, "generator");
                return generator;
            };
            field.set(this, delegate);
        } catch (Exception ex) {
            ex.printStackTrace();
            throw new IllegalStateException(ex);
        }
    }
}
