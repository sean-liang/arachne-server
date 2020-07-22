package arachne.server.domain.target.pipe;

import arachne.server.scripting.GraalvmScriptEngineTest;
import lombok.val;
import org.junit.jupiter.api.Test;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.fail;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class ScriptTargetPipeTest {

    @Test
    void testUsage() throws Throwable {
        val script = new BufferedReader(new InputStreamReader(GraalvmScriptEngineTest.class.getResourceAsStream("/test-scripting-target-pipe.js")))
                .lines().collect(Collectors.joining("\n"));

        val pipe = new ScriptTargetPipe();
        pipe.setContent(script);

        pipe.initialize();
        val stream = pipe.proceed(Stream.of(1,2,3), null, null);
        assertEquals(Arrays.asList(2,4,6), stream.collect(Collectors.toList()));

        try {
            pipe.proceed(null, null, null);
            fail("Should drop exception");
        } catch(DropFeedbackException ex) {

        }

        pipe.destroy();
    }


}
