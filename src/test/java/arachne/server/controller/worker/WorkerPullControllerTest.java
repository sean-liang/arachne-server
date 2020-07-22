package arachne.server.controller.worker;

import arachne.server.domain.Worker;
import arachne.server.service.WorkerService;
import lombok.val;
import org.bson.Document;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class WorkerPullControllerTest {

    @Autowired
    private MockMvc mvc;

    @MockBean
    private WorkerService workerService;

    @Test
    void testAuthentication() throws Exception {
        when(this.workerService.getById("1234"))
                .thenReturn(Optional.of(Worker.builder().id("1234").token("abcd").name("test").build()));

        this.mvc.perform(get("/worker/pull/current")).andExpect(status().is(401));
        this.mvc.perform(get("/worker/pull/current?clientId=1234&token=wrong")).andExpect(status().is(401));
        this.mvc.perform(get("/worker/pull/current?clientId=1234")).andExpect(status().is(401));
        this.mvc.perform(get("/worker/pull/current?token=abcd")).andExpect(status().is(401));

        val worker = Document.parse(this.mvc.perform(get("/worker/pull/current?clientId=1234&token=abcd"))
                .andExpect(status().is(200)).andReturn().getResponse().getContentAsString());
        assertEquals("1234", worker.get("id"));
        assertEquals("abcd", worker.get("token"));
        assertEquals("test", worker.get("name"));
    }

}
