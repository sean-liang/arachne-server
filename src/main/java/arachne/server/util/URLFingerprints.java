package arachne.server.util;

import arachne.server.domain.HttpHeader;
import arachne.server.domain.HttpMethod;
import edu.uci.ics.crawler4j.url.URLCanonicalizer;
import io.micrometer.core.instrument.util.StringUtils;
import lombok.NonNull;

import java.io.UnsupportedEncodingException;
import java.util.Collections;
import java.util.List;

/**
 * Create url fingerprint.
 */
public class URLFingerprints {

    private static final char DELIMITER = '|';

    /**
     * Create url fingerprint.<br>
     * <p>
     * Format: Method|Header1,Header2|Url|Body<br>
     * Header is sorted in alphabetical order: {@link Collections#sort(List)}<br>
     * Canonicalize url with {@link edu.uci.ics.crawler4j.url.URLCanonicalizer}<br>
     *
     * @param method  http method
     * @param headers headers (Optional)
     * @param url     url
     * @param body    request body (Optional)
     * @return fingerprint
     * @throws UnsupportedEncodingException
     */
    public static String createFingerPrint(@NonNull final HttpMethod method, final List<HttpHeader> headers,
                                           @NonNull final String url, final String body) throws UnsupportedEncodingException {
        final StringBuilder fingerprint = new StringBuilder();
        // http method
        fingerprint.append(method.getValue()).append(DELIMITER);
        // headers in alphabetical order delimited by comma
        if (null != headers && !headers.isEmpty()) {
            Collections.sort(headers);
            headers.forEach(h -> fingerprint.append(h.toString()).append(','));
            fingerprint.deleteCharAt(fingerprint.length() - 1).append(DELIMITER);
        }
        // canonicalize url
        fingerprint.append(URLCanonicalizer.getCanonicalURL(url));
        // body
        if (StringUtils.isNotEmpty(body)) {
            fingerprint.append(DELIMITER).append(body);
        }
        return fingerprint.toString();
    }

}
