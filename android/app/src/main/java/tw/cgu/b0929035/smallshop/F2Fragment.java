package tw.cgu.b0929035.smallshop;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.widget.AppCompatImageView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

import tw.cgu.b0929035.smallshop.Activity.ChatActivity;
import tw.cgu.b0929035.smallshop.adapters.UsersAdapter;
import tw.cgu.b0929035.smallshop.listerners.UserListener;
import tw.cgu.b0929035.smallshop.models.User;
import tw.cgu.b0929035.smallshop.utilities.Constants;
import tw.cgu.b0929035.smallshop.utilities.PreferenceManager;

public class F2Fragment extends Fragment implements UserListener {

    private PreferenceManager preferenceManager;
    private RecyclerView usersRecyclerView;
    TextView ErrorMessage;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_f2, container, false);
        if (getContext() != null) {
            preferenceManager = new PreferenceManager(getContext());
        } else {
            return rootView;
        }
        usersRecyclerView = rootView.findViewById(R.id.usersRecyclerView);
        ErrorMessage = rootView.findViewById(R.id.textErrorMessage);
        getUsersChatHistory();

        return rootView;
    }
    @Override
    public void onUserClicked(User user) {
        Intent intent = new Intent(getContext(), ChatActivity.class);
        intent.putExtra(Constants.KEY_USER, user);
        startActivity(intent);
    }

    private void getUsersFromUserIds(List<String> userIds) {
        FirebaseFirestore database = FirebaseFirestore.getInstance();
        List<User> users = new ArrayList<>();
        for (String userId : userIds) {
            database.collection(Constants.KEY_COLLECTION_USERS)
                    .document(userId)
                    .get()
                    .addOnCompleteListener(userTask -> {
                        if (userTask.isSuccessful() && userTask.getResult() != null) {
                            DocumentSnapshot userSnapshot = userTask.getResult();
                            User user = new User();
                            user.name = userSnapshot.getString(Constants.KEY_NAME);
                            user.email = userSnapshot.getString(Constants.KEY_EMAIL);
                            user.token = userSnapshot.getString(Constants.KEY_FCM_TOKEN);
                            user.id = userSnapshot.getId();
                            users.add(user);

                            if (users.size() > 0) {
                                UsersAdapter usersAdapter = new UsersAdapter(users, (UserListener) this);
                                usersRecyclerView.setAdapter(usersAdapter);
                                usersRecyclerView.setVisibility(View.VISIBLE);
                            } else {
                                ErrorMessage.setText("No users available with chat history");
                            }
                        }
                    });
        }
    }
    private void getUsersChatHistory() {
        FirebaseFirestore database = FirebaseFirestore.getInstance();
        String currentUserId = preferenceManager.getString(Constants.KEY_USER_ID);

        database.collection(Constants.KEY_COLLECTION_CHAT)
                .whereEqualTo(Constants.KEY_SENDER_ID, currentUserId)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        List<String> userIds = new ArrayList<>();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            String receiverId = document.getString(Constants.KEY_RECEIVER_ID);
                            if (!userIds.contains(receiverId) && !currentUserId.equals(receiverId)) {
                                userIds.add(receiverId);
                            }
                        }
                        getUsersFromUserIds(userIds);
                    } else {
                        ErrorMessage.setText("No users available");
                    }
                });
        database.collection(Constants.KEY_COLLECTION_CHAT)
                .whereEqualTo(Constants.KEY_RECEIVER_ID, currentUserId)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        List<String> userIds = new ArrayList<>();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            String senderId = document.getString(Constants.KEY_SENDER_ID);
                            if (!userIds.contains(senderId) && !currentUserId.equals(senderId)) {
                                userIds.add(senderId);
                            }
                        }
                        getUsersFromUserIds(userIds);
                    } else {
                        ErrorMessage.setText("No users available");
                    }
                });
    }
}