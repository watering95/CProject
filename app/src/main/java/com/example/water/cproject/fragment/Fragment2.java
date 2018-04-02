package com.example.water.cproject.fragment;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.example.water.cproject.DBResolver;
import com.example.water.cproject.machine.InfoCode;
import com.example.water.cproject.machine.InfoMachine;
import com.example.water.cproject.ListCodeAdapter;
import com.example.water.cproject.MainActivity;
import com.example.water.cproject.R;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by watering on 18. 3. 15.
 */

@SuppressWarnings("DefaultFileTemplate")
public class Fragment2 extends Fragment {

    private View mView;
    private WebView mWeb;
    private MainActivity mainActivity;
    private static final String TAG = "CProject";
    private DBResolver resolver;
    private ArrayList<String> listsCode;
    private ListCodeAdapter listAdapter;
    private ListView listView;

    public Fragment2() {
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mainActivity = (MainActivity) getActivity();
        assert mainActivity != null;
        resolver = mainActivity.resolver;

        mainActivity.setFrag2Callback(new MainActivity.Frag2Callback() {
            @Override
            public void updateView() {
                Fragment2.this.updateView();
            }
        });
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        mView = inflater.inflate(R.layout.fragment2, container, false);

        initLayout();
        openWebView();

        return mView;
    }

    private void initLayout() {
        listsCode = resolver.getCodes(mainActivity.getToday());
        listView = mView.findViewById(R.id.listView_frag2);
        listAdapter = new ListCodeAdapter(getContext(), listsCode);
        if(listsCode != null) {
            listView.setAdapter(listAdapter);
            makeHTMLFile(listsCode.get(0));
        }

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                makeHTMLFile(listsCode.get(position));
                mWeb.reload();

            }
        });
        listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                    UserDialogFragment dialog = UserDialogFragment.Companion.newInstance(position, new UserDialogFragment.UserListener() {
                        @Override
                        public void onWorkComplete() {
                            updateView();
                        }
                    });
                    dialog.show(mainActivity.getSupportFragmentManager(), "dialog");

                return true;
            }
        });
    }
    private void updateView() {
        listsCode.clear();
        listsCode = resolver.getCodes(mainActivity.getToday());

        listView = mView.findViewById(R.id.listView_frag2);
        listAdapter = new ListCodeAdapter(getContext(), listsCode);
        if(listsCode != null) {
            listView.setAdapter(listAdapter);
        }

        makeHTMLFile(listsCode.get(0));
        mWeb.reload();
    }
    private void openWebView() {
        mWeb = mView.findViewById(R.id.webView_frag2);
        mWeb.setWebViewClient(new WebViewClient());
        WebSettings set = mWeb.getSettings();
        set.setJavaScriptEnabled(true);
        set.setBuiltInZoomControls(true);
        mWeb.loadUrl("file:///" + mainActivity.getFilesDir() + "graph.html");
    }
    private void makeHTMLFile(String code) {
        try{
            BufferedWriter bw = new BufferedWriter(new FileWriter(mainActivity.getFilesDir() + "graph.html",false));
            StringBuilder data = new StringBuilder();
            StringBuffer date = new StringBuffer();
            InfoMachine infoMachine;
            InfoCode infoCode;
            float[] imu = new float[6];

            List<InfoMachine> listInfoMachine = resolver.getInfoMachine(code);

            int index = 0, limit = 0;
            if(listInfoMachine != null) limit = listInfoMachine.size();
            do {
                if(listInfoMachine != null) {
                    infoMachine = listInfoMachine.get(index);
                    imu = infoMachine.getImu();
                    infoCode = resolver.getCode(infoMachine.getCode());
                    date = new StringBuffer();
                    date.append(infoCode.getDate()).append("T").append(infoMachine.getTime()).append("-0800");
                }
                 data.append("[").append("new Date('").append(date).append("'), ")
                    .append(String.valueOf(imu[0])).append(", ").append(String.valueOf(imu[1])).append(", ").append(String.valueOf(imu[2])).append(", ")
                    .append(String.valueOf(imu[3])).append(", ").append(String.valueOf(imu[4])).append(", ").append(String.valueOf(imu[5]))
                    .append("],\n");
                index++;
            } while(index < limit);

            data.delete(data.length()-2,data.length()-1);

            Log.i(TAG, String.format("%s",data));

            StringBuilder function = new StringBuilder();
            function.append("function drawChart() {\n")
                .append("var chartDiv = document.getElementById('chart_div');\n\n")
                .append("var data = new google.visualization.DataTable();\n")
                .append("data.addColumn('datetime','Time');\n")
                .append("data.addColumn('number','gx');\n")
                .append("data.addColumn('number','gy');\n")
                .append("data.addColumn('number','gz');\n")
                .append("data.addColumn('number','ax');\n")
                .append("data.addColumn('number','ay');\n")
                .append("data.addColumn('number','az');\n")
                .append("data.addRows([\n").append(data).append("]);\n\n")
                .append("var options = {\n")
                .append("chart: {title: 'IMU Data'}\n")
                .append("};\n\n")
                .append("var chart = new google.visualization.LineChart(chartDiv);\n")
                .append("chart.draw(data, options);\n").append("}\n");

            StringBuilder script = new StringBuilder();
            script.append("<script type=\"text/javascript\" src=\"https://www.gstatic.com/charts/loader.js\"></script>\n")
                .append("<script type=\"text/javascript\">\n")
                .append("google.charts.load('current', {'packages':['line', 'corechart']});\n")
                .append("google.charts.setOnLoadCallback(drawChart);\n")
                .append(function)
                .append("</script>\n");

            StringBuilder body = new StringBuilder();
            body.append("<div id=\"chart_div\"></div>\n");

            bw.write("<!DOCTYPE html>\n" + "<head>\n" + script + "</head>\n" + "<body>\n" + body + "</body>\n" + "</html>");
            bw.close();
        } catch (IOException e) {
            Toast.makeText(mainActivity.getApplicationContext(), R.string.toast_htmlfile, Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }
}
