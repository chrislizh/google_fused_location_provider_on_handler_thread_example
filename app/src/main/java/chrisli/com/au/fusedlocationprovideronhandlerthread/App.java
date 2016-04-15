package chrisli.com.au.fusedlocationprovideronhandlerthread;

import android.util.Log;

/**
 * Created by cli on 15/04/2016.
 */
public class App {
    private static final String logTag_ = App.class.getSimpleName();

    //log level stuff, useful for debugging - Chris Li
    public final static int LOG_NONE    = 0;
    public final static int LOG_VERBOSE = 1;
    public final static int LOG_DEBUG   = 2;
    public final static int LOG_INFO    = 3;
    public final static int LOG_WARN    = 4;
    public final static int LOG_ERROR   = 5;

    public static int SHOW_LOG_LEVEL = LOG_ERROR;

    public static boolean isShowLogVerbose() {return SHOW_LOG_LEVEL > 0;}
    public static boolean isShowLogDebug  () {return SHOW_LOG_LEVEL > 1;}
    public static boolean isShowLogInfo   () {return SHOW_LOG_LEVEL > 2;}
    public static boolean isShowLogWarn   () {return SHOW_LOG_LEVEL > 3;}
    public static boolean isShowLogError  () {return SHOW_LOG_LEVEL > 4;}

    //Description: a function to handle the dynamic logging message for debugging
    //Author: Chris Li
    public static void log(final Integer logLevel, String logTag, String message) {
        //the order matters
        if (SHOW_LOG_LEVEL > LOG_NONE) {
            StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
            String callerName = stackTrace[3].getMethodName(); // Has to be 3
            message = callerName + "(): " + message;

            if(logLevel != null && logLevel > LOG_NONE) {
                switch (logLevel) {
                    case LOG_VERBOSE:
                        if (isShowLogVerbose()) {
                            Log.v(logTag, message);
                        }
                        break;
                    case LOG_DEBUG  :
                        if (isShowLogDebug()) {
                            Log.d(logTag, message);
                        }
                        break;
                    case LOG_INFO   :
                        if (isShowLogInfo()) {
                            Log.i(logTag, message);
                        }
                        break;
                    case LOG_WARN   :
                        if (isShowLogWarn()) {
                            Log.w(logTag, message);
                        }
                        break;
                    case LOG_ERROR  :
                    default:
                        if (isShowLogError()) {
                            Log.e(logTag, message);
                        }
                        break;
                }
            } else {
                if (isShowLogError()) {
                    Log.e(logTag, message);
                } else if (isShowLogWarn()) {
                    Log.w(logTag, message);
                } else if (isShowLogInfo()) {
                    Log.i(logTag, message);
                } else if (isShowLogDebug()) {
                    Log.d(logTag, message);
                } else if (isShowLogVerbose()) {
                    Log.v(logTag, message);
                }
            }
        }
    }
}
