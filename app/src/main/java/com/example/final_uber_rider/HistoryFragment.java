package com.example.final_uber_rider;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import com.example.final_uber_rider.model.HistoryAdapter;
import com.example.final_uber_rider.model.RiderInfoModel;
import com.example.final_uber_rider.model.TripPlanModel;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;

public class HistoryFragment extends Fragment {

    @BindView(R.id.rv_list_history)
    RecyclerView rv_list_history;

    private AppCompatActivity mActivity;

    @Override
    public void onCreate(@Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mActivity = (AppCompatActivity) getActivity();
    }

    @Nullable
    @org.jetbrains.annotations.Nullable
    @Override
    public View onCreateView(@NonNull @NotNull LayoutInflater inflater,
                             @Nullable @org.jetbrains.annotations.Nullable ViewGroup container,
                             @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        View history = inflater.inflate(R.layout.fragment_history, container, false);

        ButterKnife.bind(this, history);

        return history;
    }

    @Override
    public void onViewCreated(@NonNull @NotNull View view,
                              @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        onLoadHistoryData();
    }

    private void onLoadHistoryData(){
        HistoryAdapter adapter = new HistoryAdapter(initListData());
        rv_list_history.setAdapter(adapter);
    }

    /*
    * Setup list data Trip history
    * */
    private ArrayList<TripPlanModel> initListData(){
        ArrayList<TripPlanModel> listTest = new ArrayList<>();

        for (int i = 0; i < 10; i++){
            listTest.add(new TripPlanModel("", "", null,
                    new RiderInfoModel("" + i, "Test ", "+19999999999", "", 5.0),
                    "", "", "","","",
                    "","","",0.0,0.0, true, false));
        }

        return listTest;
    }
}
