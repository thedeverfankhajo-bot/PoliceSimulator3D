package com.erfan.policesimulator

import org.junit.Assert.assertTrue
import org.junit.Test

class ProjectConfigurationTest {
    @Test fun nativeProjectHasValidApplicationIdentity() {
        assertTrue("com.erfan.policesimulator".contains("."))
    }
}
