package ca.bradj.picloq.app;

import android.app.Activity;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Path;
import android.preference.PreferenceManager;
import android.util.Log;
import android.util.Pair;

import com.google.common.base.Optional;
import com.google.common.collect.Lists;

import org.joda.time.DateTime;
import org.json.JSONArray;
import org.json.JSONException;

import java.io.File;
import java.util.Collection;
import java.util.List;

/**
 * Utilites for working with {@link PicsByHour}.
 */
public class PicsByHourUtils {

    public static final String PICCHOICES = "picchoices";

    public static PicsByHour load(final Activity activity) throws JSONException {

        String saved = readSavedPictureSelections(activity);
        Log.d("ParsePics", "Pictures JSON is : " + saved);
        final JSONArray hours = new JSONArray(saved);

        final List<Optional<File>> listContents = Lists.newArrayList();
        for (int i = 0; i < hours.length(); i++) {

            String fileName = hours.getString(i);
            if (fileName.isEmpty()) {
                listContents.add(Optional.<File>absent());
                continue;
            }
            File file = new File(fileName);
            if (file.exists()) {
                listContents.add(Optional.of(file));
                continue;
            }
            listContents.add(Optional.<File>absent());
        }
        return new PicsByHour() {
            @Override
            public Bitmap getImageAtOrBefore(DateTime dateTime) {

                int hourOfDay = dateTime.getHourOfDay();

                Optional<String> foundImage = Optional.absent();
                for (int i = 0; i <= hourOfDay; i++) {
                    if (listContents.get(i).isPresent()) {
                        foundImage = Optional.of(listContents.get(i).get().getAbsolutePath());
                    }
                }
                if (foundImage.isPresent()) {
                    Log.d("ImageAtHour","Using image " + foundImage.get() + " for hour " + dateTime);
                    return BitmapFactory.decodeFile(foundImage.get());
                }
                Log.d("ImageAtHour","Using error image for hour " + dateTime);
                return BitmapFactory.decodeResource(activity.getResources(), R.drawable.error);
            }

            @Override
            public Collection<Pair<String, Optional<File>>> withHourStrings() {

                Collection<Pair<String, Optional<File>>> out = Lists.newArrayList();
                for (int i = 0; i < 24; i++) {
                    out.add(new Pair<String, Optional<File>>(i + ":00", listContents.get(i)));
                }
                return out;
            }
        };
    }

    private static String readSavedPictureSelections(Activity activity) {
        SharedPreferences p = PreferenceManager.getDefaultSharedPreferences(activity);
        StringBuilder defBld = new StringBuilder();
        defBld.append("[");
        for (int i = 0; i < 24; i++) {
            defBld.append("\"\",");
        }
        return p.getString(PICCHOICES, defBld.toString().substring(0, defBld.length() - 1) + "]");
    }

    public static void storeNewPicSelections(List<Pair<String, Optional<File>>> listContents, Activity activity) {
        SharedPreferences p = PreferenceManager.getDefaultSharedPreferences(activity);

        List<String> map = Lists.newArrayList();
        for (Pair<String, Optional<File>> i : listContents) {
            Optional<File> file = i.second;
            String str = "";
            if (file.isPresent()) {
                str = file.get().getAbsolutePath();
            }
            map.add(str);
        }
        JSONArray array = new JSONArray(map);
        Log.d("PicChoicesWrite", "Writing out JSON: " + array);
        p.edit().putString(PICCHOICES, array.toString()).apply();
    }
}
