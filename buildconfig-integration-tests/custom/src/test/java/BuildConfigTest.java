import my.website.CustomBuildConfig;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class BuildConfigTest {

    @Test
    public void main() {
        assertEquals(CustomBuildConfig.NAME, "Hello world!");
        assertEquals(CustomBuildConfig.GROUP, "my.website");
        assertEquals(CustomBuildConfig.VERSION, "2.0");
        assertEquals(CustomBuildConfig.DEBUG, true);
    }
}