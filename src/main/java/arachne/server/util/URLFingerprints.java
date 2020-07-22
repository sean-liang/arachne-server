package arachne.server.util;

import arachne.server.domain.HttpHeader;
import arachne.server.domain.HttpMethod;
import io.micrometer.core.instrument.util.StringUtils;
import lombok.NonNull;

import java.io.UnsupportedEncodingException;
import java.util.Collections;
import java.util.List;

public class URLFingerprints {

    private static final char DELIMITER = '|';

    public static String createFingerPrint(@NonNull final HttpMethod method, final List<HttpHeader> headers,
                                           @NonNull final String url, final String body) throws UnsupportedEncodingException {
        final StringBuilder fingerprint = new StringBuilder();
        fingerprint.append(method.getValue()).append(DELIMITER);
        if (null != headers && !headers.isEmpty()) {
            Collections.sort(headers);
            headers.forEach(h -> fingerprint.append(h.toString()).append(','));
            fingerprint.deleteCharAt(fingerprint.length() - 1).append(DELIMITER);
        }
        fingerprint.append(URLCanonicalizer.getCanonicalURL(url, true));
        if (StringUtils.isNotEmpty(body)) {
            fingerprint.append(DELIMITER).append(body);
        }
        return fingerprint.toString();
    }

}
