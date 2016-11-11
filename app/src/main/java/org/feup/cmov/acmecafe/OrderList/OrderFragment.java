package org.feup.cmov.acmecafe.OrderList;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;

import org.feup.cmov.acmecafe.MainActivity;
import org.feup.cmov.acmecafe.Models.Product;
import org.feup.cmov.acmecafe.Models.Voucher;
import org.feup.cmov.acmecafe.R;
import org.feup.cmov.acmecafe.Utils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.zip.GZIPOutputStream;


public class OrderFragment extends Fragment {
    private static final String ARG_ORDER_CONTENT = "order_content";
    private static final String ARG_ORDER_VOUCHERS = "order_vouchers";

    private HashMap<Product, Integer> mCurrentOrder = new HashMap<>();
    private ArrayList<Voucher> mOrderVouchers = new ArrayList<>();

    private OnOrderItemInteractionListener mListener;
    private OnOrderVoucherInteractionListener mVoucherListener;

    private RecyclerView.Adapter mOrderListAdapter;
    private RecyclerView.Adapter mOrderVoucherListAdapter;

    private TextView mPriceTextView;

    private Button mQRCodeButton;
    private ImageView mQRCodeImageView;
    private View mProgressView;

    public OrderFragment() {
    }

    public static OrderFragment newInstance(HashMap<Product, Integer> currentOrder, ArrayList<Voucher> vouchers) {
        OrderFragment fragment = new OrderFragment();
        Bundle args = new Bundle();
        args.putSerializable(ARG_ORDER_CONTENT, currentOrder);
        args.putSerializable(ARG_ORDER_VOUCHERS, vouchers);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mCurrentOrder = (HashMap<Product, Integer>) getArguments().getSerializable(ARG_ORDER_CONTENT);
            mOrderVouchers = (ArrayList<Voucher>) getArguments().getSerializable(ARG_ORDER_VOUCHERS);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_order, container, false);

        //price text view
        mPriceTextView = (TextView) view.findViewById(R.id.order_price);

        //qrcode image
        mQRCodeImageView = (ImageView) view.findViewById(R.id.qr_code_image);
        //progress view while generating qr code
        mProgressView = view.findViewById(R.id.qr_code_progress);

        //set up the products recycler view
        RecyclerView recyclerView = (RecyclerView) view.findViewById(R.id.order_list);
        recyclerView.setLayoutManager(new LinearLayoutManager(recyclerView.getContext()));
        mOrderListAdapter = new OrderAdapter(mCurrentOrder, mListener, mPriceTextView, mQRCodeImageView);
        recyclerView.setAdapter(mOrderListAdapter);
        setUpItemTouchHelper(recyclerView);
        setUpAnimationDecoratorHelper(recyclerView);

        //set up the vouchers recycler view
        RecyclerView voucherRecyclerView = (RecyclerView) view.findViewById(R.id.order_voucher_list);
        voucherRecyclerView.setLayoutManager(new LinearLayoutManager(recyclerView.getContext()));
        mOrderVoucherListAdapter = new OrderVoucherAdapter(mOrderVouchers, mVoucherListener, mPriceTextView, mQRCodeImageView);
        voucherRecyclerView.setAdapter(mOrderVoucherListAdapter);

        calculateOrderPrice(mCurrentOrder, mOrderVouchers, mPriceTextView);


        Button clearOrderButton = (Button) view.findViewById(R.id.clear_order_button);
        clearOrderButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mCurrentOrder.clear();
                mOrderListAdapter.notifyDataSetChanged();
                mOrderVouchers.clear();
                mOrderVoucherListAdapter.notifyDataSetChanged();
                List<Voucher> vouchers = Voucher.listAll(Voucher.class);
                for(Voucher voucher: vouchers) {
                    voucher.setIsUsed(false);
                    voucher.save();
                    calculateOrderPrice(mCurrentOrder, mOrderVouchers, mPriceTextView);
                }
            }
        });

        mQRCodeButton = (Button) view.findViewById(R.id.qr_code_button);
        mQRCodeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                askUserForPIN();
            }
        });
        if(mCurrentOrder.isEmpty()) {
            mQRCodeButton.setEnabled(false);
        }

        return view;
    }

    public static void calculateOrderPrice(HashMap<Product, Integer> products, ArrayList<Voucher> vouchers, TextView tv) {
        float price = Utils.calculateOrderPrice(products, vouchers);

        tv.setText("Total: " + String.valueOf(price) + "€");
    }

    private void askUserForPIN() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        LayoutInflater inflater = this.getActivity().getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_ask_pin, null);

        final EditText pinEditText = (EditText) dialogView.findViewById(R.id.ask_pin_input);

        builder.setTitle(R.string.dialog_ask_PIN_title)
                .setView(dialogView)
                .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if(Integer.parseInt(pinEditText.getText().toString()) == getUserPIN()) {
                            mQRCodeButton.setEnabled(false);
                            showProgress(true);
                            generateQRCode();
                        }
                        else {
                            showErrorSnackBar();
                        }

                        dialog.dismiss();
                    }
                }).setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void showErrorSnackBar() {
        Snackbar.make(getActivity().getCurrentFocus(), "Incorrect PIN", Snackbar.LENGTH_LONG)
                .setAction("Action", null).show();
    }

    private void generateQRCode() {
        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                String content = null;
                try {

                    JSONObject toSend = new JSONObject();
                    String uuid = getUserUUID();
                    toSend.put("uuid", uuid);

                    JSONArray products = new JSONArray();
                    for(int i = 0; i < mCurrentOrder.size(); i++) {
                        JSONObject product = new JSONObject();
                        Product item = (Product) mCurrentOrder.keySet().toArray()[i];
                        product.put("id", item.getProductId());
                        product.put("name", item.getName());
                        product.put("price", item.getPrice());
                        product.put("quantity", mCurrentOrder.get(item));
                        products.put(product);
                    }
                    toSend.put("products", products);

                    JSONArray vouchers = new JSONArray();
                    
                    for(int i = 0; i < mOrderVouchers.size(); i++) {
                        JSONObject voucher = new JSONObject();
                        Voucher item = mOrderVouchers.get(i);
                        voucher.put("id", i);
                        voucher.put("voucher_id", item.getVoucherId());
                        voucher.put("name", item.getName());
                        voucher.put("type", item.getType());
                        voucher.put("signature", item.getSignature());
                        vouchers.put(voucher);
                    }
                    toSend.put("vouchers", vouchers);

                    byte[] toSendBytes = toSend.toString().getBytes();

                    ByteArrayOutputStream os = new ByteArrayOutputStream(toSend.toString().length());
                    GZIPOutputStream gos = new GZIPOutputStream(os);
                    gos.write(toSend.toString().getBytes());
                    gos.close();
                    byte[] compressed = os.toByteArray();
                    os.close();

                    JSONObject toSend2 = new JSONObject();
                    String uuid2 = getUserUUID();
                    toSend2.put("uuid", uuid);

                    JSONArray products2 = new JSONArray();
                    for(int i = 0; i < mCurrentOrder.size(); i++) {
                        JSONObject product = new JSONObject();
                        Product item = (Product) mCurrentOrder.keySet().toArray()[i];
                        product.put("id", item.getProductId());
                        product.put("name", item.getName());
                        product.put("price", item.getPrice());
                        product.put("quantity", mCurrentOrder.get(item));
                        products2.put(product);
                    }
                    toSend2.put("products", products2);

                    JSONArray vouchers2 = new JSONArray();

                    for(int i = 0; i < mOrderVouchers.size(); i++) {
                        JSONObject voucher = new JSONObject();
                        Voucher item = mOrderVouchers.get(i);
                        voucher.put("id", i);
                        voucher.put("voucher_id", item.getVoucherId());
                        //voucher.put("name", item.getName());
                        voucher.put("type", item.getType());
                        voucher.put("signature", item.getSignature());
                        vouchers2.put(voucher);
                    }
                    toSend2.put("vouchers", vouchers2);

                    byte[] toSendBytes2 = toSend2.toString().getBytes();

                    ByteArrayOutputStream os2 = new ByteArrayOutputStream(toSend2.toString().length());
                    GZIPOutputStream gos2 = new GZIPOutputStream(os2);
                    gos2.write(toSend2.toString().getBytes());
                    gos2.close();
                    byte[] compressed2 = os2.toByteArray();
                    os2.close();

                    Log.d("BEFORE COMPRESSION WITH NAME", String.valueOf(toSendBytes.length));
                    Log.d("BEFORE COMPRESSION NO NAME", String.valueOf(toSendBytes2.length));
                    Log.d("AFTER COMPRESSION WITH NAME", String.valueOf(compressed.length));
                    Log.d("AFTER COMPRESSION NO NAME", String.valueOf(compressed2.length));

                    content = new String(toSendBytes, "ISO-8859-1");
                } catch (JSONException | IOException e) {
                    e.printStackTrace();
                }
                try {
                    final Bitmap bitmap = encodeAsBitmap(content);
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            showProgress(false);
                            mQRCodeButton.setEnabled(true);
                            mQRCodeImageView.setImageBitmap(bitmap);
                        }
                    });

                } catch (WriterException e) {
                    e.printStackTrace();
                }
            }
        });
        t.start();
    }

    private int calculateOrderSize() {
        //int, string, float, int
        int productsSize = 0;

        for(int i = 0; i < mCurrentOrder.size(); i++) {
            Product current = (Product) mCurrentOrder.keySet().toArray()[i];
            productsSize += 32 + 32 + 32 + current.getName().getBytes().length;
        }

        //int, int, int, String, String
        int vouchersSize = 0;

        for(int i = 0; i < mOrderVouchers.size(); i++) {
            Voucher current = mOrderVouchers.get(i);
            vouchersSize += 32 + 32 + 32;
            vouchersSize += current.getName().getBytes().length;
            vouchersSize += current.getSignature().getBytes().length;
        }

        return 32 + 32 + vouchersSize + productsSize;

    }

    private String getUserUUID() {
        SharedPreferences sharedPref = getActivity().getSharedPreferences(getString(R.string.user_details_prefs),Context.MODE_PRIVATE);
        String defaultValue = "Could not get UUID from shared preferences";
        return sharedPref.getString("uuid", defaultValue);
    }

    private int getUserPIN() {
        SharedPreferences sharedPref = getActivity().getSharedPreferences(getString(R.string.user_details_prefs),Context.MODE_PRIVATE);
        int defaultValue = 0000;
        return sharedPref.getInt("pin", defaultValue);
    }

    private Bitmap encodeAsBitmap(String content) throws WriterException {
        BitMatrix result;
        try {
            result = new MultiFormatWriter().encode(content, BarcodeFormat.QR_CODE, 500, 500, null);
        }
        catch (IllegalArgumentException iae) {
            // Unsupported format
            return null;
        }
        int w = result.getWidth();
        int h = result.getHeight();
        int[] pixels = new int[w * h];
        for (int y = 0; y < h; y++) {
            int offset = y * w;
            for (int x = 0; x < w; x++) {
                pixels[offset + x] = result.get(x, y) ? getResources().getColor(R.color.colorPrimary):getResources().getColor(R.color.white);
            }
        }
        Bitmap bitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        bitmap.setPixels(pixels, 0, 500, 0, 0, w, h);
        return bitmap;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnOrderItemInteractionListener && context instanceof OnOrderVoucherInteractionListener) {
            mListener = (OnOrderItemInteractionListener) context;
            mVoucherListener = (OnOrderVoucherInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnOrderItemInteractionListener and OnOrderVoucherInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @Override
    public void onResume() {
        super.onResume();
        MainActivity activity = (MainActivity) getActivity();
        activity.setToolbarTitle("Current Order");
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    private void showProgress(final boolean show) {
        // On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
        // for very easy animations. If available, use these APIs to fade-in
        // the progress spinner.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

            mQRCodeImageView.setVisibility(show ? View.GONE : View.VISIBLE);
            mQRCodeImageView.animate().setDuration(shortAnimTime).alpha(
                    show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mQRCodeImageView.setVisibility(show ? View.GONE : View.VISIBLE);
                }
            });

            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mProgressView.animate().setDuration(shortAnimTime).alpha(
                    show ? 1 : 0).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
                }
            });
        } else {
            // The ViewPropertyAnimator APIs are not available, so simply show
            // and hide the relevant UI components.
            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mQRCodeImageView.setVisibility(show ? View.GONE : View.VISIBLE);
        }
    }

    private void setUpItemTouchHelper(final RecyclerView rv) {

        ItemTouchHelper.SimpleCallback simpleItemTouchCallback = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {

            // we want to cache these and not allocate anything repeatedly in the onChildDraw method
            Drawable background;
            Drawable xMark;
            int xMarkMargin;
            boolean initiated;

            private void init() {
                background = new ColorDrawable(Color.RED);
                xMark = ContextCompat.getDrawable(getActivity(), R.drawable.ic_remove_all);
                xMark.setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_ATOP);
                xMarkMargin = (int) getActivity().getResources().getDimension(R.dimen.activity_horizontal_margin);
                initiated = true;
            }

            // not important, we don't want drag & drop
            @Override
            public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public int getSwipeDirs(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder) {
                return super.getSwipeDirs(recyclerView, viewHolder);
                /*
                int position = viewHolder.getAdapterPosition();
                TestAdapter testAdapter = (TestAdapter)recyclerView.getAdapter();
                if (testAdapter.isUndoOn() && testAdapter.isPendingRemoval(position)) {
                    return 0;
                }
                return super.getSwipeDirs(recyclerView, viewHolder);
                */
            }

            @Override
            public void onSwiped(final RecyclerView.ViewHolder viewHolder, int swipeDir) {
                final Product item = (Product) mCurrentOrder.keySet().toArray()[viewHolder.getAdapterPosition()];
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        mListener.onItemHardRemove(item, viewHolder.getAdapterPosition(), rv.getAdapter(), mPriceTextView);
                    }
                }, 300);

                /*
                int swipedPosition = viewHolder.getAdapterPosition();
                TestAdapter adapter = (TestAdapter)mRecyclerView.getAdapter();
                boolean undoOn = adapter.isUndoOn();
                if (undoOn) {
                    adapter.pendingRemoval(swipedPosition);
                } else {
                    adapter.remove(swipedPosition);
                }
                */
            }

            @Override
            public void onChildDraw(Canvas c, RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, float dX, float dY, int actionState, boolean isCurrentlyActive) {
                View itemView = viewHolder.itemView;

                // not sure why, but this method get's called for viewholder that are already swiped away
                if (viewHolder.getAdapterPosition() == -1) {
                    // not interested in those
                    return;
                }

                if (!initiated) {
                    init();
                }

                // draw red background
                background.setBounds(itemView.getRight() + (int) dX, itemView.getTop(), itemView.getRight(), itemView.getBottom());
                background.draw(c);

                // draw x mark
                int itemHeight = itemView.getBottom() - itemView.getTop();
                int intrinsicWidth = xMark.getIntrinsicWidth();
                int intrinsicHeight = xMark.getIntrinsicWidth();

                int xMarkLeft = itemView.getRight() - xMarkMargin - intrinsicWidth;
                int xMarkRight = itemView.getRight() - xMarkMargin;
                int xMarkTop = itemView.getTop() + (itemHeight - intrinsicHeight)/2;
                int xMarkBottom = xMarkTop + intrinsicHeight;
                xMark.setBounds(xMarkLeft, xMarkTop, xMarkRight, xMarkBottom);

                if(dX == 0 && dY == 0) {
                    xMark.setColorFilter(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
                }
                else {
                    xMark.setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_ATOP);
                }

                xMark.draw(c);

                super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
            }

        };
        ItemTouchHelper mItemTouchHelper = new ItemTouchHelper(simpleItemTouchCallback);
        mItemTouchHelper.attachToRecyclerView(rv);
    }

    private void setUpAnimationDecoratorHelper(RecyclerView rv) {
        rv.addItemDecoration(new RecyclerView.ItemDecoration() {

            // we want to cache this and not allocate anything repeatedly in the onDraw method
            Drawable background;
            boolean initiated;

            private void init() {
                background = new ColorDrawable(Color.RED);
                initiated = true;
            }

            @Override
            public void onDraw(Canvas c, RecyclerView parent, RecyclerView.State state) {

                if (!initiated) {
                    init();
                }

                // only if animation is in progress
                if (parent.getItemAnimator().isRunning()) {

                    // some items might be animating down and some items might be animating up to close the gap left by the removed item
                    // this is not exclusive, both movement can be happening at the same time
                    // to reproduce this leave just enough items so the first one and the last one would be just a little off screen
                    // then remove one from the middle

                    // find first child with translationY > 0
                    // and last one with translationY < 0
                    // we're after a rect that is not covered in recycler-view views at this point in time
                    View lastViewComingDown = null;
                    View firstViewComingUp = null;

                    // this is fixed
                    int left = 0;
                    int right = parent.getWidth();

                    // this we need to find out
                    int top = 0;
                    int bottom = 0;

                    // find relevant translating views
                    int childCount = parent.getLayoutManager().getChildCount();
                    for (int i = 0; i < childCount; i++) {
                        View child = parent.getLayoutManager().getChildAt(i);
                        if (child.getTranslationY() < 0) {
                            // view is coming down
                            lastViewComingDown = child;
                        } else if (child.getTranslationY() > 0) {
                            // view is coming up
                            if (firstViewComingUp == null) {
                                firstViewComingUp = child;
                            }
                        }
                    }

                    if (lastViewComingDown != null && firstViewComingUp != null) {
                        // views are coming down AND going up to fill the void
                        top = lastViewComingDown.getBottom() + (int) lastViewComingDown.getTranslationY();
                        bottom = firstViewComingUp.getTop() + (int) firstViewComingUp.getTranslationY();
                    } else if (lastViewComingDown != null) {
                        // views are going down to fill the void
                        top = lastViewComingDown.getBottom() + (int) lastViewComingDown.getTranslationY();
                        bottom = lastViewComingDown.getBottom();
                    } else if (firstViewComingUp != null) {
                        // views are coming up to fill the void
                        top = firstViewComingUp.getTop();
                        bottom = firstViewComingUp.getTop() + (int) firstViewComingUp.getTranslationY();
                    }

                    background.setBounds(left, top, right, bottom);
                    background.draw(c);

                }
                super.onDraw(c, parent, state);
            }

        });
    }

    public interface OnOrderItemInteractionListener {
        void onItemRemove(Product item, int pos, RecyclerView.Adapter adapter, TextView priceTV);
        void onItemHardRemove(Product item, int pos, RecyclerView.Adapter adapter, TextView priceTV);
    }

    public interface OnOrderVoucherInteractionListener {
        void onVoucherRemove(Voucher item, int pos, RecyclerView.Adapter adapter, TextView priceTV);
    }
}
