package arachne.server.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HttpHeader implements Serializable, Comparable<HttpHeader> {

    private static final long serialVersionUID = 1L;

    private String key;

    private String value;

    @Override
    public String toString() {
        return key + "=" + value;
    }

    @Override
    public int compareTo(final HttpHeader other) {
        if (other == null) {
            throw new IllegalArgumentException();
        }
        final int keyOrder = this.key.compareTo(other.key);
        return keyOrder == 0 ? this.value.compareTo(other.value) : keyOrder;
    }

}
