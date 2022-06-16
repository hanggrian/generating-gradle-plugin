import my.website.CustomBuildConfig;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class BuildConfigTest {
    @Test
    public void main() {
        assertEquals(CustomBuildConfig.NAME, "Hello world!");
        assertEquals(CustomBuildConfig.GROUP, "my.website");
        assertEquals(CustomBuildConfig.VERSION, "2.0");
        assertTrue(CustomBuildConfig.DEBUG);
        assertEquals(CustomBuildConfig.A_STRING, "Hello world!");
        assertEquals(CustomBuildConfig.A_DOUBLE, 12.0, 0.0);
        assertEquals(CustomBuildConfig.AN_INT, 9);
    }
}
