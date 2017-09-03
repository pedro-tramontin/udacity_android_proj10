package example.com.br.inventoryapp;

import android.content.ContentProvider;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.util.Log;

import example.com.br.inventoryapp.InventoryContract.InventoryEntry;

/**
 * Provider for the inventory DB
 */
public class InventoryProvider extends ContentProvider {

    /**
     * URI matcher code for the content URI for the inventory table
     */
    public static final int INVENTORY = 100;
    /**
     * URI matcher code for the content URI for a single inventory in the inventory table
     */
    public static final int INVENTORY_ID = 101;
    private static final String TAG = InventoryProvider.class.getSimpleName();
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

    private InventoryDbHelper mDbHelper;

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

        Cursor cursor;

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
                throw new IllegalArgumentException(Utils.format(Utils.getString(getContext(), R
                        .string.error_query_unknown_uri), uri.toString()));
        }

        cursor.setNotificationUri(getContentResolver(), uri);

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
                throw new IllegalStateException(Utils.format(Utils.getString(getContext(), R
                        .string.error_unknown_uri), uri.toString(), match));
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
                throw new IllegalArgumentException(Utils.format(Utils.getString(getContext(), R
                        .string.error_invalid_uri_insert), uri.toString()));
        }
    }

    private Uri insertInventory(Uri uri, ContentValues values) {
        SQLiteDatabase db = mDbHelper.getWritableDatabase();

        validateInventoryValues(values, true);

        long id = db.insert(InventoryEntry.TABLE_NAME, null, values);

        if (id == -1) {
            Log.e(TAG, Utils.format(Utils.getString(getContext(), R.string
                    .error_provider_insert_failed), uri.toString()));
            return null;
        }

        notifyChange(uri);

        return ContentUris.withAppendedId(uri, id);
    }

    @Override
    public int delete(@NonNull Uri uri, @Nullable String selection,
                      @Nullable String[] selectionArgs) {

        final int match = sUriMatcher.match(uri);
        switch (match) {
            case INVENTORY:
                return deleteInventory(uri, selection, selectionArgs);
            case INVENTORY_ID:
                selection = InventoryEntry._ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                return deleteInventory(uri, selection, selectionArgs);
            default:
                throw new IllegalArgumentException(Utils.format(Utils.getString(getContext(), R
                        .string.error_invalid_uri_delete), uri.toString()));
        }
    }

    private int deleteInventory(@NonNull Uri uri, @Nullable String selection,
                                @Nullable String[] selectionArgs) {

        SQLiteDatabase db = mDbHelper.getWritableDatabase();

        int rowsDeleted = db.delete(InventoryEntry.TABLE_NAME, selection, selectionArgs);

        if (rowsDeleted > 0) {
            notifyChange(uri);
        }

        return rowsDeleted;

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
                throw new IllegalArgumentException(Utils.format(Utils.getString(getContext(), R
                        .string.error_invalid_uri_update), uri.toString()));
        }
    }

    private int updateInventory(Uri uri, ContentValues values, String selection,
                                String[] selectionArgs) {

        validateInventoryValues(values, false);

        if (values.size() == 0) {
            return 0;
        }

        SQLiteDatabase database = mDbHelper.getWritableDatabase();

        int rowsUpdated = database
                .update(InventoryEntry.TABLE_NAME, values, selection, selectionArgs);

        if (rowsUpdated > 0) {
            notifyChange(uri);
        }

        return rowsUpdated;
    }

    private void validateInventoryValues(ContentValues values, boolean insert) {
        if (values.containsKey(InventoryEntry.COLUMN_NAME_NAME)) {
            String name = values.getAsString(InventoryEntry.COLUMN_NAME_NAME);
            if (name == null) {
                throwWithMessage(R.string.error_provider_product_name_required);
            }
        } else {
            if (insert) {
                throwWithMessage(R.string.error_provider_product_name_required);
            }
        }

        if (values.containsKey(InventoryEntry.COLUMN_NAME_QUANTITY)) {
            Integer quantity = values.getAsInteger(InventoryEntry.COLUMN_NAME_QUANTITY);
            if (quantity == null || quantity < 0) {
                throwWithMessage(R.string.error_provider_product_quantity_required);
            }
        } else {
            if (insert) {
                throwWithMessage(R.string.error_provider_product_quantity_required);
            }
        }

        if (values.containsKey(InventoryEntry.COLUMN_NAME_PRICE)) {
            Float price = values.getAsFloat(InventoryEntry.COLUMN_NAME_PRICE);
            if (price == null || price < 0) {
                throwWithMessage(R.string.error_provider_product_price_required);
            }
        } else {
            if (insert) {
                throwWithMessage(R.string.error_provider_product_price_required);
            }
        }

        if (values.containsKey(InventoryEntry.COLUMN_NAME_PICTURE)) {
            byte[] picture = values.getAsByteArray(InventoryEntry.COLUMN_NAME_PICTURE);
            if (picture == null) {
                if (insert) {
                    throwWithMessage(R.string.error_provider_product_picture_required);
                }
            }
        }
    }

    private void throwWithMessage(@StringRes int stringId) {
        throw new IllegalArgumentException(Utils.getString(getContext(), stringId));
    }

    private void notifyChange(Uri uri) {
        if (getContext() != null) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
    }

    private ContentResolver getContentResolver() {
        if (getContext() != null) {
            return getContext().getContentResolver();
        }

        return null;
    }
}
