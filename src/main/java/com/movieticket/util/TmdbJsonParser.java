package com.movieticket.util;

import java.util.ArrayList;
import java.util.List;

/**
 * TmdbJsonParser.java — Utility class to manually parse TMDB JSON responses.
 * 
 * Avoids adding a new dependency (like Jackson/Gson) to keep the project lightweight,
 * mirroring the approach used in OmdbService.
 */
public class TmdbJsonParser {

    /**
     * Extracts a string field value from a JSON object.
     * 
     * @param json The JSON string.
     * @param key  The key to search for (e.g., "title").
     * @return The string value, or null if not found.
     */
    public static String field(String json, String key) {
        if (json == null || key == null) return null;
        
        String searchKey = "\"" + key + "\":";
        int index = json.indexOf(searchKey);
        if (index == -1) return null;
        
        int valueStart = index + searchKey.length();
        
        // Skip leading whitespace
        while (valueStart < json.length() && Character.isWhitespace(json.charAt(valueStart))) {
            valueStart++;
        }
        
        if (valueStart >= json.length()) return null;
        
        char firstChar = json.charAt(valueStart);
        if (firstChar == '"') {
            // String value
            int startQuote = valueStart + 1;
            int endQuote = startQuote;
            boolean inEscape = false;
            
            while (endQuote < json.length()) {
                char c = json.charAt(endQuote);
                if (inEscape) {
                    inEscape = false;
                } else if (c == '\\') {
                    inEscape = true;
                } else if (c == '"') {
                    break;
                }
                endQuote++;
            }
            
            if (endQuote < json.length()) {
                String val = json.substring(startQuote, endQuote);
                // Unescape basic sequences
                return val.replace("\\\"", "\"").replace("\\\\", "\\").replace("\\n", "\n");
            }
        } else if (firstChar == '{' || firstChar == '[') {
            // Object or Array value
            int endBracket = findMatchingBracket(json, valueStart);
            if (endBracket != -1) {
                return json.substring(valueStart, endBracket + 1);
            }
        } else {
            // Number, boolean, or null
            int endVal = valueStart;
            while (endVal < json.length() && 
                   json.charAt(endVal) != ',' && 
                   json.charAt(endVal) != '}' && 
                   json.charAt(endVal) != ']' && 
                   !Character.isWhitespace(json.charAt(endVal))) {
                endVal++;
            }
            String val = json.substring(valueStart, endVal).trim();
            if ("null".equals(val)) return null;
            return val;
        }
        
        return null;
    }

    /**
     * Extracts a list of string values from a JSON array of strings.
     * 
     * @param arrayJson The JSON array string (e.g., '["a", "b", "c"]').
     * @return A list of string values.
     */
    public static List<String> stringArray(String arrayJson) {
        List<String> list = new ArrayList<>();
        if (arrayJson == null || !arrayJson.startsWith("[") || !arrayJson.endsWith("]")) {
            return list;
        }
        
        int i = 1;
        while (i < arrayJson.length() - 1) {
            while (i < arrayJson.length() - 1 && Character.isWhitespace(arrayJson.charAt(i))) {
                i++;
            }
            if (i >= arrayJson.length() - 1) break;
            
            if (arrayJson.charAt(i) == '"') {
                int start = i + 1;
                int end = start;
                boolean inEscape = false;
                while (end < arrayJson.length() - 1) {
                    char c = arrayJson.charAt(end);
                    if (inEscape) {
                        inEscape = false;
                    } else if (c == '\\') {
                        inEscape = true;
                    } else if (c == '"') {
                        break;
                    }
                    end++;
                }
                if (end < arrayJson.length() - 1) {
                    String val = arrayJson.substring(start, end).replace("\\\"", "\"").replace("\\\\", "\\");
                    list.add(val);
                }
                i = end + 1;
            } else {
                i++;
            }
            
            while (i < arrayJson.length() - 1 && arrayJson.charAt(i) != ',') {
                i++;
            }
            if (i < arrayJson.length() - 1 && arrayJson.charAt(i) == ',') {
                i++;
            }
        }
        return list;
    }

    /**
     * Splits a JSON array of objects into a list of individual JSON object strings.
     * 
     * @param arrayBody The JSON array string (e.g., '[{"id":1}, {"id":2}]').
     * @return A list of JSON object strings.
     */
    public static List<String> splitObjects(String arrayBody) {
        List<String> objects = new ArrayList<>();
        if (arrayBody == null || !arrayBody.startsWith("[") || !arrayBody.endsWith("]")) {
            return objects;
        }
        
        int i = 1;
        while (i < arrayBody.length() - 1) {
            while (i < arrayBody.length() - 1 && Character.isWhitespace(arrayBody.charAt(i))) {
                i++;
            }
            if (i >= arrayBody.length() - 1) break;
            
            if (arrayBody.charAt(i) == '{') {
                int endObj = findMatchingBracket(arrayBody, i);
                if (endObj != -1) {
                    objects.add(arrayBody.substring(i, endObj + 1));
                    i = endObj + 1;
                } else {
                    break;
                }
            } else {
                i++;
            }
            
            while (i < arrayBody.length() - 1 && arrayBody.charAt(i) != ',') {
                i++;
            }
            if (i < arrayBody.length() - 1 && arrayBody.charAt(i) == ',') {
                i++;
            }
        }
        
        return objects;
    }

    /**
     * Finds the index of the matching closing bracket '}' or ']' for the bracket at startIndex.
     */
    private static int findMatchingBracket(String text, int startIndex) {
        if (startIndex >= text.length()) return -1;
        
        char openBracket = text.charAt(startIndex);
        char closeBracket;
        if (openBracket == '{') closeBracket = '}';
        else if (openBracket == '[') closeBracket = ']';
        else return -1;
        
        int count = 1;
        boolean inString = false;
        boolean inEscape = false;
        
        for (int i = startIndex + 1; i < text.length(); i++) {
            char c = text.charAt(i);
            
            if (inString) {
                if (inEscape) {
                    inEscape = false;
                } else if (c == '\\') {
                    inEscape = true;
                } else if (c == '"') {
                    inString = false;
                }
            } else {
                if (c == '"') {
                    inString = true;
                } else if (c == openBracket) {
                    count++;
                } else if (c == closeBracket) {
                    count--;
                    if (count == 0) {
                        return i;
                    }
                }
            }
        }
        return -1;
    }
}
