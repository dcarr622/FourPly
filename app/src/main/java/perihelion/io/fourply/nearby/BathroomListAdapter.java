package perihelion.io.fourply.nearby;

import android.app.Activity;
import android.app.ActivityOptions;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RatingBar;
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
public class BathroomListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> implements View.OnClickListener {

    private List<Bathroom> mBathrooms = new ArrayList<>();
    private Activity mActivity;
    private RecyclerView mRecyclerView;

    public BathroomListAdapter(Activity activity, RecyclerView recyclerView, List<Bathroom> bathrooms) {
        mBathrooms = bathrooms;
        mActivity = activity;
        mRecyclerView = recyclerView;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.bathroom_list_item, parent, false);
        view.setOnClickListener(this);
        return new BathroomViewHolder(view);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder viewHolder, int position) {
        final Bathroom bathroom = mBathrooms.get(position);
        final BathroomViewHolder holder = (BathroomViewHolder) viewHolder;
        holder.name.setText(bathroom.getName());
        holder.description.setText(bathroom.getDescription());
        holder.rating.setRating(bathroom.getAverageReview());
        Picasso.with(mActivity).load(bathroom.getHeroImage()).into(holder.heroImage);
    }

    @Override
    public long getItemId(int i) {
        return 0;
    }

    @Override
    public int getItemCount() {
        return mBathrooms.size();
    }

    @Override
    public void onClick(View view) {
        int position = mRecyclerView.indexOfChild(view);
        final Bathroom bathroom = mBathrooms.get(position);
        Intent bathroomIntent = new Intent(mActivity, BathroomActivity.class);
        bathroomIntent.putExtra("name", bathroom.getName());
        bathroomIntent.putExtra("id", bathroom.getObjectId());
        ActivityOptions options = ActivityOptions.makeSceneTransitionAnimation(mActivity, view.findViewById(R.id.heroImage), "heroImage");
        mActivity.startActivity(bathroomIntent, options.toBundle());
    }

    static class BathroomViewHolder extends RecyclerView.ViewHolder {
        TextView name;
        TextView description;
        ImageView heroImage;
        RatingBar rating;

        public BathroomViewHolder(View view) {
            super(view);
            name = (TextView) view.findViewById(R.id.name);
            description = (TextView) view.findViewById(R.id.description);
            heroImage = (ImageView) view.findViewById(R.id.heroImage);
            rating = (RatingBar) view.findViewById(R.id.tiny_rolls_bar);
        }
    }
}
