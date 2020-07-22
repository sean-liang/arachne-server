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

    @Indexed
    private List<String> tags;

    @Indexed(unique = true)
    private String token;

    private WorkerEngine engine;

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
