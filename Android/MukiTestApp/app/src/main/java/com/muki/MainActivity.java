package com.muki;

import android.app.ProgressDialog;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.muki.core.MukiCupApi;
import com.muki.core.MukiCupCallback;
import com.muki.core.model.Action;
import com.muki.core.model.DeviceInfo;
import com.muki.core.model.ErrorCode;
import com.muki.core.model.ImageProperties;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    private EditText mSerialNumberEdit;
    private TextView mCupIdText;
    private TextView mDeviceInfoText;
    private SeekBar mContrastSeekBar;
    private ProgressDialog mProgressDialog;
    private EditText mTwitterUsernameEdit;
    private TextView mTwitterUsernameText;
    // DEBUG
    private TextView mDebugLogText;


    private String mCupId;
    private String mTwitterUsername;
    private MukiCupApi mMukiCupApi;


    public enum RSSXMLTag {
        TITLE, CREATOR, IGNORETAG, DESCRIPTION;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mProgressDialog = new ProgressDialog(this);
        mProgressDialog.setCancelable(false);
        mProgressDialog.setMessage("Loading. Please wait...");
        mProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);

        mMukiCupApi = new MukiCupApi(getApplicationContext(), new MukiCupCallback() {
            @Override
            public void onCupConnected() {
                showToast("Cup connected");
            }

            @Override
            public void onCupDisconnected() {
                showToast("Cup disconnected");
            }

            @Override
            public void onDeviceInfo(DeviceInfo deviceInfo) {
                hideProgress();
                mDeviceInfoText.setText(deviceInfo.toString());
            }

            @Override
            public void onImageCleared() {
                showToast("Image cleared");
            }

            @Override
            public void onImageSent() {
                showToast("Image sent");
            }

            @Override
            public void onError(Action action, ErrorCode errorCode) {
                showToast("Error:" + errorCode + " on action:" + action);
            }
        });

        mSerialNumberEdit = (EditText) findViewById(R.id.serailNumberText);
        mCupIdText = (TextView) findViewById(R.id.cupIdText);
        mDeviceInfoText = (TextView) findViewById(R.id.deviceInfoText);
        mTwitterUsernameEdit = (EditText) findViewById(R.id.twitterUsernameEdit);
        mTwitterUsernameText = (TextView) findViewById(R.id.twitterUsernameText);
        mDebugLogText = (TextView) findViewById(R.id.debugLogText);

        /*reset(null);*/
    }

    /*private TwitterFeed getTweets () {
        final String twitterUsername = mTwitterUsernameEdit.getText().toString();
        final TwitterFeed tf = new TwitterFeed();
        // DEBUG
        tf.username = "Devfeed";
        tf.tweets.add(new Tweet());
        tf.tweets.get(0).postCreator = "Devs";
        tf.tweets.get(0).postTitle = "Error";
        tf.tweets.get(0).postDescription = "Something went wrong";

        try {
            URL url = new URL("https://twitrss.me/twitter_user_to_rss/?user=" + twitterUsername);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setReadTimeout(10*1000);
            connection.setConnectTimeout(10*1000);
            connection.setRequestMethod("GET");
            connection.setDoInput(true);
            connection.connect();
            int responseCode = connection.getResponseCode();

            // Read message
            if (responseCode % 100 == 2) {
                InputStream is = connection.getInputStream();
                final int bufferSize = 1024;
                byte[] buffer = new byte[1024];
                ByteArrayOutputStream os = new ByteArrayOutputStream();
                while (true) {
                    int count = is.read(buffer, 0, bufferSize);
                    if (count == -1) {
                        break;
                    }

                    os.write(buffer);
                }
                os.close();
                String xmlResult = new String(os.toByteArray(), "UTF-8");


                // Parse XML
                XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
                factory.setNamespaceAware(true);
                XmlPullParser xpp = factory.newPullParser();
                xpp.setInput(is, null);

                int eventType = xpp.getEventType();
                ArrayList<Tweet> tweets = new ArrayList<>();
                Tweet tweet = null;
                RSSXMLTag currentTag = null;


                while (eventType != XmlPullParser.END_DOCUMENT) {
                    if (eventType == XmlPullParser.START_DOCUMENT) {
                        // Parsing start
                    } else if (eventType == XmlPullParser.START_TAG) {
                        // Find a starting tag
                        if (xpp.getName().equals("item")) {
                            tweet = new Tweet();
                            currentTag = RSSXMLTag.IGNORETAG;
                        }
                        else if (xpp.getName().equals("title")) {
                            currentTag = RSSXMLTag.TITLE;
                        }
                        else if (xpp.getName().equals("dc:creator")) {
                            currentTag = RSSXMLTag.CREATOR;
                        }
                        else if (xpp.getName().equals("description")) {
                            currentTag = RSSXMLTag.DESCRIPTION;
                        }
                    } else if (eventType == XmlPullParser.END_TAG) {
                        // Find a ending tag
                        if (xpp.getName().equals("item")) {
                            // format the data here, otherwise format data in
                            // Adapter
                            tweets.add(tweet);
                        } else {
                            currentTag = RSSXMLTag.IGNORETAG;
                        }
                    } else if (eventType == XmlPullParser.TEXT) {
                        // Find text between tag
                        String content = xpp.getText();
                        content = content.trim();
                        if (tweet != null) {
                            switch (currentTag) {
                                case TITLE:
                                    if (content.length() != 0) {
                                        if (tweet.postTitle != null) {
                                            tweet.postTitle += content;
                                        } else {
                                            tweet.postTitle = content;
                                        }
                                    }
                                    break;
                                case CREATOR:
                                    if (content.length() != 0) {
                                        if (tweet.postCreator != null) {
                                            tweet.postCreator += content;
                                        } else {
                                            tweet.postCreator = content;
                                        }
                                    }
                                    break;
                                case DESCRIPTION:
                                    if (content.length() != 0) {
                                        if (tweet.postDescription != null) {
                                            tweet.postDescription += content;
                                        } else {
                                            tweet.postDescription = content;
                                        }
                                    }
                                    break;
                                default:
                                    break;
                            }
                        }
                    }

                    eventType = xpp.next();
                }
                // After XML parse
                tf.username = twitterUsername;
                tf.tweets = tweets;


                mTwitterUsername = twitterUsername;
                mTwitterUsernameText.setText(mTwitterUsername);
            }
            // If http request returns an error
            else {
                connection.getErrorStream();
                tf.tweets.get(0).postTitle = "HTTP " + responseCode + " error";
            }
        }
        // If something else goes wrong
        catch (Exception e) {
            e.printStackTrace();
            tf.tweets.get(0).postDescription = e.toString();
        }
        return tf;
    }*/

    public void send(View view) {
        showProgress();

        new AsyncTask<String, Void, TwitterFeed>() {
            @Override
            protected TwitterFeed doInBackground(String... strings) {
                try {
                    /*String serialNumber = strings[0];
                    return MukiCupApi.cupIdentifierFromSerialNumber(serialNumber);*/
                    TwitterFeed tf = new TwitterFeed(strings[0]);
                    tf.getFeed();
                    return tf;
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return null;
            }

            @Override
            protected void onPostExecute(TwitterFeed tf) {
                if (tf != null) {
                    mTwitterUsername = tf.username;
                    mTwitterUsernameText.setText(mTwitterUsername);
                    Tweet tweet;
                    if (tf.tweets.size() == 1) {
                        tweet = tf.tweets.get(0);
                    } else {
                        tweet = tf.tweets.get(1);
                    }
                    mDebugLogText.setText(tweet.postCreator + "\n\n" + tweet.postTitle + "\n\n" + tweet.postDescription);
                    /*mMukiCupApi.sendImage(this.tweetToImage(tf.tweets.get(0)), new ImageProperties(mContrast), mCupId);*/
                }
                else {
                    mDebugLogText.setText("Something went wrong");
                }

                hideProgress();
            }
        }.execute(mTwitterUsernameEdit.getText().toString());
    }

    public void request(View view) {
        String serialNumber = mSerialNumberEdit.getText().toString();
        showProgress();
        new AsyncTask<String, Void, String>() {
            @Override
            protected String doInBackground(String... strings) {
                try {
                    String serialNumber = strings[0];
                    return MukiCupApi.cupIdentifierFromSerialNumber(serialNumber);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return null;
            }

            @Override
            protected void onPostExecute(String s) {
                mCupId = s;
                mCupIdText.setText(mCupId);
                hideProgress();
            }
        }.execute(serialNumber);
    }

    public void deviceInfo(View view) {
        showProgress();
        mMukiCupApi.getDeviceInfo(mCupId);
    }

    private void showToast(final String text) {
        hideProgress();
        Toast.makeText(MainActivity.this, text, Toast.LENGTH_SHORT).show();
    }

    private Bitmap tweetToImage(Tweet tweet) {
        return Bitmap.createBitmap(296, 400, Bitmap.Config.ARGB_8888);
    }

    private void showProgress() {
        mProgressDialog.show();
    }

    private void hideProgress() {
        mProgressDialog.dismiss();
    }

        /* public void getTweets (View view) {
        final String twitterUsername = mTwitterUsernameEdit.getText().toString();
        showProgress();
        new AsyncTask<String, Void, TwitterFeed>() {
            @Override
            protected TwitterFeed doInBackground(String... strings) {
                String username = strings[0];
                try {
                    URL url = new URL("https://twitrss.me/twitter_user_to_rss/?user=" + username);
                    HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                    connection.setReadTimeout(10*1000);
                    connection.setConnectTimeout(10*1000);
                    connection.setRequestMethod("GET");
                    connection.setDoInput(true);
                    connection.connect();
                    int responseCode = connection.getResponseCode();

                    // Read message
                    if (responseCode % 100 == 2) {
                        InputStream is = connection.getInputStream();
                        final int bufferSize = 1024;
                        byte[] buffer = new byte[1024];
                        ByteArrayOutputStream os = new ByteArrayOutputStream();
                        while (true) {
                            int count = is.read(buffer, 0, bufferSize);
                            if (count == -1) {
                                break;
                            }

                            os.write(buffer);
                        }
                        os.close();
                        String xmlResult = new String(os.toByteArray(), "UTF-8");


                        // Parse XML
                        XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
                        factory.setNamespaceAware(true);
                        XmlPullParser xpp = factory.newPullParser();
                        xpp.setInput(is, null);

                        int eventType = xpp.getEventType();
                        ArrayList<Tweet> postDataList = new ArrayList<Tweet>();
                        Tweet pdData = null;
                        RSSXMLTag currentTag = null;


                        while (eventType != XmlPullParser.END_DOCUMENT) {
                            if (eventType == XmlPullParser.START_DOCUMENT) {
                                // Parsing start
                            } else if (eventType == XmlPullParser.START_TAG) {
                                // Find a starting tag
                                if (xpp.getName().equals("item")) {
                                    pdData = new Tweet();
                                    currentTag = RSSXMLTag.IGNORETAG;
                                }
                                else if (xpp.getName().equals("title")) {
                                    currentTag = RSSXMLTag.TITLE;
                                }
                                else if (xpp.getName().equals("dc:creator")) {
                                    currentTag = RSSXMLTag.CREATOR;
                                }
                                else if (xpp.getName().equals("description")) {
                                    currentTag = RSSXMLTag.DESCRIPTION;
                                }
                            } else if (eventType == XmlPullParser.END_TAG) {
                                // Find a ending tag
                                if (xpp.getName().equals("item")) {
                                    // format the data here, otherwise format data in
                                    // Adapter
                                    postDataList.add(pdData);
                                } else {
                                    currentTag = RSSXMLTag.IGNORETAG;
                                }
                            } else if (eventType == XmlPullParser.TEXT) {
                                // Find text between tag
                                String content = xpp.getText();
                                content = content.trim();
                                if (pdData != null && currentTag != null) {
                                    switch (currentTag) {
                                        case TITLE:
                                            if (content.length() != 0) {
                                                if (pdData.postTitle != null) {
                                                    pdData.postTitle += content;
                                                } else {
                                                    pdData.postTitle = content;
                                                }
                                            }
                                            break;
                                        case CREATOR:
                                            if (content.length() != 0) {
                                                if (pdData.postCreator != null) {
                                                    pdData.postCreator += content;
                                                } else {
                                                    pdData.postCreator = content;
                                                }
                                            }
                                            break;
                                        case DESCRIPTION:
                                            if (content.length() != 0) {
                                                if (pdData.postDescription != null) {
                                                    pdData.postDescription += content;
                                                } else {
                                                    pdData.postDescription = content;
                                                }
                                            }
                                            break;
                                        default:
                                            break;
                                    }
                                }
                            }

                            eventType = xpp.next();
                        }
                        // After XML parse
                        TwitterFeed twitterFeed = new TwitterFeed();
                        twitterFeed.username = username;
                        twitterFeed.tweets = postDataList;
                        return twitterFeed;
                    }
                    // If http request returns an error
                    else {
                        connection.getErrorStream();
                    }
                    return null;
                }
                // If something else goes wrong
                catch (Exception e) {
                    e.printStackTrace();
                }
                return null;
            }

            @Override
            protected void onPostExecute(TwitterFeed tf) {
                mTwitterUsername = tf.username;
                mTwitterUsernameText.setText(mTwitterUsername);
                hideProgress();
            }
        }.execute(twitterUsername);

    } */
}

