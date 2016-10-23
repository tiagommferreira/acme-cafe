package org.feup.cmov.acmecafe.OrderList;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;

import org.feup.cmov.acmecafe.CafeItem;
import org.feup.cmov.acmecafe.MainActivity;
import org.feup.cmov.acmecafe.R;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;


public class OrderFragment extends Fragment {
    private static final String ARG_ORDER_CONTENT = "order_content";

    private HashMap<CafeItem, Integer> mCurrentOrder;

    private OnOrderItemInteracionListener mListener;

    private RecyclerView.Adapter mOrderListAdapter;

    private ImageView mQRCodeImageView;


    public OrderFragment() {
    }

    public static OrderFragment newInstance(HashMap<CafeItem, Integer> currentOrder) {
        OrderFragment fragment = new OrderFragment();
        Bundle args = new Bundle();
        args.putSerializable(ARG_ORDER_CONTENT, currentOrder);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mCurrentOrder = (HashMap<CafeItem, Integer>) getArguments().getSerializable(ARG_ORDER_CONTENT);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_order, container, false);

        Log.d("OrderFragment", "Size: " + String.valueOf(mCurrentOrder.size()));

        RecyclerView recyclerView = (RecyclerView) view.findViewById(R.id.order_list);
        recyclerView.setLayoutManager(new LinearLayoutManager(recyclerView.getContext()));
        mOrderListAdapter = new OrderAdapter(mCurrentOrder, mListener);
        recyclerView.setAdapter(mOrderListAdapter);

        mQRCodeImageView = (ImageView) view.findViewById(R.id.qr_code_image);

        Button generateQRCodeButton = (Button) view.findViewById(R.id.qr_code_button);
        generateQRCodeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                generateQRCode();
            }
        });

        return view;
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
        SharedPreferences sharedPref = getActivity().getPreferences(Context.MODE_PRIVATE);
        String defaultValue = "Could not get UUID from shared preferences";
        return sharedPref.getString("uuid", defaultValue);
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
        if (context instanceof OnOrderItemInteracionListener) {
            mListener = (OnOrderItemInteracionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnOrderItemInteracionListener");
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
}
