package arachne.server.service;

import arachne.server.controller.admin.form.TargetForm;
import arachne.server.domain.Target;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Primary;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.stream.Stream;

@Service
@Primary
public class TransactionalTargetService implements TargetService {

    @Autowired
    @Qualifier("nonTransactional")
    private TargetService nonTransactionalService;

    @Override
    public Optional<Target> getById(final String id) {
        return this.nonTransactionalService.getById(id);
    }

    @Override
    public Page<Target> getTargets(final Pageable pageable) {
        return this.nonTransactionalService.getTargets(pageable);
    }

    @Override
    public Stream<Target> getTargetStream() {
        return this.nonTransactionalService.getTargetStream();
    }

    @Transactional
    @Override
    public Target addTarget(final TargetForm form) {
        return this.nonTransactionalService.addTarget(form);
    }

    @Transactional
    @Override
    public Target updateTarget(final String id, final TargetForm form) {
        return this.nonTransactionalService.updateTarget(id, form);
    }

    @Transactional
    @Override
    public Target updateStatus(String id, String status) {
        return this.nonTransactionalService.updateStatus(id, status);
    }

    @Transactional
    @Override
    public void removeTarget(final String id) {
        this.nonTransactionalService.removeTarget(id);
    }
}
