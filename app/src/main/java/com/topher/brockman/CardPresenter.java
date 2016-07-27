package com.topher.brockman;

import android.content.Context;
import android.support.v17.leanback.widget.ImageCardView;
import android.support.v17.leanback.widget.Presenter;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import com.squareup.picasso.Picasso;
import com.topher.brockman.api.Broadcast;

/**
 * Created by topher on 20/07/16.
 */
public class CardPresenter extends Presenter {

    private static int cardWidth;
    private static int cardHeight;

    private Context mContext;

    static class ViewHolder extends Presenter.ViewHolder {
        private ImageCardView mCardView;
        public ViewHolder(View view) {
            super(view);
            mCardView = (ImageCardView) view;
        }
        public ImageCardView getCardView() {
            return mCardView;
        }
        public void updateCardViewImage(Context context, String link ) {
            Picasso.with(context).load(link)
                    .resize(cardWidth, cardHeight)
                    .centerCrop()
                    .into(mCardView.getMainImageView());
        }
    }

    public CardPresenter(Context context) {
        super();
        mContext = context;
        cardWidth = (int) mContext.getResources().getDimension(R.dimen.card_width);
        cardHeight = (int) (cardWidth * (9.0/16));
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent) {
        ImageCardView cardView = new ImageCardView( parent.getContext() );
        cardView.setFocusable(true);
        cardView.setBackgroundResource(R.color.card_bg_color);
        return new ViewHolder(cardView);
    }

    @Override
    public void onBindViewHolder(Presenter.ViewHolder viewHolder,
                                 Object item) {
        Broadcast video = (Broadcast) item;

        if (!TextUtils.isEmpty(video.getImgUrl())) {
            ((ViewHolder) viewHolder).mCardView
                    .setTitleText(video.getTitle());
            ((ViewHolder) viewHolder).mCardView
                    .setContentText(Utils.getContentDescription(video));
            ((ViewHolder) viewHolder).mCardView
                    .setMainImageDimensions(cardWidth, cardHeight);
            ((ViewHolder) viewHolder).mCardView
                    .setInfoAreaBackgroundColor(mContext
                            .getColor(R.color.card_info_bg_color));
            ((ViewHolder) viewHolder).updateCardViewImage(((ViewHolder) viewHolder)
                    .getCardView().getContext(), video.getImgUrl());
        }
    }

    @Override
    public void onUnbindViewHolder(Presenter.ViewHolder viewHolder) {
    }

    @Override
    public void onViewAttachedToWindow(Presenter.ViewHolder viewHolder) {
    }
}