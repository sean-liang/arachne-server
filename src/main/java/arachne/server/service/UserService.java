package arachne.server.service;

import arachne.server.controller.admin.form.UpdateUserPasswordForm;
import arachne.server.domain.User;
import arachne.server.exceptions.ResourceNotFoundException;
import arachne.server.repository.UserRepository;
import com.mongodb.client.result.UpdateResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import static org.springframework.data.mongodb.core.query.Criteria.where;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepo;

    @Autowired
    private MongoTemplate template;

    @Autowired
    @Qualifier("adminUserPasswordEncoder")
    private PasswordEncoder passwordEncoder;

    public void updateUserPassword(final String id, final UpdateUserPasswordForm form) {
        final UpdateResult result = template.updateFirst(
                new Query(where("id").is(id).and("password").is(this.passwordEncoder.encode(form.getOldPassword()))),
                new Update().set("password", this.passwordEncoder.encode(form.getNewPassword())),
                User.class);
        if (result.getModifiedCount() != 1) {
            throw new ResourceNotFoundException();
        }
    }

}
