package engine.util;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class LoggerTest
{
    static final Logger LOGGER = Logger.getLogger();
    
    @Test
    void getLogger()
    {
        Logger logger = Logger.getLogger();
        Assertions.assertNotNull(logger);
        Assertions.assertSame(LoggerTest.LOGGER, logger);
    }
    
    @Test
    void severe()
    {
        LoggerTest.LOGGER.severe("Single Object");
        LoggerTest.LOGGER.severe("Object 1", "Object 2", "Object 3");
        LoggerTest.LOGGER.severe("%s|%s|%s", "Object 1", "Object 2", "Object 3");
    }
    
    @Test
    void warning()
    {
        LoggerTest.LOGGER.warning("Single Object");
        LoggerTest.LOGGER.warning("Object 1", "Object 2", "Object 3");
        LoggerTest.LOGGER.warning("%s|%s|%s", "Object 1", "Object 2", "Object 3");
    }
    
    @Test
    void info()
    {
        LoggerTest.LOGGER.info("Single Object");
        LoggerTest.LOGGER.info("Object 1", "Object 2", "Object 3");
        LoggerTest.LOGGER.info("%s|%s|%s", "Object 1", "Object 2", "Object 3");
    }
    
    @Test
    void debug()
    {
        LoggerTest.LOGGER.debug("Single Object");
        LoggerTest.LOGGER.debug("Object 1", "Object 2", "Object 3");
        LoggerTest.LOGGER.debug("%s|%s|%s", "Object 1", "Object 2", "Object 3");
    }
    
    @Test
    void trace()
    {
        LoggerTest.LOGGER.trace("Single Object");
        LoggerTest.LOGGER.trace("Object 1", "Object 2", "Object 3");
        LoggerTest.LOGGER.trace("%s|%s|%s", "Object 1", "Object 2", "Object 3");
    }
    
    @Test
    void colorCodes()
    {
        LoggerTest.LOGGER.info("%s%s", Logger.BLACK, "BLACK");
        LoggerTest.LOGGER.info("%s%s", Logger.RED, "RED");
        LoggerTest.LOGGER.info("%s%s", Logger.GREEN, "GREEN");
        LoggerTest.LOGGER.info("%s%s", Logger.YELLOW, "YELLOW");
        LoggerTest.LOGGER.info("%s%s", Logger.BLUE, "BLUE");
        LoggerTest.LOGGER.info("%s%s", Logger.PURPLE, "PURPLE");
        LoggerTest.LOGGER.info("%s%s", Logger.CYAN, "CYAN");
        LoggerTest.LOGGER.info("%s%s", Logger.WHITE, "WHITE");
        
        LoggerTest.LOGGER.info("%s%s", Logger.BLACK_BOLD, "BLACK_BOLD");
        LoggerTest.LOGGER.info("%s%s", Logger.RED_BOLD, "RED_BOLD");
        LoggerTest.LOGGER.info("%s%s", Logger.GREEN_BOLD, "GREEN_BOLD");
        LoggerTest.LOGGER.info("%s%s", Logger.YELLOW_BOLD, "YELLOW_BOLD");
        LoggerTest.LOGGER.info("%s%s", Logger.BLUE_BOLD, "BLUE_BOLD");
        LoggerTest.LOGGER.info("%s%s", Logger.PURPLE_BOLD, "PURPLE_BOLD");
        LoggerTest.LOGGER.info("%s%s", Logger.CYAN_BOLD, "CYAN_BOLD");
        LoggerTest.LOGGER.info("%s%s", Logger.WHITE_BOLD, "WHITE_BOLD");
        
        LoggerTest.LOGGER.info("%s%s", Logger.BLACK_UNDERLINED, "BLACK_UNDERLINED");
        LoggerTest.LOGGER.info("%s%s", Logger.RED_UNDERLINED, "RED_UNDERLINED");
        LoggerTest.LOGGER.info("%s%s", Logger.GREEN_UNDERLINED, "GREEN_UNDERLINED");
        LoggerTest.LOGGER.info("%s%s", Logger.YELLOW_UNDERLINED, "YELLOW_UNDERLINED");
        LoggerTest.LOGGER.info("%s%s", Logger.BLUE_UNDERLINED, "BLUE_UNDERLINED");
        LoggerTest.LOGGER.info("%s%s", Logger.PURPLE_UNDERLINED, "PURPLE_UNDERLINED");
        LoggerTest.LOGGER.info("%s%s", Logger.CYAN_UNDERLINED, "CYAN_UNDERLINED");
        LoggerTest.LOGGER.info("%s%s", Logger.WHITE_UNDERLINED, "WHITE_UNDERLINED");
        
        LoggerTest.LOGGER.info("%s%s", Logger.BLACK_BACKGROUND, "BLACK_BACKGROUND");
        LoggerTest.LOGGER.info("%s%s", Logger.RED_BACKGROUND, "RED_BACKGROUND");
        LoggerTest.LOGGER.info("%s%s", Logger.GREEN_BACKGROUND, "GREEN_BACKGROUND");
        LoggerTest.LOGGER.info("%s%s", Logger.YELLOW_BACKGROUND, "YELLOW_BACKGROUND");
        LoggerTest.LOGGER.info("%s%s", Logger.BLUE_BACKGROUND, "BLUE_BACKGROUND");
        LoggerTest.LOGGER.info("%s%s", Logger.PURPLE_BACKGROUND, "PURPLE_BACKGROUND");
        LoggerTest.LOGGER.info("%s%s", Logger.CYAN_BACKGROUND, "CYAN_BACKGROUND");
        LoggerTest.LOGGER.info("%s%s", Logger.WHITE_BACKGROUND, "WHITE_BACKGROUND");
        
        LoggerTest.LOGGER.info("%s%s", Logger.BLACK_BRIGHT, "BLACK_BRIGHT");
        LoggerTest.LOGGER.info("%s%s", Logger.RED_BRIGHT, "RED_BRIGHT");
        LoggerTest.LOGGER.info("%s%s", Logger.GREEN_BRIGHT, "GREEN_BRIGHT");
        LoggerTest.LOGGER.info("%s%s", Logger.YELLOW_BRIGHT, "YELLOW_BRIGHT");
        LoggerTest.LOGGER.info("%s%s", Logger.BLUE_BRIGHT, "BLUE_BRIGHT");
        LoggerTest.LOGGER.info("%s%s", Logger.PURPLE_BRIGHT, "PURPLE_BRIGHT");
        LoggerTest.LOGGER.info("%s%s", Logger.CYAN_BRIGHT, "CYAN_BRIGHT");
        LoggerTest.LOGGER.info("%s%s", Logger.WHITE_BRIGHT, "WHITE_BRIGHT");
        
        LoggerTest.LOGGER.info("%s%s", Logger.BLACK_BOLD_BRIGHT, "BLACK_BOLD_BRIGHT");
        LoggerTest.LOGGER.info("%s%s", Logger.RED_BOLD_BRIGHT, "RED_BOLD_BRIGHT");
        LoggerTest.LOGGER.info("%s%s", Logger.GREEN_BOLD_BRIGHT, "GREEN_BOLD_BRIGHT");
        LoggerTest.LOGGER.info("%s%s", Logger.YELLOW_BOLD_BRIGHT, "YELLOW_BOLD_BRIGHT");
        LoggerTest.LOGGER.info("%s%s", Logger.BLUE_BOLD_BRIGHT, "BLUE_BOLD_BRIGHT");
        LoggerTest.LOGGER.info("%s%s", Logger.PURPLE_BOLD_BRIGHT, "PURPLE_BOLD_BRIGHT");
        LoggerTest.LOGGER.info("%s%s", Logger.CYAN_BOLD_BRIGHT, "CYAN_BOLD_BRIGHT");
        LoggerTest.LOGGER.info("%s%s", Logger.WHITE_BOLD_BRIGHT, "WHITE_BOLD_BRIGHT");
        
        LoggerTest.LOGGER.info("%s%s", Logger.BLACK_BACKGROUND_BRIGHT, "BLACK_BACKGROUND_BRIGHT");
        LoggerTest.LOGGER.info("%s%s", Logger.RED_BACKGROUND_BRIGHT, "RED_BACKGROUND_BRIGHT");
        LoggerTest.LOGGER.info("%s%s", Logger.GREEN_BACKGROUND_BRIGHT, "GREEN_BACKGROUND_BRIGHT");
        LoggerTest.LOGGER.info("%s%s", Logger.YELLOW_BACKGROUND_BRIGHT, "YELLOW_BACKGROUND_BRIGHT");
        LoggerTest.LOGGER.info("%s%s", Logger.BLUE_BACKGROUND_BRIGHT, "BLUE_BACKGROUND_BRIGHT");
        LoggerTest.LOGGER.info("%s%s", Logger.PURPLE_BACKGROUND_BRIGHT, "PURPLE_BACKGROUND_BRIGHT");
        LoggerTest.LOGGER.info("%s%s", Logger.CYAN_BACKGROUND_BRIGHT, "CYAN_BACKGROUND_BRIGHT");
        LoggerTest.LOGGER.info("%s%s", Logger.WHITE_BACKGROUND_BRIGHT, "WHITE_BACKGROUND_BRIGHT");
    }
}