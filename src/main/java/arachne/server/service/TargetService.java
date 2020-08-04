package arachne.server.service;

import arachne.server.controller.admin.form.TargetForm;
import arachne.server.domain.Target;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.stream.Stream;

public interface TargetService {

    Optional<Target> getById(String id);

    Page<Target> getTargets(Pageable pageable);

    Stream<Target> getTargetStream();

    Target addTarget(TargetForm form);

    Target updateTarget(String id, TargetForm form);

    Target updateStatus(String id, String status);

    void removeTarget(String id);


}
