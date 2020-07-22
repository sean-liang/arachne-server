package arachne.server.domain;

import arachne.server.util.URLCanonicalizer;
import arachne.server.util.URLFingerprints;
import lombok.Getter;
import lombok.NonNull;

import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.util.Collections;
import java.util.List;

public class JobAction implements Serializable {

    private static final long serialVersionUID = 1L;

    @Getter
    private final HttpMethod method;

    @Getter
    private final List<HttpHeader> headers;

    @Getter
    private final String url;

    @Getter
    private final String body;

    @Getter
    private final String fingerprint;

    public JobAction(@NonNull final String url) {
        this(HttpMethod.GET, url);
    }

    public JobAction(@NonNull final HttpMethod method, @NonNull final String url) {
        this(method, null, url, null);
    }

    public JobAction(@NonNull final HttpMethod method, final List<HttpHeader> headers, @NonNull final String url,
                     final String body) {
        try {
            this.url = URLCanonicalizer.getCanonicalURL(url, false);
            this.method = method;
            this.headers = headers;
            this.body = body;
            if (null != this.headers) {
                Collections.sort(this.headers);
            }
            this.fingerprint = URLFingerprints.createFingerPrint(method, headers, url, body);
        } catch (UnsupportedEncodingException e) {
            throw new IllegalArgumentException(e);
        }
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((fingerprint == null) ? 0 : fingerprint.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        JobAction other = (JobAction) obj;
        if (fingerprint == null) {
            return other.fingerprint == null;
        } else return fingerprint.equals(other.fingerprint);
    }

}
