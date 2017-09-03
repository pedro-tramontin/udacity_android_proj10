package example.com.br.inventoryapp;

import android.app.Activity;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.ParcelFileDescriptor;
import android.provider.MediaStore;
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
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.ByteArrayOutputStream;
import java.io.FileDescriptor;
import java.io.IOException;
import java.text.DecimalFormat;
import java.text.ParseException;

import butterknife.BindView;
import butterknife.ButterKnife;
import example.com.br.inventoryapp.InventoryContract.InventoryEntry;

import static android.view.View.GONE;


/**
 * Activity class to edit an inventory product
 */
public class InventoryEditorActivity extends AppCompatActivity implements LoaderCallbacks<Cursor> {

    // Used to save the activity state
    public static final String PROD_NAME_KEY = "mProductName";
    public static final String PROD_PRICE_KEY = "mProductPrice";
    public static final String PROD_QUANTITY_KEY = "mProductQuantity";
    public static final String PROD_BITMAP_KEY = "mProductBitmap";
    private static final String TAG = InventoryEditorActivity.class.getSimpleName();
    // Loader ID
    private static final int INVENTORY_ITEM_LOADER = 2;
    // Requests code for Camera and File Picker
    private static final int REQUEST_IMAGE_CAPTURE = 1;
    private static final int REQUEST_IMAGE_FILE = 2;
    // Product data
    @BindView(R.id.product_name)
    EditText mProductName;

    @BindView(R.id.product_price)
    EditText mProductPrice;

    @BindView(R.id.text_product_quantity)
    TextView mProductQuantity;

    @BindView(R.id.product_picture)
    ImageView mProductPicture;


    // Product quantity auxiliar views
    @BindView(R.id.product_quantity_step)
    EditText mQuantityStep;

    @BindView(R.id.button_rise_product_quantity)
    ImageButton mBtnRiseStep;

    @BindView(R.id.button_lower_product_quantity)
    ImageButton mBtnLowerStep;


    // Buttons for product picture
    @BindView(R.id.button_call_camera)
    ImageButton mBtnCallCamera;

    @BindView(R.id.button_call_file_pick)
    ImageButton mBtnCallFilePick;


    // Buttons for saving, deleting and ordering
    @BindView(R.id.button_save_inventory)
    Button mBtnSave;

    @BindView(R.id.button_delete_inventory)
    Button mBtnDelete;

    @BindView(R.id.button_order_product)
    Button mBtnOrder;


    // Uri, Picture Bitmap, Product instance
    private Uri itemUri;

    private Bitmap mProductBitmap;

    private Product mProduct;


    // Flags
    private boolean mItemHasChanged = false;

    private String mLastText = "";

    private boolean mEditMode = false;


    // Listener to detect real text change
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

        setProductEditorMode();
        resetProductFields();
        addListenersToViews();
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
                            finish();
                        }
                    };

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

        DialogInterface.OnClickListener discardButtonClickListener =
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        NavUtils.navigateUpFromSameTask(InventoryEditorActivity.this);
                    }
                };

        showUnsavedChangesDialog(discardButtonClickListener);

        return true;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == Activity.RESULT_OK) {

            // Process the return from the camera
            if (requestCode == REQUEST_IMAGE_CAPTURE) {
                Bundle extras = data.getExtras();

                // Use only the thumbnail since the ImageView is very small
                mProductBitmap = (Bitmap) extras.get("data");
                mProductPicture.setImageBitmap(mProductBitmap);

            } else

                // Process the return from the file pick
                if (requestCode == REQUEST_IMAGE_FILE) {
                    if (data != null) {
                        Uri uri = data.getData();
                        try {
                            setBitmapFromUri(uri);
                        } catch (IOException e) {
                            Log.e(TAG, Utils.getString(this, R.string.error_get_image_from_uri), e);
                        }
                    }
                }
        }

        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putString(PROD_NAME_KEY, mProductName.getText().toString());
        outState.putString(PROD_PRICE_KEY, mProductPrice.getText().toString());
        outState.putString(PROD_QUANTITY_KEY, mProductQuantity.getText().toString());
        outState.putParcelable(PROD_BITMAP_KEY, mProductBitmap);

        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onRestoreInstanceState(Bundle inState) {
        super.onRestoreInstanceState(inState);

        mProductName.setText(inState.getString(PROD_NAME_KEY));
        mProductPrice.setText(inState.getString(PROD_PRICE_KEY));
        mProductQuantity.setText(inState.getString(PROD_QUANTITY_KEY));
        mProductBitmap = inState.getParcelable(PROD_BITMAP_KEY);

        mProductPicture.setImageBitmap(mProductBitmap);
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

        return new CursorLoader(this, itemUri, projection, null,
                null, null);
    }

    @Override
    public void onLoadFinished(Loader loader, Cursor cursor) {
        if (cursor.moveToFirst()) {
            mProduct = new Product(cursor);

            disableLowerButtonOnZeroQuantity(mProduct.getQuantity());
            setProductDataToViews();

            // Sets delete button listener here because it's gone in add mode
            mBtnDelete.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View view) {
                    showDeleteConfirmationDialog(InventoryEditorActivity.this, mProduct.getId());
                }
            });
        }
    }

    @Override
    public void onLoaderReset(Loader loader) {
        resetProductFields();
    }

    private void setProductEditorMode() {
        itemUri = getIntent().getData();
        if (itemUri != null) {
            setEditMode();
        } else {
            setAddMode();
        }
    }

    private void setEditMode() {
        mEditMode = true;

        setTitle(Utils.getString(this, R.string.edit_mode_title));

        getSupportLoaderManager().initLoader(INVENTORY_ITEM_LOADER, null, this);
    }

    private void setAddMode() {
        mEditMode = false;

        setTitle(Utils.getString(this, R.string.add_mode_title));

        mBtnDelete.setVisibility(GONE);
    }

    private void resetProductFields() {
        mProductName.setText("");
        mProductQuantity.setText("0");
        mProductPrice.setText("");
        mQuantityStep.setText("1");

        mBtnLowerStep.setEnabled(false);
    }

    private void addListenersToViews() {
        mProductName.addTextChangedListener(mTextListener);
        mProductPrice.addTextChangedListener(mTextListener);

        mBtnSave.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                saveItem();
            }
        });

        mBtnOrder.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                makeOrder();
            }
        });

        mBtnLowerStep.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                mItemHasChanged = true;

                int quantity = getProductQuantity();
                int step = getProductStep();

                quantity = quantity - step;
                if (quantity < 0) {
                    quantity = 0;
                }

                mProductQuantity.setText(Utils.format("%d", quantity));

                disableLowerButtonOnZeroQuantity(quantity);
            }
        });

        mBtnRiseStep.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                mItemHasChanged = true;

                int quantity = getProductQuantity();
                int step = getProductStep();

                quantity = quantity + step;

                mProductQuantity.setText(Utils.format("%d", quantity));

                mBtnLowerStep.setEnabled(true);
            }
        });

        mBtnCallCamera.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                callCamera();
            }
        });

        mBtnCallFilePick.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                callFilePick();
            }
        });
    }

    private void saveItem() {
        if (areFieldsValid()) {
            ContentValues values = new ContentValues();
            values.put(InventoryEntry.COLUMN_NAME_NAME, mProductName.getText().toString());
            values.put(InventoryEntry.COLUMN_NAME_QUANTITY, getProductQuantity());
            values.put(InventoryEntry.COLUMN_NAME_PRICE, getProductPrice());
            values.put(InventoryEntry.COLUMN_NAME_PICTURE, getBitmapArray());

            if (mEditMode) {
                updateProduct(values);
            } else {
                insertProduct(values);
            }

            finish();
        }
    }

    private boolean areFieldsValid() {
        if (TextUtils.isEmpty(mProductName.getText())) {
            Utils.shortToast(this, Utils.getString(this, R.string.name_required));

            return false;
        }

        if (TextUtils.isEmpty(mProductPrice.getText())) {
            Utils.shortToast(this, Utils.getString(this, R.string.price_required));

            return false;
        }

        if (mProductBitmap == null) {
            Utils.shortToast(this, Utils.getString(this, R.string.picture_required));

            return false;
        }

        return true;
    }

    private void makeOrder() {
        if (mProduct != null) {
            String subject = Utils.format(Utils.getString(this, R.string.email_subject),
                    mProduct.getName());
            String body = Utils.format(Utils.getString(this, R.string.email_body), mProduct
                    .getName(), mProduct.getQuantity(), mProduct.getPrice());

            Intent intent = new Intent(Intent.ACTION_SENDTO);
            intent.setData(Uri.parse("mailto:")); // only email apps should handle this
            intent.putExtra(Intent.EXTRA_SUBJECT, subject);
            intent.putExtra(Intent.EXTRA_TEXT, body);
            if (intent.resolveActivity(getPackageManager()) != null) {
                startActivity(intent);
            }
        } else {
            Utils.shortToast(this, Utils.getString(this, R.string.error_save_before_ordering));
        }
    }

    private void disableLowerButtonOnZeroQuantity(int quantity) {
        if (quantity == 0) {
            mBtnLowerStep.setEnabled(false);
        }
    }

    private void callCamera() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
        }
    }

    public void callFilePick() {
        Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
        photoPickerIntent.setType("image/*");
        startActivityForResult(photoPickerIntent, REQUEST_IMAGE_FILE);
    }

    private void setProductDataToViews() {
        mProductName.setText(mProduct.getName());
        mProductQuantity.setText(Utils.format("%d", mProduct.getQuantity()));
        mProductPrice.setText(Utils.format("%.2f", mProduct.getPrice()));

        mProductBitmap = BitmapFactory.decodeByteArray(mProduct.getPicture(), 0, mProduct
                .getPicture().length);
        mProductPicture.setImageBitmap(mProductBitmap);

        // Sets the flag to false because it is true after setting data in the views from the cursor
        mItemHasChanged = false;
    }

    private int getProductQuantity() {
        return Integer.valueOf(mProductQuantity.getText().toString());
    }

    private float getProductPrice() {
        try {
            return Float.parseFloat(mProductPrice.getText().toString());
        } catch (NumberFormatException e) {
            DecimalFormat df = new DecimalFormat();
            try {
                Number number = df.parse(mProductPrice.getText().toString());
                if (number != null) {
                    return number.floatValue();
                }

                Log.e(TAG, Utils.getString(this, R.string.error_parsing_price_failed));
            } catch (ParseException ex) {
                Log.e(TAG, Utils.getString(this, R.string.error_parsing_price_exception), ex);
            }
        }

        return 0.0f;
    }

    private int getProductStep() {
        String textStep = mQuantityStep.getText().toString();

        if (!"".equals(textStep)) {
            return Integer.valueOf(textStep);
        }

        return 1;
    }

    private void showUnsavedChangesDialog(DialogInterface.OnClickListener
                                                  discardButtonClickListener) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(Utils.getString(this, R.string.dialog_title_not_saved));
        builder.setPositiveButton(Utils.getString(this, R.string.dialog_not_saved_positive)
                , discardButtonClickListener);
        builder.setNegativeButton(Utils.getString(this, R.string.dialog_not_saved_negative)
                , new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        if (dialog != null) {
                            dialog.dismiss();
                        }
                    }
                });

        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    private void showDeleteConfirmationDialog(final Context context, final int rowId) {

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setMessage(Utils.getString(this, R.string.dialog_title_delete));
        builder.setPositiveButton(Utils.getString(this, R.string.dialog_delete_positive),
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        deleteProduct(context, rowId);

                        finish();
                    }
                });
        builder.setNegativeButton(Utils.getString(this, R.string.dialog_delete_negative),
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        if (dialog != null) {
                            dialog.dismiss();
                        }
                    }
                });

        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    private void insertProduct(ContentValues values) {
        Uri uri = getContentResolver().insert(InventoryEntry.CONTENT_URI, values);
        if (uri == null) {
            Utils.shortToast(this, Utils.getString(this, R.string.error_insert));
        }
    }

    private void updateProduct(ContentValues values) {
        int rowCount = getContentResolver().update(itemUri, values, null, null);
        if (rowCount == 0) {
            Utils.shortToast(this, Utils.getString(this, R.string.error_update));
        }
    }

    private void deleteProduct(Context context, int id) {
        Uri itemUri = ContentUris.withAppendedId(InventoryEntry.CONTENT_URI, id);

        int rowsDeleted = context.getContentResolver()
                .delete(itemUri, null, null);

        if (rowsDeleted == 0) {
            Utils.shortToast(this, Utils.getString(this, R.string.error_delete));
        }
    }

    private void setBitmapFromUri(Uri uri) throws IOException {
        ParcelFileDescriptor parcelFileDescriptor =
                getContentResolver().openFileDescriptor(uri, "r");
        if (parcelFileDescriptor != null) {
            FileDescriptor fileDescriptor = parcelFileDescriptor.getFileDescriptor();

            mProductBitmap = Utils.scaleBitmapIfNeeded(fileDescriptor);
            mProductBitmap = Utils.rotateBitmapIfNeed(this, uri, mProductBitmap);

            mProductPicture.setImageBitmap(mProductBitmap);

            parcelFileDescriptor.close();
        } else {
            Log.e(TAG, Utils.getString(this, R.string.error_opening_file));
        }
    }

    public byte[] getBitmapArray() {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        mProductBitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
        return stream.toByteArray();
    }
}
