package example.com.br.inventoryapp;

import android.content.ContentUris;
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
import android.widget.ImageView;
import android.widget.TextView;

import butterknife.BindView;
import butterknife.ButterKnife;
import example.com.br.inventoryapp.InventoryContract.InventoryEntry;

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

        int nameColumnIndex = cursor
                .getColumnIndex(InventoryContract.InventoryEntry.COLUMN_NAME_NAME);
        int priceColumnIndex = cursor
                .getColumnIndex(InventoryContract.InventoryEntry.COLUMN_NAME_PRICE);
        int quantityColumnIndex = cursor
                .getColumnIndex(InventoryContract.InventoryEntry.COLUMN_NAME_QUANTITY);

        holder.id = cursor.getInt(0);
        holder.name.setText(cursor.getString(nameColumnIndex));
        holder.quantity.setText(cursor.getString(quantityColumnIndex));
        holder.price.setText(cursor.getString(priceColumnIndex));
        holder.deleteItem.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                View listItemLayout = (View) v.getParent();
                ViewHolder holder = (ViewHolder) listItemLayout.getTag();

                showDeleteConfirmationDialog(context, holder.id);
            }
        });
    }

    static class ViewHolder {

        int id;

        @BindView(R.id.product_name)
        TextView name;

        @BindView(R.id.product_quantity)
        TextView quantity;

        @BindView(R.id.inventory_price)
        TextView price;

        @BindView(R.id.button_sell_product)
        Button deleteItem;

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
}
