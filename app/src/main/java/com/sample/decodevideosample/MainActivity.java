package com.sample.decodevideosample;

import android.app.ProgressDialog;
import android.opengl.GLSurfaceView;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    private Spinner input, output;
    private ConvertUtils convertUtils;
    private ProgressDialog loading;
    private AlertDialog.Builder builder;
    private long startLoading;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        init();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return super.onOptionsItemSelected(item);
    }

    private void init() {
        loading = new ProgressDialog(this);
        loading.setMessage("Loading...");
        loading.setCancelable(false);

        builder = new AlertDialog.Builder(this);
        builder.setPositiveButton(getString(android.R.string.ok), null);

        input = (Spinner) findViewById(R.id.input);
        output = (Spinner) findViewById(R.id.output);

        findViewById(R.id.star_convert).setOnClickListener(this);

        ArrayAdapter<String> outputAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_list_item_1,
                android.R.id.text1,
                getResources().getStringArray(R.array.out));

        ArrayAdapter<String> inputAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_list_item_1,
                android.R.id.text1,
                getFiles());

        output.setAdapter(outputAdapter);
        input.setAdapter(inputAdapter);

    }

    private List<String> getFiles() {
        List<String> result = new ArrayList<>();

        Field[] fields = R.raw.class.getFields();
        for (int count = 0; count < fields.length; count++) {
            result.add(fields[count].getName());
        }

        return result;
    }

    private int getResourceID(String name, String directory) {
        return getResources().getIdentifier(name, directory, this.getPackageName());
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.star_convert:
                convert();
                break;
        }
    }

    private int[] getSize(int pos) {
        switch (pos) {
            case 0:
            default:
                return new int[]{320, 240};
            case 1:
                return new int[]{480, 320};
            case 2:
                return new int[]{720, 480};
            case 3:
                return new int[]{1280, 720};
        }
    }

    private void convert() {
        convertUtils = new ConvertUtils(this);
        int[] size = getSize(input.getSelectedItemPosition());
        convertUtils.setSize(size[0], size[1]);

        if (input.getAdapter().getCount() > 0) {
            int inputSelected = input.getSelectedItemPosition();
            int source = getResourceID(getFiles().get(inputSelected), "raw");
            convertUtils.setSource(source);
            convertUtils.setCopyVideo();
            convertUtils.setCopyAudio();
            convertUtils.setOutputFile();

            AsyncTaskExecutor.executeConcurrently(new AsyncTask<ConvertUtils, Object, Boolean>() {
                @Override
                protected Boolean doInBackground(ConvertUtils... params) {
                    ConvertUtils utils = params[0];
                    try {
                        utils.extractDecodeEditEncodeMux();
                        return true;
                    } catch (Exception e) {
                        return false;
                    }
                }

                @Override
                protected void onPreExecute() {
                    super.onPreExecute();
                    loading.show();
                    startLoading = System.currentTimeMillis();
                }

                @Override
                protected void onPostExecute(Boolean o) {
                    super.onPostExecute(o);
                    if (getApplicationContext() != null) {
                        loading.dismiss();
                    }

                    builder.setMessage(!o ? "Convert successful" +
                            "\nSpend time: " + (System.currentTimeMillis() - startLoading) : "Convert fail" +
                            "\nSpend time: " + (System.currentTimeMillis() - startLoading));
                    builder.show();


                }
            }, convertUtils);
        }
    }

}
