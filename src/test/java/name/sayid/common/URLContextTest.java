package name.sayid.common;

import org.junit.Test;

import java.net.URL;

import static org.junit.Assert.*;

public class URLContextTest {

    @Test
    public void toStringTest() throws Exception
    {
        var base = new URL("http://www.sina.com.cn/");
        var pading = "../abc/bcd/index.html";
        var test = new URLContext(new URL(base, pading));
        System.out.println(test);
        assertEquals(test.toString(), "http://www.sina.com.cn/abc/bcd/index.html");
    }

    @Test
    public void makeURLTest()
            throws Exception
    {
        var base = new URL("http://www.sina.com.cn/");
        var pading = "../abc/bcd/index.html";
        var test = new URLContext(new URL(base, pading));
        var v1 = StringsHelper.similarURLEncode("http://sohu.com.cn");
        test.addQuery("url",v1);
        System.out.println(test.toString());
        test.addQuery("archive","abc.pdf");
        System.out.println(test.toString());
    }
}