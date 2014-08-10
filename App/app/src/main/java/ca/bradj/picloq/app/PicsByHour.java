package ca.bradj.picloq.app;

import android.graphics.Bitmap;
import android.graphics.Path;
import android.util.Pair;

import com.google.common.base.Optional;

import org.joda.time.DateTime;

import java.io.File;
import java.util.Collection;

/**
 * Created by Brad on 8/9/2014.
 */
public interface PicsByHour {
    Bitmap getImageAtOrBefore(DateTime dateTime);

    Collection<? extends Pair<String,Optional<File>>> withHourStrings();
}
