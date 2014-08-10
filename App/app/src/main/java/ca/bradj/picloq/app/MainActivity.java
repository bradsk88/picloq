package ca.bradj.picloq.app;

import android.annotation.TargetApi;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Pair;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextClock;

import com.google.common.base.Optional;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.json.JSONException;

import java.io.File;
import java.util.Collection;


public class MainActivity extends ActionBarActivity {

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        TextClock clock = (TextClock) findViewById(R.id.textClock);
        clock.setTimeZone("UTC");

        updateCurrentImage();
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateCurrentImage();
    }

    private void updateCurrentImage() {

        DisplayMetrics metrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metrics);
        int height = metrics.heightPixels;
        int width = metrics.widthPixels;

        ImageView iView = (ImageView) findViewById(R.id.clockImage);

        try {
            PicsByHour load = PicsByHourUtils.load(this);

            Bitmap bmap = load.getImageAtOrBefore(DateTime.now().withZone(DateTimeZone.UTC));
            iView.setImageBitmap(bmap);

            RelativeLayout.LayoutParams params = getImageViewProportionParams(height, width, bmap);
            iView.setLayoutParams(params);


        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private RelativeLayout.LayoutParams getImageViewProportionParams(int height, int width, Bitmap bmap) {
        float bmapWidth = bmap.getWidth();
        float bmapHeight = bmap.getHeight();

        float wRatio = width / bmapWidth;
        float hRatio = height / bmapHeight;

        float ratioMultiplier = wRatio;
// Untested conditional though I expect this might work for landscape mode
        if (hRatio < wRatio) {
            ratioMultiplier = hRatio;
        }

        int newBmapWidth = (int) (bmapWidth * ratioMultiplier);
        int newBmapHeight = (int) (bmapHeight * ratioMultiplier);

        return new RelativeLayout.LayoutParams(newBmapWidth, newBmapHeight);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            Intent intent = new Intent(this, SelectPicturesActivity.class);
            startActivity(intent);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
