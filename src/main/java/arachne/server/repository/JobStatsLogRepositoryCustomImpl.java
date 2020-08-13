package arachne.server.repository;

import arachne.server.domain.JobStatsLog;
import arachne.server.domain.stats.JobStats;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;

import static org.springframework.data.mongodb.core.query.Criteria.where;

public class JobStatsLogRepositoryCustomImpl implements JobStatsLogRepositoryCustom {

    @Autowired
    private MongoTemplate mongo;

    @Override
    public boolean persistOnDirty(final String keyField, final String key, final JobStats stat) {
        return stat.persistIfDirty(slicer -> {
            final Query query = new Query(where(keyField).is(key));
            final Update update = new Update();
            update.set("meta", slicer.getMeta());
            slicer.getWindows().forEach(window -> update.push("logs", window));
            this.mongo.upsert(query, update, JobStatsLog.class);
        });
    }

}
