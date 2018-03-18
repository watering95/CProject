package com.example.water.cproject;

import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
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
    private MainActivity mainActivity;
    private WebView mWeb;
    private static final String TAG = "InvestRecord";
    private DBResolver resolver;
    ArrayList<String> listsCode;

    public Fragment2() {
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mainActivity = (MainActivity) getActivity();
        assert mainActivity != null;
        resolver = mainActivity.resolver;

        mainActivity.setFrag2Callback(new MainActivity.Frag2Callback() {

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
        ListView listView = mView.findViewById(R.id.listView_frag2);
        ListCodeAdapter listAdapter = new ListCodeAdapter(mView.getContext(), listsCode);
        if(listsCode.size() != 0) {
            listView.setAdapter(listAdapter);
        }
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                makeHTMLFile(listsCode.get(position).toString());
            }
        });
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
            BufferedWriter bw = new BufferedWriter(new FileWriter(mainActivity.getFilesDir() + "graph_total.html",false));
            StringBuilder data = new StringBuilder();

            List<Info_Machine>listInfoMachine = resolver.getInfoMachine(code);
            int index = 0, limit = listInfoMachine.size();
            do {
                // 특정일의 합계와 평가액 계산
                float[] imu = listInfoMachine.get(index).getImu();
                data.append("[").append(", ")
                        .append(String.valueOf(imu[0])).append(", ").append(String.valueOf(imu[1])).append(", ").append(String.valueOf(imu[2])).append(", ")
                        .append(String.valueOf(imu[3])).append(", ").append(String.valueOf(imu[4])).append(", ").append(String.valueOf(imu[5]))
                        .append("],\n");
                // 초기화
                index++;
                // 날짜 변경
            } while(index < limit);

            data.delete(data.length()-2,data.length()-1);

            Log.i(TAG, String.format("%s",data));

            String function = "function drawChart() {\n"
                    + "var chartDiv = document.getElementById('chart_div');\n\n"

                    + "var data = new google.visualization.DataTable();\n"
                    + "data.addColumn('number','gx');\n"
                    + "data.addColumn('number','gy');\n"
                    + "data.addColumn('number','gz');\n"
                    + "data.addColumn('number','ax');\n"
                    + "data.addColumn('number','ay');\n"
                    + "data.addColumn('number','az');\n"
                    + "data.addRows([\n" + data + "]);\n\n"

                    + "var options = {"
                    + "};\n\n"

                    + "var chart = new google.visualization.LineChart(chartDiv);\n"
                    + "chart.draw(data, options);\n"

                    + "}\n";

            String script = "<script type=\"text/javascript\" src=\"https://www.gstatic.com/charts/loader.js\"></script>\n"
                    + "<script type=\"text/javascript\">\n"
                    + "google.charts.load('current', {'packages':['line', 'corechart']});\n"
                    + "google.charts.setOnLoadCallback(drawChart);\n"
                    + function
                    + "</script>\n";

            String body = "<div id=\"chart_div\"></div>\n";

            String html = "<!DOCTYPE html>\n" + "<head>\n" + script + "</head>\n" + "<body>\n" + body + "</body>\n" + "</html>";

            bw.write(html);
            bw.close();
        } catch (IOException e) {
            Toast.makeText(mainActivity.getApplicationContext(), R.string.toast_htmlfile, Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }
}
