//package com.vipassistant.mobile.demo.View;
//
//import androidx.lifecycle.ViewModelProviders;
//import android.os.Bundle;
//import androidx.annotation.NonNull;
//import androidx.annotation.Nullable;
//import androidx.fragment.app.Fragment;
//import android.view.LayoutInflater;
//import android.view.View;
//import android.view.ViewGroup;
//import com.vipassistant.mobile.demo.R;
//import com.vipassistant.mobile.demo.ViewModel.GameViewModel;
//
//public class GameMainFragment extends Fragment {
//
//    public static GameMainFragment newInstance() {
//        return new GameMainFragment();
//    }
//
//    private GameViewModel mViewModel;
//
//    @Nullable
//    @Override
//    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
//            @Nullable Bundle savedInstanceState) {
//        return inflater.inflate(R.layout.main_fragment, container, false);
//    }
//
//    @Override
//    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
//        super.onActivityCreated(savedInstanceState);
//        mViewModel = ViewModelProviders.of(this).get(GameViewModel.class);
//        // TODO: Use the ViewModel
//    }
//
//}
