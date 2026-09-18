package com.donos.zebra.save;

import com.donos.zebra.HeadlessTestBase;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class IntroFlowTest extends HeadlessTestBase {

    @Test
    void gameplayCannotBeginBeforeIntroCompletes() {
        IntroStoryController controller = new IntroStoryController(
            new IntroStoryData(new String[]{"A", "B"}), 1f);
        assertFalse(controller.isComplete());
        controller.update(0.5f);
        assertFalse(controller.isComplete());
    }

    @Test
    void gameplayStartsAfterIntroCompletes() {
        IntroStoryController controller = new IntroStoryController(
            new IntroStoryData(new String[]{"A", "B"}), 1f);
        controller.update(1.1f);
        assertFalse(controller.isComplete());
        controller.update(1.1f);
        assertTrue(controller.isComplete());
    }

    @Test
    void skipCompletesIntroImmediately() {
        IntroStoryController controller = new IntroStoryController(new IntroStoryData(), 10f);
        assertFalse(controller.isComplete());
        controller.skip();
        assertTrue(controller.isComplete());
        assertTrue(controller.wasSkipped());
    }
}
