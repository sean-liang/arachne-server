package arachne.server.repository;

import arachne.server.domain.stats.JobStats;

public interface JobStatsLogRepositoryCustom {

    boolean persistOnDirty(String keyField, String key, JobStats stat);

}
