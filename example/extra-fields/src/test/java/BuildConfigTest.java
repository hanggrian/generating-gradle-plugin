import com.example.BuildConfig;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class BuildConfigTest {

    @Test
    public void main() {
        assertEquals(BuildConfig.APP_NAME, "extra-fields");
        assertEquals(BuildConfig.GROUP_ID, "com.example");
        assertEquals(BuildConfig.VERSION, "1.0");

        assertEquals(BuildConfig.A_STRING, "Hello world!");
        assertEquals(BuildConfig.A_DOUBLE, 12.0, 0.0);
        assertEquals(BuildConfig.AN_INT, 9);
    }
}