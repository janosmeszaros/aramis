package hu.u_szeged.inf.aramis.activities.fileselector;

import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ListView;

import java.io.File;
import java.sql.Date;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import hu.u_szeged.inf.aramis.R;

public class FileChooser extends ListActivity {

    private File currentDir;
    private FileArrayAdapter adapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        currentDir = new File("/sdcard/");
        fill(currentDir);
    }

    private void fill(File f) {
        File[] dirs = f.listFiles();
        this.setTitle("Current Dir: " + f.getName());
        List<Item> dir = new ArrayList<Item>();
        List<Item> fls = new ArrayList<Item>();
        try {
            for (File ff : dirs) {
                Date lastModDate = new Date(ff.lastModified());
                DateFormat formatter = DateFormat.getDateTimeInstance();
                String date_modify = formatter.format(lastModDate);
                if (ff.isDirectory()) {
                    dir.add(new Item(ff.getName(), date_modify, ff.getAbsolutePath(), "directory_icon"));
                } else {
                    fls.add(new Item(ff.getName(), date_modify, ff.getAbsolutePath(), "file_icon"));
                }
            }
        } catch (Exception e) {

        }
        Collections.sort(dir);
        Collections.sort(fls);
        dir.addAll(fls);
        if (!f.getName().equalsIgnoreCase("sdcard"))
            dir.add(0, new Item("..", "Parent Directory", f.getParent(), "directory_up"));
        adapter = new FileArrayAdapter(FileChooser.this, R.layout.file_view, dir);
        this.setListAdapter(adapter);
    }

    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);
        Item o = adapter.getItem(position);
        if (o.image.equalsIgnoreCase("directory_icon") || o.image.equalsIgnoreCase("directory_up")) {
            currentDir = new File(o.path);
            fill(currentDir);
        } else {
            onFileClick(o);
        }
    }

    private void onFileClick(Item o) {
        Intent intent = new Intent();
        intent.putExtra("GetPath", currentDir.toString());
        intent.putExtra("GetFileName", o.name);
        setResult(RESULT_OK, intent);
        finish();
    }
}
