package arachne.server.scripting;

import arachne.server.domain.target.pipe.AbstractTargetPipe;
import arachne.server.domain.target.pipe.DropFeedbackException;
import lombok.val;
import org.graalvm.polyglot.PolyglotException;
import org.junit.jupiter.api.Test;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

public class GraalvmScriptEngineTest {

    @Test
    void testUsage() {
        val engine = new GraalvmScriptEngine();
        val script = new BufferedReader(new InputStreamReader(GraalvmScriptEngineTest.class.getResourceAsStream("/test-scripting-target-pipe.js")))
                .lines().collect(Collectors.joining("\n"));
        val pipe = engine.createObject(script, AbstractTargetPipe.class, null);
        try {
            pipe.instance().initialize();
            pipe.instance().destroy();
            val stream =  pipe.instance().proceed(Stream.of(1,2,3), null, null);
            assertEquals(Arrays.asList(2,4,6), stream.collect(Collectors.toList()));
        } catch (DropFeedbackException e) {
            e.printStackTrace();
            fail();
        }

        pipe.close();
    }

}
