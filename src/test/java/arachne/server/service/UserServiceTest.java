package arachne.server.service;

import arachne.server.controller.admin.form.UpdateUserPasswordForm;
import arachne.server.domain.User;
import arachne.server.exceptions.ResourceNotFoundException;
import arachne.server.repository.UserRepository;
import arachne.server.security.AdminWebSecurityConfigurer;
import lombok.val;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Import({AdminWebSecurityConfigurer.class})
public class UserServiceTest {

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository repo;

    @MockBean
    @Qualifier("adminUserPasswordEncoder")
    private PasswordEncoder encoder;

    @Test
    void testUpdateUserPassword() {
        Mockito.when((this.encoder.encode("12345678"))).thenReturn("12345678");
        Mockito.when((this.encoder.encode("abcdefg"))).thenReturn("abcdefg");

        val user = User.builder().username("user1").password("12345678").build();
        val saved = this.repo.save(user);

        this.userService.updateUserPassword(saved.getId(), UpdateUserPasswordForm.builder().oldPassword("12345678").newPassword("abcdefg").build());

        val found = this.repo.findOneByUsername("user1");
        assertNotNull(found);
        assertEquals("abcdefg", found.getPassword());
        assertThrows(
                ResourceNotFoundException.class,
                () -> this.userService.updateUserPassword(
                        saved.getId(),
                        UpdateUserPasswordForm.builder().oldPassword("12345678").newPassword("abcdefg").build()));
    }

}
