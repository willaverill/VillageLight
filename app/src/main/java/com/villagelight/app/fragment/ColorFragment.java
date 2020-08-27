package com.villagelight.app.fragment;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;

import com.villagelight.app.R;
import com.villagelight.app.adapter.ColorAdapter;
import com.villagelight.app.model.ColorBean;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

public class ColorFragment extends Fragment {
    private static final String ARG_PARAM = "colors";
    @BindView(R.id.gv)
    GridView mGv;
    Unbinder unbinder;

    private ArrayList<ColorBean> mColors;

    public ColorFragment() {
        // Required empty public constructor
    }

    public static ColorFragment newInstance(ArrayList<ColorBean> colors) {
        ColorFragment fragment = new ColorFragment();
        Bundle args = new Bundle();
        args.putSerializable(ARG_PARAM, colors);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mColors = (ArrayList<ColorBean>) getArguments().getSerializable(ARG_PARAM);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_color, container, false);
        unbinder = ButterKnife.bind(this, view);

        mGv.setAdapter(new ColorAdapter(getActivity(), mColors));

        mGv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                ColorBean color = mColors.get(position);

                if (!color.getName().equalsIgnoreCase("empty")){
                    Intent intent = new Intent();
                    intent.putExtra("displayColor", color.getDisplayColor());
                    intent.putExtra("sendColor", color.getSendColor());
                    intent.putExtra("colorNo", color.getColorNo());
                    getActivity().setResult(Activity.RESULT_OK, intent);
                    getActivity().finish();
                }

            }
        });

        return view;
    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
    }
}
