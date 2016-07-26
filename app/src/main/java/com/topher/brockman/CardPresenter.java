package com.topher.brockman;

import android.content.Context;
import android.support.v17.leanback.widget.ImageCardView;
import android.support.v17.leanback.widget.Presenter;
import android.text.TextUtils;
import android.text.format.DateUtils;
import android.view.View;
import android.view.ViewGroup;
import com.squareup.picasso.Picasso;
import com.topher.brockman.api.TSchau;

import java.util.Date;

/**
 * Created by topher on 20/07/16.
 */
public class CardPresenter extends Presenter {

    public static final int CARD_WIDTH = 500;
    public static final int CARD_HEIGHT = (int) (CARD_WIDTH * (9.0/16));

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
                    .resize(CARD_WIDTH, CARD_HEIGHT)
                    .centerCrop()
                    .into(mCardView.getMainImageView());
        }
    }

    public CardPresenter(Context context) {
        super();
        mContext = context;
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
        TSchau video = (TSchau) item;

        if (!TextUtils.isEmpty(video.getImgUrl())) {
            ((ViewHolder) viewHolder).mCardView
                    .setTitleText(video.getTitle());
            ((ViewHolder) viewHolder).mCardView
                    .setContentText(Utils.getContentDescription(video));
            ((ViewHolder) viewHolder).mCardView
                    .setMainImageDimensions(CARD_WIDTH, CARD_HEIGHT);
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