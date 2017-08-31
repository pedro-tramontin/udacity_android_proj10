package example.com.br.inventoryapp;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.LinearLayout;
import android.widget.ListView;

import android.widget.TextView;
import butterknife.BindView;
import butterknife.ButterKnife;
import example.com.br.inventoryapp.InventoryContract.InventoryEntry;

public class MainActivity extends AppCompatActivity implements
    LoaderManager.LoaderCallbacks<Cursor> {

    private static final String TAG = MainActivity.class.getSimpleName();

    private static final int INVENTORY_LOADER = 1;

    InventoryAdapter mInventoryAdapter;

    @BindView(R.id.inventory_list_view)
    ListView mInventoryListView;

    @BindView(R.id.empty_view)
    TextView mEmptyView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ButterKnife.bind(this);

        //Cursor cursor = readInventory();
        mInventoryAdapter = new InventoryAdapter(this, null);
        mInventoryListView.setAdapter(mInventoryAdapter);
        mInventoryListView.setEmptyView(mEmptyView);

        mInventoryListView.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Log.d(TAG, String.format("view: %s", view.toString()));

                Cursor cursor = (Cursor) mInventoryAdapter.getItem(position);

                int rowId = cursor.getInt(0);
                Uri itemUri = ContentUris.withAppendedId(InventoryEntry.CONTENT_URI, rowId);

                startEditorActivity(itemUri);
            }
        });

        //testReadWrite();

        getSupportLoaderManager().initLoader(INVENTORY_LOADER, null, this);
    }

    private void testReadWrite() {
        Boolean inserted = insertInventory("Lapis", 10, 7.5);

        Log.i(TAG, String.format("inserted? %B", inserted));

        Cursor cursor = readInventory();
        if (cursor != null) {
            try {
                int idColumnIndex = cursor.getColumnIndex(InventoryEntry._ID);
                int nameColumIndex = cursor.getColumnIndex(InventoryEntry.COLUMN_NAME_NAME);
                int priceColumnIndex = cursor.getColumnIndex(InventoryEntry.COLUMN_NAME_PRICE);
                int quantityColumnIndex = cursor
                    .getColumnIndex(InventoryEntry.COLUMN_NAME_QUANTITY);

                Log.i(TAG, String.format("id | name | price | quantity"));

                while (cursor.moveToNext()) {
                    int id = cursor.getInt(idColumnIndex);
                    String name = cursor.getString(nameColumIndex);
                    float price = cursor.getFloat(priceColumnIndex);
                    int freq = cursor.getInt(quantityColumnIndex);

                    Log.i(TAG, String.format("%d | %s | %.2f | %d", id, name, price, freq));
                }
            } finally {
                cursor.close();
            }
        }
    }

    /**
     * Inserts a new inventory
     */
    private boolean insertInventory(String name, Integer quantity, double price) {
        ContentValues values = new ContentValues();
        values.put(InventoryEntry.COLUMN_NAME_NAME, name);
        values.put(InventoryEntry.COLUMN_NAME_QUANTITY, quantity);
        values.put(InventoryEntry.COLUMN_NAME_PRICE, price);

        Uri uri = getContentResolver().insert(InventoryEntry.CONTENT_URI, values);

        Log.i(TAG, uri.toString());

        return uri != null;
    }

    /**
     * Queries the database and returns a Cursor
     *
     * @return a Cursor to the database
     */
    private Cursor readInventory() {
        String[] projection = {
            InventoryEntry._ID,
            InventoryEntry.COLUMN_NAME_NAME,
            InventoryEntry.COLUMN_NAME_PRICE,
            InventoryEntry.COLUMN_NAME_QUANTITY,
        };

        return getContentResolver()
            .query(InventoryEntry.CONTENT_URI, projection, null, null,
                null);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_add_item) {
            startEditorActivity(null);

            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public Loader onCreateLoader(int id, Bundle args) {
        String[] projection = {
            InventoryEntry._ID,
            InventoryEntry.COLUMN_NAME_NAME,
            InventoryEntry.COLUMN_NAME_PRICE,
            InventoryEntry.COLUMN_NAME_QUANTITY,
        };

        return new CursorLoader(this, InventoryEntry.CONTENT_URI, projection, null,
            null, null);
    }

    @Override
    public void onLoadFinished(Loader loader, Cursor cursor) {
        mInventoryAdapter.swapCursor(cursor);
    }

    @Override
    public void onLoaderReset(Loader loader) {
        mInventoryAdapter.swapCursor(null);
    }

    private void startEditorActivity(Uri uri) {
        Intent intent = new Intent(this,
            InventoryEditorActivity.class);

        if (uri != null) {
            intent.setData(uri);
        }

        startActivity(intent);
    }
}
