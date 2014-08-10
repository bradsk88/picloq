package ca.bradj.picloq.app;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.os.Bundle;
import android.util.Log;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.common.base.Optional;
import com.google.common.collect.Lists;
import com.kbeanie.imagechooser.api.ChooserType;
import com.kbeanie.imagechooser.api.ChosenImage;
import com.kbeanie.imagechooser.api.FileUtils;
import com.kbeanie.imagechooser.api.ImageChooserListener;
import com.kbeanie.imagechooser.api.ImageChooserManager;
import com.soundcloud.android.crop.Crop;

import org.json.JSONArray;
import org.json.JSONException;

import java.io.File;
import java.io.IOException;
import java.util.List;

public class SelectPicturesActivity extends Activity {

    private ImageChooserManager imageChooserManager;
    private ImageView resultView;
    private int resultHour;
    final List<Pair<String, Optional<File>>> listContents = Lists.newArrayList();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getActionBar() != null) {
            getActionBar().setDisplayHomeAsUpEnabled(true);
        }
        setContentView(R.layout.activity_select_pictures);

        try {
            listContents.addAll(PicsByHourUtils.load(this).withHourStrings());
        } catch (JSONException e) {
            e.printStackTrace();
        }

        ListView myListView = (ListView) findViewById(R.id.picturesListView);
        myListView.setAdapter(new PictureSelectionAdapter());
    }

    private void setPictureForCell(int position, ImageView view) {
        Optional<File> second = listContents.get(position).second;
        if (second.isPresent()) {
            Bitmap bmp = BitmapFactory.decodeFile(second.get().getAbsolutePath());
            applyNewImage(view, bmp);
            return;
        }
        Bitmap bm = BitmapFactory.decodeResource(getResources(), R.drawable.willuse);
        applyNewImage(view, bm);
    }

    private void applyNewImage(ImageView view, Bitmap bm) {
        view.setImageBitmap(bm);
        view.getLayoutParams().height = getNewHeight(view.getLayoutParams().width, bm);
        view.requestLayout();
    }

    private int getNewHeight(int width, Bitmap bmp) {
        float bmapWidth = bmp.getWidth();
        float bmapHeight = bmp.getHeight();

        float ratioMultiplier = width / bmapWidth;

        int newBmapHeight = (int) (bmapHeight * ratioMultiplier);
        return newBmapHeight;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent result) {
        if (resultCode == RESULT_OK &&
                (requestCode == ChooserType.REQUEST_PICK_PICTURE ||
                        requestCode == ChooserType.REQUEST_CAPTURE_PICTURE)) {
            imageChooserManager.submit(requestCode, result);
        }

        if (requestCode == Crop.REQUEST_CROP) {
            handleCropEnd(resultCode, result);
        }

    }

    private void handleCropEnd(int resultCode, Intent result) {
        if (resultCode == RESULT_OK) {
            Uri output = Crop.getOutput(result);
            resultView.setImageURI(output);
            Optional<File> of = Optional.of(new File(output.getPath()));
            listContents.set(resultHour, new Pair<String, Optional<File>>(resultHour + ":00", of));

            writeListBackOut(listContents);

        } else if (resultCode == Crop.RESULT_ERROR) {
            Toast.makeText(this, Crop.getError(result).getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void writeListBackOut(List<Pair<String, Optional<File>>> listContents) {

        PicsByHourUtils.storeNewPicSelections(listContents, this);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private class OpenImageSelection implements View.OnClickListener {
        private final int position;
        private final View inflate;

        public OpenImageSelection(int position, View inflate) {
            this.position = position;
            this.inflate = inflate;
        }

        @Override
        public void onClick(final View v) {
            imageChooserManager = new ImageChooserManager(SelectPicturesActivity.this,
                    ChooserType.REQUEST_PICK_PICTURE);
            imageChooserManager.setImageChooserListener(new ImageChooserListener() {
                @Override
                public void onImageChosen(ChosenImage chosenImage) {
                    Log.d("SelectPic", "Selected picture with path " + chosenImage.getFilePathOriginal());

                    final File imgFile = new File(chosenImage.getFilePathOriginal());
                    if (imgFile.exists()) {

                        final File pic = new File(getFilesDir(), "hour" + position + "." + FileUtils.getFileExtension(imgFile.getAbsolutePath()));
                        if (!pic.exists()) {
                            try {
                                boolean result = pic.createNewFile();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                        final Uri outUri = Uri.fromFile(pic);

                        resultView = (ImageView) inflate.findViewById(R.id.hourPic);
                        resultHour = position;
                        Crop crop = new Crop(Uri.fromFile(imgFile));
                        crop.output(outUri).withAspect(4, 3).start(SelectPicturesActivity.this);

                    }
                }

                @Override
                public void onError(String s) {
                    Log.d("SelectPic", s);
                }
            });
            try {
                imageChooserManager.choose();
            } catch (IllegalArgumentException e) {
                e.printStackTrace();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private class PictureSelectionAdapter extends ArrayAdapter<Pair<String, Optional<File>>> {
        public PictureSelectionAdapter() {
            super(SelectPicturesActivity.this, android.R.layout.simple_list_item_1, SelectPicturesActivity.this.listContents);
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            LayoutInflater vi = LayoutInflater.from(getContext());
            final View inflate = vi.inflate(R.layout.fragment_select_pic, parent, false);
            if (inflate == null) {
                return null;
            }
            TextView text = (TextView) inflate.findViewById(R.id.hourText);
            Pair<String, Optional<File>> stringOptionalPair = listContents.get(position);
            text.setText(stringOptionalPair.first);

            Button button = (Button) inflate.findViewById(R.id.addPicButton);
            button.setOnClickListener(new OpenImageSelection(position, inflate));

            ImageView view = (ImageView) inflate.findViewById(R.id.hourPic);
            setPictureForCell(position, view);

            //TODO: Add "remove photo" button.

            return inflate;
        }
    }
}
