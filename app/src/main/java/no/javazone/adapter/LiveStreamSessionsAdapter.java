package no.javazone.adapter;

import android.app.Activity;
import android.graphics.drawable.ColorDrawable;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

import no.javazone.R;
import no.javazone.archframework.model.data.SessionData;
import no.javazone.ui.widget.recyclerview.UpdatableAdapter;
import no.javazone.util.ImageLoader;

public class LiveStreamSessionsAdapter
        extends UpdatableAdapter<List<SessionData>, LiveStreamSessionsAdapter.VideoViewHolder> {

    private final Activity mHost;

    private final LayoutInflater mInflater;

    private final ImageLoader mImageLoader;

    private final ColorDrawable[] mBackgroundColors;

    private final List<SessionData> mSessions;

    public LiveStreamSessionsAdapter(@NonNull Activity activity,
                                     @NonNull List<SessionData> liveSessions,
                                     @NonNull ImageLoader imageLoader) {
        mHost = activity;
        mInflater = LayoutInflater.from(activity);
        mImageLoader = imageLoader;
        mSessions = liveSessions;

        // load the background colors
        int[] colors = mHost.getResources().getIntArray(R.array.session_tile_backgrounds);
        mBackgroundColors = new ColorDrawable[colors.length];
        for (int i = 0; i < colors.length; i++) {
            mBackgroundColors[i] = new ColorDrawable(colors[i]);
        }
    }

    @Override
    public VideoViewHolder onCreateViewHolder(final ViewGroup parent, final int viewType) {
        final VideoViewHolder holder = new VideoViewHolder(
                mInflater.inflate(R.layout.video_item_list_tile, parent, false));
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                final SessionData  session = mSessions.get(holder.getAdapterPosition());
                // TODO show vimeo webview
            }
        });
        return holder;
    }

    @Override
    public void onBindViewHolder(final VideoViewHolder holder, final int position) {
        final SessionData session = mSessions.get(position);
        holder.itemView.setBackgroundDrawable(
                mBackgroundColors[position % mBackgroundColors.length]);
       // mImageLoader.loadImage(session.getImageUrl(), holder.thumbnail);
        holder.title.setText(session.getSessionName());
    }

    @Override
    public int getItemCount() {
        return mSessions.size();
    }

    @Override
    public void update(@NonNull final List<SessionData> updatedData) {
        // No-op for this class; live stream sessions don't have update-able state
    }

    static class VideoViewHolder extends RecyclerView.ViewHolder {

       // final VideoThumbnail thumbnail;
        final TextView title;

        public VideoViewHolder(final View itemView) {
            super(itemView);
           // thumbnail = (VideoThumbnail) itemView.findViewById(R.id.thumbnail);
            title = (TextView) itemView.findViewById(R.id.title);
        }
    }
}