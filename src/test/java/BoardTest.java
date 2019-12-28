import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.*;

public class BoardTest {

    @Test
    public void step() {
        Map<Integer, String> m = new HashMap<>();
        m.put(1, "cruelty");
        m.put(2, "and");
        m.put(3, "the");
        m.put(4, "beast");
        System.out.println(m);
    }
}