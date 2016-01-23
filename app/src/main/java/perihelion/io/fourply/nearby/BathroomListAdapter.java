package perihelion.io.fourply.nearby;

import android.app.Activity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import java.util.ArrayList;
import java.util.List;

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
    public View getView(int i, View view, ViewGroup viewGroup) {
        return view;
    }
}
