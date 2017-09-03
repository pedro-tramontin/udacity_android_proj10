package example.com.br.inventoryapp;

import android.database.Cursor;

import example.com.br.inventoryapp.InventoryContract.InventoryEntry;

/**
 * Product class
 */
class Product {

    private final int mId;
    private final String mName;
    private final int mQuantity;
    private final float mPrice;
    private final byte[] mPicture;

    Product(Cursor cursor) {
        int id = Utils.getInt(cursor, InventoryEntry._ID);
        String name = Utils.getString(cursor, InventoryEntry.COLUMN_NAME_NAME);
        int quantity = Utils.getInt(cursor, InventoryEntry.COLUMN_NAME_QUANTITY);
        float price = Utils.getFloat(cursor, InventoryEntry.COLUMN_NAME_PRICE);
        byte[] picture = Utils.getBlob(cursor, InventoryEntry.COLUMN_NAME_PICTURE);

        this.mId = id;
        this.mName = name;
        this.mQuantity = quantity;
        this.mPrice = price;
        this.mPicture = picture;
    }

    int getId() {
        return mId;
    }

    String getName() {
        return mName;
    }

    int getQuantity() {
        return mQuantity;
    }

    float getPrice() {
        return mPrice;
    }

    byte[] getPicture() {
        return mPicture;
    }
}
