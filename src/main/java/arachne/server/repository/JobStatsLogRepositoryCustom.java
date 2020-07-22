package arachne.server.repository;

import arachne.server.domain.stats.JobStats;

public interface JobStatsLogRepositoryCustom {

    void persistOnDirty(String keyField, String key, JobStats stat);

}
