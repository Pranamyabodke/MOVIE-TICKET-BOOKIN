package com.movieticket.util;

import java.util.*;

/**
 * Lightweight, zero-dependency JSON parser and serializer.
 * Supports Maps, Lists, Strings, Numbers, Booleans, and Null.
 */
public class SimpleJson {

    // ==========================================
    // SERIALIZER (Object -> JSON String)
    // ==========================================
    public static String stringify(Object obj) {
        StringBuilder sb = new StringBuilder();
        serializeValue(obj, sb);
        return sb.toString();
    }

    @SuppressWarnings("unchecked")
    private static void serializeValue(Object obj, StringBuilder sb) {
        if (obj == null) {
            sb.append("null");
        } else if (obj instanceof String) {
            sb.append('"').append(escapeString((String) obj)).append('"');
        } else if (obj instanceof Number || obj instanceof Boolean) {
            sb.append(obj.toString());
        } else if (obj instanceof Map) {
            Map<String, Object> map = (Map<String, Object>) obj;
            sb.append('{');
            boolean first = true;
            for (Map.Entry<String, Object> entry : map.entrySet()) {
                if (!first) sb.append(',');
                first = false;
                sb.append('"').append(escapeString(entry.getKey())).append("\":");
                serializeValue(entry.getValue(), sb);
            }
            sb.append('}');
        } else if (obj instanceof Iterable) {
            Iterable<?> list = (Iterable<?>) obj;
            sb.append('[');
            boolean first = true;
            for (Object item : list) {
                if (!first) sb.append(',');
                first = false;
                serializeValue(item, sb);
            }
            sb.append(']');
        } else if (obj.getClass().isArray()) {
            Object[] arr = (Object[]) obj;
            sb.append('[');
            for (int i = 0; i < arr.length; i++) {
                if (i > 0) sb.append(',');
                serializeValue(arr[i], sb);
            }
            sb.append(']');
        } else {
            // Fallback: treat as string
            sb.append('"').append(escapeString(obj.toString())).append('"');
        }
    }

    private static String escapeString(String s) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            switch (c) {
                case '"': sb.append("\\\""); break;
                case '\\': sb.append("\\\\"); break;
                case '\b': sb.append("\\b"); break;
                case '\f': sb.append("\\f"); break;
                case '\n': sb.append("\\n"); break;
                case '\r': sb.append("\\r"); break;
                case '\t': sb.append("\\t"); break;
                default:
                    if (c < 32 || c >= 127) {
                        sb.append(String.format("\\u%04x", (int) c));
                    } else {
                        sb.append(c);
                    }
            }
        }
        return sb.toString();
    }

    // ==========================================
    // PARSER (JSON String -> Map/List/Value)
    // ==========================================
    public static Object parse(String json) {
        if (json == null) return null;
        json = json.trim();
        if (json.isEmpty()) return null;
        Parser parser = new Parser(json);
        return parser.parseValue();
    }

    @SuppressWarnings("unchecked")
    public static Map<String, Object> parseObject(String json) {
        Object result = parse(json);
        if (result instanceof Map) {
            return (Map<String, Object>) result;
        }
        return new LinkedHashMap<>();
    }

    @SuppressWarnings("unchecked")
    public static List<Object> parseArray(String json) {
        Object result = parse(json);
        if (result instanceof List) {
            return (List<Object>) result;
        }
        return new ArrayList<>();
    }

    private static class Parser {
        private final String src;
        private int idx = 0;

        public Parser(String src) {
            this.src = src;
        }

        private void skipWhitespace() {
            while (idx < src.length()) {
                char c = src.charAt(idx);
                if (c == ' ' || c == '\t' || c == '\n' || c == '\r') {
                    idx++;
                } else {
                    break;
                }
            }
        }

        private char peek() {
            skipWhitespace();
            if (idx >= src.length()) return '\0';
            return src.charAt(idx);
        }

        private char next() {
            skipWhitespace();
            if (idx >= src.length()) return '\0';
            return src.charAt(idx++);
        }

        public Object parseValue() {
            skipWhitespace();
            char c = peek();
            if (c == '{') return parseObject();
            if (c == '[') return parseArray();
            if (c == '"') return parseString();
            if (c == 't' || c == 'f') return parseBoolean();
            if (c == 'n') return parseNull();
            if (c == '-' || Character.isDigit(c)) return parseNumber();
            throw new RuntimeException("Unexpected character '" + c + "' at index " + idx);
        }

        private Map<String, Object> parseObject() {
            Map<String, Object> map = new LinkedHashMap<>();
            next(); // consume '{'
            skipWhitespace();
            if (peek() == '}') {
                next();
                return map;
            }

            while (true) {
                skipWhitespace();
                if (peek() != '"') {
                    throw new RuntimeException("Expected string key in object at index " + idx);
                }
                String key = parseString();
                skipWhitespace();
                char colon = next();
                if (colon != ':') {
                    throw new RuntimeException("Expected ':' after key '" + key + "' at index " + idx);
                }
                Object value = parseValue();
                map.put(key, value);

                skipWhitespace();
                char sep = peek();
                if (sep == ',') {
                    next(); // consume ','
                } else if (sep == '}') {
                    next(); // consume '}'
                    break;
                } else {
                    throw new RuntimeException("Expected ',' or '}' at index " + idx);
                }
            }
            return map;
        }

        private List<Object> parseArray() {
            List<Object> list = new ArrayList<>();
            next(); // consume '['
            skipWhitespace();
            if (peek() == ']') {
                next();
                return list;
            }

            while (true) {
                Object val = parseValue();
                list.add(val);
                skipWhitespace();
                char sep = peek();
                if (sep == ',') {
                    next();
                } else if (sep == ']') {
                    next();
                    break;
                } else {
                    throw new RuntimeException("Expected ',' or ']' at index " + idx);
                }
            }
            return list;
        }

        private String parseString() {
            next(); // consume opening '"'
            StringBuilder sb = new StringBuilder();
            while (idx < src.length()) {
                char c = src.charAt(idx++);
                if (c == '"') {
                    return sb.toString();
                } else if (c == '\\') {
                    if (idx >= src.length()) break;
                    char esc = src.charAt(idx++);
                    switch (esc) {
                        case '"': sb.append('"'); break;
                        case '\\': sb.append('\\'); break;
                        case '/': sb.append('/'); break;
                        case 'b': sb.append('\b'); break;
                        case 'f': sb.append('\f'); break;
                        case 'n': sb.append('\n'); break;
                        case 'r': sb.append('\r'); break;
                        case 't': sb.append('\t'); break;
                        case 'u':
                            if (idx + 4 <= src.length()) {
                                String hex = src.substring(idx, idx + 4);
                                idx += 4;
                                sb.append((char) Integer.parseInt(hex, 16));
                            }
                            break;
                        default:
                            sb.append(esc);
                    }
                } else {
                    sb.append(c);
                }
            }
            return sb.toString();
        }

        private Boolean parseBoolean() {
            if (src.startsWith("true", idx)) {
                idx += 4;
                return Boolean.TRUE;
            } else if (src.startsWith("false", idx)) {
                idx += 5;
                return Boolean.FALSE;
            }
            throw new RuntimeException("Expected boolean at index " + idx);
        }

        private Object parseNull() {
            if (src.startsWith("null", idx)) {
                idx += 4;
                return null;
            }
            throw new RuntimeException("Expected null at index " + idx);
        }

        private Number parseNumber() {
            int start = idx;
            if (peek() == '-') idx++;
            while (idx < src.length() && Character.isDigit(src.charAt(idx))) {
                idx++;
            }
            boolean isFloating = false;
            if (idx < src.length() && src.charAt(idx) == '.') {
                isFloating = true;
                idx++;
                while (idx < src.length() && Character.isDigit(src.charAt(idx))) {
                    idx++;
                }
            }
            if (idx < src.length() && (src.charAt(idx) == 'e' || src.charAt(idx) == 'E')) {
                isFloating = true;
                idx++;
                if (idx < src.length() && (src.charAt(idx) == '+' || src.charAt(idx) == '-')) {
                    idx++;
                }
                while (idx < src.length() && Character.isDigit(src.charAt(idx))) {
                    idx++;
                }
            }
            String numStr = src.substring(start, idx);
            if (isFloating) {
                return Double.parseDouble(numStr);
            } else {
                long val = Long.parseLong(numStr);
                if (val >= Integer.MIN_VALUE && val <= Integer.MAX_VALUE) {
                    return (int) val;
                }
                return val;
            }
        }
    }
}

