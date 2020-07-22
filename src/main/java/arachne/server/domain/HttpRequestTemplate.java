package arachne.server.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HttpRequestTemplate implements Serializable {

    private static final long serialVersionUID = 1L;

    @Builder.Default
    private HttpMethod method = HttpMethod.GET;

    private List<HttpHeader> headers;

    private String url;

    private String body;

}
