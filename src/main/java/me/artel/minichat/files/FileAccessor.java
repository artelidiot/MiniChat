package me.artel.minichat.files;

import java.util.List;

import me.artel.minichat.util.MiniUtil;

public class FileAccessor {


    // TODO: announcements.yml


    // format.yml
    public static boolean FORMAT_ENABLED = false;
    public static String FORMAT_GLOBAL = "<player-name>: <message>";


    // locale.yml
    // Assume blank for all locale values so we don't override desired blank messages
    public static String LOCALE_PREFIX = "";
    public static String LOCALE_VERSION = "";

    public static String LOCALE_CHAT_CLEARED = "";

    public static String LOCALE_CHAT_DELAY = "";
    public static String LOCALE_COMMAND_DELAY = "";

    public static String LOCALE_CHAT_MOVEMENT = "";
    public static String LOCALE_COMMAND_MOVEMENT = "";

    public static String LOCALE_CHAT_PARROT = "";

    public static String LOCALE_CHAT_SIMILARITY = "";
    public static String LOCALE_COMMAND_SIMILARITY = "";

    public static String LOCALE_CHAT_UPPERCASE = "";
    public static String LOCALE_COMMAND_UPPERCASE = "";

    public static String LOCALE_COMMAND_CLEAR_SUCCESSFUL = "";

    public static String LOCALE_COMMAND_RELOAD_SUCCESSFUL = "";
    public static String LOCALE_COMMAND_RELOAD_UNSUCCESSFUL = "";


    // motd.yml
    public static boolean MOTD_ENABLED = false;


    // options.yml
    public static int OPTIONS_CHAT_DELAY = 0;
    public static int OPTIONS_COMMAND_DELAY = 0;

    public static boolean OPTIONS_MOVEMENT_REQUIRED_CHAT = false;
    public static boolean OPTIONS_MOVEMENT_REQUIRED_COMMAND = false;

    public static int OPTIONS_CHAT_PARROTING = 0;
    public static int OPTIONS_CHAT_PARROTING_DECAY = 5000;
    public static int OPTIONS_CHAT_PARROTING_THRESHOLD = 6;
    public static boolean OPTIONS_CHAT_PARROTING_IGNORE_USERNAMES = false;
    public static List<String> OPTIONS_CHAT_PARROTING_IGNORE_LIST = List.of();

    public static int OPTIONS_CHAT_SIMILARITY = 0;
    public static int OPTIONS_CHAT_SIMILARITY_THRESHOLD = 6;
    public static boolean OPTIONS_CHAT_SIMILARITY_IGNORE_USERNAMES = false;
    public static List<String> OPTIONS_CHAT_SIMILARITY_IGNORE_LIST = List.of();
    public static int OPTIONS_COMMAND_SIMILARITY = 0;
    public static int OPTIONS_COMMAND_SIMILARITY_THRESHOLD = 12;
    public static boolean OPTIONS_COMMAND_SIMILARITY_IGNORE_USERNAMES = false;
    public static List<String> OPTIONS_COMMAND_SIMILARITY_IGNORE_LIST = List.of();

    public static int OPTIONS_CHAT_UPPERCASE = 0;
    public static int OPTIONS_CHAT_UPPERCASE_THRESHOLD = 6;
    public static String OPTIONS_CHAT_UPPERCASE_ACTION = "block";
    public static boolean OPTIONS_CHAT_UPPERCASE_IGNORE_USERNAMES = false;
    public static List<String> OPTIONS_CHAT_UPPERCASE_IGNORE_LIST = List.of();
    public static int OPTIONS_COMMAND_UPPERCASE = 0;
    public static int OPTIONS_COMMAND_UPPERCASE_THRESHOLD = 12;
    public static String OPTIONS_COMMAND_UPPERCASE_ACTION = "normalize";
    public static boolean OPTIONS_COMMAND_UPPERCASE_IGNORE_USERNAMES = false;
    public static List<String> OPTIONS_COMMAND_UPPERCASE_IGNORE_LIST = List.of();


    // plugin.yml
    public static final String PERMISSION_COMMAND = "minichat.command";
    public static final String PERMISSION_COMMAND_CLEAR = "minichat.command.clear";
    public static final String PERMISSION_COMMAND_MOTD = "minichat.command.motd";
    public static final String PERMISSION_COMMAND_RELOAD = "minichat.command.reload";

    public static final String PERMISSION_ANNOUNCEMENT = "minichat.announcement.%s";

    public static final String PERMISSION_BYPASS_RULE = "minichat.bypass.rule.%s";
    public static final String PERMISSION_BYPASS_CHAT_CLEAR = "minichat.bypass.chat.clear";
    public static final String PERMISSION_BYPASS_CHAT_DELAY = "minichat.bypass.chat.delay";
    public static final String PERMISSION_BYPASS_CHAT_MOVEMENT = "minichat.bypass.chat.movement";
    public static final String PERMISSION_BYPASS_CHAT_PARROT = "minichat.bypass.chat.parrot";
    public static final String PERMISSION_BYPASS_CHAT_SIMILARITY = "minichat.bypass.chat.similarity";
    public static final String PERMISSION_BYPASS_CHAT_UPPERCASE = "minichat.bypass.chat.uppercase";

    public static final String PERMISSION_BYPASS_COMMAND_DELAY = "minichat.bypass.command.delay";
    public static final String PERMISSION_BYPASS_COMMAND_MOVEMENT = "minichat.bypass.command.movement";
    public static final String PERMISSION_BYPASS_COMMAND_SIMILARITY = "minichat.bypass.command.similarity";
    public static final String PERMISSION_BYPASS_COMMAND_UPPERCASE = "minichat.bypass.command.uppercase";

    // rules.yml
    public static boolean RULES_ENABLED = false;
    public static boolean RULES_STRIP_DIACRITICAL_MARKS = false;

    /**
     * Method to update all cached values
     */
    public static void update() {
        FORMAT_ENABLED = FileManager.getFormat().node("enabled").getBoolean(false);
        FORMAT_GLOBAL = MiniUtil.getStringFromNodeObject(FileManager.getFormat().node("global-format"));


        LOCALE_PREFIX = MiniUtil.getStringFromNodeObject(FileManager.getLocale().node("prefix"));
        LOCALE_VERSION = MiniUtil.getStringFromNodeObject(FileManager.getLocale().node("version"));

        LOCALE_CHAT_CLEARED = MiniUtil.getStringFromNodeObject(FileManager.getLocale().node("chat-cleared"));

        LOCALE_CHAT_DELAY = MiniUtil.getStringFromNodeObject(FileManager.getLocale().node("chat-delay"));
        LOCALE_COMMAND_DELAY = MiniUtil.getStringFromNodeObject(FileManager.getLocale().node("command-delay"));

        LOCALE_CHAT_MOVEMENT = MiniUtil.getStringFromNodeObject(FileManager.getLocale().node("chat-movement"));
        LOCALE_COMMAND_MOVEMENT = MiniUtil.getStringFromNodeObject(FileManager.getLocale().node("command-movement"));

        LOCALE_CHAT_PARROT = MiniUtil.getStringFromNodeObject(FileManager.getLocale().node("chat-parrot"));

        LOCALE_CHAT_SIMILARITY = MiniUtil.getStringFromNodeObject(FileManager.getLocale().node("chat-similarity"));
        LOCALE_COMMAND_SIMILARITY = MiniUtil.getStringFromNodeObject(FileManager.getLocale().node("command-similarity"));

        LOCALE_CHAT_UPPERCASE = MiniUtil.getStringFromNodeObject(FileManager.getLocale().node("chat-uppercase"));
        LOCALE_COMMAND_UPPERCASE = MiniUtil.getStringFromNodeObject(FileManager.getLocale().node("command-uppercase"));

        LOCALE_COMMAND_CLEAR_SUCCESSFUL = MiniUtil.getStringFromNodeObject(FileManager.getLocale().node("command-clear-successful"));

        LOCALE_COMMAND_RELOAD_SUCCESSFUL = MiniUtil.getStringFromNodeObject(FileManager.getLocale().node("command-reload-successful"));
        LOCALE_COMMAND_RELOAD_UNSUCCESSFUL = MiniUtil.getStringFromNodeObject(FileManager.getLocale().node("command-reload-unsuccessful"));


        MOTD_ENABLED = FileManager.getMOTD().node("enabled").getBoolean(false);


        OPTIONS_CHAT_DELAY = MiniUtil.clampMin(FileManager.getOptions().node("chat-delay").getInt(0), 0);
        OPTIONS_COMMAND_DELAY = MiniUtil.clampMin(FileManager.getOptions().node("command-delay").getInt(0), 0);

        OPTIONS_MOVEMENT_REQUIRED_CHAT = FileManager.getOptions().node("movement-required-chat").getBoolean(false);
        OPTIONS_MOVEMENT_REQUIRED_COMMAND = FileManager.getOptions().node("movement-required-command").getBoolean(false);

        OPTIONS_CHAT_PARROTING = MiniUtil.clamp(FileManager.getOptions().node("chat-parroting").getInt(0), 0, 100);
        OPTIONS_CHAT_PARROTING_DECAY = MiniUtil.clampMin(FileManager.getOptions().node("chat-parroting-decay").getInt(5000), 0);
        OPTIONS_CHAT_PARROTING_THRESHOLD = MiniUtil.clampMin(FileManager.getOptions().node("chat-parroting-threshold").getInt(6), 0);
        OPTIONS_CHAT_PARROTING_IGNORE_USERNAMES = FileManager.getOptions().node("chat-parroting-ignore-usernames").getBoolean(false);
        OPTIONS_CHAT_PARROTING_IGNORE_LIST = MiniUtil.getStringListFromNode(FileManager.getOptions().node("chat-parroting-ignore-list"));

        OPTIONS_CHAT_SIMILARITY = MiniUtil.clamp(FileManager.getOptions().node("chat-similarity").getInt(0), 0, 100);
        OPTIONS_CHAT_SIMILARITY_THRESHOLD = MiniUtil.clampMin(FileManager.getOptions().node("chat-similarity-threshold").getInt(6), 0);
        OPTIONS_CHAT_SIMILARITY_IGNORE_USERNAMES = FileManager.getOptions().node("chat-similarity-ignore-usernames").getBoolean(false);
        OPTIONS_CHAT_SIMILARITY_IGNORE_LIST = MiniUtil.getStringListFromNode(FileManager.getOptions().node("chat-similarity-ignore-list"));
        OPTIONS_COMMAND_SIMILARITY = MiniUtil.clamp(FileManager.getOptions().node("command-similarity").getInt(0), 0, 100);
        OPTIONS_COMMAND_SIMILARITY_THRESHOLD = MiniUtil.clampMin(FileManager.getOptions().node("command-similarity-threshold").getInt(12), 0);
        OPTIONS_COMMAND_SIMILARITY_IGNORE_USERNAMES = FileManager.getOptions().node("command-similarity-ignore-usernames").getBoolean(false);
        OPTIONS_COMMAND_SIMILARITY_IGNORE_LIST = MiniUtil.getStringListFromNode(FileManager.getOptions().node("command-similarity-ignore-list"));

        OPTIONS_CHAT_UPPERCASE = MiniUtil.clamp(FileManager.getOptions().node("chat-uppercase").getInt(0), 0, 100);
        OPTIONS_CHAT_UPPERCASE_THRESHOLD = MiniUtil.clampMin(FileManager.getOptions().node("chat-uppercase-threshold").getInt(6), 0);
        OPTIONS_CHAT_UPPERCASE_ACTION = FileManager.getOptions().node("chat-uppercase-action").getString("block");
        OPTIONS_CHAT_UPPERCASE_IGNORE_USERNAMES = FileManager.getOptions().node("chat-uppercase-ignore-usernames").getBoolean(false);
        OPTIONS_CHAT_UPPERCASE_IGNORE_LIST = MiniUtil.getStringListFromNode(FileManager.getOptions().node("chat-uppercase-ignore-list"));
        OPTIONS_COMMAND_UPPERCASE = MiniUtil.clamp(FileManager.getOptions().node("command-uppercase").getInt(0), 0, 100);
        OPTIONS_COMMAND_UPPERCASE_THRESHOLD = MiniUtil.clampMin(FileManager.getOptions().node("command-uppercase-threshold").getInt(12), 0);
        OPTIONS_COMMAND_UPPERCASE_ACTION = FileManager.getOptions().node("command-uppercase-action").getString("normalize");
        OPTIONS_COMMAND_UPPERCASE_IGNORE_USERNAMES = FileManager.getOptions().node("command-uppercase-ignore-usernames").getBoolean(false);
        OPTIONS_COMMAND_UPPERCASE_IGNORE_LIST = MiniUtil.getStringListFromNode(FileManager.getOptions().node("command-uppercase-ignore-list"));


        RULES_ENABLED = FileManager.getRules().node("enabled").getBoolean(false);
        RULES_STRIP_DIACRITICAL_MARKS = FileManager.getRules().node("strip-diacritical-marks").getBoolean(false);
    }
}