package com.apps.interestingapps.multibackground.common;

import java.util.Collections;
import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.apps.interestingapps.multibackground.R;

public class AnimationDetailsAdapter extends ArrayAdapter<AnimationDetails> {

	private Context context;
	private List<AnimationDetails> allAnimationDetails;

	static class ViewHolder {
		public TextView animationTypeName;
	}

	public AnimationDetailsAdapter(Context context,
			List<AnimationDetails> allAnimationDetails) {
		super(context, R.layout.animation_display_row_view, allAnimationDetails);
		this.context = context;
		this.allAnimationDetails = allAnimationDetails;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View rowView = convertView;
		if (rowView == null) {
			LayoutInflater inflater = (LayoutInflater) context
					.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			rowView = inflater.inflate(R.layout.animation_display_row_view,
					parent, false);
			ViewHolder viewHolder = new ViewHolder();
			viewHolder.animationTypeName = (TextView) rowView
					.findViewById(R.id.animationTypeNameTextView);
			rowView.setTag(viewHolder);
		}
		ViewHolder viewHolder = (ViewHolder) rowView.getTag();
		viewHolder.animationTypeName.setText(allAnimationDetails.get(position)
				.getAnimationDisplayName());
		return rowView;
	}

	/**
	 * Sorts the updated data according to the speed dial number
	 */
	@Override
	public void notifyDataSetChanged() {
		super.notifyDataSetChanged();
		Collections.sort(allAnimationDetails);
	}

	/**
	 *
	 * @param animationToRemove
	 */
	@Override
	public void remove(AnimationDetails animationToRemove) {
		super.remove(animationToRemove);
	}
}
