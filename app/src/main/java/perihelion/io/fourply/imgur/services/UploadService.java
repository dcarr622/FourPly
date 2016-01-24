package perihelion.io.fourply.imgur.services;

import android.content.Context;

import com.parse.ParseACL;
import com.parse.ParseUser;

import java.lang.ref.WeakReference;

import perihelion.io.fourply.data.Bathroom;
import perihelion.io.fourply.imgur.Constants;
import perihelion.io.fourply.imgur.helpers.NotificationHelper;
import perihelion.io.fourply.imgur.imgurmodel.ImageResponse;
import perihelion.io.fourply.imgur.imgurmodel.ImgurAPI;
import perihelion.io.fourply.imgur.imgurmodel.Upload;
import perihelion.io.fourply.imgur.utils.NetworkUtils;
import retrofit.Callback;
import retrofit.RestAdapter;
import retrofit.RetrofitError;
import retrofit.client.Response;
import retrofit.mime.TypedFile;

/**
 * Created by AKiniyalocts on 1/12/15.
 * <p/>
 * Our upload service. This creates our restadapter, uploads our image, and notifies us of the response.
 */
public class UploadService {
    public final static String TAG = UploadService.class.getSimpleName();

    private WeakReference<Context> mContext;

    private String mName;
    private String mDescription;
    private float mLng;
    private float mLat;

    public UploadService(Context context, String name, String description, float lat, float lng) {
        this.mContext = new WeakReference<>(context);
        mName = name;
        mDescription = description;
        mLat = lat;
        mLng = lng;
    }

    public void Execute(Upload upload, Callback<ImageResponse> callback) {
        final Callback<ImageResponse> cb = callback;

        if (!NetworkUtils.isConnected(mContext.get())) {
            //Callback will be called, so we prevent a unnecessary notification
            cb.failure(null);
            return;
        }

        final NotificationHelper notificationHelper = new NotificationHelper(mContext.get());
        notificationHelper.createUploadingNotification();

        RestAdapter restAdapter = buildRestAdapter();

        restAdapter.create(ImgurAPI.class).postImage(
                Constants.getClientAuth(),
                upload.title,
                upload.description,
                upload.albumId,
                null,
                new TypedFile("image/*", upload.image),
                new Callback<ImageResponse>() {
                    @Override
                    public void success(ImageResponse imageResponse, Response response) {
                        if (cb != null) cb.success(imageResponse, response);
                        if (response == null) {
                            /*
                             Notify image was NOT uploaded successfully
                            */
                            notificationHelper.createFailedUploadNotification();
                            return;
                        }
                        /*
                        Notify image was uploaded successfully
                        */
                        if (imageResponse.success) {
                            notificationHelper.createUploadedNotification(imageResponse);
                            String url = imageResponse.data.link;
                            Bathroom room = new Bathroom(mName, mDescription, url, mLat, mLng);
                            room.setACL(new ParseACL(ParseUser.getCurrentUser()));
                            room.getACL().setPublicReadAccess(true);
                            room.getACL().setPublicWriteAccess(true);
                            room.saveInBackground();
                        }
                    }

                    @Override
                    public void failure(RetrofitError error) {
                        if (cb != null) cb.failure(error);
                        notificationHelper.createFailedUploadNotification();
                    }
                });
    }

    private RestAdapter buildRestAdapter() {
        RestAdapter imgurAdapter = new RestAdapter.Builder()
                .setEndpoint(ImgurAPI.server)
                .build();

        /*
        Set rest adapter logging if we're already logging
        */
        if (Constants.LOGGING)
            imgurAdapter.setLogLevel(RestAdapter.LogLevel.FULL);
        return imgurAdapter;
    }
}
