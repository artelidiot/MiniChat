package me.artel.minichat.util;

import java.util.regex.Pattern;

public class TimeSyntax {
    private final Pattern timePattern = Pattern.compile("(\\d+)\\s*([a-zA-Z]+)");

    private final Pattern hourPattern = Pattern.compile("\\b(?:hour(?:s)?|h)\\b");
    private final Pattern minutePattern = Pattern.compile("\\b(?:minute(?:s)?|min(?:s)?|m)\\b");
    private final Pattern secondPattern = Pattern.compile("\\b(?:second(?:s)?|sec(?:s)?|s)\\b");
    private final Pattern millisecondPattern = Pattern.compile("\\b(?:millisecond(?:s)?|ms)\\b");

    
}