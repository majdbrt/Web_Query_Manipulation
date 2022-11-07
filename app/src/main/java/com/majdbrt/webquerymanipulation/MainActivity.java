package com.majdbrt.webquerymanipulation;

import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ProgressBar;

import com.majdbrt.webquerymanipulation.databinding.ActivityMainBinding;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;


public class MainActivity extends AppCompatActivity {
    String data2 = "";
    ArrayList<Integer> idList ;
    ArrayList<Integer> listidList;
    ArrayList<String> nameList;

    ArrayList<Integer> distinctIDList;
    List<List<Integer>> distinctNames;

    ArrayAdapter<String> listAdapter;
    ActivityMainBinding binding;

    ScheduledExecutorService backgroundExecutor = Executors.newSingleThreadScheduledExecutor();


    @Override
    protected void onCreate(Bundle savedInstanceState) {


        super.onCreate(savedInstanceState);
        //setContentView(R.layout.activity_main);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());


        initializeLists();
        new FetchData().start();

    }// onCreate

    private void initializeLists() {
        idList = new ArrayList<>();
        listidList = new ArrayList<>();
        nameList = new ArrayList<>();
        distinctIDList = new ArrayList<>();

        listAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1);

        binding.dynamicList.setAdapter(listAdapter);
    }// initializeLists

    class FetchData extends Thread{
        String data = "";




        @Override
        public void run(){

            try{
                URL url = new URL("https://fetch-hiring.s3.amazonaws.com/hiring.json");
                HttpURLConnection request = (HttpURLConnection) url.openConnection();
                InputStream inputStream = request.getInputStream();
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
                String line;

                while( (line = bufferedReader.readLine()) != null){
                    data = data + line;
                }// while

                if(!data.isEmpty()){
                    data2 = data;
                    JSONArray jsonArray = new JSONArray(data);
                    for (int i = 0; i < jsonArray.length(); i++) {
                        JSONObject jsonobject = jsonArray.getJSONObject(i);

                        Integer id = jsonobject.getInt( "id");
                        Integer listId = jsonobject.getInt("listId");
                        String name = jsonobject.getString("name");
                        idList.add(id);
                        listidList.add(listId);
                        nameList.add(name);

                    }// for

                }// if

                for(int i = 0; i < listidList.size(); i++){

                    if(!distinctIDList.contains(listidList.get(i))){
                        distinctIDList.add(listidList.get(i));
                    }// if
                }// for

                // sort listID items
                Collections.sort(distinctIDList);

                // create an Integer array for each listID items to hold the integer part of the name
                // this will help us sort the names later.
                distinctNames = new ArrayList<List<Integer>>(distinctIDList.size());

                for(int i = 0; i < distinctIDList.size(); i++){
                    int target = distinctIDList.get(i);
                    distinctNames.add(new ArrayList<Integer>());
                    for(int j = 0; j < listidList.size(); j++){
                        if(listidList.get(j) == target &&
                                (       // make sure the name is not null or empty
                                        !nameList.get(j).isEmpty() &&
                                        nameList.get(j) != "null"
                                )
                        ){
                          //  nameList.get(j).substring(nameList.get(j).lastIndexOf(" ")+1)
                            int itemNumber = Integer.parseInt(nameList.get(j).substring(nameList.get(j).lastIndexOf(" ")+1));
                            if(!distinctNames.get(i).contains(itemNumber)){
                                distinctNames.get(i).add(itemNumber);

                            }// if
                        }// if
                    }// for
                }// for

                // sort the name lists and create the final string to be added to list adapter
                for(int i = 0; i < distinctNames.size(); i++){
                    Collections.sort(distinctNames.get(i));
                    int finalI = i;

                    for(int j = 0; j < distinctNames.get(i).size(); j++){
                        int finalJ = j;
                        runOnUiThread(new Runnable() {

                            @Override
                            public void run() {

                                // Stuff that updates the UI
                                listAdapter.add(String.valueOf(distinctIDList.get(finalI)) + "                                                  Item "+ String.valueOf(distinctNames.get(finalI).get(finalJ)) );

                            }// run
                        });
                    }// for
                }// for

            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (JSONException e) {
                e.printStackTrace();
            }// catch

            // Execute a task in the main thread
            backgroundExecutor.execute(new Runnable() {
                @Override
                public void run() {
                    // Your code logic goes here.
                    // update adapter
                    listAdapter.notifyDataSetChanged();
                }// run
            });

        }// run
    }// FetchData
}// MainActivity