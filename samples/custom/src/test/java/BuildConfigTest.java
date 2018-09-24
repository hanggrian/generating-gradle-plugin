import my.website.BuildConfig;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class BuildConfigTest {

    @Test
    public void main() {
        assertEquals(BuildConfig.NAME, "Hello world!");
        assertEquals(BuildConfig.GROUP, "my.website");
        assertEquals(BuildConfig.VERSION, "2.0");
        assertEquals(BuildConfig.DEBUG, true);
    }
}