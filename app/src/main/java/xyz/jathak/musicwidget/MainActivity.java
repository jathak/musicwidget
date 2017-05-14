package xyz.jathak.musicwidget;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class MainActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (savedInstanceState == null) {
            getFragmentManager().beginTransaction()
                    .add(R.id.container, new PlaceholderFragment())
                    .commit();
        }

    }

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class PlaceholderFragment extends Fragment {

        public PlaceholderFragment() {
        }
        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            int layout = R.layout.fragment_main_plus;
            View rootView = inflater.inflate(layout, container, false);
            Button b = (Button) rootView.findViewById(R.id.button);
            b.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent i = new Intent("android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS");
                    startActivity(i);
                }
            });
            final SharedPreferences p = PreferenceManager.getDefaultSharedPreferences(getActivity());
            final int style = p.getInt("style", R.id.albummatch);
            RadioGroup group = (RadioGroup)rootView.findViewById(R.id.group);
            group.check(style);
            group.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(RadioGroup radioGroup, int i) {
                    if(i!=p.getInt("style", R.id.albummatch)){
                        SharedPreferences.Editor e = p.edit();
                        e.putInt("style",i);
                        e.commit();
                    }
                    getActivity().sendBroadcast(new Intent(StandardWidget.WIDGET_UPDATE));
                }
            });
            final Button app = (Button)rootView.findViewById(R.id.appselect);
            app.setText(p.getString("appName","None"));
            app.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    AlertDialog.Builder b = new AlertDialog.Builder(getActivity());
                    b.setTitle("Select your music app");
                    pm = getActivity().getPackageManager();
                    Intent intent = new Intent(Intent.ACTION_MAIN, null);
                    intent.addCategory(Intent.CATEGORY_LAUNCHER);
                    final List<ResolveInfo> packs = pm.queryIntentActivities(intent, PackageManager.PERMISSION_GRANTED);
                    Collections.sort(packs,new AppNameCompare());
                    final List<String> names = new ArrayList<String>();
                    names.add("None");
                    for(ResolveInfo ai:packs){
                        names.add((String) ai.loadLabel(pm));
                    }
                    ListAdapter la = new ArrayAdapter<String>(getActivity(),android.R.layout.simple_list_item_1,names);
                    b.setAdapter(la, new DialogInterface.OnClickListener(){
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            SharedPreferences.Editor e = p.edit();
                            if(i>0){
                                e.putString("appLaunch",packs.get(i-1).activityInfo.packageName);
                            }
                            else {
                                e.putString("appLaunch","");
                            }
                            e.putString("appName",names.get(i));
                            e.commit();
                            app.setText(names.get(i));
                        }
                    });
                    b.show();
                }
            });
            CheckBox albumName = (CheckBox)rootView.findViewById(R.id.showAlbum);
            albumName.setChecked(p.getBoolean("showAlbum",false));
            albumName.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                    SharedPreferences.Editor e = p.edit();
                    e.putBoolean("showAlbum",b);
                    e.commit();
                    update();
                }
            });
            CheckBox songCaps = (CheckBox)rootView.findViewById(R.id.songCaps);
            CheckBox albumCaps = (CheckBox)rootView.findViewById(R.id.albumCaps);
            CheckBox artistCaps = (CheckBox)rootView.findViewById(R.id.artistCaps);
            songCaps.setChecked(p.getBoolean("songCaps",false));
            artistCaps.setChecked(p.getBoolean("artistCaps",false));
            albumCaps.setChecked(p.getBoolean("albumCaps",false));
            songCaps.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                    SharedPreferences.Editor e = p.edit();
                    e.putBoolean("songCaps",b);
                    e.commit();
                    update();
                }
            });
            artistCaps.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                    SharedPreferences.Editor e = p.edit();
                    e.putBoolean("artistCaps",b);
                    e.commit();
                    update();
                }
            });
            albumCaps.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                    SharedPreferences.Editor e = p.edit();
                    e.putBoolean("albumCaps",b);
                    e.commit();
                    update();
                }
            });
            Spinner songFont = (Spinner)rootView.findViewById(R.id.songFont);
            Spinner artistFont = (Spinner)rootView.findViewById(R.id.artistFont);
            Spinner albumFont = (Spinner)rootView.findViewById(R.id.albumFont);
            CheckBox songBold = (CheckBox)rootView.findViewById(R.id.songBold);
            CheckBox songItalic = (CheckBox)rootView.findViewById(R.id.songItalic);
            CheckBox artistBold = (CheckBox)rootView.findViewById(R.id.artistBold);
            CheckBox artistItalic = (CheckBox)rootView.findViewById(R.id.artistItalic);
            CheckBox albumBold = (CheckBox)rootView.findViewById(R.id.albumBold);
            CheckBox albumItalic = (CheckBox)rootView.findViewById(R.id.albumItalic);
            SeekBar songSize = (SeekBar)rootView.findViewById(R.id.songSize);
            final TextView songSizeLabel = (TextView)rootView.findViewById(R.id.songSizeLabel);
            SeekBar artistSize = (SeekBar)rootView.findViewById(R.id.artistSize);
            final TextView artistSizeLabel = (TextView)rootView.findViewById(R.id.artistSizeLabel);
            SeekBar albumSize = (SeekBar)rootView.findViewById(R.id.albumSize);
            final TextView albumSizeLabel = (TextView)rootView.findViewById(R.id.albumSizeLabel);
            songFont.setSelection(p.getInt("songFont",0));
            artistFont.setSelection(p.getInt("artistFont",0));
            albumFont.setSelection(p.getInt("albumFont",0));
            songBold.setChecked(p.getBoolean("songBold", false));
            artistBold.setChecked(p.getBoolean("artistBold", false));
            songItalic.setChecked(p.getBoolean("songItalic", false));
            artistItalic.setChecked(p.getBoolean("artistItalic",false));
            albumBold.setChecked(p.getBoolean("albumBold", false));
            albumItalic.setChecked(p.getBoolean("albumItalic",false));
            songSize.setProgress(p.getInt("songSize", 4));
            songSizeLabel.setText((p.getInt("songSize", 4) * 2 + 10)+" sp");
            artistSize.setProgress(p.getInt("artistSize", 2));
            artistSizeLabel.setText((p.getInt("artistSize",2)*2+10)+" sp");
            albumSize.setProgress(p.getInt("albumSize", 2));
            albumSizeLabel.setText((p.getInt("albumSize",2)*2+10)+" sp");
            songFont.setOnItemSelectedListener(new Spinner.OnItemSelectedListener(){
                public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                    SharedPreferences.Editor e = p.edit();
                    e.putInt("songFont",i);
                    e.commit();
                    update();
                }
                public void onNothingSelected(AdapterView<?> adapterView) {}
            });
            artistFont.setOnItemSelectedListener(new Spinner.OnItemSelectedListener(){
                public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                    SharedPreferences.Editor e = p.edit();
                    e.putInt("artistFont",i);
                    e.commit();
                    update();
                }
                public void onNothingSelected(AdapterView<?> adapterView) {}
            });
            albumFont.setOnItemSelectedListener(new Spinner.OnItemSelectedListener(){
                public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                    SharedPreferences.Editor e = p.edit();
                    e.putInt("albumFont",i);
                    e.commit();
                    update();
                }
                public void onNothingSelected(AdapterView<?> adapterView) {}
            });
            songBold.setOnCheckedChangeListener(new CheckBox.OnCheckedChangeListener(){
                public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                    SharedPreferences.Editor e = p.edit();
                    e.putBoolean("songBold", b);
                    e.commit();
                    update();
                }
            });
            artistBold.setOnCheckedChangeListener(new CheckBox.OnCheckedChangeListener(){
                public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                    SharedPreferences.Editor e = p.edit();
                    e.putBoolean("artistBold",b);
                    e.commit();
                    update();
                }
            });
            albumBold.setOnCheckedChangeListener(new CheckBox.OnCheckedChangeListener(){
                public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                    SharedPreferences.Editor e = p.edit();
                    e.putBoolean("albumBold",b);
                    e.commit();
                    update();
                }
            });
            songItalic.setOnCheckedChangeListener(new CheckBox.OnCheckedChangeListener(){
                public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                    SharedPreferences.Editor e = p.edit();
                    e.putBoolean("songItalic",b);
                    e.commit();
                    update();
                }
            });
            artistItalic.setOnCheckedChangeListener(new CheckBox.OnCheckedChangeListener(){
                public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                    SharedPreferences.Editor e = p.edit();
                    e.putBoolean("artistItalic",b);
                    e.commit();
                    update();
                }
            });
            albumItalic.setOnCheckedChangeListener(new CheckBox.OnCheckedChangeListener(){
                public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                    SharedPreferences.Editor e = p.edit();
                    e.putBoolean("albumItalic",b);
                    e.commit();
                    update();
                }
            });
            songSize.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                @Override
                public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                    SharedPreferences.Editor e = p.edit();
                    e.putInt("songSize",i);
                    e.commit();
                    songSizeLabel.setText((i*2+10)+" sp");
                    update();
                }
                public void onStartTrackingTouch(SeekBar seekBar) {}
                public void onStopTrackingTouch(SeekBar seekBar) {}
            });
            artistSize.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                @Override
                public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                    SharedPreferences.Editor e = p.edit();
                    e.putInt("artistSize",i);
                    e.commit();
                    artistSizeLabel.setText((i*2+10)+" sp");
                    update();
                }
                public void onStartTrackingTouch(SeekBar seekBar) {}
                public void onStopTrackingTouch(SeekBar seekBar) {}
            });
            albumSize.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                @Override
                public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                    SharedPreferences.Editor e = p.edit();
                    e.putInt("albumSize",i);
                    e.commit();
                    albumSizeLabel.setText((i*2+10)+" sp");
                    update();
                }
                public void onStartTrackingTouch(SeekBar seekBar) {}
                public void onStopTrackingTouch(SeekBar seekBar) {}
            });

            return rootView;
        }

        public void update(){
            getActivity().sendBroadcast(new Intent(StandardWidget.WIDGET_UPDATE));
        }
    }
    static PackageManager pm;
    static class AppNameCompare implements Comparator<ResolveInfo> {

        @Override
        public int compare(ResolveInfo a, ResolveInfo b) {
            return ((String)a.loadLabel(pm)).compareTo((String)b.loadLabel(pm));
        }
    }
}
