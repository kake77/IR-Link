package com.hexane.katok.homecontroller;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.widget.SwipeRefreshLayout;
import android.text.Html;
import android.text.Spanned;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.webkit.JavascriptInterface;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.crashlytics.android.Crashlytics;
import io.fabric.sdk.android.Fabric;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.URL;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener{

    Globals globals;

    LinearLayout LL_home, LL_tv,LL_aircon,LL_light;
    NavigationView navigationView;
    TranslateAnimation translateAnimation;
    SwipeRefreshLayout swipeRefreshLayout;

    TextView T_temp,T_wind,T_weather_name,T_temp_home,T_humidity_home,T_temp_feeling;
    ImageView I_wind_degree,I_weather_icon;

    Handler roothandler;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Fabric.with(this, new Crashlytics());
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        roothandler =new Handler();

        globals=(Globals) this.getApplication();

        LL_home=findViewById(R.id.home);
        LL_tv=findViewById(R.id.tv);
        LL_aircon=findViewById(R.id.aircon);
        LL_light=findViewById(R.id.light);

        globals.ip=getIp();

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        navigationView.getMenu().getItem(0).setChecked(true);

        T_temp=findViewById(R.id.temp);
        T_wind=findViewById(R.id.wind_speed);
        //I_wind_degree=findViewById(R.id.wind_degree);
        I_weather_icon=findViewById(R.id.weather_icon);
        T_weather_name=findViewById(R.id.weather_name);
        T_temp_home=findViewById(R.id.temp_home);
        T_humidity_home=findViewById(R.id.humidity_home);
        T_temp_feeling=findViewById(R.id.temp_feeling);

        setSwipeView();
        setWeather();
        setButtons();
        //getArduinoValues();


        translateAnimation=new TranslateAnimation(Animation.ABSOLUTE,0,Animation.ABSOLUTE,0,Animation.RELATIVE_TO_SELF,1,Animation.ABSOLUTE,0);
        translateAnimation.setDuration(500);
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    //@Override
    //public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
    //    getMenuInflater().inflate(R.menu.main, menu);
    //    return true;
    //}

    //@Override
    //public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
    //    int id = item.getItemId();

        //noinspection SimplifiableIfStatement
    //    if (id == R.id.action_settings) {
     //       return true;
    //    }

     //   return super.onOptionsItemSelected(item);
    //}

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_home) {
            switchView(0);
        } else if (id == R.id.nav_tv) {
            switchView(1);
        } else if (id == R.id.nav_aircon) {
            switchView(2);
        } else if (id == R.id.nav_light) {
            switchView(3);
        } else if (id == R.id.nav_settings) {
            Intent intent=new Intent(this,Settings.class);
            startActivity(intent);

        } else if (id == R.id.nav_share) {

        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }


    public void switchView (int i){
        if (i==0){
            LL_home.setVisibility(View.VISIBLE);
            LL_home.startAnimation(translateAnimation);
            LL_tv.setVisibility(View.GONE);
            LL_aircon.setVisibility(View.GONE);
            LL_light.setVisibility(View.GONE);
        }if (i==1){
            LL_home.setVisibility(View.GONE);
            LL_tv.setVisibility(View.VISIBLE);
            LL_tv.startAnimation(translateAnimation);
            LL_aircon.setVisibility(View.GONE);
            LL_light.setVisibility(View.GONE);
        }if (i==2){
            LL_home.setVisibility(View.GONE);
            LL_tv.setVisibility(View.GONE);
            LL_aircon.setVisibility(View.VISIBLE);
            LL_aircon.startAnimation(translateAnimation);
            LL_light.setVisibility(View.GONE);
        }if (i==3){
            LL_home.setVisibility(View.GONE);
            LL_tv.setVisibility(View.GONE);
            LL_aircon.setVisibility(View.GONE);
            LL_light.setVisibility(View.VISIBLE);
            LL_light.startAnimation(translateAnimation);
        }
    }

    public void setWeather(){

        swipeRefreshLayout.setRefreshing(true);
        Thread t=new Thread(new Runnable() {
            @Override
            public void run() {
                try {

                }catch (final Exception e){
                    roothandler.post(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(getApplicationContext(),e.toString(),Toast.LENGTH_LONG).show();
                        }
                    });
                }
                String sss="";
                try {
                    String requestURL = "http://api.openweathermap.org/data/2.5/weather?id=1856057&units=metric&cnt=1&appid=6bf2eed41c97a98bd540b60f48d9d6bc";
                    URL url = new URL(requestURL);
                    InputStream is = url.openConnection().getInputStream();

                    // JSON形式で結果が返るためパースのためにStringに変換する
                    BufferedReader reader = new BufferedReader(new InputStreamReader(is, "UTF-8"));
                    StringBuilder sb = new StringBuilder();
                    String line;
                    while (null != (line = reader.readLine())) {
                        sb.append(line);
                    }
                    String data = sb.toString();

                    JSONObject rootObj = new JSONObject(data);
                    final String weather =rootObj.getJSONArray("weather").getJSONObject(0).getString("main");
                    final Double temp=rootObj.getJSONObject("main").getDouble("temp");
                    final Double wind_speed=rootObj.getJSONObject("wind").getDouble("speed");
                    //final int wind_deg=rootObj.getJSONObject("wind").getInt("deg");
                    final int humidity=rootObj.getJSONObject("main").getInt("humidity");
                    //final int clouds=rootObj.getJSONObject("clouds").getInt("all");

                    roothandler.post(new Runnable() {
                        @Override
                        public void run() {
                            T_temp_home.setText(getround1(temp)+"℃");
                            T_humidity_home.setText(humidity+"%");
                            T_temp.setText(getround1(temp)+"℃");
                            T_wind.setText(getround1(wind_speed)+"m/s");
                            T_temp_feeling.setText(getTempFeeling(temp,humidity,wind_speed)+"℃");
                            //I_wind_degree.setRotation(wind_deg+180);
                            if (weather.equals("Thunderstorm")){
                                T_weather_name.setText("雷雨");
                                I_weather_icon.setImageResource(R.drawable.ic_rain);
                            }if (weather.equals("Drizzle")){
                                T_weather_name.setText("霧雨");
                                I_weather_icon.setImageResource(R.drawable.ic_rain);
                            }if (weather.equals("Rain")){
                                T_weather_name.setText("雨");
                                I_weather_icon.setImageResource(R.drawable.ic_rain);
                            }if (weather.equals("Snow")){
                                T_weather_name.setText("雪");
                                I_weather_icon.setImageResource(R.drawable.ic_rain);
                            }if (weather.equals("Atmosphere")){
                                T_weather_name.setText("ミスト");
                                I_weather_icon.setImageResource(R.drawable.ic_rain);
                            }if (weather.equals("Clear")){
                                T_weather_name.setText("晴れ");
                                I_weather_icon.setImageResource(R.drawable.ic_wb_sunny_black_24dp);
                            }if (weather.equals("Clouds")){
                                T_weather_name.setText("曇り");
                                I_weather_icon.setImageResource(R.drawable.ic_cloud_black_24dp);
                            }
                            //Toast.makeText(getApplicationContext(),"受け取った！",Toast.LENGTH_LONG).show();
                            swipeRefreshLayout.setRefreshing(false);
                        }
                    });
                }catch (final Exception e){
                    Log.e("a",e.toString());
                    Log.e("a",sss);
                    e.printStackTrace();
                    roothandler.post(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(getApplicationContext(),e.toString(),Toast.LENGTH_LONG).show();
                        }
                    });
                }
            }
        });
        t.start();
        //getArduinoValues();
    }
    public void setButtons(){
        LinearLayout home_tv=findViewById(R.id.home_tv);
        LinearLayout home_aircon=findViewById(R.id.home_aircon);
        LinearLayout home_light=findViewById(R.id.home_light);
        Button switch_input=findViewById(R.id.switch_input);
        Button tv_io=findViewById(R.id.tv_io);
        Button tv_1=findViewById(R.id.TV_1);
        Button tv_2=findViewById(R.id.TV_2);
        Button tv_3=findViewById(R.id.TV_3);
        Button tv_4=findViewById(R.id.TV_4);
        Button tv_5=findViewById(R.id.TV_5);
        Button tv_6=findViewById(R.id.TV_6);
        Button tv_7=findViewById(R.id.TV_7);
        Button tv_8=findViewById(R.id.TV_8);
        Button tv_9=findViewById(R.id.TV_9);
        Button tv_10=findViewById(R.id.TV_10);
        Button tv_11=findViewById(R.id.TV_11);
        Button tv_12=findViewById(R.id.TV_12);
        Button aircon_io=findViewById(R.id.aircon_io);

        home_tv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switchView(1);
                navigationView.getMenu().getItem(1).setChecked(true);
            }
        });
        home_aircon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switchView(2);
                navigationView.getMenu().getItem(2).setChecked(true);
            }
        });
        home_light.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switchView(3);
                navigationView.getMenu().getItem(3).setChecked(true);
            }
        });
        switch_input.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                globals.ip="192.168.1.9";
            }
        });
        tv_io.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendSig("tv",0);
            }
        });
        tv_1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendSig("tv",1);
            }
        });
        tv_2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendSig("tv",2);
            }
        });
        tv_3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendSig("tv",3);
            }
        });
        tv_4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendSig("tv",4);
            }
        });
        tv_5.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendSig("tv",5);
            }
        });
        tv_6.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendSig("tv",6);
            }
        });
        tv_7.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendSig("tv",7);
            }
        });
        tv_8.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendSig("tv",8);
            }
        });
        tv_9.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendSig("tv",9);
            }
        });
        tv_10.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendSig("tv",10);
            }
        });
        tv_11.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendSig("tv",11);
            }
        });
        tv_12.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendSig("tv",12);
            }
        });
        aircon_io.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //getValues();
            }
        });

    }
    public void sendSig(final String s,final int id){
        final Handler handler=new Handler();
        final Thread thread=new Thread(new Runnable() {
            @Override
            public void run() {
                if (globals.ip!=null) {
                    final String urlSt = "http://"+globals.ip + "/" + s + "/" + id;
                    try {
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                WebView webView=new WebView(getApplicationContext());
                                webView.loadUrl(urlSt);
                            }
                        });
                    } catch (final Exception e) {
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(getApplicationContext(), urlSt+"に接続できませんでした\n"+e.toString(), Toast.LENGTH_LONG).show();
                            }
                        });
                    }
                }else {
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(getApplicationContext(),"ipアドレスを設定してください",Toast.LENGTH_LONG).show();
                        }
                    });
                }
            }
        });
        thread.start();
    }
    public String getIp(){
        try {
            InputStream inputStream=openFileInput("settings.txt");
            BufferedReader bufferedReader=new BufferedReader(new InputStreamReader(inputStream,"UTF-8"));
            return bufferedReader.readLine();
        }catch (Exception e){
            try {
                OutputStream outputStream=openFileOutput("settings.txt",MODE_PRIVATE);
                PrintWriter printWriter=new PrintWriter(new OutputStreamWriter(outputStream,"UTF-8"));
                printWriter.append("192.168.1.1");
                printWriter.close();
                return "192.168.1.1";
            }catch (Exception e2){
                return "f";
            }
        }
    }
    public void getArduinoValues(){


        WebView web=new WebView(getApplicationContext());
            web.getSettings().setJavaScriptEnabled(true);
            web.getSettings().setBuiltInZoomControls(true);
            web.setWebViewClient(new WebViewClient() {
                @Override
                public void onPageFinished(WebView view, String url) {    // STEP3
                    view.loadUrl("javascript:window.MyWebViewActivity.viewSource(document.documentElement.outerHTML);");
                }
            });
            web.addJavascriptInterface(this, "MyWebViewActivity");    // STEP1
            web.loadUrl("http://"+globals.ip + "/data/0");
        }

        @JavascriptInterface    // STEP2
        public void viewSource(final String src) {
            roothandler.post(new Runnable() {
                @Override
                public void run() {
                    try{
                        String str=src.substring(src.indexOf(",")+1,src.lastIndexOf(","));
                        TextView T_temp_feeling_arduino=findViewById(R.id.temp_feeling_arduino);
                        TextView T_temp_arduino=findViewById(R.id.temp_arduino);
                        TextView T_humidity_arduino=findViewById(R.id.humidity_arduino);
                        String temp=str.substring(str.indexOf(",")+1);
                        String humidity=str.substring(0,str.indexOf(","));
                        T_temp_arduino.setText(temp+"℃");
                        T_humidity_arduino.setText(humidity+"%");
                        T_temp_feeling_arduino.setText(getTempFeeling(Double.parseDouble(temp), Double.parseDouble(humidity),0)+"℃");
                    }catch (Exception e){
                        Toast.makeText(getApplicationContext(),e.toString(),Toast.LENGTH_LONG).show();
                    }


                }
            });
    }

    public void setSwipeView(){
        swipeRefreshLayout=findViewById(R.id.swipeRefresh);
        swipeRefreshLayout.setOnRefreshListener(onRefreshListener);
        swipeRefreshLayout.setColorSchemeColors(getColor(R.color.red),getColor(R.color.yellow),getColor(R.color.green));
    }

    private SwipeRefreshLayout.OnRefreshListener onRefreshListener=new SwipeRefreshLayout.OnRefreshListener() {
        @Override
        public void onRefresh() {
            setWeather();
        }
    };
    public double getround1(double d){
        return ((Math.floor((d*10)))/10);
    }
    public double getTempFeeling(double temp,double humidity,double windSpeed){
        double A=1.76+(1.4*(Math.pow(windSpeed,0.75)));
        double Tm1=37;
        double Tm2=(37-temp)/(0.68-(0.0014*humidity)+(1/A));
        double Tm3=(((0.29*temp)*(100-humidity))*0.01);
        double Tm=(Tm1-Tm2-Tm3);
        return getround1(Tm);
    }
}