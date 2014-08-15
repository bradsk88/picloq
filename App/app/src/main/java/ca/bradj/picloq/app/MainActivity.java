package ca.bradj.picloq.app;

import android.annotation.TargetApi;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Build;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextClock;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.json.JSONException;

import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;


public class MainActivity extends ActionBarActivity {

    private static final java.lang.String NEED_TO_SCHEDULE = "ca.bradj.picloq.app.MainActivity.NEED_TO_SCHEDULE";

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        TextClock clock = (TextClock) findViewById(R.id.textClock);
        DateTimeZone dateTimeZone = DateTimeZone.forID("Europe/London");
        clock.setTimeZone(dateTimeZone.toString());
        Log.d("Main", "Time zone in London is " + dateTimeZone);

        updateCurrentImage();

        final Button dimHi = (Button) findViewById(R.id.dimHiBtn);
        final Button dimMed = (Button) findViewById(R.id.dimMedBtn);
        final Button dimLo = (Button) findViewById(R.id.dimLowBtn);
        final SeekBar bar = (SeekBar) findViewById(R.id.dimSeek);
        bar.setProgress(100);

        dimHi.setOnClickListener(new AdjustAlpha(dimHi, dimMed, dimLo, bar, 1f));
        dimMed.setOnClickListener(new AdjustAlpha(dimHi, dimMed, dimLo, bar, 0.4f));
        dimLo.setOnClickListener(new AdjustAlpha(dimHi, dimMed, dimLo, bar, 0.1f));

        bar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                new AdjustAlpha(dimHi, dimMed, dimLo, bar, (float) i / 100f).onClick(seekBar);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                new AdjustAlpha(dimHi, dimMed, dimLo, bar, (float) seekBar.getProgress() / 100f).onClick(seekBar);
            }

            @Override
            public void onStopTrackingTouch(final SeekBar seekBar) {
                Timer t = new Timer();
                t.schedule(new AutoDimSeekBar(seekBar), DateTime.now().plusSeconds(3).toDate());
            }
        });

        Timer t = new Timer();
        t.schedule(new ScheduleUpdatePic(), nextHour());
        t.schedule(new AutoDimSeekBar(bar), DateTime.now().plusSeconds(10).toDate());
        getWindow().setBackgroundDrawableResource(R.drawable.abc_item_background_holo_dark);

    }

    private java.util.Date nextHour() {
        DateTime inAnHour = DateTime.now().plusHours(1).plusMinutes(1); //1 minute just in case it lands exactly at 11:59:99 or something.
        DateTime next = new DateTime(inAnHour.getYear(), inAnHour.getMonthOfYear(), inAnHour.getDayOfMonth(), inAnHour.getHourOfDay(), 0);
        return next.toDate();
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

            Bitmap bmap = load.getImageAtOrBefore(DateTime.now().withZone(DateTimeZone.forID("Europe/London")));
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

    private class ScheduleUpdatePic extends TimerTask {
        @Override
        public void run() {
            Log.d("UpdateImg", "Will update image on UI thread");
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Log.d("UpdateImg", "Updating image");
                    updateCurrentImage();
                    Timer t = new Timer();
                    Date when = nextHour();
                    t.schedule(new ScheduleUpdatePic(), when);
                    Log.d("UpdateImg", "Scheduled image update for " + when);
                }
            });
        }
    }

    private class AdjustAlpha implements View.OnClickListener {
        private final Button dimHi;
        private final Button dimMed;
        private final Button dimLo;
        private final float amount;
        private final SeekBar bar;

        public AdjustAlpha(Button dimHi, Button dimMed, Button dimLo, SeekBar bar, float amount) {
            this.dimHi = dimHi;
            this.dimMed = dimMed;
            this.dimLo = dimLo;
            this.amount = amount;
            this.bar = bar;
        }

        @Override
        public void onClick(View view) {
            float alwaysVisible = Math.max(amount, 0.2f);
            ImageView viewById = (ImageView) findViewById(R.id.clockImage);
            viewById.setAlpha(amount);
            dimHi.setAlpha(alwaysVisible);
            dimMed.setAlpha(alwaysVisible);
            dimLo.setAlpha(alwaysVisible);
            bar.setProgress((int) (amount * 100));
            bar.setAlpha(alwaysVisible);
            Timer t = new Timer();
            t.schedule(new AutoDimSeekBar(bar), DateTime.now().plusSeconds(3).toDate());
        }
    }

    private class AutoDimSeekBar extends TimerTask {
        private final SeekBar seekBar;

        public AutoDimSeekBar(SeekBar seekBar) {
            this.seekBar = seekBar;
        }

        @Override
        public void run() {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    final float endAlpha = 0.1f;
                    Animation animation = new AlphaAnimation(seekBar.getAlpha(), endAlpha);
                    animation.setDuration(1000);
                    animation.setAnimationListener(new Animation.AnimationListener() {
                        @Override
                        public void onAnimationStart(Animation animation) {
                        }

                        @Override
                        public void onAnimationEnd(Animation animation) {
                            seekBar.setAlpha(endAlpha);
                        }

                        @Override
                        public void onAnimationRepeat(Animation animation) {

                        }
                    });
                    seekBar.startAnimation(animation);
                }
            });
        }
    }
}
