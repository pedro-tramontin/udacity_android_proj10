package example.com.br.inventoryapp;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.database.Cursor;
import android.net.Uri;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CursorAdapter;
import android.widget.TextView;

import android.widget.Toast;
import butterknife.BindView;
import butterknife.ButterKnife;
import example.com.br.inventoryapp.InventoryContract.InventoryEntry;
import java.util.Locale;

/**
 * Created by pedro on 8/29/17.
 */

public class InventoryAdapter extends CursorAdapter {

    private static final String TAG = InventoryAdapter.class.getSimpleName();

    public InventoryAdapter(Context context, Cursor cursor) {
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
        String price_mask = context.getResources().getString(R.string.product_price_mask);
        String quantity_mask = context.getResources().getString(R.string.product_quantity_mask);

        Locale defaultLocale = Locale.getDefault();

        // Product values
        final int id = cursor.getInt(0);
        final String name = getString(cursor, InventoryContract.InventoryEntry.COLUMN_NAME_NAME);
        final int quantity = getInt(cursor, InventoryContract.InventoryEntry.COLUMN_NAME_QUANTITY);
        final float price = getFloat(cursor, InventoryContract.InventoryEntry.COLUMN_NAME_PRICE);

        // Sets the views values
        holder.id = id;
        holder.name.setText(name);
        holder.price.setText(String.format(defaultLocale, price_mask, price));
        holder.quantity.setText(String.format(defaultLocale, quantity_mask, quantity));

        if (quantity == 0) {
            holder.btnSell.setEnabled(false);
        } else {
            holder.btnSell.setEnabled(true);
            holder.btnSell.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    View listItemLayout = (View) v.getParent();
                    ViewHolder holder = (ViewHolder) listItemLayout.getTag();

                    if (quantity > 0) {
                        setProductQuantity(context, id, quantity - 1);
                    }

                    //showDeleteConfirmationDialog(context, holder.id);
                }
            });
        }
    }

    static class ViewHolder {

        int id;

        @BindView(R.id.product_name)
        TextView name;

        @BindView(R.id.inventory_price)
        TextView price;

        @BindView(R.id.product_quantity)
        TextView quantity;

        @BindView(R.id.button_sell_product)
        Button btnSell;

        private ViewHolder(View view) {
            ButterKnife.bind(this, view);
        }
    }

    private void showDeleteConfirmationDialog(final Context context, final int rowId) {

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setMessage("Really want to delete this item?");
        builder.setPositiveButton("Delete", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Delete" button, so delete the pet.
                deleteItem(context, rowId);
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Cancel" button, so dismiss the dialog
                // and continue editing the pet.
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });

        // Create and show the AlertDialog
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    private void deleteItem(Context context, int id) {
        Uri itemUri = ContentUris.withAppendedId(InventoryEntry.CONTENT_URI, id);

        int rowDeleted = context.getContentResolver()
            .delete(itemUri, null, null);

        Log.i(TAG, String.format("Deleted ID %d", rowDeleted));
    }

    private String getString(Cursor cursor, String columnName) {
        return cursor.getString(cursor.getColumnIndex(columnName));
    }

    private Float getFloat(Cursor cursor, String columnName) {
        return cursor.getFloat(cursor.getColumnIndex(columnName));
    }

    private Integer getInt(Cursor cursor, String columnName) {
        return cursor.getInt(cursor.getColumnIndex(columnName));
    }

    private void setProductQuantity(Context context, int id, int newQuantity) {
        ContentValues values = new ContentValues();
        values.put(InventoryEntry.COLUMN_NAME_QUANTITY, newQuantity);

        Uri itemUri = ContentUris.withAppendedId(InventoryEntry.CONTENT_URI, id);

        int rowCount = context.getContentResolver().update(itemUri, values, null, null);
        if (rowCount == 0) {
            Toast.makeText(context, context.getResources().getString(R.string.fail_to_sell_product),
                Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(context, context.getResources().getString(R.string.product_sold),
                Toast.LENGTH_SHORT).show();
        }
    }
}
