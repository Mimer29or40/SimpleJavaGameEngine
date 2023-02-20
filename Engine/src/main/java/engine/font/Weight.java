package engine.font;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public enum Weight
{
    THIN,
    EXTRA_LIGHT,
    LIGHT,
    REGULAR,
    MEDIUM,
    SEMI_BOLD,
    BOLD,
    EXTRA_BOLD,
    BLACK,
    ;
    
    private final String tag;
    
    Weight()
    {
        this.tag = format(name());
    }
    
    public String tag()
    {
        return tag;
    }
    
    public static @Nullable Weight get(@NotNull String subfamily)
    {
        String str = format(subfamily);
        for (Weight weight : Weight.values())
        {
            if (str.equals(weight.tag()) || str.contains(weight.tag())) return weight;
        }
        return null;
    }
    
    private static @NotNull String format(@NotNull String weight)
    {
        return weight.replace("_", "").toLowerCase();
    }
}
