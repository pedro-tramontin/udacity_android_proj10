package example.com.br.inventoryapp;

import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ListView;

import butterknife.BindView;
import butterknife.ButterKnife;
import example.com.br.inventoryapp.InventoryContract.InventoryEntry;

public class MainActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks {

    private static final String TAG = MainActivity.class.getSimpleName();

    InventoryAdapter mInventoryAdapter;

    @BindView(R.id.inventory_list_view)
    ListView mInventoryListView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ButterKnife.bind(this);

        mInventoryAdapter = new InventoryAdapter(this, null);
        mInventoryListView.setAdapter(mInventoryAdapter);

        View emptyView = findViewById(R.id.empty_view);
        mInventoryListView.setEmptyView(emptyView);

        testReadWrite();
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
    public Loader onCreateLoader(int id, Bundle args) {
        Uri.Builder uriBuilder = Uri.parse(THE_GUARDIAN_NEWS_API_URL)
                .buildUpon()
                .appendQueryParameter(SHOW_FIELDS_PARAM, "headline,byline,firstPublicationDate")
                .appendQueryParameter(API_KEY_PARAM, "36a7884f-b99c-41d7-9bbf-e94206c36fbd")
                .appendQueryParameter(TAG_PARAM, "politics/politics")
                .appendQueryParameter(PAGE_SIZE_PARAM, pageSize);

        if (!"".equals(mEditQuery.getText().toString())) {
            uriBuilder.appendQueryParameter(QUERY_PARAM, mEditQuery.getText().toString());
        }

        return new

        return new NewsLoader(this, uriBuilder.build().toString());
    }

    @Override
    public void onLoadFinished(Loader loader, Object data) {

    }

    @Override
    public void onLoaderReset(Loader loader) {

    }
}
