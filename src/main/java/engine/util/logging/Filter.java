package engine.util.logging;

@FunctionalInterface
public interface Filter
{
    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    boolean isLoggable(Record record);
}

