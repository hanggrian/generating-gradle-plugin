import com.example.R;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class RTest {
  @Test
  public void test() {
    assertEquals("im", R.string.im);
    assertEquals("a", R.string.a);
    assertEquals("little", R.string.little);
    assertEquals("piggy", R.string.piggy);
  }
}
