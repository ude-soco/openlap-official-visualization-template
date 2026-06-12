package com.openlap.template;

/**
 * Context-specific escaping helpers for visualization implementations that generate HTML and JavaScript snippets.
 */
public final class VisualizationEscaper {

    private static final char[] HEX_DIGITS = "0123456789ABCDEF".toCharArray();

    private VisualizationEscaper() {
    }

    public static String escapeHtmlText(String value) {
        return escapeHtml(value, false);
    }

    public static String escapeHtmlAttribute(String value) {
        return escapeHtml(value, true);
    }

    public static String escapeJavaScriptString(String value) {
        if (value == null) {
            return "";
        }

        StringBuilder escaped = new StringBuilder(value.length());
        for (int index = 0; index < value.length(); index++) {
            char character = value.charAt(index);
            switch (character) {
                case '\\':
                    escaped.append("\\\\");
                    break;
                case '"':
                    escaped.append("\\\"");
                    break;
                case '\'':
                    escaped.append("\\'");
                    break;
                case '\n':
                    escaped.append("\\n");
                    break;
                case '\r':
                    escaped.append("\\r");
                    break;
                case '\t':
                    escaped.append("\\t");
                    break;
                case '\b':
                    escaped.append("\\b");
                    break;
                case '\f':
                    escaped.append("\\f");
                    break;
                case '<':
                    escaped.append("\\u003C");
                    break;
                case '>':
                    escaped.append("\\u003E");
                    break;
                case '&':
                    escaped.append("\\u0026");
                    break;
                case '\u2028':
                    escaped.append("\\u2028");
                    break;
                case '\u2029':
                    escaped.append("\\u2029");
                    break;
                default:
                    if (character < 0x20) {
                        appendUnicodeEscape(escaped, character);
                    } else {
                        escaped.append(character);
                    }
                    break;
            }
        }
        return escaped.toString();
    }

    private static String escapeHtml(String value, boolean attribute) {
        if (value == null) {
            return "";
        }

        StringBuilder escaped = new StringBuilder(value.length());
        for (int index = 0; index < value.length(); index++) {
            char character = value.charAt(index);
            switch (character) {
                case '&':
                    escaped.append("&amp;");
                    break;
                case '<':
                    escaped.append("&lt;");
                    break;
                case '>':
                    escaped.append("&gt;");
                    break;
                case '"':
                    escaped.append("&quot;");
                    break;
                case '\'':
                    escaped.append("&#39;");
                    break;
                case '\n':
                    escaped.append(attribute ? "&#10;" : "\n");
                    break;
                case '\r':
                    escaped.append(attribute ? "&#13;" : "\r");
                    break;
                case '\t':
                    escaped.append(attribute ? "&#9;" : "\t");
                    break;
                case '\u2028':
                    escaped.append("&#8232;");
                    break;
                case '\u2029':
                    escaped.append("&#8233;");
                    break;
                default:
                    escaped.append(character);
                    break;
            }
        }
        return escaped.toString();
    }

    private static void appendUnicodeEscape(StringBuilder escaped, char character) {
        escaped.append("\\u");
        escaped.append(HEX_DIGITS[(character >> 12) & 0xF]);
        escaped.append(HEX_DIGITS[(character >> 8) & 0xF]);
        escaped.append(HEX_DIGITS[(character >> 4) & 0xF]);
        escaped.append(HEX_DIGITS[character & 0xF]);
    }
}
