package perihelion.io.fourply;

import android.app.Dialog;
import android.app.DialogFragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;

import perihelion.io.fourply.data.Bathroom;

/**
 * Created by vincente on 1/23/16
 */
public class ReviewFragment extends DialogFragment implements View.OnClickListener{

    private String id;
    private String image;

    public static ReviewFragment createInstance(String bathroomId, String imagePath){
        ReviewFragment fragment = new ReviewFragment();
        Bundle args = new Bundle();
        args.putString("id", bathroomId);
        args.putString("image", imagePath);
        fragment.setArguments(args);
        return fragment;
    }

    public static ReviewFragment createInstance(Bathroom bathroom){
        return createInstance(bathroom.getObjectId(), bathroom.getHeroImage());
    }

    public ReviewFragment(){};

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle args = getArguments();
        id = args.getString("id");
        image = args.getString("image");
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_review_dialog, null);
        Picasso.with(getActivity()).load(image).into((ImageView) view.findViewById(R.id.iv_bathroom));

        view.findViewById(R.id.btn_cancel).setOnClickListener(this);
        view.findViewById(R.id.btn_ok).setOnClickListener(this);
        return view;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        return super.onCreateDialog(savedInstanceState);
    }

    @Override
    public void onClick(View v) {
        switch(v.getId()){
            case R.id.btn_ok:

                break;
            case R.id.btn_cancel:
                dismiss();
                break;
        }
    }
}
