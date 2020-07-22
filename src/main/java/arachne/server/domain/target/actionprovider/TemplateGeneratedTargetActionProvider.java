package arachne.server.domain.target.actionprovider;

import arachne.server.domain.HttpRequestTemplate;
import arachne.server.domain.JobAction;
import arachne.server.exceptions.BadRequestException;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonTypeName;
import lombok.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.annotation.Transient;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.util.StringUtils;

@JsonTypeName("TEMPLATE_GENERATED")
@Slf4j
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class TemplateGeneratedTargetActionProvider extends AbstractTargetActionProvider {

    private static final long serialVersionUID = 1L;

    private static final String PLACE_HOLDER = "{{}}";

    private HttpRequestTemplate template;

    @Builder.Default
    private int start = 0;

    @Builder.Default
    private int end = Integer.MAX_VALUE;

    @Builder.Default
    private int step = 1;

    @Builder.Default
    private int next = 0;

    @Builder.Default
    @JsonIgnore
    @Transient
    private transient boolean dirty = true;

    @Override
    public synchronized void initialize() {
        if (this.end <= this.start) {
            this.end = Integer.MAX_VALUE;
        }
        this.reset();
        log.info("Init - start: {}, step: {}, end: {}, next: {}", this.start, this.step, this.end, this.next);
    }

    @Override
    public synchronized JobAction provide() {
        if (null == template || StringUtils.isEmpty(this.template.getUrl())) {
            throw new BadRequestException("TEMPLATE_URL_NOT_SPECIFIED");
        }

        if (this.end > this.start && this.next > this.end) {
            return null;
        }

        final int current = this.next;

        this.next += this.step;
        this.dirty = true;

        return new JobAction(this.template.getMethod(), this.template.getHeaders(),
                StringUtils.replace(this.template.getUrl(), PLACE_HOLDER, String.valueOf(current)),
                this.template.getBody());
    }

    @Override
    public synchronized void reset() {
        this.next = this.start;
        this.dirty = true;
    }

    @Override
    public synchronized void persistOnDirty() {
        if (this.dirty) {
            this.getTarget().updateFirstById(new Update().set("provider.next", this.next));
            this.dirty = false;
        }
    }

}
