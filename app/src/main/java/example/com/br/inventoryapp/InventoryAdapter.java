package example.com.br.inventoryapp;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.TextView;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by pedro on 8/29/17.
 */

public class InventoryAdapter extends CursorAdapter {

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
    public void bindView(View view, Context context, Cursor cursor) {
        ViewHolder holder = (ViewHolder) view.getTag();

        int nameColumIndex = cursor.getColumnIndex(InventoryContract.InventoryEntry.COLUMN_NAME_NAME);
        int priceColumnIndex = cursor.getColumnIndex(InventoryContract.InventoryEntry.COLUMN_NAME_PRICE);
        int quantityColumnIndex = cursor
                .getColumnIndex(InventoryContract.InventoryEntry.COLUMN_NAME_QUANTITY);

        holder.name.setText(cursor.getString(nameColumIndex));
        holder.quantity.setText(cursor.getString(quantityColumnIndex));
        holder.price.setText(cursor.getString(priceColumnIndex));
    }

    static class ViewHolder {
        @BindView(R.id.inventory_name)
        TextView name;

        @BindView(R.id.inventory_quantity)
        TextView quantity;

        @BindView(R.id.inventory_price)
        TextView price;

        private ViewHolder(View view) {
            ButterKnife.bind(this, view);
        }
    }
}
