package arachne.server.controller.admin;

import arachne.server.controller.admin.form.TargetForm;
import arachne.server.domain.Target;
import arachne.server.exceptions.ResourceNotFoundException;
import arachne.server.service.TargetService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@RestController
@RequestMapping("/admin/targets")
public class AdminTargetController {

    @Autowired
    private TargetService targetService;

    @GetMapping("/")
    public Page<Target> listTargets(final Pageable pageable) {
        return this.targetService.getTargets(pageable);
    }

    @GetMapping("/{id}")
    public Target getTarget(@PathVariable("id") final String id) {
        return this.targetService.getById(id).orElseThrow(ResourceNotFoundException::new);
    }

    @PostMapping("/")
    public Target createTarget(@RequestBody @Valid final TargetForm form) {
        return this.targetService.addTarget(form);
    }

    @PutMapping("/{id}")
    public Target updateTarget(@PathVariable("id") final String id, @RequestBody @Valid final TargetForm form) {
        return this.targetService.updateTarget(id, form);
    }

    @PutMapping("/{id}/status/{status}")
    public Target updateStatus(@PathVariable("id") final String id, @PathVariable("status") final String status) {
        return this.targetService.updateStatus(id, status);
    }

    @DeleteMapping("/{id}")
    public void removeTarget(@PathVariable("id") final String id) {
        this.targetService.removeTarget(id);
    }

}
