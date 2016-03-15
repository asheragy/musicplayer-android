package org.cerion.musicplayer.navigation;


import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import org.cerion.musicplayer.R;
import org.cerion.musicplayer.navigation.NavigationListItem;

import java.util.List;

public class NavigationListAdapter extends ArrayAdapter<NavigationListItem>  {

    public NavigationListAdapter(Context context) {
        super(context, R.layout.list_item);
    }

    public void setData(List<NavigationListItem> items) {
        clear();
        addAll(items);
        notifyDataSetChanged();
    }

    private static class ViewHolder {
        TextView title;
        TextView info;
        ImageView image;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        ViewHolder holder;
        if(convertView == null) {
            LayoutInflater inflater = ((Activity)getContext()).getLayoutInflater();
            convertView = inflater.inflate(R.layout.list_item, parent, false);

            holder = new ViewHolder();
            holder.image = (ImageView)convertView.findViewById(R.id.image);
            holder.title = (TextView)convertView.findViewById(R.id.title);
            holder.info = (TextView)convertView.findViewById(R.id.info);

            convertView.setTag(holder);
        } else
            holder = (ViewHolder)convertView.getTag();

        NavigationListItem item = getItem(position);
        holder.title.setText(item.title);
        holder.info.setText(item.info);
        holder.image.setVisibility( item.isFolder() ? View.VISIBLE : View.GONE );

        return convertView;
    }


}
