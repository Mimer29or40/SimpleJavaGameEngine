package engine.util.logging;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.*;
import java.util.function.Supplier;
import java.util.regex.Pattern;

public class Logger
{
    private static final Map<String, Logger> LOGGER_MAP = new HashMap<>();
    
    public static @NotNull  Level                  GLOBAL_LEVEL    = Level.INFO;
    public static final     List<@NotNull Handler> GLOBAL_HANDLERS = new ArrayList<>();
    public static @Nullable Filter                 GLOBAL_FILTER   = null;
    
    static
    {
        Handler consoleHandler = new Handler.Stream(System.out);
        
        Logger.GLOBAL_HANDLERS.add(consoleHandler);
    }
    
    public static Logger getLogger()
    {
        StackTraceElement[] elements = Thread.currentThread().getStackTrace();
        
        String name = elements.length > 2 ? elements[2].getClassName() : "";
        
        return Logger.LOGGER_MAP.computeIfAbsent(name, Logger::new);
    }
    
    // -------------------- Instance -------------------- //
    
    public final String name;
    
    public @Nullable Level                  level    = null;
    public final     List<@NotNull Handler> handlers = new ArrayList<>();
    public @Nullable Filter                 filter   = null;
    
    private Logger(String name)
    {
        this.name = name;
    }
    
    @Override
    public String toString()
    {
        return "Logger{" + this.name + '}';
    }
    
    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    public boolean isLoggable(@NotNull Level level)
    {
        int logLevel = Logger.GLOBAL_LEVEL.value;
        if (this.level != null) logLevel = Math.max(logLevel, this.level.value);
        return level.value >= logLevel;
    }
    
    private void logImpl(@NotNull Record record)
    {
        if (this.filter != null && !this.filter.isLoggable(record)) return;
        if (Logger.GLOBAL_FILTER != null && !Logger.GLOBAL_FILTER.isLoggable(record)) return;
        
        for (Handler handler : this.handlers)
        {
            handler.publish(record);
            handler.flush();
        }
        for (Handler handler : Logger.GLOBAL_HANDLERS)
        {
            handler.publish(record);
            handler.flush();
        }
    }
    
    /**
     * Logs the object at the level specified.
     * <p>
     * The objects will be printed with {@link #toString(Object)} to
     * expand arrays and print the stacktrace for
     * {@link Throwable Throwable's}.
     * <p>
     * If the first object is a string that contains
     * {@link java.util.Formatter} codes, then it will be used to format the
     * objects. Arrays will be converted to strings with
     * {@link #toString(Object)} before being formatted.
     *
     * @param level  The level to log at.
     * @param object The object to log.
     */
    public void log(@NotNull Level level, Object object)
    {
        if (!isLoggable(level)) return;
        Record record = new Record(this.name, level, toString(object));
        logImpl(record);
    }
    
    /**
     * Logs the objects separated by spaces at the level specified.
     * <p>
     * The objects will be printed with {@link #toString(Object)} to
     * expand arrays and print the stacktrace for
     * {@link Throwable Throwable's}.
     * <p>
     * If the first object is a string that contains
     * {@link java.util.Formatter} codes, then it will be used to format the
     * objects. Arrays will be converted to strings with
     * {@link #toString(Object)} before being formatted.
     *
     * @param level   The level to log at.
     * @param objects The objects to log.
     */
    public void log(@NotNull Level level, Object... objects)
    {
        if (!isLoggable(level)) return;
        int n = objects.length;
        if (n == 0) return;
        StringBuilder message = new StringBuilder();
        if (objects[0] instanceof String format)
        {
            if (isFormatterString(format))
            {
                Object[] transformed = new Object[n - 1];
                for (int i = 1; i < n; i++) transformed[i - 1] = transformObject(objects[i]);
                message.append(String.format(format, transformed));
            }
            else
            {
                message.append(toString(format));
                for (int i = 1; i < n; i++) message.append(' ').append(toString(objects[i]));
            }
        }
        else
        {
            message.append(toString(objects[0]));
            for (int i = 1; i < n; i++) message.append(' ').append(toString(objects[i]));
        }
        Record record = new Record(this.name, level, message.toString());
        logImpl(record);
    }
    
    /**
     * Logs the object at {@link Level#SEVERE}.
     * <p>
     * The objects will be printed with {@link #toString(Object)} to
     * expand arrays and print the stacktrace for
     * {@link Throwable Throwable's}.
     * <p>
     * If the first object is a string that contains
     * {@link java.util.Formatter} codes, then it will be used to format the
     * objects. Arrays will be converted to strings with
     * {@link #toString(Object)} before being formatted.
     *
     * @param object The object to log.
     */
    public void severe(Object object)
    {
        log(Level.SEVERE, object);
    }
    
    /**
     * Logs the objects separated by spaces at {@link Level#SEVERE}.
     * <p>
     * The objects will be printed with {@link #toString(Object)} to
     * expand arrays and print the stacktrace for
     * {@link Throwable Throwable's}.
     * <p>
     * If the first object is a string that contains
     * {@link java.util.Formatter} codes, then it will be used to format the
     * objects. Arrays will be converted to strings with
     * {@link #toString(Object)} before being formatted.
     *
     * @param objects The objects to log.
     */
    public void severe(Object... objects)
    {
        log(Level.SEVERE, objects);
    }
    
    /**
     * Logs the object at {@link Level#WARNING}.
     * <p>
     * The objects will be printed with {@link #toString(Object)} to
     * expand arrays and print the stacktrace for
     * {@link Throwable Throwable's}.
     * <p>
     * If the first object is a string that contains
     * {@link java.util.Formatter} codes, then it will be used to format the
     * objects. Arrays will be converted to strings with
     * {@link #toString(Object)} before being formatted.
     *
     * @param object The object to log.
     */
    public void warning(Object object)
    {
        log(Level.WARNING, object);
    }
    
    /**
     * Logs the objects separated by spaces at {@link Level#WARNING}.
     * <p>
     * The objects will be printed with {@link #toString(Object)} to
     * expand arrays and print the stacktrace for
     * {@link Throwable Throwable's}.
     * <p>
     * If the first object is a string that contains
     * {@link java.util.Formatter} codes, then it will be used to format the
     * objects. Arrays will be converted to strings with
     * {@link #toString(Object)} before being formatted.
     *
     * @param objects The objects to log.
     */
    public void warning(Object... objects)
    {
        log(Level.WARNING, objects);
    }
    
    /**
     * Logs the object at {@link Level#INFO}.
     * <p>
     * The objects will be printed with {@link #toString(Object)} to
     * expand arrays and print the stacktrace for
     * {@link Throwable Throwable's}.
     * <p>
     * If the first object is a string that contains
     * {@link java.util.Formatter} codes, then it will be used to format the
     * objects. Arrays will be converted to strings with
     * {@link #toString(Object)} before being formatted.
     *
     * @param object The object to log.
     */
    public void info(Object object)
    {
        log(Level.INFO, object);
    }
    
    /**
     * Logs the objects separated by spaces at {@link Level#INFO}.
     * <p>
     * The objects will be printed with {@link #toString(Object)} to
     * expand arrays and print the stacktrace for
     * {@link Throwable Throwable's}.
     * <p>
     * If the first object is a string that contains
     * {@link java.util.Formatter} codes, then it will be used to format the
     * objects. Arrays will be converted to strings with
     * {@link #toString(Object)} before being formatted.
     *
     * @param objects The objects to log.
     */
    public void info(Object... objects)
    {
        log(Level.INFO, objects);
    }
    
    /**
     * Logs the object at {@link Level#DEBUG}.
     * <p>
     * The objects will be printed with {@link #toString(Object)} to
     * expand arrays and print the stacktrace for
     * {@link Throwable Throwable's}.
     * <p>
     * If the first object is a string that contains
     * {@link java.util.Formatter} codes, then it will be used to format the
     * objects. Arrays will be converted to strings with
     * {@link #toString(Object)} before being formatted.
     *
     * @param object The object to log.
     */
    public void debug(Object object)
    {
        log(Level.DEBUG, object);
    }
    
    /**
     * Logs the objects separated by spaces at {@link Level#DEBUG}.
     * <p>
     * The objects will be printed with {@link #toString(Object)} to
     * expand arrays and print the stacktrace for
     * {@link Throwable Throwable's}.
     * <p>
     * If the first object is a string that contains
     * {@link java.util.Formatter} codes, then it will be used to format the
     * objects. Arrays will be converted to strings with
     * {@link #toString(Object)} before being formatted.
     *
     * @param objects The objects to log.
     */
    public void debug(Object... objects)
    {
        log(Level.DEBUG, objects);
    }
    
    /**
     * Logs the object at {@link Level#TRACE}.
     * <p>
     * The objects will be printed with {@link #toString(Object)} to
     * expand arrays and print the stacktrace for
     * {@link Throwable Throwable's}.
     * <p>
     * If the first object is a string that contains
     * {@link java.util.Formatter} codes, then it will be used to format the
     * objects. Arrays will be converted to strings with
     * {@link #toString(Object)} before being formatted.
     *
     * @param object The object to log.
     */
    public void trace(Object object)
    {
        log(Level.TRACE, object);
    }
    
    /**
     * Logs the objects separated by spaces at {@link Level#TRACE}.
     * <p>
     * The objects will be printed with {@link #toString(Object)} to
     * expand arrays and print the stacktrace for
     * {@link Throwable Throwable's}.
     * <p>
     * If the first object is a string that contains
     * {@link java.util.Formatter} codes, then it will be used to format the
     * objects. Arrays will be converted to strings with
     * {@link #toString(Object)} before being formatted.
     *
     * @param objects The objects to log.
     */
    public void trace(Object... objects)
    {
        log(Level.TRACE, objects);
    }
    
    // -------------------- Utility Methods -------------------- //
    
    private static final Pattern PATTERN = Pattern.compile("%(\\d+\\$)?([-#+ 0,(<]*)?(\\d+)?(\\.\\d+)?([tT])?([a-zA-Z%])"); // Taken from java.lang.Formatter
    
    private static boolean isFormatterString(CharSequence input)
    {
        return Logger.PATTERN.matcher(input).find();
    }
    
    private static @NotNull String toString(@Nullable Object obj)
    {
        if (obj == null) return "null";
        if (obj instanceof Throwable value)
        {
            final StringWriter sw = new StringWriter();
            value.printStackTrace(new PrintWriter(sw));
            return sw.getBuffer().toString();
        }
        if (obj.getClass().isArray())
        {
            if (obj instanceof boolean[] value) return Arrays.toString(value);
            if (obj instanceof byte[] value) return Arrays.toString(value);
            if (obj instanceof short[] value) return Arrays.toString(value);
            if (obj instanceof char[] value) return Arrays.toString(value);
            if (obj instanceof int[] value) return Arrays.toString(value);
            if (obj instanceof long[] value) return Arrays.toString(value);
            if (obj instanceof float[] value) return Arrays.toString(value);
            if (obj instanceof double[] value) return Arrays.toString(value);
            return Arrays.deepToString((Object[]) obj);
        }
        if (obj instanceof Boolean value) return Boolean.toString(value);
        if (obj instanceof Character value) return Character.toString(value);
        if (obj instanceof Number)
        {
            if (obj instanceof Byte value) return Byte.toString(value);
            if (obj instanceof Short value) return Short.toString(value);
            if (obj instanceof Integer value) return Integer.toString(value);
            if (obj instanceof Long value) return Long.toString(value);
            if (obj instanceof Float value) return Float.toString(value);
            if (obj instanceof Double value) return Double.toString(value);
        }
        if (obj instanceof Supplier<?> supplier) return toString(supplier.get());
        return String.valueOf(obj);
    }
    
    private static Object transformObject(Object obj)
    {
        if (obj instanceof Throwable) return toString(obj);
        if (obj != null && obj.getClass().isArray()) return toString(obj);
        if (obj instanceof Supplier<?> supplier) return transformObject(supplier.get());
        return obj;
    }
    
    // -------------------- Console Format Codes -------------------- //
    
    // Reset
    public static final String RESET = "\033[0m";
    
    // Regular Colors
    public static final String BLACK  = "\033[0;30m";
    public static final String RED    = "\033[0;31m";
    public static final String GREEN  = "\033[0;32m";
    public static final String YELLOW = "\033[0;33m";
    public static final String BLUE   = "\033[0;34m";
    public static final String PURPLE = "\033[0;35m";
    public static final String CYAN   = "\033[0;36m";
    public static final String WHITE  = "\033[0;37m";
    
    // Bold
    public static final String BLACK_BOLD  = "\033[1;30m";
    public static final String RED_BOLD    = "\033[1;31m";
    public static final String GREEN_BOLD  = "\033[1;32m";
    public static final String YELLOW_BOLD = "\033[1;33m";
    public static final String BLUE_BOLD   = "\033[1;34m";
    public static final String PURPLE_BOLD = "\033[1;35m";
    public static final String CYAN_BOLD   = "\033[1;36m";
    public static final String WHITE_BOLD  = "\033[1;37m";
    
    // Underline
    public static final String BLACK_UNDERLINED  = "\033[4;30m";
    public static final String RED_UNDERLINED    = "\033[4;31m";
    public static final String GREEN_UNDERLINED  = "\033[4;32m";
    public static final String YELLOW_UNDERLINED = "\033[4;33m";
    public static final String BLUE_UNDERLINED   = "\033[4;34m";
    public static final String PURPLE_UNDERLINED = "\033[4;35m";
    public static final String CYAN_UNDERLINED   = "\033[4;36m";
    public static final String WHITE_UNDERLINED  = "\033[4;37m";
    
    // Background
    public static final String BLACK_BACKGROUND  = "\033[40m";
    public static final String RED_BACKGROUND    = "\033[41m";
    public static final String GREEN_BACKGROUND  = "\033[42m";
    public static final String YELLOW_BACKGROUND = "\033[43m";
    public static final String BLUE_BACKGROUND   = "\033[44m";
    public static final String PURPLE_BACKGROUND = "\033[45m";
    public static final String CYAN_BACKGROUND   = "\033[46m";
    public static final String WHITE_BACKGROUND  = "\033[47m";
    
    // High Intensity
    public static final String BLACK_BRIGHT  = "\033[0;90m";
    public static final String RED_BRIGHT    = "\033[0;91m";
    public static final String GREEN_BRIGHT  = "\033[0;92m";
    public static final String YELLOW_BRIGHT = "\033[0;93m";
    public static final String BLUE_BRIGHT   = "\033[0;94m";
    public static final String PURPLE_BRIGHT = "\033[0;95m";
    public static final String CYAN_BRIGHT   = "\033[0;96m";
    public static final String WHITE_BRIGHT  = "\033[0;97m";
    
    // Bold High Intensity
    public static final String BLACK_BOLD_BRIGHT  = "\033[1;90m";
    public static final String RED_BOLD_BRIGHT    = "\033[1;91m";
    public static final String GREEN_BOLD_BRIGHT  = "\033[1;92m";
    public static final String YELLOW_BOLD_BRIGHT = "\033[1;93m";
    public static final String BLUE_BOLD_BRIGHT   = "\033[1;94m";
    public static final String PURPLE_BOLD_BRIGHT = "\033[1;95m";
    public static final String CYAN_BOLD_BRIGHT   = "\033[1;96m";
    public static final String WHITE_BOLD_BRIGHT  = "\033[1;97m";
    
    // High Intensity backgrounds
    public static final String BLACK_BACKGROUND_BRIGHT  = "\033[0;100m";
    public static final String RED_BACKGROUND_BRIGHT    = "\033[0;101m";
    public static final String GREEN_BACKGROUND_BRIGHT  = "\033[0;102m";
    public static final String YELLOW_BACKGROUND_BRIGHT = "\033[0;103m";
    public static final String BLUE_BACKGROUND_BRIGHT   = "\033[0;104m";
    public static final String PURPLE_BACKGROUND_BRIGHT = "\033[0;105m";
    public static final String CYAN_BACKGROUND_BRIGHT   = "\033[0;106m";
    public static final String WHITE_BACKGROUND_BRIGHT  = "\033[0;107m";
}
