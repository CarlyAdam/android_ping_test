package com.carlyadam.pingtest;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import dmax.dialog.SpotsDialog;

public class MainActivity extends AppCompatActivity {

    private TextView textView_result;
    private Button button_ping;
    private double ping;
    SpotsDialog dialog;

    //Address to make ping Test
    private String address = "8.8.8.8";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        dialog = new SpotsDialog(MainActivity.this, R.style.CustomDialog);
        textView_result = (TextView) findViewById(R.id.textView_result);
        button_ping = (Button) findViewById(R.id.button_ping);
        button_ping.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new MyPingThread().execute();
            }
        });
    }

    //Class to execute ping in background
    private class MyPingThread extends AsyncTask<Void, Void, String> {
        String avg = "";

        protected void onPreExecute() {
            dialog.show(); //show dialog before start

        }

        protected String doInBackground(Void... param) {
            List<String> comando = comando();
            try {
                avg = ejecuta(comando);
            } catch (Throwable throwable) {
                throwable.printStackTrace();
            }
            return avg;
        }

        protected void onProgressUpdate(Void... param) {
            //
        }

        protected void onPostExecute(String param) {
            //param is the avg ()
            updateMeter(param);
            dialog.dismiss();
        }
    }

    //Step [1] Formmating Ping command
    public List<String> comando() {
        List<String> s = new ArrayList<String>();
        s.add("ping");
        s.add("-c");
        s.add(String.valueOf(5));
        s.add("-s");
        s.add("31515");
        s.add(address);
        return s;
    }

    //Step [2] executing ping
    public String ejecuta(List<String> comando)
            throws Throwable {

        String scala = " B/s";
        String s = null;
        String avg = "";
        double t = 0;

        ProcessBuilder pb = new ProcessBuilder(comando);

        Process process = pb.start();

        BufferedReader stdInput = new BufferedReader(new InputStreamReader(process.getInputStream()));
        BufferedReader stdError = new BufferedReader(new InputStreamReader(process.getErrorStream()));

        int cursor = 0;
        avg = "";

        while ((s = stdInput.readLine()) != null) {
            System.out.println(s);
            if (s.contains("icmp_seq"))
                cursor++;
            System.out.println(cursor);

            if (s.contains("rtt min/avg/max/mdev =")) {
                avg = (s.substring(s.indexOf("=") + 1));
                System.out.println(avg);
            }
        }//end while

        while ((s = stdError.readLine()) != null) {
            System.out.println(s);
        }

        return avg;
    }

    //Step [3] Updating TexView
    public void updateMeter(String avg) {
        String scala = " B/s";
        String s = null;
        double t = 0;
        if (avg.length() > 0) {
            avg = avg.substring(avg.indexOf("/") + 1);
            avg = avg.substring(0, avg.indexOf("/"));
            ping = Double.valueOf(avg);
            //convert in  kb
            t = 31.5 / Double.valueOf(avg) * 1000;
            t = Redondear(t, 1);
            textView_result.setText(avg + " Ms");

        }
    }

    public static double Redondear(double numero, int digitos) {
        int cifras = (int) Math.pow(10, digitos);
        return Math.rint(numero * cifras) / cifras;
    }
}
