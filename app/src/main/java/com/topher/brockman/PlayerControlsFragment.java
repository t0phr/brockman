package com.topher.brockman;

import android.os.Bundle;
import android.support.v17.leanback.app.PlaybackOverlayFragment;
import android.support.v17.leanback.widget.*;
import com.topher.brockman.api.Broadcast;
import com.topher.brockman.api.Playable;


/**
 * Created by topher on 21/07/16.
 */
public class PlayerControlsFragment extends PlaybackOverlayFragment
        implements OnActionClickedListener {

    private Playable mVideo;
    private PlaybackGlue mGlue;
    private ArrayObjectAdapter mRowsAdapter;
    private PlaybackControlsRowPresenter mPresenter;


    public Playable getVideo() {
        return mVideo;
    }

    @Override
    public void onActionClicked(Action action) {
        mGlue.onActionClicked(action);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setBackgroundType(PlaybackOverlayFragment.BG_NONE);

        mVideo = (Playable) getActivity().getIntent()
                .getSerializableExtra(MainFragment.EXTRA_VIDEO);

        mGlue = new PlaybackGlue(this.getContext(), this);

        mPresenter = mGlue.createControlsRowAndPresenter();
        mPresenter.setOnActionClickedListener(this);
        mPresenter.setBackgroundColor(
                getContext().getColor(R.color.playback_row_bg_color));
        mPresenter.setProgressColor(
                getContext().getColor(R.color.playback_row_progress_color));

        ClassPresenterSelector ps = new ClassPresenterSelector();
        ps.addClassPresenter(PlaybackControlsRow.class, mPresenter);
        ps.addClassPresenter(ListRow.class, new ListRowPresenter());

        mRowsAdapter = new ArrayObjectAdapter(ps);
        mRowsAdapter.add(mGlue.getControlsRow());

        setAdapter(mRowsAdapter);

        mGlue.enableProgressUpdating(true);
    }

    public ArrayObjectAdapter getRowsAdapter() {
        return mRowsAdapter;
    }

    @Override
    public void onPause() {
        super.onPause();
        mGlue.pauseView();
    }
}
