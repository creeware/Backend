package util;

import java.io.File;
import java.nio.file.Files;

public class Directory {
    public static void deleteDirectory(File file) {
        File[] contents = file.listFiles();
        if (contents != null) {
            for (File f : contents) {
                if (! Files.isSymbolicLink(f.toPath())) {
                    deleteDirectory(f);
                }
            }
        }
        file.delete();
    }
}
