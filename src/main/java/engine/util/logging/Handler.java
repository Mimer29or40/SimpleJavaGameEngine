package engine.util.logging;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.List;

public abstract class Handler
{
    /**
     * OPEN_FAILURE is used when an {@code open} of an output stream fails.
     */
    public static final int OPEN_FAILURE   = 0;
    /**
     * WRITE_FAILURE is used when a {@code write} to an output stream fails.
     */
    public static final int WRITE_FAILURE  = 1;
    /**
     * FLUSH_FAILURE is used when a {@code flush} to an output stream fails.
     */
    public static final int FLUSH_FAILURE  = 2;
    /**
     * FORMAT_FAILURE is used when formatting fails for any reason.
     */
    public static final int FORMAT_FAILURE = 3;
    
    public @Nullable Filter    filter    = null;
    public @NotNull  Formatter formatter = new Formatter.Simple();
    public @Nullable Level     level     = null;
    
    private boolean errorReported = false;
    
    public abstract void publish(@NotNull Record record);
    
    public abstract void flush();
    
    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    public boolean isLoggable(@NotNull Record record)
    {
        int logLevel = Logger.GLOBAL_LEVEL.value;
        if (this.level != null) logLevel = Math.max(logLevel, this.level.value);
        if (record.level.value < logLevel) return false;
        return this.filter == null || this.filter.isLoggable(record);
    }
    
    @SuppressWarnings("SameParameterValue")
    protected void reportError(String msg, Exception ex, int code)
    {
        try
        {
            if (this.errorReported)
            {
                // We only report the first error, to avoid clogging
                // the screen.
                return;
            }
            this.errorReported = true;
            String text = "java.util.logging.ErrorManager: " + code;
            if (msg != null)
            {
                text = text + ": " + msg;
            }
            System.err.println(text);
            if (ex != null)
            {
                ex.printStackTrace();
            }
        }
        catch (Exception ex2)
        {
            System.err.println("Handler.reportError caught:");
            ex2.printStackTrace();
        }
    }
    
    public static class Stream extends Handler
    {
        private final Writer writer;
        
        public Stream(OutputStream output)
        {
            this.writer = new OutputStreamWriter(output);
        }
        
        @Override
        public void publish(@NotNull Record record)
        {
            if (!isLoggable(record)) return;
            List<String> lines;
            try
            {
                lines = this.formatter.format(record);
            }
            catch (Exception ex)
            {
                // We don't want to throw an exception here, but we
                // report the exception to any registered ErrorManager.
                reportError(null, ex, Handler.FORMAT_FAILURE);
                return;
            }
            
            try
            {
                String prefix = null;
                String suffix = null;
                if (record.level.value >= Level.SEVERE.value)
                {
                    prefix = Logger.RED;
                    suffix = Logger.RESET;
                }
                else if (record.level.value >= Level.WARNING.value)
                {
                    prefix = Logger.YELLOW;
                    suffix = Logger.RESET;
                }
                for (String line : lines)
                {
                    if (prefix != null) this.writer.write(prefix);
                    this.writer.write(line);
                    if (suffix != null) this.writer.write(suffix);
                }
            }
            catch (Exception ex)
            {
                // We don't want to throw an exception here, but we
                // report the exception to any registered ErrorManager.
                reportError(null, ex, Handler.WRITE_FAILURE);
            }
        }
        
        @Override
        public void flush()
        {
            try
            {
                this.writer.flush();
            }
            catch (Exception ex)
            {
                // We don't want to throw an exception here, but we
                // report the exception to any registered ErrorManager.
                reportError(null, ex, Handler.FLUSH_FAILURE);
            }
        }
    }
    
    // TODO - File Handler
}
