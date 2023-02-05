package engine.util.logging;

import org.jetbrains.annotations.NotNull;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public abstract class Formatter
{
    public abstract @NotNull List<String> format(@NotNull Record record);
    
    public static final class Simple extends Formatter
    {
        private static final Pattern LINE_SPLIT = Pattern.compile("(\\n|\\n\\r|\\r\\n)");
        
        public @NotNull String prefixFormat  = "[%1$tY-%1$tm-%1$td %1$tH:%1$tM:%1$tS.%1$tL] [%2$s/%3$s] [%4$s]";
        public @NotNull String messageFormat = "%s: %s%n";
        
        @Override
        public @NotNull List<String> format(@NotNull Record record)
        {
            ZonedDateTime zdt    = ZonedDateTime.ofInstant(record.instant, ZoneId.systemDefault());
            String        thread = record.thread;
            Level         level  = record.level;
            String        name   = record.logger;
            
            String prefix = String.format(this.prefixFormat, zdt, thread, level, name);
            
            List<String> lines = new ArrayList<>();
            for (String line : Simple.LINE_SPLIT.split(record.message))
            {
                lines.add(String.format(this.messageFormat, prefix, line));
            }
            
            return lines;
        }
    }
}
