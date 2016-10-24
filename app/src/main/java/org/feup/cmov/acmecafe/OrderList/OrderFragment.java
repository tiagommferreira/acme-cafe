package org.feup.cmov.acmecafe.OrderList;

import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import org.feup.cmov.acmecafe.Models.CafeItem;
import org.feup.cmov.acmecafe.MainActivity;
import org.feup.cmov.acmecafe.Models.Voucher;
import org.feup.cmov.acmecafe.R;
import org.feup.cmov.acmecafe.VoucherList.VoucherListAdapter;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;


public class OrderFragment extends Fragment {
    private static final String ARG_ORDER_CONTENT = "order_content";
    private static final String ARG_ORDER_VOUCHERS = "order_vouchers";

    private HashMap<CafeItem, Integer> mCurrentOrder = new HashMap<>();
    private ArrayList<Voucher> mOrderVouchers = new ArrayList<>();

    private OnOrderItemInteracionListener mListener;
    private OnOrderVoucherInteractionListener mVoucherListener;

    private RecyclerView.Adapter mOrderListAdapter;
    private RecyclerView.Adapter mOrderVoucherListAdapter;

    private ImageView mQRCodeImageView;

    public OrderFragment() {
    }

    public static OrderFragment newInstance(HashMap<CafeItem, Integer> currentOrder, ArrayList<Voucher> vouchers) {
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
            mCurrentOrder = (HashMap<CafeItem, Integer>) getArguments().getSerializable(ARG_ORDER_CONTENT);
            mOrderVouchers = (ArrayList<Voucher>) getArguments().getSerializable(ARG_ORDER_VOUCHERS);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_order, container, false);

        RecyclerView recyclerView = (RecyclerView) view.findViewById(R.id.order_list);
        recyclerView.setLayoutManager(new LinearLayoutManager(recyclerView.getContext()));
        mOrderListAdapter = new OrderAdapter(mCurrentOrder, mListener);
        recyclerView.setAdapter(mOrderListAdapter);

        RecyclerView voucherRecyclerView = (RecyclerView) view.findViewById(R.id.order_voucher_list);
        voucherRecyclerView.setLayoutManager(new LinearLayoutManager(recyclerView.getContext()));
        mOrderVoucherListAdapter = new OrderVoucherAdapter(mOrderVouchers, mVoucherListener);
        voucherRecyclerView.setAdapter(mOrderVoucherListAdapter);

        mQRCodeImageView = (ImageView) view.findViewById(R.id.qr_code_image);

        Button generateQRCodeButton = (Button) view.findViewById(R.id.qr_code_button);
        generateQRCodeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                askUserForPIN();
            }
        });

        return view;
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
                String content = "";
                try {
                    JSONObject toSend = new JSONObject();
                    String uuid = getUserUUID();
                    toSend.put("uuid", uuid);

                    JSONArray products = new JSONArray();
                    for(int i = 0; i < mCurrentOrder.size(); i++) {
                        JSONObject product = new JSONObject();
                        CafeItem item = (CafeItem) mCurrentOrder.keySet().toArray()[i];
                        product.put("id", item.getId());
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
                        voucher.put("id", item.getId());
                        voucher.put("name", item.getName());
                        voucher.put("type", item.getType());
                        voucher.put("signature", item.getSignature());
                        vouchers.put(voucher);
                    }
                    toSend.put("vouchers", vouchers);

                    byte[] toSendBytes = toSend.toString().getBytes();
                    content = new String(toSendBytes, "ISO-8859-1");
                } catch (UnsupportedEncodingException | JSONException e) {
                    e.printStackTrace();
                }
                try {
                    final Bitmap bitmap = encodeAsBitmap(content);
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
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
        if (context instanceof OnOrderItemInteracionListener && context instanceof OnOrderVoucherInteractionListener) {
            mListener = (OnOrderItemInteracionListener) context;
            mVoucherListener = (OnOrderVoucherInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnOrderItemInteracionListener and OnOrderVoucherInteractionListener");
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

    public interface OnOrderItemInteracionListener {
        void onItemInteraction(CafeItem item);
    }

    public interface OnOrderVoucherInteractionListener {
        void onVoucherInteraction(Voucher item);
    }
}
