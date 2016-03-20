package edu.iastate.libcompat.util;

import java.util.logging.ConsoleHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by nishanthsivakumar on 3/20/16.
 */
public class LoggingUtility {

    public static void setLoggerLevel(Logger LOGGER, Level level){

        Handler[] handlers = LOGGER.getHandlers();
        ConsoleHandler consoleHandler = null;
        for(Handler handle: handlers){
            if(handle instanceof ConsoleHandler){
                consoleHandler = (ConsoleHandler) handle;
            }
        }
        if(consoleHandler == null){
            consoleHandler = new ConsoleHandler();
        }
        consoleHandler.setLevel(level);
        LOGGER.addHandler(consoleHandler);
        LOGGER.setLevel(level);

    }

}
