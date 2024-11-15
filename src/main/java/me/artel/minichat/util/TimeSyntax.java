package me.artel.minichat.util;

import java.util.regex.Pattern;

public class TimeSyntax {
    private final Pattern timePattern = Pattern.compile("(\\d+)\\s*([a-zA-Z]+)");
    private final Pattern hourPattern = Pattern.compile("hours|hour|h");
    private final Pattern minutePattern = Pattern.compile("minutes|minute|mins|min|m");
    private final Pattern secondPattern = Pattern.compile("seconds|second|secs|sec|s");
    private final Pattern millisecondPattern = Pattern.compile("milliseconds|millisecond|ms");

}