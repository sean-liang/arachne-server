package arachne.server.controller.admin;

import arachne.server.controller.admin.form.CreateUserForm;
import arachne.server.controller.admin.form.UpdateUserPasswordForm;
import arachne.server.domain.User;
import arachne.server.repository.UserRepository;
import arachne.server.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpSession;
import javax.validation.Valid;
import java.security.Principal;
import java.util.Collections;
import java.util.Map;

@RestController
public class AdminUserController {

    @Autowired
    private UserRepository userRepo;

    @Autowired
    @Qualifier("adminUserPasswordEncoder")
    private PasswordEncoder passwordEncoder;

    @Autowired
    private UserService userService;

    @GetMapping("/admin/token")
    public Map<String, String> token(final HttpSession session) {
        return Collections.singletonMap("token", session.getId());
    }

    @GetMapping("/admin/users/current")
    public User current(final Principal user) {
        return this.userRepo.findOneByUsername(user.getName());
    }

    @GetMapping("/admin/users")
    public Page<User> listUsers(final Pageable pageable) {
        return this.userRepo.findAll(pageable);
    }

    @PostMapping("/admin/users")
    public void createUser(@RequestBody @Valid final CreateUserForm form) {
        this.userRepo.save(new User(null, form.getUsername(), this.passwordEncoder.encode(form.getPassword())));
    }

    @PutMapping("/admin/users/chpwd/{id}")
    public void updateUserPassword(@PathVariable("id") final String id,
                                   @RequestBody @Valid final UpdateUserPasswordForm form) {
        this.userService.updateUserPassword(id, form);
    }

    @DeleteMapping("/admin/users/{id}")
    public void removeUser(@PathVariable("id") final String id) {
        this.userRepo.deleteById(id);
    }
}
