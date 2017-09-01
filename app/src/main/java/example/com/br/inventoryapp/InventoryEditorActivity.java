package example.com.br.inventoryapp;

import android.content.ContentValues;
import android.content.DialogInterface;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.app.NavUtils;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import butterknife.BindView;
import butterknife.ButterKnife;
import example.com.br.inventoryapp.InventoryContract.InventoryEntry;

/**
 * Created by ptramontin on 8/30/17.
 */

public class InventoryEditorActivity extends AppCompatActivity implements LoaderCallbacks<Cursor> {

    private static final String TAG = InventoryEditorActivity.class.getSimpleName();

    private static final int INVENTORY_ITEM_LOADER = 2;

    private Uri itemUri;

    @BindView(R.id.product_name)
    TextView mItemName;

    @BindView(R.id.product_quantity)
    TextView mItemQuantity;

    @BindView(R.id.inventory_price)
    TextView mItemPrice;

    @BindView(R.id.button_save_inventory)
    Button mBtnSave;

    private boolean mItemHasChanged = false;

    private String mLastText = "";

    private TextWatcher mTextListener = new TextWatcher() {

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            mLastText = s.toString();
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            // Nothing to do
        }

        @Override
        public void afterTextChanged(Editable s) {
            if (mLastText != null && !mLastText.equals(s.toString())) {
                mItemHasChanged = true;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.product_details);

        ButterKnife.bind(this);

        itemUri = getIntent().getData();
        if (itemUri != null) {
            setTitle("Edit inventory");

            getSupportLoaderManager().initLoader(INVENTORY_ITEM_LOADER, null, this);
        } else {
            setTitle("Add inventory");
        }

        mItemName.addTextChangedListener(mTextListener);
        mItemQuantity.addTextChangedListener(mTextListener);
        mItemPrice.addTextChangedListener(mTextListener);

        mBtnSave.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                saveItem();
            }
        });
    }

    @Override
    public Loader onCreateLoader(int id, Bundle args) {
        String[] projection = {
            InventoryEntry._ID,
            InventoryEntry.COLUMN_NAME_NAME,
            InventoryEntry.COLUMN_NAME_PRICE,
            InventoryEntry.COLUMN_NAME_QUANTITY,
        };

        return new CursorLoader(this, itemUri, projection, null,
            null, null);
    }

    @Override
    public void onLoadFinished(Loader loader, Cursor cursor) {
        if (cursor.moveToFirst()) {

            int nameColumnIndex = cursor.getColumnIndex(InventoryEntry.COLUMN_NAME_NAME);
            int quantityColumnIndex = cursor.getColumnIndex(InventoryEntry.COLUMN_NAME_QUANTITY);
            int priceColumnIndex = cursor
                .getColumnIndex(InventoryEntry.COLUMN_NAME_PRICE);

            String name = cursor.getString(nameColumnIndex);
            int quantity = cursor.getInt(quantityColumnIndex);
            float price = cursor.getFloat(priceColumnIndex);

            mItemName.setText(name);
            mItemQuantity.setText(Integer.toString(quantity));
            mItemPrice.setText(Float.toString(price));

            // Sets the flag to false because it is true after loading the data from the cursor
            mItemHasChanged = false;
        }
    }

    @Override
    public void onLoaderReset(Loader loader) {
        mItemName.setText("");
        mItemQuantity.setText("");
        mItemPrice.setText("");
    }

    private void saveItem() {
        if (areFieldsValid()) {
            ContentValues values = new ContentValues();
            values.put(InventoryEntry.COLUMN_NAME_NAME, mItemName.getText().toString());
            values.put(InventoryEntry.COLUMN_NAME_QUANTITY,
                Integer.valueOf(mItemQuantity.getText().toString()));
            values.put(InventoryEntry.COLUMN_NAME_PRICE,
                Float.valueOf(mItemPrice.getText().toString()));

            if (itemUri != null) {
                int rowCount = getContentResolver().update(itemUri, values, null, null);

                Log.i(TAG, String.format("Row Count: %d", rowCount));

                if (rowCount != 0) {
                    // If no rows were affected, then there was an error with the update.
                    Toast.makeText(this, "Updated!", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, "Fail to update", Toast.LENGTH_SHORT).show();
                }
            } else {
                Uri uri = getContentResolver().insert(InventoryEntry.CONTENT_URI, values);

                Log.i(TAG, uri.toString());
            }

            finish();
        }
    }

    private boolean areFieldsValid() {
        if (TextUtils.isEmpty(mItemName.getText())) {
            Toast.makeText(this, "Name is required", Toast.LENGTH_SHORT).show();

            return false;
        }

        if (TextUtils.isEmpty(mItemQuantity.getText())) {
            Toast.makeText(this, "Quantity is required", Toast.LENGTH_SHORT).show();

            return false;
        }

        if (TextUtils.isEmpty(mItemPrice.getText())) {
            Toast.makeText(this, "Price is required", Toast.LENGTH_SHORT).show();

            return false;
        }

        return true;
    }

    private void showUnsavedChangesDialog(
        DialogInterface.OnClickListener discardButtonClickListener) {
        // Create an AlertDialog.Builder and set the message, and click listeners
        // for the positive and negative buttons on the dialog.
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Changes not saved");
        builder.setPositiveButton("Discard", discardButtonClickListener);
        builder.setNegativeButton("Keep Editing", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Keep editing" button, so dismiss the dialog
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

    @Override
    public void onBackPressed() {
        if (!mItemHasChanged) {
            super.onBackPressed();
        } else {
            DialogInterface.OnClickListener discardButtonClickListener =
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        // User clicked "Discard" button, close the current activity.
                        finish();
                    }
                };

            // Show dialog that there are unsaved changes
            showUnsavedChangesDialog(discardButtonClickListener);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == android.R.id.home) {
            if (!mItemHasChanged) {
                NavUtils.navigateUpFromSameTask(InventoryEditorActivity.this);
                return true;
            }


        }

        // Otherwise if there are unsaved changes, setup a dialog to warn the user.
        // Create a click listener to handle the user confirming that
        // changes should be discarded.
        DialogInterface.OnClickListener discardButtonClickListener =
            new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    // User clicked "Discard" button, navigate to parent activity.
                    NavUtils.navigateUpFromSameTask(InventoryEditorActivity.this);
                }
            };

        // Show a dialog that notifies the user they have unsaved changes
        showUnsavedChangesDialog(discardButtonClickListener);

        return true;
    }
}
