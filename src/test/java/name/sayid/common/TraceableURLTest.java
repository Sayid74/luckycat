package name.sayid.common;

import org.junit.Test;

import java.net.MalformedURLException;
import java.net.URL;

import static org.junit.Assert.*;

public class TraceableURLTest {

    @Test
    public void getFitnessPath() throws MalformedURLException
    {
        String s = "http://123.11.11.22/../abc/bcd/index.html";
        var t = new TraceableURL(new URL(s));
        assertEquals("http://123.11.11.22/abc/bcd/index.html", t.toString());
    }

    @Test
    public void getDecodeQuery()
    {

    }
}