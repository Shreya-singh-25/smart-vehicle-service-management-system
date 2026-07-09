package com.autocare.vsms.utils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Simple file-based backup and restore for the SQLite database.
 */
public final class BackupRestore {

    private static final Path BACKUP_DIR = Paths.get("data", "backups");
    private static final Path DB_PATH = Paths.get("data", "vsms.db");

    private BackupRestore() { }

    /** Copies the current database file into the backups folder with a timestamp. */
    public static String backupDatabase() {
        try {
            Files.createDirectories(BACKUP_DIR);
            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
            Path backupPath = BACKUP_DIR.resolve("vsms_backup_" + timestamp + ".db");
            Files.copy(DB_PATH, backupPath, StandardCopyOption.REPLACE_EXISTING);
            return backupPath.toString();
        } catch (IOException e) {
            throw new RuntimeException("Backup failed", e);
        }
    }

    /** Restores the database from a chosen backup file, overwriting the current DB. */
    public static boolean restoreDatabase(String backupFilePath) {
        Path source = Paths.get(backupFilePath);
        if (!Files.exists(source)) {
            return false;
        }
        try {
            Files.copy(source, DB_PATH, StandardCopyOption.REPLACE_EXISTING);
            return true;
        } catch (IOException e) {
            throw new RuntimeException("Restore failed", e);
        }
    }

    /** Returns available backup files, most recent first. */
    public static List<File> listBackups() {
        List<File> results = new ArrayList<>();
        File dir = BACKUP_DIR.toFile();
        if (!dir.exists()) {
            return results;
        }
        File[] files = dir.listFiles((d, name) -> name.endsWith(".db"));
        if (files != null) {
            for (File f : files) {
                results.add(f);
            }
            results.sort(Comparator.comparingLong(File::lastModified).reversed());
        }
        return results;
    }
}
