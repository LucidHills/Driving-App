/*
 * Copyright (C) 2014 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package mad.com.applicationproject.activity;

import android.content.Context;
import android.graphics.Outline;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewOutlineProvider;

import java.util.ArrayList;
import java.util.List;

import mad.com.applicationproject.R;
import mad.com.applicationproject.objects.ObdDragView;
import mad.com.applicationproject.io.ObdCommandJob;
import mad.com.applicationproject.io.ObdServiceListener;

public class DragFragment extends Fragment implements DragFrameLayout.DragFrameLayoutController, ObdServiceListener {

    public static final String TAG = "DragFragment";

    private Context mContext;
    private DragFrameLayout mRootView;
    private LayoutInflater mInflater;

    final private List<View> mDragViews = new ArrayList<>();

    /* The circular outline provider */
    private ViewOutlineProvider mOutlineProviderCircle = new CircleOutlineProvider();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        mRootView = (DragFrameLayout) inflater
                .inflate(R.layout.drag_view_fragment, container, false);

        mInflater = inflater;

        addDragView(mRootView.findViewById(R.id.circle));
        addDragView(mRootView.findViewById(R.id.circle2));
        addDragView(mRootView.findViewById(R.id.circle3));

        addDragView(mRootView.findViewById(R.id.drag_obd_item));
//        addObdView();

//        DragFrameLayout layout = (DragFrameLayout) mRootView.findViewById(R.id.drag_frame_layout);
        mRootView.setDragFrameController(this);

//        // register context menu to an invisible view that spans the whole screen
//        registerForContextMenu(mRootView.findViewById(R.id.background));

        return mRootView;
    }

    @Override
    public void onDragDrop(View view, boolean captured) {
        view.animate()
                .translationZ(captured ? 50 : 0)
                .setDuration(100);
        Log.v(TAG, captured ? "Drag" : "Drop");
    }

    @Override
    public void onCommandComplete(ObdCommandJob cmdJob) {
        // listener from OBD service to update ObdDragViews
    }

    private void addDragView(View view) {
        mDragViews.add(view);
        view.setOutlineProvider(mOutlineProviderCircle);
        view.setClipToOutline(true);
        mRootView.addDragView(view);
    }

    private void addObdView() {
        final ViewGroup obdDragView = new ObdDragView(getContext(),
                (ViewGroup) mRootView.findViewById(R.id.drag_obd_item));
//        obdDragView.setCommand(new SpeedCommand());
        addDragView(obdDragView);
    }

//    @Override
//    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
//        super.onCreateContextMenu(menu, v, menuInfo);
//        menu.clear();
//
//        String[] menuItems =
//                mDragViews.contains(v)
//                ? getResources().getStringArray(R.array.context_menu_drag_view)
//                : getResources().getStringArray(R.array.context_menu_drag_root);
//
//        int id  = 0;
//        int pos = 0;
//        for (String item : menuItems) {
//            menu.add(Menu.NONE, id, pos, item);
//            id++;
//            pos++;
//        }
//    }


    /**
     * ViewOutlineProvider which sets the outline to be an oval which fits the view bounds.
     */
    private class CircleOutlineProvider extends ViewOutlineProvider {
        @Override
        public void getOutline(View view, Outline outline) {
            outline.setOval(0, 0, view.getWidth(), view.getHeight());
        }
    }

}