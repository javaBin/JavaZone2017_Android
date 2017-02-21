/*
 * Copyright 2014 Google Inc. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package no.javazone.androidapp.v1.util;

import android.util.Log;

import com.google.firebase.crash.FirebaseCrash;

import no.javazone.androidapp.v1.BuildConfig;

public class LogUtils {
    private static final String LOG_PREFIX = "iosched_";
    private static final int LOG_PREFIX_LENGTH = LOG_PREFIX.length();
    private static final int MAX_LOG_TAG_LENGTH = 23;

    public static String ERRORLOG = "Error";
    public static String DEBUGLOG = "Debug";
    public static String VERBOSELOG = "Verbose";
    public static String INFOLOG = "Info";
    public static String WARNINGLOG = "Warning";

    public static boolean LOGGING_ENABLED = !BuildConfig.BUILD_TYPE.equalsIgnoreCase("release");

    public static String makeLogTag(String str) {
        if (str.length() > MAX_LOG_TAG_LENGTH - LOG_PREFIX_LENGTH) {
            return LOG_PREFIX + str.substring(0, MAX_LOG_TAG_LENGTH - LOG_PREFIX_LENGTH - 1);
        }

        return LOG_PREFIX + str;
    }

    /**
     * Don't use this when obfuscating class names!
     */
    public static String makeLogTag(Class cls) {
        return makeLogTag(cls.getSimpleName());
    }

    public static void LOGD(final String tag, String message) {
        if (LOGGING_ENABLED){
            if (Log.isLoggable(tag, Log.DEBUG)) {
                Log.d(tag, message);
            }
        }
    }

    public static void LOGD(final String tag, String message, Throwable cause) {
        if (LOGGING_ENABLED){
            if (Log.isLoggable(tag, Log.DEBUG)) {
                Log.d(tag, message, cause);
            }
        }
    }

    public static void LOGV(final String tag, String message) {
        if (LOGGING_ENABLED) {
            if (Log.isLoggable(tag, Log.VERBOSE)) {
                Log.v(tag, message);
            }
        }
    }

    public static void LOGV(final String tag, String message, Throwable cause) {
        if (LOGGING_ENABLED) {
            if (Log.isLoggable(tag, Log.VERBOSE)) {
                Log.v(tag, message, cause);
            }
        }
    }

    public static void LOGI(final String tag, String message) {
        if (LOGGING_ENABLED) {
            Log.i(tag, message);
        }
    }

    public static void LOGI(final String tag, String message, Throwable cause) {
        if (LOGGING_ENABLED) {
            Log.i(tag, message, cause);
        }
    }

    public static void LOGW(final String tag, String message) {
        Log.w(tag, message);
    }

    public static void LOGW(final String tag, String message, Throwable cause) {
        Log.w(tag, message, cause);
    }

    public static void LOGE(final String tag, String message) {
        Log.e(tag, message);
    }

    public static void LOGE(final String tag, String message, Throwable cause) {
        Log.e(tag, message, cause);
    }

    public static void log(final String logType, final String tag, String message) {
        logToConsole(logType, tag, message);
        FirebaseCrash.log(tag + ": " + message);
    }

    public static void log(final String logType, final String tag, String message, Throwable cause) {
        logToConsole(logType, tag, message, cause);
        FirebaseCrash.log(tag + ": " + message);
        if (cause != null) {
            FirebaseCrash.report(cause);
        }
    }

    private static void logToConsole(final String type, final String tag, String message) {
        switch(type) {
            case "Error":
                LOGE(tag, message);
                break;
            case "Debug":
                LOGD(tag, message);
                break;
            case "Verbose":
                LOGV(tag, message);
                break;
            case "Info":
                LOGI(tag, message);
                break;
            case "Warning":
                LOGW(tag, message);
                break;
            default:
                break;
        }
    }

    private static void logToConsole(final String type, final String tag, String message, Throwable cause) {
        switch(type) {
            case "Error":
                LOGE(tag, message, cause);
            break;
            case "Debug":
                LOGD(tag, message, cause);
                break;
            case "Verbose":
                LOGV(tag, message, cause);
                break;
            case "Info":
                LOGI(tag, message, cause);
                break;
            case "Warning":
                LOGW(tag, message, cause);
                break;
            default:
                break;
        }
    }

    private LogUtils() {
    }
}
