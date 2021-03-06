package com.example.sejongbooks.ServerConnect;

import android.content.ContentValues;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;

import com.example.sejongbooks.Helper.Constant;
import com.example.sejongbooks.Listener.AsyncCallback;
import com.example.sejongbooks.R;
import com.example.sejongbooks.Singleton.BookManager;
import com.example.sejongbooks.VO.BookVO;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Random;

public class BookTask extends AsyncTask<Void, Void, Void> {
    private int taskType;
    private AsyncCallback m_callback;
    private Exception m_exception;
    private String m_url;
    private ContentValues m_values;

    public BookTask(int taskType, String url, ContentValues values, AsyncCallback callback) {
        this.m_callback = callback;
        this.m_url = url;
        this.m_values = values;
        this.taskType = taskType;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        //progress bar를 보여주는 등등의 행위
    }

    @Override
    protected Void doInBackground(Void... params) {
        String result;

        try {
            PostHttpURLConnection requestHttpURLConnection = new PostHttpURLConnection();
            result = requestHttpURLConnection.request(m_url, m_values);

            if (taskType == Constant.GET_NEW) {
                initBookFromJson(new JSONObject(result).getString("bookList"));
            } else if (taskType == Constant.UPDATE_ITEMS) {
                updateBookFromJson(result);
            }
        } catch(Exception e) {
            this.m_exception = e;
            return null;
        }

        return null; // 결과가 여기에 담깁니다. 아래 onPostExecute()의 파라미터로 전달됩니다.
    }

    @Override
    protected void onPostExecute(Void aVoid) {
        super.onPostExecute(aVoid);
        if (m_callback != null && m_exception == null) {
            m_callback.onSuccess(true);
        } else {
            m_callback.onFailure(m_exception);
        }
    }

    private void updateBookFromJson(String result_json_str) {
        try {
            JSONArray jsonArray = new JSONArray(new JSONObject(result_json_str).getString("bookList"));
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject jsonObj = jsonArray.getJSONObject(i);

                int bookID = jsonObj.getInt("bookID");
                double bookStar = jsonObj.getDouble("bookStar");
                int bookCount = jsonObj.getInt("bookCount");

                for (BookVO item : BookManager.getInstance().getItems()) {
                    if (item.getID() == bookID) {
                        item.setGrade((float)bookStar);
                        item.setCount(bookCount);
                        item.setRead(false);
                    }
                }
            }

            JSONArray jsonArrayRead = new JSONArray(new JSONObject(result_json_str).getString("readList"));
            for (int i = 0; i < jsonArrayRead.length(); i++) {
                JSONObject jsonObj = jsonArrayRead.getJSONObject(i);
                int bookID = jsonObj.getInt("bookID");

                for (BookVO item : BookManager.getInstance().getItems()) {
                    if (item.getID() == bookID) {
                        item.setRead(true);
                    }
                }
            }
            } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void initBookFromJson(String bookList_json_str) {
        try {
            JSONArray jsonArray = new JSONArray(bookList_json_str);
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject jsonObj = jsonArray.getJSONObject(i);

                int bookID = jsonObj.getInt("bookID");
                String bookName = jsonObj.getString("bookName");
                String bookType = jsonObj.getString("bookType");
                String bookInfo = jsonObj.getString("bookInfo");
                int bookPage = jsonObj.getInt("bookPage");
                int bookDate = jsonObj.getInt("bookDate");
                int bookCount = jsonObj.getInt("bookCount");
                String bookAuthor = jsonObj.getString("bookAuthor");
                String bookPublisher = jsonObj.getString("bookPublisher");
                double bookStar = jsonObj.getDouble("bookStar");
                int problem = jsonObj.getInt("problem");

                BookVO newItem = new BookVO();
                newItem.setBook(bookID, bookType, bookName, bookAuthor, bookPublisher, bookDate, bookPage, bookInfo, bookCount, (float)bookStar, problem);

                String url_img = Constant.URL + "/cover/book_" + (i + 1) + ".jpg";
                newItem.setImage(BookManager.getInstance().getBookBitmapFromURL(url_img,"book" + (i + 1)));
                Log.d("mmee:bookTask", "get book resource " + (i + 1));

                newItem.setRead(false);

                // 임시 별점
                // newItem.setGrade(new Random().nextFloat() * 5);

                BookManager.getInstance().getItems().add(newItem);

                int loadPercent = (int)((i + 1) / (float)jsonArray.length() * 100.0f);
                BookManager.getInstance().setLoadPercent(loadPercent);
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }


}