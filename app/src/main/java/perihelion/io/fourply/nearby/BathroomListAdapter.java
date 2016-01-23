package perihelion.io.fourply.nearby;

import android.app.Activity;
import android.app.ActivityOptions;
import android.content.Intent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

import perihelion.io.fourply.BathroomActivity;
import perihelion.io.fourply.R;
import perihelion.io.fourply.data.Bathroom;

/**
 * Created by david on 1/22/16.
 */
public class BathroomListAdapter extends BaseAdapter {

    private List<Bathroom> mBathrooms = new ArrayList<>();
    private Activity mActivity;

    public BathroomListAdapter(Activity activity, List<Bathroom> bathrooms) {
        mBathrooms = bathrooms;
        mActivity = activity;
    }

    @Override
    public int getCount() {
        return mBathrooms.size();
    }

    @Override
    public Object getItem(int i) {
        return mBathrooms.get(i);
    }

    @Override
    public long getItemId(int i) {
        return 0;
    }

    @Override
    public View getView(int position, View view, ViewGroup viewGroup) {
        final ViewHolder holder;
        if (view != null) {
            holder = (ViewHolder) view.getTag();
        } else {
            view = View.inflate(mActivity, R.layout.bathroom_list_item, null);
            holder = new ViewHolder(view);
            view.setTag(holder);
        }
        final Bathroom bathroom = mBathrooms.get(position);
        holder.name.setText(bathroom.getName());
        holder.description.setText(bathroom.getDescription());
        Picasso.with(mActivity).load(bathroom.getHeroImage()).into(holder.heroImage);
        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent bathroomIntent = new Intent(mActivity, BathroomActivity.class);
                bathroomIntent.putExtra("name", bathroom.getName());
                bathroomIntent.putExtra("id", bathroom.getObjectId());
                ActivityOptions options = ActivityOptions.makeSceneTransitionAnimation(mActivity, holder.heroImage, "heroImage");
                mActivity.startActivity(bathroomIntent, options.toBundle());
            }
        });
        return view;
    }

    static class ViewHolder {
        TextView name;
        TextView description;
        ImageView heroImage;

        public ViewHolder(View view) {
            name = (TextView) view.findViewById(R.id.name);
            description = (TextView) view.findViewById(R.id.description);
            heroImage = (ImageView) view.findViewById(R.id.heroImage);
        }
    }
}
