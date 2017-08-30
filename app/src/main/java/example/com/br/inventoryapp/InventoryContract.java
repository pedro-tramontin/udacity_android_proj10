package example.com.br.inventoryapp;

import android.content.ContentResolver;
import android.net.Uri;
import android.provider.BaseColumns;

/**
 * Created by ptramontin on 8/29/17.
 */

public final class InventoryContract {

    public static final String CONTENT_AUTHORITY = "example.com.br.inventoryapp.provider";

    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);

    public static final String PATH_INVENTOTY = "inventory";

    private InventoryContract() {
    }

    public static class InventoryEntry implements BaseColumns {

        public static final Uri CONTENT_URI = Uri
                .withAppendedPath(BASE_CONTENT_URI, PATH_INVENTOTY);

        public static final String CONTENT_LIST_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_INVENTOTY;

        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_INVENTOTY;

        public static final String TABLE_NAME = "inventory";

        public static final String COLUMN_NAME_NAME = "name";
        public static final String COLUMN_NAME_QUANTITY = "quantity";
        public static final String COLUMN_NAME_PRICE = "price";
        public static final String COLUMN_NAME_PICTURE = "picture";
    }
}
