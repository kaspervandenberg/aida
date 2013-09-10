/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package nl.maastro.eureca.aida.indexer.util;

import nl.maastro.eureca.aida.indexer.util.CancelableTask;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;

/**
 *
 * @author kasper
 */
public abstract class FilesystemWatchTask extends CancelableTask {
	final Path watchedPath;
	final WatchService service;
	final WatchKey key;

	public FilesystemWatchTask(Path watchedPath_) throws IOException {
		this.watchedPath = watchedPath_;
		this.service = watchedPath.getFileSystem().newWatchService();
		this.key = this.watchedPath.register(service, StandardWatchEventKinds.ENTRY_CREATE, StandardWatchEventKinds.ENTRY_MODIFY);
	}

	protected abstract void createIndexTask(Path file);

	@Override
	public void run() {
		while (!isCancelled()) {
			try {
				WatchKey k = service.take();
				for (WatchEvent<?> obj_watchEvent : k.pollEvents()) {
					if (obj_watchEvent.kind() == StandardWatchEventKinds.OVERFLOW) {
						continue;
					}
					@SuppressWarnings(value = "unchecked")
					WatchEvent<Path> watchEvent = (WatchEvent<Path>) obj_watchEvent;
					Path fullpath = watchedPath.resolve(watchEvent.context());
					createIndexTask(fullpath);
				}
				k.reset();
			} catch (InterruptedException ex) {
				// Stop watching
				key.cancel();
				break;
			}
		}
	}
	
}
