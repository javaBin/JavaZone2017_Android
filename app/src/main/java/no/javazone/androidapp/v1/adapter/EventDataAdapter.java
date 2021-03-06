package no.javazone.androidapp.v1.adapter;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;

import no.javazone.androidapp.v1.R;
import no.javazone.androidapp.v1.archframework.model.data.EventCard;
import no.javazone.androidapp.v1.ui.activity.MapActivity;
import no.javazone.androidapp.v1.ui.activity.SessionDetailActivity;
import no.javazone.androidapp.v1.ui.widget.recyclerview.UpdatableAdapter;
import no.javazone.androidapp.v1.util.ActivityUtils;

public class EventDataAdapter
        extends UpdatableAdapter<List<EventCard>, EventDataAdapter.EventCardViewHolder> {

    private final Activity mHost;

    private final LayoutInflater mInflater;

    private final ColorDrawable[] mBackgroundColors;

    private final List<EventCard> mCards;

    public EventDataAdapter(@NonNull Activity activity,
                            @NonNull List<EventCard> eventCards) {
        mHost = activity;
        mInflater = LayoutInflater.from(activity);
        mCards = eventCards;

        // load the background colors
        int[] colors = mHost.getResources().getIntArray(R.array.session_tile_backgrounds);
        mBackgroundColors = new ColorDrawable[colors.length];
        for (int i = 0; i < colors.length; i++) {
            mBackgroundColors[i] = new ColorDrawable(colors[i]);
        }
    }

    @Override
    public EventCardViewHolder onCreateViewHolder(final ViewGroup parent, final int viewType) {
        return new EventCardViewHolder(
                mInflater.inflate(R.layout.explore_io_event_data_item_list_tile, parent, false));
    }

    @Override
    public void onBindViewHolder(final EventCardViewHolder holder, final int position) {
        final EventCard card = mCards.get(position);
        holder.itemView.setBackgroundDrawable(
                mBackgroundColors[position % mBackgroundColors.length]);
        holder.mCardContent = mCards.get(position);
        holder.mTitleView.setText(card.getDescription());
        holder.mActionNameView.setText(card.getActionString());
    }

    @Override
    public int getItemCount() {
        return mCards.size();
    }

    @Override
    public void update(@NonNull final List<EventCard> eventCards) {
        // No-op for this class; no update-able state
    }

    public class EventCardViewHolder extends RecyclerView.ViewHolder {
        final TextView mTitleView;
        final TextView mActionNameView;
        EventCard mCardContent;
        public EventCardViewHolder(View itemView) {
            super(itemView);
            mTitleView = (TextView) itemView.findViewById(R.id.title_text);
            mActionNameView = (TextView) itemView.findViewById(R.id.action_text);
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(final View v) {
                    if (mCardContent != null && mCardContent.isValid()) {
                        if (EventCard.ACTION_TYPE_LINK.equalsIgnoreCase(mCardContent.getActionType())) {
                            try {
                                Intent myIntent =
                                        new Intent(Intent.ACTION_VIEW,
                                                Uri.parse(mCardContent.getActionUrl()));
                                mHost.startActivity(myIntent);
                                return;
                            } catch (ActivityNotFoundException e) {
                                Toast.makeText(mHost, "Browser not available.", Toast.LENGTH_LONG)
                                        .show();
                            }
                        }
                        if (EventCard.ACTION_TYPE_MAP.equalsIgnoreCase(mCardContent.getActionType())) {
                            ActivityUtils.createBackStack(mHost,
                                    new Intent(mHost, MapActivity.class));
                            mHost.finish();
                            return;
                        }
                        if (EventCard.ACTION_TYPE_SESSION.equalsIgnoreCase(mCardContent.getActionType())) {
                            SessionDetailActivity.startSessionDetailActivity(mHost, mCardContent.getActionExtra());
                        }
                    }
                }
            });
        }
    }
}

