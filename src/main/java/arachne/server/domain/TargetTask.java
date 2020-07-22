package arachne.server.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Version;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.ArrayList;
import java.util.List;

@Document(collection = "sys_target_tasks")
@Data
@Builder
@Accessors(chain = true)
@NoArgsConstructor
@AllArgsConstructor
public class TargetTask implements DomainEntity {

    private static final long serialVersionUID = 1L;

    @Id
    private String id;

    @Indexed
    private String targetId;

    @Indexed
    @Builder.Default
    private long startTime = 0;

    @Builder.Default
    private long endTime = 0;

    @Builder.Default
    private List<TargetTaskLog> logs = new ArrayList<>();

    @JsonIgnore
    @Version
    private Long version;

}
