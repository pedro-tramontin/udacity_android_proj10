package example.com.br.inventoryapp;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import example.com.br.inventoryapp.InventoryContract.InventoryEntry;

import static android.transition.Fade.IN;

/**
 * Created by ptramontin on 8/29/17.
 */

public class InventoryProvider extends ContentProvider {

    private static final String TAG = InventoryProvider.class.getSimpleName();

    private InventoryDbHelper mDbHelper;

    /**
     * URI matcher code for the content URI for the inventory table
     */
    public static final int INVENTORY = 100;

    /**
     * URI matcher code for the content URI for a single inventory in the inventory table
     */
    public static final int INVENTORY_ID = 101;

    /**
     * URI matcher object to match a context URI to a corresponding code.
     * The input passed into the constructor represents the code to return for the root URI.
     * It's common to use NO_MATCH as the input for this case.
     */
    private static final UriMatcher sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

    static {
        sUriMatcher.addURI(InventoryContract.CONTENT_AUTHORITY, InventoryContract.PATH_INVENTOTY,
                INVENTORY);
        sUriMatcher
                .addURI(InventoryContract.CONTENT_AUTHORITY, InventoryContract.PATH_INVENTOTY + "/#",
                        INVENTORY_ID);
    }

    @Override
    public boolean onCreate() {
        mDbHelper = new InventoryDbHelper(getContext());

        return true;
    }

    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri, @Nullable String[] projection, @Nullable String selection,
                        @Nullable String[] selectionArgs, @Nullable String sortOrder) {

        SQLiteDatabase db = mDbHelper.getReadableDatabase();

        Cursor cursor = null;

        int match = sUriMatcher.match(uri);
        switch (match) {
            case INVENTORY:
                cursor = db
                        .query(InventoryEntry.TABLE_NAME, projection, null, null, null, null, null);

                break;
            case INVENTORY_ID:
                selection = InventoryEntry._ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};

                cursor = db.query(InventoryEntry.TABLE_NAME, projection, selection, selectionArgs,
                        null, null, sortOrder);
                break;
            default:
                throw new IllegalArgumentException("Cannot query unknown URI " + uri);
        }

        return cursor;
    }

    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case INVENTORY:
                return InventoryEntry.CONTENT_LIST_TYPE;
            case INVENTORY_ID:
                return InventoryEntry.CONTENT_ITEM_TYPE;
            default:
                throw new IllegalStateException("Unknown URI " + uri + " with match " + match);
        }
    }

    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, @Nullable ContentValues values) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case INVENTORY:
                return insertInventory(uri, values);
            default:
                throw new IllegalArgumentException("Insertion is not supported for " + uri);
        }
    }

    private Uri insertInventory(Uri uri, ContentValues values) {
        SQLiteDatabase db = mDbHelper.getWritableDatabase();

        validateInventoryValues(values);

        long id = db.insert(InventoryEntry.TABLE_NAME, null, values);

        if (id == -1) {
            Log.e(TAG, "Failed to insert row for " + uri);
            return null;
        }

        return ContentUris.withAppendedId(uri, id);
    }

    @Override
    public int delete(@NonNull Uri uri, @Nullable String selection,
                      @Nullable String[] selectionArgs) {

        // Get writeable database
        SQLiteDatabase database = mDbHelper.getWritableDatabase();

        final int match = sUriMatcher.match(uri);
        switch (match) {
            case INVENTORY:
                return database.delete(InventoryEntry.TABLE_NAME, selection, selectionArgs);
            case INVENTORY_ID:
                selection = InventoryEntry._ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                return database.delete(InventoryEntry.TABLE_NAME, selection, selectionArgs);
            default:
                throw new IllegalArgumentException("Deletion is not supported for " + uri);
        }
    }

    @Override
    public int update(@NonNull Uri uri, @Nullable ContentValues values, @Nullable String
            selection, @Nullable String[] selectionArgs) {

        final int match = sUriMatcher.match(uri);
        switch (match) {
            case INVENTORY:
                return updateInventory(uri, values, selection, selectionArgs);
            case INVENTORY_ID:
                selection = InventoryEntry._ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                return updateInventory(uri, values, selection, selectionArgs);
            default:
                throw new IllegalArgumentException("Update is not supported for " + uri);
        }
    }

    private int updateInventory(Uri uri, ContentValues values, String selection,
                                String[] selectionArgs) {

        validateInventoryValues(values);

        if (values.size() == 0) {
            return 0;
        }

        SQLiteDatabase database = mDbHelper.getWritableDatabase();

        return database.update(InventoryEntry.TABLE_NAME, values, selection, selectionArgs);
    }

    private void validateInventoryValues(ContentValues values) {
        if (values.containsKey(InventoryEntry.COLUMN_NAME_NAME)) {
            String name = values.getAsString(InventoryEntry.COLUMN_NAME_NAME);
            if (name == null) {
                throw new IllegalArgumentException("Inventory requires a name");
            }
        }

        if (values.containsKey(InventoryEntry.COLUMN_NAME_QUANTITY)) {
            Integer quantity = values.getAsInteger(InventoryEntry.COLUMN_NAME_QUANTITY);
            if (quantity == null || quantity < 0) {
                throw new IllegalArgumentException("Inventory requires a quantity");
            }
        }

        if (values.containsKey(InventoryEntry.COLUMN_NAME_PRICE)) {
            Float price = values.getAsFloat(InventoryEntry.COLUMN_NAME_PRICE);
            if (price == null || price < 0) {
                throw new IllegalArgumentException("Inventory requires a price");
            }
        }
    }
}
