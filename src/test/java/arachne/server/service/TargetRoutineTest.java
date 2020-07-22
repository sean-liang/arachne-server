package arachne.server.service;

import arachne.server.domain.JobAction;
import arachne.server.domain.Target;
import arachne.server.domain.TargetStatus;
import arachne.server.domain.target.actionprovider.AbstractTargetActionProvider;
import lombok.val;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

@SpringBootTest
public class TargetRoutineTest {

    @Autowired
    private TargetRoutine routine;

    @MockBean
    private JobStatsService jobStatsService;

    @Test
    void testRun() {
        val persistCalled = new AtomicBoolean(false);
        val target1 = this.mockedTarget(persistCalled);
        when(target1.getStatus()).thenReturn(TargetStatus.SCHEDULED);
        when(target1.getNextRunAt()).thenReturn(1L);
        this.routine.handleTarget(target1, 2);
        verify(target1, times(1)).start();
        assertTrue(persistCalled.get());

        persistCalled.set(false);
        val target2 = this.mockedTarget(persistCalled);
        when(target2.getStatus()).thenReturn(TargetStatus.RUNNING);
        when(target2.getNextRunAt()).thenReturn(1L);
        this.routine.handleTarget(target2, 2);
        verify(target2, never()).start();
        assertTrue(persistCalled.get());

        persistCalled.set(false);
        val target3 = this.mockedTarget(persistCalled);
        when(target3.getStatus()).thenReturn(TargetStatus.SCHEDULED);
        when(target3.getNextRunAt()).thenReturn(2L);
        this.routine.handleTarget(target3, 2);
        verify(target3, times(1)).start();
        assertTrue(persistCalled.get());

        persistCalled.set(false);
        val target4 = this.mockedTarget(persistCalled);
        when(target4.getStatus()).thenReturn(TargetStatus.SCHEDULED);
        when(target4.getNextRunAt()).thenReturn(3L);
        this.routine.handleTarget(target4, 2);
        verify(target4, never()).start();
        assertTrue(persistCalled.get());
    }

    Target mockedTarget(final AtomicBoolean persistCalled) {
        val target = Mockito.mock(Target.class);
        when(target.getProvider()).thenReturn(new AbstractTargetActionProvider() {
            @Override
            public JobAction provide() {
                return null;
            }

            @Override
            public void persistOnDirty() {
                persistCalled.set(true);
            }
        });
        return target;
    }

}
