import my.website.Build;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class BuildConfigTest {

    @Test
    public void main() {
        assertEquals(Build.NAME, "Hello world!");
        assertEquals(Build.GROUP, "my.website");
        assertEquals(Build.VERSION, "2.0");
        assertEquals(Build.DEBUG, true);
    }
}