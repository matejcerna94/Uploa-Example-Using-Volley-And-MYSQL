package com.matejcerna.uploadexample;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.EditText;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import java.util.HashMap;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class MainActivity extends AppCompatActivity {

    @BindView(R.id.enter_item_name)
    EditText enterItemName;
    @BindView(R.id.enter_item_price)
    EditText enterItemPrice;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
    }

    @OnClick(R.id.save_button)
    public void onViewClicked() {
        saveItem();
    }

    private void saveItem() {
        final String name = enterItemName.getText().toString();
        String pricee = enterItemPrice.getText().toString();
        final int price = Integer.parseInt(pricee);

        final ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Saving");

        if(name.isEmpty()){
            enterItemName.setError("Enter item name");
        }else if(pricee.isEmpty()){
            enterItemPrice.setError("Enter item price");
        }else{
            progressDialog.show();
            String INSERT_URL = "https://low-pressure-lists.000webhostapp.com/insert.php";
            StringRequest request = new StringRequest(Request.Method.POST, INSERT_URL, new Response.Listener<String>() {
                @Override
                public void onResponse(String response) {
                    if(response.equalsIgnoreCase("Successful!")){
                        Toast.makeText(MainActivity.this, "Item saved!", Toast.LENGTH_SHORT).show();
                        progressDialog.dismiss();
                    }else{
                        Toast.makeText(MainActivity.this, response, Toast.LENGTH_SHORT).show();
                        progressDialog.dismiss();
                    }
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    Toast.makeText(MainActivity.this, error.getMessage(), Toast.LENGTH_SHORT).show();
                }
            })
            {
                @Override
                protected Map<String, String> getParams() throws AuthFailureError {
                    Map<String, String> params = new HashMap<String, String>();
                    params.put("name", name);
                    params.put("price", String.valueOf(price));
                    return params;
                }
            };
            RequestQueue requestQueue = Volley.newRequestQueue(this);
            requestQueue.add(request);
        }
    }
}
