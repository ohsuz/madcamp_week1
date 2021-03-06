package com.example.tabproject;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ClipData;
import android.content.Intent;
import android.media.Image;
import android.os.AsyncTask;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.lang.reflect.Array;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import static android.app.Activity.RESULT_OK;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link Fragment3#newInstance} factory method to
 * create an instance of this fragment.
 */
public class Fragment3 extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private static String BASE_URL = "http://ec2-13-125-208-213.ap-northeast-2.compute.amazonaws.com/";
    private static String GET =  BASE_URL + "get_wordlist.php";
    private static String DELETE =  BASE_URL + "delete_wordlist.php";

    private static String TAG = "getWordlist";
    private String mJsonString;

    RecyclerView wordlist;
    ArrayList<WordList> wList;
    WordListAdapter wAdapter;

    public Fragment3() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment Fragment3.
     */
    // TODO: Rename and change types and number of parameters
    public static Fragment3 newInstance(String param1, String param2) {
        Fragment3 fragment = new Fragment3();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        LinearLayout layout = (LinearLayout) inflater.inflate(R.layout.fragment_3, container, false);

        wordlist = layout.findViewById(R.id.wordlist); // recycler view
        wordlist.setHasFixedSize(true);
        wordlist.setLayoutManager(new LinearLayoutManager(getActivity()));

        wList = new ArrayList<>(); // arraylist
        wAdapter = new WordListAdapter(wList);
        wordlist.setAdapter(wAdapter);

        // 스와이프로 아이템 삭제를 구현하기 위한 설정
        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(simpleItemTouchCallback);
        itemTouchHelper.attachToRecyclerView(wordlist);

        GetData task = new GetData();
        task.execute(GET,"");

        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(wordlist.getContext(), layout.getOrientation());
        wordlist.addItemDecoration(dividerItemDecoration);

        wordlist.addOnItemTouchListener(new RecyclerTouchListener(getActivity(), wordlist, new RecyclerTouchListener.ClickListener() {
            @Override
            public void onClick(View view, int position) {
                WordList wl = wList.get(position);
                Intent intent = new Intent(getActivity(), Words.class);
                intent.putExtra("wordlist_id", Integer.toString(wl.getWordlist_id()));
                intent.putExtra("wordlist_lan", wl.getLan());
                startActivity(intent);
            }

            @Override
            public void onLongClick(View view, int position) {
                WordList wl = wList.get(position);

                Intent intent = new Intent(getActivity(), WordListPopup.class);
                intent.putExtra("option", 1);
                intent.putExtra("wordlist_id", Integer.toString(wl.getWordlist_id()));
                startActivity(intent);
            }
        }));

        ImageView plus = (ImageView)layout.findViewById(R.id.plus);
        plus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getActivity(), WordListPopup.class);
                intent.putExtra("option", 0);
                startActivity(intent);
            }
        });

        return layout;
    }

    private class GetData extends AsyncTask<String, Void, String>{

        ProgressDialog progressDialog;
        String errorString = null;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog = ProgressDialog.show(getActivity(),
                    null, null, true, true);
        }


        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);

            progressDialog.dismiss();
            Log.d(TAG, "response - " + result);

            // 에러가 있는 경우: 에러메세지를 보여줌
            if (result == null){
                Toast.makeText(getActivity(), errorString, Toast.LENGTH_SHORT).show();
            }
            // 에러가 없는 경우: JSON을 파싱하여 화면에 보여주는 showResult 메소드를 호출함
            else {
                mJsonString = result;
                showResult();
            }
    }


        @Override
        protected String doInBackground(String... params) {

            String serverURL = params[0];
            String postParameters = params[1];


            try {

                java.net.URL url = new URL(serverURL);
                HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();


                httpURLConnection.setReadTimeout(5000);
                httpURLConnection.setConnectTimeout(5000);
                httpURLConnection.setRequestMethod("POST");
                httpURLConnection.setDoInput(true);
                httpURLConnection.connect();


                OutputStream outputStream = httpURLConnection.getOutputStream();
                outputStream.write(postParameters.getBytes("UTF-8"));
                outputStream.flush();
                outputStream.close();


                int responseStatusCode = httpURLConnection.getResponseCode();
                Log.d(TAG, "response code - " + responseStatusCode);

                InputStream inputStream;
                if(responseStatusCode == HttpURLConnection.HTTP_OK) {
                    inputStream = httpURLConnection.getInputStream();
                }
                else{
                    inputStream = httpURLConnection.getErrorStream();
                }


                InputStreamReader inputStreamReader = new InputStreamReader(inputStream, "UTF-8");
                BufferedReader bufferedReader = new BufferedReader(inputStreamReader);

                StringBuilder sb = new StringBuilder();
                String line;

                while((line = bufferedReader.readLine()) != null){
                    sb.append(line);
                }

                bufferedReader.close();

                return sb.toString().trim();


            } catch (Exception e) {

                Log.d(TAG, "GetData : Error ", e);
                errorString = e.toString();

                return null;
            }

        }
    }

    private class DeleteData extends AsyncTask<String, Void, String> {

        ProgressDialog progressDialog;
        String errorString = null;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog = ProgressDialog.show(getActivity(),
                    null, "Delete", true, true);
        }


        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);

            progressDialog.dismiss();
            Log.d(TAG, "response - " + result);

            // 에러가 있는 경우: 에러메세지를 보여줌
            if (result == null){
                Toast.makeText(getActivity(), errorString, Toast.LENGTH_SHORT).show();
            }
            // 에러가 없는 경우: Intent로 다시 탭 3로 이동
            else {
                // 프래그먼트 3번으로 바로 이동
                Intent intent = new Intent(getActivity(), MainActivity.class);
                intent.putExtra("tab3", 2);
                startActivity(intent);
            }
        }


        @Override
        protected String doInBackground(String... params) {

            String searchKeyword = params[0];
            String parameters = "wordlist_id=" + searchKeyword;

            try {

                java.net.URL url = new URL(DELETE);
                HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();


                httpURLConnection.setReadTimeout(5000);
                httpURLConnection.setConnectTimeout(5000);
                httpURLConnection.setRequestMethod("POST");
                httpURLConnection.setDoInput(true);
                httpURLConnection.connect();


                OutputStream outputStream = httpURLConnection.getOutputStream();
                outputStream.write(parameters.getBytes("UTF-8"));
                outputStream.flush();
                outputStream.close();


                int responseStatusCode = httpURLConnection.getResponseCode();
                Log.d(TAG, "response code - " + responseStatusCode);

                InputStream inputStream;
                if(responseStatusCode == HttpURLConnection.HTTP_OK) {
                    inputStream = httpURLConnection.getInputStream();
                }
                else{
                    inputStream = httpURLConnection.getErrorStream();
                }


                InputStreamReader inputStreamReader = new InputStreamReader(inputStream, "UTF-8");
                BufferedReader bufferedReader = new BufferedReader(inputStreamReader);

                StringBuilder sb = new StringBuilder();
                String line;

                while((line = bufferedReader.readLine()) != null){
                    sb.append(line);
                }


                bufferedReader.close();


                return sb.toString().trim();


            } catch (Exception e) {

                Log.d(TAG, "DeleteData: Error ", e);
                errorString = e.toString();

                return null;
            }

        }
    }



    private void showResult(){

        String TAG_JSON="firstproject";
        String TAG_WORDLIST = "wordlist";
        String TAG_WORDLIST_ID = "wordlist_id";
        String TAG_WORDLIST_LAN = "wordlist_lan";

        try {
            JSONObject jsonObject = new JSONObject(mJsonString);
            JSONArray jsonArray = jsonObject.getJSONArray(TAG_JSON);

            for(int i=0;i<jsonArray.length();i++){

                JSONObject item = jsonArray.getJSONObject(i);

                String wordlist = item.getString(TAG_WORDLIST);
                int wordlist_id = item.getInt(TAG_WORDLIST_ID);
                String wordlist_lan = item.getString(TAG_WORDLIST_LAN);

                WordList wordList = new WordList(wordlist, wordlist_lan, wordlist_id);
                wList.add(wordList);
                wAdapter.notifyDataSetChanged();
            }



        } catch (JSONException e) {

            Log.d(TAG, "showResult : ", e);
        }

    }

    // 삭제할 수 있는 방향을 더 추가할 수도 있음 ex) ItemTouchHelper.RIGHT | ItemTouchHelper.RIGHT
    ItemTouchHelper.SimpleCallback simpleItemTouchCallback = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT ) {
        @Override
        public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
            return true;
        }

        @Override
        public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
            final int position = viewHolder.getAdapterPosition();
            WordList wl = wList.get(position);
            DeleteData delete = new DeleteData();
            delete.execute(Integer.toString(wl.getWordlist_id()));
        }
    };
}