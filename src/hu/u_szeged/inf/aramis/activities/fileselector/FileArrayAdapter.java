package hu.u_szeged.inf.aramis.activities.fileselector;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

import hu.u_szeged.inf.aramis.R;


public class FileArrayAdapter extends ArrayAdapter<Item> {
    private Context c;
    private int id;
    private List<Item> items;

    public FileArrayAdapter(Context context, int textViewResourceId, List<Item> objects) {
        super(context, textViewResourceId, objects);
        c = context;
        id = textViewResourceId;
        items = objects;
    }

    public Item getItem(int i) {
        return items.get(i);
    }


    static class ViewHolder {
        protected ImageView image;
        protected TextView text1;
        protected TextView text2;
        protected CheckBox checkbox;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View v = convertView;
        Item item = items.get(position);
        if (v == null) {
            LayoutInflater vi = (LayoutInflater) c.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            v = vi.inflate(id, null);
            final ViewHolder viewHolder = new ViewHolder();
            viewHolder.image = (ImageView) v.findViewById(R.id.fd_Icon1);
            viewHolder.text1 = (TextView) v.findViewById(R.id.TextView01);
            viewHolder.text2 = (TextView) v.findViewById(R.id.TextViewDate);
            viewHolder.checkbox = (CheckBox) v.findViewById(R.id.check);
            if (item.image.contains("file_icon")) {
                viewHolder.checkbox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton buttonView,
                                                 boolean isChecked) {
                        Item element = (Item) viewHolder.checkbox.getTag();
                        element.setSelected(buttonView.isChecked());

                    }
                });
                viewHolder.checkbox.setTag(item);
            } else {
                viewHolder.checkbox.setVisibility(View.GONE);
            }
            Drawable image = getDrawable(item);
            viewHolder.image.setImageDrawable(image);
            viewHolder.text1.setText(item.name);
            viewHolder.text1.setText(item.date);
            v.setTag(viewHolder);

        } else {
            v = convertView;
            ((ViewHolder) v.getTag()).checkbox.setTag(item);
        }
        ViewHolder holder = (ViewHolder) v.getTag();
        Drawable image = getDrawable(item);
        holder.image.setImageDrawable(image);
        holder.text1.setText(item.name);
        holder.text2.setText(item.date);
        holder.checkbox.setChecked(item.isSelected());
        if (item.image.contains("file_icon")) {
            holder.checkbox.setVisibility(View.VISIBLE);
        } else {
            holder.checkbox.setVisibility(View.GONE);
        }
        return v;
    }

    private Drawable getDrawable(Item item) {
        String uri = "drawable/" + item.image;
        int imageResource = c.getResources().getIdentifier(uri, null, c.getPackageName());
        return c.getResources().getDrawable(imageResource);
    }

}
