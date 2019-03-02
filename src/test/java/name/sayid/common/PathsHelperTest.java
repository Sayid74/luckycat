package name.sayid.common;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class PathsHelperTest {
	Path testPath = null;
	@Before
	public void before() throws Exception
	{
		Path p = Paths.get("p1", "p2", "p3");
		Files.createDirectories(p);
		System.out.println("create dir: " + p);
		p = Paths.get("p1", "hello 1.txt");
		Files.write(p, "hello 1".getBytes(StandardCharsets.UTF_8));
		System.out.println("write file: " + p);
		p = Paths.get("p1", "hello 2.txt");
		Files.write(p, "Hello 2".getBytes(StandardCharsets.UTF_8));
		System.out.println("write file: " + p);
		p = Paths.get("p1", "p2", "p4");
		Files.createDirectories(p);
		System.out.println("create dir: " + p);
		p = Paths.get("p1", "p2", "p4", "p5");
		Files.write(p, "Hello 5".getBytes(StandardCharsets.UTF_8));
		System.out.println("write file: " + p);
		testPath = Paths.get("p1");
	}

	@Test
	public void rmForceTest() throws Exception
	{
		Assert.assertNotNull(testPath);
		Assert.assertTrue(testPath.toFile().exists());
		PathsHelper.rmForce(testPath);
		Assert.assertFalse(testPath.toFile().exists());
	}

}

