package example.com.br.inventoryapp;

import android.content.ContentResolver;
import android.net.Uri;
import android.provider.BaseColumns;

/**
 * Inventory DB Contract
 */
final class InventoryContract {

    static final String CONTENT_AUTHORITY = "example.com.br.inventoryapp.provider";
    static final String PATH_INVENTOTY = "inventory";
    private static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);

    private InventoryContract() {
    }

    static class InventoryEntry implements BaseColumns {

        static final Uri CONTENT_URI = Uri
                .withAppendedPath(BASE_CONTENT_URI, PATH_INVENTOTY);

        static final String CONTENT_LIST_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_INVENTOTY;

        static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_INVENTOTY;

        static final String TABLE_NAME = "inventory";

        static final String COLUMN_NAME_NAME = "name";
        static final String COLUMN_NAME_QUANTITY = "quantity";
        static final String COLUMN_NAME_PRICE = "price";
        static final String COLUMN_NAME_PICTURE = "picture";
    }
}
