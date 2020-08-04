package arachne.server.util;

import arachne.server.domain.HttpHeader;
import arachne.server.domain.HttpMethod;
import org.assertj.core.util.Lists;
import org.junit.jupiter.api.Test;

import java.io.UnsupportedEncodingException;

import static org.junit.jupiter.api.Assertions.assertEquals;

class URLFingerprintsTest {

    @Test
    void testCreateFingerPrint() throws UnsupportedEncodingException {
        assertEquals("0|http://test.com/page?b=1&a=2",
                URLFingerprints.createFingerPrint(HttpMethod.GET, null, "http://test.com/page?b=1&a=2", null));

        assertEquals("1|k1=v1,k2=v2|http://test.com/page?b=1&a=2",
                URLFingerprints.createFingerPrint(HttpMethod.POST,
                        Lists.list(new HttpHeader("k2", "v2"), new HttpHeader("k1", "v1")),
                        "http://test.com/page?b=1&a=2", null));

        assertEquals("2|k1=v1,k2=v2|http://test.com/page?b=1&a=2|somebody",
                URLFingerprints.createFingerPrint(HttpMethod.PUT,
                        Lists.list(new HttpHeader("k2", "v2"), new HttpHeader("k1", "v1")),
                        "http://test.com/page?b=1&a=2", "somebody"));
    }

}
