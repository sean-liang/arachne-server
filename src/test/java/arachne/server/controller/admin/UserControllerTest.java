package arachne.server.controller.admin;

import arachne.server.domain.User;
import arachne.server.repository.UserRepository;
import lombok.val;
import org.bson.Document;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class UserControllerTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private PasswordEncoder encoder;

    @MockBean
    private UserRepository userRepo;

    @Test
    void testAuthentication() throws Exception {
        when(this.userRepo.findOneByUsername("admin")).thenReturn(new User("123", "admin", encoder.encode("password")));

        this.mvc.perform(get("/admin/token")).andExpect(status().is(401));

        val tokenResp = Document.parse(this.mvc
                .perform(get("/admin/token").header("X-Requested-With", "XMLHttpRequest")
                        .with(httpBasic("admin", "password")))
                .andExpect(status().is(200)).andReturn().getResponse().getContentAsString());
        val token = tokenResp.get("token").toString();
        assertNotNull(token);

        this.mvc.perform(get("/admin/token")).andExpect(status().is(401));
        this.mvc.perform(get("/admin/token").header("X-Auth-Token", token)).andExpect(status().is(200));

        this.mvc.perform(get("/admin/users/current").header("X-Auth-Token", token)).andExpect(status().is(200))
                .andExpect(content().json("{'id':'123','username':'admin'}"));

        this.mvc.perform(post("/admin/logout").with(csrf()).header("X-Auth-Token", token)).andExpect(status().is(204));
        this.mvc.perform(get("/admin/users/current").header("X-Auth-Token", token)).andExpect(status().is(401));
    }

    @Test
    void testCRUD() throws Exception {

    }

}
