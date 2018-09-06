import com.example.BuildConfig;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class BuildConfigTest {

    @Test
    public void main() {
        assertEquals(BuildConfig.APP_NAME, "simple");
        assertEquals(BuildConfig.GROUP_ID, "com.example");
        assertEquals(BuildConfig.VERSION, "1.0");
    }
}