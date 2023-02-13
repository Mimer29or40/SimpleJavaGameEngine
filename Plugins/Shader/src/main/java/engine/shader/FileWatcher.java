package engine.shader;

import engine.util.IOUtil;
import engine.util.Logger;

import java.io.IOException;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicBoolean;

public class FileWatcher extends Thread
{
    //private static final Logger LOGGER = Logger.getLogger();
    //
    //private WatchService watchService;
    //
    //private final AtomicBoolean stop = new AtomicBoolean(false);
    //
    //private final List<Path> watchedFiles = new ArrayList<>();
    //
    //private final ConcurrentLinkedQueue<String> changedFiles = new ConcurrentLinkedQueue<>();
    //
    //public FileWatcher()
    //{
    //    super(null, null, "ShaderFileWatcher", 0);
    //
    //    setDaemon(true);
    //    start();
    //}
    //
    //public void join()
    //{
    //
    //}
    //
    //public void stopThread()
    //{
    //    this.stop.set(true);
    //}
    //
    //public Collection<String> changedFiles()
    //{
    //    Collection<String> changedFiles = this.changedFiles.stream().toList();
    //    this.changedFiles.clear();
    //    return changedFiles;
    //}
    //
    //public void addFile(String file)
    //{
    //    Path filePath  = IOUtil.getPath(file);
    //    Path parentDir = filePath.toAbsolutePath().getParent();
    //    if (this.dirs.contains(parentDir)) return;
    //    try
    //    {
    //        parentDir.register(this.watchService, StandardWatchEventKinds.ENTRY_MODIFY);
    //        this.watchedFiles.add(filePath);
    //        this.dirs.add(parentDir);
    //        this.changedFiles.add(file);
    //    }
    //    catch (IOException e)
    //    {
    //        FileWatcher.LOGGER.severe("Could not watch file: ", file);
    //    }
    //}
    //
    //@Override
    //public void run()
    //{
    //    try
    //    {
    //        this.watchService = FileSystems.getDefault().newWatchService();
    //
    //        while (!this.stop.get())
    //        {
    //            final WatchKey wk = this.watchService.take();
    //            for (WatchEvent<?> event : wk.pollEvents())
    //            {
    //                WatchEvent.Kind<?> kind = event.kind();
    //                if (kind == StandardWatchEventKinds.OVERFLOW)
    //                {
    //                    Thread.yield();
    //                }
    //                else if (kind == StandardWatchEventKinds.ENTRY_MODIFY)
    //                {
    //                    final Path changed = (Path) event.context();
    //                    for (Path watchedFile : this.watchedFiles)
    //                    {
    //                        if (changed.endsWith(watchedFile.getFileName().toString()))
    //                        {
    //                            this.changedFiles.add(watchedFile.toString());
    //                        }
    //                    }
    //                }
    //            }
    //            if (!wk.reset()) break;
    //            Thread.yield();
    //        }
    //    }
    //    catch (Throwable e)
    //    {
    //        FileWatcher.LOGGER.severe(e);
    //    }
    //    finally
    //    {
    //        try
    //        {
    //            if (this.watchService != null) this.watchService.close();
    //        }
    //        catch (Throwable e)
    //        {
    //            FileWatcher.LOGGER.severe(e);
    //        }
    //    }
    //}
}
