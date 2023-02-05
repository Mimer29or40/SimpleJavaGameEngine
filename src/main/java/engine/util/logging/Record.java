package engine.util.logging;

import org.jetbrains.annotations.NotNull;

import java.time.Instant;

public class Record
{
    public final String  logger;
    public final Level   level;
    public final String  message;
    public final Instant instant;
    public final String  thread;
    
    Record(@NotNull String logger, @NotNull Level level, String message)
    {
        this.logger  = logger;
        this.level   = level;
        this.message = message;
        this.instant = Instant.now();
        this.thread  = Thread.currentThread().getName();
    }
}
