package name.sayid.common;

import java.io.File;
import java.io.IOException;
import java.math.BigInteger;
import java.net.URL;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;

public class PathsHelper {
    /**
     * It similars console unix or linux rm command with -f option.
     * It will remove the resource identified by path with power force.
     * @param path It identified a corresponding resource place.
     * @throws IOException When remove a resouce from posix file system
     * some mistake my be occurred.
     */
    public static void rmForce(Path path) throws IOException
    {
        Files.walkFileTree(path, new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult
            visitFile(Path file, BasicFileAttributes attr) throws IOException
            {
                Files.delete(file); return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult
            postVisitDirectory(Path dir, IOException e) throws IOException
            {
                if (e == null) {
                    Files.delete(dir); return FileVisitResult.CONTINUE;
                } else {
                    throw e;
                }
            }
        });
    }

    /**
     * It swaps jdk Class.getSystemResources method and puts the results
     * to a List Instance
     * @param resourceName
     * @return
     * @throws IOException
     */
    public static List<URL>
    resourceURLsOf(String resourceName) throws IOException
    {
        Enumeration<URL> urls = ClassLoader.getSystemResources(resourceName);
        ArrayList<URL> l = new ArrayList<URL>();
        if (!urls.hasMoreElements()) return List.of();
        else do {
            l.add(urls.nextElement());
        } while (!urls.hasMoreElements());
        return Collections.unmodifiableList(l);
    }

    /**
     * It gets the user.dir option from System property.
     * @return the current directory of java program running.
     */
    public static Path getRuntimeDir()
    {
        return new File(System.getProperty("user.dir")).toPath();
    }

    /**
     * Builds a string to a path object by using delimiter '/'
     * @param s It is the string will be built to a path object.
     * @return The pat object from input s.
     */
    public static Path stringToPath(String s)
    {
        return stringToPath(s, '/');
    }

    /**
     * It just like over stringToPath method. The difference is adding a new opration separatorChar.
     * The new operation identifies a delimiter, so the delimiter is not only '/' and you can custom.
     * @param s It is the string will be built to a path object.
     * @param separatorChar The customer delimiter.
     * @return The pat object from input s.
     */
    public static Path stringToPath(String s, char separatorChar)
    {
        int i = 0; StringBuilder sb = new StringBuilder();
        for (int n = s.indexOf(separatorChar); n != -1 && n < s.length(); i = n + 1, n = s.indexOf(separatorChar, i)) {
            if (i < n) sb.append(s, i, n); sb.append(File.separatorChar);
        } if (i < s.length() - 1) sb.append(s.substring(i));

        return new File(sb.toString()).toPath();
    }

    /**
     * It translates a BigInteger object to a path. The object represents a md5
     * value.
     * Every segment length(Contains characters count) in the path is valued by
     * the second parameter intervalLength.
     * @param md5
     * @param intervalLength
     * @return The pat object from input s.
     */
    public static Path md5ToPath(BigInteger md5, int intervalLength)
    {
        String md5Str = md5.toString(16);
        var items = StringsHelper.divideEqually(md5Str, intervalLength);
        return Paths.get(".", (String[]) items.toArray());
    }

    /**
     * Splits out a file extension. The file is described by the input
     * operation path.
     * @param path It should have the result extension.
     * @return The extension when the input path else return empty sting.
     */
    public String fielExtension(Path path)
    {
        var fn = path.toFile().getName();
        var p = fn.lastIndexOf('.');
        if (p < 0) return null;
        else if (p == 0) return fn;
        else return fn.substring(p);
    }
}
