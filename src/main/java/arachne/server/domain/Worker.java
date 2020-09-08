package arachne.server.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.Version;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

/**
 * Worker usually represents a crawler worker that fetch contents.
 */
@Document(collection = "sys_workers")
@Data
@Builder
@Accessors(chain = true)
@NoArgsConstructor
@AllArgsConstructor
public class Worker implements DomainEntity {

    private static final long serialVersionUID = 1L;

    @Id
    private String id;

    @Indexed(unique = true)
    private String name;

    /**
     * Tags for easy grouping and use in frontend.
     */
    @Indexed
    private List<String> tags;

    /**
     * Worker use id and token to indentify and authenticate itself.
     */
    @Indexed(unique = true)
    private String token;

    /**
     * Communication protocol, eg. Http, WebSocket.
     */
    private WorkerProtocol protocol;

    /**
     * Managed workers will get jobs from server, and those jobs are tracked.
     * Unmanaged workers only push results back to server.
     */
    @Indexed
    private boolean managed;

    /**
     * For a managed worker, batch size is the maximum number of jobs it can get from server in single request. For unmanaged workers, it will be ignored.
     */
    private int batchSize;

    @Indexed
    private WorkerStatus status;

    @CreatedDate
    private long createdAt;

    @LastModifiedBy
    private long updatedAt;

    @JsonIgnore
    @Version
    private Long version;

}
