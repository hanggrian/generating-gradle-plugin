import my.website.CustomBuildConfig;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class BuildConfigTest {
  @Test
  public void test() {
    assertEquals("Hello world!", CustomBuildConfig.NAME);
    assertEquals("my.website", CustomBuildConfig.GROUP);
    assertEquals("2.0", CustomBuildConfig.VERSION);
    assertEquals("Hello world!", CustomBuildConfig.A_STRING);
    assertEquals(12.0, CustomBuildConfig.A_DOUBLE, 0.0);
    assertEquals((Integer) 9, CustomBuildConfig.AN_INT);
  }
}
