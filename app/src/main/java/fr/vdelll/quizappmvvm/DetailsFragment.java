package fr.vdelll.quizappmvvm;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;

public class DetailsFragment extends Fragment implements View.OnClickListener {

    private NavController navController;
    private QuizListViewModel quizListViewModel;
    private int position;
    private String quizId;
    private long totalQuestions = 0;

    private FirebaseFirestore firebaseFirestore;
    private FirebaseAuth firebaseAuth;

    private ImageView detailsImage;
    private TextView detailsTitle;
    private TextView detailsDesc;
    private TextView detailsDiff;
    private TextView detailsQuestions;
    private TextView detailsScore;

    private Button detailsStartBtn;

    public DetailsFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_details, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        navController = Navigation.findNavController(view);
        position = DetailsFragmentArgs.fromBundle(getArguments()).getPosition();

        // Initialize UI elements
        detailsImage = view.findViewById(R.id.details_image);
        detailsTitle = view.findViewById(R.id.details_title);
        detailsDesc = view.findViewById(R.id.details_desc);
        detailsDiff = view.findViewById(R.id.details_difficulty_text);
        detailsQuestions = view.findViewById(R.id.details_questions_text);
        detailsScore = view.findViewById(R.id.details_score_text);

        detailsStartBtn = view.findViewById(R.id.details_start_btn);
        detailsStartBtn.setOnClickListener(this);

        firebaseAuth = FirebaseAuth.getInstance();
        firebaseFirestore = FirebaseFirestore.getInstance();

    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        quizListViewModel = new ViewModelProvider(getActivity()).get(QuizListViewModel.class);
        quizListViewModel.getQuizListModelData().observe(getViewLifecycleOwner(), new Observer<List<QuizListModel>>() {
            @Override
            public void onChanged(List<QuizListModel> quizListModelList) {

                Glide.with(getContext())
                        .load(quizListModelList.get(position).getImage())
                        .centerCrop()
                        .placeholder(R.drawable.placeholder_image)
                        .into(detailsImage);

                detailsTitle.setText(quizListModelList.get(position).getName());
                detailsDesc.setText(quizListModelList.get(position).getDesc());
                detailsDiff.setText(quizListModelList.get(position).getLevel());
                detailsQuestions.setText(quizListModelList.get(position).getQuestions() + "");

                // Assign value to quiz id variable
                quizId = quizListModelList.get(position).getQuiz_id();
                totalQuestions = quizListModelList.get(position).getQuestions();

                loadResultsData();

            }
        });
    }

    private void loadResultsData() {
        // Get results
        firebaseFirestore.collection("QuizList")
                .document(quizId)
                .collection("Results")
                .document(firebaseAuth.getCurrentUser().getUid())
                .get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()){
                            DocumentSnapshot document = task.getResult();
                            if (document != null && document.exists()){
                                Long correct = document.getLong("correct");
                                Long wrong = document.getLong("wrong");
                                Long missed = document.getLong("unanswered");

                                // Calculate progress
                                Long total = correct + wrong + missed;
                                Long percent = (correct*100)/ total;

                                detailsScore.setText(percent + "%");
                            }
                        }
                    }
                });
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.details_start_btn :
                DetailsFragmentDirections.ActionDetailsFragmentToQuizFragment action = DetailsFragmentDirections.actionDetailsFragmentToQuizFragment();
                action.setTotalQuestions(totalQuestions);
                action.setQuizId(quizId);
                navController.navigate(action);
                break;
        }
    }
}