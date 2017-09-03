package example.com.br.inventoryapp;

import android.content.ContentUris;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.TextView;

import butterknife.BindView;
import butterknife.ButterKnife;
import example.com.br.inventoryapp.InventoryContract.InventoryEntry;

public class MainActivity extends AppCompatActivity implements
        LoaderManager.LoaderCallbacks<Cursor> {

    private static final int INVENTORY_LOADER = 1;

    InventoryAdapter mInventoryAdapter;

    @BindView(R.id.inventory_list_view)
    ListView mInventoryListView;

    @BindView(R.id.inventory_empty_view)
    TextView mEmptyView;

    @BindView(R.id.add_product)
    FloatingActionButton mActionButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ButterKnife.bind(this);

        mInventoryAdapter = new InventoryAdapter(this, null);
        mInventoryListView.setAdapter(mInventoryAdapter);
        mInventoryListView.setEmptyView(mEmptyView);
        mInventoryListView.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Cursor cursor = (Cursor) mInventoryAdapter.getItem(position);

                Uri itemUri = ContentUris
                        .withAppendedId(InventoryEntry.CONTENT_URI, cursor.getInt(0));

                startEditorActivity(itemUri);
            }
        });

        mActionButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                startEditorActivity(null);
            }
        });

        getSupportLoaderManager().initLoader(INVENTORY_LOADER, null, this);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        String[] projection = {
                InventoryEntry._ID,
                InventoryEntry.COLUMN_NAME_NAME,
                InventoryEntry.COLUMN_NAME_PRICE,
                InventoryEntry.COLUMN_NAME_QUANTITY,
                InventoryEntry.COLUMN_NAME_PICTURE
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
