import com.example.BuildConfig;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class BuildConfigTest {
  @Test
  public void test() {
    assertEquals("enable-properties", BuildConfig.NAME);
    assertEquals("com.example", BuildConfig.GROUP);
    assertEquals("1.0", BuildConfig.VERSION);
  }
}
