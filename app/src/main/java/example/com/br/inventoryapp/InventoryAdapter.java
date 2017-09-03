package example.com.br.inventoryapp;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import butterknife.BindView;
import butterknife.ButterKnife;
import example.com.br.inventoryapp.InventoryContract.InventoryEntry;

import static example.com.br.inventoryapp.Utils.getString;

/**
 * Adapter for the inventory items
 */
class InventoryAdapter extends CursorAdapter {

    InventoryAdapter(Context context, Cursor cursor) {
        super(context, cursor, 0);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup viewGroup) {
        View view = LayoutInflater.from(context).inflate(R.layout.list_item, viewGroup, false);
        ViewHolder holder = new ViewHolder(view);
        view.setTag(holder);

        return view;
    }

    @Override
    public void bindView(View view, final Context context, Cursor cursor) {
        ViewHolder holder = (ViewHolder) view.getTag();

        // Masks for price and quantity
        String price_mask = getString(context, R.string.product_price_mask);
        String quantity_mask = getString(context, R.string.product_quantity_mask);

        // Product values
        final int id = cursor.getInt(0);
        final String name = Utils.getString(cursor, InventoryEntry.COLUMN_NAME_NAME);
        final int quantity = Utils.getInt(cursor, InventoryEntry.COLUMN_NAME_QUANTITY);
        final float price = Utils.getFloat(cursor, InventoryEntry.COLUMN_NAME_PRICE);
        final byte[] picture = Utils.getBlob(cursor, InventoryEntry.COLUMN_NAME_PICTURE);

        // Sets the views values
        holder.id = id;
        holder.name.setText(name);
        holder.price.setText(Utils.format(price_mask, price));
        holder.quantity.setText(Utils.format(quantity_mask, quantity));
        holder.picture.setImageBitmap(BitmapFactory.decodeByteArray(picture, 0, picture.length));

        if (quantity == 0) {
            holder.btnSell.setEnabled(false);
        } else {
            holder.btnSell.setEnabled(true);
            holder.btnSell.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (quantity > 0) {
                        setProductQuantity(context, id, quantity - 1);
                    }
                }
            });
        }
    }

    private void setProductQuantity(Context context, int id, int newQuantity) {
        ContentValues values = new ContentValues();
        values.put(InventoryEntry.COLUMN_NAME_QUANTITY, newQuantity);

        Uri itemUri = ContentUris.withAppendedId(InventoryEntry.CONTENT_URI, id);

        int rowCount = context.getContentResolver().update(itemUri, values, null, null);
        if (rowCount == 0) {
            Utils.shortToast(context, context.getResources().getString(R.string
                    .fail_to_sell_product));
        } else {
            Utils.shortToast(context, context.getResources().getString(R.string.product_sold));
        }
    }

    static class ViewHolder {

        int id;

        @BindView(R.id.product_name)
        TextView name;

        @BindView(R.id.product_price)
        TextView price;

        @BindView(R.id.product_quantity)
        TextView quantity;

        @BindView(R.id.product_picture)
        ImageView picture;

        @BindView(R.id.button_sell_product)
        Button btnSell;

        private ViewHolder(View view) {
            ButterKnife.bind(this, view);
        }
    }
}
