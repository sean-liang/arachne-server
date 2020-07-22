package arachne.server.scripting;

import arachne.server.domain.target.pipe.AbstractTargetPipe;
import arachne.server.domain.target.pipe.DropFeedbackException;
import lombok.val;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@SpringBootTest
public class GraalvmScriptEngineTest {

    @Autowired
    private GraalvmScriptEngine engine;

    @Test
    void testUsage() {
        val script = new BufferedReader(new InputStreamReader(GraalvmScriptEngineTest.class.getResourceAsStream("/test-scripting-target-pipe.js")))
                .lines().collect(Collectors.joining("\n"));
        val pipe = engine.createObject(script, AbstractTargetPipe.class, null);
        try {
            pipe.instance().initialize();
            pipe.instance().destroy();
            Stream<Object> stream =  pipe.instance().proceed(Stream.of(1,2,3), null, null);
            System.out.println(stream.collect(Collectors.toList()));
        } catch (DropFeedbackException e) {
            e.printStackTrace();
        } finally {
            pipe.close();
        }
    }

}
