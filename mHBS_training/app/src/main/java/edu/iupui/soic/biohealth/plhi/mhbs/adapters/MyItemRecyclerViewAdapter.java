package edu.iupui.soic.biohealth.plhi.mhbs.adapters;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import edu.iupui.soic.biohealth.plhi.mhbs.R;
import edu.iupui.soic.biohealth.plhi.mhbs.documents.DocumentResources.ResourceItem;
import edu.iupui.soic.biohealth.plhi.mhbs.fragments.ItemFragment.OnListFragmentInteractionListener;

/**
 * {@link RecyclerView.Adapter} that can display a {@link ResourceItem} and makes a call to the
 * specified {@link OnListFragmentInteractionListener}.
 * TODO: Replace the implementation with code for your data type.
 */
public class MyItemRecyclerViewAdapter extends RecyclerView.Adapter<MyItemRecyclerViewAdapter.ViewHolder> {

    private static List<ResourceItem> mValues = new ArrayList<>();
    private OnListFragmentInteractionListener mListener;
    private Boolean isPDF;

    public MyItemRecyclerViewAdapter(OnListFragmentInteractionListener mListener) {
        this.mListener = mListener;
    }


    public void addItems(List<ResourceItem> items) {
        mValues = items;
    }


    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.fragment_rowfragment, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        holder.mItem = mValues.get(position);
        holder.mTitleView.setText(mValues.get(position).title);

            if (mValues.get(position).bitmap != null) {

                holder.mThumbnailView.setImageBitmap(mValues.get(position).bitmap);
            } else {
                //bitmap was not retrievable, display default image set in layout xml
                setDefaultImage(holder);
                //TODO: For items that were null, if they are downloaded
                // then we can grab the thumbnail once they are downloaded

        }
        //    holder.mInstitutionView.setText(mValues.get(position).institution);
        //  holder.mIdView.setText(mValues.get(position).id);

        holder.mView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (null != mListener) {
                    // Notify the active callbacks interface (the activity, if the
                    // fragment is attached to one) that an item has been selected.
                    mListener.onListFragmentInteraction(holder.mItem);
                }
            }
        });
    }

    private void setDefaultImage(ViewHolder holder) {
        int resourceId = holder.mThumbnailView.getResources().getIdentifier("mhbs_video_placeholder", "drawable", "edu.iupui.soic.biohealth.plhi.mhbs");
        holder.mThumbnailView.setImageResource(resourceId);
    }

    @Override
    public int getItemCount() {
        return mValues.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public final View mView;
        public final TextView mTitleView;
        public final TextView mInstitutionView;
        public final ImageView mThumbnailView;

        public ResourceItem mItem;

        public ViewHolder(View view) {
            super(view);
            mView = view;
            mTitleView = (TextView) view.findViewById(R.id.titleResource);
            mInstitutionView = (TextView) view.findViewById(R.id.titleLocation);
            mThumbnailView = (ImageView) view.findViewById(R.id.thumbnail);
        }

        @Override
        public String toString() {
            return super.toString();
        }
    }

}
