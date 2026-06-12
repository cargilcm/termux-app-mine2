package com.termux.app;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.net.Uri;
import android.os.ParcelFileDescriptor;
import android.provider.MediaStore;
import android.webkit.MimeTypeMap;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import java.io.File;
import java.io.FileNotFoundException;

public class ExternalFileProvider extends ContentProvider {
    private static final String LOG_TAG = "ExternalFileProvider";
    private static final String BASE_PATH = "/data/data/com.termux/files";

    @Override
    public boolean onCreate() {
        return true;
    }

    @Nullable
    @Override
    public ParcelFileDescriptor openFile(@NonNull Uri uri, @NonNull String mode) throws FileNotFoundException {
        // Retrieve the clean path requested by your text editor
        String targetPath = uri.getPath();
        if (targetPath == null) {
            throw new FileNotFoundException("Invalid or null path query received.");
        }

        // Path Traversal Mitigation: Ensure client apps cannot escape the application sandbox
        if (targetPath.contains("../") || targetPath.contains("..")) {
            throw new SecurityException("Directory traversal attempt detected.");
        }

        // Map request safely onto Termux's environment home sandbox
        File file = new File(BASE_PATH, targetPath);

        if (!file.exists() && mode.equals("r")) {
            throw new FileNotFoundException("Target file does not exist: " + file.getAbsolutePath());
        }

        int fileMode;
        if (mode.equals("r")) {
            fileMode = ParcelFileDescriptor.MODE_READ_ONLY;
        } else {
            // "rw" capabilities allow full read-write operations, auto-creating the file if missing
            fileMode = ParcelFileDescriptor.MODE_READ_WRITE | ParcelFileDescriptor.MODE_CREATE;
        }

        return ParcelFileDescriptor.open(file, fileMode);
    }

    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri, @Nullable String[] projection, @Nullable String selection, @Nullable String[] selectionArgs, @Nullable String sortOrder) {
        File file = new File(BASE_PATH, uri.getPath());
        if (projection == null) {
            projection = new String[]{
                MediaStore.MediaColumns.DISPLAY_NAME,
                MediaStore.MediaColumns.SIZE
            };
        }

        MatrixCursor cursor = new MatrixCursor(projection);
        if (file.exists()) {
            Object[] row = new Object[projection.length];
            for (int i = 0; i < projection.length; i++) {
                if (MediaStore.MediaColumns.DISPLAY_NAME.equals(projection[i])) {
                    row[i] = file.getName();
                } else if (MediaStore.MediaColumns.SIZE.equals(projection[i])) {
                    row[i] = (int) file.length();
                } else {
                    row[i] = null;
                }
            }
            cursor.addRow(row);
        }
        return cursor;
    }

    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {
        String path = uri.getLastPathSegment();
        if (path != null && path.contains(".")) {
            String ext = path.substring(path.lastIndexOf('.') + 1).toLowerCase();
            return MimeTypeMap.getSingleton().getMimeTypeFromExtension(ext);
        }
        return "application/octet-stream";
    }

    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, @Nullable ContentValues values) { return null; }

    @Override
    public int delete(@NonNull Uri uri, @Nullable String selection, @Nullable String[] selectionArgs) { return 0; }

    @Override
    public int update(@NonNull Uri uri, @Nullable ContentValues values, @Nullable String selection, @Nullable String[] selectionArgs) { return 0; }
}
