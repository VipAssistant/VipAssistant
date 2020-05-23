package com.vipassistant.mobile.demo.ui.utils;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import com.vipassistant.mobile.demo.R;

public class HomeArrayAdapter extends BaseAdapter {

	private LayoutInflater inflater;
	private String[]  Title;
	private int[] imge;

	public HomeArrayAdapter(LayoutInflater inflater, String[] text1, int[] imageIds) {
		this.inflater = inflater;
		Title = text1;
		imge = imageIds;
	}

	public int getCount() {
		// TODO Auto-generated method stub
		return Title.length;
	}

	public Object getItem(int arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	public long getItemId(int position) {
		// TODO Auto-generated method stub
		return position;
	}

	public View getView(int position, View convertView, ViewGroup parent) {
		View row;
		row = inflater.inflate(R.layout.row, parent, false);
		TextView title;
		ImageView i1;
		i1 = (ImageView) row.findViewById(R.id.imgIcon);
		title = (TextView) row.findViewById(R.id.txtTitle);
		title.setText(Title[position]);
		i1.setImageResource(imge[position]);

		return (row);
	}
}