package com.matejcerna.uploadexample;

import android.Manifest;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Cache;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

import static android.R.layout.simple_spinner_item;


public class MainActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener {

    @BindView(R.id.enter_item_name)
    EditText enterItemName;

    @BindView(R.id.image_view)
    ImageView imageView;

    @BindView(R.id.spinner)
    Spinner spinner;

    final int CODE_GALLERY_REQUEST = 999;
    Bitmap bitmap;
    private ArrayList<Category> categoryList;
    private ArrayList<String> categories = new ArrayList<String>();
    String string_category_id;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        categoryList = new ArrayList<>();
        spinner.setOnItemSelectedListener(this);
        fetchCategories();
    }

    @OnClick(R.id.save_button)
    public void onSaveClicked() {
        saveItem();
    }

    private void saveItem() {
        final String name = enterItemName.getText().toString();
        // String pricee = enterItemPrice.getText().toString();
        // final int price = Integer.parseInt(pricee);

        final ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Saving");

        if (name.isEmpty()) {
            enterItemName.setError("Enter item name");
        } else {
            progressDialog.show();
            String INSERT_URL = "https://low-pressure-lists.000webhostapp.com/upload_item.php";
            StringRequest request = new StringRequest(Request.Method.POST, INSERT_URL, new Response.Listener<String>() {
                @Override
                public void onResponse(String response) {
                    if (response.contains("success")) {
                        Toast.makeText(MainActivity.this, "Item saved!", Toast.LENGTH_SHORT).show();
                        progressDialog.dismiss();
                    } else {
                        Toast.makeText(MainActivity.this, response, Toast.LENGTH_SHORT).show();
                        progressDialog.dismiss();
                    }
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    Toast.makeText(MainActivity.this, error.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }) {
                @Override
                protected Map<String, String> getParams() throws AuthFailureError {
                    String image = imageToString(bitmap);
                    int category_id = Integer.parseInt(string_category_id);
                    Map<String, String> params = new HashMap<String, String>();

                    params.put("image", image);
                    params.put("name", name);
                    params.put("category_id", String.valueOf(category_id));
                    //params.put("price", String.valueOf(price));

                    enterItemName.setText("");

                    return params;
                }
            };
            RequestQueue requestQueue = Volley.newRequestQueue(this);
            requestQueue.add(request);
        }
    }


    @OnClick(R.id.choose_button)
    public void onChooseButtonClicked() {
        ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, CODE_GALLERY_REQUEST);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == CODE_GALLERY_REQUEST) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Intent intent = new Intent(Intent.ACTION_PICK);
                intent.setType("image/*");
                startActivityForResult(Intent.createChooser(intent, "Select image"), CODE_GALLERY_REQUEST);
            } else {
                Toast.makeText(this, "No access to gallery!", Toast.LENGTH_SHORT).show();
            }
            return;
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (requestCode == CODE_GALLERY_REQUEST && resultCode == RESULT_OK && data != null) {
            Uri filePath = data.getData();

            try {
                InputStream inputStream = getContentResolver().openInputStream(filePath);
                bitmap = BitmapFactory.decodeStream(inputStream);
                imageView.setImageBitmap(bitmap);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private String imageToString(Bitmap bitmap) {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream);
        byte[] imageBytes = byteArrayOutputStream.toByteArray();
        String encodedImage = Base64.encodeToString(imageBytes, Base64.DEFAULT);
        return encodedImage;
    }

    @OnClick(R.id.second_activity)
    public void onSecondButtonClicked() {
        Intent intent = new Intent(MainActivity.this, ViewItems.class);
        startActivity(intent);
    }

    private void fetchCategories() {
        String url = "https://low-pressure-lists.000webhostapp.com/fetch_categories.php";
        final ProgressDialog progressDialog = ProgressDialog.show(this, null, "Please wait");
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                if (progressDialog != null && progressDialog.isShowing()) {
                    progressDialog.dismiss();
                }
                GsonBuilder gsonBuilder = new GsonBuilder();
                Gson gson = gsonBuilder.create();
                Log.d("Codeeeee", String.valueOf(response));
                JSONObject jsonObject = null;
                try {
                    jsonObject = new JSONObject(String.valueOf(response));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                JSONArray jsonArray = null;
                try {
                    jsonArray = jsonObject.getJSONArray("category");
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                for (int i = 0; i < jsonArray.length(); i++) {
                    Category category = null;
                    try {
                        category = gson.fromJson(jsonArray.get(i).toString(), Category.class);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    categoryList.add(category);
                }

                for (int i = 0; i < categoryList.size(); i++){
                    categories.add(categoryList.get(i).getName());
                }

                ArrayAdapter<String> spinnerArrayAdapter = new ArrayAdapter<String>(MainActivity.this, simple_spinner_item, categories);
                spinnerArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item); // The drop down view
                spinner.setAdapter(spinnerArrayAdapter);


            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                final AlertDialog.Builder alertDialog = new AlertDialog.Builder(getApplicationContext());
                alertDialog.setMessage("Ups, došlo je do pogreške.").setCancelable(false)
                        .setPositiveButton("U redu", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                                progressDialog.dismiss();
                            }


                        });
                AlertDialog alert = alertDialog.create();
                alert.setTitle("Greška");
                alert.show();
                error.printStackTrace();
            }
        }) {
            @Override
            protected Response<JSONObject> parseNetworkResponse(NetworkResponse response) {
                Response<JSONObject> resp = super.parseNetworkResponse(response);
                if (!resp.isSuccess()) {
                    return resp;
                }
                long now = System.currentTimeMillis();
                Cache.Entry entry = resp.cacheEntry;
                if (entry == null) {
                    entry = new Cache.Entry();
                    entry.data = response.data;
                    entry.responseHeaders = response.headers;
                } else {
                    categoryList.clear();
                }
                entry.ttl = now + 300 * 1000;  //keeps cache for 5 min

                return Response.success(resp.result, entry);
            }
        };
        RequestQueue requestQueue = Volley.newRequestQueue(this);
        requestQueue.getCache().clear();
        requestQueue.add(jsonObjectRequest);
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long l) {
        Category category = categoryList.get(position);
        string_category_id = category.getId();
    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {

    }
}
