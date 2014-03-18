package hu.u_szeged.inf.aramis.activities.fileselector;

public class Item implements Comparable<Item> {
    public final String name;
    public final String date;
    public final String path;
    public final String image;
    private boolean selected = false;

    public Item(String n, String dt, String p, String img) {
        name = n;
        date = dt;
        path = p;
        image = img;

    }

    public boolean isSelected() {
        return selected;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }

    public int compareTo(Item o) {
        if (this.name != null)
            return this.name.toLowerCase().compareTo(o.name.toLowerCase());
        else
            throw new IllegalArgumentException();
    }
}
