package arachne.server.util;

import java.io.UnsupportedEncodingException;
import java.net.*;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

/**
 * See http://en.wikipedia.org/wiki/URL_normalization for a reference Note: some
 * parts of the code are adapted from: http://stackoverflow.com/a/4057470/405418
 *
 * @author Yasser Ganjisaffar
 */
public class URLCanonicalizer {

    private static boolean haltOnError = false;

    public static void setHaltOnError(boolean haltOnError) {
        URLCanonicalizer.haltOnError = haltOnError;
    }

    public static String getCanonicalURL(String url, boolean reorderQuery) throws UnsupportedEncodingException {
        return getCanonicalURL(url, null, reorderQuery);
    }

    public static String getCanonicalURL(String href, String context, boolean reorderQuery)
            throws UnsupportedEncodingException {
        return getCanonicalURL(href, context, StandardCharsets.UTF_8, reorderQuery);
    }

    public static String getCanonicalURL(String href, String context, Charset charset, boolean reorderQuery)
            throws UnsupportedEncodingException {

        try {
            URL canonicalURL = new URL(UrlResolver.resolveUrl((context == null) ? "" : context, href));

            String host = canonicalURL.getHost().toLowerCase();
            if (Objects.equals(host, "")) {
                // This is an invalid Url.
                return null;
            }

            String path = canonicalURL.getPath();

            /*
             * Normalize: no empty segments (i.e., "//"), no segments equal to ".", and no
             * segments equal to ".." that are preceded by a segment not equal to "..".
             */
            path = new URI(path.replace("\\", "/").replace(String.valueOf((char) 12288), "%E3%80%80")
                    .replace(String.valueOf((char) 32), "%20")).normalize().toString();

            int idx = path.indexOf("//");
            while (idx >= 0) {
                path = path.replace("//", "/");
                idx = path.indexOf("//");
            }

            while (path.startsWith("/../")) {
                path = path.substring(3);
            }

            path = path.trim();

            Map<String, String> params = createParameterMap(canonicalURL.getQuery(), reorderQuery);
            final String queryString;
            if ((params != null) && !params.isEmpty()) {
                String canonicalParams = canonicalize(params, charset);
                queryString = (canonicalParams.isEmpty() ? "" : ("?" + canonicalParams));
            } else {
                queryString = "";
            }

            if (path.isEmpty()) {
                path = "/";
            }

            // Drop default port: example.com:80 -> example.com
            int port = canonicalURL.getPort();
            if (port == canonicalURL.getDefaultPort()) {
                port = -1;
            }

            String protocol = canonicalURL.getProtocol().toLowerCase();
            String pathAndQueryString = normalizePath(path) + queryString;

            URL result = new URL(protocol, host, port, pathAndQueryString);
            return result.toExternalForm();

        } catch (MalformedURLException | URISyntaxException ex) {
            return null;
        }
    }

    /**
     * Takes a query string, separates the constituent name-value pairs, and stores
     * them in a LinkedHashMap ordered by their original order.
     *
     * @return Null if there is no query string.
     */
    private static Map<String, String> createParameterMap(String queryString, boolean reorderQuery) {
        if ((queryString == null) || queryString.isEmpty()) {
            return null;
        }

        final String[] pairs = queryString.split("&");
        final Map<String, String> params = reorderQuery ? new HashMap<>(pairs.length)
                : new LinkedHashMap<>(pairs.length);

        for (final String pair : pairs) {
            if (pair.isEmpty()) {
                continue;
            }

            String[] tokens = pair.split("=", 2);
            switch (tokens.length) {
                case 1:
                    if (pair.charAt(0) == '=') {
                        params.put("", tokens[0]);
                    } else {
                        params.put(tokens[0], "");
                    }
                    break;
                case 2:
                    params.put(tokens[0], tokens[1]);
                    break;
            }
        }
        return new LinkedHashMap<>(params);
    }

    /**
     * Canonicalize the query string.
     *
     * @param paramsMap Parameter map whose name-value pairs are in order of
     *                  insertion.
     * @param charset   Charset of html page
     * @return Canonical form of query string.
     * @throws UnsupportedEncodingException
     */
    private static String canonicalize(Map<String, String> paramsMap, Charset charset)
            throws UnsupportedEncodingException {
        if ((paramsMap == null) || paramsMap.isEmpty()) {
            return "";
        }

        final StringBuilder sb = new StringBuilder(100);
        for (Map.Entry<String, String> pair : paramsMap.entrySet()) {
            final String key = pair.getKey().toLowerCase();
            if ("jsessionid".equals(key) || "phpsessid".equals(key) || "aspsessionid".equals(key)) {
                continue;
            }
            if (sb.length() > 0) {
                sb.append('&');
            }
            sb.append(percentEncodeRfc3986(pair.getKey(), charset));
            if (!pair.getValue().isEmpty()) {
                sb.append('=');
                sb.append(percentEncodeRfc3986(pair.getValue(), charset));
            }
        }
        return sb.toString();
    }

    private static String normalizePath(final String path) {
        return path.replace("%7E", "~").replace(" ", "%20");
    }

    private static String percentEncodeRfc3986(String string, Charset charset) throws UnsupportedEncodingException {
        try {
            string = string.replace("+", "%2B");
            string = URLDecoder.decode(string, "UTF-8");
            string = URLEncoder.encode(string, charset.name());
            return string.replace("+", "%20").replace("*", "%2A").replace("%7E", "~");
        } catch (UnsupportedEncodingException | RuntimeException e) {
            if (haltOnError) {
                throw e;
            } else {
                return string;
            }
        }
    }
}
