package com.topher.brockman;

import android.os.Bundle;
import android.support.v17.leanback.app.PlaybackOverlayFragment;
import android.support.v17.leanback.widget.*;
import android.widget.Toast;
import com.topher.brockman.api.TSchau;
import com.topher.brockman.api.Video;

/**
 * Created by topher on 21/07/16.
 */
public class PlayerControlsFragment extends PlaybackOverlayFragment
        implements OnActionClickedListener {

    private PlayerControlsListener mControlsCallback;
    private TSchau mVideo;

    private ArrayObjectAdapter mRowsAdapter;
    private ArrayObjectAdapter mPrimaryActionsAdapter;
    private ArrayObjectAdapter mSecondaryActionsAdapter;
    private PlaybackControlsRow mPlaybackControlsRow;
    private PlaybackControlsRow.PlayPauseAction mPlayPauseAction;
    private PlaybackControlsRow.RepeatAction mRepeatAction;
    private PlaybackControlsRow.ShuffleAction mShuffleAction;
    private PlaybackControlsRow.FastForwardAction mFastForwardAction;
    private PlaybackControlsRow.RewindAction mRewindAction;
    private PlaybackControlsRow.SkipNextAction mSkipNextAction;
    private PlaybackControlsRow.SkipPreviousAction mSkipPreviousAction;
    private PlaybackControlsRow.HighQualityAction mHighQualityAction;
    private PlaybackControlsRow.ClosedCaptioningAction mClosedCaptionAction;

    @Override
    public void onActionClicked(Action action) {
        if(action.getId() == mPlayPauseAction.getId()) {
            if(mPlayPauseAction.getIndex()
                    == PlaybackControlsRow.PlayPauseAction.PLAY) {
                setFadingEnabled(true);
                mControlsCallback.play();
                mRowsAdapter.notifyArrayItemRangeChanged(0, 1);
            } else {
                setFadingEnabled( false );
                mControlsCallback.pause();
            }
            ((PlaybackControlsRow.MultiAction) action).nextIndex();
            mPrimaryActionsAdapter.notifyArrayItemRangeChanged(
                    mPrimaryActionsAdapter.indexOf(action), 1);
        } else {
            Toast.makeText( getActivity(), "Other action",
                    Toast.LENGTH_SHORT ).show();
        } }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setBackgroundType(PlaybackOverlayFragment.BG_DARK);
        setFadingEnabled(true);

        mControlsCallback = (PlayerControlsListener) getActivity();
        mVideo = (TSchau) getActivity().getIntent()
                .getSerializableExtra(MainFragment.EXTRA_VIDEO);
        setupPlaybackControlsRow();
        setupPresenter();
        initActions();
        setupPrimaryActionsRow();
        setupSecondaryActionsRow();
        setAdapter(mRowsAdapter);
    }

    private void initActions() {
        mPlayPauseAction =
                new PlaybackControlsRow.PlayPauseAction(getActivity());
        mRepeatAction =
                new PlaybackControlsRow.RepeatAction(getActivity());
        mShuffleAction =
                new PlaybackControlsRow.ShuffleAction(getActivity());
        mSkipNextAction =
                new PlaybackControlsRow.SkipNextAction(getActivity());
        mSkipPreviousAction =
                new PlaybackControlsRow.SkipPreviousAction(getActivity());
        mFastForwardAction =
                new PlaybackControlsRow.FastForwardAction(getActivity());
        mRewindAction =
                new PlaybackControlsRow.RewindAction(getActivity());
        mHighQualityAction =
                new PlaybackControlsRow.HighQualityAction(getActivity());
        mClosedCaptionAction =
                new PlaybackControlsRow.ClosedCaptioningAction(getActivity());
    }

    private void setupPrimaryActionsRow() {
        mPrimaryActionsAdapter.add(mSkipPreviousAction);
        mPrimaryActionsAdapter.add(mRewindAction);
        mPrimaryActionsAdapter.add(mPlayPauseAction);
        mPrimaryActionsAdapter.add(mFastForwardAction);
        mPrimaryActionsAdapter.add(mSkipNextAction);
    }
    private void setupSecondaryActionsRow() {
        mSecondaryActionsAdapter.add(mRepeatAction);
        mSecondaryActionsAdapter.add(mShuffleAction);
        mSecondaryActionsAdapter.add(mHighQualityAction);
        mSecondaryActionsAdapter.add(mClosedCaptionAction);
    }

    private void setupPlaybackControlsRow() {
        mPlaybackControlsRow = new PlaybackControlsRow( mVideo );
        ControlButtonPresenterSelector presenterSelector =
                new ControlButtonPresenterSelector();
        mPrimaryActionsAdapter = new ArrayObjectAdapter(presenterSelector);
        mSecondaryActionsAdapter = new ArrayObjectAdapter(presenterSelector);
        mPlaybackControlsRow.setPrimaryActionsAdapter(mPrimaryActionsAdapter);
        mPlaybackControlsRow.setSecondaryActionsAdapter(
                mSecondaryActionsAdapter);
    }

    private void setupPresenter() {
        ClassPresenterSelector ps = new ClassPresenterSelector();
        PlaybackControlsRowPresenter playbackControlsRowPresenter =
                new PlaybackControlsRowPresenter( new DescriptionPresenter() );
        playbackControlsRowPresenter.setOnActionClickedListener(this);
        playbackControlsRowPresenter.setSecondaryActionsHidden(false);
        ps.addClassPresenter(PlaybackControlsRow.class,
                playbackControlsRowPresenter);
        ps.addClassPresenter(ListRow.class, new ListRowPresenter());
        mRowsAdapter = new ArrayObjectAdapter(ps);
        mRowsAdapter.add(mPlaybackControlsRow);
    }

    public interface PlayerControlsListener {
        void play();
        void pause();
    }

    static class DescriptionPresenter extends
            AbstractDetailsDescriptionPresenter {
        @Override
        protected void onBindDescription(ViewHolder viewHolder, Object item) {
            viewHolder.getTitle().setText("tagesschau");
        }
    }
}
