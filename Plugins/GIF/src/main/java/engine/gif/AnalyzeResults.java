package engine.gif;

public interface AnalyzeResults
{
    byte[] pixels();
    
    byte[] colorTable();
    
    int transparentIndex();
}
