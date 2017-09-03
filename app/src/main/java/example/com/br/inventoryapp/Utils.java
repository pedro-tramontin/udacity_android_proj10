package example.com.br.inventoryapp;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.net.Uri;
import android.support.annotation.StringRes;
import android.support.media.ExifInterface;
import android.util.Log;
import android.widget.Toast;

import java.io.FileDescriptor;
import java.io.IOException;
import java.io.InputStream;
import java.util.Locale;

import static android.content.ContentValues.TAG;

/**
 * Utils class
 */
class Utils {

    private static final int THUMBNAIL_WIDTH = 640;
    private static final int THUMBNAIL_HEIGHT = 480;

    static String getString(Context context, @StringRes int stringId) {
        return context.getResources().getString(stringId);
    }

    static String format(String format, Object... args) {
        return String.format(Locale.getDefault(), format, args);
    }

    private static Bitmap rotateBitmap(int orientation, Bitmap srcBitmap) {
        Matrix matrix = new Matrix();
        switch (orientation) {
            case 2:
                matrix.setScale(-1, 1);
                break;
            case 3:
                matrix.setRotate(180);
                break;
            case 4:
                matrix.setRotate(180);
                matrix.postScale(-1, 1);
                break;
            case 5:
                matrix.setRotate(90);
                matrix.postScale(-1, 1);
                break;
            case 6:
                matrix.setRotate(90);
                break;
            case 7:
                matrix.setRotate(-90);
                matrix.postScale(-1, 1);
                break;
            case 8:
                matrix.setRotate(-90);
                break;
            default:
                return srcBitmap;
        }

        try {
            Bitmap oriented = Bitmap.createBitmap(srcBitmap, 0, 0, srcBitmap.getWidth(),
                    srcBitmap.getHeight(), matrix, true);
            srcBitmap.recycle();
            return oriented;
        } catch (OutOfMemoryError e) {
            e.printStackTrace();
        }

        return srcBitmap;
    }

    static String getString(Cursor cursor, String columnName) {
        return cursor.getString(cursor.getColumnIndex(columnName));
    }

    static Float getFloat(Cursor cursor, String columnName) {
        return cursor.getFloat(cursor.getColumnIndex(columnName));
    }

    static Integer getInt(Cursor cursor, String columnName) {
        return cursor.getInt(cursor.getColumnIndex(columnName));
    }

    static byte[] getBlob(Cursor cursor, String columnName) {
        return cursor.getBlob(cursor.getColumnIndex(columnName));
    }

    static void shortToast(Context context, String message) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
    }

    static Bitmap scaleBitmapIfNeeded(FileDescriptor fileDescriptor) {
        Bitmap bitmap = BitmapFactory.decodeFileDescriptor(fileDescriptor);
        if (bitmap.getWidth() > THUMBNAIL_WIDTH || bitmap.getHeight() > THUMBNAIL_HEIGHT) {
            bitmap = Bitmap.createScaledBitmap(bitmap, THUMBNAIL_WIDTH, THUMBNAIL_HEIGHT, true);
        }

        return bitmap;
    }

    static Bitmap rotateBitmapIfNeed(Context context, Uri uri, Bitmap bitmap) {
        try (InputStream inputStream = context.getContentResolver().openInputStream(uri)) {
            if (inputStream != null) {
                ExifInterface exif = new ExifInterface(inputStream);
                int orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);
                return Utils.rotateBitmap(orientation, bitmap);
            } else {
                Log.e(TAG, Utils.getString(context, R.string.error_inputstream_from_uri));
            }
        } catch (IOException e) {
            Log.e(TAG, Utils.getString(context, R.string.error_exif), e);
        }

        return bitmap;
    }
}
