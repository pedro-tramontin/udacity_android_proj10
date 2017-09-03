package example.com.br.inventoryapp;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import example.com.br.inventoryapp.InventoryContract.InventoryEntry;

/**
 * DBHelper for the inventory DB
 */
class InventoryDbHelper extends SQLiteOpenHelper {

    private static final int DATABASE_VERSION = 1;
    private static final String DATABASE_NAME = "InventoryApp.db";

    InventoryDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String SQL_CREATE_INVENTORY_APP_TABLE =
                "CREATE TABLE " + InventoryEntry.TABLE_NAME + " (" +
                        InventoryEntry._ID + " INTEGER PRIMARY KEY," +
                        InventoryEntry.COLUMN_NAME_NAME + " TEXT," +
                        InventoryEntry.COLUMN_NAME_QUANTITY + " INTEGER," +
                        InventoryEntry.COLUMN_NAME_PRICE + " INTEGER," +
                        InventoryEntry.COLUMN_NAME_PICTURE + " BLOB)";

        db.execSQL(SQL_CREATE_INVENTORY_APP_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        String SQL_DELETE_INVENTORY_APP_TABLE =
                "DROP TABLE IF EXISTS " + InventoryEntry.TABLE_NAME;

        db.execSQL(SQL_DELETE_INVENTORY_APP_TABLE);
        onCreate(db);
    }
}
