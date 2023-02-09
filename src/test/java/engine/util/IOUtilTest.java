package engine.util;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;

class IOUtilTest
{
    static Path output_dir;
    
    @BeforeAll
    static void beforeAll() throws IOException
    {
        IOUtilTest.output_dir = IOUtil.getPath("out").toAbsolutePath();
        try
        {
            Files.createDirectory(IOUtilTest.output_dir);
        }
        catch (FileAlreadyExistsException ignored) {}
    }
    
    @Test
    void getPath()
    {
        String resource;
        Path   path;
        
        resource = "util/TestFile.txt";
        path     = IOUtil.getPath(resource);
        Assertions.assertTrue(path.toString().replace('\\', '/').endsWith(resource));
        Assertions.assertTrue(path.toFile().exists());
        
        resource = "relative/file/path.txt";
        path     = IOUtil.getPath(resource);
        Assertions.assertEquals(path.toString().replace('\\', '/'), resource);
        Assertions.assertFalse(path.toFile().exists());
        
        resource = "C:/absolute/file/path.txt";
        path     = IOUtil.getPath(resource);
        Assertions.assertEquals(path.toString().replace('\\', '/'), resource);
        Assertions.assertFalse(path.toFile().exists());
    }
    
    @Test
    void readFromFile()
    {
        Path       file      = IOUtil.getPath("util/TestFile.txt");
        String     expected  = "Test File\0";
        ByteBuffer data;
        int[]      bytesRead = new int[1];
        
        data = IOUtil.readFromFile(file);
        Assertions.assertNotNull(data);
        Assertions.assertEquals(expected, StandardCharsets.UTF_8.decode(data).toString());
        
        data = IOUtil.readFromFile(file, bytesRead, ByteBuffer::allocateDirect);
        Assertions.assertNotNull(data);
        Assertions.assertEquals(9, bytesRead[0]);
        Assertions.assertEquals(expected, StandardCharsets.UTF_8.decode(data).toString());
    }
    
    @Test
    void writeToFile() throws IOException
    {
        Path file = IOUtil.getPath("out/WrittenFile.txt").toAbsolutePath();
        
        byte[] data     = new byte[] {0, 1, 2, 3, 4, 5, 6, 7, 8, 9};
        byte[] readData = new byte[data.length];
        
        ByteBuffer dataBuffer = ByteBuffer.wrap(data);
        ByteBuffer readDataBuffer;
        
        int[] bytesWritten = new int[1];
        
        Files.deleteIfExists(file);
        
        boolean result;
        
        result = IOUtil.writeToFile(file, dataBuffer.clear());
        Assertions.assertTrue(result);
        Assertions.assertTrue(file.toFile().exists());
        
        readDataBuffer = IOUtil.readFromFile(file);
        Assertions.assertNotNull(readDataBuffer);
        readDataBuffer.get(readData);
        Assertions.assertArrayEquals(data, readData);
        
        result = IOUtil.writeToFile(file, dataBuffer.clear(), bytesWritten);
        Assertions.assertTrue(result);
        Assertions.assertEquals(10, bytesWritten[0]);
        Assertions.assertTrue(file.toFile().exists());
        
        Files.deleteIfExists(file);
    }
    
    @Test
    void getExtension()
    {
        String path, actual;
        
        path   = "util/TestFile.txt";
        actual = IOUtil.getExtension(path);
        Assertions.assertEquals(".txt", actual);
        
        path   = "util/TestFile.txt";
        actual = IOUtil.getExtension(path);
        Assertions.assertNotEquals("txt", actual);
        
        path   = "util/TestFile.tar.gz";
        actual = IOUtil.getExtension(path);
        Assertions.assertEquals(".gz", actual);
    }
}